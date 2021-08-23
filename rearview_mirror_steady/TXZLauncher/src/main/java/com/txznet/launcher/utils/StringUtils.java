package com.txznet.launcher.utils;

import android.os.SystemClock;
import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.DeviceInfo;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.PreUrlUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by zackzhou on 2018/3/23.
 * 字符串处理工具
 */

public class StringUtils {
    private static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private StringUtils() {
    }

    // 获取开机欢迎语
    public static String getWelcomeText(long timeMillis, String nick) {
        String txtTime = getFriendlyTimeText(timeMillis);
        if (TextUtils.isEmpty(nick)) { // 没有登录
            if (TextUtils.isEmpty(txtTime)) {
                return "Hello";
            } else {
                return "Hello, " + txtTime;
            }
        } else {
            return (TextUtils.isEmpty(txtTime) ? "Hello" : txtTime) + ", " + nick;
        }
    }

    // 获取友好的时间问候提示
    public static String getFriendlyTimeText(long timeMillis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        if (hour >= 6 && hour < 11 || (hour == 11 && minute < 30)) { // 6:00 ~ 11:29
            return "早上好";
        } else if (hour == 11 || hour < 13) { // 11:30 ~ 12:59
            return "中午好";
        } else if (hour < 18) { // 13:00 ~ 17:59
            return "下午好";
        } else {
            return "晚上好";
        }
    }


    // 判断字符串是否未纯中文
    public static boolean isFullChinese(String str) {
        return str.matches("[\u4e00-\u9fa5]+");
    }

    /**
     * 将时间戳变成文字
     */
    public static String formatDate(long timeMillis) {
        return mDateFormat.format(new Date(timeMillis));
    }


    /**
     * 获取节假日提醒
     * @param timeMillis
     * @return
     */
    public static String getHolidayTips(long timeMillis) {
        String tips = "";
        ChineseCalendar calendar = new ChineseCalendar();
        calendar.setTimeInMillis(timeMillis);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int chineseMonth = Math.abs(calendar.get(ChineseCalendar.CHINESE_MONTH));
        int chineseDay = calendar.get(ChineseCalendar.CHINESE_DATE);

        if (year < 2018) {
            return "";
        }

        //1.开始处理农历的节日
        if (chineseMonth == 1) {
            if (chineseDay == 1) {
                tips = "欢欢喜喜迎新春，小欧祝您春节快乐，新年吉祥";
            } else if (chineseDay == 15) {
                tips = "元宵圆，人团圆，小欧祝您元宵节快乐";
            }
        } else if (chineseMonth == 5) {
            if (chineseDay == 5) {
                tips = "赛龙舟，吃粽子，小欧祝您端午节快乐";
            }
        } else if (chineseMonth == 7) {
                if (chineseDay == 7) {
                    tips = "今天是七夕节，小欧祝您七夕情人节快乐";
                }
        } else if (chineseMonth == 8) {
            if (chineseDay == 15) {
                tips = "庆中秋，乐团圆，小欧祝您中秋节快乐";
            }
        }

        //判断为农历节日，直接就返回了
        if (!TextUtils.isEmpty(tips)) {
            return tips;
        }

        //2.阳历节日
        if (month == 1 && day == 1) {
            tips = "新征程，新气象，小欧祝您元旦快乐";
        }else if (month == 2 && day == 14) {
            tips = "今天是情人节，小欧为您送上爱的祝福";
        }else if (month == 10 && day == 1) {
            tips = "迎国庆，喜洋洋，小欧祝您国庆节快乐";
        } else if (month == 5) {
            //5月的第二个星期天
            if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                if (calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 2) {
                    tips = "母亲节到了，祝伟大的母亲永远幸福快乐";
                }
            }
        } else if (month == 6) {
            //6月的第三个星期天
            if( calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                if (calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 3) {
                    tips = "父亲节到了，祝伟大的父亲幸福健康";
                }
            }
        } else if (month == 11) {
            //11月的第四个星期四
            if(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) {
                if (calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) == 4) {
                    tips = "感恩有你，一路陪伴，小欧祝您感恩节快乐";
                }
            }
        }

        return tips;
    }

    //    生成的字符串每个位置都有可能是str中的一个字母或数字，需要导入的包是import java.util.Random;
    //length用户要求产生字符串的长度
    public static String getRandomString(int length){
        String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            // TODO: 2018/8/1 这里的62可以用str.length()替代的吧？
            int number=random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 根据业务需要，构建开机二维码
     * 规则是：core_Id+";"+imei+时间戳（秒）+md5(coreId+imei)
     * 字段	例子	备注
     * core_Id	100000	长度不固定
     * imei	111111111111111	长度固定,15位
     * 时间戳	1525859568         	精确到秒10位
     * md5(coreId+imei)	91F8F24DAFF8EA78	对coreId和imei算MD5，16位大写
     * @return
     */
    public static String getDeviceQrCode(){
        StringBuilder sb = new StringBuilder();
        sb.append(DeviceUtils.getCoreID());
        sb.append(";");
        sb.append(DeviceUtils.getIMEI());
        StringBuilder timeMillis = new StringBuilder((System.currentTimeMillis() / 1000) + "");
        // 用StringBuilder补零比String.format要快。
        if (timeMillis.length() < 10) {
            for (int i = 0;i <= 10 - timeMillis.length(); i++) {
                timeMillis.insert(0, "0");
            }
        }
        sb.append(timeMillis);
        sb.append(encrypt16(DeviceUtils.getDeviceID()));
        return sb.toString();
    }

    /**
     * 算md5，取16位大写
     */
    public static String encrypt16(String encryptStr) {
        try {
            return MD5Util.generateMD5(encryptStr).substring(8, 24).toUpperCase();
        }  catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取wifi名字
     */
    public static String getWifiName() {
        // 如果有设置过wifi名字，就使用设置的wifi名字
        String name = PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_WIFI_AP_NAME, "");
        if (!TextUtils.isEmpty(name)) {
            return name;
        }

        // 没有设置过wifi名字，使用"OnStar_xxxx"格式的名字，xxx为imei的11-14位。
        String imei = DeviceUtils.getIMEI();
        if (imei != null) {
            // imei一般至少有14位，但由于imei理论上是可以修改的，为了避免用户非法修改imei导致这里的代码crash而不能获取到正常获取到名字，这里执行了try-catch
            name = PreferenceUtil.DEFAULT_WIFI_AP_NAME + "_" + imei.substring(10, 14);
            // 缓存名字，一般imei是不会动的。
            PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_WIFI_AP_NAME, name);
        } else {
            name = PreferenceUtil.DEFAULT_WIFI_AP_NAME+ "_" ;
        }
        return name;
    }
}
