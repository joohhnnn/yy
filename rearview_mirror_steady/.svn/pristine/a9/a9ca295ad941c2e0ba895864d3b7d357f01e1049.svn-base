package com.txznet.txz.module.version;

import java.io.File;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.text.TextUtils;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.text.TextManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.widget.SDKFloatView;

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

	public boolean checkInited() {
		if (mInitError == null) {
			return true;
		}

		if (mWinErrorTips == null) {
			mWinErrorTips = new WinNotice(true) {

				@Override
				public void onClickOk() {
					// TODO Auto-generated method stub

				}
			}.setMessage(mInitError);
			mWinErrorTips.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					// TODO Auto-generated method stub
					mWinErrorTips = null;
				}
			});
			mWinErrorTips.show();
		}

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

	public void initSDK(String serviceRemote, String appId, String appToken,
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
				UiEquipment.SUBEVENT_NOTIFY_ACTIVITED_WITH_NET);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_NOT_OFFICIAL_ENV);
		return super.initialize_BeforeStartJni();
	}

	WinNotice mWinNoticeInitFail = null;

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_ACTION_EQUIPMENT: {
			if (UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE == subEventId) {
				try {
					UiEquipment.ServerConfig pbServerConfig = UiEquipment.ServerConfig
							.parseFrom(data);
					if (null != pbServerConfig.bRecognOnline) {
						ProjectCfg
								.setRecognOnline(pbServerConfig.bRecognOnline);
					}
				} catch (InvalidProtocolBufferNanoException e) {
				}
				break;
			}
			if (UiEquipment.SUBEVENT_NOTIFY_ACTIVITED_WITH_NET == subEventId) {
				ServiceManager.getInstance().broadInvoke("sdk.init.actived",
						null);
				break;
			}
			if (UiEquipment.SUBEVENT_RESP_LICENSE == subEventId) {
				try {
					UiEquipment.Resp_License pbRespLicense = UiEquipment.Resp_License
							.parseFrom(data);

					if (pbRespLicense.bOk) {
						JNIHelper.logd("license ok.");
						// save license config
						ProjectCfg
								.setIflyAppId(pbRespLicense.msgKeyInfo.strIflyAppId);
						ProjectCfg	
								.setYunzhishengAppId(pbRespLicense.msgKeyInfo.strYunzhishengAppId);
						ProjectCfg
								.setYunzhishengSecret(pbRespLicense.msgKeyInfo.strYunzhishengSecrectKey);
						if(pbRespLicense.uint64Uid!=null)
							ProjectCfg.setUid(pbRespLicense.uint64Uid);
						// license init...
						AppLogic.runOnBackGround(checkInitStateRun,
								DELAY_CHECK_TIME);
						TtsManager.getInstance().initializeComponent();
						AsrManager.getInstance().initializeComponent();
						WakeupManager.getInstance().initializeComponent();
						TextManager.getInstance().initializeComponent();
						mInitError = null;

						AppLogic.runOnBackGround(new Runnable() {
							@Override
							public void run() {
								if (mWinNoticeInitFail != null) {
									mWinNoticeInitFail.cancel();
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
										mWinNoticeInitFail.cancel();
									}
									mWinNoticeInitFail = new WinNotice(true) {
										@Override
										public void onClickOk() {
											mWinNoticeInitFail = null;
										}
									}.setMessage(mInitError);
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
		if ("com.txznet.txz.component.asr.yunzhisheng_3_0.AsrYunzhishengImpl"
				.equals(ImplCfg.getAsrImplClass())
				&& !AsrManager.getInstance().isInitSuccessed()) {
			JNIHelper
					.logw("asr engine init fail, we will remove all asr model data, and restart!!!");
			String modelPath = GlobalContext.get().getApplicationContext()
					.getFilesDir().getAbsolutePath()
					+ "/YunZhiSheng/asrfix/asrfix.dat";
			File f = new File(modelPath);
			if (f.exists()) {
				f.delete();
			}

			String grammarPath = GlobalContext.get().getApplicationContext()
					.getFilesDir().getAbsolutePath()
					+ "/YunZhiSheng/asrfix/jsgf_model/txzTag.dat";
			File grammarFile = new File(grammarPath);
			if (grammarFile.exists()) {
				grammarFile.delete();
			}

			bNeededRestart = true;
		}
		if ("com.txznet.txz.component.tts.yunzhisheng_3_0.TtsYunzhishengImpl"
				.equals(ImplCfg.getTtsImplClass())
				&& !TtsManager.getInstance().isInitSuccessed()) {
			JNIHelper
					.logw("tts engine init fail, we will remove all tts model data, and restart!!!");
			String modelPath = GlobalContext.get().getApplicationContext()
					.getFilesDir().getAbsolutePath()
					+ "/YunZhiSheng/offline/tts/models/yzsttsmodel";
			File f = new File(modelPath);
			if (f.exists()) {
				f.delete();
			}
			bNeededRestart = true;
		}
        
		if (!WakeupManager.getInstance().isInitSuccessed()){
			JNIHelper.logw("wakeup engine init fail, we will restarted"
					+ "inited = "
					+ WakeupManager.getInstance().isInitSuccessed());
			bNeededRestart = true;
		}
		if (bNeededRestart) {
			JNIHelper.logw("bNeededRestart = " + bNeededRestart
					+ ", txz will restart!!!");
			AppLogic.restartProcess();
		} else {
			JNIHelper.logd("bNeededRestart = " + bNeededRestart);
		}
	}
}
