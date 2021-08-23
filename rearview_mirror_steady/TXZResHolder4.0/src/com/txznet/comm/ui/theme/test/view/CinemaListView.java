package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.DrawableRes;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CinemaListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICinemaListView;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize.ImageLoaderImpl;
import com.txznet.resholder.R;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.runnables.Runnable1;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * 电影列表
 * <p>
 * 唤醒：最近有什电影
 * <p>
 * 2020-08-20 1129
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class CinemaListView extends ICinemaListView {

    private static CinemaListView sInstance = new CinemaListView();

    private LruCache<String, Bitmap> mCachePost = new LruCache<String, Bitmap>(ConfigUtil.getCinemaItemCount());

    private List<View> mItemViews;

    private CinemaListView() {
    }

    public static CinemaListView getInstance() {
        return sInstance;
    }

    @Override
    public void updateProgress(int progress, int selection) {
        LogUtil.logd("updateProgress " + progress + "," + selection);
    }

    @Override
    public void release() {
        super.release();
        if (mItemViews != null) {
            mItemViews.clear();
        }
    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        CinemaListViewData cinemaData = (CinemaListViewData) data;
        WinLayout.getInstance().vTips = cinemaData.vTips;
        LogUtil.logd(WinLayout.logTag + "cinemaData.vTips: " + cinemaData.vTips);

        View view = createViewNone(cinemaData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = CinemaListView.getInstance();

        return viewAdapter;
    }

    private View createViewNone(CinemaListViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = viewData.mTitleInfo.maxPage;
        int curPage = viewData.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        ArrayList<CinemaListViewData.CinemaBean> dataAry = viewData.getData();

        mItemViews = new ArrayList<>();
        for (int i = 0; i < viewData.count; i++) {
            View itemView = createItemView(context, i, dataAry.get(i), i != viewData.count - 1);
            container.addView(itemView);
            mItemViews.add(itemView);
        }

        // 添加空视图填充空间
        int re = SizeConfig.pageMovieCount - viewData.count;
        for (int i = 0; i < re; i++) {
            View itemView = createItemView(context, i, null, false);
            container.addView(itemView);
        }

        return view;
    }

    private View createItemView(Context context, int position, CinemaListViewData.CinemaBean row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.cinema_list_view_item, (ViewGroup) null);

        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvTitle = view.findViewById(R.id.tvTitle);     // 标题
        ImageView ivPost = view.findViewById(R.id.ivPost);      // 海报
        ImageView ivStar = view.findViewById(R.id.ivStar);      // 评分
        TextView tvScorePre = view.findViewById(R.id.tvScorePre);
        TextView tvScoreAft = view.findViewById(R.id.tvScoreAft);
        View divider = view.findViewById(R.id.divider);

        tvTitle.setText(String.format(Locale.getDefault(), "%d. %s",
                    position + 1, LanguageConvertor.toLocale(row.title)));
        loadDrawableByUrl(ivPost, row.post);

        if (row.score > 0) {
            ivStar.setImageResource(getSoreMark(row.score));
            String sc = String.format(Locale.getDefault(), "%.1f", row.score);
            if (sc.contains(".")) {
                String[] scoreArray = sc.split("\\.");
                if (scoreArray.length > 1) {
                    tvScorePre.setText(scoreArray[0]);
                    tvScoreAft.setText("." + scoreArray[1]);
                }
            } else {
                tvScorePre.setText(LanguageConvertor.toLocale(sc));
                tvScoreAft.setText("");
            }
        } else {
            tvScorePre.setText("暂无评分");
            tvScoreAft.setText("");
            ivStar.setVisibility(View.GONE);
        }

        // 分隔线
        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        // 设置列表点击
        ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, position);
        return view;
    }

    @Override
    public void init() {
        super.init();

    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }


    @Override
    public void snapPage(boolean next) {
        LogUtil.logd("update snap " + next);
    }

    @Override
    public List<View> getFocusViews() {
        return mItemViews;
    }

    @Override
    public boolean supportKeyEvent() {
        return true;
    }

    private void loadDrawableByUrl(final ImageView ivHead, String uri) {
        Bitmap bitmap = null;
        synchronized (mCachePost) {
            bitmap = mCachePost.get(uri);
        }

        if (bitmap != null) {
            UI2Manager.runOnUIThread(new Runnable1<Bitmap>(bitmap) {

                @Override
                public void run() {
                    ivHead.setImageBitmap(mP1);
                    ivHead.setVisibility(View.VISIBLE);
                }
            }, 0);
            return;
        }

        ImageLoaderImpl.getInstance().displayImage(uri, ivHead, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
                ((ImageView) view).setImageResource(R.drawable.def_moive);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                super.onLoadingFailed(imageUri, view, failReason);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                super.onLoadingComplete(imageUri, view, loadedImage);
                if (loadedImage != null) {
                    ((ImageView) view).setImageBitmap(loadedImage);
                    view.setVisibility(View.VISIBLE);
                    synchronized (mCachePost) {
                        mCachePost.put(imageUri, loadedImage);
                    }
                }
            }
        });
    }

    @DrawableRes
    private int getSoreMark(double score) {
        if (score < 1.0f) {
            return R.drawable.dz_icon_star0;
        } else if (score < 2.0f) {
            return R.drawable.dz_icon_star1;
        } else if (score < 3.0f) {
            return R.drawable.dz_icon_star2;
        } else if (score < 4.0f) {
            return R.drawable.dz_icon_star3;
        } else if (score < 5.0f) {
            return R.drawable.dz_icon_star4;
        } else if (score < 6.0f) {
            return R.drawable.dz_icon_star5;
        } else if (score < 7.0f) {
            return R.drawable.dz_icon_star6;
        } else if (score < 8.0f) {
            return R.drawable.dz_icon_star7;
        } else if (score < 9.0f) {
            return R.drawable.dz_icon_star8;
        } else if (score < 10.0f) {
            return R.drawable.dz_icon_star9;
        } else {
            return R.drawable.dz_icon_star10;
        }
    }

    @Override
    public void updateItemSelect(int arg0) {

    }

    @Override
    public boolean hasViewAnimation() {
        return true;
    }
}
