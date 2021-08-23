package com.txznet.webchat.ui.car.adapter;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.webchat.BuildConfig;
import com.txznet.webchat.R;
import com.txznet.webchat.comm.plugin.base.WxPlugin;
import com.txznet.webchat.plugin.WxLoadedPluginInfo;
import com.txznet.webchat.plugin.WxPluginManager;
import com.txznet.webchat.stores.WxThemeStore;

/**
 * Created by J on 2016/10/17.
 */

public class HelpListAdapter extends BaseExpandableListAdapter {
    /**
     * 状态标识: 所有group都已经展开
     */
    int STAT_ALL_EXPANDED = 1;
    /**
     * 状态标识: 所有group都已经关闭
     */
    int STAT_ALL_COLLAPSED = 0;
    /**
     * 状态标识: 部分group展开
     */
    int STAT_MIX = 2;

    private Context mContext;
    private String[] mTipGroups;
    private String[][] mTipItems;
    private ExpandableListView mElvList;
    private boolean bPortraitTheme;

    private int mCurrentStat = STAT_ALL_COLLAPSED;

    // 方控焦点
    private int mFocusIndex = -1;
    // 用于方控焦点显示的背景图
    private int mBgGroup = R.drawable.shape_item_car_session_back_selected;
    private int mBgGroupExpanded = R.drawable.src_help_group_focus_expanded;
    private int mBgChild = R.drawable.src_help_child_focus;
    private int mBgChildLast = R.drawable.src_help_child_focus_last;

    private ExpandableListStateChangeListener mListener;

    private int[] mArrGroupStates;

    private String[] getStringArr(int id) {
        return mContext.getResources().getStringArray(id);
    }

    public HelpListAdapter(Context context) {
        mContext = context;
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

        mArrGroupStates = new int[getGroupCount()];
        bPortraitTheme = WxThemeStore.get().isPortraitTheme();
    }

    public void setStateChangeListener(ExpandableListStateChangeListener listener) {
        mListener = listener;
    }

    public boolean isAllGroupExpanded() {
        return mCurrentStat == STAT_ALL_EXPANDED;
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

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        GroupHolder groupHolder = null;
        if (null == convertView) {
            if (bPortraitTheme) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_car_help_group_portrait, null);
            } else {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_car_help_group, null);
            }

            groupHolder = new GroupHolder();
            groupHolder.mViewDivider = (View) convertView.findViewById(R.id.view_car_group_divider);
            groupHolder.mTvIndex = (TextView) convertView.findViewById(R.id.tv_car_help_group_index);
            groupHolder.mTvText = (TextView) convertView.findViewById(R.id.tv_car_help_group_text);
            groupHolder.mViewFocusIndicator = convertView.findViewById(R.id.view_group_focus_indicator);
            convertView.setTag(groupHolder);
        } else {
            groupHolder = (GroupHolder) convertView.getTag();
        }

        if (!isExpanded) {
            groupHolder.mViewDivider.setVisibility(View.VISIBLE);
        } else {
            groupHolder.mViewDivider.setVisibility(View.GONE);
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

        groupHolder.mTvIndex.setText((groupPosition + 1) + ". ");
        groupHolder.mTvText.setText(mTipGroups[groupPosition]);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        ItemHolder holder = null;

        if (null == convertView) {
            if (bPortraitTheme) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_car_help_item_portrait, null);
            } else {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_car_help_item, null);
            }

            holder = new ItemHolder();
            holder.mFlRoot = (FrameLayout) convertView.findViewById(R.id.fl_car_help_item_root);
            holder.mViewDivider = convertView.findViewById(R.id.view_car_help_item_divider);
            holder.mTvText = (TextView) convertView.findViewById(R.id.tv_car_help_item_text);
            holder.mViewFocusIndicator = convertView.findViewById(R.id.view_item_focus_indicator);
            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        if (isLastChild) {
            holder.mViewDivider.setVisibility(View.VISIBLE);
        } else {
            holder.mViewDivider.setVisibility(View.GONE);
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
        setGroupStat(groupPosition, true);
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
        setGroupStat(groupPosition, false);
    }

    private void setGroupStat(int position, boolean expanded) {
        mArrGroupStates[position] = expanded ? 1 : 0;

        // 检查所有group是否都已打开/关闭, 触发listener
        checkGroupStat();
    }

    private void checkGroupStat() {
        if (null == mListener) {
            return;
        }

        int stat = mArrGroupStates[0];

        for (int i = 1, len = mArrGroupStates.length; i < len; i++) {
            if (mArrGroupStates[i] != stat) {
                stat = 2;
                break;
            }
        }

        if (mCurrentStat != stat) {
            mCurrentStat = stat;
            mListener.expandStatChanged(mCurrentStat);
        }
    }

    @Override
    public long getCombinedChildId(long groupId, long childId) {
        return 0;
    }

    @Override
    public long getCombinedGroupId(long groupId) {
        return 0;
    }

    class GroupHolder {
        View mViewDivider;
        TextView mTvIndex;
        TextView mTvText;
        View mViewFocusIndicator;
    }

    class ItemHolder {
        FrameLayout mFlRoot;
        TextView mTvText;
        View mViewDivider;
        View mViewFocusIndicator;
    }

    public void setExpandableListView(ExpandableListView elv) {
        mElvList = elv;
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


    // 方控操作
    public boolean performNavOperation(int operation) {
        switch (operation) {
            case FocusSupporter.NAV_BTN_LEFT:
            case FocusSupporter.NAV_BTN_RIGHT:
                return false;

            case FocusSupporter.NAV_BTN_UP:
                return changeFocusPosition(-1, false);

            case FocusSupporter.NAV_BTN_DOWN:
                return changeFocusPosition(1, false);

            case FocusSupporter.NAV_BTN_NEXT:
                return changeFocusPosition(1, false);

            case FocusSupporter.NAV_BTN_PREV:
                return changeFocusPosition(-1, false);

            case FocusSupporter.NAV_BTN_CLICK:
                // 对于越界的焦点, 不做点击响应
                if (mFocusIndex < 0 || mFocusIndex > mTipGroups.length - 1) {
                    return true;
                }

                if (0 == mArrGroupStates[mFocusIndex]) {
                    mElvList.expandGroup(mFocusIndex, true);
                } else {
                    mElvList.collapseGroup(mFocusIndex);
                }
                return true;
        }

        return false;
    }

    private boolean changeFocusPosition(int change, boolean restrict) {
        boolean consume = true;
        mFocusIndex += change;

        if (mFocusIndex < 0) {
            mFocusIndex = 0;
            if (restrict) {

            } else {
                consume = false;
            }
        } else if (mFocusIndex > mTipGroups.length - 1) {
            /*if (restrict) {
                mFocusIndex = mTipGroups.length - 1;
            } else {
                consume = false;
            }*/

            // 帮助列表下面没有任何焦点View, 所以此处写死
            mFocusIndex = mTipGroups.length - 1;
            consume = true;
        }

        notifyDataSetChanged();
        mElvList.invalidateViews();

        return consume;
    }

    private int mLastFocusIndex;
    public void onNavGainFocus(Object rawFocus, int operation) {
        mFocusIndex = mLastFocusIndex;
        notifyDataSetChanged();
        mElvList.invalidateViews();
    }

    public void onNavLoseFocus(Object newFocus, int operation) {
        mLastFocusIndex = mFocusIndex;
        mFocusIndex = -1;
        notifyDataSetChanged();
        mElvList.invalidateViews();
    }

    public interface ExpandableListStateChangeListener {
        void expandStatChanged(int newStat);
    }
}
