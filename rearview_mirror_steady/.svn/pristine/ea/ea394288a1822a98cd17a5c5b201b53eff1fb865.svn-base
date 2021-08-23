package com.txznet.music.playerModule.ui;

import android.app.DialogFragment;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.aspsine.swipetoloadlayout.OnLoadMoreListener;
import com.aspsine.swipetoloadlayout.OnRefreshListener;
import com.aspsine.swipetoloadlayout.SwipeToLoadLayout;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.playerModule.PlayListItem;
import com.txznet.music.playerModule.PlayListPresenter;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.ui.adapter.PlayListAdapterV41;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by brainBear on 2017/9/25.
 */

public class PlayListFragmentV41 extends DialogFragment implements OnRefreshListener, OnLoadMoreListener, PlayListContract.View {

    public static final int MODE_FLOAT = 0;
    public static final int MODE_INSERT = 1;
    private static final String TAG = "PlayListFragment:";
    private static final String KEY_MODE = "mode";
    private SwipeToLoadLayout swipeToLoadLayout;
    private View mContentView;
    private TXZLinearLayoutManager mLayoutManager;
    private PlayListAdapterV41 mAdapter;
    private RecyclerView mPlayList;
    private int mMode;
    private ImageView ivPlayList;
    private TextView tvSubscribe;
    private View mLayoutSubscribe;
    private PlayListPresenter mPresenter;
    private CheckBox cbSubscribe;
    private List<PlayListItem> mPlayListItems;
    private long mCurrentAlbumId;

    public static DialogFragment newInstance(@Mode int mode) {
        PlayListFragmentV41 playListFragmentV41 = new PlayListFragmentV41();
        Bundle bundle = new Bundle();
        bundle.putInt(KEY_MODE, mode);
        playListFragmentV41.setArguments(bundle);
        return playListFragmentV41;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMode = getArguments().getInt(KEY_MODE, MODE_INSERT);
        } else {
            mMode = MODE_INSERT;
        }
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.PlayerListFragmentStyle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mContentView = inflater.inflate(R.layout.fragment_play_list_v41, container, false);

        mPlayList = (RecyclerView) mContentView.findViewById(R.id.swipe_target);
        swipeToLoadLayout = (SwipeToLoadLayout) mContentView.findViewById(R.id.swipeToLoadLayout);
        mLayoutSubscribe = mContentView.findViewById(R.id.layout_subscribe);
        cbSubscribe = (CheckBox) mContentView.findViewById(R.id.cb_subscribe);
        tvSubscribe = (TextView) mContentView.findViewById(R.id.tv_subscribe);
        ivPlayList = (ImageView) mContentView.findViewById(R.id.iv_play_list);

        Resources resources = getActivity().getResources();
        ivPlayList.setImageDrawable(resources.getDrawable(R.drawable.ic_playlist_v41));

        mLayoutSubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.subscribe(PlayInfoManager.getInstance().getCurrentAlbum(), cbSubscribe.isChecked());
                if (cbSubscribe.isChecked()) {
                    ReportEvent.clickPlayerPageRadioUnSubscribe(PlayInfoManager.getInstance().getCurrentAlbum());
                } else {
                    ReportEvent.clickPlayerPageRadioSubscribe(PlayInfoManager.getInstance().getCurrentAlbum());
                }

            }
        });

        return mContentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mMode == MODE_FLOAT) {
            Rect outRect = new Rect();
            getActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);

            Window window = getDialog().getWindow();
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.END;
            params.height = WindowManager.LayoutParams.MATCH_PARENT;
            params.width = (int) (outRect.width() * 0.5);
            window.setAttributes(params);
        } else {
            mContentView.setBackgroundColor(Color.TRANSPARENT);
        }

        swipeToLoadLayout.setOnRefreshListener(this);
        swipeToLoadLayout.setOnLoadMoreListener(this);

        mAdapter = new PlayListAdapterV41(getActivity().getApplicationContext());
        mLayoutManager = new TXZLinearLayoutManager(GlobalContext.get());
        mLayoutManager.setAutoMeasureEnabled(false);
        mPlayList.setLayoutManager(mLayoutManager);
        mPlayList.setAdapter(mAdapter);

        mPlayList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int pos = mLayoutManager.findLastCompletelyVisibleItemPosition();
                    if (pos == mAdapter.getItemCount() - 1) {
                        swipeToLoadLayout.setLoadingMore(true);
                    }
                }
            }
        });

        mAdapter.setOnItemClickListener(new PlayListOnItemClickListener() {
            @Override
            public void onPlay(Audio audio) {
                mPresenter.play(audio);
                if (mMode == MODE_FLOAT) {
                    dismiss();
                }

                //上报
                if (mMode == MODE_FLOAT) {
                    ReportEvent.clickBarPlaylistItemBtn(audio.getSid(), audio.getId(), audio.getName());
                } else {
                    ReportEvent.clickPlayerPagePlaylistItemBtn(audio.getSid(), audio.getId(), audio.getName());
                }
            }

            @Override
            public void onFavor(Audio audio, boolean isCancel) {
                mPresenter.favor(audio, isCancel);

            }
        });


        mPresenter = new PlayListPresenter(this);
        mPresenter.register();
    }

    public String getFragmentId() {
        return "ListFragment#" + this.hashCode() + "/播放列表";
    }

    @Override
    public void onResume() {
        super.onResume();
        LogUtil.d(TAG, "onResume");
    }

    private boolean needMovePos() {
        boolean result = true;
        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
        if (null != currentAlbum && currentAlbum.getId() == mCurrentAlbumId) {
            result = false;
        }

        mCurrentAlbumId = null != currentAlbum ? currentAlbum.getId() : 0;
        Logger.d(TAG, "need move:" + result);

        return result;
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
                Logger.d(TAG, "move to %d", i);
                mPlayList.scrollToPosition(i);
                break;
            }
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.unregister();
    }

    @Override
    public void onRefresh() {
        mPresenter.refresh();
    }

    @Override
    public void onLoadMore() {
        mPresenter.loadMore();
    }


    @Override
    public void setPresenter(PlayListContract.Presenter presenter) {

    }

    @Override
    public void showRefresh() {
        swipeToLoadLayout.setRefreshing(true);
    }

    @Override
    public void showLoadMore() {
        swipeToLoadLayout.setLoadingMore(true);
    }

    @Override
    public void hideRefreshOrLoadMore() {
        swipeToLoadLayout.setRefreshing(false);
        swipeToLoadLayout.setLoadingMore(false);
    }

    @Override
    public void refreshData(DiffUtil.DiffResult diffResult, List<PlayListItem> playListItems) {
        mPlayListItems = playListItems;
        mAdapter.setData(mPlayListItems);
        mAdapter.notifyDataSetChanged();
//        diffResult.dispatchUpdatesTo(mAdapter);

        if (needMovePos()) {
            moveToCurrentPlayPos();
        }
    }

    @Override
    public void refreshItem(int pos) {
        mAdapter.notifyItemChanged(pos);
    }

    @Override
    public void setSubscribeVisibility(boolean visible) {
        if (visible) {
            mLayoutSubscribe.setVisibility(View.VISIBLE);
        } else {
            mLayoutSubscribe.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void setSubscribeStatus(boolean isHighLight, boolean enable) {
        if (isHighLight) {
            cbSubscribe.setChecked(true);
            tvSubscribe.setText("已订阅");
        } else {
            cbSubscribe.setChecked(false);
            tvSubscribe.setText("订阅专辑");
        }
    }


    @IntDef({MODE_FLOAT, MODE_INSERT})
    @Retention(RetentionPolicy.SOURCE)
    private @interface Mode {
    }

}