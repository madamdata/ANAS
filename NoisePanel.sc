NoisePanel : ANASPanel {
	var labelKnobs;

	*new {|parent, bounds, nDef, outs|
		^super.newCopyArgs(parent,bounds,nDef, outs).initNoisePanel;
	}

	initNoisePanel {
		this.initANASPanel;

		// label
		label2 = StaticText.new(composite, Rect(0, 0, 190, 20));
		label2.string = nDef.key.asString;
		label2.font = Font("Helvetica", 22, true);
		label2.stringColor = Color.new(1,1,1,0.4);
		label2.align = \left;
		label2.background = Color(0,0,0,0);

		//knobs
		labelKnobs = 0!5;
		labelKnobs[0] = LabelKnob.new(composite, 2, 20, "Freq", this, 1, [40,15000, \exp].asSpec, 0.65, 1);
		labelKnobs[1] = LabelKnob.new(composite, 49, 20, "Res", this, 1, [1.5,3.95].asSpec, 0.25, 1);
		labelKnobs[2] = LabelKnob.new(composite, 96, 20, "XFade", this, 1, [0,1].asSpec, 0.1, 1);
		labelKnobs[3] = LabelKnob.new(composite, 143, 20, "Gain", this, 1, [0,2].asSpec, 0.5, 1);
		labelKnobs[4] = LabelKnob.new(composite, 2, 79, "Rand", this, 1, [0,15].asSpec, 0.4, 1);


		//outputs
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 142 + ((50/outs.size)*index), 135, (50/outs.size), nDef, whichOut);
		});

	}

	rebuild {
		Ndef(nDef.key, {
			arg knobFreq = 1000, knobRes = 0.5, knobXFade = 0, knobGain = 0.5, knobRand = 5;
			var inputs, sig, randIn, freqIn, xFadeIn, resIn;
			xFadeIn = labelKnobs[2].modInputs + knobXFade;
			xFadeIn = xFadeIn.min(1).max(-1);
			resIn = LinLin.ar(labelKnobs[1].modInputs, -1, 1, 0.2, 1.8);
			resIn = resIn * knobRes;
			resIn = resIn.max(0.005);
			randIn = LinLin.ar(labelKnobs[4].modInputs, -1, 1, 0.2, 1.8);
			randIn = Demand.ar(Impulse.ar(randIn * knobRand), 0, DNoiseRing(0.2, 0.4, knobRand/2, 10));
			randIn = LinLin.ar(randIn, 0, 2**10, 0.2, 1.8) * (knobRand/15+1);
			freqIn = LinLin.ar(labelKnobs[0].modInputs, -1, 1, 0.2, 1.8);
			freqIn = (freqIn * randIn * knobFreq).min(14000).max(50);
			sig = XFade2.ar(WhiteNoise.ar(), LFNoise0.ar(freqIn), xFadeIn);
			sig = MoogFF.ar(sig, freqIn, resIn, 0) * knobGain;

		});
	}

	save {
		var saveList = Dictionary.new;
		labelKnobs.do({|item| saveList.put(item.string.asSymbol, item.save)});
		saveList.putPairs([
			\outputButtons, outputButtons.collect({|item| item.value}),
		]);
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		labelKnobs.do({|item| item.load(loadList.at(item.string.asSymbol))});
		outputButtons.do({|item, index|
			var isOn = (loadList.at(\outputButtons).asArray[index]) ?? {0};
			{item.value_(isOn)}.defer;
			if (isOn == 1, {item.isOn = 1; item.doAction});
		});
		this.rebuild;

	}



}

