package com.txznet.record.util;

import com.txznet.comm.remote.util.LogUtil;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 
 * SharedPreference Util
 *
 */
public class SPUtil {
	private SPUtil(){
		
	}
	
	public static final String SP_HELP = "sp_help";
	public static final String SP_HELP_COUNT = "sp_help_count";

	public static boolean needShowHelpText(Context context){
		SharedPreferences sharedPreferences = context.getSharedPreferences(SP_HELP, Context.MODE_PRIVATE);
		int count = sharedPreferences.getInt(SP_HELP_COUNT, 0);
		LogUtil.logd("count >>"+count);
		return count<=3;
	}
	
	public static void updateHelpCount(Context context){
		SharedPreferences sharedPreferences = context.getSharedPreferences(SP_HELP, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		int count = sharedPreferences.getInt(SP_HELP_COUNT, 0);
		LogUtil.logd("count >>"+count);
		if(count<=4){
			editor.putInt(SP_HELP_COUNT, ++count);
			editor.commit();			
		}
	}
}
