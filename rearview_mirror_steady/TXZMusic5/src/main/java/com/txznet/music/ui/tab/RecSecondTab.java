package com.txznet.music.ui.tab;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.music.Constant;
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
 * 音乐精选页
 *
 * @author zackzhou
 * @date 2019/1/7,10:50
 */

public class RecSecondTab extends BaseTab {

    private FragmentActivity mCtx;
    private FragmentManager mFragmentManager;

    private PageItemData mRec1Data;
    private PageItemData mRec2Data;
    private PageItemData mUserMusic;
    private PageItemData mUserRadio;

    private View mRec1View;
    private View mRec2View;
    private View mUserRadioView;
    private View mUserMusicView;

    private TextView mBillboardName;
    private BillboardData mBillboard;
    private List<PageItemData> mRadioBillbroadList = new ArrayList<>(3);
    private RecyclerAdapter<PageItemData> mRadioBillboardAdapter;

    private Album mAlbum;
    private boolean isPlaying;

    private IvRecycler mIvRecycler = new IvRecycler();

    public RecSecondTab(FragmentActivity ctx, FragmentManager fragmentManager) {
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
        AlbumFragment.getAlbumFragment(itemData, "recSecondBillboard").show(mFragmentManager, "Album");
    }

    // 刷新推荐
    private void refreshRec(View content, PageItemData item) {
        if (content != null) {
            ImageView iv_logo = content.findViewById(R.id.iv_logo);
            if (item != null) {
                TextView tv_name = content.findViewById(R.id.tv_name);
                TextView tv_describe = content.findViewById(R.id.tv_describe);
                PlayingStateView iv_playing = content.findViewById(R.id.iv_playing);
                GlideHelper.loadWithCorners(mCtx, item.logo, R.drawable.home_default_cover_icon_strip_large_corners, iv_logo);
                mIvRecycler.mark(iv_logo, item.logo, R.drawable.home_default_cover_icon_strip_large_corners);
                tv_name.setText(item.name);
                tv_describe.setVisibility(View.VISIBLE);
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

                content.setOnClickListener(v -> {
                    if (iv_playing.isPlaying()) {
                        PlayerActionCreator.get().pause(Operation.MANUAL);
                    } else {
                        playItemData(item);
                        Album album = new Album();
                        album.sid = item.sid;
                        album.id = item.id;
                        ReportEvent.reportPageItemClick(SysPageItemClickEvent.PAGE_TYPE_RECOMMEND, item.posId, album);
                    }
                });
            } else {
                GlideHelper.loadWithCorners(mCtx, null, R.drawable.home_default_cover_icon_strip_large, iv_logo);
                TextView tv_describe = content.findViewById(R.id.tv_describe);
                tv_describe.setVisibility(View.INVISIBLE);
            }
        }
    }

    // 刷新用户
    private void refreshUser(View content, PageItemData item, boolean isRadio) {
        if (content != null) {
            ImageView iv_logo = content.findViewById(R.id.iv_logo);
            if (item != null) {
                TextView tv_name = content.findViewById(R.id.tv_name);
                TextView tv_tag = content.findViewById(R.id.tv_tag);
                PlayingStateView iv_playing = content.findViewById(R.id.iv_playing);
                GlideHelper.loadWithCorners(mCtx, item.logo, R.drawable.home_default_cover_icon_normal_corners, iv_logo);
                mIvRecycler.mark(iv_logo, item.logo, R.drawable.home_default_cover_icon_normal_corners);
                tv_name.setVisibility(View.VISIBLE);
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

                content.setOnClickListener(v -> {
                    if (iv_playing.isPlaying()) {
                        PlayerActionCreator.get().pause(Operation.MANUAL);
                    } else {
                        playItemData(item);
                        Album album = new Album();
                        album.sid = item.sid;
                        album.id = item.id;
                        ReportEvent.reportPageItemClick(SysPageItemClickEvent.PAGE_TYPE_RECOMMEND, item.posId, album);
                        if (isRadio) {
                            ReportEvent.reportUserRadioPlay(album);
                        } else {
                            ReportEvent.reportUserMusicPlay(album);
                        }
                    }
                });
                tv_tag.setVisibility(View.VISIBLE);
                if (isRadio) {
                    tv_tag.setText("常听电台");
                    item.posId = 12;
                } else {
                    tv_tag.setText("常听音乐");
                    item.posId = 13;
                }
            } else {
                GlideHelper.loadWithCorners(mCtx, null, R.drawable.home_default_cover_icon_normal, iv_logo);
            }
        }
    }

    /**
     * 刷新用户常听音乐
     */
    public void refreshUserMusic(PageItemData userMusic) {
        mUserMusic = userMusic;
        refreshUser(mUserMusicView, mUserMusic, false);
    }

    /**
     * 刷新用户常听电台
     */
    public void refreshUserRadio(PageItemData userRadio) {
        mUserRadio = userRadio;
        refreshUser(mUserRadioView, mUserRadio, true);
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

    // 播放专辑数据
    private void playItemData(PageItemData item) {
        Album album = new Album();
        album.name = item.name;
        album.sid = item.sid;
        album.id = item.id;
        album.arrArtistName = item.arrArtistName;
        album.albumType = item.albumType;
        album.logo = item.logo;
        album.setExtraKey(Constant.AlbumExtra.SVR_DATA, item.svrData);
        PlayerActionCreator.get().playAlbum(Operation.MANUAL, album);
    }

    /**
     * 刷新数据
     */
    public void refreshData(List<PageItemData> data) {
        if (data != null) {
//            List<PageItemData> sortList = resort(new ArrayList<>(data));
            if (data.size() > 2) {
                mRec1Data = data.get(data.size() - 2);
                mRec2Data = data.get(data.size() - 1);
            }
            if (mRec1View != null) {
                refreshRec(mRec1View, mRec1Data);
            }
            if (mRec2View != null) {
                refreshRec(mRec2View, mRec2Data);
            }
        }
    }

    private List<PageItemData> resort(List<PageItemData> data) {
        List<PageItemData> subList;
        if (data.size() < 6) {
            subList = data.subList(0, data.size() / 2 * 2);
        } else {
            subList = data.subList(0, 6);
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
        refreshRec(mRec1View, mRec1Data);
        refreshRec(mRec2View, mRec2Data);
        refreshUser(mUserMusicView, mUserMusic, false);
        refreshUser(mUserRadioView, mUserRadio, true);
        if (mRadioBillboardAdapter != null) {
            mRadioBillboardAdapter.notifyDataSetChanged();
        }
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder) {
        if (hasBind) {
            return;
        }
        hasBind = true;

        prepareTab();

        mRec1View = holder.itemView.findViewById(R.id.rec_1);
        mRec2View = holder.itemView.findViewById(R.id.rec_2);
        mUserRadioView = holder.itemView.findViewById(R.id.user_radio);
        mUserMusicView = holder.itemView.findViewById(R.id.user_music);

        // 榜单
        mBillboardName = holder.itemView.findViewById(R.id.tv_name_wrap);
        if (mBillboard == null) {
            mBillboardName.setText("车主节目榜");
        } else {
            mBillboardName.setText(mBillboard.boardName);
        }
        mBillboardName.setOnClickListener(v -> {
            if (mBillboard != null) {
                ReportEvent.reportPayListingsClick(BillBoardClickEvent.CLICK_POS_HEADER);
                showAlbumList(mBillboard);
            }
        });
        holder.itemView.findViewById(R.id.tv_more_wrap).setOnClickListener(v -> {
            if (mBillboard != null) {
                ReportEvent.reportPayListingsClick(BillBoardClickEvent.CLICK_POS_FOOTER);
                showAlbumList(mBillboard);
            }
        });
        RecyclerView rvBillboard = holder.itemView.findViewById(R.id.rv_billboard_wrap);
        rvBillboard.setLayoutManager(new GridLayoutManager(mCtx, 3, GridLayoutManager.HORIZONTAL, false));
        mRadioBillboardAdapter = new RecyclerAdapter<PageItemData>(mCtx, mRadioBillbroadList, R.layout.home_recycle_item_billboard) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder holder, int position, PageItemData item) {
                ImageView iv_logo = (ImageView) holder.getView(R.id.iv_logo);
                if (item != null) {
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
                            ReportEvent.reportPageItemClick(SysPageItemClickEvent.PAGE_TYPE_RECOMMEND, 11, album);
                            ReportEvent.reportPayListingsContentClick(album, BillBoardContentClickEvent.CLICK_POS_LIST);
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
        mRadioBillboardAdapter.setHasStableIds(true);
        rvBillboard.setAdapter(mRadioBillboardAdapter);
        rvBillboard.setHasFixedSize(true);
        rvBillboard.setItemAnimator(null);

        refreshRec(mRec1View, mRec1Data);
        refreshRec(mRec2View, mRec2Data);
        refreshUser(mUserRadioView, mUserRadio, true);
        refreshUser(mUserMusicView, mUserMusic, false);
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
