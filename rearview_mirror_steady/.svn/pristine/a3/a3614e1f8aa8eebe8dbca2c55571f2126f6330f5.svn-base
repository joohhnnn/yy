package com.txznet.music.util;

import android.os.Debug;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;

import java.util.Arrays;

import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.WARN;

/**
 * Created by brainBear on 2018/1/10.
 */

public class Logger {

    private static final String SUFFIX_JAVA = ".java";
    private static final int MIN_STACK_OFFSET = 2;
    private static boolean mNeedStackLog = BuildConfig.DEBUG;

    public static void d(String tag, Object log) {
        printText(DEBUG, tag, log);
    }

    public static void i(String tag, Object log) {
        printText(INFO, tag, log);
    }

    public static void w(String tag, Object log) {
        printText(WARN, tag, log);
    }

    public static void e(String tag, Object log) {
        printText(ERROR, tag, log);
    }

    public static void eLarge(String tag, String log) {
        //因为String的length是字符数量不是字节数量所以为了防止中文字符过多，
        //  把4*1024的MAX字节打印长度改为2001字符数
        int max_str_length = 2001 - tag.length();
        //大于4000时
        while (log.length() > max_str_length) {
            printText(ERROR, tag, log.substring(0, max_str_length));
            log = log.substring(max_str_length);
        }
        //剩余部分
        printText(ERROR, tag, log);
    }

    public static void d(String tag, String format, Object... args) {
        printText(DEBUG, tag, createMessage(format, args));
    }

    public static void i(String tag, String format, Object... args) {
        printText(INFO, tag, createMessage(format, args));
    }

    public static void w(String tag, String format, Object... args) {
        printText(WARN, tag, createMessage(format, args));
    }

    public static void e(String tag, String format, Object... args) {
        printText(ERROR, tag, createMessage(format, args));
    }

    public static void printStack(String tag) {
        printText(DEBUG, tag, Log.getStackTraceString(new Throwable()));
    }

    public static void test(String tag, Object log) {
        if (BuildConfig.DEBUG) {
            printText(DEBUG, tag, log);
        }
    }

    public static void test(String tag, String format, Object... args) {
        if (BuildConfig.DEBUG) {
            printText(DEBUG, tag, createMessage(format, args));
        }
    }

    private static String createMessage(String message, Object... args) {
        return args == null || args.length == 0 ? message : String.format(message, args);
    }


    private static void printText(int level, String tag, Object log) {
        String headInfo = getHeadInfo();

        String message;
        if (null == log) {
            message = "null";
        } else if (log.getClass().isArray()) {
            message = Arrays.deepToString((Object[]) log);
        } else {
            message = log.toString();
        }
        LogUtil.invokeRemoteLog(level, tag, headInfo + " " + message);
    }


    private static String getHeadInfo() {
        if (!mNeedStackLog) {
            return "";

        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        int index = getStackOffset(stackTrace);
        if (index == -1) {
            return "[Get Info Error]";
        }
        StackTraceElement element = stackTrace[index];
        String className = element.getClassName();
        if (className.contains(".")) {
            String[] names = className.split("\\.");
            className = names[names.length - 1] + SUFFIX_JAVA;
        }

        if (className.contains("$")) {
            className = className.split("\\$")[0] + SUFFIX_JAVA;
        }

        String methodName = element.getMethodName();
        int lineNumber = element.getLineNumber();

        return "[(" + className + ":" + lineNumber + ")#" + methodName + "]";
    }

    private static int getStackOffset(StackTraceElement[] stackTrace) {
        if (null != stackTrace) {
            for (int i = MIN_STACK_OFFSET; i < stackTrace.length; i++) {
                if (!stackTrace[i].getClassName().equals(Logger.class.getName())) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static int calSize = 0;

    public static void printCurrentMemory(String myTag) {
        calSize++;
        Debug.MemoryInfo memoryInfo = ProcessMemoryMonitor.getInstance().getMemoryInfo(GlobalContext.get().getPackageName());
        if (null != memoryInfo) {
            Logger.d(Constant.LOG_TAG_MONITOR, "[" + myTag + "]current memory:[" + calSize + "] " + memoryInfo.getTotalPss());
        } else {
            Logger.d(Constant.LOG_TAG_MONITOR, "[" + myTag + "]current memory:[" + calSize + "] 0M");
        }
    }

}
