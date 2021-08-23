package com.txznet.music.service;

import android.util.Log;

import com.txznet.audio.player.IMediaPlayer;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.music.Constant;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.helper.PushLogicHelper;
import com.txznet.music.helper.SyncCoreData;
import com.txznet.music.power.PowerManager;
import com.txznet.music.service.impl.ExtraCommand;
import com.txznet.music.service.impl.MusicCommand;
import com.txznet.music.service.impl.NetCommand;
import com.txznet.music.service.impl.ProgramCommand;
import com.txznet.music.service.impl.PushCommand;
import com.txznet.music.service.impl.RadioCommand;
import com.txznet.music.service.impl.SearchCommand;
import com.txznet.music.service.impl.SyncCommand;
import com.txznet.music.util.Logger;
import com.txznet.sdk.bean.LocationData;

import java.util.ArrayList;
import java.util.List;

/**
 * 同行者aidl传输核心Service
 */
public class MyService extends CoreService {
    public static final String TAG = Constant.LOG_TAG_SERVICE;

    private static List<ICommand> MUSIC_CMDS = new ArrayList<>();

    static {
        //内存引用
        MUSIC_CMDS.add(new SearchCommand());
        MUSIC_CMDS.add(new MusicCommand());
        MUSIC_CMDS.add(NetCommand.getInstance());
        MUSIC_CMDS.add(new SyncCommand());
        MUSIC_CMDS.add(PushCommand.getInstance());
        MUSIC_CMDS.add(new ExtraCommand());
        MUSIC_CMDS.add(ProgramCommand.getInstance());
    }

    private static RadioCommand RADIO_CMDS = new RadioCommand();

    @Override
    public IEngineCallBack getCallback() {
        return new IEngineCallBack() {
            @Override
            public byte[] deviceSleep() {
                PowerManager.getInstance().notifySleep();
                return new byte[0];
            }

            @Override
            public byte[] deviceWakeUp() {
                PowerManager.getInstance().notifyWakeUp();
                return new byte[0];
            }

            @Override
            public byte[] clientExit() {
                PowerManager.getInstance().notifyExit();
                return new byte[0];
            }

            @Override
            public byte[] clientBackCarOn() {
                PowerManager.getInstance().notifyReverseStart();
                return new byte[0];
            }

            @Override
            public byte[] clientBackCarOff() {
                PowerManager.getInstance().notifyReverseEnd();
                return new byte[0];
            }

            @Override
            public byte[] soundInitSuccess() {
                //同步给Core当前播放信息
                AudioV5 currPlay = PlayHelper.get().getCurrAudio();
                SyncCoreData.syncCurMusicModel(currPlay);
                SyncCoreData.syncCurPlayerStatus(PlayHelper.get().isPlaying());

                //开启倒计时
                Logger.d(Constant.LOG_TAG_PUSH, "init success, isOpenPush:" + SharedPreferencesUtils.isOpenPush() + ", isShowViewExecutable:" + PushLogicHelper.getInstance().isShowViewExecutable());
                if (SharedPreferencesUtils.isOpenPush() && !PushLogicHelper.getInstance().isShowViewExecutable()) {
                    PushLogicHelper.getInstance().setShowViewExecutable(true);
                    //将标志位重置，未弹出过界面
                    SharedPreferencesUtils.setShowWindowView(false);
                    PushCommand.getInstance().startShowTask();
                }
                return new byte[0];
            }

            @Override
            public byte[] invokeMusic(String pkgName, String command, byte[] data) {
                return getMusicCmdFactory(command).invoke(pkgName, command, data);
            }

            @Override
            public byte[] invokeAudio(String pkgName, String command, byte[] data) {
                if (RADIO_CMDS.intercept(command)) {
                    RADIO_CMDS.invoke(pkgName, command, data);
                } else {
                    Log.w(Constant.LOG_TAG_SERVICE, "un invoke:" + command + ":" + (data == null ? "" : new String(data)));
                }
                return new byte[0];
            }

            @Override
            public void onLocationUpdate(LocationData data) {

            }

            @Override
            public byte[] onOtherCmd(String pkgName, String command, byte[] data) {
                //网络状态
                if (NetCommand.getInstance().intercept(command)) {
                    NetCommand.getInstance().invoke(pkgName, command, data);
                }
                return new byte[0];
            }
        };
    }

    public ICommand getMusicCmdFactory(final String command) {
        for (ICommand iCommand : MUSIC_CMDS) {
            if (iCommand.intercept(command)) {
                return iCommand;
            }
        }
        return new ICommand() {
            @Override
            public byte[] invoke(String pkgName, String cmd, byte[] data) {
                Log.w(Constant.LOG_TAG_SERVICE, "un invoke:" + cmd + ":" + (data == null ? "" : new String(data)));
                return new byte[0];
            }

            @Override
            public boolean intercept(String cmd) {
                return true;
            }
        };
    }
}
