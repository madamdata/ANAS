FilterKnob {
	var parent, left, top, string, <>oscPanel, <>scale, <>spec, <>freqSpec, <>filtNum, <>default, <>knob1, <>knob2, <>onSwitch, <>typeSelector, <>inSelector, param, composite, knob1label, knob2label, <>modList, <>isOn;
	*new {
		arg parent, left, top, string, oscPanel, scale = 1, spec, freqSpec, filtNum, default;
		^super.newCopyArgs(parent, left, top, string, oscPanel, scale, spec, freqSpec, filtNum, default).initFilterKnob(parent, left, top, string, oscPanel, scale, spec, freqSpec, filtNum, default);
	}

	initFilterKnob{
	arg parent, left, top, string, oscPanel, scale, spec, freqSpec, filtNum, default;
		isOn = 0;
		default = default ?? {0.5!2};
		spec = spec ?? {ControlSpec(0, 1)};
		modList = [\none];
		composite = CompositeView.new(parent, Rect(left, top, 47*scale, 80*scale));
		composite.background_(Color.new255(85, 55, 155, 50));
		knob1label = StaticText.new(composite, Rect(2*scale, 2*scale, 40*scale, 15*scale));
		knob1label.align = \left;
		knob1label.font = (Font("Helvetica", 10*scale));
		knob1label.string = string;
		knob1label.stringColor = Color.new255(240, 205, 205, 200);
		knob1 = Knob.new(composite, Rect(13*scale, 8*scale, 34*scale, 34*scale));
		knob1.action = {|knob| oscPanel.nDef.set(("filt"++filtNum++"gain".asString).asSymbol, spec.map(knob.value))};
		knob1.value = 0.5;
		knob1.mode = \vert;
		knob1.step = 0.005;
		knob1.shift_scale = 1/10;
		knob1.color_([
			Color.new255(120, 10, 80, 190),
			Color.new255(25,10,25,205),
			Color.new255(230, 0, 40, 0),
			Color.new255(200, 150, 190, 245),
		]);
		knob2 = Knob.new(composite, Rect(2*scale, 40*scale, 25*scale, 25*scale));
		knob2.action = {|knob|
			var freq = freqSpec.map(knob.value);
			oscPanel.nDef.set(("filt"++filtNum++"freq".asString).asSymbol, freq);
			knob2label.string = freq.trunc(1).asString;
		};
		knob2.value = freqSpec.unmap(default[1]);
		knob2.mode = \vert;
		knob2.step = 0.005;
		knob2.shift_scale = 1/10;
		knob2.color_([
			Color.new255(120, 70, 200, 160),
			Color.new255(25,10,25,205),
			Color.new255(230, 50, 140, 0),
			Color.new255(200, 150, 190, 245),
		]);
		knob2label = TextField.new(composite, Rect(23, 40, 50, 10));
		knob2label.background = Color(0,0,0,0);
		knob2label.font = Font("Helvetica", 8);
		knob2label.string = default[1].asString;
		knob2label.stringColor = Color.new255(240, 205, 205, 200);
		knob2label.action_({|label|
			oscPanel.nDef.set(("filt"++filtNum++"freq".asString).asSymbol, label.value.asFloat);
			{knob2.value = freqSpec.unmap(label.value.asFloat)}.defer;
		});
		inSelector = InputSelector.new(composite, 1, 64);
	}

	save {
		var saveList;
		saveList = Dictionary.new;
		saveList.putPairs([
			\knob1, knob1.value,
			\knob2, knob2.value,
			\inSelector, inSelector.value,
		]);
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		{knob1.value = loadList.at(\knob1) ?? {spec.unmap(default[0])}}.defer;
		oscPanel.nDef.set(("filt"++filtNum++"gain".asString).asSymbol, spec.map(loadList.at(\knob1)??{default[0]}));
		{knob2.value = loadList.at(\knob2) ?? {freqSpec.unmap(default[1])};
			knob2label.string = (if (loadList.at(\knob2).notNil, {
				freqSpec.map(loadList.at(\knob2))}, {
				default[1]})).asString;
		}.defer;
		oscPanel.nDef.set(("filt"++filtNum++"freq".asString).asSymbol, freqSpec.map(loadList.at(\knob2)??{default[1]}));

	}
}

HiLoKnob : LabelKnob {

	doAction {
		arg value;
		/*oscPanel.nDef.set(("knobLo".asString).asSymbol,
			[40, 20000, 5].asSpec.map(value*2));
		oscPanel.nDef.set(("knobHi".asString).asSymbol,
			[25, 16000, 6].asSpec.map((value-0.5*2)).postln); */
		oscPanel.nDef.set(("knobLoHi").asSymbol, value);
	}


}


FilterPanel {
	var parent, left, top, <>nDef, outs, <composite, <label, <label2, <>labelKnob1, <>labelKnob2, <>labelKnob3, <>labelKnob4, <>labelKnob5, <>labelKnob6, <>labelKnob7, <>labelKnob8, <>labelKnob9, <>labelKnob10, <>outputButtons, <>inputList, selectors, spec, freqSpec, globalSpec;

	*new {
		arg parent, left, top, nDef, outs;
		^super.newCopyArgs(parent, left, top, nDef, outs).initFilterPanel;
	}

	initFilterPanel {
		composite = CompositeView.new(parent, Rect(left, top, 192, 300));
		composite.background = ~colourList.at(nDef.key) ?? {Color.new255(50, 50, 50, 50)};
		inputList = \none!4;
		spec = ControlSpec(0, 3.9);
		freqSpec = ControlSpec(40, 18000, \exp);
		globalSpec = ControlSpec(0.3, 1.7);
		/*label = StaticText.new(composite, Rect(0, 0, 190, 20));
		label.string = ("" ++ nDef.key.asString.toUpper);
		label.font = Font("courier", 18);
		label.stringColor = Color.new255(255,255,255,200);
		label.align = \center;
		label.background = Color(0,0,0,0);*/
		label2 = StaticText.new(composite, Rect(0, 0, 190, 20));
		label2.string = nDef.key.asString;
		label2.font = Font("Arial", 22, true);
		label2.stringColor = Color.new(1,1,1,0.6);
		label2.align = \center;
		label2.background = Color(0,0,0,0);
		labelKnob1 = FilterKnob.new(composite, 2, 37, "filt1", this, 1, spec, freqSpec, 1, [1,40]);
		labelKnob2 = FilterKnob.new(composite, 49, 37, "filt2", this, 1, spec, freqSpec, 2, [1,100]);
		labelKnob3 = FilterKnob.new(composite, 96, 37, "filt3", this, 1, spec, freqSpec, 3, [1,250]);
		labelKnob4 = FilterKnob.new(composite, 143, 37, "filt4", this, 1, spec, freqSpec,  4, [1,625]);
		labelKnob5 = FilterKnob.new(composite, 2, 120, "filt5", this, 1, spec, freqSpec, 5, [1, 1562]);
		labelKnob6 = FilterKnob.new(composite, 49, 120, "filt6", this, 1, spec, freqSpec, 6, [1, 3506]);
		labelKnob7 = FilterKnob.new(composite, 96, 120, "filt7", this, 1, spec, freqSpec, 7, [1, 9465]);
		labelKnob8 = FilterKnob.new(composite, 143, 120, "filt8", this, 1, spec, freqSpec, 8, [1, 14000]);
		labelKnob9 = LabelKnob.new(composite, 96, 203, "global", this, 1, globalSpec);
		labelKnob10 = HiLoKnob.new(composite, 143, 203, "LoHi", this, 1);
		selectors = 0!4;
		4.do({|i|
			selectors[i] = InputSelector.new(composite, i*48+2, 20)
		});
		selectors.do({|item, index|
			item.selector.background = ~colourList.at(\none).blend(Color.grey, 0.3);
			item.selector.action = {|selector|
				inputList[index] = selector.item.asSymbol;
				selector.background = (~colourList.at(selector.item.asSymbol) ?? {~colourList.at(\none)}).blend(Color.grey, 0.5);
			this.rebuild;
		};
		});
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 2 +((80/outs.size)*index), 282, (70/outs.size), nDef, whichOut);
		});
		this.rebuild;
	}

	rebuild {
		Ndef(nDef.key.asSymbol, {
			arg filt1gain = 1, filt2gain = 1, filt3gain = 1, filt4gain = 1, filt5gain = 1, filt6gain = 1, filt7gain = 1, filt8gain = 1, filt1freq = 40, filt2freq = 100, filt3freq = 250, filt4freq = 625, filt5freq =1562, filt6freq = 3506, filt7freq = 9465, filt8freq = 14000, knobglobal = 1, knobLoHi = 0.5;
			var inputs = Silent.ar, filts = 0!7, sig = 0, filtGains, filtFreqs, globalIn = 0, loIn = 0, hiIn = 0, loHiIn = 0;
			inputList.do({|item|
				inputs = inputs + Ndef(item);
			});
			inputs = Mix(inputs);
			filtGains = [filt1gain, filt2gain, filt3gain, filt4gain, filt5gain, filt6gain, filt7gain, filt8gain];
			filtFreqs = [filt1freq, filt2freq, filt3freq, filt4freq, filt5freq, filt6freq, filt7freq, filt8freq];
			labelKnob9.modList.do({|item|
				globalIn = globalIn + Ndef(item);
			});
			globalIn = LinLin.ar(globalIn, -1, 1, 0.5, 1.5);
			globalIn = (knobglobal * globalIn);

			8.do{|index|
				sig = sig + MoogFF.ar(inputs, (filtFreqs[index] * globalIn).min(19000), filtGains[index]);
			};
			labelKnob10.modList.do({|item|
				loHiIn = loHiIn + Ndef(item);
			});
			loHiIn = (knobLoHi.lag(0.05) + loHiIn).max(0).min(1);
			loIn = LinExp.ar(loHiIn*2, 0, 1, 40, 20000).min(20000);
			hiIn = LinExp.ar(loHiIn - 0.5 * 2, 0, 1, 10, 18000).max(10).min(18000);
			sig = DFM1.ar(sig, hiIn, 0.7, 0.4, type:1.0);
			sig = DFM1.ar(sig, loIn, 0.7, 0.4, 0);

			sig;
		});
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
			\labelKnob9, labelKnob9.save,
			\labelKnob10, labelKnob10.save,
			\selectors, selectors.collect({|item| item.value}),
			\inputList, inputList,
			\outputButton, outputButtons.collect({|item| item.value}),
		]);
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		inputList = loadList.at(\inputList) ?? {[\none, \none, \none, \none]};
		{
			if (loadList.at(\selectors).notNil, {
				loadList.at(\selectors).do({|item, index|
					selectors[index].value_(item);
					selectors[index].selector.background = (~colourList.at(selectors[index].selector.item.asSymbol) ?? {Color.new255(200, 200, 200, 200)}).blend(Color.grey, 0.5);
				});
			})
		}.defer;
		labelKnob1.load(loadList.at(\labelKnob1)??{nil});
		labelKnob2.load(loadList.at(\labelKnob2)??{nil});
		labelKnob3.load(loadList.at(\labelKnob3)??{nil});
		labelKnob4.load(loadList.at(\labelKnob4)??{nil});
		labelKnob5.load(loadList.at(\labelKnob5)??{nil});
		labelKnob6.load(loadList.at(\labelKnob6)??{nil});
		labelKnob7.load(loadList.at(\labelKnob7)??{nil});
		labelKnob8.load(loadList.at(\labelKnob8)??{nil});
		labelKnob9.load(loadList.at(\labelKnob9)??{nil});
		labelKnob10.load(loadList.at(\labelKnob10)??{nil});
		outputButtons.do({|item, index|
			var isOn = ((loadList.at(\outputButton)??{0!4}).asArray[index]) ?? {0};
			{item.value_(isOn)}.defer;
			if (isOn == 1, {item.isOn = 1; item.doAction});
		});
		this.rebuild;


	}

}