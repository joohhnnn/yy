package com.txznet.music.data.http.api.txz.entity.resp;

import com.txznet.music.data.http.api.txz.entity.TXZAudio;
import com.txznet.music.util.AlbumUtils;

import java.util.List;

public class TXZRespAudio extends TXZRespBase {

    public int sid;
    public long id;
    public int offset;
    public int orderType;
    @Deprecated
    public int totalNum;
    @Deprecated
    public int totalPage; // 后台返回可能为null
    @Deprecated
    public int categoryId; // 后台不返回
    public List<TXZAudio> arrAudio;
    public int flag;
    public Integer field; //专辑的类别，不一定会返回

    /**
     * 是否需要客户端排序
     */
    public boolean needSort() {
        return AlbumUtils.getNumInPosition(flag, 0) == 1;
    }
}
