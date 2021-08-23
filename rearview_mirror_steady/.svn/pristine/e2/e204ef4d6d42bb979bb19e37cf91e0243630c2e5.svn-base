package com.txznet.launcher.layout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.txznet.launcher.bean.AppIconInfo;
import com.txznet.launcher.bean.AppInfo;
import com.txznet.launcher.ui.base.ProxyContext;

import java.util.List;

public abstract class LayoutModel {
    public static final int LAYOUT_TYPE_GRID = 1;
    public static final int LAYOUT_TYPE_TRIANGLE = 2;

    public static class Plugin {
        public String name;
        public int start;
        public int end;
    }

    public List<Plugin> plugins;
    public List<AppInfo> iconInfo;
    protected Activity mActivity;

	protected ProxyContext mProxyContext;

    public LayoutModel(Activity act, ProxyContext proxyContext) {
        mActivity = act;
        mProxyContext = proxyContext;
    }


    public void onCreate(Bundle savedInstanceState) {

    }

    public void onRestart() {

    }

    public void onStart() {

    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void onStop() {

    }

    public void onDestroy() {

    }

    public void onNewIntent(Intent intent) {

    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {

    }

    public void onSaveInstanceState(Bundle outState) {

    }

    public View findViewById(int id) {
        View view = null;
        try {
            view = mActivity.findViewById(mProxyContext.getProxyId(id));
        } catch (Exception e) {

        }
        return view;
    }

    public void setContentView(String layout) {
        mActivity.setContentView(mProxyContext.getLayout(layout));
    }
}
