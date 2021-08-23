package com.txznet.debugtool;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.txznet.widget.DebugButton;

public class DebugMainActivity extends BaseDebugActivity {
	@Override
	protected void onInitButtons() {
		addDemoButtons(new DebugButton(this, "系统功能", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDebugModule(((Button) v).getText(),
						SystemTestActivity.class);
			}
		}), new DebugButton(this, "录音测试", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDebugModule(((Button) v).getText(),
						RecordTestActivity.class);
			}
		}), new DebugButton(this, "升级测试", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDebugModule(((Button) v).getText(),
						UpgradeTestActivity.class);
			}
		}));

		addDemoButtons(new DebugButton(this, "播放录音", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDebugModule(((Button) v).getText(),
						PlayRecordActivity.class);
			}
		}), new DebugButton(this, "测试播报TTS", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDebugModule(((Button) v).getText(), TtsTestActivity.class);
			}
		}));

		addDemoButtons(new DebugButton(this, "实时绘制录音波形", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDebugModule(((Button) v).getText(), WaveformActivity.class);
			}
		}), new DebugButton(this, "长录音", new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDebugModule(((Button) v).getText(),
						LongRecordActivity.class);
				Toast.makeText(DebugMainActivity.this, "123456", Toast.LENGTH_LONG).show();
			}
		}));
	}

}
