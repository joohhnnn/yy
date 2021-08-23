package com.txznet.webchat.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.SystemClock;

import com.txznet.loader.AppLogic;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 文件下载工具
 * Created by J on 2017/6/22.
 */

public class FileDownloadHelper {
    private static final long PROGRESS_REPORT_INTERVAL = 200;
    private static final int STUCK_CHECK_INTERVAL = 10 * 1000;

    private DownloadCallback mDownloadCallback;
    private String mDownloadUrl;
    private String mFilePath;
    private DownloadTask mDownloadTask;
    private int mCurrentProgress = 0;
    private boolean bDownloading = false;

    public void startDownload(String url, String filePath, DownloadCallback callback) {
        mDownloadUrl = url;
        mFilePath = filePath;
        mDownloadCallback = callback;
        mCurrentProgress = 0;

        bDownloading = true;
        mDownloadTask = new DownloadTask(AppLogic.getApp());
        mDownloadTask.execute(mDownloadUrl, mFilePath);
        AppLogic.runOnBackGround(mCheckStuckTask, STUCK_CHECK_INTERVAL);
    }

    public void cancel() {
        bDownloading = false;
        if (null != mDownloadTask) {
            mDownloadTask.cancel(true);
        }


        AppLogic.removeBackGroundCallback(mCheckStuckTask);
    }


    public interface DownloadCallback {
        /**
         * 下载开始
         */
        void onStart();

        /**
         * 下载进度更新
         *
         * @param progress
         */
        void onProgress(float progress);

        /**
         * 下载完成
         */
        void onFinished();

        /**
         * 下载失败
         */
        void onError(String err);

        /**
         * 下载取消
         */
        void onCancel();
    }


    class DownloadTask extends AsyncTask<String, Integer, String> {
        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private long mLastReportProgressTime = 0;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onCancelled(String s) {
            mDownloadCallback.onCancel();
            bDownloading = false;
            AppLogic.removeBackGroundCallback(mCheckStuckTask);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mDownloadCallback.onStart();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            mCurrentProgress = progress[0];
            long currentTime = SystemClock.elapsedRealtime();

            if (currentTime - mLastReportProgressTime >= PROGRESS_REPORT_INTERVAL) {
                mDownloadCallback.onProgress(progress[0]);
                mLastReportProgressTime = currentTime;
            }

        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            bDownloading = false;
            AppLogic.removeBackGroundCallback(mCheckStuckTask);

            if (result != null)
                mDownloadCallback.onError(result);
            else
                mDownloadCallback.onFinished();
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Accept-Encoding", "identity");
                connection.connect();
                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    String cause = "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                    mDownloadCallback.onError(cause);
                    return cause;
                }

                int fileLength = connection.getContentLength();
                // 文件长度为0, 按失败处理
                /*if (0 >= fileLength) {
                    return "file length is 0";
                }*/
                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(mFilePath);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;

                    publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }

    private int mLastCheckProgress = 0;
    // 下载任务阻塞检查 下载进度长时间卡住时按失败处理
    private Runnable mCheckStuckTask = new Runnable() {
        @Override
        public void run() {
            if (100 == mCurrentProgress || !bDownloading) {
                return;
            }

            // 当前进度与上次检查进度相同视为任务卡住
            if (mCurrentProgress == mLastCheckProgress) {
                mDownloadTask.cancel(true);
                mDownloadCallback.onError("download stucked for over 10 seconds");

                mLastCheckProgress = 0;
                return;
            }

            mLastCheckProgress = mCurrentProgress;

            // 一段时间后重复检查
            AppLogic.removeBackGroundCallback(this);
            AppLogic.runOnBackGround(this, STUCK_CHECK_INTERVAL);
        }
    };

    //////// single instance
    private static FileDownloadHelper sInstance;

    public static FileDownloadHelper getInstance() {
        if (null == sInstance) {
            synchronized (FileDownloadHelper.class) {
                if (null == sInstance) {
                    sInstance = new FileDownloadHelper();
                }
            }
        }

        return sInstance;
    }

    private FileDownloadHelper() {
    }
}