package com.txznet.music.image.imageLoader;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.utils.StringUtils;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * Created by ASUS User on 2016/9/28.
 */
public class ImageLoaderUtils implements IImageLoader {


    public ImageLoaderUtils() {
        init(GlobalContext.get());
    }


    public void init(Context context) {
        File discCacheDir = StorageUtils.getOwnCacheDirectory(context,
                "/txz/cache/images");
        int memClass = ((android.app.ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
        memClass = memClass > 32 ? 32 : memClass;
        // 使用可用内存的1/8作为图片缓存
        final int cacheSize = 1024 * 1024 * memClass / 16;
        LogUtil.logd("music:test:size:" + memClass + "," + cacheSize);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                context).memoryCacheExtraOptions(40, 40)
                .taskExecutor(Executors.newFixedThreadPool(5))
                .memoryCache(new LruMemoryCache(cacheSize))
                .diskCache(new UnlimitedDiskCache(discCacheDir))
                // .writeDebugLogs()// 输出Debug信息，释放版本的时候，不需要这句
                .build();
        ImageLoader.getInstance().init(config);
    }

    /**
     * 显示图片
     *
     * @param url        图片链接
     * @param view       要显示图片的ImageView
     * @param defaultRes 未下载完或下载出错的图片
     */
    public void displayImage(String url, ImageView view, int defaultRes) {
        ImageLoader.getInstance().displayImage(url, view, getDefaultOptions(defaultRes, defaultRes, 0));
    }

    public void displayImage(String url, ImageView view, int defaultRes, int roundNum) {
        ImageLoader.getInstance().displayImage(url, view, getDefaultOptions(defaultRes, defaultRes, roundNum));
    }

    public static String getDiscCacheImage(String uri) {//这里的uri一般就是图片网址

        File file = ImageLoader.getInstance().getDiskCache().get(uri);

        if (file != null) {
            return file.getAbsolutePath();
        }
        return null;
    }

    private DisplayImageOptions getDefaultOptions(int resId, int failResId, int roundNum) {
        return new DisplayImageOptions.Builder()
                .showImageOnFail(failResId)
                .showImageForEmptyUri(failResId)
                .showImageOnLoading(resId)
                .displayer(new RoundedBitmapDisplayer(roundNum))
                    /*
                     * EXACTLY :图像将完全按比例缩小的目标大小
					 * EXACTLY_STRETCHED:图片会缩放到目标大小完全
					 * IN_SAMPLE_INT:图像将被二次采样的整数倍
					 * IN_SAMPLE_POWER_OF_2:图片将降低2倍，直到下一减少步骤，使图像更小的目标大小
					 * NONE:图片不会调整
					 */
                // .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .cacheInMemory(true).cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.ARGB_4444)
                .build();
    }

    public void loadImage(String url, final ImageLoadingListener listener) {
        ImageLoader.getInstance().loadImage(url, new com.nostra13.universalimageloader.core.listener.ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String s, View view) {
                if (listener != null) {
                    listener.onLoadingStarted(s, view);
                }
            }

            @Override
            public void onLoadingFailed(String s, View view, FailReason failReason) {
                if (listener != null) {
                    listener.onLoadingFailed(s, view, (failReason == null || failReason.getCause() == null) ? null : failReason.getCause().toString());
                }
            }

            @Override
            public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                if (listener != null) {
                    listener.onLoadingComplete(s, view, bitmap);
                }
            }

            @Override
            public void onLoadingCancelled(String s, View view) {
                if (listener != null) {
                    listener.onLoadingCancelled(s, view);
                }
            }
        });
    }

    @Override
    public void setStyle(@ShowStyle int style) {

    }

    @Override
    public void display(Context context, String url, ImageView iv, int defaultRes) {
        displayImage(url, iv, defaultRes);
    }

    @Override
    public void display(Activity activity, String url, ImageView iv, int defaultRes) {
        displayImage(url, iv, defaultRes);
    }

    @Override
    public void display(FragmentActivity activity, String url, ImageView iv, int defaultRes) {
        displayImage(url, iv, defaultRes);
    }

    @Override
    public void display(Fragment fragment, String url, ImageView iv, int defaultRes) {
        displayImage(url, iv, defaultRes);
    }

    @Override
    public void display(android.support.v4.app.Fragment fragment, String url, ImageView iv, int defaultRes) {
        displayImage(url, iv, defaultRes);
    }

    @Override
    public void onLowMemory() {

    }

    @Override
    public void onTrimMemory(int level) {

    }

    @Override
    public void clearMemory() {
        ImageLoader.getInstance().clearMemoryCache();
    }

    @Override
    public void clearDiskCache() {
        ImageLoader.getInstance().clearDiskCache();
    }

    @Override
    public void pauseRequests(Context context) {
        ImageLoader.getInstance().pause();
    }

    @Override
    public void resumeRequests(Context context) {
        ImageLoader.getInstance().resume();
    }

    @Override
    public File getDiskImageFile(String url) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }

        return ImageLoader.getInstance().getDiskCache().get(url);
    }


    public interface ImageLoadingListener {
        void onLoadingStarted(String s, View view);

        void onLoadingFailed(String s, View view, String var3);

        void onLoadingComplete(String s, View view, Bitmap var3);

        void onLoadingCancelled(String s, View view);
    }


}
