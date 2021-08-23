package com.txznet.comm.ui.viewfactory.data;

public class AuthorizationViewData extends ViewData {
    public static final String KEY_URL = "url";
    public static final String KEY_TITLE = "title";
    public static final String KEY_SUB_TITLE = "sub_title";
    public static final String KEY_TIPS = "tips";
    public static final String KEY_VIEW_TIPS = "vTips";
    public String mTitle;
    public String mTips;
    //界面引导语
    public String vTips;
    /**
     * 二维码授权链接
     */
    public String mUrl;
    public String mSubTitle;

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public void setTips(String tips) {
        this.mTips = tips;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    public void setSubTitle(String subTitle) {
        this.mSubTitle = subTitle;
    }

    public AuthorizationViewData() {
        super(TYPE_AUTHORIZATION_VIEW);
    }

    public void setvTips(String vTips) {
        this.vTips = vTips;
    }
}
