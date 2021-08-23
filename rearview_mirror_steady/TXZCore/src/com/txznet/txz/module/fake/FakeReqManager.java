package com.txznet.txz.module.fake;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.equipment_manager.EquipmentManager.Req_Fake_Request_Result;
import com.txz.equipment_manager.EquipmentManager.Req_Notify_User_Config;
import com.txz.equipment_manager.EquipmentManager.Req_Traffic_Between_Company_Home;
import com.txz.equipment_manager.EquipmentManager.Resp_Fake_Request_Result;
import com.txz.equipment_manager.EquipmentManager.Resp_Notify_User_Config;
import com.txz.equipment_manager.EquipmentManager.Resp_Traffic_Between_Company_Home;
import com.txz.equipment_manager.EquipmentManager.SmartTravel;
import com.txz.push_manager.PushManager;
import com.txz.push_manager.PushManager.FakeRequest;
import com.txz.report_manager.ReportManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.ReportUtil.Report;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.news.NewsManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tmc.TMCManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * log：SUBEVENT_RESP_NOTIFY_USER_CONFIG|setSmartTrafficSetting|parseSmartTraffic|parseFakeReq|uploadCurrentGPS|onResponseFakeConnect|triggleUploadGps
 */

public class FakeReqManager extends IModule {
	private static FakeReqManager sManager;

	public static FakeReqManager getInstance() {
		if (sManager == null) {
			synchronized (FakeReqManager.class) {
				if (sManager == null) {
					sManager = new FakeReqManager();
				}
			}
		}
		return sManager;
	}

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_FAKE_REQUEST);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_FAKE_REQUEST);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_NOTIFY_USER_CONFIG);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_TRAFFIC_BETWEEN_COMPANY_HOME);
		return super.initialize_BeforeStartJni();
	}
	
	@Override
	public int initialize_AfterInitSuccess() {
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				int action = intent.getIntExtra("action", -1);
				switch (action) {
				case 5:
					FakeReqManager.getInstance().testGPS(22.5379373394, 113.9525878429);
					break;
				}
			}
		}, new IntentFilter("com.txznet.traffic"));
		return super.initialize_AfterInitSuccess();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		if (UiEvent.EVENT_ACTION_EQUIPMENT == eventId) {
			switch (subEventId) {
			case UiEquipment.SUBEVENT_RESP_FAKE_REQUEST:
				if (data == null) {
					break;
				}
				try {
					Resp_Fake_Request_Result resp = Resp_Fake_Request_Result.parseFrom(data);
					if (resp == null) {
						break;
					}
					LogUtil.logd("SUBEVENT_RESP_FAKE_REQUEST resp:" + resp.retCode);
					parseFakeReqResult(resp);
				} catch (InvalidProtocolBufferNanoException e1) {
					e1.printStackTrace();
				}
				break;
			case UiEquipment.SUBEVENT_RESP_TRAFFIC_BETWEEN_COMPANY_HOME:
				if (data == null) {
					break;
				}
				try {
					Resp_Traffic_Between_Company_Home resp = Resp_Traffic_Between_Company_Home.parseFrom(data);
					if (resp == null) {
						break;
					}
					LogUtil.logd("SUBEVENT_RESP_TRAFFIC_BETWEEN_COMPANY_HOME resp:" + resp.retCode);
				} catch (InvalidProtocolBufferNanoException e1) {
					e1.printStackTrace();
				}
				break;
			case UiEquipment.SUBEVENT_NOTIFY_FAKE_REQUEST:
				try {
					if (data == null) {
						break;
					}
					PushManager.PushCmd_FakeRequest request = PushManager.PushCmd_FakeRequest.parseFrom(data);
					if (request == null) {
						break;
					}
					PushManager.FakeRequest[] reqs = request.rptMessage;
					if (reqs == null || reqs.length <= 0) {
						LogUtil.logd("FakeRequest is null！");
						break;
					}
					parseFakeRequest(reqs);
				} catch (InvalidProtocolBufferNanoException e) {
					e.printStackTrace();
				}
				break;
			case UiEquipment.SUBEVENT_RESP_NOTIFY_USER_CONFIG:
				try {
					if (data == null) {
						break;
					}
					Resp_Notify_User_Config config = Resp_Notify_User_Config.parseFrom(data);
					LogUtil.logd("SUBEVENT_RESP_NOTIFY_USER_CONFIG:" + (config != null ? config.retCode : config));
					if (config != null && config.retCode != null) {
						onSettingResponse(true);
					} else {
						onSettingResponse(false);
					}
				} catch (InvalidProtocolBufferNanoException e) {
					e.printStackTrace();
				}
				break;
			}
		}
		return super.onEvent(eventId, subEventId, data);
	}

	private void parseFakeRequest(PushManager.FakeRequest[] request) {
		boolean enableSmart = PreferenceUtil.getInstance().isEnableNavNewspaperable();
		LogUtil.logd("parseFakeRequest len:" + request.length);
		for (FakeRequest req : request) {
			if (req.uint32ServiceType == PushManager.SMART_TRAVEL) {
				if (!enableSmart) {
					LogUtil.loge("enableSmart false！");
					continue;
				}
			}
			FakeReqTask task = new FakeReqTask(getNextSession(), req) {
				@Override
				public void onStartConnect() {
					mBusy = true;
				}

				@Override
				public void onConnectResult(int retCode, String resultStr) {
					if (resultStr == null) {
						resultStr = "";
					}
					onResponseFakeConnect(this, retCode, resultStr.getBytes());
					mBusy = false;
					procQueue();
				}
			};
			postTask(task);
		}
	}

	private Handler mHandler = null;
	private HandlerThread mThread = null;

	private void ensureHandler() {
		if (mThread == null) {
			mThread = new HandlerThread("FakeReqThread");
			mThread.start();
			mHandler = new Handler(mThread.getLooper());
		}
	}

	private List<FakeReqTask> mReqList = new ArrayList<FakeReqTask>();

	private void postTask(FakeReqTask task) {
		ensureHandler();
		if (mBusy) {
			synchronized (mReqList) {
				mReqList.add(task);
			}
		} else {
			mHandler.post(task);
		}
	}

	private boolean mBusy = false;

	private void procQueue() {
		mBusy = false;
		synchronized (mReqList) {
			if (!mReqList.isEmpty()) {
				FakeReqTask task = mReqList.remove(0);
				mHandler.post(task);
			}
		}
	}

	private int mSession;

	private int getNextSession() {
		mSession++;
		return mSession;
	}

	private void onResponseFakeConnect(FakeReqTask task, int retCode, byte[] b) {
		Req_Fake_Request_Result result = new Req_Fake_Request_Result();
		result.retCode = retCode;
		result.bytesMessage = task.mReqTask.bytesMessage;
		result.bytesResult = b;
		result.uint32ServiceType = task.mReqTask.uint32ServiceType;
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_FAKE_REQUEST, result);
		LogUtil.logd("onResponseFakeConnect");
	}

    private int mSpeechId;
    private boolean mHasWakeup;
    private NavigateInfo mDestinInfo;
    private static int AUTO_DISMISS_TIME_OUT = 5000;
	private Runnable mTimeoutRunnable = new Runnable() {

		@Override
		public void run() {
			try {
				if (mRepoObj != null) {
					mRepoObj.put(KEY_DISMISS_TYPE, REPORT_TYPE_TIMEOUT_CANCEL);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			endTrafficDialog();
		}
	};
	
	public void dismissDialog() {
		endTrafficDialog();
	}

	private void endTrafficDialog() {
		if (mRepoObj != null) {
			try {
				mRepoObj.put(KEY_TYPE, "SmartTraffic");
				mRepoObj.put(KEY_DISMISS_TIME, System.currentTimeMillis());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			ReportUtil.doReport(new Report() {

				@Override
				public int getType() {
					return ReportManager.UAT_COMMON;
				}

				@Override
				public String getData() {
					return mRepoObj.toString();
				}
			});
			mRepoObj = null;
		}

		mDestinInfo = null;
		if (mHasWakeup) {
			mHasWakeup = false;
			WakeupManager.getInstance().recoverWakeupFromAsr("START_NAVI");
		}
		TtsManager.getInstance().cancelSpeak(mSpeechId);
		AppLogic.removeUiGroundCallback(mTimeoutRunnable);
		TMCManager.getInstance().dismiss();
		if (mTmcTool != null) {
			mTmcTool.onDismissDialog();
		}
	}

	private void parseFakeReqResult(Resp_Fake_Request_Result result) {
		Integer retCode = result.retCode;
		Integer serviceType = result.uint32ServiceType;
		byte[] data = result.bytesMessage;
		if (retCode != null && retCode == 0) {// 成功
			if (serviceType == PushManager.SMART_TRAVEL) {
				if (data != null) {
					try {
						parseSmartTraffic(SmartTravel.parseFrom(data));
					} catch (InvalidProtocolBufferNanoException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private Runnable1<SmartTravel> smartTrafficTask = null;

	private TXZNavManager.TmcTool mTmcTool;

	/**
	 * 恢复播报
	 */
	public void startTrafficOperate() {
		if (smartTrafficTask != null) {
			smartTrafficTask.run();
		}
	}

	/**
	 * 是否要忽略路况早晚报
	 *
	 * @return
	 */
	private boolean isIgnoreNotify() {
		NavigateInfo homeInfo = NavManager.getInstance().getHomeNavigateInfo();
		NavigateInfo companyInfo = NavManager.getInstance().getCompanyNavigateInfo();
		if (homeInfo == null || companyInfo == null ||
				!NavManager.getInstance().isLegalNavigateInfo(homeInfo)
				|| !NavManager.getInstance().isLegalNavigateInfo(companyInfo)
				|| RecorderWin.isOpened()) {
			return true;
		}
		return false;
	}

	private void parseSmartTraffic(SmartTravel travel) {
		if (isIgnoreNotify()) {
			LogUtil.logd("parseSmartTraffic ignore!");
			return;
		}

		NewsManager.getInstance().stop();//路况早晚报新闻需要停止,长安欧尚需求

		if (mTmcTool != null) {
			if (mTmcTool.onSmartTraffic(travel)) {
				return;
			}

			if (mTmcTool.isIgnore()) {
				return;
			}

			if (mTmcTool.needWait()) {
				smartTrafficTask = new Runnable1<SmartTravel>(travel) {
					@Override
					public void run() {
						smartTrafficTask = null;
						if (mP1 == null) {
							return;
						}
						doSmartTraffic(mP1);
					}
				};
				return;
			}
		}

		if (SenceManager.getInstance().noneedProcSence("tmc", SmartTravel.toByteArray(travel))) {
			return;
		}

		doSmartTraffic(travel);
	}

	private void doSmartTraffic(SmartTravel travel) {
		boolean enable = PreferenceUtil.getInstance().isEnableNavNewspaperable();
		LogUtil.logd("parseSmartTraffic enable:" + enable);
		if (!enable) {
			return;
		}

		String title = travel.strTitle;
		String tts = travel.strTts;
		mDestinInfo = travel.navInfo;
		byte[] bytesBody = travel.bytesBody;
		if (bytesBody == null) {
			bytesBody = "".getBytes();
		}
		JSONObject obj = null;
		try {
			obj = new JSONObject(new String(bytesBody));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (obj != null && obj.has("close_delay")) {
			AUTO_DISMISS_TIME_OUT = obj.optInt("close_delay");
			LogUtil.logd("AUTO_DISMISS_TIME_OUT:" + AUTO_DISMISS_TIME_OUT);
		}

		mRepoObj = new JSONObject();
		try {
			mRepoObj.put(KEY_SHOW_TIME, System.currentTimeMillis());
		} catch (JSONException e) {
			e.printStackTrace();
		}

		if (mTmcTool != null && mTmcTool.onViewDataUpdate(title, obj.toString())) {
		} else {
			AppLogic.runOnUiGround(new Runnable2<String, JSONObject>(title, obj) {

				@Override
				public void run() {
					TMCManager.getInstance().notifyTrafficDialog(mP1, mP2);
				}
			});
		}

		AppLogic.removeUiGroundCallback(mTimeoutRunnable);
		AppLogic.runOnBackGround(new Runnable1<String>(tts) {

			@Override
			public void run() {
				if (mSpeechId != TtsManager.INVALID_TTS_TASK_ID) {
					TtsManager.getInstance().cancelSpeak(mSpeechId);
				}
				useStartNaviTask();
				TtsManager.getInstance().speakVoice(TtsManager.BEEP_VOICE_URL, new TtsUtil.ITtsCallback() {
					@Override
					public void onEnd() {
						AppLogic.runOnBackGround(new Runnable() {

							@Override
							public void run() {
								mSpeechId = TtsManager.getInstance().speakText(mP1, new ITtsCallback() {
									@Override
									public void onEnd() {
										super.onEnd();
										AppLogic.runOnUiGround(mTimeoutRunnable, AUTO_DISMISS_TIME_OUT);
									}
								});
							}
						}, 80);
					}
				});
			}
		});
	}
	
	public JSONObject mRepoObj;
	public static final String KEY_SHOW_TIME = "showTime";
	public static final String KEY_DISMISS_TIME = "dismissTime";
	public static final String KEY_WIDGET_WIDTH_HEIGHT = "wh";
	public static final String KEY_TOUCH_XY = "xy";
	public static final String KEY_DISMISS_TYPE = "dismissType";
	public static final String KEY_TYPE = "type";

	public static final int REPORT_TYPE_VOICE_CANCEL = 1;
	public static final int REPORT_TYPE_TIMEOUT_CANCEL = 2;
	public static final int REPORT_TYPE_VOICE_START = 3;
	public static final int REPORT_TYPE_TOUCH_START = 4;
	
	public void incToastCount() {
		int count = PreferenceUtil.getInstance().getTrafficToastCount();
		PreferenceUtil.getInstance().setTrafficToastCount(++count);
		LogUtil.logd("save touch count:" + count);
	}
	
	public boolean enableToast() {
		return PreferenceUtil.getInstance().getTrafficToastCount() < 5;
	}
	
	private void useStartNaviTask() {
		AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {

			@Override
			public boolean needAsrState() {
				return false;
			}

			@Override
			public String getTaskId() {
				return "START_NAVI";
			}

			@Override
			public void onCommandSelected(String type, String command) {
				RecorderWin.close();
				if ("START_NAVI".equals(type)) {
					startNavi(true);
					return;
				}
				if ("CANCEL_DIALOG".equals(type)) {
					try {
						mRepoObj.put(KEY_DISMISS_TYPE, REPORT_TYPE_VOICE_CANCEL);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					endTrafficDialog();
					return;
				}
			}
		};
		acsc.addCommand("START_NAVI", NativeData.getResStringArray("RS_CMD_TRAFFIC_START_NAVI"));
		acsc.addCommand("CANCEL_DIALOG", NativeData.getResStringArray("RS_CMD_TRAFFIC_CANCEL_DIALOG"));
		mHasWakeup = true;
		WakeupManager.getInstance().useWakeupAsAsr(acsc);
	}
	
	private boolean mCurrEnableState;
	
	public void setSmartTrafficSetting(boolean enable, boolean trueSet) {
		LogUtil.logd("setSmartTrafficSetting enable:" + enable + ",trueSet:" + trueSet);
		if (trueSet) {
			PreferenceUtil.getInstance().setNavNewspaperable(enable);
			return;
		}
		mCurrEnableState = enable;
		Req_Notify_User_Config config = new Req_Notify_User_Config();
		if (enable) {
			config.uint64Flags = (long) EquipmentManager.USER_CONFIG_FLAG_SMART_TRAVEL;
		} else {
			config.uint64Flags = 0L;
		}
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_NOTIFY_USER_CONFIG, config);
	}
	
	private void onSettingResponse(boolean isSucc) {
		if (isSucc) {
			if (mCurrEnableState) {
				AsrManager.getInstance().setNeedCloseRecord(false);
				PreferenceUtil.getInstance().setNavNewspaperable(true);
				RecorderWin.speakText(NativeData.getResString("RS_VOICE_OPEN_NAV_NEWSPAPER"), null);
			} else {
				AsrManager.getInstance().setNeedCloseRecord(false);
				PreferenceUtil.getInstance().setNavNewspaperable(false);
				RecorderWin.speakText(NativeData.getResString("RS_VOICE_CLOSE_NAV_NEWSPAPER"), null);
			}
		} else {
			AsrManager.getInstance().setNeedCloseRecord(false);
			RecorderWin.speakText(NativeData.getResString("RS_VOICE_NONET_NAV_NEWSPAPER"), null);
		}
	}

	/**
	 * 开始导航
	 */
	public void startNavi(boolean fromVoice) {
		if (mRepoObj != null) {
			if (fromVoice) {
				try {
					mRepoObj.put(KEY_DISMISS_TYPE, REPORT_TYPE_VOICE_START);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				try {
					mRepoObj.put(KEY_DISMISS_TYPE, REPORT_TYPE_TOUCH_START);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		NavManager.getInstance().NavigateTo(mDestinInfo, PoiAction.ACTION_NAVI);
		endTrafficDialog();
	}
	
	public void testGPS(double lat, double lng) {
		Req_Traffic_Between_Company_Home gps = new Req_Traffic_Between_Company_Home();
		gps.floatLat = (float) lat;
		gps.floatLng = (float) lng;
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_TRAFFIC_BETWEEN_COMPANY_HOME, gps);
		LogUtil.logd("testGPS lat:" + lat + ",lng:" + lng);
	}

	private boolean hasUploadGps;

	public void uploadCurrentGPS(double lat, double lng) {
		if (hasUploadGps) {
			return;
		}
		hasUploadGps = true;
		mUploadTaskRunnable = new Runnable2<Double, Double>(lat, lng) {

			@Override
			public void run() {
				mUploadTaskRunnable = null;
				Req_Traffic_Between_Company_Home gps = new Req_Traffic_Between_Company_Home();
				gps.floatLat = mP1.floatValue();
				gps.floatLng = mP2.floatValue();
				JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
						UiEquipment.SUBEVENT_REQ_TRAFFIC_BETWEEN_COMPANY_HOME, gps);
				LogUtil.logd("uploadCurrentGPS lat:" + gps.floatLat + ",lng:" + gps.floatLng);
			}
		};
		if (isTriggleUpload) {
			mUploadTaskRunnable.run();
		}
	}

	Runnable2<Double, Double> mUploadTaskRunnable = null;
	private boolean isTriggleUpload;
	
	public void triggleUploadGps() {
		LogUtil.logd("triggleUploadGps:" + isTriggleUpload);
		if (mUploadTaskRunnable != null) {
			mUploadTaskRunnable.run();
		} else {
			isTriggleUpload = true;
		}
	}

	private abstract class FakeReqTask implements Runnable {
		public int mReqId;
		protected PushManager.FakeRequest mReqTask;

		public FakeReqTask(int id, PushManager.FakeRequest req) {
			this.mReqId = id;
			this.mReqTask = req;
		}

		private void doFakeConnect() {
			String serverURL = mReqTask.strUrl;
			int method = mReqTask.uint32Method;
			byte[] postData = mReqTask.bytesPostData;
			try {
				if (method == 0) {
					HttpGet httpRequest = new HttpGet(serverURL);// 建立http get联机
					HttpResponse httpResponse = new DefaultHttpClient().execute(httpRequest);// 发出http请求
					int retCode = httpResponse.getStatusLine().getStatusCode();
					if (retCode == 200) {
						String result = EntityUtils.toString(httpResponse.getEntity());// 获取相应的字符串
						onConnectResult(retCode, result);
					} else {
						onConnectResult(retCode, "");
					}
				} else {

				}
			} catch (IOException e) {
				e.printStackTrace();
				onConnectResult(-1, "");
			}
		}

		@Override
		public void run() {
			onStartConnect();
			doFakeConnect();
		}

		public abstract void onStartConnect();

		public abstract void onConnectResult(int retCode, String resultStr);
	}

	public void setRemoteTmcTool(final String packageName) {
		LogUtil.logd("setTmcTool:" + packageName);
		if (TextUtils.isEmpty(packageName)) {
			mTmcTool = null;
		} else {
			mTmcTool = new TXZNavManager.TmcTool() {
				@Override
				public void setOperateListener(OperateListener listener) {
				}

				@Override
				public boolean needWait() {
					ServiceManager.ServiceData data = ServiceManager.getInstance().sendInvokeSync(packageName, "tool.tmc.needWait", null);
					return data.getBoolean();
				}

				@Override
				public boolean isIgnore() {
					ServiceManager.ServiceData data = ServiceManager.getInstance().sendInvokeSync(packageName, "tool.tmc.isIgnore", null);
					return data.getBoolean();
				}

				@Override
				public boolean onSmartTraffic(SmartTravel travel) {
					ServiceManager.ServiceData data = ServiceManager.getInstance().sendInvokeSync(packageName, "tool.tmc.onSmartTraffic", SmartTravel.toByteArray(travel));
					return data.getBoolean();
				}

				@Override
				public boolean onViewDataUpdate(String title, String data) {
					JSONBuilder jsonBuilder = new JSONBuilder();
					jsonBuilder.put("title", title);
					jsonBuilder.put("data", data);
					ServiceManager.ServiceData sd = ServiceManager.getInstance().sendInvokeSync(packageName, "tool.tmc.onViewDataUpdate", jsonBuilder.toBytes());
					return sd.getBoolean();
				}

				@Override
				public void onDismissDialog() {
					ServiceManager.getInstance().sendInvokeSync(packageName, "tool.tmc.onDismissDialog", null);
				}
			};
		}
	}
}