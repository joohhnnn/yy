package com.txznet.launcher.module.music;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.launcher.R;
import com.txznet.launcher.domain.music.MusicManager;
import com.txznet.launcher.domain.music.bean.PlayInfo;
import com.txznet.launcher.img.ImgLoader;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.widget.CircleProgressView;
import com.txznet.launcher.widget.RandomWave;
import com.txznet.loader.AppLogic;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by brainBear on 2018/2/5.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class MusicModule extends BaseModule implements MusicContract.View {

    private static final String TAG = "MusicModule:";

    @Nullable
    @Bind(R.id.tv_music_title)
    TextView tvMusicTitle;

    @Nullable
    @Bind(R.id.iv_cover)
    ImageView ivCover;
    @Nullable
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Nullable
    @Bind(R.id.tv_artist)
    TextView tvArtist;
    @Nullable
    @Bind(R.id.btn_next)
    Button btnNext;
    @Nullable
    @Bind(R.id.btn_prev)
    Button btnPrev;
    @Nullable
    @Bind(R.id.btn_play)
    Button btnPlay;
    @Nullable
    @Bind(R.id.bg)
    ImageView ivBg;
    @Nullable
    @Bind(R.id.pb_progress)
    CircleProgressView mProgress;
    @Bind(R.id.wave_anim)
    @Nullable
    RandomWave waveAnim;

    @Bind(R.id.iv_sign)
    @Nullable
    ImageView ivSign;

    @Bind(R.id.tv_loading)
    @Nullable
    TextView tvLoading;

    private View rootView;

    private int mCurrentStatus;
    private MusicPresenter mPresenter;


    private long mCurrProgress; // ??????????????????
    private long mLastSyncProgress; // ???????????????????????????

    private Runnable mCheckIsBufferingTask = new Runnable() { // ???????????????
        @Override
        public void run() {
            if (mLastSyncProgress == mCurrProgress) {
                setPlayStatusLoading();
            }
            mLastSyncProgress = mCurrProgress;
            AppLogic.removeUiGroundCallback(mCheckIsBufferingTask);
            AppLogic.runOnUiGround(mCheckIsBufferingTask, 2000);
        }
    };

    @Override
    public void onCreate(String data) {
        mPresenter = new MusicPresenter(this);
        mPresenter.attach();
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        rootView = null;

        mCurrentStatus = status;
        switch (status) {
            case STATUS_FULL:
                rootView = LayoutInflater.from(context).inflate(R.layout.module_music_large, parent, false);
                break;
            case STATUS_HALF:
                rootView = LayoutInflater.from(context).inflate(R.layout.module_music_middle, parent, false);
                break;
            case STATUS_THIRD:
                rootView = LayoutInflater.from(context).inflate(R.layout.module_music, parent, false);
                break;
            default:

                break;
        }
        try {
            ButterKnife.bind(this, rootView);
            // ?????????
            ImgLoader.loadCircleImage(R.drawable.ic_music_play_failed_cover, ivCover);
            if (ivBg != null) {
                ivBg.setImageResource(R.drawable.shape_music_bg);
            }
        } catch (Exception e) {

        }

        mPresenter.refreshData();

        loadingAnim = null;

        return rootView;
    }

    @Override
    public void onPreRemove() {
        super.onPreRemove();

        if (ivCover != null) {
            ivCover.clearAnimation();
        }
        if (animator != null) {
            animator.pause();//????????????
        }

        if (waveAnim != null) {
            waveAnim.stop();
        }
    }

    @Override
    public void onDestroy() {
        if (mPresenter != null) {
            mPresenter.detach();
            mPresenter = null;
        }
    }

    private PlayInfo mPlayInfo;
    private boolean isPlaying;

    @Override
    public void updatePlayInfo(PlayInfo playInfo) {
        mPlayInfo = playInfo;

        if (tvLoading != null) {
            tvLoading.setVisibility(View.GONE);
        }
        if (STATUS_FULL == mCurrentStatus) {
            tvTitle.setText(mPlayInfo.getTitle() + " " + mPlayInfo.getArtists());
            tvArtist.setText("?????????" + (playInfo.isSong() ? "???????????????" : "????????????"));
        } else {
            tvTitle.setText(mPlayInfo.getTitle());
            tvArtist.setText(mPlayInfo.getArtists());
        }

        if (rootView != null) {
            if (ivCover == null) {
                ivCover = (ImageView) rootView.findViewById(R.id.iv_cover);
            }
            if (ivBg == null) {
                ivBg = (ImageView) rootView.findViewById(R.id.bg);
            }
        }
        if (ivCover == null || ivBg == null) {
            return;
        }

        if (TextUtils.isEmpty(mPlayInfo.getLogo())) {
            ImgLoader.loadCircleImage(R.drawable.ic_music_play_failed_cover, ivCover);
            ivBg.setImageResource(R.drawable.shape_music_bg);
        } else {
            ImgLoader.loadCircleImage(mPlayInfo.getLogo(), ivCover);
            ImgLoader.loadBlurImage(mPlayInfo.getLogo(), ivBg);
        }

        // ?????????????????????????????????
        if (null != mProgress) {
            mProgress.setProgress(0);
        }
        if (animator != null) {
            animator.pause();//????????????
        }
        if (waveAnim != null) {
            waveAnim.stop();
            waveAnim.reset();
        }
//        if (btnPlay != null) {
//            btnPlay.setBackgroundResource(R.drawable.ic_music_play_failed);
//        }
        if (tvMusicTitle != null) {
            tvMusicTitle.setText(playInfo.isSong() ? "??????" : "??????");
        }

        if (ivSign != null) {
            ivSign.setImageResource(playInfo.isSong() ? R.drawable.ic_music_sign : R.drawable.ic_music_sign_kaola);
        }

        setPlayStatusLoading();
    }

    private void setPlayStatusLoading() {
        if (btnPlay != null) {
//            btnPlay.setBackgroundResource(R.drawable.ic_music_play_failed);
            if (STATUS_FULL == mCurrentStatus) {
                btnPlay.setBackgroundResource(R.drawable.ic_music_loading_large);
            } else {
                btnPlay.setBackgroundResource(R.drawable.ic_music_loading);
            }
            if (loadingAnim == null) {
                loadingAnim = ObjectAnimator.ofFloat(btnPlay, "rotation", 0f, 360.0f);
                loadingAnim.setDuration(15000);
                loadingAnim.setInterpolator(new LinearInterpolator());//?????????
                loadingAnim.setRepeatCount(-1);//????????????????????????
                loadingAnim.setRepeatMode(ValueAnimator.RESTART);//??????????????????
                loadingAnim.start();//????????????
            } else {
                loadingAnim.resume();
            }
        }
        if (animator != null) {
            animator.pause();
        }
        if (waveAnim != null) {
            waveAnim.stop();
        }
    }

    @Override
    public void updatePlayProgress(long progress, long duration) {
        if (null != mProgress) {
            mProgress.setMax((int) duration);
            mProgress.setProgress((int) progress);
            mCurrProgress = progress;
        }
//        if (!isPlaying) {
//            isPlaying = true;
        if (progress > 0) {
            updatePlayStatus(PlayInfo.PLAY_STATE_PLAYING);
        }
//        }
    }

    ObjectAnimator animator;
    ObjectAnimator loadingAnim;

    @Override
    public void updatePlayStatus(int playState) {
        if (playState == PlayInfo.PLAY_STATE_PLAYING) {
            if (loadingAnim != null) {
                loadingAnim.pause();
            }
            int resId;
            if (mCurrentStatus == STATUS_FULL) {
                resId = R.drawable.ic_music_pause_large;
            } else {
                resId = R.drawable.ic_music_pause;
            }
            if (btnPlay!=null) {
                btnPlay.setRotation(0);
                btnPlay.setBackgroundResource(resId);
            }

            if (animator == null) {
                animator = ObjectAnimator.ofFloat(ivCover, "rotation", 0f, 360.0f);
                animator.setDuration(15000);
                animator.setInterpolator(new LinearInterpolator());//?????????
                animator.setRepeatCount(-1);//????????????????????????
                animator.setRepeatMode(ValueAnimator.RESTART);//??????????????????
                animator.start();//????????????
            } else {
                // ?????????????????????????????????????????????ivCover?????????????????????????????????????????????????????????????????????animator??????????????????ivCover??????????????????
                if (animator.getTarget()!=ivCover) {
                    animator.setTarget(ivCover);
                    animator.start();
                }else {
                    animator.resume();//????????????
                }
            }

//            Animation rotation = AnimationUtils.loadAnimation(GlobalContext.get(), R.anim.anim_rotation);
//            rotation.setInterpolator(new LinearInterpolator());
//            ivCover.startAnimation(rotation);
            if (waveAnim != null && !waveAnim.isStart()) {
                waveAnim.start();
            }
            AppLogic.removeUiGroundCallback(mCheckIsBufferingTask);
            AppLogic.runOnUiGround(mCheckIsBufferingTask, 1000);
        } else {
            AppLogic.removeUiGroundCallback(mCheckIsBufferingTask);

            isPlaying = false;
            if (playState == PlayInfo.PLAY_STATE_FAILED) {
                if (loadingAnim != null) {
                    loadingAnim.pause();
                }
                if (btnPlay!=null) {
                    btnPlay.setRotation(0);
                    btnPlay.setBackgroundResource(R.drawable.ic_music_play_failed);
                }
                if (mPlayInfo == null) {
                    ImgLoader.loadCircleImage(R.drawable.ic_music_play_failed_cover, ivCover);
                    if (ivBg != null) {
                        ivBg.setImageResource(R.drawable.shape_music_bg);
                    }
                    if (tvTitle != null) {
                        tvTitle.setText("????????????");
                    }
                }

            } else if (playState == PlayInfo.PLAY_STATE_OPEN) {
                // ??????
                if (null != mProgress) {
                    mProgress.setProgress(0);
                }
                if (animator != null) {
                    animator.pause();//????????????
                }
                if (waveAnim != null) {
                    waveAnim.stop();
                }
                if (loadingAnim != null) {
                    loadingAnim.pause();
                }
                if (btnPlay!=null) {
                    btnPlay.setRotation(0);
                    btnPlay.setBackgroundResource(R.drawable.ic_music_play_failed);
                }
                ImgLoader.loadCircleImage(R.drawable.ic_music_play_failed_cover, ivCover);
                if (ivBg != null) {
                    ivBg.setImageResource(R.drawable.shape_music_bg);
                }
                if (tvLoading != null) {
                    tvLoading.setVisibility(View.VISIBLE);
                }
                if (tvTitle != null) {
                    tvTitle.setText(null);
                }
                if (tvArtist != null) {
                    tvArtist.setText(null);
                }
                if (tvMusicTitle != null) {
                    tvMusicTitle.setText(MusicManager.getInstance().isMusicWillBePlay() ? "??????" : "??????");
                }

            } else {
                if (loadingAnim != null) {
                    loadingAnim.pause();
                }
                int resId;
                if (mCurrentStatus == STATUS_FULL) {
                    resId = R.drawable.ic_music_play_large;
                } else {
                    resId = R.drawable.ic_music_play;
                }
                if (btnPlay!=null) {
                    btnPlay.setRotation(0);
                    btnPlay.setBackgroundResource(resId);
                }
            }
//            ivCover.clearAnimation();
            if (animator != null) {
                animator.pause();//????????????
            }

            if (waveAnim != null) {
                waveAnim.stop();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isPlaying) {
            updatePlayStatus(PlayInfo.PLAY_STATE_PLAYING);
        }
    }

    //    @OnClick(R.id.btn_play)
//    void onPlayBtnClick() {
//        mPresenter.playOrPause();
//    }
//
//
//    @OnClick(R.id.btn_prev)
//    void onPrevBtnClick() {
//        mPresenter.prev();
//    }
//
//    @OnClick(R.id.btn_next)
//    void onNextBtnClick() {
//        mPresenter.next();
//    }
}
