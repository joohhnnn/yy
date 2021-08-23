package com.txznet.reserve.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by telenewbie on 2017/6/19.
 */

public class ReserveServiceSameProcess1 extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        stopSelf();
        return START_STICKY_COMPATIBILITY;
    }
}
