package com.txznet.record.ui;

import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.record.adapter.ChatAudioAdapter;
import com.txznet.record.adapter.ChatCarControlHomeAdapter;
import com.txznet.record.adapter.ChatConsAdapter;
import com.txznet.record.adapter.ChatDisplayAdapter;
import com.txznet.record.adapter.ChatFlightAdapter;
import com.txznet.record.adapter.ChatMovieTheaterAdapter;
import com.txznet.record.adapter.ChatMovieTimeAdapter;
import com.txznet.record.adapter.ChatMusicAdapter;
import com.txznet.record.adapter.ChatPoiAdapter;
import com.txznet.record.adapter.ChatReminderAdapter;
import com.txznet.record.adapter.ChatSimRechargeAdapter;
import com.txznet.record.adapter.ChatSimpleAdapter;
import com.txznet.record.adapter.ChatTrainAdapter;
import com.txznet.record.adapter.ChatTtsThemeAdapter;
import com.txznet.record.adapter.ChatWxContactListAdapter;
import com.txznet.record.adapter.CompetitionListAdapter;
import com.txznet.record.adapter.FlightTicketListAdapter;
import com.txznet.record.adapter.QiWuTicketPayAdapter;
import com.txznet.record.adapter.TrainTicketListAdapter;
import com.txznet.record.bean.AudioMsg;
import com.txznet.record.bean.BaseDisplayMsg;
import com.txznet.record.bean.ChatMessage;
import com.txznet.record.bean.CompetitionListMsg;
import com.txznet.record.bean.DisplayConMsg;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.FlightMsg;
import com.txznet.record.bean.FlightTicketMsg;
import com.txznet.record.bean.MiHomeMsg;
import com.txznet.record.bean.MovieTheaterMsg;
import com.txznet.record.bean.MovieTimeMsg;
import com.txznet.record.bean.MusicMsg;
import com.txznet.record.bean.PhoneConsMsg;
import com.txznet.record.bean.PoiMsg;
import com.txznet.record.bean.QiWuTickectPayMsg;
import com.txznet.record.bean.ReminderMsg;
import com.txznet.record.bean.SimRechargeMsg;
import com.txznet.record.bean.SimpleMsg;
import com.txznet.record.bean.TrainMsg;
import com.txznet.record.bean.TrainTicketMsg;
import com.txznet.record.bean.TtsThemeMsg;
import com.txznet.record.bean.WxContactMsg;
import com.txznet.record.keyevent.KeyEventManagerUI1;
import com.txznet.record.lib.R;
import com.txznet.record.view.DisplayLvRef;
import com.txznet.record.view.GradientProgressBar;
import com.txznet.record.view.WinPoiShow;

import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * ChatContentAdapter中嵌套的列表
 */
public class DisplayListRefresher {
	private DisplayLvRef mDisplayChatLv;
	private BaseDisplayMsg mLastMsg; // 上次会话的消息
	private ChatDisplayAdapter mDisplayAdapter; // 列表显示适配器

	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			JSONBuilder jb = new JSONBuilder();
			jb.put("index", position);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.selected",
					jb.toBytes(), null);
		}
	};

	private OnTouchListener onTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.list.ontouch",
					(event.getAction() + "").getBytes(), null);
			return false;
		}
	};

	public void initDisplayLv(com.txznet.record.view.DisplayLvRef displayLv) {
		this.mDisplayChatLv = displayLv;
	}

	public void release() {
		mDisplayAdapter = null;
		mDisplayChatLv.setAdapter(mDisplayAdapter = new ChatPoiAdapter(GlobalContext.getModified(), null,-1));
		mLastMsg = null;
	}
	
	public boolean procBySingleView(ChatMessage cm) {
		if (cm instanceof BaseDisplayMsg) {
			BaseDisplayMsg bdms = (BaseDisplayMsg) cm;
			if (bdms instanceof DisplayConMsg) {
				View view = ((DisplayConMsg) bdms).mConView;
				mDisplayChatLv.refreshTitleView(bdms);
				mDisplayChatLv.replaceView(view);
				if (view instanceof ListView) {
					KeyEventManagerUI1.getInstance().updateListView((ListView) view);
				}
				return true;
			}
			if (bdms instanceof PoiMsg && ((PoiMsg) bdms).mItemList != null && ((PoiMsg) bdms).mItemList.size() > 0) {
				WinPoiShow.getIntance().setData(bdms);
				if(mDisplayChatLv.poiMapVisible()){
					WinPoiShow.getIntance().updateView();
				}else{
					View poiMap = WinPoiShow.getIntance().creatView();
					mDisplayChatLv.replacePoiMapView(poiMap);
				}
				mDisplayChatLv.refreshTitleView(bdms);
//				mDisplayChatLv.setOnTouchListener(onTouchListener);
				final String city = bdms.mTitleInfo.cityfix;
				mDisplayChatLv.setOnTitleClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.tip",
								TextUtils.isEmpty(city)?null:city.getBytes(), null);
					}
				});
				
				mDisplayChatLv.setOnTitleCityClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.city",
								null, null);
					}
				});
				return true;
			}
			List mItems = bdms.mItemList;
			if (mItems == null || mItems.size() < 1) {
				KeyEventManagerUI1.getInstance().updateListView(null);
				return false;
			} else {
				boolean screenC = ScreenUtil.checkScreenSizeChangeForDisplay() || ScreenUtil.isDialogHeightChange();
				boolean same = false;
				if (mLastMsg != null && mLastMsg.type == cm.type) {
					same = true;
				}

				mDisplayChatLv.refreshTitleView(bdms);
				if (isListViewClickable(bdms)) {
                    mDisplayChatLv.setOnItemClickListener(mOnItemClickListener);
                    mDisplayChatLv.setOnTouchListener(onTouchListener);

				mDisplayChatLv.setOnTitleClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.tip",
								null, null);
					}
				});
                }

				if (mDisplayAdapter == null || screenC || !same
						|| (mDisplayAdapter.lastItemHeight != ScreenUtil.getDisplayLvItemH(false))) {
					mDisplayAdapter = buildAdapter(bdms, mItems);
					mDisplayChatLv.setAdapter(mDisplayAdapter);
				} else {
					mDisplayChatLv.refreshLists(mItems);
				}
				KeyEventManagerUI1.getInstance().updateListAdapter(mDisplayAdapter);
			}
			mLastMsg = bdms;
			return true;
		}
		return false;
	}

    private boolean isListViewClickable(BaseDisplayMsg baseDisplayMsg) {
        if (baseDisplayMsg instanceof MiHomeMsg) {
            return false;
        }
        return true;
    }

	private ChatDisplayAdapter buildAdapter(BaseDisplayMsg bdms, List mItems) {
		if (bdms instanceof PoiMsg) {
			return new ChatPoiAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof AudioMsg) {
			return new ChatAudioAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof WxContactMsg) {
			return new ChatWxContactListAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof MusicMsg) {
			return new ChatMusicAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof SimRechargeMsg) {
			return new ChatSimRechargeAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof TtsThemeMsg) {
			return new ChatTtsThemeAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof PhoneConsMsg) {
			return new ChatConsAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof SimpleMsg) {
			return new ChatSimpleAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof ReminderMsg) {
			return new ChatReminderAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof FlightMsg) {
			return new ChatFlightAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof TrainMsg) {
			return new ChatTrainAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof MovieTheaterMsg) {
			mDisplayChatLv.setOnItemClickListener(null);
			mDisplayChatLv.setOnTouchListener(null);
			return new ChatMovieTheaterAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof MovieTimeMsg) {
			mDisplayChatLv.setOnItemClickListener(null);
			mDisplayChatLv.setOnTouchListener(null);
			return new ChatMovieTimeAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof MiHomeMsg) {
			mDisplayChatLv.setOnItemClickListener(null);
			mDisplayChatLv.setOnTouchListener(null);
			return new ChatCarControlHomeAdapter(GlobalContext.getModified(), mItems);
		}
		if (bdms instanceof CompetitionListMsg) {
			// mDisplayChatLv.setOnItemClickListener(null);
			// mDisplayChatLv.setOnTouchListener(null);
			return new CompetitionListAdapter(GlobalContext.getModified(),mItems);
		}
		if(bdms instanceof QiWuTickectPayMsg){
			mDisplayChatLv.setOnItemClickListener(null);
			mDisplayChatLv.setOnTouchListener(null);
			return new QiWuTicketPayAdapter(GlobalContext.getModified(),mItems);
		}
		if(bdms instanceof FlightTicketMsg){
			mDisplayChatLv.setOnItemClickListener(null);
			mDisplayChatLv.setOnTouchListener(null);
			return new FlightTicketListAdapter(GlobalContext.getModified(),mItems);
		}
		if(bdms instanceof TrainTicketMsg){
			mDisplayChatLv.setOnItemClickListener(null);
			mDisplayChatLv.setOnTouchListener(null);
			return new TrainTicketListAdapter(GlobalContext.getModified(),mItems);
		}
		return mDisplayAdapter;
	}

	public void setDisplayVisible(int visible) {
		mDisplayChatLv.setVisibility(visible);
	}

	/**
	 * 刷新背景进度条
	 * 
	 * @param val
	 *            值
	 * @param selection
	 *            第几个
	 */
	public void updateProgress(int val, int selection) {
		if (mLastMsg != null) {
			if (mLastMsg instanceof BaseDisplayMsg) {
				BaseDisplayMsg bdm = (BaseDisplayMsg) mLastMsg;
				if (bdm.mItemList != null && bdm.mItemList.size() > 0) {
					DisplayItem di = (DisplayItem) bdm.mItemList.get(selection < 0 ? 0 : selection);
					if (val >= 0 && val <= 100) {
						di.curPrg = val;
						di.shouldWaiting = true;
					} else {
						di.shouldWaiting = false;
					}
					//
					// notifyDataSetChanged();
					notifyProgress(selection, val);
				}
			}
		}
	}

	public void notifyDataSetChanged() {
		if (mDisplayAdapter != null) {
			mDisplayAdapter.notifyDataSetChanged();
		}
	}
	
	private void notifyProgress(int index, int val) {
		ListView childView = mDisplayChatLv.getCurListView();
		if (childView != null) {
			View itemView = childView.getChildAt(index);
			GradientProgressBar pb = (GradientProgressBar) itemView.getTag(R.id.key_progress);
			if (val >= 0 && val <= 100) {
				if (pb.getVisibility() != View.VISIBLE) {
					pb.setVisibility(View.VISIBLE);
				}
				pb.setProgress(val);
			} else {
				if (pb.getVisibility() == View.VISIBLE) {
					pb.setVisibility(View.INVISIBLE);
				}
			}
		}
	}
}