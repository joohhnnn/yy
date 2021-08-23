package com.txznet.music.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.image.glide.GlideImageLoader;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.BasePlayerView;
import com.txznet.music.widget.CoverPlayerView;
import com.txznet.music.widget.OnEffectiveClickListener;

import java.util.List;

/**
 * Created by brainBear on 2017/9/25.
 */

public class PlayDetailsActivity extends BaseActivity implements PlayInfoContract.View<PlayInfoContract.Presenter> {

    private static final String TAG = "PlayDetailsActivity:";

    private View mViewBack;
    private CoverPlayerView mCoverPlayerView;
    private ImageView ivBg;
    private Album lastAlbum;
    private PlayDetailPresenter mPresenter;
    private TextView tvBack;
    private ImageView ivArrow;

    @Override
    protected String getActivityTag() {
        return "PlayDetailsActivity";
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new PlayDetailPresenter(this);
        mPresenter.register();
        ReportEvent.enterPlayerPage();
    }

    @Override
    public ImageView getBg() {
        return ivBg;
    }


    private void initView() {
        ivArrow = (ImageView) findViewById(R.id.iv_arrow);
        tvBack = (TextView) findViewById(R.id.tv_back);

        ivArrow.setImageResource(R.drawable.ic_down_arrow);
        tvBack.setVisibility(View.INVISIBLE);

        mViewBack = findViewById(R.id.layout_back);
        ivBg = (ImageView) findViewById(R.id.choice_bg);
        mCoverPlayerView = (CoverPlayerView) findViewById(R.id.cover_player_view);
        View llTitle = findViewById(R.id.ll_title);

        llTitle.setVisibility(View.GONE);

        mViewBack.setOnClickListener(new OnEffectiveClickListener() {
            @Override
            public void onEffectiveClick(View v) {
                finish();
            }
        });


        mCoverPlayerView.setPlayerViewOperationListener(new BasePlayerView.PlayerViewOperationListener() {
            @Override
            public void OnPlayNext() {
                mPresenter.playNext();
                ReportEvent.clickPlayerPageNextBtn();
            }

            @Override
            public void OnPlayPrev() {
                mPresenter.playPrev();
                ReportEvent.clickPlayerPagePrevBtn();
            }

            @Override
            public void OnPlayOrPause() {
                int currentPlayerStatus = PlayInfoManager.getInstance().getCurrentPlayerUIStatus();
                switch (currentPlayerStatus) {
                    case PlayerInfo.PLAYER_UI_STATUS_BUFFER:
                    case PlayerInfo.PLAYER_UI_STATUS_PAUSE:
                    case PlayerInfo.PLAYER_UI_STATUS_RELEASE:
                        ReportEvent.clickPlayerPagePlay();
                        break;
                    case PlayerInfo.PLAYER_UI_STATUS_PLAYING:
                        ReportEvent.clickPlayerPagePause();
                        break;
                }
                mPresenter.playOrPause();
            }

            @Override
            public void OnChangePlayMode() {
                mPresenter.switchMode();
                ReportEvent.clickPlayerPageMode(SharedPreferencesUtils.getPlayMode());
            }

            @Override
            public void onFavor(boolean cancel) {
                mPresenter.favor(cancel);
                if (cancel) {
                    ReportEvent.clickPlayerPageMusicUnFavour(PlayInfoManager.getInstance().getCurrentAudio());
                } else {
                    ReportEvent.clickPlayerPageMusicFavour(PlayInfoManager.getInstance().getCurrentAudio());
                }
            }

            @Override
            public void onSubscribe(boolean cancel) {
                mPresenter.subscribe(cancel);
                if (cancel) {
                    ReportEvent.clickPlayerPageRadioUnSubscribe(PlayInfoManager.getInstance().getCurrentAlbum());
                } else {
                    ReportEvent.clickPlayerPageRadioSubscribe(PlayInfoManager.getInstance().getCurrentAlbum());
                }
            }
        });

        mCoverPlayerView.setBackgroundColor(Color.TRANSPARENT);
        ImageFactory.getInstance().setStyle(IImageLoader.BLUR_FILTER);


//        if (null == currentAudio || !Utils.isSong(currentAudio.getSid())) {
//            mCoverPlayerView.forceSetPlayModeToSequence(true);
//        }
    }


    @Override
    protected void onDestroy() {
        mPresenter.unregister();
        super.onDestroy();
        ReportEvent.exitPlayerPage();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_play_details;
    }

    @Override
    public void setPresenter(PlayInfoContract.Presenter presenter) {

    }

    @Override
    public void onPlayInfoUpdated(Audio audio, Album album) {
        mCoverPlayerView.updatePlayInfo(audio, album);
    }

    @Override
    public void onProgressUpdated(long position, long duration) {
        mCoverPlayerView.updateProgress(position, duration);
    }

    @Override
    public void onPlayerModeUpdated(int mode) {
        mCoverPlayerView.updatePlayMode(mode);
    }

    @Override
    public void onPlayerStatusUpdated(int status) {
        mCoverPlayerView.updatePlayStatus(status);
    }

    @Override
    public void onBufferProgressUpdated(List<LocalBuffer> buffers) {
        mCoverPlayerView.updateBufferProgress(buffers);
    }

    @Override
    public void showTips(String tips) {
        ToastUtils.showShort(tips);
    }

    @Override
    public void onFavorVisibilityChanged(boolean visibility) {
        mCoverPlayerView.setFavorVisibility(visibility);
    }

    @Override
    public void onFavorStatusChanged(boolean isFavor, boolean available) {
        mCoverPlayerView.setFavorStatus(isFavor, available);
    }

    @Override
    public void onSubscribeStatusChanged(boolean isSubscribe, boolean available) {
        mCoverPlayerView.setSubscribeStatus(isSubscribe, available);
    }

}
