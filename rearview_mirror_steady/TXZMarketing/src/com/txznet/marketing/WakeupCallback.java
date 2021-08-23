package com.txznet.marketing;

import android.util.Log;

import com.txznet.marketing.bean.CommandPoint;
import com.txznet.marketing.ui.MediaPlayerSurfaceView;
import com.txznet.sdk.TXZAsrManager.AsrComplexSelectCallback;
import com.txznet.sdk.TXZTtsManager;

public class WakeupCallback extends AsrComplexSelectCallback {

	public boolean isCommandSelected() {
		return isCommandSelected;
	}

	public void setCommandSelected(boolean commandSelected) {
		isCommandSelected = commandSelected;
	}

	private boolean isCommandSelected = false;

	@Override
	public String getTaskId() {
		return "MARKETING_WAKEUP_TASK";
	}

	@Override
	public boolean needAsrState() {
		return true;
	}

	@Override
	public void onCommandSelected(String type, String command) {
		Log.d("jack", "onCommandSelected: "+command);
		if (command
				.equals(CommandPoint.commandArr[MediaPlayerSurfaceView.currIndex])) {
			setCommandSelected(true);
			MediaPlayerSurfaceView.getInstance().dismissAndPlay();
		}

	}



}
