package com.txznet.sdkdemo.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class MainActivity extends BaseActivity {

	/**
	 * 进入demo演示模块
	 * 
	 * @param moduleName
	 * @param clsActivity
	 */
	private void startDemoModule(CharSequence moduleName,
			Class<? extends Activity> clsActivity) {
		if (!TXZConfigManager.getInstance().isInitedSuccess()) {
			DebugUtil.showTips("同行者引擎尚未初始化成功");
			return;
		}
		DebugUtil.showTips("进入: " + moduleName);
		Intent intentModule = new Intent();
		ComponentName cn = new ComponentName(MainActivity.this, clsActivity);
		intentModule.setComponent(cn);
		intentModule.putExtra("moduleTitle", moduleName);
		startActivity(intentModule);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "配置管理", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(),
						ConfigActivity.class);
			}
		}), new DemoButton(this, "进入倒车", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(), AsrActivity.class);
			}
		}),new DemoButton(this, "退出倒车", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(), AsrActivity.class);
			}
		}),new DemoButton(this, "语音识别", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(), AsrActivity.class);
			}
		}), new DemoButton(this, "语音合成", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(), TtsActivity.class);
			}
		}));

		addDemoButtons(new DemoButton(this, "电话功能", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(), CallActivity.class);
			}
		}), new DemoButton(this, "音乐对接", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZPoiSearchManager.getInstance().startSearch("火车站",null);
			}
		}), new DemoButton(this, "导航对接", new OnClickListener() {
			@Override
			public void onClick(View v) {
//				startDemoModule(((DemoButton) v).getText(), NavActivity.class);
				TXZPoiSearchManager.getInstance().startSearch("酒店",null);
			}
		}));

		addDemoButtons(new DemoButton(this, "抓拍对接", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(),
						CameraActivity.class);
			}
		}), new DemoButton(this, "电源对接", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(), PowerActivity.class);
			}
		}), new DemoButton(this, "系统对接", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(),
						SystemActivity.class);
			}
		}), new DemoButton(this, "场景对接", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(), SenceActivity.class);
			}
		}));

		addDemoButtons(new DemoButton(this, "状态对接", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(),
						StatusActivity.class);
			}
		}), new DemoButton(this, "资源修改", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDemoModule(((DemoButton) v).getText(),
						ResourceActivity.class);
			}
		}));
	}

}
