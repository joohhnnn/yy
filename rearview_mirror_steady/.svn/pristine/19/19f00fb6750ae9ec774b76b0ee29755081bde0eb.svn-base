package com.txznet.txz;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.component.nav.baidu.auto.AutoControlInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TXZ-METEORLUO on 2018/10/27.
 */

public class BDEntryService extends Service {
    private static final String TAG = BDEntryService.class.getName();
    private static final String RESPOND_ACTION =
            "com.baidu.baidumaps.opencontrol.ACTION.RESPOND";
    private static final String INTENT_KEY_CONTENT = "content";

    // 消息标识
    private static final String INTENT_KEY_TRANSACTION = "transaction";

    // 调用方法
    private static final String RESPOND_KEY_METHOD = "method";
    // 调用结果
    private static final String RESPOND_KEY_STATUS = "status";
    // 返回内容
    private static final String RESPOND_KEY_RESULT = "result";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && TextUtils.equals(intent.getAction(), RESPOND_ACTION)) {
            showRespond(intent.getExtras());
        }
        return START_NOT_STICKY;
    }

    private void showRespond(Bundle extras) {
        String transaction = extras.getString(INTENT_KEY_TRANSACTION);
        String content = extras.getString(INTENT_KEY_CONTENT);
        try {
            JSONObject contentJson = new JSONObject(content);
            String method = contentJson.getString(RESPOND_KEY_METHOD);
            String status = contentJson.getString(RESPOND_KEY_STATUS);
            String content1 = contentJson.getString(RESPOND_KEY_RESULT);
            String out = "Service Respond: \n{\n     method:   " + method + "\n     status:   " + status
                    + " \n     result:   " + content1 + "\n     transaction:   " + transaction + "\n}";
            LogUtil.logd("showRespond:" + out);
            onResult(transaction, method, status, content1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onResult(String transaction, String method, String status, String result) {
        for (Task task : taskList) {
            if (task.transaction.equals(transaction)) {
                task.callback.onCallResult(result);
                taskList.remove(task);
                break;
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static List<Task> taskList = new ArrayList<Task>();

    public static void addTaskRunnable(Task task) {
        if (taskList == null) {
            taskList = new ArrayList<Task>();
        }
        taskList.add(task);
    }

    public static class Task {
        public String transaction;
        public TaskCallback callback;
    }

    public static interface TaskCallback {
        void onCallResult(String result);
    }
}