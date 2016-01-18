ADSRPanel {
	var parent, left, top, <>nDef, outs, <composite, <label, <label2, <>labelKnob1, <>labelKnob2, <>labelKnob3, <>labelKnob4, <>labelKnob5, <>labelKnob6, <>inputList, selectors, specs;

	*new {
	arg parent, left, top, nDef, outs;
		^super.newCopyArgs(parent, left, top, nDef, outs).initADSRPanel;

	}

	initADSRPanel {
		composite = CompositeView.new(parent, Rect(left, top, 192, 300));
		composite.background = ~colourList.at(nDef.key) ?? {Color.new255(50, 50, 50, 50)};
		inputList = \none!4;
		specs = [
			ControlSpec(0.003, 5.5, \exp), //attack spec
			ControlSpec(0.003, 5.5, \exp), //decay spec
			ControlSpec(0,1), //sustain spec
			ControlSpec(0.003, 7, \exp), //release spec
		];
		/*label = StaticText.new(composite, Rect(0, 0, 190, 20));
		label.string = ("" ++ nDef.key.asString.toUpper);
		label.font = Font("courier", 18);
		label.stringColor = Color.new255(255,255,255,200);
		label.align = \center;
		label.background = Color(0,0,0,0); */
		label2 = StaticText.new(composite, Rect(0, 0, 190, 20));
		label2.string = nDef.key.asString;
		label2.font = Font("Arial", 22, true);
		label2.stringColor = Color.new(1,1,1,0.6);
		label2.align = \center;
		label2.background = Color(0,0,0,0);
		labelKnob1 = LabelKnob.new(composite, 2, 37, "Atk", this, 1, specs[0], 0);
		labelKnob2 = LabelKnob.new(composite, 49, 37, "Dec", this, 1, specs[1], 0.5);
		labelKnob3 = LabelKnob.new(composite, 96, 37, "Sus", this, 1, default: 0);
		labelKnob4 = LabelKnob.new(composite, 143, 37, "Rel", this, 1, specs[3]);
		labelKnob5 = LabelKnob.new(composite, 2, 134, "Level", this);
		labelKnob6 = LFOKnob.new(composite, 49, 134, "auto", this);
		selectors = 0!4;
		4.do({|i|
			selectors[i] = InputSelector.new(composite, i*48+2, 20);
		});
		selectors.do({|item, index|
			item.selector.background_(~colourList.at(\none).blend(Color.grey, 0.4));
			item.selector.action = {|selector|
				inputList[index] = selector.item.asSymbol;
				selector.background = (~colourList.at(selector.item.asSymbol) ?? {~colourList.at(\none)}).blend(Color.grey, 0.5);
				this.rebuild;
		};
		});

		Ndef(nDef.key.asSymbol, {
			arg knobAtk, knobDec, knobSus, knobRel, knobLevel;
			var trig = SinOsc.ar(3), env, sig, atkIn, decIn, susIn, relIn, levelIn;
			atkIn = knobAtk;
			decIn = knobDec;
			susIn = knobSus;
			relIn = knobRel;
			levelIn = knobLevel;
			env = EnvGen.ar(Env.adsr(atkIn, decIn, susIn, relIn), trig);
			sig = env * 2 - 1;
			sig = sig * (levelIn.lag(0.05));
			sig;
		});
		this.rebuild;

	}

	rebuild {
		Ndef(nDef.key.asSymbol, {
			arg knobAtk = 0, knobDec = 0.1, knobSus = 0, knobRel = 0.3, knobLevel = 1, knobauto, t_pgate = 0, hold = 0.1;
			var trig = Silent.ar, env, sig, atkIn = 0, decIn = 0, susIn = 0, relIn = 0, levelIn = 0, lfo, lfoIn = 0, patterntrig;
			inputList.do({|item|
				trig = trig + Ndef(item);
			});
			patterntrig = EnvGen.kr(Env.new([0,1,1,0], [0, hold, 0]), t_pgate);
			//patterntrig is an envelope that grabs the pgate argument and hold argument and uses it to time the opening and closing of the main envelope.
			trig = trig + patterntrig;
			if (labelKnob6.isOn == 1, {
				labelKnob6.modList.do({|item|
					lfoIn = lfoIn + Ndef(item);
				});
				lfoIn = LinLin.ar(lfoIn, -1, 1, 0.2, 1.8);
				lfoIn = Mix(knobauto.linexp(0, 1, 0.1, 50) * lfoIn);
				switch(labelKnob6.oscType,
					\sin, {lfo = SinOsc.kr(lfoIn)},
					\saw, {lfo = LFSaw.kr(lfoIn)},
					\tri, {lfo = LFTri.kr(lfoIn)},
					\sq, {lfo = LFPulse.kr(lfoIn).bipolar * 0.8},
					\noise0, {lfo = LFNoise0.kr(lfoIn)},
					\noise1, {lfo = LFNoise1.kr(lfoIn)},
					\noise2, {lfo = LFNoise2.kr(lfoIn)},
					\noiseA, {lfo = LFNoiseA.kr(-1,1, lfoIn, 1)};
				);
				trig = trig + lfo;
			});
			labelKnob1.modList.do({|item|
				atkIn = atkIn + Ndef(item);
			});
			atkIn = LinLin.ar(atkIn, -1, 1, 0.3, 1.7);
			atkIn = (knobAtk * atkIn);
			labelKnob2.modList.do({|item|
				decIn = decIn + Ndef(item);
			});
			decIn = LinLin.ar(decIn, -1, 1, 0.3, 1.7);
			decIn = (knobDec * decIn);
			labelKnob3.modList.do({|item|
				susIn = susIn + Ndef(item);
			});
			susIn = LinLin.ar(susIn, -1, 1, 0.5, 1.5);
			susIn = (knobSus * susIn).max(0).min(1);
			labelKnob4.modList.do({|item|
				relIn = relIn + Ndef(item);
			});
			relIn = LinLin.ar(relIn, -1, 1, 0.3, 1.7);
			relIn = (knobRel * relIn);
			env = EnvGen.ar(Env.adsr(atkIn, decIn, susIn, relIn), trig) * knobLevel.lag(0.08);
			sig = env * 2 - 1;
			sig;
		});

	}

	save {
	var saveList = Dictionary.new;
		saveList.putPairs([
			\atk, labelKnob1.save,
			\dec, labelKnob2.save,
			\sus, labelKnob3.save,
			\rel, labelKnob4.save,
			\level, labelKnob5.save,
			\auto, labelKnob6.save,
			\inputList, inputList
		]);
		^saveList;

	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		inputList = loadList.at(\inputList) ?? [\none, \none, \none, \none];
		labelKnob1.load(loadList.at(\atk) ?? {nil});
		labelKnob2.load(loadList.at(\dec) ?? {nil});
		labelKnob3.load(loadList.at(\sus) ?? {nil});
		labelKnob4.load(loadList.at(\rel) ?? {nil});
		labelKnob5.load(loadList.at(\level) ?? {nil});
		labelKnob6.load(loadList.at(\auto) ?? {nil});
		inputList.do({|item, index|
			{
				selectors[index].value_(~moduleList.indexOf(item));
				selectors[index].selector.background = (~colourList.at(item) ?? {Color.new255(200, 200, 200, 200)}).blend(Color.grey, 0.5);
			}.defer;
		});
		this.rebuild;
	}


}