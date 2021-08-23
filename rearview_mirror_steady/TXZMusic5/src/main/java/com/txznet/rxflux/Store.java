package com.txznet.rxflux;

import android.arch.lifecycle.ViewModel;
import android.support.annotation.MainThread;

import com.txznet.music.Constant;
import com.txznet.music.util.Logger;

import io.reactivex.disposables.Disposable;

/**
 * (不要执行代码格式化)
 * <p>
 * Store，系统中，关于某一块特定逻辑的数据临时存放中心
 * 采用Google AAC架构的Live通知数据刷新
 * <p>
 * 建议：
 * 数据分类三大类，
 * 1. Data(普通数据)
 * 2. Status(传给View的错误状态如验证错误、服务器错误等，一次post仅允许触发一次，建议使用{@link com.txznet.rxflux.extensions.aac.livedata.SingleLiveEvent})
 * e.g.
 * 建议定义成Store的内部类
 * enum class Status {
 * ${FUNCTION}_SUCCESS(Operation, Desc),
 * ${FUNCTION}_ERROR(Operation, Desc),
 * ${FUNCTION}_FAILED(Operation, Desc),
 * }
 * 3. Status UI状态，控制某中视图开关，如对话框是否显示
 * e.g.
 * 建议定义成Store的内部类
 * class State {
 * boolean isMusicPickerDialogShown = false;
 * }
 *
 * @author zackzhou
 */
public abstract class Store extends ViewModel {
    private static final String TAG = Constant.LOG_TAG_FLUX + ":Store";
    private Disposable mDisposable;

    protected Store() {
        register(getActionTypes());
    }

    protected abstract String[] getActionTypes();

    protected void register(String... actionType) {
        Dispatcher.get().register(this, actionType);
    }

    public void unRegister() {
        Logger.d(TAG, "unRegister, store=" + getClass().getSimpleName());
        setDisposable(null);
    }

    void setDisposable(Disposable disposable) {
        if (this.mDisposable != null && !mDisposable.isDisposed()) {
            this.mDisposable.dispose();
        }
        this.mDisposable = disposable;
    }

    public boolean isRegistered() {
        return mDisposable != null;
    }

    @MainThread
    void handleAction(RxAction action) {
        if (action.isError()) {
//            Logger.e(TAG, "handleError store=" + getTag() + "#" + this.hashCode() + ", action=" + action);
            onError(action, action.getThrowable());
        } else if (action.isData()) {
//            Logger.d(TAG, "handleData store=" + getTag() + "#" + this.hashCode() + ", action=" + action);
            onData(action);
        } else {
//            Logger.d(TAG, "handleAction store=" + getTag() + "#" + this.hashCode() + ", action=" + action);
            onAction(action);
        }
    }

    /**
     * UI行为处理
     *
     * @param action 行为
     */
    @MainThread
    protected abstract void onAction(RxAction action);

    /**
     * 数据回调处理，workflow处理成功时回调该方法
     *
     * @param action 行为
     */
    @MainThread
    protected abstract void onData(RxAction action);

    /**
     * 错误处理，workflow处理异常时回调该方法
     *
     * @param action    行为
     * @param throwable 异常信息
     */
    @MainThread
    protected abstract void onError(RxAction action, Throwable throwable);

    @Override
    protected void onCleared() {
        unRegister();
        Logger.d(TAG, "onCleared store=" + "#" + this.hashCode() + getClass().getSimpleName());
    }

    public String getTag() {
        return this.getClass().getSimpleName();
    }
}
