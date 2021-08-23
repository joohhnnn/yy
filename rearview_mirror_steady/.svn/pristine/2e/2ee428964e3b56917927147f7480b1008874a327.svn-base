package com.txznet.txz.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.txz.R;

public class ButtonImageText extends LinearLayout {

	private ImageView mImageView;
	private TextView mTextView;
	private String mText;
	private ColorStateList mTextColorStateList;
	private int mDrawablePadding = 0;
	private float mTextSize = 0;

	final int INVALID_DRAWABLE_PADDING = -9999;

	public ButtonImageText(Context context) {
		this(context, null);
	}

	public ButtonImageText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ButtonImageText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.ButtonImageText);
		mText = typedArray.getString(R.styleable.ButtonImageText_text);
		mTextColorStateList = typedArray
				.getColorStateList(R.styleable.ButtonImageText_textColor);
		mDrawablePadding = typedArray.getDimensionPixelSize(
				R.styleable.ButtonImageText_drawablePadding,
				INVALID_DRAWABLE_PADDING);
		mTextSize = typedArray.getDimension(
				R.styleable.ButtonImageText_textSize, 0.0F);
		typedArray.recycle();

		mImageView = new ImageView(context, attrs, defStyle);
		mImageView.setPadding(0, 0, 0, 0);
		mImageView.setScaleType(ScaleType.CENTER);

		mTextView = new TextView(context, attrs, defStyle);
		// 水平居中
		mTextView.setGravity(android.view.Gravity.CENTER_HORIZONTAL);
		mTextView.setPadding(0, 0, 0, 0);
		if (mText != null)
			mTextView.setText(mText);
		if (mTextColorStateList != null)
			mTextView.setTextColor(mTextColorStateList);

		if (mTextSize > 0) {
			mTextView.setTextSize(mTextSize);
		}

		if (INVALID_DRAWABLE_PADDING != mDrawablePadding) {
			mTextView.setPadding(0, mDrawablePadding, 0, 0);
		}

		setClickable(true);
		setFocusable(true);
		setOrientation(LinearLayout.VERTICAL);

		addView(mImageView);
		if (mText != null)
			addView(mTextView);
	}

	/**
	 * 设置图片和文件的间距
	 * 
	 * @param dp
	 */
	public void setTextImageSpace(int dp) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				getContext().getResources().getDisplayMetrics());
		mTextView.setPadding(0, (int) px, 0, 0);
	}
}