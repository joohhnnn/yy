package com.txznet.music.helper;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.BreakpointDao;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.Breakpoint;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.RxAction;

import java.util.List;

/**
 * 音频断点记录工具
 *
 * @author zackzhou
 * @date 2018/12/17,10:52
 */

public class BreakpointHelper {

    private BreakpointHelper() {

    }

    // 查询音频断点记录
    public static Breakpoint findBreakpointByAudio(int sid, long id) {
        BreakpointDao breakpointDao = DBUtils.getDatabase(GlobalContext.get()).getBreakpointDao();
        return breakpointDao.findByAudio(id, sid);
    }

    // 查找没有播放过的音频
    public static AudioV5 findNotPlayAudio(List<AudioV5> audioList) {
        BreakpointDao breakpointDao = DBUtils.getDatabase(GlobalContext.get()).getBreakpointDao();
        for (AudioV5 audio : audioList) {
            Breakpoint breakpoint = breakpointDao.findByAudio(audio.id, audio.sid);
            if (breakpoint == null) {
                return audio;
            }
        }
        return null;
    }

    // 查找没有播完完整的音频
    public static AudioV5 findNotPlayCompleteAudio(List<AudioV5> audioList) {
        BreakpointDao breakpointDao = DBUtils.getDatabase(GlobalContext.get()).getBreakpointDao();
        for (AudioV5 audio : audioList) {
            Breakpoint breakpoint = breakpointDao.findByAudio(audio.id, audio.sid);
            if (breakpoint == null || breakpoint.position != 0) {
                return audio;
            }
        }
        return null;
    }

    public static AudioV5 findLastAudio(@NonNull Album album) {
        BreakpointDao breakpointDao = DBUtils.getDatabase(GlobalContext.get()).getBreakpointDao();
//        breakpointDao.findByAlbum()
        return null;
    }

    // 保存断点数据
    public static void saveBreakpoint(@Nullable Album album, @NonNull AudioV5 audio, long position, long duration, boolean playEnd) {
        if (position == 0 && duration == 0 && !playEnd) {
            return;
        }
        if (audio == null || AudioUtils.isSong(audio.sid)) {
            return;
        }
        AppLogic.runOnBackGround(() -> {
            Logger.d("Music:DB:", "save breakpoint " + audio.name + " " + position + "/" + duration + ", end=" + playEnd);
            BreakpointDao breakpointDao = DBUtils.getDatabase(GlobalContext.get()).getBreakpointDao();
            Breakpoint breakpoint = breakpointDao.findByAudio(audio.id, audio.sid);
            if (breakpoint == null) {
                breakpoint = new Breakpoint();
                breakpoint.id = audio.id;
                breakpoint.sid = audio.sid;
                breakpoint.duration = duration;
            }
            breakpoint.position = position;
            if (album != null) {
                breakpoint.albumId = album.sid + "-" + album.id;
            }
            if (playEnd) {
                breakpoint.playEndCount++;
            }
            breakpointDao.saveOrUpdate(breakpoint);
            if (playEnd) {
                audio.progress = 100;
            } else {
                audio.progress = (int) (position * 1f / duration * 100 + 0.5f);
            }
            Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_BREAK_POINT_UPDATE)
                    .bundle(Constant.BreakpointConstant.KEY_AUDIO, audio)
                    .bundle(Constant.BreakpointConstant.KEY_POSITION, position)
                    .bundle(Constant.BreakpointConstant.KEY_DURATION, duration)
                    .bundle(Constant.BreakpointConstant.KEY_PLAY_END, playEnd)
                    .build());
        });
    }
}
