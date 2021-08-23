package com.txznet.launcher.ui.base;

import android.content.Intent;
import android.os.Bundle;

import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.ThemeObservable;
import com.txznet.launcher.helper.ThemeManager;
import com.txznet.launcher.layout.LayoutModel;
import com.txznet.loader.AppLogic;

public abstract class ThemeActivity extends BaseActivity {
    protected LayoutModel mLayoutModel;

    private ThemeObservable.ThemeObserver mThemeObserver = new ThemeObservable.ThemeObserver() {
        @Override
        public void onThemeChanged(String themeName) {
            LogUtil.logd("theme chage to " + themeName);
            ThemeManager.getInstance(ThemeActivity.this).installTheme(themeName, new Runnable() {
                @Override
                public void run() {
                    onDestroy();
                    onCreate(null);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeManager.getInstance(this).reloadProxyContext();//重新加载内容
        mLayoutModel = createLayoutModel(ThemeManager.getInstance(this).getProxyContext());
        mLayoutModel.onCreate(savedInstanceState);
        AppLogic.registerThemeObserver(mThemeObserver);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (mLayoutModel != null) {
            mLayoutModel.onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mLayoutModel != null) {
            mLayoutModel.onRestart();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLayoutModel != null) {
            mLayoutModel.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mLayoutModel != null) {
            mLayoutModel.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mLayoutModel != null) {
            mLayoutModel.onPause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mLayoutModel != null) {
            mLayoutModel.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppLogic.unregisterThemeObserver(mThemeObserver);
        if (mLayoutModel != null) {
            mLayoutModel.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mLayoutModel != null) {
            mLayoutModel.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mLayoutModel.onNewIntent(intent);
    }

    protected abstract LayoutModel createLayoutModel(ProxyContext proxyContext);
}
