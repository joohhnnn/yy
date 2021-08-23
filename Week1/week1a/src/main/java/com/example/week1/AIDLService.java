package com.example.week1;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AIDLService extends Service {
    List<Person> list = new ArrayList<Person>();
    public AIDLService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.e("SERVICE测试","lianjiechenggong");
        return iBinder;
    }
    private IBinder iBinder=new IMyAidlInterface.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public List<Person> getlist() throws RemoteException {
            return list;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        list=intent.getParcelableArrayListExtra("contact");
        //Person person=list.get(1);
        // Log.e("测试",person.getName());

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("SERVICE关闭","ddd");
    }
}