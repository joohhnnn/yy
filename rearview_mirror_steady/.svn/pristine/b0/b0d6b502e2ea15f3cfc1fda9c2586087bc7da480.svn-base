package com.txznet.launcher.widget;

/**
 * Created by TXZ-METEORLUO on 2018/2/26.
 * 定义小欧的状态。
 */
public interface IStateHandler {
    int STATE_NORMAL = 0; // 正常状态，显示一个录音图标
    int STATE_RECORD_START = 1; // 录音开始，显示一个声纹动画
    int STATE_RECORD_END = 2; // 录音结束，显示一个处理中动画
    int STATE_TTS_START = 3; // 播报中
    int STATE_TTS_END = 4; // 播报中

    void updateState(int state);
}