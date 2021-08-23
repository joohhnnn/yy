package com.txznet.audio.player;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.SparseArray;

import com.txznet.audio.ErrCode;
import com.txznet.audio.player.core.SysMediaPlayer;
import com.txznet.audio.player.util.PlayStateUtil;
import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogicBase;

import java.util.Map;

import static com.txznet.comm.util.ProcessUtil.getProcessIdByPkgName;

public class MediaService extends Service {
    public static final String EXTRA_KEY_SID = "sid";
    public static final String EXTRA_KEY_STATE = "state";
    public static final String EXTRA_KEY_POS = "position";
    public static final String EXTRA_KEY_DURATION = "duration";
    public static final String EXTRA_KEY_ERROR_CODE = "errCode";
    public static final String EXTRA_KEY_DESC = "desc";
    public static final String EXTRA_KEY_HINT = "hint";

    public static String ACTION_PLAYER_ON_STATE_CHANGED;
    public static String ACTION_PLAYER_ON_PROGRESS_CHANGED;
    public static String ACTION_PLAYER_ON_SEEK_COMPLETE;
    public static String ACTION_PLAYER_ON_COMPLETION;
    public static String ACTION_PLAYER_ON_ERROR;

    static {
        String pkgName = GlobalContext.get().getPackageName();
        if (pkgName == null) {
            pkgName = "com.txznet.music";
        }
        ACTION_PLAYER_ON_STATE_CHANGED = pkgName + ".action.v2.ON_STATE_CHANGED";
        ACTION_PLAYER_ON_PROGRESS_CHANGED = pkgName + ".action.v2.ON_PROGRESS_CHANGED";
        ACTION_PLAYER_ON_SEEK_COMPLETE = pkgName + ".action.v2.ON_SEEK_COMPLETE";
        ACTION_PLAYER_ON_COMPLETION = pkgName + ".action.v2.ON_COMPLETION";
        ACTION_PLAYER_ON_ERROR = pkgName + ".action.v2.ON_ERROR";
    }

    private static final SparseArray<IMediaPlayer> MEDIA_PLAYER_INSTANCES = new SparseArray<>();
    private static final SparseArray<AudioPlayer.Config> MEDIA_PLAYER_CONFIGS = new SparseArray<>();
    private static final String TAG = "MediaService";
    private static Integer mLastClientPid = null;
    private static Runnable checkMainThread = new Runnable() {

        @Override
        public void run() {
            int cur_pid;
            try {
                cur_pid = getProcessIdByPkgName(GlobalContext.get().getPackageName());
                if (cur_pid != -1) {
                    if (mLastClientPid != null && !mLastClientPid.equals(cur_pid)) {
                        killSelf();
                    } else {
                        AppLogicBase.runOnSlowGround(this, 2000);
                    }
                } else {
                    killSelf();
                }
            } catch (Exception e) {
            }
        }
    };


    private final IBinder mBinder = new AidlPlayerStub();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    class AidlPlayerStub extends IAidlPlayer.Stub {
        private Map mInfos;

        private IMediaPlayer getPlayer(int sid) {
            return MEDIA_PLAYER_INSTANCES.get(sid);
        }

        private AudioPlayer.Config getConfig(int sid) {
            AudioPlayer.Config config;
            synchronized (MEDIA_PLAYER_CONFIGS) {
                config = MEDIA_PLAYER_CONFIGS.get(sid);
            }
            return config == null ? new AudioPlayer.Config.Builder().build() : config;
        }

        @Override
        public void createPlayer(final int sid, final Map infos) throws RemoteException {
            mInfos = infos;
            Map<String, Object> _infos = mInfos;
            String path = (String) _infos.get("path");
            final int pid = (int) _infos.get("pid");
            final int streamType = (int) _infos.get("streamType");
            final float leftVol = (float) _infos.get("leftVol");
            final float rightVol = (float) _infos.get("rightVol");
            // 比较客户端进程是否发生改变
            // 开启线程判断主进程是否存在与以前的比较
            int cur_pid = getProcessIdByPkgName(GlobalContext.get().getPackageName());
            if (cur_pid != -1) {
                if (mLastClientPid != null && !mLastClientPid.equals(cur_pid)) {
                    killSelf();
                } else {
                    AppLogicBase.removeSlowGroundCallback(checkMainThread);
                    AppLogicBase.runOnSlowGround(checkMainThread, 2000);
                }
            }
            mLastClientPid = pid;

            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null && !player.getClass().equals(getConfig(sid).playerImplClass)) { // 对实现类进行了切换
                    player.release();
                    player = null;
                }
                if (player == null) {
                    Log.d(TAG, "createPlayer new,  " + sid);
                    try {
                        player = getConfig(sid).playerImplClass.newInstance();
                    } catch (Exception e) {
                        e.printStackTrace();
                        player = new SysMediaPlayer();
                    }
                    player.setAudioStreamType(streamType);
                    player.setVolume(leftVol, rightVol);
                    player.setOnPlayStateChangeListener(new OnPlayerStateChangeListener() {
                        @Override
                        public void onPlayStateChanged(@IMediaPlayer.PlayState int state) {
                            LogUtil.d(TAG, sid + "#onPlayStateChanged " + PlayStateUtil.convert2Str(state));
                            sendStateChange(sid, state);
                        }

                        @Override
                        public void onProgressChanged(long position, long duration) {
                            LogUtil.d(TAG, sid + "#onProgressChanged " + position + "/" + duration);
                            sendProgressChange(sid, position, duration);
                        }

                        @Override
                        public void onSeekComplete() {
                            LogUtil.d(TAG, sid + "#onSeekComplete");
                            sendSeekComplete(sid);
                        }

                        @Override
                        public void onCompletion() {
                            LogUtil.d(TAG, sid + "#onCompletion");
                            sendComplete(sid);
                        }

                        @Override
                        public void onError(Error error) {
                            LogUtil.d(TAG, sid + "#onError error=" + error);
                            sendErrorMsg(sid, error);
                        }
                    });
                    synchronized (MEDIA_PLAYER_INSTANCES) {
                        MEDIA_PLAYER_INSTANCES.put(sid, player);
                    }
                } else {
                    Log.d(TAG, "createPlayer from cache, " + sid);
                }
                player.setDataSource(path);
            }
        }

        @Override
        public String getPath(int sid) throws RemoteException {
            return mInfos == null ? null : (String) mInfos.get("path");
        }

        @Override
        public void prepareAsync(int sid) throws RemoteException {
            LogUtil.d(TAG, sid + "#prepareAsync");
            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null) {
                    player.prepareAsync();
                }
            }
        }

        @Override
        public void start(int sid) throws RemoteException {
            LogUtil.d(TAG, sid + "#start");
            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null) {
                    player.start();
                }
            }
        }

        @Override
        public void stop(int sid) throws RemoteException {
            LogUtil.d(TAG, sid + "#stop");
            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null) {
                    player.stop();
                }
            }
        }

        @Override
        public void pause(int sid) throws RemoteException {
            LogUtil.d(TAG, sid + "#pause");
            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null) {
                    player.pause();
                }
            }
        }

        @Override
        public boolean isPlaying(int sid) throws RemoteException {
            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null) {
                    return player.isPlaying();
                }
            }
            return false;
        }

        @Override
        public void seekTo(int sid, long msec) throws RemoteException {
            LogUtil.d(TAG, sid + "#seekTo msec=" + msec);
            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null) {
                    player.seekTo(msec);
                }
            }
        }

        @Override
        public long getCurrentPosition(int sid) throws RemoteException {
            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null) {
                    return player.getCurrentPosition();
                }
            }
            return 0;
        }

        @Override
        public long getDuration(int sid) throws RemoteException {
            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null) {
                    return player.getDuration();
                }
            }
            return 0;
        }

        @Override
        public void release(int sid) throws RemoteException {
            LogUtil.d(TAG, sid + "#release");
            try {
                StringBuilder sbSids = new StringBuilder();
                synchronized (MEDIA_PLAYER_INSTANCES) {
                    IMediaPlayer player = getPlayer(sid);
                    if (player != null) {
                        player.release();
                    }
                    MEDIA_PLAYER_INSTANCES.remove(sid);
                    for (int i = 0; i < MEDIA_PLAYER_INSTANCES.size(); i++) {
                        int _sid = MEDIA_PLAYER_INSTANCES.keyAt(i);
                        sbSids.append(_sid).append(";");
                    }
                }
                synchronized (MEDIA_PLAYER_CONFIGS) {
                    MEDIA_PLAYER_CONFIGS.remove(sid);
                }
                Log.d(TAG, "removePlayer sid=" + sid + ", currPlayers=" + sbSids.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void reset(int sid) throws RemoteException {
            LogUtil.d(TAG, sid + "#reset");
            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null) {
                    player.reset();
                }
            }
        }

        @Override
        public void setVolume(int sid, float leftVol, float rightVol) throws RemoteException {
            LogUtil.d(TAG, sid + "#setVolume leftVol=" + leftVol + ", rightVol=" + rightVol);
            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null) {
                    player.setVolume(leftVol, rightVol);
                }
            }
        }

        @Override
        public void setStreamType(int sid, int streamtype) throws RemoteException {
            LogUtil.d(TAG, sid + "#setStreamType streamtype=" + streamtype);
            synchronized (MEDIA_PLAYER_INSTANCES) {
                IMediaPlayer player = getPlayer(sid);
                if (player != null) {
                    player.setAudioStreamType(streamtype);
                }
            }
        }

        @Override
        public void syncConfig(int sid, Map infos) throws RemoteException {
            LogUtil.d(TAG, sid + "#syncConfig data=" + infos);
            synchronized (MEDIA_PLAYER_CONFIGS) {
                MEDIA_PLAYER_CONFIGS.put(sid, (AudioPlayer.Config) infos.get("config"));
            }
        }

        @Override
        public void destroy(int sid) throws RemoteException {

        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private static void killSelf() {
        LogUtil.e(TAG, "kill self, because media client pid changed, last_client_pid=" + mLastClientPid + ", curr_client_pid=" + getProcessIdByPkgName(GlobalContext.get().getPackageName()));
        Error err = new Error(ErrCode.ERROR_MEDIA_CROSS_PROCESS, "media client pid changed", "客户端进程挂掉");
        sendErrorMsg(0, err);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private static void sendStateChange(int sid, int state) {
        Intent intent = new Intent(ACTION_PLAYER_ON_STATE_CHANGED);
        intent.putExtra(EXTRA_KEY_SID, sid);
        intent.putExtra(EXTRA_KEY_STATE, state);
        GlobalContext.get().sendBroadcast(intent);
    }

    private static void sendProgressChange(int sid, long pos, long duration) {
        Intent intent = new Intent(ACTION_PLAYER_ON_PROGRESS_CHANGED);
        intent.putExtra(EXTRA_KEY_SID, sid);
        intent.putExtra(EXTRA_KEY_POS, pos);
        intent.putExtra(EXTRA_KEY_DURATION, duration);
        GlobalContext.get().sendBroadcast(intent);
    }

    private static void sendSeekComplete(int sid) {
        Intent intent = new Intent(ACTION_PLAYER_ON_SEEK_COMPLETE);
        intent.putExtra(EXTRA_KEY_SID, sid);
        GlobalContext.get().sendBroadcast(intent);
    }

    private static void sendComplete(int sid) {
        Intent intent = new Intent(ACTION_PLAYER_ON_COMPLETION);
        intent.putExtra(EXTRA_KEY_SID, sid);
        GlobalContext.get().sendBroadcast(intent);
    }

    private static void sendErrorMsg(int sid, Error error) {
        Intent intent = new Intent(ACTION_PLAYER_ON_ERROR);
        intent.putExtra(EXTRA_KEY_SID, sid);
        intent.putExtra(EXTRA_KEY_ERROR_CODE, error.errorCode);
        intent.putExtra(EXTRA_KEY_DESC, error.desc);
        intent.putExtra(EXTRA_KEY_HINT, error.hint);
        GlobalContext.get().sendBroadcast(intent);
    }
}
