package com.txznet.cldfm.util;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogicBase;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

public class FMControl {
	private static final int BASE_FM = 85;
	private static final String FM_ACTION = "com.hipad.setting.fm.action";
	private static final String FM_EXTRA = "com.hipad.setting.fm.extra";

	private static FMControl sControl = new FMControl();

	private FMControl() {
		getInstanceState();

		IntentFilter iFilter = new IntentFilter("FM_CONTROL_ACTION");
		AppLogicBase.getApp().registerReceiver(new FMReceiver(), iFilter);

		IntentFilter fmFilter = new IntentFilter("com.hipad.setting.fm.response");
		AppLogicBase.getApp().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				Log.e("FM", "===== fm backto onReceive=====");
				// 获取FM发出来的广播
				byte[] freq = intent.getByteArrayExtra(FM_EXTRA);
				if (freq != null) {
					byte flag = freq[0];
					byte value = freq[1];
					if (flag == 0x02) {
						if (value == 0x00) { // 关闭
							mIsOpen = false;
							if (mFmListener != null) {
								mFmListener.FmStateChange(false, getCurrentFreq());
							}
							Log.e("FM", "===== fm backto status close=====");
						} else { // 打开
							mIsOpen = true;
							if (mFmListener != null) {
								mFmListener.FmStateChange(true, getCurrentFreq());
							}
							Log.e("FM", "===== fm backto status open=====");
						}
					} else if (flag == 0x04) {
						// 查询频率
						mFreq = value;
						if (mFmListener != null) {
							mFmListener.FmStateChange(true, getCurrentFreq());
						}
						Log.e("FM", "===== fm backto current freq=====");
					}
				}
			}
		}, fmFilter);

		AppLogicBase.removeBackGroundCallback(mCheckStatusRunnable);
		AppLogicBase.runOnBackGround(mCheckStatusRunnable, 10);
	}

	public static FMControl getInstance() {
		return sControl;
	}

	private Activity mActivity;

	public void init(Activity activity) {
		this.mActivity = activity;
	}

	public void execOpenStatus() {
		Intent intent = new Intent(FM_ACTION);
		byte[] buffer = new byte[2];
		buffer[0] = 0x02;
		buffer[1] = (byte) 0x00;
		intent.putExtra(FM_EXTRA, buffer);
		GlobalContext.get().sendBroadcast(intent);
		Log.e("FM", "===== start query fm status=====");
	}

	Runnable mCheckStatusRunnable = new Runnable() {

		@Override
		public void run() {
			execOpenStatus();
		}
	};

	public void execFreq() {
		Intent intent = new Intent(FM_ACTION);
		byte[] buffer = new byte[2];
		buffer[0] = 0x04;
		buffer[1] = (byte) 0x00;
		intent.putExtra(FM_EXTRA, buffer);
		GlobalContext.get().sendBroadcast(intent);
	}

	public boolean open() {
		if (mIsOpen) {
			return true;
		}

		Intent intent = new Intent(FM_ACTION);
		byte[] buffer = new byte[2];
		buffer[0] = 0x03;
		buffer[1] = (byte) 0xff;
		intent.putExtra(FM_EXTRA, buffer);
		mActivity.sendBroadcast(intent);
		LogUtil.logd("FMControl open");
		mIsOpen = true;
		getInstanceState();
		setFMFreq(mCurFreq, true);
		if (mFmListener != null) {
			mFmListener.FmStateChange(true, getCurrentFreq());
		}

		AppLogicBase.removeBackGroundCallback(mCheckStatusRunnable);
		AppLogicBase.runOnBackGround(mCheckStatusRunnable, 3000);
		return true;
	}

	public boolean close() {
		if (!mIsOpen) {
			return true;
		}

		Intent intent = new Intent(FM_ACTION);
		byte[] buffer = new byte[2];
		buffer[0] = 0x03;
		buffer[1] = 0x00;
		intent.putExtra(FM_EXTRA, buffer);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("FMControl close");
		mIsOpen = false;
		if (mFmListener != null) {
			mFmListener.FmStateChange(false, getCurrentFreq());
		}
		saveInstanceState();

		AppLogicBase.removeBackGroundCallback(mCheckStatusRunnable);
		AppLogicBase.runOnBackGround(mCheckStatusRunnable, 3000);
		return true;
	}

	boolean mIsOpen;

	public boolean isFmOpen() {
		return mIsOpen;
	}

	int mFreq;

	float mCurFreq;

	public float getCurrentFreq() {
		// TODO Freq 转为频率
		if (mFreq == 0) {
			return mCurFreq;
		}

		mCurFreq = (mFreq / 10.0f) + BASE_FM;
		return mCurFreq;
	}

	public void setFMFreq(float freq, boolean ismanual) {
		Intent intent = new Intent(FM_ACTION);
		byte[] buffer = new byte[2];
		buffer[0] = 0x05;
		buffer[1] = (byte) ((freq - BASE_FM) * 10);
		intent.putExtra(FM_EXTRA, buffer);
		GlobalContext.get().sendBroadcast(intent);
		LogUtil.logd("FMControl setFMFreq:" + freq);
		mCurFreq = freq;
		FmPreferenceUtil.getInstance().setFrequency(freq);
		if (mFmListener != null) {
			mFmListener.FmFreqChange(mCurFreq, ismanual);
		}
	}

	private void getInstanceState() {
		mCurFreq = FmPreferenceUtil.getInstance().getFrequency();
	}

	public void saveInstanceState() {
		FmPreferenceUtil.getInstance().setFrequency(getCurrentFreq());
		FmPreferenceUtil.getInstance().setFMOpenStatus(isFmOpen());
	}

	FmListener mFmListener;

	public void setFmListener(FmListener listener) {
		this.mFmListener = listener;
	}

	public static interface FmListener {
		public void FmStateChange(boolean open, float channel);

		public void FmFreqChange(float freq, boolean ismanual);
	}

	private class FMReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int[] buf = intent.getIntArrayExtra("FM_EXTRA");
			if (buf == null) {
				return;
			}

			switch (buf[0]) {
			case 0x01:
				Log.e("FM", "===== fm receiver open command=====");
				// 打开FM
				open();
				if (mIsOpen) {
					TtsUtil.speakText("当前频率为" + mCurFreq + "兆赫");
				}
				break;

			case 0x02:
				Log.e("FM", "===== fm receiver close command=====");
				// 关闭FM
				close();
				break;

			case 0x03:
				Log.e("FM", "===== fm receiver set freq=====");
				// 设置频率
				setFMFreq(buf[1] / 10.0f, false);
				break;

			case 0x04:
				Log.e("FM", "===== fm receiver backpress=====");
				AppLogicBase.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						if (mActivity != null) {
							try {
								mActivity.finish();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}, 0);
				break;

			default:
				break;
			}
		}
	}
}