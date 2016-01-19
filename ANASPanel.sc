ANASPanel {
	var parent, <bounds, <nDef, <composite, subunits, <label1, <label2, <focusList, <focus, <lock, <outs, <thingsToSave;

	*new {
		arg parent, bounds, nDef;
		^super.newCopyArgs(parent, bounds, nDef).initANASPanel;
	}

	initANASPanel {
		composite = CompositeView.new(parent, bounds);
		composite.background = ~colourList.at(nDef.key);
		//label1 = StaticText.new(composite, Rect(10, 10, 50, 10));
		label2 = StaticText.new(composite, Rect(10, 10, 50, 10));
		"hi".postln;
		^this;
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