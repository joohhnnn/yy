package com.txznet.webchat.plugin.preset.logic.util;

import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.webchat.comm.plugin.db.DaoMaster;
import com.txznet.webchat.comm.plugin.db.DaoSession;
import com.txznet.webchat.comm.plugin.db.WxUserCacheEntity;
import com.txznet.webchat.comm.plugin.db.WxUserCacheEntityDao;
import com.txznet.webchat.comm.plugin.model.WxUserCache;
import com.txznet.webchat.comm.plugin.utils.PluginLogUtil;

import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.query.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 本地缓存管理
 * 提供对本地缓存的操作，及缓存数据的持久化
 * <p>
 * Created by J on 2017/3/20.
 */

public final class WxCacheManager {
    public static final String TAG = "WxCacheManager";

    private SerializableCookieStore mCookieStore;
    private WxUserCacheEntity mUserCache;
    //private List<WxUserCache> mCacheList;
    private List<WxUserCacheEntity> mEntityList;

    // DaoSession
    private DaoSession mDaoSession;
    // EntityDao
    private WxUserCacheEntityDao mCacheDao;
    // query
    private Query<WxUserCacheEntity> mQuery;

    private transient static WxCacheManager sInstance;

    public static WxCacheManager getInstance() {
        if (null == sInstance) {
            sInstance = new WxCacheManager();
        }

        return sInstance;
    }

    private WxCacheManager() {
        // 初始化数据库连接
        initDataBase();

        // 初始化缓存列表
        initData();
    }

    private void initDataBase() {
        Database db = new DaoMaster.DevOpenHelper(GlobalContext.get(), "local_cache").getWritableDb();
        mDaoSession = new DaoMaster(db).newSession();
        mCacheDao = mDaoSession.getWxUserCacheEntityDao();
        // 查询使用次数最多的前三个缓存
        mQuery = mCacheDao.queryBuilder().orderDesc(WxUserCacheEntityDao.Properties.LastHit).limit(3).build();
    }

    private void initData() {
        /*mEntityList = mQuery.list();

        // 初始化CacheList
        for (WxUserCacheEntity entity : mEntityList) {
            WxUserCache cache = new WxUserCache(String.valueOf(entity.getUin()), entity.getUserAvatar(), entity.getUserNick());
            mCacheList.add(cache);
        }

        // 初始化数据
        if (mEntityList.isEmpty()) {
            mUserCache = new WxUserCacheEntity();
        } else {
            mUserCache = mEntityList.get(0);
        }

        // 初始化HttpCookie
        mCookieStore = new SerializableCookieStore();
        mCookieStore.decodeStr(mUserCache.getCookie());*/

        mUserCache = new WxUserCacheEntity();
        mCookieStore = new SerializableCookieStore();
    }

    public List<WxUserCache> getUserList() {
        // 从数据库查询数据
        List<WxUserCacheEntity> list = mCacheDao.queryBuilder().orderDesc(WxUserCacheEntityDao.Properties.LastHit).limit(3).build().list();
        List<WxUserCache> ret = new ArrayList<>(3);

        for (WxUserCacheEntity entity : list) {
            WxUserCache cache = new WxUserCache(String.valueOf(entity.getUin()), entity.getUserAvatar(), entity.getUserNick(), entity.getHitCount());
            ret.add(cache);
        }

        return ret;
    }

    public SerializableCookieStore getCookieStore() {
        return mCookieStore;
    }

    public void switchUser(String uin) {
        if (String.valueOf(mUserCache.getUin()).equals(uin)) {
            return;
        }

        // 重新初始化数据
        if (restoreUserCacheFromDB(uin)) {
            mCookieStore.decodeStr(mUserCache.getCookie());
            return;
        }

        // 数据库中不存在对应数据, 初始化新用户
        mUserCache = new WxUserCacheEntity();
        mCookieStore.removeAll();
    }

    public String getLastUin() {
        List<WxUserCacheEntity> list = mCacheDao.queryBuilder().orderDesc(WxUserCacheEntityDao.Properties.LastHit).limit(1).build().list();

        if (list.isEmpty()) {
            return null;
        }

        return String.valueOf(list.get(0).getUin());
    }

    private boolean restoreUserCacheFromDB(final String uin) {
        if (TextUtils.isEmpty(uin)) {
            return false;
        }

        // 从数据库中查找
        List<WxUserCacheEntity> result = mCacheDao.queryBuilder().where(WxUserCacheEntityDao.Properties.Uin.eq(uin)).build().list();
        if (null != result && !result.isEmpty()) {
            mUserCache = result.get(0);

            return true;
        }

        return false;
    }

    public void save() {
        // 更新当前缓存的http cookie
        mUserCache.setCookie(mCookieStore.encodeStr());
        mUserCache.setLastHit(new Date());

        // 将当前缓存更新到数据库
        PluginLogUtil.i(TAG, "saving login cache for uin: " + mUserCache.getUin());
        //mCacheDao.insertOrReplace(mUserCache);
        mCacheDao.save(mUserCache);
    }

    public String getUin() {
        return String.valueOf(mUserCache.getUin());
    }

    public String getAvatar() {
        return mUserCache.getUserAvatar();
    }

    public String getHost() {
        return mUserCache.getHost();
    }

    public void updateUin(String uin) {
        // 修改uin前先检查下数据库中是否存在对应uin的记录, 避免存在本地记录的用户
        // 通过扫码登录导致登录次数等数据被覆盖丢失
        if (restoreUserCacheFromDB(uin)) {
            // 用户扫码登录说明本地登录态已失效, 所以此处不再从数据库恢复cookies
            return;
        }

        mUserCache.setUin(Long.parseLong(uin));
    }

    public void updateAvatar(String avatar) {
        mUserCache.setUserAvatar(avatar);
    }

    public void updateHost(String newHost) {
        mUserCache.setHost(newHost);
    }

    /**
     * 增加命中次数
     */
    public void increaseHitCount() {
        mUserCache.setValid(true);
        mUserCache.setHitCount(mUserCache.getHitCount() + 1);
    }

    public int getHitCount() {
        return mUserCache.getHitCount();
    }

    public void clearCache() {
        mCacheDao.deleteAll();
    }
}
