package com.txznet.music.push;

import android.content.Context;
import android.graphics.PixelFormat;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.music.R;
import com.txznet.music.baseModule.INormalCallback;
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.push.PushIntercepter;
import com.txznet.music.utils.ScreenUtils;
import com.txznet.music.widget.ResourceButton;

/**
 * Created by ASUS User on 2016/12/15.
 */

public class PushNotification extends FrameLayout {

    private static final String TAG = "music:PushNotification:";

    private WindowManager mWinManager;
    private LinearLayout rlRoot;
    private ImageView ivIcon;
    private TextView tvTitle;
    private ResourceButton btnClose;
    private ResourceButton btnListen;
    private boolean isShow;
    private TextView tvSubTitle;

    private Build mBuild;


    private PushNotification(Build build) {
        super(GlobalContext.get());
        this.mBuild = build;
        initView(GlobalContext.get());
        mWinManager = (WindowManager) GlobalContext.get().getSystemService(Context.WINDOW_SERVICE);
    }


    private void initView(Context context) {
        View view;
        if (ScreenUtils.isPhonePortrait()) {
            view = View.inflate(context, R.layout.view_push_notification_phone_portrait, this);
        } else {
            view = View.inflate(context, R.layout.view_push_notification, this);
        }
        rlRoot = (LinearLayout) view.findViewById(R.id.rl_root);
        ivIcon = (ImageView) view.findViewById(R.id.iv_icon);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        btnListen = (ResourceButton) view.findViewById(R.id.btn_listen);
        btnClose = (ResourceButton) view.findViewById(R.id.btn_close);
        tvSubTitle = (TextView) view.findViewById(R.id.tv_sub_title);

        setIcon(mBuild.getIconUrl());
        setTitle(mBuild.getTitle());
        setSubTitle(mBuild.getSubTitle());

        setConfirmBtnText(mBuild.getConfirmText());
        setCloseBtnText(mBuild.getCancelText());
    }


    private void setIcon(String url) {
        if (TextUtils.isEmpty(url)) {
            ivIcon.setVisibility(View.GONE);
        } else {
            ImageFactory.getInstance().setStyle(IImageLoader.NORMAL);
            ivIcon.setVisibility(View.VISIBLE);
            ImageFactory.getInstance().display(GlobalContext.get(), url, ivIcon, R.drawable.fm_item_default);
        }
    }

    private void setTitle(String title) {
        tvTitle.setText(title);
    }

    private void setSubTitle(String subTitle) {
        if (TextUtils.isEmpty(subTitle)) {
            tvSubTitle.setVisibility(View.GONE);
        } else {
            tvSubTitle.setText(subTitle);
        }
    }


    private void setCloseBtnText(String text) {
        btnClose.setText(text);
    }

    private void setConfirmBtnText(String text) {
        btnListen.setText(text);
    }

    public void setOnClickListener(OnClickListener listener) {
        btnListen.setOnClickListener(listener);
    }

    public void setCloseListener(OnClickListener listener) {
        btnClose.setOnClickListener(listener);
    }

    public void show() {
        LogUtil.d(TAG, "show:" + isShow);
        if (isShow)
            return;
        isShow = true;
        PushIntercepter.getInstance().showView(new INormalCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (!aBoolean) {
                    int width = 0;
                    if (ScreenUtils.isPhonePortrait()) {
                        width = ((int) GlobalContext.get().getResources().getDimension(R.dimen.y400));
                    } else {
                        width = ((int) GlobalContext.get().getResources().getDimension(R.dimen.x800));
                    }
                    WindowManager.LayoutParams layoutParam = new WindowManager.LayoutParams();
                    layoutParam.type = WindowManager.LayoutParams.TYPE_PHONE;
                    layoutParam.width = width;
                    layoutParam.height = ((int) GlobalContext.get().getResources().getDimension(R.dimen.m120));
                    layoutParam.flags = 40;
                    layoutParam.format = PixelFormat.RGBA_8888;
                    layoutParam.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
                    mWinManager.addView(PushNotification.this, layoutParam);
                }
            }

            @Override
            public void onError() {

            }
        });

    }

    public void dismiss() {
        LogUtil.d(TAG, "dismiss:" + isShow);
        if (!isShow)
            return;
        isShow = false;

        if (PushIntercepter.getInstance().dismissView()) {
            return;
        }
        try {
            mWinManager.removeView(this);
        } catch (Exception e) {
            //
        }
    }


    public void setCountDown(int count) {
        if (count > 0) {
            btnClose.setText(Html.fromHtml(String.format("%s(<font color=red>%s</font>s)", mBuild.getCancelText(), count)));
        } else {
            btnClose.setText(GlobalContext.get().getResources().getString(R.string.text_close));
        }
    }


    public static class Build {
        private String title;
        private String subTitle;
        private String iconUrl;
        private String confirmText;
        private String cancelText;

        public String getTitle() {
            return title;
        }

        public Build setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public Build setSubTitle(String subTitle) {
            this.subTitle = subTitle;
            return this;
        }

        public String getIconUrl() {
            return iconUrl;
        }

        public Build setIconUrl(String iconUrl) {
            this.iconUrl = iconUrl;
            return this;
        }

        public String getConfirmText() {
            return confirmText;
        }

        public Build setConfirmText(String confirmText) {
            this.confirmText = confirmText;
            return this;
        }

        public String getCancelText() {
            return cancelText;
        }

        public Build setCancelText(String cancelText) {
            this.cancelText = cancelText;
            return this;
        }

        public PushNotification create() {
            return new PushNotification(this);
        }
    }
}
