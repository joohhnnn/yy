package com.txznet.music.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * @author telenewbie
 * @version 创建时间：2016年4月14日 上午10:50:00
 * 
 */
public class PinYinUtil {

	public static String getPinYin(String inputString) {
		return getPriPinYin(inputString, false);
	}

	/**
	 * 获得全拼
	 * 
	 * @param inputString
	 * @return
	 */
	public static String getAllPinYin(String inputString) {
		return getPriPinYin(inputString, true);
	}

	private static String getPriPinYin(String inputString, boolean isAll) {

		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_U_UNICODE);
		char[] input = inputString.trim().toCharArray();
		StringBuffer output = new StringBuffer("");

		try {
			for (int i = 0; i < input.length; i++) {
				if (Character.toString(input[i]).matches("[\\u4E00-\\u9FBF]+")) {
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(
							input[i], format);

					output.append(isAll ? temp[0] : temp[0].charAt(0));
					// output.append(" ");
				} else if (Character.toString(input[i]).matches("[0-9]")) {// 如果是数字
					output.append("{");
					output.append(Character.toString(input[i]));
				} else if (Character.toString(input[i]).matches("[A-Za-z]")) {// 如果是字母
					output.append(Character.toString(input[i]).toLowerCase());
				} else {
					output.append("|");
					output.append(Character.toString(input[i]));
				}
			}
		} catch (BadHanyuPinyinOutputFormatCombination e) {
			e.printStackTrace();
		}
		return output.toString();
	}

	/**
	 * 剔除掉特殊字符的转换 斑马，斑马=banmabanma
	 * 
	 * @return
	 */
//	private static String getPinyinExcept(String input) {
//		String str = "!!！？？!!!!%*）%￥！KTV去符号标号！！当然,，。!!..**半角";
//		System.out.println(str);
//		String str1 = str.replaceAll("[\\pP\\p{Punct}]", "");
//		System.out.println("str1:" + str1);
//	}

	public static void main() {
		String chs = "我是中国人!총맞은것처럼 I'm Chinese!";
		System.out.println(chs);
		System.out.println(getPinYin(chs));
	}
}