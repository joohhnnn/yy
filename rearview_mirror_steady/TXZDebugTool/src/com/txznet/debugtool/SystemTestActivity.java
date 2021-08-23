package com.txznet.debugtool;

import android.view.View;
import android.view.View.OnClickListener;

import com.txznet.comm.base.BaseApplication;
import com.txznet.debugtool.util.SPThreshholdUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZCallManager;
import com.txznet.sdk.TXZCallManager.CallTool;
import com.txznet.sdk.TXZCallManager.CallToolStatusListener;
import com.txznet.sdk.TXZCallManager.Contact;
import com.txznet.widget.DebugButton;
import com.txznet.widget.DebugUtil;

public class SystemTestActivity extends BaseDebugActivity {

	private static CallToolStatusListener mCTSL = null;

	@Override
	protected void onInitButtons() {
		addDemoButtons(new DebugButton(this, "设置电话工具", new OnClickListener() {

			@Override
			public void onClick(View v) {

				TXZCallManager.getInstance().setCallTool(new CallTool() {

					@Override
					public void setStatusListener(
							CallToolStatusListener listener) {
						mCTSL = listener;
					}

					@Override
					public boolean rejectIncoming() {
						return false;
					}

					@Override
					public boolean makeCall(Contact con) {
						return false;
					}

					@Override
					public boolean hangupCall() {
						return false;
					}

					@Override
					public CallStatus getStatus() {
						return null;
					}

					@Override
					public boolean acceptIncoming() {
						return false;
					}
				});

				DebugUtil.showTips("设置电话工具");

			}
		}));

		addDemoButtons(new DebugButton(this, "空闲", new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCTSL.onIdle();
				DebugUtil.showTips("电话空闲");
			}
		}), new DebugButton(this, "接通", new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCTSL.onOffhook();
				DebugUtil.showTips("电话接通");
			}
		}));

		addDemoButtons(new DebugButton(this, "控制波形图上半部分",
				new OnClickListener() {
					@Override
					public void onClick(View v) {

						WaveformActivity.ifLeftShow = !(SPThreshholdUtil
								.getSPData(AppLogic.getApp(),
										SPThreshholdUtil.APPLICATION_SP_NAME,
										SPThreshholdUtil.IFLESHOW));

						SPThreshholdUtil.setSharedPreferencesData(
								AppLogic.getApp(),
								SPThreshholdUtil.APPLICATION_SP_NAME,
								SPThreshholdUtil.IFLESHOW,
								WaveformActivity.ifLeftShow);

						if (WaveformActivity.ifLeftShow) {
							DebugUtil.showTips("已开启波形图上半部分显示");
						} else {
							DebugUtil.showTips("已关闭波形图上半部分显示");
						}

					}
				}), new DebugButton(this, "控制波形图下半部分", new OnClickListener() {
			@Override
			public void onClick(View v) {

				WaveformActivity.ifRightShow = !(SPThreshholdUtil.getSPData(
						AppLogic.getApp(),
						SPThreshholdUtil.APPLICATION_SP_NAME,
						SPThreshholdUtil.IFRIGHTSHOW));

				SPThreshholdUtil.setSharedPreferencesData(AppLogic.getApp(),
						SPThreshholdUtil.APPLICATION_SP_NAME,
						SPThreshholdUtil.IFRIGHTSHOW,
						WaveformActivity.ifRightShow);

				if (WaveformActivity.ifRightShow) {
					DebugUtil.showTips("已开启波形图下半部分显示");
				} else {
					DebugUtil.showTips("已关闭波形图下半部分显示");
				}
			}
		}));

	}
}
