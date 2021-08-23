package com.txznet.music.ui.tab;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dinuscxj.itemdecoration.GridOffsetsItemDecoration;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.AlbumActionCreator;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.PageItemData;
import com.txznet.music.data.entity.PageItemDataGroup;
import com.txznet.music.helper.GlideHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.SysPageItemClickEvent;
import com.txznet.music.store.PlayInfoStore;
import com.txznet.music.util.IvRecycler;
import com.txznet.music.util.ToastUtils;
import com.txznet.music.util.adapter.RecyclerAdapter;
import com.txznet.music.widget.PlayingStateView;
import com.txznet.rxflux.Operation;

import java.util.ArrayList;
import java.util.List;

/**
 * 音乐分类tab
 *
 * @author zackzhou
 * @date 2019/1/7,12:09
 */

public class MusicCategoryTab extends BaseTab {

    private FragmentActivity mCtx;
    private RecyclerAdapter<PageItemData> mMusicCategoryAdapter;
    private List<PageItemData> mMusicCategoryList = new ArrayList<>(0);
    private PageItemDataGroup mPageItemDataGroup;
    private int bReqPageId;

    private Album mAlbum;
    private boolean isPlaying;

    private IvRecycler mIvRecycler = new IvRecycler();

    public MusicCategoryTab(FragmentActivity ctx) {
        this.mCtx = ctx;
    }

    private boolean hasBind;

    private void prepareTab() {
        if (mMusicCategoryList.isEmpty()) {
            for (int i = 0; i < 15; i++) {
                mMusicCategoryList.add(null);
            }
        }

        PlayInfoStore playInfoStore = ViewModelProviders.of(mCtx).get(PlayInfoStore.class);
        playInfoStore.getAlbum().observe(mCtx, album -> {
            mAlbum = album;
            refreshView();
        });
        playInfoStore.isPlayingStrict().observe(mCtx, isPlaying -> {
            this.isPlaying = isPlaying != null && isPlaying;
            refreshView();
        });
    }

    /**
     * 刷新数据
     */
    public void refreshData(PageItemDataGroup data) {
        mPageItemDataGroup = data;
        if (data != null && data.arrAlbum != null) {
            mMusicCategoryList = data.arrAlbum;
            if (mMusicCategoryAdapter != null) {
                mMusicCategoryAdapter.refresh(getNextPage());
            }
        }
    }

    /**
     * 刷新数据
     */
    public void dispatchRefreshData(PageItemDataGroup data) {
        if (mPageItemDataGroup == null) {
            return;
        }
        if (data.categoryId == mPageItemDataGroup.categoryId) {
            //如果是同一个分类则需要累加
        } else {
            //如果不是同一个分类则需要清空在添加
            return;
        }
        if (bReqPageId != data.pageId) {
            //请求的页数，和返回的页数不是一致的
            return;
        }
        data.pageNum = mPageItemDataGroup.pageNum;
        data.sid = mPageItemDataGroup.sid;
        refreshData(data);
    }

    /**
     * 换一批
     */
    public void nextPage() {
        if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_ASR_NET_OFFLINE);
            return;
        }
        if (mPageItemDataGroup == null) {
            return;
        }
        bReqPageId = mPageItemDataGroup.pageId + 1;
        if (bReqPageId > mPageItemDataGroup.pageNum) {
            bReqPageId = 1;
        }
        AlbumActionCreator.getInstance().getAlbumByCategory(Operation.MANUAL, bReqPageId, mPageItemDataGroup.sid, mPageItemDataGroup.categoryId, 15);
    }

    private List<PageItemData> getNextPage() {
        return mMusicCategoryList;
    }

    private void refreshView() {
        if (mMusicCategoryAdapter != null) {
            mMusicCategoryAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder) {
        if (hasBind) {
            return;
        }
        hasBind = true;

        prepareTab();

        holder.itemView.findViewById(R.id.btn_category_switch).setOnClickListener(v -> {
            nextPage();
            ReportEvent.reportMusicCategorySwitch();
        });
        RecyclerView recyclerView = holder.itemView.findViewById(R.id.rv_category);
        GridLayoutManager layoutManager = new GridLayoutManager(mCtx, 5);
        recyclerView.setLayoutManager(layoutManager);
        mMusicCategoryAdapter = new RecyclerAdapter<PageItemData>(mCtx, getNextPage(), R.layout.home_recycle_item_category) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder holder, int position, PageItemData item) {
                ImageView iv_logo = (ImageView) holder.getView(R.id.iv_logo);
                if (item != null) {
                    TextView tv_name = (TextView) holder.getView(R.id.tv_name);
                    PlayingStateView iv_playing = (PlayingStateView) holder.getView(R.id.iv_playing);
                    GlideHelper.loadWithMask(mCtx, item.logo, R.drawable.home_default_cover_icon_strip_large_corners, iv_logo);
                    mIvRecycler.mark(iv_logo, item.logo, R.drawable.home_default_cover_icon_strip_large_corners);
                    tv_name.setText(item.name);

                    if (mAlbum == null || mAlbum.sid != item.sid || mAlbum.id != item.id) {
                        iv_playing.onPause();
                        iv_playing.setVisibility(View.GONE);
                    } else {
                        iv_playing.setVisibility(View.VISIBLE);
                        if (isPlaying) {
                            iv_playing.onPlay();
                        } else {
                            iv_playing.onPause();
                        }
                    }

                    holder.itemView.setOnClickListener(v -> {
                        if (iv_playing.isPlaying()) {
                            PlayerActionCreator.get().pause(Operation.MANUAL);
                        } else {
                            Album album = new Album();
                            album.name = item.name;
                            album.sid = item.sid;
                            album.id = item.id;
                            album.arrArtistName = item.arrArtistName;
                            album.albumType = item.albumType;
                            album.logo = item.logo;
                            ReportEvent.reportPageItemClick(SysPageItemClickEvent.PAGE_TYPE_MUSIC_CATEGORY, item.posId, album);
                            PlayerActionCreator.get().playAlbum(Operation.MANUAL, album);
                        }
                    });
                } else {
                    GlideHelper.loadWithCorners(mCtx, null, R.drawable.home_default_cover_icon_strip_large, iv_logo);
                }
            }

            @Override
            public long getItemId(int position) {
                return position;
            }
        };
        mMusicCategoryAdapter.setHasStableIds(true);
        recyclerView.setAdapter(mMusicCategoryAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);
        GridOffsetsItemDecoration decoration = new GridOffsetsItemDecoration(GridOffsetsItemDecoration.GRID_OFFSETS_VERTICAL);
        decoration.setOffsetEdge(false);
        decoration.setOffsetLast(false);
        TypedValue typedValue = new TypedValue();
        mCtx.getTheme().resolveAttribute(R.attr.page_item_offset_1_1_1, typedValue, true);
        int offset = (int) typedValue.getDimension(mCtx.getResources().getDisplayMetrics());
        decoration.setVerticalItemOffsets(offset);
        decoration.setHorizontalItemOffsets(mCtx.getResources().getDimensionPixelOffset(R.dimen.m12));
        recyclerView.addItemDecoration(decoration);
    }

    @Override
    public void onViewAttachedToWindow(boolean includeGlide) {
        if (includeGlide) {
            mIvRecycler.resumeAll(mCtx, (iv, url, res) -> {
                GlideHelper.loadWithMask(mCtx, url, R.drawable.home_default_cover_icon_strip_large_corners, iv);
            });
        }
    }

    @Override
    public void onViewDetachedFromWindow(boolean includeGlide, boolean cancelReq) {
        if (includeGlide) {
            mIvRecycler.recycleAll(mCtx, iv -> {
//            GlideApp.with(mCtx).clear(iv);
            }, cancelReq);
        }
    }
}
