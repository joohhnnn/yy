package com.txznet.music.albumModule.ui;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Category;
import com.txznet.music.albumModule.bean.HeadTitle;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.CategoryEngine;
import com.txznet.music.report.ReportEvent;

import org.xmlpull.v1.XmlPullParser;

import java.util.List;
import java.util.Observable;

public class SingleMusicFragment extends AlbumFragment {

    private List<HeadTitle> mTitles;

    protected LinearLayout mLayoutTitleMusic;

    //流行
    protected RelativeLayout mRlCategory1;
    protected RelativeLayout mRlCategoryContent1;
    protected TextView mTvCategoryContent1;
//    protected ImageView mIvCategorySelect1;

    //排行榜
    protected RelativeLayout mRlCategory2;
    protected RelativeLayout mRlCategoryContent2;
    protected TextView mTvCategoryContent2;
//    protected ImageView mIvCategorySelect2;

    //歌手
    protected RelativeLayout mRlCategory3;
    protected RelativeLayout mRlCategoryContent3;
    protected TextView mTvCategoryContent3;
//    protected ImageView mIvCategorySelect3;

    //分类
    protected RelativeLayout mRlCategory4;
    protected RelativeLayout mRlCategoryContent4;
    protected TextView mTvCategoryContent4;
//    protected ImageView mIvCategorySelect4;


    private List<Category> mCategories;

    private View.OnClickListener mTitleClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.isSelected()) {
                return;
            }
            setSelected(v);
            switch (v.getId()) {
                case R.id.rl_content_category_1:
                    mTvCategoryContent1.setText(Html.fromHtml("<b>" + mTvCategoryContent1.getText() + "</b>").toString());
                    mRecyclerView.setVisibility(View.VISIBLE);
                    if (mCategories != null && mCategories.size() >= 1) {
                        mCategory = mCategories.get(0);
//                        mCategory.setShowStyle(SHOWTYPE_RECOMMAND);
                        updateAlbum(null);
                    } else {
                        CategoryEngine.getInstance().queryCategory();
                    }

                    break;
                case R.id.rl_content_category_2:
                    mTvCategoryContent2.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    mRecyclerView.setVisibility(View.VISIBLE);
                    if (mCategories != null && mCategories.size() >= 2) {
                        mCategory = mCategories.get(1);
//                        mCategory.setShowStyle(SHOWTYPE_RANKING_LIST);
                        updateAlbum(null);
                    } else {
                        CategoryEngine.getInstance().queryCategory();
                    }
                    break;
                case R.id.rl_content_category_3:
                    mTvCategoryContent3.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    mRecyclerView.setVisibility(View.VISIBLE);
                    if (mCategories != null && mCategories.size() >= 3) {
                        mCategory = mCategories.get(2);
//                        mCategory.setShowStyle(SHOWTYPE_SINGER);
                        updateAlbum(null);
                    } else {
                        CategoryEngine.getInstance().queryCategory();
                    }
                    break;
                case R.id.rl_content_category_4:
                    mTvCategoryContent4.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    mRecyclerView.setVisibility(View.VISIBLE);
                    if (mCategories != null && mCategories.size() >= 3) {
                        mCategory = mCategories.get(3);
//                        mCategory.setShowStyle(SHOWTYPE_RANKING_CLASSIFY);
                        updateAlbum(null);
                    } else {
                        CategoryEngine.getInstance().queryCategory();
                    }
                    break;
            }
        }
    };


    private void updateAlbum(Category mDefault) {
        if (mCategory == null && mDefault == null) {
            LogUtil.loge("mCategory is null");
            showNodataView();
            return;
        }
        Category category = mCategory;
        if (category == null) {
            category = mDefault;
        }
        mRecyclerView.setVisibility(View.VISIBLE);
        int itemCount = adapter.getItemCount();
        albums.clear();//清空
        adapter.notifyItemRangeRemoved(0, itemCount);
        showLoadingView(true);
        pageOff = 1;
        AlbumEngine.getInstance().queryAlbum(category.getCategoryId(), pageOff);
        ReportEvent.clickMusicCategory(category.getCategoryId());
    }

    private void setSelected(View view) {
        mRlCategoryContent1.setBackground(mRes.getDrawable(R.drawable.music_title_btn_bg));
        mRlCategoryContent2.setBackground(mRes.getDrawable(R.drawable.music_title_btn_bg));
        mRlCategoryContent3.setBackground(mRes.getDrawable(R.drawable.music_title_btn_bg));
        mRlCategoryContent4.setBackground(mRes.getDrawable(R.drawable.music_title_btn_bg));
        mRlCategoryContent1.setSelected(false);
        mRlCategoryContent2.setSelected(false);
        mRlCategoryContent3.setSelected(false);
        mRlCategoryContent4.setSelected(false);

        view.setSelected(true);
        mTvCategoryContent1.setText(mTvCategoryContent1.getText());
        mTvCategoryContent2.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        mTvCategoryContent3.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
//        mTvCategoryContent4.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
//        textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
////设置不为加粗
        mTvCategoryContent4.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
    }


    @Override
    protected void onAlbumItemClicked() {
        super.onAlbumItemClicked();
    }

    @Override
    void onCategoryUpdate() {
        mCategories = getCategories();
        updateCategoryView();
    }

    @Override
    public void bindViews() {
        super.bindViews();

        mLayoutTitleMusic = (LinearLayout) findViewById(R.id.layout_head_music);

        mRlCategory1 = (RelativeLayout) findViewById(R.id.rl_category_1);
        mRlCategoryContent1 = (RelativeLayout) findViewById(R.id.rl_content_category_1);
        mTvCategoryContent1 = (TextView) findViewById(R.id.tv_content_category_1);
//        mIvCategorySelect1 = (ImageView) findViewById(R.id.iv_content_select_1);

        mRlCategory2 = (RelativeLayout) findViewById(R.id.rl_category_2);
        mRlCategoryContent2 = (RelativeLayout) findViewById(R.id.rl_content_category_2);
        mTvCategoryContent2 = (TextView) findViewById(R.id.tv_content_category_2);
//        mIvCategorySelect2 = (ImageView) findViewById(R.id.iv_content_select_2);


        mRlCategory3 = (RelativeLayout) findViewById(R.id.rl_category_3);
        mRlCategoryContent3 = (RelativeLayout) findViewById(R.id.rl_content_category_3);
        mTvCategoryContent3 = (TextView) findViewById(R.id.tv_content_category_3);
//        mIvCategorySelect3 = (ImageView) findViewById(R.id.iv_content_select_3);

        mRlCategory4 = (RelativeLayout) findViewById(R.id.rl_category_4);
        mRlCategoryContent4 = (RelativeLayout) findViewById(R.id.rl_content_category_4);
        mTvCategoryContent4 = (TextView) findViewById(R.id.tv_content_category_4);
//        mIvCategorySelect4 = (ImageView) findViewById(R.id.iv_content_select_4);


        mRlCategoryContent1.setOnClickListener(mTitleClickListener);
        mRlCategoryContent2.setOnClickListener(mTitleClickListener);
        mRlCategoryContent3.setOnClickListener(mTitleClickListener);
        mRlCategoryContent4.setOnClickListener(mTitleClickListener);

        Resources resources = getActivity().getResources();
//        mIvCategorySelect1.setImageDrawable(resources.getDrawable(R.drawable.type_select));
//        mIvCategorySelect2.setImageDrawable(resources.getDrawable(R.drawable.type_select));
//        mIvCategorySelect3.setImageDrawable(resources.getDrawable(R.drawable.type_select));
//        mIvCategorySelect4.setImageDrawable(resources.getDrawable(R.drawable.type_select));
        mTvCategoryContent1.setTextColor(resources.getColorStateList(R.color.type_text));
        mTvCategoryContent2.setTextColor(resources.getColorStateList(R.color.type_text));
        mTvCategoryContent3.setTextColor(resources.getColorStateList(R.color.type_text));
        mTvCategoryContent4.setTextColor(resources.getColorStateList(R.color.type_text));
    }

    @Override
    public void initTitleView() {
        mLayoutTitleMusic.setVisibility(View.VISIBLE);
    }

    @Override
    public void initListener() {
        super.initListener();
    }

    @Override
    public void initData() {
        super.initData();
        initCategory();
        initSelect();
    }

    private void initSelect() {
        setSelected(mRlCategoryContent1);
    }

    private void initCategory() {
        mCategories = getCategories();
        updateCategoryView();
    }

    private void updateCategoryView() {
        if (mCategories == null || mCategories.size() == 0) {
            mRlCategory1.setVisibility(View.GONE);
            mRlCategory2.setVisibility(View.GONE);
            mRlCategory3.setVisibility(View.GONE);
            mRlCategory4.setVisibility(View.GONE);
            return;
        } else if (mCategories.size() == 1) {
            mRlCategory1.setVisibility(View.VISIBLE);
            mRlCategory2.setVisibility(View.GONE);
            mRlCategory3.setVisibility(View.GONE);
            mRlCategory4.setVisibility(View.GONE);

            mTvCategoryContent1.setText(mCategories.get(0).getDesc());
        } else if (mCategories.size() == 2) {
            mRlCategory1.setVisibility(View.VISIBLE);
            mRlCategory2.setVisibility(View.VISIBLE);
            mRlCategory3.setVisibility(View.GONE);
            mRlCategory4.setVisibility(View.GONE);

            mTvCategoryContent1.setText(mCategories.get(0).getDesc());
            mTvCategoryContent2.setText(mCategories.get(1).getDesc());
        } else if (mCategories.size() == 3) {
            mRlCategory1.setVisibility(View.VISIBLE);
            mRlCategory2.setVisibility(View.VISIBLE);
            mRlCategory3.setVisibility(View.VISIBLE);
            mRlCategory4.setVisibility(View.GONE);

            mTvCategoryContent1.setText(mCategories.get(0).getDesc());
            mTvCategoryContent2.setText(mCategories.get(1).getDesc());
            mTvCategoryContent3.setText(mCategories.get(2).getDesc());
        } else if (mCategories.size() == 4) {
            mRlCategory1.setVisibility(View.VISIBLE);
            mRlCategory2.setVisibility(View.VISIBLE);
            mRlCategory3.setVisibility(View.VISIBLE);
            mRlCategory4.setVisibility(View.VISIBLE);

            mTvCategoryContent1.setText(mCategories.get(0).getDesc());
            mTvCategoryContent2.setText(mCategories.get(1).getDesc());
            mTvCategoryContent3.setText(mCategories.get(2).getDesc());
            mTvCategoryContent4.setText(mCategories.get(3).getDesc());
        }
    }

    @Override
    public List<Category> getCategories() {
        return CategoryEngine.getInstance().getMusicCategory();
    }

    @Override
    public Category getCurrentCategory(Category category) {
        return category;
    }


    @Override
    public void onTitleClickListener(HeadTitle title) {
    }


    @Override
    public void update(Observable observable, Object data) {
        if (data instanceof InfoMessage) {
            InfoMessage info = (InfoMessage) data;
            switch (info.getType()) {
                case InfoMessage.REQ_CATEGORY_ALL:
                    initCategory();
                    break;
                case InfoMessage.RESP_ALBUM:
                    break;
            }
        }
        super.update(observable, data);
    }

    @Override
    public String getFragmentId() {
        return super.getFragmentId() + "/音乐";
    }
}
