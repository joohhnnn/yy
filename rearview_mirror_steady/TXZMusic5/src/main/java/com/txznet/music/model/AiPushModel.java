package com.txznet.music.model;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.http.api.txz.entity.resp.AiPullData;
import com.txznet.music.data.source.AiPushDataSource;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.RxWorkflow;

import java.util.List;

/**
 * @author zackzhou
 * @date 2019/2/12,11:02
 */

public class AiPushModel extends RxWorkflow {
    @Override
    public void onAction(RxAction action) {
        switch (action.type) {
            case ActionType.ACTION_AI_RADIO_PUSH:
                AiPullData pullData = (AiPullData) action.data.get(Constant.AiConstant.KEY_PUSH_DATA);
                Logger.d(Constant.LOG_TAG_AI, "ai push from service, pullData=" + pullData);
                if (AiPullData.PRE_ACTION_CLEAR_QUEUE == pullData.preAction) {
                    AiPushDataSource.get().clear();
                }
                List<Audio> audioList = AudioConverts.convert2List(pullData.arrAudios, AudioConverts::convert2MediaAudio);
                if (!audioList.isEmpty()) {
                    if (AiPullData.ACTION_TYPE_INSERT_HEAD == pullData.action) {
                        AiPushDataSource.get().pushFirst(audioList);
                    } else {
                        AiPushDataSource.get().push(audioList);
                    }
                }
                break;
            case ActionType.ACTION_AI_RADIO_DELETE:
                AudioV5 audioV5 = (AudioV5) action.data.get(Constant.AiConstant.KEY_AUDIO);
                AudioPlayer.getDefault().next(true);
                AudioPlayer.getDefault().getQueue().remove(AudioConverts.convert2MediaAudio(audioV5));
                break;
        }
    }
}
