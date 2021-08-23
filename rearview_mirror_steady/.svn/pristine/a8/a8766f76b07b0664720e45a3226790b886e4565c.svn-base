package com.txznet.comm.ui.theme.test.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.view.VoiceWaveView;
import com.txznet.comm.ui.viewfactory.data.FeedbackRecordingListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IFeedbackRecordingView;
import com.txznet.resholder.R;
import com.txznet.sdk.TXZRecordWinManager;

import java.util.Locale;

/**
 * 说明：
 *
 * @author xiaolin
 * create at 2020-10-09 11:14
 */
public class FeedbackRecordingView extends IFeedbackRecordingView {

    private static FeedbackRecordingView sInstance = new FeedbackRecordingView();

    public static FeedbackRecordingView getInstance() {
        return sInstance;
    }

    private VoiceWaveView mVoiceWaveView;
    private Button btnSend;
    private TextView tvSpareTime;


    @Override
    public ExtViewAdapter getView(ViewData viewData) {
        FeedbackRecordingListViewData data = (FeedbackRecordingListViewData) viewData;
        WinLayout.getInstance().vTips = data.vTips;

        View view = createView(data);

        ExtViewAdapter viewAdapter = new ExtViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.view.setTag(viewAdapter);
        viewAdapter.isListView = true;
        viewAdapter.object = this;

        return viewAdapter;
    }

    private View createView(FeedbackRecordingListViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.feedback_recording_view, (ViewGroup) null);

        mVoiceWaveView = view.findViewById(R.id.voiceWaveView);

        tvSpareTime = view.findViewById(R.id.tvSpareTime);

        final Button btnCancel = view.findViewById(R.id.btnCancel);

        btnSend = view.findViewById(R.id.btnSend);
        btnSend.setText("发送反馈");
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_FEEDBACK_SEND,
                        0, 0, 1);
                btnSend.setOnClickListener(null);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecordWin2Manager.getInstance().operateView(
                        TXZRecordWinManager.RecordWin2.RecordWinController.OPERATE_CLICK,
                        TXZRecordWinManager.RecordWin2.RecordWinController.VIEW_FEEDBACK_CANCEL,
                        0, 0, 1);
                btnCancel.setOnClickListener(null);
            }
        });

        return view;
    }

    @Override
    public void startAnimator() {
        mVoiceWaveView.startAnimator();
    }

    @Override
    public void cancelAnimator() {
        mVoiceWaveView.cancelAnimator();
    }

    @Override
    public void updateFeedback(int i) {
        // btnSend.setText(String.format(Locale.getDefault(), "发送反馈(%ds)", i));
    }

    @Override
    public void updateSpareTime(int i) {
        tvSpareTime.setText(String.format(Locale.getDefault(), "反馈中，乘余时长%ds", i));
    }

    @Override
    public void updateProgress(int i, int i1) {

    }

    @Override
    public void snapPage(boolean b) {

    }

    @Override
    public void updateItemSelect(int i) {

    }


}
