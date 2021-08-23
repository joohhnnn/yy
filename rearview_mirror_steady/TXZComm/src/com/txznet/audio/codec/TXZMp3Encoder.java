package com.txznet.audio.codec;

public class TXZMp3Encoder {
	private native static long createEncoder(
	/**
	 * 通道数 1为单声道，2为双声道
	 */
	int channels,
	/**
	 * 采样率
	 */
	int sample_rate,
	/**
	 * 编码质量，默认3
	 */
	int quality,
	/**
	 * 编码模式
	 */
	int mode,
	/**
	 * VBR编码模式
	 */
	int vbr_mode,
	/**
	 * 比特率，单位kbps
	 */
	int brate);

	private native static void destroyEncoder(long handler);

	private native static int writeEncoder(long handler, byte[] dataIn,
			int offsetIn, int lenIn);

	private native static int completeEncoder(long handler);

	private native static int readEncoder(long handler, byte[] dataOut,
			int offsetOut);

	public static final int DEFAULT_PARAM = -1;

	public static final int CHANNEL_MONO = 1;
	public static final int CHANNEL_STEARO = 2;

	public static final int QUALITY_BSET = 0;
	public static final int QUALITY_NEARBEST = 3;
	public static final int QUALITY_GOOD = 5;
	public static final int QUALITY_OK = 7;
	public static final int QUALITY_WORST = 10;
	public static final int QUALITY_DEFAULT = QUALITY_NEARBEST;

	public static final int MODE_STEREO = 0;
	public static final int MODE_JOINT_STEREO = 1;
	@Deprecated
	public static final int MODE_DUAL_CHANNEL = 2;
	public static final int MODE_MONO = 3;
	public static final int MODE_NOT_SET = 4;
	public static final int MODE_NOT_DEFAULT = MODE_NOT_SET;

	public static final int VBR_MODE_OFF = 0;
	public static final int VBR_MODE_MT = 1;
	public static final int VBR_MODE_RH = 2;
	public static final int VBR_MODE_ABR = 3;
	public static final int VBR_MODE_MTRH = 4;
	public static final int VBR_MODE_MAX_INDICATOR = 5;
	public static final int VBR_MODE_DEFAULT = VBR_MODE_MTRH;

	// /////////////////////////////////////////////////////////////////////////////////////////

	static {
		System.loadLibrary("TXZMp3Encode");
	}
	private long handler;

	public TXZMp3Encoder() {
		this(DEFAULT_PARAM);
	}

	public TXZMp3Encoder(int quality) {
		handler = TXZMp3Encoder.createEncoder(1, 16000, quality, DEFAULT_PARAM,
				DEFAULT_PARAM, DEFAULT_PARAM);
	}

	public TXZMp3Encoder(
	/**
	 * 通道数 1为单声道，2为双声道
	 */
	int channels,
	/**
	 * 采样率
	 */
	int sample_rate,
	/**
	 * 编码质量，默认3
	 */
	int quality,
	/**
	 * 编码模式
	 */
	int mode,
	/**
	 * VBR编码模式
	 */
	int vbr_mode,
	/**
	 * 比特率，单位kbps
	 */
	int brate) {
		handler = TXZMp3Encoder.createEncoder(channels, sample_rate, quality,
				mode, vbr_mode, brate);
	}

	public synchronized void release() {
		TXZMp3Encoder.destroyEncoder(handler);
		handler = 0;
	}

	public synchronized int write(byte[] dataIn, int offsetIn, int lenIn) {
		if (handler == 0)
			return -1;
		return TXZMp3Encoder.writeEncoder(handler, dataIn, offsetIn, lenIn);
	}

	public synchronized int complete() {
		if (handler == 0)
			return -1;
		return TXZMp3Encoder.completeEncoder(handler);
	}

	public synchronized int read(byte[] dataOut, int offsetOut) {
		if (handler == 0)
			return -1;
		return TXZMp3Encoder.readEncoder(handler, dataOut, offsetOut);
	}

	public static class Builder {
		private int channels;
		private int sample_rate;
		private int quality = DEFAULT_PARAM;
		private int mode = DEFAULT_PARAM;
		private int vbr_mode = DEFAULT_PARAM;
		private int brate = DEFAULT_PARAM;

		public Builder(int _channles, int _sample_rate) {
			channels = _channles;
			sample_rate = _sample_rate;
		}

		public Builder setQuality(int param) {
			quality = param;
			return this;
		}

		public Builder setMode(int param) {
			mode = param;
			return this;
		}

		public Builder setVBRMode(int param) {
			vbr_mode = param;
			return this;
		}

		public Builder setBitRate(int param) {
			brate = param;
			return this;
		}

		public TXZMp3Encoder build() {
			return new TXZMp3Encoder(channels, sample_rate, quality, mode,
					vbr_mode, brate);
		}
	}
}
