package com.txznet.sdk;


import android.os.Parcel;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.util.MD5Util;

import java.util.HashMap;

/**
 * SDK使用的下载工具
 */
public class TXZDownloadManager {
    public static final String DOWNLOAD_CMD_PREFIX = "txz.download.cmd.";//core -> sdk
    public static final String DOWNLOAD_INVOKE_PREFIX = "txz.download.invoke.";//sdk -> core

    public static final String DOWNLOAD_START = "start";
    public static final String DOWNLOAD_STOP = "stop";

    public static final String DOWNLOAD_NOTIFY = "notify";
    public static final String DOWNLOAD_RESULT = "result";

    public static final String DOWNLOAD_TASK_ID_PREFIX = ".sdk_";


    /**
     * 下载成功
     */
    public static final int EC_DOWNLOAD_SUCCESS = 0;
    /**
     * 下载服务器状态码异常
     */
    public static final int EC_DOWNLOAD_SERVER = 1;
    /**
     * IO错误
     */
    public static final int EC_DOWNLOAD_IO = 2;      //
    /**
     * 网络错误,网络不正常
     */
    public static final int EC_DOWNLOAD_NETWORK = 3;      //
    /**
     * 未知错误
     */
    public static final int EC_DOWNLOAD_UNKNOWN = 4;      //

    private static TXZDownloadManager sInstance = new TXZDownloadManager();

    private TXZDownloadManager() {

    }

    /**
     * 获取单例
     *
     * @return
     */
    public static TXZDownloadManager getInstance() {
        return sInstance;
    }

    /**
     * 考虑core crash的情况，重连时需要重新将下载任务同步给core
     */
    void onReconnectTXZ() {
        TXZService.setCommandProcessor(DOWNLOAD_CMD_PREFIX, mCommandProcessor);

        synchronized (mRemoteTask) {
            for (DownloadTask downloadTask : mRemoteTask.values()) {
                startDownloadInner(downloadTask.taskId, downloadTask.downloadCallback.getDownloadUrl(), downloadTask.downloadCallback.needProgress());
            }
        }
    }

    private TXZService.CommandProcessor mCommandProcessor = new TXZService.CommandProcessor() {
        @Override
        public byte[] process(String packageName, String command, byte[] data) {
            Parcel p = Parcel.obtain();
            if (null != data) {
                p.unmarshall(data, 0, data.length);
                p.setDataPosition(0);
                synchronized (mRemoteTask) {
                    if (TextUtils.equals(command, DOWNLOAD_NOTIFY)) { //进度条更新
                        String taskId = p.readString();
                        int progress = p.readInt();
                        DownloadTask downloadTask = mRemoteTask.get(taskId);
                        if (downloadTask != null && downloadTask.downloadCallback != null && downloadTask.downloadCallback.needProgress()) {
                            downloadTask.downloadCallback.onProgress(progress);
                        }
                    } else if (TextUtils.equals(command, DOWNLOAD_RESULT)) {//下载结果回调
                        int errCode = p.readInt();
                        String taskId = p.readString();
                        String file = p.readString();
                        DownloadTask downloadTask = mRemoteTask.remove(taskId);
                        if (downloadTask != null && downloadTask.downloadCallback != null) {
                            DownLoadResult result = new DownLoadResult();
                            result.errCode = errCode;
                            result.filePath = file;
                            downloadTask.downloadCallback.onResult(result);
                        } else {
                            LogUtil.logd("downloadTask == null or downloadTask.downloadCallback == null");
                        }
                    }
                }
            }
            p.recycle();
            return null;
        }
    };

    /**
     * 下载
     */
    public interface DownloadCallback {
        /**
         * 设置下载的链接
         *
         * @return url
         */
        public String getDownloadUrl();

        /**
         * 配置需要进度回调
         *
         * @return true 需要下载进度，false 不需要下载进度
         */
        public boolean needProgress();

        /**
         * 进度的回调
         *
         * @param progress 进度
         */
        public void onProgress(int progress);

        /**
         * 下载的结果回调
         *
         * @param data
         */
        public void onResult(DownLoadResult data);
    }

    /**
     * 下载结果的回调
     */
    public static class DownLoadResult {

        private int errCode = 0;
        private String filePath;

        /**
         * 错误码<br>
         * {@link TXZDownloadManager#EC_DOWNLOAD_SUCCESS} 下载成功<br>
         * {@link TXZDownloadManager#EC_DOWNLOAD_SERVER} 下载服务器状态码异常<br>
         * {@link TXZDownloadManager#EC_DOWNLOAD_IO} io错误<br>
         * {@link TXZDownloadManager#EC_DOWNLOAD_NETWORK} 网络异常<br>
         * {@link TXZDownloadManager#EC_DOWNLOAD_UNKNOWN} 位置错误<br>
         *
         * @return
         */
        public int getErrCode() {
            return errCode;
        }

        /**
         * 获取文件的路径
         *
         * @return
         */
        public String getFilePath() {
            return filePath;
        }

        @Override
        public String toString() {
            return "DownLoadResult{" +
                    "errCode=" + errCode +
                    ", filePath='" + filePath + '\'' +
                    '}';
        }
    }

    class DownloadTask {
        String taskId;
        DownloadCallback downloadCallback;
    }

    private final HashMap<String, DownloadTask> mRemoteTask = new HashMap<String, DownloadTask>();

    /**
     * 新建下载任务
     *
     * @param downloadCallback
     * @return 返回下载任务的taskId ，取消或者保存时使用
     */
    public synchronized String startDownload(DownloadCallback downloadCallback) {
        String taskId = "";
        if (downloadCallback != null) {
            if (!TextUtils.isEmpty(downloadCallback.getDownloadUrl())) {
                taskId = genDownloadTaskId(downloadCallback.getDownloadUrl());
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.taskId = taskId;
                downloadTask.downloadCallback = downloadCallback;
                addDownloadTask(taskId, downloadTask);

                startDownloadInner(taskId, downloadCallback.getDownloadUrl(), downloadCallback.needProgress());

            }
        }
        return taskId;
    }

    /**
     * @param taskId
     * @param url
     * @param needProgress
     */
    private void startDownloadInner(String taskId, String url, boolean needProgress) {
        //发送到core中准备下载
        Parcel p = Parcel.obtain();
        p.writeString(taskId);
        p.writeString(url);
        p.writeByte((byte) (needProgress ? 1 : 0));
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, DOWNLOAD_INVOKE_PREFIX + DOWNLOAD_START, p.marshall(), null);
        p.recycle();
    }

    /**
     * 停止下载任务
     *
     * @param taskId 传入 startDownload返回的
     */
    public synchronized void stopDownload(String taskId) {
        //考虑到适配挂，缓存了taskId的情况
        removeDownloadTask(taskId);
        Parcel p = Parcel.obtain();
        p.writeString(taskId);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, DOWNLOAD_INVOKE_PREFIX + DOWNLOAD_STOP, p.marshall(), null);
        p.recycle();
    }

    private void addDownloadTask(String taskId, DownloadTask downloadTask) {
        synchronized (mRemoteTask) {
            mRemoteTask.put(taskId, downloadTask);
        }
    }

    private boolean removeDownloadTask(String taskId) {
        synchronized (mRemoteTask) {
            if (mRemoteTask.remove(taskId) == null) {
                return false;
            } else {
                return true;
            }
        }
    }

    /**
     * 生成taskId
     *
     * @param url 下载地址
     * @return .sdk_ + md5(pkg + url)
     */
    private String genDownloadTaskId(String url) {
        return DOWNLOAD_TASK_ID_PREFIX + MD5Util.generateMD5(GlobalContext.get().getPackageName() + url).substring(8, 24).toUpperCase();
    }
}
