package com.txznet.comm.ui.theme.test.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.BitmapCache;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.FeedbackViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFeedbackView;
import com.txznet.txz.util.QRUtil;

public class FeedbackView extends IFeedbackView {

    private static FeedbackView sInstance = new FeedbackView();
    private int contentHeight;
    private int unit;

    private FeedbackView() {
    }

    public static FeedbackView getInstance() {
        return sInstance;
    }

    ImageView mIvQrCode;
    private String mQrCodeKey;
    private Bitmap mQrCodeBitmap;

    @Override
    public ViewAdapter getView(ViewData data) {
        final FeedbackViewData feedbackViewData = (FeedbackViewData) data;
        WinLayout.getInstance().vTips = feedbackViewData.vTips;
        View view = null;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                view = createViewFull(feedbackViewData);
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                view = createViewHalf(feedbackViewData);
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                view = createViewNone(feedbackViewData);
                break;
            default:
                break;
        }

        ViewAdapter adapter = new ViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(feedbackViewData.getType());
        adapter.object = FeedbackView.getInstance();
        return adapter;
    }

    private View createViewNone(FeedbackViewData viewData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.setBackground(LayouUtil.getDrawable("constellation_background"));

        LinearLayout titleView = new LinearLayout(GlobalContext.get());
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleView.setLayoutParams(titleViewLayoutParams);
        ImageView starImageView = new ImageView(GlobalContext.get());
        starImageView.setImageDrawable(LayouUtil.getDrawable("icon_feedback"));
        starImageView.setLayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m40"), (int) LayouUtil.getDimen("m40")));
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setText("反馈");
        tvTitle.setTextSize(LayouUtil.getDimen("m23"));
        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
        tvTitle.setGravity(Gravity.CENTER);
        titleView.addView(starImageView);
        titleView.addView(tvTitle);


        RelativeLayout contentView = new RelativeLayout(GlobalContext.get());


        RelativeLayout.LayoutParams contentViewLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        contentView.setLayoutParams(contentViewLayoutParam);


        LinearLayout llCenter = new LinearLayout(GlobalContext.get());
        llCenter.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams llCenterLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llCenterLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        llCenter.setOrientation(LinearLayout.HORIZONTAL);
        llCenter.setLayoutParams(llCenterLayoutParams);

        mIvQrCode = new ImageView(GlobalContext.get());

        final int h = (int) LayouUtil.getDimen("m160");
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
                    mIvQrCode.setImageBitmap(mQrCodeBitmap);
                }
            }, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TextView tvTips = new TextView(GlobalContext.get());
        LinearLayout.LayoutParams tvTipsLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvTipsLayoutParams.leftMargin = (int) LayouUtil.getDimen("y8");
        tvTips.setTextColor(Color.WHITE);
        tvTips.setGravity(Gravity.CENTER);
        tvTips.setLayoutParams(tvTipsLayoutParams);

        if (!TextUtils.isEmpty(viewData.qrCode)) {
            tvTips.setText(viewData.tips);
        }

        llCenter.addView(mIvQrCode);
        llCenter.addView(tvTips);
        contentView.addView(llCenter);
        root.addView(titleView);
        root.addView(contentView);
        return root;
    }

    private View createViewHalf(FeedbackViewData viewData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout titleView = new LinearLayout(GlobalContext.get());
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleViewLayoutParams.topMargin = (int) LayouUtil.getDimen("y32");
        titleView.setLayoutParams(titleViewLayoutParams);
        ImageView starImageView = new ImageView(GlobalContext.get());
        starImageView.setImageDrawable(LayouUtil.getDrawable("icon_feedback"));
        starImageView.setLayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m40"), (int) LayouUtil.getDimen("m40")));
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setText("反馈");
        tvTitle.setTextSize(LayouUtil.getDimen("m23"));
        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
        tvTitle.setGravity(Gravity.CENTER);
        titleView.addView(starImageView);
        titleView.addView(tvTitle);


        RelativeLayout contentView = new RelativeLayout(GlobalContext.get());
        contentView.setBackground(LayouUtil.getDrawable("constellation_background"));

        RelativeLayout.LayoutParams contentViewLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, contentHeight);
        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");

        contentView.setLayoutParams(contentViewLayoutParam);


        LinearLayout llCenter = new LinearLayout(GlobalContext.get());
        llCenter.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams llCenterLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llCenterLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        llCenter.setOrientation(LinearLayout.HORIZONTAL);
        llCenter.setLayoutParams(llCenterLayoutParams);

        mIvQrCode = new ImageView(GlobalContext.get());

        final int h = (int) LayouUtil.getDimen("m160");
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
                    mIvQrCode.setImageBitmap(mQrCodeBitmap);
                }
            }, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TextView tvTips = new TextView(GlobalContext.get());
        LinearLayout.LayoutParams tvTipsLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvTipsLayoutParams.leftMargin = (int) LayouUtil.getDimen("y8");
        tvTips.setTextColor(Color.WHITE);
        tvTips.setGravity(Gravity.CENTER);
        tvTips.setLayoutParams(tvTipsLayoutParams);

        if (!TextUtils.isEmpty(viewData.qrCode)) {
            tvTips.setText(viewData.tips);
        }

        llCenter.addView(mIvQrCode);
        llCenter.addView(tvTips);
        contentView.addView(llCenter);
        root.addView(titleView);
        root.addView(contentView);
        return root;
    }

    private View createViewFull(FeedbackViewData viewData) {
        LinearLayout root = new LinearLayout(GlobalContext.get());
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, ((int) LayouUtil.getDimen("y480"))));

        LinearLayout titleView = new LinearLayout(GlobalContext.get());
        titleView.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams titleViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleViewLayoutParams.topMargin = (int) LayouUtil.getDimen("y32");
        titleView.setLayoutParams(titleViewLayoutParams);
        ImageView starImageView = new ImageView(GlobalContext.get());
        starImageView.setImageDrawable(LayouUtil.getDrawable("icon_feedback"));
        starImageView.setLayoutParams(new ViewGroup.LayoutParams((int) LayouUtil.getDimen("m40"), (int) LayouUtil.getDimen("m40")));
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setText("反馈");
        tvTitle.setTextSize(LayouUtil.getDimen("m23"));
        tvTitle.setTextColor(Color.parseColor("#FF89898A"));
        tvTitle.setGravity(Gravity.CENTER);
        titleView.addView(starImageView);
        titleView.addView(tvTitle);


        RelativeLayout contentView = new RelativeLayout(GlobalContext.get());
        contentView.setBackground(LayouUtil.getDrawable("constellation_background"));

        RelativeLayout.LayoutParams contentViewLayoutParam = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y384"));
        contentViewLayoutParam.topMargin = (int) LayouUtil.getDimen("y8");

        contentView.setLayoutParams(contentViewLayoutParam);


        LinearLayout llCenter = new LinearLayout(GlobalContext.get());
        llCenter.setGravity(Gravity.CENTER_HORIZONTAL);
        RelativeLayout.LayoutParams llCenterLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llCenterLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        llCenter.setOrientation(LinearLayout.VERTICAL);
        llCenter.setLayoutParams(llCenterLayoutParams);

        mIvQrCode = new ImageView(GlobalContext.get());

        final int h = (int) LayouUtil.getDimen("m160");
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
                    mIvQrCode.setImageBitmap(mQrCodeBitmap);
                }
            }, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        TextView tvTips = new TextView(GlobalContext.get());
        LinearLayout.LayoutParams tvTipsLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvTipsLayoutParams.topMargin = (int) LayouUtil.getDimen("y8");
        tvTips.setTextColor(Color.WHITE);
        tvTips.setGravity(Gravity.CENTER);
        tvTips.setLayoutParams(tvTipsLayoutParams);

        if (!TextUtils.isEmpty(viewData.qrCode)) {
            tvTips.setText(viewData.tips);
        }

        llCenter.addView(mIvQrCode);
        llCenter.addView(tvTips);
        contentView.addView(llCenter);
        root.addView(titleView);
        root.addView(contentView);
        return root;
    }

    @Override
    public void init() {
        super.init();
    }

    public void onUpdateParams(int styleIndex) {
        switch (styleIndex) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                initFull();
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                initHalf();
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                initNone();
                break;
            default:
                break;
        }
    }

    private void initNone() {

    }

    private void initHalf() {
        if (WinLayout.isVertScreen) {
            unit = (int) LayouUtil.getDimen("vertical_unit");
            contentHeight = (int) (33.3 * unit);
        } else {
            unit = (int) LayouUtil.getDimen("unit");
            contentHeight = SizeConfig.pagePoiCount * SizeConfig.itemHeight;
        }
    }

    private void initFull() {
    }
}
