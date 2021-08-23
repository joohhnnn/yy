package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.IMovieWaitingPayQRView;
import com.txznet.comm.ui.viewfactory.data.MovieWaitingPayQRViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMovieWaitingPayQRView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.QRUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.txznet.comm.ui.util.LayouUtil.getDimen;
import static com.txznet.comm.ui.util.LayouUtil.getDrawable;

public class MovieWaitingPayQRView extends IMovieWaitingPayQRView {

    private static MovieWaitingPayQRView sInstance = new MovieWaitingPayQRView();

    private MovieWaitingPayQRView(){}

    private int topHeight;//内容中分隔线上部分高度
    private int topHorMargin;//内容中分隔线上部分左右边距
    private int topVerMargin;//内容中分隔线上部分上下两行间隔
    private int tvTitleSize;//电影名字体大小
    private int tvTitleColor;//电影名字体颜色
    private int tvTitleHeight;//电影名字体行高
    private int tvNameSize;//影院名字体大小
    private int tvNameColor;//影院名字体颜色
    private int tvNameHeight;//影院名字体行高
    private int tvInfoSize;//时间场次信息字体大小
    private int tvInfoColor;//电影名字体颜色
    private int tvInfoHeight;//电影名字体行高
    private int phoneNumTopMargin;//手机号码上边距
    private int tvPhoneNumSize;//手机号码字体大小
    private int tvPhoneNumColor;//手机号码字体颜色
    private int tvPhoneNumHeight;//手机号码字体行高
    private int qrCodeSide;//二维码大小
    private int qrCodeLogoSide;//二维码中间logo大小
    private int qrCodeInterval;//二维码中间间隔

    public static MovieWaitingPayQRView getInstance(){
        return sInstance;
    }

    @Override
    public void init() {
        super.init();
        tvTitleColor = Color.parseColor(LayouUtil.getString("color_main_title"));//电影名字体颜色
        tvNameColor = Color.parseColor(LayouUtil.getString("color_main_title"));//影院名字体颜色
        tvInfoColor = Color.parseColor(LayouUtil.getString("color_main_title"));//电影名字体颜色
        tvPhoneNumColor = Color.parseColor("#16CFFF");
    }

    //切换模式修改布局参数
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

    //全屏布局参数
    private void initFull() {
        int unit = ViewParamsUtil.unit;
        if (WinLayout.isVertScreen){
            topHeight = 15 * unit;
            topHorMargin = 4 * unit;
            topVerMargin = 2 * unit;
            tvTitleSize = ViewParamsUtil.h3;//电影名字体大小
            tvTitleHeight = ViewParamsUtil.h3Height;//电影名字体行高
            tvNameSize = ViewParamsUtil.h5;//影院名字体大小
            tvNameHeight = ViewParamsUtil.h5Height;//影院名字体行高
            tvInfoSize = ViewParamsUtil.h7;//时间场次信息字体大小
            tvInfoHeight = ViewParamsUtil.h7Height;//电影名字体行高
            phoneNumTopMargin = 2 * ViewParamsUtil.unit;
            tvPhoneNumSize = ViewParamsUtil.h5;
            tvPhoneNumHeight = ViewParamsUtil.h5Height;
            qrCodeSide = 20 * ViewParamsUtil.unit;
            qrCodeLogoSide = 4 * ViewParamsUtil.unit;
            qrCodeInterval = 8 * ViewParamsUtil.unit;
        }else {
            topHorMargin = 4 * unit;
            tvTitleSize = ViewParamsUtil.h3;//电影名字体大小
            tvTitleHeight = ViewParamsUtil.h3Height;//电影名字体行高
            tvNameSize = ViewParamsUtil.h5;//影院名字体大小
            tvNameHeight = ViewParamsUtil.h5Height;//影院名字体行高
            tvInfoSize = ViewParamsUtil.h7;//时间场次信息字体大小
            tvInfoHeight = ViewParamsUtil.h7Height;//电影名字体行高
            phoneNumTopMargin = 2 * ViewParamsUtil.unit;
            tvPhoneNumSize = ViewParamsUtil.h5;
            tvPhoneNumHeight = ViewParamsUtil.h5Height;
            qrCodeInterval = 8 * ViewParamsUtil.unit;
            if (SizeConfig.screenHeight < 480){
                qrCodeSide = SizeConfig.screenHeight / 4;
                qrCodeLogoSide = qrCodeSide / 5;
                topHeight = 10 * unit;
                topVerMargin = 2;
            }else {
                qrCodeSide = 20 * ViewParamsUtil.unit;
                qrCodeLogoSide = 4 * ViewParamsUtil.unit;
                topHeight = 15 * unit;
                topVerMargin = 2 * unit;
            }
        }
    }

    //半屏布局参数
    private void initHalf() {
        int unit = ViewParamsUtil.unit;
        if (WinLayout.isVertScreen){
            topHeight = 15 * unit;
            topHorMargin = 4 * unit;
            topVerMargin = 2 * unit;
            tvTitleSize = ViewParamsUtil.h3;//电影名字体大小
            tvTitleHeight = ViewParamsUtil.h3Height;//电影名字体行高
            tvNameSize = ViewParamsUtil.h5;//影院名字体大小
            tvNameHeight = ViewParamsUtil.h5Height;//影院名字体行高
            tvInfoSize = ViewParamsUtil.h7;//时间场次信息字体大小
            tvInfoHeight = ViewParamsUtil.h7Height;//电影名字体行高
            phoneNumTopMargin = 2 * ViewParamsUtil.unit;
            tvPhoneNumSize = ViewParamsUtil.h5;
            tvPhoneNumHeight = ViewParamsUtil.h5Height;
            qrCodeSide = 20 * ViewParamsUtil.unit;
            qrCodeLogoSide = 4 * ViewParamsUtil.unit;
            qrCodeInterval = 8 * ViewParamsUtil.unit;
        }else {
            topHorMargin = 4 * unit;
            tvTitleSize = ViewParamsUtil.h3;//电影名字体大小
            tvTitleHeight = ViewParamsUtil.h3Height;//电影名字体行高
            tvNameSize = ViewParamsUtil.h5;//影院名字体大小
            tvNameHeight = ViewParamsUtil.h5Height;//影院名字体行高
            tvInfoSize = ViewParamsUtil.h7;//时间场次信息字体大小
            tvInfoHeight = ViewParamsUtil.h7Height;//电影名字体行高
            phoneNumTopMargin = 2 * ViewParamsUtil.unit;
            tvPhoneNumSize = ViewParamsUtil.h5;
            tvPhoneNumHeight = ViewParamsUtil.h5Height;
            qrCodeInterval = 8 * ViewParamsUtil.unit;
            if (SizeConfig.screenHeight < 480){
                qrCodeSide = SizeConfig.screenHeight / 4;
                qrCodeLogoSide = qrCodeSide / 5;
                topHeight = 8 * unit;
                topVerMargin = 2;
            }else {
                qrCodeSide = 20 * ViewParamsUtil.unit;
                qrCodeLogoSide = 4 * ViewParamsUtil.unit;
                topHeight = 12 * unit;
                topVerMargin = 2 * unit;
            }
        }
    }

    //无屏布局参数
    private void initNone() {
        int unit = ViewParamsUtil.unit;
        topHeight = 10 * unit;
        topHorMargin = 3 * unit;
        topVerMargin = (int)(0.8 * unit);
        tvTitleSize = ViewParamsUtil.h3;//电影名字体大小
        tvTitleHeight = ViewParamsUtil.h3Height;//电影名字体行高
        tvNameSize = ViewParamsUtil.h5;//影院名字体大小
        tvNameHeight = ViewParamsUtil.h5Height;//影院名字体行高
        tvInfoSize = ViewParamsUtil.h7;//时间场次信息字体大小
        tvInfoHeight = ViewParamsUtil.h7Height;//电影名字体行高
        phoneNumTopMargin = (int)(1.5 * ViewParamsUtil.unit);
        tvPhoneNumSize = ViewParamsUtil.h5;
        tvPhoneNumHeight = ViewParamsUtil.h5Height;
        qrCodeInterval = 6 * ViewParamsUtil.unit;
        if (SizeConfig.screenHeight < 480){
            qrCodeSide = SizeConfig.screenHeight / 4;
            qrCodeLogoSide = qrCodeSide / 5;
        }else {
            qrCodeSide = 18 * ViewParamsUtil.unit;
            qrCodeLogoSide = (int)(3.6 * ViewParamsUtil.unit);
        }
    }

    @SuppressLint("NewApi")
    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        LogUtil.logd(WinLayout.logTag+ "getView: MovieWaitingPayQRView");

        View view;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                view = createViewFull(data);
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            default:
                view = createViewNone(data);
                break;
        }

        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.object = DefaultMovieWaitingPayQRView.getInstance();
        return viewAdapter;
    }

    private View createViewFull(ViewData data){
        final MovieWaitingPayQRViewData viewData = (MovieWaitingPayQRViewData) data;
        WinLayout.getInstance().vTips = viewData.vTips;

        LinearLayout lyContents = new LinearLayout(GlobalContext.get());
        lyContents.setGravity(Gravity.CENTER_VERTICAL);
        lyContents.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentsLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        lyContents.setLayoutParams(contentsLP);

        ViewFactory.ViewAdapter titleViewAdapter = com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getView(viewData,
                "movie","电影票支付");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        lyContents.addView(titleViewAdapter.view,layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        llContents.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pageCount * SizeConfig.itemHeight);
        lyContents.addView(llContents,layoutParams);

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,topHeight);
        LinearLayout lyTitle = new LinearLayout(GlobalContext.get());
        lyTitle.setOrientation(LinearLayout.VERTICAL);
        lyTitle.setGravity(Gravity.CENTER);
        lyTitle.setPadding(topHorMargin,0,topHorMargin,0);
        llContents.addView(lyTitle,layoutParams);

        View top = new View(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        lyTitle.addView(top,layoutParams);

        LinearLayout lyTitleFirstLine = new LinearLayout(GlobalContext.get());
        lyTitleFirstLine.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lyTitle.addView(lyTitleFirstLine,layoutParams);

        TextView tvMoiveName = new TextView(GlobalContext.get());
        tvMoiveName.setText(viewData.moiveName);
        tvMoiveName.setEllipsize(TextUtils.TruncateAt.END);
        tvMoiveName.setSingleLine();
        TextViewUtil.setTextSize(tvMoiveName,tvTitleSize);
        TextViewUtil.setTextColor(tvMoiveName,tvTitleColor);
        layoutParams = new LinearLayout.LayoutParams(0,tvTitleHeight,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        lyTitleFirstLine.addView(tvMoiveName,layoutParams);

        TextView tvcinemaName = new TextView(GlobalContext.get());
        tvcinemaName.setGravity(Gravity.END);
        tvcinemaName.setText(viewData.cinemaName);
        tvcinemaName.setEllipsize(TextUtils.TruncateAt.END);
        tvcinemaName.setSingleLine();
        TextViewUtil.setTextSize(tvcinemaName,tvNameSize);
        TextViewUtil.setTextColor(tvcinemaName,tvNameColor);
        layoutParams = new LinearLayout.LayoutParams(0,tvNameHeight,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        layoutParams.leftMargin = 2 * ViewParamsUtil.unit;
        lyTitleFirstLine.addView(tvcinemaName,layoutParams);

        View middle = new View(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,topVerMargin);
        lyTitle.addView(middle,layoutParams);

        LinearLayout lyTitleSecondLine = new LinearLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lyTitle.addView(lyTitleSecondLine,layoutParams);

        TextView showTime = new TextView(GlobalContext.get());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(viewData.showTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int showTimeMonth = calendar.get(Calendar.MONTH)+1;
        int showTimeDay = calendar.get(Calendar.DATE);
        int showTimeHour = calendar.get(Calendar.HOUR_OF_DAY);
        String showTimeMinute = calendar.get(Calendar.MINUTE) >= 10? String.valueOf(calendar.get(Calendar.MINUTE)): ("0"+calendar.get(Calendar.MINUTE));
        String showTimeText = showTimeMonth+"月"+showTimeDay+"日 "+showTimeHour+":"+showTimeMinute+"分 ("+viewData.showVersion+")";
        showTime.setText(showTimeText);
        showTime.setEllipsize(TextUtils.TruncateAt.END);
        showTime.setSingleLine();
        TextViewUtil.setTextSize(showTime,tvInfoSize);
        TextViewUtil.setTextColor(showTime,tvInfoColor);
        layoutParams = new LinearLayout.LayoutParams(0,tvInfoHeight,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        lyTitleSecondLine.addView(showTime,layoutParams);

        LinearLayout llTicket = new LinearLayout(GlobalContext.get());
        llTicket.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        layoutParams.leftMargin = 2 * ViewParamsUtil.unit;
        lyTitleSecondLine.addView(llTicket,layoutParams);

        TextView seats = new TextView(GlobalContext.get());
        seats.setGravity(Gravity.RIGHT);
        StringBuffer seatNum = new StringBuffer();
        for(int i = 0; i < viewData.seats.size(); i++){
            seatNum.append(viewData.seats.get(i));
        }
        seats.setText(seatNum);
        seats.setEllipsize(TextUtils.TruncateAt.END);
        seats.setSingleLine();
        TextViewUtil.setTextSize(seats,tvInfoSize);
        TextViewUtil.setTextColor(seats,tvInfoColor);
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        llTicket.addView(seats,layoutParams);

        TextView tvTicketNum = new TextView(GlobalContext.get());
        tvTicketNum.setGravity(Gravity.RIGHT);
        tvTicketNum.setText("(共"+viewData.seats.size()+"张)");
        tvTicketNum.setId(ViewUtils.generateViewId());
        TextViewUtil.setTextSize(tvTicketNum,tvInfoSize);
        TextViewUtil.setTextColor(tvTicketNum,tvInfoColor);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llTicket.addView(tvTicketNum,layoutParams);

        View bottom = new View(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        lyTitle.addView(bottom,layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
        llContents.addView(divider,layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        llContents.addView(llContent,layoutParams);

        LinearLayout phoneNumContents = new LinearLayout(GlobalContext.get());
        phoneNumContents.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = phoneNumTopMargin;
        layoutParams.gravity = Gravity.CENTER;
        llContent.addView(phoneNumContents,layoutParams);

        final TextView tvPhoneNumOne = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvPhoneNumOne,tvPhoneNumSize);
        TextViewUtil.setTextColor(tvPhoneNumOne,tvPhoneNumColor);
        tvPhoneNumOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null || s.length() == 0) return;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < s.length(); i++) {
                    sb.append(s.charAt(i));
                    if ((sb.length() == 4 || sb.length() == 9) && sb.charAt(sb.length() - 1) != ' ') {
                        sb.insert(sb.length() - 1, ' ');
                    }
                }
                if (!sb.toString().equals(s.toString())) {
                    tvPhoneNumOne.setText(sb.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tvPhoneNumOne.setText(viewData.phoneNum);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvPhoneNumHeight);
        layoutParams.rightMargin = (int) (0.7 * ViewParamsUtil.unit);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        phoneNumContents.addView(tvPhoneNumOne,layoutParams);

        ImageView ivReplacePhone = new ImageView(GlobalContext.get());
        //防止多次点击多次时件回调
        final int[] state = {0};
        ivReplacePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state[0] == 0) {
                    state[0] = 1;
                    RecordWin2Manager.getInstance().operateView(
                            TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                            TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_FILM_REPLACE_PHONE, 0, 0,
                            1,viewData.replacePhoneUrl);
                }
            }
        });
        ivReplacePhone.setImageDrawable(LayouUtil.getDrawable("click_artboard"));
        layoutParams = new LinearLayout.LayoutParams(tvPhoneNumSize,tvPhoneNumSize);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        phoneNumContents.addView(ivReplacePhone,layoutParams);

        LinearLayout qrCodeContents = new LinearLayout(GlobalContext.get());
        qrCodeContents.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0,1);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        llContent.addView(qrCodeContents,layoutParams);

        final ImageView ivWXQRCode = new ImageView(GlobalContext.get());
        ivWXQRCode.setScaleType(ImageView.ScaleType.FIT_XY);
        ivWXQRCode.setBackgroundColor(0xFFFFFFFF);
        // 给二维码添加logo的方法没有白边，所以需要自己通过padding实现白边的效果。
        int padding = ViewParamsUtil.unit / 2;
        ivWXQRCode.setPadding(padding, padding, padding, padding);
        Bitmap logoVX = ((BitmapDrawable) getDrawable("qiwu_ticket_wechat_pay_icon")).getBitmap();
        int h = qrCodeSide;
        int logoH = qrCodeLogoSide;
//            final Bitmap bitmap = QRUtil.createQRCodeBitmap(viewData.WXPayURL, h,1);
        final Bitmap bitmapVX = QRUtil.createQRCodeWithLogo(viewData.WXPayURL,
                h, 1, ErrorCorrectionLevel.H, logoVX, logoH);
        UI2Manager.runOnUIThread(new Runnable() {

            @Override
            public void run() {
                if (bitmapVX == null) {
                    return;
                }
                ivWXQRCode.setImageBitmap(bitmapVX);
            }
        }, 0);
        layoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, h);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        qrCodeContents.addView(ivWXQRCode,layoutParams);

        final ImageView ivZFBQRCode = new ImageView(GlobalContext.get());
        ivZFBQRCode.setScaleType(ImageView.ScaleType.FIT_XY);
        ivZFBQRCode.setBackgroundColor(0xFFFFFFFF);
        // 给二维码添加logo的方法没有白边，所以需要自己通过padding实现白边的效果。
        padding = ViewParamsUtil.unit / 2;
        ivZFBQRCode.setPadding(padding, padding, padding, padding);
        Bitmap logoZFB = ((BitmapDrawable) getDrawable("qiwu_ticket_alipay_icon")).getBitmap();
//            final Bitmap bitmap = QRUtil.createQRCodeBitmap(viewData.ZFBPayURL, h,1);
        final Bitmap bitmapZFB = QRUtil.createQRCodeWithLogo(viewData.ZFBPayURL,
                h, 1, ErrorCorrectionLevel.H, logoZFB, logoH);
        UI2Manager.runOnUIThread(new Runnable() {

            @Override
            public void run() {
                if (bitmapZFB == null) {
                    return;
                }
                ivZFBQRCode.setImageBitmap(bitmapZFB);
            }
        }, 0);
        layoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, h);
        layoutParams.leftMargin = qrCodeInterval;
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        qrCodeContents.addView(ivZFBQRCode,layoutParams);

//        LinearLayout payNameContents = new LinearLayout(GlobalContext.get());
//        payNameContents.setOrientation(LinearLayout.HORIZONTAL);
////        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////        layoutParams.topMargin = ViewParamsUtil.unit;
////        layoutParams.bottomMargin = 2 * ViewParamsUtil.unit;
////        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
//        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0,1);
//        llContent.addView(payNameContents,layoutParams);

//        LinearLayout lyWXFlag = new LinearLayout(GlobalContext.get());
//        lyWXFlag.setId(ViewUtils.generateViewId());
//        lyWXFlag.setOrientation(LinearLayout.HORIZONTAL);
//        lyWXFlag.setBackground(LayouUtil.getDrawable("film_pay_flag_bg"));
//        lyWXFlag.setGravity(Gravity.CENTER);
//        layoutParams = new LinearLayout.LayoutParams(h,5 * ViewParamsUtil.unit);
//        payNameContents.addView(lyWXFlag,layoutParams);
//        ImageView ivWXIcon = new ImageView(GlobalContext.get());
//        ivWXIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//        layoutParams = new LinearLayout.LayoutParams(3 * ViewParamsUtil.unit,3 * ViewParamsUtil.unit);
//        ivWXIcon.setImageDrawable(LayouUtil.getDrawable("icon_vx"));
//        layoutParams.rightMargin = ViewParamsUtil.unit;
//        lyWXFlag.addView(ivWXIcon,layoutParams);
//        TextView tvWX = new TextView(GlobalContext.get());
//        TextViewUtil.setTextSize(tvWX,ViewParamsUtil.h6);
//        tvWX.setText("微信支付");
//        tvWX.setSingleLine();
//        tvWX.setTextColor(Color.WHITE);
//        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//        lyWXFlag.addView(tvWX, layoutParams);
//
//        LinearLayout lyZFBFlag = new LinearLayout(GlobalContext.get());
//        lyZFBFlag.setOrientation(LinearLayout.HORIZONTAL);
//        lyZFBFlag.setBackground(LayouUtil.getDrawable("film_pay_flag_bg"));
//        lyZFBFlag.setGravity(Gravity.CENTER);
//        layoutParams = new LinearLayout.LayoutParams(h,5 * ViewParamsUtil.unit);
//        layoutParams.leftMargin = 8 * ViewParamsUtil.unit;
//        payNameContents.addView(lyZFBFlag,layoutParams);
//        ImageView ivZFBIcon = new ImageView(GlobalContext.get());
//        ivZFBIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//        layoutParams = new LinearLayout.LayoutParams(3 * ViewParamsUtil.unit,3 * ViewParamsUtil.unit);
//        ivZFBIcon.setImageDrawable(LayouUtil.getDrawable("icon_zfb"));
//        layoutParams.rightMargin = ViewParamsUtil.unit;
//        lyZFBFlag.addView(ivZFBIcon,layoutParams);
//        TextView tvZFB = new TextView(GlobalContext.get());
//        tvZFB.setText("支付宝支付");
//        tvZFB.setSingleLine();
//        TextViewUtil.setTextSize(tvZFB,ViewParamsUtil.h6);
//        tvZFB.setTextColor(Color.WHITE);
//        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//        lyZFBFlag.addView(tvZFB, layoutParams);
        return lyContents;
    }

    private View createViewNone(ViewData data){
        final MovieWaitingPayQRViewData viewData = (MovieWaitingPayQRViewData) data;
        WinLayout.getInstance().vTips = viewData.vTips;

        LinearLayout lyContents = new LinearLayout(GlobalContext.get());
        lyContents.setGravity(Gravity.CENTER_VERTICAL);
        lyContents.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams contentsLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        lyContents.setLayoutParams(contentsLP);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        lyContents.addView(llContents,layoutParams);

        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,topHeight);
        LinearLayout lyTitle = new LinearLayout(GlobalContext.get());
        lyTitle.setOrientation(LinearLayout.VERTICAL);
        lyTitle.setGravity(Gravity.CENTER);
        lyTitle.setPadding(topHorMargin,0,topHorMargin,0);
        llContents.addView(lyTitle,layoutParams);

        View top = new View(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        lyTitle.addView(top,layoutParams);

        LinearLayout lyTitleFirstLine = new LinearLayout(GlobalContext.get());
        lyTitleFirstLine.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lyTitle.addView(lyTitleFirstLine,layoutParams);

        TextView tvMoiveName = new TextView(GlobalContext.get());
        tvMoiveName.setText(viewData.moiveName);
        tvMoiveName.setEllipsize(TextUtils.TruncateAt.END);
        tvMoiveName.setSingleLine();
        TextViewUtil.setTextSize(tvMoiveName,tvTitleSize);
        TextViewUtil.setTextColor(tvMoiveName,tvTitleColor);
        layoutParams = new LinearLayout.LayoutParams(0,tvTitleHeight,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        lyTitleFirstLine.addView(tvMoiveName,layoutParams);

        TextView tvcinemaName = new TextView(GlobalContext.get());
        tvcinemaName.setGravity(Gravity.END);
        tvcinemaName.setText(viewData.cinemaName);
        tvcinemaName.setEllipsize(TextUtils.TruncateAt.END);
        tvcinemaName.setSingleLine();
        TextViewUtil.setTextSize(tvcinemaName,tvNameSize);
        TextViewUtil.setTextColor(tvcinemaName,tvNameColor);
        layoutParams = new LinearLayout.LayoutParams(0,tvNameHeight,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        layoutParams.leftMargin = 2 * ViewParamsUtil.unit;
        lyTitleFirstLine.addView(tvcinemaName,layoutParams);

        View middle = new View(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,topVerMargin);
        lyTitle.addView(middle,layoutParams);

        LinearLayout lyTitleSecondLine = new LinearLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lyTitle.addView(lyTitleSecondLine,layoutParams);

        TextView showTime = new TextView(GlobalContext.get());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf.parse(viewData.showTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int showTimeMonth = calendar.get(Calendar.MONTH)+1;
        int showTimeDay = calendar.get(Calendar.DATE);
        int showTimeHour = calendar.get(Calendar.HOUR_OF_DAY);
        String showTimeMinute = calendar.get(Calendar.MINUTE) >= 10? String.valueOf(calendar.get(Calendar.MINUTE)): ("0"+calendar.get(Calendar.MINUTE));
        String showTimeText = showTimeMonth+"月"+showTimeDay+"日 "+showTimeHour+":"+showTimeMinute+"分 ("+viewData.showVersion+")";
        showTime.setText(showTimeText);
        showTime.setEllipsize(TextUtils.TruncateAt.END);
        showTime.setSingleLine();
        TextViewUtil.setTextSize(showTime,tvInfoSize);
        TextViewUtil.setTextColor(showTime,tvInfoColor);
        layoutParams = new LinearLayout.LayoutParams(0,tvInfoHeight,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        lyTitleSecondLine.addView(showTime,layoutParams);

        LinearLayout llTicket = new LinearLayout(GlobalContext.get());
        llTicket.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        layoutParams.leftMargin = 2 * ViewParamsUtil.unit;
        lyTitleSecondLine.addView(llTicket,layoutParams);

        TextView seats = new TextView(GlobalContext.get());
        seats.setGravity(Gravity.RIGHT);
        StringBuffer seatNum = new StringBuffer();
        for(int i = 0; i < viewData.seats.size(); i++){
            seatNum.append(viewData.seats.get(i));
        }
        seats.setText(seatNum);
        seats.setEllipsize(TextUtils.TruncateAt.END);
        seats.setSingleLine();
        TextViewUtil.setTextSize(seats,tvInfoSize);
        TextViewUtil.setTextColor(seats,tvInfoColor);
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        llTicket.addView(seats,layoutParams);

        TextView tvTicketNum = new TextView(GlobalContext.get());
        tvTicketNum.setGravity(Gravity.RIGHT);
        tvTicketNum.setText("(共"+viewData.seats.size()+"张)");
        tvTicketNum.setId(ViewUtils.generateViewId());
        TextViewUtil.setTextSize(tvTicketNum,tvInfoSize);
        TextViewUtil.setTextColor(tvTicketNum,tvInfoColor);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        llTicket.addView(tvTicketNum,layoutParams);

        View bottom = new View(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        lyTitle.addView(bottom,layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
        llContents.addView(divider,layoutParams);

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        llContents.addView(llContent,layoutParams);

        LinearLayout phoneNumContents = new LinearLayout(GlobalContext.get());
        phoneNumContents.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = phoneNumTopMargin;
        layoutParams.gravity = Gravity.CENTER;
        llContent.addView(phoneNumContents,layoutParams);

        final TextView tvPhoneNumOne = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvPhoneNumOne,tvPhoneNumSize);
        TextViewUtil.setTextColor(tvPhoneNumOne,tvPhoneNumColor);
        tvPhoneNumOne.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null || s.length() == 0) return;
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < s.length(); i++) {
                    sb.append(s.charAt(i));
                    if ((sb.length() == 4 || sb.length() == 9) && sb.charAt(sb.length() - 1) != ' ') {
                        sb.insert(sb.length() - 1, ' ');
                    }
                }
                if (!sb.toString().equals(s.toString())) {
                    tvPhoneNumOne.setText(sb.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        tvPhoneNumOne.setText(viewData.phoneNum);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,tvPhoneNumHeight);
        layoutParams.rightMargin = (int) (0.7 * ViewParamsUtil.unit);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        phoneNumContents.addView(tvPhoneNumOne,layoutParams);

        ImageView ivReplacePhone = new ImageView(GlobalContext.get());
        //防止多次点击多次时件回调
        final int[] state = {0};
        ivReplacePhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(state[0] == 0) {
                    state[0] = 1;
                    RecordWin2Manager.getInstance().operateView(
                            TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                            TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_FILM_REPLACE_PHONE, 0, 0,
                            1,viewData.replacePhoneUrl);
                }
            }
        });
        ivReplacePhone.setImageDrawable(LayouUtil.getDrawable("click_artboard"));
        layoutParams = new LinearLayout.LayoutParams(tvPhoneNumSize,tvPhoneNumSize);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        phoneNumContents.addView(ivReplacePhone,layoutParams);

        LinearLayout qrCodeContents = new LinearLayout(GlobalContext.get());
        qrCodeContents.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0,1);
        layoutParams.gravity = Gravity.CENTER;
        llContent.addView(qrCodeContents,layoutParams);

        final ImageView ivWXQRCode = new ImageView(GlobalContext.get());
        ivWXQRCode.setScaleType(ImageView.ScaleType.FIT_XY);
        ivWXQRCode.setBackgroundColor(0xFFFFFFFF);
        // 给二维码添加logo的方法没有白边，所以需要自己通过padding实现白边的效果。
        int padding = ViewParamsUtil.unit / 2;
        ivWXQRCode.setPadding(padding, padding, padding, padding);
        Bitmap logoVX = ((BitmapDrawable) getDrawable("qiwu_ticket_wechat_pay_icon")).getBitmap();
        int h = qrCodeSide;
        int logoH = qrCodeLogoSide;
//            final Bitmap bitmap = QRUtil.createQRCodeBitmap(viewData.WXPayURL, h,1);
        final Bitmap bitmapVX = QRUtil.createQRCodeWithLogo(viewData.WXPayURL,
                h, 1, ErrorCorrectionLevel.H, logoVX, logoH);
        UI2Manager.runOnUIThread(new Runnable() {

            @Override
            public void run() {
                if (bitmapVX == null) {
                    return;
                }
                ivWXQRCode.setImageBitmap(bitmapVX);
            }
        }, 0);
        layoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, h);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        qrCodeContents.addView(ivWXQRCode,layoutParams);

        final ImageView ivZFBQRCode = new ImageView(GlobalContext.get());
        ivZFBQRCode.setScaleType(ImageView.ScaleType.FIT_XY);
        ivZFBQRCode.setBackgroundColor(0xFFFFFFFF);
        // 给二维码添加logo的方法没有白边，所以需要自己通过padding实现白边的效果。
        padding = ViewParamsUtil.unit / 2;
        ivZFBQRCode.setPadding(padding, padding, padding, padding);
        Bitmap logoZFB = ((BitmapDrawable) getDrawable("qiwu_ticket_alipay_icon")).getBitmap();
//            final Bitmap bitmap = QRUtil.createQRCodeBitmap(viewData.ZFBPayURL, h,1);
        final Bitmap bitmapZFB = QRUtil.createQRCodeWithLogo(viewData.ZFBPayURL,
                h, 1, ErrorCorrectionLevel.H, logoZFB, logoH);
        UI2Manager.runOnUIThread(new Runnable() {

            @Override
            public void run() {
                if (bitmapZFB == null) {
                    return;
                }
                ivZFBQRCode.setImageBitmap(bitmapZFB);
            }
        }, 0);
        layoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, h);
        layoutParams.leftMargin = qrCodeInterval;
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        qrCodeContents.addView(ivZFBQRCode,layoutParams);

//        LinearLayout payNameContents = new LinearLayout(GlobalContext.get());
//        payNameContents.setOrientation(LinearLayout.HORIZONTAL);
////        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
////        layoutParams.topMargin = ViewParamsUtil.unit;
////        layoutParams.bottomMargin = 2 * ViewParamsUtil.unit;
////        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
//        layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 0,1);
//        llContent.addView(payNameContents,layoutParams);

//        LinearLayout lyWXFlag = new LinearLayout(GlobalContext.get());
//        lyWXFlag.setId(ViewUtils.generateViewId());
//        lyWXFlag.setOrientation(LinearLayout.HORIZONTAL);
//        lyWXFlag.setBackground(LayouUtil.getDrawable("film_pay_flag_bg"));
//        lyWXFlag.setGravity(Gravity.CENTER);
//        layoutParams = new LinearLayout.LayoutParams(h,5 * ViewParamsUtil.unit);
//        payNameContents.addView(lyWXFlag,layoutParams);
//        ImageView ivWXIcon = new ImageView(GlobalContext.get());
//        ivWXIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//        layoutParams = new LinearLayout.LayoutParams(3 * ViewParamsUtil.unit,3 * ViewParamsUtil.unit);
//        ivWXIcon.setImageDrawable(LayouUtil.getDrawable("icon_vx"));
//        layoutParams.rightMargin = ViewParamsUtil.unit;
//        lyWXFlag.addView(ivWXIcon,layoutParams);
//        TextView tvWX = new TextView(GlobalContext.get());
//        TextViewUtil.setTextSize(tvWX,ViewParamsUtil.h6);
//        tvWX.setText("微信支付");
//        tvWX.setSingleLine();
//        tvWX.setTextColor(Color.WHITE);
//        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//        lyWXFlag.addView(tvWX, layoutParams);
//
//        LinearLayout lyZFBFlag = new LinearLayout(GlobalContext.get());
//        lyZFBFlag.setOrientation(LinearLayout.HORIZONTAL);
//        lyZFBFlag.setBackground(LayouUtil.getDrawable("film_pay_flag_bg"));
//        lyZFBFlag.setGravity(Gravity.CENTER);
//        layoutParams = new LinearLayout.LayoutParams(h,5 * ViewParamsUtil.unit);
//        layoutParams.leftMargin = 8 * ViewParamsUtil.unit;
//        payNameContents.addView(lyZFBFlag,layoutParams);
//        ImageView ivZFBIcon = new ImageView(GlobalContext.get());
//        ivZFBIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
//        layoutParams = new LinearLayout.LayoutParams(3 * ViewParamsUtil.unit,3 * ViewParamsUtil.unit);
//        ivZFBIcon.setImageDrawable(LayouUtil.getDrawable("icon_zfb"));
//        layoutParams.rightMargin = ViewParamsUtil.unit;
//        lyZFBFlag.addView(ivZFBIcon,layoutParams);
//        TextView tvZFB = new TextView(GlobalContext.get());
//        tvZFB.setText("支付宝支付");
//        tvZFB.setSingleLine();
//        TextViewUtil.setTextSize(tvZFB,ViewParamsUtil.h6);
//        tvZFB.setTextColor(Color.WHITE);
//        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
//        lyZFBFlag.addView(tvZFB, layoutParams);
        return lyContents;
    }

}
