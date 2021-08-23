package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.TtsListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITtsListView;
import com.txznet.resholder.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 语音主题列表
 * <p>
 * 2020-08-18 19:07
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class TtsListView extends ITtsListView {

    private static TtsListView sInstance = new TtsListView();

    private List<View> mItemViews;

    private TtsListView() {
    }

    public static TtsListView getInstance() {
        return sInstance;
    }

    @Override
    public void updateProgress(int progress, int selection) {

    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        TtsListViewData ttsListViewData = (TtsListViewData) data;

        WinLayout.getInstance().vTips = ttsListViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "ttsListViewData.vTips: " + ttsListViewData.vTips);

        View view = createView(ttsListViewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = TtsListView.getInstance();
        return viewAdapter;
    }


    /**
     *
     * @param viewData
     * @return
     */
    private View createView(TtsListViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = viewData.mTitleInfo.maxPage;
        int curPage = viewData.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        ArrayList<TtsListViewData.TtsBean> dataAry = viewData.getData();

        mItemViews = new ArrayList<>();
        for (int i = 0; i < viewData.count; i++) {
            View itemView = createItemView(context, i, dataAry.get(i), i != viewData.count - 1);
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

    private View createItemView(Context context, int pos, TtsListViewData.TtsBean row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.tts_list_item, (ViewGroup) null);

        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        View divider = view.findViewById(R.id.divider);

        tvTitle.setText(String.format(Locale.getDefault(), "%d. %s", pos + 1, row.name));

        // 分隔线
        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        // 设置列表点击
        ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, pos);
        return view;
    }


    @Override
    public void init() {
        super.init();
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }

    @Override
    public void snapPage(boolean next) {
        LogUtil.logd("update snap " + next);
    }

    @Override
    public List<View> getFocusViews() {
        return mItemViews;
    }

    @Override
    public boolean supportKeyEvent() {
        return true;
    }


    /**
     * 是否含有动画
     *
     * @return
     */
    @Override
    public boolean hasViewAnimation() {
        return true;
    }

    @Override
    public void release() {
        super.release();
        if (mItemViews != null) {
            mItemViews.clear();
        }
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

}
