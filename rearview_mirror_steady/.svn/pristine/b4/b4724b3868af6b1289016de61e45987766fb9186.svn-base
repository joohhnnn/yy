package com.txznet.comm.ui.plugin;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public abstract class PluginCommonListViewItem<T> extends PluginListViewItem<T> {
	protected boolean isInited = false;

	protected int tvNumMarginLeft;
	protected int tvNumWidth;
	protected int tvNumHeight;
	protected int dividerHeight;
	protected float tvNumSize;
	protected int tvNumColor;
	private float tvContentSize;
	private int tvContentColor;
	protected int llDetailMarginLeft;
	protected int flContentMarginTop;
	protected int flContentMarginBottom;

	protected float tvDescSize;
	protected int tvDescColor;
	protected int tvDescMarginRight;

	protected void init() {
		flContentMarginTop = (int) ThemeConfigManager.getY(2);
		flContentMarginBottom = (int) ThemeConfigManager.getY(2);
		tvNumWidth = (int) LayouUtil.getDimen("y44");
		tvNumHeight = (int) LayouUtil.getDimen("y44");
		tvNumMarginLeft = (int) ThemeConfigManager.getX(15);
		llDetailMarginLeft = (int) ThemeConfigManager.getX(15);
		tvDescMarginRight = (int) LayouUtil.getDimen("x4");
		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(2));
		tvNumSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_AUDIO_INDEX_SIZE1);
		tvNumColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_AUDIO_INDEX_COLOR1);
		tvContentSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_AUDIO_ITEM_SIZE1);
		tvContentColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_AUDIO_ITEM_COLOR1);
		tvDescSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_AUDIO_ITEM_SIZE2);
		tvDescColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_AUDIO_ITEM_COLOR2);
	}

	@SuppressLint("NewApi")
	@Override
	public View createItemView(int position, T itemData, boolean showDivider) {
		if (!isInited) {
			init();
			isInited = true;
		}
		RippleView itemView = new RippleView(GlobalContext.get());
		itemView.setTag(position);
		itemView.setOnClickListener(getOnItemClickListener());
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.topMargin = flContentMarginTop;
		layoutParams.bottomMargin = flContentMarginBottom;
		itemView.addView(flContent, layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		flContent.addView(llContent, mFLayoutParams);

		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumWidth, tvNumHeight);
		mLLayoutParams.leftMargin = tvNumMarginLeft;
		mLLayoutParams.gravity = Gravity.CENTER;
		llContent.addView(tvNum, mLLayoutParams);

		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		llDetail.setOrientation(LinearLayout.VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		mLLayoutParams.leftMargin = llDetailMarginLeft;
		llContent.addView(llDetail, mLLayoutParams);

		// TextView tvDesc = new TextView(GlobalContext.get());
		// tvDesc.setSingleLine();
		// tvDesc.setEllipsize(TruncateAt.END);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		// mLLayoutParams.leftMargin = tvDescMarginLeft;
		mLLayoutParams.rightMargin = tvDescMarginRight;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llDetail.addView(createContentItemView(position, itemData), mLLayoutParams);

		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);

		TextViewUtil.setTextSize(tvNum, tvNumSize);
		TextViewUtil.setTextColor(tvNum, tvNumColor);
//		TextViewUtil.setTextSize(tvDesc, tvDescSize);
//		TextViewUtil.setTextColor(tvDesc, tvDescColor);

		tvNum.setText(String.valueOf(position + 1));
//		tvDesc.setText(LanguageConvertor.toLocale((String) itemData));

		divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
		return itemView;
	}

	public abstract View createContentItemView(int position, T itemData);

}
