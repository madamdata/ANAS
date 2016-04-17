DelayPanel : ANASPanel {
	var <>labelKnob1, <>labelKnob2, <>labelKnob3, <>labelKnob4, <>selectors, <>outputButtons, <>buffer, <>loadList;
	*new {
		arg parent, bounds, nDef, outs;
		^super.newCopyArgs(parent, bounds, nDef, outs).initDelayPanel;
	}

	initDelayPanel {
		this.initANASPanel;
		focus = 0;
		buffer = Buffer.alloc(Server.local, Server.local.sampleRate * 5, 1);
		inputList = \none!4;
		nDef.fadeTime = 5;
		keyRoutine = Routine{
			4.do({|i|
				if (whichPanel != \same, {
					inputList[i] = whichPanel;
				});
				i.yield
			})
		};
		standardAction = {|v,c,m,u,k|
			var keys = [m, k];
			switch(keys,
				[0, 49], {
					this.rebuild;
					keyRoutine.reset;
					{inputBank.update}.defer;
				},
				[1048576, 18], {selectors[0].valueAction_(1); "hi".postln},
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
		composite.canFocus_(true).keyDownAction_(standardAction);
		composite.background = ~colourList.at(nDef.key.asSymbol);
		/*label = StaticText.new(composite, Rect(0, 0, 192, 17));
		label.string = ("" ++ nDef.key.asString.toUpper);
		label.font = Font("courier", 18);
		label.stringColor = Color.new255(255,255,255,200);
		label.align = \center;
		label.background = Color(0,0,0,0);*/
		label2 = StaticText.new(composite, Rect(0, 0, 190, 20));
		label2.string = nDef.key.asString;
		label2.font = Font("Helvetica", 22, true);
		label2.stringColor = Color.new(1,1,1,0.4);
		label2.align = \center;
		label2.background = Color(0,0,0,0);
		inputBank = InputBank.new(composite, Rect(0, 20, 192, 30), this);
		/*selectors = 0!4;
		4.do({|i|
			selectors[i] = InputSelector.new(composite, i*48+2, 18)
		});
		selectors.do({|item, index|
			item.selector.background = ~colourList.at(\none).blend(Color.grey, 0.3);
			item.selector.action = {|selector|
				inputList[index] = selector.item.asSymbol;
				selector.background = (~colourList.at(selector.item.asSymbol) ?? {~colourList.at(\none)}).blend(Color.grey, 0.5);
			this.rebuild;
		};
		});*/
		labelKnob1 = LabelKnob.new(composite, 2, 35, "delayTime", this);
		labelKnob2 = LabelKnob.new(composite, 49, 35, "decayTime", this);
		labelKnob3 = LabelKnob.new(composite, 96, 35, "volume", this);
		labelKnob4 = LFOKnob.new(composite, 143, 35, "lfo", this, 1, ControlSpec(0,1));
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 2+((80/outs.size)*index), 130, (80/outs.size), nDef, whichOut);
		});
		Ndef(nDef.key.asSymbol, {
			arg input, delayTime = 0.3, decayTime = 1.5;
			var sig;
			sig = CombL.ar(input+SinOsc.ar(250, mul:0.1), 5, delayTime, decayTime);
			sig;
		});
		focusList = [labelKnob1, labelKnob2, labelKnob3, labelKnob4];
	}

	focusOn {
		arg which;
		focus = which;
		focusList[which].focus(true);

	}

	rebuild {
		Ndef(nDef.key.asSymbol, {
			arg knobdelayTime = 0.3, knobdecayTime = 2, knobvolume = 1, knoblfo = 0.5;
			var sig = 0, phase, buf, delayTimeIn = 0, decayTimeIn = 0, volumeIn = 0, lfoIn = 0, lfo;
			labelKnob1.modList.do({|item|
				delayTimeIn = delayTimeIn + Ndef(item);
			});
			if (labelKnob4.isOn == 1, {
				labelKnob4.modList.do({|item|
					lfoIn = lfoIn + Ndef(item);
				});
				lfoIn = LinLin.ar(lfoIn, -1, 1, 0.2, 1.8);
				lfoIn = Mix(knoblfo.linexp(0, 1, 0.1, 50) * lfoIn);
				switch(labelKnob4.oscType.postln,
					\sin, {lfo = SinOsc.kr(lfoIn)},
					\saw, {lfo = LFSaw.kr(lfoIn)},
					\tri, {lfo = LFTri.kr(lfoIn)},
					\sq, {lfo = LFPulse.kr(lfoIn).bipolar * 0.8},
					\noise0, {lfo = LFNoise0.kr(lfoIn)},
					\noise1, {lfo = LFNoise1.kr(lfoIn)},
					\noise2, {lfo = LFNoise2.kr(lfoIn)},
					\noiseA, {lfo = LFNoiseA.kr(lfoIn)}
				);
				delayTimeIn = delayTimeIn + (lfo*0.25);
			});
			delayTimeIn = LinLin.ar(delayTimeIn, -1, 1, 0.5, 2);
			delayTimeIn = (knobdelayTime.linexp(0,1, 0.01, 4).lag(0.4) * delayTimeIn).max(0.01).min(4);
			inputList.do({|item|
				sig = sig + Ndef(item);
			});
			labelKnob2.modList.do({|item|
				decayTimeIn = decayTimeIn + Ndef(item);
			});
			decayTimeIn = LinLin.ar(decayTimeIn, -1, 1, 0.5, 2);
			decayTimeIn = (knobdecayTime.linlin(0,1, 0.1, 6) * decayTimeIn).max(0.1).min(7);
			labelKnob3.modList.do({|item|
				volumeIn = volumeIn + Ndef(item);
			});
			volumeIn = LinLin.ar(volumeIn, -1, 1, 0, 1);
			volumeIn = (knobvolume * 2 * volumeIn).max(0).min(1);
			/*buf = LocalBuf(4*SampleRate.ir, 1);
			phase = DelTapWr.ar(buf, sig);
			sig = DelTapRd.ar(buf, phase, delayTimeIn, 4);*/
			sig = CombL.ar(sig, 5, delayTimeIn, decayTimeIn, mul:volumeIn);
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
			\labelKnob4, labelKnob4.save,
			\outputButton, outputButtons.collect{|button| button.value},
			\inputList, inputList,
			\inputBank, inputBank.save,
		]);
		^saveList;


	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		labelKnob1.load(loadList.at(\labelKnob1));
		labelKnob2.load(loadList.at(\labelKnob2));
		labelKnob3.load(loadList.at(\labelKnob3) ?? {nil});
		labelKnob4.load(loadList.at(\labelKnob4) ?? {nil});
		inputList = loadList.at(\inputList);
		if (inputList.size < 4, {inputList = (inputList.asArray ++ ((\none)!(4-inputList.size)))});
		/*inputList.do({|item, index|
			{
				selectors[index].value_(~moduleList.indexOf(item));
				selectors[index].selector.background = (~colourList.at(item) ?? {Color.new255(200, 200, 200, 200)}).blend(Color.grey, 0.5);
			}.defer;
		});*/
		inputBank.load(loadList.at(\inputBank));
		outputButtons.do({|item, index|
			var isOn = (loadList.at(\outputButton).asArray[index]) ?? {0};
			{item.value_(isOn)}.defer;
			if (isOn == 1, {item.isOn = 1; item.doAction});
		});
		this.rebuild;

	}
}