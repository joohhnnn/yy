package com.txznet.music.ui.base;

import android.os.Bundle;

import com.txznet.music.ui.base.adapter.BaseCheckPlayerAdapter;
import com.txznet.music.ui.base.header.HeaderDeleteItemView;
import com.txznet.music.ui.base.header.HeaderNormalItemView;
import com.txznet.music.widget.dialog.DeleteAudioDialog;

import java.util.List;

/**
 * @author telen
 * @date 2018/12/26,11:23
 */
public abstract class BaseCheckPlayerFragment<T extends BaseCheckPlayerAdapter> extends BasePlayerFragment<T> {
    private HeaderDeleteItemView mHeaderDelete;
    private HeaderNormalItemView mHeaderNormal;
    protected DeleteAudioDialog mDeleteAudioDialog;

    @Override
    public T getAdapter() {
        return super.getAdapter();
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        super.initData(savedInstanceState);

        mDeleteAudioDialog = new DeleteAudioDialog(getContext(), new DeleteAudioDialog.OnClickCallback() {
            @Override
            public void onConfirm() {
                getHeaderBar().removeHeader();
                getHeaderBar().addHeader(mHeaderNormal);
                mHeaderDelete.setChecked(false);

                onClickDeleteEvent();
                getAdapter().change2Check(false);
            }

            @Override
            public void onCancel() {

            }
        });
    }

    @Override
    protected void initAdapter(T adapter) {
        super.initAdapter(adapter);

        adapter.setCheckAllChangeListener(isCheckAll -> {
            mHeaderDelete.setChecked(isCheckAll);
        });
        mHeaderNormal = new HeaderNormalItemView(getContext(), getHeaderBar());
        mHeaderDelete = new HeaderDeleteItemView(getContext(), getHeaderBar());
        mHeaderNormal.setOnchangeHeaderListener(mHeaderDelete, v -> {
            adapter.change2Check(true);
        });
        mHeaderDelete.setOnCheckListener(adapter);
        mHeaderDelete.setOnchangeHeaderListener(mHeaderNormal, v -> {
            adapter.change2Check(false);
        });
        mHeaderDelete.setOnDeleteListener(adapter, v -> {
            //确认删除
            mDeleteAudioDialog.show();
        });
    }

//    public HeaderDeleteItemView getHeaderDelete() {
//        return mHeaderDelete;
//    }
//
//    public HeaderNormalItemView getHeaderNormal() {
//        return mHeaderNormal;
//    }

    /**
     * 点击确认删除
     */
    public abstract void onClickDeleteEvent();


    public void updateData(List data) {
        if (data.size() == 0) {
            getHeaderBar().removeHeader();
            getAdapter().clear();
        } else {
            getAdapter().clear();
            if (!getHeaderBar().hasHeader()) {
                getHeaderBar().addHeader(mHeaderNormal);
            }
            mHeaderNormal.setCount(data.size());
            getAdapter().addAll(data);
        }
    }

    public void updateData(List data, String headerTitle) {
        updateData(data);
        mHeaderNormal.setHeaderText(headerTitle);
    }

    protected abstract IHeaderBar getHeaderBar();
}
