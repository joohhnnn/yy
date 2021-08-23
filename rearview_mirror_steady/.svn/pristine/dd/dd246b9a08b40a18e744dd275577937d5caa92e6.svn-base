package com.txznet.music.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;

import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by brainBear on 2017/9/29.
 */

public class BarPlayerView extends BasePlayerView {


    @Bind(R.id.iv_cover)
    ImageView ivCover;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_time)
    TextView tvTime;
    @Bind(R.id.customSeekBar)
    CustomSeekBar mSeekBar;
    @Bind(R.id.iv_prev)
    ImageView ivPrev;
    @Bind(R.id.fl_play)
    FrameLayout flPlay;
    @Bind(R.id.iv_play)
    ImageView ivPlay;
    @Bind(R.id.iv_round)
    ImageView ivRound;
    @Bind(R.id.iv_buffer_btn)
    ImageView ivBuffer;
    @Bind(R.id.iv_next)
    ImageView ivNext;
    @Bind(R.id.iv_mode)
    ImageView ivMode;
    @Bind(R.id.iv_play_list)
    ImageView ivPlayList;
    @Bind(R.id.iv_favor)
    ImageView ivFavor;

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
        inflate(getContext(), R.layout.layout_bar_player_view, this);
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
                        if (null != mListener && mListener instanceof BarPlayerViewOperationListener) {
                            ((BarPlayerViewOperationListener) mListener).onClickCover();
                        }
                        break;
                    case R.id.iv_play_list:
                        if (null != mListener && mListener instanceof BarPlayerViewOperationListener) {
                            ((BarPlayerViewOperationListener) mListener).onClickPlayList();
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

        ivPrev.setOnClickListener(mClickListener);
        ivPlay.setOnClickListener(mClickListener);
        ivNext.setOnClickListener(mClickListener);
        ivMode.setOnClickListener(mClickListener);
        ivCover.setOnClickListener(mClickListener);
        ivPlayList.setOnClickListener(mClickListener);
        ivFavor.setOnClickListener(mClickListener);

        Resources resources = getContext().getResources();
        ivPrev.setImageDrawable(resources.getDrawable(R.drawable.fm_player_prev));
        ivPlay.setImageDrawable(resources.getDrawable(R.drawable.fm_player_play1));
        ivRound.setImageDrawable(resources.getDrawable(R.drawable.fm_player_loading1));
        ivBuffer.setImageDrawable(resources.getDrawable(R.drawable.fm_player_loading1_1));
        ivNext.setImageDrawable(resources.getDrawable(R.drawable.fm_player_next));

        ivPlayList.setImageDrawable(resources.getDrawable(R.drawable.ic_playlist_v41));

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

        tvTitle.setSelected(true);
    }


    private void initAnimation() {
        mBufferAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mBufferAnimation.setRepeatMode(Animation.INFINITE);
        mBufferAnimation.setRepeatCount(-1);
        mBufferAnimation.setDuration(3000);
    }


    private CharSequence getTitle(Audio audio, Album album) {
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
            subTitle = String.format(Locale.getDefault(), "%s (来源：%s)",
                    (null == album ? "" : album.getName()), audio.getSourceFrom());
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


    @Override
    public void updatePlayInfo(Audio audio, Album album) {
        tvTitle.setText(getTitle(audio, album));

        if (null == audio) {
            return;
        }

        String logo = audio.getLogo();
        if (TextUtils.isEmpty(logo) && null != album && !TextUtils.isEmpty(album.getLogo())) {
            logo = album.getLogo();
        }
        ImageFactory.getInstance().setStyle(IImageLoader.CROP);
        ImageFactory.getInstance().display(getContext(), logo, ivCover,
                R.drawable.fm_bottom_playing_default);

        if (null == audio || !Utils.isSong(audio.getSid())) {
            forceSetPlayModeToSequence(true);
        } else {
            forceSetPlayModeToSequence(false);
        }
    }

    @Override
    public void updateProgress(long position, long duration) {
        if (duration > 0 && mSeekBar.getMax() != duration) {
            mSeekBar.setMax((int) duration);
        }
        tvTime.setText(String.format(Locale.getDefault(), "%s/%s", convertTime(position)
                , convertTime(duration)));
        mSeekBar.setProgress((int) position);
    }

    @Override
    public void updatePlayMode(@PlayerInfo.PlayerMode int mode) {
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
        mSeekBar.setBufferRange(value);
    }

    @Override
    public void setFavorVisibility(boolean visibility) {
        if (visibility) {
            ivFavor.setVisibility(View.VISIBLE);
        } else {
            ivFavor.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setFavorStatus(boolean isFavor, boolean available) {
        setType(TYPE_FAVOR);
        setHighlight(isFavor);
        if (isFavor) {
            ivFavor.setImageResource(R.drawable.ic_favorited);
        } else {
            ivFavor.setImageResource(R.drawable.ic_unfavorite);
        }
    }

    @Override
    public void setSubscribeStatus(boolean isSubscribe, boolean available) {
        setType(TYPE_SUBSCRIBE);
        setHighlight(isSubscribe);
        if (isSubscribe) {
            ivFavor.setImageResource(R.drawable.ic_subscribe);
        } else {
            ivFavor.setImageResource(R.drawable.ic_unsubscribe);
        }
    }


    private void clearBufferState() {
        ivPlay.setVisibility(View.VISIBLE);
        ivRound.clearAnimation();
        ivRound.setVisibility(View.GONE);
        ivBuffer.setVisibility(View.GONE);
    }

    public void setPlayListEnable(boolean enable) {
        if (enable) {
            ivPlayList.setOnClickListener(mClickListener);
            ivPlayList.setAlpha(1f);
        } else {
            ivPlayList.setOnClickListener(null);
            ivPlayList.setAlpha(0.5f);
        }
    }

    @Override
    public void forceSetPlayModeToSequence(boolean isSet) {
        super.forceSetPlayModeToSequence(isSet);
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


    public void setBarPlayerViewOperationListener(BarPlayerViewOperationListener listener) {
        this.mListener = listener;
    }

    public interface BarPlayerViewOperationListener extends PlayerViewOperationListener {

        void onClickPlayList();

        void onClickCover();
    }
}
