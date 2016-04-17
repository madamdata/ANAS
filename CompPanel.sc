CompPanel : ANASPanel {
	var labelKnobs, <sideChainButton, <>sideChainOn;

	*new {|parent, bounds, nDef, outs|
		^super.newCopyArgs(parent,bounds,nDef, outs).initCompPanel;
	}

	initCompPanel {
		this.initANASPanel;
		sideChainOn = 1;

		// label
		label2 = StaticText.new(composite, Rect(0, 0, 190, 20));
		label2.string = nDef.key.asString;
		label2.font = Font("Helvetica", 22, true);
		label2.stringColor = Color.new(1,1,1,0.4);
		label2.align = \left;
		label2.background = Color(0,0,0,0);

		//knobs
		labelKnobs = 0!11;
		labelKnobs = 0!6;
		labelKnobs[0] = LabelKnob.new(composite, 2, 31, "Thresh", this, 1, [0,1].asSpec, 0.4, 1);
		labelKnobs[1] = LabelKnob.new(composite, 49, 31, "Ratio", this, 1, [1,0].asSpec, 0.65, 1);
		labelKnobs[2] = LabelKnob.new(composite, 96, 31, "Atk", this, 1, [0.001, 0.15,\exp].asSpec, 0.05, 1);
		labelKnobs[3] = LabelKnob.new(composite, 143, 31, "Rel", this, 1, [0.003,0.3, \exp].asSpec, 0.2, 1);
		labelKnobs[4] = LabelKnob.new(composite, 2, 88, "Gain", this, 1, [1.2, 3.5].asSpec, 0.2, 1);
		labelKnobs[5] = LabelKnob.new(composite, 49, 88, "Filt", this, 1, [30, 5000, \exp].asSpec, 0.35, 1);

		//inputs
		inputBank = InputBank.new(composite, Rect(0, 19, 192, 30), this);

		//sidechain button
		sideChainButton = SideChainButton.new(composite, Rect(96, 90, 47, 87), "side", this, [
			Color.new255(150, 30, 30, 180),
			Color.new255(250, 140, 30, 250)
		]);

		//outputs
		outputButtons = Array.newClear(outs.size);
		outs.do({|whichOut, index|
			outputButtons[index] = OutputButton.new(composite, 142 + ((50/outs.size)*index), 135, (50/outs.size), nDef, whichOut);
		});

	}

	rebuild {
		Ndef(nDef.key, {
			arg knobThresh = 0.5, knobRatio = 0.5, knobAtk = 0.003, knobRel = 0.05, knobGain = 1.2, knobFilt = 50;
			var inputs, sig, sideChainInputs;
			inputs = inputList.sum({|item| Ndef(item.asSymbol)});
			if (sideChainOn == 0, {
				sideChainInputs = inputs;
			}, {
				sideChainInputs = BPF.ar(sideChainButton.inputs, knobFilt, 0.2, 2);
			});
			sig = Compander.ar(inputs, sideChainInputs, knobThresh, 1, knobRatio, knobAtk, knobRel, knobGain);
			sig;
		});
	}

	save {
		var saveList = Dictionary.new;
		labelKnobs.do({|item| saveList.put(item.string.asSymbol, item.save)});
		saveList.putPairs([
			\sideChainOn, sideChainOn,
			\sideChainButton, sideChainButton.save,
			\outputButtons, outputButtons.collect({|item| item.value}),
			\inputList, inputList,
			\inputBank, inputBank.save,
		]);
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		inputList = loadList.at(\inputList) ?? [\none, \none, \none, \none];
		inputBank.load(loadList.at(\inputBank));
		sideChainOn = loadList.at(\sideChainOn);
		labelKnobs.do({|item| item.load(loadList.at(item.string.asSymbol))});
		sideChainButton.load(loadList.at(\sideChainButton));
		outputButtons.do({|item, index|
			var isOn = (loadList.at(\outputButtons).asArray[index]) ?? {0};
			{item.value_(isOn)}.defer;
			if (isOn == 1, {item.isOn = 1; item.doAction});
		});
		this.rebuild;

	}


}

SideChainButton : TrigButton {

	*new {
		arg parent, bounds, string, oscPanel, colours;
		^super.newCopyArgs(parent, bounds, string, oscPanel, colours).initSideChainButton;
	}

	initSideChainButton {
		this.initTrigButton;
		button.value_(1);
	}

	doAction {
		arg buttonValue;
		oscPanel.sideChainOn = buttonValue;
		oscPanel.rebuild;
	}

	save {
		var saveList;
		saveList = Dictionary.new;
		saveList.putPairs([
			\inputList, inputList,
			\selectors, selectors.collect({|item| item.value}),
			\colours, selectors.collect({|item| item.selector.background}),
			\button, button.value
		]);
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};
		inputList = loadList.at(\inputList) ?? [\none, \none];
		inputs = inputList.sum({|item| Ndef(item.asSymbol)});
		if(loadList.at(\selectors).notNil, {{
			selectors.do({|item, index|
				item.value_(loadList.at(\selectors)[index]);
				if (loadList.at(\colours).notNil, {item.selector.background_(loadList.at(\colours)[index])});
			});
			button.value_(loadList.at(\button));
		}.defer;});
	}


}