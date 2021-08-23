package com.txznet.audio.server.response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by telenewbie on 2017/4/12.
 */

public class QQParamData {
    public String mQQMusicTicketData = null;
    public long mQQMusicTicketExpiredTime = 0;
    public String url;
    public List<String> backUrl = new ArrayList<String>();// 备用请求地址
    public int position;//使用的是哪一个url的position
}
