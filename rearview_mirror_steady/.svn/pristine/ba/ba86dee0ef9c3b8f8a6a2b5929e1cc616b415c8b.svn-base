package com.txznet.comm.ui.theme.test.smarthandyhome;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.util.ImageUtils;
import com.txznet.comm.ui.viewfactory.data.SmartHandyHomeViewData;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;

/**
 * 说明：发现新功能
 *
 * @author xiaolin
 * create at 2020-11-07 10:18
 */
public class HomeNewFunctionHolder {

    private static HomeNewFunctionHolder instance = new HomeNewFunctionHolder();
    public static HomeNewFunctionHolder getInstance(){
        return instance;
    }

    private View mRootView;
    private ImageView ivImage;

    private SmartHandyHomeViewData.NewsData newsData = null;

    public View getView(){
        if(mRootView == null){
            Context context = UIResLoader.getInstance().getModifyContext();
            mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_home_item_news, (ViewGroup)null);
            init();
        }
        return mRootView;
    }

    private void init(){
        ivImage = mRootView.findViewById(R.id.ivImage);
        mRootView.findViewById(R.id.imgBtnMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_NEW_FUNCTION_MORE,
                        0, 0, 0);
            }
        });
        ivImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_NEW_FUNCTION_DETAIL,
                        0, 0, 0, JSONObject.toJSONString(newsData));
            }
        });
    }

    public void update(SmartHandyHomeViewData.NewsData viewData){
        this.newsData = viewData;
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
