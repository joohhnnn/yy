package com.txznet.music.download;

import android.net.Uri;
import android.os.Bundle;

import com.txznet.audio.bean.SessionInfo;
import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.audio.player.factory.PlayAudioFactory;
import com.txznet.audio.server.response.CacheInfo;
import com.txznet.audio.server.response.HttpMediaResponse;
import com.txznet.audio.server.response.MediaHttpClient;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.playerModule.bean.PlayItem;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.PlayItemManager;
import com.txznet.music.utils.CollectionUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

/**
 * Created by telenewbie on 2018/2/1.
 */

/**
 * 下载音频的管理类
 */
public class DownloadManager {


    //##创建一个单例类##
    private volatile static DownloadManager singleton;

    private DownloadManager() {
    }

    public static DownloadManager getInstance() {
        if (singleton == null) {
            synchronized (DownloadManager.class) {
                if (singleton == null) {
                    singleton = new DownloadManager();
                }
            }
        }
        return singleton;
    }

    public void download(final Audio audio, final OnDownloadListener listener) {
        downloadPiece(audio, 0, -1, listener);

    }


    public void downloadPiece(final Audio audio, final long from, final long to, final OnDownloadListener listener) {


        PlayItemManager.getInstance().createPlayItem(audio, new PlayItemManager.IPlayItemListener() {
            @Override
            public void onSuccess(final PlayItem playItem) {
                //TODO:应该用线程池的形式
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Logger.d(Constant.PRELOAD_TAG, "success:" + playItem.toString());
                        if (playItem.getType() == PlayItem.TYPE_FILE) {
                            listener.onSuccess();
                            return;
                        }
                        MediaHttpClient<Audio> mediaHttpClient = new MediaHttpClient<Audio>(audio) {
                            long len = -1;
                            CacheInfo cacheInfo = null;
                            int currentDownloadPosition = 0;

                            @Override
                            public void onMediaError(Error error) throws IOException {
                                Logger.d(Constant.PRELOAD_TAG, "error:" + error.toString());
                            }

                            @Override
                            public void onResponse(int statusCode, String statusLine) throws IOException {
                                if (statusCode / 100 != 2) {
                                    if (statusCode == 302) {
                                        return;
                                    }
                                    listener.onError(-2);//返回码错误
                                }
                            }

                            @Override
                            public void onGetInfo(Map<String, String> headers, String mimeType, long contentLength) throws IOException {
                                String contentRange = headers.get("Content-Range");
                                try {
                                    len = Long.parseLong(contentRange.split("/")[1]);
                                } catch (Exception e) {
                                    len = -1;
                                    cancel();
                                }
                            }

                            @Override
                            public void onReadData(byte[] data, int offset, int len) throws IOException {
                                if (cacheInfo == null) {
                                    SessionInfo mSess = new SessionInfo(PlayAudioFactory.createPlayAudio(playItem, audio));
                                    cacheInfo = CacheInfo.createCacheInfo(mSess, len);
                                }
                                cacheInfo.addCacheBlock(currentDownloadPosition, data,
                                        offset, len);
                                currentDownloadPosition += len;
                            }
                        };


                        URI uri = null;
                        try

                        {
                            if (CollectionUtils.isNotEmpty(playItem.getUrls())) {
                                for (String url : playItem.getUrls()) {
                                    uri = new URI(url);
                                    mediaHttpClient.getMedia(uri, from, to);
                                    if (mediaHttpClient.getSuggestRetryDelay() <= 0) {//请求成功返回
                                        break;
                                    }
                                }
                            }
                            if (mediaHttpClient.getSuggestRetryDelay() > 0) {
                                //回调错误
                                if (null != listener) {
                                    listener.onError(-3);
                                }
                            } else {
                                if (null != listener) {
                                    listener.onSuccess();
                                }
                            }
                        } catch (
                                URISyntaxException e)

                        {
                            e.printStackTrace();
                        } catch (
                                IOException e)

                        {
                            e.printStackTrace();
                        }

                    }
                }).start();
            }

            @Override
            public void onError(Error error) {
                Logger.d(Constant.PRELOAD_TAG, "error:" + error.toString());
                if (null != listener) {
                    listener.onError(-1);
                }
            }
        });
    }

    public interface OnDownloadListener {
        void onSuccess();

        void onError(int code);
    }

}
