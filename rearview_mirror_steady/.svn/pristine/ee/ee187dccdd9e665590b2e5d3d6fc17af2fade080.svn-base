package com.txznet.audio.codec;

public class TXZAudioEchoCancel {
	static {
		System.loadLibrary("TXZAudio");
	}

	private native static long createEchoCancel(int frame_size,
			int filter_length);

	private native static void destroyEchoCancel(long handler);

	private native static int runEchoCancel(long handler, byte[] dataIn,
			int offsetIn, int lenIn, byte[] dataCmp, int offsetCmp,
			byte[] dataOut, int offsetOut);

	// /////////////////////////////////////////////////////////////////////////////////////////

	public static class EchoCancel {
		private long handler;

		public EchoCancel(int frame_size, int filter_length) {
			handler = TXZAudioEchoCancel.createEchoCancel(frame_size,
					filter_length);
		}

		public synchronized void release() {
			TXZAudioEchoCancel.destroyEchoCancel(handler);
			handler = 0;
		}

		public synchronized int process(byte[] dataIn, int offsetIn,
				int lenIn, byte[] dataCmp, int offsetCmp, byte[] dataOut,
				int offsetOut) {
			if (handler == 0)
				return -1;
			return TXZAudioEchoCancel.runEchoCancel(handler, dataIn, offsetIn,
					lenIn, dataCmp, offsetCmp, dataOut, offsetOut);
		}
	}
}
