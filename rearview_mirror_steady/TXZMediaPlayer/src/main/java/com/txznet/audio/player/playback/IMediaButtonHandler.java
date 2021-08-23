package com.txznet.audio.player.playback;

/**
 * 音频按键监听
 */
public interface IMediaButtonHandler {

    /**
     * 当音频按键按下的时候
     *
     * @param oriKeyCode 键码，扩展按键会映射成原始键位
     * @return 是否拦截原始逻辑，false则内部进行实际的操作
     */
    boolean onMediaButtonKeyDown(int oriKeyCode);
}
