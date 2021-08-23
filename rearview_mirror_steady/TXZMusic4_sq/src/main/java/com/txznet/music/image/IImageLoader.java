package com.txznet.music.image;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.IntDef;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by telenewbie on 2017/9/8.
 */

public interface IImageLoader {

    int NORMAL=1;
    int CIRCLE=2;
    int BLUR=3;
    int BLUR_FILTER=4;
    int CROP=5;
    int CROP_CIRCLE=6;

    @IntDef({NORMAL,CIRCLE,BLUR,BLUR_FILTER,CROP,CROP_CIRCLE})
    public @interface ShowStyle{

    }

    public void setStyle(@ShowStyle int style);

    /**
     * @param ctx
     * @param url        url
     * @param iv
     * @param defaultRes m默认资源id
     */
    void display(Context context, String url, ImageView iv, int defaultRes);

    void display(Activity activity, String url, ImageView iv, int defaultRes);

    void display(FragmentActivity activity, String url, ImageView iv, int defaultRes);

    void display(android.app.Fragment fragment, String url, ImageView iv, int defaultRes);

    void display(android.support.v4.app.Fragment fragment, String url, ImageView iv, int defaultRes);

    void onLowMemory();

    void onTrimMemory(int level);

    void clearMemory();

    void clearDiskCache();

    void pauseRequests(Context context);

    void resumeRequests(Context context);


    File getDiskImageFile(String url);

}
