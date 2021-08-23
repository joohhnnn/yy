package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.VersionManager;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.HelpDetailListViewData;
import com.txznet.comm.ui.viewfactory.data.HelpDetailListViewData.HelpDetailBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IHelpDetailListView;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class HelpDetailListView extends IHelpDetailListView {

	private static HelpDetailListView sInstance = new HelpDetailListView();
	
	private View mView; //当前显示的View
	
	private List<View> mFocusViews;
	
	//字体等参数配置
	private Drawable llContentBg;
	
	private int flContentMarginTop;
	private int flContentMarginBottom;
	private int ivIconWidth;
	private int ivIconHeight;
	private int tvNumMarginLeft;
	private int llDetailMarginLeft;
	private int tvContentMarginLeft;
	private int tvContentMarginRight;
	
	private int tvDescMarginLeft;
	private int tvDescMarginRight;
	
	
	private int dividerHeight;
	
	private int tvContentSize;    //无屏标题字体大小
	private int tvContentColor;    //无屏标题字体颜色
	private int tvContentColor2;
	private float tvDescSize;
	private int tvDescColor;
	private boolean isNew;

	private int tvTitleSize;    //列表项内容字体大小
	private int tvTitleColor;    //列表项内容字体颜色
	private boolean hasNet = true;

	private int rlTopHeight;    //顶部工具栏高度
	private int rlTopLeftMargin;    //顶部工具栏左边距
	private int rlTopRightMargin;    //顶部工具栏右边距
	private int ivBackSize;    //返回键大小
	private int tvBackSize;    //“返回”字体大小
	private int tvBackColor;    //“返回”字体颜色
	private int tvHelpSize;    //“帮助”字体大小
	private int tvHelpColor;    //“帮助”字体颜色
	private int ivSettingSize;    //设置图标大小

	private int contentHorMargin;    //内容左右边距
	private int contentHeight;    //内容高度
	private int tvTitleViewSize;    //小标题字体大小
	private int tvTitleViewColor;    //小标题字体颜色
	
	private HelpDetailListView() {
	}

	public static HelpDetailListView getInstance(){
		return sInstance;
	}
	
	@Override
	public void updateProgress(int progress, int selection) {
	}

	@Override
	public void release() {
		super.release();
		if (mFocusViews != null) {
			mFocusViews.clear();
		}
	}

	@Override
	public ViewAdapter getView(ViewData data) {
		HelpDetailListViewData helpListViewData = (HelpDetailListViewData) data;
        LogUtil.logd(WinLayout.logTag+ "HelpDetailListViewData.vTips:" + helpListViewData.vTips+StyleConfig.getInstance().getSelectStyleIndex());

		View view = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                if(WinLayout.isVertScreen){
                    view = createHelpDetailListVerticalViewFull(helpListViewData);
                }else {
                    view = createHelpDetailListViewFull(helpListViewData);
                }

				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
                WinLayout.getInstance().vTips = helpListViewData.mTitleInfo.titlefix;
				view = createHelpDetailListViewNone(helpListViewData);
				break;
		}
		
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.view.setTag(data.getType());
		viewAdapter.isListView = true;
		viewAdapter.object = HelpDetailListView.getInstance();
		return viewAdapter;
	}

	private View createHelpDetailListViewFull(HelpDetailListViewData helpListViewData){
		hasNet = helpListViewData.hasNet;
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setOrientation(LinearLayout.VERTICAL);

		RelativeLayout rTop = new RelativeLayout(GlobalContext.get());
		rTop.setGravity(Gravity.CENTER_VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,rlTopHeight);
		layoutParams.setMargins(rlTopLeftMargin,0,rlTopRightMargin,0);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(rTop,layoutParams);

		ImageView ivSetting = new ImageView(GlobalContext.get());
		ivSetting.setVisibility(com.txznet.comm.remote.util.ConfigUtil.isShowSettings()?View.VISIBLE:View.GONE);
		ivSetting.setImageDrawable(LayouUtil.getDrawable("setting_title"));
		RelativeLayout.LayoutParams rLayoutParams = new RelativeLayout.LayoutParams(ivSettingSize,ivSettingSize);
		rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rTop.addView(ivSetting,rLayoutParams);
		ivSetting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RecordWin2Manager.getInstance().operateView(
						RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_SETTING,0,0);

				ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("setting").setType("touch_voice_center")
						.putExtra("style", com.txznet.comm.remote.util.ConfigUtil.isShowHelpInfos()?"help":"setting").setSessionId().buildCommReport());
			}
		});

		TextView tvTittle = new TextView(GlobalContext.get());
		tvTittle.setGravity(Gravity.CENTER_VERTICAL);
		//tvTittle.setText("帮助");
		tvTittle.setText(LanguageConvertor.toLocale(helpListViewData.getData().get(0).title));
		RelativeLayout.LayoutParams rLayoutParams1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		rLayoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
		rTop.addView(tvTittle,rLayoutParams1);

		LinearLayout lBackRoot = new LinearLayout(GlobalContext.get());
		lBackRoot.setOrientation(LinearLayout.HORIZONTAL);
		lBackRoot.setGravity(Gravity.CENTER_VERTICAL);
		RelativeLayout.LayoutParams rLayoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		rLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rTop.addView(lBackRoot,rLayoutParams2);
		lBackRoot.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (VersionManager.getInstance().isUseHelpNewTag()) {
					com.txznet.comm.remote.util.ConfigUtil.setShowHelpNewTag(false);
				}
				RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_HELP_BACK, 0, 0);
			}
		});

		ImageView ivBack = new ImageView(GlobalContext.get());
		ivBack.setImageDrawable(LayouUtil.getDrawable("back"));
		layoutParams = new LinearLayout.LayoutParams(ivBackSize,ivBackSize);
		lBackRoot.addView(ivBack,layoutParams);

		TextView tvBack = new TextView(GlobalContext.get());
		tvBack.setGravity(Gravity.CENTER_VERTICAL);
		tvBack.setText(LanguageConvertor.toLocale("返回"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		lBackRoot.addView(tvBack,layoutParams);

		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(helpListViewData,"help","帮助");
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		//layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
		layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
		llLayout.addView(titleViewAdapter.view,layoutParams);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
		layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
		llLayout.addView(llContents,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llContents.addView(llContent,layoutParams);

		mCurPage = helpListViewData.mTitleInfo.curPage;
		mMaxPage = helpListViewData.mTitleInfo.maxPage;
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
		mFocusViews = new ArrayList<View>();
		LogUtil.logd(WinLayout.logTag+ "helpDetailListViewData.count" + helpListViewData.count);
		for (int i = 1; i < helpListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
			View itemView  = createItemView(i,helpListViewData.getData().get(i),i != SizeConfig.pageHelpDetailCount);
			mFocusViews.add(itemView);
			llContent.addView(itemView,layoutParams);
		}
		if (helpListViewData.count <= SizeConfig.pageHelpDetailCount){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,SizeConfig.pageHelpDetailCount+1 - helpListViewData.count);
			llContent.addView(linearLayout, layoutParams);
		}

		TextViewUtil.setTextSize(tvTittle,tvHelpSize);
		TextViewUtil.setTextColor(tvTittle,tvHelpColor);
		TextViewUtil.setTextSize(tvBack,tvBackSize);
		TextViewUtil.setTextColor(tvBack,tvBackColor);
		/*TextViewUtil.setTextSize(tvTitleView,tvTitleViewSize);
		TextViewUtil.setTextColor(tvTitleView,tvTitleViewColor);*/
		/*TextViewUtil.setTextSize(tvTitle, tvContentSize);
		TextViewUtil.setTextColor(tvTitle, tvContentColor);*/
		return llLayout;
	}

    private View createHelpDetailListVerticalViewFull(HelpDetailListViewData helpListViewData){
        hasNet = helpListViewData.hasNet;
        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout rTop = new RelativeLayout(GlobalContext.get());
        rTop.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,rlTopHeight);
        layoutParams.setMargins(rlTopLeftMargin,0,rlTopRightMargin,0);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(rTop,layoutParams);

        ImageView ivSetting = new ImageView(GlobalContext.get());
        ivSetting.setVisibility(com.txznet.comm.remote.util.ConfigUtil.isShowSettings()?View.VISIBLE:View.GONE);
        ivSetting.setImageDrawable(LayouUtil.getDrawable("setting_title"));
        RelativeLayout.LayoutParams rLayoutParams = new RelativeLayout.LayoutParams(ivSettingSize,ivSettingSize);
        rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rTop.addView(ivSetting,rLayoutParams);
        ivSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordWin2Manager.getInstance().operateView(
                        RecordWinController.OPERATE_CLICK,
                        RecordWinController.VIEW_SETTING,0,0);

                ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("setting").setType("touch_voice_center")
                        .putExtra("style", com.txznet.comm.remote.util.ConfigUtil.isShowHelpInfos()?"help":"setting").setSessionId().buildCommReport());
            }
        });

        TextView tvTittle = new TextView(GlobalContext.get());
        tvTittle.setGravity(Gravity.CENTER_VERTICAL);
        //tvTittle.setText("帮助");
        tvTittle.setText(LanguageConvertor.toLocale(helpListViewData.getData().get(0).title));
        RelativeLayout.LayoutParams rLayoutParams1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        rLayoutParams1.addRule(RelativeLayout.CENTER_IN_PARENT);
        rTop.addView(tvTittle,rLayoutParams1);

        LinearLayout lBackRoot = new LinearLayout(GlobalContext.get());
        lBackRoot.setOrientation(LinearLayout.HORIZONTAL);
        lBackRoot.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams rLayoutParams2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        rLayoutParams2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rTop.addView(lBackRoot,rLayoutParams2);
        lBackRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (VersionManager.getInstance().isUseHelpNewTag()) {
                    com.txznet.comm.remote.util.ConfigUtil.setShowHelpNewTag(false);
                }
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        RecordWinController.VIEW_HELP_BACK, 0, 0);
            }
        });

        ImageView ivBack = new ImageView(GlobalContext.get());
        ivBack.setImageDrawable(LayouUtil.getDrawable("back"));
        layoutParams = new LinearLayout.LayoutParams(ivBackSize,ivBackSize);
        lBackRoot.addView(ivBack,layoutParams);

        TextView tvBack = new TextView(GlobalContext.get());
        tvBack.setGravity(Gravity.CENTER_VERTICAL);
        tvBack.setText(LanguageConvertor.toLocale("返回"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lBackRoot.addView(tvBack,layoutParams);

        FrameLayout flCenterContent = new FrameLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        llLayout.addView(flCenterContent,layoutParams);

        LinearLayout llCenterContent = new LinearLayout(GlobalContext.get());
        llCenterContent.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.WRAP_CONTENT);
        flLayoutParams.gravity = Gravity.CENTER;
        flCenterContent.addView(llCenterContent,flLayoutParams);

        ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(helpListViewData,"help","帮助");
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
        //layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
        layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
        //llLayout.addView(titleViewAdapter.view,layoutParams);
        llCenterContent.addView(titleViewAdapter.view,layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
        layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
        //llLayout.addView(llContents,layoutParams);
        llCenterContent.addView(llContents,layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        llContents.addView(llContent,layoutParams);

        mCurPage = helpListViewData.mTitleInfo.curPage;
        mMaxPage = helpListViewData.mTitleInfo.maxPage;
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
        mFocusViews = new ArrayList<View>();
        LogUtil.logd(WinLayout.logTag+ "helpDetailListViewData.count" + helpListViewData.count);
        for (int i = 1; i < helpListViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
            View itemView  = createItemView(i,helpListViewData.getData().get(i),i != SizeConfig.pageHelpDetailCount);
            mFocusViews.add(itemView);
            llContent.addView(itemView,layoutParams);
        }
        if (helpListViewData.count <= SizeConfig.pageHelpDetailCount){
            LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,SizeConfig.pageHelpDetailCount+1 - helpListViewData.count);
            llContent.addView(linearLayout, layoutParams);
        }

        TextViewUtil.setTextSize(tvTittle,tvHelpSize);
        TextViewUtil.setTextColor(tvTittle,tvHelpColor);
        TextViewUtil.setTextSize(tvBack,tvBackSize);
        TextViewUtil.setTextColor(tvBack,tvBackColor);
		/*TextViewUtil.setTextSize(tvTitleView,tvTitleViewSize);
		TextViewUtil.setTextColor(tvTitleView,tvTitleViewColor);*/
		/*TextViewUtil.setTextSize(tvTitle, tvContentSize);
		TextViewUtil.setTextColor(tvTitle, tvContentColor);*/
        return llLayout;
    }

	private View createHelpDetailListViewNone(HelpDetailListViewData helpListViewData){
		WinLayout.getInstance().vTips = helpListViewData.mTitleInfo.titlefix;

		hasNet = helpListViewData.hasNet;
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.HORIZONTAL);

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		llLayout.addView(llContents,layoutParams);

		mCurPage = helpListViewData.mTitleInfo.curPage;
		mMaxPage = helpListViewData.mTitleInfo.maxPage;

			LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
			//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
			layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
			llLayout.addView(llPager,layoutParams);

		LinearLayout lTitle = new LinearLayout(GlobalContext.get());
		lTitle.setOrientation(LinearLayout.HORIZONTAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		llContents.addView(lTitle,layoutParams);

		View divider = new View(GlobalContext.get());
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		llContents.addView(divider, layoutParams);

		View.OnClickListener backListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (VersionManager.getInstance().isUseHelpNewTag()) {
                    com.txznet.comm.remote.util.ConfigUtil.setShowHelpNewTag(false);
                }
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        RecordWinController.VIEW_HELP_BACK, 0, 0);
            }
        };

		ImageView ivBack = new ImageView(GlobalContext.get());
		ivBack.setImageDrawable(LayouUtil.getDrawable("back"));
		layoutParams = new LinearLayout.LayoutParams((int)(tvContentSize*1.2),(int)(tvContentSize*1.2));
        layoutParams.leftMargin = (int) LayouUtil.getDimen("x5");
        layoutParams.rightMargin = (int) LayouUtil.getDimen("x5");
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
		lTitle.addView(ivBack,layoutParams);
		ivBack.setOnClickListener(backListener);

		TextView tvTitle = new TextView(GlobalContext.get());
		tvTitle.setGravity(Gravity.CENTER_VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.MATCH_PARENT);
		lTitle.addView(tvTitle,layoutParams);
		tvTitle.setText(LanguageConvertor.toLocale(helpListViewData.getData().get(0).title));
        tvTitle.setOnClickListener(backListener);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		//layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pageCount * (SizeConfig.pageHelpDetailCount - 1));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
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
		mFocusViews = new ArrayList<View>();
		for (int i = 1; i < helpListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
			View itemView = createItemView(i,helpListViewData.getData().get(i),i != (SizeConfig.pageHelpDetailCount));
			mFocusViews.add(itemView);
			llContent.addView(itemView,layoutParams);
		}
		if (helpListViewData.count < (SizeConfig.pageHelpDetailCount)){
			LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,(SizeConfig.pageHelpDetailCount - helpListViewData.count));
			llContent.addView(linearLayout, layoutParams);
		}

        TextViewUtil.setTextSize(tvTitle, tvContentSize);
        TextViewUtil.setTextSize(tvTitle,  tvContentColor);

		return llLayout;
	}

	@Override
	public void init() {
		super.init();
		// 初始化配置，例如字体颜色等
		llContentBg = LayouUtil.getDrawable("white_range_layout");
		flContentMarginTop = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINTOP);
		flContentMarginBottom = (int) ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_CONTENT_MARGINBOTTOM);
		ivIconWidth = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_HELP_ICON_WIDTH);
		ivIconHeight = (int)ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_HELP_ICON_HEIGHT);
		tvNumMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTNUM_MARGINLEFT);
		llDetailMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_LAYOUT_CONTENT_MARGINLEFT);
		tvContentMarginLeft = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		tvContentMarginRight = (int) ThemeConfigManager.getX(ThemeConfigManager.LIST_ITEM_TXTCONTENT_MARGINLEFT);
		tvDescMarginLeft = (int) LayouUtil.getDimen("x4");
		tvDescMarginRight = (int) LayouUtil.getDimen("x4");

		dividerHeight = 1;

		tvContentColor = Color.parseColor(LayouUtil.getString("color_main_title"));
		tvContentColor2=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR3);
		//tvDescSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE2);
		//tvDescSize= LayouUtil.getDimen("h3");
		tvDescColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR2);

		tvTitleColor = Color.parseColor(LayouUtil.getString("color_main_title"));

		tvTitleViewSize = ViewParamsUtil.h6;
		tvTitleViewColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        tvBackColor = Color.WHITE;
        tvHelpColor = Color.WHITE;
	}

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex){
		int unit = ViewParamsUtil.unit;
		tvTitleSize = ViewParamsUtil.h4;
		tvDescSize= ViewParamsUtil.h3;
		rlTopHeight = 10 * unit;
		rlTopLeftMargin = 3 * unit;
		rlTopRightMargin = 0;
		ivBackSize = 3 * unit;
		tvBackSize = ViewParamsUtil.h3;
		tvHelpSize = ViewParamsUtil.h3;
		ivSettingSize = 10 * unit;
		contentHorMargin = (WinLayout.isVertScreen?2:5) * unit;
		contentHeight = SizeConfig.itemHelpDetailHeight * SizeConfig.pageHelpDetailCount;
		tvContentSize = ViewParamsUtil.h4;
        switch (styleIndex) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
//                initFull();
				if (WinLayout.isVertScreen){
					tvContentSize = ViewParamsUtil.h3;
				}else {
					tvContentSize = ViewParamsUtil.h5;
				}
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
//                initHalf();
				if (WinLayout.isVertScreen) tvContentSize = ViewParamsUtil.h3;
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
//                initNone();
                break;
            default:
                break;
        }
    }

	@Override
	public void snapPage(boolean next) {

	}
	
	@Override
	public List<View> getFocusViews() {
		return mFocusViews;
	}

	/**
	 * 是否含有动画
	 * @return
	 */
	@Override
	public boolean hasViewAnimation() {
		return true;
	}
	
	private View createItemView(int position,HelpDetailBean helpBean,boolean showDivider){
		RelativeLayout itemView = new RelativeLayout(GlobalContext.get());
		itemView.setTag(position);
//		itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
//		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		/*layoutParams.topMargin = flContentMarginTop;
		layoutParams.bottomMargin = flContentMarginBottom;*/
		itemView.addView(flContent,layoutParams);
		
//		GradientProgressBar mProgressBar = new GradientProgressBar(GlobalContext.get());
//		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
//		flContent.addView(mProgressBar, mFLayoutParams);
		
		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
		FrameLayout.LayoutParams mFLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		mFLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		flContent.addView(llContent, mFLayoutParams);
		
//		ImageView ivIcon = new ImageView(GlobalContext.get());
//		ivIcon.setPadding(0, 0, 0, 0);
//		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(ivIconWidth,ivIconHeight);
//		mLLayoutParams.leftMargin = tvNumMarginLeft;
//		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		llContent.addView(ivIcon,mLLayoutParams);
		
		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		llDetail.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		mLLayoutParams.leftMargin = llDetailMarginLeft;
		llContent.addView(llDetail,mLLayoutParams);
		
		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.BOTTOM);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		mLLayoutParams.leftMargin = tvContentMarginLeft;
		mLLayoutParams.rightMargin = tvContentMarginRight;
		llDetail.addView(tvContent,mLLayoutParams);
		
//		TextView tvDesc = new TextView(GlobalContext.get());
//		tvDesc.setSingleLine();
//		tvDesc.setEllipsize(TruncateAt.END);
//		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
////		mLLayoutParams.leftMargin = tvDescMarginLeft;
//		mLLayoutParams.rightMargin = tvDescMarginRight;
//		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		llDetail.addView(tvDesc,mLLayoutParams);
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		//divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);
		
//		ImageView ivArrow = new ImageView(GlobalContext.get());
//		ivArrow.setPadding(0, 0, 0, 0);
//		mLLayoutParams = new LinearLayout.LayoutParams(ivIconWidth/2,ivIconHeight/2);
//		mLLayoutParams.rightMargin = tvNumMarginLeft;
//		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
//		ivArrow.setImageDrawable(LayouUtil.getDrawable("help_arrow"));
//		llContent.addView(ivArrow,mLLayoutParams);

		/*if (showDivider) {
			TextViewUtil.setTextSize(tvContent, tvTitleSize);
			TextViewUtil.setTextColor(tvContent, tvTitleColor);
		}else {
			TextViewUtil.setTextSize(tvContent, tvContentSize);
			if (!hasNet && (helpBean.netType == 1)){
				TextViewUtil.setTextColor(tvContent, tvContentColor2);
			} else {
				TextViewUtil.setTextColor(tvContent, tvContentColor);
			}
		}*/
		TextViewUtil.setTextSize(tvContent, tvTitleSize);
		TextViewUtil.setTextColor(tvContent, tvTitleColor);
//		TextViewUtil.setTextSize(tvDesc,tvDescSize);
//		TextViewUtil.setTextColor(tvDesc,tvDescColor);
//		if (isNew && (position == 0) || (!isNew)) {
//			// 是新显示的
//			if (helpBean.isNew) {
//				tvContent.setCompoundDrawablePadding((int) LayouUtil.getDimen("x4"));
//				Drawable drawable = LayouUtil.getDrawable("ic_help_new");
//				if (drawable != null) {
//					int height = drawable.getIntrinsicHeight();
//					float scale = (float) tvContent.getLineHeight() / (float) height * 0.65f;
//					drawable.setBounds(0, 0, (int) (drawable.getIntrinsicWidth() * scale), (int) (height * scale));
//					tvContent.setCompoundDrawables(null, null, drawable, null);
//				}
//			}
//		}
		
//		ivIcon.setImageDrawable(LayouUtil.getDrawable(helpBean.iconName));
		tvContent.setText(LanguageConvertor.toLocale(StringUtils.isEmpty(helpBean.title) ?"" : helpBean.title));
//		tvDesc.setText(helpBean.intro);

//		mPb.setVisibility(musicItem.shouldWaiting ? View.VISIBLE : View.INVISIBLE);
//		mPb.setProgress(musicItem.shouldWaiting ? musicItem.curPrg : 0);

		divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);
		/*itemView.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					v.setBackgroundColor(Color.parseColor("#4AA5FA"));
				} else {
					v.setBackgroundColor(Color.TRANSPARENT);
				}
			}
		});*/
		
		return itemView;
	}

	@Override
	public void updateItemSelect(int arg0) {
		// TODO Auto-generated method stub
		
	}

	
}
