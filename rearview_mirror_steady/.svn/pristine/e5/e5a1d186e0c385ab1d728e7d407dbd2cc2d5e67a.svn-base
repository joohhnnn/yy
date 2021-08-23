package com.txznet.music.push.bean;

import com.google.gson.annotations.SerializedName;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;

import java.util.List;

/**
 * Created by ASUS User on 2017/1/3.
 */

public class PushResponse {

    //前置行为
    /**
     * 强制播放list字段中第一个audio
     */
    public static final int PRE_ACTION_FORCE_PLAY = 1;

    /**
     * 如果mp3不为空则播放mp3，否则播报tts字段
     */
    public static final int PRE_ACTION_PLAY_URL_OR_TTS = 2;

    //显示样式
    public static final String SHOW_STYLE_SHORT_MODEL = "shortModal";

    //推送业务类型
    /**
     * 快报
     */
    public static final String PUSH_SERVICE_SHORT_PLAY = "shortPlay";

    /**
     * 强制播放
     */
    public static final String PUSH_SERVICE_FORCE_PLAY = "forcePlay";

    /**
     * 专辑更新
     */
    public static final String PUSH_SERVICE_UPDATE = "update";

    /**
     * 手机推送音频
     */
    public static final String PUSH_SERVICE_AUDIOS = "pushAudio";


    //后置行为
    /**
     * 播放postFlag填入的专辑id
     */
    public static final int POST_ACTION_PLAY_ALBUM = 1;

    /**
     * 播放考拉头条FM
     */
    public static final int POST_ACTION_PLAY_NEW = 2;

    /**
     * 播放考拉私人音乐
     */
    public static final int POST_ACTION_PLAY_PRIVATE_MUSIC = 3;

    /**
     * 播放arrAudio字段携带的音乐列表
     */
    public static final int POST_ACTION_PLAY_MUSIC_LIST = 4;

    /**
     * 播放albumWrappers里面的专辑，如果有多个则打开订阅界面
     */
    public static final int POST_ACTION_PLAY_ALBUMS = 5;

    /**
     * 不再提示
     */
    public static final int POST_ACTION_NO_PROMPT = 6;

    private long time;

    private String subTitle;

    private List<AlbumWrapper> arrAlbumWrappers;

    /**
     * 文本播报
     */
    private String tts;

    /**
     * 提示音
     */
    private String tip;

    /**
     * 音频播放
     */
    private String mp3;


    private List<Key> arrKeys;
    private String title;
    private String service;
    @SerializedName("show_style")
    private String showStyle;
    @SerializedName("iconurl")
    private String iconUrl;
    private List<Audio> arrAudio;
    @SerializedName("pre_action")
    private int preAction;
    @SerializedName("post_action")
    private int postAction;
    @SerializedName("post_falg")
    private String postFlag;
    private String albumName;
    private long sid;
    private int type;//类别：快报推送（0）微信推送（1）更新推送（2） 热点推送（3）个性化主动推送（4）
    private String mid;//用于上报用的，后台知道，客户端只做上报，不用于具体事务
    private int defUserChoose; // 是否默认选择，0 否 1 是，若默认选择则交互替换为自动播放，取消和不再提示的交互
    private String endTip; // 结束提示音

    private String reportData;

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public long getSid() {
        return sid;
    }

    public void setSid(long sid) {
        this.sid = sid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getShowStyle() {
        return showStyle;
    }

    public void setShowStyle(String showStyle) {
        this.showStyle = showStyle;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<Audio> getArrAudio() {
        return arrAudio;
    }

    public void setArrAudio(List<Audio> list) {
        this.arrAudio = list;
    }

    public int getPreAction() {
        return preAction;
    }

    public void setPreAction(int preAction) {
        this.preAction = preAction;
    }

    public int getPostAction() {
        return postAction;
    }

    public void setPostAction(int postAction) {
        this.postAction = postAction;
    }

    public String getPostFlag() {
        return postFlag;
    }

    public void setPostFlag(String postFlag) {
        this.postFlag = postFlag;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public List<AlbumWrapper> getArrAlbumWrappers() {
        return arrAlbumWrappers;
    }

    public void setArrAlbumWrappers(List<AlbumWrapper> arrAlbumWrappers) {
        this.arrAlbumWrappers = arrAlbumWrappers;
    }

    public String getTts() {
        return tts;
    }

    public void setTts(String tts) {
        this.tts = tts;
    }

    public String getMp3() {
        return mp3;
    }

    public void setMp3(String mp3) {
        this.mp3 = mp3;
    }

    public List<Key> getArrKeys() {
        return arrKeys;
    }

    public void setArrKeys(List<Key> arrKeys) {
        this.arrKeys = arrKeys;
    }

    public int getDefUserChoose() {
        return defUserChoose;
    }

    public void setDefUserChoose(int defUserChoose) {
        this.defUserChoose = defUserChoose;
    }

    public String getEndTip() {
        return endTip;
    }

    public void setEndTip(String endTip) {
        this.endTip = endTip;
    }

    public String getReportData() {
        return reportData;
    }

    public void setReportData(String reportData) {
        this.reportData = reportData;
    }

    @Override
    public String toString() {
        return "PushResponse{" +
                "time=" + time +
                ", subTitle='" + subTitle + '\'' +
                ", arrAlbumWrappers=" + arrAlbumWrappers +
                ", tts='" + tts + '\'' +
                ", tip='" + tip + '\'' +
                ", mp3='" + mp3 + '\'' +
                ", arrKeys=" + arrKeys +
                ", title='" + title + '\'' +
                ", service='" + service + '\'' +
                ", showStyle='" + showStyle + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", arrAudio=" + arrAudio +
                ", preAction=" + preAction +
                ", postAction=" + postAction +
                ", postFlag='" + postFlag + '\'' +
                ", albumName='" + albumName + '\'' +
                ", sid=" + sid +
                ", type=" + type +
                ", mid='" + mid + '\'' +
                ", defUserChoose=" + defUserChoose +
                ", endTip='" + endTip + '\'' +
                ", reportData='" + reportData + '\'' +
                '}';
    }

    public static class AlbumWrapper {

        private String title;
        private Album album;


        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Album getAlbum() {
            return album;
        }

        public void setAlbum(Album album) {
            this.album = album;
        }

        @Override
        public String toString() {
            return "AlbumWrapper{" +
                    "title='" + title + '\'' +
                    ", album=" + album +
                    '}';
        }
    }

    public static class Key {
        private String text;
        private List<String> arrCms;


        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<String> getArrCms() {
            return arrCms;
        }

        public void setArrCms(List<String> cmds) {
            this.arrCms = cmds;
        }


        @Override
        public String toString() {
            return "Key{" +
                    "text='" + text + '\'' +
                    ", arrCms=" + arrCms +
                    '}';
        }
    }
}
