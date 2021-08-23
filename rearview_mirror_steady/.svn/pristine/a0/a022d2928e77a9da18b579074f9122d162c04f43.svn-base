package com.txznet.txz.module.version;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Environment;
import android.os.SystemClock;
import android.text.TextUtils;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.PluginInfo;
import com.txz.ui.equipment.UiEquipment.Rep_PluginList;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.PluginUtil;
import com.txznet.comm.ui.dialog2.WinNotice;
import com.txznet.loader.AppLogic;
import com.txznet.record.ui.WinRecord;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.mix.AsrEngineController;
import com.txznet.txz.component.asr.mix.AsrLocalEngineController;
import com.txznet.txz.component.asr.mix.VoicePreProcessorController;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.netdata.NetDataManager;
import com.txznet.txz.module.offlinepromote.OfflinePromoteManager;
import com.txznet.txz.module.qrcode.QrCodeManager;
import com.txznet.txz.module.text.TextManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ttsplayer.TtsPlayerManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.userconf.UserConf;
import com.txznet.txz.module.voiceprintrecognition.VoiceprintRecognitionManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.plugin.PluginLoader;
import com.txznet.txz.ui.widget.SDKFloatView;
import com.txznet.txz.ui.win.help.WinHelpManager;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.recordcenter.TXZAudioTrack;
import com.txznet.txz.util.recordcenter.TXZSourceRecorderManager;

/**
 * 版本管理模块，负责版本数据管理，版本更新
 * 
 * @author User
 *
 */
public class LicenseManager extends IModule {
	static LicenseManager sModuleInstance = new LicenseManager();

	private LicenseManager() {

	}

	public static LicenseManager getInstance() {
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	private boolean mIsInitProcessing = false;
	private String mInitError = "语音引擎正在初始化...";
	WinNotice mWinErrorTips = null;
	private boolean mIsInitLogin = false;
	private boolean mIsInitSuccess = false;
	private Boolean mIsCheckOver = false;
	private Boolean mLocalActivated = false;
	
	public boolean isInitLogin() {
		return mIsInitLogin;
	}
	
	
	public void initSuccessCheck() {
		synchronized (mIsCheckOver) {
			mIsInitSuccess = true;
			if (mIsInitSuccess && mIsInitLogin && !mIsCheckOver) {
				JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_INIT_SUCCESS);
				mIsCheckOver = true;
			}
		}
	}

	public void loginSuccessCheck() {
		synchronized (mIsCheckOver) {
			mIsInitLogin = true;
			if (mIsInitLogin && mIsInitSuccess && !mIsCheckOver) {
				JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_INIT_SUCCESS);
				mIsCheckOver = true;
			}
		}
	}
	
	public boolean checkInited() {
		if (mInitError == null) {
			return true;
		}
		
		if (mLocalActivated){
			return true;
		}

		// if (mWinErrorTips == null) {
		// AppLogic.runOnUiGround(new Runnable() {
		// @Override
		// public void run() {
		// mWinErrorTips = new WinNotice(true) {
		//
		// @Override
		// public void onClickOk() {
		//
		// }
		// }.setMessage(mInitError);
		// mWinErrorTips.setOnDismissListener(new OnDismissListener() {
		//
		// @Override
		// public void onDismiss(DialogInterface dialog) {
		// mWinErrorTips = null;
		// }
		// });
		// mWinErrorTips.show();
		// }
		// }, 0);
		// }

		return false;
	}

	String mAppId;

	public String getAppId() {
		return mAppId;
	}

	String mAppCustomId = null;

	public String getAppCustomId() {
		return mAppCustomId;
	}

	public void initSDK(String serviceRemote, final String appId, String appToken,
			String appCustomId) {
		try {
			JNIHelper.logd(serviceRemote + " init SDK appId="
					+ appId.substring(0, 3) + "......"
					+ appId.substring(appId.length() - 3, appId.length())
					+ ", mIsInitProcessing=" + mIsInitProcessing
					+ ", appCustomId = " + appCustomId);
			if (mAppId != null && appId.equals(mAppId) == false) {
				JNIHelper
						.loge("!!!!!!!!!!!!!!!!!set different appId, checkApps");
			}
		} catch (Exception e) {
		}
		if (mIsInitProcessing)
			return;
		mAppId = appId;
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				//存文件
				FileOutputStream fos=null;
				try {
					File appidFile=new File(Environment.getExternalStorageDirectory(),"/txz/appid");
					fos=new FileOutputStream(appidFile);
					fos.write(MD5Util.generateMD5(appId+"Zodj6gI8E9WjiJLBxISqnHkU4ttSlxYx").getBytes());
					fos.flush();
				} catch (Exception e) {
					e.printStackTrace();
				}finally {
					if (fos!=null) {
						try {
							fos.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		},"w_AppIDThread").start();
		mAppCustomId = appCustomId;
		UiEquipment.Req_License pbReqLicense = new UiEquipment.Req_License();
		pbReqLicense.strTxzAppId = appId;
		pbReqLicense.strTxzAppToken = appToken;
		pbReqLicense.strTxzAppCustomId = appCustomId;

		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_REQ_LICENSE, pbReqLicense);
		mIsInitProcessing = true;
	}

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_RESP_LICENSE);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_RESP_LOCAL_VOICE_ACTIVATE);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_ACTIVITED_WITH_NET);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_NOT_OFFICIAL_ENV);
		return super.initialize_BeforeStartJni();
	}

	WinNotice mWinNoticeInitFail = null;

	private class TXZInitThread extends Thread {
		public TXZInitThread() {
			super.setPriority(Thread.MAX_PRIORITY);
		}
	}
	
	public void initTtsComponent(){
		new TXZInitThread() {
			@Override
			public void run() {
				TtsManager.getInstance().initializeComponent();
				TtsPlayerManager.getInstance().initializeComponent();
			};
		}.start();
	}
	
	public void initAsrComponent(){
		new TXZInitThread() {
			@Override
			public void run() {
				AsrManager.getInstance().initializeComponent();
			};
		}.start();
	}
	
	public void initWakeupComponent(){
		new TXZInitThread() {
			@Override
			public void run() {
				WakeupManager.getInstance().initializeComponent();
			};
		}.start();
	}

	private boolean isInitUiProcessing = false;
	public void initUIComponent() {
		new TXZInitThread() {
			@Override
			public void run() {
				if (isInitUiProcessing) {
					return;
				}
				isInitUiProcessing = true;
				WinManager.getInstance().initializeUI2Component();
				WinManager.getInstance().initializeWinConfig();
			}
		}.start();
	}
	
	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_ACTION_EQUIPMENT: {
			if (UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE == subEventId) {
				try {
					LogUtil.d("LicenseManager KEY_OFFICIAL_OFFLINE_QR_CODE " +
							TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_OFFICIAL_OFFLINE_QR_CODE, true));
					if (TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_OFFICIAL_OFFLINE_QR_CODE, true)) {
						QrCodeManager.getInstance().initOfficialOfflineQrCode();
					} else {
						QrCodeManager.getInstance().initNoOfficialOfflineQrCode();
					}
					UiEquipment.ServerConfig pbServerConfig = UiEquipment.ServerConfig
							.parseFrom(data);
					if (null != pbServerConfig.bRecognOnline) {
						ProjectCfg
								.setRecognOnline(pbServerConfig.bRecognOnline);
					}
					TtsManager.sShowTTSThemeQr = pbServerConfig.bQrcodeTts == null ? false : pbServerConfig.bQrcodeTts;
					WakeupManager.getInstance().updateInstantWakeupKeywords(pbServerConfig.strInstantWakeupWords);
				} catch (InvalidProtocolBufferNanoException e) {
				}
				break;
			}
			if (UiEquipment.SUBEVENT_NOTIFY_ACTIVITED_WITH_NET == subEventId) {
				ServiceManager.getInstance().broadInvoke("sdk.init.actived",
						null);
				HelpGuideManager.getInstance().onLicenseActive();
				OfflinePromoteManager.getInstance().onLicenseActive();
				break;
			}
			if (UiEquipment.SUBEVENT_RESP_LICENSE == subEventId 
					|| UiEquipment.SUBEVENT_RESP_LOCAL_VOICE_ACTIVATE == subEventId) {
				try {
					UiEquipment.Resp_License pbRespLicense = UiEquipment.Resp_License
							.parseFrom(data);
					TXZAudioTrack.initTime = SystemClock.elapsedRealtime();
					if (pbRespLicense.bOk) {
						if (UiEquipment.SUBEVENT_RESP_LOCAL_VOICE_ACTIVATE == subEventId){
							mLocalActivated = true;
						}
						if(TextUtils.isEmpty( PreferenceUtil.getInstance().getDeviceActiveTime())){
							OfflinePromoteManager.getInstance().onLicenseActive();
						}
						JNIHelper.logd("license ok.");
						// save license config
						boolean isUseTencent = false;
						if (null != pbRespLicense.msgKeyInfo.strTencentAppId && pbRespLicense.msgKeyInfo.strTencentAppId.length != 0) {
							ProjectCfg.setTencentAppkey(new String(pbRespLicense.msgKeyInfo.strTencentAppId));
							JNIHelper.logd("license setTencentAppkey.");
							isUseTencent = true;
						}
						if (null != pbRespLicense.msgKeyInfo.strTencentAppToken && pbRespLicense.msgKeyInfo.strTencentAppToken.length != 0) {
							ProjectCfg.setTencentToken(new String(pbRespLicense.msgKeyInfo.strTencentAppToken));
							JNIHelper.logd("license setTencentToken.");
							isUseTencent &= true;
						}
						UserConf.getInstance().getFactoryConfigData().mIsUseTencent = isUseTencent;
						UserConf.getInstance().saveFactoryConfigData();
						
						ProjectCfg
								.setIflyAppId(pbRespLicense.msgKeyInfo.strIflyAppId);
						ProjectCfg
								.setYunzhishengAppId(pbRespLicense.msgKeyInfo.strYunzhishengAppId);
						ProjectCfg
								.setYunzhishengSecret(pbRespLicense.msgKeyInfo.strYunzhishengSecrectKey);
						JNIHelper
								.logd("engineType="
										+ pbRespLicense.msgKeyInfo.uint32UseVoiceEngineType
										+ ",backup="
										+ pbRespLicense.msgKeyInfo.uint32BakVoiceEngineType);
						if (pbRespLicense.msgKeyInfo.uint32UseVoiceEngineType != null)
							ProjectCfg
									.setVoiceEngineType(pbRespLicense.msgKeyInfo.uint32UseVoiceEngineType);
						if (pbRespLicense.msgKeyInfo.uint32BakVoiceEngineType != null)
							ProjectCfg
									.setVoiceBakEngineType(pbRespLicense.msgKeyInfo.uint32BakVoiceEngineType);
						if (pbRespLicense.msgKeyInfo.uint32DisableNlpEngineType != null)
							ProjectCfg.setNlpEngineDisableType(pbRespLicense.msgKeyInfo.uint32DisableNlpEngineType);
						if (pbRespLicense.uint64Uid != null)
							ProjectCfg.setUid(pbRespLicense.uint64Uid);
						
						int[] bakEngines = pbRespLicense.msgKeyInfo.uint32BakAsrEngines;
						if (bakEngines != null){
							AsrEngineController.getIntance().updateBakEngines(bakEngines);
						}
						Integer main = pbRespLicense.msgKeyInfo.uint32MainAsrEngine;
						if (main != null){
							AsrEngineController.getIntance().setMainEngine(main);
						}
						
						UiEquipment.AsrEngineParams pbAsrParams = pbRespLicense.msgKeyInfo.msgAsrEngineParams;
						if (pbAsrParams != null){
							AsrEngineController.getIntance().parseAsrEngineParams(pbAsrParams);
						}
						if(null != pbRespLicense.msgKeyInfo.msgLocalAsrEngine && null != pbRespLicense.msgKeyInfo.msgLocalAsrEngine.uitn32EngineType) {
							AsrLocalEngineController.getInstance().setLocalAsrEngineType(pbRespLicense.msgKeyInfo.msgLocalAsrEngine.uitn32EngineType);
						}
						if (null != pbRespLicense.msgKeyInfo.msgPreProcessAecEngine && null != pbRespLicense.msgKeyInfo.msgPreProcessAecEngine.uint32EngineType) {
							VoicePreProcessorController.getInstance().setPreAECType(pbRespLicense.msgKeyInfo.msgPreProcessAecEngine.uint32EngineType);
						}
						if (null != pbRespLicense.msgKeyInfo.msgPreProcessSeEngine && null != pbRespLicense.msgKeyInfo.msgPreProcessSeEngine.uint32EngineType) {
							VoicePreProcessorController.getInstance().setPreSEType(pbRespLicense.msgKeyInfo.msgPreProcessSeEngine.uint32EngineType);
						}

						//云知声V3引擎需要激活码才能初始化
						int yzsSdkVersion = ProjectCfg.YZS_SDK_VERSION;
						JNIHelper.logd("YZS_SDK_VERSION : " + yzsSdkVersion);
						if ( yzsSdkVersion == 3){
							if (pbRespLicense.msgKeyInfo.strYzsV3Activator == null){
								JNIHelper.logw("yzs_v3_sdk need activator to init engine");
								break;
							}
							ProjectCfg.setYzsActivator(pbRespLicense.msgKeyInfo.strYzsV3Activator);
							String strDevSn = "";
							try {
								strDevSn = new String(pbRespLicense.msgKeyInfo.strYzsV3Activator);
							} catch (Exception e) {

							}
							try {
								if (strDevSn.length() > 8) {
									String displayDevSn = "";
									displayDevSn += strDevSn.substring(0, 3);
									displayDevSn += "......";
									displayDevSn += strDevSn.substring(strDevSn.length() - 3, strDevSn.length());
									JNIHelper.logd("yzs_v3_sdk activator : " + displayDevSn);
								}else{
									JNIHelper.logd("yzs_v3_sdk activator too short to display");
								}
							} catch (Exception e) {
								JNIHelper.logd("yzs_v3_sdk activator too short to display");
							}
						}
						
						// license init...
						AppLogic.runOnBackGround(checkInitStateRun,
								DELAY_CHECK_TIME);
						//降噪会影响在线识别率, 所以使用了地平线等引擎的降噪后，识别需要使用预处理之前的数据
						int aecEngineType = VoicePreProcessorController.getInstance().getPreAECType();
						switch(aecEngineType){
							case UiEquipment.VPT_AEC_HRSC:
								//在线识别时，如果不开aec，会影响vad，导致识别时间变长，所以取消这个设置
//								ProjectCfg.setUseSePreprocessedData(false);//降噪会影响在线识别率, 所以使用了地平线的降噪后，识别需要使用预处理之前的数据
								break;
						}
						
						//设置同行者2.0用到的新组件并启动录音。
						JNIHelper.logd("begin start recording");
						TXZSourceRecorderManager.start();
						JNIHelper.logd("end start recording");
						
						initTtsComponent();
						initWakeupComponent();
						initUIComponent();
//						TtsManager.getInstance().initializeComponent();
//						AsrManager.getInstance().initializeComponent();
//						WakeupManager.getInstance().initializeComponent();
						
						TextManager.getInstance().initializeComponent();
						TextResultHandle.getInstance().initializeComponent();
						VoiceprintRecognitionManager.getInstance().initializeComponent();
						
						mInitError = null;

						PluginUtil.loadPluginOnStart("com.txznet.txz",
								new Runnable() {
									@Override
									public void run() {
										List<UiEquipment.PluginInfo> infos = PluginLoader.pluginList;
										if (infos.size() == 0)
											return;
										UiEquipment.Rep_PluginList pluginList = new Rep_PluginList();
										pluginList.rptPluginInfo = new PluginInfo[infos
												.size()];
										for (int i = 0; i < infos.size(); i++) {
											pluginList.rptPluginInfo[i] = infos
													.get(i);
										}
										JNIHelper
												.sendEvent(
														UiEvent.EVENT_ACTION_EQUIPMENT,
														UiEquipment.SUBEVENT_NOTIFY_UPLOAD_PLUGIN_INFO,
														pluginList);
									}
								});
						AppLogic.runOnBackGround(new Runnable() {
							@Override
							public void run() {
								if (mWinNoticeInitFail != null) {
									mWinNoticeInitFail.dismiss("license ok");
									mWinNoticeInitFail = null;
								}
							}
						}, 0);
					} else {
						JNIHelper.loge("license fail:"
								+ pbRespLicense.strErrmsg);

						if (TextUtils.isEmpty(pbRespLicense.strErrmsg) == false
								&& pbRespLicense.strErrmsg.equals(mInitError) == false) {
							mInitError = pbRespLicense.strErrmsg;
							AppLogic.runOnBackGround(new Runnable() {
								@Override
								public void run() {
									if (mWinNoticeInitFail != null) {
										mWinNoticeInitFail.dismiss("license fail");
									}
									WinNotice.WinNoticeBuildData buildData = new WinNotice.WinNoticeBuildData();
									buildData.setMessageText(mInitError).setSystemDialog(true);
									mWinNoticeInitFail = new WinNotice(buildData) {
										@Override
										public void onClickOk() {
											mWinNoticeInitFail = null;
										}

										@Override
										public String getReportDialogId() {
											return "txz_license_fail";
										}
									};
									// mWinNoticeInitFail.show();
								}
							}, 0);
						}

						mInitError = pbRespLicense.strErrmsg;

						// checkInited();
					}

				} catch (InvalidProtocolBufferNanoException e) {
					e.printStackTrace();
				}
				break;
			}

			if (UiEquipment.SUBEVENT_NOTIFY_NOT_OFFICIAL_ENV == subEventId) {
				try {
					SDKFloatView.getInstance().showTestFlag(new String(data));
					ProjectCfg.setTestFlag(new String(data));
					NetDataManager.getInstance().updateFlowInfoCache();
					loginSuccessCheck();
					
					// 登录成功的回调
					TtsManager.getInstance().loginSuccess();
					NavManager.getInstance().loginSuccess();

				} catch (Exception e) {
				}
				break;
			}
		}
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}

	private final static int DELAY_CHECK_TIME = 120000;
	private Runnable checkInitStateRun = new Runnable() {
		@Override
		public void run() {
			checkInitState();
		}
	};

	private void checkInitState() {
		if (TextUtils.isEmpty(ProjectCfg.getYunzhishengAppId())) {
			JNIHelper.logd("WeChat Vesion not need to check init state");
			return;
		}
		boolean bNeededRestart = false;
		int nErrorCode = 0;
		if (!AsrManager.getInstance().isInitSuccessed()) {
			JNIHelper.logw("asr engine init fail, we will remove all asr model data, and restart!!!");
			bNeededRestart = true;
			nErrorCode += 0x0001;
		}
		if (!TtsManager.getInstance().isInitSuccessed()) {
			JNIHelper.logw("tts engine init fail, we will remove all tts model data, and restart!!!");
			bNeededRestart = true;
			nErrorCode += 0x0010;
		}
		if (!WakeupManager.getInstance().isInitSuccessed()) {
			JNIHelper.logw("wakeup engine init fail, we will remove all tts model data, and restart!!!");
			bNeededRestart = true;
			nErrorCode += 0x0100;
		}
		
		if (bNeededRestart) {
			JNIHelper.logw("bNeededRestart = " + bNeededRestart
					+ ", txz will restart!!!");
			JNIHelper.loge(String.format("checkInitState engine init errorCode : 0x%x", nErrorCode));
			//延时3秒重启,便于打印出日志或者以后上报该事件
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					JNIHelper.loge("checkInitState restart process");
					AppLogic.restartProcess();
				}
			}, 3000);
		} else {
			JNIHelper.logd("bNeededRestart = " + bNeededRestart);
		}
	}
}
