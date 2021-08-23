package com.txznet.comm.ui.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;

import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.txz.util.TXZFileConfigUtil;


public class FilmPayResDialog extends WinDialog {

    private ImageView ivResults;
    private TextView tvResults;
    private int dialogShapeAlpha;
    private int dialogBackgrounAlpha;

    private Context mContext;

    public FilmPayResDialog() {
        super(true);
        setCanceledOnTouchOutside(false);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

    }

    @Override
    protected View createView() {
        return initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NewApi")
    private View initView() {
        mContext = GlobalContext.get();
        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        lp.width = (int)LayouUtil.getDimen("m211");
        lp.height = (int)LayouUtil.getDimen("m211");
        RelativeLayout fullContent = new RelativeLayout(mContext);
        fullContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        RelativeLayout.LayoutParams ryParams = new RelativeLayout.LayoutParams((int)LayouUtil.getDimen("m208"), (int)LayouUtil.getDimen("m208"));
        ryParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        RelativeLayout rlContents = new RelativeLayout(mContext);
        fullContent.setBackground(LayouUtil.getDrawable("click_on"));
        fullContent.addView(rlContents,ryParams);
        rlContents.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        LinearLayout lyContents = new LinearLayout(mContext);
        lyContents.setGravity(Gravity.CENTER);
        ryParams= new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        ryParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        lyContents.setOrientation(LinearLayout.VERTICAL);
        rlContents.addView(lyContents,ryParams);
        ivResults = new ImageView(mContext);
        ivResults.setScaleType(ImageView.ScaleType.FIT_XY);
        LinearLayout.LayoutParams lyParams = new LinearLayout.LayoutParams((int)LayouUtil.getDimen("m88"),(int)LayouUtil.getDimen("m88"));
        lyContents.addView(ivResults,lyParams);
        tvResults = new TextView(mContext);
        tvResults.setTextSize((int)LayouUtil.getDimen("m21"));
        lyParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        lyParams.topMargin = (int)LayouUtil.getDimen("y25");
        lyContents.addView(tvResults,lyParams);
        dialogBackgrounAlpha = (int)(TXZFileConfigUtil
                .getSingleConfig(TXZFileConfigUtil.WAN_MI_FILM_PAY_REQUEST_DIALOG_BACKGROUND,
                        Double.class,0.878) * 255);
        dialogShapeAlpha = (int)(TXZFileConfigUtil
                .getSingleConfig(TXZFileConfigUtil.WAN_MI_FILM_PAY_REQUEST_DIALOG_SHAPE,
                        Double.class,0.586) * 255);
        fullContent.getBackground().setAlpha(dialogShapeAlpha);
        rlContents.getBackground().setAlpha(dialogBackgrounAlpha);
        return fullContent;
    }

    public void setIvResults(Drawable ivResultsDrawable){
        this.ivResults.setImageDrawable(ivResultsDrawable);
    }

    public void setTvResults(String text) {
        this.tvResults.setText(text);
    }

    @Override
    public void show() {
        super.show();
        mTimeout = 5000;
        checkTimeout();
    }

    @Override
    public void checkTimeout() {
        super.checkTimeout();
    }

}
