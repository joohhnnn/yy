package com.txznet.audio.player.audio;

import android.os.Environment;

import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.playerModule.bean.PlayItem;
import com.txznet.music.utils.DataInterfaceBroadcastHelper;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.Utils;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZMusicManager.MusicModel;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NetAudio extends PlayerAudio {


    protected Audio mAudio;
    protected PlayItem mPlayItem;
    protected int mUrlIndex = 0;
    File mCacheFile = null;
    private Audio tempAudio;
    private boolean mConnectSuccess = false;

    public NetAudio(PlayItem playItem, Audio audio) {
        mAudio = audio;
        mPlayItem = playItem;
        if (getFinalFile() != null) {
            tempAudio = new Audio();
            tempAudio.setStrDownloadUrl(getFinalFile() == null ? mAudio.getStrDownloadUrl() : getFinalFile().getAbsolutePath());
            tempAudio.setSid(mAudio.getSid());
            tempAudio.setName(mAudio.getName());
            tempAudio.setArrArtistName(mAudio.getArrArtistName());
            tempAudio.setDuration(mAudio.getDuration());
            tempAudio.setId(mAudio.getId());
        }
    }

    @Override
    public String getAudioName() {
        return mAudio.getName();
    }

    public String getUrl() {
        return mPlayItem.getUrls().get(0);
    }

    @Override
    public boolean needCodecPlayer() {
        return mAudio.getStrDownloadUrl().toLowerCase(Locale.CHINESE).endsWith(".opus");
    }

    public File getCacheDir() {
        if (mCacheFile == null) {
            try {
                if (Utils.isSong(mAudio.getSid())) {
                    mCacheFile = new File(Environment.getExternalStorageDirectory(), "txz/cache/song");
                } else {
                    mCacheFile = new File(Environment.getExternalStorageDirectory(), "txz/cache/other");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mCacheFile;
    }

    public File getFinalFile() {
        return Utils.getAudioTMDFile(mAudio);
    }

    protected String calCacheId(String key) {
        try {
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = mdInst.digest(key.getBytes());
            StringBuilder hexValue = new StringBuilder();
            for (int i = 0; i < md5Bytes.length; i++) {
                int val = ((int) md5Bytes[i]) & 0xff;
                if (val < 16)
                    hexValue.append("0");
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public String getCacheId() {
        return calCacheId(this.getUrl());
    }

    public String switchUrl() {
        List<String> urls = mPlayItem.getUrls();
        int offset = 0;
        if (!mConnectSuccess) {
            offset = 1;
        }
        mUrlIndex = (mUrlIndex + offset) % urls.size();
        return urls.get(mUrlIndex);
    }


    public List<String> getAllUrls() {
        return mPlayItem.getUrls();
    }

    public byte[] getAudioInfo() {
        return JsonHelper.toJson(tempAudio).getBytes();
    }


    public void setConnectSuccess() {
        mConnectSuccess = true;
    }

    /**
     * // 发送广播给客户端，提示下载完成的通知
     */
    public void onDownloadComplete() {
        DataInterfaceBroadcastHelper.sendDownloadBroadcast(tempAudio);
        sendToCore();
    }

    /**
     * TODO 同步给同行者,做离线识别，必须在主进程全量同步
     */
    private void sendToCore() {
        MusicModel model = new MusicModel();
        model.setAlbum(mAudio.getAlbumName());
        model.setArtist(CollectionUtils.toStrings(mAudio.getArrArtistName()));
        model.setTitle(mAudio.getName());
        model.setPath(mAudio.getStrDownloadUrl());
        List<MusicModel> musics = new ArrayList<TXZMusicManager.MusicModel>();
        musics.add(model);
        TXZMusicManager.getInstance().syncExMuicListToCore(musics);
    }

    public long getExpTime() {
        return mAudio.getIExpTime();
    }

}
