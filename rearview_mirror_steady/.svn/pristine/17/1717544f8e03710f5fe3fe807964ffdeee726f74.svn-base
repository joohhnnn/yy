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
		
		public synchronized int resample_48k216k(int channel, byte[] dataIn, int offsetIn,
				int lenIn, byte[] dataOut, int offsetOut){
			int count = 0;
			for (int k = 0, i = 0; i + 1 < lenIn && k + 1 < dataOut.length; i = i + 6, k = k + 2){
				dataOut[k + 0] = dataIn[i + 0];
				dataOut[k + 1] = dataIn[i + 1];
				count = count + 2;
			}
			return count;
		}
	}
}
