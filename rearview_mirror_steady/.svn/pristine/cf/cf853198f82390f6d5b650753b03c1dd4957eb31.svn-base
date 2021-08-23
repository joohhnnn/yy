package com.txznet.audio.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Handler;
import android.os.HandlerThread;

import com.txznet.audio.player.MediaError;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;

public class HttpMediaClient extends TXZMediaClient {
	public static final int READ_BUFFER_SIZE = 10 * 1024;
	private byte[] mBuffer = new byte[READ_BUFFER_SIZE];
	HttpGet mHttpGetSession;
	HttpClient mHttpClient;
	private HandlerThread mHttpMediaThread = null;
	private Handler mHttpMediaHandler = null; //这个handler阻塞可能会比较长
	private Runnable mRunnableReleaseHandler = new Runnable() {
		public void run() {
			releaseHandler();
		}
	};

	private static class HttpGetSession extends HttpGet {
		boolean mCancel = false;

		public HttpGetSession(String url) {
			super(url);
		}

		@Override
		public void abort() {
			mCancel = true;
			super.abort();
		}
	}

	private void createHandler() {
		if (mHttpMediaThread == null) {
			mHttpMediaThread = new HandlerThread("HttpMediaReadThread#" + mUrl);
			mHttpMediaThread.start();
			mHttpMediaHandler = new Handler(mHttpMediaThread.getLooper());
		}
	}

	private synchronized void releaseHandler() {
		if (mHttpMediaThread != null) {
			mHttpMediaThread.quit();
			mHttpMediaThread = null;
			mHttpMediaHandler = null;
		}
	}

	public HttpMediaClient(String url) {
		super(url);

		mUrl = url;
	}

	private long mLength;
	private long mTotal = 0;

	// private Lock mLockSession = new ReentrantLock();

	@Override
	public synchronized void request() {
		AppLogic.removeBackGroundCallback(mRunnableReleaseHandler);

		createHandler();

		mHttpMediaHandler.post(new Runnable() {
			@Override
			public void run() {
				int session = 0;
				HttpGetSession mHttpGet;
				synchronized (HttpMediaClient.this) {
					if (mHttpGetSession != null) {
						LogUtil.logi("http request abort session： "
								+ mHttpGetSession.hashCode());
						mHttpGetSession.abort();
					}
					mHttpClient = new DefaultHttpClient();
					LogUtil.logd("HttpMediaClient::url::" + mUrl);
					mHttpGetSession = mHttpGet = new HttpGetSession(mUrl);
					session = mHttpGet.hashCode();
					LogUtil.logi("http create request session： " + session);
				}
				{
					LogUtil.logi("http begin session： " + session);
					mTotal = 0;
					InputStream input = null;
					try {
						HttpResponse res = mHttpClient.execute(mHttpGet);
						int resCode = res.getStatusLine().getStatusCode();
						if (resCode != 200) {
							LogUtil.loge("http response: " + resCode);
							if (mOnResponseListener != null) {
								mOnResponseListener.onError(new MediaError(
										MediaError.ERR_IO, "http response: "
												+ resCode, "错误响应" + resCode));
							}
							return;
						}
						HttpEntity entity = res.getEntity();
						long mNeed = mLength = entity.getContentLength();
						if (mOnResponseListener != null) {
							mOnResponseListener.onGetInfo();
						}
						input = entity.getContent();
						//mHttpMediaHandler.heartbeat();
						int read;
						while ((read = input.read(mBuffer)) >= 0) {
							//mHttpMediaHandler.heartbeat();
							if (mOnResponseListener != null) {
								byte[] data = new byte[read];
								mTotal += read;
								System.arraycopy(mBuffer, 0, data, 0, read);
								mOnResponseListener.onRecive(mTotal * 1.0F
										/ mLength, data);
								if (mTotal >= mNeed)
									break;
							}
						}
						input.close();
						if (mOnResponseListener != null) {
							mOnResponseListener.onEnd();
						}
					} catch (Exception e) {
						if (!mHttpGet.mCancel) {
							LogUtil.logi("http session excepiton： " + session
									+ ", exception=" + e.getMessage());
							if (mOnResponseListener != null) {
								// 打开导致：：SeeKTo播放不了音频。
								mOnResponseListener.onError(new MediaError(
										MediaError.ERR_DISCONNECT,
										"http connection closed", "连接断开"));
							}
						} else {
							LogUtil.logi("http session canceled： " + session);
						}
					} finally {
						try {
							if (input != null) {
								input.close();
							}
						} catch (IOException e) {
						}
					}
				}
			}
		});
	}

	@Override
	public synchronized void seek(final long offset) {
		AppLogic.removeBackGroundCallback(mRunnableReleaseHandler);

		createHandler();

		mHttpMediaHandler.post(new Runnable() {
			@Override
			public void run() {
				int session = 0;
				HttpGetSession mHttpGet;
				synchronized (HttpMediaClient.this) {
					if (mHttpGetSession != null) {
						LogUtil.logi("http abort session： "
								+ mHttpGetSession.hashCode());
						mHttpGetSession.abort();
					}
					mHttpClient = new DefaultHttpClient();
					mHttpGetSession = mHttpGet = new HttpGetSession(mUrl);
					session = mHttpGet.hashCode();
					LogUtil.logi("http create seekto session： " + session);
				}
				{
					LogUtil.logi("http begin session： " + session);

					InputStream input = null;

					try {
						mHttpGet.addHeader("Range", "bytes=" + offset + "-");
						mTotal = offset;
						int getCount = 0;
						OnResponseListener mSeekListener = mOnResponseListener;
						HttpResponse res = mHttpClient.execute(mHttpGet);
						int resCode = res.getStatusLine().getStatusCode();
						if (resCode != 206) {
							LogUtil.loge("http response: " + resCode);
							if (mOnResponseListener != null) {
								mOnResponseListener.onError(new MediaError(
										MediaError.ERR_IO, "http response: "
												+ resCode, "错误响应" + resCode));
							}
							return;
						}
						HttpEntity entity = res.getEntity();
						try {
							String rangeLength = res.getLastHeader(
									"Content-Range").getValue();
							mLength = Long.parseLong(rangeLength.split("/")[1]);
						} catch (Exception e) {
						}
						long mNeed = entity.getContentLength();
						if (mOnResponseListener != null) {
							mOnResponseListener.onGetInfo();
						}
						//mHttpMediaHandler.heartbeat();
						input = entity.getContent();
						int read;
						while ((read = input.read(mBuffer)) >= 0) {
							//mHttpMediaHandler.heartbeat();
							if (mOnResponseListener != null) {
								byte[] data = new byte[read];
								mTotal += read;
								getCount += read;
								System.arraycopy(mBuffer, 0, data, 0, read);
								if (mSeekListener != null) {
									mSeekListener.onSeek();
									mSeekListener = null;
								}
								mOnResponseListener.onRecive(mTotal * 1.0F
										/ mLength, data);
								if (getCount >= mNeed)
									break;
							}
						}
						input.close();
						if (mOnResponseListener != null) {
							mOnResponseListener.onEnd();
						}
					} catch (Exception e) {
						if (!mHttpGet.mCancel) {
							LogUtil.logi("http session excepiton： " + session
									+ ", exception=" + e.getMessage());
							if (mOnResponseListener != null) {
								mOnResponseListener.onError(new MediaError(
										MediaError.ERR_DISCONNECT,
										"http connection closed", "连接断开"));
							}
						} else {
							LogUtil.logi("http session canceled： " + session);
						}
					} finally {
						try {
							if (input != null) {
								input.close();
							}
						} catch (IOException e) {
						}
					}
				}
			}
		});
	}

	@Override
	public long getTotalSize() {
		return mLength;
	}

	@Override
	public long getDownloadSize() {
		return mTotal;
	}

	@Override
	public synchronized void cancel() {
		int session = 0;
		if (mHttpGetSession != null) {
			session = mHttpGetSession.hashCode();
			LogUtil.logi("http cancel session enter： " + session);
			mHttpGetSession.abort();
			mHttpGetSession = null;
		}

		{
			AppLogic.runOnBackGround(mRunnableReleaseHandler, 3000);
		}

		LogUtil.logi("http cancel session return： " + session);
	}

}
