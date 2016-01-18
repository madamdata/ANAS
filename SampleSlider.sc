SampleSlider {
	var parent, <>bounds, bgColor, shadeColor, nDef, <>signalArray, leftHandle, rightHandle, background, shade, select, ldisplace, rdisplace, waveform, <lVal, <rVal;

	*new {
		arg parent, bounds, bgColor, shadeColor, nDef, signal;
		^super.newCopyArgs(parent,bounds, bgColor, shadeColor, nDef, signal).initSampleSlider(parent,bounds,bgColor,shadeColor, nDef, signal);
	}

	initSampleSlider {
		arg parent, bounds, bgColor, shadeColor, nDef, signal;
		signalArray = signal ?? {[0]};
		background = View.new(parent, bounds);
		waveform = UserView.new(background, Rect(0, 0, bounds.width, bounds.height));
		waveform.background = Color.new255(180, 120, 120, 80);
		waveform.drawFunc_({|view|
			var scale = signalArray.size / view.bounds.width;
			Pen.width = 1;
			Pen.smoothing_(false);
			Pen.strokeColor_(Color.new255(255, 255, 255, 180));
			Pen.moveTo(0@(view.bounds.height/2));
			Pen.lineTo(view.bounds.width@(view.bounds.height/2));
			Pen.stroke;
			Pen.moveTo(0@(view.bounds.height/2));
			view.bounds.width.do({|i|
				var index = (i * scale).trunc(1);
				Pen.lineTo((i@(signalArray[index]*1.5*view.bounds.height+(view.bounds.height/2))));
			});
			Pen.stroke;
		});
		waveform.acceptsMouse = false;
		waveform.refresh;
		leftHandle = View.new(background, Rect(bounds.width * 0.25, 0, 2, bounds.height));
		rightHandle = View.new(background, Rect(bounds.width * 0.75, 0, 2, bounds.height));
		shade = View.new(background, Rect(leftHandle.bounds.right, 0, rightHandle.bounds.left - leftHandle.bounds.right, bounds.height));
		leftHandle.background_(Color.black);
		rightHandle.background_(Color.black);
		shade.background_(shadeColor ?? {Color.grey(alpha:0.4)});
		background.background_(bgColor ?? {Color.red(alpha:0.3)});
		shade.acceptsMouse = false;
		select = \none;
		ldisplace = 0;
		rdisplace = 0;
		background.mouseDownAction_({|view, x, y|
			if ((((leftHandle.bounds.left-x)<30) && (leftHandle.bounds.right>=x)), {select = \left});
			if ((((x-rightHandle.bounds.right)<30) && (rightHandle.bounds.left<=x)), {select = \right});
			if (((x > leftHandle.bounds.right)&&(x<rightHandle.bounds.left)), {select = \mid});
			ldisplace = x - leftHandle.bounds.left;
			rdisplace = rightHandle.bounds.left - x;
		});
		background.mouseUpAction_({select = \none});
		background.mouseMoveAction_({|view, x, y, mod|
			switch(select,
				\left, {
					if (( x < rightHandle.bounds.left), {
						leftHandle.moveTo(x, 0);
						shade.resizeTo(rightHandle.bounds.left-leftHandle.bounds.right, 150);
						shade.moveTo(x,0);
					});
				},

				\right , {
					if (( x > leftHandle.bounds.left), {
						rightHandle.moveTo(x,0);
						shade.resizeTo(rightHandle.bounds.left-leftHandle.bounds.right, 150);
					});
				},

				\mid , {
					leftHandle.moveTo((x - ldisplace).max(0).min(bounds.width-shade.bounds.width), 0);
					rightHandle.moveTo((x + rdisplace).max(shade.bounds.width).min(bounds.width-2), 0);
					shade.moveTo((x + 2 - ldisplace).max(0).min(bounds.width-shade.bounds.width), 0);
				},
			);
			lVal = (leftHandle.bounds.left)/bounds.width;
			rVal = (rightHandle.bounds.right)/bounds.width;
			nDef.set(\startPos, lVal);
			nDef.set(\endPos, rVal);
		});

	}

	refresh {
		waveform.refresh;
	}

	load {
		arg array;
		signalArray = array;
		this.refresh;
	}


}