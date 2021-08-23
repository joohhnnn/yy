package com.txznet.rxflux;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * RxFlux的事件总线实现，用于View->Flow，Flow->Store的Action传递
 *
 * @author from network
 */
public class RxBus {
    private final Subject<Object> mBus;

    public RxBus() {
        mBus = PublishSubject.create().toSerialized();
    }

    public void post(Object obj) {
        mBus.onNext(obj);
    }

    public <T> Observable<T> toObservable(Class<T> clazz) {
        return mBus.ofType(clazz);
    }

    public Observable<Object> toObservable() {
        return mBus;
    }

    public boolean hasObservers() {
        return mBus.hasObservers();
    }
}
