package com.txznet.music.widget;

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
import com.txznet.music.image.IImageLoader;
import com.txznet.music.image.ImageFactory;

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
        View view = View.inflate(context, R.layout.view_push_notification, this);

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


        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        if(ScreenUtil.getScreenWidth() > 800){
            width = 800;
        }

        WindowManager.LayoutParams layoutParam = new WindowManager.LayoutParams();
        layoutParam.type = WindowManager.LayoutParams.TYPE_PHONE;
        layoutParam.width = width;
        layoutParam.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        layoutParam.flags = 40;
        layoutParam.format = PixelFormat.RGBA_8888;
        layoutParam.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        mWinManager.addView(this, layoutParam);
        isShow = true;
    }

    public void dismiss() {
        LogUtil.d(TAG, "dismiss:" + isShow);
        if (!isShow)
            return;
        isShow = false;
        mWinManager.removeView(this);
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
