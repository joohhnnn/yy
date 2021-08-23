package com.txznet.webchat.stores;

import android.os.Bundle;

import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.dispatcher.Dispatcher;

/**
 * 微信联系人焦点Store
 * <p>
 * 以下行为会导致焦点联系人切换:
 * 1. 开始录音
 * 2. 发送消息(包括语音/表情/分享位置)
 * 3. 点击微信新消息弹窗
 * <p>
 * 车载微信会话页面始终应该显示焦点联系人对应的界面, 且车镜版主题的联系人列表界面
 * 中, 若焦点联系人发生变化, 应自动跳转到对应的会话页面
 * <p>
 * Created by J on 2017/11/16.
 */

public class WxContactFocusStore extends Store {
    public static final String EVENT_TYPE_ALL = "wx_contact_focus_store";

    private String mFocusedSession = "";

    private WxContactFocusStore(Dispatcher dispatcher) {
        super(dispatcher);
    }

    private static WxContactFocusStore sInstance = new WxContactFocusStore(Dispatcher.get());

    public static WxContactFocusStore getInstance() {
        return sInstance;
    }

    /**
     * 获取当前的焦点联系人
     *
     * @return 焦点联系人id, 若当前无焦点返回""
     */
    public String getFocusedSession() {
        return mFocusedSession;
    }

    @Override
    public void onDispatch(Action action) {
        boolean changed = false;

        switch (action.getType()) {
            case ActionType.WX_SWITCH_SESSION:
                changed = doSwitchFocusSession((Bundle) action.getData());
                break;

            case ActionType.WX_PLUGIN_LOGIC_RESET:
                changed = doReset();
                break;
        }

        if (changed) {
            emitChange(EVENT_TYPE_ALL);
        }
    }

    private boolean doSwitchFocusSession(Bundle data) {
        String focusId = data.getString("uid");

        if (null != WxContactStore.getInstance().getContact(focusId)) {
            mFocusedSession = focusId;
            return true;
        }

        return false;
    }

    private boolean doReset() {
        mFocusedSession = "";
        return false;
    }
}
