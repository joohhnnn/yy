package com.txznet.music.ui.helper;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.ui.base.BaseFragment;
import com.txznet.music.ui.helper.entity.Child;
import com.txznet.music.ui.helper.entity.Group;

import butterknife.Bind;

/**
 * @author telen
 * @date 2019/1/3,16:14
 */
public class HelpDetailFragment extends BaseFragment {

    private final static String KEY_TITLE = "KEY_TITLE";
    private final static String KEY_CHILD = "KEY_CHILD";

    @Bind(R.id.rv_detail)
    RecyclerView mRecyclerView;


    private Adapter mAdapter;


    public static HelpDetailFragment newInstance(String groupTitle, String childContent) {
        Bundle bundle = new Bundle();
        bundle.putString(KEY_TITLE, groupTitle);
        bundle.putString(KEY_CHILD, childContent);
        HelpDetailFragment helpFragment = new HelpDetailFragment();
        helpFragment.setArguments(bundle);
        return helpFragment;
    }

    @Override
    protected int getLayout() {
        return R.layout.help_detail_fragment;
    }

    @Override
    protected void initView(View view) {
        tvTitle.setText("返回");
        Context appCtx = GlobalContext.get();//要养成好的习惯，除非需要Activity作为Context，否则能用ApplicationContext就尽量使用，减少对Activity的强引用

        mRecyclerView.setLayoutManager(new LinearLayoutManager(appCtx));
        mRecyclerView.setAdapter(mAdapter = new Adapter(appCtx));
        mRecyclerView.addItemDecoration(new DividerDecoration(getResources().getColor(R.color.base_divider), 1,getResources().getDimensionPixelOffset(R.dimen.base_divider_margin), getResources().getDimensionPixelOffset(R.dimen.base_divider_margin)));
        mRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null) {
            Group groupTitle = new Group(arguments.getString(KEY_TITLE));
            int i = mAdapter.addGroup(groupTitle);
            mAdapter.addGroupChild(i, new Child(arguments.getString(KEY_CHILD)));
        }


    }
}
