package com.txznet.music.data.http;

/**
 * Created by telen on 2018/5/21.
 */

public class CategoryManager {

    //##创建一个单例类##
    private volatile static CategoryManager singleton;

    private CategoryManager() {
    }

    public static CategoryManager getInstance() {
        if (singleton == null) {
            synchronized (CategoryManager.class) {
                if (singleton == null) {
                    singleton = new CategoryManager();
                }
            }
        }
        return singleton;
    }


    /**
     * 请求分类
     *
     * @param categoryId
     * @param fromCache
     */
    public void queryCategory(long categoryId, boolean fromCache) {

    }


}
