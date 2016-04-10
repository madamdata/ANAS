DrumPanel : ANASPanel {
	var <labelKnobs, <outputButtons, <trigButton, reverseButton, reverse, buf, patternField, pDef, durPat, multField, presetButtons, <presets, presetPat, editMode, editButton, <currentPreset, presetField, presetOverlays;

	*new {
		arg parent, bounds, nDef, outs;
		^super.newCopyArgs(parent, bounds, nDef, outs).initDrumPanel;
	}

	initDrumPanel {
		this.initANASPanel;
		buf = Buffer.alloc(Server.local, 512, 1, {|buf| buf.sine1Msg([1,1,1,1,1,1], true, true)});
		focus = 0;
		reverse = 0;
		presets = Dictionary.new!4;
		currentPreset = 0;
		editMode = 1;
		presetButtons = 0!4;
		presetOverlays = 0!4;
		label2 = StaticText.new(composite, Rect(0, 0, 190, 20));
		label2.string = nDef.key.asString;
		label2.font = Font("Helvetica", 22, true);
		label2.stringColor = Color.new(1,1,1,0.4);
		label2.align = \center;
		label2.background = Color(0,0,0,0);
		labelKnobs = 0!11;
		labelKnobs[0] = PresetLabelKnob.new(composite, 2, 20, "Pitch", this, 1, [40, 1000, \exp].asSpec, 0.2, 2);
		labelKnobs[1] = PresetLabelKnob.new(composite, 49, 20, "Dec", this, 1, [0.05, 1.8, \exp].asSpec, 0.75, 2);
		labelKnobs[2] = PresetLabelKnob.new(composite, 96, 20, "Level", this, 1, [0,1].asSpec, 1, 2);
		labelKnobs[3] = PresetLabelKnob.new(composite, 143, 20, "Mutate", this, 1, [0, 3].asSpec, 0.2, 2);
		labelKnobs[4] = PresetLabelKnob.new(composite, 2, 96, "pDec", this, 1, [0.001, 1, \exp].asSpec, 0.5, 1);
		labelKnobs[5] = PresetLabelKnob.new(composite, 49, 96, "pLevel", this, 1, [0, 8].asSpec, 0.75, 1);
		labelKnobs[6] = PresetLabelKnob.new(composite, 96, 96, "Noise", this, 1, [0,1].asSpec, 0.4, 1);
		labelKnobs[7] = PresetLabelKnob.new(composite, 143, 96, "Curve", this, 1, [-8, 8].asSpec, 0.3, 1);
		labelKnobs[8] = PresetLabelKnob.new(composite, 2, 160, "Shape", this, 1, [0, 0.55].asSpec, default:0.1, numSelectors: 1);
		labelKnobs[9] = PresetLabelKnob.new(composite, 49, 160, "Filt", this, 1, [150, 12000, \exp].asSpec, 0.8, 1);
		labelKnobs[10] = PresetLabelKnob.new(composite, 96, 160, "Distort", this, 1, [1,10].asSpec, 0, 1);
		presets.do({|preset|
			labelKnobs.do({|knob| preset.putPairs(knob.savePreset)});
			preset.put(\reverse, 0);
		});
		trigButton = DrumTrigButton.new(composite, Rect(143, 170, 47, 60), "trig", this, [Color.grey, Color.grey]);
		//trigButton.action_({|button| nDef.set(\t_trig, 1)});
		reverseButton = Button.new(composite, Rect(143, 220, 47, 15));
		reverseButton.states_([["rev", Color.white, Color.black], ["rev", Color.white, Color.red]]);
		reverseButton.action_({|button|
			reverse = button.value;
			nDef.set(\reverse, reverse);
			presets[currentPreset].put(\reverse, reverse);
		});
		durPat = Pdefn(((nDef.key) ++ "durPat").asSymbol, Pn(0.3));
		presetPat = Pdefn(((nDef.key) ++ "presetPat").asSymbol, Pn(0)).asStream;
		pDef = Pdef(nDef.key,
			Pbind(
				\midinote, \rest,
				\xyz, Pfunc({var which = presetPat.next;  this.recallPreset(which); nDef.set(\t_trig, 1); this.blinkPreset(which);}),
				\dur, durPat,
			)
		).play(~a.clock.clock);
		patternField = TextField.new(composite, Rect(3, 258, 185, 20)).background_(Color.new(0.35, 0.2, 0.12, 0.3)).stringColor_(Color.white).value_(1);
		patternField.action_({|thisField|
			var durations = thisField.value.tr($ , $/).split.collect({|item| item.interpret});
			if (multField.value != "0", {Pdefn(((nDef.key) ++ "durPat").asSymbol, Pseq(durations * multField.value.interpret, inf))});
			presetPat.reset;
			durPat.reset;
		});
		multField = TextField.new(composite, Rect(138, 280, 50, 20)).background_(Color.new(0.4, 0.15, 0.1,
		0.3)).stringColor_(Color.white).value_(0.25);
		multField.action_({|thisField|
			var durations = patternField.value.tr($ , $/).split.asFloat;
			if (thisField.value != "0", {Pdefn(((nDef.key) ++ "durPat").asSymbol, Pseq(durations * thisField.value.interpret, inf))});
			presetPat.reset;
			durPat.reset;
		});
		4.do({|i|
			presetOverlays[i] = CompositeView.new(composite, Rect(20*i + 2, 223, 20, 15)).background_(Color.new255(210,210, 210, 50)).acceptsMouse_(false);
			presetButtons[i] = Button.new(composite, Rect(20 * i + 2, 223, 20, 15));
			presetButtons[i].states_([[i.asString, Color.white, Color.new255(140, 100, 30, 100)], [i.asString, Color.white, Color.new255(190, 80, 40, 180)]]);
			presetButtons[i].action_({|thisButton|
				currentPreset = i;
				this.recallPresetWithUpdateNoChange(i);
				presetOverlays.do({|overlay, index| if (index == currentPreset, { //change overlay color to reflect currentPreset
					overlay.background_(Color.new255(230, 230, 10, 200));
				}, {overlay.background_(Color.new255(210,210,210,100))}
				)
				});
			});
		});
		presetField = TextField.new(composite, Rect(3, 238, 187, 20)).background_(Color.new(0.4, 0.3, 0.1, 0.5)).stringColor_(Color.white).value_("0");
		presetField.action_({|thisField|
			var presets = thisField.value.tr($ , $/).split.collect({|item| item.interpret});
			Pdefn(((nDef.key) ++ "presetPat").asSymbol, Pseq(presets, inf));
			presetPat.reset;
			durPat.reset;
		});
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 2+((80/outs.size)*index), 282, (80/outs.size), nDef, whichOut);
		});
		this.rebuild;
	}

	rebuild {
		Ndef(nDef.key, {
			arg knobPitch = 200, knobLevel = 1, knobDec = 0.2, knobpDec = 0.1, knobCurve = -4, knobpLevel = 1, t_trig = 0, reverse=0, knobNoise = 0.5, knobMutate = 1, knobShape = 0.5, knobFilt = 9000, knobDistort = 1, knobComp = 0.5;
			var sig, env, pEnv, pitchEnv, noiseEnv, pitchIn, decIn, pDecIn, curveIn, trigIn, shapeIn, filtIn, mutateIn, distortIn, atk = 0.008, pAtk = 0.006, timeDiff, mPitch, mpDec, mDec, mCurve, mNoise, mReverse, noiseIn, noise, nDecIn, nCurve;
			trigIn = t_trig + trigButton.inputs;
			mutateIn = LinLin.ar(labelKnobs[3].modInputs, -1, 1, 0.1, 1.9);
			mutateIn = mutateIn * knobMutate;
			mPitch = LFNoise1.kr(knobMutate * 3.3 * (knobpDec + 0.3)).range(0.9, 1.1);
			mDec = LFSaw.kr(knobMutate*5.7 * (knobDec + 1), 0.8).range(0.6, mutateIn+0.6);
			mpDec = LFSaw.kr(knobMutate*2.1, 0.3).range(0.6, 1.4);
			mCurve = LFSaw.kr(knobMutate*2.3, 0.4, mutateIn, 1);
			mNoise = LFPar.kr(knobMutate*1.6, 0.23, mutateIn*0.5, 1);
			mReverse = (Select.kr(TRand.kr(0, 0.8, trigIn) * mutateIn, [1, -1]) * (reverse*2-1)).max(0);
			decIn = LinLin.ar(labelKnobs[1].modInputs, -1, 1, 0.1, 1.9);
			decIn = (knobDec * mDec * decIn).max(0.004);
			pDecIn = LinLin.ar(labelKnobs[4].modInputs, -1, 1, 0.1, 1.9);
			pDecIn = (knobpDec *  mpDec * pDecIn).max(0.004);
			curveIn = knobCurve * mCurve;
			timeDiff = A2K.kr(decIn - pDecIn).max(0.005);
			pitchEnv = Select.ar(mReverse, [
				EnvGen.ar(Env.new([0,1,0,0], [pAtk, pDecIn, timeDiff]), trigIn, knobpLevel),
				EnvGen.ar(Env.new([0,0,1,0], [0.01, pDecIn, pAtk]), trigIn, knobpLevel)
			]);
			env = Select.ar(mReverse, [
				EnvGen.ar(Env.perc(atk, decIn, 1, curveIn), trigIn),
				EnvGen.ar(Env.new([0,0,1,0], [0.01, decIn, atk], (curveIn*(-1))), trigIn)
			]);
			nDecIn = A2K.kr(knobNoise * 0.13 * decIn);
			noiseEnv = Select.ar(mReverse, [
				EnvGen.ar(Env.perc(atk, nDecIn, 1, curveIn), trigIn, knobNoise),
				EnvGen.ar(Env.new([0,0,1,0], [0.01, nDecIn, atk], (curveIn*(-1))), trigIn, knobNoise)
			]);

			pitchIn = LinExp.ar(labelKnobs[0].modInputs, -1, 1, 0.1, 2.5);
			pitchIn = (knobPitch * (pitchEnv + 1) * pitchIn).max(50).min(18000) * mPitch;
			//noiseIn = knobNoise * 0.5 * mNoise;
			noise = HPF.ar(WhiteNoise.ar(0.3), 400, 1.4);
			noise = noise * noiseEnv;
			shapeIn = LinLin.ar(labelKnobs[8].modInputs, -1, 1, 0.5, 2);
			shapeIn = (shapeIn * knobShape * (env * env)).max(0);
			filtIn = LinLin.ar(labelKnobs[9].modInputs, -1, 1, 0.5, 1.5);
			filtIn = (knobFilt * filtIn * (pitchEnv).sqrt.sqrt.max(0.2)).max(120).min(19000);
			distortIn = LinLin.ar(labelKnobs[10].modInputs, -1, 1, 0.5, 1.5);
			distortIn = (distortIn * knobDistort).max(1);
			sig = SinOsc.ar(pitchIn);
			sig = Shaper.ar(buf, (sig*shapeIn*3) )* shapeIn + (sig * (1-shapeIn)) * knobLevel;
			sig = sig * env + noise;
			sig = MoogLadder.ar(sig*1.3, filtIn, 0.5, 2.2) * 2.0;
			sig = Compander.ar(sig, sig, 0.7, 1, 0.5, 0.008, 0.065, mul:1.3);
			sig = (sig * distortIn).clip2;
			//sig = SinOsc.ar(440);
			sig;


		})

	}

	updateReverse {
		nDef.set(\reverse, reverse);
		{reverseButton.value_(reverse)}.defer;
	}

	sync {
		~a.clock.clock.schedAbs(~a.clock.clock.nextTimeOnGrid, {
			pDef.reset;
			durPat.reset;
			presetPat.reset;
		});

	}

	recallPreset {
		arg which;
		var presetList = presets[which];
		switch(editMode,
			0, {labelKnobs.do({|item| item.doActionWithoutSavePlusUpdate(presetList[item.string.asSymbol])})},
			1, {labelKnobs.do({|item| item.doActionWithoutSave(presetList[item.string.asSymbol])})}
		);
		reverse = presetList[\reverse];
		nDef.set(\reverse, reverse);
		//{reverseButton.value_(reverse)}.defer;
	}

	recallPresetWithUpdateNoChange {
		arg which;
		var presetList = presets[which];
		labelKnobs.do({|item| {item.knob1.value_(presetList[item.string.asSymbol])}.defer});
		//reverse = presetList[\reverse];
		{reverseButton.value_(presetList[\reverse])}.defer;

	}

	blinkPreset {
		arg which;
		presetButtons.do({|button, i|
			if (i == which, {{button.value =1}.defer}, {{button.value = 0}.defer});
		});

	}

	save {
		var saveList = Dictionary.new;
		labelKnobs.do({|item| saveList.put(item.string.asSymbol, item.save)});
		saveList.putPairs([
			\trigButton, trigButton.save,
			\outputButtons, outputButtons.collect({|item| item.value}),
			\reverse, reverse,
			\presets, presets,
			\patternField, patternField.value,
			\presetField, presetField.value,
			\multField, multField.value,
			]);
		^saveList;
	}

	load {
		arg loadList;
		var durations, presetSeq, mult;
		loadList = loadList ?? {Dictionary.new};
		presets = loadList[\presets];
		durations = loadList[\patternField].tr($ , $/).split.collect({|item| item.interpret});
		presetSeq =  loadList[\presetField].tr($ , $/).split.collect({|item| item.interpret});
		mult = loadList[\multField].interpret;
		Pdefn(((nDef.key) ++ "durPat").asSymbol, Pseq(durations * mult, inf));
		Pdefn(((nDef.key) ++ "presetPat").asSymbol, Pseq(presetSeq, inf));
		labelKnobs.do({|item| item.load(loadList.at(item.string.asSymbol))});
		trigButton.load(loadList.at(\trigButton));
		reverse = loadList.at(\reverse);
		outputButtons.do({|item, index|
			var isOn = (loadList.at(\outputButtons).asArray[index]) ?? {0};
			{item.value_(isOn)}.defer;
			if (isOn == 1, {item.isOn = 1; item.doAction});
		});
		{
			patternField.value = loadList[\patternField];
			presetField.value = loadList[\presetField];
			multField.value = loadList[\multField];
		}.defer;
		this.updateReverse;
		this.rebuild;
	}



}

DrumTrigButton : TrigButton {

	doAction {
		oscPanel.nDef.set(\t_trig, 1);

	}
}

PresetLabelKnob : LabelKnob {
	doAction {
				arg value;
		oscPanel.nDef.set(("knob"++param.asString).asSymbol, spec.map(value));
		if (recording == 1, {
			var delta = (Main.elapsedTime - startTime - prevTime);
			var when = Main.elapsedTime - startTime;
			automationList.add([delta, value]);
			prevTime = when;
		});
		oscPanel.presets[oscPanel.currentPreset][string.asSymbol] = value;
	}

	doActionWithoutSave {
		arg value;
		oscPanel.nDef.set(("knob"++param.asString).asSymbol, spec.map(value));
		if (recording == 1, {
			var delta = (Main.elapsedTime - startTime - prevTime);
			var when = Main.elapsedTime - startTime;
			automationList.add([delta, value]);
			prevTime = when;
		});

	}
	doActionWithoutSavePlusUpdate {
						arg value;
		oscPanel.nDef.set(("knob"++param.asString).asSymbol, spec.map(value));
		if (recording == 1, {
			var delta = (Main.elapsedTime - startTime - prevTime);
			var when = Main.elapsedTime - startTime;
			automationList.add([delta, value]);
			prevTime = when;
		});
		{knob1.value = value}.defer;
	}

	savePreset {
		^[string.asSymbol, knob1.value];

	}
}