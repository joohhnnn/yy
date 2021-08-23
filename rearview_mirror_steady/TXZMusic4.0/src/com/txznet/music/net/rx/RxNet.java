package com.txznet.music.net.rx;

import io.reactivex.Observable;

/**
 * Created by brainBear on 2018/2/28.
 */

public class RxNet {


    public static <T> Observable<T> request(String url, Object data, Class<T> t) {
        return new NetObservable<T>(url, data, t);
    }

}
