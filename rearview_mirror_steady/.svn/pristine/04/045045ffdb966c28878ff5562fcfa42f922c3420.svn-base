package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.TicketUserInfoListViewData;
import com.txznet.comm.ui.viewfactory.data.TicketWaitingListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITicketUserInfoListView;
import com.txznet.comm.ui.viewfactory.view.ITicketWaitingListView;
import com.txznet.loader.AppLogicBase;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.sdk.TXZTtsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 说明：
 *
 * @author xiaolin
 * create at 2020-08-26 14:51
 */
public class TicketWaitingListView extends ITicketWaitingListView {

    private static TicketWaitingListView sInstance = new TicketWaitingListView();

    public static TicketWaitingListView getInstance() {
        return sInstance;
    }

    @Override
    public void updateProgress(int i, int i1) {

    }

    @Override
    public void snapPage(boolean b) {

    }

    @Override
    public void updateItemSelect(int i) {

    }

    @Override
    public void init() {
        super.init();
        LogUtil.logd(WinLayout.logTag + "TicketWaitingListView.init()");
    }

    @Override
    public ExtViewAdapter getView(ViewData viewData) {
        TicketWaitingListViewData data = (TicketWaitingListViewData) viewData;

        WinLayout.getInstance().vTips = null;
        LogUtil.logd(WinLayout.logTag + "TicketWaitingListView.getView() viewData:" + JSONObject.toJSONString(viewData));

        View view = createView(data);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = TrainTicketListView.getInstance();
        viewAdapter.showRecordView = false;// 不显示录音图标
        return viewAdapter;
    }

    public View createView(final TicketWaitingListViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.ticket_order_waiting, (ViewGroup) null);
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvDepartureStation = view.findViewById(R.id.tvDepartureStation);
        TextView tvArrivalStation = view.findViewById(R.id.tvArrivalStation);
        TextView tvTicketNum = view.findViewById(R.id.tvTicketNum);
        TextView tvTime = view.findViewById(R.id.tvTime);
        TextView tvSeatName = view.findViewById(R.id.tvSeatName);

        ImageView ivTrafficIcon = view.findViewById(R.id.ivTrafficIcon);

        ProgressBar pbName = view.findViewById(R.id.pbName);
        ProgressBar pbStation = view.findViewById(R.id.pbStation);
        ProgressBar pbTicketNum = view.findViewById(R.id.pbTicketNum);
        ProgressBar pbTime = view.findViewById(R.id.pbTime);

        ImageView ivStateName = view.findViewById(R.id.ivStateName);
        ImageView ivStateStation = view.findViewById(R.id.ivStateStation);
        ImageView ivStateTicketNum = view.findViewById(R.id.ivStateTicketNum);
        ImageView ivStateTime = view.findViewById(R.id.ivStateTime);


        tvName.setText(viewData.name);
        tvDepartureStation.setText(viewData.departureStation);
        tvArrivalStation.setText(viewData.arrivalStation);
        tvTicketNum.setText(viewData.ticketNum);
        tvTime.setText(viewData.departDate + " " + viewData.departureTime);
        tvSeatName.setText(viewData.seatName);

        if(viewData.isTrain) {
            ivTrafficIcon.setImageResource(R.drawable.trainlines_to1);
        } else {
            ivTrafficIcon.setImageResource(R.drawable.airlines_to2);
        }

        long checkTime = 1200;
        AppLogicBase.runOnUiGround(new ProgressCheck(pbName, ivStateName), checkTime);
        AppLogicBase.runOnUiGround(new ProgressCheck(pbStation, ivStateStation), checkTime * 2);
        AppLogicBase.runOnUiGround(new ProgressCheck(pbTicketNum, ivStateTicketNum), checkTime * 3);
        AppLogicBase.runOnUiGround(new ProgressCheck(pbTime, ivStateTime), checkTime * 4);
        AppLogicBase.runOnUiGround(timeoutRun, 62000);// 出票超时，与稍微比后台多一点，后台是六十秒

        return view;
    }

    // 出票失败
    private Runnable timeoutRun = new Runnable() {
        @Override
        public void run() {
            TXZTtsManager.getInstance().speakText("出票失败，请重试。");
            RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                    TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_TICKET_WAITING_TIMEOUT, 0, 0);
        }
    };

    /**
     * 关闭等待
     */
    @Override
    public void dismissWaiting(){
        LogUtil.logd(WinLayout.logTag + "TicketWaitingListView.dismissWaiting()");
        AppLogicBase.removeUiGroundCallback(timeoutRun);
    }

    private static class ProgressCheck implements Runnable{

        private ProgressBar pb;
        private ImageView iv;

        public ProgressCheck(ProgressBar pb, ImageView iv){
            this.pb = pb;
            this.iv = iv;
            iv.setVisibility(View.GONE);
        }

        @Override
        public void run() {
            pb.setVisibility(View.GONE);
            iv.setVisibility(View.VISIBLE);
        }
    }

}
