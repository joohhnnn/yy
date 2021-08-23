package com.txznet.music.report.bean;

import com.txznet.music.report.ReportEventConst;

/**
 * Created by 58295 on 2018/5/4.
 */

public class ClickThemePlaylist extends EventBase {
    public ClickThemePlaylist() {
        super(ReportEventConst.CLICK_TST_PLAYLIST);
    }
}
