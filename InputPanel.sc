InputPanel : ANASPanel {
	var <inputType, <inputTypeSelector, <labelKnob1;

	*new {
		arg parent, bounds, nDef;
		^super.newCopyArgs(parent, bounds, nDef).initInputPanel;
	}

	initInputPanel {
		this.initANASPanel;
		thingsToSave = Dictionary.new;
		inputType = \audio;
		nDef.mold(1, \audio);
		inputTypeSelector = PopUpMenu.new(composite, Rect(2, 20, 50, 15));
		inputTypeSelector.items_(["audio", "amp"]);
		inputTypeSelector.action_({|selector|
			inputType = selector.item.asSymbol;
			this.rebuild;
		});
		inputTypeSelector.font_(Font("Helvetica", 10));
		labelKnob1 = LabelKnob.new(composite, 55, 2, "gain", this, 0.8, [0,2].asSpec, default:1, numSelectors: 0);
		thingsToSave.putPairs([\inputTypeSelector, inputTypeSelector, \inputType, inputType,\labelKnob1, labelKnob1]);
		focusList = [labelKnob1];
		this.rebuild;

	}

	rebuild {
		Ndef(nDef.key, {
			arg knobgain = 1;
			var sig;
			sig = SoundIn.ar(0) * (knobgain).lag(0.08);
			switch(inputType,
				\audio, {},
				\amp, {sig = LinLin.ar(Amplitude.ar(sig), 0, 1, -1, 1)},
			);
			sig;
		});
	}

	save {
		var saveList = Dictionary.new;
		saveList.putPairs([
			\inputTypeSelector, inputTypeSelector.value,
			\inputType, inputType,
			\labelKnob1, labelKnob1.save
		]);
		^saveList;

	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		inputType = loadList.at(\inputType) ?? {\audio};
		{inputTypeSelector.value_(loadList.at(\inputTypeSelector) ?? {0})}.defer;
		labelKnob1.load(loadList.at(\labelKnob1));
		this.rebuild;

	}
}
