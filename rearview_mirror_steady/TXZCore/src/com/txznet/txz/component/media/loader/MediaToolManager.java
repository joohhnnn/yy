package com.txznet.txz.component.media.loader;

import android.os.Environment;
import android.text.TextUtils;

import com.txz.ui.data.UiData;
import com.txznet.comm.util.FilePathConstants;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.audio.txz.AudioTongTing;
import com.txznet.txz.component.media.MediaPriorityManager;
import com.txznet.txz.component.media.base.AbsAudioTool;
import com.txznet.txz.component.media.base.AbsMusicTool;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.media.chooser.AudioPriorityChooser;
import com.txznet.txz.component.media.chooser.MusicPriorityChooser;
import com.txznet.txz.component.media.remote.RemoteAudioTool;
import com.txznet.txz.component.media.remote.RemoteMusicTool;
import com.txznet.txz.component.media.util.FileReader;
import com.txznet.txz.component.media.util.MediaToolChoose;
import com.txznet.txz.component.music.txz.MusicTongTing;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.media.plugin.IPluginMediaTool;
import com.txznet.txz.module.media.plugin.PluginMediaToolDataInterface;

import org.json.JSONException;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MediaTool实例管理
 *
 * 负责内/外置媒体工具的装载和维护
 *
 * 媒体工具类型:
 * 1. 内置工具: 同听/远程音乐/远程电台, 直接随Core打包, 不需要动态装载
 * 2. 外置工具: dex包+chk文件, 存放于外置路径, Core初始化时进行校验和装载
 * 3. 预置工具: dex包+chk文件, 存放于assets目录下与Core一起打包, 装载时读入chk文件进行校验, 将dex包拷贝到
 *    应用私有路径下进行装载
 * * 外置和预置工具的dex包和chk结构相同, 采用同样流程进行打包
 *
 * 媒体工具初始化顺序:
 * 1. 初始化内置工具(同听/远程音乐/远程电台)
 * 2. 装载外置路径下的适配程序dex包
 * 3. 装载assets/presetMediaTools/路径下的预置适配包
 *
 * 媒体工具装载流程:
 * 1. 遍历装载路径, 过滤dex包
 * 2. 针对每个dex包, 寻找同名.chk文件, 解析文件内容初始化加载信息
 * 3. 数据校验, 进行媒体工具类装载和初始化
 *
 * 装载路径:
 * 外置工具测试路径(最高优先级):
 * /sdcard/txz/media_tool/
 * 其他外置路径:
 * @see FilePathConstants#getMediaToolPath()
 *
 * 预置工具路径:
 * /assets/presetMediaTools/
 *
 * Created by J on 2019/3/5.
 */
public class MediaToolManager {
    private static final String LOG_TAG = "MediaToolManager::";
    private static final String PATH_MEDIA_TOOL_PRIOR = Environment.getExternalStorageDirectory()
            + "/txz/media_tool";

    // 外部工具对应的类型
    private static final String MEDIA_TOOL_TYPE_MUSIC = "MUSIC";
    private static final String MEDIA_TOOL_TYPE_AUDIO = "AUDIO";

    private HashMap<String, IMediaTool> mMediaToolList = new HashMap<String, IMediaTool>();
    private HashMap<String, MediaToolLoadInfo> mLoadInfoMap = new HashMap<String,
            MediaToolLoadInfo>();

    private ArrayList<AbsMusicTool> mMusicToolList;
    private ArrayList<AbsAudioTool> mAudioToolList;

    private HashMap<String, Long> mSearchTimeoutMap = new HashMap<String, Long>();
    private AtomicBoolean mAbMediaLoadFinished = new AtomicBoolean(false);

    /**
     * 初始化媒体工具列表
     * 优先装载外部路径下的媒体工具
     */
    public void initMediaToolList() {
        // 初始化预置工具(远程工具和同听优先加载)
        initMusicToolList();
        initAudioToolList();

        // 加载媒体工具
        mLoadInfoMap.clear();
        loadOuterMediaTool();
        loadInnerMediaTool();

        // 加载完毕通知MediaChooser进行高优先工具恢复
        MusicPriorityChooser.getInstance().restorePriority();
        AudioPriorityChooser.getInstance().restorePriority();

        // 应用因工具加载被缓存的搜索配置
        initSearchConfig();
    }

    public byte[] sendInvoke(String pkg, String cmd, byte[] data) {
        IMediaTool tool = mMediaToolList.get(pkg);
        if (null == tool) {
            return null;
        }

        if (!(tool instanceof ILoadedMediaTool)) {
            return null;
        }

        return ((ILoadedMediaTool) tool).sendInvoke(cmd, data);
    }

    public List<AbsMusicTool> getMusicToolList() {
        return mMusicToolList;
    }

    public List<AbsAudioTool> getAudioToolList() {
        return mAudioToolList;
    }

    public synchronized void setSearchConfig(String packageName, boolean show, long timeout) {
        if (mAbMediaLoadFinished.get()) {
            IMediaTool tool = mMediaToolList.get(packageName);
            if (null == tool) {
                log(String.format("can not set search config for: %s, tool not exist.",
                        packageName));
                return;
            }
            tool.setShowSearchResult(show);
            tool.setSearchTimeout(timeout);
        } else {
            long parsedTimeout = show ? timeout : -1;
            log(String.format("cached search config for: %s, timeout = %s", packageName,
                    parsedTimeout));
            mSearchTimeoutMap.put(packageName, parsedTimeout);
        }
    }

    private void initMusicToolList() {
        mMusicToolList = new ArrayList<AbsMusicTool>();
        // 添加内置工具
        // 1.远程工具
        mMusicToolList.add(RemoteMusicTool.getInstance());
        // 2.同听
        mMusicToolList.add(MusicTongTing.getInstance());
    }

    private void initAudioToolList() {
        mAudioToolList = new ArrayList<AbsAudioTool>();
        // 添加内置工具
        // 1. 远程电台工具
        mAudioToolList.add(RemoteAudioTool.getInstance());
        // 2. 同听
        mAudioToolList.add(AudioTongTing.getInstance());
    }

    /**
     * 初始化搜索相关配置
     */
    private synchronized void initSearchConfig() {
        for (String key : mSearchTimeoutMap.keySet()) {
            log("applying cached search config for: " + key);
            IMediaTool tool = mMediaToolList.get(key);
            if (tool != null) {
                long timeout = mSearchTimeoutMap.get(key);
                tool.setShowSearchResult(timeout != -1);
                tool.setSearchTimeout(timeout);
                log("applied cached search config for: " + key);
            }
        }

        // 恢复完毕后清空缓存
        mSearchTimeoutMap.clear();
        mSearchTimeoutMap = null;
        // 加载完毕后更新标志位
        mAbMediaLoadFinished.getAndSet(true);
    }

    /**
     * 装载外部路径下的媒体工具
     */
    private void loadOuterMediaTool() {
        loadOuterToolByPath(PATH_MEDIA_TOOL_PRIOR);

        // 装载用户路径下的适配插件
        for (String path : FilePathConstants.getMediaToolPath()) {
            loadOuterToolByPath(path);
        }
    }

    /**
     * 装载指定外置路径下的媒体适配包
     * @param path 指定路径
     */
    private void loadOuterToolByPath(String path) {
        File outerToolDir = new File(path);

        if (!outerToolDir.exists() || !outerToolDir.isDirectory()) {
            return;
        }

        File[] outerJarList = outerToolDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(final File dir, final String name) {
                return name.endsWith(".jar");
            }
        });

        if (null == outerJarList) {
            return;
        }

        for (File file : outerJarList) {
            log("loading outer media tool on path: " + file.getPath());
            String chkPath = file.getAbsolutePath()
                    .substring(0, file.getAbsolutePath().length() - ".jar".length()) + ".chk";
            File chkFile = new File(chkPath);

            if (!chkFile.exists()) {
                log("error during load, chk file is missing.");
                continue;
            }

            try {
                MediaToolLoadInfo info = new MediaToolLoadInfo(file, chkFile);
                if (mLoadInfoMap.containsKey(info.targetPackageName)) {
                    log(String.format("duplicate tool found! target = %s, path = %s",
                            info.targetPackageName, file.getPath()));
                    continue;
                }

                IPluginMediaTool tool = MediaToolLoader.loadMediaToolFile(file, info,
                        mDataInterface);

                if (null == tool) {
                    log("error during load, loaded tool is null.");
                    continue;
                }

                procMediaToolLoadSuccess(info, tool, true);
            } catch (Exception e) {
                log(String.format("error during loading outer media tool, path: %s, error: %s",
                        file.getPath(), e.toString()));
                e.printStackTrace();
            }
        }
    }

    //媒体插件信息集合
    Map<String, List<MediaToolLoadInfo>> mediaToolMap = new HashMap<String, List<MediaToolLoadInfo>>();


    /**
     * 加载内置媒体插件工具集合
     */
    private void loadInnerMediaToolList() {
        log("entry loadInnerMediaToolList");
        List<MediaToolLoadInfo> mediaToolList;
        String[] presetFiles = new String[0];
        try {
            presetFiles = AppLogic.getApp().getAssets().list("presetMediaTools");
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (String fileName : presetFiles) {
            if (fileName.endsWith(".chk")) {
                String packageName = fileName.substring(0, fileName.length() - 4);
                String chkContent = FileReader.readFileFromAssets("presetMediaTools"
                        + File.separator + fileName);
                if (TextUtils.isEmpty(chkContent)) {
                    log("chk file read error for: " + fileName);
                    continue;
                }
                String targetJarName = packageName + ".jar";
                MediaToolLoadInfo info = null;
                try {
                    info = new MediaToolLoadInfo(chkContent, targetJarName);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                String packageNameKey = info.targetPackageName;
                mediaToolList = mediaToolMap.get(packageNameKey);
                if (mediaToolList == null) {
                    mediaToolList = new ArrayList<MediaToolLoadInfo>();
                }
                mediaToolList.add(info);
                mediaToolMap.put(packageNameKey, mediaToolList);
            }
        }
    }


    /**
     * 装载内置媒体工具
     */
    private void loadInnerMediaTool() {
        log("start loading inner media tools.");
        loadInnerMediaToolList();
        for (String packageName : mediaToolMap.keySet()) {
            UiData.AppInfo appInfo = PackageManager.getInstance().getAppInfo(packageName);
            if (appInfo == null) {
                log("android does not have this media installed");
                continue;
            }
            if (mMediaToolList.containsKey(packageName)) {
                log(String.format("media tool for %s already loaded from outer path.", packageName));
                continue;
            }
            List<MediaToolLoadInfo> mediaList = mediaToolMap.get(packageName);
            String version = appInfo.strVersion;
            MediaToolLoadInfo info = MediaToolChoose.mediaToolChoose(mediaList, version);
            if (info == null) {
                log("No corresponding version found");
                continue;
            }
            try {
                log("正在装载"+packageName+"适配版本"+info.targetVersionName);
                IPluginMediaTool tool = MediaToolLoader.loadMediaToolFromAssets("presetMediaTools"
                        + File.separator + info.targetJarName, info, mDataInterface);
                if (null == tool) {
                    log("error during load, loaded tool is null.");
                    continue;
                }
                procMediaToolLoadSuccess(info, tool, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void procMediaToolLoadSuccess(MediaToolLoadInfo info, IPluginMediaTool tool,
                                          boolean fromPlugin) {
        log(String.format("[%s] media tool load success: target = %s, type = %s, priority = %s",
                fromPlugin ? "plugin" : "preset", info.targetPackageName, info.type, info.priority));

        // 记录
        mLoadInfoMap.put(info.targetPackageName, info);

        if (MEDIA_TOOL_TYPE_MUSIC.equals(info.type)) {
            procMusicToolLoad(new LoadedMusicTool(tool));
        } else if (MEDIA_TOOL_TYPE_AUDIO.equals(info.type)) {
            procAudioToolLoad(new LoadedAudioTool(tool));
        }
    }

    private void procMusicToolLoad(LoadedMusicTool tool) {
        mMediaToolList.put(tool.getPackageName(), tool);

        addMediaTool(mMusicToolList, tool);
    }

    private void procAudioToolLoad(LoadedAudioTool tool) {
        mMediaToolList.put(tool.getPackageName(), tool);

        addMediaTool(mAudioToolList, tool);
    }

    private <T extends IMediaTool> void addMediaTool(List<T> list, T tool) {
        list.add(tool);

        // 添加完毕后恢复列表优先级顺序
        Collections.sort(list, new Comparator<T>() {
            @Override
            public int compare(final T o1, final T o2) {
                if (o1.getPriority() == o2.getPriority()) {
                    return 0;
                }

                return o1.getPriority() > o2.getPriority() ? -1 : 1;
            }
        });

        mMediaToolList.put(tool.getPackageName(), tool);
    }

    private PluginMediaToolDataInterface mDataInterface = new PluginMediaToolDataInterface() {
        @Override
        public byte[] onMediaToolInvoke(final String token, final String cmd, final byte[] data) {
            if ("status.change".equals(cmd)) {
                if (IMediaTool.PLAYER_STATUS.PLAYING
                        == IMediaTool.PLAYER_STATUS.valueOf(new String(data))) {
                    MediaPriorityManager.getInstance()
                            .notifyPriorityChange(mMediaToolList.get(token));
                }
            } else if ("log".equals(cmd)) {
                JNIHelper.logd(new String(data));
            }
            return null;
        }
    };

    //----------- single instance -----------
    private static volatile MediaToolManager sInstance;

    public static MediaToolManager getInstance() {
        if (null == sInstance) {
            synchronized (MediaToolManager.class) {
                if (null == sInstance) {
                    sInstance = new MediaToolManager();
                }
            }
        }

        return sInstance;
    }

    private MediaToolManager() {

    }
    //----------- single instance -----------

    // logger
    private void log(String msg) {
        JNIHelper.logd(LOG_TAG + msg);
    }
}
