package com.txznet.reserve.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.txznet.comm.base.BaseActivity;
import com.txznet.txz.jni.JNIHelper;

import java.io.File;

/**
 * 辅助安装Apk的Activity
 */
public class ReserveStandardActivity1 extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        procIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        procIntent(intent);
    }

    private void procIntent(Intent intent) {
        String url = intent.getStringExtra("url");
        JNIHelper.logd("ReserveStandardActivity1 procIntent url:" + url);
        if (TextUtils.isEmpty(url)) {
            finish();
            return;
        }
        installApk(url);
    }

    private void installApk(String url) {
        mUrl = url;
        if (url.startsWith("http://")) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            finish();
        } else {
            File fNewApk = new File(url);
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.setAction(Intent.ACTION_VIEW);
            // intent.putExtra(Intent.EXTRA_RETURN_RESULT, true);
            // intent.putExtra(Intent.EXTRA_ALLOW_REPLACE, true);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
            intent.setDataAndType(Uri.fromFile(fNewApk), "application/vnd.android.package-archive");
            startActivityForResult(intent, 2);
        }
    }

    private String mUrl;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        JNIHelper.logd("ReserveStandardActivity1 onActivityResult url:" + mUrl);
        Intent intent = new Intent("com.txznet.txz.action.INSTALLER_CLOSE");
        intent.putExtra("url", mUrl);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        finish();
    }
}