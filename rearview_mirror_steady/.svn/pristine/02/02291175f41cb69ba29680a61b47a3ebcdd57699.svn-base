package com.txznet.txz.module.music.asr;

import com.txz.ui.event.UiEvent;
import com.txz.ui.music.UiMusic;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.txz.R;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.wakeup.WakeupManager;

public class MusicAsrManager {

	private static final String ASR_CMD_PLAYER_PREVIOUS = "ASR_CMD_PLAYER_PREVIOUS";
	private static final String ASR_CMD_PLAYER_PAUSE = "ASR_CMD_PLAYER_PAUSE";
	private static final String ASR_CMD_PLAYER_PLAY = "ASR_CMD_PLAYER_PLAY";
	private static final String ASR_CMD_PLAYER_NEXT = "ASR_CMD_PLAYER_NEXT";

	public static void registerFreeWakeUp() {
		AsrComplexSelectCallback freeWakeupCallback = new AsrComplexSelectCallback() {

			@Override
			public void onCommandSelected(String type, String command) {
				if (ASR_CMD_PLAYER_PREVIOUS.equals(type)) {
					MusicManager.getInstance().onEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PREV, null);
				} else if (ASR_CMD_PLAYER_PAUSE.equals(type)) {
					MusicManager.getInstance().onEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PAUSE, null);
				} else if (ASR_CMD_PLAYER_PLAY.equals(type)) {
					MusicManager.getInstance().onEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PLAY, null);
				} else if (ASR_CMD_PLAYER_NEXT.equals(type)) {
					MusicManager.getInstance().onEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NEXT, null);
				}
			}

			@Override
			public boolean needAsrState() {
				return false;
			}

			@Override
			public String getTaskId() {
				return "txz_free_wakeUp_callback";
			}
		};
		freeWakeupCallback.addCommand(ASR_CMD_PLAYER_NEXT,
				GlobalContext.get().getResources().getStringArray(R.array.asr_cmd_player_next));
		freeWakeupCallback.addCommand(ASR_CMD_PLAYER_PREVIOUS,
				GlobalContext.get().getResources().getStringArray(R.array.asr_cmd_player_previous));
		freeWakeupCallback.addCommand(ASR_CMD_PLAYER_PLAY,
				GlobalContext.get().getResources().getStringArray(R.array.asr_cmd_player_play));
		freeWakeupCallback.addCommand(ASR_CMD_PLAYER_PAUSE,
				GlobalContext.get().getResources().getStringArray(R.array.asr_cmd_player_pause));
		WakeupManager.getInstance().useWakeupAsAsr(freeWakeupCallback);
	}
}
