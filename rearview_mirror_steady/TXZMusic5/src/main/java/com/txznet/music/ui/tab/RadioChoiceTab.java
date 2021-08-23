package com.txznet.music.ui.tab;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.dinuscxj.itemdecoration.GridOffsetsItemDecoration;
import com.txznet.music.R;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.BillboardData;
import com.txznet.music.data.entity.CategoryItemData;
import com.txznet.music.data.entity.PageItemData;
import com.txznet.music.helper.GlideHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.BillBoardClickEvent;
import com.txznet.music.report.entity.BillBoardContentClickEvent;
import com.txznet.music.report.entity.SysPageItemClickEvent;
import com.txznet.music.store.PlayInfoStore;
import com.txznet.music.ui.album.AlbumFragment;
import com.txznet.music.util.IvRecycler;
import com.txznet.music.util.adapter.RecyclerAdapter;
import com.txznet.music.widget.PlayingStateView;
import com.txznet.rxflux.Operation;

import java.util.ArrayList;
import java.util.List;

/**
 * 电台精选页
 *
 * @author zackzhou
 * @date 2019/1/7,10:50
 */

public class RadioChoiceTab extends BaseTab {

    private FragmentActivity mCtx;

    private RecyclerAdapter<PageItemData> mRadioChoiceAdapter;
    private RecyclerAdapter<PageItemData> mRadioBillboardAdapter;
    private GridLayoutManager mLayoutManager;

    private List<PageItemData> mRadioChoiceList = new ArrayList<>(0);
    private int mPageIndex = -1;

    private List<PageItemData> mRadioBillbroadList = new ArrayList<>();
    private TextView mBillboardName;
    private BillboardData mBillboard;
    private FragmentManager mFragmentManager;

    private Album mAlbum;
    private boolean isPlaying;

    private IvRecycler mIvRecycler = new IvRecycler();

    public RadioChoiceTab(FragmentActivity ctx, FragmentManager fragmentManager) {
        this.mCtx = ctx;
        this.mFragmentManager = fragmentManager;
    }

    private boolean hasBind;

    private void prepareTab() {
        if (mRadioBillbroadList.isEmpty()) {
            for (int i = 0; i < 3; i++) {
                mRadioBillbroadList.add(null);
            }
        }
        if (mRadioChoiceList.isEmpty()) {
            for (int i = 0; i < 6; i++) {
                mRadioChoiceList.add(null);
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

    private void showAlbumList(BillboardData billboardData) {
        CategoryItemData itemData = new CategoryItemData();
        itemData.desc = billboardData.boardName;
        itemData.sid = billboardData.sid;
        itemData.categoryId = billboardData.categoryId;
        AlbumFragment.getAlbumFragment(itemData, "radioChoiceBillboard").show(mFragmentManager, "Album");
    }

    // 播放专辑数据
    private void playItemData(PageItemData item) {
        Album album = new Album();
        album.name = item.name;
        album.sid = item.sid;
        album.id = item.id;
        album.arrArtistName = item.arrArtistName;
        album.albumType = item.albumType;
        album.logo = item.logo;
        PlayerActionCreator.get().playAlbum(Operation.MANUAL, album);
    }

    /**
     * 刷新榜单
     */
    public void refreshBillboard(BillboardData data) {
        if (data != null) {
            mBillboard = data;
            if (mBillboardName != null) {
                mBillboardName.setText(mBillboard.boardName);
            }

            mRadioBillbroadList = mBillboard.arrAlbum;
            if (mRadioBillbroadList != null && mRadioBillboardAdapter != null) {
                mRadioBillboardAdapter.refresh(mRadioBillbroadList);
            }
        }
    }

    /**
     * 刷新数据
     */
    public void refreshData(List<PageItemData> data) {
        if (data != null) {
            if (data.size() <= 6) {
                mRadioChoiceList = data;
            } else {
                mRadioChoiceList = data.subList(0, data.size() / 6 * 6);
            }
            nextPage();
        }
    }

    /**
     * 下一页
     */
    private void nextPage() {
        if (mRadioChoiceAdapter != null) {
            mRadioChoiceAdapter.refresh(getNextPage());
            mRadioChoiceAdapter.notifyDataSetChanged();
        }
    }

    private List<PageItemData> getNextPage() {
        if (mRadioChoiceList.isEmpty()) {
            return mRadioChoiceList;
        }
        List<PageItemData> subList;
        if (mRadioChoiceList.size() < 6) {
            int spanCount = mRadioChoiceList.size() / 2;
            if (spanCount == 0) {
                spanCount = 1;
            }
            if (mLayoutManager != null) {
                mLayoutManager.setSpanCount(spanCount);
            }
            subList = mRadioChoiceList.subList(0, mRadioChoiceList.size() / 2 * 2);
        } else {
            mPageIndex++;
            int start = mPageIndex * 2 * 3 % mRadioChoiceList.size();
            int end = start + 2 * 3;
            if (end > mRadioChoiceList.size()) {
                end = mRadioChoiceList.size();
                mPageIndex = -1;
            }
            if (mLayoutManager != null) {
                mLayoutManager.setSpanCount(3);
            }
            subList = mRadioChoiceList.subList(start, end);
        }

        List<PageItemData> sortList = new ArrayList<>();
        for (int i = 0; i < subList.size(); i += 2) {
            sortList.add(subList.get(i));
        }
        for (int i = 1; i < subList.size(); i += 2) {
            sortList.add(subList.get(i));
        }
        return sortList;
    }

    private void refreshView() {
        if (mRadioBillboardAdapter != null) {
            mRadioBillboardAdapter.notifyDataSetChanged();
        }
        if (mRadioChoiceAdapter != null) {
            mRadioChoiceAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder) {
        if (hasBind) {
            return;
        }
        hasBind = true;

        prepareTab();

        // 榜单
        mBillboardName = holder.itemView.findViewById(R.id.tv_name);
        if (mBillboard == null) {
            mBillboardName.setText("经典必听榜");
        } else {
            mBillboardName.setText(mBillboard.boardName);
        }
        mBillboardName.setOnClickListener(v -> {
            if (mBillboard != null) {
                ReportEvent.reportNeceListingsClick(BillBoardClickEvent.CLICK_POS_HEADER);
                showAlbumList(mBillboard);
            }
        });
        holder.itemView.findViewById(R.id.tv_more).setOnClickListener(v -> {
            if (mBillboard != null) {
                ReportEvent.reportNeceListingsClick(BillBoardClickEvent.CLICK_POS_FOOTER);
                showAlbumList(mBillboard);
            }
        });

        RecyclerView rvBillboard = holder.itemView.findViewById(R.id.rv_billboard);
        rvBillboard.setLayoutManager(new GridLayoutManager(mCtx, 3, GridLayoutManager.HORIZONTAL, false));
        mRadioBillboardAdapter = new RecyclerAdapter<PageItemData>(mCtx, mRadioBillbroadList, R.layout.home_recycle_item_billboard) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder holder, int position, PageItemData item) {
                if (item == null) {
                    return;
                }
                ImageView iv_logo = (ImageView) holder.getView(R.id.iv_logo);
                TextView tv_name = (TextView) holder.getView(R.id.tv_name);
                TextView tv_describe = (TextView) holder.getView(R.id.tv_describe);
                PlayingStateView iv_playing = (PlayingStateView) holder.getView(R.id.iv_playing);
                GlideHelper.loadWithCorners(mCtx, item.logo, R.drawable.home_default_cover_icon_normal_corners, iv_logo);
                mIvRecycler.mark(iv_logo, item.logo, R.drawable.home_default_cover_icon_normal_corners);
                tv_name.setText(item.name);
                tv_describe.setText(item.desc);

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
                        playItemData(item);
                        Album album = new Album();
                        album.sid = item.sid;
                        album.id = item.id;
                        ReportEvent.reportPageItemClick(SysPageItemClickEvent.PAGE_TYPE_RADIO_CHOICE, 1, album);
                        ReportEvent.reportNeceListingsContentClick(album, BillBoardContentClickEvent.CLICK_POS_LIST);
                    }
                });
            }

            @Override
            public long getItemId(int position) {
                return position;
            }
        };
        mRadioBillboardAdapter.setHasStableIds(true);
        rvBillboard.setAdapter(mRadioBillboardAdapter);
        rvBillboard.setHasFixedSize(true);
        rvBillboard.setItemAnimator(null);

        RecyclerView mRecyclerView = holder.itemView.findViewById(R.id.rv_choice);
        int spanCount = mRadioChoiceList.size() / 2;
        if (spanCount > 3) {
            spanCount = 3;
        }
        if (spanCount == 0) {
            spanCount = 1;
        }
        mLayoutManager = new GridLayoutManager(mCtx, spanCount, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRadioChoiceAdapter = new RecyclerAdapter<PageItemData>(mCtx, getNextPage(), R.layout.home_recycle_item_radio_choice) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder holder, int position, PageItemData item) {
                ImageView iv_logo = (ImageView) holder.getView(R.id.iv_logo);
                if (item != null) {
                    ImageView iv_fm = (ImageView) holder.getView(R.id.iv_fm);
                    ImageView iv_flag = (ImageView) holder.getView(R.id.iv_flag);
                    TextView tv_name = (TextView) holder.getView(R.id.tv_name);
                    PlayingStateView iv_playing = (PlayingStateView) holder.getView(R.id.iv_playing);
                    GlideHelper.loadWithCorners(mCtx, item.logo, R.drawable.home_default_cover_icon_normal_corners, iv_logo);
                    mIvRecycler.mark(iv_logo, item.logo, R.drawable.home_default_cover_icon_normal_corners);
                    tv_name.setVisibility(View.VISIBLE);
                    tv_name.setText(item.name);

                    if (item.albumType != Album.ALBUM_TYPE_CFM) {
                        iv_fm.setVisibility(View.GONE);
                    } else {
                        iv_fm.setVisibility(View.VISIBLE);
                    }

                    if (item.icon != null && item.icon.url != null) {
                        GlideHelper.load(mCtx, item.icon.url, 0, iv_flag);
                    }

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
                            album.sid = item.sid;
                            album.id = item.id;
                            ReportEvent.reportPageItemClick(SysPageItemClickEvent.PAGE_TYPE_RADIO_CHOICE, item.posId, album);
                            playItemData(item);
                        }
                    });
                } else {
                    GlideHelper.loadWithCorners(mCtx, null, R.drawable.home_default_cover_icon_normal, iv_logo);
                }
            }

            @Override
            public long getItemId(int position) {
                return position;
            }
        };
        mRadioChoiceAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mRadioChoiceAdapter);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(null);
        GridOffsetsItemDecoration decoration = new GridOffsetsItemDecoration(GridOffsetsItemDecoration.GRID_OFFSETS_VERTICAL);
        decoration.setOffsetEdge(false);
        decoration.setOffsetLast(false);
        TypedValue typedValue = new TypedValue();
        mCtx.getTheme().resolveAttribute(R.attr.page_item_offset_1_1, typedValue, true);
        int offset = (int) typedValue.getDimension(mCtx.getResources().getDisplayMetrics());
        decoration.setVerticalItemOffsets(offset);
        decoration.setHorizontalItemOffsets(mCtx.getResources().getDimensionPixelOffset(R.dimen.m12));
        mRecyclerView.addItemDecoration(decoration);
    }

    @Override
    public void onViewAttachedToWindow(boolean includeGlide) {
        if (includeGlide) {
            mIvRecycler.resumeAll(mCtx, (iv, url, res) -> {
                GlideHelper.loadWithCorners(mCtx, url, res, iv);
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
