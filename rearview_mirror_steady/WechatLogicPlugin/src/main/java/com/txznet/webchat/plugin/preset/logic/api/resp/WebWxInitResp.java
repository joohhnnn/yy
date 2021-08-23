package com.txznet.webchat.plugin.preset.logic.api.resp;

import java.util.List;

public class WebWxInitResp {

    /**
     * Ret : 0
     * ErrMsg :
     */

    public BaseResponseEntity BaseResponse;
    /**
     * BaseResponse : {"Ret":0,"ErrMsg":""}
     * Count : 1
     * ContactList : [{"Uin":0,"UserName":"@@79c685e1d6520f9af1f20914fd4dd30acb49867e0972c7b33fe2db12c399a779","NickName":"同行者","HeadImgUrl":"/cgi-bin/mmwebwx-bin/webwxgetheadimg?seq=639270491&username=@@79c685e1d6520f9af1f20914fd4dd30acb49867e0972c7b33fe2db12c399a779&skey=@crypt_facb94d1_5184fca7903f2ca9c1d8e73ee9026e24","ContactFlag":2,"MemberCount":1,"MemberList":[{"Uin":605113600,"UserName":"@992d843c3a9160648d653656ed98f26a","NickName":"","AttrStatus":0,"PYInitial":"","PYQuanPin":"","RemarkPYInitial":"","RemarkPYQuanPin":"","MemberStatus":0,"DisplayName":"","KeyWord":"liu"}],"RemarkName":"","HideInputBarFlag":0,"Sex":0,"Signature":"","VerifyFlag":0,"OwnerUin":628027320,"PYInitial":"","PYQuanPin":"","RemarkPYInitial":"","RemarkPYQuanPin":"","StarFriend":0,"AppAccountFlag":0,"Statues":1,"AttrStatus":0,"Province":"","City":"","Alias":"","SnsFlag":0,"UniFriend":0,"DisplayName":"","ChatRoomId":0,"KeyWord":"","EncryChatRoomId":""}]
     * SyncKey : {"Count":4,"List":[{"Key":1,"Val":639270462},{"Key":2,"Val":639270541},{"Key":3,"Val":639270456},{"Key":1000,"Val":1449797324}]}
     * User : {"Uin":1552368429,"UserName":"@f9fe7a1124fa4b175feffefe797062c2e34cb3f45e216aedeee54a67345cfb31","NickName":"咕嘿嘿","HeadImgUrl":"/cgi-bin/mmwebwx-bin/webwxgeticon?seq=1749720496&username=@f9fe7a1124fa4b175feffefe797062c2e34cb3f45e216aedeee54a67345cfb31&skey=@crypt_facb94d1_5184fca7903f2ca9c1d8e73ee9026e24","RemarkName":"","PYInitial":"","PYQuanPin":"","RemarkPYInitial":"","RemarkPYQuanPin":"","HideInputBarFlag":0,"StarFriend":0,"Sex":1,"Signature":"","AppAccountFlag":0,"VerifyFlag":0,"ContactFlag":0,"WebWxPluginSwitch":0,"HeadImgFlag":1,"SnsFlag":1}
     * ChatSet : filehelper,@@79c685e1d6520f9af1f20914fd4dd30acb49867e0972c7b33fe2db12c399a779,weixin,@@9c964b009ff9eb1193923e186a1d22d5f9ca040034daeccc5eda8f01ad185452,@9b156bf8e0390f702f1dfdb0f98187edd767df0698d3d4a297496738207f288e,@2053dad8c7935569a45c59794b1f206000e238a9f9f27e8fc99d8cb312221990,fmessage,@bed560a01f1a94c93dbf7f60e29b276c,
     * SKey : @crypt_facb94d1_5184fca7903f2ca9c1d8e73ee9026e24
     * ClientVersion : 637732914
     * SystemTime : 1449821952
     * GrayScale : 1
     * InviteStartCount : 40
     * MPSubscribeMsgCount : 1
     * MPSubscribeMsgList : [{"UserName":"@bed560a01f1a94c93dbf7f60e29b276c","MPArticleCount":1,"MPArticleList":[{"Title":"Android 6.0.1带来emoji表情｜PS4国行携手电信提供专线","Digest":"Android 6.0.1带来的新的emoji表情，表情控们欢呼吧！苹果送给员工的圣诞礼物居然是...哎哟，","Cover":"http://mmbiz.qpic.cn/mmbiz/0OaBWOA04MIpBgdOIvnqRibaibKFLPt5a0hUtsyPw3pjezYYFqg6EC4Tmpgt8VStpYVmIYZrTeP0c1hrwOBuj6ibg/300?wxtype=jpeg&wxfrom=0|0|0","Url":"http://mp.weixin.qq.com/s?__biz=MjM5OTAwODk0MA==&mid=401156768&idx=5&sn=2364664a5081fbc32d3552aa096cf39e&scene=0#rd"}],"Time":1449583566,"NickName":"安卓论坛"}]
     * ClickReportInterval : 600000
     */

    public int Count;
    /**
     * Count : 4
     * List : [{"Key":1,"Val":639270462},{"Key":2,"Val":639270541},{"Key":3,"Val":639270456},{"Key":1000,"Val":1449797324}]
     */

    public SyncKeyEntity SyncKey;
    /**
     * Uin : 1552368429
     * UserName : @f9fe7a1124fa4b175feffefe797062c2e34cb3f45e216aedeee54a67345cfb31
     * NickName : 咕嘿嘿
     * HeadImgUrl : /cgi-bin/mmwebwx-bin/webwxgeticon?seq=1749720496&username=@f9fe7a1124fa4b175feffefe797062c2e34cb3f45e216aedeee54a67345cfb31&skey=@crypt_facb94d1_5184fca7903f2ca9c1d8e73ee9026e24
     * RemarkName :
     * PYInitial :
     * PYQuanPin :
     * RemarkPYInitial :
     * RemarkPYQuanPin :
     * HideInputBarFlag : 0
     * StarFriend : 0
     * Sex : 1
     * Signature :
     * AppAccountFlag : 0
     * VerifyFlag : 0
     * ContactFlag : 0
     * WebWxPluginSwitch : 0
     * HeadImgFlag : 1
     * SnsFlag : 1
     */

    public ContactEntity User;
    public String ChatSet;
    public String SKey;
    public long ClientVersion;
    public long SystemTime;
    public int GrayScale;
    public int InviteStartCount;
    public int MPSubscribeMsgCount;
    public int ClickReportInterval;


    public List<ContactEntity> ContactList;
    /**
     * UserName : @bed560a01f1a94c93dbf7f60e29b276c
     * MPArticleCount : 1
     * MPArticleList : [{"Title":"Android 6.0.1带来emoji表情｜PS4国行携手电信提供专线","Digest":"Android 6.0.1带来的新的emoji表情，表情控们欢呼吧！苹果送给员工的圣诞礼物居然是...哎哟，","Cover":"http://mmbiz.qpic.cn/mmbiz/0OaBWOA04MIpBgdOIvnqRibaibKFLPt5a0hUtsyPw3pjezYYFqg6EC4Tmpgt8VStpYVmIYZrTeP0c1hrwOBuj6ibg/300?wxtype=jpeg&wxfrom=0|0|0","Url":"http://mp.weixin.qq.com/s?__biz=MjM5OTAwODk0MA==&mid=401156768&idx=5&sn=2364664a5081fbc32d3552aa096cf39e&scene=0#rd"}]
     * Time : 1449583566
     * NickName : 安卓论坛
     */

    public List<MPSubscribeMsgListEntity> MPSubscribeMsgList;

    public static class SyncKeyEntity {
        public int Count;
        /**
         * Key : 1
         * Val : 639270462
         */

        public java.util.List<ListEntity> List;

        public static class ListEntity {
            public int Key;
            public int Val;
        }
    }

    public static class MPSubscribeMsgListEntity {
        public String UserName;
        public int MPArticleCount;
        public int Time;
        public String NickName;
        /**
         * Title : Android 6.0.1带来emoji表情｜PS4国行携手电信提供专线
         * Digest : Android 6.0.1带来的新的emoji表情，表情控们欢呼吧！苹果送给员工的圣诞礼物居然是...哎哟，
         * Cover : http://mmbiz.qpic.cn/mmbiz/0OaBWOA04MIpBgdOIvnqRibaibKFLPt5a0hUtsyPw3pjezYYFqg6EC4Tmpgt8VStpYVmIYZrTeP0c1hrwOBuj6ibg/300?wxtype=jpeg&wxfrom=0|0|0
         * Url : http://mp.weixin.qq.com/s?__biz=MjM5OTAwODk0MA==&mid=401156768&idx=5&sn=2364664a5081fbc32d3552aa096cf39e&scene=0#rd
         */

        public List<MPArticleListEntity> MPArticleList;

        public static class MPArticleListEntity {
            public String Title;
            public String Digest;
            public String Cover;
            public String Url;
        }
    }
}
