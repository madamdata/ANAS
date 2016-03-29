ANASPanel {
	var parent, <bounds, <nDef, <outs, <composite, subunits, <label1, <label2, <focusList, <focus, <lock,  <thingsToSave, keyRoutine, standardAction, setInputAction, <whichPanel, <inputList;

	*new {
		arg parent, bounds, nDef;
		^super.newCopyArgs(parent, bounds, nDef).initANASPanel;
	}

	initANASPanel {
		composite = CompositeView.new(parent, bounds);
		composite.background = ~colourList.at(nDef.key) ?? {Color.grey};
		composite.canFocus_(true);
		inputList = \none!4;
		//label1 = StaticText.new(composite, Rect(10, 10, 50, 10));
		keyRoutine = Routine{
			4.do({|i|
				if (whichPanel != \same, {
					inputList[i] = whichPanel;
				});
				i.yield
			})
		};
		^this;
	}

	focusOn {
		arg which;
		focus = which;
		focusList[which].focus(true);

	}

	save {
		var saveList = Dictionary.new;
		thingsToSave.keysValuesDo({|key, value|
			saveList.put(key, value.save);
		});
		^saveList.postln;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.new};

	}


}