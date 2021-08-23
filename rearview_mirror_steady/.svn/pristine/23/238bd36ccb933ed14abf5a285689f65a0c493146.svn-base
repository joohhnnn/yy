package com.txznet.audio.server.response;

import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import android.os.SystemClock;

import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.txznet.audio.player.MediaError;
import com.txznet.audio.player.SessionManager;
import com.txznet.audio.player.SessionManager.SessionInfo;
import com.txznet.audio.player.audio.QQMusicAudio;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.bean.req.ReqProcessing;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.bean.response.ResponseURL;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.DataInterfaceBroadcastHelper.RemoteNetListener;
import com.txznet.music.utils.JsonHelper;

public class QQMusicMediaResponse extends HttpMediaResponse {
	protected final static int GET_URL_RETRY_TIME = 1000;
	protected final static int TICKET_EXPIRED_TIME = 2 * 60 * 60 * 1000; // QQ音乐的ticket有效期
	private Audio mAudio = null;
	private QQMusicAudio mMusicAudio = null;
	private static String mQQMusicTicketData = null;
	private static long mQQMusicTicketExpiredTime = 0;
	int mGetUrlRetryCount = 0;
	int mBadRequestRetryCount = 0;
	int timeOutRetry = 0;// 超过10次就停止
	private final static int DEF_TICKET_TIME_OUT = 3000;
	private int mForbiddenRetry = 0;

	@Override
	public void onHttpMediaError(MediaError err) {
		if (null == SessionManager.getInstance().getSessionInfo(
				mSess.hashCode())) {
			// 不处理改错误，修改点：因为这里可能因为上一个Mediaplayer的请求并错误回调，导致异常响应。
			// 修改原则：Mediaplayer与ErrorListener 相关联。
			return;
		}
		// 400时1s后重试
		if (err.getErrCode() == MediaError.ERR_BAD_REQUEST) {
			++mBadRequestRetryCount;

			LogUtil.logw("media session[" + mSess.getLogId()
					+ "]bad request access retry[" + mBadRequestRetryCount
					+ "]: " + mUrl);

			if (mBadRequestRetryCount > 3) {
				mBadRequestRetryCount = 0;

				mQQMusicTicketData = null;

				mUrl = null;
				mURI = null;

				refreshDownloadData();

				mHttpClient.setSuggestRetryDelay(WAIT_URL_TIME);
			} else {
				mHttpClient.setSuggestRetryDelay(GET_URL_RETRY_TIME);
			}

			return;
		}
		// 出现403时重新换取下载地址
		if (err.getErrCode() == MediaError.ERR_FILE_FOBIDDEN) {
			LogUtil.logw("media session[" + mSess.getLogId()
					+ "]forbidden access: " + mUrl);

			mQQMusicTicketData = null;

			mUrl = null;
			mURI = null;
			if(mForbiddenRetry++ < 3){
				refreshDownloadData();
				mHttpClient.setSuggestRetryDelay(WAIT_URL_TIME);
				return;
			}
		}
		if (err.getErrCode() == MediaError.ERR_REQ_TIMEOUT) {// 超时
			LogUtil.logw("timeout:");
		}
		super.onHttpMediaError(err);
	}

	protected QQMusicMediaResponse(Socket out, SessionInfo sess, long from,
			long to) {
		super(out, sess, from, to);
		mMusicAudio = (QQMusicAudio) mSess.audio;
		mAudio = mMusicAudio.getAudio();
		mURI = null;
		mForbiddenRetry = 0;
		AppLogic.removeBackGroundCallback(timeOutRunnable);
		mQQMusicTicketData = null;
		mUrl = mMusicAudio.getFinalUrl();
		try {
			mURI = new URI(mUrl);
		} catch (Exception e) {
			mUrl = null;
		}
		if (StringUtils.isEmpty(mUrl) || mURI == null) {
			refreshDownloadData();
		}
	}

	StringRequest mPreprocessRequest;

	@Override
	public void cancel() {
		synchronized (QQMusicMediaResponse.this) {
			if (mPreprocessRequest != null) {
				mPreprocessRequest.cancel();
				mPreprocessRequest = null;
			}
		}

		mMusicAudio = null;

		super.cancel();
	}

	/**
	 * 获取QQMusic的ticket
	 */
	public void refreshDownloadData() {
		LogUtil.logd("PlaySpendTime:"+"get download url start ");
		if (mQQMusicTicketData != null
				&& SystemClock.elapsedRealtime() < mQQMusicTicketExpiredTime) {
			LogUtil.logd("get download url cache ");
			QQMusicMediaResponse.this.refreshDownloadUrl();
			return;
		}

		synchronized (QQMusicMediaResponse.this) {
			mPreprocessRequest = new StringRequest(
					mAudio.getStrProcessingUrl(), new Listener<String>() {
						@Override
						public void onResponse(String data) {
							LogUtil.logd("get download url end ");
							mQQMusicTicketData = data;
							mQQMusicTicketExpiredTime = SystemClock
									.elapsedRealtime() + TICKET_EXPIRED_TIME;
								
							refreshDownloadUrl();
						}
					}, new ErrorListener() {
						@Override
						public void onErrorResponse(VolleyError err) {
							LogUtil.logd("get download url error ");
							if (err instanceof TimeoutError
									|| err instanceof NetworkError
									|| err instanceof NoConnectionError) {
								// 可重试错误
								AppLogic.runOnBackGround(new Runnable() {
									@Override
									public void run() {
										QQMusicMediaResponse.this
												.refreshDownloadData();
									}
								}, GET_URL_RETRY_TIME);
								return;
							}
							QQMusicMediaResponse.this
									.onHttpMediaError(new MediaError(
											MediaError.ERR_IO,
											"preprocess error: "
													+ err.getMessage(),
											"访问音频发生异常"));
						}
					});
			AppLogic.getServerQueue().add(mPreprocessRequest);
		}
	}

	public void refreshDownloadUrl() {
		if (mQQMusicTicketData == null
				|| SystemClock.elapsedRealtime() >= mQQMusicTicketExpiredTime) {
			LogUtil.logd(TAG+"refreshDownloadData  ticketData="
					+ mQQMusicTicketData);
			QQMusicMediaResponse.this.refreshDownloadData();
			return;
		}
		ReqProcessing reqData = new ReqProcessing();
		reqData.setStrDownloadUrl(mAudio.getStrDownloadUrl());
		reqData.setProcessingContent(mQQMusicTicketData);
		reqData.setStrProcessingUrl(mAudio.getStrProcessingUrl());
		reqData.setSid(mAudio.getSid());
		reqData.setAudioId(mAudio.getId());
		LogUtil.logd(Constant.SPENDTIME+"send to other process by broadcase now");
		DataInterfaceBroadcastHelper.sendDataInterfaceReq(
				Constant.GET_PROCESSING, JsonHelper.toJson(reqData).getBytes(),
				new RemoteNetListener() {
					@Override
					public void response(int code, byte[] data) {
						LogUtil.logd(Constant.SPENDTIME+"gain other process data code ="+code);
						AppLogic.removeBackGroundCallback(timeOutRunnable);
						if (Constant.ISTESTDATA) {
							LogUtil.logd("request refresh download ="
									+ new String(data));
						}
						if (code != 0) {
							LogUtil.loge("media session[" + mSess.getLogId()
									+ "]GET_PROCESSING error: " + code);
							MonitorUtil
									.monitorCumulant(Constant.M_GETTICKETERROR);
							AppLogic.runOnBackGround(timeOutRunnable, 0);
							return;
						}
						ResponseURL responseURL = null;
						try {
							responseURL = JsonHelper.toObject(
									ResponseURL.class, new String(data));
						} catch (Exception e1) {
							LogUtil.loge(TAG+"[Exception]"+new String(data), e1);
						}
						if (Constant.ISTESTDATA) {
							LogUtil.logd("media session[" + mSess.getLogId()
									+ "]responseURL: " + responseURL);
						}
						if (responseURL==null||responseURL.getErrCode() != 0) {
							LogUtil.loge("media session[" + mSess.getLogId()
									+ "]GET_PROCESSING error: " + code);
							MonitorUtil.monitorCumulant(Constant.M_GETTICKETERROR);
							AppLogic.runOnBackGround(timeOutRunnable, 0);
							return;
						}
						
						
						mUrl = responseURL.getStrUrl();
						mBackUrl.clear();
						if (CollectionUtils.isNotEmpty(responseURL
								.getArrBackUpUrl())) {
							mBackUrl.add(responseURL.getStrUrl());
							mBackUrl.addAll(responseURL.getArrBackUpUrl());
						}
						if (Constant.ISTESTDATA) {
							if (!mBackUrl.isEmpty()) {
								LogUtil.logd("mBackUrl is" + mBackUrl);
							}
						}

						try {
							mURI = new URI(mUrl);
						} catch (Exception e) {
							LogUtil.loge("media session[" + mSess.getLogId() + "]responseURL :", e);
							QQMusicMediaResponse.this
									.onHttpMediaError(new MediaError(
											MediaError.ERR_URI,
											"wrong media url: " + mUrl,
											"播放地址错误"));
							return;
						}
						if (mMusicAudio != null) {
							mMusicAudio.setFinalUrl(mUrl);
						}
					}
				});
		AppLogic.removeBackGroundCallback(timeOutRunnable);
		AppLogic.runOnBackGround(timeOutRunnable, DEF_TICKET_TIME_OUT);// 超过3s就重试
	}

	Runnable timeOutRunnable = new Runnable() {
		@Override
		public void run() {
			timeOutRetry++;
			LogUtil.logd("timeout ticket and retry");

			AppLogic.removeBackGroundCallback(timeOutRunnable);
			//XXX:bug如果打开注释，请确认：在线歌曲缓冲未完，从历史播放列表中进入继续播放播放到未缓冲处连上网络能继续播放。
//			if (timeOutRetry >= 10) {
//				timeOutRetry = 0;
//				QQMusicMediaResponse.this.onHttpMediaError(new MediaError(MediaError.ERR_REQ_TIMEOUT, "request timeOut", "当前网络不佳"));
//			} else {
				QQMusicMediaResponse.this.refreshDownloadUrl();
//			}
		}
	};
}
