package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.BitmapCache;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.util.ImageUtils;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.BindDeviceViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IBindDeviceView;
import com.txznet.txz.util.QRUtil;

import java.io.FileInputStream;

public class BindDeviceView extends IBindDeviceView {
  
    private static BindDeviceView sInstance = new BindDeviceView();
    private int mQrCodeWidth;    //二维码宽度
    private int mInterval;    //二维码和图片间隔
    private String mQrCodeKey;
    private String mImageKey;
    private Bitmap mQrCodeBitmap;
    private Bitmap mBitmap;

    private BindDeviceView() {
    }

    public static BindDeviceView getInstance() {
        return sInstance;
    }


    @Override
    public ViewAdapter getView(ViewData data) {
        BindDeviceViewData bindDeviceViewData = (BindDeviceViewData) data;
        View view = null;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                view = createViewFull(bindDeviceViewData);
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                view = createViewHalf(bindDeviceViewData);
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                view = createViewNone(bindDeviceViewData);
            default:
                break;
        }
        ViewAdapter adapter = new ViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(data.getType());
        adapter.object = BindDeviceView.getInstance();
        return adapter;
    }


    private View createViewFull(final BindDeviceViewData viewData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        AbsListView.LayoutParams rootLayoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
        root.setLayoutParams(rootLayoutParams);
        final ImageView mIvQrCode = new ImageView(GlobalContext.get());
        mIvQrCode.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));

        final ImageView imageView = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) LayouUtil.getDimen("m10");
        imageView.setLayoutParams(layoutParams);
        root.addView(mIvQrCode);
        root.addView(imageView);
        showBitmap(viewData, mIvQrCode, imageView);
        return root;
    }

    private void showBitmap(final BindDeviceViewData viewData, final ImageView mIvQrCode, final ImageView imageView) {
//        final int h = (int) LayouUtil.getDimen(mQrCodeWidth);
        final int h = mQrCodeWidth;
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
                FileInputStream fis = null;
//                mBitmap = Bitmap.createScaledBitmap(ImageUtils.compressBySampleSize(bitmap, h * 2, h));
                mBitmap = Bitmap.createScaledBitmap(ImageUtils.getBitmap(viewData.imageUrl, h, h*2), h * 2, h, true);
                mImageKey = GlobalContext.get().getSharedPreferences("txz", Context.MODE_PRIVATE).getString("keyBindDeviceImageCRC32", "") + mBitmap.getWidth() + mBitmap.getHeight();
                LogUtil.d("skyward " + mImageKey);
                BitmapCache.getInstance().putBitmap(mImageKey, mBitmap);
            }
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (mBitmap == null || mQrCodeBitmap == null) {
                        return;
                    }
                    mIvQrCode.setImageBitmap(mQrCodeBitmap);
                    imageView.setImageBitmap(mBitmap);
                }
            }, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View createViewHalf(final BindDeviceViewData viewData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        AbsListView.LayoutParams rootLayoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
        root.setGravity(Gravity.CENTER);
        root.setLayoutParams(rootLayoutParams);
        final ImageView mIvQrCode = new ImageView(GlobalContext.get());
        mIvQrCode.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
        mIvQrCode.setScaleType(ScaleType.CENTER);

        final ImageView imageView = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) LayouUtil.getDimen("m10");
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ScaleType.CENTER_CROP);
        root.addView(mIvQrCode);
        root.addView(imageView);
        showBitmap(viewData, mIvQrCode, imageView);
        return root;
    }

    private View createViewNone(final BindDeviceViewData viewData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        AbsListView.LayoutParams rootLayoutParams = new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
        root.setGravity(Gravity.CENTER);
        root.setLayoutParams(rootLayoutParams);
        final ImageView mIvQrCode = new ImageView(GlobalContext.get());
        mIvQrCode.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT));
        mIvQrCode.setScaleType(ScaleType.CENTER);

        final ImageView imageView = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
        layoutParams.leftMargin = (int) LayouUtil.getDimen("m10");
        imageView.setLayoutParams(layoutParams);
        root.addView(mIvQrCode);
        root.addView(imageView);
        showBitmap(viewData, mIvQrCode, imageView);
        return root;
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        switch (styleIndex) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                mQrCodeWidth = ViewParamsUtil.unit * 20;
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                mQrCodeWidth = ViewParamsUtil.unit * 20;
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                mQrCodeWidth = ViewParamsUtil.unit * 20;
                break;
            default:
                break;
        }
    }

    @Override
    public void init() {
        super.init();

        mQrCodeWidth = ViewParamsUtil.unit * 20;
        mInterval = ViewParamsUtil.unit;
    }

}
