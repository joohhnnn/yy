package com.txznet.feedback.ui;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.feedback.AppLogic;
import com.txznet.feedback.R;
import com.txznet.feedback.data.Message;

public class NoticeMsgAdapter extends BaseAdapter {
	private static final int TYPE_LEFT = 1;
	private static final int TYPE_RIGHT = 2;
	
	private List<Message> mMsgList = new ArrayList<Message>();
	
	public void setMsgList(List<Message> msgList){
		if(msgList == null){
			this.notifyDataSetChanged();
			return;
		}
		this.mMsgList = msgList;
		this.notifyDataSetChanged();
	}
	
	public void addMsg(Message msg){
		if(msg == null){
			return;
		}
		
		if(mMsgList == null){
			mMsgList = new ArrayList<Message>();
		}
		
		mMsgList.add(msg);
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mMsgList.size();
	}

	@Override
	public Object getItem(int position) {
		return mMsgList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public int getItemViewType(int position) {
		Message msg = mMsgList.get(position);
		if(msg == null){
			return TYPE_LEFT;
		}
		
		if(msg.type == Message.TYPE_NET){
			return TYPE_LEFT;
		}else {
			return TYPE_RIGHT;
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder mViewHolder = null;
		int viewType = getItemViewType(position);
		switch (viewType) {
		case TYPE_LEFT:
			mViewHolder = new ViewHolder();
			convertView = LayoutInflater.from(AppLogic.getApp()).inflate(
					R.layout.msg_left_layout, null);
			mViewHolder.mMsgTv = (TextView) convertView
					.findViewById(R.id.content_tv);
			break;

		case TYPE_RIGHT:
			mViewHolder = new ViewHolder();
			convertView = LayoutInflater.from(AppLogic.getApp()).inflate(
					R.layout.msg_right_layout, null);
			mViewHolder.mMsgTv = (TextView) convertView.findViewById(R.id.content_tv);
			mViewHolder.mNoteTv = (TextView) convertView.findViewById(R.id.note_tv);
			mViewHolder.mAnimIv = (ImageView) convertView.findViewById(R.id.anim_iv);
			
			convertView.setTag(R.id.anim, true);
			break;
		}
		
		Message msg = mMsgList.get(position);
		if(msg != null){
			if(msg.type == Message.TYPE_NET){
				mViewHolder.mMsgTv.setText(msg.msg);
			}else {
				// 音频时长
				String time = msg.note;
				mViewHolder.mNoteTv.setText(time);
			}
		}
		
		return convertView;
	}

	class ViewHolder {
		TextView mMsgTv;
		TextView mNoteTv;
		ImageView mAnimIv;
	}
}