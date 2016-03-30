OscPanel : ANASPanel {
	var <clockPanel, <>labelKnob1, <>labelKnob2, <>labelKnob3, <>labelKnob4, <>labelKnob5, <>labelKnob6, <labelKnob7, <labelKnob8, <>selector1, <>selector2, <>outputButtons, <>freqButton, <>oscType, <resetButton, <syncButton, <>lockButton, <>type, <>fadeTime, <>noteArrayField, <>transposeField, <>noteArray, <>transpose, <>distort, <>distortSelector, <>lock, <>syncToClock;
	*new {
		arg parent, bounds, nDef, outs, clockPanel;
		^super.newCopyArgs(parent, bounds, nDef, outs).initOscPanel(clockPanel);
	}

	initOscPanel {
		arg clockPanelArg;
		clockPanel = clockPanelArg;
		this.initANASPanel;
		focus = 0;
		syncToClock = 0;
		type = \DSaw;
		lock = 0; //lock prevents this panel from loading a save file if set to 1.
		noteArray = [0];
		transpose = 0;
		distort = 0;
		composite.keyDownAction_({|v,c,m,u,k|
			var keys = [m, k];
			switch(keys,
				[0,18], {this.focusOn(0)},
				[0,19], {this.focusOn(1)},
				[0,20], {this.focusOn(2)},
				[0,21], {this.focusOn(3)},
				[131072,18], {this.focusOn(4)},
				[131072,19], {this.focusOn(5)},
				[131072,20], {this.focusOn(6)},
				[131072,21], {this.focusOn(7); transposeField.string_("")},
				[0, 12], {outputButtons[0].flipRebuild},
				[0, 13], {outputButtons[1].flipRebuild},
				[0, 14], {outputButtons[2].flipRebuild},
				[0, 15], {outputButtons[3].flipRebuild},
				[0,3], {
					freqButton.valueAction = (freqButton.value + 1)%3;
				},
				[0,2], {oscType.valueAction = (oscType.value + 1) % 10},
				[0,1], {oscType.valueAction = (oscType.value - 1) % 10},
				[0,7], {distortSelector.valueAction = (distortSelector.value - 1) % 5},
				[0,8], {distortSelector.valueAction = (distortSelector.value + 1) % 5}


			);
			true;
		});
		label2 = StaticText.new(composite, Rect(0, 0, 190, 20));
		label2.string = nDef.key.asString;
		label2.font = Font("Arial", 22, true);
		label2.stringColor = Color.new(1,1,1,0.4);
		label2.align = \center;
		label2.background = Color(0,0,0,0);
		label2.toolTip_("Oscillator panel \n
freq, amp: Oscillator frequency and final amplitude control\n
preFilter: (DSaw and DPulse only) fixed band pass filter before distortion, tone and amplitude \n
tone: (DSaw and DPulse only) frequency-dependent band pass filter before distortion and amplitude \n
width: (DPulse and LFPulse and LFNoiseA only) duty cycle of square waves. Left = more low values, right = more high values. \n
For LFNoiseA, controls speed of internal rate oscillator. Left = more consistent, right = more extreme fluctuations. \n
distort: Applies the selected distortion type. Knob controls pre-distortion gain. (tanh will distort even at knob = 0) \n
postFilter: Resonant low pass filter post distortion, pre-amplitude. \n
Q: postFilter resonance value \n");
		labelKnob1 = LabelKnob.new(composite, 2, 20, "freq", this);
		labelKnob2 = LabelKnob.new(composite, 49, 20, "amp", this);
		labelKnob3 = LabelKnob.new(composite, 96, 20, "preFilter", this);
		labelKnob4 = LabelKnob.new(composite, 143, 20, "tone", this);
		labelKnob5 = LabelKnob.new(composite, 2, 117, "width", this);
		labelKnob6 = LabelKnob.new(composite, 49, 117, "distort", this);
		labelKnob7 = LabelKnob.new(composite, 96, 117, "postFilter", this, 1, [40,19000, \exp].asSpec, 0.8);
		labelKnob8 = LabelKnob.new(composite, 143, 117, "Q", this, 1, [1, 0.05].asSpec, 0.5);
		lockButton = Button.new(composite, Rect(155, 212, 35, 15));
		lockButton.font_(Font("Helvetica", 11));
		lockButton.states_([
			["lock", Color.grey.blend(Color.white, 0.5), Color.new255(70, 30, 210, 100)],
			["lock", Color.red.blend(Color.white, 0.5), Color.new255(210, 90, 210, 180)],
		]);
		lockButton.action_({|button| lock = button.value});
		lockButton.toolTip_("When active, this button prevents the entire panel from loading presets. \n All other unlocked panels will change when a preset is loaded.");
		resetButton = Button.new(composite, Rect(155, 228, 35, 15));
		resetButton.font_(Font("Helvetica", 11));
		resetButton.states_([
			["reset", Color.grey.blend(Color.white, 0.5), Color.new255(40, 10, 0, 180)]
		]);
		resetButton.action_({this.reset});
		resetButton.toolTip_("Resets this panel to its default state");
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 2+((110/outs.size)*index), 263, (110/outs.size), nDef, whichOut);

		});
		freqButton = Button.new(composite, Rect(114, 263, 40, 15));
		freqButton.states_([
			["Audio", Color.black, Color.new255(100, 100, 150, 100)],
			["LFO", Color.black, Color.new255(150, 100, 100, 100)],
			["Sync", Color.white, Color.new255(150, 140, 210, 200)]
		]);
		freqButton.action_({|button|
			if (button.value == 0, {nDef.set(\freqMin, 40, \freqMax, 12000);this.unSync},
				{nDef.set(\freqMin, 0.1, \freqMax, 55);this.unSync});
			if (button.value == 2, {this.sync});

		});
		freqButton.font_(Font("Helvetica", 10));
		freqButton.toolTip_("This button switches the frequency range of the oscillator. \n
AUDIO: 40 - 12000 hz \n
LFO: 0.1-55hz \n
SYNC: Multiples and divisions of the clock time. NOTE: as of v1.0, only LFSaw is phase resettable");
		fadeTime = FadeField.new(composite,Rect(155,263,35,15), nDef, 11);
		fadeTime.field.toolTip_("Use this field to set the Ndef's fade time in seconds.\n Fade time will be applied whenever you change an input source, the oscillator type,\n the note array field, or the distortion type. It will not be applied for transpose, knob movements, or setting outputs.\n
To fade output sends in, use the fadetime field on the output panel instead.");
		noteArrayField = TextField.new(composite, Rect(2, 245, 152, 16));
		noteArrayField.action_({|item|
			noteArray = item.value.tr($ , $/).split.asFloat;
			this.rebuild;
		});
		noteArrayField.background_(Color.new255(200, 140, 130, 95));
		noteArrayField.toolTip_("For polyphony, enter notes in semitones, separated by spaces - e.g. '0 3 7 10 14' \n Applies fade time.");
		transposeField = TextField.new(composite, Rect(155, 245, 35, 16));
		transposeField.background = Color.new255(200, 110, 70, 130);
		transposeField.mouseDownAction_({|item| item.string = ""});
		transposeField.string_("transp").stringColor_(Color.white).font_(Font("Helvetica", 11));
		transposeField.action_({|item|
			transpose = item.value.asFloat;
			Ndef(nDef.key).set(\transpose, item.value.asFloat);
		});
		transposeField.toolTip_("Type in a number here to offset the frequency of this oscillator by \n that number of semitones. Fast compared to the note array field and does not apply\n fade time.");
		oscType = PopUpMenu.new(composite, Rect(2, 280, 105, 17));
		oscType.items_(["DSaw", "DPulse", "LFSaw", "Sine", "LFPulse", "LFNoise0", "LFNoise1", "LFNoiseA", "WhiteNoise", "PinkNoise"]);
		oscType.action_({|selector|
			type = selector.item.asSymbol;
			this.rebuild;
		});
		oscType.toolTip_("Use this menu to select the oscillator type.");
		oscType.allowsReselection = true;
		oscType.background = (~colourList.at(nDef.key) ?? {Color.new(0.7, 0.5, 0.5, 0.8)}).blend(Color.grey, 0.4);
		distortSelector = PopUpMenu.new(composite, Rect(109, 280, 81, 17));
		distortSelector.background = Color.new255(217, 51, 90, 155).blend(Color.grey, 0.8);
		distortSelector.items_(["no dist", "tanh", "clip2", "distort", "fold2"]);
		distortSelector.action_({|selector|
			if (selector.value == 0, {distort = 0}, {distort = selector.item.asSymbol});
			this.rebuild;
		});
		focusList = [labelKnob1, labelKnob2, labelKnob3, labelKnob4, labelKnob5, labelKnob6, noteArrayField, transposeField, fadeTime];
		//label2.front;
	}

	rebuild {
		nDef.reshaping = \elastic;
		Ndef(nDef.key.asSymbol, {
			arg knobfreq = 0.5, knobamp = 0.5, knobtone = 0.5, knobpreFilter = 0.5, knobwidth = 0.5,  knobdistort = 0, knobpostFilter = 15000, knobQ = 0.5, freqMin = 40, freqMax = 12000, transpose, t_reset = 0, sync = 0;
			var sig, freqIn = 0, ampIn=0, toneIn=0, preFilterIn=0, knobFreqIn = 0, knobpreFilterIn = 0, knobwidthIn = 0, knobdistortIn = 0, widthIn = 0, distortIn = 0, postFilterIn = 0, qIn = 0, sr = SampleRate.ir;

			toneIn = (knobtone.linlin(0,1,1, 15)).min(20000);
			knobFreqIn = Select.kr(sync, [knobfreq.linexp(0, 1, freqMin, freqMax),
				(Ndef(\clock)*(
					Select.kr(knobfreq*7, [0.25,0.5,1,2,3,4,6,8])
			))]);
			labelKnob1.modList.do({|item|
				freqIn = freqIn + Ndef(item);
			});
			freqIn = LinExp.ar(freqIn, -1, 1, 0.1, 2.5) * 2;
			freqIn =(knobFreqIn.lag(0.07) * freqIn);
			freqIn = (freqIn * (noteArray + transpose).midiratio).min(19000);
			labelKnob2.modList.do({|item, index|
				if(index == 2, {ampIn = ampIn - Ndef(item)}, {ampIn = ampIn + Ndef(item)});
			});
			ampIn = LinLin.ar(ampIn, -1, 1, 0, 1);
			ampIn = (knobamp * 2 * ampIn).max(0).min(1);
			if (((type == \DSaw) || (type == \DPulse)), {
				knobpreFilterIn = knobpreFilter.linexp(0, 1, 100, 10000);
				labelKnob3.modList.do({|item|
					preFilterIn = preFilterIn + Ndef(item);
				});
				preFilterIn = LinExp.ar(preFilterIn, -1, 1, 0.5, 2);
				preFilterIn = (knobpreFilterIn * preFilterIn).min(20000);

			});
			if (((type == \DPulse)||(type == \LFPulse)||(type == \LFNoiseA)), {
				knobwidthIn = knobwidth.linlin(0,1, 0.02, 0.98);
				labelKnob5.modList.do({|item|
					widthIn = widthIn + Ndef(item);
				});
				widthIn = LinLin.ar(widthIn, -1, 1, 0.5, 1.5);
				widthIn = (knobwidthIn * widthIn).max(0.01).min(0.99);
			});
			if (distort != 0, {
			knobdistortIn = knobdistort.linlin(0, 1, 1, 20);
			labelKnob6.modList.do({|item|
				distortIn = distortIn + Ndef(item);
			});
			distortIn = LinLin.ar(distortIn, -1, 1, 0.5, 1.5);
			distortIn = (knobdistortIn * distortIn).max(1).min(20);
			});

			labelKnob7.modList.do({|item|
				postFilterIn = postFilterIn + Ndef(item);
			});
			postFilterIn = LinLin.ar(postFilterIn, -1, 1, 0.3, 1.7);
			postFilterIn = (knobpostFilter * postFilterIn).max(30).min(20000);
			labelKnob8.modList.do({|item|
				qIn = qIn + Ndef(item);
			});
			qIn = LinLin.ar(qIn, -1, 1, 1.5, 0.5); //q has to be inverted because the ugen uses the reciprocal
			qIn = (knobQ * qIn).min(1).max(0.03);
			switch (type, //select oscillator type
				\DSaw, {sig = DSaw.ar(freqIn, toneIn, preFilter: preFilterIn, drift: 0.004, mul: 8.5)},
				\DPulse, {sig = DPulse.ar(freqIn, toneIn, preFilter: preFilterIn, drift: 0.004, mul: 8.5, width:widthIn)},
				\LFSaw, {sig = Phasor.ar(t_reset, freqIn*2/sr, -1, 1, -1)},
				\Sine, {sig = SinOsc.ar(freqIn)},
				\LFPulse, {sig = LFPulse.ar(freqIn, 0, widthIn)},
				\LFNoise0, {sig = LFNoise0.ar(freqIn)},
				\LFNoise1, {sig = LFNoise1.ar(freqIn)},
				\LFNoiseA, {sig = LFNoiseA.ar(-1, 1, LinExp.ar(widthIn, 0, 1, 0.5, 40), freqIn)},
				\WhiteNoise, {sig = WhiteNoise.ar(0.7)},
				\PinkNoise, {sig = PinkNoise.ar},
			);
			sig = Mix.new(sig);
			if (distort!=0, {sig = (sig * distortIn)});
			switch(distort, //select distortion type
				0, {},
				\tanh, {sig = sig.tanh},
				\clip2, {sig = sig.clip2(1)},
				\distort, {sig = sig.distort},
				\fold2, {sig = sig.fold2(1)},
			);
			sig = RLPF.ar(sig, postFilterIn, qIn, 1);
			sig = sig * ampIn;
			sig;
			});


	}

	sync {
		syncToClock = 1;
		~a.clock.clock.schedAbs(~a.clock.clock.nextTimeOnGrid, {
			nDef.set(\sync, 1);
			nDef.set(\t_reset, 1);
		});

	}

	unSync {
		syncToClock = 0;
		nDef.set(\sync, 0);
	}

	focusOn {
		arg which;
		focus = which;
		focusList[which].focus(true);

	}
	reset {
		labelKnob1.reset;
		labelKnob2.reset;
		labelKnob3.reset;
		labelKnob4.reset;
		labelKnob5.reset;
		labelKnob6.reset;
		noteArray = [0];
		distort = 0;
		this.rebuild;
		{
			noteArrayField.string = "";
			distortSelector.value = 0;
		}.defer;

	}

	save {
		var saveList = Dictionary.new;
		saveList.putPairs([
			\labelKnob1, labelKnob1.save,
			\labelKnob2, labelKnob2.save,
			\labelKnob3, labelKnob3.save,
			\labelKnob4, labelKnob4.save,
			\labelKnob5, labelKnob5.save,
			\labelKnob6, labelKnob6.save,
			\labelKnob7, labelKnob7.save,
			\labelKnob8, labelKnob8.save,
			\outputButton, outputButtons.collect{|button| button.value},
			\freqButton, freqButton.value,
			\oscType, oscType.value,
			\type, type,
			\noteArray, noteArray,
			\noteArrayField, noteArrayField.value,
			\transpose, transpose,
			\transposeField, transposeField.value,
			\distort, distort,
			\distortSelector, distortSelector.value,
		]);
		^saveList;
	}

	load {
		arg loadList;
		if (lock != 1, {
			if (loadList.at(\type).notNil, {
				type = loadList.at(\type).asSymbol;
				if (type == \dSaw, {type = \DSaw}); //backward compatibility
				{oscType.value = loadList.at(\oscType)}.defer;
			}, {
				oscType.valueAction = (loadList.at(\oscType));
				"deprecated save file".postln;
			});
			labelKnob1.load(loadList.at(\labelKnob1) ?? {nil});
			labelKnob2.load(loadList.at(\labelKnob2) ?? {nil});
			labelKnob3.load(loadList.at(\labelKnob3) ?? {nil});
			labelKnob4.load(loadList.at(\labelKnob4) ?? {nil});
			labelKnob5.load(loadList.at(\labelKnob5) ?? {nil});
			labelKnob6.load(loadList.at(\labelKnob6) ?? {nil});
			labelKnob7.load(loadList.at(\labelKnob7) ?? {nil});
			labelKnob8.load(loadList.at(\labelKnob8) ?? {nil});
			outputButtons.do({|item, index|
				var isOn = (loadList.at(\outputButton).asArray[index]) ?? {0};
				if (isOn == 1, {item.isOn = 1; item.doAction});
				{item.value_(isOn)}.defer;
			});
			switch(loadList.at(\freqButton),
				0, {nDef.set(\freqMin, 40, \freqMax, 12000);this.unSync},
				1, {nDef.set(\freqMin, 0.1, \freqMax, 55);this.unSync},
				2, {this.sync}
			);
			{freqButton.value_(loadList.at(\freqButton))}.defer;
			noteArray = loadList.at(\noteArray);
			{
				noteArrayField.value = (loadList.at(\noteArrayField) ?? {
					var temp = "";
					noteArray.do({|item| temp = temp ++ " " ++ item});
					temp;
				});
			}.defer;
			transpose = loadList.at(\transpose) ?? {0};
			Ndef(nDef.key).set(\transpose, loadList.at(\transpose) ?? {0});
			{transposeField.value = loadList.at(\transposeField) ?? {0}}.defer;
			distort = loadList.at(\distort) ?? {0};
			{distortSelector.value  = loadList.at(\distortSelector) ?? {0}}.defer;
			this.rebuild;
		});
	}

}
