package com.txznet.music.albumModule.ui;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Category;
import com.txznet.music.albumModule.bean.HeadTitle;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.CategoryEngine;
import com.txznet.music.albumModule.ui.adapter.RadioTypeAdapter;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.utils.ViewUtils;

import java.util.List;

public class SingleRadioFragment extends AlbumFragment {

    private RelativeLayout mLayoutHead;
    private RecyclerView mRecyclerHeadRadio;
    private TXZLinearLayoutManager mManager;
    private RadioTypeAdapter mAdapterCategory;

    @Override
    public List<Category> getCategories() {
        return CategoryEngine.getInstance().getRadioCategory();
    }

    @Override
    public Category getCurrentCategory(Category category) {
//        if (category.getArrChild() != null && category.getArrChild().size() > 0) {
//            return category.getArrChild().get(0);
//        }
        return category;
    }

    @Override
    public void initTitleView() {
        mLayoutHead.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onAlbumItemClicked() {
        super.onAlbumItemClicked();
        mAdapterCategory.updateItemSBState(true);
        mAdapterCategory.notifyDataSetChanged();
    }

    @Override
    public void initData() {
        super.initData();

        mManager = new TXZLinearLayoutManager(GlobalContext.get());
        mManager.setOrientation(TXZLinearLayoutManager.HORIZONTAL);
        mRecyclerHeadRadio.setLayoutManager(mManager);
        mAdapterCategory = new RadioTypeAdapter(this,getCategories());
        mRecyclerHeadRadio.setAdapter(mAdapterCategory);
        mAdapterCategory.setSelectedIndex(0);
    }

    @Override
    public void initListener() {
        super.initListener();
        mAdapterCategory.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mAdapterCategory.getSelectedPosition()==position){//点击相同的模块
                    return;
                }
                mAdapterCategory.updateItemSBState(false);
                mAdapterCategory.setSelectedIndex(position);
                mAdapterCategory.notifyDataSetChanged();
//                Category category = mAdapterCategory.getItem(position);
//                if (category.getArrChild() != null && category.getArrChild().size() > 0) {
//                    mCategory = category.getArrChild().get(0);
//                } else {
                    mCategory = mAdapterCategory.getItem(position);
//                }
                mRecyclerView.setVisibility(View.VISIBLE);
                int itemCount = adapter.getItemCount();
                albums.clear();//清空
                adapter.notifyItemRangeRemoved(0, itemCount);
                showLoadingView(true);
                pageOff = 1;
                AlbumEngine.getInstance().queryAlbum(mCategory.getCategoryId(), pageOff);
            }
        });
    }

    @Override
    void onCategoryUpdate() {
        mAdapterCategory.updateCategories(mCategories);
        mAdapterCategory.notifyDataSetChanged();
    }

    @Override
    public void bindViews() {
        super.bindViews();

        mLayoutHead = (RelativeLayout) findViewById(R.id.layout_head_radio);
        mRecyclerHeadRadio = (RecyclerView) findViewById(R.id.recycler_radio);

        ViewUtils.setViewBgColor(mLayoutHead,R.color.title_header);
    }

    @Override
    public int getLayout() {
        return super.getLayout();
    }


    @Override
    public void onTitleClickListener(HeadTitle title) {

    }

    @Override
    public String getFragmentId() {
        return super.getFragmentId()+"/电台";
    }
}
