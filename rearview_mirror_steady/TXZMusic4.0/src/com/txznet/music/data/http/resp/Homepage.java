package com.txznet.music.data.http.resp;

import java.util.List;

public class Homepage<T> extends BaseResponse {
    private int reqType; // 请求类型 0为全部分类
    private List<T> arrCategory; // 分类列表

    public List<T> getArrCategory() {
        return arrCategory;
    }

    public void setArrCategory(List<T> arrCategory) {
        this.arrCategory = arrCategory;
    }

    public int getReqType() {
        return reqType;
    }

    public void setReqType(int reqType) {
        this.reqType = reqType;
    }

    @Override
    public String toString() {
        return "Homepage [reqType=" + reqType + ", errCode=" + errCode
                + ", arrCategory=" + arrCategory.toString() + "]";
    }

}
