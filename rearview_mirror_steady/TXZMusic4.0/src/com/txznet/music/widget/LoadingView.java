package com.txznet.music.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.utils.AnimationUtil;
import com.txznet.music.utils.SDKUtil;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.ViewUtil;


/**
 * @author cp
 * @des Loading view
 */
public class LoadingView extends RelativeLayout {

    private int emptyView, errorView, loadingView, contentView;
    private OnClickListener onRetryClickListener;
    private OnClickListener onEmptyClickListener;
    LoadingData loadingData;

    private int INT_Empty_View = 0;
    private int INT_Error_View = 1;
    private int INT_Loading_View = 2;
    private int INT_Content_View = 3;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.LoadingView, 0, 0);
        try {
            emptyView = a.getResourceId(R.styleable.LoadingView_emptyView, R.layout.aloading_empty_view);
            errorView = a.getResourceId(R.styleable.LoadingView_errorView, R.layout.error_layout);
            loadingView = a.getResourceId(R.styleable.LoadingView_loadingView, R.layout.layout_loading);
            contentView = a.getResourceId(R.styleable.LoadingView_contentsView, 0);
            LayoutInflater inflater = LayoutInflater.from(getContext());
            inflater.inflate(emptyView, this, true);  // 0
            inflater.inflate(errorView, this, true); // 1
            inflater.inflate(loadingView, this, true); //3
            if (0 != contentView) {
                inflater.inflate(contentView, this, true); //2
            }
            loadingData = new LoadingData();
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        for (int i = 0; i < getChildCount() - 1; i++) {
            ViewUtil.setViewVisibility(getChildAt(i), View.GONE);
        }
//        emptyView.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (null != onEmptyClickListener) {
//                    onEmptyClickListener.onClick(view);
//                }
//            }
//        });
//        findViewById(R.id.layout_load_fail).setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (null != onRetryClickListener) {
//                    onRetryClickListener.onClick(view);
//                }
//            }
//        });
    }

    public void setOnRetryClickListener(OnClickListener onRetryClickListener) {
        this.onRetryClickListener = onRetryClickListener;
    }

    public void setOnEmptyClickListener(OnClickListener onEmptyClickListener) {
        this.onEmptyClickListener = onEmptyClickListener;
    }

    /**
     * 显示指定内容的空页面
     *
     * @param info
     * @param resId
     */
    public void showEmpty(String info, int resId) {
        showEmpty(info, resId, "");
    }

    /**
     * 显示指定内容的空页面
     *
     * @param info
     * @param resId
     */
    public void showEmpty(String info, int resId, String hintInfo) {
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = this.getChildAt(i);
            if (child == null) {
                continue;
            }
            if (i == INT_Empty_View) {
                setEmptyInfoCustom(info, resId, hintInfo, child);
                ViewUtil.setViewVisibility(child, VISIBLE);
            } else {
                setOtherViewGone(i);
            }
        }
    }

    private void setOtherViewGone(int i) {
        View child = this.getChildAt(i);
        if (i == INT_Loading_View) {
            setLoading(child, 0, 0, "", false);
        }
        ViewUtil.setViewVisibility(child, GONE);

    }

    /**
     * 显示错误页
     */
    public void showError(String message, int resId) {
        String info;
        showError(message, resId, false);
    }

    /**
     * 显示错误页，指定内容
     *
     * @param info
     * @param resId
     */
    public void showError(String info, int resId, String hintInfo) {
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = this.getChildAt(i);
            if (child == null) {
                continue;
            }
            if (i == INT_Error_View) {
                setErrorInfoCustom(info, resId, hintInfo, child);
                ViewUtil.setViewVisibility(child, VISIBLE);
            } else {
                setOtherViewGone(i);
            }
        }
    }

    public boolean isShowLoading() {
        return this.getChildAt(INT_Loading_View).getVisibility() == VISIBLE;
    }

    public boolean isShowEmpty() {
        return this.getChildAt(INT_Empty_View).getVisibility() == VISIBLE;
    }

    /**
     * 显示错误页，指定内容、是否显示重试按钮
     *
     * @param info
     * @param resId
     * @param showRetryButton
     */
    public void showError(String info, int resId, boolean showRetryButton) {
        showError(info, resId, null);
        if (!showRetryButton) {
            View retry = findViewById(R.id.btn_refresh);
            ViewUtil.setViewVisibility(retry, GONE);
        }
    }

    private void setErrorInfoCustom(String info, int resId, String hintInfo, View child) {
        if (!TextUtils.isEmpty(info)) {
            TextView tv = (TextView) child.findViewById(R.id.tv_showtips);
            if (tv != null) {
                tv.setText(info);
            }
            ViewUtil.setViewVisibility(tv, VISIBLE);
        }
        if (resId > 0) {
            ImageView iv = (ImageView) child.findViewById(R.id.iv_no_result);
            if (iv != null) {
                iv.setImageResource(resId);
            }
            ViewUtil.setViewVisibility(iv, VISIBLE);
        }
        if (!TextUtils.isEmpty(hintInfo)) {
            Button btn = (Button) child.findViewById(R.id.btn_refresh);
            if (btn != null) {
                btn.setText(hintInfo);
                if (errorHintListener != null) {
                    btn.setOnClickListener(errorHintListener);
                }
            }
            ViewUtil.setViewVisibility(btn, VISIBLE);
        }
    }

    public View.OnClickListener errorHintListener = null;

    public void setErrorHintListener(View.OnClickListener listener) {
        errorHintListener = listener;
    }

    public View.OnClickListener emptyHintListener = null;

    public void setEmptyHintListener(View.OnClickListener listener) {
        emptyHintListener = listener;
    }

    private void setEmptyInfoCustom(String info, int resId, String hintInfo, View child) {
        TextView tv = (TextView) child.findViewById(R.id.tv_empty_info);
        if (!TextUtils.isEmpty(info)) {
            if (tv != null) {
                tv.setText(info);
            }
            ViewUtil.setViewVisibility(tv, VISIBLE);
        } else {
            ViewUtil.setViewVisibility(tv, GONE);
        }
        ImageView iv = (ImageView) child.findViewById(R.id.ic_empty_img);
        if (resId > 0) {
            if (iv != null) {
                iv.setImageResource(resId);
            }
            ViewUtil.setViewVisibility(iv, VISIBLE);
        } else {
            ViewUtil.setViewVisibility(iv, GONE);
        }
        Button btn = (Button) child.findViewById(R.id.btn_empty_info);
        if (!TextUtils.isEmpty(hintInfo)) {
            if (btn != null) {
                btn.setText(hintInfo);
                if (null != emptyHintListener) {
                    btn.setOnClickListener(emptyHintListener);
                }
            }
            ViewUtil.setViewVisibility(btn, VISIBLE);
        } else {
            ViewUtil.setViewVisibility(btn, GONE);
        }
    }

    public void setContentView(View view) {
        view.setId(R.id.aContentView);
        LayoutParams lp = new LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(view, lp);
    }

    public void setContentView(View view, LayoutParams layoutParams) {
        view.setId(R.id.aContentView);
        addView(view, layoutParams);
    }

    /**
     * 显示Loading
     */
    public void showLoading(int rotateIcon, int centerIcon, String loadingMessage) {
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = this.getChildAt(i);
            if (child == null) {
                continue;
            }
            if (i == INT_Loading_View) {
                setLoading(child, rotateIcon, centerIcon, loadingMessage, true);
                ViewUtil.setViewVisibility(child, VISIBLE);
            } else {
                setOtherViewGone(i);
            }
        }
    }

    /**
     * 显示Loading
     */
    public void showWithLoading(int rotateIcon, int centerIcon, String loadingMessage) {
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = this.getChildAt(i);
            if (child == null) {
                continue;
            }
            if (i == INT_Loading_View) {
                setLoading(child, rotateIcon, centerIcon, loadingMessage, true);
                ViewUtil.setViewVisibility(child, VISIBLE);
            } else if (i == INT_Content_View) {

            } else {
                setOtherViewGone(i);
            }
        }
    }

    /**
     * 显示Loading
     */
    public void hideWithLoading() {
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = this.getChildAt(i);
            if (child == null) {
                continue;
            }
            if (i == INT_Loading_View) {
                setLoading(child, 0, 0, "", false);
//                setLoading(child, rotateIcon, centerIcon, loadingMessage, true);
                ViewUtil.setViewVisibility(child, GONE);
            }/* else if (i == INT_Content_View) {

            } else {
                setOtherViewGone(i);
            }*/
        }
    }

    /**
     * 只设置界面上的元素,对可见性不做修改
     *
     * @param text
     */
    public void setLoadingText(String text) {
        TextView tvLoading = (TextView) getChildAt(2).findViewById(R.id.tv_loading);
        tvLoading.setText(text);
    }


    private void setLoading(View child, int rotateIcon, int centerIcon, String loadingMessage, boolean flag) {
        LinearLayout loadingView = (LinearLayout) child.findViewById(R.id.ll_loading);
        ImageView ivRotate = (ImageView) child.findViewById(R.id.iv_loading);
        TextView tvLoading = (TextView) child.findViewById(R.id.tv_loading);
        ImageView ivCenterIcon = (ImageView) child.findViewById(R.id.iv_loading_center_icon);
        if (loadingData.hasRotateIdChange(rotateIcon)) {
            loadingData.setRotateId(rotateIcon);
            ivRotate.setImageDrawable(getResources().getDrawable(rotateIcon));
        }
        if (loadingData.hasCenterIdChange(centerIcon)) {
            loadingData.setCenterId(centerIcon);
            ivCenterIcon.setImageDrawable(getResources().getDrawable(centerIcon));
        }
        if (loadingData.hasMessageChange(loadingMessage)) {
            loadingData.setText(loadingMessage);
            tvLoading.setText(loadingMessage);
        }
        if (ivRotate != null) {
            if (flag) {
                Animation animation = AnimationUtil.createSmoothForeverAnimation(getContext());
                ivRotate.startAnimation(animation);
                ViewUtil.setViewVisibility(loadingView, VISIBLE);
            } else {
                ivRotate.clearAnimation();
                ViewUtil.setViewVisibility(loadingView, GONE);
            }
        }
    }

    /**
     * 显示 contentView
     */
    public void showContent() {
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = this.getChildAt(i);
            if (child == null) {
                continue;
            }
            if (i == INT_Content_View || child.getId() == contentView) {
                ViewUtil.setViewVisibility(child, VISIBLE);
            } else {
                setOtherViewGone(i);
            }
        }
    }

    class LoadingData {

        private int rotateId;
        private int centerId;
        private String text;

        public boolean hasRotateIdChange(int rotateId) {
            return rotateId > 0 && this.rotateId != rotateId;
        }

        public boolean hasCenterIdChange(int centerId) {
            return centerId > 0 && this.centerId != centerId;
        }

        public boolean hasMessageChange(String text) {
            return StringUtils.isNotEmpty(text) && !TextUtils.equals(this.text, text);
        }

        public void setRotateId(int rotateId) {
            this.rotateId = rotateId;
        }

        public void setCenterId(int centerId) {
            this.centerId = centerId;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
