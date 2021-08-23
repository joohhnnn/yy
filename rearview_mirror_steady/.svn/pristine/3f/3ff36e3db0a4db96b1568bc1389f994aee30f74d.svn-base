package com.txznet.loader;// hack to remove memory leak in JarURLConnectionImpl
// from http://stackoverflow.com/questions/14610350/android-memory-leak-in-apache-harmonys-jarurlconnectionimpl

import android.content.Context;
import android.util.Log;

import com.txznet.music.Constant;
import com.txznet.music.util.Objects;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarFile;

public class JarURLMonitor {
    private static final String TAG = Constant.LOG_TAG_APPLICATION + ":JarURLMonitor";
    private static JarURLMonitor instance;
    private Field jarCacheField;
    public volatile boolean stop;

    private static final long CHECK_INTERVAL = 60 * 1000;


    public static synchronized void start(Context context) {
        if (instance == null) {
            instance = new JarURLMonitor(context);
        }
    }

    public static synchronized void stop() {
        if (instance != null) {
            instance.stop = true;
        }
    }

    private JarURLMonitor(Context context) {
        // get jar cache field
        try {
            String urlStr = "libcore.net.url.JarURLConnectionImpl";
            Class<?> jarURLConnectionImplClass = Class.forName(urlStr);
            if (jarURLConnectionImplClass == null) {
                urlStr = "org.apache.harmony.luni.internal.net.www.protocol.jar.JarURLConnectionImpl";
                jarURLConnectionImplClass = Class.forName(urlStr);
            }

            jarCacheField = jarURLConnectionImplClass.getDeclaredField("jarCache");
            jarCacheField.setAccessible(true);
        } catch (Exception e) {
            // log
        }

        if (jarCacheField != null) {

            // start background thread to check it
            new Thread("JarURLMonitor") {
                @Override
                public void run() {
                    try {
                        while (!stop) {
                            checkJarCache();
                            Thread.sleep(CHECK_INTERVAL);
                        }
                    } catch (Exception e) {
                        // log
                    }
                }
            }.start();
        }
    }

    private void checkJarCache() throws Exception {
        @SuppressWarnings("unchecked") final HashMap<URL, JarFile> jarCache = (HashMap<URL, JarFile>) jarCacheField.get(null);
        if (jarCache.size() > 0) {
            for (final Iterator<Map.Entry<URL, JarFile>> iterator = jarCache.entrySet().iterator(); iterator.hasNext(); ) {
                final Map.Entry<URL, JarFile> e = iterator.next();
                final URL url = e.getKey();
                Log.i(TAG, "Removing static hashmap entry for " + Objects.getObj2String(url));
                if (Objects.getObj2String(url).endsWith(".apk") || Objects.getObj2String(url).endsWith(".jar")) {
                    try {
                        final JarFile jarFile = e.getValue();
                        jarFile.close();
                        iterator.remove();
                    } catch (Exception f) {
                        Log.e(TAG, "Error removing hashmap entry for " + url, f);
                    }
                }
            }
            stop();
        }
    }
}