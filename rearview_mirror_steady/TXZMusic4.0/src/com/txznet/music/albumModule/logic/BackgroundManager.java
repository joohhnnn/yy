package com.txznet.music.albumModule.logic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.renderscript.RSRuntimeException;
import android.text.TextUtils;

import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPoolAdapter;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestFutureTarget;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.Transition;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.GlideApp;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.ThreadUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import jp.wasabeef.glide.transformations.internal.FastBlur;
import jp.wasabeef.glide.transformations.internal.RSBlur;


/**
 * Created by brainBear on 2018/1/30.
 */

public class BackgroundManager implements Observer {

    private static final String TAG = "BackgroundManager:";
    private static BackgroundManager sInstance;
    private final MultiTransformation<Bitmap> multiTransformation;
    private String mPreparedUrl;
    private String mCurrentUrl;
    private Bitmap mBackGroundBitmap;
    private List<BackgroundChangedListener> mListeners;
    private FutureTarget<Bitmap> mSubmit;

    int screenWidth = 0;
    int screenHeight = 0;

//    int scaleSize = 200;

    float scaleFactor = 0.3f;

    int scaleWidth = 200;
    int scaleHeight = 200;

    private BackgroundManager() {
        ObserverManage.getObserver().addObserver(this);
        mListeners = new ArrayList<>();
        screenWidth = ScreenUtil.getScreenWidth();
        screenHeight = ScreenUtil.getScreenHeight();

        scaleWidth = (int) (screenWidth * scaleFactor);
        scaleHeight = (int) (screenHeight * scaleFactor);

        multiTransformation = new MultiTransformation<Bitmap>(
                //详情请看\\192.168.0.200\apk\telenewbie\bug
                new CropTransformation(scaleWidth, scaleHeight, CropTransformation.CropType.CENTER)
                , new BlurTransformation(25, 10)
                , new ColorFilterTransformation(GlobalContext.get().getResources().getColor(R.color.bg_blur_filter))
        );//radius最大25，超出会OOM

//        multiTransformation = new MultiTransformation<Bitmap>(
//                new BlurTransformation(25),
//                new ColorFilterTransformation(R.color.bg_blur_filter)
//
//        );//radius最大25，超出会OOM


    }


    public static BackgroundManager getInstance() {
        if (null == sInstance) {
            synchronized (BackgroundManager.class) {
                if (null == sInstance) {
                    sInstance = new BackgroundManager();
                }
            }
        }
        return sInstance;
    }


    public Bitmap getAlbumBackground() {
        if (!SharedPreferencesUtils.isOpenPersonalizedSkin() || null == mBackGroundBitmap || mBackGroundBitmap.isRecycled()) {
            return getDefaultBitmap();
        }
        return mBackGroundBitmap;
    }


    public void addBackgroundChangedListener(BackgroundChangedListener listener) {
        ThreadUtil.checkMainThread("addBackgroundChangedListener");
        if (null != listener && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }


    public void removeBackgroundChangedListener(BackgroundChangedListener listener) {
        ThreadUtil.checkMainThread("removeBackgroundChangedListener");
        mListeners.remove(listener);
    }


    private void notifyBackgroundChanged(Bitmap bitmap) {
        for (BackgroundChangedListener listener : mListeners) {
            listener.onBackgroundChanged(bitmap);
        }
    }

    private Bitmap getDefaultBitmap() {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inPreferredConfig = Bitmap.Config.RGB_565;
        opts.inSampleSize = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_INSAMPLESIZE,1);//


        Bitmap bitmap = BitmapFactory.decodeResource(GlobalContext.get().getResources(), R.drawable.bg, opts);
        Logger.d(TAG, "inSampleSize:"+opts.inSampleSize +"image:size:" + bitmap.getByteCount());
        return bitmap;
    }

    private void loadBitmap(final String url) {
        if (!TextUtils.isEmpty(url) && TextUtils.equals(url, mPreparedUrl)) {
            return;
        }
        mPreparedUrl = url;
        if (TextUtils.isEmpty(url)) {
            mPreparedUrl = null;
            mCurrentUrl = null;
            if (null != mBackGroundBitmap && !mBackGroundBitmap.isRecycled()) {
//                mBackGroundBitmap.recycle();
                mBackGroundBitmap = null;
            }


            notifyBackgroundChanged(getDefaultBitmap());

            return;
        } else {
            if (null != mBackGroundBitmap && !mBackGroundBitmap.isRecycled()) {
//                mBackGroundBitmap.recycle();
                //mBackGroundBitmap.recycle();
            }
        }

        cleanRequest();

//        ActivityStack.getInstance().currentActivity()

        mSubmit = GlideApp.with(GlobalContext.get()).asBitmap().load(url).apply(RequestOptions.bitmapTransform(multiTransformation))
//                .override(screenWidth, screenHeight)//图片压缩，值越小，压缩越狠，同时效果会有斑点和锯齿（现在是屏幕宽高的一半）
                .into(new RequestFutureTarget<Bitmap>(new Handler(Looper.getMainLooper()), scaleWidth, scaleHeight) {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        Logger.d(TAG, "onResourceReady %s %s ,size = %d", resource.hashCode(), resource.isRecycled(), resource.getByteCount());
                        if (TextUtils.equals(mPreparedUrl, url)) {
                            mCurrentUrl = mPreparedUrl;
                            resource = Bitmap.createScaledBitmap(resource, screenWidth, screenHeight, true);
                            mBackGroundBitmap = resource.copy(Bitmap.Config.RGB_565, false);
                            LogUtil.d(TAG, "w:h," + screenWidth + ":" + screenHeight + "/" + mBackGroundBitmap.getWidth() + ":" + mBackGroundBitmap.getHeight() + ",size = " + mBackGroundBitmap.getByteCount());
                            notifyBackgroundChanged(mBackGroundBitmap);
                        } else {
                        }
                        cleanRequest();
                    }

                    @Override
                    public synchronized void onLoadFailed(Drawable errorDrawable) {
//                Logger.e(TAG, "load failed %s %s", url, null == e ? "null" : e.toString());

                        mCurrentUrl = null;
                        if (null != mBackGroundBitmap && !mBackGroundBitmap.isRecycled()) {
//                    mBackGroundBitmap.recycle();
                        }
                        mBackGroundBitmap = null;

                        notifyBackgroundChanged(getDefaultBitmap());
                        cleanRequest();
                    }
                });
    }

    private void cleanRequest() {
        if (null != mSubmit) {
            mSubmit.cancel(true);
            mSubmit.onDestroy();
            mSubmit = null;
        }
    }


    /**
     * DESC 设置颜色的饱和度  0到1间
     *
     * @return
     */
    public Bitmap getmBackGroundBitmap(float saturability, Bitmap bitmap) {
        Bitmap resource = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        if (saturability > 1 || saturability < 0) {
            return bitmap;
        }
        ColorMatrix saturationMatrix = new ColorMatrix();
        //设置饱和度
        saturationMatrix.reset();
        saturationMatrix.setSaturation(saturability);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColorFilter(new ColorMatrixColorFilter(saturationMatrix));
        Canvas canvas = new Canvas(resource);
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return resource;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) arg;
            Audio obj = null;
            switch (info.getType()) {
                case InfoMessage.PLAYER_CURRENT_AUDIO:
                    if (SharedPreferencesUtils.isOpenPersonalizedSkin()) {
                        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                        loadBitmap(null == currentAlbum ? null : currentAlbum.getLogo());
                    } else {
                        notifyBackgroundChanged(getDefaultBitmap());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void setPreparedUrl(String preparedUrl) {
        mPreparedUrl = preparedUrl;
    }

    public interface BackgroundChangedListener {

        void onBackgroundChanged(Bitmap bitmap);

    }

}
