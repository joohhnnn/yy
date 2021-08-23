package com.txznet.rxflux;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;

import com.txznet.music.Constant;

import io.reactivex.disposables.Disposable;

/**
 * 工作流控件Workflow
 * 处理行为的地方，耗时操作务必异步执行
 * 有且只有Workflow允许访问DataSource层接口和底层工具，
 * Workflow不建议互相访问(如遇到代码复用逻辑，建议抽取工具类)
 *
 * @author zackzhou
 */
public abstract class RxWorkflow {
    private static final String TAG = Constant.LOG_TAG_FLUX + ":Flow";
    private final Dispatcher mDispatcher;
    private final DisposableManager mDisposableManager;
    private boolean bIntercept;

    public RxWorkflow() {
        mDispatcher = Dispatcher.get();
        mDisposableManager = DisposableManager.get();
    }

    public void addRxAction(RxAction rxAction, Disposable disposable) {
        mDisposableManager.add(rxAction, disposable);
    }

    public boolean hasRxAction(RxAction rxAction) {
        return mDisposableManager.contains(rxAction);
    }

    public void removeRxAction(RxAction rxAction) {
        mDisposableManager.remove(rxAction);
    }

    @MainThread
    public void postRxData(@NonNull RxAction action) {
        mDispatcher.postData(action);
        removeRxAction(action);
    }

    @MainThread
    public void postRxError(@NonNull RxAction action, Throwable throwable) {
        if (throwable != null) {
            throwable.printStackTrace();
        }
        mDispatcher.postError(action, throwable);
        removeRxAction(action);
    }

    @MainThread
    boolean handleAction(RxAction action) {
//        Logger.d(TAG, "handleAction workflow=" + getTag() + "#" + this.hashCode() + ", action=" + action);
        bIntercept = false;
        onAction(action);
        return bIntercept;
    }

    /**
     * 拦截
     */
    public void intercept() {
        bIntercept = true;
    }

    /**
     * UI行为处理
     *
     * @param action 行为
     */
    @MainThread
    public abstract void onAction(RxAction action);

    public String getTag() {
        return this.getClass().getSimpleName();
    }
}
