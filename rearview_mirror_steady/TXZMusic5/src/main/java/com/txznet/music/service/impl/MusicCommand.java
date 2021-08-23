package com.txznet.music.service.impl;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.action.SoundCommandActionCreator;
import com.txznet.music.data.entity.PlayMode;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.SysExitEvent;
import com.txznet.music.report.entity.SysOpenEvent;
import com.txznet.music.util.Logger;
import com.txznet.music.util.Utils;
import com.txznet.rxflux.Operation;

public class MusicCommand extends BaseCommand {

    public MusicCommand() {
        ICallback releaseAudioFocusCallback = (pkgName, cmd, data) -> {
            String value = new String(data);
            Logger.d(TAG, "[Audio]set releaseAudioFocus:" + value);
            Boolean isRelease = Boolean.valueOf(value);
            SharedPreferencesUtils.setReleaseAudioFocus(isRelease);
            return new byte[0];
        };
        addCmd(new String[]{"releaseAudioFocus",
                        "tongting.releaseAudioFocus"},
                releaseAudioFocusCallback);

        addCmd("updateFavour", (pkgName, cmd, data) -> {
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            boolean favour = Boolean.parseBoolean(jsonBuilder.getVal("favour", String.class, "false"));
            if (favour) {
                SoundCommandActionCreator.getInstance().favourOrSubscribe(Operation.SOUND, false);
            } else {
                SoundCommandActionCreator.getInstance().unfavourOrUnSubscribe(Operation.SOUND, false);
            }
            return new byte[0];
        });

        //["打开音乐", "播放音乐", "听音乐", "播音乐", "播放歌曲", "听歌曲", "播歌曲", "播歌", "听歌", "我要听音乐", "随便听听", "随意听听", "随便来首歌", "随便来首音乐", "随便来点歌", "你随便唱吧", "好听的歌有哪些", "放首歌听", "放首歌听听", "放首歌"],
        addCmd(new String[]{"open.play", "open"}, (pkgName, cmd, data) -> {
            if (data != null) {
                //如果里面有值的话
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                String key = jsonBuilder.getVal("target", String.class, "");
                if ("audio".equals(key)) { // 播放电台
                    Utils.back2HomeWithRadio();
                    ReportEvent.reportEnter(SysOpenEvent.ENTRY_TYPE_VOICE);
                    return new byte[0];
                }
            }
            // 播放音乐
            Utils.back2HomeWithMusic();
            ReportEvent.reportEnter(SysOpenEvent.ENTRY_TYPE_VOICE);
            return new byte[0];
        });

        // 播放，当前有播放内容则继续播放，否则按打开同听逻辑处理
        addCmd(new String[]{/*"tongting.play",*/ "play"}, (pkgName, cmd, data) -> {
            PlayerActionCreator.get().play(getOperationByCmd(cmd));
            return new byte[0];
        });

        // 上一首
        addCmd(new String[]{/*"tongting.prev",*/ "prev"}, (pkgName, cmd, data) -> {
            PlayerActionCreator.get().prev(getOperationByCmd(cmd));
            return new byte[0];
        });

        // 下一首
        addCmd(new String[]{/*"tongting.next",*/ "next"}, (pkgName, cmd, data) -> {
            PlayerActionCreator.get().next(getOperationByCmd(cmd));
            return new byte[0];
        });

        //以下参照MusicTongting以开头"music.的所有命令字
        /////


        ////"MUSIC_HATE" : ["不喜欢这首歌","讨厌这首歌","不好听","这首歌不好听"],
        addCmd("hate.audio", (pkgName, cmd, data) -> {
            SoundCommandActionCreator.getInstance().unfavourOrUnSubscribe(Operation.SOUND, false);
            PlayerActionCreator.get().next(Operation.SOUND);
            return new byte[0];
        });

        // FIXME: 2019/1/16 查看历史功能移除 - 5.0版本
        //["查看播放历史","收听之前听过的节目","查看历史","收听之前的电台","查看收听历史","查看最近播放","查看最近播放历史","查看最近收听"],
        addCmd("sound.history.find", (pkgName, cmd, data) -> {
            return new byte[0];
        });
        //sound.history.find的取消监听
        addCmd("sound.history.cancelfind", (pkgName, cmd, data) -> {
            return new byte[0];
        });

        addCmd("playRandom", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().play(Operation.SOUND);
            return new byte[0];
        });


        addCmd("exit", (pkgName, cmd, data) -> {
            ReportEvent.reportExit(SysExitEvent.EXIT_TYPE_VOICE);
            Utils.exitApp(null);
            return new byte[0];
        });

        addCmd("pause", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().pause(Operation.SOUND);
            return new byte[0];
        });

        addCmd("switchModeLoopAll", (pkgName, cmd, data) -> {
            PlayHelper.get().setPlayMode(Operation.SOUND, PlayMode.QUEUE_LOOP);
            return new byte[0];
        });
        addCmd("switchModeLoopOne", (pkgName, cmd, data) -> {
            PlayHelper.get().setPlayMode(Operation.SOUND, PlayMode.SINGLE_LOOP);
            return new byte[0];
        });
        addCmd("switchModeRandom", (pkgName, cmd, data) -> {
            PlayHelper.get().setPlayMode(Operation.SOUND, PlayMode.RANDOM_PLAY);
            return new byte[0];
        });


        //["播放收藏", "播放收藏列表", "播放收藏音乐", "播放收藏音乐列表", "播放收藏歌曲", "播放收藏歌曲列表"],
        addCmd("play.favour", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().playFavour(Operation.SOUND);
            return new byte[0];
        });
        // ["订阅", "订阅这节目", "订阅当前电台", "订阅这个专辑", "订阅这个电台", "我要订阅", "订阅电台", "订阅这个节目", "加入订阅", "订阅这个栏目"],
        addCmd("addSubscribe", (pkgName, cmd, data) -> {
            SoundCommandActionCreator.getInstance().favourOrSubscribe(Operation.SOUND, false);
            return new byte[0];
        });
        addCmd("unSubscribe", (pkgName, cmd, data) -> {
            SoundCommandActionCreator.getInstance().unfavourOrUnSubscribe(Operation.SOUND, false);
            return new byte[0];
        });
        //["播放订阅", "播放订阅列表", "播放订阅节目", "播放订阅专辑", "播放订阅栏目", "播放订阅电台"],
        addCmd("play.subscribe", (pkgName, cmd, data) -> {
            PlayerActionCreator.get().playSubscribe(Operation.SOUND);
            return new byte[0];
        });

    }

    private Operation getOperationByCmd(String cmd) {
        return cmd.startsWith("tongting.") ? Operation.SDK : Operation.SOUND;
    }


}
