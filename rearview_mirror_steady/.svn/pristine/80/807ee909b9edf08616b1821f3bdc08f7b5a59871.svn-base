package com.txznet.comm.ui.layout;

import java.lang.reflect.Method;
import java.util.LinkedList;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.keyevent.KeyEventManager;
import com.txznet.comm.ui.layout.layout1.TXZWinLayout1;
import com.txznet.comm.ui.layout.layout1.TXZWinLayout2;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.MsgViewBase;
import com.txznet.comm.ui.viewfactory.data.ChatSysInterruptTipsViewData;
import com.txznet.comm.ui.viewfactory.data.IFilmListView;
import com.txznet.comm.ui.viewfactory.data.IMoviePhoneNumQRView;
import com.txznet.comm.ui.viewfactory.data.IMovieSeatPlanView;
import com.txznet.comm.ui.viewfactory.data.IMovieTheaterView;
import com.txznet.comm.ui.viewfactory.data.IMovieTimeListView;
import com.txznet.comm.ui.viewfactory.data.IMovieWaitingPayQRView;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IAudioListView;
import com.txznet.comm.ui.viewfactory.view.IAuthorizationView;
import com.txznet.comm.ui.viewfactory.view.IBindDeviceView;
import com.txznet.comm.ui.viewfactory.view.ICallListView;
import com.txznet.comm.ui.viewfactory.view.IChatFromSysView;
import com.txznet.comm.ui.viewfactory.view.IChatMapView;
import com.txznet.comm.ui.viewfactory.view.IChatShockView;
import com.txznet.comm.ui.viewfactory.view.IChatSysHighlightView;
import com.txznet.comm.ui.viewfactory.view.IChatSysInterruptView;
import com.txznet.comm.ui.viewfactory.view.IChatToSysPartView;
import com.txznet.comm.ui.viewfactory.view.IChatToSysView;
import com.txznet.comm.ui.viewfactory.view.IChatWeatherView;
import com.txznet.comm.ui.viewfactory.view.ICinemaListView;
import com.txznet.comm.ui.viewfactory.view.ICompetitionDetailView;
import com.txznet.comm.ui.viewfactory.view.ICompetitionView;
import com.txznet.comm.ui.viewfactory.view.IConstellationFortuneView;
import com.txznet.comm.ui.viewfactory.view.IConstellationMatchingView;
import com.txznet.comm.ui.viewfactory.view.IFeedbackView;
import com.txznet.comm.ui.viewfactory.view.IFlightListView;
import com.txznet.comm.ui.viewfactory.view.IFlightTicketList;
import com.txznet.comm.ui.viewfactory.view.IFloatView;
import com.txznet.comm.ui.viewfactory.view.IHelpDetailImageView;
import com.txznet.comm.ui.viewfactory.view.IHelpDetailListView;
import com.txznet.comm.ui.viewfactory.view.IHelpListView;
import com.txznet.comm.ui.viewfactory.view.IHelpTipsView;
import com.txznet.comm.ui.viewfactory.view.IListTitleView;
import com.txznet.comm.ui.viewfactory.view.IListView;
import com.txznet.comm.ui.viewfactory.view.ILogoQrCodeView;
import com.txznet.comm.ui.viewfactory.view.IMapPoiListView;
import com.txznet.comm.ui.viewfactory.view.INavAppListView;
import com.txznet.comm.ui.viewfactory.view.INoTtsQrcodeView;
import com.txznet.comm.ui.viewfactory.view.IOfflinePromoteView;
import com.txznet.comm.ui.viewfactory.view.IPoiListView;
import com.txznet.comm.ui.viewfactory.view.IQrCodeView;
import com.txznet.comm.ui.viewfactory.view.IRecordView;
import com.txznet.comm.ui.viewfactory.view.IReminderListView;
import com.txznet.comm.ui.viewfactory.view.ISearchEditView;
import com.txznet.comm.ui.viewfactory.view.ISelectCityView;
import com.txznet.comm.ui.viewfactory.view.ISimListView;
import com.txznet.comm.ui.viewfactory.view.IStyleListView;
import com.txznet.comm.ui.viewfactory.view.ITicketPayView;
import com.txznet.comm.ui.viewfactory.view.ITrainListView;
import com.txznet.comm.ui.viewfactory.view.ITrainTicketList;
import com.txznet.comm.ui.viewfactory.view.ITtsListView;
import com.txznet.comm.ui.viewfactory.view.IWechatListView;
import com.txznet.comm.ui.viewfactory.view.defaults.AuthorizationView;
import com.txznet.comm.ui.viewfactory.view.defaults.BindDeviceView;
import com.txznet.comm.ui.viewfactory.view.defaults.ChatSysHighlightView;
import com.txznet.comm.ui.viewfactory.view.defaults.ChatSysInterruptView;
import com.txznet.comm.ui.viewfactory.view.defaults.ConstellationFortuneView;
import com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultChatToSysPartView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultCompetitionDetailView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultCompetitionView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultFlightListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultFlightTicketListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultHelpDetailImageView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultHelpTipsView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultLogoQrCodeView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMapPoiListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMoviePhoneNumQRView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMovieSeatPlanViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMovieWaitingPayQRView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultOfflinePromoteView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultPoiListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultQiWuTicketPayView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultReminderListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultNavAppListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultSearchEditView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultSelectCityView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultTrainListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultFilmListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMovieTheaterListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMovieTimeListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultTrainTicketListView;
import com.txznet.comm.ui.viewfactory.view.defaults.FeedbackView;
import com.txznet.comm.ui.viewfactory.view.defaults.QrCodeView;

import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.widget.FrameLayout;

/**
 * 窗口布局管理类，管理当前显示的布局及View
 * 
 * @author TerryYang
 *
 */
public class WinLayoutManager {

	private static WinLayoutManager sInstance = new WinLayoutManager();
	private IWinLayout mWinLayout;
	private IListView mListView;
	private boolean mUseThirdRecordView = false;
	/**
	 * View
	 */
	private MsgViewBase mCurMsgView;
	private IRecordView mRecordView;
	private IPoiListView mPoiListView;
	private DefaultPoiListView mDeletableLv;
	private IAudioListView mAudioListView;
	private IWechatListView mWechatListView;
	private IChatFromSysView mChatFromSysView;
	private IChatToSysView mChatToSysView;
	private ITtsListView mTtsListView;
	private ISimListView mSimListView;
	private ICallListView mCallListView;
	private IHelpListView mHelpListView;
	private IHelpDetailListView mHelpDetailListView;
	private ICinemaListView mCinemaListView;
	private IChatShockView mChatShockView;
	private IChatWeatherView mChatWeatherView;
	private INoTtsQrcodeView mNoTtsQrcodeView;
	private IListTitleView mListTitleView;
	private IChatMapView mChatMapView;
	private IMapPoiListView mMapPoiListView;
	private IQrCodeView mQrCodeView;
	private IChatSysHighlightView mChatSysHighlightView;
	private IChatSysInterruptView mChatSysInterruptView;
	private INavAppListView mNavAppListView;
	private IHelpTipsView mHelpTipsView;
	private IHelpDetailImageView mHelpDetailImageView;
	private IReminderListView mReminderListView;
	private IChatToSysPartView mChatToSysPartView;
	private ISearchEditView mSearchEditView;
	private ISelectCityView mSelectCityView;
	private IFlightListView mFlightListView;
	private ITrainListView mTrainListView;
	private ITrainTicketList mTrainTicketList;
	private IFlightTicketList mFlightTicketList;
	private IStyleListView mStyleListView;
	private IFloatView mFloatView;
	private IAuthorizationView mAuthorizationView;
	private IFilmListView mFilmListView;
	private IMovieTheaterView mMovieTheaterView;
	private IMovieTimeListView mMovieTimeListView;
	private IMovieSeatPlanView mMovieSeatPlanView;
	private IMoviePhoneNumQRView mMoviePhoneNumQRView;
	private IBindDeviceView mBindDeviceView;
	private IMovieWaitingPayQRView mMovieWaitingPayQRView;
	private IFeedbackView mFeedbackView;
	private ITicketPayView mTicketPayView;
	private ILogoQrCodeView mLogoQrCodeView;
	private IOfflinePromoteView mOfflinePromoteView;

	public IFeedbackView getFeedbackView() {
		return mFeedbackView;
	}

	public IConstellationMatchingView getConstellationMatchingView() {
		return mConstellationMatchingView;
	}

	public IConstellationFortuneView getConstellationFortuneView() {
		return mConstellationFortuneView;
	}

	private IConstellationMatchingView mConstellationMatchingView;
	private IConstellationFortuneView mConstellationFortuneView;
	
	private ICompetitionView mCompetitionView;
	private ICompetitionDetailView mCompetitionDetailView;

	private WinLayoutManager() {
	}

	public static WinLayoutManager getInstance() {
		return sInstance;
	}

	private String mThemeViewPrefix = "";
	
	public boolean viewInited = false;

	/**
	 * 读取不同主题的资源包的View
	 */
	public void initView() {
		if (!TextUtils.isEmpty(mThemeViewPrefix)) {
			LogUtil.logd("[UI2.0]start init view:" + mThemeViewPrefix);
			mRecordView = (IRecordView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "RecordView");
			mRecordView.init();
			mPoiListView = (IPoiListView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "PoiListView");
			mPoiListView.init();
			mDeletableLv = (DefaultPoiListView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "DefaultPoiListView");
			if (mDeletableLv == null) {
				mDeletableLv = DefaultPoiListView.getInstance();
			}
			mDeletableLv.init();
			mAudioListView = (IAudioListView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "AudioListView");
			mAudioListView.init();
			mWechatListView = (IWechatListView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "WeChatListView");
			mWechatListView.init();
			mChatToSysView = (IChatToSysView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "ChatToSysView");
			mChatToSysView.init();
			mChatFromSysView = (IChatFromSysView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "ChatFromSysView");
			mChatFromSysView.init();
			mTtsListView = (ITtsListView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "TtsListView");
			mTtsListView.init();
			mSimListView = (ISimListView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "SimListView");
			mSimListView.init();
			mCallListView = (ICallListView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "CallListView");
			mCallListView.init();
			mHelpListView = (IHelpListView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "HelpListView");
			mHelpListView.init();
			mCinemaListView = (ICinemaListView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "CinemaListView");
			mCinemaListView.init();
			mChatShockView = (IChatShockView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "ChatShockView");
			mChatShockView.init();
			mChatWeatherView = (IChatWeatherView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "ChatWeatherView");
			mChatWeatherView.init();
			mNoTtsQrcodeView = (INoTtsQrcodeView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "NoTtsQrcodeView");
			mNoTtsQrcodeView.init();
			mListTitleView = (IListTitleView) UIResLoader.getInstance()
					.getClassInstance(mThemeViewPrefix + "ListTitleView");
			mListTitleView.init();
			// TODO 新版本的增加了帮助详情界面，使用之前的皮肤包没有这个属性
			try {
				mHelpDetailListView = (IHelpDetailListView) UIResLoader.getInstance()
						.getClassInstance(mThemeViewPrefix + "HelpDetailListView");
				mHelpDetailListView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init help detail view error!");
			}
			try {
				mMapPoiListView = (IMapPoiListView) UIResLoader.getInstance()
						.getClassInstance(mThemeViewPrefix + "MapPoiListView");
				mMapPoiListView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init IMapPoiListView error,use default!");
				mMapPoiListView = DefaultMapPoiListView.getInstance();
				mMapPoiListView.init();
			}
			try {
				mQrCodeView = (IQrCodeView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "QrCodeView");
				mQrCodeView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init help QrCode view error!");
				mQrCodeView = QrCodeView.getInstance();
				mQrCodeView.init();
			}		
			try {
				mBindDeviceView = (IBindDeviceView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "BindDeviceView");
				mBindDeviceView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init  BindDeviceView view error!");
				mBindDeviceView = BindDeviceView.getInstance();
				mBindDeviceView.init();
			}

			try {
				mConstellationFortuneView = (IConstellationFortuneView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "ConstellationFortuneView");
				mConstellationFortuneView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init  ConstellationFortuneView view error!");
				mConstellationFortuneView = ConstellationFortuneView.getInstance();
				mConstellationFortuneView.init();
			}

			try {
				mConstellationMatchingView = (IConstellationMatchingView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "ConstellationMatchingView");
				mConstellationMatchingView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init  ConstellationMatchingView view error!");
				mConstellationMatchingView = ConstellationMatchingView.getInstance();
				mConstellationMatchingView.init();
			}

			try {
				mFeedbackView = (IFeedbackView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "FeedbackView");
				mFeedbackView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init  FeedbackView view error!");
				mFeedbackView = FeedbackView.getInstance();
				mFeedbackView.init();
			}
			try {
				mChatSysHighlightView = (IChatSysHighlightView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "ChatSysHighlightView");
				mChatSysHighlightView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init help ChatSysHighlightView view error!");
				mChatSysHighlightView = ChatSysHighlightView.getInstance();
				mChatSysHighlightView.init();
			}
			try {
				mChatSysInterruptView = (IChatSysInterruptView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "ChatSysInterruptView");
				mChatSysInterruptView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init help mChatSysInterruptView view error!");
				mChatSysInterruptView = ChatSysInterruptView.getInstance();
				mChatSysInterruptView.init();
			}
			try {
				mNavAppListView = (INavAppListView) UIResLoader.getInstance()
						.getClassInstance(mThemeViewPrefix + "NavAppListView");
				mNavAppListView.init();
			} catch (Exception e) {
				mNavAppListView = DefaultNavAppListView.getInstance();
				mNavAppListView.init();
			}

			try {
				mHelpTipsView = (IHelpTipsView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "HelpTipsView");
				mHelpTipsView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init helpTipsView error!");
				mHelpTipsView = DefaultHelpTipsView.getInstance();
				mHelpTipsView.init();
			}

			try {
				mHelpDetailImageView = (IHelpDetailImageView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "HelpDetailImageView");
				mHelpDetailImageView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init HelpDetailImageView error!");
				mHelpDetailImageView = DefaultHelpDetailImageView.getInstance();
				mHelpDetailImageView.init();
			}
			try{
				mReminderListView = (IReminderListView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "ReminderListView");
				mReminderListView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init ReminderListView error!");
				mReminderListView = DefaultReminderListView.getInstance();
				mReminderListView.init();
			}
			try{
				mFlightListView = (IFlightListView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "FlightListView");
				mFlightListView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init FlightListView error!");
				mFlightListView = DefaultFlightListView.getInstance();
				mFlightListView.init();
			}

			try{
				mTrainListView = (ITrainListView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "TrainListView");
				mTrainListView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init TrainListView error!");
				mTrainListView = DefaultTrainListView.getInstance();
				mTrainListView.init();
			}
			try{
				mTrainTicketList = (ITrainTicketList) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "TrainTicketListView");
				mTrainTicketList.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init TrainTicketListView error!");
				mTrainTicketList = DefaultTrainTicketListView.getInstance();
				mTrainTicketList.init();
			}
			try{
				mFlightTicketList = (IFlightTicketList) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "FlightTicketListView");
				mFlightTicketList.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init FlightTicketListView error!");
				mFlightTicketList = DefaultFlightTicketListView.getInstance();
				mFlightTicketList.init();
			}
			try {
				mChatToSysPartView = (IChatToSysPartView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "ChatToSysPartView");
				mChatToSysPartView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init ChatToSysPartView error!");
				mChatToSysPartView = DefaultChatToSysPartView.getInstance();
				mChatToSysPartView.init();
			}

			try {
				mSearchEditView = (ISearchEditView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "SearchEditView");
				mSearchEditView.init();
			}catch (Exception e) {
				LogUtil.logw("[UI2.0] init SearchEditView error!");
				mSearchEditView = DefaultSearchEditView.getInstance();
				mSearchEditView.init();
			}

			try {
				mSelectCityView = (ISelectCityView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "SelectCityView");
				mSelectCityView.init();
			}catch (Exception e) {
				LogUtil.logw("[UI2.0] init SelectCityView error!");
				mSelectCityView = DefaultSelectCityView.getInstance();
				mSelectCityView.init();
			}



			try{
				mStyleListView = (IStyleListView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "StyleListView");
				mStyleListView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init mStyleListView error!");
			}

			try{
				mFloatView = (IFloatView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "FloatView");
				mFloatView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init mFloatView error!");
			}

			try{
				mAuthorizationView = (IAuthorizationView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "AuthorizationView");
				mAuthorizationView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init mAuthorizationView error!");
				mAuthorizationView  = AuthorizationView.getInstance();
			}

			try {
				mFilmListView = (IFilmListView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "FilmListView");
				mFilmListView.init();
			}catch (Exception e){
				LogUtil.logw("[UI2.0] init mFilmListView error!");
				mFilmListView = DefaultFilmListView.getInstance();
				mFilmListView.init();
			}

			try {
				mMovieTheaterView = (IMovieTheaterView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "MovieTheaterListView");
				mMovieTheaterView.init();
			}catch (Exception e){
				LogUtil.logw("[UI2.0] init mMovieTheaterView error!");
				mMovieTheaterView = DefaultMovieTheaterListView.getInstance();
				mMovieTheaterView.init();
			}

			try {
				mMovieTimeListView = (IMovieTimeListView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "MovieTimeListView");
				mMovieTimeListView.init();
			}catch (Exception e){
				LogUtil.logw("[UI2.0] init mMovieTimeListView error!");
				mMovieTimeListView = DefaultMovieTimeListView.getInstance();
				mMovieTimeListView.init();
			}

			try {
				mMovieSeatPlanView = (IMovieSeatPlanView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "MovieSeatPlanView");
				mMovieSeatPlanView.init();
			}catch (Exception e){
				LogUtil.logw("[UI2.0] init mMovieSeatPlanView error!");
				mMovieSeatPlanView = DefaultMovieSeatPlanViewData.getInstance();
				mMovieSeatPlanView.init();
			}

			try {
				mMoviePhoneNumQRView = (IMoviePhoneNumQRView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "MoviePhoneNumQRView");
				mMoviePhoneNumQRView.init();
			}catch (Exception e){
				LogUtil.logw("[UI2.0] init mMoviePhoneNumQRView error!");
				mMoviePhoneNumQRView = DefaultMoviePhoneNumQRView.getInstance();
				mMoviePhoneNumQRView.init();
			}

			try {
				mMovieWaitingPayQRView = (IMovieWaitingPayQRView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "MovieWaitingPayQRView");
				mMovieWaitingPayQRView.init();
			}catch (Exception e){
				LogUtil.logw("[UI2.0] init mMovieWaitingPayQRView error!");
				mMovieWaitingPayQRView = DefaultMovieWaitingPayQRView.getInstance();
				mMovieWaitingPayQRView.init();
			}

			try {
				mTicketPayView = (ITicketPayView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "TicketPayView");
				mTicketPayView.init();
			}catch (Exception e){
				LogUtil.logw("[UI2.0] init mTicketPayView error!");
				mTicketPayView = DefaultQiWuTicketPayView.getInstance();
				mTicketPayView.init();
			}

			try {
				mCompetitionView = (ICompetitionView)UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "CompetitionView");
				mCompetitionView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init mCompetitionView error!");
				mCompetitionView = DefaultCompetitionView.getInstance();
				mCompetitionView.init();
			}

			try {
				mCompetitionDetailView = (ICompetitionDetailView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "CompetitionDetailView");
				mCompetitionDetailView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init mCompetitionDetailView error!");
				mCompetitionDetailView = DefaultCompetitionDetailView.getInstance();
				mCompetitionDetailView.init();
			}

			try {
				mLogoQrCodeView = (ILogoQrCodeView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "LogoQrCodeView");
				mLogoQrCodeView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init DefaultLogoQrCodeView error!");
				mLogoQrCodeView = DefaultLogoQrCodeView.getInstance();
				mLogoQrCodeView.init();
			}

			try {
				mOfflinePromoteView = (IOfflinePromoteView) UIResLoader.getInstance().getClassInstance(mThemeViewPrefix + "OfflinePromoteView");
				mOfflinePromoteView.init();
			} catch (Exception e) {
				LogUtil.logw("[UI2.0] init DefaultLogoQrCodeView error!");
				mOfflinePromoteView = DefaultOfflinePromoteView.getInstance();
				mOfflinePromoteView.init();
			}

		}

	}

	public void init() {
		mThemeViewPrefix = ConfigUtil.getThemeViewPrefix();
		LogUtil.logd("init view prefix:" + mThemeViewPrefix);
		String layoutPrefix = ConfigUtil.getThemeLayoutPrefix();
		try {
			mWinLayout = (IWinLayout) UIResLoader.getInstance().getClassInstance(layoutPrefix + "WinLayout");
			mWinLayout.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mWinLayout == null) {
			if (ConfigUtil.getLayoutType() == ConfigUtil.LAYOUT_TYPE_HORIZONTAL) {
				TXZWinLayout2.getInstance().init();
				mWinLayout = TXZWinLayout2.getInstance();
			} else {
				TXZWinLayout1.getInstance().init();
				mWinLayout = TXZWinLayout1.getInstance();
			}
		}
		initView();
		addInnerRecordView();
		if (mListView == null) {
			mListView = mPoiListView;
		}
		viewInited = true;
	}

	public void releaseMapView() {
		if (mMapPoiListView != null) {
			mMapPoiListView.release();
		}
	}

	/**
	 * 方案商设置RecordView
	 * @param view
	 */
	public void addThirdRecordView(View view) {
		LogUtil.logd("[UI2.0] add third record view");
		if(view!=null){
			mUseThirdRecordView = true;
			mWinLayout.addRecordView(view);
		}
	}
	
	public void addInnerRecordView() {
		if (mRecordView == null) {
			LogUtil.loge("mRecordView is null");
			return;
		}
		if (ConfigUtil.getLayoutType() == ConfigUtil.LAYOUT_TYPE_HORIZONTAL){
			addRecordView(mRecordView.getView(new ViewData(ViewData.TYPE_LEFT_RECORD_VIEW)).view);
		}else if (ConfigUtil.getLayoutType() == ConfigUtil.LAYOUT_TYPE_VERTICAL) {
			addRecordView(mRecordView.getView(new ViewData(ViewData.TYPE_BOTTOM_RECORD_VIEW)).view);
		}
	}
	
	public void addRecordView(final View view) {
		if (mWinLayout == null) {
			return;
		}
		if (!mUseThirdRecordView) {
			if (Looper.myLooper() == Looper.getMainLooper()) {
				mWinLayout.addRecordView(view);
				return;
			}
			UI2Manager.runOnUIThread(new Runnable() {
				@Override
				public void run() {
					if (mWinLayout != null) {
						mWinLayout.addRecordView(view);
					}
				}
			}, 0);
		}
	}
	
	public IViewStateListener mViewStateListener = new IViewStateListener(){
		public void onAnimateStateChanged(Animation animation, int state) {
			// if (state == IViewStateListener.STATE_ANIM_ON_END) {
			// exitRecordStateIngoreMode();
			// }
			RecordWin2Manager.getInstance().sendEventToCore(RecordWin2Manager.EVENT_ANIMATION_STATE, animation, state);
		};
	};
	
	private LinkedList<MsgViewBase> msgViews = new LinkedList<MsgViewBase>();
	
	public void updateCurMsgView(MsgViewBase msgView){
		this.mCurMsgView = msgView;
		if (msgView != null) {
			msgViews.add(msgView);
		}
		KeyEventManager.getInstance().onChatViewChange(msgView);
		// if (msgView instanceof IListView && msgView.hasViewAnimation()) {
		// msgView.setIViewStateListener(mViewStateListener);
		// enterRecordStateIngoreMode();
		// return;
		// } else {
		// mViewStateListener.onAnimateStateChanged(null,
		// IViewStateListener.STATE_ANIM_ON_START);
		// mViewStateListener.onAnimateStateChanged(null,
		// IViewStateListener.STATE_ANIM_ON_END);
		// }
		// exitRecordStateIngoreMode();
	}
	
	public void releaseMsgView() {
		if (msgViews.size() == 0) {
			return;
		}
		for (MsgViewBase msgView : msgViews) {
			msgView.release();
		}
		msgViews.clear();
	}


	public void releaseRecordView() {
		if (mRecordView != null) {
			mRecordView.release();
		}
	}
	
	// private boolean mIsRecordStateIngoreMode = false;
	// private int mCurState = 0;

	// private void enterRecordStateIngoreMode() {
	// updateStateInner(0);
	// mIsRecordStateIngoreMode = true;
	// }
	//
	// private void exitRecordStateIngoreMode() {
	// if (!mIsRecordStateIngoreMode) {
	// return;
	// }
	// updateStateInner(mCurState);
	// mIsRecordStateIngoreMode = false;
	// }
	
	public MsgViewBase getCurMsgView() {
		return mCurMsgView;
	}

	public void updateListView(IListView listView) {
		this.mListView = listView;
	}

	public IListView getCurListView() {
		return mListView;
	}
	
	
	public void addView(int targetView, View view) {
		if (mWinLayout != null) {
			mWinLayout.addView(targetView, view);
		}
	}

	public void removeLastView(){
		if (mWinLayout != null) {
			mWinLayout.removeLastView();
		}
	}
	
	// public void addViewToWindow(View view,FrameLayout.LayoutParams
	// layoutParams){
	// if(mWinLayout!=null){
	// mWinLayout.addViewToWindow(view, layoutParams);
	// }
	// }
	
	
	public void updateProgress(int progress, int selection) {
		if (mListView != null) {
			mListView.updateProgress(progress, selection);
			KeyEventManager.getInstance().onUpdateProgress(selection, progress);
		}
	}

	public void updateState(int state) {
		// mCurState = state;
		updateStateInner(state);
	}

	private void updateStateInner(int state){
		// if (mRecordView == null || mIsRecordStateIngoreMode) {
		// return;
		// }
		//当窗口打开的时候，更新窗口状态为聊天模式
		if (state == IRecordView.STATE_WIN_OPEN) {
			getLayout().updateContentMode(IWinLayout.CONTENT_MODE_CHAT);
		}

		mRecordView.updateState(state);

		if (mFloatView != null) {
			mFloatView.updateState(state);
		}
	}

	public void updateVolume(int volume) {
		// if (mRecordView != null && !mIsRecordStateIngoreMode) {
		if (mRecordView != null) {
			mRecordView.updateVolume(volume);
		}
	}
	
	public void snapPage(boolean next) {
		if (mListView != null) {
			mListView.snapPage(next);
		}
	}

	public void updateItemSelect(int selection) {
		if (mListView != null) {
			try {
				Method method = mListView.getClass().getMethod("updateItemSelect", int.class);
				method.setAccessible(true);
				method.invoke(mListView, selection);
			} catch (NoSuchMethodException e) {
				LogUtil.logw("[UI2.0]calling updateItemSelect but no such method");
			} catch (Throwable e) {
				LogUtil.loge("[UI2.0]calling updateItemSelect failed");
			}
		}
	}
	
	public IWinLayout getLayout() {
		if (mWinLayout == null) {
			TXZWinLayout2.getInstance().init();
			mWinLayout = TXZWinLayout2.getInstance();
		}
		return mWinLayout;
	}
	
	public IRecordView getRecordView() {
		return mRecordView;
	}

	public void setRecordView(IRecordView mRecordView) {
		this.mRecordView = mRecordView;
	}

	public IPoiListView getPoiListView() {
		return mPoiListView;
	}

	public IAudioListView getAudioListView() {
		return mAudioListView;
	}

	public IWechatListView getWechatListView() {
		return mWechatListView;
	}

	public IChatFromSysView getChatFromSysView() {
		return mChatFromSysView;
	}

	public IChatToSysView getChatToSysView() {
		return mChatToSysView;
	}

	public ITtsListView getTtsListView() {
		return mTtsListView;
	}

	public ISimListView getSimListView() {
		return mSimListView;
	}

	public ICallListView getCallListView() {
		return mCallListView;
	}

	public IHelpListView getHelpListView() {
		return mHelpListView;
	}
	
	public IHelpDetailListView getHelpDetailListView() {
		return mHelpDetailListView;
	}

	public ICinemaListView getCinemaListView() {
		return mCinemaListView;
	}

	public IChatShockView getChatShockView() {
		return mChatShockView;
	}
	public IChatMapView getChatMapView() {
		return mChatMapView;
	}

	public IChatWeatherView getChatWeatherView() {
		return mChatWeatherView;
	}

	public INoTtsQrcodeView getNoTtsQrcodeView() {
		return mNoTtsQrcodeView;
	}

	public IMapPoiListView getMapPoiListView() {
		return mMapPoiListView;
	}
	
	public IQrCodeView getQrCodeView() {
		return mQrCodeView;
	}
	
	public IChatSysHighlightView getChatSysHighlight(){
		return mChatSysHighlightView;
	}
	
	public IChatSysInterruptView getChatSysInterrupt(){
		return mChatSysInterruptView;
	}

	public IHelpTipsView getHelpTipsView() {
		return mHelpTipsView;
	}

	public INavAppListView getNavAppListView(){
		return mNavAppListView;
	}

	public IHelpDetailImageView getHelpDetailImageView() {
		return mHelpDetailImageView;
	}
	
	public IReminderListView getReminderListView() {
		return mReminderListView;
	}
	public IFlightListView getFlightListView() {
		return mFlightListView;
	}

	public ITrainListView getTrainListView() {
		return mTrainListView;
	}

	public ITrainTicketList getmTrainTicketList(){
		return mTrainTicketList;
	}
	public IFlightTicketList getFlightTicketList(){
		return mFlightTicketList;
	}

	public IChatToSysPartView getChatToSysPartView() {
		return mChatToSysPartView;
	}

	public ITicketPayView getmTicketPayView(){
		return mTicketPayView;
	}

	public ISearchEditView getSearchEditView() {
		//如果使用的是ui1.0，读取默认的
		if (mSearchEditView == null) {
			mSearchEditView = DefaultSearchEditView.getInstance();
			mSearchEditView.init();
		} else if (RecordWin2Manager.getInstance().isForceUseUI1()) {
			if (mSearchEditView != DefaultSearchEditView.getInstance()) {
				mSearchEditView = DefaultSearchEditView.getInstance();
				mSearchEditView.init();
			}
		}
		return mSearchEditView;
	}


	public ISelectCityView getSelectCityView() {
		//如果使用的是ui1.0，读取默认的
		if (mSelectCityView == null) {
			mSelectCityView = DefaultSelectCityView.getInstance();
			mSelectCityView.init();
		} else if (RecordWin2Manager.getInstance().isForceUseUI1()) {
			if (mSelectCityView != DefaultSelectCityView.getInstance()) {
				mSelectCityView = DefaultSelectCityView.getInstance();
				mSelectCityView.init();
			}
		}
		return mSelectCityView;
	}


	public IStyleListView getStyleListView() {
		return mStyleListView;
	}


	public IFloatView getFloatView (){
		return mFloatView;
	}

	public IAuthorizationView getAuthorizationView() {
		return mAuthorizationView;
	}

	public IFilmListView getFilmListView(){
		return mFilmListView;
	}

	public IMovieTheaterView getMovieTheaterListView(){
		return mMovieTheaterView;
	}

	public IMovieTimeListView getMovieTimeListView(){
		return mMovieTimeListView;
	}

	public IMovieSeatPlanView getMovieSeatPlanView(){
		return mMovieSeatPlanView;
	}

	public IMoviePhoneNumQRView getMoviePhoneNumQRView(){
		return mMoviePhoneNumQRView;
	}

    public IBindDeviceView getBindDeviceView() {
		return mBindDeviceView;
    }
	public IMovieWaitingPayQRView getMovieWaitingPayQRView(){
		return mMovieWaitingPayQRView;
	}

	public ICompetitionView getCompetitionView() {
		return mCompetitionView;
	}

	public ICompetitionDetailView getCompetitionDetailView() {
		return mCompetitionDetailView;
	}

	public ILogoQrCodeView getLogoQrCodeView() {
		return mLogoQrCodeView;
	}

	public IOfflinePromoteView getOfflinePromoteView(){
		return mOfflinePromoteView;
	}
}
