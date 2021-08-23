package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.SelectCityViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ISelectCityView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS User on 2018/7/19.
 */

public class SelectCityView extends ISelectCityView {

    class ViewHolder {
        View rootView;
        TextView mCurCityTitle = null;
        ListView mCurCityList = null;
        TextView mPerCityTitle = null;
        ListView mPerCityList = null;
        TextView mNomCityTitle = null;
        ListView mNomCityList = null;
        TextView mTarCityTitle = null;
        ListView mTarCityList = null;
        RelativeLayout mTvBack = null;

    }

    private ViewHolder mViewHolder = null;

    private int bgColor;    //整个背景颜色
    private int titleHeight;    //标题内容高度
    private int titleMarginHorizontal;    //标题内容左右边距
    private int iconBackSide;    //返回图标大小
    private int tvBackSize;    //返回字体大小
    private int tvBackColor;    //返回字体颜色
    private int tvTitleSize;    //标题字体大小
    private int tvTitleColor;    //标题字体颜色
    private int contentMarginHorizontal;    //内容左右边距
    private int contentMarginTop;    //内容上边距
    private int listTitleHeight;    //列表标题高度
    private int listTitlePadding;    //列表标题内边距
    private int listTitleBgColor;    //列表标题背景颜色
    private int listTitleSize;    //列表标题字体大小
    private int listTitleColor;    //列表标题字体颜色
    private int listContentHeight;    //列表内容高度
    private int listContentPadding;    //列表内容内边距
    private int listContentBgColor;    //列表内容背景颜色
    private int listContentSize;    //列表内容字体大小
    private int listContentColor;    //列表内容字体颜色

    private static SelectCityView instance = new SelectCityView();

    public static SelectCityView getInstance() {
        return instance;
    }

    private SelectCityView() {

    }

    private SelectCityViewData mSelectCityViewData;

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        LogUtil.logd(WinLayout.logTag+ "getView: SelectCityView");

        mSelectCityViewData = (SelectCityViewData) data;
//        getTestDate();    //使用测试数据
        ViewFactory.ViewAdapter adapter = new ViewFactory.ViewAdapter();
        adapter.view = createContentView();
        adapter.type = ViewData.TYPE_SELECT_CITY_VIEW;
        return adapter;
    }

    private View createContentView() {
        mViewHolder = new ViewHolder();
        LinearLayout layout = new LinearLayout(GlobalContext.get());
        mViewHolder.rootView = layout;
        //layout.setBackgroundColor(Color.parseColor("#FF0A0A0A"));
        //layout.setBackgroundColor(bgColor);
        layout.setBackground(LayouUtil.getDrawable("bg"));
        layout.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout rlTitle = new RelativeLayout(GlobalContext.get());
        rlTitle.setPadding(titleMarginHorizontal, 0, titleMarginHorizontal, 0);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, titleHeight);
        rlTitle.setLayoutParams(llLayoutParams);
        layout.addView(rlTitle);

        RelativeLayout rlBack = new RelativeLayout(GlobalContext.get());
        mViewHolder.mTvBack = rlBack;
        rlBack.setClickable(true);
        RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlBack.setLayoutParams(rlLayoutParams);
        rlTitle.addView(rlBack);

        ImageView ivBack = new ImageView(GlobalContext.get());
        ivBack.setScaleType(ImageView.ScaleType.FIT_END);
        ivBack.setImageDrawable(LayouUtil.getDrawable("back"));
        rlLayoutParams = new RelativeLayout.LayoutParams(iconBackSide, iconBackSide);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        ivBack.setId(ViewUtils.generateViewId());
        ivBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(ivBack);

        TextView tvBack = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvBack,tvBackSize);
        TextViewUtil.setTextColor(tvBack,tvBackColor);
        //tvBack.setTextColor(Color.parseColor("#FFFFFF"));
        //tvBack.setHintTextColor(Color.parseColor("#40454b"));
        //tvBack.setTextSize(31);
        tvBack.setText("返回");
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.RIGHT_OF, ivBack.getId());
        tvBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(tvBack);

        TextView tvTitle = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvTitle,tvTitleSize);
        TextViewUtil.setTextColor(tvTitle,tvTitleColor);
        //tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
        //tvTitle.setHintTextColor(Color.parseColor("#40454b"));
        //tvTitle.setTextSize(37);
        //tvTitle.setText("修改关键字");
        tvTitle.setText("修改城市");
        tvTitle.setGravity(Gravity.CENTER);
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        tvTitle.setLayoutParams(rlLayoutParams);
        rlTitle.addView(tvTitle);

        ScrollView scrollView = new ScrollView(GlobalContext.get());
        scrollView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llLayoutParams.setMargins(contentMarginHorizontal,contentMarginTop,contentMarginHorizontal,contentMarginTop);
        scrollView.setLayoutParams(llLayoutParams);
        //scrollView.setBackground(LayouUtil.getDrawable("select_city_range_top_layout"));select_city_range_bottom_layout
        layout.addView(scrollView);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llContent.setLayoutParams(llLayoutParams);
        scrollView.addView(llContent);

        TextView tvCurCity = new TextView(GlobalContext.get());
        mViewHolder.mCurCityTitle = tvCurCity;
        //tvCurCity.setBackgroundColor(Color.parseColor("#40FFFFFF"));
        //tvCurCity.setBackgroundColor(listTitleBgColor);
        tvCurCity.setBackground(LayouUtil.getDrawable("select_city_range_top_layout"));
        tvCurCity.setPadding(listTitlePadding, 0, 0, 0);
        tvCurCity.setGravity(Gravity.CENTER_VERTICAL);
        tvCurCity.setVisibility(View.GONE);
        TextViewUtil.setTextSize(tvCurCity,listTitleSize);
        TextViewUtil.setTextColor(tvCurCity,listTitleColor);
        tvCurCity.setText("当前城市");
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listTitleHeight);
        tvCurCity.setLayoutParams(llLayoutParams);
        llContent.addView(tvCurCity);

        /*View view = new View(GlobalContext.get());
        view.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        view.setLayoutParams(llLayoutParams);
        llContent.addView(view);*/

        ListView lvCurCity = new ListView(GlobalContext.get());
        mViewHolder.mCurCityList = lvCurCity;
        //lvCurCity.setBackgroundColor(Color.parseColor("#26FFFFFF"));
        lvCurCity.setBackgroundColor(listContentBgColor);
        lvCurCity.setDivider(new ColorDrawable(Color.parseColor("#1AFFFFFF")));
        lvCurCity.setDividerHeight(1);
        lvCurCity.setVisibility(View.GONE);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lvCurCity.setLayoutParams(llLayoutParams);
        llContent.addView(lvCurCity);

        TextView tvTarCity = new TextView(GlobalContext.get());
        mViewHolder.mTarCityTitle = tvTarCity;
        tvTarCity.setBackgroundColor(listTitleBgColor);
        tvTarCity.setPadding(listTitlePadding, 0, 0, 0);
        tvTarCity.setGravity(Gravity.CENTER_VERTICAL);
        tvTarCity.setVisibility(View.GONE);
        TextViewUtil.setTextSize(tvTarCity,listTitleSize);
        TextViewUtil.setTextColor(tvTarCity,listTitleColor);
        tvTarCity.setText("目标城市");
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listTitleHeight);
        tvTarCity.setLayoutParams(llLayoutParams);
        llContent.addView(tvTarCity);

        /*view = new View(GlobalContext.get());
        view.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        view.setLayoutParams(llLayoutParams);
        llContent.addView(view);*/

        ListView lvTarCity = new ListView(GlobalContext.get());
        mViewHolder.mTarCityList = lvTarCity;
        //lvTarCity.setBackgroundColor(Color.parseColor("#26FFFFFF"));
        lvTarCity.setBackgroundColor(listContentBgColor);
        lvTarCity.setDivider(new ColorDrawable(Color.parseColor("#1AFFFFFF")));
        lvTarCity.setDividerHeight(1);
        lvTarCity.setVisibility(View.GONE);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lvTarCity.setLayoutParams(llLayoutParams);
        llContent.addView(lvTarCity);

        TextView tvPerCity = new TextView(GlobalContext.get());
        mViewHolder.mPerCityTitle = tvPerCity;
        tvPerCity.setBackgroundColor(listTitleBgColor);
        tvPerCity.setPadding(listTitlePadding, 0, 0, 0);
        tvPerCity.setGravity(Gravity.CENTER_VERTICAL);
        tvPerCity.setVisibility(View.GONE);
        tvPerCity.setTextColor(Color.parseColor("#999999"));
        tvPerCity.setText("常驻城市");
        TextViewUtil.setTextSize(tvPerCity,listTitleSize);
        TextViewUtil.setTextColor(tvPerCity,listTitleColor);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listTitleHeight);
        tvPerCity.setLayoutParams(llLayoutParams);
        llContent.addView(tvPerCity);

        /*view = new View(GlobalContext.get());
        view.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        view.setLayoutParams(llLayoutParams);
        llContent.addView(view);*/

        ListView lvPerCity = new ListView(GlobalContext.get());
        mViewHolder.mPerCityList = lvPerCity;
        //lvPerCity.setBackgroundColor(Color.parseColor("#26FFFFFF"));
        lvPerCity.setBackgroundColor(listContentBgColor);
        lvPerCity.setDivider(new ColorDrawable(Color.parseColor("#1AFFFFFF")));
        lvPerCity.setDividerHeight(1);
        lvPerCity.setVisibility(View.GONE);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lvPerCity.setLayoutParams(llLayoutParams);
        llContent.addView(lvPerCity);

        /*view = new View(GlobalContext.get());
        view.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        view.setLayoutParams(llLayoutParams);
        llContent.addView(view);*/

        TextView tvNomCity = new TextView(GlobalContext.get());
        mViewHolder.mNomCityTitle = tvNomCity;
        //tvNomCity.setBackgroundColor(Color.parseColor("#40FFFFFF"));
        tvNomCity.setBackgroundColor(listTitleBgColor);
        tvNomCity.setPadding(listTitlePadding, 0, 0, 0);
        tvNomCity.setGravity(Gravity.CENTER_VERTICAL);
        tvNomCity.setVisibility(View.GONE);
        TextViewUtil.setTextSize(tvNomCity,listTitleSize);
        TextViewUtil.setTextColor(tvNomCity,listTitleColor);
        tvNomCity.setText("推荐城市");
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listTitleHeight);
        tvNomCity.setLayoutParams(llLayoutParams);
        llContent.addView(tvNomCity);

        /*view = new View(GlobalContext.get());
        view.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        view.setLayoutParams(llLayoutParams);
        llContent.addView(view);*/

        ListView lvNomCity = new ListView(GlobalContext.get());
        mViewHolder.mNomCityList = lvNomCity;
        //lvNomCity.setBackgroundColor(Color.parseColor("#26FFFFFF"));
        lvNomCity.setBackgroundColor(listContentBgColor);
        lvNomCity.setDivider(new ColorDrawable(Color.parseColor("#1AFFFFFF")));
        lvNomCity.setDividerHeight(1);
        lvNomCity.setVisibility(View.GONE);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lvNomCity.setLayoutParams(llLayoutParams);
        llContent.addView(lvNomCity);


        rlBack.setOnClickListener(onClickListener);


        if (mSelectCityViewData.curCityList != null && mSelectCityViewData.curCityList.size() > 0) {
            mViewHolder.mCurCityTitle.setVisibility(View.VISIBLE);
            mViewHolder.mCurCityList.setVisibility(View.VISIBLE);
            CityListAdapter adapter = new CityListAdapter(GlobalContext.get(), mSelectCityViewData.curCityList);
            mViewHolder.mCurCityList.setAdapter(adapter);
            setListViewHeightBasedOnChildren(mViewHolder.mCurCityList);
            //mViewHolder.mCurCityList.setOnItemClickListener(onItemClickListener);
        } else {
            mViewHolder.mCurCityTitle.setVisibility(View.GONE);
            mViewHolder.mCurCityList.setVisibility(View.GONE);
        }
        if (mSelectCityViewData.perCityList != null && mSelectCityViewData.perCityList.size() > 0) {
            mViewHolder.mPerCityTitle.setVisibility(View.VISIBLE);
            mViewHolder.mPerCityList.setVisibility(View.VISIBLE);
            CityListAdapter adapter = new CityListAdapter(GlobalContext.get(), mSelectCityViewData.perCityList);
            mViewHolder.mPerCityList.setAdapter(adapter);
            setListViewHeightBasedOnChildren(mViewHolder.mPerCityList);
            mViewHolder.mPerCityList.setOnItemClickListener(onItemClickListener);
        } else {
            mViewHolder.mPerCityTitle.setVisibility(View.GONE);
            mViewHolder.mPerCityList.setVisibility(View.GONE);
        }
        if (mSelectCityViewData.nomCityList != null && mSelectCityViewData.nomCityList.size() > 0) {
            mViewHolder.mNomCityTitle.setVisibility(View.VISIBLE);
            mViewHolder.mNomCityList.setVisibility(View.VISIBLE);
            CityListAdapter adapter = new CityListAdapter(GlobalContext.get(), mSelectCityViewData.nomCityList);
            mViewHolder.mNomCityList.setAdapter(adapter);
            setListViewHeightBasedOnChildren(mViewHolder.mNomCityList);
            mViewHolder.mNomCityList.setOnItemClickListener(onItemClickListener);
            mViewHolder.mNomCityList.setBackground(LayouUtil.getDrawable("select_city_range_bottom_layout"));
        } else {
            mViewHolder.mNomCityTitle.setVisibility(View.GONE);
            mViewHolder.mNomCityList.setVisibility(View.GONE);
        }
        if (mSelectCityViewData.tarCityList != null && mSelectCityViewData.tarCityList.size() > 0) {
            mViewHolder.mTarCityList.setVisibility(View.VISIBLE);
            mViewHolder.mTarCityTitle.setVisibility(View.VISIBLE);
            CityListAdapter adapter = new CityListAdapter(GlobalContext.get(), mSelectCityViewData.tarCityList);
            mViewHolder.mTarCityList.setAdapter(adapter);
            setListViewHeightBasedOnChildren(mViewHolder.mTarCityList);
            //mViewHolder.mTarCityList.setOnItemClickListener(onItemClickListener);
            mViewHolder.mTarCityList.setOnItemClickListener(onCurrCityItemClickListener);
            mViewHolder.mCurCityList.setOnItemClickListener(onItemClickListener);
        } else {
            mViewHolder.mTarCityList.setVisibility(View.GONE);
            mViewHolder.mTarCityTitle.setVisibility(View.GONE);
            mViewHolder.mCurCityList.setOnItemClickListener(onCurrCityItemClickListener);
        }
        //底部圆角边框
        if (mViewHolder.mNomCityList.getVisibility() == View.GONE){
            if (mViewHolder.mPerCityList.getVisibility() == View.GONE){
                if (mViewHolder.mTarCityList.getVisibility() == View.GONE){
                    mViewHolder.mCurCityList.setBackground(LayouUtil.getDrawable("select_city_range_bottom_layout"));
                }else {
                    mViewHolder.mTarCityList.setBackground(LayouUtil.getDrawable("select_city_range_bottom_layout"));
                }
            }else {
                mViewHolder.mPerCityList.setBackground(LayouUtil.getDrawable("select_city_range_bottom_layout"));
            }
        }

        return layout;

    }

    @Override
    public void init() {
        bgColor = Color.parseColor(LayouUtil.getString("color_search_bg"));
        tvBackColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvTitleColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        listTitleBgColor = Color.parseColor(LayouUtil.getString("color_city_list_title"));
        listTitleColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        listContentBgColor = Color.parseColor(LayouUtil.getString("color_city_list_content"));
        listContentColor = Color.parseColor(LayouUtil.getString("color_main_title"));
//        if (WinLayout.isVertScreen){
//            int unit = (int) LayouUtil.getDimen("vertical_unit");
//            titleHeight = 10 * unit;
//            titleMarginHorizontal = 3 * unit;
//            iconBackSide = 3 * unit;
//            tvBackSize = (int) LayouUtil.getDimen("vertical_h5");
//            tvTitleSize = (int) LayouUtil.getDimen("vertical_h1");
//            contentMarginHorizontal = 5 * unit;
//            contentMarginTop = 4 * unit;
//            listTitleHeight = 6 * unit;
//            listTitlePadding = 3 * unit;
//            listTitleSize = (int) LayouUtil.getDimen("vertical_h6");
//            listContentHeight = 8 * unit;
//            listContentPadding = 5 * unit;
//            listContentSize = (int) LayouUtil.getDimen("vertical_h3");
//        }else {
//            int unit = (int) LayouUtil.getDimen("unit");
//            titleHeight = 10 * unit;
//            titleMarginHorizontal = 3 * unit;
//            iconBackSide = 3 * unit;
//            tvBackSize = (int) LayouUtil.getDimen("h5");
//            tvTitleSize = (int) LayouUtil.getDimen("h1");
//            contentMarginHorizontal = 5 * unit;
//            contentMarginTop = 4 * unit;
//            listTitleHeight = 6 * unit;
//            listTitlePadding = 3 * unit;
//            listTitleSize = (int) LayouUtil.getDimen("h6");
//            listContentHeight = 8 * unit;
//            listContentPadding = 5 * unit;
//            listContentSize = (int) LayouUtil.getDimen("h3");
//        }
        int unit = ViewParamsUtil.unit;
        titleHeight = 10 * unit;
        titleMarginHorizontal = 3 * unit;
        iconBackSide = 3 * unit;
        tvBackSize = ViewParamsUtil.h5;
        tvTitleSize = ViewParamsUtil.h1;
        contentMarginHorizontal = 5 * unit;
        contentMarginTop = 4 * unit;
        listTitleHeight = 6 * unit;
        listTitlePadding = 3 * unit;
        listTitleSize = ViewParamsUtil.h6;
        listContentHeight = 8 * unit;
        listContentPadding = 5 * unit;
        listContentSize = ViewParamsUtil.h3;
    }

    @Override
    public void onStart(String s) {

    }

    @Override
    public void onShow() {

    }

    @Override
    public void onDismiss() {
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mViewHolder != null) {
                if (mViewHolder.mTvBack == v) {
                    RecordWin2Manager.getInstance().doSelectCityClickCancel();
                }
            }
        }
    };

    //点击当前城市不触发更新页面
    AdapterView.OnItemClickListener onCurrCityItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                CityListAdapter.ItemViewHolder viewHolder = (CityListAdapter.ItemViewHolder) view.getTag();
                viewHolder.ivCitySelect.setVisibility(View.VISIBLE);
                view.setBackgroundColor(Color.rgb(16,174,255));
                RecordWin2Manager.getInstance().doSelectCityResult(viewHolder.tvCityName.getText().toString().trim());
            }catch (Exception e){

            }
        }
    };

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            try {
                CityListAdapter.ItemViewHolder viewHolder = (CityListAdapter.ItemViewHolder) view.getTag();
                viewHolder.ivCitySelect.setVisibility(View.VISIBLE);
                view.setBackgroundColor(Color.rgb(16,174,255));
                RecordWin2Manager.getInstance().doSelectCityResult(viewHolder.tvCityName.getText().toString().trim());
                WinLayout.isSearch = WinLayout.targetView != TXZRecordWinManager.RecordWin2.RecordWinController.TARGET_CONTENT_CHAT;
            }catch (Exception e){

            }
        }
    };


    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() *
                (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }


    public class CityListAdapter extends BaseAdapter {
        List mDisplayList;
        Context mContext;

        public CityListAdapter(Context context, List<String> displayList) {
            mContext = context;
            mDisplayList = displayList;
        }

        @Override
        public int getCount() {
            if (mDisplayList != null) {
                return mDisplayList.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mDisplayList != null && mDisplayList.size() > 0) {
                return mDisplayList.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ItemViewHolder itemViewHolder;
            if (convertView == null) {
                itemViewHolder = createItemViewHolder();
                convertView = itemViewHolder.mRootView;
                convertView.setTag(itemViewHolder);
            }else {
                itemViewHolder = (ItemViewHolder) convertView.getTag();
            }

            if (mDisplayList != null) {
                itemViewHolder.tvCityName.setText((String) mDisplayList.get(position));
            }
            return convertView;
        }

        public void updata(List<String> displayList) {
            mDisplayList = displayList;
        }


        class ItemViewHolder {
            RelativeLayout mRootView;
            TextView tvCityName;
            ImageView ivCitySelect;
        }

        private ItemViewHolder createItemViewHolder() {
            ItemViewHolder itemViewHolder = new ItemViewHolder();
            itemViewHolder.mRootView = new RelativeLayout(GlobalContext.get());
            itemViewHolder.mRootView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            //AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y80"));
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listContentHeight);
            itemViewHolder.mRootView.setLayoutParams(layoutParams);

            itemViewHolder.tvCityName = new TextView(GlobalContext.get());
            itemViewHolder.tvCityName.setFocusable(false);
            itemViewHolder.tvCityName.setGravity(Gravity.CENTER_VERTICAL);
            //itemViewHolder.tvCityName.setPadding((int) LayouUtil.getDimen("X60"),0,0,0);
            itemViewHolder.tvCityName.setPadding(listContentPadding,0,0,0);
            //itemViewHolder.tvCityName.setTextSize(LayouUtil.getDimen("y35"));
            //itemViewHolder.tvCityName.setTextColor(Color.parseColor("#FFFFFF"));
            TextViewUtil.setTextSize(itemViewHolder.tvCityName,listContentSize);
            TextViewUtil.setTextColor(itemViewHolder.tvCityName,listContentColor);
            RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            itemViewHolder.tvCityName.setLayoutParams(rlLayoutParams);
            itemViewHolder.mRootView.addView(itemViewHolder.tvCityName);

            itemViewHolder.ivCitySelect = new ImageView(GlobalContext.get());
            itemViewHolder.ivCitySelect.setFocusable(false);
            //itemViewHolder.ivCitySelect.setPadding(0,0,0, (int) LayouUtil.getDimen("x60"));
            itemViewHolder.ivCitySelect.setVisibility(View.GONE);
            itemViewHolder.ivCitySelect.setImageDrawable(LayouUtil.getDrawable("city_item_select"));
            rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            itemViewHolder.ivCitySelect.setLayoutParams(rlLayoutParams);
            itemViewHolder.mRootView.addView(itemViewHolder.ivCitySelect);

            return itemViewHolder;
        }
    }


    //测试数据
    private void getTestDate(){
        mSelectCityViewData = new SelectCityViewData();

        //当前城市
        List<String> curCityList = new ArrayList<>();
        curCityList.add("深圳");
        mSelectCityViewData.curCityList = curCityList;

        //目标城市
        List<String> tarCityList = new ArrayList<>();
        tarCityList.add("武汉");
        mSelectCityViewData.tarCityList = tarCityList;

        //推荐城市
        List<String> nomCityList = new ArrayList<>();
        nomCityList.add("东篱");
        nomCityList.add("南塘");
        nomCityList.add("西洲");
        nomCityList.add("北溟");
        mSelectCityViewData.nomCityList = nomCityList;

        //常驻城市
        List<String> perCityList = new ArrayList<>();
        perCityList.add("NULL");
//        perCityList.add("深圳");
        mSelectCityViewData.perCityList = perCityList;
    }

}
