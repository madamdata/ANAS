FilterPanel : ANASPanel {
	var <labelKnob1, <labelKnob2, <labelKnob3, <labelKnob4, <>labelKnob5, <>labelKnob6, <>labelKnob7, <>labelKnob8, <>labelKnob9, <>labelKnob10, <outputButtons, selectors, spec, freqSpec, globalSpec;

	*new {
	arg parent, bounds, nDef, outs;
		^super.newCopyArgs(parent, bounds, nDef, outs).initFilterPanel;

	}

	initFilterPanel {
		this.initANASPanel;
		composite = CompositeView.new(parent, bounds);
		composite.background = ~colourList.at(nDef.key) ?? {Color.new255(50, 50, 50, 50)};
		composite.canFocus_(true);
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
		labelKnob5 = FilterKnob.new(composite, 2, 104, "filt5", this, 1, spec, freqSpec, 5, [1, 1562]);
		labelKnob6 = FilterKnob.new(composite, 49, 104, "filt6", this, 1, spec, freqSpec, 6, [1, 3506]);
		labelKnob7 = FilterKnob.new(composite, 96, 104, "filt7", this, 1, spec, freqSpec, 7, [1, 9465]);
		labelKnob8 = FilterKnob.new(composite, 143, 104, "filt8", this, 1, spec, freqSpec, 8, [1, 14000]);
		labelKnob9 = LabelKnob.new(composite, 96, 203, "global", this, 1, globalSpec);
		labelKnob10 = LabelKnob.new(composite, 143, 203, "LoHi", this, 1);
		inputBank = InputBank.new(composite, Rect(0, 20, 192, 30), this);
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 2 +((80/outs.size)*index), 282, (80/outs.size), nDef, whichOut);
		});

		//KEYBOARD CONTROL
		focusList = [labelKnob1, labelKnob2, labelKnob3, labelKnob4, labelKnob5, labelKnob6, labelKnob7, labelKnob8, labelKnob9, labelKnob10];
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
				[131072,18], {this.focusOn(4)},
				[131072,19], {this.focusOn(5)},
				[131072,20], {this.focusOn(6)},
				[131072,21], {this.focusOn(7)},
				[131072,23], {this.focusOn(8)},
				[131072,22], {this.focusOn(9)},
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
			sig = DFM1.ar(sig, hiIn, 0.7, 0.05, type:1.0, mul: 8);
			sig = DFM1.ar(sig, loIn, 0.7, 0.05, 0, mul: 8);

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
			\inputBank, inputBank.save,
			\outputButton, outputButtons.collect({|item| item.value}),
		]);
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		inputList = loadList.at(\inputList) ?? {[\none, \none, \none, \none]};
		inputBank.load(loadList.at(\inputBank));
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


FilterKnob {
	var parent, left, top, string, <>oscPanel, <>scale, <>spec, <>freqSpec, <>filtNum, <>default, <>knob1, <>knob2, <>onSwitch, <>typeSelector, <>inSelector, param, <composite, knob1label, knob2label, <>modList, <>isOn, <automationList, prevTime, <recording, <automationRoutine, startTime;
	*new {
		arg parent, left, top, string, oscPanel, scale = 1, spec, freqSpec, filtNum, default;
		^super.newCopyArgs(parent, left, top, string, oscPanel, scale, spec, freqSpec, filtNum, default).initFilterKnob(parent, left, top, string, oscPanel, scale, spec, freqSpec, filtNum, default);
	}

	initFilterKnob{
	arg parent, left, top, string, oscPanel, scale, spec, freqSpec, filtNum, default;
		automationList = List.new;
		automationRoutine = Routine {
			loop {
				automationList.do({|item|
					if (item[0].notNil, {
					item[0].wait;
					this.doAction(item[1]);
						{knob1.value_(item[1])}.defer;
					}, {1.wait; "automationList is empty! Error.".postln;});
				})
			}
		};
		startTime = 0;
		recording = 0;
		isOn = 0;
		default = default ?? {0.5!2};
		spec = spec ?? {ControlSpec(0, 1)};
		modList = [\none];
		composite = CompositeView.new(parent, Rect(left, top, 47*scale, 67*scale));
		composite.background_(Color.new255(85, 55, 155, 50));
		knob1label = StaticText.new(composite, Rect(2*scale, 2*scale, 40*scale, 15*scale));
		knob1label.align = \left;
		knob1label.font = (Font("Helvetica", 10*scale));
		knob1label.string = string;
		knob1label.stringColor = Color.new255(240, 205, 205, 200);
		knob1 = Knob.new(composite, Rect(13*scale, 8*scale, 34*scale, 34*scale));
		knob1.action = {|knob| this.doAction(knob.value);};
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
		knob1.keyDownAction_({|v,c,m,u,k|
			var keys = [m,k];
			switch(keys,
				[0,15], {this.reset;oscPanel.rebuild},
				[2097152, 126], {knob1.valueAction = (knob1.value + 0.01)},
				[2097152, 125], {knob1.valueAction = (knob1.value - 0.01)},
				[2097152, 123], {knob1.valueAction = (knob1.value - 0.122)},
				[2097152, 124], {knob1.valueAction = (knob1.value + 0.122)},
				[2228224, 126], {knob1.valueAction = (knob1.value + 0.001)},
				[2228224, 125], {knob1.valueAction = (knob1.value - 0.001)},
				[2228224, 123], {knob1.valueAction = (knob1.value - 0.05)},
				[2228224, 124], {knob1.valueAction = (knob1.value + 0.05)},
				[0,0], {oscPanel.focusOn((oscPanel.focus - 1).mod(oscPanel.focusList.size))},
				[0,2], {oscPanel.focusOn((oscPanel.focus + 1).mod(oscPanel.focusList.size))},
				[0,13], {oscPanel.focusOn((oscPanel.focus - 4).mod(oscPanel.focusList.size))},
				[0,1], {oscPanel.focusOn((oscPanel.focus + 4).mod(oscPanel.focusList.size))},
			);
			true;
		});
		knob1.keyUpAction_({|a,b,c,d,e,f|
			var keys = f;
			//keys.postln;
			switch (f,
				//on ctrl - UP, turn recording off, begin automating.
				16777250, {
					if (recording == 1, {
						var delta = (Main.elapsedTime - startTime - prevTime); //add one last entry for the end of the loop
						var when = Main.elapsedTime - startTime;
						automationList.add([delta, knob1.value]);
						this.startAutomation;
					});
					recording = 0;
					//"ctrl UP".postln;
				}
			)
		});
		knob1.mouseDownAction_({|xx, xxx, xxxx, mod|
			mod.postln;
			switch(mod,
				/*131072, {
					("mapping "++oscPanel.nDef.key.asString++" "++string).postln;
					this.learn;
				},
				524288, {
					this.deMap;
				},*/
				655360, {this.resetSelectors;oscPanel.rebuild},

				262144, { // on ctrl-click, start a new automation list, store start time, and reset prevTime.
					if (recording == 0, {
					automationList = List.new;
					automationRoutine.stop;
					startTime = Main.elapsedTime;
					prevTime = 0;
					"recording automation".postln;
					recording = 1;
					});
				},
				393216, {//on ctrl-shift click, kill all automation, reset automation list, make sure recording is off.
					automationRoutine.stop;
					recording = 0;
					"stopping".postln;
				}
			);
		});
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
	}

	doAction {
		arg value;
		oscPanel.nDef.set(("filt"++filtNum++"gain".asString).asSymbol, spec.map(value));
		if (recording == 1, {
			var delta = (Main.elapsedTime - startTime - prevTime);
			var when = Main.elapsedTime - startTime;
			automationList.add([delta, value]);
			prevTime = when;
		});

	}

	focus {
		arg val;
		knob1.focus(val);

	}

	startAutomation {
		automationRoutine.reset;
		automationRoutine.play;
	}

	save {
		var saveList;
		saveList = Dictionary.new;
		saveList.putPairs([
			\knob1, knob1.value,
			\knob2, knob2.value,
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


