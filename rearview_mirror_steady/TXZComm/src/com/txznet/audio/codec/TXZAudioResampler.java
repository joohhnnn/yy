package com.txznet.audio.codec;

public class TXZAudioResampler {
	static {
		System.loadLibrary("TXZAudio");
	}

	/**
	 * 
	 * @param quality
	 *            0~10
	 * @param channels
	 * @param rateIn
	 * @param rateOut
	 * @return
	 */
	private native static long createResampler(int quality, int channels,
			int rateIn, int rateOut);

	private native static void destroyResampler(long handler);

	private native static int runResampler(long handler, byte[] dataIn,
			int offsetIn, int lenIn, byte[] dataOut, int offsetOut);

	// /////////////////////////////////////////////////////////////////////////////////////////

	public static class Resampler {
		private long handler;

		public Resampler(int channels, int rateIn, int rateOut) {
			handler = TXZAudioResampler.createResampler(10, channels, rateIn,
					rateOut);
		}

		public Resampler(int rateIn, int rateOut) {
			handler = TXZAudioResampler.createResampler(10, 1, rateIn, rateOut);
		}

		public synchronized void release() {
			TXZAudioResampler.destroyResampler(handler);
			handler = 0;
		}

		public synchronized int resample(byte[] dataIn, int offsetIn,
				int lenIn, byte[] dataOut, int offsetOut) {
			if (handler == 0)
				return -1;
			return TXZAudioResampler.runResampler(handler, dataIn, offsetIn,
					lenIn, dataOut, offsetOut);
		}
	}
}
