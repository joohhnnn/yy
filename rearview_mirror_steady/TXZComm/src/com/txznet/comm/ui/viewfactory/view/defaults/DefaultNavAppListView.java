package com.txznet.comm.ui.viewfactory.view.defaults;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.view.RippleView.RippleType;
import com.txznet.comm.ui.view.RoundImageView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.NavAppListViewData;
import com.txznet.comm.ui.viewfactory.data.NavAppListViewData.NavAppBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.INavAppListView;
import com.txznet.txz.comm.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DefaultNavAppListView extends INavAppListView {

	private static DefaultNavAppListView instance;

	private List<View> mItemViews = new ArrayList<View>();

	public static DefaultNavAppListView getInstance() {
		if (instance == null) {
			synchronized (DefaultNavAppListView.class) {
				if (instance == null) {
					instance = new DefaultNavAppListView();
				}
			}
		}
		return instance;
	}
	
	private int tvNumWidth;
	private int tvNumHeight;
	
	@Override
	public void init() {
		super.init();

		tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
		tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
	}

	@Override
	public void updateProgress(int progress, int selection) {
	}

	@Override
	public void snapPage(boolean next) {
	}

	@Override
	public void updateItemSelect(int selection) {

	}

	@Override
	public void release() {
		super.release();
		if (mItemViews != null) {
			mItemViews.clear();
		}
	}
	
	@Override
	public List<View> getFocusViews() {
		// TODO Auto-generated method stub
		return super.getFocusViews();
	}

	@Override
	public ViewAdapter getView(ViewData data) {
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.isListView = true;
		viewAdapter.object = this;
		viewAdapter.view = genLayoutView(data);
		viewAdapter.type = data.getType();
		return viewAdapter;
	}

	private View genLayoutView(ViewData viewData) {
		NavAppListViewData data = (NavAppListViewData) viewData;
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		
		ViewAdapter titleAdapter = ListTitleView.getInstance().getView(data);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				ListTitleView.getInstance().getTitleHeight());
		lp.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleAdapter.view, lp);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		int itemHeight = ConfigUtil.getDisplayLvItemH(false);
		LayoutParams layoutParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
				itemHeight * ConfigUtil.getVisbileCount());
		llLayout.addView(llContent, layoutParams);
		llContent.setLayoutAnimation(ListViewItemAnim.getAnimationController());
		llContent.setLayoutAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_START);
				}
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_REPEAT);
				}
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (mViewStateListener != null) {
					mViewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_END);
				}
			}
		});

		mItemViews = new ArrayList<View>();
		for (int i = 0; i < data.count; i++) {
			RippleView itemView = new RippleView(GlobalContext.get());
			itemView.setRippleDuration(300);
			itemView.setRippleType(RippleType.RECTANGLE);
			layoutParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, itemHeight);
			createItemView(itemView, i, data.getData().get(i), i != ConfigUtil.getVisbileCount() - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		return llLayout;
	}

	private void createItemView(final RippleView itemView, final int position, NavAppBean poi, boolean showDivider) {
		itemView.setTag(position);
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		
		int y2 = (int) itemView.getContext().getResources().getDimension(R.dimen.y2);
		int x10 = (int) itemView.getContext().getResources().getDimension(R.dimen.x10);
		int x16 = (int) itemView.getContext().getResources().getDimension(R.dimen.x16);
		int y24  = (int) itemView.getContext().getResources().getDimension(R.dimen.y24);
		int y30 = (int) itemView.getContext().getResources().getDimension(R.dimen.y30);
		int y56 = (int) itemView.getContext().getResources().getDimension(R.dimen.y56);
		int y80 = (int) itemView.getContext().getResources().getDimension(R.dimen.y80);
		
		FrameLayout contentFrameLayout = new FrameLayout(itemView.getContext());
		RelativeLayout.LayoutParams frameParams = new RelativeLayout.LayoutParams(
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT,
				android.widget.RelativeLayout.LayoutParams.MATCH_PARENT);
		frameParams.bottomMargin = y2;
		frameParams.topMargin = y2;
		contentFrameLayout.setLayoutParams(frameParams);
		itemView.addView(contentFrameLayout);

		FrameLayout layout = new FrameLayout(itemView.getContext());
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		layout.setLayoutParams(layoutParams);
		contentFrameLayout.addView(layout);

		GradientProgressBar bar = new GradientProgressBar(itemView.getContext());
		layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		bar.setLayoutParams(layoutParams);
		layout.addView(bar);

		LinearLayout linearLayout = new LinearLayout(itemView.getContext());
		linearLayout.setOrientation(LinearLayout.HORIZONTAL);
		linearLayout.setMinimumHeight(y80);
		layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.MATCH_PARENT);
		linearLayout.setLayoutParams(layoutParams);
		layout.addView(linearLayout);

		TextView tvView = new TextView(itemView.getContext());
		tvView.setTextColor(Color.WHITE);
		tvView.setTextSize(y30);
		tvView.setGravity(Gravity.CENTER);
		tvView.setIncludeFontPadding(false);
		// TODO 背景
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(tvNumWidth, tvNumHeight);
		params.gravity = Gravity.CENTER;
		params.leftMargin = x10;
		tvView.setLayoutParams(params);
		linearLayout.addView(tvView);

		RoundImageView roundImageView = new RoundImageView(itemView.getContext());
		// TODO
		params = new LinearLayout.LayoutParams(y56, y56);
		params.leftMargin = x16;
		params.gravity = Gravity.CENTER_VERTICAL;
		roundImageView.setLayoutParams(params);
		linearLayout.addView(roundImageView);

		LinearLayout layout2 = new LinearLayout(itemView.getContext());
		layout2.setOrientation(LinearLayout.HORIZONTAL);
		layout2.setMinimumHeight(y80);
		layout2.setGravity(Gravity.CENTER_VERTICAL);
		params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);
		params.gravity = Gravity.CENTER_VERTICAL;
		params.leftMargin = x16;
		layout2.setLayoutParams(params);
		linearLayout.addView(layout2);

		TextView nameTv = new TextView(itemView.getContext());
		params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.MATCH_PARENT, 1);
		params.gravity = Gravity.CENTER_VERTICAL;
		nameTv.setEms(10);
		nameTv.setEllipsize(TruncateAt.END);
		nameTv.setSingleLine();
		nameTv.setGravity(Gravity.CENTER_VERTICAL);
		nameTv.setLayoutParams(params);
		nameTv.setTextColor(Color.parseColor("#adb6cc"));
		nameTv.setTextSize(y24);
		layout2.addView(nameTv);

		// LinearLayout layout3 = new LinearLayout(itemView.getContext());
		// params = new
		// LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
		// LinearLayout.LayoutParams.MATCH_PARENT);
		// params.rightMargin = 6;
		// layout3.setGravity(Gravity.CENTER_VERTICAL);
		// layout3.setOrientation(LinearLayout.VERTICAL);
		// layout3.setLayoutParams(params);
		// layout2.addView(layout3);
		//
		// TextView rightTop = new TextView(itemView.getContext());
		// params = new
		// LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT);
		// params.setMargins(6, 6, 6, 6);
		// rightTop.setLayoutParams(params);
		// rightTop.setGravity(Gravity.CENTER);
		// rightTop.setSingleLine();
		// layout3.addView(rightTop);

		tvView.setText(String.valueOf(position + 1));
		roundImageView.setImageDrawable(getDrawableByPkn(itemView.getContext(), poi.navPkn));
		nameTv.setText(poi.title);

		if (showDivider) {
			View view = new View(itemView.getContext());
			view.setBackgroundColor(Color.parseColor("#4c4c4c"));
			int dh = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
			RelativeLayout.LayoutParams diviParams = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.MATCH_PARENT, dh);
			diviParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			view.setLayoutParams(diviParams);
			itemView.addView(view);
		}
	}

	private Drawable getDrawableByPkn(Context mContext, String navPkn) {
		PackageManager pm = mContext.getPackageManager();
		try {
			return pm.getApplicationIcon(navPkn);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}