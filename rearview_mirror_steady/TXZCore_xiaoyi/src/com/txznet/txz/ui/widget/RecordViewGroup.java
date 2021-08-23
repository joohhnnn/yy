package com.txznet.txz.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.txznet.record.view.WaveformViewDefImpl;
import com.txznet.txz.R;

public class RecordViewGroup extends LinearLayout {

	public static final int STATE_NOR = 0; // 正常
	public static final int STATE_RECORD_START = 1; // 录音
	public static final int STATE_RECORD_END = 2; // 识别

	private View mVoiceView;
	private WaveformViewDefImpl mWaveformViewDefImpl;
	private FrameLayout mContentLayout;
	private int status = -1;

	public RecordViewGroup(Context context) {
		this(context, null);
	}

	public RecordViewGroup(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RecordViewGroup(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView();
	}

	private void initView() {
		this.removeAllViews();
		if (mContentLayout == null) {
			mContentLayout = (FrameLayout) LayoutInflater.from(getContext())
					.inflate(R.layout.record_view_layout, null);

			this.mVoiceView = mContentLayout
					.findViewById(R.id.record_loading_ly);
			this.mWaveformViewDefImpl = (WaveformViewDefImpl) mContentLayout
					.findViewById(R.id.viewRecord_Wave);
		}

		this.addView(mContentLayout);
		updateRecordVisibleStatus(STATE_NOR);
	}

	public void updateRecordVisibleStatus(int status) {
		if (this.status == status) {
			return;
		}

		this.status = status;
		switch (status) {
		case STATE_RECORD_START:
			this.mWaveformViewDefImpl.setVisibility(VISIBLE);
			this.mWaveformViewDefImpl.setStopAnimation(false);
			this.mVoiceView.setVisibility(GONE);
			break;

		case STATE_RECORD_END:
			this.mWaveformViewDefImpl.setVisibility(GONE);
			this.mVoiceView.setVisibility(VISIBLE);
			this.mWaveformViewDefImpl.setStopAnimation(true);
			break;
		case STATE_NOR:
		default:
			this.mVoiceView.setVisibility(GONE);
			this.mWaveformViewDefImpl.setVisibility(VISIBLE);
			this.mWaveformViewDefImpl.setStopAnimation(true);
			break;
		}
	}
}