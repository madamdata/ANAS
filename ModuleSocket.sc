ModuleSocket {
	var parent, <bounds, <composite, <>panel, panelSelector;

	*new {
		arg parent, bounds;
		^super.newCopyArgs(parent, bounds).initModuleSocket;
	}

	initModuleSocket {
		composite = CompositeView.new(parent, bounds);
		composite.background_(Color.new(1,1,1,0));
/*		panelSelector = PopUpMenu.new(composite, Rect(140, 2, 50, 20));
		panelSelector.items_(["Osc", "ADSR"]);
		panelSelector.action_({|thisSelector| if (thisSelector.value == 1) {
			composite.close;
		}});*/

	}

	loadPanel {
		arg whatKind, nDefName;
		panel = whatKind.new(composite, Rect(0, 0, composite.bounds.width, composite.bounds.height), Ndef(nDefName), ~outPuts);

	}
	save {panel.save}

	load {arg loadList; panel.load(loadList)}

	focus {arg bool;
		panel.composite.focus(bool);
	}

	rebuild {
		panel.rebuild;
	}
	nDef {^panel.nDef}


}