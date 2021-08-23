package com.txznet.webchat.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txznet.webchat.actions.ResourceActionCreator;
import com.txznet.webchat.util.ContactEncryptUtil;

public class WxResReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String uid = intent.getStringExtra("uid");
        if (uid != null) {
            ResourceActionCreator.get().downloadContactImageForEncrypted(ContactEncryptUtil.decrypt(uid));
        }
    }
}
