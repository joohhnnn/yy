package com.txznet.feedback.util;

import java.util.Comparator;

import com.txznet.feedback.data.Message;

public class TimeComparator implements Comparator<Message> {

	@Override
	public int compare(Message lhs, Message rhs) {
		if (lhs.time < rhs.time) {
			return -1;
		} else if (lhs.time > rhs.time) {
			return 1;
		}

		return 0;
	}
}