package com.txznet.music.ui.player;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.txznet.comm.util.StringUtils;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.PlayQueueActionCreator;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.store.PlayInfoStore;
import com.txznet.music.store.PlayerQueueStore;
import com.txznet.music.ui.base.BaseFragment;
import com.txznet.music.util.AlbumUtils;
import com.txznet.music.util.PlaySceneUtils;
import com.txznet.music.util.adapter.RecyclerAdapter;
import com.txznet.music.widget.RefreshFooterView;
import com.txznet.music.widget.RefreshHeaderView;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.ViewModelProviders;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * 播放队列界面
 *
 * @author zackzhou
 * @date 2018/12/13,15:40
 */

public class PlayerQueueFragment extends BaseFragment {

    @Bind(R.id.swipeToLoadLayout)
    SwipeToLoadLayout swipeToLoadLayout;
    @Bind(R.id.swipe_refresh_header)
    RefreshHeaderView refreshHeaderView;
    @Bind(R.id.swipe_load_more_footer)
    RefreshFooterView refreshFooterView;

    @Bind(R.id.swipe_target)
    RecyclerView lvQueue;
    @Bind(R.id.tv_count)
    TextView tvCount;

    PlayerQueueStore mPlayQueueStore;
    PlayInfoStore mPlayInfoStore;

    LinearLayoutManager mLayoutManager;
    RecyclerAdapter<AudioV5> mAdapter;

    AudioV5 mCurrPlay;
    Album mAlbum;

    @Override
    protected int getLayout() {
        return R.layout.player_queue_fragment;
    }

    @Override
    protected void initView(View view) {
        mLayoutManager = new LinearLayoutManager(getContext());
        lvQueue.setLayoutManager(mLayoutManager);

        mAdapter = new RecyclerAdapter<AudioV5>(getContext(), new ArrayList<>(0), R.layout.player_queue_list_item_audio) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder holder, int position, AudioV5 item) {
                TextView tv_index = (TextView) holder.getView(R.id.tv_index);
                ImageView iv_playing = (ImageView) holder.getView(R.id.iv_playing);
                TextView tv_name = (TextView) holder.getView(R.id.tv_name);
                TextView tv_artist = (TextView) holder.getView(R.id.tv_artist);
                TextView tv_progress = (TextView) holder.getView(R.id.tv_progress);

                tv_index.setText(position + 1 + "");
                tv_name.setText(item.name);

                if (PlaySceneUtils.isMusicScene() || mAlbum == null) {
                    tv_artist.setText(StringUtils.toString(item.artist));
                    tv_progress.setVisibility(View.GONE);
                } else {
                    tv_artist.setText(mAlbum.name);
                    tv_progress.setVisibility(View.VISIBLE);
                    tv_progress.setText(item.progress + "%");
                }

                if (TextUtils.isEmpty(tv_artist.getText())) {
                    tv_artist.setText(Constant.UNKNOWN);
                }

                if (mCurrPlay != null && mCurrPlay.equals(item)) {
                    tv_index.setVisibility(View.INVISIBLE);
                    iv_playing.setVisibility(View.VISIBLE);
                    tv_name.setAlpha(1f);
                    tv_artist.setAlpha(1f);
                    tv_name.setTextColor(getResources().getColor(R.color.red));
                    tv_artist.setTextColor(getResources().getColor(R.color.red_40));
                    tv_progress.setTextColor(getResources().getColor(R.color.red_40));
                } else {
                    tv_index.setVisibility(View.VISIBLE);
                    iv_playing.setVisibility(View.GONE);
                    tv_name.setTextColor(getResources().getColor(R.color.white));
                    tv_artist.setTextColor(getResources().getColor(R.color.white_40));
                    tv_progress.setTextColor(getResources().getColor(R.color.white_40));

                    if (item.hasPlay) {
                        tv_index.setAlpha(0.2f);
                        tv_name.setAlpha(0.2f);
                        tv_artist.setAlpha(0.2f);
                    } else {
                        tv_index.setAlpha(1f);
                        tv_name.setAlpha(1f);
                        tv_artist.setAlpha(1f);
                    }
                }
                holder.itemView.setOnClickListener(v -> {
                    PlayerActionCreator.get().play(Operation.MANUAL, item);
                    ReportEvent.reportPlayListItemClick(item);
                });
            }
        };
        lvQueue.setAdapter(mAdapter);
        lvQueue.addItemDecoration(new DividerDecoration(getResources().getColor(R.color.base_divider), 1, getResources().getDimensionPixelOffset(R.dimen.base_divider_margin), getResources().getDimensionPixelOffset(R.dimen.base_divider_margin)));

        lvQueue.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (scrollToCenter()) {
                    lvQueue.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            }
        });

        swipeToLoadLayout.setOnRefreshListener(() -> PlayQueueActionCreator.get().loadMore(Operation.MANUAL, true));

        swipeToLoadLayout.setOnLoadMoreListener(() -> PlayQueueActionCreator.get().loadMore(Operation.MANUAL, false));
    }

    boolean hasScrollTo = false;

    @Override
    protected void initData(Bundle savedInstanceState) {
        mPlayQueueStore = ViewModelProviders.of(this).get(PlayerQueueStore.class);
        mPlayQueueStore.getQueue().observe(this, audioV5List -> {
            if (audioV5List != null) {
                mAdapter.notifyDataSetChanged(audioV5List);
            }
            refreshListCount();
            scrollToCenter();
        });
        mPlayQueueStore.getLoadMoreStatus().observe(this, status -> {
            if (status == null) {
                return;
            }
            swipeToLoadLayout.setRefreshing(false);
            swipeToLoadLayout.setLoadingMore(false);
            switch (status) {
                case LOAD_EMPTY:
                    if (swipeToLoadLayout.isRefreshing()) {
                        refreshHeaderView.noMore();
                    } else {
                        refreshFooterView.noMore();
                    }
                    break;
                case LOAD_FAILD:
                    if (swipeToLoadLayout.isRefreshing()) {
                        refreshHeaderView.loadFailed();
                    } else {
                        refreshFooterView.loadFailed();
                    }
                    break;
                case LOAD_SUCCESS:
                    break;
                default:
                    break;
            }
        });

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                scrollToCenter();
            }
        });

        mPlayInfoStore = ViewModelProviders.of(getActivity()).get(PlayInfoStore.class);
        mPlayInfoStore.getCurrPlaying().observe(this, audioV5 -> {
            mCurrPlay = audioV5;
            mAdapter.notifyDataSetChanged();
        });
        mPlayInfoStore.getAlbum().observe(this, album -> {
            mAlbum = album;
            if (mAlbum == null || AlbumUtils.isNews(album)) {
                swipeToLoadLayout.setRefreshEnabled(false);
                swipeToLoadLayout.setLoadMoreEnabled(false);
            } else {
                swipeToLoadLayout.setRefreshEnabled(true);
                swipeToLoadLayout.setLoadMoreEnabled(true);
            }
        });

        PlayQueueActionCreator.get().getQueue();
    }

    private boolean scrollToCenter() {
        if (mAdapter.getData() != null && !mAdapter.getData().isEmpty()) {
            int last = mLayoutManager.findLastVisibleItemPosition();
            int first = mLayoutManager.findFirstVisibleItemPosition();
            if (last != -1 && first != -1) {
                int pageSize = last - first + 1;
                int halfPageSize = pageSize / 2;
                if (!hasScrollTo && pageSize != 0 && mCurrPlay != null) {
                    hasScrollTo = true;
                    int index = mAdapter.getData().indexOf(mCurrPlay);
                    int scrollIndex;
                    if (index <= halfPageSize) {
                        // 如果是头部元素
                        scrollIndex = 0;
                    } else if (index + halfPageSize >= mAdapter.getData().size()) {
                        // 如果是尾部元素
                        scrollIndex = mAdapter.getData().size() - 1;
                    } else {
                        scrollIndex = index - halfPageSize;
                    }
                    mLayoutManager.scrollToPositionWithOffset(scrollIndex, 0);
                }
            }
        }
        return false;
    }

    @OnClick({R.id.btn_close, R.id.v_empty})
    public void btnClose(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
            case R.id.v_empty:
                dismissAllowingStateLoss();
                break;
            default:
                break;
        }
    }

    private void refreshListCount() {
        if (PlaySceneUtils.isMusicScene()) {
            tvCount.setText(String.format("(共%s首)", PlayHelper.get().getAlbumAudioCount()));
        } else {
            tvCount.setText(String.format("(共%s期)", PlayHelper.get().getAlbumAudioCount()));
        }
    }

    @Override
    public void onDestroyView() {
        if (refreshHeaderView != null) {
            refreshHeaderView.destroy();
        }
        if (refreshFooterView != null) {
            refreshFooterView.destroy();
        }
        super.onDestroyView();
    }
}
