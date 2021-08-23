package com.txznet.webchat.stores;

import com.txznet.reserve.activity.ReserveSingleTopActivity0;
import com.txznet.reserve.activity.ReserveSingleTopActivity3;
import com.txznet.reserve.activity.ReserveSingleTopActivity6;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.ui.base.AppBaseActivity;

/**
 * 主题Store
 * Created by J on 2016/10/18.
 */

public class WxThemeStore extends Store {
    public static final String THEME_CAR = "car";
    public static final String THEME_CAR_PORTRAIT = "car_portrait";
    public static final String THEME_CAR_PORTRAIT_T700 = "car_portrait_t700";
    public static final String THEME_MIRROR = "mirror";

    private static final WxThemeStore sInstance = new WxThemeStore(Dispatcher.get());

    public static WxThemeStore get() {
        return sInstance;
    }

    private WxThemeStore(Dispatcher dispatcher) {
        super(dispatcher);
    }

    @Override
    public void onDispatch(Action action) {

    }

    public String getCurrentTheme() {
        return WxConfigStore.getInstance().getUITheme();
    }

    public boolean isPortraitTheme() {
        return THEME_CAR_PORTRAIT.equals(getCurrentTheme())
                || THEME_CAR_PORTRAIT_T700.equals(getCurrentTheme());
    }

    public Class<? extends AppBaseActivity> getClassForQRActivity() {
        if (THEME_CAR.equals(getCurrentTheme()) || THEME_CAR_PORTRAIT.equals(getCurrentTheme())) {
            return ReserveSingleTopActivity3.class;
        }

        if (THEME_CAR_PORTRAIT_T700.equals(getCurrentTheme())) {
            return ReserveSingleTopActivity6.class;
        }

        return ReserveSingleTopActivity0.class;
    }
}
