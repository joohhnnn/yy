package com.txznet.sdk;

import android.util.Log;
import android.widget.Toast;

import com.txznet.comm.base.music.IMusicProgress;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.sdk.TXZService.CommandProcessor;
import com.txznet.sdk.media.AbsTXZMusicTool;
import com.txznet.sdk.media.MediaToolSearchConfig;
import com.txznet.sdk.media.constant.InvokeConstants;
import com.txznet.sdk.music.TXZMusicTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * 类名：音乐管理器
 * 用途：语音默认适配电台之家、同听、酷我、QQ音乐等主流车载音乐APP，语音检测到对应APP时，默认控制对应音乐类型。
 *       当默认音乐逻辑不满足需求或需要适配音乐控制其它音乐APP时，通过此类设置音乐工具（AbsMusicTool），修改默认音乐逻辑。
 */
public class TXZMusicManager {
    private static TXZMusicManager sInstance = new TXZMusicManager();

    // 音乐工具搜索参数设置缓存
    private HashMap<MusicToolType, MediaToolSearchConfig> mSearchConfigMap =
            new HashMap<MusicToolType, MediaToolSearchConfig>();

    private TXZMusicManager() {

    }

    /**
     * 获取单例
     *
     * @return 类实例
     */
    public static TXZMusicManager getInstance() {
        return sInstance;
    }

    /**
     * 重连时需要重新通知同行者的操作放这里
     */
    void onReconnectTXZ() {
        if (mHasSetTool) {
            if (null == mMusicTool) {
                setMusicTool((MusicToolType) null);
            } else if (mMusicTool instanceof MusicToolType) {
                setMusicTool((MusicToolType) mMusicTool);
            } else if (mMusicTool instanceof MusicTool) {
                setMusicTool((MusicTool) mMusicTool);
            } else if (mMusicTool instanceof AbsTXZMusicTool) {
                setMusicTool((AbsTXZMusicTool) mMusicTool);
            }
        }
        if (mHasSetAudioTool) {
            setDefaultAudioTool(mAudioTool);
        }
        if (mLastSyncMusics != null) {
            syncMuicList(mLastSyncMusics);
        }
        if (mLastSyncExMusics != null) {
            syncExMuicList(mLastSyncExMusics);
        }
        if (mShowKuwoSearchResult != null) {
            showKuwoSearchResult(mShowKuwoSearchResult);
        }
        if (mTTTaskId != null) {
            setTTMusicControlTaskId(mTTTaskId);
        }

        restoreSearchConfig();
    }


    /**
     * 类名：音乐模型
     * 类描述：音乐实例抽象化类，包含音乐相关信息
     */
    public static class MusicModel {
        /**
         * 变量名：标题
         * 变量解释：音乐标题名
         */
        protected String title;
        /**
         * 变量名：专辑
         * 变量解释：音乐所属专辑名
         */
        protected String album;
        /**
         * 变量名：艺术家
         * 变量解释：音乐所属表演艺术家，字符串数组
         */
        protected String[] artist;
        /**
         * 变量名：关键字
         * 变量解释：音乐类型关键字，如：轻音乐
         */
        protected String[] keywords;
        /**
         * 变量名：路径
         * 变量解释：音乐播放URL，可能为空
         */
        protected String path;
        /**
         * 变量名：标志位
         * 变量解释：音乐类标志位，默认1，预留项
         */
        protected int field;
        /**
         * 变量名：搜索文本
         * 变量解释：用户输入的语义文本
         */
        protected String text;
        /**
         * 变量名：子分类
         * 变量解释：音乐所属分类，如：60后经典
         */
        protected String subCategory;

        /**
         * 方法名：搜索文本
         * 方法描述：获取用户输入的语义文本
         *
         * @return 文本
         */
        public String getText() {
            return text;
        }

        /**
         * 方法名：设置搜索文本
         * 方法描述：设置用户输入的语义文本
         */
        public void setText(String text) {
            this.text = text;
        }

        /**
         * 方法名：获取子分类
         * 方法描述：获取音乐所属分类
         *
         * @return 分类
         */
        public String getSubCategory() {
            return subCategory;
        }

        /**
         * 方法名：设置子分类
         * 方法描述：设置音乐所属分类
         */
        public void setSubCategory(String subCategory) {
            this.subCategory = subCategory;
        }

        /**
         * 方法名：获取标志位
         * 方法描述：获取音乐类标志位
         *
         * @return 标志位
         */
        public int getField() {
            return field;
        }

        /**
         * 方法名：设置标志位
         * 方法描述：设置音乐类标志位
         */
        public void setField(int field) {
            this.field = field;
        }

        /**
         * 方法名：获取标题
         * 方法描述：获取音乐标题名
         *
         * @return 标题名
         */
        public String getTitle() {
            return title;
        }


        /**
         * 方法名：设置标题
         * 方法描述：设置音乐标题名
         */
        public void setTitle(String title) {
            this.title = title;
        }

        /**
         * 方法名：获取专辑
         * 方法描述：获取音乐所属专辑名
         *
         * @return 专辑名
         */
        public String getAlbum() {
            return album;
        }

        /**
         * 方法名：设置专辑
         * 方法描述：设置音乐所属专辑名
         */
        public void setAlbum(String album) {
            this.album = album;
        }

        /**
         * 方法名：获取艺术家
         * 方法描述：获取音乐音乐所属表演艺术家
         *
         * @return 艺术家们
         */
        public String[] getArtist() {
            return artist;
        }

        /**
         * 方法名：设置艺术家
         * 方法描述：设置音乐音乐所属表演艺术家
         */
        public void setArtist(String[] artist) {
            this.artist = artist;
        }

        /**
         * 方法名：获取关键字
         * 方法描述：获取音乐类型关键字
         *
         * @return 关键字
         */
        public String[] getKeywords() {
            return keywords;
        }

        /**
         * 方法名：设置关键字
         * 方法描述：设置音乐类型关键字
         */
        public void setKeywords(String[] keywords) {
            this.keywords = keywords;
        }

        /**
         * 方法名：获取路径
         * 方法描述：获取音乐播放URL
         *
         * @return URL字符串
         */
        public String getPath() {
            return this.path;
        }

        /**
         * 方法名：设置路径
         * 方法描述：设置音乐播放URL
         */
        public void setPath(String path) {
            this.path = path;
        }

        /**
         * 方法名：获取音乐实体json
         * 方法描述：文本化当前音乐实例为json
         *
         * @return 实例json文本
         */
        @Override
        public String toString() {
            try {
                JSONObject json = new JSONObject();
                json.put("title", title);
                json.put("album", album);
                JSONArray jsonArtists = new JSONArray();
                if (artist != null) {
                    for (int i = 0; i < artist.length; ++i) {
                        if (null != artist[i]) {
                            jsonArtists.put(artist[i]);
                        }
                    }
                }
                json.put("artist", jsonArtists);
                JSONArray jsonKeywords = new JSONArray();
                if (keywords != null) {
                    for (int i = 0; i < keywords.length; ++i) {
                        if (null != keywords[i]) {
                            jsonKeywords.put(keywords[i]);
                        }
                    }
                }
                json.put("keywords", jsonKeywords);
                json.put("field", field);
                json.put("path", path);
                json.put("text", text);
                json.put("subcategory", subCategory);
                return json.toString();
            } catch (Exception e) {
                return null;
            }
        }

        public static String collecionToString(Collection<MusicModel> musics) {
            JSONArray json = new JSONArray();
            for (MusicModel m : musics) {
                json.put(m.toString());
            }
            return json.toString();
        }

        public static Collection<MusicModel> collecionFromString(String data) {
            try {
                JSONArray json = new JSONArray(data);
                Collection<MusicModel> musics = new ArrayList<MusicModel>(json.length());
                for (int i = 0; i < json.length(); ++i) {
                    musics.add(fromString(json.getString(i)));
                }
                return musics;
            } catch (JSONException e) {
                return null;
            }
        }

        public static MusicModel fromString(String data) {
            try {
                MusicModel model = new MusicModel();
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                model.title = jsonBuilder.getVal("title", String.class);
                model.album = jsonBuilder.getVal("album", String.class);
                model.path = jsonBuilder.getVal("path", String.class);
                model.artist = jsonBuilder.getVal("artist", String[].class);
                model.keywords = jsonBuilder.getVal("keywords", String[].class);
                model.text = jsonBuilder.getVal("text", String.class, "");
                model.field = jsonBuilder.getVal("field", int.class, 0);
                return model;
            } catch (Exception e) {
                LogUtil.loge("MusicModel from json error: " + data, e);
                return null;
            }
        }
    }

    /**
     * 接口名：音乐工具状态变化监听器
     * 接口描述：外部音乐状态变化时，通过此监听器通知语音
     */
    public static interface MusicToolStatusListener extends IMusicProgress {
        /**
         * 变量名：未知状态
         * 变量描述：音乐状态，未知
         */
        public static final int STATE_UNKNOW = 0;
        /**
         * 变量名：开始播放
         * 变量描述：音乐状态，开始播放，包括继续播放
         */
        public static final int STATE_START_PLAY = 1;
        /**
         * 变量名：暂停播放
         * 变量描述：音乐状态，暂停播放
         */
        public static final int STATE_PAUSE_PLAY = 2;
        /**
         * 变量名：缓冲中
         * 变量描述：音乐状态，缓冲中
         */
        public static final int STATE_BUFFERING = 3;
        /**
         * 变量名：曲目变化
         * 变量描述：音乐状态，曲目变化
         */
        public static final int STATE_SONG_CHANGE = 4;

        /**
         * 方法名：音乐状态发生改变
         * 方法描述：音乐状态发生改变通知语音，如开始播放、暂停播放(非tts等引发的临时暂停)、缓冲中、曲目变化
         */
        public void onStatusChange();

        /**
         * 方法名：音乐状态发生改变（携带状态）
         * 方法描述：音乐状态发生改变通知语音，如开始播放、暂停播放(非tts等引发的临时暂停)、缓冲中、曲目变化
         *
         * @param state 将要变化的状态
         */
        public void onStatusChange(int state);

        /**
         * 方法名：播放指定音乐模型
         * 方法描述：通知语音播放指定音乐模型
         *
         * @param mm 音乐模型
         */
        public void playMusic(MusicModel mm);

        /**
         * 方法名：播放结束并播放下一曲
         * 方法描述：通知语音播放结束并播放下一曲
         *
         * @param nextModule 下一曲
         */
        public void endMusic(MusicModel nextModule);
    }

    /**
     * @see AbsTXZMusicTool
     * 接口名：音乐工具
     * 接口描述：音量接口实例化，通过重写此接口实现重写或适配其它音乐逻辑
     * @deprecated 已过时
     */
    public static interface MusicTool {
        /**
         * 方法名：获取当前音乐播放状态
         * 方法描述：实现此接口，用户输入播放状态主义时，语音通过此方法判断当前音乐播放状态
         *
         * @return 是否正在播放
         */
        public boolean isPlaying();

        /**
         * 方法名：开始播放
         * 方法描述：实现此接口，用户语音输入“开始播放”时，语音通过此方法控制音乐
         */
        public void play();

        /**
         * 方法名：继续播放
         * 方法描述：实现此接口，用户语音输入“继续播放”时，语音通过此方法控制音乐
         */
        public void continuePlay();

        /**
         * 方法名：暂停播放
         * 方法描述：实现此接口，用户语音输入“暂停播放”时，语音通过此方法控制音乐
         */
        public void pause();

        /**
         * 方法名：关闭音乐
         * 方法描述：实现此接口，用户语音输入“关闭音乐”时，语音通过此方法控制音乐
         */
        public void exit();

        /**
         * 方法名：下一首
         * 方法描述：实现此接口，用户语音输入“下一首”时，语音通过此方法控制音乐
         */
        public void next();

        /**
         * 方法名：上一首
         * 方法描述：实现此接口，用户语音输入“上一首”时，语音通过此方法控制音乐
         */
        public void prev();

        /**
         * 方法名：全部循环
         * 方法描述：实现此接口，用户语音输入“全部循环”时，语音通过此方法控制音乐
         */
        public void switchModeLoopAll();

        /**
         * 方法名：单曲循环
         * 方法描述：实现此接口，用户语音输入“单曲循环”时，语音通过此方法控制音乐
         */
        public void switchModeLoopOne();

        /**
         * 方法名：随机播放
         * 方法描述：实现此接口，用户语音输入“随机播放”时，语音通过此方法控制音乐
         */
        public void switchModeRandom();

        /**
         * 方法名：切换音乐
         * 方法描述：实现此接口，用户语音输入“切换音乐/换首歌”时，语音通过此方法控制音乐
         */
        public void switchSong();

        /**
         * 方法名：随便听听
         * 方法描述：实现此接口，用户语音输入“随便听听/随便来首歌”时，语音通过此方法控制音乐
         */
        public void playRandom();

        /**
         * 方法名：播放指定音乐
         * 方法描述：实现此接口，用户语音输入如:“播放张学友的歌”时，语音通过此方法控制音乐
         *
         * @param musicModel 指定的音乐
         */
        public void playMusic(MusicModel musicModel);

        /**
         * 方法名：获取当前正在播放的音乐模型
         * 方法描述：获取当前正在播放的音乐模型，没有播放返回null
         *
         * @return 当前正在播放的音乐模型
         */
        public MusicModel getCurrentMusicModel();

        /**
         * 方法名：收藏当前播放的歌曲
         * 方法描述：实现此接口，用户语音输入“收藏歌曲”时，语音通过此方法控制音乐
         */
        public void favourMusic();

        /**
         * 方法名：取消收藏当前播放的歌曲
         * 方法描述：实现此接口，用户语音输入“取消收藏歌”时，语音通过此方法控制音乐
         */
        public void unfavourMusic();

        /**
         * 方法名：播放收藏歌曲
         * 方法描述：实现此接口，用户语音输入“播放收藏歌曲”时，语音通过此方法控制音乐
         */
        public void playFavourMusic();


        /**
         * 方法名：音乐状态监听器
         * 方法描述：音乐状态监听器，保存此实例，以通知语音音乐播放状态
         */
        public void setStatusListener(MusicToolStatusListener listener);
    }

    /**
     * 接口名：扩展MusicTool方法
     * 接口描述：扩展MusicTool方法，提供额外方法
     */
    public static interface MusicToolEx extends MusicTool {

        /**
         * 方法名：是否需要Tts播报提示
         * 方法描述：实现此接口，语音通过此方法判断是否需要对应TTS播放
         *
         * @return true 需要播报 false 不需要播放
         */
        public boolean needTts();

    }

    private boolean mHasSetTool = false;
    private Object mMusicTool = null;
    private Boolean play = null;
    private String mSpeakContent;

    /**
     * 枚举类名：内置音乐工具类型
     * 枚举类描述：提供语音默认支持的音乐工具类型
     */
    public enum MusicToolType {
        /**
         * 同行者音乐工具：电台之家、同听
         */
        MUSIC_TOOL_TXZ,
        /**
         * 酷我音乐工具
         */
        MUSIC_TOOL_KUWO,
        /**
         * 考拉音乐工具
         */
        MUSIC_TOOL_KAOLA,

        /**
         * 远程工具，其它音乐类型
         */
        MUSIC_TOOL_REMOTE,
    }

    /**
     * 方法名：设置音乐工具类型
     * 方法描述：手动指定当前语音需要控制的音乐类型，如多音乐APP时，通过此方法切换
     *
     * @param type 内置音乐工具类型，自动按安装包选择，默认优先级：同行者>酷我>考拉
     */
    public void setMusicTool(MusicToolType type) {
        mHasSetTool = true;
        mMusicTool = type;
        if (type == null) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.cleartool",
                    null, null);
            return;
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.setInnerTool",
                type.name().getBytes(), null);
    }

    List<MusicToolStatusListener> mMusicToolStatusListeners;

    /**
     * 方法名：添加音乐监听器
     * 方法描述：添加额外音乐监听器
     *
     * @param mtsl 监听器
     */
    public void addMusicStatusListener(MusicToolStatusListener mtsl) {
        if (mtsl == null) {
            return;
        }

        if (mMusicToolStatusListeners == null) {
            mMusicToolStatusListeners = new ArrayList<TXZMusicManager.MusicToolStatusListener>();
        }

        boolean needNotify = false;
        if (mMusicToolStatusListeners.size() < 1) {
            needNotify = true;
        }
        mMusicToolStatusListeners.add(mtsl);
        if (needNotify) {
            TXZService.setCommandProcessor("tool.music.status.", new CommandProcessor() {

                @Override
                public byte[] process(String packageName, String command, byte[] data) {
                    if (mMusicToolStatusListeners == null) {
                        return null;
                    }
                    if (command.equals("onStatusChange")) {
                        int state = MusicToolStatusListener.STATE_UNKNOW;
                        try {
                            state = Integer.parseInt(new String(data));
                        } catch (Exception e) {
                        }

                        for (MusicToolStatusListener listener : mMusicToolStatusListeners) {
                            listener.onStatusChange(state);
                        }
                    }
                    if (command.equals("playMusic")) {
                        MusicModel mm = null;
                        try {
                            mm = MusicModel.fromString(new String(data));
                        } catch (Exception e) {
                        }
                        for (MusicToolStatusListener listener : mMusicToolStatusListeners) {
                            listener.playMusic(mm);
                        }
                    }
                    if (command.equals("endMusic")) {
                        MusicModel mm = null;
                        try {
                            mm = MusicModel.fromString(new String(data));
                        } catch (Exception e) {
                        }
                        for (MusicToolStatusListener listener : mMusicToolStatusListeners) {
                            listener.endMusic(mm);
                        }
                    }
                    if (command.equals("onProgress")) {
                        JSONBuilder json = new JSONBuilder(data);
                        for (MusicToolStatusListener listener : mMusicToolStatusListeners) {
                            listener.onProgress(json.getVal(
                                    MusicToolStatusListener.PROCESS_POSITION, int.class),
                                    json.getVal(MusicToolStatusListener.PROCESS_DURATION,
                                            int.class));
                        }
                    }
                    return null;
                }
            });
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    "txz.music.musiclistener.set", null, null);
        }
    }

    /**
     * 方法名：移除音乐状态监听器
     * 方法描述：通过实例移除已添加的监听器
     *
     * @param listener 监听器
     */
    public void removeMusicStatusListener(MusicToolStatusListener listener) {
        if (listener == null) {
            return;
        }
        if (mMusicToolStatusListeners != null && mMusicToolStatusListeners.contains(listener)) {
            mMusicToolStatusListeners.remove(listener);
        }

        if (mMusicToolStatusListeners.size() < 1) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    "txz.music.musiclistener.clear", null, null);
        }
    }

    /**
     * 方法名：设置App是否一启动就继续播放上次未关闭的音频
     * 方法描述：仅限电台之家APP
     *
     * @param play true 是 false 否，默认true
     */
    public void setStartAppPlay(Boolean play) {
        this.play = play;
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.startappplay",
                String.valueOf(play == null ? "true" : play).getBytes(), null);
    }

    /**
     * 方法名：设置App是否全屏
     * 方法名：仅限电台之家APP
     *
     * @param play true 是 false 否 默认true
     */
    @Deprecated
    public void setFullScreen(Boolean play) {
        this.play = play;
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.fullscreen",
                String.valueOf(play == null ? "true" : play).getBytes(), null);
    }

    /**
     * 方法名：设置App是否有返回按钮
     * 方法描述：仅限同听APP
     *
     * @param visible true 是 false 否 默认true
     */
    public void setBackbtnVisible(Boolean visible) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.backVisible",
                String.valueOf(visible == null ? "true" : visible).getBytes(), null);
    }

    /**
     * 方法名：设置App是否隐藏帮忙按钮
     * 方法描述：仅限同听APP
     *
     * @param visible true 是 false 否 默认true
     */
    public void setHideTips(Boolean visible) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.param.tips.show",
                String.valueOf(visible == null ? "true" : visible).getBytes(), null);
    }


    /**
     * 方法名：设置App提示框显示的位置
     * 方法描述：仅限同听APP
     *
     * @param position TOP 上，bottom 下
     */
    public void setTipShowPosition(String position) {
        if (StringUtils.isEmpty(position)) {
            Log.e("newbie", "setTipShowPosition你设置了" + position + ".不符合规范,使用默认样式TOP");
            return;
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.tips.gravity",
                position.toUpperCase().getBytes(), null);
    }

    /**
     * 方法名：设置是否开启悬浮播放器
     * 方法描述： 同听3.0以上版本支持
     *
     * @param enable true 开启 false 关闭
     */
    public void setEnableFloatingPlayer(Boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.enableFloatingPlayer",
                String.valueOf(enable == null ? "true" : enable).getBytes(), null);
    }

    /**
     * 方法名：设置是否开启闪屏页
     * 方法描述： 同听3.0以上版本支持
     *
     * @param enable true 开启 false 关闭
     */
    public void setEnableSplash(Boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.enableSplash",
                String.valueOf(enable == null ? "true" : enable).getBytes(), null);
    }

    /**
     * 方法名：设置是否自动打开播放页面
     * 方法描述： 同听3.0以上版本支持
     *
     * @param enable true 开启 false 关闭
     */
    public void setEnableAudoJumpPlayerPage(Boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.autoJumpPlayerPage",
                String.valueOf(enable == null ? "true" : enable).getBytes(), null);
    }

    /**
     * 方法名：设置是否关闭电台之家对声音的控制
     * 方法描述：仅限电台之家，电台之家默认集成了音量免唤醒功能，会导致自定义全局唤醒词“增大音量”等失效并且播放界面上下拖动不会有反应
     *
     * @param close true 是 false 否
     */
    public void setIsCloseVolume(Boolean close) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.closeVolume",
                String.valueOf(close == null ? "false" : close).getBytes(), null);
    }

    /**
     * 方法名：声控后是否打开播放器界面
     * 方法描述：仅限电台之家
     *
     * @param sContent null 声控后不打开 or value 其他应用的包名(当遇到该包名时不打开,建议增加自己的导航包名)
     */
    public void setNotOpenAppPName(String[] sContent) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("data", sContent);
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.notOpenAppPName",
                builder.toBytes(), null);
    }

    /**
     * 方法名：设置点击返回按键是否退出播放
     * 方法描述：仅限电台之家
     *
     * @param withplay true 为继续播放 false 为退出播放
     */
    public void setExitWithPlay(boolean withplay) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.setExitWithPlay",
                String.valueOf(withplay).getBytes(), null);
    }

    /**
     * 方法名：设置是否适用额外的字体库
     * 方法描述：支持韩文和日文，仅限电台之家
     */
    public void setExtraTypeface(boolean needExtra) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.setExtraTypeface",
                String.valueOf(needExtra).getBytes(), null);
    }

    /**
     * 方法名 :电台之家丢失音频焦点后是否释放焦点
     * 方法描述：仅限电台之家
     *
     * @param isRealse true则释放焦点，false则不释放 默认不释放
     */
    public void setReleaseAudioFocus(Boolean isRealse) {
        if (isRealse == null) {
            isRealse = Boolean.valueOf(true);
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.releaseAudioFocus",
                String.valueOf(isRealse).getBytes(), null);
    }

    /**
     * 方法名：设置电台之家在休眠唤醒后是否继续播放
     * 方法描述：仅限电台之家
     *
     * @param isPlay true则继续播放 false则不播放
     */
    public void setWakeupPlay(Boolean isPlay) {
        if (isPlay == null) {
            isPlay = Boolean.valueOf(true);
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.wakeupPlay",
                String.valueOf(isPlay).getBytes(), null);
    }


    /**
     * 方法名：设置打开同听后是否自动开始播放
     * 方法描述：仅限同听APP
     *
     * @param set true则自动播放 false则不播放 默认不播放
     */
    public void setResumeAutoPlay(boolean set) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.resume_auto_play",
                String.valueOf(set).getBytes(), null);
    }

    /**
     * 方法名：设置默认是否开启30s快报推送
     * 方法描述：同听3.0以上版本有效
     *
     * @param enable true开启 false关闭
     */
    public void setShortPlayEnable(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.shortPlayEnable",
                String.valueOf(enable).getBytes(), null);
    }

    /**
     * 方法名：设置30s快报是否需要手动触发
     * 方法描述：针对同听APP，默认开机触发，手动时需要调用对应方法
     *
     * @param enable true 设置快报需要手动触发 false 设置快报开机自动播放
     */
    public void setShortPlayNeedTrigger(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC,
                "music.setShortPlayNeedTrigger", String.valueOf(enable).getBytes(), null);
    }

    /**
     * 方法名：手动触发同听30s快报
     * 方法描述：针对同听APP，设置为手动触发时，需要调用此方法以触发
     */
    public void triggerShortPlay() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.triggerShortPlay",
                "".getBytes(), null);
    }

    /**
     * 方法名：设置点击返回退出同听时是否弹出对话框
     * 方法描述：同听3.1以上版本有效
     *
     * @param enable true开启 false关闭
     */
    public void setShowExitDialog(boolean enable) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.showExitDialog",
                String.valueOf(enable).getBytes(), null);
    }

    /**
     * 方法名：设置本地扫描最小扫描文件大小
     * 方法描述：仅限电台之家，默认最小文件为500K
     *
     * @param minSize 最小参数大小，单位KB
     */
    public void setLocalSearchSize(Integer minSize) {
        if (minSize != null && minSize >= 100 * 1024) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.searchSize",
                    String.valueOf(minSize).getBytes(), null);
        } else {
            LogUtil.logd("本地扫描设置参数错误,支持的范围[100K~+]");
        }
    }

    /**
     * 方法名：设置本地扫描路径
     * 方法描述：针对同听、电台之家，设置同听、电台之前描述音乐文件路径，将仅扫描对应路径
     *
     * @param paths 设置本地路径
     */
    public void setLocalPath(String[] paths) {
        // 判空
        if (null != paths && paths.length > 0) {

            JSONBuilder builder = new JSONBuilder();
            builder.put("data", paths);

            ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.searchPath",
                    builder.toBytes(), null);
        } else {
            Toast.makeText(GlobalContext.get(), "本地扫描路径设置错误", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 方法名：设置App是否启用全局唤醒词
     * 方法描述：仅限电台之家
     *
     * @param need true 是 false 否
     */
    public void setNeedAsr(Boolean need) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.needAsr",
                String.valueOf(need == null ? "true" : need).getBytes(), null);
    }


    /**
     * 方法名：设置同听是否注册免唤醒词的默认值
     * 方法描述：针对同听，用户修改后以用户设置为准
     *
     * @param defaultValue 默认值，默认注册“上一首”、“下一首”、“暂停播放”、“继续播放”、“开始播放”
     */
    public void setWakeupDefaultValue(boolean defaultValue) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, "music.wakeup_default",
                String.valueOf(defaultValue).getBytes(), null);
    }


    /**
     * 方法名：设置音乐工具
     * 方法描述：重写音乐逻辑或适配其它音乐逻辑时，可以使用此方法
     *
     * @param tool 音乐工具
     * @see AbsTXZMusicTool
     * @deprecated 已过时，推荐使用新类型
     */
    @Deprecated
    public void setMusicTool(final MusicTool tool) {
        mHasSetTool = true;
        mMusicTool = tool;

        if (tool == null) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    "txz.music.cleartool", null, null);
            return;
        }
        tool.setStatusListener(new MusicToolStatusListener() {
            JSONBuilder json = new JSONBuilder();

            @Override
            public void onStatusChange() {
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                        "txz.music.notifyMusicStatusChange", null, null);
            }

            @Override
            public void onStatusChange(int state) {
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                        "txz.music.notifyMusicStatusChange", (state + "").getBytes(), null);
            }

            @Override
            public void playMusic(MusicModel mm) {
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                        "txz.music.musiclistener.playMusic", mm.toString().getBytes(), null);
            }

            @Override
            public void endMusic(MusicModel nextModule) {
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                        "txz.music.musiclistener.endMusic", nextModule.toString().getBytes(), null);
            }

            @Override
            public void onProgress(int position, int duration) {
                //TODO
            }
        });
        TXZService.setCommandProcessor("tool.music.", new CommandProcessor() {
            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                if (command.equals("isPlaying")) {
                    return ("" + tool.isPlaying()).getBytes();
                }
                if (command.equals("play")) {
                    JSONBuilder jsonBuilder = new JSONBuilder(data);

                    if (jsonBuilder.getVal("continue", boolean.class, false)) {
                        tool.play();
                    } else {
                        tool.continuePlay();
                    }
                    return null;
                }
                if (command.equals("pause")) {
                    tool.pause();
                    return null;
                }
                if (command.equals("exit")) {
                    tool.exit();
                    return null;
                }
                if (command.equals("playMusic")) {
                    tool.playMusic(MusicModel.fromString(new String(data)));
                    return null;
                }
                if (command.equals("next")) {
                    tool.next();
                    return null;
                }
                if (command.equals("prev")) {
                    tool.prev();
                    return null;
                }
                if (command.equals("switchSong")) {
                    tool.switchSong();
                    return null;
                }
                if (command.equals("favourMusic")) {
                    tool.favourMusic();
                    return null;
                }
                if (command.equals("unfavourMusic")) {
                    tool.unfavourMusic();
                    return null;
                }
                if (command.equals("playFavourMusic")) {
                    tool.playFavourMusic();
                    return null;
                }
                if (command.equals("playRandom")) {
                    tool.playRandom();
                    return null;
                }
                if (command.equals("getCurrentMusicModel")) {
                    return tool.getCurrentMusicModel().toString().getBytes();
                }
                if (command.equals("switchModeLoopAll")) {
                    tool.switchModeLoopAll();
                    return null;
                }
                if (command.equals("switchModeLoopOne")) {
                    tool.switchModeLoopOne();
                    return null;
                }
                if (command.equals("switchModeRandom")) {
                    tool.switchModeRandom();
                    return null;
                }
                return null;
            }
        });
        if (tool instanceof MusicToolEx) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.settool",
                    (((MusicToolEx) tool).needTts() + "").getBytes(), null);
            return;
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.music.settool", null, null);
    }

    /**
     * 方法名：设置音乐工具
     * 方法描述：重写音乐逻辑或适配其它音乐逻辑时，可以使用此方法
     *
     * @param tool 音乐工具
     */
    public void setMusicTool(final AbsTXZMusicTool tool) {
        mHasSetTool = true;
        mMusicTool = tool;

        if (null == tool) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.cleartool", null,
                    null);
            return;
        }

        TXZService.setCommandProcessor(InvokeConstants.INVOKE_PREFIX_MUSIC, new CommandProcessor() {
            @Override
            public byte[] process(final String packageName, final String command,
                                  final byte[] data) {
                return tool.procSdkInvoke(packageName, command, data);
            }
        });

        JSONBuilder builder = new JSONBuilder();
        builder.put(InvokeConstants.PARAM_SDK_VERSION, tool.getSDKVersion());
        builder.put(InvokeConstants.PARAM_INTERCEPT_TTS, tool.interceptTts());
        builder.put(InvokeConstants.PARAM_SHOW_SEARCH_RESULT, tool.showSearchResult());
        builder.put(InvokeConstants.PARAM_SEARCH_MEDIA_TIMEOUT, tool.getSearchTimeoout());
        // 设置远程音乐工具
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.settool",
                builder.toBytes(), null);
    }

    AudioTool mAudioTool;
    boolean mHasSetAudioTool;

    /**
     * 枚举类：电台工具类型
     * 枚举类描述：默认支持电台类型
     */
    public static enum AudioTool {
        /**
         * 同行者
         */
        AUDIO_TXZ,
        /**
         * 考拉FM
         */
        AUDIO_KL,
        /**
         * 听听FM
         */
        AUDIO_TT,
        /**
         * 喜马拉雅FM
         */
        AUDIO_XMLY
    }

    /**
     * 方法名：设置当前默认电台类型
     * 方法描述：手动设置当前默认电台类型，如多电台APP时，手动切换语音控制的APP
     *
     * @param at 电台工具
     */
    @Deprecated
    public void setDefaultAudioTool(AudioTool at) {
        mHasSetAudioTool = true;
        mAudioTool = at;
        if (at == null) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.audio.cleartool",
                    null, null);
            return;
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.audio.setInnerTool",
                mAudioTool.name().getBytes(), null);
    }

    private String mTTTaskId;

    /**
     * 方法名：设置外部程序自定义命令字控制同听的任务ID
     * 方法描述：用于将适配注册的指令同步至同行者金手指功能
     *
     * @param taskId 外部程序注册的免唤醒词ID
     */
    public void setTTMusicControlTaskId(String taskId) {
        mTTTaskId = taskId;
        if (mTTTaskId == null) {
            return;
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.music.setTTMusicControlTaskId", mTTTaskId.getBytes(), null);
    }

    /**
     * 方法名：播放状态获取接口
     * 方法描述：获取当前音乐的播放状态
     *
     * @return 是否正在播放
     */
    public boolean isPlaying() {
        byte[] data = ServiceManager.getInstance().sendTXZInvokeSync("txz.music.isPlaying", null);
        if (data == null)
            return false;
        return Boolean.parseBoolean(new String(data));
    }

    /**
     * 方法名：获取是否缓冲的状态
     * 方法描述：仅限电台之家和同听APP
     *
     * @return 是否正在播放
     */
    public boolean isBuffering() {
        byte[] data = ServiceManager.getInstance().sendTXZInvokeSync("txz.music.isBuffering", null);
        if (data == null)
            return false;
        return Boolean.parseBoolean(new String(data));
    }

    /**
     * 方法名：开始播放
     * 方法描述：控制当前音乐类型，包括同行者支持的音乐APP
     */
    public void play() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.play", null, null);
    }

    /**
     * 方法名：继续播放
     * 方法描述：控制当前音乐类型，包括同行者支持的音乐APP
     */
    public void continuePlay() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.cont", null, null);
    }

    /**
     * 方法名：暂停播放
     * 方法描述：控制当前音乐类型，包括同行者支持的音乐APP
     */
    public void pause() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.pause", null, null);
    }

    /**
     * 方法名：关闭音乐
     * 方法描述：控制当前音乐类型，包括同行者支持的音乐APP
     */
    public void exit() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.exit", null, null);
    }

    /**
     * 方法名：退出所有音乐工具
     * 方法描述：关闭当前安装的全部或已适配的音乐类型APP
     */
    public void exitAllMusicToolImmediately() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.music.exitAllMusicToolImmediately", null, null);
    }

    /**
     * 方法名：下一首
     * 方法描述：控制当前音乐类型，包括同行者支持的音乐APP
     */
    public void next() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.next", null, null);
    }

    /**
     * 方法名：上一首
     * 方法描述：控制当前音乐类型，包括同行者支持的音乐APP
     */
    public void prev() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.prev", null, null);
    }

    /**
     * 方法名：全部循环
     * 方法描述：控制当前音乐类型，包括同行者支持的音乐APP
     */
    public void switchModeLoopAll() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.switchModeLoopAll",
                null, null);
    }

    /**
     * 方法名：单曲循环
     * 方法描述：控制当前音乐类型，包括同行者支持的音乐APP
     */
    public void switchModeLoopOne() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music" +
                ".switchModeLoopOne", null, null);
    }

    /**
     * 方法名：随机播放
     * 方法描述：控制当前音乐类型，包括同行者支持的音乐APP
     */
    public void switchModeRandom() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.switchModeRandom",
                null, null);
    }

    /**
     * 方法名：切换歌曲
     * 方法描述：控制当前音乐类型，包括同行者支持的音乐APP
     */
    public void switchSong() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.switchSong", null,
                null);
    }

    /**
     * 方法名：随便听听/随便来首歌
     * 方法描述：控制当前音乐类型，包括同行者支持的音乐APP
     */
    public void playRandom() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.playRandom", null,
                null);
    }

    /**
     * 方法名：获取当前正在播放的音乐模型，没有播放返回null
     * 方法描述：获取当前音乐类型，包括同行者支持的音乐APP
     */
    public MusicModel getCurrentMusicModel() {
        try {
            byte[] data = ServiceManager.getInstance()
                    .sendTXZInvokeSync("txz.music.getCurrentMusicModel", null);
            if (data == null)
                return null;
            return MusicModel.fromString(new String(data));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 方法名：播放收藏歌曲
     * 方法描述：仅支持电台之家和同听，播放收藏歌曲，
     */
    public void playFavourMusic() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.playFavourMusic",
                null, null);
    }

    /**
     * 方法名：收藏当前歌曲
     * 方法描述：仅支持电台之家和同听，收藏当前歌曲，
     */
    public void favourMusic() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.favourMusic",
                null, null);
    }

    /**
     * 方法名：取消收藏当前歌曲
     * 方法描述：仅支持电台之家和同听，取消收藏当前歌曲
     */
    public void unfavourMusic() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.unfavourMusic",
                null, null);
    }

    /**
     * 方法名：获取同行者播放器接口
     * 方法描述：获取同行者播放器接口实例，直接控制对应APP，支持同行者已支持相关类型
     *
     * @return 播放器接口实例
     */
    TXZMusicTool getTXZMusicTool() {
        return TXZMusicTool.getInstance();
    }

    Boolean mShowKuwoSearchResult = null;

    /**
     * 方法名：是否展示酷我搜索结果
     * 方法描述：默认不显示酷我搜索结果，直接播放
     *
     * @param show 是否需要列表显示
     * @deprecated 转用 {@link TXZMusicManager#setSearchConfig}, 提供针对特定工具的更详细配置
     */
    @Deprecated
    public void showKuwoSearchResult(boolean show) {
        mShowKuwoSearchResult = show;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.music.showKuwoSearchResult", ("" + show).getBytes(), null);
    }

    /**
     * 方法名：设置音乐工具搜索参数
     * 方法描述：设置具体搜索音乐类型对应参数值，根据音乐类型设置
     *
     * @param type       工具类型
     * @param showResult 是否在声控界面显示搜索列表
     * @param timeout    搜索超时时间
     */
    public void setSearchConfig(MusicToolType type, boolean showResult, int timeout) {
        MediaToolSearchConfig config = new MediaToolSearchConfig(type.name(), showResult, timeout);
        setSearchConfig(config);
        // 保存配置, 用于重连时恢复
        mSearchConfigMap.put(type, config);
    }

    private void restoreSearchConfig() {
        if (null == mSearchConfigMap) {
            return;
        }

        if (mSearchConfigMap.isEmpty()) {
            return;
        }

        for (MediaToolSearchConfig config : mSearchConfigMap.values()) {
            setSearchConfig(config);
        }
    }

    private void setSearchConfig(MediaToolSearchConfig config) {
        JSONBuilder builder = new JSONBuilder();
        builder.put(InvokeConstants.PARAM_SEARCH_TOOL_TYPE, config.toolName);
        builder.put(InvokeConstants.PARAM_SEARCH_SHOW_RESULT, config.showResult);
        builder.put(InvokeConstants.PARAM_SEARCH_TIMEOUT, config.timeout);

        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.music." + InvokeConstants.INVOKE_SEARCH_CONFIG, builder.toBytes(), null);
    }

    Collection<MusicModel> mLastSyncMusics = null;

    /**
     * 方法名：同步音乐列表
     * 方法描述：将音乐列表同步给语音，之后将不会再从系统数据库读取音乐，支持电台之家和同听
     *
     * @param musics 音乐集合
     */
    public void syncMuicList(Collection<MusicModel> musics) {
        mLastSyncMusics = musics;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncMuicList",
                MusicModel.collecionToString(mLastSyncMusics).getBytes(), null);
    }

    Collection<MusicModel> mLastSyncExMusics = null;

    /**
     * 方法名：同步额外音乐列表
     * 方法描述：将音乐列表同步给语音，与系统数据库共存
     *
     * @param musics 音乐集合
     */
    public void syncExMuicList(Collection<MusicModel> musics) {
        mLastSyncExMusics = musics;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncExMuicList",
                MusicModel.collecionToString(mLastSyncExMusics).getBytes(), null);
    }

    /**
     * 方法名：同步额外音乐列表
     * 方法描述：将音乐列表同步给语音，与系统数据库共存，但会在语音断开连接时清空数据
     *
     * @param musics 音乐集合
     */
    public void syncExMuicListToCore(Collection<MusicModel> musics) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.syncExMuicList",
                MusicModel.collecionToString(musics).getBytes(), null);
    }
}
