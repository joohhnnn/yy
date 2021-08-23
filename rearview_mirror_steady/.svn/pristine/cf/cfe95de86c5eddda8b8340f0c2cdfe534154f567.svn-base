package com.txznet.music.playerModule.logic;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.utils.Utils;

import junit.framework.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 播放逻辑,属于播放业务的需求
 * Created by telenewbie on 2018/3/13.
 */

public class PlayerBizLogic {


    private static final String TAG = "music:PlayerBizLogic:";


    //##创建一个单例类##
    private volatile static PlayerBizLogic singleton;

    private PlayerBizLogic() {
    }

    public static PlayerBizLogic getInstance() {
        if (singleton == null) {
            synchronized (PlayerBizLogic.class) {
                if (singleton == null) {
                    singleton = new PlayerBizLogic();
                }
            }
        }
        return singleton;
    }


    /**
     * 从播单中删除哪一类的碎片
     *
     * @param audio
     * @param sourceId 来源:本地,历史,收藏等
     */
    public void deleteAudioFromPlayList(Audio audio, int sourceId) {
        Assert.assertNotNull(audio);

        if (sourceId != PlayInfoManager.getInstance().getCurrentScene()) {
            LogUtil.d(TAG, "needn't refresh player list ,scene isn't same");
            return;
        }
        //场景相同
        PlayInfoManager.getInstance().removePlayListAudio(audio);//删除播单中的碎片

        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        if (!audio.equals(currentAudio)) {
            LogUtil.d(TAG, "needn't refresh switch audio ,current audio haven't delete");
            return;
        }
        //相同的碎片(与正在播放的碎片),则跳歌
        Audio nextaudio = getNextCanPlayAudio();//获取下一首歌曲
        LogUtil.d(TAG, "will play " + (nextaudio == null ? "" : nextaudio.toString()));
        if (nextaudio == null) {
            notifyEmptyList();
        } else {
            PlayEngineFactory.getEngine().playAudio(EnumState.Operation.auto, nextaudio);
        }
    }

    /**
     * 从播单中删除哪一类的碎片
     *
     * @param audios
     * @param sourceId 来源:本地,历史,收藏等
     */
    public void deleteAudiosFromPlayList(List<Audio> audios, int sourceId) {
        Assert.assertNotNull(audios);

        if (sourceId != PlayInfoManager.getInstance().getCurrentScene()) {
            LogUtil.d(TAG, "needn't refresh player list ,scene isn't same");
            return;
        }
        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        //场景相同
        PlayInfoManager.getInstance().removePlayListAudios(audios);//删除播单中的碎片
        if (!audios.contains(currentAudio)) {
            LogUtil.d(TAG, "needn't refresh switch audio ,current audio haven't delete");
            return;
        }
        //相同的碎片(与正在播放的碎片),则跳歌
        Audio nextaudio = getNextCanPlayAudio();//获取下一首歌曲
        LogUtil.d(TAG, "will play " + (nextaudio == null ? "" : nextaudio.toString()));
        if (nextaudio == null) {
            notifyEmptyList();
        } else {
            PlayEngineFactory.getEngine().playAudio(EnumState.Operation.auto, nextaudio);
        }
    }

    /**
     * 更新当前没有播单
     */
    private void notifyEmptyList() {
        ObserverManage.getObserver().send(InfoMessage.PLAYER_LIST_EMPTY);
    }

    /**
     * 获取下一首可以播放的音乐,并刷新播放列表
     *
     * @return
     */
    public Audio getNextCanPlayAudio() {
        Audio nextAudio = PlayInfoManager.getInstance().getNextAudio(true);
        if (nextAudio == null) {
            return null;
        }
        ///FIXME:判断是本地音乐 出现问题.
        if (Utils.isLocalSong(nextAudio.getSid())) {//判断是本地音乐
            if (!new File(nextAudio.getStrDownloadUrl()).exists()) {
                PlayInfoManager.getInstance().removeNotExistLocalFile();
                return getNextCanPlayAudio();
            }
        }
        return nextAudio;
    }


    /**
     * @param audios
     * @param sourceId 场景
     */
    public void updatePlayerList(List<Audio> audios, int sourceId) {

        //相同场景的可以被修改
        int currentScene = PlayInfoManager.getInstance().getCurrentScene();
        if (currentScene != sourceId) {
            LogUtil.d(TAG, "needn't update player list ,scene isn't same");
            return;
        }
        //【【同听4.4.1】【本地音乐】正在播放本地音乐“给你的歌”，此时声控我要听“尘埃”，点击本地TAB，此时音乐切歌播放“从此以后”】
        //https://www.tapd.cn/21711881/bugtrace/bugs/view?bug_id=1121711881001004256

        List<Audio> audiosLocal = new ArrayList<>(audios);
        if (PlayInfoManager.getInstance().getCurrentScene() == PlayInfoManager.DATA_LOCAL) {
            ArrayList<Audio> playlist = new ArrayList<>(PlayInfoManager.getInstance().getPlayList());
            List<Audio> insertAudios = new ArrayList<>();
            for (Audio audio : playlist) {
                if (audio.getIsInsert() == 1 && !audiosLocal.contains(audio)) {
                    insertAudios.add(audio);
                }
            }
            audiosLocal.addAll(0, insertAudios);
        }


        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        //设置新的播单
        PlayInfoManager.getInstance().setAudios(audiosLocal, currentScene);

        //新的播单中,当前播放的碎片不存在.
        if (!audiosLocal.contains(currentAudio)) {
            Audio nextCanPlayAudio = getNextCanPlayAudio();
            PlayEngineFactory.getEngine().playAudio(EnumState.Operation.auto, nextCanPlayAudio);
        }
    }


}
