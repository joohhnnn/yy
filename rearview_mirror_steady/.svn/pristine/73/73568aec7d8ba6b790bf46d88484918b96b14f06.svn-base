package com.txznet.launcher.module.wechat;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.percent.PercentRelativeLayout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.nav.NavManager;
import com.txznet.launcher.domain.wechat.WechatManager;
import com.txznet.launcher.img.ImgLoader;
import com.txznet.launcher.module.BaseModule;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZWechatManagerV2;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ASUS User on 2018/3/20.
 * 展示发微信的界面
 */

public class WechatRecordModule extends BaseModule {

    @Bind(R.id.tv_wechat_title)
    TextView tvWechatTitle;
    @Bind(R.id.iv_wechat_user_icon)
    ImageView ivWechatUserIcon;
    @Bind(R.id.tv_wechat_user_nick)
    TextView tvWechatUserNick;
    @Bind(R.id.iv_wechat_icon)
    ImageView ivWechatIcon;
    @Bind(R.id.rl_wechat_user)
    PercentRelativeLayout rlWechatUser;
    @Bind(R.id.iv_wechat_record)
    ImageView ivWechatRecord;
    @Bind(R.id.tv_wechat_send_tip)
    TextView tvWechatSendTip;
    @Bind(R.id.tv_wechat_send_tip2)
    TextView tvWechatSendTip2;
    @Bind(R.id.ll_wechat_record)
    LinearLayout llWechatRecord;
    @Bind(R.id.iv_wechat_send_state)
    ImageView ivWechatSendState;
    private int mCurrentType = -1;

    private String url = "";
    private String id = "";
    private String nick = "";
    private int timeRemain = 0;

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        parseData(data);
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_wechat_record, null);

        ButterKnife.bind(this, view);

        refreshContainer();

        return view;
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        parseData(data);

        refreshContainer();
    }

    private void parseData(String data) {
        JSONBuilder jsonBuilder = new JSONBuilder(data);
        mCurrentType = jsonBuilder.getVal("type", Integer.class);
        switch (mCurrentType) {
            case WechatManager.TYPE_WECHAT_RECORD:
                timeRemain = jsonBuilder.getVal("timeRemain", Integer.class, 0);
                id = jsonBuilder.getVal("id", String.class);
                nick = jsonBuilder.getVal("nick", String.class);
            case WechatManager.TYPE_WECHAT_SEND_BEGIN:
                break;
            case WechatManager.TYPE_WECHAT_SEND_SUCCESS:
                break;
            case WechatManager.TYPE_WECHAT_SEND_FAIL:
                break;
        }
    }

    private boolean bReturnNavAfterRecord; // 是否需要在回复微信后返回导航

    @Override
    public void onResume() {
        super.onResume();
        if (!bReturnNavAfterRecord) {
            bReturnNavAfterRecord = NavManager.getInstance().isFocus();
        }
    }

    private void refreshContainer() {
        switch (mCurrentType) {
            case WechatManager.TYPE_WECHAT_RECORD:
                WechatManager.getInstance().getUsericon(id, new TXZWechatManagerV2.ImageListener() {
                    @Override
                    public void onImageReady(String id, String imgPath) {
                        ImgLoader.loadCircleImage(imgPath, ivWechatUserIcon);
                    }
                });
                tvWechatUserNick.setText(nick);
                tvWechatSendTip.setText("完毕完毕");
                tvWechatSendTip2.setText("（录音将在" + timeRemain + "s自动发送）");
                tvWechatSendTip2.setVisibility(View.VISIBLE);
                ivWechatSendState.setVisibility(View.GONE);
                llWechatRecord.setVisibility(View.VISIBLE);
                ((LinearLayout.LayoutParams)ivWechatRecord.getLayoutParams()).gravity = Gravity.BOTTOM;
                ivWechatRecord.setImageResource(R.drawable.anim_wechat_record);
                ((AnimationDrawable) ivWechatRecord.getDrawable()).start();
                break;
            case WechatManager.TYPE_WECHAT_SEND_BEGIN:
                tvWechatSendTip.setText("发送中...");
                tvWechatSendTip2.setVisibility(View.GONE);
                ivWechatSendState.setVisibility(View.GONE);
                llWechatRecord.setVisibility(View.VISIBLE);
                ((LinearLayout.LayoutParams)ivWechatRecord.getLayoutParams()).gravity = Gravity.CENTER_VERTICAL;
                ivWechatRecord.setImageResource(R.drawable.anim_wechat_send);
                ((AnimationDrawable) ivWechatRecord.getDrawable()).start();
                break;
            case WechatManager.TYPE_WECHAT_SEND_SUCCESS:
                tvWechatSendTip.setText("发送成功");
                tvWechatSendTip2.setVisibility(View.GONE);
                ivWechatSendState.setVisibility(View.VISIBLE);
                llWechatRecord.setVisibility(View.GONE);
                ivWechatSendState.setImageResource(R.drawable.ic_wechat_send_success);
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        LaunchManager.getInstance().launchBack();
                        if (bReturnNavAfterRecord) {
                            bReturnNavAfterRecord = false;
                            NavManager.getInstance().enterNav();
                        }
                    }
                }, 1000);
                break;
            case WechatManager.TYPE_WECHAT_SEND_FAIL:
                tvWechatSendTip.setText("发送失败");
                tvWechatSendTip2.setVisibility(View.GONE);
                ivWechatSendState.setVisibility(View.VISIBLE);
                llWechatRecord.setVisibility(View.GONE);
                ivWechatSendState.setImageResource(R.drawable.ic_wechat_send_error);
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        LaunchManager.getInstance().launchBack();
                        if (bReturnNavAfterRecord) {
                            bReturnNavAfterRecord = false;
                            NavManager.getInstance().enterNav();
                        }
                    }
                }, 1000);
                break;
            case WechatManager.TYPE_WECHAT_SEND_CANCEL:
                LaunchManager.getInstance().launchBack();
                if (bReturnNavAfterRecord) {
                    bReturnNavAfterRecord = false;
                    NavManager.getInstance().enterNav();
                }
                break;
        }
    }


}
