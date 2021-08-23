package com.txznet.txz.component.audio.txz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.component.audio.IAudio;
import com.txznet.txz.ui.win.record.RecorderWin;

public class AudioImpl implements IAudio {

	public static String mTxzMusicToolCurrentMusicModel;

	public static final int AUDIOFLAG = 2;

	@Override
	public String getPackageName() {
		return ServiceManager.MUSIC;
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
		MusicModel musicModel = new MusicModel();
		JSONBuilder builder = new JSONBuilder(jsonData);
		musicModel.setArtist(builder.getVal("artists", String[].class));
		musicModel.setKeywords(new String[] { builder.getVal("tag", String.class) });
		musicModel.setAlbum(builder.getVal("album", String.class));
		musicModel.setTitle(builder.getVal("title", String.class));

		musicModel.setField(AUDIOFLAG);
		LogUtil.logd("playFm::toString()::" + musicModel.toString());
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.sound.find", musicModel.toString().getBytes(), null);
		
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

}
