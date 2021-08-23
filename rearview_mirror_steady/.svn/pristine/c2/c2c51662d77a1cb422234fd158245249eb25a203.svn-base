package com.txznet.txz.module.weixin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult.ERRORNO;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.makewechatssesion.UiMakeWechatSession;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.wechat.UiWechat;
import com.txz.ui.wechatcontact.WechatContactData;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContact;
import com.txz.ui.wechatcontact.WechatContactData.WeChatContacts;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.StatusUtil;
import com.txznet.comm.remote.util.StatusUtil.StatusListener;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.dvr.DVRScaner;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZNavManager.PathInfo;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.component.choice.list.AbstractChoice;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.camera.CameraManager;
import com.txznet.txz.module.camera.CameraManager.CapturePictureListener;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavInscriber;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.ui.widget.PhotoFloatView;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.ImageUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.text.TextUtils;

public class WeixinManager extends IModule {
	private static final String WEBCHAT_PACKAGE_NAME = "com.txznet.webchat";

	static WeixinManager sModuleInstance = new WeixinManager();
	private List<OnQRCodeListener> mOnQRCodeListeners = new ArrayList<OnQRCodeListener>();
	private Map<String, String> mExpressionMap = new HashMap<String, String>();

	// 绑定二维码
	private String mBindQrUrl = "";
	// 绑定用户昵称
	private String mBindUserNick = "";
	// 是否已有用户绑定
	private boolean bHasBind;
	
	private WeixinManager() {
		initExpressionMap();
		StatusUtil.addStatusListener(new StatusListener() {
			
			@Override
			public void onMusicPlay() {
			}
			
			@Override
			public void onMusicPause() {
			}
			
			@Override
			public void onEndTts() {
				mIsInTtsPlaying=false;
				AppLogic.runOnBackGround(new Runnable() {
					
					@Override
					public void run() {
						dealDelayConfirmRunnable();
					}
				},200);
			}
			
			@Override
			public void onEndCall() {
			}
			
			@Override
			public void onEndAsr() {
				mIsInAsrWorking = false;
				AppLogic.runOnBackGround(new Runnable() {
					
					@Override
					public void run() {
						dealDelayConfirmRunnable();
					}
				},200);
			}
			
			@Override
			public void onBeginTts() {
				mIsInTtsPlaying=true;
			}
			
			@Override
			public void onBeginCall() {
			}
			
			@Override
			public void onBeginAsr() {
				mIsInAsrWorking = true;

			}
			
			@Override
			public void onBeepEnd() {

			}
		});
	}
	private boolean mIsInTtsPlaying = false;
	private boolean mIsInAsrWorking = false;
	private Runnable mConfirmDelayRun = null;
	private void dealDelayConfirmRunnable(){
		if( !mIsInAsrWorking && !mIsInTtsPlaying && mConfirmDelayRun != null){
			AppLogic.runOnBackGround(mConfirmDelayRun, 0);
			mConfirmDelayRun = null;
		}
	}

	public boolean hasBind() {
		return bHasBind;
	}

	public String getBindQr() {
		return mBindQrUrl;
	}

	public String getBindUserNick() {
		return mBindUserNick;
	}

	private void initExpressionMap() {
//		"[微笑]", "[撇嘴]", "[色]", "[发呆]", "[得意]", "[流泪]", "[害羞]",
		mExpressionMap.put("呵呵哒", "[微笑]");
		mExpressionMap.put("微笑", "[微笑]");
		mExpressionMap.put("撇嘴", "[撇嘴]");
		mExpressionMap.put("色", "[色]");
		mExpressionMap.put("发呆", "[发呆]");
		mExpressionMap.put("得意", "[得意]");
		mExpressionMap.put("流泪", "[流泪]");
		mExpressionMap.put("害羞", "[害羞]");
//	    "[闭嘴]", "[睡]", "[大哭]", "[尴尬]", "[发怒]", "[调皮]", "[呲牙]",
		mExpressionMap.put("闭嘴", "[闭嘴]");
		mExpressionMap.put("睡", "[睡]");
		mExpressionMap.put("大哭", "[大哭]");
		mExpressionMap.put("尴尬", "[尴尬]");
		mExpressionMap.put("发怒", "[发怒]");
		mExpressionMap.put("调皮", "[调皮]");
		mExpressionMap.put("呲牙", "[呲牙]");
//	    "[惊讶]", "[难过]", "[酷]", "[冷汗]", "[抓狂]", "[吐]", "[偷笑]",
		mExpressionMap.put("惊讶", "[惊讶]");
		mExpressionMap.put("难过", "[难过]");
		mExpressionMap.put("酷", "[酷]");
		mExpressionMap.put("冷汗", "[冷汗]");
		mExpressionMap.put("抓狂", "[抓狂]");
		mExpressionMap.put("吐", "[吐]");
		mExpressionMap.put("偷笑", "[偷笑]");
//	    "[愉快]", "[白眼]", "[傲慢]", "[饥饿]", "[困]", "[惊恐]", "[流汗]",
		mExpressionMap.put("萌萌哒", "[愉快]");
		mExpressionMap.put("愉快", "[愉快]");
		mExpressionMap.put("白眼", "[白眼]");
		mExpressionMap.put("傲慢", "[傲慢]");
		mExpressionMap.put("饥饿", "[饥饿]");
		mExpressionMap.put("困", "[困]");
		mExpressionMap.put("惊恐", "[惊恐]");
		mExpressionMap.put("流汗", "[流汗]");
//	    "[憨笑]", "[悠闲]", "[奋斗]", "[咒骂]", "[疑问]", "[嘘]", "[晕]",
		mExpressionMap.put("憨笑", "[憨笑]");
		mExpressionMap.put("悠闲", "[悠闲]");
		mExpressionMap.put("奋斗", "[奋斗]");
		mExpressionMap.put("咒骂", "[咒骂]");
		mExpressionMap.put("疑问", "[疑问]");
		mExpressionMap.put("嘘", "[嘘]");
		mExpressionMap.put("晕", "[晕]");
//	    "[疯了]", "[衰]", "[骷髅]", "[敲打]", "[再见]", "[擦汗]", "[抠鼻]",
		mExpressionMap.put("疯了", "[疯了]");
		mExpressionMap.put("衰", "[衰]");
		mExpressionMap.put("骷髅", "[骷髅]");
		mExpressionMap.put("敲打", "[敲打]");
		mExpressionMap.put("再见", "[再见]");
		mExpressionMap.put("擦汗", "[擦汗]");
		mExpressionMap.put("抠鼻", "[抠鼻]");
//	    "[鼓掌]", "[糗大了]", "[坏笑]", "[左哼哼]", "[右哼哼]", "[哈欠]", "[鄙视]",
		mExpressionMap.put("鼓掌", "[鼓掌]");
		mExpressionMap.put("糗大了", "[糗大了]");
		mExpressionMap.put("坏笑", "[坏笑]");
		mExpressionMap.put("左哼哼", "[左哼哼]");
		mExpressionMap.put("右哼哼", "[右哼哼]");
		mExpressionMap.put("哈欠", "[哈欠]");
		mExpressionMap.put("鄙视", "[鄙视]");
//	    "[委屈]", "[快哭了]", "[阴险]", "[亲亲]", "[吓]", "[可怜]", "[菜刀]",
		mExpressionMap.put("委屈", "[委屈]");
		mExpressionMap.put("快哭了", "[快哭了]");
		mExpressionMap.put("阴险", "[阴险]");
		mExpressionMap.put("么么哒", "[亲亲]");
		mExpressionMap.put("亲亲", "[亲亲]");
		mExpressionMap.put("吓", "[吓]");
		mExpressionMap.put("可怜", "[可怜]");
		mExpressionMap.put("菜刀", "[菜刀]");
//	    "[西瓜]", "[啤酒]", "[篮球]", "[乒乓]", "[咖啡]", "[饭]", "[猪头]",
		mExpressionMap.put("西瓜", "[西瓜]");
		mExpressionMap.put("啤酒", "[啤酒]");
		mExpressionMap.put("篮球", "[篮球]");
		mExpressionMap.put("乒乓", "[乒乓]");
		mExpressionMap.put("咖啡", "[咖啡]");
		mExpressionMap.put("饭", "[饭]");
		mExpressionMap.put("猪头", "[猪头]");
//	    "[玫瑰]", "[凋谢]", "[嘴唇]", "[爱心]", "[心碎]", "[蛋糕]", "[闪电]",
		mExpressionMap.put("玫瑰", "[玫瑰]");
		mExpressionMap.put("凋谢", "[凋谢]");
		mExpressionMap.put("嘴唇", "[嘴唇]");
		mExpressionMap.put("爱心", "[爱心]");
		mExpressionMap.put("心碎", "[心碎]");
		mExpressionMap.put("蛋糕", "[蛋糕]");
		mExpressionMap.put("闪电", "[闪电]");
//	    "[炸弹]", "[刀]", "[足球]", "[瓢虫]", "[便便]", "[月亮]", "[太阳]",
		mExpressionMap.put("炸弹", "[炸弹]");
		mExpressionMap.put("刀", "[刀]");
		mExpressionMap.put("足球", "[足球]");
		mExpressionMap.put("瓢虫", "[瓢虫]");
		mExpressionMap.put("便便", "[便便]");
		mExpressionMap.put("月亮", "[月亮]");
		mExpressionMap.put("太阳", "[太阳]");
//	    "[礼物]", "[拥抱]", "[强]", "[弱]", "[握手]", "[胜利]", "[抱拳]",
		mExpressionMap.put("礼物", "[礼物]");
		mExpressionMap.put("拥抱", "[拥抱]");
		mExpressionMap.put("强", "[强]");
		mExpressionMap.put("弱", "[弱]");
		mExpressionMap.put("握手", "[握手]");
		mExpressionMap.put("胜利", "[胜利]");
		mExpressionMap.put("抱", "[抱]");
//	    "[勾引]", "[拳头]", "[差劲]", "[爱你]", "[NO]", "[OK]", "[爱情]",
		mExpressionMap.put("勾引", "[勾引]");
		mExpressionMap.put("拳头", "[拳头]");
		mExpressionMap.put("差劲", "[差劲]");
		mExpressionMap.put("爱你", "[爱你]");
		mExpressionMap.put("NO", "[NO]");
		mExpressionMap.put("OK", "[OK]");
		mExpressionMap.put("爱情", "[爱情]");
//	    "[飞吻]", "[跳跳]", "[发抖]", "[怄火]", "[转圈]", "[磕头]", "[回头]",
		mExpressionMap.put("飞吻", "[飞吻]");
		mExpressionMap.put("跳跳", "[跳跳]");
		mExpressionMap.put("发抖", "[发抖]");
		mExpressionMap.put("怄火", "[怄火]");
		mExpressionMap.put("转圈", "[转圈]");
		mExpressionMap.put("磕头", "[磕头]");
		mExpressionMap.put("回头", "[回头]");
//	    "[跳绳]", "[投降]", "[激动]", "[乱舞]", "[献吻]", "[左太极]", "[右太极]"
		mExpressionMap.put("跳绳", "[跳绳]");
		mExpressionMap.put("投降", "[投降]");
		mExpressionMap.put("激动", "[激动]");
		mExpressionMap.put("乱舞", "[乱舞]");
		mExpressionMap.put("献吻", "[献吻]");
		mExpressionMap.put("左太极", "[左太极]");
		mExpressionMap.put("右太极", "[右太极]");
	}

	public static WeixinManager getInstance() {
		return sModuleInstance;
	}
	
	public static final String ACTION_SYNC_SESSION_LIST = "com.txznet.webchat.action.sync_session_list";
	private void initSyncSessionBroadcastReceiver() {
		IntentFilter filter = new IntentFilter(ACTION_SYNC_SESSION_LIST);
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				String jsonStr = intent.getStringExtra("contacts");
				updateWechatContact(jsonStr);
			}
		}, filter);
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_BIND_WX_SUCCESS);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_UNBIND_WX_SUCCESS);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_NAVIGATION);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_RESP_GET_BIND_WX_URL);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_RESP_UPLOAD_VOICE);

		regEvent(UiEvent.EVENT_WECHAT_MAKESESSION);

		regCommand("WECHAT_LANUCH");
		regCommand("WECHAT_MSG_RESPONSE");
		regCommand("WECHAT_MSG_SEND");
		regCommand("WECHAT_SESSION_MASK");
		// 新微信cmd
		regCommand("WECHAT_EXIT");
		regCommand("WECHAT_ENABLE_MSG_BROAD");
		regCommand("WECHAT_DISABLE_MSG_BROAD");
		regCommand("WECHAT_ENABLE_GROUP_MSG_BROAD");
		regCommand("WECHAT_DISABLE_GROUP_MSG_BROAD");
		
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_BATCH_UPLOAD_PIC);
		regEvent(UiEvent.EVENT_ACTION_WECHAT,
				UiWechat.SUBEVENT_GET_WECHAT_LOGIN_STATE);
		regEvent(UiEvent.EVENT_ACTION_WECHAT, UiWechat.SUBEVENT_ERR_NO_CONTACT);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE);
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_addPluginCommandProcessor() {
		//2.5.1增加
		PluginManager.addCommandProcessor("txz.wx.", new CommandProcessor() {
			
			@Override
			public Object invoke(String command, Object[] args) {
				if("qrcode".equals(command)){
					return mBindQrUrl;
				}
				return null;
			}
		});
		return super.initialize_addPluginCommandProcessor();
	}
	
	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件

		// 请求一次绑定信息
		AppLogic.runOnBackGround(reqQRCodeRun, 3000);
		
		return super.initialize_AfterStartJni();
	}
	
	Runnable reqQRCodeRun = new Runnable(){
		@Override
		public void run(){
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_BIND_WX_URL);
		}
	};

	WinConfirmAsr mWinConfirmNavigate;

	UiMap.NavigateInfo mLastNavigateInfo;

	@Override
	public int onCommand(String cmd) {
		JNIHelper.logd("cmd" + cmd);
		do {
			if (cmd.equals("WECHAT_LANUCH")) {
				if (SenceManager.getInstance().noneedProcSence(
						"app",
						new JSONBuilder().put("sence", "weixin")
								.put("action", "open").toBytes())) {
					break;
				}

				launchWebChat();
				break;
			}

			if (cmd.equals("WECHAT_MSG_RESPONSE")) {
				if (SenceManager.getInstance().noneedProcSence(
						"wechat",
						new JSONBuilder()
								.put("sence", "wechat")
								.put("action", "send")
								.put("text",
										TextResultHandle.getInstance()
												.getParseText()).toBytes())) {
					break;
				}

				if (interceptWeChat()) {
					break;
				}
				
				if (!checkEnabled()) {
					requestLogin();
					break;
				}
				
				mWeChatChoice = ON_CALL;
				AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
				requestRecentSession();
				break;
			}

			if (cmd.equals("WECHAT_MSG_SEND")) {
				if (SenceManager.getInstance().noneedProcSence(
						"wechat",
						new JSONBuilder()
								.put("sence", "wechat")
								.put("action", "send")
								.put("text",
										TextResultHandle.getInstance()
												.getParseText()).toBytes())) {
					break;
				}

				if (interceptWeChat()) {
					break;
				}

				if (!checkEnabled()) {
					requestLogin();
					break;
				}
				
				mWeChatChoice = ON_CALL;
				AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
				requestRecentSession();
				break;
			}

			if (cmd.equals("WECHAT_SESSION_MASK")) {
				if (SenceManager.getInstance().noneedProcSence(
						"wechat",
						new JSONBuilder()
								.put("sence", "wechat")
								.put("action", "mask")
								.put("text",
										TextResultHandle.getInstance()
												.getParseText()).toBytes())) {
					break;
				}

				if (interceptWeChat()) {
					break;
				}
				
				if (!checkEnabled()) {
					requestLogin();
					break;
				}
				
				mWeChatChoice = ON_SHIELD;
				AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
				requestRecentSessionForMaskByVersion();
				break;
			}
			
			if ("WECHAT_EXIT".equals(cmd)) {
				if (SenceManager.getInstance().noneedProcSence(
						"wechat",
						new JSONBuilder()
								.put("sence", "wechat")
								.put("action", "exit")
								.put("text",
										TextResultHandle.getInstance()
												.getParseText()).toBytes())) {
					break;
				}

				if (interceptWeChat()) {
					break;
				}
				
				if (!checkEnabled()) {
					requestLogin();
					break;
				}
				
				String spk = NativeData.getResString("RS_VOICE_WEBCHAT_CTRL_EXIT");
				RecorderWin.speakTextWithClose(spk, new Runnable() {
					
					@Override
					public void run() {
						ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wechat.ctrl.exit", String.valueOf(true).getBytes(), null);
					}
				});
				
				break;
			}
			
			if ("WECHAT_ENABLE_MSG_BROAD".equals(cmd)) {
				if (SenceManager.getInstance().noneedProcSence(
						"wechat",
						new JSONBuilder()
								.put("sence", "wechat")
								.put("action", "enable_msg_broad")
								.put("text",
										TextResultHandle.getInstance()
												.getParseText()).toBytes())) {
					break;
				}

				if (interceptWeChat()) {
					break;
				}

				if (!checkEnabled()) {
					requestLogin();
					break;
				}

				String spk = NativeData
						.getResString("RS_VOICE_WEBCHAT_DO_AUTO_SPEAK_ENABLE");
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.WEBCHAT,
								"wechat.ctrl.enableAutoSpeak",
								String.valueOf(true).getBytes(), null);
					}
				});
				
				break;
			}
			
			if ("WECHAT_DISABLE_MSG_BROAD".equals(cmd)) {
				if (SenceManager.getInstance().noneedProcSence(
						"wechat",
						new JSONBuilder()
								.put("sence", "wechat")
								.put("action", "disable_msg_broad")
								.put("text",
										TextResultHandle.getInstance()
												.getParseText()).toBytes())) {
					break;
				}

				if (interceptWeChat()) {
					break;
				}

				if (!checkEnabled()) {
					requestLogin();
					break;
				}

				String spk = NativeData
						.getResString("RS_VOICE_WEBCHAT_DO_AUTO_SPEAK_DISABLE");
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.WEBCHAT,
								"wechat.ctrl.enableAutoSpeak",
								String.valueOf(false).getBytes(), null);
					}
				});
				
				break;
			}
			
			if ("WECHAT_ENABLE_GROUP_MSG_BROAD".equals(cmd)) {
				if (SenceManager.getInstance().noneedProcSence(
						"wechat",
						new JSONBuilder()
								.put("sence", "wechat")
								.put("action", "enable_group_msg_broad")
								.put("text",
										TextResultHandle.getInstance()
												.getParseText()).toBytes())) {
					break;
				}

				if (interceptWeChat()) {
					break;
				}

				if (!checkEnabled()) {
					requestLogin();
					break;
				}

				String spk = NativeData
						.getResString("RS_VOICE_WEBCHAT_DO_GROUP_SPEAK_ENABLE");
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.WEBCHAT,
								"wechat.ctrl.filter.groupmsg",
								String.valueOf(false).getBytes(), null);
					}
				});
				
				break;
			}
			
			if ("WECHAT_DISABLE_GROUP_MSG_BROAD".equals(cmd)) {
				if (SenceManager.getInstance().noneedProcSence(
						"wechat",
						new JSONBuilder()
								.put("sence", "wechat")
								.put("action", "disable_group_msg_broad")
								.put("text",
										TextResultHandle.getInstance()
												.getParseText()).toBytes())) {
					break;
				}

				if (interceptWeChat()) {
					break;
				}

				if (!checkEnabled()) {
					requestLogin();
					break;
				}

				String spk = NativeData
						.getResString("RS_VOICE_WEBCHAT_DO_GROUP_SPEAK_DISABLE");
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.WEBCHAT,
								"wechat.ctrl.filter.groupmsg",
								String.valueOf(true).getBytes(), null);
					}
				});
				
				break;
			}
		} while (false);
		return 0;
	}

	private boolean isInQuickNav = false;
	public boolean getIsInQuickNav(){
		return isInQuickNav;
	}
	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_ACTION_EQUIPMENT:
			switch (subEventId) {
			case UiEquipment.SUBEVENT_RESP_UPLOAD_VOICE: {
				try {
					JNIHelper.logd("upload voice response");
					UiEquipment.Resp_UploadVoice res = UiEquipment.Resp_UploadVoice
							.parseFrom(data);
					if (!res.bOk) {
						JNIHelper.loge("upload voice error: " + res.strErrMsg);
						ServiceManager.getInstance().sendInvoke(
								ServiceManager.WEBCHAT,
								"wx.upload.voice.error",
								res.strErrMsg.getBytes(), null);
						break;
					}
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.WEBCHAT, "wx.upload.voice.success",
							res.strUrl.getBytes(), null);
				} catch (Exception e) {
					JNIHelper.loge("upload voice exception");
					e.printStackTrace();
					ServiceManager.getInstance().sendInvoke(
							ServiceManager.WEBCHAT, "wx.upload.voice.error",
							null, null);
				}
				break;
			}
			case UiEquipment.SUBEVENT_RESP_GET_BIND_WX_URL: {
				try {
					UiEquipment.Resp_GetBindWxUrl res = UiEquipment.Resp_GetBindWxUrl
							.parseFrom(data);
					if (!TextUtils.isEmpty(res.strChannelNo)) {
						return super.onEvent(eventId, subEventId, data);
					}
					JSONBuilder builder = new JSONBuilder();
					builder.put("issuccess", res.bOk);
					if (res.bOk) {
						// 通知车载微信绑定状态变化
						builder.put("qrcode", res.strBindWxUrl);
						builder.put("isbind", res.bIsBind);
						if (res.bIsBind) {
							builder.put("nick", res.msgWx.strNick);
						}
						if (res.msgServerConfigWechatInfo != null) {
							UiEquipment.SC_WeChat info = res.msgServerConfigWechatInfo;
							builder.put("uint64Flag", info.uint64Flag);
						}
						updateBindStatus(res.bIsBind, res.strBindWxUrl, res.msgWx.strNick);
					}

					ServiceManager.getInstance().broadInvoke("wx.info.qrcode",
							builder.toString().getBytes());
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case UiEquipment.SUBEVENT_NOTIFY_UNBIND_WX_SUCCESS: {
				try {
					UiEquipment.Notify_UnbindWxSuccess notify = UiEquipment.Notify_UnbindWxSuccess
							.parseFrom(data);
					JNIHelper.logd("unbind: url=" + notify.strBindWxUrl);

					JSONBuilder builder = new JSONBuilder();
					builder.put("issuccess", true);
					builder.put("qrcode", notify.strBindWxUrl);
					builder.put("isbind", false);
					builder.put("nick", null);

					ServiceManager.getInstance().broadInvoke("wx.info.qrcode",
							builder.toString().getBytes());
					updateBindStatus(false, notify.strBindWxUrl, "");
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case UiEquipment.SUBEVENT_NOTIFY_BIND_WX_SUCCESS: {
				try {
					UiEquipment.Notify_BindWxSuccess notify = UiEquipment.Notify_BindWxSuccess
							.parseFrom(data);
					ServiceManager.getInstance().broadInvoke("wx.info.nick",
							notify.msgWx.strNick.getBytes());
					updateBindStatus(true, "", notify.msgWx.strNick);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			case UiEquipment.SUBEVENT_NOTIFY_NAVIGATION: {
				notifyNavigation(data);
				break;
			}
			case UiEquipment.SUBEVENT_NOTIFY_BATCH_UPLOAD_PIC:
				batchCapturePhoto(data);
				break;
			case UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE: {
				try {
					UiEquipment.ServerConfig config = UiEquipment.ServerConfig
							.parseFrom(data);
					if (config != null) {
						if (config.msgWechatInfo != null) {
							if (config.msgWechatInfo.uint64Flag != null) {
								//判断第2位，值为1的时候微信可以使用
                                if (((config.msgWechatInfo.uint64Flag & (0x1 << 1)) >> 1) == 1) {
                                    mEnableWechat = true;
                                } else {
                                    mEnableWechat = false;
                                }
                                LogUtil.d("wx serverCongif uint64Flag=" + config.msgWechatInfo.uint64Flag + " ; enableWx : " + mEnableWechat);
							}
						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			}
			}

			break;
		case UiEvent.EVENT_WECHAT_MAKESESSION:
			doWeChatMakeSession(subEventId, data);
			break;

		case UiEvent.EVENT_ACTION_WECHAT:
			switch (subEventId) {
			case com.txz.ui.wechat.UiWechat.SUBEVENT_GET_WECHAT_LOGIN_STATE:
				doGetWechatLoginState();
				break;
			case UiWechat.SUBEVENT_ERR_NO_CONTACT:
				doWechatNoContact();
				break;
			}
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}

	public void notifyNavigation(byte[] data){

		try {
			//倒车影像的情况下，不进行微信下发导航
			if (TXZPowerControl.isEnterReverse()) {
				TXZPowerControl.setLastNavData(data);
				return;
			}

			final UiMap.NavigateInfo info = mLastNavigateInfo = UiMap.NavigateInfo
					.parseFrom(data);

			if (info.msgServerPushInfo != null) {
				JNIHelper.logd("time="
						+ info.msgServerPushInfo.uint32Time + ", nick="
						+ info.msgServerPushInfo.strFromWxNick
						+ ",now=" + System.currentTimeMillis());

				

				if (TextUtils.isEmpty(info.strTargetAddress)) {
					// 地址为空时通过地理信息转换重新获取
					final GeoCoder geoCoder = GeoCoder.newInstance();
					geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
						@Override
						public void onGetReverseGeoCodeResult(
								ReverseGeoCodeResult result) {
							geoCoder.destroy();

							if (info != mLastNavigateInfo) {
								return;
							}
							// if (result.getLocation().latitude !=
							// mLastNavigateInfo.msgGpsInfo.dblLat
							// || result.getLocation().longitude !=
							// mLastNavigateInfo.msgGpsInfo.dblLng) {
							// return;
							// }

							mLastNavigateInfo = null;

							if (result.error == ERRORNO.NO_ERROR)
								info.strTargetAddress = result
										.getAddress();
							else
								info.strTargetAddress = "未知地址";

							JNIHelper
									.sendEvent(
											UiEvent.EVENT_ACTION_EQUIPMENT,
											UiEquipment.SUBEVENT_NOTIFY_NAVIGATION,
											info);
						}

						@Override
						public void onGetGeoCodeResult(
								GeoCodeResult result) {
						}
					});
					geoCoder.reverseGeoCode(new ReverseGeoCodeOption()
							.location(new LatLng(
									info.msgGpsInfo.dblLat,
									info.msgGpsInfo.dblLng)));
					return;
				}

				// String time = "";
				// if (ProtoBufferUtil.isIntegerZero(notify.uint32Time)
				// ==
				// false) {
				// Date date = new Date(((long) notify.uint32Time) *
				// 1000);
				// time = new SimpleDateFormat("HH:mm",
				// Locale.CHINESE).format(date);
				// }

				if (info.msgServerPushInfo.uint32Type == UiMap.NT_MULTI_NATIGATION) {// 多车同行

					String message = new String();
					String tts = new String();
					if (info.msgServerPushInfo.uint64FromUid == NativeData
							.getUID()) {
						// 本人
						message = NativeData
								.getResString("RS_WX_MULTI_NAV_YOUR_HINT")
								.replace(
										"%TIME%",
										makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
								.replace(
										"%TAR%",
										info.strTargetName + "["
												+ info.strTargetAddress
												+ "]");
						tts = NativeData
								.getResString("RS_WX_MULTI_NAV_YOUR")
								.replace(
										"%TIME%",
										makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
								.replace(
										"%TAR%",
										info.strTargetName + "["
												+ info.strTargetAddress
												+ "]");
					} else {
						message = NativeData
								.getResString("RS_WX_MULTI_NAV_FRIEND_HINT")
								.replace(
										"%NAME%",
										info.msgServerPushInfo.strFromWxNick)
								.replace(
										"%TIME%",
										makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
								.replace(
										"%TAR%",
										info.strTargetName + "["
												+ info.strTargetAddress
												+ "]");
						tts = NativeData
								.getResString("RS_WX_MULTI_NAV_FRIEND")
								.replace(
										"%NAME%",
										info.msgServerPushInfo.strFromWxNick)
								.replace(
										"%TIME%",
										makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
								.replace(
										"%TAR%",
										info.strTargetName + "["
												+ info.strTargetAddress
												+ "]");
					}

					multiNavigateConfirm(message, tts,
							new Runnable() {
								@Override
								public void run() {
									// TtsManager.getInstance().speakText("好的，即将开始进入多车同行，并规划目的地");
								}
							}, info);

				} else {
					String hint = NativeData
							.getResString("RS_WX_RECEIVE_NAV_HINT")
							.replace(
									"%TIME%",
									makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
							.replace(
									"%NAME%",
									info.msgServerPushInfo.strFromWxNick)
							.replace(
									"%TAR%",
									info.strTargetName + "["
											+ info.strTargetAddress
											+ "]");
					String spk = NativeData
							.getResString("RS_WX_RECEIVE_NAV")
							.replace(
									"%TIME%",
									makeFriendlyTime(((long) info.msgServerPushInfo.uint32Time) * 1000))
							.replace(
									"%NAME%",
									info.msgServerPushInfo.strFromWxNick)
							.replace(
									"%TAR%",
									info.strTargetName + "["
											+ info.strTargetAddress
											+ "]");
					navigateConfirm(hint, spk,
							new Runnable() {
								@Override
								public void run() {
									// TtsManager.getInstance().speakText("好的，即将开始规划目的地");
								}
							}, info);
				}
			} else {
				JNIHelper.loge("info.msgServerPushInfo is null");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateBindStatus(boolean hasBind, String qrUrl, String userNick) {
		// 记录绑定信息
		mBindQrUrl = qrUrl;
		bHasBind = hasBind;
		mBindUserNick = userNick;

		notifyBindStatusChanged();
	}

	private void notifyBindStatusChanged() {
		for (OnQRCodeListener listener : mOnQRCodeListeners) {
			listener.onGetQrCode(bHasBind, mBindQrUrl);
		}

		// 这个广播调用因为年代久远已经不清楚具体是谁出于什么需求考虑加的了, 为兼容性保留处理
		JSONBuilder jb = new JSONBuilder();
		jb.put("isBind", bHasBind);
		jb.put("url", mBindQrUrl);
		ServiceManager.getInstance().broadInvoke("wx.qrcode.broadcast",
				jb.toBytes());
	}

	private void doWechatNoContact() {
		if (interceptWeChat()) {
			return;
		}
		if (checkEnabled()) {
			TtsUtil.speakTextOnRecordWin("未找到相关联系人", false, null);
		} else {
			requestLogin();
		}
	}

	/** 拍照后TIME_SHARE_PHOTO内可以分享照片 */
	public final int TIME_SHARE_PHOTO = 6000;

	/**
	 * 拍照后分享图片功能是否开启
	 */
	public boolean isSharePhotoEnabled() {
		if (isWeixinInstalled() && checkEnabled()) {
			return true;
		}
		return false;
	}

	private PhotoFloatView mPhotoFloatView;
	private String mShareUrl;

	public void startSharePhoto(final String url) {
		mShareUrl = url;
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (mPhotoFloatView != null && mPhotoFloatView.isShowing()) {
					mPhotoFloatView.dismiss("new dialog show");
				}

				mPhotoFloatView = new PhotoFloatView(url);
				mPhotoFloatView.show();
			}
		}, 0);
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (mPhotoFloatView != null) {
					mPhotoFloatView.dismiss("time exceed");
				}
			}
		}, 2000);
		String spk = NativeData.getResString("RS_WX_SHARE_PIC");
		TtsManager.getInstance().speakText(spk, new ITtsCallback() {
			@Override
			public void onEnd() {
				AsrUtil.useWakeupAsAsr(new AsrUtil.AsrComplexSelectCallback() {
					@Override
					public String getTaskId() {
						return "WAKEUP_SHARE";
					}

					@Override
					public boolean needAsrState() {
						return true;
					}

					@Override
					public void onCommandSelected(String type, String command) {
						AppLogicBase
								.removeBackGroundCallback(mReleaseWakeupShare);
						mReleaseWakeupShare.run();
						procSharePhotoSence();
					}
				}.addCommand("SHARE", "分享照片"));
				AppLogicBase.removeBackGroundCallback(mReleaseWakeupShare);
				AppLogicBase.runOnBackGround(mReleaseWakeupShare,
						TIME_SHARE_PHOTO);
			}
		});
	}

	public void procSendMsgSence() {
		mWeChatChoice = ON_CALL;
		RecorderWin.show();
		String spk = NativeData.getResString("RS_WX_SEARCH_CONVERSATION");
		RecorderWin.addSystemMsg(spk);
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
		requestRecentSession();
	}

	public void procSharePhotoSence() {
		mWeChatChoice = ON_PHOTO;
		RecorderWin.show();
		String spk = NativeData.getResString("RS_WX_SEARCH_CONVERSATION");
		RecorderWin.addSystemMsg(spk);
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
		requestRecentSession();
	}

	static Runnable mReleaseWakeupShare = new Runnable() {
		@Override
		public void run() {
			AsrUtil.recoverWakeupFromAsr("WAKEUP_SHARE");
		}
	};

	
	/**
	 * 抽帧上传照片
	 * @param data
	 */
	public void batchCapturePhoto(byte[] data){
		try {
			final UiEquipment.Notify_BatchUploadPic notify_batchUploadPic = UiEquipment.Notify_BatchUploadPic.parseFrom(data);
			long times[];
			for(UiEquipment.BatchUploadPicRange ranges: notify_batchUploadPic.rptMsgRanges) {
				LogUtil.logd("startTime:" + ranges.uint32BeginTime + " ; endTime:"
						+ ranges.uint32EndTime + " ; interval:" + ranges.uint32Interval
						+ "; taskId: " + ranges.uint64TaskId);
				times = new long[(ranges.uint32EndTime-ranges.uint32BeginTime)/ranges.uint32Interval];
				for (int i = ranges.uint32BeginTime, j = 0; i < ranges.uint32EndTime ;i += ranges.uint32Interval , j++){
					times[j] = (long)i * 1000l;
				}
				doBatchCapturePhoto(times,notify_batchUploadPic.uint64PushMid,notify_batchUploadPic.uint32Type,notify_batchUploadPic.uint64Timeout,ranges.uint64TaskId);
			}

		} catch (InvalidProtocolBufferNanoException e) {
			LogUtil.loge("batch upload pic InvalidProtocolBufferNanoException");
		} catch (Exception e) {
			LogUtil.loge("batch upload pic otherException");
		}

	}

	private void doBatchCapturePhoto(long[] times, final long pushMid, final int type, long timeOut, final long taskId){
		DVRScaner.getInstance().getPictureAtTime(times, new DVRScaner.CaptureProcessor() {
			@Override
			public void onCapture(DVRScaner.CaptureResult[] res) {
				UiEquipment.Req_BatchUploadPic reqBatchUploadPic = new UiEquipment.Req_BatchUploadPic();
				reqBatchUploadPic.uint64PushMid = pushMid;
				reqBatchUploadPic.rptUploadPic = new UiEquipment.Req_UploadPic[res.length];
				DVRScaner.CaptureResult result;
				for ( int i = 0 ; i < res.length ; i++) {
					result = res[i];
					UiEquipment.Req_UploadPic req = new UiEquipment.Req_UploadPic();
					req.uint64PushMid = pushMid;
					req.uint32RealTime = (int)(result.time/1000);
					req.uint32Type = type;
					req.uint64TaskId = taskId;
					LogUtil.logd("batchCapturePhoto errorCode:" + result.err);
					if (result.err == DVRScaner.ERROR_SUCCESS && result.picFile != null){
						LogUtil.logd("path :" + result.picFile.getAbsolutePath());
						req.strPicPath = result.picFile.getAbsolutePath();

					} else {
						req.uint32ErrCode = UiEquipment.EC_UPLOAD_PIC_UNKNOWN;
						switch (result.err){
							case DVRScaner.ERROR_DECODE:break;
							case DVRScaner.ERROR_NOT_PROC:break;
							case DVRScaner.ERROR_NOT_FOUND:
								req.uint32ErrCode = UiEquipment.EC_UPLOAD_PIC_NOT_FOUND;
								break;
							case DVRScaner.ERROR_TIMEOUT:
								req.uint32ErrCode = UiEquipment.EC_UPLOAD_PIC_CATCH_TIMEOUT;
								break;
						}
					}
					reqBatchUploadPic.rptUploadPic[i] = req;
				}
				JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,UiEquipment.SUBEVENT_REQ_BATCH_UPLOAD_PIC,reqBatchUploadPic);
			}
		},timeOut);
	}
	
	// 根据时间差转换成友好提示
	// ms : 时间毫秒数
	private String makeFriendlyTime(long ms) {
		int minute = Math.round((System.currentTimeMillis() - ms)
				/ (60 * 1000f));
		if (minute < 5) {
			return "";
		} else if (minute <= 10) {
			return minute + "分钟前";
		} else if (minute < 60) {
			return Math.round(minute / 10f) * 10 + "分钟前";
		} else if (minute < 60 * 24) {
			return minute / 60 + "小时前";
		} else if (minute < 60 * 24 * 7) {
			return minute / 60 / 24 + "天";
		}
		return "";
	}

	class NavigateConfirmData {
		Runnable run;
		NavigateInfo info;
	}

	private long mDismissDelay;

	public void setConfirmDismissDelay(long delay) {
		this.mDismissDelay = delay;
	}

	Runnable mDismissTask = new Runnable() {

		@Override
		public void run() {
			isInQuickNav = false;
			if (mWinConfirmNavigate != null) {
				mWinConfirmNavigate.dismiss("dismiss");
			}
		}
	};

	public void dismissConfirm(){
		AppLogic.runOnUiGround(mDismissTask, 0);
	}

	void checkDismissConfirm() {
		AppLogic.removeUiGroundCallback(mDismissTask);
		if (mDismissDelay >= 1000) {
			AppLogic.runOnUiGround(mDismissTask, mDismissDelay);
		}
	}

	void multiNavigateConfirm(String message, String hint, Runnable run,
			final NavigateInfo info) {
		if (mWinConfirmNavigate != null) {
			mWinConfirmNavigate.dismiss("dismiss last dialog");
		}

		WinConfirmAsr.WinConfirmAsrBuildData buildData = new WinConfirmAsr.WinConfirmAsrBuildData();
		buildData.setMessageText(message);
		buildData.setSureText("加入", new String[] { "加入", "导航", "确定", "开始" });
		buildData.setCancelText("取消", new String[] { "取消", "放弃", "返回" });
		buildData.setHintTts(hint);
		buildData.setSystemDialog(true);
		mWinConfirmNavigate = new WinConfirmAsr(buildData) {
			@Override
			public void onClickOk() {
				NavManager.getInstance().NavigateTo(info, PoiAction.ACTION_NAVI);
			}

			@Override
			public void onSpeakOk() {
				this.dismiss("onSpeakOk");
				AppLogic.removeUiGroundCallback(mDismissTask);
				String spk = NativeData.getResString("RS_WX_PATH_PLAN");
				TtsManager.getInstance().speakText(spk,
						new ITtsCallback() {
					@Override
					public void onSuccess() {
						onClickOk();
					};
				});
			}

			@Override
			protected void onEndTts() {
				super.onEndTts();
				checkDismissConfirm();
			}

			@Override
			public String getReportDialogId() {
				return "wx_multi_nav";
			}
		};
		mWinConfirmNavigate.show();
	}
	
	private PathInfo convertFromNavInfo(NavigateInfo info) {
		if (info == null || info.msgGpsInfo == null) {
			return null;
		}
		PathInfo pathInfo = new PathInfo();
		LocationInfo locationInfo = LocationManager.getInstance().getLastLocation();
		if (locationInfo != null && locationInfo.msgGeoInfo != null && locationInfo.msgGpsInfo != null) {
			String addr = locationInfo.msgGeoInfo.strAddr;
			if (!TextUtils.isEmpty(addr)) {
				pathInfo.fromPoiAddr = addr;
				if(!TextUtils.isEmpty(locationInfo.msgGeoInfo.strProvice)){
					addr = addr.replace(locationInfo.msgGeoInfo.strProvice, "");
				}
				if(!TextUtils.isEmpty(locationInfo.msgGeoInfo.strProvice)){
					addr = addr.replace(locationInfo.msgGeoInfo.strCity, "");
				}
				if(!TextUtils.isEmpty(locationInfo.msgGeoInfo.strProvice)){
					addr = addr.replace(locationInfo.msgGeoInfo.strDistrict, "");
				}
				pathInfo.fromPoiName = addr;
			}
			pathInfo.fromPoiLat = locationInfo.msgGpsInfo.dblLat;
			pathInfo.fromPoiLng = locationInfo.msgGpsInfo.dblLng;
		}

		pathInfo.toCity = info.strTargetCity;
		pathInfo.toPoiName = info.strTargetName;
		pathInfo.toPoiAddr = info.strTargetAddress;
		pathInfo.toPoiLat = info.msgGpsInfo.dblLat;
		pathInfo.toPoiLng = info.msgGpsInfo.dblLng;
		return pathInfo;
	}

	public void navigateConfirm(final String message, final String hint,
			final Runnable run, final NavigateInfo info) {
		// 将地址记住为导航历史
		NavInscriber.getInstance().addRecordFromWx(convertFromNavInfo(info));
		Runnable notifyRun = new Runnable() {
			@Override
			public void run() {
				JSONBuilder builder = new JSONBuilder();
				builder.put("type", "navigate");
				String name = info.strTargetName;
				String addr = info.strTargetAddress;
				String city = info.strTargetCity;
				double lat = info.msgGpsInfo.dblLat;
				double lng = info.msgGpsInfo.dblLng;
				builder.put("name", name);
				builder.put("addr", addr);
				builder.put("city", city);
				builder.put("lat", lat);
				builder.put("lng", lng);
				builder.put("msg",message);
				if (SenceManager.getInstance().noneedProcSence("wechat", builder.toString().getBytes())) {
					return;
				}
				if (mWinConfirmNavigate != null) {
					mWinConfirmNavigate.dismiss("dismiss last dialog ");
				}
				WinConfirmAsr.WinConfirmAsrBuildData buildData = new WinConfirmAsr.WinConfirmAsrBuildData();
				buildData.setMessageText(message);
				buildData.setSureText("导航", new String[] { "导航", "确定", "开始" });
				buildData.setCancelText("取消", new String[] { "取消", "放弃", "返回" });
				buildData.setHintTts(hint);
				buildData.setSystemDialog(true);
				mWinConfirmNavigate = new WinConfirmAsr(buildData) {
					@Override
					public void onClickOk() {
						isInQuickNav = false;

						if (NetworkManager.getInstance().checkLeastFlow()) {
							String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
							TtsManager.getInstance().speakText(resText, PreemptType.PREEMPT_TYPE_NONE);
						}
						NavManager.getInstance().NavigateTo(info, PoiAction.ACTION_NAVI);
					}

					@Override
					public void onSpeakOk() {
						this.dismiss("onSpeakOk");
						AppLogic.removeUiGroundCallback(mDismissTask);
						String spk = NativeData.getResString("RS_WX_PATH_PLAN");
						TtsManager.getInstance().speakText(spk,
								new ITtsCallback() {
									@Override
									public void onSuccess() {
										onClickOk();
									};
								});
					}

					@Override
					public void onClickCancel() {
						isInQuickNav = false;
						String spk = NativeData.getResString("RS_WX_CANCEL");
//						TtsManager.getInstance().speakText(spk);
						RecorderWin.speakTextWithClose(spk, null);		
					}

					@Override
					protected void onEndTts() {
						super.onEndTts();
						checkDismissConfirm();
					}

					@Override
					public String getReportDialogId() {
						return "wx_navToLoc";
					}
				};
				mWinConfirmNavigate.show();
				isInQuickNav = true;
			}
		};
		mNotifyTask = notifyRun;
		if (waitRunnable()) {
			RecorderWin.addCloseRunnable(mWaitTaskRunnable);
			return;
		}
		notifyRun.run();
	}
	
	private Runnable mNotifyTask;
	
	private Runnable mWaitTaskRunnable = new Runnable() {
		
		@Override
		public void run() {
			if (mNotifyTask != null) {
				mNotifyTask.run();
				mNotifyTask = null;
			}
		}
	};
	
	/**
	 * 取消当前选择框
	 */
	public void cancelDialog() {
		if (mWinConfirmNavigate != null && mWinConfirmNavigate.isShowing()) {
			mWinConfirmNavigate.dismiss("dismiss by other");
		}
	}
	
	private boolean waitRunnable() {
		return RecorderWin.isNotifyRecordShow;
	}

	Thread mThreadGetLocation;

	public static final int OFF_LINE = 0;
	public static final int ON_UPDATING_USER = 1;
	public static final int ON_LINE = 2;
	public static final int ON_INIT = 0;
	public static final int ON_CALL = 1;
	public static final int ON_SHIELD = 2;
	public static final int ON_PLACE = 3;
	public static final int ON_HISTORY = 4;
	public static final int ON_PHOTO = 5;
	public static final int ON_UNSHILED = 6;
	public static final int ON_EXPRESSION = 7;
	public static final int ON_SHARE_POI = 8;

	public int mWeChatState = OFF_LINE;
	public int mWeChatChoice = ON_INIT;
	public String mExpression = null;
	public String mExpressionValue = null;

	private void launchWebChat() {
		// 默认按getLaunchIntentForPackage方式启动
		final Intent intent = GlobalContext.get().getPackageManager()
				.getLaunchIntentForPackage("com.txznet.webchat");
		if (intent == null) {
			String spk = NativeData.getResString("RS_WX_NOT_INSTALL");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}

		String spk = NativeData.getResString("RS_WX_WILL_OPEN");
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(true);
		RecorderWin.speakTextWithClose(spk, new Runnable() {
			@Override
			public void run() {
				launchWebchatByVersion(intent);
			}
		});
		
		if(NetworkManager.getInstance().checkLeastFlow()){
			String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
			TtsManager.getInstance().speakText(resText, PreemptType.PREEMPT_TYPE_NONE);
		}
		
	}

	private void launchWebchatByVersion(Intent intent) {
		if (getWebchatVersionCode() < 11) {
			performLaunchWebchat(intent);
		} else {
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wechat.ctrl.launch", null, null);
		}
	}

	private void performLaunchWebchat(Intent intent) {
		if (intent != null) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			// 添加标志位，登录成功后退出微信界面
			intent.putExtra("quitAfterLogin", true);
			GlobalContext.get().startActivity(intent);
			return;
		}
	}
	
	private int getWebchatVersionCode() {
		PackageInfo info = null;

		try {
			info = GlobalContext.get().getPackageManager()
					.getPackageInfo(WEBCHAT_PACKAGE_NAME, 0);

			return info.versionCode;
		} catch (Exception e) {
			JNIHelper.logw("WeixinManger::get webchat version encoutnered error: "
					+ e.toString());
		}

		return 0;
	}

	private void setLoginState(int state) {
		mWeChatState = state;
		if (state == 0) {
			mLastWeChatContacts = null;
		}
	}

	public boolean checkEnabled() {
		return mWeChatState == ON_LINE;
	}

	private void doWeChatMakeSession(final int subEventId, byte[] data) {
		mWeChatChoice = ON_INIT;
		boolean isSubEvent = true;
		switch (subEventId) {
		case UiMakeWechatSession.SUBEVENT_MAKE_UNSHIELD_DIRECT:
		case UiMakeWechatSession.SUBEVENT_MAKE_UNSHIELD_INDIRECT:
			if (isSubEvent) {
				mWeChatChoice = ON_UNSHILED;
				isSubEvent = false;
			}
		case UiMakeWechatSession.SUBEVENT_MAKE_HISTORY_DIRECT:
		case UiMakeWechatSession.SUBEVENT_MAKE_HISTORY_INDIRECT:
			if (isSubEvent) {
				mWeChatChoice = ON_HISTORY;
				isSubEvent = false;
			}
		case UiMakeWechatSession.SUBEVENT_MAKE_PLACE_DIRECT:
		case UiMakeWechatSession.SUBEVENT_MAKE_PLACE_INDIRECT:
			if (isSubEvent) {
				mWeChatChoice = ON_PLACE;
				isSubEvent = false;
			}
		case UiMakeWechatSession.SUBEVENT_MAKE_SHIELD_DIRECT:
		case UiMakeWechatSession.SUBEVENT_MAKE_SHIELD_INDIRECT:
			if (isSubEvent) {
				mWeChatChoice = ON_SHIELD;
				isSubEvent = false;
			}
		case UiMakeWechatSession.SUBEVENT_MAKE_SESSION_DIRECT:
		case UiMakeWechatSession.SUBEVENT_MAKE_SESSION_INDIRECT:
			if (isSubEvent) {
				mWeChatChoice = ON_CALL;
				isSubEvent = false;
			}
			JNIHelper.logd("mWeChoice=" + mWeChatChoice);
			if (interceptWeChat()) {
				return;
			}
			if (!checkEnabled()) {
				requestLogin();
				return;
			}
			try {
				final WeChatContacts targets = WeChatContacts.parseFrom(data);
				mExpression = targets.expression;
				if (!TextUtils.isEmpty(targets.expression)) {
					mExpressionValue = mExpressionMap.get(targets.expression);
					if (!TextUtils.isEmpty(mExpressionValue)) {
						if (getWebchatVersionCode() > 13) { // 1.1.3版本以上才支持发表情
							mWeChatChoice = ON_EXPRESSION;
						} else {
							mWeChatChoice = ON_INIT;
							String hint = NativeData.getResString("RS_WX_HINT_UNSUPPORT_EXPRESSION_VERSION");
							RecorderWin.speakTextWithClose(hint, null);
							return;
						}
					} else {
						mWeChatChoice = ON_INIT;
						String hint = "";
						if (targets.expression.contains("个")) {
							hint = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
						} else {
							hint = NativeData.getResString("RS_VOICE_WECHAT_NOT_EXPRESSION").replace("%EXPRESSION%",
									targets.expression);
						}
						RecorderWin.speakTextWithClose(hint, null);
						return;
					}
				}
				
				JNIHelper.logd("Wechat::Choice::" + mWeChatChoice
						+ " Expression:" + mExpression + " value:" + mExpressionValue);
				AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
				if (targets.cons != null && targets.cons.length != 0) {

					AppLogic.runOnBackGround(new Runnable() {

						@Override
						public void run() {
							ChoiceManager.getInstance().showWxList(subEventId, targets, "");
						}
					}, 50);
				} else {
					// 若是解除屏蔽，请求屏蔽列表
					if (mWeChatChoice == ON_UNSHILED) {
						requestMaskedSession();
					} else {
						requestRecentSession();
					}
				}
			} catch (Exception e) {
				if (mWeChatChoice == ON_UNSHILED) {
					requestMaskedSession();
				} else {
					requestRecentSession();
				}
				e.printStackTrace();
			}

			break;
		default:
			break;
		}
	}

	public boolean isWeixinInstalled() {
		PackageInfo packageInfo = null;
		try {
			packageInfo = GlobalContext.get().getPackageManager()
					.getPackageInfo("com.txznet.webchat", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return packageInfo == null ? false : true;
	}

	public void requestLogin() {
		PackageInfo packageInfo = null;
		try {
			packageInfo = GlobalContext.get().getPackageManager()
					.getPackageInfo("com.txznet.webchat", 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		if (packageInfo == null) {
			String spk = NativeData.getResString("RS_WX_NOT_INSTALL");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if (packageInfo.versionCode <= 9) {
			AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
			String spk = NativeData.getResString("RS_WX_NOT_LOGIN");
			RecorderWin.open(spk);
		} else if (packageInfo.versionCode <= 10) {
			String spk = NativeData.getResString("RS_WX_NOT_LOGIN_CODE");
			TtsManager.getInstance().speakText(spk);
			JNIHelper.logi("send wx.contact.need.login");
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.contact.need.login", null, null);
			RecorderWin.dismiss();
		} else {
			if(NetworkManager.getInstance().checkLeastFlow()){
				String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
				TtsManager.getInstance().speakText(resText, PreemptType.PREEMPT_TYPE_NONE);
			}
			
			Boolean needTts = true;
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.contact.need.login", needTts.toString().getBytes(),
					null);
			if (WinManager.getInstance().isHudRecordWin()) {
				return;
			}

			RecorderWin.dismiss();
		}
	}


	/*
	 * function:发起微信会话 who:联系人昵称 id:联系人ID
	 */
	public void makeSession(String who, String id) {
		JSONBuilder doc = new JSONBuilder().put("name", who).put("id", id);
		JNIHelper.logd("send Message to weixin:" + mWeChatChoice);
		switch (mWeChatChoice) {
		case ON_SHIELD:
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.mask", doc.toString().getBytes(), null);
			break;
		case ON_PLACE:
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.sharePlace", doc.toString().getBytes(), null);
			break;
		case ON_HISTORY:
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.history", doc.toString().getBytes(), null);
			break;
		case ON_PHOTO:
			doc.put("data", mShareUrl);
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.sharePhoto", doc.toString().getBytes(), null);
			break;
		case ON_UNSHILED:
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.unmask", doc.toString().getBytes(), null);
			break;
		case ON_EXPRESSION:
			doc.put("data", mExpressionValue);
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.expression", doc.toString().getBytes(), null);
			break;
		case ON_SHARE_POI:
			Poi poi = NavManager.getInstance().mSharePoi;
			if (poi != null) {
				double lat = poi.getLat() * 1.0D;
				double lng = poi.getLng() * 1.0D;
				String addr = poi.getGeoinfo();
				doc.put("lat", lat);
				doc.put("lng", lng);
				doc.put("addr", addr);
			}
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.session.share_poi",
					doc.toString().getBytes(), null);
			break;
		default:
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.session.make", doc.toString().getBytes(), null);
			break;
		}
		mWeChatChoice = ON_INIT;
	}

	/*
	 * 更新微信客户端登陆状态 status: 0:offline 2:online
	 */
	public void updateLoginState(String comm, byte[] data) {
		JNIHelper.logd("" + comm);
		if (data == null) {
			JNIHelper.loge("data == null");
			return;
		}

		if (!comm.equals("wx.loginstate.update")) {
			return;
		}

		String jsonStr = new String(data);
		try {
			JSONObject json = new JSONObject(jsonStr);
			int state = json.getInt("status");
			setLoginState(state);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/*
	 * 更新微信联系人 comm : wx.contact.update
	 */
	public void updateWeChatContact(String comm, byte[] data) {
		JNIHelper.logd("" + comm);
		if (data == null) {
			JNIHelper.loge("data == null");
			return;
		}
		if (comm.equals("wx.contact.update")) {
			String jsonStr = new String(data);
			updateWechatContact(jsonStr);
		}
	}
	
	private void updateWechatContact(String jsonStr) {
		WeChatContacts weChatContacts = null;
		try {
			weChatContacts = new WeChatContacts();

			JSONObject json = null;
			JSONArray jsonArray = null;
			do {
				json = new JSONObject(jsonStr);
				jsonArray = json.getJSONArray("list");

				if (null == jsonArray) {
					break;
				}

				int length = jsonArray.length();
				if (length <= 0) {
					break;
				}

				weChatContacts.cons = new WeChatContact[length];

				for (int i = 0; i < jsonArray.length(); ++i) {
					JSONObject jsonObject = (JSONObject) jsonArray.get(i);
					if (jsonObject == null) {
						continue;
					}

					WeChatContact con = new WeChatContact();
					con.id = jsonObject.getString("id");
					con.name = jsonObject.getString("name");
					try {
						con.uint32Type = jsonObject.getInt("type");
					} catch (JSONException e) {
						con.uint32Type = WechatContactData.TYPE_FRIEND;
					}
					weChatContacts.cons[i] = con;
				}

				JNIHelper
						.sendEvent(
								UiEvent.EVENT_WECHAT_FRIENDSHIP,
								WechatContactData.SUBEVENT_UPDATED_WECHAT_CONTACT_LIST,
								weChatContacts);

			} while (false);

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 用于缓存上一次同步的微信联系人列表
	 */
	private WeChatContacts mLastWeChatContacts = null;

	/*
	 * 更新微信最近会话 comm : "wx.contact.recentsession.update"
	 */
	public synchronized void updateWeChatRecentChat(String comm, byte[] data) {
		JNIHelper.logd("" + comm);
		if (data == null) {
			JNIHelper.loge("data == null");
			return;
		}
		if (mQuestTimeOutTask == null) {
			return;
		}
		if (comm.equals("wx.contact.recentsession.update")) {
			AppLogic.removeBackGroundCallback(mQuestTimeOutTask);
			mQuestTimeOutTask = null;

			WeChatContacts weChatContacts = null;
			String jsonStr = new String(data);
			try {
				weChatContacts = new WeChatContacts();

				JSONObject json = null;
				JSONArray jsonArray = null;
				do {
					json = new JSONObject(jsonStr);
					jsonArray = json.getJSONArray("list");

					if (null == jsonArray) {
						break;
					}

					int length = jsonArray.length();
					if (length <= 0) {
						break;
					}

					weChatContacts.cons = new WeChatContact[length];

					for (int i = 0; i < jsonArray.length(); ++i) {
						JSONObject jsonObject = (JSONObject) jsonArray.get(i);
						if (jsonObject == null) {
							continue;
						}

						WeChatContact con = new WeChatContact();
						con.id = jsonObject.getString("id");
						con.name = jsonObject.getString("name");
						weChatContacts.cons[i] = con;
					}
					mLastWeChatContacts = weChatContacts;

					String strText = NativeData.getResString("RS_WX_SELECT_LIST_SPK").replace("%COUNT%", length + "");

					/*if (ON_CALL == mWeChatChoice || ON_PHOTO == mWeChatChoice || ON_PLACE == mWeChatChoice) {
						SelectorHelper
								.entryWxContactSelector(
										UiMakeWechatSession.SUBEVENT_MAKE_SESSION_INDIRECT,
										weChatContacts, strText);
					} else if (ON_SHIELD == mWeChatChoice) {
						SelectorHelper
								.entryWxContactSelector(
										UiMakeWechatSession.SUBEVENT_MAKE_SHIELD_INDIRECT,
										weChatContacts, strText);
					}*/
					
					if (ON_SHIELD == mWeChatChoice) {
						ChoiceManager.getInstance().showWxList(
										UiMakeWechatSession.SUBEVENT_MAKE_SHIELD_INDIRECT,
										weChatContacts, strText);
					} else {
						ChoiceManager.getInstance().showWxList(
										UiMakeWechatSession.SUBEVENT_MAKE_SESSION_INDIRECT,
										weChatContacts, strText);
					}

					return;
				} while (false);
				String spk = NativeData
						.getResString("RS_WX_CONTACTS_NOT_FOUND");
				RecorderWin.speakTextWithClose(spk, null);

			} catch (JSONException e) {
				e.printStackTrace();
				String spk = NativeData
						.getResString("RS_WX_CONTACTS_SYNC_FAIL");
				RecorderWin.speakTextWithClose(spk, null);
			}
		}

	}

	/*
	 * 更新微信屏蔽列表 comm : "wx.contact.maskedsession.update"
	 */
	public synchronized void updateWeChatMaskedChat(String comm, byte[] data) {
		JNIHelper.logd("" + comm);
		if (data == null) {
			JNIHelper.loge("data == null");
			return;
		}
		if (mQuestTimeOutTask == null) {
			return;
		}
		if (comm.equals("wx.contact.maskedsession.update")) {
			AppLogic.removeBackGroundCallback(mQuestTimeOutTask);
			mQuestTimeOutTask = null;

			WeChatContacts weChatContacts = null;
			String jsonStr = new String(data);
			try {
				weChatContacts = new WeChatContacts();

				JSONObject json = null;
				JSONArray jsonArray = null;
				do {
					json = new JSONObject(jsonStr);
					jsonArray = json.getJSONArray("list");

					if (null == jsonArray) {
						break;
					}

					int length = jsonArray.length();
					if (length <= 0) {
						break;
					}

					weChatContacts.cons = new WeChatContact[length];

					for (int i = 0; i < jsonArray.length(); ++i) {
						JSONObject jsonObject = (JSONObject) jsonArray.get(i);
						if (jsonObject == null) {
							continue;
						}

						WeChatContact con = new WeChatContact();
						con.id = jsonObject.getString("id");
						con.name = jsonObject.getString("name");
						weChatContacts.cons[i] = con;
					}

					String strText = NativeData
							.getResString("RS_WX_SELECT_SHILED_SPK");
					ChoiceManager.getInstance().showWxList(
							UiMakeWechatSession.SUBEVENT_MAKE_UNSHIELD_DIRECT,
							weChatContacts, strText);
					return;
				} while (false);
				mWeChatChoice = ON_INIT;
				String spk = NativeData
						.getResString("RS_WX_NOT_SHIELD_CONTACTS");
				RecorderWin.speakTextWithClose(spk, null);

			} catch (JSONException e) {
				e.printStackTrace();
				mWeChatChoice = ON_INIT;
				String spk = NativeData.getResString("RS_WX_SHIELD_SYNC_FAIL");
				RecorderWin.speakTextWithClose(spk, null);
			}
		}

	}

	private Runnable mQuestTimeOutTask = null;

	/*
	 * 更新微信最近会话 comm : "wx.contact.recentsession.request"
	 */
	public synchronized void requestRecentSession() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
				"wx.contact.recentsession.request", null, null);
		mQuestTimeOutTask = new Runnable() {
			@Override
			public void run() {
				questTimeOut();
			};
		};
		 AppLogic.runOnBackGround(mQuestTimeOutTask, 2000);
	}
	
	public synchronized void requestRecentSessionForMaskByVersion() {
		if (getWebchatVersionCode() < 18) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.contact.recentsession.request", null, null);
		} else {
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
					"wx.contact.session_for_mask.request", null, null);
		}
		
		mQuestTimeOutTask = new Runnable() {
			@Override
			public void run() {
				questTimeOut();
			};
		};
		AppLogic.runOnBackGround(mQuestTimeOutTask, 2000);
	}

	/*
	 * 更新微信屏蔽会话 comm : "wx.contact.maskedsession.request"
	 */
	public synchronized void requestMaskedSession() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
				"wx.contact.maskedsession.request", null, null);
		mQuestTimeOutTask = new Runnable() {
			@Override
			public void run() {
				questTimeOut();
			};
		};
		AppLogic.runOnBackGround(mQuestTimeOutTask, 2000);
	}

	private synchronized void questTimeOut() {
		if (mQuestTimeOutTask == null) {
			return;
		}
		mQuestTimeOutTask = null;

		if (mLastWeChatContacts == null) {
			String spk = NativeData.getResString("RS_WX_CONTACTS_SYNC_TIMEOUT");
			RecorderWin.speakTextWithClose(spk, null);
		} else {
			String strText = NativeData.getResString("RS_WX_SELECT_LIST_SPK");
			if (mLastWeChatContacts != null && mLastWeChatContacts.cons != null
					&& mLastWeChatContacts.cons.length > 0) {
				strText = strText.replace("%COUNT%", mLastWeChatContacts.cons.length + "");
			}

			if (ON_CALL == mWeChatChoice) {
				ChoiceManager.getInstance().showWxList(
						UiMakeWechatSession.SUBEVENT_MAKE_SESSION_INDIRECT,
						mLastWeChatContacts, strText);
			} else if (ON_SHIELD == mWeChatChoice) {
				mWeChatChoice = ON_SHIELD;
				ChoiceManager.getInstance().showWxList(
						UiMakeWechatSession.SUBEVENT_MAKE_SESSION_INDIRECT,
						mLastWeChatContacts, strText);
			}
		}
	}

	public void addOnQrCodeListener(OnQRCodeListener listener) {
		if (listener == null) {
			return;
		}

		if (mOnQRCodeListeners.contains(listener)) {
			return;
		}

		mOnQRCodeListeners.add(listener);
	}

	public void onRemoveQrCodeListener(OnQRCodeListener listener) {
		if (listener == null) {
			return;
		}

		if (mOnQRCodeListeners.contains(listener)) {
			mOnQRCodeListeners.remove(listener);
		}
	}

	public interface OnQRCodeListener {
		public void onGetQrCode(boolean isBind, String url);
	}

	public void doGetWechatLoginState() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT,
				"wx.state.login.req", null, null);
	}

	public void doReportWxchatLoginState(byte[] data) {
		try {
			JSONBuilder doc = new JSONBuilder(data);
			EquipmentManager.Req_WeChatLoginState state = new EquipmentManager.Req_WeChatLoginState();
			state.bLogin = doc.getVal("status", Integer.class) == 2;
			if (state.bLogin) {
				state.strNick = doc.getVal("nick", String.class);
				state.uint32LoginTime = doc.getVal("loginTime", Integer.class);
			} else {
				state.strCodeData = doc.getVal("code", String.class).getBytes();
			}
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_WECHAT,
					UiWechat.SUBEVENT_NOTIFY_WECHAT_LOGIN_STATE, state);
			JNIHelper.logd("report_login_state:bLogin=" + state.bLogin
					+ ", nick=" + state.strNick + ", code="
					+ doc.getVal("code", String.class));
		} catch (Exception e) {

		}
	}

	/**
	 * 默认关闭微信
	 */
	private boolean mEnableWechat = false;
	/**
	 * 判断是否启用微信的语义和界面展示
	 * @return
	 */
	public boolean enableWeChat(){
		return mEnableWechat;
	}

	/**
	 * 拦截微信处理的逻辑
	 * @return
	 */
	public boolean interceptWeChat(){
		boolean intercept = !enableWeChat();
		if (intercept) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_WITH_BYE"),null);
		}
		return intercept;
	}
}
