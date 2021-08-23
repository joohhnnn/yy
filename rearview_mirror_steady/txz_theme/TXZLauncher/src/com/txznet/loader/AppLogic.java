package com.txznet.loader;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.launcher.R;
import com.txznet.launcher.ThemeObservable;
import com.txznet.launcher.helper.ThemeManager;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZAsrManager.CommandListener;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZConfigManager.AsrEngineType;
import com.txznet.sdk.TXZConfigManager.InitListener;
import com.txznet.sdk.TXZConfigManager.InitParam;
import com.txznet.sdk.TXZConfigManager.TtsEngineType;
import com.txznet.sdk.TXZResourceManager;

import android.content.ComponentName;
import android.content.Intent;

public class AppLogic extends AppLogicBase {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化sdk
        TXZConfigManager.getInstance().initialize(GlobalContext.get(),
//                new InitParam(//
//                        GlobalContext.get().getString(R.string.txz_sdk_init_app_id), // 接入的appId
//                        GlobalContext.get().getString(R.string.txz_sdk_init_app_token) // 接入的appToken
//                ).setWakeupKeywordsNew(AppLogic.this.getResources().getStringArray(R.array.txz_sdk_init_wakeup_keywords) // 设置的唤醒词
//                ).setTtsType(TtsEngineType.TTS_YUNZHISHENG)
//                .setAsrType(AsrEngineType.ASR_YUNZHISHENG),
                new InitListener() {
                    @Override
                    public void onSuccess() {
                        TXZAsrManager.getInstance().regCommand(
                                new String[]{"打开应用"}, "CMD_LAUNCHER_OPEN_ALL_APPS_VIEW");
                        TXZAsrManager.getInstance().addCommandListener(
                                new CommandListener() {
                                    @Override
                                    public void onCommand(String cmd, String data) {
                                        if (data.equals("CMD_LAUNCHER_OPEN_ALL_APPS_VIEW")) {
                                        	TXZResourceManager.getInstance().speakTextOnRecordWin("将为你打开应用", true, new Runnable() {
												@Override
												public void run() {
													openAllAppsView();
												}
											});
                                        }
                                    }
                                });
                    }

                    @Override
                    public void onError(int arg0, String arg1) {
                    }
                });

        // 初始化主题相关组件
        mThemeObservable = new ThemeObservable(GlobalContext.get());
        ThemeManager.getInstance(GlobalContext.get());
    }

    public static void openAllAppsView() {
        ComponentName componentName = new ComponentName("com.txznet.launcher", "com.txznet.launcher.ui.AllAppView");
        Intent intent = new Intent();
        intent.setComponent(componentName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (intent != null)
        	GlobalContext.get().startActivity(intent);
    }

    private static ThemeObservable mThemeObservable;

    public static void registerThemeObserver(ThemeObservable.ThemeObserver observer) {
        if (mThemeObservable != null) {
            mThemeObservable.registerObserver(observer);
        }
    }

    public static void unregisterThemeObserver(ThemeObservable.ThemeObserver observer) {
        if (mThemeObservable != null && mThemeObservable.containsObserver(observer)) {
            mThemeObservable.unregisterObserver(observer);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        mThemeObservable.release();
    }
}
