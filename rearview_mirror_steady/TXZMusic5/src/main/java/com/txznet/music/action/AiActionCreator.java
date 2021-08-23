package com.txznet.music.action;

import com.txznet.music.Constant;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.http.api.txz.entity.resp.AiPullData;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.RxAction;

/**
 * AI电台
 *
 * @author zackzhou
 * @date 2019/2/12,10:49
 */

public class AiActionCreator {

    private static AiActionCreator sInstance = new AiActionCreator();

    private AiActionCreator() {
    }

    public static AiActionCreator get() {
        return sInstance;
    }

    public void pushAi(AiPullData aiPullData) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_AI_RADIO_PUSH)
                .bundle(Constant.AiConstant.KEY_PUSH_DATA, aiPullData)
                .build());
    }

    public void deleteAi(AudioV5 audioV5) {
        Logger.d(Constant.LOG_TAG_AI, "deleteAi ->" + audioV5);
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_AI_RADIO_DELETE)
                .bundle(Constant.AiConstant.KEY_AUDIO, audioV5)
                .build());
    }
}
