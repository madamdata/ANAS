PatternPanel {
		var parent, left, top, <nDef, <anasGui, <>syncSource, <>valPat, <>durPat, <legatoPat, <lagPat,<composite, patternField, patternField2,patternField3,patternField4, typeSelector, <syncSelector, <>syncOn, <>currentDur, <>prevDur, <condition, <type, <adsr, adsrSelector, <inputSelector, <inputList, <receivingInput, <inputResponder;

	*new {
		arg parent, left, top, nDef, anasGui;
		^super.newCopyArgs(parent, left, top, nDef, anasGui).initPatternPanel;
	}
	initPatternPanel {
		type = \freq;
		adsr = ~a.adsr1.nDef;
		inputList = [\none];
		receivingInput = 0;
		inputResponder = InputResponder.new(Ndef((nDef.key.asString++"input").asSymbol), this);
		inputResponder.rebuild;
		composite = CompositeView.new(parent, Rect(left, top, 258, 79));
		composite = composite.background = ~colourList.at(nDef.key);
		patternField = TextField.new(composite, Rect(2,2,165, 18));
		patternField.font = Font("Helvetica", 11);
		patternField.background = Color.new255(105, 50, 100, 125);
		patternField.stringColor_(Color.new255(255,255,255,255)).string_("value");
		patternField.action_({|field|
			Pdefn((nDef.key.asString ++"val").asSymbol, field.value.interpret);
		});
		patternField2 = TextField.new(composite, Rect(2, 21, 165, 18));
		patternField2.background = Color.new255(105, 80, 100, 125);
		patternField2.stringColor_(Color.new255(255,255,255,255)).string_("duration");
		patternField2.font = Font("Helvetica", 11);
		patternField2.action_({|field|
			Pdefn((nDef.key.asString ++ "dur").asSymbol, field.value.interpret);
		});
		patternField3 = TextField.new(composite, Rect(2, 40, 165, 18));
		patternField3.background = Color.new255(105, 80, 100, 125);	patternField3.stringColor_(Color.new255(255,255,255,255)).string_("legato").font_(Font("Helvetica", 11));
		patternField3.action_({|field|
			Pdefn((nDef.key.asString ++ "legato").asSymbol, field.value.interpret);
		});
		patternField4 = TextField.new(composite, Rect(2, 59, 165, 18));
		patternField4.background = Color.new255(105, 80, 100, 125);	patternField4.stringColor_(Color.new255(255,255,255,255)).string_("lag").font_(Font("Helvetica", 11));
		patternField4.action_({|field|
			Pdefn((nDef.key.asString ++ "lag").asSymbol, field.value.interpret);
		});
		[patternField, patternField2, patternField3, patternField4].do({|item|
			item.mouseDownAction_({item.string = "";item.mouseDownAction_({})});
		});
		typeSelector = PopUpMenu.new(composite, Rect(167, 2, 50, 18)).background_(~colourList.at(\none));
		typeSelector.items_(["freq", "note", "ADSR", "amp", "trig"]).stringColor_(Color.white);
		typeSelector.action_({|selector|
			type = selector.item.asSymbol;
			this.rebuild;
		});
		syncSelector = PopUpMenu.new(composite, Rect(167, 21, 50, 18)).background_(~colourList.at(\none));
		syncSelector.items_(["none", "P1", "P2", "P3", "Input"]).stringColor_(Color.white);
		syncSelector.action_({|selector|
			if (selector.value==0, {syncOn = 0}, {
				syncSource = switch(selector.value,
					1, {anasGui.patterns[0]},
					2, {anasGui.patterns[1]},
					3, {anasGui.patterns[2]},
					4, {this.inputResponder},
				);
				syncOn = 1;
			});
			Tdef(nDef.key).stop.play(anasGui.clock.clock);
			durPat.reset;
		}).allowsReselection_(true);
		adsrSelector = PopUpMenu.new(composite, Rect(167, 40, 50, 18));
		adsrSelector.items_(["adsr1", "adsr2"]);
		adsrSelector.action_({|selector| adsr = Ndef(selector.item.asSymbol)});
		[typeSelector, syncSelector, adsrSelector].do({|item|
			item.font_(Font("Helvetica", 11));
		});
		inputSelector = InputSelector.new(composite, 167, 59);
		inputSelector.action_({|selector|
			inputList[0] = selector.item.asSymbol;
			inputResponder.rebuild;
		});
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
			\valPat, if (patternField.value.asSymbol != \value, {patternField.value}, {"Pn(0)"}),
			\durPat,  if (patternField2.value.asSymbol != \duration, {patternField2.value}, {"Pn(0.3)"}),
			\legatoPat, if (patternField3.value.asSymbol != \legato, {patternField3.value}, {"Pn(0.3)"}),
			\lagPat, if (patternField4.value.asSymbol != \lag, {patternField4.value}, {"Pn(0)"}),
			\syncSelector, syncSelector.value,
			\syncOn, syncOn,
			\typeSelector, typeSelector.value,
			\type, type,
			\adsrSelector, adsrSelector.value,
			\adsr, adsr.asCompileString,
			\inputList, inputList,
			\inputSelector, inputSelector.value,
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
		inputList = loadList.at(\inputList) ?? {[\none]};
		{
		inputSelector.value = loadList.at(\inputSelector) ?? {0};
		inputSelector.selector.background = (~colourList.at(inputSelector.selector.item.asSymbol) ?? {Color.new255(200, 200, 200, 200)}).blend(Color.grey, 0.5);
		}.defer;
		{
			patternField.value_(loadList.at(\valPat));
			patternField2.value_(loadList.at(\durPat));
			patternField3.value_(loadList.at(\legatoPat));
			patternField4.value_(loadList.at(\lagPat));
			syncSelector.value_(loadList.at(\syncSelector));
			typeSelector.value_(loadList.at(\typeSelector));
		}.defer;
		[patternField, patternField2, patternField3, patternField4].do({|item|
			item.mouseDownAction_({});
		});
		Pdefn((nDef.key.asString ++ "dur").asSymbol, (loadList.at(\durPat)).interpret);
		Pdefn((nDef.key.asString ++ "legato").asSymbol, (loadList.at(\legatoPat)).interpret);
		Pdefn((nDef.key.asString ++ "lag").asSymbol, (loadList.at(\lagPat)).interpret);
		Pdefn((nDef.key.asString ++ "val").asSymbol, (loadList.at(\valPat)).interpret);
		this.rebuild;

	}


}

InputResponder {
	var <nDef, patternPanel, <condition, <oscDef;
	*new {
		arg nDef, patternPanel;
		^super.newCopyArgs(nDef, patternPanel).initInputResponder
	}

	initInputResponder {
		condition = Condition.new(false);
		OSCFunc.newMatching({|msg|
			condition.test = true;
			condition.signal;
			condition.test = false;
		}, ("/" ++ nDef.key.asString).asSymbol);

	}

	rebuild {
		Ndef(nDef.key, {
			var input, msgName;
			input = Ndef(patternPanel.inputList[0]);
			input = A2K.kr(input);
			msgName = ("/" ++ nDef.key.asString).asSymbol;
			SendReply.kr(input, msgName, 1);
		});

	}

}