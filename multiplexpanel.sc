MultiPlexPanel {
	var parent, left, top, <>nDef, <>outs, <>composite, <label, <label2, <>labelKnob1, <>labelKnob2, <>labelKnob3, <>selector, <>inputList, <>outList, <>selectors, <>outputButtons;
	*new {
		arg parent, left, top, nDef, outs;
		^super.newCopyArgs(parent, left, top, nDef, outs).initMultiPlex(parent, left, top, nDef, outs);
	}

	initMultiPlex {
		arg parent, left, top, nDef, outs;
		composite = CompositeView.new(parent, Rect(left, top, 192, 150));
		inputList = [\none, \none, \none, \none];
		composite.background = ~colourList.at(nDef.key) ?? {Color.new255(50, 50, 50, 50)};
		/*label = StaticText.new(composite, Rect(0, 0, 192, 15));
		label.string = ("" ++ nDef.key.asString.toUpper);
		label.font = Font("courier", 18);
		label.stringColor = Color.new255(255,255,255,200);
		label.align = \center;
		label.background = Color(0,0,0,0);*/
		label2 = StaticText.new(composite, Rect(0, 0, 190, 20));
		label2.string = nDef.key.asString;
		label2.font = Font("Arial", 22, true);
		label2.stringColor = Color.new(1,1,1,0.4);
		label2.align = \center;
		label2.background = Color(0,0,0,0);
		labelKnob1 = LabelKnob.new(composite, 2, 35, "fade", this);
		labelKnob2 = LabelKnob.new(composite, 49, 35, "volume", this);
		labelKnob3 = LFOKnob.new(composite, 96, 35, "lfo", this);
		selectors = 0!4;
		4.do({|i|
			selectors[i] = InputSelector.new(composite, i*48+2, 18)
		});

		selectors.do({|item, index|
			item.selector.background_(~colourList.at(\none));
			item.selector.action = {|selector|
				inputList[index] = selector.item.asSymbol;
				selector.background = (~colourList.at(selector.item.asSymbol) ?? {~colourList.at(\none)}).blend(Color.grey, 0.4);
			this.rebuild;
		};
		});
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 2 +((80/outs.size)*index), 130, (70/outs.size), nDef, whichOut);
		});
		Ndef(nDef.key.asSymbol).clear;
		this.rebuild;
	}

	rebuild {
		Ndef(nDef.key.asSymbol, {
			arg knobfade = 0.5, knobvolume = 1, knoblfo = 0.5;
			var input = List.new, sig, fadeIn = Silent.ar, volumeIn = 0, lfo, lfoIn = 0;
			inputList.do({|item, index|
				if (((item!=\none) && (item!=\dummy)), {
					input.add(Ndef(item));
				});
			});
			if (input.size == 0, {input.add(Ndef(\none))});
			labelKnob1.modList.do({|item|
				fadeIn = fadeIn + Ndef(item);
			});
			fadeIn = K2A.ar(fadeIn);
			//fadeIn = fadeIn/2;
			fadeIn = Mix((knobfade.linlin(0,1,-1,1) + fadeIn).max(-1).min(1));
			labelKnob2.modList.do({|item|
				volumeIn = volumeIn + Ndef(item);
			});
			volumeIn = LinLin.ar(volumeIn, -1, 1, 0, 1);
			volumeIn = (knobvolume * 2 * volumeIn).max(0).min(1);
			//lfoIn = knoblfo.linexp(0, 1, 0.1, 50);

			if (labelKnob3.isOn == 1, {
				labelKnob3.modList.do({|item|
					lfoIn = lfoIn + Ndef(item);
				});
				lfoIn = LinLin.ar(lfoIn, -1, 1, 0.2, 1.8);
				lfoIn = Mix(knoblfo.linexp(0, 1, 0.1, 50) * lfoIn);
				switch(labelKnob3.oscType.postln,
					\sin, {lfo = SinOsc.kr(lfoIn)},
					\saw, {lfo = LFSaw.kr(lfoIn)},
					\tri, {lfo = LFTri.kr(lfoIn)},
					\sq, {lfo = LFPulse.kr(lfoIn).bipolar * 0.8},
					\noise0, {lfo = LFNoise0.kr(lfoIn)},
					\noise1, {lfo = LFNoise1.kr(lfoIn)},
					\noise2, {lfo = LFNoise2.kr(lfoIn)},
				);
				fadeIn = fadeIn + lfo;
			});
			sig = CrossFader.ar(input, fadeIn, 2);
			sig = sig * volumeIn;
			sig;

		});

	}

	save {
		var saveList;
		saveList = Dictionary.new;
		saveList.putPairs([
			\labelKnob1, labelKnob1.save,
			\labelKnob2, labelKnob2.save,
			\labelKnob3, labelKnob3.save,
			\inputList, inputList,
			\outputButton, outputButtons.collect{|button| button.value},
		]);
		^saveList;


	}


	load {
		arg loadList;
		var inputs;
		loadList = loadList ?? {Dictionary.new};
		inputList = (loadList.at(\inputList) ?? {[\osc1, \osc2, \osc3, \osc4]});
		labelKnob1.load(loadList.at(\labelKnob1)??{nil});
		labelKnob2.load(loadList.at(\labelKnob2)??{nil});
		labelKnob3.load(loadList.at(\labelKnob3)??{nil});
		inputList.do({|item, index|
			{
				selectors[index].value_(~moduleList.indexOf(item));
				selectors[index].selector.background = (~colourList.at(item) ?? {Color.new255(200, 200, 200, 200)}).blend(Color.grey, 0.5);
			}.defer;
		});

		outputButtons.do({|item, index|
			var isOn = (loadList.at(\outputButton).asArray[index]) ?? {0};
			{item.value_(isOn)}.defer;
			if (isOn == 1, {item.isOn = 1; item.doAction});
		});
		this.rebuild;
	}
}