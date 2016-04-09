SamplerPanel : ANASPanel {
	var <>labelKnob1, <>labelKnob2, <>labelKnob3, <>samplePosLabelKnob, <>sampleSlider, <>specs, outputButtons, selectors, samplePath, sampleList, sampleMenu, <>bufs, <>whichSample, <recordButton, <overdubButton, <resetButton, <>fileName, saveField, saveButton, interpolationButton, <>currentBuffer, <>signalArray, <>startTime, <>endTime, <sr, <>loopFrames, oscFunc, oscFunc2;

	*new {
		arg parent, bounds, nDef, outs;
		^super.newCopyArgs(parent,bounds,nDef,outs).initSamplerPanel;
	}

	initSamplerPanel {
		this.initANASPanel;
		//if "Samples" folder does not exist in the recordPath, make one.
		if (File.exists((AnasGui.recordPath.fullPath +/+ "Samples")), {}, {
			File.mkdir((AnasGui.recordPath.fullPath +/+ "Samples"))
		});
		samplePath = (AnasGui.recordPath +/+ "Samples/");
		sr = Server.local.sampleRate;
		sampleList = samplePath.files.collect({|item| item.fileName});
		whichSample = samplePath.files[0];
		inputList = \none!4;
		bufs = Dictionary.new;
		specs = Dictionary.new;
		specs.put(\Rate, ControlSpec(0.1, 50, \exp));
		specs.put(\Volume, ControlSpec(0, 2));
		samplePath.files.do({|item, index|
			if (item.extension == "wav", {
				bufs.put(item.fileName.asSymbol, Buffer.readChannel(Server.local, item.fullPath, 0, -1, 0));
			});
		});
		bufs.put(\temp, Buffer.alloc(Server.local, 44100 * 60, 1));
		//KEYBOARD CONTROL
		keyRoutine = Routine{
			4.do({|i|
				if (whichPanel != \same, {
					inputList[i] = whichPanel;
				});
				i.yield
			})
		};
		standardAction = {|v,c,m,u,k|
			var keys = [m, k];
			switch(keys,
				[0, 49], {
					this.rebuild;
					keyRoutine.reset;
					{inputBank.update}.defer;
				},
				[1048576, 18], {selectors[0].valueAction_(1)},
				[1048576, 19], {selectors[0].valueAction_(2)},
				[0,18], {this.focusOn(0)},
				[0,19], {this.focusOn(1)},
				[0,20], {this.focusOn(2)},
				//[0,21], {this.focusOn(3)},
				[0, 12], {outputButtons[0].flipRebuild},
				[0, 13], {outputButtons[1].flipRebuild},
				[0, 14], {outputButtons[2].flipRebuild},
				[0, 15], {outputButtons[3].flipRebuild},
				[0, 0], {
					composite.keyDownAction_(setInputAction);
					{inputBank.setRed}.defer;
				},
			);
			//nDef.key.asString.postln;
			true;
		};

		composite.canFocus_(true).keyDownAction_(standardAction);
		//END KEYBOARD CONTROL
		composite.background = ~colourList.at(nDef.key) ?? {Color.new255(50, 50, 50, 50)};
		/*label = StaticText.new(composite, Rect(0, 0, 190, 20));
		label.string = ("" ++ nDef.key.asString.toUpper);
		label.font = Font("courier", 18);
		label.stringColor = Color.new255(255,255,255,200);
		label.align = \center;
		label.background = Color(0,0,0,0);*/
		label2 = StaticText.new(composite, Rect(0, 0, 190, 20));
		label2.string = nDef.key.asString;
		label2.font = Font("Arial", 22, true);
		label2.stringColor = Color.new(1,1,1,0.4);
		label2.align = \center;
		label2.background = Color(0,0,0,0);
		inputBank = InputBank.new(composite, Rect(0, 20, 192, 30), this);
		/*selectors = 0!4;
		4.do({|i|
			selectors[i] = InputSelector.new(composite, i*48+2, 20)
		});
		selectors.do({|item, index|
			item.selector.background = ~colourList.at(\none).blend(Color.grey, 0.5);
			item.selector.action = {|selector|
				inputList[index] = selector.item.asSymbol;
				selector.background = (~colourList.at(selector.item.asSymbol) ?? {~colourList.at(\none)}).blend(Color.grey, 0.5);
				this.rebuild;
			};
		});*/
		labelKnob1 = LabelKnob.new(composite, 2, 37, "Rate", this, 1, specs.at(\Rate), specs.at(\Rate).unmap(1));
		labelKnob2 = LabelKnob.new(composite, 49, 37, "Volume", this, 1, specs.at(\Volume));
		labelKnob3 = LabelKnob.new(composite, 96, 37, "writeMix", this, 1, default: 1);

		sampleSlider = SampleSlider.new(composite, Rect(2, 180, 188, 64), Color.new255(200, 150, 150, 0.5), Color.new255(200, 200, 200, 100), nDef);

		samplePosLabelKnob = SamplePosLabelKnob.new(composite, 143, 37, "Pos", this, 1, [-1,1].asSpec, 0.5, 2, sampleSlider);
		samplePosLabelKnob.knob1.toolTip = "Position offset from the GUI start position (offset is -1 to 1)";

		recordButton = RecordButton.new(composite, Rect(2, 135, 47, 55), "rec", this,
			[Color.new255(200,70,90,180), Color.new255(210,50,40,255)]
		);
		overdubButton = OverdubButton.new(composite, Rect(49, 135, 47, 55), "dub", this,
			[Color.new255(200, 130, 100, 180), Color.new255(250, 150, 100, 240)]);

		resetButton = ResetButton.new(composite, Rect(96, 135, 47, 55), "<-", this,
			[Color.new255(50, 190, 150, 200), Color.new255(50, 190, 150, 200)]
		);
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 2 +((80/outs.size)*index), 282, (80/outs.size), nDef, whichOut);
		});
		sampleMenu = PopUpMenu.new(composite, Rect(2, 250, 80, 15));
		sampleMenu.items = sampleList;
		sampleMenu.action_({|menu|
			currentBuffer = menu.item.asSymbol;
			loopFrames = bufs.at(currentBuffer).numFrames;
			nDef.set(\buffer, bufs.at(currentBuffer));
			nDef.set(\numFrames, loopFrames);
			bufs[currentBuffer].loadToFloatArray(action: {|array|
				sampleSlider.signalArray = array;
				{sampleSlider.refresh}.defer;
			});
		});
		sampleMenu.allowsReselection_(true);
		saveField = TextField.new(composite, Rect(82, 250, 68, 15)).background_(composite.background.blend(Color.grey, 0.8));
		saveButton = Button.new(composite, Rect(151, 250, 40, 15));
		saveButton.states_([["Save", Color.white, Color.new255(128,80,20,180)]]);
		saveButton.action_({
			var temp, fileName = (saveField.value ++ ".wav");
			temp = Buffer.alloc(Server.local, loopFrames, 1);
			bufs.at(currentBuffer).copyData(temp);
			bufs.put(fileName.asSymbol, temp);
			currentBuffer = fileName.asSymbol;
			sampleList = sampleList.add(fileName.asString);
			sampleMenu.items = sampleList;
			{0.1.wait; temp.write(samplePath.fullPath +/+ fileName, "WAV", "int16", numFrames:loopFrames, completionMessage: {(fileName ++ " saved").postln})}.fork;
		});
		// Button to turn interpolation on/off
		interpolationButton = Button.new(composite, Rect(151, 270, 40, 15));
		interpolationButton.toolTip="Turn this on to make your life more C O O L (turns interpolation off, try using at low rates)";
		interpolationButton.states_([
			["Boring", Color.fromHexString("222222"), Color.fromHexString("F0F0F0")],
			["C O O L", Color.black, Color.fromHexString("D690DA")],]);
		interpolationButton.action_({
			arg thisButton;
			switch(thisButton.value,
				0, {nDef.set(\interpolation, 4); this.rebuild},
				1, {nDef.set(\interpolation, 1); this.rebuild},
			);
		});
		interpolationButton.font = Font("Arial", 10, true);
		focusList = [labelKnob1, labelKnob2, labelKnob3];
		//oscFunc for record triggering.
		oscFunc = OSCFunc.newMatching({{recordButton.valueAction_(1)}.defer}, '/record');
		oscFunc2 = OSCFunc.newMatching({{recordButton.valueAction_(0)}.defer}, '/recordOff');
		this.rebuild;
		this.rebuild;
	}

	rebuild {
		Ndef(nDef.key.asSymbol, {
			arg buffer = bufs[0], numFrames, knobRate = 1, knobPos = 0, knobVolume = 0.8, knobwriteMix = 1, recordOn = 0, startPos = 0, endPos = 1, t_reset = 0, interpolation=4;
			var inputs = Silent.ar, sig, phase, writer, rateIn = 0, volumeIn = 0, posIn=0, phasePhase, readHead, record, resetIn = Silent.ar, recordIn = Silent.ar, rtrigIn = Silent.ar, rtrigOff, length;
			//numFrames = BufFrames.kr(buffer);
			labelKnob1.modList.do({|item|
				rateIn = rateIn + Ndef(item);
			});
			rateIn = LinLin.ar(rateIn, -1, 1, 0.2, 1.8);
			rateIn = knobRate * rateIn;
			labelKnob2.modList.do({|item|
				volumeIn = volumeIn + Ndef(item);0
			});
			volumeIn = LinLin.ar(volumeIn, -1, 1, 0, 1);
			volumeIn = (knobVolume.lag(0.07) * volumeIn).max(0).min(1);
			resetButton.inputList.do({|item|
				resetIn = resetIn + Ndef(item);
			});

			length = endPos - startPos; // Calculate length from GUI
			// Modulate sample start position
			samplePosLabelKnob.modList.do{
				|item|
				posIn = posIn + Ndef(item);
			};
			// add start (from gui), knob position, mod
			// but make sure it doesn't go too high for the length
			startPos = (startPos + posIn + knobPos).min(1-endPos).max(0);
			// re-calculate end position from new startPos
			endPos = startPos + length;
			phase = Phasor.ar(t_reset + resetIn, BufRateScale.kr(buffer) * rateIn, startPos * numFrames, endPos * numFrames, startPos * numFrames);
			//phaseIn = Select.ar(recordOn, [DC.ar(1), phase]); //turns record head on and off
			inputList.do({|item|
				if (item!= \none, {inputs = inputs + Ndef(item)});
			});
			inputs = Mix(inputs);
			record = recordOn.lag(0.02);
			overdubButton.inputList.do({|item|
				recordIn = recordIn + Ndef(item)
			});
			recordIn = (recordIn > 0);
			recordIn = (recordIn + record).max(0).min(1);
			readHead = BufRd.ar(1, buffer, phase, interpolation:1); //the writer works by continually rewriting old data into the buffer, mixed with new data. It has to write uninterpolated old data so the file doesn't get slowly degraded through repeated interpolation.
			sig = BufRd.ar(1, buffer, phase, 1, interpolation:interpolation);
			writer = BufWr.ar(
				inputs*knobwriteMix*recordIn + (readHead*(1-(knobwriteMix*recordIn))),
				buffer,
				phase);
			//the write head uses phaseIn, which is exactly the same as phase but can be switched on and off via an argument.

			//send a reply to the language to trigger re-recording.
			recordButton.inputList.do({|item|
				rtrigIn = rtrigIn + Ndef(item);
			});
			rtrigIn = A2K.kr(rtrigIn);
			rtrigOff = rtrigIn * -1;
			SendReply.kr(rtrigIn, '/record', 1);
			SendReply.kr(rtrigOff, '/recordOff', 1);
			sig = sig * volumeIn;
			sig;
		});
	}

	setBuf {
		arg which;
		currentBuffer = which;
		nDef.set(\buffer, bufs.at(currentBuffer));
		nDef.set(\numFrames, bufs.at(currentBuffer).numFrames);
	}

	refresh {
		arg numberofFrames;
		bufs[currentBuffer].loadToFloatArray(
			count: numberofFrames ?? {-1},
			action: {|array|
				sampleSlider.signalArray = array;
				{sampleSlider.refresh}.defer;
		});

	}

	focusOn {
		arg which;
		focus = which;
		focusList[which].focus(true);

	}

	save {
		var saveList;
		saveList = Dictionary.new;
		saveList.putPairs([
			\labelKnob1, labelKnob1.save,
			\labelKnob2, labelKnob2.save,
			\labelKnob3, labelKnob3.save,
			\samplePosLabelKnob, samplePosLabelKnob.save,
			\currentBuffer, currentBuffer,
			\sampleMenu, sampleMenu.value,
			\inputList, inputList,
			\inputBank, inputBank.save,
			\selectors, selectors.collect({|item| item.value}),
			\outputButton, outputButtons.collect({|item| item.value}),
			\lVal, sampleSlider.lVal,
			\rVal, sampleSlider.rVal,
			\recordButton, recordButton.save,
			\overdubButton, overdubButton.save,
			\resetButton, resetButton.save,
			\interpolationButton, interpolationButton.value,
		]);

		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		labelKnob1.load(loadList.at(\labelKnob1) ?? {nil});
		labelKnob2.load(loadList.at(\labelKnob2) ?? {nil});
		labelKnob3.load(loadList.at(\labelKnob3) ?? {nil});
		samplePosLabelKnob.load(loadList.at(\samplePosLabelKnob) ?? {nil});
		currentBuffer = (loadList.at(\currentBuffer) ?? {\temp});
		this.setBuf(currentBuffer);
		this.refresh;
		inputList = loadList.at(\inputList) ?? {\none!4};
		inputBank.load(loadList.at(\inputBank));
		{
			sampleMenu.value_(loadList.at(\sampleMenu) ?? {0});
			interpolationButton.value=(loadList.at(\interpolationButton) ?? {0});
		}.defer;
		// I was too lazy to make another pair of variables for interpolation, so...
		// Interpolation 0 -> 4, 1 -> 1
		nDef.set(\interpolation, (4-((loadList.at(\interpolationButton) ?? {0})*3)));

		outputButtons.do({|item, index|
			var isOn = ((loadList.at(\outputButton)??{0!4}).asArray[index]) ?? {0};
			{item.value_(isOn)}.defer;
			if (isOn == 1, {item.isOn = 1; item.doAction});
		});
		recordButton.load(loadList.at(\recordButton));
		overdubButton.load(loadList.at(\overdubButton));
		resetButton.load(loadList.at(\resetButton));

		// update lVal and rVal
		this.sampleSlider.setLVal(loadList.at(\lVal) ?? {0});
		this.sampleSlider.setRVal(loadList.at(\rVal) ?? {1});
		this.rebuild;
	}
}

TrigButton {
	var parent, bounds, string, oscPanel, colours, <composite, <button, <trigSwitch, <selectors, <inputList, <inputs;
	*new {
		arg parent, bounds, string, oscPanel, colours;
		^super.newCopyArgs(parent, bounds, string, oscPanel, colours).initTrigButton;
	}

	initTrigButton {
		var buttonBounds, trigBounds, selectorBounds, totalHeight;
		inputList = \none!2;
		inputs = (Ndef(\none));
		selectors = 0!2;
		composite = CompositeView.new(parent, bounds);
		button = Button.new(composite, Rect(1,0,composite.bounds.width - 2, composite.bounds.height/3));
		button.states_([
			[string, Color.white, colours[0]],
			[string, Color.white, colours[1]]
		]);
		button.action_({|button| this.doAction(button.value)});
		2.do({|i|
			selectors[i] = InputSelector.new(composite, 1, button.bounds.bottom + (i*15));
			selectors[i].selector.background_(~colourList.at(\none));
		});
		selectors.do({|item, index|
			item.selector.action_({|selector| this.setInput(selector.item, index); item.setColour; oscPanel.rebuild;});
		});
	}
	doAction {}
	setInput {
		arg which, index;
		inputList[index] = which.asSymbol;
		inputs = inputList.sum({|item| Ndef(item.asSymbol)});
	}

	save {
		var saveList;
		saveList = Dictionary.new;
		saveList.putPairs([
			\inputList, inputList,
			\selectors, selectors.collect({|item| item.value}),
			\colours, selectors.collect({|item| item.selector.background});
		]);
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		inputList = loadList.at(\inputList) ?? [\none, \none];
		inputs = inputList.sum({|item| Ndef(item.asSymbol)});
		if(loadList.at(\selectors).notNil, {{
			selectors.do({|item, index|
				item.value_(loadList.at(\selectors)[index]);
				if (loadList.at(\colours).notNil, {item.selector.background_(loadList.at(\colours)[index])});
			})
		}.defer;});
	}
}

ResetButton : TrigButton {

	doAction {
		oscPanel.nDef.set(\t_reset, 1);
	}

}

RecordButton : TrigButton {

	doAction {
		arg which;
		switch(which,
			0, {oscPanel.nDef.set(\recordOn, 0);
				oscPanel.endTime = Main.elapsedTime;
				oscPanel.loopFrames = oscPanel.endTime - oscPanel.startTime + 0.02 * oscPanel.sr;
				oscPanel.nDef.set(\numFrames, oscPanel.loopFrames);
				//nDef.set(\t_reset, 1);
				oscPanel.refresh(oscPanel.loopFrames);
			},
			1, {
				oscPanel.currentBuffer = \temp;
				oscPanel.nDef.set(\buffer, oscPanel.bufs.at(oscPanel.currentBuffer));
				oscPanel.nDef.set(\numFrames, oscPanel.bufs.at(oscPanel.currentBuffer).numFrames);
				oscPanel.nDef.set(\t_reset, 1);
				oscPanel.nDef.set(\recordOn, 1);
				oscPanel.startTime = Main.elapsedTime;
			}
		);
		//{button.value_(which)}.defer;
	}

	valueAction_ {
		arg value;
		button.valueAction_(value);
	}

}

OverdubButton : TrigButton {

	doAction {
		arg which;
		oscPanel.nDef.set(\recordOn, which);
		if (which == 0, {oscPanel.refresh(oscPanel.loopFrames);});

	}

}


