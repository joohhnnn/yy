package com.txznet.txz.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.jni.JNIHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class PermissionUtils {

    private static final String TAG = "[PermissionUtils]--";

    public static final int NEED_REQUEST_RUNTIME_PERMISSIONS =  TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_NEED_REQUEST_RUNTIME_PERMISSIONS,0);
    public static final String REQUEST_RUNTIME_PERMISSIONS_TIPS = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_REQUEST_RUNTIME_PERMISSIONS_TIPS);

    /**
     * 可以显示在其他应用之上的权限
     */
    private static final int FLAG_SYSTEM_ALERT_WINDOW = 0x1;

    public static void grantOverlayPermission() {
        if ((NEED_REQUEST_RUNTIME_PERMISSIONS & FLAG_SYSTEM_ALERT_WINDOW) != 0) {
            if (!(Build.VERSION.SDK_INT >= 23)) {
                return;
            }
            if (canDrawOverlays(GlobalContext.get())) {
               return;
            }
            Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION",
                    Uri.parse("package:" + GlobalContext.get().getPackageName()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            JNIHelper.logd(TAG+ "grantOverlayPermission NEED_REQUEST_RUNTIME_PERMISSIONS=" + NEED_REQUEST_RUNTIME_PERMISSIONS + ";;REQUEST_RUNTIME_PERMISSIONS_TIPS=" + REQUEST_RUNTIME_PERMISSIONS_TIPS);
            try {
                GlobalContext.get().startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (REQUEST_RUNTIME_PERMISSIONS_TIPS == null) {
                Toast.makeText(GlobalContext.get(), "需要授予显示在其他应用之上的权限", Toast.LENGTH_SHORT).show();
                return;
            }
            if (REQUEST_RUNTIME_PERMISSIONS_TIPS.length() == 0) {
                return;
            }
            Toast.makeText(GlobalContext.get(), REQUEST_RUNTIME_PERMISSIONS_TIPS, Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean canDrawOverlays(Context context) {
        Class<?> threadClazz = Settings.class;
        try {
            Method method = threadClazz.getMethod("canDrawOverlays", Context.class);
            method.setAccessible(true);
            return (Boolean) method.invoke(null, context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
