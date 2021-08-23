package com.txznet.comm.ui.theme.test.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
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
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ReminderListViewData;
import com.txznet.comm.ui.viewfactory.data.ReminderListViewData.ReminderItemBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IReminderListView;
import com.txznet.comm.util.TextViewUtil;


public class ReminderListView extends IReminderListView {
	private static ReminderListView instance = new ReminderListView();

	private List<View> mItemViews; 

	private int dividerHeight;

    private int tvNumSide;    //序号宽高
    private int tvNumHorMargin;    //序号左右边距
    private int tvNumSize;    //序号字体大小
    private int tvNumColor;    //序号字体颜色
    private int tvContentSize;    //内容字体大小
    private int tvContentHeight;    //内容行高
    private int tvContentColor;    //内容字体颜色
    private int ivIconSize;    //提醒图标大小
    private int tvTimeSize;    //时间字体大小
    private int tvTimeHeight;    //时间行高
    private int tvTimeColor;    //时间字体颜色
    private int tvTimeLeftMargin;    //时间左边距


	public static ReminderListView getInstance() {
		return instance;
	}

	@Override
	public void init() {
		super.init();
		dividerHeight = 1;
        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvContentColor =  Color.parseColor(LayouUtil.getString("color_main_title"));
        tvTimeColor = Color.parseColor(LayouUtil.getString("color_vice_title"));

	}

	//切换模式修改布局参数
	public void onUpdateParams(int styleIndex){
		int unit = ViewParamsUtil.unit;
		tvNumSide = 6 * unit;
		tvNumHorMargin = 2 * unit;
		tvNumSize = ViewParamsUtil.h0;
		tvContentSize = ViewParamsUtil.h4;
		tvContentHeight = ViewParamsUtil.h4Height;
		ivIconSize = ViewParamsUtil.h2;
		tvTimeSize = ViewParamsUtil.h6;
		tvTimeHeight = ViewParamsUtil.h6Height;
		tvTimeLeftMargin = unit;
		if (styleIndex != StyleConfig.STYLE_ROBOT_NONE_SCREES && WinLayout.isVertScreen){
			tvContentSize = ViewParamsUtil.h3;
			tvContentHeight = ViewParamsUtil.h3Height;
			ivIconSize = ViewParamsUtil.h3;
			tvTimeSize = ViewParamsUtil.h5;
			tvTimeHeight = ViewParamsUtil.h5Height;
		}
	}

	@SuppressLint("NewApi")
	@Override
	public ViewAdapter getView(ViewData data) {
		ReminderListViewData reminderListViewData = (ReminderListViewData) data;
		WinLayout.getInstance().vTips = reminderListViewData.vTips;
		LogUtil.logd(WinLayout.logTag+ "reminderListViewData.vTips: "+reminderListViewData.vTips);

		View view = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				view = createViewFull(reminderListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				view = createViewHalf(reminderListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = createViewNone(reminderListViewData);
				break;
		}

		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.isListView = true;
		viewAdapter.object = ReminderListView.getInstance();
		return viewAdapter;
	}

	private View createViewFull(ReminderListViewData reminderListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(reminderListViewData,"reminders","提醒事项");

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(llContent,layoutParams);

		mCurPage= reminderListViewData.mTitleInfo.curPage;
		mMaxPage = reminderListViewData.mTitleInfo.maxPage;
		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llContents.addView(llPager,layoutParams);

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
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i,reminderListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		/*if (reminderListViewData.count < 4){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,4 - reminderListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}*/

		return llLayout;
	}

	private View createViewHalf(ReminderListViewData reminderListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(reminderListViewData,"reminders","提醒事项");

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(llContent,layoutParams);

		mCurPage= reminderListViewData.mTitleInfo.curPage;
		mMaxPage = reminderListViewData.mTitleInfo.maxPage;
		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llContents.addView(llPager,layoutParams);

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
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i,reminderListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		/*if (reminderListViewData.count < 4){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,4 - reminderListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}*/

		return llLayout;
	}

	private View createViewNone(ReminderListViewData reminderListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(reminderListViewData,"reminders","提醒事项");

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llLayout.addView(llContents,layoutParams);

		mCurPage= reminderListViewData.mTitleInfo.curPage;
		mMaxPage = reminderListViewData.mTitleInfo.maxPage;
		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		llLayout.addView(llPager,layoutParams);

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContents.addView(titleViewAdapter.view,layoutParams);

		View divider = new View(GlobalContext.get());
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		llContents.addView(divider, layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pagePoiCount * SizeConfig.itemHeight);
		llContents.addView(llContent,layoutParams);

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
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i,reminderListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		/*if (reminderListViewData.count < 3){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,3 - reminderListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}*/

		return llLayout;
	}

	@SuppressLint("NewApi")
	private View createItemView(int position, ReminderItemBean reminderBean,boolean showDivider){
		LogUtil.logd(WinLayout.logTag+ "ReminderItemBean: "+ reminderBean.id+"--"+reminderBean.content+"--"+reminderBean.position+"--"+reminderBean.time);
		RippleView itemView = new RippleView(GlobalContext.get());
		itemView.setTag(position);
		ListTitleView.getInstance().mItemViews = mItemViews;
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		/*itemView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()){
					case MotionEvent.ACTION_UP:
						showSelectItem((int)v.getTag());
						break;
				}
				return false;
			}
		});*/
		/*FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		itemView.addView(flContent,layoutParams);*/
		
		/*RelativeLayout rlItem = new RelativeLayout(GlobalContext.get());
		FrameLayout.LayoutParams mLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		itemView.addView(rlItem, mLayoutParams);*/

        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(llItem, llLayoutParams);
		
		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setId(ViewUtils.generateViewId());
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		llLayoutParams = new LinearLayout.LayoutParams(tvNumSide,tvNumSide);
        llLayoutParams.leftMargin = tvNumHorMargin;
        llLayoutParams.rightMargin = tvNumHorMargin;
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llItem.addView(tvNum, llLayoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        llItem.addView(llContents, llLayoutParams);

        FrameLayout tvLayout = new FrameLayout(GlobalContext.get());
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,0,1);
        llContents.addView(tvLayout,llLayoutParams);

		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setSingleLine();
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setId(ViewUtils.generateViewId());
        FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, tvContentHeight);
        flLayoutParams.gravity = Gravity.BOTTOM;
        tvLayout.addView(tvContent, flLayoutParams);

        LinearLayout llDetail = new LinearLayout(GlobalContext.get());
        llDetail.setOrientation(LinearLayout.HORIZONTAL);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0,1);
        llContents.addView(llDetail, llLayoutParams);

		ImageView ivTimeIcon = new ImageView(GlobalContext.get());
//		ivTimeIcon.setBackground(LayouUtil.getDrawable("reminder_time_icon"));
		ivTimeIcon.setBackground(LayouUtil.getDrawable("reminder_time"));
		ivTimeIcon.setScaleType(ScaleType.CENTER_CROP);
		ivTimeIcon.setId(ViewUtils.generateViewId());
        llLayoutParams = new LinearLayout.LayoutParams(ivIconSize, ivIconSize);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llDetail.addView(ivTimeIcon, llLayoutParams);
		
		TextView tvTime = new TextView(GlobalContext.get());
		tvContent.setSingleLine();
		tvContent.setEllipsize(TruncateAt.END);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, tvTimeHeight);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayoutParams.leftMargin = tvTimeLeftMargin;
        llDetail.addView(tvTime, llLayoutParams);
		
		ImageView ivPositionIcon = new ImageView(GlobalContext.get());
//		ivPositionIcon.setBackground(LayouUtil.getDrawable("reminder_position_icon"));
		ivPositionIcon.setBackground(LayouUtil.getDrawable("reminder_position"));
		ivPositionIcon.setScaleType(ScaleType.CENTER_CROP);
		ivPositionIcon.setId(ViewUtils.generateViewId());
        llLayoutParams = new LinearLayout.LayoutParams(ivIconSize, ivIconSize);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayoutParams.leftMargin = tvTimeLeftMargin;
        llDetail.addView(ivPositionIcon, llLayoutParams);
		
		TextView tvPosition = new TextView(GlobalContext.get());
		tvPosition.setSingleLine();
		tvPosition.setEllipsize(TruncateAt.END);
        llLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, tvTimeHeight);
        llLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayoutParams.leftMargin = tvTimeLeftMargin;
        llDetail.addView(tvPosition, llLayoutParams);
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackground(LayouUtil.getDrawable("line"));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);
		
		tvNum.setText(String.valueOf(position + 1));
		tvContent.setText(reminderBean.content);
		divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
		
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
		
		/*itemView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				RippleView rippleView = (RippleView) v;
				if (hasFocus) {
					rippleView.setBackgroundColor(Color.parseColor("#4AA5FA"));
				} else {
					rippleView.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});*/

		TextViewUtil.setTextSize(tvNum, tvNumSize);
		TextViewUtil.setTextColor(tvNum, tvNumColor);
		TextViewUtil.setTextSize(tvContent, tvContentSize);
		TextViewUtil.setTextColor(tvContent, tvContentColor);
		TextViewUtil.setTextSize(tvTime,tvTimeSize);
		TextViewUtil.setTextColor(tvTime, tvTimeColor);
		//TextViewUtil.setTextSize(tvFullTime, ViewConfiger.SIZE_REMINDER_ITEM_SIZE2);
		TextViewUtil.setTextSize(tvPosition, tvTimeSize);
		TextViewUtil.setTextColor(tvPosition, tvTimeColor);
		
		return itemView;
	}

	@Override
	public void updateProgress(int progress, int selection) {

	}

	@Override
	public void snapPage(boolean next) {

	}

	@Override
	public void updateItemSelect(int index) {
		LogUtil.logd(WinLayout.logTag+ "train updateItemSelect " + index);
		showSelectItem(index);
	}


	private void showSelectItem(int index){
		for (int i = 0;i< mItemViews.size();i++){
			if (i == index){
				mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
			}else {
				mItemViews.get(i).setBackground(null);
			}
		}
	}
}
