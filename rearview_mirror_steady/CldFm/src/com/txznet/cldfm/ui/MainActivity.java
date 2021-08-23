package com.txznet.cldfm.ui;

import com.txznet.cldfm.R;
import com.txznet.cldfm.ui.widget.SeekBarEx;
import com.txznet.cldfm.ui.widget.wheel.WheelDatePicker;
import com.txznet.cldfm.ui.widget.wheel.WheelDatePicker.OnDateChangedListener;
import com.txznet.cldfm.ui.widget.wheel.WheelDatePicker.WheelScrollListener;
import com.txznet.cldfm.util.FMControl;
import com.txznet.cldfm.util.FMControl.FmListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.runnables.Runnable1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

public class MainActivity extends Activity implements OnClickListener {

	private static final int BASESTARTVALUE = 84;
	private static final float SCALE_RATIO = 10.0f;
	private static final float MIN_FM_DEGREE = 84.00f;
	private static final float MAX_FM_DEGREE = 108.00f;
	private static final float SEEKBAR_MAX = (MAX_FM_DEGREE - MIN_FM_DEGREE) * SCALE_RATIO;

	private CheckBox mPowerCb;
	private WheelDatePicker mPicker;
	private SeekBarEx mSeekBarEx;

	private TextView mPowerStatusTextView;
	private TextView mFmMhzTv;

	private int mCurrentLeftDegress;
	private int mCurrentRightDegress;
	private float mCurrentFMDegress;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initHandler();
		setContentView(R.layout.fm_activity);

		FMControl.getInstance().init(this);
		FMControl.getInstance().setFmListener(new FmListener() {

			@Override
			public void FmStateChange(boolean open, float channel) {
				mMainHandler.post(new Runnable() {

					@Override
					public void run() {
						init();
					}
				});
			}

			@Override
			public void FmFreqChange(float freq, boolean ismanual) {
				if (!ismanual) {
					initUiStatus(true);
				}
			}
		});

		findWidget();
		init();
		mPowerCb.setOnClickListener(this);
	}

	private void findWidget() {
		mPowerCb = (CheckBox) this.findViewById(R.id.power_status_cb);
		mPowerStatusTextView = (TextView) this.findViewById(R.id.power_status_tv);
		mFmMhzTv = (TextView) this.findViewById(R.id.fm_mhz_tv);
		mPicker = (WheelDatePicker) this.findViewById(R.id.picker_date_wdp);
		mSeekBarEx = (SeekBarEx) this.findViewById(R.id.fm_progressBar);
	}

	private void initDatePickView() {

		mPicker.setOnWheelScrollListener(new WheelScrollListener() {

			@Override
			public void onScrollStatus(boolean isScroll) {

				float rightValue = mCurrentRightDegress / 100.0f;
				mCurrentFMDegress = mCurrentLeftDegress + rightValue;
				// 打开此频道
				invokeFMOpen();
				setProgressBarThumbPosition(mCurrentFMDegress);
			}
		});
	}

	private void invokeFMOpen() {
		FMControl.getInstance().open();
		mAppLogicBase.removeCallbacks(mSetFreqRunnable);
		mAppLogicBase.postDelayed(mSetFreqRunnable, 200);
	}

	Runnable mSetFreqRunnable = new Runnable() {

		@Override
		public void run() {
			LogUtil.logd("MainActivity setFreq:" + mCurrentFMDegress);
			FMControl.getInstance().setFMFreq(mCurrentFMDegress, true);
		}
	};

	private void setProgressBarThumbPosition(float currentFrequency) {
		mSeekBarEx.setProgress((int) ((currentFrequency - MIN_FM_DEGREE) * 10));
	}

	private void initWheelDatePicker(float currentDegress) {
		LogUtil.logi("initWheelDatePicker currentDegress is:" + currentDegress);
		int leftValue = (int) currentDegress;
		int rightValue = (int) (currentDegress * 10 - leftValue * 10);

		if (mPicker.getVisibility() == View.INVISIBLE || mPicker.getVisibility() == View.GONE) {
			mPicker.setVisibility(View.VISIBLE);
		}

		if (leftValue == 0) {
			leftValue = 99;
		}

		mPicker.init(leftValue - BASESTARTVALUE, rightValue, onDateChangedListener);
	}

	private OnDateChangedListener onDateChangedListener = new WheelDatePicker.OnDateChangedListener() {

		@Override
		public void onDateChanged(WheelDatePicker view, int left, int right) {
			mCurrentLeftDegress = left + BASESTARTVALUE;
			mCurrentRightDegress = right * 10;
			LogUtil.logd(
					"MainActivity onDateChanged leftDeg:" + mCurrentLeftDegress + ",rightDeg:" + mCurrentRightDegress);
		}
	};

	private void init() {
		initDatePickView();
		this.mSeekBarEx.setMax((int) SEEKBAR_MAX);
		initUiStatus(FMControl.getInstance().isFmOpen());
		this.mPowerCb.setChecked(FMControl.getInstance().isFmOpen());
	}

	@SuppressLint("NewApi")
	private void initUiStatus(boolean isOpen) {
		LogUtil.logd("initUiStatus:" + isOpen);
		if (isOpen) {
			this.mPowerStatusTextView.setText(getString(R.string.string_fm_opened));
			this.mSeekBarEx.getThumb().setAlpha(255);
			this.mFmMhzTv.setTextColor(getResources().getColor(R.color.color_white));
			this.mSeekBarEx.setBackground(getResources().getDrawable(R.drawable.fm_graduaction));
			setProgressBarThumbPosition(FMControl.getInstance().getCurrentFreq());
			initWheelDatePicker(FMControl.getInstance().getCurrentFreq());
		} else {
			this.mPowerStatusTextView.setText(getString(R.string.string_fm_closed));
			this.mSeekBarEx.getThumb().setAlpha(0);
			this.mFmMhzTv.setTextColor(getResources().getColor(R.color.color_gray));
			this.mSeekBarEx.setBackground(getResources().getDrawable(R.drawable.fm_graduaction_gray));
			mPicker.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		mPowerCb.setChecked(FMControl.getInstance().isFmOpen());
	}

	@Override
	protected void onStop() {
		super.onStop();
		FMControl.getInstance().saveInstanceState();
	}

	private Runnable1<Boolean> mOpenFMRunnable;

	@Override
	public void onClick(View v) {
		mAppLogicBase.removeCallbacks(mOpenFMRunnable);
		mOpenFMRunnable = new Runnable1<Boolean>(!FMControl.getInstance().isFmOpen()) {

			@Override
			public void run() {
				if (FMControl.getInstance().isFmOpen() == mP1)
					return;
				if ((boolean) mP1) {
					if (FMControl.getInstance().open()) {
						FMControl.getInstance().setFMFreq(FMControl.getInstance().getCurrentFreq(), true);
					} else {
						LogUtil.loge("open FM fail!");
						mPowerCb.setChecked(false);
					}
				} else {
					if (!FMControl.getInstance().close()) {
						LogUtil.loge("close FM fail!");
					}
				}
			}
		};
		mAppLogicBase.postDelayed(mOpenFMRunnable, 0);
	}

	HandlerThread mApp;
	Handler mAppLogicBase;

	Handler mMainHandler;

	private void initHandler() {
		mApp = new HandlerThread("@@BackGroundThread");
		mApp.start();
		mAppLogicBase = new Handler(mApp.getLooper());
		mMainHandler = new Handler(getMainLooper());
	}
}
