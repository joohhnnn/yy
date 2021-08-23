package com.txznet.audio.client;

import com.txznet.audio.player.MediaError;

public abstract class TXZMediaClient {

	public abstract void request();

	public abstract void seek(long offset);

	public abstract long getTotalSize();
	
	public abstract long getDownloadSize();

	public abstract void cancel();

	// //////////////////////////////////////////////////////////////////

	public interface OnResponseListener {
		public void onGetInfo();
		
		public void onSeek();
		
		public void onRecive(float percent, byte[] data);

		public void onEnd();
		
		public void onError(MediaError err);
	}

	protected OnResponseListener mOnResponseListener;

	public void setOnResponseListener(OnResponseListener listener) {
		mOnResponseListener = listener;
	}

	protected String mUrl;

	protected TXZMediaClient(String url) {
		mUrl = url;
	}

	public String getUrl() {
		return mUrl;
	}
}
