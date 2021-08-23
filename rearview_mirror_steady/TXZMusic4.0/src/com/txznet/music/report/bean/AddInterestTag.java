package com.txznet.music.report.bean;

import com.txznet.music.report.ReportEventConst;

import java.util.List;

/**
 * Created by 58295 on 2018/5/4.
 */

public class AddInterestTag extends EventBase {
    public List<Integer> tagsId;
    public String show;
    public String action;
    public AddInterestTag(List<Integer> tagsId, String show, String action) {
        super(ReportEventConst.ADD_INTEREST_TAG);
        this.tagsId = tagsId;
        this.show = show;
        this.action = action;
    }
}
