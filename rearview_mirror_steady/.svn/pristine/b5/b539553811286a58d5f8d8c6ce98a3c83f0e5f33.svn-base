package com.txznet.comm.ui.viewfactory.view.defaults;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.IMovieWaitingPayQRView;
import com.txznet.comm.ui.viewfactory.data.MovieWaitingPayQRViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.txz.util.QRUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DefaultMovieWaitingPayQRView  extends IMovieWaitingPayQRView {

    private static DefaultMovieWaitingPayQRView sInstance = new DefaultMovieWaitingPayQRView();

    private DefaultMovieWaitingPayQRView(){}

    public static DefaultMovieWaitingPayQRView getInstance(){
        return sInstance;
    }

    @SuppressLint("NewApi")
    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        final MovieWaitingPayQRViewData viewData = (MovieWaitingPayQRViewData) data;
        LinearLayout lyContents = new LinearLayout(GlobalContext.get());
        lyContents.setOrientation(LinearLayout.VERTICAL);
        lyContents.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        LinearLayout.LayoutParams contentsLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        lyContents.setLayoutParams(contentsLP);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        /*TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setText("扫码支付");
        tvTitle.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvTitle,LayouUtil.getDimen("m23"));
        tvTitle.setTextColor(Color.WHITE);
        lyContents.addView(tvTitle,layoutParams);*/

        LinearLayout lyTitle = new LinearLayout(GlobalContext.get());
        lyTitle.setOrientation(LinearLayout.VERTICAL);
        lyTitle.setGravity(Gravity.CENTER);
        lyContents.addView(lyTitle,layoutParams);
        LinearLayout lyTitleFirstLine = new LinearLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.topMargin = (int)LayouUtil.getDimen("y10");
        layoutParams.bottomMargin = (int)LayouUtil.getDimen("y16");
        lyTitle.addView(lyTitleFirstLine,layoutParams);
        TextView tvMoiveName = new TextView(GlobalContext.get());
        tvMoiveName.setText(viewData.moiveName);
        tvMoiveName.setTextColor(Color.WHITE);
        tvMoiveName.setEllipsize(TextUtils.TruncateAt.END);
        tvMoiveName.setSingleLine();
        TextViewUtil.setTextSize(tvMoiveName,LayouUtil.getDimen("m23"));
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        layoutParams.leftMargin = (int)LayouUtil.getDimen("m32");
        lyTitleFirstLine.addView(tvMoiveName,layoutParams);
        LinearLayout lyEmpty = new LinearLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(20,LinearLayout.LayoutParams.WRAP_CONTENT);
        lyTitleFirstLine.addView(lyEmpty,layoutParams);

        RelativeLayout rlCinemaName = new RelativeLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        lyTitleFirstLine.addView(rlCinemaName,layoutParams);
        layoutParams.rightMargin = (int)LayouUtil.getDimen("m32");
        TextView tvcinemaName = new TextView(GlobalContext.get());
        tvcinemaName.setText(viewData.cinemaName);
        tvcinemaName.setTextColor(Color.WHITE);
        tvcinemaName.setEllipsize(TextUtils.TruncateAt.END);
        tvcinemaName.setSingleLine();
        RelativeLayout.LayoutParams mRelativeLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        mRelativeLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlCinemaName.addView(tvcinemaName,mRelativeLayout);
        TextViewUtil.setTextSize(tvcinemaName,LayouUtil.getDimen("m19"));

        LinearLayout lyTitleSecondLine = new LinearLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = (int)LayouUtil.getDimen("y10");
        lyTitle.addView(lyTitleSecondLine,layoutParams);

        TextView showTime = new TextView(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        layoutParams.leftMargin = (int)LayouUtil.getDimen("m32");
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
        showTime.setTextColor(Color.WHITE);
        showTime.setEllipsize(TextUtils.TruncateAt.END);
        showTime.setSingleLine();
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        layoutParams.leftMargin = (int)LayouUtil.getDimen("m32");
        lyTitleSecondLine.addView(showTime,layoutParams);
        TextViewUtil.setTextSize(showTime,LayouUtil.getDimen("m19"));

        LinearLayout Empty = new LinearLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(20,LinearLayout.LayoutParams.WRAP_CONTENT);
        lyTitleSecondLine.addView(Empty,layoutParams);

        RelativeLayout rlTicket = new RelativeLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        layoutParams.rightMargin = (int)LayouUtil.getDimen("m32");
        lyTitleSecondLine.addView(rlTicket,layoutParams);
        TextView tvTicketNum = new TextView(GlobalContext.get());
        tvTicketNum.setText("(共"+viewData.seats.size()+"张)");
        tvTicketNum.setId(ViewUtils.generateViewId());
        tvTicketNum.setTextColor(Color.WHITE);
        TextViewUtil.setTextSize(tvTicketNum,LayouUtil.getDimen("m19"));
        mRelativeLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        mRelativeLayout.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlTicket.addView(tvTicketNum,mRelativeLayout);

        TextView seats = new TextView(GlobalContext.get());
        StringBuffer seatNum = new StringBuffer();
        for(int i = 0; i < viewData.seats.size(); i++){
            seatNum.append(viewData.seats.get(i));
        }
        seats.setText(seatNum);
        seats.setTextColor(Color.WHITE);
        seats.setEllipsize(TextUtils.TruncateAt.END);
        seats.setSingleLine();
        TextViewUtil.setTextSize(seats,LayouUtil.getDimen("m19"));
        mRelativeLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        mRelativeLayout.addRule(RelativeLayout.START_OF,tvTicketNum.getId());
        rlTicket.addView(seats,mRelativeLayout);


        RelativeLayout rlContents = new RelativeLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,2.3f);
        lyContents.addView(rlContents,layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("divider_h"));
        RelativeLayout.LayoutParams rlLayParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,1);
        rlLayParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rlContents.addView(divider,rlLayParams);

        LinearLayout lyContent = new LinearLayout(GlobalContext.get());
        lyContent.setOrientation(LinearLayout.VERTICAL);
        lyContent.setGravity(Gravity.CENTER);
        rlLayParams = new RelativeLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        rlLayParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        rlContents.addView(lyContent,rlLayParams);

        LinearLayout phoneNumContents = new LinearLayout(GlobalContext.get());
        phoneNumContents.setPadding(0,(int)LayouUtil.getDimen("y18"),0,(int)LayouUtil.getDimen("y18"));
        phoneNumContents.setOrientation(LinearLayout.HORIZONTAL);
        phoneNumContents.setGravity(Gravity.CENTER);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

        lyContent.addView(phoneNumContents,layoutParams);

        final TextView tvPhoneNumOne = new TextView(GlobalContext.get());
        tvPhoneNumOne.setTextColor(Color.parseColor("#16CFFF"));
        TextViewUtil.setTextSize(tvPhoneNumOne,LayouUtil.getDimen("m25"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.rightMargin = (int) LayouUtil.getDimen("m6");
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
        layoutParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("m36"),(int) LayouUtil.getDimen("m36"));
        phoneNumContents.addView(ivReplacePhone,layoutParams);

        RelativeLayout rlQRcontents = new RelativeLayout(GlobalContext.get());
        layoutParams = new LinearLayout.LayoutParams((int)LayouUtil.getDimen("x375"), LinearLayout.LayoutParams.WRAP_CONTENT);
        lyContent.addView(rlQRcontents,layoutParams);
        final ImageView ivWXQRCode = new ImageView(GlobalContext.get());
        ivWXQRCode.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int h = (int) LayouUtil.getDimen("m157");
        try {
            final Bitmap bitmap = QRUtil.createQRCodeBitmap(viewData.WXPayURL, h,1);
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (bitmap == null) {
                        return;
                    }
                    ivWXQRCode.setImageBitmap(bitmap);
                }
            }, 0);
        } catch (WriterException ignored) {
        }
        RelativeLayout.LayoutParams rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ivWXQRCode.setId(ViewUtils.generateViewId());
        rlQRcontents.addView(ivWXQRCode,rlParams);

        LinearLayout lyWXFlag = new LinearLayout(GlobalContext.get());
        lyWXFlag.setId(ViewUtils.generateViewId());
        lyWXFlag.setOrientation(LinearLayout.HORIZONTAL);
        rlParams = new RelativeLayout.LayoutParams(h,RelativeLayout.LayoutParams.WRAP_CONTENT);
        lyWXFlag.setPadding(0,(int)LayouUtil.getDimen("y6"),0,(int)LayouUtil.getDimen("y6"));
        rlParams.topMargin = (int) LayouUtil.getDimen("y18");
        rlParams.addRule(RelativeLayout.BELOW, ivWXQRCode.getId());
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lyWXFlag.setBackground(LayouUtil.getDrawable("film_pay_flag_bg"));
        lyWXFlag.setGravity(Gravity.CENTER);
        rlQRcontents.addView(lyWXFlag,rlParams);
        ImageView ivWXIcon = new ImageView(GlobalContext.get());
        ivWXIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        layoutParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("m24"),(int) LayouUtil.getDimen("m24"));
        ivWXIcon.setImageDrawable(LayouUtil.getDrawable("icon_vx"));
        layoutParams.rightMargin = (int) LayouUtil.getDimen("x10");
        lyWXFlag.addView(ivWXIcon,layoutParams);
        TextView tvWX = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvWX,LayouUtil.getDimen("m18"));
        tvWX.setText("微信支付");
        tvWX.setTextColor(Color.WHITE);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lyWXFlag.addView(tvWX, layoutParams);

        final ImageView ivZFBQRCode = new ImageView(GlobalContext.get());
        ivZFBQRCode.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        try {
            final Bitmap bitmap = QRUtil.createQRCodeBitmap(viewData.ZFBPayURL, h,1);
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (bitmap == null) {
                        return;
                    }
                    ivZFBQRCode.setImageBitmap(bitmap);
                }
            }, 0);
        } catch (WriterException ignored) {
        }
        rlParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ivZFBQRCode.setId(ViewUtils.generateViewId());
        rlQRcontents.addView(ivZFBQRCode,rlParams);

        LinearLayout lyZFBFlag = new LinearLayout(GlobalContext.get());
        lyZFBFlag.setId(ViewUtils.generateViewId());
        lyZFBFlag.setOrientation(LinearLayout.HORIZONTAL);
        rlParams = new RelativeLayout.LayoutParams(h,RelativeLayout.LayoutParams.WRAP_CONTENT);
        lyZFBFlag.setPadding(0,(int)LayouUtil.getDimen("y6"),0,(int)LayouUtil.getDimen("y6"));
        rlParams.topMargin = (int) LayouUtil.getDimen("y18");
        rlParams.addRule(RelativeLayout.BELOW, ivZFBQRCode.getId());
        rlParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlParams.addRule(RelativeLayout.ALIGN_TOP,ivWXIcon.getId());
        lyZFBFlag.setBackground(LayouUtil.getDrawable("film_pay_flag_bg"));
        lyZFBFlag.setGravity(Gravity.CENTER);
        rlQRcontents.addView(lyZFBFlag,rlParams);
        ImageView ivZFBIcon = new ImageView(GlobalContext.get());
        ivZFBIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        layoutParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("m24"),(int) LayouUtil.getDimen("m24"));
        ivZFBIcon.setImageDrawable(LayouUtil.getDrawable("icon_zfb"));
        layoutParams.rightMargin = (int) LayouUtil.getDimen("x10");
        lyZFBFlag.addView(ivZFBIcon,layoutParams);
        TextView tvZFB = new TextView(GlobalContext.get());
        TextViewUtil.setTextSize(tvZFB,LayouUtil.getDimen("m18"));
        tvZFB.setText("支付宝支付");
        tvZFB.setTextColor(Color.WHITE);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lyZFBFlag.addView(tvZFB, layoutParams);
        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = lyContents;
        viewAdapter.object = DefaultMovieWaitingPayQRView.getInstance();
        return viewAdapter;
    }

}