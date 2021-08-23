package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.txz.ui.voice.VoiceData.StockInfo;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.LouHolder;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatShockViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatShockView;
import com.txznet.resholder.R;

import java.util.Locale;

/**
 * 股票的界面
 * <p>
 * 2020-08-10 14:15
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class ChatShockView extends IChatShockView {

    private static ChatShockView sInstance = new ChatShockView();

    private StockInfo mStockInfo;
    private int shareColorUp;
    private int shareColorDown;
    private int shareColorNormal;


    private ChatShockView() { }
    public static ChatShockView getInstance() {
        return sInstance;
    }

    @Override
    public void init() {
        super.init();
        shareColorUp = 0xFFF54545;
        shareColorDown = 0xFF00C873;
        shareColorNormal = 0xFFFFFFFF;
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        ChatShockViewData chatShockViewData = (ChatShockViewData) data;
        WinLayout.getInstance().vTips = chatShockViewData.vTips;

        View view = createViewNone(chatShockViewData);

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = ChatShockView.getInstance();
        return adapter;
    }


    private View createViewNone(ChatShockViewData viewData) {
        StockInfo stockInfo = viewData.mStockInfo;
        this.mStockInfo = stockInfo;
        Context context = UIResLoader.getInstance().getModifyContext();

        View view = LayoutInflater.from(context).inflate(R.layout.chat_shock_view, (ViewGroup) null, false);
        LouHolder holder = LouHolder.createInstance(view);


        // 股票名字
        holder.putText(R.id.tvName, stockInfo.strName);

        // 股票代码
        holder.putText(R.id.tvCode, stockInfo.strCode);

        // 价格
        holder.putText(R.id.tvPrice, subZeroAndDot(format(stockInfo.strCurrentPrice)));
        holder.putTextColor(R.id.tvPrice, getColorByPrice(
                Float.parseFloat(mStockInfo.strCurrentPrice),
                Float.parseFloat(mStockInfo.strYestodayClosePrice)));

        // 升降图标
        refreshUpAndDown((ImageView) holder.getView(R.id.ivUpAndDown));

        if (format(mStockInfo.strChangeRate).startsWith("-")) {
            holder.putText(R.id.tvChangePriceLabel, "跌值");
            holder.putText(R.id.tvChangePercentLabel, "跌幅");
        } else {
            holder.putText(R.id.tvChangePriceLabel, "涨值");
            holder.putText(R.id.tvChangePercentLabel, "涨幅");
        }


        int changeColor = getColorByPrice(
                Float.parseFloat(mStockInfo.strCurrentPrice),
                Float.parseFloat(mStockInfo.strYestodayClosePrice));

        // 跌/涨值
        holder.putText(R.id.tvChangeAmount, addMark(subZeroAndDot(format(mStockInfo.strChangeAmount))));
        holder.putTextColor(R.id.tvChangeAmount, changeColor);

        // 跌/涨幅
        holder.putTextColor(R.id.tvChangeRate, changeColor);
        if (!TextUtils.isEmpty(mStockInfo.strChangeRate)) {
            holder.putText(R.id.tvChangeRate, addMark(subZeroAndDot(format(mStockInfo.strChangeRate))) + "%");
        } else {
            holder.putText(R.id.tvChangeRate, "");
        }

        // 昨收
        holder.putText(R.id.tvYestodayClosePrice, format(mStockInfo.strYestodayClosePrice));

        // 最高
        holder.putText(R.id.tvHighestPrice, format(mStockInfo.strHighestPrice));
        holder.putTextColor(R.id.tvHighestPrice, changeColor);

        // 今开
        holder.putText(R.id.tvTodayOpenPrice, format(mStockInfo.strTodayOpenPrice));
        holder.putTextColor(R.id.tvTodayOpenPrice, changeColor);

        // 最低
        holder.putText(R.id.tvLowestPrice, format(mStockInfo.strLowestPrice));

        return view;
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
        return String.format(Locale.getDefault(), "%.2f", Float.parseFloat(value));
    }

    public static String subZeroAndDot(String s) {
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");//去掉多余的0    
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉    
        }
        return s;
    }

    private int getColorByPrice(float curprice, float compareprice) {
        if (curprice == 0) {  //停牌股票的昨收不为0，其他为0
            return shareColorNormal;
        }
        if (curprice > compareprice) {
            return shareColorUp;
        } else if (curprice < compareprice) {
            return shareColorDown;
        } else {
            return shareColorNormal;
        }
    }

    private void refreshUpAndDown(ImageView ivUpAndDown) {
        float todayOpenPrice = Float
                .parseFloat(mStockInfo.strYestodayClosePrice);
        float currentPrice = Float.parseFloat(mStockInfo.strCurrentPrice);
        ivUpAndDown.setVisibility(View.VISIBLE);
        if (currentPrice == 0) {  //停牌股票的昨收不为0，其他为0
            ivUpAndDown.setVisibility(View.INVISIBLE);
            return;
        }
        if (currentPrice > todayOpenPrice) {
            ivUpAndDown.setImageResource(R.drawable.stock_up_icon);
        } else if (currentPrice < todayOpenPrice) {
            ivUpAndDown.setImageResource(R.drawable.stock_down_icon);
        } else {
            ivUpAndDown.setVisibility(View.INVISIBLE);
        }
    }

}
