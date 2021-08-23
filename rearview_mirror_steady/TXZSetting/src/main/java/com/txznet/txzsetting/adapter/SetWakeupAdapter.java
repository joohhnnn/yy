package com.txznet.txzsetting.adapter;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.txzsetting.R;

import java.util.List;

/**
 * Created by ASUS User on 2017/6/20.
 */

public class SetWakeupAdapter extends BaseAdapter {
    public static final String TAG = SetWakeupAdapter.class.getSimpleName();

    private TextView mShowWakeupName;
    private LinearLayout mListItemLayout;

    private LayoutInflater mInflater;
    private List<String> mListData;
    private Context mContext;


    public SetWakeupAdapter(Context context, List<String> datas) {
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mListData = datas;

    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int i) {
        return mListData.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View itemView = mInflater.inflate(R.layout.list_item_set_wakeup, null);
        mShowWakeupName = (TextView) itemView.findViewById(R.id.show_wakeup_name);
        Log.d(TAG,"mShowWakeupName = "+mListData.get(position));
        mShowWakeupName.setText(mListData.get(position));
        mListItemLayout = (LinearLayout) itemView.findViewById(R.id.layout_list_item2);
        if (position == mListData.size()-1) {
            mListItemLayout.setBackgroundResource(R.drawable.list_bg3);
//            setBackgroundOfVersion(mListItemLayout,R.drawable.list_bg1,mContext);
        }else {
            mListItemLayout.setBackgroundResource(R.drawable.list_bg2);
//            setBackgroundOfVersion(mListItemLayout,R.drawable.list_bg2,mContext);
        }



        return itemView;
    }

    /**
     * 因威仕特设备不支持此写法故废弃
     * 在API16以前使用setBackgroundDrawable，在API16以后使用setBackground
     * API16<---->4.1
     * @param view
     * @param resid
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setBackgroundOfVersion(View view, int resid, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //Android系统大于等于API16，使用setBackground
            view.setBackground(context.getDrawable(resid));
//            view.setBackgroundResource(resid);
        } else {
            //Android系统小于API16，使用setBackground
            view.setBackgroundResource(resid);
        }
    }
}
