package com.txznet.music.albumModule.logic.net.request;

import java.util.List;

/**
 * Created by telen on 2018/5/11.
 */

public class ReqInterestTag {
    public static final String GET = "get";
    public static final String SET = "set";


    private String action;
    private List<Integer> tagIds;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<Integer> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Integer> tagIds) {
        this.tagIds = tagIds;
    }
}
