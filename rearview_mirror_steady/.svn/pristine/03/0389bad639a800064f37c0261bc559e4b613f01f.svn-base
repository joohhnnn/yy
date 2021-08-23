package com.txznet.debugtool.util;

import java.util.HashSet;
import java.util.Set;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;


public class SPThreshholdUtil {
    public static final String TAG = "SPThreshholdUtil";
    
    public static final String APPLICATION_SP_NAME = "DebugToolSP";
    
    public static final String IFLESHOW = "ifLeftShow";
    public static final String IFRIGHTSHOW = "ifRightShow";

    
    public static void setSharedPreferencesData(Context context, String name, String key, float value) {
        SharedPreferences settings = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat(key, value);
        editor.apply();
    }

    public static void setSharedPreferencesData(Context context, String name, String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static void setSharedPreferencesData(Context context, String name, String key, String value) {
    	
    	SharedPreferences sp = null;
    	if (context instanceof Activity) {
			Log.e("Prisoner", "Activity");
			sp = ((Activity) context).getPreferences(Context.MODE_PRIVATE);
		} else if (context instanceof Application) {
			Log.e("Prisoner", "Application");
			sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		}
        sp.edit().putString(key, value).apply();  // commit
        sp.edit().putString(key, value).commit();
    }

    public static void setSharedPreferencesData(Context context, String name, String key, String[] value) {
        SharedPreferences settings = context.getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Set set = new HashSet<String>();
        for (int i = 0; i < value.length; i++) {
            set.add(value[i]);
        }
        editor.putStringSet(key, set);
        editor.apply();
    }
   
    
    public static boolean getSPData (Context context, String name, String key){
    	SharedPreferences sp = context.getSharedPreferences(name, Context.MODE_PRIVATE);
		return sp.getBoolean(key,true);
    }
}
