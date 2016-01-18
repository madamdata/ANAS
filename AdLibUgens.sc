Drift {
	*kr {|depth = 0.03, rate = 0.2|
		^LFNoise1.kr(rate).range(1-depth, 1+depth);
	}
		*ar {|depth = 0.03, rate = 0.2|
		^LFNoise1.ar(rate).range(1-depth, 1+depth);
	}
}

LFNoiseA {
	*kr {
	arg low = 50, high = 3000, rate1 = 1, rate2 = 1;
	var sig;
		sig = LFNoise1.kr(rate1).exprange(0.8, 50);
		sig = LFNoise0.kr(sig * rate2).range(low, high);
		^sig;
	}
		*ar {
	arg low = 50, high = 3000, rate1 = 1, rate2 = 1;
	var sig;
		sig = LFNoise1.ar(rate1).exprange(0.8, 50);
		sig = LFNoise0.ar(sig * rate2).range(low, high);
		^sig;
	}
}

DSaw {
	*ar {
	arg freq = [60].midicps, tone = 4, preFilter = 7000, drift = 0.006, maxFilt = 12000, minFilt = 180, mul = 1;
	var sig = Saw.ar(freq * Drift.ar(drift, 1.1), mul:1);
		sig = MoogFF.ar(sig, preFilter * Drift.ar(0.05), 2);
		sig = MoogFF.ar(sig, (tone * freq).min(maxFilt).max(minFilt) * Drift.ar(0.05), 1.5);
		sig = sig * mul;
		^sig;
	}
}

DPulse {
	*ar {
		arg freq = [60].midicps, tone = 7, preFilter = 9000, drift = 0.008, maxFilt = 8000, minFilt = 1500, mul = 1, width = 0.5;
		var sig = (Pulse.ar(freq * Drift.kr(drift, 1.1), width * Drift.kr(0.1, 0.3), mul:1) * 1.5).distort;
		sig = MoogFF.ar(sig, preFilter * Drift.kr(0.05), 2);
		sig = MoogFF.ar(sig, (tone * freq).min(maxFilt).max(minFilt) * Drift.kr(0.05), 1.5);
		sig = sig * mul;
		^sig;
	}

}

FNoise {
	*ar {
		arg filtFreq = 1000, gain = 2, mul = 1;
		var sig = WhiteNoise.ar(mul);
		sig = MoogFF.ar(sig, filtFreq, gain);
		^sig;
	}
}

HBleep {
	*ar {
		var sig;
		sig = Impulse.ar(LocalIn.ar(1).range(1000, 7000)) + SinOsc.ar(LocalIn.ar(1).range(100,10000)) + Impulse.ar(1);
		sig = sig + (LocalIn.ar(1) * 1);
		sig = RLPF.ar(sig, LFNoise0.kr(LFPulse.kr(2).exprange(3, 15)).range(150, 15000), LFNoise1.kr(3.8).range(0.05, 0.85));
		LocalOut.ar(sig);
		sig = Pan2.ar(sig.tanh);
	}
}

FMTest {
	*ar {
		arg freq;
	var sig;
		sig = SinOsc.ar(freq, mul:0.3);
		^sig;
	}
}

CrossFader {

	*ar { arg inputs, bipolar,width=2.0;
		var whiches;
		inputs = inputs.dereference;
		whiches = PanAz.ar(inputs.size,SinOsc.ar(0.0,add:1.0),bipolar, width: width);

		^Mix.new(
			inputs.collect({ arg sound,i;
				sound * whiches.at(i)
			})
		)
	}
	*kr { arg inputs, bipolar,width=2.0;
		var whiches;
		inputs = inputs.dereference;
		whiches = PanAz.ar(inputs.size,SinOsc.ar(0.0,add:1.0),bipolar,width: width);

		^Mix.new(
			inputs.collect({ arg sound,i;
				sound * whiches.at(i)
			})
		)
	}
}


