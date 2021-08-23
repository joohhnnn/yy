package com.txznet.music.ui;

import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.ui.PlayListFragmentV41;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.adapter.PlayDetailContract;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.widget.BarPlayerView;
import com.txznet.music.widget.CoverPlayerView;
import com.txznet.music.widget.OnEffectiveClickListener;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by brainBear on 2017/9/25.
 */

public class PlayDetailsActivity extends BaseActivity implements PlayDetailContract.View {

    private static final String TAG = "PlayDetailsActivity:";

    private View mViewBack;
    private CoverPlayerView mCoverPlayerView;
    private WeakReference<DialogFragment> mRefPlayListFragment;
    private ImageView ivBg;
    private Album lastAlbum;
    private PlayDetailPresenter mPresenter;
    private TextView tvBack;
    private ImageView ivArrow;
    //    private LoadingView mLoadingView;
    private Album mAlbum;
    private long mCategoryID;
    private int mScreen;

    @Override
    protected String getActivityTag() {
        return "PlayDetailsActivity";
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        initView();
        if (getIntent() != null) {
            mAlbum = JsonHelper.toObject(this.getIntent().getStringExtra(PlayInfoManager.INTENT_FIELD_ALBUM), Album.class);
            mCategoryID = this.getIntent().getLongExtra(PlayInfoManager.INTENT_FIELD_CATEGORY_ID, 0);
            mScreen = this.getIntent().getIntExtra(PlayInfoManager.INTENT_FIELD_SCREEN, 0);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new PlayDetailPresenter(this);
        mPresenter.register();
        if (mAlbum != null) {
            mPresenter.playAlbum(mScreen, mAlbum, mCategoryID, true);
        }
        ReportEvent.enterPlayerPage();
    }

    @Override
    public ImageView getBg() {
        return ivBg;
    }


    private void initView() {
        ivArrow = (ImageView) findViewById(R.id.iv_arrow);
        if(ScreenUtils.isPhonePortrait()){
            ViewGroup.LayoutParams layoutParams = ivArrow.getLayoutParams();
            int width = (int)AttrUtils.getAttrDimension(this,R.attr.bar_player_view_icon_size,58)/2;
            int height = (int)AttrUtils.getAttrDimension(this,R.attr.bar_player_view_icon_size,58);
            layoutParams.height = height;
            layoutParams.width = width;
            ivArrow.setLayoutParams(layoutParams);
        }
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


        mCoverPlayerView.setBarPlayerViewOperationListener(new BarPlayerView.BarPlayerViewOperationListener() {
            @Override
            public void onClickPlayList() {
                mPresenter.showPlayList();
                ReportEvent.clickBarPlaylistBtn();
            }

            @Override
            public void onClickCover() {

            }

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
        mAlbum = null;
        super.onDestroy();
        ReportEvent.exitPlayerPage();
    }

    @Override
    public int getLayout() {
        if (ScreenUtils.isPhonePortrait()) {
            return R.layout.activity_play_details_phone_portrait;
        }
        return R.layout.activity_play_details;
    }

    @Override
    public void onPlayInfoUpdated(Audio audio, Album album) {
        mCoverPlayerView.updatePlayInfo(this, audio, album);
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

    @Override
    public void exitView() {
        //退出界面
        onBackPressed();
    }

    @Override
    public void showLoadContent() {
        ObserverManage.getObserver().send(InfoMessage.PLAY_LIST_NORMAL);
    }

    @Override
    public void showLoadTimeOut() {
        ObserverManage.getObserver().send(InfoMessage.PLAY_LIST_NET_TIMEOUT_ERROR);
    }

    @Override
    public void showLoadNotData() {
        ObserverManage.getObserver().send(InfoMessage.PLAY_LIST_RESP_ALBUM_AUDIO_ERROR_NO_DATA);
    }

    @Override
    public void showLoadNotNet() {
        ObserverManage.getObserver().send(InfoMessage.PLAY_LIST_NET_ERROR);
    }

    @Override
    public void showLoading() {
        ObserverManage.getObserver().send(InfoMessage.PLAY_LIST_LOADING);
    }

    private DialogFragment getPlayListFragment() {
        if (mRefPlayListFragment == null || mRefPlayListFragment.get() == null) {
            DialogFragment playListFragment = PlayListFragmentV41.newInstance(PlayListFragmentV41.MODE_FLOAT);
            mRefPlayListFragment = new WeakReference<DialogFragment>(playListFragment);
        }
        return mRefPlayListFragment.get();
    }

    @Override
    public void showPlayList(boolean show) {
        if (show) {
            getPlayListFragment().show(getFragmentManager(), "PlayListFragment");
        } else {
            if (mRefPlayListFragment != null && mRefPlayListFragment.get() != null) {
                mRefPlayListFragment.get().dismiss();
            }
        }
    }

    @Override
    public void setPresenter(PlayDetailContract.Presenter presenter) {
    }
}
