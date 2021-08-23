package com.txznet.music.ui;

import android.content.ComponentName;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by telenewbie on 2017/7/13.
 */

public class InfoPopView implements Observer {
    private static final String TAG = "music:observer:InfoPopView:";
    //##创建一个单例类##
    private volatile static InfoPopView singleton;

    private InfoPopView() {
    }

    public static InfoPopView getInstance() {
        if (singleton == null) {
            synchronized (InfoPopView.class) {
                if (singleton == null) {
                    singleton = new InfoPopView();
                }
            }
        }
        return singleton;
    }

    @Override
    public void update(Observable o, Object data) {
        if (data instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) data;
            LogUtil.logd(TAG + "" + info.getType());
            switch (info.getType()) {
                case InfoMessage.PLAYER_CURRENT_AUDIO:
                    ComponentName topActivity = Utils.getTopActivity();
                    if (topActivity == null || (PlayInfoManager.getInstance().getCurrentAudio() != null && !TextUtils.equals(topActivity.getPackageName(), GlobalContext.get().getPackageName())))
                        ToastUtils.showShortOnUI("已切换到" + PlayInfoManager.getInstance().getCurrentAudio().getName());
                    break;
            }
        }
    }

    public void addObserver() {
//        LogUtil.logd(TAG + "addObserver");
//        ObserverManage.getObserver().addObserver(this);
    }

    public void removeObserver() {
        LogUtil.logd(TAG + "removeObserver");
        ObserverManage.getObserver().deleteObserver(this);
        singleton = null;
    }
}
