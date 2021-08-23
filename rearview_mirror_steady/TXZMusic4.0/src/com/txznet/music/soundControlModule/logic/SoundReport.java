//package com.txznet.music.soundControlModule.logic;
//
//
//import com.txznet.music.baseModule.bean.EnumState;
//import com.txznet.music.baseModule.net.request.ReqDataStats;
//import com.txznet.music.net.NetManager;
//import com.txznet.music.utils.NetHelp;
//
///**
// * 声控上报工具类
// * Created by telenewbie on 2017/1/15.
// */
//
//public class SoundReport implements ISoundCommand, ISoundSearch {
//
//    //##创建一个单例类##
//    private volatile static SoundReport singleton;
//
//    private SoundReport() {
//    }
//
//    public static SoundReport getInstance() {
//        if (singleton == null) {
//            synchronized (SoundReport.class) {
//                if (singleton == null) {
//                    singleton = new SoundReport();
//                }
//            }
//        }
//        return singleton;
//    }
//
//    @Override
//    public byte[] playAudio() {
////        Reporter.SoundReportHelper.getInstance().playAudio();
//        return SoundCommand.getInstance().playAudio();
//    }
//
//    @Override
//    public byte[] playMusic() {
////        Reporter.SoundReportHelper.getInstance().playMusic();
//        return SoundCommand.getInstance().playMusic();
//    }
//
//    @Override
//    public byte[] playRecommandMusic() {
////        Reporter.SoundReportHelper.getInstance().playRecommandMusic();
//        return SoundCommand.getInstance().playRecommandMusic();
//    }
//
//    @Override
//    public byte[] pause() {
//        NetManager.getInstance().sendReportData(ReqDataStats.Action.PAUSE_SOUND);
//        return SoundCommand.getInstance().pause();
//    }
//
//    @Override
//    public byte[] play() {
//        NetManager.getInstance().sendReportData(ReqDataStats.Action.PLAY_SOUND);
//        return SoundCommand.getInstance().play();
//    }
//
//    @Override
//    public byte[] next() {
////        Reporter.SoundReportHelper.getInstance().next();
//        return SoundCommand.getInstance().next();
//    }
//
//    @Override
//    public byte[] prev() {
////        Reporter.SoundReportHelper.getInstance().prev();
//        return SoundCommand.getInstance().prev();
//    }
//
//    @Override
//    public byte[] exit() {
////        Reporter.SoundReportHelper.getInstance().exit();
//        return SoundCommand.getInstance().exit();
//    }
//
//    @Override
//    public byte[] changeSingleMode(EnumState.PlayMode mode) {
////        Reporter.SoundReportHelper.getInstance().changeSingleMode(mode);
//        return SoundCommand.getInstance().changeSingleMode(mode);
//    }
//
//    @Override
//    public byte[] open() {
////        Reporter.SoundReportHelper.getInstance().open();
//        return SoundCommand.getInstance().open();
//    }
//
//    @Override
//    public byte[] favour(byte[] objects) {
////        Reporter.SoundReportHelper.getInstance().favour(objects);
//        return SoundCommand.getInstance().favour(objects);
//    }
//
//    @Override
//    public byte[] playfavour() {
////        Reporter.SoundReportHelper.getInstance().playfavour();
//        return SoundCommand.getInstance().playfavour();
//    }
//
//    @Override
//    public byte[] hateAudio() {
////        Reporter.SoundReportHelper.getInstance().hateAudio();
//        return SoundCommand.getInstance().hateAudio();
//    }
//
//    @Override
//    public void searchContent(String json) {
////        Reporter.SoundReportHelper.getInstance().searchContent(json);
//        SoundSearch.getInstance().searchContent(json);
//    }
//
//    @Override
//    public void searchResult(byte[] result) {
//        SoundSearch.getInstance().searchResult(result);
//    }
//
//    @Override
//    public void searchChoiceIndex(int position) {
//        SoundSearch.getInstance().searchChoiceIndex(position);
//    }
//
//    @Override
//    public void searchPreloadIndex(int position) {
//        SoundSearch.getInstance().searchPreloadIndex(position);
//    }
//}
