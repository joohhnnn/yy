package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.DateUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.TicketBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class QiWuFlightTicketData extends ListViewData {

    public QiWuFlightTicketData() {
        super(TYPE_FULL_LIST_FLIGHT_TICKET);
    }

    public ArrayList<FlightTicketBean> mFlightTicketBeans = new ArrayList<FlightTicketBean>();
    public String departureCity;
    public String arrivalCity;
    public String date;


    public static class FlightTicketBean extends TicketBean {
        public String airline;//航空公司
        public String flightNo;//航班编号
        public String departAirportName;//出发机场
        public String departAirportCode;
        public String departDate;
        public String arrivalAirportName;//到达机场
        public String arrivalAirportCode;
        public String arrivalTime;//
        public String addDate;//跨天数
    }

    @Override
    public void parseItemData(JSONBuilder data) {
        mFlightTicketBeans.clear();
        JSONArray obJsonArray = data.getVal("cines", JSONArray.class);
        departureCity = data.getVal("departureCity",String.class);
        arrivalCity = data.getVal("arrivalCity",String.class);
        date = data.getVal("date",String.class);
        for(int i = 0; i < count; i++){
            try {
                JSONBuilder cBuilder = new JSONBuilder(obJsonArray.getJSONObject(i));
                FlightTicketBean fb = new FlightTicketBean();
                fb.addDate = cBuilder.getVal("addDate", String.class);
                fb.airline = cBuilder.getVal("airline",String.class);
                fb.arrivalAirportName = cBuilder.getVal("arrivalAirportName", String.class);
                fb.arrivalTime = cBuilder.getVal("arrivalTime",String.class);
                fb.departureTime = cBuilder.getVal("departTime", String.class);
                fb.departDate = cBuilder.getVal("departDate", String.class);
                fb.departAirportName = cBuilder.getVal("departAirportName", String.class);
                fb.recommendPrice = cBuilder.getVal("cabinPrice", String.class);
                fb.recommendSeat = cBuilder.getVal("cabin", String.class);
                fb.seatCode = cBuilder.getVal("seatCode", String.class);
                fb.flightNo = cBuilder.getVal("number", String.class);
                mFlightTicketBeans.add(fb);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public String getShowDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String show = "";
        try {
            Date date = simpleDateFormat.parse(this.date);
            show += (date.getMonth() + 1) + "月";
            show += date.getDate()+"日";
        } catch (Exception e) {
            show = this.date;
        }
        return show;
    }

    public static QiWuFlightTicketData objectFromData(JSONObject jsonObject) {
        if(jsonObject == null){
            return null;
        }
        return parseItem(jsonObject);
    }

    private static QiWuFlightTicketData parseItem(JSONObject jsonObject) {
        QiWuFlightTicketData flightTicketData = new QiWuFlightTicketData();
        try {
            jsonObject = jsonObject.getJSONObject("data");
            flightTicketData.arrivalCity = jsonObject.getString("arrival_city");
            flightTicketData.departureCity = jsonObject.getString("departure_city");
            JSONArray jsonArray = jsonObject.getJSONArray("tickets");
            flightTicketData.date = jsonArray.getJSONObject(0).getJSONArray("departure_time").getString(0).split("T")[0];
            for(int i = 0; i < jsonArray.length(); i++){
                FlightTicketBean fb = new FlightTicketBean();
                JSONObject ticket = jsonArray.getJSONObject(i);
                fb.airline = ticket.getJSONArray("carrier").getString(0);
                fb.arrivalAirportName = ticket.getJSONArray("arrival_airport").getString(0);
                fb.arrivalAirportCode = ticket.getJSONArray("arrival_airport_code").getString(0);
                if(mMap.containsKey(fb.arrivalAirportCode)){
                    fb.arrivalAirportName = mMap.get( fb.arrivalAirportCode);
                }
                fb.arrivalTime = ticket.getJSONArray("arrival_time").getString(0).split("T")[1];
                String arrivalTime = ticket.getJSONArray("arrival_time").getString(0).split("T")[0];
                Date arrivalTimeeDate = new Date();
                arrivalTimeeDate.setYear(Integer.valueOf( arrivalTime.substring(0, 4)));
                arrivalTimeeDate.setMonth(Integer.valueOf( arrivalTime.substring(5, 7)) - 1);
                arrivalTimeeDate.setDate(Integer.valueOf( arrivalTime.substring(8, 10)));

                fb.departureTime = ticket.getJSONArray("departure_time").getString(0).split("T")[1];
                fb.departDate = ticket.getJSONArray("departure_time").getString(0).split("T")[0];
                Date departureDate = new Date();

                departureDate.setYear(Integer.valueOf( fb.departDate.substring(0, 4)));
                departureDate.setMonth(Integer.valueOf( fb.departDate.substring(5, 7)) - 1);
                departureDate.setDate(Integer.valueOf( fb.departDate.substring(8, 10)));
                fb.departAirportName = ticket.getJSONArray("departure_airport").getString(0);
                fb.departAirportCode = ticket.getJSONArray("departure_airport_code").getString(0);
                if(mMap.containsKey(fb.departAirportCode)){
                    fb.departAirportName = mMap.get(fb.departAirportCode);
                }
                fb.recommendPrice = ticket.getString("price");
                fb.flightNo = ticket.getJSONArray("number").getString(0);
                fb.addDate = String.valueOf(DateUtils.getGapCount(departureDate, arrivalTimeeDate));
                fb.seatCode = ticket.getJSONArray("cabin").getString(0);
                if(fb.seatCode.length() < 3){
                    fb.recommendSeat = "经济舱";
                }else{
                    fb.recommendSeat = fb.seatCode;
                }
                flightTicketData.mFlightTicketBeans.add(fb);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.logd("QiWuFlightTicketData is Exception");
            return null;
        }
        return flightTicketData;
    }

    private static HashMap<String, String> mMap = new HashMap<String, String>();

    static {
        mMap.put("HRB", "太平机场");
        mMap.put("NDG", "三家子机场");
        mMap.put("MDG", "海浪机场");
        mMap.put("JMU", "东郊机场");
        mMap.put("HEK", "黑河机场");
        mMap.put("YLN", "依兰机场");
        mMap.put("CGQ", "龙嘉机场");
        mMap.put("YNJ", "朝阳川机场");
        mMap.put("JIL", "二台子机场");
        mMap.put("DLC", "周水子机场");
        mMap.put("SHE", "桃仙机场");
        mMap.put("JNZ", "小岭子机场");
        mMap.put("DDG", "浪头机场");
        mMap.put("CHG", "朝阳机场");
        mMap.put("IOB", "鞍山机场");
        mMap.put("CNI", "大长山岛机场");
        mMap.put("HET", "白塔机场");
        mMap.put("BAV", "二里半机场");
        mMap.put("HLH", "乌兰浩特机场");
        mMap.put("HLD", "东山机场");
        mMap.put("XIL", "锡林浩特机场");
        mMap.put("CIF", "赤峰机场");
        mMap.put("TGO", "通辽机场");
        mMap.put("WUA", "乌海机场");
        mMap.put("DSN", "伊金霍洛机场");
        mMap.put("PEK", "首都机场");
        mMap.put("NAY", "南苑机场");
        mMap.put("TSN", "滨海机场");
        mMap.put("SJW", "正定机场");
        mMap.put("SHP", "山海关机场");
        mMap.put("TYN", "武宿机场");
        mMap.put("DAT", "怀仁机场");
        mMap.put("CIH", "王村机场");
        mMap.put("YCU", "关公机场");
        mMap.put("CGO", "新郑机场");
        mMap.put("LYA", "北郊机场");
        mMap.put("NNY", "姜营机场");
        mMap.put("AYN", "安阳机场");
        mMap.put("WUH", "天河机场");
        mMap.put("SHS", "沙市机场");
        mMap.put("XFN", "刘集机场");
        mMap.put("YIH", "三峡机场");
        mMap.put("ENH", "许家坪机场");
        mMap.put("DYG", "荷花大庸机场");
        mMap.put("CSX", "黄花机场");
        mMap.put("CGD", "桃花源机场");
        mMap.put("HNY", "衡阳机场");
        mMap.put("HJJ", "芷江机场");
        mMap.put("LLF", "零陵机场");
        mMap.put("CAN", "白云机场");
        mMap.put("MXZ", "梅县机场");
        mMap.put("ZUH", "三灶机场");
        mMap.put("SWA", "外砂机场");
        mMap.put("SZX", "宝安机场");
        mMap.put("ZHA", "湛江机场");
        mMap.put("HUZ", "平潭机场");
        mMap.put("ZCP", "佛山机场");
        mMap.put("XIN", "兴宁机场");
        mMap.put("HAK", "美兰机场");
        mMap.put("SYX", "凤凰机场");
        mMap.put("NNG", "吴圩机场");
        mMap.put("KWL", "两江机场");
        mMap.put("BHY", "福成机场");
        mMap.put("LZH", "白莲机场");
        mMap.put("WUZ", "长洲岛机场");
        mMap.put("AEB", "百色机场");
        mMap.put("TNA", "遥墙机场");
        mMap.put("WEH", "大水泊机场");
        mMap.put("TAO", "流亭机场");
        mMap.put("WEF", "文登机场");
        mMap.put("YNT", "莱山机场");
        mMap.put("LYI", "临沂机场");
        mMap.put("SUB", "朱安达机场");
        mMap.put("JNG", "济宁机场");
        mMap.put("DOY", "东营机场");
        mMap.put("KHN", "昌北机场");
        mMap.put("JDZ", "罗家机场");
        mMap.put("KOW", "黄金机场");
        mMap.put("JGS", "井冈山机场");
        mMap.put("TXN", "屯溪机场");
        mMap.put("HFE", "新桥机场");
        mMap.put("FUG", "西关机场");
        mMap.put("HGH", "萧山机场");
        mMap.put("WNZ", "永强机场");
        mMap.put("HSN", "普陀山机场");
        mMap.put("NGB", "栎社机场");
        mMap.put("YIW", "义乌机场");
        mMap.put("HYN", "黄岩路桥机场");
        mMap.put("JUZ", "衢州机场");
        mMap.put("NKG", "禄口机场");
        mMap.put("XUZ", "观音机场");
        mMap.put("LYG", "白塔埠机场");
        mMap.put("YNZ", "盐城机场");
        mMap.put("CZX", "奔牛机场");
        mMap.put("NTG", "兴东机场");
        mMap.put("WUX", "无锡机场");
        mMap.put("SHA", "虹桥机场");
        mMap.put("PVG", "浦东机场");
        mMap.put("XMN", "高崎机场");
        mMap.put("FOC", "长乐机场");
        mMap.put("JJN", "晋江机场");
        mMap.put("WUS", "武夷山机场");
        mMap.put("LCX", "连城机场");
        mMap.put("KMG", "巫家坝机场");
        mMap.put("LJG", "三义机场");
        mMap.put("JHG", "嘎洒机场");
        mMap.put("DLU", "大理机场");
        mMap.put("LUM", "芒市机场");
        mMap.put("DIG", "香格里拉机场");
        mMap.put("SYM", "思茅机场");
        mMap.put("BSD", "保山机场");
        mMap.put("ZAT", "昭通机场");
        mMap.put("LNJ", "临沧机场");
        mMap.put("YUA", "元谋机场");
        mMap.put("LXA", "贡嘎机场");
        mMap.put("BPX", "邦达机场");
        mMap.put("LZY", "林芝机场");
        mMap.put("CTU", "双流机场");
        mMap.put("MIG", "南郊机场");
        mMap.put("YBP", "菜坝机场");
        mMap.put("LZO", "蓝田机场");
        mMap.put("JZH", "黄龙机场");
        mMap.put("PZI", "保安营机场");
        mMap.put("XIC", "青山机场");
        mMap.put("DAX", "河市机场");
        mMap.put("NAO", "高坪机场");
        mMap.put("GHN", "广汉机场");
        mMap.put("GYS", "广元机场");
        mMap.put("KGT", "康定机场");
        mMap.put("CKG", "江北机场");
        mMap.put("WXN", "梁平机场");
        mMap.put("KWE", "龙洞堡机场");
        mMap.put("TEN", "大兴机场");
        mMap.put("ZYI", "遵义机场");
        mMap.put("AVA", "黄果树机场");
        mMap.put("ACX", "兴义机场");
        mMap.put("SIA", "咸阳机场");
        mMap.put("HZG", "城固机场");
        mMap.put("ENY", "南泥湾机场");
        mMap.put("AKA", "五里铺机场");
        mMap.put("UYN", "西沙机场");
        mMap.put("LHW", "中川机场");
        mMap.put("DNH", "敦煌机场");
        mMap.put("JGN", "嘉峪关机场");
        mMap.put("IQN", "西峰镇机场");
        mMap.put("CHW", "酒泉机场");
        mMap.put("XNN", "曹家堡机场");
        mMap.put("GOQ", "格尔木机场");
        mMap.put("INC", "河东机场");
        mMap.put("URC", "地窝堡机场");
        mMap.put("HTN", "和田机场");
        mMap.put("YIN", "伊宁机场");
        mMap.put("KRY", "克拉玛依机场");
        mMap.put("TCG", "塔城机场");
        mMap.put("AAT", "阿勒泰机场");
        mMap.put("AKU", "阿克苏机场");
        mMap.put("KRL", "库尔勒机场");
        mMap.put("KCA", "库车机场");
        mMap.put("KHG", "喀什机场");
        mMap.put("IQM", "且末机场");
        mMap.put("HMI", "哈密机场");
        mMap.put("FYN", "可可托托海机场");
        mMap.put("JIU", "庐山机场");
        mMap.put("AQG", "天柱山机场");
        mMap.put("TLQ", "交河机场");


    }

}
