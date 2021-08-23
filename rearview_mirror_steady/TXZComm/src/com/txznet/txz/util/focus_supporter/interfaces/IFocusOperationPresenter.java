package com.txznet.txz.util.focus_supporter.interfaces;

/**
 * 自己处理导航按键操作的View需要继承此接口
 * 导航按键事件会优先被传递到该View进行处理
 * Created by J on 16/10/30.
 */
public interface IFocusOperationPresenter {
	
    /**
     * 响应导航按键事件
     * @param operation 操作码
     * @return 是否消费事件
     */
    boolean onNavOperation(int operation);
}
