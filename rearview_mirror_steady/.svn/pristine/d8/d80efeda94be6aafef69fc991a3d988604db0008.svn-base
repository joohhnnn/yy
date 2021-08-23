package com.txznet.alldemo.ui;

import java.util.List;

import com.txznet.rmtrecorddemo.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;


public class ActionListAdapter extends BaseAdapter{
	
    private List<AutoAction> mDataList;
    private Context mContext;
    
    public static class ViewHolder{
    	public Button mButton;
    }
    
    public ActionListAdapter(Context context, List<AutoAction> actionList) {
    	mContext = context;
        mDataList = actionList;			
		}
    
		@Override
		public int getCount() {
			return mDataList.size();
		}

		@Override
		public Object getItem(int position) {
			return mDataList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder; 
        if(convertView == null) { 
            holder = new ViewHolder(); 
            convertView = LayoutInflater.from(mContext).inflate(R.layout.list_item, null); 
            holder.mButton = (Button) convertView.findViewById(R.id.actionBtn); 
            convertView.setTag(holder); 
        }else { 
            holder = (ViewHolder)convertView.getTag(); 
        } 
        final int location = position;
        holder.mButton.setText(mDataList.get(position).getName());
        holder.mButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					mDataList.get(location).aciton();
				}
			});
        return convertView; 
		}
	}
	
