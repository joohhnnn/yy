package com.txznet.txzsetting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.txzsetting.R;
import com.txznet.txzsetting.TXZApplication;
import com.txznet.txzsetting.adapter.SetWakeupAdapter;
import com.txznet.txzsetting.data.SettingData;
import com.txznet.txzsetting.util.FileUtil;
import com.txznet.txzsetting.util.JsonIntentUtil;
import com.txznet.txzsetting.util.SPThreshholdUtil;
import com.txznet.txzsetting.util.TextUtil;
import com.txznet.txzsetting.util.ToastUtil;
import com.txznet.txzsetting.util.TxzReportUtil;
import com.txznet.txzsetting.view.CheckSwitchButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.List;

public class SetWakupNameActivity extends Activity {
    public static final String TAG = SetWakupNameActivity.class.getSimpleName();
    public static final int RESULT_CODE_SET_WAKEUP_NAME_ACTIVITY = 1;
    public static final int REQUEST_CODE_USER_WAKEUP_SHOW = 11;
    public static final int WAKEUP_NAME_ERROR = 0;
    public static final int WAKEUP_NAME_NULL = 1;
    public static final int WAKEUP_NAME_OK = 2;


    private RelativeLayout mSettingWakeupAddlistLayout;
    private ImageView mSettingWakeupAddlistImage;
    private EditText mAddWakeupNameList;
    private Button mSetWakeupReturn;
    private CheckSwitchButton mCheckBoxWakeup, mCheckBoxWakeupCommand;
    private ListView mWakeupNameList;
    private SettingData mSettingDataUser;
    private SettingData mSettingDataFactory;
    private TextView mWakeupNowText;
    private TextView mShowWakeup;
    private LinearLayout mWakeupCheckboxLayout, mWakeupCommandCheckboxLayout;
    private TextView mSettingWakeupAddlistText;

    private String editableWakeupName;
    private String mShowUserWakeupText = "";


    private String mWakeupName;
    private SetWakeupAdapter adapterWakeup;

    private List<String> mAdapterList = new ArrayList<>();

    private int mEditTextSet = WHAT.TEXT_SET_OK;

    private interface WHAT {
        int TEXT_SET_OK = 0x2001;//??????
        int TEXT_SET_ERROR_FIRST_ILLEGAL = 0x2002;//???????????????
        int TEXT_SET_ERROR_HAS_ILLEGAL = 0x2003;//???????????????
        int WHAT_INIT_UI = 0x2004;
        int WHAT_SET_WAKEUP_NAME_OK = 0x2005;
        int WHAT_SET_WAKEUP_NAME_ERROR = 0x2006;
        int WHAT_SET_WAKEUP_NAME_NULL = 0x2007;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mSettingDataUser == null) {
                Intent intent = SetWakupNameActivity.this.getIntent();
                mSettingDataUser = (SettingData) intent.getSerializableExtra("settingdata");
            }

            switch (msg.what) {
                case WHAT.WHAT_SET_WAKEUP_NAME_NULL:
                    String textNull = msg.obj.toString();
                    Log.d(TAG, "??????????????? WHAT_SET_WAKEUP_NAME_NULL= " + textNull);
                    mSettingDataUser.setWakeupWords(null);
                    setWakeupAddUi(WAKEUP_NAME_NULL);
                    JsonIntentUtil.getInstance().sendTXZSettingBroadcast(SetWakupNameActivity.this, mSettingDataUser);
                    break;
                case WHAT.WHAT_SET_WAKEUP_NAME_ERROR:
                    String textError = msg.obj.toString();
                    setWakeupAddUi(WAKEUP_NAME_ERROR);
                    break;
                case WHAT.WHAT_SET_WAKEUP_NAME_OK:
                    String text = msg.obj.toString();
                    Log.d(TAG, "??????????????? = " + text);
                    mSettingDataUser.setWakeupWords(new String[]{text});
                    setWakeupAddUi(WAKEUP_NAME_OK);
                    JsonIntentUtil.getInstance().sendTXZSettingBroadcast(SetWakupNameActivity.this, mSettingDataUser);
                    break;
                case WHAT.WHAT_INIT_UI:
                    if (mSettingDataUser.isWakeupEnable()) {
                        mCheckBoxWakeup.setChecked(false);//??????
                        mWakeupNameList.setVisibility(View.VISIBLE);
                        mSettingWakeupAddlistLayout.setVisibility(View.VISIBLE);
                    } else {
                        mCheckBoxWakeup.setChecked(true);//??????
                        mWakeupNameList.setVisibility(View.INVISIBLE);
                        mSettingWakeupAddlistLayout.setVisibility(View.INVISIBLE);
                    }
                    if (mSettingDataUser.getWakeupWords() != null) {
                        for (int i = 0; i < mSettingDataUser.getWakeupWords().length; i++) {
                            mAddWakeupNameList.setText(mSettingDataUser.getWakeupWords()[i]);
                            Log.d(TAG, "????????????????????????????????? = " + mSettingDataUser.getWakeupWords()[i]);
                        }
                    } else {
                        mAddWakeupNameList.setText("");
                    }

                    if (SPThreshholdUtil.getWakupCommandData(TXZApplication.getApp())) {
                        mCheckBoxWakeupCommand.setChecked(false);//??????
                    } else {
                        mCheckBoxWakeupCommand.setChecked(true);//??????
                    }
                    break;
            }
        }
    };

    /**
     * ????????????????????????UI
     *
     * @param wakeupNameData
     */
    private void setWakeupAddUi(int wakeupNameData) {
        switch (wakeupNameData) {
            case WAKEUP_NAME_ERROR:
                Log.d(TAG, "=======WAKEUP_NAME_ERROR");
//                mAddWakeupNameList.setCompoundDrawables(draeableEditing, null, draeableJiantou, null);
                mSettingWakeupAddlistText.setVisibility(View.VISIBLE);
                mSettingWakeupAddlistText.setText(getResources().getString(R.string.show_wakeup_name_remind));
                mSettingWakeupAddlistText.setTextColor(getResources().getColor(R.color.color_red));

                mSettingWakeupAddlistImage.setVisibility(View.VISIBLE);
                mSettingWakeupAddlistImage.setImageResource(R.drawable.editing);

                mAddWakeupNameList.setTextSize(getResources().getDimension(R.dimen.x13));
                break;
            case WAKEUP_NAME_OK:
                Log.d(TAG, "=======WAKEUP_NAME_OK");
                mSettingWakeupAddlistText.setVisibility(View.GONE);
                mSettingWakeupAddlistImage.setVisibility(View.GONE);

                mAddWakeupNameList.setTextSize(getResources().getDimension(R.dimen.x14));
                break;
            case WAKEUP_NAME_NULL:
                Log.d(TAG, "=======WAKEUP_NAME_NULL");
                mSettingWakeupAddlistText.setVisibility(View.VISIBLE);
                mSettingWakeupAddlistText.setText(getResources().getString(R.string.show_wakeup_name_remind));
                mSettingWakeupAddlistText.setTextColor(getResources().getColor(R.color.color_text_item));

                mSettingWakeupAddlistImage.setVisibility(View.VISIBLE);
                mSettingWakeupAddlistImage.setImageResource(R.drawable.add_wakeup_list);

                mAddWakeupNameList.setTextSize(getResources().getDimension(R.dimen.x13));

                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_wakup_name);
        TXZApplication.getApp().addActivity(this);

        init();
        handler.sendEmptyMessage(WHAT.WHAT_INIT_UI);
    }

    /**
     * ???????????????????????????
     */
    private void initFactoryWakeup() {
        JSONArray jsonArray;
        String[] jsonWakeupText = null;
        try {
            if (!FileUtil.fileIsExists(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.FACTORYCONF_NAME)) {
                ToastUtil.showTips(getResources().getString(R.string.error1));
                finish();
                return;
            }
            String data = FileUtil.getFileContentBuffer(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.FACTORYCONF_NAME);
            Log.d(TAG, "initFactoryWakeup = " + data);
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();

            if (jsonObject.has(JsonIntentUtil.JSON_WAKEUP_WORDS)) {
                jsonArray = jsonObject.getJSONArray(JsonIntentUtil.JSON_WAKEUP_WORDS);
                jsonWakeupText = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonWakeupText[i] = jsonArray.getString(i);
                }
            }
            mSettingDataFactory = new SettingData();
            mSettingDataFactory.setWakeupWords(jsonWakeupText);
            for (int i = 0; i < mSettingDataFactory.getWakeupWords().length; i++) {
                mAdapterList.add(mSettingDataFactory.getWakeupWords()[i]);
                Log.d(TAG, "????????????????????????????????? = " + mSettingDataFactory.getWakeupWords()[i]);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void init() {
        initFactoryWakeup();
        mSettingWakeupAddlistLayout = (RelativeLayout) findViewById(R.id.setting_wakeup_addlist_layout);
        mSettingWakeupAddlistLayout.setOnClickListener(showCheckBoxWakeupOnClickListener);
        mSettingWakeupAddlistImage = (ImageView) findViewById(R.id.setting_wakeup_addlist_image);
        mSettingWakeupAddlistText = (TextView) findViewById(R.id.setting_wakeup_addlist_text);
        mWakeupNowText = (TextView) findViewById(R.id.wakeup_now_text);
        mAddWakeupNameList = (EditText) findViewById(R.id.setting_wakeup_addlist);
        mAddWakeupNameList.setCursorVisible(false);
        mAddWakeupNameList.setOnClickListener(showCheckBoxWakeupOnClickListener);


        mAddWakeupNameList.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {//????????????????????????
                Log.d(TAG, "afterTextChanged = " + editable.toString() + ",length = " + editable.toString().length());
                String textFirst = "";
                editableWakeupName = editable.toString().trim();
                if (editableWakeupName.length() <= 0) {
                    textFirst = " ";
                } else {
                    textFirst = editableWakeupName.substring(0, 1);
                }
                if (editableWakeupName.length() == 0) {//????????????
                    Message message = new Message();
                    message.what = WHAT.WHAT_SET_WAKEUP_NAME_NULL;
                    message.obj = editableWakeupName;
                    handler.sendMessage(message);
                    return;
                }
                String str = TextUtil.isChineseOkText(editableWakeupName);
                if (!editableWakeupName.equals(str)) {//?????????
                    mAddWakeupNameList.setText(str);
                    mAddWakeupNameList.setSelection(str.length());
                    ToastUtil.showTips(getResources().getString(R.string.setting_welcome_has_illegal));
                    return;
                }
                if (editableWakeupName.length() > 8) {
                    mAddWakeupNameList.setText(editableWakeupName.substring(0, 8));
                    mAddWakeupNameList.setSelection(editableWakeupName.length());
                    return;
                }
                if (editableWakeupName.length() >= 4 && editableWakeupName.length() <= 8) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.what = WHAT.WHAT_SET_WAKEUP_NAME_OK;
                            message.obj = editableWakeupName;
                            handler.sendMessage(message);
                        }
                    }).start();
                } else {
                    if (editableWakeupName.length() == 0) {
                        editableWakeupName = "";
                        Message message = new Message();
                        message.what = WHAT.WHAT_SET_WAKEUP_NAME_NULL;
                        message.obj = editableWakeupName;
                        handler.sendMessage(message);
                    } else {
                        ToastUtil.showTips(getResources().getString(R.string.setting_new_wakeup_text_explain));
                        Message message = new Message();
                        message.what = WHAT.WHAT_SET_WAKEUP_NAME_ERROR;
                        message.obj = editableWakeupName;
                        handler.sendMessage(message);
                    }
                }

            }
        });

        mSetWakeupReturn = (Button) findViewById(R.id.actionbar_return_setting_wakeup);
        mSetWakeupReturn.setOnClickListener(showCheckBoxWakeupOnClickListener);

        mWakeupCheckboxLayout = (LinearLayout) findViewById(R.id.layout_wakup);
        mWakeupCheckboxLayout.setOnClickListener(showCheckBoxWakeupOnClickListener);
        mShowWakeup = (TextView) findViewById(R.id.setting_wakup_checkbox);
        mCheckBoxWakeup = (CheckSwitchButton) findViewById(R.id.setting_wakup_show_checkbox);
        mCheckBoxWakeup.setOnCheckedChangeListener(mCheckBoxWakeupListener);

        mWakeupCommandCheckboxLayout = (LinearLayout) findViewById(R.id.layout_wakup_command);
        mWakeupCommandCheckboxLayout.setOnClickListener(showCheckBoxWakeupOnClickListener);
        mCheckBoxWakeupCommand = (CheckSwitchButton) findViewById(R.id.setting_wakup_show_checkbox_command);
        mCheckBoxWakeupCommand.setOnCheckedChangeListener(mCheckBoxWakeupListener);

        if (TXZApplication.getShowWakeupCommand()) {
            mWakeupCommandCheckboxLayout.setVisibility(View.VISIBLE);
        } else {
            mWakeupCommandCheckboxLayout.setVisibility(View.GONE);
        }

        mWakeupNameList = (ListView) findViewById(R.id.setting_wakeup_listview);
        adapterWakeup = new SetWakeupAdapter(this, mAdapterList);
        mWakeupNameList.setAdapter(adapterWakeup);

    }

    CompoundButton.OnCheckedChangeListener mCheckBoxWakeupListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isCheck) {
            Log.d(TAG, "mCheckBoxWakeup setOnCheckedChangeListener = " + isCheck);
            if (!MainActivity.getInstance().initErrorShow()) {
                if (!isCheck) {
                    mCheckBoxWakeup.setChecked(true);//??????
                    mCheckBoxWakeupCommand.setChecked(true);//??????
                } else {
                    mCheckBoxWakeup.setChecked(false);//??????
                    mCheckBoxWakeupCommand.setChecked(false);//??????
                }
                ToastUtil.showTips(getResources().getString(R.string.error1));
                return;
            }
            switch (buttonView.getId()) {
                case R.id.setting_wakup_show_checkbox:
                    if (!mCheckBoxWakeup.isChecked()) {
                        mWakeupNowText.setVisibility(View.VISIBLE);
                        mWakeupNameList.setVisibility(View.VISIBLE);
                        mSettingWakeupAddlistLayout.setVisibility(View.VISIBLE);
                        mSettingDataUser.setWakeupEnable(true);
                    } else {
                        mWakeupNowText.setVisibility(View.INVISIBLE);
                        mWakeupNameList.setVisibility(View.INVISIBLE);
                        mSettingWakeupAddlistLayout.setVisibility(View.INVISIBLE);
                        mSettingDataUser.setWakeupEnable(false);
                    }
                    TxzReportUtil.doReportWakeupEnable(mSettingDataUser.isWakeupEnable());
                    JsonIntentUtil.getInstance().sendTXZSettingBroadcast(SetWakupNameActivity.this, mSettingDataUser);
                    break;
                case R.id.setting_wakup_show_checkbox_command:
                    /**
                     * ????????????:
                     * ???sdcard/txz???system/txz?????????(sdcard/txz????????????system/txz??????????????????????????????????????????system/txz??????)
                     * ????????????????????????com.txznet.txzsetting.cfg??????????????????SHOW_WAKEUP_COMMAND = true???
                     * ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????TXZCore????????????????????????
                     * ??????/??????????????????????????????????????????
                     * ?????????????????????????????????????????????/????????????????????????????????????
                     */
                    Intent intent = new Intent(JsonIntentUtil.BROADCAST_TXZ_SEND);
                    intent.putExtra("action", "com.txznet.txzsetting");
                    if (!mCheckBoxWakeupCommand.isChecked()) {  //???
                        intent.putExtra("wakeup_command", true);
                        SPThreshholdUtil.setSharedPreferencesData(SetWakupNameActivity.this, SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_WAKEUP_COMMAND, true);

                    } else {//???
                        intent.putExtra("wakeup_command", false);
                        SPThreshholdUtil.setSharedPreferencesData(SetWakupNameActivity.this, SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_WAKEUP_COMMAND, false);
                    }
                    TXZApplication.getApp().sendBroadcast(intent);
                    Log.d(TAG, "send intent command = " + intent.getBooleanExtra("wakeup_command",true));
                    break;
            }

        }
    };

    View.OnClickListener showCheckBoxWakeupOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!MainActivity.getInstance().initErrorShow()) {
                return;
            }
            switch (view.getId()) {
                case R.id.actionbar_return_setting_wakeup:
                    onBackPressed();
                    break;
                case R.id.layout_wakup:
                    mCheckBoxWakeup.toggle();
                    break;
                case R.id.layout_wakup_command:
                    mCheckBoxWakeupCommand.toggle();
                    break;
                case R.id.setting_wakeup_addlist_layout:
                case R.id.setting_wakeup_addlist:
                    //???????????????
                    Intent intent = new Intent(SetWakupNameActivity.this, SetUserWakeupNameActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("settingdata", mSettingDataUser);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, REQUEST_CODE_USER_WAKEUP_SHOW);
                    break;

            }

        }
    };


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("wakeupname", mSettingDataUser);
        intent.putExtras(bundle);
        setResult(RESULT_CODE_SET_WAKEUP_NAME_ACTIVITY, intent);
        SetWakupNameActivity.this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode = " + requestCode + ",resultCode = " + resultCode + ",data =" + (data != null));
        switch (requestCode) {
            case REQUEST_CODE_USER_WAKEUP_SHOW:
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    mShowUserWakeupText = bundle.getString("wakeuptext");
                } else {
                    if (mSettingDataUser.getWakeupWords() != null) {
                        mShowUserWakeupText = mSettingDataUser.getWakeupWords()[0];
                    } else {
                        mShowUserWakeupText = "";
                    }
                }
                Log.d(TAG, "requestCode name = " + mShowUserWakeupText);
                mAddWakeupNameList.setText(mShowUserWakeupText);
                mSettingDataUser.setWakeupWords(new String[]{mShowUserWakeupText});
                break;
        }
    }
}
