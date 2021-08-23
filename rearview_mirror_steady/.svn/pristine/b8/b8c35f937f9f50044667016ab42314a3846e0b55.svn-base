package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.dialog.ConfirmDialog;
import com.txznet.comm.ui.theme.test.dialog.LoadingDialog;
import com.txznet.comm.ui.theme.test.utils.DimenUtils;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.QiwuTrainTicketPayViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITicketPayView;
import com.txznet.loader.AppLogicBase;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.QRUtil;

import org.json.JSONException;
import org.json.JSONObject;

import static com.txznet.comm.ui.util.LayouUtil.getDrawable;

/**
 * 说明：车票/飞机票订单页面
 *
 * @author xiaolin
 * create at 2020-08-26 14:51
 */
public class TicketPayView extends ITicketPayView {

    private static TicketPayView sInstance = new TicketPayView();

    public static TicketPayView getInstance() {
        return sInstance;
    }

    QiwuTrainTicketPayViewData.TicketPayBean ticketPayBean;

    private Bitmap bitmapZFB;
    private Bitmap bitmapVX;
    private boolean urlType = false; // true:支付宝，false:微信

    private LoadingDialog mLoadingDialog;

    @Override
    public void init() {
        super.init();
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        LogUtil.logd(WinLayout.logTag + "TrainTicket onUpdateParams: " + styleIndex);

    }

    //无屏布局参数
    private void initNone() {

    }

    @Override
    public ExtViewAdapter getView(final ViewData data) {
        // 获取数据
        QiwuTrainTicketPayViewData viewData = (QiwuTrainTicketPayViewData) data;
        WinLayout.getInstance().vTips = viewData.vTips;
        LogUtil.logd(WinLayout.logTag + "QiwuTrainTicketPayViewData.vTips:" + viewData.vTips);
        LogUtil.logd(WinLayout.logTag + "QiwuTrainTicketPayViewData.getview() json:" + com.alibaba.fastjson.JSONObject.toJSONString(data));

        if(mLoadingDialog != null && mLoadingDialog.isShow()){
            mLoadingDialog.dismiss();
        }

        View view = createViewNone(viewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = TicketPayView.getInstance();
        return viewAdapter;
    }

    @Override
    public void showConfirmCancelOrderDialog() {
        cancelOrderListener.onClick(null);
    }

    private View createViewNone(QiwuTrainTicketPayViewData viewData) {
        ticketPayBean = viewData.mTicketBeans.get(0);
        if (ticketPayBean.payType.contains("flight")) {
            return createViewFlight(viewData);
        } else if (ticketPayBean.payType.contains("train")) {
            return createViewTrain(viewData);
        }
        return null;
    }

    /**
     * 车票
     *
     * @param viewData
     * @return
     */
    private View createViewTrain(QiwuTrainTicketPayViewData viewData) {
        QiwuTrainTicketPayViewData.TrainTicketPayBean bean = (QiwuTrainTicketPayViewData.TrainTicketPayBean) ticketPayBean;


        final Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.ticket_train_order_unpaid, (ViewGroup) null);

        View surplusPayTimeWrap = view.findViewById(R.id.surplusPayTimeWrap);
        View payWrap = view.findViewById(R.id.payWarp);

        ImageView ivIcon = view.findViewById(R.id.ivIcon);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        final TextView tvSurplusPayTime = view.findViewById(R.id.tvSurplusPayTime);// 待支付时间
        TextView tvPriceTitle = view.findViewById(R.id.tvPriceTitle);
        final ImageView ivQrCode = view.findViewById(R.id.ivQrCode);
        final ImageView ivPayType = view.findViewById(R.id.ivPayType);// 支付类型切换
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvTrainNum = view.findViewById(R.id.tvTrainNum);
        TextView tvBeginAddr = view.findViewById(R.id.tvBeginAddr);
        TextView tvEndAddr = view.findViewById(R.id.tvEndAddr);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvTime = view.findViewById(R.id.tvTime);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvSeat = view.findViewById(R.id.tvSeat);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        TextView tvTips = view.findViewById(R.id.tvTips);

        String payType = bean.payType;
        if ("flight".equals(payType) || "train".equals(payType)) {
            ivIcon.setImageResource(R.drawable.qiwu_ticket_pending_order_icon);
            tvTitle.setText("待支付");
        } else if (payType.contains("Success")) {
            ivIcon.setImageResource(R.drawable.qiwu_ticket_pay_ok);
            tvTitle.setText("购票成功");
        } else if (payType.contains("Payed")) {
            ivIcon.setImageResource(R.drawable.qiwu_ticket_pay_ok);
            tvTitle.setText("支付成功");
        } else if (payType.contains("CANCEL")) {
            ivIcon.setImageResource(R.drawable.qiwu_cancel_ord_icon);
            tvTitle.setText("无效票");
        } else if (payType.contains("REFUND")) {
            ivIcon.setImageResource(R.drawable.qiwu_refund_icon);
            tvTitle.setText("退票成功");
        }

        // 取消订单按钮
        if ("flight".equals(payType) || "train".equals(payType)) {
            btnCancel.setText("取消订单");
            btnCancel.setOnClickListener(cancelOrderListener);
        } else if (payType.contains("Success")) {
            btnCancel.setText("退票");
            btnCancel.setOnClickListener(cancelOrderListener);
        } else if (payType.contains("Payed")) {
            btnCancel.setText("出票中");
            btnCancel.setPressed(true);
            btnCancel.setEnabled(false);
        } else if (payType.contains("CANCEL")) {
            btnCancel.setVisibility(View.GONE);
        } else if (payType.contains("REFUND")) {
            btnCancel.setText("退款中");
            btnCancel.setPressed(true);
            btnCancel.setEnabled(false);
        }

        if ("flight".equals(payType) || "train".equals(payType)) {// 待支付
            payWrap.setVisibility(View.VISIBLE);
        } else {
            payWrap.setVisibility(View.GONE);
        }

        if (payType.contains("REFUND")) { // 退票成功
            tvTips.setVisibility(View.VISIBLE);
        } else {
            tvTips.setVisibility(View.GONE);
        }


        if ("flight".equals(payType) || "train".equals(payType)) {// 待支付
            AppLogicBase.runOnUiGround(new Runnable() {

                // 剩余时间
                long remainTime = Long.parseLong(ticketPayBean.expirationTime);

                @Override
                public void run() {
                    AppLogicBase.removeUiGroundCallback(this);
                    remainTime = remainTime - 1;
                    if (remainTime < 0) {
                        remainTime = 0;
                    }
                    long remainHour = remainTime / 60;
                    long remainSecond = remainTime % 60;
                    if (remainSecond < 10) {
                        tvSurplusPayTime.setText(remainHour + ":0" + remainSecond);
                    } else {
                        tvSurplusPayTime.setText(remainHour + ":" + remainSecond);
                    }
                    if (remainTime > 0) {
                        AppLogicBase.runOnUiGround(this, 1000);
                    }
                }
            }, 1000);
        } else {
            surplusPayTimeWrap.setVisibility(View.GONE);// 隐藏时间
        }

        int qrCodeSide = (int) context.getResources().getDimension(R.dimen.m144);// 二维码大小
        int qrCodeLogoSide = qrCodeSide / 5;

        Bitmap logoVX = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.qiwu_ticket_wechat_pay_icon)).getBitmap();
        bitmapVX = QRUtil.createQRCodeWithLogo(ticketPayBean.parUrlWX, qrCodeSide, 1, ErrorCorrectionLevel.H, logoVX, qrCodeLogoSide);

        Bitmap logoZFB = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.qiwu_ticket_alipay_icon)).getBitmap();
        bitmapZFB = QRUtil.createQRCodeWithLogo(ticketPayBean.payUrlZFB, qrCodeSide, 1, ErrorCorrectionLevel.H, logoZFB, qrCodeLogoSide);

        ivQrCode.setImageBitmap(bitmapVX);
        ivPayType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (urlType) {
                    urlType = false;
                    ivPayType.setImageResource(R.drawable.qiwu_ticket_pending_order_select_wechat_pay);
                    ivQrCode.setImageBitmap(bitmapVX);
                } else {
                    urlType = true;
                    ivPayType.setImageResource(R.drawable.qiwu_ticket_pending_order_select_alipay);
                    ivQrCode.setImageBitmap(bitmapZFB);
                }
            }
        });


        tvPriceTitle.setText("¥" + bean.price);
        tvName.setText(bean.passengerName);
        tvTrainNum.setText(bean.ticketNo);
        tvBeginAddr.setText(bean.station);
        tvEndAddr.setText(bean.endStation);
        tvDate.setText(bean.departureDate);
        tvTime.setText(bean.departureTime);
        tvPrice.setText("¥" + bean.price);
        tvSeat.setText(bean.seat);

        return view;
    }

    /**
     * 飞机票
     *
     * @param viewData
     * @return
     */
    private View createViewFlight(QiwuTrainTicketPayViewData viewData) {
        QiwuTrainTicketPayViewData.FlightTicketPayBean bean = (QiwuTrainTicketPayViewData.FlightTicketPayBean) ticketPayBean;


        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.ticket_flight_order_unpaid, (ViewGroup) null);

        View surplusPayTimeWrap = view.findViewById(R.id.surplusPayTimeWrap);
        View payWrap = view.findViewById(R.id.payWarp);

        ImageView ivIcon = view.findViewById(R.id.ivIcon);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        final TextView tvSurplusPayTime = view.findViewById(R.id.tvSurplusPayTime);// 待支付时间
        TextView tvPriceTitle = view.findViewById(R.id.tvPriceTitle);
        final ImageView ivQrCode = view.findViewById(R.id.ivQrCode);
        final ImageView ivPayType = view.findViewById(R.id.ivPayType);// 支付类型切换
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvFlightNo = view.findViewById(R.id.tvFlightNo);
        TextView tvBeginAddr = view.findViewById(R.id.tvBeginAddr);
        TextView tvEndAddr = view.findViewById(R.id.tvEndAddr);
        TextView tvDate = view.findViewById(R.id.tvDate);
        TextView tvTime = view.findViewById(R.id.tvTime);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        TextView tvFuelSurcharge = view.findViewById(R.id.tvFuelSurcharge);
        Button btnCancel = view.findViewById(R.id.btnCancel);
        TextView tvTips = view.findViewById(R.id.tvTips);

        String payType = bean.payType;
        if ("flight".equals(payType) || "train".equals(payType)) {
            ivIcon.setImageResource(R.drawable.qiwu_ticket_pending_order_icon);
            tvTitle.setText("待支付");
        } else if (payType.contains("Success")) {
            ivIcon.setImageResource(R.drawable.qiwu_ticket_pay_ok);
            tvTitle.setText("购票成功");
        } else if (payType.contains("Payed")) {
            ivIcon.setImageResource(R.drawable.qiwu_ticket_pay_ok);
            tvTitle.setText("支付成功");
        } else if (payType.contains("CANCEL")) {
            ivIcon.setImageResource(R.drawable.qiwu_cancel_ord_icon);
            tvTitle.setText("无效票");
        } else if (payType.contains("REFUND")) {
            ivIcon.setImageResource(R.drawable.qiwu_refund_icon);
            tvTitle.setText("退票成功");
        }

        // 取消订单按钮
        if ("flight".equals(payType) || "train".equals(payType)) {
            btnCancel.setText("取消订单");
            btnCancel.setOnClickListener(cancelOrderListener);
        } else if (payType.contains("Success")) {
            btnCancel.setText("退票");
            btnCancel.setOnClickListener(cancelOrderListener);
        } else if (payType.contains("Payed")) {
            btnCancel.setText("出票中");
            btnCancel.setPressed(true);
            btnCancel.setEnabled(false);
        } else if (payType.contains("CANCEL")) {
            btnCancel.setVisibility(View.GONE);
        } else if (payType.contains("REFUND")) {
            btnCancel.setText("退款中");
            btnCancel.setPressed(true);
            btnCancel.setEnabled(false);
        }

            if ("flight".equals(payType) || "train".equals(payType)) {// 待支付
            payWrap.setVisibility(View.VISIBLE);
        } else {
            payWrap.setVisibility(View.GONE);
        }

        if (payType.contains("REFUND")) { // 退票成功
            tvTips.setVisibility(View.VISIBLE);
        } else {
            tvTips.setVisibility(View.GONE);
        }

        if ("flight".equals(payType) || "train".equals(payType)) {// 待支付
            AppLogicBase.runOnUiGround(new Runnable() {

                // 剩余时间
                long remainTime = Long.parseLong(ticketPayBean.expirationTime);

                @Override
                public void run() {
                    AppLogicBase.removeUiGroundCallback(this);
                    remainTime = remainTime - 1;
                    if (remainTime < 0) {
                        remainTime = 0;
                    }
                    long remainHour = remainTime / 60;
                    long remainSecond = remainTime % 60;
                    if (remainSecond < 10) {
                        tvSurplusPayTime.setText(remainHour + ":0" + remainSecond);
                    } else {
                        tvSurplusPayTime.setText(remainHour + ":" + remainSecond);
                    }
                    if (remainTime > 0) {
                        AppLogicBase.runOnUiGround(this, 1000);
                    }
                }
            }, 1000);
        } else {
            surplusPayTimeWrap.setVisibility(View.GONE);// 隐藏时间
        }

        int qrCodeSide = (int) context.getResources().getDimension(R.dimen.m144);// 二维码大小
        int qrCodeLogoSide = qrCodeSide / 5;

        Bitmap logoVX = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.qiwu_ticket_wechat_pay_icon)).getBitmap();
        bitmapVX = QRUtil.createQRCodeWithLogo(ticketPayBean.parUrlWX, qrCodeSide, 1, ErrorCorrectionLevel.H, logoVX, qrCodeLogoSide);

        Bitmap logoZFB = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.qiwu_ticket_alipay_icon)).getBitmap();
        bitmapZFB = QRUtil.createQRCodeWithLogo(ticketPayBean.payUrlZFB, qrCodeSide, 1, ErrorCorrectionLevel.H, logoZFB, qrCodeLogoSide);

        ivQrCode.setImageBitmap(bitmapVX);
        ivPayType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (urlType) {
                    urlType = false;
                    ivPayType.setImageResource(R.drawable.qiwu_ticket_pending_order_select_wechat_pay);
                    ivQrCode.setImageBitmap(bitmapVX);
                } else {
                    urlType = true;
                    ivPayType.setImageResource(R.drawable.qiwu_ticket_pending_order_select_alipay);
                    ivQrCode.setImageBitmap(bitmapZFB);
                }
            }
        });


        tvPriceTitle.setText("¥" + bean.price);
        tvName.setText(bean.passengerName);
        tvFlightNo.setText(bean.ticketNo);
        tvBeginAddr.setText(bean.station);
        tvEndAddr.setText(bean.endStation);
        tvDate.setText(bean.departureDate);
        tvTime.setText(bean.departureTime);
        tvPrice.setText("¥" + bean.price);
        tvFuelSurcharge.setText("¥" + bean.fuelSurcharge);

        return view;
    }

    private View.OnClickListener cancelOrderListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context = UIResLoader.getInstance().getModifyContext();
            ConfirmDialog dialog = new ConfirmDialog(context, TicketPayView.this);
            dialog.setTitle("温馨提示");
            dialog.setMessage("确定要取消订单吗？");
            dialog.setDoneButton("确定", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cancelOrder();
                    showCancellingDialog();
                }
            });
            dialog.setCancelButton("取消", null);
            dialog.show();
        }
    };

    /**
     * 取消订单
     */
    private void cancelOrder() {
        LogUtil.logd(WinLayout.logTag + "QiwuTicketPayView cancelOrder");
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
            if (ticketPayBean.payType.contains("train")) {
                QiwuTrainTicketPayViewData.TrainTicketPayBean tb = (QiwuTrainTicketPayViewData.TrainTicketPayBean) ticketPayBean;
                jsonObject.put("passengerId", tb.passengeId);
            } else {
                jsonObject.put("passengerId", "");
            }
            RecordWin2Manager.getInstance().operateView(
                    TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                    TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_TICKET_INFO_CANCEL, 0, 0,
                    1, jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示取消中对话框
     */
    private void showCancellingDialog(){
        LogUtil.logd(WinLayout.logTag + "QiwuTicketPayView.showCancellingDialog()");
        mLoadingDialog = new LoadingDialog(UIResLoader.getInstance().getModifyContext());
        mLoadingDialog.setMessage("退票中\n请耐心等候...");
        mLoadingDialog.show();
    }

    /**
     * 关闭取消中的对话框
     */
    @Override
    public void dismissCancellingDialog() {
        if(mLoadingDialog != null && mLoadingDialog.isShow()){
            mLoadingDialog.dismiss();
        }
    }

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
