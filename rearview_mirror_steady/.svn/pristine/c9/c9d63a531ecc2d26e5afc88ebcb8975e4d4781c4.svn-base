package com.txznet.launcher.ui.base;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ProxyContext extends ContextWrapper {
    private Context mContext;
    private AssetManager mAssetManager = null;
    private Resources mResources = null;
    private LayoutInflater mLayoutInflater = null;
    private Theme mTheme = null;
    private String mPackageName = null;

    // *****************资源ID类型*******************
    public static final String LAYOUT = "layout";
    public static final String ID = "id";
    public static final String DRAWABLE = "drawable";
    public static final String STYLE = "style";
    public static final String STRING = "string";
    public static final String COLOR = "color";
    public static final String DIMEN = "dimen";

    public ProxyContext(Context base) {
        super(base);
        this.mContext = base;
    }

    public void loadResources(String resPath, String resPackageName) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, resPath);
            mAssetManager = assetManager;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Resources superRes = super.getResources();
        mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        mPackageName = resPackageName;
        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getIdentifier(String type, String name) {
        return mResources.getIdentifier(name, type, mPackageName);
    }

    public int getProxyId(int res) {
        try {
            Class clazz = Class.forName(mContext.getPackageName() + ".R$id");
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getInt(mContext) == res) {
                    return getId(field.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    public int getId(String name) {
        return mResources.getIdentifier(name, ID, mPackageName);
    }

    public View getLayout(String name) {
        return mLayoutInflater.inflate(getIdentifier(LAYOUT, name), null);
    }

    public String getString(String name) {
        return mResources.getString(getIdentifier(STRING, name));
    }

    public int getColor(String name) {
        return mResources.getColor(getIdentifier(COLOR, name));
    }

    public Drawable getDrawable(String name) {
        return mResources.getDrawable(getIdentifier(DRAWABLE, name));
    }

    public Drawable getDrawableById(int res) {
        return mResources.getDrawable(getProxyId(res));
    }

    public int getStyle(String name) {
        return getIdentifier(STYLE, name);
    }

    public float getDimen(String name) {
        return mResources.getDimension(getIdentifier(DIMEN, name));
    }

    /**
     * 创建一个当前类的布局加载器，用于专门加载插件资源
     */
    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mLayoutInflater == null) {
                try {
                    Class<?> cls = Class.forName("com.android.internal.policy.PolicyManager");
                    Method m = cls.getMethod("makeNewLayoutInflater", Context.class);
                    //传入当前PluginProxyContext类实例，创建一个布局加载器
                    mLayoutInflater = (LayoutInflater) m.invoke(null, this);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else {
                return mLayoutInflater;
            }
        }
        return super.getSystemService(name);
    }


    @Override
    public AssetManager getAssets() {
        return mAssetManager;
    }

    @Override
    public Resources getResources() {
        return mResources;
    }

    @Override
    public ClassLoader getClassLoader() {
        return mContext.getClassLoader();
    }

    @Override
    public Theme getTheme() {
        if (mTheme == null) {
            mTheme = mResources.newTheme();
            mTheme.applyStyle(android.R.style.Theme_Light, true);
        }
        return mTheme;
    }

    @Override
    public String getPackageName() {
        return mPackageName;
    }
}
