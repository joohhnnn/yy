package com.txznet.music.albumModule.ui;

import com.txznet.comm.remote.util.Logger;
import com.txznet.music.data.entity.Category;
import com.txznet.music.baseModule.bean.Error;

import java.util.List;

/**
 * Created by brainBear on 2018/2/23.
 */

public class CategoryPresenter implements CategoryContract.Presenter {


    private static final String TAG = "CategoryPresenter:";

    private CategoryContract.View mView;

    private CategoryContract.DataSource mDataSource;


    public CategoryPresenter(CategoryContract.View view) {
        this.mView = view;
        this.mDataSource = CategoryDataSource.getInstance();
    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {
        mView = null;
    }

    public List<Category> onCategoriesFilter(List<Category> categories) {
        return categories;
    }


    int retryCount = 0;

    @Override
    public void queryCategories() {
        if (mView == null) {
            return;
        }
        mView.showLoading();
        mDataSource.queryCategories(new CategoryContract.QueryCategoriesListener() {
            @Override
            public void onQueryCategories(List<Category> categories) {
//                List<Category> categoriesFilter = onCategoriesFilter(categories);
                if (null != mView) {
                    mView.showCategories(categories);
                }


                //请求专辑
//                mDataSource.queryAlbumList(categoriesFilter.get(0));
            }

            @Override
            public void onError(Error error) {
                if (retryCount < 5) {
                    retryCount++;
                    queryCategories();
                } else {
                    mView.showError(error.getErrorCode());
                    Logger.e(TAG, error);
                }
            }
        });
    }
}
