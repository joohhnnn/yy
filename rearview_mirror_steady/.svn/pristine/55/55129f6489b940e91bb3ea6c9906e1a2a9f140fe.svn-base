package com.txznet.music.ui.album;

import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.jude.easyrecyclerview.adapter.RecyclerArrayAdapter;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.AlbumActionCreator;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.CategoryItemData;
import com.txznet.music.helper.GlideHelper;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.BillBoardContentClickEvent;
import com.txznet.music.store.AlbumStore;
import com.txznet.music.ui.base.BasePlayerFragment;
import com.txznet.music.widget.AlphaLinearLayout;
import com.txznet.music.widget.GridDividerItemDecoration;
import com.txznet.music.widget.RefreshLoadingView;
import com.txznet.music.widget.dialog.CategoryMoreDialog;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.ViewModelProviders;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * @author telen
 * @date 2019/1/14,10:58
 */
public class AlbumFragment extends BasePlayerFragment<AlbumAdapter> implements RecyclerArrayAdapter.OnLoadMoreListener {

    private final static String KEY_CATEGORY = "category";
    private final static String KEY_CATEGORY_ALL = "categorys";
    private final static String KEY_SOURCE_FROM = "sourceFrom";


    CategoryItemData mCategoryItemData = null;
    List<CategoryItemData> mCategoryList = null;

    @Bind(R.id.ll_more_category)
    AlphaLinearLayout llMoreCategory;

    private int pageIndex = 0;

    @Bind(R.id.rv_data)
    EasyRecyclerView mRecyclerView;
    private CategoryMoreDialog mCategoryMoreDialog;

    private String mSourceFrom;

    private long mBaseCategoryId;

    @Override
    protected int getLayout() {
        return R.layout.album_fragment;
    }

    public static DialogFragment getAlbumFragment(CategoryItemData category, String sourceFrom) {
        AlbumFragment albumFragment = new AlbumFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_CATEGORY, category);
        bundle.putParcelableArrayList(KEY_CATEGORY_ALL, category.arrChild);
        bundle.putString(KEY_SOURCE_FROM, sourceFrom);
        albumFragment.setArguments(bundle);
        return albumFragment;
    }

    @Override
    protected void initView(View view) {
        mSourceFrom = getArguments().getString(KEY_SOURCE_FROM);

        //展示主标题
        mCategoryItemData = getArguments().getParcelable(KEY_CATEGORY);
        mBaseCategoryId = mCategoryItemData.categoryId;

        // 有声书
        if (mBaseCategoryId == 500000) {
            ReportEvent.reportCategoryAudioBookEnter(mBaseCategoryId);
        } else if (mBaseCategoryId == 1200000) {
            ReportEvent.reportCategoryPcEnter(mBaseCategoryId);
        } else {
            ReportEvent.reportCategoryOtherEnter(mBaseCategoryId);
        }

        //展示子标题
        mCategoryList = getArguments().getParcelableArrayList(KEY_CATEGORY_ALL);
        if (CollectionUtils.isNotEmpty(mCategoryList)) {
            tvTitle.setText(String.format("%s |", mCategoryItemData.desc));
            llMoreCategory.setVisibility(View.VISIBLE);
            mCategoryItemData = mCategoryList.get(0);
            mCategoryMoreDialog = new CategoryMoreDialog(getContext(), o -> {
                mCategoryItemData = o;
                pageIndex = 0;
                tvSubTitle.setText(mCategoryItemData.desc);
                getAdapter().clear();
                mRecyclerView.showProgress();
                reqMoreData();

                if (mCategoryItemData.categoryId >= 500000 && mCategoryItemData.categoryId <= 600000) {
                    // 有声书
                    ReportEvent.reportCategoryAudioBookMoreClick(mCategoryItemData.categoryId);
                } else if (mCategoryItemData.categoryId >= 1200000 && mCategoryItemData.categoryId <= 1300000) {
                    // 亲子
                    ReportEvent.reportCategoryPcMoreClick(mCategoryItemData.categoryId);
                }
            }).setData(mCategoryList);
            tvSubTitle.setText(mCategoryItemData.desc);
        } else {
            tvTitle.setText(mCategoryItemData.desc);
            llMoreCategory.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);

        AlbumStore albumStore = ViewModelProviders.of(this).get(AlbumStore.class);

        albumStore.getRespAlbum().observe(this, txzRespAlbum -> {
            if (BuildConfig.DEBUG) {
                Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",initData:" + txzRespAlbum);
            }

            if (Long.parseLong(txzRespAlbum.categoryId) == mCategoryItemData.categoryId) {
                //如果是同一个分类则需要累加
            } else {
                //如果不是同一个分类则需要清空在添加
                return;
            }
            pageIndex = txzRespAlbum.pageId;
            if (getAdapter().getCount() <= 0 && CollectionUtils.isEmpty(txzRespAlbum.arrAlbum)) {
                mRecyclerView.showEmpty();
            } else {
                getAdapter().addAll(txzRespAlbum.arrAlbum);
            }
        });

        albumStore.getStatusLiveEvent().observe(this, new Observer<AlbumStore.Status>() {
            @Override
            public void onChanged(@Nullable AlbumStore.Status status) {

                int count = getAdapter().getCount();
                if (BuildConfig.DEBUG) {
                    Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + "error:status,onChanged:" + count);
                }

                if (count <= 0) {
                    if (status == AlbumStore.Status.NO_NET) {
                        ((TextView) mRecyclerView.getErrorView().findViewById(R.id.tv_tips)).setText(getResources().getText(R.string.album_category_error_no_net_str));
                    } else {
                        ((TextView) mRecyclerView.getErrorView().findViewById(R.id.tv_tips)).setText(getResources().getText(R.string.album_category_error_poor_net_str));
                    }

                    mRecyclerView.showError();
                } else {
                    if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                        getAdapter().resumeMore();
                    } else {
                        getAdapter().pauseMore();
                    }
                }
            }
        });

        getAdapter().setMore(R.layout.view_more, this);
        getAdapter().setNoMore(R.layout.album_no_more_view);
        getAdapter().setError(R.layout.album_load_error_view, new RecyclerArrayAdapter.OnErrorListener() {
            @Override
            public void onErrorShow() {

            }

            @Override
            public void onErrorClick() {
                getAdapter().resumeMore();
            }
        });
        //请求数据
        reqMoreData();

        mRecyclerView.getErrorView().setOnClickListener(v -> {
            mRecyclerView.showProgress();
            reqMoreData();
        });
        mRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        getAdapter().setOnItemClickListener(pos -> {
            Album album = getAdapter().getItem(pos);
            Album currAlbum = PlayHelper.get().getCurrAlbum();
            if (currAlbum != null && currAlbum.equals(album)) {
                if (PlayHelper.get().isPlaying()) {
                    PlayerActionCreator.get().pause(Operation.MANUAL);
                } else {
                    PlayerActionCreator.get().playAlbum(Operation.MANUAL, album);
                }
            } else {
                PlayerActionCreator.get().playAlbum(Operation.MANUAL, album);
            }
            if ("recSecondBillboard".equals(mSourceFrom)) {
                ReportEvent.reportPayListingsContentClick(album, BillBoardContentClickEvent.CLICK_POS_LIST_SECONDARY);
            } else if ("radioChoiceBillboard".equals(mSourceFrom)) {
                ReportEvent.reportNeceListingsContentClick(album, BillBoardContentClickEvent.CLICK_POS_LIST_SECONDARY);
            } else if ("radioCategory".equals(mSourceFrom)) {
                ReportEvent.reportPayListingsContentClick(album, BillBoardContentClickEvent.CLICK_POS_LIST_SECONDARY);
            }
            // 有声书
            if (mCategoryItemData.categoryId >= 500000 && mCategoryItemData.categoryId <= 600000) {
                ReportEvent.reportCategoryAudioBookListItemClick(album, mCategoryItemData.categoryId);
            } else if (mCategoryItemData.categoryId >= 1200000 && mCategoryItemData.categoryId <= 1300000) {
                ReportEvent.reportCategoryPcListItemClick(album, mCategoryItemData.categoryId);
            } else {
                ReportEvent.reportCategoryOtherListItemClick(album, mCategoryItemData.categoryId);
            }
        });
    }

    @Override
    protected AlbumAdapter setAdapter() {
        return new AlbumAdapter(this);
    }

    @Override
    protected void initAdapter(AlbumAdapter adapter) {
        super.initAdapter(adapter);

        GridLayoutManager manager = new GridLayoutManager(GlobalContext.get(), 2);
        manager.setSpanSizeLookup(adapter.obtainGridSpanSizeLookUp(2));
        mRecyclerView.setLayoutManager(manager);

        GridDividerItemDecoration gridDividerItemDecoration = new GridDividerItemDecoration(getResources().getDimensionPixelOffset(R.dimen.m24), getResources().getColor(R.color.transparent));
        mRecyclerView.addItemDecoration(gridDividerItemDecoration);

        mRecyclerView.setProgressView(new RefreshLoadingView(getContext()));

        mRecyclerView.setAdapterWithProgress(adapter);
        GlideHelper.attachToRecyclerView(this, mRecyclerView.getRecyclerView());
    }


    @OnClick(R.id.ll_more_category)
    public void onViewClicked() {
        mCategoryMoreDialog.show();
    }

    @Override
    public void onLoadMore() {
        reqMoreData();
    }

    private void reqMoreData() {
        if (BuildConfig.DEBUG) {
            Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",reqMoreData");
        }

        AlbumActionCreator.getInstance().getAlbumByCategory(Operation.MANUAL, pageIndex + 1, mCategoryItemData.sid, mCategoryItemData.categoryId);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // 有声书
        if (mBaseCategoryId == 500000) {
            ReportEvent.reportCategoryAudioBookExit(mBaseCategoryId);
        } else if (mBaseCategoryId == 1200000) {
            ReportEvent.reportCategoryPcExit(mBaseCategoryId);
        } else {
            ReportEvent.reportCategoryOtherExit(mBaseCategoryId);
        }
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerView != null && mRecyclerView.getRecyclerView() != null) {
            mRecyclerView.getRecyclerView().setAdapter(null);
        }
        super.onDestroyView();
    }
}
