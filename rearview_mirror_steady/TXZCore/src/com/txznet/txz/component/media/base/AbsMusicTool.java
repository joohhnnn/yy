package com.txznet.txz.component.media.base;

import com.txznet.txz.component.media.MediaPriorityManager;

/**
 * 音乐工具基类
 *
 * Core内适配的音乐工具需要继承此基类并实现相关接口
 *
 * Created by J on 2018/4/28.
 */

public abstract class AbsMusicTool implements IMediaTool {

    @Override
    public boolean interceptRecordWinControl(MEDIA_TOOL_OP op) {
        return false;
    }

    protected final void notifyPlayerStatusChange(PLAYER_STATUS newStatus) {
        if (PLAYER_STATUS.PLAYING == newStatus) {
            MediaPriorityManager.getInstance().notifyPriorityChange(this);
        }
    }
}
