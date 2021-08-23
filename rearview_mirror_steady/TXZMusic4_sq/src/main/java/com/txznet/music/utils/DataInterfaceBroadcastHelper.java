package com.txznet.music.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;

import com.txz.ui.audio.UiAudio.Resp_DataInterface;
import com.txznet.audio.player.SessionManager;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestRawCallBack;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static com.txznet.music.utils.DataInterfaceBroadcastHelper.RemoteNetListener.CODE_SUCCESS;


public class DataInterfaceBroadcastHelper {
    private static final String TAG = "music:process:";
    private static final String PROCESS_TAG = "Music:process:";
    private static final String KEY_AUDIO_ITEM = "KEY_AUDIO_ITEM";
    private final static int mChaosKey = 79;
    static Map<Integer, String> mMapSeqs = new ConcurrentHashMap<Integer, String>();//响应ID->请求ID
    static Map<String, Long> mMapseqToId = new ConcurrentHashMap<>();//请求ID->音频ID
    static Map<Long, RemoteNetListener> mMapListeners = new ConcurrentHashMap<Long, RemoteNetListener>();//音频ID->响应主体
    private static int mSeq = new Random().nextInt();
    private static byte[] mRandomKey = null;
//    private static String lstRequestRreprocess;//上次请求预处理的请求参数

    private static int getNextSeq() {
        ++mSeq;
        if (mSeq == 0) {
            ++mSeq;
        }
        return mSeq;
    }

    public static void initListeners() {
        if (AppLogic.isMainProcess()) {
//            initRandomKey();

            IntentFilter intentFilter = new IntentFilter(ACTION_REQDATA);
            GlobalContext.get().registerReceiver(new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent) {
                    String cmdString = intent.getStringExtra("cmd");
//					int seqReq = NetHelp.sendRequestByData(cmdString, decodeByRandomKey(intent.getByteArrayExtra("data")));
                    int seqReq = NetManager.getInstance().sendRequestToCore(cmdString, decodeByRandomKey(intent.getByteArrayExtra("data")), new RequestRawCallBack() {
                        @Override
                        public void onResponse(Resp_DataInterface respDataInterface) {
                            sendDataInterfaceResp(respDataInterface);
                        }

                        @Override
                        public void onError(String cmd, Error error) {
                            LogUtil.e(TAG + "cmd:" + cmd + " errorCode:" + error.getErrorCode());
                        }
                    });
//                    mMapSeqs.clear();
                    mMapSeqs.put(seqReq, intent.getStringExtra("seq"));
                    if (Constant.ISTEST) {
                        LogUtil.logd(TAG + AppLogic.getProcessName() + ",requestData," + Constant.GET_WAY + "/" + seqReq);
                    }
                }
            }, intentFilter);

            IntentFilter downloadFilter = new IntentFilter("com.txznet.music.action.DownloadComplete");
            GlobalContext.get().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // XXX:改成内存数据
                    try {
                        Audio audio = JsonHelper.toObject(Audio.class, new String(decodeByRandomKey(intent.getByteArrayExtra("data"))));
//                        audio.setSourceFrom("本地音乐");
                        audio.setLocal(true);
//                        audio.setPinyin(PinYinUtil.getPinYin(audio.getName()));
                        LogUtil.logd(TAG + "Download complete save to database:" + audio.toString());
                        DBManager.getInstance().saveLocalAudio(audio);
                        if (Constant.ISTEST) {
                            LogUtil.logd(TAG + AppLogic.getProcessName() + ",receive downloadComplete," + audio.getName());
                        }
                    } catch (Exception e) {
                        LogUtil.logw(TAG + "downloadComplete error," + decodeByRandomKey(intent.getByteArrayExtra("data")));
                    }
                }
            }, downloadFilter);

//            intentFilter = new IntentFilter(ACTION_PRELOAD);
//            GlobalContext.get().registerReceiver(new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    if (Constant.ISTEST) {
//                        LogUtil.logd(TAG + AppLogic.getProcessName() + ",receive preload request");
//                    }
//                    Audio nextAudio = (Audio) intent.getExtras().getSerializable(KEY_AUDIO_ITEM);
//                    SessionManager.preloadData(nextAudio);
//                }
//            }, intentFilter);
        } else {
            IntentFilter intentFilter = new IntentFilter(ACTION_RESPONSE_DATA);
            GlobalContext.get().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    // NetHelp.sendRequest(url, reqData)
                    String seqString = intent.getStringExtra("seq");
                    if (Constant.ISTEST) {
                        LogUtil.logd(TAG + AppLogic.getProcessName() + ",receive qq url response, id=" + seqString);
                    }
                    Long audioId = mMapseqToId.remove(seqString);
                    if (audioId != null) {
                        RemoteNetListener remoteNetListener = mMapListeners.get(audioId);
                        if (remoteNetListener != null && CODE_SUCCESS == remoteNetListener.response(intent.getIntExtra("code", 0), decodeByRandomKey(intent.getByteArrayExtra("data")))) {
                            mMapListeners.clear();
                        } else {
                            if (!mMapListeners.isEmpty()) {
                                LogUtil.logw(TAG + "can not find RespDataInterface seq audioId: " + seqString);
                            }
                        }
                    } else {
                        if (!mMapListeners.isEmpty()) {
                            LogUtil.logw(TAG + "can not find RespDataInterface seq: " + seqString);
                        }
                    }
                }
            }, intentFilter);
        }


    }

    private static final String ACTION_RESPONSE_DATA = "com.txznet.music.action.RespDataInterface";

    public static boolean sendDataInterfaceResp(Resp_DataInterface resp) {
        String seqBroadcast = mMapSeqs.remove(resp.uint32Seq);
        if (seqBroadcast == null) {
            if (!mMapSeqs.isEmpty()) {
                LogUtil.logw(TAG + "can not find DataInterface seq: " + resp.uint32Seq);
            }
            return false;
        }
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + AppLogic.getProcessName() + ",receive qq url ,id=" + resp.uint32Seq + ",errorCode=" + resp.uint32ErrCode);
        }
        Intent intent = new Intent();
        intent.putExtra("data", encodeByRandomKey(resp.strData));
//		intent.putExtra("data", (resp.strData));
        intent.putExtra("cmd", resp.strCmd);
        intent.putExtra("seq", seqBroadcast);
        intent.putExtra("code", resp.uint32ErrCode);

        intent.setAction(ACTION_RESPONSE_DATA);
        GlobalContext.get().sendBroadcast(intent);
        return true;
    }

    private static final String ACTION_REQDATA = "com.txznet.music.action.ReqDataInterface";

    public static void sendDataInterfaceReq(String cmd, byte[] reqData, long audioId, RemoteNetListener listener) {
        Intent intent = new Intent();
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + AppLogic.getProcessName() + ",request qq url:" + new String(reqData));
        }
//		} else {
//			LogUtil.logd(TAG + "sendDataInterfaceReq:" + cmd);
//		}
        intent.putExtra("data", encodeByRandomKey(reqData));
        intent.putExtra("cmd", cmd);
        String seq = AppLogic.getProcessName() + "_" + getNextSeq();
        mMapseqToId.put(seq, audioId);
        mMapListeners.put(audioId, listener);

        intent.putExtra("seq", seq);
//        intent.putExtra("newRequest", isNewRequest);

        intent.setAction(ACTION_REQDATA);
        GlobalContext.get().sendBroadcast(intent);
    }
//
//    private static final String ACTION_PRELOAD = "com.txznet.music.action.PRELOAD_NEXT_AUDIO";
//
//    public static void sendStartPreloadNextAudioInfo(Audio audio) {
//        if (audio == null) {
//            LogUtil.logd(Constant.PRELOAD_TAG + " can't send broadcast to other process ,next audio is null");
//            return;
//        } else {
//            if (Constant.ISTEST) {
//                LogUtil.logd(TAG + AppLogic.getProcessName() + ",start preload request ,name=" + audio.getName());
//            }
//        }
//        Intent intent = new Intent();
//        intent.setAction(ACTION_PRELOAD);
//        intent.putExtra(KEY_AUDIO_ITEM, audio);
//        GlobalContext.get().sendBroadcast(intent);
//    }


    // ///////////////////////////////////////////////////////////////////////////

    public static void sendDownloadBroadcast(Audio tempAudio) {
        Intent intent = new Intent();
        intent.putExtra("data", encodeByRandomKey(JsonHelper.toJson(tempAudio).getBytes()));
        intent.setAction("com.txznet.music.action.DownloadComplete");
        GlobalContext.get().sendBroadcast(intent);
    }

    private static byte[] encodeRandomKey(int pid, byte[] key) {
        if (null == key) {
            return null;
        }
        byte[] ret = new byte[key.length];
        for (int i = 0; i < ret.length; ++i) {
            ret[i] = (byte) (mChaosKey ^ pid ^ key[i]);
        }
        return ret;
    }

    public static byte[] getRandomKey() {
        if (!AppLogic.isMainProcess()) {
            return null;
        }
        int pid = Process.myPid();
        return encodeRandomKey(pid, mRandomKey);
    }

    public static void setRandomKey(int pid, byte[] key) {
        if (AppLogic.isMainProcess() == false) {
            mRandomKey = encodeRandomKey(pid, key);
        }
    }

    public static void initRandomKey() {
        if (mRandomKey == null && AppLogic.isMainProcess()) {
            mRandomKey = new byte[32 + new Random().nextInt(32)];
            new Random().nextBytes(mRandomKey);
        }
    }

    // 对称编码
    public static byte[] encodeByRandomKey(byte[] data) {
        LogUtil.loge("data:" + data.length + "/" + (mRandomKey != null ? new String(mRandomKey) : "null"));
        if (mRandomKey != null) {
            int offset = (data.length + mRandomKey.length) % mChaosKey;
            for (int i = 0; i < data.length; ++i) {
                data[i] ^= mRandomKey[(i + offset) % mRandomKey.length];
            }
        }
        return data;
    }

    public static byte[] decodeByRandomKey(byte[] data) {
        // 可逆算法
        return encodeByRandomKey(data);
    }

    public static interface RemoteNetListener {
        public final static int CODE_SUCCESS = 0;
        public final static int CODE_FAILURE = 1;

        /**
         * 是否成功消费该事件
         *
         * @param code 返回的错误码
         * @param data 数据中还含有错误码
         * @return 这一次的响应是否是成功的响应
         */
        public int response(int code, byte[] data);
    }
}
