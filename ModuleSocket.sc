ModuleSocket {
	var parent, <bounds, <composite, <>panel, panelSelector;

	*new {
		arg parent, bounds;
		^super.newCopyArgs(parent, bounds).initModuleSocket;
	}

	initModuleSocket {
		composite = CompositeView.new(parent, bounds);
		panelSelector = PopUpMenu.new(composite, Rect(140, 2, 50, 20));
		panelSelector.items_(["Osc", "ADSR"]);
		panelSelector.action_({|thisSelector| if (thisSelector.value == 1) {
			composite.close;
		}});

	}

	save {panel.save}
	load {arg loadList; panel.load(loadList)}
	rebuild {panel ?? {panel.rebuild}}


}