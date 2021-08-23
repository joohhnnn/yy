package com.txznet.music.util;

import android.os.Looper;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.music.BuildConfig;
import com.txznet.music.R;

import java.lang.ref.WeakReference;

/**
 * Toast统一管理类
 */
public class ToastUtils {

    public static boolean isShow = true;
    private static WeakReference<Toast> toastRef;

    private ToastUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    private static TextView buildToast() {
        return (TextView) LayoutInflater.from(GlobalContext.get()).inflate(R.layout.gloal_toast, null, false);
    }

    /**
     * 短时间显示Toast
     *
     * @param message
     */
    public static void showShortOnUI(final CharSequence message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            AppLogic.runOnUiGround(() -> {
                showShortOnUI(message);
            }, 0);
            return;
        }

        if (BuildConfig.DEBUG) {
            Logger.d("Music:Toast", "show toast msg=" + message);
        }
        if (isShow) {
            if (toastRef != null && toastRef.get() != null) {
                toastRef.get().cancel();
                toastRef = null;
            }
            Toast toast = Toast.makeText(GlobalContext.get().getApplicationContext(),
                    message, Toast.LENGTH_SHORT);
            TextView content = buildToast();
            content.setText(message);
            toast.setView(content);
            toast.show();
            toastRef = new WeakReference<>(toast);
        }
    }

    /**
     * 短时间显示Toast
     *
     * @param strRes
     */
    public static void showShortOnUI(@StringRes int strRes) {
        showShortOnUI(GlobalContext.get().getResources().getString(strRes));
    }

    /**
     * 长时间显示Toast
     *
     * @param message
     */
    public static void showLongOnUI(CharSequence message) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            AppLogic.runOnUiGround(() -> {
                showLongOnUI(message);
            }, 0);
            return;
        }

        if (BuildConfig.DEBUG) {
            Logger.d("Music:Toast", "show toast msg=" + message);
        }

        if (isShow) {
            if (toastRef != null && toastRef.get() != null) {
                toastRef.get().cancel();
                toastRef = null;
            }
            Toast toast = Toast.makeText(GlobalContext.get().getApplicationContext(),
                    message, Toast.LENGTH_LONG);
            TextView content = buildToast();
            content.setText(message);
            toast.setView(content);
            toast.show();
            toastRef = new WeakReference<>(toast);
        }
    }
}
