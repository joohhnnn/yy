package com.txznet.launcher.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.launcher.R;

public class ButtonImageText extends LinearLayout {

	private ImageView mImageView;
	private TextView mTextView;
	private String mText;
	private ColorStateList mTextColorStateList;
	private int mDrawablePadding = 0;
	private float mTextSize = 0;
	private int mMiniWidth;
	private int mMiniHeight;

	final int INVALID_DRAWABLE_PADDING = -9999;

	public ButtonImageText(Context context) {
		this(context, null);
	}

	public ButtonImageText(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ButtonImageText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ButtonImageText);
		mText = typedArray.getString(R.styleable.ButtonImageText_text);
		mTextColorStateList = typedArray.getColorStateList(R.styleable.ButtonImageText_textColor);
		mDrawablePadding = typedArray.getDimensionPixelSize(R.styleable.ButtonImageText_drawablePadding, INVALID_DRAWABLE_PADDING);
		mTextSize = typedArray.getDimension(R.styleable.ButtonImageText_textSize, 0.0F);
		typedArray.recycle();

		mImageView = new ImageView(context, attrs, defStyle);
		mImageView.setPadding(0, 0, 0, 0);
		mImageView.setScaleType(ScaleType.CENTER);

		mTextView = new TextView(context, attrs, defStyle);
		// ????????????
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
	 * ??????????????????????????????
	 * 
	 * @param dp
	 */
	public void setTextImageSpace(int dp) {
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getContext().getResources()
				.getDisplayMetrics());
		mTextView.setPadding(0, (int) px, 0, 0);
	}

	/**
	 * ????????????
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
	 * ?????????????????????????????????????????????????????????
	 * 
	 * @param count
	 */
	public void setMaxText(int count) {
		mTextView.setEms(count);
		mTextView.setSingleLine(); // ??????viewpager????????????
		mTextView.setEllipsize(TruncateAt.END);
	}

	/**
	 * ????????????
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

	/**
	 * ??????????????????
	 * 
	 * @param resId
	 */
	public void setImageMaskResource(int resId) {
		// ???????????????????????????
		BitmapDrawable bd = (BitmapDrawable) mImageView.getDrawable();
		Bitmap original = bd.getBitmap();
		Bitmap mBitmap = null;
		if (mMiniWidth != 0 && mMiniHeight != 0)
			mBitmap = Bitmap.createScaledBitmap(original, mMiniWidth, mMiniHeight, true);
		// ?????????????????????
		Bitmap mask = BitmapFactory.decodeResource(getResources(), resId);
		Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Config.ARGB_8888);
		// ????????????????????????????????????
		Canvas mCanvas = new Canvas(result);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		mCanvas.drawBitmap(mBitmap, 0, 0, null);
		mCanvas.drawBitmap(mask, 0, 0, paint);
		paint.setXfermode(null);
		mImageView.setImageBitmap(result);
	}

	public void setImageBackgroundResource(int resId) {
		mImageView.setBackgroundResource(resId);
	}
}