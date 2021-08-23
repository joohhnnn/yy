package com.txznet.txz.util.recordcenter.cache;

import java.io.IOException;

import android.os.SystemClock;

import com.txznet.comm.remote.util.LogUtil;

/**
 * 回溯型缓冲区
 */
public class TraceCacheBuffer_PcmMono16K extends TraceCacheBuffer {
	public final static int DEFAULT_CACHE_SIZE = 1 * 2 * 16000; // 默认按1s的单声道的语音大小

	public TraceCacheBuffer_PcmMono16K() {
		this(DEFAULT_CACHE_SIZE);
	}

	public TraceCacheBuffer_PcmMono16K(int cacheSize) {
		super(cacheSize);
	}

	public int readByClock(DataWriter writer, long clock) throws IOException {
		LogUtil.logd("TraceCacheBuffer_PcmMono16K readByClock, clock=" + clock);

		if (clock <= 0)
			return 0;

		int millSecond = (int) (SystemClock.elapsedRealtime() - clock);
		return readByDurnation(writer, millSecond);
	}

	public int readByDurnation(DataWriter writer, int millSecond)
			throws IOException {
		LogUtil.logd("TraceCacheBuffer_PcmMono16K readByDurnation, millSecond=" + millSecond);

		if (millSecond <= 0)
			return 0;

		int dataLength = (int) (millSecond * 32);
		return super.readBySize(writer, dataLength);
	}
}
