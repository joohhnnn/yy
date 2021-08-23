package com.txznet.rxflux.extensions.aac;

import android.arch.lifecycle.ViewModelStore;

/**
 * 应用内全局的GlobalViewModelStore
 *
 * @author zackzhou
 */
public class GlobalViewModelStore extends ViewModelStore {

    private static final ViewModelStore GLOBAL_STORE = new ViewModelStore();

    private GlobalViewModelStore() {

    }

    public static ViewModelStore get() {
        return GLOBAL_STORE;
    }
}
