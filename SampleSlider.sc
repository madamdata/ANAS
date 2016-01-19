SampleSlider {
	var parent, <>bounds, bgColor, shadeColor, nDef, <>signalArray, leftHandle, rightHandle, background, shade, select, ldisplace, rdisplace, waveform, <lVal, <rVal, <>handleSize=24;

	*new {
		arg parent, bounds, bgColor, shadeColor, nDef, signal;
		^super.newCopyArgs(parent,bounds, bgColor, shadeColor, nDef, signal).initSampleSlider(parent,bounds,bgColor,shadeColor, nDef, signal);
	}

	initSampleSlider {
		arg parent, bounds, bgColor, shadeColor, nDef, signal;
		signalArray = signal ?? {[0]};
		background = View.new(parent, bounds);
		waveform = UserView.new(background, Rect(0, 0, bounds.width, bounds.height-handleSize));
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
    leftHandle = View.new(background, Rect(0, bounds.height-handleSize, handleSize/2, handleSize));
    rightHandle = View.new(background, Rect(bounds.width-(handleSize/2), bounds.height-handleSize, handleSize/2, handleSize));
    shade = View.new(background, Rect(0, 0, rightHandle.bounds.right - leftHandle.bounds.left, bounds.height-handleSize));
    leftHandle.background_(Color.red(alpha:0));
    rightHandle.background_(Color.red(alpha:0));
    leftHandle.backgroundImage_(Image.new((AnasGui.filenameSymbol.asString.dirname) +/+ "img/slider-left.png"));
    rightHandle.backgroundImage_(Image.new((AnasGui.filenameSymbol.asString.dirname) +/+"img/slider-right.png"));
		shade.background_(shadeColor ?? {Color.grey(alpha:0.4)});
		background.background_(bgColor ?? {Color.red(alpha:0.3)});
    // shade.acceptsMouse = false;
		select = \none;
		ldisplace = 0;
		rdisplace = 0;
    leftHandle.mouseDownAction_({|view, x, y|
      select = \left;
    });
    rightHandle.mouseDownAction_({|view, x, y|
      select = \right;
    });
    shade.mouseDownAction_({|view, x, y|
      select = \mid;
    });
    background.mouseDownAction_({|view, x, y|
      if (y<(bounds.height-handleSize),
        {
          select = \mid;
        },{
          if(x<leftHandle.bounds.left,
            {
              select=\left;
              this.moveLeft(x);
          });
          if(x>rightHandle.bounds.right,
            {
              select=\right;
              this.moveRight(x);
          });
        }
      );

      ldisplace = x - leftHandle.bounds.left;
      rdisplace = rightHandle.bounds.left - x;
    });
    background.mouseMoveAction_({|view, x, y|
      switch(select,
        \left, {
          this.moveLeft(x-(handleSize/2));
        },
        \right, {
          this.moveRight(x-(handleSize/2));
        },
        \mid, {
   //       leftHandle.moveTo((x - ldisplace).max(0).min(bounds.width-(handleSize/2)), bounds.height-handleSize);
   //       rightHandle.moveTo((x + rdisplace).max(bounds.width-(handleSize/2)).min(), bounds.height-handleSize);
          this.moveLeft(x-ldisplace);
          this.moveRight(x+rdisplace);
          shade.moveTo((x - ldisplace).max(0).min(bounds.width-shade.bounds.width), 0);
          this.updatePos;
        },
      );
    });
    background.mouseUpAction_({select = \none});

	}

  moveLeft{
    arg x;
    // Don't allow moving past right handle or edge
    if (( (x < (rightHandle.bounds.left+(handleSize/2))) && (x > 0)), {
      leftHandle.moveTo(x, bounds.height-handleSize);
      shade.resizeTo(rightHandle.bounds.right-leftHandle.bounds.left, bounds.height-handleSize);
      shade.moveTo(x,0);
    });
    this.updatePos;
  }

  moveRight{
    arg x;
    // Don't allow moving past left handle or edge
    if (( (x > (leftHandle.bounds.left-(handleSize/2))) && (x < (bounds.right-(handleSize/2)))), {
      rightHandle.moveTo(x, bounds.height-handleSize);
      shade.resizeTo(rightHandle.bounds.right-leftHandle.bounds.left, bounds.height-handleSize);
    });
    this.updatePos;
  }

  updatePos {
    lVal = (leftHandle.bounds.left)/bounds.width;
    rVal = (rightHandle.bounds.right)/bounds.width;
    nDef.set(\startPos, lVal);
    nDef.set(\endPos, rVal);
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