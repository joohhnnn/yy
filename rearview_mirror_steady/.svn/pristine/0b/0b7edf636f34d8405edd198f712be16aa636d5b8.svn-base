package com.txznet.sdkdemo.bean;

import android.content.Context;
import android.widget.Button;

public class DemoButton extends Button {
	public static int COLOR1 = 0xFF5698BB;
	public static int COLOR2 = 0xFF569874;
	public static int COLOR3 = 0xaa5698CD;
	public static int COLOR4 = 0x66CD9856;

	private static int[] COLORS = new int[] { COLOR1, COLOR2, COLOR3, COLOR4 };
	private static int COLOR_INDEX = 0;

	public static void nextColor() {
		COLOR_INDEX = (COLOR_INDEX + 1) % COLORS.length;
	}

	public DemoButton(Context context, String buttonName, int buttonColor,
			OnClickListener listener) {
		super(context);

		this.setText(buttonName);
		this.setBackgroundColor(buttonColor);
		this.setOnClickListener(listener);
		this.setTextSize(18.0f);
	}

	public DemoButton(Context context, String buttonName,
			OnClickListener listener) {
		super(context);

		this.setText(buttonName);
		this.setBackgroundColor(COLORS[COLOR_INDEX % COLORS.length]);
		this.setOnClickListener(listener);
		this.setTextSize(18.0f);
	}
}
