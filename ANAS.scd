//ANAS modular synthesizer
//run this code for version number:
AnasGui.version
// coded by thumbthumb aka Adam Adhiyatma (they, them)
// December 2015
//www.adamadhiyatma.com
//http://soundcloud.com/dj-thumbthumb

/*
There is now a helpfile!
select this ---> AnasGui
and press Cmd + D to take a look at it.

Update list:

0.95 Clock sync added.
0.948 sampler now has a triggerable reset button and triggerable record and overdub.
0.947 keyboard shortcuts improved and extended; GUI looks a little prettier.
0.94 Sampler Panel will save now, almost full functionality. Output panels now have settable fadeTime and mute buttons.Colour scheme tweaked.
KEYBOARD CONTROL!!! helpfile soon to come.
0.93 OscPanel gui updates, reset button, lock button that prevents loading, version number now accessible via AnasGui.version
LabelKnobs now have a variable number of selectors, in case you need to build a module that doesn't need 3 mod parameters. Hopefully that doesn't bork all your save files, but I think it should work.
oscPanel now lives in its own .sc file, rather than inside anasguiParts.sc.

0.92 configuration button, now depends on ANASconfig file in the extensions folder.
0.91 convenience launcher /recompile panel
0.9 Incomplete sampler panel and folder structure
0.85 Delay panel LFO implemented.
0.845 Folder system implemented. **You may have to put all your presets into a subfolder inside your existing save folder!**
0.84 MIDI control learning and saving. Basic MIDI keyboard input (unfinished). Shift + Click on a GUI knob, then turn the midi controller knob to map it. Alt + click to demap.

0.83 Transposition is now possible. Added a lo-hi filter and dependency on DFM1 Ugen. Helpfiles in progress.
0.82 Modulatable ADSR
0.81 Fixed preset loading bug


INCOMPLETES
Setting modulation for the "Tone" parameter on the oscillators currently does nothing. (might not happen)

UPCOMING FEATURES
help files and a PDF user guide
Gate module
The ability to patch modules into the delay loop
the ability to invert inputs (multiply by -1)
pattern control for oscillators and ADSR

RUNNING ANAS
as of v0.91, ANAS is now launched from the launcher in the bottom right corner in the screen. You must configure it with your save and recording folders before starting! Click on "configure" and follow the instructions.

Of course you can still launch ANAS via code:
*/
s.waitForBoot({
a = AnasGui.new;
});

/*

but be aware that ANAS uses a config file called ANASconfig that lives in your extensions folder in order to locate saves and recordings. Be sure to use the configuration GUI tool at least once before you try to launch ANAS with code.

*/


//saving and recording:
// type the filename into the text box to the right of the save or record buttons and PRESS ENTER before you save or record!
Pbind(
	\dur, 0.5,
	\val, Pdefn(\pattern1val),
	\send, Pfunc({|event|
		Ndef(\pattern1).set(\input, event.at(\val))
	}),
).play;

//this Tdef will automatically scroll through all of your presets.
(
Tdef(\loader, {
	i = 0;
	loop {
		var preset = ~a.menuEntries.choose;
			[~a.osc1, ~a.osc2, ~a.osc3, ~a.osc4, ~a.osc5].do({|item|
				var lock = [0,1].choose;
				item.lock = lock;
				{item.lockButton.value = lock}.defer;
			});
			{~a.load(preset)}.defer;
		rrand(0.3, 1).wait;

	}
}).play;
);
Tdef(\loader).stop;
//this Tdef can be used to play melodies by changing the transposition of an oscillator.
(
Tdef(\trtest, {
	var pat = Pseq([0, 12, 24, 36], inf).asStream;
	loop {
		Ndef(\osc1).set(\transpose, pat.next);
		0.15.wait;
	}

}).play;
)

(
Pdef(\script2,
	Pbind(
		\type, \anasNote,
		\osc, [\osc1],
		\adsr, \adsr1,
		\transpose, Pseq([0,3,7,10,14,17], inf),
		\dur, Prand([Pseq(0.07!59), Pseq(0.11!40)], inf),
		\legato, 0.1,
		\preFilter, 0.5,
	)
).play;
)

~a.moduleObjects.asCompileString
