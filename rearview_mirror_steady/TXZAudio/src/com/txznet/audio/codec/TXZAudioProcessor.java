package com.txznet.audio.codec;

import com.txznet.txz.util.LittleEndianBytesUtil;

public class TXZAudioProcessor {
	static {
		System.loadLibrary("TXZAudio");
	}

	private native static long createPreprocessor(int frame_size,
			int sampling_rate);

	private native static void destroyPreprocessor(long processor);

	private native static int controlPreprocessor(long processor, int request,
			byte[] param);

	private native static void updatePreprocessor(long processor, byte[] data,
			int offset, int len);

	private native static int runPreprocessor(long processor, byte[] data,
			int offset, int len);

	// /////////////////////////////////////////////////////////////////////////////////////////

	public final static int SPEEX_PREPROCESS_SET_AGC = 2;

	public final static int SPEEX_PREPROCESS_SET_AGC_LEVEL = 6;

	// /////////////////////////////////////////////////////////////////////////////////////////

	public static class Preprocessor {
		private long processor;

		public Preprocessor(int frame_size, int sampling_rate) {
			processor = TXZAudioProcessor.createPreprocessor(frame_size,
					sampling_rate);
		}

		public synchronized void release() {
			TXZAudioProcessor.destroyPreprocessor(processor);
			processor = 0;
		}

		public synchronized int control(int request, byte[] param) {
			if (processor == 0)
				return -1;
			return TXZAudioProcessor.controlPreprocessor(processor, request,
					param);
		}

		public int control(int request, int param) {
			return control(request, LittleEndianBytesUtil.intToBytes(param));
		}

		public int control(int request, float param) {
			return control(request, LittleEndianBytesUtil.floatToBytes(param));
		}

		public int AGC(float param) {
			int ret = control(TXZAudioProcessor.SPEEX_PREPROCESS_SET_AGC, 1);
			if (ret != 0)
				return ret;
			control(TXZAudioProcessor.SPEEX_PREPROCESS_SET_AGC_LEVEL, param);
			return ret;
		}

		public synchronized int run(byte[] data, int offset, int len) {
			if (processor == 0)
				return -1;
			return TXZAudioProcessor.runPreprocessor(processor, data, offset,
					len);
		}

		public synchronized void update(byte[] data, int offset, int len) {
			if (processor == 0)
				return;
			TXZAudioProcessor.updatePreprocessor(processor, data, offset, len);
		}
	}
}
