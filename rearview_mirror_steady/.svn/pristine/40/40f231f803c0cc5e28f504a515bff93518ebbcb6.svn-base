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

import com.txznet.txzsetting.R;
import com.txznet.txzsetting.TXZApplication;
import com.txznet.txzsetting.activity.MainActivity;
import com.txznet.txzsetting.data.SettingData;
import com.txznet.txzsetting.util.JsonIntentUtil;
import com.txznet.txzsetting.util.TxzReportUtil;

/**
 * Created by ASUS User on 2017/6/3.
 */

public class DialogThreshhold extends Dialog {
    public static final String TAG = DialogThreshhold.class.getSimpleName();
    private SettingData mSettingData = null;
    private Context mContext;


    private CheckBox mCheckboxHighVery, mCheckboxHigh, mCheckboxLowVery, mCheckboxLow, mCheckboxNormal;
    private TextView mCheckboxHighVeryShow, mCheckboxHighShow, mCheckboxLowVeryShow, mCheckboxLowShow, mCheckboxNormalShow;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT.WHAT_CHECK_BOX_HIGH_VERY:
                    setItemChekBoxTrue(mCheckboxHighVery);
                    break;
                case WHAT.WHAT_CHECK_BOX_HIGH:
                    setItemChekBoxTrue(mCheckboxHigh);
                    break;
                case WHAT.WHAT_CHECK_BOX_LOW_VERY:
                    setItemChekBoxTrue(mCheckboxLowVery);
                    break;
                case WHAT.WHAT_CHECK_BOX_LOW:
                    setItemChekBoxTrue(mCheckboxLow);
                    break;
                case WHAT.WHAT_CHECK_BOX_NORMAL:
                    setItemChekBoxTrue(mCheckboxNormal);
                    break;

            }
        }

    };


    public interface DialogCheckBoxListener {
        public void refreshDialogCheckBoxUI(String string);
    }

    private DialogCheckBoxListener listener;

    public DialogThreshhold(Context context, int themeResId) {
        super(context, themeResId);
        this.mContext = context;
    }

    public DialogThreshhold(Context context, int themeResId, DialogCheckBoxListener listener) {
        super(context, themeResId);
        this.mContext = context;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        //获得dialog的window窗口
//        Window window = getWindow();
//        window.setGravity(Gravity.CENTER);
////        window.getDecorView().setPadding(0, 0, 0, 0);
//        //获得window窗口的属性
//        WindowManager.LayoutParams lp = window.getAttributes();
//        WindowManager wm = (WindowManager) getContext()
//                .getSystemService(Context.WINDOW_SERVICE);
//        int width = wm.getDefaultDisplay().getWidth();
//        int height = wm.getDefaultDisplay().getHeight();
//        lp.width = (int) (width * 0.6);
//        lp.height = (int) (height * 0.8);
////        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        window.setAttributes(lp);
        this.setContentView(R.layout.threshhold_show);
        init();
    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
//        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
//        layoutParams.gravity= Gravity.BOTTOM;
//        layoutParams.width= WindowManager.LayoutParams.MATCH_PARENT;
//        layoutParams.height= WindowManager.LayoutParams.WRAP_CONTENT;
//        getWindow().getDecorView().setPadding(0, 0, 0, 0);
//        getWindow().setAttributes(layoutParams);
    }

    private static interface WHAT {
        int WHAT_CHECK_BOX_HIGH_VERY = 0x0001;
        int WHAT_CHECK_BOX_HIGH = 0x0002;
        int WHAT_CHECK_BOX_LOW_VERY = 0x0003;
        int WHAT_CHECK_BOX_LOW = 0x0004;
        int WHAT_CHECK_BOX_NORMAL = 0x0005;
    }


    private void init() {
        mSettingData = MainActivity.getSettingData();
        mCheckboxHighVeryShow = (TextView) findViewById(R.id.threshhold_dialog_checkbox_highvery_show);
        mCheckboxHighVeryShow.setOnClickListener(mCheckboxHighVeryListene);
        mCheckboxHighVery = (CheckBox) findViewById(R.id.threshhold_dialog_checkbox_highvery);
        mCheckboxHighVery.setOnClickListener(mCheckboxHighVeryListene);

        mCheckboxHighShow = (TextView) findViewById(R.id.threshhold_dialog_checkbox_high_show);
        mCheckboxHighShow.setOnClickListener(mCheckboxHighListene);
        mCheckboxHigh = (CheckBox) findViewById(R.id.threshhold_dialog_checkbox_high);
        mCheckboxHigh.setOnClickListener(mCheckboxHighListene);

        mCheckboxLowShow = (TextView) findViewById(R.id.threshhold_dialog_checkbox_low_show);
        mCheckboxLowShow.setOnClickListener(mCheckboxLowListene);
        mCheckboxLow = (CheckBox) findViewById(R.id.threshhold_dialog_checkbox_low);
        mCheckboxLow.setOnClickListener(mCheckboxLowListene);

        mCheckboxLowVeryShow = (TextView) findViewById(R.id.threshhold_dialog_checkbox_lowvery_show);
        mCheckboxLowVeryShow.setOnClickListener(mCheckboxLowVeryListene);
        mCheckboxLowVery = (CheckBox) findViewById(R.id.threshhold_dialog_checkbox_lowvery);
        mCheckboxLowVery.setOnClickListener(mCheckboxLowVeryListene);

        mCheckboxNormalShow = (TextView) findViewById(R.id.threshhold_dialog_checkbox_normal_show);
        mCheckboxNormalShow.setOnClickListener(mCheckboxNormalListene);
        mCheckboxNormal = (CheckBox) findViewById(R.id.threshhold_dialog_checkbox_normal);
        mCheckboxNormal.setOnClickListener(mCheckboxNormalListene);

//修改为读文件方式，废弃SPThreshholdUtil方式
//        String threshholdData = SPThreshholdUtil.getThreshholdData(TXZApplication.getApp());
        String threshholdData = MainActivity.getShowThreshhold();
        Log.d(TAG, "threshholdData = " + threshholdData);
        if (threshholdData.equals("")) {
            threshholdData = "正常";
        }

        switch (threshholdData) {
            case "极高":
                setCheckdTrue(mCheckboxHighVery);
                break;
            case "高":
                setCheckdTrue(mCheckboxHigh);
                break;
            case "正常":
                setCheckdTrue(mCheckboxNormal);
                break;
            case "低":
                setCheckdTrue(mCheckboxLow);
                break;
            case "极低":
                setCheckdTrue(mCheckboxLowVery);
                break;
        }
    }


    private View.OnClickListener mCheckboxHighVeryListene = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            mHandler.obtainMessage(WHAT.WHAT_CHECK_BOX_HIGH_VERY).sendToTarget();
        }
    };
    private View.OnClickListener mCheckboxHighListene = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            mHandler.obtainMessage(WHAT.WHAT_CHECK_BOX_HIGH).sendToTarget();
        }
    };
    private View.OnClickListener mCheckboxLowListene = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            mHandler.obtainMessage(WHAT.WHAT_CHECK_BOX_LOW).sendToTarget();
        }
    };
    private View.OnClickListener mCheckboxLowVeryListene = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            mHandler.obtainMessage(WHAT.WHAT_CHECK_BOX_LOW_VERY).sendToTarget();
        }
    };
    private View.OnClickListener mCheckboxNormalListene = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            mHandler.obtainMessage(WHAT.WHAT_CHECK_BOX_NORMAL).sendToTarget();
        }
    };

    private void setItemChekBoxTrue(CheckBox chekBoxTrue) {
        mSettingData = MainActivity.getSettingData();
        if (mSettingData == null) {
            Log.d(TAG, "SettingData = " + mSettingData);
            return;
        }
        switch (chekBoxTrue.getId()) {
            case R.id.threshhold_dialog_checkbox_high:
                Log.d(TAG, "高灵敏度");
                setCheckdTrue(mCheckboxHigh);
                listener.refreshDialogCheckBoxUI("高");
                TxzReportUtil.doReportThreshhold(mSettingData.getThreshhold(),JsonIntentUtil.JSON_HIGH);
                mSettingData.setThreshhold(JsonIntentUtil.JSON_HIGH);


                Log.d(TAG, "xxxxxxxxxxxxxxxxxxxxxxxx" + mSettingData.getThreshhold() + "ttttttttttttttttt" + JsonIntentUtil.JSON_HIGH);
//                TXZConfigManager.getInstance().setAsrWakeupThreshhold(SPThreshholdUtil.THRESHHOLD_HIGH);
//                SPThreshholdUtil.setSharedPreferencesData(TXZApplication.getApp(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_THRESHHOLD,SPThreshholdUtil.THRESHHOLD_HIGH);
                break;
            case R.id.threshhold_dialog_checkbox_highvery:
                Log.d(TAG, "极高灵敏度");
                setCheckdTrue(mCheckboxHighVery);
                listener.refreshDialogCheckBoxUI("极高");
                TxzReportUtil.doReportThreshhold(mSettingData.getThreshhold(),JsonIntentUtil.JSON_HIGH_VERY);
                mSettingData.setThreshhold(JsonIntentUtil.JSON_HIGH_VERY);

//                TXZConfigManager.getInstance().setAsrWakeupThreshhold(SPThreshholdUtil.THRESHHOLD_HIGH_VERY);
//                SPThreshholdUtil.setSharedPreferencesData(TXZApplication.getApp(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_THRESHHOLD,SPThreshholdUtil.THRESHHOLD_HIGH_VERY);
                break;
            case R.id.threshhold_dialog_checkbox_low:
                Log.d(TAG, "低灵敏度");
                setCheckdTrue(mCheckboxLow);
                listener.refreshDialogCheckBoxUI("低");
                TxzReportUtil.doReportThreshhold(mSettingData.getThreshhold(),JsonIntentUtil.JSON_LOW);
                mSettingData.setThreshhold(JsonIntentUtil.JSON_LOW);

//                TXZConfigManager.getInstance().setAsrWakeupThreshhold(SPThreshholdUtil.THRESHHOLD_LOW);
//                SPThreshholdUtil.setSharedPreferencesData(TXZApplication.getApp(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_THRESHHOLD,SPThreshholdUtil.THRESHHOLD_LOW);
                break;
            case R.id.threshhold_dialog_checkbox_lowvery:
                Log.d(TAG, "极低灵敏度");
                setCheckdTrue(mCheckboxLowVery);
                listener.refreshDialogCheckBoxUI("极低");
                TxzReportUtil.doReportThreshhold(mSettingData.getThreshhold(),JsonIntentUtil.JSON_LOW_VERY);
                mSettingData.setThreshhold(JsonIntentUtil.JSON_LOW_VERY);

//                TXZConfigManager.getInstance().setAsrWakeupThreshhold(SPThreshholdUtil.THRESHHOLD_LOW_VERY);
//                SPThreshholdUtil.setSharedPreferencesData(TXZApplication.getApp(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_THRESHHOLD,SPThreshholdUtil.THRESHHOLD_LOW_VERY);
                break;
            case R.id.threshhold_dialog_checkbox_normal:
                Log.d(TAG, "正常灵敏度");
                setCheckdTrue(mCheckboxNormal);
                listener.refreshDialogCheckBoxUI("正常");
                TxzReportUtil.doReportThreshhold(mSettingData.getThreshhold(),JsonIntentUtil.JSON_NORMAL);
                mSettingData.setThreshhold(JsonIntentUtil.JSON_NORMAL);

//                TXZConfigManager.getInstance().setAsrWakeupThreshhold(SPThreshholdUtil.THRESHHOLD_NORMAL);
//                SPThreshholdUtil.setSharedPreferencesData(TXZApplication.getApp(), SPThreshholdUtil.SP_NAME_SETTING, SPThreshholdUtil.SP_KEY_THRESHHOLD,SPThreshholdUtil.THRESHHOLD_NORMAL);
                break;
        }
        JsonIntentUtil.getInstance().sendTXZSettingBroadcast(TXZApplication.getApp(), mSettingData);
        dismiss();
    }


    /**
     * 设置当前哪个CheckBox为true
     *
     * @param checkdTrue
     */
    private void setCheckdTrue(CheckBox checkdTrue) {
        mCheckboxNormal.setChecked(false);
        mCheckboxLowVery.setChecked(false);
        mCheckboxLow.setChecked(false);
        mCheckboxHighVery.setChecked(false);
        mCheckboxHigh.setChecked(false);
        checkdTrue.setChecked(true);
    }
}
