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
import android.widget.Toast;

import com.txznet.txzsetting.R;
import com.txznet.txzsetting.TXZApplication;
import com.txznet.txzsetting.data.SettingData;
import com.txznet.txzsetting.util.TextUtil;
import com.txznet.txzsetting.util.ToastUtil;
import com.txznet.txzsetting.util.TxzReportUtil;

public class SetWelcomeActivity extends Activity {
    public static final String TAG = "nickhu";
    public static final int RESULT_CODE_SET_WELCOME_ACTIVITY = 0;

    private EditText mSetWelcomeText;
    private Button mSetWelcomeBtn, mSetWelcomeReturn, mSetWelcomeDefault;

    private String mBeforeTextChanged = "";
    private String mOnTextChanged = "";
    private String mWelcomeText;
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
            Log.d(TAG, "mSettingDataUser222222222");
            if (mSettingDataUser == null) {
                Intent intent = SetWelcomeActivity.this.getIntent();
                mSettingDataUser = (SettingData) intent.getSerializableExtra("settingdata");
            }
            switch (msg.what) {
                case EDIT_TEXT_SET.WHAT_INIT_UI:
                    Log.d(TAG, "mSettingDataUser = " + mSettingDataUser.getWelcomeTest());
                    mSetWelcomeText.setText(mSettingDataUser.getWelcomeTest());
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_welcome);
        TXZApplication.getApp().addActivity(this);
        init();
        handler.sendEmptyMessage(SetWelcomeActivity.EDIT_TEXT_SET.WHAT_INIT_UI);
    }


    private void init() {

        mSetWelcomeDefault = (Button) findViewById(R.id.setting_welcome_default);
        mSetWelcomeDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtil.showTips(getResources().getString(R.string.setting_welcome_default));
                TxzReportUtil.doReportWelcome(TxzReportUtil.WELCOME_DEFAULT);
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("welcometext", "默认");
                intent.putExtras(bundle);
                setResult(RESULT_CODE_SET_WELCOME_ACTIVITY, intent);
                SetWelcomeActivity.this.finish();
//                Toast.makeText(SetWelcomeActivity.this, getResources().getString(R.string.setting_welcome_default), Toast.LENGTH_LONG).show();
//                TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_ASR_START_HINT", new String[]{"我在呢", "您好", "哈喽", "乐意为您效劳", "需要帮忙吗", "有什么可以帮您"});
            }
        });

        mSetWelcomeText = (EditText) findViewById(R.id.welcome_text_set);
        mSetWelcomeText.addTextChangedListener(new TextWatcher() {
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
                String textEditable = editable.toString();
                String textFirst = "";
                String textLast = "";
                String str = TextUtil.isOkText(textEditable);
                if (!textEditable.equals(str)) {//判断是否合法
//                    mSetWelcomeText.setText(str);
//                    mSetWelcomeText.setSelection(str.length());
                    mEditTextSet = EDIT_TEXT_SET.TEXT_SET_ERROR_HAS_ILLEGAL;
//                    ToastUtil.showTips(getResources().getString(R.string.setting_welcome_has_illegal));
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
//                        mSetWelcomeText.setText(textEditable.substring(1));
//                        ToastUtil.showTips(getResources().getString(R.string.setting_welcome_first_illegal));
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

        mSetWelcomeReturn = (Button) findViewById(R.id.actionbar_return_setting);
        mSetWelcomeReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                getEqualsText("测试文字", "测文字");
                onBackPressed();
            }
        });

        mSetWelcomeBtn = (Button) findViewById(R.id.welcome_btn_set);
        mSetWelcomeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWelcomeText = mSetWelcomeText.getText().toString();
                if (mWelcomeText.equals("") || mWelcomeText == null) {
                    mEditTextSet = EDIT_TEXT_SET.TEXT_SET_OK;
//                    Toast.makeText(SetWelcomeActivity.this, getResources().getString(R.string.setting_welcome_not_custom), Toast.LENGTH_LONG).show();
                }
                if (mWelcomeText.contains("_")) {
                    mEditTextSet = EDIT_TEXT_SET.TEXT_SET_ERROR_HAS_ILLEGAL;
                }
                Log.d(TAG, "启用欢迎语::" + mEditTextSet);
                switch (mEditTextSet) {
                    case EDIT_TEXT_SET.TEXT_SET_OK:
                        Log.d("nickhu", "mSetWelcomeBtn onclick = " + mWelcomeText);
                        TxzReportUtil.doReportWelcome(mWelcomeText);
                        mSettingDataUser.setWelcomeTest(mWelcomeText);
                        Intent intent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("welcometext", mWelcomeText);
                        intent.putExtras(bundle);
                        setResult(RESULT_CODE_SET_WELCOME_ACTIVITY, intent);
                        SetWelcomeActivity.this.finish();
                        break;
                    case EDIT_TEXT_SET.TEXT_SET_ERROR_FIRST_ILLEGAL:
                        ToastUtil.showTips(getResources().getString(R.string.setting_welcome_first_illegal));
//                       Toast.makeText(SetWelcomeActivity.this, getResources().getString(R.string.setting_welcome_first_illegal), Toast.LENGTH_LONG);
                        break;
                    case EDIT_TEXT_SET.TEXT_SET_ERROR_HAS_ILLEGAL:
                        ToastUtil.showTips(getResources().getString(R.string.setting_welcome_has_illegal));
//                        Toast.makeText(SetWelcomeActivity.this, getResources().getString(R.string.setting_welcome_has_illegal), Toast.LENGTH_LONG).show();
                        break;
                }
            }
        });


    }

    @Override
    public void onBackPressed() {
        String welcometext = mSettingDataUser.getWelcomeTest();
        Log.d(TAG, "welcome text = " + welcometext);
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString("welcometext", welcometext);
        intent.putExtras(bundle);
        setResult(RESULT_CODE_SET_WELCOME_ACTIVITY, intent);
        super.onBackPressed();
    }

}
