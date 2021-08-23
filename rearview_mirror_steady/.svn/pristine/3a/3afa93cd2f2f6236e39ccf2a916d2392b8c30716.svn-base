package com.txznet.comm.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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

    public static int getMinu(long time) {
        CALENDAR.setTimeInMillis(time);
        int minute = CALENDAR.get(Calendar.MINUTE);
        return minute;
    }

    public static boolean isNight() {
		long curMillis = System.currentTimeMillis();
        int hour = DateUtils.getHour(curMillis);
        int minu = DateUtils.getMinu(curMillis);
        if (hour >= 6 && hour < 18) { // 白天
            if (hour == 6) {
                if (minu > 30) {
                    return false;
                } else {
                    return true;
                }
            }
            return false;
        } else { // 夜晚
            if (hour == 18) {
                if (minu > 30) {
                    return true;
                } else {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * 1、早上 2、中午 3、下午 4、晚上
     *
     * @return
     */
    public static int getDayTimeQuantum() {
        long curMillis = System.currentTimeMillis();
        int hour = getHour(curMillis);
        int minu = getMinu(curMillis);
        if (hour >= 6 && hour < 19) { // 白天
            if (hour < 11) {
                return 1;
            } else if (hour == 11) {
                if (minu < 30) {
                    return 1;
                } else {
                    return 2;
                }
            } else { // 大于11点
                if (hour < 14) {
                    return 2;
                } else if (hour == 14) {
                    if (minu < 30) {
                        return 2;
                    } else {
                        return 3;
                    }
                } else {
                    if (hour == 18) {
                        if (minu < 30) {
                            return 3;
                        } else {
                            return 4;
                        }
                    } else {
                        return 3;
                    }
                }
            }
        } else { // 夜晚
            return 4;
        }
    }

    /**
     * 获取一天时间段的说法
     * @return
     */
    public static String getTimeQuantum() {
        String quantumStr = "早上";
        int quantum = getDayTimeQuantum();
        switch (quantum) {
            case 1:
                quantumStr = "早上";
                break;
            case 2:
                quantumStr = "中午";
                break;
            case 3:
                quantumStr = "下午";
                break;
            case 4:
                quantumStr = "晚上";
                break;
        }
        return quantumStr;
    }

    public static String getSimpleDateStr() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(CALENDAR.getTime());
    }

    public static String getAMPMTimeStr() {
        long time = System.currentTimeMillis();
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        int hour = mCalendar.get(Calendar.HOUR);
        int minu = mCalendar.get(Calendar.MINUTE);
        int apm = mCalendar.get(Calendar.AM_PM);
        String h = hour < 10 ? "0" + hour : hour + "";
        String mi = minu < 10 ? "0" + minu : minu + "";
        String m = apm == 0 ? "上午" : "下午";
        return h + ":" + mi + " " + m;
    }

    public static String getHMTimeStr() {
        long time = System.currentTimeMillis();
        final Calendar mCalendar = Calendar.getInstance();
        mCalendar.setTimeInMillis(time);
        mCalendar.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        int hour = mCalendar.get(Calendar.HOUR);
        int minu = mCalendar.get(Calendar.MINUTE);
        int apm = mCalendar.get(Calendar.AM_PM);
        String h = hour < 10 ? "0" + hour : hour + "";
        String mi = minu < 10 ? "0" + minu : minu + "";
        return h + ":" + mi;
    }

    private static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static String formatDate(long timeMillis) {
        return mDateFormat.format(new Date(timeMillis));
    }

    /**
     * 获取两个日期之间的间隔天数
     * @return
     */
    public static int getGapCount(Date startDate, Date endDate) {
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(startDate);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(endDate);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        return (int) ((toCalendar.getTime().getTime() - fromCalendar.getTime().getTime()) / (1000 * 60 * 60 * 24));
    }

    /**
     * 获取两个日期直接相隔的月数
     * @param startDate
     * @param endDate
     * @return
     */
    public static int getGapMonth(Date startDate, Date endDate){
        Calendar bef = Calendar.getInstance();
        Calendar aft = Calendar.getInstance();
        bef.setTime(startDate);
        aft.setTime(endDate);
        int result = aft.get(Calendar.MONTH) - bef.get(Calendar.MONTH);
        int month = (aft.get(Calendar.YEAR) - bef.get(Calendar.YEAR)) * 12;
        return Math.abs(month + result);
    }
    
    /**
     * 获取两个日期之间相隔的月数，包含半个月
     * 	0910 - 1011 -> 1.5
     * 	0910 - 1010 -> 1.0
     * 	0910 - 1009 -> 0.5
     * @param startDate
     * @param endDate
     * @return
     */
    public static float getGapMonthWithHalf(Date startDate, Date endDate) {
		Calendar bef = Calendar.getInstance();
		Calendar aft = Calendar.getInstance();
		bef.setTime(startDate);
		aft.setTime(endDate);
		int result = aft.get(Calendar.MONTH) - bef.get(Calendar.MONTH);
		int month = (aft.get(Calendar.YEAR) - bef.get(Calendar.YEAR)) * 12;
		float day = 0f;
		if(aft.get(Calendar.DAY_OF_MONTH) - bef.get(Calendar.DAY_OF_MONTH) > 0){
			day = 0.5f;
		}else if(aft.get(Calendar.DAY_OF_MONTH) - bef.get(Calendar.DAY_OF_MONTH) < 0){
			day = -0.5f;
		}
		return Math.abs(month + result + day);
	}

    /**
     * 获取日期相对于今天的说法
     * 前天，昨天，今天，明天，后天，超出范围返回 ""
     * @param time 毫秒
     * @return
     */
    public static String getDayQuantum(long time) {
        Calendar today = Calendar.getInstance();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //老的时间减去今天的时间
        long intervalMilli = calendar.getTimeInMillis() - today.getTimeInMillis();
        int xcts = (int) (intervalMilli / (24 * 60 * 60 * 1000));
        String ret = "";
        // -2:前天 -1：昨天 0：今天 1：明天 2：后天
        if (xcts >= -2 && xcts <= 2) {
            switch (xcts) {
                case -2:
                    ret = "(前天)";
                    break;
                case -1:
                    ret = "(昨天)";
                    break;
                case 0:
                    ret = "(今天)";
                    break;
                case 1:
                    ret = "(明天)";
                    break;
                case 2:
                    ret = "(后天)";
                    break;
            }
        }
        return ret;
    }

    /**
     * 判定两个日期的间隔是否超过一个自然周
     * 5月第一周周日   第二周周一  true
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return
     */
    public static boolean compareDateMoreThanOneWeek(Date startDate,Date endDate){
        final long day = 24 * 60 * 60 * 1000;
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        //周日则需要减一天，国外周日是一周的第一天
        if (cal.get(Calendar.DAY_OF_WEEK) == 1) {
            cal.setTimeInMillis(cal.getTimeInMillis() - day);
        }
        cal.set(cal.DAY_OF_WEEK, cal.MONDAY);
        cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        final long oneWeekTime = 7 * day;
        if (endDate.getTime() - cal.getTimeInMillis() > oneWeekTime) {
            return true;
        }
        return false;
    }

}