package com.txznet.music.aTestPlayerModule;

import com.txznet.music.albumModule.bean.Audio;

import java.util.List;

/**
 * 播放列表
 * 名字我先随便瞎起，到时候，确定了之后再进行修改
 */
public interface IPlayInfoTestManager {


    public interface IPlayListListener {
        //播放列表
        void onPlayList(List<Audio> audios);
    }

    /**
     * 添加到播放列表
     *
     * @param audioList
     */
    public void addPlayList(List<Audio> audioList);


    /**
     * 添加单首歌曲到播放列表
     *
     * @param audio
     */
    public void addPlayList(Audio audio);


    public void removePlayList(Audio audio);

    public void removePlayLists(List<Audio> audios);

    public void getPlaylist();
}
