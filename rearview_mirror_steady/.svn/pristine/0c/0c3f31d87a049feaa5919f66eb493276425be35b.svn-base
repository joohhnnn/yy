package com.txznet.sdk;

import com.txznet.comm.remote.ServiceManager;

import java.util.LinkedList;
import java.util.List;

/**
 * 电源管理器
 */
public class TXZPowerManager {
    private static TXZPowerManager sInstance = new TXZPowerManager();

    private TXZPowerManager() {

    }

    /**
     * 获取单例
     *
     * @return
     */
    public static TXZPowerManager getInstance() {
        return sInstance;
    }

    /**
     * 重连时需要重新通知同行者的操作放这里
     */
    void onReconnectTXZ() {

    }

    /**
     * 电源行为
     */
    public enum PowerAction {
        /**
         * 汽车打火
         */
        POWER_ACTION_POWER_ON,
        /**
         * 将要休眠(提前10秒)
         */
        POWER_ACTION_BEFORE_SLEEP,
        /**
         * 休眠
         */
        POWER_ACTION_SLEEP,
        /**
         * 唤醒
         */
        POWER_ACTION_WAKEUP,
        /**
         * 震动唤醒
         */
        POWER_ACTION_SHOCK_WAKEUP,
        /**
         * 进入倒车
         */
        POWER_ACTION_ENTER_REVERSE,
        /**
         * 退出倒车
         */
        POWER_ACTION_QUIT_REVERSE,
        /**
         * 将要关机
         */
        POWER_ACTION_BEFORE_POWER_OFF,
        /**
         * 汽车熄火
         */
        POWER_ACTION_POWER_OFF
    }

    /**
     * 通知电源状态变更
     *
     * @param action 电源行为
     */
    public void notifyPowerAction(PowerAction action) {
        if (action != null) {
            switch (action) {
                case POWER_ACTION_ENTER_REVERSE:
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                            "comm.power.ENTER_REVERSE", null, null);
                    break;
                case POWER_ACTION_QUIT_REVERSE:
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                            "comm.power.QUIT_REVERSE", null, null);
                    break;
                case POWER_ACTION_BEFORE_POWER_OFF:
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                            "comm.power.BEFORE_POWER_OFF", null, null);
                    break;
                case POWER_ACTION_BEFORE_SLEEP:
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                            "comm.power.BEFORE_SLEEP", null, null);
                    break;
                case POWER_ACTION_POWER_OFF:
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                            "comm.power.POWER_OFF", null, null);
                    break;
                case POWER_ACTION_POWER_ON:
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                            "comm.power.POWER_ON", null, null);
                    break;
                case POWER_ACTION_SLEEP:
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                            "comm.power.SLEEP", null, null);
                    break;
                case POWER_ACTION_WAKEUP:
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                            "comm.power.WAKEUP", null, null);
                    break;
                case POWER_ACTION_SHOCK_WAKEUP:
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                            "comm.power.SHOCK_WAKEUP", null, null);
                    break;
                default:
                    break;
            }
        }
    }

    static Boolean mReleased = null;

    /**
     * 销毁txz资源
     */
    public void releaseTXZ() {
        synchronized (TXZPowerManager.class) {
            ServiceManager.getInstance().mDisableSendInvoke = false;
            ServiceManager.getInstance().releaseAllConnectionExcludeTXZ();
            mReleased = true;
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "comm.exitTXZ", null, null);
    }

    /**
     * 退出TXZ后重新初始化
     */
    public void reinitTXZ() {
        synchronized (TXZPowerManager.class) {
            mReleased = false;
            TXZService.mTXZHasExited = false;
            ServiceManager.getInstance().mDisableSendInvoke = false;
        }
        TXZConfigManager.getInstance().initializeSDK();
    }

    /**
     * 退出TXZ后重新初始化
     *
     * @param onSucc 初始化完成后执行回调
     */
    public void reinitTXZ(Runnable onSucc) {
        if (mReinitTXZCallbacks == null) {
            mReinitTXZCallbacks = new LinkedList<Runnable>();
        }
        synchronized (mReinitTXZCallbacks) {
            mReinitTXZCallbacks.add(onSucc);
        }
        reinitTXZ();
    }

    private List<Runnable> mReinitTXZCallbacks;

    void notifyReInitFinished() {
        if (mReinitTXZCallbacks != null && !mReinitTXZCallbacks.isEmpty()) {
            synchronized (mReinitTXZCallbacks) {
                for (Runnable callback : mReinitTXZCallbacks) {
                    ServiceManager.getInstance().runOnServiceThread(callback, 0);
                }
                mReinitTXZCallbacks.clear();
            }
        }
    }
}
