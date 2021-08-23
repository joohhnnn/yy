package com.txznet.launcher.domain;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.guide.GuideManager;
import com.txznet.launcher.domain.nav.NavManager;
import com.txznet.launcher.domain.settings.SettingsManager;
import com.txznet.launcher.domain.txz.RecordWinManager;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.module.DesktopModule;
import com.txznet.launcher.module.IModule;
import com.txznet.launcher.module.help.HelpModule;
import com.txznet.launcher.module.login.LoginModule;
import com.txznet.launcher.module.nav.PoiIssuedModule;
import com.txznet.launcher.module.nav.TmcModule;
import com.txznet.launcher.module.notification.TodayNoticeModule;
import com.txznet.launcher.module.record.ChatListModule;
import com.txznet.launcher.module.record.ChatStockModule;
import com.txznet.launcher.module.record.ChatTtsModule;
import com.txznet.launcher.module.record.ChatWeatherModule;
import com.txznet.launcher.module.settings.APModule;
import com.txznet.launcher.module.settings.SystemAppInfoModule;
import com.txznet.launcher.module.settings.SystemInfoModule;
import com.txznet.launcher.module.wechat.WechatBindModule;
import com.txznet.launcher.module.wechat.WechatDialogModule;
import com.txznet.launcher.module.wechat.WechatQrModule;
import com.txznet.launcher.module.wechat.WechatRecordModule;
import com.txznet.launcher.module.wechat.bean.WechatMsgData;
import com.txznet.launcher.utils.DateUtils;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.launcher.widget.IImage;
import com.txznet.launcher.widget.StatusBar;
import com.txznet.launcher.widget.container.DialogRecordWin;
import com.txznet.launcher.widget.container.MainContainer;
import com.txznet.launcher.widget.container.ViewContainer;
import com.txznet.launcher.widget.image.SQImageDynamic;
import com.txznet.launcher.widget.image.SQImageLittle;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZRecordWinManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.txznet.launcher.domain.LaunchManager.ViewModuleType.TYPE_DESKTOP;
import static com.txznet.launcher.widget.container.MainContainer.VIEW_TYPE_CONTENT;
import static com.txznet.launcher.widget.container.MainContainer.VIEW_TYPE_IMAGE;

/**
 * Created by meteorluo on 2018/2/14.
 */
public class LaunchManager extends BaseManager {
    public interface ViewLifeCallback {
        void onResume();

        void onPreRemove();
    }

    private class ViewMark {
        public String type;
        public View content;
        public IModule module;
        public boolean isShowing;
        public ViewLifeCallback callback;
        public int status;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ViewModuleType {
        String TYPE_HELLO = "hello";
        String TYPE_TTS_DISPLAY = "tts_display";
        String TYPE_POIISSUED = "poiIssued";
        String TYPE_TMC = "tmc";
        String TYPE_DESKTOP = "desktop";
        String TYPE_NOTIFICATION = "notification";
        String TYPE_CHAT_WEATHER = "chat_weather";
        String TYPE_CHAT_STOCK = "chat_stock";
        String TYPE_CHAT_LIST = "chat_list";
        String TYPE_WECHAT_QR_MODULE = "wechat_qr_module";
        String TYPE_WECHAT_RECORD_MODULE = "wechat_record_module";
        String TYPE_WECHAT_MSG_DIALOG = "wechat_msg_dialog";
        String TYPE_LOGIN = "login_module";
        String TYPE_WECHAT_BIND = "wechat_bind";
        String TYPE_SETTINGS_AP = "settings_ap";
        String TYPE_SETTINGS_SYS_INFO = "settings_sys_info";
        String TYPE_SETTINGS_APP_INFO = "settings_app_info";
        String TYPE_HELP = "help";
    }

    /**
     * Launcher是否在桌面
     */
    public static final int FLAG_IS_FOREGROUND = 0x0001;
    /**
     * 是否已经打开了音乐
     */
    public static final int FLAG_IS_MUSIC_WORKING = 0x0002;
    /**
     * 是否微信登录成功
     */
    public static final int FLAG_IS_WX_LOGIN = 0x0004;
    /**
     * 是否处于导航中
     */
    public static final int FLAG_IS_NAV_WORKING = 0x0008;
    /**
     * 是否处于声控中
     */
    public static final int FLAG_IS_VOICE_OPEN = 0x0010;

    private int mPrivateFlag;
    private Context mContext;
    private ViewGroup mActRootView;
    private MainContainer mContainer;
    private DialogRecordWin mDialogWin;

    private List<ViewMark> mCaches = new ArrayList<>();
    private List<ViewMark> mBackStacks = new ArrayList<>();

    private ViewContainer.ContainerCallback mCallback = new MainContainer.ContainerCallback() {
        @Override
        public void onViewPreRemove(int type, View view) {
            onViewRemove(type, view);
        }

        @Override
        public void onViewAdded(int type, View view) {
            onViewAdd(type, view);
        }
    };

    private static LaunchManager sInstance = new LaunchManager();

    public static LaunchManager getInstance() {
        return sInstance;
    }


    @Override
    public void init() {
        super.init();
        mContext = GlobalContext.get();
    }

    public LaunchManager initContainer() {
        if (mContext == null) {
            mContext = GlobalContext.get();
        }
        mContainer = new MainContainer(mContext);
        mContainer.setContainerCallback(mCallback);
        mContainer.setImageView(new SQImageDynamic(mContext));
        mContainer.setStatusBar(new StatusBar(mContext));
        mContainer.setViewGroupBackground(R.drawable.full_image_bg);
        mContainer.setClipChildren(false);
        return this;
    }

    private void onViewRemove(int type, View view) {
        if (type == VIEW_TYPE_CONTENT) {
            synchronized (mCaches) {
                for (ViewMark mark : mCaches) {
                    if (view == mark.content) {
                        onViewMarkRemove(mark);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 移除
     *
     * @param mark
     */
    private void onViewMarkRemove(ViewMark mark) {
        LogUtil.logd("onViewMarkRemove:" + mark.type);
        mark.isShowing = false;
        IModule module = mark.module;
        if (module != null) {
            module.onPreRemove();
        }
        ViewLifeCallback callback = mark.callback;
        if (callback != null) {
            callback.onPreRemove();
        }
    }

    private void onViewAdd(int type, View view) {
        if (type == VIEW_TYPE_CONTENT) {
            synchronized (mCaches) {
                for (ViewMark mark : mCaches) {
                    if (view == mark.content) {
                        onViewMarkAdd(mark);
                        break;
                    }
                }
            }
        } else if (type == VIEW_TYPE_IMAGE) {
            onTimeUpdate();
        }
    }

    private void onViewMarkAdd(ViewMark mark) {
        LogUtil.logd("onViewMarkAdd:" + mark.type);
        mark.isShowing = true;
        IModule module = mark.module;
        if (module != null) {
            module.onResume();
        }
        ViewLifeCallback callback = mark.callback;
        if (callback != null) {
            callback.onResume();
        }
    }

    public LaunchManager assignViewParent(ViewGroup viewRoot) {
        this.mActRootView = viewRoot;
        checkAddRootView();
        return this;
    }

    private void checkAddRootView() {
        if (mActRootView != null && mContainer != null) {
            mActRootView.removeAllViewsInLayout();
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(480, 272);
            Rect outRect = new Rect();
            mActRootView.getWindowVisibleDisplayFrame(outRect);// 应用界面

            float scaleX = outRect.width() / 480f;
            float scaleY = outRect.height() / 272f;
            float scale = 1f;
            if (scaleX > scaleY) {
                scale = scaleY;
            } else {
                scale = scaleX;
            }
            mContainer.setScaleX(scale);
            mContainer.setScaleY(scale);
            mActRootView.removeAllViews();
            mActRootView.addView(mContainer, params);
        }
    }

    /**
     * 开机显示文本
     *
     * @param noticeText
     */
    public void launchHello(String noticeText) {
        if (mHasBackgroundActive) {
            return;
        }
        showAtImageBottom(noticeText);
        toggleContentView(false);
    }

    /**
     * 判断某个module是不是活跃的module
     * note:是否是活跃是现在的状态，过一段时间就不可靠了，所以如果你的代码依赖于这个判断，并且代码要在
     * 一段时候后才执行。那么这个方法可能会给你带来错误。
     *
     * @param type 要判断的module
     * @return true 活跃
     */
    public boolean isActiveModule(String type) {
        ViewMark mark = currActiveModule();
        LogUtil.logw("isActiveModule, type=" + type + ", mark=" + (mark == null ? "null" : mark.type));
        if (mark != null && mark.type.equals(type) && mark.isShowing) {
            return true;
        }
        return false;
    }

    public boolean isCurrModule(String type) {
        ViewMark mark = currActiveModule();
        LogUtil.logw("isCurrModule, type=" + type + ", mark=" + (mark == null ? "null" : mark.type));
        if (mark != null && mark.type.equals(type)) {
            return true;
        }
        return false;
    }

    /**
     * 执行回退
     */
    private void backViewStack() {
        int backSize = 0;
        synchronized (mBackStacks) {
            if (mBackStacks.size() > 0) {
                mBackStacks.remove(mBackStacks.size() - 1);
            }
            backSize = mBackStacks.size();
        }
        if (backSize <= 0) {
            launchDesktop();
        } else {
            refreshBackStackView();
        }
    }

    /**
     * 刷新当前栈
     */
    private void refreshBackStackView() {
        ViewMark mark = null;
        synchronized (mBackStacks) {
            if (mBackStacks.size() > 0) {
                mark = mBackStacks.get(mBackStacks.size() - 1);
            }
        }
        if (mark != null) {
            displayContentView(mark);
        } else {
            launchDesktop();
        }
    }

    /**
     * 当前显示的信息
     *
     * @return
     */
    private ViewMark currActiveModule() {
        if (mContainer != null && mContainer.isFullScreen()) {
            return null;
        }
        return mCurrActiveViewMark;
    }

    private ViewMark findModuleInCache(String type) {
        for (ViewMark mark : mCaches) {
            if (mark.type.equals(type)) {
                return mark;
            }
        }
        return null;
    }

    public void launchNotification() {
        launchModule(TodayNoticeModule.class, ViewModuleType.TYPE_NOTIFICATION, false,
                IModule.STATUS_FULL, null, null);
    }

    private void launchModule(
            final Class<? extends IModule> moduleClass,
            final String type,
            final boolean addBackStack,
            final int status,
            final ViewLifeCallback callback,
            final String data) {
        LogUtil.logd("launchModule cls:" + moduleClass + ",type:" + type + ",backStack:"
                + addBackStack + ",status:" + status);
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (mContext == null) {
                    return;
                }
                launchModuleInner(moduleClass, type, addBackStack, status, callback, data);
            }
        }, DesktopModule.class.equals(moduleClass) ? 0 : 200);
    }

    private void launchModuleV2(
            final Class<? extends IModule> moduleClass,
            final String type,
            final boolean addBackStack,
            final int status,
            final ViewLifeCallback callback,
            final String data) {
        LogUtil.logd("v2 launchModule cls:" + moduleClass + ",type:" + type + ",backStack:"
                + addBackStack + ",status:" + status);
        Runnable launchRun = new Runnable() {
            @Override
            public void run() {
                if (mContext == null) {
                    return;
                }
                launchModuleInner(moduleClass, type, addBackStack, status, callback, data);
            }
        };
        if (DesktopModule.class.equals(moduleClass)) {
            if (Looper.getMainLooper() == Looper.myLooper()) {
                launchRun.run();
            }else {
                AppLogic.runOnUiGround(launchRun, 0);
            }
        }else {
            AppLogic.runOnUiGround(launchRun, 200);
        }
    }


    private void launchModuleInner(
            Class<? extends IModule> moduleClass,
            String type,
            boolean addBackStack,
            int status,
            ViewLifeCallback callback,
            String data
    ) {
        if (isActiveModule(type)) {
            ViewMark mark = currActiveModule();
            if (mark != null && mark.module != null) {
                mark.module.refreshView(data);
            }
            LogUtil.logd("launchModule isActiveModule！");
            return;
        }

        ViewMark mark = findModuleInCache(type);
        if (mark == null || mark.status != status) {
            mark = createViewMark(type, moduleClass, status, callback, data);
            if (mark != null) {
                addViewMarkToCache(mark);
            }
        } else {
            IModule module = mark.module;
            if (module != null) {
                module.refreshView(data);
            }
        }

        if (mark == null) {
            LogUtil.loge("launchModule create ViewMark fail！");
            return;
        }
        if (addBackStack) {
            addViewMarkToBackStack(mark, false);
        }

        mark.module.notifyIsNavInFocusBeforeDisplay(NavManager.getInstance().isFocus());
        displayContentView(mark);
    }

    private ViewMark mCurrActiveViewMark;

    private void displayContentView(ViewMark mark) {
        if (mark == mCurrActiveViewMark && isLaunchResume()) {
            LogUtil.logd("displayContentView same ViewMark！");
            return;
        }

        mCurrActiveViewMark = mark;
        boolean isDialogWin = isDialogWinShow();

        if (justShowOnDialog(mark)) {
            displayOnDialogWin(mark);
        } else {
            if (isDialogWin) {
                dismissDialogWin();
            }
            if (!isLaunchResume()) {
                launchMain();
            }
            displayOnNormalView(mark);
        }
    }

    // TODO 是否显示在Dialog屏上
    private boolean justShowOnDialog(ViewMark mark) {
        String type = mark.type;
        switch (type) {
            case ViewModuleType.TYPE_TTS_DISPLAY:
                return isDialogWinShow();
            case ViewModuleType.TYPE_WECHAT_MSG_DIALOG:
                return true;
        }
        return false;
    }

    private void displayOnNormalView(ViewMark mark) {
        showSysText("");
        if (!(mark.module instanceof DesktopModule
                && !mHasBackgroundActive)) {
            toggleContentView(true);
        }
        replaceContentView(mark.content, VIEW_TYPE_CONTENT);
    }

    private void displayOnDialogWin(ViewMark mark) {
        if (mDialogWin == null) {
            createDialogWin();
        }
        mDialogWin.open(null);
        mDialogWin.setContentView(mark.content);
    }

    public boolean isDialogWinShow() {
        return mDialogWin != null && mDialogWin.isShowing();
    }

    /**
     * 创建一个View Module
     *
     * @param cls
     * @return
     */
    private ViewMark createViewMark(String type, Class<? extends IModule> cls,
                                    int status, ViewLifeCallback callback, String data) {
        try {
            IModule module = cls.newInstance();
            module.onCreate(data);
            View view = module.onCreateView(mContext, mContainer, status);
            ViewMark mark = new ViewMark();
            mark.type = type;
            mark.module = module;
            mark.content = view;
            mark.isShowing = false;
            mark.callback = callback;
            mark.status = status;
            return mark;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加一个ViewMark到缓存中
     *
     * @param mark
     */
    private void addViewMarkToCache(ViewMark mark) {
        synchronized (mCaches) {
            boolean bFound = false;
            for (ViewMark viewMark : mCaches) {
                if (isSameModule(viewMark, mark)) {
                    bFound = true;
                    mCaches.remove(viewMark);
                    mCaches.add(mark);
                    break;
                }
            }
            if (!bFound) {
                mCaches.add(mark);
            }
        }
    }

    /**
     * 清空回退栈
     */
    public void clearBackStack() {
        synchronized (mBackStacks) {
            mBackStacks.clear();
        }
    }

    /**
     * 添加ViewMark到回退列表
     *
     * @param mark
     */
    private void addViewMarkToBackStack(ViewMark mark, boolean bottom) {
        synchronized (mBackStacks) {
            boolean bFound = false;
            for (ViewMark viewMark : mBackStacks) {
                if (isSameModule(viewMark, mark)) {
                    bFound = true;
                    if (bottom) {
                        int idx = mBackStacks.indexOf(viewMark);
                        mBackStacks.remove(viewMark);
                        mBackStacks.add(idx, mark);
                    } else {
                        mBackStacks.remove(viewMark);
                        mBackStacks.add(mark);
                    }
                    break;
                }
            }
            if (!bFound) {
                if (bottom) {
                    mBackStacks.add(0, mark);
                } else {
                    mBackStacks.add(mark);
                }
            }
        }
    }

    /**
     * 简单通过type判断是否相等
     *
     * @param left
     * @param right
     * @return
     */
    private boolean isSameModule(ViewMark left, ViewMark right) {
        if (left.type.equals(right.type)) {
            return true;
        }
        return false;
    }

    /**
     * 从Stop状态返回
     */
    public void launchMain() {
        ComponentName cName = new ComponentName(mContext.getPackageName(),
                mContext.getPackageName() + ".MainActivity");
        Intent intent = new Intent();
        intent.setComponent(cName);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(GlobalContext.get(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * 返回
     */
    public void launchBack() {
//        if (mTmpViewMark != null) {
//            launchModule(mTmpViewMark.moduleClass, mTmpViewMark.type, mTmpViewMark.addBackStack, mTmpViewMark.status,
//                    mTmpViewMark.callback, mTmpViewMark.data);
//            mTmpViewMark = null;
//            return;
//        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            refreshBackStackView();
        } else {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    refreshBackStackView();
                }
            });
        }
    }

    /**
     * 回退一个View
     */
    public void launchBackWithStack() {
        if (isLaunchResume()) {
            backViewStack();
        }
    }

    /**
     * 返回桌面
     */
    public void launchDesktop() {
        clearBackStack();
        buildBgModule(true);
        if (!mHasBackgroundActive) {
            fullImage();
            onTimeUpdate();
        }
    }

    private void buildBgModule(boolean showImmediate) {
        String actData = checkAndBuildBgModuleData();
        if (actData != null && showImmediate) {
            launchModuleV2(DesktopModule.class, ViewModuleType.TYPE_DESKTOP, false,
                    IModule.STATUS_FULL, null, actData);
        }
        if (!showImmediate) {
            ViewMark desktopModule = findModuleInCache(ViewModuleType.TYPE_DESKTOP);
            if (desktopModule != null) {
                desktopModule.module.refreshView(actData);
            }
        }
    }

    private String checkAndBuildBgModuleData() {
        boolean hasMusic = isMusicWorking();
        boolean hasWx = (mPrivateFlag & FLAG_IS_WX_LOGIN) == FLAG_IS_WX_LOGIN;
        boolean hasNav = (mPrivateFlag & FLAG_IS_NAV_WORKING) == FLAG_IS_NAV_WORKING;
        LogUtil.logd("checkBackgroundActive hasMusic:" + hasMusic
                + ",hasWx:" + hasWx + ",hasNav:" + hasNav + ", mDesktopState=" + mDesktopState);
        mHasBackgroundActive = hasMusic || hasWx || hasNav;
        if (mHasBackgroundActive) {
            return DesktopModule.fromData(mDesktopState);
        }

        return null;
    }

    private ArrayList<String> mDesktopState = new ArrayList<String>();

    private void addDesktopState(int flag) {
        String key;
        if ((key = getDesktopKey(flag)) != null) {
            if (!mDesktopState.contains(key)) {
                mDesktopState.add(key);
            }
        }
    }

    private String getDesktopKey(int flag) {
        String key = null;
        switch (flag) {
            case FLAG_IS_MUSIC_WORKING:
                key = DesktopModule.KEY_MUSIC;
                break;
            case FLAG_IS_WX_LOGIN:
                key = DesktopModule.KEY_WX;
                break;
            case FLAG_IS_NAV_WORKING:
                key = DesktopModule.KEY_NAV;
                break;
        }
        return key;
    }

    private void removeDesktopState(int flag) {
        String key;
        if ((key = getDesktopKey(flag)) != null) {
            mDesktopState.remove(key);
        }
    }

    /**
     * 回到全屏形象界面
     */
    private void fullImage() {
//        ViewMark mark = findModuleInCache(TYPE_DESKTOP);
//        if (mark == null) {
//            mark = new ViewMark();
//            mark.content = mContainer.getChildView(VIEW_TYPE_IMAGE);
//            mark.type = TYPE_DESKTOP;
//            addViewMarkToCache(mark);
//        }
//        mCurrActiveViewMark = mark;
        toggleContentView(false);
        mCurrActiveViewMark = null;
    }

    /**
     * 当前是否在桌面状态
     * 桌面状态：没有加载东西的时候，界面是Desktop的时候，界面全屏的时候（什么都没做，形象在中间的时候；录音窗口打开的时候）
     *
     * @return
     */
    private boolean isDesktop() {
        if (mContainer == null) {
            return true;
        }
        if (isActiveModule(ViewModuleType.TYPE_DESKTOP)) {
            return true;
        }
        if (mContainer.isFullScreen()) {
            return true;
        }
        return false;
    }

    /**
     * 通过自定义界面显示路况
     *
     * @param viewData
     * @return
     */
    public LaunchManager launchTmc(String viewData) {
        launchModule(TmcModule.class, ViewModuleType.TYPE_TMC, false, IModule.STATUS_FULL, null, viewData);
        return this;
    }

    public LaunchManager launchPoiIssued() {
        launchModule(PoiIssuedModule.class, ViewModuleType.TYPE_POIISSUED, true, IModule.STATUS_FULL, new ViewLifeCallback() {
            @Override
            public void onResume() {
            }

            @Override
            public void onPreRemove() {
                mContainer.recoverDefaultPadding();
            }
        }, null);
        return this;
    }
    //********************************************************************************//
    //*******************************声控界面相关开始**********************************//
    //********************************************************************************//

    public LaunchManager launchWeather(String data) {
        launchModule(ChatWeatherModule.class, ViewModuleType.TYPE_CHAT_WEATHER, false, IModule.STATUS_FULL, null, data);
        return this;
    }

    public LaunchManager launchStock(String data) {
        launchModule(ChatStockModule.class, ViewModuleType.TYPE_CHAT_STOCK, false, IModule.STATUS_FULL, null, data);
        return this;
    }

    public LaunchManager launchList(String data) {
        try {
            JSONBuilder jsData = new JSONBuilder(data);
            if (jsData.getVal("count", Integer.class, -1) == 0) {
                String tts = jsData.getVal("tts", String.class, null);
                if (tts != null) {
                    // 当有dialog的时候要展示在dialog上；
                    if (isDialogWinShow()) {
                        launchTts(tts);
                    }else  if (isShowAtImage(tts)){// 根据判断可以展示在小欧下面就展示在小欧下面
                        showAtImageBottom(tts);
                    } else {// 在TtsModule的时候，展示在TtsModule上
                        launchTts(tts);
                    }
                }
                return this;
            }
        } catch (Exception e) {
        }
        launchModule(ChatListModule.class, ViewModuleType.TYPE_CHAT_LIST, false, IModule.STATUS_FULL, null, data);
        return this;
    }

    //********************************************************************************//
    //*******************************声控界面相关结束**********************************//
    //********************************************************************************//

    public boolean isActiveWechatQr() {
        return isActiveModule(ViewModuleType.TYPE_WECHAT_QR_MODULE);
    }

    public boolean isActiveWechatRecord() {
        return isActiveModule(ViewModuleType.TYPE_WECHAT_RECORD_MODULE);
    }

    private class ViewMarkInfo {
        public String type;
        public Class<? extends IModule> moduleClass;
        public boolean addBackStack;
        public int status;
        public ViewLifeCallback callback;
        public String data;
    }

//    private ViewMarkInfo mTmpViewMark;

    public LaunchManager launchWechatQr(String data) {
//        if ((mPrivateFlag & FLAG_IS_VOICE_OPEN) == FLAG_IS_VOICE_OPEN) {
//            mTmpViewMark = new ViewMarkInfo();
//            mTmpViewMark.type = ViewModuleType.TYPE_WECHAT_QR_MODULE;
//            mTmpViewMark.moduleClass = WechatQrModule.class;
//            mTmpViewMark.addBackStack = false;
//            mTmpViewMark.status = IModule.STATUS_FULL;
//            mTmpViewMark.callback = null;
//            mTmpViewMark.data = data;
//            return this;
//        }
        launchModule(WechatQrModule.class, ViewModuleType.TYPE_WECHAT_QR_MODULE, false, IModule.STATUS_FULL, null, data);
        return this;
    }

    public LaunchManager launchWechatRecord(String data) {
        launchModule(WechatRecordModule.class, ViewModuleType.TYPE_WECHAT_RECORD_MODULE, false, IModule.STATUS_FULL, null, data);
        return this;
    }

    public boolean launchWechatDialog(WechatMsgData wechatMsgData) {
        if (isLaunchResume()) {
            return false;
        } else {
            launchModule(WechatDialogModule.class, ViewModuleType.TYPE_WECHAT_MSG_DIALOG, false, IModule.STATUS_DIALOG, null, wechatMsgData.toJsonString());
        }
        return true;
    }

    public LaunchManager launchLoginModule(String data) {
        launchModule(LoginModule.class, ViewModuleType.TYPE_LOGIN, false, IModule.STATUS_FULL, null, data);
        return this;
    }

    public LaunchManager launchWechatBindModule() {
        launchModule(WechatBindModule.class, ViewModuleType.TYPE_WECHAT_BIND, false, IModule.STATUS_FULL, null, null);
        return this;
    }

    public LaunchManager launchAPModule(String data) {
        launchModule(APModule.class, ViewModuleType.TYPE_SETTINGS_AP, false, IModule.STATUS_FULL, null, data);
        return this;
    }

    // 启动系统信息
    public LaunchManager launchSystemInfoModule() {
        launchModule(SystemInfoModule.class, ViewModuleType.TYPE_SETTINGS_SYS_INFO, false, IModule.STATUS_FULL, null, null);
        return this;
    }

    public LaunchManager launchAppInfoModule() {
        launchModule(SystemAppInfoModule.class, ViewModuleType.TYPE_SETTINGS_APP_INFO, false, IModule.STATUS_FULL, null, null);
        return this;
    }

    // 帮助界面
    public LaunchManager launchHelpModule() {
        launchModule(HelpModule.class, ViewModuleType.TYPE_HELP, false, IModule.STATUS_FULL, null, null);
        return this;
    }

    public boolean dismissNotify() {
        if (isLaunchResume()) {
            return false;
        } else if (isActivityWechatMsgDialog()) {
            dismissDialogWin();
            return true;
        }
        return true;
    }

    public boolean isActivityWechatMsgDialog() {
        if (mCurrActiveViewMark != null && mCurrActiveViewMark.type.equals(ViewModuleType.TYPE_WECHAT_MSG_DIALOG) && mCurrActiveViewMark.isShowing) {
            return true;
        }
        return false;
    }


    /**
     * 显示文本
     *
     * @param ttsText
     */
    public void addSystemText(final String ttsText) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            showSysText(ttsText);
        } else {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    showSysText(ttsText);
                }
            });
        }

    }

    /**
     * 更新声控状态
     *
     * @param state
     */
    public void updateState(final int state) {
        if (mDialogWin != null && mDialogWin.isShowing()) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                mDialogWin.updateState(state);
            } else {
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        mDialogWin.updateState(state);
                    }
                });
            }
//            return;
        }

        if (mContainer == null || mContainer.getChildView(MainContainer.VIEW_TYPE_IMAGE) == null) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            IImage image = (IImage) mContainer.getChildView(MainContainer.VIEW_TYPE_IMAGE);
            image.updateState(state);
        } else {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    IImage image = (IImage) mContainer.getChildView(MainContainer.VIEW_TYPE_IMAGE);
                    image.updateState(state);
                }
            });
        }
    }

    /**
     * 创建对话框聊天界面
     */
    private void createDialogWin() {
        if (mDialogWin != null) {
            mDialogWin.dismiss();
            mDialogWin.setContainerCallback(null);
            mDialogWin = null;
        }

        mDialogWin = new DialogRecordWin(mContext);
        mDialogWin.setContainerCallback(mCallback);
        mDialogWin.setImageView(new SQImageLittle(mContext));
    }

    public void dismissDialogWin() {
        if (mDialogWin != null && mDialogWin.isShowing()) {
            mDialogWin.dismiss();
        }
    }

    private String mTtsText = null;

    /**
     * 显示文本信息，如果全屏并且字数可显示完，则显示在形象下方，否则开一个新的界面
     *
     * @param text
     */
    private void showSysText(String text) {
        if (!needShowText()) {
            LogUtil.logd("showSysText no show！");
            return;
        }

        if (!TextUtils.isEmpty(text)) {
            text = text.replaceAll("小欧小欧", "小O小O");
        }

        boolean isShowAtImage = isShowAtImage(text);
        LogUtil.logd("showSysText isShowAtImage:" + isShowAtImage + ",text:" + text);
        if (!isShowAtImage) {
            showAtImageBottom("");
            launchTts(text);
        } else {
            showAtImageBottom(text);
            if (!mContainer.isFullScreen()) {
                mContainer.fullScreen();
                if (!TextUtils.isEmpty(text)) { // 修正微信录音闪动，录音取消后界面不消失问题
                    mCurrActiveViewMark = null; // 修正增大音量界面不退出问题
                }
            }
        }
    }

    /**
     * 如果当前Launcher不在前台，并且没有显示对话框，则忽略文本
     *
     * @return
     */
    private boolean needShowText() {
        if (!isLaunchResume() && !isDialogWinShow()) {
            return false;
        }
        return true;
    }

    public void launchTts(String text) {
        launchModule(ChatTtsModule.class, ViewModuleType.TYPE_TTS_DISPLAY, false,
                isDialogWinShow() ? IModule.STATUS_DIALOG : IModule.STATUS_FULL, null, text);
    }

    /**
     * 是否直接显示在形象下方
     *
     * @return
     */
    private boolean isShowAtImage(String showText) {
        if (TextUtils.isEmpty(showText)) {
            return true;
        }
        ViewMark mark = currActiveModule();
        // 如果当前是新开的TTS显示界面，则直接刷新文本
        if (mark != null && ViewModuleType.TYPE_TTS_DISPLAY.equals(mark.type)) {
            return false;
        }
        if (isLaunchResume()) {
            int len = showText.length();
            if (showText.contains("</span>")) {
                return false;
            }
            return len <= 18;
        }
        return false;
    }

    public boolean isLaunchResume() {
        return (mPrivateFlag & FLAG_IS_FOREGROUND) == FLAG_IS_FOREGROUND;
    }

    public void showAtImageBottom(String text) {
        showTextAtImage(text);
    }

    private void showTextAtImage(String text) {
        if (mContainer == null || mContainer.getChildView(MainContainer.VIEW_TYPE_IMAGE) == null) {
            return;
        }
        IImage image = (IImage) mContainer.getChildView(MainContainer.VIEW_TYPE_IMAGE);
        image.showDescText(text);
    }

    /**
     * 时间更新
     */
    public void onTimeUpdate(boolean force) {
        // 强制不允许刷新
        LogUtil.logd("isCurrModule=TYPE_LOGIN , " + (isCurrModule(ViewModuleType.TYPE_LOGIN)) + ", enable=" + enableTimeShow() + ", force=" + force);
        if (isCurrModule(ViewModuleType.TYPE_LOGIN)) { // 登录界面下
            return;
        }

        if (enableTimeShow() || force) {
            SimpleDateFormat sdf;
            if (DateUtils.is24HourFormat(GlobalContext.get())) {
                sdf = new SimpleDateFormat("HH:mm");
            } else {
                sdf = new SimpleDateFormat("hh:mm");
            }
            Date date = new Date(System.currentTimeMillis());
            String currStr = sdf.format(date);
            LogUtil.logd("onTimeUpdate currStr:" + currStr);
//            addSystemText(currStr);
            showAtImageBottom(currStr);
        }
    }

    public void onTimeUpdate() {
        onTimeUpdate(false);
    }


    private boolean enableFreshTime = true;

    /**
     * 允许时间更新
     *
     * @param enable
     * @return
     */
    public LaunchManager enableTime(boolean enable) {
        return enableTime(enable, false);
    }

    public LaunchManager enableTime(boolean enable, boolean force) {
        this.enableFreshTime = enable;
        if (this.enableFreshTime || force) {
            onTimeUpdate(force);
        }
        return this;
    }

    /**
     * 是否允许时间更新显示
     *
     * @return
     */
    private boolean enableTimeShow() {
        // 界面上没有组件的时候，没办法显示时间
        if (mContainer == null) {
            return false;
        }
        // 如果设置了不更新时间，这里就不更新
        if (!enableFreshTime) {
            return false;
        }
        // 录音界面打开着的时候，也不能更新时间
        if ((mPrivateFlag & FLAG_IS_VOICE_OPEN) != 0) {
            return false;
        }
        // TODO: 2018/8/17 这个是什么
        if (mHasBackgroundActive) {
            return false;
        }
        // 产品没有定义非全屏时时间的展示，所以不更新时间
        if (!mContainer.isFullScreen()) {
            return false;
        }
        // 展示开机欢迎语的时候不更新时间
        if (BootStrapManager.getInstance().isDisableRefreshTime()) {
            return false;
        }
        return true;
    }

    public LaunchManager toggleContentView(boolean isShowContent) {
        if (mContainer == null) {
            return this;
        }
        if (mContainer.isFullScreen() == !isShowContent) {
            LogUtil.logd("toggleContentView same mode");
            return this;
        }
        if (isShowContent) {
            mContainer.setViewGroupBackground(R.drawable.half_image_bg);
            mContainer.halfScreen();
        } else {
            mContainer.setViewGroupBackground(R.drawable.full_image_bg);
            mContainer.fullScreen();
        }
        return this;
    }

    private void replaceContentView(View objView, int type) {
        if (mContainer == null) {
            initContainer();
        }

        LogUtil.logd("replaceContentView objView:" + objView + ",type:" + type);
        switch (type) {
            case VIEW_TYPE_CONTENT:
                mContainer.setContentView(objView);
                break;
            case MainContainer.VIEW_TYPE_IMAGE:
                mContainer.setImageView(objView);
                break;
            case MainContainer.VIEW_TYPE_TIPS:
//                mContainer.setTipView(objView);
                break;
        }
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{
                EventTypes.EVENT_NAV_START_NAVI,
                EventTypes.EVENT_NAV_END_NAVI,
                EventTypes.EVENT_MUSIC_OPEN,
                EventTypes.EVENT_MUSIC_PLAYING,
                EventTypes.EVENT_MUSIC_FAIL,
                EventTypes.EVENT_MUSIC_EXIT,
                EventTypes.EVENT_WX_LOGIN,
                EventTypes.EVENT_WX_LOGOUT,
                EventTypes.EVENT_VOICE_OPEN,
                EventTypes.EVENT_VOICE_DISMISS,
                EventTypes.EVENT_TIME_CHANGE,
                EventTypes.EVENT_LAUNCH_ONRESUME,
                EventTypes.EVENT_LAUNCH_ONSTOP,
                EventTypes.EVENT_VOIP_CALLING,
                EventTypes.EVENT_VOIP_READY,
                EventTypes.EVENT_DEVICE_RECORY_FACTORY,
                EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP,
                EventTypes.EVENT_VOIP_BEFORE_READY
        };
    }

    @Override
    protected void onEvent(String eventType) {
        if (EventTypes.EVENT_NAV_START_NAVI.equals(eventType)) {
            mPrivateFlag |= FLAG_IS_NAV_WORKING;
            addDesktopState(FLAG_IS_NAV_WORKING);
            if (isLaunchResume()) {
                onDesktopUpdate();
            }
        } else if (EventTypes.EVENT_NAV_END_NAVI.equals(eventType)) {
            mPrivateFlag &= ~FLAG_IS_NAV_WORKING;
            removeDesktopState(FLAG_IS_NAV_WORKING);
            onDesktopUpdate();
        } else if (EventTypes.EVENT_MUSIC_PLAYING.equals(eventType) || EventTypes.EVENT_MUSIC_OPEN.equals(eventType)) {
            mPrivateFlag |= FLAG_IS_MUSIC_WORKING;
            addDesktopState(FLAG_IS_MUSIC_WORKING);
            onDesktopUpdate();
        } else if (EventTypes.EVENT_MUSIC_FAIL.equals(eventType)) {
            mPrivateFlag |= FLAG_IS_MUSIC_WORKING;
            addDesktopState(FLAG_IS_MUSIC_WORKING);
            if (RecordWinManager.getInstance().isRecordWinClosed()) {
                onDesktopUpdate();
            }
        } else if (EventTypes.EVENT_MUSIC_EXIT.equals(eventType)) {
            if ((mPrivateFlag & FLAG_IS_MUSIC_WORKING) != 0) {
                mPrivateFlag &= ~FLAG_IS_MUSIC_WORKING;
                removeDesktopState(FLAG_IS_MUSIC_WORKING);
                onDesktopUpdate();
            }
        } else if (EventTypes.EVENT_WX_LOGIN.equals(eventType)) {
            mPrivateFlag |= FLAG_IS_WX_LOGIN;
            addDesktopState(FLAG_IS_WX_LOGIN);
            onDesktopUpdate();
        } else if (EventTypes.EVENT_WX_LOGOUT.equals(eventType)) {
            mPrivateFlag &= ~FLAG_IS_WX_LOGIN;
            removeDesktopState(FLAG_IS_WX_LOGIN);
            onDesktopUpdate();
        } else if (EventTypes.EVENT_VOICE_OPEN.equals(eventType)) {
            mPrivateFlag |= FLAG_IS_VOICE_OPEN;
            onVoiceOpen();
        } else if (EventTypes.EVENT_VOICE_DISMISS.equals(eventType)) {
            mPrivateFlag &= ~FLAG_IS_VOICE_OPEN;
            onVoiceClose();
        } else if (EventTypes.EVENT_TIME_CHANGE.equals(eventType)) {
            onTimeUpdate();
        } else if (EventTypes.EVENT_LAUNCH_ONRESUME.equals(eventType)) {
            mPrivateFlag |= FLAG_IS_FOREGROUND;
            LogUtil.logd("EVENT_LAUNCH_ONRESUME");
            onDesktopUpdate();
        } else if (EventTypes.EVENT_LAUNCH_ONSTOP.equals(eventType)) {
            mPrivateFlag &= ~FLAG_IS_FOREGROUND;
            LogUtil.logd("EVENT_LAUNCH_ONSTOP");
            if (!isDialogWinShow()) {
                launchBack();
            }
        } else if (EventTypes.EVENT_VOIP_CALLING.equals(eventType)) {
            /*
                voip呼叫中的时候覆盖会悬浮覆盖launcher界面
                需求在voip挂断的时候返回主界面
              */
            if (isActiveModule(ViewModuleType.TYPE_LOGIN)) {
                bReturnLoginAfterCall = true;
            }
            launchBackWithStack();
        } else if (EventTypes.EVENT_VOIP_BEFORE_READY.equals(eventType)) {
            if (bReturnLoginAfterCall) {
                bReturnLoginAfterCall = false;
                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put(LoginModule.PARAMS_RETURN_LOGIN_AFTER_CALL, true);
                launchLoginModule(jsonBuilder.toString());
            } else if (!PreferenceUtil.getInstance().getBoolean(PreferenceUtil.KEY_ANJIXING_LOGIN, false)) {
                /*
                 * 安吉星未登录的情况下挂断电话返回安吉星登录界面
                 * 这个是为了开机流程的登录之前用户打电话后，也让用户去登录。
                 */
                launchLoginModule(null);
            }
        } else if (EventTypes.EVENT_DEVICE_RECORY_FACTORY.equals(eventType)) {
            if (!isCurrModule(ViewModuleType.TYPE_LOGIN)) {
                launchBackWithStack();
            }
        } else if (EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP.equals(eventType)) {
            launchBackWithStack();
            showAtImageBottom("");
            onTimeUpdate(true);
            LaunchManager.getInstance().refreshSignalIcon(-1);
            LaunchManager.getInstance().refreshSignalText("");
            updateState(IImage.STATE_TTS_END);
            updateState(IImage.STATE_NORMAL);
        }
    }

    private boolean bReturnLoginAfterCall = false; // 当通话结束后，是否需要返回桌面


    private void onVoiceOpen() {
        SettingsManager.getInstance().ctrlScreen(true);
        if (!isLaunchResume() && !GuideManager.getInstance().isGuideActive()) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                showDialog();
            } else {
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        showDialog();
                    }
                });
            }
        } else {
            fullImage();
        }
    }

    private void showDialog() {
        if (mDialogWin == null) {
            createDialogWin();
        } else {
            mDialogWin.dismiss();
        }
        mDialogWin.open(mTtsText);
    }

    private void onVoiceClose() {
        if (mDialogWin != null && mDialogWin.isShowing()) {
            mDialogWin.dismiss();
//            if (mTmpViewMark != null) {
//                launchModule(mTmpViewMark.moduleClass, mTmpViewMark.type, mTmpViewMark.addBackStack, mTmpViewMark.status,
//                        mTmpViewMark.callback, mTmpViewMark.data);
//                mTmpViewMark = null;
//                return;
//            }
            return;
        }

        onTimeUpdate();

        /*
         * created by daviddai at 2018/9/25 14:18
         * 当与导航交互时，进入导航app可能比录音界面close来得快，这时候会由于launcher不在前台而没有清除展示的文字。因此这里将isLaunchResume()的条件去掉
         */
        if (/*isLaunchResume() &&*/ mContainer != null && !mContainer.isFullScreen()
                && !isCurrModule(ViewModuleType.TYPE_LOGIN)) { // 非全屏状态下，退出声控时复位文本
            showAtImageBottom("");
        }
    }

    private boolean mHasBackgroundActive = false;

    public boolean isMusicWorking() {
        return (mPrivateFlag & FLAG_IS_MUSIC_WORKING) == FLAG_IS_MUSIC_WORKING;
    }

    private void onDesktopUpdate() {
        if (isLaunchResume()) {
            AppLogic.removeUiGroundCallback(onDesktopUpdateRunnable);
            AppLogic.runOnUiGround(onDesktopUpdateRunnable, 20);
        }
    }

    private Runnable onDesktopUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            boolean hasMusic = isMusicWorking();
            boolean hasWx = (mPrivateFlag & FLAG_IS_WX_LOGIN) == FLAG_IS_WX_LOGIN;
            boolean hasNav = (mPrivateFlag & FLAG_IS_NAV_WORKING) == FLAG_IS_NAV_WORKING;
            mHasBackgroundActive = hasMusic || hasWx || hasNav;
            LogUtil.logd("checkStateFlag:" + mPrivateFlag + ",mHasBackgroundActive:" + mHasBackgroundActive);

            boolean isDesktop = isDesktop();
            boolean isRecordWinOpen = TXZRecordWinManager.getInstance().isOpened();
            LogUtil.logd("onDesktopUpdate isDesktop=" + isDesktop + ", mHasBackgroundActive=" + mHasBackgroundActive
                    + ", isRecordWinOpen=" + isRecordWinOpen);
            if (mHasBackgroundActive) {
                if (isDesktop && !isRecordWinOpen) { // when is at desktop and record win is closed,launch desktop layout
                    /*
                     * 这里需要用v2版本的launchModule方法。这是因为如果打开桌面不是马上执行的，而判断是否是桌面是马上执行的。
                     * 那就会有要打开桌面的时候已经不是桌面了的情况。这种情况下界面会被替换成桌面。而按逻辑来说应该是走下面那个方法的。
                     */
                    buildBgModule(true);
                } else {
                    buildBgModule(false);
                }
            } else if (isDesktop) {
                ViewMark desktopModule = findModuleInCache(TYPE_DESKTOP);
                if (desktopModule != null && desktopModule.module != null) {
                    desktopModule.module.onDestroy();
                }
                fullImage();
            }
        }
    };


    /*******************************************************************
     *************************** 更新状态栏******************************
     *******************************************************************/
    /**
     * 刷新信号强度
     *
     * @param signal
     */
    public void refreshSignalIcon(final int signal) {
        if (mContainer!=null) {
            StatusBar statusBar = (StatusBar) mContainer.getChildView(ViewContainer.VIEW_TYPE_STATUS_BAR);
            if (statusBar!=null) {
                statusBar.refreshSignalIcon(signal);
            }
        }
    }

    /**
     * 刷新网络类型
     *
     * @param signal
     */
    public void refreshSignalText(final String signal) {
        if (mContainer!=null) {
            StatusBar statusBar = (StatusBar) mContainer.getChildView(ViewContainer.VIEW_TYPE_STATUS_BAR);
            if (statusBar!=null) {
                statusBar.refreshSignalText(signal);
            }
        }
    }

    /**
     * 刷新定位开启状态
     *
     * @param isOpen
     */
    public void refreshLocation(final boolean isOpen) {
        if (mContainer!=null) {
            StatusBar statusBar = (StatusBar) mContainer.getChildView(ViewContainer.VIEW_TYPE_STATUS_BAR);
            if (statusBar!=null) {
                statusBar.refreshLocation(isOpen);
            }
        }
    }

    /**
     * 刷新热点状态
     *
     * @param isOpen
     */
    public void refreshAP(final boolean isOpen) {
        if (mContainer!=null) {
            StatusBar statusBar = (StatusBar) mContainer.getChildView(ViewContainer.VIEW_TYPE_STATUS_BAR);
            if (statusBar!=null) {
                statusBar.refreshAP(isOpen);
            }
        }
    }

    /**
     * 刷新FM状态
     *
     * @param isOpen
     */
    public void refreshFM(final boolean isOpen) {
        if (mContainer!=null) {
            StatusBar statusBar = (StatusBar) mContainer.getChildView(ViewContainer.VIEW_TYPE_STATUS_BAR);
            if (statusBar!=null) {
                statusBar.refreshFM(isOpen);
            }
        }
    }
}