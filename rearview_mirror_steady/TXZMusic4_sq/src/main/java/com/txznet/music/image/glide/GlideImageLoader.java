package com.txznet.music.image.glide;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.LruCache;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.GlideApp;
import com.txznet.loader.GlideRequest;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.ui.BaseActivity;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;

import java.io.File;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jp.wasabeef.glide.transformations.BitmapTransformation;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropCircleTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;

/**
 * Created by telenewbie on 2017/9/8.
 */

public class GlideImageLoader implements IImageLoader {

    private int showStyle = NORMAL;
    RequestOptions circleRequestOption, blurRequestOption, blurFliterRequestOption, cropRequestOption, cropCircleRequestOption;
    int width;

    private RequestOptions getOptions() {
        BitmapTransformation bitmapTransformation = null;
        if (showStyle == NORMAL) {
            return null;
//            bitmapTransformation = new RoundedCornersTransformation(0, 0);
        } else if (showStyle == CIRCLE) {
            return circleRequestOption;
        } else if (showStyle == BLUR) {
            return blurRequestOption;
        } else if (showStyle == BLUR_FILTER) {
            return blurFliterRequestOption;
        } else if (showStyle == CROP) {
            if (width == 0 && ActivityStack.getInstance().currentActivity() != null) {
                width = (int) AttrUtils.getAttrDimension(ActivityStack.getInstance().currentActivity(), R.attr.album_item_content_size_width, 0);
                LogUtil.logd("music:test:load:width:CROP:" + width);
                cropRequestOption = RequestOptions.bitmapTransform(new CropTransformation(width, width, CropTransformation.CropType.TOP));
            }
            return cropRequestOption;
        } else if (showStyle == CROP_CIRCLE) {
            if (cropCircleRequestOption == null && ActivityStack.getInstance().currentActivity() != null) {
                width = (int) AttrUtils.getAttrDimension(ActivityStack.getInstance().currentActivity(), R.attr.album_item_content_size_width, 0);
                LogUtil.logd("music:test:load:width:CROP_CIRCLE:" + width);
                cropCircleRequestOption = RequestOptions.bitmapTransform(new MultiTransformation<Bitmap>(new CropTransformation(width, width, CropTransformation.CropType.TOP), new CropCircleTransformation()));
            }
            return cropCircleRequestOption;
        }
//        return RequestOptions.bitmapTransform(bitmapTransformation);
        return null;
    }

    public GlideImageLoader() {
        circleRequestOption = RequestOptions.circleCropTransform();
        blurRequestOption = RequestOptions.bitmapTransform(new BlurTransformation(25));
        blurFliterRequestOption = RequestOptions.bitmapTransform(new MultiTransformation<Bitmap>(new CropTransformation(0, 0, CropTransformation.CropType.CENTER), new ColorFilterTransformation(R.color.bg_blur_filter), new BlurTransformation(25)));
        if (ActivityStack.getInstance().currentActivity() != null) {
            width = (int) AttrUtils.getAttrDimension(ActivityStack.getInstance().currentActivity(), R.attr.album_item_content_size_width, 0);
            LogUtil.logd("music:test:load:width:" + width);
            cropRequestOption = RequestOptions.bitmapTransform(new CropTransformation(width, width, CropTransformation.CropType.TOP));
            cropCircleRequestOption = RequestOptions.bitmapTransform(new MultiTransformation<Bitmap>(new CropTransformation(width, width, CropTransformation.CropType.TOP), new CropCircleTransformation()));
        }
    }

    @Override
    public void setStyle(@ShowStyle int style) {
        showStyle = style;
    }

    @Override
    public void display(Context context, String url, ImageView iv, int defaultRes) {
        Drawable drawable = null;
        if (defaultRes != 0) {
            Resources resources = context.getResources();
            drawable = resources.getDrawable(defaultRes);
        }
        GlideRequest<Bitmap> placeholder = GlideApp.with(context).asBitmap().load(url).placeholder(drawable).thumbnail(0.1f);
        RequestOptions options = getOptions();
        if (options == null) {
            placeholder.into(iv);
        } else {
            placeholder.apply(options).into(iv);
        }

    }

    @Override
    public void display(Activity activity, String url, ImageView iv, int defaultRes) {
        GlideRequest<Bitmap> placeholder = GlideApp.with(activity).asBitmap().load(url).placeholder(defaultRes).thumbnail(0.1f);
        RequestOptions options = getOptions();
        if (options == null) {
            placeholder.into(iv);
        } else {
            placeholder.apply(options).into(iv);
        }
    }

    @Override
    public void display(FragmentActivity activity, String url, ImageView iv, int defaultRes) {
        GlideRequest<Bitmap> placeholder = GlideApp.with(activity).asBitmap().load(url).placeholder(defaultRes).thumbnail(0.1f);
        RequestOptions options = getOptions();
        if (options == null) {
            placeholder.into(iv);
        } else {
            placeholder.apply(options).into(iv);
        }
    }

    @Override
    public void display(Fragment fragment, String url, ImageView iv, int defaultRes) {
        Drawable drawable = null;
        if (defaultRes != 0) {
            Resources resources = fragment.getResources();
            drawable = resources.getDrawable(defaultRes);
        }
        GlideRequest<Bitmap> placeholder = GlideApp.with(fragment).asBitmap().load(url).placeholder(drawable).thumbnail(0.1f);
        RequestOptions options = getOptions();
        if (options == null) {
            placeholder.into(iv);
        } else {
            placeholder.apply(options).into(iv);
        }
    }

    @Override
    public void display(android.support.v4.app.Fragment fragment, String url, ImageView iv, int defaultRes) {
        GlideRequest<Bitmap> placeholder = GlideApp.with(fragment).asBitmap().load(url).placeholder(defaultRes).thumbnail(0.1f);
        RequestOptions options = getOptions();
        if (options == null) {
            placeholder.into(iv);
        } else {
            placeholder.apply(options).into(iv);
        }
    }

    @Override
    public void onLowMemory() {
        GlideApp.with(GlobalContext.get()).onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        GlideApp.with(GlobalContext.get()).onTrimMemory(level);
    }

    @Override
    public void clearMemory() {
        GlideApp.get(GlobalContext.get()).clearMemory();
    }

    @Override
    public void clearDiskCache() {
        GlideApp.get(GlobalContext.get()).clearDiskCache();
    }

    @Override
    public void pauseRequests(Context context) {
        GlideApp.with(context).pauseRequests();
    }

    @Override
    public void resumeRequests(Context context) {
        GlideApp.with(context).resumeRequests();
    }

    @Override
    public File getDiskImageFile(String url) {
        return null;
    }

}