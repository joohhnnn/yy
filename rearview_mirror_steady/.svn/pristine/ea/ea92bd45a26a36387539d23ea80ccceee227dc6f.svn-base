package com.txznet.audio.player;

import android.app.DownloadManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.SparseArray;

import com.google.gson.reflect.TypeToken;
import com.txznet.audio.ProcessMemoryMonitor;
import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.playerModule.bean.PlayItem;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.PlayItemManager;
import com.txznet.music.playerModule.logic.PlayerControlManager;
import com.txznet.music.util.BrokenThread;
import com.txznet.music.util.TimeUtils;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.Utils;
import com.txznet.reserve.service.ReserveService0;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RemoteAudioPlayer extends TXZAudioPlayer implements PlayerServiceConstants {
    private static final String TAG = "music:player:client:";
    private static SparseArray<RemoteAudioPlayer> sPlayArray = new SparseArray<>();
    private Audio mAudio;

    private boolean mPrepared = false;
    private static int service_id;

    private static Messenger mServiceMessenger = null;
    private static Messenger mClientMessenger = null;


    private static ProcessMemoryMonitor.MonitorProcess mMonitorProcess =
            new ProcessMemoryMonitor.MonitorProcess(Constant.PACKAGE_PLAYER, new ProcessMemoryMonitor.MonitorCallBack() {
                @Override
                public void onMemoryChange(Debug.MemoryInfo info) {
                    LogUtil.logd(TAG + "onMemoryMonitor:" + info.getTotalPss());
                }
            });

    private static ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            LogUtil.logd(TAG + " onServiceDisconnected");
            mServiceMessenger = null;
            service_id = -1;
            ProcessMemoryMonitor.getInstance().removeMonitorProcess(mMonitorProcess);
            onRemoteError();
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mServiceMessenger = new Messenger(service);
            LogUtil.logd(TAG + " onServiceConnected");
            service_id = Utils.getProcessIdByPkgName(Constant.PACKAGE_PLAYER);
            mClientMessenger = new Messenger(clientHandler);
            Message message = Message.obtain(null, CLIENT_ACTION_BIND);
            message.replyTo = mClientMessenger;
            sendMsgToService(message);
            ProcessMemoryMonitor.getInstance().removeMonitorProcess(mMonitorProcess);
            ProcessMemoryMonitor.getInstance().addMonitorProcess(mMonitorProcess);
        }
    };

    public static TXZAudioPlayer createAudioPlayer(Audio audio) {
        if (null != mServiceMessenger) {// 如果绑定成功
            try {
                return new RemoteAudioPlayer(audio);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.loge("[Audio][RemoteAudioPlayer]:createAudioPlayer:", e);
            }
        } else {
            initRemotePlayer();
        }
        return null;
    }

    private RemoteAudioPlayer(Audio audio) throws Exception {
        super(null, AudioManager.STREAM_MUSIC);
        this.mAudio = audio;
        // 判断是否绑定
        LogUtil.d(TAG + "create player " + this.hashCode());
        sPlayArray.put(this.hashCode(), this);
//	    session_id = mServiceMessenger.createAudioPlayer(JsonHelper.toJson(mAudio)
//			    .getBytes(), Process.myPid(), DataInterfaceBroadcastHelper
//			    .getRandomKey(), RemoteAudioPlayer.this.hashCode());
//	    int sessionId = mServiceMessenger.createAudioPlayer(JsonHelper.toJson(mAudio)
//			    .getBytes(), Process.myPid(), DataInterfaceBroadcastHelper
//			    .getRandomKey(), RemoteAudioPlayer.this.hashCode());

//        Message message = Message.obtain();
//        message.what = CLIENT_ACTION_SET_AUDIO;
//        Bundle bundle = new Bundle();
//        bundle.putByteArray(KEY_AUDIO, JsonHelper.toJson(mAudio).getBytes());
//        bundle.putInt(KEY_PID, Process.myPid());
//        bundle.putByteArray(KEY_KEY, DataInterfaceBroadcastHelper.getRandomKey());
//        bundle.putInt(KEY_SID, RemoteAudioPlayer.this.hashCode());
//        message.setData(bundle);
//
//        sendMsgToService(message);
//        setSessionId(RemoteAudioPlayer.this.hashCode());
    }

    private static void sendMsgToService(Message message) {
        if (null != mServiceMessenger) {
            try {
                LogUtil.logd(TAG + "music:client:sendCommand:" + message.what);
                mServiceMessenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private int mSessionId;

    private int getSessionId() {
        return mSessionId;
    }

    private void setSessionId(int id) {
        this.mSessionId = id;
    }

    private static void onRemoteError() {
        initRemotePlayer();
        AppLogic.runOnBackGround(new Runnable() {

            @Override
            public void run() {
                Error error = new Error(Error.ERROR_CLIENT_MEDIA_REMOTE,
                        "remote service have been  killed", "远程服务挂掉");
                for (int i = 0; i < sPlayArray.size(); i++) {
                    RemoteAudioPlayer player = sPlayArray.get(sPlayArray.keyAt(i));
                    PlayerControlManager.getInstance().notifyOnErrorListener(player.hashCode(), player.mAudio, error);
                }
                sPlayArray.clear();
            }
        }, 20);
    }

    static Intent mLastIntent = null;

    public static void initRemotePlayer() {
        if (mLastIntent == null) {
            mLastIntent = new Intent(GlobalContext.get(), ReserveService0.class);
        }
        LogUtil.logd(TAG + "service:initRemotePlayer::::" + mLastIntent.hashCode());
        GlobalContext.get().bindService(mLastIntent, mConn, Service.BIND_AUTO_CREATE | Service.BIND_IMPORTANT);
        GlobalContext.get().startService(mLastIntent);
    }

    public static void stopRemotePlayer() {
        if (mLastIntent != null) {
            Message obtain = Message.obtain();
            obtain.what = CLIENT_ACTION_EXIT;
            sendMsgToService(obtain);
            mServiceMessenger = null;
            mClientMessenger = null;
            sPlayArray.clear();
            LogUtil.logd(TAG + "service:stopRemotePlayer" + mLastIntent.hashCode());
            GlobalContext.get().unbindService(mConn);
            GlobalContext.get().stopService(mLastIntent);
        }
    }

    private void notifyAidlError() {
        Error err = new Error(Error.ERROR_CLIENT_MEDIA_REMOTE, "remote io err",
                "媒体服务发生异常");
        notifyError(err);
    }

    private void notifyAidlError(Exception e) {
        e.printStackTrace();
        notifyAidlError();
    }

    private void notifyInvalidStateError() {
        Error err = new Error(Error.ERROR_CLIENT_MEDIA_SYS_PLAYER,
                " calling this method in an invalid state", "媒体服务发生异常");
        notifyError(err);
    }

    private void notifyNullStateError() {
        Error err = new Error(Error.ERROR_CLIENT_MEDIA_NULL_STATE,
                " calling this method in an null object", "空指针发生");
        notifyError(err);
    }

    @Override
    public int getDuration() {
        if (mServiceMessenger == null || !mPrepared) {
            return 0;
        }
        try {
            return 100 * 1000;
        } catch (Exception e) {
            notifyAidlError(e);
        }
        return 0;
    }

    @Override
    public float getPlayPercent() {
        if (mServiceMessenger == null) {
            return 0;
        }
        try {
            return 50 * 1000;
        } catch (Exception e) {
            notifyAidlError(e);
        }
        return 0;
    }

    @Override
    public float getBufferingPercent() {
        if (mServiceMessenger == null) {
            return 0;
        }
        try {
            return 0;
        } catch (Exception e) {
            notifyAidlError(e);
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        if (mServiceMessenger == null) {
            return false;
        }
        try {
            return true;
        } catch (Exception e) {
            notifyAidlError(e);
        }
        return false;
    }

    @Override
    public boolean isBuffering() {
        if (mServiceMessenger == null) {
            return false;
        }
        try {
            return false;
        } catch (Exception e) {
            notifyAidlError(e);
        }
        return false;
    }

    @Override
    public boolean needMoreData() {
        if (mServiceMessenger == null) {
            return false;
        }
        try {
            return true;
        } catch (Exception e) {
            notifyAidlError(e);
        }
        return false;
    }

    @Override
    public long getDataPieceSize() {
        if (mServiceMessenger == null) {
            return 0;
        }
        try {
            return 500 * 1024;
        } catch (Exception e) {
            notifyAidlError(e);
        }
        return 0;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setDataSource(String url) {

    }

    @Override
    public String getUrl() {
        return null;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        Map<String, Object> key_values = new HashMap<>();
        key_values.put(KEY_REDUCE_VOLUME, leftVolume);
        sendMsgEventToServer(CLIENT_ACTION_REDUCE_VOLUME, getSessionId(), key_values);
    }


    //解决内存问题:原因切换线程,导致子进程没有被释放掉
    static SparseArray<Integer> saveSessionId = new SparseArray<>();

    @Override
    public void prepareAsync() {
        final int sessionId = RemoteAudioPlayer.this.hashCode();
        setSessionId(sessionId);
        saveSessionId.put(sessionId, sessionId);
        Logger.d("test:sessionId:add:", sessionId + "|---|");
        PlayItemManager.getInstance().createPlayItem(mAudio, new PlayItemManager.IPlayItemListener() {
            @Override
            public void onSuccess(PlayItem playItem) {
                Logger.d("test:sessionId:charge:", getSessionId() + "|---|" + saveSessionId.indexOfKey(getSessionId()));
                if (saveSessionId.indexOfKey(getSessionId()) < 0) {
                    return;
                }


                Message message = Message.obtain();
                message.what = CLIENT_ACTION_SET_AUDIO;
                Bundle bundle = new Bundle();
                bundle.putByteArray(KEY_AUDIO, JsonHelper.toJson(mAudio).getBytes());
                bundle.putInt(KEY_PID, Process.myPid());
                bundle.putByteArray(KEY_KEY, DataInterfaceBroadcastHelper.getRandomKey());
                bundle.putInt(KEY_SID, sessionId);
                bundle.putParcelable(KEY_PLAY_ITEM, playItem);
                message.setData(bundle);

                sendMsgToService(message);


                sendMsgEventToServer(CLIENT_ACTION_THEURL, getSessionId());

                PlayerControlManager.getInstance().notifyOnPreparedStartListener(sessionId, mAudio);
            }

            @Override
            public void onError(Error error) {
                Logger.d("test:sessionId:error:", getSessionId() + "|---|");
                if (error.getErrorCode() == Error.ERROR_CLIENT_MEDIA_NOT_FOUND) {
                    //切换倒下一首
                    PlayerControlManager.getInstance().notifyOnErrorListener(sessionId, mAudio, error);
                }

            }
        });
    }

    @Override
    public void prepareAsyncSub() {
        PlayItemManager.getInstance().createPlayItem(mAudio, new PlayItemManager.IPlayItemListener() {
            @Override
            public void onSuccess(PlayItem playItem) {
                Message message = Message.obtain();
                message.what = CLIENT_ACTION_SET_AUDIO;
                Bundle bundle = new Bundle();
                bundle.putByteArray(KEY_AUDIO, JsonHelper.toJson(mAudio).getBytes());
                bundle.putInt(KEY_PID, Process.myPid());
                bundle.putByteArray(KEY_KEY, DataInterfaceBroadcastHelper.getRandomKey());
                bundle.putInt(KEY_SID, RemoteAudioPlayer.this.hashCode());
                bundle.putParcelable(KEY_PLAY_ITEM, playItem);
                message.setData(bundle);

                sendMsgToService(message);
                setSessionId(RemoteAudioPlayer.this.hashCode());

                sendMsgEventToServer(CLIENT_ACTION_THEURL_SUB, getSessionId());
            }

            @Override
            public void onError(Error error) {

            }
        });
    }

    private void sendMsgEventToServer(int eventId, int sid) {
        sendMsgEventToServer(eventId, sid, null);
    }

    private void sendMsgEventToServer(int eventId, int sid, Map<String, Object> obj) {
        Message message = Message.obtain(null, eventId);
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_SID, sid);
        if (obj != null) {
            Set<Map.Entry<String, Object>> entries = obj.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                if (entry.getValue() instanceof Integer) {
                    bundle.putInt(entry.getKey(), (Integer) entry.getValue());
                } else if (entry.getValue() instanceof String) {
                    bundle.putString(entry.getKey(), (String) entry.getValue());
                } else if (entry.getValue() instanceof Byte[]) {
                    bundle.putByteArray(entry.getKey(), (byte[]) entry.getValue());
                } else if (entry.getValue() instanceof Float) {
                    bundle.putFloat(entry.getKey(), (Float) entry.getValue());
                } else if (entry.getValue() instanceof Long) {
                    bundle.putLong(entry.getKey(), (Long) entry.getValue());
                } else if (entry.getValue() instanceof Boolean) {
                    bundle.putBoolean(entry.getKey(), (Boolean) entry.getValue());
                } else {
                    throw new RuntimeException("传递数据异常，请补充相应的分支");
                }
            }
        }
        message.setData(bundle);
        sendMsgToService(message);
    }

    @Override
    public void start() {
        sendMsgEventToServer(CLIENT_ACTION_PLAY, getSessionId());
    }

    @Override
    public void pause() {
        sendMsgEventToServer(CLIENT_ACTION_PAUSE, getSessionId());
    }

    @Override
    public void stop() {
        release();
//        sendMsgEventToServer(CLIENT_ACTION_DESTROY, getSessionId());
    }

    @Override
    public void seekTo(long position) {
        TimeUtils.startTime(Constant.SPEND_TAG + "quick:seek");
        Map<String, Object> keys = new HashMap<String, Object>();
        keys.put(KEY_POSTION, position);
        if (mAudio != null) {
            keys.put(KEY_FORCE_MORE, true);
        }
        sendMsgEventToServer(CLIENT_ACTION_SEEK, getSessionId(), keys);
        TimeUtils.endTime(Constant.SPEND_TAG + "quick:seekto");
    }

    private static boolean isBlock = false;// 是否阻塞

    static Runnable checkRunnable = new Runnable() {

        @Override
        public void run() {
//			ActivityManager manager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
            int processIdByPkgName = Utils.getProcessIdByPkgName(Constant.PACKAGE_PLAYER);
            LogUtil.logd(TAG + "XXX" + processIdByPkgName);
            Process.killProcess(processIdByPkgName);
//			manager.killBackgroundProcesses("com.txznet.sdkdemo"); 
//			GlobalContext.get().unbindService(mConn);
            isBlock = true;
        }
    };

    @Override
    public synchronized void release() {
        int key = RemoteAudioPlayer.this.hashCode();
        if (Constant.ISTEST) {
            LogUtil.logd(TAG + "SESSION_" + key
                    + "_" + (mServiceMessenger == null ? "null" : mServiceMessenger.hashCode())
                    + "::release");
        }
        // handler.sendEmptyMessage(RELEASECODE);
        PlayItemManager.getInstance().cancelRetry();

        //调用release之后会remove掉sPlayArray里面的播放器，所以当事件回调回来的时候无法找到对应的player去回调stop事件
        PlayerControlManager playerControlManager = PlayerControlManager.getInstance();
        RemoteAudioPlayer remoteAudioPlayer = sPlayArray.get(this.hashCode());
        if (remoteAudioPlayer != null) {
            playerControlManager.notifyOnStopListener(getSessionId(), remoteAudioPlayer.mAudio);
        }

        saveSessionId.remove(key);
        Logger.d("test:sessionId:remove:", key + "|---|");
        if (mServiceMessenger != null) {
            int processIdByPkgName = Utils.getProcessIdByPkgName(Constant.PACKAGE_PLAYER);
            LogUtil.logd("service_id=" + service_id + "/"
                    + processIdByPkgName);
            if (service_id == processIdByPkgName) {
                try {
                    isBlock = false;
                    AppLogic.runOnSlowGround(checkRunnable, 1000);
                    sendMsgEventToServer(CLIENT_ACTION_DESTROY, getSessionId());
                    AppLogic.removeSlowGroundCallback(checkRunnable);
                    // 开启一个守护线程
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mServiceMessenger = null;
            }
        }
        sPlayArray.remove(this.hashCode());
        mOnPreparedListener = null;
        mOnBufferingUpdateListenerSet = null;
        mOnCompletionListener = null;
        mOnErrorListenerSet = null;
        mOnPlayProgressListener = null;
        mOnSeekCompleteListener = null;
    }

    private OnPreparedListener mOnPreparedListener;

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mOnPreparedListener = listener;
    }

    private OnCompletionListener mOnCompletionListener;

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        this.mOnCompletionListener = listener;
    }

    private OnSeekCompleteListener mOnSeekCompleteListener;

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        this.mOnSeekCompleteListener = listener;
    }

    private OnPlayProgressListener mOnPlayProgressListener;

    @Override
    public void setOnPlayProgressListener(OnPlayProgressListener listener) {
        this.mOnPlayProgressListener = listener;
    }

    private boolean mIsForce = false;

    @Override
    public synchronized void forceNeedMoreData(boolean isForce) {
        if (mIsForce != isForce) {
            mIsForce = isForce;
            Map<String, Object> key_values = new HashMap<>();
            key_values.put(KEY_FORCE_MORE, isForce);
            sendMsgEventToServer(CLIENT_ACTION_FORCE_NEED_MORE_DATA, getSessionId(), key_values);
        }
    }

    private static boolean isNeedNextAudioPreloadData = true;
    private static Handler clientHandler = new Handler(Looper.getMainLooper()) {
        /*
         * (non-Javadoc)
         *
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtil.logd(TAG + "handleCommand:" + msg.what);

            msg.getData().setClassLoader(Error.class.getClassLoader());
            int sid = msg.getData().getInt(KEY_SID);
            RemoteAudioPlayer player = sPlayArray.get(sid);

            if (player == null || player.hashCode() != sid) {
                LogUtil.logd(TAG + "callback occur error due to " + (player == null ? "null" : player.hashCode()) + " sid:" + sid + " msg:" + msg.what);
                return;
            }
            PlayerControlManager playerControlManager = PlayerControlManager.getInstance();
            switch (msg.what) {
                case SERVICE_ACTION_PAUSED:
                    playerControlManager.notifyOnPauseListener(sid, player.mAudio);
                    break;
                case SERVICE_ACTION_STOPED:
                    playerControlManager.notifyOnStopListener(sid, player.mAudio);
                    break;
                case SERVICE_ACTION_PLAYING:
                    playerControlManager.notifyOnPlayingListener(sid, player.mAudio);
                    break;
                case SERVICE_ACTION_PREPARING:
                    playerControlManager.notifyOnPreparedListener(sid, player.mAudio);

                    break;
                case SERVICE_ACTION_DISTORIED:
                    break;
                case SERVICE_ACTION_ERRORINFO:
                    Error error = msg.getData().getParcelable(KEY_ERR);
                    playerControlManager.notifyOnErrorListener(sid, player.mAudio, error);
                    break;
                case SERVICE_ACTION_SEEK_READY:
                    playerControlManager.notifyOnSeekCompleteListener(sid, player.mAudio, msg.getData().getLong(KEY_SEEK_TIME));
                    break;
                case SERVICE_ACTION_PROGRESS:
                    playerControlManager.notifyOnProgressListener(sid, player.mAudio, msg.getData().getLong(KEY_POSTION, 0), msg.getData().getLong(KEY_DURATION, 0));



                    break;
                case SERVICE_ACTION_COMPLETED: {
                    playerControlManager.notifyOnPlayCompleteListener(sid, player.mAudio);
                    break;
                }
                case SERVICE_ACTION_BUFFERING_DOWNLOAD:
                    playerControlManager.notifyOnBufferProgressListener(sid, player.mAudio, (List<LocalBuffer>) JsonHelper.toObject(msg.getData().getString(KEY_BUFFERS), new TypeToken<List<LocalBuffer>>() {
                    }.getType()));
                    break;
                case SERVICE_ACTION_BUFFERING:
//                    player.mOnBufferingUpdateListenerSet.onBufferingUpdate(player, msg.getData().getFloat(KEY_PERCENT));
                    break;
                case SERVICE_ACTION_BUFFERING_START:
                    playerControlManager.notifyOnBufferStartListener(sid, player.mAudio);
                    break;
                case SERVICE_ACTION_BUFFERING_END:
                    playerControlManager.notifyOnBufferEndListener(sid, player.mAudio);
                    break;

                default:
                    break;
            }
        }
    };
}
