//package com.txznet.music.push;
//
//import com.txz.report_manager.ReportManager;
//import com.txznet.comm.remote.util.Logger;
//import com.txznet.comm.remote.util.ReportUtil;
//import com.txznet.music.push.bean.PullData;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//
///**
// * Created by brainBear on 2018/1/17.
// */
//
//public class PushReport {
//
//    /**
//     * 推送到达
//     */
//    public final static String ACTION_PUSH_ARRIVE = "push_arrive";
//    /**
//     * 推送展示成功
//     */
//    public final static String ACTION_SHOW = "show";
//    /**
//     * 声控取消
//     */
//    public final static String ACTION_CANCEL_SOUND = "cancel_sound";
//    /**
//     * 手动取消
//     */
//    public final static String ACTION_CANCEL_MANUAL = "cancel_manual";
//    /**
//     * 声控继续
//     */
//    public final static String ACTION_CONTINUE_SOUND = "continue_sound";
//    /**
//     * 手动继续
//     */
//    public final static String ACTION_CONTINUE_MANUAL = "continue_manual";
//    /**
//     * 倒计时结束取消
//     */
//    public final static String ACTION_TIMEOUT = "timeout";
//
//    /**
//     * 倒计时开始
//     */
//    public final static String ACTION_COUNTDOWN = "countdown";
//    /**
//     * 快报推送
//     */
//    public final static String TYPE_SHORTPLAY = "push_shortplay";
//    /**
//     * 微信推送
//     */
//    public final static String TYPE_WX = "push_wx";
//    /**
//     * 更新推送
//     */
//    public final static String TYPE_UPDATE = "push_update";
//
//
//    public final static String TYPE_UNKNOWN = "push_unknown";
//
//    private static final String TAG = "PushReport";
//    private final static int UAT = ReportManager.UAT_MUSIC;
//
//    private final static String KEY_TYPE = "type";
//    private final static String KEY_ACTION = "action";
//    private final static String KEY_ID = "id";
//
//
//    private PushReport() {
//    }
//
//
//    private static String createJsonString(String action, String type, int id) {
//        JSONObject jsonObject = new JSONObject();
//        try {
//            jsonObject.put(KEY_TYPE, type);
//            jsonObject.put(KEY_ACTION, action);
//            jsonObject.put(KEY_ID, id);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return jsonObject.toString();
//    }
//
//    public static void doReport(String action, String type, int id) {
//        String jsonString = createJsonString(action, type, id);
//        Logger.d(TAG, "do report:" + jsonString);
//        ReportUtil.doReport(UAT, jsonString);
//    }
//
//
//    public static void doReportImmediate(String action, String type, int id) {
//        String jsonString = createJsonString(action, type, id);
//        Logger.d(TAG, "do report Immediate:" + jsonString);
//        ReportUtil.doReportImmediate(UAT, jsonString.getBytes());
//    }
//
//
//    public static String getType(PullData data) {
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
//    }
//
//}
