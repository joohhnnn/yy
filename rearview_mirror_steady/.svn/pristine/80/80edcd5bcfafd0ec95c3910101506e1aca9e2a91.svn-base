package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.PoiListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IPoiListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.resholder.R;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.util.LanguageConvertor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 导航列表
 * <p>
 * 商圈列表可能有, 团,惠,券, 评分, 价格
 * <p>
 * 历史导般列表
 * <p>
 * 2020-08-13 10:00
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class PoiListView extends IPoiListView {

    private static PoiListView sInstance = new PoiListView();

    private ViewGroup mView = null;
    private List<View> mItemViews;


    private PoiListView() {
        mFlags = 0;
        // 表明当前View支持更新
        mFlags = mFlags | UPDATEABLE;
    }

    public static PoiListView getInstance() {
        return sInstance;
    }

    @Override
    public Object updateView(ViewData data) {
        LogUtil.logd(WinLayout.logTag + "updateView: ");

        return super.updateView(data);
    }

    @Override
    public void updateProgress(int progress, int selection) {

    }

    @Override
    public Integer getFlags() {
        return super.getFlags();
    }

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
        if (mView != null) {
            mView.removeAllViews();
            mView = null;
        }
    }

    @Override
    public ViewAdapter getView(ViewData data) {
        PoiListViewData poiListViewData = (PoiListViewData) data;
        LogUtil.logd(WinLayout.logTag + "getView: " + "poiListViewData.vTips:" + poiListViewData.vTips + "--" + poiListViewData);

        WinLayout.getInstance().vTips = poiListViewData.vTips;

        ViewGroup view = createView(poiListViewData);
        this.mView = view;

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.flags = 0;
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = PoiListView.getInstance();
        return viewAdapter;
    }

    /**
     * 是否是商圈模式
     *
     * @param viewData
     * @return
     */
    private boolean isBusiness(PoiListViewData viewData) {
        ArrayList<Poi> pois = viewData.getData();
        for (Poi poi : pois) {
            if (poi instanceof BusinessPoiDetail) {
                return true;
            }
        }
        return false;
    }

    private ViewGroup createView(PoiListViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = viewData.mTitleInfo.maxPage;
        int curPage = viewData.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        ArrayList<Poi> pois = viewData.getData();
        mItemViews = new ArrayList<>();


        /*历史导航*/
        if (PoiAction.ACTION_NAV_HISTORY.equals(viewData.action)) {
            for (int i = 0; i < viewData.count; i++) {
                View itemView = createItemHistoryView(context, i, pois.get(i), i != viewData.count - 1);
                container.addView(itemView);
                mItemViews.add(itemView);
            }

            // 添加空视图填充空间
            int re = SizeConfig.pagePoiHistoryCount - viewData.count;
            for (int i = 0; i < re; i++) {
                View itemView = createItemHistoryView(context, i, null, false);
                container.addView(itemView);
            }
        }
        /*商圈模式*/
        else if (isBusiness(viewData)) {
            for (int i = 0; i < viewData.count; i++) {
                View itemView = createItemBusinessView(context, i, pois.get(i), i != viewData.count - 1);
                container.addView(itemView);
                mItemViews.add(itemView);
            }

            // 添加空视图填充空间
            int re = SizeConfig.pageBusinessPoiCount - viewData.count;
            for (int i = 0; i < re; i++) {
                View itemView = createItemBusinessView(context, i, null, false);
                container.addView(itemView);
            }
        }
        /*导航列表*/
        else {
            for (int i = 0; i < viewData.count; i++) {
                View itemView = createItemView(context, i, pois.get(i), i != viewData.count - 1);
                container.addView(itemView);
                mItemViews.add(itemView);
            }

            // 添加空视图填充空间
            int re = SizeConfig.pagePoiCount - viewData.count;
            for (int i = 0; i < re; i++) {
                View itemView = createItemView(context, i, null, false);
                container.addView(itemView);
            }
        }

        return view;
    }

    /**
     * 导航地址
     *
     * @param context
     * @param row
     * @param index
     * @param showDivider
     * @return
     */
    private View createItemView(Context context, int index, Poi row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.poi_list_view_item, (ViewGroup) null);

        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDetail = view.findViewById(R.id.tvDetail);
        View divider = view.findViewById(R.id.divider);


        // 设置显示距离
        long d = row.getDistance();
        String distanceStr;
        if (d < 1000) {
            distanceStr = d + "m";
        } else {
            distanceStr = String.format(Locale.getDefault(), "%.1fkm", d / 1000.0);
        }

        tvTitle.setText(String.format(Locale.getDefault(), "%d. %s", index + 1, row.getName()));
        tvDetail.setText(String.format(Locale.getDefault(), "%s | %s", distanceStr, row.getGeoinfo()));

        // 分隔线
        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        // 设置列表点击
        ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, index);
        return view;
    }

    /**
     * 历史导航
     *
     * @param context
     * @param row
     * @param index
     * @param showDivider
     * @return
     */
    private View createItemHistoryView(Context context, final int index, Poi row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.poi_list_view_history_item, (ViewGroup) null);
        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDetail = view.findViewById(R.id.tvDetail);
        ImageView ivClose = view.findViewById(R.id.ivClose);
        View divider = view.findViewById(R.id.divider);

        // 设置显示距离
        long d = row.getDistance();
        String distanceStr;
        if (d < 1000) {
            distanceStr = d + "m";
        } else {
            distanceStr = String.format(Locale.getDefault(), "%.1fkm", d / 1000.0);
        }

        tvTitle.setText(String.format(Locale.getDefault(), "%d. %s", index + 1, row.getName()));
        tvDetail.setText(String.format(Locale.getDefault(), "%s | %s", distanceStr, row.getGeoinfo()));

        // 删除历史记录
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONBuilder jb = new JSONBuilder();
                jb.put("index", index);
                jb.put("action","delete");
                ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
                        "txz.record.ui.event.item.selected", jb.toBytes(), null);
            }
        });

        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        // 设置列表点击
        ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, index);
        return view;
    }

    /**
     * 美食
     *
     * @param context
     * @param row
     * @param index
     * @param showDivider
     * @return
     */
    private View createItemBusinessView(Context context, int index, Poi row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.poi_list_view_business_item, (ViewGroup) null);

        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDetail = view.findViewById(R.id.tvDetail);
        View divider = view.findViewById(R.id.divider);         // 分隔线
        ViewGroup businessWrap = view.findViewById(R.id.businessWrap);
        ImageView ivJuan = view.findViewById(R.id.ivJuan);      // 券
        ImageView ivTuan = view.findViewById(R.id.ivTuan);      // 团
        ImageView ivHui = view.findViewById(R.id.ivHui);        // 惠
        ImageView ivScore = view.findViewById(R.id.ivScore);    // 评分的星星
        TextView tvPrice = view.findViewById(R.id.tvPrice);     // 均价


        // 设置显示距离
        long d = row.getDistance();
        String distanceStr;
        if (d < 1000) {
            distanceStr = d + "m";
        } else {
            distanceStr = String.format(Locale.getDefault(), "%.1fkm", d / 1000F);
        }

        tvTitle.setText(String.format(Locale.getDefault(), "%d. %s", index + 1, row.getName()));
        tvDetail.setText(String.format(Locale.getDefault(), "%s | %s", distanceStr, row.getGeoinfo()));


        if(row instanceof BusinessPoiDetail) {
            BusinessPoiDetail poiDetail = (BusinessPoiDetail) row;

            // 惠
            if (poiDetail.isHasCoupon()) {
                ivHui.setVisibility(View.VISIBLE);
            } else {
                ivHui.setVisibility(View.GONE);
            }

            // 团
            if (poiDetail.isHasDeal()) {
                ivTuan.setVisibility(View.VISIBLE);
            } else {
                ivTuan.setVisibility(View.GONE);
            }

            // 评分
            double score = poiDetail.getScore();
            if (score < 1) {
                ivScore.setVisibility(View.GONE);
            } else {
                ivScore.setImageResource(getSoreMarkDrawableRes(score));
            }

            // 价格
            int price = (int) poiDetail.getAvgPrice();
            if (price > 0) {
                String txt = String.format(Locale.getDefault(), "￥%d/人", price);
                tvPrice.setText(LanguageConvertor.toLocale(txt));
            } else {
                tvPrice.setVisibility(View.GONE);
            }
        }else {
            businessWrap.setVisibility(View.GONE);
        }

        if (!showDivider) {
            divider.setVisibility(View.GONE);
        }

        // 设置列表项点击
        ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, index);
        return view;
    }

    @Override
    public void init() {

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

    private int getSoreMarkDrawableRes(double score) {
        if (score < 1.0f) {
            return R.drawable.dz_icon_star0;
        } else if (score < 2.0f) {
            return R.drawable.dz_icon_star1;
        } else if (score < 3.0f) {
            return R.drawable.dz_icon_star2;
        } else if (score < 4.0f) {
            return R.drawable.dz_icon_star3;
        } else if (score < 5.0f) {
            return R.drawable.dz_icon_star4;
        } else if (score < 6.0f) {
            return R.drawable.dz_icon_star5;
        } else if (score < 7.0f) {
            return R.drawable.dz_icon_star6;
        } else if (score < 8.0f) {
            return R.drawable.dz_icon_star7;
        } else if (score < 9.0f) {
            return R.drawable.dz_icon_star8;
        } else if (score < 10.0f) {
            return R.drawable.dz_icon_star9;
        } else {
            return R.drawable.dz_icon_star10;
        }
    }

    @Override
    public void updateItemSelect(int index) {
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
