package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.ui.BitmapCache;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.DimenUtils;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.FeedbackViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFeedbackView;
import com.txznet.resholder.R;
import com.txznet.txz.util.QRUtil;

/**
 * 反馈界面在core中
 *
 * 反馈完毕后展示二维码的界面
 *
 * @author xiaolin
 * 2020-08-25 17:56
 */
public class FeedbackView extends IFeedbackView {

    private static FeedbackView sInstance = new FeedbackView();

    private FeedbackView() {
    }

    public static FeedbackView getInstance() {
        return sInstance;
    }

    private String mQrCodeKey;
    private Bitmap mQrCodeBitmap;

    @Override
    public ExtViewAdapter getView(ViewData data) {
        final FeedbackViewData feedbackViewData = (FeedbackViewData) data;
        WinLayout.getInstance().vTips = feedbackViewData.vTips;

        View view = createViewNone(feedbackViewData);


        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = FeedbackView.getInstance();
        adapter.cardHeightType = ExtViewAdapter.SIZE_TYPE.MATCH_PARENT;// 内容占卡片最大
        return adapter;
    }

    private View createViewNone(FeedbackViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();

        View view = LayoutInflater.from(context).inflate(R.layout.feedback_view, (ViewGroup)null);
        final ImageView ivQRCode = view.findViewById(R.id.ivQrCode);
        TextView tvContent = view.findViewById(R.id.tvContent);

        tvContent.setText(viewData.tips);

        final int h = (int) DimenUtils.dp2px(context, 160F);
        try {
            if (BitmapCache.getInstance().getBitmap(mQrCodeKey) != null) {
                mQrCodeBitmap = BitmapCache.getInstance().getBitmap(mQrCodeKey);
            } else {
                mQrCodeBitmap = QRUtil.createQRCodeBitmap(viewData.qrCode, h);
                mQrCodeKey = viewData.qrCode + mQrCodeBitmap.getWidth() + mQrCodeBitmap.getHeight();
                BitmapCache.getInstance().putBitmap(mQrCodeKey, mQrCodeBitmap);
            }

            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (mQrCodeBitmap == null) {
                        return;
                    }
                    ivQRCode.setImageBitmap(mQrCodeBitmap);
                }
            }, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void init() {
        super.init();
    }

    public void onUpdateParams(int styleIndex) {

    }

}
