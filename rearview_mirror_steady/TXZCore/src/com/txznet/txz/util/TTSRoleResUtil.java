package com.txznet.txz.util;

import java.io.File;
import android.text.TextUtils;

public class TTSRoleResUtil {
	public static String getTtsRes(String strText, String[] strResList, String strResDir, String strSuffix) {
		String strPath = null;
		for (String strResName : strResList) {
			if (TextUtils.isEmpty(strResName)) {
				continue;
			}
			if (strResName.equals(strText + strSuffix)) {
				strPath = strResDir + File.separator + strResName;
				break;
			}
		}
		return strPath;
	}

	public static String getTtsRes(String strRole, String strText) {
		File role = new File(strRole);
		if (!role.exists() || !role.isDirectory()) {
			return null;
		}
		return getTtsRes(strText, role.list(), role.getPath(), "");
	}
	
	public static boolean checkCorrect(String strPath){
		File f = new File(strPath);
		return f.exists() && f.isFile();
	}
	
}
