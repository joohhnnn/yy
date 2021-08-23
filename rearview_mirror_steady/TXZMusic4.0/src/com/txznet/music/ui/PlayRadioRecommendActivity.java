package com.txznet.music.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.adapter.ItemRadioRecommendAdapter;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.JumpUtils;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.BarPlayerView;
import com.txznet.music.widget.LoadingView;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.txznet.music.albumModule.bean.Album.FLAG_CURRENT_TIME_ZONE;
import static com.txznet.music.albumModule.bean.Album.FLAG_SUPPORT;

/**
 * Created by 58295 on 2018/4/16.
 */

public class PlayRadioRecommendActivity extends BaseActivity implements PlayInfoRadioRecContract.View {

    @Bind(R.id.recycler_radio_recommend)
    RecyclerView mRecyclerRadioRecommend;
    @Bind(R.id.choice_bg)
    ImageView ivBg;
    @Bind(R.id.ll_left_back)
    LinearLayout leftBack;
    @Bind(R.id.head_title)
    TextView tvTitle;
    @Bind(R.id.bar_player_view)
    BarPlayerView mBarPlayerView;
    @Bind(R.id.layout_library_loading_view)
    LoadingView mLoadingView;
    //    boolean flag = true;
//    private Album mAlbum;
    //    private long mAlbumId;
//    private long mCategoryID;
//    public int type = -1;


    private PlayRadioRecPresenter mPresenter;


    private TXZLinearLayoutManager manager;
    private ItemRadioRecommendAdapter mAdapter;
    private List<Album> mAlbums;

    @Override
    protected String getActivityTag() {
        return "PlayRadioRecommendActivity";
    }

    @Override
    public void bindViews(Bundle savedInstanceState) {
        ButterKnife.bind(this);
        showLoading();
        mPresenter = new PlayRadioRecPresenter(this, getIntent());
        mPresenter.register();
        initView();
        initListener();
        reqData();
    }

    public void reqData() {
        mPresenter.reqData();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Logger.d(TAG, "test:onNewIntent");
//        mPresenter.reqData();
    }

    public void initListener() {
        mLoadingView.setErrorHintListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoading();
                mPresenter.refreshContent();
            }
        });
    }

    public void initView() {
        mBarPlayerView.setPlayListVisible(false);
        manager = new TXZLinearLayoutManager(this);
        mRecyclerRadioRecommend.setLayoutManager(manager);
        mRecyclerRadioRecommend.setAdapter(getAdapter());

        mRecyclerRadioRecommend.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int pos = manager.findLastCompletelyVisibleItemPosition();
                    if (pos == mAdapter.getItemCount() - 1) {

                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        mAdapter.setOnItemClickListener(new TimeOfThemeClickListener() {
            @Override
            public void clickTheme(Album album) {
                mPresenter.changeTheme(album, 0);
            }
        });

        mBarPlayerView.setBarPlayerViewOperationListener(new BarPlayerView.BarPlayerViewOperationListener() {
            @Override
            public void onClickPlayList() {
//                mPresenter.showPlayList();
//                ReportEvent.clickBarPlaylistBtn();
            }

            @Override
            public void onClickCover() {
//                if (null == PlayInfoManager.getInstance().getCurrentAudio()) {
//                    ToastUtils.showShort("目前没有正在播放的音乐");
//                    return;
//                }
//                Intent intent = new Intent(BaseBarActivity.this, ReserveConfigSingleTaskActivity2.class);
//                startActivity(intent);
//                ReportEvent.clickBarCoverBtn();
            }

            @Override
            public void OnPlayNext() {
                mPresenter.playNext();
                ReportEvent.clickBarNextBtn(PlayInfoManager.getInstance().getCurrentAudio(), PlayInfoManager.getInstance().getCurrentAlbum());
            }

            @Override
            public void OnPlayPrev() {
                mPresenter.playPrev();
                ReportEvent.clickBarPrevBtn(PlayInfoManager.getInstance().getCurrentAudio(), PlayInfoManager.getInstance().getCurrentAlbum());
            }

            @Override
            public void OnPlayOrPause() {
                int currentPlayerStatus = PlayInfoManager.getInstance().getCurrentPlayerUIStatus();
                switch (currentPlayerStatus) {
                    case PlayerInfo.PLAYER_UI_STATUS_BUFFER:
                    case PlayerInfo.PLAYER_UI_STATUS_PAUSE:
                    case PlayerInfo.PLAYER_UI_STATUS_RELEASE:
                        ReportEvent.clickBarPlay(PlayInfoManager.getInstance().getCurrentAudio(), PlayInfoManager.getInstance().getCurrentAlbum());
                        break;
                    case PlayerInfo.PLAYER_UI_STATUS_PLAYING:
                        ReportEvent.clickBarPause(PlayInfoManager.getInstance().getCurrentAudio(), PlayInfoManager.getInstance().getCurrentAlbum());
                        break;
                }

                mPresenter.playOrPause();
            }

            @Override
            public void OnChangePlayMode() {
                mPresenter.switchMode();
                ReportEvent.clickBarMode(SharedPreferencesUtils.getPlayMode());
            }

            @Override
            public void onFavor(boolean cancel) {
                mPresenter.favor(cancel);
                if (cancel) {
                    ReportEvent.clickBarMusicUnFavour(PlayInfoManager.getInstance().getCurrentAudio());
                } else {
                    ReportEvent.clickBarMusicFavour(PlayInfoManager.getInstance().getCurrentAudio());
                }
            }

            @Override
            public void onSubscribe(boolean cancel) {
                mPresenter.subscribe(cancel);
                if (cancel) {
                    ReportEvent.clickBarRadioUnSubscribe(PlayInfoManager.getInstance().getCurrentAlbum());
                } else {
                    ReportEvent.clickBarRadioSubscribe(PlayInfoManager.getInstance().getCurrentAlbum());
                }
            }
        });


    }

    public RecyclerView.Adapter getAdapter() {
        mAdapter = new ItemRadioRecommendAdapter(this, mAlbums);
        return mAdapter;
    }

    @OnClick(R.id.ll_left_back)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ll_left_back:
                onBackPressed();
                break;
        }

    }

    @Override
    public ImageView getBg() {
        return ivBg;
    }


    @Override
    public int getLayout() {
        if(ScreenUtils.isPhonePortrait()){
            return R.layout.act_play_radio_recomend_phone_portrait;
        }
        return R.layout.act_play_radio_recomend;
    }

    @Override
    public void setPresenter(PlayInfoRadioRecContract.Presenter presenter) {

    }

    @Override
    public void onPlayInfoUpdated(Audio audio, Album album) {
        mBarPlayerView.updatePlayInfo(this,audio, album);
    }

    @Override
    public void onProgressUpdated(long position, long duration) {
        mBarPlayerView.updateProgress(position, duration);
    }

    @Override
    public void onPlayerModeUpdated(int mode) {
        mBarPlayerView.updatePlayMode(mode);
    }

    @Override
    public void onPlayerStatusUpdated(int status) {
        mBarPlayerView.updatePlayStatus(status);
    }

    @Override
    public void onBufferProgressUpdated(List<LocalBuffer> buffers) {
        mBarPlayerView.updateBufferProgress(buffers);
    }


    @Override
    public void showTips(String tips) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.unregister();
    }

    @Override
    public void onFavorVisibilityChanged(boolean visibility) {
        mBarPlayerView.setFavorVisibility(visibility);
    }

    @Override
    public void onFavorStatusChanged(boolean isFavor, boolean available) {
        mBarPlayerView.setFavorStatus(isFavor, available);
    }

    @Override
    public void onSubscribeStatusChanged(boolean isSubscribe, boolean available) {
        mBarPlayerView.setSubscribeStatus(isSubscribe, available);
    }

    @Override
    public void exitView() {
        onBackPressed();
    }


    @Override
    public void showLoadContent() {
        if (mLoadingView.isShowLoading()) {
            mLoadingView.showContent();
        }
    }

    @Override
    public void showLoadTimeOut() {
        if (mLoadingView.isShowLoading()) {
            mLoadingView.showError(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT, R.drawable.load_out_time, Constant.RS_VOICE_MUSIC_CLICK_RETRY);
        }
    }

    @Override
    public void showLoadNotData() {
        if (mLoadingView.isShowLoading()) {
            mLoadingView.showEmpty(Constant.RS_VOICE_MUSIC_NO_DATA, R.drawable.fm_me_no_file);
        }

    }

    @Override
    public void showLoadNotNet() {
        if (mLoadingView.isShowLoading()) {
            mLoadingView.showError(Constant.RS_VOICE_SPEAK_NONE_NET, R.drawable.fm_me_no_result_network, Constant.RS_VOICE_MUSIC_CLICK_RETRY);
        }

    }

    @Override
    public void showLoading() {
        if (!mLoadingView.isShowLoading()) {
            mLoadingView.showLoading(R.drawable.fm_album_loading_rotate, R.drawable.fm_album_loading_icon, "正在加载中...");
        }
    }

    @Override
    public void setTitle(String name) {
        tvTitle.setText(name);
    }

    @Override
    public void changeAlbum(Album album) {
        mAdapter.setSelectPosition(mAlbums.indexOf(album));
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showAlbums(List<Album> albums) {
        if (albums == null) {
            return;
        }
        mAlbums = albums;
        mAdapter.setData(mAlbums);
        mAdapter.setSelectPosition(mAlbums.indexOf(PlayInfoManager.getInstance().getCurrentAlbum()));
        mAdapter.notifyDataSetChanged();
        MoveToPosition(albums);
    }

    /**
     * des 移动到标识位下的Album专辑
     *
     * @param albums
     */
    public void MoveToPosition(List<Album> albums) {
        int i = 0;
        for (Album album : albums) {
            if (Utils.getDataWithPosition(album.getFlag(), FLAG_CURRENT_TIME_ZONE) == FLAG_SUPPORT) {
                break;
            }
            i++;
        }
        mRecyclerRadioRecommend.scrollToPosition(i);
    }

    @Override
    public void onBackPressed() {
        if (ActivityStack.getInstance().getSize() == 1) {
            JumpUtils.getInstance().jumpToHomePageActivity(this, HomeActivity.RADIO_i);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
