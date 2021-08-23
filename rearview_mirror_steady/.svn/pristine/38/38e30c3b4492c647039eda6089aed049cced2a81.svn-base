package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.NavAppListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.INavAppListView;
import com.txznet.comm.util.StringUtils;
import com.txznet.resholder.R;
import com.txznet.txz.util.LanguageConvertor;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置默认导航App列表
 * <p>
 * 2020-08-18
 *
 * @author xiaolin
 */
public class NavAppListView extends INavAppListView {
    private static NavAppListView instance;

    private List<View> mItemViews = new ArrayList<View>();

    public static NavAppListView getInstance() {
        if (instance == null) {
            synchronized (NavAppListView.class) {
                if (instance == null) {
                    instance = new NavAppListView();
                }
            }
        }
        return instance;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void updateProgress(int progress, int selection) {
    }

    @Override
    public void snapPage(boolean next) {
    }

    @Override
    public void updateItemSelect(int index) {
        for (int i = 0; i < mItemViews.size(); i++) {
            if (i == index) {
                Context context = UIResLoader.getInstance().getModifyContext();
                mItemViews.get(i).setBackground(context.getResources().getDrawable(R.drawable.item_setlected));
            } else {
                mItemViews.get(i).setBackground(null);
            }
        }
    }

    @Override
    public void release() {
        super.release();
        if (mItemViews != null) {
            mItemViews.clear();
        }
    }

    @Override
    public List<View> getFocusViews() {
        return mItemViews;
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData viewData) {
        NavAppListViewData data = (NavAppListViewData) viewData;
        WinLayout.getInstance().vTips = data.vTips;
        LogUtil.logd(WinLayout.logTag + "weChatListViewData.vTips: " + data.vTips);

        View view = createViewNone(data);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.isListView = true;
        viewAdapter.object = this;
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.type = data.getType();
        return viewAdapter;
    }

    private View createViewNone(NavAppListViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = viewData.mTitleInfo.maxPage;
        int curPage = viewData.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        ArrayList<NavAppListViewData.NavAppBean> navAppBeans = viewData.getData();

        mItemViews = new ArrayList<>();
        for (int i = 0; i < viewData.count; i++) {
            View itemView = createItemView(context, i, navAppBeans.get(i), i != viewData.count - 1);
            container.addView(itemView);
            mItemViews.add(itemView);
        }

        // 添加空视图填充空间
        int re = SizeConfig.pagePoiCount - viewData.count;
        for (int i = 0; i < re; i++) {
            View itemView = createItemView(context, i, null, false);
            container.addView(itemView);
        }

        return view;
    }

    private View createItemView(Context context, int position, NavAppListViewData.NavAppBean row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.nav_app_list_item, (ViewGroup) null);

        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvPos = view.findViewById(R.id.tvPos);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        ImageView ivLogo = view.findViewById(R.id.ivLogo);
        View divider = view.findViewById(R.id.divider);

        tvPos.setText(String.valueOf(position + 1));
        tvTitle.setText(StringUtils.isEmpty(row.title) ? "" : LanguageConvertor.toLocale(row.title));
        ivLogo.setImageDrawable(getDrawableByPkn(context, row.navPkn));


        // 分隔线
        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        // 设置列表点击
        ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, position);
        return view;
    }

    private Drawable getDrawableByPkn(Context mContext, String navPkn) {
        PackageManager pm = mContext.getPackageManager();
        try {
            return pm.getApplicationIcon(navPkn);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
