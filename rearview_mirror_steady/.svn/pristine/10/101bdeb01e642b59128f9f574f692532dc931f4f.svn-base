package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.BitmapCache;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ImageUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.CarManualViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICarManualView;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.resholder.R;

/**
 * 说明：电子手册
 *
 * @author xiaolin
 * create at 2020-10-18 12:14
 */
public class CarManualView extends ICarManualView {

    private static CarManualView mCarManualView = new CarManualView();

    private CarManualView() {
    }

    public static CarManualView getInstance() {
        return mCarManualView;
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData viewData) {
        CarManualViewData carManualViewData = (CarManualViewData) viewData;
        WinLayout.getInstance().vTips = carManualViewData.vTips;

        View view = getView(carManualViewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = viewData.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.object = getInstance();

        return viewAdapter;
    }

    private View getView(CarManualViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.car_manual_view, (ViewGroup) null);

        ImageView ivImage = view.findViewById(R.id.ivImage);
        TextView tvMessage = view.findViewById(R.id.tvMessage);
        tvMessage.setText(viewData.message);

        String img = viewData.img;

        try {
            if (img.startsWith("http:") || img.startsWith("https:")) {
                loadDrawableByUrl(ivImage, img);
            } else if (img.startsWith("file:")) {
                Bitmap bitmap = ImageUtils.getBitmap(img.substring("file:".length()));
                ivImage.setImageBitmap(bitmap);
            } else {
                Bitmap bitmap = ImageUtils.getBitmap(img);
                ivImage.setImageBitmap(bitmap);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void loadDrawableByUrl(final ImageView ivHead, String uri) {
        ImageLoaderInitialize.ImageLoaderImpl.getInstance().displayImage(uri, ivHead, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                super.onLoadingStarted(imageUri, view);
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
                }
            }
        });
    }

}
