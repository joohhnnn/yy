package com.txznet.txz.module.mtj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mobstat.ExtraInfo;
import com.baidu.mobstat.SendStrategyEnum;
import com.baidu.mobstat.StatService;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.util.EncodeUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class MtjModule extends IModule {

	private static final String TAG = "MTJ::";
	private static final String SP_NAME = "mtj";
	private static final String KEY_BDUID = "bduid";

	public static final String LABEL_DEFAULT = "";

	public static final String EVENTID_VOICE_TRIGGER = "1001";// 调起语音
	public static final String EVENTID_VOICE_WEAKUP = "1016";// 语音唤醒
	public static final String EVENTID_VOICE_MANUAL = "1017";// 手动唤醒
	public static final String EVENTID_VOICE_MULTIPLE = "1018";// 二次交互
	public static final String EVENTID_VOICE_INVALID = "1002";// 语音无效唤醒
	public static final String EVENTID_CALL = "1003";// 电话功能
	public static final String EVENTID_NAV = "1004";// 发起导航
	public static final String EVENTID_NAV_NEARBY = "1005";// 周边请求
	public static final String EVENTID_NAV_ABOUT = "1006";// 导航相关指令
	public static final String EVENTID_MUSIC = "1007";// 音乐功能
	public static final String EVENTID_CMD = "1009"; // 语音设置
	public static final String EVENTID_NAV_MULTIPLE = "1014"; // 语音发起导航、周边进入多轮澄清
	public static final String EVENTID_VOICE_ERROR = "1015"; // 语音错误
	public static final String EVENTID_USE_TIME = "1028"; // 使用时长
	public static final String EVENTID_USER_NLP = "1026"; // 使用NLP服务
	public static final String EVENTID_NET_ASR_BAIDU = "1031"; // 访问百度语音在线识别
	public static final String EVENTID_EMOTION_TTS = "1030"; // 情感tts

	private boolean mIsInited = false;
	private boolean mIsMtjEnable;
	private boolean mIsReportId;
	private SharedPreferences sp;
	private ExtraInfo mInfo;
	private List<String> mEventCache;
	
	private Boolean closeMtjModule = null;

	private static MtjModule mInstance = new MtjModule();

	private MtjModule() {
		sp = GlobalContext.get().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);

		mEventCache = new ArrayList<String>();
	}

	public static MtjModule getInstance() {
		return mInstance;
	}

	private BroadcastReceiver baiduLoginReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			JNIHelper.logd(TAG + "receive login broadcast");
			String bduid = intent.getStringExtra("uid");
			String username = intent.getStringExtra("username");
			String displayname = intent.getStringExtra("displayname");
			JNIHelper.logd(TAG + "BDuid:" + bduid + " username:" + username + " displayname:" + displayname);

			sp.edit().putString(KEY_BDUID, bduid).apply();
			mInfo = new ExtraInfo();
			mInfo.setV1(getTXZRecodeUid());
			mInfo.setV2(bduid);
		}
	};

	private BroadcastReceiver baiduLogoutReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			JNIHelper.logd(TAG + "receive logout broadcast");
			sp.edit().putString(KEY_BDUID, "").apply();

			mInfo = new ExtraInfo();
			mInfo.setV1(getTXZRecodeUid());
		}
	};

	private BroadcastReceiver MtjReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			JNIHelper.logd(TAG + "receive mtj broadcast");
			String command = intent.getStringExtra("command");
			String json = intent.getStringExtra("json");
			handleJson(command, json);
		}
	};

	@Override
	public int initialize_addPluginCommandProcessor() {
		IntentFilter filter = new IntentFilter("com.txznet.mtj");
		GlobalContext.get().registerReceiver(MtjReceiver, filter);
		PluginManager.addCommandProcessor("txz.mtj.", new PluginManager.CommandProcessor() {

			@Override
			public Object invoke(String command, Object[] args) {
				if (TextUtils.isEmpty(command) || args == null || args.length == 0) {
					return null;
				}
				if (!(args[0] instanceof String)) {
					return null;
				}
				JNIHelper.logd(TAG + "plugin processor command:" + command + " data:" + (String) args[0]);
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.mtj." + command,
						((String) args[0]).getBytes(), null);
				return null;
			}
		});
		return super.initialize_addPluginCommandProcessor();
	}

	@Override
	public int initialize_AfterInitSuccess() {

		Intent loginIntent = new Intent();
		loginIntent.setAction("com.txznet.txz.loginstatus.login");
		loginIntent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		loginIntent.putExtra("uid", getTXZRecodeUid());
		JNIHelper.logd(TAG + "uid:" + getTXZRecodeUid());
		GlobalContext.get().sendBroadcast(loginIntent);

		IntentFilter baiduLoginFilter = new IntentFilter("com.txznet.txz.baidu.loginstatus.login");
		IntentFilter baiduLogoutFilter = new IntentFilter("com.txznet.txz.baidu.loginstatus.logout");
		GlobalContext.get().registerReceiver(baiduLoginReceiver, baiduLoginFilter);
		GlobalContext.get().registerReceiver(baiduLogoutReceiver, baiduLogoutFilter);
		
		if (closeMtjModule == null) {
			HashMap<String, String> config = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_CLOSE_MTJ_MODULE);
			closeMtjModule = false;
			if (config != null && config.get(TXZFileConfigUtil.KEY_CLOSE_MTJ_MODULE) != null) {
				try {
					closeMtjModule = Boolean.parseBoolean(config.get(TXZFileConfigUtil.KEY_CLOSE_MTJ_MODULE));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			JNIHelper.logd("closeMtjModule::" + closeMtjModule);
		}
		
		if (!closeMtjModule) {
			initMtj();
		}
		
		return super.initialize_AfterInitSuccess();
	}

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE);

		return super.initialize_BeforeStartJni();
	}

	private void initMtj() {
		if (mIsInited || !mIsMtjEnable) {
			return;
		}
		JNIHelper.logd(TAG + "Init");
		String sign = "2cd7ffca4cb8795ff7a4616c3f362256";
		if (mIsReportId) {
			sign = LicenseManager.getInstance().getAppId();
		}
		String channel = EncodeUtil.Md5Str("baidu" + sign);
		JNIHelper.logd(TAG + "channel:" + channel);

		mInfo = new ExtraInfo();
		mInfo.setV1(getTXZRecodeUid());
		String bduid = sp.getString(KEY_BDUID, "");
		if (!TextUtils.isEmpty(bduid)) {
			mInfo.setV2(bduid);
		}
		//百度mtj ceb589b4bf 测试 7c54447f9e 百度 ecea5ab978
		StatService.setAppKey("ceb589b4bf");
		StatService.setAppChannel(GlobalContext.get(), channel, true);
		StatService.setSendLogStrategy(GlobalContext.get(), SendStrategyEnum.APP_START, 1, false);
		StatService.setDebugOn(true);
		mIsInited = true;

		if (mEventCache != null && !mEventCache.isEmpty()) {
			JNIHelper.logd(TAG + "event cache:" + mEventCache.size());
			for (String id : mEventCache) {
				event(id);
			}
		}
		mEventCache.clear();

		eventStart(EVENTID_USE_TIME);
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_ACTION_EQUIPMENT: {
			if (UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE == subEventId) {
				try {
					UiEquipment.ServerConfig pbServerConfig = UiEquipment.ServerConfig.parseFrom(data);
					JNIHelper.logd(TAG + " mtj:" + pbServerConfig.bMtj + " rerport:" + pbServerConfig.bMtjReportApp);
					if (null == pbServerConfig.bMtj || !pbServerConfig.bMtj) {
						mIsMtjEnable = true;
					} else {
						mIsMtjEnable = false;
					}

					if (pbServerConfig.bMtjReportApp != null && pbServerConfig.bMtjReportApp) {
						mIsReportId = true;
					} else {
						mIsReportId = false;
					}
					JNIHelper.logd(TAG + " enable:" + mIsMtjEnable + " rerport:" + mIsReportId);
				} catch (InvalidProtocolBufferNanoException e) {
				}
				break;
			}
		}
		}
		return super.onEvent(eventId, subEventId, data);
	}

	public void event(String eventId) {
		event(eventId, mInfo);
	}

	public void eventStart(String id) {
		JNIHelper.logd(TAG + "event start:" + id + " inited:" + mIsInited);
		if (mIsInited) {
			StatService.onEventStart(GlobalContext.get(), id, LABEL_DEFAULT);
		}
	}

	public void eventEnd(String id) {
		eventEnd(id, mInfo);
	}

	public void event(String eventId, ExtraInfo info) {
		JNIHelper.logd(TAG + "event:" + eventId + " inited:" + mIsInited);
		if (mIsInited) {
			StatService.onEvent(GlobalContext.get(), eventId, LABEL_DEFAULT, info);
		} else {
			mEventCache.add(eventId);
		}
	}

	public void eventEnd(String id, ExtraInfo info) {
		JNIHelper.logd(TAG + "event end:" + id + " inited:" + mIsInited);
		if (mIsInited) {
			StatService.onEventEnd(GlobalContext.get(), id, LABEL_DEFAULT, info);
		}
	}

	public String getTXZRecodeUid() {
		return EncodeUtil.Md5Str("baidu" + ProjectCfg.getUid());
	}

	public String getBDUid() {
		return sp.getString(KEY_BDUID, "");
	}

	public byte[] invokeTXZMtj(final String packageName, String command, byte[] data) {
		if (TextUtils.isEmpty(command) || data == null) {
			return null;
		}
		String str = new String(data);
		JNIHelper.logd(TAG + "invokeTXZMtj command:" + command + " data:" + str);
		handleJson(command, new String(data));
		return null;
	}

	private void handleJson(String command, String json) {
		if(TextUtils.isEmpty(command) || TextUtils.isEmpty(json)){
			JNIHelper.loge(TAG + "command or json is empty");
			return;
		}
		try {
			JSONObject jsonObject = new JSONObject(json);
			if (jsonObject == null || !jsonObject.has("id")) {
				return;
			}
			ExtraInfo info = new ExtraInfo();
			info.setV1(getTXZRecodeUid());
			if (!TextUtils.isEmpty(getBDUid())) {
				info.setV2(getBDUid());
			}
			if (jsonObject.has("v3")) {
				info.setV3(jsonObject.getString("v3"));
			}
			if (jsonObject.has("v4")) {
				info.setV4(jsonObject.getString("v4"));
			}
			if (jsonObject.has("v5")) {
				info.setV5(jsonObject.getString("v5"));
			}
			if (jsonObject.has("v6")) {
				info.setV6(jsonObject.getString("v6"));
			}
			if (jsonObject.has("v7")) {
				info.setV7(jsonObject.getString("v7"));
			}
			if (jsonObject.has("v8")) {
				info.setV8(jsonObject.getString("v8"));
			}
			if (jsonObject.has("v9")) {
				info.setV9(jsonObject.getString("v9"));
			}
			if (jsonObject.has("v10")) {
				info.setV3(jsonObject.getString("v3"));
			}
			String id = jsonObject.getString("id");
			if ("onEvent".equals(command)) {
				event(id, info);
			} else if ("onEventStart".equals(command)) {
				eventStart(id);
			} else if ("onEventEnd".equals(command)) {
				eventEnd(id, info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			JNIHelper.loge(TAG + "handle json error:" + e.toString());
		}
	}
}
