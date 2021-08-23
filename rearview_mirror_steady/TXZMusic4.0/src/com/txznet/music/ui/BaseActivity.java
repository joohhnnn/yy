package com.txznet.music.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.loader.UIConfig;
import com.txznet.music.R;
import com.txznet.music.albumModule.logic.BackgroundManager;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.Objects;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * @author telenewbie
 * @version 创建时间：2016年4月18日 下午5:11:15
 */
public abstract class BaseActivity extends com.txznet.comm.base.BaseActivity implements BackgroundManager.BackgroundChangedListener {

    public static final String TAG = "MUSIC:Activity: ";
    private TextView mTestMark;
    private static HashMap<URL, JarFile> jarCache;


    private static UIConfig mUIConfig;

    private void refreshWindowParams(boolean enableRestart) {
        UIConfig uiConfig = AppLogic.mUIConfig;
        //如果有值已改制为准
        if (uiConfig == null) {
//        如果没有值则以文件设置为准
            uiConfig = new UIConfig();
            uiConfig.x = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_UI_WINDOW_X, 0);
            uiConfig.y = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_UI_WINDOW_Y, 0);
            uiConfig.width = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_UI_WINDOW_WIDTH, 0);
            uiConfig.height = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_UI_WINDOW_HEIGHT, 0);
            uiConfig.gravity = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_UI_WINDOW_GRAVITY, 0);
            if (uiConfig.width == 0 || uiConfig.height == 0) {
                Log.d(TAG, "refreshWindowParams: :error");
                return;
            }

        } else {

        }


        if (null != uiConfig) {
            LogUtil.e("AppBaseActivity", "ui config: " + uiConfig.toString());
            boolean sizeChanged = checkUISizeChanged(uiConfig);
            mUIConfig = uiConfig;
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.width = mUIConfig.width;
            lp.height = mUIConfig.height;
            lp.x = mUIConfig.x;
            lp.y = mUIConfig.y;
            lp.gravity = mUIConfig.gravity;
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
//            com.txznet.comm.util.ScreenUtils.updateScreenSize(mUIConfig.width, mUIConfig.height, true);
            getWindow().setAttributes(lp);

            if (sizeChanged && enableRestart) {
                recreate();
            }
        }
    }

    private boolean checkUISizeChanged(UIConfig newConfig) {
        if (null == mUIConfig) {
            return true;
        }
        return !(mUIConfig.width == newConfig.width && mUIConfig.height == newConfig.height);
    }

    static {
        try {
            String urlStr = "libcore.net.url.JarURLConnectionImpl";
            Class<?> jarURLConnectionImplClass = Class.forName(urlStr);
            if (jarURLConnectionImplClass == null) {
                urlStr = "org.apache.harmony.luni.internal.net.www.protocol.jar.JarURLConnectionImpl";
                jarURLConnectionImplClass = Class.forName(urlStr);
            }

            final Field jarCacheField = jarURLConnectionImplClass.getDeclaredField("jarCache");
            jarCacheField.setAccessible(true);
            //noinspection unchecked
            jarCache = (HashMap<URL, JarFile>) jarCacheField.get(null);
        } catch (Exception e) {
            // ignored
        }
    }

    protected abstract String getActivityTag();

    public abstract void bindViews(Bundle savedInstanceState);

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.d(TAG, getActivityTag() + "onConfigurationChanged");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_SCREEN_TYPE, 1) == 5) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, getActivityTag() + ":onCreate");

//        if (GlobalContext.get().getApplicationInfo().metaData != null) {
//            boolean skin_default_full_screen = GlobalContext.get().getApplicationInfo().metaData.getBoolean("SKIN_DEFAULT_FULL_SCREEN");
//            if (skin_default_full_screen) {
//            }
//        }
        if (SharedPreferencesUtils.getFullScreen()) {
            setFullScreen(this);
        }
        Intent intent = new Intent();
        intent.setAction("com.sysom.multidisplay.bind");
        sendBroadcast(intent);
//        setAppTheme();

        setContentView(getLayout());

        bindViews(savedInstanceState);

//        if (getBg() != null) {
//            if (PlayInfoManager.getInstance().getCurrentAlbum() != null) {
//                GlideImageLoader.setActBg(this, PlayInfoManager.getInstance().getCurrentAlbum().getLogo(), getBg());
//            }
//        }
        BackgroundManager.getInstance().addBackgroundChangedListener(this);
        if (null != getBg()) {
            getBg().setImageBitmap(BackgroundManager.getInstance().getAlbumBackground());
        }

        // HACK http://stackoverflow.com/questions/14610350/android-memory-leak-in-apache-harmonys-jarurlconnectionimpl
        if (jarCache != null) {
            try {
                for (final Iterator<Map.Entry<URL, JarFile>> iterator = jarCache.entrySet().iterator(); iterator.hasNext(); ) {
                    final Map.Entry<URL, JarFile> e = iterator.next();
                    final URL url = e.getKey();
                    Log.i(TAG, "Removing static hashmap entry for " + Objects.getObj2String(url));
                    if (Objects.getObj2String(url).endsWith(".apk") || Objects.getObj2String(url).endsWith(".jar")) {
                        try {
                            final JarFile jarFile = e.getValue();
                            jarFile.close();
                            iterator.remove();
                        } catch (Exception f) {
                            Log.e(TAG, "Error removing hashmap entry for " + url, f);
                        }
                    }
                }
            } catch (Exception e) {
                // ignored
            }
        }
        com.txznet.comm.util.ScreenUtils.addSceenSizeChangeListener(new com.txznet.comm.util.ScreenUtils.ScreenSizeChangeListener() {
            @Override
            public void onScreenSizeChange(int width, int height) {
                refreshWindowParams(true);

            }
        });


        View root = findViewById(android.R.id.content);
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int w = root.getWidth();
                int h = root.getHeight();
                if (w != 0 && h != 0 && (w != lastW || h != lastH)) {
                    onGlobalLayoutChanged(w, h);
                    lastW = w;
                    lastH = h;
                }
            }
        });
    }

    /**
     * 容器大小发生改变
     *
     * @param width  宽度
     * @param height 高度
     */
    protected void onGlobalLayoutChanged(int width, int height) {
        int screen_type_ori = ScreenUtils.getScreenType();
        ScreenUtils.initScreenType(width, height, true);
        if (ScreenUtils.getScreenType() != screen_type_ori) {
            ObserverManage.getObserver().send(InfoMessage.SCREEN_TYPE_CHANGED);
        }
    }

    private int lastW, lastH;


    @Override
    protected void setTheme() {
        setAppTheme();
    }

    protected void setAppTheme() {
        try {
            int screenStyle = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_SCREEN_STYLE, 0);
            ScreenUtils.initScreenType(this, false);
            Logger.d(TAG, "-->screenStyleTheme:" + screenStyle + " , screenType:" + ScreenUtils.getScreenType());
            if (screenStyle == 0) {
                if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_CHEJI) {
                    setTheme(R.style.AppThemeCheji_Translucent);
                    GlobalContext.get().setTheme(R.style.AppThemeCheji_Translucent);
                } else if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_HOUSHIJING_SHORT) {
                    setTheme(R.style.AppThemeShortHoushijing_Translucent);
                    GlobalContext.get().setTheme(R.style.AppThemeShortHoushijing_Translucent);
                } else if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_VERTICAL) {
                    setTheme(R.style.AppThemeVertical_Translucent);
                    GlobalContext.get().setTheme(R.style.AppThemeVertical_Translucent);
                } else {
                    setTheme(R.style.AppThemeHoushijing_Translucent);
                    GlobalContext.get().setTheme(R.style.AppThemeHoushijing_Translucent);
                }
            } else if (screenStyle == 1) {
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
            } else if (screenStyle == 2) {
                if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_CHEJI) {
                    setTheme(R.style.AppThemeCheji_fullScreen);
                    GlobalContext.get().setTheme(R.style.AppThemeCheji_fullScreen);
                } else if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_HOUSHIJING_SHORT) {
                    setTheme(R.style.AppThemeShortHoushijing_fullScreen);
                    GlobalContext.get().setTheme(R.style.AppThemeShortHoushijing_fullScreen);
                } else if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_VERTICAL) {
                    setTheme(R.style.AppThemeVertical_fullScreen);
                    GlobalContext.get().setTheme(R.style.AppThemeVertical_fullScreen);
                } else {
                    setTheme(R.style.AppThemeHoushijing_fullScreen);
                    GlobalContext.get().setTheme(R.style.AppThemeHoushijing_fullScreen);
                }
            }
            if (ScreenUtils.isPhonePortrait()) {
                setTheme(R.style.APPPhone_phone_Portrait);
            }

        } catch (Exception e) {
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
            printTestMark();
        }
        refreshWindowParams(true);
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
//            Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
            LogUtil.d(TAG, "test:::" + bitmap.getWidth() + ":" + bitmap.getHeight() + ",size = " + bitmap.getByteCount());
//            getBg().setImageBitmap(bitmap1);
            getBg().setImageBitmap(bitmap);

            if (Constant.ISTEST) {
                String s = FileUtils.saveBitmap(GlobalContext.get(), bitmap);
                LogUtil.d(TAG, "test:::" + s);
            }
        }
    }
}
