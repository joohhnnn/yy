package com.txznet.widget;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class DebugButton extends Button {
	public static int COLOR1 = 0xFF5698BB;
	public static int COLOR2 = 0xFF569874;
	public static int COLOR3 = 0xaa5698CD;
	public static int COLOR4 = 0x66CD9856;

	private static int[] COLORS = new int[] { COLOR1, COLOR2, COLOR3, COLOR4 };
	private static int COLOR_INDEX = 0;

	public static void nextColor() {
		COLOR_INDEX = (COLOR_INDEX + 1) % COLORS.length;
	}

	public DebugButton(Context context, String buttonName, int buttonColor,
			OnClickListener listener) {
		super(context);

		this.setText(buttonName);
		this.setBackgroundColor(buttonColor);
		this.setOnClickListener(listener);
		this.setTextSize(18.0f);
	}

	public DebugButton(Context context, String buttonName,
			OnClickListener listener) {
		super(context);

		this.setText(buttonName);
		this.setBackgroundColor(COLORS[COLOR_INDEX % COLORS.length]);
		this.setOnClickListener(listener);
		this.setTextSize(18.0f);
	}
	
	
	static final int PADDING = 10;
	
	/**
	 * 添加一行演示按钮
	 * 
	 * @param bts
	 */
	public static void addDemoButtons(Activity activity, ViewGroup vg, DebugButton... bts) {
		LinearLayout llButtons = new LinearLayout(activity);
		vg.addView(llButtons);

		Rect outRect = new Rect();  
		activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(outRect);
		int widthScreen =outRect.width();
		LayoutParams lp = new LayoutParams(widthScreen / bts.length - 2
				* PADDING, 80);

		for (int i = 0; i < bts.length; ++i) {
			DebugButton bt = bts[i];
			FrameLayout fbt = new FrameLayout(activity);
			fbt.setPadding(PADDING, PADDING, PADDING, PADDING);
			fbt.addView(bt, lp);
			llButtons.addView(fbt);
		}

		DebugButton.nextColor();
	}
}
