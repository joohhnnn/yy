package com.txznet.comm.ui.dialog;

import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import com.txznet.txz.comm.R;

import java.util.List;

/**
 * Created by ASUS User on 2015/8/7.
 */
public abstract class WinConfirmList extends WinConfirm {
    public WinConfirmList() {
        super();
        initView();
    }

    public WinConfirmList(boolean isSystem) {
        super(isSystem);
        initView();
    }

    private void initView() {
        setLeftButton("确定");
        setRightButton("取消");
        mViewHolder.mText.setVisibility(View.GONE);
        mViewHolder.mTextList.setVisibility(View.VISIBLE);
    }


    public WinConfirmList setListItem(List<String> msgs) {
        mViewHolder.mTextList.setAdapter(new ArrayAdapter<String>(getContext(), R.layout.comm_win_list_item, msgs));
        return this;
    }

    public WinConfirmList setListAdapter(ListAdapter adapter) {
        mViewHolder.mTextList.setAdapter(adapter);
        return this;
    }

    @Override
    public WinConfirmList setTitle(String s) {
        super.setTitle(s);
        return this;
    }

    @Override
    public WinConfirmList setMessageData(Object data) {
        super.setMessageData(data);
        return this;
    }

    @Override
    public WinConfirmList setCancelText(String s) {
        super.setRightButton(s);
        return this;
    }

    @Override
    public WinConfirmList setSureText(String s) {
        super.setLeftButton(s);
        return this;
    }
}
