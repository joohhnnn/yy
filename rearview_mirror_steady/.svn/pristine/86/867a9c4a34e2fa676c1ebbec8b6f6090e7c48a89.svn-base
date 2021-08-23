package com.txznet.txzsetting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.txznet.txzsetting.R;
import com.txznet.txzsetting.TXZApplication;
import com.txznet.txzsetting.data.SettingData;
import com.txznet.txzsetting.util.TextUtil;
import com.txznet.txzsetting.util.ToastUtil;
import com.txznet.txzsetting.util.TxzReportUtil;

public class SetUserWakeupNameActivity extends Activity {
    public static final String TAG = "nickhu";
    public static final int RESULT_CODE_SET_USERWAKEUP_ACTIVITY = 11;

    private EditText mSetUserWakeupText;
    private Button mSetUserWakeupBtn, mSetUserWakeupReturn;

    private String mBeforeTextChanged = "";
    private String mOnTextChanged = "";
    private String mUserWakeupText;
    private int mEditTextSet = EDIT_TEXT_SET.TEXT_SET_OK;

    private SettingData mSettingDataUser;
    private SettingData mSettingDataFactory;

    private interface EDIT_TEXT_SET {
        int TEXT_SET_OK = 0x2001;//正常
        int TEXT_SET_ERROR_FIRST_ILLEGAL = 0x2002;//首字母非法
        int TEXT_SET_ERROR_HAS_ILLEGAL = 0x2003;//含非法字符
        int WHAT_INIT_UI = 0x2004;//初始化UI
        int TEXT_SET_ERROR_LENGTH = 0x2005;//长度不符合规范
    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mSettingDataUser == null) {
                Intent intent = SetUserWakeupNameActivity.this.getIntent();
                mSettingDataUser = (SettingData) intent.getSerializableExtra("settingdata");
            }
            switch (msg.what) {
                case EDIT_TEXT_SET.WHAT_INIT_UI:
                    if (mSettingDataUser.getWakeupWords()!=null) {
                        Log.d(TAG, "mSettingDataUser = " + mSettingDataUser.getWakeupWords()[0]);
                        mSetUserWakeupText.setText(mSettingDataUser.getWakeupWords()[0]);
                    }else {
                        mSetUserWakeupText.setText("");
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_user_wakeup_name);
        TXZApplication.getApp().addActivity(this);
        init();
        handler.sendEmptyMessage(SetUserWakeupNameActivity.EDIT_TEXT_SET.WHAT_INIT_UI);
    }

    private void init() {
        mSetUserWakeupText = (EditText) findViewById(R.id.wakeup_user_text_set);
        mSetUserWakeupText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d(TAG, "beforeTextChanged");
                mBeforeTextChanged = charSequence.toString();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mOnTextChanged = charSequence.toString();
                Log.d(TAG, "onTextChanged");
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d(TAG, "afterTextChanged = " + editable.toString() + ",length = " + editable.toString().length());
                String textEditable = editable.toString().trim();
                ;
                String textFirst = "";
                String textLast = "";
                String str = TextUtil.isOkText(textEditable);

                if (textEditable.length() > 0 && textEditable.length() < 4) {
                    mEditTextSet = EDIT_TEXT_SET.TEXT_SET_ERROR_LENGTH;
                    return;
                }

                if (!textEditable.equals(str)) {//判断是否合法
                    mEditTextSet = EDIT_TEXT_SET.TEXT_SET_ERROR_HAS_ILLEGAL;
                    return;
                }
                if (textEditable.length() <= 0) {
                    textFirst = " ";
                    textLast = " ";
                } else {
                    textFirst = textEditable.substring(0, 1);
                    textLast = textEditable.substring(textEditable.length() - 1, textEditable.length());
                    String fristStr = TextUtil.isFristOkText(textFirst);
                    if (!textFirst.equals(fristStr)) {
                        mEditTextSet = EDIT_TEXT_SET.TEXT_SET_ERROR_FIRST_ILLEGAL;
                    }
                }
                Log.d(TAG, "当前输入的最后一个字是：" + textLast + ",第一个字是：" + textFirst);
                if (TextUtil.hasChinese(textFirst) || TextUtil.hasEnglish(textFirst) || TextUtil.hasNumber(textFirst)) {
                    Log.d(TAG, "首字符正确");
                    mEditTextSet = EDIT_TEXT_SET.TEXT_SET_OK;
                } else {
                    Log.d(TAG, "首字符不可以是标点符号");
                    mEditTextSet = EDIT_TEXT_SET.TEXT_SET_ERROR_FIRST_ILLEGAL;
                }
            }
        });

        mSetUserWakeupReturn = (Button) findViewById(R.id.actionbar_return_setting);
        mSetUserWakeupReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        mSetUserWakeupBtn = (Button) findViewById(R.id.wakeup_user_btn_set);
        mSetUserWakeupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUserWakeupText = mSetUserWakeupText.getText().toString();
                if (mUserWakeupText.equals("") || mUserWakeupText == null) {
                    mEditTextSet = EDIT_TEXT_SET.TEXT_SET_OK;
                }
                if (mUserWakeupText.contains("_")) {
                    mEditTextSet = EDIT_TEXT_SET.TEXT_SET_ERROR_HAS_ILLEGAL;
                }
                Log.d(TAG, "启用唤醒词::" + mEditTextSet);
                switch (mEditTextSet) {
                    case EDIT_TEXT_SET.TEXT_SET_OK:
                        Log.d("nickhu", "mSetWelcomeBtn onclick = " + mUserWakeupText);
                        TxzReportUtil.doReportWakeup(mUserWakeupText);
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("wakeuptext", mUserWakeupText);
                        intent.putExtras(bundle);
                        setResult(RESULT_CODE_SET_USERWAKEUP_ACTIVITY, intent);
                        SetUserWakeupNameActivity.this.finish();
                        break;
                    case EDIT_TEXT_SET.TEXT_SET_ERROR_FIRST_ILLEGAL:
                        ToastUtil.showTips(getResources().getString(R.string.setting_welcome_first_illegal));
                        break;
                    case EDIT_TEXT_SET.TEXT_SET_ERROR_HAS_ILLEGAL:
                        ToastUtil.showTips(getResources().getString(R.string.setting_welcome_has_illegal));
                        break;
                    case EDIT_TEXT_SET.TEXT_SET_ERROR_LENGTH:
                        ToastUtil.showTips(getResources().getString(R.string.wakeup_text_explain));
                        break;
                }
            }
        });


    }
}
