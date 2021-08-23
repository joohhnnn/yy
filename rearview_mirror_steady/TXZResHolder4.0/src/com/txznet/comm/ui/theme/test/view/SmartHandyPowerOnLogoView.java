package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.anim.FrameAnimation;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.SmartHandyPowerOnLogoViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.ISmartHandyPowerOnLogoView;
import com.txznet.resholder.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 说明：开机logo动画
 *
 * @author xiaolin
 * create at 2020-11-06 10:30
 */
public class SmartHandyPowerOnLogoView extends ISmartHandyPowerOnLogoView {

    private static SmartHandyPowerOnLogoView instance = new SmartHandyPowerOnLogoView();
    public static SmartHandyPowerOnLogoView getInstance(){
        return instance;
    }

    private View mRootView;
    private ImageView ivLogo;
    private TextView tvDate;
    private TextView tvWeek;
    private TextView tvHello;

    private FrameAnimation frameAnimation;

    @Override
    public void init(){
        Context context = UIResLoader.getInstance().getModifyContext();
        mRootView = LayoutInflater.from(context).inflate(R.layout.smart_handy_logo_view, (ViewGroup)null);

        ivLogo = mRootView.findViewById(R.id.ivLogo);
        tvDate = mRootView.findViewById(R.id.tvDate);
        tvWeek = mRootView.findViewById(R.id.tvWeek);
        tvHello = mRootView.findViewById(R.id.tvHello);

        frameAnimation = new FrameAnimation();
        for (int i = 1; i <= 20; i++) {
            Drawable drawable = LayouUtil.getDrawable("logo_frame_large_smile_" + i);
            frameAnimation.addFrame(drawable, 42);
        }
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData viewData) {
        updateView(viewData);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = viewData.getType();
        viewAdapter.view = mRootView;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.object = getInstance();

        return viewAdapter;
    }

    @Override
    public Object updateView(ViewData data) {
        SmartHandyPowerOnLogoViewData viewData = (SmartHandyPowerOnLogoViewData) data;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        tvDate.setText(format.format(new Date()));
        tvWeek.setText(getWeekOfDate(new Date()));
        tvHello.setText(viewData.tip);

        frameAnimation.setOneShot(false);
        ivLogo.setImageDrawable(frameAnimation);
        frameAnimation.stop();
        frameAnimation.start();
        return null;
    }

    /**
     * 获取当前日期是星期几<br>
     *
     * @param dt
     * @return 当前日期是星期几
     */
    public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

}
