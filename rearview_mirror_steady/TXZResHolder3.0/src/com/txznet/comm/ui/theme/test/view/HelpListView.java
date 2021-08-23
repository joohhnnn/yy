package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.google.zxing.WriterException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.FloatPointSP;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.VersionManager;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.HelpListViewData;
import com.txznet.comm.ui.viewfactory.data.HelpListViewData.HelpBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IHelpListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.loader.AppLogicBase;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.sdk.TXZReportManager;
import com.txznet.txz.util.QRUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.runnables.Runnable3;

@SuppressLint("NewApi")
public class HelpListView extends IHelpListView {

	private static HelpListView sInstance = new HelpListView();

	private View mView; //当前显示的View

	private List<View> mItemViews;
	ImageView ivQRCode;

	private int dividerHeight;
	private boolean canOpenDetail;
	private boolean isShowTips;
	private String tips;

	private int rlTopHeight;    //顶部工具栏高度
    private int rlTopLeftMargin;    //顶部工具栏左边距
    private int rlTopRightMargin;    //顶部工具栏右边距
    private int ivBackSize;    //返回键大小
    private int tvBackSize;    //“返回”字体大小
    private int tvBackColor;    //“返回”字体颜色
    private int tvHelpSize;    //“帮助”字体大小
    private int tvHelpColor;    //“帮助”字体颜色
    private int ivSettingSize;    //设置图标大小
    private int ivSettingSizeNone;    //无屏设置图标大小
    private int contentHorMargin;    //内容左右边距
    private int contentHeight;    //内容高度

    private int ivIconSide;    //列表图标的边长
    private int ivIconHorMargin;    //列表图标左右边距
    private int tvContentSize;    //列表标题字体大小
    private int tvContentHeight;    //列表标题行高
    private int tvContentColor;    //列表标题颜色
	private int centerInterval;    //内容到距离的间距
    private int tvDescSize;    //列表详细内容字体大小
    private int tvDescHeight;    //列表详细内容行高
    private int tvDescColor;    //列表详细内容颜色

	public static final int TYPE_FULL_SCREEN = 1;
	public static final int TYPE_VERTICAL_SCREEN = 2;
	public static final int TYPE_NONE_SCREEN = 3;

	private HelpListView() {
	}

	public static HelpListView getInstance(){
		return sInstance;
	}

	@Override
	public void updateProgress(int progress, int selection) {
	}

	@Override
	public void release() {
		super.release();
//		LayoutInflater mLayoutInflater = LayoutInflater.from(context)
		if (mItemViews != null) {
			mItemViews.clear();
		}
		ivQRCode = null;
	}

	@Override
	public ViewAdapter getView(ViewData data) {
		HelpListViewData helpListViewData = (HelpListViewData) data;
		//WinLayout.getInstance().vTips = helpListViewData.vTips;
		LogUtil.logd(WinLayout.logTag+ "helpListViewData.vTips: "+helpListViewData.vTips+ " helpListViewData.canOpenDetail: "+helpListViewData.canOpenDetail);

		View view = null;
		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
			    if (WinLayout.isVertScreen){
                    view = createVerticalViewFull(helpListViewData);
                }else {
                    view = createViewFull(helpListViewData);
                }
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
                WinLayout.getInstance().vTips = helpListViewData.mTitleInfo.prefix;
				view = createViewNone(helpListViewData);
				break;
		}

		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = view;
		viewAdapter.view.setTag(data.getType());
		viewAdapter.isListView = true;
		viewAdapter.object = HelpListView.getInstance();
		return viewAdapter;
	}

	private View createViewFull(HelpListViewData helpListViewData){
		canOpenDetail = helpListViewData.canOpenDetail;
		isShowTips = helpListViewData.isShowTips;
		tips = helpListViewData.tips;

		mCurPage = helpListViewData.mTitleInfo.curPage;
		mMaxPage = helpListViewData.mTitleInfo.maxPage;

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setOrientation(LinearLayout.VERTICAL);

		RelativeLayout rTop = new RelativeLayout(GlobalContext.get());
		rTop.setGravity(Gravity.CENTER_VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,rlTopHeight);
        layoutParams.setMargins(rlTopLeftMargin,0,rlTopRightMargin,0);
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(rTop,layoutParams);

        /*TextView tvTitleView = new TextView(GlobalContext.get());
        tvTitleView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        String text = helpListViewData.mTitleInfo.prefix;
        LogUtil.logd(WinLayout.logTag+ "helpListViewData.mTitleInfo.prefix: "+helpListViewData.mTitleInfo.prefix);
        TextViewUtil.setTextSize(tvTitleView,tvDescSize);
        TextViewUtil.setTextColor(tvTitleView,tvDescColor);
        int startIdx = text.indexOf("“")+1;
        int endIdx = text.indexOf("”");
        if (endIdx != -1){
			SpannableString textString = new SpannableString(text);
			textString.setSpan(new ForegroundColorSpan(Color.parseColor("#16CAF9")),startIdx,endIdx,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			tvTitleView.setText(textString);
		}else {
            tvTitleView.setText(text);
        }*/
		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(helpListViewData,"help","帮助");
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
        //layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
        layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
        llLayout.addView(titleViewAdapter.view,layoutParams);

		ImageView ivSetting = new ImageView(GlobalContext.get());
		ivSetting.setVisibility(ConfigUtil.isShowSettings()?View.VISIBLE:View.GONE);
		ivSetting.setImageDrawable(LayouUtil.getDrawable("setting_title"));
		RelativeLayout.LayoutParams rLayoutParams = new RelativeLayout.LayoutParams(ivSettingSize,ivSettingSize);
        rLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rTop.addView(ivSetting,rLayoutParams);
		ivSetting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RecordWin2Manager.getInstance().operateView(
						RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_SETTING,0,0);

				ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("setting").setType("touch_voice_center")
						.putExtra("style",ConfigUtil.isShowHelpInfos()?"help":"setting").setSessionId().buildCommReport());
			}
		});

		TextView tvTittle = new TextView(GlobalContext.get());
		tvTittle.setGravity(Gravity.CENTER_VERTICAL);
		tvTittle.setText(LanguageConvertor.toLocale("帮助"));
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
				LogUtil.logd(WinLayout.logTag+ "onClick: back");
				TXZAsrManager.getInstance().restart("");
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

//		int width = (int) LayouUtil.getDimen("x582");
//		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.HORIZONTAL);
//		llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
//		llContents.setLayoutParams(params);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		layoutParams.rightMargin = (int) LayouUtil.getDimen("x8");
		LinearLayout helpContentLayout = new LinearLayout(GlobalContext.get());
		helpContentLayout.setOrientation(LinearLayout.HORIZONTAL);
		helpContentLayout.setBackground(LayouUtil.getDrawable("white_range_layout"));
		helpContentLayout.setLayoutParams(layoutParams);
		llContents.addView(helpContentLayout);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		helpContentLayout.addView(llContent,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		helpContentLayout.addView(llPager,layoutParams);
		//二维码
		if(!TextUtils.isEmpty(helpListViewData.qrCodeUrl)){
			llContents.addView(createQRCodeLayout(helpListViewData.qrCodeTitleIcon,helpListViewData.qrCodeTitle,
					helpListViewData.qrCodeUrl, helpListViewData.qrCodeDesc,helpListViewData.qrCodeNeedShowGuide,helpListViewData.qrCodeGuideDesc));
		}

		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, contentHeight);
		layoutParams.setMargins(contentHorMargin, 0, contentHorMargin, 0);
        llLayout.addView(llContents,layoutParams);

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
		//mCurFocusIndex = -1;
		for (int i = 0; i < helpListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHelpHeight);
			View itemView = createItemView(i,helpListViewData.getData().get(i),i != SizeConfig.pageHelpCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		//再加一层tips的显示
		TextView tvTips = new TextView(GlobalContext.get());
		tvTips.setGravity(Gravity.CENTER);
		tvTips.setBackground(LayouUtil.getDrawable("white_help_tip_layout"));

		tvTips.setPadding(15,0,15,0);

        FrameLayout flContent = new FrameLayout(GlobalContext.get());
        FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		flParams.leftMargin = 15;
		flParams.rightMargin = 15;
		flParams.gravity = Gravity.CENTER;
		TextViewUtil.setTextSize(tvTips, ViewConfiger.SIZE_HELP_ITEM_SIZE1);
		TextViewUtil.setTextColor(tvTips,ViewConfiger.COLOR_HELP_ITEM_COLOR1);
		if (!TextUtils.isEmpty(tips)) {
			tvTips.setText(LanguageConvertor.toLocale(tips));
		}
		if (isShowTips) {
			tvTips.setVisibility(View.VISIBLE);
			UI2Manager.runOnUIThread(new Runnable1<TextView>(tvTips) {
				@Override
				public void run() {
					mP1.setVisibility(View.GONE);
				}
			},3000);
		} else {
			tvTips.setVisibility(View.GONE);
		}
		flContent.addView(tvTips,flParams);


        TextViewUtil.setTextSize(tvTittle,tvHelpSize);
        TextViewUtil.setTextColor(tvTittle,tvHelpColor);
        TextViewUtil.setTextSize(tvBack,tvBackSize);
        TextViewUtil.setTextColor(tvBack,tvBackColor);
		return llLayout;
	}

    private View createVerticalViewFull(HelpListViewData helpListViewData){
        canOpenDetail = helpListViewData.canOpenDetail;
        isShowTips = helpListViewData.isShowTips;
        tips = helpListViewData.tips;

        mCurPage = helpListViewData.mTitleInfo.curPage;
        mMaxPage = helpListViewData.mTitleInfo.maxPage;

        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout rTop = new RelativeLayout(GlobalContext.get());
        rTop.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,rlTopHeight);
        layoutParams.setMargins(rlTopLeftMargin,0,rlTopRightMargin,0);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(rTop,layoutParams);

        /*TextView tvTitleView = new TextView(GlobalContext.get());
        tvTitleView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        String text = helpListViewData.mTitleInfo.prefix;
        LogUtil.logd(WinLayout.logTag+ "helpListViewData.mTitleInfo.prefix: "+helpListViewData.mTitleInfo.prefix);
        TextViewUtil.setTextSize(tvTitleView,tvDescSize);
        TextViewUtil.setTextColor(tvTitleView,tvDescColor);
        int startIdx = text.indexOf("“")+1;
        int endIdx = text.indexOf("”");
        if (endIdx != -1){
			SpannableString textString = new SpannableString(text);
			textString.setSpan(new ForegroundColorSpan(Color.parseColor("#16CAF9")),startIdx,endIdx,Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			tvTitleView.setText(textString);
		}else {
            tvTitleView.setText(text);
        }*/

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

        ImageView ivSetting = new ImageView(GlobalContext.get());
        ivSetting.setVisibility(ConfigUtil.isShowSettings()?View.VISIBLE:View.GONE);
        ivSetting.setImageDrawable(LayouUtil.getDrawable("setting_title"));
        RelativeLayout.LayoutParams rLayoutParams = new RelativeLayout.LayoutParams(ivSettingSize,ivSettingSize);
        rLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rTop.addView(ivSetting,rLayoutParams);
        ivSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RecordWin2Manager.getInstance().operateView(
                        RecordWinController.OPERATE_CLICK,
                        RecordWinController.VIEW_SETTING,0,0);

                ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("setting").setType("touch_voice_center")
                        .putExtra("style",ConfigUtil.isShowHelpInfos()?"help":"setting").setSessionId().buildCommReport());
            }
        });

        TextView tvTittle = new TextView(GlobalContext.get());
        tvTittle.setGravity(Gravity.CENTER_VERTICAL);
        tvTittle.setText(LanguageConvertor.toLocale("帮助"));
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
                LogUtil.logd(WinLayout.logTag+ "onClick: back");
                TXZAsrManager.getInstance().restart("");
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

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setLayoutParams(layoutParams);
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        llContents.addView(llContent,layoutParams);

        LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
        //llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
        llContents.addView(llPager,layoutParams);

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
        layoutParams.setMargins(contentHorMargin,0,contentHorMargin,0);
        //llLayout.addView(llContents,layoutParams);
        llCenterContent.addView(llContents,layoutParams);

        //二维码
		if (!TextUtils.isEmpty(helpListViewData.qrCodeUrl)) {
			llCenterContent.addView(createVerticalQRCodeLayout(helpListViewData.qrCodeTitleIcon, helpListViewData.qrCodeTitle,
					helpListViewData.qrCodeUrl, helpListViewData.qrCodeDesc, helpListViewData.qrCodeNeedShowGuide,helpListViewData.qrCodeGuideDesc));
		}

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
        //mCurFocusIndex = -1;
        for (int i = 0; i < helpListViewData.count; i++) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHelpHeight);
            View itemView = createItemView(i,helpListViewData.getData().get(i),i != SizeConfig.pageHelpCount - 1);
            llContent.addView(itemView, layoutParams);
            mItemViews.add(itemView);
        }

        //再加一层tips的显示
        TextView tvTips = new TextView(GlobalContext.get());
        tvTips.setGravity(Gravity.CENTER);
        tvTips.setBackground(LayouUtil.getDrawable("white_help_tip_layout"));

        tvTips.setPadding(15,0,15,0);

        FrameLayout flContent = new FrameLayout(GlobalContext.get());
        FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        flParams.leftMargin = 15;
        flParams.rightMargin = 15;
        flParams.gravity = Gravity.CENTER;
        TextViewUtil.setTextSize(tvTips, ViewConfiger.SIZE_HELP_ITEM_SIZE1);
        TextViewUtil.setTextColor(tvTips,ViewConfiger.COLOR_HELP_ITEM_COLOR1);
        if (!TextUtils.isEmpty(tips)) {
            tvTips.setText(LanguageConvertor.toLocale(tips));
        }
        if (isShowTips) {
            tvTips.setVisibility(View.VISIBLE);
            UI2Manager.runOnUIThread(new Runnable1<TextView>(tvTips) {
                @Override
                public void run() {
                    mP1.setVisibility(View.GONE);
                }
            },3000);
        } else {
            tvTips.setVisibility(View.GONE);
        }
        flContent.addView(tvTips,flParams);


        TextViewUtil.setTextSize(tvTittle,tvHelpSize);
        TextViewUtil.setTextColor(tvTittle,tvHelpColor);
        TextViewUtil.setTextSize(tvBack,tvBackSize);
        TextViewUtil.setTextColor(tvBack,tvBackColor);
        return llLayout;
    }

	private View createViewNone(HelpListViewData helpListViewData){

		canOpenDetail = helpListViewData.canOpenDetail;
		isShowTips = helpListViewData.isShowTips;
		tips = helpListViewData.tips;

		mCurPage = helpListViewData.mTitleInfo.curPage;
		mMaxPage = helpListViewData.mTitleInfo.maxPage;

		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		llLayout.setLayoutParams(params);
		llLayout.setOrientation(LinearLayout.HORIZONTAL);
		//帮助界面
		LinearLayout helpContentLayout = new LinearLayout(GlobalContext.get());
		LinearLayout.LayoutParams helpContentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		helpContentLayout.setBackground(LayouUtil.getDrawable("bg_none"));
		helpContentLayout.setLayoutParams(helpContentParams);

		//不需要展示二维码
		if (TextUtils.isEmpty(helpListViewData.qrCodeUrl)) {
			llLayout.addView(helpContentLayout);
		} else {
			View view = createQRCodeLayout(helpListViewData.qrCodeTitleIcon,helpListViewData.qrCodeTitle,
					helpListViewData.qrCodeUrl, helpListViewData.qrCodeDesc, helpListViewData.qrCodeNeedShowGuide,helpListViewData.qrCodeGuideDesc);
			view.setBackground(LayouUtil.getDrawable("bg_none"));
			helpContentParams.width = (int) LayouUtil.getDimen("x500");
			if (FloatPointSP.getInstance().getX() > SizeConfig.screenWidth / 2) {
				helpContentParams.leftMargin = (int) LayouUtil.getDimen("x8");
				llLayout.setGravity(Gravity.RIGHT);
				llLayout.addView(view);
				llLayout.addView(helpContentLayout);
			}else{
				llLayout.setGravity(Gravity.LEFT);
				helpContentParams.rightMargin = (int) LayouUtil.getDimen("x8");
				llLayout.addView(helpContentLayout);
				llLayout.addView(view);
			}
		}

		LinearLayout llContents = new LinearLayout(GlobalContext.get());
		llContents.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		helpContentLayout.addView(llContents,layoutParams);

		LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
		//llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
		layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
		helpContentLayout.addView(llPager,layoutParams);

		RelativeLayout rlTitle = new RelativeLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight);
		llContents.addView(rlTitle,layoutParams);

		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(helpListViewData,"help_none","帮助");
		RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		rlTitle.addView(titleViewAdapter.view,rlLayoutParams);

		ImageView ivSetting = new ImageView(GlobalContext.get());
        ivSetting.setVisibility(ConfigUtil.isShowSettings()?View.VISIBLE:View.GONE);
		ivSetting.setImageDrawable(LayouUtil.getDrawable("setting_title_none"));
		rlLayoutParams = new RelativeLayout.LayoutParams(ivSettingSizeNone,ivSettingSizeNone);
		rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		rlTitle.addView(ivSetting,rlLayoutParams);
		ivSetting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RecordWin2Manager.getInstance().operateView(
						RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_SETTING,0,0);

				ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("setting").setType("touch_voice_center")
						.putExtra("style",ConfigUtil.isShowHelpInfos()?"help":"setting").setSessionId().buildCommReport());
			}
		});

		View divider = new View(GlobalContext.get());
		divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		llContents.addView(divider, layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
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
		//mCurFocusIndex = -1;
		for (int i = 0; i < helpListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.itemHelpHeight);
			View itemView = createItemView(i,helpListViewData.getData().get(i),i != SizeConfig.pageHelpCount - 1);
			llContent.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		//再加一层tips的显示
		TextView tvTips = new TextView(GlobalContext.get());
		tvTips.setGravity(Gravity.CENTER);
		tvTips.setBackground(LayouUtil.getDrawable("white_help_tip_layout"));

		tvTips.setPadding(15,0,15,0);
        FrameLayout flContent = new FrameLayout(GlobalContext.get());
        FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		flParams.leftMargin = 15;
		flParams.rightMargin = 15;
		flParams.gravity = Gravity.CENTER;
		TextViewUtil.setTextSize(tvTips, ViewConfiger.SIZE_HELP_ITEM_SIZE1);
		TextViewUtil.setTextColor(tvTips,ViewConfiger.COLOR_HELP_ITEM_COLOR1);
		if (!TextUtils.isEmpty(tips)) {
			tvTips.setText(LanguageConvertor.toLocale(tips));
		}
		if (isShowTips) {
			tvTips.setVisibility(View.VISIBLE);
			UI2Manager.runOnUIThread(new Runnable1<TextView>(tvTips) {
				@Override
				public void run() {
					mP1.setVisibility(View.GONE);
				}
			},3000);
		} else {
			tvTips.setVisibility(View.GONE);
		}
		flContent.addView(tvTips,flParams);

		return llLayout;
	}

	@Override
	public void init() {
		super.init();
		dividerHeight = 1;

        tvBackColor = Color.WHITE;
        tvHelpColor = Color.WHITE;
        tvContentColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvDescColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
	}

	//切换模式修改布局参数
	public void onUpdateParams(int styleIndex){
		int unit = ViewParamsUtil.unit;
		//rlTopHeight = 10 * unit;
		rlTopHeight = WinLayout.isVertScreen?(int)(SizeConfig.screenWidth * 0.15):10 * unit;
		rlTopLeftMargin = 3 * unit;
		rlTopRightMargin = 0;
		ivBackSize = 3 * unit;
		tvBackSize = ViewParamsUtil.h3;
		tvHelpSize = ViewParamsUtil.h3;
		ivSettingSize = 10 * unit;
		ivSettingSizeNone = 6 * unit;
		contentHorMargin = (WinLayout.isVertScreen?2:5) * unit;
		contentHeight = SizeConfig.itemHelpHeight * SizeConfig.pageHelpCount;

		ivIconSide = 6 * unit;
		ivIconHorMargin = 2 * unit;
		tvContentSize = ViewParamsUtil.h4;
		tvContentHeight = ViewParamsUtil.h4Height;
		centerInterval = ViewParamsUtil.centerInterval;
		tvDescSize = ViewParamsUtil.h6;
		tvDescHeight = ViewParamsUtil.h6Height;
		switch (styleIndex) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
//				initFull();
				if (WinLayout.isVertScreen){
					tvContentSize = ViewParamsUtil.h3;
					tvDescSize = ViewParamsUtil.h5;
				}else {
					tvContentSize = ViewParamsUtil.h5;
					tvDescSize = ViewParamsUtil.h7;
				}
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
//				initHalf();
				if (WinLayout.isVertScreen){
					tvContentSize = ViewParamsUtil.h3;
					tvDescSize = ViewParamsUtil.h5;
				}
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
//				initNone();
				break;
			default:
				break;
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


	private View createItemView(int position,HelpBean helpBean,boolean showDivider){

        LogUtil.logd(WinLayout.logTag+ "helpBean: "+helpBean.iconName+"--"+helpBean.isFromFile);

		RippleView itemView = new RippleView(GlobalContext.get());
		itemView.setTag(position);
		if (canOpenDetail) {
		    ListTitleView.getInstance().mItemViews = mItemViews;
            itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
			itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		}
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

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		llContent.setOrientation(LinearLayout.HORIZONTAL);
		llContent.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        itemView.addView(llContent, layoutParams);

		ImageView ivIcon = new ImageView(GlobalContext.get());
		ivIcon.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(ivIconSide,ivIconSide);
		mLLayoutParams.leftMargin = ivIconHorMargin;
		mLLayoutParams.rightMargin = ivIconHorMargin;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContent.addView(ivIcon,mLLayoutParams);

		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		llDetail.setOrientation(LinearLayout.VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContent.addView(llDetail,mLLayoutParams);

		FrameLayout flContent = new FrameLayout(GlobalContext.get());
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        llDetail.addView(flContent,mLLayoutParams);

		TextView tvContent = new TextView(GlobalContext.get());
		tvContent.setEllipsize(TruncateAt.END);
		tvContent.setSingleLine();
		tvContent.setGravity(Gravity.BOTTOM);
		FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,tvContentHeight);
        flLayoutParams.gravity = Gravity.BOTTOM;
        flContent.addView(tvContent,flLayoutParams);

		FrameLayout interval = new FrameLayout(GlobalContext.get());
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,centerInterval);
		llDetail.addView(interval,mLLayoutParams);

        FrameLayout flDesc = new FrameLayout(GlobalContext.get());
        mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        llDetail.addView(flDesc,mLLayoutParams);

		TextView tvDesc = new TextView(GlobalContext.get());
		tvDesc.setSingleLine();
		tvDesc.setEllipsize(TruncateAt.END);
        flLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,tvDescHeight);
        flLayoutParams.gravity = Gravity.TOP;
        flDesc.addView(tvDesc,flLayoutParams);

        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackground(LayouUtil.getDrawable("line"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);

		TextViewUtil.setTextSize(tvContent,tvContentSize);
		TextViewUtil.setTextColor(tvContent,tvContentColor);
		TextViewUtil.setTextSize(tvDesc,tvDescSize);
		TextViewUtil.setTextColor(tvDesc,tvDescColor);

		if (VersionManager.getInstance().isUseHelpNewTag()) {

			if (helpBean.isNew) {
				tvContent.setCompoundDrawablePadding((int) LayouUtil.getDimen("x4"));
				Drawable drawable = LayouUtil.getDrawable("ic_help_new");
				if (drawable != null) {
					int height = drawable.getIntrinsicHeight();
					float scale = (float) tvContent.getLineHeight()
							/ (float) height * 0.65f;
					drawable.setBounds(0, 0,
							(int) (drawable.getIntrinsicWidth() * scale),
							(int) (height * scale));
					tvContent.setCompoundDrawables(null, null, drawable, null);
				}
			}
			if (helpBean.isFromFile) {
				ImageLoader.getInstance().displayImage(
						"file://" + helpBean.iconName, new ImageViewAware(ivIcon));
			} else {
				ivIcon.setImageDrawable(LayouUtil.getDrawable(helpBean.iconName));
			}
			/*if (canOpenDetail) {
				ivArrow.setVisibility(View.VISIBLE);
			}else {
				ivArrow.setVisibility(View.GONE);
			}*/
		}else {
			ivIcon.setImageDrawable(LayouUtil.getDrawable(helpBean.iconName));
		}

		tvContent.setText(StringUtils.isEmpty(helpBean.title) ?"" : LanguageConvertor.toLocale(helpBean.title));
		tvDesc.setText(Html.fromHtml(LanguageConvertor.toLocale(helpBean.intro)));

		divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);

		return itemView;
	}

	@Override
	public void updateItemSelect(int index) {
		LogUtil.logd(WinLayout.logTag+ "helpList updateItemSelect " + index);
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

	/**
	 * 竖屏时的二维码
     * @param titleIcon 标题图标
     * @param title 标题
	 * @param qrCodeUrl
	 * @param qrCodeDesc
     * @param needShowGuide 是否展示引导
	 * @return
	 */
	private View createVerticalQRCodeLayout(String titleIcon, final String title, final String qrCodeUrl, final String qrCodeDesc, boolean needShowGuide, final String qrCodeGuideDesc) {
		//二维码展示 qrCode
		int qrCodeHeight = SizeConfig.itemHelpHeight;
		LinearLayout.LayoutParams qrCodeLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, qrCodeHeight);
		qrCodeLayoutParams.leftMargin = contentHorMargin;
		qrCodeLayoutParams.rightMargin = contentHorMargin;
		qrCodeLayoutParams.topMargin = (int) LayouUtil.getDimen("y3");
		LinearLayout qrCodeLayout = new LinearLayout(GlobalContext.get());
		qrCodeLayout.setOrientation(LinearLayout.HORIZONTAL);
		qrCodeLayout.setGravity(Gravity.CENTER_VERTICAL);
		qrCodeLayout.setLayoutParams(qrCodeLayoutParams);
		qrCodeLayout.setBackground(LayouUtil.getDrawable("white_range_layout"));

		qrCodeLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				JSONBuilder jsonBuilder = new JSONBuilder();
				jsonBuilder.put("title",title);
				jsonBuilder.put("url",qrCodeUrl);
				jsonBuilder.put("desc",qrCodeDesc);
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.qrcode",
						jsonBuilder.toBytes(), null);
			}
		});

		//img
//		int ivQRTitleWidth = (int) LayouUtil.getDimen("m57");
		int ivQRTitleWidth = 7 * ViewParamsUtil.unit;
		LinearLayout.LayoutParams ivTitleParams = new LinearLayout.LayoutParams(ivQRTitleWidth, ivQRTitleWidth);
		ivTitleParams.leftMargin = (int) (1.5 * ViewParamsUtil.unit);
		ImageView ivQRCodeTitle = new ImageView(GlobalContext.get());
		ivQRCodeTitle.setLayoutParams(ivTitleParams);
		if (!TextUtils.isEmpty(titleIcon)) {
			ImageLoader.getInstance().displayImage("file://" + titleIcon, ivQRCodeTitle);
		} else {
			ivQRCodeTitle.setImageDrawable(LayouUtil.getDrawable("win_help_play"));
		}

		qrCodeLayout.addView(ivQRCodeTitle);

		//text
		LinearLayout.LayoutParams tvQRCodeTitleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		tvQRCodeTitleParams.leftMargin = (int) (1.5 * ViewParamsUtil.unit);
		TextView tvQRCodeTitle = new TextView(GlobalContext.get());
		tvQRCodeTitle.setLayoutParams(tvQRCodeTitleParams);
		tvQRCodeTitle.setTextColor(Color.WHITE);
		tvQRCodeTitle.setText(LanguageConvertor.toLocale(title));
		tvQRCodeTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, ViewParamsUtil.h3);

		qrCodeLayout.addView(tvQRCodeTitle);

		//QRCODE 二维码
		int ivQRCodeWidth = 13 * ViewParamsUtil.unit;
		LinearLayout.LayoutParams ivQRCodeParams = new LinearLayout.LayoutParams(ivQRCodeWidth, ivQRCodeWidth);
		ivQRCodeParams.leftMargin = (int) LayouUtil.getDimen("x62");
		ivQRCode = new ImageView(GlobalContext.get());
		ivQRCode.setLayoutParams(ivQRCodeParams);
		try {
			ivQRCode.setImageBitmap(QRUtil.createQRCodeBitmap(qrCodeUrl, ivQRCodeWidth));
		} catch (WriterException e) {
			e.printStackTrace();
		}


		qrCodeLayout.addView(ivQRCode);

		if(needShowGuide){
			mGuideTask.update(ivQRCode,qrCodeUrl,qrCodeGuideDesc);
			ivQRCode.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
		}

		//二维码描述
		LinearLayout.LayoutParams qrCodeDescParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		qrCodeDescParams.leftMargin = (int) (1.5 * ViewParamsUtil.unit);
		TextView tvQRCodeDesc = new TextView(GlobalContext.get());
		tvQRCodeDesc.setLayoutParams(qrCodeDescParams);
		tvQRCodeDesc.setText(LanguageConvertor.toLocale(qrCodeDesc));
		tvQRCodeDesc.setTextColor(Color.WHITE);
		tvQRCodeDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX,ViewParamsUtil.h7);

		qrCodeLayout.addView(tvQRCodeDesc);
		return qrCodeLayout;
	}

    /**
     * 横屏和无屏
     * @param titleIcon 标题icon
     * @param title 标题
     * @param qrCodeUrl 二维码链接
     * @param qrCodeDesc 描述
     * @param needShowGuide 是否展示引导界面
     * @return
     */
	private View createQRCodeLayout(String titleIcon, final String title, final String qrCodeUrl, final String qrCodeDesc, boolean needShowGuide, String qrCodeGuideDesc){
		//二维码展示 qrCode
//		int qrCodeWidth = (int) LayouUtil.getDimen("x133");
		LinearLayout.LayoutParams qrCodeLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
//		qrCodeLayoutParams.leftMargin = (int) LayouUtil.getDimen("x8");
		LinearLayout qrCodeLayout = new LinearLayout(GlobalContext.get());
		qrCodeLayout.setOrientation(LinearLayout.VERTICAL);
		qrCodeLayout.setLayoutParams(qrCodeLayoutParams);
		qrCodeLayout.setBackground(LayouUtil.getDrawable("white_range_layout"));

		qrCodeLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				JSONBuilder jsonBuilder = new JSONBuilder();
				jsonBuilder.put("title",title);
				jsonBuilder.put("url",qrCodeUrl);
				jsonBuilder.put("desc",qrCodeDesc);
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.qrcode",
						jsonBuilder.toBytes(), null);
			}
		});

		//视频演示 标题
		int height;//
//		if (StyleConfig.getInstance().getSelectStyleIndex() == StyleConfig.STYLE_ROBOT_NONE_SCREES) {//无屏处理
//			height = SizeConfig.titleHeight;
//		} else {
//			height = (int) LayouUtil.getDimen("y48");
//		}
		height = SizeConfig.titleHeight;
		LinearLayout.LayoutParams qrCodeTitleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
		LinearLayout qrCodeTitleLayout = new LinearLayout(GlobalContext.get());
		int padding = (int) LayouUtil.getDimen("x16");
		qrCodeTitleLayout.setPadding(padding, 0, padding, 0);
		qrCodeTitleLayout.setLayoutParams(qrCodeTitleParams);
		qrCodeTitleLayout.setOrientation(LinearLayout.HORIZONTAL);
		qrCodeTitleLayout.setGravity(Gravity.CENTER);
		qrCodeTitleLayout.setBackgroundColor(Color.parseColor("#0CFFFFFF"));

		qrCodeLayout.addView(qrCodeTitleLayout);

		//img
		int ivQRTitleWidth = ViewParamsUtil.h2;
		LinearLayout.LayoutParams ivTitleParams = new LinearLayout.LayoutParams(ivQRTitleWidth, ivQRTitleWidth);
        ivTitleParams.rightMargin = ViewParamsUtil.unit;
		ImageView ivQRCodeTitle = new ImageView(GlobalContext.get());
		ivQRCodeTitle.setLayoutParams(ivTitleParams);
		if (!TextUtils.isEmpty(titleIcon)) {
			ImageLoader.getInstance().displayImage("file://" + titleIcon, ivQRCodeTitle);
		} else {
			ivQRCodeTitle.setImageDrawable(LayouUtil.getDrawable("win_help_play"));
		}

		qrCodeTitleLayout.addView(ivQRCodeTitle);
		//text
		TextView tvQRCodeTitle = new TextView(GlobalContext.get());
		tvQRCodeTitle.setTextColor(Color.WHITE);
		tvQRCodeTitle.setText(LanguageConvertor.toLocale(title));
		tvQRCodeTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, ViewParamsUtil.h6);

		qrCodeTitleLayout.addView(tvQRCodeTitle);

		//QRCode content
		LinearLayout.LayoutParams qrCodeContentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		LinearLayout qrCodeContentLayout = new LinearLayout(GlobalContext.get());
		qrCodeContentLayout.setLayoutParams(qrCodeContentParams);
		qrCodeContentLayout.setGravity(Gravity.CENTER);
		qrCodeContentLayout.setOrientation(LinearLayout.VERTICAL);

		qrCodeLayout.addView(qrCodeContentLayout);

		//QRCODE 二维码
		int ivQRCodeWidth = 13 * ViewParamsUtil.unit;
		LinearLayout.LayoutParams ivQRCodeParams = new LinearLayout.LayoutParams(ivQRCodeWidth, ivQRCodeWidth);
		final ImageView ivQRCode = new ImageView(GlobalContext.get());
		ivQRCode.setLayoutParams(ivQRCodeParams);
		try {
			ivQRCode.setImageBitmap(QRUtil.createQRCodeBitmap(qrCodeUrl, ivQRCodeWidth));
		} catch (WriterException e) {
			e.printStackTrace();
		}

		qrCodeContentLayout.addView(ivQRCode);
		if(needShowGuide){
			mGuideTask.update(ivQRCode,qrCodeUrl,qrCodeGuideDesc);
			ivQRCode.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
		}

		//二维码描述
		LinearLayout.LayoutParams qrCodeDescParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		qrCodeDescParams.topMargin = ViewParamsUtil.unit;
		TextView tvQRCodeDesc = new TextView(GlobalContext.get());
		tvQRCodeDesc.setLayoutParams(qrCodeDescParams);
		tvQRCodeDesc.setText(LanguageConvertor.toLocale(qrCodeDesc));
		tvQRCodeDesc.setTextColor(Color.WHITE);
		tvQRCodeDesc.setLineSpacing(2, 1);
		tvQRCodeDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, ViewParamsUtil.h7);

		qrCodeContentLayout.addView(tvQRCodeDesc);
		return qrCodeLayout;
	}

	Runnable3<ImageView,String,String> mGuideTask = new Runnable3<ImageView,String,String>(null,null,null) {
		@Override
		public void run() {
			mP1.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
			int[] loc = new int[2];
			mP1.getLocationOnScreen(loc);
			LogUtil.d("qrLayout ivQRCode loc 0:" + loc[0]);
			LogUtil.d("qrLayout ivQRCode loc 1:" + loc[1]);
			JSONBuilder jsonBuilder = new JSONBuilder();
			jsonBuilder.put("locationX", loc[0]);
			jsonBuilder.put("locationY", loc[1]);
			jsonBuilder.put("qrCodeUrl", mP2);
			jsonBuilder.put("qrCodeGuideDesc", mP3);
			if (StyleConfig.getInstance().getSelectStyleIndex() != StyleConfig.STYLE_ROBOT_FULL_SCREES &&
					StyleConfig.getInstance().getSelectStyleIndex() != StyleConfig.STYLE_ROBOT_HALF_SCREES) {
				jsonBuilder.put("screenType", TYPE_NONE_SCREEN);
			} else if(WinLayout.isVertScreen && StyleConfig.getInstance().getSelectStyleIndex() != StyleConfig.STYLE_ROBOT_NONE_SCREES) {
				jsonBuilder.put("screenType", TYPE_VERTICAL_SCREEN);
			} else {
				jsonBuilder.put("screenType", TYPE_FULL_SCREEN);
			}
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.qrcode.guide",
					jsonBuilder.toBytes(), null);
		}
	};
	ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener =new ViewTreeObserver.OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			if (ivQRCode != null) {
				ivQRCode.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
			}
			AppLogicBase.removeBackGroundCallback(mGuideTask);
			AppLogicBase.runOnBackGround(mGuideTask,150);
		}
	};


}
