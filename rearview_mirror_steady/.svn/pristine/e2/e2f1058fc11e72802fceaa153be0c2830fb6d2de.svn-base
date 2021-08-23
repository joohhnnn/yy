package com.txznet.jni;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TXZStrComparator implements Comparator<String> {

	public static final int CHAR_TYPE_UNSET = 0; // 未设置
	public static final int CHAR_TYPE_CTRL = 1; // 打印控制符
	public static final int CHAR_TYPE_SPACE = 2; // 空格
	public static final int CHAR_TYPE_CHINESE = 3; // 中文
	public static final int CHAR_TYPE_ALPHA = 4; // 字母
	public static final int CHAR_TYPE_DIGIT = 5; // 数字
	public static final int CHAR_TYPE_SYMBOL = 6; // 符号
	public static final int CHAR_TYPE_SPECIAL_SYMBOL = 7; // 特殊符号
	public static final int CHAR_TYPE_OTHER = 8; // 其他语言
	public static final int CHAR_TYPE_UNKNOW = 9; // 未知字符

	public static final long FEATURE_FLAG_IGNORE_CASE = 1; // 忽略大小写
	public static final long FEATURE_FLAG_IGNORE_WIDTH = 2; // 忽略全半角，对字母和数字有效，符号无效

	public static final long COMPARE_FLAG_IGNORE_CASE = FEATURE_FLAG_IGNORE_CASE; // 忽略大小写
	public static final long COMPARE_FLAG_IGNORE_WIDTH = FEATURE_FLAG_IGNORE_WIDTH; // 忽略全半角，对字母和数字有效，符号无效
	public static final long COMPARE_FLAG_SPACE_BLOCK = 4; // 连续空格块处理，多个空格当作一个块
	public static final long COMPARE_FLAG_SKIP_CTRL_CHAR = 8; // 跳过控制字符

	public static final long COMPARE_FLAG_RECOMMAND = COMPARE_FLAG_IGNORE_CASE
			| COMPARE_FLAG_IGNORE_WIDTH | COMPARE_FLAG_SPACE_BLOCK
			| COMPARE_FLAG_SKIP_CTRL_CHAR; // 推荐比较标志

	public static final long COMPARE_TYPE_CHINESE = 0; // 啊<座<0<9<A<Z<a<z<#<else
	public static final long COMPARE_TYPE_ENGLISH = 1; // 0<9<A<Z<a<z<#<else
	public static final long COMPARE_TYPE_CHINESE_NAME = 2; // 啊<曾<座<A<Z<a<z<0<9<#<else
	public static final long COMPARE_TYPE_CHINESE_NAME_CHAOS = 3; // 啊<A<a<曾<座<Z<z<0<9<#<else

	private static native int init(String dataFile);

	public static int initialize(String dataFile) {
		System.loadLibrary("TXZUtil");
		return init(dataFile);
	}

	public static int initialize(String libFile, String dataFile) {
		System.load(libFile);
		return init(dataFile);
	}

	public static native int release();

	public static native long getCharFeature(char ch, long flag);

	public static native int compareStr(String lhs, String rhs, long flag,
			long type);

	private long flag;
	private long type;

	public TXZStrComparator(long flag, long type) {
		this.flag = flag;
		this.type = type;
	}

	@Override
	public int compare(String lhs, String rhs) {
		return compareStr(lhs, rhs, this.flag, this.type);
	}

	// ///////////////////////////////////////////////////////////////////////////////////////

	public static int compareChinese(String left, String right) {
		return compareStr(left, right, COMPARE_FLAG_RECOMMAND,
				COMPARE_TYPE_CHINESE);
	}

	public static int compareContact(String left, String right) {
		return compareStr(left, right, COMPARE_FLAG_RECOMMAND,
				COMPARE_TYPE_CHINESE_NAME_CHAOS);
	}

	// ///////////////////////////////////////////////////////////////////////////////////////

	public static void test(String left, String right) {
		int ret = compareContact(left, right);
		System.out.println("[" + left + "]  " + ret + "  [" + right + "]");
	}

	public static void main(String[] args) {
		String root = "D:\\svn\\android\\projects\\rearview_mirror\\TXZUtil\\jni\\TXZPinyinCompare\\";
		int ret = initialize(root + "TXZUtil.dll", root + "unipy.dat");
		System.out.println("load " + root + " return " + ret);

		ArrayList<String> lst = new ArrayList<String>();
		try {
			FileReader reader = new FileReader(root + "constacts.lst");
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			while ((str = br.readLine()) != null) {
				lst.add(str);
			}
			br.close();
			reader.close();
		} catch (Exception e) {
		}

		Collections.sort(lst, new TXZStrComparator(COMPARE_FLAG_RECOMMAND,
				COMPARE_TYPE_CHINESE_NAME_CHAOS));
		for (String s : lst) {
			System.out.println(s);
		}
	}
}
