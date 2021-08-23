package com.txznet.webchat.plugin.preset.logic.module;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.utils.PluginLogUtil;
import com.txznet.webchat.comm.plugin.utils.PluginTaskRunner;
import com.txznet.webchat.plugin.preset.logic.action.ActionType;
import com.txznet.webchat.plugin.preset.logic.action.PluginInvokeAction;
import com.txznet.webchat.plugin.preset.logic.api.WeChatClient;
import com.txznet.webchat.plugin.preset.logic.api.resp.ContactEntity;
import com.txznet.webchat.plugin.preset.logic.api.resp.WebWxBatchGetContactResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.WebWxGetContactResp;
import com.txznet.webchat.plugin.preset.logic.api.resp.WebWxInitResp;
import com.txznet.webchat.plugin.preset.logic.base.WxModule;
import com.txznet.webchat.plugin.preset.logic.util.Runnable1;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 微信联系人，负责联系人解析、同步
 * Created by J on 2016/8/17.
 */
public class WxContactModule extends WxModule {
    private static final String TOKEN_CONTACT_PLUGIN = "wx_contact_module";

    // 好友缓存，只缓存GetContact返回的好友列表中的群、普通联系人（排除公众号等）
    private HashMap<String, Boolean> mContactMap = new HashMap<>();
    // 缓存当前登录用户
    private WxContact mLoginUser;
    // 缓存待发送的联系人列表
    private ArrayList<String> mSessionCache = new ArrayList<>();
    // 好友同步标志位，好友同步完成前不发送群同步事件
    private boolean bContactSync = false;

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public String getToken() {
        return TOKEN_CONTACT_PLUGIN;
    }

    @Override
    public void reset() {
        mContactMap = new HashMap<>();
        mLoginUser = null;
        mSessionCache = new ArrayList<>();
        bContactSync = false;
        WxContact.sLoginUser = null;
        PluginTaskRunner.removeBackGroundCallback(mRetryGetGroupMemberTask);
        PluginTaskRunner.removeBackGroundCallback(mRetryGetContact);
    }

    private static WxContactModule sInstance = new WxContactModule();

    public static WxContactModule getInstance() {
        return sInstance;
    }

    private WxContactModule() {

    }

    public void startResolveContact(WebWxInitResp resp) {
        // 同步登陆用户信息
        mLoginUser = convertWxContact(resp.User, false);
        dispatchEvent(ActionType.WX_PLUGIN_SYNC_USER, mLoginUser);
        WxContact.sLoginUser = mLoginUser.mUserOpenId;
        ArrayList<String> needResyncGroupList = new ArrayList<>();

        // 同步ContactList
        List<WxContact> list = new ArrayList<>(resp.ContactList.size());
        for (ContactEntity entity : resp.ContactList) {
            WxContact contact = convertWxContact(entity, false);

            if (null == contact) {
                continue;
            }

            /*
            * 测试发现initResp中携带的群联系人可能没有EncryChatroomId字段, 而这个
            * 字段对于非好友的群成员头像的加载很重要, 所以对于没有此字段的群联系人再
            * 重新同步下
            * */
            if (WxContact.Type.GROUP == contact.mType
                    && TextUtils.isEmpty(entity.EncryChatRoomId)) {
                PluginLogUtil.i(getToken(), String.format("need resync group: %s, nick = %s",
                        contact.mUserOpenId, contact.mNickName));
                needResyncGroupList.add(contact.mUserOpenId);
            }

            if (WxContact.Type.GROUP == contact.mType || WxContact.Type.PEOPLE == contact.mType) {
                list.add(convertWxContact(entity, false));
                mContactMap.put(contact.mUserOpenId, true);
            }
        }
        dispatchEvent(ActionType.WX_PLUGIN_SYNC_SESSION, list);

        // 同步群联系人
        resolveSession(resp.ChatSet);
        // 将需要重新同步的群联系人进行同步
        batchSyncContact(needResyncGroupList.toArray(new String[0]));
        // 同步好友
        getContacts(0);
    }

    public WxContact getLoginUser() {
        return mLoginUser;
    }

    /**
     * 根据初始化或批量同步的联系人id列表进行联系人同步
     *
     * @param ids 需要批量同步的联系人列表, 以逗号分隔
     */
    public void resolveSession(String ids) {
        String[] split = ids.split(",");
        List<String> batchList = new ArrayList<>(10);
        List<String> sessionList = new ArrayList<>();
        for (int i = 0, len = split.length; i < len; i++) {
            String id = split[i].trim();
            if (id.startsWith("@")
                    || id.startsWith("wxid")
                    || id.endsWith("@chatroom")
                    || id.endsWith("@talkroom")) {
                sessionList.add(id);

                // 2018/09/10 更新
                // 批量更新不应限制群联系人类型, 也可能出现单独联系人个体资料不全需要重新同步的情况
                /*if (id.startsWith("@@")) {
                    groupList.add(id);
                }*/
                if (!id.equals(mLoginUser.mUserOpenId)) {
                    batchList.add(id);
                }
            }

            // 最多50个一组进行群联系人同步
            if (len - 1 <= i || batchList.size() >= 50) {
                batchSyncContact(batchList.toArray(new String[0]));
                batchList.clear();
            }
        }

        dispatchSyncSessionList(sessionList);
    }


    /**
     * 批量更新联系人
     *
     * @param groupIds 需要更新的联系人列表
     */
    private void batchSyncContact(final String[] groupIds) {
        if (0 == groupIds.length) {
            PluginLogUtil.d("batchSyncContact: list is empty");
            return;
        }

        WeChatClient.getInstance().Api
                .webwxbatchgetcontact(new WeChatClient.WeChatResp<WebWxBatchGetContactResp>() {
                    @Override
                    public void onResp(WebWxBatchGetContactResp resp) {
                        if (0 == resp.BaseResponse.Ret) {
                            dispatchSyncGroupContact(resp);
                        } else {
                            PluginLogUtil.d(String.format("batchSyncContact: resp err(%s)",
                                    resp.BaseResponse.Ret));
                            retryGetGroupMember(groupIds);
                        }

                    }

                    @Override
                    public void onError(int statusCode, String message) {
                        retryGetGroupMember(groupIds);
                    }
                }, groupIds);
    }

    private void dispatchSyncSessionList(List<String> list) {
        List<String> syncList = new ArrayList<>();
        for (String id : list) {
            if (null != mContactMap.get(id) || id.startsWith("@@")) {
                syncList.add(id);
            }
        }

        // 好友同步完成后再发送联系人列表更新事件
        if (bContactSync) {
            dispatchEvent(ActionType.WX_PLUGIN_SYNC_SESSION_LIST, syncList);
        } else {
            mSessionCache.addAll(syncList);
        }
    }

    private void dispatchSyncGroupContact(WebWxBatchGetContactResp resp) {
        List<WxContact> groups = new ArrayList<>(resp.ContactList.size());

        for (ContactEntity group : resp.ContactList) {
            WxContact groupCon = convertWxContact(group, true);

            if (null == groupCon) {
                // 无效的群可能已经被同步到了session list， 要进行删除
                dispatchEvent(ActionType.WX_PLUGIN_SYNC_DEL_CONTACT, group.UserName);
                continue;
            }

            groups.add(groupCon);
        }

        dispatchEvent(ActionType.WX_PLUGIN_SYNC_GROUP_CONTACT, groups);
    }

    /**
     * 获取通讯录联系人
     */
    private void getContacts(final int seq) {
        WeChatClient.getInstance().Api
                .webwxgetcontact(new WeChatClient.WeChatResp<WebWxGetContactResp>() {
                    @Override
                    public void onResp(WebWxGetContactResp resp) {
                        PluginLogUtil.d("getContact:onResp, ret=" + resp.BaseResponse.Ret);

                        if (0 == resp.BaseResponse.Ret) {
                            // 请求成功时重置重试次数
                            mRetryGetContactCount = 0;
                            dispatchSyncContact(resp.MemberList);

                            // seq != 0时表示还有待同步的联系人, 需要根据seq再次进行同步
                            if (resp.Seq != 0) {
                                getContacts(resp.Seq);
                            }
                        } else {
                            retryGetContact(seq);
                        }
                    }

                    @Override
                    public void onError(int statusCode, String message) {
                        retryGetContact(seq);
                    }
                }, seq);
    }

    public void modContact(JSONArray modContactList) {
        for (int i = 0, len = modContactList.length(); i < len; i++) {
            try {
                ContactEntity entity = JSON.parseObject(modContactList.getString(i),
                        ContactEntity.class);
                WxContact con = convertWxContact(entity, false);

                // 若联系人无效，直接删除
                if (null == con) {
                    dispatchEvent(ActionType.WX_PLUGIN_SYNC_DEL_CONTACT, entity.UserName);
                    return;
                }
                dispatchEvent(ActionType.WX_PLUGIN_SYNC_MOD_CONTACT, con);
            } catch (Exception e) {
                PluginLogUtil.e(getToken(), "modContact convert contact entity encountered error: "
                        + e.toString());
            }

        }

    }

    public void delContact(JSONArray delContactList) {
        for (int i = 0, len = delContactList.length(); i < len; i++) {
            try {
                ContactEntity entity = JSON.parseObject(delContactList.getString(i),
                        ContactEntity.class);
                //WxContact con = convertWxContact(entity);

                dispatchEvent(ActionType.WX_PLUGIN_SYNC_DEL_CONTACT, entity.UserName);
            } catch (Exception e) {
                PluginLogUtil.e(getToken(), "delContact convert contact entity encountered error: "
                        + e.toString());
            }

        }
    }

    private void dispatchSyncContact(List<ContactEntity> entityList) {
        List<WxContact> list = new ArrayList<>(entityList.size());
        for (ContactEntity entity : entityList) {
            WxContact contact = convertWxContact(entity, false);
            if (null == contact) {
                continue;
            }

            if (WxContact.Type.GROUP == contact.mType || WxContact.Type.PEOPLE == contact.mType) {
                // 缓存群和联系人
                mContactMap.put(contact.mUserOpenId, true);
                list.add(contact);
            }
        }
        dispatchEvent(ActionType.WX_PLUGIN_SYNC_CONTACT, list);
        bContactSync = true;

        // 同步之前缓存的联系人列表
        dispatchEvent(ActionType.WX_PLUGIN_SYNC_SESSION_LIST, mSessionCache);
    }

    private int mRetryGetContactCount = 0;
    private Runnable1 mRetryGetContact = new Runnable1<Integer>(0) {
        @Override
        public void run() {
            getContacts(mP1);
        }
    };

    private void retryGetContact(int seq) {
        if (mRetryGetContactCount >= 3) {
            PluginManager.invoke("wx.cmd.invoke_plugin", "", PluginInvokeAction.INVOKE_CMD_LOGOUT,
                    true);
            return;
        }

        mRetryGetContactCount++;

        PluginTaskRunner.removeBackGroundCallback(mRetryGetContact);
        mRetryGetContact.update(seq);
        PluginTaskRunner.runOnBackGround(mRetryGetContact, 1000);
    }

    private void retryGetGroupMember(String[] groupIds) {
        mRetryGetGroupMemberTask.addIds(groupIds);
        PluginTaskRunner.removeBackGroundCallback(mRetryGetGroupMemberTask);
        PluginTaskRunner.runOnBackGround(mRetryGetGroupMemberTask, 3000);
    }


    private final GetGroupMemberRunnable mRetryGetGroupMemberTask = new GetGroupMemberRunnable();

    private class GetGroupMemberRunnable implements Runnable {
        Set<String> mIds = new HashSet<String>();

        public void reset() {
            synchronized (mRetryGetGroupMemberTask) {
                mIds.clear();
            }
        }

        public void addIds(String[] ids) {
            synchronized (mRetryGetGroupMemberTask) {
                Collections.addAll(mIds, ids);
            }
        }

        @Override
        public void run() {
            String[] ids;
            synchronized (mRetryGetGroupMemberTask) {
                ids = new String[mIds.size()];
                mIds.toArray(ids);
            }
            batchSyncContact(ids);
        }
    }

    private WxContact convertWxContact(ContactEntity entity, boolean isFromBatch) {
        WxContact contact = null;
        try {
            contact = new WxContact();
            contact.mUserOpenId = entity.UserName;
            contact.mNickName = entity.NickName;
            contact.mRemarkName = entity.RemarkName;
            contact.mNotifyMsg = entity.Statues != 0;
            contact.mMemberCount = entity.MemberCount;

            switch (entity.Sex) {
                case 1:
                    contact.mSex = WxContact.Sex.MALE;
                    break;
                case 2:
                    contact.mSex = WxContact.Sex.FEMALE;
                    break;
                default:
                    contact.mSex = WxContact.Sex.UNKNOW;
                    break;
            }
            if (WxContact.isGroupOpenId(contact.mUserOpenId)) {
                contact.mType = WxContact.Type.GROUP;
                contact.mEncryChatroomId = entity.EncryChatRoomId;

                if (entity.MemberCount != 0 && entity.MemberList != null) {
                    boolean bIllegalGroupContact = false;
                    for (ContactEntity groupCon : entity.MemberList) {
                        WxContact con = new WxContact();
                        con.mType = WxContact.Type.GROUP_MEMBER;
                        con.mMemberCount = 0;
                        con.mRemarkName = groupCon.RemarkName;
                        con.mNickName = groupCon.NickName;
                        con.mUserOpenId = groupCon.UserName;
                        // 群用户也记录下来EncryChatroomId，以群的对应字段填充，用于加载群聊头像
                        con.mEncryChatroomId = entity.EncryChatRoomId;
                        con.mHeadImgUrl = resolveHeadUrl(con, "");


                        contact.mGroupMembers.add(con);

                        if (mLoginUser.mUserOpenId.equals(groupCon.UserName)) {
                            // 当前登录用户在群成员内，设置有效群
                            bIllegalGroupContact = true;
                        }
                    }

                    // 若群不是有效群，忽略
                    if (!bIllegalGroupContact) {
                        return null;
                    }

                    // 若群昵称为空，取前3个成员名拼为昵称
                    if (TextUtils.isEmpty(contact.mNickName)
                            && TextUtils.isEmpty(contact.mRemarkName)) {
                        String strGroupNick = "";
                        int count = 0;
                        for (int i = 0, len = contact.mGroupMembers.size(); i < len; i++) {
                            WxContact con = contact.mGroupMembers.get(i);
                            String strCurNick = "";

                            if (!TextUtils.isEmpty(con.mNickName)) {
                                strCurNick = con.mNickName;
                            } else if (!TextUtils.isEmpty(con.mRemarkName)) {
                                strCurNick = con.mRemarkName;
                            }

                            if (!TextUtils.isEmpty(strCurNick)) {
                                strGroupNick += strCurNick;
                                count++;
                            }

                            if (count >= 3 || len - 1 == i) {
                                strGroupNick += "...";
                                break;
                            } else {
                                strGroupNick += ",";
                            }
                        }

                        contact.mNickName = strGroupNick;
                    }
                } else {
                    // 忽略联系人为空的群
                    // 某些情况下通过getContacts返回的群组成员字段为空, 此时将该联系人送入resolveSession
                    // 重新获取下信息, 为防止resolveSession获取回来的联系人信息还是不合法导致死循环, 通过
                    // isFromBacth进行规避
                    if (!isFromBatch) {
                        resolveSession(entity.UserName);
                    }

                    return null;
                }
            } else {
                if (contact.mUserOpenId.startsWith("@")
                        || contact.mUserOpenId.startsWith("wxid_")) {
                    if (entity.VerifyFlag == 0) {
                        contact.mType = WxContact.Type.PEOPLE;
                        if (((entity.ContactFlag & (0x1 << 9)) >> 9) == 1) {
                            contact.mNotifyMsg = false;
                        } else {
                            contact.mNotifyMsg = true;
                        }
                    } else {
                        contact.mType = WxContact.Type.SERVICE;
                    }
                } else {
                    contact.mType = WxContact.Type.SYSTEM;
                }
            }

            contact.mHeadImgUrl = resolveHeadUrl(contact, entity.HeadImgUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contact;
    }

    private String resolveHeadUrl(WxContact contact, String rawHeadUrl) {
        // 设置用户头像url
        if (!TextUtils.isEmpty(rawHeadUrl)) {
            return WeChatClient.getInstance().mUrlSupport.getUrl_headByUrl(rawHeadUrl);
        } else if (WxContact.Type.GROUP == contact.mType) {
            return WeChatClient.getInstance().mUrlSupport.getUrl_webwxgetheadimg(rawHeadUrl);
        } else {
            return WeChatClient.getInstance().mUrlSupport.getUrl_webwxgeticon(contact.mUserOpenId,
                    contact.mEncryChatroomId);
        }
    }
}