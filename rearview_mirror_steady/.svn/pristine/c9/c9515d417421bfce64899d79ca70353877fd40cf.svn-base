package com.txznet.webchat.ui.common.widget.user_picker;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.squareup.otto.Subscribe;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.LoginActionCreator;
import com.txznet.webchat.comm.plugin.model.WxUserCache;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.log.L;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.widgets.AppBaseWinDialog;

import java.util.List;

import butterknife.Bind;

/**
 * 推送登录用户选择Dialog
 * Created by J on 2017/3/27.
 */

public class UserPickerDialog extends AppBaseWinDialog {
    private static final String LOG_TAG = "UserPickerDialog";
    @Bind(R.id.rl_user_picker_root)
    RelativeLayout mRlRoot;
    @Bind(R.id.rv_user_picker_list)
    RecyclerView mRlList;

    private LinearLayoutManager mLayoutManager;
    private UserPickerAdapter mAdapter;

    public UserPickerDialog() {
        super(false);
    }

    public void setUserList(List<WxUserCache> userList) {
        mAdapter.setUserList(userList);
    }

    @Override
    public int getLayout() {
        if (WxThemeStore.get().isPortraitTheme()) {
            return R.layout.layout_user_picker_dialog_portrait;
        }

        return R.layout.layout_user_picker_dialog;
    }

    @Override
    public void init() {
        initUserList();

        mRlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void initUserList() {
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mAdapter = new UserPickerAdapter(getContext());

        mRlList.setLayoutManager(mLayoutManager);
        mRlList.setAdapter(mAdapter);

        mAdapter.setUserPickerListener(new UserPickerAdapter.OnUserPickerListener() {
            @Override
            public void onPickUser(String uid) {
                L.i(LOG_TAG, "user selected: " + uid);
                LoginActionCreator.get().switchUserCache(uid);

                dismiss();
            }

            @Override
            public void onPickAdd() {
                L.e(LOG_TAG, "add selected");
                LoginActionCreator.get().refreshQRCode();

                dismiss();
            }
        });

        mRlList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void initFocusViewList() {
        getNavBtnSupporter().setViewList(mRlList);

        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocus(mRlList);
        }
    }

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
