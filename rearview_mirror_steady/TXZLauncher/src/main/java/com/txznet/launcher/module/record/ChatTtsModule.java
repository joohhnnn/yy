package com.txznet.launcher.module.record;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.module.BaseModule;

import me.wcy.htmltext.HtmlText;

/**
 * Created by TXZ-METEORLUO on 2018/3/14.
 * 展示播报文字的界面，如讲笑话,长文本
 */

public class ChatTtsModule extends BaseModule {
    private String mTtsText = null;
    private TextView mTtsTv;
    private int mStatus = STATUS_FULL;

    @Override
    public void onCreate(String data) {
        this.mTtsText = data;
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_tts_text_ly, null);
        mTtsTv = (TextView) view.findViewById(R.id.tts_text_tv);
        mStatus = status;
        if (status == STATUS_DIALOG) {
            initDialogView();
        } else {
            initView();
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(mTtsText)) {
            LogUtil.logd("mTtsTv mTtsText:" + mTtsText);
            parseDisplayText(mTtsText,mTtsTv);
            mTtsText = null;
        }
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        if (mTtsTv != null) {
            LogUtil.logd("mTtsTv refreshView:" + data);
            parseDisplayText(data,mTtsTv);
        }
    }

    private void parseDisplayText(String data,TextView textView) {
        if (mStatus == STATUS_DIALOG){
            HtmlText.from(data).after(new HtmlText.After() {
                @Override
                public CharSequence after(SpannableStringBuilder spannableStringBuilder) {
                    spannableStringBuilder.clearSpans();
                    return spannableStringBuilder;
                }
            }).into(textView);
        } else {
            HtmlText.from(data).into(textView);
        }
    }

    private void initDialogView() {
        mTtsTv.setSingleLine();
    }

    private void initView() {
    }
}