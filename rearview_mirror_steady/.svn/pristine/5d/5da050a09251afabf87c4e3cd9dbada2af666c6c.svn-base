package com.txznet.txz.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;

public class KeywordsParser {
	// 分隔符表达式
	public final static String CHARS_SPLIT = "\\u2022\\.\\,\\;\\:@#~&\\(\\)\\[\\]\\{\\}\\<\\>\\u0001-\\u002F\\u003A-\\u0040\\u005b-\\u0060\\u007b-\\u007f，；。：、·！%￥‘’“”「」『』（）〔〕【】｛｝《》〈〉";
	public final static String CHARS_SPLIT_ADDR = "\\u2022\\.。\\,，\\;；\\:：@#~&\\(\\)\\[\\]\\{\\}\\<\\>‘’“”「」『』（）〔〕【】｛｝《》〈〉";
	public final static String EXPR_SPLIT = "[" + CHARS_SPLIT + "]+";
	public final static String EXPR_SPLIT_ADDR = "[" + CHARS_SPLIT_ADDR + "]+";
	public final static String EXPR_NOT_SPLIT_5 = "[^" + CHARS_SPLIT + "]{1,5}";
	public final static String EXPR_NOT_SPLIT = "[^" + CHARS_SPLIT + "]+";
	// 编号表达式
	public final static String CHARS_NUMBER = "\\w\\-\\u4e00\\u4e8c\\u4e09\\u56db\\u4e94\\u516d\\u4e03\\u516b\\u4e5d\\u5341\\u96f6";
	public final static String EXPR_NUMBER = "[" + CHARS_NUMBER + "]+";

	// 地址分割表达式
	public final static String EXPR_PROVINCE = "(" + EXPR_NOT_SPLIT_5
			+ "(?:省|自治区))?";
	public final static String EXPR_CITY = "(" + EXPR_NOT_SPLIT_5 + "(?:市))?";
	public final static String EXPR_XIAN = "(" + EXPR_NOT_SPLIT_5 + "(?:县))?";
	public final static String EXPR_TOWN = "(" + EXPR_NOT_SPLIT_5 + "(?:乡|镇))?";
	public final static String EXPR_AREA = "(" + EXPR_NOT_SPLIT_5 + "(?:区))?";
	public final static String EXPR_VILLAGE = "(" + EXPR_NOT_SPLIT_5
			+ "(?:村))?";
	public final static String EXPR_ROAD = "(" + EXPR_NOT_SPLIT_5
			+ "(?:路|道|街|巷|(?:胡同)))?";
	public final static String EXPR_BUILDING = "(" + EXPR_NOT_SPLIT + ")?";
	public final static String EXPR_ROOM = "(?:" + EXPR_NUMBER + "号)?(?:"
			+ EXPR_NUMBER + "(?:栋|号?楼?))?(?:" + EXPR_NUMBER + "(?:楼|层))?(?:"
			+ EXPR_NUMBER + "区)?(?:" + EXPR_NUMBER + "室?)?";

	public final static String EXPR_ADDRESS_END_KEYWORDS = "(?:省|自治区|市|县|旗|盟|乡|镇|区|村|路|道|街|巷|(?:胡同)|(?:大厦)|楼)";

	public final static String EXPR_PATTERN_ADDRESS = EXPR_PROVINCE + EXPR_CITY
			+ EXPR_XIAN + EXPR_TOWN + EXPR_AREA + EXPR_VILLAGE + EXPR_ROAD
			+ EXPR_VILLAGE + EXPR_ROOM + EXPR_BUILDING + EXPR_ROOM
			+ EXPR_BUILDING;

	// 地址分割
	public final static Pattern PATTERN_ADDRESS = Pattern
			.compile(EXPR_PATTERN_ADDRESS);
	// 标点分割地址
	public final static Pattern PATTERN_SPLIT_ADDR = Pattern.compile("("
			+ EXPR_SPLIT_ADDR + ")");
	// 标点分割
	public final static Pattern PATTERN_SPLIT = Pattern.compile("("
			+ EXPR_SPLIT + ")");

	// 分割关键字
	public static Set<String> splitKeywords(CharSequence str) {
		Set<String> lst = new HashSet<String>();
		if (str == null) {
			return lst;
		}
		String[] kws = PATTERN_SPLIT.split(str);
		for (String kw : kws) {
			if (TextUtils.isEmpty(kw))
				continue;
			lst.add(kw);
		}
		return lst;
	}

	// 地址分割关键字
	public static Set<String> splitAddressKeywords(CharSequence addr) {
		Set<String> lst = new HashSet<String>();
		if (addr != null) {
			for (String str : PATTERN_SPLIT_ADDR.split(addr)) {
				if (TextUtils.isEmpty(str))
					continue;
				Matcher m = PATTERN_ADDRESS.matcher(str);
				if (!m.find())
					continue;
				for (int j = 1; j <= m.groupCount(); ++j) {
					String kw = m.group(j);
					if (TextUtils.isEmpty(kw) || TextUtils.isDigitsOnly(kw))
						continue;
					lst.add(kw);
					if (j < m.groupCount()) {
						if (kw.endsWith("自治区")) {
							lst.add(kw.substring(0, kw.length() - 3));
						} else if (kw.endsWith("省") || kw.endsWith("市")
								|| kw.endsWith("县") || kw.endsWith("乡")
								|| kw.endsWith("镇") || kw.endsWith("区")) {
							lst.add(kw.substring(0, kw.length() - 1));
						}
					}
				}
			}
		}
		return lst;
	}

	public static List<String> splitNavAdrress(String addr) {
		Set<Integer> kws = new HashSet<Integer>();
		// for (char kw : new char[] { '号', '村', '街', '路', '道', '乡', '旗', '镇',
		// '县', '州', '盟', '区', '市' }) {
		// kws.add((int) kw);
		// }
		// 刘卫反馈找到"市"、"区"这么大的范围也没什么意义，决定去掉。
		for (char kw : new char[] { '号', '村', '街', '路', '道', '乡', '旗', '镇'}) {
			kws.add((int) kw);
		}
		List<String> ret = new ArrayList<String>();
		int last = addr.length();
		for (int i = addr.length() - 1; i >= 0;) {
			// System.out.println("" + addr.charAt(i)
			// + kws.contains((int) addr.charAt(i)));
			if (kws.contains((int) addr.charAt(i))) {
				if (last != i + 1) {
					ret.add(addr.substring(i + 1, last));
				}
				last = i + 1;
				i -= 3;
			} else {
				--i;
			}
		}
		ret.add(addr.substring(0, last));
		return ret;
	}

	public static String getNextNavAddress(String addr) {
		List<String> addrs = splitNavAdrress(addr);
		if (addrs.size() > 1) {
			return addr.substring(0, addr.length() - addrs.get(0).length());
		}
		return null;
	}
}
