package com.txznet.sdk.media.constant;

/**
 * 远程媒体工具相关的ipc常量
 *
 * Created by J on 2018/5/8.
 */

public interface InvokeConstants {
    // 音乐和电台的命令字前缀
    String CMD_PREFIX_MUSIC = "txz.music.sdk.";
    String CMD_PREFIX_AUDIO = "txz.audio.sdk.";

    // 常规控制
    String INVOKE_PREFIX_MUSIC = "tool.music.invoke.";
    String INVOKE_PREFIX_AUDIO = "tool.audio.invoke.";
    String INVOKE_PLAY = "play";
    String INVOKE_OPEN = "open";
    String INVOKE_CONTINUE_PLAY = "continue_play";
    String INVOKE_PAUSE = "pause";
    String INVOKE_EXIT = "exit";
    String INVOKE_NEXT = "next";
    String INVOKE_PREV = "prev";
    String INVOKE_SWITCH_LOOP_MODE = "switch_loop_mode";
    String INVOKE_COLLECT = "collect";
    String INVOKE_UNCOLLECT = "uncollect";
    String INVOKE_PLAY_COLLECTION = "play_collection";
    String INVOKE_SUBSCRIBE = "subscribe";
    String INVOKE_UNSUBSCRIBE = "unsubscribe";
    String INVOKE_PLAY_SUBSCRIBE = "play_subscribe";
    String INVOKE_GET_PLAYER_STATUS = "get_status";
    String INVOKE_SUPPORT_LOOP_MODE = "support_loop_mode";
    String INVOKE_SUPPORT_SUBSCRIBE = "support_subscribe";
    String INVOKE_SUPPORT_UNSUBSCRIBE = "support_unsubscribe";
    String INVOKE_SUPPORT_COLLECT = "support_collect";
    String INVOKE_SUPPORT_UNCOLLECT = "support_uncollect";
    String INVOKE_SUPPORT_PLAY_SUBSCRIBE = "support_play_subscribe";
    String INVOKE_SUPPORT_PLAY_COLLECTION = "support_play_collection";
    String INVOKE_SUPPORT_SEARCH = "support_search";
    String INVOKE_HAS_NEXT = "has_next";
    String INVOKE_HAS_PREV = "has_prev";
    String INVOKE_SEARCH_CONFIG = "search_config";

    // 搜索相关
    String INVOKE_SEARCH_MEDIA = "search_media";
    String INVOKE_PLAY_SEARCH_RESULT = "play_search";

    // 主动状态通知
    String CMD_NOTIFY_PLAYER_STATUS = "status_notify";
    String CMD_NOTIFY_PLAYING_MODEL = "playing_model";
    String CMD_NOTIFY_SEARCH_SUCCESS = "search_success";
    String CMD_NOTIFY_SEARCH_ERROR = "search_error";

    // ipc data字段参数名
    String PARAM_SDK_VERSION = "version";
    String PARAM_INTERCEPT_TTS = "interceptTts";
    String PARAM_SHOW_SEARCH_RESULT = "show_search_result";
    String PARAM_SEARCH_MEDIA_TIMEOUT = "search_timeout";

    String PARAM_OPEN_PLAY = "play";
    String PARAM_PLAY_MODE = "mode";
    String PARAM_SEARCH_MODEL = "search_model";
    String PARAM_SEARCH_MEDIA_RESULT = "search_result";
    String PARAM_SEARCH_TASK_ID = "search_task_id";
    String PARAM_SEARCH_ERROR_CAUSE = "search_error_cause";
    String PARAM_PLAY_RESULT_MODEL = "play_model";
    String PARAM_PLAY_RESULT_INDEX = "play_index";

    String PARAM_SEARCH_SHOW_RESULT = "show_result";
    String PARAM_SEARCH_TIMEOUT = "timeout";
    String PARAM_SEARCH_TOOL_TYPE = "tool_type";
}
