package com.txznet.launcher.module.wechat;

import android.content.Context;
import android.support.percent.PercentRelativeLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amap.api.maps.TextureMapView;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.wechat.WechatManager;
import com.txznet.launcher.img.ImgLoader;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.module.wechat.bean.WechatMsgData;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZWechatManagerV2;
import com.txznet.sdk.bean.WechatMessageV2;
import com.txznet.txz.util.runnables.Runnable1;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by TXZ-METEORLUO on 2018/2/24.
 * 用Dialog展示接受微信消息的界面
 * 大概是在导航时展示用的吧。一般的展示是直接在WechatModule中展示的。
 */

public class WechatDialogModule extends BaseModule {

    private int status;
    private WechatMsgDialogViewHolder wechatMsgDialogViewHolder;
    private WechatMsgData wechatMsgData;

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        this.status = status;
        View rootView = null;
        switch (status) {
            case STATUS_DIALOG:
                rootView = View.inflate(context, R.layout.module_wechat_dialog, null);
                wechatMsgDialogViewHolder = new WechatMsgDialogViewHolder(rootView);
                break;
        }
        showMessageView(wechatMsgData);
        return rootView;
    }


    private void showMessageView(WechatMsgData wechatMsgData) {
        switch (status) {
            case STATUS_DIALOG:
                switch (wechatMsgData.mMsgType) {
                    case WechatMessageV2.MSG_TYPE_LOCATION:
                        wechatMsgDialogViewHolder.tvWechatMsgNav.setVisibility(View.VISIBLE);
                        break;
                    default:
                        wechatMsgDialogViewHolder.tvWechatMsgNav.setVisibility(View.GONE);
                        break;
                }
                WechatManager.getInstance().getUsericon(this.wechatMsgData.mSenderId, new TXZWechatManagerV2.ImageListener() {
                    @Override
                    public void onImageReady(String id, String imgPath) {
                        ImgLoader.loadCircleImage(imgPath, wechatMsgDialogViewHolder.ivWechatSenderIcon);
                    }
                });
                break;
        }
    }

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        parseData(data);
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        parseData(data);
        showMessageView(wechatMsgData);
    }

    private void parseData(String data) {
        wechatMsgData = new WechatMsgData();
        wechatMsgData.parseFromJsonString(data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    static class WechatMsgDialogViewHolder {
        @Bind(R.id.iv_wechat_sender_icon)
        ImageView ivWechatSenderIcon;
        @Bind(R.id.tv_wechat_msg_answer)
        TextView tvWechatMsgAnswer;
        @Bind(R.id.tv_wechat_msg_nav)
        TextView tvWechatMsgNav;

        WechatMsgDialogViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
