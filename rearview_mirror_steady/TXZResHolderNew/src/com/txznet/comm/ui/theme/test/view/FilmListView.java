package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.ScaleImageView;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.FilmListViewData;
import com.txznet.comm.ui.viewfactory.data.IFilmListView;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.ListTitleView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.runnables.Runnable1;

import java.util.ArrayList;
import java.util.List;

public class FilmListView extends IFilmListView {

    private int contentHeight;    //内容高度
    private int ivBillWidth;    //电影海报宽度
    private int ivBillHeight;    //电影海报高度
    private int itemHeight;    //列表高度
    private int tvNameSize;    //电影名称字号
    private int tvNameHeight;    //电影名称行高
    private int tvNameColor;    //电影名称字体颜色
    private int tvNameTopMargin;    //电影名称上边距
    private int tvNameBottomMargin;    //电影名称下边距
    private int ivStarBgColor;    //无屏评分背景颜色
    private int ivStarWidth;    //评分星星宽度
    private int ivStarHeight;    //评分星星高度
    private int ivStarRightMargin;    //评分星星右边距
    private int tvScorePreSize;    //评分数值整数字号
    private int tvScorePreHeight;    //评分数值整数行高
    private int tvScoreColor;    //评分数值颜色
    private int tvScoreAftSize;    //评分数值分数字号

    private int dividerHeight;

    private static FilmListView sInstance = new FilmListView();

    private FilmListView(){}

    private List<View> mItemViews;

    private LruCache<String, Bitmap> mCachePost = new LruCache<String, Bitmap>(ConfigUtil.getCinemaItemCount());

    public static FilmListView getInstance(){
        return sInstance;
    }

    @Override
    public void updateProgress(int i, int i1) {

    }

    @Override
    public void snapPage(boolean b) {

    }

    @Override
    public void init() {
        super.init();
        initFull();
    }

    private void initFull(){
        // 初始化配置，例如字体颜色等
        if (WinLayout.isVertScreen){
            int unit = (int) LayouUtil.getDimen("vertical_unit");
            ivBillWidth = (SizeConfig.screenWidth - 6 * unit)/3;
            ivBillHeight = ivBillWidth * 3 / 2;
            tvNameSize = (int) LayouUtil.getDimen("vertical_h3");
            tvNameHeight = (int) LayouUtil.getDimen("vertical_h3_height");
            tvNameTopMargin = 2 * unit;
            tvNameBottomMargin = unit;
            ivStarWidth = (int) (ivBillWidth * 0.7);
            ivStarHeight = (int) LayouUtil.getDimen("vertical_h1");
            ivStarRightMargin = unit / 3;
            tvScorePreSize = (int) LayouUtil.getDimen("vertical_h1");
            tvScorePreHeight = (int) LayouUtil.getDimen("vertical_h1_height");
            tvScoreAftSize = (int) LayouUtil.getDimen("vertical_h3");
            itemHeight = ivBillHeight + tvNameTopMargin + tvNameHeight + tvNameBottomMargin + tvScorePreHeight + tvNameTopMargin;
            //itemHeight = SizeConfig.itemHeight * SizeConfig.pageCount - unit;
            contentHeight = itemHeight + unit;
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            ivBillWidth = (int) LayouUtil.getDimen("billWidth");
            ivBillHeight = (int) LayouUtil.getDimen("billHeight");
            tvNameSize = (int) LayouUtil.getDimen("h3");
            tvNameHeight = (int) LayouUtil.getDimen("h3_height");
            tvNameTopMargin = 2 * unit;
            tvNameBottomMargin = unit;
            ivStarWidth = (int) (ivBillWidth * 0.7);
            ivStarHeight = (int) LayouUtil.getDimen("h1");
            ivStarRightMargin = unit / 3;
            tvScorePreSize = (int) LayouUtil.getDimen("h1");
            tvScorePreHeight = (int) LayouUtil.getDimen("h1_height");
            tvScoreAftSize = (int) LayouUtil.getDimen("h6");
            if (SizeConfig.pageCount < 4){
                tvNameBottomMargin = 0;    //去掉电影名底部间距,增加界面内容高度
                itemHeight = ivBillHeight + tvNameTopMargin + tvNameHeight + tvScorePreHeight + tvNameTopMargin;
            }else {
                itemHeight = SizeConfig.itemHeight * SizeConfig.pageCount - unit;
            }
            //contentHeight = SizeConfig.itemHeight * SizeConfig.pageCount;
            contentHeight = itemHeight + unit;
        }
    }


    @Override
    public void updateItemSelect(int index) {
        showSelectItem(index);
    }

    private void showSelectItem(int index){
        index = index % SizeConfig.pageMovieCount;
        for (int i = 0;i< mItemViews.size();i++){
            if (i == index){
                mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
            }else {
                mItemViews.get(i).setBackground(null);
            }
        }
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData viewData) {

            FilmListViewData filmListViewData = (FilmListViewData) viewData;
            WinLayout.getInstance().vTips = filmListViewData.vTips;
            ViewFactory.ViewAdapter titleViewAdapter = com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getView(filmListViewData,"movie","电影");
            LinearLayout llLayout = new LinearLayout(GlobalContext.get());
            llLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,ListTitleView.getInstance().getTitleHeight());
            layoutParams.topMargin = (int) LayouUtil.getDimen("y24");
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            llLayout.addView(titleViewAdapter.view,layoutParams);

            LinearLayout llContents = new LinearLayout(GlobalContext.get());
            llContents.setOrientation(LinearLayout.HORIZONTAL);
            llContents.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));



            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.CENTER;
            llLayout.addView(llContents,layoutParams);

            mItemViews = new ArrayList<View>();

            for (int i = 0; i < filmListViewData.count; i++) {
                View itemView ;
                itemView = createMoiveItemView(i, filmListViewData.getData().get(i));
                layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
                if(i != filmListViewData.count - 1){
                    layoutParams.rightMargin = (int)LayouUtil.getDimen("x5");
                }
                layoutParams.topMargin = (int)LayouUtil.getDimen("x2");
                layoutParams.bottomMargin = (int)LayouUtil.getDimen("x2");
                llContents.addView(itemView,layoutParams);
                mItemViews.add(itemView);
            }
            //处理列表选择数不足个最大一页数量时，添加空的选择元素。
            for(int i = mItemViews.size(); i < SizeConfig.pageMovieCount; i++){
                View itemView = new View(GlobalContext.get());
                layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
                llContents.addView(itemView,layoutParams);
            }
            LinearLayout llPager = new PageView(GlobalContext.get(),filmListViewData.mTitleInfo.curPage,filmListViewData.mTitleInfo.maxPage);
            layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
            llContents.addView(llPager,layoutParams);
            ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
            viewAdapter.type = viewData.getType();
            viewAdapter.view = llLayout;
            viewAdapter.isListView = true;
            viewAdapter.object = FilmListView.getInstance();
            return viewAdapter;

    }

    @SuppressLint("NewApi")
    private View createMoiveItemView(int position, FilmListViewData.FilmBean filmBean) {
        LinearLayout llItem = new LinearLayout(GlobalContext.get());
        llItem.setOrientation(LinearLayout.VERTICAL);
        llItem.setGravity(Gravity.CENTER_HORIZONTAL);
        llItem.setBackground(LayouUtil.getDrawable("movie_bg"));
        llItem.setPadding(2, 2, 2, 2);
        llItem.setTag(position);
        llItem.setOnClickListener(ListTitleView.getInstance().getOnItemClickListener());
        llItem.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        showSelectItem((int) v.getTag());
                        break;
                }
                return false;
            }
        });
        // 设置2px的padding用来显示焦点框
        //llItem.setPadding(2, 2, 2, 2);
        //llItem.setBackground(LayouUtil.getDrawable("white_bottom_range_layout"));

        //ScaleImageView ivBill = new ScaleImageView(GlobalContext.get());
        ImageView ivBill = new ImageView(GlobalContext.get());
        //ivBill.setMode(ScaleImageView.MODE_AUTO_HEIGHT);
        //ivBill.setScale(1.5f);
        ivBill.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //ivBill.setAdjustViewBounds(true);
        //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        //LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,(int) LayouUtil.getDimen("y258"));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ivBillWidth, ivBillHeight);
        llItem.addView(ivBill, layoutParams);

		/*LinearLayout lBottom = new LinearLayout(GlobalContext.get());
		lBottom.setOrientation(LinearLayout.VERTICAL);
		layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,0,1);
		llItem.addView(lBottom,layoutParams);*/

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setGravity(Gravity.CENTER_VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(ivBillWidth, itemHeight - ivBillHeight);
        llItem.addView(llContent, layoutParams);

        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setSingleLine();
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, tvNameHeight);
        //layoutParams.topMargin = tvNameTopMargin;
        layoutParams.bottomMargin = tvNameBottomMargin;
        llContent.addView(tvTitle, layoutParams);

        FrameLayout fLScore = new FrameLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        //layoutParams.bottomMargin = tvNameBottomMargin;
        llContent.addView(fLScore, layoutParams);

        LinearLayout llScore = new LinearLayout(GlobalContext.get());
        llScore.setGravity(Gravity.CENTER);
        llScore.setOrientation(LinearLayout.HORIZONTAL);
        FrameLayout.LayoutParams fLayoutParams = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, tvScorePreSize);
        fLayoutParams.gravity = Gravity.CENTER;
        fLScore.addView(llScore, fLayoutParams);
        if(filmBean.score > 0){

            ImageView ivScore = new ImageView(GlobalContext.get());
            ivScore.setScaleType(ImageView.ScaleType.FIT_CENTER);
            layoutParams = new LinearLayout.LayoutParams(ivStarWidth, ivStarHeight);
            layoutParams.rightMargin = ivStarRightMargin;
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            llScore.addView(ivScore, layoutParams);

            TextView tvScorePre = new TextView(GlobalContext.get());
            tvScorePre.setGravity(Gravity.BOTTOM);
            tvScorePre.setIncludeFontPadding(false);
            tvScorePre.setSingleLine();
            tvScorePre.setId(ViewUtils.generateViewId());
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, tvScorePreHeight);
            layoutParams.gravity = Gravity.BOTTOM;
            llScore.addView(tvScorePre, layoutParams);

            TextView tvScoreAft = new TextView(GlobalContext.get());
            tvScoreAft.setGravity(Gravity.BOTTOM);
            tvScoreAft.setSingleLine();
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            //layoutParams.gravity = Gravity.BOTTOM;
            llScore.addView(tvScoreAft, layoutParams);
            TextViewUtil.setTextSize(tvScorePre, tvScorePreSize);
            TextViewUtil.setTextColor(tvScorePre, Color.parseColor("#FA952F"));
            TextViewUtil.setTextSize(tvScoreAft, tvScoreAftSize);
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
            llScore.addView(tvNoScore, layoutParams);
        }

        TextViewUtil.setTextSize(tvTitle, tvNameSize);
        TextViewUtil.setTextColor(tvTitle, Color.WHITE);


        tvTitle.setText(LanguageConvertor.toLocale(filmBean.title));
        loadDrawableByUrl(ivBill, filmBean.post);



		/*llItem.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				LinearLayout itemView = (LinearLayout) v;
				if (hasFocus) {
					itemView.setBackgroundColor(Color.parseColor("#4AA5FA"));
				} else {
					itemView.setBackground(LayouUtil.getDrawable("white_range_layout"));
				}
			}
		});*/

        return llItem;
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

}
