package com.txznet.audio.codec;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class TXZMp3Decoder {
	static {
		System.loadLibrary("TXZMp3Decode");
	}

	private native long nativeCreate();

	private native void nativeRelease(long h);

	private native int nativeRun(long h);

	/**
	 * 结束返回0，出错返回-1
	 * 
	 */
	public abstract int read(byte[] data, int offset, int len);

	public abstract int write(int channels, int sample_rate, byte[] data,
			int offset, int len);

	public int run() {
		return this.nativeRun(handler);
	}

	long handler = 0;

	public void init() {

	}

	public TXZMp3Decoder() {
		handler = this.nativeCreate();

		this.init();
	}

	public void release() {
		this.nativeRelease(handler);
		handler = 0;
	}

	// ////////////////////////////////////////////////////////////////////

	/**
	 * 文件转换示例代码
	 *
	 */
	public static class TXZMp3FileDecoder extends TXZMp3Decoder {
		InputStream in;
		OutputStream out;

		public TXZMp3FileDecoder(InputStream _in, OutputStream _out) {
			this.in = _in;
			this.out = _out;
		}

		public TXZMp3FileDecoder(String _in, String _out) {
			try {
				in = new FileInputStream(_in);
				out = new FileOutputStream(_out);
			} catch (Exception e) {
			}
		}

		@Override
		public void release() {
			try {
				in.close();
				out.close();
			} catch (Exception e) {
			}
			super.release();
		}

		@Override
		public int write(int channels, int sample_rate, byte[] data,
				int offset, int len) {
			try {
				out.write(data, offset, len);
			} catch (Exception e) {
			}

			return 0;
		}

		@Override
		public int read(byte[] data, int offset, int len) {
			try {
				while (true) {
					int ret = in.read(data, offset, len);
					if (ret < 0) {
						return 0;
					}
					if (ret > 0) {
						return ret;
					}
				}
			} catch (Exception e) {
				return -1;
			}
		}

		/**
		 * 测试参考代码
		 */
		public static void test() {
			TXZMp3FileDecoder dec = new TXZMp3FileDecoder("test.mp3",
					"test.pcm");
			dec.run();
			dec.release();
		}
	}

}
