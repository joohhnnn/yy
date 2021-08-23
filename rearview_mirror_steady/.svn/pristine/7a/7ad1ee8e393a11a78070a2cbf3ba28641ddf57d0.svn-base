package com.txznet.txz.component.music.remote;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.sdk.TXZMusicManager.MusicModel;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.music.util.StringInfoUtils;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable1;

public class MusicRemoteImpl {
    private String mMusicToolServiceName = "";
    private byte[] mRemoteMusicToolgetCurrentMusicModel;
    private boolean mIsNotNeedTts = false;

    // ##创建一个单例类##
    private volatile static MusicRemoteImpl singleton;

    private MusicRemoteImpl() {
    }

    public static MusicRemoteImpl getInstance() {
        if (singleton == null) {
            synchronized (MusicRemoteImpl.class) {
                if (singleton == null) {
                    singleton = new MusicRemoteImpl();
                }
            }
        }
        return singleton;
    }

    public boolean isValid() {
        return StringUtils.isNotEmpty(mMusicToolServiceName);
    }

    public boolean getIsNotNeedTts() {
        return mIsNotNeedTts;
    }

    public void setmIsNotNeedTts(boolean mIsNotNeedTts) {
        this.mIsNotNeedTts = mIsNotNeedTts;
    }

    public void setPackageName(String packagename) {
        mMusicToolServiceName = packagename;
    }

    public String getPackageName() {
        return mMusicToolServiceName;
    }

    public void setMusicModel(byte[] modelJson) {
        mRemoteMusicToolgetCurrentMusicModel = modelJson;
    }

    public void start() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.play", null, null);
    }

    public void continuePlay() {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("continue", true);
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.play", jsonBuilder.toBytes(), null);
    }

    public void pause() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.pause", null, null);
    }

    public void exit() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.exit", null, null);
    }

    public void exitImmediately() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.exit", null, null);
    }

    public void next() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.next", null, null);
    }

    public void prev() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.prev", null, null);
    }

    public void switchModeLoopAll() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.switchModeLoopAll", null, null);
    }

    public void switchModeLoopOne() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.switchModeLoopOne", null, null);

    }

    public void switchModeRandom() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.switchModeRandom", null, null);
    }

    public void switchSong() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.switchSong", null, null);
    }

    public void playRandom() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.playRandom", null, null);
    }

    public void playMusic(MusicModel musicModel) {
        String[] kws = musicModel.getKeywords();
        String kw = "";
        String title = musicModel.getTitle();

        if (!TextUtils.isEmpty(title)) {
            kw = title;
        }
        if (kws != null && kws.length > 0) {
            for (String k : kws) {
                if (TextUtils.isEmpty(k))
                    continue;
                if (!kw.isEmpty()) {
                    kw += "";
                }
                kw += k;
            }
        }
        String title1 = StringInfoUtils.genMediaModelTitle(musicModel.getTitle(), musicModel.getAlbum(), musicModel.getArtist(), kw, "歌曲");

        if (mIsNotNeedTts) {
            if (RecorderWin.isOpened()) {
                RecorderWin.close();
            }

            ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.playMusic", musicModel.toString().getBytes(), null);
        } else {
            String spk = NativeData.getResPlaceholderString("RS_MUSIC_WILL_PLAY", "%MUSIC%", title1);
            RecorderWin.speakTextWithClose(spk, new Runnable1<byte[]>(musicModel.toString().getBytes()) {
                @Override
                public void run() {
                    ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.playMusic", mP1,
                            null);
                }
            });
        }
    }

    public MusicModel getCurrentMusicModel() {
        return MusicModel.fromString(new String(mRemoteMusicToolgetCurrentMusicModel));
    }

    public boolean favourMusic() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.favourMusic", null, null);
        return true;
    }

    public boolean unfavourMusic() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.unfavourMusic", null, null);
        return true;
    }

    public void playFavourMusic() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.playFavourMusic", null, null);
    }

    public void cancelRequest() {
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.cancelRequest", null, null);
    }

    public boolean supportRequestHistory() {
        // TODO Auto-generated method stub
        return false;
    }

    public void requestHistory(String type) {
        // TODO Auto-generated method stub

    }

    public void openApp() {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("open", true);
        ServiceManager.getInstance().sendInvoke(mMusicToolServiceName, "tool.music.play", jsonBuilder.toBytes(), null);
    }
}
