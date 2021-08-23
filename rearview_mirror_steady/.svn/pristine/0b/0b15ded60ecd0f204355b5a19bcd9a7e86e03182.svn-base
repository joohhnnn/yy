package com.txznet.txz.ui.win.help;

import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.media.chooser.AudioPriorityChooser;
import com.txznet.txz.component.media.chooser.MusicPriorityChooser;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.nav.NavManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HelpHitTispUtil {

    private static HelpHitTispUtil mInstance = null;
    private static JSONObject jsonNearActions = null;


    private HelpHitTispUtil() {

    }

    public static HelpHitTispUtil getInstance() {
        if (mInstance == null)
            synchronized (HelpPreferenceUtil.class) {
                if (mInstance == null) {
                    mInstance = new HelpHitTispUtil();
                }
            }
        return mInstance;
    }

    private static void initJsonNearActions(){
        try {
            if(jsonNearActions == null){
                jsonNearActions = new JSONObject(NativeData
                        .getResJson("RS_VOICE_USUAL_SPEAK_GRAMMAR_TIPS"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    /**
     * 导航去XXX被命中
     */
    public void hitNavToTips() {
        if(!hasNavTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_NAV_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 我饿了命中
     */
    public void hitHungryTips() {
        if(!hasNavTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_NAV_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void hitGoHomeTips() {
        if(!hasNavTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_NAV_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void hitCompanyTips() {
        if(!hasNavTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_NAV_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(3));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 我要听歌
     */
    public void hitSuiBianTingTips() {
        if(!hasMusicTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_MUSIC_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 我要听刘德华的歌
     */
    public void hitSingerTips() {
        if(!hasMusicTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_MUSIC_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 我要听青花瓷
     */
    public void hitSongNameTips() {
        if(!hasMusicTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_MUSIC_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 我要听林俊杰的江南
     */
    public void hitSingerAndSongNameTips() {
        if(!hasMusicTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_MUSIC_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(3));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 我要听相声
     */
    public void hitXiangShengTips() {
        if(!hasRadioTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_RADIO_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 我要听<段子来了><盗墓笔记>
     */
    public void hitAlbumTips() {
        if(!hasRadioTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_RADIO_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(1));
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打电话给张三
     */
    public void hitCallByNameTips() {
        if(!hasCallTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_DIANHUA_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打电话给13xxxxxxxxx
     */
    public void hitCallByNumberTips() {
        if(!hasCallTool()){
            return;
        }
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_DIANHUA_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加亮度/降低亮度
     */
    public void hitUpdateLightTips() {
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_CONTROL_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 打开WIFI/关闭WIFI
     */
    public void hitUpdateWifiStatusTips() {
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_CONTROL_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 增加音量/减少音量
     */
    public void hitUpdateVolumeTips() {
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_CONTROL_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 讲个笑话
     */
    public void hitJokeTips() {
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_OTHER_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 明天的天气怎么样
     */
    public void hitWeatherTips() {
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_OTHER_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(1));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 我在哪里
     */
    public void hitLocationTips() {
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_OTHER_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(2));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 腾讯的股票
     */
    public void hitStockTips() {
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_OTHER_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(3));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 食谱：红烧肉怎么做
     */
    public void hitCookbookTips() {
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_OTHER_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(4));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 百科：刘德华是谁
     */
    public void hitBaiKeTips() {
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_OTHER_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(5));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 电影：最近上映的电影
     */
    public void hitMovieTips() {
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_OTHER_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(6));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 炒冷饭指令
     */
    public void hitChaoLengFanTip(int index) {
        try {
            if(jsonNearActions == null){
                initJsonNearActions();
            }
            String key = HelpPreferenceUtil.KEY_HIT_CHAOLENGFAN_TIPS;
            helpHitTips(key,jsonNearActions.getJSONArray(key).getString(index));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param key
     * @param index    json串的索引
     * @param defValue 默认值
     */
    private void hitTips(String key, int index, String defValue) {
        String hitTips = HelpPreferenceUtil.getInstance().getString(key, defValue);
        if (hitTips.substring(index, index + 1).equals("0")) {
            String tips = "";
            for (int i = 0; i < hitTips.length(); i++) {
                if (i == index) {
                    tips += "1";
                } else {
                    tips += hitTips.toCharArray()[i];
                }
            }
            HelpPreferenceUtil.getInstance().setString(key, tips);
        }
    }

    public void helpHitTips(String key,String hitTips){
        String tips = HelpPreferenceUtil.getInstance().getString("RS_VOICE_USUAL_SPEAK_GRAMMAR_TIPS", null);
        try {
            JSONObject jsonObject = null;
            if(tips == null){
                jsonObject= new JSONObject();
            }else{
                jsonObject = new JSONObject(tips);
            }
            JSONArray jsonArray = null;
            if(jsonObject.has(key)){
                jsonArray = jsonObject.getJSONArray(key);
            }else{
                jsonArray = new JSONArray();
            }
            if(jsonArray.length() == 0){
                jsonArray.put(hitTips);
            }else{
                boolean isHit = false;
                for (int i = 0; i < jsonArray.length(); i++) {
                    if (jsonArray.get(i).equals(hitTips)) {
                        isHit = true;
                    }
                }
                if(!isHit){
                    jsonArray.put(hitTips);
                }
            }
            jsonObject.put(key, jsonArray);
            HelpPreferenceUtil.getInstance().setString("RS_VOICE_USUAL_SPEAK_GRAMMAR_TIPS", jsonObject.toString().replace("\\",""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private boolean hasWebchatTool(){
        if (!PackageManager.getInstance().checkAppExist("com.txznet.webchat")) {
            return false;
        }
        return true;
    }

    private boolean hasCallTool(){
        if (!CallManager.getInstance().hasRemoteProcTool()) {
            return false;
        }
        return true;
    }

    private boolean hasNavTool(){
        if(!"".equals(NavManager.getInstance().getDisableResaon())){//没安装导航工具
           return false;
        }
        return true;
    }

    private boolean hasMusicTool(){
        IMediaTool tool = MusicPriorityChooser.getInstance().getMediaTool(null);
        if (tool != null) {
            return true;
        }
        return false;
    }

    private boolean hasRadioTool(){
        IMediaTool tool = AudioPriorityChooser.getInstance().getMediaTool(null);
        if (tool != null) {
            return true;
        }
        return false;
    }

}
