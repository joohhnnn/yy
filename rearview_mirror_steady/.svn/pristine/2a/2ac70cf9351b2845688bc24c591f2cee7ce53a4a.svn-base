package com.txznet.txz.component.audio.txz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.audio.IAudio;
import com.txznet.txz.component.music.ITxzMedia;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.ui.win.record.RecorderWin;

public class AudioImpl implements IAudio, ITxzMedia {
	public static final String PACKAGE_NAME = ServiceManager.MUSIC;
	public static String mTxzMusicToolCurrentMusicModel;

	public static final int AUDIOFLAG = 2;

	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public void start() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "audio.play", null, null);
	}

	@Override
	public void pause() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.pause", null, null);
	}

	@Override
	public void exit() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.exit", null, null);
	}

	@Override
	public void next() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.next", null, null);
	}

	@Override
	public void prev() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.prev", null, null);
	}

	@Override
	public void playFm(String jsonData) {
		LogUtil.logd(AudioManager.TAG + "playFm::toString()::" + jsonData.toString());
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.find", jsonData.getBytes(), null);

		RecorderWin.addCloseRunnable(new Runnable() {
			@Override
			public void run() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.cancelfind", null, null);
			}
		});
	}

	@Override
	public String getCurrentFmName() {
		if (mTxzMusicToolCurrentMusicModel == null || mTxzMusicToolCurrentMusicModel.length() == 0)
			return null;
		JSONObject jsonObject = JSON.parseObject(mTxzMusicToolCurrentMusicModel);
		return jsonObject.getString("name");
	}

	@Override
	public void cancelRequest() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.cancelfind", null, null);
	}

	@Override
	public void openApp() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.open", null, null);
	}

	@Override
	public void openAndPlay() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "audio.open.play", null, null);
	}

	@Override
	public void play() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.play", null, null);

	}

	@Override
	public void hateAudio() {
		//TODO:暂时不支持
	}

	@Override
	public void switchPlayModeToOnce() {
		// if (com.txznet.comm.util.StringUtils.isNotEmpty(getMusicVersion())) {
		// ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
		// "music.switchModeLoopAll", null, null);
		// } else {
		// String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
		// RecorderWin.speakText(spk, null);
		// }
	}

	@Override
	public void addSubscribe() {
		// if (com.txznet.comm.util.StringUtils.isNotEmpty(getMusicVersion())) {
		// ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
		// "music.addSubscribe", null, null);
		// } else {
		// String spk = NativeData.getResString("RS_VOICE_FUNCTION_UNSUPPORT");
		// RecorderWin.speakText(spk, null);
		// }
	}

	@Override
	public boolean supportRequestHistory() {
		ServiceManager.ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.MUSIC, "music.history.support", null);
		if (serviceData == null) {
			return false;
		}
		byte[] result = serviceData.getBytes();
		if (result != null) {
			try {
				return Boolean.parseBoolean(new String(result));
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}

	@Override
	public void requestHistory(String type) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("type", type);
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.history.find", jsonBuilder.toBytes(), null);
		RecorderWin.addCloseRunnable(new Runnable() {
			@Override
			public void run() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.history.cancelfind", null, null);
			}
		});
	}

}
