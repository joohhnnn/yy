package com.txznet.music.store;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;

/**
 * @author telen
 * @date 2018/12/17,20:59
 */
public class SettingStore extends Store {

    MutableLiveData<Boolean> mBootEnable = new MutableLiveData<>();
    MutableLiveData<Boolean> mASREnable = new MutableLiveData<>();

    @Override
    protected String[] getActionTypes() {
        return new String[]{
                ActionType.ACTION_SETTING_CLICK_BOOT_PLAY,
                ActionType.ACTION_SETTING_CLICK_CHANGE_FLOAT_PLAYER,
                ActionType.ACTION_SETTING_CLICK_CLEAR_MEMORY,
                ActionType.ACTION_SETTING_CLICK_HELP,
                ActionType.ACTION_SETTING_CLICK_OPEN_ASR
        };
    }

    @Override
    protected void onAction(RxAction action) {

    }

    @Override
    protected void onData(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_SETTING_CLICK_BOOT_PLAY:
                mBootEnable.setValue((Boolean) action.data.get(Constant.SettingConstant.KEY_ENABLE));
                break;
            case ActionType.ACTION_SETTING_CLICK_OPEN_ASR:
                mASREnable.setValue((Boolean) action.data.get(Constant.SettingConstant.KEY_ENABLE));
                break;
            default:
                break;
        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {

    }

    public LiveData<Boolean> getBootEnable() {
        return mBootEnable;
    }

    public LiveData<Boolean> getASREnable() {
        return mASREnable;
    }
}
