package com.txznet.music.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.baseModule.Constant;

/**
 * Created by 58295 on 2018/3/28.
 */

public class LoadingProgress {


    private static Context mContext;
    //##创建一个单例类##
    private volatile static LoadingProgress singleton;
    Animation hyperspaceJumpAnimation;
    private final ImageView mLoadingIv;
    private final TextView mLoadingTv;
    private final Dialog mLoadingDialog;

    private LoadingProgress() {
        View view = View.inflate(mContext, R.layout.loading_progress, null);// 得到加载view
        mLoadingIv = (ImageView) view.findViewById(R.id.loading_img);
        // 提示文字
        mLoadingTv = (TextView) view.findViewById(R.id.tv_loading);
        hyperspaceJumpAnimation = AnimationUtil.createSmoothForeverAnimation(mContext);
        // 加载动画
        // 创建自定义样式dialog
        mLoadingDialog = new Dialog(mContext, R.style.loading_dialog);
        if (mLoadingDialog.getWindow() != null) {
            mLoadingDialog.getWindow().setFormat(PixelFormat.RGBA_8888);
        }
        mLoadingDialog.setCancelable(false);// 不可以用“返回键”取消
        mLoadingDialog.setContentView(view);// 设置布局
    }

    public static LoadingProgress getInstance(Context ctx) {
        if (singleton == null) {
            synchronized (LoadingProgress.class) {
                if (singleton == null) {
                    initContext(ctx);
                    singleton = new LoadingProgress();
                }
            }
        }
        return singleton;
    }

    private static void initContext(Context ctx) {
        mContext = ctx;
    }


    public void show(String msg) {
        // 使用ImageView显示动画
        mLoadingIv.startAnimation(hyperspaceJumpAnimation);
        mLoadingTv.setText(msg);// 设置加载信息

        mLoadingDialog.show();
    }

    public void dismiss() {
        mLoadingIv.clearAnimation();
        mLoadingDialog.dismiss();
    }
}
