package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZRecordWinManager;

public class PageView extends LinearLayout  {

    private ImageView ivPrePager;
    private TextView tvCurPager;
    private ImageView ivNextPager;

    private int mMaxPage;
    private int mCurPage;

    private float tvCurPagerSize;

    public PageView(Context context) {
        super(context);
    }

    public PageView(Context context,int mCurPage,int mMaxPage) {
        super(context);
        this.mCurPage = mCurPage;
        this.mMaxPage = mMaxPage < 1 ?1:mMaxPage;

        initView();
    }

    //设置页数
    public void updatePage(int mCurPage,int mMaxPage){
        this.mCurPage = mCurPage;
        this.mMaxPage = mMaxPage < 1 ?1:mMaxPage;
        destoryView();
        initView();
    }

    private void initView(){

        if (WinLayout.isVertScreen){
            //tvCurPagerSize = LayouUtil.getDimen("x20");
            tvCurPagerSize = SizeConfig.screenWidth/40;
        }else {
            //tvCurPagerSize = LayouUtil.getDimen("y16");
            tvCurPagerSize = SizeConfig.screenHeight/30;
        }
        setOrientation(LinearLayout.VERTICAL);

        FrameLayout flPre = new FrameLayout(GlobalContext.get());
        ivPrePager = new ImageView(GlobalContext.get());
        FrameLayout.LayoutParams flLayoutParams = new FrameLayout.LayoutParams(SizeConfig.pageButtonSize,SizeConfig.pageButtonSize);
        flLayoutParams.gravity = Gravity.CENTER;
        flPre.addView(ivPrePager,flLayoutParams);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        addViewInLayout(flPre,0,layoutParams);

        tvCurPager = new TextView(GlobalContext.get());
        tvCurPager.setGravity(Gravity.CENTER);
        layoutParams = new LinearLayout.LayoutParams(SizeConfig.pageWidth,SizeConfig.pageTextHeight);
        addViewInLayout(tvCurPager,1,layoutParams);

        FrameLayout flNext = new FrameLayout(GlobalContext.get());
        ivNextPager = new ImageView(GlobalContext.get());
        flLayoutParams = new FrameLayout.LayoutParams(SizeConfig.pageButtonSize,SizeConfig.pageButtonSize);
        flLayoutParams.gravity = Gravity.CENTER;
        flNext.addView(ivNextPager,flLayoutParams);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        addViewInLayout(flNext,2,layoutParams);

        TextViewUtil.setTextSize(tvCurPager,tvCurPagerSize);

        flPre.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK, TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_LIST_PREPAGE, 0, 0);
            }
        });
        flNext.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK, TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_LIST_NEXTPAGE, 0, 0);
            }
        });

        if (mMaxPage != 0 && mMaxPage != -1 /*&& mMaxPage > 1*/) {
            setVisibility(View.VISIBLE);
            if (mCurPage == 0) {
                ivPrePager.setImageDrawable(LayouUtil.getDrawable("prev_btn"));
                ivPrePager.setAlpha(0.3f);
            } else {
                ivPrePager.setImageDrawable(LayouUtil.getDrawable("prev_btn"));
        }
            if (mCurPage == mMaxPage - 1) {
                ivNextPager.setImageDrawable(LayouUtil.getDrawable("next_btn"));
                ivNextPager.setAlpha(0.3f);
            } else {
                ivNextPager.setImageDrawable(LayouUtil.getDrawable("next_btn"));
            }
            if(mMaxPage == 1){
                ivPrePager.setImageDrawable(LayouUtil.getDrawable("prev_btn"));
                ivPrePager.setAlpha(0.3f);
                ivNextPager.setImageDrawable(LayouUtil.getDrawable("next_btn"));
                ivPrePager.setAlpha(0.3f);
            }
            tvCurPager.setText((mCurPage + 1) + "/" + mMaxPage);
        } else {
            //llPager.setVisibility(View.GONE);
            setVisibility(View.GONE);
        }


        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                this.setBackground(LayouUtil.getDrawable("page_bg"));
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            default:
                this.setBackground(LayouUtil.getDrawable("page_bg_none"));
                break;
        }
    }

    private void destoryView(){
        removeAllViews();
    }

}
