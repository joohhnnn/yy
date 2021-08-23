package com.txznet.txz.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExchangeHelper {

    /*
    * 阿拉伯数字转中文
    */
    public static String numberToChinese(int num){

        String[] chineseSectionPosition = {"","万","亿","万亿"}; //中文数字节权位

        if(num == 0){
            return "零";
        }
        int sectionPosition = 0; //节权位标识
        String endChineseNum = new String(); //最终转换完成的中文字符串
        String sectionChineseNum = new String(); //每个小节转换的字符串
        while(num>0){ //将阿拉伯数字从右往左每隔4位分成一个小节，在分别对每个小节进行转换处理，最后加上该小节对应的节权
            int section = num%10000; //获取最后一个小节（低4位数：千百十个）
            sectionChineseNum = eachSection(section); //将当前小节转换为中文
            if(section != 0){ //当前小节不为0时，添加节权
                sectionChineseNum = sectionChineseNum + chineseSectionPosition[sectionPosition];
            }
            num = num / 10000; //去掉已经转换的末尾4位数
            endChineseNum = sectionChineseNum + endChineseNum;
            sectionPosition++; //节权位增加1
        }
        if('零' == endChineseNum.charAt(0)){
            endChineseNum = endChineseNum.substring(1);
        }
        return endChineseNum;
    }

    /*
     * 中文转阿拉伯数字
     */
    public static int chineseToNumber(String chineseNum){

        int number = 0; //保存阿拉伯数字
        String str1 = new String(); //保存“亿”节权部分
        String str2 = new String(); //保存“万”节权部分
        String str3 = new String(); //保存 “” 节权部分
        int k = 0; //用于记录节权位置
        boolean deal = true; //该节权部分已经处理
        //去除字符串中“零”
        for(int i=0;i<chineseNum.length();i++){
            if('零' == chineseNum.charAt(i)){
                chineseNum = chineseNum.substring(0, i) + chineseNum.substring(i+1);
            }
        }
        //各部分节权处理,将中文以小节为单位截取存入不同的字符串中
        String str = chineseNum;
        for(int i=0;i<str.length();i++){
            if(str.charAt(i) == '亿'){ //截取“亿”前面部分
                str1 = str.substring(0, i);
                k = i+1; //“亿”的位置
                deal = false;
            }
            if(str.charAt(i) == '万'){//截取“亿”后面部分
                str2 = str.substring(k,i); //截取“万”前面部分
                str3 = str.substring(i+1); //截取“万”后面部分
                deal = false;
            }
        }
        if(deal){ //该数小于万
            str3 = str;
        }
        //将中文转换为阿拉伯数字
        number = eachSection(str1)*100000000 + eachSection(str2)*10000 + eachSection(str3);

        return number;
    }

    /*
     * 阿拉伯数字转中文---一个小节的操作
     */
    public static String eachSection(int num){ //重载方法

        String[] chineseNumber = {"零","一","二","三","四","五","六","七","八","九"}; //中文数字位
        String[] chinesePosition = {"","十","百","千"}; //中文数字权位

        String chineseNum = new String(); //转换的中文数字
        boolean zero = true; //小节内部制零判断，每个小节内部只能出现一个中文“零”
        for(int i=0;i<4;i++){ //每个小节中只有4位数
            int end = num%10; //获取末位值
            //判断该数字是否为0。若不是0就直接转换并加上权位，若是0，继续判断是否已经出现过中文数字“零”
            if(end == 0){ //该数字是0
                if(!zero){ //上一位数不为0，执行补0
                    zero = true;
                    chineseNum = chineseNumber[0] + chineseNum;
                }
            }else{ //该数字不为0，直接转换并加上权位
                zero = false;
                chineseNum = chineseNumber[end] + chinesePosition[i] + chineseNum; //数字+权位
            }
            num = num/10;
        }
        return chineseNum;
    }

    /*
     * 中文转阿拉伯数字---一个小节的操作
     */
    public static int eachSection(String chineseNum){ //重载方法

        //map集合，用于中文转数字
        Map<Character,Integer> map = new HashMap<Character,Integer>();
        char[] ch = {'零','一','二','三','四','五','六','七','八','九'}; //数字位
        for(int i=0;i<ch.length;i++){
            map.put(ch[i],i);
        }
        map.put('两', 2);
        //权位
        map.put('十',10);
        map.put('百',100);
        map.put('千', 1000);

        int result = 0; //保存转换的数字
        int num = 0; //保存每一个数字
        for(int i=0;i<chineseNum.length();i++){
            int v = map.get(chineseNum.charAt(i));
            //判断是数字还是权位（第1个字符是数字，2个是权位。。。依次类推）
            if(v==10 || v==100 || v==1000){ //权位
                result = result + num * v;
            }else if(i == chineseNum.length()-1){ //判断是否为个位
                result = result + v;
            }else{ //不是个位
                num = v;
            }
        }
        return result;
    }
    
    
    /*
    *
       System.out.println(toDigit("幺零零八六"));
       System.out.println(toDigit("一千九百九十九"));
    * 转换一万以内的中文数字或者单纯的数字
    */
	public static String toDigit(String strChineseNumber) {
       //map集合，用于中文转数字
       Map<Character,Integer> digitMap = new HashMap<Character,Integer>();
       char[] digitChs = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九'}; //数字位
       for(int i = 0; i < digitChs.length; i++){
       	digitMap.put(digitChs[i], i);
       	digitMap.put((char) ('0' + i), i);
       }
       digitMap.put('幺', 1);
       digitMap.put('两', 2);
       
       //权位
       Map<Character,Integer> weighMap = new HashMap<Character,Integer>();
       weighMap.put('十', 10);
       weighMap.put('百', 100);
       weighMap.put('千', 1000);
      
       StringBuffer sb = new StringBuffer();
       int value = 0;
       boolean bHasWeigh = false;
       for (int i = 0; i < strChineseNumber.length(); ++i){
       	char ch = strChineseNumber.charAt(i);
			do {
				//缓存数字
				if (digitMap.get(ch) != null) {
					sb.append(digitMap.get(ch));
					break;
				}
				//包含'千'、'百'、'十'
				if (weighMap.get(ch) != null) {
					bHasWeigh = true;
					if (sb.length() > 0){
						char tailch = sb.charAt(sb.length() - 1);
						sb.delete(0, sb.length());
						value += (tailch - '0') * weighMap.get(ch);
						break;
					}else{
						if (ch == '十'){
							boolean bIgnore = false;
							if (i < strChineseNumber.length() - 1){
								char lastCh = strChineseNumber.charAt(i + 1);
								if (weighMap.get(lastCh) != null){
									bIgnore = true;
								}
							}
							if (!bIgnore){
								bHasWeigh = true;
								value += weighMap.get(ch);
								break;
							}
						}
					}
				}
				//包含非法字符直接返回空串
				return "";
			} while (false);	
       }
       
       String strDigit = "";
       //取出个数
       if (bHasWeigh){
			if (sb.length() > 0){
				char tailch = sb.charAt(sb.length() - 1);
				sb.delete(0, sb.length());
				value += (tailch - '0');
			}
			strDigit = "" + value;
       }else{
       		strDigit = sb.toString();
       }
		return strDigit;
	}
	
   /*
   *
   * 转换一万以内的中文数字或者单纯的数字(小数点)
   */
	public static String toComplexDigit(String strChineseNumber){
		String[] temp = strChineseNumber.split("\\.");
		if (temp.length == 2){
			return toDigit(temp[0]) + "." + toDigit(temp[1]);
		}else if (temp.length == 1 && !strChineseNumber.endsWith(".")){
			return toDigit(strChineseNumber);
		}
		return "";
	}
	
	/*
	 * 是否包含阿拉伯数字
	 */
	public static boolean hasDigit(String strText){
		for (int i = 0; i < strText.length(); ++i){
			char ch = strText.charAt(i);
			if (ch >= '0' + 0 && ch <= '0' + 9 ){
				return true;
			}
		}
		return false;
	}
	
	/*
	 * 是否包含中文数字
	 */
	public static boolean hasChineseDigit(String strText){
		Set<Character> set = new HashSet<Character>();
		char[] digitChs = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九', '十', '百', '千'}; //数字位
        for(int i = 0; i < digitChs.length; i++){
        	set.add(digitChs[i]);
        }
		for (int i = 0; i < strText.length(); ++i){
			char ch = strText.charAt(i);
			if (set.contains(ch)){
				return true;
			}
		}
		return false;
	}
	
	 /*
    *
    * 转换一万以内的中文数字或者单纯的数字(可以包含特殊符号)
    */
	public static String toDigit(String strChineseNumber, boolean b) {
        //map集合，用于中文转数字
        Map<Character,Integer> digitMap = new HashMap<Character,Integer>();
        char[] digitChs = {'零', '一', '二', '三', '四', '五', '六', '七', '八', '九'}; //数字位
        for(int i = 0; i < digitChs.length; i++){
        	digitMap.put(digitChs[i], i);
        	digitMap.put((char) ('0' + i), i);
        }
        digitMap.put('幺', 1);
        digitMap.put('两', 2);
        
        //权位
        Map<Character,Integer> weighMap = new HashMap<Character,Integer>();
        weighMap.put('十', 10);
        weighMap.put('百', 100);
        weighMap.put('千', 1000);
        
        StringBuffer buffer = new StringBuffer();
        StringBuffer sb = new StringBuffer();
        int value = 0;
        boolean bHasValue = false;
        boolean bHasWeigh = false;
        for (int i = 0; i < strChineseNumber.length(); ++i){
        	char ch = strChineseNumber.charAt(i);
			do {
				//缓存数字
				if (digitMap.get(ch) != null) {
					sb.append(digitMap.get(ch));
					break;
				}
				//包含'千'、'百'、'十'
				if (weighMap.get(ch) != null) {
					bHasWeigh = true;
					if (sb.length() > 0){
						char tailch = sb.charAt(sb.length() - 1);
						sb.delete(0, sb.length());
						value += (tailch - '0') * weighMap.get(ch);
						bHasValue = true;
						break;
					}else{
						if (ch == '十'){
							boolean bIgnore = false;
							if (i < strChineseNumber.length() - 1){
								char lastCh = strChineseNumber.charAt(i + 1);
								if (weighMap.get(lastCh) != null){
									bIgnore = true;
								}
							}
							if (!bIgnore){
								bHasWeigh = true;
								value += weighMap.get(ch);
								bHasValue = true;
								break;
							}
						}
					}
				}
				
				//包含其他字符
				String strDigit = "";
		        if (bHasWeigh){
					if (sb.length() > 0){
						char tailch = sb.charAt(sb.length() - 1);
						value += (tailch - '0');
					}
					if (bHasValue){
						strDigit = "" + value;
					}
		        }else{
		        	strDigit = sb.toString();
		        }
		        sb.delete(0, sb.length());
		        buffer.append(strDigit);
				buffer.append(ch);
				
				//置位
				value = 0;
				bHasWeigh = false;
				bHasValue = false;
			} while (false);	
        }
        
        //
        String strDigit = "";
        if (bHasWeigh){
			if (sb.length() > 0){
				char tailch = sb.charAt(sb.length() - 1);
				sb.delete(0, sb.length());
				value += (tailch - '0');
			}
			if (bHasValue){
				strDigit = "" + value;
			}
        }else{
        	strDigit = sb.toString();
        }
        sb.delete(0, sb.length());
        buffer.append(strDigit);
        
		return buffer.toString();//strDigit;
	}
}
