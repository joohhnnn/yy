package com.txznet.music.model;

import com.txznet.audio.player.IMediaPlayer;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.AsrManager;
import com.txznet.music.service.impl.PushCommand;
import com.txznet.music.service.push.AppPushInvoker;
import com.txznet.music.util.Logger;
import com.txznet.proxy.util.StorageUtil;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.txznet.music.action.ActionType.ACTION_PLAYER_ON_STATE_CHANGE;
import static com.txznet.music.action.ActionType.ACTION_RECORD_WIN_DISMISS;
import static com.txznet.music.action.ActionType.ACTION_RECORD_WIN_SHOW;

/**
 * 真正执行逻辑的位置
 *
 * @author telen
 * @date 2018/12/17,17:51
 */
public class SettingModel extends RxWorkflow {
    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_SETTING_CLICK_BOOT_PLAY:
                clickBootPlay(action);
                break;
            case ActionType.ACTION_SETTING_CLICK_CHANGE_FLOAT_PLAYER:
                clickChangeFloatUI(action, (Integer) action.data.get(Constant.SettingConstant.KEY_CHANGE_FLOAT_STYPE));
                break;
            case ActionType.ACTION_SETTING_CLICK_CLEAR_MEMORY:
                clickClearMemory(action);
                break;
            case ActionType.ACTION_SETTING_CLICK_HELP:
                clickHelp(action);
                break;
            case ActionType.ACTION_SETTING_CLICK_OPEN_ASR:
                clickAsr(action);
                break;
            case ACTION_RECORD_WIN_SHOW:
                closeAsr();
                AppPushInvoker.getInstance().onCancel(true);
                break;
            case ACTION_RECORD_WIN_DISMISS:
                openAsr();
                PushCommand.getInstance().processPushListDelay();
                break;
            case ACTION_PLAYER_ON_STATE_CHANGE:
                int state = (int) action.data.get(Constant.PlayConstant.KEY_PLAY_STATE);
                if (IMediaPlayer.STATE_ON_PLAYING == state) {
                    openAsr();
                }
                break;
            default:
                break;
        }
    }

    private void closeAsr() {
        AppLogic.runOnBackGround(() -> {
            Logger.d(Constant.LOG_TAG_SETTINGS, "closeAsr");
            if (SharedPreferencesUtils.isWakeupEnable()) {
                AsrManager.getInstance().unregCMD();
            }
        });
    }

    private void openAsr() {
        AppLogic.runOnBackGround(() -> {
            Logger.d(Constant.LOG_TAG_SETTINGS, "openAsr");
            if (SharedPreferencesUtils.isWakeupEnable()) {
                AsrManager.getInstance().regCMD();
            }
        });
    }

    private void clickAsr(RxAction action) {
        //现在的值
        //依据现在的值进行设置,如果已经是注册过了,则反注册
        boolean wakeupEnable = SharedPreferencesUtils.isWakeupEnable();
        SharedPreferencesUtils.setWakeupEnable(!wakeupEnable);
        if (wakeupEnable) {
            AsrManager.getInstance().unregCMD();
        } else {
            AsrManager.getInstance().regCMD();
        }
        postRxData(RxAction.type(ActionType.ACTION_SETTING_CLICK_OPEN_ASR).bundle(Constant.SettingConstant.KEY_ENABLE, !wakeupEnable).build());
    }

    private void clickHelp(RxAction action) {
        //展示帮助信息?

    }

    private void clickClearMemory(RxAction action) {
        long cacheSize = StorageUtil.getCacheSize();
        Disposable disposable = Observable.create(emitter -> {
            StorageUtil.deleteCache(1.0f);
            emitter.onComplete();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(o -> {
            action.data.put(Constant.SettingConstant.KEY_CACHE_SIZE, cacheSize);
            postRxData(action);
        });
    }

    private void clickChangeFloatUI(RxAction action, int type) {
// TODO: 2018/12/17 需要发送给Core当前的视图状态吧!?
        SharedPreferencesUtils.setFloatUIType(type);


    }

    private void clickBootPlay(RxAction action) {
        // 从sp中获取到现在的值
        // 改变相应的值
        boolean enable = SharedPreferencesUtils.isOpenPush();
        SharedPreferencesUtils.setOpenPush(!enable);
        postRxData(RxAction.type(ActionType.ACTION_SETTING_CLICK_BOOT_PLAY).bundle(Constant.SettingConstant.KEY_ENABLE, !enable).build());
    }


}
