package com.txznet.txz.component.choice.list;

import android.text.TextUtils;
import android.view.animation.Animation;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResTrainPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.ticket.TrainTicketData;
import com.txznet.txz.module.ticket.TrainTicketData.ResultBean.TicketListBean;
import com.txznet.txz.module.ticket.TrainTicketData.ResultBean.TicketListBean.TrainSeatsBean;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrainWorkChoice extends WorkChoice<TrainTicketData, TrainTicketData.ResultBean.TicketListBean> {

    public TrainWorkChoice(CompentOption<TrainTicketData.ResultBean.TicketListBean> option) {
        super(option);
    }

    @Override
    public void showChoices(TrainTicketData data) {
        if (getOption().getCanSure() == null) {
            boolean canSure = false;
            if (data.result.ticketList.size() ==1) {
                canSure = true;
            }
            getOption().setCanSure(canSure);
        }
        String strSpeakText = getSpeakText(data);
        if (getOption().getTtsText() == null) {
            getOption().setTtsText(strSpeakText);
        }
        super.showChoices(data);
    }

    private String getSpeakText(TrainTicketData data) {
        String spk;
        int size = data.result.ticketList.size();
        if (size == 1) {
            spk = NativeData.getResString("RS_TRAIN_TICKET_SELECT_SINGLE");
        } else {
            spk = NativeData.getResString("RS_TRAIN_TICKET_SELECT_MULTIPLE");
        }
        return spk;
    }

    @Override
    protected void onConvToJson(TrainTicketData ts, JSONBuilder jsonBuilder) {
        jsonBuilder.put("type", RecorderWin.TRAIN_SENCE);
        jsonBuilder.put("count", ts.result.ticketList.size());
        jsonBuilder.put("origin", ts.origin);
        jsonBuilder.put("destination", ts.destination);
        jsonBuilder.put("departDate", ts.departDate);
        jsonBuilder.put("departTime", ts.departTime);
        JSONObject result = new JSONObject();
		if (ts.result != null && ts.result.ticketList != null) {
			JSONArray jsonArray = new JSONArray();
			for (TicketListBean item : ts.result.ticketList) {
				JSONObject jsonItem = new JSONObject();
				try {
				jsonItem.put("departureStation", item.departureStation);
				jsonItem.put("departureTime", item.departureTime);
				jsonItem.put("arrivalStation", item.arrivalStation);
				jsonItem.put("arrivalTime", item.arrivalTime);
				jsonItem.put("daysApart", item.daysApart);
				jsonItem.put("journeyTime", item.journeyTime);
				jsonItem.put("trainNo", item.trainNo);
				if (item.trainSeats != null) {
					JSONArray trainSeatArray = new JSONArray();
					for (TrainSeatsBean seat : item.trainSeats) {
						JSONObject seatJson = new JSONObject();
						seatJson.put("isBookable", seat.isBookable);
						seatJson.put("price", seat.price);
						seatJson.put("seatName", seat.seatName);
						seatJson.put("seatType", seat.seatType);
						seatJson.put("ticketsRemainingNumer", seat.ticketsRemainingNumer);
						trainSeatArray.put(seatJson);
					}
					jsonItem.put("trainSeats", trainSeatArray);
				}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jsonArray.put(jsonItem);
			}
			try {
				result.put("ticketList", jsonArray);
			} catch (JSONException e) {
				e.printStackTrace();
			}
        }
        jsonBuilder.put("result", result);
        jsonBuilder.put("midfix", "为你找到");
        jsonBuilder.put("titlefix", ts.origin + "-" + ts.destination + " " + ts.getShowDate());
        jsonBuilder.put("aftfix", "的列车信息");
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
    protected String convItemToString(TrainTicketData.ResultBean.TicketListBean item) {
    	if (item == null) {
			return null;
		}
    	JSONObject jsonItem = new JSONObject();
		try {
		jsonItem.put("departureStation", item.departureStation);
		jsonItem.put("departureTime", item.departureTime);
		jsonItem.put("arrivalStation", item.arrivalStation);
		jsonItem.put("arrivalTime", item.arrivalTime);
		jsonItem.put("daysApart", item.daysApart);
		jsonItem.put("journeyTime", item.journeyTime);
		jsonItem.put("trainNo", item.trainNo);
		if (item.trainSeats != null) {
			JSONArray trainSeatArray = new JSONArray();
			for (TrainSeatsBean seat : item.trainSeats) {
				JSONObject seatJson = new JSONObject();
				seatJson.put("isBookable", seat.isBookable);
				seatJson.put("price", seat.price);
				seatJson.put("seatName", seat.seatName);
				seatJson.put("seatType", seat.seatType);
				seatJson.put("ticketsRemainingNumer", seat.ticketsRemainingNumer);
				trainSeatArray.put(seatJson);
			}
			jsonItem.put("trainSeats", trainSeatArray);
		}
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return jsonItem.toString();
    }

    @Override
    protected void onSelectIndex(TrainTicketData.ResultBean.TicketListBean item, boolean isFromPage, int idx, String fromVoice) {
    	// 18:23由福田站开往长沙南站，商务座X张、一等座X张、二等座X张、无座X张
        StringBuilder spk = new StringBuilder();
        if (item != null) {
        	LogUtil.logd("TrainWorkChoice::onSelectIndex index = " + idx + ", item = " + item.trainNo + ", fromVoice = " + fromVoice);
            spk.append(item.departureTime);
            spk.append("由");
            spk.append(item.departureStation);
            spk.append("站开往");
            spk.append(item.arrivalStation);
            spk.append("站");
            if (item.trainSeats != null && item.trainSeats.size() > 0) {
                spk.append("，");
                TrainTicketData.ResultBean.TicketListBean.TrainSeatsBean trainSeat;
                int size = item.trainSeats.size();
//            if (size > 4) {
//                size = 4;
//            }
                for (int i = 0; i < size - 1; i++) {
                    trainSeat = item.trainSeats.get(i);
                    spk.append(trainSeat.seatName);
                    if (trainSeat.ticketsRemainingNumer <= 0) {
                        spk.append("无");
                    } else {
                        spk.append(trainSeat.ticketsRemainingNumer);
                        spk.append("张");
                    }
                    spk.append("、");
                }
                trainSeat = item.trainSeats.get(size - 1);
                spk.append(trainSeat.seatName);
                if (trainSeat.ticketsRemainingNumer <= 0) {
                    spk.append("无");
                } else {
                    spk.append(trainSeat.ticketsRemainingNumer);
                    spk.append("张");
                }
            }
        }
//		TtsManager.getInstance().speakText(spk.toString(), PreemptType.PREEMPT_TYPE_IMMEADIATELY);
        speakWithTips(spk.toString());
    }

    @Override
    protected ResourcePage<TrainTicketData, TrainTicketData.ResultBean.TicketListBean> createPage(TrainTicketData sources) {
       final int allSize = sources.result.ticketList.size();
        return new ResTrainPage(sources) {
            @Override
            protected int numOfPageSize() {
                if (!is2_0Version()) {
                    return allSize;
                }
                return getOption().getNumPageSize();
            }
        };
    }

    @Override
    public String getReportId() {
        return "Train_Ticket_Select";
    }

    private int earliestIndex = -1;
    private int latestIndex = -1;
    private int cheapestIndex = -1;

    @Override
    protected void onAddWakeupAsrCmd(AsrUtil.AsrComplexSelectCallback acsc, TrainTicketData data) {
        super.onAddWakeupAsrCmd(acsc, data);
        TrainTicketData trainTicketData = mData; 
        if (trainTicketData.result.ticketList.size() == 0) {
        	return;
        }
        
        if (trainTicketData.result.ticketList.size() > 1) {
            acsc.addCommand("SORT_PRICE","价格排序");
            acsc.addCommand("SORT_TIME", "时间排序");
        }

        TrainTicketData.ResultBean.TicketListBean earliestTicket = trainTicketData.result.ticketList.get(0);
        TrainTicketData.ResultBean.TicketListBean latestTicket = trainTicketData.result.ticketList.get(0);
        double cheapestPrice = getMinPrice(trainTicketData.result.ticketList.get(0));

        earliestIndex = 0;
        latestIndex = 0;
        cheapestIndex = 0;
        for (int i = 1; i < trainTicketData.result.ticketList.size(); i++) {
            TrainTicketData.ResultBean.TicketListBean ticket = trainTicketData.result.ticketList.get(i);
            double price = getMinPrice(ticket);
            if (cheapestPrice > price) {
                cheapestIndex = i;
                cheapestPrice = price;
            }
            String departureTime = ticket.departureTime;
            if (TextUtils.isEmpty(departureTime)) {
                continue;
            }
            boolean hasSeat = hasTrainSeat(ticket);
            if (hasSeat) {
                acsc.addIndex(i, "有票的");
            }
            int earlyRet = compareTimeString(earliestTicket.departureTime, ticket.departureTime);
            if (earlyRet > 0 || earlyRet == 0 && hasSeat && !hasTrainSeat(earliestTicket)) {
                earliestIndex = i;
                earliestTicket = ticket;
            }
            int lastRet = compareTimeString(latestTicket.departureTime, ticket.departureTime);
            if (lastRet < 0 || lastRet == 0 && hasSeat && !hasTrainSeat(latestTicket)) {
                latestIndex = i;
                latestTicket = ticket;
            }
        }
        acsc.addCommand("SORT_EARLIEST", "最早的");
        acsc.addCommand("SORT_LATEST", "最晚的");
        acsc.addCommand("SORT_CHEAPEST", "最便宜的");
        LogUtil.logd("TrainWorkChoice::onAddWakeupAsrCmd" + "earliest:" + earliestIndex + ", latest:" + latestIndex + ", cheapest:" + cheapestIndex);
        // 高铁/快速/动车/直达
    }

    private boolean hasTrainSeat(TrainTicketData.ResultBean.TicketListBean ticket) {
        if (ticket == null || ticket.trainSeats == null) {
            return false;
        }
        for (TrainTicketData.ResultBean.TicketListBean.TrainSeatsBean trainSeat : ticket.trainSeats) {
            if (trainSeat.ticketsRemainingNumer > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean onCommandSelect(String type, String command) {
        if ("SORT_PRICE".equals(type)) {
            sortListByComparator(command, new Comparator<TrainTicketData.ResultBean.TicketListBean>() {
                @Override
                public int compare(TrainTicketData.ResultBean.TicketListBean o1, TrainTicketData.ResultBean.TicketListBean o2) {
                    double lPrice = getMinPrice(o1);
                    double rPrice = getMinPrice(o2);
                    if (lPrice > rPrice) {
                        return 1;
                    }
                    if (lPrice < rPrice) {
                        return -1;
                    }
                    return 0;
                }
            });
        } else if ("SORT_TIME".equals(type)) {
            sortListByComparator(command, new Comparator<TrainTicketData.ResultBean.TicketListBean>() {
                @Override
                public int compare(TrainTicketData.ResultBean.TicketListBean o1, TrainTicketData.ResultBean.TicketListBean o2) {
                    return compareTimeString(o1.departureTime, o2.departureTime);
                }
            });
        } else if ("SORT_EARLIEST".equals(type)) {
            int page = earliestIndex / mCompentOption.getNumPageSize() + 1;
            int index = earliestIndex % mCompentOption.getNumPageSize();
            selectPage(page, null);
            selectIndex(index, command);
        } else if ("SORT_LATEST".equals(type)) {
            int page = latestIndex / mCompentOption.getNumPageSize() + 1;
            int index = latestIndex % mCompentOption.getNumPageSize();
            selectPage(page, null);
            selectIndex(index, command);
        } else if ("SORT_CHEAPEST".equals(type)){
            int page = cheapestIndex / mCompentOption.getNumPageSize() + 1;
            int index = cheapestIndex % mCompentOption.getNumPageSize();
            selectPage(page, null);
            selectIndex(index, command);
        }
        return super.onCommandSelect(type, command);
    }

    private double getMinPrice(TrainTicketData.ResultBean.TicketListBean ticket) {
        double minPrice = Double.MAX_VALUE;
        if (ticket == null || ticket.trainSeats == null) {
            return minPrice;
        }

        for (TrainTicketData.ResultBean.TicketListBean.TrainSeatsBean trainSeat : ticket.trainSeats) {
            if (trainSeat.price <= Double.MIN_VALUE) {
                // 当票价为零是不做处理
                continue;
            }
            if (trainSeat.price < minPrice) {
                minPrice = trainSeat.price;
            }
        }
        return minPrice;
    }

    /**
     *
     * @param departureTime1
     * @param departureTime2
     * @return 返回 -1 表示 departureTime1 早于 departureTime2
     */
    private int compareTimeString(String departureTime1, String departureTime2) {
        // 时间有误排在最后面
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date date1;
        try {
            date1 = formatter.parse(departureTime1);
        } catch (ParseException e) {
            e.printStackTrace();
            return 1;
        }
        Date date2;
        try {
            date2 = formatter.parse(departureTime2);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
        if (date1.before(date2)) {
            return -1;
        }
        if (date1.after(date2)){
            return 1;
        }
        // 两个时间相等，有票的排前面
        return 0;

    }

    private void sortListByComparator(final String speech, Comparator<TrainTicketData.ResultBean.TicketListBean> comparator) {
        Collections.sort(mData.result.ticketList, comparator);
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
    
    @Override
    protected boolean onIndexSelect(final List<Integer> indexs, String command) {
    	 LogUtil.logd("TrainWorkChoice::onIndexSelect command:" + command + ", size:" + indexs.size());
    	if (indexs.size() != 1) {
    		ArrayList<TrainTicketData.ResultBean.TicketListBean> ticketList = new ArrayList<TrainTicketData.ResultBean.TicketListBean>(indexs.size());
    		for (Integer idx : indexs) {
    			if (idx == null) {
					continue;
				}
    			if (idx > 0 && idx < mData.result.ticketList.size()) {
					ticketList.add(mData.result.ticketList.get(idx));
				}
			}
    		mData.result.ticketList = ticketList;
    		Collections.sort(mData.result.ticketList, new Comparator<TrainTicketData.ResultBean.TicketListBean>() {
                @Override
                public int compare(TrainTicketData.ResultBean.TicketListBean o1, TrainTicketData.ResultBean.TicketListBean o2) {
                    return compareTimeString(o1.departureTime, o2.departureTime);
                }
            });
    		refreshData(mData);
    		if (isDelayAddWkWords()) {
				WinManager.getInstance().addViewStateListener(new IViewStateListener() {
					@Override
					public void onAnimateStateChanged(Animation animation, int state) {
						if (IViewStateListener.STATE_ANIM_ON_START != state) {
							return;
						}
						String mLastHintText = NativeData.getResString("RS_VOICE_MULTIPLE_SELECTOR")
								.replace("%NUM%", String.valueOf(indexs.size()));
						speakWithTips(mLastHintText);
						WinManager.getInstance().removeViewStateListener(this);
						super.onAnimateStateChanged(animation, state);
					}
				});
			} else {
				String mLastHintText = NativeData.getResString("RS_VOICE_MULTIPLE_SELECTOR").replace("%NUM%",
						String.valueOf(indexs.size()));
				speakWithTips(mLastHintText);
			}
    		return true;
		}
    	return false;
    }
}
