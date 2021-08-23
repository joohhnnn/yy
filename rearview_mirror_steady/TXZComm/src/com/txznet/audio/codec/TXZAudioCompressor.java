package com.txznet.audio.codec;

public class TXZAudioCompressor {
	static {
		System.loadLibrary("TXZAudio");
	}

	private native static long createEncoder(int quality);

	private native static void destroyEncoder(long handler);

	private native static int writeEncoder(long handler, byte[] dataIn,
			int offsetIn, int lenIn);

	private native static int readEncoder(long handler, byte[] dataOut,
			int offsetOut);

	private native static long createDecoder();

	private native static void destroyDecoder(long handler);

	private native static int writeDecoder(long handler, byte[] dataIn,
			int offsetIn, int lenIn);

	private native static int readDecoder(long handler, byte[] dataOut,
			int offsetOut);

	public static final int ENCODE_QUILITY_BEST = 10;
	public static final int ENCODE_QUILITY_WORST = 0;
	public static final int ENCODE_QUILITY_DEFAULT = 8;

	// /////////////////////////////////////////////////////////////////////////////////////////

	public static class Encoder {
		private long handler;

		public Encoder() {
			this(ENCODE_QUILITY_DEFAULT);
		}

		public Encoder(int quality) {
			handler = TXZAudioCompressor.createEncoder(quality);
		}

		public synchronized void release() {
			TXZAudioCompressor.destroyEncoder(handler);
			handler = 0;
		}

		public synchronized int write(byte[] dataIn, int offsetIn, int lenIn) {
			if (handler == 0)
				return -1;
			return TXZAudioCompressor.writeEncoder(handler, dataIn, offsetIn,
					lenIn);
		}

		public synchronized int read(byte[] dataOut, int offsetOut) {
			if (handler == 0)
				return -1;
			return TXZAudioCompressor.readEncoder(handler, dataOut, offsetOut);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////////////

	public static class Decoder {
		private long handler;

		public Decoder() {
			handler = TXZAudioCompressor.createDecoder();
		}

		public synchronized void release() {
			TXZAudioCompressor.destroyDecoder(handler);
			handler = 0;
		}

		public synchronized int write(byte[] dataIn, int offsetIn, int lenIn) {
			if (handler == 0)
				return -1;
			return TXZAudioCompressor.writeDecoder(handler, dataIn, offsetIn,
					lenIn);
		}

		public synchronized int read(byte[] dataOut, int offsetOut) {
			if (handler == 0)
				return -1;
			return TXZAudioCompressor.readDecoder(handler, dataOut, offsetOut);
		}
	}
}
