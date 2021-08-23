package com.txznet.music.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.KeyEvent;
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
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.UIHelper;
import com.txznet.music.utils.Utils;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity1;
//import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity2;

public class SplashActivity extends BaseActivity {

    private static final String TAG = "Music:Splash:";
    public static boolean sIsFirstLaunch = true;
    private int requestId;

    private int jumpToType = HomeActivity.mTabIndex;
    private HomeWatcherReceiver mHomeWatcherReceiver = null;


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
        mHomeWatcherReceiver = new HomeWatcherReceiver();
        IntentFilter filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(mHomeWatcherReceiver, filter);

        if (getIntent() != null && getIntent().getExtras() != null) {
            jumpToType = getIntent().getExtras().getInt(Utils.KEY_TYPE, HomeActivity.DEFAULT_TYPE);
        }


    }

    private RequestCallBack<RespCheck> callBack = new RequestCallBack<RespCheck>(RespCheck.class) {
        @Override
        public void onResponse(RespCheck data) {
            SharedPreferencesUtils.setConfig(JsonHelper.toJson(data));
            Utils.forceRefreshConfig(data);
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
            if (mHomeWatcherReceiver != null) {
                try {
                    unregisterReceiver(mHomeWatcherReceiver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            LogUtil.d(TAG + "test:restart:");
            Intent intent = new Intent(SplashActivity.this, ReserveConfigSingleTaskActivity1.class);
            if (jumpToType >= 0) {
                intent.putExtra(Utils.KEY_TYPE, jumpToType);
            }
            startActivity(intent);
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    overridePendingTransition(0, 0);
                    finish();
                }
            }, 2000);
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
        super.onDestroy();
//        setContentView(R.layout.layout_null);
        NetManager.getInstance().cancelRequest(requestId);
        Runtime.getRuntime().gc();
        if (mHomeWatcherReceiver != null) {
            try {
                unregisterReceiver(mHomeWatcherReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public int getLayout() {
        return R.layout.activity_splash;
    }

    /**
     * 处理在启动页面点返回键回到桌面，之后会跳到homeActivity
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.moveTaskToBack(true);
            UIHelper.exit();
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 处理在启动页面点Home键回到桌面，之后会跳到homeActivity
     */
    public class HomeWatcherReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d("HomeWatcherReceiver", "accept this broadcast receiver..");
            SplashActivity.this.moveTaskToBack(true);
            UIHelper.exit();
        }
    }

}
