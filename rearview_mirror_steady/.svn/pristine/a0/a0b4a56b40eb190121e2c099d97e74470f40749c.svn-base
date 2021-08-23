package com.txznet.music.action;

import com.txznet.music.data.http.api.txz.entity.resp.PushResponse;

/**
 * @author telen
 * @date 2018/12/21,17:04
 */
public class PushActionCreator {

    /**
     * 单例对象
     */
    private volatile static PushActionCreator singleton;

    private PushActionCreator() {
    }

    public static PushActionCreator getInstance() {
        if (singleton == null) {
            synchronized (PushActionCreator.class) {
                if (singleton == null) {
                    singleton = new PushActionCreator();
                }
            }
        }
        return singleton;
    }

    /**
     * 选中第几个
     *
     * @param pushResponse
     */
    public void choiceItem(PushResponse pushResponse) {
//        release();
//        if (!isWakeupResult()) {
//            TtsUtil.speakTextOnRecordWin("RS_VOICE_MUSIC_WILL_PLAY_SHORT_PLAY", Constant.RS_VOICE_MUSIC_WILL_PLAY_SHORT_PLAY, true, new Runnable() {
//                @Override
//                public void run() {
//                    PushManager.getInstance().playDetail(EnumState.Operation.sound, pushResponse);
//                }
//            });
//        } else {
//            PushManager.getInstance().playDetail(EnumState.Operation.sound, pushResponse);
//        }
//
//        ReportEventProtocol.reportPushEvent(PushEvent.ACTION_CONTINUE_SOUND, PushEvent.getType(pushResponse), pushResponse.getMid());
//        saveMessage(pushResponse, true);
    }


    /**
     * 取消选择
     *
     * @param pushResponse
     */
    public void cancel(PushResponse pushResponse) {

    }
}
