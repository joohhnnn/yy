package com.txznet.music.albumModule.ui;

import com.google.gson.reflect.TypeToken;
import com.txznet.loader.AppLogic;
import com.txznet.music.data.entity.Category;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.data.http.resp.Homepage;
import com.txznet.music.utils.JsonHelper;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by brainBear on 2018/2/23.
 */

public class CategoryDataSource implements CategoryContract.DataSource {


    private static CategoryDataSource sInstance;

    private CategoryDataSource() {

    }

    public static CategoryDataSource getInstance() {
        if (null == sInstance) {
            synchronized (CategoryDataSource.class) {
                if (null == sInstance) {
                    sInstance = new CategoryDataSource();
                }
            }
        }
        return sInstance;
    }

    @Override
    public void queryCategories(final CategoryContract.QueryCategoriesListener listener) {

        NetManager.getInstance().requestCategory(new RequestCallBack<String>(String.class) {
            @Override
            public void onResponse(String data) {
                final Homepage<Category> homepage = JsonHelper.toObject(data, new TypeToken<Homepage<Category>>() {
                }.getType());
                handleCategory(homepage, listener);
            }

            @Override
            public void onError(String cmd, final Error error) {
                listener.onError(error);
            }
        });
    }

    @Override
    public Observable queryAlbumList(Category category) {
//        ReqSearchAlbum reqSearchAlbum = new ReqSearchAlbum();
//        reqSearchAlbum.setPageId(1);
//        reqSearchAlbum.setCategoryId(category.getCategoryId());
//        RxNet.request(Constant.GET_SEARCH_LIST, reqSearchAlbum, ResponseSearchAlbum.class).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new TXZObserver<ResponseSearchAlbum>() {
//            @Override
//            public void onResponse(ResponseSearchAlbum responseAlbum) {
//
//
//                pageOff = responseAlbum.getPageId();
//                handleAlbum(responseAlbum.getArrAlbum(), pageOff != 1);
//            }
//        });

        return null;
    }


    private void handleCategory(Homepage<Category> homepage, final CategoryContract.QueryCategoriesListener listener) {
        if (null == homepage || homepage.getErrCode() != 0) {
            listener.onError(new Error(Error.ERROR_CLIENT_QUERY_CATEGORY_ERROR));
            return;
        }

        listener.onQueryCategories(homepage.getArrCategory());
        saveCategories(homepage.getArrCategory());
    }


    private void saveCategories(final List<Category> categories) {
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                DBManager.getInstance().saveToCategory(categories);
            }
        });
    }
}
