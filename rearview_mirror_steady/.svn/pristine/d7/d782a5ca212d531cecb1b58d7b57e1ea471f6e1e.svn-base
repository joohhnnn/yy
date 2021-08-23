package com.txznet.txzsetting.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IdRes;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.txznet.txzsetting.R;
import com.txznet.txzsetting.TXZApplication;
import com.txznet.txzsetting.data.SettingData;
import com.txznet.txzsetting.util.JsonIntentUtil;
import com.txznet.txzsetting.util.TxzReportUtil;

public class SetPoiMapModeActivity extends Activity {

    public static final String TAG = SetPoiMapModeActivity.class.getSimpleName();
    public static final int RESULT_CODE_SET_POI_MODE_ACTIVITY = 2;

    private RadioButton mPoiModeList, mPoiModeBlend;
    private RadioGroup mPoiModeRadioGroup;
    private Button mSetPoiModeReturn;

    private SettingData mSettingDataUser;

    private interface WHAT_POI_MODE {
        int WHAT_INIT_UI = 0x2004;//初始化UI
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mSettingDataUser == null) {
                Intent intent = SetPoiMapModeActivity.this.getIntent();
                mSettingDataUser = (SettingData) intent.getSerializableExtra("settingdata");
            }
            switch (msg.what) {
                case WHAT_POI_MODE.WHAT_INIT_UI:
                    initRadioButtonView(mPoiModeList, R.id.setting_poi_mode_list);
                    initRadioButtonView(mPoiModeBlend, R.id.setting_poi_mode_blend);
                    int poiMode = mSettingDataUser.getPoiMode();
                    initChecked(poiMode);
                    Log.d(TAG, "poi mode == " + poiMode);

                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_poi_map_mode);
        TXZApplication.getApp().addActivity(this);
        init();
        handler.sendEmptyMessage(WHAT_POI_MODE.WHAT_INIT_UI);
    }

    private void init() {
        mPoiModeRadioGroup = (RadioGroup) findViewById(R.id.setting_poi_mode_radiogroup);
        mPoiModeRadioGroup.setOnCheckedChangeListener(mPoiModeRadioGroupListener);
        mPoiModeBlend = (RadioButton) findViewById(R.id.setting_poi_mode_blend);
        mPoiModeList = (RadioButton) findViewById(R.id.setting_poi_mode_list);
        mSetPoiModeReturn = (Button) findViewById(R.id.actionbar_return_setting);
        mSetPoiModeReturn.setOnClickListener(mReturnListener);
    }

    private View.OnClickListener mReturnListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onBackPressed();
        }
    };


    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        if (mPoiModeList.isChecked()) {
            bundle.putInt("poimode", 1);
        } else if (mPoiModeBlend.isChecked()) {
            bundle.putInt("poimode", 2);
        } else {
            bundle.putInt("poimode", 0);
        }
        intent.putExtras(bundle);
        setResult(RESULT_CODE_SET_POI_MODE_ACTIVITY, intent);
        super.onBackPressed();
    }

    private RadioGroup.OnCheckedChangeListener mPoiModeRadioGroupListener = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {
            switch (checkedId) {
                case R.id.setting_poi_mode_blend:
                    mSettingDataUser.setPoiMode(JsonIntentUtil.JSON_POI_MODE_BLEND);
                    TxzReportUtil.doReportPoiMapMode(TxzReportUtil.POI_MODE_BLEND);
                    break;
                case R.id.setting_poi_mode_list:
                    mSettingDataUser.setPoiMode(JsonIntentUtil.JSON_POI_MODE_LIST);
                    TxzReportUtil.doReportPoiMapMode(TxzReportUtil.POI_MODE_LIST);
                    break;
            }
            JsonIntentUtil.getInstance().sendTXZSettingBroadcast(SetPoiMapModeActivity.this, mSettingDataUser);
        }
    };


    /**
     * 初始化RadioButton的属性
     *
     * @param view
     */
    private void initRadioButtonView(RadioButton view, int ResId) {
        WindowManager winManager = this.getWindowManager();
        int width = winManager.getDefaultDisplay().getWidth();
        int height = winManager.getDefaultDisplay().getHeight();
        int boundsWidthH = (int) (width/2.5);//横屏宽
        int boundsHeightH = (int) (height/1.8);//横屏高
        int boundsWidthV = (int) (width/1.5);//竖屏宽
        int boundsHeightV = (int) (height/4);//竖屏高


        Log.d(TAG, "width = " + width + ",height = " + width);
        Drawable drawable = null;
        switch (ResId) {
            case R.id.setting_poi_mode_list:
                Log.d(TAG, "initRadioButtonView setting_poi_mode_list");
                if (width < height) {//竖屏
                    drawable = getResources().getDrawable(R.drawable.selector_poi_mode_list_vertical);
                    mPoiModeRadioGroup.setOrientation(LinearLayout.VERTICAL);
                    drawable.setBounds(0, 0, (int)((drawable.getMinimumWidth()*boundsHeightV)/drawable.getMinimumHeight()), boundsHeightV);
                } else {//横屏
                    drawable = getResources().getDrawable(R.drawable.selector_poi_mode_list);
                    mPoiModeRadioGroup.setOrientation(LinearLayout.HORIZONTAL);
                    drawable.setBounds(0, 0, boundsWidthH, (int) ((drawable.getMinimumHeight() * boundsWidthH) / drawable.getMinimumWidth()));
                }
                double ddd = drawable.getMinimumWidth() / drawable.getMinimumHeight();
                double www = boundsWidthH / ((drawable.getMinimumHeight() * boundsWidthH) / drawable.getMinimumWidth());

                Log.d(TAG, "drawable.getMinimumWidth() = " + drawable.getMinimumWidth() + ",drawable.getMinimumHeight() = " + drawable.getMinimumHeight() + ",dddddddddddd = " + ddd + ",wwwwwwwww = " + www);

                break;
            case R.id.setting_poi_mode_blend:
                Log.d(TAG, "initRadioButtonView setting_poi_mode_blend");
                if (width < height) {//竖屏
                    drawable = getResources().getDrawable(R.drawable.selector_poi_mode_blend_vertical);
                    mPoiModeRadioGroup.setOrientation(LinearLayout.VERTICAL);
                    drawable.setBounds(0, 0, (int)((drawable.getMinimumWidth()*boundsHeightV)/drawable.getMinimumHeight()), boundsHeightV);

                } else {//横屏
                    drawable = getResources().getDrawable(R.drawable.selector_poi_mode_blend);
                    mPoiModeRadioGroup.setOrientation(LinearLayout.HORIZONTAL);
                    drawable.setBounds(0, 0, boundsWidthH, (int) ((drawable.getMinimumHeight() * boundsWidthH) / drawable.getMinimumWidth()));
                }
                break;
        }
        view.setPadding(0,0,0,15);
        view.setCompoundDrawables(null, drawable, null, null);
    }

    /**
     * 设置当前哪个被选中
     *
     * @param checkedId
     */
    private void initChecked(int checkedId) {
        switch (checkedId) {
            case JsonIntentUtil.JSON_POI_MODE_LIST:
                mPoiModeList.setChecked(true);
                mPoiModeBlend.setChecked(false);
                break;
            case JsonIntentUtil.JSON_POI_MODE_BLEND:
                mPoiModeList.setChecked(false);
                mPoiModeBlend.setChecked(true);
                break;
            default:
                mPoiModeList.setChecked(false);
                mPoiModeBlend.setChecked(false);
                break;
        }
    }
}
