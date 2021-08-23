package com.txznet.music.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.bitmap.TransformationUtils;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.GlideApp;
import com.txznet.music.GlideRequest;
import com.txznet.music.GlideRequests;
import com.txznet.music.R;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import jp.wasabeef.glide.transformations.internal.Utils;

/**
 * Glide加载
 *
 * @author zackzhou
 * @date 2019/1/21,17:24
 */

public class GlideHelper {

    private GlideHelper() {

    }

    private static MaskTransformation mMaskTran = new MaskTransformation(R.drawable.home_category_bg);
    private static RoundedCornersTransformation mRoundedCornersTran = new RoundedCornersTransformation(GlobalContext.get().getResources().getDimensionPixelSize(R.dimen.m3), 0, RoundedCornersTransformation.CornerType.ALL);
    private static RoundedCorners mRoundedCorners = new RoundedCorners(GlobalContext.get().getResources().getDimensionPixelOffset(R.dimen.m3));
    private static CenterCrop mCenterCrop = new CenterCrop();

    private static GlideRequests getRequests(Object obj) {
        GlideRequests requests = null;
        if (obj instanceof Fragment) {
            requests = GlideApp.with((Fragment) obj);
        } else if (obj instanceof FragmentActivity) {
            requests = GlideApp.with((FragmentActivity) obj);
        } else if (obj instanceof Activity) {
            requests = GlideApp.with((Activity) obj);
        } else if (obj instanceof Context) {
            requests = GlideApp.with((Context) obj);
        } else if (obj instanceof View) {
            requests = GlideApp.with((View) obj);
        }
        return requests;
    }

    public static void loadWithCorners(@NonNull Object obj, String url, @DrawableRes int res, ImageView iv, boolean needCache) {
        Drawable drawable = DrawablePool.get(res);
        GlideRequest request;
        if (url == null) {
            request = getRequests(obj).load(drawable).transform(mCenterCrop, mRoundedCorners);
        } else {
            request = getRequests(obj).load(url).transform(mCenterCrop, mRoundedCorners)
                    .transform(mCenterCrop, mRoundedCornersTran);
        }
        if (!needCache) {
            request = request.diskCacheStrategy(DiskCacheStrategy.NONE);
        }
        if (drawable != null) {
            request = request.placeholder(drawable).error(drawable);
        }
        request.into(iv);
    }

    public static void loadWithCorners(@NonNull Object obj, String url, @DrawableRes int res, ImageView iv) {
        loadWithCorners(obj, url, res, iv, true);
    }

    public static void loadWithCorners(@NonNull Object obj, @DrawableRes int res, View v) {
        Drawable drawable = DrawablePool.get(res);
        getRequests(obj).load(drawable)
                .transform(mRoundedCornersTran)
                .into(new CustomTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        v.setBackground(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        v.setBackground(null);
                    }
                });
    }

    public static void loadWithMask(@NonNull Object obj, String url, @DrawableRes int res, ImageView iv) {
        Drawable drawable = DrawablePool.get(res);
        getRequests(obj).load(url)
                .transform(mMaskTran, mRoundedCornersTran)
                .placeholder(drawable)
                .error(drawable)
                .into(iv);
    }

    public static void load(@NonNull Object obj, String url, @DrawableRes int res, ImageView iv) {
        Drawable drawable = DrawablePool.get(res);
        GlideRequest request = getRequests(obj).load(url);
        if (drawable != null) {
            request = request.placeholder(drawable).error(drawable);
        }
        request.into(iv);
    }

    public static void clear(Object obj, View... views) {
        for (View view : views) {
            if (view != null) {
                getRequests(obj).clear(view);
            }
        }
    }

    /**
     * 注入RecyclerView，处理滑动
     */
    public static void attachToRecyclerView(Context ctx, RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (ctx instanceof Activity) {
                    if (((Activity) ctx).isDestroyed()) {
                        return;
                    }
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    GlideApp.with(ctx).resumeRequestsRecursive();
                } else {
                    GlideApp.with(ctx).pauseRequestsRecursive();
                }
            }
        });
    }


    /**
     * 注入RecyclerView，处理滑动
     */
    public static void attachToRecyclerView(Fragment frag, RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (frag.isDetached()) {
                    return;
                }
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    GlideApp.with(frag).resumeRequestsRecursive();
                } else {
                    GlideApp.with(frag).pauseRequestsRecursive();
                }
            }
        });
    }

    static class MaskTransformation extends jp.wasabeef.glide.transformations.MaskTransformation {
        int maskId;

        /**
         * @param maskId If you change the mask file, please also rename the mask file, or Glide will get
         *               the cache with the old mask. Because key() return the same values if using the
         *               same make file name. If you have a good idea please tell us, thanks.
         */
        public MaskTransformation(int maskId) {
            super(maskId);
            this.maskId = maskId;
        }

        @Override
        protected Bitmap transform(@NonNull Context context, @NonNull BitmapPool pool,
                                   @NonNull Bitmap toTransform, int outWidth, int outHeight) {
            Bitmap bitmap = TransformationUtils.centerCrop(pool, toTransform, outWidth, outHeight);
            bitmap.setHasAlpha(true);

            Canvas canvas = new Canvas(bitmap);
            Drawable mask = Utils.getMaskDrawable(GlobalContext.get(), maskId);
            mask.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());
            mask.draw(canvas);

            return bitmap;
        }
    }
}
