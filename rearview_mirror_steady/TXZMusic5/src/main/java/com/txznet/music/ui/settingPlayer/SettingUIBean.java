package com.txznet.music.ui.settingPlayer;

/**
 * @author telen
 * @date 2019/1/4,15:10
 */
public class SettingUIBean {


    public final static int TYPE_SHOW = 1;
    public final static int TYPE_SHOW_WHEN = 2;
    public final static int TYPE_DISMISS = 3;
    int type;
    public String title;
    public String subTitle;

//    public boolean isCheck;

    public SettingUIBean(int type, String title, String subTitle) {
        this.type = type;
        this.title = title;
        this.subTitle = subTitle;
    }

    @Override
    public String toString() {
        return "SettingUIBean{" +
                "title='" + title + '\'' +
                ", subTitle='" + subTitle + '\'' +
                '}';
    }
}
