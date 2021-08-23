package com.txznet.music.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.txznet.music.R;


/**
 * @author cp
 * @des Loading view
 */
public class LoadingView extends RelativeLayout {

    private int i_emptyView, i_errorView, i_loadingView, i_contentView;

    private View empty_View = null;
    private View error_View = null;
    private View loading_View = null;
    private View content_View = null;

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
            i_emptyView = a.getResourceId(R.styleable.LoadingView_emptyView, 0);
            i_errorView = a.getResourceId(R.styleable.LoadingView_errorView, 0);
            i_loadingView = a.getResourceId(R.styleable.LoadingView_loadingView, 0);
            i_contentView = a.getResourceId(R.styleable.LoadingView_contentsView, 0);
            LayoutInflater inflater = LayoutInflater.from(getContext());
            if (i_emptyView != 0) {
                empty_View = inflater.inflate(i_emptyView, this, false);  // 0}
                this.addView(empty_View);
            }
            if (i_errorView != 0) {
                error_View = inflater.inflate(i_errorView, this, false); // 1
                this.addView(error_View);
            }
            if (i_loadingView != 0) {
                loading_View = inflater.inflate(i_loadingView, this, false); //3
                this.addView(loading_View);
            }
            if (0 != i_contentView) {
                content_View = inflater.inflate(i_contentView, this, false); //2
                this.addView(content_View);
            }
        } finally {
            a.recycle();
        }
    }


    private void hideAll() {
        if (empty_View != null) {
            empty_View.setVisibility(View.GONE);
        }
        if (error_View != null) {
            error_View.setVisibility(View.GONE);
        }
        if (loading_View != null) {
            loading_View.setVisibility(View.GONE);
        }
        if (content_View != null) {
            content_View.setVisibility(View.GONE);
        }
    }

    /**
     * 显示指定内容的空页面
     */
    public void showEmpty() {
        if (empty_View != null) {
            hideAll();
            empty_View.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示错误页，指定内容
     */
    public void showError() {
        if (error_View != null) {
            hideAll();
            error_View.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示指定内容的空页面
     */
    public void showLoading() {
        if (loading_View != null) {
            hideAll();
            loading_View.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示指定内容的空页面
     */
    public void showContent() {
        if (content_View != null) {
            hideAll();
            content_View.setVisibility(View.VISIBLE);
        }
    }

}
