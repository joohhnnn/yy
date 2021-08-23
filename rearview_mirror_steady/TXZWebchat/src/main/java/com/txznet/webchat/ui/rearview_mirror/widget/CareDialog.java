package com.txznet.webchat.ui.rearview_mirror.widget;

import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.otto.Subscribe;
import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusOperationPresenter;
import com.txznet.webchat.R;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.TXZBindStore;
import com.txznet.webchat.ui.base.widgets.AppBaseWinDialog;
import com.txznet.webchat.ui.rearview_mirror.BindReasonActivity;
import com.txznet.webchat.util.QRCodeHandler;

import butterknife.Bind;

/**
 * 关注设备Dialog
 * Created by ASUS User on 2016/3/26.
 */
public class CareDialog extends AppBaseWinDialog {
    @Bind(R.id.iv_care_qrcode)
    ImageView mIvQRCode;
    @Bind(R.id.fl_care_reason)
    FrameLayout mFlReason;
    @Bind(R.id.rl_care_root)
    RelativeLayout mRlRoot;

    public CareDialog() {
        super(true);
    }

    @Override
    public int getLayout() {
        return R.layout.layout_care_dialog;
    }

    @Override
    public void init() {
        this.setCanceledOnTouchOutside(true);

        // show binding Qrcode
        showControlQR();

        mFlReason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                BindReasonActivity.show(getContext());
            }
        });

        mRlRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private IFocusOperationPresenter mFocusOperationProxy = new IFocusOperationPresenter() {
        @Override
        public boolean onNavOperation(int operation) {
            if (FocusSupporter.NAV_BTN_BACK == operation || FocusSupporter.NAV_BTN_CLICK == operation) {
                dismiss();
            }

            return true;
        }
    };

    @Override
    public void initFocusViewList() {
        getNavBtnSupporter().setViewList(mFocusOperationProxy);

        getNavBtnSupporter().setCurrentFocus(mFocusOperationProxy);
    }

    /**
     * 显示远程控制二维码
     */
    private void showControlQR() {
        String QRStr = TXZBindStore.get().getBindUrl();
        if (!TextUtils.isEmpty(QRStr)) {
            final Bitmap mQRBitmap;
            try {
                mQRBitmap = QRCodeHandler.createQRCode(QRStr, 350);
                mIvQRCode.setScaleType(ImageView.ScaleType.FIT_XY);
                mIvQRCode.setImageBitmap(mQRBitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
