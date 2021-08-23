package com.txznet.music.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.R;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.ui.net.response.RespCheck;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity1;
//import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity2;

public class SplashActivity extends BaseActivity {

    private static final String TAG = "Music:Splash:";
    public static boolean sIsFirstLaunch = true;
    private int requestId;

    @Override
    protected String getActivityTag() {
        return "SplashActivity";
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        RelativeLayout rlRoot = (RelativeLayout) findViewById(R.id.rl_root);
        ImageView ivSplash = (ImageView) findViewById(R.id.iv_splash);
        rlRoot.setBackground(getResources().getDrawable(R.drawable.bg_splash));
        ivSplash.setImageDrawable(getResources().getDrawable(R.drawable.bg_welcome));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.logd(TAG + "onCreate");
        //请求网络数据
        requestId = NetManager.getInstance().requestTag(callBack);

    }

    private RequestCallBack<RespCheck> callBack = new RequestCallBack<RespCheck>(RespCheck.class) {
        @Override
        public void onResponse(RespCheck data) {
            SharedPreferencesUtils.setConfig(JsonHelper.toJson(data));
        }

        @Override
        public void onError(String cmd, Error error) {
            LogUtil.e(TAG + "request tag error " + error.getErrorCode());
        }
    };

    @Override
    protected void onStart() {

        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG + "onResume sIsFirstLaunch:" + sIsFirstLaunch);
        int time = 0;
        if (sIsFirstLaunch) {
            time = 2000;
            sIsFirstLaunch = false;
        }
        AppLogic.removeUiGroundCallback(mRunnableJump);
        AppLogic.runOnUiGround(mRunnableJump, time);
    }

    private Runnable mRunnableJump = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(TAG + "test:restart:");
            Intent intent = new Intent(SplashActivity.this, ReserveConfigSingleTaskActivity1.class);
            startActivity(intent);
            finish();
        }
    };

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        setContentView(R.layout.layout_null);
        NetManager.getInstance().cancelRequest(requestId);
        super.onDestroy();
        Runtime.getRuntime().gc();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_splash;
    }

}
