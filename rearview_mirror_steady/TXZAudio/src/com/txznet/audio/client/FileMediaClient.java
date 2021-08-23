package com.txznet.audio.client;

import java.io.File;
import java.io.FileInputStream;

import android.os.HandlerThread;

import com.txznet.audio.player.MediaError;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.TXZHandler;

public class FileMediaClient extends TXZMediaClient {
	public static final int READ_BUFFER_SIZE = 10 * 1024;
	private HandlerThread mFileMediaThread = null;
	private TXZHandler mFileMediaHandler = null;

	private File mFile;
	private long mLength;
	private long mTotal = 0;
	private FileInputStream mFileInputStream;
	private byte[] mBuffer = new byte[READ_BUFFER_SIZE];

	private synchronized void createHandler() {
		if (mFileMediaThread == null) {
			mFileMediaThread = new HandlerThread("FileMediaReadThread#"
					+ mFile.getPath());
			mFileMediaThread.start();
			mFileMediaHandler = new TXZHandler(mFileMediaThread.getLooper());
		}
	}

	private synchronized void releaseHandler() {
		if (mFileMediaThread != null) {
			mFileMediaThread.quit();
			mFileMediaThread = null;
			mFileMediaHandler = null;
		}
	}

	public FileMediaClient(String url) {
		super(url);

		mFile = new File(url);
		mLength = mFile.length();
	}

	private void closeStream() {
		if (mFileInputStream != null) {
			try {
				mFileInputStream.close();
			} catch (Exception e) {
			}
		}
	}

	private void openInputStream(long skip) {
		synchronized (FileMediaClient.this) {
			try {
				closeStream();
				mFileInputStream = new FileInputStream(mFile);
				if (mOnResponseListener != null) {
					mOnResponseListener.onGetInfo();
				}
				mFileInputStream.skip(skip);
			} catch (Exception e) {
				if (mOnResponseListener != null) {
					LogUtil.logi("FileMediaClient::openInputStream::");
					mOnResponseListener.onError(new MediaError(
							MediaError.ERR_IO, "open file failed: "
									+ mFile.getPath(), "读取音频文件发生错误"));
				}
			}
		}
	}

	@Override
	public void request() {
		createHandler();
		mFileMediaHandler.post(new Runnable() {
			@Override
			public void run() {
				openInputStream(0);
				int read;
				mTotal = 0;
				try {
					while ((read = mFileInputStream.read(mBuffer)) >= 0) {
						if (mOnResponseListener != null) {
							byte[] data = new byte[read];
							mTotal += read;
							System.arraycopy(mBuffer, 0, data, 0, read);
							mOnResponseListener.onRecive(mTotal * 1.0F
									/ mLength, data);
						}
					}
					mFileInputStream.close();
					if (mOnResponseListener != null) {
						mOnResponseListener.onEnd();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void seek(final long offset) {
		createHandler();
		mFileMediaHandler.post(new Runnable() {
			@Override
			public void run() {
				LogUtil.logd("seek to: " + offset);
				openInputStream(offset);
				int read;
				mTotal = 0;
				OnResponseListener mSeekListener = mOnResponseListener;
				try {
					while ((read = mFileInputStream.read(mBuffer)) >= 0) {
						if (mOnResponseListener != null) {
							byte[] data = new byte[read];
							if (mTotal == 0) {
								mTotal = offset;
							}
							mTotal += read;
							System.arraycopy(mBuffer, 0, data, 0, read);
							if (mSeekListener != null) {
								mSeekListener.onSeek();
								mSeekListener = null;
							}
							mOnResponseListener.onRecive(mTotal * 1.0F
									/ mLength, data);
						}
					}
					mFileInputStream.close();
					if (mOnResponseListener != null) {
						mOnResponseListener.onEnd();
					}
				} catch (Exception e) {
					e.printStackTrace();
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
	public void cancel() {
		mFileMediaHandler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (FileMediaClient.this) {
					closeStream();
				}
				releaseHandler();
			}
		});
	}

}
