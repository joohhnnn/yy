package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.sdk.TXZRecordWinManager;


public class HelpButtonView extends FrameLayout {

    private ImageView mHelpIv;
    private ImageView mHelpNewTagIv;
    private ImageView mSettingIv;

    public HelpButtonView(@NonNull Context context,int width,int height) {
        super(context);
        initView(width,height);

        LogUtil.logd(WinLayout.logTag+ "HelpButtonView: isShowHelpInfos:"+ConfigUtil.isShowHelpInfos()+"--isShowHelpNewTag:"+ConfigUtil.isShowHelpNewTag());
    }

    private void initView(int width,int height){
            mHelpIv = showHelp();
        mHelpIv.setVisibility(GONE);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(width,height);
            addView(mHelpIv,layoutParams);

            mHelpNewTagIv = showHelpNewTag();
        mHelpNewTagIv.setVisibility(GONE);
            layoutParams = new FrameLayout.LayoutParams((int) (width/2.7), (int) (height/2.7));
            layoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
            addView(mHelpNewTagIv,layoutParams);

            mSettingIv = showSetting();
        mSettingIv.setVisibility(GONE);
            layoutParams = new FrameLayout.LayoutParams( width, height);
            addView(mSettingIv,layoutParams);
    }

    private ImageView showHelp(){
        ImageView ivHelp = new ImageView(GlobalContext.get());
        ivHelp.setImageDrawable(LayouUtil.getDrawable("question_mark"));
        ivHelp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_HELP, 0, 0);

                ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("voice_center").setType("touch_voice_center")
                        .putExtra("style", "help").setSessionId().buildCommReport());
            }
        });
        return ivHelp;
    }

    private ImageView showSetting(){
        ImageView ivSetting = new ImageView(GlobalContext.get());
        ivSetting.setImageDrawable(LayouUtil.getDrawable("ic_setting"));
        ivSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_SETTING,0,0);

                ReportUtil.doReport(new ReportUtil.Report.Builder().setAction("voice_center").setType("touch_voice_center")
                        .putExtra("style", "settting").setSessionId().buildCommReport());
            }
        });
        return ivSetting;
    }

    private ImageView showHelpNewTag(){
        ImageView ivHelpNewTag = new ImageView(GlobalContext.get());
        ivHelpNewTag.setImageDrawable(LayouUtil.getDrawable("ic_help_new_tag"));
        return ivHelpNewTag;
    }

    //根据配置显示帮助设置按钮
    public boolean showByConfig(){
        LogUtil.logd(WinLayout.logTag+ "showByConfig: isShowHelp:"+ConfigUtil.isShowHelpInfos()+"--isShowSetting:"+ConfigUtil.isShowSettings()+"--isShowHelpNewTag:"+ConfigUtil.isShowHelpNewTag());
        if (ConfigUtil.isShowHelpInfos()){
            mSettingIv.setVisibility(GONE);
            mHelpIv.setVisibility(VISIBLE);
            mHelpNewTagIv.setVisibility(ConfigUtil.isShowHelpNewTag()?VISIBLE:GONE);
            return true;
        }else if (ConfigUtil.isShowSettings()){
            mSettingIv.setVisibility(VISIBLE);
            mHelpIv.setVisibility(GONE);
            mHelpNewTagIv.setVisibility(GONE);
            return true;
        }else {
            mSettingIv.setVisibility(GONE);
            mHelpIv.setVisibility(GONE);
            mHelpNewTagIv.setVisibility(GONE);
            return false;
        }
    }
}
