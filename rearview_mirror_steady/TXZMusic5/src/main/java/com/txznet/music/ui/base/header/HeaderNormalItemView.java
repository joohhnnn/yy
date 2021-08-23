package com.txznet.music.ui.base.header;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.ui.base.IHeaderBar;
import com.txznet.music.ui.base.IHeaderView;

import java.util.Locale;

/**
 * @author telen
 * @date 2018/12/25,16:18
 */
public class HeaderNormalItemView implements IHeaderView {
    private TextView mTvCount;
    private IHeaderBar mHeaderBar;

    private View mHeaderView;

    public HeaderNormalItemView(Context ctx, IHeaderBar headerBar) {
        mHeaderBar = headerBar;
        mHeaderView = View.inflate(ctx, R.layout.header_normal_include, null);
        mTvCount = mHeaderView.findViewById(R.id.tv_count);
    }

    @Override
    public View getView() {
        return mHeaderView;
    }

    public void setCount(int count) {
        mTvCount.setText(String.format(Locale.CHINA, "共%d条数据", count));
    }

    public void setHeaderText(String content) {
        mTvCount.setText(content);
    }

    public void setOnchangeHeaderListener(IHeaderView headerItemView, View.OnClickListener listener) {
        mHeaderView.findViewById(R.id.ll_delete_mgr).setOnClickListener((View v) -> {
            mHeaderBar.removeHeader();
            mHeaderBar.addHeader(headerItemView);
            if (listener != null) {
                listener.onClick(v);
            }
        });
    }
}
