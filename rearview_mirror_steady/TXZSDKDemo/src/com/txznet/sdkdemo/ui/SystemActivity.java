package com.txznet.sdkdemo.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.txznet.sdk.TXZSysManager;
import com.txznet.sdk.TXZSysManager.AppInfo;
import com.txznet.sdk.TXZSysManager.AppMgrTool;
import com.txznet.sdk.TXZSysManager.VolumeMgrTool;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class SystemActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "设置音量工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZSysManager.getInstance().setVolumeMgrTool(mVolumeMgrTool);

				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "取消音量工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZSysManager.getInstance().setVolumeMgrTool(null);

				DebugUtil.showTips(((Button) v).getText());
			}
		}));

		addDemoButtons(new DemoButton(this, "同步应用列表", new OnClickListener() {
			@Override
			public void onClick(View v) {
				syncAppInfoList();
			}
		}), new DemoButton(this, "设置应用管理工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZSysManager.getInstance().setAppMgrTool(mAppMgrTool);

				DebugUtil.showTips(((Button) v).getText());
			}
		}), new DemoButton(this, "取消应用管理工具", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZSysManager.getInstance().setAppMgrTool(null);

				DebugUtil.showTips(((Button) v).getText());
			}
		}));
	}

	private AppMgrTool mAppMgrTool = new AppMgrTool() {

		@Override
		public void openApp(String packageName) {
			DebugUtil.showTips("将打开" + packageName);
			// TODO 打开应用
			// super.openApp(packageName);
		}

		@Override
		public void closeApp(String packageName) {
			DebugUtil.showTips("将关闭" + packageName);
			// TODO 关闭应用
			// super.closeApp(packageName);
		}
	};

	private VolumeMgrTool mVolumeMgrTool = new VolumeMgrTool() {
		@Override
		public void mute(boolean enable) {
			if (enable)
				DebugUtil.showTips("关闭声音");
			else
				DebugUtil.showTips("打开声音");
			// TODO 静音控制
		}

		@Override
		public void minVolume() {
			DebugUtil.showTips("最小音量");
			// TODO 最小音量
		}

		@Override
		public void maxVolume() {
			DebugUtil.showTips("最大音量");
			// TODO 最大音量
		}

		@Override
		public void incVolume() {
			DebugUtil.showTips("增加音量");
			// TODO 增加音量
		}

		@Override
		public void decVolume() {
			DebugUtil.showTips("减小音量");
			// TODO 减小音量
		}

		@Override
		public boolean decVolume(int arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean incVolume(int arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isMaxVolume() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean isMinVolume() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean setVolume(int arg0) {
			// TODO Auto-generated method stub
			return false;
		}
	};

	private void syncAppInfoList() {
		// TODO 默认从系统同步应用列表，使用该方法同步后，将不再读取系统的
		AppInfo[] apps = new AppInfo[2];
		apps[0] = new AppInfo();
		apps[0].strAppName = "行车记录仪";
		apps[0].strPackageName = "com.txznet.record";
		apps[1] = new AppInfo();
		apps[1].strAppName = "电子狗";
		apps[1].strPackageName = "com.txznet.edog";
		TXZSysManager.getInstance().syncAppInfoList(apps);

		DebugUtil.showTips("已同步应用列表：行车记录仪、电子狗");
	}
}
