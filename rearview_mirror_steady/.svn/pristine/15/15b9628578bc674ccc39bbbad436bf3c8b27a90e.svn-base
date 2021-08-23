package com.txznet.sdk.media;

import com.txznet.sdk.media.base.AbsTXZMediaTool;
import com.txznet.sdk.media.constant.InvokeConstants;
import com.txznet.sdk.media.constant.PlayerStatus;

/**
 * 同行者远程电台工具
 *
 * 自定义远程工具需要实现此基类的抽象方法, 并在播放器发生状态改变时主动调用接口进行通知.
 *
 * 通知播放状态变化:
 * @see AbsTXZAudioTool#onPlayerStatusChanged(PlayerStatus)
 * 通知播放节目变化:
 * @see AbsTXZAudioTool#onPlayingModelChanged(TXZMediaModel)
 *
 * Created by J on 2018/5/10.
 */

public abstract class AbsTXZAudioTool extends AbsTXZMediaTool {

    /**
     * 版本修改记录:
     * 1: 初始版本
     * 2: 添加搜索结果列表展示相关接口
     */
    public final int getSDKVersion() {
        return 2;
    }

    @Override
    public final String getRemoteInvokePrefix() {
        return InvokeConstants.CMD_PREFIX_AUDIO;
    }
}
