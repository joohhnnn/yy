package com.txznet.feedback;

import java.io.File;

import android.text.TextUtils;

import com.txznet.feedback.db.SqliteDaoImpl;
import com.txznet.loader.AppLogicBase;
import com.txznet.sqlite.SqliteManager.SqliteParams;

public class AppLogic extends AppLogicBase {

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public static SqliteParams getSqliteParams() {
		SqliteParams sparams = new SqliteParams();
		sparams.context = AppLogic.getApp();
		sparams.isAndroidVersion = true;
		sparams.isDemoMode = false;
		sparams.sqliteDao = SqliteDaoImpl.getInstance();
		sparams.databasePath = getDatabasePath();
		return sparams;
	}

	public static String getDatabasePath() {
		String filePath = AppLogic.getApp().getFilesDir().getPath();
		if (!TextUtils.isEmpty(filePath)) {
			return filePath.substring(0,
					filePath.lastIndexOf(File.separator) + 1);
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("data/data/")
					.append(AppLogic.getApp().getPackageName())
					.append("/");
			return sb.toString() + "databases/";
		}
	}
}