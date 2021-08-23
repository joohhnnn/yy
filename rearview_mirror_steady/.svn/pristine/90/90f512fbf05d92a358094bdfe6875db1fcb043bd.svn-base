package com.txznet.comm.ui.resloader;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Honge on 2019/1/3.
 */

public class UiContext extends ContextThemeWrapper {
    ClassLoader mNewClassLoader = null;
    private Resources mNewResources;
    /**
     * layout缓存：构造器表
     */
    HashMap<String, Constructor<?>> mConstructors = new HashMap<String, Constructor<?>>();

    /**
     * layout缓存：忽略表
     */
    HashSet<String> mIgnores = new HashSet<String>();

    LayoutInflater.Factory mFactory = new LayoutInflater.Factory() {

        @Override
        public View onCreateView(String name, Context context, AttributeSet attrs) {
            return handleCreateView(name, context, attrs);
        }
    };
    private LayoutInflater mInflater;

    public UiContext(Context base, int themeres, ClassLoader cl, Resources r) {
        super(base, themeres);
        mNewClassLoader = cl;
        mNewResources = r;
    }

    @Override
    public ClassLoader getClassLoader() {
        if (mNewClassLoader != null) {
            return mNewClassLoader;
        }
        return super.getClassLoader();
    }

    @Override
    public Resources getResources() {
        if (mNewResources != null) {
            return mNewResources;
        }
        return super.getResources();
    }

    @Override
    public AssetManager getAssets() {
        if (mNewResources != null) {
            return mNewResources.getAssets();
        }
        return super.getAssets();
    }


    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mInflater == null) {
                LayoutInflater inflater = (LayoutInflater) super.getSystemService(name);
                // 新建一个，设置其工厂
                mInflater = inflater.cloneInContext(this);
                mInflater.setFactory(mFactory);
                // 再新建一个，后续可再次设置工厂
                mInflater = mInflater.cloneInContext(this);
            }
            return mInflater;
        }
        return super.getSystemService(name);
    }


    private final View handleCreateView(String name, Context context, AttributeSet attrs) {

        // 构造器缓存
        Constructor<?> construct = mConstructors.get(name);

        // 缓存失败
        if (construct == null) {
            // 找类
            Class<?> c = null;
            boolean found = false;
            do {
                try {
                    c = mNewClassLoader.loadClass(name);
                    if (c == null) {
                        // 没找到，不管
                        break;
                    }
                    if (c == ViewStub.class) {
                        // 系统特殊类，不管
                        break;
                    }
                    if (c.getClassLoader() != mNewClassLoader) {
                        // 不是插件类，不管
                        break;
                    }
                    // 找到
                    found = true;
                } catch (ClassNotFoundException e) {
                    // 失败，不管
                    break;
                }
            } while (false);
            if (!found) {
               mIgnores.add(name);
                return null;
            }
            // 找构造器
            try {
                construct = c.getConstructor(Context.class, AttributeSet.class);
                mConstructors.put(name, construct);
            } catch (Exception e) {
                InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating mobilesafe class " + name, e);
                throw ie;
            }
        }

        // 构造
        try {
            View v = (View) construct.newInstance(context, attrs);
            return v;
        } catch (Exception e) {
            InflateException ie = new InflateException(attrs.getPositionDescription() + ": Error inflating mobilesafe class " + name, e);
            throw ie;
        }
    }
}
