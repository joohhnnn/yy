package com.txznet.txz.ui.widget;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.util.DeviceInfo;
import com.txznet.txz.util.DeviceUtil;
import com.txznet.txz.util.PreferenceUtil;

import java.io.File;

public class SimAuthDialog extends WinDialog {

    private String mUrl;
    private WebView mWebView;
    private RelativeLayout llAuth;

    private static final String TAG = "SimAuthDialog::";

    private boolean loadError = false;
    private boolean bCancel = true;
    private boolean bNet = true;
    private int cancelTime = 0;

    private static SimAuthDialog mInstance;

    private SimAuthDialog(DialogBuildData data) {
        super(data);
    }

    public static void showAuthDialog(String url, Integer cancelTime, boolean bNet) {
        if(TextUtils.isEmpty(url)){
            return;
        }
        if (mInstance != null && mInstance.isShowing()) {
            mInstance.dismiss("repeat");
        }
        boolean bCancel = true;
        if(cancelTime != null && cancelTime == -1){
            bCancel = false;
        }
        mInstance = new SimAuthDialog(new DialogBuildData().setCancelable(false)
                .setCancelOutside(false).setFullScreen(false).setWindowType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
        mInstance.mUrl = url;
        mInstance.bCancel = bCancel;
        mInstance.cancelTime = cancelTime;
        mInstance.bNet = bNet;
        mInstance.show();
    }

    public static void closeAuthDialog(String reason) {
        if (mInstance != null && mInstance.isShowing()) {
            mInstance.dismiss(reason);
        }
    }

    @Override
    protected View createView() {
        LayoutInflater layoutInflater = super.mDialog.getLayoutInflater();
        mView = layoutInflater.inflate(R.layout.sim_auth_dialog, null, false);
        return mView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.logd(TAG + "onCreate");
        initWebview();
    }

    @Override
    protected void onShow() {
        Resources resources = GlobalContext.get().getResources();
        Integer width = (int) resources.getDimension(R.dimen.x450);
        Integer height = (int) resources.getDimension(R.dimen.y360);
        showUrl();
        super.mDialog.getWindow().setLayout(width, height);
        super.onShow();
    }

    @Override
    protected void onGetFocus() {
        super.onGetFocus();
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();
        mWebView.destroy();
        mWebView = null;
        addCancelTime();
        AppLogic.removeBackGroundCallback(runnableLoadingTimeout);
        GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeBoserver);
    }

    HomeObservable.HomeObserver mHomeBoserver = new HomeObservable.HomeObserver() {
        @Override
        public void onHomePressed() {
            LogUtil.logd(TAG + "Home pressed");
            if(bCancel){
                dismiss("home click");
            }
        }
    };

    private void addCancelTime() {
        int limit = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_SIM_AUTH_CANCEL_LIMIT,0);
        if(limit != 0){
            int cancelTime = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_SIM_AUTH_CANCEL_TIME, 0);
            cancelTime ++;
            PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_SIM_AUTH_CANCEL_TIME, cancelTime);
        }
    }

    private void showUrl() {
        LogUtil.logd(TAG + "showUrl = " + mUrl);
        mWebView.loadUrl(mUrl);
        AppLogic.runOnBackGround(runnableLoadingTimeout, 30 * 1000);
    }

    private void initWebview() {
        mWebView = (WebView) mView.findViewById(R.id.web_view_sim_auth);
        llAuth = (RelativeLayout) mView.findViewById(R.id.ll_auth);

        llAuth.setVisibility(View.GONE);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.addJavascriptInterface(new SimAuthDialog.Cross(), "cross");
        mWebView.getSettings().setDomStorageEnabled(true);
        mWebView.getSettings().setUseWideViewPort(true);
        String path = AppLogic.getApp().getDir("cache", Context.MODE_PRIVATE).getPath();
        mWebView.getSettings().setAppCachePath(path);
        String dbPath = AppLogic.getApp().getDir("databases", Context.MODE_PRIVATE).getPath();;
        mWebView.getSettings().setDatabasePath(dbPath);
        mWebView.getSettings().setDatabaseEnabled(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setScrollContainer(false);
        mWebView.setBackgroundColor(Color.BLACK);
        if (bNet) {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            LogUtil.logd(TAG + "web cache mode default");
        } else {
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            LogUtil.logd(TAG + "web cache mode use cache");
        }
        mWebView.getSettings().setAppCacheEnabled(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUserAgentString(
                "Mozilla/5.0 (Windows NT 6.2; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1667.0 Safari/537.36");
        mWebView.getSettings().setUserAgentString(
                "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Mobile Safari/537.36");
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView arg0, String arg1) {
                return false;
            }

            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                loadError = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                LogUtil.logd(TAG + "onPageFinished error = " + loadError);
                if (!loadError) {
//                    setCancelable(bCancel);
//                    setCanceledOnTouchOutside(bCancel);
//                    llAuth.setVisibility(View.VISIBLE);
                } else {
                    dismiss("load error");
                }
            }
        });

        Window window = super.mDialog.getWindow();
        WindowManager.LayoutParams mLayoutParams = window.getAttributes();
        mLayoutParams.dimAmount = 0f;
        window.setAttributes(mLayoutParams);
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        mWebView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onReceivedTitle(WebView view, String title) {
                //判断标题 title 中是否包含有“error”字段，如果包含“error”字段，则设置加载失败，显示加载失败的视图
                if (!TextUtils.isEmpty(title) && title.toLowerCase().contains("error")) {
                    loadError = true;
                }
            }

        });

        mWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        GlobalObservableSupport.getHomeObservable().registerObserver(mHomeBoserver);
    }

    @Override
    public String getReportDialogId() {
        return "sim_auth";
    }

    Runnable runnableLoadingTimeout = new Runnable() {

        @Override
        public void run() {
            AppLogic.runOnUiGround(new Runnable() {

                @Override
                public void run() {
                    LogUtil.logd(TAG + "Loading view outtime.");
                    dismiss("simauth outtime");
                }
            });
        }
    };

    public class Cross {
        @JavascriptInterface
        public void speakText(String text) {
            LogUtil.logd(TAG + "speakText = " + text);
            if (text != null && text.contains("M")) {
                text = text.replace("M", "兆");
            }
            TtsManager.getInstance().speakText(text);
        }

        @JavascriptInterface
        public void closeWindow() {
            LogUtil.logd(TAG + "closeWindow");
            dismiss("web exceed");
        }

        @JavascriptInterface
        public void time(String time) {
            LogUtil.logd(TAG + "LoadingTime = " + time);
        }

        @JavascriptInterface
        public void hide() {
            LogUtil.logd(TAG + "hide");
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    setCancelable(bCancel);
                    setCanceledOnTouchOutside(bCancel);
                    llAuth.setVisibility(View.VISIBLE);
                    AppLogic.removeBackGroundCallback(runnableLoadingTimeout);
                }
            });
            if(mWebView != null){
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl("javascript:TXZ.setAuthWarnCancelTime("+ cancelTime + ")");
                        LogUtil.logd(TAG + "javascript:TXZ.setAuthWarnCancelTime = " + cancelTime);
                    }
                });
            }
        }

        @JavascriptInterface
        public void log(String s) {
            LogUtil.logd(TAG + s);
        }

        @JavascriptInterface
        public void submitted() {
            LogUtil.logd(TAG + "submitted");
            PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_SIM_AUTH_SUCCESS_NUM, DeviceInfo.getSimSerialNumber());
        }

    }
}
