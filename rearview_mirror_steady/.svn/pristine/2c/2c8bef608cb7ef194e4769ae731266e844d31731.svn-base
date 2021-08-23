package com.txznet.txz.ui.widget;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.txz.R;

public class SimEmptyDialog extends WinDialog {
    private boolean bExit = true;
    private String mContent, mTips, mUrl;
    private int mClickTime = 0;

    private TextView mTvContent, mTvTips;
    private ImageView mIvExit;
    private WebView mWebView;
    private static final String TAG = "SimEmptyDialog::";

    public SimEmptyDialog(Builder builder) {
        super(builder);
        this.bExit = builder.bExit;
        this.mContent = builder.mContent;
        this.mTips = builder.mTips;
        this.mUrl = builder.mUrl;
    }

    @Override
    protected View createView() {
        LayoutInflater layoutInflater = super.mDialog.getLayoutInflater();
        mView = layoutInflater.inflate(R.layout.sim_empty_dialog, null, false);
        return mView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView() {
        mTvContent = (TextView) mView.findViewById(R.id.tv_sim_empty_content);
        mIvExit = (ImageView) mView.findViewById(R.id.iv_sim_empty_exit);
        mTvTips = (TextView) mView.findViewById(R.id.tv_sim_empty_tips);
        mWebView = (WebView) mView.findViewById(R.id.web_view_sim_empty);

        if(!TextUtils.isEmpty(mContent)){
            mTvContent.setText(mContent);
        }
        if(!TextUtils.isEmpty(mTips)){
            mTvTips.setText(mTips);
        }
        if(bExit){
            mIvExit.setVisibility(View.VISIBLE);
        }else{
            mIvExit.setVisibility(View.INVISIBLE);
        }
        mIvExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss("exit");
            }
        });
        mTvTips.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickTime ++;
                if(mClickTime >= 10){
                    dismiss("backdoor");
                }
            }
        });
        GlobalObservableSupport.getHomeObservable().registerObserver(mHomeBoserver);
    }

    @Override
    protected void onShow() {
        super.onShow();
        mClickTime = 0;
    }

    @Override
    protected void onDismiss() {
        GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeBoserver);
        super.onDismiss();
    }

    HomeObservable.HomeObserver mHomeBoserver = new HomeObservable.HomeObserver() {
        @Override
        public void onHomePressed() {
            LogUtil.logd(TAG + "Home pressed");
            if(bExit){
                dismiss("home click");
            }
        }
    };

    @Override
    public String getReportDialogId() {
        return "sim_empty";
    }

    public static class Builder extends DialogBuildData {
        private boolean bExit = true;
        private String mContent;
        private String mTips;
        private String mUrl;

        public boolean isbExit() {
            return bExit;
        }

        public Builder setbExit(boolean bExit) {
            this.bExit = bExit;
            return this;
        }

        public String getmContent() {
            return mContent;
        }

        public Builder setmContent(String mContent) {
            this.mContent = mContent;
            return this;
        }

        public String getmTips() {
            return mTips;
        }

        public Builder setmTips(String mTips) {
            this.mTips = mTips;
            return this;
        }

        public String getmUrl() {
            return mUrl;
        }

        public Builder setmUrl(String mUrl) {
            this.mUrl = mUrl;
            return this;
        }
    }

}
