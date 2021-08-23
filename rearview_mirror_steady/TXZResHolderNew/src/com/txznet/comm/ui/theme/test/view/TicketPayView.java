package com.txznet.comm.ui.theme.test.view;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.QiwuTrainTicketPayViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITicketPayView;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.QRUtil;

import org.json.JSONException;
import org.json.JSONObject;

import static com.txznet.comm.ui.util.LayouUtil.getDimen;
import static com.txznet.comm.ui.util.LayouUtil.getDrawable;

/**
 * Created by daviddai on 2019/9/18
 */
public class TicketPayView extends ITicketPayView {

    private static TicketPayView sInstance = new TicketPayView();

    public static TicketPayView getInstance() {
        return sInstance;
    }

    private static final String TAG = "TicketPayView::";

    private String mCancelText = "取消订单";

    QiwuTrainTicketPayViewData.TicketPayBean ticketPayBean;

    @Override
    public ViewFactory.ViewAdapter getView(final ViewData data) {
        // 获取数据
        QiwuTrainTicketPayViewData viewData = (QiwuTrainTicketPayViewData) data;

        ticketPayBean = viewData.mTicketBeans.get(0);


        // 最外层的布局，用LinearLayout来实现
        LinearLayout view = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setLayoutParams(layoutParams);
        // title
        View title = genTitleLayout(viewData);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        view.addView(title, layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.pageFlightCount * SizeConfig.itemHeightPro);
        view.addView(llContents,layoutParams);

        // 内容部分
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        llContent.setOrientation(LinearLayout.VERTICAL);
        // 这里的高度是直接用了飞机票列表的高度，由于这个高度和我们的订单高度没有联系，所以不区分飞机票和火车票。
        int contentHeight = SizeConfig.pageFlightCount * SizeConfig.itemHeightPro;
        llContents.addView(llContent,
                new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1));

        mCurPage = viewData.mTitleInfo.curPage;
        mMaxPage = viewData.mTitleInfo.maxPage;
        LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
        //llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
        if(mMaxPage > 1){
            llContents.addView(llPager,layoutParams);
            WinLayout.getInstance().vTips = viewData.vTips;
        }

        // 上面部分
        llContent.addView(genTicketTitleLayout(),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));

        // 分割线
        View vDivider = new View(GlobalContext.get());
        vDivider.setBackgroundColor(0x4DFFFFFF);
        LinearLayout.LayoutParams vDividerLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ((int) getDimen("y2")));
        vDividerLayoutParams.leftMargin = (int) getDimen("x2");
        llContent.addView(vDivider, vDividerLayoutParams);

        // 下面部分
        LinearLayout llBottom = new LinearLayout(GlobalContext.get());
        llBottom.setOrientation(LinearLayout.HORIZONTAL);
        llBottom.setGravity(Gravity.CENTER_VERTICAL);
        llContent.addView(llBottom,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        // 信息部分
        View vInfo = genInfoLayout();
        LinearLayout.LayoutParams vInfoLayoutParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 6.5f);
        vInfoLayoutParams.gravity = Gravity.CENTER;
        int vInfoLeftMargin;
        if (getMaxPage() > 1) {
            vInfoLeftMargin =(int) getDimen("x16");
        }else {
            vInfoLeftMargin = (int) getDimen("x32");
        }
        vInfoLayoutParams.leftMargin = vInfoLeftMargin;
        vInfoLayoutParams.topMargin = (int) getDimen("y31");
        vInfoLayoutParams.bottomMargin = (int) getDimen("y31");
        llBottom.addView(vInfo, vInfoLayoutParams);

        // 支付部分
        View vPay = genPayLayout();
        LinearLayout.LayoutParams vPayLayoutParams =
                new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 3.5f);
        vPayLayoutParams.topMargin = (int) getDimen("y7");
        vPayLayoutParams.bottomMargin = (int) getDimen("y7");
        llBottom.addView(vPay, vPayLayoutParams);

        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.isListView = true;
        viewAdapter.object = TicketPayView.getInstance();
        return viewAdapter;
    }

    private View genTitleLayout(QiwuTrainTicketPayViewData viewData) {
        View result = null;
        ListTitleView titleView = ListTitleView.getInstance();
        if(ticketPayBean.payType.contains("flight")){
            result = titleView.getView(viewData, "", "飞机票订单").view;
            titleView.setIvIcon("flight");
        }else if(ticketPayBean.payType.contains("train")){
            result = titleView.getView(viewData, "", "火车票订单").view;
            titleView.setIvIcon("train");
        }
        return result;
    }

    /**
     * 返回用来展示车辆信息的view
     */
    private View genInfoLayout() {
        String payType = ticketPayBean.payType;

        QiwuTrainTicketPayViewData.FlightTicketPayBean flightTicketPayBean;
        QiwuTrainTicketPayViewData.TrainTicketPayBean trainTicketPayBean;
        String seatView = "";
        String seatValue = "";
        String ticketType = "";
        String ticketValue = "";
        String ticketTime = "";
        String timeValue = "";
        if (payType.contains("flight")) {
            flightTicketPayBean = (QiwuTrainTicketPayViewData.FlightTicketPayBean) ticketPayBean;
            seatView = "机建燃油:";
            seatValue = flightTicketPayBean.fuelSurcharge;
            ticketType = "航    班:";
            ticketValue = flightTicketPayBean.ticketNo;
            ticketTime = "登机时间:";
            timeValue = flightTicketPayBean.departureTime;
        } else if (payType.contains("train")) {
            trainTicketPayBean = (QiwuTrainTicketPayViewData.TrainTicketPayBean) ticketPayBean;
            seatView = "座    位:";
            seatValue = trainTicketPayBean.seat;
            ticketType = "车    次:";
            ticketValue = trainTicketPayBean.ticketNo;
            ticketTime = "登车时间:";
            timeValue = trainTicketPayBean.departureTime;
        }
        RelativeLayout result = new RelativeLayout(GlobalContext.get());

        // 第一行
        // 姓名
        View vName = genInfoItemView("姓    名:", ticketPayBean.passengerName);
        vName.setId(ViewUtils.generateViewId());
        result.addView(vName, new RelativeLayout.LayoutParams((int) getDimen("x153"),
                ViewGroup.LayoutParams.WRAP_CONTENT));

        View vTrainNo = genInfoItemView(ticketType, ticketValue);
        vTrainNo.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams vTrainNoLayoutParams =
                new RelativeLayout.LayoutParams(((int) getDimen("x153")),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        vTrainNoLayoutParams.addRule(RelativeLayout.RIGHT_OF, vName.getId());
        vTrainNoLayoutParams.addRule(RelativeLayout.ALIGN_TOP, vName.getId());
        vTrainNoLayoutParams.leftMargin = (int) getDimen("x16");
        result.addView(vTrainNo, vTrainNoLayoutParams);

        // 第二行
        // 始发地
        View vStation = genInfoItemView("始发地:", ticketPayBean.station);
        vStation.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams vStationLayoutParams =
                new RelativeLayout.LayoutParams(((int) getDimen("x153")),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        vStationLayoutParams.addRule(RelativeLayout.BELOW, vName.getId());
        vStationLayoutParams.topMargin = (int) getDimen("y24");
        result.addView(vStation, vStationLayoutParams);

        // 到达地
        View vEndStation = genInfoItemView("到达地:", ticketPayBean.endStation);
        vEndStation.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams vEndStationLayoutParams =
                new RelativeLayout.LayoutParams(((int) getDimen("x153")),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        vEndStationLayoutParams.addRule(RelativeLayout.RIGHT_OF, vStation.getId());
        vEndStationLayoutParams.addRule(RelativeLayout.ALIGN_TOP, vStation.getId());
        vEndStationLayoutParams.leftMargin = (int) getDimen("x16");
        result.addView(vEndStation, vEndStationLayoutParams);

        // 第三行
        // 日期
        View vDepartureTime = genInfoItemView("日    期:", ticketPayBean.departureDate);
        vDepartureTime.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams vDepartureTimeLayoutParams =
                new RelativeLayout.LayoutParams(((int) getDimen("x153")),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        vDepartureTimeLayoutParams.addRule(RelativeLayout.BELOW, vStation.getId());
        vDepartureTimeLayoutParams.topMargin = (int) getDimen("y24");
        result.addView(vDepartureTime, vDepartureTimeLayoutParams);

        // 时长
        View vCostTime = genInfoItemView(ticketTime, timeValue);
        vCostTime.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams vCostTimeLayoutParams =
                new RelativeLayout.LayoutParams(((int) getDimen("x153")),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        vCostTimeLayoutParams.addRule(RelativeLayout.RIGHT_OF, vDepartureTime.getId());
        vCostTimeLayoutParams.addRule(RelativeLayout.ALIGN_TOP, vDepartureTime.getId());
        vCostTimeLayoutParams.leftMargin = (int) getDimen("x16");
        result.addView(vCostTime, vCostTimeLayoutParams);

        // 第四行
        // 成人票
        View vPrice = genInfoItemView("成人票:", "¥" + ticketPayBean.price);
        vPrice.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams vPriceLayoutParams =
                new RelativeLayout.LayoutParams(((int) getDimen("x153")),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        vPriceLayoutParams.addRule(RelativeLayout.BELOW, vDepartureTime.getId());
        vPriceLayoutParams.topMargin = (int) getDimen("y24");
        result.addView(vPrice, vPriceLayoutParams);

        // 座位
        View vSeat = genInfoItemView(seatView, seatValue);
        vSeat.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams vSeatLayoutParams =
                new RelativeLayout.LayoutParams(((int) getDimen("x153")),
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        vSeatLayoutParams.addRule(RelativeLayout.RIGHT_OF, vPrice.getId());
        vSeatLayoutParams.addRule(RelativeLayout.ALIGN_TOP, vPrice.getId());
        vSeatLayoutParams.leftMargin = (int) getDimen("x16");
        result.addView(vSeat, vSeatLayoutParams);

        return result;
    }

    TextView tvRemainTime;
    long remainTime;
    Runnable remainTimeRun = new Runnable() {
        @Override
        public void run() {
            AppLogicBase.removeUiGroundCallback(this);
            remainTime = remainTime - 1;
            if(remainTime < 0){
                remainTime  = 0;
            }
            long remainHour = remainTime / 60;
            long remainSecond = remainTime % 60;
            if (remainSecond < 10) {
                tvRemainTime.setText("剩余支付时间: " + remainHour + ":0" + remainSecond);
            } else {
                tvRemainTime.setText("剩余支付时间: " + remainHour + ":" + remainSecond);
            }
            if (remainTime > 0) {
                AppLogicBase.runOnUiGround(this, 1000);
            }
        }
    };

    /**
     * 返回展示剩余时间和取消订单按钮的布局
     */
    private View genTicketTitleLayout() {
        String payType = ticketPayBean.payType;
        LinearLayout result = new LinearLayout(GlobalContext.get());
        result.setGravity(Gravity.CENTER_VERTICAL);

        // 表示这是待支付界面的图标
        ImageView ivIcon = new ImageView(GlobalContext.get());

        LinearLayout.LayoutParams iconLayoutParams =
                new LinearLayout.LayoutParams(((int) getDimen("m48")),
                        ((int) getDimen("m48")));
        iconLayoutParams.leftMargin = ((int) getDimen("x32"));
        iconLayoutParams.topMargin = ((int) getDimen("y16"));
        iconLayoutParams.bottomMargin = ((int) getDimen("y16"));
        result.addView(ivIcon, iconLayoutParams);

        // 包含title待支付和剩余时间的layout
        LinearLayout llTitle = new LinearLayout(GlobalContext.get());
        llTitle.setOrientation(LinearLayout.VERTICAL);

        // title
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setGravity(Gravity.CENTER_VERTICAL);
        tvTitle.setSingleLine();
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        tvTitle.setIncludeFontPadding(false);

        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m23"));
        tvTitle.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams tvTitleLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int) getDimen("y32"));
        llTitle.addView(tvTitle, tvTitleLayoutParams);

        // 剩余时间
        remainTime = Long.valueOf(ticketPayBean.expirationTime);

        if ("train".equals(payType) || "flight".equals(payType)) {
            tvRemainTime = new TextView(GlobalContext.get());
            tvRemainTime.setGravity(Gravity.CENTER_VERTICAL);
            tvRemainTime.setSingleLine();
            tvRemainTime.setEllipsize(TextUtils.TruncateAt.END);
            tvRemainTime.setIncludeFontPadding(false);
            tvRemainTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m18"));
            tvRemainTime.setTextColor(0x99FFFFFF);
            long remainHour = remainTime / 60;
            long remainSecond = remainTime % 60;
            tvRemainTime.setText("剩余支付时间: " + remainHour + ":" + remainSecond);
            LinearLayout.LayoutParams tvRemainTitleLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (int) getDimen("y24"));
            tvRemainTitleLayoutParams.topMargin = (int) getDimen("y5");
            llTitle.addView(tvRemainTime, tvRemainTitleLayoutParams);
            AppLogicBase.runOnUiGround(remainTimeRun, 1000);
        }else if(payType.contains("REFUND")){
            TextView tvRefundHint = new TextView(GlobalContext.get());
            tvRefundHint.setGravity(Gravity.CENTER_VERTICAL);
            tvRefundHint.setSingleLine();
            tvRefundHint.setEllipsize(TextUtils.TruncateAt.END);
            tvRefundHint.setIncludeFontPadding(false);
            tvRefundHint.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m18"));
            tvRefundHint.setTextColor(0x99FFFFFF);
            tvRefundHint.setText("资金在3-5个工作日原来返回");
            LinearLayout.LayoutParams tvRemainTitleLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, (int) getDimen("y24"));
            tvRemainTitleLayoutParams.topMargin = (int) getDimen("y5");
            llTitle.addView(tvRefundHint, tvRemainTitleLayoutParams);
        }

        LinearLayout.LayoutParams llTitleLayoutParams = new LinearLayout.LayoutParams(
                (int) getDimen("x238"),
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llTitleLayoutParams.leftMargin = (int) getDimen("x16");
        llTitleLayoutParams.topMargin = (int) getDimen("y10");
        llTitleLayoutParams.bottomMargin = (int) getDimen("y10");
        result.addView(llTitle, llTitleLayoutParams);

        // 包含取消按钮的layout。为了将按钮居中才额外加上这个布局的。

        if(!payType.contains("CANCEL")){
            FrameLayout flCancel = new FrameLayout(GlobalContext.get());

            // 取消订单按钮
            TextView btnCancel = new TextView(GlobalContext.get());
            btnCancel.setGravity(Gravity.CENTER);
            btnCancel.setSingleLine();
            btnCancel.setEllipsize(TextUtils.TruncateAt.END);
            btnCancel.setIncludeFontPadding(false);
            if(payType.contains("Success")){
                btnCancel.setText("退票");
                btnCancel.setOnClickListener(cancelOrd);
            }else if(payType.contains("REFUND")){
                btnCancel.setText("退款中");
                btnCancel.setPressed(true);
                btnCancel.setEnabled(false);
            }else if(payType.contains("Payed")){
                btnCancel.setText("出票中");
                btnCancel.setPressed(true);
                btnCancel.setEnabled(false);
            }
            else {
                btnCancel.setText(mCancelText);
                btnCancel.setOnClickListener(cancelOrd);
            }
            btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m16"));
            btnCancel.setTextColor(0xFFFFFFFF);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnCancel.setBackground(getDrawable("qiwu_ticket_pending_order_cancel_btn_bg"));
            }
            FrameLayout.LayoutParams btnCancelLayoutParams = new FrameLayout.LayoutParams(
                    (int) getDimen("x110"),
                    ((int) getDimen("y32")));
            btnCancelLayoutParams.gravity = Gravity.CENTER;
            flCancel.addView(btnCancel, btnCancelLayoutParams);

            LinearLayout.LayoutParams flCancelLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            result.addView(flCancel, flCancelLayoutParams);
        }

        if ("flight".equals(payType) || "train".equals(payType)) {
            ivIcon.setImageDrawable(getDrawable("qiwu_ticket_pending_order_icon"));
            tvTitle.setText("待支付");
        }else if(payType.contains("Success")){
            ivIcon.setImageDrawable(getDrawable("qiwu_ticket_pay_ok"));
            tvTitle.setText("购票成功");
        }else if(payType.contains("Payed")){
            ivIcon.setImageDrawable(getDrawable("qiwu_ticket_pay_ok"));
            tvTitle.setText("支付成功");
        }
        else if(payType.contains("CANCEL")){
            ivIcon.setImageDrawable(getDrawable("qiwu_cancel_ord_icon"));
            tvTitle.setText("无效票");
        }else if(payType.contains("REFUND")){
            ivIcon.setImageDrawable(getDrawable("qiwu_refund_icon"));
            tvTitle.setText("退票成功");
        }
        return result;
    }

    /**
     * 生成信息项的view
     */
    private View genInfoItemView(String key, String value) {
        LinearLayout result = new LinearLayout(GlobalContext.get());
        result.setOrientation(LinearLayout.HORIZONTAL);
        result.setGravity(Gravity.CENTER_VERTICAL);

        // key部分
        TextView tvKey = new TextView(GlobalContext.get());
        tvKey.setGravity(Gravity.CENTER_VERTICAL);
        tvKey.setSingleLine();
        tvKey.setEllipsize(TextUtils.TruncateAt.END);
        tvKey.setIncludeFontPadding(true); // false的时候，太少，改成true
        tvKey.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m19"));
        tvKey.setTextColor(0x99FFFFFF);
        tvKey.setText(key);
        result.addView(tvKey, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // value部分
        TextView tvValue = new TextView(GlobalContext.get());
        tvValue.setGravity(Gravity.CENTER_VERTICAL);
        tvValue.setSingleLine();
        tvValue.setEllipsize(TextUtils.TruncateAt.END);
        tvValue.setIncludeFontPadding(true);
        tvValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m19"));
        tvValue.setTextColor(0xFFFFFFFF);
        tvValue.setText(value);
        LinearLayout.LayoutParams tvValueLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        tvValueLayoutParams.leftMargin = (int) getDimen("x8");
        result.addView(tvValue,
                tvValueLayoutParams);

        return result;
    }

    /**
     * 返回展示价格和支付方式的view
     */
    private View genPayLayout() {
        LinearLayout result = new LinearLayout(GlobalContext.get());
        result.setOrientation(LinearLayout.VERTICAL);
        result.setGravity(Gravity.CENTER);
        String payType = ticketPayBean.payType;
        if (!("train".equals(payType) || "flight".equals(payType))) {
            return result;
        }

        // 包含价格的layout
        LinearLayout llPrice = new LinearLayout(GlobalContext.get());
        llPrice.setOrientation(LinearLayout.HORIZONTAL);
        llPrice.setGravity(Gravity.CENTER);

        // 表示价格的key
        TextView tvPriceKey = new TextView(GlobalContext.get());
        tvPriceKey.setGravity(Gravity.CENTER_VERTICAL);
        tvPriceKey.setSingleLine();
        tvPriceKey.setEllipsize(TextUtils.TruncateAt.END);
        tvPriceKey.setIncludeFontPadding(false);
        tvPriceKey.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m16"));
        tvPriceKey.setTextColor(0x99FFFFFF);
        tvPriceKey.setText("总价：");
        LinearLayout.LayoutParams tvPriceKeyLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llPrice.addView(tvPriceKey, tvPriceKeyLayoutParams);

        // 表示价格的value
        TextView tvPriceValue = new TextView(GlobalContext.get());
        tvPriceValue.setGravity(Gravity.CENTER);
        tvPriceValue.setSingleLine();
        tvPriceValue.setEllipsize(TextUtils.TruncateAt.END);
        tvPriceValue.setIncludeFontPadding(false);
        tvPriceValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m23"));
        tvPriceValue.setTextColor(0xFFFF8205);
        SpannableStringBuilder ssb = new SpannableStringBuilder("¥" + ticketPayBean.price);
        ssb.setSpan(new RelativeSizeSpan(0.8f), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPriceValue.setText(ssb);
        LinearLayout.LayoutParams tvPriceValueLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llPrice.addView(tvPriceValue, tvPriceValueLayoutParams);

        result.addView(llPrice, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // 二维码

        ivQrcode = new ImageView(GlobalContext.get());
        ivQrcode.setBackgroundColor(0xFFFFFFFF);
        // 给二维码添加logo的方法没有白边，所以需要自己通过padding实现白边的效果。
        int padding = (int) getDimen("m4");
        ivQrcode.setPadding(padding, padding, padding, padding);
        Bitmap logoVX = ((BitmapDrawable) getDrawable("qiwu_ticket_wechat_pay_icon")).getBitmap();

        bitmapVX = QRUtil.createQRCodeWithLogo(ticketPayBean.parUrlWX,
                (int) getDimen("m160"), 1, ErrorCorrectionLevel.H, logoVX, ((int) getDimen("m40")));
        UI2Manager.runOnUIThread(new Runnable() {

            @Override
            public void run() {
                if (bitmapVX == null) {
                    return;
                }
                ivQrcode.setImageBitmap(bitmapVX);
            }
        }, 0);

        Bitmap logoZFB = ((BitmapDrawable) getDrawable("qiwu_ticket_alipay_icon")).getBitmap();

        bitmapZFB = QRUtil.createQRCodeWithLogo(ticketPayBean.payUrlZFB,
                (int) getDimen("m160"), 1, ErrorCorrectionLevel.H, logoZFB, ((int) getDimen("m40")));


        LinearLayout.LayoutParams ivQrcodeLayoutParams =
                new LinearLayout.LayoutParams(((int) getDimen("m160")), ((int) getDimen("m160")));
        ivQrcodeLayoutParams.gravity = Gravity.CENTER;
        result.addView(ivQrcode, ivQrcodeLayoutParams);

        // 切换支付方式

        ivSwitch = new ImageView(GlobalContext.get());
        ivSwitch.setImageDrawable(getDrawable("qiwu_ticket_pending_order_select_wechat_pay"));
        LinearLayout.LayoutParams ivSwitchLayoutParams =
                new LinearLayout.LayoutParams((int) getDimen("x157"),
                        ((int) getDimen("y32")));
        ivSwitchLayoutParams.topMargin = (int) getDimen("y4");
        ivSwitchLayoutParams.gravity = Gravity.CENTER;
        ivSwitch.setOnClickListener(switchListener);
        result.addView(ivSwitch, ivSwitchLayoutParams);

        return result;
    }

    Bitmap bitmapZFB;
    Bitmap bitmapVX;
    ImageView ivQrcode;
    ImageView ivSwitch;
    boolean urlType = false;

    View.OnClickListener switchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (urlType) {
                ivSwitch.setImageDrawable(
                        getDrawable("qiwu_ticket_pending_order_select_wechat_pay"));
                urlType = false;
                UI2Manager.runOnUIThread(new Runnable() {

                    @Override
                    public void run() {
                        if (bitmapVX == null) {
                            return;
                        }
                        ivQrcode.setImageBitmap(bitmapVX);
                    }
                }, 0);
            } else {
                ivSwitch.setImageDrawable(getDrawable("qiwu_ticket_pending_order_select_alipay"));
                urlType = true;
                UI2Manager.runOnUIThread(new Runnable() {

                    @Override
                    public void run() {
                        if (bitmapZFB == null) {
                            return;
                        }
                        ivQrcode.setImageBitmap(bitmapZFB);
                    }
                }, 0);
            }
        }
    };

    View.OnClickListener cancelOrd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", ticketPayBean.payType);
                jsonObject.put("orderId", ticketPayBean.orderId);
                jsonObject.put("sonAccount", ticketPayBean.sonAccount);
                jsonObject.put("idNumber", ticketPayBean.idNumber);
                jsonObject.put("phoneNum", ticketPayBean.phoneNum);
                jsonObject.put("sonAccount", ticketPayBean.sonAccount);
                jsonObject.put("passengerName", ticketPayBean.passengerName);
                jsonObject.put("orderUniqueId", ticketPayBean.orderUniqueId);
                jsonObject.put("canRefund", ticketPayBean.canRefund);
                if(ticketPayBean.payType.contains("train")){
                    QiwuTrainTicketPayViewData.TrainTicketPayBean tb = (QiwuTrainTicketPayViewData.TrainTicketPayBean)ticketPayBean;
                    jsonObject.put("passengerId", tb.passengeId);
                }else {
                    jsonObject.put("passengerId", "");
                }
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_TICKET_INFO_CANCEL, 0, 0,
                        1,jsonObject.toString());
            } catch (JSONException e) {
                return;
            }
        }
    };

    @Override
    public void updateProgress(final int i, final int i1) {

    }

    @Override
    public void snapPage(final boolean b) {

    }

    @Override
    public void updateItemSelect(final int i) {

    }
}
