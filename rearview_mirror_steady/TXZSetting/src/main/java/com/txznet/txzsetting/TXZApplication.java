package com.txznet.txzsetting;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.clw.recognition.IRecognitionFloat;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZConfigManager.ActiveListener;
import com.txznet.sdk.TXZConfigManager.InitListener;
import com.txznet.txzsetting.activity.MainActivity;
import com.txznet.txzsetting.util.FilePathConstants;
import com.txznet.txzsetting.util.FileUtil;
import com.txznet.txzsetting.util.JsonIntentUtil;
import com.txznet.txzsetting.util.SPThreshholdUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Nickhu on 2017/6/2.
 * {
 * "wakeup.threshhold.value" : 0.0f,
 * "wakeup.words":["你好小贱", "你好菜菜"],
 * "wakeup.enable":false,
 * "device.welcome.msg":"主人你好",
 * }
 */

public class TXZApplication extends Application implements InitListener,
        ActiveListener {

    public static final boolean isDiEnJie = false;//迪恩杰天之眼T2项目专用
    public static final boolean isIsDiEnJieGongBan = false;//迪恩杰公版专用
    public static final boolean isShowFloatTool = false;//是否要显示悬浮窗（部分版本需要）
    public static final boolean isShowTtsRole = true;//是否要显示TTS主题包（部分版本需要）


    public static final String TAG = TXZApplication.class.getSimpleName();
    private static TXZApplication instance;
    protected static Handler uiHandler = new Handler(Looper.getMainLooper());

    private List<Activity> mList = new LinkedList<>();

    private static boolean isShowWakeupCommand = false;//是否显示快捷指令开关（免唤醒词）读配置文件处理
    public static void setShowWakeupCommand(boolean showWakeupCommand){
        isShowWakeupCommand = showWakeupCommand;
    }
    public static boolean getShowWakeupCommand(){
        return isShowWakeupCommand;
    }

    /**
     * 添加Activity
     *
     * @param activity
     */
    public void addActivity(Activity activity) {
        mList.add(activity);
    }

    /**
     * 关闭所有Activity
     */
    public void exitActivity() {
        try {
            for (Activity activity : mList) {
                if (activity != null)
                    activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }

    /************************************************/
    public static IRecognitionFloat mIRecognitionFloat;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
            mIRecognitionFloat = null;
        }

        @Override
        public synchronized void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected");
            mIRecognitionFloat = IRecognitionFloat.Stub.asInterface(service);
        }
    };

    /************************************************/
    private interface WHAT {
        int WHAT_INIT_SUCCESS = 0x1000;
    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT.WHAT_INIT_SUCCESS:
                    Log.d(TAG, "WHAT_INIT_SUCCESS");
                    initDienjieGongBan();//初始化迪恩杰公版信息
                    initSettingCfgData();
                    break;
            }
        }
    };


    public static TXZApplication getApp() {
        return instance;
    }

    public static void runOnUiGround(Runnable r, long delay) {
        if (delay > 0) {
            uiHandler.postDelayed(r, delay);
        } else {
            uiHandler.post(r);
        }
    }

    public static void removeUiGroundCallback(Runnable r) {
        uiHandler.removeCallbacks(r);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        if (isDiEnJie) {
            bindServiceDienjie(this);
        }
        TXZConfigManager.getInstance().initialize(this, this);
    }

    @Override
    public void onFirstActived() {
    }

    @Override
    public void onError(int errCode, String errDesc) {
    }

    JSONObject json = null;

    @Override
    public void onSuccess() {
        Log.d(TAG, "onSuccess");
        mHandler.sendEmptyMessage(WHAT.WHAT_INIT_SUCCESS);
    }

    /**
     * 初始化配置信息
     */
    private void initSettingCfgData(){
        if (FileUtil.fileIsExists(FilePathConstants.getTXZSettingCfgFile(), null)) {//判断com.txznet.txzsetting.cfg是否存在
            Log.d(TAG, "读配置文件com.txznet.txzsetting.cfg");
            List<String> configKeys = new ArrayList<>();
            configKeys.add(JsonIntentUtil.TXZ_SETTING_CFG_SHOW_WAKEUP_COMMAND);
            HashMap<String, String> configs = FilePathConstants.getConfig(configKeys);
            if (configs.get(JsonIntentUtil.TXZ_SETTING_CFG_SHOW_WAKEUP_COMMAND) != null) {
                try {
                    boolean setCfgWakeupCommand = Boolean.parseBoolean(configs.get(JsonIntentUtil.TXZ_SETTING_CFG_SHOW_WAKEUP_COMMAND));
                    Log.d(TAG, "com.txznet.txzsetting.cfg setCfgWakeupCommand = " + setCfgWakeupCommand);
                    setShowWakeupCommand(setCfgWakeupCommand);
                } catch (Exception e) {
                    Log.d(TAG, "txzsetting cfg error set setCfgWakeupCommand = " + e.toString());
                }
            }
        }
    }

    /**
     * 约定初始化迪恩杰公版信息
     */
    private void initDienjieGongBan(){
        if (isIsDiEnJieGongBan) {
            TXZConfigManager.getInstance().setUIConfigListener(new TXZConfigManager.UIConfigListener() {
                @Override
                public void onConfigChanged(String s) {
                    Log.d(TAG, "onConfigChanaged::" + s);
                    try {
                        json = new JSONObject(s);
                        String floatTool = json.optString("floatTool");
                        if (floatTool.equals("FLOAT_NONE")) {
                            Log.d(TAG, "sava changed float none");
                            SPThreshholdUtil.setSharedPreferencesData(getApplicationContext(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_FLOAT_TOOL, false);
                        } else if (floatTool.equals("FLOAT_TOP")) {
                            Log.d(TAG, "sava changed float top");
                            SPThreshholdUtil.setSharedPreferencesData(getApplicationContext(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_FLOAT_TOOL, true);
                        } else {
                            Log.d(TAG, "sava changed float others");
                            SPThreshholdUtil.setSharedPreferencesData(getApplicationContext(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_FLOAT_TOOL, true);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            });
            //获取迪恩杰系统配置信息，确认悬浮窗状态
            domParse();
        }
    }

    public void domParse() {
        Log.d(TAG, "di en jie gong ban");
        try {
            FileInputStream fileInputStream = new FileInputStream(new File("/bootlogo/config/sr_txz.xml"));
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            while (true) {
                String s = bufferedReader.readLine();
                if (s == null)
                    return;
                if (s.contains("float_tool")) {
                    String[] split = s.split("<|>");
                    for (int i = 0; i < split.length; i++) {
                        Log.d("kayle", "split--->" + split[i]);
                    }
                    if (split[1].equals("FLOAT_NONE")) {
                        Log.d(TAG, "sava changed float none");
                        SPThreshholdUtil.setSharedPreferencesData(getApplicationContext(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_FLOAT_TOOL, false);
                    } else {
                        Log.d(TAG, "sava changed float top");
                        SPThreshholdUtil.setSharedPreferencesData(getApplicationContext(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_FLOAT_TOOL, true);
                    }
                }
                Log.d("kayle", "--->" + s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 迪恩杰专用
     *
     * @param context
     */
    private void bindServiceDienjie(Context context) {
        try {
            Intent intent = new Intent();
            intent.setAction("imotor.intent.action.CONTROL_FLOAT_VIEW");
            intent.setPackage("com.clw.recognition");
            context.startService(intent);
            context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
