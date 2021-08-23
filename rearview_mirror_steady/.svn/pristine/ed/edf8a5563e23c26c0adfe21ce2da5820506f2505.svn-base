package com.txznet.sdk;


import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;

/**
 * 星座管理类
 * 自定义推送弹窗样式
 */
public class TXZConstellationManager {

    private static TXZConstellationManager mInstance = new TXZConstellationManager();
    private ConstellationTool mConstellationTool;
    private boolean mHasSetConstellationTool;


    private TXZConstellationManager() {
    }

    /**
     * 获取单例
     *
     * @return
     */
    public static TXZConstellationManager getInstance() {
        return mInstance;
    }

    public void onReconnectTXZ() {
        if (mHasSetConstellationTool) {
            setConstellationTool(mConstellationTool);
        }
    }

    public static interface ConstellationTool {
        /**
         * 需要展示提醒弹窗
         * @param json
         *             {
         *             "level":1, //运势等级
         *             "name":"双鱼座",//星座名
         *             "desc":"双鱼座今天的运势XXX",//运势描述
         *             "type":"今日运势"//运势类型
         *             }
         */
        void onShowPush(String json);
    }

    public void cancel() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.constellation.cancel", null, null);
    }

    /**
     * 设置星座推送工具
     * @param tool
     */
    public void setConstellationTool(ConstellationTool tool) {
        mConstellationTool = tool;

        if (tool == null) {
            mHasSetConstellationTool = false;
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.constellation.tool.clear", null, null);
            return;
        }

        mHasSetConstellationTool = true;
        TXZService.setCommandProcessor("tool.constellation.", new TXZService.CommandProcessor() {

            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                if (TextUtils.isEmpty(command)) {
                    return null;
                }
                final ConstellationTool tool = mConstellationTool;
                if (tool == null) {
                    return null;
                }
                if (command.equals("push.show")) {
                    tool.onShowPush(new String(data));
                }
                return null;
            }
        });
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.constellation.tool.set", null, null);
    }
}
