package com.txznet.txz.ui.win.help;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.zxing.WriterException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.txz.ui.data.UiData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.util.DateUtils;
import com.txznet.comm.util.FilePathConstants;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.bean.DisplayConMsg;
import com.txznet.record.helper.ChatMsgFactory;
import com.txznet.record.keyevent.KeyEventManagerUI1;
import com.txznet.record.ui.WinRecord;
import com.txznet.record.view.DisplayLvRef;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.txz.R;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.choice.IChoice;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.download.DownloadManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.reminder.ReminderManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.userconf.UserConf;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.ui.win.help.HelpDetail.HelpDetailItem;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.StatusObervable.StatusObserver;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.QRUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;
import com.txznet.txz.util.runnables.Runnable3;
import com.txznet.txz.util.runnables.Runnable4;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

/**
 *	帮助界面
 *	1.兼容UI1.0，兼容第三方界面
 *	TODO 加入判断，是否是新版本的皮肤包，新版本的皮肤包才支持帮助三级界面
 */
public class WinHelpDetailTops implements IChoice<Void>{
	private static WinHelpDetailTops mInstance;
	private boolean mIsSelecting;

	PageHelper mPageHelper;
	static boolean mHasWakeup;
	static int mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;

	private ArrayList<HelpDetail> mCacheHelpDetails = null;
	private int mShowType = SHOW_TYPE_NORMAL;
	private String mImgPath = "";
	private boolean isFromFile = false;
	private String tipS = "";
	private boolean isShowTips = false;
	private HelpDetail mGuideHelpDetail = null;
	
	//默认的时间和默认的时间格式
	private static final String DEF_TIME = "2017-12-25 00:00:00";
	private static final String DEF_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	//默认可以打开帮助详情页
	private boolean canOpenDetail = true;

	//第一页显示的提示条数
	private static final int SHOW_DESC_COUNT = 2;

	/**显示的类型
	 * SHOW_TYPE_NORMAL:全部亮显
	 * SHOW_TYPE_NET_GRAY:是没网状态下在线命令暗显
	 * SHOW_TYPE_NET_HIDE:没网状态下在线命令不显示
	 */
	public static final int SHOW_TYPE_NORMAL = 0;
	public static final int SHOW_TYPE_NET_GRAY = 1;
	public static final int SHOW_TYPE_NET_HIDE = 2;

	/**指令的类型
	 * CMD_NET_TYPE_MIX:在线和离线都可以
	 * CMD_NET_TYPE_NET:在线指令
	 * CMD_NET_TYPE_LOCAL:离线指令
	 */
	public static final int CMD_NET_TYPE_MIX = 0;
	public static final int CMD_NET_TYPE_NET = 1;
	public static final int CMD_NET_TYPE_LOCAL = 2;

	/**
	 * 帮助三级页面打开的类型
	 * CMD_OPEN_TYPE_NORMAL:普通的列表
	 * CMD_OPEN_TYPE_IMAGE_TEXT:图片模式
	 * CMD_OPEN_TYPE_APP:打开app模式
	 * CMD_OPEN_TYPE_MIX:图片和文本混合模式
	 */
	public static final int CMD_OPEN_TYPE_NORMAL = 0;
	public static final int CMD_OPEN_TYPE_IMAGE_TEXT = 1;
	public static final int CMD_OPEN_TYPE_APP = 2;
	public static final int CMD_OPEN_TYPE_MIX = 3;

	private WinHelpDetailDialog mWinHelpDetailDialog;

	//二维码内容t
	private String mQRCodeTitleIcon;//二维码标题icon
	private String mQRCodeTitle;//二维码标题
	private String mQRCodeUrl;//二维码链接
	private String mQRCodeDesc;//描述
	private String mQRCodeGuideDesc;//二维码引导描述
	private String mQRCodeGuideShowInterval;//二维码引导间隔：周week、月month
	public boolean mQRCodeNeedShowGuide;//二维码引导首次下发是否需要立即展示
	private boolean isShowQRCode = true;

	//三级页面二维码
	private String[] mQRCodeDetailNameList;
	private String mQRCodeDetailUrl;
	private String mQRCodeDetailDesc;

	private static final long ONE_DAY_TIME = 24 * 60 * 60 * 1000;
	private static final int QRCODE_USER_COUNT = 2;
	
	public WinHelpDetailTops() {
		RecorderWin.OBSERVABLE.registerObserver(new StatusObserver() {

			@Override
			public void onShow() {
			}

			@Override
			public void onDismiss() {
				if (!hasThirdImpl()) {
					AppLogic.removeUiGroundCallback(mDismissRun);
					AppLogic.runOnUiGround(mDismissRun, 10);
				}
			}
		});
	}
	
	Runnable mDismissRun = new Runnable() {
		
		@Override
		public void run() {
			dismiss("");
		}
	};

	public void snapPage(boolean isNext) {
		if (!mIsSelecting) {
			return;
		}

		boolean bSucc = true;
		if (isNext) {
			bSucc = mPageHelper.nextPage();
			ReportUtil.doReport(
					new ReportUtil.Report.Builder()
							.setAction("helplist")
							.setType("nextPage")
							.putExtra("snaptype",WinHelpManager.TYPE_SNAP_PAGE_FROM_CLICK)
							.setSessionId()
							.buildCommReport());
		} else {
			bSucc = mPageHelper.prePage();
			ReportUtil.doReport(
					new ReportUtil.Report.Builder()
							.setAction("helplist")
							.setType("prePage")
							.putExtra("snaptype",WinHelpManager.TYPE_SNAP_PAGE_FROM_CLICK)
							.setSessionId()
							.buildCommReport());
		}

		if (bSucc) {
			showList();
			beginWakeup();
		}
	}

	private void speakText(String spk) {
		TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
		mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
		mSpeechTaskId = TtsManager.getInstance().speakText(spk,new TtsUtil.ITtsCallback() {
			@Override
			public boolean isNeedStartAsr() {
				return true;
			}
		});
	}
	
	//用来实现跳转到帮助详情列表再返回
//	private static int currentPage = 0;
//	private static boolean isBackFromDetail = false;
//	public void updateCurPage(int curPage){
//		currentPage = curPage + 1;
//		isBackFromDetail = true;
//	}
	
	//
	private HelpDetail mSelectHelpDetail = null;

	public void show(String params) {
		WinHelpManager.getInstance().updateCloseIconState(true);
		RecorderWin.removeHelpTip();
		if (mIsSelecting) {
			return;
		}
		JSONBuilder jsonBuilder = new JSONBuilder(params);
		int openType = jsonBuilder.getVal("type",Integer.class,0);
		int selectPage = jsonBuilder.getVal("selectPage",Integer.class,0);
		ReportUtil.doReport(
				new ReportUtil.Report.Builder()
						.setAction("helplist")
						.setType("open")
						.putExtra("opentype",openType)
						.putExtra("isNew",HelpPreferenceUtil.getInstance().getBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_NEWS, false))
						.setSessionId()
						.buildCommReport());
		//小红点显示的时候，通知隐藏
		if (HelpPreferenceUtil.getInstance().getBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_NEWS, true)) {
			JSONBuilder info = new JSONBuilder();
			info.put("type", 0);
			info.put("showHelpNewTag", false);
			RecorderWin.sendInformation(info.toString());
			ConfigUtil.setShowHelpNewTag(false);
			HelpPreferenceUtil.getInstance().setBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_NEWS, false);
		}

		getHelpMsg();
		AsrManager.getInstance().cancel();
		TtsManager.getInstance().pause();
		NativeData.getNativeData(UiData.DATA_ID_VOICE_CANCEL_PARSE);
		TextResultHandle.getInstance().cancel();
		ChoiceManager.getInstance().clearIsSelectingClickHelp();

		mSelectHelpDetail = null;

		isShowTips = false;

		mIsSelecting = true;

		initQRCodeData();//二维码数据加载
		initData();
		GlobalContext.get().sendBroadcast(new Intent("com.txznet.txz.action.WIN_HELP_DETAIL_SHOW"));
		if (hasThirdImpl()) {
			try {
//				GlobalObservableSupport.getHomeObservable().registerObserver(mHomeReceiver);
				GlobalContext.get().registerReceiver(mWinRecordReceiver, new IntentFilter("com.txznet.txz.record.show"));
			}catch (Exception e) {
				LogUtil.loge("homeReceiver",e);
			}
		}else {
			//拦截帮助弹出事件，当需要弹出第三级界面的时候，mSelectHelpDetail不为空，直接跳转到三级界面
			if (null != mSelectHelpDetail) {
				HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_HELP_DETAIL_NAME, "");
				//帮助没有子项,不应该出现的
				if (mSelectHelpDetail.detailItems.isEmpty()) {
				}else {
					dismiss(new JSONBuilder().put("type",WinHelpManager.TYPE_CLOSE_FROM_DETAIL).toString());
					//设置默认的返回页是第一页
					WinHelpDetailSelector.getInstance().showHelpNewsList(mSelectHelpDetail, mSelectHelpDetail.isNew , NativeData.getResPlaceholderString("RS_HELP_DETAIL_TIPS_WITH_NEW_ITEM","%ITEM%",mSelectHelpDetail.name) , 0,isFromFile);

					if (mSelectHelpDetail.isNew) {
						HelpPreferenceUtil.getInstance().setString(mSelectHelpDetail.name,mSelectHelpDetail.time);
						mSelectHelpDetail.isNew = false;
						for (HelpDetailItem mHelpDetailItem: mSelectHelpDetail.detailItems ) {
							if (mHelpDetailItem.isNew) {
								HelpPreferenceUtil.getInstance().setString(mHelpDetailItem.name,mHelpDetailItem.time);
								mHelpDetailItem.isNew = false;
							}
						}
					}
					return;
				}

			}
		}

		//如果没有可显示的数据，使用默认的帮助
		if (helpDetails.size() == 0) {
			LogUtil.loge("load helpdata error");
			initData(true);
		}

		HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_HELP_DETAIL_NAME, "");

		mPageHelper = new PageHelper();

		Integer pageSize = getPageSize();

		mPageHelper.reset(helpDetails.size(), pageSize);
		mPageHelper.selectPage(selectPage + 1);
		showList();
		beginWakeup();
		if (openType == WinHelpManager.TYPE_OPEN_FROM_BACK) {
			speakText(NativeData.getResString("RS_HELP_DETAIL_BACK"));
		}else if (openType == WinHelpManager.TYPE_OPEN_FROM_VOICE){
			//从声控过来的不播报已为您打开帮助
			speakText("");
		}else if (openType == WinHelpManager.TYPE_OPEN_FROM_CLICK) {
			speakText(NativeData.getResString("RS_HELP_INTRO_HINT"));
		} else {
			speakText(NativeData.getResString("RS_HELP_INTRO_HINT"));
		}
	}

	public int getPageSize(){
		Integer pageSize = ChoiceManager.getInstance().getNumPageSize(TXZConfigManager.PageType.PAGE_TYPE_HELP_LIST.name());
		if (pageSize == null) {
			pageSize = ChoiceManager.getInstance().getNumPageSize();
		}
		if(isShowQRCode){
			if(!WinManager.getInstance().isRecordWin2() || hasThirdImpl()){
				if (ScreenUtil.getScreenHeight() > ScreenUtil.getScreenWidth()) {
					pageSize = pageSize - QRCODE_USER_COUNT;
				}
			}
		}
		return pageSize;
	}
	
	public void onItemSelected(int index,boolean fromVoice) {
		if (mIsSelecting) {
			if (canOpenDetail) {
				if (tmpList != null) {
					if (index < tmpList.size()) {
						HelpDetail helpDetail = tmpList.get(index);
						ReportUtil.doReport(
								new ReportUtil.Report.Builder()
										.setAction("helplist")
										.setType("click")
										.putExtra("name", helpDetail.name)
										.putExtra("openType",fromVoice?WinHelpManager.TYPE_OPEN_FROM_CLICK:WinHelpManager.TYPE_OPEN_FROM_VOICE)
										.putExtra("isNew",helpDetail.isNew)
										.setSessionId()
										.buildCommReport());
						if (fromVoice) {
							RecorderWin.refreshItemSelect(index);
						}
						onItemSelected(helpDetail, mPageHelper.getCurPage());
					}
				}
			}
		}
	}

	private boolean onItemSelected(HelpDetail helpDetail,int index){
		boolean isSelected = false;
		switch (helpDetail.openType) {
			case CMD_OPEN_TYPE_NORMAL:
				//帮助没有子项,不应该出现的
				if (!helpDetail.detailItems.isEmpty()) {
					dismiss(new JSONBuilder().put("type", WinHelpManager.TYPE_CLOSE_FROM_DETAIL).toString());
					WinHelpDetailSelector.getInstance().showHelpList(helpDetail, helpDetail.isNew, helpMsg, index, isFromFile);
					isSelected = true;
				}
				break;
			case CMD_OPEN_TYPE_IMAGE_TEXT:
				if (!helpDetail.detailImgs.isEmpty()) {
					dismiss(new JSONBuilder().put("type", WinHelpManager.TYPE_CLOSE_FROM_DETAIL).toString());
					WinHelpDetailSelector.getInstance().showHelpImageText(helpDetail, helpDetail.isNew, isGuideHelp(helpDetail)?"":helpMsg,index, isFromFile);
					isSelected = true;
				}
				break;
			case CMD_OPEN_TYPE_APP:
				if (!TextUtils.isEmpty(helpDetail.strPackage)) {
					if (TextUtils.equals(helpDetail.strPackage, HelpGuideManager.GUIDE_PACKAGE_NAME)) {
						//新手引导需要先关闭掉Core
						RecorderWin.close();
						HelpGuideManager.getInstance().startGuideAnimFromHelp(index);
					}else {
						PackageManager.getInstance().openApp(helpDetail.strPackage);
					}
					isSelected = true;
				}
				RecorderWin.close();
				break;
			case CMD_OPEN_TYPE_MIX:
				if (helpDetail.detailItems != null && helpDetail.detailImgs != null) {
					dismiss(new JSONBuilder().put("type", WinHelpManager.TYPE_CLOSE_FROM_DETAIL).toString());
					WinHelpDetailSelector.getInstance().showMixHelp(helpDetail, helpDetail.isNew, helpMsg, index, isFromFile);
					isSelected = true;
				}
				break;
		}

		if (helpDetail.isNew) {
			HelpPreferenceUtil.getInstance().setString(helpDetail.name, helpDetail.time);
			helpDetail.isNew = false;
			for (HelpDetailItem mHelpDetailItem : helpDetail.detailItems) {
				if (mHelpDetailItem.isNew) {
					HelpPreferenceUtil.getInstance().setString(mHelpDetailItem.name, mHelpDetailItem.time);
					mHelpDetailItem.isNew = false;
				}
			}

		}
		return isSelected;
	}
	
	String helpMsg = "";
	private ArrayList<HelpDetail> tmpList = new ArrayList<HelpDetail>();
	
	private ArrayList<String> valueKeys(String[] keys){
		ArrayList<String> listKeys = new ArrayList<String>();
		if (keys!=null) {
			for (String key : keys) {
				if (!TextUtils.isEmpty(key)) {
					listKeys.add(key);
					LogUtil.loge(key);
				}
			}
		}
		return listKeys;
	}
	
	public void getHelpMsg() {
		//设置关闭唤醒和适配关闭唤醒
		Boolean mWakeupEnable = UserConf.getInstance().getUserConfigData().mWakeupEnable;
		if (!WakeupManager.getInstance().mEnableWakeup || (mWakeupEnable != null && !mWakeupEnable)) {
			helpMsg = NativeData.getResString("RS_HELP_DETAIL_TIPS_NO_KEYS");
		}  else {
			List<String> userKeys = valueKeys(WakeupManager.getInstance().getWakeupKeywords_User());
			List<String> sdkKeys = valueKeys(WakeupManager.getInstance().getWakeupKeywords_Sdk());
			if (userKeys.isEmpty()) {
				if (sdkKeys.isEmpty()) {
					helpMsg = NativeData.getResString("RS_HELP_DETAIL_TIPS_NO_KEYS");
				} else {
					helpMsg = NativeData.getResPlaceholderString("RS_HELP_DETAIL_TIPS_WITH_KEYS", "%KEY%", sdkKeys.get(0));
				}
			} else {
				helpMsg = NativeData.getResPlaceholderString("RS_HELP_DETAIL_TIPS_WITH_KEYS", "%KEY%", userKeys.get(userKeys.size() - 1));

			}
		}
	}

	private void snapPager(boolean isNext, boolean bSucc, String command) {
		if (bSucc) {
			showList();
			beginWakeup();
		}

		String endSpk = "";
		String pager = command;
		if (command.contains("翻")) {
			pager = NativeData.getResString("RS_SELECTOR_SELECT_PAGE").replace(
					"%CMD%", command);
		} else {
			pager = NativeData.getResPlaceholderString("RS_SELECTOR_SELECT",
					"%CMD%", command);
		}

		if (!bSucc) {
			String slot = "";
			if (isNext) {
				slot = NativeData.getResString("RS_SELECTOR_THE_LAST");
			} else {
				slot = NativeData.getResString("RS_SELECTOR_THE_FIRST");
			}

			endSpk = NativeData.getResString("RS_SELECTOR_PAGE_COMMAND")
					.replace("%NUM%", slot);
		}

		speakText(bSucc ? pager : endSpk);
	}

	private void showList() {
		final int curPage = mPageHelper.getCurPage();
		final int pageSize = mPageHelper.getPageSize();
		final int sIndex = curPage * pageSize;
		sendCineList(sIndex, pageSize);
	}
	
	

	private void sendCineList(int sIndex, int count) {
		if (helpDetails == null) {
			return;
		}

		final int c = helpDetails.size();
		if (sIndex >= c) {
			return;
		}

		JSONBuilder jBuilder = new JSONBuilder();
		jBuilder.put("type", 7);
		jBuilder.put("keywords", "帮助");
		jBuilder.put("prefix", helpMsg);
		jBuilder.put("curPage", mPageHelper.getCurPage());
		jBuilder.put("maxPage", mPageHelper.getMaxPage());

		tmpList.clear();
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < count; i++) {
			if (sIndex >= helpDetails.size()) {
				break;
			}

			HelpDetail mHelpDetail = helpDetails.get(sIndex);
			tmpList.add(mHelpDetail);
			JSONObject obj;
			if (isFromFile) {
				obj = new JSONBuilder()
				.put("title", mHelpDetail.name)
				.put("desps", mHelpDetail.desps)
				.put("intro", mHelpDetail.intro)
				.put("iconName", mHelpDetail.iconName)
				.put("time", mHelpDetail.time)
				.put("isNew", mHelpDetail.isNew && HelpPreferenceUtil.getInstance().getBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_TAG, false))
				.put("isFromFile", isFromFile)
				.getJSONObject();
			}else {
				obj = new JSONBuilder()
				.put("title", mHelpDetail.title)
				.put("desps", mHelpDetail.desps)
				.put("intro", mHelpDetail.intro)
				.put("iconName", mHelpDetail.iconName)
				.put("isNew", mHelpDetail.isNew && HelpPreferenceUtil.getInstance().getBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_TAG, false) )
				.getJSONObject();
			}
			jsonArray.put(obj);
			sIndex++;
		}
		jBuilder.put("canOpenDetail",canOpenDetail);
		jBuilder.put("isShowTips",isShowTips);
		jBuilder.put("tips",tipS);
		jBuilder.put("helps", jsonArray);
		jBuilder.put("count", jsonArray.length());

		if (isShowQRCode) {
			jBuilder.put("qrCodeTitleIcon", mQRCodeTitleIcon);
			jBuilder.put("qrCodeTitle", mQRCodeTitle);
			jBuilder.put("qrCodeUrl", mQRCodeUrl);
			jBuilder.put("qrCodeDesc", mQRCodeDesc);
			jBuilder.put("qrCodeGuideDesc", mQRCodeGuideDesc);
			jBuilder.put("qrCodeNeedShowGuide", mQRCodeNeedShowGuide);
		}

		 if (WinManager.getInstance().isRecordWin2()) {
		 // ui2.0框架直接发送数据,将type转换成8
			 jBuilder.put("type", 8);
			 RecorderWin.sendSelectorList(jBuilder.toString());
			 isShowTips = false;
			 return;
		 }

		 if (hasThirdImpl()) {
			AppLogic.runOnUiGround(new Runnable1<JSONBuilder>(jBuilder) {
				@Override
				public void run() {
					if (mWinHelpDetailDialog == null) {
						mWinHelpDetailDialog = new WinHelpDetailDialog();
					}
					mWinHelpDetailDialog.replaceView((DisplayConMsg) ChatMsgFactory.createContainMsg(mP1.toString(),createView(tmpList,isFromFile,isShowTips)));
					mWinHelpDetailDialog.showImediately();
				}
			});
		 	return;
		 }

		AppLogic.runOnUiGround(new Runnable1<JSONBuilder>(jBuilder) {

			@Override
			public void run() {
				// 直接发送给本地界面
				WinRecord.getInstance().addMsg(ChatMsgFactory.createContainMsg(mP1.toString(), createView(tmpList,isFromFile,isShowTips)));
				isShowTips = false;
				if (helpList != null) {
					helpList.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
						@SuppressLint("NewApi")
						@Override
						public void onGlobalLayout() {
							// KeyEventManagerUI1.getInstance().updateListAdapter(detailAdapter);
							KeyEventManagerUI1.getInstance().updateListView(helpList);
							helpList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						}
					});
				}
			}
		}, 0);
	}

	private void beginWakeup() {
		AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {
			Runnable removeBackground = null;
			Runnable2<Object, String> taskRunnable = null;
			private static final int speechDelay = 700;
			private static final int handleDelay = 800;
			private boolean isEnd = false;
			private long mLastSpeechEndTime = 0;
			@Override
			public boolean needAsrState() {
				if (InterruptTts.getInstance().isInterruptTTS()) {//如果是识别模式，就不需要开启beep音
					return false;
				}else {
					return true;
				}
			}

			@Override
			public String getTaskId() {
				return TASK_HELP;
			}
			
			public void onSpeechEnd() {
				mLastSpeechEndTime = SystemClock.elapsedRealtime();
				if(removeBackground != null){
					AppLogic.removeBackGroundCallback(removeBackground);
				}
			};

			@Override
			public void onCommandSelected(String type, String command) {
				if (taskRunnable != null) {
					AppLogic.removeBackGroundCallback(taskRunnable);
					taskRunnable = null;
				}
				taskRunnable = new Runnable2<Object, String>(type,command) {
					
					@Override
					public void run() {
						
						JNIHelper.logd("do onCommandSelected");
						
						String type = (String) mP1;
						String command = mP2;
						isEnd = true;
						if ("滴个".equals(command)) {
							command = "第一个";
						}
						if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
							//唤醒结果执行时，如果还在录音，则取消掉
							if (AsrManager.getInstance().isBusy()) {
								AsrManager.getInstance().cancel();
							}
						}
						if ("HELP$CANCEL".equals(type)) {
							dismiss(new JSONBuilder().put("type",WinHelpManager.TYPE_CLOSE_FROM_VOICE).toString());
							RecorderWin.setLastUserText(command);
							if (!hasThirdImpl()) {
								RecorderWin.open(NativeData.getResString("RS_SELECTOR_HELP"));
							}
						} else if ("HELP$NEXTPAGE".equals(type)) {
							snapPager(true, mPageHelper.nextPage(), command);
							ReportUtil.doReport(
									new ReportUtil.Report.Builder()
											.setAction("helplist")
											.setType("nextPage")
											.putExtra("snaptype",WinHelpManager.TYPE_SNAP_PAGE_FROM_VOICE)
											.setSessionId()
											.buildCommReport());
						} else if ("HELP$PREPAGE".equals(type)) {
							snapPager(false, mPageHelper.prePage(), command);
							ReportUtil.doReport(
									new ReportUtil.Report.Builder()
											.setAction("helplist")
											.setType("prePage")
											.putExtra("snaptype",WinHelpManager.TYPE_SNAP_PAGE_FROM_VOICE)
											.setSessionId()
											.buildCommReport());
						} else if (type.startsWith("HELP_PAGE_INDEX_")) {
							int index = Integer.parseInt(type
									.substring("HELP_PAGE_INDEX_".length()));
							speakText(NativeData.getResPlaceholderString(
									"RS_SELECTOR_SELECT", "%CMD%", command));
							mPageHelper.selectPage(index);
							showList();
							beginWakeup();
						} else if (type.startsWith("ITEM_INDEX_")) {
							int index = Integer.parseInt(type.substring("ITEM_INDEX_".length()));
							onItemSelected(index,true);
							return;
						}
					}
				};
				removeBackground = new Runnable() {
					
					@Override
					public void run() {
						AppLogic.removeBackGroundCallback(taskRunnable);
					}
				};
				if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
					if (isWakeupResult()) {//是唤醒的结果
						isEnd = false;
						//判断唤醒的说话结束了
						if (SystemClock.elapsedRealtime() - mLastSpeechEndTime < 300) {
							AppLogic.runOnBackGround(taskRunnable, 0);
							AppLogic.removeBackGroundCallback(removeBackground);
						}else {
							AppLogic.runOnBackGround(removeBackground, speechDelay);
							AppLogic.runOnBackGround(taskRunnable, handleDelay);							
						}
					} else if (!isEnd) {//识别到的唤醒词并且唤醒没有执行完成
						AppLogic.runOnBackGround(taskRunnable, 0);
						AppLogic.removeBackGroundCallback(removeBackground);
					}
				}else {
					taskRunnable.run();
				}
			}
					
		}.addCommand("HELP$CANCEL",
				NativeData.getResStringArray("RS_SELECT_WAKEUP_CANCEL"));

		if (mPageHelper.getMaxPage() > 1) {
			acsc.addCommand("HELP$NEXTPAGE",
					NativeData.getResStringArray("RS_SELECT_WAKEUP_NEXTPAGE"))
					.addCommand(
							"HELP$PREPAGE",
							NativeData
									.getResStringArray("RS_SELECT_WAKEUP_PREPAGE"));

			int pageSize = mPageHelper.getMaxPage();
			int i = 1;
			for (; i <= pageSize; i++) {
				String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i);
				acsc.addCommand("HELP_PAGE_INDEX_" + i, "第" + strIndex + "页");
			}
			acsc.addCommand("HELP_PAGE_INDEX_" + (i - 1), "最后一页");
		}

		if (canOpenDetail) {
			for (int i = 0; i < tmpList.size(); i++) {
				String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i + 1);
				if (i == 0) {
					String[] cmd = NativeData.getResStringArray("RS_CMD_SELECT_FIRST");
					String[] tmp = new String[cmd.length + 3];
					System.arraycopy(cmd, 0, tmp, 0, cmd.length);
					String name = tmpList.get(i).name.replaceAll("/","");
					tmp[cmd.length] = name;
					tmp[cmd.length + 1] ="查看" + name;
					tmp[cmd.length + 2] ="打开" + name;

					acsc.addCommand("ITEM_INDEX_" + i, tmp);
				} else {
					String name = tmpList.get(i).name.replaceAll("/","");
					acsc.addCommand("ITEM_INDEX_" + i, "第" + strIndex + "个", "第" + strIndex + "条",name, "查看" + name, "打开" + name);
					}
			}
		}

		mHasWakeup = true;
		WakeupManager.getInstance().useWakeupAsAsr(acsc);
	}

	public static final String TASK_HELP = "WinHelpControl";
	
	void clearIsSelecting_Inner() {
		if (mWinHelpDetailDialog != null && mWinHelpDetailDialog.isShowing()) {
			mWinHelpDetailDialog.dismiss("do dismiss");
		}
		WinHelpManager.getInstance().closeQRCodeDialog();
//		currentPage = 0;
//		isBackFromDetail = false;

		if (mHasWakeup) {
			WinHelpManager.getInstance().resetCloseIconState();
			mHasWakeup = false;
			TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			WakeupManager.getInstance().recoverWakeupFromAsr(TASK_HELP);
			ChoiceManager.getInstance().clearIsSelecting();
			helpDetails.clear();
			tmpList.clear();
		}
		mIsSelecting = false;

	}
	
	public void dismiss(String params) {
		WinHelpManager.getInstance().resetCloseIconState();
		if ( !TextUtils.isEmpty(params)) {
			ReportUtil.doReport(new ReportUtil.Report.Builder()
					.setType("helplist")
					.setAction("close")
					.putExtra("closetype", new JSONBuilder(params).getVal("type", Integer.class, WinHelpManager.TYPE_CLOSE_FROM_OTHER))
					.setSessionId()
					.buildCommReport());
		}
		if (mWinHelpDetailDialog != null && mWinHelpDetailDialog.isShowing() ) {
			mWinHelpDetailDialog.dismiss("do dismiss");
		}
//		currentPage = 0;
//		isBackFromDetail = false;
		if (mHasWakeup) {
			mHasWakeup = false;
			TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
			WakeupManager.getInstance().recoverWakeupFromAsr(TASK_HELP);
			GlobalContext
					.get()
					.sendBroadcast(
							new Intent(
									"com.txznet.txz.action.WIN_HELP_DETAIL_DISMISS"));
			ChoiceManager.getInstance().clearIsSelecting();
			helpDetails.clear();
			tmpList.clear();
		}
		if (hasThirdImpl()) {
			try {
//				GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeReceiver);
				GlobalContext.get().unregisterReceiver(mWinRecordReceiver);
			}catch (Exception e) {
				LogUtil.loge("homeReceiver",e);
			}
		}
		mIsSelecting = false;
	}

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static WinHelpDetailTops getInstance() {
		if (mInstance == null) {
			synchronized (WinHelpDetailTops.class) {
				if (mInstance == null) {
					mInstance = new WinHelpDetailTops();
				}
			}
		}
		return mInstance;
	}

	private void back() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.help.ui.detail.back", null, null);
	}

	private ListView helpList;
	private ArrayList<HelpDetail> helpDetails;

	protected View createView(List<HelpDetail> helpDetails, boolean isFromFile ,boolean showToast) {
		boolean needShowQRCode = false;
		int resourceId = R.layout.win_help_detail;
		if(!TextUtils.isEmpty(mQRCodeUrl)){
			needShowQRCode = true;
			if(ScreenUtil.getScreenWidth()>ScreenUtil.getScreenHeight()){
				resourceId = R.layout.win_help_qrcode_detail;
			}else{
				resourceId = R.layout.win_help_qrcode_detail_port;
			}
		}
		FrameLayout mView = (FrameLayout) LayoutInflater.from(GlobalContext.get()).inflate(
				resourceId, null);
		TextView tvToast = (TextView) mView.findViewById(R.id.tvToast);
		TextViewUtil.setTextSize(tvToast, ViewConfiger.SIZE_HELP_ITEM_SIZE1);
		TextViewUtil.setTextColor(tvToast,ViewConfiger.COLOR_HELP_ITEM_COLOR1);
		tvToast.getLayoutParams().height = ScreenUtil.getDisplayLvItemH(false);

		helpList = (ListView) mView
				.findViewById(R.id.list_help_detail);
		helpList.setSelector(R.drawable.selector_none);
		if (needShowQRCode) {
			LinearLayout mQRCodeLayout = (LinearLayout) mView.findViewById(R.id.win_help_qrcode);
			mQRCodeLayout.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					AppLogic.runOnUiGround(new Runnable() {
						@Override
						public void run() {
						    JSONBuilder jsonBuilder = new JSONBuilder();
                            jsonBuilder.put("title",mQRCodeTitle);
						    jsonBuilder.put("url",mQRCodeUrl);
						    jsonBuilder.put("desc",mQRCodeDesc);
							WinHelpManager.getInstance().showQRCodeDialog(jsonBuilder);
						}
					});
				}
			});
			ImageView mIvQRCodeIcon = (ImageView) mView.findViewById(R.id.iv_help_qrcode_icon);
			LogUtil.d("ImageLoader:" + mQRCodeTitleIcon);
			ImageLoader.getInstance().displayImage("file://" + mQRCodeTitleIcon, mIvQRCodeIcon);
			TextView mTvQRCodeTitle = (TextView) mView.findViewById(R.id.tv_help_qrcode_title);
			mTvQRCodeTitle.setText(LanguageConvertor.toLocale(mQRCodeTitle));
			final ImageView mIvQRCode = (ImageView) mView.findViewById(R.id.iv_help_qrcode);
			TextView mTvQRCodeDesc = (TextView) mView.findViewById(R.id.tv_help_qrcode_desc);
			mQRCodeLayout.setVisibility(View.VISIBLE);
			mTvQRCodeDesc.setText(LanguageConvertor.toLocale(mQRCodeDesc));
			int width = (int) LayouUtil.getDimen("m104");
			try {
				mIvQRCode.setImageBitmap(QRUtil.createQRCodeBitmap(mQRCodeUrl, width));
			} catch (WriterException e) {
				e.printStackTrace();
			}
			if(mQRCodeNeedShowGuide){
				mGuideTask.update(mIvQRCode,mQRCodeUrl,mQRCodeGuideDesc,false);
				mIvQRCode.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
			}
		}

		WinHelpDetailAdapter detailAdapter = new WinHelpDetailAdapter(GlobalContext.get(),
				helpDetails,isFromFile,canOpenDetail);
		helpList.setAdapter(detailAdapter);
		ViewGroup.LayoutParams layoutParams = helpList.getLayoutParams();
		layoutParams.height = ScreenUtil.getDisplayLvItemH(false)*getPageSize();
		helpList.setLayoutParams(layoutParams);
		helpList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (canOpenDetail) {
					onItemSelected(position,false);
				}
			}
		});

		if (showToast) {
			tvToast.setText(LanguageConvertor.toLocale(tipS));
			tvToast.setVisibility(View.VISIBLE);
			AppLogic.runOnUiGround(new Runnable1<TextView>(tvToast) {
				@Override
				public void run() {
					mP1.setVisibility(View.GONE);
				}
			},3000);
		} else {
			tvToast.setVisibility(View.GONE);
		}
		return mView;
	}

	Runnable4<ImageView,String,String, Boolean> mGuideTask = new Runnable4<ImageView,String,String, Boolean>(null,null,null, false) {
		@Override
		public void run() {
			if(mP4){
				return;
			}
			mP1.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
			int[] loc = new int[2];
			mP1.getLocationOnScreen(loc);
			LogUtil.d("qrLayout ivQRCode loc 0:" + loc[0]);
			LogUtil.d("qrLayout ivQRCode loc 1:" + loc[1]);
			JSONBuilder jsonBuilder = new JSONBuilder();
			if (ScreenUtil.getScreenWidth() > ScreenUtil.getScreenHeight()) {
				jsonBuilder.put("screenType", WinHelpGuideQRCodeDialog.TYPE_LOW_POWER);
			} else {
				jsonBuilder.put("screenType", WinHelpGuideQRCodeDialog.TYPE_VERTICAL_SCREEN);
			}
			jsonBuilder.put("qrCodeUrl", mP2);
			jsonBuilder.put("qrCodeGuideDesc", mP3);
			jsonBuilder.put("locationX", loc[0]);
			jsonBuilder.put("locationY", loc[1]);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.qrcode.guide",
					jsonBuilder.toBytes(), null);
			mP4 = true;
		}
	};

	ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener =new ViewTreeObserver.OnGlobalLayoutListener() {
		@Override
		public void onGlobalLayout() {
			AppLogicBase.removeBackGroundCallback(mGuideTask);
			AppLogicBase.runOnBackGround(mGuideTask,500);
		}
	};


	/**
	 * 初始化帮助的数据
	 * 1.如果存放的路径为空，直接读取应用内部的数据
	 * 2.如果存放的路径不为空，并且使用ui2.0，则尝试从文件夹读取帮助数据
	 * 3.如果不是ui2.0，或者从帮助文件夹中读取帮助数据失败，则使用默认的一套
	 * update:兼容到1.0，只要不是使用1.0的第三方界面
	 * 20171228:只有第一次会从文件中读取，之后缓存到内存中
	 * 20180109:将1.0第三方的对话框合并到当前界面中
	 * 20180125:1.0第三方界面默认读取系统的自带的帮助信息，不使用下发的数据，TODO:后续提供数据给第三方界面
	 * 20180125:增加从系统配置文件中读取帮助信息
	 */
	private void initData(){
		initData(false);
	}
	private void initData(boolean forceNormal){
		if (forceNormal) {
			isFromFile = false;
			for (String mHelpDir : FilePathConstants.getUserHelpPath()) {
				if (initHelpFromFile(mHelpDir, false)) {
					isFromFile = true;
					break;
				}
			}
			if (!isFromFile) {
				initHelpFromFile(GlobalContext.get().getApplicationInfo().dataDir + "/data", true);
				isFromFile = false;
			}
		} else if (mCacheHelpDetails == null || mCacheHelpDetails.size() == 0) {
			isFromFile = false;
			if (WinManager.getInstance().hasThirdImpl()) {
				initHelpFromFile(GlobalContext.get().getApplicationInfo().dataDir + "/data", true);
				isFromFile = false;
			} else {
				String filePath = HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HELP_FILE_PATH, FilePathConstants.DEFAULT_HELP_FILE_DIR);
				if (initHelpFromFile(filePath, false)) {
					isFromFile = true;
				} else {
					for (String mHelpDir : FilePathConstants.getUserHelpPath()) {
						if (initHelpFromFile(mHelpDir, false)) {
							isFromFile = true;
							break;
						}
					}
				}

				if (!isFromFile) {
					initHelpFromFile(GlobalContext.get().getApplicationInfo().dataDir + "/data", true);
					isFromFile = false;
				}
			}
		}
		valueHelpList();

	}

	//默认的获取帮助信息的途径，已废弃
	public void initHelpData(Context context) {
		helpDetails = new ArrayList<HelpDetail>();
		String[] titles = context.getResources().getStringArray(
				R.array.win_help_title);
		String[] intros = context.getResources().getStringArray(
				R.array.win_help_intro);
		String[] desps = context.getResources().getStringArray(
				R.array.win_help_desps);
		TypedArray array = context.getResources().obtainTypedArray(
				R.array.win_delp_icon);
		String[] iconName = context.getResources().getStringArray(R.array.win_delp_icon_name);
		HelpDetail helpDetail;
		boolean isWeixinInstalled = WeixinManager.getInstance().isWeixinInstalled();
		String[] mHelpDetailsItemArray;
		for (int i = 0; i < titles.length; i++) {
			if (i == 8) {
				if (!isWeixinInstalled) {
					continue;
				}
			}
			helpDetail = new HelpDetail();
			helpDetail.title = titles[i];
			helpDetail.name = titles[i];
			helpDetail.intro = intros[i];
			helpDetail.iconResId = array.getResourceId(i,
					R.drawable.win_help_wechat);
			helpDetail.iconName = iconName[i];
			helpDetail.desps = new String[0];//desps[i].split("\n");
			helpDetails.add(helpDetail);
			
			helpDetail.detailItems = new ArrayList<HelpDetail.HelpDetailItem>();
			mHelpDetailsItemArray = helpDetail.intro.split("，");
			for (int j = 0; j < mHelpDetailsItemArray.length; j++) {
				HelpDetailItem helpDetailItem = new HelpDetailItem();
				helpDetailItem.name = mHelpDetailsItemArray[j];
				helpDetail.detailItems.add(helpDetailItem);
			}
			
		}
		array.recycle();
	}


	public static boolean checkHasTools(String tool) {
		boolean hasTool = true;
		if (!TextUtils.isEmpty(tool)) {
			if (TextUtils.equals(tool,"tool.call")) {
				hasTool = CallManager.getInstance().hasRemoteProcTool();
			} else if (TextUtils.equals(tool,"tool.nav")) {
				hasTool = TextUtils.isEmpty(NavManager.getInstance().getDisableResaon());
			} else if (TextUtils.equals(tool,"tool.loc")) {
				hasTool = TextUtils.isEmpty(NavManager.getInstance().getDisableResaon());
			} else if (TextUtils.equals(tool,"tool.near")) {
				hasTool = TextUtils.isEmpty(NavManager.getInstance().getDisableResaon());
			} else if (TextUtils.equals(tool,"tool.music")) {
				hasTool = TextUtils.isEmpty(MusicManager.getInstance().getDisableResaon());
			} else if (TextUtils.equals(tool,"tool.typing_effect")) {
				hasTool = ProjectCfg.isSupportTypingEffect();
			} else if(TextUtils.equals(tool, "tool.home_control")) {
				hasTool = ProjectCfg.isSupportHomeControl();
			} else if (TextUtils.equals(tool, "tool.remind")) {
				hasTool = ReminderManager.getInstance().enableReminderFunc();
			}
		}
		return hasTool;
	}

	/**
	 * 从文件中读取帮助信息
	 * @param path 文件父文件夹
	 * @return
	 */
	public boolean initHelpFromFile(String path,boolean isDefault) {
		boolean isSucess = false;
		try {
//			isFromFile = true;
			HelpDetail helpDetail;
			ArrayList<HelpDetail> tmpDetails = new ArrayList<HelpDetail>();
			JSONBuilder jBuilder = new JSONBuilder(new File(path + File.separator + "help.txt"));
			JSONArray helpDetailsArray;
			helpDetailsArray = jBuilder.getVal("help",JSONArray.class,null);
			mImgPath = path + File.separator + jBuilder.getVal("imgDir", String.class);
			JSONArray helpDetailsItemArray;
			JSONArray helpDetailsImgArray;
			JSONBuilder helpDetailJson;
			JSONBuilder helpDetailItemJson;
			JSONBuilder helpDetailImgJson;

			//显示的类型,0表示全部亮显，1表示没网时在线命令灰显，2表示没网时在线命令不显示
			mShowType = jBuilder.getVal("type", Integer.class, SHOW_TYPE_NORMAL);
			canOpenDetail = jBuilder.getVal("showDetail", Integer.class, 1) != 0
					&& !hasThirdImpl()
					&&  !(WinManager.getInstance().isRecordWin2() && WinLayoutManager.getInstance().getHelpDetailListView() == null);
			if (jBuilder.getVal("showTag", Integer.class, 0) == 1) {//是否需要显示new标签,0不显示,1显示
				HelpPreferenceUtil.getInstance().setBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_TAG, true);
			} else {
				HelpPreferenceUtil.getInstance().setBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_TAG, false);
			}

			for (int i = 0; i < helpDetailsArray.length(); i++) {
				helpDetailJson = new JSONBuilder(helpDetailsArray.getJSONObject(i));

				helpDetail = new HelpDetail();
				helpDetail.id = helpDetailJson.getVal("id", String.class);
				helpDetail.name = helpDetailJson.getVal("name", String.class);
				helpDetail.lastName = helpDetailJson.getVal("lastName", String.class);
				helpDetail.title = helpDetail.name;
				if (isDefault) {
					helpDetail.iconName = helpDetailJson.getVal("icon", String.class, "");
					int index = helpDetail.iconName.lastIndexOf(".");
					if (index != -1) {
						helpDetail.iconName = helpDetail.iconName.substring(0, index);
					}
				} else {
					helpDetail.iconName = mImgPath + File.separator + helpDetailJson.getVal("icon", String.class);
				}
				helpDetail.time = helpDetailJson.getVal("time", String.class);
				//helpDetail.isNew = isNew(HelpPreferenceUtil.getInstance().getString(helpDetail.name, DEF_TIME), helpDetail.time);
				helpDetail.openType = helpDetailJson.getVal("openType", Integer.class, CMD_OPEN_TYPE_NORMAL);
				helpDetail.strPackage = helpDetailJson.getVal("package", String.class, null);
				helpDetail.tool = helpDetailJson.getVal("tool", String.class, null);

				JSONArray intros = helpDetailJson.getVal("intro", JSONArray.class, null);
				if (intros != null && intros.length() != 0) {
					helpDetail.intros = new String[intros.length()];
					for (int k = 0; k < intros.length(); k++) {
						helpDetail.intros[k] = intros.getString(k);
					}
				}

				helpDetail.detailItems = new ArrayList<HelpDetail.HelpDetailItem>();
				helpDetailsItemArray = helpDetailJson.getVal("desc",JSONArray.class,null);

				for (int j = 0; j < helpDetailsItemArray.length(); j++) {
					helpDetailItemJson = new JSONBuilder(helpDetailsItemArray.getJSONObject(j));
					HelpDetailItem helpDetailItem = new HelpDetailItem();
					helpDetailItem.id = helpDetailItemJson.getVal("id", String.class);
					helpDetailItem.name = "“" + helpDetailItemJson.getVal("name", String.class) + "”";
					helpDetailItem.time = helpDetailItemJson.getVal("time", String.class);
//						helpDetailItem.isNew = isNew(HelpPreferenceUtil.getInstance().getString(helpDetailItem.name, DEF_TIME), helpDetailItem.time);
					helpDetailItem.netType = helpDetailItemJson.getVal("netType", Integer.class, CMD_NET_TYPE_MIX);

					helpDetail.detailItems.add(helpDetailItem);
				}

				helpDetail.detailImgs = new ArrayList<HelpDetail.HelpDetailImg>();
				helpDetailsImgArray = helpDetailJson.getVal("imgs",JSONArray.class,null);
				if (helpDetailsImgArray != null) {
					for (int j = 0; j < helpDetailsImgArray.length(); j++) {
						helpDetailImgJson = new JSONBuilder(helpDetailsImgArray.getJSONObject(j));
						HelpDetail.HelpDetailImg helpDetailImg = new HelpDetail.HelpDetailImg();
						helpDetailImg.id = helpDetailImgJson.getVal("id", String.class, null);
						helpDetailImg.text = helpDetailImgJson.getVal("text", String.class, null);
						helpDetailImg.time = helpDetailImgJson.getVal("time", String.class, null);
						helpDetailImg.img = helpDetailImgJson.getVal("img", String.class, "");
						if (TextUtils.equals("qrcord.wx_bind", helpDetailImg.img)) {
							if (!TextUtils.isEmpty(HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_QCORD_WX_BIND_URL, ""))) {
								helpDetailImg.img = "qrcode:" + HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_QCORD_WX_BIND_URL, "");
							}
						} else if (isDefault) {
							int index = helpDetailImg.img.lastIndexOf(".");
							if (index != -1) {
								helpDetailImg.img = helpDetailImg.img.substring(0, index);
							}
						} else {
							helpDetailImg.img = mImgPath + File.separator + helpDetailImg.img;
						}
						helpDetail.detailImgs.add(helpDetailImg);
					}
				}

				//插入三级页面二维码内容
				if (mQRCodeDetailNameList != null && mQRCodeDetailNameList.length != 0) {
					for (int k = 0; k < mQRCodeDetailNameList.length; k++) {
						String detailName = mQRCodeDetailNameList[k];
						if (detailName.equals(helpDetail.name)) {
							if(helpDetail.openType == CMD_OPEN_TYPE_NORMAL){
								helpDetail.openType = CMD_OPEN_TYPE_MIX;
							}
							HelpDetail.HelpDetailImg helpDetailImg = new HelpDetail.HelpDetailImg();
							helpDetailImg.id = WinHelpQRCodeConstants.ID;
							helpDetailImg.text = mQRCodeDetailDesc;
							helpDetailImg.time = WinHelpQRCodeConstants.TIME;
							helpDetailImg.img = "qrcode:" + mQRCodeDetailUrl;
							helpDetail.detailImgs.add(helpDetailImg);
						}
					}
				}

				tmpDetails.add(helpDetail);
			}
			if (mCacheHelpDetails == null) {
				mCacheHelpDetails = new ArrayList<HelpDetail>();
			}
			mCacheHelpDetails.clear();
			mCacheHelpDetails.addAll(tmpDetails);
			isSucess = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSucess;
	}

	/**当前条目是否是语音使用手册
	 * @param helpDetail
	 * @return
	 */
	private boolean isGuideHelp(HelpDetail helpDetail){
		return TextUtils.equals(helpDetail.lastName,"语音使用手册") || TextUtils.equals(helpDetail.name,"语音使用手册");
	}

	/**
	 *从加载的缓存中获取帮助详情信息
	 */
	private void valueHelpList(){
		String mHelpDetailName = HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HELP_DETAIL_NAME, "");
		boolean hasNet = NetworkManager.getInstance().hasNet();
		helpDetails = new ArrayList<HelpDetail>();
		HelpDetail helpDetail;
		for (int i = 0; i < mCacheHelpDetails.size(); i++) {
			//隐藏模式的话，没网情况下去掉列表的数据源，如果全部去掉了就不显示
			if (mShowType == SHOW_TYPE_NET_HIDE && !hasNet) {
				helpDetail = mCacheHelpDetails.get(i).clone();
				Iterator<HelpDetailItem> items = helpDetail.detailItems.iterator();
				while (items.hasNext()) {
					HelpDetailItem helpDetailItem = items.next();
					if (helpDetailItem.netType == CMD_NET_TYPE_NET) {
						items.remove();
					}
				}
				if (helpDetail.detailItems.size() ==0 && helpDetail.openType == CMD_OPEN_TYPE_NORMAL) {
					continue;
				}
			}else {
				helpDetail = mCacheHelpDetails.get(i);
			}
			helpDetail.isNew = isNew(HelpPreferenceUtil.getInstance().getString(helpDetail.name, DEF_TIME), helpDetail.time);
			//当是默认显示类型的时候，就无须判断网络了；隐藏模式的话，数据源已经去掉了；在详情界面就只有灰显模式用到了网络判断
			helpDetail.hasNet = ( mShowType == SHOW_TYPE_NORMAL ) || hasNet;

			//检查app是否安装，没有的话不显示帮助
			if (!TextUtils.isEmpty(helpDetail.strPackage)) {
				if (helpDetail.strPackage.equals(HelpGuideManager.GUIDE_PACKAGE_NAME)) {
					if (!HelpGuideManager.getInstance().hasGuideAnim()) {
						continue;
					}
				} else if (helpDetail.strPackage.equals(ServiceManager.WEBCHAT)) {
				    //判断微信不可用的时候不展示微信帮助
				    if (!WeixinManager.getInstance().enableWeChat()) {
				        continue;
                    }
                }
				if (!PackageManager.getInstance().checkAppExist(helpDetail.strPackage)) {
					continue;
				}
			}

			//检查工具是否存在
			if (!checkHasTools(helpDetail.tool)) {
				continue;
			}

			helpDetail.intro = "";
			typeNetHideIndex = 0;
			typeNetGrayIndex = 0;

			if (isGuideHelp(helpDetail)) {
				mGuideHelpDetail = helpDetail.clone();
			}

			for (int j = 0; j < helpDetail.detailItems.size(); j++) {
				HelpDetailItem helpDetailItem = helpDetail.detailItems.get(j);
				helpDetailItem.isNew = isNew(HelpPreferenceUtil.getInstance().getString(helpDetailItem.name, DEF_TIME), helpDetailItem.time);

				//指令更新，则认为整个条目更新了，
				if (helpDetailItem.isNew) {
					helpDetail.isNew = true;
				}

				if (helpDetail.intros != null && helpDetail.intros.length != 0) {
					for (int k = 0; k < helpDetail.intros.length; k++) {
						if (TextUtils.equals("“" + helpDetail.intros[k] + "”", helpDetailItem.name)) {
							helpDetail.intro = valueHelpIntro(hasNet, helpDetail.intro, helpDetailItem.name, mShowType, helpDetailItem.netType);
						}
					}
				}
			}

			if (TextUtils.isEmpty(helpDetail.intro) || helpDetail.isNew) {
				helpDetail.intro = "";
				typeNetHideIndex = 0;
				typeNetGrayIndex = 0;
				for (int j = 0; j < helpDetail.detailItems.size(); j++) {
					helpDetail.intro = valueHelpIntro(hasNet, helpDetail.intro, helpDetail.detailItems.get(j).name, mShowType, helpDetail.detailItems.get(j).netType);
				}
			}

			//如果还为空，说明没有指令需要展示了，就不显示这个条目
			if (TextUtils.isEmpty(helpDetail.intro)) {
				continue;
			}

			
			if (helpDetail.detailImgs != null) {
				for (int j = 0; j < helpDetail.detailImgs.size(); j++) {
					HelpDetail.HelpDetailImg helpDetailImg = helpDetail.detailImgs.get(j);
					if (TextUtils.equals("qrcord.wx_bind", helpDetailImg.img)) {
						if (!TextUtils.isEmpty(HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_QCORD_WX_BIND_URL, ""))) {
							helpDetailImg.img = "qrcode:" + HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_QCORD_WX_BIND_URL, "");
						}
					}
				}
			}

			//现在按名字来做唯一标识
			if (!TextUtils.isEmpty(mHelpDetailName)) {
				if (TextUtils.equals(mHelpDetailName, helpDetail.name)) {
					// 这个需要只显示更新的条目，考虑没有内容的情况，直接展示帮助界面
					mSelectHelpDetail = helpDetail.clone();
					if (mSelectHelpDetail != null) {
						Iterator<HelpDetailItem> items = mSelectHelpDetail.detailItems.iterator();
						while (items.hasNext()) {
							HelpDetailItem helpDetailItem = items.next();
							if (!helpDetailItem.isNew) {
								items.remove();
							}
						}
					}
				}
			}

			helpDetails.add(helpDetail);
		}

		isShowTips = false;

		if (mShowType == SHOW_TYPE_NET_GRAY) {
			if (!hasNet) {
				tipS = NativeData.getResString("RS_HELP_TOAST_GRAY_NET_CMD");
				isShowTips = true;
			}
		} else if (mShowType == SHOW_TYPE_NET_HIDE) {
			if (ProjectCfg.getNetModule() == 0 || ProjectCfg.hasNetModule()) { //有sim模块，或者无法判断的情况
				if (!hasNet) {
					tipS = NativeData.getResString("RS_HELP_TOAST_HIDE_NET_CMD");
					isShowTips = true;
				}
			} else {
				if (hasNet) {
					tipS = NativeData.getResString("RS_HELP_TOAST_SHOW_NET_CMD");
					isShowTips = true;
				}
			}
		}
	}


	private int typeNetGrayIndex = 0;
	private int typeNetHideIndex = 0;
	private String valueHelpIntro(boolean hasNet,String intro,String name,int type,int netType){
		//显示的类型,0表示全部亮显，1表示没网时在线命令灰显，2表示没网时在线命令不显示
		if (hasNet) {
			if (typeNetGrayIndex < SHOW_DESC_COUNT) {
				intro += name;
				intro += " ";
				typeNetGrayIndex ++;
			}
		} else {
			if (type == SHOW_TYPE_NET_GRAY) {
				if (typeNetGrayIndex < SHOW_DESC_COUNT) {
					if (netType == CMD_NET_TYPE_NET) {
						intro = intro + "<font color='#808080'>" + name + "</font>";
					} else {
						intro = intro + name;
					}
					intro += " ";
					typeNetGrayIndex++;
				}
			} else if (type == SHOW_TYPE_NET_HIDE) {
				if (typeNetHideIndex < SHOW_DESC_COUNT) {
					if (netType == CMD_NET_TYPE_NET) {

					} else {
						intro += name;
						intro += " ";
						typeNetHideIndex++;
					}
				}
			} else {
				if (typeNetGrayIndex < SHOW_DESC_COUNT) {
					intro += name;
					intro += " ";
					typeNetGrayIndex ++;
				}
			}
		}
		return intro;
	}


	/**
	 * 打开语音使用手册
	 * @return
	 */
	public boolean openGuideHelpDetail(){
		boolean isOpen = false;
		if (canOpenDetail) {
			if (mGuideHelpDetail == null) {
				if (mCacheHelpDetails == null) {
					initData();
				}
			}
			if (mGuideHelpDetail != null && canOpenDetail) {
				isOpen = onItemSelected(mGuideHelpDetail, -1);
			}
		}
		return isOpen;
	}

	private String[] defEnableHelpItems = {"音乐","导航","微信","电话"};

	public String checkHelpEnable(String helpItem){
		String ret = "false";
		for (String help : defEnableHelpItems) {
			if (TextUtils.equals(help,helpItem)) {
				if (mCacheHelpDetails == null || mCacheHelpDetails.size() == 0) {
					initData();
				}
				for (HelpDetail helpDetail: mCacheHelpDetails ) {
					if (TextUtils.equals(helpItem,helpDetail.name)){
						ret = "true";
						break;
					}
				}
				break;
			}
		}

		return ret;
	}

	private boolean isNew(String lastTime, String currentTime){
		boolean isNew = false;
		if (TextUtils.isEmpty(lastTime)) {
			isNew = true;
			if (TextUtils.isEmpty(currentTime)) {
				isNew = false;
			}
		}else {
			if (TextUtils.isEmpty(currentTime)) {
				isNew = false;
			}else {
				isNew = compareDate(lastTime,currentTime);
			}
		}
		return isNew;
	}
	
	/**
	 * 比较时间是否是新的
	 * @param DATE1
	 * @param DATE2
	 * @return
	 */
	public static boolean compareDate(String DATE1, String DATE2) {
        SimpleDateFormat df = new SimpleDateFormat(DEF_TIME_FORMAT);
        try {
            Date dt1 = df.parse(DATE1);
            Date dt2 = df.parse(DATE2);
            if (dt1.getTime() > dt2.getTime()) {
                return false;
            } else if (dt1.getTime() < dt2.getTime()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

	public static class PageHelper {
		public int curPage;
		public int maxPage;
		public int pageSize;
		public int totalCount;

		public boolean nextPage() {
			if (curPage < (maxPage - 1)) {
				curPage++;
				return true;
			}
			return false;
		}

		public boolean selectPage(int page) {
			if (page <= maxPage && page > 0) {
				curPage = page - 1;
				return true;
			}
			return false;
		}

		public boolean prePage() {
			if (curPage > 0) {
				curPage--;
				return true;
			}
			return false;
		}

		public int getCurPage() {
			return curPage;
		}

		public int getMaxPage() {
			return maxPage;
		}

		public int getPageSize() {
			return pageSize;
		}

		public void reset(int totalCount, int pageSize) {
			this.curPage = 0;
			this.maxPage = 0;
			this.pageSize = pageSize;
			this.totalCount = totalCount;

			if (totalCount > pageSize) {
				maxPage = totalCount / pageSize;
				if (totalCount % pageSize != 0) {
					maxPage++;
				}
			}
		}

		public int getTotalCount() {
			return totalCount;
		}
	}

	@Override
	public void showChoices(Void data) {
	}

	@Override
	public boolean isSelecting() {
		return mIsSelecting;
	}

	@Override
	public void clearIsSelecting() {
		clearIsSelecting_Inner();
	}

	private boolean hasThirdImpl(){
		return WinManager.getInstance().hasThirdImpl();
	}

	BroadcastReceiver mWinRecordReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if("com.txznet.txz.record.show".equals(intent.getAction())){
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						dismiss("");
					}
				}, 0);
			}
		}
	};

	private class WinHelpDetailDialog extends WinDialog {

		DisplayLvRef displayLvRef = null;
		DisplayConMsg displayMsg = null;
		private boolean isAdd = false;
		public WinHelpDetailDialog() {
			super(new WinDialog.DialogBuildData());
		}

		@TargetApi(Build.VERSION_CODES.KITKAT)
		@Override
		protected View createView() {
			displayLvRef = new DisplayLvRef(GlobalContext.get());
			if (Build.VERSION.SDK_INT >= 16) {
				displayLvRef.setBackground(new ColorDrawable(GlobalContext.get().getResources().getColor(com.txznet.record.lib.R.color.win_bg)));
			} else {
				displayLvRef.setBackgroundDrawable(new ColorDrawable(GlobalContext.get().getResources().getColor(com.txznet.record.lib.R.color.win_bg)));
			}
			return displayLvRef;
		}

		@Override
		protected void onInitDialog() {
			super.onInitDialog();
			if (!isAdd) {
				if (mView != null) {
					displayLvRef.refreshTitleView(displayMsg);
					displayLvRef.replaceView(displayMsg.mConView);
				}

			}
		}

		public void replaceView(DisplayConMsg displayMsg) {
			isAdd = false;
			this.displayMsg = displayMsg;
			if (displayLvRef != null) {
				displayLvRef.refreshTitleView(displayMsg);
				displayLvRef.replaceView(displayMsg.mConView);
				isAdd = true;
			}
		}

		@Override
		public String getReportDialogId() {
			return "win_help_third_1";
		}

		@Override
		protected void onDismiss() {
			super.onDismiss();
			clearIsSelecting();
		}
	}

	private void initQRCodeData(){
		if ("true".equals(HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HELP_QRCODE_IS_SHOW, "true")) &&
				WinHelpManager.getInstance().getEnableShowHelpQRCode()) {
			isShowQRCode = true;
			String data = HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HELP_QRCODE_DATA,"");
			JSONBuilder jsonBuilder = new JSONBuilder(data);

			long lastReqTime = HelpPreferenceUtil.getInstance().getLong(HelpPreferenceUtil.KEY_HELP_QRCODE_LAST_REQ_TIME, 0L);
            LogUtil.d("qrcode lastReqTime:" + lastReqTime + ",current time:" + System.currentTimeMillis());
			if (System.currentTimeMillis() - lastReqTime > 30 * ONE_DAY_TIME) {//如果超30天，失效
				mQRCodeTitleIcon = "";
				mQRCodeTitle = WinHelpQRCodeConstants.QRCODE_TITLE_TEXT;
				mQRCodeUrl = WinHelpQRCodeConstants.QRCODE_URL;
				mQRCodeDesc = WinHelpQRCodeConstants.QRCODE_DESC;
				mQRCodeGuideDesc = WinHelpQRCodeConstants.QRCODE_GUIDE_DESC;
				mQRCodeGuideShowInterval = WinHelpQRCodeConstants.QRCODE_GUIDE_SHOW_INTERVAL;
				mQRCodeDetailUrl = WinHelpQRCodeConstants.QRCODE_DETAIL_URL;
				mQRCodeDetailDesc = WinHelpQRCodeConstants.QRCODE_DETAIL_DESC;
				mQRCodeDetailNameList = WinHelpQRCodeConstants.QRCODE_DETAIL_NAME_LIST;
			}else{
				mQRCodeTitleIcon = jsonBuilder.getVal("qrCodeTitleIcon", String.class);
				if (!TextUtils.isEmpty(mQRCodeTitleIcon)) {
					File file = new File(DownloadManager.DOWNLOAD_FILE_ROOT, MD5Util.generateMD5(mQRCodeTitleIcon));
					if (file.exists()) {
						mQRCodeTitleIcon = file.getPath();
					} else {
						mQRCodeTitleIcon = "";
					}
				}
				mQRCodeTitle = jsonBuilder.getVal("qrCodeTitleText", String.class, WinHelpQRCodeConstants.QRCODE_TITLE_TEXT);

				mQRCodeUrl = jsonBuilder.getVal("qrCodeUrl", String.class);
				if (TextUtils.isEmpty(mQRCodeUrl)) {
					mQRCodeUrl = WinHelpQRCodeConstants.QRCODE_URL;
				}

				mQRCodeDesc = jsonBuilder.getVal("qrCodeDesc", String.class);
				if (TextUtils.isEmpty(mQRCodeDesc)) {
					mQRCodeDesc = WinHelpQRCodeConstants.QRCODE_DESC;
				}

				mQRCodeGuideDesc = jsonBuilder.getVal("qrCodeGuideDesc", String.class);
				if (TextUtils.isEmpty(mQRCodeGuideDesc)) {
					mQRCodeGuideDesc = WinHelpQRCodeConstants.QRCODE_GUIDE_DESC;
				}

				mQRCodeGuideShowInterval = jsonBuilder.getVal("qrCodeGuideShowInterval", String.class);
				if (TextUtils.isEmpty(mQRCodeGuideShowInterval)) {
					mQRCodeGuideShowInterval = WinHelpQRCodeConstants.QRCODE_GUIDE_SHOW_INTERVAL;
				}

				//初始化三级页面帮助详情
				mQRCodeDetailNameList = jsonBuilder.getVal("qrCodeDetailNameList", String[].class, WinHelpQRCodeConstants.QRCODE_DETAIL_NAME_LIST);
				if (mQRCodeDetailNameList != null && mQRCodeDetailNameList.length > 0) {
					mQRCodeDetailUrl = jsonBuilder.getVal("qrCodeDetailUrl", String.class, WinHelpQRCodeConstants.QRCODE_DETAIL_URL);
					if (TextUtils.isEmpty(mQRCodeDetailUrl)) {
						mQRCodeDetailUrl = WinHelpQRCodeConstants.QRCODE_DETAIL_URL;
					}

					mQRCodeDetailDesc = jsonBuilder.getVal("qrCodeDetailDesc", String.class);
					if (TextUtils.isEmpty(mQRCodeDetailDesc)) {
						mQRCodeDetailDesc = WinHelpQRCodeConstants.QRCODE_DETAIL_DESC;
					}
				}
			}

            //是否要展示引导
            String isShowGuide = jsonBuilder.getVal("isShowGuide", String.class, "");
            if (TextUtils.isEmpty(isShowGuide) || "true".equals(isShowGuide)) {
                mQRCodeNeedShowGuide = false;
                if (HelpPreferenceUtil.getInstance().getLong(HelpPreferenceUtil.KEY_HELP_GUIDE_LAST_SHOW_TIME, 0L) == 0) {
                    mQRCodeNeedShowGuide = true;
                }

                if ("true".equals(HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HELP_NEED_IMMEDIATELY_SHOW, ""))) {
                    mQRCodeNeedShowGuide = true;
                }

                long lastShowTime = HelpPreferenceUtil.getInstance().getLong(HelpPreferenceUtil.KEY_HELP_GUIDE_LAST_SHOW_TIME, 0L);
                LogUtil.d("qrcode lastShowTime:" + lastShowTime + ",current time:" + System.currentTimeMillis());
                if (mQRCodeGuideShowInterval.equals("month")) {
                    //间隔一个月
                    if (DateUtils.getGapMonthWithHalf(new Date(lastShowTime), new Date()) >= 1) {
                        mQRCodeNeedShowGuide = true;
                    }
                } else {
                    //间隔一周
                    if (DateUtils.compareDateMoreThanOneWeek(new Date(lastShowTime), new Date())) {
                        mQRCodeNeedShowGuide = true;
                    }
                }
            }
		} else {
			isShowQRCode = false;
		}
	}

	public void notNeedShowGuide(){
		mQRCodeNeedShowGuide = false;
	}
	
	/**
	 * @return ArrayList<HelpDetail>  所返回的帮助类目缓存为一个临时克隆对象，帮助缓存发送改变是不会影响改克隆对象，该克隆对象进行写操作时也不会影响到帮助缓存。
	 */
	public ArrayList<HelpDetail> getCacheHelpDetails(){
		if(mCacheHelpDetails == null){
			initData();
		}
		return mCacheHelpDetails != null ? new ArrayList<HelpDetail>(mCacheHelpDetails) : new ArrayList<HelpDetail>();
	}

}
