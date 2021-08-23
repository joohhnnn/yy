package com.txznet.comm.ui.view;

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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.ui.IKeepClass;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.comm.R;

public class IconTextView extends RelativeLayout implements IKeepClass{

	private ImageView mIconImageView;
	private TextView mTitleTextView;
	private TextView mHeadTextView;

	private int mNorTextColor;
	private int mSelTextColor;

	private String mTitleText;
	private String mHeadText;

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
		mNorTextColor =  Color.parseColor("#adb6cc");
		mSelTextColor = Color.WHITE;
		mTextSize = 24;
		mHeadSize = 24;
	}

	public void init() {

		String mLayoutId = "icon_textview_layout";
		View view = null;
		int tvHeadTopMargin = 0;
		int tvHeadBottomMargin = 0;
		int tvTitleTopMargin = 0;
		int tvTitleBottomMargin = 0;
		int ivIconWidth = 0;
		int ivIconHeight = 0;
		int tvTitleLeftMargin = 0;
		switch (ConfigUtil.getScreenType()) {
		case ConfigUtil.SCREEN_TYPE_NORMAL:
			mLayoutId = "icon_textview_layout_normal";
			break;
		case ConfigUtil.SCREEN_TYPE_LITTLE:
			mLayoutId = "icon_textview_layout";
			break;
		case ConfigUtil.SCREEN_TYPE_LARGE:
			mLayoutId = "icon_textview_layout_large";
			break;
		case ConfigUtil.SCREEN_TYPE_CAR:
			mLayoutId = "icon_textview_layout_car";
			break;
		default:
			break;
		}
		tvHeadTopMargin = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_WEATHER_HEAD_MARGINTOP);
		tvHeadBottomMargin = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_WEATHER_HEAD_MARGINBOTTOM);
		tvTitleTopMargin = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_WEATHER_TITLE_MARGINTOP);
		tvTitleBottomMargin = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_WEATHER_TITLE_MARGINBOTTOM);
		tvTitleLeftMargin = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_WEATHER_TITLE_MARGINLEFT);
		ivIconWidth = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_WEATHER_ICON_WIDTH);
		ivIconHeight = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_WEATHER_ICON_HEIGHT);
		view = LayouUtil.getView(mLayoutId);
		mIconImageView = (ImageView) LayouUtil.findViewByName("itv_icon_iv", view);
		mTitleTextView = (TextView) LayouUtil.findViewByName("itv_title_tv",view);
		mHeadTextView = (TextView)LayouUtil.findViewByName("itv_head_tv",view);
		LinearLayout.LayoutParams layoutParams = (android.widget.LinearLayout.LayoutParams) mHeadTextView.getLayoutParams();
		layoutParams.topMargin = tvHeadTopMargin;
		layoutParams.bottomMargin = tvHeadBottomMargin;
		mHeadTextView.setLayoutParams(layoutParams);
		
		layoutParams = (android.widget.LinearLayout.LayoutParams) mTitleTextView.getLayoutParams();
		layoutParams.topMargin = tvTitleTopMargin;
		layoutParams.bottomMargin = tvTitleBottomMargin;
		layoutParams.leftMargin = tvTitleLeftMargin;
		mTitleTextView.setLayoutParams(layoutParams);
		
		layoutParams = (android.widget.LinearLayout.LayoutParams) mIconImageView.getLayoutParams();
		layoutParams.width = ivIconWidth;
		layoutParams.height = ivIconHeight;
		mIconImageView.setLayoutParams(layoutParams);
		
		if (ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_NORMAL|ConfigUtil.getScreenType() == ConfigUtil.SCREEN_TYPE_LITTLE) {
			View mIconImageViewParent = (View) mIconImageView.getParent();
			layoutParams = (android.widget.LinearLayout.LayoutParams) mIconImageViewParent.getLayoutParams();
			layoutParams.topMargin = (int) LayouUtil.getDimen("y16");
			mIconImageViewParent.setLayoutParams(layoutParams);
		}
		
		
		mTitleTextView.setText(mTitleText);
		mTitleTextView.setTextColor(mNorTextColor);
		mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
		
		mHeadTextView.setText(mHeadText);
		mHeadTextView.setTextColor(mNorTextColor);
		mHeadTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, mHeadSize);

		addView(view, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));

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
		mIconImageView.setImageDrawable(d);
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
