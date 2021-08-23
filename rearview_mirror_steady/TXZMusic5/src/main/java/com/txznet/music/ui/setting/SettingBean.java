package com.txznet.music.ui.setting;

/**
 * 设置的选项样式
 *
 * @author telen
 * @date 2018/12/12,17:36
 */
public class SettingBean implements IGetType {

    public static final int STYLE_CHOICE = 1;
    public static final int STYLE_TEXT = 2;
    public static final int STYLE_ARROW = 3;
    public static final int STYLE_TWO_LINE_ARROW = 4;
    public static final int DEFAULT_VALUE = -1;

    //样式,1,有开关选项 2, 左右文本  3, 文本加箭头
    public int style;

    public String leftText;
    public String rightText;
    public String subText;
    public int rightIconId = DEFAULT_VALUE;

    public boolean choice;
    public OnClickSettingListener listener = null;

    @Override
    public int getStyle() {
        return style;
    }


    public interface OnClickSettingListener {
        void onClick(int postion, SettingBean bean);
    }

    public OnClickSettingListener getListener() {
        return listener;
    }

    public SettingBean(int style, String leftText, String rightText, OnClickSettingListener listener) {
        this.leftText = leftText;
        this.rightText = rightText;
        this.listener = listener;
        this.style = style;
    }

    public SettingBean(String leftText, String rightText, OnClickSettingListener listener) {
        this.leftText = leftText;
        this.rightText = rightText;
        this.listener = listener;
        style = STYLE_TEXT;
    }


    public SettingBean(String leftText, boolean choice, OnClickSettingListener listener) {
        this.leftText = leftText;
        this.choice = choice;
        this.subText = null;
        this.listener = listener;
        style = STYLE_CHOICE;
    }

    public SettingBean(String leftText, String subText, boolean choice, OnClickSettingListener listener) {
        this.leftText = leftText;
        this.subText = subText;
        this.choice = choice;
        this.listener = listener;
        style = STYLE_CHOICE;
    }

    public SettingBean(String leftText, String subText, int style, OnClickSettingListener listener) {
        this.leftText = leftText;
        this.subText = subText;
        this.listener = listener;
        this.style = style;
    }

    public SettingBean(String leftText, OnClickSettingListener listener) {
        this.leftText = leftText;
        this.listener = listener;
        style = STYLE_ARROW;
    }


    @Override
    public String toString() {
        return "SettingBean{" +
                "style=" + style +
                ", leftText='" + leftText + '\'' +
                ", rightText='" + rightText + '\'' +
                ", choice=" + choice +
                '}';
    }

}
