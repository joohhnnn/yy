package com.txznet.webchat.comm.plugin.utils;

import com.txznet.loader.AppLogicBase;

/**
 * Created by J on 2017/7/10.
 */

public class PluginTaskRunner {
    public static final String INVOKE_CMD_RUN_TASK = "wx.cmd.run_task";
    public static final String INVOKE_CMD_REMOVE_TASK = "wx.cmd.remove_task";

    public static final int TASK_TYPE_UI_GROUND = 0;
    public static final int TASK_TYPE_BACK_GROUND = 1;
    public static final int TASK_TYPE_SLOW_GROUND = 2;

    public static void runOnUiGround(Runnable runnable) {
        runOnUiGround(runnable, 0);
    }

    public static void runOnUiGround(Runnable runnable, int delay) {
        //PluginManager.invoke(INVOKE_CMD_RUN_TASK, TASK_TYPE_UI_GROUND, runnable, delay);
        AppLogicBase.runOnUiGround(runnable, delay);
    }

    public static void removeUiGroundCallback(Runnable runnable) {
        //PluginManager.invoke(INVOKE_CMD_REMOVE_TASK, TASK_TYPE_UI_GROUND, runnable);
        AppLogicBase.removeUiGroundCallback(runnable);
    }

    public static void runOnBackGround(Runnable runnable) {
        runOnBackGround(runnable, 0);
    }

    public static void runOnBackGround(Runnable runnable, int delay) {
        //PluginManager.invoke(INVOKE_CMD_RUN_TASK, TASK_TYPE_BACK_GROUND, runnable, delay);
        AppLogicBase.runOnBackGround(runnable, delay);
    }

    public static void removeBackGroundCallback(Runnable runnable) {
        //PluginManager.invoke(INVOKE_CMD_REMOVE_TASK, TASK_TYPE_BACK_GROUND, runnable);
        AppLogicBase.removeBackGroundCallback(runnable);
    }

    public static void runOnSlowGround(Runnable runnable) {
        runOnSlowGround(runnable, 0);
    }

    public static void runOnSlowGround(Runnable runnable, int delay) {
        //PluginManager.invoke(INVOKE_CMD_RUN_TASK, TASK_TYPE_SLOW_GROUND, runnable, delay);
        AppLogicBase.runOnSlowGround(runnable, delay);
    }

    public static void removeSlowGroundCallback(Runnable runnable) {
        //PluginManager.invoke(INVOKE_CMD_REMOVE_TASK, TASK_TYPE_SLOW_GROUND, runnable);
        AppLogicBase.removeSlowGroundCallback(runnable);
    }

}
