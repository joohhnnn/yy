package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.BitmapCache;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ImageUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.BindDeviceViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IBindDeviceView;
import com.txznet.resholder.R;
import com.txznet.txz.util.QRUtil;

/**
 * 说明：绑定设备？？？
 *
 * @author xiaolin
 * create at 2020-09-07 19:51
 */
public class BindDeviceView extends IBindDeviceView {
  
    private static BindDeviceView sInstance = new BindDeviceView();
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
    public ExtViewAdapter getView(ViewData data) {
        BindDeviceViewData viewData = (BindDeviceViewData) data;
        LogUtil.logd(WinLayout.logTag + "BindDeviceView.getView(): viewData:" + JSONObject.toJSONString(viewData));

        View view = createViewNone(viewData);

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = BindDeviceView.getInstance();
        adapter.cardHeightType = ExtViewAdapter.SIZE_TYPE.MATCH_PARENT;// 内容占卡片最大
        return adapter;
    }



    private void showBitmap(final BindDeviceViewData viewData, final ImageView mIvQrCode, final ImageView imageView) {

        try {
            if (BitmapCache.getInstance().getBitmap(mQrCodeKey) != null) {
                mQrCodeBitmap = BitmapCache.getInstance().getBitmap(mQrCodeKey);
            } else {
                int size = (int) UIResLoader.getInstance().getModifyContext().getResources().getDimension(R.dimen.m144);
                mQrCodeBitmap = QRUtil.createQRCodeBitmap(viewData.qrCode, size);
                mQrCodeKey = viewData.qrCode + mQrCodeBitmap.getWidth() + mQrCodeBitmap.getHeight();
                BitmapCache.getInstance().putBitmap(mQrCodeKey, mQrCodeBitmap);
            }
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (mBitmap == null || mQrCodeBitmap == null) {
                        return;
                    }
                    mIvQrCode.setImageBitmap(mQrCodeBitmap);
                }
            }, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (BitmapCache.getInstance().getBitmap(mImageKey) != null) {
                mBitmap = BitmapCache.getInstance().getBitmap(mImageKey);
            } else {
                int size = (int) UIResLoader.getInstance().getModifyContext().getResources().getDimension(R.dimen.x234);
                mBitmap = ImageUtils.getBitmap(viewData.imageUrl, size, size/2);
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
                    imageView.setImageBitmap(mBitmap);
                }
            }, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View createViewNone(final BindDeviceViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.bind_device_view, (ViewGroup)null);

        ImageView ivQrCode = view.findViewById(R.id.ivQrCode);
        ImageView ivImage = view.findViewById(R.id.ivImage);

        showBitmap(viewData, ivQrCode, ivImage);
        return view;
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }

    @Override
    public void init() {
        super.init();
    }

}
