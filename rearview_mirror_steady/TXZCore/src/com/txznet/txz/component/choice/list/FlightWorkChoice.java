package com.txznet.txz.component.choice.list;

import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResFlightPage;
import com.txznet.txz.component.choice.page.ResListPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FlightWorkChoice extends WorkChoice<FlightWorkChoice.FlightDataBean, FlightWorkChoice.FlightItem> {

	public FlightWorkChoice(CompentOption<FlightItem> option) {
		super(option);
	}

	public static class FlightItem {
		public String airline;//航空公司
		public String flightNo;//航班编号
		public String departAirportName;//出发机场
		public String departTimeHm;//出发时间Hm格式
		public String departTime;//出发时间（2018-0814-14 22:10）格式
		public long departTimestamp;//出发时间
		public String arrivalAirportName;//到达机场
		public String arrivalTimeHm;//到达时间Hm格式
		public String arrivalTime;//到达时间（2018-0814-14 22:10）格式
		public long arrivalTimestamp;//到达时间
		public int economyCabinPrice;//经济舱价格
		public String economyCabinDiscount;//经济舱价格折扣
		public int ticketCount;//机票数量
		public String addDate;//跨天数
	}
	
	public static class FlightDataBean {
		public List<FlightItem> datas;
		public String departCity;
		public String arrivalCity;
		public String date;
		public String prefix;
	}

	@Override
	public String getReportId() {
		return "flight_select";
	}

	@Override
	public void showChoices(FlightDataBean dataBean) {
		if (dataBean == null || dataBean.datas == null) {
			return;
		}
		if(dataBean.datas.size() == 1){
			getOption().setTtsText(NativeData.getResString("RS_VOICE_FLIGHT_LIST_TIP_TTS"));
			dataBean.prefix = NativeData.getResString("RS_VOICE_FLIGHT_LIST_TIP_PREFIX");
		}else{
			getOption().setTtsText(NativeData.getResString("RS_VOICE_FLIGHT_LIST_TIPS_TTS"));
			dataBean.prefix = NativeData.getResString("RS_VOICE_FLIGHT_LIST_TIPS_PREFIX");
		}
		super.showChoices(dataBean);
	}

	@Override
	protected void onConvToJson(FlightDataBean dataBean, JSONBuilder jsonBuilder) {
		List<FlightItem> datas = dataBean.datas;
		jsonBuilder.put("type", RecorderWin.FlightSence);
		jsonBuilder.put("count", dataBean.datas.size());
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < datas.size(); i++) {
			FlightItem item = datas.get(i);
			JSONObject jsonItem = new JSONObject();
			try {
				jsonItem.put("airline", item.airline);
				jsonItem.put("flightNo", item.flightNo);
				jsonItem.put("departAirportName", item.departAirportName);
				jsonItem.put("departTimeHm", item.departTimeHm);
				jsonItem.put("departTime", item.departTime);
				jsonItem.put("departTimestamp", item.departTimestamp);
				jsonItem.put("arrivalAirportName", item.arrivalAirportName);
				jsonItem.put("arrivalTimeHm", item.arrivalTimeHm);
				jsonItem.put("arrivalTime", item.arrivalTime);
				jsonItem.put("arrivalTimestamp", item.arrivalTimestamp);
				jsonItem.put("economyCabinPrice", item.economyCabinPrice);
				jsonItem.put("economyCabinDiscount", item.economyCabinDiscount);
				jsonItem.put("ticketCount", item.ticketCount);
				jsonItem.put("addDate", item.addDate);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			jsonArray.put(jsonItem);
		}
		jsonBuilder.put("departCity", dataBean.departCity);
		jsonBuilder.put("arrivalCity", dataBean.arrivalCity);
		jsonBuilder.put("date", dataBean.date);
		jsonBuilder.put("flights", jsonArray);
		jsonBuilder.put("midfix", "为你找到");
		jsonBuilder.put("titlefix", dataBean.departCity + "-" + dataBean.arrivalCity + " " +dataBean.date);
		jsonBuilder.put("aftfix", "的航班信息");
		jsonBuilder.put("hideDrawable", true);
		jsonBuilder.put("vTips", getTips());
	}

	private String getTips(){
		String tips = "";
		if (mPage != null) {
			if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
				if (mPage.getCurrPageSize() == 1) {
					tips =  NativeData.getResString(mPage.getCurrPage() == 0 ? "RS_VOICE_TIPS_TICKET_ONE" : "RS_VOICE_TIPS_TICKET_ONE_LAST");
				} else if (mPage.getCurrPageSize() == 2) {
					tips = NativeData.getResString("RS_VOICE_TIPS_TICKET_TWO");
				} else {
					tips = NativeData.getResString("RS_VOICE_TIPS_TICKET_MORE");
				}
			} else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
				tips = NativeData.getResString("RS_VOICE_TIPS_TICKET_FIRST_PAGE");
			} else { //其他中间页
				tips = NativeData.getResString("RS_VOICE_TIPS_TICKET_OTHER_PAGE");
			}
		}
		return tips;
	}

	@Override
	protected String convItemToString(FlightItem item) {
		JSONObject jsonItem = new JSONObject();
		try {
			jsonItem.put("airline", item.airline);
			jsonItem.put("flightNo", item.flightNo);
			jsonItem.put("departAirportName", item.departAirportName);
			jsonItem.put("departTimeHm", item.departTimeHm);
			jsonItem.put("departTime", item.departTime);
			jsonItem.put("departTimestamp", item.departTimestamp);
			jsonItem.put("arrivalAirportName", item.arrivalAirportName);
			jsonItem.put("arrivalTimeHm", item.arrivalTimeHm);
			jsonItem.put("arrivalTime", item.arrivalTime);
			jsonItem.put("arrivalTimestamp", item.arrivalTimestamp);
			jsonItem.put("economyCabinPrice", item.economyCabinPrice);
			jsonItem.put("economyCabinDiscount", item.economyCabinDiscount);
			jsonItem.put("ticketCount", item.ticketCount);
			jsonItem.put("addDate", item.addDate);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return jsonItem.toString();
	}

	@Override
	protected void onSelectIndex(FlightItem item, boolean isFromPage,
			int idx, String fromVoice) {
		String text = item.departTimeHm + "由" + item.departAirportName + "飞往" + item.arrivalAirportName + "，剩余票数" + item.ticketCount + "张";
		speakWithTips(text);
	}

	@Override
	protected ResourcePage<FlightDataBean, FlightItem> createPage(
			FlightDataBean dataBean) {
		return new ResFlightPage(dataBean) {
			@Override
			protected int numOfPageSize() {
				return getOption().getNumPageSize();
			}
		};
	}
	
	
	
	@Override
	protected void onClearSelecting() {
		super.onClearSelecting();
	}
	
	@Override
	protected void onAddWakeupAsrCmd(AsrUtil.AsrComplexSelectCallback acsc, FlightDataBean dataBean) {
		getOption().setCanSure(false);
		if(dataBean != null && dataBean.datas != null && dataBean.datas.size() > 1){
			acsc.addCommand("SORT_PRICE","价格排序");
			acsc.addCommand("SORT_TIME", "时间排序");
			acsc.addCommand("SORT_LEFT", "有票的");
			addSelectCmd(acsc, dataBean);
			
			
		}else if(dataBean != null && dataBean.datas != null && dataBean.datas.size() == 1){
			getOption().setCanSure(true);
		}
		super.onAddWakeupAsrCmd(acsc, dataBean);
	}
	
	private int earliestIndex = -1;
	private int latestIndex = -1;
	private int cheapestIndex = -1;
	
	private void addSelectCmd(AsrUtil.AsrComplexSelectCallback acsc, FlightDataBean dataBean) {
		long earliestTime = 0;
		long latestTime = 0;
		int cheapestPrice = 0;
		earliestIndex = -1;
		latestIndex = -1;
		cheapestIndex = -1;
		for (int i = 0; i < mData.datas.size(); i++) {
			FlightItem item = mData.datas.get(i);
			if(item == null){
				continue;
			}
			if(earliestTime == 0 || earliestTime > item.departTimestamp){
				earliestTime = item.departTimestamp;
				earliestIndex = i;
			}
			if(latestTime == 0 || latestTime < item.departTimestamp){
				latestTime = item.departTimestamp;
				latestIndex = i;
			}
			if(cheapestPrice == 0 || cheapestPrice > item.economyCabinPrice){
				cheapestPrice = item.economyCabinPrice;
				cheapestIndex = i;
			}
		}
		if(earliestIndex != -1){
			acsc.addCommand("SORT_EARLIEST", "最早的");
		}
		if(latestIndex != -1){
			acsc.addCommand("SORT_LATEST", "最晚的");
		}
		if(cheapestIndex != -1){
			acsc.addCommand("SORT_CHEAPEST", "最便宜的");
		}
		LogUtil.logd("FlightWorkChoice insert command earliestIndex = " + earliestIndex + ", latestIndex = " + latestIndex + ", cheapestIndex = " + cheapestIndex);
	}

	@Override
	protected boolean onCommandSelect(String type, String command) {
		if("SORT_PRICE".equals(type)){
			sortListByComparator(command, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					try {
						FlightItem lItem = (FlightItem) o1;
						FlightItem rItem = (FlightItem) o2;
						if(lItem.economyCabinPrice > rItem.economyCabinPrice){
							return 1;
						}
						if(lItem.economyCabinPrice < rItem.economyCabinPrice){
							return -1;
						}
					}catch (Exception e){
					}
					return 0;
				}
			});
			return true;
		} else if("SORT_TIME".equals(type)){
			sortListByComparator(command, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					try {
						FlightItem lItem = (FlightItem) o1;
						FlightItem rItem = (FlightItem) o2;
						if(lItem.departTimestamp > rItem.departTimestamp){
							return 1;
						}
						if(lItem.departTimestamp < rItem.departTimestamp){
							return -1;
						}
					}catch (Exception e){
					}
					return 0;
				}
			});
			return true;
		} else if("SORT_LEFT".equals(type)){
			sortListByComparator(command, new Comparator<Object>() {
				@Override
				public int compare(Object o1, Object o2) {
					try {
						FlightItem lItem = (FlightItem) o1;
						FlightItem rItem = (FlightItem) o2;
						if(lItem.ticketCount <= 0){
							return 1;
						}
						if(rItem.ticketCount <= 0){
							return -1;
						}
						if(lItem.departTimestamp > rItem.departTimestamp){
							return 1;
						}
						if(lItem.departTimestamp < rItem.departTimestamp){
							return -1;
						}
					}catch (Exception e){
					}
					return 0;
				}
			});
			return true;
		} else if("SORT_EARLIEST".equals(type)) {
			int page = earliestIndex / mCompentOption.getNumPageSize() + 1;
			int index = earliestIndex % mCompentOption.getNumPageSize();
			selectPage(page, null);
			selectIndex(index, command);
		} else if("SORT_LATEST".equals(type)) {
			int page = latestIndex / mCompentOption.getNumPageSize() + 1;
			int index = latestIndex % mCompentOption.getNumPageSize();
			selectPage(page, null);
			selectIndex(index, command);
		} else if("SORT_CHEAPEST".equals(type)) {
			int page = cheapestIndex / mCompentOption.getNumPageSize() + 1;
			int index = cheapestIndex % mCompentOption.getNumPageSize();
			selectPage(page, null);
			selectIndex(index, command);
		}
		return super.onCommandSelect(type, command);
	}

	private void sortListByComparator(final String speech, Comparator<Object> comparator) {
		Collections.sort(mData.datas, comparator);
		// 更新数据
		refreshData(mData);
		if (isDelayAddWkWords()) {
			WinManager.getInstance().addViewStateListener(new IViewStateListener() {
				@Override
				public void onAnimateStateChanged(Animation animation, int state) {
					if (IViewStateListener.STATE_ANIM_ON_START != state) {
						return;
					}
					String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK");
					if (!TextUtils.isEmpty(sortSpk)) {
						sortSpk = sortSpk.replace("%SORTSLOT%", speech);
					}
					speakWithTips(sortSpk + "," + NativeData.getResString("RS_POI_SELECT_AGAIN_HINT"));
					WinManager.getInstance().removeViewStateListener(this);
				}
			});
			return;
		}
		String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK");
		if (!TextUtils.isEmpty(sortSpk)) {
			sortSpk = sortSpk.replace("%SORTSLOT%", speech);
		}
		speakWithTips(sortSpk + "," + NativeData.getResString("RS_POI_SELECT_AGAIN_HINT"));
	}


}
