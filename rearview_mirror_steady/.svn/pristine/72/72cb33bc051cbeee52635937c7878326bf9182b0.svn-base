package com.txznet.music.albumModule.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.ui.adapter.RadioListAdapter;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.ui.AudioBaseFragment;
import com.txznet.music.playerModule.PlayListItem;
import com.txznet.music.playerModule.PlayListPresenter;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.ui.PlayListContract;
import com.txznet.music.playerModule.ui.PlayListOnItemClickListener;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.widget.LoadingView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.Bind;

/**
 * Created by 58295 on 2018/4/17.
 */

public class RadioListFragment extends AudioBaseFragment implements OnRefreshListener, OnLoadMoreListener, PlayListContract.View {

    @Bind(R.id.swipe_target)
    RecyclerView mRecyRadio;
    @Bind(R.id.swipeToLoadLayout)
    SwipeToLoadLayout mSwipeToLoadLayout;
    @Bind(R.id.layout_library_loading_view)
    LoadingView mLoadingView;
    private TXZLinearLayoutManager manager;

    private RadioListAdapter mAdapter;
    private List<PlayListItem> mPlayListItems;
    private PlayListPresenter mPresenter;
    private long mCurrentAlbumId;

    @Override
    public String getFragmentId() {
        return "RadioListFragment";
    }

    @Override
    protected int getLayout() {
        return R.layout.fragment_radio_list;
    }


    @Override
    protected void initView(View view) {
        mSwipeToLoadLayout.setOnRefreshListener(this);
        mSwipeToLoadLayout.setOnLoadMoreListener(this);
        manager = new TXZLinearLayoutManager(getActivity());
        mAdapter = new RadioListAdapter(getActivity());
        mRecyRadio.setLayoutManager(manager);
        mRecyRadio.setAdapter(mAdapter);

        mRecyRadio.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int pos = manager.findLastCompletelyVisibleItemPosition();
                    if (pos == mAdapter.getItemCount() - 1) {
                        mSwipeToLoadLayout.setLoadingMore(true);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        mAdapter.setOnItemClickListener(new PlayListOnItemClickListener() {
            @Override
            public void onPlay(Audio audio) {
                Log.d("musicPlay", "onPlay this News: ");
                mPresenter.play(audio);
            }

            @Override
            public void onFavor(Audio audio, boolean isCancel) {

            }
        });

        mPresenter = new PlayListPresenter(this);
        mPresenter.register();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

    }

    @Override
    public void onLoadMore() {
        mPresenter.loadMore();
    }

    @Override
    public void onRefresh() {
        mPresenter.refresh();
    }

    @Override
    public void setPresenter(PlayListContract.Presenter presenter) {

    }

    @Override
    public void showRefresh() {
        mSwipeToLoadLayout.setRefreshing(true);
    }

    @Override
    public void showLoadMore() {
        mSwipeToLoadLayout.setLoadingMore(true);
    }

    @Override
    public void hideRefreshOrLoadMore() {
        mSwipeToLoadLayout.setRefreshing(false);
        mSwipeToLoadLayout.setLoadingMore(false);
    }

    @Override
    public void refreshData(DiffUtil.DiffResult diffResult, List<PlayListItem> playListItems) {
        mPlayListItems = playListItems;
        mAdapter.setData(mPlayListItems);
        mAdapter.notifyDataSetChanged();
        if (needMovePos()) {
            moveToCurrentPlayPos();
        }
    }

    private void moveToCurrentPlayPos() {
        Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
        if (null == currentAudio) {
            return;
        }

        if (null == mPlayListItems || mPlayListItems.isEmpty()) {
            return;
        }

        for (int i = 0; i < mPlayListItems.size(); i++) {
            PlayListItem item = mPlayListItems.get(i);
            if (item.getAudio().getId() == currentAudio.getId()
                    && item.getAudio().getSid() == currentAudio.getSid()) {
                Logger.d("demo", "move to %d", i);
                mRecyRadio.scrollToPosition(i);
                break;
            }
        }
    }

    private boolean needMovePos() {
        boolean result = true;
        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
        if (null != currentAlbum && currentAlbum.getId() == mCurrentAlbumId) {
            result = false;
        }

        mCurrentAlbumId = null != currentAlbum ? currentAlbum.getId() : 0;
        Logger.d("demo", "need move:" + result);

        return result;
    }

    @Override
    public void refreshItem(int pos) {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.unregister();
    }

    @Override
    public void setSubscribeVisibility(boolean visible) {

    }

    @Override
    public void setSubscribeStatus(boolean isHighLight, boolean enable) {

    }

    @Override
    public void showLoadContent() {
        mLoadingView.showContent();
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
        mLoadingView.showLoading(R.drawable.fm_album_loading_rotate, R.drawable.fm_album_loading_icon, "正在加载中...");
    }


}
