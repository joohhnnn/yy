package com.txznet.webchat.util;

import com.txznet.comm.remote.ServiceManager;

/**
 * 远程调用工具
 * Created by J on 2018/8/16.
 */

public class ServiceManagerInvoker {
    /**
     * 发起异步远程调用
     * @param remote 被调端包名
     * @param cmd 命令字
     * @param data 调用数据
     */
    public static void sendInvoke(String remote, String cmd, byte[] data) {
        ServiceManager.getInstance().sendInvoke(remote, cmd, data, null);
    }

    /**
     * 发起同步远程调用
     * @param remote 被调端包名
     * @param cmd 命令字
     * @param data 调用数据
     * @param defValue 默认值
     * @return 调用结果, 调用失败或发生错误时返回默认值
     */
    public static boolean sendInvokeSync(String remote, String cmd, byte[] data, boolean defValue) {
        ServiceManager.ServiceData ret = ServiceManager.getInstance().sendInvokeSync(remote,
                cmd, data);

        if (null == ret) {
            return defValue;
        }

        return ret.getBoolean();
    }

    /**
     * 发起同步远程调用
     * @param remote 被调端包名
     * @param cmd 命令字
     * @param data 调用数据
     * @param defValue 默认值
     * @return 调用结果, 调用失败或发生错误时返回默认值
     */
    public static int sendInvokeSync(String remote, String cmd, byte[] data, int defValue) {
        ServiceManager.ServiceData ret = ServiceManager.getInstance().sendInvokeSync(remote,
                cmd, data);

        if (null == ret) {
            return defValue;
        }

        return ret.getInt();
    }

    /**
     * 发起同步远程调用
     * @param remote 被调端包名
     * @param cmd 命令字
     * @param data 调用数据
     * @param defValue 默认值
     * @return 调用结果, 调用失败或发生错误时返回默认值
     */
    public static long sendInvokeSync(String remote, String cmd, byte[] data, long defValue) {
        ServiceManager.ServiceData ret = ServiceManager.getInstance().sendInvokeSync(remote,
                cmd, data);

        if (null == ret) {
            return defValue;
        }

        return ret.getLong();
    }

    /**
     * 发起同步远程调用
     * @param remote 被调端包名
     * @param cmd 命令字
     * @param data 调用数据
     * @param defValue 默认值
     * @return 调用结果, 调用失败或发生错误时返回默认值
     */
    public static String sendInvokeSync(String remote, String cmd, byte[] data, String defValue) {
        ServiceManager.ServiceData ret = ServiceManager.getInstance().sendInvokeSync(remote,
                cmd, data);

        if (null == ret) {
            return defValue;
        }

        return ret.getString();
    }
}
