ANASLauncher {
	var launcher, closeButton, launchButton, configButton, reopenButton, configWindow, anasFolder, anasDir, pathFields, saveConfigButton, configText, recompileButton, outDeviceSelector, inDeviceSelector;
	*new {
	^super.new.initANASLauncher;
	}

	initANASLauncher {
		anasFolder = AnasGui.anasFolder;
		anasDir = AnasGui.anasDir;
		launcher = Window.new("ANASLauncher", Rect(895, 30, 360, 20), border:false);
		launcher.background = Color.new255(150, 90, 110, 105);
		launcher.front.alwaysOnTop_(true);
		launchButton = Button.new(launcher, Rect(2, 2, 94, 15));
		launchButton.states_([["Start ANAS", Color.black, Color.new255(210, 180, 180, 140)]]);
		launchButton.action_({
			//code to start the synth.
			Server.local.quit;
			Server.scsynth;
			Server.local.options.outDevice = (AnasGui.config.at(\outDevice).postln);
			Server.local.options.inDevice = (AnasGui.config.at(\inDevice).postln);
			Server.local.options.numOutputBusChannels_(2);
			Server.local.options.hardwareBufferSize_(512);
			Server.local.options.blockSize_(512);
			Server.local.latency = 0.05;
			Server.local.waitForBoot({
				//replace the paths below with your own path for a save folder and a recordings folder. (don't make them the same folder)
				//b = Buffer.read(Server.local,"/Users/adamadhiyatma/Music/SuperCollider Recordings/livesamples/Anas/Samples/STE-093BellRapid.wav", 0, -1);
				~a = AnasGui.new;
			});
		});
		Platform.case(\osx, {
			recompileButton = Button.new(launcher, Rect(98, 2, 94, 15));
			recompileButton.states_([["Recompile", Color.black, Color.new255(240, 100, 200, 150)]]);
			recompileButton.action_({
				thisProcess.recompile;
			});
		});
		configButton = Button.new(launcher, Rect(194, 2, 94, 15));
		configButton.states_([["Configure", Color.black, Color.new255(150, 100, 210)]]);
		configButton.action_({
			//configuration window
			configWindow = Window.new("ANAS Configuration", Rect(1200, 170, 400, 240));
			configWindow.front.background_(Color.new255(170, 120, 210, 215));
			pathFields = 0!2;
			configText = 0!5;
			configText[0] = StaticText.new(configWindow, Rect(5,5, 390, 25)).background_(Color(1,1,1,0));
			configText[0].stringColor_(Color.white).font_(Font("Helvetica", 11));
			configText[0].string = "1. Drag your save folder into this text box, or enter a path:";
			pathFields[0] = TextField.new(configWindow, Rect(5, 30, 390, 25)).background_(Color(1,1,1,0.2));
			pathFields[0].string = AnasGui.loadPath.fullPath;
			configText[1] = StaticText.new(configWindow, Rect(5,57, 390, 25)).background_(Color(1,1,1,0));
			configText[1].stringColor_(Color.white).font_(Font("Helvetica", 11));
			configText[1].string = "2. Drag your recordings folder into this text box, or enter a path:";
			pathFields[1] = TextField.new(configWindow, Rect(5, 80, 390, 25)).background_(Color(1,1,1,0.3));
			pathFields[1].string = AnasGui.recordPath.fullPath;
			inDeviceSelector = PopUpMenu.new(configWindow, Rect(150, 110, 200, 20));
			inDeviceSelector.items_(ServerOptions.devices);
			configText[2] = StaticText.new(configWindow, Rect(5, 110, 120, 25));
			configText[2].stringColor_(Color.white).font_(Font("Helvetica", 11)).string_("3. Select input device.");
			outDeviceSelector = PopUpMenu.new(configWindow, Rect(150, 135, 200, 20));
			outDeviceSelector.items_(ServerOptions.devices);
			configText[3] = StaticText.new(configWindow, Rect(5, 135, 120, 25));
			configText[3].stringColor_(Color.white).font_(Font("Helvetica", 11)).string_("4. Select output device.");
			saveConfigButton = Button.new(configWindow, Rect(150, 160, 100, 25));
			configText[4] = StaticText.new(configWindow, Rect(5, 160, 150, 25)).background_(Color(1,1,1,0));
			configText[4].stringColor_(Color.white).font_(Font("Helvetica", 11));
			configText[4].string = "5. Click here to save -->";
			saveConfigButton.states_([["Save and close", Color.white, Color.new255(110, 80, 200)]]);
			saveConfigButton.action_({
				var saveArray = Dictionary.newFrom([
					\loadPath, pathFields[0].value,
					\recordingsPath, pathFields[1].value,
					\inDevice, inDeviceSelector.item,
					\outDevice, outDeviceSelector.item
				]);
				saveArray.writeArchive(anasDir.fullPath +/+ "ANASconfig");
				configWindow.close;
				thisProcess.recompile;
			});


		});
		reopenButton = Button.new(launcher, Rect(290, 2, 50, 15));
		reopenButton.states_([["Reopen", Color.black, Color.new255(100, 130, 220, 180)]]);
		reopenButton.action_({~a.window.visible = true;});
		closeButton = Button.new(launcher, Rect(343, 2, 15, 15));
		closeButton.states_([["x", Color.white, launcher.background]]);
		closeButton.action_({launcher.close});


	}

}