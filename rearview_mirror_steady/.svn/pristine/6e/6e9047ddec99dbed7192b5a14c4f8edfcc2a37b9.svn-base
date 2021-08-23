package com.txznet.txz.util;

import org.json.JSONObject;
import com.txznet.comm.remote.util.LogUtil;

public class YZSErrorCode {
	private int mErrorCode = 0;
	private String mErrorMsg = null;

	public YZSErrorCode(String strJsonErrorMsg) {
		JSONObject json = null;
		do {
			try {
				json = new JSONObject(strJsonErrorMsg);
			} catch (Exception e) {
				LogUtil.loge("YZSErrorCode exception : " + e.toString());
				break;
			}
			
			try {
				mErrorCode = json.getInt("errorCode");
			} catch (Exception e) {
				LogUtil.logw("YZSErrorCode exception : " + e.toString());
			}
			
			try {
				mErrorCode = json.getInt("errorMsg");
			} catch (Exception e) {
				LogUtil.logw("YZSErrorCode exception : " + e.toString());
			}
		} while (false);
		
	}
	
	public int ErrorCode(){
		return mErrorCode;
	}
	
	public String ErrorMsg(){
		return mErrorMsg;
	}
}
