package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.util.ImageUtils;
import com.txznet.comm.ui.viewfactory.data.SmartHandyNewFunctionDetailViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ISmartHandyNewFunctionDetailView;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;

/**
 * 说明：
 *
 * @author xiaolin
 * create at 2020-11-17 15:20
 */
public class SmartHandyNewFunctionDetailView extends ISmartHandyNewFunctionDetailView {

    private static SmartHandyNewFunctionDetailView instance = new SmartHandyNewFunctionDetailView();

    public static SmartHandyNewFunctionDetailView getInstance() {
        return instance;
    }


    @Override
    public void init() {

    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        View view = createView(data);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.object = getInstance();

        return viewAdapter;
    }

    public View createView(ViewData data) {
        SmartHandyNewFunctionDetailViewData viewData = (SmartHandyNewFunctionDetailViewData) data;

        Context context = UIResLoader.getInstance().getModifyContext();
        View mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_newfunction_detail, (ViewGroup)null);
        ImageView ivImage = mRootView.findViewById(R.id.ivImage);

        String img = viewData.image;
        try {
            if(TextUtils.isEmpty(viewData.image)){
                ivImage.setImageResource(R.drawable.smart_handy_img_news);
            } else if (img.startsWith("http:") || img.startsWith("https:")) {
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

        return mRootView;
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
