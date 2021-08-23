package com.txznet.comm.ui.theme.test.winlayout.inner;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.ConfigUtil.IconStateChangeListener;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.layout.layout1.ChatContentView;
import com.txznet.comm.ui.layout.layout1.FullContentView;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.SkillfulReminding;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.view.ChatFromSysView;
import com.txznet.comm.ui.theme.test.view.ChatToSysView;
import com.txznet.comm.ui.theme.test.view.HelpButtonView;
import com.txznet.comm.ui.theme.test.view.SkilledRemaindView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.txz.util.LanguageConvertor;

import java.util.Random;

/**
 * 横屏布局形式
 */
public class WinLayoutFullH extends IWinLayout {

	private static WinLayoutFullH instance = new WinLayoutFullH();
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

	public static int weightRecord;
	public static int weightContent;

	private int countMargin;    //对话内容左右外边距
	private int helpIconSize;    //语音中心图标大小
	private int helpLeftMargin;    //语音中心图标左边距
	private int helpBottomMargin;    //语音中心图标下边距
    private int tvHintTop;    //引导语到动画间隔
	private int tvHintSize;    //引导语字体大小
	private int tvHintColor;    //引导语颜色RGB值

    private int remindViewWidth;    //熟手模式提示框宽度
    private int remindViewHeight;    //熟手模式提示框高度

    private int chatTopMargin;    //第一条对话的上边距

	//初始化界面布局的相关数值
	private void initSize(){
		int unit = ViewParamsUtil.unit;
		countMargin = 2 * unit;
		helpIconSize = 5 * unit;
		helpLeftMargin = 5 * unit;
		helpBottomMargin = 4 * unit;

        tvHintTop = unit;
        tvHintSize = ViewParamsUtil.h6;
        tvHintColor = Color.parseColor(LayouUtil.getString("color_remind"));

        remindViewWidth = 80 * unit;
        remindViewHeight = 6 * unit;
		if (SizeConfig.screenWidth < 900 ||remindViewWidth > SizeConfig.screenWidth){
			remindViewWidth = SizeConfig.screenWidth;
		}

        chatTopMargin = (SizeConfig.screenHeight < 480?4:8) * unit;
	}

	public static void initWeight() {
		/*weightRecord = ConfigUtil.getRecordWeight();
		weightContent = ConfigUtil.getContentWeight();*/
		weightRecord = 1;
		weightContent = 3;
		LogUtil.logd(WinLayout.logTag+"initWeight:" + weightRecord + "," + weightContent);
	}

	public static WinLayoutFullH getInstance() {
		return instance;
	}

	@Override
	public View get() {
		//return mRootLayout;
		return fRootLayout;
	}

	@Override
	public void setBackground(Drawable drawable) {
		LogUtil.d("WinLayoutFullH setBackground.");
		if(drawable == null){
			get().setBackground(LayouUtil.getDrawable("bg"));
			return;
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

		Object tag = view.getTag();
		//上一个view是桌面提示指令时隐藏view
		//if (lastView != null && lastView.getTag() != null && ((Integer)lastView.getTag() == 23)){
		if (isRemovelastView){
			LogUtil.logd(WinLayout.logTag+ "removeHelpTips");
            isRemovelastView = false;
            mChatContent.removeLastView();
        }

		//view.setVisibility(View.GONE);
        LogUtil.logd(WinLayout.logTag+ "addView: tag:"+tag+"--targetView:"+targetView);
		switch (targetView) {
		case RecordWinController.TARGET_CONTENT_CHAT:
			if (tag instanceof Integer) {
				Integer type = (Integer) tag;
				switch (type) {
					case ViewData.TYPE_CHAT_WEATHER:
					case ViewData.TYPE_CHAT_SHARE:
					case ViewData.TYPE_CHAT_COMPETITION_DETAIL:
						mFullContent.reset();
						mFullContent.addView(view);
                        //此处只能设置为INVISIBLE，设置为GONE的话子view中对话列表可能有
                        //其他线程在操作（手动换行），会造成死锁。
						mChatContent.get().setVisibility(View.INVISIBLE);
						mFullContent.get().setVisibility(View.VISIBLE);
						if (skilledRemaindView != null){
							//隐藏熟手模式提示框
							skilledRemaindView.hide();
						}

						//显示引导语
						setmHintText();
						return null;
				}
			}

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
			mChatContent.addView(view);
			showRecord();

			break;
		case RecordWinController.TARGET_CONTENT_FULL:
			if (skilledRemaindView != null) {
				//隐藏熟手模式提示框
				skilledRemaindView.hide();
			}
			mFullContent.reset();
			mFullContent.addView(view);
			mChatContent.get().setVisibility(View.GONE);
			mFullContent.get().setVisibility(View.VISIBLE);
			//帮助界面需要全屏
			if ( tag != null && tag instanceof Integer) {
				Integer type = (Integer) tag;
				LogUtil.logd(WinLayout.logTag+ "winLayoutFullH.tag:"+type);
				switch (type) {
					case ViewData.TYPE_FULL_LIST_HELP:
					case ViewData.TYPE_FULL_LIST_HELP_DETAIL:
					case ViewData.TYPE_FULL_LIST_HELP_IMAGE_DETAIL:
						hideRecord();
				}
			}else{
				showRecord();
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

	@Override
	public void init() {
		super.init();
		//初始化页面列表数量
		SizeConfig.getInstance().init(4);
		initWeight();
		initSize();
		if(fRootLayout == null){
			LogUtil.logd(WinLayout.logTag+ "WinLayoutFullH:"+"init weightRecord:" + weightRecord + ",weightContent:" + weightContent);

			fRootLayout = new LinearLayout(GlobalContext.get());
			fRootLayout.setOrientation(LinearLayout.VERTICAL);
			fRootLayout.setBackground(LayouUtil.getDrawable("bg"));

			// 初始化一些配置等
			mRootLayout = new LinearLayout(GlobalContext.get());
			mParams = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.MATCH_PARENT);
			mRootLayout.setLayoutParams(mParams);
			mRootLayout.setOrientation(LinearLayout.HORIZONTAL);
			mRootLayout.setWeightSum(weightRecord + weightContent);

			skilledRemaindView = new SkilledRemaindView(GlobalContext.get());
			skilledRemaindView.setBackground(LayouUtil.getDrawable("white_bottom_range_layout"));
			LinearLayout.LayoutParams fLayoutParams = new LinearLayout.LayoutParams(remindViewWidth,remindViewHeight);
			fLayoutParams.gravity = Gravity.CENTER | Gravity.TOP;
			fRootLayout.addView(skilledRemaindView,fLayoutParams);

			fLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
			fRootLayout.addView(mRootLayout,fLayoutParams);

			// 声控
			mRecordRoot = new RelativeLayout(GlobalContext.get());
			LayoutParams layoutParams = new LayoutParams(0,LayoutParams.MATCH_PARENT,weightRecord);
			mRootLayout.addView(mRecordRoot, layoutParams);

			// 声控和引导语一起居中显示
			RelativeLayout rlRecord = new RelativeLayout(GlobalContext.get());
            RelativeLayout.LayoutParams  rllayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            rllayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mRecordRoot.addView(rlRecord, rllayoutParams);

			//声控动画
			mRecoderContent = new FrameLayout(GlobalContext.get());
			mRecoderContent.setId(ViewUtils.generateViewId());
			rllayoutParams = new RelativeLayout.LayoutParams((int) ViewParamsUtil.getDimen("x133"),(int) ViewParamsUtil.getDimen("x147"));
			rllayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            rlRecord.addView(mRecoderContent, rllayoutParams);

			//提示语
			mHintText1 = new TextView(GlobalContext.get());
			mHintText1.setId(ViewUtils.generateViewId());
			rllayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			rllayoutParams.topMargin = tvHintTop;
			rllayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rllayoutParams.addRule(RelativeLayout.BELOW,mRecoderContent.getId());
			rllayoutParams.addRule(Gravity.CENTER);
            rlRecord.addView(mHintText1, rllayoutParams);
			mHintText2 = new TextView(GlobalContext.get());
			mHintText2.setId(ViewUtils.generateViewId());
			rllayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			rllayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rllayoutParams.addRule(RelativeLayout.BELOW,mHintText1.getId());
			rllayoutParams.addRule(Gravity.CENTER);
            rlRecord.addView(mHintText2, rllayoutParams);
			/*mHintText3 = new TextView(GlobalContext.get());
			mHintText3.setId(ViewUtils.generateViewId());
			rllayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			rllayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rllayoutParams.addRule(RelativeLayout.BELOW,mHintText2.getId());
			rllayoutParams.addRule(Gravity.CENTER);
            rlRecord.addView(mHintText3, rllayoutParams);
			mHintText4 = new TextView(GlobalContext.get());
			mHintText4.setId(ViewUtils.generateViewId());
			rllayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			rllayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			rllayoutParams.addRule(RelativeLayout.BELOW,mHintText3.getId());
			rllayoutParams.addRule(Gravity.CENTER);
            rlRecord.addView(mHintText4, rllayoutParams);*/

			//帮助按钮
			mHelp = new HelpButtonView(GlobalContext.get(),helpIconSize,helpIconSize);
			rllayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
			rllayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
			rllayoutParams.leftMargin = helpLeftMargin;
			rllayoutParams.bottomMargin = helpBottomMargin;
			mRecordRoot.addView(mHelp,rllayoutParams);
            mHelp.showByConfig();

			//返回按钮
            ImageView backBtn = new ImageView(GlobalContext.get());
            backBtn.setBackground(LayouUtil.getDrawable("close_icon"));
            rllayoutParams = new RelativeLayout.LayoutParams(helpIconSize,helpIconSize);
            rllayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);;
            rllayoutParams.leftMargin = helpLeftMargin;
            rllayoutParams.topMargin = helpBottomMargin;
            mRecordRoot.addView(backBtn,rllayoutParams);
            backBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
                            RecordWinController.VIEW_CLOSE, 0, 0);
                }
            });

			// 内容
			mContentView = new FrameLayout(GlobalContext.get());
			mContentParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, weightContent);
			mContentParams.setMargins(countMargin,0,countMargin,0);
			mRootLayout.addView(mContentView, mContentParams);

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

            //注册帮助按钮状态监听，更新帮助按钮状态
            ConfigUtil.registerIconStateChangeListener(new IconStateChangeListener() {
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

	//全屏展示，去掉左侧动画
    public void hideRecord(){
		if (mRecordRoot.getVisibility() == View.VISIBLE){
			mRecordRoot.setVisibility(View.GONE);
			mContentParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mContentView.setLayoutParams(mContentParams);}
	}

	//右侧展示，显示左侧动画
	public void showRecord(){
		if (mRecordRoot.getVisibility() == View.GONE){
			mRecordRoot.setVisibility(View.VISIBLE);
			mContentParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, weightContent);
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
        chatNum = 0;

        if (skilledRemaindView != null && skilledRemaindView.getVisibility() == View.VISIBLE){
            SkillfulReminding.getInstance().reduceOnce();
            skilledRemaindView.hide();
        }
	}
	
	@Override
	public void addRecordView(View recordView) {
		LogUtil.logd(WinLayout.logTag+ "addRecordView");
	    if (recordView != null){
            ViewGroup parent = (ViewGroup) recordView.getParent();
            if (parent!= null){
                parent.removeView(recordView);
            }
            if (mRecoderContent != null) {
                mRecoderContent.removeAllViews();
                mRecoderContent.addView(recordView);
                showRecord();
            }
	    }
	}

	@Override
	public Object removeLastView() {
        LogUtil.logd(WinLayout.logTag+ "removeLastView: full");

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
		LogUtil.logd(WinLayout.logTag+ "release: fallH");

        isRemovelastView = false;
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
        mHintText2.setTypeface(Typeface.SERIF);
		if (WinLayout.getInstance().vTips != null){
			text = WinLayout.getInstance().vTips;
			boolean disQuotationMark = false;
			//带有~符号的引导语不需要加上““””引号
			if(text.contains("~")){
				String[] texts = text.split("~");
				text = texts[0];
				disQuotationMark = Boolean.valueOf(texts[1]);
			}
			if (text.contains("；")){
				String[] texts = text.split("；");
				for (int i = 0;i < texts.length;i++){
					texts[i] = "“" + texts[i] + "”";
				}
                Random random = new Random();
				switch (texts.length){
					case 1:
						mHintText1.setText(LanguageConvertor.toLocale(texts[0]));
						mHintText2.setText("");
						/*mHintText3.setText("");
						mHintText4.setText("");*/
						break;
					case 2:
						mHintText1.setText(LanguageConvertor.toLocale(texts[0]));
						mHintText2.setText(LanguageConvertor.toLocale(texts[1]));
						/*mHintText3.setText("");
						mHintText4.setText("");*/
						break;
					case 3:
                        int i = random.nextInt(2);
                        mHintText1.setText(LanguageConvertor.toLocale(texts[i]));
                        mHintText2.setText(LanguageConvertor.toLocale(texts[i+1]));
						/*mHintText3.setText(texts[2]);
						mHintText4.setText("");*/
                        break;
					case 4:
                        i = random.nextInt(3);
                        mHintText1.setText(LanguageConvertor.toLocale(texts[i]));
                        mHintText2.setText(LanguageConvertor.toLocale(texts[i+1]));
						/*mHintText3.setText(texts[2]);
						mHintText4.setText("");*/
						break;
					default:
						mHintText1.setText("");
						mHintText2.setText("");
						/*mHintText3.setText("");
						mHintText4.setText("");*/
						break;
				}
			}else {
				if(disQuotationMark){
					mHintText1.setText(LanguageConvertor.toLocale(text));
				}else {
					mHintText1.setText(LanguageConvertor.toLocale("“"+text+"”"));
				}
				mHintText2.setText(" ");
			}
		}else {
			mHintText1.setText(" ");
			mHintText2.setText(" ");
			/*mHintText3.setText(" ");
			mHintText4.setText(" ");*/
		}
            TextViewUtil.setTextSize(mHintText1,tvHintSize);
            TextViewUtil.setTextColor(mHintText1,tvHintColor);
            TextViewUtil.setTextSize(mHintText2,tvHintSize);
            TextViewUtil.setTextColor(mHintText2,tvHintColor);
            /*TextViewUtil.setTextSize(mHintText3,tvHintSize);
            TextViewUtil.setTextColor(mHintText3,tvHintColor);
            TextViewUtil.setTextSize(mHintText4,tvHintSize);
            TextViewUtil.setTextColor(mHintText4,tvHintColor);*/
		WinLayout.getInstance().vTips = null;
	}

}
