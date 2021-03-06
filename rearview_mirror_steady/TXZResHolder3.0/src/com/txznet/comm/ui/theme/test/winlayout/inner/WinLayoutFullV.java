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
 * ????????????
 * 
 */
public class WinLayoutFullV extends IWinLayout {
	private static WinLayoutFullV instance = new WinLayoutFullV();
	private LayoutParams mParams;

	private FrameLayout mContentView;
	private LayoutParams mContentParams;

	/**
	 * ?????????????????????
	 */
	private ChatContentView mChatContent;
	private FrameLayout.LayoutParams mChatContentParams;
	/**
	 * ?????????????????????
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
	 * ????????????
	 */
	private LinearLayout mBannerAdvertisingLayout;

	//?????????????????????view
	private boolean isRemovelastView;
	private int chatNum = 0;    //????????????

	public static int weightRecord;    //????????????????????????
	public static int weightHint;    //???????????????????????????
	public static int weightContent;    //????????????????????????

	private int countMargin;    //???????????????????????????
	private int helpIconSize;    //????????????????????????
	private int helpLeftMargin;    //??????????????????????????????
	//private int helpBottomMargin;    //???????????????????????????
	private int tvHintMargin;    //????????????????????????
	private int tvHintSize;    //?????????????????????
	private int tvHintHeight;    //?????????????????????
	private int tvHintColor;    //???????????????RGB???

	private int remindViewWidth;    //???????????????????????????
	private int remindViewHeight;    //???????????????????????????

	private int chatTopMargin;    //???????????????????????????

	//????????????????????????????????????
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

        //?????????view??????????????????????????????view
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
                //????????????????????????????????????????????????list???setSelection?????????
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
                //????????????????????????
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
                //??????????????????????????????
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
					//???????????????????????????
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

        //???????????????
        setmHintText();

        //?????????view???HelpTips???
        if(view.getTag() != null && (Integer)view.getTag() == 23){
            isRemovelastView = true;
        }
		return null;
	}

	@Override
	public void init() {
		super.init();
		//???????????????????????????
		SizeConfig.getInstance().init(4);
		initWeight();
		initSize();
		if(fRootLayout == null){
			LogUtil.logd(WinLayout.logTag+ "WinLayoutFullV:"+"init weightRecord:" + weightRecord + ",weightContent:" + weightContent);

			fRootLayout = new LinearLayout(GlobalContext.get());
			fRootLayout.setOrientation(LinearLayout.VERTICAL);
			fRootLayout.setBackground(LayouUtil.getDrawable("bg"));

			// ????????????????????????
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

			// ??????
			mContentView = new FrameLayout(GlobalContext.get());
			mContentParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0, weightContent);

			// ????????????
			mChatContent = new ChatContentView(GlobalContext.get());
			mChatContentParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			mContentView.addView(mChatContent.get(), mChatContentParams);

			// ?????????????????????
			mFullContent = new FullContentView(GlobalContext.get());
			mFullContentParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			mContentView.addView(mFullContent.get(), mFullContentParams);

			//banner??????
			mBannerAdvertisingLayout = new LinearLayout(GlobalContext.get());
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
			mBannerAdvertisingLayout.setLayoutParams(params);
			mBannerAdvertisingLayout.setVisibility(View.GONE);
			mContentView.addView(mBannerAdvertisingLayout);

            //?????????
            mHintText1 = new TextView(GlobalContext.get());
            mHintText1.setGravity(Gravity.CENTER);
            /*LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,0,weightHint);
            mRootLayout.addView(mHintText1, layoutParams);*/

			// ???????????????
			mRecordRoot = new RelativeLayout(GlobalContext.get());
			/*layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,0,weightRecord);
			mRootLayout.addView(mRecordRoot, layoutParams);*/

			//???????????????????????????????????????????????????
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

			//????????????
			mRecoderContent = new FrameLayout(GlobalContext.get());
			mRecoderContent.setId(ViewUtils.generateViewId());
            RelativeLayout.LayoutParams  rllayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.MATCH_PARENT);
            rllayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mRecordRoot.addView(mRecoderContent, rllayoutParams);

			//??????????????????????????????
			LinearLayout llHint = new LinearLayout(GlobalContext.get());
            llHint.setOrientation(LinearLayout.VERTICAL);
			rllayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            rllayoutParams.leftMargin = tvHintMargin;
			rllayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
			rllayoutParams.addRule(RelativeLayout.RIGHT_OF,mRecoderContent.getId());
			mRecordRoot.addView(llHint, rllayoutParams);

			//?????????
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

            //?????????
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

			//?????????????????????????????????????????????????????????
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

    //?????????????????????
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

	//?????????????????????????????????
	public void hideRecord(){
		if (mRecordRoot.getVisibility() == View.VISIBLE){
			mRecordRoot.setVisibility(View.GONE);
			mContentParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			mContentView.setLayoutParams(mContentParams);}
	}

	//?????????????????????????????????
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
        //??????helpTips????????????????????????????????????????????????????????????????????????
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

    //??????????????????
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
            if (text.contains("???")){
                String[] texts = text.split("???");
                Random random = new Random();
                switch (texts.length){
                    case 3:
                        int i = random.nextInt(2);
                        text = "??????????????????" + texts[i]+"?????????"+texts[i+1]+ "???";
                        break;
                    case 4:
                        i = random.nextInt(3);
                        text = "??????????????????" + texts[i]+"?????????"+texts[i+1]+ "???";
                        break;
                    case 1:
                    case 2:
                        text = "??????????????????" + text.replaceAll("???","?????????") + "???";
                        break;
                }
            }else {
				if(!disQuotationMark) {
					text = "??????????????????" + text + "???";
				}
			}
            mHintText1.setText(text);
            mHintText1.setVisibility(View.VISIBLE);
            //mRootLayout.setWeightSum(weightRecord + weightHint + weightContent);
            /*mContentParams = (LayoutParams) mContentView.getLayoutParams();
            //mContentParams.weight = weightContent;
            //?????????????????????listView???setSelection?????????????????????????????????????????????
            mContentParams.height = (int) (SizeConfig.screenHeight * (weightContent) / mRootLayout.getWeightSum());
            mContentView.setLayoutParams(mContentParams);*/

            TextViewUtil.setTextSize(mHintText1,tvHintSize);
            WinLayout.getInstance().vTips = null;
        }else {
            //mHintText1.setText(" ");
            mHintText1.setVisibility(View.GONE);   //??????????????????????????????
            //mRootLayout.setWeightSum(weightRecord + weightContent);
            /*mContentParams = (LayoutParams) mContentView.getLayoutParams();
            //mContentParams.weight = weightContent + weightHint;
            mContentParams.height = (int) (SizeConfig.screenHeight * (weightContent + weightHint) / mRootLayout.getWeightSum());
			mContentView.setLayoutParams(mContentParams);
            Log.d("jack", "setmHintText: "+mContentParams.height);*/
        }
    }

}
