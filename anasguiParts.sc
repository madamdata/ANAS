OutputButton {
	var parent, left, top, width, <>in, <>outPanel, <>button, <>isOn, oscPath, oscFunc;
	*new {
		arg parent, left, top, width, in, outPanel;
		^super.newCopyArgs(parent, left, top, width, in, outPanel).initOutputButton(parent, left, top, width, in, outPanel);
	}

	initOutputButton {
		arg parent, left, top, width, in, outPanel;
		var string;
		oscPath = ("/" ++ in.key.asString ++ "/" ++ outPanel.nDef.key.asString).asSymbol;
		isOn = 0;
		string = (if (width > 15, {outPanel.nDef.key.asString}, {outPanel.nDef.key.asString.drop(3)}));
		button = Button.new(parent, Rect(left, top, width, 15));
		button.states_([
			[string, Color.white.blend(Color.grey, 0.5), ~colourList.at(outPanel.nDef.key).blend(Color.grey, 0.8).blend(Color.black, 0.3)],
			[string, Color.white, ~colourList.at(outPanel.nDef.key)]]);
		button.action_({|button|
			if (button.value == 1, { //update list of oscs to be output, rebuild ndef
				outPanel.outList.add(in.key.asSymbol);
				isOn = 1;
				outPanel.rebuild;

			},{
				outPanel.outList.remove(in.key.asSymbol);
				isOn = 0;
				outPanel.rebuild;
			}
			);
		});
		button.font_(Font("Helvetia", 8));
		button.toolTip_("Click on this button to send this panel's output to the respective Output Panel.");
		oscFunc = OSCFunc.newMatching({|msg| isOn = msg[1]*(-1)+1; {this.flipRebuild}.defer}, oscPath);

	}

	value {
		^button.value;
	}

	value_ {
		arg input;
		^button.value_(input);
	}

	valueAction_ {
		arg thing;
		button.valueAction = thing;
	}

	doAction {
		if (isOn == 1, {
			outPanel.outList.add(in.key.asSymbol);
		}, {
			outPanel.outList.remove(in.key.asSymbol);
		}
		);
	}

	flipRebuild {
		if (isOn == 1, {
			isOn = 0;
			this.doAction;
			outPanel.rebuild;
			button.value_(isOn);
		}, {
			isOn = 1;
			this.doAction;
			outPanel.rebuild;
			button.value_(isOn);
		}
		);

	}

}

LabelKnob {
	var parent, left, top, <>string, oscPanel, scale, <>spec, <>default, <>numSelectors, <composite, <>knob1, <>knob1label, <>action, param, <>selector1, <>selector2, <>selector3, <>selectors, <>saveList, <>modList, <modInputs, <>midiFunc, <>mapped, <keyRoutine, <whichPanel, <automationList, prevTime, <recording, <automationRoutine, startTime, <oscFunc, oscString;
	*new {
		arg parent, left, top, string, oscPanel, scale = 1, spec = ControlSpec(0,1), default = 0.5, numSelectors = 3;
		^super.newCopyArgs(parent, left, top, string, oscPanel, scale, spec, default, numSelectors).initLabelKnob;
	}

	initLabelKnob {
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
		spec = spec ?? {ControlSpec(0, 1)};
		mapped = 0;
		param = string.asSymbol;
		modList = \none!(numSelectors);
		modInputs = Ndef(\none);
		composite = CompositeView.new(parent, Rect(left, top, 47*scale, (15*numSelectors+47)*scale));
		composite.background_(Color.new255(85, 55, 155, 50));
		knob1label = StaticText.new(composite, Rect(2*scale, 2*scale, 40*scale, 15*scale));
		knob1label.align = \left;
		knob1label.font = (Font("Helvetica", 11*scale));
		knob1label.string = param.asString;
		knob1label.stringColor = Color.new255(255, 225, 225, 240);
		knob1 = Knob.new(composite, Rect(8*scale, 14*scale, 34*scale, 34*scale));
		knob1.action = {|knob| this.doAction(knob.value)};
		knob1.value = default;
		knob1.mode = \vert;
		knob1.step = 0.005;
		knob1.shift_scale = 1/10;
		knob1.toolTip_("Shift-click to map to the next MIDI control message received \n
Opt-click to remove MIDI mapping\n
Ctrl-click and hold ctrl while moving the knob to record automation. Release ctrl to begin automation. \n
Ctrl-shift-click to remove automation.");
		knob1.color_([
			Color.new255(120, 10, 80, 190),
			Color.new255(25,10,25,205),
			Color.new255(220, 0, 30, 0),
			Color.new255(190, 150, 180, 245),
		]);
		knob1.focusColor_(Color.new255(255, 30, 80, 200));
		//KEYBOARD KONTROLLL
		keyRoutine = Routine{
			numSelectors.do({|i|
				if (whichPanel != \same, {
					modList[i] = whichPanel;
				});
				i.yield
			})
		};
		knob1.keyDownAction_({|v,c,m,u,k|
			var keys = [m,k];
			switch(keys,
				[0,15], {this.reset;oscPanel.rebuild},
				[0,49], {
					modInputs = modList.sum({|item| Ndef(item.asSymbol)});
					oscPanel.rebuild;
					keyRoutine.reset;
					{
						selectors.do({|item, index|
							item.value_(~moduleList.indexOf(modList[index]));
							item.background = (~colourList.at(item.item.asSymbol) ?? {~colourList.at(\none)});
							item.update;
						});
					}.defer;
				},
				[0,50], {whichPanel = \none; keyRoutine.next},
				[0,18], {whichPanel = ~moduleList[1]; keyRoutine.next},
				[0,19], {whichPanel = ~moduleList[2]; keyRoutine.next},
				[0,20], {whichPanel = ~moduleList[3]; keyRoutine.next},
				[0,21], {whichPanel = ~moduleList[4]; keyRoutine.next},
				[0,23], {whichPanel = ~moduleList[5]; keyRoutine.next},
				[131072, 18], {whichPanel = ~moduleList[6]; keyRoutine.next},
				[131072, 19], {whichPanel = ~moduleList[7]; keyRoutine.next},
				[131072, 20], {whichPanel = ~moduleList[8]; keyRoutine.next},
				[131072, 21], {whichPanel = ~moduleList[9]; keyRoutine.next},
				[131072, 23], {whichPanel = ~moduleList[10]; keyRoutine.next},
				[131072, 22], {whichPanel = ~moduleList[11]; keyRoutine.next},
				[524288, 18], {whichPanel = \pattern1; keyRoutine.next},
				[524288, 19], {whichPanel = \pattern2; keyRoutine.next},
				[524288, 20], {whichPanel = \pattern3; keyRoutine.next},
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
			switch(mod,
				131072, {
					("mapping "++oscPanel.nDef.key.asString++" "++string).postln;
					this.learn;
				},
				524288, {
					this.deMap;
				},
				655360, {this.resetSelectors;oscPanel.rebuild},

				262144, { // on ctrl-click, start a new automation list, store start time, and reset prevTime.
					if (recording == 0, {
						automationList = List.new;
						automationRoutine.stop;
						startTime = Main.elapsedTime;
						prevTime = 0;
						automationList.add([0, knob1.value]);
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

		selectors = 0!(numSelectors);
		numSelectors.do({|i|
			selectors[i] = Selector.new(composite, Rect(1*scale, (15*i+48)*scale, 47, 13));
			selectors[i].background_(~colourList.at(\none).blend(Color.grey, 0.3));
			selectors[i].action_({|thisSelector|
				modList[i] = thisSelector.item.asSymbol;
				modInputs = modList.sum({|item| Ndef(item.asSymbol)});
				oscPanel.rebuild;
				//item.background = (~colourList.at(item.item.asSymbol) ?? {~colourList.at(\none)}).blend(Color.grey, 0.3);
			});
		});
		this.doAction(default); //set the control parameter to the default value when initialized.
		//OSCfunc
		oscString = (oscPanel.nDef.key.asString +/+ string).asSymbol;
		oscFunc = OSCFunc.newMatching({|msg| this.doAction(msg[1]); {knob1.value_(msg[1])}.defer}, oscString);
		^this;

	}


	doAction {
		arg value;
		oscPanel.nDef.set(("knob"++param.asString).asSymbol, spec.map(value));
		if (recording == 1, {
			var delta = (Main.elapsedTime - startTime - prevTime);
			var when = Main.elapsedTime - startTime;
			automationList.add([delta, value]);
			prevTime = when;
		});

	}

	doActionPlusUpdate {
		arg value;
		this.doAction(value);
		{knob1.value = value}.defer;

	}

	startAutomation {
		automationRoutine.reset;
		automationRoutine.play;
	}

	setAction {
		arg func;
		knob1.action_(func);
	}

	mapToKnob {
		arg whichKnob;
		midiFunc.free;
		midiFunc = MIDIFunc.cc({|cv|
			this.doAction(cv/127);
			{knob1.value_(cv/127)}.defer;
		}, whichKnob);
		mapped = 1;
		{knob1.background_(knob1.background.blend(Color.white,0.3));}.defer;
	}

	deMap {
		midiFunc.free;
		midiFunc = nil;
		mapped = 0;
		{knob1.background_(Color.new255(120, 10, 80, 190))}.defer;
	}

	defaultSpec {
		^spec.map(default);
	}

	learn {
		midiFunc.free;
		midiFunc = MIDIFunc.cc({|cv|
			this.doAction(cv/127);
			{knob1.value_(cv/127)}.defer;
		});
		midiFunc.learn;
		{knob1.background_(knob1.background.blend(Color.white,0.3));}.defer;
	}

	focus {
		arg val;
		knob1.focus(val);

	}

	reset {
		this.doAction(default);
		{knob1.value = default}.defer;
		modList = \none!(numSelectors);
		modInputs = modList.sum({|item| Ndef(item.asSymbol)});
		{selectors.do({|item|
			item.value = 0;
			item.update;
			item.background = ~colourList.at(\none).blend(Color.grey, 0.4);
		})}.defer;

	}

	resetSelectors {
		modList = \none!(numSelectors);
		{selectors.do({|item|
			item.value = 0;
			item.background = ~colourList.at(\none).blend(Color.grey, 0.4);
		})}.defer;

	}

	save {
		saveList = Dictionary.new;
		saveList.putPairs([
			\Ndef, oscPanel.nDef.key,
			\string, string,
			\knob, knob1.value,
			\modList, modList,
			\msgNum, if(midiFunc.notNil, {midiFunc.msgNum}), //if this knob is mapped, save the ccNum it is mapped to.

		]);
		selectors.do({|item, index|
			var saveSymbol = ("selector" ++ (index+1)).asSymbol;
			var colourSymbol = (saveSymbol ++ "colour").asSymbol;
			saveList.put(saveSymbol, item.value); //annoying but for backward compatibility purposes i have to increment the index by 1, so selectors[0] loads the item at \selector1, etc
			saveList.put(colourSymbol, item.background);

		});
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		modList = loadList.at(\modList) ?? {[\none, \none, \none]};
		modInputs = modList.sum({|item| Ndef(item.asSymbol)});
		oscPanel.nDef.set(("knob"++param.asString).asSymbol, (spec.map(loadList.at(\knob) ?? {default}))); //set ndef param
		if (~midiLock == 0, { //don't load MIDI mapping if midilock is on
		if(midiFunc.notNil, {this.deMap}); //if there is already a midifunc, free it.\
		if (loadList.at(\msgNum).notNil, { //load the midi mapping from the loadlist
			this.mapToKnob(loadList.at(\msgNum));
		});
		});
		//update GUI
		{knob1.value = loadList.at(\knob) ?? {default};}.defer;
		selectors.do({|item, index|
			var selectorSymbol = ("selector" ++ (index+1)).asSymbol;
			var colourSymbol = (selectorSymbol ++ "colour").asSymbol;
			var loadValue = loadList.at(selectorSymbol) ?? {default};
			item.value_(loadValue); //can't set value directly because moduleList has to be updated, which will reset the value to 0. selectorValue stores the proper value independent of the gui object, which will be updated with inputSelector.update
			item.background_(loadList.at(colourSymbol));

				//~a.saves.at(\kord).at(\7)[\atk][\selector]
				//~a.moduleSockets[7].panel.labelKnob1.selectors[0].value
		});
	}
}

// A clone of LabelKnob that changes the sampler slider position
SamplePosLabelKnob : LabelKnob {
  var mySampleSlider;

  *new {
    arg parent, left, top, string, oscPanel, scale = 1, spec = ControlSpec(0,1), default = 0.5, numSelectors = 3, sampleSlider;
    ^super.newCopyArgs(parent, left, top, string, oscPanel, scale, spec, default, numSelectors).initSamplePosLabelKnob(sampleSlider);

  }

  initSamplePosLabelKnob {
    arg sampleSlider;
    this.initLabelKnob;
    mySampleSlider = sampleSlider;
    knob1.action = {|knob|
      this.doAction(knob.value);
      // {sampleSlider.moveBoth(knob.value)}.defer; @TODO: update gui to match pos
    };
  }




}

// Basically a clone of LabelKnob.
// @TODO make LFOKnob a subclass of LabelKnob
LFOKnob {
	var parent, left, top, string, <>oscPanel, <>scale, <>spec, <>default, <>knob1, <>onSwitch, <>typeSelector, <>inSelector, param, composite, knob1label, <>modList, <>isOn, <>oscType, <>midiFunc, mapped, keyRoutine;
	*new {
		arg parent, left, top, string, oscPanel, scale = 1, spec = [0,1].asSpec, default = 0.5;
		^super.newCopyArgs(parent, left, top, string, oscPanel, scale, spec, default).initLFOKnob;
	}

	initLFOKnob{
		param = string.asSymbol;
		isOn = 0;
		oscType = \sin;
		modList = [\none];
		composite = CompositeView.new(parent, Rect(left, top, 47*scale, 96*scale));
		composite.background_(Color.new255(85, 55, 155, 50));
		knob1label = StaticText.new(composite, Rect(2*scale, 2*scale, 40*scale, 15*scale));
		knob1label.align = \left;
		knob1label.font = (Font("Helvetica", 10*scale));
		knob1label.string = param.asString;
		knob1label.stringColor = Color.new255(240, 205, 205, 200);
		knob1 = Knob.new(composite, Rect(8*scale, 14*scale, 34*scale, 34*scale));
		knob1.keyDownAction_({|v,c,m,u,k|
			var keys = [m,k];
			switch(keys,
				[0,15], {this.reset;oscPanel.rebuild},
				[0,7], {typeSelector.valueAction = (typeSelector.value - 1) % 8},
				[0,8], {typeSelector.valueAction = (typeSelector.value + 1) % 8},
				[0,3], {onSwitch.valueAction = (1 - onSwitch.value)},
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
    // Midi map by clicking
    knob1.mouseDownAction_({|xx, xxx, xxxx, mod|
			switch(mod,
				131072, { // Shift click to learn
					("mapping "++oscPanel.nDef.key.asString++" "++string).postln;
					this.learn;
				},
				524288, { // Alt click to deMap
					this.deMap;
				}, // Command shift click to rebuild
				655360, {this.resetSelectors;oscPanel.rebuild},
			);
		});
		knob1.action = {|knob| oscPanel.nDef.set(("knob"++param.asString).asSymbol, spec.map(knob.value))};
		knob1.value = default;
		knob1.mode = \vert;
		knob1.step = 0.005;
		knob1.shift_scale = 1/10;
		knob1.color_([
			Color.new255(120, 10, 80, 190),
			Color.new255(25,10,25,205),
			Color.new255(230, 0, 40, 0),
			Color.new255(200, 150, 190, 245),
		]);
		onSwitch = Button.new(composite, Rect(1, 48, 45, 15));
		onSwitch.states_([
			["off", Color.white, Color.new255(120, 50, 70, 155)],
			["on", Color.white, Color.new255(210, 80, 100, 155)],
		]);
		onSwitch.font_(Font("Helvetica", 11, true));
		onSwitch.action_({|button|
			switch(button.value,
				0, {isOn = 0; oscPanel.rebuild;},
				1, {isOn = 1; oscPanel.rebuild;},
			)
		});
		typeSelector = PopUpMenu.new(composite, Rect(1, 63, 45, 15));
		typeSelector.items_([
			"sin", "saw", "tri", "sq", "noise0", "noise1", "noise2", "noiseA"
		]);
		typeSelector.font_(Font("Helvetica", 8)).background_(Color.new255(180, 180, 180, 130));
		typeSelector.action_({|selector|
			oscType = selector.item.asSymbol;
			oscPanel.rebuild;
		});
		inSelector = InputSelector.new(composite, 1, 78);
		inSelector.selector.background_(~colourList.at(\none).blend(Color.grey, 0.4));
		inSelector.selector.action_({|item|
			modList[0] = item.item.asSymbol;
			oscPanel.rebuild;
			item.background = (~colourList.at(item.item.asSymbol) ?? {~colourList.at(\none)}).blend(Color.grey, 0.5);
		});
		this.doAction(default);
	}

	doAction {
		arg value;
		oscPanel.nDef.set(("knob"++param.asString).asSymbol, spec.map(value));

	}

	focus {
		arg val;
		knob1.focus(val);
	}

	reset {


	}

  // Midi mapping (methods copied from LabelKnob)
  // Map to knob
  mapToKnob {
		arg whichKnob;
		midiFunc.free;
		midiFunc = MIDIFunc.cc({|cv|
			this.doAction(cv/127);
			{knob1.value_(cv/127)}.defer;
		}, whichKnob);
		mapped = 1;
		{knob1.background_(knob1.background.blend(Color.white,0.3));}.defer;
	}
  // Learn a midi mapping
  learn {
		midiFunc.free;
		midiFunc = MIDIFunc.cc({|cv|
			this.doAction(cv/127);
			{knob1.value_(cv/127)}.defer;
		});
		midiFunc.learn;
		{knob1.background_(knob1.background.blend(Color.white,0.3));}.defer;
	}
  // Demap a midi mapping
  deMap {
		midiFunc.free;
		midiFunc = nil;
		mapped = 0;
		{knob1.background_(Color.new255(120, 10, 80, 190))}.defer;
	}

	save {
		var saveList = Dictionary.new;
		saveList.putPairs([
			\Ndef, oscPanel.nDef.key,
			\string, string,
			\knob, knob1.value,
			\onSwitch, onSwitch.value,
			\oscType, oscType,
			\typeSelector, typeSelector.value,
			\inSelector, inSelector.value,
			\modList, modList,
      \msgNum, if(midiFunc.notNil, {midiFunc.msgNum}), //if this knob is mapped, save the ccNum it is mapped to.
		]);
		^saveList;

	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		oscPanel.nDef.set(("knob"++param.asString).asSymbol, (loadList.at(\knob) ?? {0.5}));
		modList = loadList.at(\modList) ?? {[\none]};
		{
			knob1.value = loadList.at(\knob) ?? {0.5};
			onSwitch.value_(loadList.at(\onSwitch) ?? {0})
		}.defer;
		switch(loadList.at(\onSwitch) ?? {0},
			0, {isOn = 0; oscPanel.rebuild;},
			1, {isOn = 1; oscPanel.rebuild;},
		);
		oscType = loadList.at(\oscType) ?? {\sin};
		{
			typeSelector.value_(loadList.at(\typeSelector) ?? {0});
			inSelector.value_(loadList.at(\inSelector) ?? {0});
			//inSelector.selector.background = (~colourList.at(inSelector.selector.item.asSymbol) ?? {Color.new255(200, 200, 200, 200)}).blend(Color.grey, 0.5)
		}.defer;

		if (~midiLock == 0, {//don't load MIDI mappings if midilock is on.
		if(midiFunc.notNil, {this.deMap}); //if there is already a midifunc, free it.
		if (loadList.at(\msgNum).notNil, { // Map midi messages to knob
			this.mapToKnob(loadList.at(\msgNum));
		});
		});
	}



}


InputSelector {
	var parent, left, top, scale, <modListNumber, <>selector, <function, <>selectorValue;
	*new {
		arg parent, left, top, scale = 1, modListNumber = 0;
		^super.newCopyArgs(parent,left,top,scale, modListNumber).initInputSelector(parent, left, top, scale, modListNumber);
	}

	initInputSelector {
		selector = PopUpMenu.new(parent, Rect(left, top, 45*scale, 15*scale));
		selectorValue = 0;
		selector.font = Font("Helvetica", 8.3*scale, true);
		//selector.canFocus_(true);
		selector.stringColor = Color.new255(220, 220, 250, 240);
		selector.background = Color.new255(200, 180, 180, 10);
		selector.allowsReselection = true;
		selector.toolTip_("Use this drop-down menu to select a modulation source for the knob or panel it's attached to. \n If it is on the top of a panel, this menu selects audio or trigger inputs for that panel.");
		Routine({
			~updateInputSelectors.wait;
			{selector.items = ~moduleList.collect({|item| item.asString})}.defer;
		}).play;
		~allInputSelectors.add(this);
	}

	action_ {
		arg func;
		function = func;
		selector.action = {func.value(selector);this.setColour};

	}

	doAction {
		function.value(selector);

	}

	setColour {
		selector.background = (~colourList.at(selector.item.asSymbol) ?? {~colourList.at(\none)}).blend(Color.grey, 0.5);
		//selector.background = Color.red;

	}

	value {
		^selector.value;
	}

	value_ {
		arg val;
		selector.value = val;
		selectorValue = val;
	}

	valueAction_{
		arg input;
		selector.valueAction = input;

	}

	update {
		selector.value = selectorValue ?? {0};
	}

	background_ {
		arg val;
		selector.background_(val);
	}
}

InputBank {
	var parent, bounds, panel, composite, selectors;
	*new { |parent, bounds, panel| ^super.newCopyArgs(parent, bounds, panel).initInputBank;}

	initInputBank {
		composite = CompositeView.new(parent, bounds);
		selectors = 0!4;
		4.do({|i|
			selectors[i] = Selector.new(composite, Rect(2 + (47*i), 0, 46, 15));
			selectors[i].action_({|selector| panel.inputList[i] = selector.item; panel.rebuild}).background_(~colourList.at(\none));
		//	~a.moduleSockets[9].panel.inputBank.update

		});
	}

	update {
		panel.inputList.do({|item, index|
			selectors[index].value = ~moduleList.indexOf(item);
			selectors[index].update;
			selectors[index].setColour;
		});
	}

	setRed {
		selectors.do({|item|
			item.background_(Color.red);
		});
	}
	save {
		var saveList = Dictionary.new;
		selectors.do({|selector, index|
			saveList.put(index.asSymbol, selector.value);
			saveList.put(("colour" ++ index).asSymbol, selector.background);
		});
		^saveList;
		//doesn't save inputList. that gets saved in the parent Panel.
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		selectors.do({|selector, index|
			selector.value_(loadList.at(index.asSymbol));
			selector.background_(loadList.at(("colour"++index).asSymbol));
		})
	}


}

FadeField {
	var parent, bounds, <>nDef, fontSize, <field;
	*new {
		arg parent, bounds, nDef, fontSize = 11;
		^super.newCopyArgs(parent, bounds, nDef, fontSize).initFadeField;
	}

	initFadeField {
		field = TextField.new(parent, bounds);
		field.action_({|item|
			if (item.value.asSymbol != \fade, {
			nDef.fadeTime_(item.value.asFloat)
			});
		});
		field.mouseDownAction_({|item| item.string = ""});
		field.background = (~colourList.at(nDef.key) ?? {Color.new(0.7, 0.5, 0.5, 0.8)}).blend(Color.grey, 0.5);
		field.string_("fade").stringColor_(Color.white);
		field.font_(Font("Helvetica", fontSize));

	}

	focus {
		arg val;
		field.focus(val);

	}


}


OutPanel {
	var <>parent, <>left, <>top, <>nDef, <bottom, <>composite, <>pan, panLabel, <>volume, <>meter, <>saveList, <>outChannel, <>outList, <outputSelector, <fadeField, <muteButton, <label, <label2, <label3, <focusList, <focus;
	*new {
		arg parent, left, top, nDef;
		^super.newCopyArgs(parent, left, top, nDef).initOutPanel;
	}

	initOutPanel {
		outChannel = 0;
		focus = 0;
		outList = List.new;
		composite = CompositeView.new(parent, Rect(left, top, 98, 120));
		bottom = top + composite.bounds.height;
		composite.background = ~colourList.at(nDef.key) ?? {Color.new255(50, 50, 50, 50)};
		composite.canFocus_(true);
		composite.keyDownAction_({|v,c,m,u,k|
			var keys = [m, k];
			switch(keys,
				[0,18], {this.focusOn(0)},
				[0,19], {this.focusOn(1)},
				[0,20], {this.focusOn(2)},
			);
			true;
		});
		label = StaticText.new(composite, Rect(0,0,0,0)).background_(Color(0,0,0,0));
		label2 = StaticText.new(composite, Rect(0,0,0,0)).background_(Color(0,0,0,0));
		label3 = StaticText.new(composite, Rect(0, 0, 98, 120)).background_(Color(1,1,1,0));
		label3.font_(Font("Arial", 110, true)).stringColor_(Color(1,1,1,0.5)).align_(\center);
		label3.string_(nDef.key.asString.last);
		pan = LabelKnob.new(composite, 2, 5, "pan", this, numSelectors:2);
		volume = LabelKnob.new(composite, 49, 5, "volume", this, numSelectors:2);
		muteButton = Button.new(composite, Rect(2, 84, 94, 15));
		muteButton.states_([
			["Mute", Color.white, Color.new255(100, 20, 20, 180)],
			["Mute", Color.white, Color.new255(220, 10, 20, 220)],
		]);
		muteButton.action_({|button|
			nDef.set(\mute, (1-button.value));
		});
		outputSelector = PopUpMenu.new(composite, Rect(2, 100, 50, 15));
		outputSelector.background_(composite.background.blend(Color.grey, 0.8)).font_(Font("Helvetica", 10));
		outputSelector.items_(["0/1", "2/3", "none"]);
		outputSelector.action_({|selector|
			if (selector.item.asSymbol!=\none, {
				outChannel = (selector.value) * 2;
			}, {
				outChannel = Server.local.options.numOutputBusChannels;
			}
			);
			this.rebuild;
		});
		fadeField = FadeField.new(composite, Rect(54, 100, 42, 15), nDef, 11);
		focusList = [pan, volume, muteButton];
		this.rebuild;

	}

	focusOn {
		arg which;
		focus = which;
		focusList[which].focus(true);

	}

	save {
		saveList = Dictionary.new;
		saveList.putPairs([
			\pan, pan.save,
			\volume, volume.save,
			\outputSelector, outputSelector.value,
			\outChannel, outChannel,
			\mute, muteButton.value,
		]);
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		pan.load(loadList.at(\pan) ?? {nil});
		if (loadList.at(\volume).isFloat, { //backward compatibility code
			volume.load(Dictionary.newFrom([\volume, loadList.at(\volume)]));
		}, {
			volume.load(loadList.at(\volume) ?? {nil});
		});
		outChannel = loadList.at(\outChannel) ?? {0};
		nDef.set(\mute, (1 - (loadList.at(\mute) ?? {0})));
		{
			outputSelector.value = loadList.at(\outputSelector)??{0};
			muteButton.value = loadList.at(\mute) ?? {0};
		}.defer;
		this.rebuild;
	}

	rebuild {
		nDef.play(out:outChannel);
		Ndef(nDef.key.asSymbol, {
			arg knobpan = 0.5, knobvolume = 0.8, mute = 1;
			var sig = Silent.ar, panIn = 0, volumeIn = 0;
			outList.do({|item|
				sig = sig + (Ndef(item.asSymbol)*0.75);
			});
			pan.modList.do({|item|
				panIn = panIn + Ndef(item);
			});
			panIn = (knobpan.linlin(0,1,-1,1) + panIn).min(1).max(-1);
			volume.modList.do({|item|
				volumeIn = volumeIn + Ndef(item);
			});
			volumeIn = LinLin.ar(volumeIn, -1, 1, 0, 1);
			volumeIn = (knobvolume.lag(0.08) * 2 * volumeIn).max(0).min(1);
			sig = HPF.ar(sig, 5, mul: volumeIn * mute.lag(0.03));
			sig = Pan2.ar(sig, panIn).flatten;
		});


	}

}

ClockPanel {
	var parent, <bounds, <>nDef, <anasGui, <composite, <blinker, bpmField, <clock, <bpm, <syncButton;

	*new {
		arg parent, bounds, nDef, anasGui;
		^super.newCopyArgs(parent, bounds, nDef, anasGui).initClockPanel;
	}

	initClockPanel {
		bpm = 120;
		clock = TempoClock.new(bpm/60, 4);
		composite = CompositeView.new(parent, bounds);
		composite.background_(Color.new255(150,150,220,185));
		blinker = CompositeView.new(composite, Rect(2,2,20,20)).background_(Color.new255(100,100,100,125));
		bpmField = TextField.new(composite, Rect(23, 1, 50, 22));
		bpmField.background_(Color.new255(190, 100, 50, 140));
		bpmField.stringColor_(Color.white).string_("BPM").align_(\center);
		bpmField.mouseDownAction_({bpmField.string = ""});
		bpmField.action_({|field| bpm = field.value.asFloat; clock.tempo = bpm/60; nDef.set(\bpm, bpm)});
		clock.sched(0, {
			{
				{blinker.background_(Color.new255(210, 30, 30, 190))}.defer;
				(12/bpm).wait;
				{blinker.background_(Color.new255(100,100,100,125))}.defer;
			}.fork;
			1;
		});
		syncButton = Button.new(composite, Rect(72, 1, 27, 22));
		syncButton.states_([["S", Color.white, Color.new255(100, 100, 150, 140)]]);
		syncButton.action_({this.reSyncAll});

		this.rebuild;

	}

	reSyncAll {
		[\osc1, \osc2, \osc3, \osc4, \osc5].do({|item|
			//if(anasGui.perform(item).syncToClock == 1, {anasGui.perform(item).sync});
		});
		anasGui.patterns.do({|item|
			item.sync;
		});
		anasGui.moduleSockets.do({|item|
			if (item.module == DrumPanel, {item.panel.sync})
		});
	}
	rebuild {
		Ndef(nDef.key, {
			arg bpm = 120;
      // Send OSC message /anas/bang 1 on clock hit
      SendReply.kr(Impulse.kr(bpm/60),'/anas/bang', 1);
			SinOsc.kr(0, add: bpm/60);
		});

	}
}

Selector {
	var <parent, <bounds, <composite, <items, <value, <item, <>action, menu, menuBounds, previousColor, <>menuAttached;

	*new {|parent, bounds| ^super.newCopyArgs(parent,bounds).initSelector}

	initSelector {
		var widthLimit = Window.screenBounds.width - 418;
		menuAttached = 0;
		value = 0;
		menuBounds = [
			(bounds.left+parent.bounds.left+parent.parent.bounds.left+parent.parent.parent.bounds.left + 46)
			.min(widthLimit),
			(bounds.top+parent.bounds.top+parent.parent.bounds.top+parent.parent.parent.bounds.top),
		];
		composite = StaticText.new(parent, bounds).align_(\center).stringColor_(Color.new255(240,225,240, 230));
		composite.string_("none").font_(Font("Helvetica", 11, false));
		items = List.new;
		composite.background_(Color.new255(180, 100, 100, 160)).acceptsMouse_(true);
		composite.mouseDownAction_({|thisView|
			var menu = SelectorMenu.instances[0];
		    if (menuAttached == 0, {
				if (menu.selector.notNil, {menu.detach});
				previousColor = thisView.background;
				thisView.background_(Color.new255(235, 170, 40, 245));
				menu.visible_(true)
				.selector_(this)
				.moveTo(menuBounds[0], menuBounds[1]);
				menuAttached = 1;
			}, { menu.close;
				menuAttached = 0;
			});
		});
		~allInputSelectors.add(this);
	}

	value_ {
		arg val;
		value = val;
		item = items[value];
	}

	items_ {
		arg val;
		items = val;
		item = items[value];
		this.update;
	}

	background {
		^composite.background;
	}

	background_ {
		arg val;
		composite.background_(val);
	}

	stringColor {
		^composite.stringColor;
	}

	stringColor_ {
		arg val;
		composite.stringColor_(val);
	}

	absoluteBounds {
		^composite.absoluteBounds;
	}

	doAction {
		action.value(this);
	}

	update {
		composite.string_(item);
	}

	setColour {
		composite.background = (~colourList.at(item.asSymbol) ?? {~colourList.at(\none)});
	}

	setPreviousColor {
		composite.background_(previousColor);
	}
}


SelectorMenu {
	classvar <instances;
	var parent, <>selector, <window;

	*new {|parent| instances = List.new; ^super.newCopyArgs(parent).initSelectorMenu}

	initSelectorMenu {
		var bounds;
		this.class.instances.add(this);
		bounds= Rect.new(0,0,238,128);
		window = FlowView.new(parent, bounds, 3@3, 3@3).visible_(false);
		window.mouseDownAction_({|view|
			window.visible_(false);
			selector.setPreviousColor;
			"hi".postln;
		});
		window.background_(Color.new255(235, 170, 40, 245));
		this.updateItems;

	}

	updateItems {
		var selectorMenu = this;
		window.children.do({|child| child.close});
		~moduleList.do({|item, index|
		StaticText.new(window, 44@22)
			.string_(item.asString)
			.background_(~colourList.at(item.asSymbol))
			.stringColor_(Color.white)
			.align_(\center)
			.font_(Font("Helvetica", 11, true))
			.mouseDownAction_({|thisview|
				selector.value_(index);
				selector.doAction;
				selector.update;
				selector.background_(thisview.background);
				selector.menuAttached = 0;
				window.visible_(false);
				true;
			});
		});
		window.reflowAll;

	}

	setSelector {
		arg sel;
		selector = sel;
	}

	close {
		window.visible_(false);
		selector.setPreviousColor;
	}

	detach {
		if (selector.menuAttached == 1, {selector.setPreviousColor});
	}

	visible_ {
		arg val;
		window.visible_(val);
	}

	moveTo {
		arg whereX, whereY;
		window.moveTo(whereX, whereY);
	}


}