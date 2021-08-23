package com.txznet.comm.ui.viewfactory.view.defaults;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.SelectCityViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ISearchEditView;
import com.txznet.comm.ui.viewfactory.view.ISelectCityView;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ASUS User on 2018/7/19.
 */

public class DefaultSelectCityView extends ISelectCityView {

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

    private static DefaultSelectCityView instance = new DefaultSelectCityView();

    public static DefaultSelectCityView getInstance() {
        return instance;
    }

    private DefaultSelectCityView() {

    }

    public void onStart(String mKey) {

    }

    private SelectCityViewData mSelectCityViewData;

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {

        mSelectCityViewData = (SelectCityViewData) data;
        ViewFactory.ViewAdapter adapter = new ViewFactory.ViewAdapter();
        adapter.view = createContentView();
        adapter.type = ViewData.TYPE_SELECT_CITY_VIEW;
        return adapter;
    }

    private View createContentView() {
        mViewHolder = new ViewHolder();
        LinearLayout layout = new LinearLayout(GlobalContext.get());
        mViewHolder.rootView = layout;
        layout.setBackgroundColor(Color.parseColor("#FF0A0A0A"));
        layout.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout rlTitle = new RelativeLayout(GlobalContext.get());
        rlTitle.setPadding((int) LayouUtil.getDimen("x30"), 0, (int) LayouUtil.getDimen("x60"), 0);
        LinearLayout.LayoutParams llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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
        ivBack.setImageDrawable(LayouUtil.getDrawable("button_back"));
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) LayouUtil.getDimen("y16"));
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlLayoutParams.rightMargin = (int) LayouUtil.getDimen("x20");
        ivBack.setId(ViewUtils.generateViewId());
        ivBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(ivBack);

        TextView tvBack = new TextView(GlobalContext.get());
        tvBack.setTextColor(Color.parseColor("#FFFFFF"));
        tvBack.setHintTextColor(Color.parseColor("#40454b"));
        tvBack.setTextSize(31);
        tvBack.setText("返回");
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        rlLayoutParams.addRule(RelativeLayout.RIGHT_OF, ivBack.getId());
        tvBack.setLayoutParams(rlLayoutParams);
        rlBack.addView(tvBack);

        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
        tvTitle.setHintTextColor(Color.parseColor("#40454b"));
        tvTitle.setTextSize(37);
        tvTitle.setText("选择搜索区域");
        tvTitle.setGravity(Gravity.CENTER);
        rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rlLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        tvTitle.setLayoutParams(rlLayoutParams);
        rlTitle.addView(tvTitle);

        ScrollView scrollView = new ScrollView(GlobalContext.get());
        scrollView.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        scrollView.setLayoutParams(llLayoutParams);
        layout.addView(scrollView);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setPadding(0, (int) LayouUtil.getDimen("y20"), 0, 0);
        llContent.setOrientation(LinearLayout.VERTICAL);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        llContent.setLayoutParams(llLayoutParams);
        scrollView.addView(llContent);

        TextView tvCurCity = new TextView(GlobalContext.get());
        mViewHolder.mCurCityTitle = tvCurCity;
        tvCurCity.setBackgroundColor(Color.parseColor("#40FFFFFF"));
        tvCurCity.setPadding((int) LayouUtil.getDimen("x40"), 0, 0, 0);
        tvCurCity.setTextSize(LayouUtil.getDimen("y27"));
        tvCurCity.setGravity(Gravity.CENTER_VERTICAL);
        tvCurCity.setVisibility(View.GONE);
        tvCurCity.setTextColor(Color.parseColor("#999999"));
        tvCurCity.setText("当前城市");
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y60"));
        tvCurCity.setLayoutParams(llLayoutParams);
        llContent.addView(tvCurCity);

        View view = new View(GlobalContext.get());
        view.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        view.setLayoutParams(llLayoutParams);
        llContent.addView(view);

        ListView lvCurCity = new ListView(GlobalContext.get());
        mViewHolder.mCurCityList = lvCurCity;
        lvCurCity.setBackgroundColor(Color.parseColor("#26FFFFFF"));
        lvCurCity.setDivider(new ColorDrawable(Color.parseColor("#1AFFFFFF")));
        lvCurCity.setDividerHeight(1);
        lvCurCity.setVisibility(View.GONE);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lvCurCity.setLayoutParams(llLayoutParams);
        llContent.addView(lvCurCity);

        TextView tvTarCity = new TextView(GlobalContext.get());
        mViewHolder.mTarCityTitle = tvTarCity;
        tvTarCity.setBackgroundColor(Color.parseColor("#40FFFFFF"));
        tvTarCity.setPadding((int) LayouUtil.getDimen("x40"), 0, 0, 0);
        tvTarCity.setTextSize(LayouUtil.getDimen("y27"));
        tvTarCity.setGravity(Gravity.CENTER_VERTICAL);
        tvTarCity.setVisibility(View.GONE);
        tvTarCity.setTextColor(Color.parseColor("#999999"));
        tvTarCity.setText("目标城市");
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y60"));
        tvTarCity.setLayoutParams(llLayoutParams);
        llContent.addView(tvTarCity);

        view = new View(GlobalContext.get());
        view.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        view.setLayoutParams(llLayoutParams);
        llContent.addView(view);

        ListView lvTarCity = new ListView(GlobalContext.get());
        mViewHolder.mTarCityList = lvTarCity;
        lvTarCity.setBackgroundColor(Color.parseColor("#26FFFFFF"));
        lvTarCity.setDivider(new ColorDrawable(Color.parseColor("#1AFFFFFF")));
        lvTarCity.setDividerHeight(1);
        lvTarCity.setVisibility(View.GONE);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lvTarCity.setLayoutParams(llLayoutParams);
        llContent.addView(lvTarCity);

        TextView tvPerCity = new TextView(GlobalContext.get());
        mViewHolder.mPerCityTitle = tvPerCity;
        tvPerCity.setBackgroundColor(Color.parseColor("#40FFFFFF"));
        tvPerCity.setPadding((int) LayouUtil.getDimen("x40"), 0, 0, 0);
        tvPerCity.setTextSize(LayouUtil.getDimen("y27"));
        tvPerCity.setGravity(Gravity.CENTER_VERTICAL);
        tvPerCity.setVisibility(View.GONE);
        tvPerCity.setTextColor(Color.parseColor("#999999"));
        tvPerCity.setText("常驻城市");
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y60"));
        tvPerCity.setLayoutParams(llLayoutParams);
        llContent.addView(tvPerCity);

        view = new View(GlobalContext.get());
        view.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        view.setLayoutParams(llLayoutParams);
        llContent.addView(view);

        ListView lvPerCity = new ListView(GlobalContext.get());
        mViewHolder.mPerCityList = lvPerCity;
        lvPerCity.setBackgroundColor(Color.parseColor("#26FFFFFF"));
        lvPerCity.setDivider(new ColorDrawable(Color.parseColor("#1AFFFFFF")));
        lvPerCity.setDividerHeight(1);
        lvPerCity.setVisibility(View.GONE);
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lvPerCity.setLayoutParams(llLayoutParams);
        llContent.addView(lvPerCity);

        view = new View(GlobalContext.get());
        view.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        view.setLayoutParams(llLayoutParams);
        llContent.addView(view);

        TextView tvNomCity = new TextView(GlobalContext.get());
        mViewHolder.mNomCityTitle = tvNomCity;
        tvNomCity.setBackgroundColor(Color.parseColor("#40FFFFFF"));
        tvNomCity.setPadding((int) LayouUtil.getDimen("x40"), 0, 0, 0);
        tvNomCity.setTextSize(LayouUtil.getDimen("y27"));
        tvNomCity.setGravity(Gravity.CENTER_VERTICAL);
        tvNomCity.setVisibility(View.GONE);
        tvNomCity.setTextColor(Color.parseColor("#999999"));
        tvNomCity.setText("推荐城市");
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y60"));
        tvNomCity.setLayoutParams(llLayoutParams);
        llContent.addView(tvNomCity);

        view = new View(GlobalContext.get());
        view.setBackgroundColor(Color.parseColor("#1AFFFFFF"));
        llLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2);
        view.setLayoutParams(llLayoutParams);
        llContent.addView(view);

        ListView lvNomCity = new ListView(GlobalContext.get());
        mViewHolder.mNomCityList = lvNomCity;
        lvNomCity.setBackgroundColor(Color.parseColor("#26FFFFFF"));
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
            mViewHolder.mCurCityList.setOnItemClickListener(onItemClickListener);
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
            mViewHolder.mTarCityList.setOnItemClickListener(onItemClickListener);
        } else {
            mViewHolder.mTarCityList.setVisibility(View.GONE);
            mViewHolder.mTarCityTitle.setVisibility(View.GONE);
        }


        return layout;

    }

    @Override
    public void init() {

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

    AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
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
            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y80"));
            itemViewHolder.mRootView.setLayoutParams(layoutParams);

            itemViewHolder.tvCityName = new TextView(GlobalContext.get());
            itemViewHolder.tvCityName.setTextColor(Color.parseColor("#FFFFFF"));
            itemViewHolder.tvCityName.setFocusable(false);
            itemViewHolder.tvCityName.setGravity(Gravity.CENTER_VERTICAL);
            itemViewHolder.tvCityName.setPadding((int) LayouUtil.getDimen("X60"),0,0,0);
            itemViewHolder.tvCityName.setTextSize(LayouUtil.getDimen("y35"));
            RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            itemViewHolder.tvCityName.setLayoutParams(rlLayoutParams);
            itemViewHolder.mRootView.addView(itemViewHolder.tvCityName);

            itemViewHolder.ivCitySelect = new ImageView(GlobalContext.get());
            itemViewHolder.ivCitySelect.setFocusable(false);
            itemViewHolder.ivCitySelect.setPadding(0,0,0, (int) LayouUtil.getDimen("x60"));
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
}
