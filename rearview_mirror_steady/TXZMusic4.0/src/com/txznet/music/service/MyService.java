package com.txznet.music.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.RequiresApi;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.baseModule.logic.ServiceEngine;
import com.txznet.music.ui.SplashActivity;
import com.txznet.txz.service.IService;

public class MyService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return new SampleBinder();
    }

    public class SampleBinder extends IService.Stub {
        @Override
        public byte[] sendInvoke(final String packageName, final String command, final byte[] data) throws RemoteException {
            return ServiceEngine.getInstance().sendInvoke(packageName, command, data);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onCreate() {
        super.onCreate();
    }


}
