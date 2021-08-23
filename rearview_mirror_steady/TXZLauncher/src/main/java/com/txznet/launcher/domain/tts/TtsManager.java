package com.txznet.launcher.domain.tts;

import android.support.annotation.Nullable;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.domain.txz.RecordWinManager;
import com.txznet.sdk.TXZResourceManager;

/**
 * Created by daviddai on 2018/11/16
 * tts管理类.
 * comm和core的tts方法不够用,需要我们自己封装.
 */
/**
 * 关闭所有的tts
 * 方案4：将所有的tts相关的东西放在界面移除的时候，拨打客服的时候关闭当前界面。
 *      - 优点：界面本来就应该考虑使用到的功能的tts的取消，所以这里没有工作量
 *      - 缺点：可能会改变产品定义
 * 方案6：电话工具可能会处理这个。还是使用电话工具。现在这个有问题，是因为我们发送电话状态的代码有问题。现在由于voip服务器的问题，暂时不能测试电话工具
 *      - 优点：通用
 *      - 缺点：要和大唐再联调下。让他们保证能发出界面出现和关闭的状态，不论服务器是否在线或者注册失败。
 */
public class TtsManager extends BaseManager {

    /////////////////////////////singleton/////////////////////////////
    private static TtsManager instance = null;

    private TtsManager() {
    }

    public static TtsManager getInstance() {
        synchronized (TtsManager.class) {
            if (instance == null) {
                instance = new TtsManager();
            }
        }

        return instance;
    }

    /////////////////////////////singleton/////////////////////////////

    /**
     * 播报tts,并且执行一些额外功能,如
     * 1. 下发文字给launcher
     * 2. 如果录音窗口打开中,可以关闭.
     *
     * @param text 要播报的文字
     * @param showText 是否展示文字，有时候会希望不展示文字。如增大音量
     * @param preemptType 排队类型
     * @param close 是否关闭窗口
     * @param needAsr 如果窗口时打开的话,tts播报完后是否继续进行设备.fixme 还没有实现
     * @param ttsCallback tts回调接口
     */
    public void speakTextOnRecordWin(final String text, final boolean showText, TtsUtil.PreemptType preemptType, final boolean close, final boolean needAsr, boolean isRemoteCommand, @Nullable final TtsUtil.ITtsCallback ttsCallback) {
        if (isRemoteCommand) {
            TXZResourceManager.getInstance().cancelCloseRecordWin();
        }
        TtsUtil.speakText(text, preemptType, new TtsUtil.ITtsCallback() {
            @Override
            public void onBegin() {
                super.onBegin();
                if (showText) {
                    // 目前好像comm没有提供播报tts并且下发文字的方法,所以这里就直接调用launcher的方法了.
                    RecordWinManager.getInstance().showSysText(text);
                }
                if (ttsCallback != null) {
                    ttsCallback.onBegin();
                }
            }

            @Override
            public void onSuccess() {
                super.onSuccess();
                if (close) {
                    TXZResourceManager.getInstance().dissmissRecordWin();
                } /*else if (needAsr && TXZRecordWinManager.getInstance().isOpened()) {
                    TXZAsrManager.getInstance().start();
                }*/
                if (ttsCallback != null) {
                    ttsCallback.onSuccess();
                }
            }

            @Override
            public void onCancel() {
                super.onCancel();
                if (ttsCallback != null) {
                    ttsCallback.onCancel();
                }
            }

            @Override
            public void onError(int iError) {
                super.onError(iError);
                TXZResourceManager.getInstance().dissmissRecordWin();
                if (ttsCallback != null) {
                    ttsCallback.onError(iError);
                }
            }

            @Override
            public void onEnd() {
                super.onEnd();
                if (ttsCallback != null) {
                    ttsCallback.onEnd();
                }
            }
        });
    }

    public static final class TtsBuilder {
        private String text;
        private boolean showText;
        private boolean close;
        private boolean needAsr;
        private boolean isRemoteCommand;
        private TtsUtil.PreemptType preemptType;
        private TtsUtil.ITtsCallback ttsCallback;

        private TtsBuilder() {
            // 设置一些默认值，可以减少set方法的调用
            showText = true;
            close = false;
            needAsr = true;
            isRemoteCommand = false;
            preemptType = TtsUtil.PreemptType.PREEMPT_TYPE_NEXT;
        }

        public static TtsBuilder create() {
            return new TtsBuilder();
        }

        public TtsBuilder setText(String text) {
            this.text = text;
            return this;
        }

        public TtsBuilder setShowText(boolean showText) {
            this.showText = showText;
            return this;
        }

        public TtsBuilder setClose(boolean close) {
            this.close = close;
            return this;
        }

        // FIXME: 2018/11/23 暂时没找到怎么设置启动asr的方法，就不让设置了。
        private TtsBuilder setNeedAsr(boolean needAsr) {
            this.needAsr = needAsr;
            return this;
        }

        public TtsBuilder setIsRemoteCommand(boolean isRemoteCommand) {
            this.isRemoteCommand = isRemoteCommand;
            return this;
        }

        public TtsBuilder setPreemptType(TtsUtil.PreemptType preemptType) {
            this.preemptType = preemptType;
            return this;
        }

        public TtsBuilder setTtsCallback(TtsUtil.ITtsCallback ttsCallback) {
            this.ttsCallback = ttsCallback;
            return this;
        }

        public void speak() {
            TtsManager.getInstance().speakTextOnRecordWin(text,showText, preemptType, close, needAsr,isRemoteCommand, ttsCallback);
        }
    }


}
