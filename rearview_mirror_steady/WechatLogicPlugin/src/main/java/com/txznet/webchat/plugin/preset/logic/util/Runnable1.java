package com.txznet.webchat.plugin.preset.logic.util;

public abstract class Runnable1<T1> implements Runnable {
    protected T1 mP1;

	public Runnable1(T1 p1) {
		mP1 = p1;
	}
	
	public void update(T1 p1) {
		mP1 = p1;
	}
}