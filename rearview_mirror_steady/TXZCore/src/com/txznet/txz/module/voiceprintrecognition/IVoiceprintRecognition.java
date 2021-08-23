package com.txznet.txz.module.voiceprintrecognition;

import com.txznet.txz.module.voiceprintrecognition.voiceai.VPRCSDKWrapper;
import com.txznet.txz.module.voiceprintrecognition.voiceai.VoiceAIImpl;

import java.util.List;

public interface IVoiceprintRecognition {
    public interface UploadStateListener {
        void onSuccess(String fileID);
        void onFail(int errorCode);
        void onProgress(int i);
    }

    public interface ExecuteResultCallback<T> {
        void onSuccess(T s);
        void onFail(int errorCode);
    }
    void init();

    void guessGender(List<String> fileIds,final ExecuteResultCallback executeResultCallback);

    void verify(String clientId, String groupId, String fileId,final ExecuteResultCallback executeResultCallback);

    void register(String username, String describe, String groupId, List<String> fileIds, IVoiceprintRecognition.ExecuteResultCallback executeResultCallback);

    void upload(String path, VoiceAIImpl.UploadStateListener listener);
    void resetUser(String clientId, String username, String groupId, List<String> fileIdList, final ExecuteResultCallback executeResultCallback) ;

    void removeUser(String clientId, String groupId, final ExecuteResultCallback<String> callback);

    void getAllUser(String groupId, final ExecuteResultCallback<List<VPRCSDKWrapper.NativeUser>> executeResultCallback);

    void groupAdd(String groupName, String describe, final ExecuteResultCallback<String> executeResultCallback);
    void groupRemove(String groupId, final ExecuteResultCallback<String> executeResultCallback);


}
