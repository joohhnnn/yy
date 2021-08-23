package com.txznet.webchat.stores;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.txznet.webchat.Constant;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.actions.ResourceActionCreator;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.ui.common.WxImageLoader;
import com.txznet.webchat.util.ContactEncryptUtil;

import java.io.File;

public class WxResourceStore extends Store {
    private static WxResourceStore sInstance = new WxResourceStore(Dispatcher.get());
    private String mResourceCookie = "";

    /**
     * Constructs and registers an instance of this mWrapper with the given dispatcher.
     *
     * @param dispatcher
     */
    WxResourceStore(Dispatcher dispatcher) {
        super(dispatcher);
    }

    public static WxResourceStore get() {
        return sInstance;
    }

    @Override
    public void onDispatch(Action action) {
        boolean changed = false;

        switch (action.getType()) {
            case ActionType.WX_DOWNLOAD_IMAGE_RESP:
                // bundle中携带的是加密的id
                emitChange(new StoreChangeEvent(EVENT_TYPE_ALL, (Bundle) action.getData()));
                break;

            case ActionType.WX_DOWNLOAD_FILE_ADD:
            case ActionType.WX_DOWNLOAD_FILE_CANCEL:
            case ActionType.WX_DOWNLOAD_FILE_REQ:
            case ActionType.WX_DOWNLOAD_FILE_RESP:
            case ActionType.WX_DOWNLOAD_FILE_RESP_ERROR:
                changed = true;
                break;

            case ActionType.WX_PLUGIN_LOGIC_RESET:
                ResourceActionCreator.get().reset();
                break;

            case ActionType.WX_PLUGIN_UPDATE_RESOURCE_COOKIE:
                mResourceCookie = (String) action.getData();
                WxImageLoader.updateRequestCookie(mResourceCookie);
                break;
        }

        if (changed) {
            emitChange(new StoreChangeEvent(EVENT_TYPE_ALL));
        }
    }

    public String getResourceCookie() {
        return mResourceCookie;
    }

    public String getContactHeadImage(String openId) {
        return getContactHeadImage(openId, true);
    }

    /**
     * 获取指定联系人头像地址
     *
     * @param openId       联系人id
     * @param autoDownload 是否自动下载头像，若为false
     * @return 联系人头像地址，若还未下载成功返回null
     */
    public String getContactHeadImage(String openId, boolean autoDownload) {
        String filePath = getContactHeadImagePath(openId);
        if (checkContactHeadImageExists(openId)) {
            return filePath;
        }

        if (autoDownload) {
            ResourceActionCreator.get().downloadContactImage(openId);
        }

        return null;
    }

    /**
     * 获取联系人头像地址， 不会自动下载
     *
     * @param openId 无论联系人头像是否已经下载成功，都返回正确的地址
     * @return
     */
    public
    @NonNull
    String getContactHeadImagePath(String openId) {
        return Constant.PATH_HEAD_CACHE + ContactEncryptUtil.encrypt(openId);
    }

    /**
     * 检查指定联系人的头像是否已经下载完毕
     *
     * @param openId
     * @return
     */
    public boolean checkContactHeadImageExists(String openId) {
        File file = new File(getContactHeadImagePath(openId));
        return file.exists();
    }

    public static final String EVENT_TYPE_ALL = "wx_res_store";
}
