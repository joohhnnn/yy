/**
 * 
 */
package com.txznet.music.utils;

import java.lang.reflect.Method;
import java.util.Locale;

import android.util.Log;

/**
 * @desc <pre></pre>
 * @author Erich Lee
 * @Date Mar 29, 2013
 */
public class BeanUtils {
	private static final String TAG = "BeanUtils";

	private static final String GET = "get";
	private static final String SET = "set";

	public static Method getMethod(Class clazz, String fieldName, String prefix) {
		if (clazz == null || StringUtils.isEmpty(fieldName)) {
			return null;
		}
		String methodName = prefix.concat(fieldName.substring(0, 1).toUpperCase(Locale.US)
				.concat(fieldName.substring(1)));
		try {
			Method[] methods = clazz.getMethods();
			for (Method method : methods) {
				if (method.getName().equals(methodName)) {
					return method;
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return null;
	}
	
	public static Method[] getMethods(Class clazz) {
		if (clazz == null ) {
			return null;
		}
	
		try {
			Method[] methods = clazz.getMethods();
			return methods;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return null;
	}


	public static void copy(Object src, Object dest, String... excludeProperties) {
		if (src == null || dest == null) {
			return;
		}
		if (!src.getClass().equals(dest.getClass())) {
			return;
		}
		Class clazz = dest.getClass();
		Method[] methods = clazz.getMethods();
		String methodName;
		String fieldName;
		Method srcGetMethod;
		for (Method method : methods) {
			methodName = method.getName();
			if (methodName.startsWith(SET)) {
				fieldName = methodName.substring(SET.length());
				fieldName = fieldName.substring(0, 1).toLowerCase(Locale.US).concat(fieldName.substring(1));
				if (ArrayUtils.contains(excludeProperties, fieldName)) {
					continue;
				}
				srcGetMethod = getMethod(src.getClass(), fieldName, GET);
				if (srcGetMethod != null) {
					try {
						method.invoke(dest, srcGetMethod.invoke(src));
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
					}
				}
			}
		}
	}
}
