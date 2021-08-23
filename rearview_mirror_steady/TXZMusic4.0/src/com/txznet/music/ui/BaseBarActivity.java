package com.txznet.music.ui;

import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.ui.PlayListFragmentV41;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.widget.BarPlayerView;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity2;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity9;

import java.lang.ref.WeakReference;
import java.util.List;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by brainBear on 2017/12/22.
 */

public abstract class BaseBarActivity extends BaseActivity implements BaseBarContract.View {

    protected BarPlayerView mBarPlayerView;
    private WeakReference<DialogFragment> mRefPlayListFragment;
    private BaseBarPresenter mPresenter;
    private CompositeDisposable mCompositeDisposable;

    @Override
    public abstract int getLayout();

    /**
     * 如果返回值为空,必须在布局文件中包含id为bar_player_view的BarPlayerView
     *
     * @return
     */
    public BarPlayerView getBarPlayerView() {
        return null;
    }

    @Override
    public abstract void bindViews(Bundle savedInstanceState);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBarPlayerView = getBarPlayerView();
        if (null == mBarPlayerView) {
            mBarPlayerView = (BarPlayerView) findViewById(R.id.bar_player_view);
        }

        mPresenter = new BaseBarPresenter(this);
        mPresenter.register();


        mBarPlayerView.setBarPlayerViewOperationListener(new BarPlayerView.BarPlayerViewOperationListener() {
            @Override
            public void onClickPlayList() {
                //区分这里，跳转不同的界面
                if (PlayInfoManager.getInstance().getParentAlbum() != null) {
                    Intent intent = new Intent(BaseBarActivity.this, ReserveConfigSingleTaskActivity9.class);
                    startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, PlayInfoManager.DATA_CHEZHU_FM, PlayInfoManager.getInstance().getParentAlbum(), PlayInfoManager.getInstance().getParentAlbum().getCategoryId(), "", PlayInfoManager.TYPE_CAR_FM));
                    ReportEvent.clickBarCoverBtn();
                } else if (PlayInfoManager.getInstance().getCurrentAlbum() != null && PlayInfoManager.getInstance().getCurrentAlbum().getAlbumType() == Album.ALBUM_TYPE_NORMAL_FM) {
                    Intent intent = new Intent(BaseBarActivity.this, ReserveConfigSingleTaskActivity9.class);
                    startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, PlayInfoManager.DATA_CHEZHU_TYPE_FM, PlayInfoManager.getInstance().getCurrentAlbum(), PlayInfoManager.getInstance().getCurrentAlbum().getCategoryId(), "", PlayInfoManager.TYPE_NORMAL_FM));
                    ReportEvent.clickBarCoverBtn();
                } else {
                    mPresenter.showPlayList();
                }

                ReportEvent.clickBarPlaylistBtn();
            }

            @Override
            public void onClickCover() {
                if (null == PlayInfoManager.getInstance().getCurrentAudio()) {
                    ToastUtils.showShort("目前没有正在播放的音乐");
                    return;
                }
                if (PlayInfoManager.getInstance().getParentAlbum() != null) {
                    Intent intent = new Intent(BaseBarActivity.this, ReserveConfigSingleTaskActivity9.class);
                    startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, PlayInfoManager.DATA_CHEZHU_FM, PlayInfoManager.getInstance().getParentAlbum(), PlayInfoManager.getInstance().getParentAlbum().getCategoryId(), "", PlayInfoManager.TYPE_CAR_FM));
                    ReportEvent.clickBarCoverBtn();
                } else if (PlayInfoManager.getInstance().getCurrentAlbum() != null && PlayInfoManager.getInstance().getCurrentAlbum().getAlbumType() == Album.ALBUM_TYPE_NORMAL_FM) {
                    Intent intent = new Intent(BaseBarActivity.this, ReserveConfigSingleTaskActivity9.class);
                    startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, PlayInfoManager.DATA_CHEZHU_TYPE_FM, PlayInfoManager.getInstance().getCurrentAlbum(), PlayInfoManager.getInstance().getCurrentAlbum().getCategoryId(), "", PlayInfoManager.TYPE_NORMAL_FM));
                    ReportEvent.clickBarCoverBtn();
                } else {
                    Intent intent = new Intent(BaseBarActivity.this, ReserveConfigSingleTaskActivity2.class);
                    startActivity(intent);
                    ReportEvent.clickBarCoverBtn();
                }
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


    @Override
    protected void onDestroy() {
        mPresenter.unregister();
        super.onDestroy();
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
        ToastUtils.showShort(tips);
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
    public void setPresenter(BaseBarContract.Presenter presenter) {
    }

    @Override
    public void setPlayListEnable(boolean enable) {
        mBarPlayerView.setPlayListEnable(enable);
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

    private DialogFragment getPlayListFragment() {
        if (mRefPlayListFragment == null || mRefPlayListFragment.get() == null) {
            DialogFragment playListFragment = PlayListFragmentV41.newInstance(PlayListFragmentV41.MODE_FLOAT);
            mRefPlayListFragment = new WeakReference<DialogFragment>(playListFragment);
        }
        return mRefPlayListFragment.get();
    }

    @Override
    public void exitView() {

    }
}
