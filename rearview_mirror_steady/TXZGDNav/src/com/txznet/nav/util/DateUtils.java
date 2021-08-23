package com.txznet.nav.util;

import java.util.Calendar;

/**
 * 
 */
public class DateUtils {
	private static final Calendar CALENDAR = Calendar.getInstance();

	public static int getHour(long time) {
		CALENDAR.setTimeInMillis(time);
		int hour = CALENDAR.get(Calendar.HOUR_OF_DAY);
		return hour;
	}

	public static boolean isNight() {
		int hour = DateUtils.getHour(System.currentTimeMillis());
		if (hour >= 6 && hour < 18) { // 白天
			return false;
		} else { // 夜晚
			return true;
		}
	}
}