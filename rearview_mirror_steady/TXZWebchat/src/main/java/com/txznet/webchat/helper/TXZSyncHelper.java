package com.txznet.webchat.helper;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.log.L;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.stores.WxQrCodeStore;
import com.txznet.webchat.util.ContactEncryptUtil;
import com.txznet.webchat.util.SmileyParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

/**
 * 联系人同步Helper
 * Created by J on 2016/11/29.
 */

public class TXZSyncHelper {
    private static final String LOG_TAG = "TXZSyncHelper";
    private static TXZSyncHelper sInstance = new TXZSyncHelper();
    // 活跃联系人列表
    private final LinkedList<String> mListActiveContact = new LinkedList<>();

    public static TXZSyncHelper getInstance() {
        return sInstance;
    }

    private TXZSyncHelper() {
    }

    public void reset() {
        mListActiveContact.clear();
    }

    public void pushActive(String openId) {
        notifySync();
        synchronized (mListActiveContact) {
            if (mListActiveContact.contains(openId)) {
                mListActiveContact.remove(openId);
            }
            mListActiveContact.addFirst(openId);
        }
    }

    private Runnable mSyncTask;

    /**
     * 通知Core进行联系人同步
     */
    public void notifySync() {
        /*if (mSyncTask == null) {
            mSyncTask = new Runnable() {
                @Override
                public void run() {
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.sdk.init", null, null);
                }
            };
            ServiceManager.getInstance().keepConnection(ServiceManager.TXZ, mSyncTask);
        }
        mSyncTask.run();*/
        syncContact();
    }

    public void syncContact() {
        AppLogic.removeBackGroundCallback(mSyncContactTask);
        AppLogic.runOnBackGround(mSyncContactTask, 1000);
    }

    private Runnable mSyncContactTask = new Runnable() {
        @Override
        public void run() {
            reportLoginStatus();
            if (!WxLoginStore.get().isLogin()) {
                return;
            }

            JSONObject data = new JSONObject();
            JSONArray list = new JSONArray();

            try {
                List<String> listContact = WxContactStore.getInstance().getSessionList();
                for (int i = 0; i < listContact.size(); i++) {
                    String openId = listContact.get(i);
                    WxContact con = WxContactStore.getInstance().getContact(openId);

                    if (null == con) {
                        continue;
                    }

                    // 屏蔽群联系人的状态下不同步群会话
                    if (!AppStatusStore.get().isGroupContactEnabled() && WxContact.isGroupOpenId(con.mUserOpenId)) {
                        continue;
                    }

                    String name = SmileyParser.removeEmoji(con.getDisplayName()).replaceAll("\\p{Punct}|\\s", "");
                    JSONObject item = new JSONObject()
                            .put("id", ContactEncryptUtil.encrypt(con.mUserOpenId))
                            .put("name", name)
                            .put("type", con.mUserOpenId.contains("@@") ? 1 : 0);
                    if (con.mUserOpenId.contains("@@") && con.mMemberCount != 0) {
                        item.put("msize", con.mMemberCount);
                    }
                    list.put(item);
                }
                data.put("list", list);
            } catch (Exception e) {
                L.e(LOG_TAG, "webchat sync contact generate json data encountered error: " + e.toString());
            }

            L.d(LOG_TAG, "invoke contact sync, size = " + list.length());
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "wx.contact.update", data.toString().getBytes(), null);
        }
    };

    public void reportLoginStatus() {
        notifyLoginStatusChanged();
    }

    private void notifyLoginStatusChanged() {
        boolean isLogin = WxLoginStore.get().isLogin();

        L.d(LOG_TAG, "notifyLoginStatusChanged isLogin= " + isLogin);
        JSONBuilder doc = new JSONBuilder();
        doc.put("status", isLogin ? 2 : 0);

        if (isLogin) {
            if (WxContactStore.getInstance().getLoginUser() != null) {
                doc.put("nick", WxContactStore.getInstance().getLoginUser().getDisplayName());
                doc.put("loginTime", WxLoginStore.get().getLoginTime());
            }
        } else {
            doc.put("code", WxQrCodeStore.get().getQrCode());
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "wx.loginstate.update", doc.toBytes(), null);
    }

    public void notifyUpdateRecentSession() {
        try {
            JSONArray lists = new JSONArray();
            for (String openId : mListActiveContact) {
                if (lists.length() >= 20) {
                    break;
                }

                WxContact con = WxContactStore.getInstance().getContact(openId);
                if (null != con) {
                    // 根据是否屏蔽群联系人进行判断是否添加
                    if (!(!AppStatusStore.get().isGroupContactEnabled() && WxContact.isGroupOpenId(openId))) {
                        lists.put(new JSONBuilder().put("id", ContactEncryptUtil.encrypt(openId)).put("name", con.getDisplayName()).build());
                    }
                }
            }

            if (lists.length() < 20) {
                List<String> contacts = WxContactStore.getInstance().getSessionList();
                if (contacts != null) {
                    for (int i = 0; i < contacts.size(); i++) {
                        String openId = contacts.get(i);
                        if (lists.length() >= 20) {
                            break;
                        }

                        if (mListActiveContact.contains(openId)) {
                            continue;
                        }

                        if (!(!AppStatusStore.get().isGroupContactEnabled() && WxContact.isGroupOpenId(openId))) {
                            lists.put(new JSONBuilder().put("id", ContactEncryptUtil.encrypt(openId)).put("name", SmileyParser.removeEmoji(WxContactStore.getInstance().getContact(openId).getDisplayName())).build());
                        }
                    }
                }
            }
            JSONBuilder doc = new JSONBuilder();
            doc.put("list", lists);
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "wx.contact.recentsession.update", doc.toBytes(), null);
        } catch (Exception e) {

        }
    }

    // 同步已屏蔽的联系人列表
    public void notifyUpdateBlockedSession() {
        try {
            JSONArray lists = new JSONArray();

            List<String> blockedSessions = WxContactStore.getInstance().getBlockedSession();
            for (int i = 0, j = 0; i < blockedSessions.size() && j < 20; i++) {
                String openId = blockedSessions.get(i);
                WxContact con = WxContactStore.getInstance().getContact(openId);

                if (null != con) {
                    lists.put(new JSONBuilder().put("id", ContactEncryptUtil.encrypt(openId)).put("name", con.getDisplayName()).build());
                    j++;
                }
            }

            JSONBuilder doc = new JSONBuilder();
            doc.put("list", lists);
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "wx.contact.maskedsession.update", doc.toBytes(), null);
        } catch (Exception e) {
            L.e(LOG_TAG, "notify update blocked session encountered error: " + e.toString());
        }
    }

    // 同步用于屏蔽的最近联系人列表
    public void notifyUpdateSessionForBlock() {
        try {
            JSONArray lists = new JSONArray();

            for (int i = 0; i < mListActiveContact.size() && lists.length() < 20; i++) {
                String openId = mListActiveContact.get(i);

                if (!WxContactStore.getInstance().isContactBlocked(openId)) {
                    WxContact con = WxContactStore.getInstance().getContact(openId);

                    if (null != con) {
                        lists.put(new JSONBuilder().put("id", ContactEncryptUtil.encrypt(openId)).put("name", con.getDisplayName()).build());
                    }
                }
            }

            if (lists.length() < 20) {
                List<String> contacts = WxContactStore.getInstance().getSessionList();
                if (contacts != null) {
                    for (int i = 0, j = lists.length(); i < contacts.size() && j < 20; i++) {
                        String openId = contacts.get(i);
                        if (TextUtils.isEmpty(openId) || mListActiveContact.contains(openId) || WxContactStore.getInstance().isContactBlocked(openId)) {
                            continue;
                        }

                        if (!(!AppStatusStore.get().isGroupContactEnabled() && WxContact.isGroupOpenId(openId))) {
                            WxContact con = WxContactStore.getInstance().getContact(openId);
                            lists.put(new JSONBuilder().put("id", ContactEncryptUtil.encrypt(openId)).put("name", SmileyParser.removeEmoji(con.getDisplayName())).build());
                            j++;
                        }
                    }
                }
            }
            JSONBuilder doc = new JSONBuilder();
            doc.put("list", lists);
            // aidl cmd与拉最近会话一致，Core的处理逻辑不需要修改
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "wx.contact.recentsession.update", doc.toBytes(), null);
        } catch (Exception e) {
            L.e(LOG_TAG, "notify update session for block encountered error: " + e.toString());
        }
    }
}


