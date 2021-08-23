package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.LouAdapter;
import com.txznet.comm.ui.theme.test.utils.LouHolder;
import com.txznet.comm.ui.util.ImageUtils;
import com.txznet.comm.ui.viewfactory.data.SmartHandyNewFunctionListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ISmartHandyNewFunctionListView;
import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;

import java.util.ArrayList;

/**
 * 说明：
 *
 * @author xiaolin
 * create at 2020-11-17 15:20
 */
public class SmartHandyNewFunctionListView extends ISmartHandyNewFunctionListView {

    private static SmartHandyNewFunctionListView instance = new SmartHandyNewFunctionListView();

    public static SmartHandyNewFunctionListView getInstance() {
        return instance;
    }

    private View mRootView;
    private ListView listView;
    private LouAdapter<SmartHandyNewFunctionListViewData.Bean> mAdapter;

    @Override
    public void init() {
        Context context = UIResLoader.getInstance().getModifyContext();
        mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_newfunction_list, (ViewGroup)null);
        listView = mRootView.findViewById(R.id.listView);
        mRootView.findViewById(R.id.imgBtnBack).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_BACK_TO_HOME,
                        0, 0, 0);
            }
        });

        mAdapter = new LouAdapter<SmartHandyNewFunctionListViewData.Bean>(
                        context, listView, R.layout.smart_handy_newfunction_list_item) {
            @Override
            protected void assign(int position, LouHolder holder, SmartHandyNewFunctionListViewData.Bean bean) {
                ImageView ivImage = holder.getView(R.id.ivImage);
                String img = bean.image;
                try {
                    if(TextUtils.isEmpty(bean.image)){
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
        };
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SmartHandyNewFunctionListViewData.Bean bean = mAdapter.getItem(position);
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_NEW_FUNCTION_DETAIL,
                        0, 0, 0, JSONObject.toJSONString(bean));
            }
        });

    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        updateView(data);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = mRootView;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.object = getInstance();

        return viewAdapter;
    }

    @Override
    public Object updateView(ViewData data) {
        SmartHandyNewFunctionListViewData viewData = (SmartHandyNewFunctionListViewData) data;
        mAdapter.initList(new ArrayList<SmartHandyNewFunctionListViewData.Bean>());
        mAdapter.initList(viewData.beans);
        return null;
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
