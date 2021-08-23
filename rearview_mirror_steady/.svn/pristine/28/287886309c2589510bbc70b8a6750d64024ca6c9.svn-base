package com.txznet.music.albumModule.ui;

import com.txznet.music.data.entity.Category;
import com.txznet.music.baseModule.BasePresenter;
import com.txznet.music.baseModule.BaseView;
import com.txznet.music.baseModule.bean.Error;

import java.util.List;

import io.reactivex.Observable;

/**
 * Created by brainBear on 2018/2/23.
 */

public interface CategoryContract {


    public interface View extends BaseView<Presenter> {

        void showCategories(List<Category> categories);

        void showError(int code);

        void showLoading();
    }

    public interface Presenter extends BasePresenter {

        void queryCategories();

    }


    public interface DataSource {

        void queryCategories(QueryCategoriesListener listener);

        Observable queryAlbumList(Category param);
    }


    public interface QueryCategoriesListener {

        void onQueryCategories(List<Category> categories);

        void onError(Error error);

    }
}
