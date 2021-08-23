package com.txznet.music.image.glide;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.widget.ImageView;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.loader.GlideApp;
import com.txznet.loader.GlideRequest;
import com.txznet.music.R;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.utils.AttrUtils;

import java.io.File;

import jp.wasabeef.glide.transformations.BitmapTransformation;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
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
            if (width == 0) {
                Logger.e("music:test:width=0", ActivityStack.getInstance().currentActivity());
            }
            return cropRequestOption;
        } else if (showStyle == CROP_CIRCLE) {
            if (cropCircleRequestOption == null && ActivityStack.getInstance().currentActivity() != null) {
                width = (int) AttrUtils.getAttrDimension(ActivityStack.getInstance().currentActivity(), R.attr.album_item_content_size_width, 0);
                LogUtil.logd("music:test:load:width:CROP_CIRCLE:" + width);
                cropCircleRequestOption = RequestOptions.bitmapTransform(new MultiTransformation<Bitmap>(new CenterCrop(), new CircleCrop()));
            }
            if (cropCircleRequestOption == null) {
                Logger.e("music:test:cropCircleRequestOption=null", ActivityStack.getInstance().currentActivity());
                cropCircleRequestOption = new RequestOptions().circleCrop();
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
            cropCircleRequestOption = RequestOptions.bitmapTransform(new MultiTransformation<Bitmap>(new CenterCrop(), new CircleCrop()));
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
        GlideRequest<Bitmap> placeholder = GlideApp.with(context).asBitmap().load(url).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(drawable).thumbnail(0.1f);

        RequestOptions options = getOptions();

        if (options == null) {
            placeholder.into(iv);
        } else {
            placeholder.apply(options).into(iv);
        }

    }

    @Override
    public void display(Activity activity, String url, ImageView iv, int defaultRes) {
        Drawable drawable = null;
        if (defaultRes != 0) {
            Resources resources = activity.getResources();
            drawable = resources.getDrawable(defaultRes);
        }

        GlideRequest<Bitmap> placeholder = GlideApp.with(activity).asBitmap().load(url).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(drawable).thumbnail(0.1f);
//        RequestOptions options = getOptions();
//        if (showStyle == CROP_CIRCLE) {
//            placeholder.into(new BitmapImageViewTarget(iv) {
//                @Override
//                protected void setResource(Bitmap resource) {
//                    RoundedBitmapDrawable circularBitmapDrawable =
//                            RoundedBitmapDrawableFactory.create(activity.getResources(), resource);
//                    circularBitmapDrawable.setCircular(true);
//                    iv.setImageDrawable(circularBitmapDrawable);
//                }
//            });
//        } else {
        RequestOptions options = getOptions();
        if (options == null) {
            placeholder.into(iv);
        } else {
            placeholder.apply(options).into(iv);
        }
//        }
    }

    @Override
    public void display(FragmentActivity activity, String url, ImageView iv, int defaultRes) {
        GlideRequest<Bitmap> placeholder = GlideApp.with(activity).asBitmap().load(url).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(defaultRes).thumbnail(0.1f);
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
        GlideRequest<Bitmap> placeholder = GlideApp.with(fragment).asBitmap().load(url).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(drawable).thumbnail(0.1f);
        RequestOptions options = getOptions();
        if (options == null) {
            placeholder.into(iv);
        } else {
            placeholder.apply(options).into(iv);
        }
    }

    @Override
    public void display(android.support.v4.app.Fragment fragment, String url, ImageView iv, int defaultRes) {
        GlideRequest<Bitmap> placeholder = GlideApp.with(fragment).asBitmap().load(url).diskCacheStrategy(DiskCacheStrategy.ALL).placeholder(defaultRes).thumbnail(0.1f);
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