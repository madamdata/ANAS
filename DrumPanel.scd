DrumPanel : ANASPanel {
	var <labelKnob1, <labelKnob2, <labelKnob3, <labelKnob4, <labelKnob5, <labelKnob6, <labelKnob7, <labelKnob8, <outputButtons, trigButton;

	*new {
		arg parent, bounds, nDef;
		^super.newCopyArgs(parent, bounds, nDef).initDrumPanel;
	}

	initDrumPanel {
		this.initANASPanel;
		focus = 0;
		labelKnob1 = LabelKnob.new(composite, 2, 35, "Pitch", this, 1, [40, 10000, \exp]);
		labelKnob2 = LabelKnob.new(composite, 49, 35, "Atk", this);
		labelKnob3 = LabelKnob.new(composite, 96, 35, "Dec", this);
		labelKnob4 = LFOKnob.new(composite, 143, 35, "lfo", this, 1, ControlSpec(0,1));
		labelKnob5 = LabelKnob.new(composite, 2, 134, "pAtk", 1, [0.001, 0.7, \exp]);
		labelKnob6 = LabelKnob.new(composite, 2, 134, "pDec", 1, [0.001, 0.7, \exp]);
		trigButton = Button.new(composite, Rect(49, 150, 30, 30));
		trigButton.action_({|button| nDef.set(\t_trig, 1)});
	}

	rebuild {
		Ndef(nDef.key, {
			arg knobPitch = 200; knobAtk = 0.005; knobDec = 0.2, knobpAtk = 0.005, knobpDec = 0.1, t_trig = 0;
			var sig, env, pitchEnv, pitchIn, atkIn, decIn;
			env = EnvGen.ar(Env.perc(knobAtk, knobDec), t_trig);
			pitchEnv = EnvGen.ar(Env.perc(knobpAtk, knobpDec), t_trig);
			sig = SinOsc.ar(knobPitch * (pitchEnv + 1));
			sig = sig * env;			sig;


		})

	}

	save {
		var saveList = Dictionary.new;
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};

	}


}