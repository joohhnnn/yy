package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.VersionManager;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.data.HelpDetailImageViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IHelpDetailImageView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.QRUtil;

/**
 * 帮助, 显示二维码
 * <p>
 * 2020-08-17 18:00
 *
 * @author xiaolin
 */
public class HelpDetailImageView extends IHelpDetailImageView {
    private static HelpDetailImageView instance = new HelpDetailImageView();

    public static HelpDetailImageView getInstance() {
        return instance;
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
        HelpDetailImageViewData helpDetailImageViewData = (HelpDetailImageViewData) data;
        WinLayout.getInstance().vTips = helpDetailImageViewData.vTips;
        LogUtil.logd(WinLayout.logTag + "helpDetailImageViewData.vTips:" + helpDetailImageViewData.vTips + StyleConfig.getInstance().getSelectStyleIndex());

        View view = createViewNone(helpDetailImageViewData);

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = HelpDetailImageView.getInstance();
        adapter.cardHeightType = ExtViewAdapter.SIZE_TYPE.MATCH_PARENT;// 内容占卡片最大
        return adapter;
    }

    View.OnClickListener backListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (VersionManager.getInstance().isUseHelpNewTag()) {
                com.txznet.comm.remote.util.ConfigUtil.setShowHelpNewTag(false);
            }
            RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                    TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_HELP_BACK, 0, 0);
        }
    };

    private View createViewNone(final HelpDetailImageViewData helpDetailImageViewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        ViewGroup view = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.help_tip_view_detail_imageview, (ViewGroup) null);

        ImageView ivBack = view.findViewById(R.id.ivBack);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        ImageView ivImageView = view.findViewById(R.id.ivImage);
        TextView tvDesc = view.findViewById(R.id.tvDesc);

        ivBack.setOnClickListener(backListener);
        tvTitle.setText(LanguageConvertor.toLocale(helpDetailImageViewData.getHelpTitle()));

        final HelpDetailImageViewData.HelpDetailBean helpDetailImg = helpDetailImageViewData.getData().get(0);
        if (TextUtils.isEmpty(helpDetailImg.img)) {
            ivImageView.setVisibility(View.GONE);
        } else {
			ivImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONBuilder data = new JSONBuilder();
                    data.put("title", helpDetailImageViewData.getHelpTitle());
                    data.put("url", helpDetailImg.img);
                    data.put("desc", helpDetailImg.text);
                    data.put("isFromFile", helpDetailImageViewData.isFromFile);
                    data.put("from", "detail");
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.detail.qrcode", data.toBytes(), null);
                }
            });
            if (helpDetailImg.img.startsWith("qrcode:")) {
                try {
					ivImageView.setImageBitmap(QRUtil.createQRCodeBitmap(helpDetailImg.img.replace("qrcode:", ""), (int) context.getResources().getDimension(R.dimen.m200)));
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            } else if (helpDetailImageViewData.isFromFile) {
                ImageLoader.getInstance().displayImage("file://" + helpDetailImg.img, new ImageViewAware(ivImageView));
            } else {
				ivImageView.setImageDrawable(LayouUtil.getDrawable(helpDetailImg.img));
            }
        }

        if (!TextUtils.isEmpty(helpDetailImg.text)) {
            helpDetailImg.text = helpDetailImg.text.replace("\n\n", "\n");
            tvDesc.setText(LanguageConvertor.toLocale(helpDetailImg.text));
        }

        return view;
    }

    @Override
    public void updateProgress(int progress, int selection) {

    }

    @Override
    public void snapPage(boolean next) {

    }

    @Override
    public void updateItemSelect(int arg0) {

    }

}
