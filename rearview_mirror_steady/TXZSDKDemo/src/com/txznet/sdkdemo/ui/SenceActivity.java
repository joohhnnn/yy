package com.txznet.sdkdemo.ui;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.txznet.sdk.TXZResourceManager;
import com.txznet.sdk.TXZSenceManager;
import com.txznet.sdk.TXZSenceManager.SenceTool;
import com.txznet.sdk.TXZSenceManager.SenceType;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class SenceActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "捕获命令场景", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZSenceManager.getInstance().setSenceTool(
						TXZSenceManager.SenceType.SENCE_TYPE_COMMAND,
						mCommandSenceTool);
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}), new DemoButton(this, "取消场景捕获", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZSenceManager.getInstance().setSenceTool(
						TXZSenceManager.SenceType.SENCE_TYPE_COMMAND, null);
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}));
	}

	SenceTool mCommandSenceTool = new SenceTool() {
		@Override
		public boolean process(SenceType type, String data) {
			try {
				JSONObject json = new JSONObject(data);
				String cmd = json.optString("cmd");
				if ("wifi_on".equals(cmd)) {
					// TODO 打开WIFI
					TXZResourceManager.getInstance().speakTextOnRecordWin(
							"捕获到了打开wifi，已为您打开wifi", true, null);
					return true; // 已处理的命令返回true，否则还是会走内部交互
				}
				if ("wifi_off".equals(cmd)) {
					TXZResourceManager.getInstance().speakTextOnRecordWin(
							"捕获到了关闭wifi，将为您关闭wifi", true, new Runnable() {
								@Override
								public void run() {
									// TODO 关闭WIFI
								}
							});
					return true; // 已处理的命令返回true，否则还是会走内部交互
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return false;
		}
	};
}
