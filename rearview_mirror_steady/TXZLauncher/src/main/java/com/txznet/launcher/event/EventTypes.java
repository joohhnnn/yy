package com.txznet.launcher.event;

import java.util.ArrayList;

/**
 * Created by TXZ-METEORLUO on 2018/2/7.
 * 事件分发机制要实现的内容。这里是事件的类型，每一次加新的事件都应该在这里添加新类型。
 */

public class EventTypes {
    private static EventTypes sTypes;
    private static ArrayList<String> events = new ArrayList<>();

    // 初始化成功
    public static final String EVENT_TXZ_INIT_SUCCESS = "txz_init_success";
    public static final String EVENT_BOOT_COMPLETE = "boot_complete";
    public static final String EVENT_BOOT_OPERATION_COMPLETE = "boot_operation_complete"; // 开机的一系列欢迎、登录等操作执行完毕，处于空闲状态
    public static final String EVENT_BOOT_INVALID = "boot_invalid"; // 非法重启
    public static final String EVENT_BOOT_TODAY_NOTICE_COMPLETE="boot_today_notice_complete";//今日提示执行结束

    /*导航相关*/
    public static final String EVENT_NAV_FOREGROUND = "nav_foreground";// 前台通知
    public static final String EVENT_NAV_BACKGROUND = "nav_background";// 后台通知
    public static final String EVENT_NAV_START_NAVI = "nav_start_navi";// 开始导航
    public static final String EVENT_NAV_END_NAVI = "nav_end_navi";// 结束导航
    public static final String EVENT_NAV_EXIT_NAVI = "nav_exit_navi";//关闭导航
    public static final String EVENT_POI_ISSUED = "poi_issued";// poi地址下发
    public static final String EVENT_TRAFFIC = "traffic";

    // 时间通知
    public static final String EVENT_TIME_CHANGE = "time_change";
    // 微信登录
    public static final String EVENT_WX_LOGIN = "wx_login";
    // 微信注销
    public static final String EVENT_WX_LOGOUT = "wx_logout";
    // 微信扫码
    public static final String EVENT_WX_SCANNED = "wx_scanned";
    // 微信录音开始
    public static final String EVENT_WX_RECORD_BEGIN = "wx_record_begin";
    // 微信录音发送成功
    public static final String EVENT_WX_RECORD_SEND_SUCCESS = "wx_record_send_success";
    // 微信录音发送失败
    public static final String EVENT_WX_RECORD_SEND_FAILED = "wx_record_send_failed";
    // 微信消息播报(含等待回复)
    public static final String EVENT_WX_MSG_NOTIFY = "wx_msg_notify";
    // 微信消息播报完毕
    public static final String EVENT_WX_MSG_NOTIFY_DISMISS = "wx_msg_notify_dismiss";

    // 音乐播放
    public static final String EVENT_MUSIC_PLAYING = "music_playing";
    // 音乐打开
    public static final String EVENT_MUSIC_OPEN = "music_open";
    // 音乐暂停
    public static final String EVENT_MUSIC_PAUSE = "music_pause";
    // 音乐出错
    public static final String EVENT_MUSIC_FAIL = "music_fail";
    // 音乐退出
    public static final String EVENT_MUSIC_EXIT = "music_exit";
    // Launcher可见
    public static final String EVENT_LAUNCH_ONRESUME = "onResume";
    // Launcher不可见
    public static final String EVENT_LAUNCH_ONSTOP = "onStop";
    // Launcher后退
    public static final String EVENT_LAUNCH_ONBACKPRESSED = "onBackPressed";

    //界面相关
    //电台
    public static final String EVENT_RECORD_AUDIO_LIST = "record_audio_list";
    //电话
    public static final String EVENT_RECORD_CALL_LIST = "record_call_list";
    //股票
    public static final String EVENT_RECORD_CHAT_STOCK = "record_chat_shock";
    //天气
    public static final String EVENT_RECORD_CHAT_WEATHER = "record_chat_weather";
    //电影
    public static final String EVENT_RECORD_CINEMA_LIST = "record_cinema_list";
    //MapPoi
    public static final String EVENT_RECORD_MAP_POI_LIST = "record_map_poi_list";
    //nav app
    public static final String EVENT_RECORD_NAV_APP_LIST = "record_nav_app_list";
    //no tts Qrcode
    public static final String EVENT_RECORD_NO_TTS_QRCODE = "record_no_tts_qrcode";
    //poi
    public static final String EVENT_RECORD_POI_LIST = "record_poi_list";
    //qrcode
    public static final String EVENT_RECORD_QRCODE = "record_qrcode";
    //提醒
    public static final String EVENT_RECORD_REMINDER_LIST = "record_reminder_list";
    //流量充值
    public static final String EVENT_RECORD_SIM_LIST = "record_sim_list";
    //TTS
    public static final String EVENT_RECORD_TTS_LIST = "record_tts_list";
    //微信
    public static final String EVENT_RECORD_WECHAT_LIST = "record_wechat_list";

    // 唤醒词触发
    public static final String EVENT_WAKE_UP = "wakeup";
    // 声控界面起来
    public static final String EVENT_VOICE_OPEN = "record_open";
    // 声控界面取消
    public static final String EVENT_VOICE_DISMISS = "record_dismiss";

    // -- 设备相关
    public static final String EVENT_DEVICE_BLUE_BUTTON_PRESSED = "device_blue_button_pressed"; // 蓝键触发
    public static final String EVENT_DEVICE_RED_BUTTON_PRESSED = "device_red_button_pressed"; // 红键触发
    public static final String EVENT_DEVICE_RECORY_FACTORY = "device_recory_factory"; // 恢复出厂设置
    public static final String EVENT_DEVICE_SIM_READY = "device_sim_ready"; // sim卡可用(有网)
    public static final String EVENT_DEVICE_POWER_BEFORE_SLEEP = "device_power_before_sleep"; // 设备准备休眠
    public static final String EVENT_DEVICE_POWER_WAKEUP = "device_power_wakeup"; // 设备唤醒

    // -- 安吉星相关
    public static final String EVENT_ANJIXING_LOGIN = "anjixing_login";
    public static final String EVENT_ANJIXING_LOGOUT = "anjixing_logout";
    public static final String EVENT_ANJIXING_HAS_BIND = "anjixing_has_bind";

    // -- voip相关
    public static final String EVENT_VOIP_READY = "voip_ready"; // voip空闲状态,界面已经退出
    public static final String EVENT_VOIP_BEFORE_READY = "voip_before_ready"; // voip空闲状态，界面退出前
    public static final String EVENT_VOIP_CALLING = "voip_calling"; // voip呼叫中
    public static final String EVENT_VOIP_TALKING = "voip_talking"; // voip通话中

    // 开机引导相关
    public static final String EVENT_GUIDE_COMPLETE = "guide_complete";
    public static final String EVENT_GUIDE_INTERRUPT = "guide_interrupt";

    //app升级相关
    public static final String EVENT_UPGRADE_APP_SHOW = "upgrade_app_show";
    public static final String EVENT_UPGRADE_APP_DISMISS = "upgrade_app_dismiss";

    // fm控制相关
    public static final String EVENT_FM_START_NOTIFY_ADJUST = "fm_start_notify_adjust"; // 开始通知系统调整fm
    public static final String EVENT_FM_RECEIVE_STATE = "fm_receive_state"; // 收到系统通知的fm状态

    static {
        events.add(EVENT_NAV_FOREGROUND);
        events.add(EVENT_NAV_BACKGROUND);
        events.add(EVENT_NAV_START_NAVI);
        events.add(EVENT_NAV_END_NAVI);
        events.add(EVENT_NAV_EXIT_NAVI);
        events.add(EVENT_POI_ISSUED);
        events.add(EVENT_TIME_CHANGE);
        events.add(EVENT_WX_LOGIN);
        events.add(EVENT_WX_LOGOUT);
        events.add(EVENT_MUSIC_OPEN);
        events.add(EVENT_MUSIC_PLAYING);
        events.add(EVENT_MUSIC_PAUSE);
        events.add(EVENT_MUSIC_FAIL);
        events.add(EVENT_MUSIC_EXIT);
        events.add(EVENT_LAUNCH_ONRESUME);
        events.add(EVENT_LAUNCH_ONSTOP);
        events.add(EVENT_LAUNCH_ONBACKPRESSED);
        events.add(EVENT_RECORD_AUDIO_LIST);
        events.add(EVENT_RECORD_CALL_LIST);
        events.add(EVENT_RECORD_CHAT_STOCK);
        events.add(EVENT_RECORD_CHAT_WEATHER);
        events.add(EVENT_RECORD_CINEMA_LIST);
        events.add(EVENT_RECORD_MAP_POI_LIST);
        events.add(EVENT_RECORD_NAV_APP_LIST);
        events.add(EVENT_RECORD_NO_TTS_QRCODE);
        events.add(EVENT_RECORD_POI_LIST);
        events.add(EVENT_RECORD_QRCODE);
        events.add(EVENT_RECORD_REMINDER_LIST);
        events.add(EVENT_RECORD_SIM_LIST);
        events.add(EVENT_RECORD_TTS_LIST);
        events.add(EVENT_RECORD_WECHAT_LIST);
        events.add(EVENT_VOICE_OPEN);
        events.add(EVENT_VOICE_DISMISS);
        events.add(EVENT_TXZ_INIT_SUCCESS);
        events.add(EVENT_ANJIXING_LOGIN);
        events.add(EVENT_ANJIXING_LOGOUT);
        events.add(EVENT_ANJIXING_HAS_BIND);
        events.add(EVENT_DEVICE_RED_BUTTON_PRESSED);
        events.add(EVENT_DEVICE_BLUE_BUTTON_PRESSED);
        events.add(EVENT_BOOT_COMPLETE);
        events.add(EVENT_WX_SCANNED);
        events.add(EVENT_BOOT_OPERATION_COMPLETE);
        events.add(EVENT_VOIP_READY);
        events.add(EVENT_VOIP_CALLING);
        events.add(EVENT_VOIP_TALKING);
        events.add(EVENT_DEVICE_RECORY_FACTORY);
        events.add(EVENT_DEVICE_SIM_READY);
        events.add(EVENT_WAKE_UP);
        events.add(EVENT_BOOT_INVALID);
        events.add(EVENT_WX_RECORD_BEGIN);
        events.add(EVENT_WX_RECORD_SEND_SUCCESS);
        events.add(EVENT_WX_RECORD_SEND_FAILED);
        events.add(EVENT_DEVICE_POWER_BEFORE_SLEEP);
        events.add(EVENT_DEVICE_POWER_WAKEUP);
        events.add(EVENT_GUIDE_COMPLETE);
        events.add(EVENT_GUIDE_INTERRUPT);
        events.add(EVENT_WX_MSG_NOTIFY);
        events.add(EVENT_WX_MSG_NOTIFY_DISMISS);
		events.add(EVENT_BOOT_TODAY_NOTICE_COMPLETE);
		events.add(EVENT_UPGRADE_APP_DISMISS);
		events.add(EVENT_UPGRADE_APP_SHOW);
		events.add(EVENT_VOIP_BEFORE_READY);
        events.add(EVENT_FM_START_NOTIFY_ADJUST);
        events.add(EVENT_FM_RECEIVE_STATE);
    }

    public static EventTypes getInstance() {
        if (sTypes == null) {
            synchronized (EventTypes.class) {
                if (sTypes == null) {
                    sTypes = new EventTypes();
                }
            }
        }
        return sTypes;
    }

    public boolean isContainType(String eventType) {
        return events.contains(eventType);
    }
}