package com.txznet.webchat.plugin.preset;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.webchat.comm.plugin.base.WxPlugin;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.comm.plugin.model.WxUserCache;
import com.txznet.webchat.comm.plugin.utils.PluginLogUtil;
import com.txznet.webchat.comm.plugin.utils.PluginMonitorUtil;
import com.txznet.webchat.comm.plugin.utils.PluginTaskRunner;
import com.txznet.webchat.plugin.preset.logic.BuildConfig;
import com.txznet.webchat.plugin.preset.logic.action.ActionType;
import com.txznet.webchat.plugin.preset.logic.action.PluginInvokeAction;
import com.txznet.webchat.plugin.preset.logic.api.WeChatClient;
import com.txznet.webchat.plugin.preset.logic.api.resp.JsLoginResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.LoginResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.PushLoginResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.WebWxInitResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.WebWxNewLoginPageResp;
import com.txznet.webchat.plugin.preset.logic.consts.MonitorConsts;
import com.txznet.webchat.plugin.preset.logic.http.SimpleErrorListener;
import com.txznet.webchat.plugin.preset.logic.module.WxContactModule;
import com.txznet.webchat.plugin.preset.logic.module.WxMessageModule;
import com.txznet.webchat.plugin.preset.logic.module.WxResourceModule;
import com.txznet.webchat.plugin.preset.logic.util.WxCacheManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 微信核心逻辑plugin
 * Created by ASUS User on 2016/8/16.
 */
public class WxLogicPlugin extends WxPlugin {
    public static final String PLUGIN_TOKEN = "wx_logic_plugin";
    public static final int PLUGIN_VERSION_CODE = BuildConfig.VERSION_CODE;
    public static final String PLUGIN_VERSION_NAME = BuildConfig.VERSION_NAME;

    private static final String SYSTEM_POWER_SLEEP = "system_power_sleep"; // 系统休眠
    private static final String SYSTEM_POWER_WAKEUP = "system_power_wakeup"; // 系统休眠

    private final static int MSG_SYNC_RET_LOGOUT_FROM_MOBILE = 1101; //手机侧注销
    private final static int MSG_SYNC_RET_LOGOUT_RET_CODE_1100 = 1100; //未知
    private final static int MSG_SYNC_RET_LOGOUT_RET_CODE_1102 = 1102; //未知

    private static final int RETRY_PUSH_LOGIN_TIME_MAX = 3; // 唤醒推送登录最大重试次数
    private static final int RETRY_PUSH_LOGIN_INTERVAL = 7 * 1000; // 唤醒推送登录重试间隔

    private boolean bSleepMode = false; // 休眠状态标志位

    // Modules
    private WxContactModule mContactModule;
    private WxMessageModule mMessageModule;
    private WxResourceModule mResModule;

    public static boolean sIsLoggedIn = false;

    public WxLogicPlugin() {
        // 初始化Modules
        mContactModule = WxContactModule.getInstance();
        mMessageModule = WxMessageModule.getInstance();
        mResModule = WxResourceModule.getInstance();
    }

    @Override
    public int getVersionCode() {
        return PLUGIN_VERSION_CODE;
    }

    @Override
    public String getVersionName() {
        return PLUGIN_VERSION_NAME;
    }

    @Override
    public String getToken() {
        return PLUGIN_TOKEN;
    }

    @Override
    public Object backup() {
        return null;
    }

    @Override
    public boolean restore(Object data) {
        return false;
    }

    @Override
    public void invoke(String cmd, Object... arg) {
        if (TextUtils.isEmpty(cmd)) {
            return;
        }
        switch (cmd) {
            case PluginInvokeAction.INVOKE_CMD_GET_USER_CACHE:
                notifyUserCacheList();
                break;

            case PluginInvokeAction.INVOKE_CMD_CLEAR_CACHE:
                doClearUserCache();
                break;

            case PluginInvokeAction.INVOKE_CMD_PUSH_LOGIN:
                PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOGIN_PUSH_ENTER);
                doPushLogin((String) arg[0]);
                break;

            case PluginInvokeAction.INVOKE_CMD_REFRESH_LOGIN_QR:
                getQrCode();
                break;

            case PluginInvokeAction.INVOKE_CMD_LOGOUT:
                doLogout((Boolean) arg[0]);
                break;

            case PluginInvokeAction.INVOKE_CMD_SLEEP:
                doSleep(arg);
                break;

            case PluginInvokeAction.INVOKE_CMD_WAKEUP:
                doWakeup(arg);
                break;

            // 资源下载相关调用
            case PluginInvokeAction.INVOKE_CMD_GET_USER_HEAD:
                mResModule.downloadContactImage((WxContact) arg[0], (String) arg[1]);
                break;

            case PluginInvokeAction.INVOKE_CMD_GET_VOICE:
                mResModule.downloadVoice((WxMessage) arg[0], (String) arg[1]);
                break;

            // 发消息
            case PluginInvokeAction.INVOKE_CMD_SEND_MSG:
                mMessageModule.sendMessage((WxMessage) arg[0]);
                break;

            // 撤回消息
            case PluginInvokeAction.INVOKE_CMD_REVOKE_MSG:
                mMessageModule.revokeMessage((WxMessage) arg[0]);
                break;
        }
    }

    private boolean bAutoLogin = false; // 当前是否启用了唤醒自动推送登录

    private void doWakeup(Object... arg) {
        if (!bSleepMode) {
            PluginLogUtil.i(getToken(), "doWakeup: not in sleep mode, skip");
            return;
        }

        bSleepMode = false;

        // 判断当前是否开启了唤醒自动推送登录
        Bundle bundle = (Bundle) arg[0];
        bAutoLogin = bundle.getBoolean("autoLogin");

        if (sIsLoggedIn) {
            checkSync();
        } else if (bAutoLogin) {
            // 当前没有登录， 启动自动登录逻辑
            //doAutoLogin();
            getQrCode();
        } else {
            getQrCode();
        }

        dispatchEvent(SYSTEM_POWER_WAKEUP, null);
    }

    private void doSleep(Object... arg) {
        if (bSleepMode) {
            PluginLogUtil.i(getToken(), "doSleep: already in sleep mode, skip");
            return;
        }

        bSleepMode = true;
        WeChatClient.getInstance().cancelAll();

        dispatchEvent(SYSTEM_POWER_SLEEP, null);
    }

    private void reset() {
        WeChatClient.getInstance().restart();
        PluginTaskRunner.removeUiGroundCallback(mRetryCheckAuthTask);
        PluginTaskRunner.removeUiGroundCallback(mRetryCheckSyncTask);
        PluginTaskRunner.removeUiGroundCallback(mRetryGetQrCodeTask);
        PluginTaskRunner.removeUiGroundCallback(mRetrySyncChangeTask);

        mResModule.reset();
        mContactModule.reset();
        mMessageModule.reset();

        mCurRetry = 0;
    }

    private void doClearUserCache() {
        WxCacheManager.getInstance().clearCache();
    }

    private void notifyUserCacheList() {
        List<WxUserCache> cacheList = WxCacheManager.getInstance().getUserList();

        PluginLogUtil.d(getToken(), "got user cache, size = " + cacheList.size());
        dispatchEvent(ActionType.WX_PLUGIN_PUSH_LOGIN_SYNC_CONTACT, cacheList);
    }

    private void doPushLogin(String uid) {
        // 重置WechatClient
        reset();
        // 切换缓存用户
        WxCacheManager.getInstance().switchUser(uid);

        // 先检测有无历史登录记录
        //WeChatClient.getInstance().restore();
        //if (!TextUtils.isEmpty(WeChatClient.getInstance().ClientData.mUUID)) {
        if (!TextUtils.isEmpty(WxCacheManager.getInstance().getUin())) {
            PluginLogUtil.d(getToken(), "found local uuid, push login");
            dispatchEvent(ActionType.WX_PLUGIN_PUSH_LOGIN_REQUEST, WxCacheManager.getInstance().getAvatar());

            WeChatClient.getInstance().Api.pushLogin(new WeChatClient.WeChatResp<PushLoginResp>() {
                @Override
                public void onResp(PushLoginResp pushLoginResp) {
                    if (0 == pushLoginResp.ret) {
                        PluginLogUtil.d(getToken(), "push login success");
                        PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOGIN_PUSH_SUCCESS);
                        dispatchEvent(ActionType.WX_PLUGIN_PUSH_LOGIN_SUCCESS, WxCacheManager.getInstance().getAvatar());
                        checkLogin();
                    } else {
                        PluginLogUtil.d(getToken(), "push login failed, ret = " + pushLoginResp.ret + ", msg = " + pushLoginResp.msg);
                        PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOGIN_PUSH_FAILED_RET);
                        getQrCode();
                    }
                }

                @Override
                public void onError(int statusCode, String message) {
                    if (SimpleErrorListener.CONNECTION_ERROR == statusCode) {
                        if (doRetryPushLogin()) {
                            return;
                        }
                    }

                    PluginLogUtil.d(getToken(), "push login failed: " + message);
                    PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOGIN_PUSH_FAILED_NET);
                    dispatchEvent(ActionType.WX_PLUGIN_PUSH_LOGIN_FAILED, null);
                    getQrCode();
                }
            });
        } else {
            PluginLogUtil.d(getToken(), "no local uuid, get QRCode");
            dispatchEvent(ActionType.WX_PLUGIN_PUSH_LOGIN_FAILED, null);
            getQrCode();
        }
    }

    private int mRetryPushLoginTimes = 0;

    private boolean doRetryPushLogin() {
        if (mRetryPushLoginTimes++ < RETRY_PUSH_LOGIN_TIME_MAX) {
            PluginLogUtil.i(getToken(), "retry push login times: " + mRetryPushLoginTimes);
            PluginTaskRunner.runOnBackGround(mRetryPushLoginTask, RETRY_PUSH_LOGIN_INTERVAL);
            return true;
        }

        mRetryPushLoginTimes = 0;
        return false;
    }

    private Runnable mRetryPushLoginTask = new Runnable() {
        @Override
        public void run() {
            // 重试时已切换用户，可以直接取
            doPushLogin(WxCacheManager.getInstance().getUin());
        }
    };

    private void getQrCode() {
        //dispatchEvent(ActionType.WX_PLUGIN_LOGIC_RESET, null);
        WxCacheManager.getInstance().switchUser(null);
        reset();
        dispatchEvent(ActionType.WX_QRCODE_REQUEST, null);

        if (bSleepMode) {
            PluginLogUtil.i(getToken(), "getQrCode: sleep mode , skip");
            return;
        }

        WeChatClient.getInstance().Api.jslogin(new WeChatClient.WeChatResp<JsLoginResp>() {
            @Override
            public void onResp(JsLoginResp jsLoginResp) {
                if (jsLoginResp.window_QRLogin_code != 200) {
                    getQrCodeAgain();
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putInt("code", jsLoginResp.window_QRLogin_code);
                bundle.putString("qr_code", WeChatClient.getInstance().getQrCodeImage());
                bundle.putString("uuid", jsLoginResp.window_QRLogin_uuid);
                PluginLogUtil.d(getToken(), "getQrCode:onResp, QrCode: https://login.weixin.qq.com/qrcode/" + jsLoginResp.window_QRLogin_uuid);

                dispatchEvent(ActionType.WX_QRCODE_SUCCESS, bundle);
                mRetryGetQRTimes = 0;

                checkLogin();
            }

            @Override
            public void onError(int statusCode, String message) {
                PluginLogUtil.d(getToken(), "getQrCode:onError, statusCode=" + statusCode + ", msg=" + message);
                // 自动重试时不发布二维码获取失败事件, 改为连续重试失败停止请求时发送
                //dispatchEvent(ActionType.WX_QRCODE_FAIL, null);
                getQrCodeAgain();
            }
        });


    }


    private int mRetryGetQRTimes = 0;

    private void getQrCodeAgain() {
        if (mRetryGetQRTimes++ < 5) {
            PluginTaskRunner.removeUiGroundCallback(mRetryGetQrCodeTask);
            PluginTaskRunner.runOnUiGround(mRetryGetQrCodeTask, 1000 * 3);
            return;
        }

        mRetryGetQRTimes = 0;
        dispatchEvent(ActionType.WX_QRCODE_FAIL, null);
        PluginLogUtil.e(getToken(), "getQRCode failed 5 times, stop retrying");
    }

    private void checkLogin() {
        if (bSleepMode) {
            PluginLogUtil.i(getToken(), "checkLogin: sleep mode , skip");
            return;
        }

        WeChatClient.getInstance().Api.login(new WeChatClient.WeChatResp<LoginResp>() {
            @Override
            public void onResp(LoginResp loginResp) {

                switch (loginResp.window_code) {
                    case 408: //正常状态
                        checkLogin();
                        break;
                    case 201: // 设备扫码二维码
                        Bundle bundle = new Bundle();
                        bundle.putInt("code", loginResp.window_code);
                        bundle.putString("uri", loginResp.window_redirect_uri);
                        bundle.putString("userAvatar", loginResp.window_user_avatar);
                        PluginLogUtil.d(getToken(), "checkLogin:onResp, code=" + loginResp.window_code);

                        dispatchEvent(ActionType.WX_QRCODE_SCAN, bundle);
                        PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOGIN_QR_SCANNED);
                        checkLogin();
                        break;
                    case 400: // 二维码失效
                        getQrCode();
                        break;
                    case 200: // 用户确认，检测权限
                        checkAuth();
                        break;
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                PluginLogUtil.d(getToken(), "checkLogin:onError, statusCode=" + statusCode + ", msg=" + message);

                retryCheckLogin();
            }
        });
    }

    private void checkAuth() {
        WeChatClient.getInstance().Api.webwxnewloginpage(new WeChatClient.WeChatResp<WebWxNewLoginPageResp>() {
            @Override
            public void onResp(WebWxNewLoginPageResp webWxNewLoginPageResp) {
                Bundle bundle = new Bundle();
                bundle.putInt("ret", webWxNewLoginPageResp.ret);
                PluginLogUtil.d(getToken(), "checkAuth:onResp, ret=" + webWxNewLoginPageResp.ret);
                if (webWxNewLoginPageResp.ret == 0) {
                    //成功
                    doLogin();
                } else {
                    //失败
                    getQrCode();
                    PluginMonitorUtil.monitorAuthRet(webWxNewLoginPageResp.ret);
                    Toast.makeText(GlobalContext.get(), "服务器异常导致登录失败，请稍候再试。",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                PluginLogUtil.d(getToken(), "checkAuth:onError, statusCode=" + statusCode + ", msg=" + message);

                retryCheckAuth();
            }
        });
    }

    private void doLogin() {
        final AtomicBoolean errDone = new AtomicBoolean(false);
        final AtomicBoolean respDone = new AtomicBoolean(false);
        WeChatClient.WeChatResp<WebWxInitResp> respWeChatResp = new WeChatClient.WeChatResp<WebWxInitResp>() {
            @Override
            public void onResp(WebWxInitResp webWxInitResp) {
                if (respDone.compareAndSet(false, true)) {
                    WeChatClient.getInstance().cancelAll("webwxinit");
                    respDone.getAndSet(true);
                    PluginLogUtil.d(getToken(), "doLogin:onResp, ret=" + webWxInitResp.BaseResponse.Ret);
                    if (webWxInitResp.BaseResponse.Ret != 0) {
                        retryDoLogin();
                        PluginMonitorUtil.monitorInitRet(webWxInitResp.BaseResponse.Ret);
                    } else {
                        sIsLoggedIn = true;
                        notifyLogin();
                        checkSync();
                        mContactModule.startResolveContact(webWxInitResp);

                        PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOGIN_SUCCESS);
                        // 保存登录信息
                        WxCacheManager.getInstance().increaseHitCount();
                        WxCacheManager.getInstance().save();

                        dispatchLoginSuccess();
                    }
                }
            }

            @Override
            public void onError(int statusCode, String message) {

                if (errDone.compareAndSet(false, true)) {
                    if (respDone.get()) {
                        return;
                    }

                    PluginLogUtil.d(getToken(), "doLogin:onError, statusCode=" + statusCode + ", msg=" + message);
                    dispatchEvent(ActionType.WX_LOGIN_FAIL, null);

                    doLogout(true);
                    WeChatClient.getInstance().reset();
                }
            }
        };
        WeChatClient.getInstance().Api.webwxinit(respWeChatResp);
        WeChatClient.getInstance().Api.webwxinit(respWeChatResp);
    }

    private void dispatchLoginSuccess() {
        Bundle bundle = new Bundle();
        String uin = WxCacheManager.getInstance().getUin();
        int hitCount = WxCacheManager.getInstance().getHitCount();

        bundle.putString("uin", uin);
        bundle.putInt("hitCount", hitCount);
        PluginLogUtil.i(getToken(), "dispatchLoginSuccess, uin = " + uin + ", hitCount = " + hitCount);
        dispatchEvent(ActionType.WX_LOGIN_SUCCESS, bundle);
    }

    private void notifyLogin() {
        WeChatClient.getInstance().Api.webwxstatusnotify(new WeChatClient.WeChatResp<String>() {
            @Override
            public void onResp(String s) {
                PluginLogUtil.d(getToken(), "notifylogin success");
            }

            @Override
            public void onError(int statusCode, String message) {
                PluginLogUtil.d(getToken(), "notifylogin error: " + message);
            }
        });
    }

    private void doLogout(boolean exitWx) {
//        TXZReportActionCreator.getInstance().reportLogout(false);
        sIsLoggedIn = false;
        dispatchEvent(ActionType.WX_LOGOUT_REQUEST, null);
        dispatchEvent(ActionType.WX_PLUGIN_LOGIC_RESET, null);
        if (exitWx) {
            WeChatClient.WeChatResp<String> resp = new WeChatClient.WeChatResp<String>() {
                @Override
                public void onResp(String s) {
                    PluginLogUtil.d(getToken(), "doLogout:onResp");
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("exitWx", true);
//                    dispatcher.dispatch(new Action<Bundle>(ActionType.WX_LOGOUT_RESP, bundle));
                    dispatchEvent(ActionType.WX_LOGOUT_SUCCESS, bundle);
                    //WxCacheManager.getInstance().save();
                }

                @Override
                public void onError(int statusCode, String message) {
                    PluginLogUtil.d(getToken(), "doLogout:onError, statusCode=" + statusCode + ", msg=" + message);
//                    dispatcher.dispatch(new Action<String>(ActionType.WX_LOGOUT_RESP_ERROR, null));
                    dispatchEvent(ActionType.WX_LOGOUT_FAIL, null);
                }
            };
            WeChatClient.getInstance().Api.webwxlogout(resp);
        } else {
            dispatchEvent(ActionType.WX_LOGOUT_SUCCESS, null);
        }
    }

    private boolean checkLogoutRetCode(int retCode) {
        if (!sIsLoggedIn) {
            return true;
        }

        // 唤醒自动推送登录处理
        if (0 != retCode && bAutoLogin) {
            // 若当前开启了唤醒自动推送，且本次恢复登录状态失败
            // 触发自动推送登录逻辑
            if (doAutoLogin()) {
                return true;
            }
        }

        bAutoLogin = false;

        if (MSG_SYNC_RET_LOGOUT_FROM_MOBILE == retCode) {
            PluginLogUtil.d(getToken(), "checkLogoutRetCode, ret=" + retCode);
            doLogout(true);
            return true;
        }

        if (MSG_SYNC_RET_LOGOUT_RET_CODE_1100 == retCode || MSG_SYNC_RET_LOGOUT_RET_CODE_1102 == retCode) {
            PluginLogUtil.d(getToken(), "checkLogoutRetCode, ret=" + retCode);
            doLogout(true);
            return true;
        }

        return false;
    }

    private boolean doAutoLogin() {
        bAutoLogin = false;
        // 选择最近的登陆人
        String currentUin = WxCacheManager.getInstance().getLastUin();
        if (TextUtils.isEmpty(currentUin)) {
            PluginLogUtil.i(getToken(), "performing auto login: no recent user found");
            return false;
        }

        PluginLogUtil.i(getToken(), "performing auto login for user: " + currentUin);

        // 重置逻辑
        reset();
        dispatchEvent(ActionType.WX_PLUGIN_LOGIC_RESET, null);

        Bundle bundle = new Bundle();
        bundle.putString("uin", WxCacheManager.getInstance().getUin());
        bundle.putString("avatar", WxCacheManager.getInstance().getAvatar());
        dispatchEvent(ActionType.WX_PLUGIN_AUTO_LOGIN_REQUEST, bundle);
        doPushLogin(currentUin);
        return true;
    }

    private void checkSync() {
        if (bSleepMode) {
            PluginLogUtil.i(getToken(), "checkSync: sleep mode , skip");
            return;
        }

        PluginLogUtil.d(getToken(), "start checksync");
        WeChatClient.getInstance().Api.synccheck(new WeChatClient.WeChatResp<String>() {
            @Override
            public void onResp(String s) {
                try {
                    PluginLogUtil.d(getToken(), "sync resp: " + s);
                    JSONObject res = new JSONObject(s.substring("window.synccheck=".length()));
                    int ret = res.getInt("retcode");
                    String selector = res.getString("selector");

                    if (checkLogoutRetCode(ret)) {
                        return;
                    }

                    // 重置重试次数
                    mCurRetry = 0;

                    if ("0".equals(selector)) {
                        checkSync(); // 开始下一轮检测
                    } else {
                        syncChange();
                    }
                } catch (JSONException e) {
                    PluginLogUtil.e(getToken(), "checkSync encountered error: " + e.getMessage());
                    e.printStackTrace();
                }

            }

            @Override
            public void onError(int statusCode, String message) {
                PluginLogUtil.d(getToken(), "checkSync:onError, statusCode=" + statusCode + ", msg=" + message);

                retryCheckSync();
            }
        });
    }

    private void syncChange() {
        if (bSleepMode) {
            PluginLogUtil.i(getToken(), "syncChange: sleep mode , skip");
            return;
        }

        WeChatClient.getInstance().Api.webwxsync(new WeChatClient.WeChatResp<JSONObject>() {
            @Override
            public void onResp(JSONObject jsonObject) {

                try {
                    JSONObject baseResponse = jsonObject.getJSONObject("BaseResponse");
                    int ret = baseResponse.getInt("Ret");
                    PluginLogUtil.d(getToken(), "syncChange: ret = " + ret);

                    if (ret == 0) {
                        // 重置重试次数
                        mCurRetry = 0;

                        JSONArray addMsgList = jsonObject.getJSONArray("AddMsgList");
                        if (addMsgList.length() > 0) {
                            mMessageModule.resolveMessage(addMsgList);
                        }
                        JSONArray modContactList = jsonObject.getJSONArray("ModContactList");
                        if (modContactList.length() > 0) {
                            mContactModule.modContact(modContactList);
                        }
                        JSONArray deleteContactList = jsonObject.getJSONArray("DelContactList");
                        if (deleteContactList.length() > 0) {
                            mContactModule.delContact(deleteContactList);
                        }
                        checkSync();
                    } else {
                        if (!checkLogoutRetCode(ret)) {
                            retrySyncChange();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    PluginLogUtil.d(getToken(), "syncChange:onException, e = " + e.toString() + ", raw = " + jsonObject.toString());
                    retrySyncChange();
                }
            }

            @Override
            public void onError(int statusCode, String message) {
                PluginLogUtil.d(getToken(), "syncChange:onError, statusCode=" + statusCode + ", msg=" + message);

                retrySyncChange();
            }
        });
    }

    private int mCurRetry = 0;
    private final int RETRY_LIMIT = 5;

    private void retryCheckLogin() {
        if (mCurRetry < RETRY_LIMIT) {
            checkLogin();
        } else {
            PluginTaskRunner.removeUiGroundCallback(mRetryGetQrCodeTask);
            PluginTaskRunner.runOnUiGround(mRetryGetQrCodeTask, 1000 * 6);
            mCurRetry = 0;
        }
        mCurRetry++;
    }

    private void retryCheckAuth() {
        if (mCurRetry < RETRY_LIMIT) {
            PluginTaskRunner.removeUiGroundCallback(mRetryCheckAuthTask);
            PluginTaskRunner.runOnUiGround(mRetryCheckAuthTask, 1000);
            mCurRetry++;
        } else {
            // 重新登录
            getQrCode();
            PluginMonitorUtil.doMonitor(MonitorConsts.WX_LOGIN_FAILED_CHECK_AUTH);
            mCurRetry = 0;
        }
    }

    private void retryDoLogin() {
        if (mCurRetry < RETRY_LIMIT) {
            checkAuth();
            mCurRetry++;
        } else {
            dispatchEvent(ActionType.WX_LOGIN_FAIL, null);
            doLogout(true);
            mCurRetry = 0;
        }
    }


    private void retryCheckSync() {
        if (mCurRetry < RETRY_LIMIT) {
            PluginTaskRunner.removeUiGroundCallback(mRetryCheckSyncTask);
            PluginTaskRunner.runOnUiGround(mRetryCheckSyncTask, 4000);
            mCurRetry++;
        } else {
            mCurRetry = 0;
            doLogout(true);
            PluginMonitorUtil.doMonitor(MonitorConsts.WX_SYNC_FAILED_RETRY_LIMIT);
        }
    }

    private void retrySyncChange() {
        if (mCurRetry < RETRY_LIMIT) {
            PluginTaskRunner.removeBackGroundCallback(mRetrySyncChangeTask);
            PluginTaskRunner.runOnBackGround(mRetrySyncChangeTask, 4000);
            mCurRetry++;
        } else {
            mCurRetry = 0;
            doLogout(true);
            PluginMonitorUtil.doMonitor(MonitorConsts.WX_SYNC_CHANGE_FAILED_RETRY_LIMIT);
        }
    }


    private Runnable mRetryGetQrCodeTask = new Runnable() {
        @Override
        public void run() {
            getQrCode();
        }
    };

    private Runnable mRetryCheckAuthTask = new Runnable() {
        @Override
        public void run() {
            checkAuth();
        }
    };

    private Runnable mRetryCheckSyncTask = new Runnable() {
        @Override
        public void run() {
            checkSync();
        }
    };

    private Runnable mRetrySyncChangeTask = new Runnable() {
        @Override
        public void run() {
            syncChange();
        }
    };

}
