package com.txznet.music.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.favor.FavorHelper;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;

import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by brainBear on 2017/9/29.
 */

public class BarPlayerView extends BasePlayerView implements Observer {

    @Bind(R.id.iv_cover)
    @Nullable
    ImageView ivCover;
    @Bind(R.id.tv_title)
    @Nullable
    TextView tvTitle;
    @Bind(R.id.tv_time)
    @Nullable
    TextView tvTime;
    @Bind(R.id.customSeekBar)
    @Nullable
    CustomSeekBar mSeekBar;
    @Bind(R.id.iv_prev)
    @Nullable
    ImageView ivPrev;
    @Bind(R.id.fl_play)
    @Nullable
    FrameLayout flPlay;
    @Bind(R.id.iv_play)
    @Nullable
    ImageView ivPlay;
    @Bind(R.id.iv_round)
    @Nullable
    ImageView ivRound;
    @Bind(R.id.iv_buffer_btn)
    @Nullable
    ImageView ivBuffer;
    @Bind(R.id.iv_next)
    @Nullable
    ImageView ivNext;
    @Bind(R.id.iv_mode)
    @Nullable
    ImageView ivMode;
    @Bind(R.id.iv_play_list)
    @Nullable
    ImageView ivPlayList;
    @Bind(R.id.iv_favor)
    @Nullable
    ImageView ivFavor;
    @Nullable
    @Bind(R.id.tv_artist)
    TextView tvArtists;

    private RotateAnimation mBufferAnimation;
    private OnEffectiveClickListener mClickListener;

    public BarPlayerView(Context context) {
        super(context);
        init();
    }

    public BarPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BarPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        if (ScreenUtils.isPhonePortrait()) {
            inflate(getContext(), R.layout.layout_bar_player_phone_view, this);
        } else {
            inflate(getContext(), R.layout.layout_bar_player_view, this);
        }
        setBackgroundColor(Color.TRANSPARENT);
        initView();
        initAnimation();
    }


    private void initView() {
        ButterKnife.bind(this);
        mClickListener = new OnEffectiveClickListener() {
            @Override
            public void onEffectiveClick(View v) {
                switch (v.getId()) {
                    case R.id.iv_prev:
                        playPrev();

                        break;
                    case R.id.iv_play:
                        playOrPause();
                        break;
                    case R.id.iv_next:
                        playNext();

                        break;
                    case R.id.iv_mode:
                        changePlayMode();
                        break;
                    case R.id.iv_cover:
                        if (null != mListener && mListener instanceof BarPlayerView.BarPlayerViewOperationListener) {
                            ((BarPlayerView.BarPlayerViewOperationListener) mListener).onClickCover();
                        }
                        break;
                    case R.id.iv_play_list:
                        if (null != mListener && mListener instanceof BarPlayerView.BarPlayerViewOperationListener) {
                            ((BarPlayerView.BarPlayerViewOperationListener) mListener).onClickPlayList();
                        }
                        break;
                    case R.id.iv_favor:
                        if (mListener == null) {
                            break;
                        }
                        if (getType() == TYPE_FAVOR) {
                            mListener.onFavor(isHighlight());
                        } else if (getType() == TYPE_SUBSCRIBE) {
                            mListener.onSubscribe(isHighlight());
                        }
                        break;
                }
            }
        };
        Resources resources = getContext().getResources();

        if (ivPrev != null) {
            ivPrev.setOnClickListener(mClickListener);
            ivPrev.setImageDrawable(resources.getDrawable(R.drawable.fm_player_prev));
        }

        if (ivPlay != null) {
            ivPlay.setOnClickListener(mClickListener);
            ivPlay.setImageDrawable(resources.getDrawable(R.drawable.fm_player_play1));
        }

        if (ivNext != null) {
            ivNext.setOnClickListener(mClickListener);
        }
        if (ivMode != null) {
            ivMode.setOnClickListener(mClickListener);
        }
        if (ivCover != null) {
            ivCover.setOnClickListener(mClickListener);
        }

        if (ivPlayList != null) {
            ivPlayList.setOnClickListener(mClickListener);
            ivPlayList.setImageDrawable(resources.getDrawable(R.drawable.ic_playlist_v41));
        }

        if (ivFavor != null) {
            ivFavor.setOnClickListener(mClickListener);
        }

        if (ivRound != null) {
            ivRound.setImageDrawable(resources.getDrawable(R.drawable.fm_player_loading1));
        }
        if (ivBuffer != null) {
            ivBuffer.setImageDrawable(resources.getDrawable(R.drawable.fm_player_loading1_1));
        }
        if (ivNext != null) {
            ivNext.setImageDrawable(resources.getDrawable(R.drawable.fm_player_next));
        }


        if (mSeekBar != null) {
            mSeekBar.setSeekBarBackgroundColor(resources.getColor(R.color.seekbar_background_color));
            mSeekBar.setSeekBarBufferColor(resources.getColor(R.color.seekbar_buffer_color));
            mSeekBar.setSeekBarProgressColor(resources.getColor(R.color.seekbar_progress_color));

            mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                private long lastTime = 0;
                private long startTime = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mSeekBar.setDrag(true);
                    startTime = seekBar.getProgress();
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (SystemClock.elapsedRealtime() - lastTime > 500) {
                        lastTime = SystemClock.elapsedRealtime();
                        PlayEngineFactory.getEngine().seekTo(EnumState.Operation.manual, seekBar.getProgress());
                        ReportEvent.clickBarDragProgress(startTime, seekBar.getProgress());
                    }
                    mSeekBar.setDrag(false);
                }
            });
        }

        if (tvTitle != null) {
            tvTitle.setSelected(true);
        }
    }


    private void initAnimation() {
        mBufferAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mBufferAnimation.setRepeatMode(Animation.INFINITE);
        mBufferAnimation.setRepeatCount(-1);
        mBufferAnimation.setDuration(3000);
    }


    private CharSequence getTitleAndSubTitle(Audio audio, Album album) {
        SpannableString spannableString;
        String title = "";
        String subTitle = "";

        if (null == audio) {
            title = getContext().getString(R.string.text_no_audio);
        } else if (Utils.isSong(audio.getSid())) {
            title = audio.getName();
            subTitle = String.format(Locale.getDefault(), "%s (来源：%s)",
                    StringUtils.toString(audio.getArrArtistName()), audio.getSourceFrom());
        } else {
            title = audio.getName();
            String albumName = "";
            if (StringUtils.isEmpty(audio.getAlbumName())) {
                albumName = null == album ? "" : album.getName();
            } else {
                albumName = audio.getAlbumName();
            }
            subTitle = String.format(Locale.getDefault(), "%s (来源：%s)",
                    albumName, audio.getSourceFrom());
        }

        spannableString = new SpannableString(String.format(Locale.getDefault(), "%s  %s", title
                , subTitle));
        spannableString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, title.length()
                , Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        if (!TextUtils.isEmpty(subTitle)) {
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#939393"))
                    , title.length() + 1, title.length() + subTitle.length() + 2
                    , Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        return spannableString;
    }

    private String getSubTitle(Audio audio, Album album) {
        if (null == audio) {
            return "";
        }
        if (Utils.isSong(audio.getSid())) {
            return StringUtils.toString(audio.getArrArtistName());
        } else {
            String albumName = "";
            if (StringUtils.isEmpty(audio.getAlbumName())) {
                albumName = null == album ? "" : album.getName();
            } else {
                albumName = audio.getAlbumName();
            }
            return albumName;
        }
    }

    private String getTitle(Audio audio, Album album) {
        String title = "";
        if (null == audio) {
            title = getContext().getString(R.string.text_no_audio);
        } else {
            title = audio.getName();
        }
        return title;
    }


    @Override
    public void updatePlayInfo(Activity activity, Audio audio, Album album) {
        if (null == audio) {
            showEmptyInfo(activity);
            return;
        }
        if (tvArtists != null) {
            updateTitle(getTitle(audio, album), getSubTitle(audio, album));
        } else {
            updateTitle(getTitleAndSubTitle(audio, album));
        }


        String logo = audio.getLogo();
        if (TextUtils.isEmpty(logo) && null != album && !TextUtils.isEmpty(album.getLogo())) {
            logo = album.getLogo();
        }
        updateCoverIV(activity, logo);

        if (null == audio || !Utils.isSong(audio.getSid())) {
            forceSetPlayModeToSequence(true);
        } else {
            forceSetPlayModeToSequence(false);
        }
    }

    public void showEmptyInfo(Activity activity) {
        updateProgress(0, 0);
        updateBufferProgress(null);
        updateCoverIV(activity, "");
        forceSetPlayModeToSequence(true);
        setFavorVisibility(false);
        updateTitle(getContext().getString(R.string.text_no_audio));
    }

    private void updateTitle(CharSequence title) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
    }

    private void updateTitle(String title, String artists) {
        if (tvTitle != null) {
            tvTitle.setText(title);
        }
        if (tvArtists != null) {
            tvArtists.setText(artists);
        }
    }

    private void updateCoverIV(Activity activity, String logo) {
        if (ivCover != null) {
            if (StringUtils.isNotEmpty(logo)) {
                ImageFactory.getInstance().setStyle(IImageLoader.CROP);
                ImageFactory.getInstance().display(activity, logo, ivCover,
                        R.drawable.fm_bottom_playing_default);
            } else {
                ivCover.setImageResource(R.drawable.fm_bottom_playing_default);
            }
        }
    }

    @Override
    public void updateProgress(long position, long duration) {
        if (mSeekBar != null) {

            if (duration > 0 && mSeekBar.getMax() != duration) {
                mSeekBar.setMax((int) duration);
            }
            if (tvTime != null) {
                tvTime.setText(String.format(Locale.getDefault(), "%s/%s", convertTime(position)
                        , convertTime(duration)));
            }
            mSeekBar.setProgress((int) position);
        }
    }

    @Override
    public void updatePlayMode(@PlayerInfo.PlayerMode int mode) {
        if (ivMode == null) {
            return;
        }
        if (PlayInfoManager.getInstance().getCurrentAudio() == null) {
            setImageResource(ivMode, R.drawable.fm_player_sequential_playing_disabled);
            ivMode.setOnClickListener(null);
            return;
        } else {
            ivMode.setOnClickListener(mClickListener);
        }
        switch (mode) {
            case PlayerInfo.PLAYER_MODE_RANDOM:
                setImageResource(ivMode, R.drawable.fm_player_random_play);
                break;
            case PlayerInfo.PLAYER_MODE_SEQUENCE:
                setImageResource(ivMode, R.drawable.fm_player_sequential_playing);
                break;
            case PlayerInfo.PLAYER_MODE_SINGLE_CIRCLE:
                setImageResource(ivMode, R.drawable.fm_player_single_cycle);
                break;

        }
    }

    @Override
    public void updatePlayStatus(@PlayerInfo.PlayerUIStatus int status) {
        switch (status) {
            case PlayerInfo.PLAYER_UI_STATUS_RELEASE:
                clearBufferState();
                updateProgress(0, 0);
                setImageResource(ivPlay, R.drawable.fm_player_play1);
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PAUSE:
                clearBufferState();
                setImageResource(ivPlay, R.drawable.fm_player_play1);
                break;
            case PlayerInfo.PLAYER_UI_STATUS_BUFFER:
                ivPlay.setVisibility(View.INVISIBLE);
                ivRound.setVisibility(View.VISIBLE);
                ivBuffer.setVisibility(View.VISIBLE);
                ivRound.startAnimation(mBufferAnimation);
                break;
            case PlayerInfo.PLAYER_UI_STATUS_PLAYING:
                clearBufferState();
                setImageResource(ivPlay, R.drawable.fm_player_stop1);
                break;
        }
    }

    @Override
    public void updateBufferProgress(List<LocalBuffer> value) {
        if (mSeekBar != null) {
            mSeekBar.setBufferRange(value);
        }
    }

    @Override
    public void setFavorVisibility(boolean visibility) {
        if (ivFavor == null) {
            return;
        }
        //短屏不显示收藏和订阅键
        if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_HOUSHIJING_SHORT) {
            ivFavor.setVisibility(GONE);
        } else if (visibility) {
            ivFavor.setVisibility(View.VISIBLE);
        } else {
            ivFavor.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setFavorStatus(boolean isFavor, boolean available) {
        setType(TYPE_FAVOR);
        setHighlight(isFavor);
        if (ivFavor != null) {
            if (isFavor) {
                ivFavor.setImageResource(R.drawable.ic_favorited);
            } else {
                ivFavor.setImageResource(R.drawable.ic_unfavorite);
            }
        }
    }

    @Override
    public void setSubscribeStatus(boolean isSubscribe, boolean available) {
        setType(TYPE_SUBSCRIBE);
        setHighlight(isSubscribe);
        if (ivFavor != null) {
            if (isSubscribe) {
                ivFavor.setImageResource(R.drawable.ic_subscribe);
            } else {
                ivFavor.setImageResource(R.drawable.ic_unsubscribe);
            }
        }
    }


    private void clearBufferState() {
        if (ivPlay != null) {
            ivPlay.setVisibility(View.VISIBLE);
        }
        if (ivRound != null) {
            ivRound.clearAnimation();
        }
        if (ivRound != null) {
            ivRound.setVisibility(View.GONE);
        }
        if (ivBuffer != null) {
            ivBuffer.setVisibility(View.GONE);
        }
    }

    public void setPlayListEnable(boolean enable) {
        if (ivPlayList == null) {
            return;
        }
        if (enable) {
            ivPlayList.setOnClickListener(mClickListener);
            ivPlayList.setAlpha(1f);
        } else {
            ivPlayList.setOnClickListener(null);
            ivPlayList.setAlpha(0.5f);
        }
    }

    public void setPlayListVisible(boolean visible) {
        if (ivPlayList == null) {
            return;
        }
        if (visible) {
            ivPlayList.setVisibility(VISIBLE);
            ViewGroup.LayoutParams layoutParams = ivPlayList.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            ivPlayList.setLayoutParams(layoutParams);
        } else {
            ivPlayList.setVisibility(INVISIBLE);
            ViewGroup.LayoutParams layoutParams = ivPlayList.getLayoutParams();
            layoutParams.width = 0;
            ivPlayList.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void forceSetPlayModeToSequence(boolean isSet) {
        super.forceSetPlayModeToSequence(isSet);
        if (ivMode == null) {
            return;
        }
        if (isSet) {
            PlayEngineFactory.getEngine().changeMode(EnumState.Operation.manual, PlayerInfo.PLAYER_MODE_SEQUENCE);
            setImageResource(ivMode, R.drawable.fm_player_sequential_playing_disabled);
            ivMode.setOnClickListener(null);
        } else {
            int currentPlayMode = PlayInfoManager.getInstance().getCurrentPlayMode();
            updatePlayMode(currentPlayMode);
            ivMode.setOnClickListener(mClickListener);
        }
    }


    public void setBarPlayerViewOperationListener(BarPlayerView.BarPlayerViewOperationListener listener) {
        this.mListener = listener;
    }


    public interface BarPlayerViewOperationListener extends PlayerViewOperationListener {

        void onClickPlayList();

        void onClickCover();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

    }

    boolean hasObs;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!hasObs) {
            ObserverManage.getObserver().addObserver(this);
            hasObs = true;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (hasObs) {
            ObserverManage.getObserver().deleteObserver(this);
            hasObs = false;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg != null && arg instanceof InfoMessage) {
            if (!InfoMessage.SCREEN_TYPE_CHANGED.equals(((InfoMessage) arg).getType()) || ivFavor == null) {
                return;
            }
            //短屏不显示收藏和订阅键
            if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_HOUSHIJING_SHORT) {
                ivFavor.setVisibility(GONE);
            } else {
                if ((PlayInfoManager.getInstance().getCurrentAudio() != null && FavorHelper.isSupportFavour(PlayInfoManager.getInstance().getCurrentAudio()))
                        || (PlayInfoManager.getInstance().getCurrentAlbum() != null && FavorHelper.isSupportSubscribe(PlayInfoManager.getInstance().getCurrentAlbum()))) {
                    ivFavor.setVisibility(View.VISIBLE);
                } else {
                    ivFavor.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
