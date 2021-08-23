package com.txznet.comm.ui.theme.test.smarthandyhome;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.DimenUtils;
import com.txznet.comm.ui.theme.test.widget.TrafficLightCrossView;
import com.txznet.comm.ui.viewfactory.data.SmartHandyHomeViewData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.MD5Util;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 说明：导航
 *
 * @author xiaolin
 * create at 2020-11-06 20:23
 */
public class HomeNavHolder {

    private static HomeNavHolder instance = new HomeNavHolder();

    public static HomeNavHolder getInstance() {
        return instance;
    }

    private View mRootView;

    private ViewGroup wrapTips;
    private TextView tvTips;

    private ViewGroup wrapRoadConditionsMsg;
    private TextView tvName;
    private TextView tvTime;
    private TextView tvDistance;

    private LinearLayout wrapBtn;
    private TrafficLightCrossView trafficLightCrossView;


    private String lastDataMd5 = "";

    public View getView() {
        if (mRootView == null) {
            Context context = UIResLoader.getInstance().getModifyContext();
            mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_home_item_nav, (ViewGroup) null);
            init();
        }
        return mRootView;
    }

    private void init() {
        mRootView.findViewById(R.id.imgBtnMore).setOnClickListener(clickMoreListener);
        mRootView.findViewById(R.id.wrapTips).setOnClickListener(clickMoreListener);

        wrapTips = mRootView.findViewById(R.id.wrapTips);
        wrapBtn = mRootView.findViewById(R.id.warpBtn);
        tvTips = mRootView.findViewById(R.id.tvTips);

        wrapRoadConditionsMsg = mRootView.findViewById(R.id.wrapRoadConditionsMsg);
        tvName = wrapRoadConditionsMsg.findViewById(R.id.tvName);
        tvTime = wrapRoadConditionsMsg.findViewById(R.id.tvTime);
        tvDistance = wrapRoadConditionsMsg.findViewById(R.id.tvDistance);

        trafficLightCrossView = mRootView.findViewById(R.id.trafficLightCross);
    }

    public void update(SmartHandyHomeViewData.NavData data) {
        LogUtil.d("xxxx", JSONObject.toJSONString(data));
        try {
            String md5 = MD5Util.generateMD5(JSONObject.toJSONString(data));
            if (md5.equals(lastDataMd5)) {
                return;
            }
            lastDataMd5 = md5;
        } catch (Exception e) {
            e.printStackTrace();
            lastDataMd5 = null;
        }
        Context context = UIResLoader.getInstance().getModifyContext();

        if (TextUtils.isEmpty(data.tip)) {
            wrapTips.setVisibility(View.GONE);
        } else {
            wrapTips.setVisibility(View.VISIBLE);
            tvTips.setText(data.tip);
        }

        if (TextUtils.isEmpty(data.roadConditionsJson)) {
            wrapRoadConditionsMsg.setVisibility(View.GONE);
            trafficLightCrossView.setVisibility(View.GONE);
        } else {
            wrapRoadConditionsMsg.setVisibility(View.VISIBLE);
            trafficLightCrossView.setVisibility(View.VISIBLE);

            JSONBuilder jsonBuilder = new JSONBuilder(data.roadConditionsJson);
            tvName.setText(jsonBuilder.getVal("name", String.class, ""));
            tvTime.setText(timeToStr(jsonBuilder.getVal("time", Integer.class, -1)));
            tvDistance.setText(distanceToStr(jsonBuilder.getVal("distance", Integer.class, -1)));

            List<TrafficLightCrossView.Step> stepArrayList = new ArrayList<>();

            JSONArray ary = jsonBuilder.getVal("trafficStatus", JSONArray.class);
            if (ary != null && ary.length() > 0) {
                for (int i = 0; i < ary.length(); i++) {
                    try {
                        org.json.JSONObject obj = ary.getJSONObject(i);
                        String status = obj.getString("status");
                        int distance = obj.getInt("distance");
                        TrafficLightCrossView.Step step = new TrafficLightCrossView.Step();
                        step.color = getColorByTrafficStatus(status);
                        step.distance = distance;
                        stepArrayList.add(step);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            trafficLightCrossView.setTrafficSteps(stepArrayList);
        }

        wrapBtn.removeAllViews();
        int size = data.addressArrays.size();
        for (int i = 0; i < size; i++) {
            final SmartHandyHomeViewData.NavData.Address item = data.addressArrays.get(i);
            View itemView = LayoutInflater.from(context).inflate(R.layout.smart_handy_home_item_nav_item, (ViewGroup) null);
            ImageView imgIcon = itemView.findViewById(R.id.imgIcon);
            TextView tvName = itemView.findViewById(R.id.tvName);
            itemView.findViewById(R.id.wrap).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 开始导航
                    RecordWin2Manager.getInstance().operateView(
                            TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                            TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_NAV_BEGIN,
                            0, 0, 0,
                            JSONObject.toJSONString(item));
                }
            });

            imgIcon.setImageResource(getNavIcon(item.type));
            tvName.setText(item.name);

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.weight = 1;
            wrapBtn.addView(itemView, lp);

            if (i + 1 != size) {
                View view = new View(context);
                ViewGroup.LayoutParams lpPadding = new ViewGroup.LayoutParams(
                        (int) DimenUtils.dp2px(context, 10F),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                wrapBtn.addView(view, lpPadding);
            }
        }
    }

    /**
     * 点击更多
     */
    private View.OnClickListener clickMoreListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RecordWin2Manager.getInstance().operateView(
                    TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                    TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SMART_HANDY_NAV_SET_TRAVEL_MODE,
                    0, 0, 0);
        }
    };

    /**
     * 导航图标
     *
     * @param type {@link SmartHandyHomeViewData.NavData}
     * @return
     */
    private int getNavIcon(int type) {
        switch (type) {
            case SmartHandyHomeViewData.NavData.ADDRESS_TYPE_HOME:
                return R.drawable.smart_handy_icon_home;
            case SmartHandyHomeViewData.NavData.ADDRESS_TYPE_COMPANY:
                return R.drawable.smart_handy_icon_company;
        }
        return R.drawable.smart_handy_icon_home;
    }

    /**
     * 路况距离转常用单位
     *
     * @param distance 单位：米
     * @return
     */
    private String distanceToStr(int distance) {
        if (distance <= 0) {
            return "";
        }
        if (distance < 100) {
            return distance + "米";
        }
        return String.format(Locale.getDefault(),
                "%.1f公里", distance / 1000F);
    }

    /**
     * 路况时长转常用单位
     *
     * @param time 单位：秒
     * @return
     */
    private String timeToStr(int time) {
        if (time == -1) {
            return "";
        }
        if (time < 3600) {
            return (int) Math.ceil(time / 60F) + "分钟";
        }
        int h = time / 3600;// 小时
        int m = (int) Math.ceil((time % 3600) / 60F);// 分钟
        return h + "小时" + m + "分钟";
    }

    public static int getColorByTrafficStatus(String status) {
        // 未知、畅通、缓行、拥堵
        int color = 0xFF0F89F5;
        if ("畅通".equals(status)) {
            color = 0xFF02B81D;
        } else if ("缓行".equals(status)) {
            color = 0xFFFEBA01;
        } else if ("拥堵".equals(status) || "严重拥堵".equals(status)) {
            color = 0xFFEE1F20;
        }
        return color;
    }

}
