package com.txznet.audio.server.response;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.player.MediaError;
import com.txznet.audio.player.SessionManager;
import com.txznet.audio.player.SessionManager.SessionInfo;
import com.txznet.audio.player.audio.NetAudio;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.music.Constant;

public class HttpMediaResponse extends MediaResponseBase {
	private final int DATA_PIECE_SIZE_MIN = 4096 * 8;
	protected List<String> mBackUrl = new ArrayList<String>();// 备用请求地址
	private int mCurPosition = -1;// 默认使用第一个地址

	protected URI mURI = null;
	protected String mUrl = null;

	protected long mCurPos = 0;

	CacheInfo mCacheInfo = null;

	protected HttpMediaResponse(Socket out, SessionInfo sess, long from, long to) {
		super(out, sess, from, to);

		NetAudio audio = (NetAudio) mSess.audio;
		mUrl = audio.getUrl();
		try {
			mURI = new URI(mUrl);
		} catch (URISyntaxException e) {
			HttpMediaResponse.this.onHttpMediaError(new MediaError(
					MediaError.ERR_URI, "wrong media url: " + mUrl, "播放地址错误"));
		}
	}

	public void onHttpMediaError(MediaError err) {
		if (null != SessionManager.getInstance().getSessionInfo(
				mSess.hashCode())) {
			LogUtil.loge("media session[" + mSess.getLogId() + "]media error: "
					+ err.getErrDesc());
			mSess.player.notifyError(err);
		}
	}

	int mRetryCount = 0;
	MediaHttpClient<HttpMediaResponse> mHttpClient;

	@Override
	public void cancel() {
		super.cancel();

		if (mHttpClient != null) {
			mHttpClient.cancel();
		}
	}

	// 等待
	protected void waitDownloadUrl(OutputStream out) throws IOException {
		while (TextUtils.isEmpty(mUrl) || mURI == null) {
			printEmptyData(out);
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
			printEmptyData(out);
		}
	}

	@Override
	protected long getLength(final OutputStream out) throws IOException {
		mCurPos = mFrom;
		if (mCurPos < 0) {
			mCurPos = 0;
		}
		LogUtil.logd(Constant.SPENDTIME + "get length cache ");
		mLen = getCacheLength(out);
		printEmptyData(out);
		if (mLen > 0) {
			return mLen;
		}
		LogUtil.logd(Constant.SPENDTIME + "get length with data ");
		mLen = getNetLengthWithData(out);
		if (mLen > 0) {
			return mLen;
		}
		LogUtil.logd(Constant.SPENDTIME + "get  length  net");
		return getNetLength(out);
	}

	protected long getCacheLength(final OutputStream out) throws IOException {
		mCacheInfo = CacheInfo.createCacheInfo(mSess, -1);
		if (mCacheInfo != null) {
			return mCacheInfo.getTotalSize();
		}
		return -1;
	}

	protected long getNetLength(final OutputStream out) throws IOException {
		mRetryCount = 0;

		mHttpClient = new MediaHttpClient<HttpMediaResponse>(
				HttpMediaResponse.this) {
			@Override
			public void onMediaError(MediaError err) throws IOException {
				HttpMediaResponse.this.onHttpMediaError(err);
			}

			@Override
			public void onResponse(int statusCode, String statusLine)
					throws IOException {
				if (statusCode / 100 != 2) {
					if (statusCode / 100 != 4) {// 4XX错误
						setSuggestRetryDelay(mSuggestRetryDelayDefault);
					}
					MonitorUtil.monitorCumulant(Constant.M_URL_PLAY_ERROR);
					LogUtil.logw("media session[" + mSess.getLogId()
							+ "]get data url[" + statusCode + "]: "
							+ mHttpClient.mUri.toString() + "");
				}
				if (statusCode == 400) {
					HttpMediaResponse.this.onHttpMediaError(new MediaError(
							MediaError.ERR_BAD_REQUEST,
							"uri media bad request: " + mUrl, "音频已下架"));
					cancel();
					return;
				}
				if (statusCode == 403) {
					HttpMediaResponse.this.onHttpMediaError(new MediaError(
							MediaError.ERR_FILE_FOBIDDEN,
							"uri media forbidden: " + mUrl, "音频已下架"));
					cancel();
					return;
				}
				if (statusCode == 404) {
					HttpMediaResponse.this.onHttpMediaError(new MediaError(
							MediaError.ERR_FILE_NOT_EXIST,
							"uri media not found: " + mUrl, "音频不存在"));
					cancel();
					return;
				}
				if (statusCode != 200) {
					HttpMediaResponse.this
							.onHttpMediaError(new MediaError(
									MediaError.ERR_GATE_WAY,
									"get length response code: " + statusCode,
									"音频访问失败"));
					cancel();
					return;
				}
			}

			@Override
			public void onGetInfo(Map<String, String> headers, String mimeType,
					long contentLength) throws IOException {
				LogUtil.logw("media session[" + mSess.getLogId()
						+ "]get media length: " + contentLength);
				mLen = contentLength;
			}

			@Override
			public void onReadData(byte[] data, int offset, int len)
					throws IOException {
			}

			@Override
			public void onIdle() throws IOException {
				HttpMediaResponse.super.printEmptyData(out);
			}

			@Override
			public void onConnectTimout() throws IOException {
				LogUtil.logw("media session[" + mSess.getLogId()
						+ "]get length onConnectTimout");
			}

			@Override
			public void onReadTimout() throws IOException {
				LogUtil.logw("media session[" + mSess.getLogId()
						+ "]get length onReadTimout");
			}
		};

		do {
			waitDownloadUrl(out);
			try {
				if (!mBackUrl.isEmpty()) {
					LogUtil.logd("mBackUrl =" + mBackUrl.size());
					mCurPosition = ++mCurPosition % mBackUrl.size();
					mURI = new URI(mBackUrl.get(mCurPosition));
				}
				LogUtil.logd("media session[" + mSess.getLogId()
						+ "] request url=" + mURI.toString());
			} catch (Exception e) {
				LogUtil.loge("media session[" + mSess.getLogId()
						+ "]get url error,url is " + mBackUrl.get(mCurPosition));
				mURI = null;
				continue;
			}

			mHttpClient.headMedia(mURI);

			printEmptyData(out);
			int delay = mHttpClient.getSuggestRetryDelay();
			retry(delay, out);
		} while (mHttpClient.getSuggestRetryDelay() > 0);
		MonitorUtil.monitorCumulant(Constant.M_URL_PLAY_SUCCESS);
		mHttpClient.cancel();

		mCacheInfo = CacheInfo.createCacheInfo(mSess, mLen);

		return mLen;
	}

	protected long getNetLengthWithData(final OutputStream out)
			throws IOException {
		mRetryCount = 0;

		mHttpClient = new MediaHttpClient<HttpMediaResponse>(
				HttpMediaResponse.this) {
			@Override
			public void onMediaError(MediaError err) throws IOException {
				HttpMediaResponse.this.onHttpMediaError(err);
			}

			@Override
			public void onResponse(int statusCode, String statusLine)
					throws IOException {
				LogUtil.logd(Constant.SPENDTIME + "conn response "+statusCode+"/"+statusLine);
				if (statusCode / 100 != 2) {
					MonitorUtil.monitorCumulant(Constant.M_URL_PLAY_ERROR);
					if (statusCode / 100 != 4) {// 4XX错误
						setSuggestRetryDelay(mSuggestRetryDelayDefault);
					}
					LogUtil.logw("media session[" + mSess.getLogId()
							+ "]get data url[" + statusCode + "]: "
							+ mHttpClient.mUri.toString() + "");
				}
				if (statusCode == 400) {
					HttpMediaResponse.this.onHttpMediaError(new MediaError(
							MediaError.ERR_BAD_REQUEST,
							"uri media bad request: " + mUrl, "音频已下架"));
					cancel();
					return;
				}
				if (statusCode == 403) {
					HttpMediaResponse.this.onHttpMediaError(new MediaError(
							MediaError.ERR_FILE_FOBIDDEN,
							"uri media forbidden: " + mUrl, "音频已下架"));
					cancel();
					return;
				}
				if (statusCode == 404) {
					HttpMediaResponse.this.onHttpMediaError(new MediaError(
							MediaError.ERR_FILE_NOT_EXIST,
							"uri media not found: " + mUrl, "音频不存在"));
					cancel();
					return;
				}
				if (statusCode == 302) {
					return;
				}
				if (statusCode != 206) {
					cancel();
					return;
				}
			}

			@Override
			public void onGetInfo(Map<String, String> headers, String mimeType,
					long contentLength) throws IOException {
				String contentRange = headers.get("Content-Range");
				try {
					mLen = Long.parseLong(contentRange.split("/")[1]);
					LogUtil.logd(Constant.SPENDTIME + "media session["
							+ mSess.getLogId()
							+ "]get media length with data: " + mLen);
				} catch (Exception e) {
					mLen = -1;
					LogUtil.logw("media session[" + mSess.getLogId()
							+ "]get media length with data error: "
							+ e.getMessage());
					cancel();
				}
				printResponseHeader(out);
			}

			@Override
			public void onReadData(byte[] data, int offset, int len)
					throws IOException {
				LogUtil.logd(Constant.SPENDTIME + "conn ReadData begin length : "+len);
				LogUtil.logd(Constant.SPENDTIME + "conn save cache begin ");
				
				if (mCacheInfo == null) {
					mCacheInfo = CacheInfo.createCacheInfo(mSess, mLen);
				}
				LogUtil.logd(Constant.SPENDTIME + "conn save cache end");
				IOException exp = null;
				try {
					printData(out, data, offset, len);
				} catch (IOException e) {
					exp = e;
				}
				LogUtil.logd(Constant.SPENDTIME + "conn data print end");
				mCacheInfo.addCacheBlock(HttpMediaResponse.this.mCurPos, data,
						offset, len);
				HttpMediaResponse.this.mCurPos += len;
				if (exp != null) {
					throw exp;
				}
				mSess.player.notifyDownloading(mCacheInfo.getCacheBlocks());
				LogUtil.logd(Constant.SPENDTIME + "conn ReadData length end");
			}

			@Override
			public void onIdle() throws IOException {
				HttpMediaResponse.super.printEmptyData(out);
			}

			@Override
			public void onConnectTimout() throws IOException {
				LogUtil.logw("media session[" + mSess.getLogId()
						+ "]get length onConnectTimout");
			}

			@Override
			public void onReadTimout() throws IOException {
				LogUtil.logw("media session[" + mSess.getLogId()
						+ "]get length onReadTimout");
			}
		};

		long need = mSess.player.getDataPieceSize();

		LogUtil.logd("media session[" + mSess.getLogId() + "]need data size="
				+ need + ", now=" + mCurPos + ", to=" + mTo + ", min="
				+ DATA_PIECE_SIZE_MIN);

		if (need < DATA_PIECE_SIZE_MIN) {
			need = DATA_PIECE_SIZE_MIN;
		}

		long endPos = mCurPos + need - 1;

		do {
			waitDownloadUrl(out);
			LogUtil.logd(Constant.SPENDTIME + "media session["
					+ mSess.getLogId() + "]get length with data[" + mCurPos
					+ "~" + endPos + "]|[" + mFrom + "~" + mTo + "]");

			try {
				if (!mBackUrl.isEmpty()) {
					LogUtil.logd("mBackUrl is not null,size " + mBackUrl.size()
							+ ",curPosition=" + mCurPosition);
					mCurPosition = ++mCurPosition % mBackUrl.size();
					mURI = new URI(mBackUrl.get(mCurPosition));
					// mURI = new
					// URI("http://cc.stream.qqmusic.qq.com/C200004SaUSe4V6QSV.m4a?vkey=663F84E23D7C5748A90BED497FF866EC478F5718FF8C01D7D23223DDE06A23F919FA0300F0EAD688005F5383B741BD8EB0F7EC4A1F1FD2A7&guid=a48ea10aab3ab7bd09c76c9461b01223&fromtag=0");
				}
				if (Constant.ISTESTDATA) {
					LogUtil.logd("media session[" + mSess.getLogId()
							+ "] request backurl " + mCurPosition + " url="
							+ mURI.toString());
				}
			} catch (Exception e) {
				LogUtil.loge("media session[" + mSess.getLogId()
						+ "]get url error,url is "
						+ mBackUrl.get(mCurPosition < 0 ? 0 : mCurPosition));
				mURI = null;
				continue;
			}
			mHttpClient.getMedia(mURI, mCurPos, endPos);

			printEmptyData(out);

			int delay = mHttpClient.getSuggestRetryDelay();
			retry(delay, out);
		} while (mHttpClient.getSuggestRetryDelay() > 0);

		MonitorUtil.monitorCumulant(Constant.M_URL_PLAY_SUCCESS);

		mHttpClient.cancel();

		return mLen;
	}

	protected void createGetDataClient(final OutputStream out) {
		mHttpClient = new MediaHttpClient<HttpMediaResponse>(
				HttpMediaResponse.this) {
			@Override
			public void onMediaError(MediaError err) throws IOException {
				HttpMediaResponse.this.onHttpMediaError(err);
			}

			@Override
			public void onResponse(int statusCode, String statusLine)
					throws IOException {
				if (statusCode / 100 != 2) {
					MonitorUtil.monitorCumulant(Constant.M_URL_PLAY_ERROR);
					LogUtil.logw("media session[" + mSess.getLogId()
							+ "]get data url[" + statusCode + "]: "
							+ mHttpClient.mUri.toString() + "");
				}
				if (statusCode == 400) {
					HttpMediaResponse.this.onHttpMediaError(new MediaError(
							MediaError.ERR_BAD_REQUEST,
							"uri media bad request: " + mUrl, "音频已下架"));
					cancel();
					return;
				}
				if (statusCode == 403) {
					HttpMediaResponse.this.onHttpMediaError(new MediaError(
							MediaError.ERR_FILE_FOBIDDEN,
							"uri media forbidden: " + mUrl, "音频已下架"));
					cancel();
					return;
				}
				if (statusCode == 404) {
					HttpMediaResponse.this.onHttpMediaError(new MediaError(
							MediaError.ERR_FILE_NOT_EXIST,
							"uri media not found: " + mUrl, "音频不存在"));
					cancel();
					return;
				}
				if (statusCode == 302) {
					// 如果是302则不处理
					return;
				}
				if (statusCode != 206) {
					HttpMediaResponse.this.onHttpMediaError(new MediaError(
							MediaError.ERR_GATE_WAY, "get data response code: "
									+ statusCode, "音频访问异常"));
					cancel();
					return;
				}
			}

			@Override
			public void onGetInfo(Map<String, String> headers, String mimeType,
					long contentLength) throws IOException {
				String contentRange = headers.get("Content-Range");
				LogUtil.logd("media session[" + mSess.getLogId()
						+ "]get data range response: " + contentRange + "|"
						+ contentLength);
				setSuggestRetryDelay(1000);
			}

			@Override
			public void onReadData(byte[] data, int offset, int len)
					throws IOException {
				setSuggestRetryDelay(0);

				mSess.player.notifyDownloading(mCacheInfo.getCacheBlocks());

				IOException exp = null;
				try {
					printData(out, data, offset, len);
				} catch (IOException e) {
					exp = e;
				}
				mCacheInfo.addCacheBlock(HttpMediaResponse.this.mCurPos, data,
						offset, len);
				HttpMediaResponse.this.mCurPos += len;
				if (exp != null) {
					throw exp;
				}
				mSess.player.notifyDownloading(mCacheInfo.getCacheBlocks());
			}

			@Override
			public void onIdle() throws IOException {
				HttpMediaResponse.super.printEmptyData(out);
			}

			@Override
			public void onConnectTimout() throws IOException {
				LogUtil.logw("media session[" + mSess.getLogId()
						+ "]get data onConnectTimout");
			}
		};
	}

	@Override
	protected void getData(final OutputStream out) throws IOException {
		LogUtil.logd(TAG + " getData ");
		if (mCacheInfo == null) {
			LogUtil.loge("mCacheInfo is null on getData ,have error");
			cancel();
		}
		mHttpClient = null;

		printEmptyData(out);

		mRetryCount = 0;

		List<LocalBuffer> buffers = mCacheInfo.getCacheBlocks();
		mSess.player.notifyDownloading(buffers);
		CacheInfo.CacheData cacheData = new CacheInfo.CacheData();

		while (true) {
			// 等待需要更多的数据
			if (mCurPos != 0 && mCurPos != mFrom) {
				LogUtil.logd(TAG + " waitNeedData");
				if (!waitNeedData(out)) {
					LogUtil.logw(TAG + " can't exit");
					return;
				}
			}

			// 计算需要的数据量
			long need = mSess.player.getDataPieceSize();

			LogUtil.logd("media session[" + mSess.getLogId()
					+ "]need data size=" + need + ", now=" + mCurPos + ", to="
					+ mTo + ", min=" + DATA_PIECE_SIZE_MIN);

			if (need < DATA_PIECE_SIZE_MIN) {
				need = DATA_PIECE_SIZE_MIN;
			}
			long endPos = mCurPos + need - 1;
			if (endPos > mTo) {
				endPos = mTo;
			}

			if (endPos < mCurPos) {
				LogUtil.logi("media session[" + mSess.getLogId()
						+ "]all data response complete: [" + mCurPos + "~"
						+ endPos + "]|[" + mFrom + "~" + mTo + "]");
				printEmptyData(out);
				return;
			}

			// 尝试从缓存读取数据
			mCacheInfo.getCacheData(cacheData, mCurPos, endPos, true);
			if (cacheData.data != null && cacheData.len > 0) {
				LogUtil.logd("media session[" + mSess.getLogId()
						+ "]write cache data size=" + cacheData.len);
				printData(out, cacheData.data, 0, cacheData.len);
				mCurPos += cacheData.len;
			}
			if (endPos > mCurPos) {
				if (mHttpClient == null) {
					createGetDataClient(out);
				}

				do {
					waitDownloadUrl(out);

					LogUtil.logd("media session[" + mSess.getLogId()
							+ "]get data[" + mCurPos + "~" + endPos + "]|["
							+ mFrom + "~" + mTo + "]");

					mHttpClient.getMedia(mURI, mCurPos, endPos);
					mSess.player.notifyDownloading(mCacheInfo.getCacheBlocks());

					LogUtil.logd("media session[" + mSess.getLogId()
							+ "]write net data size="
							+ mHttpClient.getReadCount());

					printEmptyData(out);
					int delay = mHttpClient.getSuggestRetryDelay();
					retry(delay, out);
				} while (mHttpClient.getSuggestRetryDelay() > 0);

				mHttpClient.cancel();
			}
		}
	}

	private void retry(int delay, OutputStream out) throws IOException {
		try {
			if (delay > 0) {
				++mRetryCount;
				LogUtil.logw("media session[" + mSess.getLogId()
						+ "]get data need retry: " + mRetryCount);
				if (mRetryCount == 3 | mRetryCount == 7 | mRetryCount == 15) {
					// TtsUtil.speakText(Constant.SPEAK_NET_POOR);
				}
			}

			while (delay > 0) {
				Thread.sleep(delay >= WAIT_DATA_TIME ? WAIT_DATA_TIME : delay);
				printEmptyData(out);
				delay -= WAIT_DATA_TIME;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
