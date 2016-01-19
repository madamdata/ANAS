InputPanel : ANASPanel {
	var <inputType, <inputTypeSelector, labelKnob1;

	*new {
		arg parent, bounds, nDef;
		^super.newCopyArgs(parent, bounds, nDef).initInputPanel;
	}

	initInputPanel {
		this.initANASPanel;
		thingsToSave = 0!3;
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
}
