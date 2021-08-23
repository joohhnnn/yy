package com.txznet.txz.util.recordcenter.cache;

import java.io.IOException;

import com.txznet.comm.remote.util.LogUtil;

/**
 * 回溯型缓冲区
 */
public class TraceCacheBuffer {
	public final static int DEFAULT_CACHE_SIZE = 1 * 2 * 16000; // 默认按1s的单声道的语音大小
	private int mCacheSize = DEFAULT_CACHE_SIZE;
	// 内部维护的录音buffer
	private byte[] mBuffer = null;
	// 内部buffer写数据头(当前有效数据尾)
	private int mWriteIndex = 0;

	public TraceCacheBuffer() {
		this(DEFAULT_CACHE_SIZE);
	}

	public TraceCacheBuffer(int cacheSize) {
		mCacheSize = cacheSize;
	}

	public void write(byte[] data, int offset, int len) {
		// 分配内存
		if (mBuffer == null) {
			mBuffer = new byte[mCacheSize];
		}
		// 防止内存越界
		if (len > mBuffer.length) {
			offset += len - mBuffer.length;
			len = mBuffer.length;
		}
		// 写数据到对应buffer
		if (len <= mBuffer.length - mWriteIndex) {
			System.arraycopy(data, offset, mBuffer, mWriteIndex, len);
		} else {
			int remainLength = mBuffer.length - mWriteIndex;
			System.arraycopy(data, offset, mBuffer, mWriteIndex, remainLength);
			System.arraycopy(data, offset + remainLength, mBuffer, 0, len
					- remainLength);
		}

		mWriteIndex = (mWriteIndex + len) % mBuffer.length;
	}
	
	public int readAll(DataWriter writer) throws IOException {
		return readBySize(writer, mCacheSize);
	}

	public int readBySize(DataWriter writer, int dataLength) throws IOException {
		LogUtil.logd("TraceCacheBuffer readBySize, dataLength=" + dataLength);

		if (dataLength <= 0)
			return 0;

		if (mBuffer == null) {
			LogUtil.loge("TraceCacheBuffer readBySize null buffer");
			return 0;
		}

		LogUtil.logd("TraceCacheBuffer readBySize buffer length=" + mBuffer.length
				+ ", write=" + mWriteIndex);

		int dataTail = mWriteIndex;

		if (dataLength <= dataTail) {
			return writer.writeData(mBuffer, dataTail - dataLength, dataLength);
		} else {
			// 最多只取缓冲区长度的数据，防止越界
			if (dataLength > mBuffer.length) {
				dataLength = mBuffer.length;
			}

			return writer.writeData(mBuffer, mBuffer.length
					- (dataLength - dataTail), dataLength - dataTail)
					+ writer.writeData(mBuffer, 0, dataTail);
		}
	}
}
