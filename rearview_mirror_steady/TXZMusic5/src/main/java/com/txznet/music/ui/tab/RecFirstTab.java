package com.txznet.music.ui.tab;

import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dinuscxj.itemdecoration.GridOffsetsItemDecoration;
import com.txznet.music.R;
import com.txznet.music.action.PlayerActionCreator;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.PageItemData;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.GlideHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.SysPageItemClickEvent;
import com.txznet.music.store.PlayInfoStore;
import com.txznet.music.ui.local.LocalMusicFragment;
import com.txznet.music.ui.user.UserFragment;
import com.txznet.music.util.IvRecycler;
import com.txznet.music.util.StringUtils;
import com.txznet.music.util.TimeManager;
import com.txznet.music.util.adapter.RecyclerAdapter;
import com.txznet.music.widget.PlayingStateView;
import com.txznet.rxflux.Operation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 音乐精选页
 *
 * @author zackzhou
 * @date 2019/1/7,10:50
 */

public class RecFirstTab extends BaseTab {

    private FragmentActivity mCtx;
    private FragmentManager mFragmentManager;

    private ImageView mAiIv;
    private TextView mAiName;
    private TextView mAiDesc;
    private PlayingStateView mAiPlaying;
    private ImageView mDailyIv;
    private PlayingStateView mDailyPlaying;
    private View llDay;
    private TextView tvMonthEn;
    private TextView tvMonthCh;
    private TextView tvDay;
    private TextView tvDailyName;

    private RecyclerAdapter<PageItemData> mRecAdapter;
    private GridLayoutManager mLayoutManager;
    private List<PageItemData> mRecList = new ArrayList<>(4);
    private int mPageIndex = -1;

    private PageItemData mAiItemData, mDailyRecData;

    private ImageView btnLocalPlay;
    private View btnLocal;
    private View btnUser;

    private Album mAlbum;
    private boolean isPlaying;

    private IvRecycler mIvRecycler = new IvRecycler();

    public RecFirstTab(FragmentActivity ctx, FragmentManager fragmentManager) {
        this.mCtx = ctx;
        this.mFragmentManager = fragmentManager;
    }

    private boolean hasBind;

    private void prepareTab() {
        if (mRecList.isEmpty()) {
            for (int i = 0; i < 4; i++) {
                mRecList.add(null);
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

    // 播放专辑数据
    private void playItemData(PageItemData item) {
        Album album = new Album();
        album.name = item.name;
        album.sid = item.sid;
        album.id = item.id;
        album.arrArtistName = item.arrArtistName;
        album.albumType = item.albumType;
        album.logo = item.logo;
        ReportEvent.reportPageItemClick(SysPageItemClickEvent.PAGE_TYPE_RECOMMEND, item.posId, album);
        PlayerActionCreator.get().playAlbum(Operation.MANUAL, album);
    }

    /**
     * 刷新数据
     */
    public void refreshData(List<PageItemData> data) {
        if (data != null) {
            if (data.size() <= 4) {
                mRecList = data;
            } else {
                mRecList = data.subList(0, data.size() / 4 * 4);
            }
            nextPage();
        }
    }

    /**
     * 刷新AI电台信息
     */
    public void refreshAiData(PageItemData data) {
        if (data != null) {
            mAiItemData = data;
            if (mAiIv != null) {
                GlideHelper.loadWithCorners(mCtx, mAiItemData.logo, R.drawable.home_default_cover_icon_strip_large_corners, mAiIv);
            }
            if (mAiName != null) {
                mAiName.setText(mAiItemData.name);
            }
            if (mAiDesc != null) {
                mAiDesc.setVisibility(View.VISIBLE);
                mAiDesc.setText(mAiItemData.desc);
            }
        }
        checkAiRadioPlay();
    }

    /**
     * 刷新每日推荐信息
     */
    public void refreshDailyRec(PageItemData data) {
        if (data != null) {
            mDailyRecData = data;
            if (mDailyIv != null) {
                GlideHelper.loadWithCorners(mCtx, mDailyRecData.logo, R.drawable.home_default_cover_icon_normal_corners, mDailyIv);
            }
            if (llDay != null) {
                llDay.setVisibility(View.VISIBLE);
            }
            if (tvMonthEn != null && tvMonthCh != null && tvDay != null && tvDailyName != null) {
                long timeMillis = TimeManager.getInstance().getTimeMillis();
                Date date = new Date(timeMillis);
                tvMonthEn.setText(new SimpleDateFormat("MMM", Locale.US).format(date));
                tvMonthCh.setText(StringUtils.getFormatedMonth(date));
                tvDay.setText(new SimpleDateFormat("d", Locale.getDefault()).format(date));
                tvDailyName.setText(mDailyRecData.name);
            }
        }
        checkDailyRadioPlay();
    }


    /**
     * 下一页
     */
    private void nextPage() {
        if (mRecAdapter != null) {
            mRecAdapter.refresh(getNextPage());
            mRecAdapter.notifyDataSetChanged();
        }
    }

    private List<PageItemData> getNextPage() {
        if (mRecList.isEmpty()) {
            return mRecList;
        }
        List<PageItemData> subList;
        if (mRecList.size() < 4) {
            int spanCount = mRecList.size() / 2;
            if (spanCount == 0) {
                spanCount = 1;
            }
            if (mLayoutManager != null) {
                mLayoutManager.setSpanCount(spanCount);
            }
            subList = mRecList.subList(0, mRecList.size() / 2 * 2);
        } else {
            mPageIndex++;
            int start = mPageIndex * 2 * 2 % mRecList.size();
            int end = start + 2 * 2;
            if (end > mRecList.size()) {
                end = mRecList.size();
                mPageIndex = -1;
            }
            if (mLayoutManager != null) {
                mLayoutManager.setSpanCount(2);
            }
            subList = mRecList.subList(start, end);
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

    public void notifyLocalPlaying() {
        if (btnLocalPlay != null) {
            btnLocalPlay.setImageResource(R.drawable.home_local_pause_btn);
        }
        isLocalPlaying = true;
    }

    public void notifyLocalPaused() {
        if (btnLocalPlay != null) {
            btnLocalPlay.setImageResource(R.drawable.home_local_play_btn);
        }
        isLocalPlaying = false;
    }

    private boolean isLocalPlaying;


    private void checkAiRadioPlay() {
        if (mAiPlaying == null) {
            return;
        }
        if (mAiItemData == null) {
            mAiPlaying.onPause();
            mAiPlaying.setVisibility(View.GONE);
            return;
        }
        mAiPlaying.setVisibility(View.VISIBLE);
        if (mAlbum == null || mAlbum.sid != 100 || mAlbum.id != 1000001) {
            mAiPlaying.onPause();
            mAiPlaying.setVisibility(View.GONE);
        } else {
            mAiPlaying.setVisibility(View.VISIBLE);
            if (isPlaying) {
                mAiPlaying.onPlay();
            } else {
                mAiPlaying.onPause();
            }
        }
    }

    private void checkDailyRadioPlay() {
        if (mDailyPlaying == null) {
            return;
        }
        if (mDailyRecData == null) {
            mDailyPlaying.onPause();
            mDailyPlaying.setVisibility(View.GONE);
            return;
        }
        mDailyPlaying.setVisibility(View.VISIBLE);
        if (mAlbum == null || mAlbum.sid != 100 || mAlbum.id != 1000002) {
            mDailyPlaying.onPause();
            mDailyPlaying.setVisibility(View.GONE);
        } else {
            mDailyPlaying.setVisibility(View.VISIBLE);
            if (isPlaying) {
                mDailyPlaying.onPlay();
            } else {
                mDailyPlaying.onPause();
            }
        }
    }

    private void checkRecItemPlay() {
        if (mRecAdapter != null) {
            mRecAdapter.notifyDataSetChanged();
        }
    }

    private void checkLocalPlay() {
        if (isLocalPlaying) {
            notifyLocalPlaying();
        } else {
            notifyLocalPaused();
        }
    }

    private void refreshView() {
        checkAiRadioPlay();
        checkLocalPlay();
        checkDailyRadioPlay();
        checkRecItemPlay();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder) {
        if (hasBind) {
            return;
        }
        hasBind = true;

        prepareTab();

        // init ai
        holder.itemView.findViewById(R.id.include_ai).setOnClickListener(v -> {
            if (mAiItemData != null) {
                if (mAiPlaying.isPlaying()) {
                    PlayerActionCreator.get().pause(Operation.MANUAL);
                } else {
                    playItemData(mAiItemData);
                    ReportEvent.reportAiRadioClick();
                }
            }
        });
        ViewGroup vgAi = holder.itemView.findViewById(R.id.include_ai);
        mAiIv = vgAi.findViewById(R.id.iv_logo);
        mAiName = vgAi.findViewById(R.id.tv_name);
        mAiDesc = vgAi.findViewById(R.id.tv_describe);
        mAiPlaying = vgAi.findViewById(R.id.iv_playing);
        GlideHelper.loadWithCorners(mCtx, null, R.drawable.home_default_cover_icon_strip_large, mAiIv);
        if (mAiItemData != null) {
            if (mAiItemData.logo == null) {
                mAiItemData.logo = SharedPreferencesUtils.getAiRadioLogoUrl();
            }
            GlideHelper.loadWithCorners(mCtx, mAiItemData.logo, R.drawable.home_default_cover_icon_strip_large_corners, mAiIv);
            mIvRecycler.mark(mAiIv, mAiItemData.logo, R.drawable.home_default_cover_icon_strip_large_corners);
            mAiName.setText(mAiItemData.name);
            mAiDesc.setVisibility(View.VISIBLE);
            mAiDesc.setText(mAiItemData.desc);
            checkAiRadioPlay();
        } else {
            mAiDesc.setVisibility(View.INVISIBLE);
        }

        // init daily rec
        ViewGroup fl_daily_rec = holder.itemView.findViewById(R.id.fl_daily_rec);
        fl_daily_rec.setOnClickListener(v -> {
            if (mDailyRecData != null) {
                if (mDailyPlaying.isPlaying()) {
                    PlayerActionCreator.get().pause(Operation.MANUAL);
                } else {
                    playItemData(mDailyRecData);
                    ReportEvent.reportDailyClick();
                }
            }
        });
        llDay = fl_daily_rec.findViewById(R.id.llDay);
        mDailyIv = fl_daily_rec.findViewById(R.id.iv_logo);
        mDailyPlaying = fl_daily_rec.findViewById(R.id.iv_playing);
        tvMonthEn = fl_daily_rec.findViewById(R.id.tv_month_en);
        tvMonthCh = fl_daily_rec.findViewById(R.id.tv_month_ch);
        tvDay = fl_daily_rec.findViewById(R.id.tv_day);
        tvDailyName = fl_daily_rec.findViewById(R.id.tvDailyName);
        GlideHelper.loadWithCorners(mCtx, null, R.drawable.home_default_cover_icon_normal, mDailyIv);
        if (mDailyRecData != null) {
            if (mDailyRecData.logo == null) {
                mDailyRecData.logo = SharedPreferencesUtils.getRecommendLogoUrl();
            }
            GlideHelper.loadWithCorners(mCtx, mDailyRecData.logo, R.drawable.home_default_cover_icon_normal_corners, mDailyIv);
            mIvRecycler.mark(mDailyIv, mDailyRecData.logo, R.drawable.home_default_cover_icon_normal_corners);
            llDay.setVisibility(View.VISIBLE);
            long timeMillis = TimeManager.getInstance().getTimeMillis();
            Date date = new Date(timeMillis);
            tvMonthEn.setText(new SimpleDateFormat("MMM", Locale.US).format(date));
            tvMonthCh.setText(StringUtils.getFormatedMonth(date));
            tvDay.setText(new SimpleDateFormat("d", Locale.getDefault()).format(date));
            tvDailyName.setText(mDailyRecData.name);
            checkDailyRadioPlay();
        } else {
            llDay.setVisibility(View.GONE);
        }

        // init local
        btnLocal = holder.itemView.findViewById(R.id.btn_local);
        btnLocal.setOnClickListener(v -> {
            ReportEvent.reportPageItemClick(SysPageItemClickEvent.PAGE_TYPE_RECOMMEND, 2, null);
            ReportEvent.reportLocalEnter();
            new LocalMusicFragment().show(mFragmentManager, "Local");
        });

        GlideHelper.loadWithCorners(mCtx, R.drawable.home_local_btn_bg_normal, btnLocal);

        btnLocalPlay = holder.itemView.findViewById(R.id.btn_local_play);
        btnLocalPlay.setOnClickListener(v -> {
            ReportEvent.reportLocalPlay();
            if (isLocalPlaying) {
                PlayerActionCreator.get().pause(Operation.MANUAL);
            } else {
                PlayerActionCreator.get().playLocal(Operation.MANUAL);
            }
        });

        // init user
        btnUser = holder.itemView.findViewById(R.id.btn_user);
        btnUser.setOnClickListener(v -> {
            ReportEvent.reportPageItemClick(SysPageItemClickEvent.PAGE_TYPE_RECOMMEND, 3);
            ReportEvent.reportUserEnter();
            new UserFragment().show(mFragmentManager, "User");
        });
        GlideHelper.loadWithCorners(mCtx, R.drawable.home_user_btn_bg_normal, btnUser);

        checkLocalPlay();

        // init rec
        RecyclerView recyclerView = holder.itemView.findViewById(R.id.rv_data);
        int spanCount = mRecList.size() / 2;
        if (spanCount > 2) {
            spanCount = 2;
        }
        if (spanCount == 0) {
            spanCount = 1;
        }
        mLayoutManager = new GridLayoutManager(mCtx, spanCount, GridLayoutManager.VERTICAL, false) {
            @Override
            public boolean isAutoMeasureEnabled() {
                return true;
            }
        };

        recyclerView.setLayoutManager(mLayoutManager);
        mRecAdapter = new RecyclerAdapter<PageItemData>(mCtx, getNextPage(), R.layout.home_recycle_item_rec) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder holder, int position, PageItemData item) {
                ImageView iv_logo = (ImageView) holder.getView(R.id.iv_logo);
                if (item != null) {
                    TextView tv_name = (TextView) holder.getView(R.id.tv_name);
                    PlayingStateView iv_playing = (PlayingStateView) holder.getView(R.id.iv_playing);
                    GlideHelper.loadWithCorners(mCtx, item.logo, R.drawable.home_default_cover_icon_normal_corners, iv_logo);
                    mIvRecycler.mark(iv_logo, item.logo, R.drawable.home_default_cover_icon_normal_corners);
                    tv_name.setVisibility(View.VISIBLE);
                    tv_name.setText(item.name);
                    iv_playing.setVisibility(View.VISIBLE);

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

            @Override
            public void notifyDataSetChanged(List<PageItemData> mDatas) {
                super.notifyDataSetChanged(mDatas);
            }
        };
        mRecAdapter.setHasStableIds(true);
        recyclerView.setAdapter(mRecAdapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(null);

        GridOffsetsItemDecoration decoration = new GridOffsetsItemDecoration(GridOffsetsItemDecoration.GRID_OFFSETS_VERTICAL);
        decoration.setOffsetEdge(false);
        decoration.setOffsetLast(false);

        TypedValue typedValue = new TypedValue();
        mCtx.getTheme().resolveAttribute(R.attr.page_item_offset_1_1, typedValue, true);
        int offset = (int) typedValue.getDimension(mCtx.getResources().getDisplayMetrics());
        decoration.setVerticalItemOffsets(offset);
        decoration.setHorizontalItemOffsets(mCtx.getResources().getDimensionPixelOffset(R.dimen.m12));
        recyclerView.addItemDecoration(decoration);
    }

    @Override
    public void onViewAttachedToWindow(boolean includeGlide) {
        if (includeGlide) {
            mIvRecycler.resumeAll(mCtx, (iv, url, res) -> {
                GlideHelper.loadWithCorners(mCtx, url, res, iv);
            });

            if (btnLocal != null) {
                GlideHelper.loadWithCorners(mCtx, R.drawable.home_local_btn_bg_normal, btnLocal);
            }

            if (btnUser != null) {
                GlideHelper.loadWithCorners(mCtx, R.drawable.home_user_btn_bg_normal, btnUser);
            }
        }
    }

    @Override
    public void onViewDetachedFromWindow(boolean includeGlide, boolean cancelReq) {
        if (includeGlide) {
            mIvRecycler.recycleAll(mCtx, iv -> {
//            GlideApp.with(mCtx).clear(iv);
            }, cancelReq);

            if (cancelReq) {
                if (btnLocal != null) {
                    btnLocal.setBackground(null);
                }
                if (btnUser != null) {
                    btnUser.setBackground(null);
                }
            }

        }
    }
}
