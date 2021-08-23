package com.txznet.music.report.bean;

import com.txznet.music.push.bean.PullData;
import com.txznet.music.push.bean.PushResponse;
import com.txznet.music.report.ReportEventConst;

/**
 * Created by brainBear on 2018/1/24.
 */

public class PushEvent extends EventBase {

    /**
     * 推送到达
     */
    public final static String ACTION_PUSH_ARRIVE = "push_arrive";
    /**
     * 预推送到达
     */
    public final static String ACTION_PRE_PUSH_ARRIVE = "pre_push_arrive";
    /**
     * 推送展示成功
     */
    public final static String ACTION_SHOW = "show";
    /**
     * 声控取消
     */
    public final static String ACTION_CANCEL_SOUND = "cancel_sound";
    /**
     * 手动取消
     */
    public final static String ACTION_CANCEL_MANUAL = "cancel_manual";
    /**
     * 声控继续
     */
    public final static String ACTION_CONTINUE_SOUND = "continue_sound";
    /**
     * 手动继续
     */
    public final static String ACTION_CONTINUE_MANUAL = "continue_manual";
    /**
     * 倒计时结束取消
     */
    public final static String ACTION_TIMEOUT = "timeout";
    /**
     * 倒计时开始
     */
    public final static String ACTION_COUNTDOWN = "countdown";
    /**
     * 快报推送
     */
    public final static String TYPE_SHORTPLAY = "push_shortplay";
    /**
     * 微信推送
     */
    public final static String TYPE_WX = "push_wx";
    /**
     * 更新推送
     */
    public final static String TYPE_UPDATE = "push_update";

    /**
     * 未知推送
     */
    public final static String TYPE_UNKNOWN = "push_unknown";

    public String action;
    public String type;
    public String id;


    public PushEvent(String action, String type, String id) {
        super(ReportEventConst.PUSH_EVENT_ID);
        this.action = action;
        this.type = type;
        this.id = id;
    }


    public static String getType(PullData data) {
//        String type = TYPE_UNKNOWN;
//        switch (data.getType()) {
//            case PullData.TYPE_AUDIOS:
//                type = TYPE_WX;
//                break;
//
//            case PullData.TYPE_NEWS:
//                type = TYPE_SHORTPLAY;
//                break;
//
//            case PullData.TYPE_UPDATE:
//                type = TYPE_UPDATE;
//                break;
//        }
//        return type;

        return String.valueOf(data.getType());

    }

    public static  String getType(PushResponse pushResponse) {
        if (pushResponse != null) {
            return String.valueOf(pushResponse.getType());
        }
        return String.valueOf(-1);//默认

    }

}
