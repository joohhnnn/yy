package com.txznet.music.albumModule.ui;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.loader.AppLogic;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Category;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.CategoryEngine;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.albumModule.ui.adapter.AlbumDecoration;
import com.txznet.music.albumModule.ui.adapter.CategoryAlbumAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumBaseAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumClassifyAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumFragmentAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumRankListAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumRecommandAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumSingerAdapter;
import com.txznet.music.albumModule.ui.adapter.RadioTypeAdapter;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.novelModule.NovelActivity;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.LoadingView;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity2;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity7;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import butterknife.Bind;

/**
 * Created by telenewbie on 2017/12/21.
 */

public class SingleRadioFragmentV42 extends BaseFragment {
    //增加一个小说分类的不同Adapter,产品真的瞎改
    private static final int CATEGORY_NOVEL = 500000;//小说分类的
    protected int lastVisibleItem;
    //子分类的视图
    @Bind(R.id.recycler_radio)
    RecyclerView mCategoryRecycleView;
    @Bind(R.id.layout_sub)
    RelativeLayout layoutHeadRadio;
    //专辑的视图
    @Bind(R.id.recycler_album)
    RecyclerView mALbumRecycleView;
    @Bind(R.id.layout_library_loading_view)
    LoadingView layoutLibraryLoadingView;
    //    两个Adapter,一个是category的,一个是专辑的
    List<Category> mCategorys = new ArrayList<>();
    List<Album> mAlbum = new ArrayList<>();
    CategoryAlbumAdapter mCategoryAlbumAdapter;
    private TXZLinearLayoutManager mCategoryLayoutManager;
    private RadioTypeAdapter mCategoryAdapter;
    private Category mCategory;
    private TXZLinearLayoutManager mAlbumLayoutManager;
    private ItemAlbumBaseAdapter mAlbumAdapter;
    private int pageOff;
    private int playindex = -1;

    @Override
    public void reqData() {
        layoutLibraryLoadingView.showLoading(R.drawable.fm_album_loading_rotate, R.drawable.fm_album_loading_icon, "正在加载中...");
        CategoryEngine.getInstance().queryCategory();
    }

    @Override
    public void bindViews() {

    }

    @Override
    public void initListener() {
        layoutLibraryLoadingView.setErrorHintListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reqData();
            }
        });
    }

    @Override
    public void initData() {
        pageOff = 0;
        layoutHeadRadio.setVisibility(View.VISIBLE);

        //设置类别category的recycleview
        mCategoryLayoutManager = new TXZLinearLayoutManager(GlobalContext.get());
        mCategoryLayoutManager.setOrientation(TXZLinearLayoutManager.HORIZONTAL);
        mCategoryRecycleView.setLayoutManager(mCategoryLayoutManager);
        mCategoryAdapter = new RadioTypeAdapter(this, mCategorys);
        mCategoryRecycleView.setAdapter(mCategoryAdapter);
        mCategoryAdapter.setSelectedIndex(0);

        mCategoryAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mCategoryAdapter.getSelectedPosition() == position) {//点击相同的模块
                    return;
                }
                mAlbumAdapter = null;
                mCategoryAdapter.updateItemSBState(false);
                mCategoryAdapter.setSelectedIndex(position);
                mCategoryAdapter.notifyDataSetChanged();
                mCategory = mCategoryAdapter.getItem(position);
                layoutLibraryLoadingView.showLoading(R.drawable.fm_album_loading_rotate, R.drawable.fm_album_loading_icon, "正在加载中...");
                pageOff = 1;
//                mAlbum.clear();
                AlbumEngine.getInstance().queryAlbum(mCategory.getCategoryId(), pageOff);
                ReportEvent.clickRadioCategory(mCategory.getCategoryId());
            }
        });


        //设置内容album的recycleview
        mAlbumLayoutManager = new TXZLinearLayoutManager(GlobalContext.get());
        mAlbumLayoutManager.setOrientation(TXZLinearLayoutManager.HORIZONTAL);
        mAlbumLayoutManager.scrollToPosition(0);
        mAlbumLayoutManager.scrollToPositionWithOffset(0, 0);
        mALbumRecycleView.setLayoutManager(mAlbumLayoutManager);
        mALbumRecycleView.addItemDecoration(new AlbumDecoration((int) AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_gap, 0)));
        mALbumRecycleView.setHasFixedSize(true);

        mALbumRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isNewClick = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (null != mAlbumAdapter && mCategory != null && !mAlbumAdapter.isShowLoading() && newState == 0 && lastVisibleItem + 1 >= mAlbumAdapter.getItemCount() && isNewClick) {
                    mAlbumAdapter.setShowLoading(true);
                    AlbumEngine.getInstance().queryAlbum(mCategory.getCategoryId(), pageOff + 1);
                }
                if (newState == 0) {
                    isNewClick = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                lastVisibleItem = mAlbumLayoutManager.findLastVisibleItemPosition();
                isNewClick = true;
            }

        });

        layoutLibraryLoadingView.setErrorHintListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reqData();
            }
        });
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_album_v42;
    }

    @Override
    public String getFragmentId() {
        return "SingleRadioFragmentV42" + this.hashCode();
    }

    @Override
    public void update(Observable o, Object data) {
        if (data instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) data;
            ItemAlbumBaseAdapter adapter = mAlbumAdapter;
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "reqData:info type:" + info.getType());
            switch (info.getType()) {
                case InfoMessage.REQ_CATEGORY_ALL:
                    handleCategory();
                    break;
                case InfoMessage.NET_ERROR:
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_NET:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_NO_NET:
                    layoutLibraryLoadingView.showError(Constant.RS_VOICE_SPEAK_NONE_NET, R.drawable.fm_me_no_result_network, Constant.RS_VOICE_MUSIC_CLICK_RETRY);
                    break;
                case InfoMessage.NET_TIMEOUT_ERROR:
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_TIMEOUT:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_TIMEOUT:
                    layoutLibraryLoadingView.showError(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT, R.drawable.fm_me_no_result_network, Constant.RS_VOICE_MUSIC_CLICK_RETRY);
                    break;
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_UNKNOWN:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_UNKNOWN:
                    layoutLibraryLoadingView.showError(Constant.RS_VOICE_SPEAK_TIPS_UNKNOWN, R.drawable.fm_me_no_result_network, Constant.RS_VOICE_MUSIC_CLICK_RETRY);
                    break;
                case InfoMessage.RESP_ALBUM:
                    ResponseSearchAlbum responseAlbum = (ResponseSearchAlbum) info.getObj();
                    if (null != mCategory && String.valueOf(mCategory.getCategoryId()).equals(responseAlbum.getCategoryId())) {
                        pageOff = responseAlbum.getPageId();
                        handleAlbum(responseAlbum.getArrAlbum(), pageOff != 1);
                    } else {
                        LogUtil.logw(TAG + "[" + getFragmentId() + "]" + (mCategory != null ? mCategory.getCategoryId() : "null") + "/" + responseAlbum.getCategoryId());
                    }
                    break;
                case InfoMessage.PLAY:
                case InfoMessage.PAUSE:
                    if (PlayInfoManager.getInstance().getCurrentAlbum() != null && adapter != null) {
                        playindex = mAlbum.indexOf(PlayInfoManager.getInstance().getCurrentAlbum());
                        LogUtil.logd(getFragmentId() + "notify item index i=" + playindex);
                        adapter.notifyItemChanged(playindex);
                    }
                    break;

                case InfoMessage.PLAYER_LIST:
                case InfoMessage.PLAYER_LOADING:
                    if (playindex >= 0 && adapter != null) {
                        LogUtil.logd(getFragmentId() + "notify item index " + playindex);
                        adapter.notifyItemChanged(playindex);
                        playindex = -1;
                    }
//
//                    Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
//                    int index = -1;
//                    if (null != currentAlbum) {
//                        for (int i = 0; i < mAlbum.size(); i++) {
//                            if (mAlbum.get(i).getId() == currentAlbum.getId()) {
//                                index = i;
//                                break;
//                            }
//                        }
//                    }
//
//
//                    if (playindex >= 0) {
//                        mAlbumAdapter.notifyItemChanged(playindex);
//                        playindex = -1;
//                    }
//
//                    LogUtil.logd(getFragmentId() + "notify item index " + index);
//                    if (index >= 0) {
//                        mAlbumAdapter.notifyItemChanged(index);
//                        playindex = index;
//                    }

                    break;

            }
        }

    }

    /**
     * 处理category分类
     */
    private void handleCategory() {
        mCategorys.clear();
        mCategorys.addAll(CategoryEngine.getInstance().getRadioCategory());
        mCategoryAdapter.notifyDataSetChanged();

        if (mCategory == null) {
            mCategory = mCategorys.get(0);
            AlbumEngine.getInstance().queryAlbum(mCategory.getCategoryId(), 1);
        }

    }

    /**
     * 处理category分类
     */
    private void handleAlbum(List<Album> albums, boolean isAdd) {
        int startIndex = 0;
        int count = albums.size();

        if (!isAdd) {
            layoutLibraryLoadingView.showContent();
            int removeSize = mAlbum.size();
            mAlbum.clear();
            if (mAlbumAdapter != null) {
                mAlbumAdapter.notifyItemRangeRemoved(0, removeSize);
            }
        } else {
            startIndex = mAlbum.size();
        }
        mAlbum.addAll(albums);

        if (mCategory.getCategoryId() != CATEGORY_NOVEL) {
            if (!isAdd || null == mAlbumAdapter) {
                if (mCategorys != null) {
                    switch (mCategory.getShowStyle()) {
                        case ItemAlbumBaseAdapter.SHOWTYPE_SINGER:
                            mAlbumAdapter = new ItemAlbumSingerAdapter(this, mAlbum);
                            break;
                        case ItemAlbumBaseAdapter.SHOWTYPE_RECOMMAND:
                            mAlbumAdapter = new ItemAlbumRecommandAdapter(this, mAlbum);
                            break;
                        case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_LIST:
                            mAlbumAdapter = new ItemAlbumRankListAdapter(this, mAlbum);
                            break;
                        case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_CLASSIFY:
                            mAlbumAdapter = new ItemAlbumClassifyAdapter(this, mAlbum);
                            break;
                        case ItemAlbumBaseAdapter.SHOWTYPE_UNDEFINE:
                            mAlbumAdapter = new ItemAlbumFragmentAdapter(this, mAlbum);
                            break;
                        case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_OTHER:
                            mAlbumAdapter = new ItemAlbumFragmentAdapter(this, mAlbum);
                            break;
                    }
                    // 设置mAlbumAdapter
                }
                if (null == mAlbumAdapter) {
                    mAlbumAdapter = new ItemAlbumFragmentAdapter(this, mAlbum);
                }
                mAlbumAdapter.setOnItemClickListener(getAlbumItemClickListener());
                mAlbumAdapter.setOnItemIconClickListener(getAlbumItemIconClickListener());
                mALbumRecycleView.setAdapter(mAlbumAdapter);
            }
            mAlbumAdapter.setShowLoading(false);
            mAlbumAdapter.notifyDataSetChanged();
        } else {
            if (!isAdd || null == mCategoryAlbumAdapter) {
                mCategoryAlbumAdapter = new CategoryAlbumAdapter(this, mCategory.getArrChild());
                mCategoryAlbumAdapter.setOnItemClickListener(getCategoryAlbumClick());
                mALbumRecycleView.setAdapter(mCategoryAlbumAdapter);
            }
            mCategoryAlbumAdapter.notifyItemRangeInserted(startIndex, count);
        }

//        mAlbumAdapter.notifyDataSetChanged();
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
                    ReportEvent.clickRadioAlbumCategory(mCategory.getCategoryId());
                }
            }
        };
    }

    protected AdapterView.OnItemClickListener getAlbumItemClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAlbumAdapter != null) {
                    final Album album = (Album) mAlbumAdapter.getItem(position);
                    playALbum(album, false);
                    ReportEvent.clickRadioAlbumIcon(album.getId(), position, null);
                }
            }
        };
    }

    protected AdapterView.OnItemClickListener getAlbumItemIconClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (null != mAlbumAdapter) {
                    final Album album = (Album) mAlbumAdapter.getItem(position);
                    playALbum(album, true);
                    ReportEvent.clickRadioAlbumIconPlay(album.getId(), position, null);
                }
            }
        };
    }

    private void playALbum(final Album album, final boolean needplay) {
        final long categoryID;
        if (CollectionUtils.isNotEmpty(album.getArrCategoryIds())) {
            categoryID = album.getArrCategoryIds().get(0);
        } else {
            categoryID =/*0;*/mCategory.getCategoryId();
        }
        Album playingAlbum = PlayEngineFactory.getEngine().getCurrentAlbum();

        try {
            if (playingAlbum != null && playingAlbum.equals(album)) {
                if (PlayEngineFactory.getEngine().isPlaying()) {
                    return;
                }
                if (PlayEngineFactory.getEngine().getState() == PlayerInfo.PLAYER_STATUS_PAUSE) {
                    PlayEngineFactory.getEngine().play(EnumState.Operation.manual);
                    return;
                }
            }
            PlayEngineFactory.getEngine().release(EnumState.Operation.manual);
            AppLogic.runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    if (Utils.isSong(album.getSid())) {
                        AlbumEngine.getInstance().playAlbum(EnumState.Operation.manual, album, categoryID, needplay, null);
                    } else {
                        AlbumEngine.getInstance().playAlbumWithBreakpoint(EnumState.Operation.manual, album, categoryID, needplay);
                    }
                }
            }, 0);
        } finally {
            if (ActivityStack.getInstance().currentActivity().hashCode() == getActivity().hashCode()) {
                Intent intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity2.class);
                getActivity().startActivity(intent);
            }
        }

    }
}
