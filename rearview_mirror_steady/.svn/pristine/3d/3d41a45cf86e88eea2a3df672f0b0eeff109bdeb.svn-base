package com.txznet.music.ui.base.header;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import com.txznet.music.R;
import com.txznet.music.ui.base.IHeaderBar;
import com.txznet.music.ui.base.IHeaderView;
import com.txznet.music.ui.base.adapter.BaseCheckPlayerAdapter;
import com.txznet.music.util.ToastUtils;

/**
 * @author telen
 * @date 2018/12/25,16:19
 */
public class HeaderDeleteItemView implements IHeaderView {

    private CheckBox mCheckBox;

    private IHeaderBar mHeaderBar;

    private View mHeaderView;


    public HeaderDeleteItemView(Context ctx, IHeaderBar headerBar) {
        mHeaderBar = headerBar;
        mHeaderView = View.inflate(ctx, R.layout.header_delete_msg_include, null);
        mCheckBox = mHeaderView.findViewById(R.id.cb_choice);
    }

    public void setChecked(boolean isChecked) {
        mCheckBox.setChecked(isChecked);
    }


    public void setOnCheckListener(BaseCheckPlayerAdapter adapter) {
        mCheckBox.setOnClickListener(v -> {
            adapter.change2AllSelected(mCheckBox.isChecked());
        });
    }

    public void setOnchangeHeaderListener(IHeaderView headerItemView, View.OnClickListener listener) {
        mHeaderView.findViewById(R.id.btn_delete_cancel).setOnClickListener((View v) -> {
            mHeaderBar.removeHeader();
            mHeaderBar.addHeader(headerItemView);
            //复位
            mCheckBox.setChecked(false);
            if (listener != null) {
                listener.onClick(v);
            }
        });
    }

    public void setOnDeleteListener(BaseCheckPlayerAdapter adapter, View.OnClickListener listener) {
        mHeaderView.findViewById(R.id.btn_delete_sure).setOnClickListener(v -> {
            if (adapter.getCheckedData().size() > 0) {
                listener.onClick(v);
            } else {
                ToastUtils.showLongOnUI("你还没有选择内容");
            }
        });
    }

    @Override
    public View getView() {
        return mHeaderView;
    }
}
