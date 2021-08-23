package com.txznet.music.widget;

import com.txznet.music.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.ViewConfiguration;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ButtonImageText extends LinearLayout {

	private ImageView mImageView;
	private TextView mTextView;
	private String mText;
	private ColorStateList mTextColorStateList;
	private int mDrawablePadding = 0;
	private float mTextSize = 0;
	private int mMiniWidth;
	private int mMiniHeight;
	private int resId;

	final int INVALID_DRAWABLE_PADDING = -9999;

	float offsetX;
	float offsetY;
	long down;

	public ButtonImageText(Context context) {
		this(context, null);
	}

	public ButtonImageText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageView getImage() {
		return mImageView;
	}

	@SuppressLint("NewApi")
	public ButtonImageText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
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

		resId = typedArray.getInteger(R.styleable.ButtonImageText_drawable, 0);
		typedArray.recycle();

		mImageView = new ImageView(context, attrs, defStyle);
		mImageView.setPadding(0, 0, 0, 0);
		mImageView.setScaleType(ScaleType.CENTER);
		if (resId != 0) {
			mImageView.setImageResource(resId);
		}

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

	/**
	 * 设置文字
	 * 
	 * @param text
	 */
	public void setText(CharSequence text) {
		boolean isAddView = false;
		if (mText == null)
			isAddView = true;
		mText = text.toString();
		mTextView.setText(mText);
		if (isAddView)
			addView(mTextView);
	}

	public void setText(int resId) {
		this.setText(getContext().getResources().getString(resId));
	}

	public void setTextSize(float size) {
		mTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
	}

	public void setTextColor(ColorStateList colors) {
		mTextView.setTextColor(colors);
	}

	public void setTextColor(int color) {
		mTextView.setTextColor(color);
	}

	/**
	 * 设置一行最多多少个字，多了拿省略号代替
	 * 
	 * @param count
	 */
	public void setMaxText(int count) {
		mTextView.setEms(count);
		mTextView.setSingleLine(); // 导致viewpager不能滑动
		mTextView.setEllipsize(TruncateAt.END);
	}

	/**
	 * 设置图片
	 * 
	 * @param resId
	 */
	public void setImageResource(int resId) {
		mImageView.setImageResource(resId);
	}

	public void setImageDrawable(Drawable drawable) {
		mImageView.setImageDrawable(drawable);
	}

	public void setImageMiniWidth(int minWidth) {
		mMiniWidth = minWidth;
		mImageView.setMinimumWidth(minWidth);
	}

	public void setImageMiniHeight(int minHeight) {
		mMiniHeight = minHeight;
		mImageView.setMinimumHeight(minHeight);
	}

	public void setImageScaleType(ScaleType scaleType) {
		mImageView.setScaleType(scaleType);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mImageView.setColorFilter(Color.GRAY, PorterDuff.Mode.MULTIPLY);
			offsetX = event.getX();
			offsetY = event.getY();
			down = SystemClock.currentThreadTimeMillis();
			return true;
		case MotionEvent.ACTION_CANCEL:
			mImageView.clearColorFilter();
			break;
		case MotionEvent.ACTION_UP:
			mImageView.clearColorFilter();
			if (mOnClickListener != null
					&& SystemClock.currentThreadTimeMillis() - down < ViewConfiguration
							.getLongPressTimeout()) {
				if (Math.abs(event.getX() - offsetX) < ViewConfiguration.get(
						this.getContext()).getScaledTouchSlop()
						&& Math.abs(event.getY() - offsetY) < ViewConfiguration
								.get(this.getContext()).getScaledTouchSlop()) {
					playSoundEffect(SoundEffectConstants.CLICK);
					mOnClickListener.onClick(this);
				}
			}
			break;
		}

		return super.onTouchEvent(event);
	}

	public void setImageBackgroundResource(int resId) {
		this.resId = resId;
		mImageView.setBackgroundResource(resId);
	}

	private OnClickListener mOnClickListener;

	@Override
	public void setOnClickListener(OnClickListener l) {
		mOnClickListener = l;
		super.setOnClickListener(l);
	}
}