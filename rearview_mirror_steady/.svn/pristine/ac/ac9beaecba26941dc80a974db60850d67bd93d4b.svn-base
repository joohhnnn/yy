package com.txznet.comm.ui.theme.test.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.txznet.resholder.BuildConfig;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 打印效果的TextView
 *
 * @author zackzhou
 */
public class PrinterTextView extends TextView {

    private final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * 变换节拍
     */
    private final int DEFAULT_PRINT_TIME_DELAY = 200;

    private int printIntervalTime = DEFAULT_PRINT_TIME_DELAY; // 每个字的间隔

    private String needPrintStr; // 需要打印的字符串

    private int lastPrintStrLen;

    public PrinterTextView(Context context) {
        super(context);
    }

    public PrinterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
        if (text == null || text.length() == 0) {
            lastPrintStrLen = 0;
        }
    }

    /**
     * 字数比原文本小或相同的情况，直接替换，重置打字个数
     * 字数比原文本大的情况，
     */
    public void setPrintText(String str) {
        _debuglog("setPrintText str=" + str);
        setPrintText(str, DEFAULT_PRINT_TIME_DELAY);
    }

    /**
     * 示例：
     * 吃
     * 在不在
     * 则不在此限
     * 则不在此限
     * 这不再只是
     * 这不再只是
     * 这不再只是
     * 这不但只是
     * 这不再只是简单
     * 这不再只是简单
     * 这不再只是简单的代步工具
     * 这不再只是简单的代步工具
     * 这不再只是简单的代步工具
     * 这不再只是简单的代步工具在券商
     * 这不再只是简单的代步工具在券商的
     * 这不再只是简单的代步工具债券上的
     * 这不再只是简单的代步工具债券上的
     * 这不再只是简单的代步工具债券上的纤夫
     * 这不再只是简单的代步工具债券上的纤夫
     * <p>
     * 长度一致高概率相同，跳过动画
     * 长度增多，多为添加
     * 长度减少，多为新文本
     */
    public void setPrintText(String str, int printInterval) {
        _debuglog("set## " + str);

        // 清除内容
        if (str == null || str.length() == 0) {
            lastPrintStrLen = 0;
            setText(str);
            return;
        }

        // 重置任务
        this.printIntervalTime = printInterval;
        releasePrintTextTimer();

        int maxPrint = getText().length();
        int dstLen = str.length();
        lastPrintStrLen = str.length();
        // 文本最大打印长度比原文本长度小
        if (dstLen <= maxPrint) {
            // 直接替换
            setText(str);
            _debuglog("replace## " + str);
        } else {
            // 文本最大打印长度比原文本长度大
            setText(str.substring(0, maxPrint));
            _debuglog("replace## " + str.substring(0, maxPrint));
            needPrintStr = str.substring(maxPrint);
            appendTextIfNeeded();
            _debuglog("append## " + needPrintStr);
        }
    }

    private void appendTextIfNeeded() {
        if (TextUtils.isEmpty(needPrintStr)) {
            return;
        }
        appendTextByInterval(needPrintStr, printIntervalTime);
    }

    protected void appendTextAtTime(String text, int time) {
        _debuglog("appendTextAtTime text=" + text);
        int len = text.length();
        int delay = time / len;
        releasePrintTextTimer();
        printTextTimer = new Timer();
        printTextTimer.schedule(new PrintTextTask(text), 0, delay);
    }

    protected void appendTextByInterval(String text, int time) {
        _debuglog("appendTextByInterval text=" + text);
        releasePrintTextTimer();
        printTextTimer = new Timer();
        printTextTimer.schedule(new PrintTextTask(text), 0, time);
    }

    private Timer printTextTimer; // 打印计时器

    /**
     * 退格文本任务
     */
    private class PrintTextTask extends TimerTask {

        private String text;
        private int index;

        public PrintTextTask(String text) {
            this.text = text;
            this.index = 0;
        }

        @Override
        public void run() {
            if (isDetachedFromWindow || printTextTimer == null) {
                return;
            }
            post(new Runnable() {
                @Override
                public void run() {
                    if (isDetachedFromWindow || printTextTimer == null) {
                        return;
                    }
                    if (index > text.length() - 1) {
                        releasePrintTextTimer();
                    } else {
                        char c = text.charAt(index++);
                        append("" + c);
                    }
                }
            });
        }
    }

    private void releasePrintTextTimer() {
        if (printTextTimer != null) {
            _debuglog("releasePrintTextTimer");
            printTextTimer.cancel();
            printTextTimer = null;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isDetachedFromWindow = true;
        // 回收异步任务
        releasePrintTextTimer();
    }

    // 死亡标记
    protected volatile boolean isDetachedFromWindow;

    private void _debuglog(String msg) {
        if (DEBUG) {
            Log.d("PrinterTextView", msg);
        }
    }
}
