package com.txznet.comm.remote.util;

import android.os.SystemClock;
import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZVoiceprintRecognitionManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import static com.txznet.comm.remote.ServiceManager.TXZ;

public class VoiceprintRecognitionUtil {

    public static final String PREFIX_CALLBACK_EVENT = "comm.voiceprint.recognition.event.";
    public static final String PREFIX_SEND = "comm.voiceprint.recognition.";

    /**
     * 命令字和回调事件后缀
     */
    public static final String SET_RECORD_FILE_STATE_SUFFIX = "setRecordFileState";
    public static final String REMOVE_RECORD_FILE_STATE_SUFFIX = "removeRecordFileState";
    public static final String UPLOAD_SUFFIX = "upload";
    public static final String REGISTER_SUFFIX = "register";
    public static final String REMOVE_USER_SUFFIX = "removeUser";
    public static final String VERIFY_SUFFIX = "verify";
    public static final String GROUP_ADD_SUFFIX = "groupAdd";
    public static final String GROUP_REMOVE_SUFFIX = "groupRemove";
    public static final String GET_ALL_USER_SUFFIX = "getAllUser";
    public static final String GET_CORE_GROUP_ID_SUFFIX = "getCoreGroupId";
    public static final String RESET_USER_SUFFIX = "resetUser";

    /**
     * 远程回调事件
     */
    public static final String RECORD_FILE_STATE_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + SET_RECORD_FILE_STATE_SUFFIX;
    public static final String UPLOAD_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + UPLOAD_SUFFIX;
    public static final String REGISTER_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + REGISTER_SUFFIX;
    public static final String REMOVE_USER_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + REMOVE_USER_SUFFIX;
    public static final String VERIFY_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + VERIFY_SUFFIX;
    public static final String GROUP_ADD_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + GROUP_ADD_SUFFIX;
    public static final String GROUP_REMOVE_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + GROUP_REMOVE_SUFFIX;
    public static final String GET_ALL_USER_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + GET_ALL_USER_SUFFIX;
    public static final String RESET_USER_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + RESET_USER_SUFFIX;

    /**
     * 发起远程调用
     */
    public static final String SEND_SET_RECORD_FILE = PREFIX_SEND + SET_RECORD_FILE_STATE_SUFFIX;
    public static final String SEND_REMOVE_RECORD_FILE = PREFIX_SEND + REMOVE_RECORD_FILE_STATE_SUFFIX;
    public static final String SEND_UPLOAD = PREFIX_SEND + UPLOAD_SUFFIX;
    public static final String SEND_REGISTER = PREFIX_SEND + REGISTER_SUFFIX;
    public static final String SEND_REMOVE_USER = PREFIX_SEND + REMOVE_USER_SUFFIX;
    public static final String SEND_VERIFY = PREFIX_SEND + VERIFY_SUFFIX;
    public static final String SEND_GROUP_ADD = PREFIX_SEND + GROUP_ADD_SUFFIX;
    public static final String SEND_GROUP_REMOVE = PREFIX_SEND + GROUP_REMOVE_SUFFIX;
    public static final String SEND_GET_ALL_USER = PREFIX_SEND + GET_ALL_USER_SUFFIX;
    public static final String SEND_GET_CORE_GROUP_ID = PREFIX_SEND + GET_CORE_GROUP_ID_SUFFIX;
    public static final String SEND_RESET_USER = PREFIX_SEND + RESET_USER_SUFFIX;

    /**
     * 设置保存录音文件回调监听,后面设置的回调会覆盖之前的回调
     * @param callback
     */
    public static void setRecordFileStateCallback(TXZVoiceprintRecognitionManager.RecordFileStateCallback callback) {
        sRecordFileStateCallback = callback;
        ServiceManager.getInstance().sendInvoke(TXZ, SEND_SET_RECORD_FILE,
                null, null);
    }

    public static void removeRecordFileStateCallback() {
        ServiceManager.getInstance().sendInvoke(TXZ, SEND_REMOVE_RECORD_FILE,
                null, null);
    }

    /**
     * 上传录音文件用于声纹功能，使用语音缓存的录音文件
     * @param callback 上传结果回调
     */
    public static void upload(final TXZVoiceprintRecognitionManager.ResultCallback<String> callback) {
        upload(null, callback);
    }

    /**
     * 上传录音文件用于声纹功能
     * @param path 录音文件路径
     * @param callback 上传结果回调
     */
    public static void upload(String path, final TXZVoiceprintRecognitionManager.ResultCallback<String> callback) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("path", path);
        sendInvoke(SEND_UPLOAD, jsonBuilder.toBytes(), callback);
    }

    /**
     * 注册Core用户
     * @param username 用户名
     * @param groupId 用户组ID
     * @param fileIds 录音文件列表
     * @param callback 注册结果回调
     */
    public static void register(String username, String groupId, List<String> fileIds, final TXZVoiceprintRecognitionManager.ResultCallback<String> callback) {
        register(username, "", groupId, fileIds, callback);
    }

    /**
     * 重置用户声纹
     * @param clientId 用户ID
     * @param username 用户名
     * @param groupId 用户组ID
     * @param fileIds 上传文件ID列表
     * @param callback 注册结果回调
     */
    public static void resetUser(String clientId, String username, String groupId, List<String> fileIds, TXZVoiceprintRecognitionManager.ResultCallback<Boolean> callback) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("username", username);
        jsonBuilder.put("groupId", groupId);
        jsonBuilder.put("clientId", clientId);
        JSONArray array = new JSONArray();
        for (String fileId : fileIds) {
            array.put(fileId);
        }
        jsonBuilder.put("fileIds", array);
        sendInvoke(SEND_RESET_USER, jsonBuilder.toBytes(), callback);
    }

    /**
     * 注册Core用户
     * @param username 用户名
     * @param describe 用户描述
     * @param groupId 用户组ID
     * @param fileIds 录音文件列表
     * @param callback 注册结果回调
     */
    public static void register(String username, String describe, String groupId, List<String> fileIds, final TXZVoiceprintRecognitionManager.ResultCallback<String> callback) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("username", username);
        jsonBuilder.put("describe", describe);
        jsonBuilder.put("groupId", groupId);
        JSONArray array = new JSONArray();
        for (String fileId : fileIds) {
            array.put(fileId);
        }
        jsonBuilder.put("fileIds", array);
        sendInvoke(SEND_REGISTER, jsonBuilder.toBytes(), callback);
    }

    /**
     * 删除用户
     * @param clientId 用户ID
     * @param groupId 用户组ID
     * @param callback 删除结果回调
     */
    public static void removeUser(String clientId, String groupId, final TXZVoiceprintRecognitionManager.ResultCallback<Boolean> callback) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("groupId", groupId);
        jsonBuilder.put("clientId", clientId);
        sendInvoke(SEND_REMOVE_USER, jsonBuilder.toBytes(), callback);
    }

    /**
     * 验证用户声纹
     * @param clientId 用户ID
     * @param groupId 用户组ID
     * @param fileId 上传文件ID
     * @param callback 验证结果回调
     */
    public static void verify(String clientId, String groupId, String fileId, final TXZVoiceprintRecognitionManager.ResultCallback<Boolean> callback) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("fileId", fileId);
        jsonBuilder.put("clientId", clientId);
        jsonBuilder.put("groupId", groupId);
        sendInvoke(SEND_VERIFY, jsonBuilder.toBytes(), callback);
    }

    /**
     * 验证用户声纹
     *
     * @param groupId  用户组ID
     * @param fileId   上传文件ID
     * @param callback 验证结果回调
     */
    public static void verify(String groupId, String fileId, final TXZVoiceprintRecognitionManager.ResultCallback<List<TXZVoiceprintRecognitionManager.CloudUser>> callback) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("fileId", fileId);
        jsonBuilder.put("groupId", groupId);
        sendInvoke(SEND_VERIFY, jsonBuilder.toBytes(), callback);
    }

    /**
     * 添加用户组
     * @param groupName 用户组名
     * @param describe 用户组描述
     * @param callback 结果回调
     */
    public static void groupAdd(String groupName, String describe, final TXZVoiceprintRecognitionManager.ResultCallback<String> callback) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("groupName", groupName);
        jsonBuilder.put("describe", describe);
        sendInvoke(SEND_GROUP_ADD, jsonBuilder.toBytes(), callback);
    }

    /**
     * 删除用户组
     * @param groupId 用户组ID
     * @param callback 结果回调
     */
    public static void groupRemove(String groupId, final TXZVoiceprintRecognitionManager.ResultCallback<Boolean> callback) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("groupId", groupId);
        sendInvoke(SEND_GROUP_REMOVE, jsonBuilder.toBytes(), callback);
    }

    /**
     *  获取指定用户组Id的所有用户信息
     * @param groupId 用户组ID
     * @param callback 数据结果回调
     */
    public static void getAllUser(String groupId,
                                  final TXZVoiceprintRecognitionManager.ResultCallback<List<TXZVoiceprintRecognitionManager.CloudUser>> callback) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("groupId", groupId);
        sendInvoke(SEND_GET_ALL_USER, jsonBuilder.toBytes(), callback);
    }

    /**
     * 获取Core用户组ID
     */
    public static String getCoreGroupId() {
        ServiceManager.ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, SEND_GET_CORE_GROUP_ID, null);
        if (serviceData != null) {
            return serviceData.getString();
        }
        return "";
    }



    private static final Map<Integer, RemoteTask> remoteTaskMapper = new HashMap<Integer, RemoteTask>();

    public static void addTask(int localTaskId, TXZVoiceprintRecognitionManager.ResultCallback callback) {
        addTask(localTaskId, callback, DEFAULT_TASK_TIMEOUT);
    }

    private final static int DEFAULT_TASK_TIMEOUT = 60 * 1000;

    public static void addTask(int localTaskId, TXZVoiceprintRecognitionManager.ResultCallback callback, int timeout) {
        synchronized (remoteTaskMapper) {
            long now = SystemClock.elapsedRealtime();
            cleanTimeoutTask(now);
            RemoteTask remoteTask = new RemoteTask();
            remoteTask.callback = callback;
            remoteTask.timeout = now + timeout;
            remoteTaskMapper.put(localTaskId, remoteTask);
        }
    }

    private static void cleanTimeoutTask(long now) {
        Iterator<Integer> iterator = remoteTaskMapper.keySet().iterator();
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            RemoteTask task = remoteTaskMapper.get(key);
            if (now > task.timeout) {
                if (task.callback != null) {
                    task.callback.onError(2);
                }
                LogUtil.logd("task(" + key + ") process timeout clean");
                iterator.remove();
            }
        }
    }

    private static TXZVoiceprintRecognitionManager.RecordFileStateCallback sRecordFileStateCallback;

    static class RemoteTask {
        int remoteId = -1;
        TXZVoiceprintRecognitionManager.ResultCallback callback;
        long timeout = 0;
    }

    @SuppressWarnings("unchecked")
    private static void notifyUploadCallback(RemoteTask remoteTask, JSONBuilder jsonBuilder) {
            int errorCode = jsonBuilder.getVal("errorCode", Integer.class, -1);
            if (errorCode == 0) {
                final String fileId = jsonBuilder.getVal("fileId", String.class);
                remoteTask.callback.onResult(fileId);
            } else {
                remoteTask.callback.onError(errorCode);
            }
    }

    @SuppressWarnings("unchecked")
    private static void notifyRegisterCallback(RemoteTask remoteTask, JSONBuilder jsonBuilder) {
        int errorCode = jsonBuilder.getVal("errorCode", Integer.class, -1);
        if (errorCode == 0) {
            final String clientId = jsonBuilder.getVal("clientId", String.class);
            remoteTask.callback.onResult(clientId);
        } else {
            remoteTask.callback.onError(errorCode);
        }
    }

    private static void notifyResetUserCallback(RemoteTask remoteTask, JSONBuilder jsonBuilder) {
        int errorCode = jsonBuilder.getVal("errorCode", Integer.class, -1);
        if (errorCode == 0) {
            final String result = jsonBuilder.getVal("result", String.class);
            remoteTask.callback.onResult(TextUtils.equals("true", result));
        } else {
            remoteTask.callback.onError(errorCode);
        }
    }

    @SuppressWarnings("unchecked")
    private static void notifyVerifyCallback(RemoteTask remoteTask, JSONBuilder jsonBuilder) {
        int errorCode = jsonBuilder.getVal("errorCode", Integer.class, -1);
        if (errorCode == 0) {
            final String result = jsonBuilder.getVal("result", String.class);
            if ("true".equals(result) || "false".equals(result)) {
                remoteTask.callback.onResult(TextUtils.equals("true", result));
            } else {
                List<TXZVoiceprintRecognitionManager.CloudUser> cloudUsers = new ArrayList<TXZVoiceprintRecognitionManager.CloudUser>();
                try {
                    if (!TextUtils.isEmpty(result)) {
                        JSONArray jsonArray = new JSONArray(result);
                        LogUtil.e("skyward: " + result);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            TXZVoiceprintRecognitionManager.CloudUser user = new TXZVoiceprintRecognitionManager.CloudUser();
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String client_name = jsonObject.getString("client_name");
                            String client_id = jsonObject.getString("clientid");
                            user.setClientId(client_id);
                            user.setUserName(client_name);
                            cloudUsers.add(user);
                        }
                    }
                    remoteTask.callback.onResult(cloudUsers);
                } catch (JSONException e) {
                    e.printStackTrace();
                    remoteTask.callback.onResult(cloudUsers);
                }
            }
        } else {
            remoteTask.callback.onError(errorCode);
        }
    }

    @SuppressWarnings("unchecked")
    private static void notifyGetAllUserCallback(RemoteTask remoteTask, JSONBuilder jsonBuilder) {
        int errorCode = jsonBuilder.getVal("errorCode", Integer.class, -1);
        if (errorCode == 0) {
            List<TXZVoiceprintRecognitionManager.CloudUser> cloudUsers = null;
            cloudUsers = new ArrayList<TXZVoiceprintRecognitionManager.CloudUser>();
            JSONArray jsonArray = null;
            try {
                if (jsonBuilder.getJSONObject().has("users")) {
                    jsonArray = jsonBuilder.getJSONObject().getJSONArray("users");
                }
                if (jsonArray != null && jsonArray.length() > 0) {
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        TXZVoiceprintRecognitionManager.CloudUser user = new TXZVoiceprintRecognitionManager.CloudUser();
                        user.setClientId(jsonObject.getString("clientId"));
                        user.setUserName(jsonObject.getString("clientName"));
                        user.setDescribe(jsonObject.getString("describe"));
                        user.setOriginalName(jsonObject.getString("originalName"));
                        cloudUsers.add(user);
                    }
                }
                remoteTask.callback.onResult(cloudUsers);
            } catch (JSONException e) {
                remoteTask.callback.onError(-1);
                e.printStackTrace();
            }
        } else {
            remoteTask.callback.onError(errorCode);
        }
    }

    @SuppressWarnings("unchecked")
    private static void notifyRemoveUserCallback(RemoteTask remoteTask, JSONBuilder jsonBuilder) {
        int errorCode = jsonBuilder.getVal("errorCode", Integer.class, -1);
        if (errorCode == 0) {
            final String result = jsonBuilder.getVal("result", String.class);
            remoteTask.callback.onResult(TextUtils.equals("true", result));
        } else {
            remoteTask.callback.onError(errorCode);
        }
    }

    @SuppressWarnings("unchecked")
    private static void notifyGroupAddCallback(RemoteTask remoteTask, JSONBuilder jsonBuilder) {
        int errorCode = jsonBuilder.getVal("errorCode", Integer.class, -1);
        if (errorCode == 0) {
            final String groupId = jsonBuilder.getVal("groupId", String.class);
            remoteTask.callback.onResult(groupId);
        } else {
            remoteTask.callback.onError(errorCode);
        }
    }

    @SuppressWarnings("unchecked")
    private static void notifyGroupRemoveCallback(RemoteTask remoteTask, JSONBuilder jsonBuilder) {
        int errorCode = jsonBuilder.getVal("errorCode", Integer.class, -1);
        if (errorCode == 0) {
            final String result = jsonBuilder.getVal("result", String.class);
            remoteTask.callback.onResult(TextUtils.equals("true", result));
        } else {
            remoteTask.callback.onError(errorCode);
        }
    }

    private static byte[] notifyRecordFileStateCallback(byte[] data) {
        JSONBuilder jsonBuilder = new JSONBuilder(new String(data));
        if (sRecordFileStateCallback == null) {
            LogUtil.d("sRecordFileStateCallback is null");
            return null;
        }
        LogUtil.d("path= " + jsonBuilder.getVal("path", String.class , null));
        sRecordFileStateCallback.onResult(jsonBuilder.getVal("path", String.class, null));
        return null;
    }

    public static byte[] notifyCallback(String event, byte[] data) {
        if (RECORD_FILE_STATE_CALLBACK_EVENT.equals(event)) {
            return notifyRecordFileStateCallback(data);
        }
        synchronized (remoteTaskMapper) {
            for (Integer key : remoteTaskMapper.keySet()) {
                final RemoteTask remoteTask = remoteTaskMapper.get(key);
                if (remoteTask != null && remoteTask.callback != null) {
                    JSONBuilder jsonBuilder = new JSONBuilder(data);
                    int remoteTaskId = jsonBuilder.getVal("taskId", Integer.class);
                    if (remoteTask.remoteId == remoteTaskId) {
                        if (UPLOAD_CALLBACK_EVENT.equals(event)) {
                            notifyUploadCallback(remoteTask, jsonBuilder);
                        } else if (REGISTER_CALLBACK_EVENT.equals(event)) {
                            notifyRegisterCallback(remoteTask, jsonBuilder);
                        } else if (VERIFY_CALLBACK_EVENT.equals(event)) {
                            notifyVerifyCallback(remoteTask, jsonBuilder);
                        } else if (GET_ALL_USER_CALLBACK_EVENT.equals(event)) {
                            notifyGetAllUserCallback(remoteTask, jsonBuilder);
                        } else if (REMOVE_USER_CALLBACK_EVENT.equals(event)) {
                            notifyRemoveUserCallback(remoteTask, jsonBuilder);
                        } else if (GROUP_REMOVE_CALLBACK_EVENT.equals(event)) {
                            notifyGroupRemoveCallback(remoteTask, jsonBuilder);
                        } else if (GROUP_ADD_CALLBACK_EVENT.equals(event)) {
                            notifyGroupAddCallback(remoteTask, jsonBuilder);
                        } else if (RESET_USER_CALLBACK_EVENT.equals(event)) {
                            notifyResetUserCallback(remoteTask, jsonBuilder);
                        }
                        remoteTaskMapper.remove(key);
                        break;
                    }
                }
            }
            return null;
        }
    }



    private static void sendInvoke(String command, byte[] data, final TXZVoiceprintRecognitionManager.ResultCallback callback) {
        int localTaskId = ServiceManager.getInstance().sendInvoke(TXZ, command,
                data, new ServiceManager.GetDataCallback() {
                    @Override
                    public void onGetInvokeResponse(ServiceManager.ServiceData data) {
                        if (data != null) {
                            synchronized (remoteTaskMapper) {
                                RemoteTask remoteTask = remoteTaskMapper.get(getTaskId());
                                if (remoteTask != null) {
                                    remoteTask.remoteId = data.getInt();
                                }
                            }
                        }
                        if (callback != null && isTimeout()) {
                            callback.onError(1);
                            synchronized (remoteTaskMapper) {
                                remoteTaskMapper.remove(getTaskId());
                            }
                        }
                    }
                });
        if (callback != null) {
            addTask(localTaskId, callback);
        }
    }
}
