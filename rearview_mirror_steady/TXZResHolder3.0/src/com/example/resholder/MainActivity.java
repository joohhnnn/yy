package com.example.resholder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.txznet.comm.ui.util.LayouUtil;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		View activity_main = LayouUtil.getView("activity_main");
		activity_main.setPadding((int)LayouUtil.getDimen("activity_vertical_margin"),
				(int)LayouUtil.getDimen("activity_horizontal_margin"),
				(int)LayouUtil.getDimen("activity_horizontal_margin"),
				(int)LayouUtil.getDimen("activity_vertical_margin"));

		setContentView(activity_main);
	}
}