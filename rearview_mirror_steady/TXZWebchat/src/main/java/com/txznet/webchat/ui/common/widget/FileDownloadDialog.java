package com.txznet.webchat.ui.common.widget;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.webchat.R;
import com.txznet.webchat.RecordStatusObservable;
import com.txznet.webchat.actions.ResourceActionCreator;
import com.txznet.webchat.actions.TtsActionCreator;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.ui.base.widgets.AppBaseWinDialog;
import com.txznet.webchat.util.FileUtil;

import butterknife.Bind;

/**
 * 文件下载结果Dialog
 * Created by J on 2017/6/15.
 */

public class FileDownloadDialog extends AppBaseWinDialog {
    @Bind(R.id.tv_file_download_title)
    TextView mTvTitle;
    @Bind(R.id.fl_file_download_confirm)
    FrameLayout mFlConfirm;
    @Bind(R.id.tv_file_download_confirm)
    TextView mTvConfirm;
    @Bind(R.id.fl_file_download_cancel)
    FrameLayout mFlCancel;
    @Bind(R.id.tv_file_download_cancel)
    TextView mTvCancel;

    private static final String ASR_CMD = "WX_FILE_DOWNLOAD_DIALOG_CMD";

    private static final String TTS_DOWNLOAD_FAILED = "文件, %s, 下载失败, 确定要重新下载吗?";
    private static final String TTS_DOWNLOAD_SUCCESS = "文件, %s, 下载成功, 确定要现在打开吗?";

    private WxMessage mMessage;
    private boolean bDownloadSuccess = false;
    private int mTtsId;

    public FileDownloadDialog updateMessage(WxMessage msg) {
        mMessage = msg;
        bDownloadSuccess = ResourceActionCreator.get().isFileDownloaded(msg);
        if (bDownloadSuccess) {
            mTvTitle.setText(getContext().getResources().getText(R.string.lb_file_download_success));
        } else {
            mTvTitle.setText(getContext().getResources().getText(R.string.lb_file_download_failed));
        }

        return this;
    }

    @Override
    public int getLayout() {
        return R.layout.layout_file_download_dialog;
    }

    @Override
    public void init() {
        mFlConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bDownloadSuccess) {
                    FileUtil.openFile(mMessage.mFilePath);
                } else {
                    ResourceActionCreator.get().downloadFile(mMessage);
                }
                dismiss();
            }
        });

        mFlCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    @Override
    public void initFocusViewList() {

    }

    public void show(boolean enableTts) {
        super.show();

        resetCountdown();
        if (enableTts) {
            String tts;
            if (!bDownloadSuccess) {
                tts = String.format(TTS_DOWNLOAD_FAILED, FileUtil.getFileNameForTts(mMessage.mFileName));
            } else {
                tts = String.format(TTS_DOWNLOAD_SUCCESS, FileUtil.getFileNameForTts(mMessage.mFileName));
            }

            mTtsId = TtsUtil.speakText(tts, mTtsCallback);
        } else {
            AppLogic.runOnUiGround(mRefreshCountDownTask, 0);
        }

        AppLogic.registerRecordStatusObserver(mRecordObserver);
    }


    @Override
    public void show() {
        show(true);
    }

    @Override
    protected void onStop() {
        AsrUtil.recoverWakeupFromAsr(ASR_CMD);
        TtsUtil.cancelSpeak(mTtsId);
        super.onStop();
    }

    @Override
    public void dismiss() {
        TtsUtil.cancelSpeak(mTtsId);
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                TtsActionCreator.get().procTtsQueue();
            }
        }, 1000);

        AppLogic.removeUiGroundCallback(mRefreshCountDownTask);
        AppLogic.unregisterRecordStatusObserver(mRecordObserver);

        super.dismiss();
    }

    private TtsUtil.ITtsCallback mTtsCallback = new TtsUtil.ITtsCallback() {
        @Override
        public void onSuccess() {
            AsrUtil.useWakeupAsAsr(new AsrUtil.AsrComplexSelectCallback() {
                public void onCommandSelected(String type, String command) {
                    //TtsActionCreator.get().insertTts("", "", true, null);
                    TtsUtil.cancelSpeak(mTtsId);
                    if ("CONFIRM".equals(type)) {
                        if (bDownloadSuccess) {
                            TtsUtil.speakText("正在为您打开", new TtsUtil.ITtsCallback() {
                                @Override
                                public void onSuccess() {
                                    FileUtil.openFile(mMessage.mFilePath);
                                }
                            });
                        } else {
                            TtsUtil.speakText("将为您下载", new TtsUtil.ITtsCallback() {
                                @Override
                                public void onSuccess() {
                                    ResourceActionCreator.get().downloadFile(mMessage);
                                }
                            });
                        }

                    }

                    dismiss();
                }

                @Override
                public boolean needAsrState() {
                    return true;
                }

                @Override
                public String getTaskId() {
                    return ASR_CMD;
                }
            }.addCommand("CONFIRM", "确定")
                    .addCommand("CANCEL", "取消"));

            AppLogic.runOnUiGround(mRefreshCountDownTask, 0);
        }
    };

    private RecordStatusObservable.StatusObserver mRecordObserver = new RecordStatusObservable.StatusObserver() {
        @Override
        public void onStatusChanged(boolean isShowing) {
            if (isShowing && isShowing()) {
                dismiss();
            }
        }
    };


    private Runnable mRefreshCountDownTask = new Runnable() {
        @Override
        public void run() {
            if (tickCountdown() >= 0) {
                AppLogic.removeUiGroundCallback(this);
                AppLogic.runOnUiGround(this, 1000);
            } else {
                dismiss();
            }

        }
    };

    private int mCountdown = 6;

    private int tickCountdown() {
        mTvCancel.setText("取消(" + mCountdown + ")");

        mCountdown--;
        return mCountdown;
    }

    private int resetCountdown() {
        mCountdown = 6;
        mTvCancel.setText("取消");

        return mCountdown;
    }


    ////////// single instance
    private static FileDownloadDialog sInstance;

    public static FileDownloadDialog getInstance() {
        if (null == sInstance) {
            synchronized (FileDownloadDialog.class) {
                if (null == sInstance) {
                    sInstance = new FileDownloadDialog();
                }
            }
        }

        return sInstance;
    }

    private FileDownloadDialog() {
        super(true);
    }
}
