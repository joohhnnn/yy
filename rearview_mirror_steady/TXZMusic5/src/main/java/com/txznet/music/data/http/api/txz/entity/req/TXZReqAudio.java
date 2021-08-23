package com.txznet.music.data.http.api.txz.entity.req;

import com.txznet.music.config.Configuration;
import com.txznet.txz.util.TXZFileConfigUtil;

public class TXZReqAudio extends TXZReqBase {
    public int sid; // 原id, 如1：qq音乐等，必填
    public long id; // 	专辑id，必填
    //    public int pageId = 0; // 页码 默认是1，必填 【请求小说分类，pageId只能是0，不然没有断点返回！！】
    public int offset; // 每页返回多少数量， 默认是10
    public int orderType = 0; // 排序方式 ， 默认0 按数量
    //    public long categoryId; // 分类id ，必填【佳明说可以不带，在5.0的版本】
    public int up; // 拉取方式 默认 0 向下 1 向上
    public int version; // 版本
    public int type; // 	默认0:音频内专辑排序 1:上次收听+专辑内排序，2 重头开始排序
    public long audioId; // 从某个audio开始，搜索辅助，可选
    public long audioSid; // 从某个audio开始，搜索辅助，可选
    public long txz_album_id; // 从某个audio开始，搜索辅助，可选

    public String svrData;

    public TXZReqAudio() {
        version = TXZFileConfigUtil.getIntSingleConfig(Configuration.Key.TXZ_AUDIO_VERSION, Configuration.DefVal.AUDIO_VERSION);
        offset = Configuration.DefVal.PAGE_COUNT;
    }
}
