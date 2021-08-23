package com.txznet.launcher.module.record;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.launcher.R;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.module.BaseModule;

public class NoTtsQrcodeModule extends BaseModule {
    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_record_no_tts_qrcode, null);
        return view;
    }
}
