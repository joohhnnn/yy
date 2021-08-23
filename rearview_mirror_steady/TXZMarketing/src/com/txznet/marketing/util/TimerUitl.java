package com.txznet.marketing.util;

import android.os.Handler;
import android.text.Html;
import android.util.Log;

import com.txznet.marketing.MainActivity;
import com.txznet.marketing.TipDialog;
import com.txznet.marketing.ui.MediaPlayerSurfaceView;
import com.txznet.sdk.TXZTtsManager;


public class TimerUitl {

    private static TimerUitl instance = null;

    public static TimerUitl getInstance() {
        if (instance == null){
            instance = new TimerUitl();
        }
        return instance;
    }

    private Handler mHander = new Handler();
    String str = "";
    String text = "";
    int taskId = 0;

    //修改提示语颜色为红色，并再次播报
    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            text = "您还没有说"+str+",请说"+ str;
            Log.d("jack", "run: ");
            TipDialog.getInstance(MainActivity.getInstance()).setColor("#FF0000");
            taskId = TXZTtsManager.getInstance().speakText(text);
        }
    };

    public void timer(int time,String str){
        this.str = str;
        Log.d("jack", "timer: ");
        mHander.postDelayed(mRunnable,time);
    }

    public void destory(){
        if (taskId != 0){
            //停止当前播报
            TXZTtsManager.getInstance().cancelSpeak(taskId);
            taskId = 0;
        }
        mHander.removeCallbacks(mRunnable);
    }
}
