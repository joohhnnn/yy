package com.txznet.webchat.actions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZMediaFocusManager;
import com.txznet.webchat.dispatcher.Dispatcher;

/**
 * Created by J on 2016/5/31.
 */
public class MediaFocusActionCreator {
    private static MediaFocusActionCreator sInstance;

    public static MediaFocusActionCreator getInstance(){
        if(null == sInstance){
            synchronized (MediaFocusActionCreator.class){
                if(null == sInstance){
                    sInstance = new MediaFocusActionCreator();
                }
            }
        }

        return sInstance;
    }

    BroadcastReceiver mMediaFocusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(TXZMediaFocusManager.INTENT_FOCUS_GAINED.equals(intent.getAction())){
                Dispatcher.get().dispatch(new Action<>(ActionType.WX_MEDIA_FOCUS_CHANGED, true));
            }else if(TXZMediaFocusManager.INTENT_FOCUS_RELEASED.equals(intent.getAction())){
                AppLogic.runOnBackGround(new Runnable() {
                    @Override
                    public void run() {
                        Dispatcher.get().dispatch(new Action<>(ActionType.WX_MEDIA_FOCUS_CHANGED, false));
                    }
                }, 500);

            }
        }
    };

    private MediaFocusActionCreator(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(TXZMediaFocusManager.INTENT_FOCUS_GAINED);
        filter.addAction(TXZMediaFocusManager.INTENT_FOCUS_RELEASED);
        GlobalContext.get().registerReceiver(mMediaFocusReceiver, filter);
    }

}
