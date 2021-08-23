package com.txznet.txz.component.media.base;

import com.txznet.txz.component.media.MediaPriorityManager;

/**
 * 电台工具基类
 * Created by J on 2018/4/28.
 */

public abstract class AbsAudioTool implements IMediaTool {
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
