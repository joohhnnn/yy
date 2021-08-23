package com.txznet.music.util;


import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.ArrayMap;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.Constant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 监控进程内存变化
 * <p>
 * Created by Terry on 2017/8/22.
 */

public class ProcessMemoryMonitor {

    private static final String TAG = Constant.LOG_TAG_MONITOR;

    private ProcessMemoryMonitor() {
    }

    private static ProcessMemoryMonitor sInstance = new ProcessMemoryMonitor();

    public static ProcessMemoryMonitor getInstance() {
        return sInstance;
    }

    private Handler mHandlerMonitor;
    private HandlerThread mThreadMonitor;

    private List<Integer> mMonitorPids;
    private ActivityManager mActivityManager;
    private List<MonitorProcess> mMonitorProcesses = new ArrayList<>();
    private Map<String, Integer> mMapNamePid = new ArrayMap<>();
    private Map<Integer, String> mMapPidName = new ArrayMap<>();
    private static int INTERVAL_MONITOR = 20000;
    private final Object mLock = new Object();
    private boolean mIsInited = false;

    public void init() {
        if (mHandlerMonitor == null || mThreadMonitor == null) {
            mThreadMonitor = new HandlerThread("monitor");
            mThreadMonitor.start();
            mHandlerMonitor = new Handler(mThreadMonitor.getLooper());
        }
        mHandlerMonitor.removeCallbacks(mTaskMonitor);
        mHandlerMonitor.postDelayed(mTaskMonitor, INTERVAL_MONITOR);
    }

    public Debug.MemoryInfo getMemoryInfo(String packageName) {
        int pid = getRunningPid(packageName);
        if (pid == 0) {
            Logger.w(TAG, " get process pid failed!");
            return null;
        }
        int[] pids = new int[]{pid};
        Debug.MemoryInfo[] memoryInfoArray = mActivityManager.getProcessMemoryInfo(pids);
        Logger.d(TAG, " start print meminfo");
        if (memoryInfoArray != null && memoryInfoArray.length >= 1) {
            return memoryInfoArray[0];
        }
        Logger.e(TAG, "get process memory failed!");
        return null;
    }

    public void addMonitorProcess(MonitorProcess monitorProcess) {
        if (monitorProcess == null || TextUtils.isEmpty(monitorProcess.mProcessName)) {
            Logger.e(TAG, "processName can't be null!");
            return;
        }
        if (!mIsInited) {
            init();
            mIsInited = true;
        }
        synchronized (mLock) {
            mMonitorProcesses.add(monitorProcess);
            mMapNamePid.put(monitorProcess.mProcessName, 0);
        }
    }

    public void removeMonitorProcess(MonitorProcess monitorProcess) {
        synchronized (mLock) {
            mMonitorProcesses.remove(monitorProcess);
        }
    }

    private int getRunningPid(String processName) {
        if (mActivityManager == null) {
            mActivityManager = (ActivityManager) GlobalContext.get().getSystemService(Context.ACTIVITY_SERVICE);
        }
        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos = mActivityManager.getRunningAppProcesses();
        if (runningAppProcessInfos != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : runningAppProcessInfos) {
                if (processName.equals(runningAppProcessInfo.processName)) {
                    return runningAppProcessInfo.pid;
                }
            }
        }
        return 0;
    }

    public void updatePid() {
        synchronized (mLock) {
            mMonitorPids = new ArrayList<>();
            mMapPidName = new ArrayMap<>();
            for (String key : mMapNamePid.keySet()) {
                int pid = getRunningPid(key);
                mMapNamePid.put(key, pid);
                if (pid != 0) {
                    mMonitorPids.add(pid);
                    mMapPidName.put(pid, key);
                }
            }
        }
    }


    private Runnable mTaskMonitor = new Runnable() {
        @Override
        public void run() {
            mHandlerMonitor.removeCallbacks(mTaskMonitor);
            synchronized (mLock) {
                updatePid();
                int[] pids = new int[mMonitorPids.size()];
                for (int i = 0; i < mMonitorPids.size(); i++) {
                    pids[i] = mMonitorPids.get(i);
                }
                Debug.MemoryInfo[] memoryInfoArray = mActivityManager.getProcessMemoryInfo(pids);
                Logger.d(TAG, " start print meminfo");
                for (int i = 0; i < memoryInfoArray.length; i++) {
                    Debug.MemoryInfo info = memoryInfoArray[i];
                    String processName = mMapPidName.get(pids[i]);
                    notifyMemoryInfo(processName, info);
                    Logger.d(TAG, "process:" + pids[i] + processName +
                            " totalPss:" + info.getTotalPss());
                }
            }
            mHandlerMonitor.postDelayed(mTaskMonitor, INTERVAL_MONITOR);
        }
    };

    private void notifyMemoryInfo(String packageName, Debug.MemoryInfo info) {
        for (MonitorProcess monitorProcess : mMonitorProcesses) {
            if (monitorProcess.mProcessName.equals(packageName)) {
                if (monitorProcess.mCallBack != null) {
                    monitorProcess.mCallBack.onMemoryChange(info);
                }
            }
        }
    }

    public static class MonitorProcess {

        public String mProcessName;

        public MonitorCallBack mCallBack;

        /**
         * @param processName 进程名
         * @param callBack    数据监测回调
         */
        public MonitorProcess(String processName, MonitorCallBack callBack) {
            this.mProcessName = processName;
            this.mCallBack = callBack;
        }
    }

    public interface MonitorCallBack {
        void onMemoryChange(Debug.MemoryInfo info);
    }
}
