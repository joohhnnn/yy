package com.txznet.txz.component.film;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog.FilmPayResDialog;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.choice.IChoice;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.film.FilmManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

public class MovieWaitingPayQRControl implements IChoice {
    private static MovieWaitingPayQRControl sInstance = new MovieWaitingPayQRControl();

    public static MovieWaitingPayQRControl getInstance() {
        return sInstance;
    }

    private MovieWaitingPayQRControl(){}
    private boolean mHasWakeup = false;
    private static final String TASK_MOVIVE_PAY_CADE = "TASK_MOVIVE_PAY_CADE";

    public void show(final JSONBuilder jsonBuilder) {
        ChoiceManager.getInstance().showMovieWaitingPayQR();
        RecorderWin.showData(jsonBuilder.toString());
        registerWakeupCommand();

    }





    private void registerWakeupCommand() {
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
                return TASK_MOVIVE_PAY_CADE;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                if("FILM_TICKET_CANCEL".equals(type)){
                    clearIsSelecting();
                    FilmManager.getInstance().cancel(command);
                }
            }
        }.addCommand("FILM_TICKET_CANCEL",
                        NativeData.getResStringArray("RS_VOICE_TIPS_FILM_TICKET_CANCEL"));
        WakeupManager.getInstance().useWakeupAsAsr(asrComplexSelectCallback);
        mHasWakeup = true;
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
        if (mHasWakeup) {
            WakeupManager.getInstance().recoverWakeupFromAsr(TASK_MOVIVE_PAY_CADE);
            mHasWakeup = false;
        }
    }
}
