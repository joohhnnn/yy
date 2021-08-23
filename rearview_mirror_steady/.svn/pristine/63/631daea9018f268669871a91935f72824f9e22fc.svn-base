package com.txznet.marketing.HttpRequest;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefencesUtil {

    private static final String TAG = "SharedPrefencesUtil";

    private static SharedPrefencesUtil mInstance = new SharedPrefencesUtil();
    private SharedPrefencesUtil(){
    }
    public static SharedPrefencesUtil getmInstance(){
        return mInstance;
    }

    private final static String spNmae = "self_marketing_sp";

    private static SharedPreferences mSharePreferences;
   // private boolean isCanUse;

    public void init(Context context){
        mSharePreferences = context.getSharedPreferences("spNmae",Context.MODE_PRIVATE);
    }

    public void setCanUse(boolean canUse) {
        mSharePreferences.edit().putBoolean("isCanUse",canUse).commit();
    }

    public boolean getCanUse(){
        return mSharePreferences.getBoolean("isCanUse",false);
    }
}
