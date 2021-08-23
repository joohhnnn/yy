package com.txznet.music.favor.bean;

import java.util.List;

/**
 * Created by telenewbie on 2017/12/9.
 */

public class ReqBesendBean {
    private List<BeSendBean> arr_store_oper;

    public List<BeSendBean> getArr_store_oper() {
        return arr_store_oper;
    }

    public void setArr_store_oper(List<BeSendBean> arr_store_oper) {
        this.arr_store_oper = arr_store_oper;
    }


    @Override
    public String toString() {
        return "ReqBesendBean{" +
                "arr_store_oper=" + arr_store_oper +
                '}';
    }
}
