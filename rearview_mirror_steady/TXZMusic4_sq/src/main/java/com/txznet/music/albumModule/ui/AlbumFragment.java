package com.txznet.music.albumModule.ui;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dinuscxj.itemdecoration.GridOffsetsItemDecoration;
import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.loader.AppLogic;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Category;
import com.txznet.music.albumModule.bean.HeadTitle;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.CategoryEngine;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.albumModule.ui.adapter.AlbumDecoration;
import com.txznet.music.albumModule.ui.adapter.AlbumSnapHelper;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumBaseAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumClassifyAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumEmptyAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumFragmentAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumRankListAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumRecommandAdapter;
import com.txznet.music.albumModule.ui.adapter.ItemAlbumSingerAdapter;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.layout.TXZGridLayoutManager;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.AnimationUtil;
import com.txznet.music.utils.AttrUtils;
import com.txznet.music.utils.SDKUtil;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity2;
import com.txznet.txz.util.runnables.Runnable1;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;


/**
 * 上面标题栏，下面专辑Item
 * <p>
 * Created by ASUS User on 2016/11/9.
 */
public abstract class AlbumFragment extends BaseFragment implements View.OnClickListener {

    private static final int CATEGORY_NOVEL = 500000;//小说分类的
    protected ItemAlbumBaseAdapter adapter;
    protected TextView mType_category;
    protected Resources mRes;
    protected ImageView mIv_filter;
    protected ImageView ivNoResult;
    protected TextView tvLoading;
    //    protected TextView mType;
//    protected TextView mType_filter;
    protected RecyclerView mRecyclerView;
    //    protected AlbumRecyclerView mRecyclerView;
    protected LinearLayout llLoadingLayout;
    protected ImageView ivLoading, ivLoadingCenterIcon;
    protected LinearLayout llErrorView;
    protected Button btnRefresh;
    protected TextView mTvShowTips;
    protected RelativeLayout llAlbumContent;
    protected View popView;
    protected List<Category> mCategories = new ArrayList<Category>();// 选择类目
    protected Category mCategory;
    protected int pageOff = 1;// 当前页码数,从1 开始
    // private AsyncTask<Void, Void, List<Album>> getAlbumAndRequestTask;
    protected int lastVisibleItem;
    protected RecyclerView.LayoutManager manager;
    protected List<Album> albums = new ArrayList<Album>();

    private AlbumSnapHelper mAlbumSnapHelper;
    private boolean isShowLoading = false;
    private int playindex = -1;

    abstract void onCategoryUpdate();

    @Override
    public void bindViews() {
        mRes = getActivity().getResources();

        llLoadingLayout = (LinearLayout) findViewById(R.id.ll_loading);
        tvLoading = (TextView) findViewById(R.id.tv_loading);

        ivLoading = (ImageView) findViewById(R.id.iv_loading);
        ivLoadingCenterIcon = (ImageView) findViewById(R.id.iv_loading_center_icon);
        llErrorView = (LinearLayout) findViewById(R.id.ll_error);
        btnRefresh = (Button) llErrorView.findViewById(R.id.btn_refresh);
        mTvShowTips = (TextView) llErrorView.findViewById(R.id.tv_showtips);
        ivNoResult = (ImageView) llErrorView.findViewById(R.id.iv_no_result);

        llAlbumContent = (RelativeLayout) findViewById(R.id.ll_album_list);
//        mType = (TextView) findViewById(R.id.frag_title);
//        mType_category = (TextView) findViewById(R.id.frag_type);
//        mType_filter = (TextView) findViewById(R.id.tv_filter_name);
//        mIv_filter = (ImageView) findViewById(R.id.iv_filter);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);


//        mType.setText(mRes.getString(R.string.music_title_text));
//        mType.setTextColor(mRes.getColor(R.color.title_text_color));
//        mType.setTextSize(TypedValue.COMPLEX_UNIT_PX, mRes.getDimension(R.dimen.title_text_size));
//
//        mType_filter.setTextColor(mRes.getColor(R.color.filter_name_text_color));
//        mType_filter.setTextSize(TypedValue.COMPLEX_UNIT_PX, mRes.getDimension(R.dimen.filter_text_size));
//
//        mIv_filter.setImageDrawable(mRes.getDrawable(R.drawable.fm_item_screening_down_1));


//        ivNoResult.setImageDrawable(mRes.getDrawable(R.drawable.fm_me_no_result_network));

//        mTvShowTips.setTextColor(mRes.getColor(R.color.error_tip_color));
//        int size = (int) AttrUtils.getAttrDimension(getActivity(), R.attr.text_size_h3, 0);
//        mTvShowTips.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
        ivLoading.setImageDrawable(mRes.getDrawable(R.drawable.fm_album_loading_rotate));
        ivLoadingCenterIcon.setImageDrawable(mRes.getDrawable(R.drawable.fm_album_loading_icon));
        tvLoading.setText(mRes.getString(R.string.loading_text));
        tvLoading.setTextSize(TypedValue.COMPLEX_UNIT_PX, mRes.getDimension(R.dimen.loading_text_size));
        tvLoading.setTextColor(mRes.getColor(R.color.loading_text_color));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public abstract void initTitleView();

    public void initPopView() {
        popView = View.inflate(getActivity(), R.layout.pop_type_select_layout,
                null);
        ImageView ivClose = (ImageView) popView.findViewById(R.id.close);
        ivClose.setImageDrawable(mRes.getDrawable(R.drawable.fm_item_close));
    }

    /**
     * 刷新数据
     *
     * @param result 添加的值
     * @param add    是否累加
     */
    private void notifyAlbum(List<Album> result, boolean add) {
        notifyAlbum(result, add, true, true);
    }

    /**
     * 刷新数据
     *
     * @param result 添加的值
     * @param add    是否累加
     */
    private void notifyAlbum(final List<Album> result, final boolean add, final boolean showNodataView, boolean needTips) {
        LogUtil.logd(TAG + "[" + getFragmentId() + "] notify albums size =" + (result != null ? result.size() : 0) + ", is add?:" + add);
//        if (this instanceof SingleMusicFragment) {


        if (!add || null == adapter || adapter instanceof ItemAlbumEmptyAdapter) {
            if (mCategories != null) {
//                if (mCategory.getCategoryId() != CATEGORY_NOVEL) {
                switch (mCategory.getShowStyle()) {
                    case ItemAlbumBaseAdapter.SHOWTYPE_SINGER:
                        adapter = new ItemAlbumSingerAdapter(this, albums);
                        break;
                    case ItemAlbumBaseAdapter.SHOWTYPE_RECOMMAND:
                        adapter = new ItemAlbumRecommandAdapter(this, albums);
                        break;
                    case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_LIST:
                        adapter = new ItemAlbumRankListAdapter(this, albums);
                        break;
                    case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_CLASSIFY:
                        adapter = new ItemAlbumClassifyAdapter(this, albums);
                        break;
                    case ItemAlbumBaseAdapter.SHOWTYPE_UNDEFINE:
                        adapter = new ItemAlbumFragmentAdapter(this, albums);
                        break;
                    case ItemAlbumBaseAdapter.SHOWTYPE_RANKING_OTHER:
                        adapter = new ItemAlbumFragmentAdapter(this, albums);
                        break;
                }
                if (null == adapter) {
                    adapter = new ItemAlbumFragmentAdapter(this, albums);
                }
                // 设置adapter
                adapter.setOnItemClickListener(getAlbumItemClickListener());
                adapter.setOnItemIconClickListener(getAlbumItemIconClickListener());
//                } else {
//                    adapter=new
//                }
                mRecyclerView.setAdapter(adapter);
            }
//            adapter.setShowType(mCategory.getShowStyle());
//
//                if (mCategory.getShowStyle()== SHOWTYPE_SINGER) {
//                    adapter.setShowType(ItemAlbumAdapter.SHOWTYPE_RECOMMAND);
//                } else if (mCategories.size() > 1 && mCategories.get(1).equals(mCategory)) {
//                    adapter.setShowType(ItemAlbumAdapter.SHOWTYPE_RANKING_LIST);
//                } else if (mCategories.size() > 2 && mCategories.get(2).equals(mCategory)) {
//                    adapter.setShowType(SHOWTYPE_SINGER);
//                } else if (mCategories.size() > 3 && mCategories.get(3).equals(mCategory)) {
//                    adapter.setShowType(ItemAlbumAdapter.SHOWTYPE_RANKING_CLASSIFY);
//                }
        }
//        } else {
//            adapter.setShowType(ItemAlbumAdapter.SHOWTYPE_RECOMMAND);
//        }

        if (!add) {
            ImageFactory.getInstance().clearMemory();
            showLoadingView(false);
            albums.clear();
            adapter.notifyDataSetChanged();
        } else {
            adapter.setShowLoading(false);
        }
        if (CollectionUtils.isNotEmpty(result)) {
            int insertIndex = albums.size();
            albums.addAll(result);
            adapter.notifyItemRangeInserted(insertIndex, result.size());
//            adapter.notifyDataSetChanged();
        } else if (needTips) {
            ToastUtils.showShort(Constant.RS_VOICE_MUSIC_NO_MORE_DATA);
        }

        if (showNodataView && CollectionUtils.isEmpty(albums)) {
            // 展示没有数据
            showNodataView();
        }


//        adapter.notifyDataSetChanged();

    }

    @Override
    public void reqData() {
        showLoadingView(true);
        CategoryEngine.getInstance().queryCategory();
    }

    @Override
    public void initListener() {
        btnRefresh.setOnClickListener(this);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean isNewClick = false;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (null != adapter && mCategory != null && !adapter.isShowLoading() && newState == 0 && lastVisibleItem + 1 >= adapter.getItemCount() && isNewClick) {
                    adapter.setShowLoading(true);
                    AlbumEngine.getInstance().queryAlbum(mCategory.getCategoryId(), getPageOff() + 1);
                }
                if (newState == 0) {
                    isNewClick = false;
                }
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (manager instanceof TXZGridLayoutManager) {
                    lastVisibleItem = ((GridLayoutManager) manager).findLastVisibleItemPosition();
                } else if (manager instanceof LinearLayoutManager) {
                    lastVisibleItem = ((LinearLayoutManager) manager).findLastVisibleItemPosition();
                }
                isNewClick = true;
            }

        });

    }

    public int getPageOff() {
        return pageOff;
    }

    @Override
    public void initData() {
        pageOff = 0;
        mCategories.clear();
        RecyclerView.ItemDecoration itemDecoration = null;
        if (ScreenUtils.getScreenType() == ScreenUtils.TYPE_VERTICAL) {
            manager = new TXZGridLayoutManager(getActivity(), 4);
            itemDecoration = new GridOffsetsItemDecoration(GridOffsetsItemDecoration.GRID_OFFSETS_VERTICAL);
            ((GridOffsetsItemDecoration) itemDecoration).setHorizontalItemOffsets(200);
            ((GridOffsetsItemDecoration) itemDecoration).setVerticalItemOffsets((int) AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_gap, 0));
//            itemDecoration=new GridSpacingItemDecoration(4,30,false);
        } else {
            manager = new TXZLinearLayoutManager(GlobalContext.get(), LinearLayoutManager.HORIZONTAL, false);
            itemDecoration = new AlbumDecoration((int) AttrUtils.getAttrDimension(getActivity(), R.attr.album_item_gap, 0));
        }
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addItemDecoration(itemDecoration);


        mRecyclerView.setHasFixedSize(true);

        mAlbumSnapHelper = new AlbumSnapHelper();

        mAlbumSnapHelper.attachToRecyclerView(mRecyclerView);
        adapter = new ItemAlbumEmptyAdapter(null);
        adapter.notifyDataSetChanged();


        // 设置Item增加、移除动画
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.scrollToPosition(0);

        initPopView();
        initTitleView();
    }

    protected AdapterView.OnItemClickListener getAlbumItemClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Album album = (Album) adapter.getItem(position);

                LogUtil.logd(TAG + "[" + getFragmentId() + "]item onItemClick:" + album.getName() + "(" + position + ")");
                final long categoryID;
                if (CollectionUtils.isNotEmpty(album.getArrCategoryIds())) {
                    categoryID = album.getArrCategoryIds().get(0);
                } else {
                    categoryID =/*0;*/mCategory.getCategoryId();
                }
                onAlbumItemClicked();
                Album playingAlbum = PlayEngineFactory.getEngine().getCurrentAlbum();

                try {
                    if (playingAlbum != null && playingAlbum.equals(album)) {
                        return;
                    }
                    PlayEngineFactory.getEngine().release(EnumState.Operation.manual);
                    AppLogic.runOnBackGround(new Runnable() {
                        @Override
                        public void run() {
                            if (Utils.isSong(album.getSid())) {
                                AlbumEngine.getInstance().playAlbum(EnumState.Operation.manual, album, categoryID, false, null);
                            } else {
                                AlbumEngine.getInstance().playAlbumWithBreakpoint(EnumState.Operation.manual, album, categoryID, false);
                            }
                        }
                    }, 0);
                } finally {
                    if (ActivityStack.getInstance().currentActivity().hashCode() == getActivity().hashCode()) {
                        ReportEvent.clickMusicAlbumIcon(album.getId(), position, null);
                        Intent intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity2.class);
                        getActivity().startActivity(intent);
                    }
                }
            }
        };
    }

    protected AdapterView.OnItemClickListener getAlbumItemIconClickListener() {
        return new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final Album album = (Album) adapter.getItem(position);

                if (null == album) {
                    return;
                }

                LogUtil.logd(TAG + "[" + getFragmentId() + "] play icon ClickListener --- onItemClick:" + album.getName() + "(" + position + ")");
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
//                ImageFactory.getInstance().setStyle(IImageLoader.BLUR_FILTER);
//                ((SingleActivity) getActivity()).setChoiceBgVisible(true);
//                ImageFactory.getInstance().display(AlbumFragment.this, album.getLogo(), ((SingleActivity) getActivity()).getChoiceBg(), R.drawable.bg);


                    PlayEngineFactory.getEngine().release(EnumState.Operation.manual);
                    AppLogic.runOnBackGround(new Runnable() {
                        @Override
                        public void run() {
                            if (Utils.isSong(album.getSid())) {
                                AlbumEngine.getInstance().playAlbum(EnumState.Operation.manual, album, categoryID, null);
                            } else {
                                AlbumEngine.getInstance().playAlbumWithBreakpoint(EnumState.Operation.manual, album, categoryID);
                            }
                        }
                    }, 0);
                } finally {
                    if (ActivityStack.getInstance().currentActivity().hashCode() == getActivity().hashCode()) {
                        ReportEvent.clickMusicAlbumPlay(album.getId(), position);
                        Intent intent = new Intent(getActivity(), ReserveConfigSingleTaskActivity2.class);
                        getActivity().startActivity(intent);
                    }
                }
            }
        };
    }

    @Override
    public int getLayout() {
        return R.layout.fragment_album;
    }

    @Override
    public String getFragmentId() {
        if (mCategory != null) {
            return "AlbumFragment#" + this.hashCode() + "/" + mCategory.getDesc();
        } else {
            return "AlbumFragment#" + this.hashCode();
        }
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) data;
            LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "reqData:info type:" + info.getType());
            switch (info.getType()) {
                case InfoMessage.REQ_CATEGORY_ALL:
                    handleCategory();
                    break;
                case InfoMessage.RESP_ALBUM:
                    ResponseSearchAlbum responseAlbum = (ResponseSearchAlbum) info.getObj();
                    if (null != mCategory && String.valueOf(mCategory.getCategoryId()).equals(responseAlbum.getCategoryId())) {
                        pageOff = responseAlbum.getPageId();
                        if (pageOff == 1) {// 如果是第一页
                            notifyAlbum(responseAlbum.getArrAlbum(), false);
                        } else {
                            notifyAlbum(responseAlbum.getArrAlbum(), true);
                        }
                    } else {
                        LogUtil.logw(TAG + "[" + getFragmentId() + "]" + (mCategory != null ? mCategory.getCategoryId() : "null") + "/" + responseAlbum.getCategoryId());
                    }
                    break;
                case InfoMessage.NET_ERROR:
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_NET:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_NO_NET:
                    showNetTimeOutView(Constant.RS_VOICE_SPEAK_NONE_NET);
                    break;
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_NO_DATA:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_NO_DATA:
                    updateCurrentAlbum(PlayEngineFactory.getEngine().getCurrentAlbum());
                    break;
                case InfoMessage.NET_TIMEOUT_ERROR:
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_TIMEOUT:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_TIMEOUT:
                    showNetTimeOutView(Constant.RS_VOICE_SPEAK_TIPS_TIMEOUT);
                    break;
                case InfoMessage.RESP_ALBUM_AUDIO_ERROR_UNKNOWN:
                case InfoMessage.RESP_ALBUM_LIST_ERROR_UNKNOWN:
                    showNetTimeOutView(Constant.RS_VOICE_SPEAK_TIPS_UNKNOWN);
                    break;
                case InfoMessage.PLAY:
                case InfoMessage.PAUSE:
                    if (PlayInfoManager.getInstance().getCurrentAlbum() != null) {
                        playindex = albums.indexOf(PlayInfoManager.getInstance().getCurrentAlbum());
                        LogUtil.logd(getFragmentId() + "notify item index i=" + playindex);
                        adapter.notifyItemChanged(playindex);
                    }
                    break;

                case InfoMessage.PLAYER_LIST:
                case InfoMessage.PLAYER_LOADING:
                    if (playindex >= 0) {
                        LogUtil.logd(getFragmentId() + "notify item index " + playindex);
                        adapter.notifyItemChanged(playindex);
                        playindex = -1;
                    }
                default:
                    break;
            }
        }
    }

    public abstract List<Category> getCategories();

    public abstract Category getCurrentCategory(Category category);

    public abstract void onTitleClickListener(HeadTitle title);

    protected void onAlbumItemClicked() {
    }

    private void handleCategory() {
        List<Category> categories = getCategories();
        if (categories == null || categories.isEmpty()) {
            LogUtil.logd(TAG + "[" + getFragmentId() + "]category is null or empty");
            return;
        }
        mCategories.clear();
        mCategories.addAll(categories);
        onCategoryUpdate();
        if (mCategory == null) {
            mCategory = mCategories.get(0);
//			if (mCategory.getArrChild() != null && mCategory.getArrChild().size() > 0) {
//				mCategory =  mCategory.getArrChild().get(0);
//			}
            AlbumEngine.getInstance().queryAlbum(mCategory.getCategoryId(), 1);
        }
    }



    public void hideNetErrorView() {
        showLoadingView(true);
        if (null != llErrorView) {
            llErrorView.setVisibility(View.GONE);
        }
    }

    public void showNetTimeOutView(String tips) {
        LogUtil.logd(TAG + "[" + getFragmentId() + "]" + " showNetTimeOutView "
                + "[tip]:" + tips + ",albums:"
                + (albums != null ? albums.size() : 0));
        if (CollectionUtils.isEmpty(albums)) {
            setViewVisible(llErrorView);
            mTvShowTips.setText(tips);
            btnRefresh.setVisibility(View.VISIBLE);
        } else {
            adapter.setShowLoading(false);
        }
        ivNoResult.setImageDrawable(mRes.getDrawable(R.drawable.fm_me_no_result_network));
    }

    /**
     * 更新正在播放的专辑列表
     *
     * @param album
     */
    public void updateCurrentAlbum(final Album album) {
        AppLogic.runOnUiGround(new Runnable1<Album>(album) {
            @Override
            public void run() {
                Album currentAlbum = adapter.getCurrentPlayingAlbum();


                if (currentAlbum != null && album != null && album.equals(currentAlbum)) {

//                    adapter.updateCurrentAlbum(album);
                    int oldIndex = albums.indexOf(currentAlbum);
                    int newIndex = albums.indexOf(album);
                    if (newIndex >= 0 && newIndex < albums.size()) {
                        adapter.notifyItemChanged(newIndex);
                    }
                    if (oldIndex != newIndex) {
                        if (oldIndex >= 0 && oldIndex < albums.size()) {
                            adapter.notifyItemChanged(oldIndex);
                        }
                    }
                } else if (currentAlbum == null && album == null) {
                    return;
                } else {
//                    adapter.updateCurrentAlbum(album);
                    adapter.notifyDataSetChanged();
                }
            }
        }, 0);
    }

    /**
     * 没有数据的时候展示
     */
    public void showNodataView() {
        LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "[view][empty]");
        MonitorUtil.monitorCumulant(Constant.M_EMPTY_ALBUM);
        mTvShowTips.setText("当前分类没有数据");
        setViewVisible(llErrorView);
        btnRefresh.setVisibility(View.GONE);
        ivNoResult.setImageDrawable(mRes.getDrawable(R.drawable.fm_me_no_file));
    }

    /**
     * 是否显示加载中图像
     *
     * @param show
     */
    public void showLoadingView(boolean show) {
        LogUtil.logd(TAG + "[" + getFragmentId() + "]" + "[view][load]" + show);
        isShowLoading = show;
        if (show) {
            setViewVisible(llLoadingLayout);
            Animation animation = AnimationUtil.createSmoothForeverAnimation(getActivity());
            ivLoading.startAnimation(animation);
        } else {
            ivLoading.clearAnimation();
            setViewVisible(llAlbumContent);
        }
    }

    /**
     * 设置是否显示界面
     */
    public void setViewVisible(View view) {
        llErrorView.setVisibility(View.GONE);
        llLoadingLayout.setVisibility(View.GONE);
        llAlbumContent.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ll_filter:
                // 弹出框
                break;
            case R.id.btn_refresh:
                hideNetErrorView();
                CategoryEngine.getInstance().queryCategory();
                if (mCategory != null) {
                    AlbumEngine.getInstance().queryAlbum(mCategory.getCategoryId(), 1);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {

        } else {
//            if (PlayEngineFactory.getEngine().getCurrentAlbum() != null) {
//                adapter.updateCurrentAlbum(PlayEngineFactory.getEngine().getCurrentAlbum());
//            }
            adapter.notifyDataSetChanged();
            CategoryEngine.getInstance().queryCategory();
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }


}
