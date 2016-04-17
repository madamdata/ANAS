ANASPanel {
	var parent, <bounds, <nDef, <outs, <outputButtons, <composite, subunits, <label1, <label2, <focusList, <focus, <lock,  <thingsToSave, keyRoutine, standardAction, setInputAction, <whichPanel, <inputList, <inputBank;

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
				setInputAction = {|v,c,m,u,k|
			var keys = [m,k];
			switch(keys,
				[0, 49], {
					this.rebuild;
					keyRoutine.reset;
					composite.keyDownAction_(standardAction);
					{inputBank.update}.defer;
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
			);
			true;
		};
		//key control -------
		standardAction = {|v,c,m,u,k|
			var keys = [m, k];
			switch(keys,
				[0, 49], {
					this.rebuild;
					keyRoutine.reset;
					{inputBank.update}.defer;
				},
				[0,18], {this.focusOn(0)},
				[0,19], {this.focusOn(1)},
				[0,20], {this.focusOn(2)},
				[0,21], {this.focusOn(3)},
				[131072,18], {this.focusOn(4)},
				[131072,19], {this.focusOn(5)},
				[131072,20], {this.focusOn(6)},
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
		^this;
	}

	focusOn {
		arg which;
		focus = which;
		focusList[which].focus(true);

	}


	nDefNames {
		^nDef.key.asSymbol;
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

	close {
		composite.close;
		nDef.free;

	}


}