package com.txznet.music.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;

import com.txznet.comm.base.ActivityStack;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.BaseBarActivity;
import com.txznet.music.ui.PlayDetailsActivity;
import com.txznet.music.ui.PlayRadioRecommendActivity;
import com.txznet.music.ui.SplashActivity;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity2;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity9;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.txznet.music.utils.Utils.KEY_TYPE;

public class JumpUtils {


    //##创建一个单例类##
    private volatile static JumpUtils singleton;

    private JumpUtils() {
    }

    public static JumpUtils getInstance() {
        if (singleton == null) {
            synchronized (JumpUtils.class) {
                if (singleton == null) {
                    singleton = new JumpUtils();
                }
            }
        }
        return singleton;
    }

    /**
     * 获取当前显示的界面
     *
     * @return
     */
    private Activity getCurrentActivity() {
        return ActivityStack.getInstance().currentActivity();
    }


    /**
     * 再播放详情界面，跳转到播放详情界面
     * 播放详情页分为三种：
     * 1.车主超级电台
     * 2.分类Fm
     * 3.普通的播放详情页
     */
    private int getPlayerUIType(Album album) {
        if (album != null) {
            if (album.getParentAlbum() != null) {
                return PlayInfoManager.TYPE_CAR_FM;
            }
            if (album.getAlbumType() == Album.ALBUM_TYPE_NORMAL_FM) {
                return PlayInfoManager.TYPE_NORMAL_FM;
            }
        }
        return PlayInfoManager.TYPE_NORMAL_ALBUM;
    }

    public void jumpToPlayUi(Context ctx, Album album) {
        int playerUIType = getPlayerUIType(album);
        if (playerUIType == PlayInfoManager.TYPE_CAR_FM) {
            jumpToCarFMPlayer(ctx, album);
        } else if (playerUIType == PlayInfoManager.TYPE_NORMAL_FM) {
            jumpToFmPlayer(ctx);
        } else if (playerUIType == PlayInfoManager.TYPE_NORMAL_ALBUM) {
            jumpToNomalDetailPlayer(ctx);
        }
    }

    public boolean isNeedJumpToPlayer(Album album) {
        //如果不在播放界面则不要

        //如果不在播放界面，就不需要跳转，
        //如果再播放界面，则需要判断是否需要跳转
        if (!isInPlayerUI()) {
            return false;
        }
        //如果已经在相同的界面则不需要
        int playerUIType = getPlayerUIType(album);
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return false;
        }
        if (currentActivity instanceof PlayRadioRecommendActivity) {
            if (CollectionUtils.isNotEmpty(PlayInfoManager.getCarFmAlbums())) {
                //表示在车主FM界面
                return playerUIType != PlayInfoManager.TYPE_CAR_FM;
            } else {
                return playerUIType != PlayInfoManager.TYPE_NORMAL_FM;
            }
        }
        if (currentActivity instanceof PlayDetailsActivity) {
            return playerUIType != PlayInfoManager.TYPE_NORMAL_ALBUM;
        }

        return false;

    }

    private void jumpToCarFMPlayer(Context ctx, Album album) {
        Intent intent = new Intent(ctx, ReserveConfigSingleTaskActivity9.class);
        if (!(ctx instanceof Activity)) {
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        }
        Album redayPlayAlbum = null;
        if (PlayInfoManager.getInstance().getParentAlbum() == null) {
            if (album.getParentAlbum() == null) {
                return;
            } else {
                redayPlayAlbum = album.getParentAlbum();
            }
        } else {
            redayPlayAlbum = PlayInfoManager.getInstance().getParentAlbum();
        }

        ctx.startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, PlayInfoManager.DATA_CHEZHU_FM, redayPlayAlbum, redayPlayAlbum.getCategoryId(), "", PlayInfoManager.TYPE_CAR_FM));
    }

    private void jumpToFmPlayer(Context ctx) {
        Intent intent = new Intent(ctx, ReserveConfigSingleTaskActivity9.class);
        if (!(ctx instanceof Activity)) {
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        }
        ctx.startActivity(PlayInfoManager.getInstance().addReadyPlayInfoIntent(intent, PlayInfoManager.DATA_CHEZHU_TYPE_FM, PlayInfoManager.getInstance().getCurrentAlbum(), PlayInfoManager.getInstance().getCurrentAlbum().getCategoryId(), "", PlayInfoManager.TYPE_NORMAL_FM));

    }

    private void jumpToNomalDetailPlayer(Context ctx) {
        if (ctx == null) {
            Logger.d("music:warning:", "jumpToNomalDetailPlayer:ctx == null");
            return;
        }
        Intent intent = new Intent(ctx, ReserveConfigSingleTaskActivity2.class);
        if (!(ctx instanceof Activity)) {
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        }
        ctx.startActivity(intent);
    }


    /**
     * 跳转到首页
     *
     * @param ctx
     * @param type
     */
    public void jumpToHomePageActivity(Context ctx, final int type) {
        if (Looper.myLooper() == null || Looper.myLooper() != Looper.getMainLooper()) {
            throw new IllegalArgumentException("you must call jumpToHomePageActivity with ui thread");
        }

        Intent it = new Intent(GlobalContext.get(), SplashActivity.class);
        if (type > -1) {
            it.putExtra(KEY_TYPE, type);
        }
        if (!(ctx instanceof Activity)) {
            it.addFlags(FLAG_ACTIVITY_NEW_TASK);
        }
        try {
            ctx.startActivity(it);
        } catch (Exception e) {
            LogUtil.loge("open mainactivity error!");
        }
    }

    /**
     * 是否在播放详情界面
     *
     * @return
     */
    private boolean isInPlayerUI() {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return false;
        }
        if (currentActivity instanceof PlayRadioRecommendActivity || currentActivity instanceof PlayDetailsActivity) {
            return true;
        }
        return false;
    }
}
