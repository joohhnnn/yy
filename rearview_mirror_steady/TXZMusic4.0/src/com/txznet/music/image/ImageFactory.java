package com.txznet.music.image;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.txznet.music.image.glide.GlideImageLoader;
import com.txznet.music.image.imageLoader.ImageLoaderUtils;

import java.io.File;


/**
 * Created by telenewbie on 2017/9/8.
 */

public class ImageFactory implements IImageLoader {
    //##创建一个单例类##
    private volatile static ImageFactory singleton;
    private IImageLoader iImageLoader;
    private static final int IMAGE_TYPE = 1;

    private ImageFactory() {
        init(IMAGE_TYPE);
    }


    private void init(int type) {
        if (type == 1) {
            iImageLoader = new GlideImageLoader();
        } else if (type == 2) {
            iImageLoader = new ImageLoaderUtils();
        } else if (type == 3) {
//            iImageLoader = new PicassoImageLoader();//内存占用过高。50M，滑动两下而已
        } else if (type == 4) {
//            iImageLoader = new FrescoImageLoader();
        } else {
            iImageLoader = new EmptyImageLoader();
        }
    }

    public static ImageFactory getInstance() {
        if (singleton == null) {
            synchronized (ImageFactory.class) {
                if (singleton == null) {
                    singleton = new ImageFactory();
                }
            }
        }
        return singleton;
    }

    public int getImageType() {
        return IMAGE_TYPE;
    }

    @Override
    public void setStyle(@ShowStyle int style) {
        iImageLoader.setStyle(style);
    }

    @Override
    public void display(Context context, String url, ImageView iv, int defaultRes) {

        //推送的时候会使用到Context
        iImageLoader.display(context, url, iv, defaultRes);
    }

    @Override
    public void display(Activity activity, String url, ImageView iv, int defaultRes) {
        iImageLoader.display(activity, url, iv, defaultRes);
    }

    @Override
    public void display(FragmentActivity activity, String url, ImageView iv, int defaultRes) {
        iImageLoader.display(activity, url, iv, defaultRes);
    }

    @Override
    public void display(Fragment fragment, String url, ImageView iv, int defaultRes) {
        iImageLoader.display(fragment, url, iv, defaultRes);
    }

    @Override
    public void display(android.support.v4.app.Fragment fragment, String url, ImageView iv, int defaultRes) {
        iImageLoader.display(fragment, url, iv, defaultRes);
    }

    @Override
    public void onLowMemory() {
        iImageLoader.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        iImageLoader.onTrimMemory(level);
    }

    @Override
    public void clearMemory() {
        iImageLoader.clearMemory();
    }

    @Override
    public void clearDiskCache() {
        iImageLoader.clearDiskCache();
    }

    @Override
    public void pauseRequests(Context context) {
        iImageLoader.pauseRequests(context);
    }

    @Override
    public void resumeRequests(Context context) {
        iImageLoader.resumeRequests(context);
    }

    @Override
    public File getDiskImageFile(String url) {
        return iImageLoader.getDiskImageFile(url);
    }

}
