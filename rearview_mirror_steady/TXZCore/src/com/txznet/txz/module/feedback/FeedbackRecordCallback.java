package com.txznet.txz.module.feedback;

import com.txznet.comm.remote.util.RecorderUtil;

public abstract class FeedbackRecordCallback extends RecorderUtil.RecordCallback {

    abstract void onSpeechBegin();
    abstract void onSpeechEnd();
}
