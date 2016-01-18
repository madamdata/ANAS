MIDIPanel {
	var parent, left, top, composite, <>keybList, <>offset, <>keybButtons;
	*new {
		arg parent, left, top;
		^super.newCopyArgs(parent, left, top).initMIDIPanel(parent, left, top);
	}

	initMIDIPanel {
		arg parent, left, top;
		MIDIClient.init;
		MIDIIn.connectAll;
		offset = 0;
		keybButtons = 0!5;
		keybList = List.new;
		composite = CompositeView.new(parent, Rect(left, top, 192, 55));
		composite.background = ~colourList.at(\midi);
		this.initDefs;
		5.do({|i|
			keybButtons[i] = Button.new(composite, Rect(2 + (i*22), 10 , 20, 20));
			keybButtons[i].states = [
				["", Color.black, Color.grey],
				[("osc"++(i+1)), Color.black, Color.white],
			];
			keybButtons[i].font = Font("Helvetica", 9);
			keybButtons[i].action_({|button|
				if (button.value==1, {keybList.add(("osc"++(i+1)).asSymbol)},
					{keybList.remove(("osc"++(i+1)).asSymbol)})
			});
		});
	}

	initDefs {
		~test = Pseq([\osc1, \osc2, \osc3, \osc4, \osc5], inf).asStream;
		MIDIdef.noteOn(\tr, {|vel, note|
			~thisNote = note;
			keybList.do({|item|
				if (item!=\none, {Ndef(item).set(\transpose, note+offset-60)});
			});
			//Ndef(~test.next).set(\transpose, note+offset-60);
			Ndef(\noteBus).set(\val, 1);
		});
		MIDIdef.noteOff(\trOff, {|vel, note|
			if (note == ~thisNote, {Ndef(\noteBus).set(\val, 0)});
		});
		MIDIdef.bend(\bend, {|cv, abc|
			Ndef(\del1).set(\knobdelayTime, {cv/16256});
		});

		Ndef(\noteBus, {
			arg val;
			SinOsc.ar(0.0, 0, add: val);
		});
		5.do({|i|
			Ndef(("cc"++(i*2+60)).asSymbol, {arg val; SinOsc.ar(0.0, 0, add:val)});
		});


	}
}