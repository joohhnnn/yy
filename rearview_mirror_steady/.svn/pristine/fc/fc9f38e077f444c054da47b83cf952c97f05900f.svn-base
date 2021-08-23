package com.txznet.webchat.util;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.loader.AppLogic;
import com.txznet.webchat.log.L;

import java.io.File;

/**
 * 提供与uid相关的工具方法
 * Created by J on 2017/7/19.
 */

public class UidUtil {
    private static final String LOG_TAG = "UidUtil";
    private static final int REMOTE_INVOKE_RETRY_LIMIT = 3;
    private static final long REMOTE_INVOKE_RETRY_INTERVAL = 10000;

    private UidCallback mUidCallback;
    private int mRemoteInvokeRetryCount = 0;

    private Runnable mRemoteSendInvokeRunnable = new Runnable() {
        @Override
        public void run() {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.getUid", null,
                    new ServiceManager.GetDataCallback() {
                        @Override
                        public void onGetInvokeResponse(final ServiceManager.ServiceData data) {
                            if (null == data) {
                                L.i(LOG_TAG, "get uid from remote failed, invoke data is null");
                                return;
                            }

                            checkRemoteInvokeResult(data.getLong());
                        }
                    });
        }
    };

    /**
     * 获取当前设备uid
     *
     * 优先采用远程调用方式从TXZCore获取, 远程调用可能会失败(Core版本太低未对远程调用进行响应/远程
     * 调用超时/远程调用出错等), 出错的情况下按文件方式获取
     *
     * @param callback
     */
    public void getTXZUID(@NonNull final UidCallback callback) {
        mUidCallback = callback;
        mRemoteInvokeRetryCount = 0;

        getUidFromRemoteInvoke();
    }

    private void getUidFromRemoteInvoke() {
        AppLogic.runOnBackGround(mRemoteSendInvokeRunnable, REMOTE_INVOKE_RETRY_INTERVAL);
    }

    private void checkRemoteInvokeResult(Long uid) {
        L.d(LOG_TAG, "check remote invoke result: " + uid);
        // 获取的uid有效, 直接返回
        if (null != uid && uid > 0) {
            notifyGetUidSuccess(String.valueOf(uid));
            return;
        }

        // 检查是否需要重试
        if (mRemoteInvokeRetryCount++ < REMOTE_INVOKE_RETRY_LIMIT) {
            L.d(LOG_TAG, "retry get uid from remote, times: " + mRemoteInvokeRetryCount);
            getUidFromRemoteInvoke();
            return;
        }

        // 远程调用失败, 从文件获取
        notifyGetUidSuccess(getTXZUidFromFile());
        L.i(LOG_TAG, "get uid from remote failed, get from local file");
    }

    /**
     * 获取当前设备UID
     * 因不能与TXZCode耦合, 采用的是取uid文件名的方式
     * 该方法会检查/sdcard/txz/目录下的所有uid_xxx.txt文件, 取最后修改的文件截取uid
     *
     * @return uid, 获取失败会返回""
     */
    private String getTXZUidFromFile() {
        L.i(LOG_TAG, "getting uid from file");
        String uidDir = Environment.getExternalStorageDirectory().getPath() + "/txz";
        File dir = new File(uidDir);

        if (!dir.exists() || !dir.isDirectory()) {
            return "";
        }

        File[] fileList = dir.listFiles();

        if (null == fileList || fileList.length <= 0) {
            return "";
        }

        String lastModifiedUidFileName = "";
        long newestModifiedTime = 0;
        for (File file : fileList) {
            String name = file.getName();
            if (!name.startsWith("uid_") || !name.endsWith(".txt")) {
                continue;
            }

            if (file.lastModified() <= newestModifiedTime) {
                continue;
            }

            newestModifiedTime = file.lastModified();
            lastModifiedUidFileName = file.getName();
        }

        if (!TextUtils.isEmpty(lastModifiedUidFileName)) {
            // 除掉开头的"uid_"和末尾的".txt"
            lastModifiedUidFileName = lastModifiedUidFileName
                    .substring(4, lastModifiedUidFileName.length() - 4);
        }

        return lastModifiedUidFileName;
    }

    private void notifyGetUidSuccess(String uid) {
        if (null != mUidCallback) {
            L.d(LOG_TAG, "notify get uid success: " + uid);
            mUidCallback.onSuccess(uid);
        }
    }

    public interface UidCallback {
        void onSuccess(String uid);
    }

    //----------- single instance -----------
    private static volatile UidUtil sInstance;

    public static UidUtil getInstance() {
        if (null == sInstance) {
            synchronized (UidUtil.class) {
                if (null == sInstance) {
                    sInstance = new UidUtil();
                }
            }
        }

        return sInstance;
    }

    private UidUtil() {

    }
    //----------- single instance -----------
}
