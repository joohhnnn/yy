package com.txznet.music.albumModule.logic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.Logger;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.GlideApp;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.utils.ThreadUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import jp.wasabeef.glide.transformations.CropTransformation;


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

    private BackgroundManager() {
        ObserverManage.getObserver().addObserver(this);
        mListeners = new ArrayList<>();

        multiTransformation = new MultiTransformation<Bitmap>(
                new CropTransformation(ScreenUtil.getScreenWidth() / 2, ScreenUtil.getScreenHeight() / 2, CropTransformation.CropType.CENTER),
                new ColorFilterTransformation(R.color.bg_blur_filter),
                new BlurTransformation(25));
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
        if (null == mBackGroundBitmap || mBackGroundBitmap.isRecycled()) {
            return BitmapFactory.decodeResource(GlobalContext.get().getResources(), R.drawable.bg);
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
            Bitmap bitmap = BitmapFactory.decodeResource(GlobalContext.get().getResources(), R.drawable.bg);
            notifyBackgroundChanged(bitmap);

            return;
        }

        if (null != mSubmit) {
            mSubmit.cancel(true);
        }
        mSubmit = GlideApp.with(GlobalContext.get()).asBitmap().apply(RequestOptions.bitmapTransform(multiTransformation)).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                Logger.e(TAG, "load failed %s %s", url, null == e ? "null" : e.toString());

                mCurrentUrl = null;
                if (null != mBackGroundBitmap && !mBackGroundBitmap.isRecycled()) {
//                    mBackGroundBitmap.recycle();
                }
                mBackGroundBitmap = null;

                Bitmap bitmap = BitmapFactory.decodeResource(GlobalContext.get().getResources(), R.drawable.bg);
                notifyBackgroundChanged(bitmap);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                Logger.d(TAG, "onResourceReady %s %s", resource.hashCode(), resource.isRecycled());
                if (TextUtils.equals(mPreparedUrl, url)) {
                    mCurrentUrl = mPreparedUrl;
                    Bitmap tempBitmap = mBackGroundBitmap;
                    mBackGroundBitmap = resource;
                    notifyBackgroundChanged(mBackGroundBitmap);

//                    if (null != tempBitmap) {
//                        tempBitmap.recycle();
//                    }
                } else {
//                    resource.recycle();
                }
                return false;
            }
        }).load(url).override(100, 100).submit();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) arg;
            Audio obj = null;
            switch (info.getType()) {
                case InfoMessage.PLAYER_CURRENT_AUDIO:
                    Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
                    loadBitmap(null == currentAlbum ? null : currentAlbum.getLogo());
                    break;
                default:
                    break;
            }
        }
    }

    public interface BackgroundChangedListener {

        void onBackgroundChanged(Bitmap bitmap);

    }

}
