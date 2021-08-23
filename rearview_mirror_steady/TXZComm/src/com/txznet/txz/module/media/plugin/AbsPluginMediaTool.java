package com.txznet.txz.module.media.plugin;

import android.util.Log;

/**
 * 外部适配的媒体工具基类
 *
 * Created by J on 2019/3/15.
 */
public abstract class AbsPluginMediaTool implements IPluginMediaTool {
    private PluginMediaToolDataInterface mDataInterface;

    /**
     * 接收Core发起的数据调用并进行处理
     *
     * @param cmd 命令字
     * @param data 调用数据
     * @return 调用结果
     */
    protected byte[] onInvoke(final String cmd, final byte[] data) throws Exception {
        return null;
    }

    /**
     * 主动向Core发起调用
     *
     * @param cmd 命令字
     * @param data 调用数据
     * @return 调用结果
     */
    protected final byte[] sendInvoke(String cmd, final byte[] data) {
        if (null == mDataInterface) {
            Log.e("PluginMediaTool", "data interface is null, invoke abandoned: " + cmd);
            return null;
        }

        return mDataInterface.onMediaToolInvoke(getPackageName(), cmd, data);
    }

    public AbsPluginMediaTool(PluginMediaToolDataInterface dataInterface) {
        mDataInterface = dataInterface;
    }

    @Override
    public boolean interceptRecordWinControl(MEDIA_TOOL_OP op) {
        return false;
    }

    @Override
    public final byte[] invoke(final String cmd, final byte[] data) {
        try {
            return onInvoke(cmd, data);
        } catch (Exception e) {
            return null;
        }
    }

    protected final void notifyPlayerStatusChange(PLAYER_STATUS newStatus) {
        sendInvoke("status.change", newStatus.name().getBytes());
    }
}
