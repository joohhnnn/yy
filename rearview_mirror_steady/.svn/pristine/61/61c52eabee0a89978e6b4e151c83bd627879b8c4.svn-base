package com.txznet.sdkdemo.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZAsrManager.AsrComplexSelectCallback;
import com.txznet.sdk.TXZAsrManager.CommandListener;
import com.txznet.sdk.TXZPowerManager;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class AsrActivity extends BaseActivity {

	CommandListener mCommandListener = new CommandListener() {
		@Override
		public void onCommand(String cmd, String data) {
			if ("OPEN_AIRCON".equals(data)) {
				TXZResourceManager.getInstance().speakTextOnRecordWin(
						"将为您打开空调", true, new Runnable() {
							@Override
							public void run() {
								// TODO 打开空调，先提示后打开空调
							}
						});
				return;
			}
			if ("CLOSE_AIRCON".equals(data)) {
				// TODO 关闭空调，先关后提示
				TXZResourceManager.getInstance().speakTextOnRecordWin(
						"已为您关闭空调", true, null);
				return;
			}

			if (data.startsWith("FM#")) {
				DebugUtil.showTips("调频到：" + data.substring("FM#".length()));
				return;
			}
		}
	};

	String[] arrOpenAircon = new String[] { "打开空调", "开启空调" };
	String[] arrCloseAircon = new String[] { "关闭空调", "关掉空调" };

	AsrComplexSelectCallback mAsrComplexSelectCallback = new AsrComplexSelectCallback() {
		@Override
		public boolean needAsrState() {
			// TODO 是否需要识别状态，识别会对系统静音
			return false;
		}

		@Override
		public String getTaskId() {
			// TODO 返回任务ID，可以取消唤醒识别任务
			return "WAKEUP_TASK";
		}

		@Override
		public void onCommandSelected(String type, String command) {
			if ("OPEN_SCREEN".equals(type)) {
				// TODO 亮屏
				DebugUtil.showTips("已识别到：" + command);
				return;
			}
			if ("CLOSE_SCREEN".equals(type)) {
				// TODO 熄屏
				DebugUtil.showTips("已识别到：" + command);
				return;
			}
		};
	} //
	.addCommand("OPEN_SCREEN", "进入倒车", "开启屏幕") //
			.addCommand("CLOSE_SCREEN", "退出倒车", "熄灭屏幕");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "模拟声控按钮", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZAsrManager.getInstance().triggerRecordButton();
			}
		}), new DemoButton(this, "停止录音立即识别", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZAsrManager.getInstance().stop();
			}
		}), new DemoButton(this, "取消声控", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZAsrManager.getInstance().cancel();
			}
		}));

		addDemoButtons(new DemoButton(this, "无界面启动声控", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZAsrManager.getInstance().start();
			}
		}), new DemoButton(this, "带提示启动声控", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZAsrManager.getInstance().start("有什么可以帮您");
			}
		}), new DemoButton(this, "通过文本启动声控", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZAsrManager.getInstance().startWithRawText("导航去世界之窗");
			}
		}));

		addDemoButtons(new DemoButton(this, "注册命令", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZAsrManager.getInstance()
						.addCommandListener(mCommandListener);
				TXZAsrManager.getInstance().regCommand(arrOpenAircon,
						"OPEN_AIRCON");
				TXZAsrManager.getInstance().regCommand(arrCloseAircon,
						"CLOSE_AIRCON");

				DebugUtil.showTips("已增加命令字："
						+ DebugUtil.convertArrayToString(arrOpenAircon) + "、"
						+ DebugUtil.convertArrayToString(arrCloseAircon));
			}
		}), new DemoButton(this, "反注册命令", new OnClickListener() {
			@Override
			public void onClick(View v) {

				TXZAsrManager.getInstance().unregCommand(arrOpenAircon);
				TXZAsrManager.getInstance().unregCommand(arrCloseAircon);
				TXZAsrManager.getInstance().removeCommandListener(
						mCommandListener);

				DebugUtil.showTips("已删除命令字："
						+ DebugUtil.convertArrayToString(arrOpenAircon) + "、"
						+ DebugUtil.convertArrayToString(arrCloseAircon));
			}
		}), new DemoButton(this, "注册FM命令", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZAsrManager.getInstance()
						.addCommandListener(mCommandListener);
				TXZAsrManager.getInstance()
						.regCommandForFM(88.0F, 108.9F, "FM");

				DebugUtil.showTips("已注册FM频段：88.0~108.9");
			}
		}));

		addDemoButtons(new DemoButton(this, "注册唤醒命令", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZAsrManager.getInstance().useWakeupAsAsr(
						mAsrComplexSelectCallback);

				DebugUtil.showTips("已增加全局命令字："
						+ DebugUtil
								.convertArrayToString(mAsrComplexSelectCallback
										.genKeywords()));
			}
		}), new DemoButton(this, "反注册唤醒命令", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZAsrManager.getInstance().recoverWakeupFromAsr(
						mAsrComplexSelectCallback.getTaskId());

				DebugUtil.showTips("已删除全局命令字："
						+ DebugUtil
								.convertArrayToString(mAsrComplexSelectCallback
										.genKeywords()));
			}
		}));
	}
}