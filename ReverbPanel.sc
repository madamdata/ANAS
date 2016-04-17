ReverbPanel : ANASPanel {
	var labelKnobs;

	*new {|parent, bounds, nDef, outs|
		^super.newCopyArgs(parent,bounds,nDef, outs).initReverbPanel;
	}

	initReverbPanel {
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
		labelKnobs[0] = LabelKnob.new(composite, 2, 31, "Mix", this, 1, [0,1].asSpec, 0.4, 1);
		labelKnobs[1] = LabelKnob.new(composite, 49, 31, "Room", this, 1, [0,1].asSpec, 0.65, 1);
		labelKnobs[2] = LabelKnob.new(composite, 96, 31, "Damp", this, 1, [0,1].asSpec, 0.05, 1);
		labelKnobs[3] = LabelKnob.new(composite, 143, 31, "Gain", this, 1, [0,10].asSpec, 0.2, 1);
		labelKnobs[4] = LabelKnob.new(composite, 2, 88, "Input", this, 1, [0,1].asSpec, 0.5, 1);

		//inputs
		inputBank = InputBank.new(composite, Rect(0, 19, 192, 30), this);


		//outputs
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 142 + ((50/outs.size)*index), 135, (50/outs.size), nDef, whichOut);
		});

	}

	rebuild {
		Ndef(nDef.key, {
			arg knobMix = 0.5, knobRoom = 0.5, knobDamp = 0.5, knobGain = 1.2, knobInput = 0.5;
			var inputs, sig, mixIn, roomIn, dampIn, gainIn, inputIn;
			inputIn = labelKnobs[4].modInputs + knobInput;
			inputIn = inputIn.max(0).min(1);
			inputs = inputList.sum({|item| Ndef(item.asSymbol)}) * inputIn;
			mixIn = (labelKnobs[0].modInputs + knobMix).min(1).max(0);
			sig = FreeVerb.ar(inputs, 1, knobRoom, knobDamp, knobGain);
			sig = sig.clip(-1,1);
			sig = sig * mixIn + (inputs * (1-mixIn));
		});
	}

	save {
		var saveList = Dictionary.new;
		labelKnobs.do({|item| saveList.put(item.string.asSymbol, item.save)});
		saveList.putPairs([
			\outputButtons, outputButtons.collect({|item| item.value}),
			\inputList, inputList,
			\inputBank, inputBank.save,
		]);
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		inputList = loadList.at(\inputList) ?? [\none, \none, \none, \none];
		inputBank.load(loadList.at(\inputBank));
		labelKnobs.do({|item| item.load(loadList.at(item.string.asSymbol))});
		outputButtons.do({|item, index|
			var isOn = (loadList.at(\outputButtons).asArray[index]) ?? {0};
			{item.value_(isOn)}.defer;
			if (isOn == 1, {item.isOn = 1; item.doAction});
		});
		this.rebuild;

	}



}

