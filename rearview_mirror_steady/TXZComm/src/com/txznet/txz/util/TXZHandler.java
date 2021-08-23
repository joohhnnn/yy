package com.txznet.txz.util;

import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;

import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogicBase;

public class TXZHandler {
    /**
     * Runnable最大执行告警时间
     */
    protected static final int MAX_RUNNABLE_ALARM_TIME = 10 * 1000;
    /**
     * Runnable最大执行重启时间
     */
    protected static final int MAX_RUNNABLE_RESTART_TIME = 5 * 60 * 1000;
    /**
     * 最大队列值存在的时间
     */
    protected static final int MAX_COUNT_ALIVE_TIME = 30 * 1000;
    /**
     * 最大队列值
     */
    protected static final int MAX_EXECUTE_COUNT = 100;
    /**
     * 死锁检测线程
     */
    protected static Thread sLockWatchThread;
    /**
     * 创建的TXZHandler集合，用于给死锁线程检测
     */
    protected static List<WeakReference<TXZHandler>> sTXZHandlers = new ArrayList<WeakReference<TXZHandler>>();
    /**
     * UI线程最后的心跳时间
     */
    protected static long sUiThreadLastRunTime = SystemClock.elapsedRealtime();
	
	/**
	 * 设置线程为最大优先级
	 */
	public static void updateMaxPriority()
	{
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		try{
			Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_AUDIO);
		} catch (Exception e) {
		}
		try{
			Process.setThreadPriority(Process.myTid(), Process.THREAD_PRIORITY_URGENT_AUDIO);
		} catch (Exception e) {
		}
		LogUtil.logd("set thread[" + Thread.currentThread().getName() + "] pid[" + Process.myPid() + "] tid[" + Process.myTid() + "] max priority");
	}
	
	public static void updateToPriorityPriority(int priority)
	{
		try{
			Process.setThreadPriority(Process.myTid(), priority);
		} catch (Exception e) {
		}
		LogUtil.logd("set thread[" + Thread.currentThread().getName() + "] pid[" + Process.myPid() + "] tid[" + Process.myTid() + "] priority: " + priority);
	}


    public static final int TYPE_THREAD_MAX_SPEND_TIME = 1;//线程耗时最大时间导致
    public static final int TYPE_MAX_QUEUE_COUNT = 2;//最大连接队列导致


    /**
     * 为UI更新心跳时间
     */
    public static void heartbeatUi() {
        sUiThreadLastRunTime = SystemClock.elapsedRealtime();
    }

    public AtomicInteger count = new AtomicInteger(0);

    /**
     * 死锁异常，用于生成crash文件，会打出死锁的线程栈
     */
    /**
     * @author pppi
     */
    public static class DeadLockException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        private String mName;
        private StackTraceElement[] mStack;
        private String mMoreInfo;

        /**
         * 构造死锁异常
         *
         * @param name  线程名
         * @param stack 线程栈
         */
        public DeadLockException(String name, StackTraceElement[] stack) {
            this.mName = name;
            this.mStack = stack;
        }

        /**
         * 构造死锁异常
         *
         * @param name     线程名
         * @param stack    线程栈
         * @param moreInfo 其他额外信息
         */
        public DeadLockException(String name, StackTraceElement[] stack,
                                 String moreInfo) {
            this.mName = name;
            this.mStack = stack;
        }

		@Override
		public String getMessage() {
			if (null != mMoreInfo) {
				return "May dead lock at thread " + mName + ": " + mMoreInfo;
			}
			return "May dead lock at thread " + mName;
		}

		@Override
		public StackTraceElement[] getStackTrace() {
			return mStack;
		}

		@Override
		public void printStackTrace(PrintWriter err) {
			err.append(toString());
			err.append("\n");

			StackTraceElement[] stack = getStackTrace();
			if (stack != null) {
				for (int i = 0; i < stack.length; i++) {
					err.append("\tat ");
					err.append(stack[i].toString());
					err.append("\n");
				}
			}
		}
	}

    private static HandlerThreadExceptionListener mHandlerThreadExceptionListener = null;

    public static interface HandlerThreadExceptionListener {
        /**
         * @param type 异常类型
         * @return 是否需要杀掉进程
         */
        public boolean exception(int type);
    }

    public static void setCrashListener(HandlerThreadExceptionListener handlerThreadExceptionListener) {
        mHandlerThreadExceptionListener = handlerThreadExceptionListener;
    }

    /**
     * 输出线程异常状态，如果超过MAX_RUNNABLE_RESTART_TIME，则会保留死锁信息并重启
     *
     * @param th   线程
     * @param time 心跳间隔时长
     */
    protected static void showThreadStatus(Thread th, long time) {
        LogUtil.logw("LockWatch " + GlobalContext.get().getPackageName()
                + " may dead lock at thread " + th.getId() + ": "
                + th.getName() + ", lastHeartbeat=" + time);

        // 获取线程额外的死锁信息
        String info = null;
        int count = 0;
        synchronized (sTXZHandlers) {
            for (int i = 0; i < sTXZHandlers.size(); ++i) {
                TXZHandler h = sTXZHandlers.get(i).get();
                if (h == null)
                    continue;
                if (h.mThread.getId() == th.getId()) {
                    info = h.getInfo();
                    if (count < h.count.get()) {
                        count = h.count.get();
                    }

                    if (null != info) {
                        LogUtil.logw("LockWatch dump handler info: " + info
                                + "@" + h.toString());
                    }
                }
            }
        }
        boolean needKillSelf = false;
        if (time > MAX_RUNNABLE_RESTART_TIME) {
            if (mHandlerThreadExceptionListener != null) {
                needKillSelf = mHandlerThreadExceptionListener.exception(TYPE_THREAD_MAX_SPEND_TIME);
            } else {
                needKillSelf = true;
            }
        }
        if ((time > MAX_COUNT_ALIVE_TIME && count > MAX_EXECUTE_COUNT)) {
            if (mHandlerThreadExceptionListener != null) {
                needKillSelf = mHandlerThreadExceptionListener.exception(TYPE_MAX_QUEUE_COUNT);
            } else {
                needKillSelf = true;
            }
        }
        if (needKillSelf) {
            // 死锁过长作为crash上报并重启
            CrashCommonHandler.getInstance().uncaughtException(
                    null,
                    new DeadLockException(th.getName(), th.getStackTrace(),
                            info));
            return;
        }

		//输出死锁信息到日志
		StackTraceElement[] stacks = th.getStackTrace();
		for (StackTraceElement s : stacks) {
			LogUtil.logw("LockWatch thread " + th.getId() + "------"
					+ s.getClassName() + "::" + s.getMethodName() + "@"
					+ s.getFileName() + "#" + s.getLineNumber());
		}
	}

	/**
	 * 是否需要死锁监控
	 */
	public static boolean sNeedLockWatch = true;

	/**
	 * 初始化死锁监控
	 */
	public static void initLockWatch() {
		LogUtil.logd("LockWatch start...");
		heartbeatUi();
		// 监听同行者释放消息
		IntentFilter intentFilter = new IntentFilter(
				"com.txznet.txz.power.notify");
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String type = intent.getStringExtra("type");
				if ("release".equals(type)) {
					sNeedLockWatch = false;
				} else if ("init".equals(type)) {
					sNeedLockWatch = true;
				}
				LogUtil.logw("recv power notify : NeedLockWatch="
						+ sNeedLockWatch);
			}
		}, intentFilter);
		// 查询最后的状态
		Intent intent = new Intent("com.txznet.txz.power.query");
		GlobalContext.get().sendBroadcast(intent);
		final Handler uiHandler = new Handler(Looper.getMainLooper());
		// UI线程定时心跳
		uiHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				heartbeatUi();
				uiHandler.postDelayed(this, MAX_RUNNABLE_ALARM_TIME / 3);
			}
		}, MAX_RUNNABLE_ALARM_TIME / 3);
		// 死锁监控线程
		sLockWatchThread = new Thread(new Runnable() {
			@Override
			public void run() {
				updateToPriorityPriority(Process.THREAD_PRIORITY_BACKGROUND);
				long mainThreadId = Looper.getMainLooper().getThread().getId();
				long tPrev = SystemClock.elapsedRealtime();
				while (true) {
					try {
						// 等待一段时间检查每一个Handler
						Thread.sleep(MAX_RUNNABLE_ALARM_TIME / 2);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.HEARTBEAT_ENABLE, true)) {
						// 在主进程发送心跳，供第三方心跳监测使用
						if (AppLogicBase.isMainProcess()) {
							Intent intent = new Intent();
							intent.setAction(GlobalContext.get().getPackageName()
									+ ".heartbeat");
							intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
							GlobalContext.get().sendBroadcast(intent);
						}
					}
					// 判断是否需要死锁监控
					if (sNeedLockWatch == false) {
						continue;
					}
					long t = SystemClock.elapsedRealtime();
					// 当出现单次循环时间过长时，认为本次死锁判定环境不正常，可能系统负载过高或处于休眠状态，不做出判断，等待恢复正常
					if (t - tPrev > MAX_RUNNABLE_ALARM_TIME * 2) {
						LogUtil.logw("TXZHandler check heartbeat too long time");
						tPrev = t;
						continue;
					}
					tPrev = t;
					long lastRunTime = sUiThreadLastRunTime;
					long tt = t - lastRunTime;
					if (tt >= MAX_RUNNABLE_ALARM_TIME) {
						showThreadStatus(Looper.getMainLooper().getThread(), tt);
					}
					synchronized (sTXZHandlers) {
						for (int i = 0; i < sTXZHandlers.size(); ++i) {
							TXZHandler h = sTXZHandlers.get(i).get();
							if (h == null || !h.mThread.isAlive()) {
								sTXZHandlers.remove(i);
								--i;
								continue;
							}
							// 跳过主线程判断，主线程单独判断
							if (mainThreadId == h.mThread.getId()) {
								continue;
							}
							lastRunTime = h.mLastRunTime; // 必须取出来存下来
							if (lastRunTime == 0) {
								continue;
							}
							tt = t - lastRunTime;
							if (tt >= MAX_RUNNABLE_ALARM_TIME) {
								showThreadStatus(h.mThread, tt);
							}
						}
					}
				}
			}
		});
		sLockWatchThread.setName("LockWatcher");
		sLockWatchThread.setPriority(Thread.MIN_PRIORITY);
		sLockWatchThread.start();
	}

	/**
	 * 监控的线程
	 */
	private Thread mThread;
	/**
	 * 内部的真正的Handler
	 */
	private Handler mHandler;
	/**
	 * 最后的Runnable心跳时间点，为0表示执行结束
	 */
	private long mLastRunTime;

	/**
	 * 模拟Handler构造器
	 * 
	 * @param looper
	 *            线程的Looper
	 */
	public TXZHandler(Looper looper) {
		mLastRunTime = 0;
		mThread = looper.getThread();
		mHandler = new Handler(looper) {
			@Override
			public void handleMessage(Message msg) {
				TXZHandler.this.heartbeat();
				TXZHandler.this.handleMessage(msg);
				TXZHandler.this.resetTime();
			};
		};

		synchronized (sTXZHandlers) {
			sTXZHandlers.add(new WeakReference<TXZHandler>(this));
		}
	}

    /*
     * (non-Javadoc)
     *
     * @see android.os.Handler#postDelayed(Runnable r, long delayMillis)
     */
    public boolean postDelayed(Runnable r, long delayMillis) {
        count.getAndIncrement();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TXZHandler.this.heartbeat();
            }
        }, delayMillis);
        boolean ret = mHandler.postDelayed(r, delayMillis);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                count.getAndDecrement();
                TXZHandler.this.resetTime();
            }
        }, delayMillis);
        return ret;
    }

    /*
     * (non-Javadoc)
     *
     * @see android.os.Handler#postDelayed(Runnable r)
     */
    public boolean post(Runnable r) {
        count.getAndIncrement();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                TXZHandler.this.heartbeat();
            }
        });
        boolean ret = mHandler.post(r);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                count.getAndDecrement();
                TXZHandler.this.resetTime();
            }
        });
        return ret;
    }

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler#handleMessage(Message msg)
	 */
	public void handleMessage(Message msg) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler#removeCallbacks(Runnable r)
	 */
	public void removeCallbacks(Runnable r) {
		mHandler.removeCallbacks(r);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler#removeCallbacksAndMessages(Object token)
	 */
	public void removeCallbacksAndMessages(Object token) {
		mHandler.removeCallbacksAndMessages(token);
		if (token == null) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					TXZHandler.this.resetTime();
				}
			});
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.os.Handler#sendEmptyMessage(int what)
	 */
	public boolean sendEmptyMessage(int what) {
		return mHandler.sendEmptyMessage(what);
	}

	/**
	 * 手工触发心跳，同步心跳时间，如果Runnable执行时间过长，在内循环中调用心跳同步心跳时间
	 */
	public void heartbeat() {
		TXZHandler.this.mLastRunTime = SystemClock.elapsedRealtime();
	}

	/**
	 * 重置心跳时间，如果某Runnable不需要检查心跳，可在内部调用重置
	 */
	public void resetTime() {
		TXZHandler.this.mLastRunTime = 0;
	}

	/**
	 * @return 返回额外的定位信息，如ServiceManager线程的Handler，可以打印出当时现场死锁的命令字
	 */
	public String getInfo() {
		return null;
	}
}
