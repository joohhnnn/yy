package com.txznet.music.model.logic.queue;

import com.txznet.audio.player.entity.Audio;
import com.txznet.audio.player.queue.PlayQueue;
import com.txznet.music.Constant;
import com.txznet.music.data.entity.Album;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.util.TtsHelper;
import com.txznet.rxflux.Operation;

/**
 * 新闻专辑下的音频选择器
 *
 * @author zackzhou
 * @date 2018/12/18,15:23
 */

public class NewsQueueItemPicker extends AbstractQueueItemPicker {

    public NewsQueueItemPicker(Album album) {
        super(album);
    }

    @Override
    public void pickPrevItem(PlayQueue queue, Audio oriAudio, Callback callback) {
        // 已经是第一个的处理逻辑
        super.pickPrevItem(queue, oriAudio, callback);
        if (queue.isEmpty()) {
            return;
        }
        if (queue.getCurrentPosition() == 0) {
            notifyAlreadyFirst(PlayHelper.get().getLastSwitchAudioOperation());
            return;
        }
        // 上一首不判断断点逻辑
        callback.onPickResult(oriAudio);
    }

    @Override
    public void pickNextItem(PlayQueue queue, final Audio oriAudio, boolean fromUser, Callback callback) {
        super.pickNextItem(queue, oriAudio, fromUser, callback);
        if (queue.isEmpty()) {
            return;
        }
        // FIXME: 2018/12/19 新闻排序后，最后一个音频不是原始数据最后一个音频
        if (oriAudio == null) {
            onAlbumEnd(PlayHelper.get().getLastSwitchAudioOperation(), fromUser);
        } else {
            callback.onPickResult(oriAudio);
        }
    }

    // 专辑播放完毕
    private void onAlbumEnd(Operation operation, boolean fromUser) {
        if (fromUser) {
            if (Operation.SOUND == operation) {
                TtsHelper.speakResource("RS_VOICE_ALREADY_LAST", Constant.RS_VOICE_ALREADY_LAST);
            } else {
                ToastUtils.showShortOnUI(Constant.RS_VOICE_ALREADY_LAST);
            }
        } else {
            TtsHelper.speakResource("RS_VOICE_SPEAK_PLAY_END", Constant.RS_VOICE_SPEAK_PLAY_END);
            Logger.d(TAG, "onAlbumEnd album=" + album.name);
            album.isPlayEnd = true;
        }
    }
}
