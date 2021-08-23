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
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ReminderListViewData;
import com.txznet.comm.ui.viewfactory.data.ReminderListViewData.ReminderItemBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IReminderListView;
import com.txznet.comm.util.TextViewUtil;


public class DefaultReminderListView extends IReminderListView {
	private static DefaultReminderListView instance = new DefaultReminderListView();

	private List<View> mItemViews; 
	
	private int tvNumWidth;
	private int tvNumHeight;
	private int tvNumMarginLeft;
	private int dividerHeight;
	
	public static DefaultReminderListView getInstance() {
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
	public ViewFactory.ViewAdapter getView(ViewData data) {
		ReminderListViewData reminderListViewData = (ReminderListViewData) data;
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(reminderListViewData);
		
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
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
		for (int i = 0; i < reminderListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			View itemView = createItemView(i,reminderListViewData.getData().get(i),i != ConfigUtil.getVisbileCount() - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = llLayout;
		viewAdapter.isListView = true;
		viewAdapter.object = DefaultReminderListView.getInstance();
		return viewAdapter;
	}

	@SuppressLint("NewApi")
	private View createItemView(int position, ReminderItemBean reminderBean,boolean showDivider){
		RippleView itemView = new RippleView(GlobalContext.get());
		itemView.setTag(position);
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		itemView.setMinimumHeight((int) LayouUtil.getDimen("m100"));
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
//		layoutParams.topMargin = flContentMarginTop;
//		layoutParams.bottomMargin = flContentMarginBottom;
		itemView.addView(flContent,layoutParams);
		
		RelativeLayout rlItem = new RelativeLayout(GlobalContext.get());
		FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		flContent.addView(rlItem, mLayoutParams);
		rlItem.setMinimumHeight((int) LayouUtil.getDimen("m80"));
		
		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setId(ViewUtils.generateViewId());
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		RelativeLayout.LayoutParams mRLayoutParams = new RelativeLayout.LayoutParams(tvNumWidth,tvNumHeight);
		mRLayoutParams.leftMargin = tvNumMarginLeft;
		mRLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rlItem.addView(tvNum, mRLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setTextColor(Color.WHITE);
		tvContent.setTextSize(LayouUtil.getDimen("m26"));
		tvContent.setSingleLine();
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m16");
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m19");
		mRLayoutParams.addRule(RelativeLayout.RIGHT_OF, tvNum.getId());
		rlItem.addView(tvContent, mRLayoutParams);
		
		ImageView ivTimeIcon = new ImageView(GlobalContext.get());
		ivTimeIcon.setBackground(LayouUtil.getDrawable("reminder_time_icon"));
		ivTimeIcon.setScaleType(ScaleType.CENTER_CROP);
		ivTimeIcon.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams((int)LayouUtil.getDimen("m20"), (int)LayouUtil.getDimen("m24"));
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m5");
		mRLayoutParams.addRule(RelativeLayout.ALIGN_LEFT, tvContent.getId());
		mRLayoutParams.addRule(RelativeLayout.BELOW, tvContent.getId());
		rlItem.addView(ivTimeIcon, mRLayoutParams);
		
		TextView tvTime = new TextView(GlobalContext.get());
		tvTime.setTextColor(Color.WHITE);
		tvTime.setTextSize(LayouUtil.getDimen("m20"));
		tvContent.setSingleLine();
		tvContent.setEllipsize(TruncateAt.END);
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m12");
		mRLayoutParams.addRule(RelativeLayout.ALIGN_BASELINE, ivTimeIcon.getId());
		mRLayoutParams.addRule(RelativeLayout.BELOW, tvContent.getId());
		mRLayoutParams.addRule(RelativeLayout.RIGHT_OF, ivTimeIcon.getId());
		rlItem.addView(tvTime, mRLayoutParams);
		
		TextView tvFullTime = new TextView(GlobalContext.get()); 
		tvFullTime.setId(ViewUtils.generateViewId());
		tvFullTime.setVisibility(View.INVISIBLE);
		tvFullTime.setText("2017/12/19 12:00");
		tvFullTime.setTextSize(LayouUtil.getDimen("m20"));
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, (int) LayouUtil.getDimen("m1"));
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m12");
		mRLayoutParams.addRule(RelativeLayout.BELOW, tvContent.getId());
		mRLayoutParams.addRule(RelativeLayout.RIGHT_OF, ivTimeIcon.getId());
		rlItem.addView(tvFullTime, mRLayoutParams);
		
		ImageView ivPositionIcon = new ImageView(GlobalContext.get());
		ivPositionIcon.setBackground(LayouUtil.getDrawable("reminder_position_icon"));
		ivPositionIcon.setScaleType(ScaleType.CENTER_CROP);
		ivPositionIcon.setId(ViewUtils.generateViewId());
		mRLayoutParams = new RelativeLayout.LayoutParams((int)LayouUtil.getDimen("m20"), (int)LayouUtil.getDimen("m24"));
		mRLayoutParams.topMargin = (int) LayouUtil.getDimen("m5");
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m27");
		mRLayoutParams.addRule(RelativeLayout.RIGHT_OF, tvFullTime.getId());
		mRLayoutParams.addRule(RelativeLayout.BELOW, tvContent.getId());
		rlItem.addView(ivPositionIcon, mRLayoutParams);
		
		TextView tvPosition = new TextView(GlobalContext.get());
		tvPosition.setTextColor(Color.WHITE);
		tvPosition.setTextSize(LayouUtil.getDimen("m20"));
		tvContent.setSingleLine();
		tvContent.setEllipsize(TruncateAt.END);
		mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		mRLayoutParams.leftMargin = (int) LayouUtil.getDimen("m12");
		mRLayoutParams.addRule(RelativeLayout.ALIGN_BASELINE, ivPositionIcon.getId());
		mRLayoutParams.addRule(RelativeLayout.BELOW, tvContent.getId());
		mRLayoutParams.addRule(RelativeLayout.RIGHT_OF, ivPositionIcon.getId());
		rlItem.addView(tvPosition, mRLayoutParams);
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);
		
		tvNum.setText(String.valueOf(position + 1));
		tvContent.setText(reminderBean.content);
		divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
		
		TextViewUtil.setTextSize(tvNum, ViewConfiger.SIZE_REMINDER_INDEX_SIZE1);
		TextViewUtil.setTextColor(tvNum, ViewConfiger.COLOR_REMINDER_INDEX_COLOR1);
		TextViewUtil.setTextSize(tvContent, ViewConfiger.SIZE_REMINDER_ITEM_SIZE1);
		TextViewUtil.setTextColor(tvContent, ViewConfiger.COLOR_REMINDER_INDEX_COLOR1);
		TextViewUtil.setTextSize(tvTime, ViewConfiger.SIZE_REMINDER_ITEM_SIZE2);
		TextViewUtil.setTextColor(tvTime, ViewConfiger.COLOR_REMINDER_ITEM_COLOR2);
		TextViewUtil.setTextSize(tvFullTime, ViewConfiger.SIZE_REMINDER_ITEM_SIZE2);
		TextViewUtil.setTextSize(tvPosition, ViewConfiger.SIZE_REMINDER_ITEM_SIZE3);
		TextViewUtil.setTextColor(tvPosition, ViewConfiger.COLOR_REMINDER_ITEM_COLOR3);
		
		if(!TextUtils.isEmpty(reminderBean.time)){
			tvTime.setText(reminderBean.time);
			ivTimeIcon.setVisibility(View.VISIBLE);
			tvTime.setVisibility(View.VISIBLE);
		}else{
			tvTime.setVisibility(View.INVISIBLE);
			ivTimeIcon.setVisibility(View.INVISIBLE);
		}
		if(!TextUtils.isEmpty(reminderBean.position)){
			tvPosition.setText(reminderBean.position);
			ivPositionIcon.setVisibility(View.VISIBLE);
			tvPosition.setVisibility(View.VISIBLE);
		}else{
			tvPosition.setVisibility(View.INVISIBLE);
			ivPositionIcon.setVisibility(View.INVISIBLE);
		}
		
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
