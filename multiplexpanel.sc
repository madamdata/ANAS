MultiPlexPanel : ANASPanel {
	var <>labelKnob1, <>labelKnob2, <>labelKnob3, <>selector, <>outList, <>selectors, <>outputButtons;

	*new {
	arg parent, bounds, nDef, outs;
		^super.newCopyArgs(parent, bounds, nDef, outs).initMultiPlex;

	}

	initMultiPlex {
		this.initANASPanel;
		label2 = StaticText.new(composite, Rect(0, 0, 190, 20));
		label2.string = nDef.key.asString;
		label2.font = Font("Arial", 22, true);
		label2.stringColor = Color.new(1,1,1,0.4);
		label2.align = \center;
		label2.background = Color(0,0,0,0);
		labelKnob1 = LabelKnob.new(composite, 2, 35, "fade", this);
		labelKnob2 = LabelKnob.new(composite, 49, 35, "volume", this);
		labelKnob3 = LFOKnob.new(composite, 96, 35, "lfo", this);
		inputBank = InputBank.new(composite, Rect(0, 20, 192, 30), this);
		/*selectors = 0!4;
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
		});*/
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 2 +((80/outs.size)*index), 130, (80/outs.size), nDef, whichOut);
		});
		Ndef(nDef.key.asSymbol).clear;
		focusList = [labelKnob1, labelKnob2, labelKnob3];
		standardAction = {|v,c,m,u,k|
			var keys = [m, k];
			switch(keys,
				[0, 49], {
					this.rebuild;
					keyRoutine.reset;
					{inputBank.update}.defer;
				},
				[1048576, 18], {selectors[0].valueAction_(1)},
				[1048576, 19], {selectors[0].valueAction_(2)},
				[0,18], {this.focusOn(0)},
				[0,19], {this.focusOn(1)},
				[0,20], {this.focusOn(2)},
				[0,21], {this.focusOn(3)},
				[0, 12], {outputButtons[0].flipRebuild},
				[0, 13], {outputButtons[1].flipRebuild},
				[0, 14], {outputButtons[2].flipRebuild},
				[0, 15], {outputButtons[3].flipRebuild},
				[0, 0], {
					composite.keyDownAction_(setInputAction);
					inputBank.setRed;
				},
			);
			nDef.key.asString.postln;
			true;
		};
		composite.keyDownAction_(standardAction);
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
			//fadeIn = K2A.ar(fadeIn);
			fadeIn = fadeIn;
			fadeIn = Mix((knobfade.linlin(0,1,-1,1) + fadeIn).max(-1).min(1));
			fadeIn = LinLin.ar(fadeIn, -1, 1, 0, 1);
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
			\inputBank, inputBank.save,
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
		inputBank.load(loadList.at(\inputBank));
		/*inputList.do({|item, index|
			{
				selectors[index].value_(~moduleList.indexOf(item));
				selectors[index].selector.background = (~colourList.at(item) ?? {Color.new255(200, 200, 200, 200)}).blend(Color.grey, 0.5);
			}.defer;
		});*/

		outputButtons.do({|item, index|
			var isOn = (loadList.at(\outputButton).asArray[index]) ?? {0};
			{item.value_(isOn)}.defer;
			if (isOn == 1, {item.isOn = 1; item.doAction});
		});
		this.rebuild;
	}
}