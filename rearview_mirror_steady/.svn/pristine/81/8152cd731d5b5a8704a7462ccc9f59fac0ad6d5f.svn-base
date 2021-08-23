package com.txznet.team;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.txznet.comm.base.BaseActivity;
import com.txznet.loader.AppLogic;

public class MainActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppLogic.setMainActivity(this);
		View v = buildContentView(R.layout.activity_main);
		setContentView(v);
		WinControler.getInstance().setContentView(v);
	}

	@Override
	protected void onStart() {
		super.onStart();
		WinControler.getInstance().snycBQCode();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		AppLogic.setMainActivity(null);
		super.onDestroy();
	}

	private View buildContentView(int layoutId) {
		return LayoutInflater.from(MainActivity.this).inflate(layoutId, null);
	}
}
