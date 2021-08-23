//package com.txznet.music.soundControlModule.logic;
//
//import com.alibaba.fastjson.JSONArray;
//import com.txznet.comm.remote.util.LogUtil;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.baseModule.bean.EnumState;
//import com.txznet.music.soundControlModule.asr.AsrManager;
//import com.txznet.music.soundControlModule.logic.net.request.ReqSearch;
//import com.txznet.music.utils.SharedPreferencesUtils;
//import com.txznet.music.utils.StringUtils;
//
//import java.util.List;
//
//
///**
// * Created by telenewbie on 2016/12/23.
// */
//
//public class SoundAllCallback implements ISoundCommand, ISoundCommon, ISoundSearch, IThirdSetting, ISettingInner, IFackLogic {
//
//    private static final String TAG = "music::sound::callback";
//    private static final int LENGTH = 20;
//    //##创建一个单例类##
//    private volatile static SoundAllCallback singleton;
//
//    private SoundAllCallback() {
//    }
//
//    public static SoundAllCallback getInstance() {
//        if (singleton == null) {
//            synchronized (SoundAllCallback.class) {
//                if (singleton == null) {
//                    singleton = new SoundAllCallback();
//                }
//            }
//        }
//        return singleton;
//    }
//
//    @Override
//    public void loadPlugin() {
//
//    }
//
//    @Override
//    public byte[] playAudio() {
//        return SoundReport.getInstance().playAudio();
//    }
//
//    @Override
//    public byte[] playMusic() {
//        return SoundReport.getInstance().playMusic();
//    }
//
//    @Override
//    public byte[] playRecommandMusic() {
//        return SoundReport.getInstance().playRecommandMusic();
//    }
//
//    @Override
//    public byte[] pause() {
//        return SoundReport.getInstance().pause();
//    }
//
//    @Override
//    public byte[] play() {
//        return SoundReport.getInstance().play();
//    }
//
//    @Override
//    public byte[] next() {
//        return SoundReport.getInstance().next();
//    }
//
//    @Override
//    public byte[] prev() {
//        return SoundReport.getInstance().prev();
//    }
//
//    @Override
//    public byte[] exit() {
//        return SoundReport.getInstance().exit();
//    }
//
//    @Override
//    public byte[] changeSingleMode(EnumState.PlayMode mode) {
//        return SoundReport.getInstance().changeSingleMode(mode);
//    }
//
//    @Override
//    public byte[] open() {
//        return SoundReport.getInstance().open();
//    }
//
//    @Override
//    public byte[] favour(byte[] objects) {
//        return SoundReport.getInstance().favour(objects);
//    }
//
//    @Override
//    public byte[] playfavour() {
//        return SoundReport.getInstance().playfavour();
//    }
//
//    @Override
//    public byte[] hateAudio() {
//        return SoundReport.getInstance().hateAudio();
//    }
//
//    @Override
//    public byte[] isPlaying() {
//        return SoundCommon.getInstance().isPlaying();
//    }
//
//    @Override
//    public byte[] getVersion() {
//        return SoundCommon.getInstance().getVersion();
//    }
//
//
//    private ReqSearch reqData = null;
//    private JSONArray array = new JSONArray();
//    private List<Audio> audios;
//
//    @Override
//    public void setPlayWhenBoot(boolean playOrNot) {
//        SharedPreferencesUtils.setAppFirstPlay(playOrNot);
//    }
//
//    @Override
//    public void setNeedAsr(boolean need) {
//        SharedPreferencesUtils.setNeedAsr(need);
//        if (!need) {
//            // 反注册掉相应的全局唤醒字
//            AsrManager.getInstance().unregAsrCommand();
//        } else {
//            AsrManager.getInstance().regAsrCommand();
//        }
//    }
//
//    @Override
//    public void setSearchSize(long searchSize) {
//        try {
//
//            SharedPreferencesUtils.setSearchSize(searchSize);
//        } catch (Exception e) {
//            LogUtil.logd(TAG + "set search size error：" + searchSize);
//        }
//    }
//
//    @Override
//    public void setNotOpenAppPName(String pName) {
//        SharedPreferencesUtils.setNotOpenAppPName(pName);
//    }
//
//    @Override
//    public void setSearchPath(String path) {
////        if ()
//        LogUtil.logd(TAG + "search path =" + path);
////        if(StringUtil.isNotEmpty(path)&& StringUtils.isInteger(path)){
////            SharedPreferencesUtils.setSearchSize(Long.parseLong(path));
////        }
//        if (StringUtils.isNotEmpty(path)) {
//            SharedPreferencesUtils.setLocalPaths(path);
//        }
//    }
//
//    @Override
//    public void setBackVisible(boolean visible) {
//        SharedPreferencesUtils.setBackVisible(visible);
//    }
//
//    @Override
//    public void setExtraTypeface(boolean needExtra) {
//        SharedPreferencesUtils.setExtraTypeFace(needExtra);
//    }
//
//    @Override
//    public void setExitWithPlay(boolean play) {
//        SharedPreferencesUtils.setExitWithPlay(play);
//    }
//
//    @Override
//    public void searchContent(String json) {
//        SoundReport.getInstance().searchContent(json);
//    }
//
//    @Override
//    public void searchResult(byte[] result) {
//        SoundReport.getInstance().searchResult(result);
//    }
//
//    @Override
//    public void searchChoiceIndex(int position) {
//        SoundReport.getInstance().searchChoiceIndex(position);
//    }
//
//    @Override
//    public void searchPreloadIndex(int position) {
//        SoundReport.getInstance().searchPreloadIndex(position);
//    }
//
//    @Override
//    public void doFakeReq(String data) {
//        FackLogic.getInstance().doFakeReq(data);
//    }
//}
