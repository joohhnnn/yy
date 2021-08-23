package com.txznet.music.service;

import java.util.List;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat.MediaItem;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.ui.MediaPlayerActivity;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

	public static String TAG = MediaPlaybackService.class.getSimpleName();

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private MediaSessionCompat mSession;

	@Override
	public void onCreate() {
		super.onCreate();
		LogUtil.logd(TAG + ":onCreate");
		mSession = new MediaSessionCompat(this, "MusicService");
		setSessionToken(mSession.getSessionToken());
		mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
				| MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
		Intent intent = new Intent(this, MediaPlayerActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 99 /* request code */,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mSession.setSessionActivity(pi);
		mSession.setCallback(new MediaSessionCallback());
		mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
				| MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

		mSession.setPlaybackState(new PlaybackStateCompat.Builder()
				.setActions(getAvailableActions())
				.setState(PlaybackStateCompat.STATE_PLAYING, 1, 1.0f,
						SystemClock.elapsedRealtime())
				.setActiveQueueItemId(Math.round(Integer.MAX_VALUE)).build());
		mSession.setActive(true);


	}

	@Override
	public void onDestroy() {
		LogUtil.logd(TAG + ":onDestroy");
		super.onDestroy();
		mSession.setCallback(null);
		mSession.setActive(false);
		mSession.release();
	}
	
	
	private long getAvailableActions() {
		long actions = PlaybackStateCompat.ACTION_PLAY
				| PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
				| PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH
				| PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
				| PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
		actions |= PlaybackStateCompat.ACTION_PAUSE;
		return actions;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		MediaButtonReceiver.handleIntent(mSession, intent);
		LogUtil.logd(TAG + ":onstartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	public static final String MEDIA_ID_ROOT = "__ROOT__";

	@Override
	public BrowserRoot onGetRoot(@NonNull String clientPackageName,
			int clientUid, Bundle rootHints) {
		Log.d(TAG, "OnGetRoot: clientPackageName=" + clientPackageName
				+ "; clientUid=" + clientUid + " ; rootHints=");
		return new BrowserRoot(MEDIA_ID_ROOT, null);
	}

	@Override
	public void onLoadChildren(@NonNull final String parentMediaId,
			@NonNull final Result<List<MediaItem>> result) {
		Log.d(TAG, "OnLoadChildren: parentMediaId=" + parentMediaId);
	}

	private class MediaSessionCallback extends MediaSessionCompat.Callback {
		@Override
		public void onPlay() {
			LogUtil.logd(TAG + ":play");
			MediaPlayerActivityEngine.getInstance().play();
		}

		@Override
		public void onSkipToQueueItem(long queueId) {
			Log.d(TAG, "OnSkipToQueueItem:" + queueId);
		}

		@Override
		public void onSeekTo(long position) {
			Log.d(TAG, "onSeekTo:");
		}

		@Override
		public void onPlayFromMediaId(String mediaId, Bundle extras) {
			Log.d(TAG, "playFromMediaId mediaId:");
		}

		@Override
		public void onPause() {
			Log.d(TAG, "pause. current state=");
			MediaPlayerActivityEngine.getInstance().pause();
		}

		@Override
		public void onStop() {
			Log.d(TAG, "stop. current state=");
			MediaPlayerActivityEngine.getInstance().stop();
		}

		@Override
		public void onSkipToNext() {
			Log.d(TAG, "skipToNext");
			MediaPlayerActivityEngine.getInstance().next();
		}

		@Override
		public void onSkipToPrevious() {
			Log.d(TAG, "skipToPrev");
			MediaPlayerActivityEngine.getInstance().last();
		}

		@Override
		public void onCustomAction(@NonNull String action, Bundle extras) {
		}


		@Override
		public void onPlayFromSearch(final String query, final Bundle extras) {
			Log.d(TAG, "playFromSearch  query=");

		}
	}

}
