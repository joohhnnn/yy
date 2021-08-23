package com.txznet.music.data.http.resp;

/**
 * 网络请求返回基类
 * <p>
 * Created by Terry on 2017/7/20.
 */

public abstract class BaseResponse {

    protected int errCode; // 成功为0，不成功则为错误码


    public int getErrCode() {
        return errCode;
    }

    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }
}
