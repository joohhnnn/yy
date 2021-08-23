package com.txznet.audio.player;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.player.audio.PlayerAudio;
import com.txznet.audio.player.factory.PlayAudioFactory;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.playerModule.bean.PlayItem;
import com.txznet.music.util.TestUtil;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.JsonHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by telenewbie on 2017/6/6.
 */

public class TestAudioPlayerService extends Service implements PlayerServiceConstants {


    private static final String TAG = "music:player:service:";
    private static final int MAX_RAISE_MEMORY = 40000; // 最多允许增加40M
    static Messenger clientMessenger = null;
    static Map<Integer, TXZAudioPlayer> mAudioPlayerBeans = new HashMap<Integer, TXZAudioPlayer>();
    private static Integer mLastClientPid = null;
    private static Runnable checkMainThread = new Runnable() {

        @Override
        public void run() {
            int cur_pid = getProcessIdByPkgName(Constant.PACKAGE_MAIN);
            if (cur_pid != -1) {
                if (mLastClientPid != null && !mLastClientPid.equals(cur_pid)) {
                    killSelf();
                } else {
                    AppLogic.runOnSlowGround(this, 2000);
                }
            } else {
                killSelf();
            }
        }
    };
    private final IncomingHandler mIncomingHandler = new IncomingHandler();
    final Messenger mMessenger = new Messenger(mIncomingHandler);
    AudioPlayerBinder binder = null;
    private int mInitialMemory = 0;

    private static TXZAudioPlayer createPlayer(int sessionID, PlayerAudio audio) {
        for (Integer sid : mAudioPlayerBeans.keySet()) {
            TXZAudioPlayer audioPlayer = mAudioPlayerBeans.get(sid);
            if (audioPlayer != null) {
                audioPlayer.mSessionInfo.player.release();
                audioPlayer.release();
            }
        }
        mAudioPlayerBeans.clear();
        TXZAudioPlayer audioPlayer = mAudioPlayerBeans.get(sessionID);
        if (null == audioPlayer) {
            audioPlayer = SessionManager.getInstance().createPlayer(audio);
        }
        mAudioPlayerBeans.put(sessionID, audioPlayer);
        LogUtil.logd(TAG + "[Server]SERVICE_SESSION_CREATE(" + sessionID + ")");
        return audioPlayer;
    }

    private static void releasePlayer(int sessionID) {
        TXZAudioPlayer audioPlayer = mAudioPlayerBeans.remove(sessionID);
        if (audioPlayer != null) {
//            audioPlayer.abandonAudioFocus();
            audioPlayer.release();
        }
        TestUtil.printMap("mAudioPlayerBeans", mAudioPlayerBeans);

        LogUtil.logd(TAG + "[Server]SERVICE_SESSION_RELEASE(" + sessionID + ")");
    }

    public static TXZAudioPlayer getPlayer(int sid) {
        TXZAudioPlayer audioPlayer = mAudioPlayerBeans.get(sid);
        if (audioPlayer == null) {
            LogUtil.loge("AudioPlayerService is null");
        }
        return audioPlayer;
    }

    private static int getProcessIdByPkgName(String pkgName) {
        ActivityManager activityManager = (ActivityManager) GlobalContext.get()
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> infos = activityManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo info : infos) {
            if (info.processName.equals(pkgName)) {
                return info.pid;
            }
        }
        return -1;
    }

    private static void killSelf() {
        Error err = new Error(Error.ERROR_CLIENT_MEDIA_REMOTE, "music client pid changed", "客户端进程挂掉");
        sendErrorMessage(0, err);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private static void sendErrorMessage(int sid, Error errorInfo) {
        Message message = Message.obtain();
        message.what = SERVICE_ACTION_ERRORINFO;
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_SID, sid);
        bundle.putParcelable(KEY_ERR, errorInfo);
        message.setData(bundle);
        sendMessageToClient(message);
    }

    public static void sendMessageToClient(Message message) {
        if (clientMessenger == null) {
            return;
        }
        try {
            LogUtil.logd(TAG + "music:server:sendCommand:" + message.what);
            clientMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        return mMessenger.getBinder();
    }

    public static class AudioPlayerBinder {

        int test = 0;
        private TXZAudioPlayer mAudioPlayer;
        private int mSid;


        public AudioPlayerBinder() {
        }

        public int getDuration(int sid) {
            return getPlayer(sid).getDuration();
        }

        public float getPlayPercent(int sid) {
            return getPlayer(sid).getPlayPercent();
        }

        public float getBufferingPercent(int sid) {
            return getPlayer(sid).getBufferingPercent();
        }

        public boolean isPlaying(int sid) {
            return getPlayer(sid).isPlaying();
        }

        public boolean isBuffering(int sid) {
            return getPlayer(sid).isBuffering();
        }

        public boolean needMoreData(int sid) {
            return getPlayer(sid).needMoreData();
        }

        public long getDataPieceSize(int sid) {
            return getPlayer(sid).getDataPieceSize();
        }

        public void setVolume(int sid, float leftVol, float rightVol) {
            if (getPlayer(sid) != null) {
                getPlayer(sid).setVolume(leftVol, rightVol);
            }
        }

        public void prepareAsync(int sid) {
            LogUtil.logd(TAG + "[Server]SERVICE_SESSION_PREPAREASYNC(" + sid
                    + ")");
            if (getPlayer(sid) != null) {
//                getPlayer(sid).requestAudioFocus(AudioManager.AUDIOFOCUS_GAIN);
                getPlayer(sid).prepareAsync();
            } else {
                Error err = new Error(Error.ERROR_CLIENT_MEDIA_GET_AUDIO,
                        "重新加载中...", "获取Audio失败");
                sendErrorMessage(sid, err);
            }
        }

        public void prepareAsyncSub(int sid) {
            if (getPlayer(sid) != null) {
//                getPlayer(sid).requestAudioFocus(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                getPlayer(sid).prepareAsync();
            } else {
                Error err = new Error(Error.ERROR_CLIENT_MEDIA_GET_AUDIO,
                        "重新加载中...", "获取Audio失败");
                sendErrorMessage(sid, err);
            }
        }

        public void start(int sid) {
            LogUtil.logd(TAG + "[Server]SERVICE_SESSION_START(" + sid + ")");
            if (getPlayer(sid) != null) {
                LogUtil.logd(TAG + "[Server]SERVICE_SESSION_START(" + sid + ")!=null");
//                getPlayer(sid).requestAudioFocus(AudioManager.AUDIOFOCUS_GAIN);
                getPlayer(sid).start();
            } else {
                Error err = new Error(Error.ERROR_CLIENT_MEDIA_GET_AUDIO,
                        "重新加载中...", "获取Audio失败");
                sendErrorMessage(sid, err);
            }
        }

        public void pause(int sid) {
            LogUtil.logd(TAG + "[Server]SERVICE_SESSION_PAUSE(" + sid + ")");
            if (getPlayer(sid) != null) {
//                getPlayer(sid).abandonAudioFocus();
                getPlayer(sid).pause();
            }
        }

        public void stop(int sid) {
            if (getPlayer(sid) != null) {
                getPlayer(sid).stop();
            }
        }

        public void seekTo(int sid, long position) {
            LogUtil.logd(TAG + "service seek to " + position);
            if (getPlayer(sid) != null) {
                getPlayer(sid).seekTo(position);
            }
        }

        public void release(int sid) {
            releasePlayer(sid);
        }

        public int createAudioPlayer(PlayItem playItem, Audio audio, int pid, byte[] key, final int sid) {
            // int sid = intent.getIntExtra(KEY_SID, 0);
            // int pid = intent.getIntExtra("pid", 0);

//            Audio currentAudio = JsonHelper.toObject(Audio.class, new String(
//                    audioBytes));
            // 比较客户端进程是否发生改变
            // 开启线程判断主进程是否存在与以前的比较
            int cur_pid = getProcessIdByPkgName(Constant.PACKAGE_MAIN);
            if (cur_pid != -1) {
                if (mLastClientPid != null && !mLastClientPid.equals(cur_pid)) {
                    killSelf();
                    return sid;
                } else {
                    AppLogic.removeSlowGroundCallback(checkMainThread);
                    AppLogic.runOnSlowGround(checkMainThread, 2000);
                }
            }

            mLastClientPid = pid;
            DataInterfaceBroadcastHelper.setRandomKey(pid, key);
            mSid = sid;
//            PlayerAudio playAudio = PlayAudioFactory.createPlayAudio(playItem);
            PlayerAudio playAudio = PlayAudioFactory.createPlayAudio(playItem, audio);

            mAudioPlayer = createPlayer(sid, playAudio);
            LogUtil.logd(TAG + "[Server]REMOTTE_SESSION create "
                    + playAudio.getAudioName());

//            if (Constant.ISTEST) {
//                LogUtil.logd(TAG + "[Server]REMOTTE_SESSION playurl "
//                        + currentAudio.getStrProcessingUrl());
//            }
            mAudioPlayer
                    .setOnBufferingUpdateListener(new TXZAudioPlayer.OnBufferingUpdateListener() {
                        @Override
                        public void onDownloading(TXZAudioPlayer ap,
                                                  List<LocalBuffer> buffers) {
                            Message message = Message.obtain();
                            message.what = SERVICE_ACTION_BUFFERING_DOWNLOAD;
                            Bundle bundle = new Bundle();
                            bundle.putInt(KEY_SID, mSid);
                            bundle.putString(KEY_BUFFERS, JsonHelper.toJson(buffers));
                            message.setData(bundle);
                            sendMessageToClient(message);
                        }

                        @Override
                        public void onBufferingUpdate(TXZAudioPlayer ap,
                                                      float percent) {

//                            Message message = Message.obtain();
//                            message.what = SERVICE_ACTION_BUFFERING;
//                            Bundle bundle = new Bundle();
//                            bundle.putInt(KEY_SID, mSid);
//                            bundle.putFloat(KEY_PERCENT, percent);
//                            message.setData(bundle);
//                            sendMessageToClient(message);
                        }
                    });
            mAudioPlayer.setOnCompletionListener(new TXZAudioPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(TXZAudioPlayer ap) {
                    if (mAudioPlayer.hashCode() != ap.hashCode()) {
                        return;
                    }
                    Message message = Message.obtain();
                    message.what = SERVICE_ACTION_COMPLETED;
                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_SID, mSid);
                    message.setData(bundle);
                    sendMessageToClient(message);
//                    Debug.MemoryInfo memoryInfo = ProcessMemoryMonitor.getInstance().getMemoryInfo(Constant.PACKAGE_PLAYER);
//                    if (memoryInfo.getTotalPss() >= 10000) {
//                        CrashCommonHandler.getInstance().uncaughtException(Thread.currentThread(),
//                                new RuntimeException("MemoryTooLarge:" + memoryInfo.getTotalPss()));
//                    }
                }
            });
            mAudioPlayer.setOnErrorListener(new TXZAudioPlayer.OnErrorListener() {
                @Override
                public boolean onError(TXZAudioPlayer ap, Error err) {
                    LogUtil.logw(TAG + " player occur error:" + err.toString());
                    if (mAudioPlayer.hashCode() == ap.hashCode()) {
                        sendErrorMessage(mSid, err);
                        return true;
                    }
                    return false;
                }
            });
            mAudioPlayer
                    .setOnPlayProgressListener(new TXZAudioPlayer.OnPlayProgressListener() {
                        @Override
                        public boolean onPlayProgress(TXZAudioPlayer ap, long position, long duration) {
                            Message message = Message.obtain();
                            message.what = SERVICE_ACTION_PROGRESS;
                            Bundle bundle = new Bundle();
                            bundle.putInt(KEY_SID, mSid);
                            bundle.putLong(KEY_DURATION, duration);
                            bundle.putLong(KEY_POSTION, position);
                            message.setData(bundle);
                            sendMessageToClient(message);
                            return false;
                        }
                    });
            mAudioPlayer.setOnPreparedListener(new TXZAudioPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(TXZAudioPlayer ap) {
                    Message message = Message.obtain();
                    message.what = SERVICE_ACTION_PREPARING;
                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_SID, mSid);
                    message.setData(bundle);
                    sendMessageToClient(message);
                }
            });
            mAudioPlayer
                    .setOnSeekCompleteListener(new TXZAudioPlayer.OnSeekCompleteListener() {
                        @Override
                        public void onSeekComplete(TXZAudioPlayer ap, long seekTime) {

                            Message message = Message.obtain();
                            message.what = SERVICE_ACTION_SEEK_READY;
                            Bundle bundle = new Bundle();
                            bundle.putInt(KEY_SID, mSid);
                            bundle.putLong(KEY_SEEK_TIME, seekTime);
                            message.setData(bundle);
                            sendMessageToClient(message);
                        }
                    });
            mAudioPlayer.setOnPausedCompleteListener(new TXZAudioPlayer.OnPausedCompleteListener() {
                @Override
                public void onPausedCompleteListener(String theURL) {
                    Message message = Message.obtain();
                    message.what = SERVICE_ACTION_PAUSED;
                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_SID, mSid);
                    message.setData(bundle);
                    sendMessageToClient(message);
                }
            });
            mAudioPlayer.setOnPlayingListener(new TXZAudioPlayer.OnPlayingListener() {
                @Override
                public void onPlayingListener(String theURL, int position) {
                    Message message = Message.obtain();
                    message.what = SERVICE_ACTION_PLAYING;
                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_SID, mSid);
                    message.setData(bundle);
                    sendMessageToClient(message);
                }
            });
            mAudioPlayer.setOnStoppedListener(new TXZAudioPlayer.OnStoppedListener() {
                @Override
                public void onStoppedListener(String theURL) {
                    Message message = Message.obtain();
                    message.what = SERVICE_ACTION_STOPED;
                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_SID, mSid);
                    message.setData(bundle);
                    sendMessageToClient(message);
                }
            });
            mAudioPlayer.setOnBufferingStatusListener(new TXZAudioPlayer.OnBufferingStatusListener() {
                @Override
                public void onBufferingStart(String dns) {
                    Message message = Message.obtain();
                    message.what = SERVICE_ACTION_BUFFERING_START;
                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_SID, mSid);
                    message.setData(bundle);
                    sendMessageToClient(message);
                }

                @Override
                public void onBufferingEnd() {
                    Message message = Message.obtain();
                    message.what = SERVICE_ACTION_BUFFERING_END;
                    Bundle bundle = new Bundle();
                    bundle.putInt(KEY_SID, mSid);
                    message.setData(bundle);
                    sendMessageToClient(message);
                }
            });
            return mSid;
        }

        public void forceNeedMoreData(boolean isForce) {
            if (mAudioPlayer != null) {
                mAudioPlayer.forceNeedMoreData(isForce);
            }
        }
    }

    class IncomingHandler extends Handler {


        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            LogUtil.logd(TAG + "handleCommand:" + msg.what + " binder is null?" + (null == binder));
            switch (msg.what) {
                case CLIENT_ACTION_BIND:
                    clientMessenger = msg.replyTo;
                    break;
                case CLIENT_ACTION_SET_AUDIO:
                    msg.getData().setClassLoader(PlayItem.class.getClassLoader());
                    int sid = msg.getData().getInt(KEY_SID);
                    byte[] audioBytes = msg.getData().getByteArray(KEY_AUDIO);
                    int pid = msg.getData().getInt(KEY_PID);
                    byte[] key = msg.getData().getByteArray(KEY_KEY);
                    PlayItem playItem = msg.getData().getParcelable(KEY_PLAY_ITEM);
                    binder = new TestAudioPlayerService.AudioPlayerBinder();
                    binder.createAudioPlayer(playItem, JsonHelper.toObject(Audio.class, new String(audioBytes)), pid, key, sid);
                    break;
                case CLIENT_ACTION_UNBIND:
                    clientMessenger = null;
                    break;
                case CLIENT_ACTION_THEURL:
                    if (null != binder) {
                        binder.prepareAsync(msg.getData().getInt(KEY_SID));
                    }
                    break;
                case CLIENT_ACTION_THEURL_SUB:
                    if (null != binder) {
                        binder.prepareAsyncSub(msg.getData().getInt(KEY_SID));
                    }
                    break;
                case CLIENT_ACTION_PLAY:
                    if (null != binder) {
                        binder.start(msg.getData().getInt(KEY_SID));

                    }
                    break;
                case CLIENT_ACTION_SEEK:
                    if (null != binder) {
                        binder.forceNeedMoreData(msg.getData().getBoolean(KEY_FORCE_MORE, false));
                        binder.seekTo(msg.getData().getInt(KEY_SID), msg.getData().getLong(KEY_POSTION));
                    }
                    break;
                case CLIENT_ACTION_PAUSE:
                    if (null != binder) {
                        binder.pause(msg.getData().getInt(KEY_SID));
                    }
                    break;
                case CLIENT_ACTION_STOP:
                    if (null != binder) {
                        binder.stop(msg.getData().getInt(KEY_SID));
                    }
                    break;
                case CLIENT_ACTION_RESET:
                    if (null != binder) {
                        binder.release(msg.getData().getInt(KEY_SID));
                    }
                    break;
                case CLIENT_ACTION_DESTROY:
                    if (null != binder) {
                        binder.release(msg.getData().getInt(KEY_SID));
                    }
                    break;
                case CLIENT_ACTION_FORCE_NEED_MORE_DATA:
                    if (null != binder) {
                        binder.forceNeedMoreData(msg.getData().getBoolean(KEY_FORCE_MORE));
                    }
                    break;
                case CLIENT_ACTION_EXIT:
                    stopSelf();
                    killSelf();
                    break;
                case CLIENT_ACTION_REDUCE_VOLUME:
                    if (null != binder) {
                        float volume = msg.getData().getFloat(KEY_REDUCE_VOLUME);
                        int sourceId = msg.getData().getInt(KEY_SID);
                        binder.setVolume(sourceId, volume, volume);
                    }
                default:
                    break;
            }
        }
    }


}
