package com.txznet.record.setting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.txznet.sdk.TXZConfigManager;
import com.txznet.txz.comm.R;

public class MyCommandAdapter extends BaseAdapter {

	Context mContext = null;

	/**
	 * 当前打开便捷的条目下标
	 */
	public int currentPosition = -1;

	public MyCommandAdapter(Context context) {
		this.mContext = context;
	}

	@Override
	public int getCount() {
		return ChangeCommandActivity.mCommands.size();
	}

	@Override
	public Object getItem(int position) {
		return ChangeCommandActivity.mCommands.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private boolean mEditable = true;

	public void setEditable(boolean editable) {
		mEditable = editable;
		notifyDataSetChanged();
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.layout_editcommand_item, parent, false);
			holder.command = (TextView) convertView.findViewById(R.id.txt_command);
			holder.imgbnt_openOperate = (ImageView) convertView.findViewById(R.id.imgbnt_openOperate);
			holder.layout_operate = (LinearLayout) convertView.findViewById(R.id.layout_operate);
			holder.bnt_edit = (FrameLayout) convertView.findViewById(R.id.bnt_edit);
			holder.bnt_delete = (FrameLayout) convertView.findViewById(R.id.bnt_delete);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.imgbnt_openOperate.setVisibility(mEditable ? View.VISIBLE : View.GONE);
		holder.imgbnt_openOperate.setClickable(mEditable);
		holder.command.setClickable(mEditable);

		String command = ChangeCommandActivity.mCommands.get(position);
		holder.command.setText(command);

		holder.layout_operate.setVisibility(currentPosition == position ? View.VISIBLE : View.GONE);
		holder.imgbnt_openOperate.setImageDrawable(currentPosition == position ? mContext.getResources().getDrawable(
				R.drawable.txz_item_up) : mContext.getResources().getDrawable(R.drawable.txz_item_down));

		holder.command.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openItem(position);
			}
		});
		holder.imgbnt_openOperate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openItem(position);
			}
		});
		holder.bnt_edit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ChangeCommandActivity.changeCommandDialog(ChangeCommandActivity.mCommands.get(position), mContext,
						"修改唤醒词", position);
			}
		});
		holder.bnt_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				synchronized (v) {
					if (ChangeCommandActivity.mCommands.size() <= 1) {
						Toast.makeText(mContext, "亲，唤醒词全部删除，不能唤醒哦", Toast.LENGTH_LONG).show();
					} else {
						ChangeCommandActivity.mCommands.remove(position);
						openItem(-1);
					}
				}
				TXZConfigManager.getInstance().setWakeupKeywordsNew(
						ChangeCommandActivity.mCommands.toArray(new String[ChangeCommandActivity.mCommands.size()]));
			}
		});
		return convertView;
	}

	protected void openItem(int position) {
		if (!mEditable) {
			return;
		}
		currentPosition = (currentPosition == position ? -1 : position);
		notifyDataSetChanged();
	}

	class ViewHolder {
		TextView command;
		ImageView imgbnt_openOperate;
		LinearLayout layout_operate;
		FrameLayout bnt_edit, bnt_delete;
	}
}
