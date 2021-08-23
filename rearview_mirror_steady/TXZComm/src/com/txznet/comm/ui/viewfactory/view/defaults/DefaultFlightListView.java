package com.txznet.comm.ui.viewfactory.view.defaults;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.FlightListViewData;
import com.txznet.comm.ui.viewfactory.data.FlightListViewData.FlightItemBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFlightListView;
import com.txznet.txz.comm.R;


public class DefaultFlightListView extends IFlightListView {
	private static DefaultFlightListView instance = new DefaultFlightListView();

	private List<View> mItemViews;

	private int tvNumWidth;
	private int tvNumHeight;
	private int tvNumMarginLeft;
	private int dividerHeight;

	public static DefaultFlightListView getInstance() {
		return instance;
	}

	@Override
	public void init() {
		super.init();
		tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
		tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
		tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
	}

	@SuppressLint("NewApi")
	@Override
	public ViewAdapter getView(ViewData data) {
		FlightListViewData flightListViewData = (FlightListViewData) data;
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(flightListViewData);
		
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);


		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		llContent.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false) * ConfigUtil.getVisbileCount());
		llLayout.addView(llContent,layoutParams);
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
		for (int i = 0; i < flightListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			View itemView = createItemView(i,flightListViewData.getData().get(i),i != ConfigUtil.getVisbileCount() - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = llLayout;
		viewAdapter.isListView = true;
		viewAdapter.object = DefaultFlightListView.getInstance();
		return viewAdapter;
	}

	@SuppressLint("NewApi")
	private View createItemView(int position, FlightItemBean flightBean,boolean showDivider){
		RippleView itemView = new RippleView(GlobalContext.get());
		itemView.setTag(position);
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		itemView.setMinimumHeight((int) LayouUtil.getDimen("m100"));
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		itemView.addView(flContent,layoutParams);
		
		RelativeLayout rlItem = new RelativeLayout(GlobalContext.get());
		FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(rlItem, mLayoutParams);
		rlItem.setMinimumHeight((int) LayouUtil.getDimen("m80"));
		
		TextView tvAirline = new TextView(GlobalContext.get());
		tvAirline.setTextColor(Color.WHITE);
		tvAirline.setTextSize(LayouUtil.getDimen("m14"));
		tvAirline.setSingleLine();
		tvAirline.setEllipsize(TruncateAt.END);
		tvAirline.setId(ViewUtils.generateViewId());
		RelativeLayout.LayoutParams mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m40");
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m19");
		rlItem.addView(tvAirline, mRLayoutParams);

		TextView tvflightNo = new TextView(GlobalContext.get());
		tvflightNo.setTextColor(Color.WHITE);
		tvflightNo.setTextSize(LayouUtil.getDimen("m14"));
		tvflightNo.setSingleLine();
		tvflightNo.setEllipsize(TruncateAt.END);
		tvflightNo.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m3");
		mRLayoutParams.addRule(RelativeLayout.BELOW, tvAirline.getId());
		mRLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, tvAirline.getId());
		rlItem.addView(tvflightNo, mRLayoutParams);
		
		RelativeLayout rlRoute = new RelativeLayout(GlobalContext.get());
		rlRoute.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m19");
		rlItem.addView(rlRoute, mRLayoutParams);
		
		TextView tvDepartName = new TextView(GlobalContext.get());
		tvDepartName.setTextColor(Color.WHITE);
		tvDepartName.setTextSize(LayouUtil.getDimen("m14"));
		tvDepartName.setSingleLine();
		tvDepartName.setEllipsize(TruncateAt.END);
		tvDepartName.setId(ViewUtils.generateViewId());
		tvDepartName.setGravity(Gravity.RIGHT);
		RelativeLayout.LayoutParams mLLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		rlRoute.addView(tvDepartName, mLLayoutParams);
		
		ImageView view = new ImageView(GlobalContext.get());
		view.setId(ViewUtils.generateViewId());
		view.setImageResource(R.drawable.arrow_flight_white);
		mLLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.leftMargin = (int) LayouUtil.getDimen("m5");
		mLLayoutParams.rightMargin = (int) LayouUtil.getDimen("m5");
		mLLayoutParams.addRule(RelativeLayout.BELOW, tvDepartName.getId());
		mLLayoutParams.addRule(RelativeLayout.RIGHT_OF, tvDepartName.getId());
		rlRoute.addView(view, mLLayoutParams);
		
		TextView tvDateAdd = new TextView(GlobalContext.get());
		tvDateAdd.setTextColor(Color.WHITE);
		tvDateAdd.setTextSize(LayouUtil.getDimen("m12"));
		tvDateAdd.setGravity(Gravity.CENTER_HORIZONTAL);
		tvDateAdd.setSingleLine();
		tvDateAdd.setEllipsize(TruncateAt.END);
		tvDateAdd.setId(ViewUtils.generateViewId());
		mLLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.ABOVE, view.getId());
		mLLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, view.getId());
		mLLayoutParams.addRule(RelativeLayout.ALIGN_RIGHT, view.getId());
		rlRoute.addView(tvDateAdd, mLLayoutParams);
		
		TextView tvArrivalName = new TextView(GlobalContext.get());
		tvArrivalName.setTextColor(Color.WHITE);
		tvArrivalName.setTextSize(LayouUtil.getDimen("m14"));
		tvArrivalName.setSingleLine();
		tvArrivalName.setEllipsize(TruncateAt.END);
		tvArrivalName.setId(ViewUtils.generateViewId());
		mLLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.RIGHT_OF, view.getId());
		mLLayoutParams.addRule(RelativeLayout.ALIGN_TOP, tvDepartName.getId());
		rlRoute.addView(tvArrivalName, mLLayoutParams);
		
		TextView tvDepartTime = new TextView(GlobalContext.get());
		tvDepartTime.setTextColor(Color.WHITE);
		tvDepartTime.setTextSize(LayouUtil.getDimen("m14"));
		tvDepartTime.setSingleLine();
		tvDepartTime.setId(ViewUtils.generateViewId());
		tvDepartName.setMinWidth((int)LayouUtil.getDimen("m55"));
		mLLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.BELOW, view.getId());
		mLLayoutParams.addRule(RelativeLayout.LEFT_OF, view.getId());
		rlRoute.addView(tvDepartTime, mLLayoutParams);
		
		TextView tvArrivalTime = new TextView(GlobalContext.get());
		tvArrivalTime.setTextColor(Color.WHITE);
		tvArrivalTime.setTextSize(LayouUtil.getDimen("m14"));
		tvArrivalTime.setSingleLine();
		tvArrivalTime.setEllipsize(TruncateAt.END);
		tvArrivalTime.setId(ViewUtils.generateViewId());
		mLLayoutParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.addRule(RelativeLayout.ALIGN_TOP, tvDepartTime.getId());
		mLLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, tvArrivalName.getId());
		rlRoute.addView(tvArrivalTime, mLLayoutParams);
		
		TextView tvCount = new TextView(GlobalContext.get());
		tvCount.setTextColor(Color.WHITE);
		tvCount.setTextSize(LayouUtil.getDimen("m14"));
		tvCount.setSingleLine();
		tvCount.setEllipsize(TruncateAt.END);
		tvCount.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m19");
		mRLayoutParams.rightMargin = (int) LayouUtil.getDimen("m40");
		rlItem.addView(tvCount, mRLayoutParams);
	
		TextView tvDiscount = new TextView(GlobalContext.get());
		tvDiscount.setTextColor(Color.parseColor("#00B9FF"));
		tvDiscount.setTextSize(LayouUtil.getDimen("m14"));
		tvDiscount.setSingleLine();
		tvDiscount.setEllipsize(TruncateAt.END);
		tvDiscount.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m3");
		mRLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, tvCount.getId());
		mRLayoutParams.addRule(RelativeLayout.BELOW, tvCount.getId());
		rlItem.addView(tvDiscount, mRLayoutParams);
		
		TextView tvSeat = new TextView(GlobalContext.get());
		tvSeat.setTextColor(Color.WHITE);
		tvSeat.setTextSize(LayouUtil.getDimen("m14"));
		tvSeat.setSingleLine();
		tvSeat.setEllipsize(TruncateAt.END);
		tvSeat.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.addRule(RelativeLayout.ALIGN_TOP, tvCount.getId());
		mRLayoutParams.addRule(RelativeLayout.LEFT_OF, tvCount.getId());
		mRLayoutParams.rightMargin = (int) LayouUtil.getDimen("m14");
		rlItem.addView(tvSeat, mRLayoutParams);
		
		TextView tvPrice = new TextView(GlobalContext.get());
		tvPrice.setTextColor(Color.parseColor("#00B9FF"));
		tvPrice.setTextSize(LayouUtil.getDimen("m14"));
		tvPrice.setSingleLine();
		tvPrice.setEllipsize(TruncateAt.END);
		tvPrice.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m3");
		mRLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, tvSeat.getId());
		mRLayoutParams.addRule(RelativeLayout.BELOW, tvSeat.getId());
		rlItem.addView(tvPrice, mRLayoutParams);
		
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);

		tvAirline.setText(flightBean.airline);
		tvflightNo.setText(flightBean.flightNo);
		if(!TextUtils.isEmpty(flightBean.departAirportName)){
			tvDepartName.setText(flightBean.departAirportName);
		}else{
			tvDepartName.setText("未知名称");
		}
		if(!TextUtils.isEmpty(flightBean.arrivalAirportName)){
			tvArrivalName.setText(flightBean.arrivalAirportName);
		}else{
			tvArrivalName.setText("未知名称");
		}
		tvDepartTime.setText(flightBean.departTimeHm);
		tvArrivalTime.setText(flightBean.arrivalTimeHm);
		tvCount.setText(flightBean.ticketCount + " 张");
		tvDiscount.setText(flightBean.economyCabinDiscount + "折");
		tvSeat.setText("经济舱");
		tvPrice.setText("¥" + flightBean.economyCabinPrice);
		divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
		tvDateAdd.setText(flightBean.addDate);

		itemView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				RippleView rippleView = (RippleView) v;
				if (hasFocus) {
					rippleView.setBackgroundColor(Color.parseColor("#4AA5FA"));
				} else {
					rippleView.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});
		
		return itemView;
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
}
