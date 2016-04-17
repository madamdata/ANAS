Sampler2Panel : ANASPanel {

	*new {
		arg parent, bounds, nDef;
		^super.newCopyArgs(parent,bounds,nDef).initSampler2Panel;
	}

	initSampler2Panel {
		this.initANASPanel;

	}



}