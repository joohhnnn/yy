package com.txznet.music.service.impl;

import android.util.ArrayMap;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.music.Constant;
import com.txznet.music.action.WxPushActionCreator;
import com.txznet.music.data.entity.QrCodeInfo;
import com.txznet.music.helper.TXZNetRequest;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;

import java.util.Map;
import java.util.Set;

/**
 * 网络请求相关的命令字
 *
 * @author telen
 * @date 2018/12/28,11:07
 */
public class NetCommand extends BaseCommand {


    /**
     * 单例对象
     */
    private volatile static NetCommand singleton;


    private Map<String, ExcuteRequest> requestMap = new ArrayMap<>(3);
    public static final String mStrReqQrcodeCMD = "wx.subscribe.qrcode";
    private static final String mStrRespQrcodeCMD = "wx.qrcode.broadcast";
    public static final String mStrDataInterface = "txz.music.dataInterface";

    public static NetCommand getInstance() {
        if (singleton == null) {
            synchronized (NetCommand.class) {
                if (singleton == null) {
                    singleton = new NetCommand();
                }
            }
        }
        return singleton;
    }


    private NetCommand() {
//        网络请求的统一响应的位置
        requestMap.put(mStrDataInterface, new ExcuteRequest() {

            @Override
            public void executeCore(byte[] params) {
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, mStrDataInterface, params, null);
            }

            @Override
            public void executeProgram(byte[] params) {
//                EventManager.getInstance().sendEvent(
//                        UiEvent.EVENT_ACTION_AUDIO,
//                        UiAudio.SUBEVENT_REQ_DATA_INTERFACE,
//                        params);
            }

            @Override
            public void regProgramResponse() {
//                EventManager.getInstance().regEvent(
//                        UiEvent.EVENT_ACTION_AUDIO,
//                        UiAudio.SUBEVENT_RESP_DATA_INTERFACE,
//                        (eventId, subEventId, data) -> {
//                            if (eventId == UiEvent.EVENT_ACTION_AUDIO) {
//                                if (UiAudio.SUBEVENT_RESP_DATA_INTERFACE == subEventId) {
//                                    TXZNetRequest.get().handleDataInterface(data);
//                                }
//                            }
//                            return 0;
//                        });
            }

            @Override
            public void regCoreResponse() {
                addCmd("dataInterface", (pkgName, cmd, data) -> TXZNetRequest.get().handleDataInterface(data));
            }
        });

        requestMap.put(mStrReqQrcodeCMD, new ExcuteRequest() {

            @Override
            public void executeCore(byte[] params) {
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, mStrReqQrcodeCMD, params, null);
            }

            @Override
            public void executeProgram(byte[] params) {
//                EventManager.getInstance().sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_BIND_WX_URL, params);
            }

            @Override
            public void regProgramResponse() {
//                EventManager.getInstance().regEvent(
//                        UiEvent.EVENT_ACTION_EQUIPMENT,
//                        UiEquipment.SUBEVENT_RESP_GET_BIND_WX_URL,
//                        (eventId, subEventId, data) -> {
//                            if (eventId == UiEvent.EVENT_ACTION_EQUIPMENT) {
//                                if (UiEquipment.SUBEVENT_RESP_GET_BIND_WX_URL == subEventId) {
//                                    // TODO: 2019/1/9 车车互联二维码 json串
//                                    if (BuildConfig.DEBUG) {
//                                        Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",regProgramResponse：" + new String(data));
//                                    }
//
//                                }
//                            }
//                            return 0;
//                        });
//                EventManager.getInstance().regEvent(
//                        UiEvent.EVENT_ACTION_EQUIPMENT,
//                        UiEquipment.SUBEVENT_NOTIFY_BIND_WX_SUCCESS,
//                        (eventId, subEventId, data) -> {
//                            if (eventId == UiEvent.EVENT_ACTION_EQUIPMENT) {
//                                if (UiEquipment.SUBEVENT_NOTIFY_BIND_WX_SUCCESS == subEventId) {
//                                    // TODO: 2019/1/9 String 是否绑定
//                                    if (BuildConfig.DEBUG) {
//                                        Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",regProgramResponse:bindSuccess:" + new String(data));
//                                    }
//                                }
//                            }
//                            return 0;
//                        });
            }

            @Override
            public void regCoreResponse() {
                addCmd("wx.info.qrcode", (pkgName, cmd, data) -> {
                    //1.主动发起请求的时候，会通过这个响应回来，2.解绑的时候也会受到响应
                    //{"uint64Flag":32,"nick":"晴空一垩","issuccess":true,"isbind":true,"qrcode":"http:\/\/weixin.qq.com\/q\/02NZBqE-FRea41uZhahscU"}
                    //{"issuccess":true,"isbind":false,"qrcode":"http:\/\/weixin.qq.com\/q\/02SgeYFEFRea41r-_bhscI"}
                    return updateQrcodeInfo(data);
                });
                addCmd("wx.info.nick", (pkgName, cmd, data) -> {
                    //1.绑定的时候会通过这个状态回调
                    return updateQrcodeInfo("{\"isbind\":true}".getBytes());
                });
//                addCmd(mStrRespQrcodeCMD, (pkgName, cmd, data) -> {
//                    //这个通知已经被弃用
////                    {"url":"http:\/\/weixin.qq.com\/q\/02dvNlFtFRea41omg51scc","isBind":false}
//                    return updateQrcodeInfo(data);
//                });
            }
        });
        //推送走的是不同的通道
        requestMap.put("", new ExcuteRequest() {

            @Override
            public void executeCore(byte[] params) {
            }

            @Override
            public void executeProgram(byte[] params) {
            }

            @Override
            public void regProgramResponse() {
//                EventManager.getInstance().regEvent(
//                        UiEvent.EVENT_ACTION_AUDIO,
//                        UiAudio.SUBEVENT_DATA_PUSH_INTERFACE,
//                        (eventId, subEventId, data) -> {
//                            if (eventId == UiEvent.EVENT_ACTION_AUDIO) {
//                                if (UiAudio.SUBEVENT_DATA_PUSH_INTERFACE == subEventId) {
//                                    PushCommand.getInstance().invoke(null, "dataPushInterface", data);
//                                }
//                            }
//                            return 0;
//                        });
            }

            @Override
            public void regCoreResponse() {
            }
        });


        //注册
        if (false) {
            Set<String> cmds = requestMap.keySet();
            for (String cmd : cmds) {
                requestMap.get(cmd).regProgramResponse();
            }
        } else {
            Set<String> cmds = requestMap.keySet();
            for (String cmd : cmds) {
                requestMap.get(cmd).regCoreResponse();
            }
        }
    }

    private byte[] updateQrcodeInfo(byte[] data) {
        Logger.d(Constant.LOG_TAG_WX_PUST, "updateQrcodeInfo:" + new String(data));
        QrCodeInfo qrCodeInfo = JsonHelper.fromJson(new String(data), QrCodeInfo.class);
        WxPushActionCreator.getInstance().updateWxPushQRCode(qrCodeInfo);
        return new byte[0];
    }

    public void request(byte[] dataBytes) {
        request(mStrDataInterface, dataBytes);
    }

    public void request(String cmd, byte[] params) {
        if (false) {
            requestMap.get(cmd).executeProgram(params);
        } else {
            requestMap.get(cmd).executeCore(params);
        }
    }

    private interface ExcuteRequest {
//
//        String getCmd();

//        byte[] getParam();

        /**
         * 请求Core的方式
         */
        void executeCore(byte[] params);

        /**
         * 请求小程序的方式
         */
        void executeProgram(byte[] params);

        void regProgramResponse();

        void regCoreResponse();
    }
}

