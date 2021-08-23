package com.txznet.launcher.event;

/**
 * 用于业务层的观察者，监听一些业务逻辑的改变用于做一些事情
 * <p>观察者的{@link #onChange()}执行默认会在当前线程，如果是长时间的操作，请
 * 在实现{@link #onChange()}方法时使用{@link AsyncBackgroundTask}进行操作
 * @author meteorluo
 */
public abstract class EventObserver {
	/**
	 * 某个事件触发了。
	 * 这些事件分发都只是通知事件发生了，没有做数据的传递
	 */
	protected abstract  void onChange(String eventType);

	public final void dispatchChange(String eventType) {
	    onChange(eventType);
	}
}
