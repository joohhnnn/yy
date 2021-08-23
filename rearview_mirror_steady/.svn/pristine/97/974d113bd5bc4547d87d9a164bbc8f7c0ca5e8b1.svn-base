package com.txznet.music.action;

import android.util.Log;

import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;

/**
 * @author telen
 * @date 2018/12/13,15:01
 */
public class LyricActionCreator {

    /**
     * 单例对象
     */
    private volatile static LyricActionCreator singleton;

    private LyricActionCreator() {
    }

    public static LyricActionCreator getInstance() {
        if (singleton == null) {
            synchronized (LyricActionCreator.class) {
                if (singleton == null) {
                    singleton = new LyricActionCreator();
                }
            }
        }
        return singleton;
    }

    /**
     * 获取歌词
     *
     * @param operation 操作来源
     * @param audioV5   需要获取歌词的歌曲
     */
    public void getLyric(Operation operation, AudioV5 audioV5) {
        if (audioV5 == null) {
            if (BuildConfig.DEBUG) {
                Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",getLyric:audio is null");
            }

            return;
        }
        Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_LYRIC_GET).operation(operation).bundle(Constant.LyricConstant.KEY_LYRIC_AUDIO, audioV5).build());
    }
}
