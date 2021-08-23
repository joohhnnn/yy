package com.txznet.comm.ui.theme.test.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.LruCache;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.view.RoundImageView;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.IMovieSeatPlanView;
import com.txznet.comm.ui.viewfactory.data.MovieSeatPlanViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.ListTitleView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.txz.util.runnables.Runnable1;

public class MovieSeatPlanView extends IMovieSeatPlanView {

    private static MovieSeatPlanView sInstance = new MovieSeatPlanView();

    private LruCache<String, Bitmap> mCachePost = new LruCache<String, Bitmap>(ConfigUtil.getCinemaItemCount());
    private MovieSeatPlanView(){}

    public static MovieSeatPlanView getInstance(){
        return sInstance;
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        MovieSeatPlanViewData viewData = (MovieSeatPlanViewData) data;
        WinLayout.getInstance().vTips = viewData.vTips;
        LinearLayout lyContents = new LinearLayout(GlobalContext.get());
        lyContents.setOrientation(LinearLayout.VERTICAL);
        LinearLayout lyTitle = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, com.txznet.comm.ui.viewfactory.view.defaults.ListTitleView.getInstance().getTitleHeight());
        layoutParams.topMargin = (int) LayouUtil.getDimen("y24");
        lyTitle.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams.gravity = Gravity.CENTER;
        lyContents.addView(lyTitle, layoutParams);
        ImageView ivTitle = new ImageView(GlobalContext.get());
        ivTitle.setScaleType(ImageView.ScaleType.FIT_CENTER);
        layoutParams = new LinearLayout.LayoutParams( (int)LayouUtil.getDimen("x40"), com.txznet.comm.ui.viewfactory.view.defaults.ListTitleView.getInstance().getTitleHeight());
        layoutParams.leftMargin = (int)LayouUtil.getDimen("m6");
        ivTitle.setImageDrawable(LayouUtil.getDrawable("icon_seat"));
        lyTitle.addView(ivTitle, layoutParams);
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setText("座位选择");
        TextViewUtil.setTextSize(tvTitle,LayouUtil.getDimen("m22"));
        TextViewUtil.setTextColor(tvTitle, Color.parseColor("#85868B"));
        layoutParams = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT,ListTitleView.getInstance().getTitleHeight());
        layoutParams.topMargin = (int)LayouUtil.getDimen("m13");
        lyTitle.addView(tvTitle, layoutParams);
        RoundImageView ivSeat = new RoundImageView(GlobalContext.get(),true);
        ivSeat.setScaleType(ImageView.ScaleType.FIT_XY);
        loadDrawableByUrl(ivSeat, viewData.seatPlanImageUrl);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        lyContents.addView(ivSeat, layoutParams);

        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = lyContents;
        viewAdapter.object = MovieSeatPlanView.getInstance();
        return viewAdapter;
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

}
