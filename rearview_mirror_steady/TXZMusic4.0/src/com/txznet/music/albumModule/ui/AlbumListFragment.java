package com.txznet.music.albumModule.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.ui.adapter.AlbumDecoration;
import com.txznet.music.albumModule.ui.adapter.CategoryAlbumAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumBaseAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumClassifyAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumFragmentAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumRankListAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumRecommandAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumSingerAdapter;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.ui.AudioBaseFragment;
import com.txznet.music.data.entity.Category;
import com.txznet.music.novelModule.NovelActivity;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.CarFmUtils;
import com.txznet.music.ui.layout.TXZGridLayoutManager;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.SpaceItemDecoration;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.LoadingView;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity2;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity7;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity9;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.ArrayList;
import java.util.List;

import app.dinus.com.itemdecoration.GridOffsetsItemDecoration;
import butterknife.Bind;

/**
 * Created by brainBear on 2018/2/24.
 */

public class AlbumListFragment extends AudioBaseFragment implements AlbumListContract.View {

    private static final String TAG = "AlbumListFragment:";
    private static final String KEY_CATEGORY = "category";

    @Bind(R.id.v_loading)
    LoadingView mLoadingView;
    @Bind(R.id.list_album)
    RecyclerView mRvList;
    CategoryAlbumAdapter mCategoryAlbumAdapter;
    private TXZLinearLayoutManager mCategoryLayoutManager;
    private Category mCategory;
    private AlbumListContract.Presenter mPresenter;
    private RecyclerView.LayoutManager mLayoutManager;
    private ItemAlbumBaseAdapter mAdapter;
    private List<Album> mAlbums = new ArrayList<>();
    private int playindex = -1;
    private int oldPlayIndex = -1;
    private static final int ALBUM_ITEM_HORIZONTAL_GAY = 20;

    public static AlbumListFragment newInstance(Category category) {
        AlbumListFragment albumListFragment = new AlbumListFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_CATEGORY, category);
        albumListFragment.setArguments(bundle);

        return albumListFragment;
    }

    @Override
    public String getFragmentId() {
        return "AlbumListFragment";
    }

    @Override
    protected int getLayout() {
        if (ScreenUtils.isPhonePortrait()) {
            return R.layout.fragment_album_list_phone_portrait;
        }
        return R.layout.fragment_album_list;
    }


//    private void init

    @Override
    protected void initView(View view) {
        mLoadingView.setErrorHintListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPresenter.requestAlbum(false);
            }
        });

        RecyclerView.ItemDecoration itemDecoration = null;
        if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_VERTICAL || ScreenUtils.isPhonePortrait()) {
            int colCount = 4;
            int defCol = 3;
            if (ScreenUtils.isPhonePortrait()) {
                defCol = 3;
            } else {
                defCol = 4;
            }
            colCount = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_MUSIC_SCREEN_ALBUM_COL_NUM, defCol);

            mLayoutManager = new TXZGridLayoutManager(getActivity(), colCount);
            if (ScreenUtils.isPhonePortrait()) {
                int right = (int) AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_horizontal_gap, 0);
                int left = (int) AttrUtils.getAttrDimension(getActivity(), R.dimen.y45, 45);
                int bottom = (int) AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_gap, 0);
                ViewGroup.LayoutParams layoutParams = mRvList.getLayoutParams();

                layoutParams.width = right * 2 + (int) AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_content_size_width, 0) * 2 + left * 2;
                mRvList.setLayoutParams(layoutParams);
                itemDecoration = new SpaceItemDecoration(left, bottom, right);
            } else {
                itemDecoration = new GridOffsetsItemDecoration(GridOffsetsItemDecoration.GRID_OFFSETS_VERTICAL);
                ((GridOffsetsItemDecoration) itemDecoration).setHorizontalItemOffsets(ALBUM_ITEM_HORIZONTAL_GAY);
                ((GridOffsetsItemDecoration) itemDecoration).setVerticalItemOffsets((int) AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_gap, 0));
            }
        } else {
            mLayoutManager = new TXZLinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            itemDecoration = new AlbumDecoration((int) AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_gap, 0));
        }

        mRvList.setLayoutManager(mLayoutManager);
        mRvList.addItemDecoration(itemDecoration);

        mRvList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isNewClick = false;

            private int lastVisibleItem;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (null != mPresenter && null != mAdapter && mCategory != null && !mAdapter.isShowLoading() && newState == 0 && lastVisibleItem + 1 >= mAdapter.getItemCount() && isNewClick) {
                    mPresenter.requestAlbum(true);
                }
                if (newState == 0) {
                    isNewClick = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mLayoutManager instanceof TXZGridLayoutManager) {
                    lastVisibleItem = ((GridLayoutManager) mLayoutManager).findLastVisibleItemPosition();
                } else if (mLayoutManager instanceof LinearLayoutManager) {
                    lastVisibleItem = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
                }
                isNewClick = true;
            }

        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        mCategory = (Category) arguments.getSerializable(KEY_CATEGORY);

        Logger.d(TAG, mCategory);

        mPresenter = new AlbumListPresenter(this, mCategory);
        mPresenter.register();

        if (mCategory.getCategoryId() != Constant.CATEGORY_NOVEL) {
            switch (mCategory.getShowStyle()) {
                case ItemAlbumBaseAdapter.SHOWTYPE_SINGER:
                    mAdapter = new ItemAlbumSingerAdapter(getActivity(), this, mAlbums);
                    break;
                case ItemAlbumBaseAdapter.SHOWTYPE_RECOMMAND:
                    mAdapter = new ItemAlbumRecommandAdapter(getActivity(), this, mAlbums);
                    break;
                case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_LIST:
                    mAdapter = new ItemAlbumRankListAdapter(getActivity(), this, mAlbums);
                    break;
                case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_CLASSIFY:
                    mAdapter = new ItemAlbumClassifyAdapter(getActivity(), this, mAlbums);
                    break;
                case ItemAlbumBaseAdapter.SHOWTYPE_UNDEFINE:
                    mAdapter = new ItemAlbumFragmentAdapter(getActivity(), this, mAlbums);
                    break;
                case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_OTHER:
                    mAdapter = new ItemAlbumFragmentAdapter(getActivity(), this, mAlbums);
                    break;
            }
            // 设置mAdapter
            if (null == mAdapter) {
                mAdapter = new ItemAlbumFragmentAdapter(getActivity(), this, mAlbums);
            }
            mAdapter.setOnItemClickListener(getAlbumItemIconClickListener());
            mAdapter.setOnItemIconClickListener(getAlbumItemIconClickListener());
            mRvList.setAdapter(mAdapter);
            mPresenter.requestAlbum(false);
        } else {
            mCategoryAlbumAdapter = new CategoryAlbumAdapter(this, mCategory.getArrChild());
            mCategoryAlbumAdapter.setOnItemClickListener(getCategoryAlbumClick());
            mRvList.setAdapter(mCategoryAlbumAdapter);
            mCategoryAlbumAdapter.notifyDataSetChanged();
        }


    }


    public void requestData() {
        //如果是小说的话，请求的逻辑不一样，小说直接将分类中的子分类取出，
        if (mCategory.getCategoryId() != Constant.CATEGORY_NOVEL) {
            mPresenter.requestAlbum(false);
        } else {
            //小说的那种不需要清掉，因为不能清空，清空，会把数据破坏掉
        }
    }

    public void refreshData() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void clearData() {
        if (mCategory.getCategoryId() != Constant.CATEGORY_NOVEL) {
            mAlbums.clear();
            mAdapter.notifyDataSetChanged();
        } else {
            //小说的那种不需要清掉，因为不能清空，清空，会把数据破坏掉
        }

    }

    /**
     * 是否包含车主Fm，因为此处要开一个定时器，来刷新专辑名称
     *
     * @param albums
     * @return
     */
    private Album isContainCarFm(List<Album> albums) {
        if (CollectionUtils.isNotEmpty(albums)) {
            for (Album album : albums) {
                if (Utils.isCarFm(album)) {
                    return album;
                }
            }
        }
        return null;
    }

    /**
     * 点击小说分类,进入相应小说分类界面
     *
     * @return
     */
    private AdapterView.OnItemClickListener getCategoryAlbumClick() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category category = mCategory.getArrChild().get(position);
                if (ActivityStack.getInstance().currentActivity().hashCode() == getActivity().hashCode()) {
                    Intent intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity7.class);
                    intent.putExtra(NovelActivity.KEY_CATEGORY, category);
                    getActivity().startActivity(intent);
                    ReportEvent.clickRadioAlbumCategory(category.getCategoryId());
                }
            }
        };
    }

    protected AdapterView.OnItemClickListener getAlbumItemIconClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Album album = (Album) mAdapter.getItem(position);
                mPresenter.jumpToDetail(PlayInfoManager.DATA_ALBUM, album, mCategory, position);
                /*
                 * 第一次打开同听时，播放上次未播完的电台时，监听播放状态（updateItemPlayStatus）里面playIndex获取不到准确的，
                 * 因为当前的专辑是音乐专辑不是电台专辑。所以需要在点击的时候在判断
                 */
                if (playindex == -1) {
                    setPlayIndex();
                }
                oldPlayIndex = playindex;
                playindex = position;
            }
        };
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.unregister();
        clearCheckAlbumNameTimer();
        mPresenter = null;
        mAlbums.clear();
        mAlbums = null;
    }

    @Override
    public void setPresenter(AlbumListContract.Presenter presenter) {

    }

    @Override
    public void showLoadMore() {
        mAdapter.setShowLoading(true);
    }

    @Override
    public void hideLoadMore() {
        mAdapter.setShowLoading(false);
    }

    @Override
    public void showLoading() {
        if (!mLoadingView.isShowLoading()) {
            mLoadingView.showLoading(R.drawable.fm_album_loading_rotate, R.drawable.fm_album_loading_icon, "正在加载中...");
        }
    }


    @Override
    public void showAlbums(List<Album> albums, boolean isLoadMore) {
        mLoadingView.showContent();
        if (!isLoadMore) {
            mAlbums.clear();
            mAlbums.addAll(albums);
            mAdapter.notifyDataSetChanged();


            //添加定时器，一般车主Fm的再第一页
            addCheckAlbumNameTimer();
        } else {
            int oldSize = mAlbums.size();
            mAlbums.addAll(albums);
            mAdapter.notifyItemRangeInserted(oldSize - 1, albums.size());
        }
    }

    /**
     * 添加一个改变车主Fm的当前时段的名称
     */
    public void addCheckAlbumNameTimer() {
        Album containCarFm = isContainCarFm(mAlbums);
        if (containCarFm != null) {
            CarFmUtils.getInstance().changeAlbumName(containCarFm, SystemClock.elapsedRealtime());
        }
    }

    public void clearCheckAlbumNameTimer() {
        Album containCarFm = isContainCarFm(mAlbums);
        if (containCarFm != null) {
            CarFmUtils.getInstance().clearCheckAlbumNameTimer();
        }
    }

    @Override
    public void refreshItem(int pos) {
        mAdapter.notifyItemChanged(pos);
    }

    @Override
    public void showEmpty() {
        if (mLoadingView.isShowLoading()) {
            mLoadingView.showEmpty(Constant.RS_VOICE_MUSIC_NO_DATA, R.drawable.fm_me_no_file);
        }
    }

    @Override
    public void showError(String hint) {
        if (mLoadingView.isShowLoading()) {
            mLoadingView.showError(Constant.RS_VOICE_SPEAK_TIPS_UNKNOWN, R.drawable.fm_me_no_result_network, hint);
        } else if (mAdapter != null && mAdapter.isShowLoading()) {
            mAdapter.setShowLoading(false);
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_TIPS_UNKNOWN);
        }
    }


    @Override
    public void jumpToSuperRadioView(Album album, long categoryID) {
        Intent intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity9.class);
        getActivity().startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, PlayInfoManager.DATA_CHEZHU_FM, album, categoryID, Constant.ALBUM_SHOW_URL, PlayInfoManager.TYPE_CAR_FM));
    }

    @Override
    public void jumpToPlayerDetailView(int screen, Album album, long categoryID) {
        Intent intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity2.class);
        getActivity().startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, screen, album, categoryID, null, PlayInfoManager.TYPE_NORMAL_ALBUM));
    }

    @Override
    public void jumpToTypeRadioView(Album album, long categoryID) {
        Intent intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity9.class);
        getActivity().startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, PlayInfoManager.DATA_CHEZHU_TYPE_FM, album, categoryID, Constant.ALBUM_SHOW_URL, PlayInfoManager.TYPE_NORMAL_FM));
    }

    @Override
    public void updateItemPlayStatus() {
        if (mAdapter != null) {
            setPlayIndex();//第一次进入同听的时候点击暂停时需要获取playIndex,否则就Item的Icon没有修改
            mAdapter.notifyItemChanged(playindex);
            mAdapter.notifyItemChanged(oldPlayIndex);
        }
    }

    public void setPlayIndex() {
        if (PlayInfoManager.getInstance().getCurrentAlbum() != null) {
            playindex = mAlbums.indexOf(PlayInfoManager.getInstance().getCurrentAlbum().getParentAlbum());
            if (playindex == -1) {
                playindex = mAlbums.indexOf(PlayInfoManager.getInstance().getCurrentAlbum());
            }
        }
    }
}
