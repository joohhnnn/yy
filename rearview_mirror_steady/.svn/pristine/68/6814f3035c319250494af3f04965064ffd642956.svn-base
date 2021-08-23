package com.txznet.txz.util;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.ByteBuffer;

import android.media.AudioFormat;
import android.media.AudioRecord;

public class TXZAECAudioRecorder extends AudioRecord {
	public final static int BUFFER_SIZE_READ = 1200;

	private boolean mRightCompare = true;
	private PipedInputStream mPipeRead = null;
	private PipedOutputStream mPipeWrite = null;
	private Thread mRecordThread = null;
	private boolean mRecordWorking = true;
	private RuntimeException mLastError = null;
	private int mReadError = 0;

	private void buildPipe() {
		mPipeRead = new PipedInputStream();
		mPipeWrite = new PipedOutputStream();
		try {
			mPipeRead.connect(mPipeWrite);
		} catch (IOException e) {
		}
	}

	private void releasePipe() {
		if (this.mPipeRead != null) {
			try {
				this.mPipeRead.close();
			} catch (Exception e) {
			}
			this.mPipeRead = null;
		}
		if (this.mPipeWrite != null) {
			try {
				this.mPipeWrite.close();
			} catch (Exception e) {
			}
			this.mPipeWrite = null;
		}
	}

	private void buildRecordThread() {
		this.mRecordThread = new Thread() {
			private boolean writeRecordData(byte[] data) {
				try {
					TXZAECAudioRecorder.this.mPipeWrite.write(data, 0,
							data.length);
				} catch (IOException e) {
					TXZAECAudioRecorder.this.mLastError = new IllegalStateException(
							"write record data error: " + e.getMessage());
					return false;
				} catch (RuntimeException re) {
					TXZAECAudioRecorder.this.mLastError = re;
					return false;
				}
				return true;
			}

			@Override
			public void run() {
				com.unisound.jni.AEC aec = new com.unisound.jni.AEC(16000, 1);
				aec.setOptionInt(2, 1);
				aec.setOptionInt(3, TXZAECAudioRecorder.this.mRightCompare ? 0
						: 1);

				byte[] buf = new byte[BUFFER_SIZE_READ];
				while (TXZAECAudioRecorder.this.mRecordWorking) {
					try {
						TXZAECAudioRecorder.this.mReadError = TXZAECAudioRecorder.super
								.read(buf, 0, buf.length);
					} catch (RuntimeException re) {
						TXZAECAudioRecorder.this.mLastError = re;
						break;
					}
					if (TXZAECAudioRecorder.this.mReadError < 0) {
						break;
					}
					if (TXZAECAudioRecorder.this.mReadError == 0) {
						continue;
					}
					byte[] data;
					if (TXZAECAudioRecorder.this.mReadError < buf.length) {
						data = new byte[TXZAECAudioRecorder.this.mReadError];
						System.arraycopy(buf, 0, data, 0,
								TXZAECAudioRecorder.this.mReadError);
					} else {
						data = buf;
					}
					byte[] ret = aec.process(data, null);
					writeRecordData(ret);
				}

				writeRecordData(aec.getlast());
			}
		};

		mRecordThread.start();
	}

	private void releaseRecordThread() {
		this.mRecordWorking = false;
		if (this.mRecordThread != null) {
			try {
				this.mRecordThread.interrupt();
			} catch (Exception e) {
			}
			this.mRecordThread = null;
		}
	}

	public TXZAECAudioRecorder(int audioSource, int sampleRateInHz,
			boolean rightCompare) throws IllegalArgumentException {
		super(audioSource, sampleRateInHz, AudioFormat.CHANNEL_IN_STEREO,
				AudioFormat.ENCODING_PCM_16BIT, AudioRecord.getMinBufferSize(
						sampleRateInHz, AudioFormat.CHANNEL_IN_STEREO,
						AudioFormat.ENCODING_PCM_16BIT));

		this.mRightCompare = rightCompare;
	}

	@Override
	public int getChannelConfiguration() {
		return AudioFormat.CHANNEL_IN_MONO;
	}

	@Override
	public int getChannelCount() {
		return 1;
	}

	@Override
	public void release() {
		releaseRecordThread();

		releasePipe();

		super.release();
	}

	@Override
	public int read(byte[] audioData, int offsetInBytes, int sizeInBytes) {
		if (this.mReadError < 0) {
			return this.mReadError;
		}
		if (this.mLastError != null) {
			throw this.mLastError;
		}
		try {
			return this.mPipeRead.read(audioData, offsetInBytes, sizeInBytes);
		} catch (IOException e) {
			throw new IllegalStateException("read record data error: "
					+ e.getMessage());
		}
	}

	@Override
	public void startRecording() throws IllegalStateException {
		mLastError = null;
		mReadError = 0;

		buildPipe();

		super.startRecording();
		buildRecordThread();
	}

	@Override
	public void stop() throws IllegalStateException {
		super.stop();
		releaseRecordThread();

		releasePipe();
	}

	@Override
	public int read(short[] audioData, int offsetInShorts, int sizeInShorts) {
		throw new RuntimeException("not support read short data");
	}

	@Override
	public int read(ByteBuffer audioBuffer, int sizeInBytes) {
		byte[] data = new byte[BUFFER_SIZE_READ];
		int len = this.read(data, 0, data.length);
		if (len > 0) {
			audioBuffer.put(data, 0, len);
		}
		return len;
	}
}
