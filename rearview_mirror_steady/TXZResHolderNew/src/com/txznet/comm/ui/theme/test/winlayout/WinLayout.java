package com.txznet.comm.ui.theme.test.winlayout;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.SkillfulReminding;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.mappoi.MapPoiPoiList;
import com.txznet.comm.ui.theme.test.view.AudioListView;
import com.txznet.comm.ui.theme.test.view.AuthorizationView;
import com.txznet.comm.ui.theme.test.view.BindDeviceView;
import com.txznet.comm.ui.theme.test.view.CallListView;
import com.txznet.comm.ui.theme.test.view.ChatFromSysView;
import com.txznet.comm.ui.theme.test.view.ChatShockView;
import com.txznet.comm.ui.theme.test.view.ChatSysHighlightView;
import com.txznet.comm.ui.theme.test.view.ChatSysInterruptView;
import com.txznet.comm.ui.theme.test.view.ChatToSysPartView;
import com.txznet.comm.ui.theme.test.view.ChatToSysView;
import com.txznet.comm.ui.theme.test.view.ChatWeatherView;
import com.txznet.comm.ui.theme.test.view.CinemaListView;
import com.txznet.comm.ui.theme.test.view.CompetitionDetailView;
import com.txznet.comm.ui.theme.test.view.ConstellationFortuneView;
import com.txznet.comm.ui.theme.test.view.ConstellationMatchingView;
import com.txznet.comm.ui.theme.test.view.LogoQrCodeView;
import com.txznet.comm.ui.theme.test.view.FlightListView;
import com.txznet.comm.ui.theme.test.view.FlightTicketListView;
import com.txznet.comm.ui.theme.test.view.HelpDetailImageView;
import com.txznet.comm.ui.theme.test.view.HelpDetailListView;
import com.txznet.comm.ui.theme.test.view.HelpListView;
import com.txznet.comm.ui.theme.test.view.ListTitleView;
import com.txznet.comm.ui.theme.test.view.PoiListView;
import com.txznet.comm.ui.theme.test.view.RecordView;
import com.txznet.comm.ui.theme.test.view.ReminderListView;
import com.txznet.comm.ui.theme.test.view.CompetitionView;
import com.txznet.comm.ui.theme.test.view.StyleListView;
import com.txznet.comm.ui.theme.test.view.TrainListView;
import com.txznet.comm.ui.theme.test.view.TrainTicketListView;
import com.txznet.comm.ui.theme.test.view.TtsListView;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutFull;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutFullH;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutFullV;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutHalf;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutNone;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.TXZFileConfigUtil;

public class WinLayout extends IWinLayout {

	private static WinLayout sInstance = new WinLayout();
	private FrameLayout mRootView;

	public static final String logTag = "[ResHolder3.0] ";    //????????????log??????
	public static boolean isVertScreen;    //??????????????????
	public String vTips;
	public int halfHeight;
    public boolean isHalfBottom;    //???????????????????????????????????????
    public boolean isVerticalFullBottom;    //???????????????????????????????????????????????????????????????
	public int skilledRemindViewHeight = 0;    //??????????????????????????????
	//????????????????????????
	public String chatToSysText;

	public static boolean isSearch = false;    //??????poi????????????
	public static boolean isHideView = false;    //?????????????????????view????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
	public static int targetView = -1;    //?????????view??????

	private WinLayout() {
	}

	public static WinLayout getInstance() {
		return sInstance;
	}

	private IWinLayout mWinLayoutImpl;

	/**
	 * ??????????????????
	 */
	@Override
	public void addRecordView(View recordView) {
		// ??????????????????????????????????????????View?????????
		LogUtil.logd(WinLayout.logTag+ "WinLayout addRecordView"+recordView);

		if (mWinLayoutImpl != null){
			mWinLayoutImpl.addRecordView(recordView);
		}

	}

	//????????????????????????
	public void onUpdateState(){
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                if (isVertScreen){
                    WinLayoutFullV.getInstance().showChatView();
                }else {
                    WinLayoutFullH.getInstance().showChatView();
                }
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                WinLayoutHalf.getInstance().showChatView();
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                WinLayoutNone.getInstance().showChatView();
                break;
            default:
                break;
        }
    }

	@Override
	public Object removeLastView() {
		mWinLayoutImpl.removeLastView();
		return null;
	}

	@Override
	public View get() {
		LogUtil.logd(WinLayout.logTag+ "WinLayout get");

		return mRootView;
	}

	@Override
	public void setBackground(Drawable drawable) {
		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.setBackground(drawable);
		}
	}

	@Override
	public void setBannerAdvertisingView(View view) {
		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.setBannerAdvertisingView(view);
		}
	}

	@Override
	public void removeBannerAdvertisingView() {
		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.removeBannerAdvertisingView();
		}
	}

	/**
	 * ??????View??????????????????
	 */
	@Override
	public Object addView(int targetView, View view,
			ViewGroup.LayoutParams layoutParams) {

        this.targetView = targetView;
        Object tag = view.getTag();
        if (tag instanceof Integer && (Integer)tag == ViewData.TYPE_FULL_LIST_MAPPOI){
                this.targetView = TXZRecordWinManager.RecordWin2.RecordWinController.TARGET_CONTENT_CHAT;
        }

		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.addView(targetView, view, layoutParams);
		}
		return null;
	}



	/**
	 * ???????????????????????????????????????
	 */
	@Override
	public void release() {
	}

	/**
	 * ?????????????????????????????????????????????
	 */
	@Override
	public void reset() {
		LogUtil.logd(WinLayout.logTag+ "WinLayout reset");

		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.reset();
		}
		WinLayout.getInstance().vTips = null;
	}

	@Override
	public void init() {
		LogUtil.logd(WinLayout.logTag+ "WinLayout init");
        //???????????????????????????
        SizeConfig.getInstance().initScreenSize();
        //?????????????????????????????????
        TXZRecordWinManager.getInstance().enableFullScreen(true);
		/*SizeConfig.getInstance().initScreenSize();
		isVertScreen = (double)SizeConfig.screenWidth / (double) SizeConfig.screenHeight <= 1.325 && SizeConfig.screenHeight > 600;
		LogUtil.logd(WinLayout.logTag+ "initScreen----screenWidth: " + SizeConfig.screenWidth+"--screenHeight:"+SizeConfig.screenHeight+"--isVertScreen:"+isVertScreen);*/


		/*
		 * ??????????????? onStyleUpdate ??????????????? mWinLayoutImpl ????????? createLayout(); if
		 * (mWinLayoutImpl == null) { mWinLayoutImpl =
		 * WinLayoutFullH.getInstance(); //
		 * RecordWin2True.mAllowOutSideClickSentToBehind = false; //
		 * RecordWin2True.mDialogCanceledOnTouchOutside = true;
		 * 
		 * LogUtil.logd("WinLayout:"+"init weightRecord:"+mWinLayoutImpl.getClass
		 * ().getName()); mWinLayoutImpl.init();
		 * 
		 * ViewParent viewParent = mWinLayoutImpl.get().getParent(); if
		 * (viewParent!=null) { if (viewParent instanceof ViewGroup) {
		 * ((ViewGroup)viewParent).removeView(mWinLayoutImpl.get()); } }
		 * 
		 * mRootView.removeAllViews(); mRootView.addView(mWinLayoutImpl.get(),
		 * new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
		 * ViewGroup.LayoutParams.MATCH_PARENT)); }
		 */

	}

	private synchronized void createLayout() {
		if (mRootView == null) {
			mRootView = new FrameLayout(GlobalContext.get());
		}
	}

	public void onStyleUpdate(int styleIndex) {
		LogUtil.logd(WinLayout.logTag+ "onStyleUpdate " + styleIndex);

		//???????????????????????????
		SizeConfig.getInstance().initScreenSize();
		halfHeight = SizeConfig.screenHeight / 5;
		// if(mCurrentScreenType == styleIndex)
		// return;
		// mCurrentScreenType = styleIndex;

		//String str = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_IS_HALF_VIEW_ON_BOTTOM);
        isHalfBottom = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_IS_HALF_VIEW_ON_BOTTOM,true);
		isVerticalFullBottom = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_IS_VERTICAL_FULL_VIEW_ON_BOTTOM,true);
		LogUtil.logd(WinLayout.logTag+ "isHalfBottom: "+ isHalfBottom + "--isVerticalFullBottom:" + isVerticalFullBottom);

		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.reset();
			mWinLayoutImpl.release();
		}

		switch (styleIndex) {
		case StyleConfig.STYLE_ROBOT_FULL_SCREES:
			mWinLayoutImpl = WinLayoutFull.getInstance();
			updateScreenType(StyleConfig.STYLE_ROBOT_FULL_SCREES);
			break;
		case StyleConfig.STYLE_ROBOT_HALF_SCREES:
			mWinLayoutImpl = WinLayoutHalf.getInstance();
			updateScreenType(StyleConfig.STYLE_ROBOT_HALF_SCREES);
			break;
		case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			mWinLayoutImpl = WinLayoutNone.getInstance();
			updateScreenType(StyleConfig.STYLE_ROBOT_NONE_SCREES);
			break;
		default:
			mWinLayoutImpl = WinLayoutFull.getInstance();
			updateScreenType(StyleConfig.STYLE_ROBOT_FULL_SCREES);
			break;
		}

		LogUtil.logd(WinLayout.logTag+"onStyleUpdate:" + "init weightRecord:"
				+ mWinLayoutImpl.getClass().getName());
		RecordView.getInstance().onUpdateAnim();
		mWinLayoutImpl.init();

		ViewParent viewParent = mWinLayoutImpl.get().getParent();
		if (viewParent != null) {
			if (viewParent instanceof ViewGroup) {
				((ViewGroup) viewParent).removeView(mWinLayoutImpl.get());
			}
		}

		createLayout();

		mRootView.removeAllViews();
		mRootView.addView(mWinLayoutImpl.get(), new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		//????????????????????????
        AudioListView.getInstance().onUpdateParams(styleIndex);
        AuthorizationView.getInstance().onUpdateParams(styleIndex);
        CallListView.getInstance().onUpdateParams(styleIndex);
        ChatFromSysView.getInstance().onUpdateParams(styleIndex);
        ChatShockView.getInstance().onUpdateParams(styleIndex);
        ChatSysInterruptView.getInstance().onUpdateParams(styleIndex);
        ChatToSysPartView.getInstance().onUpdateParams(styleIndex);
        ChatToSysView.getInstance().onUpdateParams(styleIndex);
        ChatWeatherView.getInstance().onUpdateParams(styleIndex);
        CinemaListView.getInstance().onUpdateParams(styleIndex);
        FlightListView.getInstance().onUpdateParams(styleIndex);
        HelpDetailImageView.getInstance().onUpdateParams(styleIndex);
        HelpDetailListView.getInstance().onUpdateParams(styleIndex);
        HelpListView.getInstance().onUpdateParams(styleIndex);
        ListTitleView.getInstance().onUpdateParams(styleIndex);
        MapPoiPoiList.getInstance().onUpdateParams(styleIndex);
		PoiListView.getInstance().onUpdateParams(styleIndex);
        ReminderListView.getInstance().onUpdateParams(styleIndex);
        StyleListView.getInstance().onUpdateParams(styleIndex);
        TrainListView.getInstance().onUpdateParams(styleIndex);
        TtsListView.getInstance().onUpdateParams(styleIndex);
		BindDeviceView.getInstance().onUpdateParams(styleIndex);
		ConstellationFortuneView.getInstance().onUpdateParams(styleIndex);
		ConstellationMatchingView.getInstance().onUpdateParams(styleIndex);
		CompetitionView.getInstance().onUpdateParams(styleIndex);
		CompetitionDetailView.getInstance().onUpdateParams(styleIndex);
		TrainTicketListView.getInstance().onUpdateParams(styleIndex);
		FlightTicketListView.getInstance().onUpdateParams(styleIndex);
		ChatSysHighlightView.getInstance().onUpdateParams(styleIndex);
		LogoQrCodeView.getInstance().onUpdateParams(styleIndex);
	}

	/*
	 * public static final int LAYOUT_NORMAL_SCREEN = 1;
	 * 
	 * 
	 * public static final int LAYOUT_NONE_SCREEN = 2;
	 */

	// public int mCurrentScreenType;

	/**
	 * @param type
	 */
	public void updateScreenType(int type) {
		switch (type) {
		case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			SkillfulReminding.getInstance().useSkillful();
			RecordWin2Manager.getInstance().updateDisplayArea(0, 0, 1, 1);
			break;
		case StyleConfig.STYLE_ROBOT_FULL_SCREES:
			RecordWin2Manager.getInstance().updateDisplayArea(0, 0,
					WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.MATCH_PARENT);
			break;
		case StyleConfig.STYLE_ROBOT_HALF_SCREES:
			if (!isHalfBottom){    //??????????????????
				RecordWin2Manager.getInstance().updateDisplayArea(0,0,
						WindowManager.LayoutParams.MATCH_PARENT, halfHeight + skilledRemindViewHeight);
			}else {    //??????????????????
				RecordWin2Manager.getInstance().updateDisplayArea(0,
                        SizeConfig.screenHeight - halfHeight - skilledRemindViewHeight,
						WindowManager.LayoutParams.MATCH_PARENT, halfHeight + skilledRemindViewHeight);
			}
			break;
		default:
			break;
		}
	}



	/**
	 *????????????????????????
     * @param height ?????????????????????????????????
	 */
	public float recordScaling(int height){
	    float f = 1.0f;
        if (height <= 170){
            f = 0.9f;
        }else if (height <= 241){
            f = 0.8f;
        }else if (height <= 312){
            f = 0.7f;
        }else if (height <= 348){
            f = 0.6f;
        }else {
            f = 0.5f;
        }
        return f;
    }

}
