package com.txznet.txz.util.recordcenter.cache;

import java.io.IOException;

import com.txznet.comm.remote.util.LogUtil;

/**
 * 丢弃型缓冲
 */
public class DiscardCacheBuffer {
	public final static int DEFAULT_CACHE_SIZE = 1 * 2 * 16000; // 默认按1s的单声道的语音大小
	private byte[] mBuffer = null; // 1s的缓存，超过丢弃
	private int mBufferReadIndex = 0;
	private int mBufferWriteIndex = 0;
	private int mFlushIndex = 0;

	public DiscardCacheBuffer() {
		this(DEFAULT_CACHE_SIZE);
	}

	public DiscardCacheBuffer(int cacheSize) {
		mBuffer = new byte[cacheSize];
	}
	
	public String getDebugId() {
		return this.toString();
	}

	public void flush() {
		mFlushIndex++;
		mBufferWriteIndex = mBufferReadIndex = 0;
		mFlushIndex++;
	}

	// 读取数据，一直读取到没有数据为止
	public void read(DataWriter writer, Runnable runIdle) throws IOException {
		while (true) {
			if (null != runIdle) {
				runIdle.run();
			}
			int fIndex = mFlushIndex;
			int writeIndex = mBufferWriteIndex;
			int readIndex = mBufferReadIndex;
			if (writeIndex == readIndex) {
				// 没有数据了
				// LogUtil.logw("empty record buffer : " + this.getDebugId());
				break;
			}
			int r;
			if (writeIndex > readIndex) {
				r = writer.writeData(mBuffer, readIndex, writeIndex
						- readIndex);
			} else {
				r = writer.writeData(mBuffer, readIndex, mBuffer.length
						- readIndex);
			}
			readIndex = (readIndex + r) % (mBuffer.length);
			if (fIndex != mFlushIndex) {
				return;
			}
			mBufferReadIndex = readIndex;
			// LogUtil.logd("update read[" + mBufferReadIndex + "]: " +
			// this.getDebugId());
		}
	}

	public int write(byte[] data, int offset, int len) {
		int ret = -1;
		int fIndex = mFlushIndex;
		int readIndex = mBufferReadIndex;
		int writeIndex = mBufferWriteIndex;
		if (readIndex > writeIndex) {
			ret = len;
			// 至少保留缓冲区4个字节，是为了做字节对齐
			if (len > readIndex - writeIndex
					- DataWriter.BUFFER_WRITE_ALIGN) {
				ret = readIndex - writeIndex
						- DataWriter.BUFFER_WRITE_ALIGN;
				LogUtil.logw("discard record data size[" + (len - ret)
						+ "], read[" + readIndex + "], write["
						+ writeIndex + "]: " + this.getDebugId());
			}
			System.arraycopy(data, offset, mBuffer, writeIndex, ret);
			writeIndex += ret;
		} else {
			if (len > mBuffer.length - writeIndex) {
				if (readIndex == 0) {
					ret = mBuffer.length - writeIndex - 1;
					// 缓冲区满了
					LogUtil.logw("discard record data size: " + (len - ret)
							+ "], read[" + readIndex + "], write["
							+ writeIndex + "]: " + this.getDebugId());
					System.arraycopy(data, offset, mBuffer, writeIndex,
							ret);
					writeIndex += ret;
				} else {
					ret = mBuffer.length - writeIndex;
					System.arraycopy(data, offset, mBuffer, writeIndex,
							ret);
					if (fIndex != mFlushIndex) {
						return -999;
					}
					mBufferWriteIndex = 0;
					// 循环写
					int w = write(data, offset + ret, len - ret);
					if (w < 0) {
						return w;
					}
					return w + ret;
				}
			} else {
				System.arraycopy(data, offset, mBuffer, writeIndex, len);
				writeIndex = (writeIndex + len) % mBuffer.length;
			}
		}
		
		if (fIndex != mFlushIndex) {
			return -999;
		}
		mBufferWriteIndex = writeIndex;

		return ret;
	}
}