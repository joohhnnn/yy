package com.txznet.music.ui.setting;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.txznet.music.R;
import com.txznet.music.action.SettingActionCreator;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.store.SettingStore;
import com.txznet.music.ui.base.BaseFragment;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.ViewModelProviders;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * @author telen
 * @date 2019/1/8,20:26
 */
public class SettingAsrFragment extends BaseFragment {
    @Bind(R.id.cb_checked)
    CheckBox mCheckBox;

    @Override
    protected int getLayout() {
        return R.layout.setting_asr_fragment;
    }

    @Override
    protected void initView(View view) {
        tvTitle.setText(getResources().getString(R.string.str_open_asr_cmd));
    }

    @Override
    protected void initData(Bundle savedInstanceState) {
        mCheckBox.setChecked(SharedPreferencesUtils.isWakeupEnable());
        SettingStore settingStore = ViewModelProviders.of(this).get(SettingStore.class);
        settingStore.getASREnable().observe(this, aBoolean -> {
            mCheckBox.setChecked(aBoolean);
        });
    }


    @OnClick(R.id.ll_title)
    public void onViewClicked() {
        SettingActionCreator.getInstance().clickAsr(Operation.MANUAL);
    }
}
