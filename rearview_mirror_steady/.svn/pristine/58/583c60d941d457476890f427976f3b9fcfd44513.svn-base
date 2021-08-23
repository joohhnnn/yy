package com.txznet.txz.component.nav.base;

import android.text.TextUtils;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.wakeup.WakeupManager;

import java.util.HashMap;
import java.util.Map;

public class SelectAsr extends AsrUtil.AsrComplexSelectCallback {
    public String taskId;
    Map<String, Runnable> keySelectRunMap;
    private boolean isSelectedClose = true;

    public SelectAsr setTaskId(String taskId) {
        this.taskId = taskId;
        return this;
    }

    public void addCmds(String type, Runnable selectRun, String... cmds) {
        if (cmds == null || cmds.length <= 0) {
            JNIHelper.logw("addCmds fail cmds is null！");
            return;
        }

        if (keySelectRunMap == null) {
            keySelectRunMap = new HashMap<String, Runnable>();
        }
        keySelectRunMap.put(type, selectRun);
        addCommand(type, cmds);
    }

    public void build() {
        WakeupManager.getInstance().useWakeupAsAsr(this);
    }

    public void onPause() {
        WakeupManager.getInstance().recoverWakeupFromAsr(getTaskId());
    }

    public void destory() {
        if (keySelectRunMap != null) {
            keySelectRunMap.clear();
        }
        if (!TextUtils.isEmpty(getTaskId())) {
            WakeupManager.getInstance().recoverWakeupFromAsr(getTaskId());
//				taskId = "";
        }
    }

    @Override
    public boolean needAsrState() {
        return false;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public void onCommandSelected(String type, String command) {
        Runnable selectRun = keySelectRunMap.get(type);
        if (isSelectedClose) {
            destory();
        }
        if (selectRun != null) {
            selectRun.run();
        }
    }

    public void isSelectedClose(boolean flag) {
        isSelectedClose = flag;
    }
}