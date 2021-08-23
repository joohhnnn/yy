package com.txznet.webchat.plugin.preset.logic.api.resp;

import java.util.List;

public class ContactEntity {
    /**
     * Uin : 0
     * UserName : @@79c685e1d6520f9af1f20914fd4dd30acb49867e0972c7b33fe2db12c399a779
     * NickName : 同行者
     * HeadImgUrl : /cgi-bin/mmwebwx-bin/webwxgetheadimg?seq=639270491&username=@@79c685e1d6520f9af1f20914fd4dd30acb49867e0972c7b33fe2db12c399a779&skey=@crypt_facb94d1_5184fca7903f2ca9c1d8e73ee9026e24
     * ContactFlag : 2
     * MemberCount : 1
     * MemberList : [{"Uin":605113600,"UserName":"@992d843c3a9160648d653656ed98f26a","NickName":"","AttrStatus":0,"PYInitial":"","PYQuanPin":"","RemarkPYInitial":"","RemarkPYQuanPin":"","MemberStatus":0,"DisplayName":"","KeyWord":"liu"}]
     * RemarkName :
     * HideInputBarFlag : 0
     * Sex : 0
     * Signature :
     * VerifyFlag : 0
     * OwnerUin : 628027320
     * PYInitial :
     * PYQuanPin :
     * RemarkPYInitial :
     * RemarkPYQuanPin :
     * StarFriend : 0
     * AppAccountFlag : 0
     * Statues : 1
     * AttrStatus : 0
     * Province :
     * City :
     * Alias :
     * SnsFlag : 0
     * UniFriend : 0
     * DisplayName :
     * ChatRoomId : 0
     * KeyWord :
     * EncryChatRoomId :
     */

    public int MemberStatus;
    public long Uin;
    public String UserName;
    public String NickName;
    public String HeadImgUrl;
    public String RemarkName;
    public String PYInitial;
    public String PYQuanPin;
    public String RemarkPYInitial;
    public String RemarkPYQuanPin;
    public int HideInputBarFlag;
    public long StarFriend;
    public int Sex;
    public String Signature;
    public int AppAccountFlag;
    public int VerifyFlag;
    public int ContactFlag;
    public int WebWxPluginSwitch;
    public int HeadImgFlag;
    public int SnsFlag;
    public int MemberCount;
    public long OwnerUin;
    public long Statues;
    public long AttrStatus;
    public String Province;
    public String City;
    public String Alias;
    public long UniFriend;
    public String DisplayName;
    public long ChatRoomId;
    public String KeyWord;
    public String EncryChatRoomId;
    public List<ContactEntity> MemberList;
}
