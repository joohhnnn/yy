package com.txznet.sdkdemo.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZConfigManager.FloatToolType;
import com.txznet.sdk.TXZConfigManager.UserConfigListener;
import com.txznet.sdk.TXZTtsPlayerManager;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class ConfigActivity extends BaseActivity {

	private static final String TAG = "ConfigActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "判断初始化是否成功", new OnClickListener() {
			@Override
			public void onClick(View v) {
				DebugUtil.showTips("初始化状态: "
						+ TXZConfigManager.getInstance().isInitedSuccess());
				TXZTtsPlayerManager.getInstance().speakText("txzcore 啊速度快娟啊爱死了放假拉可使肌肤沙拉疯狂机啊杀了开发家里靠发杀了开发的接收到了咖啡机来刷卡机", new TXZTtsPlayerManager.ITtsCallback() {
					@Override
					public void onPause() {
						super.onPause();
						Log.d(TAG, "onPause: ");
					}

					@Override
					public void onResume() {
						super.onResume();
						Log.d(TAG, "onResume: ");
					}

					@Override
					public void onBegin() {
						super.onBegin();
						Log.d(TAG, "onBegin: ");
					}

					@Override
					public void onEnd() {
						super.onEnd();
						Log.d(TAG, "onEnd: ");
					}

					@Override
					public void onCancel() {
						super.onCancel();
						Log.d(TAG, "onCancel: ");
					}

					@Override
					public void onSuccess() {
						super.onSuccess();
						Log.d(TAG, "onSuccess: ");
					}

					@Override
					public void onError(int iError) {
						super.onError(iError);
						Log.d(TAG, "onError: ");
					}

					@Override
					public boolean isNeedStartAsr() {
						return super.isNeedStartAsr();
					}
				});
			}
		}));

		addDemoButtons(new DemoButton(this, "隐藏声控按钮", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZConfigManager.getInstance().showFloatTool(
						FloatToolType.FLOAT_NONE);
				
				DebugUtil.showTips("已为您隐藏声控按钮");
			}
		}), new DemoButton(this, "显示声控按钮", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZConfigManager.getInstance().showFloatTool(
						FloatToolType.FLOAT_NORMAL);
				
				DebugUtil.showTips("已为您显示声控按钮");
			}
		}), new DemoButton(this, "置顶声控按钮", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZConfigManager.getInstance().showFloatTool(
						FloatToolType.FLOAT_TOP);
				
				DebugUtil.showTips("已为您置顶声控按钮");
			}
		}));

		addDemoButtons(new DemoButton(this, "打开调试日志", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZConfigManager.getInstance().setLogLevel(Log.DEBUG);
				DebugUtil.showTips("已为您打开调试日志");
			}
		}), new DemoButton(this, "关闭调试日志", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZConfigManager.getInstance().setLogLevel(Log.ERROR);
				DebugUtil.showTips("已为您关闭日志");
			}
		}));

		addDemoButtons(new DemoButton(this, "允许用户修改唤醒词", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZConfigManager.getInstance().enableChangeWakeupKeywords(true);
			}
		}), new DemoButton(this, "禁止用户修改唤醒词", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZConfigManager.getInstance()
						.enableChangeWakeupKeywords(false);
			}
		}));

		addDemoButtons(new DemoButton(this, "清空唤醒词", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZConfigManager.getInstance().setWakeupKeywordsNew(
						new String[] {});
			}
		}), new DemoButton(this, "修改唤醒词", new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] kws = new String[] { "你好同行者", "同行者你好" };
				TXZConfigManager.getInstance().setWakeupKeywordsNew(kws);
				DebugUtil.showTips("唤醒词修改为："
						+ DebugUtil.convertArrayToString(kws));
			}
		}), new DemoButton(this, "监听声控修改唤醒词", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZConfigManager.getInstance().setUserConfigListener(
						mUserConfigListener);
				DebugUtil.showTips("已为您监听声控修改唤醒词");
			}
		}));
		addDemoButtons(new DemoButton(this, "开启唤醒大模型", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZConfigManager.getInstance().setUseHQualityWakeupModel(true);
				DebugUtil.showTips("已开启唤醒大模型");
			}
		}), new DemoButton(this, "关闭唤醒大模型", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZConfigManager.getInstance().setUseHQualityWakeupModel(false);
				DebugUtil.showTips("已关闭唤醒大模型");
			}
		}));
		addDemoButtons(new DemoButton(this, "启用远程TtsTool", new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TXZConfigManager.getInstance().enableRemoteTtsTool(true);
				DebugUtil.showTips("已启用远程TtsTool");
			}
		}), new DemoButton(this, "关闭远程TtsTool", new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TXZConfigManager.getInstance().enableRemoteTtsTool(false);
				DebugUtil.showTips("关闭远程TtsTool");
			}
		}));
	}

	private UserConfigListener mUserConfigListener = new UserConfigListener() {
		@Override
		public void onChangeWakeupKeywords(String[] keywords) {
			DebugUtil.showTips("用户修改唤醒词为："
					+ DebugUtil.convertArrayToString(keywords));
		}

		@Override
		public void onChangeCommunicationStyle(String style) {
			DebugUtil.showTips("用户修改语音交互风格为：" + style);
		}
	};
}
