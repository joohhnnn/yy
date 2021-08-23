package com.txznet.music.ui.base;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleRegistry;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.util.FileConfigUtil;
import com.txznet.music.util.GcTrigger;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ScreenUtils;
import com.txznet.txz.util.TXZFileConfigUtil;

import butterknife.ButterKnife;

/**
 * @author telenewbie
 * @version 创建时间：2016年4月18日 下午5:11:15
 */
//public abstract class BaseActivity extends PluginFragmentActivity {
public abstract class BaseActivity extends com.txznet.comm.base.BaseActivity {

    public static final String TAG = Constant.LOG_TAG_ACTIVITY;
    private LifecycleRegistry mLifecycleRegistry = new LifecycleRegistry(this);

    private View mRoot;

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return mLifecycleRegistry;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawable(null);

        getTheme().applyStyle(R.style.AppTheme, true);

        // 屏幕类型,1车机,2车镜,3短屏后视镜,4车机竖屏，5手机竖屏
        if (TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_SCREEN_TYPE, 1) == 5) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        Logger.d(TAG, getActivityTag() + ":onCreate");
        compatSysom();

        mRoot = findViewById(android.R.id.content);
        mRoot.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);
    }


    protected void initContentView() {
        setContentView(getLayout());
        ButterKnife.bind(this);
        initView();
    }

    private int lastW, lastH;
    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = () -> {
        int w = mRoot.getWidth();
        int h = mRoot.getHeight();
        if (w != 0 && h != 0 && (w != lastW || h != lastH)) {
            ScreenUtils.initScreenType(w, h, true);
            lastW = w;
            lastH = h;
        }
    };

    // FIXME 方案商需求，兼容
    private void compatSysom() {
        Intent intent = new Intent();
        intent.setAction("com.sysom.multidisplay.bind");
        sendBroadcast(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.d(TAG, getActivityTag() + ":onStart");
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(TAG, getActivityTag() + ":onResume");
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME);
    }

    @Override
    protected void onPause() {
        Logger.d(TAG, getActivityTag() + ":onPause");
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE);
        super.onPause();
    }

    @Override
    protected void onStop() {
        Logger.d(TAG, getActivityTag() + ":onStop");
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP);
        super.onStop();
        GcTrigger.runGc();
    }

    @Override
    protected void onDestroy() {
        Logger.d(TAG, getActivityTag() + ":onDestroy");
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        if (mRoot != null) {
            mRoot.getViewTreeObserver().removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }

        // 关闭软键盘
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            InputMethodManager.class.getDeclaredMethod("windowDismissed", IBinder.class).invoke(imm,
                    getWindow().getDecorView().getWindowToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
        ButterKnife.unbind(this);
        super.onDestroy();
    }

    /**
     * 获取ActivityTag标签，影响日志打印
     */
    protected abstract String getActivityTag();

    /**
     * 获取布局id
     */
    public abstract int getLayout();

    /**
     * 初始化界面
     */
    public abstract void initView();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Logger.d(TAG, getActivityTag() + "onConfigurationChanged");
    }

    @Override
    public void recreate() {
        Logger.d(TAG, getActivityTag() + "recreate");
        try {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                fragmentTransaction.remove(fragment);
            }
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception e) {
        }
        super.recreate();
    }

    @Override
    protected void setTheme() {
        try {
            // 主题.默认为0透明主题,1,不透明 2 不透明+全屏
            int screenStyle = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_SCREEN_STYLE, 0);
            ScreenUtils.initScreenType(this, false);
            Logger.d(TAG, "screenStyleTheme:" + screenStyle + " , screenType:" + ScreenUtils.getScreenType());
            if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_CHEJI) {
                setTheme(R.style.AppTheme_CheJi);
                GlobalContext.get().setTheme(R.style.AppTheme_CheJi);
            } else if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_HOUSHIJING_LONG) {
                setTheme(R.style.AppTheme_LongHouShiJing);
                GlobalContext.get().setTheme(R.style.AppTheme_LongHouShiJing);
            } else if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_HOUSHIJING_SHORT) {
                setTheme(R.style.AppTheme_ShortHouShiJing);
                GlobalContext.get().setTheme(R.style.AppTheme_ShortHouShiJing);
            } else {
                setTheme(R.style.AppTheme);
                GlobalContext.get().setTheme(R.style.AppTheme);
            }
        } catch (Exception e) {
        }
    }
}
