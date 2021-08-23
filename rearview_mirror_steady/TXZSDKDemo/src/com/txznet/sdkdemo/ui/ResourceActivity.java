package com.txznet.sdkdemo.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.txznet.sdk.TXZResourceManager;
import com.txznet.sdk.TXZResourceManager.RecordWin;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class ResourceActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "替换录音窗口", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZResourceManager.getInstance().setRecordWin(mRecordWin);

				DebugUtil.showTips("已替换录音窗口");
			}
		}), new DemoButton(this, "还原录音窗口", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZResourceManager.getInstance().setRecordWin(null);

				DebugUtil.showTips("已还原录音窗口");
			}
		}));

		addDemoButtons(new DemoButton(this, "替换提示语", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZResourceManager.getInstance().setTextResourceString(
						"RS_VOICE_ASR_START_HINT", "新的启动提示语");
				TXZResourceManager.getInstance().setTextResourceString(
						"RS_VOICE_EMPTY_CLOSE",
						new String[] { "没有说话的提示语1", "没有说话的提示语2" });

				DebugUtil.showTips("已修改启动提示和没有说话的提示");
			}
		}));
	}

	RecordWin mRecordWin = new RecordWin() {
		@Override
		public void showWheatherInfo(String data) {
			DebugUtil.showTips("显示天气数据：" + data);
		}

		@Override
		public void showUsrText(String data) {
			DebugUtil.showTips("显示用户文本：" + data);
		}

		@Override
		public void showUsrPartText(String s) {

		}

		@Override
		public void showSysText(String data) {
			DebugUtil.showTips("显示系统文本：" + data);
		}

		@Override
		public void showStockInfo(String data) {
			DebugUtil.showTips("显示股票数据：" + data);
		}

		@Override
		public void showContactChoice(String data) {
			DebugUtil.showTips("显示联系人选择数据：" + data);
		}

		@Override
		public void showAddressChoice(String data) {
			DebugUtil.showTips("显示地址选择数据：" + data);
		}

		@Override
		public void setOperateListener(RecordWinOperateListener listener) {
			// TODO 记录下监听器，通知窗口操作给同行者
		}

		@Override
		public void open() {
			DebugUtil.showTips("打开录音窗口");
		}

		@Override
		public void onVolumeChange(int volume) {
			// DebugUtil.showTips("音量变化：" + volume);
			// TODO 处理音量变化
		}

		@Override
		public void onStatusChange(RecordStatus status) {
			DebugUtil.showTips("录音状态变化：" + status);
		}

		@Override
		public void onProgressChanged(int progress) {
			DebugUtil.showTips("自动操作进度条变化：" + progress);
		}

		@Override
		public void close() {
			DebugUtil.showTips("关闭录音窗口");
		}
		
		@Override
		public void showAudioChoice(String data) {
			DebugUtil.showTips("显示音频选择数据：" + data);
		}

		@Override
		public void showWxContactChoice(String data) {
			DebugUtil.showTips("显示微信联系人选择数据：" + data);
		}

		@Override
		public void snapPager(boolean arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onItemSelect(int i) {

		}

		@Override
		public void showListChoice(int arg0, String arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void showData(String arg0) {
			// TODO Auto-generated method stub
			
		}
	};
}
