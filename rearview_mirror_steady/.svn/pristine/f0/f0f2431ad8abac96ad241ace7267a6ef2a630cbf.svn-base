package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.AuthorizationViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IAuthorizationView;
import com.txznet.resholder.R;
import com.txznet.txz.util.QRUtil;

/**
 * 说明：智能家居控制授权
 *
 * @author xiaolin
 * create at 2020-09-07 18:02
 */
public class AuthorizationView extends IAuthorizationView {
    private static AuthorizationView sAuthorizationView = new AuthorizationView();


    private AuthorizationView() {
    }

    public static AuthorizationView getInstance() {
        return sAuthorizationView;
    }

    @Override
    public void init() {
        super.init();
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        AuthorizationViewData authorizationViewData = (AuthorizationViewData) data;
        WinLayout.getInstance().vTips = authorizationViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "getView: authorizationViewData:" + authorizationViewData.vTips);

        View view = createView(authorizationViewData);

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = AuthorizationView.getInstance();
        return adapter;
    }

    private View createView(AuthorizationViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.authorization_view, (ViewGroup) null);

        final ImageView ivQRCode = view.findViewById(R.id.ivQrCode);
        TextView tvTitle = view.findViewById(R.id.tvDesc);
        TextView tvDesc = view.findViewById(R.id.tvTitle);

        tvTitle.setText(viewData.mTips);
        tvDesc.setText(viewData.mSubTitle);
        int h = (int) context.getResources().getDimension(R.dimen.m144);
        try {
            final Bitmap bitmap = QRUtil.createQRCodeBitmapNoWhite(viewData.mUrl, h);
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (bitmap == null) {
                        return;
                    }
                    ivQRCode.setImageBitmap(bitmap);
                }
            }, 0);
        } catch (WriterException e) {
            e.printStackTrace();
        }

        return view;
    }

}
