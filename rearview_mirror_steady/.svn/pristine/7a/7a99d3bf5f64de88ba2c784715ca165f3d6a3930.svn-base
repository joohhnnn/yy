package com.txznet.txz.entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;

import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.module.launch.LaunchManager;

/**
 * 启动Activity，用于被动启动窗口
 * 
 * @author bihongpi
 *
 */
public class LaunchActivity extends Activity {

	public void onLaunch() {

	}	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		AppLogic.printStatementCycle(this.toString());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		LaunchManager.getInstance().onMainActivityCreate();

		onLaunch();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		AppLogic.printStatementCycle(this.toString());
		super.onNewIntent(intent);
	}

	@Override
	protected void onStart() {
		AppLogic.printStatementCycle(this.toString());
		super.onStart();
	}

	@Override
	protected void onResume() {
		AppLogic.printStatementCycle(this.toString());
		super.onResume();
	}

	@Override
	protected void onRestart() {
		AppLogic.printStatementCycle(this.toString());
		super.onRestart();
		onLaunch();
	}

	@Override
	protected void onPause() {
		AppLogic.printStatementCycle(this.toString());
		super.onPause();
	}

	@Override
	protected void onStop() {
		AppLogic.printStatementCycle(this.toString());
		super.onStop();

		// 按home时强制关闭录音
		// RecorderWin.close();
		// AsrManager.getInstance().cancel();
	}

	@Override
	protected void onDestroy() {
		AppLogic.printStatementCycle(this.toString());
		super.onDestroy();
	}

	@Override
	public void onBackPressed() {
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		CrashCommonHandler.getInstance().setAgain();
		return super.dispatchTouchEvent(ev);
	}
}
