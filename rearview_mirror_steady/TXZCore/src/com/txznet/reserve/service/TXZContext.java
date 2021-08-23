package com.txznet.reserve.service;

import java.io.File;

import com.txznet.comm.remote.util.LogUtil;

import android.content.Context;
import android.content.ContextWrapper;
import android.text.TextUtils;

public class TXZContext extends ContextWrapper{
	private String mFilesDirName = null;
	private File mFilesDir = null;
	public TXZContext(Context base) {
		super(base);
	}
	
	public TXZContext(Context base, String strFilesDirName){
		super(base);
		mFilesDirName = strFilesDirName;
		mFilesDir = new File(getApplicationInfo().dataDir + File.separator + mFilesDirName);
		if (!mFilesDir.exists()){
			boolean bRet = mFilesDir.mkdirs();
			if (!bRet){
				LogUtil.loge("TXZContext mkdirs fail : " + mFilesDir.getPath());
			}
		}
	}
	
	public TXZContext(Context base, String strParentDir, String strFilesDirName){
		super(base);
		mFilesDirName = strFilesDirName;
		mFilesDir = new File(strParentDir + File.separator + mFilesDirName);
		if (!mFilesDir.exists()){
			boolean bRet = mFilesDir.mkdirs();
			if (!bRet){
				LogUtil.loge("TXZContext mkdirs fail : " + mFilesDir.getPath());
			}
		}
	}

	@Override
	public Context getApplicationContext(){ 
		return this;
	}
	
	@Override
	public File  getFilesDir(){
		if (TextUtils.isEmpty(mFilesDirName)){
			return super.getFilesDir();
		}
		return mFilesDir;
	}
}
