package com.txznet.feedback.ui.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.feedback.R;

/**
 * 一个Tab
 * @author meteorluo
 * @date 2015年3月25日
 * @company txznet
 */
public class IconTextView extends RelativeLayout {

	private TextView mTitleTextView;

	private int mNorBackgroundColor;
	private int mSelBackgroundColor;

	private int mNorTextColor;
	private int mSelTextColor;

	private String mTitleText;

	private Paint mBackgroundPaint;
	private Rect mBackgroundRect;

	public IconTextView(Context context) {
		this(context, null);
	}

	public IconTextView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IconTextView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChangeColorIconWithText);
		int n = a.getIndexCount();
		for (int i = 0; i < n; i++) {
			int attr = a.getIndex(i);
			switch (attr) {

				case R.styleable.ChangeColorIconWithText_color_nor:
					mNorTextColor = a.getColor(attr, Color.parseColor("#adb6cc"));
					break;

				case R.styleable.ChangeColorIconWithText_color_sel:
					mSelTextColor = a.getColor(attr, Color.WHITE);
					break;

				case R.styleable.ChangeColorIconWithText_text:
					mTitleText = a.getString(attr);
					break;
				case R.styleable.ChangeColorIconWithText_text_size:
					break;
				case R.styleable.ChangeColorIconWithText_nor_background:
					mNorBackgroundColor = a.getColor(attr, Color.parseColor("#2d3135"));
					break;

				case R.styleable.ChangeColorIconWithText_sel_background:
					mSelBackgroundColor = a.getColor(attr, Color.parseColor("#34bfff"));
					break;
			}
		}
		a.recycle();
		init();
	}

	private void init() {
		setBackgroundColor(mNorBackgroundColor);
		setGravity(Gravity.CENTER);

		mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mBackgroundPaint.setStyle(Style.FILL);
		mBackgroundPaint.setColor(mSelBackgroundColor);

		mBackgroundRect = new Rect();
//		View view = LayoutInflater.from(getContext()).inflate(R.layout.icon_textview_layout, null);
//		mTitleTextView = (TextView) view.findViewById(R.id.itv_title_tv);
//		mTitleTextView.setText(mTitleText);
//		mTitleTextView.setTextColor(mNorTextColor);
		
		mTitleTextView = new TextView(getContext());
		mTitleTextView.setText(mTitleText);
		mTitleTextView.setTextSize(30);
		mTitleTextView.setGravity(Gravity.CENTER);
		mTitleTextView.setTextColor(mNorTextColor);
		LayoutParams lp = (LayoutParams) mTitleTextView.getLayoutParams();
		if(lp == null){
			lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		}
		
		mTitleTextView.setLayoutParams(lp);

		addView(mTitleTextView);

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
		mTitleTextView.setTextColor(mSelTextColor);
		invalidateView();
	}

	public void reset() {
		mTitleTextView.setTextColor(mNorTextColor);
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
	protected void dispatchDraw(Canvas canvas) {
		canvas.drawRect(mBackgroundRect, mBackgroundPaint);
		super.dispatchDraw(canvas);
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
}
