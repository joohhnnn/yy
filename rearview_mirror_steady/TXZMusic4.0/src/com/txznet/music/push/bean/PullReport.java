package com.txznet.music.push.bean;

import java.util.Map;

/**
 * @author zackzhou
 * @date 2019/2/20,21:15
 */

public class PullReport {
    // --- 上报数据
    /**
     * url : https://app.leting.io/play/feedback
     * data : {"action_type":3,"timestamp":1514453668,"imei":"357457041364911_f8:01:13=>f2:2d:03","os":"android","brand":"samsung","clarity":"NORMAL","log_id":"2314","data":[{"sid":"m6J2LW-4z-H2dtxea5Sv6_voziweppX1K_aGb7fKtdjq3y36awwXd_NeoKgBXD7a","duration":138,"ext":{"district":"浦东新区","city":"上海","province":"上海"}},{"sid":"hAQGpxeSe5_e9RR03wPLe9JValwMyM5Ce9OwJR3eayS6byOmUUunPl03ciB1e-8a9","duration":138,"ext":{"district":"玄武区","city":"南京","province":"江苏"}}]}
     * isPost : 1
     * header : ["logid: afadfdfasd","uid: -1","token: adfadsfads"]
     */

    private String url;
    private String data;
    private int isPost;
    private Map<String, Object> header;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getIsPost() {
        return isPost;
    }

    public void setIsPost(int isPost) {
        this.isPost = isPost;
    }

    public Map<String, Object> getHeader() {
        return header;
    }

    public void setHeader(Map<String, Object> header) {
        this.header = header;
    }
}
