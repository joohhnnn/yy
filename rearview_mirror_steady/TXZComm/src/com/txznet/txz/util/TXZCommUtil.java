package com.txznet.txz.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import android.graphics.Color;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.text.TextUtils;

public class TXZCommUtil {
	//日期转成秒
	public static long dateToTime(String strDate, String strDateFormat) {
		long time = 0;
		try {
			SimpleDateFormat format = new SimpleDateFormat(strDateFormat);
			Date date = null;
			date = format.parse(strDate);
			time = date.getTime()/1000;
		} catch (Exception e) {
		}
		return time;
	}
	
	//秒转日期
	public static String timeToDate(long nTime, String strDateFormat) {
		String strDate = null;
		try {
			Date date = new Date();
			date.setTime(nTime * 1000);
			SimpleDateFormat dateFormat = new SimpleDateFormat(strDateFormat);
			strDate = dateFormat.format(date);
		} catch (Exception e) {

		}
		return strDate;
	}
	
	/**
	 * 更改已有颜色的alpha值
	 * @param alpha
	 * @param color
	 * @return
	 */
	public static int changeColorAlpha(float alpha, int color) {
		String hex = Integer.toHexString(color);
		if (TextUtils.isEmpty(hex) || hex.length() < 6) {
			throw new NumberFormatException();
		}
		String prefix = "#" + Integer.toHexString((int) (256 * alpha));
		return Color.parseColor(prefix + hex.substring(hex.length() - 6, hex.length()));
	}
	
	/**
	 * 判断某个Activity是否在前台
	 * @param context
	 * @param className
	 * @return
	 */
	public static boolean isActivityForeground(Context context, String className) {
		if (context == null || TextUtils.isEmpty(className)) {
			return false;
		}
		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> list = am.getRunningTasks(1);
		if (list != null && list.size() > 0) {
			ComponentName cpn = list.get(0).topActivity;
			if (className.equals(cpn.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * 获取application中指定的meta-data.
	 * @return 如果没有获取成功(没有对应值，或者异常)，则返回值为空
	 */
	public static String getAppMetaData(Context ctx, String key) {
		if (ctx == null || TextUtils.isEmpty(key)) {
			return null;
		}
		String resultData = null;
		try {
			PackageManager packageManager = ctx.getPackageManager();
			if (packageManager != null) {
				ApplicationInfo applicationInfo = packageManager.getApplicationInfo(ctx.getPackageName(), PackageManager.GET_META_DATA);
				if (applicationInfo != null) {
					if (applicationInfo.metaData != null) {
						resultData = applicationInfo.metaData.getString(key);
					}
				}

			}
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		return resultData;
	}

}
