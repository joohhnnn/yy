package com.txznet.music.ui.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.dinuscxj.itemdecoration.GridOffsetsItemDecoration;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.action.SettingActionCreator;
import com.txznet.music.config.ConfigUtils;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.store.SettingStore;
import com.txznet.music.ui.about.AboutUsFragment;
import com.txznet.music.ui.base.BaseFragment;
import com.txznet.music.ui.helper.HelpFragment;
import com.txznet.music.ui.settingPlayer.SettingPlayerUIFragment;
import com.txznet.music.util.ProgramUtils;
import com.txznet.music.util.ToastUtils;
import com.txznet.proxy.util.StorageUtil;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.ViewModelProviders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;

/**
 * 设置界面
 *
 * @author telen
 * @date 2018/12/18,10:18
 */
public class SettingFragment extends BaseFragment implements OnRecyclerViewListener.OnItemClickListener {

    @Bind(R.id.recycler_setting)
    RecyclerView recyclerSetting;
    SettingExpendAdapter mSettingExpendAdapter;


    List<RecyclerViewData<SettingBean, String>> mRecyclerViewData = new ArrayList<>();

    public void reqData() {


        if (ConfigUtils.getInstance().isShorPlayEnable()) {
            mRecyclerViewData.add(new RecyclerViewData<>(new SettingBean(getResources().getString(R.string.str_close_push_msg_init),
                    "可能会错过精彩内容，点击开启",
                    SharedPreferencesUtils.isOpenPush(),
                    (position, bean) -> SettingActionCreator.getInstance().clickBootPlay(Operation.MANUAL)),
                    Collections.emptyList()));
        }
        if (ConfigUtils.getInstance().isAsrEnable()) {
            mRecyclerViewData.add(new RecyclerViewData<>(
                    new SettingBean(
                            getResources().getString(R.string.str_open_asr_cmd),
                            SharedPreferencesUtils.isWakeupEnable() ? "已开启" : "未开启",
//                            SharedPreferencesUtils.isWakeupEnable(),
                            SettingBean.STYLE_TWO_LINE_ARROW,
                            (position, bean) -> {
//                                SettingActionCreator.getInstance().clickAsr(Operation.MANUAL);
                                new SettingAsrFragment().show(getChildFragmentManager(), SettingAsrFragment.class.getSimpleName());
                            }),
                    Collections.emptyList()));
        }
        if (ProgramUtils.isProgram()) {
            mRecyclerViewData.add(new RecyclerViewData<>(
                    new SettingBean(
                            "播放设置",
                            "播放时显示悬浮窗",
                            R.drawable.setting_right_arrow_icon,
                            (position, bean) -> {
                                new SettingPlayerUIFragment().show(getChildFragmentManager(), SettingPlayerUIFragment.class.getSimpleName());
                            }),
                    Collections.emptyList()));
        }
        long cacheSize = StorageUtil.getCacheSize();
        mRecyclerViewData.add(new RecyclerViewData<>(new SettingBean("清除缓存", cacheSize == 0 ? "0 MB" : StorageUtil.formatSize(cacheSize), (position, bean) -> {
            SettingActionCreator.getInstance().clickClearMemory(Operation.MANUAL);
            bean.rightText = "0 MB";
            ToastUtils.showShortOnUI("已清理");
            mSettingExpendAdapter.notifyItemChanged(position);
        }), Collections.emptyList()));

        mRecyclerViewData.add(new RecyclerViewData<>(new SettingBean("使用帮助",
                (position, bean) -> {
//                    SettingActionCreator.getInstance().clickHelp(Operation.MANUAL)
                    new HelpFragment().show(getChildFragmentManager(), "helpFragment");
                }
        ), Collections.emptyList()));
        mRecyclerViewData.add(new RecyclerViewData<>(new SettingBean(SettingBean.STYLE_ARROW, "关于我们",
                String.format("V %s", BuildConfig.VERSION_NAME),
                (position, bean) -> {
//                    SettingActionCreator.getInstance().clickHelp(Operation.MANUAL)
                    new AboutUsFragment().show(getChildFragmentManager(), AboutUsFragment.class.getSimpleName());
                }
        ), Collections.emptyList()));


        mSettingExpendAdapter.setAllDatas(mRecyclerViewData);

    }


    @Override
    public void initData(Bundle savedInstanceState) {
        SettingStore settingStore = ViewModelProviders.of(this).get(SettingStore.class);
        settingStore.getASREnable().observe(this, aBoolean -> {
            if (aBoolean == null) {
                return;
            }
            //获取哪一项
            for (int i = 0; i < mRecyclerViewData.size(); i++) {
                SettingBean groupData = mRecyclerViewData.get(i).getGroupData();
                if (groupData.leftText.equals(getResources().getString(R.string.str_open_asr_cmd))) {
                    groupData.subText = aBoolean ? "已开启" : "未开启";
                    int showParentIndex = mSettingExpendAdapter.getShowParentIndex(groupData);
                    mSettingExpendAdapter.notifyItemChanged(showParentIndex);

                    if (BuildConfig.DEBUG) {
                        Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",initData:" + showParentIndex + "," + i);
                    }


                    break;
                }
            }
        });
        settingStore.getBootEnable().observe(this, aBoolean -> {
            if (aBoolean == null) {
                return;
            }
//获取哪一项
            for (int i = 0; i < mRecyclerViewData.size(); i++) {
                if (mRecyclerViewData.get(i).getGroupData().leftText.equals(getResources().getString(R.string.str_close_push_msg_init))) {
                    mRecyclerViewData.get(i).getGroupData().choice = aBoolean;
                    mSettingExpendAdapter.notifyItemChanged(i);
                    break;
                }
            }
        });


        reqData();

    }

    @Override
    public int getLayout() {
        return R.layout.setting_fragment;
    }

    @Override
    protected void initView(View view) {
        tvTitle.setText("设置");

        mSettingExpendAdapter = new SettingExpendAdapter(getContext(), mRecyclerViewData);
        mSettingExpendAdapter.setOnItemClickListener(this);

        recyclerSetting.setLayoutManager(new GridLayoutManager(GlobalContext.get(), 2));
        recyclerSetting.setAdapter(mSettingExpendAdapter);
        GridOffsetsItemDecoration decor = new GridOffsetsItemDecoration(GridOffsetsItemDecoration.GRID_OFFSETS_VERTICAL);
        decor.setVerticalItemOffsets(getResources().getDimensionPixelOffset(R.dimen.m24));
        decor.setHorizontalItemOffsets(getResources().getDimensionPixelOffset(R.dimen.m24));
        decor.setOffsetEdge(false);
        recyclerSetting.addItemDecoration(decor);
        recyclerSetting.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

    }

    @Override
    public void onGroupItemClick(int position, int groupPosition, View view) {
        SettingBean groupData = mRecyclerViewData.get(groupPosition).getGroupData();
        if (groupData != null) {
            groupData.getListener().onClick(groupPosition, groupData);
        }
    }

    @Override
    public void onChildItemClick(int position, int groupPosition, int childPosition, View view) {
//        SettingActionCreator.getInstance().clickChangeFloatSetting(Operation.MANUAL, childPosition);
//        Toast.makeText(getActivity(), "child click:" + position + "," + groupPosition + "," + childPosition, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ReportEvent.reportSettingsEnter();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ReportEvent.reportSettingsExit();
    }
}
