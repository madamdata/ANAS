ModuleSocket {
	var parent, <bounds, <anasGui, <composite, <>panel, panelField, <module;

	*new {
		arg parent, bounds, anasGui;
		^super.newCopyArgs(parent, bounds, anasGui).initModuleSocket;
	}

	initModuleSocket {
		composite = CompositeView.new(parent, bounds);
		composite.background_(Color.new(1,1,1,0));
		panelField = TextField.new(composite, Rect(120, 2, 70, 17)).background_(Color.new(0.5,0.5,0.5,0.5));
		panelField.font_(Font("Helvetica", 11, true)).stringColor_(Color.new(1,1,1,1));
		panelField.action_({|thisField|
			var whatKind, nDefName;
			#whatKind, nDefName =  thisField.value.tr($ , $/).split;
		    (case
				{whatKind == "adsr"} {whatKind = "ADSRPanel"}
				{whatKind == "osc"} {whatKind = "OscPanel"}
				{whatKind == "samp"} {whatKind = "SamplerPanel"}
				{whatKind == "mult"} {whatKind = "MultiPlexPanel"}
				{whatKind == "filt"} {whatKind = "FilterPanel"}
				{whatKind == "del"} {whatKind = "DelayPanel"}
				{whatKind == "drum"} {whatKind = "DrumPanel"}
				{whatKind == "comp"} {whatKind = "CompPanel"}
			);
			panel.composite.close;
			this.loadPanelWithUpdate(whatKind.interpret, nDefName.asSymbol)
		});
		panelField.front;


	}

	loadPanel {
		arg whatKind, nDefName;
		module = whatKind; //store the module type in variable 'module'
		Ndef(nDefName).mold(1, \audio);
		if (panel.notNil) {panel.composite.close; panel.nDef.free};
			panel = whatKind.new(composite, Rect(0, 0, composite.bounds.width, composite.bounds.height), Ndef(nDefName), ~outPuts);
		panelField.front;
	}

	loadPanelWithUpdate { //same as above, but updates all the inputselectors. Use this for selecting panels manually, use the above for loading entire layouts in presets.
		arg whatKind, nDefName;
		this.loadPanel(whatKind, nDefName);
		anasGui.updateModuleList;
	}

	save {
		var saveList = panel.save;
		saveList.putPairs([\moduleType, module.asString, \moduleName, panel.nDef.key]); //add an entry to the saveList with module type and name
		^saveList;
	}

	load {arg loadList;
		var loadModule = loadList.at(\moduleType);
		var loadName = loadList.at(\moduleName);
		if (loadModule.notNil) {
			if (module.asString != loadModule) {
				this.loadPanel(loadModule.interpret, loadName);
			}


		};
		panel.load(loadList);
	}

	focus {arg bool; //passes the 'focus' method to panel (will have to modify for multiple panels)
		panel.composite.focus(bool);
	}

	rebuild {
		panel.rebuild;
	}
	nDef {^panel.nDef}
	nDefNames {^panel.nDefNames}


}