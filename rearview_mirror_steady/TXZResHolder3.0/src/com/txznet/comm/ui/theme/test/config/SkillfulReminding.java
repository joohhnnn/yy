package com.txznet.comm.ui.theme.test.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;

public class SkillfulReminding {

	private SharedPreferences mSharePre;

	/* 熟手模式提醒 */
	private static final String REMINDING_SP_NAME = "skillful_reminding_sp";
	private static SkillfulReminding sInstance;

	protected SkillfulReminding() {
		mSharePre = GlobalContext.get().getSharedPreferences(REMINDING_SP_NAME,
				Context.MODE_PRIVATE);
		initSkilledModeReminder();

		if (residuaInitTimes > 0){
            LogUtil.logd(WinLayout.logTag+ "residuaInitTimes-- "+residuaInitTimes);
			setResiduaInitTimes(--residuaInitTimes);
		}
	}

	public static SkillfulReminding getInstance() {
		if (sInstance == null) {
			synchronized (SkillfulReminding.class) {
				if (sInstance == null) {
					sInstance = new SkillfulReminding();
				}
			}
		}
		return sInstance;
	}

	public boolean getIsUseSkillful() {
		return mSharePre.getBoolean("isUseSkillful", false);
	}

	public void setIsUseSkillful(boolean isUse) {
		mSharePre.edit().putBoolean("isUseSkillful", isUse).commit();
	}

	public int getListShowCount() {
		return mSharePre.getInt("listShowCount", 0);
	}

	public void setListShowCount(int count) {
		mSharePre.edit().putInt("listShowCount", count).commit();
	}

	public int getResidualDisplayTimes() {
		return mSharePre.getInt("residuaDisplayTimes", 3);
	}

	public void setResidualDisplayTimes(int count) {
		mSharePre.edit().putInt("residuaDisplayTimes", count).commit();
	}

	public int getResiduaInitTimes() {
		return mSharePre.getInt("residuaInitTimes",0);
	}

	public void setResiduaInitTimes(int residuaInitTimes) {
		mSharePre.edit().putInt("residuaInitTimes",residuaInitTimes).commit();
	}

	public static boolean isHasSkillful = false;    //是否包含熟手模式
	public static boolean isUseSkillful = false;    //是否使用过熟手模式
	public static int listViewShowTimes = 0;    //音乐、导航累计使用次数
	public static int residuaDisplayTimes = 3;    //本次初始化core可以提示的剩余次数
	public static int residuaInitTimes = 0;    //剩余初始化core可提示次数

	private void initSkilledModeReminder() {
		LogUtil.logd(WinLayout.logTag+ "isHasSkillful-- "+isHasSkillful);
		isUseSkillful = getIsUseSkillful();
		if (!isUseSkillful) { // 提醒还没有结束
			listViewShowTimes = getListShowCount();
			if (listViewShowTimes >= 10) {
                residuaInitTimes = getResiduaInitTimes();
                if (residuaInitTimes > 0){
					residuaDisplayTimes = 3;
                   /* residuaDisplayTimes = getResidualDisplayTimes();
                    if (residuaDisplayTimes > 0) { // 需要显示
                        setResidualDisplayTimes(residuaDisplayTimes - 1);
                        residuaDisplayTimes = 3;
                    } else {
                        residuaDisplayTimes = 0;
                        setIsUseSkillful(isUseSkillful = true);
                    }*/
                }else {
                    residuaInitTimes = 0;
					residuaDisplayTimes = 0;
                }
			} else {
				residuaDisplayTimes = 0;
			}
		} else {
			residuaDisplayTimes = 0;
		}
	}

	public void listViewShowOneTime() {
        LogUtil.logd(WinLayout.logTag+ "listViewShowOneTime "+listViewShowTimes);
		if ((!isUseSkillful) && (listViewShowTimes < 10)) {
			++listViewShowTimes;
			setListShowCount(listViewShowTimes);
			if (listViewShowTimes >= 10) {
			    setResiduaInitTimes(3);
				setResidualDisplayTimes(3);
			}
		}
	}
	
	public void hasSkillful() {
		isHasSkillful = true;
	}


	public void useSkillful() {
		if (!isUseSkillful) {
			setIsUseSkillful(isUseSkillful = true);
			setResidualDisplayTimes(residuaDisplayTimes = 0);
		}
	}

	public boolean ifShowRemind() {
		if (isHasSkillful && (!isUseSkillful) && (residuaDisplayTimes > 0)) {
			return true;
		}
		return false;
	}

	public void reduceOnce() {
		--residuaDisplayTimes;
	}

}
