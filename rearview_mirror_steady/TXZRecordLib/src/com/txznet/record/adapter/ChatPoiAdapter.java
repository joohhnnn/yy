package com.txznet.record.adapter;

import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;
import com.txznet.record.view.GradientProgressBar;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.util.LanguageConvertor;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatPoiAdapter extends ChatDisplayAdapter {

	public static class PoiItem extends DisplayItem<Poi> {
		public boolean mIsBus;
		public OnClickListener onClickListener;
	}
	private Integer mShowCount = null;
	public ChatPoiAdapter(Context context, List<PoiItem> displayList,int showCount) {
		super(context, displayList);
		mShowCount = showCount;	
	}
	
	private boolean mIsList = false;
	public void setIsList(boolean isList){
		mIsList=isList;
	}
	public ChatPoiAdapter(Context context, List<PoiItem> displayList) {
		super(context, displayList);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			if(mIsHistory){
				convertView = LayoutInflater.from(mContext).inflate(R.layout.poi_map_history_item, parent, false);
			}else if(mIsUseNewLayout){
				convertView = LayoutInflater.from(mContext).inflate(R.layout.poi_map_item_ly, parent, false);
			}else{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.poi_item_ly, parent, false);
			}
			
			prepareSetLayoutParams(convertView);
			if (mIsHistory) {
				RelativeLayout layoutdel = ViewHolder.get(convertView,
						R.id.rl_del);
				LayoutParams layoutParams = layoutdel.getLayoutParams();
				layoutParams.width = lastItemHeight;
				layoutdel.setLayoutParams(layoutParams);
			}
		}

		final PoiItem poiItem = (PoiItem) getItem(position);
		final Poi poi = poiItem.mItem;

		if(mIsHistory){
			TextView content = ViewHolder.get(convertView, R.id.txtContent);
			TextView desc= ViewHolder.get(convertView, R.id.txtDesc);
			RelativeLayout layoutdel = ViewHolder.get(convertView, R.id.rl_del);
			TextView numDel = ViewHolder.get(convertView, R.id.txtNum);	
			View mDivider = ViewHolder.get(convertView, R.id.divider);
			
			TextViewUtil.setTextSize(content,ViewConfiger.SIZE_POI_ITEM_SIZE1);
			TextViewUtil.setTextColor(content,ViewConfiger.COLOR_POI_ITEM_COLOR1);
			TextViewUtil.setTextSize(desc,ViewConfiger.SIZE_POI_ITEM_SIZE2);
			TextViewUtil.setTextColor(desc,ViewConfiger.COLOR_POI_ITEM_COLOR2);
			TextViewUtil.setTextSize(numDel,ViewConfiger.SIZE_POI_INDEX_SIZE1);
			TextViewUtil.setTextColor(numDel,ViewConfiger.COLOR_POI_INDEX_COLOR1);
			
			numDel.setText(String.valueOf(position + 1));
			String name = LanguageConvertor.toLocale(poi.getName());
			if(TextUtils.isEmpty(name)){
				content.setVisibility(View.GONE);
			}else{
				content.setText(LanguageConvertor.toLocale(poi.getName()));
			}
			String locale = LanguageConvertor.toLocale(poi.getGeoinfo());
			if(TextUtils.isEmpty(locale)){
				desc.setVisibility(View.GONE);
			}else{
				desc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));	
			}
						
			numDel.setText(String.valueOf(position + 1));
			numDel.setEnabled(true);
			numDel.setClickable(true);
			numDel.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(mTextClickListener != null){
						mTextClickListener.onClick(position);
					}
				}
			});
			
			layoutdel.setEnabled(true);
			layoutdel.setClickable(true);
			layoutdel.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(mHistoryDelClickListener != null){
						mHistoryDelClickListener.onClick(position);
					}
				}
			});
			
			mDivider.setVisibility(position == ConfigUtil.getVisbileCount() - 1 ? View.INVISIBLE : View.VISIBLE);
			
			return convertView;
		}
		GradientProgressBar mPb = ViewHolder.get(convertView, R.id.my_progress);
		View mDivider = ViewHolder.get(convertView, R.id.divider);
		RelativeLayout layoutTop = ViewHolder.get(convertView, R.id.rlTop);
		FrameLayout flDistanceDel = ViewHolder.get(convertView, R.id.flDistanceDel);
		FrameLayout flDistance = ViewHolder.get(convertView, R.id.flDistance);
		TextView contentDel = ViewHolder.get(convertView, R.id.txtContentDel);
		TextView desc = ViewHolder.get(convertView, R.id.txtDesc);
		TextView mCostTv = ViewHolder.get(convertView, R.id.cost_tv);
		TextView numDel =null;
		if(TextUtils.isEmpty(poi.getGeoinfo())){
			
			TextView txtDistanceDel = ViewHolder.get(convertView, R.id.txtDistance_del);
			numDel = ViewHolder.get(convertView, R.id.txtNum);	
			layoutTop.setVisibility(View.GONE);
			flDistance.setVisibility(View.GONE);
			desc.setVisibility(View.GONE);
			mCostTv.setVisibility(View.GONE);
			contentDel.setVisibility(View.VISIBLE);
			flDistanceDel.setVisibility(View.VISIBLE);
		
			TextViewUtil.setTextSize(numDel,ViewConfiger.SIZE_POI_INDEX_SIZE1);
			TextViewUtil.setTextColor(numDel,ViewConfiger.COLOR_POI_INDEX_COLOR1);
			TextViewUtil.setTextSize(contentDel,ViewConfiger.SIZE_POI_ITEM_SIZE1);
			TextViewUtil.setTextColor(contentDel,ViewConfiger.COLOR_POI_ITEM_COLOR1);
			TextViewUtil.setTextSize(txtDistanceDel,ViewConfiger.SIZE_POI_ITEM_SIZE2);
			TextViewUtil.setTextColor(txtDistanceDel,ViewConfiger.COLOR_POI_ITEM_COLOR2);
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)contentDel.getLayoutParams();
			layoutParams.addRule(RelativeLayout.LEFT_OF,  R.id.flDistanceDel);
			contentDel.setLayoutParams(layoutParams);
			double d = poiItem.mItem.getDistance() / 1000.0;
			if (d < 1) {
				txtDistanceDel.setText(poiItem.mItem.getDistance()+ "米");
			} else {
				txtDistanceDel.setText(String.format("%.1f", d) + "公里");
			}			
			numDel.setText(String.valueOf(position + 1));
			contentDel.setText(LanguageConvertor.toLocale(poi.getName()));
		}else{
			TextView txtDistance = ViewHolder.get(convertView, R.id.txtDistance);
			numDel = ViewHolder.get(convertView, R.id.txtNum);
			TextView content = ViewHolder.get(convertView, R.id.txtContent);
			
			ImageView mStarsIv = ViewHolder.get(convertView, R.id.star_grade_iv);
			ImageView mStarsIvIn = ViewHolder.get(convertView, R.id.star_grade_iv_in);
			ImageView mJuanIv = ViewHolder.get(convertView, R.id.juan_iv);
			ImageView mHuiIv = ViewHolder.get(convertView, R.id.hui_iv);
			ImageView mTuanIv = ViewHolder.get(convertView, R.id.tuan_iv);
			
			LinearLayout mMarkLayout = ViewHolder.get(convertView, R.id.mark_icon_ly);
			
			TextViewUtil.setTextSize(numDel,ViewConfiger.SIZE_POI_INDEX_SIZE1);
			TextViewUtil.setTextColor(numDel,ViewConfiger.COLOR_POI_INDEX_COLOR1);
			TextViewUtil.setTextSize(content,ViewConfiger.SIZE_POI_ITEM_SIZE1);
			TextViewUtil.setTextColor(content,ViewConfiger.COLOR_POI_ITEM_COLOR1);
			TextViewUtil.setTextSize(desc,ViewConfiger.SIZE_POI_ITEM_SIZE2);
			TextViewUtil.setTextColor(desc,ViewConfiger.COLOR_POI_ITEM_COLOR2);
			TextViewUtil.setTextSize(txtDistance,ViewConfiger.SIZE_POI_ITEM_SIZE2);
			TextViewUtil.setTextColor(txtDistance,ViewConfiger.COLOR_POI_ITEM_COLOR2);
			TextViewUtil.setTextSize(mCostTv,ViewConfiger.SIZE_POI_ITEM_SIZE2);
			TextViewUtil.setTextColor(mCostTv,ViewConfiger.COLOR_POI_ITEM_COLOR2);
			layoutTop.setVisibility(View.VISIBLE);
			flDistance.setVisibility(View.VISIBLE);
			desc.setVisibility(View.VISIBLE);
			mCostTv.setVisibility(View.VISIBLE);
			contentDel.setVisibility(View.GONE);
			flDistanceDel.setVisibility(View.GONE);
			if (poi instanceof BusinessPoiDetail) {
				mMarkLayout.setVisibility(View.VISIBLE);
				mCostTv.setVisibility(View.VISIBLE);
				mStarsIv.setVisibility(View.VISIBLE);
				mStarsIvIn.setVisibility(View.INVISIBLE);

				BusinessPoiDetail bpd = (BusinessPoiDetail) poi;
				double score = bpd.getScore();
				if (score < 1) {
					mStarsIv.setVisibility(View.GONE);
					mStarsIvIn.setVisibility(View.GONE);
				} else {
					int resId = getSoreMark(score);
					mStarsIv.setImageResource(resId);
					mStarsIvIn.setImageResource(resId);
				}

				if (bpd.isHasCoupon()) {
					mHuiIv.setVisibility(View.VISIBLE);
				} else {
					mHuiIv.setVisibility(View.GONE);
				}

				if (bpd.isHasDeal()) {
					mTuanIv.setVisibility(View.VISIBLE);
				} else {
					mTuanIv.setVisibility(View.GONE);
				}

				int price = (int) bpd.getAvgPrice();
				if (price > 0) {
					String txt = String.format("￥%d/人", price);
					mCostTv.setText(txt);
				} else {
					mCostTv.setVisibility(View.GONE);
				}
			} else {
				mMarkLayout.setVisibility(View.GONE);
				mCostTv.setVisibility(View.GONE);
				mStarsIv.setVisibility(View.GONE);
				mStarsIvIn.setVisibility(View.GONE);
			}

			// 设置显示距离
			double d = poiItem.mItem.getDistance() / 1000.0;
			if (d < 1) {
				txtDistance.setText(d * 1000 + "米");
			} else {
				txtDistance.setText(String.format("%.1f", d) + "公里");
			}
			
			numDel.setText(String.valueOf(position + 1));
			content.setText(LanguageConvertor.toLocale(poi.getName()));
			desc.setText(LanguageConvertor.toLocale(poi.getGeoinfo()));			
		}
//		LinearLayout llSetOut = ViewHolder.get(convertView, R.id.llSetOut);
//		TextView txtSetOut = ViewHolder.get(convertView, R.id.txtSetOut);


//		txtSetOut.setText(mContext.getResources().getString(R.string.activity_search_start_planing_text));
//		llSetOut.setTag(position);
//		llSetOut.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.right",
//						(position + "").getBytes(), null);
//			}
//		});
		numDel.setText(String.valueOf(position + 1));
		if(mTextClickListener != null){
			numDel.setEnabled(true);
			numDel.setClickable(true);
			numDel.setOnClickListener( new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					TextView textView = (TextView) v;				
					if(mTextClickListener != null){
						mTextClickListener.onClick(position);
					}
				}
			});			
		}else{
			numDel.setEnabled(false);
			numDel.setClickable(false);
		}
		
		mPb.setVisibility(poiItem.shouldWaiting ? View.VISIBLE : View.INVISIBLE);
		mPb.setProgress(poiItem.shouldWaiting ? poiItem.curPrg : 0);
		convertView.setTag(R.id.key_progress, mPb);

		mDivider.setVisibility((position == ConfigUtil.getVisbileCount() - 1 ? View.INVISIBLE : View.VISIBLE));
		
        if (position == mFocusIndex) {
        	convertView.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_focused));
		} else {
			convertView.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_nor));
		}

		return convertView;
	}

	private int getSoreMark(double score) {
		if (score < 1.0f) {
			return R.drawable.dz_icon_star0;
		} else if (score < 2.0f) {
			return R.drawable.dz_icon_star1;
		} else if (score < 3.0f) {
			return R.drawable.dz_icon_star2;
		} else if (score < 4.0f) {
			return R.drawable.dz_icon_star3;
		} else if (score < 5.0f) {
			return R.drawable.dz_icon_star4;
		} else if (score < 6.0f) {
			return R.drawable.dz_icon_star5;
		} else if (score < 7.0f) {
			return R.drawable.dz_icon_star6;
		} else if (score < 8.0f) {
			return R.drawable.dz_icon_star7;
		} else if (score < 9.0f) {
			return R.drawable.dz_icon_star8;
		} else if (score < 10.0f) {
			return R.drawable.dz_icon_star9;
		} else {
			return R.drawable.dz_icon_star10;
		}
	}
	
	private TextClickListener mTextClickListener = null;
	public interface TextClickListener{
		public void onClick(int index);
	}
	
	
	public void setNumberOnClickListener(TextClickListener listener){
		mTextClickListener = listener;
	}

	private TextClickListener mHistoryDelClickListener = null;
	public void setHistoryDelOnClickListener(TextClickListener listener){
		mHistoryDelClickListener = listener;
	}
	
	private int mListViewHeight = -1;
	public void setListViewHeight(int height){
		mListViewHeight = height;
	}
	
	private boolean  mIsUseNewLayout= false;
	public void setIsUseNewLayout(boolean use){
		mIsUseNewLayout = use;
	}
	
	private boolean mIsHistory = false;
	public void setIsHistoryLayout(boolean history){
		mIsHistory = history;
	}
	/**
	 * 设置listview item的高度，设置的LayoutParams是AbsListView.LayoutParams
	 * @param view
	 */
	protected void prepareSetLayoutParams(final View view) {
		if (ScreenUtil.getDisplayLvItemH(false) > 0) {
			LayoutParams lp = view.getLayoutParams();
			AbsListView.LayoutParams lParams;
			if(mShowCount ==null || mShowCount == -1){
				lastItemHeight =ScreenUtil.getDisplayLvItemH(false);
			}else{
				lastItemHeight = mListViewHeight/mShowCount;
			}

			if (lp == null) {
				lParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, lastItemHeight);
			}else {
				lParams = (AbsListView.LayoutParams) lp;
				lParams.width = LayoutParams.MATCH_PARENT;
				lParams.height = lastItemHeight;
			}
			view.setLayoutParams(lParams);
		}
	}
    public void update(int index,ListView listview,int preClick){
    	View view =null;
    	TextView view2  =null;
    	if(preClick != -1){
            view = listview.getChildAt(preClick);
            view2 = ViewHolder.get(view, R.id.txtNum);
            view2.setBackgroundResource(R.drawable.poi_item_circle_bg);    		
    	}
        view = listview.getChildAt(index);
        view2 = ViewHolder.get(view, R.id.txtNum);
		view2.setBackgroundResource(R.drawable.poi_item_circle_seleted_bg);
    }
	
}