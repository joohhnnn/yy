package com.txznet.fm.manager;

import java.util.List;

/**
 * Created by telen on 2018/5/16.
 */

public interface IManager<T> {

    /**
     * 只需要管理返回的数据（主要用于显示），其他的数据都不用关系
     *
     * @param <T>
     */
    public interface CallbackManager<T> {
        void onSuccess(List<T> tList);

        void onError(String message);
    }
}
