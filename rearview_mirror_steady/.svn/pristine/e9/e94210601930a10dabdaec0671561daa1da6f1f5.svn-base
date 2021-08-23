package com.txznet.txz.util;

import android.content.Intent;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;

public class IntentUtil {
    private static IntentUtil mInstance;
    private boolean mNeedSetPackage = false;

    public static IntentUtil getInstance(){
        if(mInstance == null){
            synchronized (IntentUtil.class){
                if(mInstance == null){
                    mInstance = new IntentUtil();
                }
            }
        }
        return mInstance;
    }
    private IntentUtil(){

    }

    public void setNeedSetPackage(boolean needSetPackage){
        this.mNeedSetPackage = needSetPackage;
    }

    /**
     * 通过配置项决定是否设置Intent package
     */
    public void sendBroadcastFixSetPackage(Intent intent,String intentPackage){
        if(mNeedSetPackage && !TextUtils.isEmpty(intentPackage)){
            intent.setPackage(intentPackage);
        }
        GlobalContext.get().sendBroadcast(intent);
    }

}
