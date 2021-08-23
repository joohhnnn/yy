package com.txznet.music.albumModule.logic;

import com.google.gson.reflect.TypeToken;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.CollectionUtils;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Category;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.ui.bean.Homepage;
import com.txznet.music.util.TestUtil;
import com.txznet.music.util.TimeUtils;
import com.txznet.music.utils.JsonHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS User on 2016/11/7.
 */
public class CategoryEngine {

    /**
     * 获取相应子模块下的分类
     *
     * @param categoryId 0表示基类，表示获取全部
     * @return
     */
    public static final int ALL_CATEGORY_FLAG = 0;
    private static final String TAG = "Music:CategoryEngine:";
    private static CategoryEngine mInstance;
    private List<Category> mCategories;
    private boolean isQuerySuccess = false;

    private CategoryEngine() {
        mCategories = new ArrayList<Category>();
    }

    public static CategoryEngine getInstance() {
        if (mInstance == null) {
            synchronized (CategoryEngine.class) {
                if (mInstance == null) {
                    mInstance = new CategoryEngine();
                }
            }
        }
        return mInstance;
    }

    RequestCallBack categoryCallback = new RequestCallBack<String>(String.class) {
        @Override
        public void onResponse(String data) {
            Homepage<Category> homepage = JsonHelper.toObject(data, new TypeToken<Homepage<Category>>() {
            }.getType());
            handleCategory(homepage);
        }

        @Override
        public void onError(String cmd, Error error) {
            //重新发起请求
            queryCategory();

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LogUtil.e(TAG + " request category error " + error.getErrorCode());
        }
    };
    Runnable categoryRunnable = new Runnable() {
        @Override
        public void run() {
            NetManager.getInstance().requestCategory(categoryCallback);
        }
    };


    public void queryCategory() {
//        if (!isQuerySuccess) {
//            AppLogic.runOnBackGround(categoryRunnable, 0);
//        } else {
//            ObserverManage.getObserver().send(InfoMessage.REQ_CATEGORY_ALL);
//        }
    }

    public void handleCategory(Homepage<Category> homepage) {
        isQuerySuccess = false;
        if (homepage == null) {
            LogUtil.loge(TAG + "category is null");
            return;
        }
        if (homepage.getErrCode() != 0) {
            LogUtil.loge(TAG + "category occur error:" + homepage.getErrCode());
            return;
        }
        LogUtil.logd(TAG + "reqType:" + homepage.getReqType());
        if (CollectionUtils.isNotEmpty(homepage.getArrCategory())) {
            mCategories = homepage.getArrCategory();
            isQuerySuccess = true;
            ObserverManage.getObserver().send(InfoMessage.REQ_CATEGORY_ALL);
            AppLogic.runOnBackGround(new Runnable() {
                @Override
                public void run() {
                    DBManager.getInstance().saveToCategory(mCategories);
                }
            }, 0);
        }
    }

    public List<Category> getMusicCategory() {
        List<Category> categories = new ArrayList<Category>();
        for (Category category : mCategories) {
            if (category.getCategoryId() == 100000) {
                categories.addAll(category.getArrChild());
            }
        }
        return categories;
    }

    public List<Category> getRadioCategory() {
        List<Category> categories = new ArrayList<Category>();
        for (Category category : mCategories) {
            if (category.getCategoryId() != 100000 && category.getCategoryId() != 1) {
                categories.add(category);
            }
        }
        return categories;
    }

    public List<Category> getChildCategory(int categoryId) {
        if (categoryId == ALL_CATEGORY_FLAG) {
            return mCategories;
        }

        List<Category> categories = new ArrayList<Category>();
        if (mCategories != null && CollectionUtils.isNotEmpty(mCategories)) {
            for (Category category : mCategories) {
                if (category.getCategoryId() == categoryId) {
                    categories.addAll(category.getArrChild());
                }
            }
        }
        return categories;
    }
}
