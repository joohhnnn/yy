package com.txznet.cldfm.ui.widget.wheel;

import com.txznet.cldfm.R;
import com.txznet.cldfm.ui.widget.wheel.adapter.ArrayWheelAdapter;
import com.txznet.comm.remote.util.LogUtil;

import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * 日期选择滚轮组件，支持时分的选择，可以自由定制
 * 
 * @see WheelDatePicker#WheelDatePicker(Context, boolean)
 */

public class WheelDatePicker extends FrameLayout {

	private static final int MIN_BASE_FREQ = 84;
	private static final int MAX_BASE_FREQ = 108;

	// private static final int LEFT_MAX_VALUE = 30;
	// private static final int LEFT_MAX_VALUE = 8;

	private static final int RIGHT_COUNT = 10;

	private int mRightDegress;
	private int mLeftDegress;

	private int mTextViewSize;
	private int mTextViewHeight;

	// 小数点左侧的字符数组
	private String[] mLeftTextArray = new String[25];
	// private String[] mLeftTextArray = new String[9];
	private String[] mRightTextArray = new String[RIGHT_COUNT];

	private WheelView mRightDegressPickerWv;
	private WheelView mLeftDegressPickerWv;

	private Context mContext;

	private OnDateChangedListener mOnDateChangedListener;

	private WheelScrollListener mWheelScrollListener;

	/**
	 * 数值改变监听器
	 */
	public interface OnDateChangedListener {

		void onDateChanged(WheelDatePicker view, int leftDegress, int rightDegress);
	}

	public OnDateChangedListener getOnDateChangedListener() {
		return this.mOnDateChangedListener;
	}

	public void setOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
		this.mOnDateChangedListener = onDateChangedListener;
	}

	public void setOnWheelScrollListener(WheelScrollListener listener) {
		this.mWheelScrollListener = listener;
	}

	public WheelDatePicker(Context context) {
		super(context);
		initWidget(context);
	}

	public WheelDatePicker(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WheelDatePicker(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initWidget(context);
	}

	/** 初始化滚轮组建 */
	private void initWidget(Context context) {
		mContext = context;

		mTextViewSize = 54;
		mTextViewHeight = (int) getResources().getDimension(R.dimen.y60);

		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.widget_date_picker, this, true);

		mLeftDegressPickerWv = (WheelView) findViewById(R.id.left_degress_wv);
		mRightDegressPickerWv = (WheelView) findViewById(R.id.right_degress_wv);

		mRightDegressPickerWv.setCyclic(true);
		mLeftDegressPickerWv.setCyclic(false);

		int leftAndRightPadding = 10;
		mLeftDegressPickerWv.setFixWidth(120);
		mLeftDegressPickerWv.setContentLeftAndRightPadding(leftAndRightPadding);
		mRightDegressPickerWv.setContentLeftAndRightPadding(leftAndRightPadding);

		// int baseLeftDegress = 78;
		// int baseLeftDegress = 100;
		int currentMonth = 2;

		for (int i = 0; i < 25; i++) {
			if (i + MIN_BASE_FREQ < 100) {
				mLeftTextArray[i] = "0" + (i + MIN_BASE_FREQ);
			} else {
				mLeftTextArray[i] = "" + (i + MIN_BASE_FREQ);
			}
		}

		for (int i = 0; i < RIGHT_COUNT; i++) {
			mRightTextArray[i] = String.format("%02d", i * RIGHT_COUNT);
		}

		mLeftDegressPickerWv.setViewAdapter(new StringArrayAdapter(mContext, mLeftTextArray, 4));
		mRightDegressPickerWv.setViewAdapter(new StringArrayAdapter(mContext, mRightTextArray, currentMonth));
		setOnListener();
	}

	private void setOnListener() {
		mLeftDegressPickerWv.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {

				// newValue是索引值
				mLeftDegress = newValue;
				if (mLeftDegress == ((MAX_BASE_FREQ - MIN_BASE_FREQ) + 1)) {
					mRightDegress = 0;
				}

				if (mOnDateChangedListener != null) {
					mOnDateChangedListener.onDateChanged(WheelDatePicker.this, mLeftDegress, mRightDegress);
				}
				updateWheelViews();
			}
		});

		mRightDegressPickerWv.addChangingListener(new OnWheelChangedListener() {
			@Override
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				// if (!mIsMonthWheelViewScrolling) {
				// newValue是索引值
				mRightDegress = newValue;

				if (oldValue == 9 && newValue == 0) {
					if (mLeftDegress < ((MAX_BASE_FREQ - MIN_BASE_FREQ) + 1)) {
						mLeftDegress++;
					}
				} else if (oldValue == 0 && newValue == 9) {
					if (mLeftDegress > 0) {
						mLeftDegress--;
					}
				}

				if (mOnDateChangedListener != null) {
					mOnDateChangedListener.onDateChanged(WheelDatePicker.this, mLeftDegress, mRightDegress);
				}

				LogUtil.logi("mleft is:" + mLeftDegress + ",mRight is:" + mRightDegress);
				updateWheelViews();
			}
		});
	}

	public void init(int leftDegress, int rightDegress, OnDateChangedListener onDateChangedListener) {
		mLeftDegress = leftDegress;
		mRightDegress = rightDegress;
		mOnDateChangedListener = onDateChangedListener;
		updateWheelViews();
	}

	// 初始化的值
	public void setInitLeftWheelValue(int startValue) {
		mLeftDegressPickerWv.setViewAdapter(new StringArrayAdapter(mContext, mLeftTextArray, startValue));
	}

	private void updateWheelViews() {
		mLeftDegressPickerWv.setCurrentItem(mLeftDegress);
		mRightDegressPickerWv.setCurrentItem(mRightDegress);
		invokeOnWheelScrollListener();
	}

	private void invokeOnWheelScrollListener() {
		if (mWheelScrollListener != null) {
			mWheelScrollListener.onScrollStatus(true);
		}
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();

		return new SavedState(superState, mLeftDegress, mRightDegress);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		SavedState ss = (SavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());
		mLeftDegress = ss.getLeftDegress();
		mRightDegress = ss.getRightDegress();
	}

	private static class SavedState extends BaseSavedState {

		private final int mLeftDegress;
		private final int mRightDegress;

		private SavedState(Parcelable superState, int year, int month) {
			super(superState);
			mLeftDegress = year;
			mRightDegress = month;
		}

		private SavedState(Parcel in) {
			super(in);
			mLeftDegress = in.readInt();
			mRightDegress = in.readInt();
		}

		public int getLeftDegress() {
			return mLeftDegress;
		}

		public int getRightDegress() {
			return mRightDegress;
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(mLeftDegress);
			dest.writeInt(mRightDegress);
		}

		@SuppressWarnings("unused")
		public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {

			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}

	private class StringArrayAdapter extends ArrayWheelAdapter<String> {
		int currentItem;
		int currentValue;

		public StringArrayAdapter(Context context, String[] items, int current) {
			super(context, items);
			this.currentValue = current;
		}

		@Override
		protected void configureTextView(TextView textView) {
			super.configureTextView(textView);
			if (currentItem == currentValue) {
				textView.setTextColor(Color.WHITE);
			} else {
				textView.setTextColor(Color.WHITE);
			}

			textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextViewSize);
			android.widget.LinearLayout.LayoutParams lp = new android.widget.LinearLayout.LayoutParams(
					android.widget.LinearLayout.LayoutParams.MATCH_PARENT, mTextViewHeight);
			lp.gravity = Gravity.CENTER_HORIZONTAL;
			textView.setLayoutParams(lp);

			textView.setLines(1);
			textView.setGravity(Gravity.CENTER);
		}

		@Override
		public View getItem(int index, View cachedView, ViewGroup parent) {
			currentItem = index;
			return super.getItem(index, cachedView, parent);
		}
	}

	public interface WheelScrollListener {
		public void onScrollStatus(boolean isScroll);
	}
}