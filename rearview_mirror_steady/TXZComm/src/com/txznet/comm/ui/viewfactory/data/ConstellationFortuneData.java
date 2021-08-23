package com.txznet.comm.ui.viewfactory.data;

public class ConstellationFortuneData extends ViewData {
    public int level;
    public String name;
    public String fortuneType;
    public String desc;
    public String vTips;
    public ConstellationFortuneData() {
        super(ViewData.TYPE_CHAT_CONSTELLATION_FORTUNE);
    }
}
