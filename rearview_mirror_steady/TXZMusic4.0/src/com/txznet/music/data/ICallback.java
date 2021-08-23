package com.txznet.music.data;

import java.util.List;

/**
 * 只需要管理返回的数据（主要用于显示），其他的数据都不用关系
 *
 * @param <T>
 */

public interface ICallback<T> {

    void onSuccess(List<T> tList);


    /**
     * 返回提内容为空
     */
    void onEmptyData();

    /**
     * 网络未连接错误
     */
    void onNetError();

    /**
     * 超时错误
     */
    void onTimeOutError();

    /**
     * 服务端错误
     *
     * @param errorCode
     */
    void onServerError(int errorCode);

}
