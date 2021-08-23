package com.txznet.audio.player;

import com.txznet.audio.player.entity.Audio;

/**
 * 音频资源的实际播放路径提供方
 */
public interface PlayUrlProvider {

    interface OnPlayUrlCallback {

        /**
         * 这里一个音频只能对应一个播放链接，不支持候选链接，没有重试机制，需要自己筛选
         * 开启代理的情况，标识资源是依据audioId的，一对一，多个候选链接对应同一个缓存文件
         */
        void onPlayUrlResp(String playUrl);

        void onError();

    }

    /**
     * 获取音频资源的实际播放路径
     *
     * @param audio       音频资源
     * @param urlCallback 回调
     */
    void getPlayUrl(Audio audio, OnPlayUrlCallback urlCallback);
}
