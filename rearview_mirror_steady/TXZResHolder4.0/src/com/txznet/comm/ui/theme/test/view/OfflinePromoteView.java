package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.viewfactory.data.OfflinePromoteViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IOfflinePromoteView;
import com.txznet.resholder.R;
import com.txznet.txz.util.QRUtil;

/**
 * 文本+LOGO的离线促活页面
 * <p>
 * 2020-09-10 18:26
 *
 * @author xiaolin
 */
public class OfflinePromoteView extends IOfflinePromoteView {
    private static OfflinePromoteView sInstance = new OfflinePromoteView();

    public static OfflinePromoteView getInstance() {
        return sInstance;
    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        OfflinePromoteViewData offlineData = (OfflinePromoteViewData) data;

        View view = createView(offlineData);

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = offlineData.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = OfflinePromoteView.getInstance();
        adapter.cardHeightType = ExtViewAdapter.SIZE_TYPE.MATCH_PARENT;
        return adapter;
    }

    public View createView(OfflinePromoteViewData viewData){
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.offline_promote_view, (ViewGroup)null);
        ImageView ivQrCode = view.findViewById(R.id.ivQrCode);
        TextView tvContent = view.findViewById(R.id.tvContent);

        tvContent.setText(Html.fromHtml(viewData.text));


        int logoWidth = (int) context.getResources().getDimension(R.dimen.m23);
        int width = (int) context.getResources().getDimension(R.dimen.m120);
        Bitmap logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        Bitmap bitmap = QRUtil.createQRCodeWithLogo(viewData.qrCode,width,0,logoBitmap,logoWidth);
        ivQrCode.setImageBitmap(bitmap);

        LinearLayout layout = new LinearLayout(GlobalContext.get());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        return view;
    }

    @Override
    public void init() {
        super.init();
    }
}
