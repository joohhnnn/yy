package com.txznet.txz.component.choice.option;

import com.txznet.txz.component.choice.ListHook;

/**
 * 带钩子的参数
 * 
 * @param <V>
 */
public class ListHookCompentOption<V> extends CompentOption<V> {
	private ListHook<V> hook;

	public ListHook<V> getHook() {
		return hook;
	}

	public void setHook(ListHook<V> hook) {
		this.hook = hook;
	}
}