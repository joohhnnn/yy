package com.txznet.txzsetting.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.txznet.sdk.TXZTtsManager;
import com.txznet.txzsetting.R;
import com.txznet.txzsetting.TXZApplication;
import com.txznet.txzsetting.activity.MainActivity;
import com.txznet.txzsetting.data.SettingData;
import com.txznet.txzsetting.util.SPThreshholdUtil;

/**
 * Created by ASUS User on 2017/6/3.
 */

public class DialogTtsRole extends Dialog {
    public static final String TAG = DialogTtsRole.class.getSimpleName();
    private SettingData mSettingData = null;
    private Context mContext;


    private CheckBox mCheckboxdefault, mCheckbox10003, mCheckbox10006;
    private TextView mCheckboxDefaultShow, mCheckbox10003Show, mCheckbox10006Show;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT.WHAT_CHECK_BOX_DEFAULT:
                    setItemChekBoxTrue(mCheckboxdefault);
                    break;
                case WHAT.WHAT_CHECK_BOX_10003:
                    setItemChekBoxTrue(mCheckbox10003);
                    break;
                case WHAT.WHAT_CHECK_BOX_10006:
                    setItemChekBoxTrue(mCheckbox10006);
                    break;
            }
        }

    };


    public interface DialogCheckBoxListenerTts {
        public void refreshDialogCheckBoxUITts(int tts);
    }

    private DialogCheckBoxListenerTts listener;

    public DialogTtsRole(Context context, int themeResId, DialogCheckBoxListenerTts listener) {
        super(context, themeResId);
        this.mContext = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.ttsrole_show);
        init();
    }

    @Override
    public void show() {
        super.show();
    }

    private static interface WHAT {
        int WHAT_CHECK_BOX_DEFAULT = 0x0001;
        int WHAT_CHECK_BOX_10003 = 0x0002;
        int WHAT_CHECK_BOX_10006 = 0x0003;
    }


    private void init() {
        mSettingData = MainActivity.getSettingData();
        mCheckboxDefaultShow = (TextView) findViewById(R.id.ttsrole_dialog_checkbox_default_show);
        mCheckboxDefaultShow.setOnClickListener(mCheckboxDefaultListene);
        mCheckboxdefault = (CheckBox) findViewById(R.id.ttsrole_dialog_checkbox_default);
        mCheckboxdefault.setOnClickListener(mCheckboxDefaultListene);

        mCheckbox10003Show = (TextView) findViewById(R.id.ttsrole_dialog_checkbox_10003_show);
        mCheckbox10003Show.setOnClickListener(mCheckbox10003Listene);
        mCheckbox10003 = (CheckBox) findViewById(R.id.ttsrole_dialog_checkbox_10003);
        mCheckbox10003.setOnClickListener(mCheckbox10003Listene);


        mCheckbox10006Show = (TextView) findViewById(R.id.ttsrole_dialog_checkbox_10006_show);
        mCheckbox10006Show.setOnClickListener(mCheckbox10006Listene);
        mCheckbox10006 = (CheckBox) findViewById(R.id.ttsrole_dialog_checkbox_10006);
        mCheckbox10006.setOnClickListener(mCheckbox10006Listene);


        int ttsRoleData = SPThreshholdUtil.getTtsRoleData(TXZApplication.getApp());

        Log.d(TAG, "ttsRoleData = " + ttsRoleData);

        switch (ttsRoleData) {
            case SPThreshholdUtil.TTS_ROLE_DEFAULT:
                setCheckdTrue(mCheckboxdefault);
                break;
            case SPThreshholdUtil.TTS_ROLE_10006:
                setCheckdTrue(mCheckbox10006);
                break;
            case SPThreshholdUtil.TTS_ROLE_10003:
                setCheckdTrue(mCheckbox10003);
                break;
            default:
                Log.d(TAG, "拿到的不是荣威RX5的TTS");
                break;
        }
    }


    private View.OnClickListener mCheckboxDefaultListene = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            mHandler.obtainMessage(WHAT.WHAT_CHECK_BOX_DEFAULT).sendToTarget();
        }
    };
    private View.OnClickListener mCheckbox10003Listene = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            mHandler.obtainMessage(WHAT.WHAT_CHECK_BOX_10003).sendToTarget();
        }
    };
    private View.OnClickListener mCheckbox10006Listene = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            mHandler.obtainMessage(WHAT.WHAT_CHECK_BOX_10006).sendToTarget();
        }
    };

    private void setItemChekBoxTrue(CheckBox chekBoxTrue) {
        mSettingData = MainActivity.getSettingData();
        if (mSettingData == null) {
            Log.d(TAG, "SettingData = " + mSettingData);
            return;
        }

        TXZTtsManager.TtsTheme ttsTheme = new TXZTtsManager.TtsTheme();
        switch (chekBoxTrue.getId()) {
            case R.id.ttsrole_dialog_checkbox_10003:
                Log.d(TAG, "ttsrole_dialog_checkbox_10003");
                setCheckdTrue(mCheckbox10003);
                listener.refreshDialogCheckBoxUITts(SPThreshholdUtil.TTS_ROLE_10003);
                SPThreshholdUtil.setSharedPreferencesData(TXZApplication.getApp(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_TTSROLE, SPThreshholdUtil.TTS_ROLE_10003);
                ttsTheme.mThemeId = 10003;
                ttsTheme.mThemeName = "国语女声";
                break;
            case R.id.ttsrole_dialog_checkbox_default:
                Log.d(TAG, "ttsrole_dialog_checkbox_default");
                setCheckdTrue(mCheckboxdefault);
                listener.refreshDialogCheckBoxUITts(SPThreshholdUtil.TTS_ROLE_DEFAULT);
                SPThreshholdUtil.setSharedPreferencesData(TXZApplication.getApp(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_TTSROLE, SPThreshholdUtil.TTS_ROLE_DEFAULT);
                ttsTheme.mThemeId = 1;
                ttsTheme.mThemeName = "默认主题";
                break;
            case R.id.ttsrole_dialog_checkbox_10006:
                Log.d(TAG, "ttsrole_dialog_checkbox_10006");
                setCheckdTrue(mCheckbox10006);
                listener.refreshDialogCheckBoxUITts(SPThreshholdUtil.TTS_ROLE_10006);
                SPThreshholdUtil.setSharedPreferencesData(TXZApplication.getApp(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_TTSROLE, SPThreshholdUtil.TTS_ROLE_10006);
                ttsTheme.mThemeId = 10006;
                ttsTheme.mThemeName = "国语男声";
                break;
        }
        TXZTtsManager.getInstance().setTtsThmeme(ttsTheme);
        dismiss();
    }


    /**
     * 设置当前哪个CheckBox为true
     *
     * @param checkdTrue
     */
    private void setCheckdTrue(CheckBox checkdTrue) {
        mCheckbox10006.setChecked(false);
        mCheckboxdefault.setChecked(false);
        mCheckbox10003.setChecked(false);
        checkdTrue.setChecked(true);
    }
}
