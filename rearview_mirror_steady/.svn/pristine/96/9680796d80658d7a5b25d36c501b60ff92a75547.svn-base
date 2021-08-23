package com.txznet.webchat.plugin.preset.logic.api;

import android.text.TextUtils;

import com.txznet.webchat.plugin.preset.logic.util.WxCacheManager;

import org.json.JSONObject;

import java.net.HttpCookie;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;

/*
    获取微信请求连接的帮助类
 */
public class WeChatUrlSupport {
    private WeChatClient.ClientData mClientData;
    private long mStepTimeStamp = 0; // 微信同步请求的递增时间戳, 每次递增1

    public WeChatUrlSupport(WeChatClient.ClientData data) {
        mClientData = data;
    }

    private long getStepTimeStamp() {
        if (0 == mStepTimeStamp) {
            mStepTimeStamp = System.currentTimeMillis();
        }
        return mStepTimeStamp++;
    }


    // step1
    public String getUrl_jslogin() {
        mStepTimeStamp = System.currentTimeMillis();
        return "https://login.wx.qq.com/jslogin?appid=wx782c26e4c19acffb&redirect_uri=https%3A%2F%2Fwx.qq.com%2Fcgi-bin%2Fmmwebwx-bin%2Fwebwxnewloginpage&fun=new&lang=zh_CN&_=" + getStepTimeStamp();
    }

    // step1-1
    public String getUrl_pushLogin() {
        // return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxpushloginurl?uin=" + mClientData.mWxUin;
        // 因为递增时间戳可能因为上次登录后进程未重启导致不为0, 此处进行下重置
        mStepTimeStamp = 0;
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxpushloginurl?uin=" + WxCacheManager.getInstance().getUin();
    }

    // step2
    public String getUrl_login() {
        return "https://login.wx.qq.com/cgi-bin/mmwebwx-bin/login?loginicon=true&uuid=" + mClientData.mUUID + "&tip=0&r=" + getTimeStampShort() + "&_=" + getStepTimeStamp();
    }

    // step3
    public String getUrl_webwxinit() {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxinit?r=" + getTimeStampShort() + "&lang=zh_CN&pass_ticket=" + encodeStr(mClientData.mPassTicket);
    }

    // step5
    public String getUrl_webwxstatusnotify() {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxstatusnotify?lang=zh_CN&pass_ticket=" + encodeStr(mClientData.mPassTicket);
    }

    // step6
    public String getUrl_webwxgetcontact(int seq) {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxgetcontact?lang=zh_CN&pass_ticket="
                + encodeStr(mClientData.mPassTicket) + "&r=" + System.currentTimeMillis() + "&seq="+ seq + "&skey="
                + encodeStr(mClientData.mWxSkey);
    }

    // step 7
    public String getUrl_synccheck(String syncKey) {
        String syncUrl;
        if (WxCacheManager.getInstance().getHost().equals("web.wechat.com")) {
            syncUrl = "https://webpush.wechat.com/cgi-bin/mmwebwx-bin/synccheck?r=" + System.currentTimeMillis()
                    + "&skey=" + encodeStr(mClientData.mWxSkey)
                    + "&sid=" + encodeStr(mClientData.mWxSid)
                    + "&uin=" + encodeStr(mClientData.mWxUin)
                    + "&deviceid=e" + genStr(15)
                    + "&synckey=" + encodeStr(syncKey)
                    + "&pass_ticket=" + encodeStr(mClientData.mPassTicket);
        } else {
            String version = WxCacheManager.getInstance().getHost().replace("wx", "").replace(".qq.com", "");
            syncUrl = "https://webpush.wx" + version + ".qq.com/cgi-bin/mmwebwx-bin/synccheck?r=" + System.currentTimeMillis()
                    + "&skey=" + encodeStr(mClientData.mWxSkey)
                    + "&sid=" + encodeStr(mClientData.mWxSid)
                    + "&uin=" + encodeStr(mClientData.mWxUin)
                    + "&deviceid=e" + genStr(15)
                    + "&synckey=" + encodeStr(syncKey)
                    + "&_=" + getStepTimeStamp();
        }
        return syncUrl;
    }

    // step 8
    public String getUrl_webwxsync() {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxsync?sid="
                + encodeStr(mClientData.mWxSid)
                + "&skey=" + encodeStr(mClientData.mWxSkey)
                + "&lang=zh_CN&pass_ticket=" + encodeStr(mClientData.mPassTicket);
    }

    public String getUrl_webwxbatchgetcontact() {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxbatchgetcontact?type=ex&r="
                + System.currentTimeMillis() + "&lang=zh_CN&pass_ticket=" + encodeStr(mClientData.mPassTicket);
    }

    public String getUrl_webwxsendmsg() {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxsendmsg?pass_ticket="
                + encodeStr(mClientData.mPassTicket);
    }

    public String getUrl_webWxsendImg() {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxsendmsgimg?fun=async&f=json&pass_ticket="
                + encodeStr(mClientData.mPassTicket);
    }

    public String getUrl_webwxrevokemsg() {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxrevokemsg";
    }

    public String getUrl_webwxgetvoice(String msgId) {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxgetvoice?msgid=" + msgId + "&skey=" + mClientData.mWxSkey;
    }

    public String getUrl_webwxgetheadimg(String userId) {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxgetheadimg?seq=627090357&username=" + userId + "&skey=" + mClientData.mWxSkey;
    }

    public String getUrl_webwxgeticon(String userId, String encryChatroomId) {
        String url = "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxgeticon?seq=0&username=" + userId;
        if (!TextUtils.isEmpty(encryChatroomId)) {
            url += "&chatroomid=" + encryChatroomId;
        }
        url += "&skey=" + mClientData.mWxSkey;
        return url;
    }

    public String getUrl_webwxgetmedia(String sender, String mediaId, String fileName) {
        return "https://file." + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxgetmedia?" +
                "sender=" + sender +
                "&mediaid=" + mediaId +
                "&filename=" + encodeStr(fileName) +
                "&fromuser=" + WxCacheManager.getInstance().getUin() +
                "&pass_ticket=" + mClientData.mPassTicket +
                "&webwx_data_ticket=" + getWebWxDataTicket();
    }

    private String mDataTicket = "";

    private String getWebWxDataTicket() {
        if (TextUtils.isEmpty(mDataTicket)) {
            List<HttpCookie> cookieList = WxCacheManager.getInstance().getCookieStore().getCookies();
            for (HttpCookie cookie : cookieList) {
                if ("webwx_data_ticket".equals(cookie.getName())) {
                    mDataTicket = cookie.getValue();
                }
            }
        }

        return mDataTicket;
    }


    public String getUrl_headByUrl(String url) {
        return "https://" + WxCacheManager.getInstance().getHost() + url;
    }

    public String getUrl_logout() {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxlogout?redirect=1&type=0&skey=" + encodeStr(mClientData.mWxSkey);
    }

    public String getUrl_uploadImage() {
        return "https://" + WxCacheManager.getInstance().getHost() + "/cgi-bin/mmwebwx-bin/webwxpreview?fun=upload";
    }

    public int getTimeStampShort() {
        return ~((int) System.currentTimeMillis());
    }

    private String genStr(int length) {
        StringBuilder random = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < length; ++i) {
            random.append(r.nextInt(10));
        }
        return random.toString();
    }

    private String encodeStr(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (Exception e) {
        }
        return str;
    }

    public JSONObject getBaseRequest() {
        JSONObject BaseRequest = new JSONObject();
        JSONObject ret = new JSONObject();
        try {
            BaseRequest.put("Uin", Long.parseLong(mClientData.mWxUin));
            BaseRequest.put("Sid", mClientData.mWxSid);
            BaseRequest.put("Skey", mClientData.mWxSkey);
            BaseRequest.put("DeviceID", "e" + genStr(15));
            ret.put("BaseRequest", BaseRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }
}
