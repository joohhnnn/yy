package com.txznet.record.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;

public class HelpIconButton extends ImageButton {

	public HelpIconButton(Context context) {
		super(context);
	}

	public HelpIconButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HelpIconButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(event.getAction()==MotionEvent.ACTION_UP){
			performClick();
			return true;
		}
		return super.onTouchEvent(event);
	}	
	
	@Override
	public boolean performClick(){
		
		return super.performClick();
	}

}
