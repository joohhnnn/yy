package com.txznet.rxflux;

import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.txznet.loader.AppLogic;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.util.Logger;
import com.txznet.proxy.util.TimeUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Dispatcher
 * 事件分发器，全局唯一的，负责注册Flow和Store，控制内部事件分发
 * View负责postAction发送操作行为
 * Flow负责解析并作出业务处理，随后postData/postError发送处理结果
 *
 * @author zackzhou
 */
public class Dispatcher {
    private final String TAG = Constant.LOG_TAG_FLUX + ":Dispatcher";
    private final RxBus mBus;
    private List<RxWorkflow> mWorkFlows;

    private static class Holder {
        private static final Dispatcher DISPATCHER = new Dispatcher(new RxBus());
    }

    private Dispatcher(RxBus bus) {
        mBus = bus;
    }

    public static Dispatcher get() {
        return Holder.DISPATCHER;
    }

    /**
     * 注册工作流控件
     *
     * @param workflows 工作流控件
     */
    @MainThread
    void register(final RxWorkflow... workflows) {
        if (mWorkFlows == null) {
            mWorkFlows = new ArrayList<>(workflows.length);
        }
        for (RxWorkflow workflow : workflows) {
            Logger.d(TAG, "register, workflow=" + workflow.getTag());
            mWorkFlows.add(workflow);
        }
    }

    /**
     * 根据指定行为类型注册Store组件
     */
    @MainThread
    public void register(final Store store, final String... actionTypes) {
        Logger.d(TAG, "register, store=" + store.getTag() + ", actionTypes=" + (actionTypes == null ? null : Arrays.toString(actionTypes)));
        store.setDisposable(mBus.toObservable(RxAction.class)
                .filter(action -> {
//                    if (null == actionTypes || actionTypes.length == 0) {
//                        return true;
//                    }
//                    for (String type : actionTypes) {
//                        if (TextUtils.equals(type, action.type)) {
//                            return true;
//                        }
//                    }
//                    return false;

                    // FIXME: 2019/7/10 switch比if更高效
                    return true;
                }).subscribe(action -> {
                    try {
                        store.handleAction(action);
                    } catch (Exception e) {
                        Logger.e(TAG, "handle action error, flow=" + store.getClass().getSimpleName() + ", action=" + action);
                        e.printStackTrace();
                    }
                }));
    }

    /**
     * 发送一个UI行为操作
     */
    @MainThread
    public void postAction(@NonNull RxAction action) {
        PostActionInMainThread post = new PostActionInMainThread(action);
        if (Looper.myLooper() != Looper.getMainLooper()) {
            AppLogic.runOnUiGround(post);
        } else {
            post.run();
        }
    }

    private class PostActionInMainThread implements Runnable {
        RxAction action;

        public PostActionInMainThread(RxAction action) {
            this.action = action;
        }

        @Override
        public void run() {
            if (BuildConfig.DEBUG) {
                if (action != null && !ActionType.ACTION_PLAYER_ON_PROGRESS_CHANGE.equals(action.type)) {
                    Logger.d(TAG, "postAction action=" + action);
                }
                TimeUtils.startTime("postAction " + action);
            } else {
                Logger.d(TAG, "postAction action=" + action);
            }
            boolean hasIntercept = false;
            if (mWorkFlows != null) {
                for (RxWorkflow workflow : mWorkFlows) {
                    if (workflow.handleAction(action)) {
                        hasIntercept = true;
                        break;
                    }
                }
            }
            if (!hasIntercept) {
                mBus.post(action);
            }

            if (BuildConfig.DEBUG) {
                TimeUtils.endTime("postAction " + action);
            }

//            try {
//                RxActionPool.getPool().returnObject(action.type, action);
//                Logger.d("Pool", "returnObject ->" + action);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
        }
    }

    /**
     * 响应数据
     */
    @MainThread
    void postData(@NonNull RxAction action) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (BuildConfig.DEBUG) {
                TimeUtils.startTime("postData " + action);
            }
            Logger.d(TAG, "postData action=" + action);
            mBus.post(action.markData());
            if (BuildConfig.DEBUG) {
                TimeUtils.endTime("postData " + action);
            }
        } else {
            AppLogic.runOnUiGround(() -> postData(action));
        }
    }

    /**
     * 响应处理异常
     */
    @MainThread
    void postError(@NonNull RxAction action, @Nullable Throwable throwable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (BuildConfig.DEBUG) {
                TimeUtils.startTime("postError " + action);
            }
            Logger.d(TAG, "postError action=" + action);
            mBus.post(action.markError(throwable));
            if (BuildConfig.DEBUG) {
                TimeUtils.endTime("postError " + action);
            }
        } else {
            AppLogic.runOnUiGround(() -> postError(action, throwable));
        }
    }

    private void checkPostActionThreadSafety() {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalThreadStateException("You must call postAction method on main thread!");
        }
    }
}
