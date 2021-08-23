package com.txznet.comm.ui.theme.test.winlayout;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.dialog.IDialog;
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
import com.txznet.comm.ui.theme.test.view.FilmListView;
import com.txznet.comm.ui.theme.test.view.FlightListView;
import com.txznet.comm.ui.theme.test.view.FlightTicketListView;
import com.txznet.comm.ui.theme.test.view.HelpDetailImageView;
import com.txznet.comm.ui.theme.test.view.HelpDetailListView;
import com.txznet.comm.ui.theme.test.view.HelpListView;
import com.txznet.comm.ui.theme.test.view.ListTitleView;
import com.txznet.comm.ui.theme.test.view.MoviePhoneNumQRView;
import com.txznet.comm.ui.theme.test.view.MovieTheaterListView;
import com.txznet.comm.ui.theme.test.view.MovieTimeListView;
import com.txznet.comm.ui.theme.test.view.MovieWaitingPayQRView;
import com.txznet.comm.ui.theme.test.view.PoiListView;
import com.txznet.comm.ui.theme.test.view.RecordView;
import com.txznet.comm.ui.theme.test.view.ReminderListView;
import com.txznet.comm.ui.theme.test.view.CompetitionView;
import com.txznet.comm.ui.theme.test.view.StyleListView;
import com.txznet.comm.ui.theme.test.view.TicketPayView;
import com.txznet.comm.ui.theme.test.view.TrainListView;
import com.txznet.comm.ui.theme.test.view.TrainTicketListView;
import com.txznet.comm.ui.theme.test.view.TtsListView;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutNone2;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.theme.test.view.FeedbackView;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.TXZFileConfigUtil;

public class WinLayout extends IWinLayout {

	private static WinLayout sInstance = new WinLayout();
	private FrameLayout mRootView;

	public static final String logTag = "[ResHolder4.0] ";    //皮肤包的log标记
	public static boolean isVertScreen;    //当前是否竖屏
	public String vTips;
	public int halfHeight;
    public boolean isHalfBottom;    //半屏语音界面在底部还是顶部
    public boolean isVerticalFullBottom;    //竖屏全屏语音界面声控动画部分在底部还是顶部
	public int skilledRemindViewHeight = 0;    //熟手模式提醒框的高度
	//用户说的上一句话
	public String chatToSysText;

	public static boolean isSearch = false;    //是否poi列表查询
	public static int targetView = -1;    //上一个view类型

	private WinLayout() {
	}

	public static WinLayout getInstance() {
		return sInstance;
	}

	private WinLayoutNone2 mWinLayoutImpl;

	/**
	 * 添加录音图标
	 */
	@Override
	public void addRecordView(View recordView) {
		// 这种形式的录音动画直接在聊天View里面的
		LogUtil.logd(WinLayout.logTag+ "WinLayout addRecordView"+recordView);

		if (mWinLayoutImpl != null){
			mWinLayoutImpl.addRecordView(recordView);
		}

	}

	/**
	 * 响应界面打开状态
	 */
	public void openWin(){
		if(mWinLayoutImpl != null){
			mWinLayoutImpl.openWin();
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
	 * 添加View到对应的地方
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
	 * 释放内存，界面关闭时会调用
	 */
	@Override
	public void release() {

	}

	/**
	 * 重置聊天记录，界面关闭时会调用
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
        //初始化屏幕宽高数据
        SizeConfig.getInstance().initScreenSize();
        //设置语音界面起来时全屏
        TXZRecordWinManager.getInstance().enableFullScreen(true);




	}

	private synchronized void createLayout() {
		if (mRootView == null) {
			mRootView = new FrameLayout(GlobalContext.get());
		}
	}

	public void onStyleUpdate(int styleIndex) {
		LogUtil.logd(WinLayout.logTag+ "onStyleUpdate " + styleIndex);

		//初始化屏幕宽高数据
		SizeConfig.getInstance().initScreenSize();
		halfHeight = isVertScreen?SizeConfig.screenWidth / 4:SizeConfig.screenHeight / 5;
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

		mWinLayoutImpl = WinLayoutNone2.getInstance();


		LogUtil.logd(WinLayout.logTag+"onStyleUpdate:" + "init weightRecord:" + mWinLayoutImpl.getClass().getName());
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

		//更新界面布局参数
		ViewParamsUtil.getIntance().updateViewParams(WinLayout.isVertScreen);
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
		FeedbackView.getInstance().onUpdateParams(styleIndex);
		FilmListView.getInstance().onUpdateParams(styleIndex);
		MovieTheaterListView.getInstance().onUpdateParams(styleIndex);
		MovieTimeListView.getInstance().onUpdateParams(styleIndex);
		MoviePhoneNumQRView.getInstance().onUpdateParams(styleIndex);
		MovieWaitingPayQRView.getInstance().onUpdateParams(styleIndex);
        TrainTicketListView.getInstance().onUpdateParams(styleIndex);
        FlightTicketListView.getInstance().onUpdateParams(styleIndex);
		TicketPayView.getInstance().onUpdateParams(styleIndex);
		ChatSysHighlightView.getInstance().onUpdateParams(styleIndex);
		LogoQrCodeView.getInstance().onUpdateParams(styleIndex);
	}

	/**
	 *竖屏动画缩放比例
     * @param height 竖屏底部语音动画栏高度
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

	public void showDialog(IDialog dialog){
		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.showDialog(dialog);
		}
	}

	public void dismissDialog(IDialog dialog){
		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.dismissDialog(dialog);
		}
	}

	/**
	 * 更新录音界面状态
	 * @param state {@link com.txznet.comm.ui.viewfactory.view.IRecordView}
	 */
	public void onUpdateState(int state){
		if (mWinLayoutImpl != null) {
			mWinLayoutImpl.onUpdateState(state);
		}
	}

}
