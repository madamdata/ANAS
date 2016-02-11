/* to do
mapping LFOKnobs
finish Sampler panel
keyboard window control
volume knobs for everything?
invert input (?)

*/
AnasGui {
	classvar <>launcher, launchButton, recompileButton, closeButton, configButton, configWindow, reopenButton, pathFields, configText, saveConfigButton, <version, <config, <anasFolder, <anasDir, <>loadPath, <>recordPath, <>netAddress, <>oscMessageSender, isAlwaysOnTop;
	var <>loadPath, <>recordPath, <>window, <clock, <>osc1, <>osc2, <>osc3, <>osc4, <>osc5, <>out1, <>out2, <>out3, <>out4, <>del1, <>mult1, <>adsr1, <>adsr2, <>filt1, <>midipanel, <>sampler, <>in1, <in2, <>patterns, composite, <>saveList, <>savePath, <>whichFolder, <>fileName, fileNameField, saveButton, recordButton, openRecordingsButton, recordFileName, <>recordPanel, <>loadMenu, <>menuEntries, <>folderMenu, <>folderEntries, <>loadPathFolders, <>moduleList, <>saves, img, header, closeButton, <moduleObjects, <midiLockButton, <guiPositions;
	*new {

		^super.new.initAnasGui;

	}

	*initClass {
		version = "ANAS v0.982";
		this.loadEventTypes;
		anasFolder = PathName(AnasGui.filenameSymbol.asString.dirname);
		anasDir = PathName(AnasGui.filenameSymbol.asString.dirname.dirname);
		if (File.exists(anasDir.fullPath +/+ "ANASconfig"), {
			//if config file exists, load save and recording paths
			config = Object.readArchive(anasDir.fullPath +/+ "ANASconfig");
			loadPath = PathName(config.at(\loadPath));
			recordPath = PathName(config.at(\recordingsPath));
		},
		{ //if not, prompt the user to configure.
			StartUp.add(
				config = Dictionary.new;
				{{0.5.wait; "You must configure ANAS before starting! Click below to configure.".postln;}.fork;}
			);
		}
		);
		Window.closeAll;

		//if there's nothing in the save folder, make a subfolder
		if (loadPath.notNil, {
			if (loadPath.folders.size == 0, {
				File.mkdir(loadPath.fullPath +/+ "folder1");
			});
		});
		{0.5.wait; (version ++ " installed").postln;}.fork;
    launcher = ANASLauncher.new;
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

	initAnasGui {
		var loadPath, recordPath;

		netAddress = NetAddr.new("localhost", 9090); // send OSC messages to localhost:9090
		oscMessageSender = OSCdef.newMatching(\messageSender, { arg msg, time;
			//  netAddress.sendMsg("/renoise/transport/start"); // send sync messages to renoise? @TODO
		},'/anas/bang');
		guiPositions = Dictionary.new; //dictionary for storing gui positions in pixels e.g. first row = 5 pixels from top
		loadPath = this.class.loadPath;
		recordPath = this.class.recordPath;
		saves = Dictionary.new;
		{
			//initialize and load all the preset files
			loadPathFolders = loadPath.folders;
			whichFolder = loadPath.folders[0];
			loadPathFolders.do({|item|
				var temp = List.new;
				item.files.do({|file|
					saves.put(file.fileName.asSymbol, Object.readArchive(file.fullPath));
				});
			});

			menuEntries = whichFolder.files.collect({|item| item.fileName});
			folderEntries = loadPath.folders.collect({|item| item.folderName});
			View.globalKeyDownAction = {|view, char, mod, unicode, keycode| //set global key action for switching windows and sampler activation
				var keys = [mod, keycode];
				//[view, char, mod, unicode, keycode].postln;
				switch(keys,
					[1048576, 12], { //cmd + q toggles the main window visibility.
						if (window.visible == true, {window.visible = false}, {window.visible = true});
					},
					[0,53], { //ESC temporarily minimizes the window
						window.visible = false;
					},
					[0, 48], {composite.focus(true); "composite focus".postln;this.updateFocus}, //tab key - reset focus
					[1048576, 36], {sampler.recordButton.button.valueAction = (1-sampler.recordButton.button.value)}, //cmd+enter - activate sampler record
					[524288, 36], {sampler.overdubButton.button.valueAction = (1-sampler.overdubButton.button.value)},
				);
			};

			this.initNdefs; //initialize Ndefs - sets up some dummy ndef things with the appropriate audio rate inputs. for some reason it bugs out if I don't do this.
			0.3.wait;
			{ //gui code - must be deferred
				guiPositions.putPairs([ //pixel locations of panels
					\topRowTop, 3,
					\topRowHeight, 25,
					\firstRowPanels, 32,
					\secondRowPanels, 337,
					\thirdRowPanels, 643,
					\firstColumnLeft, 10,
					\secondColumnLeft, 208,
					\thirdColumnLeft, 406,
					\fourthColumnLeft, 604,
					\fifthColumnLeft, 802,
				]);
				window = Window.new(\anasGui, Rect(250, 200, 1105, 730));
				window.front;
				window.alwaysOnTop = isAlwaysOnTop;
				window.background = Color.new255(255, 200, 210, 200);
				window.onClose = {Ndef.clear};
				~midiLock = 0;
				~colourList = Dictionary.newFrom([ //this is how each panel and input selector knows what colour it is.
					\none, Color.new255(105, 65, 110, 140),
					\osc1, Color.new255(205,50,140, 140),
					\osc2, Color.new255(182,129,152, 140),
					\osc3, Color.new255(217,94,90, 140),
					\osc4, Color.new255(110,75,242, 140),
					\osc5, Color.new255(18,50,155, 140),
					\adsr1, Color.new255(70, 60, 50, 140),
					\adsr2, Color.new255(10, 10, 50, 140),
					\del1, Color.new255(0, 0, 0, 180),
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
				composite = CompositeView.new(window, Rect(2, 2, 1100, 725));
				composite.canFocus_(true).keyDownAction_({|v,c,m,u,k| //keyboard control - selecting panels
					var keys = [m, k];
					[v,keys].postln;
					switch(keys,
						[0,18], {osc1.composite.focus(true); this.updateFocus},
						[0,19], {osc2.composite.focus(true); this.updateFocus},
						[0,20], {osc3.composite.focus(true); this.updateFocus},
						[0,21], {osc4.composite.focus(true); this.updateFocus},
						[0,23], {osc5.composite.focus(true); this.updateFocus},
						[131072,18], {del1.composite.focus(true); this.updateFocus},
						[131072,19], {adsr1.composite.focus(true); this.updateFocus},
						[131072,20], {adsr2.composite.focus(true); this.updateFocus},
						[131072,21], {filt1.composite.focus(true); this.updateFocus},
						[131072,23], {sampler.composite.focus(true); this.updateFocus},
						[131072,22], {mult1.composite.focus(true); this.updateFocus},
						[131072,26], {in1.composite.focus(true); this.updateFocus},
						[0, 26], {out1.composite.focus(true); this.updateFocus},
						[0, 28], {out2.composite.focus(true); this.updateFocus},
						[0, 25], {out3.composite.focus(true); this.updateFocus},
						[0, 29], {out4.composite.focus(true); this.updateFocus},

					);
				});
				composite.mouseDownAction_({this.updateFocus});
				//header image
				Platform.case( //image works differently on Linux, or so I've heard. for now, header image only appears in OS X.
					\osx, {
						img = Image.open((this.class.anasFolder +/+ "ANASLogo.png").fullPath);
						img.scalesWhenResized_(true).interpolation_(\smooth);
						header = View.new(composite, Rect(10,2, 96, 25)).background_(Color(0,0,0,0));
						header.setBackgroundImage(img, 10, 0.8);
						header.toolTip_("ANAS is the work of thumbthumb (Adam Adhiyatma) and agargara (David Cummings). \n
www.adamadhiyatma.com \n agargara.bandcamp.com");
				});
				//modules
				moduleObjects = 0!17;
				~updateInputSelectors = Condition.new(false);
				clock = ClockPanel.new(composite, Rect(1000, guiPositions.at(\topRowTop), 98, guiPositions.at(\topRowHeight)), Ndef(\clock), this);
				moduleObjects[13] = out1 = OutPanel.new(composite, 1000, guiPositions.at(\firstRowPanels), Ndef(\out1));
				moduleObjects[14] = out2 = OutPanel.new(composite, 1000, out1.bottom, Ndef(\out2));
				moduleObjects[15] = out3 = OutPanel.new(composite, 1000, out2.bottom, Ndef(\out3));
				moduleObjects[16] = out4 = OutPanel.new(composite, 1000, out3.bottom, Ndef(\out4));
				~outPuts = [out1, out2, out3, out4];
				moduleObjects[0] = osc1 = OscPanel.new(composite, 10, guiPositions.at(\firstRowPanels), Ndef(\osc1), ~outPuts, clock);
				moduleObjects[1] = osc2 = OscPanel.new(composite, 208, guiPositions.at(\firstRowPanels), Ndef(\osc2), ~outPuts, clock);
				moduleObjects[2] = osc3 = OscPanel.new(composite, 406, guiPositions.at(\firstRowPanels), Ndef(\osc3), ~outPuts, clock);
				moduleObjects[3] = osc4 = OscPanel.new(composite, 604, guiPositions.at(\firstRowPanels), Ndef(\osc4), ~outPuts, clock);
				moduleObjects[4] = osc5 = OscPanel.new(composite, 802, guiPositions.at(\firstRowPanels), Ndef(\osc5), ~outPuts, clock);
				moduleObjects[5] = del1 = DelayPanel.new(composite, 10, guiPositions.at(\secondRowPanels), Ndef(\del1), ~outPuts);
				moduleObjects[6] = mult1 = MultiPlexPanel.new(composite, Rect(10, guiPositions.at(\secondRowPanels) + 150, 192, 150), Ndef(\mult1), ~outPuts);
				moduleObjects[7] = adsr1 = ADSRPanel.new(composite, Rect(208, guiPositions.at(\secondRowPanels), 192, 300), Ndef(\adsr1), ~outPuts);
				moduleObjects[8] = adsr2 = ADSRPanel.new(composite, Rect(406, guiPositions.at(\secondRowPanels), 192, 300), Ndef(\adsr2), ~outPuts);
				moduleObjects[9] = filt1 = FilterPanel.new(composite, Rect(604,guiPositions.at(\secondRowPanels),192,300), Ndef(\filt1), ~outPuts);
				moduleObjects[10] = sampler = SamplerPanel.new(composite, 802, guiPositions.at(\secondRowPanels), Ndef(\sampler), ~outPuts);
				moduleObjects[11] = in1 = InputPanel.new(composite, Rect(1000, 530, 100, 80), Ndef(\in1));
				moduleObjects[12] = in2 = InputPanel.new(composite, Rect(1000, 625, 100, 80), Ndef(\in2));
				midipanel = MIDIPanel.new(composite, 10, guiPositions.at(\thirdRowPanels));
				patterns = 0!3;
				patterns[0] = PatternPanel.new(composite, 208, guiPositions.at(\thirdRowPanels), Ndef(\pattern1), this);
				patterns[1] = PatternPanel.new(composite, 472, guiPositions.at(\thirdRowPanels), Ndef(\pattern2), this);
				patterns[2] = PatternPanel.new(composite, 736, guiPositions.at(\thirdRowPanels), Ndef(\pattern3), this);
				~moduleList =  //this is how all the input selectors know what their menu items are, and more
				[\none] ++
				moduleObjects.collect({|item| item.nDef.key}) ++
				[\pattern1, \pattern2, \pattern3, \noteBus];
				~updateInputSelectors.test_(true).signal; //now that ~moduleList is fully populated, signal all input selector to update their lists.
				//controls at the top of the window
				fileNameField = TextField.new(composite, Rect(guiPositions.at(\fifthColumnLeft), guiPositions.at(\topRowTop), 110, guiPositions.at(\topRowHeight)));
				fileNameField.background_(Color.new255(100, 80, 80, 100)).stringColor_(Color.white);
				fileNameField.action_({|field| fileName = field.value});
				fileNameField.toolTip_("Type the name of the preset you wish to save in here and PRESS ENTER before saving.");
				saveButton = Button.new(composite, Rect(guiPositions.at(\fourthColumnLeft) + 132,  guiPositions.at(\topRowTop), 59, guiPositions.at(\topRowHeight)));
				saveButton.states_([["Save ->", Color.white, Color.new255(200,135, 185, 180)]]).font_(Font("Helvetica", 13, true));
				saveButton.action_({this.save; (fileName.asString ++ " saved").postln;});
				openRecordingsButton = Button.new(composite, Rect(guiPositions.at(\secondColumnLeft), guiPositions.at(\topRowTop), 92, guiPositions.at(\topRowHeight)));
				openRecordingsButton.states_([["Rec Folder", Color.white, Color.new255(30, 30, 150, 150)]]).font_(Font("Helvetica", 12, true));
				openRecordingsButton.action_({
					this.class.recordPath.fullPath.openOS;
					window.visible = false;
				});
				recordButton = Button.new(composite, Rect(guiPositions.at(\secondColumnLeft) + 96, guiPositions.at(\topRowTop), 96, guiPositions.at(\topRowHeight)));
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
				recordPanel = TextField.new(composite, Rect(guiPositions.at(\thirdColumnLeft), guiPositions.at(\topRowTop), 100, guiPositions.at(\topRowHeight)));
				recordPanel.background = Color.new255(80, 80, 80, 150);
				recordPanel.action_({|panel| recordFileName = panel.value.asString}).stringColor_(Color.white);
				recordPanel.toolTip_("Type the name of the recording you wish to save here and PRESS ENTER before recording.");
				folderMenu = PopUpMenu.new(composite, Rect(guiPositions.at(\thirdColumnLeft) + 102, guiPositions.at(\topRowTop), 90, guiPositions.at(\topRowHeight)));
				folderMenu.items_(folderEntries).background_(Color.new255(150, 135, 135, 180)).toolTip_("Use this menu to select a folder of presets.");
				folderMenu.action_({|menu|
					menu.value.postln;
					whichFolder = loadPathFolders[menu.value];
					this.updateMenuEntries;
				});
				loadMenu = PopUpMenu.new(composite, Rect(guiPositions.at(\fourthColumnLeft), guiPositions.at(\topRowTop), 130, guiPositions.at(\topRowHeight)));
				loadMenu.items_(menuEntries).background_(Color.new255(130, 120, 195, 180).blend(Color.grey, 0.3));
				loadMenu.action_({|menu|
					this.load(menuEntries.at(menu.value));
				});
				loadMenu.allowsReselection_(true).toolTip_("Use this menu to select and load presets.");
				midiLockButton = Button.new(composite, Rect(guiPositions.at(\fifthColumnLeft) + 112, guiPositions.at(\topRowTop), 80, guiPositions.at(\topRowHeight)));
				midiLockButton.states_([
					["MIDI unlocked", Color.white, Color.new255(50, 50, 50, 200)],
					["MIDI locked", Color.white, Color.new255(200, 40, 40, 150)]
				]).font_(Font("Helvetica", 11, true));
				midiLockButton.action_({|button| ~midiLock = button.value});
		}.defer;
			0.5.wait;
		[osc1, osc2, osc3, osc4, osc5, del1, mult1, adsr1, filt1, out1, out2, out3, out4].do{|item| item.rebuild;};
			0.05.wait;
		}.fork;

	}

	updateMenuEntries {
		menuEntries = whichFolder.files.collect({|item| item.fileName});
		{loadMenu.items = menuEntries}.defer;
	}

	updateFocus { //check every panel to see if it has focus. if it is, change the label to reflect focus
		{
			0.005.wait;
			{
		moduleObjects.do({|item|
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
		});
		}.defer;
		}.fork;

	}

	initNdefs {
		Ndef.clear;
		Ndef(\dummy, {Silent.ar});
		Ndef(\none, {DC.ar(0)});
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
			\modules, ~moduleList,
			\osc1, osc1.save,
			\osc2, osc2.save,
			\osc3, osc3.save,
			\osc4, osc4.save,
			\osc5, osc5.save,
			\del1, del1.save,
			\mult1, mult1.save,
			\adsr1, adsr1.save,
			\adsr2, adsr2.save,
			\filt1, filt1.save,
			\sampler, sampler.save,
			\in1, in1.save,
			\out1, out1.save,
			\out2, out2.save,
			\out3, out3.save,
			\out4, out4.save,
			\pattern1, patterns[0].save,
			\pattern2, patterns[1].save,
			\pattern3, patterns[2].save,
		]);
		saves.put(fileName.asSymbol, saveList);
		savePath = PathName.new(whichFolder.fullPath ++ fileName);
		saveList.writeArchive(savePath.fullPath);
		this.updateMenuEntries;
	}

	load {
		arg file;
		var loadList;
		loadList = saves.at(file.asSymbol);
		~outPuts.do({|out| out.outList = List.new});
		clock.reSyncAll;
		osc1.load(loadList.at(\osc1));
		osc2.load(loadList.at(\osc2));
		osc3.load(loadList.at(\osc3));
		osc4.load(loadList.at(\osc4));
		osc5.load(loadList.at(\osc5));
		del1.load(loadList.at(\del1));
		mult1.load(loadList.at(\mult1) ??{nil});
		adsr1.load(loadList.at(\adsr1)??{nil});
		adsr2.load(loadList.at(\adsr2)??{nil});
		filt1.load(loadList.at(\filt1)??{nil});
		sampler.load(loadList.at(\sampler)??{nil});
		in1.load(loadList.at(\in1)??{nil});
		out1.load(loadList.at(\out1) ??{nil});
		out2.load(loadList.at(\out2)??{nil});
		out3.load(loadList.at(\out3)??{nil});
		out4.load(loadList.at(\out4)??{nil});
		patterns[0].load(loadList.at(\pattern1));
		patterns[1].load(loadList.at(\pattern2));
		patterns[2].load(loadList.at(\pattern3));
		[clock, osc1, osc2, osc3, osc4, osc5, del1, mult1, adsr1, filt1, sampler, out1, out2, out3, out4].do{|item| item.rebuild;};
		//clock.reSyncAll;
		//[osc1, osc2, osc3, osc4, osc5, del1, mult1, adsr1, filt1, out1, out2, out3, out4].do{|item| item.rebuild;};

	}


}