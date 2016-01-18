PatternPanel {
		var parent, left, top, nDef, <anasGui, <>syncSource, <>valPat, <>durPat, composite, patternField, patternField2, typeSelector, <syncSelector, <>syncOn, <>currentDur, <>prevDur, <condition, <type;

	*new {
		arg parent, left, top, nDef, anasGui;
		^super.newCopyArgs(parent, left, top, nDef, anasGui).initPatternPanel;
	}

	initPatternPanel {
		type = \freq;
		composite = CompositeView.new(parent, Rect(left, top, 192, 54));
		composite = composite.background = ~colourList.at(nDef.key);
		patternField = TextField.new(composite, Rect(2,2,150, 20));
		patternField.font = Font("Helvetica", 11);
		patternField.background = Color.new255(105, 50, 100, 125);
		patternField.stringColor = Color.new255(255,255,255,255);
		patternField.action_({|field|
			Pdefn((nDef.key.asString ++"val").asSymbol, field.value.interpret).postln;
		});
		patternField2 = TextField.new(composite, Rect(2, 23, 150, 20));
		patternField2.background = Color.new255(105, 80, 100, 125);
		patternField2.stringColor = Color.new255(255,255,255,255);
		patternField2.font = Font("Helvetica", 11);
		patternField2.action_({|field|
			Pdefn((nDef.key.asString ++ "dur").asSymbol, field.value.interpret);
		});
		typeSelector = PopUpMenu.new(composite, Rect(152, 2, 38, 20)).background_(~colourList.at(\none));
			typeSelector.items_(["freq", "ADSR", "amp", "trig"]).stringColor_(Color.white);
		typeSelector.action_({|selector|
			type = selector.item.asSymbol;
			this.rebuild;
		});
		syncSelector = PopUpMenu.new(composite, Rect(152, 23, 38, 20)).background_(~colourList.at(\none));
		syncSelector.items_(["1", "2", "3", "4"]).stringColor_(Color.white);
		syncSelector.action_({|selector|
			syncSource = anasGui.patterns[selector.value];
			syncOn = 1;
		}).allowsReselection_(true);
		syncOn = 0;
		currentDur = 1;
		durPat = 1;
		valPat = 0;
		/*Pdef(nDef.key,
			Pbind(
				\freq, \rest,
				\dur, Pdefn((nDef.key.asString ++ "dur").asSymbol),
				\val, Pdefn((nDef.key.asString ++ "val").asSymbol).midiratio.explin(0.1, 2.5, -1, 1, \min),
				\send, Pfunc({|event|
					nDef.set(\input, event.at(\val));
				})
			)
		).postln.play;*/
		valPat = Pdefn((nDef.key.asString ++ "val").asSymbol, Pn(0)).asStream;
		durPat = Pdefn((nDef.key.asString ++ "dur").asSymbol, Pn(0.3)).asStream;
		condition = Condition.new(false);
		Tdef(nDef.key, {
			loop{
				prevDur = currentDur;
				currentDur = durPat.next;
				switch(type,
					\freq, {nDef.set(\input, (valPat.next - 12).midiratio.explin(0.1, 2.5, -1, 1, \min))},
					\ADSR, {nDef.set(\input, 1);
						{(valPat.next*currentDur).wait;nDef.set(\input, 0)}.fork;
					}
				);
				condition.test = true;
				condition.signal;
				condition.test = false;
				if (syncOn == 0, {currentDur.wait}, {syncSource.condition.wait});
			}
		}).play(anasGui.clock.clock);
		this.rebuild;
	}

	rebuild {
	Ndef(nDef.key, {
			arg input, lag = 0;
			var sig;
			sig = SinOsc.ar(0.0, 0, add: input);
			sig;
		});
		Tdef(nDef.key, {
			loop{
				prevDur = currentDur;
				currentDur = durPat.next;
				switch(type,
					\freq, {nDef.set(\input, (valPat.next - 12).midiratio.explin(0.1, 2.5, -1, 1, \min))},
					\ADSR, {nDef.set(\input, 1);
						{(valPat.next*currentDur).wait;nDef.set(\input, 0)}.fork(anasGui.clock.clock);
					}
				);
				condition.test = true;
				condition.signal;
				condition.test = false;
				if (syncOn == 0, {currentDur.wait}, {syncSource.condition.wait});
			}
			}).play(anasGui.clock.clock);

	}

	sync {
		anasGui.clock.clock.schedAbs(anasGui.clock.clock.nextTimeOnGrid, {
			Tdef(nDef.key).reset;
			valPat.reset;
			durPat.reset;
		});

	}

}