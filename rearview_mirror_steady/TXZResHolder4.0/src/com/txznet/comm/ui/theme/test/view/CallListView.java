package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
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
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.CallListViewData;
import com.txznet.comm.ui.viewfactory.data.CallListViewData.CallBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ICallListView;
import com.txznet.resholder.R;
import com.txznet.txz.util.LanguageConvertor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * 联系人列表
 * <p>
 * 唤起：打电话给张三
 * <p>
 * 2020-08-18
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class CallListView extends ICallListView {

    private static CallListView sInstance = new CallListView();

    private List<View> mItemViews;

    private CallListView() {
    }

    public static CallListView getInstance() {
        return sInstance;
    }

    @Override
    public void updateProgress(int progress, int selection) {

    }


    @Override
    public void release() {
        super.release();
        if (mItemViews != null) {
            mItemViews.clear();
        }
    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        CallListViewData callListViewData = (CallListViewData) data;
        WinLayout.getInstance().vTips = callListViewData.vTips;

        View view = createViewNone(callListViewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = CallListView.getInstance();
        return viewAdapter;
    }

    private View createViewNone(CallListViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = viewData.mTitleInfo.maxPage;
        int curPage = viewData.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        ArrayList<CallListViewData.CallBean> dataAry = viewData.getData();

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

    private View createItemView(Context context, int position, CallBean row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.call_list_view_item, (ViewGroup) null);

        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvTitle = view.findViewById(R.id.tvTitle);         // 标题
        TextView tvDesc = view.findViewById(R.id.tvDesc);            // 副标题
        View divider = view.findViewById(R.id.divider);

        if (TextUtils.isEmpty(row.name)) {
            // 名字为空使用号码作为标题
            String number = row.number;
            if (row.number != null && row.number.length() == 11) {
                StringBuilder stringBuilder = new StringBuilder(row.number);
                stringBuilder.insert(3, " ");
                stringBuilder.insert(8, " ");
                number = stringBuilder.toString();
            }
            tvTitle.setText(String.format(Locale.getDefault(), "%d. %s",
                    position + 1, number));
        } else {
            tvTitle.setText(String.format(Locale.getDefault(), "%d. %s",
                    position + 1, LanguageConvertor.toLocale(row.name)));
        }

        if (TextUtils.isEmpty(row.number)) {
            tvDesc.setText("");
        } else {
            String number = row.number;
            if (row.number.length() == 11) {
                StringBuilder stringBuilder = new StringBuilder(row.number);
                stringBuilder.insert(3, " ");
                stringBuilder.insert(8, " ");
                number = stringBuilder.toString();
            }
            tvDesc.setText(number);

            StringBuilder sb = new StringBuilder();

            // 名字不为空，第二行显示号码
            if(!TextUtils.isEmpty(row.name)){
                sb.append(row.number);
            }

            // 省份
            if (!TextUtils.isEmpty(row.province)) {
                sb.append(" ").append(row.province);
            }
            // 城市
            if (!TextUtils.isEmpty(row.city)) {
                sb.append(" ").append(row.city);
            }
            // 运营商名称
            if (!TextUtils.isEmpty(row.isp)) {
                sb.append(" ").append(row.isp);
            }
            tvDesc.setText(sb.toString().trim());
        }


        // 分隔线
        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        // 设置列表点击
        ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, position);
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
    public void updateItemSelect(int index) {
        LogUtil.logd(WinLayout.logTag + "train updateItemSelect " + index);
        showSelectItem(index);
    }

    private void showSelectItem(int index) {
        for (int i = 0; i < mItemViews.size(); i++) {
            if (i == index) {
                mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
            } else {
                mItemViews.get(i).setBackground(null);
            }
        }
    }

}
