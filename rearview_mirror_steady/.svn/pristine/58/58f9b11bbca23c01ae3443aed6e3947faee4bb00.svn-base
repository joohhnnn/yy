package com.txznet.music.albumModule.logic.net.response;

import com.txznet.music.albumModule.bean.InterestTag;
import com.txznet.music.data.http.resp.BaseResponse;

import java.util.List;

/**
 * Created by telen on 2018/5/11.
 */

public class ResponseInterestTag extends BaseResponse {

    public static final int IS_SETTING = 1;//兴趣标签，已经设置过了
    public static final int SUCCESS = 0;//成功返回兴趣标签

    private List<InterestTag> data;
    private int code;//取值为IS_SETTING和SUCCESS

    public List<InterestTag> getData() {
        return data;
    }

    public void setData(List<InterestTag> data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
