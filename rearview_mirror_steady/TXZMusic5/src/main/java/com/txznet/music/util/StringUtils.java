package com.txznet.music.util;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import com.txznet.music.Constant;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static java.util.Calendar.APRIL;
import static java.util.Calendar.AUGUST;
import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.FEBRUARY;
import static java.util.Calendar.JANUARY;
import static java.util.Calendar.JULY;
import static java.util.Calendar.JUNE;
import static java.util.Calendar.MARCH;
import static java.util.Calendar.MAY;
import static java.util.Calendar.NOVEMBER;
import static java.util.Calendar.OCTOBER;
import static java.util.Calendar.SEPTEMBER;
import static java.util.Calendar.UNDECIMBER;

/**
 * @author zackzhou
 * @date 2019/1/7,20:47
 */

public class StringUtils {

    private StringUtils() {
    }

    public static SpannableString getTitleForAudioNameWithArtists(String title, String subTitle) {
        SpannableString spannableString;
        int songColor = 0;
        int artistColor = 0;
        songColor = Color.WHITE;
        artistColor = Color.parseColor("#7FFFFFFF");

        if (TextUtils.isEmpty(subTitle)) {
            subTitle = Constant.UNKNOWN;
        }
        String name = String.format(Locale.getDefault(), "%s - %s", title, subTitle);
        spannableString = new SpannableString(name);
        spannableString.setSpan(new ForegroundColorSpan(songColor), 0, title.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(artistColor)
                , title.length() + 1, name.length()
                , Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        return spannableString;
    }

    public static String getFormatedMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int month = calendar.get(Calendar.MONTH);
        switch (month) {
            case JANUARY:
                return "??????";
            case FEBRUARY:
                return "??????";
            case MARCH:
                return "??????";
            case APRIL:
                return "??????";
            case MAY:
                return "??????";
            case JUNE:
                return "??????";
            case JULY:
                return "??????";
            case AUGUST:
                return "??????";
            case SEPTEMBER:
                return "??????";
            case OCTOBER:
                return "??????";
            case NOVEMBER:
                return "?????????";
            case DECEMBER:
                return "?????????";
            case UNDECIMBER:
                return "";
        }
        return "";
    }

    // ??????????????????
    public static String stringFilter(String str) throws PatternSyntaxException {
        // ????????????????????????
        // String regEx = "[^a-zA-Z0-9]";
        // ???????????????????????????
        String regEx = "[`~!@#$%^&*()+=|{}':;,\\[\\].<>/???????????????????????????????????????????????????????]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll(" ").trim();
    }
}
