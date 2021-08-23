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
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.dialog.LoadingDialog;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.QiWuPhoneRechargePayViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IQiWuPhoneRechargePayView;
import com.txznet.resholder.R;
import com.txznet.txz.util.QRUtil;

/**
 * 说明：话费充值付款页面
 *
 * @author xiaolin
 * create at 2020-09-07 16:19
 */
public class QiWuPhoneRechargePayView extends IQiWuPhoneRechargePayView {

    private static QiWuPhoneRechargePayView sInstance = new QiWuPhoneRechargePayView();

    public static QiWuPhoneRechargePayView getInstance() {
        return sInstance;
    }

    private LoadingDialog mLoadingDialog;
    private boolean isWx = true;
    private Bitmap bitmapVX, bitmapZFB;

    @Override
    public ExtViewAdapter getView(ViewData data) {
        QiWuPhoneRechargePayViewData viewData = (QiWuPhoneRechargePayViewData) data;
        WinLayout.getInstance().vTips = viewData.vTips;
        LogUtil.logd(WinLayout.logTag + "QiWuPhoneRechargePayView.getView() viewData:" + com.alibaba.fastjson.JSONObject.toJSONString(viewData));

        // 关闭之前的对话框
        if (mLoadingDialog != null && mLoadingDialog.isShow()) {
            mLoadingDialog.dismiss();
        }

        View view = getView(viewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.isListView = false;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.object = getInstance();
        viewAdapter.cardHeightType = ExtViewAdapter.SIZE_TYPE.MATCH_PARENT;// 内容占卡片最大
        return viewAdapter;
    }

    private View getView(QiWuPhoneRechargePayViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.sim_order, (ViewGroup) null);
        ViewGroup payWrap = view.findViewById(R.id.payWarp);

        ImageView ivIcon = view.findViewById(R.id.ivIcon);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvPriceTitle = view.findViewById(R.id.tvPriceTitle);
        final ImageView ivQrCode = view.findViewById(R.id.ivQrCode);
        final ImageView ivPayType = view.findViewById(R.id.ivPayType);
        TextView tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        TextView tvOperator = view.findViewById(R.id.tvOperator);
        TextView tvPrice = view.findViewById(R.id.tvPrice);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        if (viewData.isLoading) {
            mLoadingDialog = new LoadingDialog(context, getInstance());
            mLoadingDialog.setMessage("加载中...");
            mLoadingDialog.show();
            return view;
        }

        if (viewData.orderState == QiWuPhoneRechargePayViewData.ORDER_STATE_PAY_SUCCESS) {
            // 支付成功的情况
            ivIcon.setImageResource(R.drawable.qiwu_ticket_pay_ok);
            tvTitle.setText("充值成功");
            payWrap.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
        } else {
            ivIcon.setImageResource(R.drawable.qiwu_ticket_pending_order_icon);
            tvTitle.setText("待支付");
            tvPriceTitle.setText("¥" + viewData.amount);// 支付金额
            tvPhoneNumber.setText("" + viewData.phone);
            tvOperator.setText(viewData.operator);
            tvPrice.setText(viewData.denomination + "元");// 充值金额

            int qrCodeSide = (int) context.getResources().getDimension(R.dimen.m144);// 二维码大小
            int qrCodeLogoSide = qrCodeSide / 5;

            Bitmap logoVX = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.qiwu_ticket_wechat_pay_icon)).getBitmap();
            bitmapVX = QRUtil.createQRCodeWithLogo(viewData.wxQRCode, qrCodeSide, 1, ErrorCorrectionLevel.H, logoVX, qrCodeLogoSide);

            Bitmap logoZFB = ((BitmapDrawable) context.getResources().getDrawable(R.drawable.qiwu_ticket_alipay_icon)).getBitmap();
            bitmapZFB = QRUtil.createQRCodeWithLogo(viewData.zfbQRCode, qrCodeSide, 1, ErrorCorrectionLevel.H, logoZFB, qrCodeLogoSide);
            isWx = true;
            ivPayType.setImageResource(R.drawable.qiwu_ticket_pending_order_select_wechat_pay);
            ivQrCode.setImageBitmap(bitmapVX);
            ivPayType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isWx) {
                        ivPayType.setImageResource(R.drawable.qiwu_ticket_pending_order_select_wechat_pay);
                        ivQrCode.setImageBitmap(bitmapVX);
                    } else {
                        ivPayType.setImageResource(R.drawable.qiwu_ticket_pending_order_select_alipay);
                        ivQrCode.setImageBitmap(bitmapZFB);
                    }

                    isWx = !isWx;
                }
            });
        }

        return view;
    }
}
