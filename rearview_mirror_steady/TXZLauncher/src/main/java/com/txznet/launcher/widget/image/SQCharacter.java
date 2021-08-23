package com.txznet.launcher.widget.image;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.UiThread;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.txz.RecordWinManager;
import com.txznet.launcher.utils.AnimUtils;
import com.txznet.launcher.utils.IAnim;
import com.txznet.launcher.widget.IStateHandler;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SQCharacter extends FrameLayout implements IStateHandler {
    private static final int ANIM_SPEAK = R.array.anim_list_bofang;
    private static final int ANIM_WAIT = R.array.anim_list_jiazai;
    private static final int ANIM_SHOW_AND_SPEAK = R.array.anim_list_chongfubofang;
    private static final int DEFAULT_DURATION = 83;

    @Bind(R.id.iv_light)
    @Nullable
    ImageView ivLight;
    @Bind(R.id.iv_character)
    ImageView ivCharacter;
    @Bind(R.id.v_wave_lhs)
    WaveView vWaveLhs;
    @Bind(R.id.v_wave_rhs)
    WaveView vWaveRhs;

    private long mShowHandDuration;
    private long mUnShowHandDuration;

    public SQCharacter(@NonNull Context context) {
        super(context);
        initialView();
    }

    public SQCharacter(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialView();
    }

    public SQCharacter(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialView();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SQCharacter(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initialView();
    }

    private void initialView() {
        setClipChildren(false);

        LayoutInflater.from(getContext()).inflate(R.layout.sq_image_character, this, true);
        ButterKnife.bind(this, this);

        onIdle();
    }

    public void littleMode() {
        enableFullAction = false;
        setClipChildren(true);

        removeAllViews();
        LayoutInflater.from(getContext()).inflate(R.layout.sq_image_character_little, this, true);
        ButterKnife.bind(this, this);
        onIdle();
    }

    private IAnim mAnim;
    private int mLastState = -1;
    private boolean enableFullAction = true;

    public void setEnableFullAction(boolean enableFullAction) {
        this.enableFullAction = enableFullAction;
    }

    // tts是否在播报中
    private boolean isTtsBusy = false;

    @UiThread
    @Override
    public void updateState(int state) {
        LogUtil.logi("updateState: state=" + state);
        if (mLastState == state) {
            return;
        }
        /*
         * 远峰系统 BUG2018080909459：
         * 经常小欧说话时嘴巴不动，这是因为在收到STATE_TTS_START的同时收到多个STATE_NORMAL，也就是在onSpeak执行后马上执行了onIdle将小欧说话动作打断了。
         * 暂时特殊处理:在speak的时候忽略STATE_NORMAL的状态。
         * 日志大致如下：
         * 08-23 09:48:19.173 4099-4099/com.txznet.launcher E/TXZ: [(SQCharacter.java:95)#updateState] updateState: state=0
         * 08-23 09:48:19.219 4099-4099/com.txznet.launcher E/TXZ: [(SQCharacter.java:95)#updateState] updateState: state=0
         * 08-23 09:48:19.285 4099-4099/com.txznet.launcher E/TXZ: [(SQCharacter.java:95)#updateState] updateState: state=0
         * 08-23 09:48:19.288 4099-4099/com.txznet.launcher E/TXZ: [(SQCharacter.java:95)#updateState] updateState: state=0
         * 08-23 09:48:19.341 4099-4099/com.txznet.launcher E/TXZ: [(SQCharacter.java:95)#updateState] updateState: state=0
         * 08-23 09:48:19.345 4099-4099/com.txznet.launcher E/TXZ: [(SQCharacter.java:95)#updateState] updateState: state=3
         * 08-23 09:48:19.371 4099-4099/com.txznet.launcher E/TXZ: [(SQCharacter.java:95)#updateState] updateState: state=0
         * 08-23 09:48:19.462 4099-4099/com.txznet.launcher E/TXZ: [(SQCharacter.java:95)#updateState] updateState: state=0
         * 08-23 09:48:19.500 4099-4099/com.txznet.launcher E/TXZ: [(SQCharacter.java:95)#updateState] updateState: state=0
         */
        if (state == STATE_NORMAL && mLastState == STATE_TTS_START) {
            return;
        }
        if (mAnim != null) {
            mAnim.release();
            mAnim = null;
        }
        switch (state) {
            case STATE_NORMAL:
                if (isTtsBusy) {// 如果是tts中，让小欧说话
                    onSpeak();
                } else {
                    onIdle();
                }
                break;
            case STATE_RECORD_START:
                onListen();
                break;
            case STATE_RECORD_END:
                if (!RecordWinManager.getInstance().isRecordWinClosed()) {
                    onWait();
                }
                break;
            case STATE_TTS_START:
                isTtsBusy = true;
                onSpeak();
                break;
            case STATE_TTS_END:
                isTtsBusy = false;
                onIdle();
                break;
        }
        mLastState = state;
    }

    private boolean isAlreadyListModule;

    public void onIdle() {
        vWaveLhs.stop();
        vWaveRhs.stop();
        ivCharacter.setImageResource(R.drawable.jingmo_00000);
    }

    public void onSpeak() {
        vWaveLhs.stop();
        vWaveRhs.stop();
        if (enableFullAction && LaunchManager.getInstance().isActiveModule(LaunchManager.ViewModuleType.TYPE_CHAT_LIST)) {
            if (isAlreadyListModule) {
                mAnim = AnimUtils.load(ivCharacter, ANIM_SPEAK, DEFAULT_DURATION, true);
            } else {
                mAnim = AnimUtils.load(ivCharacter, ANIM_SHOW_AND_SPEAK, DEFAULT_DURATION, true);
            }
            isAlreadyListModule = true;
        } else {
            mAnim = AnimUtils.load(ivCharacter, ANIM_SPEAK, DEFAULT_DURATION, true);
            isAlreadyListModule = false;
        }
    }

    public void onWait() {
        vWaveLhs.stop();
        vWaveRhs.stop();
        mAnim = AnimUtils.load(ivCharacter, ANIM_WAIT, DEFAULT_DURATION, true);
    }

    public void onListen() {
        vWaveLhs.play();
        vWaveRhs.play();
    }
}





