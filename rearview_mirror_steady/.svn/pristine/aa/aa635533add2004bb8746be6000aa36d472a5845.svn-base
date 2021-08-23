package com.txznet.txzsetting.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.txzsetting.R;
import com.txznet.txzsetting.TXZApplication;
import com.txznet.txzsetting.activity.MainActivity;
import com.txznet.txzsetting.util.CountDownTimer;
import com.txznet.txzsetting.util.FileUtil;
import com.txznet.txzsetting.util.JsonIntentUtil;
import com.txznet.txzsetting.util.ToastUtil;

/**
 * Created by nick on 2017/12/1.
 */

public class DialogHiddenDoor extends Dialog {
    private static final String TAG = DialogHiddenDoor.class.getSimpleName();
    private Context mContext;

    boolean hasLogEnableFile = false;
    boolean hasPcmEnableDebug = false;
    private Button mBtnYes, mBtnNo;
    private TextView mTvOpenLogShow,mTvOpenVoiceShow;
    private CheckSwitchButton mCheckboxOpenLog, mCheckboxOpenVoice;//这里注意了！true是灰色的，false是蓝色的

    private boolean isShowHiddenCanClick = false;//是否可点击确认取消
    private static final int CAN_CLICK_TIME = 1000;//允许dialog弹出后多久可以点击确认取消

    private CountDownTimer countDownTimer = new CountDownTimer(CAN_CLICK_TIME, CAN_CLICK_TIME) {
        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            Log.d(TAG, "时间到");
            isShowHiddenCanClick = true;
        }
    };
    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "msg.what = " + msg.what);

            switch (msg.what) {
                case WHAT.WHAT_LOG_ENABLE_FILE:
                    FileUtil.makeFile(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.LOG_ENABLE_FILE);
                    break;
                case WHAT.WHAT_PCM_ENABLE_DEBUG:
                    FileUtil.makeFile(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.PCM_ENABLE_DEBUG);
                    break;
                case WHAT.WHAT_DELETE_FILE_LOG:
                    FileUtil.deleteFile(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.LOG_ENABLE_FILE);
                    break;
                case WHAT.WHAT_DELETE_FILE_PCM:
                    FileUtil.deleteFile(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.PCM_ENABLE_DEBUG);
                    break;

                case WHAT.WHAT_INIT_UI:
                    hasLogEnableFile = FileUtil.fileIsExists(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.LOG_ENABLE_FILE);
                    hasPcmEnableDebug = FileUtil.fileIsExists(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.PCM_ENABLE_DEBUG);

                    if (hasLogEnableFile) {
                        mCheckboxOpenLog.setChecked(false);//蓝色
                        mTvOpenLogShow.setText(TXZApplication.getApp().getResources().getText(R.string.hidden_door_open_log_ok));
                    } else {
                        mCheckboxOpenLog.setChecked(true);//灰色
                        mTvOpenLogShow.setText(TXZApplication.getApp().getResources().getText(R.string.hidden_door_open_log_show));
                    }
                    if (hasPcmEnableDebug) {
                        mCheckboxOpenVoice.setChecked(false);//蓝色
                        mTvOpenVoiceShow.setText(TXZApplication.getApp().getResources().getText(R.string.hidden_door_open_log_ok));
                    } else {
                        mCheckboxOpenVoice.setChecked(true);//灰色
                        mTvOpenVoiceShow.setText(TXZApplication.getApp().getResources().getText(R.string.hidden_door_open_log_show));
                    }
                    break;
            }
        }

    };

    private interface WHAT {
        int WHAT_LOG_ENABLE_FILE = 0x0001;
        int WHAT_PCM_ENABLE_DEBUG = 0x0002;
        int WHAT_DELETE_FILE_LOG = 0x0003;
        int WHAT_DELETE_FILE_PCM = 0x0004;
        int WHAT_INIT_UI = 0x0005;
    }

    public DialogHiddenDoor(Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.hidden_door_show);
        isShowHiddenCanClick = false;
        init();
    }

    @Override
    protected void onStart() {
        super.onStart();
        countDownTimer.start();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        MainActivity.setEnterHiddenDoor(false);
    }

    private void init() {
        mBtnYes = (Button) findViewById(R.id.hidden_door_yes);
        mBtnYes.setOnClickListener(mOnClickListener);
        mBtnNo = (Button) findViewById(R.id.hidden_door_no);
        mBtnNo.setOnClickListener(mOnClickListener);

        mCheckboxOpenLog = (CheckSwitchButton) findViewById(R.id.checkbox_hidden_door_open_log);
        mTvOpenLogShow = (TextView) findViewById(R.id.textview_hidden_door_open_log_show);

        mCheckboxOpenVoice = (CheckSwitchButton) findViewById(R.id.checkbox_hidden_door_open_voice);
        mTvOpenVoiceShow = (TextView) findViewById(R.id.textview_hidden_door_open_voice_show);

        mHandler.obtainMessage(WHAT.WHAT_INIT_UI).sendToTarget();

    }


    /**
     * 点击事件汇总
     */
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                case R.id.hidden_door_yes:
                    Log.d(TAG, "hidden_door_yes isShowHiddenCanClick = " + isShowHiddenCanClick);
                    if (isShowHiddenCanClick) {
                        if (!mCheckboxOpenLog.isChecked()) {//打开了log
                            mHandler.obtainMessage(WHAT.WHAT_LOG_ENABLE_FILE).sendToTarget();
                        }else {//删除log
                            mHandler.obtainMessage(WHAT.WHAT_DELETE_FILE_LOG).sendToTarget();
                        }
                        if (!mCheckboxOpenVoice.isChecked()) {//打开了voice录音
                            mHandler.obtainMessage(WHAT.WHAT_PCM_ENABLE_DEBUG).sendToTarget();
                        }else {
                            mHandler.obtainMessage(WHAT.WHAT_DELETE_FILE_PCM).sendToTarget();
                        }
                        dismiss();
                    } else {
                        Log.d(TAG, "你点的太快了咯，心脏受不了啦！");
                    }
                    break;

                case R.id.hidden_door_no:
                    if (isShowHiddenCanClick) {
                        dismiss();
                    } else {
                        Log.d(TAG, "你点的太快了咯，心脏受不了啦！");
                    }
                    break;
            }
        }
    };

}
