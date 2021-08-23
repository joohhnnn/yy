package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.SkillfulReminding;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.comm.util.TextViewUtil;

public class SkilledRemaindView extends RelativeLayout {

    public SkilledRemaindView(Context context) {
        super(context);
        init();
    }

    public SkilledRemaindView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SkilledRemaindView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        int unit;
        int size;
        unit = ViewParamsUtil.unit;
        size = ViewParamsUtil.h6;
        //默认隐藏，避免问候语为空的时候，winalyout没有走addview方法，不会更新SkilledRemaindView状态，使得它在不该显示的时候显示
        setVisibility(GONE);

        setGravity(Gravity.CENTER_VERTICAL);

        TextView tvRemind =new TextView(GlobalContext.get());
        tvRemind.setTypeface(Typeface.SERIF);
        tvRemind.setText("你已经是熟手啦！可以说“打开熟手模式”立即体验不一样的交互哦");
        TextViewUtil.setTextSize(tvRemind, size);
        TextViewUtil.setTextColor(tvRemind, Color.WHITE);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        //tvRemind.setPadding(0,0,2 * unit,0);
        //addViewInLayout(tvRemind,0,layoutParams);
        this.addView(tvRemind,layoutParams);

        ImageView ivClose = new ImageView(GlobalContext.get());
        ivClose.setImageDrawable(LayouUtil.getDrawable("close"));
        layoutParams = new RelativeLayout.LayoutParams(2*unit,2*unit);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
        //addViewInLayout(ivClose,1,layoutParams);
        this.addView(ivClose,layoutParams);

        ivClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("close").setType("skilled_remind")
                        .putExtra("initNum", 3 - SkillfulReminding.getInstance().getResiduaInitTimes())
                        .putExtra("showNum", 4 - SkillfulReminding.getInstance().residuaDisplayTimes).setSessionId().buildCommReport());

                LogUtil.logd(WinLayout.logTag+"closed skilledRemindView");

                setVisibility(INVISIBLE);
                SkillfulReminding.getInstance().reduceOnce();
                SkillfulReminding.getInstance().reduceOnce();
                SkillfulReminding.getInstance().reduceOnce();
                SkillfulReminding.getInstance().setResiduaInitTimes(0);
            }
        });

        setPadding(2*unit,0,2*unit,0);
    }

    public void show(int hight){
        if (SkillfulReminding.getInstance().ifShowRemind()){
            if (this.getVisibility() != VISIBLE){
                LogUtil.logd(WinLayout.logTag+"show skilledRemindView--residuaDisplayTimes:"+SkillfulReminding.residuaDisplayTimes
                +"--residuaInitTimes:"+SkillfulReminding.residuaInitTimes);
                //WinLayout.getInstance().halfHeight += LayouUtil.getDimen("y50");
                WinLayout.getInstance().skilledRemindViewHeight = hight;
                setVisibility(VISIBLE);

                ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("show").setType("skilled_remind")
                        .putExtra("initNum", 3 - SkillfulReminding.getInstance().getResiduaInitTimes())
                        .putExtra("showNum", 4 - SkillfulReminding.getInstance().residuaDisplayTimes).setSessionId().buildCommReport());
            }
        }else {
            WinLayout.getInstance().skilledRemindViewHeight = 0;
            setVisibility(GONE);
        }
    }

    public void hide(){
        WinLayout.getInstance().skilledRemindViewHeight = 0;
        setVisibility(GONE);
    }
}
