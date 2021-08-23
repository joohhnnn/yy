package com.txznet.txz.module.ui;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txz.ui.voice.VoiceData;
import com.txz.ui.voice.VoiceData.StockInfo;
import com.txz.ui.voice.VoiceData.WeatherInfos;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.UI2Manager.UIInitListener;
import com.txznet.comm.ui.UIVersionManager;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.layout.layout1.TXZWinLayout1;
import com.txznet.comm.ui.layout.layout1.TXZWinLayout2;
import com.txznet.comm.ui.recordwin.RecordWin2;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.ThemeStyle;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.DataConvertUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.loader.AppLogic;
import com.txznet.record.bean.BaseDisplayMsg;
import com.txznet.record.bean.ChatMessage;
import com.txznet.record.bean.PoiMsg;
import com.txznet.record.helper.ChatMsgFactory;
import com.txznet.record.keyevent.KeyEventManagerUI1;
import com.txznet.record.ui.IWinRecord;
import com.txznet.record.ui.WinRecord;
import com.txznet.record.ui.WinRecordImpl3;
import com.txznet.record2.winrecord.yidong.WinRecordImpl;
import com.txznet.reserve.activity.ReserveSingleInstanceActivity0;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZConfigManager.InitParam;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.sdk.TXZWheelControlEvent;
import com.txznet.txz.R;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.film.FilmManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.ticket.QiWuTicketManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.userconf.ConfigData;
import com.txznet.txz.module.userconf.UserConf;
import com.txznet.txz.module.wheelcontrol.WheelControlManager;
import com.txznet.txz.module.wheelcontrol.WheelControlManager.OnStateChangeListener;
import com.txznet.txz.plugin.interfaces.AbsTextJsonParse;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.service.TXZService;
import com.txznet.txz.ui.widget.SDKFloatView;
import com.txznet.txz.ui.win.help.HelpPreferenceUtil;
import com.txznet.txz.ui.win.help.WinHelpDetailTops;
import com.txznet.txz.ui.win.help.WinHelpManager;
import com.txznet.txz.ui.win.nav.SearchEditManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.WinRecordCycler;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.SignatureUtil;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.runnables.Runnable1;

import android.graphics.drawable.Drawable;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;

public class WinManager extends IModule {

	// UI2.0相关接口
	private boolean mUseRecordWin2 = ProjectCfg.DEFAULT_USE_2_0; // 是否使用UI2.0界面
	private Boolean mForceUseRecordWin1User = null; // 用户配置的是否切换到UI1.0
	private String mThirdImplWin2; // UI2.0第三方实现包名
	private boolean mDisableThirdWin2 = false;
	private boolean mShowHelpTips = false;
	
	//腾讯方控
	private boolean mBLEConnected = false;
	private int mTtsWheelState = TtsManager.INVALID_TTS_TASK_ID;

	// TODO 还原正确的值
	private boolean mIsInnerHudRecordWin = false;
	private boolean mReserveInnerRecordWin = false;
	private Boolean mSupportPoiMap = null;

	private String mThirdImpl;
	public RecordInvokeAdapter mPluginInvokeAdapter;
	private RecordInvokeAdapter mCurRecordInvokeAdapter;

	// 强制使用默认的界面
	public boolean mForceLocalAdapter;
	private ViewPluginUtil mViewPluginUtil;

	// 插件语义显示处理
	private List<AbsTextJsonParse> mJsonParses = new ArrayList<AbsTextJsonParse>();
	
	private Integer mWinRecordImpl;
	private Boolean mEnableFullScreen;
	
	private static final String TAG = "WinManager:";
	private static HashMap<String, WinConfirmAsr> mTaskDialog;

	public void setUseUI1(boolean useUI1){
		JNIHelper.logd("[UI2.0]setUseUI1 UI1:" + useUI1);
		if (mForceUseRecordWin1User == null || mForceUseRecordWin1User != useUI1) {
			mForceUseRecordWin1User = useUI1;
			if (mForceUseRecordWin1User) {
				switchUseUI1(mForceUseRecordWin1User);
			}
		}
	}
	
	public void switchUseUI1(boolean useUI1) {
		JNIHelper.logd("[UI2.0]force Use UI1:" + useUI1);
		if (useUI1) {
			MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_USE_1);
			ConfigUtil.initThemeType(ConfigUtil.THEME_TYPE_SIRI);
			ViewConfiger.getInstance().initRecordWin1ThemeConfig();
			ThemeConfigManager.getInstance().initThemeConfig();
			//初始化成功后判断是否要显示小红点
			boolean showHelpNews = HelpPreferenceUtil.getInstance().getBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_NEWS, true);
			com.txznet.comm.remote.util.ConfigUtil.setShowHelpNewTag(showHelpNews);
			HelpPreferenceUtil.getInstance().setBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_NEWS, showHelpNews);
			mUseRecordWin2 = false;
		} else {
			mUseRecordWin2 = true;
		}
	}

	public void disableThirdWin2(boolean disable) {
		JNIHelper.logd("[UI2.0]disable third win:" + disable);
		if (mDisableThirdWin2 != disable) {
			mDisableThirdWin2 = disable;
		}
	}
	
	@Override
	public int initialize_AfterInitSuccess() {
		WheelControlManager.getInstance().setOnStateChangeListener(mWheelStateListener);
		return super.initialize_AfterInitSuccess();
	}

	private static WinManager sWinManager = new WinManager();

	private WinManager() {
		mViewPluginUtil = new ViewPluginUtil(this);
		mInited = false;
		mInitSuccessed = false;
		TXZUITester.initTest();
	}

	public static WinManager getInstance() {
		return sWinManager;
	}

	@Override
	public int initialize_addPluginCommandProcessor() {
		mViewPluginUtil.addPluginProcessor();
		RecorderWin.addPluginCommandProcessor();
		return super.initialize_addPluginCommandProcessor();
	}

	public ViewPluginUtil getViewPluginUtil() {
		return mViewPluginUtil;
	}

	/**
	 * 腾讯方控点击了语音按钮
	 */
	public void clickWheelVoiceBtn() {
		getAdapter().dealKeyEvent(TXZWheelControlEvent.VOICE_KEY_CLICKED_EVENTID);
	}
	
	@Override
	public boolean isInited() {
		if (!mUseRecordWin2 || !TextUtils.isEmpty(mThirdImplWin2)) {
			return true;
		}
		return mInited;
	}

	@Override
	public boolean isInitSuccessed() {
		if (!mUseRecordWin2 || !TextUtils.isEmpty(mThirdImplWin2)) {
			return true;
		}
		return mInitSuccessed;
	}

	public boolean isDelayAddWkWords() {
		// if (getAdapter() != null && ADAPTER_RECORDWIN_2 != null &&
		// getAdapter() == ADAPTER_RECORDWIN_2) {
		// if (UIVersionManager.getInstance().getVersionCode() >= 101) {
		// boolean ifAddWkWordsDelay = ConfigUtil.isDelayAddWkWords();
		// JNIHelper.logd("mIfAddWkWordsDelay:" + ifAddWkWordsDelay);
		// return ifAddWkWordsDelay;
		// }
		// }
		JNIHelper.logd("mIfAddWkWordsDelay: false");
		return false;
	}
	
	
	private void reportUIInfo() {
		// 根据签名判断皮肤包是不是我们自己的皮肤包
		X509Certificate txzCert = SignatureUtil.getSignCertificate("com.txznet.txz");
		X509Certificate skinApkCert = SignatureUtil
				.getSignCertificateFromApk(UIResLoader.getInstance().getResApkPath());
		if (txzCert.equals(skinApkCert)) {
			MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_SKIN_TXZ);
			int themeCode = ConfigUtil.getThemeType();
			switch (themeCode) {
			case ConfigUtil.THEME_TYPE_IRONMAN:
				MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_SKIN_IRONMAN);
				break;
			case ConfigUtil.THEME_TYPE_SIRI:
				MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_SKIN_SIRI);
				break;
			case ConfigUtil.THEME_TYPE_WAVE:
				MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_SKIN_WAVE);
				break;
			default:
				break;
			}
		} else {
			MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_SKIN_USER);
		}
	}
	
	private void onBLEStateChange(boolean connected){
		JNIHelper.logd("[UI2.0] onBLEStateChanged state:" + connected);
		if (mTtsWheelState != TtsManager.INVALID_TTS_TASK_ID) {
			TtsManager.getInstance().cancelSpeak(mTtsWheelState);
		}
		String text = NativeData.getResString(connected ? "RS_VOICE_WHEELCONTROL_STATE_CONNECTED"
				: "RS_VOICE_WHEELCONTROL_STATE_DISCONNECTED");
		mTtsWheelState = TtsManager.getInstance().speakText(text);
		getAdapter().refreshState("wheelControl", connected ? 1 : 0);
	}
	
	public OnStateChangeListener mWheelStateListener = new OnStateChangeListener() {

		@Override
		public void isConnected(boolean enable) {
			if (enable != mBLEConnected) {
				mBLEConnected = enable;
				onBLEStateChange(mBLEConnected);
			}

		}
	};
	
	
	/**
	 * 初始化窗口参数配置
	 */
	public void initializeWinConfig() {
		List<String> configKeys = new ArrayList<String>();
		configKeys.add(TXZFileConfigUtil.KEY_WIN_TYPE);
		configKeys.add(TXZFileConfigUtil.KEY_WIN_FLAGS);
		configKeys.add(TXZFileConfigUtil.KEY_IF_SET_WINDOW_BG);
		configKeys.add(TXZFileConfigUtil.KEY_WINDOW_CONTENT_WIDTH);
		configKeys.add(TXZFileConfigUtil.KEY_FLOAT_VIEW_WIN_TYPE);
		configKeys.add(TXZFileConfigUtil.KEY_WIN_FIT_SCREEN_CHANGE);
		configKeys.add(TXZFileConfigUtil.KEY_SYSTEM_UI_VISIBILITY);
		configKeys.add(TXZFileConfigUtil.KEY_ENABLE_LIST_ANIM);
		configKeys.add(TXZFileConfigUtil.KEY_SHOW_HELP_TIPS);
		configKeys.add(TXZFileConfigUtil.KEY_SUPPORT_MORE_RECORD_STATE);
		configKeys.add(TXZFileConfigUtil.KEY_WIN_SOFT);
		HashMap<String, String> configs = TXZFileConfigUtil.getConfig(configKeys);
		if (configs.get(TXZFileConfigUtil.KEY_WIN_SOFT) != null) {
			try {
				int winSoft = Integer.parseInt(configs
						.get(TXZFileConfigUtil.KEY_WIN_SOFT));
				
				setWinSoft(winSoft);
			} catch (NumberFormatException e) {
				LogUtil.d("WinManager error set win soft");

			}
		}
		if (configs.get(TXZFileConfigUtil.KEY_WIN_TYPE) != null) {
			try { 
				int winType = Integer.parseInt(configs.get(TXZFileConfigUtil.KEY_WIN_TYPE));
				setWinType(winType);
			} catch (NumberFormatException e) {
				LogUtil.loge("WinManager error set win type");
			}
		}
		if (configs.get(TXZFileConfigUtil.KEY_WIN_FLAGS) != null) {
			try {
				int winFlags = Integer.parseInt(configs.get(TXZFileConfigUtil.KEY_WIN_FLAGS));
				setWinFlags(winFlags);
			} catch (NumberFormatException e) {
				LogUtil.loge("WinManager error set win type");
			}
		}
		if (configs.get(TXZFileConfigUtil.KEY_WINDOW_CONTENT_WIDTH) != null) {
			try {
				int winContentWidth = Integer.parseInt(configs.get(TXZFileConfigUtil.KEY_WINDOW_CONTENT_WIDTH));
				setWinContentWidth(winContentWidth);
			} catch (NumberFormatException e) {
				LogUtil.loge("WinManager error set winContentWidth");
			}
		}
		if (configs.get(TXZFileConfigUtil.KEY_IF_SET_WINDOW_BG) != null) {
			try {
				boolean setWinBg = Boolean.parseBoolean(configs.get(TXZFileConfigUtil.KEY_IF_SET_WINDOW_BG));
				setIfSetWinBg(setWinBg);
			} catch (Exception e) {
				LogUtil.loge("WinManager error set setWinBg");
			}
			
		}
		if (configs.get(TXZFileConfigUtil.KEY_FLOAT_VIEW_WIN_TYPE) != null) {
			try {
				int floatViewWinType = Integer.parseInt(configs.get(TXZFileConfigUtil.KEY_FLOAT_VIEW_WIN_TYPE));
				setFloatViewWinType(floatViewWinType);
			} catch (NumberFormatException e) {
				LogUtil.loge("WinManager error set setFloatViewWinType", e);
			}
		}
		if (configs.get(TXZFileConfigUtil.KEY_HELP_FLOAT_REFRESH_DELAY) != null) {
			try {
				int refreshDelay = Integer.parseInt(configs.get(TXZFileConfigUtil.KEY_HELP_FLOAT_REFRESH_DELAY));
				HelpGuideManager.getInstance().setHelpFloatRefreshDelay(refreshDelay);
			} catch (Exception e) {
				LogUtil.loge("WinManager error set setHelpFloatRefreshDelay", e);
			}
		}
		if (configs.get(TXZFileConfigUtil.KEY_WIN_FIT_SCREEN_CHANGE) != null) {
			try {
				boolean fitScreenChange = Boolean.parseBoolean(configs.get(TXZFileConfigUtil.KEY_WIN_FIT_SCREEN_CHANGE));
				setFitScreenChange(fitScreenChange);
			} catch (Exception e) {
				LogUtil.loge(TAG+"error fitScreenChange",e);
			}
		}
		if (configs.get(TXZFileConfigUtil.KEY_SYSTEM_UI_VISIBILITY) != null) {
			try {
				int systemUiVisibility = Integer.parseInt(configs.get(TXZFileConfigUtil.KEY_SYSTEM_UI_VISIBILITY));
				setSystemUiVisibility(systemUiVisibility);
			} catch (NumberFormatException e) {
				LogUtil.loge("WinManager error set win type");
			}
		}
		if (configs.get(TXZFileConfigUtil.KEY_ENABLE_LIST_ANIM) != null) {
			try {
				boolean enableListAnim = Boolean.parseBoolean(configs.get(TXZFileConfigUtil.KEY_ENABLE_LIST_ANIM));
				enableListAnim(enableListAnim);
			} catch (Exception e) {
				LogUtil.loge(TAG + "error enableListAnim");
			}
		}

		if (configs.get(TXZFileConfigUtil.KEY_SHOW_HELP_TIPS) != null) {
			try {
				mShowHelpTips = Boolean.parseBoolean(configs.get(TXZFileConfigUtil.KEY_SHOW_HELP_TIPS));
			}catch (Exception e) {
				LogUtil.loge(TAG + "error showHelpTips");
			}
		}

		if (configs.get(TXZFileConfigUtil.KEY_SUPPORT_MORE_RECORD_STATE) != null) {
			try {
				mSupportMoreRecordState = Boolean.parseBoolean(configs.get(TXZFileConfigUtil.KEY_SUPPORT_MORE_RECORD_STATE));
			}catch (Exception e) {
				LogUtil.loge(TAG + "error mSupportMoreRecordState");
			}
		}

		if (mWinRecordImpl != null) {
			setWinRecordImplInner(mWinRecordImpl);
		}
		if (mEnableFullScreen != null) {
			enableFullScreenInner(mEnableFullScreen);
		}
		if(ScreenUtils.getScreenWidthDp()>0 && ScreenUtils.getScreenHeightDp()>0){
			ScreenUtils.updateScreenSize(ScreenUtils.getScreenWidthDp(), ScreenUtils.getScreenHeightDp(), true);
			SDKFloatView.getInstance().newViewInstance();
			LogUtil.logd("SceenSize change:" + "width:" + GlobalContext.get().getResources().getDimension(R.dimen.x800) +","+ "height:" +GlobalContext.get().getResources().getDimension(R.dimen.y480)) ;
		}
		
		ConfigUtil.useTypingEffect(true);
		ConfigUtil.supportMoreRecordState(true);
		UserConf.getInstance().getFactoryConfigData().mEnableTypingEffectItem = ProjectCfg.isSupportTypingEffect();
		UserConf.getInstance().saveFactoryConfigData();

	}
		
	public void enableListAnim(boolean enable) {
		ListViewItemAnim.enableListAnim(enable);
	}
	
	public void setFitScreenChange(boolean ifFit){
		ScreenUtils.setFitScreenChange(ifFit);
	}
	
	/**
	 * 设置悬浮图标层级
	 */
	public void setFloatViewWinType(int winType) {
		LogUtil.logd("setFloatViewWinType :" + winType);
		SDKFloatView.getInstance().setWinType(winType);
		HelpGuideManager.getInstance().setWinType(winType);
	}
	/**
	 * 设置对话框是否是可撤销的
	 */
	public void setCancelable(boolean flag){
		if (getAdapter() != null && ADAPTER_RECORDWIN_2 != null && getAdapter() == ADAPTER_RECORDWIN_2) {
			RecordWin2.getInstance().setCancelable(flag);
			return;
		}
		if (getAdapter() != null && (getAdapter() == LOCAL_ADAPTER || getAdapter() == LOCAL_ADAPTER_INNER
				|| getAdapter() == COPY_ADAPTER)) {
			AppLogic.runOnUiGround(new Runnable1<Boolean>(flag) {
				@Override
				public void run() {
					WinRecord.getInstance().setCancelable(mP1);
				}
			});
			return;
		}
	}

	public void setCanceledOnTouchOutside(boolean cancel) {
		if (getAdapter() != null && ADAPTER_RECORDWIN_2 != null && getAdapter() == ADAPTER_RECORDWIN_2) {
			RecordWin2.getInstance().setCanceledOnTouchOutside(cancel);
			return;
		}
		if (getAdapter() != null && (getAdapter() == LOCAL_ADAPTER || getAdapter() == LOCAL_ADAPTER_INNER
				|| getAdapter() == COPY_ADAPTER)) {
			AppLogic.runOnUiGround(new Runnable1<Boolean>(cancel) {
				@Override
				public void run() {
					WinRecord.getInstance().setCanceledOnTouchOutside(mP1);
				}
			});
			return;
		}
	}
	
	public void setAllowOutSideClickSentToBehind(boolean allow) {
		if (getAdapter() != null && ADAPTER_RECORDWIN_2 != null && getAdapter() == ADAPTER_RECORDWIN_2) {
			RecordWin2.getInstance().setAllowOutSideClickSentToBehind(allow);
			return;
		}
		if (getAdapter() != null && (getAdapter() == LOCAL_ADAPTER || getAdapter() == LOCAL_ADAPTER_INNER
				|| getAdapter() == COPY_ADAPTER)) {
			AppLogic.runOnUiGround(new Runnable1<Boolean>(allow) {
				@Override
				public void run() {
					WinRecord.getInstance().setAllowOutSideClickSentToBehind(mP1);
				}
			});
			return;
		}
	}
	
	public void enableFullScreen(boolean isFull) {
		this.mEnableFullScreen = isFull;
	}

	public void enableFullScreenInner(boolean isFull) {
		LogUtil.logd(TAG + "enableFullScreen:" + isFull);
		WinHelpManager.getInstance().setEnableFullScreen(isFull);
		if (WinManager.getInstance().isRecordWin2()) {
			AppLogic.runOnUiGround(new Runnable1<Boolean>(isFull) {
				
				@Override
				public void run() {
					if (TextUtils.isEmpty(WinManager.getInstance().getThirdImpl2())) {
						RecordWin2.getInstance().setIsFullSreenDialog(mP1);
					}else {
						ServiceManager.getInstance().sendInvoke(WinManager.getInstance().getThirdImpl2(), "win.record2.fullScreen",
								(mP1+"").getBytes(), null);
					}
					SearchEditManager.getInstance().setIsFullSreenDialog(mP1);
				}
			}, 0);
		}else {
			AppLogic.runOnUiGround(new Runnable1<Boolean>(isFull) {
				@Override
				public void run() {
					WinRecord.getInstance().setIsFullSreenDialog(mP1);
					SearchEditManager.getInstance().setIsFullSreenDialog(mP1);
				}
			}, 0);
		}
	}

	public boolean checkUseRecordWin2(){
		boolean ret = ProjectCfg.DEFAULT_USE_2_0;
		LogUtil.logd("UI2.0 mForceUseRecordWin1User:" + mForceUseRecordWin1User + ",hasThirdImpl:" + hasThirdImpl()
				+ ",isUserResExist:" + UIResLoader.getInstance().isUserResExist() + ",isPriorResExist:" + UIResLoader.getInstance().isPriorResExist());
		if (mForceUseRecordWin1User == null || !mForceUseRecordWin1User) {
			if (hasThirdImpl()) {
				ret = false;
			} else {
				//所有用户定制的情况
				if (UIResLoader.getInstance().isUserResExist() || UIResLoader.getInstance().isPriorResExist()) {
					ret = true;
				}else if (!PreferenceUtil.getInstance().getBoolean(PreferenceUtil.KEY_USE_UI_2_0, ProjectCfg.DEFAULT_USE_2_0)) {
					//如果后台强制关掉了，客户端这边如果不是外部文件的话也关闭
					ret = false;
				}
			}
		}else {
			ret = false;
		}
		return ret;
	}
	
	/**
	 * 初始化UI2相关组件
	 */
	public void initializeUI2Component() {

		mUseRecordWin2 = checkUseRecordWin2();

		if (!mUseRecordWin2) {
			MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_USE_1);
			TXZService.checkSdkInitResult();
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					WinRecord.getInstance().realInit();
				}
			});
			// 初始化成功后判断是否要显示小红点
			boolean showHelpNews = HelpPreferenceUtil.getInstance().getBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_NEWS,true);
			com.txznet.comm.remote.util.ConfigUtil.setShowHelpNewTag(showHelpNews);
			HelpPreferenceUtil.getInstance().setBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_NEWS, showHelpNews);

			//使用1.0 的时候清空配置
			UserConf.getInstance().getFactoryConfigData().mThemeStyle = "";
			UserConf.getInstance().saveFactoryConfigData();
			UserConf.getInstance().getUserConfigData().mSelectThemeStyle = "";
			UserConf.getInstance().saveUserConfigData();

			return;
		}
		MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_START);
		try {
			UI2Manager.getInstance().init(new UIInitListener() {
				@Override
				public void onSuccess() {
					JNIHelper.logd("[UI2.0] init success");
					MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_SUCCESS_ALL, MonitorUtil.UI_INIT_SUCCESS_CORE,
							MonitorUtil.UI_INIT_USE_2);
					mInited = true;
					mInitSuccessed = true;
					WinRecordCycler.getInstance().setWin2Observer();
					TXZService.checkSdkInitResult();
					reportUIInfo();
					//初始化成功后判断是否要显示小红点
					boolean showHelpNews = HelpPreferenceUtil.getInstance().getBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_NEWS, true);
					WinHelpManager.showHelpNewTag(showHelpNews);
					// initIfAddWkWordsDelay();

					ConfigUtil.useTypingEffect(true);
					UserConf.getInstance().getFactoryConfigData().mEnableTypingEffectItem = ProjectCfg.isSupportTypingEffect();
					UserConf.getInstance().saveFactoryConfigData();

					JSONArray styles = new JSONArray();
					//获取主题样式，注册命令字
					final ThemeStyle themeStyle = ThemeConfigManager.getInstance().getThemeStyle();
					if (themeStyle != null) {
						HashSet<ThemeStyle.Model> models = new HashSet<ThemeStyle.Model>();

						for (ThemeStyle.Style s : themeStyle.getStyles()) {
							if (s != null) {
								models.add(s.getModel());
								styles.put(themeStyle2Json(s));
							}
						}

						for (ThemeStyle.Model model : models) {
							String[] cmds = getCMD(model.getName());
							if (cmds != null && cmds.length != 0) {
								regCmdString(cmds);
								for (String cmd:cmds) {
									themeCmds.put(cmd, model.getName());
								}
							}
						}
					}

					UserConf.getInstance().getFactoryConfigData().mThemeStyle = styles.toString();
					UserConf.getInstance().saveFactoryConfigData();

					saveSelectedStyle();
					ThemeStyle.Style style = ThemeConfigManager.getInstance().getStyle();
					if (style != null) {
						ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("init").setType("skin_usage")
								.putExtra("mode", style.getModel().getName())
								.putExtra("theme", style.getTheme().getName())
								.putExtra("screenType", style.getName()).setSessionId().buildCommReport());
					}
				}

				private String[] getCMD(String modelName) {
					String s = NativeData.getResString("RS_VOICE_STYLE_CMD");
					if (!TextUtils.isEmpty(s)) {
						s = s.replaceAll("%MODEL%",modelName);
						return s.split(",");
					}
					return null;
				}

				@Override
				public void onErrorError() {
					JNIHelper.logd("[UI2.0] init error");
					MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_ERROR);
					// 切换成UI1.0的框架
					switchUseUI1(true);
					TXZService.checkSdkInitResult();
				}

				@Override
				public void onError() {
					JNIHelper.logd("[UI2.0] init error");
					MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_ERROR,MonitorUtil.UI_INIT_ERROR_CORE);
					// 暂时先直接使用1.0的框架
					switchUseUI1(true);
					TXZService.checkSdkInitResult();
				}
			});
			ServiceManager.getInstance().addConnectionListener(new ConnectionListener() {
				@Override
				public void onDisconnected(String serviceName) {
					if (!TextUtils.isEmpty(mThirdImplWin2) && mThirdImplWin2.equals(serviceName)) {
						mThirdImplWin2 = null;
					}
				}
				@Override
				public void onConnected(String serviceName) {
				}
			});
		} catch (Exception e) {
			MonitorUtil.monitorCumulant(MonitorUtil.UI_INIT_ERROR,MonitorUtil.UI_INIT_ERROR_CORE);
			switchUseUI1(true);
			JNIHelper.loge("init ui error!");
		}
	}

	public void saveSelectedStyle(){
		String selectStyleJson = "";
		ThemeStyle.Style selectStyle = ThemeConfigManager.getInstance().getStyle();
		if (selectStyle != null) {
			selectStyleJson = themeStyle2Json(selectStyle).toString();
		}

		UserConf.getInstance().getUserConfigData().mSelectThemeStyle = selectStyleJson;
		UserConf.getInstance().saveUserConfigData();
	}

	private JSONObject themeStyle2Json(ThemeStyle.Style style){
		return new JSONBuilder()
				.put("theme", style.getTheme().getName())
				.put("modelIndex",style.getModel().getModel())
				.put("model", style.getModel().getName())
				.put("style", style.getName())
				.put("imgUrl",style.getImgUrl())
				.getJSONObject();
	}

	/**
	 * 从config文件中读取到的style，解析判断是否符合需求
	 * @param styleJson
	 * @return
	 */
	private ThemeStyle.Style parseStyle(String styleJson) {
		ThemeStyle.Style style = null;
		if (!TextUtils.isEmpty(styleJson)) {
			JSONBuilder jsonBuilder = new JSONBuilder(styleJson);
			String theme = jsonBuilder.getVal("theme",String.class,"");
			int modelIndex = jsonBuilder.getVal("modelIndex",Integer.class,-1);
			String model = jsonBuilder.getVal("model",String.class,"");
			String styleName = jsonBuilder.getVal("style",String.class,"");
			String imgUrl = jsonBuilder.getVal("imgUrl",String.class,"");

			if (TextUtils.isEmpty(theme)
					||TextUtils.isEmpty(model)
					||TextUtils.isEmpty(styleName)
					|| modelIndex == -1) {
				return null;
			}
			style = new ThemeStyle.Style(styleName,new ThemeStyle.Model(modelIndex,model),new ThemeStyle.Theme(theme), imgUrl);
		}
		return style;
	}

	/**
	 * 从config文件中读取到的style，解析判断是否符合需求
	 * @param styleJson
	 * @return
	 */
	private ThemeStyle.Style parseSelectStyle(String styleJson){
		ThemeStyle.Style style = null;
		ThemeStyle.Style tmpStyle = parseStyle(styleJson);
		if (tmpStyle != null) {
			ThemeStyle.Style selectStyle = ThemeConfigManager.getInstance().getStyle();
			if (!tmpStyle.equals(selectStyle)) {
				ThemeStyle themeStyle = ThemeConfigManager.getInstance().getThemeStyle();
				if (themeStyle != null) {
					for (ThemeStyle.Style s : themeStyle.getStyles()) {
						if (tmpStyle.equals(s)) {
							style = tmpStyle;
							break;
						}
					}
				}
			}
		}
		return style;
	}

	/**
	 * 当用户从设置选中主题的时候回调
	 */
	public void onSettingConfigUpgrade(){
		ThemeStyle.Style style = parseSelectStyle(UserConf.getInstance().getUserConfigData().mSelectThemeStyle);
		if (style != null) {
			ThemeConfigManager.getInstance().setSelectStyle(style);
			ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("from_setting").setType("skin_usage")
					.putExtra("mode", style.getModel().getName())
					.putExtra("theme", style.getTheme().getName())
					.putExtra("screenType", style.getName()).setSessionId().buildCommReport());

		}
	}

	private HashMap<String,String> themeCmds = new HashMap<String, String>();

	@Override
	public int onCommand(String cmd, String keywords, String voiceString) {
		ReportUtil.doReport(new ReportUtil.Report.Builder().setType("change_mode")
				.putExtra("keywords", keywords).setSessionId().buildCommReport());
		LogUtil.loge("cmd :" + cmd + keywords + voiceString);
		if (!TextUtils.isEmpty(keywords) && themeCmds.size() > 0) {
			String mName = themeCmds.get(keywords);
			if (!TextUtils.isEmpty(mName)) {
				ArrayList<ThemeStyle.Style> styles = new ArrayList<ThemeStyle.Style>();
				for (ThemeStyle.Style s : ThemeConfigManager.getInstance().getThemeStyle().getStyles()) {
					if (TextUtils.equals(s.getModel().getName(), mName)) {
						styles.add(s);
					}
				}
				ChoiceManager.getInstance().showStyleList(styles, null);
			}
		}
		return super.onCommand(cmd, keywords, voiceString);
	}

	/**
	 * 接收comm发过来的事件
	 */
	public IViewStateListener mViewStateTransfer = new IViewStateListener(){
		public void onAnimateStateChanged(Animation animation, int state) {
			JNIHelper.logd("WinManager receive state:" + state);
			for (int i = 0; i < mViewStateListeners.size(); i++) {
				mViewStateListeners.get(i).onAnimateStateChanged(animation, state);
			}
		};
	};
	
	
	private List<IViewStateListener> mViewStateListeners = new ArrayList<IViewStateListener>();
	private Object mLock = new Object();

	public void addViewStateListener(IViewStateListener stateListener) {
		synchronized (mLock) {
			mViewStateListeners.add(stateListener);
		}
	}

	public void removeViewStateListener(IViewStateListener stateListener) {
		synchronized (mLock) {
			mViewStateListeners.remove(stateListener);
		}
	}

	public void removeAllViewStateListener() {
		synchronized (mLock) {
			mViewStateListeners = new ArrayList<IViewStateListener>();
		}
	}

	public void setWinRecordImpl(int impl) {
		mWinRecordImpl = impl;
	}
	
	/**
	 * 是否Activity方式显示
	 * @return
	 */
	public boolean isActivityDialog() {
		return mWinRecordImpl != null && mWinRecordImpl == InitParam.WIN_RECORD_IMPL_ACTIVITY;
	}

	private void setWinRecordImplInner(final int impl) {
		JNIHelper.logd("setWinRecordImplInner:" + impl);
		if (getAdapter() != null && ADAPTER_RECORDWIN_2 != null && getAdapter() == ADAPTER_RECORDWIN_2) {
			if(impl == TXZConfigManager.InitParam.WIN_RECORD_IMPL_YIDONG){
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						RecordWin2.getInstance().setWinImpl(WinRecordImpl.getInstance());
					}
				});
				
			}else {
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						RecordWin2.getInstance().setWinImpl(impl);						
					}
				});
			}
			return;
		}
		if (getAdapter() != null && (getAdapter() == LOCAL_ADAPTER || getAdapter() == LOCAL_ADAPTER_INNER
				|| getAdapter() == COPY_ADAPTER)) {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					WinRecord.getInstance().setWinImpl(impl);					
				}
			});
			return;
		}
	}

	public void setIfSetWinBg(boolean ifSet) {
		LogUtil.logd(TAG + "setIfSetWinBg :" + ifSet);
		AppLogic.runOnUiGround(new Runnable1<Boolean>(ifSet) {
			@Override
			public void run() {
				if (isRecordWin2()) {
					RecordWin2.getInstance().setIfsetWinBg(mP1);
				}
				WinRecord.getInstance().setIfsetWinBg(mP1);
			}
		});
	}
	
	
	public void setWinType(int type) {
		JNIHelper.logd(TAG + "setWinType:" + type);
		AppLogic.runOnUiGround(new Runnable1<Integer>(type) {
			@Override
			public void run() {
				if (isRecordWin2()) {
					RecordWin2.getInstance().setWinType(mP1);
				}
				WinRecord.getInstance().setWinType(mP1);
				HelpGuideManager.getInstance().setWinType(mP1);
				SearchEditManager.getInstance().updateDialogType(mP1);
				WinHelpManager.getInstance().setWinType(mP1);
			}
		});
	}

	public void setWinContentWidth(int width) {
		JNIHelper.logd(TAG + "setWinContentWidth:" + width);
		AppLogic.runOnUiGround(new Runnable1<Integer>(width) {
			@Override
			public void run() {
				if (isRecordWin2()) {
					RecordWin2.getInstance().setWinContentWidth(mP1);
				}
				WinRecord.getInstance().setContentWidth(mP1);
			}
		});
	}
	public void setWinSoft(int winSoft) {
		JNIHelper.logd(TAG + "setWinSoft:" + winSoft);
		AppLogic.runOnUiGround(new Runnable1<Integer>(winSoft) {
			@Override
			public void run() {
			
//				if (isRecordWin2()) {
//					RecordWin2.getInstance().setWinSoft(mP1);
//				}
				WinRecord.getInstance().setWinSoft(mP1);
			}
		});
	}
	
	/**
	 * 是否开心消息添加时的进入动画
	 * @param enable
	 */
	public void enableMsgEntryAnim(boolean enable) {
		JNIHelper.logd("enableMsgEntryAnim :" + enable);
		ConfigUtil.enableChatAnimation(enable);
	}

	public void setWinFlags(int flags) {
		JNIHelper.logd(TAG + "setWinFlags:" + flags);
		AppLogic.runOnUiGround(new Runnable1<Integer>(flags) {
			@Override
			public void run() {
				if (isRecordWin2()) {
					RecordWin2.getInstance().setWinFlags(mP1);
				}
				WinRecord.getInstance().setWinFlags(mP1);
				SearchEditManager.getInstance().updateDialogWinFlag(mP1);
			}
		});
	}
	
	public void setSystemUiVisibility(int flags){
		JNIHelper.logd("setSystemUiVisibility:" + flags);
		if (getAdapter() != null && ADAPTER_RECORDWIN_2 != null && getAdapter() == ADAPTER_RECORDWIN_2) {
			AppLogic.runOnUiGround(new Runnable1<Integer>(flags) {
				@Override
				public void run() {
					RecordWin2.getInstance().setSystemUiVisibility(mP1);
				}
			});
			return;
		}
		if (getAdapter() != null && (getAdapter() == LOCAL_ADAPTER || getAdapter() == LOCAL_ADAPTER_INNER
				|| getAdapter() == COPY_ADAPTER)) {
			AppLogic.runOnUiGround(new Runnable1<Integer>(flags) {
				@Override
				public void run() {
					WinRecord.getInstance().setSystemUiVisibility(mP1);
				}
			});
			return;
		}
	}

	public void setIntentFlags(int flags) {
		JNIHelper.logd("setIntentFlags:" + flags);
		if (getAdapter() != null && ADAPTER_RECORDWIN_2 != null && getAdapter() == ADAPTER_RECORDWIN_2) {
			ReserveSingleInstanceActivity0.setIntentFlags(flags);
			return;
		}
		if (getAdapter() != null && (getAdapter() == LOCAL_ADAPTER || getAdapter() == LOCAL_ADAPTER_INNER
				|| getAdapter() == COPY_ADAPTER)) {
			WinRecordImpl3.getInstance().setIntentFlags(flags);
			return;
		}
	}

	public boolean isUI1_0Adapter() {
		if ((hasThirdImpl() && !mReserveInnerRecordWin) || PackageManager.getInstance().mInstalledRecord) {
			return true;
		}
		return false;
	}
	
	public RecordInvokeAdapter getAdapter() {
		RecordInvokeAdapter NewRecordInvokeAdapter;
		if (mForceLocalAdapter) {
			return LOCAL_ADAPTER_INNER;
		}
		if (mPluginInvokeAdapter != null) {
			mCurRecordInvokeAdapter = mPluginInvokeAdapter;
			return mPluginInvokeAdapter;
		}
		if (mUseRecordWin2 && !hasThirdImpl() && !PackageManager.getInstance().mInstalledRecord) {
			return ADAPTER_RECORDWIN_2;
		}
		if (hasThirdImpl()) {
			if (mReserveInnerRecordWin) {
				NewRecordInvokeAdapter = COPY_ADAPTER;
			} else {
				NewRecordInvokeAdapter = THIRD_ADAPTER;
			}
		} else {
			if (PackageManager.getInstance().mInstalledRecord) {
				NewRecordInvokeAdapter = REMOTE_ADAPTER;
			} else {
				NewRecordInvokeAdapter = LOCAL_ADAPTER;
			}
		}
		if (mCurRecordInvokeAdapter != null && mCurRecordInvokeAdapter != NewRecordInvokeAdapter) {
			if (mCurRecordInvokeAdapter == LOCAL_ADAPTER_INNER || mCurRecordInvokeAdapter == REMOTE_ADAPTER) {
				mCurRecordInvokeAdapter.dismiss();
			}
		}
		mCurRecordInvokeAdapter = NewRecordInvokeAdapter;
		return NewRecordInvokeAdapter;
	}

	public void snapPager(boolean isNext) {
		getAdapter().snapPager(isNext);
	}

	public boolean isSupportMapPoi() {
		if (mSupportPoiMap != null) {
			return mSupportPoiMap;
		}
		
		if (hasThirdImpl() && !mReserveInnerRecordWin) {
			return false;
		}
		if (PackageManager.getInstance().mInstalledRecord) {
			return false;
		}
		return true;
	}
	
	/**
	 * 判断是否支持新增UI内容，即支持UI2.0或使用我们的UI1.0的界面
	 * @return
	 */
	public boolean isSupportNewContent(){
		if (hasThirdImpl() && !mReserveInnerRecordWin) {
			return false;
		}
		if (PackageManager.getInstance().mInstalledRecord) {
			return false;
		}
		return true;
	}
	
	public boolean hasThirdImpl() {
		return !TextUtils.isEmpty(mThirdImpl);
	}


	//当前不支持1.0第三方界面，不支持自定义的皮肤包，支持配置文件的方式设置是否需要显示声控界面帮助
	public boolean canShowHelpTips(){
		return mShowHelpTips //用户配置
				||(!hasThirdImpl() && !isRecordWin2()) //默认ui1.0
				||(isRecordWin2() && ConfigUtil.isShowHelpTips()); //ui2.0 配置了显示帮助
	}

	private boolean mSupportMoreRecordState = false;
	public boolean isSupportMoreRecordState(){
		boolean support = false;
		if (mSupportMoreRecordState) {
			support = true;
		} else {
			if (WinManager.getInstance().isRecordWin2()) {
				if (ConfigUtil.supportMoreRecordState()) {
					support = true;
				}
			} else if (!WinManager.getInstance().hasThirdImpl()) {
				support = true;
			}
		}
		return support;
	}

	public String getThirdImpl2() {
		return mThirdImplWin2;
	}

	public boolean isRecordWin2() {
		return getAdapter() != null && ADAPTER_RECORDWIN_2 != null && getAdapter() == ADAPTER_RECORDWIN_2;
	}

	public boolean isReserveInnerRecordWin() {
		return mReserveInnerRecordWin;
	}

	public boolean isHudRecordWin() {
		return mIsInnerHudRecordWin;
	}
	
	/**
	 * args[0]:String // 插件View的ID args[1]:View // 显示的View
	 * args[2]:boolean // 是否替换上次相同的类型的View args[3]:boolean //
	 * 是否不是嵌入到聊天界面
	 */
	public void addPluginView(String typeId,View view,boolean replace,boolean isInDep){
		if (getAdapter() != null && ADAPTER_RECORDWIN_2 != null && getAdapter() == ADAPTER_RECORDWIN_2) {
			if (!isInDep) {
				RecordWin2Manager.getInstance().addView(RecordWinController.TARGET_CONTENT_FULL, view);
			} else {
				RecordWin2Manager.getInstance().addView(RecordWinController.TARGET_CONTENT_CHAT, view);
			}
			return;
		}
		if (getAdapter() != null && (getAdapter() == LOCAL_ADAPTER || getAdapter() == LOCAL_ADAPTER_INNER
				|| getAdapter() == COPY_ADAPTER)) {
			WinRecord.getInstance().addMsg(ChatMsgFactory.getPluginMessage(typeId, view, replace, isInDep));
			return;
		}
		
	}

	public byte[] invokeRecordWin2(final String packageName, String command, byte[] data) {
		if (command.equals("set")) {
			if (mUseRecordWin2) {
				mThirdImplWin2 = packageName;
			}
			return null;
		}
		if (command.equals("clear")) {
			mThirdImplWin2 = null;
			return null;
		}
		if (command.equals("forceUI1")) {
			switchUseUI1(true);
			return null;
		}
		if (command.equals("disableThirdWin")) {
			Boolean disable = Boolean.parseBoolean(new String(data));
			disableThirdWin2(disable);
			return null;
		}
		return null;
	}

	public byte[] invokeRecordWin(final String packageName, String command, byte[] data) {
		if (command.equals("setSystemUiVisibility")) {
			if (data != null) {
				JSONBuilder jsonBuilder = new JSONBuilder(data);
				Integer flag = jsonBuilder.getVal("flag", Integer.class);
				if (flag != null) {
					setSystemUiVisibility(flag);
				}
			}

			return null;
		}
		if (command.equals("prepare")) {
			mReserveInnerRecordWin = false;
			if (data != null) {
				try {
					JSONBuilder cfg = new JSONBuilder(data);
					mReserveInnerRecordWin = cfg.getVal("reserveInner", Boolean.class, false);
				} catch (Exception e) {
				}
			}
			JNIHelper.logd("set third win:" + packageName);
			mThirdImpl = packageName;
			return null;
		}
		if (command.equals("prepare.hud")) {
			try {
				if (data != null) {
					mIsInnerHudRecordWin = Boolean.parseBoolean(new String(data));
					FilmManager.getInstance().setWanMiControl(false);
					QiWuTicketManager.getInstance().setQiWuControl(false);
					JNIHelper.logd("mIsInnerHudRecordWin:" + mIsInnerHudRecordWin);
				}
			} catch (Exception e) {
			}
		}
		if (command.equals("clear")) {
			mReserveInnerRecordWin = false;
			mIsInnerHudRecordWin = false;
			mThirdImpl = null;
			JNIHelper.logd("clear third win");
			FilmManager.getInstance().setNewContent(false);
			QiWuTicketManager.getInstance().setNewContent(false);
			return null;
		}
		if (command.equals("closeHelpWin")) {
			WinHelpManager.getInstance().close(new JSONBuilder().put("type",WinHelpManager.TYPE_CLOSE_FROM_CLICK).toString());
		}
		if (command.equals("dissmiss")) {
			RecorderWin.close();
			return null;
		}
		if (command.equals("cancelClose")) {
			RecorderWin.cancelClose();
		}
		if (command.equals("showSysText")) {
			//在倒车影像中，不允许操作界面
			if (TXZPowerControl.isEnterReverse()) {
				return null;
			}
			RecorderWin.addSystemMsg(new String(data));
			return null;
		}
		if (command.equals("enterSpecifyAsrSence")) {
			//在倒车影像中，不允许操作界面
			if (TXZPowerControl.isEnterReverse()) {
				return null;
			}
			int sence = Integer.parseInt(new String(data));
			if (sence == 1) {
				String spk = NativeData.getResString("RS_RECORD_NAV");
				RecorderWin.open(spk, VoiceData.GRAMMAR_SENCE_NAVIGATE);
			} else if (sence == 2) {
				String spk = NativeData.getResString("RS_RECORD_CALL");
				RecorderWin.open(spk, VoiceData.GRAMMAR_SENCE_MAKE_CALL);
			}
		}
		if (command.equals("enterSpecifyAsrScene")) {
			//在倒车影像中，不允许操作界面
			if (TXZPowerControl.isEnterReverse()) {
				return null;
			}
			JSONBuilder json = new JSONBuilder(data);
			int scene = json.getVal("scene", Integer.class);
			String hintText = json.getVal("hintText", String.class);
			Boolean keepScene = json.getVal("keepScene", Boolean.class,true);
			Boolean needSpeak = json.getVal("needSpeak", Boolean.class,true);
			String strData = json.getVal("data", String.class);
			
			int grammar = -1;
			if (scene == 1) {
				grammar = VoiceData.GRAMMAR_SENCE_NAVIGATE;
			}else if (scene == 2) {
				grammar = VoiceData.GRAMMAR_SENCE_MAKE_CALL;
			}else if (scene == 3) {
				grammar = VoiceData.GRAMMAR_SENCE_MUSIC;
				if (!TextUtils.isEmpty(strData)) {
					MusicManager.getInstance().setStartAsrMusicTool(strData);
				}
			}
			if (grammar != -1) {
				if (keepScene) {
					AsrManager.getInstance().mKeepGrammar = grammar;
				}
				if (needSpeak) {
					RecorderWin.open(hintText, grammar);
				}else {
					RecorderWin.open("", grammar);
					RecorderWin.addSystemMsg(hintText);
				}
			}
			return null;
		}
		if (command.equals("speakTextOnRecordWin")) {
			//在倒车影像中，不允许操作界面
			if (TXZPowerControl.isEnterReverse()) {
				return null;
			}
			JSONBuilder json = new JSONBuilder(data);
			AsrManager.getInstance().setNeedCloseRecord(json.getVal("close", Boolean.class, true));
			String text = json.getVal("text", String.class);
			String resId = json.getVal("resId", String.class);
			final long taskId = json.getVal("taskId", Long.class, 0l);
            boolean isCancleExecute = json.getVal("isCancleExecute", Boolean.class, true);
			if (!TextUtils.isEmpty(resId)) {
				String res = NativeData.getResString(resId);
				if (res != null && res.length() != 0) {
					text = res;
					JNIHelper.logd("use res " + res + " instead of " + json);
				}
			}
			RecorderWin.speakTextWithClose(text, true, isCancleExecute, new Runnable() {
				@Override
				public void run() {
					ServiceManager.getInstance().sendInvoke(packageName, "sdk.record.win.speakTextOnRecordWin.end",
							(taskId+"").getBytes(), null);
				}
			});
			return null;
		}
		if (command.equals("enableAnim")) {
			//在倒车影像中，不允许操作界面
			if (TXZPowerControl.isEnterReverse()) {
				return null;
			}
			Boolean enable = Boolean.parseBoolean(new String(data));
			AppLogic.runOnUiGround(new Runnable1<Boolean>(enable) {
				@Override
				public void run() {
					WinRecord.getInstance().enableAnim(mP1);
				}
			});
			return null;
		}
		if("contentWidth".equals(command)){
			try {
				Integer width = Integer.parseInt(new String(data));
				if (width != null && width > 0) {
					setWinContentWidth(width);
				}
			} catch (Exception e) {
				LogUtil.loge("parseContentWidth", e);
			}
			return null;
		}
		if ("enableMsgEntryAnim".equals(command)) {
			try {
				Boolean enable = Boolean.parseBoolean(new String(data));
				enableMsgEntryAnim(enable);
			} catch (Exception e) {
				LogUtil.loge("parseMsgEntry:", e);
			}
			return null;
		}
		if ("dialog".equals(command)) {
			try {
				JSONObject job = new JSONObject(new String(data));
				final String message = job.getString("message");
				final String sureText = job.getString("sureText");
				JSONArray jry = new JSONArray(job.getString("sureCmds"));
				final String[] sureCmds = new String[jry.length()];
				for (int i = 0; i < sureCmds.length; i++) {
					sureCmds[i] = jry.getString(i);
				}
				final String cancelText = job.getString("cancelText");
				jry = new JSONArray(job.getString("cancelCmds"));
				final String[] cancelCmds = new String[jry.length()];
				for (int i = 0; i < cancelCmds.length; i++) {
					cancelCmds[i] = jry.getString(i);
				}
				final String hintText = job.getString("hintText");
				final String dialogId = packageName + "-" + job.getInt("taskId");
				AppLogic.runOnUiGround(new Runnable() {
					
					@Override
					public void run() {
						WinConfirmAsr.WinConfirmAsrBuildData buildData = new WinConfirmAsr.WinConfirmAsrBuildData();
						buildData.setMessageText(message);
						buildData.setSureText(sureText, sureCmds);
						buildData.setCancelText(cancelText, cancelCmds);
						buildData.setHintTts(hintText);
						WinConfirmAsr userDialog = new WinConfirmAsr(buildData) {
							@Override
							public void onClickOk() {
								ServiceManager.getInstance().sendInvoke(packageName, "sdk.record.win.dialog", "ok".getBytes(), null);
								canceUserlDialog(dialogId);
							}

							@Override
							public void onClickCancel() {
								ServiceManager.getInstance().sendInvoke(packageName, "sdk.record.win.dialog", "cancel".getBytes(), null);
								canceUserlDialog(dialogId);
							}

							@Override
							public String getReportDialogId() {
								return "txz_sdk_confirmAsr";
							}

							@Override
							protected void onEndTts() {
								super.onEndTts();
								ServiceManager.getInstance().sendInvoke(packageName, "sdk.record.win.dialog", "runnable".getBytes(), null);
							}
						};
						userDialog.show();
						if (mTaskDialog == null) {
							mTaskDialog = new HashMap<String, WinConfirmAsr>();
						}
						mTaskDialog.put(dialogId, userDialog);
						JNIHelper.logd("create dialog " + dialogId);
					}
				}, 0);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		if ("cancel.dialog".equals(command)) {
			String id = packageName + "-" + Integer.parseInt(new String(data));
			canceUserlDialog(id);
			return null;
		}

		// 同步声控界面状态
		if (command.equals("isOpened")) {
			return ("" + RecorderWin.isOpened()).getBytes();
		}
		if ("openShowText".equals(command)) {
			//在倒车影像中，不允许操作界面
			if (TXZPowerControl.isEnterReverse()) {
				return null;
			}
			LogUtil.logd("openShowText:" + new String(data) + " from:" + packageName);
			AppLogic.runOnUiGround(new Runnable1<byte[]>(data) {
				@Override
				public void run() {
					getAdapter().show();
					getAdapter().addMsg(RecorderWin.OWNER_SYS, new String(mP1));
					RecorderWin.mIsAlreadyOpened = true;
				}
			}, 0);
			return null;
		}
		if (command.equals("isSupportMapPoi")) {
			Boolean supp = Boolean.parseBoolean(new String(data));
			mSupportPoiMap = supp;
			LogUtil.logd(packageName + " supp:" + supp);
			return null;
		}
		return null;
	}
	
	public static void canceUserlDialog(String dialogId){
		if (mTaskDialog == null) {
			return;
		}
		WinConfirmAsr dialog = mTaskDialog.get(dialogId);
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss("cancel from sdk");
		}
		mTaskDialog.remove(dialogId);
		JNIHelper.logd("cancel dialog " + dialogId);
	}

	/**
	 * 更新banner广告view(进入声控后可以进行多次展示)
	 * @param view
	 */
	public boolean setBannerAdvertisingView(final View view){
		JNIHelper.logd(TAG + "Banner Advertising.");
		if (getAdapter() != null && ADAPTER_RECORDWIN_2 != null && getAdapter() == ADAPTER_RECORDWIN_2) {
			WinLayoutManager.getInstance().getLayout().setBannerAdvertisingView(view);
			return true;
		}

		if (getAdapter() != null && (getAdapter() == LOCAL_ADAPTER || getAdapter() == LOCAL_ADAPTER_INNER
				|| getAdapter() == COPY_ADAPTER)) {
			AppLogic.runOnUiGround(new Runnable1<View>(view) {
				@Override
				public void run() {
					WinRecord.getInstance().setBannerAdvertisingView(mP1);
				}
			});
			return true;
		}
		JNIHelper.logd(TAG + "Banner Advertising control false.");
		return false;

	}

	public void removeBannerAdvertisingView(){
		if (getAdapter() != null && ADAPTER_RECORDWIN_2 != null && getAdapter() == ADAPTER_RECORDWIN_2) {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					WinLayoutManager.getInstance().getLayout().removeBannerAdvertisingView();
				}
			});
		}

		if (getAdapter() != null && (getAdapter() == LOCAL_ADAPTER || getAdapter() == LOCAL_ADAPTER_INNER
				|| getAdapter() == COPY_ADAPTER)) {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					WinRecord.getInstance().removeBannerAdvertisingView();
				}
			});
		}
	}

	/**
	 * 添加背景广告进声控页面（进入声控只有一次展示）
	 * @param drawable
	 */
	public boolean setBackground(final Drawable drawable){
		JNIHelper.logd(TAG + "Background Advertising.");
		if (getAdapter() != null && ADAPTER_RECORDWIN_2 != null && getAdapter() == ADAPTER_RECORDWIN_2) {
			if(drawable == null){
				IWinLayout winLayout = WinLayoutManager.getInstance().getLayout();
				if(winLayout instanceof TXZWinLayout1 || winLayout instanceof TXZWinLayout2){
					LogUtil.d("drawable is null setBackground error");
					return false;
				}
			}
			UI2Manager.runOnUIThread(new Runnable1<Drawable>(drawable) {

				@Override
				public void run() {
					LogUtil.d("background ad setBg UI2.0");
					WinLayoutManager.getInstance().getLayout().setBackground(mP1);
				}
			},0);
			return true;
		}

		if (getAdapter() != null && (getAdapter() == LOCAL_ADAPTER || getAdapter() == LOCAL_ADAPTER_INNER
				|| getAdapter() == COPY_ADAPTER)) {
			AppLogic.runOnUiGround(new Runnable1<Drawable>(drawable) {
				@Override
				public void run() {
					LogUtil.d("background ad setBg UI1.0");
					WinRecord.getInstance().setBackground(mP1);
				}
			});
			return true;
		}
		JNIHelper.logd(TAG + "Background Advertising control false.");
		return false;

	}

	public static interface RecordInvokeAdapter {
		public void show();

		public void dismiss();

		public void refreshState(String type, int state);

		public void refreshVolume(int volume);

		public void refreshProgress(int val, int selection);

		public void refreshItemSelect(int selection);

		public void addMsg(int owner, String txt);

		public void addListMsg(String data);

		public void showStock(byte[] data);

		public void showWeather(byte[] data);

		public void snapPager(boolean next);

		public void addData(String data);
		
		public void dealKeyEvent(int keyEvent);
		
		public void sendInformation(String data);
		
		public void showMap(byte[] data);
	}
	

	private final RecordInvokeAdapter ADAPTER_RECORDWIN_2 = new RecordInvokeAdapter() {

		@Override
		public void snapPager(boolean next) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().showData(DataConvertUtil.convertSnapPage(next));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertSnapPage(next).getBytes(), null);
			}
		}

		@Override
		public void showWeather(byte[] data) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().showData(DataConvertUtil.convertWeather(data,NativeData.getResString("RS_VOICE_TIPS_WEATHER")));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertWeather(data,NativeData.getResString("RS_VOICE_TIPS_WEATHER")).getBytes(), null);
			}
		}

		@Override
		public void showStock(byte[] data) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().showData(DataConvertUtil.convertShock(data,NativeData.getResString("RS_VOICE_TIPS_STOCK")));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertShock(data,NativeData.getResString("RS_VOICE_TIPS_STOCK")).getBytes(), null);
			}
		}

		@Override
		public void show() {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().show();
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.show", null, null);
			}
		}

		@Override
		public void refreshVolume(int volume) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().updateVolume(volume);
				// RecordWin2Manager.getInstance().showData(DataConvertUtil.convertVolume(volume));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertVolume(volume).getBytes(), null);
			}
		}

		@Override
		public void  refreshState(String type, int state) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				if("record".equals(type)){
					RecordWin2Manager.getInstance().updateRecordState(state);
					return;
				}
				RecordWin2Manager.getInstance().showData(DataConvertUtil.convertState(type,state));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertState(type,state).getBytes(), null);
			}
		}

		@Override
		public void refreshProgress(int val, int selection) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().updateProgress(val, selection);
				// RecordWin2Manager.getInstance().showData(DataConvertUtil.convertProgress(val,
				// selection));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertProgress(val, selection).getBytes(), null);
			}
		}

		@Override
		public void refreshItemSelect(int selection) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().updateItemSelect(selection);
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertItemSelect(selection).getBytes(), null);
			}
		}

		@Override
		public void dismiss() {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().dismiss();
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.dismiss", null, null);
			}
		}

		@Override
		public void addMsg(int owner, String txt) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().showData(DataConvertUtil.convertMsg(owner, txt));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertMsg(owner, txt).getBytes(), null);
			}
		}

		@Override
		public void addListMsg(String data) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().showData(DataConvertUtil.convertList(data));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertList(data).getBytes(), null);
			}
		}

		@Override
		public void addData(String data) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().showData(DataConvertUtil.convertData(data));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertData(data).getBytes(), null);
			}
		}

		@Override
		public void dealKeyEvent(int keyEvent) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().showData(DataConvertUtil.convertKeyEvent(keyEvent));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertKeyEvent(keyEvent).getBytes(), null);
			}
		}
		
		@Override
		public void sendInformation(String data) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().showData(DataConvertUtil.convertInformation(data));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertInformation(data).getBytes(), null);
			}
		}
		@Override
		public void showMap(byte[] data) {
			if (TextUtils.isEmpty(mThirdImplWin2) || mDisableThirdWin2) {
				RecordWin2Manager.getInstance().showData(DataConvertUtil.convertMap(data));
			} else {
				ServiceManager.getInstance().sendInvoke(mThirdImplWin2, "win.record2.showData",
						DataConvertUtil.convertMap(data).getBytes(), null);
			}
		}
	};
	
	
	private final RecordInvokeAdapter LOCAL_ADAPTER_INNER = new RecordInvokeAdapter() {
		@Override
		public void show() {
			WinRecord.getInstance().show();
		}

		@Override
		public void dismiss() {
			WinRecord.getInstance().dismiss();
		}

		@Override
		public void  refreshState(String type, int state) {
			WinRecord.getInstance().notifyUpdateLayout(type, state);
		}

		@Override
		public void refreshVolume(int volume) {
			WinRecord.getInstance().notifyUpdateVolume(volume);
		}

		@Override
		public void refreshProgress(int val, int selection) {
			WinRecord.getInstance().notifyUpdateProgress(val, selection);
		}

		@Override
		public void refreshItemSelect(int selection) {
		}

		@Override
		public void addMsg(int owner, String txt) {
			if (owner == ChatMessage.OWNER_SYS) {
				WinRecord.getInstance().addMsg(ChatMsgFactory.getSysTextMsg(txt));
			} else if (owner == ChatMessage.OWNER_USER){
				WinRecord.getInstance().addMsg(ChatMsgFactory.getTextMsg(txt));
			} else if (owner == ChatMessage.OWNER_USER_PART) {
				WinRecord.getInstance().addMsg(ChatMsgFactory.getTextPartMsg(txt));
			}
		}

		@Override
		public void addListMsg(String data) {
//			if( !TextUtils.isEmpty(data) ){
//				JSONBuilder doc = new JSONBuilder(data);
//				Integer type = doc.getVal("type", Integer.class);
//				if (type != null && type == 2) {
//					View showPoiView= null;
//					BaseDisplayMsg createPoiMsg = ChatMsgFactory.createPoiMsg(data, null);
//					if(createPoiMsg instanceof PoiMsg){
//						showPoiView = WinPoiShow.getIntance().showPoiView(createPoiMsg);
//						((PoiMsg)createPoiMsg).mMapView = showPoiView;
//					}
//					WinRecord.getInstance().addMsg(createPoiMsg);
//					return ;
//				}else{
//					WinPoiShow.getIntance().stopWakeup();
//				}
//			}
			WinRecord.getInstance().addMsg(ChatMsgFactory.parseListMsgFromJson(data));
		}

		@Override
		public void showStock(byte[] data) {
			StockInfo infos = null;
			try {
				infos = StockInfo.parseFrom(data);
			} catch (Exception e) {
				JNIHelper.loge("StockData parse error!");
				return;
			}
			WinRecord.getInstance().addMsg(ChatMsgFactory.getStockMessage(infos));
		}

		@Override
		public void showWeather(byte[] data) {
			WeatherInfos infos = null;
			try {
				infos = WeatherInfos.parseFrom(data);
			} catch (Exception e) {
				JNIHelper.loge("WeatherData parse error!");
				return;
			}
			WinRecord.getInstance().addMsg(ChatMsgFactory.getWeatherMessage(infos));
		}

		@Override
		public void snapPager(boolean next) {
		}

		@Override
		public void addData(String data) {
			WinRecord.getInstance().addMsg(ChatMsgFactory.getDataMessage(data));
		}

		@Override
		public void dealKeyEvent(int keyEvent) {
			KeyEventManagerUI1.getInstance().onKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, keyEvent));
		}
		
		@Override
		public void sendInformation(String data) {
			
		}

		@Override
		public void showMap(byte[] data) {
			
		}
	};

	private static final RecordInvokeAdapter REMOTE_ADAPTER = new RecordInvokeAdapter() {

		@Override
		public void show() {
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.show", null, null);
		}

		@Override
		public void dismiss() {
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.dismiss", null, null);
		}

		@Override
		public void refreshState(String type, int state) {
			String data = new JSONBuilder().put("status", state).toString();
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.refresh", data.getBytes(),
					null);
		}

		@Override
		public void refreshVolume(int volume) {
			String data = new JSONBuilder().put("volume", volume).toString();
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.refresh.volume",
					data.getBytes(), null);
		}

		@Override
		public void refreshProgress(int val, int selection) {
			String data = new JSONBuilder().put("progress", val).toString();
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.refresh.progressbar",
					data.getBytes(), null);
		}

		@Override
		public void refreshItemSelect(int selection) {
		}

		@Override
		public void addMsg(int owner, String txt) {
			String data = new JSONBuilder().put("owner", owner).put("text", txt).toString();
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.chat", data.getBytes(), null);
		}

		@Override
		public void addListMsg(String data) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.list", data.getBytes(), null);
		}

		@Override
		public void showStock(byte[] data) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.show.stock", data, null);
		}

		@Override
		public void showWeather(byte[] data) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.show.weather", data, null);
		}

		@Override
		public void snapPager(boolean next) {
			if (next) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.list.next", null, null);
			} else {
				ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.list.pre", null, null);
			}
		}

		@Override
		public void addData(String data) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "txz.record.ui.show.data", data.getBytes(), null);
		}

		@Override
		public void dealKeyEvent(int keyEvent) {
			
		}
		
		@Override
		public void sendInformation(String data) {
			
		}
		@Override
		public void showMap(byte[] data) {

		}

	};

	private final RecordInvokeAdapter THIRD_ADAPTER = new RecordInvokeAdapter() {
		@Override
		public void show() {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.show", null, null);
		}

		@Override
		public void dismiss() {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.dismiss", null, null);
		}

		@Override
		public void refreshState(String type, int state) {
			String data = new JSONBuilder().put("status", state).toString();
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.status", data.getBytes(), null);
		}

		@Override
		public void refreshVolume(int volume) {
			String data = new JSONBuilder().put("volume", volume).toString();
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.volume", data.getBytes(), null);
		}

		@Override
		public void refreshProgress(int val, int selection) {
			String data = new JSONBuilder().put("progress", val).put("selection", selection).toString();
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.progress", data.getBytes(), null);
		}

		@Override
		public void refreshItemSelect(int selection) {
			String data = new JSONBuilder().put("selection",selection).toString();
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.onItemSelect", data.getBytes(), null);
		}

		@Override
		public void addMsg(int owner, String txt) {
			String data = new JSONBuilder().put("text", txt).toString();
			if (owner == ChatMessage.OWNER_SYS) { 
				ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.chat.sys", data.getBytes(), null);
			} else if (owner == ChatMessage.OWNER_USER) {
				ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.chat.usr", data.getBytes(), null);
			} else if (owner == ChatMessage.OWNER_USER_PART) {
				ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.chat.usr_part", data.getBytes(), null);
			}
		}

		@Override
		public void addListMsg(String data) {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.list", data.getBytes(), null);
		}

		@Override
		public void showStock(byte[] data) {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.stock", data, null);
		}

		@Override
		public void showWeather(byte[] data) {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.weather", data, null);
		}

		@Override
		public void snapPager(boolean next) {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.list.pager", (next + "").getBytes(), null);
		}

		@Override
		public void addData(String data) {
			ServiceManager.getInstance().sendInvoke(mThirdImpl, "win.record.data", data.getBytes(), null);
		}

		@Override
		public void dealKeyEvent(int keyEvent) {
			
		}
		
		@Override
		public void sendInformation(String data) {
			
		}
		@Override
		public void showMap(byte[] data) {
		}
	};
	
	private final RecordInvokeAdapter LOCAL_ADAPTER = (RecordInvokeAdapter) Proxy.newProxyInstance(
			LOCAL_ADAPTER_INNER.getClass().getClassLoader(), LOCAL_ADAPTER_INNER.getClass().getInterfaces(),
			new RecordProxy());

	class RecordProxy2 implements InvocationHandler {
		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			if (mUseRecordWin2) {
				try {
					method.invoke(THIRD_ADAPTER, args);
				} catch (Exception e) {
				}
				try {
					return method.invoke(ADAPTER_RECORDWIN_2, args);
				} catch (Exception e) {
				}
				
			}else {
				try {
					method.invoke(THIRD_ADAPTER, args);
				} catch (Exception e) {
				}
				try {
					return method.invoke(LOCAL_ADAPTER, args);
				} catch (Exception e) {
				}
			}
			return null;
		}
	}

	private final RecordInvokeAdapter COPY_ADAPTER = (RecordInvokeAdapter) Proxy.newProxyInstance(
			LOCAL_ADAPTER_INNER.getClass().getClassLoader(), LOCAL_ADAPTER_INNER.getClass().getInterfaces(),
			new RecordProxy2());
	
	class RecordProxy implements InvocationHandler {
		@Override
		public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					try {
						method.invoke(LOCAL_ADAPTER_INNER, args);
					} catch (Exception e) {
					}
				}
			}, 0);
			return null;
		}
	}
	
}