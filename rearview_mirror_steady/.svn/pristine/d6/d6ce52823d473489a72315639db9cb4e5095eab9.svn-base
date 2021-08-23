package com.txznet.nav.ui.widget;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.nav.R;

public class CityItemView extends RelativeLayout {

	public static final int MODE_DOWNLOAD = 1;
	public static final int MODE_OFFLINE = 2;
	public static final float MAX_COMPLETE = 100.0f;

	public static final int STATUS_NO_DOWNLOAD = 0;
	public static final int STATUS_DOWNLOADED = 1;
	public static final int STATUS_DOWNLOADING = 2;
	public static final int STATUS_DOWNLOAD_PAUSE = 3;
	public static final int STATUS_UNZIP = 4;

	private View mContentView;

	private LinearLayout mTxtLayout;
	private TextView mNameTv;
	private TextView mSizeTv;
	private LinearLayout mPauseLayout;
	private ImageButton mPauseContinueIb;
	private ImageButton mCancelIb;
	private ImageButton mDownDeleteIb;
	private TextView mStatusTv;
	private ProgressBar mProgressBar;

	private int mode;
	private int status = -1;

	private OnMapListener mOnMapListener;

	public CityItemView(Context context) {
		this(context, null);
	}

	public CityItemView(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public CityItemView(Context context, AttributeSet attr, int defValue) {
		super(context, attr, defValue);
		init();
	}

	private void init() {
		initView();
		setMode(MODE_OFFLINE);
		setStatus(STATUS_NO_DOWNLOAD);
	}

	private void initView() {
		this.removeAllViews();
		if (mContentView == null) {
			mContentView = LayoutInflater.from(getContext()).inflate(
					R.layout.offline_child_layout, null);
			mTxtLayout = (LinearLayout) mContentView
					.findViewById(R.id.txt_layout);
			mNameTv = (TextView) mContentView.findViewById(R.id.name);
			mSizeTv = (TextView) mContentView.findViewById(R.id.size);
			mPauseLayout = (LinearLayout) mContentView
					.findViewById(R.id.pause_cancel_ly);
			mPauseContinueIb = (ImageButton) mContentView
					.findViewById(R.id.pause_ib);
			mCancelIb = (ImageButton) mContentView.findViewById(R.id.cancel_ib);
			mDownDeleteIb = (ImageButton) mContentView
					.findViewById(R.id.download_delete_ib);
			mStatusTv = (TextView) mContentView
					.findViewById(R.id.download_status_tv);
			mProgressBar = (ProgressBar) mContentView
					.findViewById(R.id.my_progress);
		}

		addView(mContentView);
	}

	public void setName(String name) {
		mNameTv.setText(name);
	}

	public void setSize(String size) {
		mSizeTv.setText(size);
	}

	public void setSize(double size) {
		String t = String.format("%.2f", size / (1024 * 1024f));
		mSizeTv.setText(t + "MB");
		if (status == STATUS_DOWNLOADED && mode == MODE_DOWNLOAD) {
			mSizeTv.append("(已下载)");
		}
	}

	public void setUnzip(int zip) {
		String txt = "正在解压" + zip + "%";
		mStatusTv.setText(txt);
	}

	public void setCompleteCode(float code) {
		mProgressBar.setProgress((int) code);
	}

	public void setStatus(int status) {
		if (this.status == status) {
			return;
		}

		this.status = status;
		switch (mode) {
		case MODE_OFFLINE:
			switch (status) {
			case STATUS_NO_DOWNLOAD:
				mSizeTv.setTextColor(Color.parseColor("#6a7180"));
				mDownDeleteIb.setVisibility(VISIBLE);
				mDownDeleteIb.setImageResource(R.drawable.offline_map_download);
				mDownDeleteIb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 开始下载
						if (mOnMapListener != null) {
							mOnMapListener.beginDownload();
						}
					}
				});
				mPauseLayout.setVisibility(GONE);
				mStatusTv.setVisibility(GONE);
				mProgressBar.setVisibility(INVISIBLE);
				break;

			case STATUS_DOWNLOADED:
				mSizeTv.setTextColor(Color.parseColor("#6a7180"));
				mStatusTv.setVisibility(VISIBLE);
				mStatusTv.setTextColor(Color.parseColor("#6a7180"));
				mStatusTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 24);
				mStatusTv.setText("已下载");
				mPauseLayout.setVisibility(GONE);
				mDownDeleteIb.setVisibility(GONE);
				mProgressBar.setVisibility(INVISIBLE);
				break;

			case STATUS_DOWNLOADING:
				mSizeTv.setVisibility(VISIBLE);
				mSizeTv.setTextColor(Color.parseColor("#34bfff"));
				mPauseLayout.setVisibility(VISIBLE);
				mPauseContinueIb.setImageResource(R.drawable.offline_map_pause);
				mPauseContinueIb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 暂停下载
						if (mOnMapListener != null) {
							mOnMapListener.pauseDownload();
						}
					}
				});
				mCancelIb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 取消下载
						if (mOnMapListener != null) {
							mOnMapListener.cancelDownload();
						}
					}
				});
				mDownDeleteIb.setVisibility(GONE);
				mStatusTv.setVisibility(GONE);
				mProgressBar.setVisibility(VISIBLE);
				break;

			case STATUS_DOWNLOAD_PAUSE:
				mSizeTv.setTextColor(Color.parseColor("#34bfff"));
				mPauseLayout.setVisibility(VISIBLE);
				mPauseContinueIb.setImageResource(R.drawable.offline_map_goon);
				mPauseContinueIb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 继续下载
						if (mOnMapListener != null) {
							mOnMapListener.continueDownload();
						}
					}
				});
				mCancelIb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 取消下载
						if (mOnMapListener != null) {
							mOnMapListener.cancelDownload();
						}
					}
				});
				mDownDeleteIb.setVisibility(GONE);
				mStatusTv.setVisibility(GONE);
				mProgressBar.setVisibility(VISIBLE);
				break;

			case STATUS_UNZIP:
				mSizeTv.setTextColor(Color.parseColor("#6a7180"));
				mStatusTv.setVisibility(View.VISIBLE);
				mStatusTv.setTextColor(Color.parseColor("#34bfff"));
				mStatusTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 30);
				mPauseLayout.setVisibility(GONE);
				mDownDeleteIb.setVisibility(GONE);
				mProgressBar.setVisibility(VISIBLE);
				break;
			}
			break;

		case MODE_DOWNLOAD:
			switch (status) {

			case STATUS_DOWNLOADED:
				mSizeTv.setTextColor(Color.parseColor("#6a7180"));
				mSizeTv.append("(已下载)");
				mDownDeleteIb.setVisibility(VISIBLE);
				mDownDeleteIb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 删除操作
						if (mOnMapListener != null) {
							mOnMapListener.deleteDownload();
						}
					}
				});
				mDownDeleteIb.setVisibility(GONE);
				mPauseLayout.setVisibility(GONE);
				mStatusTv.setVisibility(GONE);
				mProgressBar.setVisibility(INVISIBLE);
				break;

			case STATUS_DOWNLOADING:
				mSizeTv.setTextColor(Color.parseColor("#34bfff"));
				mPauseLayout.setVisibility(VISIBLE);
				mPauseContinueIb.setImageResource(R.drawable.offline_map_pause);
				mPauseContinueIb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 暂停下载
						if (mOnMapListener != null) {
							mOnMapListener.pauseDownload();
						}
					}
				});
				mCancelIb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 取消下载
						if (mOnMapListener != null) {
							mOnMapListener.cancelDownload();
						}
					}
				});
				mDownDeleteIb.setVisibility(GONE);
				mStatusTv.setVisibility(GONE);
				mProgressBar.setVisibility(VISIBLE);
				break;

			case STATUS_DOWNLOAD_PAUSE:
				mSizeTv.setTextColor(Color.parseColor("#34bfff"));
				mPauseLayout.setVisibility(VISIBLE);
				mPauseContinueIb.setImageResource(R.drawable.offline_map_goon);
				mPauseContinueIb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 继续下载
						if (mOnMapListener != null) {
							mOnMapListener.continueDownload();
						}
					}
				});
				mCancelIb.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 取消下载
						if (mOnMapListener != null) {
							mOnMapListener.cancelDownload();
						}
					}
				});
				mDownDeleteIb.setVisibility(GONE);
				mStatusTv.setVisibility(GONE);
				mProgressBar.setVisibility(VISIBLE);
				break;

			case STATUS_UNZIP:
				mSizeTv.setTextColor(Color.parseColor("#6a7180"));
				mStatusTv.setVisibility(VISIBLE);
				mStatusTv.setTextColor(Color.parseColor("#34bfff"));
				mStatusTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 30);
				mPauseLayout.setVisibility(GONE);
				mDownDeleteIb.setVisibility(GONE);
				mProgressBar.setVisibility(VISIBLE);
				break;
			}
			break;
		}
	}

	public void setMode(int mode) {
		if (this.mode == mode) {
			return;
		}
		this.mode = mode;
		switch (mode) {
		case MODE_DOWNLOAD:
			setBackgroundColor(Color.parseColor("#32373b"));
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mTxtLayout
					.getLayoutParams();
			lp.leftMargin = 30;
			mTxtLayout.setLayoutParams(lp);
			break;

		case MODE_OFFLINE:
			setBackgroundColor(Color.parseColor("#42474a"));
			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mTxtLayout
					.getLayoutParams();
			params.leftMargin = 90;
			mTxtLayout.setLayoutParams(params);
			break;
		}

		setStatus(status);
	}

	// 刷新
	public void refreshView() {
		setMode(mode);
		setStatus(status);
	}

	public int getMode() {
		return mode;
	}

	public int getStatus() {
		return status;
	}

	public void setOnMapListener(OnMapListener onMapListener) {
		mOnMapListener = onMapListener;
	}

	public interface OnMapListener {
		public void beginDownload();

		public void pauseDownload();

		public void cancelDownload();

		public void continueDownload();

		public void deleteDownload();
	}
}
