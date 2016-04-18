OscPanel : ANASPanel {
	var <clockPanel, <labelKnobs, <>selector1, <>selector2, <>outputButtons, <>freqButton, <>oscType, <resetButton, <syncButton, <>lockButton, <>type, <>fadeTime, <>noteArrayField, <>transposeField, <>noteArray, <>transpose, <>distort, <>distortSelector, <>lock, <>syncToClock;
	*new {
		arg parent, bounds, nDef, outs, clockPanel;
		^super.newCopyArgs(parent, bounds, nDef, outs).initOscPanel(clockPanel);
	}

	initOscPanel {
		arg clockPanelArg;
		clockPanel = clockPanelArg;
		this.initANASPanel;

		// ------------------------- INITIALIZE VARIABLES -------------------------
		focus = 0;
		syncToClock = 0;
		type = \Sine;
		lock = 0; //lock prevents this panel from loading a save file if set to 1.
		noteArray = [0];
		transpose = 0;
		distort = 0;
		// ------------------------- !! INITIALIZE VARIABLES -------------------------

		// ------------------------- KEY COMMANDS -------------------------
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
		// -------------------------!! KEY COMMANDS -------------------------


		// ------------------------- Label -------------------------
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
		// ------------------------- !! Label -------------------------

		// ------------------------- DEFINE KNOBS  -------------------------
		labelKnobs = 0!8;
		labelKnobs[0] = LabelKnob.new(composite, 2, 20, "freq", this);
		labelKnobs[1] = LabelKnob.new(composite, 49, 20, "amp", this);
		labelKnobs[2] = LabelKnob.new(composite, 96, 20, "preFilter", this);
		labelKnobs[3] = LabelKnob.new(composite, 143, 20, "tone", this);
		labelKnobs[4] = LabelKnob.new(composite, 2, 117, "width", this);
		labelKnobs[5] = LabelKnob.new(composite, 49, 117, "distort", this);
		labelKnobs[6] = LabelKnob.new(composite, 96, 117, "postFilter", this, 1, [40,19000, \exp].asSpec, 0.8);
		labelKnobs[7] = LabelKnob.new(composite, 143, 117, "Q", this, 1, [1, 0.05].asSpec, 0.5);
		// ------------------------- !!DEFINE KNOBS  -------------------------

		// ------------------------- DEFINE BUTTONS -------------------------
		//// Lock button
		lockButton = Button.new(composite, Rect(155, 212, 35, 15));
		lockButton.font_(Font("Helvetica", 11));
		lockButton.states_([
			["lock", Color.grey.blend(Color.white, 0.5), Color.new255(70, 30, 210, 100)],
			["lock", Color.red.blend(Color.white, 0.5), Color.new255(210, 90, 210, 180)],
		]);
		lockButton.action_({|button| lock = button.value});
		lockButton.toolTip_("When active, this button prevents the entire panel from loading presets. \n All other unlocked panels will change when a preset is loaded.");

		////Reset button
		resetButton = Button.new(composite, Rect(155, 228, 35, 15));
		resetButton.font_(Font("Helvetica", 11));
		resetButton.states_([
			["reset", Color.grey.blend(Color.white, 0.5), Color.new255(40, 10, 0, 180)]
		]);
		resetButton.action_({this.reset});
		resetButton.toolTip_("Resets this panel to its default state");

		////Output buttons
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 2+((110/outs.size)*index), 263, (110/outs.size), nDef, whichOut);

		});

		////Frequency range button
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
		// ------------------------- !! DEFINE BUTTONS -------------------------

		// ------------------------- DEFINE FIELDS AND SELECTORS -------------------------
		//// Fade time
		fadeTime = FadeField.new(composite,Rect(155,263,35,15), nDef, 11);
		fadeTime.field.toolTip_("Use this field to set the Ndef's fade time in seconds.\n Fade time will be applied whenever you change an input source, the oscillator type,\n the note array field, or the distortion type. It will not be applied for transpose, knob movements, or setting outputs.\n
To fade output sends in, use the fadetime field on the output panel instead.");

		////Note array
		noteArrayField = TextField.new(composite, Rect(2, 245, 152, 16));
		noteArrayField.action_({|item|
			noteArray = item.value.tr($ , $/).split.asFloat;
			this.rebuild;
		});
		noteArrayField.background_(Color.new255(200, 140, 130, 95));
		noteArrayField.toolTip_("For polyphony, enter notes in semitones, separated by spaces - e.g. '0 3 7 10 14' \n Applies fade time.");

		////Transpose
		transposeField = TextField.new(composite, Rect(155, 245, 35, 16));
		transposeField.background = Color.new255(200, 110, 70, 130);
		transposeField.mouseDownAction_({|item| item.string = ""});
		transposeField.string_("transp").stringColor_(Color.white).font_(Font("Helvetica", 11));
		transposeField.action_({|item|
			transpose = item.value.asFloat;
			Ndef(nDef.key).set(\transpose, item.value.asFloat);
		});
		transposeField.toolTip_("Type in a number here to offset the frequency of this oscillator by \n that number of semitones. Fast compared to the note array field and does not apply\n fade time.");

		////Oscillator type
		oscType = PopUpMenu.new(composite, Rect(2, 280, 105, 17));
		oscType.items_(["DSaw", "DPulse", "LFSaw", "Sine", "LFPulse", "LFNoise0", "LFNoise1", "LFNoiseA", "WhiteNoise", "PinkNoise"])
		.action_({|selector|
			type = selector.item.asSymbol;
			this.rebuild;
		})
		.value_(3)
		.toolTip_("Use this menu to select the oscillator type.")
		.allowsReselection_(true)
		.background = (~colourList.at(nDef.key) ?? {Color.new(0.7, 0.5, 0.5, 0.8)}).blend(Color.grey, 0.4);

		////Distortion type
		distortSelector = PopUpMenu.new(composite, Rect(109, 280, 81, 17));
		distortSelector.background = Color.new255(217, 51, 90, 155).blend(Color.grey, 0.8);
		distortSelector.items_(["no dist", "tanh", "clip2", "distort", "fold2"]);
		distortSelector.action_({|selector|
			if (selector.value == 0, {distort = 0}, {distort = selector.item.asSymbol});
			this.rebuild;
		});
		// ------------------------- !! DEFINE FIELDS AND SELECTORS -------------------------

		//define list of things for keyboard focus switching
		focusList = labelKnobs ++ [noteArrayField, transposeField, fadeTime];
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
			labelKnobs[0].modList.do({|item|
				freqIn = freqIn + Ndef(item);
			});
			freqIn = LinExp.ar(freqIn, -1, 1, 0.1, 2.5) * 2;
			freqIn =(knobFreqIn.lag(0.07) * freqIn);
			freqIn = (freqIn * (noteArray + transpose).midiratio).min(19000);
			labelKnobs[1].modList.do({|item, index|
				if(index == 2, {ampIn = ampIn - Ndef(item)}, {ampIn = ampIn + Ndef(item)});
			});
			ampIn = LinLin.ar(ampIn, -1, 1, 0, 1);
			ampIn = (knobamp * 2 * ampIn).max(0).min(1);
			if (((type == \DSaw) || (type == \DPulse)), {
				knobpreFilterIn = knobpreFilter.linexp(0, 1, 100, 10000);
				labelKnobs[2].modList.do({|item|
					preFilterIn = preFilterIn + Ndef(item);
				});
				preFilterIn = LinExp.ar(preFilterIn, -1, 1, 0.5, 2);
				preFilterIn = (knobpreFilterIn * preFilterIn).min(20000);

			});
			if (((type == \DPulse)||(type == \LFPulse)||(type == \LFNoiseA)), {
				knobwidthIn = knobwidth.linlin(0,1, 0.02, 0.98);
				labelKnobs[4].modList.do({|item|
					widthIn = widthIn + Ndef(item);
				});
				widthIn = LinLin.ar(widthIn, -1, 1, 0.5, 1.5);
				widthIn = (knobwidthIn * widthIn).max(0.01).min(0.99);
			});
			if (distort != 0, {
			knobdistortIn = knobdistort.linlin(0, 1, 1, 20);
			labelKnobs[5].modList.do({|item|
				distortIn = distortIn + Ndef(item);
			});
			distortIn = LinLin.ar(distortIn, -1, 1, 0.5, 1.5);
			distortIn = (knobdistortIn * distortIn).max(1).min(20);
			});

			labelKnobs[6].modList.do({|item|
				postFilterIn = postFilterIn + Ndef(item);
			});
			postFilterIn = LinLin.ar(postFilterIn, -1, 1, 0.3, 1.7);
			postFilterIn = (knobpostFilter * postFilterIn).max(30).min(20000);
			labelKnobs[7].modList.do({|item|
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
		labelKnobs.do({|knob| knob.reset});
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
		labelKnobs.do({|item| saveList.put(item.string.asSymbol, item.save)});
		saveList.putPairs([
			\outputButton, outputButtons.collect{|button| button.value},
			\freqButton, freqButton.value,
			\oscType, oscType.value,
			\type, type,
			\noteArray, noteArray,
			\noteArrayField, noteArrayField.value,
			\transpose, transpose,
			\transposeField, transposeField.value,
			\distortVar, distort,
			\distortSelector, distortSelector.value,
		]);
		^saveList;
	}
	load {
		arg loadList;
		nDef.key.postln;
		loadList.at(\freq).postln;
		if (lock != 1, {
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
			labelKnobs.do({|item| item.load(loadList.at(item.string.asSymbol))});
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
			distort = loadList.at(\distortVar) ?? {0};
			{distortSelector.value  = loadList.at(\distortSelector) ?? {0}}.defer;
			this.rebuild;
		});
	}

}
