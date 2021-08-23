package com.txznet.music.ui.subscribe;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;

import com.jude.easyrecyclerview.EasyRecyclerView;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.SubscribeActionCreator;
import com.txznet.music.data.entity.SubscribeAlbum;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.store.SubscribeStore;
import com.txznet.music.ui.base.BasePlayerFragment;
import com.txznet.music.widget.GridDividerItemDecoration;
import com.txznet.music.widget.RefreshLoadingView;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.extensions.aac.ViewModelProviders;

import java.util.Locale;

import butterknife.Bind;

/**
 * @author telen
 * @date 2018/12/5,16:53
 */
public class SubscribeFragment extends BasePlayerFragment<SubscribeAdapter> {


    private SubscribeActionCreator mSubscribeActionCreator;

    @Bind(R.id.rv_data)
    EasyRecyclerView mRecyclerView;
//            RecyclerView mRecyclerView;


    @Bind(R.id.btn_test)
    Button btnTest;

    @Override
    protected int getLayout() {
        return R.layout.subscribe_fragment;
    }


    @Override
    protected void initView(View view) {
        tvTitle.setText("我订阅的节目");

        btnTest.setOnClickListener(v -> {
            SubscribeAlbum subscribeAlbum = new SubscribeAlbum();
            subscribeAlbum.id = (long) (Math.random() * 10000);
            subscribeAlbum.sid = 3;
            if (subscribeAlbum.id % 3 == 0) {
                subscribeAlbum.logo = "http://g.hiphotos.baidu.com/image/pic/item/a6efce1b9d16fdfa31b61748b98f8c5495ee7bba.jpg";
            } else if (subscribeAlbum.id % 3 == 1) {
                subscribeAlbum.logo = "http://a.hiphotos.baidu.com/image/pic/item/d833c895d143ad4be9fbd4518f025aafa50f064d.jpg";
            } else {
                subscribeAlbum.logo = "http://g.hiphotos.baidu.com/image/pic/item/a686c9177f3e6709c7c4d26e36c79f3df9dc555e.jpg";
            }


            subscribeAlbum.name = "test" + subscribeAlbum.id;
            mSubscribeActionCreator.subscribe(Operation.MANUAL, subscribeAlbum, "subscribe");
        });
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);
        mSubscribeActionCreator = SubscribeActionCreator.getInstance();

        SubscribeStore subscribeStore = ViewModelProviders.of(this).get(SubscribeStore.class);
        subscribeStore.getSubscribeAlbums().observe(this, result -> {
            if (BuildConfig.DEBUG) {
                Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",initData:success:" + result.size());
            }


            getAdapter().clear();
            getAdapter().addAll(result);
            if (result.size() > 0) {
                tvSubTitle.setText(String.format(Locale.getDefault(), "已订阅%d个节目", result.size()));
            } else {
                tvSubTitle.setText("");
            }


        });


        subscribeStore.getErrorStatus().observe(this, status -> {
            if (status == null) {
                return;
            }
            if (BuildConfig.DEBUG) {
                Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",initData:error:" + status);
            }

//            if (mAdapter.getAllData().size() <= 0) {
//                mRecyclerView.showError();
//            }
        });
        mSubscribeActionCreator.getSubscribeData(Operation.AUTO, null);

    }

    @Override
    protected SubscribeAdapter setAdapter() {
        return new SubscribeAdapter(this);
    }

    @Override
    protected void initAdapter(SubscribeAdapter adapter) {
        super.initAdapter(adapter);
        mRecyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mRecyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int measuredWidth = mRecyclerView.getMeasuredWidth();
                int startSize = getResources().getDimensionPixelOffset(R.dimen.m160);
                int count = 1;
                int spanSize = 0;//剩余的宽度
                for (int i = 0; i < 10; i++) {
                    startSize += getResources().getDimensionPixelOffset(R.dimen.m24);
                    startSize += getResources().getDimensionPixelOffset(R.dimen.m160);
                    if (startSize > measuredWidth) {
                        break;
                    }
                    spanSize = measuredWidth - startSize;
                    count++;
                }
                if (BuildConfig.DEBUG) {
                    Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",onGlobalLayout:" + count + "," + startSize + "," + measuredWidth + "," + spanSize);
                }
                ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mRecyclerView.getLayoutParams();
                if (lp == null) {
                    lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                }
                lp.leftMargin = spanSize / 2 + getResources().getDimensionPixelOffset(R.dimen.m48);
                lp.rightMargin = spanSize / 2 + getResources().getDimensionPixelOffset(R.dimen.m48);
                mRecyclerView.setLayoutParams(lp);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), count);
                gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
                mRecyclerView.setLayoutManager(gridLayoutManager);
            }
        });


        GridDividerItemDecoration gridDividerItemDecoration = new GridDividerItemDecoration(getResources().getDimensionPixelOffset(R.dimen.m24), getResources().getColor(R.color.transparent));
        mRecyclerView.addItemDecoration(gridDividerItemDecoration);
        mRecyclerView.getRecyclerView().setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        mRecyclerView.setProgressView(new RefreshLoadingView(getContext()));

        mRecyclerView.setAdapterWithProgress(adapter);
//        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReportEvent.reportUserSubscribeEnter();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ReportEvent.reportUserSubscribeExit();
    }

    @Override
    public void onDestroyView() {
        if (mRecyclerView != null && mRecyclerView.getRecyclerView() != null) {
            mRecyclerView.getRecyclerView().setAdapter(null);
        }
        super.onDestroyView();
    }
}
