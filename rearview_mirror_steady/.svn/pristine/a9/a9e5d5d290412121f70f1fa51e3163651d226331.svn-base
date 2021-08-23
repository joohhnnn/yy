package com.txznet.comm.ui.theme.test.config;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.BaseSceneInfoForward;
import com.txznet.sdk.TXZSceneManager;
import com.txznet.sdk.TXZSenceManager;
import com.txznet.sdk.TXZSceneManager.SceneType;

@SuppressLint("NewApi")
public class SceneInfoForward extends BaseSceneInfoForward {

	private static SceneInfoForward sInstance = new SceneInfoForward();

	public static SceneInfoForward getInstance() {
		return sInstance;
	}

	@Override
	public void updateSceneInfo(String scene) {
		LogUtil.logd(WinLayout.logTag+ "updateSceneInfo " + scene);
		JSONObject jsonObj = null;
		try {
			
			jsonObj = new JSONObject(scene);
			String jsonScene = (String) jsonObj.get("scene");
			if(!TextUtils.isEmpty(jsonScene)) {
				if(jsonScene.equals("nav") || jsonScene.equals("music") || jsonScene.equals("audio")) {
					SkillfulReminding.getInstance().listViewShowOneTime();
				}else if (jsonScene.equals("weather") || jsonScene.equals("stock") || jsonScene.equals("competition")){
                    WinLayout.isHideView = true;
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}


	}

}
