package com.txznet.audio.player;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.bean.SessionInfo;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.playerModule.logic.focus.MyFocusListener;

import java.util.List;

public abstract class TXZAudioPlayer {
    protected static final String TAG = "music:player:";
    public static final int PLAY_PROGRESS_NOTIFY_INTERVAL = 1000; // 通知播放进度时间间隔500ms
    public static final int PREPARE_BUFFER_DATA_TIME = 2 * 60 * 1000; // 预缓冲数据时长，决定碎片大小，默认30s
    public static final int NEED_BUFFER_DATA_TIME = 60 * 1000 /* 15000 */; // 需要开始缓冲的数据时长，默认15s
    protected int currentState = IDELSTATE;
    public static final int IDELSTATE = 0;
    public static final int PREPAREDSTATE = 1;
    public static final int PLAYSTATE = 2;
    public static final int PAUSESTATE = 3;
    public static final int BUFFERSTATE = 4;
    public static final int ERRORSTATE = 5;

    public int getCurrentState() {
        return currentState;
    }

//    public void requestAudioFocus(int durationHint) {
//        if (focusChangeListener != null) {
//            focusChangeListener.requestAudioFocus(durationHint);
//        }
//    }

//    public void abandonAudioFocus() {
//        if (focusChangeListener != null) {
//            focusChangeListener.abandonAudioFocus();
//        }
//    }


    protected TXZAudioPlayer(SessionInfo sess, int streamtype) {
        mSessionInfo = sess;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    public abstract int getDuration();

    public abstract float getPlayPercent();

    public abstract float getBufferingPercent();

    public abstract boolean isPlaying();

    public abstract boolean isBuffering();

    public abstract boolean needMoreData();

    public abstract long getDataPieceSize();

    public abstract void reset();

    public abstract void setDataSource(String url);

    public abstract String getUrl();

    // 设置一块数据量的大小
    public void settDataPieceSize(long dataSize) {

    }

    /**
     * 是否可以seek，没有的不要显示进度条
     *
     * @return
     */
    public boolean seekable() {
        return true;
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    public abstract void setVolume(float leftVolume, float rightVolume);

    public void setVolume(float volume) {
        setVolume(volume, volume);
    }

    /**
     * 强制需要更多的数据
     */
    public abstract void forceNeedMoreData(boolean isForce);

    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    public abstract void prepareAsync();

    /**
     * 子播放器播放
     */
    public abstract void prepareAsyncSub();


    public abstract void start();

    public abstract void pause();

    public abstract void stop();

    /**
     * 拖动
     *
     * @param percent 毫秒数
     */
    public abstract void seekTo(long percent);

    public void release() {
        if (mSessionInfo != null) {
            mSessionInfo.cancelAllResponse();
            mSessionInfo = null;
        }
//        mOnErrorListenerSet = null;
//        mOnPlayingListener = null;
//        mOnPausedCompleteListener = null;
//        mOnStoppedListener = null;
    }


    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    public interface OnPreparedListener {
        void onPrepared(TXZAudioPlayer ap);
    }

    public abstract void setOnPreparedListener(OnPreparedListener listener);

    public interface OnCompletionListener {
        void onCompletion(TXZAudioPlayer ap);
    }

    public abstract void setOnCompletionListener(OnCompletionListener listener);

    public interface OnDownloadProgressListener {
        void onDownloadProgress(long downloadSize, long totalSize);
        // public void onDownloadComplete();
    }

    protected OnDownloadProgressListener mOnDownloadProgressListener;

    public void setOnDownloadProgressListener(OnDownloadProgressListener onDownloadProgressListener) {
        this.mOnDownloadProgressListener = onDownloadProgressListener;
    }

    public interface OnBufferingUpdateListener {
        void onBufferingUpdate(TXZAudioPlayer ap, float percent);

        void onDownloading(TXZAudioPlayer ap, List<LocalBuffer> buffers);
    }

    protected OnBufferingUpdateListener mOnBufferingUpdateListenerSet = null;

    protected void onBufferingUpdate(float percent) {
        if (mOnBufferingUpdateListenerSet != null) {
            mOnBufferingUpdateListenerSet.onBufferingUpdate(this, percent);
        }
    }

    public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
        mOnBufferingUpdateListenerSet = listener;
    }

    public void notifyDownloading(List<LocalBuffer> buffers) {
        if (mOnBufferingUpdateListenerSet != null) {
            mOnBufferingUpdateListenerSet.onDownloading(this, buffers);
            if (buffers != null && buffers.size() > 0) {
                this.onBufferingUpdate(buffers.get(buffers.size() - 1).getToP());
            }
        }
    }

    public interface OnSeekCompleteListener {
        public void onSeekComplete(TXZAudioPlayer ap,long seekTime);
    }

    public abstract void setOnSeekCompleteListener(
            OnSeekCompleteListener listener);

    public interface OnErrorListener {
        boolean onError(TXZAudioPlayer ap, Error err);
    }

    protected OnErrorListener mOnErrorListenerSet = null;

    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListenerSet = listener;
    }

    public void notifyError(Error err) {
        if (mOnErrorListenerSet != null) {
            mOnErrorListenerSet.onError(this, err);
        }
    }

    public interface OnPlayProgressListener {
        boolean onPlayProgress(TXZAudioPlayer ap, long currentPosition, long duration);
    }

    public abstract void setOnPlayProgressListener(
            OnPlayProgressListener listener);

    protected OnBufferingStatusListener mOnBufferingStatusListener;

    public void setOnBufferingStatusListener(OnBufferingStatusListener onBufferingStatusListener) {
        mOnBufferingStatusListener = onBufferingStatusListener;
    }

    public interface OnBufferingStatusListener {
        void onBufferingStart(String dns);

        void onBufferingEnd();
    }

    protected OnPausedCompleteListener mOnPausedCompleteListener;

    public void setOnPausedCompleteListener(OnPausedCompleteListener onPausedCompleteListener) {
        mOnPausedCompleteListener = onPausedCompleteListener;
    }

    public interface OnPausedCompleteListener {
        void onPausedCompleteListener(String theURL);
    }

    protected OnPlayingListener mOnPlayingListener;

    public void setOnPlayingListener(OnPlayingListener onPlayingListener) {
        mOnPlayingListener = onPlayingListener;
    }

    public interface OnPlayingListener {
        void onPlayingListener(String theURL, int position);
    }

    protected OnStoppedListener mOnStoppedListener;

    public void setOnStoppedListener(OnStoppedListener onStoppedListener) {
        mOnStoppedListener = onStoppedListener;
    }

    public interface OnStoppedListener {
        void onStoppedListener(String theURL);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////////////
    public SessionInfo mSessionInfo = null;


}
