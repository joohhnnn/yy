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

    public static final int TYPE_OPEN_FROM_CLICK = 0;//点击打开帮助
    public static final int TYPE_OPEN_FROM_VOICE = 1;//声控打开帮助
    public static final int TYPE_OPEN_FROM_BACK = 2;//从三级界面返回
    public static final int TYPE_OPEN_FROM_SDK = 4;//从SDK打开帮助

    public static final int TYPE_CLOSE_FROM_CLICK = 0;//点击关闭帮助
    public static final int TYPE_CLOSE_FROM_VOICE = 1;//声控关闭帮助
    public static final int TYPE_CLOSE_FROM_DETAIL = 2;//进入三级界面
    public static final int TYPE_CLOSE_FROM_OTHER = 3;//其他原因关闭帮助界面

    public static final int TYPE_SNAP_PAGE_FROM_CLICK = 0;//点击翻页
    public static final int TYPE_SNAP_PAGE_FROM_VOICE = 1;//声控翻页
    public static final String KEY_OPEN_FROM_SDK = "sdk";//调用sdk打开帮助

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
        // 注册需要处理的事件
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
				//暂时不处理
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
                            //下载图片
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
     * 当接收到帮助界面下发的时候
     *
     * @param zipPath
     */
    public void onDownloadFile(String zipPath) {
        //校验文件的完整性
        //备份之前的帮助文件
        String path = HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HELP_FILE_PATH, FilePathConstants.DEFAULT_HELP_FILE_DIR);
        File oldHelpFile = new File(path);
        File backFile = new File(FilePathConstants.DEFAULT_HELP_FILE_BACKUP_DIR);
        if (oldHelpFile.exists()) {
            //删除之前备份的帮助文件
            FileUtil.removeDirectory(backFile);
            //备份当前帮助文件
            oldHelpFile.renameTo(backFile);
            //删除之前的帮助文件
            FileUtil.removeDirectory(oldHelpFile);
        }
        //解压当前帮助文件到指定目录
        UnZipUtil.getInstance().UnZip(zipPath, path);
        //配置显示小红点
        showHelpNewTag(true);
        //更新配置帮助存放路径
        HelpPreferenceUtil.getInstance().setString(HelpPreferenceUtil.KEY_HELP_FILE_PATH, path);
        //是否跳转到指定的三级界面

        //TODO 复制帮助zip文件到help目录下,是否存在误删了帮助文件的情况
	}

    /**
     * 下载文件
     * @param url 链接
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
        task.bForbidUseReservedSpace = true; // 禁止使用预留空间
        JNIHelper.sendEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_REQ, task);
        return taskId;
    }

    /**
     * 获取core中的版本号
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
            //其他不需要处理了
        }

    }

    /**
     * 校验core的版本是否在帮助的版本区间，不在的时候删除帮助
     * 先校验下发的帮助
     * TODO 是否需要考虑方案商内置的帮助
     */
    public void checkHelpVersion() {

        JSONBuilder mNewHelpJson = new JSONBuilder(new File(FilePathConstants.DEFAULT_HELP_FILE_PATH));
        //默认-1的时候，认为是之前放出去的帮助，后续的帮助必须填支持的最低和最高的版本，版本不对，不更新
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
                //解压当前帮助文件到临时目录
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

                //默认-1的时候，认为是之前放出去的帮助，后续的帮助必须填支持的最低和最高的版本，版本不对，不更新
                Integer minVer = mNewHelpJson.getVal("minVer", Integer.class, -1);
                Integer maxVer = mNewHelpJson.getVal("maxVer", Integer.class, -1);
                int curVer = getCurVersion();
                if ((minVer != -1 && curVer < minVer) || (maxVer != -1 && curVer > maxVer)) {
                    LogUtil.loge("version error , minVer: " + minVer + " ,maxVer: " + maxVer + " ,curVer: " + curVer);
                    doUpdateFail();
                    return;
                }

                int updateType = mNewHelpJson.getVal("update", Integer.class, 0);//0覆盖模式，1追加模式

                switch (updateType) {
                    case 1: {//增量更新模式
                        if (isHelpAvailable(FilePathConstants.DEFAULT_HELP_FILE_PATH)) {
                            FileUtil.copyFiles(FilePathConstants.DEFAULT_HELP_FILE_DIR, FilePathConstants.DEFAULT_HELP_FILE_BACKUP_DIR);
                            boolean isUpdate = doUpdate(mNewHelpJson, mHelpJson);
                            if (isUpdate) {
                                FileUtil.copyFiles(FilePathConstants.DEFAULT_HELP_FILE_TEMP_DIR, FilePathConstants.DEFAULT_HELP_FILE_DIR);
                                //TODO 需要在生成之后对新的json进行判断，然后确定要不要修改显示
                                if (mNewHelpJson.getVal("showNew", Integer.class, 0) == 1) {//是否需要显示小红点,0不显示,1显示
                                    showHelpNewTag(true);
                                } else {
                                    showHelpNewTag(false);
                                }

                                if (mNewHelpJson.getVal("showTag", Integer.class, 0) == 1) {//是否需要显示new标签,0不显示,1显示
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
                        }//如果下发增量模式时，之前没有下发过的话就不处理
                        FileUtil.removeDirectory(tmpFile);
                    }
                    break;
//					case 2:{//指定修改模式
//
//					}
//					break;
                    //其他更新类型，按照覆盖处理
                    case 0:
                    default: {
                        if (tmpFile.exists()) {
                            if (isHelpAvailable(FilePathConstants.DEFAULT_HELP_FILE_PATH)) {
                                //删除之前备份的帮助文件
                                FileUtil.removeDirectory(backFile);
                                //备份当前帮助文件
                                mHelpFile.renameTo(backFile);
                            } else {
                                FileUtil.removeDirectory(mHelpFile);
                            }
                            tmpFile.renameTo(mHelpFile);
                            //TODO 需要在生成之后对新的json进行判断，然后确定要不要修改显示
                            if (mNewHelpJson.getVal("showNew", Integer.class, 0) == 1) {//是否需要显示小红点,0不显示,1显示
                                showHelpNewTag(true);
                            } else {
                                showHelpNewTag(false);
                            }

                            if (mNewHelpJson.getVal("showTag", Integer.class, 0) == 1) {//是否需要显示new标签,0不显示,1显示
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
                //TODO 帮助更新出现异常
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
     * 增量升级的时候调用
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
     * 显示帮助小红点
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
     * 指定打开帮助的时候，直接打开第三级界面
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
     * 请求帮助界面二维码数据
     */
    public void requestHelpQRCodeData(){
        LogUtil.d("QRCode SUBEVENT_REQ_GET_RECOMMEND_INFO");
        UiEquipment.Req_Recommend_Info req_recommend_info = new UiEquipment.Req_Recommend_Info();
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_RECOMMEND_INFO, req_recommend_info);
    }

    /**
     * 获取当前帮助的版本号
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
     * 当登陆成功的时候去请求帮助是否有更新
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
        } else if ("txz.help.ui.qrcode".equals(command)) {//二级界面
            if (data != null && data.length > 0) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                showQRCodeDialog(jsonBuilder);
            }
        } else if ("txz.help.ui.detail.qrcode".equals(command)) {//三级界面
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
     * 放大二维码
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
     * 放大二维码-来自详情页
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
     * 上报详情页点开二维码
     */
    public void reportHelpDetailQRCode(){
        ReportUtil.doReport(new ReportUtil.Report.Builder()
                .setType("helpDetailQRCode")
                .setAction("open")
                .buildCommReport());
    }

    /**
     * 上报帮助页点开二维码
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
     * 如果是第三方的帮助界面，拿不到数据，不跳转到帮助界面
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
     ****************以下是用户辅助系统相关帮助界面上的内容**************************
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
     * 全文匹配语义词
     * @param text 语义文本
	 *   @param checkAll 语义文本,为true则进行全文匹配，为false则以语义文本作为子串进行匹配
     */
    public void checkSpeakHelpTips(String text, boolean checkAll) {
       if(helpTips == null){
            initHelpTips();
       }
       //使用过程中引导语不足时加载
       if(helpTips.size() < 5){
           //若重新加载引导语之后还是不符合条件，则不做全文匹配。
           if(!loadHelpTips()){
               return;
           }
       }
       if(TextUtils.isEmpty(text)){
           return;
       }
       if(checkAll){
		   for(int i = 0; i <helpTips.size(); i++){
			   String tip = helpTips.get(i).tip.replaceAll("“","").replaceAll("”","");
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
					   //命中出队后，需要重新加载排序，以免下一轮轮播出现重复的类目
					   initHelpTips();
				   }
				   break;
			   }
		   }
	   }else{
		   for(int i = 0; i <helpTips.size(); i++){
			   String tip = helpTips.get(i).tip.replaceAll("“","").replaceAll("”","");
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
					   //命中出队后，需要重新加载排序，以免下一轮轮播出现重复的类目
					   initHelpTips();
				   }
				   break;
			   }
		   }
	   }

    }


    ArrayList<HelpTip> helpTips = null;

	/**
	 * false SDK外放设置的轮播引导语所拥有的语音指令数量不足五条
	 * true SDK外放设置的轮播引导语所拥有的语音指令数量大于等于五条
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
					if(!"".equals(NavManager.getInstance().getDisableResaon())){//没安装导航工具
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
                LogUtil.d("load helptips residue:"+allType.get(i).getFirst().tip+"图标："+allType.get(i).getFirst().icon);
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
    * 引导提示语初始化，此接口与loadHelpTips区别在于，这个是首次初始化，另外一个是重载。
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
                //检查app是否安装，没有的话不显示帮助
                if (!TextUtils.isEmpty(strPackage)) {
                    if (strPackage.equals(HelpGuideManager.GUIDE_PACKAGE_NAME)) {
                        if (!HelpGuideManager.getInstance().hasGuideAnim()) {
                            continue;
                        }
                    }
                    else if (strPackage.equals(ServiceManager.WEBCHAT)) {
                        //判断微信不可用的时候不展示微信帮助
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

                if (TextUtils.indexOf( type, "电话") >= 0) {
                    if (!CallManager.getInstance().hasRemoteProcTool()) {
                        continue;
                    }
                } else if (TextUtils.indexOf(type, "聊天" )>=0 || TextUtils.indexOf(type, "闲聊" ) >= 0) {
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
                }else if(TextUtils.indexOf(type ,"导航") >= 0){
                    if(!"".equals(NavManager.getInstance().getDisableResaon())){//没安装导航工具
                        continue;
                    }
                } else if (TextUtils.indexOf( type, "音乐") >= 0) {
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
                LogUtil.d("load helptips residue:"+allType.get(i).getFirst().tip+"图标："+allType.get(i).getFirst().icon);
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
     * false 所需要展示的指令加载不符合条件，总数不足五条
     * true  所需要展示的指令加载数量大于等于五条
	 * 谨慎调用此接口，因为此接口会同时清除命中的sp文本
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
                //检查app是否安装，没有的话不显示帮助
                if (!TextUtils.isEmpty(strPackage)) {
                    if (strPackage.equals(HelpGuideManager.GUIDE_PACKAGE_NAME)) {
                        if (!HelpGuideManager.getInstance().hasGuideAnim()) {
                            continue;
                        }
                    }else if (strPackage.equals(ServiceManager.WEBCHAT)) {
                        //判断微信不可用的时候不展示微信帮助
                        if (!WeixinManager.getInstance().enableWeChat()) {
                            continue;
                        }
                    }
                    if (!PackageManager.getInstance().checkAppExist(strPackage)) {
                        continue;
                    }
                }
                //检查工具是否存在
                if (!WinHelpDetailTops.checkHasTools(tool)) {
                    continue;
                }
                if (TextUtils.indexOf( type, "电话") >= 0) {
                    if (!CallManager.getInstance().hasRemoteProcTool()) {
                        continue;
                    }
                } else if (TextUtils.indexOf(type, "聊天" )>=0 || TextUtils.indexOf(type, "闲聊" ) >= 0) {
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
                }else if(TextUtils.indexOf(type ,"导航") >= 0){
                    if(!"".equals(NavManager.getInstance().getDisableResaon())){//没安装导航工具
                        continue;
                    }
                } else if (TextUtils.indexOf( type, "音乐") >= 0) {
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
				LogUtil.d("·:"+allType.get(i).getFirst().tip+"图标："+allType.get(i).getFirst().icon);
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
     * false 帮助列表缓存所拥有的语音指令数量不足五条
     * true 帮助列表缓存所拥有的语音指令数量大于等于五条
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
     * 1.是否支持帮助指令
     * 2.帮助指令是否为空
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

    ///////////////////////////////////////以下是帮助数据解析////////////////////////////////

    //默认的时间和默认的时间格式
    private static final String DEF_TIME = "2017-12-25 00:00:00";
    //第一页显示的提示条数
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
     * 初始化帮助数据
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
     * 帮助数据的json
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
     * 从文件中读取帮助信息
     *
     * @param path 文件父文件夹
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

            //显示的类型,0表示全部亮显，1表示没网时在线命令灰显，2表示没网时在线命令不显示
            mShowType = jBuilder.getVal("type", Integer.class, SHOW_TYPE_NORMAL);
            canOpenDetail = jBuilder.getVal("showDetail", Integer.class, 1) != 0
                    && !hasThirdImpl()
                    && !(WinManager.getInstance().isRecordWin2() && WinLayoutManager.getInstance().getHelpDetailListView() == null);
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
                helpDetailsItemArray = helpDetailJson.getVal("desc", JSONArray.class, null);

                for (int j = 0; j < helpDetailsItemArray.length(); j++) {
                    helpDetailItemJson = new JSONBuilder(helpDetailsItemArray.getJSONObject(j));
                    HelpDetail.HelpDetailItem helpDetailItem = new HelpDetail.HelpDetailItem();
                    helpDetailItem.id = helpDetailItemJson.getVal("id", String.class);
                    helpDetailItem.name = "“" + helpDetailItemJson.getVal("name", String.class) + "”";
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
     * 从加载的缓存中获取帮助详情信息
     */
    private void valueHelpList() {
        String mHelpDetailName = HelpPreferenceUtil.getInstance().getString(HelpPreferenceUtil.KEY_HELP_DETAIL_NAME, "");
        boolean hasNet = NetworkManager.getInstance().hasNet();
        helpDetails = new ArrayList<HelpDetail>();
        HelpDetail helpDetail;
        for (int i = 0; i < mCacheHelpDetails.size(); i++) {
            //隐藏模式的话，没网情况下去掉列表的数据源，如果全部去掉了就不显示
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
            //当是默认显示类型的时候，就无须判断网络了；隐藏模式的话，数据源已经去掉了；在详情界面就只有灰显模式用到了网络判断
            helpDetail.hasNet = (mShowType == SHOW_TYPE_NORMAL) || hasNet;

            //检查app是否安装，没有的话不显示帮助
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

            //检查工具是否存在
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

    private String valueHelpIntro(boolean hasNet, String intro, String name, int type, int netType) {
        //显示的类型,0表示全部亮显，1表示没网时在线命令灰显，2表示没网时在线命令不显示
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
     * 当前条目是否是语音使用手册
     *
     * @param helpDetail
     * @return
     */
    private boolean isGuideHelp(HelpDetail helpDetail) {
        return TextUtils.equals(helpDetail.lastName, "语音使用手册") || TextUtils.equals(helpDetail.name, "语音使用手册");
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
     * 更新返回图标的状态
     * @param enable
     */
    public void updateCloseIconState(boolean enable) {
        if (enableHelpListBackIcon()) {
            ConfigUtil.updateCloseIconState(enable);
        }
    }

    /**
     * 还原按钮的状态
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
