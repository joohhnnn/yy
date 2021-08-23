package com.txznet.music.widget.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.music.R;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.SysExitEvent;
import com.txznet.music.util.Utils;

import java.lang.ref.WeakReference;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author telen
 * @date 2019/1/18,14:23
 */
public class ExitAppDialog extends Dialog {
    @Bind(R.id.fl_content)
    ViewGroup flContent;
    @Bind(R.id.tv_back_run)
    TextView tvBackRun;
    @Bind(R.id.tv_confirm)
    TextView tvConfirm;
    @Bind(R.id.tv_cancel)
    TextView tvCancel;


    private ClickCallbackListener mClickCallbackListener;


    public ExitAppDialog(@NonNull Context context) {
        super(context, R.style.TXZ_Dialog_Style_Full);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_dialog_exit_app);
        ButterKnife.bind(this);
        flContent.setOnClickListener(v -> {
            dismiss();
        });
    }

    @OnClick({R.id.tv_back_run, R.id.tv_confirm, R.id.tv_cancel})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back_run:
                if (mClickCallbackListener != null) {
                    mClickCallbackListener.onBackRun();
                }
                break;
            case R.id.tv_confirm:
                if (mClickCallbackListener != null) {
                    mClickCallbackListener.onConfirm();
                }
                break;
            case R.id.tv_cancel:
                if (mClickCallbackListener != null) {
                    mClickCallbackListener.onCancel();
                }
                break;
            default:
                break;
        }
        this.dismiss();
    }

    public interface ClickCallbackListener {
        void onCancel();

        void onConfirm();

        void onBackRun();
    }

    public ExitAppDialog setClickCallback(ClickCallbackListener clickCallback) {
        mClickCallbackListener = clickCallback;
        return this;
    }

    public static class ExitAppCallback implements ClickCallbackListener {

        WeakReference<Activity> touchActivity;
        WeakReference<Dialog> touchDialog;

        public ExitAppCallback(Activity activity, Dialog dialog) {
            this.touchActivity = new WeakReference<>(activity);
            this.touchDialog = new WeakReference<>(dialog);
        }

        @Override
        public void onCancel() {
            Dialog dialog = touchDialog.get();
            if (dialog != null) {
                dialog.dismiss();
            }
        }

        @Override
        public void onConfirm() {
            ReportEvent.reportExit(SysExitEvent.EXIT_TYPE_MANUAL);
            Dialog dialog = touchDialog.get();
            if (dialog != null) {
                dialog.dismiss();
            }
            Utils.exitApp(touchActivity.get());
        }

        @Override
        public void onBackRun() {
            ReportEvent.reportExit(SysExitEvent.EXIT_TYPE_MANUAL);
            Dialog dialog = touchDialog.get();
            if (dialog != null) {
                dialog.dismiss();
            }
            Utils.onBackRun(touchActivity.get());
        }
    }

}
