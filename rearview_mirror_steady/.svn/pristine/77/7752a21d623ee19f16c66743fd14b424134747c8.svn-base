package com.txznet.comm.ui.viewfactory.view.defaults;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.view.ScaleImageView;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.FilmListViewData;
import com.txznet.comm.ui.viewfactory.data.IFilmListView;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.runnables.Runnable1;

import java.util.ArrayList;
import java.util.List;

public class DefaultFilmListView extends IFilmListView {

    private static DefaultFilmListView sInstance = new DefaultFilmListView();

    private List<View> mItemViews;

    private LruCache<String, Bitmap> mCachePost = new LruCache<String, Bitmap>(ConfigUtil.getCinemaItemCount());

    private DefaultFilmListView(){}

    public static DefaultFilmListView getInstance(){
        return sInstance;
    }

    @SuppressLint("NewApi")
    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        FilmListViewData filmListViewData = (FilmListViewData) data;
        ViewFactory.ViewAdapter titleViewAdapter = ListTitleView.getInstance().getView(filmListViewData);

        LinearLayout llLayout = new LinearLayout(GlobalContext.get());
        llLayout.setGravity(Gravity.CENTER_VERTICAL);
        llLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llLayout.addView(titleViewAdapter.view,layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ConfigUtil.getDisplayLvItemH(false) * ConfigUtil.getVisbileCount());
        layoutParams.gravity = Gravity.CENTER;
        llLayout.addView(llContents,layoutParams);

        mItemViews = new ArrayList<View>();

        for (int i = 0; i < filmListViewData.count; i++) {
            View itemView ;
            itemView = createMoiveItemView(i, filmListViewData.getData().get(i));
            layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
            llContents.addView(itemView,layoutParams);
            mItemViews.add(itemView);
        }
        //处理列表选择数不足4个时，添加空的选择元素。
        for(int i = mItemViews.size(); i < ConfigUtil.getCinemaItemCount(); i++){
            View itemView = new View(GlobalContext.get());
            layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
            llContents.addView(itemView,layoutParams);
        }
        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = llLayout;
        viewAdapter.isListView = true;
        viewAdapter.object = DefaultFilmListView.getInstance();
        return viewAdapter;
    }

    @Override
    public void init() {
        super.init();
    }

    @SuppressLint("NewApi")
    private View createMoiveItemView(int position, FilmListViewData.FilmBean filmBean) {
        RippleView itemView = new RippleView(GlobalContext.get());
        itemView.setTag(position);
        itemView.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        itemView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        itemView.setPadding(2, 2, 2, 2);
        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOrientation(LinearLayout.VERTICAL);
        llItem.setBackground(LayouUtil.getDrawable("movie_rang_bg"));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("x159"),
                LinearLayout.LayoutParams.MATCH_PARENT);
        llItem.setLayoutParams(layoutParams);

        ScaleImageView ivBill = new ScaleImageView(GlobalContext.get());
        ivBill.setMode(ScaleImageView.MODE_AUTO_HEIGHT);
        ivBill.setScale(1.5f);
        ivBill.setScaleType(ImageView.ScaleType.CENTER_CROP);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 2);
        llItem.addView(ivBill, layoutParams);

        LinearLayout llcon = new LinearLayout(GlobalContext.get());
        llcon.setOrientation(LinearLayout.VERTICAL);
        llcon.setGravity(Gravity.CENTER);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);
        llItem.addView(llcon, layoutParams);

        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setSingleLine();
        tvTitle.setPadding((int) LayouUtil.getDimen("y10"), (int) LayouUtil.getDimen("y20"), (int) LayouUtil.getDimen("y10"), (int) LayouUtil.getDimen("y5"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        //layoutParams.topMargin = tvNameTopMargin;
        llcon.addView(tvTitle, layoutParams);

        //layoutParams.bottomMargin = tvNameBottomMargin;

        LinearLayout llScore = new LinearLayout(GlobalContext.get());
        llScore.setGravity(Gravity.CENTER);
        llScore.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)LayouUtil.getDimen("y30"));
        layoutParams.gravity = Gravity.CENTER;
        llcon.addView(llScore, layoutParams);
        if(filmBean.score > 0){
            ImageView ivScore = new ImageView(GlobalContext.get());
            ivScore.setScaleType(ImageView.ScaleType.FIT_CENTER);
            layoutParams = new LinearLayout.LayoutParams(0, (int) LayouUtil.getDimen("y20"));
            layoutParams.rightMargin = (int) LayouUtil.getDimen("x2");
            //layoutParams.gravity = Gravity.CENTER;
            layoutParams.weight = 0.65f;
            llScore.addView(ivScore, layoutParams);

            RelativeLayout rlScore = new RelativeLayout(GlobalContext.get());
            layoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.weight = 0.35f;
            llScore.addView(rlScore, layoutParams);

            TextView tvScorePre = new TextView(GlobalContext.get());
            tvScorePre.setIncludeFontPadding(false);
            tvScorePre.setSingleLine();
            tvScorePre.setId(ViewUtils.generateViewId());
            RelativeLayout.LayoutParams mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rlScore.addView(tvScorePre, mRLayoutParams);

            TextView tvScoreAft = new TextView(GlobalContext.get());
            tvScoreAft.setSingleLine();
            mRLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            mRLayoutParams.addRule(RelativeLayout.RIGHT_OF, tvScorePre.getId());
            mRLayoutParams.addRule(RelativeLayout.ALIGN_BASELINE, tvScorePre.getId());
            rlScore.addView(tvScoreAft, mRLayoutParams);
            TextViewUtil.setTextSize(tvScorePre, LayouUtil.getDimen("m30"));
            TextViewUtil.setTextColor(tvScorePre, Color.parseColor("#FA952F"));
            TextViewUtil.setTextSize(tvScoreAft, LayouUtil.getDimen("m18"));
            TextViewUtil.setTextColor(tvScoreAft, Color.parseColor("#FA952F"));
            String sc = String.format("%.1f", filmBean.score);
            if (sc.contains(".")) {
                String[] scoreArray = sc.split("\\.");
                if (scoreArray != null && scoreArray.length > 1) {
                    tvScorePre.setText(scoreArray[0]);
                    tvScoreAft.setText("." + scoreArray[1]);
                }
            } else {
                tvScorePre.setText(LanguageConvertor.toLocale(sc));
                tvScoreAft.setText("");
            }

            ivScore.setImageDrawable(getSoreMark(filmBean.score));
            if (ivScore.getVisibility() != View.VISIBLE) {
                ivScore.setVisibility(View.VISIBLE);
            }
        }else {
            TextView tvNoScore = new TextView(GlobalContext.get());
            tvNoScore.setText("暂无评分");
            TextViewUtil.setTextSize(tvNoScore, LayouUtil.getDimen("m16"));
            TextViewUtil.setTextColor(tvNoScore, Color.parseColor("#FFFFFF"));
            layoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            llScore.addView(tvNoScore, layoutParams);
        }

        TextViewUtil.setTextSize(tvTitle, LayouUtil.getDimen("m24"));
        TextViewUtil.setTextColor(tvTitle, Color.WHITE);


        tvTitle.setText(LanguageConvertor.toLocale(filmBean.title));
        loadDrawableByUrl(ivBill, filmBean.post);



        itemView.addView(llItem);
        return itemView;

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

        ImageLoaderInitialize.ImageLoaderImpl.getInstance().displayImage(uri,ivHead, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
                ((ImageView) view).setImageDrawable(LayouUtil.getDrawable("def_moive"));
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

    private Drawable getSoreMark(double score) {
        if (score < 1.0f) {
            return LayouUtil.getDrawable("dz_icon_star0");
        } else if (score < 2.0f) {
            return LayouUtil.getDrawable("dz_icon_star1");
        } else if (score < 3.0f) {
            return LayouUtil.getDrawable("dz_icon_star2");
        } else if (score < 4.0f) {
            return LayouUtil.getDrawable("dz_icon_star3");
        } else if (score < 5.0f) {
            return LayouUtil.getDrawable("dz_icon_star4");
        } else if (score < 6.0f) {
            return LayouUtil.getDrawable("dz_icon_star5");
        } else if (score < 7.0f) {
            return LayouUtil.getDrawable("dz_icon_star6");
        } else if (score < 8.0f) {
            return LayouUtil.getDrawable("dz_icon_star7");
        } else if (score < 9.0f) {
            return LayouUtil.getDrawable("dz_icon_star8");
        } else if (score < 10.0f) {
            return LayouUtil.getDrawable("dz_icon_star9");
        } else {
            return LayouUtil.getDrawable("dz_icon_star10");
        }
    }

    @Override
    public void updateProgress(int progress, int selection) {

    }

    @Override
    public void snapPage(boolean next) {

    }

    @Override
    public void updateItemSelect(int selection) {

    }

}
