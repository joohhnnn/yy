package com.txznet.music.service.impl;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.service.MyService;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ProgramUtils;
import com.txznet.txz.service.IService;

import java.util.concurrent.CountDownLatch;

/**
 * @author telen
 * @date 2019/1/16,19:10
 */
public class ProgramCommand extends BaseCommand {
    public static final String TAG = "ProgramCommand";

    /**
     * 单例对象
     */
    private volatile static ProgramCommand singleton;

    private boolean isConnected = false;
    //同步锁
    private CountDownLatch mCountDownLatch = new CountDownLatch(1);

    private IService mIService;
    ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (BuildConfig.DEBUG) {
                Logger.d(TAG, getClass().getSimpleName() + ",onServiceConnected");
            }
            isConnected = true;
            mIService = IService.Stub.asInterface(service);
            mCountDownLatch.countDown();
            try {
                service.linkToDeath(new IBinder.DeathRecipient() {
                    @Override
                    public void binderDied() {
                        if (BuildConfig.DEBUG) {
                            Logger.d(TAG, getClass().getSimpleName() + ",binderDied:");
                        }
                        isConnected = false;
                        bindInnerService();
                    }
                }, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (BuildConfig.DEBUG) {
                Logger.d(TAG, getClass().getSimpleName() + ",onServiceDisconnected");
            }
            isConnected = false;
            mIService = null;
            bindInnerService();
        }
    };

    private void bindInnerService() {
        Intent intent = new Intent(GlobalContext.get(), MyService.class);
        GlobalContext.get().bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private ProgramCommand() {
        if (BuildConfig.DEBUG) {
            Logger.d(Constant.LOG_TAG_LOGIC, getClass().getSimpleName() + ",ProgramCommand：" + ProgramUtils.isProgram());
        }

        if (ProgramUtils.isProgram()) {
//            ServiceManager.getInstance().regInterceptCallback("com.txznet.music", new ServiceManager.ProgramCallback() {
//
//                @Override
//                public byte[] invoke(String cmd, byte[] data) {
//                    if (BuildConfig.DEBUG) {
//                        Log.d("telenewbie::", getClass().getSimpleName() + ",invoke:cmd=" + cmd + ",data=" + new String(data));
//                    }
//
//                    if (!isConnected) {
//                        bindInnerService();
//                        try {
//                            mCountDownLatch.await();
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                    if (BuildConfig.DEBUG) {
//                        Log.d("telenewbie::", getClass().getSimpleName() + ",invoke2:bindService:finish,cmd=" + cmd + ",connected=" + (mIService != null));
//                    }
//
//                    if (mIService != null) {
//                        try {
//                            return mIService.sendInvoke(GlobalContext.get().getPackageName(), cmd, data);
//                        } catch (RemoteException e) {
//                        }
//                    }
//                    return new byte[0];
//                }
//            });
        }
    }

    public static ProgramCommand getInstance() {
        if (singleton == null) {
            synchronized (ProgramCommand.class) {
                if (singleton == null) {
                    singleton = new ProgramCommand();
                }
            }
        }
        return singleton;
    }


}
