package com.txznet.music.data.http.api.txz.entity.resp;

import com.txznet.music.data.http.api.txz.entity.TXZAudio;

import java.util.List;

/**
 * @author zackzhou
 * @date 2019/2/1,17:03
 */

public class AiPullData {
    public static final int PRE_ACTION_NONE = 0;
    public static final int PRE_ACTION_CLEAR_QUEUE = 4;

    public static final int ACTION_TYPE_INSERT_HEAD = 1; // 插入到头部
    public static final int ACTION_TYPE_INSERT_FOOT = 2; // 插入到尾部
    public static final int ACTION_TYPE_PLAY_NOW = 3; // 替换正在播放的

    /**
     * preAction : 4
     * action : 1
     * postAction : 0
     * arrAudios : [{"sid":2,"id":649428,"name":"故事的第一行","albumId":28,"duration":0,"arrArtistName":[],"artist_ids":["267"],"bShowSource":true,"bNoCache":false,"downloadType":1,"strDownloadUrl":"C400003rjWoO32yeUH","strProcessingUrl":"http://c.y.qq.com/base/fcgi-bin/fcg_music_express_mobile3.fcg?format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&cid=205361747&songmid=003rjWoO32yeUH&filename=C400003rjWoO32yeUH.m4a&guid=04bef34a5eff3822def710f2ea339051","report":"故事的第一行","sourceFrom":"QQ音乐","score":100,"urlType":2,"flag":10,"wakeUp":[]},{"sid":2,"id":649982,"name":"展翅天空","albumId":28,"duration":0,"arrArtistName":[],"artist_ids":["15798"],"bShowSource":true,"bNoCache":false,"downloadType":1,"strDownloadUrl":"C400003ua0K31qCFPI","strProcessingUrl":"http://c.y.qq.com/base/fcgi-bin/fcg_music_express_mobile3.fcg?format=json&inCharset=utf8&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0&cid=205361747&songmid=003ua0K31qCFPI&filename=C400003ua0K31qCFPI.m4a&guid=04bef34a5eff3822def710f2ea339051","report":"展翅天空","sourceFrom":"QQ音乐","score":100,"urlType":2,"flag":10,"wakeUp":[]}]
     */

    public int preAction;
    public int action;
    public int postAction;
    public List<TXZAudio> arrAudios;

    @Override
    public String toString() {
        return "AiPullData{" +
                "preAction=" + preAction +
                ", action=" + action +
                ", postAction=" + postAction +
                ", arrAudios=" + arrAudios +
                '}';
    }
}
