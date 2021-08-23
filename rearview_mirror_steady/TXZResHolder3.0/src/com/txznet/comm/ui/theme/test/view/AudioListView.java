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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.SkillfulReminding;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.GradientProgressBar;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.AudioListViewData;
import com.txznet.comm.ui.viewfactory.data.AudioListViewData.AudioBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IAudioListView;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class AudioListView extends IAudioListView {

	private static AudioListView sInstance = new AudioListView();
	
	private List<View> mItemViews;

	private boolean isHasTag;    //当前列表是否有有有声书标签
	
	//字体等参数配置
	private int dividerHeight;

	private ArrayList<GradientProgressBar> progressBars = new ArrayList<GradientProgressBar>(4);

    private int tvNumSide;    //序号宽高
    private int tvNumHorMargin;    //序号左右边距
    private int tvNumSize;    //序号字体大小
    private int tvNumColor;    //序号字体颜色
    private int tvContentSize;    //内容字体大小
    private int tvContentHeight;    //内容行高
    private int tvContentColor;    //内容字体颜色
	private int centerInterval;    //内容到详情的间距
    private int tvDescSize;    //详情字体大小
    private int tvDescHeight;    //详情行高
    private int tvDescColor;    //详情字体颜色
    private int ivTagSide;    //有声书标签大小
    private int ivTagMargin;    //有声书标签边距


	private AudioListView() {
	}

	public static AudioListView getInstance(){
		return sInstance;
	}
	
	@Override
	public void updateProgress(int progress, int selection) {
		/*
		LogUtil.logd("updateProgress " + progress + "," + selection);
		if (progressBars.size() > selection) {
			GradientProgressBar progressBar = progressBars.get(selection);
			if (progress < 0) {
				if (progressBar.getVisibility() == View.VISIBLE) {
					progressBar.setVisibility(View.GONE);
				}
			}else {
				if (progressBar.getVisibility() == View.GONE) {
					progressBar.setVisibility(View.VISIBLE);
				}
				progressBar.setProgress(progress);
			}
		}
		*/
	}

	@Override
	public void release() {
		super.release();
		if (mItemViews != null) {
			mItemViews.clear();
		}
		if (progressBars != null) {
			progressBars.clear();
		}
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		AudioListViewData audioListViewData = (AudioListViewData)data;
		WinLayout.getInstance().vTips = audioListViewData.vTips;

        isHasTag = !audioListViewData.isMusic;
        LogUtil.logd(WinLayout.logTag+ "audioListViewData.isMusic: "+audioListViewData.isMusic+ "audioListViewData.vTips: "+audioListViewData.vTips);
		View view;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				view = createViewFull(audioListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				view = createViewHalf(audioListViewData);
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				view = createViewNone(audioListViewData);
				break;
		}

		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.isListView = true;
		viewAdapter.object = AudioListView.getInstance();
		return viewAdapter;
	}

	//当前列表是否有有声书标签
	private boolean isHasTag(AudioListViewData audioListViewData){
        for (int i = 0;i < audioListViewData.count;i++){
            AudioBean audioBean = audioListViewData.getData().get(i);
            if (audioBean.novelStatus != AudioBean.NOVEL_STATUS_INVALID ||
                    audioBean.paid ||
                    audioBean.lastPlay ||
                    audioBean.latest){
                return true;
            }
        }
        return false;
    }

	private View createViewFull(AudioListViewData audioListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(audioListViewData,"music",isHasTag?"电台":"音乐");
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

		LinearLayout llPager = new PageView(GlobalContext.get(),audioListViewData.mTitleInfo.curPage,audioListViewData.mTitleInfo.maxPage);
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
					mViewStateListener.onAnimateStateChanged(animation, 3);
				}
			}
		});
		progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < audioListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i,audioListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		return llLayout;
	}

	private View createViewHalf(AudioListViewData audioListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(audioListViewData,"music",isHasTag?"电台":"音乐");
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

		LinearLayout llPager = new PageView(GlobalContext.get(),audioListViewData.mTitleInfo.curPage,audioListViewData.mTitleInfo.maxPage);
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
					mViewStateListener.onAnimateStateChanged(animation, 3);
				}
			}
		});
		progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < audioListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHeight);
			View itemView = createItemView(i,audioListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		return llLayout;
	}

	private View createViewNone(AudioListViewData audioListViewData){
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(audioListViewData,"music",isHasTag?"电台":"音乐");
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),audioListViewData.mTitleInfo.curPage,audioListViewData.mTitleInfo.maxPage);
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
					mViewStateListener.onAnimateStateChanged(animation, 3);
				}
			}
		});
		progressBars.clear();
		mItemViews = new ArrayList<View>();
		for (int i = 0; i < audioListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,isHasTag?SizeConfig.itemHeightPro:SizeConfig.itemHeight);
            View itemView;
            if (isHasTag){
                itemView = createItemViewTag(i,audioListViewData.getData().get(i),i != SizeConfig.pageAudioTagCount - 1);
            }else {
                itemView = createItemView(i,audioListViewData.getData().get(i),i != SizeConfig.pageCount - 1);
            }
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}
		return llLayout;
	}

	@Override
	public void init() {
		super.init();
		// 初始化配置，例如字体颜色等
		/*flContentMarginTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINTOP);
		flContentMarginBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINBOTTOM);
		tvNumWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_WIDTH);
		tvNumHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_TXTNUM_HEIGHT);
		tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
		llDetailMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT);
		tvContentMarginRight = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		tvDescMarginRight = (int) LayouUtil.getDimen("x4");*/
		
//		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
		dividerHeight = 1;
		
		/*tvNumSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_AUDIO_INDEX_SIZE1);
		tvNumColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_AUDIO_INDEX_COLOR1);
		tvContentSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_AUDIO_ITEM_SIZE1);
		tvContentColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_AUDIO_ITEM_COLOR1);
		tvDescSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_AUDIO_ITEM_SIZE2);
		tvDescColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_AUDIO_ITEM_COLOR2);*/

        tvNumColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvContentColor =  Color.parseColor(LayouUtil.getString("color_main_title"));
        tvDescColor =  Color.parseColor(LayouUtil.getString("color_vice_title"));
	}

	//切换模式修改布局参数
	public void onUpdateParams(int styleIndex){
		int unit = ViewParamsUtil.unit;
		tvNumSide =  6 * unit;
		tvNumHorMargin =  unit;
		tvNumSize = ViewParamsUtil.h0;
		tvContentSize = ViewParamsUtil.h4;
		tvContentHeight = ViewParamsUtil.h4Height;
		centerInterval = ViewParamsUtil.centerInterval;
		tvDescSize = ViewParamsUtil.h6;
		tvDescHeight = ViewParamsUtil.h6Height;
		ivTagSide = ViewParamsUtil.musicTagSide;
		ivTagMargin = unit/3;
		//竖屏全屏、半屏列表字体size加2
		if (styleIndex != StyleConfig.STYLE_ROBOT_NONE_SCREES && WinLayout.isVertScreen){
			tvContentSize = ViewParamsUtil.h3;
			tvContentHeight = ViewParamsUtil.h3Height;
			tvDescSize = ViewParamsUtil.h5;
			tvDescHeight = ViewParamsUtil.h5Height;
		}
	}

	@Override
	public void snapPage(boolean next) {
		LogUtil.logd("update snap "+next);
	}
	
	@Override
	public List<View> getFocusViews() {
		return mItemViews;
	}
	
	/**
	 * 是否含有动画
	 * @return
	 */
	@Override
	public boolean hasViewAnimation() {
		return true;
	}
	
	
	private View createItemView(int position,AudioBean audioBean,boolean showDivider){

		LogUtil.logd(WinLayout.logTag+ "AudioBean: "+audioBean.paid+"--"+audioBean.lastPlay+"--"+audioBean.latest+"--"+audioBean.novelStatus);

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
		layoutParams.topMargin = flContentMarginTop;
		layoutParams.bottomMargin = flContentMarginBottom;
		itemView.addView(flContent,layoutParams);*/
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(llContent,layoutParams);
		
		TextView tvNum = new TextView(GlobalContext.get());
		tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
		tvNum.setGravity(Gravity.CENTER);
		tvNum.setIncludeFontPadding(false);
		tvNum.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumSide,tvNumSide);
		mLLayoutParams.leftMargin = tvNumHorMargin;
		mLLayoutParams.rightMargin = tvNumHorMargin;
		mLLayoutParams.gravity = Gravity.CENTER;
		llContent.addView(tvNum,mLLayoutParams);
		
		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
        llDetail.setOrientation(LinearLayout.VERTICAL);
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContent.addView(llDetail,mLLayoutParams);

		LinearLayout llTitle = new LinearLayout(GlobalContext.get());
        llTitle.setOrientation(LinearLayout.HORIZONTAL);
		//mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        mLLayoutParams.rightMargin = centerInterval;
		llDetail.addView(llTitle,mLLayoutParams);

		TextView tvContent = new TextView(GlobalContext.get());
        tvContent.setGravity(Gravity.CENTER_VERTICAL);
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
        mLLayoutParams = new LinearLayout.LayoutParams(0,tvContentHeight,1);
        mLLayoutParams.gravity = Gravity.BOTTOM;
        llTitle.addView(tvContent,mLLayoutParams);

		ImageView latest = new ImageView(GlobalContext.get());
        latest.setVisibility(View.GONE);
		mLLayoutParams = new LinearLayout.LayoutParams(ivTagSide,ivTagSide);
		mLLayoutParams.rightMargin = ivTagMargin;
        mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llTitle.addView(latest,mLLayoutParams);

		ImageView last = new ImageView(GlobalContext.get());
        last.setVisibility(View.GONE);
        llTitle.addView(last,mLLayoutParams);

		ImageView state = new ImageView(GlobalContext.get());
        state.setVisibility(View.GONE);
        llTitle.addView(state,mLLayoutParams);

		ImageView ivPaid = new ImageView(GlobalContext.get());
        ivPaid.setVisibility(View.GONE);
        llTitle.addView(ivPaid,mLLayoutParams);

		if (audioBean.latest){
			latest.setImageDrawable(LayouUtil.getDrawable("music_tag_latest"));
            latest.setVisibility(View.VISIBLE);
		}
		if (audioBean.lastPlay){
			last.setImageDrawable(LayouUtil.getDrawable("music_tag_last"));
            last.setVisibility(View.VISIBLE);
		}
		switch (audioBean.novelStatus){
			case AudioBean.NOVEL_STATUS_SERILIZE:
				state.setImageDrawable(LayouUtil.getDrawable("music_tag_status1"));
                state.setVisibility(View.VISIBLE);
				break;
			case AudioBean.NOVEL_STATUS_END:
				state.setImageDrawable(LayouUtil.getDrawable("music_tag_status2"));
                state.setVisibility(View.VISIBLE);
				break;
			case AudioBean.NOVEL_STATUS_INVALID:
			default:
				state.setVisibility(View.GONE);
		}
		if (audioBean.paid){
			ivPaid.setImageDrawable(LayouUtil.getDrawable("music_tag_paid"));
            ivPaid.setVisibility(View.VISIBLE);
		}

		//内容到详情的间距
        View view = new View(GlobalContext.get());
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,centerInterval);
        llDetail.addView(view,mLLayoutParams);

		FrameLayout flLayout = new FrameLayout(GlobalContext.get());
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llDetail.addView(flLayout,mLLayoutParams);

		TextView tvDesc = new TextView(GlobalContext.get());
        tvDesc.setGravity(Gravity.CENTER_VERTICAL);
		tvDesc.setSingleLine();
		tvDesc.setEllipsize(TruncateAt.END);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,tvDescHeight);
		mLLayoutParams.gravity = Gravity.TOP;
        flLayout.addView(tvDesc,mLLayoutParams);
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);
		
		TextViewUtil.setTextSize(tvNum,tvNumSize);
		TextViewUtil.setTextColor(tvNum,tvNumColor);
		TextViewUtil.setTextSize(tvContent,tvContentSize);
		TextViewUtil.setTextColor(tvContent,tvContentColor);
		TextViewUtil.setTextSize(tvDesc,tvDescSize);
		TextViewUtil.setTextColor(tvDesc,tvDescColor);
		
		tvNum.setText(String.valueOf(position + 1));
		tvContent.setText(StringUtils.isEmpty(audioBean.title) ?"" : LanguageConvertor.toLocale(audioBean.title));
		tvDesc.setText(LanguageConvertor.toLocale(TextUtils.isEmpty(audioBean.name) ? "无名" : audioBean.name));

		divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);
		
		return itemView;
	}

    private View createItemViewTag(int position,AudioBean audioBean,boolean showDivider){

        LogUtil.logd(WinLayout.logTag+ "AudioBean: "+audioBean.paid+"--"+audioBean.lastPlay+"--"+audioBean.latest+"--"+audioBean.novelStatus);

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

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.HORIZONTAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(llContent,layoutParams);

        TextView tvNum = new TextView(GlobalContext.get());
        tvNum.setBackground(LayouUtil.getDrawable("poi_item_circle_bg"));
        tvNum.setGravity(Gravity.CENTER);
        tvNum.setIncludeFontPadding(false);
        tvNum.setPadding(0, 0, 0, 0);
        LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(tvNumSide,tvNumSide);
        mLLayoutParams.leftMargin = tvNumHorMargin;
        mLLayoutParams.rightMargin = tvNumHorMargin;
        mLLayoutParams.gravity = Gravity.CENTER;
        llContent.addView(tvNum,mLLayoutParams);

        LinearLayout llDetail = new LinearLayout(GlobalContext.get());
        llDetail.setOrientation(LinearLayout.VERTICAL);
        llDetail.setGravity(Gravity.CENTER_VERTICAL);
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        llContent.addView(llDetail,mLLayoutParams);

        LinearLayout llTitle = new LinearLayout(GlobalContext.get());
        llTitle.setOrientation(LinearLayout.HORIZONTAL);
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llDetail.addView(llTitle,mLLayoutParams);

        TextView tvContent = new TextView(GlobalContext.get());
        tvContent.setGravity(Gravity.CENTER_VERTICAL);
        tvContent.setEllipsize(TruncateAt.END);
        tvContent.setSingleLine();
        mLLayoutParams = new LinearLayout.LayoutParams(0,tvContentHeight,1);
        mLLayoutParams.gravity = Gravity.BOTTOM;
        llTitle.addView(tvContent,mLLayoutParams);

        View view1 = new View(GlobalContext.get());
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,centerInterval);
        llDetail.addView(view1,mLLayoutParams);

        FrameLayout flLayout = new FrameLayout(GlobalContext.get());
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llDetail.addView(flLayout,mLLayoutParams);

        TextView tvDesc = new TextView(GlobalContext.get());
        tvDesc.setGravity(Gravity.CENTER_VERTICAL);
        tvDesc.setSingleLine();
        tvDesc.setEllipsize(TruncateAt.END);
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,tvDescHeight);
        mLLayoutParams.gravity = Gravity.TOP;
        flLayout.addView(tvDesc,mLLayoutParams);

        View view2 = new View(GlobalContext.get());
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,centerInterval);
        llDetail.addView(view2,mLLayoutParams);

        LinearLayout llTag = new LinearLayout(GlobalContext.get());
        llTag.setOrientation(LinearLayout.HORIZONTAL);
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llDetail.addView(llTag,mLLayoutParams);

        ImageView latest = new ImageView(GlobalContext.get());
        latest.setVisibility(View.GONE);
        mLLayoutParams = new LinearLayout.LayoutParams(ivTagSide,ivTagSide);
        mLLayoutParams.rightMargin = ivTagMargin;
        mLLayoutParams.gravity = Gravity.BOTTOM;
        llTag.addView(latest,mLLayoutParams);

        ImageView last = new ImageView(GlobalContext.get());
        last.setVisibility(View.GONE);
        llTag.addView(last,mLLayoutParams);

        ImageView state = new ImageView(GlobalContext.get());
        state.setVisibility(View.GONE);
        llTag.addView(state,mLLayoutParams);

        ImageView ivPaid = new ImageView(GlobalContext.get());
        ivPaid.setVisibility(View.GONE);
        llTag.addView(ivPaid,mLLayoutParams);

        if (audioBean.latest){
            latest.setImageDrawable(LayouUtil.getDrawable("music_tag_latest"));
            latest.setVisibility(View.VISIBLE);
        }
        if (audioBean.lastPlay){
            last.setImageDrawable(LayouUtil.getDrawable("music_tag_last"));
            last.setVisibility(View.VISIBLE);
        }
        switch (audioBean.novelStatus){
            case AudioBean.NOVEL_STATUS_SERILIZE:
                state.setImageDrawable(LayouUtil.getDrawable("music_tag_status1"));
                state.setVisibility(View.VISIBLE);
                break;
            case AudioBean.NOVEL_STATUS_END:
                state.setImageDrawable(LayouUtil.getDrawable("music_tag_status2"));
                state.setVisibility(View.VISIBLE);
                break;
            case AudioBean.NOVEL_STATUS_INVALID:
            default:
                state.setVisibility(View.GONE);
        }
        if (audioBean.paid){
            ivPaid.setImageDrawable(LayouUtil.getDrawable("music_tag_paid"));
        }

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);

        TextViewUtil.setTextSize(tvNum,tvNumSize);
        TextViewUtil.setTextColor(tvNum,tvNumColor);
        TextViewUtil.setTextSize(tvContent,tvContentSize);
        TextViewUtil.setTextColor(tvContent,tvContentColor);
        TextViewUtil.setTextSize(tvDesc,tvDescSize);
        TextViewUtil.setTextColor(tvDesc,tvDescColor);

        tvNum.setText(String.valueOf(position + 1));
        tvContent.setText(StringUtils.isEmpty(audioBean.title) ?"" : LanguageConvertor.toLocale(audioBean.title));
        tvDesc.setText(LanguageConvertor.toLocale(audioBean.name));

        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

        return itemView;
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
