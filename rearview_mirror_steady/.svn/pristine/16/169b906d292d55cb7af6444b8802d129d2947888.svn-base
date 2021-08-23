package com.txznet.txz.component.media.util;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.txz.component.media.base.IRemoteMediaTool;

/**
 * 远程媒体工具调用处理工具
 *
 * Created by J on 2018/10/12.
 */

public class RemoteMediaToolInvoker {
    public static void sendInvoke(IRemoteMediaTool tool, String cmd) {
        sendInvoke(tool, cmd, null);
    }

    public static void sendInvoke(IRemoteMediaTool tool, String cmd, byte[] data) {
        ServiceManager.getInstance().sendInvoke(tool.getInvokePackegeName(),
                tool.getInvokePrefix() + cmd, data, null);
    }

    public static ServiceManager.ServiceData sendInvokeSync(IRemoteMediaTool tool, String cmd) {
        return sendInvokeSync(tool, cmd, (byte[]) null);
    }

    public static ServiceManager.ServiceData sendInvokeSync(IRemoteMediaTool tool, String cmd,
                                                            byte[] data) {
        return ServiceManager.getInstance().sendInvokeSync(tool.getInvokePackegeName(),
                tool.getInvokePrefix() + cmd, data);
    }

    public static boolean sendInvokeSync(IRemoteMediaTool tool, String cmd, boolean defValue) {
        return sendInvokeSync(tool, cmd, null, defValue);
    }

    public static boolean sendInvokeSync(IRemoteMediaTool tool, String cmd, byte[] data,
                                         boolean defValue) {
        ServiceManager.ServiceData invokeRet = sendInvokeSync(tool, cmd, data);
        if (null == invokeRet) {
            return defValue;
        }

        Boolean retBoolean = invokeRet.getBoolean();
        if (null == retBoolean) {
            return defValue;
        }

        return retBoolean;
    }

    public static String sendInvokeSync(IRemoteMediaTool tool, String cmd, String defValue) {
        return sendInvokeSync(tool, cmd, null, defValue);
    }

    public static String sendInvokeSync(IRemoteMediaTool tool, String cmd, byte[] data,
                                        String defValue) {
        ServiceManager.ServiceData invokeRet = sendInvokeSync(tool, cmd, data);
        if (null == invokeRet) {
            return defValue;
        }

        return invokeRet.getString();
    }
}
