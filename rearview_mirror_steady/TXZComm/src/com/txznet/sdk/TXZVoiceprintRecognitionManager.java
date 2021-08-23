package com.txznet.sdk;

import com.txznet.comm.remote.util.VoiceprintRecognitionUtil;

import java.util.List;

public class TXZVoiceprintRecognitionManager {
    private static TXZVoiceprintRecognitionManager sInstance = new TXZVoiceprintRecognitionManager();
    private RecordFileStateCallback mRecordFileStateCallback;
    private TXZVoiceprintRecognitionManager() {
    }

    public void onReconnectTXZ() {
        if (mRecordFileStateCallback != null) {
            setRecordFileStateCallback(mRecordFileStateCallback);
        }
    }

    public interface RecordFileStateCallback {
        /**
         * @param path 录音文件路径，null表示保存录音文件失败
         */
        void onResult(String path);
    }

    public static TXZVoiceprintRecognitionManager getInstance() {
        return sInstance;
    }

    public static class CloudUser {
        String userName;
        String originalName;
        String clientId;
        String describe;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getOriginalName() {
            return originalName;
        }

        public void setOriginalName(String originalName) {
            this.originalName = originalName;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getDescribe() {
            return describe;
        }

        public void setDescribe(String describe) {
            this.describe = describe;
        }

    }

    public interface ResultCallback<T> {
        /**
         * 成功时回调
         */
        void onResult(T data);

        /**
         * 失败时回调
         */
        void onError(int errorCode);
    }

    /**
     * 设置保存录音文件回调监听,后面设置的回调会覆盖之前的回调
     * @param callback
     */
    public void setRecordFileStateCallback(RecordFileStateCallback callback) {
        VoiceprintRecognitionUtil.setRecordFileStateCallback(callback);
        mRecordFileStateCallback = callback;
    }

    public void removeRecordFileStateCallback() {
        VoiceprintRecognitionUtil.removeRecordFileStateCallback();
        mRecordFileStateCallback = null;
    }

    /**
     * 上传录音文件用于声纹功能
     * @param path 录音文件路径
     * @param callback 上传结果回调
     */
    public void upload(String path, ResultCallback<String> callback) {
        VoiceprintRecognitionUtil.upload(path, callback);
    }

    /**
     * 上传录音文件用于声纹功能，使用语音缓存的录音文件
     * @param callback 上传结果回调
     */
    public void upload(ResultCallback<String> callback) {
        VoiceprintRecognitionUtil.upload(callback);
    }

    /**
     * 注册用户声纹
     * @param username 用户名
     * @param groupId 用户组ID
     * @param fileIds 上传文件ID列表
     * @param callback 注册结果回调
     */
    public void register( String username, String describe, String groupId, List<String> fileIds, ResultCallback<String> callback) {
        VoiceprintRecognitionUtil.register(username, describe, groupId, fileIds, callback);
    }

    /**
     * 重置用户声纹
     * @param clientId 用户ID
     * @param username 用户名
     * @param groupId 用户组ID
     * @param fileIds 上传文件ID列表
     * @param callback 注册结果回调
     */
    public void resetUser(String clientId, String username, String groupId, List<String> fileIds, ResultCallback<Boolean> callback) {
        VoiceprintRecognitionUtil.resetUser(clientId, username, groupId, fileIds, callback);
    }

    /**
     * 验证用户声纹
     * @param clientId 用户ID
     * @param groupId 用户组ID
     * @param fileId 上传文件ID
     * @param callback 验证结果回调
     */
    public void verify(String clientId, String groupId, String fileId, ResultCallback<Boolean> callback) {
        VoiceprintRecognitionUtil.verify(clientId, groupId, fileId, callback);
    }

    /**
     * 验证用户声纹
     * @param groupId 用户组ID
     * @param fileId 上传文件ID
     * @param callback 验证结果回调
     */
    public void verify(String groupId, String fileId, ResultCallback<List<CloudUser>> callback) {
        VoiceprintRecognitionUtil.verify(groupId, fileId, callback);
    }


    /**
     * 删除用户
     * @param clientId 用户ID
     * @param groupId 用户组ID
     * @param callback 删除结果回调
     */
    public void removeUser(String clientId, String groupId, ResultCallback<Boolean> callback) {
        VoiceprintRecognitionUtil.removeUser(clientId, groupId, callback);
    }

    /**
     *  获取指定用户组Id的所有用户信息
     * @param groupId 用户组ID
     * @param callback 数据结果回调
     */
    public void getAllUserByGroupId(String groupId, ResultCallback<List<CloudUser>> callback) {
        VoiceprintRecognitionUtil.getAllUser(groupId, callback);
    }

    /**
     * 添加用户组
     * @param groupName 用户组名
     * @param describe 用户组描述
     * @param callback 结果回调
     */
    public void groupAdd(String groupName, String describe, final TXZVoiceprintRecognitionManager.ResultCallback<String> callback) {
        VoiceprintRecognitionUtil.groupAdd(groupName, describe, callback);
    }

    /**
     * 删除用户组
     * @param groupId 用户组ID
     * @param callback 结果回调
     */
    public void groupRemove(String groupId, final TXZVoiceprintRecognitionManager.ResultCallback<Boolean> callback) {
        VoiceprintRecognitionUtil.groupRemove(groupId, callback);
    }

    /**
     * 获取Core用户组ID
     */
    public String getCoreGroupId() {
        return VoiceprintRecognitionUtil.getCoreGroupId();
    }
}
