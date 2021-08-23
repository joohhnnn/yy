package com.txznet.music.utils;

import android.app.Activity;

import com.txznet.audio.player.RemoteAudioPlayer;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState.Operation;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;

/**
 * 界面的操作类，支持退出，进入等操作
 *
 * @author ASUS User
 */
public class UIHelper {

    /**
     * 退出播放器
     */
    public static void exit() {
        LogUtil.logd("receive a broascast about exit");
        PlayEngineFactory.getEngine().release(Operation.manual);
        PlayerCommunicationManager.getInstance().sendPlayStatusChanged(PlayerCommunicationManager.STATE_ON_EXIT);
        RemoteAudioPlayer.stopRemotePlayer();//关闭服务
//        ImageFactory.getInstance().clearMemory();
//        ReportHelper.getInstance().sendReportData(Action.ACT_EXIT);
//		PlayEngineFactory.getEngine().closeAllRunnable();
        ActivityStack.getInstance().exit();
        Constant.setIsExit(true);

        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 打开界面
     *
     * @param activity
     */
    public static void open(Activity activity) {

    }

}
