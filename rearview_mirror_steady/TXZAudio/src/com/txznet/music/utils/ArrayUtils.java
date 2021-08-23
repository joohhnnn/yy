/**
 * 
 */
package com.txznet.music.utils;

/**
 * @desc <pre></pre>
 * @author Erich Lee
 * @Date Mar 21, 2013
 */
public class ArrayUtils {

	public static boolean isEmpty(byte[] array) {
		if (array == null) {
			return true;
		}
		if (array.length == 0) {
			return true;
		}
		return false;
	}

	public static boolean isEmpty(String[] array) {
		if (array == null) {
			return true;
		}
		if (array.length == 0) {
			return true;
		}
		return false;
	}

	public static <T> boolean isEmpty(T[] array) {
		if (array == null) {
			return true;
		}
		if (array.length == 0) {
			return true;
		}
		return false;
	}
	
	public static <T> boolean isEmpty(int[] array) {
		if (array == null) {
			return true;
		}
		if (array.length == 0) {
			return true;
		}
		return false;
	}

	public static <T> boolean contains(T[] array, T obj) {
		if (isEmpty(array)) {
			return false;
		}
		if (obj == null) {
			return false;
		}
		for (T t : array) {
			if (t.equals(obj)) {
				return true;
			}
		}
		return false;
	}

	public static Integer[] toObject(int[] array) {
		if (array == null || array.length == 0) {
			return new Integer[0];
		}
		Integer[] r = new Integer[array.length];
		int i = 0;
		for (int item : array) {
			r[i++] = Integer.valueOf(item);
		}
		return r;
	}

	public static byte[] append(byte[] first, byte[] end) {
		if (first == null || end == null) {
			return null;
		}
		byte[] data = new byte[first.length + end.length];
		System.arraycopy(first, 0, data, 0, first.length);
		System.arraycopy(end, 0, data, first.length, end.length);
		return data;
	}

	public static String getSpStrFromIntegerArray(int[] ints) {
		if (isEmpty(ints)) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		int size = ints.length;
		int i = 0;
		for (int value : ints) {
			sb.append(value);
			if (i < size - 1) {
				sb.append(",");
			}
			i++;
		}
		return sb.toString();

	}

}
