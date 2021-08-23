package com.txznet.music.ui.helper.holder;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.ui.helper.BaseViewHolder;
import com.txznet.music.ui.helper.entity.Child;

/**
 * @author QBW
 * @createtime 2016/04/05 11:29
 */


public class ItemViewHolder extends BaseViewHolder<Child> {
    private TextView text;

    public ItemViewHolder(Context context, ViewGroup parent) {
        super(context, R.layout.holder_child, parent);
    }

    @Override
    public void findView() {
        text = itemView.findViewById(R.id.text);
    }

    @Override
    public void bindData(int adapPos, Child child) {
        super.bindData(adapPos, child);

        text.setText(child.text);
    }
}
