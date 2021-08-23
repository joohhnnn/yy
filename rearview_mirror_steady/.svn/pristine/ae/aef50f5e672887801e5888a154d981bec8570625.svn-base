package com.txznet.music.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.percent.PercentFrameLayout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
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
import com.txznet.comm.remote.util.LogUtil;
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
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;

import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by brainBear on 2017/9/28.
 */

public class CoverPlayerView extends BasePlayerView implements Observer {


    private static final String TAG = "Music:CoverPlayerView:";
    private ImageView ivCover, ivConverCd;
    private TextView tvName;
    private TextView tvArtist;
    private TextView tvCurrentTime;
    private TextView tvSumTime;
    private CustomSeekBar mSeekBar;
    private ImageView ivPrev;
    private FrameLayout flPlay;
    private ImageView ivPlay;
    private ImageView ivRound;
    private ImageView ivBuffer;
    private ImageView ivNext;
    private ImageView ivMode;
    private ImageView ivPlayList;
    private RotateAnimation mBufferAnimation;
    private OnEffectiveClickListener mClickListener;
    private TextView tvFrom;
    private ImageView ivFavor;
    private PercentFrameLayout pflCover;

    public CoverPlayerView(Context context) {
        super(context);
        init();
    }

    public CoverPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CoverPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initView();
        initAnimation();

    }

    private void initAnimation() {
        mBufferAnimation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mBufferAnimation.setRepeatMode(Animation.INFINITE);
        mBufferAnimation.setRepeatCount(-1);
        mBufferAnimation.setDuration(3000);
    }


    private void initView() {
        if (ScreenUtils.isPhonePortrait()) {
            inflate(getContext(), R.layout.layout_cover_player_phone_portrait_view, this);
            //新增按钮（播放列表）
            ivPlayList = (ImageView) findViewById(R.id.iv_play_list);
        } else {
            inflate(getContext(), R.layout.layout_cover_player_view, this);
        }

        ivCover = (ImageView) findViewById(R.id.iv_cover);
        pflCover = (PercentFrameLayout) findViewById(R.id.rl_cover);
        ivConverCd = (ImageView) findViewById(R.id.iv_conver_cd);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvArtist = (TextView) findViewById(R.id.tv_artist);
        tvCurrentTime = (TextView) findViewById(R.id.tv_current_time);
        tvSumTime = (TextView) findViewById(R.id.tv_sum_time);
        mSeekBar = (CustomSeekBar) findViewById(R.id.customSeekBar);
        ivPrev = (ImageView) findViewById(R.id.iv_prev);
        flPlay = (FrameLayout) findViewById(R.id.fl_play);
        ivPlay = (ImageView) findViewById(R.id.iv_play);
        ivRound = (ImageView) findViewById(R.id.iv_round);
        ivBuffer = (ImageView) findViewById(R.id.iv_buffer_btn);
        ivNext = (ImageView) findViewById(R.id.iv_next);
        ivMode = (ImageView) findViewById(R.id.iv_mode);
        tvFrom = (TextView) findViewById(R.id.tv_from);
        ivFavor = (ImageView) findViewById(R.id.iv_favor);

        tvName.setSelected(true);

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
                    case R.id.iv_play_list:
                        if (null != mListener && mListener instanceof BarPlayerView.BarPlayerViewOperationListener) {
                            ((BarPlayerView.BarPlayerViewOperationListener) mListener).onClickPlayList();
                        }
                        break;
                    case R.id.iv_mode:
                        changePlayMode();
                        break;
                    case R.id.iv_favor:
                        if (getType() == TYPE_FAVOR) {
                            mListener.onFavor(isHighlight());
                        }
                        break;
                    case R.id.rl_cover:
                        //上报:
                        ReportEvent.clickPlayerPageCoverBtn();
                        break;
                }
            }
        };

        ivPrev.setOnClickListener(mClickListener);
        ivPlay.setOnClickListener(mClickListener);
        ivNext.setOnClickListener(mClickListener);
        ivMode.setOnClickListener(mClickListener);
        ivFavor.setOnClickListener(mClickListener);
        pflCover.setOnClickListener(mClickListener);
        if (ivPlayList != null) {
            ivPlayList.setOnClickListener(mClickListener);
        }
        Resources resources = getContext().getResources();
        ivPrev.setImageDrawable(resources.getDrawable(R.drawable.fm_player_prev));
        ivPlay.setImageDrawable(resources.getDrawable(R.drawable.fm_player_play1));
        ivRound.setImageDrawable(resources.getDrawable(R.drawable.fm_player_loading1));
        ivBuffer.setImageDrawable(resources.getDrawable(R.drawable.fm_player_loading1_1));
        ivNext.setImageDrawable(resources.getDrawable(R.drawable.fm_player_next));
        ivConverCd.setImageDrawable(resources.getDrawable(R.drawable.ic_play_cd_v42));

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
                    ReportEvent.clickPlayerPageDragProgress(startTime, seekBar.getProgress());
                }
                mSeekBar.setDrag(false);
            }
        });
    }

    private CharSequence getTitle(Audio audio, Album album) {
        SpannableString spannableString;
        String title = "";
        String subTitle = "";

        if (null == audio) {
            title = getContext().getString(R.string.text_no_audio);
        } else if (Utils.isSong(audio.getSid())) {
            title = audio.getName();
            subTitle = StringUtils.toString(audio.getArrArtistName());
        } else {
            title = audio.getName();
            subTitle = null == album ? "" : album.getName();
        }

        spannableString = new SpannableString(String.format(Locale.getDefault(), "%s  %s", title
                , subTitle));
        spannableString.setSpan(new ForegroundColorSpan(Color.WHITE), 0, title.length()
                , Spanned.SPAN_INCLUSIVE_INCLUSIVE);

        if (!TextUtils.isEmpty(subTitle)) {
            spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#939393"))
                    , title.length() + 1, title.length() + subTitle.length() + 2
                    , Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannableString.setSpan(new AbsoluteSizeSpan((int) AttrUtils.getAttrDimension(getContext(), R.attr.text_size_h3, 0))
                    , title.length() + 1, title.length() + subTitle.length() + 2
                    , Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        return spannableString;
    }

    @Override
    public void updatePlayInfo(Activity activity, Audio audio, Album album) {
        if (null == audio) {
            showEmptyInfo();
            return;
        }

        if (ScreenUtils.isPhonePortrait()) {
            tvArtist.setText(getSubTitle(audio, album));
        } else {
            LogUtil.d("LayoutParamsDemo", "this is not null");
            tvArtist.setText(StringUtils.toString(audio.getArrArtistName()));
        }
        tvName.setText(audio.getName());
        tvFrom.setVisibility(VISIBLE);
        tvFrom.setText(String.format(Locale.getDefault(), "来源：%s", audio.getSourceFrom()));


        String logo = audio.getLogo();
        if (TextUtils.isEmpty(logo) && null != album && !TextUtils.isEmpty(album.getLogo())) {
            logo = album.getLogo();
        }
        ImageFactory.getInstance().setStyle(IImageLoader.NORMAL);
        ImageFactory.getInstance().display(activity, logo, ivCover, 0);

        if (null == audio || !Utils.isSong(audio.getSid())) {
            forceSetPlayModeToSequence(true);
        } else {
            forceSetPlayModeToSequence(false);
        }
    }

    private void showEmptyInfo() {
        tvName.setText("音频名称");
        tvArtist.setText("艺术家");
        tvFrom.setVisibility(INVISIBLE);
        updateProgress(0, 0);
    }

    @Override
    public void updateProgress(long position, long duration) {
        if (duration > 0 && mSeekBar.getMax() != duration) {
            mSeekBar.setMax((int) duration);
        }
        if (position == 0 && duration == 0) {
            mSeekBar.setDrag(false);
        }
        mSeekBar.setProgress((int) position);
        tvCurrentTime.setText(convertTime(position));
        tvSumTime.setText(convertTime(duration));
    }

    @Override
    public void updatePlayMode(@PlayerInfo.PlayerMode int mode) {
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
            ivFavor.setVisibility(View.GONE);
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

    }

    private void clearBufferState() {
        ivPlay.setVisibility(View.VISIBLE);
        ivRound.clearAnimation();
        ivRound.setVisibility(View.GONE);
        ivBuffer.setVisibility(View.GONE);
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

    public void setBarPlayerViewOperationListener(BarPlayerView.BarPlayerViewOperationListener listener) {
        this.mListener = listener;
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
                if ((PlayInfoManager.getInstance().getCurrentAudio() != null && FavorHelper.isSupportFavour(PlayInfoManager.getInstance().getCurrentAudio()))) {
                    ivFavor.setVisibility(View.VISIBLE);
                } else {
                    ivFavor.setVisibility(View.INVISIBLE);
                }
            }
        }
    }
}
