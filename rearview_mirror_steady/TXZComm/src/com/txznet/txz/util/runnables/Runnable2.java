package com.txznet.txz.util.runnables;

public abstract class Runnable2<T1, T2> implements Runnable {
	protected T1 mP1;
	protected T2 mP2;

	public Runnable2(T1 p1, T2 p2) {
		mP1 = p1;
		mP2 = p2;
	}

	public void update(T1 p1, T2 p2) {
		mP1 = p1;
		mP2 = p2;
	}
}
