package com.txznet.txz.component.advertising.view;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.Win2Dialog;
import com.txznet.txz.module.advertising.AdvertisingManager;

public class WebViewDialog extends Win2Dialog {
    private LinearLayout mWindowsView;
    private String mUrl;
    private WebView mWebView;

    public WebViewDialog() {
        super(true, true);
    }

    public WebViewDialog(boolean isSystem, boolean isFullScreen) {
        super(isSystem, isFullScreen);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mWindowsView.removeAllViews();
    }

    @Override
    public void show() {
        LogUtil.d("webview dialog show ");
        if (TextUtils.isEmpty(mUrl)) {
            mUrl = "https://www.baidu.com/";
        }
        if (!isShowing() && !TextUtils.isEmpty(mUrl)) {
            if (mWebView == null) {
                mWebView = new WebView(GlobalContext.get());
            }
            LogUtil.d("webview dialog show mUrl:" + mUrl);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            mWebView.setLayoutParams(params);
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.loadUrl(mUrl);
            mWindowsView.addView(mWebView);
            super.show();
        }
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    private void log(String log) {
        LogUtil.d("open advertising " + log);
    }

    @Override
    protected View createView(Object... objects) {
        if (mWindowsView == null) {
            mWindowsView = new LinearLayout(GlobalContext.get());
            mWindowsView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            mWindowsView.setLayoutParams(params);
        }
        return mWindowsView;
    }
}
