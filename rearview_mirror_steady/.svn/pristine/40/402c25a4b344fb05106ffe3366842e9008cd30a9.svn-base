package com.txznet.music.ui.favour;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.action.FavourActionCreator;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.store.FavourStore;
import com.txznet.music.store.PlayInfoStore;
import com.txznet.music.ui.base.BasePlayerFragment;
import com.txznet.music.widget.RefreshLoadingView;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.extensions.aac.ViewModelProviders;

import java.util.List;
import java.util.Locale;

import butterknife.Bind;

/**
 * @author telen
 * @date 2018/12/6,14:40
 */
public class FavourFragment extends BasePlayerFragment<FavourAdapter> {


    @Bind(R.id.rv_data)
    EasyRecyclerView mRecyclerView;

    private FavourActionCreator mFavourActionCreator;

    @Override
    protected int getLayout() {
        return R.layout.favour_fragment;
    }

    @Override
    protected void initView(View view) {
        if (tvTitle != null) {
            tvTitle.setText("我的收藏音乐");
        }
        if (tvSubTitle != null) {
            tvSubTitle.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        FavourStore favourStore = ViewModelProviders.of(this).get(FavourStore.class);

        //可能耗时
        favourStore.getFavourAudios().observe(this, this::adapterRefresh);

        favourStore.getErrorStatus().observe(this, error -> {

//            if (mFavourAudios.size() <= 0) {
//                //如果没有数据则展示错误界面
//                mRecyclerView.showError();
//            }
        });

        PlayInfoStore playInfoStore = ViewModelProviders.of(this).get(PlayInfoStore.class);

        playInfoStore.getCurrPlaying().observe(this, audioV5 -> getAdapter().setPlayingAudio(audioV5));

        mFavourActionCreator = FavourActionCreator.getInstance();

        //发起请求
        mFavourActionCreator.getData(Operation.MANUAL, null);

    }

    @Override
    protected FavourAdapter setAdapter() {
        return new FavourAdapter(getContext());
    }

    @Override
    protected void initAdapter(FavourAdapter adapter) {
        super.initAdapter(adapter);

        LinearLayoutManager layout = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(layout);

        mRecyclerView.setProgressView(new RefreshLoadingView(getContext()));

        mRecyclerView.setAdapterWithProgress(adapter);

        mRecyclerView.addItemDecoration(new DividerDecoration(getResources().getColor(R.color.base_divider), 1, GlobalContext.get().getResources().getDimensionPixelOffset(R.dimen.m40), GlobalContext.get().getResources().getDimensionPixelOffset(R.dimen.m40)));
        mRecyclerView.getRecyclerView().setItemAnimator(null);
        mRecyclerView.getRecyclerView().setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        mRecyclerView.getErrorView().setOnClickListener(v -> {
            mRecyclerView.showProgress();
            mFavourActionCreator.getData(Operation.MANUAL, null);
        });

    }

    private void adapterRefresh(List<FavourAudio> audios) {
        getAdapter().clear();
        getAdapter().addAll(audios);

        if (tvSubTitle != null) {
            if (audios.size() > 0) {
                tvSubTitle.setVisibility(View.VISIBLE);
                tvSubTitle.setText(String.format(Locale.CHINA, "已收藏%d首歌曲", audios.size()));
            } else {
                tvSubTitle.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReportEvent.reportUserFavourEnter();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ReportEvent.reportUserFavourExit();
    }
}
