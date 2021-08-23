package com.txznet.comm.ui.resloader;

import com.txznet.comm.util.ReflectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * Created by Honge on 2019/1/3.
 */

public class UiClassLoader extends DexClassLoader {
    private final ClassLoader mHostClassLoader;

    private static Method sLoadClassMethod;
    /**
     * 初始化插件的DexClassLoader的构造函数。插件化框架会调用此函数。
     *
     * @param dexPath            the list of jar/apk files containing classes and
     *                           resources, delimited by {@code File.pathSeparator}, which
     *                           defaults to {@code ":"} on Android
     * @param optimizedDirectory directory where optimized dex files
     *                           should be written; must not be {@code null}
     * @param librarySearchPath  the list of directories containing native
     *                           libraries, delimited by {@code File.pathSeparator}; may be
     *                           {@code null}
     * @param parent             the parent class loader
     */
    public UiClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);

        mHostClassLoader = parent;

        initMethods(mHostClassLoader);
    }

    private static void initMethods(ClassLoader cl) {
        Class<?> clz = cl.getClass();
        if (sLoadClassMethod == null) {
            try {
                sLoadClassMethod = ReflectUtils.getMethod(clz, "loadClass", String.class, Boolean.TYPE);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            if (sLoadClassMethod == null) {
                throw new NoSuchMethodError("loadClass");
            }
        }
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
        // 插件自己的Class。从自己开始一直到BootClassLoader，采用正常的双亲委派模型流程，读到了就直接返回
        Class<?> pc = null;
        ClassNotFoundException cnfException = null;
        try {
            pc = super.loadClass(className, resolve);
            if (pc != null) {
                // 只有开启“详细日志”才会输出，防止“刷屏”现象
                return pc;
            }
        } catch (ClassNotFoundException e) {
            // Do not throw "e" now
            cnfException = e;

            try {
                return loadClassFromHost(className, resolve);
            } catch (ClassNotFoundException e1) {
                // Do not throw "e1" now
                cnfException = e1;
            }
        }

        // At this point we can throw the previous exception
        if (cnfException != null) {
            throw cnfException;
        }
        return null;
    }

    private Class<?> loadClassFromHost(String className, boolean resolve) throws ClassNotFoundException {
        Class<?> c;
        try {
            c = (Class<?>) sLoadClassMethod.invoke(mHostClassLoader, className, resolve);
        } catch (IllegalAccessException e) {
            // Just rethrow
            throw new ClassNotFoundException("Calling the loadClass method failed (IllegalAccessException)", e);
        } catch (InvocationTargetException e) {
            // Just rethrow
            throw new ClassNotFoundException("Calling the loadClass method failed (InvocationTargetException)", e);
        }
        return c;
    }
}
