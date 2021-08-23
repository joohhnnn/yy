package com.txznet.music.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.R;
import com.txznet.music.albumModule.logic.BackgroundManager;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.SharedPreferencesUtils;

/**
 * @author telenewbie
 * @version 创建时间：2016年4月18日 下午5:11:15
 */
public abstract class BaseActivity extends com.txznet.comm.base.BaseActivity implements BackgroundManager.BackgroundChangedListener {

    public static final String TAG = "MUSIC:Activity: ";
    private TextView mTestMark;


    protected abstract String getActivityTag();

    public abstract void bindViews(Bundle savedInstanceState);

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.d(TAG, getActivityTag() + "onConfigurationChanged");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, getActivityTag() + ":onCreate");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        if (SharedPreferencesUtils.getFullScreen()) {
            setFullScreen(this);
        }
        Intent intent = new Intent();
        intent.setAction("com.sysom.multidisplay.bind");
        sendBroadcast(intent);

        ScreenUtils.initScreenType(this, false);
        if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_CHEJI) {
            setTheme(R.style.AppThemeCheji);
            GlobalContext.get().setTheme(R.style.AppThemeCheji);
        } else if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_HOUSHIJING_SHORT) {
            setTheme(R.style.AppThemeShortHoushijing);
            GlobalContext.get().setTheme(R.style.AppThemeShortHoushijing);
        } else if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_VERTICAL) {
            setTheme(R.style.AppThemeVertical);
            GlobalContext.get().setTheme(R.style.AppThemeVertical);
        } else {
            setTheme(R.style.AppThemeHoushijing);
            GlobalContext.get().setTheme(R.style.AppThemeHoushijing);
        }

        setContentView(getLayout());

        bindViews(savedInstanceState);

//        if (getBg() != null) {
//            if (PlayInfoManager.getInstance().getCurrentAlbum() != null) {
//                GlideImageLoader.setActBg(this, PlayInfoManager.getInstance().getCurrentAlbum().getLogo(), getBg());
//            }
//        }
        BackgroundManager.getInstance().addBackgroundChangedListener(this);
        if(null != getBg()){
            getBg().setImageBitmap(BackgroundManager.getInstance().getAlbumBackground());
        }
    }


    /**
     * 背景图片
     *
     * @return
     */
    public ImageView getBg() {
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.d(TAG, getActivityTag() + ":onStart");

    }


    public void setFullScreen(Activity activity) {
        //由于全屏会遮挡部分设备的虚拟按键，导致无法返回，但是已经发布过版本，所以仍保留这个接口但是不执行全屏的操作
//		activity.getWindow().getDecorView().setSystemUiVisibility(
//				View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//						| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//						| View.SYSTEM_UI_FLAG_FULLSCREEN
//						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
    }

    @Override
    public void onWindowFocusChanged(boolean newFocus) {
        if (newFocus) {
            if (SharedPreferencesUtils.getFullScreen()) {
                setFullScreen(this);
            }
        }
        super.onWindowFocusChanged(newFocus);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, getActivityTag() + ":onResume");

        Constant.setIsExit(false);
        if (Constant.ISTESTDATA) {
//			printTestMark();
        }
    }

    @Override
    protected void onPause() {
        LogUtil.d(TAG, getActivityTag() + ":onPause");
        if (mTestMark != null) {
            getWindowManager().removeView(mTestMark);
            mTestMark = null;
        }
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.d(TAG, getActivityTag() + ":onStop");
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, getActivityTag() + ":onDestroy");

        BackgroundManager.getInstance().removeBackgroundChangedListener(this);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        try {
            InputMethodManager.class.getDeclaredMethod("windowDismissed", IBinder.class).invoke(imm,
                    getWindow().getDecorView().getWindowToken());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getBg() != null) {
            getBg().setImageBitmap(null);
        }
        System.gc();


        super.onDestroy();
    }

    private void printTestMark() {
        try {
            if (mTestMark != null) {
                return;
            }
            mTestMark = new TextView(this);
            mTestMark.setText("此版本为内部测试版");
            mTestMark.setTextSize(16);
            mTestMark.setTextColor(Color.parseColor("#ccffffff"));
            WindowManager.LayoutParams mLp = new WindowManager.LayoutParams();
            int w = View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED);
            int h = View.MeasureSpec.makeMeasureSpec(0,
                    View.MeasureSpec.UNSPECIFIED);
            mTestMark.measure(w, h);
            mLp.width = mTestMark.getMeasuredWidth();
            mLp.height = mTestMark.getMeasuredHeight();
            mLp.flags = 40;
            mLp.format = PixelFormat.RGBA_8888;
            mLp.gravity = Gravity.LEFT | Gravity.TOP;
            mLp.x = 10;
            mLp.y = 10;
            getWindowManager().addView(mTestMark, mLp);
        } catch (Exception e) {
        }
    }

    public abstract int getLayout();

    @Override
    public void onBackgroundChanged(Bitmap bitmap) {
        if (null != getBg()) {
            getBg().setImageBitmap(bitmap);
        }
    }
}
