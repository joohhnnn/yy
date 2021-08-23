package com.txznet.music.data.http.api.txz.entity.resp;

import java.util.Map;

/**
 * {"stepId":1,"bCache":true,"strRequestUrl":
 * "http:\/\/open.kaolafm.com\/v1\/app\/active",
 * "body":{"sign":"be14755a093ca8958bfcd027c4a37499"
 * ,"appid":"ptvhk0241","deviceid":"fbb128c82b1eb09b4015cb0c01f7964e"},
 * "bIsFinished":false, "errCode":0 }
 *
 * @author telenewbie
 * @version 创建时间：2016年4月19日 下午4:03:54
 */
public class TXZRespFake extends TXZRespBase {
    public int stepId;//步骤
    public boolean bCache;// 是否要缓存
    public String strRequestUrl;//请求的路径
    public Map<String, String> body;//请求的实体
    public boolean bIsFinished;//是否假请求完毕
    public int sid;//
    public long id;//
    public String method;//请求的方式

    public String deviceNum;//
    public long timeStamp;//时间戳

    public long cacheTime;//缓存时间

    @Override
    public String toString() {
        return "TXZRespFake{" +
                "stepId=" + stepId +
                ", bIsFinished=" + bIsFinished +
                ", sid=" + sid +
                ", id=" + id +
                '}';
    }
}
