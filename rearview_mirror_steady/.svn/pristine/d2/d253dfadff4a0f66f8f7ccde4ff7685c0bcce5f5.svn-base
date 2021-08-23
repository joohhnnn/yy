package com.txznet.sdk.wechat;

/**
 * 微信工具
 * Created by J on 2018/8/14.
 */

public interface ITXZWechatTool {
    /**
     * 获取sdk版本号
     * 微信sdk发生无法兼容的修改时提升版本号, 通过版本号进行兼容
     *
     * @return 当前微信工具的版本号
     */
    int getSdkVersion();

    /**
     * 处理微信客户端发起的sdk调用
     * @param packageName 微信的包名
     * @param command 调用命令
     * @param data 调用携带的数据
     * @return 处理结果
     */
    byte[] procSdkInvoke(String packageName, String command, byte[] data);
}
