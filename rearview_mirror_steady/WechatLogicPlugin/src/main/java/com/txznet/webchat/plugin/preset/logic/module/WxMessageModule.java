package com.txznet.webchat.plugin.preset.logic.module;

import android.text.TextUtils;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.comm.plugin.utils.PluginLogUtil;
import com.txznet.webchat.comm.plugin.utils.PluginMonitorUtil;
import com.txznet.webchat.plugin.preset.logic.action.ActionType;
import com.txznet.webchat.plugin.preset.logic.api.WeChatClient;
import com.txznet.webchat.plugin.preset.logic.base.WxModule;
import com.txznet.webchat.plugin.preset.logic.consts.MonitorConsts;
import com.txznet.webchat.plugin.preset.logic.http.JsonObjectRequest;
import com.txznet.webchat.plugin.preset.logic.http.RawRequest;
import com.txznet.webchat.plugin.preset.logic.model.MsgConst;
import com.txznet.webchat.plugin.preset.logic.model.PoiInfo;
import com.txznet.webchat.plugin.preset.logic.util.LocationDecodeUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信消息插件
 * Created by J on 2016/8/24.
 */
public class WxMessageModule extends WxModule {
    private static final String TOKEN_MESSAGE_PLUGIN = "wx_message_module";

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public String getToken() {
        return TOKEN_MESSAGE_PLUGIN;
    }

    @Override
    public void reset() {

    }

    private static WxMessageModule sInstance = new WxMessageModule();

    public static WxMessageModule getInstance() {
        return sInstance;
    }

    private WxMessageModule() {

    }

    public void resolveMessage(JSONArray data) {
        if (null == WxContactModule.getInstance().getLoginUser()) {
            PluginLogUtil.e(getToken(), "error resolving message: login user is null");
        }

        PluginLogUtil.i(getToken(), "resolveMessage::data = " + data.toString());

        for (int i = 0, len = data.length(); i < len; i++) {
            do {
                int msgType;
                JSONObject rawJsonObj;
                try {
                    rawJsonObj = data.getJSONObject(i);
                    msgType = rawJsonObj.getInt("MsgType");
                } catch (JSONException e) {
                    PluginLogUtil.w(getToken(), "get msgType encountered error: " + e.getMessage());
                    break;
                }

                // 处理状态通知消息
                if (MsgConst.MSG_TYPE_STATUSNOTIFY == msgType) {
                    try {
                        if (rawJsonObj.getInt("StatusNotifyCode")
                                == MsgConst.STATUS_NOTIFY_CODE_SYNC_CONV) {
                            PluginLogUtil.e(getToken(), "sync session msg: "
                                    + rawJsonObj.getString("StatusNotifyUserName"));
                            WxContactModule.getInstance()
                                    .resolveSession(rawJsonObj.getString("StatusNotifyUserName"));
                        }
                        if (rawJsonObj.getInt("StatusNotifyCode")
                                == MsgConst.STATUS_NOTIFY_CODE_TOP_SESSION) {
                            String to = rawJsonObj.getString("ToUserName");
                            dispatchEvent(ActionType.WX_PLUGIN_SYNC_TOP_SESSION, to);
                        }
                    } catch (JSONException e) {
                        PluginLogUtil.w(getToken(),
                                "resolve status notify message encountered error: "
                                        + e.getMessage());
                    }

                    break;
                }

                WxMessage message = resolveMessageContact(rawJsonObj);

                if (null == message) {
                    break;
                }

                // 判断消息联系人是否合法
                if (TextUtils.isEmpty(message.mSessionId)
                        || TextUtils.isEmpty(message.mSenderUserId)
                        || !message.mSenderUserId.startsWith("@")
                        || !message.mSessionId.startsWith("@")) {
                    PluginLogUtil.w(getToken(),
                            "resolving message contact: contact is invalid, session = "
                                    + message.mSessionId
                                    + ", sender = " + message.mSenderUserId);
                }

                try {
                    message.mMsgId = Long.parseLong(rawJsonObj.getString("MsgId"));
                    message.mServerMsgId = message.mMsgId;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 文本消息
                if (MsgConst.MSG_TYPE_TEXT == msgType) {
                    resolveTextMsg(message, rawJsonObj);
                    break;
                }

                // 语音
                if (MsgConst.MSG_TYPE_VOICEMSG == msgType) {
                    resolveVoiceMsg(message, rawJsonObj);
                    break;
                }

                // 图片
                if (MsgConst.MSG_TYPE_IMG == msgType) {
                    resolveImageMsg(message, rawJsonObj);
                    break;
                }

                // 动画表情
                if (MsgConst.MSG_TYPE_ANIM_IMG == msgType) {
                    resolveAnimMsg(message, rawJsonObj);
                    break;
                }

                // AppMsg
                if (MsgConst.MSG_TYPE_URL == msgType) {
                    resolveAppMsg(message, rawJsonObj);
                    break;
                }

                // 系统消息
                if (MsgConst.MSG_TYPE_SYSTEM == msgType) {
                    resolveSystemMsg(message, rawJsonObj);
                    break;
                }

                PluginLogUtil.w(getToken(), "unsupported message type: " + msgType);

            } while (false);
        }
    }

    /**
     * 解析消息发送者
     *
     * @param json
     * @return
     */
    private WxMessage resolveMessageContact(JSONObject json) {
        // 微信消息联系人规则：
        // 单聊： FromUserName 发送者id， ToUserName 登录用户id
        // 群聊： FromUserName 群id， ToUserName 登陆用户id， Content格式: "发送者id:<br/>消息内容"
        WxMessage message = new WxMessage();

        try {
            String strFromUserId = json.getString("FromUserName");
            String strToUserId = json.getString("ToUserName");

            if (null == strFromUserId) {
                PluginLogUtil.e(getToken(), "resolve message contact failed: FromUserName = null");
                return null;
            }

            // 群聊, 从Content中提取发送者
            if (WxContact.isGroupOpenId(strFromUserId)) {
                String content = json.getString("Content");

                int n = content.indexOf(":<br/>");
                if (n < 0) {
                    PluginLogUtil.e(getToken(),
                            "resolve message contact failed: cant find sender in group message");
                    return null;
                }

                message.mSessionId = strFromUserId;
                message.mSenderUserId = content.substring(0, n);
            } else if (strFromUserId.equals(WxContactModule.getInstance()
                    .getLoginUser().mUserOpenId)) {
                message.mSessionId = strToUserId;
                message.mSenderUserId = strFromUserId;
            } else {
                message.mSessionId = strFromUserId;
                message.mSenderUserId = strFromUserId;
            }

            return message;
        } catch (Exception e) {
            PluginLogUtil.e(getToken(),
                    "resolve message contact encountered error: " + e.getMessage());
            return null;
        }
    }

    private String getMsgContent(WxMessage message) {
        boolean isSelfMsg = WxContactModule.getInstance().getLoginUser().mUserOpenId.equals
                (message.mSenderUserId);

        switch (message.mMsgType) {
            case WxMessage.MSG_TYPE_ANIM:
                return isSelfMsg ? "[发送了一个动态表情, 请在手机查看]"
                        : "[收到一个动态表情, 请在手机查看]";

            case WxMessage.MSG_TYPE_IMG:
                return isSelfMsg ? "[发送了一张图片, 请在手机查看]"
                        : "[收到一张图片, 请在手机查看]";

            case WxMessage.MSG_TYPE_URL:
                return isSelfMsg ? "[发送了一条链接, 请在手机查看]"
                        : "[收到一条链接, 请在手机查看]";

            case WxMessage.MSG_TYPE_VOICE:
                return isSelfMsg ? "              [语音_R]"
                        : "[语音_L]              ";

            case WxMessage.MSG_TYPE_LOCATION:
                return isSelfMsg ? "[发送了一条位置消息, 请在手机查看]"
                        : "[收到一条位置消息, 请在手机查看]";

            case WxMessage.MSG_TYPE_FILE:
                return isSelfMsg ? "[发送了一个文件, 请在手机查看]"
                        : "[收到一个文件, 请在手机查看]";
        }

        return "";
    }

    private void resolveTextMsg(WxMessage message, JSONObject json) {
        try {
            int subType = json.getInt("SubMsgType");
            String rawContent = json.getString("Content").trim();

            switch (subType) {
                case MsgConst.SUBMSG_TYPE_TEXT:
                    // 车载语音消息
                    if (rawContent.replaceAll("\n", "")
                            .matches("(.)+http://u\\.txzing\\.com/\\w+】?")) {
                        // 车载消息发送成功时已添加到消息列表, 此处不做处理
                        if (message.mSessionId
                                .equals(WxContactModule.getInstance().getLoginUser().mUserOpenId)) {
                            return;
                        }

                        message.mMsgType = WxMessage.MSG_TYPE_VOICE;
                        message.mContent = getMsgContent(message);
                        // 解析车载语音消息长度和url
                        resolveTXZVoiceInfo(message, rawContent);

                        dispatchEvent(ActionType.WX_PLUGIN_MSG_ADD_MSG, message);
                        break;
                    }

                    // 车载微信端位置信息
                    Pattern pattern = Pattern
                            .compile("我在(.+) http://(.+)lat=(.+)(&amp;|&)lng=(.+)");
                    Matcher matcher = pattern.matcher(rawContent);
                    if (matcher.find()) {
                        // 车载消息发送成功时已添加到消息列表, 此处不做处理
                        if (message.mSessionId
                                .equals(WxContactModule.getInstance().getLoginUser().mUserOpenId)) {
                            return;
                        }
                        message.mMsgType = WxMessage.MSG_TYPE_LOCATION;

                        message.mAddress = matcher.group(1);
                        message.mContent = "[" + message.mAddress + "]";
                        message.mLatitude = Double.valueOf(matcher.group(3));
                        message.mLongtitude = Double.valueOf(matcher.group(5));

                        dispatchEvent(ActionType.WX_PLUGIN_MSG_ADD_MSG, message);
                        PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOC_RESOLVE_SUCCESS);
                        break;
                    }

                    // 旧版车载微信端位置信息
                    pattern = Pattern
                            .compile("(我的位置详情.+?)http://(.+)lat=(.+)(&amp;|&)lng=(.+)");
                    matcher = pattern.matcher(rawContent);
                    if (matcher.find()) {
                        message.mMsgType = WxMessage.MSG_TYPE_LOCATION;

                        message.mAddress = "";
                        message.mContent = getMsgContent(message);
                        message.mLatitude = Double.valueOf(matcher.group(3));
                        message.mLongtitude = Double.valueOf(matcher.group(5));

                        dispatchEvent(ActionType.WX_PLUGIN_MSG_ADD_MSG, message);
                        PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOC_RESOLVE_SUCCESS);
                        break;
                    }

                    // 普通文本消息
                    // 收到的群消息需要对内容进行截取
                    if (WxContact.isGroupOpenId(message.mSessionId)
                            && !message.mSenderUserId
                            .equals(WxContactModule.getInstance().getLoginUser().mUserOpenId)) {
                        message.mContent = removeEmoji(
                                rawContent.substring(message.mSenderUserId.length() + 6));
                    } else {
                        message.mContent = removeEmoji(rawContent);
                    }
                    message.mMsgType = WxMessage.MSG_TYPE_TEXT;
                    dispatchAddMsgEvent(message);
                    break;

                // 手机端的位置消息
                case MsgConst.SUBMSG_TYPE_LOCATION:
                    message.mMsgType = WxMessage.MSG_TYPE_LOCATION;
                    resolveLocationUrl(message, json);
                    break;
            }

        } catch (Exception e) {
            PluginLogUtil.e(getToken(), "resolving textMsg encountered error: " + e.getMessage());
        }
    }

    private String removeEmoji(String rawContent) {
        return rawContent.replaceAll("<span class=\"emoji ", "[")
                .replaceAll("\"></span(>)?", "]")
                .replaceAll("<br/>", "");
    }

    private void resolveTXZVoiceInfo(WxMessage message, final String rawContent) {
        message.mVoiceUrl = "";
        message.mVoiceLength = 0;
        // 截取短链接
        String txzUrl = rawContent
                .substring(rawContent.indexOf("http://u.txzing.com/")).replace("】", "");

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(txzUrl).openConnection();
            connection.setInstanceFollowRedirects(false);
            String location = connection.getHeaderField("Location");
            String host = new URL(location).getHost();
            String tmp = location.substring(0, location.length() - 4);
            tmp = tmp.substring(tmp.indexOf("=") + 1, tmp.length());
            String[] ps = tmp.split("_");

            message.mVoiceUrl = "http://" + host
                    + "/service/media/voice.php?u=" + ps[0] + "&t="
                    + ps[1];
            message.mVoiceLength = Integer.valueOf(ps[2]);
        } catch (Exception e) {
            PluginLogUtil.e(getToken(), "resolving TXZVoiceUrl encountered error: " + e.toString());
        }
    }

    private void resolveLocationUrl(final WxMessage msg, JSONObject json) {
        LocationDecodeUtil.decodeLocation(json, new LocationDecodeUtil.DecodeCallback() {
            @Override
            public void onCallback(PoiInfo poi) {
                if (null == poi) {
                    PluginLogUtil.w(getToken(), "resolve location message failed");
                    PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOC_RESOLVE_FAILED);
                } else {
                    PluginLogUtil.d(getToken(), "location resolve info: " + poi);
                    // 更新消息地址信息
                    if (!TextUtils.isEmpty(poi.getGeoInfo())) {
                        msg.mAddress = poi.getGeoInfo();
                        msg.mContent = "[" + msg.mAddress + "]";
                    } else {
                        // 地址消息未解析成功的, 提示用户在手机端查看
                        msg.mContent = getMsgContent(msg);
                    }

                    msg.mLatitude = poi.getLat();
                    msg.mLongtitude = poi.getLng();
                    dispatchEvent(ActionType.WX_PLUGIN_MSG_ADD_MSG, msg);
                    PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOC_RESOLVE_SUCCESS);
                }
            }
        });
    }

    private void resolveVoiceMsg(WxMessage message, JSONObject json) {
        message.mMsgType = WxMessage.MSG_TYPE_VOICE;
        message.mVoiceUrl = WeChatClient.getInstance().mUrlSupport.getUrl_webwxgetvoice(message
                .mMsgId + "");
        message.mContent = getMsgContent(message);
        try {
            message.mVoiceLength = json.getInt("VoiceLength");
        } catch (Exception e) {
            PluginLogUtil.e(getToken(), "resolve voice length encountered error: " + e.toString());
            message.mVoiceLength = 0;
        }
        dispatchAddMsgEvent(message);
    }

    private void resolveImageMsg(WxMessage message, JSONObject json) {
        message.mMsgType = WxMessage.MSG_TYPE_IMG;
        message.mContent = getMsgContent(message);
        dispatchAddMsgEvent(message);
    }

    private void resolveAnimMsg(WxMessage message, JSONObject json) {
        message.mMsgType = WxMessage.MSG_TYPE_ANIM;
        message.mContent = getMsgContent(message);
        dispatchAddMsgEvent(message);
    }

    private void resolveAppMsg(WxMessage message, JSONObject json) {
        try {
            int subType = json.getInt("AppMsgType");

            // 解析文件消息
            if (MsgConst.APPMSG_TYPE_FILE == subType) {
                String fileName = json.getString("FileName");
                long fileSize = Long.parseLong(json.getString("FileSize"));
                String mediaId = json.getString("MediaId");
                String fileUrl = WeChatClient.getInstance()
                        .getFileUrl(message.mSenderUserId, mediaId, fileName);

                message.mMsgType = WxMessage.MSG_TYPE_FILE;
                message.mFileName = fileName;
                message.mFileSize = fileSize;
                message.mFileUrl = fileUrl;
                message.mContent = getMsgContent(message);

                dispatchAddMsgEvent(message);
                return;
            }

        } catch (Exception e) {

        }

        message.mMsgType = WxMessage.MSG_TYPE_URL;
        message.mContent = getMsgContent(message);
        dispatchAddMsgEvent(message);
    }

    private void resolveSystemMsg(WxMessage message, JSONObject json) {
        //// TODO: 2016/11/23 待分析具体的系统消息类型
        /*message.mMsgType = WxMessage.MSG_TYPE_RED_PACKET;
        message.mContent = "[红包请在手机侧查看]";
        dispatchAddMsgEvent(message);*/
    }

    private void dispatchAddMsgEvent(WxMessage message) {
        dispatchEvent(ActionType.WX_PLUGIN_MSG_ADD_MSG, message);
    }

    /////////////// 消息发送接口

    public void sendMessage(WxMessage msg) {
        switch (msg.mMsgType) {
            case WxMessage.MSG_TYPE_LOCATION:
                sendLocationMsg(msg);
                break;

            case WxMessage.MSG_TYPE_TEXT:
            case WxMessage.MSG_TYPE_VOICE:
                sendMsg(msg);
                break;

            case WxMessage.MSG_TYPE_IMG:
                sendImageMsg(msg);
                break;

            default:
                PluginLogUtil.w(getToken(), "sendMessage::unsupported msg type: " + msg.mMsgType);
                break;
        }
    }

    private void sendLocationMsg(final WxMessage msg) {
        // 没有获取到地址文本的，填入默认地址
        String address = TextUtils.isEmpty(msg.mAddress) ? "我的当前位置" : msg.mAddress;

        msg.mContent = String.format("<?xml version=\"1.0\"?>\n<msg>\n\t" +
                "<location x=\"%s\" y=\"%s\" scale=\"16\" label=\"%s\" maptype=\"0\"" +
                " poiname=\"[位置]\" />\n</msg>\n", msg.mLatitude, msg.mLongtitude, address);
        sendMsg(msg);
    }

    private void sendMsg(final WxMessage msg) {
        dispatchEvent(ActionType.WX_SEND_MSG_REQ, msg);
        // 构造发送消息请求数据
        JSONObject params = WeChatClient.getInstance().mUrlSupport.getBaseRequest();
        final JSONObject Msg = new JSONObject();
        try {
            Msg.put("LocalID", msg.mMsgId);
            Msg.put("ClientMsgId", msg.mMsgId);

            // 设置消息类型
            if (WxMessage.MSG_TYPE_LOCATION == msg.mMsgType) {
                Msg.put("Type", MsgConst.SUBMSG_TYPE_LOCATION);
            } else {
                Msg.put("Type", MsgConst.MSG_TYPE_TEXT);
            }

            Msg.put("Content", msg.mContent);
            Msg.put("FromUserName", msg.mSenderUserId);
            Msg.put("ToUserName", msg.mSessionId);
            params.put("Msg", Msg);
            params.put("Scene", 0);
        } catch (Exception e) {
            monitorSendMsgErr(msg.mMsgType);
            PluginLogUtil.e(getToken(), "sendMsg::make entity failed: " + e.toString());
            dispatchEvent(ActionType.WX_SEND_MSG_RESP_ERROR, msg);
        }

        // 网络请求
        String url = WeChatClient.getInstance().mUrlSupport.getUrl_webwxsendmsg();
        JsonObjectRequest sendmsgReq
                = new JsonObjectRequest(url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                PluginLogUtil.d(getToken(), "sendMsg::onResp: " + jsonObject);
                try {
                    JSONObject baseResponse = jsonObject.getJSONObject("BaseResponse");
                    int ret = baseResponse.getInt("Ret");
                    if (ret == 0) {
                        msg.mServerMsgId = Long.valueOf(jsonObject.getString("MsgID"));
                        // 根据消息类型对消息进行必要处理
                        if (WxMessage.MSG_TYPE_LOCATION == msg.mMsgType) {
                            if (TextUtils.isEmpty(msg.mAddress)) {
                                msg.mContent = getMsgContent(msg);
                            } else {
                                msg.mContent = "[" + msg.mAddress + "]";
                            }
                            PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOC_SEND_SUCCESS);
                        } else if (WxMessage.MSG_TYPE_VOICE == msg.mMsgType) {
                            msg.mContent = getMsgContent(msg);
                            PluginMonitorUtil.doMonitor(MonitorConsts.WX_VOICE_SEND_SUCCESS);
                        }

                        PluginMonitorUtil.doMonitor(MonitorConsts.WX_TEXT_SEND_SUCCESS);

                        dispatchEvent(ActionType.WX_SEND_MSG_RESP, msg);
                    } else {
                        PluginLogUtil.e(getToken(), "sendMsg::invalid ret: " + ret);
                        monitorSendMsgErr(msg.mMsgType);
                        dispatchEvent(ActionType.WX_SEND_MSG_RESP_ERROR, msg);
                    }
                } catch (JSONException e) {
                    monitorSendMsgErr(msg.mMsgType);
                    PluginLogUtil.e(getToken(),
                            "sendMsg::handling response encountered error: " + e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                monitorSendMsgErr(msg.mMsgType);
                dispatchEvent(ActionType.WX_SEND_MSG_RESP_ERROR, msg);
            }
        }, JSONBuilder.toPostString(params));
        WeChatClient.getInstance().mRequestQueue.add(sendmsgReq);
    }

    private void monitorSendMsgErr(int msgType) {
        switch (msgType) {
            case WxMessage.MSG_TYPE_LOCATION:
                PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOC_SEND_FAILED);
                break;

            case WxMessage.MSG_TYPE_VOICE:
                PluginMonitorUtil.doMonitor(MonitorConsts.WX_VOICE_SEND_FAILED);
                break;

            default:
                PluginMonitorUtil.doMonitor(MonitorConsts.WX_TEXT_SEND_FAILED);
                break;
        }
    }

    private void sendImageMsg(final WxMessage msg) {
        // create request body
        final ByteArrayOutputStream os = new ByteArrayOutputStream();

        StringBuilder postData = new StringBuilder();
        try {
            postData.append("---------------------------acebdf13572468\r\n");
            postData.append("Content-Disposition: form-data; name=\"filename\";" +
                    " filename=\"C:\\Users\\TXZ_ZACK\\AppData\\Local\\Temp" +
                    "\\{2CDEE1E0-7F89-4712-AD57-5AB453BAA04F}.tmp\"" + "\r\n");
            postData.append("Content-Type: image/jpeg" + "\r\n");
            postData.append("\r\n");
            os.write(postData.toString().getBytes("UTF-8"));

            FileInputStream is = new FileInputStream(msg.mImgCachePath);
            byte[] buff = new byte[2048];
            int hasRead = -1;
            while ((hasRead = is.read(buff)) > 0) {
                os.write(buff, 0, hasRead);
            }
            is.close();

            postData = new StringBuilder();
            postData.append("\r\n");
            postData.append("---------------------------acebdf13572468--\r\n\r\n");
            os.write(postData.toString().getBytes("UTF-8"));
        } catch (Exception e) {
            PluginMonitorUtil.doMonitor(MonitorConsts.WX_IMG_UPLOAD_FAILED);
            PluginLogUtil.e(getToken(),
                    "uploadImage::generating post data encountered error: " + e.toString());
            e.printStackTrace();
        }

        String url = WeChatClient.getInstance().mUrlSupport.getUrl_uploadImage();
        RawRequest request
                = new RawRequest(Request.Method.POST, url, new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] bytes) {
                try {
                    String jsonBody = new String(bytes, "UTF-8");
                    JSONObject jsonObject = new JSONObject(jsonBody);
                    sendImgById(msg, jsonObject.getString("MediaId"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                PluginMonitorUtil.doMonitor(MonitorConsts.WX_IMG_UPLOAD_FAILED);
                PluginLogUtil.e(getToken(), "uploadImage::doing upload request encountered error: "
                        + volleyError.getMessage());
            }
        }) {
            @Override
            public byte[] getBody() {
                return os.toByteArray();
            }
        };
        WeChatClient.getInstance().mRequestQueue.add(request);
    }

    private void sendImgById(final WxMessage msg, final String imgId) {
        JSONObject params = WeChatClient.getInstance().mUrlSupport.getBaseRequest();
        final JSONObject Msg = new JSONObject();
        try {
            Msg.put("LocalID", msg.mMsgId);
            Msg.put("ClientMsgId", msg.mMsgId);
            Msg.put("Type", WxMessage.MSG_TYPE_IMG);
            Msg.put("MediaId", imgId);
            Msg.put("FromUserName", msg.mSenderUserId);
            Msg.put("ToUserName", msg.mSessionId);
            params.put("Msg", Msg);
        } catch (Exception e) {
            PluginMonitorUtil.doMonitor(MonitorConsts.WX_IMG_SEND_FAILED);
            PluginLogUtil.e(getToken(), "sendImgById::generating param encountered error: "
                    + e.toString());
            return;
        }

        String url = WeChatClient.getInstance().mUrlSupport.getUrl_webWxsendImg();
        JsonObjectRequest sendmsgReq = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            JSONObject baseResponse = jsonObject.getJSONObject("BaseResponse");
                            int ret = baseResponse.getInt("Ret");
                            if (ret == 0) {
                                msg.mServerMsgId = Long.parseLong(jsonObject.getString("MsgID"));
                                msg.mContent = getMsgContent(msg);
                                dispatchEvent(ActionType.WX_SEND_MSG_RESP, msg);
                                PluginMonitorUtil.doMonitor(MonitorConsts.WX_IMG_SEND_SUCCESS);
                            } else {
                                PluginMonitorUtil.doMonitor(MonitorConsts.WX_IMG_SEND_FAILED);
                            }
                        } catch (JSONException e) {
                            PluginMonitorUtil.doMonitor(MonitorConsts.WX_IMG_SEND_FAILED);
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                PluginMonitorUtil.doMonitor(MonitorConsts.WX_IMG_SEND_FAILED);
                PluginLogUtil.e(getToken(), "sendImgById::doing request encountered error: "
                        + volleyError.getMessage());
            }
        }, JSONBuilder.toPostString(params));
        WeChatClient.getInstance().mRequestQueue.add(sendmsgReq);
    }

    public void revokeMessage(final WxMessage msg) {
        dispatchEvent(ActionType.WX_PLUGIN_REVOKE_MSG_REQUEST, msg);

        WeChatClient.getInstance().Api.webwxrevokemsg(new WeChatClient.WeChatResp<JSONObject>() {
            @Override
            public void onResp(JSONObject jsonObject) {
                PluginLogUtil.e(getToken(), jsonObject.toString());
                try {
                    JSONObject baseResponse = jsonObject.getJSONObject("BaseResponse");
                    int ret = baseResponse.getInt("Ret");

                    if (ret == 0) {
                        dispatchEvent(ActionType.WX_PLUGIN_REVOKE_MSG_SUCCESS, msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dispatchEvent(ActionType.WX_PLUGIN_REVOKE_MSG_FAILED, msg);
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                dispatchEvent(ActionType.WX_PLUGIN_REVOKE_MSG_FAILED, msg);
            }
        }, msg);
    }
}
