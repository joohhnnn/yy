package com.txznet.music.model;

import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.util.FileConfigUtil;
import com.txznet.music.util.Logger;
import com.txznet.music.util.Utils;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;
import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * 设备休眠相关
 *
 * @author telen
 * @date 2018/12/3,11:39
 */
public class PowerModel extends RxWorkflow {

    private boolean isReversingPause;

    public PowerModel() {
    }

    @Override
    public void onAction(RxAction action) {
        boolean isPlay;
        boolean isReverse;
        switch (action.type) {
            case ActionType.ACTION_POWER_SLEEP:
                isPlay = SharedPreferencesUtils.getIsPlay();
                Logger.d(Constant.LOG_TAG_POWER, "client sleep:  isPlay:" + isPlay);
                Utils.sleep();
                break;
            case ActionType.ACTION_POWER_WAKEUP:
                isPlay = SharedPreferencesUtils.getIsPlay();
                Logger.d(Constant.LOG_TAG_POWER, "client wakeup:  isPlay:" + isPlay);
                Utils.wakeup();
                if (isPlay) {
                    PlayerActionCreator.get().play(Operation.ACC);
                }
                break;
            case ActionType.ACTION_POWER_ENTER_REVERSE:
                boolean play = FileConfigUtil.getBooleanConfig(TXZFileConfigUtil.KEY_MUSIC_REVERSING_PLAY, true);
                Logger.d(Constant.LOG_TAG_POWER, "client enter reverse, play=" + play);
                if (!play) {
                    PlayerActionCreator.get().pause(Operation.ACC);
                    isReversingPause = true;
                }
                break;
            case ActionType.ACTION_POWER_EXIT_REVERSE:
                Logger.d(Constant.LOG_TAG_POWER, "client exit reverse");
                if (isReversingPause) {
                    PlayerActionCreator.get().play(Operation.ACC);
                }
                break;
            default:
                break;
        }
    }
}
