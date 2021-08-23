package com.txznet.music.data.http.api.txz.entity.resp;

import com.txznet.music.data.http.api.txz.entity.TXZCategory;

import java.util.List;

public class TXZRespCategory extends TXZRespBase {
    public int reqType;
    public List<TXZCategory> arrCategory;
}
