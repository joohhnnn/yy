package com.txznet.sdkdemo.ui;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.sdk.TXZStatusManager;
import com.txznet.sdk.TXZStatusManager.StatusListener;
import com.txznet.sdkdemo.bean.DebugUtil;
import com.txznet.sdkdemo.bean.DemoButton;

public class StatusActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addDemoButtons(new DemoButton(this, "监听状态", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZStatusManager.getInstance().addStatusListener(
						mMusicStatusListener);
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}), new DemoButton(this, "取消监听", new OnClickListener() {
			@Override
			public void onClick(View v) {
				TXZStatusManager.getInstance().removeStatusListener(
						mMusicStatusListener);
				
				DebugUtil.showTips(((Button)v).getText());
			}
		}));
	}

	StatusListener mMusicStatusListener = new StatusListener() {
		@Override
		public void onMusicPlay() {
			MusicModel model = TXZMusicManager.getInstance()
					.getCurrentMusicModel();
			if (model != null) {
				DebugUtil.showTips("开始播放：" + model.getTitle() + "-"
						+ DebugUtil.convertArrayToString(model.getArtist()));
			} else {
				DebugUtil.showTips("开始播放未知音乐");
			}
		}

		@Override
		public void onMusicPause() {
			DebugUtil.showTips("停止播放音乐");
		}

		@Override
		public void onEndTts() {
			DebugUtil.showTips("语音播报结束");
		}

		@Override
		public void onEndCall() {
			DebugUtil.showTips("电话结束");
		}

		@Override
		public void onEndAsr() {
			DebugUtil.showTips("识别结束");
		}

		@Override
		public void onBeginTts() {
			DebugUtil.showTips("语音播报开始");
		}

		@Override
		public void onBeginCall() {
			DebugUtil.showTips("电话开始");
		}

		@Override
		public void onBeginAsr() {
			DebugUtil.showTips("识别开始");
		}

		@Override
		public void onBeepEnd() {
			DebugUtil.showTips("BEEP音结束");
		}
	};
}
