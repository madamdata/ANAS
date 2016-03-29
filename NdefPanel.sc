NdefPanel : ANASPanel {
	var outputButtons, <>outList;


	*new {
		arg parent, bounds, nDef, outs;
		^super.newCopyArgs(parent, bounds, nDef, outs).initNdefPanel;

	}

	initNdefPanel {
		this.initANASPanel;
		"hi!".postln;


	}

	rebuild {


	}


}