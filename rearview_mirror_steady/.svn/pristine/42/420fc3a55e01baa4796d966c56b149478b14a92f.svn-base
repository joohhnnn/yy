package com.txznet.launcher.module.nav;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.nav.NavManager;
import com.txznet.launcher.module.BaseModule;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by TXZ-METEORLUO on 2018/5/22.
 * 路况早晚报界面
 * tmc 应该是交通管制（Traffic Movement Control）
 */

public class TmcModule extends BaseModule {
    public static class TrafficData {
        public static final int ICON_COMPANY = 0;
        public static final int ICON_HOME = 1;

        public static class TrafficStep {
            public String status;
            public int distance;
            public String road;
        }

        private int resIconId;
        private String title;
        private String statusDesc;
        private String totalDistanceDesc;
        private List<TrafficStep> steps;

        public TrafficData(String distance, String status) {
            totalDistanceDesc = distance;
            statusDesc = status;
        }

        public void addStep(TrafficStep step) {
            if (steps == null) {
                steps = new ArrayList<TrafficStep>();
            }
            steps.add(step);
        }

        public void setIconType(int id) {
            resIconId = id;
        }

        public int getIconType() {
            return resIconId;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitle() {
            return this.title;
        }

        public String getTotalDistance() {
            return totalDistanceDesc;
        }

        public String getStatusDesc() {
            return statusDesc;
        }

        public List<TrafficStep> getTrafficSteps() {
            return steps;
        }
    }

    @Bind(R.id.tmc_bg_ly)
    LinearLayout tmcBgLy;
    @Bind(R.id.icon_iv)
    ImageView iconIv;
    @Bind(R.id.rem_min_tv)
    TextView rMinTv;
    @Bind(R.id.rem_tv)
    TextView tRemTv;
    @Bind(R.id.tmc_ly)
    LinearLayout tmcLy;

    private String mTmpData;

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
//        mTmpData = genTestData();
        mTmpData = data;
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_tmc, null);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        mTmpData = data;
        if (!TextUtils.isEmpty(mTmpData)) {
            parseViewData(mTmpData);
            mTmpData = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mTmpData)) {
            parseViewData(mTmpData);
            mTmpData = null;
        }
    }

    private String genTestData() {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("title", "63分钟到家");

        JSONObject object = new JSONObject();


        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("status", "畅通");
            jsonObject.put("distance", 200);
            jsonObject.put("road", "深南大道");
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("status", "拥堵");
            jsonObject.put("distance", 500);
            jsonObject.put("road", "深南大道");
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("status", "畅通");
            jsonObject.put("distance", 200);
            jsonObject.put("road", "深南大道");
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("status", "拥堵");
            jsonObject.put("distance", 100);
            jsonObject.put("road", "宝安大道");
            jsonArray.put(jsonObject);

            jsonObject = new JSONObject();
            jsonObject.put("status", "畅通");
            jsonObject.put("distance", 200);
            jsonObject.put("road", "宝龙大道");
            jsonArray.put(jsonObject);

            object.put("data", jsonArray);
            object.put("total", "1.2公里");
            object.put("traffic", "深南大道有拥堵");
            object.put("target", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        jsonBuilder.put("data", object.toString());
        return jsonBuilder.toString();
    }

    private void parseViewData(String tmcStr) {
        JSONObject jsonObject = null;
        JSONBuilder jsonBuilder = new JSONBuilder(tmcStr);
        String title = jsonBuilder.getVal("title", String.class);
        String data = jsonBuilder.getVal("data", String.class);
        if (!TextUtils.isEmpty(data)) {
            try {
                jsonObject = new JSONObject(data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (jsonObject != null) {
            String total = null;
            String status = null;
            TrafficData td = null;
            int target = 0;
            if (jsonObject.has("total")) {
                total = jsonObject.optString("total");
            }
            if (jsonObject.has("traffic")) {
                status = jsonObject.optString("traffic");
            }
            if (jsonObject.has("target")) {
                target = jsonObject.optInt("target");
            }
            td = new TrafficData(total, status);
            td.setTitle(title);
            td.setIconType(target);
            if (jsonObject.has("data")) {
                JSONArray jsonArray = jsonObject.optJSONArray("data");
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = (JSONObject) jsonArray.opt(i);
                        if (object != null) {
                            TrafficData.TrafficStep step = new TrafficData.TrafficStep();
                            if (object.has("status")) {
                                step.status = object.optString("status");
                            }
                            if (object.has("distance")) {
                                step.distance = object.optInt("distance");
                            }
                            if (object.has("road")) {
                                step.road = object.optString("road");
                            }
                            td.addStep(step);
                        }
                    }
                }
            }
            refreshView(td);
        }
    }

    private void refreshView(TrafficData td) {
        int type = td.getIconType();
        if (type == TrafficData.ICON_HOME) {
            iconIv.setImageResource(R.drawable.icon_tmc_home);
            tmcBgLy.setBackgroundResource(R.drawable.icon_tmc_home_bg);
        } else if (type == TrafficData.ICON_COMPANY) {
            iconIv.setImageResource(R.drawable.icon_tmc_company);
            tmcBgLy.setBackgroundResource(R.drawable.icon_tmc_com_bg);
        }

        String title = td.getTitle();
        rMinTv.setText(title);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(td.getTotalDistance());
        stringBuilder.append("   ");
        stringBuilder.append(td.getStatusDesc());
        tRemTv.setText(stringBuilder.toString());

        if (tmcLy != null) {
            tmcLy.removeAllViews();
        }
        if (td.getTrafficSteps() == null || td.getTrafficSteps().isEmpty()) {
            insertColorStatus("未知", 1);
            return;
        }

        for (TrafficData.TrafficStep step : td.getTrafficSteps()) {
            insertColorStatus(step.status, step.distance);
        }
    }

    private void insertColorStatus(String status, int weight) {
        int color = getColorByTrafficStatus(status);
        View v = new View(GlobalContext.get());
        v.setBackgroundColor(color);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
        params.weight = weight;
        if (tmcLy != null) {
            tmcLy.addView(v, params);
        }
    }

    public static int getColorByTrafficStatus(String status) {
        // 未知、畅通、缓行、拥堵
        int color = Color.parseColor("#0F89F5");
        if ("畅通".equals(status)) {
            color = Color.parseColor("#33B100");
        } else if ("缓行".equals(status)) {
            color = Color.parseColor("#FFCC00");
        } else if ("拥堵".equals(status) || "严重拥堵".equals(status)) {
            color = Color.parseColor("#DE0000");
        }
        return color;
    }

    @Override
    public void onPreRemove() {
        super.onPreRemove();
        NavManager.getInstance().dismissTmc();
    }
}