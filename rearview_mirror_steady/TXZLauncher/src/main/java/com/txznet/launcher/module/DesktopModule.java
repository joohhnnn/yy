package com.txznet.launcher.module;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.launcher.R;
import com.txznet.launcher.module.music.MusicModule;
import com.txznet.launcher.module.nav.HUDModule;
import com.txznet.launcher.module.wechat.WechatModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by TXZ-METEORLUO on 2018/3/26.
 * 桌面界面。部分界面是显示在一起的，如音乐、微信和导航，将他们一起显示的这个界面我们叫做桌面。
 * 实际上，桌面是界面的content的一种，而音乐、微信和导航不是content，是桌面界面的一部分。
 */

public class DesktopModule extends BaseModule {
    private String mMDString = "";
    private String mLastMDString = "";
    private List<IModule> mStateTmpList = new ArrayList<>();
    private Map<String, IModule> stateModuleMap = new HashMap<>();

    private Context mContext;
    private ViewGroup mContentView;
    private String mCurrStateData;

    public static final String KEY_MUSIC = "music";
    public static final String KEY_WX = "wx";
    public static final String KEY_NAV = "nav";

    public static String fromData(ArrayList<String> mStates) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mStates.size(); i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(mStates.get(i));
        }
        return sb.toString();
    }

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        mCurrStateData = data;
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        this.mContext = context;
        return mContentView = createContentView(context);
    }

    private ViewGroup createContentView(Context context) {
        FrameLayout container = new FrameLayout(context);
        LayoutAnimationController animationController = new LayoutAnimationController(AnimationUtils.loadAnimation(context, R.anim.anim_fade_scale_down_in));
        animationController.setOrder(LayoutAnimationController.ORDER_NORMAL);
        animationController.setDelay(0.3f);
        container.setLayoutAnimation(animationController);
        return container;
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        refreshCard(data);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mCurrStateData)) {
            refreshCard(mCurrStateData);
            mCurrStateData = null;
        }
        for (IModule module:mStateTmpList) {
            if (module != null) {
                module.onResume();
            }
        }
    }

    @Override
    public void onPreRemove() {
        super.onPreRemove();
        for (IModule module:mStateTmpList) {
            if (module != null) {
                module.onPreRemove();
            }
        }
    }

    private void refreshCard(String stateStr) {
        String[] moduleKeys = stateStr.split(",");

        View view = createView(moduleKeys);
        if (view != null) {
            mContentView.removeAllViews();
            mContentView.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private View createView(String[] moduleKeys) {
        View view = null;

        mStateTmpList.clear();
        mMDString = "";
        ArrayList<String> tmpKeys = new ArrayList<String>();
        tmpKeys.add(KEY_MUSIC);
        tmpKeys.add(KEY_NAV);
        tmpKeys.add(KEY_WX);
        //处理存在的module
        for (String key:moduleKeys) {
            tmpKeys.remove(key);
            prepareModule(key, true, stateModuleMap.get(key), null);
        }

        for (String key: tmpKeys) {
            prepareModule(key, false, stateModuleMap.get(key), null);
        }

        tmpKeys.clear();

        LogUtil.loge("mMDString:"+mMDString + ";last:"+mLastMDString);

        boolean needReCreate = !mMDString.equals(mLastMDString);
        if (needReCreate) { // 需要重新排版卡片
            view = genCardStateLayoutView(mStateTmpList);
        }
        mLastMDString = mMDString;
        return view;
    }

    private void prepareModule(String key, boolean has, IModule module, String data) {
        if (has) {
            mMDString += key;
            // 如果之前没有对应的卡片就生成。如果有了不能生成。
            if (module == null) {
                if (KEY_MUSIC.equals(key)) {
                    module = new MusicModule();
                } else if (KEY_WX.equals(key)) {
                    module = new WechatModule();
                } else if (KEY_NAV.equals(key)) {
                    module = new HUDModule();
                }

                if (module != null) {
                    module.onCreate(data);
                    stateModuleMap.put(key, module);
                }
            }
            if (module != null) {
                mStateTmpList.add(module);
            }
        } else {
            mMDString = mMDString.replace(key, "");
            if (module != null) {
                module.onDestroy();
            }
            stateModuleMap.remove(key);
        }
    }

    /**
     * 生成卡片的View
     *
     * @return
     */
    private View genCardStateLayoutView(List<IModule> createList) {
        int size = createList.size();
        if (size > 0) {
            int status = size;
            LinearLayout layout = new LinearLayout(mContext);
            layout.setOrientation(LinearLayout.VERTICAL);
            for (int i = 0; i < createList.size(); i++) {
                IModule module = createList.get(i);
                View view = module.onCreateView(mContext, layout, status);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
                params.weight = 1;
                if (i != 0) {
                    params.topMargin = mContext.getResources().getDimensionPixelSize(R.dimen.dimen_notice_card_offset);
                }
                layout.addView(view, params);
            }
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layout.setLayoutParams(params);
            return layout;
        }
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stateModuleMap.clear();
        mLastMDString = "";
        mMDString = "";
    }
}