package com.txznet.webchat.ui.common;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.txznet.loader.GlideApp;
import com.txznet.webchat.R;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.log.L;

/**
 * 加载微信image资源使用的ImageLoader
 * <p>
 * Created by J on 2018/1/4.
 */

public class WxImageLoader {
    private static final String LOG_TAG = "WxImageLoader";

    private static String sRequestCookie = "";
    private static LazyHeaders sRequestHeader = new LazyHeaders.Builder().addHeader("Cookie",
            sRequestCookie).build();

    public static void updateRequestCookie(String cookie) {
        if (checkRequestCookieChanged(cookie)) {
            sRequestCookie = cookie;
            sRequestHeader = new LazyHeaders.Builder().addHeader("Cookie", cookie).build();
        }
    }

    private static boolean checkRequestCookieChanged(String newCookie) {
        if (null == newCookie) {
            return false;
        }

        return !newCookie.equals(sRequestCookie);
    }

    public static void loadHead(Activity activity, WxContact contact, ImageView imageView) {
        String headUrl = null;
        if (checkContact(contact)) {
            headUrl = contact.mHeadImgUrl;
        }

        loadImage(activity, headUrl, imageView,
                activity.getResources().getDrawable(R.drawable.default_headimage),
                activity.getResources().getDrawable(R.drawable.default_headimage));
    }

    public static void loadHead(Context context, WxContact contact, ImageView imageView) {
        String headUrl = null;
        if (checkContact(contact)) {
            headUrl = contact.mHeadImgUrl;
        }

        loadImage(context, headUrl, imageView,
                context.getResources().getDrawable(R.drawable.default_headimage),
                context.getResources().getDrawable(R.drawable.default_headimage));
    }

    public static void loadHead(View view, WxContact contact, ImageView imageView) {
        String headUrl = null;
        if (checkContact(contact)) {
            headUrl = contact.mHeadImgUrl;
        }

        loadImage(view, headUrl, imageView,
                view.getResources().getDrawable(R.drawable.default_headimage),
                view.getResources().getDrawable(R.drawable.default_headimage));
    }

    public static boolean checkContact(WxContact contact) {
        if (null == contact) {
            L.e(LOG_TAG, "checkContact: contact is null!");
            return false;
        }

        return true;
    }

    public static void loadImage(Activity activity, String url, ImageView imageView, int err, int loading) {
        loadImage(activity, url, imageView,
                activity.getResources().getDrawable(err),
                activity.getResources().getDrawable(loading));
    }

    public static void loadImage(Activity activity, String url, ImageView imageView, Drawable err, Drawable loading) {
        if (!checkParam(url, imageView, loading, err)) {
            return;
        }

        GlideApp.with(activity)
                .load(new GlideUrl(url, sRequestHeader))
                .placeholder(loading)
                .error(err)
                .into(imageView);
    }

    public static void loadImage(Context context, String url, ImageView imageView, int err, int loading) {
        loadImage(context, url, imageView,
                context.getResources().getDrawable(err),
                context.getResources().getDrawable(loading));
    }

    public static void loadImage(Context context, String url, ImageView imageView, Drawable err, Drawable loading) {
        if (!checkParam(url, imageView, loading, err)) {
            return;
        }

        GlideApp.with(context)
                .load(new GlideUrl(url, sRequestHeader))
                .placeholder(loading)
                .error(err)
                .into(imageView);
    }

    public static void loadImage(View view, String url, ImageView imageView, int err, int loading) {
        loadImage(view, url, imageView,
                view.getResources().getDrawable(err),
                view.getResources().getDrawable(loading));
    }

    public static void loadImage(View view, String url, ImageView imageView, Drawable err, Drawable loading) {
        if (!checkParam(url, imageView, loading, err)) {
            return;
        }

        GlideApp.with(view)
                .load(new GlideUrl(url, sRequestHeader))
                .placeholder(loading)
                .error(err)
                .into(imageView);
    }

    private static boolean checkParam(String url, ImageView imageView, Drawable loading, Drawable err) {
        if (null == imageView) {
            L.e(LOG_TAG, "check param: ImageView is null");
            return false;
        }

        if (TextUtils.isEmpty(url)) {
            L.e(LOG_TAG, "check param: url is empty");

            // 尝试直接设置错误占位符
            if (null != err) {
                L.e(LOG_TAG, "check param: setting error holder to ImageView");
                imageView.setImageDrawable(err);
            }

            return false;
        }

        return true;
    }
}