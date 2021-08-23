package com.txznet.sdkdemo.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.txznet.sdk.TXZPowerManager;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class PowerActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "休眠", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZPowerManager.getInstance().notifyPowerAction(
						TXZPowerManager.PowerAction.POWER_ACTION_BEFORE_SLEEP);
				TXZPowerManager.getInstance().releaseTXZ();

				DebugUtil.showTips("将释放同行者资源");
			}
		}), new DemoButton(this, "唤醒", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZPowerManager.getInstance().reinitTXZ(new Runnable() {
					@Override
					public void run() {
						TXZPowerManager
								.getInstance()
								.notifyPowerAction(
										TXZPowerManager.PowerAction.POWER_ACTION_WAKEUP);

						DebugUtil.showTips("重新初始化同行者完成");
					}
				});

				DebugUtil.showTips("正在重新初始化同行者");
			}
		}), new DemoButton(this, "震动唤醒", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZPowerManager.getInstance().reinitTXZ(new Runnable() {
					@Override
					public void run() {
						TXZPowerManager
								.getInstance()
								.notifyPowerAction(
										TXZPowerManager.PowerAction.POWER_ACTION_SHOCK_WAKEUP);

						DebugUtil.showTips("重新初始化同行者完成");
					}
				});

				DebugUtil.showTips("正在重新初始化同行者");
			}
		}));
	}
}
