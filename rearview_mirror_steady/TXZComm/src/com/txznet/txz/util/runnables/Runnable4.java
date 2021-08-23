package com.txznet.txz.util.runnables;

public abstract class Runnable4<T1, T2, T3, T4> implements Runnable {
	protected T1 mP1;
	protected T2 mP2;
	protected T3 mP3;
	protected T4 mP4;

	public Runnable4(T1 p1, T2 p2, T3 p3, T4 p4) {
		mP1 = p1;
		mP2 = p2;
		mP3 = p3;
		mP4 = p4;
	}

	public void update(T1 p1, T2 p2, T3 p3, T4 p4) {
		mP1 = p1;
		mP2 = p2;
		mP3 = p3;
		mP4 = p4;
	}
}
