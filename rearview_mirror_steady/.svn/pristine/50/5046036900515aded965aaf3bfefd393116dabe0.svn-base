package com.txznet.music.net;

import android.support.annotation.NonNull;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.playerModule.logic.PlayerCallbackManager;
import com.txznet.music.utils.JsonHelper;
import com.txznet.txz.util.MD5Util;

import junit.framework.Assert;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * 可以提供缓存的网络请求
 * 如果缓存有则使用缓存的数据进行回调
 * 如果没有缓存则从网络上加载新数据并回调
 * Created by telenewbie on 2017/10/26.
 */

public class NetCacheManager {
    protected static String TAG = "music:NetCacheManager:";
    private final static int CACHE_INIT = 0;
    private final static int CACHE_CACHE = 1;//缓存状态中获取过
    private final static int CACHE_NET = 2;//网状态中获取过

    private File baseDir = null;

    //##创建一个单例类##
    private volatile static NetCacheManager singleton;

    private NetCacheManager() {
        baseDir = GlobalContext.get().getCacheDir();
    }

    public static NetCacheManager getInstance() {
        if (singleton == null) {
            synchronized (NetCacheManager.class) {
                if (singleton == null) {
                    singleton = new NetCacheManager();
                }
            }
        }
        return singleton;
    }

    public int requestCache(String url, Object requestParam, boolean fromCache, RequestCallBack requestCallBack) {
        Assert.assertNotNull(requestParam);
        //先从缓存文件(MD5)里面获取
        final File netCacheFile = getCacheFileName(url, requestParam);
        int i = CACHE_INIT;
        if (netCacheFile.exists() && fromCache) {
            String result = readFromFile(netCacheFile);
            if (requestCallBack != null) {
                Object response = requestCallBack.getResponse(url, result);
                if (response != null) {
                    requestCallBack.onResponse(response);
                    i = CACHE_CACHE;
                }
            }
        }

        if (i == CACHE_CACHE) {
            return NetManager.getInstance().sendRequestToCore(url, requestParam, new RequestCallBack<String>(String.class) {
                @Override
                public void onError(String cmd, Error error) {

                }

                @Override
                public void onResponse(String data) {
                    LogUtil.logd(TAG + "just save data from net");
                    saveDataToFile(netCacheFile, data);

                }
            });
        } else {
            i = CACHE_NET;
            return NetManager.getInstance().sendRequestToCore(url, requestParam, new AdapterRequestCallback(requestCallBack) {
                @Override
                public void adapterDoing(String adapterThing) {
                    LogUtil.logd(TAG + "save data from net and show UI");
                    saveDataToFile(netCacheFile, adapterThing);
                }
            });
        }


        //在从网络上获取

    }

    public int requestCache(String url, Object requestParam, RequestCallBack requestCallBack) {
        return requestCache(url, requestParam, false, requestCallBack);
    }

    @NonNull
    private String readFromFile(File netCacheFile) {
        LogUtil.logd(TAG + "readFromFile--" + netCacheFile.getAbsoluteFile());
        String result = "";
        try {
            FileInputStream fileInputStream = new FileInputStream(netCacheFile);
            int size = fileInputStream.available();
            byte[] var6 = new byte[size];
            fileInputStream.read(var6);
            result = this.getString(var6, "UTF-8");
            fileInputStream.close();
        } catch (Exception var7) {
            var7.printStackTrace();
        }
        return result;
    }

    private void saveDataToFile(File file, String data) {
        LogUtil.logd(TAG + "saveDataToFile--" + file.getAbsoluteFile());
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(data);
            bufferedWriter.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getString(byte[] var1, String var2) {
        return this.getString(var1, 0, var1.length, var2);
    }

    private String getString(byte[] var1, int var2, int var3, String var4) {
        try {
            return new String(var1, var2, var3, var4);
        } catch (UnsupportedEncodingException var6) {
            return new String(var1, var2, var3);
        }
    }

    private File getCacheFileName(String url, Object requestParam) {
        String md5Str = MD5Util.generateMD5(url + "_" + requestParam.toString());
        return new File(baseDir, md5Str);
    }

    public  void  deleteCacheFile(String url, Object requestParam){
        File cacheFileName = NetCacheManager.getInstance().getCacheFileName(url, requestParam);
        if (cacheFileName.exists()) {
            cacheFileName.delete();
        }
    }

}
