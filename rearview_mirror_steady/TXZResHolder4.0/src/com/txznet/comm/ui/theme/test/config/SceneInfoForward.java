package com.txznet.comm.ui.theme.test.config;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.BaseSceneInfoForward;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;

@SuppressLint("NewApi")
public class SceneInfoForward extends BaseSceneInfoForward {

    /**
     * 需要显示播报小喇叭的场景
     */
    private static final HashSet<String> speechBroadcastKey = new HashSet<String>() {{
        add("joke");                // 笑话
        add("calculator");          // 计算
        add("calendar");            // 日历
        add("baike");               // 百科
        add("story");               // 故事
        add("translation");         // 翻译
        add("translator");          // 翻译
        add("cookbook");            // 菜谱
        add("poem");                // 诗词
    }};

    public boolean isSpeechBroadcast = false; // 当前场景是否要显示播报中动画

    private static SceneInfoForward sInstance = new SceneInfoForward();

    public static SceneInfoForward getInstance() {
        return sInstance;
    }

    /**
     * 更新场景
     * @param json
     */
    @Override
    public void updateSceneInfo(String json) {
        LogUtil.logd(WinLayout.logTag + "updateSceneInfo: " + json);

        try {
            JSONObject jsonObj = new JSONObject(json);
            String scene =  jsonObj.optString("scene", "");
            String style =  jsonObj.optString("style", "");

            if (!TextUtils.isEmpty(scene)) {
                if (scene.equals("nav") || scene.equals("music") || scene.equals("audio")) {
                    SkillfulReminding.getInstance().listViewShowOneTime();
                }
            }

            // 是否播报中
            isSpeechBroadcast = !TextUtils.isEmpty(style) && speechBroadcastKey.contains(style);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新配置
     * @param data
     */
    @Override
    public void updateConfig(String data) {
        LogUtil.logd(WinLayout.logTag + "updateConfig:" + data);
        try {
            JSONObject jsonObj = new JSONObject(data);
            JSONArray ary = jsonObj.getJSONArray("cfg");

            for(int i=0; i<ary.length(); i++) {
                JSONObject row = ary.getJSONObject(i);
                String key = (String) row.get("key");
                Object val = row.get("val");
                switch (key) {
                    case "enableStartAsrWhenDismiss":// 是否开启非全时免唤醒
                        Constants.enableStartAsrWhenDismiss = (boolean) val;
                        break;
                    case "autoStopAstTimeout":// 非全时免唤醒超时时间
                        Constants.autoStopAstTimeout = Long.parseLong(String.valueOf(val));
                        break;
                        // TODO 使用主题包
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
