package com.txznet.txz.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.util.TXZFileConfigUtil;

import org.json.JSONException;
import org.json.JSONObject;

public class TicketWaitingDialog extends WinDialog {

    public enum TicketType{
        Train,
        Flight
    }


    private Context mContext;

    private boolean ticketType = false;

    private TextView nameTv;//乘车乘机人标题
    private ProgressBar namePB;
    private ImageView ivName;
    private ProgressBar stationPB;
    private ImageView ivStation;
    private ProgressBar noPB;
    private ImageView ivNo;
    private ProgressBar dataPB;
    private ImageView ivData;
    private ProgressBar seatPB;
    private TextView tvNo;
    private ImageView ivSeat;
    private TextView checkTv;
    private TextView tvName;
    private TextView tvSeat;
    private ImageView view;
    private TextView tvData;
    private TextView tvTime;
    private TextView tvArrStation;
    private TextView tvDepSation;
    private JSONObject userInfo;

    public void setUserInfo(JSONObject userInfo){
        this.userInfo = userInfo;
        try {
            tvName.setText(userInfo.getString("name"));
            tvSeat.setText(userInfo.getString("seatName"));
            tvData.setText(userInfo.getJSONObject("ticketInfoJson").getString("departDate"));
            tvTime.setText(userInfo.getJSONObject("ticketInfoJson").getString("departureTime"));
            tvNo.setText(userInfo.getJSONObject("ticketInfoJson").getString("ticketNum"));
            tvDepSation.setText(userInfo.getJSONObject("ticketInfoJson").getString("departureCity"));
            tvArrStation.setText(userInfo.getJSONObject("ticketInfoJson").getString("arrivalCity"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setTicketType(TicketType type){
        switch (type){
            case Train: ticketType = false;
                nameTv.setText("乘车人");
                view.setImageDrawable(LayouUtil.getDrawable("trainlines_to4"));
                checkTv.setText("确认列车信息");
                break;
            case Flight: ticketType = true;
                nameTv.setText("乘机人");
                checkTv.setText("确认航班信息");
                view.setImageDrawable(LayouUtil.getDrawable("airlines_to4"));
                break;
        }
    }


    private static TicketWaitingDialog instance = new TicketWaitingDialog();

    public static TicketWaitingDialog getInstance() {

        return instance;
    }

    public TicketWaitingDialog() {
        super(true);
        setCanceledOnTouchOutside(false);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        setOnDismissListener(new OnDismissListener(){
            @Override
            public void onDismiss(DialogInterface dialog) {
                namePB.setVisibility(View.VISIBLE);
                ivName.setVisibility(View.GONE);
                stationPB.setVisibility(View.VISIBLE);
                ivStation.setVisibility(View.GONE);
                noPB.setVisibility(View.VISIBLE);
                ivNo.setVisibility(View.GONE);
                dataPB.setVisibility(View.VISIBLE);
                ivData.setVisibility(View.GONE);
                AppLogicBase.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        TicketUseInfoDialog.getInstance().setTvSearch();
                    }
                });
                AppLogicBase.removeUiGroundCallback(outTime);
            }
        });
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if((keyCode == KeyEvent.KEYCODE_BACK) && event.getRepeatCount()==0)
                {
                    if(instance.isShowing()){
                        TicketUseInfoDialog.getInstance().setAfterDissmiss(null);
                        TicketUseInfoDialog.getInstance().dismiss();
                        instance.dismiss();
                        AsrUtil.cancel();
                    }
                }
                return false;
            }
        });
        mView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private HomeObservable.HomeObserver mHomeObserver = new HomeObservable.HomeObserver() {
        @Override
        public void onHomePressed() {
            if(instance.isShowing()){
                TicketUseInfoDialog.getInstance().setAfterDissmiss(null);
                TicketUseInfoDialog.getInstance().dismiss();
                instance.dismiss();
                AsrUtil.cancel();
            }
        }
    };

    @Override
    protected View createView() {
        return initView();
    }

    @SuppressLint("NewApi")
    private View initView() {
        mContext = GlobalContext.get();
        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        //w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        lp.width = (int) LayouUtil.getDimen("m211");
        lp.height = (int) LayouUtil.getDimen("m211");
        RelativeLayout fullContent = new RelativeLayout(mContext);
        /*fullContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });*/
        fullContent.setBackground(LayouUtil.getDrawable("click_off"));
        RelativeLayout.LayoutParams ryParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ryParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout rlContents = new RelativeLayout(mContext);
        rlContents.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        rlContents.setPadding(0, (int) LayouUtil.getDimen("y32"), 0, (int) LayouUtil.getDimen("y32"));
        fullContent.addView(rlContents,ryParams);
        LinearLayout lyContents = new LinearLayout(mContext);
        ryParams= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ryParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        ryParams.rightMargin = (int) LayouUtil.getDimen("x32");
        ryParams.leftMargin = (int) LayouUtil.getDimen("x32");
        lyContents.setOrientation(LinearLayout.VERTICAL);
        rlContents.addView(lyContents,ryParams);

        TextView title  = new TextView(mContext);
        title.setTextColor(Color.WHITE);
        title.setText("出票中...");
        title.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(title, (int) LayouUtil.getDimen("m24"));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = (int) LayouUtil.getDimen("y24");
        lyContents.addView(title, layoutParams);

        LinearLayout userNameLy = new LinearLayout(mContext);
        userNameLy.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = (int) LayouUtil.getDimen("y8");
        lyContents.addView(userNameLy, layoutParams);
        nameTv = new TextView(mContext);
        nameTv.setTextColor(Color.parseColor("#646464"));
        TextViewUtil.setTextSize(nameTv, (int) LayouUtil.getDimen("m20"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        userNameLy.addView(nameTv, layoutParams);

        RelativeLayout rlName = new RelativeLayout(mContext);
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x399"), RelativeLayout.LayoutParams.WRAP_CONTENT);
        ryParams.leftMargin = (int) LayouUtil.getDimen("x47");
        ryParams.rightMargin = 1;
        ryParams.bottomMargin = (int) LayouUtil.getDimen("y16");
        lyContents.addView(rlName, ryParams);

        tvName = new TextView(mContext);
        TextViewUtil.setTextSize(tvName, (int) LayouUtil.getDimen("m24"));
        ryParams.leftMargin = (int) LayouUtil.getDimen("x16");
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlName.addView(tvName, ryParams);

        namePB= new ProgressBar(mContext);
        namePB.setIndeterminateDrawable(LayouUtil.getDrawable("progressbar"));
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m29"), (int) LayouUtil.getDimen("m29"));
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        namePB.setVisibility(View.VISIBLE);
        rlName.addView(namePB, ryParams);

        ivName= new ImageView(mContext);
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m29"), (int) LayouUtil.getDimen("m29"));
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivName.setVisibility(View.GONE);
        ivName.setImageDrawable(LayouUtil.getDrawable("icon_success_01"));
        ivName.setScaleType(ImageView.ScaleType.FIT_XY);
        rlName.addView(ivName, ryParams);



        LinearLayout checkInfoLy = new LinearLayout(mContext);
        checkInfoLy.setOrientation(LinearLayout.HORIZONTAL);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.bottomMargin = (int) LayouUtil.getDimen("y8");
        lyContents.addView(checkInfoLy, layoutParams);
        checkTv = new TextView(mContext);
        checkTv.setTextColor(Color.parseColor("#646464"));
        TextViewUtil.setTextSize(checkTv, (int) LayouUtil.getDimen("m20"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        checkInfoLy.addView(checkTv, layoutParams);
        RelativeLayout rlStation = new RelativeLayout(mContext);
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x399"), RelativeLayout.LayoutParams.WRAP_CONTENT);
        ryParams.rightMargin = 1;
        lyContents.addView(rlStation, ryParams);

        LinearLayout lyStation = new LinearLayout(mContext);
        lyStation.setOrientation(LinearLayout.HORIZONTAL);
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        ryParams.leftMargin = (int) LayouUtil.getDimen("x16");
        ryParams.bottomMargin = (int) LayouUtil.getDimen("y8");
        rlStation.addView(lyStation, ryParams);

        tvDepSation = new TextView(mContext);
        TextViewUtil.setTextSize(tvDepSation, (int) LayouUtil.getDimen("m24"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lyStation.addView(tvDepSation, layoutParams);

        view = new ImageView(GlobalContext.get());

        layoutParams = new LinearLayout.LayoutParams((int) LayouUtil.getDimen("x66"),(int) LayouUtil.getDimen("y32"));
        layoutParams.leftMargin = (int) LayouUtil.getDimen("x12");
        layoutParams.rightMargin = (int) LayouUtil.getDimen("x12");
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        lyStation.addView(view, layoutParams);

        tvArrStation = new TextView(mContext);
        TextViewUtil.setTextSize(tvArrStation, (int) LayouUtil.getDimen("m24"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lyStation.addView(tvArrStation, layoutParams);

        stationPB= new ProgressBar(mContext);
        stationPB.setIndeterminateDrawable(LayouUtil.getDrawable("progressbar"));
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m29"), (int) LayouUtil.getDimen("m29"));
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        stationPB.setVisibility(View.VISIBLE);
        rlStation.addView(stationPB, ryParams);

        ivStation= new ImageView(mContext);
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m29"), (int) LayouUtil.getDimen("m29"));
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivStation.setVisibility(View.GONE);
        ivStation.setImageDrawable(LayouUtil.getDrawable("icon_success_01"));
        ivStation.setScaleType(ImageView.ScaleType.FIT_XY);
        rlStation.addView(ivStation, ryParams);

        RelativeLayout rlNo = new RelativeLayout(mContext);
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x399"), RelativeLayout.LayoutParams.WRAP_CONTENT);
        ryParams.bottomMargin = (int) LayouUtil.getDimen("y8");
        ryParams.rightMargin = 1;
        lyContents.addView(rlNo, ryParams);

        tvNo = new TextView(mContext);
        TextViewUtil.setTextSize(tvNo, (int) LayouUtil.getDimen("m24"));
        ryParams.leftMargin = (int) LayouUtil.getDimen("x16");
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlNo.addView(tvNo, ryParams);

        noPB= new ProgressBar(mContext);
        noPB.setIndeterminateDrawable(LayouUtil.getDrawable("progressbar"));
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m29"), (int) LayouUtil.getDimen("m29"));
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        noPB.setVisibility(View.VISIBLE);
        rlNo.addView(noPB, ryParams);

        ivNo= new ImageView(mContext);
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m29"), (int) LayouUtil.getDimen("m29"));
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivNo.setVisibility(View.GONE);
        ivNo.setImageDrawable(LayouUtil.getDrawable("icon_success_01"));
        ivNo.setScaleType(ImageView.ScaleType.FIT_XY);
        rlNo.addView(ivNo, ryParams);

        RelativeLayout rlData = new RelativeLayout(mContext);
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x399"), RelativeLayout.LayoutParams.WRAP_CONTENT);
        ryParams.bottomMargin = (int) LayouUtil.getDimen("y8");
        ryParams.rightMargin = 1;
        lyContents.addView(rlData, ryParams);

        LinearLayout lyData = new LinearLayout(mContext);
        lyData.setOrientation(LinearLayout.HORIZONTAL);
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        ryParams.leftMargin = (int) LayouUtil.getDimen("x16");
        ryParams.bottomMargin = (int) LayouUtil.getDimen("y8");
        rlData.addView(lyData, ryParams);

        tvData = new TextView(mContext);
        tvData.setId(ViewUtils.generateViewId());
        TextViewUtil.setTextSize(tvData, (int) LayouUtil.getDimen("m24"));
        LinearLayout.LayoutParams lyparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lyData.addView(tvData, lyparams);

        tvTime = new TextView(mContext);
        tvTime.setId(ViewUtils.generateViewId());
        TextViewUtil.setTextSize(tvTime, (int) LayouUtil.getDimen("m24"));
        LinearLayout.LayoutParams lyparams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lyparams2.leftMargin = (int) LayouUtil.getDimen("x5");
        lyData.addView(tvTime, lyparams2);

        dataPB= new ProgressBar(mContext);
        dataPB.setIndeterminateDrawable(LayouUtil.getDrawable("progressbar"));
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m29"), (int) LayouUtil.getDimen("m29"));
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        dataPB.setVisibility(View.VISIBLE);
        rlData.addView(dataPB, ryParams);

        ivData= new ImageView(mContext);
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m29"), (int) LayouUtil.getDimen("m29"));
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivData.setVisibility(View.GONE);
        ivData.setImageDrawable(LayouUtil.getDrawable("icon_success_01"));
        ivData.setScaleType(ImageView.ScaleType.FIT_XY);
        rlData.addView(ivData, ryParams);

        RelativeLayout rlSeat = new RelativeLayout(mContext);
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("x399"), RelativeLayout.LayoutParams.WRAP_CONTENT);
        ryParams.bottomMargin = (int) LayouUtil.getDimen("y8");
        ryParams.rightMargin = 1;
        lyContents.addView(rlSeat, ryParams);

        tvSeat = new TextView(mContext);
        TextViewUtil.setTextSize(tvSeat, (int) LayouUtil.getDimen("m24"));
        ryParams.leftMargin = (int) LayouUtil.getDimen("x16");
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlSeat.addView(tvSeat, ryParams);

        seatPB= new ProgressBar(mContext);
        seatPB.setIndeterminateDrawable(LayouUtil.getDrawable("progressbar"));
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m29"), (int) LayouUtil.getDimen("m29"));
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        seatPB.setVisibility(View.VISIBLE);
        rlSeat.addView(seatPB, ryParams);

        ivSeat= new ImageView(mContext);
        ryParams = new RelativeLayout.LayoutParams((int) LayouUtil.getDimen("m29"), (int) LayouUtil.getDimen("m29"));
        ryParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ivSeat.setVisibility(View.GONE);
        ivSeat.setImageDrawable(LayouUtil.getDrawable("icon_success_01"));
        ivSeat.setScaleType(ImageView.ScaleType.FIT_XY);
        rlSeat.addView(ivSeat, ryParams);

        fullContent.getBackground().setAlpha(200);
        rlContents.getBackground().setAlpha(230);
        return fullContent;
    }

    private long checkTime = 1200;

    @Override
    public void show() {
        super.show();
        AppLogicBase.runOnUiGround(checkNamePB, checkTime);
        AppLogicBase.runOnUiGround(checkStationPB, checkTime * 2);
        AppLogicBase.runOnUiGround(checkNoPB, checkTime * 3);
        AppLogicBase.runOnUiGround(checkDataPB, checkTime * 4);
        AppLogicBase.runOnUiGround(outTime ,62000);//与稍微比后台多一点，后台是六十秒
        String[] wakeupKeywords_Sdk = WakeupManager.getInstance().getWakeupKeywords_Sdk();
        String[] wakeupKeywords_User = WakeupManager.getInstance().getWakeupKeywords_User();
        int sdkSize = wakeupKeywords_Sdk == null?0:wakeupKeywords_Sdk.length;
        int useSize = wakeupKeywords_User == null?0:wakeupKeywords_User.length;
        String[] wakeupKeywordS = new String[sdkSize + useSize];
        int index = 0;
        for(int i = 0; wakeupKeywords_Sdk != null && i <  wakeupKeywords_Sdk.length; i++){
            wakeupKeywordS[index++] = wakeupKeywords_Sdk[i];
        }
        for(int i = 0; wakeupKeywords_User != null && i <  wakeupKeywords_User.length; i++){
            wakeupKeywordS[index++] = wakeupKeywords_User[i];
        }
        GlobalObservableSupport.getHomeObservable().registerObserver(mHomeObserver);
    }

    @Override
    public void dismiss(){
        if(!isShowing()){
            return;
        }
        //优先反注册home监听对象，确保线程安全
        try{
            GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeObserver);
        }catch (Exception ignored){}
        super.dismiss();
    }

    private TicketUseInfoDialog.AfterDissmiss outTimeAction = new TicketUseInfoDialog.AfterDissmiss() {
        @Override
        public void afterDissmiss() {
            TicketUseInfoDialog.getInstance().useInfoWake();
        }
    };

    Runnable outTime = new Runnable() {
        @Override
        public void run() {
            dismiss();
            TicketUseInfoDialog.mSpeechTaskId = TXZTtsManager.getInstance().speakText("出票失败，请重试。");
            outTimeAction.afterDissmiss();
        }
    };


    private Runnable checkNamePB = new Runnable() {
        @Override
        public void run() {
                namePB.setVisibility(View.GONE);
                ivName.setVisibility(View.VISIBLE);

        }
    };

    private Runnable checkStationPB = new Runnable() {
        @Override
        public void run() {
            stationPB.setVisibility(View.GONE);
            ivStation.setVisibility(View.VISIBLE);

        }
    };

    private Runnable checkNoPB = new Runnable() {
        @Override
        public void run() {

            noPB.setVisibility(View.GONE);
            ivNo.setVisibility(View.VISIBLE);

        }
    };

    private Runnable checkDataPB = new Runnable() {
        @Override
        public void run() {
            dataPB.setVisibility(View.GONE);
            ivData.setVisibility(View.VISIBLE);
        }
    };

    }
