package com.txznet.txz.component.choice.list;

import android.text.TextUtils;

import com.txz.ui.data.UiData;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.viewfactory.data.QiWuFlightTicketData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResFlightTicketPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.component.ticket.TicketPriceComparator;
import com.txznet.txz.component.ticket.TicketTimeComparator;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.asr.InterruptTts;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.ticket.QiWuTicketManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.widget.TicketUseInfoDialog;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FlightTicketWorkChioce extends WorkChoice<QiWuFlightTicketData, QiWuFlightTicketData.FlightTicketBean> {

    public FlightTicketWorkChioce(CompentOption<QiWuFlightTicketData.FlightTicketBean> option) {
        super(option);
    }

    @Override
    protected void onConvToJson(QiWuFlightTicketData ts, JSONBuilder jsonBuilder) {
        jsonBuilder.put("type", RecorderWin.FLIGHT_TICKET);
        String title = ts.departureCity+"-"+ts.arrivalCity+"  "+ts.getShowDate();
        //jsonBuilder.put("titlefix", ts.departureCity + "-" + ts.arrivalCity + " " + ts.getShowDate());
        jsonBuilder.put("prefix", title);
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < ts.mFlightTicketBeans.size(); i++){
            QiWuFlightTicketData.FlightTicketBean fb = ts.mFlightTicketBeans.get(i);
            JSONObject obj = new JSONBuilder().put("addDate", fb.addDate).put("airline", fb.airline).
                    put("arrivalAirportName", fb.arrivalAirportName).
                    put("arrivalAirportCode", fb.arrivalAirportCode).
                    put("arrivalTime", fb.arrivalTime).
                    put("departTime", fb.departureTime).
                    put("departAirportName", fb.departAirportName).
                    put("departAirportCode", fb.departAirportCode).
                    put("cabinPrice", fb.recommendPrice).
                    put("cabin", fb.recommendSeat).
                    put("seatCode", fb.seatCode).
                    put("departDate", fb.departDate).
                    put("addDate", fb.addDate).
                    put("number", fb.flightNo).build();
            jsonArray.put(obj);
        }
        jsonBuilder.put("cines", jsonArray);
        jsonBuilder.put("count", jsonArray.length());
        jsonBuilder.put("vTips",getTips());
    }

    @Override
    protected String convItemToString(QiWuFlightTicketData.FlightTicketBean fb) {
        JSONObject obj = new JSONBuilder().put("addDate", fb.addDate).put("airline", fb.airline).
                put("arrivalAirportName", fb.arrivalAirportName).
                put("arrivalAirportCode", fb.arrivalAirportCode).
                put("arrivalTime", fb.arrivalTime).put("departTime", fb.departureTime).
                put("departAirportName", fb.departAirportName).
                put("departAirportCode", fb.departAirportCode).
                put("cabinPrice", fb.recommendPrice).
                put("cabin", fb.recommendSeat).
                put("seatCode", fb.seatCode).
                put("departDate", fb.departDate).
                put("addDate", fb.addDate).
                put("number", fb.flightNo).build();
        return obj.toString();
    }

    @Override
    protected void onSelectIndex(final QiWuFlightTicketData.FlightTicketBean item, boolean isFromPage, int idx, String fromVoice) {
        AppLogicBase.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                TicketUseInfoDialog dialog = TicketUseInfoDialog.getInstance();
                dialog.setAfterDissmiss(new TicketUseInfoDialog.AfterDissmiss() {
                    @Override
                    public void afterDissmiss() {
                        ChoiceManager.getInstance().showFlightTicketList(mData, null);
                    }
                });
                JSONObject ticeketInfo = new JSONObject();
                try {
                    ticeketInfo.put("seatCode",item.seatCode);
                    ticeketInfo.put("ticketNum", item.flightNo);
                    ticeketInfo.put("departDate",item.departDate);
                    ticeketInfo.put("departureCity",mData.departureCity);
                    ticeketInfo.put("arrivalCity", mData.arrivalCity);
                    ticeketInfo.put("arrivalAirportCode",item.arrivalAirportCode);
                    ticeketInfo.put("departAirportCode",item.departAirportCode);
                    ticeketInfo.put("departDate",item.departDate);
                    ticeketInfo.put("departureTime",item.departureTime);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.setTicketInfoJson(ticeketInfo.toString());
                dialog.setSeatLevel(item.seatCode);
                dialog.setSeatName(item.recommendSeat);
                dialog.setTicketType(TicketUseInfoDialog.TicketType.Flight);
                UiData.TTime currentTime = NativeData.getMilleServerTime();
                QiWuTicketManager.currentSelectIndexId = ""+currentTime.uint64Time;
                dialog.setCurrentSelectIndexId(QiWuTicketManager.currentSelectIndexId);
            }
        });
        UiEquipment.Req_NLP pushCmdQRResults = new UiEquipment.Req_NLP();
        JSONObject reqJson = new JSONObject();
        try {
            reqJson.put("uid", NativeData.getUID());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        pushCmdQRResults.strJson = reqJson.toString().getBytes();
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_QIWU_GET_USER_INFO, pushCmdQRResults);
        //dialog.show();
        clearIsSelecting();
    }

    @Override
    protected boolean onIndexSelect(final List<Integer> indexs, final String command) {
        if (indexs.size() != 1) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    ArrayList<QiWuFlightTicketData.FlightTicketBean> data = new ArrayList<QiWuFlightTicketData.FlightTicketBean>();
                    for (Integer idx : indexs) {
                        if (idx < mData.mFlightTicketBeans.size()) {
                            data.add(mData.mFlightTicketBeans.get(idx));
                        }
                    }
                    mData.mFlightTicketBeans = data;
                    refreshData(mData);
                }
            });
            return true;
        }
        int page = indexs.get(0) / mCompentOption.getNumPageSize() + 1;
        selectPage(page, null);
        return false;
    }

    @Override
    protected ResourcePage<QiWuFlightTicketData, QiWuFlightTicketData.FlightTicketBean> createPage(QiWuFlightTicketData sources) {
        return  new ResFlightTicketPage(sources, sources.mFlightTicketBeans.size()) {
            @Override
            protected int numOfPageSize() {
                return getOption().getNumPageSize();
            }
        };
    }

    @Override
    public void showChoices(QiWuFlightTicketData data) {
        if(data.mFlightTicketBeans.size() == 1){
            getOption().setCanSure(true);
            getOption().setTtsText("找到以下航班信息");
        }
        else if(data.mFlightTicketBeans.size() > getOption().getNumPageSize()){
            if (getOption().getTtsText() == null) {
                getOption().setTtsText("找到多个航班结果，请选择查看第几页或者取消。");
            }
        }else {
            if (getOption().getTtsText() == null) {
                getOption().setTtsText("找到以下航班结果，并且展示相关的航班信息。");
            }
        }
        super.showChoices(data);
    }

    @Override
    public String getReportId() {
        return "Flight_ticket_Select";
    }

    private String getTips(){
        String tips = "";
        if (mPage != null) {
            if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
                if (mPage.getMaxPage() == 1) {
                    tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_ONE_PAGE");
                } else {
                    tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_LAST");
                }
            } else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
                tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_FIRST");
            } else { //其他中间页
                tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE");
            }
        }
        return tips;
    }

    @Override
    protected void onAddWakeupAsrCmd(AsrUtil.AsrComplexSelectCallback acsc, QiWuFlightTicketData data) {
        super.onAddWakeupAsrCmd(acsc, data);
        int earliestIndex = -1;
        Date earliestDate = null;
        Date lastDate = null;
        int lastIndex = -1;
        int cheapIndex = -1;
        double cheapPrice = Integer.MAX_VALUE;
        ArrayList<QiWuFlightTicketData.FlightTicketBean> mFlightTicketBeans = mData.mFlightTicketBeans;
        try {
            for(int i = 0; i <mFlightTicketBeans.size(); i++){
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                Date date = sdf.parse(mFlightTicketBeans.get(i).departureTime);
                if(earliestDate == null || date.before(earliestDate)){
                    earliestDate = date;
                    earliestIndex = i;
                }
                if(lastDate == null || date.after(lastDate)){
                    lastDate = date;
                    lastIndex = i;
                }
                if(Double.valueOf(mFlightTicketBeans.get(i).recommendPrice) < cheapPrice){
                    cheapIndex = i;
                    cheapPrice = Double.valueOf(mFlightTicketBeans.get(i).recommendPrice);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if(earliestIndex >= 0){
            addKeyWord(acsc, earliestIndex, "最早的");
        }
        if(lastIndex >= 0){
            addKeyWord(acsc, lastIndex, "最晚的");
        }

        if(cheapIndex >= 0){
            addKeyWord(acsc, cheapIndex, "最便宜的");
        }
        if(mFlightTicketBeans.size() > 1){
            acsc.addCommand("SORT_TIME", "时间排序");
            acsc.addCommand("SORT_PRICE", "价格排序");
        }
    }

    private void addKeyWord(AsrUtil.AsrComplexSelectCallback acsc, int index, String keywords) {
        if (!TextUtils.isEmpty(keywords) && keywords.length() > 1) {
            acsc.addIndex(index, keywords);
        }
    }

    @Override
    protected void commandSelect(String type, String command) {
        if(QiWuTicketManager.getInstance().isCancel()){
            return;
        }
        super.commandSelect(type, command);
    }

    @Override
    protected boolean onCommandSelect(String type, String speech) {
        final String command = speech;
        if ("SORT_TIME".equals(type)) {
            sortTrainTime(mData,command);
            return true;
        }
        if("SORT_PRICE".equals(type)){
            sortTrainPrice(mData, command);
            return true;
        }
        return super.onCommandSelect(type, command);
    }

    private void sortTrainPrice(QiWuFlightTicketData data, String speech){
        TicketPriceComparator trainPriceComparator = new TicketPriceComparator();
        Collections.sort(data.mFlightTicketBeans,trainPriceComparator);
        refreshData(data);
        String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK") + "请选择第几个或取消";
        if (!TextUtils.isEmpty(sortSpk)) {
            sortSpk = sortSpk.replace("%SORTSLOT%", speech);
        }
        speakWithTips(sortSpk);
    }

    private void sortTrainTime(QiWuFlightTicketData data, String speech){
        TicketTimeComparator trainTimeComparator = new TicketTimeComparator();
        Collections.sort(data.mFlightTicketBeans,trainTimeComparator);
        refreshData(data);
        String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK") + "请选择第几个或取消";
        if (!TextUtils.isEmpty(sortSpk)) {
            sortSpk = sortSpk.replace("%SORTSLOT%", speech);
        }
        speakWithTips(sortSpk);
    }

    @Override
    protected void onClearSelecting() {
        super.onClearSelecting();
    }

    public void speakWithTips(String spk) {
        TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
        mSpeechTaskId = TtsManager.getInstance().speakVoice(spk,
                InterruptTts.getInstance().isInterruptTTS() ? "" : TtsManager.BEEP_VOICE_URL,
                new TtsUtil.ITtsCallback() {
                    @Override
                    public void onSuccess() {
                        AsrManager.getInstance().mSenceRepeateCount = 0;

                        JNIHelper.logd("call select SenceRepeateCount: " + AsrManager.getInstance().mSenceRepeateCount);
                        if (isCoexistAsrAndWakeup() && !InterruptTts.getInstance().isInterruptTTS()) {
                            AsrManager.getInstance().mSenceRepeateCount++;
                            if (AsrManager.getInstance().mSenceRepeateCount < AsrManager.ASR_SENCE_REPEATE_COUNT) {
                                AsrManager.getInstance().start(createSelectAgainAsrOption());
                            }
                        }
                    }

                    @Override
                    public boolean isNeedStartAsr() {
                        return true;
                    }
                });
    }
}
