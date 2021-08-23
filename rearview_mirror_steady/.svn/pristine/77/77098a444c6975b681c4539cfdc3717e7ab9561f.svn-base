package com.txznet.comm.ui.theme.test.config;

import android.util.Log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;

/**
 * Created by JackPan on 2019/5/10
 * Describe:
 */
public class ViewParamsUtil {

    private static ViewParamsUtil mIntance = new ViewParamsUtil();
    private static float x;
    private static float y;

    public static int unit;
    public static int h0;
    public static int h1;
    public static int h2;
    public static int h3;
    public static int h4;
    public static int h5;
    public static int h6;
    public static int h7;
    public static int h1Height;
    public static int h2Height;
    public static int h3Height;
    public static int h4Height;
    public static int h5Height;
    public static int h6Height;
    public static int h7Height;
    public static int billWidth;
    public static int billWidthHalf;
    public static int billWidthNone;
    public static int centerInterval;
    public static int musicTagSide;
    public static int shockPriceSize;
    public static int shockPriceHeight;
    public static int shockNameSize;

    private ViewParamsUtil(){}

    public static ViewParamsUtil getIntance(){
        return mIntance;
    }

//    public void updateViewParams(boolean isVertScreen){
//        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
//            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
//                if (isVertScreen){
//                    unit = Math.round(LayouUtil.getDimen("x10"));
//                    h0 = Math.round(LayouUtil.getDimen("x38"));
//                    h1 = Math.round( LayouUtil.getDimen("x33"));
//                    h2 = Math.round( LayouUtil.getDimen("x31"));
//                    h3 = Math.round( LayouUtil.getDimen("x29"));
//                    h4 = Math.round( LayouUtil.getDimen("x27"));
//                    h5 = Math.round( LayouUtil.getDimen("x25"));
//                    h6 = Math.round( LayouUtil.getDimen("x23"));
//                    h7 = Math.round( LayouUtil.getDimen("x21"));
//                    billWidth = Math.round( LayouUtil.getDimen("x267"));
//                    billWidthHalf = Math.round( LayouUtil.getDimen("x265"));
//                    billWidthNone = Math.round( LayouUtil.getDimen("x208"));
//                    centerInterval = Math.round( LayouUtil.getDimen("x6"));
//                    musicTagSide = Math.round( LayouUtil.getDimen("x29"));
//                    shockPriceSize = Math.round( LayouUtil.getDimen("x66"));
//                    shockPriceHeight = shockPriceSize + 12;
//                    shockNameSize = Math.round( LayouUtil.getDimen("x25"));
//                }else {
//                    unit = Math.round(LayouUtil.getDimen("y8"));
//                    h0 = Math.round( LayouUtil.getDimen("y29"));
//                    h1 = Math.round( LayouUtil.getDimen("y26"));
//                    h2 = Math.round( LayouUtil.getDimen("y24"));
//                    h3 = Math.round( LayouUtil.getDimen("y22"));
//                    h4 = Math.round( LayouUtil.getDimen("y21"));
//                    h5 = Math.round( LayouUtil.getDimen("y19"));
//                    h6 = Math.round( LayouUtil.getDimen("y18"));
//                    h7 = Math.round( LayouUtil.getDimen("y16"));
//                    billWidth = Math.round( LayouUtil.getDimen("y163"));
//                    billWidthHalf = Math.round( LayouUtil.getDimen("y158"));
//                    billWidthNone = Math.round( LayouUtil.getDimen("y131"));
//                    centerInterval = Math.round( LayouUtil.getDimen("y5"));
//                    musicTagSide = Math.round( LayouUtil.getDimen("y22"));
//                    shockPriceSize = Math.round( LayouUtil.getDimen("y67"));
//                    shockPriceHeight = shockPriceSize + 12;
//                    shockNameSize = Math.round( LayouUtil.getDimen("y26"));
//                }
//                break;
//            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
//                if (isVertScreen){
//                    unit = Math.round(LayouUtil.getDimen("x10"));
//                    h0 = Math.round(LayouUtil.getDimen("x38"));
//                    h1 = Math.round( LayouUtil.getDimen("x33"));
//                    h2 = Math.round( LayouUtil.getDimen("x31"));
//                    h3 = Math.round( LayouUtil.getDimen("x29"));
//                    h4 = Math.round( LayouUtil.getDimen("x27"));
//                    h5 = Math.round( LayouUtil.getDimen("x25"));
//                    h6 = Math.round( LayouUtil.getDimen("x23"));
//                    h7 = Math.round( LayouUtil.getDimen("x21"));
//                    billWidth = Math.round( LayouUtil.getDimen("x267"));
//                    billWidthHalf = Math.round( LayouUtil.getDimen("x265"));
//                    billWidthNone = Math.round( LayouUtil.getDimen("x208"));
//                    centerInterval = Math.round( LayouUtil.getDimen("x6"));
//                    musicTagSide = Math.round( LayouUtil.getDimen("x29"));
//                    shockPriceSize = Math.round( LayouUtil.getDimen("x66"));
//                    shockPriceHeight = shockPriceSize + 12;
//                    shockNameSize = Math.round( LayouUtil.getDimen("x25"));
//                }else {
//                    unit = Math.round(LayouUtil.getDimen("y8"));
//                    h0 = Math.round( LayouUtil.getDimen("y27"));
//                    h1 = Math.round( LayouUtil.getDimen("y24"));
//                    h2 = Math.round( LayouUtil.getDimen("y22"));
//                    h3 = Math.round( LayouUtil.getDimen("y21"));
//                    h4 = Math.round( LayouUtil.getDimen("y19"));
//                    h5 = Math.round( LayouUtil.getDimen("y18"));
//                    h6 = Math.round( LayouUtil.getDimen("y16"));
//                    h7 = Math.round( LayouUtil.getDimen("y14"));
//                    billWidth = Math.round( LayouUtil.getDimen("y163"));
//                    billWidthHalf = Math.round( LayouUtil.getDimen("y160"));
//                    billWidthNone = Math.round( LayouUtil.getDimen("y131"));
//                    centerInterval = Math.round( LayouUtil.getDimen("y3"));
//                    musicTagSide = Math.round( LayouUtil.getDimen("y20"));
//                    shockPriceSize = Math.round( LayouUtil.getDimen("y54"));
//                    shockPriceHeight = shockPriceSize + 12;
//                    shockNameSize = Math.round( LayouUtil.getDimen("y22"));
//                }
//                break;
//            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
//                if (isVertScreen){
//                    unit = Math.round(LayouUtil.getDimen("x10"));
//                    h0 = Math.round(LayouUtil.getDimen("x35"));
//                    h1 = Math.round( LayouUtil.getDimen("x31"));
//                    h2 = Math.round( LayouUtil.getDimen("x29"));
//                    h3 = Math.round( LayouUtil.getDimen("x27"));
//                    h4 = Math.round( LayouUtil.getDimen("x25"));
//                    h5 = Math.round( LayouUtil.getDimen("x23"));
//                    h6 = Math.round( LayouUtil.getDimen("x21"));
//                    h7 = Math.round( LayouUtil.getDimen("x19"));
//                    billWidth = Math.round( LayouUtil.getDimen("x267"));
//                    billWidthHalf = Math.round( LayouUtil.getDimen("x265"));
//                    billWidthNone = Math.round( LayouUtil.getDimen("x180"));
//                    centerInterval = Math.round( LayouUtil.getDimen("x6"));
//                    musicTagSide = Math.round( LayouUtil.getDimen("x21"));
//                    shockPriceSize = Math.round( LayouUtil.getDimen("x53"));
//                    shockPriceHeight = shockPriceSize + 12;
//                    shockNameSize = Math.round( LayouUtil.getDimen("x22"));
//                }else {
//                    unit = Math.round(LayouUtil.getDimen("y8"));
//                    h0 = Math.round( LayouUtil.getDimen("y27"));
//                    h1 = Math.round( LayouUtil.getDimen("y24"));
//                    h2 = Math.round( LayouUtil.getDimen("y22"));
//                    h3 = Math.round( LayouUtil.getDimen("y21"));
//                    h4 = Math.round( LayouUtil.getDimen("y19"));
//                    h5 = Math.round( LayouUtil.getDimen("y18"));
//                    h6 = Math.round( LayouUtil.getDimen("y16"));
//                    h7 = Math.round( LayouUtil.getDimen("y14"));
//                    billWidth = Math.round( LayouUtil.getDimen("y163"));
//                    billWidthHalf = Math.round( LayouUtil.getDimen("y160"));
//                    billWidthNone = Math.round( LayouUtil.getDimen("y131"));
//                    centerInterval = Math.round( LayouUtil.getDimen("y3"));
//                    musicTagSide = Math.round( LayouUtil.getDimen("y20"));
//                    shockPriceSize = Math.round( LayouUtil.getDimen("y54"));
//                    shockPriceHeight = shockPriceSize + 12;
//                    shockNameSize = Math.round( LayouUtil.getDimen("y22"));
//                }
//                break;
//            default:
//                break;
//        }
//        h1Height = h1 + 12;
//        h2Height = h2 + 12;
//        h3Height = h3 + 12;
//        h4Height = h4 + 10;
//        h5Height = h5 + 10;
//        h6Height = h6 + 10;
//        h7Height = h7 + 8;
//
//    }

    //直接根据分辨率计算，避免没有加载到对应的dimens。
    // 而且用这样的方式的，在配置文件设置了“screenWidthDp”和“screenHeightDp”之后，
    // 不需要再往core里面加入100000增量的dimen文件夹。
    public void updateViewParams(boolean isVertScreen){
        x = SizeConfig.screenWidth / 800.00f;
        y = SizeConfig.screenHeight / 480.00f;
        LogUtil.logd(WinLayout.logTag+ "initScreen: base x--"+x+"  y--"+y);
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                if (isVertScreen){
                    unit = Math.round(10 * x);
                    h0 = Math.round(38 * x);
                    h1 = Math.round(33 * x);
                    h2 = Math.round(31 * x);
                    h3 = Math.round(29 * x);
                    h4 = Math.round(27 * x);
                    h5 = Math.round(25 * x);
                    h6 = Math.round(23 * x);
                    h7 = Math.round(21 * x);
                    billWidth = Math.round(267 * x);
                    billWidthHalf = Math.round(265 * x);
                    billWidthNone = Math.round(208 * x);
                    centerInterval = Math.round(6 * x);
                    musicTagSide = Math.round(29 * x);
                    shockPriceSize = Math.round(66 * x);
                    shockPriceHeight = shockPriceSize + 12;
                    shockNameSize = Math.round(25 * x);
                }else {
                    unit = Math.round(8 * y);
                    h0 = Math.round(29 * y);
                    h1 = Math.round(26 * y);
                    h2 = Math.round(24 * y);
                    h3 = Math.round(22 * y);
                    h4 = Math.round(21 * y);
                    h5 = Math.round(19 * y);
                    h6 = Math.round(18 * y);
                    h7 = Math.round(16 * y);
                    billWidth = Math.round(163 * y);
                    billWidthHalf = Math.round(158 * y);
                    billWidthNone = Math.round(131 * y);
                    centerInterval = Math.round(5 * y);
                    musicTagSide = Math.round(22 * y);
                    shockPriceSize = Math.round(67 * y);
                    shockPriceHeight = shockPriceSize + 12;
                    shockNameSize = Math.round(26 * y);
                }
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                if (isVertScreen){
                    unit = Math.round(10 * x);
                    h0 = Math.round(38 * x);
                    h1 = Math.round(33 * x);
                    h2 = Math.round(31 * x);
                    h3 = Math.round(29 * x);
                    h4 = Math.round(27 * x);
                    h5 = Math.round(25 * x);
                    h6 = Math.round(23 * x);
                    h7 = Math.round(21 * x);
                    billWidth = Math.round(267 * x);
                    billWidthHalf = Math.round(265 * x);
                    billWidthNone = Math.round(208 * x);
                    centerInterval = Math.round(6 * x);
                    musicTagSide = Math.round(29 * x);
                    shockPriceSize = Math.round(66 * x);
                    shockPriceHeight = shockPriceSize + 12;
                    shockNameSize = Math.round(25 * x);
                }else {
                    unit = Math.round(8 * y);
                    h0 = Math.round(27 * y);
                    h1 = Math.round(24 * y);
                    h2 = Math.round(22 * y);
                    h3 = Math.round(21 * y);
                    h4 = Math.round(19 * y);
                    h5 = Math.round(18 * y);
                    h6 = Math.round(16 * y);
                    h7 = Math.round(14 * y);
                    billWidth = Math.round(163 * y);
                    billWidthHalf = Math.round(160 * y);
                    billWidthNone = Math.round(131 * y);
                    centerInterval = Math.round(3 * y);
                    musicTagSide = Math.round(20 * y);
                    shockPriceSize = Math.round(54 * y);
                    shockPriceHeight = shockPriceSize + 12;
                    shockNameSize = Math.round(22 * y);
                }
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                if (isVertScreen){
                    unit = Math.round(10 * x);
                    h0 = Math.round(35 * x);
                    h1 = Math.round(31 * x);
                    h2 = Math.round(29 * x);
                    h3 = Math.round(27 * x);
                    h4 = Math.round(25 * x);
                    h5 = Math.round(23 * x);
                    h6 = Math.round(21 * x);
                    h7 = Math.round(19 * x);
                    billWidth = Math.round(267 * x);
                    billWidthHalf = Math.round(265 * x);
                    billWidthNone = Math.round(180 * x);
                    centerInterval = Math.round(6 * x);
                    musicTagSide = Math.round(21 * x);
                    shockPriceSize = Math.round(53 * x);
                    shockPriceHeight = shockPriceSize + 12;
                    shockNameSize = Math.round(22 * x);
                }else {
                    unit = Math.round(8 * y);
                    h0 = Math.round(27 * y);
                    h1 = Math.round(24 * y);
                    h2 = Math.round(22 * y);
                    h3 = Math.round(21 * y);
                    h4 = Math.round(19 * y);
                    h5 = Math.round(18 * y);
                    h6 = Math.round(16 * y);
                    h7 = Math.round(14 * y);
                    billWidth = Math.round(163 * y);
                    billWidthHalf = Math.round(160 * y);
                    billWidthNone = Math.round(131 * y);
                    centerInterval = Math.round(3 * y);
                    musicTagSide = Math.round(20 * y);
                    shockPriceSize = Math.round(54 * y);
                    shockPriceHeight = shockPriceSize + 12;
                    shockNameSize = Math.round(22 * y);
                }
                break;
            default:
                break;
        }
        h1Height = h1 + 12;
        h2Height = h2 + 12;
        h3Height = h3 + 12;
        h4Height = h4 + 10;
        h5Height = h5 + 10;
        h6Height = h6 + 10;
        h7Height = h7 + 8;

    }

    //基于分辨率计算dimen
    public static float getDimen(String dimen){
        float f = 0.0f;
        if (dimen.startsWith("x")){
            dimen = dimen.substring(1);
            f = x * Integer.parseInt(dimen);
        }
        else if (dimen.startsWith("y")){
            dimen = dimen.substring(1);
            f = y * Integer.parseInt(dimen);
        }
        LogUtil.logd(WinLayout.logTag+ "dimen--"+dimen+"--getDimen"+f);
        return f;
    }

}
