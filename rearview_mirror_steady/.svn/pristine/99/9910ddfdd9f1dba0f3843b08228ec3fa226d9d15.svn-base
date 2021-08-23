package com.txznet.comm.ui.viewfactory.view.defaults;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.BitmapCache;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.util.ImageUtils;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.BindDeviceViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IBindDeviceView;
import com.txznet.txz.util.QRUtil;

public class BindDeviceView extends IBindDeviceView {

    private static BindDeviceView sInstance = new BindDeviceView();

    private BindDeviceView() {
    }

    public static BindDeviceView getInstance() {
        return sInstance;
    }

    ImageView mIvQrCode;
    LinearLayout mRoot;
    ImageView imageView;
    private String mQrCodeKey;
    private String mImageKey;
    private Bitmap mQrCodeBitmap;
    private Bitmap mBitmap;

    @Override
    public ViewAdapter getView(ViewData data) {
        final BindDeviceViewData viewData = (BindDeviceViewData) data;
        mRoot = new LinearLayout(GlobalContext.get());
        mRoot.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        mIvQrCode = new ImageView(GlobalContext.get());
        mIvQrCode.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));

        imageView = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) LayouUtil.getDimen("m10");
        imageView.setLayoutParams(layoutParams);
        mRoot.addView(mIvQrCode);
        mRoot.addView(imageView);
        final int h = (int) LayouUtil.getDimen("m160");
        try {

            if (BitmapCache.getInstance().getBitmap(mQrCodeKey) != null) {
                mQrCodeBitmap = BitmapCache.getInstance().getBitmap(mQrCodeKey);
            } else {
                mQrCodeBitmap = QRUtil.createQRCodeBitmap(viewData.qrCode, h);
                mQrCodeKey = viewData.qrCode + mQrCodeBitmap.getWidth() + mQrCodeBitmap.getHeight();
                BitmapCache.getInstance().putBitmap(mQrCodeKey, mQrCodeBitmap);
            }
            if (BitmapCache.getInstance().getBitmap(mImageKey) != null) {
                mBitmap = BitmapCache.getInstance().getBitmap(mImageKey);
            } else {
                mBitmap = Bitmap.createScaledBitmap(ImageUtils.getBitmap(viewData.imageUrl, h * 2, h), h * 2, h, true);
                mImageKey = GlobalContext.get().getSharedPreferences("txz", Context.MODE_PRIVATE).getString("keyBindDeviceImageCRC32", "") + mBitmap.getWidth() + mBitmap.getHeight();
                BitmapCache.getInstance().putBitmap(mImageKey, mBitmap);
            }
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (mQrCodeBitmap == null || mBitmap == null) {
                        return;
                    }
                    mIvQrCode.setImageBitmap(mQrCodeBitmap);
                    imageView.setImageBitmap(mBitmap);
                }
            }, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ViewAdapter adapter = new ViewAdapter();
        adapter.type = data.getType();
        adapter.view = mRoot;
        adapter.object = BindDeviceView.getInstance();
        return adapter;
    }

    @Override
    public void init() {
        super.init();
    }

}
