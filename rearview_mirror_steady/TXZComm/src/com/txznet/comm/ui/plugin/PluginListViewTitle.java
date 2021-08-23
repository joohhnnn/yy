package com.txznet.comm.ui.plugin;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.data.ListViewData.TitleInfo;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;
import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public abstract class PluginListViewTitle {

	protected int mCurPage;
	protected int mMaxPage;
	protected String mPrefix;
	protected String mTitlefix;
	protected String mAftfix;

	protected float tvPreFixSize;
	protected int tvPreFixColor;
	protected float tvAftFixSize;
	protected int tvAftFixColor;
	protected float tvTitleSize;
	protected int tvTitleColor;
	protected float tvPrePagerSize;
	protected int tvPrePagerColor;
	protected float tvCurPagerSize;
	protected int tvCurPagerColor;
	protected float tvNextPagerSize;
	protected int tvNextPagerColor;
	protected int llTitleViewPaddingLeft;
	protected int llTitleViewPaddingRight;
	protected int tvTitlePaddingLeft;
	protected int tvTitlePaddingRight;
	protected int tvTitleMarginRight;
	protected int tvTitleMarginLeft;
	protected int tvPrePagerMarginRight;
	protected int tvNextPagerMarginLeft;
	protected int llTitleHeight;

	// public ViewAdapter getView(ViewData data) {
	// if (data instanceof ListViewData) {
	// initTitleData(((ListViewData)data).mTitleInfo);
	// }
	// ViewAdapter adapter = new ViewAdapter();
	// adapter.view = createTitleView();
	// adapter.type = ViewData.TYPE_LIST_TITLE_VIEW;
	// return adapter;
	// }

	public void init() {
		tvPreFixSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_INTRO_SIZE1);
		tvPreFixColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_INTRO_COLOR1);
		tvAftFixSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_INTRO_SIZE1);
		tvAftFixColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_INTRO_COLOR1);
		tvTitleSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_INTRO_SIZE2);
		tvTitleColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_INTRO_CLOR2);
		tvPrePagerSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_PAGE_SIZE1);
		tvPrePagerColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_PAGE_COLOR1);
		tvCurPagerSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_PAGE_SIZE1);
		tvCurPagerColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_PAGE_COLOR2);
		tvNextPagerSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_POI_PAGE_SIZE1);
		tvNextPagerColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_POI_PAGE_COLOR2);

		llTitleHeight = (int) LayouUtil.getDimen("y64");
		llTitleViewPaddingLeft = (int) LayouUtil.getDimen("x10");
		llTitleViewPaddingRight = (int) LayouUtil.getDimen("x10");
		tvTitlePaddingLeft = (int) LayouUtil.getDimen("x14");
		tvTitlePaddingRight = (int) LayouUtil.getDimen("x14");
		tvTitleMarginLeft = (int) LayouUtil.getDimen("x5");
		tvTitleMarginRight = (int) LayouUtil.getDimen("x5");
		tvPrePagerMarginRight = (int) LayouUtil.getDimen("x10");
		tvNextPagerMarginLeft = (int) LayouUtil.getDimen("x10");
	}

	private boolean isInited = false;

	@SuppressLint("NewApi")
	protected View createTitleView(PluginListViewData pluginListViewData) {
		if (!isInited) {
			init();
		}
		mTitlefix = pluginListViewData.title;
		mPrefix = pluginListViewData.prefixTitle;
		mAftfix = pluginListViewData.suffixTitle;
		mCurPage = pluginListViewData.curPage;
		mMaxPage = pluginListViewData.maxPage;
		LinearLayout llTitleView = new LinearLayout(GlobalContext.get());
		llTitleView.setOrientation(LinearLayout.HORIZONTAL);
		llTitleView.setGravity(Gravity.CENTER_VERTICAL);
		llTitleView.setPadding(llTitleViewPaddingLeft, 0, llTitleViewPaddingRight, 0);

		LinearLayout llTips = new LinearLayout(GlobalContext.get());
		llTips.setOrientation(LinearLayout.HORIZONTAL);
		llTips.setGravity(Gravity.CENTER_VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,
				LinearLayout.LayoutParams.WRAP_CONTENT, 1);
		llTitleView.addView(llTips, layoutParams);

		TextView tvPreFix = new TextView(GlobalContext.get());
		tvPreFix.setSingleLine(true);
		tvPreFix.setEllipsize(TruncateAt.END);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		llTips.addView(tvPreFix, layoutParams);

		TextView tvTitle = new TextView(GlobalContext.get());
		tvTitle.setClickable(true);
		tvTitle.setVisibility(View.GONE);
		// tvTitle.setBackground(LayouUtil.getDrawable("white_range_layout"));
		tvTitle.setEllipsize(TruncateAt.END);
		tvTitle.setGravity(Gravity.CENTER);
		int maxLength = 8;
		InputFilter[] fArray = new InputFilter[1];
		fArray[0] = new InputFilter.LengthFilter(maxLength);
		tvTitle.setFilters(fArray);
		tvTitle.setPadding(tvTitlePaddingLeft, 0, tvTitlePaddingRight, 0);
		tvTitle.setSingleLine(true);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = tvTitleMarginLeft;
		layoutParams.rightMargin = tvTitleMarginRight;
		llTips.addView(tvTitle, layoutParams);
		if (getOnTitleClickListener() != null) {
			tvTitle.setOnClickListener(getOnTitleClickListener());
		}

		TextView tvAftFix = new TextView(GlobalContext.get());
		tvAftFix.setSingleLine(true);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		llTips.addView(tvAftFix, layoutParams);

		LinearLayout llPager = new LinearLayout(GlobalContext.get());
		llPager.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
		llPager.setOrientation(LinearLayout.HORIZONTAL);
		llPager.setVisibility(View.GONE);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		layoutParams.gravity = Gravity.END;
		llTitleView.addView(llPager, layoutParams);

		TextView tvPrePager = new TextView(GlobalContext.get());
		tvPrePager.setSingleLine();
		tvPrePager.setText("上一页");
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.rightMargin = tvPrePagerMarginRight;
		llPager.addView(tvPrePager, layoutParams);

		TextView tvCurPager = new TextView(GlobalContext.get());
		tvCurPager.setSingleLine();
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		llPager.addView(tvCurPager, layoutParams);

		TextView tvNextPager = new TextView(GlobalContext.get());
		tvNextPager.setSingleLine();
		tvNextPager.setText("下一页");
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.leftMargin = tvNextPagerMarginLeft;
		llPager.addView(tvNextPager, layoutParams);

		TextViewUtil.setTextSize(tvPreFix, tvPreFixSize);
		TextViewUtil.setTextColor(tvPreFix, tvPreFixColor);
		TextViewUtil.setTextSize(tvAftFix, tvAftFixSize);
		TextViewUtil.setTextColor(tvAftFix, tvAftFixColor);
		TextViewUtil.setTextSize(tvTitle, tvTitleSize);
		TextViewUtil.setTextColor(tvTitle, tvTitleColor);
		TextViewUtil.setTextSize(tvPrePager, tvPrePagerSize);
		TextViewUtil.setTextColor(tvPrePager, tvPrePagerColor);
		TextViewUtil.setTextSize(tvCurPager, tvCurPagerSize);
		TextViewUtil.setTextColor(tvCurPager, tvCurPagerColor);
		TextViewUtil.setTextSize(tvNextPager, tvNextPagerSize);
		TextViewUtil.setTextColor(tvNextPager, tvNextPagerColor);

		tvPrePager.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				JSONBuilder jb = new JSONBuilder();
				jb.put("type", 1);
				jb.put("clicktype", 1);
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.page",
						jb.toBytes(), null);

				OnClickListener onPrePageClickListener = getOnPrePageClickListener();
				if (onPrePageClickListener != null) {
					onPrePageClickListener.onClick(v);
				}
			}
		});
		tvNextPager.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				JSONBuilder jb = new JSONBuilder();
				jb.put("type", 1);
				jb.put("clicktype", 2);
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.page",
						jb.toBytes(), null);

				OnClickListener onNextPageClickListener = getOnNextPageClickListener();
				if (onNextPageClickListener != null) {
					onNextPageClickListener.onClick(v);
				}
			}
		});

		if (mMaxPage != 0 && mMaxPage != -1 && mMaxPage > 1) {
			llPager.setVisibility(View.VISIBLE);
			if (mCurPage == 0) {
				TextViewUtil.setTextColor(tvPrePager, tvPrePagerColor);
			} else {
				TextViewUtil.setTextColor(tvPrePager, tvNextPagerColor);
			}
			if (mCurPage == mMaxPage - 1) {
				TextViewUtil.setTextColor(tvNextPager, tvPrePagerColor);
			} else {
				TextViewUtil.setTextColor(tvNextPager, tvNextPagerColor);
			}

			tvCurPager.setText((mCurPage + 1) + "/" + mMaxPage);
		} else {
			llPager.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(mPrefix)) {
			tvPreFix.setText(LanguageConvertor.toLocale(mPrefix));
			tvPreFix.setVisibility(View.VISIBLE);
		}

		if (!TextUtils.isEmpty(mTitlefix)) {
			tvTitle.setText(LanguageConvertor.toLocale(mTitlefix));
			tvTitle.setVisibility(View.VISIBLE);
			Drawable tvTitleDrawableLeft = LayouUtil.getDrawable("icon_edit");
			if (tvTitleDrawableLeft != null) {
				int height = tvTitleDrawableLeft.getIntrinsicHeight();
				float scale = (float) tvTitle.getLineHeight() / (float) height;
				tvTitleDrawableLeft.setBounds(0, 0, (int) (tvTitleDrawableLeft.getIntrinsicWidth() * scale),
						(int) (height * scale));
				tvTitle.setCompoundDrawables(null, null, tvTitleDrawableLeft, null);
			}
		}

		if (!TextUtils.isEmpty(mAftfix)) {
			tvAftFix.setText(LanguageConvertor.toLocale(mAftfix));
			tvAftFix.setVisibility(View.VISIBLE);
		}
		return llTitleView;
	}

	public abstract View.OnClickListener getOnTitleClickListener();

	public abstract View.OnClickListener getOnPrePageClickListener();

	public abstract View.OnClickListener getOnNextPageClickListener();

	public abstract Drawable getBackageDrawable();

	public int getTitleHeight() {
		return llTitleHeight;
	}

}
