package com.txznet.sdk;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;

import java.util.List;

public class TXZJinshouzhiManager {
    public static final int TYPE_HOME = 1;
    public static final int TYPE_MUSIC = 2;
    public static final int TYPE_RADIO = 3;
    public static final int TYPE_NAV = 4;
    public static final int TYPE_WX = 5;
    private static final String REGISTER = "txz.help.guide.registerTipsInfo";
    private static final String UNREGISTER = "txz.help.guide.unRegisterTipsInfo";
    private static final String CLOSE_WINDOW = "txz.help.guide.helpWindowsClose";
    private static final String JUMP_GUIDE_ANIM = "txz.help.guide.finishGuideAnim";//通知关闭新手引导

    private static TXZJinshouzhiManager manager = null;

    private TXZJinshouzhiManager() {

    }

    public static TXZJinshouzhiManager getInstance() {
        if (manager == null) {
            synchronized (TXZJinshouzhiManager.class) {
                if (manager == null) {
                    manager = new TXZJinshouzhiManager();
                }
            }
        }
        return manager;
    }

    /**
     * 微信前台
     *
     */
    @Deprecated
    public void wxForeSceneShowTips(boolean isOpenWindows) {
//        JSONBuilder jsonBuilder = new JSONBuilder();
//        jsonBuilder.put("foreScene", TYPE_WX);
//        jsonBuilder.put("isOpenWindows", isOpenWindows);
//        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, REGISTER, jsonBuilder.toBytes(), null);
    }

    /**
     * 微信录音中
     * @param wpLists 指令
     */
    @Deprecated
    public void wxRecordingSceneShowTips(List<String> wpLists) {
//        JSONBuilder jsonBuilder = new JSONBuilder();
//        jsonBuilder.put("foreScene", TYPE_WX);
//        jsonBuilder.put("wpLists", wpLists);
//        jsonBuilder.put("isRecording", true);
//        jsonBuilder.put("isOpenWindows", true);
//        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, REGISTER, jsonBuilder.toBytes(), null);
    }

    /**
     * 微信收到消息
     * @param wpLists
     */
    @Deprecated
    public void wxGetWXInfoSceneShowTips(List<String> wpLists) {
//        JSONBuilder jsonBuilder = new JSONBuilder();
//        jsonBuilder.put("foreScene", TYPE_WX);
//        jsonBuilder.put("wpLists", wpLists);
//        jsonBuilder.put("isGetWXInfo", true);
//        jsonBuilder.put("isOpenWindows", true);
//        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, REGISTER, jsonBuilder.toBytes(), null);
    }

    /**
     * 桌面场景
     * @param wpLists
     * @param isOpenWindows
     */
    public void homeSceneShowTips(List<String> wpLists, boolean isOpenWindows) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("foreScene", TYPE_HOME);
        jsonBuilder.put("wpLists", wpLists);
        jsonBuilder.put("isOpenWindows", isOpenWindows);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, REGISTER, jsonBuilder.toBytes(), null);
    }

    /**
     * 音乐在前台场景
     * @param wpLists
     * @param isOpenWindows
     */
    public void musicForeSceneShowTips(List<String> wpLists, boolean isOpenWindows){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("foreScene", TYPE_MUSIC);
        jsonBuilder.put("wpLists", wpLists);
        jsonBuilder.put("isOpenWindows", isOpenWindows);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, REGISTER, jsonBuilder.toBytes(), null);
    }

    /**
     * 音乐在后台场景
     * @param wpLists
     * @param isOpenWindows
     */
    public void musicBackgroundSceneShowTips(List<String> wpLists, boolean isOpenWindows){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("backgroundScene", TYPE_MUSIC);
        jsonBuilder.put("wpLists", wpLists);
        jsonBuilder.put("isOpenWindows", isOpenWindows);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, REGISTER, jsonBuilder.toBytes(), null);
    }

    /**
     * 电台在前台场景
     * @param wpLists
     * @param isOpenWindows
     */
    public void radioForeSceneShowTips(List<String> wpLists, boolean isOpenWindows){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("foreScene", TYPE_RADIO);
        jsonBuilder.put("wpLists", wpLists);
        jsonBuilder.put("isOpenWindows", isOpenWindows);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, REGISTER, jsonBuilder.toBytes(), null);
    }

    /**
     * 电台在后台场景
     * @param wpLists
     * @param isOpenWindows
     */
    public void radioBackgroundSceneShowTips(List<String> wpLists, boolean isOpenWindows){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("backgroundScene", TYPE_RADIO);
        jsonBuilder.put("wpLists", wpLists);
        jsonBuilder.put("isOpenWindows", isOpenWindows);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, REGISTER, jsonBuilder.toBytes(), null);
    }

    /**
     *导航前台场景
     * @param wpLists
     * @param isNav 是否在规划路线
     * @param isOpenWindows
     */
    public void navForeSceneShowTips(List<String> wpLists,boolean isNav, boolean isOpenWindows){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("foreScene", TYPE_NAV);
        jsonBuilder.put("isNav",isNav);
        jsonBuilder.put("wpLists", wpLists);
        jsonBuilder.put("isOpenWindows", isOpenWindows);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, REGISTER, jsonBuilder.toBytes(), null);
    }

    /**
     * 其它场景
     * @param foreScene 前台场景（0表示不是前台场景）
     * @param backgroundScene 后台场景（0表示不是后台场景）
     * @param wpLists
     * @param isOpenWindows
     */
    public void otherSceneShowTips(int foreScene,int backgroundScene,List<String> wpLists,boolean isOpenWindows){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("foreScene", foreScene);
        jsonBuilder.put("backgroundScene", backgroundScene);
        jsonBuilder.put("wpLists", wpLists);
        jsonBuilder.put("isOpenWindows", isOpenWindows);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, REGISTER, jsonBuilder.toBytes(), null);

    }

    public void closeWindow(){
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, CLOSE_WINDOW, null, null);
    }

    public void wxUnregister(){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("foreScene",TYPE_WX);
        unregisterShowTips(jsonBuilder);
    }

    public void musicUnregister(){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("foreScene",TYPE_MUSIC);
        unregisterShowTips(jsonBuilder);
    }

    public void musicBackgroundUnregister(){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("backgroundScene",TYPE_MUSIC);
        unregisterShowTips(jsonBuilder);
    }

    public void radioUnregister(){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("foreScene",TYPE_RADIO);
        unregisterShowTips(jsonBuilder);
    }

    public void navUnregister(){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("foreScene",TYPE_NAV);
        unregisterShowTips(jsonBuilder);
    }

    public void radioBackgroundUnregister(){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("backgroundScene",TYPE_RADIO);
        unregisterShowTips(jsonBuilder);
    }

    public void homeUnregister(){
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("foreScene",TYPE_HOME);
        unregisterShowTips(jsonBuilder);
    }

    public void unregisterShowTips(JSONBuilder jsonBuilder){
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, UNREGISTER, jsonBuilder.toBytes(), null);
    }

    /**
     * 通知结束新手引导
     */
    public void jumpGuideAnim(){
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, JUMP_GUIDE_ANIM, null, null);
    }
}
