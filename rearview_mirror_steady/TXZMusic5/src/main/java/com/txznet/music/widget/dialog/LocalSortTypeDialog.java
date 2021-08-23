package com.txznet.music.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

import com.txznet.music.R;
import com.txznet.music.action.LocalActionCreator;
import com.txznet.music.data.entity.SortType;
import com.txznet.rxflux.Operation;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 本地音乐排序方式选择对话框
 *
 * @author zackzhou
 * @date 2018/12/25,11:14
 */

public class LocalSortTypeDialog extends Dialog {

    @Bind(R.id.fl_content)
    ViewGroup flContent;
    @Bind(R.id.rl_sort_by_time)
    ViewGroup rlSortByTime;
    @Bind(R.id.rl_sort_by_name)
    ViewGroup rlSortByName;

    @Bind(R.id.rb_sort_by_time)
    RadioButton rBSortByTime;
    @Bind(R.id.rb_sort_by_name)
    RadioButton rBSortByName;

    SortType mSortType;

    public LocalSortTypeDialog(@NonNull Context context) {
        super(context, R.style.TXZ_Dialog_Style_Full);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_dialog_sort_type);
        ButterKnife.bind(this);

        flContent.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void refreshSortType() {
        if (mSortType == null || SortType.SORT_BY_TIME_DESC == mSortType) {
            rBSortByTime.setChecked(true);
            rBSortByName.setChecked(false);
        } else {
            rBSortByTime.setChecked(false);
            rBSortByName.setChecked(true);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        refreshSortType();
    }

    @OnClick({R.id.rl_sort_by_time, R.id.rl_sort_by_name, R.id.fl_content, R.id.tv_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.fl_content:
                dismiss();
                break;
            case R.id.rl_sort_by_time:
                LocalActionCreator.get().sortByTime(Operation.MANUAL);
                dismiss();
                break;
            case R.id.rl_sort_by_name:
                LocalActionCreator.get().sortByName(Operation.MANUAL);
                dismiss();
                break;
            case R.id.tv_cancel:
                dismiss();
                break;
        }
    }

    public void setSortType(SortType value) {
        mSortType = value;
    }
}
