package com.txznet.music.helper;

/**
 * 推送帮助类
 *
 * @author telen
 * @date 2019/1/28,15:53
 */
public class PushLogicHelper {

    /**
     * 单例对象
     */
    private volatile static PushLogicHelper singleton;

    private PushLogicHelper() {
    }

    public static PushLogicHelper getInstance() {
        if (singleton == null) {
            synchronized (PushLogicHelper.class) {
                if (singleton == null) {
                    singleton = new PushLogicHelper();
                }
            }
        }
        return singleton;
    }

    /**
     * 界面是否打开过【判断标准是：app从进入界面开始到点击退出音乐为止】
     */
    private boolean isOpened = false;
    /**
     * 当前进程是否已经执行了弹出推送界面的操作【判断标准：从Core启动开始到Core死亡为止，期间只能弹一次】
     */
    private boolean isShowViewExecutable = false;


    public void setAppOpened(boolean isOpen) {
        isOpened = isOpen;
    }

    public boolean isAppOpened() {
        return isOpened;
    }

    public boolean isShowViewExecutable() {
        return isShowViewExecutable;
    }

    public void setShowViewExecutable(boolean executable) {
        isShowViewExecutable = executable;
    }
}
