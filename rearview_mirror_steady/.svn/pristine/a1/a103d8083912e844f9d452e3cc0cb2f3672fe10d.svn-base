package com.txznet.record.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.record.bean.BaseDisplayMsg;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.MiHomeInfo;
import com.txznet.record.bean.MiHomeMsg;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;
import com.txznet.record.view.TitleView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatCarControlHomeAdapter extends ChatDisplayAdapter {
    public ChatCarControlHomeAdapter(Context context, List displayList) {
        super(context, displayList);
    }

    public static BaseDisplayMsg generateMiHomeItem(JSONBuilder jsonBuilder, TitleView.Info info) {
        JSONObject[] jsonObjects = jsonBuilder.getVal("devices", JSONObject[].class);
        MiHomeMsg miHomeMsg = new MiHomeMsg();
        try {
            List<ChatCarControlHomeAdapter.MiHomeItem> items = new ArrayList<MiHomeItem>();
            for (int i = 0; i < jsonObjects.length; i++) {
                JSONObject jsonObject = jsonObjects[i];
                if (jsonObject == null) {
                    continue;
                }
                String action = jsonBuilder.getVal("action", String.class);
                MiHomeInfo miHomeInfo = null;
                if ("query".equals(action)) {
                    if (jsonObject.has("temperature")) {
                        miHomeInfo = new MiHomeInfo.TemperatureHumiditySensorDevice();
                        ((MiHomeInfo.TemperatureHumiditySensorDevice) miHomeInfo).temperature = jsonObject.optInt("temperature");
                        ((MiHomeInfo.TemperatureHumiditySensorDevice) miHomeInfo).humidity = jsonObject.optInt("humidity");
                    } else {
                        miHomeInfo = new MiHomeInfo.Device();
                    }
                    ((MiHomeInfo.Device) miHomeInfo).deviceName = jsonObject.optString("deviceName");
                    ((MiHomeInfo.Device) miHomeInfo).deviceId = jsonObject.optString("deviceId");
                    ((MiHomeInfo.Device) miHomeInfo).isOnline = jsonObject.optInt("isOnline");
                    ((MiHomeInfo.Device) miHomeInfo).deviceState = jsonObject.optInt("deviceState");
                } else if ("control".equals(action)) {
                    miHomeInfo = new MiHomeInfo.AreaDevice();
                    ((MiHomeInfo.AreaDevice) miHomeInfo).deviceTypeName = jsonObject.optString("deviceTypeName");
                    ((MiHomeInfo.AreaDevice) miHomeInfo).closeNum = jsonObject.optString("closeNum");
                    ((MiHomeInfo.AreaDevice) miHomeInfo).openNum = jsonObject.optString("openNum");
                } else {
                    miHomeInfo = new MiHomeInfo();
                }
                miHomeInfo.deviceArea = jsonObject.optString("deviceArea");
                miHomeInfo.state = jsonObject.optInt("state");
                miHomeInfo.deviceLocation = jsonObject.optString("deviceLocation");

                MiHomeItem miHomeItem = new MiHomeItem();
                miHomeItem.curPrg = 0;
                miHomeItem.mItem = miHomeInfo;
                items.add(miHomeItem);
            }
            info.hideDrawable = true;
            miHomeMsg.mTitleInfo = info;
            miHomeMsg.mItemList = items;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return miHomeMsg;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_car_control_home, parent, false);
            prepareSetLayoutParams(convertView);
        }
        MiHomeItem item = (MiHomeItem) getItem(position);
        MiHomeInfo info = item.mItem;
        TextView tvMainTitle = ViewHolder.get(convertView, R.id.tv_main_title);
        TextView tvSubTitle = ViewHolder.get(convertView, R.id.tv_sub_title);
        TextView tvNum = ViewHolder.get(convertView, R.id.tv_num);
        TextView tvState = ViewHolder.get(convertView, R.id.tv_state);
        TextView tvDeviceLocation = ViewHolder.get(convertView, R.id.tv_device_location);
        TextView tvOpenNum = ViewHolder.get(convertView, R.id.tv_open_num);
        TextView tvCloseNum = ViewHolder.get(convertView, R.id.tv_close_num);
        ImageView ivProgress = ViewHolder.get(convertView, R.id.iv_progress);
        TextView tvTemperature = ViewHolder.get(convertView, R.id.tv_temperature);
        TextView tvTemperatureTitle = ViewHolder.get(convertView, R.id.tv_title_temperature);
        TextView tvHumidityTitle = ViewHolder.get(convertView, R.id.tv_title_humidity);
        TextView tvHumidity = ViewHolder.get(convertView, R.id.tv_humidity);
        int state = info.state;
        tvDeviceLocation.setText(info.deviceLocation);
        tvSubTitle.setText(info.deviceArea);
        if (info instanceof MiHomeInfo.TemperatureHumiditySensorDevice) {
            tvMainTitle.setText(((MiHomeInfo.Device) info).deviceName);
            if (((MiHomeInfo.Device) info).isOnline == 1) {
                tvState.setVisibility(View.VISIBLE);
                ivProgress.setVisibility(View.GONE);
                tvCloseNum.setVisibility(View.GONE);
                tvOpenNum.setVisibility(View.GONE);
                tvTemperature.setVisibility(View.GONE);
                tvTemperatureTitle.setVisibility(View.GONE);
                tvHumidity.setVisibility(View.GONE);
                tvHumidityTitle.setVisibility(View.GONE);
                tvState.setTextColor(Color.parseColor("#AAAAAA"));
                tvState.setText("离线");
            } else {
                switch (state) {
                    case MiHomeInfo.STATE_QUERYING:
                        tvTemperature.setVisibility(View.GONE);
                        tvTemperatureTitle.setVisibility(View.GONE);
                        tvHumidity.setVisibility(View.GONE);
                        tvHumidityTitle.setVisibility(View.GONE);
                        tvCloseNum.setVisibility(View.GONE);
                        tvOpenNum.setVisibility(View.GONE);
                        tvState.setVisibility(View.VISIBLE);
                        ivProgress.setVisibility(View.VISIBLE);
                        tvState.setTextColor(Color.parseColor("#2790FF"));
                        tvState.setText("查询中");
                        break;
                    default:
                        tvTemperatureTitle.setVisibility(View.VISIBLE);
                        tvTemperature.setVisibility(View.VISIBLE);
                        tvHumidityTitle.setVisibility(View.VISIBLE);
                        tvHumidity.setVisibility(View.VISIBLE);
                        tvCloseNum.setVisibility(View.GONE);
                        tvOpenNum.setVisibility(View.GONE);
                        tvState.setVisibility(View.GONE);
                        ivProgress.setVisibility(View.GONE);
                        tvHumidity.setText(((MiHomeInfo.TemperatureHumiditySensorDevice) info).humidity + "%");
                        tvTemperature.setText(((MiHomeInfo.TemperatureHumiditySensorDevice) info).temperature + "℃");
                        break;
                }
            }
        } else if (info instanceof MiHomeInfo.Device) {
            tvTemperature.setVisibility(View.GONE);
            tvTemperatureTitle.setVisibility(View.GONE);
            tvHumidity.setVisibility(View.GONE);
            tvHumidityTitle.setVisibility(View.GONE);
            tvMainTitle.setText(((MiHomeInfo.Device) info).deviceName);
            if (((MiHomeInfo.Device) info).isOnline == 1) {
                tvState.setVisibility(View.VISIBLE);
                ivProgress.setVisibility(View.GONE);
                tvCloseNum.setVisibility(View.GONE);
                tvOpenNum.setVisibility(View.GONE);
                tvState.setTextColor(Color.parseColor("#AAAAAA"));
                tvState.setText("离线");
            } else {
                switch (state) {
                    case MiHomeInfo.STATE_EXECUTING:
                        ivProgress.setVisibility(View.VISIBLE);
                        tvCloseNum.setVisibility(View.GONE);
                        tvOpenNum.setVisibility(View.GONE);
                        tvState.setVisibility(View.VISIBLE);
                        tvState.setTextColor(Color.parseColor("#2790FF"));
                        tvState.setText("执行中");
                        break;
                    case MiHomeInfo.STATE_QUERYED:
                    case MiHomeInfo.STATE_EXECUTED:
                        tvState.setVisibility(View.VISIBLE);
                        tvCloseNum.setVisibility(View.GONE);
                        tvOpenNum.setVisibility(View.GONE);
                        ivProgress.setVisibility(View.GONE);
                        if (((MiHomeInfo.Device) info).deviceState == MiHomeInfo.DEVICE_STATE_OPEN) {
                            tvState.setTextColor(Color.parseColor("#00CF7F"));
                            tvState.setText("已开启");
                        } else if (((MiHomeInfo.Device) info).deviceState == MiHomeInfo.DEVICE_STATE_CLOSE){
                            tvState.setTextColor(Color.parseColor("#AAAAAA"));
                            tvState.setText("已关闭");
                        }
                        break;
                    case MiHomeInfo.STATE_QUERYING:
                        tvCloseNum.setVisibility(View.GONE);
                        tvOpenNum.setVisibility(View.GONE);
                        tvState.setVisibility(View.VISIBLE);
                        ivProgress.setVisibility(View.VISIBLE);
                        tvState.setTextColor(Color.parseColor("#2790FF"));
                        tvState.setText("查询中");
                        break;
                    case MiHomeInfo.STATE_QUERY_FAIL:
                        tvCloseNum.setVisibility(View.GONE);
                        tvOpenNum.setVisibility(View.GONE);
                        tvState.setVisibility(View.VISIBLE);
                        ivProgress.setVisibility(View.GONE);
                        tvState.setTextColor(Color.parseColor("#2790FF"));
                        tvState.setText("查询失败");
                        break;
                    default:
                        tvState.setVisibility(View.GONE);
                        ivProgress.setVisibility(View.GONE);
                        tvCloseNum.setVisibility(View.GONE);
                        tvOpenNum.setVisibility(View.GONE);
                        break;
                }
            }
        } else if (info instanceof MiHomeInfo.AreaDevice) {
            tvMainTitle.setText(((MiHomeInfo.AreaDevice) info).deviceTypeName);
            tvTemperature.setVisibility(View.GONE);
            tvTemperatureTitle.setVisibility(View.GONE);
            tvHumidity.setVisibility(View.GONE);
            tvHumidityTitle.setVisibility(View.GONE);
            switch (state) {
                case MiHomeInfo.STATE_EXECUTING:
                    ivProgress.setVisibility(View.VISIBLE);
                    tvCloseNum.setVisibility(View.GONE);
                    tvOpenNum.setVisibility(View.GONE);
                    tvState.setVisibility(View.VISIBLE);
                    tvState.setTextColor(Color.parseColor("#2790FF"));
                    tvState.setText("执行中");
                    break;
                case MiHomeInfo.STATE_EXECUTED:
                    ivProgress.setVisibility(View.GONE);
                    if ("1".equals(((MiHomeInfo.AreaDevice) info).closeNum)) {
                        tvState.setVisibility(View.VISIBLE);
                        tvCloseNum.setVisibility(View.GONE);
                        tvOpenNum.setVisibility(View.GONE);
                        tvState.setTextColor(Color.parseColor("#AAAAAA"));
                        tvState.setText("已关闭");
                    } else if ("1".equals(((MiHomeInfo.AreaDevice) info).openNum)) {
                        tvState.setVisibility(View.VISIBLE);
                        tvCloseNum.setVisibility(View.GONE);
                        tvOpenNum.setVisibility(View.GONE);
                        tvState.setTextColor(Color.parseColor("#00CF7F"));
                        tvState.setText("已开启");
                    } else {
                        tvState.setVisibility(View.GONE);
                        tvCloseNum.setVisibility(View.VISIBLE);
                        tvOpenNum.setVisibility(View.VISIBLE);
                        tvCloseNum.setText("已关闭" + ((MiHomeInfo.AreaDevice) info).closeNum + "个");
                        tvOpenNum.setText("已开启" + ((MiHomeInfo.AreaDevice) info).openNum + "个");
                    }
                    break;
                default:
                    tvState.setVisibility(View.GONE);
                    ivProgress.setVisibility(View.GONE);
                    tvCloseNum.setVisibility(View.GONE);
                    tvOpenNum.setVisibility(View.GONE);
                    break;
            }
        }


        tvNum.setText(String.valueOf(position + 1));
        View mDivider = ViewHolder.get(convertView, R.id.divider);
        mDivider.setVisibility(position == (getCount() - 1) ? View.INVISIBLE : View.VISIBLE);
        View layoutItem = ViewHolder.get(convertView, R.id.layout_item);
        if (position == mFocusIndex) {
            layoutItem.setBackgroundColor(GlobalContext.get().getResources()
                    .getColor(R.color.bg_ripple_focused));
        } else {
            layoutItem.setBackgroundColor(GlobalContext.get().getResources()
                    .getColor(R.color.bg_ripple_nor));
        }
        convertView.setEnabled(false);
        convertView.setClickable(false);

        return convertView;
    }

    public static class MiHomeItem extends DisplayItem<MiHomeInfo> {

    }

}
