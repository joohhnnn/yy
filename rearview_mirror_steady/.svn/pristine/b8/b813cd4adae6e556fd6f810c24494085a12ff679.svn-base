package com.txznet.music.action;

import com.txznet.music.Constant;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

import java.util.List;

/**
 * 本地音乐操作行为
 *
 * @author zackzhou
 * @date 2018/12/3,10:59
 */
public class LocalActionCreator {

    private static LocalActionCreator sInstance = new LocalActionCreator();

    private LocalActionCreator() {
    }

    public static LocalActionCreator get() {
        return sInstance;
    }

    /**
     * 扫描
     */
    public void scan(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SCAN).operation(operation).build());
    }

    /**
     * 取消扫描
     */
    public void cancelScan(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_SCAN_CANCEL).operation(operation).build());
    }

    /**
     * 获取本地音乐列表
     */
    public void getLocalAudio(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_GET_LOCAL).operation(operation).build());
    }

    /**
     * 按时间排序
     */
    public void sortByTime(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_LOCAL_SORT_BY_TIME).operation(operation).build());
    }

    /**
     * 按名称排序
     */
    public void sortByName(Operation operation) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_LOCAL_SORT_BY_NAME).operation(operation).build());
    }

    /**
     * 删除本地音乐
     */
    public void deleteLocal(Operation operation, final List<LocalAudio> audioList) {
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_LOCAL_DELETE).operation(operation).bundle(Constant.LocalConstant.KEY_LOCAL_MUSIC_AUDIO, audioList).build());
    }
}
