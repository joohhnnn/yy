package com.txznet.txz.module.device;

import android.os.Environment;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.UrlUtil;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

import static com.txz.ui.equipment.UiEquipment.SUBEVENT_REQ_INIT_SUCCESS;
import static com.txz.ui.equipment.UiEquipment.SUBEVENT_RESP_BIND_OA_PIC;
import static com.txz.ui.equipment.UiEquipment.SUBEVENT_RESP_GET_BIND_WX_URL;

public class BindDeviceManager extends IModule {

    private static BindDeviceManager sBindDeviceManager = new BindDeviceManager();

    private BindDeviceManager() {
        RecorderWin.OBSERVABLE.registerObserver(new RecorderWin.StatusObervable.StatusObserver() {

            @Override
            public void onShow() {
            }

            @Override
            public void onDismiss() {
                TtsManager.getInstance().cancelSpeak(mTtsTaskId);
            }
        });
    }
    private int mTtsTaskId;

    public static BindDeviceManager getInstance() {
        return sBindDeviceManager;
    }

    @Override
    public int initialize_BeforeStartJni() {
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, SUBEVENT_RESP_GET_BIND_WX_URL);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, SUBEVENT_RESP_BIND_OA_PIC);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, SUBEVENT_REQ_INIT_SUCCESS);
        return super.initialize_BeforeStartJni();
    }

    public boolean handleResult(JSONObject json) {
        String answer = json.getString("answer");
        if (TextUtils.isEmpty(answer)) {
            answer = "违章提醒，洗车保养等服务和特惠活动，关注公众号车车互联即可享受";
        }
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject();
            jsonObject.put("type", 9);
            jsonObject.put("qrCode", mBindUrl);
            if (new File(mImagePath).exists()) {
                jsonObject.put("imageUrl", mImagePath);
                speakAndShow(answer, jsonObject);
            } else if (new File(mBackupImagePath).exists()) {
                jsonObject.put("imageUrl", mBackupImagePath);
                speakAndShow(answer, jsonObject);
            } else {
                LogUtil.e("skyward image not exist");
            }
            ReportUtil.doReport(new ReportUtil.Report.Builder().setType("show_bind_device_qrcode")
                    .putExtra("time", System.currentTimeMillis())
                    .setSessionId().buildCommReport());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void speakAndShow(String answer, org.json.JSONObject jsonObject) {
        RecorderWin.showUserText();
        mTtsTaskId = TtsManager.getInstance().speakText(answer, new TtsUtil.ITtsCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess();
                AsrManager.getInstance().start();
            }
        });
        RecorderWin.showData(jsonObject.toString());
    }


    private static final String KEY_BIND_DEVICE_IMAGE_CRC32 = "keyBindDeviceImageCRC32";

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        if (eventId == UiEvent.EVENT_ACTION_EQUIPMENT) {
            if (subEventId == SUBEVENT_REQ_INIT_SUCCESS) {
                LogUtil.d("skyward request bind device qrcode");
                UiEquipment.Req_GetBindWxUrl reqGetBindWxUrl = new UiEquipment.Req_GetBindWxUrl();
                reqGetBindWxUrl.strChannelNo = "txzBindDeviceQrCode";//不要下划线，不要点
                JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_BIND_WX_URL, reqGetBindWxUrl);

                EquipmentManager.Req_GetBindWXUrl reqGetImage = new EquipmentManager.Req_GetBindWXUrl();
                JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_BIND_OA_PIC, reqGetImage);
            } else if (subEventId == UiEquipment.SUBEVENT_RESP_GET_BIND_WX_URL) {
                UiEquipment.Resp_GetBindWxUrl res = null;
                try {
                    res = UiEquipment.Resp_GetBindWxUrl
                            .parseFrom(data);
                    if (TextUtils.isEmpty(res.strChannelNo)) {
                        LogUtil.d("skyward: no ChannelNo");
                        return super.onEvent(eventId, subEventId, data);
                    }
                    if (res.bOk) {
                        mBindUrl = res.strBindWxUrl;
                        LogUtil.d("skyward: mBindUrl=" + mBindUrl);
                    } else {
                        LogUtil.d("skyward; no ok");
                    }
                } catch (InvalidProtocolBufferNanoException e) {
                    e.printStackTrace();
                }
            } else if (subEventId == SUBEVENT_RESP_BIND_OA_PIC) {
                try {

                    final EquipmentManager.Resp_GetBindWXUrl respGetImage = EquipmentManager.Resp_GetBindWXUrl.parseFrom(data);
                    LogUtil.d("skyward strBindOaPicUrl " + respGetImage.strBindOaPicUrl);
                    LogUtil.d("skyward strBindOaPicCrc32 " + respGetImage.strBindOaPicCrc32);
                    File noMediaFile = new File(mNoMediaPath);
                    if (!noMediaFile.getParentFile().exists()) {
                        noMediaFile.getParentFile().mkdirs();
                    }
                    if (!noMediaFile.exists()) {
                        noMediaFile.createNewFile();
                    }
                    if (TextUtils.isEmpty(respGetImage.strBindOaPicUrl)) {
                        return super.onEvent(eventId, subEventId, data);
                    }

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            File file = new File(mImagePath);
                            if (PreferenceUtil.getInstance().getString(KEY_BIND_DEVICE_IMAGE_CRC32, "").equals(respGetImage.strBindOaPicCrc32) && file.exists()) {
                                LogUtil.d("skyward image has been downloaded");
                                return;
                            }
                            InputStream inputStream = null;
                            FileOutputStream outputStream = null;
                            try {
                                BaseImageDownloader baseImageDownloader = new BaseImageDownloader(GlobalContext.get());
                                UrlUtil.ConnectInfo connectInfo = UrlUtil.parseUrl(respGetImage.strBindOaPicUrl);
                                inputStream = baseImageDownloader.getStream(respGetImage.strBindOaPicUrl, connectInfo);
                                if (!file.getParentFile().exists()) {
                                    file.getParentFile().mkdirs();
                                }
                                if (!file.exists()) {
                                    file.createNewFile();
                                }
                                outputStream = new FileOutputStream(file);
                                int read;
                                byte[] bytes = new byte[1024];
                                CRC32 crc32 = new CRC32();
                                while ((read = inputStream.read(bytes)) != -1) {
                                    crc32.update(bytes, 0, read);
                                    outputStream.write(bytes, 0, read);
                                }
                                LogUtil.d("skyward crc32" + crc32.getValue());
                                if (String.valueOf(crc32.getValue()).equals(respGetImage.strBindOaPicCrc32)) {
                                    PreferenceUtil.getInstance().setString(KEY_BIND_DEVICE_IMAGE_CRC32, respGetImage.strBindOaPicCrc32);
                                    FileUtil.copyFile(mImagePath, mBackupImagePath);
                                } else {
                                    file.delete();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    if (outputStream != null) {
                                        outputStream.close();
                                    }
                                    if (inputStream != null) {
                                        inputStream.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return super.onEvent(eventId, subEventId, data);
    }

    private String mBindUrl = null;
    private String mImagePath = mImageRootPath + "new.png";
    private String mBackupImagePath =  mImageRootPath + "old.png";
    private String mNoMediaPath =  mImageRootPath + ".nomedia";
    private static final String mImageRootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/txz/image/";
}
