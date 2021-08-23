package com.txznet.tts.ui;

import java.util.LinkedList;

import android.app.Activity;

public class ActivityManager {
	private static LinkedList<Activity> sActivityStack = new LinkedList<Activity>();

	public static void push(Activity activity) {
		sActivityStack.add(activity);
	}

	public static void pop() {
		if (!sActivityStack.isEmpty()) {
			sActivityStack.remove(sActivityStack.size() - 1);
		}

	}

	public static Activity top() {
		Activity activity = null;
		if (!sActivityStack.isEmpty()) {
			activity = sActivityStack.get(sActivityStack.size() - 1);
		}
		return activity;
	}

}
