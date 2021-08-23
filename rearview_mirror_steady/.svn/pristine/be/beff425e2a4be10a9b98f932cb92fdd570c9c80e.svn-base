package com.txznet.launcher.img;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.SystemClock;
import android.widget.ImageView;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.launcher.utils.DimenUtils;
import com.txznet.loader.AppLogic;

import java.io.ByteArrayOutputStream;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by Anudorannador on 2018/3/13.
 * 对第三方图片加载库的封装。避免以后换加载库很麻烦
 */

public class ImgLoader {

    private static final MultiTransformation<Bitmap> multiTransformation = new MultiTransformation<Bitmap>(
            new CenterCrop()
            , new ColorFilterTransformation(Color.parseColor("#4CFFFFFF"))
            , new BlurTransformation(25)
            , new RoundedCorners((int) DimenUtils.dp2px(GlobalContext.get(), 3))
    );

    private ImgLoader() {
    }


    public static void loadImage(Object url, ImageView imageView) {
        GlideApp.with(GlobalContext.get()).load(url).into(imageView);
    }

    public static void loadCircleImage(Object url, ImageView imageview) {
        loadCircleImage(url, imageview, false);
    }

    public static void loadCircleImage(Object url, ImageView imageview, boolean skipCache) {
        if (skipCache) {
            GlideApp.with(GlobalContext.get()).load(url).apply(RequestOptions.bitmapTransform(new CircleCrop())).skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).signature(new ObjectKey(SystemClock.currentThreadTimeMillis())).into(imageview);
        } else {
            GlideApp.with(GlobalContext.get()).load(url).diskCacheStrategy(DiskCacheStrategy.RESOURCE).apply(RequestOptions.bitmapTransform(new CircleCrop())).into(imageview);
        }

    }

    public static void loadRoundedImage(Object url, ImageView imageview, int roundingRadius) {
        GlideApp.with(GlobalContext.get()).load(url).apply(RequestOptions.bitmapTransform(new RoundedCorners(roundingRadius))).into(imageview);
    }

    public static void loadRoundedImage(final Bitmap bitmap, final ImageView imageview, int roundingRadius) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] data = bos.toByteArray();
        GlideApp.with(GlobalContext.get()).load(data).transform(new RoundedCornersTransformation(roundingRadius, 0, RoundedCornersTransformation.CornerType.TOP)).into(imageview);
    }


    public static void loadBlurImage(Object url, ImageView imageView) {
        GlideApp.with(GlobalContext.get()).load(url).diskCacheStrategy(DiskCacheStrategy.ALL).apply(RequestOptions.bitmapTransform(multiTransformation)).into(imageView);
    }


}
