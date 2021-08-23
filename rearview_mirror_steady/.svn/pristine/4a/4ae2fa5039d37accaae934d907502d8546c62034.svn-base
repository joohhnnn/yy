package com.txznet.comm.ui.theme.test.winlayout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.layout.ISmartHandyLayout;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.anim.SimpleAnimationListener;
import com.txznet.comm.ui.theme.test.anim.SimpleOnClickListener;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.view.SmartHandyHomeView;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.loader.AppLogicBase;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZSmartHandyManager;

import java.util.Locale;

/**
 * 说明：智能捷径
 *
 * @author xiaolin
 * create at 2020-11-04 14:56
 */
public class SmartHandyLayout extends ISmartHandyLayout {

    private static final String TAG = "SmartHandyLayout";

    public static SmartHandyLayout instance = new SmartHandyLayout();

    public static SmartHandyLayout getInstance() {
        return instance;
    }

    private ViewGroup mRootView;
    private FrameLayout mContainer;

    // 是否显示开机动画，开机第一次显示动画过程不允许关闭窗口
    private boolean isShowEnterAnim = true;
    private ViewFactory.ViewAdapter lastViewAdapter;

    @Override
    public void init() {
        Context context = UIResLoader.getInstance().getModifyContext();
        mRootView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.smart_handy_layout, (ViewGroup) null);
        mContainer = mRootView.findViewById(R.id.container);

        // 点击空白处关闭窗口
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShowEnterAnim) {
                    Context context = UIResLoader.getInstance().getModifyContext();
                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.smart_handy_container_out);
                    animation.setAnimationListener(new SimpleAnimationListener() {
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            TXZSmartHandyManager.getInstance().close();
                        }
                    });
                    mContainer.startAnimation(animation);
                }
            }
        });
        mContainer.setOnClickListener(new SimpleOnClickListener());

    }

    @Override
    public ViewGroup get() {
        return mRootView;
    }

    @Override
    public void updateView(String json) {
        LogUtil.d(TAG, "SmartHandy updateView():" + json);

    }

    @Override
    public void show() {
        super.show();
        LogUtil.d(TAG, "SmartHandy show()");
        isShowEnterAnim = false;
        lastViewAdapter = null;

        Context context = UIResLoader.getInstance().getModifyContext();
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.smart_handy_container_in);
        mContainer.startAnimation(animation);
    }

    /**
     * 开机第一次显示
     */
    @Override
    public void firstShow() {
        isShowEnterAnim = true;
        lastViewAdapter = null;
        AppLogicBase.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                isShowEnterAnim = false;
                showHomeView();
            }
        }, 3000);

        Context context = UIResLoader.getInstance().getModifyContext();
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.smart_handy_container_in);
        mContainer.startAnimation(animation);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        LogUtil.d(TAG, "SmartHandy dismiss()");
    }

    @Override
    public void addView(ViewFactory.ViewAdapter viewAdapter) {
        LogUtil.d(TAG, String.format(Locale.getDefault(),
                "SmartHandy addView type:%d", viewAdapter.type));
        if (isShowEnterAnim) {// 显示开机动画期间
            if (viewAdapter.type == ViewData.TYPE_SMART_HANDY_POWER_ON_LOGO) {
                mContainer.removeAllViews();
                mContainer.addView(viewAdapter.view);
            }
            lastViewAdapter = viewAdapter;
            return;
        }

        /* 从主页跳转到详情页 */
        if (lastViewAdapter != null && lastViewAdapter.type == ViewData.TYPE_SMART_HANDY_HOME_VIEW) {
            Context context = UIResLoader.getInstance().getModifyContext();
            Animation animationOut = AnimationUtils.loadAnimation(context, R.anim.smart_handy_view_enter_out);
            lastViewAdapter.view.startAnimation(animationOut);

            Animation animationIn = AnimationUtils.loadAnimation(context, R.anim.smart_handy_view_enter_in);
            viewAdapter.view.startAnimation(animationIn);
        }
        /* 从详情页返回主页 */
        else if (lastViewAdapter != null && viewAdapter.type == ViewData.TYPE_SMART_HANDY_HOME_VIEW) {
            Context context = UIResLoader.getInstance().getModifyContext();
            Animation animationOut = AnimationUtils.loadAnimation(context, R.anim.smart_handy_view_exit_out);
            lastViewAdapter.view.startAnimation(animationOut);

            Animation animationIn = AnimationUtils.loadAnimation(context, R.anim.smart_handy_view_exit_in);
            viewAdapter.view.startAnimation(animationIn);
        }

        mContainer.removeAllViews();
        mContainer.addView(viewAdapter.view);

        lastViewAdapter = viewAdapter;
    }

    /**
     * 结束开机动画，显示主页
     */
    private void showHomeView() {
        mContainer.removeAllViews();
        if (lastViewAdapter != null) {
            mContainer.addView(lastViewAdapter.view);
            if (lastViewAdapter instanceof ExtViewAdapter) {
                ExtViewAdapter adapter = (ExtViewAdapter) lastViewAdapter;
                if (adapter.object instanceof SmartHandyHomeView) {
                    SmartHandyHomeView smartHandyHomeView = (SmartHandyHomeView) adapter.object;
                    smartHandyHomeView.startAnimEnter();
                }
            }
        }
    }

}
