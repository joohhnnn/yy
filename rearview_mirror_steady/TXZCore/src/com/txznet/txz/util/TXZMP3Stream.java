package com.txznet.txz.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import android.os.Handler;
import android.os.HandlerThread;

import com.txznet.audio.codec.TXZMp3Decoder;
import com.txznet.txz.util.AudioTrackPlayer.Decryption;

public class TXZMP3Stream  extends InputStream{
	private static HandlerThread sDecodeThread = null;
	private static Handler sDecodeHandler = null;
	
	private TXZMp3StreamDecoder mDecoder = null;
	public TXZMP3Stream(InputStream in, Decryption decryption) throws IOException{
		mDecoder = new TXZMp3StreamDecoder(in, decryption);
		run(oDecodeTask);
	}
	
	private Runnable oDecodeTask = new Runnable(){
		@Override
		public void run() {
			if (mDecoder != null) {
				mDecoder.run();
			}
		}
	};
	
	private static void run(Runnable oRun){
		if (sDecodeHandler == null){
			synchronized (TXZMP3Stream.class) {
				if (sDecodeHandler == null){
					sDecodeThread = new HandlerThread("mp3_stream_decode_thread");
					sDecodeThread.start();
					sDecodeHandler = new Handler(sDecodeThread.getLooper());
					sDecodeHandler.post(new Runnable() {
						@Override
						public void run() {
							TXZHandler.updateMaxPriority();
						}
					});
				}
			}
		}
		sDecodeHandler.postDelayed(oRun, 0);
	}
	
	public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
		return mDecoder.read(buffer, byteOffset, byteCount, true);
	};
	
	@Override
	public void close(){
		mDecoder.release();
	}
	
	public static class TXZMp3StreamDecoder extends TXZMp3Decoder {
		private PipedInputStream mInPipe = null;
		private PipedOutputStream mOut = null;
		private InputStream mIn = null;
		private boolean isBusy = false;
		private long mDecodedStreamLength = 0;
		private long mReadStreamLenght = 0;
		private Decryption mDecryption = null;
		private long mInputStreamOffset = 0;
		
		public TXZMp3StreamDecoder(InputStream in, Decryption decryption) throws IOException {
			mIn = in;
			mInPipe = new PipedInputStream(1024*128);
			mOut = new PipedOutputStream();
			mInPipe.connect(mOut);
			mDecryption = decryption;
			mInputStreamOffset  = 0;
			mOut.flush();
			isBusy = true;
		}
		
		public boolean isEnd(){
			return !isBusy && mDecodedStreamLength <= mReadStreamLenght;//解码结束and所有解码的数据全部被取走
		}
		
		@Override
		public synchronized void release() {
			if (mIn != null) {
				try {
					mIn.close();
				} catch (Exception e) {
				}
				mIn = null;
			}
			
			if (mOut != null) {
				try {
					mOut.close();
				} catch (Exception e) {
				}
				mOut = null;
			}

			if (mInPipe != null) {
				try {
					mInPipe.close();
				} catch (Exception e) {
				}
				mInPipe = null;
			}
		}
		
		@Override
		public int run() {
			int ret = super.run();
			isBusy = false;
			super.release();
			//解码数据为空,释放资源
			if (mDecodedStreamLength == 0){
				release();
			}
			return ret;
		}

		@Override
		public int write(int channels, int sample_rate, byte[] data,
				int offset, int len) {
			try {
				mOut.write(data, offset, len);
				mDecodedStreamLength += len;
			} catch (Exception e) {
			}

			return 0;
		}

		@Override
		public int read(byte[] data, int offset, int len) {
			try {
				while (true) {
					int ret = mIn.read(data, offset, len);
					if (ret < 0) {
						return 0;
					}
					if (ret > 0) {
						if (mDecryption != null){
							mDecryption.decrypt(data, offset, ret, mInputStreamOffset);
							mInputStreamOffset += ret;
						}
						return ret;
					}
				}
			} catch (Exception e) {
				return -1;
			}
		}
		
		public int read(byte[] data, int offset, int len, boolean flag) throws IOException {
			int read = -1;
			if (!isEnd()){
				read = mInPipe.read(data, offset, len);
				if (read > 0){
					mReadStreamLenght += read;
				}
			}
			return read;
	}

}

	@Override
	public int read() throws IOException {
		return 0;
	}
}
