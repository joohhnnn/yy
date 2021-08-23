package com.txznet.sdk;


import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;

/**
 * 提醒管理类
 * 设置提醒工具，自定义提醒弹窗样式
 */
public class TXZReminderManager {

    private static TXZReminderManager mInstance = new TXZReminderManager();
    private ReminderTool mReminderTool;
    private boolean mHasSetReminderTool;


    private TXZReminderManager() {

    }

    /**
     * 获取单例
     *
     * @return
     */
    public static TXZReminderManager getInstance() {
        return mInstance;
    }

    public void onReconnectTXZ() {
        if (mHasSetReminderTool) {
            setReminderTool(mReminderTool);
        }
        if (mEnableReminder != null) {
            enableReminder(mEnableReminder);
        }
        if(mCloseTimeView != null){
            closeTimeView(mCloseTimeView);
        }
    }

    public static interface ReminderTool {
        /**
         * 需要展示提醒弹窗
         * @param json json格式的弹窗内容，内含两个字段“text”和“nav”，text是提示的内容，nav是是否支持导航
         *             {"nav":false,"text":"你有1则提醒事项：起床"}
         */
        void onShowPush(String json);
    }

    /**
     * 导航到提醒附带的地址，只有一个地址时生效
     */
    public void navToReminder() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.reminder.push.nav", null, null);
    }

    private Boolean mCloseTimeView = null;

    /**
     * 关闭到钟提醒的弹窗，true为关闭，同时不再打开，需要重新设置为false
     */
    public void closeTimeView(boolean closeTimeView) {
        mCloseTimeView = closeTimeView;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.reminder.closeView", (mCloseTimeView + "").getBytes(), null);
    }

    /**
     * 设置提醒工具
     * @param tool
     * 使用时需要自己添加唤醒词“导航过去”
     */
    public void setReminderTool(ReminderTool tool) {
        mReminderTool = tool;

        if (tool == null) {
            mHasSetReminderTool = false;
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.reminder.tool.clear", null, null);
            return;
        }

        mHasSetReminderTool = true;
        TXZService.setCommandProcessor("tool.reminder.", new TXZService.CommandProcessor() {

            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                if (TextUtils.isEmpty(command)) {
                    return null;
                }
                final ReminderTool tool = mReminderTool;
                if (tool == null) {
                    return null;
                }
                if (command.equals("push.show")) {
                    tool.onShowPush(new String(data));
                }
                return null;
            }
        });
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.reminder.tool.set", null, null);
    }

    private Boolean mEnableReminder = null;
    /**
     * 设置可使用提醒功能，使用适配自定义ui时，需要调用该接口来启用提醒功能，默认不开启
     * @param enable 是否启用提醒功能，不设置，则使用Core默认的逻辑<br>
     * true：启用提醒功能<br>
     * false：关闭提醒功能<br>
     */
    public void enableReminder(boolean enable) {
        mEnableReminder = enable;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.reminder.set.enable", (mEnableReminder + "").getBytes(), null);
    }
}
