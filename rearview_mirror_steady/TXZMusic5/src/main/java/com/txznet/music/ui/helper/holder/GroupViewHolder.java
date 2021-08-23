package com.txznet.music.ui.helper.holder;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.ui.helper.BaseViewHolder;
import com.txznet.music.ui.helper.entity.Group;

/**
 * @author QBW
 * @createtime 2016/04/05 11:29
 */


public class GroupViewHolder extends BaseViewHolder<Group> {
    private TextView text;

    public GroupViewHolder(Context context, ViewGroup parent) {
        super(context, R.layout.holder_group, parent);
    }

    @Override
    public void findView() {
        text = itemView.findViewById(R.id.text);
    }

    @Override
    public void bindData(int adapPos, Group group) {
        super.bindData(adapPos, group);
        text.setText(group.text);
    }
}
