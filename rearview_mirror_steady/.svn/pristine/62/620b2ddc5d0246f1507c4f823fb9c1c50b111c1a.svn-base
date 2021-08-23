package com.txznet.txz.component.audio.kaola;

import com.kaolafm.sdk.client.PlayState;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.txz.component.audio.IAudio;
import com.txznet.txz.component.music.kaola.AdapterKaola;
import com.txznet.txz.jni.JNIHelper;

public class AudioKaoLaImpl implements IAudio {
	public static final String PACKAGE_NAME = "com.edog.car";

	public AudioKaoLaImpl() {
		AdapterKaola.getAdapter();
	}

	@Override
	public String getPackageName() {
		return AdapterKaola.getAdapter().getPackageName();
	}

	public PlayState getPlayState() {
		return AdapterKaola.getAdapter().getPlayState();
	}

	@Override
	public void start() {
		AdapterKaola.getAdapter().start();
	}

	@Override
	public void pause() {
		AdapterKaola.getAdapter().pause();
	}

	@Override
	public void playFm(final String jsonData) {
		JSONBuilder builder = new JSONBuilder(jsonData);
		final String keyWord = StringUtils.toString(builder.getVal("keywords", String[].class));
		JNIHelper.logd("search fm:" + keyWord);
		AdapterKaola.getAdapter().searchMusic(keyWord);
	}

	@Override
	public void exit() {
		AdapterKaola.getAdapter().exit();
	}

	@Override
	public void next() {
		AdapterKaola.getAdapter().next();
	}

	@Override
	public void prev() {
		AdapterKaola.getAdapter().prev();
	}

	@Override
	public String getCurrentFmName() {
		return AdapterKaola.getAdapter().getCurrentMusicName();
	}

	@Override
	public void cancelRequest() {
		AdapterKaola.getAdapter().cancelRequest();
	}
}