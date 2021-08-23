package com.txznet.txz.component.choice.list;

import android.text.TextUtils;

import com.txz.ui.data.UiData;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.ui.viewfactory.data.QiWuTrainTicketData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResTrainTicketPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.component.ticket.TicketPriceComparator;
import com.txznet.txz.component.ticket.TicketTimeComparator;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.text.TextResultHandle;
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

public class TrainTicketWorkChoice extends WorkChoice<QiWuTrainTicketData, QiWuTrainTicketData.TrainTicketBean> {

    public TrainTicketWorkChoice(CompentOption<QiWuTrainTicketData.TrainTicketBean> option) {
        super(option);
    }

    @Override
    protected void onConvToJson(QiWuTrainTicketData td, JSONBuilder jsonBuilder) {
        jsonBuilder.put("type", RecorderWin.TARIN_TICKET);
        String title = td.departureCity+"-"+td.arrivalCity+"  "+td.getShowDate();
        //jsonBuilder.put("titlefix", td.departureCity + "-" + td.arrivalCity + " " + td.getShowDate());
        jsonBuilder.put("prefix", title);
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < td.mTrainTicketBeans.size(); i++) {
            QiWuTrainTicketData.TrainTicketBean tb = td.mTrainTicketBeans.get(i);
            JSONObject obj = new JSONBuilder().put("arrivalTime", tb.arrivalTime).put("departureTime", tb.departureTime)
                    .put("costTime", tb.costTime).put("endStation",tb.endStation).put("ticketType", tb.recommendSeat)
                    .put("station", tb.station).put("ticketNo", tb.trainNo).put("allSeatJSONArray", tb.allSeatJSONArray)
                    .put("ticketPrice", tb.recommendPrice).put("departDate", tb.departDate).put("addDate", tb.addDate)
                    .getJSONObject();
            jsonArray.put(obj);
        }
        jsonBuilder.put("cines", jsonArray);
        jsonBuilder.put("count", jsonArray.length());
        jsonBuilder.put("vTips",getTips());
    }

    @Override
    protected String convItemToString(QiWuTrainTicketData.TrainTicketBean tb) {
        JSONBuilder obj = new JSONBuilder().put("arrivalTime", tb.arrivalTime).put("departureTime", tb.departureTime)
                .put("costTime", tb.costTime).put("endStation",tb.endStation).put("departDate", tb.departDate).put("addDate", tb.addDate)
                .put("station", tb.station).put("ticketNo", tb.trainNo).put("allSeatJSONArray", tb.allSeatJSONArray);
        return obj.toString();
    }

    @Override
    protected void onSelectIndex(final QiWuTrainTicketData.TrainTicketBean item, boolean isFromPage, int idx, String fromVoice) {
        AppLogicBase.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                TicketUseInfoDialog dialog = TicketUseInfoDialog.getInstance();
                dialog.setAfterDissmiss(new TicketUseInfoDialog.AfterDissmiss() {
                    @Override
                    public void afterDissmiss() {
                        ChoiceManager.getInstance().showTrainTicketList(mData, null);
                    }
                });
                JSONObject ticeketInfo = new JSONObject();
                try {
                    ticeketInfo.put("seatCode",item.seatCode);
                    ticeketInfo.put("ticketNum", item.trainNo);
                    ticeketInfo.put("departureCity",mData.departureCity);
                    ticeketInfo.put("arrivalCity", mData.arrivalCity);
                    ticeketInfo.put("endStation",item.endStation);
                    ticeketInfo.put("station",item.station);
                    ticeketInfo.put("departDate", item.departDate);
                    ticeketInfo.put("allSeatJSONArray",item.allSeatJSONArray);
                    ticeketInfo.put("departureTime",item.departureTime);
                    ticeketInfo.put("queryKey",item.queryKey);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                dialog.setTicketInfoJson(ticeketInfo.toString());
                dialog.setSeatName(item.recommendSeat);
                dialog.setTicketType(TicketUseInfoDialog.TicketType.Train);
                UiData.TTime currentTime = NativeData.getMilleServerTime();
                QiWuTicketManager.currentSelectIndexId = ""+currentTime.uint64Time;
                dialog.setCurrentSelectIndexId(QiWuTicketManager.currentSelectIndexId);
            }
        });

        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_QIWU_GET_USER_INFO);
        clearIsSelecting();
    }

    @Override
    protected boolean onIndexSelect(final List<Integer> indexs, final String command) {
        if (indexs.size() != 1) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    ArrayList<QiWuTrainTicketData.TrainTicketBean> data = new ArrayList<QiWuTrainTicketData.TrainTicketBean>();
                    for (Integer idx : indexs) {
                        if (idx < mData.mTrainTicketBeans.size()) {
                            data.add(mData.mTrainTicketBeans.get(idx));
                        }
                    }
                    mData.mTrainTicketBeans = data;
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
    public String getReportId() {
        return "Train_ticket_Select";
    }

    @Override
    public void showChoices(QiWuTrainTicketData data) {
        if(data.mTrainTicketBeans.size() == 1){
            getOption().setCanSure(true);
            getOption().setTtsText("找到以下车次信息");
        }else if(data.mTrainTicketBeans.size() > getOption().getNumPageSize()){
            if (getOption().getTtsText() == null) {
                getOption().setTtsText("找到多个列车结果，请选择查看第几页或者取消。");
            }
        }else {
            if (getOption().getTtsText() == null) {
                getOption().setTtsText("找到多个列车结果，并且展示相关的车次信息。");
            }
        }
        super.showChoices(data);
    }

    @Override
    public void stopTtsAndAsr() {
        JNIHelper.logd("stopTtsAndAsr");
        TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
        mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
        AsrManager.getInstance().cancel();
        if(isCoexistAsrAndWakeup()){
            TextResultHandle.getInstance().cancel();
        }
    }

    @Override
    protected ResourcePage<QiWuTrainTicketData, QiWuTrainTicketData.TrainTicketBean> createPage(QiWuTrainTicketData sources) {
        return  new ResTrainTicketPage(sources, sources.mTrainTicketBeans.size()) {

            @Override
            protected int numOfPageSize() {
                return getOption().getNumPageSize();
            }
        };
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
    protected void commandSelect(String type, String command) {
        if(QiWuTicketManager.getInstance().isCancel()){
            return;
        }
        super.commandSelect(type, command);
    }

    @Override
    protected boolean onCommandSelect(String type, String speech) {
        final String command = speech;
        if(mData.mTrainTicketBeans.size() > 1){
            if ("SORT_TIME".equals(type)) {
                sortTrainTime(mData,command);
                return true;
            }
            if("SORT_PRICE".equals(type)){
                sortTrainPrice(mData, command);
                return true;
            }
        }
        return super.onCommandSelect(type, command);
    }

    private void sortTrainPrice(QiWuTrainTicketData data, String speech){
        TicketPriceComparator trainPriceComparator = new TicketPriceComparator();
        Collections.sort(data.mTrainTicketBeans,trainPriceComparator);
        refreshData(data);
        String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK") + "请选择第几个或取消";
        if (!TextUtils.isEmpty(sortSpk)) {
            sortSpk = sortSpk.replace("%SORTSLOT%", speech);
        }
        //speakTtsInChoice(sortSpk + "，" + NativeData.getResString("RS_POI_SELECT_AGAIN_HINT"));
        speakWithTips(sortSpk);
    }

    private void sortTrainTime(QiWuTrainTicketData data, String speech){
        TicketTimeComparator trainTimeComparator = new TicketTimeComparator();
        Collections.sort(data.mTrainTicketBeans,trainTimeComparator);
        refreshData(data);
        String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK")  + "请选择第几个或取消";
        if (!TextUtils.isEmpty(sortSpk)) {
            sortSpk = sortSpk.replace("%SORTSLOT%", speech);
        }
        //speakTtsInChoice(sortSpk + "，" + NativeData.getResString("RS_POI_SELECT_AGAIN_HINT"));
        speakWithTips(sortSpk);
    }

    @Override
    protected void onAddWakeupAsrCmd(AsrUtil.AsrComplexSelectCallback acsc, QiWuTrainTicketData data) {
        super.onAddWakeupAsrCmd(acsc, data);
        int earliestIndex = -1;
        Date earliestDate = null;
        Date lastDate = null;
        int lastIndex = -1;
        int cheapIndex = -1;
        double cheapPrice = Integer.MAX_VALUE;
        ArrayList<QiWuTrainTicketData.TrainTicketBean> mTrainTicketBeans = mData.mTrainTicketBeans;
        try {
            for(int i = 0; i <mTrainTicketBeans.size(); i++){
                SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
                Date date = sdf.parse(mTrainTicketBeans.get(i).departureTime);
                if(earliestDate == null || date.before(earliestDate)){
                    earliestDate = date;
                    earliestIndex = i;
                }
                if(lastDate == null || date.after(lastDate)){
                    lastDate = date;
                    lastIndex = i;
                }
                if(Double.valueOf(mTrainTicketBeans.get(i).recommendPrice) < cheapPrice){
                    cheapIndex = i;
                    cheapPrice = Double.valueOf(mTrainTicketBeans.get(i).recommendPrice);
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
        if(mTrainTicketBeans.size() > 1){
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
    protected void onClearSelecting() {
        super.onClearSelecting();
    }

}
