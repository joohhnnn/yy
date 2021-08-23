package com.txznet.comm.remote.util;

import static android.util.Log.ASSERT;
import static android.util.Log.DEBUG;
import static android.util.Log.ERROR;
import static android.util.Log.INFO;
import static android.util.Log.WARN;
import static com.txznet.comm.remote.ServiceManager.TXZ;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.Arrays;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.udprpc.TXZUdpClient;
import com.txznet.comm.remote.udprpc.UdpDataFactory.UdpData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.plugin.PluginManager;

public class LogUtil {
	private LogUtil() {
	}

	static StackTraceElement mStackTraceElement = null;
	static boolean mNeedStackLog = LogUtil.class.getName().equals(
			"com.txznet.comm.remote.util.LogUtil");

	static int _log(int level, String content) {
		String tag = "TXZ";
		try {
			if (mNeedStackLog) {
				if (null == mStackTraceElement) {
					recordLogStack(2);
				}
				tag = mStackTraceElement.getClassName();
				// int n = tag.lastIndexOf('.');
				// if (n >= 0)
				// tag = tag.substring(n + 1);
				String prefix = "[" + tag + "::"
						+ mStackTraceElement.getMethodName() + "]";
				String suffix = "[" + mStackTraceElement.getFileName() + ":"
						+ mStackTraceElement.getLineNumber() + "]";
				content = prefix + content + suffix;
			}
			invokeRemoteLog(level, tag, content);
		} catch (Exception e) {
		} finally {
			mStackTraceElement = null;
		}
		return 0;
	}

	private static Class<?> clsJNIHelper = null;
	private static Method mMethodLogRaw = null;

    public static void invokeRemoteLog(int level, String tag, String content) {
		if (GlobalContext.isTXZ() && AppLogicBase.isMainProcess()) {
			try {
				if (mMethodLogRaw == null || clsJNIHelper == null) {
					clsJNIHelper = Class.forName("com.txznet.txz.jni.JNIHelper");
					mMethodLogRaw = clsJNIHelper.getMethod("_logRaw", String.class, int.class,
							String.class, String.class);
				}
				mMethodLogRaw.invoke(clsJNIHelper, "", level, tag, content);
			} catch (Exception e) {
			}
		} else {
			if (level >= mConsoleLogLevel) {
				Log.println(level, tag, content);
			}
			if (level >= mFileLogLevel) {
				try {
					// 没有初始化上下文不允许打远程日志
					GlobalContext.get();
				} catch (IllegalStateException e) {
					return;
				}
				if (content.length() > 900) {
					content = content.substring(0, 300) + "\n............too many logs...........\n"
							+ content.substring(content.length() - 300);
				}
				String data = null;
				if (TXZUdpClient.getInstance().isInConnection()) {
					data = new JSONBuilder().put("pid", Process.myPid()).put("tid", Process.myTid()).put("level", level)
							.put("tag", tag).put("content", content).put("seq", sUdpSeq)
							.put("package", GlobalContext.get().getPackageName()).toString();
					sUdpSeq = getNextSeq();
					TXZUdpClient.getInstance().sendInvoke(UdpData.CMD_LOG, data.getBytes());
				} else {
					data = new JSONBuilder().put("pid", Process.myPid()).put("tid", Process.myTid()).put("level", level)
							.put("tag", tag).put("content", content).toString();
					ServiceManager.getInstance().sendInvoke(TXZ, "comm.log", data.getBytes(),
                            null, ServiceManager.DEFAULT_TIMEOUT_SHORT);
                }
            }
		}
	}

	static int mConsoleLogLevel = Log.VERBOSE;
	static int mFileLogLevel = Log.VERBOSE;
	
	public static boolean needConsoleLog(int level) {
		return level >= mConsoleLogLevel;
	}

	public static void notifyLogLevels() {
		if (AppLogicBase.isMainProcess()) {
			Intent intent = new Intent( GlobalContext.get().getPackageName() + ".LogUtil.level.notify");
			intent.putExtra("console", mConsoleLogLevel);
			intent.putExtra("file", mFileLogLevel);
			GlobalContext.get().sendBroadcast(intent);
		}
	}
	
	public static boolean ENABLE_LOG = new File(Environment.getExternalStorageDirectory() +"/txz/" ,"log_enable_file").exists();
	
	static {
		if (!AppLogicBase.isMainProcess()) {
			IntentFilter intentFilter = new IntentFilter( GlobalContext.get().getPackageName() + ".LogUtil.level.notify");
			GlobalContext.get().registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					mConsoleLogLevel = intent.getIntExtra("console", mConsoleLogLevel);
					mFileLogLevel = intent.getIntExtra("file", mFileLogLevel);
				}
			}, intentFilter);
			
			Intent intent = new Intent( GlobalContext.get().getPackageName() + ".LogUtil.level.query");
			GlobalContext.get().sendBroadcast(intent);
		} else {
			IntentFilter intentFilter = new IntentFilter( GlobalContext.get().getPackageName() + ".LogUtil.level.query");
			GlobalContext.get().registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					notifyLogLevels();
				}
			}, intentFilter);
		} 
	}
	
	public static void setConsoleLogLevel(int level) {
		if(ENABLE_LOG) {
			mConsoleLogLevel = Log.VERBOSE;
		} else {
			mConsoleLogLevel = level;
		}
		
		if (GlobalContext.isTXZ()) {
			try {
				Class<?> cls = Class.forName("com.txznet.txz.jni.JNIHelper");
				Method m = cls.getMethod("setConsoleLogLevel", int.class);
				m.invoke(cls, level);
			} catch (Exception e) {
			}
		}
		
		notifyLogLevels();
	}
	

	public static void setFileLogLevel(int level) {
		if(ENABLE_LOG) {
			mFileLogLevel  = Log.VERBOSE;
		} else {
			mFileLogLevel = level;
		}
		
		if (GlobalContext.isTXZ()) {
			try {
				Class<?> cls = Class.forName("com.txznet.txz.jni.JNIHelper");
				Method m = cls.getMethod("setFileLogLevel", int.class);
				m.invoke(cls, level);
			} catch (Exception e) {
			}
		}
		
		notifyLogLevels();
	}

	public static int log(int level, String content) {
		return _log(level, content);
	}

	public static int logd(String content) {
		return _log(DEBUG, content);
	}

	public static int logi(String content) {
		return _log(INFO, content);
	}

	public static int logw(String content) {
		return _log(WARN, content);
	}

	public static int loge(String content) {
		return _log(ERROR, content);
	}
	
    static void printLogToConsole(int level, String tag, String content) {
    	if(level > mConsoleLogLevel) {
    		Log.println(level, tag, content);
    	}
    }

	public static void justConsole_logd(String tag , String content) {
		printLogToConsole(DEBUG, tag , content);
	}

	public static void justConsole_logi(String tag , String content) {
		printLogToConsole(INFO, tag , content);
	}

	public static void justConsole_logw(String tag , String content) {
		printLogToConsole(WARN, tag , content);
	}

	public static void justConsole_loge(String tag , String content) {
		printLogToConsole(ERROR, tag , content);
	}
	
	public static int loge(String content,Throwable tr) {
		if(tr!=null){
			StringWriter sw=new StringWriter();
			PrintWriter pw=new PrintWriter(sw);
			tr.printStackTrace(pw);
			pw.flush();
			content+="\r\n"+sw.toString();
		}
		
		return _log(ERROR, content);
	}

	public static int logf(String content) {
		return _log(ASSERT, content);
	}

	/**
	 * 记录日志栈
	 * 
	 * @param depth
	 *            栈深度，0表示当前当前调用位置，1表示上一级
	 */
	public static void recordLogStack(int depth) {
		if (mNeedStackLog) {
			mStackTraceElement = new Throwable().getStackTrace()[depth + 1];
		}
	}
	
	public static void addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("comm.log.",logProcessor );
	}
	private static LogProcessor logProcessor = new LogProcessor(); 
	private static class LogProcessor implements PluginManager.CommandProcessor {

		@Override
		public Object invoke(String command, Object[] args) {
			if (command.equals("logd")) {
				LogUtil.logd((String) args[0]);
			}
			else if (command.equals("loge")) {
				LogUtil.loge((String) args[0]);
			}
			else if (command.equals("logf")) {
				LogUtil.logf((String) args[0]);
			}
			else if (command.equals("logi")) {
				LogUtil.logi((String) args[0]);
			}
			else if (command.equals("logw")) {
				LogUtil.logw((String) args[0]);
			}
			else if (command.equals("log")) {
				LogUtil.log((Integer)args[0],(String) args[1]);
			}
			return null;
		}
		
	}
	
	private static final String SUFFIX_JAVA = ".java";
	private static final int MIN_STACK_OFFSET = 2;


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

	public static void d(Object log) {
		printText(DEBUG, "TXZ", log);
	}

	public static void i(Object log) {
		printText(INFO, "TXZ", log);
	}

	public static void w(Object log) {
		printText(WARN, "TXZ", log);
	}

	public static void e(Object log) {
		printText(ERROR, "TXZ", log);
	}

	private static int sUdpSeq = 0;

	private static int getNextSeq() {
		if (sUdpSeq < Integer.MAX_VALUE) {
			return sUdpSeq + 1;
		} else {
			return 0;
		}
	}

//	public static void setUdpSeq(int seq) {
//		sUdpSeq = seq;
//	}
	
	private static void printText(int level, String tag, Object log){
		String headInfo = getHeadInfo();

        String message;
        if (log.getClass().isArray()) {
            message = Arrays.deepToString((Object[]) log);
        } else {
            message = log.toString();
        }

        invokeRemoteLog(level, tag, headInfo + " " + message);
	}
	
	private static String getHeadInfo() {
		if(!mNeedStackLog){
			return "";
		}
		
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        int index = getStackOffset(stackTrace);
        if(index == -1){
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
            for (int i = MIN_STACK_OFFSET; i < stackTrace.length; i++){
                if(!stackTrace[i].getClassName().equals(LogUtil.class.getName())){
                    return i;
                }
            }
        }
        return -1;
    }
}
