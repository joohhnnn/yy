package com.txznet.music.ui.tab;

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
import com.txznet.music.data.entity.CategoryItemData;
import com.txznet.music.helper.GlideHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.SysPageItemClickEvent;
import com.txznet.music.ui.album.AlbumFragment;
import com.txznet.music.util.IvRecycler;
import com.txznet.music.util.adapter.RecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 电台分类tab
 *
 * @author zackzhou
 * @date 2019/1/7,12:09
 */

public class RadioCategoryTab extends BaseTab {
    private FragmentActivity mCtx;
    private FragmentManager mFragmentManager;
    private RecyclerAdapter<CategoryItemData> mRadioCategoryAdapter;
    private List<CategoryItemData> mRadioCategoryList = new ArrayList<>(0);
    private IvRecycler mIvRecycler = new IvRecycler();

    public RadioCategoryTab(FragmentActivity ctx, FragmentManager fragmentManager) {
        this.mCtx = ctx;
        this.mFragmentManager = fragmentManager;
    }

    private boolean hasBind;

    private void prepareTab() {
        if (mRadioCategoryList.isEmpty()) {
            for (int i = 0; i < 15; i++) {
                mRadioCategoryList.add(null);
            }
        }
    }

    /**
     * 刷新数据
     */
    public void refreshData(List<CategoryItemData> data) {
        if (data != null) {
            mRadioCategoryList = data;
            nextPage();
        }
    }

    /**
     * 换一批
     */
    private void nextPage() {
        if (mRadioCategoryAdapter != null) {
            mRadioCategoryAdapter.refresh(getNextPage());
        }
    }

    private List<CategoryItemData> getNextPage() {
        int start = 0;
        int end = start + 15;
        if (end > mRadioCategoryList.size()) {
            end = mRadioCategoryList.size();
        }
        List<CategoryItemData> categoryItemDataList = mRadioCategoryList.subList(start, end);
        for (int i = 0; i < categoryItemDataList.size(); i++) {
            if (categoryItemDataList.get(i) != null) {
                categoryItemDataList.get(i).posId = i + 1;
            }
        }
        return categoryItemDataList;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder) {
        if (hasBind) {
            return;
        }
        hasBind = true;

        prepareTab();

        RecyclerView recyclerView = holder.itemView.findViewById(R.id.rv_category);
        GridLayoutManager layoutManager = new GridLayoutManager(mCtx, 5);
        recyclerView.setLayoutManager(layoutManager);
        mRadioCategoryAdapter = new RecyclerAdapter<CategoryItemData>(mCtx, getNextPage(), R.layout.home_recycle_item_category_radio) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder holder, int position, CategoryItemData item) {
                if (item == null) {
                    return;
                }
                ImageView iv_logo = (ImageView) holder.getView(R.id.iv_logo);
                TextView tv_name = (TextView) holder.getView(R.id.tv_name);
                GlideHelper.loadWithCorners(mCtx, item.logo, R.drawable.home_default_cover_icon_small_normal, iv_logo);
                mIvRecycler.mark(iv_logo, item.logo, R.drawable.home_default_cover_icon_small_normal);
                tv_name.setText(item.desc);
                tv_name.setVisibility(View.VISIBLE);
                holder.itemView.setOnClickListener((View v) -> {
                    ReportEvent.reportPageItemClick(SysPageItemClickEvent.PAGE_TYPE_RADIO_CATEGORY, item.posId, item.categoryId);
                    AlbumFragment.getAlbumFragment(item, "radioCategory").show(mFragmentManager, "albumFragment");
                });
            }

            @Override
            public long getItemId(int position) {
                return position;
            }
        };
        mRadioCategoryAdapter.setHasStableIds(true);
        recyclerView.setAdapter(mRadioCategoryAdapter);
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
