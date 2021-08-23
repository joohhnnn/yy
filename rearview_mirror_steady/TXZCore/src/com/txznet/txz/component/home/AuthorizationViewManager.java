package com.txznet.txz.component.home;

import android.os.SystemClock;
import android.view.View;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.viewfactory.data.AuthorizationViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.AuthorizationView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.choice.IChoice;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.CipherUtil;
import com.txznet.txz.util.runnables.Runnable2;

public class AuthorizationViewManager implements IChoice {
    private static AuthorizationViewManager sAuthorizationViewManager = new AuthorizationViewManager();
    private boolean mHasWakeup = false;
    private boolean mIsSelecting = false;
    private static final String TASK_AUTHORIZATION = "TASK_AUTHORIZATION";
    private static int mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
    private Runnable mDismissRun = new Runnable() {

        @Override
        public void run() {
            unregisterWakeupCommand();
            mIsSelecting = false;
        }
    };

    @Override
    public void showChoices(Object data) {

    }

    @Override
    public boolean isSelecting() {
        return mIsSelecting;
    }

    public void clearIsSelecting() {
        unregisterWakeupCommand();
        mIsSelecting = false;
    }

    public static AuthorizationViewManager getAuthorizationViewManager() {
        return sAuthorizationViewManager;
    }

    private AuthorizationViewManager() {
        RecorderWin.OBSERVABLE.registerObserver(new RecorderWin.StatusObervable.StatusObserver() {

            @Override
            public void onShow() {
            }

            @Override
            public void onDismiss() {
                AppLogic.removeUiGroundCallback(mDismissRun);
                AppLogic.runOnUiGround(mDismissRun, 10);
            }
        });
    }

    public String appendUrlParam(String url) {
        StringBuilder builder = new StringBuilder();
        builder.append(url);
        builder.append("?client_id=txzing");
        builder.append("&response_type=code");
        builder.append("&redirect_uri=http://thirdparty.txzing.com/nlp/fangde/auth_callback/");
        builder.append("&state=" + desState());
        return builder.toString();
    }

    private String desState() {
        Long uid = ProjectCfg.getUid();
        LogUtil.logd("AyAuthor des uid:" + uid);
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("uid", uid);
        jsonBuilder.put("request_id", System.currentTimeMillis());
        jsonBuilder.put("client_id", "txzing");
        jsonBuilder.put("secret", "67995c47-6e4f-4cb5-82a0-a01acaff1622");
        return CipherUtil.enCrypt("txzing666", jsonBuilder.toString());
    }

    public void show(String url) {
        mIsSelecting = true;
        final JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(AuthorizationViewData.KEY_URL, appendUrlParam(url));
        JNIHelper.logd(appendUrlParam(url));
        jsonBuilder.put(AuthorizationViewData.KEY_TITLE, NativeData.getResString("RS_AUTHORIZATION_TITLE"));
        jsonBuilder.put(AuthorizationViewData.KEY_SUB_TITLE, NativeData.getResString("RS_AUTHORIZATION_SUB_TITLE"));
        jsonBuilder.put(AuthorizationViewData.KEY_TIPS, NativeData.getResString("RS_AUTHORIZATION_TIPS"));
        jsonBuilder.put(AuthorizationViewData.KEY_VIEW_TIPS,NativeData.getResString("RS_AUTHORIZATION_VIEW_TIPS"));
        jsonBuilder.put("type", 8);

        /**
         * 填充无用数据，避免空指针异常
         */
        jsonBuilder.put("count", 0);
        jsonBuilder.put("curPage", 0);
        jsonBuilder.put("maxPage", 0);
        AsrManager.getInstance().cancel();
        TtsManager.getInstance().pause();
        TextResultHandle.getInstance().cancel();
        speakText(NativeData.getResString("RS_AUTHORIZATION_HINT"));
		RecorderWin.showData(jsonBuilder.toString());
        registerWakeupCommand();
    }

    private void registerWakeupCommand() {
        AsrUtil.AsrComplexSelectCallback asrComplexSelectCallback = new AsrUtil.AsrComplexSelectCallback() {
            Runnable removeBackground = null;
            Runnable2<Object, String> taskRunnable = null;
            private static final int speechDelay = 700;
            private static final int handleDelay = 800;
            private boolean isEnd = false;
            private long mLastSpeechEndTime = 0;

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
                return TASK_AUTHORIZATION;
            }

            public void onSpeechEnd() {
                mLastSpeechEndTime = SystemClock.elapsedRealtime();
                if (removeBackground != null) {
                    AppLogic.removeBackGroundCallback(removeBackground);
                }
            }

            @Override
            public void onCommandSelected(String type, String command) {
                if (taskRunnable != null) {
                    AppLogic.removeBackGroundCallback(taskRunnable);
                    taskRunnable = null;
                }
                taskRunnable = new Runnable2<Object, String>(type, command) {

                    @Override
                    public void run() {

                        JNIHelper.logd("do onCommandSelected");

                        String type = (String) mP1;
                        String command = mP2;
                        isEnd = true;
                        if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
                            //唤醒结果执行时，如果还在录音，则取消掉
                            if (AsrManager.getInstance().isBusy()) {
                                AsrManager.getInstance().cancel();
                            }
                        }
                        if ("AUTHORIZATION$CANCEL".equals(type)) {
                            unregisterWakeupCommand();
                            RecorderWin.setLastUserText(command);
                            RecorderWin.open(NativeData.getResString("RS_SELECTOR_HELP"));
                            mIsSelecting = false;
                        }
                    }
                };
                removeBackground = new Runnable() {
                    @Override
                    public void run() {
                        AppLogic.removeBackGroundCallback(taskRunnable);
                    }
                };
                if (InterruptTts.getInstance().isInterruptTTS() || ProjectCfg.mCoexistAsrAndWakeup) {
                    if (isWakeupResult()) {//是唤醒的结果
                        isEnd = false;
                        //判断唤醒的说话结束了
                        if (SystemClock.elapsedRealtime() - mLastSpeechEndTime < 300) {
                            AppLogic.runOnBackGround(taskRunnable, 0);
                            AppLogic.removeBackGroundCallback(removeBackground);
                        } else {
                            AppLogic.runOnBackGround(removeBackground, speechDelay);
                            AppLogic.runOnBackGround(taskRunnable, handleDelay);
                        }
                    } else if (!isEnd) {//识别到的唤醒词并且唤醒没有执行完成
                        AppLogic.runOnBackGround(taskRunnable, 0);
                        AppLogic.removeBackGroundCallback(removeBackground);
                    }
                } else {
                    taskRunnable.run();
                }
            }
        }.addCommand("AUTHORIZATION$CANCEL",
                NativeData.getResStringArray("RS_AUTHORIZATION_CANCEL"));
        WakeupManager.getInstance().useWakeupAsAsr(asrComplexSelectCallback);
        mHasWakeup = true;
    }

    private void speakText(String spk) {
        TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
        mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
        mSpeechTaskId = TtsManager.getInstance().speakText(spk, new TtsUtil.ITtsCallback() {
            @Override
            public boolean isNeedStartAsr() {
                return true;
            }
        });
    }

    public void close() {
        unregisterWakeupCommand();
        RecorderWin.close();
        mIsSelecting = false;
    }

    private void unregisterWakeupCommand() {
        if (mHasWakeup) {
            TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
            WakeupManager.getInstance().recoverWakeupFromAsr(TASK_AUTHORIZATION);
            mHasWakeup = false;
        }
    }
}
