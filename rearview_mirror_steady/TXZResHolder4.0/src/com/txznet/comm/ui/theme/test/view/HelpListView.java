package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
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
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.VersionManager;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.utils.ListLayoutUtils;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.HelpListViewData;
import com.txznet.comm.ui.viewfactory.data.HelpListViewData.HelpBean;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IHelpListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.StringUtils;
import com.txznet.resholder.R;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.QRUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * 帮助一级列表
 * <p>
 * 2020-08-17 18:00
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class HelpListView extends IHelpListView {

    private static HelpListView sInstance = new HelpListView();

    private List<View> mItemViews;

    private boolean canOpenDetail;

    private HelpListView() {
    }

    public static HelpListView getInstance() {
        return sInstance;
    }

    @Override
    public void updateProgress(int progress, int selection) {
    }

    @Override
    public void release() {
        super.release();
        if (mItemViews != null) {
            mItemViews.clear();
        }
    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        HelpListViewData helpListViewData = (HelpListViewData) data;
        WinLayout.getInstance().vTips = helpListViewData.mTitleInfo.prefix;

        LogUtil.logd(WinLayout.logTag + "helpListViewData.vTips: " + helpListViewData.vTips + " helpListViewData.canOpenDetail: " + helpListViewData.canOpenDetail);

        View view = createViewNone(helpListViewData);
        // 第二个卡片
        View extView = createExtView(helpListViewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = HelpListView.getInstance();
        viewAdapter.extView = extView;

        return viewAdapter;
    }

    private View createViewNone(HelpListViewData data) {
        canOpenDetail = data.canOpenDetail;// 二级列表

        final Context context = UIResLoader.getInstance().getModifyContext();
        int maxPage = data.mTitleInfo.maxPage;
        int curPage = data.mTitleInfo.curPage;

        ListLayoutUtils.ListContainer listContainer = ListLayoutUtils.createListLayout(context, maxPage, curPage, mViewStateListener);
        final ViewGroup view = listContainer.rootView;
        ViewGroup container = listContainer.container;

        mItemViews = new ArrayList<>();
        for (int i = 0; i < data.count; i++) {
            HelpBean row = data.getData().get(i);

            View itemView = createItemView(context, i, row, i != data.count - 1);

            container.addView(itemView);
            mItemViews.add(itemView);
        }

        // 添加空视图填充空间
        int re = SizeConfig.pageHelpCount - data.count;
        for (int i = 0; i < re; i++) {
            View itemView = createItemView(context, i, null, false);
            container.addView(itemView);
        }

        return view;
    }

    /**
     * 第二个卡片
     *
     * @param viewData
     * @return
     */
    private View createExtView(final HelpListViewData viewData) {
        if (TextUtils.isEmpty(viewData.qrCodeUrl)) {
            return null;
        }
        final Context context = UIResLoader.getInstance().getModifyContext();
        final View view = LayoutInflater.from(context).inflate(R.layout.help_tip_view_qrcode, (ViewGroup) null);
        // 点击第二个卡片不消失
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ImageView ivIcon = view.findViewById(R.id.ivIcon);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        ImageView ivQRCode = view.findViewById(R.id.ivQrCode);
        TextView tvContent = view.findViewById(R.id.tvContent);

        if (!TextUtils.isEmpty(viewData.qrCodeTitleIcon)) {
            ImageLoader.getInstance().displayImage("file://" + viewData.qrCodeTitleIcon, ivIcon);
        } else {
            ivIcon.setImageDrawable(LayouUtil.getDrawable("win_help_play"));
        }

        try {
            Bitmap bitmap = QRUtil.createQRCodeBitmap(viewData.qrCodeUrl, 500);
            ivQRCode.setImageBitmap(bitmap);
            ivQRCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONBuilder jsonBuilder = new JSONBuilder();
                    jsonBuilder.put("title", viewData.qrCodeTitle);
                    jsonBuilder.put("url", viewData.qrCodeUrl);
                    jsonBuilder.put("desc", viewData.qrCodeDesc);
                    ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.help.ui.qrcode",
                            jsonBuilder.toBytes(), null);
                }
            });
        } catch (WriterException e) {
            e.printStackTrace();
        }

        tvTitle.setText(viewData.qrCodeTitle);
        tvContent.setText(viewData.qrCodeDesc);

        return view;
    }

    @Override
    public void init() {
        super.init();

    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }

    @Override
    public void snapPage(boolean next) {
        LogUtil.logd("update snap " + next);
    }

    @Override
    public List<View> getFocusViews() {
        return mItemViews;
    }

    /**
     * 是否含有动画
     *
     * @return
     */
    @Override
    public boolean hasViewAnimation() {
        return true;
    }

    private View createItemView(Context context, int index, HelpBean row, boolean showDivider) {
        View view = LayoutInflater.from(context).inflate(R.layout.help_tip_view_item, (ViewGroup) null);
        if (row == null) {
            view.setVisibility(View.INVISIBLE);
            return view;
        }

        if (canOpenDetail) {
            // 设置列表点击
            ListLayoutUtils.setItemViewOnClickOnTouch(view, mItemViews, index);
        }

        ImageView ivIcon = view.findViewById(R.id.ivIcon);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        TextView tvDesc = view.findViewById(R.id.tvDesc);
        View divider = view.findViewById(R.id.divider);

        if (VersionManager.getInstance().isUseHelpNewTag()) {
            if (row.isFromFile) {
                if (new File(row.iconName).exists()) {
                    ImageLoader.getInstance().displayImage("file://" + row.iconName, new ImageViewAware(ivIcon));
                }
            } else {
                ivIcon.setImageDrawable(LayouUtil.getDrawable(row.iconName));
            }
        } else {
            ivIcon.setImageDrawable(LayouUtil.getDrawable(row.iconName));
        }

        tvTitle.setText(StringUtils.isEmpty(row.title) ? "" : LanguageConvertor.toLocale(row.title));
        tvDesc.setText(Html.fromHtml(LanguageConvertor.toLocale(row.intro)));
        divider.setVisibility(showDivider ? View.VISIBLE : View.INVISIBLE);

        return view;
    }

    @Override
    public void updateItemSelect(int index) {
        LogUtil.logd(WinLayout.logTag + "helpList updateItemSelect " + index);
        showSelectItem(index);
    }

    private void showSelectItem(int index) {
        for (int i = 0; i < mItemViews.size(); i++) {
            if (i == index) {
                mItemViews.get(i).setBackground(LayouUtil.getDrawable("item_setlected"));
            } else {
                mItemViews.get(i).setBackground(null);
            }
        }
    }

}
