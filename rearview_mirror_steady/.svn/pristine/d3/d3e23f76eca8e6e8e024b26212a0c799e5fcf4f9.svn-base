package com.txznet.music.ui.player;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.txznet.comm.util.StringUtils;
import com.txznet.loader.AppLogic;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.AiActionCreator;
import com.txznet.music.action.FavourActionCreator;
import com.txznet.music.action.LyricActionCreator;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.action.SubscribeActionCreator;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.PlayMode;
import com.txznet.music.helper.AlbumConverts;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.GlideHelper;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.OpPlayModeEvent;
import com.txznet.music.store.LyricStore;
import com.txznet.music.store.PlayInfoStore;
import com.txznet.music.ui.base.BaseFragment;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.Logger;
import com.txznet.music.util.PlayModeUtil;
import com.txznet.music.util.PlaySceneUtils;
import com.txznet.music.util.SourceFromUtils;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.widget.CustomSeekBar;
import com.txznet.music.widget.LoadingView;
import com.txznet.music.widget.LyricView;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.extensions.aac.ViewModelProviders;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

/**
 * 播放页
 *
 * @author zackzhou
 * @date 2018/12/4,17:13
 */
public class PlayerFragment extends BaseFragment {

    @Bind(R.id.bar_music)
    View barMusic;
    @Bind(R.id.bar_radio)
    View barRadio;
    @Bind(R.id.bar_radio_ai)
    View barRadioAi;
    @Bind(R.id.bar_music_ai)
    View barMusicAi;

    @Bind(R.id.ll_ai_title)
    ViewGroup aiTitle;

    @Bind(R.id.flRadioCover)
    ViewGroup flRadioCover;
    @Bind(R.id.ivCover)
    ImageView ivCover;
    @Bind(R.id.ivRadioCover)
    ImageView ivRadioCover;
    @Bind(R.id.sb_player_progress)
    CustomSeekBar sb_player_progress;

    @Bind(R.id.tv_position)
    TextView tvPosition;
    @Bind(R.id.tv_duration)
    TextView tvDuration;


    @Bind(R.id.btn_prev)
    Button btnPrev;
    @Bind(R.id.btn_play_or_pause)
    Button btnPlayOrPause;
    @Bind(R.id.btn_next)
    Button btnNext;
    @Bind(R.id.tv_source_from)
    TextView tvSourceFrom;
    @Bind(R.id.ll_control)
    View llControl;

    @Bind(R.id.cb_favour)
    CheckBox cbFavour;
    @Bind(R.id.cb_music_favour)
    CheckBox cbMusicFavour;
    @Bind(R.id.cb_radio_subscribe)
    CheckBox cbRadioSubscribe;
    @Bind(R.id.cb_subscribe)
    CheckBox cbSubscribe;
    @Bind(R.id.cb_music_lrc)
    CheckBox cbMusicLrc;
    @Bind(R.id.cb_lrc)
    CheckBox cbLrc;
    @Bind(R.id.btn_play_mode)
    Button btnPlayMode;
    @Bind(R.id.img_logo_loading)
    LottieAnimationView imgLogoLoading;
    @Bind(R.id.lyric_loading_view)
    LoadingView lyricLoadingView;
    LyricView mLyricView;

    @Bind(R.id.btn_delete)
    Button btnDelete;

    private PlayInfoStore mPlayInfoStore;
    private LyricStore mLyricStore;
    private String mDuration, mPosition;
    private boolean showLrc = false;
    private boolean reqLrcFinished = false;
    private boolean mIsPlaying = false;

    // 播放模式
    final PlayMode[] MODE_LIST = new PlayMode[]{
            PlayMode.QUEUE_LOOP, PlayMode.RANDOM_PLAY, PlayMode.SINGLE_LOOP
    };

    @Override
    protected int getLayout() {
        return R.layout.player_fragment;
    }

    @Override
    protected void initView(View view) {
        ReportEvent.reportPlayerEnter();

        imgLogoLoading.setImageAssetDelegate(asset -> {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.outWidth = asset.getWidth();
            options.outHeight = asset.getHeight();
            return BitmapFactory.decodeResource(getResources(), R.drawable.img_0_2, options);
        });
        mLyricView = lyricLoadingView.findViewById(R.id.lrcView);

        // 检测轻触
        View.OnTouchListener onTouchListener = new View.OnTouchListener() {
            private int touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
            private boolean hasMove;
            private int mLastX, mLastY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        hasMove = false;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int delayX = mLastX - x;
                        int delayY = mLastY - y;
                        if (Math.abs(delayX) > touchSlop || Math.abs(delayY) > touchSlop) {
                            hasMove = true;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (!hasMove) {
                            notifyLyricClick();
                        }
                        break;
                }
                mLastX = x;
                mLastY = y;
                return false;
            }
        };
        mLyricView.setOnTouchListener(onTouchListener);

        View.OnClickListener lyricClickListener = v -> {
            notifyLyricClick();
        };

        View lyricErrorView = lyricLoadingView.findViewById(R.id.ll_lyric_error);
        if (lyricErrorView != null) {
            lyricErrorView.setOnClickListener(lyricClickListener);
            lyricErrorView.findViewById(R.id.tv_error_retry).setOnClickListener(v -> {
                AppLogic.runOnUiGround(mAutoHideControlTask, 0);
                reqLyricData();
            });
        }
        View lyricEmptyView = lyricLoadingView.findViewById(R.id.ll_lyric_empty);
        if (lyricEmptyView != null) {
            lyricEmptyView.setOnClickListener(lyricClickListener);
        }

        View lyricLoadView = lyricLoadingView.findViewById(R.id.ll_lyric_loading);
        if (lyricLoadView != null) {
            lyricLoadView.setOnClickListener(lyricClickListener);
        }

        sb_player_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private long lastTime = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (sb_player_progress.isDrag() && mPlayInfoStore.getDuration().getValue() != null) {
                    long duration = mPlayInfoStore.getDuration().getValue();
                    long val = (long) (duration * progress * 1f / 100);
                    String postionStr = String.format(Locale.getDefault(), "%02d:%02d", (int) (val / 1000f / 60), (int) (val / 1000f % 60));
                    tvPosition.setText(postionStr);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                sb_player_progress.setDrag(true);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (SystemClock.elapsedRealtime() - lastTime > 500) {
                    lastTime = SystemClock.elapsedRealtime();
                    int progress = seekBar.getProgress();
                    int max = seekBar.getMax();
                    PlayerActionCreator.get().seekTo(Operation.MANUAL, progress * 1f / max);
                }
                sb_player_progress.setDrag(false);
            }
        });
        sb_player_progress.setMax(100);


        refreshPlayMode();

        refreshTitle();
        refreshActionBar();
        refreshCover();
    }

    private Runnable mAutoHideControlTask = () -> {
        if (cbMusicLrc.isChecked() || cbLrc.isChecked()) {
            // 播放中
            if (mIsPlaying) {
                setControlInVisible();
            }

            // 处于错误界面
            View lyricErrorView = lyricLoadingView.findViewById(R.id.ll_lyric_error);
            if (lyricErrorView != null && lyricErrorView.getVisibility() == View.VISIBLE) {
                setControlInVisible();
            }

            // 处于歌词为空界面
            View lyricEmptyView = lyricLoadingView.findViewById(R.id.ll_lyric_empty);
            if (lyricEmptyView != null && lyricEmptyView.getVisibility() == View.VISIBLE) {
                setControlInVisible();
            }

            // 处于歌词加载中界面
            View lyricLoadView = lyricLoadingView.findViewById(R.id.ll_lyric_loading);
            if (lyricLoadView != null && lyricLoadView.getVisibility() == View.VISIBLE) {
                setControlInVisible();
            }
        }
    };

    private void notifyLyricClick() {
        setControlVisible();
        AppLogic.removeUiGroundCallback(mAutoHideControlTask);
        AppLogic.runOnUiGround(mAutoHideControlTask, 5000);
    }

    private void refreshPlayMode() {
        PlayMode playMode = PlayHelper.get().getCurrPlayMode();
        btnPlayMode.setBackgroundResource(PlayModeUtil.getDrawableRes(playMode));
    }

    private void reqLyricData() {
        reqLrcFinished = false;
        lyricLoadingView.showLoading();
        LyricActionCreator.getInstance().getLyric(Operation.MANUAL, mPlayInfoStore.getCurrPlaying().getValue());
    }

    // 播放信息为空
    private void onPlayInfoEmpty() {
        ivRadioCover.setImageResource(R.drawable.player_default_cover_icon_large);
        ivCover.setImageResource(R.drawable.player_default_cover_icon_large);
        imgLogoLoading.setVisibility(View.INVISIBLE);
        imgLogoLoading.cancelAnimation();
        btnPlayOrPause.setVisibility(View.VISIBLE);
        isLoadingNow = false;
    }

    // 播放中
    private void onPlaying() {
        imgLogoLoading.setVisibility(View.INVISIBLE);
        imgLogoLoading.cancelAnimation();
        btnPlayOrPause.setVisibility(View.VISIBLE);
    }

    // 播放缓冲中
    private void onPlayBuffering() {
        imgLogoLoading.setVisibility(View.VISIBLE);
        if (!imgLogoLoading.isAnimating()) {
            imgLogoLoading.playAnimation();
        }
        btnPlayOrPause.setVisibility(View.INVISIBLE);
        if (mPlayInfoStore.getCurrPlaying().getValue() == null) {
            isLoadingNow = true;
        }
    }

    // 播放暂停
    private void onPlayPause() {
        if (isLoadingNow) {
            imgLogoLoading.setVisibility(View.VISIBLE);
            if (!imgLogoLoading.isAnimating()) {
                imgLogoLoading.playAnimation();
            }
            btnPlayOrPause.setVisibility(View.INVISIBLE);
        } else {
            imgLogoLoading.setVisibility(View.INVISIBLE);
            imgLogoLoading.cancelAnimation();
            btnPlayOrPause.setVisibility(View.VISIBLE);
        }
    }

    private boolean isLoadingNow = false;

    private void setControlVisible() {
        if (View.VISIBLE != llControl.getVisibility()) {
            AlphaAnimation showAnim = new AlphaAnimation(0f, 1f);
            showAnim.setDuration(500);
            llControl.startAnimation(showAnim);
            llControl.setVisibility(View.VISIBLE);
        }
    }

    private void setControlInVisible() {
        if (View.GONE != llControl.getVisibility()) {
            AlphaAnimation hideAnim = new AlphaAnimation(1f, 0f);
            hideAnim.setDuration(500);
            llControl.startAnimation(hideAnim);
            llControl.setVisibility(View.GONE);
        }
    }

    private AudioV5 mLastAudioV5;

    @Override
    protected void initData(Bundle savedInstanceState) {
        mPlayInfoStore = ViewModelProviders.of(getActivity()).get(PlayInfoStore.class);

        mLastAudioV5 = mPlayInfoStore.getCurrPlaying().getValue();

        // FIXME: 2019/5/15 订阅的顺序会影响进来时，收到通知的顺序
        mPlayInfoStore.getAlbum().observe(this, album -> {
            Log.d(Constant.LOG_TAG_UI_DEBUG, "getAlbum ->" + album);

            refreshTitle();
            refreshActionBar();
            refreshCover();
            if (album != null) {
                if (!PlaySceneUtils.isMusicScene()) {
                    tvTitle.setText(album.name);
                }
                cbSubscribe.setChecked(album.isSubscribe);
                cbRadioSubscribe.setChecked(album.isSubscribe);
                if (!TextUtils.isEmpty(album.logo)) {
                    GlideHelper.load(PlayerFragment.this, album.logo, R.drawable.player_default_cover_icon_large, ivCover);
                    GlideHelper.load(PlayerFragment.this, album.logo, R.drawable.player_default_cover_icon_large, ivRadioCover);
                } else {
                    ivCover.setImageResource(R.drawable.player_default_cover_icon_large);
                    ivRadioCover.setImageResource(R.drawable.player_default_cover_icon_large);
                }

                if (PlayHelper.get().getCurrAudio() == null) {
                    onPlayBuffering();
                }
            } else {
                if (!PlaySceneUtils.isMusicScene()) {
                    tvTitle.setText(null);
                }
                ivCover.setImageResource(R.drawable.player_default_cover_icon_large);
                ivRadioCover.setImageResource(R.drawable.player_default_cover_icon_large);
            }
        });

        mPlayInfoStore.isPlayingStrict().observe(this, isPlaying -> {
            Log.d(Constant.LOG_TAG_UI_DEBUG, "isPlayingStrict ->" + isPlaying);
            if (isPlaying == null || !isPlaying) {
                onPlayPause();
                mIsPlaying = false;
                btnPlayOrPause.setBackgroundResource(R.drawable.player_play_btn);
            } else {
                onPlaying();
                mIsPlaying = true;
                btnPlayOrPause.setBackgroundResource(R.drawable.player_pause_btn);

                AppLogic.removeUiGroundCallback(mAutoHideControlTask);
                AppLogic.runOnUiGround(mAutoHideControlTask, 5000);
            }
        });


        mPlayInfoStore.isBuffering().observe(this, isBuffering -> {
            Log.d(Constant.LOG_TAG_UI_DEBUG, "isBuffering ->" + isBuffering);
            if (mPlayInfoStore.getCurrPlaying().getValue() != null && isBuffering != null && isBuffering) {
                onPlayBuffering();
            }
        });

        mPlayInfoStore.getCurrPlaying().observe(this, audio -> {
            Log.d(Constant.LOG_TAG_UI_DEBUG, "getCurrPlaying ->" + audio);
            tvTitle.clearFocus();
            tvSubTitle.clearFocus();

            refreshTitle();
            refreshActionBar();
            refreshCover();

            if (audio == null || audio.name == null) {
                onPlayInfoEmpty();
                tvTitle.setText(null);
                tvSubTitle.setText(null);
                tvSourceFrom.setText(null);
                if (lyricLoadingView.getVisibility() == View.VISIBLE) {
                    lyricLoadingView.showEmpty();
                }
                return;
            }
            isLoadingNow = false;
            if (lyricLoadingView.getVisibility() == View.VISIBLE) {
                if (!audio.equals(mLastAudioV5)) {
                    reqLyricData();
                }
                //如果在歌词界面，则重新请求
                //如果不在歌词界面？不请求
                //如果更新的为空？
            }
            if (PlaySceneUtils.isMusicScene() || mPlayInfoStore.getAlbum().getValue() == null) {
                tvTitle.setText(audio.name);
            } else {
                tvTitle.setText(mPlayInfoStore.getAlbum().getValue().name);
            }
            if (!audio.equals(mLastAudioV5)) {
                mLastAudioV5 = audio;
                showLrc = false;
                reqLrcFinished = false;
                mDuration = mPosition = "00:00";
                sb_player_progress.setProgress(0);
                Logger.d("PlayerFragment", "setProgress -> 0");
                tvDuration.setText(mDuration);
                tvPosition.setText(mPosition);
                sb_player_progress.setBufferRange(new ArrayList<>());
            }
            Boolean isPlaying = mPlayInfoStore.isPlayingStrict().getValue();
            if (isPlaying == null || !isPlaying) {
                onPlayBuffering();
            }
            if (PlaySceneUtils.isAiScene()) {
                if (AudioUtils.isSong(audio.sid) || mPlayInfoStore.getAlbum().getValue() == null) {
                    String artist = StringUtils.toString(audio.artist);
                    if (TextUtils.isEmpty(artist)) {
                        artist = Constant.UNKNOWN;
                    }
                    tvSubTitle.setText(audio.name + " - " + artist);
                } else {
                    if (TextUtils.isEmpty(audio.albumName)) {
                        tvSubTitle.setText(audio.name);
                    } else {
                        tvSubTitle.setText(audio.name + " - " + audio.albumName);
                    }
                }
                tvSubTitle.requestFocus();
            } else {
                boolean isMusic;
                if (PlaySceneUtils.isMusicScene() || mPlayInfoStore.getAlbum().getValue() == null) {
                    tvSubTitle.setText(StringUtils.toString(audio.artist));
                    if (TextUtils.isEmpty(tvSubTitle.getText())) {
                        tvSubTitle.setText(Constant.UNKNOWN);
                    }
                    isMusic = true;
                } else {
                    if (Album.ALBUM_TYPE_CFM == mPlayInfoStore.getAlbum().getValue().albumType) {
                        tvSubTitle.setText(mPlayInfoStore.getAlbum().getValue().name + " - " + audio.name);
                    } else {
                        tvSubTitle.setText(audio.name);
                    }
                    if (tvTitle.getText().length() > tvSubTitle.getText().length()) {
                        tvTitle.requestFocus();
                    } else {
                        tvSubTitle.requestFocus();
                    }
                    isMusic = false;
                }

                // 音乐场景，若两者都超出长度，则标题优先度高于副标题，否则超出长度的滚动
                TextPaint titlePaint = new TextPaint();
                titlePaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.base_tv_h1));
                StaticLayout titleLayout = new StaticLayout(tvTitle.getText(), titlePaint, tvTitle.getMaxWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                int titleLine = titleLayout.getLineCount();

                TextPaint subTitlePaint = new TextPaint();
                subTitlePaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.base_tv_h4));
                StaticLayout subTitleLayout = new StaticLayout(tvSubTitle.getText(), subTitlePaint, tvSubTitle.getMaxWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                int subTitleLine = subTitleLayout.getLineCount();

                if (BuildConfig.DEBUG) {
                    Log.d(Constant.LOG_TAG_UI_DEBUG, "titleLine=" + titleLine + ", subTitleLine=" + subTitleLine);
                }


                if (isMusic) {
                    // 主标题优先
                    if (subTitleLine > 1) {
                        if (titleLine > 1) {
                            tvTitle.requestFocus();
                        } else {
                            tvSubTitle.requestFocus();
                        }
                    } else {
                        tvTitle.requestFocus();
                    }
                } else {
                    // 副标题优先
                    if (titleLine > 1) {
                        if (subTitleLine > 1) {
                            tvSubTitle.requestFocus();
                        } else {
                            tvTitle.requestFocus();
                        }
                    } else {
                        tvSubTitle.requestFocus();
                    }
                }
            }
            tvSourceFrom.setText("来源：" + SourceFromUtils.getSourceFrom(audio.sid));
            cbFavour.setChecked(audio.isFavour);
            cbMusicFavour.setChecked(audio.isFavour);

            //AI电台播放页不更新音频封面的显示
            /*if (!TextUtils.isEmpty(audioV5.logo)) {
                GlideHelper.load(this, audioV5.logo, R.drawable.player_default_cover_icon_large, ivCover);
                GlideHelper.load(this, audioV5.logo, R.drawable.player_default_cover_icon_large, ivRadioCover);
            }*/
        });

        mPlayInfoStore.getBuffer().observe(this, buffers -> {
            if (buffers != null) {
                sb_player_progress.setBufferRange(buffers);
            }
        });

        mPlayInfoStore.getPlayMode().observe(this, playMode -> {
            refreshPlayMode();
        });

        mPlayInfoStore.getDurationFormatted().observe(this, duration -> {
            mDuration = duration;
            tvDuration.setText(duration);
        });

        mPlayInfoStore.getPositionFormatted().observe(this, position -> {
            long progress = mPlayInfoStore.getPosition().getValue();
            long duration = mPlayInfoStore.getDuration().getValue();
            if (!sb_player_progress.isDrag()) {
                mPosition = position;
                tvPosition.setText(mPosition);
                sb_player_progress.setProgress((int) ((progress * 1f / duration + 0.005f) * 100));
//                Logger.d("PlayerFragment", "setProgress -> " + progress + "/" + duration);
            }

            if (showLrc) {
                mLyricView.setCurrentTimeMillis(progress);
            }

        });

        mLyricStore = ViewModelProviders.of(this).get(LyricStore.class);
        mLyricStore.getLyricData().observe(this, file -> {
            //文件
            reqLrcFinished = true;
            showLrc = true;
            if (file == null) {
                lyricLoadingView.showEmpty();
            } else {
                lyricLoadingView.showContent();
                mLyricView.setLyricFile(file);
            }
        });
        mLyricStore.getStatus().observe(this, status -> {
            reqLrcFinished = true;
            switch (status) {
                case LYRIC_EMPTY:
                    lyricLoadingView.showEmpty();
                    break;
                case LYRIC_NO_NET:
                    ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
                case LYRIC_ERROR:
                    lyricLoadingView.showError();
                    break;
                default:
                    break;
            }
        });


//        PlayerActionCreator.get().getPlayInfo(Operation.AUTO);
    }

    private void refreshTitle() {
        if (PlaySceneUtils.isAiScene()) {
            aiTitle.setVisibility(View.VISIBLE);
            tvTitle.setVisibility(View.GONE);
            tvSubTitle.setTextColor(Color.WHITE);
        } else {
            aiTitle.setVisibility(View.GONE);
            tvTitle.setVisibility(View.VISIBLE);
            tvSubTitle.setTextColor(getResources().getColor(R.color.base_sub_title_tv_color));
        }
    }

    private void refreshActionBar() {
        if (PlaySceneUtils.isAiScene()) {
            if (PlaySceneUtils.isMusicScene()) {
                barMusicAi.setVisibility(View.VISIBLE);
                barRadioAi.setVisibility(View.INVISIBLE);
            } else {
                cbLrc.setChecked(false);
                cbMusicLrc.setChecked(false);
                barMusicAi.setVisibility(View.INVISIBLE);
                barRadioAi.setVisibility(View.VISIBLE);
            }
            barMusic.setVisibility(View.INVISIBLE);
            barRadio.setVisibility(View.INVISIBLE);
        } else if (PlaySceneUtils.isMusicScene()) {
            barMusic.setVisibility(View.VISIBLE);
            barRadioAi.setVisibility(View.INVISIBLE);
            barMusicAi.setVisibility(View.INVISIBLE);
            barRadio.setVisibility(View.INVISIBLE);
        } else if (PlaySceneUtils.isRadioScene()) {
            barMusic.setVisibility(View.INVISIBLE);
            barRadioAi.setVisibility(View.INVISIBLE);
            barMusicAi.setVisibility(View.INVISIBLE);
            barRadio.setVisibility(View.VISIBLE);
        } else {
            barMusic.setVisibility(View.INVISIBLE);
            barRadioAi.setVisibility(View.VISIBLE);
            barMusicAi.setVisibility(View.INVISIBLE);
            barRadio.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshCover() {
        if (lyricLoadingView.getVisibility() == View.VISIBLE) {
            if (PlaySceneUtils.isMusicScene()) {
                return;
            } else {
                cbLrc.setChecked(false);
                cbMusicLrc.setChecked(false);
            }
        }
        if (PlaySceneUtils.isMusicScene()) {
            ivCover.setVisibility(View.VISIBLE);
            flRadioCover.setVisibility(View.INVISIBLE);
        } else {
            ivCover.setVisibility(View.INVISIBLE);
            flRadioCover.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.btn_prev)
    public void prev(View view) {
        PlayerActionCreator.get().prev(Operation.MANUAL);
    }

    @OnClick(R.id.btn_play_or_pause)
    public void playOrPause(View view) {
        PlayerActionCreator.get().playOrPause(Operation.MANUAL);
    }

    @OnClick(R.id.btn_next)
    public void next(View view) {
        PlayerActionCreator.get().next(Operation.MANUAL);
    }

    @OnClick(R.id.btn_play_mode)
    public void playMode(View view) {
        PlayMode mode = PlayHelper.get().getCurrPlayMode();
        int index = 0;
        for (int i = 0; i < MODE_LIST.length; i++) {
            if (MODE_LIST[i] == mode) {
                index = i;
                break;
            }
        }
        mode = MODE_LIST[++index % MODE_LIST.length];
        PlayHelper.get().setPlayMode(Operation.MANUAL, mode);
        btnPlayMode.setBackgroundResource(PlayModeUtil.getDrawableRes(mode));
        ToastUtils.showShortOnUI("已切换为 " + PlayModeUtil.getName(mode));

        switch (mode) {
            case QUEUE_LOOP:
                ReportEvent.reportPlayModeClick(OpPlayModeEvent.OP_TYPE_QUEUE_LOOP);
                break;
            case RANDOM_PLAY:
                ReportEvent.reportPlayModeClick(OpPlayModeEvent.OP_TYPE_RANDOM_PLAY);
                break;
            case SINGLE_LOOP:
                ReportEvent.reportPlayModeClick(OpPlayModeEvent.OP_TYPE_SINGLE_LOOP);
                break;
        }

    }

    @OnClick({R.id.btn_queue, R.id.btn_radio_queue})
    public void btn_queue(View view) {
        new PlayerQueueFragment().show(getChildFragmentManager(), "playerQueue");
        ReportEvent.reportPlayListClick();
    }

    @OnClick({R.id.cb_favour, R.id.cb_music_favour})
    public void btn_cb_favour(View v) {
        CheckBox cb = (CheckBox) v;
        AudioV5 currAudio = mPlayInfoStore.getCurrPlaying().getValue();
        if (currAudio == null) {
            if (v != null) {
                cb.setChecked(!cb.isChecked());
            }
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_POOR);
            return;
        }
        if (cb.isChecked()) {
            FavourActionCreator.getInstance().favour(Operation.MANUAL, AudioConverts.convertAudio2FavourAudio(currAudio), "player");
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_FAVOUR);
            ReportEvent.reportPlayerFavour(mPlayInfoStore.getCurrPlaying().getValue(), true);
        } else {
            FavourActionCreator.getInstance().unFavour(Operation.MANUAL, AudioConverts.convertAudio2FavourAudio(currAudio), "player");
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_FAVOUR_CANCEL);
            ReportEvent.reportPlayerFavour(mPlayInfoStore.getCurrPlaying().getValue(), false);
        }
    }

    @OnClick({R.id.cb_radio_subscribe, R.id.cb_subscribe})
    public void btn_cb_radio_subscribe(View v) {
        CheckBox cb = (CheckBox) v;
        AudioV5 currAudio = mPlayInfoStore.getCurrPlaying().getValue();
        if (currAudio == null) {
            if (v != null) {
                cb.setChecked(!cb.isChecked());
            }
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_POOR);
            return;
        }
        Album album = mPlayInfoStore.getAlbum().getValue();
        if (album == null) {
            return;
        }
        // AI电台执行订阅，被订阅的是该音频对应的专辑
        if (PlaySceneUtils.isAiScene()) {
            album = new Album();
            album.id = currAudio.albumId;
            album.sid = currAudio.albumSid;
            album.name = currAudio.albumName;
            album.logo = currAudio.logo;
        }
        if (cb.isChecked()) {
            SubscribeActionCreator.getInstance().subscribe(Operation.MANUAL, AlbumConverts.convert2subscribe(album, 0), "player");
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_SUBSCRIBE);
            ReportEvent.reportPlayerSubscribe(album, true);
        } else {
            SubscribeActionCreator.getInstance().unSubscribe(Operation.MANUAL, AlbumConverts.convert2subscribe(album, 0), "player");
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_SUBSCRIBE_CANCEL);
            ReportEvent.reportPlayerSubscribe(album, false);
        }
    }

    @OnClick({R.id.btn_delete, R.id.btn_music_delete})
    public void btn_delete(View view) {
        AudioV5 audioV5 = mPlayInfoStore.getCurrPlaying().getValue();
        if (audioV5 != null) {
            AiActionCreator.get().deleteAi(audioV5);
            ToastUtils.showShortOnUI(Constant.TIP_AI_MODEL_DELETE);
        }
    }

    @OnCheckedChanged({R.id.cb_music_lrc, R.id.cb_lrc})
    public void cb_lrc(CompoundButton view, boolean isChecked) {
        if (isChecked) {
            // ToastUtils.showShortOnUI(Constant.TIP_LYRIC_PAUSE);
            if (mIsPlaying) {
                setControlInVisible();
            }
            lyricLoadingView.setVisibility(View.VISIBLE);
            ivCover.setVisibility(View.INVISIBLE);
            flRadioCover.setVisibility(View.INVISIBLE);
            tvSourceFrom.setVisibility(View.INVISIBLE);
            if (!reqLrcFinished) {
                reqLyricData();
            }
            AudioV5 audioV5 = mPlayInfoStore.getCurrPlaying().getValue();
            if (audioV5 != null) {
                ReportEvent.reportPlayLrcClick(audioV5);
            }
        } else {
            setControlVisible();
            ivCover.setVisibility(View.VISIBLE);
            flRadioCover.setVisibility(View.INVISIBLE);
            tvSourceFrom.setVisibility(View.VISIBLE);
            lyricLoadingView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        if (llControl != null) {
            llControl.clearAnimation();
        }
        if (imgLogoLoading != null) {
            imgLogoLoading.cancelAnimation();
        }
        if (sb_player_progress != null) {
            sb_player_progress.setOnSeekBarChangeListener(null);
        }
        AppLogic.removeUiGroundCallback(mAutoHideControlTask);
        if (mLyricView != null) {
            mLyricView.setOnTouchListener(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onStart() {
        super.onStart();
        resetTitleFocus();
    }

    private void resetTitleFocus() {
        if (tvTitle == null || tvSubTitle == null) {
            return;
        }
        if (tvTitle.getText().length() == 0 || tvSubTitle.getText().length() == 0) {
            return;
        }
        if (mPlayInfoStore == null) {
            return;
        }
        if (PlaySceneUtils.isAiScene()) {
            tvSubTitle.requestFocus();
        } else {
            boolean isMusic;
            if (PlaySceneUtils.isMusicScene() || mPlayInfoStore.getAlbum().getValue() == null) {
                isMusic = true;
            } else {
                isMusic = false;
            }
            // 音乐场景，若两者都超出长度，则标题优先度高于副标题，否则超出长度的滚动
            TextPaint titlePaint = new TextPaint();
            titlePaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.base_tv_h1));
            StaticLayout titleLayout = new StaticLayout(tvTitle.getText(), titlePaint, tvTitle.getMaxWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            int titleLine = titleLayout.getLineCount();

            TextPaint subTitlePaint = new TextPaint();
            subTitlePaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.base_tv_h4));
            StaticLayout subTitleLayout = new StaticLayout(tvSubTitle.getText(), subTitlePaint, tvSubTitle.getMaxWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            int subTitleLine = subTitleLayout.getLineCount();

            if (BuildConfig.DEBUG) {
                Log.d(Constant.LOG_TAG_UI_DEBUG, "titleLine=" + titleLine + ", subTitleLine=" + subTitleLine);
            }
            if (isMusic) {
                // 主标题优先
                if (subTitleLine > 1) {
                    if (titleLine > 1) {
                        tvTitle.requestFocus();
                    } else {
                        tvSubTitle.requestFocus();
                    }
                } else {
                    tvTitle.requestFocus();
                }
            } else {
                // 副标题优先
                if (titleLine > 1) {
                    if (subTitleLine > 1) {
                        tvSubTitle.requestFocus();
                    } else {
                        tvTitle.requestFocus();
                    }
                } else {
                    tvSubTitle.requestFocus();
                }
            }
        }
    }
}

