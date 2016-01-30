PatternPanel {
		var parent, left, top, <nDef, <anasGui, <>syncSource, <>valPat, <>durPat, <legatoPat, <lagPat,<composite, patternField, patternField2,patternField3,patternField4, typeSelector, <syncSelector, <>syncOn, <>currentDur, <>prevDur, <condition, <type, <adsr, adsrSelector;

	*new {
		arg parent, left, top, nDef, anasGui;
		^super.newCopyArgs(parent, left, top, nDef, anasGui).initPatternPanel;
	}

	initPatternPanel {
		type = \freq;
		adsr = ~a.adsr1.nDef;
		composite = CompositeView.new(parent, Rect(left, top, 192, 79));
		composite = composite.background = ~colourList.at(nDef.key);
		patternField = TextField.new(composite, Rect(2,2,150, 18));
		patternField.font = Font("Helvetica", 11);
		patternField.background = Color.new255(105, 50, 100, 125);
		patternField.stringColor_(Color.new255(255,255,255,255)).string_("value");
		patternField.action_({|field|
			Pdefn((nDef.key.asString ++"val").asSymbol, field.value.interpret).postln;
		});
		patternField2 = TextField.new(composite, Rect(2, 21, 150, 18));
		patternField2.background = Color.new255(105, 80, 100, 125);
		patternField2.stringColor_(Color.new255(255,255,255,255)).string_("duration");
		patternField2.font = Font("Helvetica", 11);
		patternField2.action_({|field|
			Pdefn((nDef.key.asString ++ "dur").asSymbol, field.value.interpret);
		});
		patternField3 = TextField.new(composite, Rect(2, 40, 150, 18));
		patternField3.background = Color.new255(105, 80, 100, 125);	patternField3.stringColor_(Color.new255(255,255,255,255)).string_("legato").font_(Font("Helvetica", 11));
		patternField3.action_({|field|
			Pdefn((nDef.key.asString ++ "legato").asSymbol, field.value.interpret);
		});
		patternField4 = TextField.new(composite, Rect(2, 59, 150, 18));
		patternField4.background = Color.new255(105, 80, 100, 125);	patternField4.stringColor_(Color.new255(255,255,255,255)).string_("lag").font_(Font("Helvetica", 11));
		patternField4.action_({|field|
			Pdefn((nDef.key.asString ++ "lag").asSymbol, field.value.interpret);
		});
		[patternField, patternField2, patternField3, patternField4].do({|item|
			item.mouseDownAction_({item.string = "";item.mouseDownAction_({})});
		});
		typeSelector = PopUpMenu.new(composite, Rect(152, 2, 38, 18)).background_(~colourList.at(\none));
		typeSelector.items_(["freq", "note", "ADSR", "amp", "trig"]).stringColor_(Color.white);
		typeSelector.action_({|selector|
			type = selector.item.asSymbol;
			this.rebuild;
		});
		syncSelector = PopUpMenu.new(composite, Rect(152, 21, 38, 18)).background_(~colourList.at(\none));
		syncSelector.items_(["none", "1", "2", "3", "4"]).stringColor_(Color.white);
		syncSelector.action_({|selector|
			if (selector.value==0, {syncOn = 0}, {
				syncSource = anasGui.patterns[selector.value];
				syncOn = 1;
			});
		}).allowsReselection_(true);
		adsrSelector = PopUpMenu.new(composite, Rect(152, 40, 38, 18));
		adsrSelector.items_(["adsr1", "adsr2"]);
		adsrSelector.action_({|selector| adsr = Ndef(selector.item.asSymbol)});
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
		legatoPat = Pdefn((nDef.key.asString ++ "legato").asSymbol, Pn(0.1)).asStream;
		lagPat = Pdefn((nDef.key.asString ++ "lag").asSymbol, Pn(0)).asStream;
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
			sig = Lag.ar(sig, lag);
			sig;
		});
		Tdef(nDef.key, {
			loop{
				prevDur = currentDur;
				currentDur = durPat.next;
				switch(type,
					\note, {
						nDef.set(\lag, lagPat.next);
						nDef.set(\input, ((valPat.next??{12}) - 12).midiratio.explin(0.1, 2.5, -1, 1, \min));
						adsr.set(\hold, (legatoPat.next * currentDur));
						adsr.set(\t_pgate, 1);
					},
					\freq, {
						nDef.set(\lag, lagPat.next);
						nDef.set(\input, (valPat.next - 12).midiratio.explin(0.1, 2.5, -1, 1, \min));
					},
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
			legatoPat.reset;
			lagPat.reset;
		});

	}

	save {
		var saveList = Dictionary.new;
		saveList.putPairs([
			\valPat, patternField.value,
			\durPat, patternField2.value,
			\legatoPat, patternField3.value,
			\lagPat, patternField4.value,
			\syncSelector, syncSelector.value,
			\syncOn, syncOn,
			\typeSelector, typeSelector.value,
			\type, type,
			\adsrSelector, adsrSelector.value,
			\adsr, adsr.asCompileString,
		]);
		^saveList;
	}

	load {
		arg loadList;
		loadList = loadList ?? {Dictionary.newFrom([\valPat, "Pn(0)", \durPat, "Pn(0.3)", \legatoPat, "Pn(0.3)", \lagPat, "Pn(0)", \adsr, "Ndef('adsr1')"])};

		//valPat = Pdefn((nDef.key.asString ++ "val").asSymbol).asStream;

		type = loadList.at(\type) ?? {\freq};
		adsr = loadList.at(\adsr).interpret;
		syncOn = loadList.at(\syncOn) ?? {0};
		{
			patternField.value_(loadList.at(\valPat));
			patternField2.value_(loadList.at(\durPat));
			patternField3.value_(loadList.at(\legatoPat));
			patternField4.value_(loadList.at(\lagPat));
			syncSelector.value_(loadList.at(\syncSelector));
			typeSelector.value_(loadList.at(\typeSelector));
		}.defer;
		Pdefn((nDef.key.asString ++ "dur").asSymbol, (loadList.at(\durPat)).interpret);
		Pdefn((nDef.key.asString ++ "legato").asSymbol, (loadList.at(\legatoPat)).interpret);
		Pdefn((nDef.key.asString ++ "lag").asSymbol, (loadList.at(\lagPat)).interpret);
		Pdefn((nDef.key.asString ++ "val").asSymbol, (loadList.at(\valPat)).interpret);
		this.rebuild;

	}


}