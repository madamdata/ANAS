ControlPanel : ANASPanel {
	var <ctrlDefs, invertButtons;

*new {|parent, bounds, nDef, outs|
		^super.newCopyArgs(parent,bounds,nDef, outs).initControlPanel;
	}

	initControlPanel {
		this.initANASPanel;
		ctrlDefs = 0!3;
		3.do({|index|
			ctrlDefs[index] = Ndef((nDef.key ++ "-" ++ index.asString).asSymbol)
		});
		// label
		label2 = StaticText.new(composite, Rect(0, 0, 190, 20));
		label2.string = nDef.key.asString;
		label2.font = Font("Helvetica", 22, true);
		label2.stringColor = Color.new(1,1,1,0.4);
		label2.align = \left;
		label2.background = Color(0,0,0,0);

		//knobs
		labelKnobs = 0!7;
		labelKnobs[0] = ControlKnob.new(composite, 2, 31, "Control1", this, 1, [-1, 1].asSpec, 0.5, 3);
		labelKnobs[1] = ControlSubKnob.new(composite, 42, 31, "mul1", this, 0.7, [0,1].asSpec, 1, 0, 0);
		labelKnobs[2] = ControlSubKnob.new(composite, 42, 60, "mul2", this, 0.7, [0,1].asSpec, 1, 0, 1);
		labelKnobs[3] = ControlSubKnob.new(composite, 42, 89, "mul3", this, 0.7, [0,1].asSpec, 1, 0, 2);
		labelKnobs[4] = ControlSubKnob.new(composite, 68, 31, "offs1", this, 0.7, [-1,1].asSpec, 0.5, 0, 0);
		labelKnobs[5] = ControlSubKnob.new(composite, 68, 60, "offs2", this, 0.7, [-1,1].asSpec, 0.5, 0, 1);
		labelKnobs[6] = ControlSubKnob.new(composite, 68, 89, "offs3", this, 0.7, [-1,1].asSpec, 0.5, 0, 2);

		//Invert Buttons
		invertButtons = 0!3;
		3.do({|i|
			invertButtons[i] = Button.new(composite, Rect(94, i*29+44, 20, 20))
			.states_([["inv", Color.white, Color.new255(150,100, 100, 150)],
				["inv", Color.white, Color.new255(220, 100, 50, 180)]])
			.action_({|thisButton| ctrlDefs[i].set(\invert, thisButton.value * (-2) + 1)});
		});



	}

	rebuild {
		Ndef(nDef.key, {
			arg knobControl1 = 0;
			var sig, inputs;
			inputs = labelKnobs[0].modInputs;
			sig = knobControl1 + inputs;
			sig;
		});
		Ndef(ctrlDefs[0].key, {
			arg knobControl1 = 0, knobmul1 = 1, knoboffs1 = 0, invert = 1;
			var sig, inputs;
			inputs = labelKnobs[0].modInputs;
			sig = knobControl1 + inputs;
			sig = sig * invert;
			sig = sig * knobmul1 + knoboffs1;
			sig;
		});
		Ndef(ctrlDefs[1].key, {
			arg knobControl1 = 0, knobmul2 = 1, knoboffs2 = 0, invert = 1;
			var sig, inputs;
			inputs = labelKnobs[0].modInputs;
			sig = knobControl1 + inputs;
			sig = sig * invert;
			sig = sig * knobmul2 + knoboffs2;
			sig;
		});
		Ndef(ctrlDefs[2].key, {
			arg knobControl1 = 0, knobmul3 = 1, knoboffs3 = 0, invert = 1;
			var sig, inputs;
			inputs = labelKnobs[0].modInputs;
			sig = knobControl1 + inputs;
			sig = sig * invert;
			sig = sig * knobmul3 + knoboffs3;
			sig;
		});

	}

	nDefNames {
		^[
			nDef.key,
			(nDef.key ++ "-0").asSymbol,
			(nDef.key ++ "-1").asSymbol,
			(nDef.key ++ "-2").asSymbol
		]
	}

	panelName {
		^nDef.key;
	}
	save {
		var saveList = Dictionary.new;
		labelKnobs.do({|item| saveList.put(item.string.asSymbol, item.save)});
		saveList.putPairs([
			\inverts, invertButtons.collect({|item| item.value})
		]);
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		invertButtons.do({|button, index|
			var val = loadList.at(\inverts)[index];
			ctrlDefs[index].set(\invert, val * (-2) + 1);
			{button.value = val}.defer;
		});
		labelKnobs.do({|item| item.load(loadList.at(item.string.asSymbol))});
		this.rebuild;

	}
}

ControlKnob : LabelKnob {

	doAction {
		arg value;
		oscPanel.nDef.set(("knob"++param.asString).asSymbol, spec.map(value));
		oscPanel.ctrlDefs.do({|def|
			def.set(("knob"++param.asString).asSymbol, spec.map(value));
		});
		if (recording == 1, {
			var delta = (Main.elapsedTime - startTime - prevTime);
			var when = Main.elapsedTime - startTime;
			automationList.add([delta, value]);
			prevTime = when;
		});
	}

}

ControlSubKnob : LabelKnob {
	var index;

		*new {
		arg parent, left, top, string, oscPanel, scale = 1, spec = ControlSpec(0,1), default = 0.5, numSelectors = 3, index = 0;
		^super.newCopyArgs(parent, left, top, string, oscPanel, scale, spec, default, numSelectors, index).initControlSubKnob;
	}

	initControlSubKnob {
		this.initLabelKnob;
		knob1.background_(~colourList.at(oscPanel.ctrlDefs[index].key));
		knob1label.align_(\center);

	}

	doAction {
		arg value;
		index.postln;
		oscPanel.ctrlDefs[index].set(("knob"++param.asString).asSymbol, spec.map(value));
		if (recording == 1, {
			var delta = (Main.elapsedTime - startTime - prevTime);
			var when = Main.elapsedTime - startTime;
			automationList.add([delta, value]);
			prevTime = when;
		});
	}

}
