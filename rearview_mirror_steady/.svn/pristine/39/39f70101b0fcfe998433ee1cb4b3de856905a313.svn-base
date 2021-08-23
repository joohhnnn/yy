package com.txznet.music.fragment.manager;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.dao.AlbumDBHelper;
import com.txznet.fm.dao.CategoryDBHelper;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.bean.response.Album;
import com.txznet.music.bean.response.Category;
import com.txznet.music.bean.response.ResponseSearchAlbum;
import com.txznet.music.utils.StringUtils;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * 数据库帮助类
 */
public class DBManage implements Observer {

    private static final String TAG = "[manager] ";

    // 单例
    private static DBManage instance;

    private DBManage() {
    	ObserverManage.getObserver().addObserver(this);
    }

    public static DBManage getInstance() {
        if (instance == null) {
            synchronized (DBManage.class) {
                if (instance == null) {
                    instance = new DBManage();
                }
            }
        }
        return instance;
    }

    /**
     * 保存到数据库
     *
     * @param albums
     * @param categoryID
     * @param i 
     */
    public void saveToAlbum(List<Album> albums, int categoryID, int pageID) {
        LogUtil.logd(TAG + "saveAlbum size :"
                + (albums != null ? albums.size() : 0) + ",current categoryID:"
                + categoryID);
        if (pageID==1) {
			//如果是第一页，则删除相应的数据
        	AlbumDBHelper.getInstance().remove(AlbumDBHelper.TABLE_CATEGORYID+"=?", new String[]{String.valueOf(categoryID)});
		}
        
        for (Album album : albums) {
            album.setCategoryID(categoryID);
        }
		AlbumDBHelper.getInstance().saveOrUpdate(albums);
//        AlbumDBUtils.getInstance().saveOrUpdate(albums);
    }

    /**
     * 保存到数据库
     *
     * @param arrCategory
     */
    public void saveToCategory(List<Category> arrCategory) {
        LogUtil.logd(TAG + "saveCategory size :"
                + (arrCategory != null ? arrCategory.size() : 0));
        CategoryDBHelper.getInstance().removeAll();
        CategoryDBHelper.getInstance().saveOrUpdate(arrCategory);
    }

    @Override
    public void update(Observable observable, final Object data) {
        AppLogic.runOnBackGround(new Runnable() {

            @Override
            public void run() {
                // 更新数据库
                if (data instanceof InfoMessage) {
                    InfoMessage info = (InfoMessage) data;
                    switch (info.getType()) {
                        case InfoMessage.REQ_CATEGORY_ALL:
                            List<Category> arrCategory = (List<Category>) info
                                    .getObj();
                            saveToCategory(arrCategory);
                            break;
                        case InfoMessage.RESP_ALBUM:
                            ResponseSearchAlbum responseAlbum = (ResponseSearchAlbum) info
                                    .getObj();
                            if (StringUtils.isNumeric(responseAlbum.getCategoryId())) {
                                saveToAlbum(responseAlbum.getArrAlbum(), Integer
                                        .parseInt(responseAlbum.getCategoryId()),responseAlbum.getPageId());
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }, 0);
    }

}
