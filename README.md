# ANAS

ANAS is a digital modular synthesizer for the SuperCollider open source audio environment. 
code by thumbthumb (adam adhiyatma) and agargara (david cummings) 
with help from the community at Mills College / Center for Contemporary Music and the East Bay.


### Installation Instructions
- install SuperCollider 3.7 (any version of 3.7 works, but 3.6 will not work due to the major JITlib updates in 3.7)
- download and install SC3Plugins (required) https://sourceforge.net/projects/sc3-plugins/files/
- download as zip (click the "download zip" button in the top right) and extract to Extensions folder (you can find this by going to file --> open user support directory. If there is no extensions folder there, make one.) 
- recompile. A launcher should appear in the bottom right of the screen. 
- make two separate new folders anywhere for audio recordings and preset files. 
- click 'configure' and follow the instructions on screen.
- click 'start ANAS'. 

### Getting help
- Most panels, knobs and menus have tooltips. Hover the cursor over an item to see what it does. To see a general description of knob functions, hover over the panel's name text. 
- Leave an issue or email me at forks.andhope@gmail.com
- Take a look at the help file for AnasGui. It's incomplete, but there's some information in there! You can find it by searching "AnasGui" in the help browser. 
- Check out the Wiki on this page for sound samples of music made with ANAS! 

### Features
- Modular - style signal routing between different sound generation objects. 
- Usable without any knowledge of SuperCollider code. 
- non-hideous GUI. 
- No difference between control and audio signals - everything is scaled so that you can plug anything into anything. 
- Feedback possible and encouraged. 
- Integration with the SuperCollider language and patterns / events library. (in progress)
- Recordable knob automation. (ctrl-click and hold ctrl)
- easy midi mapping. (shift-click and move controller)
- Five type-switching audio / low frequency oscillators with modulateable parameters and built-in filters, including
  simple analog simulation UGens. 
- Polyphony within each oscillator. 
- Includes fadeTime features from JITLib. Fade settings in and out! 
- Support for saving and loading presets. 
- Keyboard shortcuts for fast, one-handed patching. 
- Integrated recording button for easy sample generation. 
- Moog-style (almost) fixed filter bank, with modulateable filter frequencies and Hi / Lo pass. 
- 2 ADSR units with modulateable parameters. 
- A super duper delay unit
- Multiplexer / crossfader with built-in LFO control
- Sampler panel with support for recording samples, playing back pre-loaded samples, and dynamically recording, overdubbing and 
  glitching via control signal. 
- Four outputs with modulateable pan, which can double as routing busses. 
- Two inputs with support for direct audio and amplitude detection. 
- Three pattern panels with support for controlling oscillator frequencies, triggering envelopes, or modulating any parameter
  using the Patterns library. These accept signals as trigger inputs to generate the next pattern value. 
- 93 Knobs!
- MIDI keyboard control routing for oscillators. 


### Upcoming

- Replaceable panels
- Multi-filters, generic effects like chorus, phasing, gating
- Drum generator
- MIDI / OSC sync with DAWs.
- Pitch detection. 


------
