package com.txznet.music.helper;

import android.util.ArrayMap;
import android.util.SparseArray;

import com.txznet.comm.err.Error;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.Constant;
import com.txznet.music.data.http.api.txz.TXZMusicApi;
import com.txznet.music.data.http.api.txz.TXZMusicApiImpl;
import com.txznet.music.data.http.api.txz.entity.req.TXZReqFake;
import com.txznet.music.data.http.api.txz.entity.resp.TXZRespFake;
import com.txznet.music.util.DisposableManager;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ThreadManager;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 假请求辅助工具
 *
 * @author zackzhou
 * @date 2018/12/21,11:02
 */

public class FakeReqHelper {
    public static final String TAG = Constant.LOG_TAG_NET + ":Fake";

    private static final class Holder {
        private static final FakeReqHelper INSTANCE = new FakeReqHelper();
    }

    private SparseArray<Map<Integer, TempReq>> mCacheArray = new SparseArray<>();
    private TXZMusicApi txzMusicApi = TXZMusicApiImpl.getDefault();

    private FakeReqHelper() {
    }

    public static FakeReqHelper get() {
        return Holder.INSTANCE;
    }

    /**
     * 用于存放临时请求
     *
     * @author telenewbie
     * @version 创建时间：2016年4月22日 下午9:14:47
     */
    public class TempReq {
        public long timestamp;
        public String data;
        public long cacheTime;
    }

    /**
     * 对指定音频进行假请求
     *
     * @param sid 音频的sid
     * @param id  音频的id
     */
    public void doFakeReq(int sid, long id) {
        Disposable disposable = txzMusicApi.fakeRequest(new TXZReqFake(sid, id, 0))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.single())
                .subscribe(this::doFakeReqInner, this::handleError);
        DisposableManager.get().add("doFakeReq", disposable);
    }


    private void doFakeReqInner(TXZRespFake respFake) {
        Runnable runnable = () -> {
            Logger.d(TAG, respFake.toString());
            if (!respFake.bIsFinished) {// 没有完成
                try {
                    Map<Integer, TempReq> temp = mCacheArray.get(respFake.sid);
                    if (temp != null) {
                        TempReq tempReq = temp.get(respFake.stepId);
                        if (tempReq != null) {
                            // TODO:如何算超时？？？
                            if (System.currentTimeMillis() - tempReq.timestamp <= tempReq.cacheTime) {// 如果没有超时的话
                                Logger.e(TAG, "use cache request " + respFake.stepId);
                                TXZReqFake reqFake = new TXZReqFake(respFake.sid, respFake.id, respFake.stepId);
                                reqFake.deviceNum = respFake.deviceNum;
                                reqFake.timeStamp = respFake.timeStamp;

                                TXZNetRequest.get().sendSeqRequestToCore(TXZMusicApi.GET_FAKE_SEARCH, JsonHelper.toJson(reqFake).getBytes(), null);
                                return;
                            }
                        }
                    }
                    StringBuilder sBuilder = new StringBuilder();
                    if (respFake.body != null && respFake.body.size() > 0) {
                        sBuilder.append("?");
                        for (String key : respFake.body.keySet()) {
                            sBuilder.append(key).append("=").append(respFake.body.get(key)).append("&");
                        }
                        if (sBuilder.length() > 0) {
                            sBuilder.deleteCharAt(sBuilder.length() - 1);
                        }
                    }
                    if (StringUtils.isEmpty(respFake.strRequestUrl)) {
                        Logger.d(TAG, "request url is null");//假请求路径是null
                        return;
                    }
                    HttpURLConnection connection = (HttpURLConnection) new URL(respFake.strRequestUrl + sBuilder.toString()).openConnection();
                    if (StringUtils.isEmpty(respFake.method)) {
                        Logger.e(TAG, "request method have error ");
                        return;
                    }
                    connection.setRequestMethod(respFake.method.toUpperCase());
                    if (respFake.body != null && respFake.body.size() > 0) {
                        for (String key : respFake.body.keySet()) {
                            connection.setRequestProperty(key, respFake.body.get(key));
                        }

                    }
                    if (connection.getResponseCode() == 200) {// 正常
                        Logger.e(TAG, "response fack url success : " + respFake.stepId);
                        StringBuilder sbBuffer = new StringBuilder();
                        byte[] bytes = new byte[2048];
                        TXZReqFake reqData = new TXZReqFake(respFake.sid, respFake.id, respFake.stepId);
                        reqData.deviceNum = respFake.deviceNum;
                        reqData.timeStamp = respFake.timeStamp;
                        int read = 0;
                        if (respFake.bCache) {
                            // 保存起来数据
                            try (InputStream inputStream = connection.getInputStream()) {
                                while ((read = inputStream.read(bytes)) > 0) {
                                    sbBuffer.append(new String(bytes, 0, read));
                                }
                            }

                            // 保存临时变量
                            Map<Integer, TempReq> map = mCacheArray.get(respFake.sid);
                            if (map == null) {
                                map = new ArrayMap<>(1);
                            }
                            TempReq req = new TempReq();
                            req.data = sbBuffer.toString();
                            req.timestamp = System.currentTimeMillis();
                            req.cacheTime = respFake.cacheTime;

                            map.put(respFake.stepId, req);
                            mCacheArray.append(respFake.sid, map);
                            reqData.strCache = sbBuffer.toString();
                            Logger.d(TAG, reqData.toString());
                        }
                        TXZNetRequest.get().sendSeqRequestToCore(TXZMusicApi.GET_FAKE_SEARCH, JsonHelper.toJson(reqData).getBytes(), null);

                    } else {
                        Logger.d(TAG, "connection.getResponseCode()=" + connection.getResponseCode());
                    }
                } catch (Exception e) {
                    Logger.e(TAG, "occur error" + e);
                }

            } else {
                try {
                    if (StringUtils.isNotEmpty(respFake.strRequestUrl)) {
                        HttpURLConnection connection = (HttpURLConnection) new URL(respFake.strRequestUrl).openConnection();
                        if (connection.getResponseCode() == 200) {// 正常
                            // 不做任何处理
                            Logger.d(TAG, "fake is over");
                        } else {
                            Logger.d(TAG, "fake is over,but " + connection.getResponseCode());
                        }
                    } else {
                        Logger.d(TAG, "fake is over ,but realurl is empty");
                    }
                } catch (Exception e) {
                    Logger.e(TAG, "fake is over,but occur error ", e);
                }
            }
        };
        ThreadManager.getPool().execute(runnable);
    }

    private void handleError(Throwable throwable) {
        if (throwable instanceof Error) {
            Logger.e(TAG, "fake request error;" + ((Error) throwable).errorCode);
        }
    }
}
