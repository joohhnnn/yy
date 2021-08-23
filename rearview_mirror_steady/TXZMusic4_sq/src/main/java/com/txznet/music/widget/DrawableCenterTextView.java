package com.txznet.music.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

public class DrawableCenterTextView extends AppCompatTextView {

	public DrawableCenterTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public DrawableCenterTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public DrawableCenterTextView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onDraw(Canvas canvas) {
		Drawable[] compoundDrawables = getCompoundDrawables();
		float bodyWidth = 0;
		float measureText = getPaint().measureText(getText().toString());
		int compoundDrawablePadding = getCompoundDrawablePadding();
		for (int i = 0; i < compoundDrawables.length;) {
			if (null != compoundDrawables[i]) {
				int intrinsicWidth = compoundDrawables[i].getIntrinsicWidth();
				bodyWidth = intrinsicWidth + compoundDrawablePadding;
			}
			i += 2;
		}
		bodyWidth += measureText;

		canvas.translate((getWidth() - bodyWidth) / 2, 0);
		super.onDraw(canvas);
	}
}
