package com.txznet.launcher.module.record;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.R;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.module.record.bean.ChatStockMsgData;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 展示股票的界面
 */
public class ChatStockModule extends BaseModule {

    @Bind(R.id.tv_stock_name)
    TextView tvStockName;
    @Bind(R.id.tv_stock_code)
    TextView tvStockCode;
    @Bind(R.id.tv_stock_price)
    TextView tvStockPrice;
    @Bind(R.id.iv_stock_state)
    ImageView ivStockState;
    @Bind(R.id.tv_stock_changeRate)
    TextView tvStockChangeRate;
    @Bind(R.id.tv_stock_highest_price)
    TextView tvStockHighestPrice;
    @Bind(R.id.tv_stock_lowest_price)
    TextView tvStockLowestPrice;
    @Bind(R.id.tv_stock_yestoday_close_price)
    TextView tvStockYestodayClosePrice;
    @Bind(R.id.tv_stock_today_open_price)
    TextView tvStockTodayOpenPrice;

    private ChatStockMsgData chatStockMsgData;

    @Override
    public void onCreate(String data) {
        super.onCreate(data);

        chatStockMsgData = parseData(data);
    }


    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_record_chat_stock, null);

        ButterKnife.bind(this, view);

        refreshStock();

        return view;
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        chatStockMsgData = parseData(data);

        refreshStock();
    }


    private void refreshStock() {
        tvStockName.setText(chatStockMsgData.mStockData.strName);
        tvStockCode.setText(chatStockMsgData.mStockData.strCode);
        tvStockPrice.setText(subZeroAndDot(format(chatStockMsgData.mStockData.strCurrentPrice)));
        tvStockChangeRate.setText(addMark(subZeroAndDot(format(chatStockMsgData.mStockData.strChangeAmount))) + "(" + addMark(subZeroAndDot(format(chatStockMsgData.mStockData.strChangeRate))) + "%)");
        float todayOpenPrice = Float.parseFloat(chatStockMsgData.mStockData.strYestodayClosePrice);
        float currentPrice = Float.parseFloat(chatStockMsgData.mStockData.strCurrentPrice);
        ivStockState.setVisibility(View.VISIBLE);
        if (currentPrice > todayOpenPrice) {
            ivStockState.setImageResource(R.drawable.ic_stock_up);
            tvStockPrice.setTextColor(tvStockPrice.getResources().getColor(R.color.color_stock_price_up));
            tvStockChangeRate.setTextColor(tvStockPrice.getResources().getColor(R.color.color_stock_price_up));
        } else if (currentPrice < todayOpenPrice) {
            ivStockState.setImageResource(R.drawable.ic_stock_down);
            tvStockPrice.setTextColor(tvStockPrice.getResources().getColor(R.color.color_stock_price_down));
            tvStockChangeRate.setTextColor(tvStockPrice.getResources().getColor(R.color.color_stock_price_down));
        } else {
            ivStockState.setVisibility(View.GONE);
            tvStockPrice.setTextColor(tvStockPrice.getResources().getColor(R.color.color_stock_price_normal));
            tvStockChangeRate.setTextColor(tvStockPrice.getResources().getColor(R.color.color_stock_price_normal));
        }

        tvStockHighestPrice.setText(format(chatStockMsgData.mStockData.strHighestPrice));
        tvStockLowestPrice.setText(format(chatStockMsgData.mStockData.strLowestPrice));
        tvStockYestodayClosePrice.setText(format(chatStockMsgData.mStockData.strYestodayClosePrice));
        tvStockTodayOpenPrice.setText(format(chatStockMsgData.mStockData.strTodayOpenPrice));
    }

    private ChatStockMsgData parseData(String data) {
        ChatStockMsgData chatStockMsgData = new ChatStockMsgData();
        chatStockMsgData.parseData(new JSONBuilder(data));
        return chatStockMsgData;
    }

    private String addMark(String value) {
        if (!value.startsWith("-")) {
            value = "+" + value;
        }
        return value;
    }

    private String format(String value) {
        if (TextUtils.isEmpty(value)) {
            return "";
        }
        return String.format("%.2f", Float.parseFloat(value));
    }

    public String subZeroAndDot(String s) {
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉
        }
        return s;
    }
}
