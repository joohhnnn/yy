package com.txznet.txz.component.film;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.view.RoundImageView;
import com.txznet.comm.ui.viewfactory.data.MovieSeatPlanViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMovieSeatPlanViewData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.loader.AppLogic;
import com.txznet.record.helper.ChatMsgFactory;
import com.txznet.record.ui.WinRecord;
import com.txznet.txz.component.choice.IChoice;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.film.FilmManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable1;

public class MovieSeatPlanControl implements IChoice {

    private boolean mHasWakeup = false;
    private static final String TASK_MOVIVE_SEAT_PLAN = "TASK_MOVIVE_SEAT_PLAN";

    private static MovieSeatPlanControl sInstance = new MovieSeatPlanControl();

    public static MovieSeatPlanControl getInstance() {
        return sInstance;
    }

    private MovieSeatPlanControl(){}

    public void show(final JSONBuilder jsonBuilder, Long timeOut, int ticketCount) {
        ChoiceManager.getInstance().showMovieSeatPlan();
        RecorderWin.showData(jsonBuilder.toString());
        registerWakeupCommand(ticketCount);
    }

    private LruCache<String, Bitmap> mCachePost = new LruCache<String, Bitmap>(ConfigUtil.getCinemaItemCount());
    private void loadDrawableByUrl(final ImageView ivHead, String uri) {
        Bitmap bitmap = null;
        synchronized (mCachePost) {
            bitmap = mCachePost.get(uri);
        }

        if (bitmap != null) {
            UI2Manager.runOnUIThread(new Runnable1<Bitmap>(bitmap) {

                @Override
                public void run() {
                    ivHead.setImageBitmap(mP1);
                    ivHead.setVisibility(View.VISIBLE);
                }
            }, 0);
            return;
        }

        ImageLoaderInitialize.ImageLoaderImpl.getInstance().displayImage(uri,ivHead, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
                ((ImageView) view).setImageDrawable(LayouUtil.getDrawable("def_moive"));
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                if (loadedImage != null) {
                    ((ImageView) view).setImageBitmap(loadedImage);
                    view.setVisibility(View.VISIBLE);
                    synchronized (mCachePost) {
                        mCachePost.put(imageUri, loadedImage);
                    }
                }
            }
        });
    }


    public Runnable mTimeoutTask = new Runnable() {

        @Override
        public void run() {
            FilmManager.getInstance().cancel(null);
            unregisterWakeupCommand();
            RecorderWin.open("选择超时，还有什么需要");
        }
    };

    private void registerWakeupCommand(int ticketCount) {
        String[] ticketCountWake;
        if(ticketCount >= 2){
            ticketCountWake = new String[ticketCount * 4 + 4];
        }else {
            ticketCountWake = new String[ticketCount * 4];
        }
        int ticketCountIndex = 0;
        for(int i = 1; i <= ticketCount; i++){
            ticketCountWake[ticketCountIndex++] = "我要买"+ NativeData.getResString("RS_VOICE_DIGITS", i)+"张";
            ticketCountWake[ticketCountIndex++] = "要买"+ NativeData.getResString("RS_VOICE_DIGITS", i)+"张";
            ticketCountWake[ticketCountIndex++] = "要"+ NativeData.getResString("RS_VOICE_DIGITS", i)+"张";
            ticketCountWake[ticketCountIndex++] = "买"+ NativeData.getResString("RS_VOICE_DIGITS", i)+"张";
            if(i == 2){
                ticketCountWake[ticketCountIndex++] = "我要买两张";
                ticketCountWake[ticketCountIndex++] = "要买两张";
                ticketCountWake[ticketCountIndex++] = "要两张";
                ticketCountWake[ticketCountIndex++] = "买两张";
            }
        }
        AsrUtil.AsrComplexSelectCallback asrComplexSelectCallback = new AsrUtil.AsrComplexSelectCallback() {

            @Override
            public boolean needAsrState() {
                if (InterruptTts.getInstance().isInterruptTTS()) {//如果是识别模式，就不需要开启beep音
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public String getTaskId() {
                return TASK_MOVIVE_SEAT_PLAN;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                if("TICKET_COUNT".equals(type)){
                    unregisterWakeupCommand();
                    FilmManager.getInstance().requestTxz(command, new FilmManager.RequestCallBack() {
                        @Override
                        public void onResult() {
                            super.onResult();

                        }

                        @Override
                        public void onError() {
                            super.onError();
                        }
                    });
                }
                if("FILM_TICKET_CANCEL".equals(type)){
                    unregisterWakeupCommand();
                    FilmManager.getInstance().cancel(command);
                }
            }
        }.addCommand("TICKET_COUNT", ticketCountWake)
                .addCommand("FILM_TICKET_CANCEL",
                        NativeData.getResStringArray("RS_VOICE_TIPS_FILM_TICKET_CANCEL"));
        WakeupManager.getInstance().useWakeupAsAsr(asrComplexSelectCallback);
        mHasWakeup = true;
    }

    private void unregisterWakeupCommand() {
        if (mHasWakeup) {
            WakeupManager.getInstance().recoverWakeupFromAsr(TASK_MOVIVE_SEAT_PLAN);
            mHasWakeup = false;
            AppLogic.removeBackGroundCallback(mTimeoutTask);
        }
    }


    @Override
    public void showChoices(Object data) {

    }

    @Override
    public boolean isSelecting() {
        return mHasWakeup;
    }

    @Override
    public void clearIsSelecting() {
        unregisterWakeupCommand();
    }
}
