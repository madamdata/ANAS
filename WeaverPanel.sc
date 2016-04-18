WeaverPanel {
	var parent, bounds, anasPanel, composite, label;

	*new {
		arg parent, bounds, anasPanel;
		^super.newCopyArgs(parent, bounds,anasPanel).initWeaverPanel
	}

	initWeaverPanel {
		composite = CompositeView.new(parent, bounds);
		label = StaticText.new(parent, Rect(0, 0, 150, 20))
		.string_(anasPanel.nDef.key.asString)
		.stringColor_(Color.white)
		.font_(Font("Helvetica", 15, true));



	}

}