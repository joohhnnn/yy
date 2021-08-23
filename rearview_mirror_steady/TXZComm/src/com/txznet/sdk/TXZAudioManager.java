package com.txznet.sdk;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZService.CommandProcessor;
import com.txznet.sdk.media.AbsTXZAudioTool;
import com.txznet.sdk.media.MediaToolSearchConfig;
import com.txznet.sdk.media.TXZMediaModel;
import com.txznet.sdk.media.constant.InvokeConstants;
import com.txznet.sdk.media.constant.PlayerStatus;

import java.util.HashMap;

/**
 * 类名：语音电台语义管理类
 * 类描述：同行者电台相关语义工具管理类，默认支持同听、喜马拉雅等APP，同时支持对接其它电台APP。
 *         主要包含电台工具设置、切换及电台相关语音等配置的管理。
 */
public class TXZAudioManager {
    private static TXZAudioManager sInstance = new TXZAudioManager();

    // 电台工具搜索参数设置缓存
    private HashMap<AudioTool, MediaToolSearchConfig> mSearchConfigMap =
            new HashMap<AudioTool, MediaToolSearchConfig>();

    private TXZAudioManager() {

    }

    /**
     * 获取单例
     *
     * @return 类实例
     */
    public static TXZAudioManager getInstance() {
        return sInstance;
    }

    /**
     * 重连时需要重新通知同行者的操作放这里
     */
    void onReconnectTXZ() {
        if (mHasSetAudioTool) {
            setDefaultAudioTool(mAudioTool);
        }

        if (mHasSetAT) {
            setAudioTool(mAT);
        }

        if (!TextUtils.isEmpty(mXmlyAppSecret)) {
            setXMLYAppkey(mXmlyAppSecret, mXmlyAppKey, mXmlyPkgName);
        }

        if (bShowXmlySearchResult != null) {
            showXmlySearchResult(bShowXmlySearchResult);
        }

        restoreSearchConfig();
    }

    /**
     * 接口名：电台工具状态变化监听器
     * 接口描述：电台工具状态变化监听器，自定义电台工具时，需要通过此监听器通知语音第三方APP状态
     */
    public static interface AudioToolStatusListener {
        /**
         * 方法名：电台媒体播放状态改变回调
         * 方法描述：电台媒体状态发生改变，如开始播放、暂停播放(非tts等引发的临时暂停)、缓冲中、曲目变化
         */
        public void onStatusChange();
    }

    /**
     * 接口名：自定义电台工具
     * 接口描述：自定义电台工具，用于对接第三方电台APP
     *
     * @deprecated 已弃用 {@link AbsTXZAudioTool}
     */
    public static abstract class IAudioTool {

        /**
         * 方法名：设置状态监听器
         * 方法描述：设置外部电台状态监听器，保存此监听器，状态变化时，通过此实例通知语音
         *
         * @param atsl 状态监听器
         */
        public abstract void setAudioStatusListener(AudioToolStatusListener atsl);

        /**
         * 方法名：开始播放
         * 方法描述：电台控制接口逻辑实现，识别到电台“开始播放”等语义时，回调此方法
         */
        public abstract void start();

        /**
         * 方法名：暂停播放
         * 方法描述：电台控制接口逻辑实现，识别到电台“暂停播放”等语义时，回调此方法
         */
        public abstract void pause();

        /**
         * 方法名：通过关键字播放
         * 方法描述：电台控制接口逻辑实现，通过关键关键字识别，搜索电台并播放，如：我要听“郭德纲的相声”
         *
         * @param keyWord 关键字
         */
        public abstract void playFm(String keyWord);

        /**
         * 方法名：退出电台
         * 方法描述：电台控制接口逻辑实现，识别到电台“退出电台”等语义时，回调此方法
         */
        public abstract void exit();

        /**
         * 方法名：下一个电台
         * 方法描述：电台控制接口逻辑实现，识别到电台“下一个电台”等语义时，回调此方法
         */
        public void next() {
        }

        /**
         * 方法名：上一个电台
         * 方法描述：电台控制接口逻辑实现，识别到电台“上一个电台”等语义时，回调此方法
         */
        public void prev() {
        }

        /**
         * 方法名：获取当前播放的名称
         * 方法描述：电台控制接口逻辑实现，用户询问当前电台语义时，回调此方法
         *
         * @return 当前播放的电台名称
         */
        public String getCurrentFmName() {
            return "";
        }

        /**
         * 方法名：当前电台是否在播放
         * 方法描述：电台控制接口逻辑实现，语音需要获取当前电台播报状态时，回调此方法
         *
         * @return 当前电台是否在播放中
         */
        public boolean isPlaying() {
            return false;
        }
    }

    private AudioTool mAudioTool;
    private boolean mHasSetAudioTool;

    /**
     * 枚举类名：默认电台工具
     * 枚举类描述：有同行者。考拉fm，听听fm，喜马拉雅fm，远程工具
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
        AUDIO_XMLY,
        /**
         * 远程工具
         */
        AUDIO_TOOL_REMOTE,
    }

    /**
     * 方法名：设置默认的电台工具
     * 方法描述：当前无同行者已适配电台APP时，可以通过设置额外电台工具进行控制适配
     *
     * @param at 需要设置的音乐工具类型
     * @deprecated 已弃用，{@link AbsTXZAudioTool}
     */
    public void setDefaultAudioTool(AudioTool at) {
        mHasSetAudioTool = true;
        mAudioTool = at;
        if (at == null) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    "txz.audio.cleartool", null, null);
            return;
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.audio.setInnerTool", mAudioTool.name().getBytes(), null);
    }

    private String mXmlyAppSecret;
    private String mXmlyAppKey;
    private String mXmlyPkgName;

    /**
     * 方法名：设置喜马拉雅电台SDK激活参数
     * 方法描述：喜马拉雅对SDK调用有要求，需要针对不同项目配置SDK APP_Key、APP_Secret及授权方包名
     *
     * @param appSecret APP_Secret由喜马拉雅提供
     * @param appKey    APP_Key由喜马拉雅提供
     * @param pkgName   授权方包名由喜马拉雅提供
     */
    public void setXMLYAppkey(String appSecret, String appKey, String pkgName) {
        mXmlyAppSecret = appSecret;
        mXmlyAppKey = appKey;
        mXmlyPkgName = pkgName;

        JSONBuilder builder = new JSONBuilder();
        builder.put("appSecret", appSecret);
        builder.put("appKey", appKey);
        builder.put("pkgName", pkgName);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.audio.setkey.xmly", builder.toBytes(), null);
    }

    AbsTXZAudioTool mAT;
    boolean mHasSetAT;

    /**
     * 方法名：设置电台适配工具
     * 方法描述：当前无同行者已适配电台APP时，可以通过设置额外电台工具进行控制适配
     *
     * @param tool 同行者远程电台工具
     */
    public void setAudioTool(final AbsTXZAudioTool tool) {
        mHasSetAT = true;
        mAT = tool;

        if (tool == null) {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                    "txz.audio.cleartool", null, null);
            return;
        }

        TXZService.setCommandProcessor(InvokeConstants.INVOKE_PREFIX_AUDIO, new CommandProcessor() {
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
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.audio.setTool",
                builder.toBytes(), null);
    }

    /**
     * 方法名：播放状态获取接口
     * 方法描述：音频是否正在播放中
     * @return 是否正在播放
     */
    public boolean isPlaying() {
        byte[] data = ServiceManager.getInstance().sendTXZInvokeSync(
                "txz.audio.isPlaying", null);
        if (data == null)
            return false;
        return Boolean.parseBoolean(new String(data));
    }

    /**
     * 方法名：开始播放
     * 方法描述：开始播放当前的电台媒体，自适应调用当前电台类型API
     */
    public void play() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.audio.play", null, null);
    }

    /**
     * 方法名：暂停播放
     * 方法描述：暂停当前的电台媒体，自适应调用当前电台类型API
     */
    public void pause() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.audio.pause", null, null);
    }

    /**
     * 方法名：指定关键字播放电台
     * 方法描述：根据关键字搜索并播放电台，如：郭德纲的相声，自适应调用当前电台类型API
     *
     * @param keywords 关键字文本
     */
    public void playKeywords(String keywords) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.audio.playFm", keywords.getBytes(), null);
    }

    /**
     * 方法名：退出播放
     * 方法描述：退出播放当前的电台媒体，自适应调用当前电台类型API
     */
    public void exit() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.audio.exit", null, null);
    }

    /**
     * 方法名：下一个电台
     * 方法描述：播放下一个电台，自适应调用当前电台类型API
     */
    public void next() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.audio.next", null, null);
    }

    /**
     * 方法名：上一个电台
     * 方法描述：上一个电台，自适应调用当前电台类型API
     */
    public void prev() {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.audio.prev", null, null);
    }

    private Boolean bShowXmlySearchResult;

    /**
     * 方法名：设置显示喜马拉雅搜索列表
     * 方法描述：设置是否显示通过喜马拉雅搜索时，是否在语音界面展示搜索结果列表，默认不展示
     *
     * @param show 是否显示列表
     * @deprecated 转用 {@link TXZAudioManager#setSearchConfig}, 提供针对特定工具的更详细配置
     */
    @Deprecated
    public void showXmlySearchResult(boolean show) {
        bShowXmlySearchResult = show;
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                "txz.audio.showSelect.xmly",
                bShowXmlySearchResult.toString().getBytes(), null);
    }

    /**
     * 方法名：设置电台工具搜索参数
     * 方法描述：设置电台数据搜索工具、是否需要展示语音结果展示页和搜索超时时间
     *
     * @param type       AudioTool工具类型
     * @param showResult 是否在声控界面显示搜索列表
     * @param timeout    搜索超时时间
     */
    public void setSearchConfig(AudioTool type, boolean showResult, int timeout) {
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
                "txz.audio." + InvokeConstants.INVOKE_SEARCH_CONFIG, builder.toBytes(), null);
    }
}