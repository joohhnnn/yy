package com.txznet.launcher.module.settings;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.app.PackageManager;
import com.txznet.launcher.domain.app.bean.AppInfo;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.launcher.utils.RecyclerAdapter;
import com.txznet.loader.AppLogic;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 应用信息界面
 */
public class SystemAppInfoModule extends BaseModule {

    @Bind(R.id.lv_settings_app_info)
    RecyclerView lvAppInfo;

    private Context mContext;

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_settings_app_info, null);
        ButterKnife.bind(this, view);
        initialView(context);
        return view;
    }

    private void initialView(final Context context) {
        mContext = context;
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext) {
            @Override
            public boolean isAutoMeasureEnabled() {
                return true;
            }
        };
        linearLayoutManager.setAutoMeasureEnabled(true);
        lvAppInfo.setLayoutManager(linearLayoutManager);
        refreshContainer();
    }

    private void refreshList(List<AppInfo> appInfoList) {
        lvAppInfo.setAdapter(new RecyclerAdapter<AppInfo>(mContext, appInfoList, R.layout.layout_app_info_item) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder helper, int position, AppInfo item) {
                String txt = item.appName + ": V" + item.versionName;
                TextView tvAppInfo = (TextView) helper.getView(R.id.tv_app_info);
                tvAppInfo.setText(txt);
            }
        });
    }

    private void refreshContainer() {
        PackageManager.getInstance().getAppInfoList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<AppInfo>>() {
                    @Override
                    public void accept(List<AppInfo> appInfoList) throws Exception {
                        LogUtil.logd("success " + appInfoList);
                        refreshList(appInfoList);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        LogUtil.logd("throwable " + throwable);
                    }
                });
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        refreshContainer();
    }

    private Runnable closeRunnable = new Runnable() {
        @Override
        public void run() {
            LaunchManager.getInstance().showAtImageBottom("");
            LaunchManager.getInstance().launchDesktop();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        AppLogic.removeUiGroundCallback(closeRunnable);
        AppLogic.runOnUiGround(closeRunnable, PreferenceUtil.getInstance().getLong(PreferenceUtil.KEY_WECHAT_BIND_QR_TIMEOUT, PreferenceUtil.DEFAULT_WECHAT_BIND_QR_TIMEOUT));
    }

    @Override
    public void onPreRemove() {
        super.onPreRemove();
        AppLogic.removeUiGroundCallback(closeRunnable);
    }
}
