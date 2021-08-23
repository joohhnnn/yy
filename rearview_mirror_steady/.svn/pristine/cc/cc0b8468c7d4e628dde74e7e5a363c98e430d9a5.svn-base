package com.txznet.music.playerModule.logic.focus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.view.KeyEvent;

import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * Created by telenewbie on 2018/3/30.
 */

public class MyBroadcast extends BroadcastReceiver {
    private static final String TAG = "music:broadcast:";

    /*
     * It should be safe to use static variables here once registered via the
     * AudioManager
     */
    private static long mHeadsetDownTime = 0;
    private static long mHeadsetUpTime = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (HeadSetHelper.getInstance().getIsRegister() && FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MEDIA_BUTTON, 1) == 2) {
            handleInevent(context, intent);
        }
    }

    public static void handleInevent(Context context, Intent intent) {
//获取对应Acton，判断是否是需要的ACTION_MEDIA_BUTTON
        String action = intent.getAction();
        if (!HeadSetHelper.getInstance().getIsRegister()) {
            Logger.d(TAG, "dont need process this action:" + action);
            return;
        } else {
            Logger.d(TAG, "process this action:" + action);
        }


        if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent event = (KeyEvent) intent
                    .getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            if (event == null)
                return;

            if (event.getKeyCode() != KeyEvent.KEYCODE_HEADSETHOOK
                    && event.getKeyCode() != KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                    && event.getAction() != KeyEvent.ACTION_DOWN)
                return;

//            if (event.getAction() != KeyEvent.ACTION_UP) {
//                return;
//            }

            if (!MyFocusListener.playerInFocus) {
                return;
            }

            Logger.d(TAG, "MyBroadcast:receive:keycode:" + event.getKeyCode() + "/" + event.getAction() + "/" + event.getRepeatCount());
            Intent i = null;
            switch (event.getKeyCode()) {
                /*
                 * one click => play/pause long click => previous double click =>
                 * next
                 */
                //这里根据按下的时间和操作，分离出具体的控制
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    long time = SystemClock.uptimeMillis();
                    switch (event.getAction()) {
                        case KeyEvent.ACTION_DOWN:
                            if (event.getRepeatCount() > 0)
                                break;
                            mHeadsetDownTime = time;
                            break;
                        case KeyEvent.ACTION_UP:
                            PlayEngineFactory.getEngine().playOrPause(EnumState.Operation.sound);
                            break;
                    }
                    break;
//下面是常规的播放、暂停、停止、上下曲　
                case KeyEvent.KEYCODE_MEDIA_PLAY:
//                    i = new Intent(AudioService.ACTION_REMOTE_PLAY);
                    PlayEngineFactory.getEngine().play(EnumState.Operation.sound);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
//                    i = new Intent(AudioService.ACTION_REMOTE_PAUSE);
                    PlayEngineFactory.getEngine().pause(EnumState.Operation.sound);
                    break;
                case KeyEvent.KEYCODE_MEDIA_STOP:
//                    i = new Intent(AudioService.ACTION_REMOTE_STOP);
                    PlayEngineFactory.getEngine().release(EnumState.Operation.sound);
                    break;
                case KeyEvent.KEYCODE_MEDIA_NEXT:
//                    i = new Intent(AudioService.ACTION_REMOTE_FORWARD);
                    PlayEngineFactory.getEngine().next(EnumState.Operation.sound);
                    break;
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
//                    i = new Intent(AudioService.ACTION_REMOTE_BACKWARD);
                    PlayEngineFactory.getEngine().last(EnumState.Operation.sound);
                    break;
                default:
                    int next_code = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_KEYCODE_NEXT, -1);
                    if (next_code != -1) {
                        if (event.getKeyCode() == next_code) {
                            PlayEngineFactory.getEngine().next(EnumState.Operation.sound);
                        }
                    }
                    int perv_code = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_KEYCODE_PREV, -1);
                    if (perv_code != -1) {
                        if (event.getKeyCode() == perv_code) {
                            PlayEngineFactory.getEngine().last(EnumState.Operation.sound);
                        }
                    }
                    break;
            }
        }
    }


}
