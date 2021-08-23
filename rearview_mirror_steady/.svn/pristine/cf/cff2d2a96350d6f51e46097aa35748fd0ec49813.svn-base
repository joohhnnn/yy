package com.txznet.comm.base;

import android.app.Activity;

import com.txznet.loader.AppLogicBase;

import java.util.NoSuchElementException;
import java.util.Stack;

public class ActivityStack {
    private Stack<Activity> stack;
    private static ActivityStack instance;
    private static int sForegroundActivityCount = 0; // 前台Activity数目，用于检测应用前后台变化

    private ActivityStack() {
        stack = new Stack<Activity>();
    }

    public static ActivityStack getInstance() {
        if (instance == null) {
            synchronized (ActivityStack.class) {
                if (instance == null) {
                    instance = new ActivityStack();
                }
            }
        }
        return instance;
    }

    public void pop(Activity activity) {
        if (activity != null) {
            stack.remove(activity);
        }
    }

    public void push(Activity activity) {
        stack.add(activity);
    }

    public boolean has() {
        return stack.size() > 0;
    }

    public void exit() {
        for (int i = 0; i < stack.size(); i++) {
            Activity activity = stack.get(i);
            stack.remove(i);
            i--;
            if (activity != null) {
                activity.finish();
                activity = null;
            }
        }
    }

    public void popAllActivityExceptOne(Class cls) {
        while (true) {
            Activity activity = currentActivity();
            if (activity == null) {
                break;
            }
            if (activity.getClass().equals(cls)) {
                break;
            }
            popActivity();
        }
    }

    public void popActivity() {
        Activity activity = stack.lastElement();
        if (activity != null) {
            stack.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    public Activity currentActivity() {
        Activity activity = null;
        try {
            activity = stack.lastElement();
        } catch (NoSuchElementException e) {
        }
        return activity;
    }

    /**
     * 新Activity进入前台时调用此方法计数
     * 正常情况下在onStart()调用
     */
    public void pushForeground() {
        sForegroundActivityCount++;

        if (1 == sForegroundActivityCount) {
            notifyAppVisiblityChanged(true);
        }
    }

    /**
     * Activity进入后台时调用此方法计数
     * 正常情况下在onStop()调用
     */
    public void popForeground() {
        sForegroundActivityCount--;

        if (0 == sForegroundActivityCount) {
            notifyAppVisiblityChanged(false);
        }
    }

    public int getsForegroundActivityCount() {
        return sForegroundActivityCount;
    }

    private void notifyAppVisiblityChanged(final boolean visible) {
        AppLogicBase.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                BaseApplication.callAppLogicMethod("onVisibilityChanged", boolean.class, visible);
            }
        });

    }

    public int getSize() {
        int size = 0;
        if (stack != null) {
            size = stack.size();
        }
        return size;
    }
}
