package com.txznet.rxflux.extensions.aac;

import android.app.Application;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

/**
 * 扩展ViewModelProvide工具类
 * 追加ApplicationScope范围的ViewModelProvider
 *
 * @author zackzhou
 */
public class ViewModelProviders extends android.arch.lifecycle.ViewModelProviders {

    @Deprecated
    public ViewModelProviders() {
        super();
    }

    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull Application application) {
        return of(application, null);
    }

    @NonNull
    @MainThread
    public static ViewModelProvider of(@NonNull Application application, ViewModelProvider.Factory factory) {
        if (factory == null) {
            factory = ViewModelProvider.AndroidViewModelFactory.getInstance(application);
        }
        return new ViewModelProvider(GlobalViewModelStore.get(), factory);
    }
}
