package com.txznet.webchat.plugin.preset.logic.module;

import android.os.Bundle;
import android.text.TextUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.comm.plugin.utils.PluginLogUtil;
import com.txznet.webchat.comm.plugin.utils.PluginMonitorUtil;
import com.txznet.webchat.plugin.preset.logic.action.ActionType;
import com.txznet.webchat.plugin.preset.logic.api.WeChatClient;
import com.txznet.webchat.plugin.preset.logic.base.WxModule;
import com.txznet.webchat.plugin.preset.logic.consts.MonitorConsts;
import com.txznet.webchat.plugin.preset.logic.http.RawRequest;
import com.txznet.webchat.plugin.preset.logic.util.ThreadManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by J on 2016/11/23.
 */

public class WxResourceModule extends WxModule {
    private static final String MODULE_TOKEN = "wx_resource_module";

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public String getToken() {
        return MODULE_TOKEN;
    }

    @Override
    public void reset() {

    }

    private static WxResourceModule sInstance = new WxResourceModule();

    public static WxResourceModule getInstance() {
        return sInstance;
    }

    private WxResourceModule() {

    }


    //// TODO: 2016/11/23 添加下载失败重试逻辑
    public void downloadContactImage(final WxContact con, final String path) {
        if (null == con) {
            PluginLogUtil.e(getToken(), "download contact image failed: contact is null");
            return;
        }

        RawRequest getImgRequest = new RawRequest(con.mHeadImgUrl, new Response.Listener<byte[]>() {
            @Override
            public void onResponse(final byte[] data) {
                ThreadManager.getPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (0 == data.length) {
                            PluginLogUtil.e(getToken(), "download contact image failed: data is empty, id = " + con.mUserOpenId);
                            return;
                        }

                        FileOutputStream fos = null;
                        try {
                            File file = new File(path);
                            if (file.exists() && file.length() != 0) {
                                return;
                            }
                            file.getParentFile().mkdirs();
                            fos = new FileOutputStream(file);
                            fos.write(data);
                            Bundle bundle = new Bundle();
                            bundle.putString("uid", con.mUserOpenId);
                            dispatchEvent(ActionType.WX_DOWNLOAD_IMAGE_RESP, bundle);
                        } catch (IOException e) {
                            PluginLogUtil.e(getToken(), "download contact image failed: " + e.getMessage());
                        } finally {
                            if (fos != null) {
                                try {
                                    fos.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                PluginLogUtil.e(getToken(), "download image failed: " + volleyError.toString());
                PluginMonitorUtil.doMonitor(MonitorConsts.WX_RES_GET_AVATAR_FAILED);
            }
        });
        getImgRequest.setShouldCache(true);
        WeChatClient.getInstance().mRequestQueue.add(getImgRequest);
    }

    public void downloadVoice(final WxMessage message, final String path) {
        dispatchEvent(ActionType.WX_DOWNLOAD_VOICE_REQ, message);

        if (new File(path).exists()) {
            message.mVoiceCachePath = path;
            dispatchEvent(ActionType.WX_DOWNLOAD_VOICE_RESP, message);
        }

        if (TextUtils.isEmpty(message.mVoiceUrl)) {
            PluginLogUtil.w(getToken(), "downloadVoice::voice url is empty!");
            dispatchEvent(ActionType.WX_DOWNLOAD_VOICE_RESP_ERROR, message);
        }

        try {
            RawRequest getVoiceReq = new RawRequest(message.mVoiceUrl, new Response.Listener<byte[]>() {
                @Override
                public void onResponse(final byte[] resp) {
                    ThreadManager.getPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            FileOutputStream fos = null;
                            try {
                                File file = new File(path);
                                if (!file.exists()) {
                                    file.getParentFile().mkdirs();
                                    fos = new FileOutputStream(file);
                                    fos.write(resp);
                                }
                                message.mVoiceCachePath = path;
                                PluginMonitorUtil.doMonitor(MonitorConsts.WX_VOICE_DOWNLOAD_SUCCESS);
                                dispatchEvent(ActionType.WX_DOWNLOAD_VOICE_RESP, message);
                            } catch (IOException e) {
                                PluginLogUtil.e(getToken(), "downloadVoice saving encountered error: " + e.toString());
                                PluginMonitorUtil.doMonitor(MonitorConsts.WX_VOICE_DOWNLOAD_FAILED_SAVE);
                            } finally {
                                if (fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                    }
                                }
                            }
                        }
                    });
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    PluginMonitorUtil.doMonitor(MonitorConsts.WX_VOICE_DOWNLOAD_FAILED);
                    dispatchEvent(ActionType.WX_DOWNLOAD_VOICE_RESP_ERROR, message);
                }
            });
            getVoiceReq.setShouldCache(true);
            WeChatClient.getInstance().mRequestQueue.add(getVoiceReq);
        } catch (Exception e) {
            PluginLogUtil.e(getToken(), "downloadVoice encountered error: " + e.toString());
        }
    }
}
