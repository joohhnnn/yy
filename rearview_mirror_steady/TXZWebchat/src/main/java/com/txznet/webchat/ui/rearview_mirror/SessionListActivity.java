package com.txznet.webchat.ui.rearview_mirror;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;

import com.squareup.otto.Subscribe;
import com.txznet.webchat.R;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxContactFocusStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.stores.WxResourceStore;
import com.txznet.webchat.ui.base.AppBaseActivity;
import com.txznet.webchat.ui.rearview_mirror.adapter.SessionListAdapter;
import com.txznet.webchat.ui.rearview_mirror.widget.PagedRecyclerView;

import butterknife.Bind;

/**
 * 会话列表页面
 * Created by J on 2016/3/24.
 */
public class SessionListActivity extends AppBaseActivity {
    @Bind(R.id.btn_session_list_back)
    ImageButton mBtnBack;
    @Bind(R.id.rv_session_list)
    PagedRecyclerView mRvList;

    private LinearLayoutManager mLayoutManager;
    private SessionListAdapter mAdapter;
    private int mListScrollState;
    private boolean mNeedRefreshUsericon = false; // 是否需要刷新用户头像，防止列表滑动时刷新头像造成卡顿

    @Override
    protected int getLayout() {
        return R.layout.activity_session_list;
    }

    @Override
    protected Store[] getRegisterStores() {
        return new Store[]{
                WxContactStore.getInstance(),
                WxContactFocusStore.getInstance(),
                WxResourceStore.get(),
                AppStatusStore.get(),
        };
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        mLayoutManager = new LinearLayoutManager(this);
        mAdapter = new SessionListAdapter(this);

        mRvList.setLayoutManager(mLayoutManager);
        mRvList.setAdapter(mAdapter);

        if (WxConfigStore.getInstance().isBackButtonEnabled()) {
            mBtnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        } else {
            mBtnBack.setVisibility(View.GONE);
        }


        mAdapter.setOnItemClickListener(new SessionListAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View v, int index) {
                String openId = mAdapter.getItem(index).mUserOpenId;
                ChatActivity.show(SessionListActivity.this, openId);
            }
        });

        //mAdapter.setContactList(WxContactStore.get().getSessionList());
        mAdapter.setContactList(WxContactStore.getInstance().getSessionList());

        mRvList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                //
                mListScrollState = newState;

                switch (mListScrollState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        // refresh usericon if needed
                        if (mNeedRefreshUsericon) {
                            updateUserIcons();
                            mNeedRefreshUsericon = false;
                        }
                        break;

                    default:
                        break;
                }

            }
        });
    }

    @Override
    protected void initFocusViewList() {
        getNavBtnSupporter().setViewList(mRvList);

        // 设置默认焦点
        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocus(mRvList);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateList();
    }

    @Subscribe
    @Override
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);

        switch (event.getType()) {
            case WxContactStore.EVENT_TYPE_ALL:
                updateSessions();
                break;

            case WxContactFocusStore.EVENT_TYPE_ALL:
                updateSessionFocus();
                break;

            case WxResourceStore.EVENT_TYPE_ALL:
                updateUserIcons();
                break;

            case AppStatusStore.EVENT_TYPE_ALL: // 屏蔽群消息状态变化
                if (mAdapter != null) {
                    mAdapter.notifyDataSetChanged();
                }
                break;

            case WxMessageStore.EVENT_TYPE_ALL:
                updateSessions();
                break;
        }
    }

    private void updateUserIcons() {
        if (mListScrollState == RecyclerView.SCROLL_STATE_IDLE) {
            updateList();
        } else {
            mNeedRefreshUsericon = true;
        }
    }

    private void updateSessions() {
        updateList();
        //mRvList.scrollToPage(0);
    }

    private void updateSessionFocus() {
        // 焦点会话发生变化时, 跳转到对应的会话页面
        String focusId = WxContactFocusStore.getInstance().getFocusedSession();

        if (!TextUtils.isEmpty(focusId)) {
            ChatActivity.show(this, focusId);
        }
    }

    private void updateList() {
        if (mAdapter != null) {
            mAdapter.setContactList(WxContactStore.getInstance().getSessionList());
            mAdapter.notifyDataSetChanged();
        }
    }

}
