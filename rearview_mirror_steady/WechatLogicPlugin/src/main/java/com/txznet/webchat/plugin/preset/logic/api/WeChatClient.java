package com.txznet.webchat.plugin.preset.logic.api;

import android.os.Environment;

import com.alibaba.fastjson.JSON;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.okhttp.OkHttpClient;
import com.txznet.algorithm.TXZJsonParser;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.comm.plugin.utils.PluginLogUtil;
import com.txznet.webchat.plugin.preset.WxLogicPlugin;
import com.txznet.webchat.plugin.preset.logic.action.ActionType;
import com.txznet.webchat.plugin.preset.logic.api.resp.JsLoginResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.LoginResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.PushLoginResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.WebWxBatchGetContactResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.WebWxGetContactResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.WebWxInitResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.WebWxNewLoginPageResp;
import com.txznet.webchat.plugin.preset.logic.http.JsonObjectRequest;
import com.txznet.webchat.plugin.preset.logic.http.OkHttpStack;
import com.txznet.webchat.plugin.preset.logic.http.RawRequest;
import com.txznet.webchat.plugin.preset.logic.http.SimpleErrorListener;
import com.txznet.webchat.plugin.preset.logic.util.RespParser;
import com.txznet.webchat.plugin.preset.logic.util.SerializableCookieStore;
import com.txznet.webchat.plugin.preset.logic.util.ThreadManager;
import com.txznet.webchat.plugin.preset.logic.util.WxCacheManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信网络接口封装
 */
public class WeChatClient {
    public static interface WeChatResp<T> {
        void onResp(T t);

        void onError(int statusCode, String message);
    }

    private static WeChatClient sInstance;
    public TXZRequestQueue mRequestQueue;
    public ClientData ClientData;
    public Api Api;
    private List<TXZJsonParser.ParseTask> mParseTaskRef;

    // 校验身份的数据
    public static class ClientData implements Serializable {
        public String mHost = "wx.qq.com";
        String mRedirectUrl;
        public String mUUID;
        public String mWxUin;
        public String mWxSkey;
        public String mWxSid;
        public String mPassTicket;
        transient WebWxInitResp.SyncKeyEntity mSyncKey;

        public void clear() {
            mHost = "wx.qq.com";
            mRedirectUrl = null;
            mUUID = null;
            mWxUin = null;
            mWxSkey = null;
            mWxSid = null;
            mPassTicket = null;
            mSyncKey = null;
        }

        @Override
        public String toString() {
            return "ClientData{" +
                    "mHost='" + mHost + '\'' +
                    ", mRedirectUrl='" + mRedirectUrl + '\'' +
                    ", mUUID='" + mUUID + '\'' +
                    ", mWxUin='" + mWxUin + '\'' +
                    ", mWxSkey='" + mWxSkey + '\'' +
                    ", mWxSid='" + mWxSid + '\'' +
                    ", mPassTicket='" + mPassTicket + '\'' +
                    ", mSyncKey=" + mSyncKey +
                    '}';
        }
    }

    public WeChatUrlSupport mUrlSupport;

    private static final String DEFAULT_HOST = "wx.qq.com";

    public static WeChatClient getInstance() {
        if (sInstance == null) {
            synchronized (WeChatClient.class) {
                if (sInstance == null) {
                    sInstance = new WeChatClient();
                }
            }
        }
        return sInstance;
    }

    public WeChatClient() {
        ClientData = new ClientData();
        mUrlSupport = new WeChatUrlSupport(ClientData);
        Api = new Api();

        initData();
    }

    private void initData() {
        mParseTaskRef = new ArrayList<TXZJsonParser.ParseTask>();

        OkHttpClient okHttpClient = new OkHttpClient();
        SerializableCookieStore cookieStore = WxCacheManager.getInstance().getCookieStore();
        cookieStore.setCookieChangeListener(new SerializableCookieStore.CookieChangeListener() {
            private String KEY_DATA_TICKET = "webwx_data_ticket";
            private String KEY_WX_SID = "wxsid";
            private String KEY_WX_UIN = "wxuin";
            private String mDataTicket = "";
            private String mSid = "";
            private String mUin = "";

            @Override
            public void onCookieUpdated(HttpCookie cookie) {
                boolean bUpdate = false;
                String name = cookie.getName();
                if (KEY_DATA_TICKET.equals(name)) {
                    mDataTicket = cookie.getValue();
                    bUpdate = true;
                } else if (KEY_WX_SID.equals(name)) {
                    mSid = cookie.getValue();
                    bUpdate = true;
                } else if (KEY_WX_UIN.equals(name)) {
                    mUin = cookie.getValue();
                    bUpdate = true;
                }

                if (bUpdate) {
                    String cookieStr = encodeResourceCookieString();
                    // data ticket更新时通知微信客户端更新
                    PluginManager.invoke("wx.cmd.dispatch_event",
                            ActionType.WX_PLUGIN_UPDATE_RESOURCE_COOKIE, cookieStr);
                }
            }

            @Override
            public void onCookieRemove(HttpCookie cookie) {

            }

            private String encodeResourceCookieString() {
                StringBuilder sb = new StringBuilder();
                sb.append(KEY_DATA_TICKET + "=" + mDataTicket + ";");
                sb.append(KEY_WX_SID + "=" + mSid + ";");
                sb.append(KEY_WX_UIN + "=" + mUin);
                return sb.toString();
            }
        });
        okHttpClient.setCookieHandler(new CookieManager(cookieStore, CookiePolicy.ACCEPT_ALL));
        mRequestQueue = new TXZRequestQueue(Volley.newRequestQueue(GlobalContext.get(), new OkHttpStack(okHttpClient)));
        mRequestQueue.start();
    }

    public void restart() {
        reset();
        cancelAll();
//        mCookieStore.removeAll();
        // 清空校验数据
//        ClientData.clear();
    }

    public void reset() {
        //mCookieStore.removeAll();
        //mUserCache.getCookieStore().removeAll();
        ClientData.clear();
    }

    public void cancelAll() {
        mRequestQueue.stop();
        // 取消当前的请求
        mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                if (request.getTag() != null && request.getTag().equals("UN_FILTER")) {
                    return false;
                }
                return true;
            }
        });

        // 取消正在进行的json解析任务
        for (int i = 0; i < mParseTaskRef.size(); i++) {
            mParseTaskRef.get(i).cancel();
        }
        mParseTaskRef.clear();

        mRequestQueue.start();
    }

    public void cancelAll(String tag) {
        mRequestQueue.cancelAll(tag);
    }

    // 数据
    public class Api {
        // 获取二维码
        public void jslogin(final WeChatResp<JsLoginResp> resp) {
            String url = mUrlSupport.getUrl_jslogin();
            StringRequest request = new StringRequest(url, new Listener<String>() {
                @Override
                public void onResponse(String s) {
                    try {
                        Map<String, String> data = RespParser.parseSplitUrl(s);
                        JsLoginResp jsLoginResp = new JsLoginResp();
                        if (data.containsKey("window.QRLogin.code")) {
                            jsLoginResp.window_QRLogin_code = Integer.parseInt(data.get("window.QRLogin.code"));
                            jsLoginResp.window_QRLogin_uuid = data.get("window.QRLogin.uuid");

                            // 内部逻辑，保存uuid
                            if (jsLoginResp.window_QRLogin_code == 200) {
                                ClientData.mUUID = jsLoginResp.window_QRLogin_uuid;
                            }
                            resp.onResp(jsLoginResp);
                        } else {
                            resp.onError(-1, null);
                        }
                    } catch (Exception e) {
                        PluginLogUtil.d("get qrcode resp error: " + e.getMessage());
                        resp.onError(-1, null);
                        e.printStackTrace();
                    }
                }
            }, new SimpleErrorListener(resp));
            request.setShouldCache(false);
            mRequestQueue.add(request);
        }

        // 推送登录确认
        public void pushLogin(final WeChatResp<PushLoginResp> resp) {
            String url = mUrlSupport.getUrl_pushLogin();
            JsonObjectRequest request = new JsonObjectRequest(url, null, new Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject jsonObject) {
                    try{
                        PushLoginResp jsonResp = new PushLoginResp();
                        jsonResp.ret = jsonObject.getInt("ret");

                        if (0 == jsonResp.ret) {
                            jsonResp.msg = jsonObject.getString("msg");
                            jsonResp.uuid = jsonObject.getString("uuid");
                            ClientData.mUUID = jsonResp.uuid;

                            resp.onResp(jsonResp);
                        }else{
                            resp.onError(-1, jsonObject.getString("msg"));
                        }



                    }catch(Exception e) {
                        resp.onError(-1, e.toString());
                        e.printStackTrace();
                    }

                }
            }, new SimpleErrorListener(resp));
            request.setShouldCache(false);
            request.setRetryPolicy(new DefaultRetryPolicy(1000 * 10, 0, 1f));
            mRequestQueue.add(request);
        }

        // 检测登录状态
        public void login(final WeChatResp<LoginResp> resp) {
            String url = mUrlSupport.getUrl_login();
            StringRequest request = new StringRequest(url, new Listener<String>() {
                @Override
                public void onResponse(String s) {
                    try {
                        Map<String, String> data = RespParser.parseSplitUrl(s);
                        LoginResp loginResp = new LoginResp();
                        PluginLogUtil.d("login resp: " + s);
                        loginResp.window_code = Integer.parseInt(data.get("window.code"));
                        loginResp.window_redirect_uri = data.get("window.redirect_uri");

                        PluginLogUtil.d("window_code = " + loginResp.window_code);
                        // 内部逻辑，更新host
                        if (loginResp.window_code == 200) {
                            Pattern pattern = Pattern.compile("https://[\\w\\.]+/");
                            Matcher matcher = pattern.matcher(loginResp.window_redirect_uri);
                            if (matcher.find()) {
                                ClientData.mHost = matcher.group().replace("https:/", "").replace("/", "");
                                WxCacheManager.getInstance().updateHost(ClientData.mHost);
                                PluginLogUtil.d("WeChatClient::login, host changed, newHost=" + ClientData.mHost);
                            }
                            ClientData.mRedirectUrl = loginResp.window_redirect_uri;
                        }
                        // 更新UserAvatar
                        if (201 == loginResp.window_code) {
                            String strAvatar = data.get("window.userAvatar");
                            loginResp.window_user_avatar = strAvatar.substring(strAvatar.indexOf(",") + 1);
                            WxCacheManager.getInstance().updateAvatar(loginResp.window_user_avatar);
                        }
                        resp.onResp(loginResp);
                    } catch (Exception e) {
                        resp.onError(-1, null);
                        e.printStackTrace();
                    }
                }
            }, new SimpleErrorListener(resp));
            request.setShouldCache(false);
            request.setRetryPolicy(new DefaultRetryPolicy(1000 * 60, 1, 1f));
            mRequestQueue.add(request);
        }

        // 获取校验字段，pass_ticket
        public void webwxnewloginpage(final WeChatResp<WebWxNewLoginPageResp> resp) {
            String url = ClientData.mRedirectUrl;

            // 补充协议中可能未包含的字段
            if (!url.contains("&fun=new")) {
                PluginLogUtil.i("webwxnewloginpage: fix redirect url, add fun=new");
                url += "&fun=new";
            }

            if (!url.contains("&version=v2")) {
                PluginLogUtil.i("webwxnewloginpage: fix redirect url, add version=v2");
                url += "&version=v2";
            }

            if (!url.contains("&lang=zh_CN")) {
                PluginLogUtil.i("webwxnewloginpage: fix redirect url, add lang=zh_CN");
                url += "&lang=zh_CN";
            }

            PluginLogUtil.i("webwxnewloginpage: fix redirect url, final = " + url);

            StringRequest request = new StringRequest(url, new Listener<String>() {
                @Override
                public void onResponse(String s) {
                    try {
                        PluginLogUtil.d("xml content: " + s);
                        Map<String, String> xmlParams = RespParser.parseXml(s);
                        WebWxNewLoginPageResp loginPageResp = new WebWxNewLoginPageResp();
                        loginPageResp.ret = Integer.parseInt(xmlParams.get("ret"));
                        // 内部逻辑，设置校验字段
                        if (loginPageResp.ret == 0) {
                            Map<String, String> errorEle = RespParser.parseXml(xmlParams.get("error"));
                            loginPageResp.skey = errorEle.get("skey");
                            loginPageResp.wxsid = errorEle.get("wxsid");
                            loginPageResp.wxuin = errorEle.get("wxuin");
                            loginPageResp.pass_ticket = errorEle.get("pass_ticket");
                            loginPageResp.isgrayscale = Integer.parseInt(errorEle.get("isgrayscale"));
                            loginPageResp.message = errorEle.get("message");
                            ClientData.mWxSkey = loginPageResp.skey;
                            ClientData.mWxSid = loginPageResp.wxsid;
                            ClientData.mWxUin = loginPageResp.wxuin;
                            ClientData.mPassTicket = loginPageResp.pass_ticket;
                            // 保存用户缓存
                            WxCacheManager.getInstance().updateUin(loginPageResp.wxuin);
                        }
                        resp.onResp(loginPageResp);
                    } catch (Exception e) {
                        resp.onError(-1, e.getMessage() + ", raw=" + s);
                        e.printStackTrace();
                    }
                }
            }, new SimpleErrorListener(resp));
            request.setShouldCache(false);
            mRequestQueue.add(request);
        }

        private String mLoginUserOpenId = "";
        // 获取会话列表
        public void webwxinit(final WeChatResp<WebWxInitResp> resp) {
            String url = mUrlSupport.getUrl_webwxinit();
            RawRequest request = new RawRequest(Method.POST, url, mUrlSupport.getBaseRequest().toString(), new Listener<byte[]>() {
                @Override
                public void onResponse(final byte[] bytes) {
                    // 耗时操作，切到后台线程解析
                    ThreadManager.getPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                TXZJsonParser.ParseTask task = TXZJsonParser.createTask(new TXZJsonParser.ParseResult<WebWxInitResp>() {
                                    @Override
                                    public void onSuccess(WebWxInitResp initResp) {
                                        // 内部逻辑，获取SyncKey
                                        if (initResp.BaseResponse.Ret == 0) {
                                            ClientData.mWxSkey = initResp.SKey;
                                            ClientData.mSyncKey = initResp.SyncKey;
                                            mLoginUserOpenId = initResp.User.UserName;
                                        }
                                        resp.onResp(initResp);
                                    }

                                    @Override
                                    public void onCancel() {
                                    }

                                    @Override
                                    public void onError(int errOffset, Exception e) {
                                        resp.onError(-1, e.getMessage());
                                    }
                                });
                                task.write(bytes);
                                task.complete();
                                mParseTaskRef.add(task);
                            } catch (Exception e) {
                                resp.onError(-1, e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }, new SimpleErrorListener(resp));
            request.setShouldCache(false);
            request.setRetryPolicy(new DefaultRetryPolicy(1000 * 8, 1, 1f));
            request.setTag("webwxinit");
            mRequestQueue.add(request);
        }

        // 通知服务器已登录网页微信
        public void webwxstatusnotify(final WeChatResp<String> resp) {
            String url = mUrlSupport.getUrl_webwxstatusnotify();
            JSONObject params = mUrlSupport.getBaseRequest();
            try {
                //Contact self = WxContactStore.get().getSelf();
                params.put("ClientMsgId", System.currentTimeMillis());
                params.put("Code", 3);
                params.put("FromUserName", mLoginUserOpenId);
                params.put("ToUserName", mLoginUserOpenId);
            } catch (Exception e) {
                PluginLogUtil.e("WeChatClient::webwxstatusnotify make entity Failed " + e);
                return;
            }
            RawRequest request = new RawRequest(Method.POST, url, params.toString(), new Listener<byte[]>() {
                @Override
                public void onResponse(final byte[] bytes) {
                    ThreadManager.getPool().execute(new AuthCheckRunnable() {
                        @Override
                        public void _run() {
                            resp.onResp(bytes == null ? null : new String(bytes));
                        }
                    });
                }
            }, new SimpleErrorListener(resp));
            request.setShouldCache(false);
            mRequestQueue.add(request);
        }

        // 获取联系人列表
        public void webwxgetcontact(final WeChatResp<WebWxGetContactResp> resp, int seq) {
            String url = mUrlSupport.getUrl_webwxgetcontact(seq);
            RawRequest request = new RawRequest(Method.POST, url, new Listener<byte[]>() {
                @Override
                public void onResponse(final byte[] bytes) {
                    // 耗时操作，切到后台线程解析
                    ThreadManager.getPool().execute(new AuthCheckRunnable() {
                        @Override
                        public void _run() {
                            try {
                                TXZJsonParser.ParseTask task = TXZJsonParser.createTask(new TXZJsonParser.ParseResult<WebWxGetContactResp>() {
                                    @Override
                                    public void onSuccess(WebWxGetContactResp getContactResp) {
                                        resp.onResp(getContactResp);
                                    }

                                    @Override
                                    public void onCancel() {
                                    }

                                    @Override
                                    public void onError(int errOffset, Exception e) {

                                    }
                                });
                                task.write(bytes);
                                //task.write(generateFakeContacts(3000));
                                task.complete();
                                mParseTaskRef.add(task);
                            } catch (Exception e) {
                                resp.onError(-1, e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }, new SimpleErrorListener(resp)) {
                @Override
                public Priority getPriority() {
                    return Priority.HIGH;
                }
            };
            request.setShouldCache(false);
            request.setRetryPolicy(new DefaultRetryPolicy(1000 * 20, 1, 1f));
            mRequestQueue.add(request);
        }


        private byte[] generateFakeContacts(int count){
            // build fake contacts
            StringBuilder sb = new StringBuilder();
            sb.append("{\n" +
                    "\"BaseResponse\": {\n" +
                    "\"Ret\": 0,\n" +
                    "\"ErrMsg\": \"\"\n" +
                    "}\n" +
                    ",\n" +
                    "\"MemberCount\": ");
            sb.append(count);
            sb.append(",\n" +
                    "\"MemberList\": [");

            for(int i = 0; i < count; i++){
                sb.append(String.format("{\n" +
                        "\"Uin\": 0,\n" +
                        "\"UserName\": \"@5557a1e2652c0005226d25e4d8ccab6a%s\",\n" +
                        "\"NickName\": \"始终%s\",\n" +
                        "\"HeadImgUrl\": \"/cgi-bin/mmwebwx-bin/webwxgeticon?seq=640571872&username=@5557a1e2652c0005226d25e4d8ccab6a&skey=@crypt_c13d7bc2_418aaaf7f73cf05aee762b35818b0c20\",\n" +
                        "\"ContactFlag\": 8195,\n" +
                        "\"MemberCount\": 0,\n" +
                        "\"MemberList\": [],\n" +
                        "\"RemarkName\": \"\",\n" +
                        "\"HideInputBarFlag\": 0,\n" +
                        "\"Sex\": 1,\n" +
                        "\"Signature\": \"烟花易冷\",\n" +
                        "\"VerifyFlag\": 0,\n" +
                        "\"OwnerUin\": 0,\n" +
                        "\"PYInitial\": \"SZ\",\n" +
                        "\"PYQuanPin\": \"shizhong\",\n" +
                        "\"RemarkPYInitial\": \"\",\n" +
                        "\"RemarkPYQuanPin\": \"\",\n" +
                        "\"StarFriend\": 0,\n" +
                        "\"AppAccountFlag\": 0,\n" +
                        "\"Statues\": 0,\n" +
                        "\"AttrStatus\": 33793127,\n" +
                        "\"Province\": \"广东\",\n" +
                        "\"City\": \"深圳\",\n" +
                        "\"Alias\": \"Re_suki\",\n" +
                        "\"SnsFlag\": 17,\n" +
                        "\"UniFriend\": 0,\n" +
                        "\"DisplayName\": \"\",\n" +
                        "\"ChatRoomId\": 0,\n" +
                        "\"KeyWord\": \"sho\",\n" +
                        "\"EncryChatRoomId\": \"\"\n" +
                        "},", i, i));
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("],\n" +
                    "\"Seq\": 0\n" +
                    "}");

            return sb.toString().getBytes();
        }

        // 获取群组内的联系人列表
        public void webwxbatchgetcontact(final WeChatResp<WebWxBatchGetContactResp> resp, String... groupIds) {
            String url = mUrlSupport.getUrl_webwxbatchgetcontact();
            JSONObject params = mUrlSupport.getBaseRequest();
            // TODO 交叉访问
            try {
                params.put("Count", groupIds.length);
                JSONArray idList = new JSONArray();
                for (int i = 0; i < groupIds.length; ++i) {
                    JSONObject p = new JSONObject();
                    p.put("UserName", groupIds[i]);
                    p.put("ChatRoomId", "");
                    p.put("EncryChatRoomId", "");
                    idList.put(p);
                }
                params.put("List", idList);
            } catch (Exception e) {
                PluginLogUtil.e("WeChatClient::webwxbatchgetcontact, make entity Failed");
                return;
            }
            RawRequest request = new RawRequest(Method.POST, url, params.toString(), new Listener<byte[]>() {
                @Override
                public void onResponse(final byte[] bytes) {
                    // 耗时操作，切到后台线程解析
                    ThreadManager.getPool().execute(new AuthCheckRunnable() {
                        @Override
                        public void _run() {
                            try {
                                TXZJsonParser.ParseTask task = TXZJsonParser.createTask(new TXZJsonParser.ParseResult<WebWxBatchGetContactResp>() {
                                    @Override
                                    public void onSuccess(WebWxBatchGetContactResp result) {
                                        resp.onResp(result);
                                    }

                                    @Override
                                    public void onError(int errOffset, Exception e) {

                                    }

                                    @Override
                                    public void onCancel() {

                                    }
                                });
                                task.write(bytes);
                                task.complete();
                                mParseTaskRef.add(task);
                            } catch (Exception e) {
                                resp.onError(-1, e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }, new SimpleErrorListener(resp)){
                @Override
                public Priority getPriority() {
                    return Priority.NORMAL;
                }
            };
            request.setShouldCache(false);
            request.setRetryPolicy(new DefaultRetryPolicy(1000 * 10, 1, 1f));
            mRequestQueue.add(request);
        }

        // 检测是否消息变化
        public void synccheck(final WeChatResp<String> resp) {
            String url = mUrlSupport.getUrl_synccheck(getSyncKey());
            StringRequest request = new StringRequest(Method.GET, url, new Listener<String>() {
                @Override
                public void onResponse(final String string) {
                    ThreadManager.getPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            resp.onResp(string);
                        }
                    });
                }
            }, new SimpleErrorListener(resp)){
                @Override
                public Priority getPriority() {
                    return Priority.HIGH;
                }
            };
            request.setShouldCache(false);
            request.setRetryPolicy(new DefaultRetryPolicy(1000 * 60, 1, 1f));
            mRequestQueue.add(request);
        }

        // 同步更新信息
        public void webwxsync(final WeChatResp<JSONObject> resp) {
            String url = mUrlSupport.getUrl_webwxsync();
            JSONObject params = mUrlSupport.getBaseRequest();
            try {
                params.put("SyncKey", new JSONObject(JSON.toJSONString(ClientData.mSyncKey)));
                params.put("rr", mUrlSupport.getTimeStampShort());
            } catch (Exception e) {
                return;
            }
            JsonObjectRequest request = new JsonObjectRequest(Method.POST, url, params, new Listener<JSONObject>() {
                @Override
                public void onResponse(final JSONObject jsonObject) {
                    ThreadManager.getPool().execute(new AuthCheckRunnable() {
                        @Override
                        public void _run() {
                            try {
                                JSONObject baseResponse = jsonObject.getJSONObject("BaseResponse");
                                int ret = baseResponse.getInt("Ret");
                                if (ret == 0) {
                                    ClientData.mSyncKey = JSON.parseObject(jsonObject.getJSONObject("SyncCheckKey").toString(), WebWxInitResp.SyncKeyEntity.class);
                                }
                            } catch (Exception e) {

                            }
                            resp.onResp(jsonObject);
                        }
                    });
                }
            }, new SimpleErrorListener(resp)) {
                @Override
                public Priority getPriority() {
                    return Priority.HIGH;
                }
            };
            request.setShouldCache(false);
            mRequestQueue.add(request);
        }

        public void webwxrevokemsg(final WeChatResp<JSONObject> resp, final WxMessage msg) {
            String url = mUrlSupport.getUrl_webwxrevokemsg();
            JSONObject params = mUrlSupport.getBaseRequest();

            try {
                params.put("ClientMsgId", String.valueOf(msg.mMsgId));
                params.put("SvrMsgId", String.valueOf(msg.mServerMsgId));
                params.put("ToUserName", msg.mSessionId);
            } catch (Exception e) {
                resp.onError(-1, "generate request param encountered error: " + e.toString());
            }

            JsonObjectRequest request = new JsonObjectRequest(Method.POST, url, params, new Listener<JSONObject>() {
                @Override
                public void onResponse(final JSONObject jsonObject) {
                    ThreadManager.getPool().execute(new AuthCheckRunnable() {
                        @Override
                        public void _run() {
                            resp.onResp(jsonObject);
                        }
                    });
                }
            }, new SimpleErrorListener(resp)){
                @Override
                public Priority getPriority() {
                    return Priority.HIGH;
                }
            };
            request.setShouldCache(false);
            mRequestQueue.add(request);
        }

        // 退出登录
        public void webwxlogout(final WeChatResp<String> resp) {
            String url = mUrlSupport.getUrl_logout();
            StringBuilder params = new StringBuilder();
            params.append("sid=").append(ClientData.mWxSid);
            params.append("&uin=").append(ClientData.mWxUin);

            RawRequest request = new RawRequest(Method.POST, url, params.toString(), new Listener<byte[]>() {
                @Override
                public void onResponse(byte[] data) {
                    resp.onResp(new String(data));
                }
            }, new SimpleErrorListener(resp));
            request.setTag("UN_FILTER");
            request.setShouldCache(false);
            mRequestQueue.add(request);
        }
    }

    public String getQrCodeImage() {
        return "https://login.weixin.qq.com/l/" + ClientData.mUUID; // 这个是二维码的文本内容
        // return "https://login.weixin.qq.com/qrcode/" + mUUID; //这个是二维码的图片的内容
    }

    public String getSyncKey() {
        StringBuilder syncKey = new StringBuilder();
        try {
            if (ClientData.mSyncKey != null) {
                List<WebWxInitResp.SyncKeyEntity.ListEntity> keyList = ClientData.mSyncKey.List;
                for (int i = 0; i < keyList.size(); ++i) {
                    if (i > 0)
                        syncKey.append('|');
                    syncKey.append(keyList.get(i).Key).append("_").append(keyList.get(i).Val);
                }
            }
        } catch (Exception e) {
        }
        return syncKey.toString();
    }


    /*
     *  带登录校验的Runnable，
     *  由于把耗时操作异步转到后台线程处理，
     *  从而引起网络数据已经返回，异步操作的任务已经压到队列中等待处理，逻辑处理和请求回调分离，此时取消Volley的任务，无法中断任务
     */
    public static abstract class AuthCheckRunnable implements Runnable {
        @Override
        public void run() {
            if (!WxLogicPlugin.sIsLoggedIn) {
                return;
            }
            _run();
        }

        public abstract void _run();
    }

    public void backup() {
        backup(null);
    }

    public void backup(String flag) {
        File backup = new File(Environment.getExternalStorageDirectory() + "/txz/webchat/.backup");
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(backup));
            oos.writeObject(ClientData);
        } catch (Exception e) {

        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public boolean restore() {
        File backup = new File(Environment.getExternalStorageDirectory() + "/txz/webchat/.backup");
        if (backup.exists()) {
            ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(backup));
                ClientData = (WeChatClient.ClientData) ois.readObject();
                mUrlSupport = new WeChatUrlSupport(ClientData);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (ois != null) {
                    try {
                        ois.close();
                        backup.delete();
                    } catch (IOException e) {
                    }
                }
            }
        }
        return false;
    }

    public String getFileUrl(String sender, String mediaId, String fileName) {
        return mUrlSupport.getUrl_webwxgetmedia(sender, mediaId, fileName);
    }
}
