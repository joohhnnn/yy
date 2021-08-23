package com.txznet.sdkdemo.ui;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.sdkdemo.R;
import com.txznet.sdkdemo.bean.DemoButton;
import com.txznet.sdkdemo.bean.ScreenUtils;

public class BaseActivity extends Activity {

	private static LinearLayout mFunctions;

	public static AudioManager mAudioManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		Intent intent = this.getIntent();
		String moduleName = "";
		if (intent != null) {
			moduleName = intent.getStringExtra("moduleTitle");
		}
		if (TextUtils.isEmpty(moduleName)) {
			moduleName = "同行者演示程序";
		}

		((TextView) findViewById(R.id.txtTitleName)).setText(moduleName);

		findViewById(R.id.back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BaseActivity.this.finish();
			}
		});
		mFunctions = (LinearLayout) findViewById(R.id.llFunctions);
	}

	static final int PADDING = 10;

	/**
	 * 添加一行演示按钮
	 * 
	 * @param bts
	 */
	public void addDemoButtons(DemoButton... bts) {
		LinearLayout llButtons = new LinearLayout(this);
		mFunctions.addView(llButtons);

		int widthScreen = ScreenUtils.getScreenWidth(BaseActivity.this);
		LayoutParams lp = new LayoutParams(widthScreen / bts.length - 2
				* PADDING, 80);

		for (int i = 0; i < bts.length; ++i) {
			DemoButton bt = bts[i];
			FrameLayout fbt = new FrameLayout(this);
			fbt.setPadding(PADDING, PADDING, PADDING, PADDING);
			fbt.addView(bt, lp);
			llButtons.addView(fbt);
		}

		DemoButton.nextColor();
	}
}
