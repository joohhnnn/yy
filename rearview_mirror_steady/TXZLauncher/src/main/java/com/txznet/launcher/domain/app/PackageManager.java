package com.txznet.launcher.domain.app;

import android.content.pm.PackageInfo;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.domain.app.bean.AppInfo;

import org.json.JSONObject;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.schedulers.Schedulers;

/**
 * 应用信息业务类
 */
public class PackageManager extends BaseManager {
    private static PackageManager sInstance;

    private String[] mAppNameArr;
    private String[] mAppPkgArr;

    private PackageManager() {
        mAppNameArr = GlobalContext.get().getResources().getStringArray(R.array.inner_app_name);
    }

    public static PackageManager getInstance() {
        if (sInstance == null) {
            sInstance = new PackageManager();
        }
        return sInstance;
    }

    @Override
    public void init() {
        super.init();
        mAppNameArr = GlobalContext.get().getResources().getStringArray(R.array.inner_app_name);
        mAppPkgArr = GlobalContext.get().getResources().getStringArray(R.array.inner_app_pkg);
    }

    public Observable<List<AppInfo>> getAppInfoList() {
        Observable<String> appNameObs = Observable.fromArray(mAppNameArr);
        Observable<String> appPkgObs = Observable.fromArray(mAppPkgArr);
        Observable<String> versionObs = Observable.fromArray(mAppPkgArr).concatMap(new Function<String, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(String s) throws Exception {
                return getVersionName(s);
            }
        });
        return Observable.zip(appNameObs, appPkgObs, versionObs, new Function3<String, String, String, AppInfo>() {
            @Override
            public AppInfo apply(String appName, String appPkg, String version) throws Exception {
                AppInfo appInfo = new AppInfo();
                appInfo.appName = appName;
                appInfo.pkgName = appPkg;
                appInfo.versionName = version;
                return appInfo;
            }
        }).toList().toObservable();
    }

    /*
        根据包名获取版本名
     */
    public Observable<String> getVersionName(@NonNull final String pkgName) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(final ObservableEmitter<String> e) throws Exception {
                if (pkgName.startsWith("com.txznet")) {
                    ServiceManager.getInstance().sendInvoke(pkgName, "comm.PackageInfo", null, new ServiceManager.GetDataCallback() {
                        @Override
                        public void onGetInvokeResponse(ServiceManager.ServiceData data) {
                            if (data == null || data.getJSONObject() == null) {
                                LogUtil.logw("get package VERSION failed: " + pkgName);
                                e.onNext(getVersionNameByPm(pkgName));
                                e.onComplete();
                                return;
                            }
                            JSONObject json = data.getJSONObject();
                            e.onNext(json.optString("versionName", getVersionNameByPm(pkgName)));
                            e.onComplete();
                        }
                    });
                } else {
                    e.onNext(getVersionNameByPm(pkgName));
                    e.onComplete();
                }
            }
        });
    }

    public boolean checkAppInstalled(String pkgName){
        if (pkgName== null || pkgName.isEmpty()) {
            return false;
        }
        PackageInfo packageInfo;
        try {
            packageInfo = GlobalContext.get().getPackageManager().getPackageInfo(pkgName, 0);
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            packageInfo = null;
            e.printStackTrace();
        }
        return packageInfo != null;
    }

    private String getVersionNameByPm(@NonNull String pkgName) {
        try {
            PackageInfo packInfo = GlobalContext.get()
                    .getPackageManager()
                    .getPackageInfo(pkgName, 0);
            return TextUtils.isEmpty(packInfo.versionName) ? "" : packInfo.versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }
}
