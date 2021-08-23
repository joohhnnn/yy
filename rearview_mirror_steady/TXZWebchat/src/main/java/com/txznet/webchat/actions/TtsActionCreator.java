package com.txznet.webchat.actions;


import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.model.TtsModel;

public class TtsActionCreator {
    private static TtsActionCreator sInstance;
    private Dispatcher dispatcher;

    TtsActionCreator(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static TtsActionCreator get() {
        if (sInstance == null) {
            synchronized (TtsActionCreator.class) {
                if (sInstance == null) {
                    sInstance = new TtsActionCreator(Dispatcher.get());
                }
            }
        }
        return sInstance;
    }

    /**
     * 添加待播报的消息到队列末尾
     *
     * @param message 需要播报的消息
     */
    public void addTts(WxMessage message) {
        TtsModel model = new TtsModel();
        model.message = message;
        addTts(model);
    }

    /**
     * 添加tts到队列末尾
     * @param text 文本
     * @param callback 播报完毕回调
     */
    public void addTts(String text, TtsUtil.ITtsCallback callback) {
        TtsModel model = new TtsModel();
        model.text = text;
        model.force = false;
        model.callback = callback;

        addTts(model);
    }

    /**
     * 添加tts到队列末尾
     *
     * @param data 构造的TtsModel
     */
    public void addTts(TtsModel data) {
        dispatcher.dispatch(new Action<TtsModel>(ActionType.TXZ_TTS_QUEUE_ADD, data));
    }

    /**
     * 通知处理tts队列
     */
    public void procTtsQueue() {
        dispatcher.dispatch(new Action<String>(ActionType.TXZ_TTS_QUEUE_PROC, null));
    }

    /**
     * 插入tts到队列开头
     *
     * @param text     文本
     * @param force    是否打断当前播报
     * @param callback 播报完毕回调
     */
    public void insertTts(String text, boolean force, TtsUtil.ITtsCallback callback) {
        insertTts("", null, text, force, callback);
    }

    public void insertTts(String resId, String text, boolean force, TtsUtil.ITtsCallback callback) {
        insertTts(resId, null, text, force, callback);
    }

    public void insertTts(String resId, String[] resArgs, String text, boolean force, TtsUtil.ITtsCallback callback) {
        TtsModel model = new TtsModel();
        model.resId = resId;
        model.resArgs = resArgs;
        model.text = text;
        model.force = force;
        model.callback = callback;
        insertTts(model);
    }

    public void insertTts(WxMessage msg) {
        TtsModel model = new TtsModel();
        model.message = msg;
        insertTts(model);
    }


    /**
     * 插入tts到队列开头
     * @param model 构造的TtsModel
     */
    public void insertTts(TtsModel model) {
        dispatcher.dispatch(new Action<>(ActionType.TXZ_TTS_QUEUE_INSERT, model));
    }

    public void repeatMessage(WxMessage message) {
        dispatcher.dispatch(new Action<>(ActionType.WX_REPEAT_MSG, message));
    }

    public void skipRepeatMessage() {
        dispatcher.dispatch(new Action<>(ActionType.WX_SKIP_REPEAT_MSG, null));
    }
}
