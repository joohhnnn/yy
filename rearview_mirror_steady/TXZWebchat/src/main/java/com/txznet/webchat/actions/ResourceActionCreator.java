package com.txznet.webchat.actions;

import android.text.TextUtils;

import com.txznet.loader.AppLogic;
import com.txznet.webchat.Constant;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.helper.FileDownloadHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.plugin.WxPluginManager;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.ui.common.widget.FileDownloadDialog;
import com.txznet.webchat.ui.common.widget.FileDownloadNotification;
import com.txznet.webchat.util.ContactEncryptUtil;
import com.txznet.webchat.util.FileUtil;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;

public class ResourceActionCreator {
    private static final String LOG_TAG = "ResourceAC";
    private static ResourceActionCreator instance;
    private Dispatcher dispatcher;

    private ResourceActionCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static ResourceActionCreator get() {
        if (instance == null) {
            synchronized (ResourceActionCreator.class) {
                if (instance == null) {
                    instance = new ResourceActionCreator(Dispatcher.get());
                }
            }
        }
        return instance;
    }


    /**
     * 下载用户头像
     *
     * @param openId
     */
    public void downloadContactImage(final String openId) {
        if (TextUtils.isEmpty(openId)) {
            return;
        }

        String filePath = getHeadCachePath(ContactEncryptUtil.encrypt(openId));

        // 尝试获取用户头像地址
        WxContact con = WxContactStore.getInstance().getContact(openId);

        WxPluginManager.getInstance().invokePlugin("", PluginInvokeAction.INVOKE_CMD_GET_USER_HEAD, con, filePath);
    }

    /**
     * 下载用户头像，接受加密的用户id， 主要用于sdk等外部环境调用
     *
     * @param openId 加密的用户id
     */
    public void downloadContactImageForEncrypted(final String openId) {
        if (TextUtils.isEmpty(openId)) {
            return;
        }

        String decryptedOpenId = ContactEncryptUtil.decrypt(openId);

        if (TextUtils.isEmpty(decryptedOpenId)) {
            L.e(LOG_TAG, "error downloading contact image: cant decrypt openId");
        }

        String filePath = getHeadCachePath(openId);
        if (new File(filePath).exists()) {
            return;
        }

        downloadContactImage(decryptedOpenId);
    }

    private String getHeadCachePath(String headId) {
        return Constant.PATH_HEAD_CACHE + headId;
    }

    public void downloadVoice(final WxMessage message) {
        String filePath = Constant.PATH_MSG_VOICE_CACHE + message.mMsgId;
        WxPluginManager.getInstance().invokePlugin("", PluginInvokeAction.INVOKE_CMD_GET_VOICE, message, filePath);
    }


    ////////////////// 文件下载相关

    private LinkedList<WxMessage> mFileDownloadQueue = new LinkedList<>();
    private HashSet<Long> mDownLoadSuccessList = new HashSet<>();
    private HashSet<Long> mDownloadFailedList = new HashSet<>();
    private WxMessage mCurrentDownloading = null; // 当前正在下载文件的消息

    /**
     * 下载文件
     *
     * @param message 文件消息
     */
    public void downloadFile(final WxMessage message) {
        if (isFileDownloading(message)) {
            L.i(LOG_TAG, "downloadFile: task already exists: " + message.mFileName);
            return;
        }

        mFileDownloadQueue.add(message);
        dispatcher.dispatch(new Action<>(ActionType.WX_DOWNLOAD_FILE_ADD, message));
        procDownloadQueue();
    }

    /**
     * 返回文件是否已在下载队列中
     *
     * @param message
     * @return
     */
    public boolean isFileDownloading(final WxMessage message) {
        if (mFileDownloadQueue.contains(message) || (mCurrentDownloading != null && message.mMsgId == mCurrentDownloading.mMsgId)) {
            return true;
        }

        return false;
    }

    public boolean isFileDownloaded(final WxMessage message) {
        if (mDownLoadSuccessList.contains(message.mMsgId)) {
            return true;
        }

        return false;
    }

    public boolean isFileDownloadFailed(final WxMessage message) {
        if (mDownloadFailedList.contains(message.mMsgId)) {
            return true;
        }

        return false;
    }

    public void cancelDownloadFile(final WxMessage message) {
        if (null == mCurrentDownloading) {
            return;
        }

        if (mCurrentDownloading.mMsgId == message.mMsgId) {
            FileDownloadHelper.getInstance().cancel();
            dispatcher.dispatch(new Action<>(ActionType.WX_DOWNLOAD_FILE_CANCEL, mCurrentDownloading));
            mCurrentDownloading = null;
            FileDownloadNotification.getInstance().dismiss();
            procDownloadQueue();
        }
    }

    private void procDownloadQueue() {
        if (null != mCurrentDownloading) {
            L.d(LOG_TAG, "downloading file: " + mCurrentDownloading.mFileName + ", skipping proc queue");
            return;
        }

        // 寻找下一条需要处理下载的任务
        while (!mFileDownloadQueue.isEmpty()) {
            final WxMessage msg = mFileDownloadQueue.remove(0);
            if (WxMessage.MSG_TYPE_FILE == msg.mMsgType) {
                // 如果本地路径字段不为空说明之前已经下载过, 直接通知下载成功
                if (isFileDownloaded(msg)) {
                    dispatcher.dispatch(new Action<>(ActionType.WX_DOWNLOAD_FILE_RESP, msg));
                    mCurrentDownloading = null;
                    continue;
                }

                // 开始下载任务
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        startDownload(msg);
                    }
                }, 0);

                break;
            }
        }
    }

    private void startDownload(final WxMessage msg) {
        final String fileDir = getFileLocalPath(msg);
        msg.mFilePath = fileDir;
        L.i(LOG_TAG, "startDownloading file, path = " + fileDir + ", url = " + msg.mFileUrl);
        mCurrentDownloading = msg;
        dispatcher.dispatch(new Action<>(ActionType.WX_DOWNLOAD_FILE_REQ, msg));

        FileDownloadHelper.getInstance().startDownload(msg.mFileUrl, fileDir, new FileDownloadHelper.DownloadCallback() {
            @Override
            public void onStart() {
                L.i(LOG_TAG, "onStart");
                FileDownloadNotification.getInstance().show(msg);
            }

            @Override
            public void onProgress(float progress) {
                L.i(LOG_TAG, "onProgress: " + progress);
                FileDownloadNotification.getInstance().updateProgress(progress);
            }

            @Override
            public void onFinished() {
                L.i(LOG_TAG, "onFinished");

                mCurrentDownloading = null;
                mDownLoadSuccessList.add(msg.mMsgId);
                dispatcher.dispatch(new Action<>(ActionType.WX_DOWNLOAD_FILE_RESP, msg));
                FileDownloadNotification.getInstance().dismiss();
                procDownloadQueue();

                // 将msg送回tts队列进行处理
                TtsActionCreator.get().insertTts(msg);
            }

            @Override
            public void onError(String err) {
                L.i(LOG_TAG, "onError: " + err);

                mCurrentDownloading = null;
                mDownloadFailedList.add(msg.mMsgId);
                dispatcher.dispatch(new Action<>(ActionType.WX_DOWNLOAD_FILE_RESP_ERROR, msg));
                FileDownloadNotification.getInstance().dismiss();
                //FileDownloadDialog.getInstance().updateMessage(msg).show();
                procDownloadQueue();

                // 将msg送回tts队列进行处理
                TtsActionCreator.get().insertTts(msg);
            }

            @Override
            public void onCancel() {
                L.i(LOG_TAG, "onCancel");
            }
        });

        mDownloadFailedList.remove(msg.mMsgId);
    }

    private String getFileLocalPath(WxMessage msg) {
        return WxConfigStore.getInstance().getFileDownloadPath() + FileUtil.getFilePrefix(msg.mFileName)
                + "_" + System.currentTimeMillis() + "." + FileUtil.getFileSuffix(msg.mFileName);
    }

    public void reset() {
        mCurrentDownloading = null;
        mDownloadFailedList.clear();
        mDownLoadSuccessList.clear();
        FileDownloadHelper.getInstance().cancel();
        FileDownloadNotification.getInstance().dismiss();
        FileDownloadDialog.getInstance().dismiss();
    }

}
