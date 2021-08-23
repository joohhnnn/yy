package com.txznet.audio.player.factory;

import android.text.TextUtils;

import com.txznet.audio.player.audio.FileAudio;
import com.txznet.audio.player.audio.NetAudio;
import com.txznet.audio.player.audio.PlayerAudio;
import com.txznet.audio.player.audio.QQMusicAudio;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.playerModule.bean.PlayItem;

public class PlayAudioFactory {

//    public static PlayerAudio createPlayAudio(Audio currentAudio) {
//
//        PlayerAudio playAudio = null;
////        currentAudio.setStrDownloadUrl("http://119.147.83.17/streamoc.music.tc.qq.com/C200003HeDdo13D71J.m4a?vkey=CD308128CA4B3B116A85B2C958EC05948C78652AA410F141FD227855B5412626BC1F642B5FF8283A4630E1FA60CFFC3A26BF60FD3A835C24&guid=de5de9d6a56fb90bae931bf5f429a338&fromtag=0");
//
////        if (TextUtils.equals("1", currentAudio.getDownloadType()) && (currentAudio.getSid() != Constant.LOCAL_MUSIC_TYPE)) {
////            playAudio = new QQMusicAudio(currentAudio);
////        } else if (StringUtils.isNotEmpty(currentAudio.getStrDownloadUrl())
////                && !currentAudio.getStrDownloadUrl().startsWith("http")) {
////            playAudio = new FileAudio(currentAudio.getStrDownloadUrl());
////        } else {
////            playAudio = new NetAudio(currentAudio);
////        }
//        return playAudio;
//    }


    public static PlayerAudio createPlayAudio(PlayItem playItem, Audio audio) {
        PlayerAudio playerAudio = null;
        switch (playItem.getType()) {
            case PlayItem.TYPE_FILE:
                playerAudio = new FileAudio(playItem.getUrls().get(0));
                break;
            case PlayItem.TYPE_NET:
                playerAudio = new NetAudio(playItem, audio);
                break;
            case PlayItem.TYPE_QQ:
                playerAudio = new QQMusicAudio(playItem, audio);
                break;


            default:
                break;
        }
        return playerAudio;
    }
}
