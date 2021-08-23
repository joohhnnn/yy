package com.txznet.music.service.impl;

import com.txznet.comm.base.ActivityStack;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.action.SoundCommandActionCreator;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.PlayMode;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.SysExitEvent;
import com.txznet.music.util.Utils;
import com.txznet.rxflux.Operation;
import com.txznet.sdk.TXZMusicManager;

/**
 * 第三方可以直接操控的命令字回调
 *
 * @author telen
 * @date 2018/12/28,15:02
 */
public class ExtraCommand extends BaseCommand {

    public ExtraCommand() {

        //同步是否展示UI界面
        addCmd("tongting.isShowUI", (pkgName, cmd, data) -> String.valueOf(ActivityStack.getInstance().getsForegroundActivityCount() != 0).getBytes());

        //是否播放中
        addCmd("tongting.isPlaying", (pkgName, cmd, data) -> String.valueOf(PlayHelper.get().isPlaying()).getBytes());

        // 播放，当前有播放内容则继续播放，否则按打开同听逻辑处理
        addCmd("tongting.start", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().play(Operation.SDK);
            return new byte[0];
        });

        // 播放，当前有播放内容则继续播放，否则按打开同听逻辑处理
        addCmd("tongting.continuePlay", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().play(Operation.SDK);
            return new byte[0];
        });

        addCmd("tongting.playOnlineMusic", (pkgName, cmd, data) -> {
            // TODO: 2018/12/28 播放在线音乐,如果不实现的话, 将TXZTongTingManager相应的接口屏蔽掉
            return new byte[0];
        });

        addCmd("tongting.queryOnlineMusic", (pkgName, cmd, data) -> {
            // TODO: 2018/12/28 播放在线音乐,如果不实现的话, 将TXZTongTingManager相应的接口屏蔽掉
            return new byte[0];
        });
        addCmd("tongting.pause", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().pause(Operation.SDK);
            return new byte[0];
        });
        addCmd("tongting.exit", (pkgName, cmd, data) -> {
            ReportEvent.reportExit(SysExitEvent.EXIT_TYPE_VOICE);
            Utils.exitApp(null);
            return new byte[0];
        });
        addCmd("tongting.next", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().next(Operation.SDK);
            return new byte[0];
        });
        addCmd("tongting.prev", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().prev(Operation.SDK);
            return new byte[0];
        });
        addCmd("tongting.switchModeLoopAll", (pkgName, cmd, data) -> {
            PlayHelper.get().setPlayMode(Operation.SDK, PlayMode.QUEUE_LOOP);
            return new byte[0];
        });
        addCmd("tongting.switchModeLoopOne", (pkgName, cmd, data) -> {
            PlayHelper.get().setPlayMode(Operation.SDK, PlayMode.SINGLE_LOOP);
            return new byte[0];
        });

        addCmd("tongting.switchModeRandom", (pkgName, cmd, data) -> {
            PlayHelper.get().setPlayMode(Operation.SDK, PlayMode.RANDOM_PLAY);
            return new byte[0];
        });
        addCmd("tongting.switchSong", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().next(Operation.SDK);
            return new byte[0];
        });
        addCmd("tongting.playRandom", (pkgName, cmd, data) -> {
            // TODO: 2018/12/28 随便听听
            return new byte[0];
        });
        //同步当前播放的音频模型
        addCmd("tongting.getCurrentMusicModel", (pkgName, cmd, data) -> {
            AudioV5 currAudio = PlayHelper.get().getCurrAudio();
            if (currAudio != null) {
                TXZMusicManager.MusicModel musicModel = new TXZMusicManager.MusicModel();
                musicModel.setTitle(currAudio.name);
                musicModel.setAlbum(currAudio.albumName);
                musicModel.setArtist(currAudio.artist);
                return musicModel.toString().getBytes();
            }
            return new byte[0];
        });

        addCmd("tongting.playFavourMusic", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().playFavour(Operation.SDK);
            return new byte[0];
        });

        //收藏音乐
        //TXZTongTingManager.getInstance().favourMusic();
        addCmd("tongting.favourMusic", (pkgName, cmd, data) -> {
            SoundCommandActionCreator.getInstance().favourOrSubscribe(Operation.SDK, false);
            return new byte[0];
        });

        //取消收藏音乐
        //TXZTongTingManager.getInstance().unfavourMusic();
        addCmd("tongting.unfavourMusic", (pkgName, cmd, data) -> {
            SoundCommandActionCreator.getInstance().unfavourOrUnSubscribe(Operation.SDK, false);
            return new byte[0];
        });
        addCmd("tongting.play.inner", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().playMusic(Operation.SDK);
            return new byte[0];
        });
        addCmd("tongting.play.audio", (pkgName, cmd, data) -> {
            // TODO: 2018/12/28 播放电台,如果这里要实现,记得把TXZTongTingManager的播放电台接口改成这个命令字,可能涉及兼容性的问题
            return new byte[0];
        });
    }
}
