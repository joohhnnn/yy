package com.txznet.txz.util;

public class LittleEndianBytesUtil {
	/**
	 * 将一个2位字节数组转换为char字符。<br>
	 * 注意，函数中不会对字节数组长度进行判断，请自行保证传入参数的正确性。
	 * 
	 * @param b
	 *            字节数组
	 * @return char字符
	 */
	public static char bytesToChar(byte[] b) {
		return bytesToChar(b, 0);
	}
	
	public static char bytesToChar(byte[] b, int offset) {
		char c = (char) ((b[offset+1] << 8) & 0xFF00L);
		c |= (char) (b[offset+0] & 0xFFL);
		return c;
	}

	/**
	 * 将一个8位字节数组转换为双精度浮点数。<br>
	 * 注意，函数中不会对字节数组长度进行判断，请自行保证传入参数的正确性。
	 * 
	 * @param b
	 *            字节数组
	 * @return 双精度浮点数
	 */
	public static double bytesToDouble(byte[] b) {
		return Double.longBitsToDouble(bytesToLong(b));
	}
	
	public static double bytesToDouble(byte[] b, int offset) {
		return Double.longBitsToDouble(bytesToLong(b, offset));
	}

	/**
	 * 将一个4位字节数组转换为浮点数。<br>
	 * 注意，函数中不会对字节数组长度进行判断，请自行保证传入参数的正确性。
	 * 
	 * @param b
	 *            字节数组
	 * @return 浮点数
	 */
	public static float bytesToFloat(byte[] b) {
		return Float.intBitsToFloat(bytesToInt(b, 0));
	}

	public static float bytesToFloat(byte[] b, int offset) {
		return Float.intBitsToFloat(bytesToInt(b, offset));
	}
	
	/**
	 * 将一个4位字节数组转换为4整数。<br>
	 * 注意，函数中不会对字节数组长度进行判断，请自行保证传入参数的正确性。
	 * 
	 * @param b
	 *            字节数组
	 * @return 整数
	 */
	public static int bytesToInt(byte[] b) {
		return bytesToInt(b, 0);
	}
	
	public static int bytesToInt(byte[] b, int offset) {
		int i = (b[offset+3] << 24) & 0xFF000000;
		i |= (b[offset+2] << 16) & 0xFF0000;
		i |= (b[offset+1] << 8) & 0xFF00;
		i |= b[offset+0] & 0xFF;
		return i;
	}

	/**
	 * 将一个8位字节数组转换为长整数。<br>
	 * 注意，函数中不会对字节数组长度进行判断，请自行保证传入参数的正确性。
	 * 
	 * @param b
	 *            字节数组
	 * @return 长整数
	 */
	public static long bytesToLong(byte[] b) {
		return bytesToLong(b, 0);
	}
	
	public static long bytesToLong(byte[] b, int offset) {
		long l = ((long) b[offset+7] << 56) & 0xFF00000000000000L;
		// 如果不强制转换为long，那么默认会当作int，导致最高32位丢失
		l |= ((long) b[offset+6] << 48) & 0xFF000000000000L;
		l |= ((long) b[offset+5] << 40) & 0xFF0000000000L;
		l |= ((long) b[offset+4] << 32) & 0xFF00000000L;
		l |= ((long) b[offset+3] << 24) & 0xFF000000L;
		l |= ((long) b[offset+2] << 16) & 0xFF0000L;
		l |= ((long) b[offset+1] << 8) & 0xFF00L;
		l |= (long) b[offset+0] & 0xFFL;
		return l;
	}

	/**
	 * 将一个char字符转换位字节数组（2个字节），b[0]存储高位字符，大端
	 * 
	 * @param c
	 *            字符（java char 2个字节）
	 * @return 代表字符的字节数组
	 */
	public static byte[] charToBytes(char c) {
		byte[] b = new byte[8];
		b[1] = (byte) (c >>> 8);
		b[0] = (byte) c;
		return b;
	}

	/**
	 * 将一个双精度浮点数转换位字节数组（8个字节），b[0]存储高位字符，大端
	 * 
	 * @param d
	 *            双精度浮点数
	 * @return 代表双精度浮点数的字节数组
	 */
	public static byte[] doubleToBytes(double d) {
		return longToBytes(Double.doubleToLongBits(d));
	}

	/**
	 * 将一个浮点数转换为字节数组（4个字节），b[0]存储高位字符，大端
	 * 
	 * @param f
	 *            浮点数
	 * @return 代表浮点数的字节数组
	 */
	public static byte[] floatToBytes(float f) {
		return intToBytes(Float.floatToIntBits(f));
	}

	/**
	 * 将一个整数转换位字节数组(4个字节)，b[0]存储高位字符，大端
	 * 
	 * @param i
	 *            整数
	 * @return 代表整数的字节数组
	 */
	public static byte[] intToBytes(int i) {
		byte[] b = new byte[4];
		b[3] = (byte) (i >>> 24);
		b[2] = (byte) (i >>> 16);
		b[1] = (byte) (i >>> 8);
		b[0] = (byte) i;
		return b;
	}
	
	public static byte[] intToBytes(int i, byte[] b) {
		b[3] = (byte) (i >>> 24);
		b[2] = (byte) (i >>> 16);
		b[1] = (byte) (i >>> 8);
		b[0] = (byte) i;
		return b;
	}

	/**
	 * 将一个长整数转换位字节数组(8个字节)，b[0]存储高位字符，大端
	 * 
	 * @param l
	 *            长整数
	 * @return 代表长整数的字节数组
	 */
	public static byte[] longToBytes(long l) {
		byte[] b = new byte[8];
		return longToBytes(l, b);
	}
	
	public static byte[] longToBytes(long l, byte[] b) {
		b[7] = (byte) (l >>> 56);
		b[6] = (byte) (l >>> 48);
		b[5] = (byte) (l >>> 40);
		b[4] = (byte) (l >>> 32);
		b[3] = (byte) (l >>> 24);
		b[2] = (byte) (l >>> 16);
		b[1] = (byte) (l >>> 8);
		b[0] = (byte) (l);
		return b;
	}
}
