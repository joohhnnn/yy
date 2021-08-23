package com.txznet.txzsetting.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.txzsetting.R;
import com.txznet.txzsetting.TXZApplication;
import com.txznet.txzsetting.data.SettingData;
import com.txznet.txzsetting.util.FileUtil;
import com.txznet.txzsetting.util.JsonIntentUtil;
import com.txznet.txzsetting.util.SPThreshholdUtil;
import com.txznet.txzsetting.util.ToastUtil;
import com.txznet.txzsetting.util.TxzReportUtil;
import com.txznet.txzsetting.view.CheckSwitchButton;
import com.txznet.txzsetting.view.DialogHiddenDoor;
import com.txznet.txzsetting.view.DialogThreshhold;
import com.txznet.txzsetting.view.DialogTtsRole;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class MainActivity extends Activity implements DialogThreshhold.DialogCheckBoxListener, DialogTtsRole.DialogCheckBoxListenerTts {
    public static final String TAG = "nickhu";
    public static final boolean isFileUtil = true;//是否用读指定目录的文件的方式存储数据
    private static final int VERSION_COMPATIBLE260 = 260;//TXZSetting仅在2.6.0及其以上版本支持

    private static final int VERSION_COMPATIBLE270 = 270;//设置地图模式，仅在2.7.0及其以上版本支持
    public static MainActivity instance = null;
    public LinearLayout mWakeupNameLayout, mThreshholdLayout, mWelcomeLayout, mVersionLayout, mUidLayout, mTtsroleLayout;

    public static final int REQUEST_CODE_WELCOME_SHOW = 0;
    public static final int REQUEST_CODE_WAKEUP_NAME_SHOW = 1;
    public static final int REQUEST_CODE_POI_MODE_SHOW = 2;
    private static boolean isEnterHiddenDoor = false;//是否进入了同行者后门，防止快速点击下多次show dialog


    private static boolean isError = false;


    private Button mActionbarReturn;
    private TextView mSettingWakupShow;
    private TextView mSettingThreshholdShow, mSettingThreshhold;
    private TextView mSettingWelcomeShow, mSettingWelcome;
    private TextView mTXZVersionShow;
    private TextView mSettingWakupShowExample;
    private TextView mSettingUidShow;
    private TextView mSettingWakup;
    private TextView mPoiMode, mPoiModeShow;
    private ImageView mTencentDingdang, mTencentCar;
    private CheckSwitchButton mSwitchButtonFloatTool;
    private LinearLayout mLayoutFloattool;
    private LinearLayout mLayoutPoiMode;

    private TextView mSettingTtsRoleShow;

    private String[] mWakupName = null;
    private String mShowWelcometext = "";
    private String mShowWakeupName = "";
    private String changeWakeupKeywords1 = null;
    private String changeWakeupKeywords2 = null;

    private static SettingData mSettingData = null;
    public static String mShowThreshhold = "";

    public static MainActivity getInstance() {
        if (instance == null) {
            instance = new MainActivity();
        }
        return instance;
    }

    public static String getShowThreshhold() {
        return mShowThreshhold;
    }

    public static SettingData getSettingData() {
        return mSettingData;
    }

    public static void setEnterHiddenDoor(boolean isEnter) {
        isEnterHiddenDoor = isEnter;
    }

    public Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT.WHAT_INIT_UI_FILE:
                    initDienjieFloat();//初始化迪恩杰的悬浮窗
                    initJsonData();
                    initFileThreshholdData();
//                    initFileWakupData();
//                    initFileWakupShowTextData();不用显示唤醒词了，所以去除此行
                    initFileWelcomeData();
                    initShowTxzUid();
                    initFilePoiModeData();
                    initTtsRoleData();
                    break;

                case WHAT.WHAT_UPDATE_WAKUP_FILE:
                    if (changeWakeupKeywords1 != null) {
                        if (changeWakeupKeywords2 != null) {
                            mSettingWakupShowExample.setText("(当前唤醒词为\"" + changeWakeupKeywords1 + "\"、\"" + changeWakeupKeywords2 + "\")");
                        } else {
                            mSettingWakupShowExample.setText("(当前唤醒词为\"" + changeWakeupKeywords1 + "\")");
                        }
                    } else {
                        mSettingWakupShowExample.setText("默认");
                    }
                    break;
                case WHAT.WHAT_INIT_UI:
                    initDienjieFloat();//初始化迪恩杰的悬浮窗
                    initThreshholdData();
                    initWakupData();
                    initWelcomeData();
                    initWakupShowTextData();
                    initShowTxzUid();
                    initFilePoiModeData();
                    break;
                case WHAT.WHAT_UPDATE_WAKUP:
                    break;
            }
        }

    };


    private static interface WHAT {
        int WHAT_INIT_UI_FILE = 0x1000;
        int WHAT_UPDATE_WAKUP_FILE = 0x1001;
        int WHAT_INIT_UI = 0x1002;
        int WHAT_UPDATE_WAKUP = 0x1003;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        TXZApplication.getApp().addActivity(this);

        TxzReportUtil.doReportMain();

        mTencentDingdang = (ImageView) findViewById(R.id.logo_dingdang);
        mTencentCar = (ImageView) findViewById(R.id.logo_txcl);

        mLayoutPoiMode = (LinearLayout) findViewById(R.id.layout_poi_map_mode);
        mPoiMode = (TextView) findViewById(R.id.setting_poi_map_mode);
        mPoiMode.setOnClickListener(mPoiMapModeListener);
        mPoiModeShow = (TextView) findViewById(R.id.setting_poi_map_mode_show);
        mPoiModeShow.setOnClickListener(mPoiMapModeListener);

        mLayoutFloattool = (LinearLayout) findViewById(R.id.layout_floattool);
        mLayoutFloattool.setOnClickListener(showCheckBoxFloatToolOnClickListener);
        mSwitchButtonFloatTool = (CheckSwitchButton) findViewById(R.id.setting_floattool_show);
        mSwitchButtonFloatTool.setOnCheckedChangeListener(mCheckBoxFloatToolListener);

        mWakeupNameLayout = (LinearLayout) findViewById(R.id.layout_wakup_main);
        mWakeupNameLayout.setOnClickListener(settingWakupShowListener);
        mThreshholdLayout = (LinearLayout) findViewById(R.id.layout_threshhold);
        mThreshholdLayout.setOnClickListener(settingThreshholdListener);
        mTtsroleLayout = (LinearLayout) findViewById(R.id.layout_ttsrole);
        mTtsroleLayout.setOnClickListener(settingTtsroleListener);
        mWelcomeLayout = (LinearLayout) findViewById(R.id.layout_welcome);
        mWelcomeLayout.setOnClickListener(settingWelcomeShowListener);

        mVersionLayout = (LinearLayout) findViewById(R.id.layout_version);
        mVersionLayout.setOnClickListener(layoutHiddenDoorListener);
        mUidLayout = (LinearLayout) findViewById(R.id.layout_uid);
        mUidLayout.setOnClickListener(layoutNullListener);

        mActionbarReturn = (Button) findViewById(R.id.actionbar_return);
        mActionbarReturn.setOnClickListener(actionbarReturnListener);

        mSettingWakup = (TextView) findViewById(R.id.setting_wakup);
        mSettingWakupShow = (TextView) findViewById(R.id.setting_wakup_show);
        mSettingWakupShow.setOnClickListener(settingWakupShowListener);

        mSettingThreshholdShow = (TextView) findViewById(R.id.setting_threshhold_show);
        mSettingThreshholdShow.setOnClickListener(settingThreshholdListener);
        mSettingThreshhold = (TextView) findViewById(R.id.setting_threshhold);

        mSettingTtsRoleShow = (TextView) findViewById(R.id.setting_ttsrole_show);
        mSettingTtsRoleShow.setOnClickListener(settingTtsroleListener);

        mSettingWelcome = (TextView) findViewById(R.id.setting_welcome);
        mSettingWelcomeShow = (TextView) findViewById(R.id.setting_welcome_show);
        mSettingWelcomeShow.setOnClickListener(settingWelcomeShowListener);

        mTXZVersionShow = (TextView) findViewById(R.id.setting_txzversion_show);
        mSettingUidShow = (TextView) findViewById(R.id.setting_uid);
        mSettingData = new SettingData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "nickhu:: onresume");
        isEnterHiddenDoor = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (isFileUtil)
                    mHandler.sendEmptyMessage(WHAT.WHAT_INIT_UI_FILE);
                else
                    mHandler.sendEmptyMessage(WHAT.WHAT_INIT_UI);
            }
        }).start();
    }

    /**
     * POI模式选择
     */
    private View.OnClickListener mPoiMapModeListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!initErrorShow()) {
                return;
            }
            Intent intentPoiMode = new Intent(MainActivity.this, SetPoiMapModeActivity.class);
            Bundle bundlePoiMode = new Bundle();
            bundlePoiMode.putSerializable("settingdata", mSettingData);
            intentPoiMode.putExtras(bundlePoiMode);
            startActivityForResult(intentPoiMode, REQUEST_CODE_POI_MODE_SHOW);
        }
    };

    /**
     * 悬浮窗
     */
    private CompoundButton.OnCheckedChangeListener mCheckBoxFloatToolListener = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (!initErrorShow()) {
                return;
            }
            Log.d(TAG, "mCheckBoxFloatToolListener = " + b);
            /**
             * 约定规则:
             * 悬浮窗的开关加发如下广播，要者自取
             */
            Intent intent = new Intent(JsonIntentUtil.BROADCAST_TXZ_SEND);
            intent.putExtra("action", "com.txznet.txzsetting");
            intent.putExtra("float_tool", !b);//灰色和蓝色是反的，要注意
            sendBroadcast(intent);
            if (b) {
                if (TXZApplication.isDiEnJie)
                    setDienjieFloat(false);
                TXZConfigManager.getInstance().showFloatTool(
                        TXZConfigManager.FloatToolType.FLOAT_NONE);
                SPThreshholdUtil.setSharedPreferencesData(MainActivity.this, SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_FLOAT_TOOL, false);
            } else {
                if (TXZApplication.isDiEnJie)
                    setDienjieFloat(true);
                TXZConfigManager.getInstance().showFloatTool(
                        TXZConfigManager.FloatToolType.FLOAT_TOP);
                SPThreshholdUtil.setSharedPreferencesData(MainActivity.this, SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_FLOAT_TOOL, true);

            }
        }
    };
    private View.OnClickListener showCheckBoxFloatToolOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!initErrorShow()) {
                return;
            }
            mSwitchButtonFloatTool.toggle();
        }
    };
    /**
     * 空实现
     */
    private View.OnClickListener layoutNullListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
        }
    };

    /**
     * 点击触发同行者暗门
     */
    private View.OnClickListener layoutHiddenDoorListener = new View.OnClickListener() {
        final static int COUNTS = 10;//点击次数
        final static long DURATION = 3 * 1000;//规定有效时间
        long[] mHits = new long[COUNTS];


        @Override
        public void onClick(View view) {
            System.arraycopy(mHits, 1, mHits, 0, mHits.length - 1);//把从第二位至最后一位之间的数字复制到第一位至倒数第一位实现左移
            //然后最后一个位置更新距离开机的时间，如果最后一个时间和最开始时间小于DURATION，即连续5次点击
            mHits[mHits.length - 1] = SystemClock.uptimeMillis();//从开机到现在的时间毫秒数
            if (mHits[0] >= (SystemClock.uptimeMillis() - DURATION)) {//触发同行者吊炸天后门
                Log.d(TAG, "nickhu::触发同行者后门了！");
                if (!isEnterHiddenDoor) {
                    isEnterHiddenDoor = true;
                    Dialog dialog = new DialogHiddenDoor(MainActivity.this, R.style.DialogTheme);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
            } else {
                isEnterHiddenDoor = false;
            }
        }
    };
    /**
     * 返回键
     */
    private View.OnClickListener actionbarReturnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
//            if (!initErrorShow()) {
//                return;
//            }
            onBackPressed();
            TXZAsrManager.getInstance().start("还有什么需要");
        }
    };

    /**
     * 唤醒词按键
     */
    private View.OnClickListener settingWakupShowListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!initErrorShow()) {
                return;
            }
            Intent intentWakup = new Intent(MainActivity.this, SetWakupNameActivity.class);
            Bundle bundleWakeup = new Bundle();
            bundleWakeup.putSerializable("settingdata", mSettingData);
            intentWakup.putExtras(bundleWakeup);
            startActivityForResult(intentWakup, REQUEST_CODE_WAKEUP_NAME_SHOW);
        }
    };

    /**
     * 点击TTS主题包按键
     */
    private View.OnClickListener settingTtsroleListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!initErrorShow()) {
                return;
            }
            Log.d(TAG, "threshhold_show");
            Dialog dialog = new DialogTtsRole(MainActivity.this, R.style.DialogTheme, MainActivity.this);
            dialog.show();
        }
    };


    /**
     * 点击阀值按键
     */
    private View.OnClickListener settingThreshholdListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!initErrorShow()) {
                return;
            }
            Log.d(TAG, "threshhold_show");
            Dialog dialog = new DialogThreshhold(MainActivity.this, R.style.DialogTheme, MainActivity.this);
            dialog.show();
        }
    };
    /**
     * 点击欢迎按键
     */
    private View.OnClickListener settingWelcomeShowListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (!initErrorShow()) {
                return;
            }
            Log.d(TAG, "welcome_show");
            Intent intent = new Intent(MainActivity.this, SetWelcomeActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("settingdata", mSettingData);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQUEST_CODE_WELCOME_SHOW);
        }
    };

    /**
     * 判断当前版本是否大于version版本
     */
    private boolean isVersionOk(int version) {
        //比较版本号
        String versionString = getVersion("com.txznet.txz");
        int versionInt = Integer.parseInt(versionString.replace(".", ""));
        Log.d(TAG, "versionInt = " + versionInt);
        if (versionInt >= version)
            return true;
        else
            return false;
    }

    /**
     * POI模式选择设置（TXZCore2.7.0才支持）
     */
    private void initFilePoiModeData() {
        int poimodeData = mSettingData.getPoiMode();
        Log.d(TAG, "initFilePoiModeData = " + poimodeData);

        if (isVersionOk(VERSION_COMPATIBLE270)) {
            mLayoutPoiMode.setVisibility(View.VISIBLE);
        }

        switch (poimodeData) {
            case JsonIntentUtil.JSON_POI_MODE_BLEND:
                mPoiModeShow.setText(R.string.setting_poi_map_mode_blend);
                break;
            case JsonIntentUtil.JSON_POI_MODE_LIST:
                mPoiModeShow.setText(R.string.setting_poi_map_mode_list);
                break;
            case JsonIntentUtil.JSON_POI_MODE_NOTSET:
                mLayoutPoiMode.setVisibility(View.GONE);
                break;
            default:
                mPoiModeShow.setText("");
                break;
        }
    }

    /**
     * 显示唤醒词阀值高低
     */
    private void initFileThreshholdData() {
        double threshholdData = mSettingData.getThreshhold();
        Log.d(TAG, "initFileThreshholdData = " + threshholdData);
        if (threshholdData >= JsonIntentUtil.JSON_LOW_VERY) {//2.6
            mShowThreshhold = "极低";
            mSettingThreshholdShow.setText("极低");
        } else if (threshholdData >= JsonIntentUtil.JSON_LOW && threshholdData < JsonIntentUtil.JSON_LOW_VERY) {//2.8
            mShowThreshhold = "低";
            mSettingThreshholdShow.setText("低");
        } else if (threshholdData > JsonIntentUtil.JSON_HIGH && threshholdData < JsonIntentUtil.JSON_LOW) {//3.2、3.0
            mShowThreshhold = "正常";
            mSettingThreshholdShow.setText("正常");
        } else if (threshholdData <= JsonIntentUtil.JSON_HIGH && threshholdData > JsonIntentUtil.JSON_HIGH_VERY) {//3.4
            mShowThreshhold = "高";
            mSettingThreshholdShow.setText("高");
        } else if (threshholdData <= JsonIntentUtil.JSON_HIGH_VERY) {//4.0
            mShowThreshhold = "极高";
            mSettingThreshholdShow.setText("极高");
        }
    }

    /**
     * 初始化TTS主题包显示
     */
    private void initTtsRoleData() {
        if (TXZApplication.isShowTtsRole) {
            mTtsroleLayout.setVisibility(View.VISIBLE);
            TXZTtsManager.TtsTheme[] ttsThemes = TXZTtsManager.getInstance().getTtsThemes();
            int tts_role = SPThreshholdUtil.getTtsRoleData(TXZApplication.getApp());

            switch (tts_role) {
                case SPThreshholdUtil.TTS_ROLE_DEFAULT:
                    mSettingTtsRoleShow.setText("标准女声");
                    break;
                case SPThreshholdUtil.TTS_ROLE_10006:
                    mSettingTtsRoleShow.setText("情感男声");
                    break;
                case SPThreshholdUtil.TTS_ROLE_10003:
                    mSettingTtsRoleShow.setText("甜美女声");
                    break;
                default:
                    Log.d(TAG, "拿到的不是荣威RX5的TTS");
                    mSettingTtsRoleShow.setText("标准女声");
                    break;
            }
        } else {
            mTtsroleLayout.setVisibility(View.GONE);
        }
    }

    /**
     * 显示唤醒词开关
     */
    private void initFileWakupData() {
        boolean wakupData = mSettingData.isWakeupEnable();
//        mSettingWakupShow.setChecked(wakupData);
    }

    /**
     * 显示欢迎语
     */
    private void initFileWelcomeData() {
        String welcomeData = " ";
        welcomeData = mSettingData.getWelcomeTest();
        if (welcomeData == null) {
            welcomeData = "默认";
        }
        mSettingWelcomeShow.setText(welcomeData);
        Log.d(TAG, "initFileWelcomeData welcomeData = " + welcomeData);
    }

    /**
     * 显示唤醒词
     */
    private void initFileWakupShowTextData() {
        String wakupShowExample = "";
        String test = "";
        if (mSettingData.getWakeupWords() == null) {
            Log.d(TAG, "读取的唤醒词为空");
            return;
        }
        if (mSettingData.getWakeupWords().length > 0) {
            test = mSettingData.getWakeupWords()[0];
            for (int i = 1; i < mSettingData.getWakeupWords().length; i++) {
                test = test + "、" + mSettingData.getWakeupWords()[i];
            }
        }
        Log.d(TAG, "initFileWakupShowTextData test = " + test);
        wakupShowExample = "(当前唤醒词为：" + test + ")";
        changeWakeupKeywords1 = test;
        mSettingWakupShowExample.setText(wakupShowExample);
    }


    /**
     * 单独校验腾讯品牌背书LOGO，此项配置仅在factoryconf.json文件中才有
     *
     * @param data      传当前已经读取的json
     * @param isFactory 当前data是否已经是factory数据了，避免重复读文件
     */
    private void initTencentData(String data, boolean isFactory) {
        Boolean jsonTencentIsuse = null;
        try {
            if (!isFactory) {
                if (FileUtil.fileIsExists(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.FACTORYCONF_NAME)) {//判断出厂配置文件是否存在
                    Log.d(TAG, "读出厂配置文件");
                    isError = false;
                    data = FileUtil.getFileContentBuffer(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.FACTORYCONF_NAME);
                } else {
                    return;
                }
            }
            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            if (jsonObject.has(JsonIntentUtil.JSON_ENGINE_TENCENT_ISUSE)) {
                jsonTencentIsuse = jsonObject.getBoolean(JsonIntentUtil.JSON_ENGINE_TENCENT_ISUSE);
                if (jsonTencentIsuse) {
                    mTencentDingdang.setVisibility(View.VISIBLE);
                    mTencentCar.setVisibility(View.VISIBLE);
                } else {
                    mTencentDingdang.setVisibility(View.GONE);
                    mTencentCar.setVisibility(View.GONE);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * 读文件初始化数据
     */
    private void initJsonData() {
        Log.d(TAG, "initJsonData读文件初始化数据");
        try {
            String jsonWelcomeText = null;
            Double jsonThreshhold = Double.valueOf(-3.1f);
            boolean jsonWakeupEnable = true;
            JSONArray jsonArray;
            String[] jsonWakeupText = null;
            int jsonPoiMode = 0;
            String data = "";
            String dataCfg = "";


            if (FileUtil.fileIsExists(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.USERCONF_NAME)) {//判断用户配置文件是否存在
                Log.d(TAG, "读用户配置文件");
                isError = false;
                data = FileUtil.getFileContentBuffer(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.USERCONF_NAME);
                initTencentData(data, false);//再读一遍出厂配置文件中的腾讯LOGO配置
            } else if (FileUtil.fileIsExists(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.FACTORYCONF_NAME)) {//判断出厂配置文件是否存在
                Log.d(TAG, "读出厂配置文件");
                isError = false;
                data = FileUtil.getFileContentBuffer(JsonIntentUtil.USERCONF_SAVE_DIR, JsonIntentUtil.FACTORYCONF_NAME);
                initTencentData(data, true);//直接根据data判断腾讯logo是否需要显示
            } else {
                isError = true;
                initErrorShow();
                Log.d(TAG, "配置文件不存在");
                return;
            }

            JSONTokener jsonTokener = new JSONTokener(data);
            JSONObject jsonObject = (JSONObject) jsonTokener.nextValue();
            if (jsonObject.has(JsonIntentUtil.JSON_WELCOME_TEXT))
                jsonWelcomeText = jsonObject.getString(JsonIntentUtil.JSON_WELCOME_TEXT);
            if (jsonObject.has(JsonIntentUtil.JSON_THRESHHOLD))
                jsonThreshhold = jsonObject.getDouble(JsonIntentUtil.JSON_THRESHHOLD);
            if (jsonObject.has(JsonIntentUtil.JSON_WAKEUP_ENABLE))
                jsonWakeupEnable = jsonObject.getBoolean(JsonIntentUtil.JSON_WAKEUP_ENABLE);
            if (jsonObject.has(JsonIntentUtil.JSON_WAKEUP_WORDS)) {
                jsonArray = jsonObject.getJSONArray(JsonIntentUtil.JSON_WAKEUP_WORDS);
                jsonWakeupText = new String[jsonArray.length()];
                for (int i = 0; i < jsonArray.length(); i++) {
                    jsonWakeupText[i] = jsonArray.getString(i);
                }
            }
            if (jsonObject.has(JsonIntentUtil.JSON_POI_MAP_MODE)) {
                jsonPoiMode = jsonObject.getInt(JsonIntentUtil.JSON_POI_MAP_MODE);
            }


            mSettingData.setThreshhold(jsonThreshhold);
            mSettingData.setWakeupEnable(jsonWakeupEnable);
            mSettingData.setWakeupWords(jsonWakeupText);
            mSettingData.setWelcomeTest(jsonWelcomeText);
            mSettingData.setPoiMode(jsonPoiMode);


            /***********************读com.txznet.txzsetting.cfg配置文件**************************************/
            //改到application里面只读一次

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 初始化显示的UID
     */
    private void initShowTxzUid() {
        String uidName = FileUtil.getFileName(JsonIntentUtil.USERCONF_SAVE_DIR);
        Log.d(TAG, "TXZ uid ==" + uidName);
        mSettingUidShow.setText(uidName);


        String txzVersion = getVersion("com.txznet.txz");
        String settingVersion = getVersion(TXZApplication.getApp().getPackageName());
        String webchatVersion = getVersion("com.txznet.webchat");
        String musicVersion = getVersion("com.txznet.music");
        String showVersion = "TXZCore:" + txzVersion + " - TXZSetting:" + settingVersion;
        if (!webchatVersion.isEmpty()) {
            showVersion += " - TXZWebchat:" + webchatVersion;
            mTXZVersionShow.setTextSize(12);
        }
        if (!musicVersion.isEmpty()) {
            showVersion += " - TXZMusic:" + musicVersion;
            mTXZVersionShow.setTextSize(12);
        }
        mTXZVersionShow.setText(showVersion);

    }

    /**
     * 初始化显示的唤醒词
     */
    private void initWakupShowTextData() {
        String wakupShowExample = "";
        wakupShowExample = SPThreshholdUtil.getWakupShowExample(TXZApplication.getApp());
        mSettingWakupShowExample.setText(wakupShowExample);
    }

    /**
     * 初始化欢迎语
     */
    private void initWelcomeData() {
        String welcomeData = "";
        welcomeData = SPThreshholdUtil.getWelcomeData(TXZApplication.getApp());
        mSettingWelcomeShow.setText(welcomeData);
        if (welcomeData.equals("默认")) {
            TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_ASR_START_HINT", new String[]{"我在呢", "您好", "哈喽", "乐意为您效劳", "需要帮忙吗", "有什么可以帮您"});
        } else {
            TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_ASR_START_HINT", new String[]{welcomeData});
        }
    }

    /**
     * 初始化唤醒开关
     */
    private void initWakupData() {
        boolean wakupData = SPThreshholdUtil.getWakupData(TXZApplication.getApp());
        String[] wakupNameData = SPThreshholdUtil.getWakupNameData(TXZApplication.getApp());
        if (wakupData) {
            if (wakupNameData != null) {
                TXZConfigManager.getInstance().setWakeupKeywordsNew(wakupNameData);
            } else {
                TXZConfigManager.getInstance().setWakeupKeywordsNew(new String[]{"你好小踢", "小踢你好"});
            }
            TXZConfigManager.getInstance().enableChangeWakeupKeywords(true);
        } else {
            TXZConfigManager.getInstance().getUserWakeupKeywords(new TXZConfigManager.UserKeywordsCallback() {
                @Override
                public void result(String[] strings) {
                    SPThreshholdUtil.setSharedPreferencesData(TXZApplication.getApp(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_WAKUP_NAME, strings);
                    mWakupName = new String[strings.length];
                    for (int i = 0; i < strings.length; i++) {
                        mWakupName[i] = strings[i];
                    }
                }
            });
            TXZConfigManager.getInstance().setWakeupKeywordsNew();
            TXZConfigManager.getInstance().enableChangeWakeupKeywords(false);
        }
    }

    /**
     * 初始化唤醒阀值
     */
    private void initThreshholdData() {
        String threshholdData = SPThreshholdUtil.getThreshholdData(TXZApplication.getApp());
        mSettingThreshholdShow.setText(threshholdData);
        switch (threshholdData) {
            case "极高":
                TXZConfigManager.getInstance().setAsrWakeupThreshhold(SPThreshholdUtil.THRESHHOLD_HIGH_VERY);
                break;
            case "高":
                TXZConfigManager.getInstance().setAsrWakeupThreshhold(SPThreshholdUtil.THRESHHOLD_HIGH);
                break;
            case "正常":
                TXZConfigManager.getInstance().setAsrWakeupThreshhold(SPThreshholdUtil.THRESHHOLD_NORMAL);
                break;
            case "低":
                TXZConfigManager.getInstance().setAsrWakeupThreshhold(SPThreshholdUtil.THRESHHOLD_LOW);
                break;
            case "极低":
                TXZConfigManager.getInstance().setAsrWakeupThreshhold(SPThreshholdUtil.THRESHHOLD_LOW_VERY);
                break;
            default:
                Log.d(TAG, "getThreshholdData == " + threshholdData);
                mSettingThreshholdShow.setText("正常");
                TXZConfigManager.getInstance().setAsrWakeupThreshhold(SPThreshholdUtil.THRESHHOLD_NORMAL);
                break;
        }
    }


    /**
     * 获取版本号
     *
     * @param packagename
     * @return
     */
    public String getVersion(String packagename) {
        PackageManager manager = getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(packagename, 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode = " + requestCode + ",resultCode = " + resultCode);
        switch (requestCode) {
            case REQUEST_CODE_WELCOME_SHOW:
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    mShowWelcometext = bundle.getString("welcometext");
                }
                if (mShowWelcometext == null) {
                    mShowWelcometext = "默认";
                }
                Log.d(TAG, "welcomeData = " + mShowWelcometext);
                mSettingWelcomeShow.setText(mShowWelcometext);

                if (mShowWelcometext.equals("默认")) {
                    mSettingData.setWelcomeTest(null);
                    JsonIntentUtil.getInstance().sendTXZSettingBroadcast(MainActivity.this, mSettingData);
                } else {
                    mSettingData.setWelcomeTest(mShowWelcometext);
                    JsonIntentUtil.getInstance().sendTXZSettingBroadcast(MainActivity.this, mSettingData);
                }
                break;
            case REQUEST_CODE_WAKEUP_NAME_SHOW:
                if (data != null) {
                    mSettingData = (SettingData) data.getSerializableExtra("wakeupname");
                    Log.d(TAG, mSettingData.toString());
                }
                JsonIntentUtil.getInstance().sendTXZSettingBroadcast(MainActivity.this, mSettingData);
                break;
            case REQUEST_CODE_POI_MODE_SHOW:
                int poimode = -1;
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    poimode = bundle.getInt("poimode", -1);
                    Log.d(TAG, "poi mode = " + poimode);
                }

                if (poimode == 1) {
                    mPoiModeShow.setText(R.string.setting_poi_map_mode_list);
                } else if (poimode == 2) {
                    mPoiModeShow.setText(R.string.setting_poi_map_mode_blend);
                } else if (poimode == 0 || poimode == -1) {
                    mPoiModeShow.setText("");
                }
                mSettingData.setPoiMode(poimode);
                break;
        }
    }


    /**
     * 回调啊啊啊
     *
     * @param tts
     */
    @Override
    public void refreshDialogCheckBoxUITts(int tts) {
        switch (tts) {
            case SPThreshholdUtil.TTS_ROLE_10003:
                mSettingTtsRoleShow.setText("甜美女声");
                break;
            case SPThreshholdUtil.TTS_ROLE_10006:
                mSettingTtsRoleShow.setText("情感男声");
                break;
            case SPThreshholdUtil.TTS_ROLE_DEFAULT:
                mSettingTtsRoleShow.setText("标准女声");
                break;
            default:
                mSettingTtsRoleShow.setText("标准女声");
                break;
        }
    }

    @Override
    public void refreshDialogCheckBoxUI(String string) {
        mSettingThreshholdShow.setText(string);
        switch (string) {
            case "极高":
                mShowThreshhold = "极高";
                mSettingData.setThreshhold(JsonIntentUtil.JSON_HIGH_VERY);
                break;
            case "高":
                mShowThreshhold = "高";
                mSettingData.setThreshhold(JsonIntentUtil.JSON_HIGH);
                break;
            case "正常":
                mShowThreshhold = "正常";
                mSettingData.setThreshhold(JsonIntentUtil.JSON_NORMAL);
                break;
            case "低":
                mShowThreshhold = "低";
                mSettingData.setThreshhold(JsonIntentUtil.JSON_LOW);
                break;
            case "极低":
                mShowThreshhold = "极低";
                mSettingData.setThreshhold(JsonIntentUtil.JSON_LOW_VERY);
                break;
        }
    }


    /**
     * 判断当前设置功能是否可用
     *
     * @return
     */
    public boolean initErrorShow() {
        Log.d(TAG, "isError = " + isError);
        if (isError) {
            if (isVersionOk(VERSION_COMPATIBLE260)) {
                ToastUtil.showTips(getResources().getString(R.string.error1));
            } else {
                ToastUtil.showTips(getResources().getString(R.string.error3));
            }
            return false;
        }
        if (!TXZConfigManager.getInstance().isInitedSuccess()) {
            ToastUtil.showTips(getResources().getString(R.string.error2_no_init_txzsdk));
            return false;
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            TxzReportUtil.doReportDestroy(TxzReportUtil.KEY_CODE_BACK);
            finish();
            TXZAsrManager.getInstance().start("还有什么需要");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        TxzReportUtil.doReportDestroy(TxzReportUtil.KEY_CODE_BACK);
        super.onBackPressed();
    }

    private void setDienjieFloat(boolean isChecked) {
        Log.d(TAG, "state->setDienjieFloat" + isChecked);
        if (TXZApplication.isDiEnJie) {
            try {
                TXZApplication.mIRecognitionFloat.setState(isChecked);
                Log.d(TAG, "state->" + TXZApplication.mIRecognitionFloat.getState());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 初始化悬浮窗的状态
     */
    private void initDienjieFloat() {
        Log.d(TAG, "state->initDienjieFloat");

        if (TXZApplication.isDiEnJie || TXZApplication.isIsDiEnJieGongBan || TXZApplication.isShowFloatTool) {
            mLayoutFloattool.setVisibility(View.VISIBLE);
            mSwitchButtonFloatTool.setVisibility(View.VISIBLE);
            boolean floatToolExample = SPThreshholdUtil.getFloatToolExample(MainActivity.this);
            Log.d(TAG, "get floatToolExample = " + floatToolExample);
            if (floatToolExample) {
                TXZConfigManager.getInstance().showFloatTool(TXZConfigManager.FloatToolType.FLOAT_TOP);
                mSwitchButtonFloatTool.setChecked(false);//这里的checked是反的
            } else {
                TXZConfigManager.getInstance().showFloatTool(TXZConfigManager.FloatToolType.FLOAT_NONE);
                mSwitchButtonFloatTool.setChecked(true);//这里的checked是反的
            }
            if (TXZApplication.isDiEnJie) {
                try {
                    boolean state = TXZApplication.mIRecognitionFloat.getState();
                    Log.d(TAG, "init state->" + state);
                    mSwitchButtonFloatTool.setChecked(state);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            mLayoutFloattool.setVisibility(View.GONE);
            mSwitchButtonFloatTool.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onDestroy() {
        TxzReportUtil.doReportDestroy(TxzReportUtil.DESTROY_UNKNOWN);
        super.onDestroy();
    }
}
