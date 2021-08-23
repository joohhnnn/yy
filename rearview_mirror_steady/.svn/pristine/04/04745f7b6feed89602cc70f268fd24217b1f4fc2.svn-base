package com.txznet.comm.ui.theme.test.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ReminderListViewData;
import com.txznet.comm.ui.viewfactory.data.ReminderListViewData.ReminderItemBean;
import com.txznet.comm.ui.viewfactory.data.TtsListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IReminderListView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.resholder.R;

/**
 * 说明：提醒事项列表
 *
 * @author xiaolin
 * create at 2020-09-01 16：14
 */
public class ReminderListView extends IReminderListView {
    private static ReminderListView instance = new ReminderListView();

    private List<View> mItemViews;

    public static ReminderListView getInstance() {
        return instance;
    }

    @Override
    public void init() {
        super.init();

    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }

    @SuppressLint("NewApi")
    @Override
    public ExtViewAdapter getView(ViewData data) {
        ReminderListViewData reminderListViewData = (ReminderListViewData) data;
        WinLayout.getInstance().vTips = reminderListViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "ReminderListViewData.vTips: " + reminderListViewData.vTips);

        View view = createViewNone(reminderListViewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = ReminderListView.getInstance();
        return viewAdapter;
    }

    private View createViewNone(ReminderListViewData viewData) {
		Context context = UIResLoader.getInstance().getModifyContext();
		int maxPage = viewData.mTitleInfo.maxPage;
		int curPage = viewData.mTitleInfo.curPage;

		ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
		ViewGroup view = listContainer.rootView;
		ViewGroup container = listContainer.container;

		ArrayList<ReminderListViewData.ReminderItemBean> dataAry = viewData.getData();

		mItemViews = new ArrayList<>();
		for (int i = 0; i < viewData.count; i++) {
			View itemView = createItemView(context, i, dataAry.get(i), i != viewData.count - 1);
			container.addView(itemView);
			mItemViews.add(itemView);
		}

		// 添加空视图填充空间
		int re = SizeConfig.pageCount - viewData.count;
		for (int i = 0; i < re; i++) {
			View itemView = createItemView(context, i, null, false);
			container.addView(itemView);
		}

		return view;
    }

    @SuppressLint("NewApi")
    private View createItemView(Context context, int pos, ReminderItemBean row, boolean showDivider) {
    	View view = LayoutInflater.from(context).inflate(R.layout.reminder_list_view_item, (ViewGroup)null);

		if (row == null) {
			view.setVisibility(View.INVISIBLE);
			return view;
		}

    	TextView tvTitle = view.findViewById(R.id.tvTitle);
    	TextView tvDesc = view.findViewById(R.id.tvDesc);
    	View divider = view.findViewById(R.id.divider);

    	tvTitle.setText(String.format(Locale.getDefault(), "%d. %s", pos+1, row.content));
    	tvDesc.setText(row.time);

		// 分隔线
		if (!showDivider) {
			divider.setVisibility(View.GONE);
		}

		// 设置列表点击
		ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, pos);
		return view;
    }

    @Override
    public void updateProgress(int progress, int selection) {

    }

    @Override
    public void snapPage(boolean next) {

    }

    @Override
    public void updateItemSelect(int index) {
        LogUtil.logd(WinLayout.logTag + "ReminderListView.updateItemSelect " + index);
        showSelectItem(index);
    }

    private void showSelectItem(int index) {
        for (int i = 0; i < mItemViews.size(); i++) {
            if (i == index) {
                mItemViews.get(i).setBackgroundResource(R.drawable.item_setlected);
            } else {
                mItemViews.get(i).setBackground(null);
            }
        }
    }
}
