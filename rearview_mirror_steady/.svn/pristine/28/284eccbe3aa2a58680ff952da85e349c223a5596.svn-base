package com.txznet.txz.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txz.ui.event.UiEvent;
import com.txz.ui.music.UiMusic;
import com.txz.ui.music.UiMusic.MediaModel;
import com.txznet.txz.jni.JNIHelper;

public class MusicActionReciever extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		JNIHelper.logd("recive action: " + action);

		if (action.equals("com.txznet.txz.music.play")) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_PLAY);
			return;
		}

		if (action.equals("com.txznet.txz.music.pause")) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_PAUSE);
			return;
		}

		if (action.equals("com.txznet.txz.music.next")) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_NEXT);
			return;
		}

		if (action.equals("com.txznet.txz.music.prev")) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_PREV);
			return;
		}

		if (action.equals("com.txznet.txz.music.prev")) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_PREV);
			return;
		}

		if (action.equals("com.txznet.txz.music.command")) {
			if (!intent.hasExtra("cmd"))
				return;
			String cmd = intent.getStringExtra("cmd");
			JNIHelper.logd("recive command: " + cmd);
			if (cmd.equals("play"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_PLAY);
			else if (cmd.equals("pause"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_PAUSE);
			else if (cmd.equals("next"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_NEXT);
			else if (cmd.equals("prev"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_PREV);
			else if (cmd.equals("switch"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_RANDOM);
			else if (cmd.equals("mode_loop_single"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_MODE_LOOP_SINGLE);
			else if (cmd.equals("mode_loop_all"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_MODE_LOOP_ALL);
			else if (cmd.equals("mode_random"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_MODE_RANDOM);
			else if (cmd.equals("favour"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_FAVOURITE_CUR);
			else if (cmd.equals("unfavour"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE_CUR);
			else if (cmd.equals("hate"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_HATE_CUR);
			else if (cmd.equals("play_favour"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_PLAY_FAVOURITE_LIST);
			else if (cmd.equals("speak"))
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
						UiMusic.SUBEVENT_MEDIA_SPEAK_MUSIC_INFO);

			return;
		}

		if (action.equals("com.txznet.txz.music.search")) {
			MediaModel model = new MediaModel();
			if (intent.hasExtra("title"))
				model.strTitle = intent.getStringExtra("title");
			if (intent.hasExtra("album"))
				model.strAlbum = intent.getStringExtra("album");
			if (intent.hasExtra("type"))
				model.strType = intent.getStringExtra("type");
			if (intent.hasExtra("artist"))
				model.rptStrArtist = intent.getStringArrayExtra("artist");
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MUSIC,
					UiMusic.SUBEVENT_MEDIA_SEARCH_AND_PLAY, model);
			return;
		}
	}

}
