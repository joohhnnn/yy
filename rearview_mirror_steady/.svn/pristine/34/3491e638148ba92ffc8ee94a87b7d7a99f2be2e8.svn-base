package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ScrollView;

import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.skin.SK;
import com.txznet.comm.ui.theme.test.smarthandyhome.HomeHelpHolder;
import com.txznet.comm.ui.theme.test.smarthandyhome.HomeLogoHolder;
import com.txznet.comm.ui.theme.test.smarthandyhome.HomeMusicHolder;
import com.txznet.comm.ui.theme.test.smarthandyhome.HomeNavHolder;
import com.txznet.comm.ui.theme.test.smarthandyhome.HomeNewFunctionHolder;
import com.txznet.comm.ui.theme.test.smarthandyhome.HomeRemindHolder;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.SmartHandyHomeViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ISmartHandyHomeView;
import com.txznet.resholder.R;

import java.util.HashMap;

/**
 * 说明：
 *
 * @author xiaolin
 * create at 2020-11-06 20:04
 */
public class SmartHandyHomeView extends ISmartHandyHomeView {

    private static SmartHandyHomeView instance = new SmartHandyHomeView();
    public static SmartHandyHomeView getInstance(){
        return instance;
    }

    private View mRootView;
    private ScrollView scrollView;
    private ViewGroup mItemContainer;
    private ImageView ivLogo;

    private HomeNavHolder navHolder = HomeNavHolder.getInstance();
    private HomeMusicHolder musicHolder = HomeMusicHolder.getInstance();
    private HomeNewFunctionHolder newsHolder = HomeNewFunctionHolder.getInstance();
    private HomeRemindHolder remindHolder = HomeRemindHolder.getInstance();
    private HomeLogoHolder logoHolder = HomeLogoHolder.getInstance();
    private HomeHelpHolder helpHolder = HomeHelpHolder.getInstance();

    @Override
    public void init() {
        Context context = UIResLoader.getInstance().getModifyContext();
        mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_home_container, (ViewGroup) null);
        scrollView = mRootView.findViewById(R.id.scrollView);
        mItemContainer = mRootView.findViewById(R.id.itemContainer);
        ivLogo = mRootView.findViewById(R.id.ivLogo);

        ivLogo.setImageDrawable(SK.getDrawable(SK.DRAWABLE.smart_handy_logo));

        mItemContainer.addView(navHolder.getView());
        mItemContainer.addView(musicHolder.getView());
        mItemContainer.addView(newsHolder.getView());
        mItemContainer.addView(remindHolder.getView());
        mItemContainer.addView(logoHolder.getView());
        mItemContainer.addView(helpHolder.getView());
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData viewData) {
        SmartHandyHomeViewData data = (SmartHandyHomeViewData) viewData;
        updateView(data);

//        scrollView.scrollTo(0, 0);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = viewData.getType();
        viewAdapter.view = mRootView;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.object = getInstance();
        return viewAdapter;
    }

    @Override
    public Object updateView(ViewData viewData) {
        SmartHandyHomeViewData data = (SmartHandyHomeViewData) viewData;
        HashMap<Integer, SmartHandyHomeViewData.DataBase> map = data.homeItemDataHashMap;

        navHolder.update((SmartHandyHomeViewData.NavData) map.get(SmartHandyHomeViewData.DataBase.TYPE_NAV));
        musicHolder.update((SmartHandyHomeViewData.MusicData) map.get(SmartHandyHomeViewData.DataBase.TYPE_MUSIC));
        newsHolder.update((SmartHandyHomeViewData.NewsData) map.get(SmartHandyHomeViewData.DataBase.TYPE_NEWS));
        remindHolder.update((SmartHandyHomeViewData.ReminderData) map.get(SmartHandyHomeViewData.DataBase.TYPE_REMINDER));
        logoHolder.update((SmartHandyHomeViewData.LogoData) map.get(SmartHandyHomeViewData.DataBase.TYPE_LOGO));
        helpHolder.update((SmartHandyHomeViewData.HelpData) map.get(SmartHandyHomeViewData.DataBase.TYPE_HELP));

        return super.updateView(data);
    }

    /**
     * 开启进入动画
     */
    public void startAnimEnter(){
        Context context = UIResLoader.getInstance().getModifyContext();
        LayoutAnimationController animationController = AnimationUtils.loadLayoutAnimation(context, R.anim.smart_handy_layout_in);
        ViewGroup itemContainer = mRootView.findViewById(R.id.itemContainer);
        itemContainer.setLayoutAnimation(animationController);
    }
}
