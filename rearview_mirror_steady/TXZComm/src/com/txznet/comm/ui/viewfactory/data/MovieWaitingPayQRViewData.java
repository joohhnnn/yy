package com.txznet.comm.ui.viewfactory.data;

import java.util.List;

public class MovieWaitingPayQRViewData extends ViewData{

    public String WXPayURL;
    public String ZFBPayURL;
    public String phoneNum;
    public String replacePhoneUrl;
    public String vTips;
    public String moiveName;
    public String cinemaName;
    public String showTime;
    public String showVersion;
    public String hallName;
    public List<String> seats;

    public MovieWaitingPayQRViewData() {
        super(TYPE_FULL_MOVIE_WAITING_PAY_QRCODE);
    }

}
