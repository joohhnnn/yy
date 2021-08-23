package com.txznet.music.ui.helper.holder;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.ui.helper.BaseViewHolder;
import com.txznet.music.ui.helper.entity.GroupChild;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author QBW
 * @createtime 2016/04/05 11:29
 */


public class GroupItemViewHolder extends BaseViewHolder<GroupChild> {
    @Bind(R.id.tv_command)
    TextView tvCommand;
    @Bind(R.id.tv_speak)
    TextView tvSpeak;
    @Bind(R.id.ll_right_range)
    View llRightRange;

    public GroupItemViewHolder(Context context, ViewGroup parent) {
        super(context, R.layout.holder_groupchild, parent);
    }

    @Override
    public void findView() {
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindData(int adapPos, GroupChild groupChild) {
        super.bindData(adapPos, groupChild);
        tvCommand.setText(groupChild.LeftText);
        tvSpeak.setText(groupChild.mSpeakText);
        if (groupChild.isNeedShowRight) {
            llRightRange.setVisibility(View.VISIBLE);
            itemView.setOnClickListener(groupChild.mOnClickListener);
        } else {
            llRightRange.setVisibility(View.GONE);
            itemView.setOnClickListener(null);
        }


    }
}
