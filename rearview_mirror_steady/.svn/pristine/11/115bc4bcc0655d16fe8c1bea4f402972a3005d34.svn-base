package com.txznet.music.helper;

import android.os.SystemClock;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.http.NetRequestManager;
import com.txznet.music.data.http.api.txz.entity.TXZAudio;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ThreadManager;
import com.txznet.proxy.ProxyParam;
import com.txznet.proxy.ProxySession;
import com.txznet.proxy.ProxyUtils;
import com.txznet.proxy.SessionManager;
import com.txznet.proxy.cache.LocalBuffer;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.RxAction;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @author zackzhou
 * @date 2019/1/24,15:21
 */

public class PreLoadHelper {

    public static final String TAG = Constant.LOG_TAG_UTILS + ":PreLoad";

    private PreLoadHelper() {

    }

    private static int lastPreloadHash;

    /**
     * 强制加载
     */
    public static void forceNeedMoreData() {
        if (TXZFileConfigUtil.getBooleanSingleConfig(Configuration.Key.ENABLE_MEDIA_PROXY, Configuration.DefVal.ENABLE_MEDIA_PROXY)) { // 开启本地代理服务
            AudioV5 audio = PlayHelper.get().getCurrAudio();
            if (audio != null) {
                ProxySession session = SessionManager.get().findSessionByTag(audio.sid, audio.id);
                if (session == null) {
                    Logger.w(TAG, "forceNeedMoreData not found, audio=" + audio.sid + "_" + audio.id);
                } else {
                    if (!session.param.needMoreData) {
                        session.param.needMoreData = true;
                        Logger.w(TAG, "forceNeedMoreData, audio=" + audio.name);
                    }
                }
            }
        }
    }

    public static class PreloadProxyCallback implements ProxyParam.IProxyCallback {
        private Audio audio;

        public PreloadProxyCallback(Audio audio) {
            this.audio = audio;
        }

        @Override
        public void onError(int errorCode, String desc, String hint) {

        }

        @Override
        public void onBufferingUpdate(List<LocalBuffer> buffers) {

        }

        @Override
        public void onDownloadComplete() {
            Logger.d("Proxy", "onDownloadComplete");
            Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PROXY_DOWNLOAD_COMPLETE)
                    .bundle(Constant.ProxyConstant.KEY_AUDIO, AudioConverts.convert2Audio(audio))
                    .build());
        }

        @Override
        public float getPlayPercent() {
            return 0;
        }

        @Override
        public long getDuration() {
            return 0;
        }

        @Override
        public boolean isPlaying() {
            return true;
        }
    }

    /**
     * 预加载
     */
    public static void preloadNext() {
        Audio audio = AudioPlayer.getDefault().getQueue().getNextItem();
        if (audio != null && !Objects.equals(audio, AudioPlayer.getDefault().getCurrentAudio())) {
            int hash = audio.hashCode();
            if (hash != lastPreloadHash) {
                Logger.w(TAG, "preloadNext, audio=" + audio.name);
                lastPreloadHash = hash;
                File tmdFile = AudioUtils.getAudioTMDFile(audio);
                if (!AudioUtils.isLocalSong(audio.sid) && (tmdFile == null || !tmdFile.exists())) {
                    TXZUri uri = TXZUri.parse(audio.sourceUrl);
                    if (uri == null) {
                        Logger.w(TAG, "uri parse failed, sourceUrl=" + audio.sourceUrl);
                        return;
                    }
                    if (TXZAudio.DOWNLOADTYPE_PROXY.equals(uri.downloadType)) { // qq 需要预处理
                        Disposable disposable = TXZMusicDataSource.get().getAudioPlayUrls(AudioConverts.convert2Audio(audio))
                                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(playUrlInfo -> {
                                    List<String> urls = new ArrayList<>();
                                    urls.add(playUrlInfo.strUrl);
                                    if (playUrlInfo.arrBackUpUrl != null) {
                                        urls.addAll(playUrlInfo.arrBackUpUrl);
                                    }
                                    if (Objects.equals(audio, AudioPlayer.getDefault().getCurrentAudio())) {
                                        return;
                                    }
                                    reqProxy(audio, ProxyHelper.getProxyUrl(audio, new PreloadProxyCallback(audio), urls.toArray(new String[0])));
                                }, throwable -> {
                                });
                        NetRequestManager.addMonitor("getTXZUrl", disposable);
                    } else { // 可直接用downloadUrl下载
                        reqProxy(audio, ProxyHelper.getProxyUrl(audio, new PreloadProxyCallback(audio), uri.downloadUrl));
                    }
                }
            }
        }
    }


    private static void reqProxy(Audio audio, String proxyUrl) {
        ThreadManager.getPool().execute(() -> {
            try {
                long start = SystemClock.elapsedRealtime();
                HttpURLConnection connection = openConnection(proxyUrl, 0);

                ProxySession session = SessionManager.get().findSessionByTag(audio.sid, audio.id);
                if (session != null) {
                    session.param.needMoreData = true;
                    session.param.needMoreWriteData = true;
                }
                int perfectPieceSize = 1024 * 512;

                try (InputStream is = connection.getInputStream()) {
                    int hasRead = 0;
                    int statistic = 0;
                    byte[] buff = new byte[8196];
                    while (hasRead < perfectPieceSize) {
                        if (SystemClock.elapsedRealtime() - start > 50 * 1000) { // 下载时间超过50s
                            Logger.w(TAG, "req buff timeout, audio=" + audio.name);
                            break;
                        }
                        int tmp = is.read(buff);
                        hasRead += tmp;
                        statistic += tmp;
                        if (statistic / 60000 > 0 || hasRead >= perfectPieceSize) {
                            statistic = 0;
                            Logger.w(TAG, "req buff hasRead=" + hasRead + ", audio=" + audio.name);
                        }
                    }
                }
                connection.disconnect();
                Logger.w(TAG, "req buff disconnect, audio= " + audio.name);

                if (session != null && session.len > 0) {
                    Logger.w(TAG, "session len has read, len=" + session.len);
                    Logger.w(TAG, "req buff end, audio= " + audio.name);
                    connection = openConnection(proxyUrl, session.len - 128);
                    try (InputStream is = connection.getInputStream()) {
                        int hasRead = 0;
                        byte[] buff = new byte[128];
                        hasRead = is.read(buff);
                        Logger.w(TAG, "req buff end hasRead=" + hasRead + ", audio=" + audio.name);
                    }
                    connection.disconnect();
                    Logger.w(TAG, "req buff end disconnect, audio= " + audio.name);
                }
            } catch (IOException e) {
                Logger.w(TAG, "req buff end disconnect, audio= " + audio.name);
//                Logger.e(TAG, "req buff error, audio= " + audio.name + ", msg=" + e.getMessage());
//                e.printStackTrace();
            }
        });
    }

    private static HttpURLConnection openConnection(String proxyUrl, long from) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(proxyUrl).openConnection();
        connection.setRequestMethod("GET");
        connection.addRequestProperty("icy-metadata", "1");
        connection.addRequestProperty("host", "127.0.0.1:" + ProxyUtils.getProxyPort());
        connection.addRequestProperty("range", "bytes=" + from + "-");
        connection.addRequestProperty("user-agent", "Lavf/57.83.100");
        connection.addRequestProperty("connection", "close");
        connection.addRequestProperty("accept", "*/*");
        connection.setConnectTimeout(60 * 1000);
        connection.setReadTimeout(60 * 1000);
        return connection;
    }
}
