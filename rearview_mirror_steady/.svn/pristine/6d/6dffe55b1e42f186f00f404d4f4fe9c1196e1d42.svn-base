package com.txznet.music.ui.settingPlayer;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jude.easyrecyclerview.decoration.DividerDecoration;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.action.SettingActionCreator;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.ui.base.BaseFragment;
import com.txznet.music.util.adapter.RecyclerAdapter;
import com.txznet.rxflux.Operation;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;

/**
 * @author telen
 * @date 2019/1/4,14:35
 */
public class SettingPlayerUIFragment extends BaseFragment {
    @Bind(R.id.rv_data)
    RecyclerView mRecyclerView;

    List<SettingUIBean> mSettingUIBeans = new ArrayList<>();

    SettingUIBean mCheckedSettingBean;

    @Override
    protected int getLayout() {
        return R.layout.setting_player_fragment;
    }

    @Override
    protected void initView(View view) {

        SettingUIBean showSettingUIBean = new SettingUIBean(SettingUIBean.TYPE_SHOW, "显示悬浮窗", "面一直出现悬浮窗，可快速进入应用发现自己感兴趣的内容");
        SettingUIBean showWhenSettingBean = new SettingUIBean(SettingUIBean.TYPE_SHOW_WHEN, "播放时显示悬浮窗", "使用时才出现悬浮窗，方便快速进入应用选择自己感兴趣的内容");
        SettingUIBean dismissSettingUIBean = new SettingUIBean(SettingUIBean.TYPE_DISMISS, "隐藏悬浮窗", "后台执行，需要进入应用可长按语音助手或声控打开应用");
        mSettingUIBeans.add(showSettingUIBean);
        mSettingUIBeans.add(showWhenSettingBean);
        mSettingUIBeans.add(dismissSettingUIBean);

        if (SharedPreferencesUtils.getFloatUIType() == SettingUIBean.TYPE_SHOW_WHEN) {
            mCheckedSettingBean = showWhenSettingBean;
        } else if (SharedPreferencesUtils.getFloatUIType() == SettingUIBean.TYPE_DISMISS) {
            mCheckedSettingBean = dismissSettingUIBean;
        } else {
            mCheckedSettingBean = showSettingUIBean;
        }


        mRecyclerView.setLayoutManager(new LinearLayoutManager(GlobalContext.get()));
        mRecyclerView.addItemDecoration(new DividerDecoration(getResources().getColor(R.color.base_divider), 1, getResources().getDimensionPixelOffset(R.dimen.base_divider_margin), getResources().getDimensionPixelOffset(R.dimen.base_divider_margin)));
        mRecyclerView.setAdapter(new RecyclerAdapter<SettingUIBean>(GlobalContext.get(), mSettingUIBeans, R.layout.setting_player_item_view) {

            @Override
            public void convert(RecyclerAdapter.ViewHolder holder, int position, SettingUIBean item) {
                TextView tvTitle = (TextView) holder.getView(R.id.tv_setting_title);
                TextView tvSubTitle = (TextView) holder.getView(R.id.tv_setting_sub_title);
                CheckBox cbCheck = (CheckBox) holder.getView(R.id.cb_checked);

                tvSubTitle.setText(item.subTitle);
                tvTitle.setText(item.title);
                cbCheck.setChecked(mCheckedSettingBean == item);

                holder.itemView.setOnClickListener(v -> {
                    if (cbCheck.isChecked()) {
                        return;
                    }
//刷新
                    int oriPosition = mSettingUIBeans.indexOf(mCheckedSettingBean);
                    mCheckedSettingBean = item;

                    cbCheck.setChecked(true);
//                    通知出去
                    SettingActionCreator.getInstance().clickChangeFloatSetting(Operation.MANUAL, item.type);

                    notifyItemChanged(oriPosition);
                    notifyItemChanged(position);

                });
            }
        });

    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        tvTitle.setText("悬浮窗设置");
    }

}
