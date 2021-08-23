package com.txznet.comm.ui.theme.test.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;

import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.test.widget.ProgressBarScrollView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;

import java.util.List;

/**
 * 2020-08-13 19:45
 *
 * @author xiaolin
 */
public class ListLayoutUtils {

    public static class ListContainer {

        public ViewGroup rootView;
        public ViewGroup container;

    }

    // 是否加载一下页，用在动画上
    private static boolean isNextPage = true;

    public static ListContainer createListLayout(Context context, int maxPage, int curPage, final IViewStateListener viewStateListener) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.list_container, (ViewGroup) null);
        ViewGroup container = view.findViewById(R.id.container);
        SmartRefreshLayout refreshLayout = view.findViewById(R.id.refreshLayout);
        ProgressBarScrollView progressBarScrollView = view.findViewById(R.id.progressBarScrollView);

        refreshLayout.setEnableRefresh(maxPage > 1 && curPage != 0);                // 下拉上一页
        refreshLayout.setEnableLoadMore(maxPage > 1 && curPage != maxPage - 1);     // 上拉下一页
        refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                isNextPage = false;
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK, TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_LIST_PREPAGE, 0, 0);
            }
        });
        refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                isNextPage = true;
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK, TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_LIST_NEXTPAGE, 0, 0);
            }
        });

        progressBarScrollView.setMaxPage(maxPage);
        progressBarScrollView.setCurPage(curPage);

//        if(isNextPage){
//            view.setLayoutAnimation(getAnimationControllerFadeTop());
//        } else {
//            view.setLayoutAnimation(getAnimationControllerFadeBottom());
//        }
        view.setLayoutAnimation(getAnimationControllerFadeTop());

        view.setLayoutAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (viewStateListener != null) {
                    viewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_START);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (viewStateListener != null) {
                    viewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_REPEAT);
                }
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (viewStateListener != null) {
                    viewStateListener.onAnimateStateChanged(animation, IViewStateListener.STATE_ANIM_ON_END);
                }
            }
        });

        ListContainer c = new ListContainer();
        c.rootView = view;
        c.container = container;

        return c;
    }

    /**
     * 列表点击效果，为按下去的列表项设置不同的背景
     *
     * @param itemViews
     * @param index
     */
    private static void showSelectItem(List<View> itemViews, int index) {
        if (itemViews != null) {
            for (int i = 0; i < itemViews.size(); i++) {
                if (i == index) {
                    itemViews.get(i).setBackgroundResource(R.drawable.item_setlected);
                } else {
                    itemViews.get(i).setBackground(null);
                }
            }
        }
    }

    /**
     * 点击事件和触摸事件
     *
     * @param view
     * @param itemViews
     * @param index
     */
    public static void setItemViewOnClickOnTouch(View view, final List<View> itemViews, final int index) {
        View.OnClickListener onClickListener = new View.OnClickListener() {

            public void onClick(View v) {
                showSelectItem(itemViews, index);
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK, TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_LIST_ITEM, 0, index);
            }
        };

        View.OnTouchListener onTouchListener = new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_TOUCH, TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_LIST_ITEM, 0, 0);
                return false;
            }
        };

        view.setOnClickListener(onClickListener);
        view.setOnTouchListener(onTouchListener);
    }

    public static LayoutAnimationController getAnimationControllerFadeTop() {
        LayoutAnimationController controller = new LayoutAnimationController(getAnimationSetFadeTop(), 0.08f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }

    public static LayoutAnimationController getAnimationControllerFadeBottom() {
        LayoutAnimationController controller = new LayoutAnimationController(getAnimationSetFadeBottom(), 0.08f);
        controller.setOrder(LayoutAnimationController.ORDER_NORMAL);
        return controller;
    }

    public static AnimationSet getAnimationSetFadeTop() {
        int duration=300;
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.2f, 1.0f);
        animation.setDuration(duration);
        set.addAnimation(animation);

        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.9f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(duration);
        set.addAnimation(animation);
        return set;
    }

    public static AnimationSet getAnimationSetFadeBottom() {
        int duration=300;
        AnimationSet set = new AnimationSet(true);

        Animation animation = new AlphaAnimation(0.2f, 1.0f);
        animation.setDuration(duration);
        set.addAnimation(animation);

        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                -0.9f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(duration);
        set.addAnimation(animation);
        return set;
    }

}
