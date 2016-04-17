/* to do
keyboard window control
volume knobs for everything?
invert input (?)
*/
AnasGui {

	classvar <>launcher, launchButton, recompileButton, closeButton,
	configButton, configWindow, reopenButton, pathFields,
	configText, saveConfigButton, <version, <config,
	<anasFolder, <anasDir, <>loadPath, <>recordPath,
	<>netAddress, <>oscMessageSender, isAlwaysOnTop;

	var <>loadPath, <>recordPath, <>window, <clock,
	<>osc1, <>osc2, <>osc3, <>osc4,
	<>osc5, <>out1, <>out2, <>out3,
	<>out4, <>del1, <>mult1, <>adsr1,
	<>adsr2, <>filt1, <>midipanel, <>sampler,
	<>in1, <in2, <>patterns, composite,
	<>saveList, <>savePath, <>whichFolder, <>fileName,
	fileNameField, saveButton, recordButton, openRecordingsButton,
	recordFileName, <>recordPanel, <>loadMenu, <>menuEntries,
	<>folderMenu, <>folderEntries, <>loadPathFolders, <>moduleList,
	<>saves, img, header, closeButton, <moduleObjects,
	<moduleSockets, <midiLockButton, <guiPositions, <guiBounds,
	<oscSend, <selectorMenu;

	*new {^super.new.initAnasGui}

	*initClass {
		version = "ANAS v1.0 - Master";
		this.loadEventTypes;
		version = "ANAS v1.2-change_panels";

		//--------------------------------  initialize file structure ------------------------------------
		anasFolder = PathName(AnasGui.filenameSymbol.asString.dirname);
		anasDir = PathName(AnasGui.filenameSymbol.asString.dirname.dirname);

		////if config file exists, load save and recording paths
		if (File.exists(anasDir.fullPath +/+ "ANASconfig"), {
			config = Object.readArchive(anasDir.fullPath +/+ "ANASconfig");
			loadPath = PathName(config.at(\loadPath));
			recordPath = PathName(config.at(\recordingsPath));
		},
		////if not, prompt the user to configure.
		{
			StartUp.add(
				config = Dictionary.new;
				{{0.5.wait; "You must configure ANAS before starting! Click below to configure.".postln;}.fork;}
			);
		}
		);

		////if there's nothing in the save folder, make a subfolder
		if (loadPath.notNil, {
			if (loadPath.folders.size == 0, {
				File.mkdir(loadPath.fullPath +/+ "folder1");
			});
		});
		// ------------------------------------ !  initialize file structure ---------------------------

		Window.closeAll; //close all windows
		this.loadEventTypes; //load events for Patterns library control
		{0.5.wait; (version ++ " installed").postln;}.fork; //startup blip to indicate ANAS Version
		launcher = ANASLauncher.new; //open Launcher
		// Set always on top or not
		if(config.at(\alwaysOnTop).isNil,{
			isAlwaysOnTop = true;
		},{
			switch(config.at(\alwaysOnTop),
				0, {isAlwaysOnTop = true},
				1, {isAlwaysOnTop = false},
			);
		});
		launcher.launcher.front.alwaysOnTop = isAlwaysOnTop;
		//end INITCLASS
	}

	*openLauncher {
		this.initClass;
	}

	*closeLauncher {
		launcher.close;
	}

	*loadEventTypes {
		Event.addEventType(\anasNote, {
			if (~osc.isSymbol, {
				Ndef(~osc.asSymbol).set(\transpose, ~transpose);
				Ndef(~osc.asSymbol).set(\knobpreFilter, ~preFilter);
			});
			if (~osc.isArray, {
				~osc.do({|item|
					Ndef(item.asSymbol).set(\transpose, ~transpose);
					Ndef(item.asSymbol).set(\knobpreFilter, ~preFilter);
				});
			});
			Ndef(~adsr.asSymbol).set(\hold, (~dur*~legato));
			Ndef(~adsr.asSymbol).set(\t_pgate, 1);
			//{(~dur*~legato).wait; Ndef(~adsr.asSymbol).set(\pgate, 0)}.fork;
		});
	}

// ----------------------------- INSTANCE METHODS -----------------------------

	initAnasGui {
		var loadPath, recordPath;

		//define global variables
		~midiLock = 0;
		~updateInputSelectors = Condition.new(false);
		~allInputSelectors = List.new;

		//define OSC communications addresses
		//// send OSC messages to localhost:9090
		netAddress = NetAddr.new("localhost", 9090);
		////  netAddress.sendMsg("/renoise/transport/start"); // send sync messages to renoise? @TODO
		oscMessageSender = OSCdef.newMatching(\messageSender, {
			arg msg, time;
		},'/anas/bang');
		oscSend = NetAddr.new("10.0.3.251", 58100);

		// ------------------------------ Load Presets ------------------------------
		loadPath = this.class.loadPath;
		recordPath = this.class.recordPath;
		saves = Dictionary.new;
		{
			//initialize and load all the preset files
			loadPathFolders = loadPath.folders;
			whichFolder = loadPath.folders[0];

			//iterate through folders, opening the files and loading the contents into the dictionary "saves"
			loadPathFolders.do({|item|
				var temp = List.new;
				item.files.do({|file|
					saves.put(file.fileName.asSymbol, Object.readArchive(file.fullPath));
				});
			});

			menuEntries = whichFolder.files.collect({|item| item.fileName});
			folderEntries = loadPath.folders.collect({|item| item.folderName});
			// ------------------------------ ! Load Presets ------------------------------

			// ------------------------------ Global Key Actions ------------------------------
			//set global key action for switching windows and sampler activation
			View.globalKeyDownAction = {|view, char, mod, unicode, keycode|
				var keys = [mod, keycode];
				switch(keys,
					//cmd + q toggles the main window visibility.
					[1048576, 12], {
						if (window.visible == true, {window.visible = false}, {window.visible = true});
					},
					//ESC temporarily minimizes the window
					[0,53], {window.visible = false;},
					//tab key - reset focus
					[0, 48], {composite.focus(true); "composite focus".postln;this.updateFocus},
					[524288, 48], {composite.visible_(false)},
					//cmd+enter - activate sampler record
					[1048576, 36], {sampler.recordButton.button.valueAction = (1-sampler.recordButton.button.value)},
					[524288, 36], {sampler.overdubButton.button.valueAction = (1-sampler.overdubButton.button.value)},
				);
			};
			// ------------------------------ ! Global Key Actions ------------------------------

			//initialize Ndefs - sets up some dummy ndef things with the appropriate audio rate inputs. for some reason it bugs out if I don't do this.
			this.initNdefs;
			0.3.wait;

			// ------------------------------ GUI CODE  ------------------------------

			////guiPositions - a dictionary of pixel locations for rows and columns, so moving panels around can be done in one place. All subsequent panel placements reference this dictionary.
			guiPositions = Dictionary.new;
			guiPositions.putPairs([
				\topRowTop, 3,
				\topRowHeight, 25,
				\panelRows, [32, 337, 643],
				\panelColumns, [10, 208, 406, 604, 802],
				\firstRowPanels, 32,
				\secondRowPanels, 337,
				\thirdRowPanels, 643,
				\firstColumnLeft, 10,
				\secondColumnLeft, 208,
				\thirdColumnLeft, 406,
				\fourthColumnLeft, 604,
				\fifthColumnLeft, 802,
			]);

			////guiBounds - calculates Rect objects based on guiPositions. Makes the Actual Gui Code much cleaner.
			guiBounds = 0!17;
			5.do({|i|
				guiBounds[i] = Rect(guiPositions.at(\panelColumns)[i], guiPositions.at(\panelRows)[0], 192, 300);
			});
			guiBounds[5] = Rect(guiPositions.at(\panelColumns)[0], guiPositions.at(\panelRows)[1], 192, 150);
			guiBounds[6] = Rect(guiPositions.at(\panelColumns)[0], guiPositions.at(\panelRows)[1] + 150, 192, 150);
			4.do({|i|
				guiBounds[7+i] = Rect(guiPositions.at(\panelColumns)[1+i], guiPositions.at(\panelRows)[1], 192, 300)
			});
			guiBounds[11] = Rect(1000, 530, 100, 80);
			guiBounds[12] = Rect(1000, 625, 100, 80);

			////colourList - a dictionary of panel names and background colours. Referenced by panels and inputSelectors.
			~colourList = Dictionary.newFrom([ //this is how each panel and input selector knows what colour it is.
				\none, Color.new255(105, 65, 110, 140),
				\osc1, Color.new255(205,50,140, 140),
				\osc2, Color.new255(182,129,152, 140),
				\osc3, Color.new255(217,94,90, 140),
				\osc4, Color.new255(110,75,242, 140),
				\osc5, Color.new255(18,50,155, 140),
				\osc6, Color.new255(120, 40, 200, 150),
				\drum1, Color.new255(230, 190, 180, 170),
				\drum2, Color.new255(250, 80, 30, 160),
				\comp1, Color.new255(240, 170, 150, 165),
				\noise1, Color.new255(245, 100, 215, 160),
				\adsr1, Color.new255(90, 70, 60, 150),
				\adsr2, Color.new255(10, 10, 50, 140),
				\del1, Color.new255(20, 0, 0, 180),
				\del2, Color.new255(0, 80, 50, 180),
				\del3, Color.new255(0, 100, 120, 150),
				\del4, Color.new255(40, 130, 170, 150),
				\rev1, Color.new255(50, 60, 70, 170),
				\mult1, Color.new255(232, 215, 196, 160),
				\filt1, Color.new255(220, 180, 85, 140),
				\out1, Color.new255(255, 50, 70, 170),
				\out2, Color.new255(255, 80, 244, 170),
				\out3, Color.new255(40, 251, 255, 170),
				\out4,Color.new255(255, 166, 20, 170),
				\midi, Color.new255(70, 100, 90, 140),
				\sampler, Color.new255(50, 185, 170, 155),
				\pattern1, Color.new255(150, 0, 120, 130),
				\pattern2, Color.new255(120, 0, 175, 130),
				\pattern3, Color.new255(80, 0, 255, 130),
				\in1, Color.new255(200, 50, 50, 180),
				\in2, Color.new255(180, 79, 55, 180),
			]);


			////#########################   Actual Gui Code - must be deferred  ####################
			{
				//////create parent window
				window = Window.new(\anasGui, Rect(250, 200, 1105, 730));
				window.front;
				window.alwaysOnTop = isAlwaysOnTop;
				window.background = Color.new255(255, 200, 210, 200);
				window.onClose = {Ndef.clear};

				//////create composite view to contain panels
				composite = CompositeView.new(window, Rect(2, 2, 1100, 725));

				////keyboard control - shortcuts to select panels
				composite.canFocus_(true).keyDownAction_({|v,c,m,u,k|
					var keys = [m, k];
					[v,keys].postln;
					switch(keys,
						[0,18], {moduleSockets[0].focus(true); this.updateFocus},
						[0,19], {moduleSockets[1].focus(true); this.updateFocus},
						[0,20], {moduleSockets[2].focus(true); this.updateFocus},
						[0,21], {moduleSockets[3].focus(true); this.updateFocus},
						[0,23], {moduleSockets[4].focus(true); this.updateFocus},
						[131072,18], {moduleSockets[5].focus(true); this.updateFocus},
						[131072,19], {moduleSockets[6].focus(true); this.updateFocus},
						[131072,20], {moduleSockets[7].focus(true); this.updateFocus},
						[131072,21], {moduleSockets[8].focus(true); this.updateFocus},
						[131072,23], {moduleSockets[9].focus(true); this.updateFocus},
						[131072,22], {moduleSockets[10].focus(true); this.updateFocus},
						[131072,26], {moduleSockets[11].focus(true); this.updateFocus},
						[0, 26], {out1.composite.focus(true); this.updateFocus},
						[0, 28], {out2.composite.focus(true); this.updateFocus},
						[0, 25], {out3.composite.focus(true); this.updateFocus},
						[0, 29], {out4.composite.focus(true); this.updateFocus},

					);
				});
				////// clicking anywhere outside a panel returns focus to the compositeview
				composite.mouseDownAction_({this.updateFocus;});

				//////Display Header image
				//////image works differently on Linux, or so I've heard. for now, header image only appears in OS X.
				Platform.case(
					\osx, {
						img = Image.open((this.class.anasFolder +/+ "ANASLogo.png").fullPath);
						img.scalesWhenResized_(true).interpolation_(\smooth);
						header = View.new(composite, Rect(10,2, 96, 25)).background_(Color(0,0,0,0));
						header.setBackgroundImage(img, 10, 0.8);
						header.toolTip_("ANAS is the work of thumbthumb (Adam Adhiyatma) and agargara (David Cummings). \n
www.adamadhiyatma.com \n agargara.bandcamp.com");
				});

				// ************************************ MODULES/PANELS ****************************************
				moduleObjects = 0!13;

				//////Define clock panel
				clock = ClockPanel.new(composite, Rect(1000, guiPositions.at(\topRowTop), 98, guiPositions.at(\topRowHeight)), Ndef(\clock), this);

				/////Define output panels
				out1 = OutPanel.new(composite, 1000, guiPositions.at(\firstRowPanels), Ndef(\out1));
				out2 = OutPanel.new(composite, 1000, out1.bottom, Ndef(\out2));
				out3 = OutPanel.new(composite, 1000, out2.bottom, Ndef(\out3));
				out4 = OutPanel.new(composite, 1000, out3.bottom, Ndef(\out4));
				///////Store output panels in an array for reference
				~outPuts = [out1, out2, out3, out4];

				/////Define moduleSockets - containers for panels.
				moduleSockets = 0!13;
				13.do({|i| moduleSockets[i] = ModuleSocket.new(composite, guiBounds[i], this)});

				///// @@@@@@@@@@  Load panels into moduleSockets - CHANGE DEFAULT PANELS HERE @@@@@@@@@
				[
					[OscPanel, \osc1],
					[OscPanel, \osc2],
					[OscPanel, \osc3],
					[OscPanel, \osc4],
					[DrumPanel, \drum1],
					[ReverbPanel, \rev1],
					[NoisePanel, \noise1],
					[ADSRPanel, \adsr1],
					[ADSRPanel, \adsr2],
					[FilterPanel, \filt1],
					[DelayPanel, \del1],
					[InputPanel, \in1],
					[InputPanel, \in2]
				].do({|item, index|
					moduleSockets[index].loadPanel(item[0], item[1]);
				});
				///// @@@@@@@@@@  !! Load panels into moduleSockets - CHANGE DEFAULT PANELS HERE @@@@@@@@@

				////other panels not contained in sockets
				midipanel = MIDIPanel.new(composite, 10, guiPositions.at(\thirdRowPanels));
				patterns = 0!3;
				patterns[0] = PatternPanel.new(composite, 208, guiPositions.at(\thirdRowPanels), Ndef(\pattern1), this);
				patterns[1] = PatternPanel.new(composite, 472, guiPositions.at(\thirdRowPanels), Ndef(\pattern2), this);
				patterns[2] = PatternPanel.new(composite, 736, guiPositions.at(\thirdRowPanels), Ndef(\pattern3), this);

				////tell inputSelectors what the list of modules is.
				selectorMenu = SelectorMenu.new(composite);
				this.updateModuleList;

				// ************************************ !! MODULES/PANELS ****************************************
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^ Top of Window Controls ^^^^^^^^^^^^^^^^^^^^^^^^^^^^

				////Field for savefile name entry
				fileNameField = TextField.new(composite, Rect(guiPositions.at(\fifthColumnLeft), guiPositions.at(\topRowTop), 110, guiPositions.at(\topRowHeight)));
				fileNameField.background_(Color.new255(100, 80, 80, 100)).stringColor_(Color.white);
				fileNameField.action_({|field| fileName = field.value});
				fileNameField.toolTip_("Type the name of the preset you wish to save in here and PRESS ENTER before saving.");

				////"save preset" button
				saveButton = Button.new(composite,
					Rect(
						guiPositions.at(\fourthColumnLeft) + 132,
						guiPositions.at(\topRowTop), 59,
						guiPositions.at(\topRowHeight)
				));
				saveButton.states_([["Save ->", Color.white, Color.new255(200,135, 185, 180)]]
				).font_(Font("Helvetica", 13, true));
				saveButton.action_({this.save; (fileName.asString ++ " saved").postln;});

				////"open recordings folder" button
				openRecordingsButton = Button.new(composite,
					Rect(
						guiPositions.at(\secondColumnLeft),
						guiPositions.at(\topRowTop), 92,
						guiPositions.at(\topRowHeight)
				));
				openRecordingsButton.states_([["Rec Folder",	Color.white,Color.new255(30, 30, 150, 150)]]
				).font_(Font("Helvetica", 12, true));
				openRecordingsButton.action_({
					this.class.recordPath.fullPath.openOS;
					window.visible = false;
				});

				////"record audio" button
				recordButton = Button.new(composite,
					Rect(
						guiPositions.at(\secondColumnLeft) + 96,
						guiPositions.at(\topRowTop),
						96,
						guiPositions.at(\topRowHeight)
					)
				);
				recordButton.font = Font("Helvetica", 11);
				recordButton.states_([
					["record ->", Color.white, Color.new255(155, 35, 35, 150)],
					["recording", Color.white, Color.new255(255, 90, 100, 185)],
				]).font_(Font("Helvetica", 13, true));
				recordButton.action_({|button|
					if (button.value == 1, {
						Server.local.recHeaderFormat = "WAV";
						Server.local.recSampleFormat = "int24";
						Server.local.record(recordPath.fullPath ++ recordFileName ++ ".wav");
					}, {Server.local.stopRecording});
				});

				////Field for name of recording file
				recordPanel = TextField.new(composite,
					Rect(
						guiPositions.at(\thirdColumnLeft),
						guiPositions.at(\topRowTop),
						100,
						guiPositions.at(\topRowHeight)
					)
				);
				recordPanel.background = Color.new255(80, 80, 80, 150);
				recordPanel.action_({|panel| recordFileName = panel.value.asString}).stringColor_(Color.white);
				recordPanel.toolTip_("Type the name of the recording you wish to save here and PRESS ENTER before recording.");

				////Folder Menu
				folderMenu = PopUpMenu.new(composite,
					Rect(
						guiPositions.at(\thirdColumnLeft) + 102,
						guiPositions.at(\topRowTop),
						90,
						guiPositions.at(\topRowHeight)
					)
				);
				folderMenu.items_(folderEntries).background_(Color.new255(150, 135, 135, 180)).toolTip_("Use this menu to select a folder of presets.");
				folderMenu.action_({|menu|
					menu.value.postln;
					whichFolder = loadPathFolders[menu.value];
					this.updateMenuEntries;
				});

				////Preset Menu
				loadMenu = PopUpMenu.new(composite,
					Rect(
						guiPositions.at(\fourthColumnLeft),
						guiPositions.at(\topRowTop),
						130,
						guiPositions.at(\topRowHeight)
					)
				);
				loadMenu.items_(menuEntries).background_(Color.new255(130, 120, 195, 180).blend(Color.grey, 0.3));
				loadMenu.action_({|menu|
					this.load(menuEntries.at(menu.value));
				});
				loadMenu.allowsReselection_(true).toolTip_("Use this menu to select and load presets.");

				////Midi Lock Button
				midiLockButton = Button.new(composite,
					Rect(
						guiPositions.at(\fifthColumnLeft) + 112,
						guiPositions.at(\topRowTop),
						80,
						guiPositions.at(\topRowHeight)
					)
				);
				midiLockButton.states_([
					["MIDI unlocked", Color.white, Color.new255(50, 50, 50, 200)],
					["MIDI locked", Color.white, Color.new255(200, 40, 40, 150)]
				]).font_(Font("Helvetica", 11, true));
				midiLockButton.action_({|button| ~midiLock = button.value}).toolTip_("When this button is set to 'MIDI Locked', loading presets will not load their associated midi mappings. \n Otherwise, mappings will follow the ones that were saved in the preset. \n Useful for live performances.");

			////#########################  !! Actual Gui Code - must be deferred  ####################
			}.defer;
			0.5.wait;

			//rebuild all ndefs
			moduleSockets.do({|item| item.rebuild});
			0.05.wait;
		}.fork;

	}

	updateMenuEntries {
		menuEntries = whichFolder.files.collect({|item| item.fileName});
		{loadMenu.items = menuEntries}.defer;
	}

	updateModuleList {
		~moduleList =  //this is how all the input selectors know what their menu items are, and more
		[\none] ++
		moduleSockets.collect({|item| item.nDefNames}).flatten ++   //flatten in anticipation of future updates where module sockets may contain multiple panels and return an array of keys rather than just one.
		[\out1, \out2, \out3, \out4] ++
		[\pattern1, \pattern2, \pattern3, \noteBus];
		{
			selectorMenu.updateItems;
			~allInputSelectors.do({|item|
			switch(item.class,
				InputSelector, {item.selector.items = ~moduleList},
				Selector, {item.items = ~moduleList},
			);
			item.update;
		})}.defer;
		//{0.5.wait; {~allInputSelectors.do({|item| item.setColour})}.defer;}.fork;
	}

	updateFocus { //check every panel to see if it has focus. if it is, change the label to reflect focus
		{
			0.005.wait;
			{
				moduleSockets.do({|socket|
					var item = socket.panel;
					if (item.notNil) {
						if (item.composite.hasFocus, {
							var size1, size2;
							//item.label.stringColor_(Color.new255(255, 255, 255, 250));
							//item.label.font_(Font("Courier", 22));
							item.label2.stringColor_(Color.new255(255,190,95, 210));
							size2 = item.label2.font.size;
							item.label2.font_(Font("Arial", size2, true));
							item.tryPerform(\label3) !? {|item|
								item.font_(Font("Arial", 130, true)).stringColor_(Color.new255(250,240, 40, 170));
							};
							item.composite.background = item.composite.background.alpha_(0.9);
							oscSend.sendMsg(("/" ++ item.nDef.key.asString).postln);
						}, {
							var size1, size2;
							//item.label.stringColor_(Color.new255(255,255,255,200));
							//item.label.font_(Font("Courier", 18));
							item.label2.stringColor_(Color.new(1,1,1,0.5));
							size2 = item.label2.font.size;
							item.label2.font_(Font("Arial", size2, true));
							item.tryPerform(\label3) !? {|item|
								item.font_(Font("Arial", 110, true)).stringColor_(Color(1,1,1,0.6));
							};
							item.composite.background = item.composite.background.alpha_(0.6);
						});
					};
				});
			}.defer;
		}.fork;

	}

	initNdefs {
		Ndef.clear;
		Ndef(\dummy, {Silent.ar});
		Ndef(\none, {DC.ar(0)}).ar;
		Ndef(\osc1, {
			arg knobfreq = 0.5, knobamp = 0.5, knobtone = 0, knobpreFilter = 0.5, knobwidth = 0.5, knobdistort = 0, freqMin = 40, freqMax = 12000;
			var sig, freqIn=Silent.ar, ampIn=Silent.ar, toneIn=Silent.ar, preFilterIn=Silent.ar,widthIn = Silent.ar, distortIn = Silent.ar, knobFreqIn, knobpreFilterIn, knobwidthIn, knobdistortIn;

			toneIn = (knobtone.linlin(0,1,1, 15)).min(20000);
			knobFreqIn = knobfreq.linexp(0, 1, freqMin, freqMax);
			[\none, \none, \none].do({|item|
				freqIn = freqIn + Ndef(item);
			});
			freqIn = LinExp.ar(freqIn, -1, 1, 0.1, 2.5);
			freqIn = (knobFreqIn.lag(0.06) * freqIn);
			freqIn = (freqIn).min(19000);
			[\none, \none, \none].do({|item|
				ampIn = ampIn + Ndef(item);
			});
			ampIn = LinLin.ar(ampIn, -1, 1, 0, 1);
			ampIn = (knobamp * 2 * ampIn).max(0).min(1);
			knobpreFilterIn = knobpreFilter.linexp(0, 1, 100, 10000);
			[\none, \none, \none].do({|item|
				preFilterIn = preFilterIn + Ndef(item);
			});
			preFilterIn = LinExp.ar(preFilterIn, -1, 1, 0.5, 2);
			preFilterIn = (knobpreFilterIn * preFilterIn).min(20000);
			knobwidthIn = knobwidth.linlin(0,1, 0.02, 0.98);
			[\none, \none, \none].do({|item|
				widthIn = widthIn + Ndef(item);
			});
			widthIn = LinLin.ar(widthIn, -1, 1, 0.3, 3);
			widthIn = (knobwidthIn * widthIn).max(0.01).min(0.99);
			knobdistortIn = knobdistort.linlin(0, 1, 1, 20);
			[\none,\none,\none].do({|item|
				distortIn = distortIn + Ndef(item);
			});
			distortIn = LinLin.ar(distortIn, -1, 1, 0.5, 1.5);
			distortIn = (knobdistortIn * distortIn).max(1).min(20);
			sig = DSaw.ar(freqIn, toneIn, preFilter: preFilterIn, drift: 0.004, mul:ampIn * 8.5);
			sig = Mix.new(sig);
			sig;
		});
		Ndef(\osc1).reshaping_(\elastic).mold(1, \audio);
		Ndef(\osc1).copy(\osc2).copy(\osc3).copy(\osc4).copy(\osc5);


	}


	save {
		saveList = Dictionary.new;
		saveList.putPairs([
			\out1, out1.save,
			\out2, out2.save,
			\out3, out3.save,
			\out4, out4.save,
			\pattern1, patterns[0].save,
			\pattern2, patterns[1].save,
			\pattern3, patterns[2].save,
			\tempo, clock.clock.tempo,
		]);
		moduleSockets.do({|item, index|
			saveList.put(index.asSymbol, item.save);
		});
		//saveList.put(\modules, moduleSockets.collect({|item| item.module.asString}));
		saves.put(fileName.asSymbol, saveList); //add the list straight into the pre-loaded saves dictionary
		savePath = PathName.new(whichFolder.fullPath ++ fileName); //name and write the file
		saveList.writeArchive(savePath.fullPath);
		this.updateMenuEntries;
	}

	load {
		arg file;
		var loadList;
		loadList = saves.at(file.asSymbol); //load dictionary
		~outPuts.do({|out, index|
			var outSymbol = ("out"++(index+1)).asSymbol.postln;
			out.outList = List.new;
			out.load(loadList.at(outSymbol));
		});
		patterns[0].load(loadList.at(\pattern1));
		patterns[1].load(loadList.at(\pattern2));
		patterns[2].load(loadList.at(\pattern3));
		moduleSockets.do({|item, index|
			item.load(loadList.at(index.asSymbol) ?? {nil});
		});
		this.updateModuleList;
		moduleSockets.do({|item| item.rebuild;});
		patterns.do({|item| item.rebuild});
		~outPuts.do({|item| item.rebuild;});
		clock.clock.tempo = loadList.at(\tempo) ?? {2};
		clock.reSyncAll;
		//~a.saves.at(\filtFeedback).at(\out2).at(\volume)
	}

}