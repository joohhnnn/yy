package com.txznet.txz.component.tts.mix;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.tts.ITts;
import com.txznet.txz.util.CipherUtil;
import com.txznet.txz.util.DynamicLoaderUtil;
import com.txznet.txz.util.TtsAuthorizeUtil;
import com.txznet.txz.util.UnZipUtil;

public class OuterTtsEngine extends TtsEngine{
	private String mJarParentPath;
	private String mJarName;
	private String mAssetsPath;
	
	public OuterTtsEngine(String strJarParentPath, String strJarName, String strAssetsPath) {
		mJarParentPath = strJarParentPath;
		mJarName = strJarName;
		mAssetsPath = strAssetsPath;
	}
	
	public OuterTtsEngine(File jar) {
		mJarParentPath =  jar.getParent();	
		mJarName = jar.getName(); 
		
		String strJsonInfo = UnZipUtil.getInstance().UnZipToString(jar.getPath(), TtsEngine.OUTER_ENGINE_PACKAGE_INFO);
		if (TextUtils.isEmpty(strJsonInfo)) {
			return;
		}
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(strJsonInfo);
			int nId = jsonObject.getInt(TtsTheme.TTS_THEME_PKT_KEY_ID);
			String strName = jsonObject.getString(TtsTheme.TTS_THEME_PKT_KEY_NAME);
			int nVersion = jsonObject.getInt(TtsTheme.TTS_THEME_PKT_KEY_VERSION);
			String strClassName = jsonObject.getString(TtsTheme.TTS_THEME_PKT_KEY_CLASSNAME);
			setId(nId);
			setName(strName);
			setVersion(nVersion);
			setClassName(strClassName);
			setFilePath(jar.getPath());
			mAssetsPath = TtsEngine.OUTER_ENGINE_ASSETS_DIR + nId;
		} catch (Exception e) {
		}
	}
	
	@Override
	public ITts getEngine() {
		if (mTtsEngine == null) {
			synchronized (this) {
				if (mTtsEngine == null) {
					String strJson = UnZipUtil.getInstance().UnZipToString(getFilePath(), TtsEngine.OUTER_ENGINE_PACKAGE_DATA);
					if (!TextUtils.isEmpty(strJson)) {
						// 解密参数文件
						try {
							strJson = CipherUtil.deCrypt(TtsAuthorizeUtil.TTS_PUBLIC_KEY, strJson);
						} catch (Exception e) {
							strJson = null;
						}
					}
					mTtsEngine = DynamicLoaderUtil.getTtsTool(AppLogic.getApp(), mJarParentPath, mJarName, mClassName, mAssetsPath, strJson);
				}
			}
		}
		return mTtsEngine;
	}

	@Override
	public TtsType getType() {
		return TtsType.OUTER;
	}

	public static OuterTtsEngine getTtsEngine(File jar) {
		if (!jar.getName().endsWith(TtsTheme.TTS_THEME_PKT_SUFFIX)) {
			return null;
		}
	
		// 校验文件
		if (!TtsAuthorizeUtil.checkAuthorization(jar)) {
			LogUtil.loge("tts theme : authorize fail : " + jar.getPath());
			return null;
		}
	
		String strJsonInfo = UnZipUtil.getInstance().UnZipToString(jar.getPath(), TtsEngine.OUTER_ENGINE_PACKAGE_INFO);
		if (TextUtils.isEmpty(strJsonInfo)) {
			return null;
		}
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(strJsonInfo);
		} catch (Exception e) {
			return null;
		}
	
		// 必须有的字段  ////////////////////////////////////////////
		int type = jsonObject.optInt(TtsTheme.TTS_THEME_PKT_KEY_TYPE);
		if (type != TtsTheme.TTS_THEME_TYPE_ENGINE) {
			// 主题类型不是引擎类型
			return null;
		}
		
		int nId = -1;
		try {
			nId = jsonObject.getInt("id");
		} catch (Exception e) {
			return null;
		}
	
		int nVersion = 0;
		try {
			String strVersion = jsonObject.getString("version");
			nVersion = Integer.parseInt(strVersion);
		} catch (Exception e) {
			return null;
		}
	
		String strName = null;
		try {
			strName = jsonObject.getString("name");
		} catch (Exception e) {
			return null;
		}
	
		String strClassName = null;
		try {
			strClassName = jsonObject.getString("className");
		} catch (Exception e) {
			return null;
		}
	
		// 可选字段  ///////////////////////////////////////////////
		JSONArray jsonArray = null;
		String strRole = null;
		JSONObject jsonObj = null;
		try {
			jsonArray = jsonObject.getJSONArray("roles");
			strRole = (String) jsonArray.get(0);
			jsonObj = jsonObject.getJSONObject(strRole);
		} catch (JSONException e1) {
		}
		
		int nSex = 0;
		try {
			nSex = jsonObj.getInt("sex");
		} catch (Exception e) {
		}
	
		String strLanguage = "putonghua";
		try {
			strLanguage = jsonObj.getString("language");
		} catch (JSONException e) {
		}
	
		int nAge = 0;
		try {
			nAge = jsonObj.getInt("age");
		} catch (Exception e) {
		}
	
		int nPriority = 0;
		try {
			nPriority = jsonObj.getInt("priority");
		} catch (Exception e) {
		}
	
		OuterTtsEngine engine = new OuterTtsEngine(jar.getParent(),	jar.getName(), TtsEngine.OUTER_ENGINE_ASSETS_DIR + nId);
		engine.setId(nId);
		engine.setName(strName);
		engine.setVersion(nVersion);
		engine.setClassName(strClassName);
		engine.setFilePath(jar.getPath());
		engine.setSex(nSex);
		engine.setLanguage(strLanguage);
		engine.setAge(nAge);
		engine.setPriority(nPriority);
		
		return engine;
	}
}
