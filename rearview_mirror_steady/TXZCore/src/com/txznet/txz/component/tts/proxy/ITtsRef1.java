package com.txznet.txz.component.tts.proxy;

import android.content.Context;

import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.component.tts.ITtsRefVersion;

public abstract class ITtsRef1 implements ITts, ITtsRefVersion {
	
	public static final String ASSETS_PATH = "ASSETS_PATH";
	
	@Override
	public int getVersion() {
		return ITtsRefVersion.VERSION_1;
	}

	/**
	 * 设置参数，在initialize之前调用
	 * @param context 
	 * @param data
	 */
	public abstract void setArguments(Context context, byte[] data);
	
	@Override
	public void setOption(TTSOption oOption) {}

}
