package com.txznet.txz.module.activation;

import java.io.File;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
/*
 * note:放置激活数据库文件到指定系统路径时，一定要确保第三方有可读权限
 */
public class DBActivator implements IActivator{
	private static DBActivator sIntance = new DBActivator();
	
	public static final String DATABASE_FILENAME = "lv_activate.db";
	public static final String DATABASE_FULL_PATH = getDBFullPath();
	public static final String DEVSN_TABLE_NAME = "devsns";
	public static final String DEVSN_COLUMN_NAME = "devsn";
	
	private static String getDBFullPath(){
		String strFullPath = "";
		strFullPath = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_ACTIVATE_LOCAL_VERIFICATE_FILE_PATH);
		if (TextUtils.isEmpty(strFullPath)){
			strFullPath = "/etc/txz/" + DATABASE_FILENAME;
		}
		LogUtil.logd("getDBFullPath:" + strFullPath);
		return strFullPath;
	}
	
	private DBActivator(){
		
	}
	
	public static DBActivator getInstance(){
		return sIntance;
	}
	
	private SQLiteDatabase openDatabase(String strDataBase){
		try {
			SQLiteDatabase database = null;
			File f = new File(strDataBase);
			if (!f.exists()){
				LogUtil.logw(strDataBase + " is not exists");
				return null;
			}
			database = SQLiteDatabase.openDatabase(strDataBase, null, SQLiteDatabase.OPEN_READONLY);
			return database;
		} catch (Exception e) {
			LogUtil.logw(strDataBase + " open error:" + e.toString());
		}
		return null;
	}
	
	
	@Override
	public boolean checkPermission(String sEncryptedDevSn) {
		boolean bRet = false;
		SQLiteDatabase database = null;
		Cursor cursor = null;
		do {
			database = openDatabase(DATABASE_FULL_PATH);
			if (database == null) {
				break;
			}
			
			try{
				String strSelection = String.format("%s=?", DEVSN_COLUMN_NAME);
				cursor = database.query(DEVSN_TABLE_NAME, new String[]{DEVSN_COLUMN_NAME}, strSelection, new String[]{sEncryptedDevSn}, null, null, null);
			
				if (cursor != null){
					if (cursor.moveToNext()){
						LogUtil.logd("checkPermission " + sEncryptedDevSn + " : " + cursor.getString(cursor.getColumnIndex(DEVSN_COLUMN_NAME)));
						bRet = true;
					}
				}
			}catch(Exception e){//shit!!! query接口会抛异常,如果查询的表不存在
				LogUtil.loge("checkPermission exception:" + e.toString());
			}
			
		} while (false);
		
		if (cursor != null) {
			try {
				cursor.close();
			} catch (Exception e) {

			}
		}
		
		if (database != null) {
			try {
				database.close();
			} catch (Exception e) {

			}
		}
		return bRet;
	}
	
	@Override
	public boolean isSupportLocalActivation() {
		boolean bRet = false;
		File f = new File(DATABASE_FULL_PATH);
		bRet = f.exists();
		return bRet;
	}
	
}
