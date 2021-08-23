package com.txznet.music.ui.history;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.HistoryActionCreator;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.helper.AlbumConverts;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.store.HistoryAlbumStore;
import com.txznet.music.ui.base.BaseCheckPlayerFragment;
import com.txznet.music.ui.base.IHeaderBar;
import com.txznet.music.ui.base.IHeaderView;
import com.txznet.music.ui.base.adapter.UnifyCheckAdapter;
import com.txznet.music.ui.base.adapter.UnifyCheckHolder;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.ViewModelProviders;

import butterknife.Bind;

/**
 * 历史界面
 *
 * @author telen
 * @date 2018/12/3,16:51
 */
public class HistoryAlbumFragment extends BaseCheckPlayerFragment<UnifyCheckAdapter> {
    @Bind(R.id.rv_history)
    EasyRecyclerView mRecyclerView;
    @Bind(R.id.fl_header_bar)
    ViewGroup mHeaderBarView;

    IHeaderBar mHeaderBar;

    HistoryAlbumStore mHistoryAlbumStore;

    HistoryActionCreator mHistoryActionCreator;


    @Override
    protected int getLayout() {
        return R.layout.history_music_fragment;
    }

    @Override
    protected void initView(View view) {
        LinearLayoutManager layout = new LinearLayoutManager(getContext());
        layout.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.addItemDecoration(new DividerDecoration(getResources().getColor(R.color.base_divider), 1, getResources().getDimensionPixelOffset(R.dimen.base_divider_margin), getResources().getDimensionPixelOffset(R.dimen.base_divider_margin)));
        mRecyclerView.setAdapterWithProgress(getAdapter());
        mRecyclerView.getRecyclerView().setItemAnimator(null);
        mRecyclerView.getRecyclerView().setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        mHeaderBar = new IHeaderBar() {
            @Override
            public void removeHeader() {
                mHeaderBarView.removeAllViews();
                mHeaderBarView.setVisibility(View.GONE);
            }

            @Override
            public void addHeader(IHeaderView headerView) {
                mHeaderBarView.removeAllViews();
                mHeaderBarView.addView(headerView.getView());
                mHeaderBarView.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean hasHeader() {
                return mHeaderBarView.getChildCount() > 0;
            }
        };
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mDeleteAudioDialog.setTitle("删除电台");

        //请求数据
        mHistoryActionCreator = HistoryActionCreator.getInstance();
        //从数据库中获取,发事件给到ActionCreator
        mHistoryAlbumStore = ViewModelProviders.of(this).get(HistoryAlbumStore.class);

        mHistoryAlbumStore.getHistoryAlbumData().observe(this, this::updateData);

        mHistoryAlbumStore.getAlbumStatus().observe(this, state -> {
            if (state != null) {
                switch (state) {
                    case HISTORY_GET_EMPTY_DATA:
                        //界面刷新
                        getAdapter().clear();
                        break;
                    case HISTORY_NET_ERROR:
                        mRecyclerView.showError();
                        break;
                    case HISTORY_DELETE_FAIL:
                        break;
                    default:
                        break;
                }
            }
        });
        mHistoryActionCreator.getHistoryAlbumData(Operation.AUTO);//第一次进入为自动
    }

    @Override
    protected UnifyCheckAdapter<HistoryAlbum> setAdapter() {
        return new UnifyCheckAdapter<HistoryAlbum>(getContext()) {
            @Override
            public UnifyCheckHolder getBaseViewHolder(ViewGroup parent) {
                return new UnifyCheckHolder<HistoryAlbum>(parent) {
                    @Override
                    public void setData(HistoryAlbum data) {
                        super.setData(data);
                        cbFavour.setVisibility(View.GONE);
                        tvIndex.setText(getLayoutPosition() - getAdapter().getHeaderCount() + 1 + "");
                        if (data.audio != null) {
                            tvName.setText(data.audio.name);
                        }
                        if (TextUtils.isEmpty(tvName.getText())) {
                            tvName.setText(Constant.UNKNOWN);
                        }
                        tvArtist.setText(data.name);

                        setItemClickListener(v -> {
                            PlayerActionCreator.get().playHistoryAlbum(Operation.MANUAL, AlbumConverts.convert2Album(data));
                        });
                    }

                };
            }
        };
    }


    @Override
    public void onClickDeleteEvent() {
        HistoryActionCreator.getInstance().deleteHistoryAlbumItem(Operation.MANUAL, getAdapter().getCheckedData());
    }

    @Override
    protected IHeaderBar getHeaderBar() {
        return mHeaderBar;
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            ReportEvent.reportUserHistoryRadioEnter();
        } else {
            if (isAdded()) {
                ReportEvent.reportUserHistoryRadioExit();
            }
        }
    }
}
