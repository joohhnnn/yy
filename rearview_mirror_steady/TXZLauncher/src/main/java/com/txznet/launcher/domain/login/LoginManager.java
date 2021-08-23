package com.txznet.launcher.domain.login;

import android.os.Environment;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.data.entity.BaseResp;
import com.txznet.launcher.data.entity.BindInfoResp;
import com.txznet.launcher.data.http.ApiClient;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.utils.DeviceUtils;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.loader.AppLogic;

import java.io.File;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

/**
 * 登录业务类
 */
public class LoginManager extends BaseManager{

    private static volatile LoginManager sInstance;

//    private static final File INIT_FLAG_FILE = new File(Environment.getExternalStorageDirectory(), "/txz/.b_init"); // 标志位，决定是否完成了初始化

    private LoginManager() {
    }

    public static LoginManager getInstance() {
        if (null == sInstance) {
            synchronized (LoginManager.class) {
                if (null == sInstance) {
                    sInstance = new LoginManager();
                }
            }
        }
        return sInstance;
    }

//    private boolean bDeviceInit; // 设备初始化完毕(格式化过/首次使用)

    private boolean isLogout=false; // 是否登出过.这个状态会在登录成功和acc off时被清除。

    private Disposable mCheckQrCodeDisposable, mUnBindDisposable;

    // 检测设备绑定状态
    public void checkBindState() {
//        if (!INIT_FLAG_FILE.getParentFile().exists()) {
//            INIT_FLAG_FILE.getParentFile().mkdirs();
//        }
//        if (INIT_FLAG_FILE.exists()) {
//            bDeviceInit = true;
//        }
//        if (bDeviceInit) {
        AppLogic.removeBackGroundCallback(doCheckBindStateTask);
        AppLogic.runOnBackGround(doCheckBindStateTask);
//        } else {
//            AppLogic.removeBackGroundCallback(doUnbindTask);
//            AppLogic.runOnBackGround(doUnbindTask);
//        }
    }

//    private Runnable doUnbindTask = new Runnable() {
//        @Override
//        public void run() {
//            cancelUnBind();
//            String deviceId = DeviceUtils.getDeviceID();
//            mUnBindDisposable = ApiClient.getInstance().getApiService().unbind(deviceId)
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(new Consumer<BaseResp>() {
//                        @Override
//                        public void accept(BaseResp baseResp) throws Exception {
//                            if ("E0000".equals(baseResp.errorCode)) { // 成功返回
//                                INIT_FLAG_FILE.createNewFile();
//                                AppLogic.removeBackGroundCallback(doUnbindTask);
//                                AppLogic.removeBackGroundCallback(doCheckBindStateTask);
//                                AppLogic.runOnBackGround(doCheckBindStateTask);
//                            } else {
//                                AppLogic.removeBackGroundCallback(doUnbindTask);
//                                AppLogic.runOnBackGround(doUnbindTask, 3000);
//                            }
//                        }
//                    }, new Consumer<Throwable>() {
//                        @Override
//                        public void accept(Throwable throwable) throws Exception {
//                            if (throwable instanceof HttpException) { // 本身就未绑定的情况下，会返回404
//                                if (((HttpException) throwable).code() == 404) {
//                                    Object obj = ((HttpException) throwable).response().body();
//                                    if (obj != null && obj instanceof String && ((String) obj).contains("E4004")) {
//                                        AppLogic.removeBackGroundCallback(doUnbindTask);
//                                        AppLogic.removeBackGroundCallback(doCheckBindStateTask);
//                                        AppLogic.runOnBackGround(doCheckBindStateTask);
//                                    }
//                                }
//                            } else {
//                                AppLogic.removeBackGroundCallback(doUnbindTask);
//                                AppLogic.runOnBackGround(doUnbindTask, 3000);
//                            }
//                        }
//                    });
//        }
//    };

    private Runnable doCheckBindStateTask = new Runnable() {
        @Override

        public void run() {
            cancelCheckBindState();
            // 请求二维码状态
            mCheckQrCodeDisposable = ApiClient.getInstance().getApiService().getBindInfo(DeviceUtils.getDeviceID())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<BindInfoResp>() {
                        @Override
                        public void accept(BindInfoResp bindInfoResp) {
                            if (bindInfoResp.errorCode == null || "E0000".equals(bindInfoResp.errorCode)) {
                                LogUtil.logd("bindInfo " + bindInfoResp);
                                cancelCheckBindState();
                                PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_ANJIXING_LOGIN, true);
                                if (bindInfoResp.userInfo != null) {
                                    PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_ANJIXING_ACC_NAME, bindInfoResp.userInfo.name);
                                    PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_ANJIXING_ACC_BIRTHDAY, bindInfoResp.userInfo.birthday);
                                }
                                if (bindInfoResp.vehicleInfo != null) {
                                    PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_ANJIXING_ACC_VEHICLE_LICENSE, bindInfoResp.vehicleInfo.vehicleLicense);
                                }
                                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_ANJIXING_HAS_BIND);
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            LogUtil.logd("bindInfo throwable=" + throwable);
                            AppLogic.removeBackGroundCallback(doCheckBindStateTask);
                            AppLogic.runOnBackGround(doCheckBindStateTask, 3000);
                        }
                    });
        }
    };

    // 取消检测
    public void cancelCheckBindState() {
        if (mCheckQrCodeDisposable != null) {
            if (!mCheckQrCodeDisposable.isDisposed()) {
                mCheckQrCodeDisposable.dispose();
            }
            mCheckQrCodeDisposable = null;
        }
    }

    /**
     * 只清除保存的登录状态。
     * 不会发送事件，也不修改标志位
     */
    public void clearSaveLoginData(){
        PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_ANJIXING_LOGIN, false);
        PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_ANJIXING_ACC_NAME, null);
        PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_ANJIXING_ACC_BIRTHDAY, null);
        PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_ANJIXING_ACC_VEHICLE_LICENSE, null);
    }

    private void cancelUnBind() {
        if (mUnBindDisposable != null) {
            if (!mUnBindDisposable.isDisposed()) {
                mUnBindDisposable.dispose();
            }
            mUnBindDisposable = null;
        }
    }

    public boolean isLogout(){
        return isLogout;
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{
                EventTypes.EVENT_ANJIXING_LOGOUT,
                EventTypes.EVENT_ANJIXING_LOGIN,
                EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP
        };
    }

    @Override
    protected void onEvent(String eventType) {
        LogUtil.e("onEvent: eventType"+eventType);
        super.onEvent(eventType);
        switch (eventType) {
            // 登录成功后或者休眠触发时，将logout的标志去掉。
            case EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP:
            case EventTypes.EVENT_ANJIXING_LOGIN:
                isLogout=false;
                break;
            case EventTypes.EVENT_ANJIXING_LOGOUT:
                isLogout=true;
                break;
        }
    }
}
