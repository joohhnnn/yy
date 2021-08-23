package com.txznet.comm.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by ASUS User on 2017/11/7.
 */
public class ScaleImageView extends ImageView {
    public static final int MODE_NONE = 0;
    public static final int MODE_AUTO_HEIGHT = 1;
    public static final int MODE_AUTO_WIDTH = 2;

    private int mMode = MODE_NONE;
    private float mScale = 1f;

    public void setMode(int mMode) {
        this.mMode = mMode;
    }

    public void setScale(float mScale) {
        this.mScale = mScale;
    }

    public ScaleImageView(Context context) {
        super(context);
    }

    public ScaleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		if (mMode == MODE_AUTO_HEIGHT) {
//			int widthMeasure = MeasureSpec.getSize(widthMeasureSpec);
//			int heightMeasure = (int) (widthMeasure * mScale);
//			heightMeasureSpec =  MeasureSpec.makeMeasureSpec(heightMeasure, MeasureSpec.EXACTLY);
//
//		} else if (mMode == MODE_AUTO_WIDTH) {
//			int heightMeasure = MeasureSpec.getSize(heightMeasureSpec);
//			int widthMeasure = (int) (heightMeasure * mScale);
//			widthMeasureSpec =  MeasureSpec.makeMeasureSpec(widthMeasure, MeasureSpec.EXACTLY);
//		}
//		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
//	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (getLayoutParams() != null) {
			if (getLayoutParams() instanceof LinearLayout.LayoutParams) {
				((LinearLayout.LayoutParams) getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;
			}
			else if (getLayoutParams() instanceof FrameLayout.LayoutParams) {
				((FrameLayout.LayoutParams) getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;
			}
			else if (getLayoutParams() instanceof RelativeLayout.LayoutParams) {
				((RelativeLayout.LayoutParams) getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
			}
		}
	}

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        if (mMode == MODE_AUTO_HEIGHT) {
            int perfWSize = wSize;
            int perfHSize = (int) (wSize * mScale);
            if (perfHSize > hSize) { // 期望值超出测量范围
                float scale = perfHSize * 1f / hSize;
                perfWSize /= scale;
                perfHSize = hSize;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(perfWSize, MeasureSpec.EXACTLY);
            }
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(perfHSize, MeasureSpec.EXACTLY);
        } else if (mMode == MODE_AUTO_WIDTH) {
            int perfWSize = (int) (hSize * mScale);
            int perfHSize = hSize;
            if (perfWSize > wSize) {
                float scale = perfWSize * 1f / wSize;
                perfWSize = wSize;
                perfHSize /= scale;
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(perfHSize, MeasureSpec.EXACTLY);
            }
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(perfWSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
