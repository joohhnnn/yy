package com.txznet.record.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.lib.R;

public class IconTextView extends RelativeLayout {

	private ImageView mIconImageView;
	private TextView mTitleTextView;
	private TextView mHeadTextView;

	private int mNorBackgroundColor;
	private int mSelBackgroundColor;

	private Drawable mNorDrawable;
	private Drawable mSelDrawable;

	private int mNorTextColor;
	private int mSelTextColor;

	private String mTitleText;
	private String mHeadText;

	private Paint mBackgroundPaint;
	private Rect mBackgroundRect;

	private Context mContext;
	private float mTextSize = 24;
	private float mHeadSize = 24;

	public IconTextView(Context context) {
		this(context, null);
	}

	public IconTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IconTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mContext = context;
		TypedArray a = context.obtainStyledAttributes(attrs,
				R.styleable.ChangeColorIconWithText);
		int n = a.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = a.getIndex(i);
			if (attr == R.styleable.ChangeColorIconWithText_icon_normal) {
				mNorDrawable = a.getDrawable(attr);

			} else if (attr == R.styleable.ChangeColorIconWithText_icon_while) {
				mSelDrawable = a.getDrawable(attr);

			} else if (attr == R.styleable.ChangeColorIconWithText_color_nor) {
				mNorTextColor = a.getColor(attr, Color.parseColor("#adb6cc"));

			} else if (attr == R.styleable.ChangeColorIconWithText_color_sel) {
				mSelTextColor = a.getColor(attr, Color.WHITE);

			} else if (attr == R.styleable.ChangeColorIconWithText_icon_text) {
				mTitleText = a.getString(attr);
				mHeadText = mTitleText;

			} else if (attr == R.styleable.ChangeColorIconWithText_nor_background) {
				mNorBackgroundColor = a.getColor(attr,
						Color.parseColor("#2d3135"));

			} else if (attr == R.styleable.ChangeColorIconWithText_sel_background) {
				mSelBackgroundColor = a.getColor(attr,
						Color.parseColor("#34bfff"));
			} else if (attr == R.styleable.ChangeColorIconWithText_text_size) {
				mTextSize = a.getDimension(attr, 24);
			}else if (attr == R.styleable.ChangeColorIconWithText_head_size) {
				mHeadSize = a.getDimension(attr, 24);
			}
		}
		a.recycle();
		init();
	}

	private void init() {
		setBackgroundColor(mNorBackgroundColor);

		mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBackgroundPaint.setStyle(Style.FILL);
		mBackgroundPaint.setColor(mSelBackgroundColor);

		mBackgroundRect = new Rect();
		int mLayoutId = R.layout.icon_textview_layout;
		switch (ScreenUtil.getScreenType()) {
		case ScreenUtil.SCREEN_TYPE_LITTLE:
			mLayoutId = R.layout.icon_textview_layout;
			break;
		case ScreenUtil.SCREEN_TYPE_NORMAL:
			mLayoutId = R.layout.icon_textview_layout_normal;
			break;
		case ScreenUtil.SCREEN_TYPE_LARGE:
			mLayoutId = R.layout.icon_textview_layout_large;
			break;
		case ScreenUtil.SCREEN_TYPE_CAR:
			mLayoutId = R.layout.icon_textview_layout_car;
			break;
		default:
			break;
		}
		View view = LayoutInflater.from(getContext()).inflate(
				R.layout.icon_textview_layout, null);
		mIconImageView = (ImageView) view.findViewById(R.id.itv_icon_iv);
		mTitleTextView = (TextView) view.findViewById(R.id.itv_title_tv);
		mHeadTextView = (TextView)view.findViewById(R.id.itv_head_tv);

		mIconImageView.setImageDrawable(mNorDrawable);
		mTitleTextView.setText(mTitleText);
		mTitleTextView.setTextColor(mNorTextColor);
		mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
		
		mHeadTextView.setText(mHeadText);
		mHeadTextView.setTextColor(mNorTextColor);
		mHeadTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHeadSize);

		addView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

		reset();
		setIconAlpha(0);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasure();
	}

	private void setMeasure() {

		mBackgroundRect.left = 0;
		mBackgroundRect.top = 0;
		mBackgroundRect.right = getMeasuredWidth();
		mBackgroundRect.bottom = getMeasuredHeight();
	}

	public void open() {
		setBackgroundColor(mSelBackgroundColor);
		mIconImageView.setImageDrawable(mSelDrawable);
		mTitleTextView.setTextColor(mSelTextColor);
		mHeadTextView.setTextColor(mSelTextColor);
		invalidateView();
	}

	public void reset() {
		setBackgroundColor(mNorBackgroundColor);
		mIconImageView.setImageDrawable(mNorDrawable);
		mTitleTextView.setTextColor(mNorTextColor);
		mHeadTextView.setTextColor(mNorTextColor);
		invalidateView();
	}

	/** 设置透明度参数 **/
	public void setIconAlpha(float alpha) {
		float a = 255 * alpha;
		mBackgroundPaint.setAlpha((int) a);
		invalidateView();
	}

	public void setSel(boolean sel) {
		if (sel) {
			open();
		} else {
			reset();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(mBackgroundRect, mBackgroundPaint);
		super.onDraw(canvas);
	}

	/**
	 * 更新视图
	 */
	private void invalidateView() {
		if (Looper.getMainLooper() == Looper.myLooper()) {
			invalidate();
		} else {
			postInvalidate();
		}
	}

	public void setDrawable(Drawable d) {
		mNorDrawable = d;
		mSelDrawable = d;
		mIconImageView.setImageDrawable(mNorDrawable);
	}

	public void setTitle(String t) {
		mTitleText = t;
		mTitleTextView.setText(mTitleText);
	}
	public void setHead(String t) {
		mHeadText = t;
		mHeadTextView.setText(mHeadText);
	}
	
	public void setTitleSize(float size) {
		mTextSize = size;
		TextViewUtil.setTextSize(mTitleTextView, mTextSize);
	}
	
	public void setHeadSize(float size) {
		mHeadSize = size;
		TextViewUtil.setTextSize(mHeadTextView, mHeadSize);
	}
	
	public void setTitleColor(int color) {
		mTitleTextView.setTextColor(color);
	}
	
	public void setHeadColor(int color) {
		mHeadTextView.setTextColor(color);
	}
}
