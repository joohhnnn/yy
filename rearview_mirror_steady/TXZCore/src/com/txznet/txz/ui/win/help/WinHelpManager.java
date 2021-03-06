package com.txznet.txz.ui.win.help;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import com.google.gson.JsonObject;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.innernet.UiInnerNet;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.util.FilePathConstants;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.media.chooser.AudioPriorityChooser;
import com.txznet.txz.component.media.chooser.MusicPriorityChooser;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.advertising.AdvertisingManager;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.download.DownloadManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.version.VersionManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.UnZipUtil;
import com.txznet.txz.util.runnables.Runnable1;

import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.txznet.txz.ui.win.help.WinHelpDetailTops.CMD_NET_TYPE_MIX;
import static com.txznet.txz.ui.win.help.WinHelpDetailTops.CMD_NET_TYPE_NET;
import static com.txznet.txz.ui.win.help.WinHelpDetailTops.CMD_OPEN_TYPE_NORMAL;
import static com.txznet.txz.ui.win.help.WinHelpDetailTops.SHOW_TYPE_NET_GRAY;
import static com.txznet.txz.ui.win.help.WinHelpDetailTops.SHOW_TYPE_NET_HIDE;
import static com.txznet.txz.ui.win.help.WinHelpDetailTops.SHOW_TYPE_NORMAL;
import static com.txznet.txz.ui.win.help.WinHelpDetailTops.compareDate;

public class WinHelpManager extends IModule {

    public static final int TYPE_OPEN_FROM_CLICK = 0;//??????????????????
    public static final int TYPE_OPEN_FROM_VOICE = 1;//??????????????????
    public static final int TYPE_OPEN_FROM_BACK = 2;//?????????????????????
    public static final int TYPE_OPEN_FROM_SDK = 4;//???SDK????????????

    public static final int TYPE_CLOSE_FROM_CLICK = 0;//??????????????????
    public static final int TYPE_CLOSE_FROM_VOICE = 1;//??????????????????
    public static final int TYPE_CLOSE_FROM_DETAIL = 2;//??????????????????
    public static final int TYPE_CLOSE_FROM_OTHER = 3;//??????????????????????????????

    public static final int TYPE_SNAP_PAGE_FROM_CLICK = 0;//????????????
    public static final int TYPE_SNAP_PAGE_FROM_VOICE = 1;//????????????
    public static final String KEY_OPEN_FROM_SDK = "sdk";//??????sdk????????????

    public static final String DOWNLOAD_WIN_HELP_PARAM = "win.help";

    private WinHelpQRCodeDialog mWinHelpQRCodeDialog;
    private WinHelpGuideQRCodeDialog mWinHelpGuideQRCodeDialog;
    private int mWinType = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 3;
    private boolean mEnableFullScreen = false;
    private boolean mEnableShowHelpQRCode = true;


	private static WinHelpManager sWinHeloManager = new WinHelpManager();

    private WinHelpManager() {
        RecorderWin.OBSERVABLE.registerObserver(new RecorderWin.StatusObervable.StatusObserver() {
            @Override
            public void onShow() {
               closeQRCodeDialog();
            }

            @Override
            public void onDismiss() {
                closeQRCodeDialog();
            }
        });
    }

    public static WinHelpManager getInstance() {
        return sWinHeloManager;
    }

    @Override
    public int initialize_BeforeStartJni() {
        // ???????????????????????????
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_NOTIFY_HELPINFO);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_INIT_SUCCESS);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_GET_RECOMMEND_INFO);
        WeixinManager.getInstance().addOnQrCodeListener(onQRCodeListener);
        return super.initialize_BeforeStartJni();
    }

    @Override
    public int initialize_AfterStartJni() {
//		if (HelpGuideManager.getInstance().hasGuideAnim()) {
        regCommand("OPEN_VOICE_GUIDE");
//		}
        regCustomCommand("OPEN_HELP", UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SHOW_HELP);
        return super.initialize_AfterStartJni();
    }

    @Override
    public int onCommand(String cmd) {
        closeQRCodeDialog();
        if (cmd.equals("OPEN_VOICE_GUIDE")) {
            if (!WinHelpDetailTops.getInstance().openGuideHelpDetail()) {
                RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE_2"), null);
            }
        } else if (cmd.equals("OPEN_GUIDE_ANIM")) {
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_OPEN_GUIDE_ANIM"), new Runnable() {

                @Override
                public void run() {
                    HelpGuideManager.getInstance().startGuideAnimFromVoiceCmd();
                }
            });
        }
        return 0;
    }

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		if (eventId == UiEvent.EVENT_ACTION_EQUIPMENT) {
			if (subEventId == UiEquipment.SUBEVENT_RESP_NOTIFY_HELPINFO) {
				//???????????????
			} else if (subEventId == UiEquipment.SUBEVENT_REQ_INIT_SUCCESS) {
				checkHelpInfo();
				updateWXBindUrl();
                requestHelpQRCodeData();
            } else if (subEventId == UiEquipment.SUBEVENT_RESP_GET_RECOMMEND_INFO){
                LogUtil.d("QRCode SUBEVENT_RESP_GET_RECOMMEND_INFO");
                try {
                    UiEquipment.Resp_Recommend_Info info = UiEquipment.Resp_Recommend_Info.parseFrom(data);
                    JSONBuilder jsonBuilder;
                    if (info.strJson != null && info.strJson.length != 0) {
                        jsonBuilder = new JSONBuilder(info.strJson);
                        LogUtil.d("QRCode json:" + jsonBuilder.toString());
                        String qrCodeIsShow = jsonBuilder.getVal("qrCodeIsShow", String.class);
                        HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_HELP_QRCODE_IS_SHOW, qrCodeIsShow);
                        if ("true".equals(qrCodeIsShow)) {
                            //????????????
                            downloadFile(jsonBuilder.getVal("qrCodeTitleIcon", String.class));
                            HelpPreferenceUtil.getInstance().setLong(HelpPreferenceUtil.KEY_HELP_QRCODE_LAST_REQ_TIME, System.currentTimeMillis());
                            HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_HELP_QRCODE_DATA, jsonBuilder.toString());
                            int version = jsonBuilder.getVal("version", Integer.class, 0);
                            int localVersion = HelpPreferenceUtil.getInstance().getInt(HelpPreferenceUtil.KEY_HELP_QRCODE_DATA_VERSION, 0);
                            if (version != 0 && version != localVersion) {
                                HelpPreferenceUtil.getInstance().setInt(HelpPreferenceUtil.KEY_HELP_QRCODE_DATA_VERSION, version);
                                String immediatelyShow = jsonBuilder.getVal("qrCodeNeedImmediatelyShow", String.class);
                                HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_HELP_NEED_IMMEDIATELY_SHOW, immediatelyShow);
                            }
                        }
                    }
                } catch (Exception e) {
                	LogUtil.d("QRCode data error.");
                    e.printStackTrace();
                }
			}
		}else if (eventId == UiEvent.EVENT_VOICE){
			if (subEventId == VoiceData.SUBEVENT_VOICE_SHOW_HELP) {
				if (SenceManager.getInstance().noneedProcSence(
						"help",
						new JSONBuilder()
								.put("action", "open")
								.put("scene", "help")
								.put("text",
										TextResultHandle.getInstance()
												.getParseText()).toBytes()))
					return super.onEvent(eventId, subEventId, data);
				if (hasThirdImpl()) {
					WinHelpManager.getInstance().show(new JSONBuilder().put("type", WinHelpManager.TYPE_OPEN_FROM_VOICE).toString());
				} else {
					String spk = NativeData.getResString("RS_ASR_OPEN_HELP");
					AsrManager.getInstance().setNeedCloseRecord(false);
					RecorderWin.speakTextWithClose(spk, false, false, new Runnable() {
						@Override
						public void run() {
							WinHelpManager.getInstance().show(new JSONBuilder().put("type", WinHelpManager.TYPE_OPEN_FROM_VOICE).toString());
						}
					});
				}
			}
		}
		return super.onEvent(eventId, subEventId, data);
	}

    public void show(String params) {
        AppLogic.runOnUiGround(new Runnable1<String>(params) {
            @Override
            public void run() {
                getAdapter().show(mP1);
            }
        }, 0);
		AdvertisingManager.getInstance().clearAdvertising();
    }

    public void close(String params) {
        AppLogic.runOnUiGround(new Runnable1<String>(params) {
            @Override
            public void run() {
                getAdapter().close(mP1);
            }
        }, 0);
    }

    @Override
    public int initialize_AfterInitSuccess() {
        HelpGuideManager.getInstance().init(GlobalContext.get());
        return super.initialize_AfterInitSuccess();
    }

//	@Override
//	public int initialize_BeforeStartJni() {
//		regEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP);
//		return super.initialize_BeforeStartJni();
//	}
//
//	@Override
//	public int onEvent(int eventId, int subEventId, byte[] data) {
//		switch (eventId) {
//		case UiEvent.EVENT_INNER_NET:
//			switch (subEventId) {
//				case UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP:
//					try {
//						UiInnerNet.DownloadHttpFileTask task = UiInnerNet.DownloadHttpFileTask.parseFrom(data);
//						onDownLoad(task);
//					} catch (Exception e) {
//						JNIHelper.logd(e.toString());
//					}
//					break;
//			}
//			break;
//		}
//		return super.onEvent(eventId, subEventId, data);
//	}
//
//
//	private void onDownLoad(UiInnerNet.DownloadHttpFileTask task) {
////		if (task.strFile == )
//
//
//	}


    /**
     * ???????????????????????????????????????
     *
     * @param zipPath
     */
    public void onDownloadFile(String zipPath) {
        //????????????????????????
        //???????????????????????????
        String path = HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HELP_FILE_PATH, FilePathConstants.DEFAULT_HELP_FILE_DIR);
        File oldHelpFile = new File(path);
        File backFile = new File(FilePathConstants.DEFAULT_HELP_FILE_BACKUP_DIR);
        if (oldHelpFile.exists()) {
            //?????????????????????????????????
            FileUtil.removeDirectory(backFile);
            //????????????????????????
            oldHelpFile.renameTo(backFile);
            //???????????????????????????
            FileUtil.removeDirectory(oldHelpFile);
        }
        //???????????????????????????????????????
        UnZipUtil.getInstance().UnZip(zipPath, path);
        //?????????????????????
        showHelpNewTag(true);
        //??????????????????????????????
        HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_HELP_FILE_PATH, path);
        //????????????????????????????????????

        //TODO ????????????zip?????????help?????????,??????????????????????????????????????????
	}

    /**
     * ????????????
     * @param url ??????
     */
    public String downloadFile(String url){
        String taskId = MD5Util.generateMD5(url);
        File file = new File(DownloadManager.DOWNLOAD_FILE_ROOT, taskId);
        if (file.exists()) {
            return taskId;
        }
        UiInnerNet.DownloadHttpFileTask task = new UiInnerNet.DownloadHttpFileTask();
        task.strUrl = url;
        task.strTaskId = taskId;
        task.strDefineParam = DOWNLOAD_WIN_HELP_PARAM;
        task.bForbidUseReservedSpace = true; // ????????????????????????
        JNIHelper.sendEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_REQ, task);
        return taskId;
    }

    /**
     * ??????core???????????????
     *
     * @return
     */
    public int getCurVersion() {
        int versionCode = -1;
        try {
            String[] subStrings = VersionManager.getInstance().getUserVersionNumber().split("\\.");
            int weight = 1;
            for (int i = subStrings.length - 1; i >= 0; i--) {
                versionCode += Integer.parseInt(subStrings[i]) * weight;
                weight *= 100;
            }
            versionCode = 1000000 + versionCode + 1;
        } catch (Exception e) {
            LogUtil.loge("getVersionError", e);
        }
        return versionCode;
    }

    private void checkShowHelpNewTag() {
        int startCount = HelpPreferenceUtil.getInstance().getInt(HelpPreferenceUtil.KEY_LAUNCH_COUNT, 0);
        if (startCount == 0) {
            showHelpNewTag(true);
            startCount++;
            HelpPreferenceUtil.getInstance().setInt(HelpPreferenceUtil.KEY_LAUNCH_COUNT, startCount);
            HelpPreferenceUtil.getInstance().setLong(HelpPreferenceUtil.KEY_LAST_LAUNCH_TIME, NativeData.getMilleServerTime().uint64Time);
        } else if (startCount == 1) {
            long lastStartTime = HelpPreferenceUtil.getInstance().getLong(HelpPreferenceUtil.KEY_LAST_LAUNCH_TIME, -1);
            long currentTime = NativeData.getMilleServerTime().uint64Time;
            if (currentTime - lastStartTime >= (long) 3 * 24 * 60 * 60 * 1000) {
                showHelpNewTag(true);
            }
            startCount++;
            HelpPreferenceUtil.getInstance().setInt(HelpPreferenceUtil.KEY_LAUNCH_COUNT, startCount);
            HelpPreferenceUtil.getInstance().setLong(HelpPreferenceUtil.KEY_LAST_LAUNCH_TIME, currentTime);
        } else if (startCount == 2) {
            long lastStartTime = HelpPreferenceUtil.getInstance().getLong(HelpPreferenceUtil.KEY_LAST_LAUNCH_TIME, -1);
            long currentTime = NativeData.getMilleServerTime().uint64Time;
            if (currentTime - lastStartTime >= (long) 90 * 24 * 60 * 60 * 1000) {
                showHelpNewTag(true);
            }
            startCount++;
            HelpPreferenceUtil.getInstance().setInt(HelpPreferenceUtil.KEY_LAUNCH_COUNT, startCount);
            HelpPreferenceUtil.getInstance().setLong(HelpPreferenceUtil.KEY_LAST_LAUNCH_TIME, currentTime);
        } else {
            //????????????????????????
        }

    }

    /**
     * ??????core?????????????????????????????????????????????????????????????????????
     * ????????????????????????
     * TODO ??????????????????????????????????????????
     */
    public void checkHelpVersion() {

        JSONBuilder mNewHelpJson = new JSONBuilder(new File(FilePathConstants.DEFAULT_HELP_FILE_PATH));
        //??????-1????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        Integer minVer = mNewHelpJson.getVal("minVer", Integer.class, -1);
        Integer maxVer = mNewHelpJson.getVal("maxVer", Integer.class, -1);
        int curVer = getCurVersion();
        if ((minVer != -1 && curVer < minVer) || (maxVer != -1 && curVer > maxVer)) {
            LogUtil.loge("checkHelpVersion error , minVer: " + minVer + " ,maxVer: " + maxVer + " ,curVer: " + curVer);
            FileUtil.removeDirectory(FilePathConstants.DEFAULT_HELP_FILE_DIR);
        }
    }

    public void unZipHelpData() {

        checkShowHelpNewTag();

        checkHelpVersion();

        File helpZipFile = new File(FilePathConstants.DEFAULT_HELP_FILE);
        if (helpZipFile.exists()) {
            File mHelpFile = new File(FilePathConstants.DEFAULT_HELP_FILE_DIR);
            File backFile = new File(FilePathConstants.DEFAULT_HELP_FILE_BACKUP_DIR);
            File tmpFile = new File(FilePathConstants.DEFAULT_HELP_FILE_TEMP_DIR);
            try {
                FileUtil.removeDirectory(FilePathConstants.DEFAULT_HELP_FILE_TEMP_DIR);
                //???????????????????????????????????????
                UnZipUtil.getInstance().UnZip(FilePathConstants.DEFAULT_HELP_FILE, FilePathConstants.DEFAULT_HELP_FILE_TEMP_DIR);

                if (!isHelpAvailable(FilePathConstants.DEFAULT_HELP_FILE_TEMP_PATH)) {
                    LogUtil.loge("isHelpAvailable false");
                    doUpdateFail();
                    return;
                }

                JSONBuilder mNewHelpJson = new JSONBuilder(new File(FilePathConstants.DEFAULT_HELP_FILE_TEMP_PATH));
                JSONBuilder mHelpJson = new JSONBuilder(new File(FilePathConstants.DEFAULT_HELP_FILE_PATH));

                if (mNewHelpJson.getVal("version", Integer.class, -1) <= mHelpJson.getVal("version", Integer.class, -1)) {
                    LogUtil.loge("version error");
                    doUpdateFail();
                    return;
                }

                //??????-1????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                Integer minVer = mNewHelpJson.getVal("minVer", Integer.class, -1);
                Integer maxVer = mNewHelpJson.getVal("maxVer", Integer.class, -1);
                int curVer = getCurVersion();
                if ((minVer != -1 && curVer < minVer) || (maxVer != -1 && curVer > maxVer)) {
                    LogUtil.loge("version error , minVer: " + minVer + " ,maxVer: " + maxVer + " ,curVer: " + curVer);
                    doUpdateFail();
                    return;
                }

                int updateType = mNewHelpJson.getVal("update", Integer.class, 0);//0???????????????1????????????

                switch (updateType) {
                    case 1: {//??????????????????
                        if (isHelpAvailable(FilePathConstants.DEFAULT_HELP_FILE_PATH)) {
                            FileUtil.copyFiles(FilePathConstants.DEFAULT_HELP_FILE_DIR, FilePathConstants.DEFAULT_HELP_FILE_BACKUP_DIR);
                            boolean isUpdate = doUpdate(mNewHelpJson, mHelpJson);
                            if (isUpdate) {
                                FileUtil.copyFiles(FilePathConstants.DEFAULT_HELP_FILE_TEMP_DIR, FilePathConstants.DEFAULT_HELP_FILE_DIR);
                                //TODO ??????????????????????????????json????????????????????????????????????????????????
                                if (mNewHelpJson.getVal("showNew", Integer.class, 0) == 1) {//???????????????????????????,0?????????,1??????
                                    showHelpNewTag(true);
                                } else {
                                    showHelpNewTag(false);
                                }

                                if (mNewHelpJson.getVal("showTag", Integer.class, 0) == 1) {//??????????????????new??????,0?????????,1??????
                                    HelpPreferenceUtil.getInstance().setBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_TAG, true);
                                } else {
                                    HelpPreferenceUtil.getInstance().setBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_TAG, false);
                                }

                                JSONArray details = mNewHelpJson.getVal("detail", JSONArray.class, null);
                                if (details != null && details.length() > 0) {
                                    HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_HELP_DETAIL_NAME, details.getString(0));
                                }
                            }
                        } else {
                        }//?????????????????????????????????????????????????????????????????????
                        FileUtil.removeDirectory(tmpFile);
                    }
                    break;
//					case 2:{//??????????????????
//
//					}
//					break;
                    //???????????????????????????????????????
                    case 0:
                    default: {
                        if (tmpFile.exists()) {
                            if (isHelpAvailable(FilePathConstants.DEFAULT_HELP_FILE_PATH)) {
                                //?????????????????????????????????
                                FileUtil.removeDirectory(backFile);
                                //????????????????????????
                                mHelpFile.renameTo(backFile);
                            } else {
                                FileUtil.removeDirectory(mHelpFile);
                            }
                            tmpFile.renameTo(mHelpFile);
                            //TODO ??????????????????????????????json????????????????????????????????????????????????
                            if (mNewHelpJson.getVal("showNew", Integer.class, 0) == 1) {//???????????????????????????,0?????????,1??????
                                showHelpNewTag(true);
                            } else {
                                showHelpNewTag(false);
                            }

                            if (mNewHelpJson.getVal("showTag", Integer.class, 0) == 1) {//??????????????????new??????,0?????????,1??????
                                HelpPreferenceUtil.getInstance().setBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_TAG, true);
                            } else {
                                HelpPreferenceUtil.getInstance().setBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_TAG, false);
                            }

                            JSONArray details = mNewHelpJson.getVal("detail", JSONArray.class, null);
                            if (details != null && details.length() > 0) {
                                HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_HELP_DETAIL_NAME, details.getString(0));
                            }
                        }
                    }
                    break;
                }

                FileUtil.removeDirectory(helpZipFile);
            } catch (Exception e) {
                //TODO ????????????????????????
                LogUtil.loge("update help error", e);
                doUpdateFail();
                if (!mHelpFile.exists()) {
                    if (backFile.exists()) {
                        backFile.renameTo(mHelpFile);
                    }
                }
            }
        }
    }

    private void doUpdateFail() {
        FileUtil.removeDirectory(FilePathConstants.DEFAULT_HELP_FILE_TEMP_DIR);
        FileUtil.removeDirectory(FilePathConstants.DEFAULT_HELP_FILE);
    }

    private boolean isHelpAvailable(String filePath) {
        boolean isHelpAvailable = false;
        File file = new File(filePath);
        if (file.exists()) {
            JSONBuilder jsonBuilder = new JSONBuilder(file);
            isHelpAvailable = jsonBuilder.getVal("version", Integer.class, -1) != -1;
        }
        return isHelpAvailable;
    }

    /**
     * ???????????????????????????
     *
     * @param mNewHelpJson
     * @param mHelpJson
     * @return
     */
    public boolean doUpdate(JSONBuilder mNewHelpJson, JSONBuilder mHelpJson) {
        boolean isUpdate = false;
        try {

            JSONBuilder mFinalJson = new JSONBuilder();

            String mImgPath = mHelpJson.getVal("imgDir", String.class);
            String mNewImgPath = mNewHelpJson.getVal("imgDir", String.class);
            mFinalJson.put("update", mNewHelpJson.getVal("update", Integer.class, mHelpJson.getVal("update", Integer.class, 0)));
            mFinalJson.put("version", mNewHelpJson.getVal("version", Integer.class, mHelpJson.getVal("version", Integer.class, 0)));
            mFinalJson.put("minVer", mNewHelpJson.getVal("minVer", Integer.class, mHelpJson.getVal("minVer", Integer.class, -1)));
            mFinalJson.put("maxVer", mNewHelpJson.getVal("maxVer", Integer.class, mHelpJson.getVal("maxVer", Integer.class, -1)));
            mFinalJson.put("type", mNewHelpJson.getVal("type", Integer.class, mHelpJson.getVal("type", Integer.class, 0)));
            mFinalJson.put("showNew", mNewHelpJson.getVal("showNew", Integer.class, mHelpJson.getVal("showNew", Integer.class, 1)));
            mFinalJson.put("showTag", mNewHelpJson.getVal("showTag", Integer.class, mHelpJson.getVal("showTag", Integer.class, 1)));
            mFinalJson.put("showNew", mNewHelpJson.getVal("showNew", Integer.class, mHelpJson.getVal("showNew", Integer.class, 1)));
            mFinalJson.put("imgDir", mImgPath);
            mFinalJson.put("detail", mNewHelpJson.getVal("detail", JSONArray.class, mHelpJson.getVal("detail", JSONArray.class, null)));

            ArrayList<HelpDetail> mNewHelpDetails = getHelpDetails(mNewHelpJson);
            if (mNewHelpDetails == null || mNewHelpDetails.size() == 0) {
                return isUpdate;
            }
            ArrayList<HelpDetail> mHelpDetails = getHelpDetails(mHelpJson);
            if (mHelpDetails == null || mHelpDetails.size() == 0) {
                return isUpdate;
            }
            ArrayList<HelpDetail> mFinalHelpDetails = new ArrayList<HelpDetail>();
            HelpDetail mNewHelpDetail;
            HelpDetail mHelpDetail;
            HelpDetail.HelpDetailItem mNewHelpDetailItem;
            HelpDetail.HelpDetailItem mHelpDetailItem;
            ArrayList<HelpDetail> tmpHelpDetails = new ArrayList<HelpDetail>();
            tmpHelpDetails.addAll(mHelpDetails);
            for (int i = 0; i < mNewHelpDetails.size(); i++) {
                mNewHelpDetail = mNewHelpDetails.get(i);
                for (int j = 0; j < mHelpDetails.size(); j++) {
                    mHelpDetail = mHelpDetails.get(j);
                    if (TextUtils.equals(mHelpDetail.name, mNewHelpDetail.name)) {
                        tmpHelpDetails.remove(mHelpDetail);
                        ArrayList<HelpDetail.HelpDetailItem> tmpHelpDetailItems = new ArrayList<HelpDetail.HelpDetailItem>();
                        tmpHelpDetailItems.addAll(mHelpDetail.detailItems);
                        for (int k = 0; k < mNewHelpDetail.detailItems.size(); k++) {
                            mNewHelpDetailItem = mNewHelpDetail.detailItems.get(k);
                            for (int l = 0; l < mHelpDetail.detailItems.size(); l++) {
                                mHelpDetailItem = mHelpDetail.detailItems.get(l);
                                if (TextUtils.equals(mHelpDetailItem.name, mNewHelpDetailItem.name)) {
                                    tmpHelpDetailItems.remove(mHelpDetailItem);
                                }
                            }
                        }
                        mNewHelpDetail.detailItems.addAll(tmpHelpDetailItems);
                    }
                }
                mFinalHelpDetails.add(mNewHelpDetail);
            }
            mFinalHelpDetails.addAll(tmpHelpDetails);
            JSONArray tmp = valueHelpDetails2Json(mFinalHelpDetails);
            if (tmp != null) {
                mFinalJson.put("help", tmp);
            }
            try {
                File file = new File(FilePathConstants.DEFAULT_HELP_FILE_TEMP_PATH);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(mFinalJson.toPostString().getBytes());
                fileOutputStream.flush();
                fileOutputStream.close();
                isUpdate = true;
            } catch (IOException e) {
                e.printStackTrace();
                isUpdate = false;
            }

        } catch (Exception e) {
            e.printStackTrace();
            isUpdate = false;
        }

        return isUpdate;
    }

    private ArrayList<HelpDetail> getHelpDetails(JSONBuilder jBuilder) {
        ArrayList<HelpDetail> tmpDetails = new ArrayList<HelpDetail>();
        try {
            HelpDetail helpDetail;
            JSONArray helpDetailsArray;
            helpDetailsArray = jBuilder.getVal("help", JSONArray.class, null);
            JSONArray helpDetailsItemArray;
            JSONArray helpDetailsImgArray;
            JSONBuilder helpDetailJson;
            JSONBuilder helpDetailItemJson;
            JSONBuilder helpDetailImgJson;

            for (int i = 0; i < helpDetailsArray.length(); i++) {
                helpDetailJson = new JSONBuilder(helpDetailsArray.getJSONObject(i));
                helpDetail = new HelpDetail();
                helpDetail.id = helpDetailJson.getVal("id", String.class);
                helpDetail.name = helpDetailJson.getVal("name", String.class);
                helpDetail.lastName = helpDetailJson.getVal("lastName", String.class);
                helpDetail.iconName = helpDetailJson.getVal("icon", String.class);
                helpDetail.time = helpDetailJson.getVal("time", String.class);
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
                helpDetailsItemArray = helpDetailJson.getVal("desc", JSONArray.class, null);

                if (helpDetailsItemArray != null) {
                    for (int j = 0; j < helpDetailsItemArray.length(); j++) {
                        helpDetailItemJson = new JSONBuilder(helpDetailsItemArray.getJSONObject(j));
                        HelpDetail.HelpDetailItem helpDetailItem = new HelpDetail.HelpDetailItem();
                        helpDetailItem.id = helpDetailItemJson.getVal("id", String.class);
                        helpDetailItem.name = helpDetailItemJson.getVal("name", String.class);
                        helpDetailItem.time = helpDetailItemJson.getVal("time", String.class);
                        helpDetailItem.netType = helpDetailItemJson.getVal("netType", Integer.class, CMD_NET_TYPE_MIX);
                        helpDetail.detailItems.add(helpDetailItem);
                    }
                }

                helpDetail.detailImgs = new ArrayList<HelpDetail.HelpDetailImg>();
                helpDetailsImgArray = helpDetailJson.getVal("imgs", JSONArray.class, null);
                if (helpDetailsImgArray != null) {
                    for (int j = 0; j < helpDetailsImgArray.length(); j++) {
                        helpDetailImgJson = new JSONBuilder(helpDetailsImgArray.getJSONObject(j));
                        HelpDetail.HelpDetailImg helpDetailImg = new HelpDetail.HelpDetailImg();
                        helpDetailImg.id = helpDetailImgJson.getVal("id", String.class, null);
                        helpDetailImg.text = helpDetailImgJson.getVal("text", String.class, null);
                        helpDetailImg.time = helpDetailImgJson.getVal("time", String.class, null);
                        helpDetailImg.img = helpDetailImgJson.getVal("img", String.class, "");
                        helpDetail.detailImgs.add(helpDetailImg);
                    }
                }
                tmpDetails.add(helpDetail);
            }
        } catch (Exception e) {
            tmpDetails.clear();
            tmpDetails = null;
        }
        return tmpDetails;
    }

    private JSONArray valueHelpDetails2Json(ArrayList<HelpDetail> tmpDetails) {
        JSONArray helpDetailsArray = null;
        try {
            HelpDetail helpDetail;

            helpDetailsArray = new JSONArray();
            JSONArray helpDetailsItemArray;
            JSONArray helpDetailsImgArray;
            JSONBuilder helpDetailJson;
            JSONBuilder helpDetailItemJson;
            JSONBuilder helpDetailImgJson;

            for (int i = 0; i < tmpDetails.size(); i++) {
                helpDetailJson = new JSONBuilder();
                helpDetail = tmpDetails.get(i);
                helpDetailJson.put("id", helpDetail.id);
                helpDetailJson.put("name", helpDetail.name);
                helpDetailJson.put("lastName", helpDetail.lastName);
                helpDetailJson.put("icon", helpDetail.iconName);
                helpDetailJson.put("time", helpDetail.time);
                helpDetailJson.put("openType", helpDetail.openType);
                helpDetailJson.put("package", helpDetail.strPackage);
                helpDetailJson.put("tool", helpDetail.tool);

                if (helpDetail.intros != null && helpDetail.intros.length != 0) {
                    JSONArray intros = new JSONArray();
                    for (int k = 0; k < helpDetail.intros.length; k++) {
                        intros.put(helpDetail.intros[k]);
                    }
                    helpDetailJson.put("intro", intros);
                }

                helpDetailsItemArray = new JSONArray();

                for (int j = 0; j < helpDetail.detailItems.size(); j++) {
                    helpDetailItemJson = new JSONBuilder();
                    HelpDetail.HelpDetailItem helpDetailItem = helpDetail.detailItems.get(j);
                    helpDetailItemJson.put("id", helpDetailItem.id);
                    helpDetailItemJson.put("name", helpDetailItem.name);
                    helpDetailItemJson.put("time", helpDetailItem.time);
                    helpDetailItemJson.put("netType", helpDetailItem.netType);
                    helpDetailsItemArray.put(helpDetailItemJson.getJSONObject());
                }
                helpDetailJson.put("desc", helpDetailsItemArray);


                helpDetailsImgArray = new JSONArray();
                if (helpDetail.detailImgs != null) {
                    for (int j = 0; j < helpDetail.detailImgs.size(); j++) {
                        helpDetailImgJson = new JSONBuilder();
                        HelpDetail.HelpDetailImg helpDetailImg = helpDetail.detailImgs.get(j);
                        helpDetailImgJson.put("id", helpDetailImg.id);
                        helpDetailImgJson.put("text", helpDetailImg.text);
                        helpDetailImgJson.put("time", helpDetailImg.time);
                        helpDetailImgJson.put("img", helpDetailImg.img);
                        helpDetailsImgArray.put(helpDetailImgJson.getJSONObject());
                    }
                }
                helpDetailJson.put("imgs", helpDetailsImgArray);
                helpDetailsArray.put(helpDetailJson.getJSONObject());
            }
        } catch (Exception e) {
            helpDetailsArray = null;
            LogUtil.loge("valueHelpDetails2Json error", e);
        }
        return helpDetailsArray;
    }


    /**
     * ?????????????????????
     */
    public static void showHelpNewTag(boolean show) {
        ConfigUtil.setShowHelpNewTag(show);
        HelpPreferenceUtil.getInstance().setBoolean(HelpPreferenceUtil.KEY_SHOW_HELP_NEWS, show);
        JSONBuilder info = new JSONBuilder();
        info.put("type", 0);
        info.put("showHelpNewTag", show);
        RecorderWin.sendInformation(info.toString());
    }

    /**
     * ?????????????????????????????????????????????????????????
     *
     * @param id
     */
    public static void selectHelpDetail(String id) {
        HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_HELP_DETAIL_NAME, id);
    }

    private WeixinManager.OnQRCodeListener onQRCodeListener = new WeixinManager.OnQRCodeListener() {
        @Override
        public void onGetQrCode(boolean isBind, String url) {
            if (!TextUtils.isEmpty(url)) {
                HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_QCORD_WX_BIND_URL, url);
            }
        }
    };

    public void updateWXBindUrl() {
        if (TextUtils.isEmpty(HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_QCORD_WX_BIND_URL, null))) {
            JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_BIND_WX_URL);
        }
    }

    /**
     * ?????????????????????????????????
     */
    public void requestHelpQRCodeData(){
        LogUtil.d("QRCode SUBEVENT_REQ_GET_RECOMMEND_INFO");
        UiEquipment.Req_Recommend_Info req_recommend_info = new UiEquipment.Req_Recommend_Info();
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_RECOMMEND_INFO, req_recommend_info);
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    public Integer getHelpVersion() {
        Integer helpVersion = null;
        File mHelpFile = new File(FilePathConstants.DEFAULT_HELP_FILE_DIR + File.separator + "help.txt");
        if (mHelpFile.exists()) {
            JSONBuilder jsonBuilder = new JSONBuilder(mHelpFile);
            helpVersion = jsonBuilder.getVal("version", Integer.class, null);
        }
        return helpVersion;
    }

    /**
     * ??????????????????????????????????????????????????????
     */
    public void checkHelpInfo() {
        EquipmentManager.Req_HelpInfo helpInfo = new EquipmentManager.Req_HelpInfo();
        helpInfo.uint32HelpVersion = getHelpVersion();
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_NOTIFY_HELPINFO, helpInfo);
    }

    public static interface HelpWinInvokeAdapter {
        public void show(String params);

        public void close(String params);
    }

    private static final HelpWinInvokeAdapter REMOTE_ADAPTER = new HelpWinInvokeAdapter() {
        @Override
        public void show(String params) {
            ChoiceManager.getInstance().clearIsSelectingClickHelp();
            ServiceManager.getInstance().sendInvoke(thirdImpl, "help.win.show", null, null);
        }

        @Override
        public void close(String params) {
            ServiceManager.getInstance().sendInvoke(thirdImpl, "help.win.dismiss", null, null);
        }
    };


    private static final HelpWinInvokeAdapter LOCAL_ADAPTER = new HelpWinInvokeAdapter() {

        @Override
        public void show(String params) {
            if (WinManager.getInstance().hasThirdImpl()) {
                RecorderWin.close();
            }
            ChoiceManager.getInstance().clearIsSelectingClickHelp();
            WinHelpDetailTops.getInstance().show(params);
        }

        @Override
        public void close(String params) {
            WinHelpDetailTops.getInstance().dismiss(params);
            if (WinManager.getInstance().hasThirdImpl()) {
                RecorderWin.close();
            }
        }
    };

    private static String thirdImpl;

    public boolean hasThirdImpl() {
        return !TextUtils.isEmpty(thirdImpl);
    }


    public HelpWinInvokeAdapter getAdapter() {
        if (hasThirdImpl()) {
            return REMOTE_ADAPTER;
        }
        return LOCAL_ADAPTER;
    }

    class WinHelpProxy implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {

                }
            }, 0);
            return null;
        }

    }

    public byte[] invokeWinHelp(final String packageName, String command, byte[] data) {
        if (command.equals("txz.help.ui.detail.open")) {
            int type = TYPE_OPEN_FROM_CLICK;
            if (data != null && data.length > 0) {
                if (TextUtils.equals(new String(data), KEY_OPEN_FROM_SDK)) {
                    type = TYPE_OPEN_FROM_SDK;
                }
            }
            show(new JSONBuilder().put("type", type).toString());
        } else if (command.equals("txz.help.ui.detail.back")) {
            close(new JSONBuilder().put("type", TYPE_CLOSE_FROM_CLICK).toString());
        } else if ("txz.help.win.set".equals(command)) {
            if (packageName != null) {
                thirdImpl = packageName;
            }
        } else if ("txz.help.win.clear".equals(command)) {
            thirdImpl = null;
        } else if (command.startsWith("txz.help.guide.")) {
            return HelpGuideManager.getInstance().invokeHelp(command.substring("txz.help.guide.".length()), data);
        } else if ("txz.help.getHelpDetailItems".equals(command)) {
            if (helpDetails == null || helpDetails.isEmpty()) {
                initHelpData();
            }
            return getHelpDetailJson().getBytes();
        } else if ("txz.help.ui.qrcode".equals(command)) {//????????????
            if (data != null && data.length > 0) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                showQRCodeDialog(jsonBuilder);
            }
        } else if ("txz.help.ui.detail.qrcode".equals(command)) {//????????????
            if (data != null && data.length > 0) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                String title = jsonBuilder.getVal("title",String.class);
                String url = jsonBuilder.getVal("url",String.class);
                String desc = jsonBuilder.getVal("desc",String.class);
                Boolean isFromFile = jsonBuilder.getVal("isFromFile",Boolean.class,false);
                showQRCodeDialogFromDetail(title,url,desc,isFromFile);
            }
        } else if ("txz.help.ui.qrcode.guide".equals(command)) {
            if (data != null && data.length > 0) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                showQRCodeGuideDialog(jsonBuilder);
            }
        }
        return null;
    }

    /**
     * ???????????????
     */
    public void showQRCodeDialog(JSONBuilder jsonBuilder) {
        if(mWinHelpQRCodeDialog != null && mWinHelpQRCodeDialog.isShowing()){
            LogUtil.d("win qrcode dialog isShowing.");
            return;
        }
        if(!RecorderWin.isOpened()){
            LogUtil.d("win qrcode dialog cant show,record win is dismiss.");
            return;
        }
        String title = jsonBuilder.getVal("title", String.class);
        String url = jsonBuilder.getVal("url", String.class);
        String desc = jsonBuilder.getVal("desc", String.class);
        url = "qrcode:" + url;
        mWinHelpQRCodeDialog = new WinHelpQRCodeDialog(mEnableFullScreen, title, url, desc, false);
        mWinHelpQRCodeDialog.updateDialogType(mWinType);
        mWinHelpQRCodeDialog.show();
        reportHelpQRCode();
    }

    /**
     * ???????????????-???????????????
     */
    public void showQRCodeDialogFromDetail(String title, String url, String desc, Boolean isFromFile) {
        if(mWinHelpQRCodeDialog != null && mWinHelpQRCodeDialog.isShowing()){
            LogUtil.d("win qrcode dialog isShowing.");
            return;
        }
        if(!RecorderWin.isOpened()){
            LogUtil.d("win qrcode dialog cant show,record win is dismiss.");
            return;
        }
        mWinHelpQRCodeDialog = new WinHelpQRCodeDialog(mEnableFullScreen, title, url, desc, isFromFile);
        mWinHelpQRCodeDialog.updateDialogType(mWinType);
        mWinHelpQRCodeDialog.show();
        reportHelpDetailQRCode();
    }

    /**
     * ??????????????????????????????
     */
    public void reportHelpDetailQRCode(){
        ReportUtil.doReport(new ReportUtil.Report.Builder()
                .setType("helpDetailQRCode")
                .setAction("open")
                .buildCommReport());
    }

    /**
     * ??????????????????????????????
     */
    public void reportHelpQRCode(){
        ReportUtil.doReport(new ReportUtil.Report.Builder()
                .setType("helpQRCode")
                .setAction("open")
                .buildCommReport());
    }


    public void showQRCodeGuideDialog(JSONBuilder jsonBuilder){
        if(jsonBuilder == null){
            return;
        }
        if(!RecorderWin.isOpened()){
            LogUtil.d("win qrcode dialog cant show,record win is dismiss.");
            return;
        }
        WinHelpDetailTops.getInstance().notNeedShowGuide();
        HelpPreferenceUtil.getInstance().setLong(HelpPreferenceUtil.KEY_HELP_GUIDE_LAST_SHOW_TIME, System.currentTimeMillis());
        HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_HELP_NEED_IMMEDIATELY_SHOW, "false");
        mWinHelpGuideQRCodeDialog = new WinHelpGuideQRCodeDialog(mEnableFullScreen,
                jsonBuilder);
        mWinHelpGuideQRCodeDialog.updateDialogType(mWinType);
        mWinHelpGuideQRCodeDialog.show();
    }

    public void closeQRCodeDialog() {
        if (mWinHelpQRCodeDialog != null) {
            mWinHelpQRCodeDialog.dismiss();
            mWinHelpQRCodeDialog = null;
        }
        if (mWinHelpGuideQRCodeDialog != null) {
            mWinHelpGuideQRCodeDialog.dismiss();
            mWinHelpGuideQRCodeDialog = null;
        }
    }

    /**
     * ??????????????????????????????????????????????????????????????????????????????
     *
     * @param helpItem
     * @return
     */
    public String checkHelpEnable(String helpItem) {
        if (TextUtils.isEmpty(thirdImpl)) {
            return WinHelpDetailTops.getInstance().checkHelpEnable(helpItem);
        } else {
            return "false";
        }
    }


    /****************************************************************************
     ****************?????????????????????????????????????????????????????????**************************
     ****************************************************************************/


    public JSONObject createHelpTip(String resId, String label) {
        JSONBuilder jsonBuilder;
        jsonBuilder = new JSONBuilder();
        jsonBuilder.put("resId", resId);
        jsonBuilder.put("label", label);
        return jsonBuilder.getJSONObject();
    }


    class HelpTip implements Comparable<HelpTip> {
        public HelpTip() {
        }

        public HelpTip(String icon, String tip, UUID id) {
            this.icon = icon;
            this.tip = tip;
            this.id = id;
        }

        String tip;
        String icon;
        UUID id;

        @Override
        public int compareTo(HelpTip o) {
            return id.compareTo(o.id);
        }
    }

    /**
     * ?????????????????????
     * @param text ????????????
	 *   @param checkAll ????????????,???true???????????????????????????false??????????????????????????????????????????
     */
    public void checkSpeakHelpTips(String text, boolean checkAll) {
       if(helpTips == null){
            initHelpTips();
       }
       //???????????????????????????????????????
       if(helpTips.size() < 5){
           //??????????????????????????????????????????????????????????????????????????????
           if(!loadHelpTips()){
               return;
           }
       }
       if(TextUtils.isEmpty(text)){
           return;
       }
       if(checkAll){
		   for(int i = 0; i <helpTips.size(); i++){
			   String tip = helpTips.get(i).tip.replaceAll("???","").replaceAll("???","");
			   if(tip.contains("/")){
				   String[] strings = tip.split("/");
				   for (String string : strings) {
					   if (text.equalsIgnoreCase(string)) {
						   int hitCount = HelpTipsPreferenceUtil.getInstance().getInt(helpTips.get(i).tip, 0);
						   hitCount += 1;
						   HelpTipsPreferenceUtil.getInstance().setInt(helpTips.get(i).tip, hitCount);
						   if (hitCount >= 3) {
							   helpTips.remove(i);
						   }
						   break;
					   }
				   }
			   }
			   if(text.equalsIgnoreCase(tip)){
				   int hitCount = HelpTipsPreferenceUtil.getInstance().getInt(helpTips.get(i).tip,0);
				   hitCount += 1;
				   HelpTipsPreferenceUtil.getInstance().setInt(helpTips.get(i).tip, hitCount);
				   if(hitCount == 3){
					   helpTips.remove(i);
					   //???????????????????????????????????????????????????????????????????????????????????????
					   initHelpTips();
				   }
				   break;
			   }
		   }
	   }else{
		   for(int i = 0; i <helpTips.size(); i++){
			   String tip = helpTips.get(i).tip.replaceAll("???","").replaceAll("???","");
			   if(tip.contains("/")){
				   String[] strings = tip.split("/");
				   for (String string : strings) {
					   if (string.contains(text)) {
						   int hitCount = HelpTipsPreferenceUtil.getInstance().getInt(helpTips.get(i).tip, 0);
						   hitCount += 1;
						   HelpTipsPreferenceUtil.getInstance().setInt(helpTips.get(i).tip, hitCount);
						   if (hitCount >= 3) {
							   helpTips.remove(i);
						   }
						   break;
					   }
				   }
			   }
			   if(tip.contains(text)){
				   int hitCount = HelpTipsPreferenceUtil.getInstance().getInt(helpTips.get(i).tip,0);
				   hitCount += 1;
				   HelpTipsPreferenceUtil.getInstance().setInt(helpTips.get(i).tip, hitCount);
				   if(hitCount == 3){
					   helpTips.remove(i);
					   //???????????????????????????????????????????????????????????????????????????????????????
					   initHelpTips();
				   }
				   break;
			   }
		   }
	   }

    }


    ArrayList<HelpTip> helpTips = null;

	/**
	 * false SDK????????????????????????????????????????????????????????????????????????
	 * true SDK??????????????????????????????????????????????????????????????????????????????
	 * @return
	 */
	public boolean checkSDKHelpTips(Iterator<String> keys, JSONObject jsonNearActions){
		int count = 0;
		while(keys.hasNext()){
			JSONArray tips;
			try {
				tips = jsonNearActions.getJSONArray(keys.next());
				count += tips.length();
				if(count >= 5){
					return true;
				}
			} catch (JSONException e) {
				return false;
			}
		}
		return false;
	}

	public boolean loadHelpTipsFromSDK(String hitJson) {
		if(TextUtils.isEmpty(hitJson)){
			return false;
		}
		LogUtil.d("loadHelpTipsFromSDK:"+hitJson);
		helpTips.clear();
		try {
			JSONObject jsonNearActions  = new JSONObject(hitJson);
			Iterator<String> keys = jsonNearActions.keys();
			if(!checkSDKHelpTips(keys, jsonNearActions)){
				return false;
			}
			HelpTip helpTip;
			String tip, icon;
			JSONArray tips;
			LinkedList<LinkedList<HelpTip>> allType = new LinkedList<LinkedList<HelpTip>>();
			while(keys.hasNext()){
				icon = keys.next();
				tips = jsonNearActions.getJSONArray(icon);
				if (TextUtils.equals("win_help_dianhua", icon)) {
					if (!CallManager.getInstance().hasRemoteProcTool()) {
						continue;
					}
				} else if(TextUtils.equals("win_help_daohang",icon)){
					if(!"".equals(NavManager.getInstance().getDisableResaon())){//?????????????????????
						continue;
					}
				} else if (TextUtils.equals("win_help_music", icon)) {
					if(!hasMusicTool()){
						continue;
					}
				} else if(TextUtils.equals("win_help_diantai", icon)){
					if(!hasRadioTool()){
						continue;
					}
				}
				LinkedList<HelpTip> currentType = new LinkedList<HelpTip>();
				for (int i = 0; i < tips.length(); i++) {
					if (HelpTipsPreferenceUtil.getInstance().getInt(tips.getString(i),-1) >= 3) {
						continue;
					}
					tip = tips.getString(i);
					helpTip = new HelpTip(icon, tip, UUID.randomUUID());
					currentType.add(helpTip);
				}
				Collections.sort(currentType);
				allType.add(currentType);
			}
			int i = 0;
			while(allType.size() > 0){
				helpTips.add(allType.get(i).getFirst());
                LogUtil.d("load helptips residue:"+allType.get(i).getFirst().tip+"?????????"+allType.get(i).getFirst().icon);
				allType.get(i).removeFirst();
				if(allType.get(i).size() == 0){
					allType.remove(i);
					i--;
				}
				i++;
				if(i >= allType.size()){
					i = 0;
				}
			}
		} catch (JSONException e) {
			LogUtil.loge("loadHelpTipsFromSDK Exception:"+e.toString());
			return false;
		}
		return true;
	}

	/*
    * ???????????????????????????????????????loadHelpTips??????????????????????????????????????????????????????????????????
    * */
    private boolean initHelpTips(){
		String hitJson = NativeData
				.getResJson("RS_VOICE_USUAL_SPEAK_GRAMMAR_TIPS");
		helpTips = new ArrayList<HelpTip>();
    	if(loadHelpTipsFromSDK(hitJson)){
			return true;
		}
        ArrayList<HelpDetail> cacheHelpDetails = WinHelpDetailTops.getInstance().getCacheHelpDetails();
        if(!checkCacheHelpDetails(cacheHelpDetails)){
            return false;
        }
		helpTips.clear();
        if (cacheHelpDetails != null && cacheHelpDetails.size() > 0) {
            HelpTip helpTip;
            String tip, icon, type, strPackage, tool;
            ArrayList<HelpDetail.HelpDetailItem> tips;
            LinkedList<LinkedList<HelpTip>> allType = new LinkedList<LinkedList<HelpTip>>();
            for (int i = 0; i < cacheHelpDetails.size();i++) {
                if(cacheHelpDetails.get(i).openType == 1)
                    continue;
                LinkedList<HelpTip> currentType = new LinkedList<HelpTip>();
                type = cacheHelpDetails.get(i).name;
                icon = cacheHelpDetails.get(i).iconName;
                tips = cacheHelpDetails.get(i).detailItems;
                strPackage =  cacheHelpDetails.get(i).strPackage;
                tool = cacheHelpDetails.get(i).tool;
                //??????app??????????????????????????????????????????
                if (!TextUtils.isEmpty(strPackage)) {
                    if (strPackage.equals(HelpGuideManager.GUIDE_PACKAGE_NAME)) {
                        if (!HelpGuideManager.getInstance().hasGuideAnim()) {
                            continue;
                        }
                    }
                    else if (strPackage.equals(ServiceManager.WEBCHAT)) {
                        //???????????????????????????????????????????????????
                        if (!WeixinManager.getInstance().enableWeChat()) {
                            continue;
                        }
                    }
                    if (!PackageManager.getInstance().checkAppExist(strPackage)) {
                        continue;
                    }
                }

                if (!WinHelpDetailTops.checkHasTools(tool)) {
                    continue;
                }

                if (TextUtils.indexOf( type, "??????") >= 0) {
                    if (!CallManager.getInstance().hasRemoteProcTool()) {
                        continue;
                    }
                } else if (TextUtils.indexOf(type, "??????" )>=0 || TextUtils.indexOf(type, "??????" ) >= 0) {
                    for (int j = 0; j < tips.size(); j++) {
                        tip = tips.get(j).name;
                        if(HelpTipsPreferenceUtil.getInstance().getInt(tip,-1) >= 3){
                            continue;
                        }
                        helpTip = new HelpTip(icon, tip, UUID.randomUUID());
                        currentType.add(helpTip);
                    }
                    Collections.sort(currentType);
                    allType.add(currentType);
                    continue;
                }else if(TextUtils.indexOf(type ,"??????") >= 0){
                    if(!"".equals(NavManager.getInstance().getDisableResaon())){//?????????????????????
                        continue;
                    }
                } else if (TextUtils.indexOf( type, "??????") >= 0) {
                    if(!hasMusicTool()){
                        continue;
                    }
                }
                for (int j = 0; j < tips.size(); j++) {
                    tip = tips.get(j).name;
                    if(HelpTipsPreferenceUtil.getInstance().getInt(tip,-1) >= 3){
                        continue;
                    }
                    helpTip = new HelpTip(icon, tip, UUID.randomUUID());
                    currentType.add(helpTip);
                }
                Collections.sort(currentType);
                allType.add(currentType);
            }
            int i = 0;
            while(allType.size() > 0){
                if(allType.get(i).size() == 0){
                    break;
                }
                helpTips.add(allType.get(i).getFirst());
                LogUtil.d("load helptips residue:"+allType.get(i).getFirst().tip+"?????????"+allType.get(i).getFirst().icon);
                allType.get(i).removeFirst();
                if(allType.get(i).size() == 0){
                    allType.remove(i);
                    i--;
                }
                i++;
                if(i >= allType.size()){
                    i = 0;
                }
            }
        }else{
            LogUtil.logd("CacheHelpDetails = null" );
            return false;
        }
        return helpTips.size() >= 5;
    }

    /**
     * false ??????????????????????????????????????????????????????????????????
     * true  ??????????????????????????????????????????????????????
	 * ???????????????????????????????????????????????????????????????sp??????
     * @return
     */
    private boolean loadHelpTips() {
        if(helpTips == null){
            return initHelpTips();
        }
        if(helpTips.size() >= 5){
            return true;
        }
		String hitJson = NativeData
				.getResJson("RS_VOICE_USUAL_SPEAK_GRAMMAR_TIPS");
		helpTips.clear();
		HelpTipsPreferenceUtil.getInstance().clear();
		if(loadHelpTipsFromSDK(hitJson)){
			return true;
		}
		ArrayList<HelpDetail> cacheHelpDetails = WinHelpDetailTops.getInstance().getCacheHelpDetails();
		if(!checkCacheHelpDetails(cacheHelpDetails)){
			return false;
		}
        if (cacheHelpDetails != null && cacheHelpDetails.size() > 0) {
            HelpTip helpTip;
            String tip, icon, type, strPackage, tool;
            ArrayList<HelpDetail.HelpDetailItem> tips;
            LinkedList<LinkedList<HelpTip>> allType = new LinkedList<LinkedList<HelpTip>>();
            for (int i = 0; i < cacheHelpDetails.size();i++) {
                if(cacheHelpDetails.get(i).openType == 1)
                    continue;
                LinkedList<HelpTip> currentType = new LinkedList<HelpTip>();
                type = cacheHelpDetails.get(i).name;
                icon = cacheHelpDetails.get(i).iconName;
                tips = cacheHelpDetails.get(i).detailItems;
                strPackage =  cacheHelpDetails.get(i).strPackage;
                tool = cacheHelpDetails.get(i).tool;
                //??????app??????????????????????????????????????????
                if (!TextUtils.isEmpty(strPackage)) {
                    if (strPackage.equals(HelpGuideManager.GUIDE_PACKAGE_NAME)) {
                        if (!HelpGuideManager.getInstance().hasGuideAnim()) {
                            continue;
                        }
                    }else if (strPackage.equals(ServiceManager.WEBCHAT)) {
                        //???????????????????????????????????????????????????
                        if (!WeixinManager.getInstance().enableWeChat()) {
                            continue;
                        }
                    }
                    if (!PackageManager.getInstance().checkAppExist(strPackage)) {
                        continue;
                    }
                }
                //????????????????????????
                if (!WinHelpDetailTops.checkHasTools(tool)) {
                    continue;
                }
                if (TextUtils.indexOf( type, "??????") >= 0) {
                    if (!CallManager.getInstance().hasRemoteProcTool()) {
                        continue;
                    }
                } else if (TextUtils.indexOf(type, "??????" )>=0 || TextUtils.indexOf(type, "??????" ) >= 0) {
                    for (int j = 0; j < tips.size(); j++) {
                        tip = tips.get(j).name;
                        if(HelpTipsPreferenceUtil.getInstance().getInt(tip,-1) >= 3){
                            continue;
                        }
                        helpTip = new HelpTip(icon, tip, UUID.randomUUID());

                        currentType.add(helpTip);
                    }
                    Collections.sort(currentType);
                    allType.add(currentType);
                    continue;
                }else if(TextUtils.indexOf(type ,"??????") >= 0){
                    if(!"".equals(NavManager.getInstance().getDisableResaon())){//?????????????????????
                        continue;
                    }
                } else if (TextUtils.indexOf( type, "??????") >= 0) {
                    if(!hasMusicTool()){
                        continue;
                    }
                }
                for (int j = 0; j < tips.size(); j++) {
                    tip = tips.get(j).name;
                    if(HelpTipsPreferenceUtil.getInstance().getInt(tip,-1) >= 3){
                        continue;
                    }
                    helpTip = new HelpTip(icon, tip, UUID.randomUUID());
                    currentType.add(helpTip);

                }
                Collections.sort(currentType);
                allType.add(currentType);
            }
            int i = 0;
            while(allType.size() > 0){
                if(allType.get(i).size() == 0){
                    break;
                }
                helpTips.add(allType.get(i).getFirst());
				LogUtil.d("??:"+allType.get(i).getFirst().tip+"?????????"+allType.get(i).getFirst().icon);
                allType.get(i).removeFirst();
                if(allType.get(i).size() == 0){
                    allType.remove(i);
                    i--;
                }
                i++;
                if(i >= allType.size()){
                    i = 0;
                }
            }
        }else{
            LogUtil.logd("CacheHelpDetails = null" );
            return false;
        }
        return helpTips.size() >= 5;
    }

    /**
     * false ????????????????????????????????????????????????????????????
     * true ??????????????????????????????????????????????????????????????????
     * @return
     */
    public boolean checkCacheHelpDetails(ArrayList<HelpDetail> cacheHelpDetails){
        int count = 0;
        if(cacheHelpDetails == null){
            return false;
        }
        for(int i = 0; i < cacheHelpDetails.size(); i++){
            if(cacheHelpDetails.get(i).openType == 1)
                continue;
            for(int j = 0; j < cacheHelpDetails.get(i).detailItems.size(); j++){
                count++;
                if(count >= 5){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasMusicTool(){
        IMediaTool tool = MusicPriorityChooser.getInstance().getMediaTool(null);
        if (tool != null) {
            LogUtil.d("load helptips MusicTool exist");
            return true;
        }
        LogUtil.d("load helptips MusicTool not exist");
        return false;
    }

    public boolean hasRadioTool(){
        IMediaTool tool = AudioPriorityChooser.getInstance().getMediaTool(null);
        if (tool != null) {
            LogUtil.d("load helptips RadioTool exist");
            return true;
        }
        LogUtil.d("load helptips RadioTool not exist");
        return false;
    }

    /**
     * 1.????????????????????????
     * 2.????????????????????????
     * @return
     */
    public boolean isNotNullHelpTips(){
        if(!TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_IS_SHOW_HELP_TIPS,true)){
            return false;
        }
        return loadHelpTips();
    }

    private String getDefValue(int len) {
        String defValue = "";
        for (int i = 0; i < len; i++) {
            defValue += "0";
        }
        return defValue;
    }

    private boolean isHideHelpTips(Set<String> hideHelpTips, String key) {
        if (hideHelpTips != null) {
            for (String hideHelpKey : hideHelpTips) {
                if (TextUtils.equals(hideHelpKey, key)) {
                    return true;
                }
            }
        }
        return false;
    }

    int mCurrentIndex = 0;

    public JSONArray getHelpTips() {
        JSONArray jsonHelpTips = new JSONArray();
        int index = mCurrentIndex;
        int len = 5 <= helpTips.size() ? 5 : helpTips.size();
        for (int i = 0; i < len; i++) {
            if (index >= helpTips.size()) {
                index = 0;
            }
            jsonHelpTips.put(createHelpTip(helpTips.get(index).icon, helpTips.get(index).tip));
            index++;
        }
        mCurrentIndex++;
        if (mCurrentIndex >= helpTips.size()) {
            mCurrentIndex = 0;
        }
        return jsonHelpTips;
    }

    ///////////////////////////////////////???????????????????????????////////////////////////////////

    //???????????????????????????????????????
    private static final String DEF_TIME = "2017-12-25 00:00:00";
    //??????????????????????????????
    private static final int SHOW_DESC_COUNT = 2;

    private String mImgPath;
    private Integer mShowType;
    private boolean canOpenDetail = true;
    private List<HelpDetail> mCacheHelpDetails;
    private List<HelpDetail> helpDetails;
    private boolean isShowTips;
    private HelpDetail mSelectHelpDetail;
    private HelpDetail mGuideHelpDetail;
    private int typeNetHideIndex;
    private int typeNetGrayIndex;
    private String tipS = "";
    private boolean isFromFile;

    /**
     * ?????????????????????
     */
    public void initHelpData() {
        isFromFile = false;
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

        valueHelpList();
    }

    /**
     * ???????????????json
     *
     * @return
     */
    private String getHelpDetailJson() {
        if (helpDetails != null && !helpDetails.isEmpty()) {
            JSONArray jsonArray = new JSONArray();
            for (HelpDetail detail : helpDetails) {
                jsonArray.put(detail.toString());
            }
            return jsonArray.toString();
        }
        return "";
    }

    /**
     * ??????????????????????????????
     *
     * @param path ??????????????????
     * @return
     */
    public boolean initHelpFromFile(String path, boolean isDefault) {
        boolean isSucess = false;
        try {
//            isFromFile = true;
            HelpDetail helpDetail;
            ArrayList<HelpDetail> tmpDetails = new ArrayList<HelpDetail>();
            JSONBuilder jBuilder = new JSONBuilder(new File(path + File.separator + "help.txt"));
            JSONArray helpDetailsArray;
            helpDetailsArray = jBuilder.getVal("help", JSONArray.class, null);
            mImgPath = path + File.separator + jBuilder.getVal("imgDir", String.class);
            JSONArray helpDetailsItemArray;
            JSONArray helpDetailsImgArray;
            JSONBuilder helpDetailJson;
            JSONBuilder helpDetailItemJson;
            JSONBuilder helpDetailImgJson;

            //???????????????,0?????????????????????1????????????????????????????????????2????????????????????????????????????
            mShowType = jBuilder.getVal("type", Integer.class, SHOW_TYPE_NORMAL);
            canOpenDetail = jBuilder.getVal("showDetail", Integer.class, 1) != 0
                    && !hasThirdImpl()
                    && !(WinManager.getInstance().isRecordWin2() && WinLayoutManager.getInstance().getHelpDetailListView() == null);
            if (jBuilder.getVal("showTag", Integer.class, 0) == 1) {//??????????????????new??????,0?????????,1??????
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
                helpDetailsItemArray = helpDetailJson.getVal("desc", JSONArray.class, null);

                for (int j = 0; j < helpDetailsItemArray.length(); j++) {
                    helpDetailItemJson = new JSONBuilder(helpDetailsItemArray.getJSONObject(j));
                    HelpDetail.HelpDetailItem helpDetailItem = new HelpDetail.HelpDetailItem();
                    helpDetailItem.id = helpDetailItemJson.getVal("id", String.class);
                    helpDetailItem.name = "???" + helpDetailItemJson.getVal("name", String.class) + "???";
                    helpDetailItem.time = helpDetailItemJson.getVal("time", String.class);
//						helpDetailItem.isNew = isNew(HelpPreferenceUtil.getInstance().getString(helpDetailItem.name, DEF_TIME), helpDetailItem.time);
                    helpDetailItem.netType = helpDetailItemJson.getVal("netType", Integer.class, CMD_NET_TYPE_MIX);

                    helpDetail.detailItems.add(helpDetailItem);
                }

                helpDetail.detailImgs = new ArrayList<HelpDetail.HelpDetailImg>();
                helpDetailsImgArray = helpDetailJson.getVal("imgs", JSONArray.class, null);
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

    /**
     * ?????????????????????????????????????????????
     */
    private void valueHelpList() {
        String mHelpDetailName = HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HELP_DETAIL_NAME, "");
        boolean hasNet = NetworkManager.getInstance().hasNet();
        helpDetails = new ArrayList<HelpDetail>();
        HelpDetail helpDetail;
        for (int i = 0; i < mCacheHelpDetails.size(); i++) {
            //????????????????????????????????????????????????????????????????????????????????????????????????
            if (mShowType == SHOW_TYPE_NET_HIDE && !hasNet) {
                helpDetail = mCacheHelpDetails.get(i).clone();
                Iterator<HelpDetail.HelpDetailItem> items = helpDetail.detailItems.iterator();
                while (items.hasNext()) {
                    HelpDetail.HelpDetailItem helpDetailItem = items.next();
                    if (helpDetailItem.netType == CMD_NET_TYPE_NET) {
                        items.remove();
                    }
                }
                if (helpDetail.detailItems.size() == 0 && helpDetail.openType == CMD_OPEN_TYPE_NORMAL) {
                    continue;
                }
            } else {
                helpDetail = mCacheHelpDetails.get(i);
            }
            helpDetail.isNew = isNew(HelpPreferenceUtil.getInstance().getString(helpDetail.name, DEF_TIME), helpDetail.time);
            //????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            helpDetail.hasNet = (mShowType == SHOW_TYPE_NORMAL) || hasNet;

            //??????app??????????????????????????????????????????
            if (!TextUtils.isEmpty(helpDetail.strPackage)) {
                if (helpDetail.strPackage.equals(HelpGuideManager.GUIDE_PACKAGE_NAME)) {
                    if (!HelpGuideManager.getInstance().hasGuideAnim()) {
                        continue;
                    }
                }
                if (!PackageManager.getInstance().checkAppExist(helpDetail.strPackage)) {
                    continue;
                }
            }

            //????????????????????????
            if (!WinHelpDetailTops.checkHasTools(helpDetail.tool)) {
                continue;
            }

            helpDetail.intro = "";
            typeNetHideIndex = 0;
            typeNetGrayIndex = 0;

            if (isGuideHelp(helpDetail)) {
                mGuideHelpDetail = helpDetail.clone();
            }

            for (int j = 0; j < helpDetail.detailItems.size(); j++) {
                HelpDetail.HelpDetailItem helpDetailItem = helpDetail.detailItems.get(j);
                helpDetailItem.isNew = isNew(HelpPreferenceUtil.getInstance().getString(helpDetailItem.name, DEF_TIME), helpDetailItem.time);

                //????????????????????????????????????????????????
                if (helpDetailItem.isNew) {
                    helpDetail.isNew = true;
                }

                if (helpDetail.intros != null && helpDetail.intros.length != 0) {
                    for (int k = 0; k < helpDetail.intros.length; k++) {
                        if (TextUtils.equals("???" + helpDetail.intros[k] + "???", helpDetailItem.name)) {
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

            //??????????????????????????????????????????????????????????????????????????????
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

            //?????????????????????????????????
            if (!TextUtils.isEmpty(mHelpDetailName)) {
                if (TextUtils.equals(mHelpDetailName, helpDetail.name)) {
                    // ?????????????????????????????????????????????????????????????????????????????????????????????
                    mSelectHelpDetail = helpDetail.clone();
                    if (mSelectHelpDetail != null) {
                        Iterator<HelpDetail.HelpDetailItem> items = mSelectHelpDetail.detailItems.iterator();
                        while (items.hasNext()) {
                            HelpDetail.HelpDetailItem helpDetailItem = items.next();
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
            if (ProjectCfg.getNetModule() == 0 || ProjectCfg.hasNetModule()) { //???sim????????????????????????????????????
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

    private String valueHelpIntro(boolean hasNet, String intro, String name, int type, int netType) {
        //???????????????,0?????????????????????1????????????????????????????????????2????????????????????????????????????
        if (hasNet) {
            if (typeNetGrayIndex < SHOW_DESC_COUNT) {
                intro += name;
                intro += " ";
                typeNetGrayIndex++;
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
                    typeNetGrayIndex++;
                }
            }
        }
        return intro;
    }

    private boolean isNew(String lastTime, String currentTime) {
        boolean isNew = false;
        if (TextUtils.isEmpty(lastTime)) {
            isNew = true;
            if (TextUtils.isEmpty(currentTime)) {
                isNew = false;
            }
        } else {
            if (TextUtils.isEmpty(currentTime)) {
                isNew = false;
            } else {
                isNew = compareDate(lastTime, currentTime);
            }
        }
        return isNew;
    }

    /**
     * ???????????????????????????????????????
     *
     * @param helpDetail
     * @return
     */
    private boolean isGuideHelp(HelpDetail helpDetail) {
        return TextUtils.equals(helpDetail.lastName, "??????????????????") || TextUtils.equals(helpDetail.name, "??????????????????");
    }

    public void setWinType(int winType){
        this.mWinType = winType;
    }

    public void setEnableFullScreen(boolean enableFullScreen){
        this.mEnableFullScreen = enableFullScreen;
    }

    public void setEnableShowHelpQRCode(boolean enableShowHelpQRCode){
        this.mEnableShowHelpQRCode = enableShowHelpQRCode;
    }

    public boolean getEnableShowHelpQRCode(){
        return mEnableShowHelpQRCode;
    }

    /**
     * ???????????????????????????
     * @param enable
     */
    public void updateCloseIconState(boolean enable) {
        if (enableHelpListBackIcon()) {
            ConfigUtil.updateCloseIconState(enable);
        }
    }

    /**
     * ?????????????????????
     */
    public void resetCloseIconState() {
       if (enableHelpListBackIcon()) {
           ConfigUtil.resetCloseIconState();
       }
    }

    private boolean enableHelpListBackIcon(){
        if (WinManager.getInstance().checkUseRecordWin2() ) {
            return com.txznet.comm.ui.util.ConfigUtil.enableHelpListBackIcon();
        }
        return !WinManager.getInstance().hasThirdImpl();
    }
}
