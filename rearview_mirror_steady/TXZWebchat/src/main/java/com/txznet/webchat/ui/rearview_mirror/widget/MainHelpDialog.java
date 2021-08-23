package com.txznet.webchat.ui.rearview_mirror.widget;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.txz.util.focus_supporter.wrappers.SimpleDrawableWrapper;
import com.txznet.webchat.R;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.ui.base.widgets.AppBaseWinDialog;

import butterknife.Bind;

/**
 * 用户第一次进入主界面时显示的帮助Dialog
 * Created by J on 2016/4/12.
 */
public class MainHelpDialog extends AppBaseWinDialog {
    private static final String ASR_CMD = "WX_MAIN_HELP_DIALOG_CMD";

    @Bind(R.id.tv_main_help_dialog_commit)
    TextView mTvCommit;

    private int mTipTtsId = TtsUtil.INVALID_TTS_TASK_ID;

    public MainHelpDialog(Context context) {
        super(false);
    }

    @Override
    public int getLayout() {
        return R.layout.layout_main_help_dialog;
    }

    @Override
    public void init() {
        this.setCanceledOnTouchOutside(true);

        mTvCommit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TtsUtil.cancelSpeak(mTipTtsId);
                dismiss();
            }
        });

        this.setOnCancelListener(new OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                AsrUtil.recoverWakeupFromAsr(ASR_CMD);
                //TtsActionCreator.get().insertTts("", "", true, null);
                TtsUtil.cancelSpeak(mTipTtsId);
            }
        });

        AsrUtil.useWakeupAsAsr(new AsrUtil.AsrComplexSelectCallback() {
            public void onCommandSelected(String type, String command) {
                //TtsActionCreator.get().insertTts("", "", true, null);
                TtsUtil.cancelSpeak(mTipTtsId);
                dismiss();
            }

            @Override
            public boolean needAsrState() {
                return false;
            }

            @Override
            public String getTaskId() {
                return ASR_CMD;
            }
        }.addCommand("CLOSE", "我知道了", "确定"));
    }

    @Override
    public void initFocusViewList() {
        getNavBtnSupporter().setViewList(new SimpleDrawableWrapper(mTvCommit, getContext().getResources().getDrawable(R.drawable.ic_nav_indicator_dialog_commit_full)));

        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocus(mTvCommit);
        }

    }

    @Override
    public void show() {
        super.show();
        // 播报帮助信息
        //TtsActionCreator.get().insertTts("", getContext().getString(R.string.lb_main_help_dialog_tts_content), false, null);
        mTipTtsId = TtsUtil.speakText(getContext().getString(R.string.lb_main_help_dialog_tts_content), TtsUtil.PreemptType.PREEMPT_TYPE_NEXT);
    }

    @Override
    protected void onStop() {
        AsrUtil.recoverWakeupFromAsr(ASR_CMD);
        //TtsActionCreator.get().insertTts("", "", true, null);
        TtsUtil.cancelSpeak(mTipTtsId);
        super.onStop();
    }

    @Override
    public void dismiss() {
        TtsUtil.cancelSpeak(mTipTtsId);
        super.dismiss();
    }

    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
