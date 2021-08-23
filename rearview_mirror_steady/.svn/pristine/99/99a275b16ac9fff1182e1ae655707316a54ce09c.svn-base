/**
 * 
 */
package com.txznet.music.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.util.Log;

/**
 * @author telenewbie
 * @Date 2016年7月18日17:44:06
 */
public final class AsyncTaskManager {

    private static final String TAG = "TaskManager";

    private static final int POOL_SIZE = 1;

    // 线程池
    private static ExecutorService EXEC = Executors
    // .newFixedThreadPool(POOL_SIZE);
            .newCachedThreadPool();

    private AsyncTaskManager() {
    }

    /**
     * @desc <pre>
     * 异步执行
     * </pre>
     * @author Erich Lee
     * @date Mar 11, 2013
     * @param callable
     */
    public static <V> void submitSync(Callable<V> callable) {
        EXEC.submit(callable);
    }

    /**
     * @desc <pre>
     * 同步执行
     * </pre>
     * @author Erich Lee
     * @date Mar 11, 2013
     * @param callable
     * @return
     */
    public static <V> V submitAsync(Callable<V> callable) {
        V result = null;
        try {
            Future<V> future = EXEC.submit(callable);
            result = future.get();
            return result;
        } catch (InterruptedException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        } catch (ExecutionException e) {
            Log.e(TAG, e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * @desc <pre>
     * 停止所有线程
     * </pre>
     * @author Erich Lee
     * @date Mar 11, 2013
     */
    public static void shutdownAndAwaitTermination() {
        try { // Wait a while for existing tasks to terminate
            EXEC.shutdownNow(); // 立即关闭所有的线程，与shutdown的区别：当我们shutdown调用这个方法时，ExecutorService停止接受任何新的任务且等待已经提交的任务执行完成
            if (!EXEC.awaitTermination(60, TimeUnit.MILLISECONDS)) {// 60mm
                EXEC.shutdown(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!EXEC.awaitTermination(60, TimeUnit.MILLISECONDS))// 60mm
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            EXEC.shutdownNow(); // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
        EXEC = Executors
        // .newFixedThreadPool(POOL_SIZE);
                .newCachedThreadPool();

    }

    public static void cancelTask(AsyncTask task) {
        if (task != null) {
            if (!task.isCancelled() && !task.getStatus().equals(Status.FINISHED)) {
                task.cancel(true);
            }
        }
    }

    public static boolean isInvalid(AsyncTask task) {
        if (task == null || task.isCancelled() || task.getStatus().equals(Status.FINISHED)) {
            return true;
        }
        return false;
    }

    public static boolean canExecute(AsyncTask task) {
        if (task != null && (task.getStatus().equals(Status.FINISHED) || task.isCancelled())) {
            return true;
        }
        return false;
    }

}
