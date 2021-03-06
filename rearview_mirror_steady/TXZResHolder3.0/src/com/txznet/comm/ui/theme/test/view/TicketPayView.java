package com.txznet.comm.ui.theme.test.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.WriterException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.QiwuTrainTicketPayViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ITicketPayView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.QRUtil;

import org.json.JSONException;
import org.json.JSONObject;

import static com.txznet.comm.ui.util.LayouUtil.getDimen;
import static com.txznet.comm.ui.util.LayouUtil.getDrawable;

/**
 * Created by daviddai on 2019/9/18
 */
public class TicketPayView extends ITicketPayView {

    private static TicketPayView sInstance = new TicketPayView();

    public static TicketPayView getInstance() {
        return sInstance;
    }

    private static final String TAG = "TicketPayView::";

    private String mCancelText = "????????????";

    QiwuTrainTicketPayViewData.TicketPayBean ticketPayBean;

    private float topHeight;//????????????????????????
    private float bottomHeight;//????????????????????????
    private int contentLeftMargin;//???????????????
    private int iconSide;//????????????????????????
    private int iconHorMargin;//????????????????????????
    private int tvFormTypeSize;//????????????????????????
    private int tvFormTypeHeight;//????????????????????????
    private int tvFormTypeColor;//????????????????????????
    private int tvPayTimeSize;//??????????????????????????????
    private int tvPayTimeHeight;//??????????????????????????????
    private int tvPayTimeColor;//??????????????????????????????
    private int btnCancelWidth;//??????????????????
    private int btnCancelHeight;//??????????????????
    private int btnCancelColor;//??????????????????
    private int tvCancelSize;//????????????????????????
    private int tvCancelColor;//????????????????????????
    private int tvContenkeySize;//????????????key????????????
    private int tvContenkeyHeight;//????????????key????????????
    private int tvContenkeyColor;//????????????key????????????
    private int centerInterval;//????????????????????????
    private int centerIntervalH;//????????????????????????
    private int tvContenValueSize;//????????????Value????????????
    private int tvContenValueHeight;//????????????Value????????????
    private int tvContenValueColor;//????????????Value????????????
    private int tvPriceSize;//??????????????????
    private int tvPriceColor;//??????????????????
    private int qrCodeSide;//???????????????
    private int qrCodeLogoSide;//???????????????logo??????
    private int qrCodeLogoMarginTop;//??????????????????
    private int qrCodeLogoMarginBottom;//??????????????????
    private int btnSwitchHeight;//???????????????????????????

    private int dividerHeight;//???????????????

    @Override
    public void init() {
        super.init();
        dividerHeight = 1;
        tvFormTypeColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvPayTimeColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        btnCancelColor = Color.parseColor("#4D5668");
        tvCancelColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvContenkeyColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        tvContenValueColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        tvPriceColor = Color.parseColor(LayouUtil.getString("color_flight_price"));

    }

    //??????????????????????????????
    public void onUpdateParams(int styleIndex) {
        LogUtil.logd(WinLayout.logTag + "TrainTicket onUpdateParams: " + styleIndex);

        // XXX: 2019/9/9 ????????????????????????????????????????????????
        // ??????????????????UI?????????????????????core???????????????????????????????????????????????????????????????
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

    //??????????????????
    private void initFull() {
        int unit = ViewParamsUtil.unit;
        if (WinLayout.isVertScreen){
            topHeight = 1f;//????????????????????????
            bottomHeight = 5.1f;//????????????????????????
            contentLeftMargin = unit;
            iconSide = 6 * unit;//????????????????????????
            iconHorMargin = 2 * unit;//????????????????????????
            tvFormTypeSize = ViewParamsUtil.h3;//????????????????????????
            tvFormTypeHeight = ViewParamsUtil.h3Height;//????????????????????????
            tvPayTimeSize = ViewParamsUtil.h6;//??????????????????????????????
            tvPayTimeHeight = ViewParamsUtil.h6Height;//??????????????????????????????
            btnCancelWidth = 14 * unit;//??????????????????
            btnCancelHeight = 4 * unit;//??????????????????
            tvCancelSize = ViewParamsUtil.h7;//????????????????????????
            tvContenkeySize = ViewParamsUtil.h5;//????????????key????????????
            tvContenkeyHeight = ViewParamsUtil.h5Height;//????????????key????????????
            centerInterval = 3 * unit / 2;
            centerIntervalH = 10 * unit;
            tvContenValueSize = ViewParamsUtil.h5;//????????????Value????????????
            tvContenValueHeight = ViewParamsUtil.h5Height;//????????????Value????????????
            tvPriceSize = ViewParamsUtil.h7;//???????????????
            qrCodeSide = 20 * unit;//???????????????
            qrCodeLogoSide = 4 * unit;//???????????????logo??????
            qrCodeLogoMarginTop = unit;
            qrCodeLogoMarginBottom = 0;
            btnSwitchHeight = 4 * unit;//???????????????????????????
        }else {
            topHeight = 1f;//????????????????????????
            bottomHeight = 3f;//????????????????????????
            contentLeftMargin = 2 * unit;
            iconSide = 6 * unit;//????????????????????????
            iconHorMargin = 2 * unit;//????????????????????????
            tvFormTypeSize = ViewParamsUtil.h3;//????????????????????????
            tvFormTypeHeight = ViewParamsUtil.h3Height;//????????????????????????
            tvPayTimeSize = ViewParamsUtil.h6;//??????????????????????????????
            tvPayTimeHeight = ViewParamsUtil.h6Height;//??????????????????????????????
            btnCancelWidth = 14 * unit;//??????????????????
            btnCancelHeight = 4 * unit;//??????????????????
            tvCancelSize = ViewParamsUtil.h7;//????????????????????????
            tvContenkeySize = ViewParamsUtil.h5;//????????????key????????????
            tvContenkeyHeight = ViewParamsUtil.h5Height;//????????????key????????????
            centerInterval = 3 * unit;
            centerIntervalH = 2 * unit;
            tvContenValueSize = ViewParamsUtil.h5;//????????????Value????????????
            tvContenValueHeight = ViewParamsUtil.h5Height;//????????????Value????????????
            tvPriceSize = ViewParamsUtil.h7;//??????????????????
            btnSwitchHeight = 4 * unit;//???????????????????????????
            if (SizeConfig.screenHeight < 480){
                qrCodeSide = SizeConfig.screenHeight / 3;
                qrCodeLogoSide = qrCodeSide / 5;
                qrCodeLogoMarginTop = unit/2;
                qrCodeLogoMarginBottom = unit/2;
            }else {
                qrCodeSide = 20 * unit;//???????????????
                qrCodeLogoSide = 4 * unit;//???????????????logo??????
                qrCodeLogoMarginTop = unit;
                qrCodeLogoMarginBottom = 2 * unit;
            }
        }
    }

    //??????????????????
    private void initHalf(){
        int unit = ViewParamsUtil.unit;
        if (WinLayout.isVertScreen){
            topHeight = 1f;//????????????????????????
            bottomHeight = 5.1f;//????????????????????????
            contentLeftMargin = unit;
            iconSide = 6 * unit;//????????????????????????
            iconHorMargin = 2 * unit;//????????????????????????
            tvFormTypeSize = ViewParamsUtil.h3;//????????????????????????
            tvFormTypeHeight = ViewParamsUtil.h3Height;//????????????????????????
            tvPayTimeSize = ViewParamsUtil.h6;//??????????????????????????????
            tvPayTimeHeight = ViewParamsUtil.h6Height;//??????????????????????????????
            btnCancelWidth = 14 * unit;//??????????????????
            btnCancelHeight = 4 * unit;//??????????????????
            tvCancelSize = ViewParamsUtil.h7;//????????????????????????
            tvContenkeySize = ViewParamsUtil.h5;//????????????key????????????
            tvContenkeyHeight = ViewParamsUtil.h5Height;//????????????key????????????
            centerInterval = 3 * unit / 2;
            centerIntervalH = 10 * unit;
            tvContenValueSize = ViewParamsUtil.h5;//????????????Value????????????
            tvContenValueHeight = ViewParamsUtil.h5Height;//????????????Value????????????
            tvPriceSize = ViewParamsUtil.h6;//??????????????????
            qrCodeSide = 20 * unit;//???????????????
            qrCodeLogoSide = 4 * unit;//???????????????logo??????
            qrCodeLogoMarginTop = unit;
            qrCodeLogoMarginBottom = 0;
            btnSwitchHeight = 4 * unit;//???????????????????????????
        }else {
            topHeight = 1f;//????????????????????????
            bottomHeight = 3f;//????????????????????????
            contentLeftMargin = 2 * unit;
            iconSide = 6 * unit;//????????????????????????
            iconHorMargin = 4 * unit;//????????????????????????
            tvFormTypeSize = ViewParamsUtil.h3;//????????????????????????
            tvFormTypeHeight = ViewParamsUtil.h3Height;//????????????????????????
            tvPayTimeSize = ViewParamsUtil.h6;//??????????????????????????????
            tvPayTimeHeight = ViewParamsUtil.h6Height;//??????????????????????????????
            btnCancelWidth = 14 * unit;//??????????????????
            btnCancelHeight = 4 * unit;//??????????????????
            tvCancelSize = ViewParamsUtil.h7;//????????????????????????
            tvContenkeySize = ViewParamsUtil.h5;//????????????key????????????
            tvContenkeyHeight = ViewParamsUtil.h5Height;//????????????key????????????
            centerIntervalH = 2 * unit;
            tvContenValueSize = ViewParamsUtil.h5;//????????????Value????????????
            tvContenValueHeight = ViewParamsUtil.h5Height;//????????????Value????????????
            tvPriceSize = ViewParamsUtil.h7;//??????????????????
            btnSwitchHeight = (int)(3.6 * unit);//???????????????????????????
            if (SizeConfig.screenHeight < 480){
                centerInterval = unit;
                qrCodeSide = SizeConfig.screenHeight / 4;
                qrCodeLogoSide = qrCodeSide / 5;
                qrCodeLogoMarginTop = unit/2;
                qrCodeLogoMarginBottom = unit/2;
            }else {
                centerInterval = 3 * unit;
                qrCodeSide = 18 * unit;//???????????????
                qrCodeLogoSide = (int)(3.6 * unit);//???????????????logo??????
                qrCodeLogoMarginTop = unit/2;
                qrCodeLogoMarginBottom = unit/2;
            }
        }
    }

    //??????????????????
    private void initNone(){
        int unit = ViewParamsUtil.unit;
        contentLeftMargin = 2 * unit;
        topHeight = 4f;//????????????????????????
        bottomHeight = 11f;//????????????????????????
        iconSide = 5 * unit;//????????????????????????
        iconHorMargin = unit;//????????????????????????
        tvFormTypeSize = ViewParamsUtil.h3;//????????????????????????
        tvFormTypeHeight = ViewParamsUtil.h3Height;//????????????????????????
        tvPayTimeSize = ViewParamsUtil.h7;//??????????????????????????????
        tvPayTimeHeight = ViewParamsUtil.h7Height;//??????????????????????????????
        btnCancelWidth = (int)(12.6 * unit);//??????????????????
        btnCancelHeight = (int)(3.6 * unit);//??????????????????
        tvCancelSize = ViewParamsUtil.h7;//????????????????????????
        tvContenkeySize = ViewParamsUtil.h5;//????????????key????????????
        tvContenkeyHeight = ViewParamsUtil.h5Height;//????????????key????????????
        centerInterval = 2 * unit;
        centerIntervalH = unit / 2;
        tvContenValueSize = ViewParamsUtil.h5;//????????????Value????????????
        tvContenValueHeight = ViewParamsUtil.h5Height;//????????????Value????????????
        tvPriceSize = ViewParamsUtil.h7;//??????????????????
        qrCodeLogoMarginTop = 0;
        btnSwitchHeight = (int)(2.8 * unit);//???????????????????????????
        if (SizeConfig.screenHeight < 480){
            qrCodeSide = SizeConfig.screenHeight / 4;
            qrCodeLogoSide = qrCodeSide / 5;
            qrCodeLogoMarginBottom = 0;
        }else {
            qrCodeSide = 14 * unit;//???????????????
            qrCodeLogoSide = (int)(2.8 * unit);//???????????????logo??????
            qrCodeLogoMarginBottom = unit/5;
        }
    }

    @Override
    public ViewFactory.ViewAdapter getView(final ViewData data) {
        // ????????????
        QiwuTrainTicketPayViewData viewData = (QiwuTrainTicketPayViewData) data;
        LogUtil.logd(WinLayout.logTag + "QiwuTrainTicketPayViewData.vTips:" + viewData.vTips);
        View view;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                if (WinLayout.isVertScreen){
                    view = createViewVert(viewData);
                }else {
                    view = createViewFull(viewData);
                }
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            default:
                view = createViewNone(viewData);
                break;
        }

        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.isListView = true;
        viewAdapter.object = TicketPayView.getInstance();
        return viewAdapter;
    }

    private View createViewFull(QiwuTrainTicketPayViewData viewData){
        ticketPayBean = viewData.mTicketBeans.get(0);

        // ????????????????????????LinearLayout?????????
        LinearLayout view = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setLayoutParams(layoutParams);
        // title
        View title = genTitleLayout(viewData);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        view.addView(title, layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        int contentHeight = SizeConfig.pageFlightCount * SizeConfig.itemHeightPro;
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
        view.addView(llContents,layoutParams);

        // ????????????
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
//        llContent.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContents.addView(llContent,
                new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1));

        mCurPage = viewData.mTitleInfo.curPage;
        mMaxPage = viewData.mTitleInfo.maxPage;
        if(mMaxPage > 1){
            contentLeftMargin = 0;
            LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
            //llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
            layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
            llContents.addView(llPager,layoutParams);
            WinLayout.getInstance().vTips = viewData.vTips;
        }
        llContents.setPadding(contentLeftMargin,0,0,0);

        // ????????????
        llContent.addView(genTicketTitleLayout(),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        0,topHeight));

        // ?????????
        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        llContent.addView(divider, layoutParams);

        // ????????????
        LinearLayout llBottom = new LinearLayout(GlobalContext.get());
        llBottom.setOrientation(LinearLayout.HORIZONTAL);
        llBottom.setGravity(Gravity.CENTER_VERTICAL);
        llContent.addView(llBottom,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        0,bottomHeight));

        // ????????????
        View vInfo = genInfoLayout();
        LinearLayout.LayoutParams vInfoLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vInfoLayoutParams.gravity = Gravity.CENTER;
        vInfoLayoutParams.leftMargin = iconHorMargin;
        llBottom.addView(vInfo, vInfoLayoutParams);

        // ????????????
        View vPay = genPayLayout();
        LinearLayout.LayoutParams vPayLayoutParams =
                new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 1);
        vPayLayoutParams.gravity = Gravity.CENTER;
        llBottom.addView(vPay, vPayLayoutParams);

        return view;
    }

    private View createViewNone(QiwuTrainTicketPayViewData viewData){
        ticketPayBean = viewData.mTicketBeans.get(0);

        // ????????????????????????LinearLayout?????????
        LinearLayout view = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setOrientation(LinearLayout.HORIZONTAL);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setLayoutParams(layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1);
        view.addView(llContents,layoutParams);

        mCurPage = viewData.mTitleInfo.curPage;
        mMaxPage = viewData.mTitleInfo.maxPage;
        if(mMaxPage > 1){
            contentLeftMargin = 0;
            LinearLayout llPager = new PageView(GlobalContext.get(),mCurPage,mMaxPage);
            //llPager.setBackground(LayouUtil.getDrawable("white_right_range_layout"));
            layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,LinearLayout.LayoutParams.MATCH_PARENT);
            view.addView(llPager,layoutParams);
            WinLayout.getInstance().vTips = viewData.vTips;
        }

        // title
        View title = genTitleLayout(viewData);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llContents.addView(title, layoutParams);

        // ?????????
        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        llContents.addView(divider, layoutParams);

        // ????????????
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContents.addView(llContent,
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1));
        llContents.setPadding(contentLeftMargin,0,0,0);

        // ????????????
        llContent.addView(genTicketTitleLayout(),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        0,topHeight));

        // ????????????
        LinearLayout llBottom = new LinearLayout(GlobalContext.get());
        llBottom.setOrientation(LinearLayout.HORIZONTAL);
        llBottom.setGravity(Gravity.CENTER_VERTICAL);
        llContent.addView(llBottom,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        0,bottomHeight));

        // ????????????
        View vInfo = genInfoLayout();
        LinearLayout.LayoutParams vInfoLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vInfoLayoutParams.gravity = Gravity.CENTER;
        vInfoLayoutParams.leftMargin = iconHorMargin;
        llBottom.addView(vInfo, vInfoLayoutParams);

        // ????????????
        View vPay = genPayLayout();
        LinearLayout.LayoutParams vPayLayoutParams =
                new LinearLayout.LayoutParams(0,
                        ViewGroup.LayoutParams.MATCH_PARENT, 1);
        vPayLayoutParams.gravity = Gravity.CENTER;
        llBottom.addView(vPay, vPayLayoutParams);

        return view;
    }

    private View createViewVert(QiwuTrainTicketPayViewData viewData){
        ticketPayBean = viewData.mTicketBeans.get(0);

        // ????????????????????????LinearLayout?????????
        LinearLayout view = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setLayoutParams(layoutParams);
        // title
        View title = genTitleLayout(viewData);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        view.addView(title, layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.HORIZONTAL);
        llContents.setBackground(LayouUtil.getDrawable("white_range_layout"));
        // ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        int contentHeight = SizeConfig.pageFlightCount * SizeConfig.itemHeightPro;
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,contentHeight);
        view.addView(llContents,layoutParams);

        // ????????????
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
//        llContent.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContents.addView(llContent,
                new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1));

        mCurPage = viewData.mTitleInfo.curPage;
        mMaxPage = viewData.mTitleInfo.maxPage;

//        llContents.setPadding(contentLeftMargin,0,0,0);

        // ????????????
        llContent.addView(genTicketTitleLayout(),
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        0,topHeight));

        // ?????????
        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        llContent.addView(divider, layoutParams);

        // ????????????
        LinearLayout llBottom = new LinearLayout(GlobalContext.get());
        llBottom.setOrientation(LinearLayout.VERTICAL);
        llBottom.setGravity(Gravity.CENTER_HORIZONTAL);
        llContent.addView(llBottom,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        0,bottomHeight));

        // ????????????
        View vInfo = genInfoLayout();
        LinearLayout.LayoutParams vInfoLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vInfoLayoutParams.gravity = Gravity.CENTER;
        vInfoLayoutParams.topMargin = ViewParamsUtil.unit;
        vInfoLayoutParams.bottomMargin = ViewParamsUtil.unit;
        llBottom.addView(vInfo, vInfoLayoutParams);

        // ????????????
        View vPay = genPayLayoutVert();
        LinearLayout.LayoutParams vPayLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        0, 1);
        vPayLayoutParams.gravity = Gravity.CENTER;
        llBottom.addView(vPay, vPayLayoutParams);

        return view;
    }

    private View genTitleLayout(QiwuTrainTicketPayViewData viewData) {
        View result = null;
        ListTitleView titleView = ListTitleView.getInstance();
        if(ticketPayBean.payType.contains("flight")){
            result = titleView.getView(viewData, "", "???????????????").view;
            titleView.setIvIcon("flight");
        }else if(ticketPayBean.payType.contains("train")){
            result = titleView.getView(viewData, "", "???????????????").view;
            titleView.setIvIcon("train");
        }
        return result;
    }

    /**
     * ?????????????????????????????????view
     */
    private View genInfoLayout() {
        String payType = ticketPayBean.payType;

        QiwuTrainTicketPayViewData.FlightTicketPayBean flightTicketPayBean;
        QiwuTrainTicketPayViewData.TrainTicketPayBean trainTicketPayBean;
        String seatView = "";
        String seatValue = "";
        String ticketType = "";
        String ticketValue = "";
        String ticketTime = "";
        String timeValue = "";
        if (payType.contains("flight")) {
            flightTicketPayBean = (QiwuTrainTicketPayViewData.FlightTicketPayBean) ticketPayBean;
            seatView = "????????????:";
            seatValue = flightTicketPayBean.fuelSurcharge;
            ticketType = "???    ???:";
            ticketValue = flightTicketPayBean.ticketNo;
            ticketTime = "????????????:";
            timeValue = flightTicketPayBean.departureTime;
        } else if (payType.contains("train")) {
            trainTicketPayBean = (QiwuTrainTicketPayViewData.TrainTicketPayBean) ticketPayBean;
            seatView = "???    ???:";
            seatValue = trainTicketPayBean.seat;
            ticketType = "???    ???:";
            ticketValue = trainTicketPayBean.ticketNo;
            ticketTime = "????????????:";
            timeValue = trainTicketPayBean.departureTime;
        }
        RelativeLayout result = new RelativeLayout(GlobalContext.get());

        int width = 38 * ViewParamsUtil.unit;
        if (WinLayout.isVertScreen){
            width = qrCodeSide + centerIntervalH + qrCodeSide;
        }
        if (StyleConfig.getInstance().getSelectStyleIndex() == StyleConfig.STYLE_ROBOT_NONE_SCREES){
            width = 36 * ViewParamsUtil.unit;
        }
        // ?????????
        // ??????
        View vName = genInfoItemView("???    ???:", ticketPayBean.passengerName,ticketType, ticketValue);
        vName.setId(ViewUtils.generateViewId());
        result.addView(vName, width,
                ViewGroup.LayoutParams.WRAP_CONTENT);

//        View vTrainNo = genInfoItemView();
//        vTrainNo.setId(ViewUtils.generateViewId());
//        RelativeLayout.LayoutParams vTrainNoLayoutParams =
//                new RelativeLayout.LayoutParams(((int) getDimen("x153")),
//                        ViewGroup.LayoutParams.WRAP_CONTENT);
//        vTrainNoLayoutParams.addRule(RelativeLayout.RIGHT_OF, vName.getId());
//        vTrainNoLayoutParams.addRule(RelativeLayout.ALIGN_TOP, vName.getId());
//        vTrainNoLayoutParams.leftMargin = (int) getDimen("x16");
//        result.addView(vTrainNo, vTrainNoLayoutParams);

        // ?????????
        // ?????????
        View vStation = genInfoItemView("?????????:", ticketPayBean.station,"?????????:", ticketPayBean.endStation);
        vStation.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams vStationLayoutParams =
                new RelativeLayout.LayoutParams(width,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        vStationLayoutParams.addRule(RelativeLayout.BELOW, vName.getId());
        vStationLayoutParams.topMargin = centerInterval;
        result.addView(vStation, vStationLayoutParams);

        // ?????????
//        View vEndStation = genInfoItemView("?????????:", ticketPayBean.endStation);
//        vEndStation.setId(ViewUtils.generateViewId());
//        RelativeLayout.LayoutParams vEndStationLayoutParams =
//                new RelativeLayout.LayoutParams(((int) getDimen("x153")),
//                        ViewGroup.LayoutParams.WRAP_CONTENT);
//        vEndStationLayoutParams.addRule(RelativeLayout.RIGHT_OF, vStation.getId());
//        vEndStationLayoutParams.addRule(RelativeLayout.ALIGN_TOP, vStation.getId());
//        vEndStationLayoutParams.leftMargin = (int) getDimen("x16");
//        result.addView(vEndStation, vEndStationLayoutParams);

        // ?????????
        // ??????
        View vDepartureTime = genInfoItemView("???    ???:", ticketPayBean.departureDate,ticketTime, timeValue);
        vDepartureTime.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams vDepartureTimeLayoutParams =
                new RelativeLayout.LayoutParams(width,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        vDepartureTimeLayoutParams.addRule(RelativeLayout.BELOW, vStation.getId());
        vDepartureTimeLayoutParams.topMargin = centerInterval;
        result.addView(vDepartureTime, vDepartureTimeLayoutParams);

        // ??????
//        View vCostTime = genInfoItemView(ticketTime, timeValue);
//        vCostTime.setId(ViewUtils.generateViewId());
//        RelativeLayout.LayoutParams vCostTimeLayoutParams =
//                new RelativeLayout.LayoutParams(((int) getDimen("x153")),
//                        ViewGroup.LayoutParams.WRAP_CONTENT);
//        vCostTimeLayoutParams.addRule(RelativeLayout.RIGHT_OF, vDepartureTime.getId());
//        vCostTimeLayoutParams.addRule(RelativeLayout.ALIGN_TOP, vDepartureTime.getId());
//        vCostTimeLayoutParams.leftMargin = (int) getDimen("x16");
//        result.addView(vCostTime, vCostTimeLayoutParams);

        // ?????????
        // ?????????
        View vPrice = genInfoItemView("?????????:", "??" + ticketPayBean.price,seatView, seatValue);
        vPrice.setId(ViewUtils.generateViewId());
        RelativeLayout.LayoutParams vPriceLayoutParams =
                new RelativeLayout.LayoutParams(width,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        vPriceLayoutParams.addRule(RelativeLayout.BELOW, vDepartureTime.getId());
        vPriceLayoutParams.topMargin = centerInterval;
        result.addView(vPrice, vPriceLayoutParams);

        // ??????
//        View vSeat = genInfoItemView(seatView, seatValue);
//        vSeat.setId(ViewUtils.generateViewId());
//        RelativeLayout.LayoutParams vSeatLayoutParams =
//                new RelativeLayout.LayoutParams(((int) getDimen("x153")),
//                        ViewGroup.LayoutParams.WRAP_CONTENT);
//        vSeatLayoutParams.addRule(RelativeLayout.RIGHT_OF, vPrice.getId());
//        vSeatLayoutParams.addRule(RelativeLayout.ALIGN_TOP, vPrice.getId());
//        vSeatLayoutParams.leftMargin = (int) getDimen("x16");
//        result.addView(vSeat, vSeatLayoutParams);

        return result;
    }

    TextView tvRemainTime;
    long remainTime;
    Runnable remainTimeRun = new Runnable() {
        @Override
        public void run() {
            AppLogicBase.removeUiGroundCallback(this);
            remainTime = remainTime - 1;
            if(remainTime < 0){
                remainTime  = 0;
            }
            long remainHour = remainTime / 60;
            long remainSecond = remainTime % 60;
            if (remainSecond < 10) {
                tvRemainTime.setText("??????????????????: " + remainHour + ":0" + remainSecond);
            } else {
                tvRemainTime.setText("??????????????????: " + remainHour + ":" + remainSecond);
            }
            if (remainTime > 0) {
                AppLogicBase.runOnUiGround(this, 1000);
            }
        }
    };

    /**
     * ??????????????????????????????????????????????????????
     */
    private View genTicketTitleLayout() {
        String payType = ticketPayBean.payType;
        LinearLayout result = new LinearLayout(GlobalContext.get());
        result.setOrientation(LinearLayout.HORIZONTAL);
        result.setGravity(Gravity.CENTER_VERTICAL);

        // ????????????????????????????????????
        ImageView ivIcon = new ImageView(GlobalContext.get());

        LinearLayout.LayoutParams iconLayoutParams =
                new LinearLayout.LayoutParams(iconSide,iconSide);
        iconLayoutParams.leftMargin = iconHorMargin;
        iconLayoutParams.rightMargin = iconHorMargin;
        result.addView(ivIcon, iconLayoutParams);

        // ??????title???????????????????????????layout
        LinearLayout llTitle = new LinearLayout(GlobalContext.get());
        llTitle.setOrientation(LinearLayout.VERTICAL);

        // title
        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setGravity(Gravity.CENTER_VERTICAL);
        tvTitle.setSingleLine();
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        tvTitle.setIncludeFontPadding(false);
        TextViewUtil.setTextSize(tvTitle,tvFormTypeSize);
        TextViewUtil.setTextColor(tvTitle,tvFormTypeColor);
//        tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m23"));
//        tvTitle.setTextColor(0xFFFFFFFF);
        LinearLayout.LayoutParams tvTitleLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, tvFormTypeHeight);
        llTitle.addView(tvTitle, tvTitleLayoutParams);

        // ????????????
        remainTime = Long.valueOf(ticketPayBean.expirationTime);

        if ("train".equals(payType) || "flight".equals(payType)) {
            tvRemainTime = new TextView(GlobalContext.get());
            tvRemainTime.setGravity(Gravity.CENTER_VERTICAL);
            tvRemainTime.setSingleLine();
            tvRemainTime.setEllipsize(TextUtils.TruncateAt.END);
            tvRemainTime.setIncludeFontPadding(false);
//            tvRemainTime.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m18"));
//            tvRemainTime.setTextColor(0x99FFFFFF);
            TextViewUtil.setTextSize(tvRemainTime,tvPayTimeSize);
            TextViewUtil.setTextColor(tvRemainTime,tvPayTimeColor);
            long remainHour = remainTime / 60;
            long remainSecond = remainTime % 60;
            tvRemainTime.setText("??????????????????: " + remainHour + ":" + remainSecond);
            LinearLayout.LayoutParams tvRemainTitleLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, tvPayTimeHeight);
            tvRemainTitleLayoutParams.topMargin = ViewParamsUtil.unit / 2;
            llTitle.addView(tvRemainTime, tvRemainTitleLayoutParams);
            AppLogicBase.runOnUiGround(remainTimeRun, 1000);
        }else if(payType.contains("REFUND")){
            TextView tvRefundHint = new TextView(GlobalContext.get());
            tvRefundHint.setGravity(Gravity.CENTER_VERTICAL);
            tvRefundHint.setSingleLine();
            tvRefundHint.setEllipsize(TextUtils.TruncateAt.END);
            tvRefundHint.setIncludeFontPadding(false);
//            tvRefundHint.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m18"));
//            tvRefundHint.setTextColor(0x99FFFFFF);
            TextViewUtil.setTextSize(tvRefundHint,tvPayTimeSize);
            TextViewUtil.setTextColor(tvRefundHint,tvPayTimeColor);
            tvRefundHint.setText("?????????3-5????????????????????????");
            LinearLayout.LayoutParams tvRemainTitleLayoutParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, tvPayTimeHeight);
            tvRemainTitleLayoutParams.topMargin = ViewParamsUtil.unit / 2;
            llTitle.addView(tvRefundHint, tvRemainTitleLayoutParams);
        }

        int width = 38 * ViewParamsUtil.unit - iconSide - iconHorMargin;
        if (WinLayout.isVertScreen){
            width = qrCodeSide + centerIntervalH + qrCodeSide  - iconSide - iconHorMargin;
        }
        if (StyleConfig.getInstance().getSelectStyleIndex() == StyleConfig.STYLE_ROBOT_NONE_SCREES){
            width = 36 * ViewParamsUtil.unit  - iconSide - iconHorMargin;
        }
        LinearLayout.LayoutParams llTitleLayoutParams = new LinearLayout.LayoutParams(
//                (int) getDimen("x238"),
//                30 * ViewParamsUtil.unit,
                width,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llTitleLayoutParams.gravity = Gravity.CENTER_VERTICAL;
        result.addView(llTitle, llTitleLayoutParams);

        // ?????????????????????layout?????????????????????????????????????????????????????????

        if(!payType.contains("CANCEL")){
            FrameLayout flCancel = new FrameLayout(GlobalContext.get());

            // ??????????????????
            TextView btnCancel = new TextView(GlobalContext.get());
            btnCancel.setGravity(Gravity.CENTER);
            btnCancel.setSingleLine();
            btnCancel.setEllipsize(TextUtils.TruncateAt.END);
            btnCancel.setIncludeFontPadding(false);
            if(payType.contains("Success")){
                btnCancel.setText("??????");
                btnCancel.setOnClickListener(cancelOrd);
            }else if(payType.contains("REFUND")){
                btnCancel.setText("?????????");
                btnCancel.setPressed(true);
                btnCancel.setEnabled(false);
            }else if(payType.contains("Payed")){
                btnCancel.setText("?????????");
                btnCancel.setPressed(true);
                btnCancel.setEnabled(false);
            }
            else {
                btnCancel.setText(mCancelText);
                btnCancel.setOnClickListener(cancelOrd);
            }
//            btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m16"));
//            btnCancel.setTextColor(0xFFFFFFFF);
            TextViewUtil.setTextSize(btnCancel,tvCancelSize);
            TextViewUtil.setTextColor(btnCancel,tvCancelColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                btnCancel.setBackground(getDrawable("qiwu_order_cancel_bg"));
            }
            FrameLayout.LayoutParams btnCancelLayoutParams = new FrameLayout.LayoutParams(
                    btnCancelWidth,
                    btnCancelHeight);
            btnCancelLayoutParams.gravity = Gravity.CENTER;
            flCancel.addView(btnCancel, btnCancelLayoutParams);

            LinearLayout.LayoutParams flCancelLayoutParams = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT,1);
            flCancelLayoutParams.gravity = Gravity.CENTER;
            result.addView(flCancel, flCancelLayoutParams);
        }

        if ("flight".equals(payType) || "train".equals(payType)) {
            ivIcon.setImageDrawable(getDrawable("qiwu_ticket_pending_order_icon"));
            tvTitle.setText("?????????");
        }else if(payType.contains("Success")){
            ivIcon.setImageDrawable(getDrawable("qiwu_ticket_pay_ok"));
            tvTitle.setText("????????????");
        }else if(payType.contains("Payed")){
            ivIcon.setImageDrawable(getDrawable("qiwu_ticket_pay_ok"));
            tvTitle.setText("????????????");
        }
        else if(payType.contains("CANCEL")){
            ivIcon.setImageDrawable(getDrawable("qiwu_cancel_ord_icon"));
            tvTitle.setText("?????????");
        }else if(payType.contains("REFUND")){
            ivIcon.setImageDrawable(getDrawable("qiwu_refund_icon"));
            tvTitle.setText("????????????");
        }
        return result;
    }

    /**
     * ??????????????????view
     */
    private View genInfoItemView(String key1, String value1,String key2, String value2) {
        LinearLayout result = new LinearLayout(GlobalContext.get());
        result.setOrientation(LinearLayout.HORIZONTAL);
        result.setGravity(Gravity.CENTER_VERTICAL);

        // key??????
        TextView tvKey1 = new TextView(GlobalContext.get());
        tvKey1.setGravity(Gravity.CENTER_VERTICAL);
        tvKey1.setSingleLine();
        tvKey1.setEllipsize(TextUtils.TruncateAt.END);
        tvKey1.setIncludeFontPadding(true); // false???????????????????????????true
        tvKey1.setText(key1);
        result.addView(tvKey1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                tvContenkeyHeight));

        // value??????
        TextView tvValue1 = new TextView(GlobalContext.get());
        tvValue1.setGravity(Gravity.CENTER_VERTICAL);
        tvValue1.setSingleLine();
        tvValue1.setEllipsize(TextUtils.TruncateAt.END);
        tvValue1.setIncludeFontPadding(true);
        tvValue1.setText(value1);
        int width = 9 * ViewParamsUtil.unit;
        if (WinLayout.isVertScreen){
            width = 11 * ViewParamsUtil.unit;
        }
        if (StyleConfig.getInstance().getSelectStyleIndex() == StyleConfig.STYLE_ROBOT_NONE_SCREES){
            width = 9 * ViewParamsUtil.unit;
        }
        LinearLayout.LayoutParams tvValueLayoutParams =
                new LinearLayout.LayoutParams(width,
//                new LinearLayout.LayoutParams(0,
                        tvContenValueHeight);
        tvValueLayoutParams.leftMargin = ViewParamsUtil.unit;
        result.addView(tvValue1,
                tvValueLayoutParams);

        // key??????
        TextView tvKey2 = new TextView(GlobalContext.get());
        tvKey2.setGravity(Gravity.CENTER_VERTICAL);
        tvKey2.setSingleLine();
        tvKey2.setEllipsize(TextUtils.TruncateAt.END);
        tvKey2.setIncludeFontPadding(true); // false???????????????????????????true
        tvKey2.setText(key2);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,tvContenkeyHeight);
        layoutParams.leftMargin = centerIntervalH;
        result.addView(tvKey2, layoutParams);

        // value??????
        TextView tvValue2 = new TextView(GlobalContext.get());
        tvValue2.setGravity(Gravity.CENTER_VERTICAL);
        tvValue2.setSingleLine();
        tvValue2.setEllipsize(TextUtils.TruncateAt.END);
        tvValue2.setIncludeFontPadding(true);
        tvValue2.setText(value2);
        tvValueLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                new LinearLayout.LayoutParams(0,
                        tvContenValueHeight);
        tvValueLayoutParams.leftMargin = ViewParamsUtil.unit;
        result.addView(tvValue2,
                tvValueLayoutParams);

        TextViewUtil.setTextSize(tvKey1,tvContenkeySize);
        TextViewUtil.setTextColor(tvKey1,tvContenkeyColor);
        TextViewUtil.setTextSize(tvValue1,tvContenValueSize);
        TextViewUtil.setTextColor(tvValue1,tvContenValueColor);
        TextViewUtil.setTextSize(tvKey2,tvContenkeySize);
        TextViewUtil.setTextColor(tvKey2,tvContenkeyColor);
        TextViewUtil.setTextSize(tvValue2,tvContenValueSize);
        TextViewUtil.setTextColor(tvValue2,tvContenValueColor);
        return result;
    }

    /**
     * ????????????????????????????????????view
     */
    private View genPayLayout() {
        LinearLayout result = new LinearLayout(GlobalContext.get());
        result.setOrientation(LinearLayout.VERTICAL);
        result.setGravity(Gravity.CENTER);
        String payType = ticketPayBean.payType;
        if (!("train".equals(payType) || "flight".equals(payType))) {
            return result;
        }

        // ???????????????layout
        LinearLayout llPrice = new LinearLayout(GlobalContext.get());
        llPrice.setOrientation(LinearLayout.HORIZONTAL);
        llPrice.setGravity(Gravity.CENTER);

        // ???????????????key
        TextView tvPriceKey = new TextView(GlobalContext.get());
        tvPriceKey.setGravity(Gravity.CENTER_VERTICAL);
        tvPriceKey.setSingleLine();
        tvPriceKey.setEllipsize(TextUtils.TruncateAt.END);
        tvPriceKey.setIncludeFontPadding(false);
//        tvPriceKey.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m16"));
//        tvPriceKey.setTextColor(0x99FFFFFF);
        TextViewUtil.setTextSize(tvPriceKey,tvPriceSize);
        TextViewUtil.setTextColor(tvPriceKey,tvContenkeyColor);
        tvPriceKey.setText("?????????");
        LinearLayout.LayoutParams tvPriceKeyLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llPrice.addView(tvPriceKey, tvPriceKeyLayoutParams);

        // ???????????????value
        TextView tvPriceValue = new TextView(GlobalContext.get());
        tvPriceValue.setGravity(Gravity.CENTER);
        tvPriceValue.setSingleLine();
        tvPriceValue.setEllipsize(TextUtils.TruncateAt.END);
        tvPriceValue.setIncludeFontPadding(false);
//        tvPriceValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m23"));
//        tvPriceValue.setTextColor(0xFFFF8205);
        TextViewUtil.setTextSize(tvPriceValue,tvPriceSize);
        TextViewUtil.setTextColor(tvPriceValue,tvPriceColor);
        SpannableStringBuilder ssb = new SpannableStringBuilder("??" + ticketPayBean.price);
        ssb.setSpan(new RelativeSizeSpan(1.4f), 1, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPriceValue.setText(ssb);
        LinearLayout.LayoutParams tvPriceValueLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llPrice.addView(tvPriceValue, tvPriceValueLayoutParams);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        result.addView(llPrice, layoutParams);

        // ?????????

        ivQrcode = new ImageView(GlobalContext.get());
        ivQrcode.setBackgroundColor(0xFFFFFFFF);
        // ??????????????????logo????????????????????????????????????????????????padding????????????????????????
        int padding = ViewParamsUtil.unit / 2;
        ivQrcode.setPadding(padding, padding, padding, padding);
        Bitmap logoVX = ((BitmapDrawable) getDrawable("qiwu_ticket_wechat_pay_icon")).getBitmap();

        bitmapVX = QRUtil.createQRCodeWithLogo(ticketPayBean.parUrlWX,
                qrCodeSide, 1,ErrorCorrectionLevel.H, logoVX, qrCodeLogoSide);
        UI2Manager.runOnUIThread(new Runnable() {

            @Override
            public void run() {
                if (bitmapVX == null) {
                    return;
                }
                ivQrcode.setImageBitmap(bitmapVX);
            }
        }, 0);

        Bitmap logoZFB = ((BitmapDrawable) getDrawable("qiwu_ticket_alipay_icon")).getBitmap();

        bitmapZFB = QRUtil.createQRCodeWithLogo(ticketPayBean.payUrlZFB,
                qrCodeSide, 1, ErrorCorrectionLevel.H, logoZFB, qrCodeLogoSide);


        LinearLayout.LayoutParams ivQrcodeLayoutParams =
                new LinearLayout.LayoutParams(qrCodeSide, qrCodeSide);
        ivQrcodeLayoutParams.gravity = Gravity.CENTER;
        ivQrcodeLayoutParams.topMargin = qrCodeLogoMarginTop;
        ivQrcodeLayoutParams.bottomMargin = qrCodeLogoMarginBottom;
        result.addView(ivQrcode, ivQrcodeLayoutParams);

        // ??????????????????

        ivSwitch = new ImageView(GlobalContext.get());
        ivSwitch.setImageDrawable(getDrawable("qiwu_ticket_pending_order_select_wechat_pay"));
        LinearLayout.LayoutParams ivSwitchLayoutParams =
                new LinearLayout.LayoutParams(qrCodeSide,
                        btnSwitchHeight);
        ivSwitchLayoutParams.gravity = Gravity.CENTER;
        ivSwitch.setOnClickListener(switchListener);
        result.addView(ivSwitch, ivSwitchLayoutParams);

        return result;
    }
    /**
     * ????????????????????????????????????view
     */
    private View genPayLayoutVert() {
        LinearLayout result = new LinearLayout(GlobalContext.get());
        result.setOrientation(LinearLayout.VERTICAL);
        result.setGravity(Gravity.CENTER);
        String payType = ticketPayBean.payType;
        if (!("train".equals(payType) || "flight".equals(payType))) {
            return result;
        }

        // ???????????????layout
        LinearLayout llPrice = new LinearLayout(GlobalContext.get());
        llPrice.setOrientation(LinearLayout.HORIZONTAL);
        llPrice.setGravity(Gravity.CENTER);

        // ???????????????key
        TextView tvPriceKey = new TextView(GlobalContext.get());
        tvPriceKey.setGravity(Gravity.CENTER_VERTICAL);
        tvPriceKey.setSingleLine();
        tvPriceKey.setEllipsize(TextUtils.TruncateAt.END);
        tvPriceKey.setIncludeFontPadding(false);
//        tvPriceKey.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m16"));
//        tvPriceKey.setTextColor(0x99FFFFFF);
        TextViewUtil.setTextSize(tvPriceKey,tvPriceSize);
        TextViewUtil.setTextColor(tvPriceKey,tvContenValueColor);
        tvPriceKey.setText("?????????");
        LinearLayout.LayoutParams tvPriceKeyLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llPrice.addView(tvPriceKey, tvPriceKeyLayoutParams);

        // ???????????????value
        TextView tvPriceValue = new TextView(GlobalContext.get());
        tvPriceValue.setGravity(Gravity.CENTER);
        tvPriceValue.setSingleLine();
        tvPriceValue.setEllipsize(TextUtils.TruncateAt.END);
        tvPriceValue.setIncludeFontPadding(false);
//        tvPriceValue.setTextSize(TypedValue.COMPLEX_UNIT_PX, getDimen("m23"));
//        tvPriceValue.setTextColor(0xFFFF8205);
        TextViewUtil.setTextSize(tvPriceValue,tvPriceSize);
        TextViewUtil.setTextColor(tvPriceValue,tvPriceColor);
        SpannableStringBuilder ssb = new SpannableStringBuilder("??" + ticketPayBean.price);
        ssb.setSpan(new RelativeSizeSpan(1.4f), 1, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPriceValue.setText(ssb);
        LinearLayout.LayoutParams tvPriceValueLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llPrice.addView(tvPriceValue, tvPriceValueLayoutParams);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        result.addView(llPrice, layoutParams);

        // ???????????????layout
        LinearLayout llQrcode = new LinearLayout(GlobalContext.get());
        llQrcode.setOrientation(LinearLayout.HORIZONTAL);
        llQrcode.setGravity(Gravity.CENTER);

        // ?????????
        ivQrcode = new ImageView(GlobalContext.get());
        ivQrcode.setBackgroundColor(0xFFFFFFFF);
        // ??????????????????logo????????????????????????????????????????????????padding????????????????????????
        int padding = ViewParamsUtil.unit / 2;
        ivQrcode.setPadding(padding, padding, padding, padding);

        ImageView ivQrcode2 = new ImageView(GlobalContext.get());
        ivQrcode2.setBackgroundColor(0xFFFFFFFF);
        ivQrcode2.setPadding(padding, padding, padding, padding);

        Bitmap logoVX = ((BitmapDrawable) getDrawable("qiwu_ticket_wechat_pay_icon")).getBitmap();

        bitmapVX = QRUtil.createQRCodeWithLogo(ticketPayBean.parUrlWX,
                qrCodeSide, 1,ErrorCorrectionLevel.H, logoVX, qrCodeLogoSide);
        UI2Manager.runOnUIThread(new Runnable() {

            @Override
            public void run() {
                if (bitmapVX == null) {
                    return;
                }
                ivQrcode.setImageBitmap(bitmapVX);
            }
        }, 0);

        Bitmap logoZFB = ((BitmapDrawable) getDrawable("qiwu_ticket_alipay_icon")).getBitmap();

        bitmapZFB = QRUtil.createQRCodeWithLogo(ticketPayBean.payUrlZFB,
                qrCodeSide, 1, ErrorCorrectionLevel.H, logoZFB, qrCodeLogoSide);
        ivQrcode2.setImageBitmap(bitmapZFB);

        LinearLayout.LayoutParams ivQrcodeLayoutParams =
                new LinearLayout.LayoutParams(qrCodeSide, qrCodeSide);
        ivQrcodeLayoutParams.gravity = Gravity.CENTER;
        llQrcode.addView(ivQrcode, ivQrcodeLayoutParams);

        ivQrcodeLayoutParams =
                new LinearLayout.LayoutParams(qrCodeSide, qrCodeSide);
        ivQrcodeLayoutParams.gravity = Gravity.CENTER;
        ivQrcodeLayoutParams.leftMargin = 10 * ViewParamsUtil.unit;
        llQrcode.addView(ivQrcode2, ivQrcodeLayoutParams);

        LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams1.topMargin = qrCodeLogoMarginTop;
        layoutParams1.bottomMargin = qrCodeLogoMarginBottom;
        result.addView(llQrcode, layoutParams1);

        return result;
    }

    Bitmap bitmapZFB;
    Bitmap bitmapVX;
    ImageView ivQrcode;
    ImageView ivSwitch;
    boolean urlType = false;

    View.OnClickListener switchListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (urlType) {
                ivSwitch.setImageDrawable(
                        getDrawable("qiwu_ticket_pending_order_select_wechat_pay"));
                urlType = false;
                UI2Manager.runOnUIThread(new Runnable() {

                    @Override
                    public void run() {
                        if (bitmapVX == null) {
                            return;
                        }
                        ivQrcode.setImageBitmap(bitmapVX);
                    }
                }, 0);
            } else {
                ivSwitch.setImageDrawable(getDrawable("qiwu_ticket_pending_order_select_alipay"));
                urlType = true;
                UI2Manager.runOnUIThread(new Runnable() {

                    @Override
                    public void run() {
                        if (bitmapZFB == null) {
                            return;
                        }
                        ivQrcode.setImageBitmap(bitmapZFB);
                    }
                }, 0);
            }
        }
    };

    View.OnClickListener cancelOrd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("type", ticketPayBean.payType);
                jsonObject.put("orderId", ticketPayBean.orderId);
                jsonObject.put("sonAccount", ticketPayBean.sonAccount);
                jsonObject.put("idNumber", ticketPayBean.idNumber);
                jsonObject.put("phoneNum", ticketPayBean.phoneNum);
                jsonObject.put("sonAccount", ticketPayBean.sonAccount);
                jsonObject.put("passengerName", ticketPayBean.passengerName);
                jsonObject.put("orderUniqueId", ticketPayBean.orderUniqueId);
                jsonObject.put("canRefund", ticketPayBean.canRefund);
                if(ticketPayBean.payType.contains("train")){
                    QiwuTrainTicketPayViewData.TrainTicketPayBean tb = (QiwuTrainTicketPayViewData.TrainTicketPayBean)ticketPayBean;
                    jsonObject.put("passengerId", tb.passengeId);
                }else {
                    jsonObject.put("passengerId", "");
                }
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_TICKET_INFO_CANCEL, 0, 0,
                        1,jsonObject.toString());
            } catch (JSONException e) {
                return;
            }
        }
    };

    @Override
    public void updateProgress(final int i, final int i1) {

    }

    @Override
    public void snapPage(final boolean b) {

    }

    @Override
    public void updateItemSelect(final int i) {

    }
}
