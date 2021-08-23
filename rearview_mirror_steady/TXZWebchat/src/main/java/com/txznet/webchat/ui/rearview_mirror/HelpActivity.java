package com.txznet.webchat.ui.rearview_mirror;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.txznet.webchat.BuildConfig;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.TXZReportActionCreator;
import com.txznet.webchat.comm.plugin.base.WxPlugin;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.plugin.WxLoadedPluginInfo;
import com.txznet.webchat.plugin.WxPluginManager;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.ui.base.AppBaseActivity;

import butterknife.Bind;

/**
 * 帮助页面
 * Created by ASUS User on 2016/3/23.
 */
public class HelpActivity extends AppBaseActivity {
    @Bind(R.id.btn_help_back)
    ImageButton mBtnBack;
    @Bind(R.id.elv_help_list)
    ExpandableListView mElvList;

    private String[] mTipGroups;
    private String[][] mTipItems;

    private NavExpandableListAdapter mAdapter;

    @Override
    protected int getLayout() {
        return R.layout.activity_help;
    }

    @Override
    protected Store[] getRegisterStores() {
        return new Store[0];
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        if (WxConfigStore.getInstance().isBackButtonEnabled()) {
            mBtnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        } else {
            mBtnBack.setVisibility(View.GONE);
        }


        initTipData();
        initTipList();
    }

    @Override
    protected void initFocusViewList() {
        getNavBtnSupporter().setViewList(mElvList);
        getNavBtnSupporter().setCurrentFocus(mElvList);
    }

    @Subscribe
    @Override
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }

    private void initTipData() {
        mTipGroups = getStringArr(R.array.arr_help_tip_title);
        mTipItems = new String[][]{
                getStringArr(R.array.arr_help_subtitle_1),
                getStringArr(R.array.arr_help_subtitle_2),
                getStringArr(R.array.arr_help_subtitle_3),
                getStringArr(R.array.arr_help_subtitle_4),
                getStringArr(R.array.arr_help_subtitle_5),
                getStringArr(R.array.arr_help_subtitle_6),
                getStringArr(R.array.arr_help_subtitle_7),
                getStringArr(R.array.arr_help_subtitle_8),
                getStringArr(R.array.arr_help_subtitle_9),
                getStringArr(R.array.arr_help_subtitle_10),
                getStringArr(R.array.arr_help_subtitle_11),
                generateAboutInfo(),
        };
    }

    private String[] generateAboutInfo() {
        WxLoadedPluginInfo[] arrPlugin = WxPluginManager.getInstance().getPluginList();
        String[] arrInfo = new String[]{
                "当前版本: " + BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")_" +
                        BuildConfig.SVN_VERSION,
                "插件信息：",
        };

        // 添加插件信息
        for (int i = 0, len = arrPlugin.length; i < len; i++) {
            WxPlugin plugin = arrPlugin[i].getPlugin();
            arrInfo[i + 1] = plugin.getToken() + ": " + plugin.getVersionName()
                    + "(" + plugin.getVersionCode() + ")";
        }

        return arrInfo;
    }

    private String[] getStringArr(int id) {
        return getResources().getStringArray(id);
    }

    private void initTipList() {
        mAdapter = new NavExpandableListAdapter();
        mElvList.setAdapter(mAdapter);
        mElvList.setDividerHeight(0);
        mElvList.setGroupIndicator(null);
        mElvList.setSelector(R.drawable.selector_none);

        mElvList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition,
                                        long id) {
                if (!parent.isGroupExpanded(groupPosition)) {
                    TXZReportActionCreator.getInstance().reportHelpItemClick
                            (mTipGroups[groupPosition]);
                }
                return false;
            }
        });
    }

    public class NavExpandableListAdapter extends BaseExpandableListAdapter {
        Context mContext = HelpActivity.this;

        // 方控焦点
        private int mFocusIndex = -1;

        // 用于方控焦点显示的背景图
        private int mBgGroup = R.drawable.shape_item_car_session_back_selected;
        private int mBgGroupExpanded = R.drawable.src_help_group_focus_expanded;
        private int mBgChild = R.drawable.src_help_child_focus;
        private int mBgChildLast = R.drawable.src_help_child_focus_last;

        private int[] mArrGroupStates = new int[getGroupCount()];
        private View[] mArrGroupViews = new View[getGroupCount()];

        public NavExpandableListAdapter() {
            super();
            if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
                mFocusIndex = 0;
            }
        }

        @Override
        public void registerDataSetObserver(DataSetObserver observer) {
            super.registerDataSetObserver(observer);
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            super.unregisterDataSetObserver(observer);
        }

        @Override
        public int getGroupCount() {
            return mTipGroups.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return mTipItems[groupPosition].length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mTipGroups[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mTipItems[groupPosition][childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            mArrGroupViews[groupPosition] = convertView;
            GroupHolder groupHolder = null;
            if (null == convertView) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_help_group,
                        null);
                groupHolder = new GroupHolder();
                groupHolder.mFlRoot = (FrameLayout) convertView.findViewById(R.id.fl_group_root);
                groupHolder.mIvIndicator = (ImageView) convertView.findViewById(R.id
                        .iv_group_indicator);
                groupHolder.mViewDivider = convertView.findViewById(R.id.view_group_divider);
                groupHolder.mTvIndex = (TextView) convertView.findViewById(R.id.tv_group_index);
                groupHolder.mTvText = (TextView) convertView.findViewById(R.id.tv_group_text);
                groupHolder.mViewFocusIndicator = convertView.findViewById(R.id
                        .view_group_focus_indicator);
                convertView.setTag(groupHolder);
            } else {
                groupHolder = (GroupHolder) convertView.getTag();
            }

            if (!isExpanded) {
                groupHolder.mFlRoot.setBackgroundResource(R.drawable.shape_help_group_back);
                groupHolder.mIvIndicator.setRotation(0);
                groupHolder.mViewDivider.setVisibility(View.GONE);
            } else {
                groupHolder.mFlRoot.setBackgroundResource(R.drawable
                        .shape_help_group_back_expanded);
                groupHolder.mIvIndicator.setRotation(180);
                groupHolder.mViewDivider.setVisibility(View.VISIBLE);
            }

            // 设置导航按键焦点显示
            if (groupPosition == mFocusIndex) {
                groupHolder.mViewFocusIndicator.setVisibility(View.VISIBLE);
                if (isExpanded) {
                    groupHolder.mViewFocusIndicator.setBackgroundResource(mBgGroupExpanded);
                } else {
                    groupHolder.mViewFocusIndicator.setBackgroundResource(mBgGroup);
                }
            } else {
                groupHolder.mViewFocusIndicator.setVisibility(View.GONE);
            }

            groupHolder.mTvIndex.setText((groupPosition + 1) + "");
            groupHolder.mTvText.setText(mTipGroups[groupPosition]);
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View
                convertView, ViewGroup parent) {
            ItemHolder holder = null;

            if (null == convertView) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_help_item,
                        null);
                holder = new ItemHolder();
                holder.mFlRoot = (FrameLayout) convertView.findViewById(R.id.fl_item_root);
                holder.mTvText = (TextView) convertView.findViewById(R.id.tv_item_text);
                holder.mViewFocusIndicator = convertView.findViewById(R.id
                        .view_item_focus_indicator);
                convertView.setTag(holder);
            } else {
                holder = (ItemHolder) convertView.getTag();
            }

            if (childPosition == 0) {
                holder.mFlRoot.setPadding(0, (int) getResources().getDimension(R.dimen.y10), 0, 0);
            }

            if (isLastChild) {
                holder.mFlRoot.setBackgroundResource(R.drawable.shape_help_item_back_last);
                holder.mFlRoot.setPadding(0, 0, 0, (int) getResources().getDimension(R.dimen.y10));
            } else {
                holder.mFlRoot.setBackgroundColor(0xff282828);
                holder.mFlRoot.setPadding(0, (int) getResources().getDimension(R.dimen.y10), 0, 0);
            }

            // 设置导航按键
            if (groupPosition == mFocusIndex) {
                holder.mViewFocusIndicator.setVisibility(View.VISIBLE);
                if (isLastChild) {
                    holder.mViewFocusIndicator.setBackgroundResource(mBgChildLast);
                } else {
                    holder.mViewFocusIndicator.setBackgroundResource(mBgChild);
                }
            } else {
                holder.mViewFocusIndicator.setVisibility(View.GONE);
            }

            String str = mTipItems[groupPosition][childPosition];
            holder.mTvText.setText(str);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void onGroupExpanded(int groupPosition) {
            mArrGroupStates[groupPosition] = 1;
        }

        @Override
        public void onGroupCollapsed(int groupPosition) {
            mArrGroupStates[groupPosition] = 0;
        }

        @Override
        public long getCombinedChildId(long groupId, long childId) {
            return 0;
        }

        @Override
        public long getCombinedGroupId(long groupId) {
            return 0;
        }

        // 方控操作
        public int performNext() {
            if (mFocusIndex < mTipGroups.length - 1) {
                mFocusIndex++;
            } else {
                mFocusIndex = mTipGroups.length - 1;
            }

            return mFocusIndex;
        }

        public int performPrev() {
            if (mFocusIndex > 0) {
                mFocusIndex--;
            } else {
                mFocusIndex = 0;
            }

            return mFocusIndex;
        }

        /**
         * 获取当前焦点Group中最后一个child的position
         * 当前的方控交互, 方控焦点的切换是以Group为单位的, 但ExpandableListView在计算滚动位置时会将已展开的
         * child item列入计算位置, 所以为保证滚动后已展开的Group的所有child能完整显示, 应将targetPosition设置
         * 为最后一个child的实际位置
         *
         * @return
         */
        public int getShiftedCurrentFocusPosition() {
            if (mFocusIndex < 0) {
                return 0;
            }

            int result = 0;
            for (int i = 0; i < mFocusIndex; i++) {
                if (1 == mArrGroupStates[i]) {
                    result += getChildrenCount(i);
                }
                result += 1;
            }

            // 若当前Group已展开, 将按child数量偏移
            if (1 == mArrGroupStates[mFocusIndex]) {
                result += getChildrenCount(mFocusIndex);
            }

            return result;
        }

        /**
         * 获取当前焦点Group的实际position
         *
         * @return
         */
        public int getShiftedGroupFocusPosition() {
            int result = 0;
            for (int i = 0; i < mFocusIndex; i++) {
                if (1 == mArrGroupStates[i]) {
                    result += getChildrenCount(i);
                }
                result += 1;
            }

            return result;
        }

        public void performClick() {
            if (0 == mArrGroupStates[mFocusIndex]) {
                mElvList.expandGroup(mFocusIndex, true);
            } else {
                mElvList.collapseGroup(mFocusIndex);
            }
        }

        class GroupHolder {
            FrameLayout mFlRoot;
            View mViewDivider;
            ImageView mIvIndicator;
            TextView mTvIndex;
            TextView mTvText;
            View mViewFocusIndicator;
        }

        class ItemHolder {
            FrameLayout mFlRoot;
            TextView mTvText;
            View mViewFocusIndicator;
        }
    }
}
