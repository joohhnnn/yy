package com.txznet.music.ui.setting;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static com.txznet.music.ui.setting.BaseViewHolder.VIEW_TYPE_CHILD;
import static com.txznet.music.ui.setting.BaseViewHolder.VIEW_TYPE_PARENT;


/**
 * author：Drawthink
 * describe:
 * date: 2017/5/22
 * T :group  data
 * S :child  data
 * VH :ViewHolder
 */

public abstract class BaseRecyclerViewAdapter<T, S, VH extends BaseViewHolder> extends RecyclerView.Adapter<VH> {

    public static final String TAG = BaseRecyclerViewAdapter.class.getSimpleName();

    private static final int BASE_TYPE = 100;
    private Context ctx;
    /**
     * all data
     */
    private List<RecyclerViewData<T, S>> allDatas;
    /**
     * showing datas
     */
    private List showingDatas = new ArrayList<>();

    /**
     * child datas
     */
    private List<List<S>> childDatas;

    private OnRecyclerViewListener.OnItemClickListener itemClickListener;
    private OnRecyclerViewListener.OnItemLongClickListener itemLongClickListener;

    public void setOnItemClickListener(OnRecyclerViewListener.OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnRecyclerViewListener.OnItemLongClickListener longClickListener) {
        this.itemLongClickListener = longClickListener;
    }

    public BaseRecyclerViewAdapter(Context ctx, List<RecyclerViewData<T, S>> datas) {
        this.ctx = ctx;
        this.allDatas = datas;
        setShowingDatas();
        this.notifyDataSetChanged();
    }

    public void setAllDatas(List<RecyclerViewData<T, S>> allDatas) {
        this.allDatas = allDatas;
        setShowingDatas();
        this.notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return null == showingDatas ? 0 : showingDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        int type = BASE_TYPE;
        Object o = showingDatas.get(position);
        if (o instanceof GroupItem) {
            type *= VIEW_TYPE_PARENT;
            Object groupData = ((GroupItem) o).getGroupData();
            if (groupData instanceof IGetType) {
                type += ((IGetType) groupData).getStyle();
            }
        } else {
            type *= VIEW_TYPE_CHILD;
        }
        return type;
    }


    protected int getViewType(int viewType) {
        return viewType / BASE_TYPE;
    }

    protected boolean isParent(int viewType) {
        return getViewType(viewType) == VIEW_TYPE_PARENT;
    }

    protected boolean isChild(int viewType) {
        return getViewType(viewType) == VIEW_TYPE_CHILD;
    }

    /**
     * 获取数据自带的类型
     *
     * @param viewType
     * @return
     */
    protected int getDataType(int viewType) {
        return viewType % BASE_TYPE;
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType / BASE_TYPE) {
            case VIEW_TYPE_PARENT:
                view = getGroupView(parent, viewType % BASE_TYPE);
                break;
            case VIEW_TYPE_CHILD:
                view = getChildView(parent, viewType % BASE_TYPE);
                break;
        }
        return createRealViewHolder(ctx, view, viewType);
    }


    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {
        final Object item = showingDatas.get(position);
        final int gp = getGroupPosition(position);
        final int cp = getChildPosition(gp, position);
        if (item instanceof GroupItem) {
            onBindGroupHolder(holder, gp, position, (T) ((GroupItem) item).getGroupData());
            holder.groupView.setOnClickListener(v -> {
                if (null != itemClickListener) {
                    itemClickListener.onGroupItemClick(holder.getAdapterPosition(), gp, holder.groupView);
                }
                if (((GroupItem) item).isExpand()) {
                    collapseGroup(holder.getAdapterPosition());
                } else {
                    expandGroup(holder.getAdapterPosition());
                }
            });
            holder.groupView.setOnLongClickListener(v -> {
                if (null != itemLongClickListener) {
                    itemLongClickListener.onGroupItemLongClick(holder.getAdapterPosition(), gp, holder.groupView);
                }
                return true;
            });
        } else {
            onBindChildpHolder(holder, gp, cp, position, (S) item);
            holder.childView.setOnClickListener(v -> {
                if (null != itemClickListener) {
                    itemClickListener.onChildItemClick(holder.getAdapterPosition(), gp, cp, holder.childView);
                }
            });
            holder.childView.setOnLongClickListener(v -> {
                if (null != itemLongClickListener) {
                    int gp1 = getGroupPosition(position);
                    itemLongClickListener.onChildItemLongClick(holder.getAdapterPosition(), gp1, cp, holder.childView);
                }
                return true;
            });
        }
    }


    /**
     * setup showing datas
     */
    private void setShowingDatas() {
        if (null != showingDatas) {
            showingDatas.clear();
        }
        if (this.childDatas == null) {
            this.childDatas = new ArrayList<>();
        }
        childDatas.clear();
        GroupItem groupItem;
        for (int i = 0; i < allDatas.size(); i++) {
            if (allDatas.get(i).getGroupItem() instanceof GroupItem) {
                groupItem = allDatas.get(i).getGroupItem();
            } else {
                break;
            }
            childDatas.add(i, groupItem.getChildDatas());
            showingDatas.add(groupItem);
            if (null != groupItem && groupItem.hasChilds() && groupItem.isExpand()) {
                showingDatas.addAll(groupItem.getChildDatas());
            }
        }
    }

    /**
     * expandGroup
     *
     * @param position showingDatas position
     */
    private void expandGroup(int position) {
        Object item = showingDatas.get(position);
        if (null == item) {
            return;
        }
        if (!(item instanceof GroupItem)) {
            return;
        }
        if (((GroupItem) item).isExpand()) {
            return;
        }
        if (!canExpandAll()) {
            for (int i = 0; i < showingDatas.size(); i++) {
                if (i != position) {
                    int tempPositino = collapseGroup(i);
                    if (tempPositino != -1) {
                        position = tempPositino;
                    }
                }
            }
        }

        List<BaseItem> tempChilds;
        if (((GroupItem) item).hasChilds()) {
            tempChilds = ((GroupItem) item).getChildDatas();
            ((GroupItem) item).onExpand();
            if (canExpandAll()) {
                showingDatas.addAll(position + 1, tempChilds);
                notifyItemRangeInserted(position + 1, tempChilds.size());
                notifyItemRangeChanged(position + 1, showingDatas.size() - (position + 1));
            } else {
                int tempPsi = showingDatas.indexOf(item);
                showingDatas.addAll(tempPsi + 1, tempChilds);
                notifyItemRangeInserted(tempPsi + 1, tempChilds.size());
                notifyItemRangeChanged(tempPsi + 1, showingDatas.size() - (tempPsi + 1));
            }
        }
    }

    /**
     * collapseGroup
     *
     * @param position showingDatas position
     */
    private int collapseGroup(int position) {
        Object item = showingDatas.get(position);
        if (null == item) {
            return -1;
        }
        if (!(item instanceof GroupItem)) {
            return -1;
        }
        if (!((GroupItem) item).isExpand()) {
            return -1;
        }
        int tempSize = showingDatas.size();
        List<BaseItem> tempChilds;
        if (((GroupItem) item).hasChilds()) {
            tempChilds = ((GroupItem) item).getChildDatas();
            ((GroupItem) item).onExpand();
            showingDatas.removeAll(tempChilds);
            notifyItemRangeRemoved(position + 1, tempChilds.size());
            notifyItemRangeChanged(position + 1, tempSize - (position + 1));
            return position;
        }
        return -1;
    }

    /**
     * @param position showingDatas position
     * @return GroupPosition
     */
    private int getGroupPosition(int position) {
        Object item = showingDatas.get(position);
        if (item instanceof GroupItem) {
            for (int j = 0; j < allDatas.size(); j++) {
                if (allDatas.get(j).getGroupItem().equals(item)) {
                    return j;
                }
            }
        }
        for (int i = 0; i < childDatas.size(); i++) {
            if (childDatas.get(i).contains(item)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * @param groupPosition
     * @param showDataPosition
     * @return ChildPosition
     */
    private int getChildPosition(int groupPosition, int showDataPosition) {
        Object item = showingDatas.get(showDataPosition);
        try {
            return childDatas.get(groupPosition).indexOf(item);
        } catch (IndexOutOfBoundsException ex) {
            Log.e(TAG, ex.getMessage());
        }
        return 0;
    }

    public int getShowParentIndex(T t) {
        for (int i = 0; i < showingDatas.size(); i++) {
            Object item = showingDatas.get(i);
            if (item instanceof GroupItem) {
                if (((GroupItem) item).getGroupData().equals(t)) {
                    return i;
                }
            }
        }
        return -1;
    }


    /**
     * return groupView
     */
    public abstract View getGroupView(ViewGroup parent, int groupType);

    /**
     * return childView
     */
    public abstract View getChildView(ViewGroup parent, int childType);

    /**
     * return <VH extends BaseViewHolder> instance
     */
    public abstract VH createRealViewHolder(Context ctx, View view, int viewType);

    /**
     * onBind groupData to groupView
     *
     * @param holder
     * @param position
     */
    public abstract void onBindGroupHolder(VH holder, int groupPos, int position, T groupData);

    /**
     * onBind childData to childView
     *
     * @param holder
     * @param position
     */
    public abstract void onBindChildpHolder(VH holder, int groupPos, int childPos, int position, S childData);

    /**
     * if return true Allow all expand otherwise Only one can be expand at the same time
     */
    public boolean canExpandAll() {
        return true;
    }

    /**
     * 对原数据进行增加删除，调用此方法进行notify
     */
    public void notifyRecyclerViewData() {
        notifyDataSetChanged();
        setShowingDatas();
    }


}
