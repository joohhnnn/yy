package com.txznet.comm.ui.theme.test.winlayout.inner;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.layout.layout1.ChatContentView;
import com.txznet.comm.ui.layout.layout1.FullContentView;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.SkillfulReminding;
import com.txznet.comm.ui.theme.test.config.VersionManager;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.view.ChatFromSysView;
import com.txznet.comm.ui.theme.test.view.ChatToSysView;
import com.txznet.comm.ui.theme.test.view.HelpButtonView;
import com.txznet.comm.ui.theme.test.view.RecordView;
import com.txznet.comm.ui.theme.test.view.SkilledRemaindView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.List;
import java.util.Random;

/**
 * 竖屏布局
 * 
 */
public class WinLayoutFullV extends IWinLayout {
	private static WinLayoutFullV instance = new WinLayoutFullV();
	private LayoutParams mParams;

	private FrameLayout mContentView;
	private LayoutParams mContentParams;

	/**
	 * 聊天形式的内容
	 */
	private ChatContentView mChatContent;
	private FrameLayout.LayoutParams mChatContentParams;
	/**
	 * 全屏形式的内容
	 */
	private LinearLayout mRootLayout;
	private FullContentView mFullContent;
	private FrameLayout.LayoutParams mFullContentParams;
	private RelativeLayout mRecordRoot;
	private FrameLayout mRecoderContent;
	private TextView mHintText1;
	private TextView mHintText2;
	/*private TextView mHintText3;
	private TextView mHintText4;*/
	private LayoutParams mRecoderContentParams;
	private HelpButtonView mHelp;

	LinearLayout fRootLayout;
	private SkilledRemaindView skilledRemaindView;
	/**
	 * 广告内容
	 */
	private LinearLayout mBannerAdvertisingLayout;

	//是否移除上一个view
	private boolean isRemovelastView;
	private int chatNum = 0;    //对话次数

	public static int weightRecord;    //动画部分布局权重
	public static int weightHint;    //引导语部分布局权重
	public static int weightContent;    //内容部分布局权重

	private int countMargin;    //对话内容左右外边距
	private int helpIconSize;    //语音中心图标大小
	private int helpLeftMargin;    //语音中心图标右左边距
	//private int helpBottomMargin;    //语音中心图标下边距
	private int tvHintMargin;    //引导语到动画间隔
	private int tvHintSize;    //引导语字体大小
	private int tvHintHeight;    //引导语布局高度
	private int tvHintColor;    //引导语颜色RGB值

	private int remindViewWidth;    //熟手模式提示框宽度
	private int remindViewHeight;    //熟手模式提示框高度

	private int chatTopMargin;    //第一条对话的上边距

	//初始化界面布局的相关数值
	private void initSize(){
		int unit = ViewParamsUtil.unit;
		countMargin = 2 * unit;
		helpIconSize = 5 * unit;
		helpLeftMargin = 3 * unit;
		//helpBottomMargin = 4 * unit;

		tvHintMargin = 2 * unit;
		tvHintSize = ViewParamsUtil.h6;
        tvHintHeight = SizeConfig.itemHeight * 4 / 50;
		tvHintColor = Color.parseColor(LayouUtil.getString("color_remind"));

		remindViewWidth = 80 * unit;
		remindViewHeight = 6 * unit;
		if (SizeConfig.screenWidth < 900 ||remindViewWidth > SizeConfig.screenWidth){
			remindViewWidth = SizeConfig.screenWidth;
		}

		chatTopMargin = 8 * unit;
	}

	public static void initWeight() {
		weightRecord = SizeConfig.screenWidth /4;
		weightHint = SizeConfig.screenWidth /20;
		weightContent = SizeConfig.screenHeight - weightRecord -weightHint;
		LogUtil.logd(WinLayout.logTag+"initWeight:" + weightRecord + "," + weightContent);
	}

	public static WinLayoutFullV getInstance() {
		return instance;
	}

	@Override
	public View get() {
		//return mRootLayout;
		return fRootLayout;
	}

	@Override
	public void setBackground(Drawable drawable) {
		LogUtil.d("WinLayoutFullV setBackground.");
		if(drawable == null){
			get().setBackground(LayouUtil.getDrawable("bg"));
		}
		get().setBackground(drawable);
	}

	@Override
	public void setBannerAdvertisingView(View view) {
		addView(RecordWinController.TARGET_VIEW_BANNER_AD, view);
	}

	@Override
	public void removeBannerAdvertisingView() {
		if(mBannerAdvertisingLayout != null){
			mBannerAdvertisingLayout.removeAllViews();
			mBannerAdvertisingLayout.setVisibility(View.GONE);
		}
	}

	@Override
	public Object addView(int targetView, final View view,ViewGroup.LayoutParams layoutParams) {

        //上一个view是桌面提示指令时隐藏view
        if (isRemovelastView){
            isRemovelastView = false;
            mChatContent.removeLastView();
        }

		Object tag = view.getTag();
        Integer type = null;
        if (tag instanceof Integer){
            type = (Integer) tag;
        }
        LogUtil.logd(WinLayout.logTag+ "winLayoutFullH.tag:"+type);

		switch (targetView) {
			case RecordWinController.TARGET_CONTENT_CHAT:
				if (skilledRemaindView != null){
					skilledRemaindView.show(remindViewHeight);
				}
				mChatContent.get().setVisibility(View.VISIBLE);
				mFullContent.get().setVisibility(View.GONE);
				mFullContent.reset();

				if(++chatNum == 1){
					//view.setTop(chatTopMargin);
					LogUtil.logd(WinLayout.logTag+ "addView: first");
					View spaceView = new View(GlobalContext.get());
					spaceView.setMinimumHeight(chatTopMargin);
					mChatContent.addView(spaceView);
				}
                //父布局更新时，需要延迟处理，否则list的setSelection会失效
				if(type != null && (type == ViewData.TYPE_CHAT_WEATHER || type == ViewData.TYPE_CHAT_SHARE ||type == ViewData.TYPE_CHAT_COMPETITION_DETAIL)){
                    AppLogicBase.runOnUiGround(new Runnable() {
                        @Override
                        public void run() {
                            mChatContent.addView(view);
                        }
                    },100);
				}else{
                    mChatContent.addView(view);
                }
				showRecord();
				break;
			case RecordWinController.TARGET_CONTENT_FULL:
                //帮助界面需要全屏
                if ( type != null) {
                    switch (type) {
                        case ViewData.TYPE_FULL_LIST_HELP:
                        case ViewData.TYPE_FULL_LIST_HELP_DETAIL:
                        case ViewData.TYPE_FULL_LIST_HELP_IMAGE_DETAIL:
                            hideRecord();
                            break;
                    }
                }else{
                    showRecord();
                }
                //电影列表在对话框显示
                /*if (type != null && type == ViewData.TYPE_FULL_LIST_CINEMA){
                    if (cinemaCount == 2){
                        mChatContent.removeLastView();
                        cinemaCount--;
                    }
                    mChatContent.get().setVisibility(View.VISIBLE);
                    mChatContent.addView(view);
                    mFullContent.get().setVisibility(View.GONE);
                    mFullContent.reset();
                }else {
                    mChatContent.get().setVisibility(View.GONE);
                    mFullContent.reset();
                    mFullContent.addView(view);
                    mFullContent.get().setVisibility(View.VISIBLE);
                }*/
                /*if (type != null && type == ViewData.TYPE_FULL_LIST_CINEMA){

                }*//*if (type != null && type == ViewData.TYPE_FULL_LIST_CINEMA){

                }*/
                mChatContent.get().setVisibility(View.GONE);
                mFullContent.reset();
                mFullContent.addView(view);
                mFullContent.get().setVisibility(View.VISIBLE);
				if (skilledRemaindView != null) {
					//隐藏熟手模式提示框
					skilledRemaindView.hide();
				}
				break;
			case RecordWinController.TARGET_VIEW_MIC:
				mRecoderContent.removeAllViews();
				mRecoderContent.addView(view);
				break;
			case RecordWinController.TARGET_VIEW_BANNER_AD:
				mBannerAdvertisingLayout.removeAllViews();
				mBannerAdvertisingLayout.setVisibility(View.VISIBLE);
				mBannerAdvertisingLayout.addView(view);
				break;
			default:
				break;
		}

        //显示引导语
        setmHintText();

        //上一个view是HelpTips时
        if(view.getTag() != null && (Integer)view.getTag() == 23){
            isRemovelastView = true;
        }
		return null;
	}

	@Override
	public void init() {
		super.init();
		//初始化页面列表数量
		SizeConfig.getInstance().init(4);
		initWeight();
		initSize();
		if(fRootLayout == null){
			LogUtil.logd(WinLayout.logTag+ "WinLayoutFullV:"+"init weightRecord:" + weightRecord + ",weightContent:" + weightContent);

			fRootLayout = new LinearLayout(GlobalContext.get());
			fRootLayout.setOrientation(LinearLayout.VERTICAL);
			fRootLayout.setBackground(LayouUtil.getDrawable("bg"));

			// 初始化一些配置等
			mRootLayout = new LinearLayout(GlobalContext.get());
			mParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			mRootLayout.setLayoutParams(mParams);
			mRootLayout.setOrientation(LinearLayout.VERTICAL);
			//mRootLayout.setWeightSum(weightRecord + weightHint + weightContent);

			skilledRemaindView = new SkilledRemaindView(GlobalContext.get());
			skilledRemaindView.setBackground(LayouUtil.getDrawable("white_bottom_range_layout"));
			LinearLayout.LayoutParams fLayoutParams = new LinearLayout.LayoutParams(remindViewWidth,remindViewHeight);
			fLayoutParams.gravity = Gravity.CENTER | Gravity.TOP;
			fRootLayout.addView(skilledRemaindView,fLayoutParams);

			fLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
			fRootLayout.addView(mRootLayout,fLayoutParams);

			// 内容
			mContentView = new FrameLayout(GlobalContext.get());
			mContentParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0, weightContent);

			// 聊天内容
			mChatContent = new ChatContentView(GlobalContext.get());
			mChatContentParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			mContentView.addView(mChatContent.get(), mChatContentParams);

			// 全屏显示的内容
			mFullContent = new FullContentView(GlobalContext.get());
			mFullContentParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			mContentView.addView(mFullContent.get(), mFullContentParams);

			//banner广告
			mBannerAdvertisingLayout = new LinearLayout(GlobalContext.get());
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
			mBannerAdvertisingLayout.setLayoutParams(params);
			mBannerAdvertisingLayout.setVisibility(View.GONE);
			mContentView.addView(mBannerAdvertisingLayout);

            //提示语
            mHintText1 = new TextView(GlobalContext.get());
            mHintText1.setGravity(Gravity.CENTER);
            /*LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,0,weightHint);
            mRootLayout.addView(mHintText1, layoutParams);*/

			// 底部声控栏
			mRecordRoot = new RelativeLayout(GlobalContext.get());
			/*layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,0,weightRecord);
			mRootLayout.addView(mRecordRoot, layoutParams);*/

			//根据配置文件确定语音动画部分的位置
			if (WinLayout.getInstance().isVerticalFullBottom){
                mContentParams.setMargins(countMargin,0,countMargin,0);
                mRootLayout.addView(mContentView, mContentParams);
                //LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,0,weightHint);
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,weightHint);
                mRootLayout.addView(mHintText1, layoutParams);
                //layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,0,weightRecord);
                layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,weightRecord);
                mRootLayout.addView(mRecordRoot, layoutParams);
            }else {
                //LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,0,weightRecord);
                LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,weightRecord);
                mRootLayout.addView(mRecordRoot, layoutParams);
                //layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,0,weightHint);
                layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,weightHint);
                mRootLayout.addView(mHintText1, layoutParams);
                mContentParams.setMargins(countMargin,0,countMargin,0);
                mRootLayout.addView(mContentView, mContentParams);
            }

			//声控动画
			mRecoderContent = new FrameLayout(GlobalContext.get());
			mRecoderContent.setId(ViewUtils.generateViewId());
            RelativeLayout.LayoutParams  rllayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
            rllayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mRecordRoot.addView(mRecoderContent, rllayoutParams);

			//提示语在语音动画右边
			LinearLayout llHint = new LinearLayout(GlobalContext.get());
            llHint.setOrientation(LinearLayout.VERTICAL);
			rllayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            rllayoutParams.leftMargin = tvHintMargin;
			rllayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			rllayoutParams.addRule(RelativeLayout.RIGHT_OF,mRecoderContent.getId());
			mRecordRoot.addView(llHint, rllayoutParams);

			//提示语
			/*mHintText1 = new TextView(GlobalContext.get());
			mHintText1.setId(ViewUtils.generateViewId());
            LinearLayout.LayoutParams lllayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            lllayoutParams.gravity = Gravity.LEFT;
            llHint.addView(mHintText1, lllayoutParams);
			mHintText2 = new TextView(GlobalContext.get());
			mHintText2.setId(ViewUtils.generateViewId());
            lllayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            lllayoutParams.gravity = Gravity.LEFT;
            llHint.addView(mHintText2, lllayoutParams);*/

            //返回键
            ImageView backBtn = new ImageView(GlobalContext.get());
            backBtn.setBackground(LayouUtil.getDrawable("close_icon"));
            rllayoutParams = new RelativeLayout.LayoutParams(helpIconSize,helpIconSize);
            rllayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            rllayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            rllayoutParams.leftMargin = helpLeftMargin;
            mRecordRoot.addView(backBtn,rllayoutParams);
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
                            RecordWinController.VIEW_CLOSE, 0, 0);
                }
            });

			mHelp = new HelpButtonView(GlobalContext.get(),helpIconSize,helpIconSize);
			rllayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
			rllayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			rllayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			rllayoutParams.rightMargin = helpLeftMargin;
			//rllayoutParams.bottomMargin = helpBottomMargin;
			mRecordRoot.addView(mHelp,rllayoutParams);
			mHelp.showByConfig();

			//注册帮助按钮状态监听，更新帮助按钮状态
			com.txznet.comm.remote.util.ConfigUtil.registerIconStateChangeListener(new com.txznet.comm.remote.util.ConfigUtil.IconStateChangeListener() {
				@Override
				public void onStateChanged(int i, boolean b) {
					UI2Manager.runOnUIThread(new Runnable() {
						@Override
						public void run() {
							mHelp.showByConfig();

						}
					},0);
				}
			});

		}else {
			if (mContentView != null) {
				mContentView.setPadding((int)ViewParamsUtil.getDimen("x24"), 0, (int)ViewParamsUtil.getDimen("x24"), 0);
			}
		}
	}

    //切换到对话界面
	public void showChatView(){
        if (skilledRemaindView != null){
            skilledRemaindView.show(remindViewHeight);
        }
        mChatContent.get().setVisibility(View.VISIBLE);
        mFullContent.get().setVisibility(View.GONE);
        mFullContent.reset();
		setmHintText();
        showRecord();
    }

	//全屏展示，去掉底部动画
	public void hideRecord(){
		if (mRecordRoot.getVisibility() == View.VISIBLE){
			mRecordRoot.setVisibility(View.GONE);
			mContentParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mContentView.setLayoutParams(mContentParams);}
	}

	//上面展示，显示底部动画
	public void showRecord(){
		if (mRecordRoot.getVisibility() == View.GONE){
			mRecordRoot.setVisibility(View.VISIBLE);
			mContentParams = new LayoutParams(LayoutParams.MATCH_PARENT,0 , weightContent);
			mContentParams.setMargins(countMargin,0,countMargin,0);
			mContentView.setLayoutParams(mContentParams);
		}
	}

	@Override
	public void reset() {
		LogUtil.logd(WinLayout.logTag+ "fullH reset: ");
		//ChatFromSysView.getInstance().removeRunnable();
		//ChatToSysView.getInstance().removeRunnable();
		if (mFullContent != null && mChatContent != null) {
			mFullContent.reset();
			mChatContent.reset();
		}
		if (mHintText1 != null && mHintText2 != null){
            mHintText1.setText(" ");
            mHintText2.setText(" ");
        }

		if (skilledRemaindView.getVisibility() == View.VISIBLE){
			SkillfulReminding.getInstance().reduceOnce();
			skilledRemaindView.hide();
		}

		chatNum = 0;
	}

	@Override
	public void addRecordView(View recordView) {
		if (recordView != null){
			ViewGroup parent = (ViewGroup) recordView.getParent();
			if (parent!= null){
				parent.removeView(recordView);
			}
			if (mRecoderContent != null) {
				mRecoderContent.removeAllViews();

                    float scaling = WinLayout.getInstance().recordScaling(WinLayout.getInstance().halfHeight);
                    int width = (int) (WinLayout.getInstance().halfHeight * scaling);
                    FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(width, width, Gravity.CENTER);

                    mRecoderContent.addView(recordView, layoutParam);
				//mRecoderContent.addView(recordView);
			}
		}
	}

	@Override
	public Object removeLastView() {
        //移除helpTips的回调有延时，此处不做处理，在用户说话后主动移除
        if (!isRemovelastView){
			//ChatFromSysView.getInstance().removeRunnable();
			//ChatToSysView.getInstance().removeRunnable();
            mChatContent.removeLastView();
        }
		return null;
	}

	@Override
	public void release() {
		if (mRecoderContent != null){
			mRecoderContent.removeAllViews();
			mRecoderContent = null;
		}
		if (fRootLayout != null){
			fRootLayout.removeAllViews();
			fRootLayout = null;
		}
	}

    //设置引导语语
    public void setmHintText(){
        String text;
        mHintText1.setTypeface(Typeface.SERIF);
        if (WinLayout.getInstance().vTips != null){
            text = WinLayout.getInstance().vTips;
			boolean disQuotationMark = false;
			if(text.contains("~")){
				String[] texts = text.split("~");
				text = texts[0];
				disQuotationMark = Boolean.valueOf(texts[1]);
			}
            if (text.contains("；")){
                String[] texts = text.split("；");
                Random random = new Random();
                switch (texts.length){
                    case 3:
                        int i = random.nextInt(2);
                        text = "你可以说：“" + texts[i]+"”，“"+texts[i+1]+ "”";
                        break;
                    case 4:
                        i = random.nextInt(3);
                        text = "你可以说：“" + texts[i]+"”，“"+texts[i+1]+ "”";
                        break;
                    case 1:
                    case 2:
                        text = "你可以说：“" + text.replaceAll("；","”，“") + "”";
                        break;
                }
            }else {
				if(!disQuotationMark) {
					text = "你可以说：“" + text + "”";
				}
			}
            mHintText1.setText(text);
            mHintText1.setVisibility(View.VISIBLE);
            //mRootLayout.setWeightSum(weightRecord + weightHint + weightContent);
            /*mContentParams = (LayoutParams) mContentView.getLayoutParams();
            //mContentParams.weight = weightContent;
            //更新权重会导致listView的setSelection不成功，因此采用更新高度的方式
            mContentParams.height = (int) (SizeConfig.screenHeight * (weightContent) / mRootLayout.getWeightSum());
            mContentView.setLayoutParams(mContentParams);*/

            TextViewUtil.setTextSize(mHintText1,tvHintSize);
            WinLayout.getInstance().vTips = null;
        }else {
            //mHintText1.setText(" ");
            mHintText1.setVisibility(View.GONE);   //隐藏内容，不保留布局
            //mRootLayout.setWeightSum(weightRecord + weightContent);
            /*mContentParams = (LayoutParams) mContentView.getLayoutParams();
            //mContentParams.weight = weightContent + weightHint;
            mContentParams.height = (int) (SizeConfig.screenHeight * (weightContent + weightHint) / mRootLayout.getWeightSum());
			mContentView.setLayoutParams(mContentParams);
            Log.d("jack", "setmHintText: "+mContentParams.height);*/
        }
    }

}
