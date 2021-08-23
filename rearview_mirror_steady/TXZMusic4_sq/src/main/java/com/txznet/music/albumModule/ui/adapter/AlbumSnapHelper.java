package com.txznet.music.albumModule.ui.adapter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.music.R;
import com.txznet.music.widget.ShadeImageView;

/**
 * View移动后位置居中
 *
 * Created by Terry on 2017/5/16.
 */

public class AlbumSnapHelper{

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private ItemAlbumFragmentAdapter mAdapter;

    private int mCenterItem = -1;

    private float mMinWidth;
    private float mMinHeight;
    private float mMaxWidth;
    private float mMaxHeight;

    private float mMaxContentHeight; // 最大屏幕高度

    private int mRecyclerCenter;
    private int mRecyclerWidth;

    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        this.mRecyclerView = recyclerView;
        this.mLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//        mAdapter = (ItemAlbumAdapter) recyclerView.getAdapter();
//
//        mMinWidth = GlobalContext.get().getResources().getDimension(R.dimen.x96);
//        mMinHeight = GlobalContext.get().getResources().getDimension(R.dimen.x96);
//
//        mMaxWidth = GlobalContext.get().getResources().getDimension(R.dimen.x140);
//        mMaxHeight = GlobalContext.get().getResources().getDimension(R.dimen.x140);
//
//        mMaxContentHeight = GlobalContext.get().getResources().getDimension(R.dimen.y240);
//        if (mMaxHeight > mMaxContentHeight) {
//            mMinWidth = GlobalContext.get().getResources().getDimension(R.dimen.y120);
//            mMinHeight = GlobalContext.get().getResources().getDimension(R.dimen.y120);
//
//            mMaxWidth = GlobalContext.get().getResources().getDimension(R.dimen.y180);
//            mMaxHeight = GlobalContext.get().getResources().getDimension(R.dimen.y180);
//        }
//
//        LogUtil.logd("screen width:" + GlobalContext.get().getResources().getDimension(R.dimen.x800) + "\n screenHeight:" + GlobalContext.get().getResources().getDimension(R.dimen.y480));
//        LogUtil.logd("screen min:" + mMinWidth + "\n screenHeight:" + mMinHeight);
//        LogUtil.logd("screen max:" + mMaxWidth + "\n screenHeight:" + mMaxHeight);
//
//        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//
//            private boolean mScrolled = false;
//            private boolean mDragged = false;
//
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                if(newState == RecyclerView.SCROLL_STATE_DRAGGING){
//                    mDragged = true;
//                }
//                if (RecyclerView.SCROLL_STATE_IDLE == newState && mScrolled && mDragged) {
//                    mDragged = false;
//                    mScrolled = false;
//                    int center = findCenterPosition();
//                    snapToCenter(center);
//                }
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//
//            @Override
//            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                if (dx != 0 | dy != 0) {
//                    mScrolled = true;
//                    scaleViewWhenScroll();
//                }
//
//            }
//        });
//
//        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver(){
//            @Override
//            public void onChanged() {
//                super.onChanged();
//                mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        scaleViewWhenScroll();
//                        mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                    }
//                });
//            }
//        });
    }


    public void snapToCenter(final int center) {
//        int left = center - 2;
//        int right = center + 2;
//        int lastFullVisible = mLayoutManager.findLastCompletelyVisibleItemPosition();
//        int itemCount = mLayoutManager.getItemCount();
//        // 当前已显示到最后且想要移动
//        if (lastFullVisible == itemCount - 1) {
//            if (center >= itemCount - 3) {
//                return;
//            }
//        }
//        View leftView = mLayoutManager.findViewByPosition(left);
//        mAdapter.setCenterItem(center);
//        int[] location = new int[2];
//        if (leftView != null) {
//            leftView.getLocationInWindow(location);
//            int nowLeft = location[0];
//            mRecyclerView.getLocationInWindow(location);
//            int targetLeft = location[0];
//            AppLogic.runOnUiGround(new Runnable1<Integer>(nowLeft - targetLeft) {
//                @Override
//                public void run() {
//                    if (mP1 >= 5 || mP1 <= -5) {
//                        mRecyclerView.smoothScrollBy(mP1, 0);
//                        mCenterItem = center;
//                    }
//                }
//            }, 0);
//            return;
//        }
//        View rightView = mLayoutManager.findViewByPosition(right);
//        if (rightView != null) {
//            rightView.getLocationInWindow(location);
//            int nowRight = location[0] + rightView.getWidth();
//            mRecyclerView.getLocationInWindow(location);
//            int targetRight = location[0] + mRecyclerView.getWidth();
//            AppLogic.runOnUiGround(new Runnable1<Integer>(nowRight - targetRight) {
//                @Override
//                public void run() {
//                    if (mP1 >= 5 || mP1 <= -5) {
//                        mRecyclerView.smoothScrollBy(mP1, 0);
//                        mCenterItem = center;
//                    }
//                }
//            }, 0);
//            return;
//        }
//        LogUtil.loge("leftView rightView both invisible!");
    }



    public int findCenterPosition() {
        int firstVisible = mLayoutManager.findFirstVisibleItemPosition();
        int lastVisible = mLayoutManager.findLastVisibleItemPosition();
        return (firstVisible+lastVisible)/2;
    }


    // Orientation helpers are lazily created per LayoutManager.
    @Nullable
    private OrientationHelper mVerticalHelper;
    @Nullable
    private OrientationHelper mHorizontalHelper;

    @NonNull
    private OrientationHelper getVerticalHelper(@NonNull RecyclerView.LayoutManager layoutManager) {
        if (mVerticalHelper == null) {
            mVerticalHelper = OrientationHelper.createVerticalHelper(layoutManager);
        }
        return mVerticalHelper;
    }

    @NonNull
    private OrientationHelper getHorizontalHelper(
            @NonNull RecyclerView.LayoutManager layoutManager) {
        if (mHorizontalHelper == null) {
            mHorizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager);
        }
        return mHorizontalHelper;
    }

    @SuppressLint("NewApi")
    private void scaleViewWhenScroll(){
        int first = mLayoutManager.findFirstVisibleItemPosition();
        int last = mLayoutManager.findLastVisibleItemPosition();
        View view;
        int[] location = new int[2];
        mRecyclerView.getLocationInWindow(location);
        mRecyclerCenter = location[0] + mRecyclerView.getWidth() / 2;
        mRecyclerWidth = mRecyclerView.getWidth();
        for (int i = first; i <= last; i++) {
            view = mLayoutManager.findViewByPosition(i);
            if (view != null) {
                view.getLocationInWindow(location);
                ShadeImageView imageView = (ShadeImageView) view.findViewById(R.id.type_iv);
                TextView textView = (TextView) view.findViewById(R.id.intro_tv);
                LinearLayout llAlbum = (LinearLayout) view.findViewById(R.id.ll_album);
                ImageView focusView = (ImageView) view.findViewById(R.id.iv_focus);

                int center = location[0] + view.getWidth() / 2;
                float factor = ((float)Math.abs(mRecyclerCenter - center) * 2) / mRecyclerWidth;
                float width = mMaxWidth - (mMaxWidth - mMinWidth) * factor;
                float height = mMaxHeight - (mMaxHeight - mMinHeight) * factor;
                LinearLayout.LayoutParams imageParams = (LinearLayout.LayoutParams) imageView.getLayoutParams();
                LinearLayout.LayoutParams textParams = (LinearLayout.LayoutParams) textView.getLayoutParams();
                RelativeLayout.LayoutParams llParmas = (RelativeLayout.LayoutParams) llAlbum.getLayoutParams();
                RelativeLayout.LayoutParams focusParams = (RelativeLayout.LayoutParams) focusView.getLayoutParams();

                int leftPadding = llAlbum.getPaddingLeft();
                int topPadding = llAlbum.getPaddingTop();

                if (i == first) {
                    imageParams.gravity = Gravity.RIGHT;
                    textParams.gravity = Gravity.RIGHT;
                    llParmas.removeRule(RelativeLayout.CENTER_IN_PARENT);
                    llParmas.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    llParmas.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    focusParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
                    focusParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    focusParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                } else if (i == last) {
                    imageParams.gravity = Gravity.LEFT;
                    textParams.gravity = Gravity.LEFT;
                    llParmas.removeRule(RelativeLayout.CENTER_IN_PARENT);
                    llParmas.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    llParmas.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    focusParams.removeRule(RelativeLayout.CENTER_IN_PARENT);
                    focusParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    focusParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                } else {
                    imageParams.gravity = Gravity.CENTER;
                    textParams.gravity = Gravity.CENTER;
                    llParmas.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    llParmas.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    llParmas.addRule(RelativeLayout.CENTER_IN_PARENT);
                    focusParams.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    focusParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    focusParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                }
                llParmas.addRule(RelativeLayout.CENTER_VERTICAL);
                if (imageParams.width != width || imageParams.height != height) {
                    imageParams.width = (int) width - leftPadding*2;
                    imageParams.height = (int) width - topPadding;
                    imageView.setLayoutParams(imageParams);
                    textParams.width = (int) width - leftPadding*2;
                    int textHeight = (int) (GlobalContext.get().getResources().getDimension(R.dimen.y76) * height / mMaxHeight - topPadding);
                    textParams.height = textHeight;
                    textView.setLayoutParams(textParams);
                    TextViewUtil.setTextSize(textView,GlobalContext.get().getResources().getDimension(R.dimen.y20) * height / mMaxHeight);
                    llAlbum.setLayoutParams(llParmas);
                    if (focusView.getVisibility() == View.VISIBLE) {
                        focusParams.width = imageParams.width + leftPadding * 2;
                        focusParams.height = imageParams.height + textParams.height + topPadding * 2;
                        focusView.setLayoutParams(focusParams);
                    }
                }

            }
        }
    }


    private int findCenterView(RecyclerView.LayoutManager layoutManager,
                                OrientationHelper helper) {
        int childCount = layoutManager.getChildCount();
        if (childCount == 0) {
            return 0;
        }

        final int center;
        if (layoutManager.getClipToPadding()) {
            center = helper.getStartAfterPadding() + helper.getTotalSpace() / 2;
        } else {
            center = helper.getEnd() / 2;
        }
        int absClosest = Integer.MAX_VALUE;

        int position = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = layoutManager.getChildAt(i);
            int childCenter = helper.getDecoratedStart(child) +
                    (helper.getDecoratedMeasurement(child) / 2);
            int absDistance = Math.abs(childCenter - center);

            /** if child center is closer than previous closest, set it as closest  **/
            if (absDistance < absClosest) {
                absClosest = absDistance;
                position = i;
            }
        }
        return position;
    }

}
