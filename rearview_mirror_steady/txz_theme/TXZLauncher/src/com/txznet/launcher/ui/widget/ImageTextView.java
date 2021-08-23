package com.txznet.launcher.ui.widget;

import android.annotation.SuppressLint;
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
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;

public class ImageTextView extends LinearLayout {

	private TXZImageView mImageView;
	private TextView mTextView;
	private String mText;
	private ColorStateList mTextColorStateList;
	private int mDrawablePadding = 0;
	private float mTextSize = 0;
	private int mMiniWidth;
	private int mMiniHeight;

	final int INVALID_DRAWABLE_PADDING = -9999;

	public ImageTextView(Context context) {
		this(context, null);
	}

	public ImageTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ImageTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		TypedArray typedArray = context.obtainStyledAttributes(attrs,
				R.styleable.ButtonImageText);
		mText = typedArray.getString(R.styleable.ButtonImageText_textSize);
		mTextColorStateList = typedArray
				.getColorStateList(R.styleable.ButtonImageText_textSize);
		mDrawablePadding = typedArray.getDimensionPixelSize(
				R.styleable.ButtonImageText_textSize, INVALID_DRAWABLE_PADDING);
		mTextSize = typedArray.getDimension(
				R.styleable.ButtonImageText_textSize, 0.0F);
		typedArray.recycle();

		mImageView = new TXZImageView(context, attrs, defStyle);
		mImageView.setPadding(0, 0, 0, 0);
		mImageView.setScaleType(ScaleType.FIT_CENTER);

		mTextView = new TextView(context, attrs, defStyle);
		// 水平居中
		mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
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

	public void setImageWidthHeight(int width, int height) {
		LayoutParams layoutParams = (LayoutParams) mImageView
				.getLayoutParams();
		layoutParams.width = width;
		layoutParams.height = height;
		layoutParams.gravity = Gravity.CENTER;
		mImageView.setLayoutParams(layoutParams);
	}

	public void setImageMiniWidth(int minWidth) {
		mMiniWidth = minWidth;
		mImageView.setMinimumWidth(minWidth);
	}

	public void setImageMiniHeight(int minHeight) {
		mMiniHeight = minHeight;
		mImageView.setMinimumHeight(minHeight);
	}

	/*
	 * 隐藏该接口 确保该UI控件中ImageView的ScaleType为默认的格式 public void
	 * setImageScaleType(ScaleType scaleType) {
	 * mImageView.setScaleType(scaleType); }
	 */

	/**
	 * 设置遮罩图片
	 * 
	 * @param resId
	 */
	public void setImageMaskResource(int resId) {
		// 获取图片的资源文件
		BitmapDrawable bd = (BitmapDrawable) mImageView.getDrawable();
		Bitmap original = bd.getBitmap();
		Bitmap mBitmap = null;
		if (mMiniWidth != 0 && mMiniHeight != 0)
			mBitmap = Bitmap.createScaledBitmap(original, mMiniWidth,
					mMiniHeight, true);
		// 获取遮罩层图片
		Bitmap mask = BitmapFactory.decodeResource(getResources(), resId);
		Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),
				Config.ARGB_8888);
		// 将遮罩层的图片放到画布中
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

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mImageView.setPressState(true);
			LogUtil.logi("down");
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			mImageView.setPressState(false);
			LogUtil.logi("up");
		} else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
			// 如果motion事件被底层View拦截了，那么目标View会接收到到ACTION_CANCEL事件
			mImageView.setPressState(false);
			LogUtil.logi("cancel");
		}
		return super.onTouchEvent(event);
	}

}
