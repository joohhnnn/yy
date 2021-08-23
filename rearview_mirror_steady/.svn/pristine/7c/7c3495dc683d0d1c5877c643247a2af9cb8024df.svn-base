package com.txznet.record.setting;

import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.txznet.txz.comm.R;

public class TXZShowDialog extends Dialog {

	private ArrayList<Object> mDatas;
	private String[] mStrDatas;
	private int layoutId;
	private Context mContext;
	private TxzDialogItemHolder mViewHolder;
	private String mDialogTitle;
	/**
	 * datas
	 * @param context
	 * @param datas
	 * @param id
	 */
	public TXZShowDialog(Context context,ArrayList<Object> datas,int id,TxzDialogItemHolder holder,String dialogtitle) {
		super(context);
		this.mDatas=datas;
		this.layoutId=id;
		this.mContext=context;
		this.mViewHolder=holder;
		this.mDialogTitle=dialogtitle;
		initView();
	}
	public TXZShowDialog(Context context,String[] datas,int id,TxzDialogItemHolder holder,String dialogtitle) {
		super(context);
		this.mStrDatas=datas;
		this.layoutId=id;
		this.mContext=context;
		this.mViewHolder=holder;
		this.mDialogTitle=dialogtitle;
		initView();
	}

	private TextView txtShowDialogTitle;
	private ListView lvShowDailogInfo;
	private TXZShowDialogAdapter mAdapter;
	private void initView(){
		View viewParent=LayoutInflater.from(mContext).inflate(R.layout.txz_dialog, null);
		txtShowDialogTitle=(TextView) viewParent.findViewById(R.id.txtDialogTitle);
		lvShowDailogInfo=(ListView) viewParent.findViewById(R.id.lv_txz_show_dialog);
		txtShowDialogTitle.setText(mDialogTitle);
		mAdapter=new TXZShowDialogAdapter();
		lvShowDailogInfo.setAdapter(mAdapter);
	}
	private class TXZShowDialogAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return mDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return mDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@SuppressWarnings("null")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TxzDialogItemHolder holder;
			DialogModule dialogModule=null;
			if(convertView==null){
				holder=mViewHolder;
				convertView=LayoutInflater.from(mContext).inflate(layoutId, parent,false);
				dialogModule.getModuleId(holder, convertView);
				convertView.setTag(holder);
			}else{
				holder=(TxzDialogItemHolder) convertView.getTag();
			}
			Object object=mDatas.get(position);
			dialogModule.setModuledata(holder, object);
			return convertView;
		}

		
		
	}
	
	//同行者自定义数据显示弹出框Item适配接口
	public static interface  DialogModule {
		/**
		 * 获取item布局组件
		 * @param holder
		 * @param view
		 */
		public  void getModuleId(TxzDialogItemHolder holder,View view);
		/**
		 * 给Item布局设置数据
		 * @param holder
		 * @param object
		 */
		public  void setModuledata(TxzDialogItemHolder holder, Object object);
	}
	class TxzDialogItemHolder extends Object{
		
	}
}
