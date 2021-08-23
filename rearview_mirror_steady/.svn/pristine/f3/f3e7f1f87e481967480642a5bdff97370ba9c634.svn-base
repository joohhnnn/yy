package com.txznet.music.service;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.KeyEvent;

import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.EnumState.Operation;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.playerModule.logic.focus.MyFocusListener;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    public static final String MEDIA_ID_ROOT = "__ROOT__";
    public static String TAG = "Music:MediaPlaybackService:";
    private MediaSessionCompat mSession;
//    private Notification mNotification;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d(TAG + ":onCreate");
        mSession = new MediaSessionCompat(this, "MusicService");
        setSessionToken(mSession.getSessionToken());
//        Intent intent = new Intent(this, MediaPlayerActivity.class);
//        PendingIntent pi = PendingIntent.getActivity(this, 99 /* request code */,
//                intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        mSession.setSessionActivity(pi);
        mSession.setCallback(new MediaSessionCallback());
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
                        | PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
                        | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_PAUSE)
                .setState(PlaybackStateCompat.STATE_PLAYING, 1, 1.0f,
                        SystemClock.elapsedRealtime())
                .setActiveQueueItemId(Math.round(Integer.MAX_VALUE)).build());
        mSession.setActive(true);

    }

    @Override
    public void onDestroy() {
        LogUtil.d(TAG + ":onDestroy");
        super.onDestroy();
    }


    public int onStartCommand(Intent intent, int flags, int startId) {

        mSession.setActive(false);
        mSession.setActive(true);
        if (MyFocusListener.playerInFocus) {
            MediaButtonReceiver.handleIntent(mSession, intent);
        }
        LogUtil.d(TAG + ":onStartCommand:" + (intent == null ? "null" : intent.getAction()));

//        if (null == mNotification) {
//            Notification.Builder builder = new Notification.Builder(this);
//            builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher))
//                    .setContentTitle(getResources().getString(R.string.app_name))
//                    .setSmallIcon(R.drawable.ic_launcher)
//                    .setContentText(getResources().getString(R.string.app_name))
//                    .setWhen(System.currentTimeMillis());
//
//            mNotification = builder.build();
//            startForeground(110, mNotification);
//        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName,
                                 int clientUid, Bundle rootHints) {
        LogUtil.d(TAG + "OnGetRoot: clientPackageName=" + clientPackageName
                + "; clientUid=" + clientUid + " ; rootHints=");
        return new BrowserRoot(MEDIA_ID_ROOT, null);
    }

    @Override
    public void onLoadChildren(@NonNull final String parentMediaId,
                               @NonNull final Result<List<MediaItem>> result) {
        LogUtil.d(TAG + "OnLoadChildren: parentMediaId=" + parentMediaId);
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {
            LogUtil.d(TAG + ":play");
            PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            LogUtil.d(TAG + "OnSkipToQueueItem:" + queueId);
        }

        @Override
        public void onSeekTo(long position) {
            LogUtil.d(TAG + "onSeekTo:");
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            LogUtil.d(TAG + "playFromMediaId mediaId:");
        }

        @Override
        public void onPause() {
            LogUtil.d(TAG + "pause. current state=");
            PlayEngineFactory.getEngine().pause(EnumState.Operation.sound);
        }

        @Override
        public void onStop() {
            LogUtil.d(TAG + "stop. current state=");
            PlayEngineFactory.getEngine().release(EnumState.Operation.sound);
        }

        @Override
        public void onSkipToNext() {
            LogUtil.d(TAG + "skipToNext");
            PlayEngineFactory.getEngine().next(Operation.sound);
        }

        @Override
        public void onSkipToPrevious() {
            LogUtil.d(TAG + "skipToPrev");
            PlayEngineFactory.getEngine().last(EnumState.Operation.sound);
        }

        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
        }

        /**
         * Handle free and contextual searches.
         * <p/>
         * All voice searches on Android Auto are sent to this method through a
         * connected
         * {@link android.support.v4.media.session.MediaControllerCompat}.
         * <p/>
         * Threads and async handling: Search, as a potentially slow operation,
         * should run in another thread.
         * <p/>
         * Since this method runs on the main thread, most apps with non-trivial
         * metadata should defer the actual search to another thread (for
         * example, by using an {@link AsyncTask} as we do here).
         **/
        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
            LogUtil.d(TAG + "playFromSearch  query=");

        }


        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            KeyEvent key = mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (key != null) {
                LogUtil.d(TAG, "media button event:" + key.getAction() + " " + key.getKeyCode());
                if (key.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                    if (key.getAction() == KeyEvent.ACTION_UP) {
                        //判断当前的状态
                        if (PlayEngineFactory.getEngine().isPlaying()) {
                            onPause();
                        } else {
                            onPlay();
                        }
                    }
                    return true;
                }
                int next_code = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_KEYCODE_NEXT, -1);
                if (next_code != -1) {
                    if (key.getKeyCode() == next_code) {
                        onSkipToNext();
                        return true;
                    }
                }
                int perv_code = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_KEYCODE_PREV, -1);
                if (perv_code != -1) {
                    if (key.getKeyCode() == perv_code) {
                        onSkipToNext();
                        return true;
                    }
                }
            }
            return super.onMediaButtonEvent(mediaButtonEvent);
        }
    }

}
