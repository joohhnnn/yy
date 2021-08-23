package com.txznet.music.soundControlModule.logic;

import android.util.SparseArray;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.playerModule.bean.TempReq;
import com.txznet.music.playerModule.logic.net.request.ReqThirdSearch;
import com.txznet.music.playerModule.logic.net.response.RespThirdSearch;
import com.txznet.music.service.MusicInteractionWithCore;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.StringUtils;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by telenewbie on 2017/1/4.
 */

public class FackLogic implements IFackLogic {
    public static final String FACKTAG = "music::fack:";
    //##创建一个单例类##
    private volatile static FackLogic singleton;
    private static SparseArray<Map<Integer, TempReq>> mCacheArray = new SparseArray<Map<Integer, TempReq>>();

    private FackLogic() {
    }

    public static FackLogic getInstance() {
        if (singleton == null) {
            synchronized (FackLogic.class) {
                if (singleton == null) {
                    singleton = new FackLogic();
                }
            }
        }
        return singleton;
    }


    public void handleError() {
        // TODO: 2017/6/20 异常处理
    }

    @Override
    public void doFakeReq(final RespThirdSearch object) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtil.logd(FACKTAG + object.toString());
                if (!object.isbIsFinished()) {// 没有完成
                    try {
                        Map<Integer, TempReq> temp = mCacheArray.get(object.getSid());
                        if (temp != null) {
                            TempReq tempReq = temp.get(object.getStepId());
                            if (tempReq != null) {
                                // TODO:如何算超时？？？
                                if (System.currentTimeMillis() - tempReq.getTimestamp() <= tempReq
                                        .getCacheTime()) {// 如果没有超时的话
                                    LogUtil.loge(FACKTAG + "use cache request " + object.getStepId());
                                    ReqThirdSearch reqData = new ReqThirdSearch(
                                            object.getSid(), object.getId(),
                                            object.getStepId());
                                    reqData.setDeviceNum(object.getDeviceNum());
                                    reqData.setTimeStamp(object.getTimeStamp());
                                    MusicInteractionWithCore.requestData(Constant.GET_WAY, Constant.GET_FAKE_SEARCH, JsonHelper.toJson(reqData).getBytes());
                                    return;
                                }
                            }
                        }
                        StringBuilder sBuffer = new StringBuilder();
                        if (object != null && object.getBody() != null
                                && object.getBody().size() > 0) {
                            sBuffer.append("?");
                            Iterator<String> iterator = object.getBody().keySet()
                                    .iterator();
                            while (iterator.hasNext()) {
                                String key = iterator.next();
                                sBuffer.append(key).append("=").append(object.getBody().get(key)).append("&");
                            }
                            if (sBuffer.length() > 0) {
                                sBuffer.deleteCharAt(sBuffer.length() - 1);
                            }
                        }
                        if (StringUtils.isEmpty(object.getStrRequestUrl())) {
                            LogUtil.logd(FACKTAG + "request url is null");//假请求路径是null
                            return;
                        }
                        HttpURLConnection connection = (HttpURLConnection) new URL(
                                object.getStrRequestUrl() + sBuffer.toString())
                                .openConnection();
                        if (StringUtils.isEmpty(object.getMethod())) {
                            LogUtil.loge(FACKTAG + "request method have error ");
                            return;
                        }
                        connection.setRequestMethod(object.getMethod().toUpperCase());
                        if (object != null && object.getBody() != null
                                && object.getBody().size() > 0) {
                            Iterator<String> iterator1 = object.getBody().keySet()
                                    .iterator();
                            while (iterator1.hasNext()) {
                                String key = iterator1.next();
                                connection.setRequestProperty(key, object.getBody()
                                        .get(key));
                            }

                        }
                        if (connection.getResponseCode() == 200) {// 正常
                            LogUtil.loge(FACKTAG + "response fack url success : " + object.getStepId());
                            StringBuilder sbBuffer = new StringBuilder();
                            byte[] bytes = new byte[2048];
                            ReqThirdSearch reqData = new ReqThirdSearch(
                                    object.getSid(), object.getId(), object.getStepId());
                            reqData.setDeviceNum(object.getDeviceNum());
                            reqData.setTimeStamp(object.getTimeStamp());
                            int read = 0;
                            if (object.isbCache()) {
                                // 保存起来数据
                                InputStream inputStream = connection.getInputStream();
                                while ((read = inputStream.read(bytes)) > 0) {
                                    sbBuffer.append(new String(bytes, 0, read));
                                }
                                // 保存临时变量
                                Map<Integer, TempReq> map = mCacheArray.get(object
                                        .getSid());
                                if (map == null) {
                                    map = new HashMap<>();
                                }
                                TempReq req = new TempReq();
                                req.setData(sbBuffer.toString());
                                req.setTimestamp(System.currentTimeMillis());
                                req.setCacheTime(object.getCacheTime());

                                map.put(object.getStepId(), req);
                                mCacheArray.append(object.getSid(), map);
                                reqData.setStrCache(sbBuffer.toString());
                                LogUtil.logd(FACKTAG + reqData.toString());
                            }
                            MusicInteractionWithCore.requestData(Constant.GET_WAY, Constant.GET_FAKE_SEARCH, JsonHelper.toJson(reqData).getBytes());

                        } else {
                            LogUtil.logd(FACKTAG + "connection.getResponseCode()="
                                    + connection.getResponseCode());
                        }
                    } catch (Exception e) {
                        LogUtil.loge(FACKTAG + "occur error" + e);
                    }

                } else {
                    try {
                        if (StringUtils.isNotEmpty(object.getStrRequestUrl())) {
                            HttpURLConnection connection = (HttpURLConnection) new URL(
                                    object.getStrRequestUrl()).openConnection();
                            if (connection.getResponseCode() == 200) {// 正常
                                // 不做任何处理
                                LogUtil.logd(FACKTAG + "fake is over");
                            } else {
                                LogUtil.logd(FACKTAG + "fake is over,but " + connection.getResponseCode());
                            }
                        } else {
                            LogUtil.logd(FACKTAG + "fake is over ,but realurl is empty");
                        }
                    } catch (Exception e) {
                        LogUtil.loge(FACKTAG + "fake is over,but occur error ", e);
                    }
                }
            }
        }).start();

    }

}
