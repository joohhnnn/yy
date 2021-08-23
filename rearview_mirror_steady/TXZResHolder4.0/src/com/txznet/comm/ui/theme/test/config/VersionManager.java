package com.txznet.comm.ui.theme.test.config;

import com.txznet.comm.remote.util.ConfigUtil;

/**
 * 一些版本相关的判断
 *
 */
public class VersionManager {
	static VersionManager instance = new VersionManager();
	public static VersionManager getInstance() {
		return instance;
	}
	
	Boolean isUseHelpNewTag = null;
	/**
	 * 判断当前版本的Core或SDK是否支持帮助小红点
	 * @return
	 */
	public Boolean isUseHelpNewTag() {
		if (isUseHelpNewTag == null) {
			try {
				ConfigUtil.class.getMethod("isShowHelpNewTag", new  Class[0]);
				isUseHelpNewTag = true;
			} catch (NoSuchMethodException e) {
				isUseHelpNewTag = false;
				e.printStackTrace();
			}
		}
		return isUseHelpNewTag;
	}
}
