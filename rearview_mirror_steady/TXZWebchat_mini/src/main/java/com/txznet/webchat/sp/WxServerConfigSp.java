package com.txznet.webchat.sp;

import android.content.Context;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.sp.CommonSp;

/**
 * 微信插件信息sp
 * 插件信息会维护一份当前加载的插件列表, 同时每个插件单独用一个key记录插件版本
 * Created by J on 2017/7/14.
 */

public class WxServerConfigSp extends CommonSp {
    private static final String SP_NAME = "wx_server_config_sp";

    private static final String KEY_PUSH_LOGIN_ENABLED = "key_push_login_enabled";

    private WxServerConfigSp(Context context, String spName) {
        super(context, spName);
    }

    // single instance
    private static WxServerConfigSp sInstnace = new WxServerConfigSp(GlobalContext.get(), SP_NAME);

    public static WxServerConfigSp getInstance() {
        return sInstnace;
    }
    // eof single instance

    /**
     * 获取免扫码登录打开状态
     *
     * @return
     */
    public int getPushLoginEnabled() {
        return getValue(KEY_PUSH_LOGIN_ENABLED, 0);
    }

    /**
     * 保存免扫码登录打开状态
     *
     * @param enable
     */
    public void setPushLoginEnabled(int enable) {
        setValue(KEY_PUSH_LOGIN_ENABLED, enable);
    }
}
