package com.txznet.comm.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.BaseAdapter;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.ListViewItemAnim;

public class ChatListViewAdapter extends BaseAdapter {

    List<View> mItemViews;

    public ChatListViewAdapter() {
        mItemViews = new ArrayList<View>();
    }

    @Override
    public int getCount() {
        return mItemViews.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mItemViews.get(position);
        boolean animation = ConfigUtil.getChatAnimation(true);
        boolean animationFirst = ConfigUtil.getChatAnimationAtFirst(true);

        if (position == 0) {
            if (animationFirst)
                startAnimation(position, view);
            LogUtil.logd("chat_list_animation_first::" + animationFirst);
        } else {
            if (animation) {
                startAnimation(position, view);
            }
            LogUtil.logd("chat_list_animation::" + animation);
        }
        return view;
    }

    public void addView(View view) {
        mItemViews.add(view);
        notifyDataSetChanged();
    }

    public void reset() {
        mItemViews = new ArrayList<View>();
        notifyDataSetChanged();
        isFrist.clear();
    }

    private Animation animation = ListViewItemAnim.getAnimationSet();

    private Map<Integer, Boolean> isFrist = new HashMap<Integer, Boolean>();


    private View lastAnimView;

    private void startAnimation(int position, View convertView) {
        if (lastAnimView != null) {
            lastAnimView.getAnimation().cancel();
        }
        // 如果是第一次加载该view，则使用动画
        if (isFrist.get(position) == null || isFrist.get(position)) {
            if (position == mItemViews.size() - 1) {
                convertView.startAnimation(animation);
                isFrist.put(position, false);
            }

        }
    }

	public void removeLastView() {
    	if (mItemViews.size() > 0) {
			mItemViews.remove(mItemViews.size() - 1);
			notifyDataSetChanged();
		}
	}
}
