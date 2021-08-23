package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.ViewTreeObserver;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.List;

import com.google.zxing.WriterException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.VersionManager;
import com.txznet.comm.ui.util.ConfigUtil;
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
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.txz.util.QRUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class HelpListView extends IHelpListView {

	private static HelpListView sInstance = new HelpListView();
	
	private View mView; //当前显示的View
	
	private List<View> mItemViews; 
	private int mCurFocusIndex;
	
	//字体等参数配置
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
	
	private float tvContentSize;
	private int tvContentColor;
	private float tvDescSize;
	private int tvDescColor;
	private boolean canOpenDetail;
	private boolean isShowTips;
	private String tips;

	private int TYPE_LOW_POWER = 0;
	private int TYPE_VERTICAL_SCREEN = 2;

	private int QRCODE_COUNT = 2;
	private static int pageHelpCount;
	
	private HelpListView() {
	}

	public static HelpListView getInstance(){
		return sInstance;
	}
	
	@Override
	public void updateProgress(int progress, int selection) {
		LogUtil.logd("updateProgress " + progress + "," + selection);
	}

	@Override
	public void release() {
		super.release();
//		LayoutInflater mLayoutInflater = LayoutInflater.from(context)
		if (mItemViews != null) {
			mItemViews.clear();
		}
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		HelpListViewData helpListViewData = (HelpListViewData) data;

		canOpenDetail = helpListViewData.canOpenDetail;
		isShowTips = helpListViewData.isShowTips;
		tips = helpListViewData.tips;

		ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(helpListViewData);
		
		LinearLayout llLayout = new LinearLayout(GlobalContext.get());
		llLayout.setGravity(Gravity.CENTER_VERTICAL);
		llLayout.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
		layoutParams.gravity = Gravity.CENTER_VERTICAL;
		llLayout.addView(titleViewAdapter.view,layoutParams);

		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false) * ConfigUtil.getVisbileCount());
		llLayout.addView(flContent,layoutParams);

		LinearLayout llContent = new LinearLayout(GlobalContext.get());
		LinearLayout helpContentLayout = new LinearLayout(GlobalContext.get());
		helpContentLayout.setOrientation(LinearLayout.VERTICAL);
		helpContentLayout.setBackground(LayouUtil.getDrawable("white_range_layout"));
		if(ScreenUtil.getScreenWidth()>ScreenUtil.getScreenHeight()){
			llContent.setOrientation(LinearLayout.HORIZONTAL);

			flContent.addView(llContent);
			//帮助界面
			layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT,1);
			llContent.addView(helpContentLayout,layoutParams);
			//二维码界面
			if(!TextUtils.isEmpty(helpListViewData.qrCodeUrl)){
				llContent.addView(createQRCodeLayout(helpListViewData.qrCodeTitleIcon,helpListViewData.qrCodeTitle,
						helpListViewData.qrCodeUrl, helpListViewData.qrCodeDesc, helpListViewData.qrCodeNeedShowGuide,helpListViewData.qrCodeGuideDesc));
			}
		}else{
			llContent.setOrientation(LinearLayout.VERTICAL);

			flContent.addView(llContent);
			//帮助界面
			layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			llContent.addView(helpContentLayout,layoutParams);
			//二维码界面
			if(!TextUtils.isEmpty(helpListViewData.qrCodeUrl)){
				llContent.addView(createVerticalQRCodeLayout(helpListViewData.qrCodeTitleIcon,helpListViewData.qrCodeTitle,
						helpListViewData.qrCodeUrl, helpListViewData.qrCodeDesc, helpListViewData.qrCodeNeedShowGuide,helpListViewData.qrCodeGuideDesc));
			}
		}

		helpContentLayout.setLayoutAnimation(ListViewItemAnim.getAnimationController());
		helpContentLayout.setLayoutAnimationListener(new AnimationListener() {
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
		mCurFocusIndex = -1;
		for (int i = 0; i < helpListViewData.count; i++) {
			layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ConfigUtil.getDisplayLvItemH(false));
			View itemView = createItemView(i,helpListViewData.getData().get(i),i != ConfigUtil.getVisbileCount() - 1);
			helpContentLayout.addView(itemView, layoutParams);
			mItemViews.add(itemView);
		}

		//再加一层tips的显示
		TextView tvTips = new TextView(GlobalContext.get());
		tvTips.setGravity(Gravity.CENTER);
		tvTips.setBackground(LayouUtil.getDrawable("white_help_tip_layout"));

		tvTips.setPadding(15,0,15,0);
		FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ConfigUtil.getDisplayLvItemH(false));
		flParams.leftMargin = 15;
		flParams.rightMargin = 15;
		flParams.gravity = Gravity.CENTER;
		TextViewUtil.setTextSize(tvTips, ViewConfiger.SIZE_HELP_ITEM_SIZE1);
		TextViewUtil.setTextColor(tvTips,ViewConfiger.COLOR_HELP_ITEM_COLOR1);
		if (!TextUtils.isEmpty(tips)) {
			tvTips.setText(tips);
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

		
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.type = data.getType();
		viewAdapter.view = llLayout;
		viewAdapter.isListView = true;
		viewAdapter.object = HelpListView.getInstance();
		return viewAdapter;
	}

	@Override
	public void init() {
		super.init();
		// 初始化配置，例如字体颜色等
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
		
		dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
		
		tvContentSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE1);
		tvContentColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR1);
		tvDescSize=(Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_HELP_ITEM_SIZE2);
		tvDescColor=(Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_HELP_ITEM_COLOR2);
		if(ScreenUtil.getScreenHeight()>ScreenUtil.getScreenWidth()){
			updatePageCount();
		}
	}

	/**
	 * 竖屏修改pageCount
	 */
	public void updatePageCount(){
		pageHelpCount = ConfigUtil.getVisbileCount();
		TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_HELP_LIST,pageHelpCount - QRCODE_COUNT);
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
		RippleView itemView = new RippleView(GlobalContext.get());
		itemView.setTag(position);
		if (canOpenDetail) {
			itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
		}
		itemView.setOnTouchListener(ListTitleView.getInstance().getOnTouchListener());
		FrameLayout flContent = new FrameLayout(GlobalContext.get());
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.topMargin = flContentMarginTop;
		layoutParams.bottomMargin = flContentMarginBottom;
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
		
		ImageView ivIcon = new ImageView(GlobalContext.get());
		ivIcon.setPadding(0, 0, 0, 0);
		LinearLayout.LayoutParams mLLayoutParams = new LinearLayout.LayoutParams(ivIconWidth,ivIconHeight);
		mLLayoutParams.leftMargin = tvNumMarginLeft;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llContent.addView(ivIcon,mLLayoutParams);
		
		LinearLayout llDetail = new LinearLayout(GlobalContext.get());
		llDetail.setGravity(Gravity.CENTER_VERTICAL);
		llDetail.setOrientation(LinearLayout.VERTICAL);
		mLLayoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
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
		
		TextView tvDesc = new TextView(GlobalContext.get());
		tvDesc.setSingleLine();
		tvDesc.setEllipsize(TruncateAt.END);
		mLLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//		mLLayoutParams.leftMargin = tvDescMarginLeft;
		mLLayoutParams.rightMargin = tvDescMarginRight;
		mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		llDetail.addView(tvDesc,mLLayoutParams);
		
		View divider = new View(GlobalContext.get());
		divider.setVisibility(View.GONE);
		divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
		layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		itemView.addView(divider, layoutParams);
		
		TextViewUtil.setTextSize(tvContent,tvContentSize);
		TextViewUtil.setTextColor(tvContent,tvContentColor);
		TextViewUtil.setTextSize(tvDesc,tvDescSize);
		TextViewUtil.setTextColor(tvDesc,tvDescColor);
		
		if (VersionManager.getInstance().isUseHelpNewTag()) {
			ImageView ivArrow = new ImageView(GlobalContext.get());
			ivArrow.setPadding(0, 0, 0, 0);
			mLLayoutParams = new LinearLayout.LayoutParams(ivIconWidth/2,ivIconHeight/2);
			mLLayoutParams.rightMargin = tvNumMarginLeft;
			mLLayoutParams.gravity = Gravity.CENTER_VERTICAL;
			ivArrow.setImageDrawable(LayouUtil.getDrawable("help_arrow"));
			llContent.addView(ivArrow,mLLayoutParams);
			
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
			if (canOpenDetail) {
				ivArrow.setVisibility(View.VISIBLE);
			}else {
				ivArrow.setVisibility(View.GONE);
			}
		}else {
			ivIcon.setImageDrawable(LayouUtil.getDrawable(helpBean.iconName));
		}
		

		
		tvContent.setText(StringUtils.isEmpty(helpBean.title) ?"" : LanguageConvertor.toLocale(helpBean.title));
		tvDesc.setText(Html.fromHtml(LanguageConvertor.toLocale(helpBean.intro)));

		// mPb.setVisibility(musicItem.shouldWaiting ? View.VISIBLE :
		// View.INVISIBLE);
		// mPb.setProgress(musicItem.shouldWaiting ? musicItem.curPrg : 0);

		divider.setVisibility(showDivider?View.VISIBLE:View.INVISIBLE);
		
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

	/**
	 * @param titleIcon 标题的icon
	 * @param title 标题
	 * @param qrCodeUrl 二维码url
	 * @param qrCodeDesc 描述
	 * @param needShowGuide 是否需要展示引导页
	 * @return
	 */
	private View createQRCodeLayout(String titleIcon, final String title,
									final String qrCodeUrl, final String qrCodeDesc, boolean needShowGuide, final String qrCodeGuideDesc) {
		//二维码展示 qrCode
		int qrCodeWidth = (int) LayouUtil.getDimen("x133");
		LinearLayout.LayoutParams qrCodeLayoutParams = new LinearLayout.LayoutParams(qrCodeWidth, ViewGroup.LayoutParams.MATCH_PARENT);
		qrCodeLayoutParams.leftMargin = (int) LayouUtil.getDimen("x8");
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
		int height = (int) LayouUtil.getDimen("y48");
		LinearLayout.LayoutParams qrCodeTitleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
		LinearLayout qrCodeTitleLayout = new LinearLayout(GlobalContext.get());
		qrCodeTitleLayout.setLayoutParams(qrCodeTitleParams);
		qrCodeTitleLayout.setOrientation(LinearLayout.HORIZONTAL);
		qrCodeTitleLayout.setGravity(Gravity.CENTER);
		qrCodeTitleLayout.setBackground(LayouUtil.getDrawable("white_range_qrcode_title_layout"));

		qrCodeLayout.addView(qrCodeTitleLayout);

		//img
		int ivQRTitleWidth = (int) LayouUtil.getDimen("m24");
		LinearLayout.LayoutParams ivTitleParams = new LinearLayout.LayoutParams(ivQRTitleWidth, ivQRTitleWidth);
		ivTitleParams.rightMargin = (int) LayouUtil.getDimen("y8");
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
		tvQRCodeTitle.setText(title);
		tvQRCodeTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,LayouUtil.getDimen("m18"));

		qrCodeTitleLayout.addView(tvQRCodeTitle);

		//QRCode content
		LinearLayout.LayoutParams qrCodeContentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		LinearLayout qrCodeContentLayout = new LinearLayout(GlobalContext.get());
		qrCodeContentLayout.setLayoutParams(qrCodeContentParams);
		qrCodeContentLayout.setGravity(Gravity.CENTER);
		qrCodeContentLayout.setOrientation(LinearLayout.VERTICAL);

		qrCodeLayout.addView(qrCodeContentLayout);

		//QRCODE 二维码
		int ivQRCodeWidth = (int) LayouUtil.getDimen("m104");
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
			ivQRCode.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
				@Override
				public void onGlobalLayout() {
					ivQRCode.getViewTreeObserver().removeOnGlobalLayoutListener(this);
					int[] loc = new int[2];
					ivQRCode.getLocationOnScreen(loc);
					LogUtil.d("qrLayout ivQRCode loc 0:" + loc[0]);
					LogUtil.d("qrLayout ivQRCode loc 1:" + loc[1]);
					JSONBuilder jsonBuilder = new JSONBuilder();
					jsonBuilder.put("screenType", TYPE_LOW_POWER);
					jsonBuilder.put("qrCodeUrl", qrCodeUrl);
					jsonBuilder.put("qrCodeGuideDesc", qrCodeGuideDesc);
					jsonBuilder.put("locationX", loc[0]);
					jsonBuilder.put("locationY", loc[1]);
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.qrcode.guide",
							jsonBuilder.toBytes(), null);
				}
			});
		}

		//二维码描述
		LinearLayout.LayoutParams qrCodeDescParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		qrCodeDescParams.topMargin = (int) LayouUtil.getDimen("y8");
		TextView tvQRCodeDesc = new TextView(GlobalContext.get());
		tvQRCodeDesc.setLayoutParams(qrCodeDescParams);
		tvQRCodeDesc.setText(qrCodeDesc);
		tvQRCodeDesc.setTextColor(Color.WHITE);
		tvQRCodeDesc.setLineSpacing(2, 1);
		tvQRCodeDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m16"));

		qrCodeContentLayout.addView(tvQRCodeDesc);
		return qrCodeLayout;
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
		LinearLayout.LayoutParams qrCodeLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		qrCodeLayoutParams.topMargin = (int) LayouUtil.getDimen("y3");
		LinearLayout qrCodeLayout = new LinearLayout(GlobalContext.get());
		qrCodeLayout.setOrientation(LinearLayout.HORIZONTAL);
		qrCodeLayout.setGravity(Gravity.CENTER_VERTICAL);
		qrCodeLayout.setLayoutParams(qrCodeLayoutParams);
		qrCodeLayout.setBackground(LayouUtil.getDrawable("white_range_layout"));
		qrCodeLayout.setPadding(0,10,0,10);

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
		int ivQRTitleWidth = (int) LayouUtil.getDimen("m57");
		LinearLayout.LayoutParams ivTitleParams = new LinearLayout.LayoutParams(ivQRTitleWidth, ivQRTitleWidth);
		ivTitleParams.leftMargin = (int) LayouUtil.getDimen("y13");
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
		tvQRCodeTitleParams.leftMargin = (int) LayouUtil.getDimen("y13");
		TextView tvQRCodeTitle = new TextView(GlobalContext.get());
		tvQRCodeTitle.setLayoutParams(tvQRCodeTitleParams);
		tvQRCodeTitle.setTextColor(Color.WHITE);
		tvQRCodeTitle.setText(title);
		tvQRCodeTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m22"));

		qrCodeLayout.addView(tvQRCodeTitle);

		//QRCODE 二维码
		int ivQRCodeWidth = (int) LayouUtil.getDimen("m105");
		LinearLayout.LayoutParams ivQRCodeParams = new LinearLayout.LayoutParams(ivQRCodeWidth, ivQRCodeWidth);
		ivQRCodeParams.leftMargin = (int) LayouUtil.getDimen("m34");
		final ImageView ivQRCode = new ImageView(GlobalContext.get());
		ivQRCode.setLayoutParams(ivQRCodeParams);
		try {
			ivQRCode.setImageBitmap(QRUtil.createQRCodeBitmap(qrCodeUrl, ivQRCodeWidth));
		} catch (WriterException e) {
			e.printStackTrace();
		}


		qrCodeLayout.addView(ivQRCode);

		if(needShowGuide){
			if(needShowGuide){
				ivQRCode.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						ivQRCode.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						int[] loc = new int[2];
						ivQRCode.getLocationOnScreen(loc);
						LogUtil.d("qrLayout ivQRCode loc 0:" + loc[0]);
						LogUtil.d("qrLayout ivQRCode loc 1:" + loc[1]);
						JSONBuilder jsonBuilder = new JSONBuilder();
						jsonBuilder.put("screenType", TYPE_VERTICAL_SCREEN);
						jsonBuilder.put("qrCodeUrl", qrCodeUrl);
						jsonBuilder.put("qrCodeGuideDesc", qrCodeGuideDesc);
						jsonBuilder.put("locationX", loc[0]);
						jsonBuilder.put("locationY", loc[1]);
						ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.qrcode.guide",
								jsonBuilder.toBytes(), null);
					}
				});
			}
		}

		//二维码描述
		LinearLayout.LayoutParams qrCodeDescParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		qrCodeDescParams.leftMargin = (int) LayouUtil.getDimen("y8");
		TextView tvQRCodeDesc = new TextView(GlobalContext.get());
		tvQRCodeDesc.setLayoutParams(qrCodeDescParams);
		tvQRCodeDesc.setText(qrCodeDesc);
		tvQRCodeDesc.setTextColor(Color.WHITE);
		tvQRCodeDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX,LayouUtil.getDimen("m16"));

		qrCodeLayout.addView(tvQRCodeDesc);
		return qrCodeLayout;
	}

	@Override
	public void updateItemSelect(int selection){

	}
}
