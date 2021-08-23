package com.txznet.audio.server.response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;

import android.os.Environment;

import com.txznet.audio.player.SessionManager.SessionInfo;
import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.audio.server.NanoHTTPD;
import com.txznet.audio.server.NanoHTTPD.Response;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;

public abstract class MediaResponseBase extends Response {
	protected static final String MIME_AUDIO = "audio/mpeg";
	protected static final String TAG = "[MUSIC][Server][Response]";

	protected final static byte[] EMPTY_DATA = new byte[0];
	protected final static int WAIT_DATA_TIME = 1000;
	protected final static int WAIT_URL_TIME = 200;
	protected final static int NET_RETRY_WAIT_COUNT = 3; // 重试等待时间为WAIT_DATA_TIME*NET_RETRY_WAIT_COUNT;

	protected final byte[] READ_BUFFER = new byte[512 * 1024];

	FileOutputStream fosFileOutputStream = null;

	private int mPrintTotal = 0;

	protected void printData(OutputStream out, final byte[] data,
			final int offset, final int len) throws IOException {
		try {
			mPrintTotal += len;

			if (Constant.ISTESTDATA) {
				if (len > 0) {
					LogUtil.logd("media session[" + mSess.getLogId()
							+ "] write data length: " + len + "/" + mPrintTotal);
				}
			}
			if (mSocketOut == null) {
				throw new IOException("reponse canceled");
			}
			if (data != null && data.length > 0 && len > 0) {
				// 写该块数据到文件
				if (Constant.ISTESTDATA) {
					if (fosFileOutputStream == null) {
						File file = new File(Environment
								.getExternalStorageDirectory().getPath()
								+ "/txz/audio/"
								+ mSess.audio.hashCode()
								+ "_"
								+ this.hashCode()
								+ "_"
								+ (mFrom + mPrintTotal)
								+ "_"
								+ (mFrom + mPrintTotal + len - 1)
								+ "_"
								+ mTo);
						file.getParentFile().mkdirs();
						try {
							fosFileOutputStream = new FileOutputStream(file);
						} catch (IOException e) {
							LogUtil.loge(
									"(Audio)(MediaResponse)FileOutputStream:" + e.toString() + " cause:" + e.getCause());
							e.printStackTrace();
						}
					}

					try {
						fosFileOutputStream.write(data, offset, len);
						fosFileOutputStream.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			try{
				out.write(EMPTY_DATA);
				out.write(data, offset, len);
				mWrite += len;
				if (mSocketOut == null) {
					throw new IOException("reponse canceled");
				}
				out.write(EMPTY_DATA);
			}catch(Exception e){
				if (mSocketOut == null) {
					throw new IOException("reponse canceled");
				}
				cancel();
				LogUtil.loge("[MUSIC][EXCEPTION]close socket",e);
			}

			if (mSocketOut == null) {
				throw new IOException("reponse canceled");
			}
		} catch (IOException e) {
			LogUtil.logw("media session[" + mSess.getLogId()
					+ "] write data to release player: "
					+ mSess.player.toString());
			throw e;
		}
	}

	protected void printData(OutputStream out, byte[] data) throws IOException {
		printData(out, data, 0, data.length);
	}

	protected void printEmptyData(OutputStream out) throws IOException {
		printData(out, EMPTY_DATA);
	}

	boolean mLastNeedData = false;

	protected boolean waitNeedData(OutputStream out) throws IOException {
		while (true) {
			try {
				Thread.sleep(WAIT_DATA_TIME);
			} catch (InterruptedException e) {
			}

			try {
				boolean ret = mSess.player.needMoreData();
				LogUtil.logd(Constant.SPENDTIME + "wait data begin" +ret);
				if (ret == false) {
					if (Constant.ISNEED) {
						LogUtil.logd(TAG
								+ " waitNeedData :write="
								+ mWrite
								+ ",mFrom="
								+ mFrom
								+ ",size="
								+ (mSess.player.getDataPieceSize()
										* TXZAudioPlayer.NEED_BUFFER_DATA_TIME / TXZAudioPlayer.PREPARE_BUFFER_DATA_TIME)
										+ ",len=" + mLen + ",playPercent="
										+ mSess.player.getPlayPercent());
					}
					// 增加通过码率计算保护，防止出现播放器计算进度卡住播放
					if ( 
							/*播放器整体的数据量*/
							(mWrite + mFrom) 
							-
							/*预缓冲至少要保留的数据量*/
							(mSess.player.getDataPieceSize()* TXZAudioPlayer.NEED_BUFFER_DATA_TIME / TXZAudioPlayer.PREPARE_BUFFER_DATA_TIME)
							< 
							/*当前播放过得数据量*/
							mLen* mSess.player.getPlayPercent()) {
						
						LogUtil.logw("media session[" + mSess.getLogId()
								+ "] need more data warning");
						ret = true;
					}
				}
				if (mLastNeedData != ret) {
					LogUtil.logd("media session[" + mSess.getLogId()
							+ "] need more data: " + ret);
					mLastNeedData = ret;
				}
				printEmptyData(out);
				if (ret) {
					break;
				}
			} catch (Exception e) {
				LogUtil.loge("media session[" + mSess.getLogId()
						+ "] already release player: "
						,e);
				return false;
			}

		}
		return true;
	}

	protected SessionInfo mSess;
	protected long mFrom;
	protected long mTo;
	protected long mLen;
	protected long mWrite;
	protected Socket mSocketOut = null;

	protected MediaResponseBase(Socket out, SessionInfo sess, long from, long to) {
		mSess = sess;
		mFrom = from;
		mTo = to;
		mSocketOut = out;
		mWrite = 0;

		mSess.addResponse(this);

		LogUtil.logi("media session[" + mSess.getLogId()
				+ "] create response: Range=" + mFrom + "~" + mTo);
	}

	public void cancel() {
		LogUtil.logw("media session[" + mSess.getLogId()
				+ "] cancel response: Range=" + mFrom + "~" + mTo);

		if (mSocketOut != null) {
			try {
				mSocketOut.close();
			} catch (IOException e) {
			}
			mSocketOut = null;
		}
	}

	protected void prepare(OutputStream out) {

	}

	boolean mGenHeader = false;

	public void printResponseHeader(OutputStream out) throws IOException {
		if (mGenHeader == false) {
			mGenHeader = true;
			genHeaders();
			printEmptyData(out);
			super.printHeader(out);
		}
	};

	@Override
	public void print(OutputStream out) throws IOException {
		printEmptyData(out);
		prepare(out);
		printEmptyData(out);
		LogUtil.logd(Constant.SPENDTIME+"get length begin ");
		mLen = getLength(out);
		LogUtil.logd(Constant.SPENDTIME+"get length end ");
		printEmptyData(out);
		LogUtil.logd(Constant.SPENDTIME+"print header begin");
		printResponseHeader(out);
		LogUtil.logd(Constant.SPENDTIME+"print header end ");
		printEmptyData(out);
		LogUtil.logd(Constant.SPENDTIME+"get data begin ");
		getData(out);
		LogUtil.logd(Constant.SPENDTIME+"get data end ");
	}

	protected abstract long getLength(OutputStream out) throws IOException;

	protected abstract void getData(OutputStream out) throws IOException;

	protected void genHeaders() {
		LogUtil.logd("media session[" + mSess.getLogId()
				+ "] genHeaders: Range=" + mFrom + "~" + mTo + ", size=" + mLen
				+ ", player=" + mSess.player.toString() + ", audio="
				+ mSess.audio.toString());

		boolean customHeaders = false;

		if (customHeaders) {
			// this.responseHeadString = "Accept-Ranges: bytes\r\n"
			// + "Age: 56410\r\n" + //
			// "Cache-Control: max-age=315360000\r\n" + //
			// "Connection: keep-alive\r\n" + //
			// "Content-Type: audio/x-m4a\r\n" + //
			// // "Expires: Sat, 09 May 2026 12:49:33 GMT\r\n" + //
			// "Last-Modified: Fri, 18 Dec 2015 13:06:54 GMT\r\n" + //
			// "Server: nginx\r\n" + //
			// "";
			this.responseHeadString =
			// "Expires: Mon, 11 May 2026 08:34:14 GMT\r\n" +
			// "Date: Fri, 13 May 2016 08:34:14 GMT\r\n" +
			// "Server: nginx\r\n" +
			"Content-Type: audio/x-m4a\r\n" +
			// "Last-Modified: Wed, 10 Jun 2015 04:47:06 GMT\r\n" +
					"Accept-Ranges: bytes\r\n" +
					// "Cache-Control: max-age=315360000\r\n" +
					// "Content-Length: 5691534\r\n" +
					// "Age: 277\r\n" +
					"Connection: keep-alive\r\n";
			// this.addHeader("Accept-Ranges", "bytes");
			// this.addHeader( "Age","56410");
			// this.addHeader( "Cache-Control","max-age=315360000");
			// this.addHeader("Connection", "close");
			// this.addHeader( "Content-Length","2091397");
			// this.addHeader("Content-Type", "audio/x-m4a");
			// this.addHeader( "Date","Wed, 11 May 2016 12:49:33 GMT");
			// this.addHeader( "Last-Modified","Fri, 18 Dec 2015 13:06:54 GMT");
			// this.addHeader( "Server","nginx");

		} else {
			this.addHeader("Connection", "close");

			this.mimeType = MIME_AUDIO;

			this.addHeader("Accept-Ranges", "bytes");
		}
		// 重新定义碎片范围
		if (mTo == -1) {
			mTo = mLen - 1;
		}

		this.status = (mFrom == -1 ? NanoHTTPD.HTTP_OK
				: NanoHTTPD.HTTP_PARTIALCONTENT);

		// 416 HTTP_RANGE_NOT_SATISFIABLE
		if (mFrom > mTo || mTo > mLen - 1 || mFrom > mLen - 1) {
			this.status = NanoHTTPD.HTTP_RANGE_NOT_SATISFIABLE;
			if (customHeaders) {
				this.responseHeadString += "Content-Range: bytes */" + mLen
						+ "\r\n";
			}
			this.addHeader("Content-Range", "bytes */" + mLen);
			this.printer = null;
			return;
		}

		// 计算Content-Length长度
		long ContentLength = mLen;
		if (mFrom >= 0) {
			// Content-Range: bytes 0-12184829/12184830
			ContentLength = mTo - mFrom + 1;
			if (customHeaders) {
				this.responseHeadString += "Content-Range: bytes " + mFrom
						+ "-" + mTo + "/" + mLen + "\r\n";
			}
			this.addHeader("Content-Range", "bytes " + mFrom + "-" + mTo + "/"
					+ mLen);
		}
		if (customHeaders) {
			this.responseHeadString += "Content-Length: "
					+ (ContentLength <= 0 ? 0 : ContentLength) + "\r\n";
		}
		this.addHeader("Content-Length", ""
				+ (ContentLength <= 0 ? 0 : ContentLength));
	}
}
