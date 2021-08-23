package com.txznet.txz.ui.win.help;

import android.content.Context;
import android.content.SharedPreferences;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;

import java.util.Set;

public class HelpTipsPreferenceUtil {

    private static final String NAME = "helpTips";

    public static final String KEY_HELP_USUAL_SPEAK_GRAMMAR_TIPS = "KEY_HELP_USUAL_SPEAK_GRAMMAR_TIPS";

    private static HelpTipsPreferenceUtil mInstance = null;
    private SharedPreferences preferences = null;
    private SharedPreferences.Editor editor = null;
    private Context mContext;

    private HelpTipsPreferenceUtil() {
        mContext = GlobalContext.get();
        preferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static HelpTipsPreferenceUtil getInstance() {
        if (mInstance == null)
            synchronized (HelpTipsPreferenceUtil.class) {
                if (mInstance == null) {
                    mInstance = new HelpTipsPreferenceUtil();
                }
            }
        return mInstance;
    }

    public void setString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public String getString(String key, String defValue) {
        return preferences.getString(key, defValue);
    }

    public void setBoolean(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.commit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return preferences.getBoolean(key, defValue);
    }

    public long getLong(String key, long def) {
        return preferences.getLong(key,def);
    }

    public void setLong(String key, long value) {
        editor.putLong(key,value);
        editor.commit();
    }

    public void setStringSet(String key ,Set<String> values){
        editor.putStringSet(key,values);
        editor.commit();
    }

    public Set<String> getStringSet(String key,Set<String> def) {
        return preferences.getStringSet(key ,def);
    }

    public int getInt(String key,int def) {
        return preferences.getInt(key,def);
    }

    public void setInt(String key,int value) {
        LogUtil.d("HelpTipsPreferenceUtil match :"+key+"count :"+value);
        editor.putInt(key,value);
        editor.commit();
    }

    public void remove(String key) {
        editor.remove(key);
        editor.commit();
    }

    public void clear(){
        editor.clear();
        editor.commit();
    }

}
