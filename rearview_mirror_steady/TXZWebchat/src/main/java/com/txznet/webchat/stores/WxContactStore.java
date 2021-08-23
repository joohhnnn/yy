package com.txznet.webchat.stores;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.helper.TXZSyncHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.sdk.WxSDKManager;
import com.txznet.webchat.util.SmileyParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 联系人Store
 * Created by J on 2016/8/19.
 */
public class WxContactStore extends Store {
    public static final String EVENT_TYPE_ALL = "wechat_contact_store";
    private static final String LOG_TAG = "WxContactStore";
    private static final WxContactStore sInstance = new WxContactStore(Dispatcher.get());

    // 当前登录用户
    private WxContact mLoginUser;
    // 所有联系人集合，包含非好友的群联系人等（主要用于群消息显示和播报）
    private ConcurrentHashMap<String, WxContact> mContactMap = new ConcurrentHashMap<>();
    // 会话列表（=网页版的会话列表）
    private ArrayList<String> mSessionList = new ArrayList<>();
    // 好友列表（=网页版的联系人列表）
    private ArrayList<String> mContactList = new ArrayList<>();
    // 完整会话列表（实际用于显示的会话列表， = 会话列表 + 好友列表 + 去重）
    private LinkedList<String> mTotalSessionList = new LinkedList<>();
    // 联系人缓存集合，用于判断用户是否已添加到会话和好友列表，防止重复添加
    private HashMap<String, Boolean> mSessionListCache = new HashMap<>();
    // 屏蔽联系人列表
    private ArrayList<String> mBlockedSessions = new ArrayList<String>();

    private WxContactStore(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void onDispatch(Action action) {
        boolean changed = false;

        switch (action.getType()) {
            case ActionType.WX_PLUGIN_LOGIC_RESET:
                reset();
                break;
            // 扫码成功时也重置下
            // 这个是为了避免某些极端情况下，mUpdateTotalSessionListTask运行过程中退出了微信，导致联系人没有
            // 被正确重置
            case ActionType.WX_QRCODE_SCAN:
                reset();
                break;

            case ActionType.WX_PLUGIN_SYNC_USER:
                changed = doSyncUser((WxContact) action.getData());
                break;

            case ActionType.WX_PLUGIN_SYNC_SESSION_LIST:
                changed = doSyncSessionList((List<String>) action.getData());
                break;

            case ActionType.WX_PLUGIN_SYNC_SESSION:
                changed = doSyncSession((List<WxContact>) action.getData());
                break;

            case ActionType.WX_PLUGIN_SYNC_CONTACT:
                changed = doSyncContact((List<WxContact>) action.getData());
                break;

            case ActionType.WX_PLUGIN_SYNC_GROUP_CONTACT:
                changed = doSyncBatchContact((List<WxContact>) action.getData());
                break;

            case ActionType.WX_PLUGIN_SYNC_MOD_CONTACT:
                changed = doModContact((WxContact) action.getData());
                break;

            case ActionType.WX_PLUGIN_SYNC_DEL_CONTACT:
                changed = doDelContact((String) action.getData());
                break;

            case ActionType.WX_PLUGIN_SYNC_TOP_SESSION:
                changed = doTopSession((String) action.getData());
                break;

            case ActionType.WX_BLOCK_CONTACT:
                changed = doBlockSession((Bundle) action.getData());
                break;

            case ActionType.WX_UNBLOCK_CONTACT:
                changed = doUnblockSession((Bundle) action.getData());
                break;
        }

        if (changed) {
            emitChange(EVENT_TYPE_ALL);
        }

    }

    public static WxContactStore getInstance() {
        return sInstance;
    }

    private void reset() {
        bNeedReSync.getAndSet(false);
        bUpdatingTotalSessionList.getAndSet(false);
        AppLogic.removeBackGroundCallback(mUpdateTotalSessionListTask);
        mLoginUser = null;
        mContactMap.clear();
        mSessionList.clear();
        mContactList.clear();
        mTotalSessionList.clear();
        mSessionListCache.clear();
        mBlockedSessions.clear();
    }

    /**
     * 获取会话列表
     *
     * @return
     */
    public List<String> getSessionList() {
        return mTotalSessionList;
    }

    /**
     * 根据openId获取指定联系人
     *
     * @param openId
     * @return
     */
    public WxContact getContact(String openId) {
        return mContactMap.get(openId);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return
     */
    public WxContact getLoginUser() {
        return mLoginUser;
    }

    /**
     * 返回指定openId是否被屏蔽
     *
     * @param openId
     * @return
     */
    public boolean isContactBlocked(String openId) {
        if (mBlockedSessions.contains(openId)) {
            return true;
        }

        if (!AppStatusStore.get().isGroupMsgBroadEnabled() && WxContact.isGroupOpenId(openId)) {
            return true;
        }

        WxContact contact = mContactMap.get(openId);
        if (null != contact && !contact.mNotifyMsg) {
            return true;
        }

        return false;
    }

    /**
     * 获取屏蔽的联系人列表
     *
     * @return
     */
    public List<String> getBlockedSession() {
        return mBlockedSessions;
    }

    private boolean doSyncUser(WxContact contact) {
        mLoginUser = contact;
        WxSDKManager.getInstance().notifyUserInfo();
        mContactMap.put(contact.mUserOpenId, contact);
        return true;
    }

    private boolean doSyncSession(List<WxContact> list) {
        for (WxContact contact : list) {
            if (null != contact && (contact.mType.equals(WxContact.Type.GROUP) || contact.mType.equals(WxContact.Type.PEOPLE))) {
                mContactMap.put(contact.mUserOpenId, contact);
                addToSessionList(contact.mUserOpenId);
            }
        }

        return false;
    }

    private boolean doSyncSessionList(List<String> list) {
        for (String id : list) {
            addToSessionList(id);
        }
        return false;
    }


    private boolean doSyncContact(List<WxContact> list) {
        for (WxContact contact : list) {
            if (checkContactLegal(contact)) {
                mContactMap.put(contact.mUserOpenId, contact);
                addToContactList(contact.mUserOpenId);
            }
        }

        return false;
    }

    /**
     * 同步批量更新的联系人
     *
     * 微信批量更新联系人分两种情况:
     * 1. 同步联系人时, 部分群联系人可能信息不完整(成员昵称为空等)
     * 2. 登录后MsgType=51的消息, 带有批量的联系人id需要主动更新
     *
     * 更新联系人不限制一定是群类型, 也可能是单独的个人联系人
     *
     * @param list
     * @return
     */
    private boolean doSyncBatchContact(List<WxContact> list) {
        boolean listChanged = false;
        for (WxContact groupCon : list) {
            // 跳过错误的群
            if (!checkContactLegal(groupCon)) {
                continue;
            }

            // 修改后此方法同步的不一定是群联系人, 也可能是单独联系人, 所以不再过滤非群联系人
            // 跳过成员列表为空的群
            /*if (0 == groupCon.mMemberCount || null == groupCon.mGroupMembers) {
                L.e(LOG_TAG, "doSyncBatchContact: group member is empty");
                continue;
            }*/

            // 跳过可能存在的服务号等其他类型账号
            if (groupCon.mType != WxContact.Type.PEOPLE
                    && groupCon.mType != WxContact.Type.GROUP) {
                continue;
            }

            // 将群添加到联系人列表
            mContactMap.put(groupCon.mUserOpenId, groupCon);
            if (addToSessionList(groupCon.mUserOpenId)) {
                listChanged = true;
            }

            // 将群成员添加到联系人列表
            if (0 != groupCon.mMemberCount && null != groupCon.mGroupMembers) {
                for (WxContact contact : groupCon.mGroupMembers) {
                    mergeGroupContact(contact);
                }
            }

        }

        if (listChanged) {
            updateTotalSessionList();
        }

        return true;
    }

    /**
     * 合并群联系人
     *
     * 来自群的成员可能同时也是普通好友, 此时不应该直接在mContactMap中进行覆盖, 否则可能导致联系人某些
     * 字段错误(如屏蔽状态, 群成员的屏蔽状态可能是不正确的值), 所以需要进行merge处理
     *
     * @param groupContact 群成员
     */
    private void mergeGroupContact(WxContact groupContact) {
        if (checkContactLegal(groupContact)) {
            WxContact contact = mContactMap.get(groupContact.mUserOpenId);

            if (null == contact || WxContact.Type.GROUP_MEMBER == contact.mType) {
                // 如果ContactMap中缓存的本来就是群联系人, 直接替换
                mContactMap.put(groupContact.mUserOpenId, groupContact);
            } else if (WxContact.Type.PEOPLE == contact.mType) {
                // 已经缓存了普通联系人, 群成员信息可以忽略
            }
        }
    }

    private boolean checkContactLegal(WxContact contact) {
        if (null == contact || TextUtils.isEmpty(contact.mUserOpenId)) {
            return false;
        }

        return true;
    }

    private boolean doModContact(WxContact contact) {
        if (!checkContactLegal(contact)) {
            L.e(LOG_TAG, "doModContact: contact is illegal");
            return false;
        }

        mContactMap.put(contact.mUserOpenId, contact);

        // 通知sdk联系人变动
        WxSDKManager.getInstance().notifyModContact(contact);

        if (contact.mMemberCount > 0) {
            for (WxContact con : contact.mGroupMembers) {
                mergeGroupContact(con);
            }
        }

        boolean needUpdateSessionList = false;

        if (!mSessionList.contains(contact.mUserOpenId)) {
            mSessionList.add(0, contact.mUserOpenId);
            needUpdateSessionList = true;
        }

        if (!mContactList.contains(contact.mUserOpenId)) {
            mContactList.add(contact.mUserOpenId);
            needUpdateSessionList = true;
        }

        if (needUpdateSessionList) {
            updateTotalSessionList();
        }

        TXZSyncHelper.getInstance().notifySync();
        return true;
    }

    private boolean doDelContact(String openId) {
        boolean changed = false;
        int index = mSessionList.indexOf(openId);
        if (index >= 0) {
            mSessionList.remove(index);
            changed = true;

            try {
                mSessionListCache.remove(openId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        index = mContactList.indexOf(openId);
        if (index >= 0) {
            mContactList.remove(index);
            changed = true;
        }

        if (changed) {
            TXZSyncHelper.getInstance().notifySync();
            updateTotalSessionList();
            // 通知sdk联系人被删除
            WxSDKManager.getInstance().notifyDelContact(openId);
        }

        L.d(LOG_TAG, "del contact: " + openId + ", changed = " + changed);
        return changed;
    }

    private boolean doTopSession(String id) {
        if (TextUtils.isEmpty(id) || !(id.startsWith("@") || id.startsWith("wxid") || id.endsWith("@chatroom") || id.endsWith("@talkroom"))) {
            L.e(LOG_TAG, "doTopSession: sessionId is illegal: " + id);
            return false;
        }

        if (null == mSessionListCache.get(id)) {
            mSessionListCache.put(id, true);
        }

        if (mSessionList.remove(id)) {
            mSessionList.add(0, id);
        }

        if (mTotalSessionList.remove(id)) {
            mTotalSessionList.addFirst(id);
        }

        return true;
    }

    private boolean doBlockSession(Bundle data) {
        if (null == data) {
            L.e(LOG_TAG, "doBlockSession data is null");
        } else {
            String uid = data.getString("uid");
            boolean manual = data.getBoolean("manual");

            WxContact targetCon = mContactMap.get(uid);
            if (null != targetCon) {
                if (!mBlockedSessions.contains(uid)) {
                    mBlockedSessions.add(uid);
                    // 通知sdk联系人信息发生变化
                    WxSDKManager.getInstance().notifyModContact(targetCon);
                }

                if (!manual) {
                    String nick = SmileyParser.removeEmoji(targetCon.getDisplayName());
                    String defaultTxt = String.format("%s的微信消息已屏蔽成功", nick);
                    TtsUtil.speakResource("RS_VOICE_WEBCHAT_FILTER_SPEAK", new String[]{"%TAR%", nick}, defaultTxt);
                }
                return true;
            }
        }

        return false;
    }

    private boolean doUnblockSession(Bundle data) {
        if (null != data) {
            String uid = data.getString("uid");
            boolean manual = data.getBoolean("manual");

            if (mBlockedSessions.contains(uid)) {
                mBlockedSessions.remove(uid);

                // 通知sdk联系人信息发生变化
                WxContact con = mContactMap.get(uid);
                if (null != con) {
                    WxSDKManager.getInstance().notifyModContact(con);
                }


                if (!manual) {
                    String nick = SmileyParser.removeEmoji(mContactMap.get(uid).getDisplayName());
                    String defaultTxt = String.format("%s的消息屏蔽已解除", nick);
                    TtsUtil.speakResource("RS_VOICE_WEBCHAT_UNFILTER_SPEAK", new String[]{"%TAR%", nick}, defaultTxt);
                }

                return true;
            }
        }

        return false;
    }

    /**
     * 添加联系人到会话列表
     *
     * @param openId
     */
    private boolean addToSessionList(String openId) {
        if (TextUtils.isEmpty(openId)) {
            return false;
        }

        // mSessionListCache命中说明之前已经添加过了联系人
        if (null != mSessionListCache.get(openId)) {
            return false;
        }

        // 跳过还未同步到具体信息的联系人
        if (null == mContactMap.get(openId)) {
            return false;
        }

        mSessionList.add(openId);
        mSessionListCache.put(openId, true);
        updateTotalSessionList();

        return true;
    }

    private void addToContactList(String openId) {
        if (TextUtils.isEmpty(openId)) {
            return;
        }

        mContactList.add(openId);
        updateTotalSessionList();
    }

    private void updateTotalSessionList() {
        if (bUpdatingTotalSessionList.compareAndSet(false, true)) {
            AppLogic.runOnBackGround(mUpdateTotalSessionListTask, 0);
            return;
        }

        bNeedReSync.getAndSet(true);
    }

    private AtomicBoolean bNeedReSync = new AtomicBoolean(false);
    private AtomicBoolean bUpdatingTotalSessionList = new AtomicBoolean(false);
    private Runnable mUpdateTotalSessionListTask = new Runnable() {
        @Override
        public void run() {
            long startTime = SystemClock.elapsedRealtime();
            LinkedList<String> totalContactList = new LinkedList<>();
            HashSet<String> syncCache = new HashSet<>();

            for (int i = 0; i < mSessionList.size(); i++) {
                String openId = mSessionList.get(i);
                if (syncCache.add(openId)) {
                    totalContactList.add(openId);
                }
            }

            for (int i = 0; i < mContactList.size(); i++) {
                String openId = mContactList.get(i);
                if (syncCache.add(openId)) {
                    totalContactList.add(openId);
                }
            }

            mTotalSessionList = totalContactList;

            TXZSyncHelper.getInstance().syncContact();
            emitChange(EVENT_TYPE_ALL);

            if (bNeedReSync.compareAndSet(true, false)) {
                AppLogic.runOnBackGround(this, 500);
            } else {
                bUpdatingTotalSessionList.getAndSet(false);
            }
            L.i(LOG_TAG, String.format("process %s contacts cost %s ms",
                    totalContactList.size(), SystemClock.elapsedRealtime() - startTime));
        }
    };

}
