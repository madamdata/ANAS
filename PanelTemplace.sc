PanelTemplate {

	var parent, left, top, <>nDef, outs, composite, label, label2, <>labelKnob1, <>labelKnob2, <>labelKnob3, <>labelKnob4, <>labelKnob5, <>labelKnob6, <>inputList, selectors;

	*new {
		arg parent, left, top, nDef, outs;
		^super.newCopyArgs(parent, left, top, nDef, outs).initADSRPanel;
	}



}