package com.txznet.music.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.PlayerInfo;

import java.util.List;
import java.util.Locale;

/**
 * Created by brainBear on 2017/9/28.
 */

public abstract class BasePlayerView extends CardView {

    protected static final int TYPE_NONE = 0;
    protected static final int TYPE_FAVOR = 1;
    protected static final int TYPE_SUBSCRIBE = 2;

    protected boolean isHighlight = false;
    protected int mType = TYPE_NONE;


    protected PlayerViewOperationListener mListener;

    public BasePlayerView(Context context) {
        super(context);
    }

    public BasePlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BasePlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public abstract void updatePlayInfo(Activity activity, Audio audio, Album album);
//    public abstract void updatePlayInfo(Audio audio, Album album);

    public abstract void updateProgress(long position, long duration);

    public abstract void updatePlayMode(@PlayerInfo.PlayerMode int mode);

    public abstract void updatePlayStatus(@PlayerInfo.PlayerUIStatus int status);

    public abstract void updateBufferProgress(List<LocalBuffer> value);

    public abstract void setFavorVisibility(boolean visibility);

    public abstract void setFavorStatus(boolean isFavor, boolean available);

    public abstract void setSubscribeStatus(boolean isSubscribe, boolean available);

    protected void playNext() {
        if (null != mListener) {
            mListener.OnPlayNext();
        }
    }


    protected void playPrev() {
        if (null != mListener) {
            mListener.OnPlayPrev();
        }
    }


    protected void playOrPause() {
        if (null != mListener) {
            mListener.OnPlayOrPause();
        }
    }


    protected void setImageResource(ImageView view, @DrawableRes int resId) {
        Drawable drawable = getContext().getResources().getDrawable(resId);
        view.setImageDrawable(drawable);
    }


    protected String convertTime(long time) {
        return String.format(Locale.getDefault(), "%02d:%02d", time / 60, time % 60);
    }


    protected void changePlayMode() {
        if (null != mListener) {
            mListener.OnChangePlayMode();
        }
    }

    public void updateTips(String tips) {

    }

    public void forceSetPlayModeToSequence(boolean enable) {
    }


    public boolean isHighlight() {
        return isHighlight;
    }

    public void setHighlight(boolean highlight) {
        isHighlight = highlight;
    }

    public int getType() {
        return mType;
    }

    public void setType(int mType) {
        this.mType = mType;
    }

    public interface PlayerViewOperationListener {

        void OnPlayNext();

        void OnPlayPrev();

        void OnPlayOrPause();

        void OnChangePlayMode();

        void onFavor(boolean cancel);

        void onSubscribe(boolean cancel);


    }

}
