package com.txznet.music.historyModule.bean;

import com.txznet.music.FavourModule.adapter.ItemAudioAdapter;

/**
 * Created by telenewbie on 2017/12/23.
 */

public class SettingBean {

    public static final int STYLE_CHOICE = 1;
    public static final int STYLE_TEXT = 2;
    public static final int STYLE_ARROW = 31;


    private int style;//样式,1,有开关选项 2, 左右文本  3, 文本加箭头

    private String leftText;
    private String rightText;

    private boolean choice;
    private ItemAudioAdapter.OnItemClickListener listener = null;


    public ItemAudioAdapter.OnItemClickListener getListener() {
        return listener;
    }

    public SettingBean(String leftText, String rightText, ItemAudioAdapter.OnItemClickListener listener) {
        this.leftText = leftText;
        this.rightText = rightText;
        this.listener = listener;
        style = STYLE_TEXT;
    }

    public SettingBean(String leftText, boolean choice, ItemAudioAdapter.OnItemClickListener listener) {
        this.leftText = leftText;
        this.choice = choice;
        this.listener = listener;
        style = STYLE_CHOICE;
    }

    public SettingBean(String leftText, ItemAudioAdapter.OnItemClickListener listener) {
        this.leftText = leftText;
        this.listener = listener;
        style = STYLE_ARROW;
    }

    public int getStyle() {
        return style;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public String getLeftText() {
        return leftText;
    }

    public void setLeftText(String leftText) {
        this.leftText = leftText;
    }

    public String getRightText() {
        return rightText;
    }

    public void setRightText(String rightText) {
        this.rightText = rightText;
    }

    public boolean isChoice() {
        return choice;
    }

    public void setChoice(boolean choice) {
        this.choice = choice;
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
