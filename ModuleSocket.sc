ModuleSocket {
	var parent, <bounds, <anasGui, <composite, <weaverComposite, <>panel, <weaverPanel, panelField, <module, <moduleStrings, <weaverModule;

	*new {
		arg parent, bounds, anasGui;
		^super.newCopyArgs(parent, bounds, anasGui).initModuleSocket;
	}

	initModuleSocket {
		composite = CompositeView.new(parent, bounds);
		composite.background_(Color.new(1,1,1,0));
		weaverComposite = CompositeView.new(anasGui.weaverComposite, bounds);
		weaverComposite.background_(Color.new(1,1,1,0));
		panelField = TextField.new(composite, Rect(120, 2, 70, 17)).background_(Color.new(0.5,0.5,0.5,0.5));
		panelField.font_(Font("Helvetica", 11, true)).stringColor_(Color.new(1,1,1,1));
		panelField.action_({|thisField|
			var whatKind, nDefName, whatKind2 = nil, nDefName2 = nil;
			#whatKind, nDefName, whatKind2, nDefName2 =  thisField.value.tr($ , $/).split;
			whatKind = this.convertShortcut(whatKind);
			whatKind2 = this.convertShortcut(whatKind2);
			panel.do({|thispanel| thispanel.composite.close});
			if (whatKind2.isNil, {this.loadPanelWithUpdate(whatKind.interpret, [nDefName.asSymbol])}, {
				this.loadPanelWithUpdate([whatKind.interpret, whatKind2.interpret], [nDefName.asSymbol, nDefName2.asSymbol]);
			});
		});
		panelField.front;

	}

	loadPanel {
		arg whatKind, nDefName;
		module = [whatKind].flatten; //store the module type in variable 'module'
		moduleStrings = module.collect({|item| item.asString}).asString; //store an array of strings naming each module
		nDefName.do({|name| Ndef(name).mold(1, \audio)});
		//a string is an array! convert to symbol first.

		if (panel.notNil) {
			panel.do({|thispanel|
				thispanel.composite.close;
				thispanel.nDef.free;
			});
		};
		if (whatKind.isArray, {panel = 0!(whatKind.size)}, {panel = [0]});
		whatKind.do({|thispanel, index|
			panel[index] = thispanel.new(this, Rect(0, 150*index, composite.bounds.width, composite.bounds.height), Ndef(nDefName[index]), ~outPuts);
		});
		panelField.front;
	}


	loadPanelWithUpdate { //same as above, but updates all the inputselectors. Use this for selecting panels manually, use the above for loading entire layouts in presets.
		arg whatKind, nDefName;
		this.loadPanel(whatKind, nDefName);
		anasGui.updateModuleList;
	}

	loadWeaverPanel {
		arg whatKind;
		weaverModule = whatKind;
		weaverPanel = whatKind.new(weaverComposite, Rect(0, 0, composite.bounds.width, composite.bounds.height), panel);


	}

	save {
		var saveList = Dictionary.new;
		panel.do({|thispanel| saveList.put(thispanel.nDef.key, thispanel.save)});
		saveList.putPairs([
			\moduleTypes, moduleStrings,
			\moduleNames, this.panelNames,
			\numPanels, panel.size
		]); //add an entry to the saveList with module type and name
		^saveList;
	}

	load {arg loadList;
		var loadModule = loadList.at(\moduleTypes);
		var loadName = loadList.at(\moduleNames).postln;
			if (moduleStrings != loadModule) {
				this.loadPanel(loadModule.interpret, loadName.flatten);
		};
		panel.do({|thispanel|
				thispanel.load(loadList.at(thispanel.nDef.key));
			});
	}

	focus {arg bool; //passes the 'focus' method to panel (will have to modify for multiple panels)
		panel[0].composite.focus(bool);
	}

	convertShortcut {
		arg string;
		var output = string;
		(case
			{string == "adsr"} {output = "ADSRPanel"}
			{string == "osc"} {output = "OscPanel"}
			{string == "samp"} {output = "SamplerPanel"}
			{string == "mult"} {output = "MultiPlexPanel"}
			{string == "filt"} {output = "FilterPanel"}
			{string == "del"} {output = "DelayPanel"}
			{string == "drum"} {output = "DrumPanel"}
			{string == "comp"} {output = "CompPanel"}
			{string == "ctrl"} {output = "ControlPanel"}
		);
		^output;
	}

	rebuild {
		panel.do({|item| item.rebuild});
	}
	nDef {^panel.nDef}
	nDefNames { //used for setting the input selector menu - returns all ndefs contained within all the panels
		^panel.collect({|item| item.nDefNames});
	}

	panelNames { //used for loading panels - returns only the name of the panel
		^panel.collect({|item| item.panelName});
	}

}