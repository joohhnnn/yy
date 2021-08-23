package com.txznet.txz.module.advertising;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.sp.CommonSp;

import java.text.SimpleDateFormat;
import java.util.Date;

public class AdvertisingSp extends CommonSp {
    private static AdvertisingSp sInstance;
    private static final String SP_NAME = "advertising";
    private static final String OPEN_AD_SHOW_NUM = "open_ad_show_num";
    private static final String OPEN_LAST_TIME = "open_ad_last_time";
    private static final String BANNER_AD_SHOW_NUM = "banner_ad_show_num";
    private static final String BACKGROUND_AD_SHOW_NUM = "background_ad_show_num";
    private static final String TIME = "time";
    private static final String USER_AGENT = "user_agent";

    private AdvertisingSp() {
        super(GlobalContext.get(), SP_NAME);
    }

    public static AdvertisingSp getInstance() {
        if (sInstance == null) {
            synchronized (AdvertisingSp.class) {
                if (sInstance == null) {
                    sInstance = new AdvertisingSp();
                }
            }
        }
        return sInstance;
    }

    public void setBannerShowNum(int num) {
        setValue(BANNER_AD_SHOW_NUM, num);
    }

    public int getBannerShowNum() {
        if(!checkIsToday()){
            clearStatus();
        }
        return getValue(BANNER_AD_SHOW_NUM, 0);
    }

    public void setBackgroundShowNum(int num) {
        setValue(BACKGROUND_AD_SHOW_NUM, num);
    }

    public int getBackgroundShowNum() {
        if(!checkIsToday()){
            clearStatus();
        }
        return getValue(BACKGROUND_AD_SHOW_NUM, 0);
    }


    public void setOpenShowNum(int num) {
        setValue(OPEN_AD_SHOW_NUM, num);

    }

    public int getOpenShowNum() {
        if(!checkIsToday()){
            clearStatus();
        }
        return getValue(OPEN_AD_SHOW_NUM, 0);
    }

    public void setOpenLastTime(long time) {
        setValue(OPEN_LAST_TIME, time);
    }

    public long getOpenLastTime() {
        return getValue(OPEN_LAST_TIME, 0L);
    }

    public void setUserAgent(String userAgent){
        setValue(USER_AGENT,userAgent);
    }

    public String getUserAgent(){
        return getValue(USER_AGENT,"");
    }

    /**
     * 检查之前保存的记录是否是今天的
     * @return
     */
    private boolean checkIsToday(){
        String dateStr = getValue(TIME,"");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String data = simpleDateFormat.format(new Date());
        if(!data.equals(dateStr)){
            return false;
        }
        return true;
    }

    private void setTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String data = simpleDateFormat.format(new Date());
        setValue(TIME,data);
    }

    /**
     * 清空广告展示次数
     */
    private void clearStatus(){
        setTime();
        setOpenShowNum(0);
        setBannerShowNum(0);
        setBackgroundShowNum(0);
    }


}
