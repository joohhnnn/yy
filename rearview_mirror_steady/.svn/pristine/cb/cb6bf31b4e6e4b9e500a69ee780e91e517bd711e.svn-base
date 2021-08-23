package com.txznet.txz.component.media;

import android.text.TextUtils;

import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.media.model.MediaModel;
import com.txznet.txz.component.media.remote.RemoteAudioTool;
import com.txznet.txz.component.media.remote.RemoteMusicTool;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.text.TextResultHandle;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 音乐/电台相关场景处理
 *
 * 负责音乐电台相关场景的场景外放/上报等
 * Created by J on 2018/5/17.
 */

public class MediaSceneProcessor {
    private static final String LOG_TAG = "MediaScene::";
    private static final String SCENE_MUSIC = "music";
    private static final String SCENE_AUDIO = "audio";

    /**
     * 处理场景外放
     * @param op   对应的操作
     * @param type 对应的优先级类型
     * @return 是否需要拦截默认处理, 外放场景
     */
    public boolean procMediaScene(IMediaTool.MEDIA_TOOL_OP op,
                                    MediaPriorityManager.PRIORITY_TYPE type) {
        // 音乐和电台场景都按mtj的音乐场景进行上报
        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
        boolean intercept = false;
        String scene = getSceneString(type);
        String action = getAction(op);

        if (!(TextUtils.isEmpty(scene) || TextUtils.isEmpty(action))) {
            intercept = SenceManager.getInstance().procSenceByRemote(getSceneString(type),
                    getAction(op));
        }

        JNIHelper.logd(LOG_TAG
                + String.format("procMediaScene {scene = %s, action = %s}, intercept = %s", scene,
                    action, intercept));

        return intercept;
    }

    public boolean procSearchMediaScene(MediaPriorityManager.PRIORITY_TYPE type, MediaModel model) {
        // 音乐和电台场景都按mtj的音乐场景进行上报
        MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
        boolean intercept = false;
        String scene = getSceneString(type);

        if (!TextUtils.isEmpty(scene)) {
            JSONObject root = new JSONObject();
            try {
                root.put("scene", scene);
                root.put("action", "play");
                root.put("text", TextResultHandle.getInstance().getParseText());
                // 添加MediaModel相关信息
                root.put("model", model.toJsonObject());
            } catch (JSONException e) {
                JNIHelper.loge(LOG_TAG + "procSearchMediaScene error: " + e.toString());
            }


            byte[] ret = SenceManager.getInstance().procSenceByRemote(getSceneString(type),
                    root.toString().getBytes());
            intercept = Boolean.parseBoolean(new String(ret));
        }

        JNIHelper.logd(LOG_TAG
                + String.format("procSearchMediaScene {scene = %s, action = play}, intercept = %s",
                scene, intercept));

        return intercept;
    }

    /**
     * 处理媒体操作上报
     * @param op   对应的操作
     * @param type 对应的优先级类型
     * @param tool 执行操作的媒体工具
     */
    public void reportMediaScene(IMediaTool.MEDIA_TOOL_OP op,
                                 MediaPriorityManager.PRIORITY_TYPE type,
                                 IMediaTool tool) {
        ReportUtil.doReport(new ReportUtil.Report.Builder().setType(getSceneString(type))
                .setAction(getAction(op)).setSessionId().putExtra("toolName", getToolName(tool))
                .buildCommReport());
    }

    private String getToolName(IMediaTool tool) {
        if (null == tool) {
            return "";
        }

        if (tool instanceof RemoteMusicTool || tool instanceof RemoteAudioTool) {
            return "remote" + tool.getPackageName();
        }

        return tool.getPackageName();
    }

    private String getSceneString(MediaPriorityManager.PRIORITY_TYPE type) {
        // 默认按音乐场景处理
        String scene = SCENE_MUSIC;

        if (null != type) {
            switch (type) {
                case NONE:
                case MUSIC:
                case MUSIC_ONLY:
                    scene = SCENE_MUSIC;
                    break;

                case AUDIO:
                case AUDIO_ONLY:
                    scene = SCENE_AUDIO;
                    break;

            }
        }

        return scene;
    }

    private String getAction(IMediaTool.MEDIA_TOOL_OP op) {
        String action = "";
        switch (op) {
            case OPEN:
                action = "play";
                break;

            case EXIT:
                action = "exit";
                break;

            case PLAY:
                action = "search";
                break;

            case PAUSE:
                action = "pause";
                break;

            case CONTINUE_PLAY:
                action = "continue";
                break;

            case NEXT:
                action = "next";
                break;

            case PREV:
                action = "prev";
                break;

            case SWITCH_MODE_SEQUENTIAL:
                action = "switchModeLoopOnce";
                break;

            case SWITCH_MODE_SINGLE_LOOP:
                action = "switchModeLoopOne";
                break;

            case SWITCH_MODE_LIST_LOOP:
                action = "switchModeLoopAll";
                break;

            case SWITCH_MODE_SHUFFLE:
                action = "switchModeRandom";
                break;

            case COLLECT:
                action = "favourMusic";
                break;

            case UNCOLLECT:
                action = "unfavourMusic";
                break;

            case SUBSCRIBE:
                action = "addSubscribe";
                break;

            case UNSUBSCRIBE:
                action = "unSubscribe";
                break;

            case PLAY_COLLECTION:
                action = "playFavourMusic";
                break;

            case PLAY_SUBSCRIBE:
                action = "playSubscribe";
                break;

            case GET_PLAYING_MODEL:
                action = "ask";
                break;
        }

        return action;
    }

    //----------- single instance -----------
    private static volatile MediaSceneProcessor sInstance;

    public static MediaSceneProcessor getInstance() {
        if (null == sInstance) {
            synchronized (MediaSceneProcessor.class) {
                if (null == sInstance) {
                    sInstance = new MediaSceneProcessor();
                }
            }
        }

        return sInstance;
    }

    private MediaSceneProcessor() {

    }
    //----------- single instance -----------
}
