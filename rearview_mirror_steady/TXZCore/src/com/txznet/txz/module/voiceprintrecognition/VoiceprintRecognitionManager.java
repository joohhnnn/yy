package com.txznet.txz.module.voiceprintrecognition;

import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.VoiceprintRecognitionUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.voiceprintrecognition.voiceai.VPRCSDKWrapper;
import com.txznet.txz.module.voiceprintrecognition.voiceai.VoiceAIImpl;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class VoiceprintRecognitionManager extends IModule {
    private static VoiceprintRecognitionManager sInstance = new VoiceprintRecognitionManager();
    private IVoiceprintRecognition mVoiceprintRecognition;

    private final Set<String> mRemotePackageNames = new HashSet<String>();
    public static VoiceprintRecognitionManager getInstance() {
        return sInstance;
    }
    private String mRecordPath;

    @Override
    public int initialize_AfterInitSuccess() {
        initializeComponent();
        return super.initialize_AfterInitSuccess();
    }

    public synchronized void initializeComponent() {
        if (!isVoiceprintRecognitionEnable() || mVoiceprintRecognition != null) {
            return ;
        }
        TXZAudioRecorder.addRecordFileStateListener(new TXZAudioRecorder.RecordFileStateListener() {
            @Override
            public void saveSuccess(File file) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("path", file.getAbsolutePath());
                    mRecordPath = file.getAbsolutePath();
                    LogUtil.e("save record file success " + mRecordPath);
                    for (String remotePackageName : mRemotePackageNames) {
                        LogUtil.d("skyward remotePackageName " + remotePackageName);
                        ServiceManager.getInstance().sendInvoke(remotePackageName, VoiceprintRecognitionUtil.RECORD_FILE_STATE_CALLBACK_EVENT, jsonObject.toString().getBytes(), null);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void saveFail(File file) {
                mRecordPath = null;
                if (file != null) {
                    LogUtil.e("save record file failed " + file.getAbsolutePath());
                }
                for (String remotePackageName : mRemotePackageNames) {
                    ServiceManager.getInstance().sendInvoke(remotePackageName, VoiceprintRecognitionUtil.RECORD_FILE_STATE_CALLBACK_EVENT, null, null);
                }
            }
        });
        regCommand("CMD_GUESS_GENDER");
        regCommand("CMD_GUESS_DRESS");
        mVoiceprintRecognition = new VoiceAIImpl();
        mVoiceprintRecognition.init();
    }


    @Override
    public int onCommand(String cmd) {
        if (mVoiceprintRecognition == null || !isVoiceprintRecognitionEnable()) {
            RecorderWin.speakText(NativeData.getResString("RS_VOICE_OPERATION_UNSUPPORT"), null);
            LogUtil.e("VoiceprintRecognition initialization not completed or not enable");
            return super.onCommand(cmd);
        }
        if ("CMD_GUESS_GENDER".equals(cmd)) {
            mVoiceprintRecognition.upload(mRecordPath, new VoiceAIImpl.UploadStateListener() {
                @Override
                public void onSuccess(String fileID) {
                    mVoiceprintRecognition.guessGender(Collections.singletonList(fileID), new VoiceAIImpl.ExecuteResultCallback<String>() {
                        @Override
                        public void onSuccess(String s) {
                            if ("men".equals(s)) {
                                RecorderWin.speakText("听你的声音，是个男的", null);
                            } else {
                                RecorderWin.speakText("听你的声音，是个女的", null);
                            }
                        }

                        @Override
                        public void onFail(int errorCode) {
                            LogUtil.e("CMD_GUESS_GENDER guessGender" + errorCode);
                            RecorderWin.speakText("网络错误，请重试", null);
                        }
                    });
                }

                @Override
                public void onFail(int errorCode) {
                    RecorderWin.speakText("网络错误，请重试", null);
                    LogUtil.e("CMD_GUESS_GENDER 文件上传失败" + errorCode);
                }


                @Override
                public void onProgress(int i) {

                }
            });
        } else if ("CMD_GUESS_DRESS".equals(cmd)) {
            mVoiceprintRecognition.upload(mRecordPath, new VoiceAIImpl.UploadStateListener() {
                @Override
                public void onSuccess(String fileID) {
                    mVoiceprintRecognition.guessGender(Collections.singletonList(fileID), new VoiceAIImpl.ExecuteResultCallback<String>() {
                        @Override
                        public void onSuccess(String s) {
                            if ("men".equals(s)) {
                                RecorderWin.speakText("男的穿裙子很奇怪，不建议", null);
                            } else {
                                RecorderWin.speakText("粉色的裙子更适合你", null);
                            }
                        }

                        @Override
                        public void onFail(int errorCode) {
                            LogUtil.e("CMD_GUESS_DRESS guessGender" + errorCode);
                            RecorderWin.speakText("网络错误，请重试", null);
                        }
                    });
                }

                @Override
                public void onFail(int errorCode) {
                    LogUtil.e("CMD_GUESS_DRESS 文件上传失败" + errorCode);
                    RecorderWin.speakText("网络错误，请重试", null);
                }


                @Override
                public void onProgress(int i) {

                }
            });
        }
        return super.onCommand(cmd);
    }
    private int mTaskId;

    private byte[] processUpload(final String packageName, byte[] data) {
        final int remoteTaskId = mTaskId++;
        String path = null;
        if (data != null) {
            try {
                JSONBuilder json = new JSONBuilder(new String(data));
                path = json.getVal("path", String.class, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        uploadByRemote(path, remoteTaskId, packageName);
        return String.valueOf(remoteTaskId).getBytes();
    }

    private byte[] processResetUser(String packageName, byte[] data) {
        final int remoteTaskId = mTaskId++;
        String username = null;
        String clientId = null;
        String groupId = null;
        List<String> fileIds = null;
        try {
            fileIds = new ArrayList<String>();
            JSONBuilder json = new JSONBuilder(new String(data));
            username = json.getVal("username",String.class, null);
            clientId = json.getVal("clientId", String.class, null);
            groupId = json.getVal("groupId", String.class, null);
            JSONArray jsonArray = json.getJSONObject().getJSONArray("fileIds");
            for (int i = 0; i < jsonArray.length(); i++) {
                fileIds.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        resetUserByRemote(clientId, username, groupId, fileIds, remoteTaskId, packageName);
        return String.valueOf(remoteTaskId).getBytes();
    }

    private byte[] processRegister(final String packageName, byte[] data) {
        final int remoteTaskId = mTaskId++;
        String username = null;
        String describe = null;
        String groupId = null;
        List<String> fileIds = null;
        try {
            fileIds = new ArrayList<String>();
            JSONBuilder json = new JSONBuilder(new String(data));
            username = json.getVal("username",String.class, null);
            describe = json.getVal("describe", String.class, null);
            groupId = json.getVal("groupId", String.class, null);
            JSONArray jsonArray = json.getJSONObject().getJSONArray("fileIds");
            for (int i = 0; i < jsonArray.length(); i++) {
                fileIds.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        registerByRemote(username, describe, groupId, fileIds, remoteTaskId, packageName);
        return String.valueOf(remoteTaskId).getBytes();
    }

    private byte[] processVerify(final String packageName, byte[] data) {
        final int remoteTaskId = mTaskId++;
        try {
            JSONBuilder json = new JSONBuilder(new String(data));
            String clientId = json.getVal("clientId", String.class, null);
            String fileId = json.getVal("fileId", String.class, null);
            String groupId = json.getVal("groupId", String.class, null);
            verifyByRemote(clientId, groupId, fileId, remoteTaskId, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.valueOf(remoteTaskId).getBytes();
    }

    private byte[] processRemoveUser(final String packageName, byte[] data) {
        final int remoteTaskId = mTaskId++;
        String groupId = null;
        String clientId = null;
        if (data != null) {
            JSONBuilder jsonBuilder = new JSONBuilder(new String(data));
            groupId = jsonBuilder.getVal("groupId", String.class, null);
            clientId = jsonBuilder.getVal("clientId", String.class, null);
        }
        removeUserByRemote(clientId, groupId, remoteTaskId, packageName);
        return String.valueOf(remoteTaskId).getBytes();
    }

    private byte[] processGetAllUser(final String packageName, byte[] data) {
        final int remoteTaskId = mTaskId++;
        String groupId = null;
        if (data != null) {
            JSONBuilder jsonBuilder = new JSONBuilder(new String(data));
            groupId = jsonBuilder.getVal("groupId", String.class, null);
        }
        getAllUserByRemote(groupId, remoteTaskId, packageName);
        return String.valueOf(remoteTaskId).getBytes();
    }

    private byte[] processGroupAdd(String packageName, byte[] data) {
        final int remoteTaskId = mTaskId++;
        String groupName = null;
        String describe = null;
        if (data != null) {
            JSONBuilder jsonBuilder = new JSONBuilder(new String(data));
            groupName = jsonBuilder.getVal("groupName", String.class, null);
            describe = jsonBuilder.getVal("describe", String.class, null);
        }
        groupAddByRemote(groupName, describe, remoteTaskId, packageName);
        return String.valueOf(remoteTaskId).getBytes();
    }

    private byte[] processGroupRemove(String packageName, byte[] data) {
        final int remoteTaskId = mTaskId++;
        String groupId = null;
        if (data != null) {
            JSONBuilder jsonBuilder = new JSONBuilder(new String(data));
            groupId = jsonBuilder.getVal("groupId", String.class, null);
        }
        groupRemoveByRemote(groupId, remoteTaskId, packageName);
        return String.valueOf(remoteTaskId).getBytes();

    }

    public byte[] invokeCommVoiceprintRecognition(final String packageName, String command, byte[] data) {
        if (mVoiceprintRecognition == null || !isVoiceprintRecognitionEnable()) {
            LogUtil.e("VoiceprintRecognition initialization not completed or not enable" + command);
            return null;
        }
        if (command.equals(VoiceprintRecognitionUtil.SEND_UPLOAD)) {
            return processUpload(packageName, data);
        }
        if (command.equals(VoiceprintRecognitionUtil.SEND_RESET_USER)) {
            return processResetUser(packageName, data);
        }
        if (command.equals(VoiceprintRecognitionUtil.SEND_REGISTER)) {
            return processRegister(packageName, data);
        }
        if (command.equals(VoiceprintRecognitionUtil.SEND_VERIFY)) {
            return processVerify(packageName, data);
        }
        if (command.equals(VoiceprintRecognitionUtil.SEND_GET_ALL_USER)) {
            return processGetAllUser(packageName, data);
        }
        if (command.equals(VoiceprintRecognitionUtil.SEND_REMOVE_USER)) {
            return processRemoveUser(packageName, data);
        }
        if (command.equals(VoiceprintRecognitionUtil.SEND_GROUP_ADD)) {
            return processGroupAdd(packageName, data);
        }
        if (command.equals(VoiceprintRecognitionUtil.SEND_GROUP_REMOVE)) {
            return processGroupRemove(packageName, data);
        }
        if (command.equals(VoiceprintRecognitionUtil.SEND_SET_RECORD_FILE)) {
            synchronized (mRemotePackageNames) {
                mRemotePackageNames.add(packageName);
            }
        }
        if (command.equals(VoiceprintRecognitionUtil.REMOVE_RECORD_FILE_STATE_SUFFIX)) {
            synchronized (mRemotePackageNames) {
                mRemotePackageNames.remove(packageName);
            }
        }
        if (command.equals(VoiceprintRecognitionUtil.SEND_GET_CORE_GROUP_ID)) {
            if (canProcessCoreData(packageName)) {
                if (VPRCSDKWrapper.getInstance().getCoreGroupId() != null)
                    return VPRCSDKWrapper.getInstance().getCoreGroupId().getBytes();
                else {
                    return PreferenceUtil.getInstance().getVoiceRecognitionCoreGroupId().getBytes();
                }
            } else {
                return null;
            }
        }
        return null;
    }



    public boolean canProcessCoreData(String packageName) {
        if (packageName == null) return false;
        return packageName.startsWith("com.txznet") || packageName.equals(ProjectCfg.getSDKSettingPackage()) || packageName.equals(
                mVoiceprintRecognitionPackageName);
    }

    private void getAllUserByRemote(String groupId, int remoteTaskId, final String packageName) {
        final JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("taskId", remoteTaskId);
        IVoiceprintRecognition.ExecuteResultCallback callback = new IVoiceprintRecognition.ExecuteResultCallback<List<VPRCSDKWrapper.NativeUser>>() {

            @Override
            public void onSuccess(List<VPRCSDKWrapper.NativeUser> users) {
                try {
                    if (users != null && !users.isEmpty()) {
                        JSONArray jsonArray = new JSONArray();
                        for (VPRCSDKWrapper.NativeUser user : users) {
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("clientId", user.getClientId());
                            jsonObject.put("clientName", user.getUserName());
                            jsonObject.put("describe", user.getDescribe());
                            jsonObject.put("originalName", user.getOriginalName());
                            jsonArray.put(jsonObject);
                        }
                        jsonBuilder.put("users", jsonArray);
                    }
                    jsonBuilder.put("errorCode", 0);
                    ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.GET_ALL_USER_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
                } catch (JSONException e) {
                    e.printStackTrace();
                    jsonBuilder.put("errorCode", -1);
                    ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.GET_ALL_USER_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
                }
            }

            @Override
            public void onFail(int errorCode) {
                jsonBuilder.put("errorCode", errorCode);
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.GET_ALL_USER_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }
        };
        mVoiceprintRecognition.getAllUser(groupId, callback);
    }


    private void resetUserByRemote(String clientId, String username, String groupId, List<String> fileIds, int remoteTaskId, final String packageName) {
        final JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("taskId", remoteTaskId);
        IVoiceprintRecognition.ExecuteResultCallback callback = new IVoiceprintRecognition.ExecuteResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                jsonBuilder.put("errorCode", 0);
                jsonBuilder.put("result", s);
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.RESET_USER_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }

            @Override
            public void onFail(int errorCode) {
                jsonBuilder.put("errorCode", errorCode);
                jsonBuilder.put("result", "false");
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.RESET_USER_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }
        };
        mVoiceprintRecognition.resetUser(clientId, username, groupId, fileIds, callback);
    }

    private void registerByRemote(String username, String describe, String groupId, List<String> fileIds, final int remoteTaskId, final String packageName) {
        final JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("taskId", remoteTaskId);
        IVoiceprintRecognition.ExecuteResultCallback callback = new IVoiceprintRecognition.ExecuteResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                jsonBuilder.put("errorCode", 0);
                jsonBuilder.put("clientId", s);
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.REGISTER_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }

            @Override
            public void onFail(int errorCode) {
                jsonBuilder.put("errorCode", errorCode);
                jsonBuilder.put("clientId", null);
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.REGISTER_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }
        };
        mVoiceprintRecognition.register(username, describe, groupId, fileIds, callback);
    }

    private void uploadByRemote(String path, final int remoteTaskId, final String packageName) {
        LogUtil.d("skyward" + path);
        VoiceAIImpl.UploadStateListener listener = new VoiceAIImpl.UploadStateListener() {
            @Override
            public void onSuccess(String fileID) {
                uploadResult(remoteTaskId, fileID, VoiceAIImpl.CODE_UPLOAD_SUCCESS, packageName);
            }

            @Override
            public void onFail(int errorCode) {
                uploadResult(remoteTaskId, null, errorCode, packageName);
            }

            @Override
            public void onProgress(int i) {

            }
        };
        mVoiceprintRecognition.upload(path, listener);
    }

    private void uploadResult(int remoteTaskId, String fileId, int errorCode, String packageName) {
        try {
            JSONBuilder jsonBuilder = new JSONBuilder();
            jsonBuilder.put("taskId", remoteTaskId);
            jsonBuilder.put("errorCode", errorCode);
            jsonBuilder.put("fileId", fileId);
            ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.UPLOAD_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verifyByRemote(String clientId, String groupId, String fileId, final int remoteTaskId, final String packageName) {
        final JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("taskId", remoteTaskId);
        IVoiceprintRecognition.ExecuteResultCallback executeResultCallback = new IVoiceprintRecognition.ExecuteResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                jsonBuilder.put("errorCode", 0);
                jsonBuilder.put("result", s);
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.VERIFY_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }

            @Override
            public void onFail(int errorCode) {
                jsonBuilder.put("errorCode", errorCode);
                jsonBuilder.put("result", "false");
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.VERIFY_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }
        };
        mVoiceprintRecognition.verify(clientId, groupId, fileId, executeResultCallback);
    }

    private void removeUserByRemote(String clientId, String groupId, final int remoteTaskId, final String packageName) {
        final JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("taskId", remoteTaskId);
        IVoiceprintRecognition.ExecuteResultCallback executeResultCallback = new IVoiceprintRecognition.ExecuteResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                jsonBuilder.put("errorCode", 0);
                jsonBuilder.put("result", s);
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.REMOVE_USER_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }

            @Override
            public void onFail(int errorCode) {
                jsonBuilder.put("errorCode", errorCode);
                jsonBuilder.put("result", "false");
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.REMOVE_USER_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }
        };
        mVoiceprintRecognition.removeUser(clientId, groupId, executeResultCallback);
    }

    private void groupAddByRemote(String groupName, String describe, int remoteTaskId, final String packageName) {
        final JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("taskId", remoteTaskId);
        IVoiceprintRecognition.ExecuteResultCallback executeResultCallback = new IVoiceprintRecognition.ExecuteResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                jsonBuilder.put("errorCode", 0);
                jsonBuilder.put("groupId", s);
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.GROUP_ADD_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }

            @Override
            public void onFail(int errorCode) {
                jsonBuilder.put("errorCode", errorCode);
                jsonBuilder.put("groupId", null);
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.GROUP_ADD_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }
        };
        if (groupName == null || groupName.equals(String.valueOf(ProjectCfg.getUid()))) {
            executeResultCallback.onFail(-1);
            return;
        }
        mVoiceprintRecognition.groupAdd(groupName, describe, executeResultCallback);
    }

    private void groupRemoveByRemote(String groupId, int remoteTaskId, final String packageName) {
        final JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("taskId", remoteTaskId);
        IVoiceprintRecognition.ExecuteResultCallback executeResultCallback = new IVoiceprintRecognition.ExecuteResultCallback<String>() {
            @Override
            public void onSuccess(String s) {
                jsonBuilder.put("errorCode", 0);
                jsonBuilder.put("result", s);
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.REMOVE_USER_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }

            @Override
            public void onFail(int errorCode) {
                jsonBuilder.put("errorCode", errorCode);
                jsonBuilder.put("result", "false");
                ServiceManager.getInstance().sendInvoke(packageName, VoiceprintRecognitionUtil.REMOVE_USER_CALLBACK_EVENT, jsonBuilder.toBytes(), null);
            }
        };
        mVoiceprintRecognition.groupRemove(groupId, executeResultCallback);
    }

    public boolean isVoiceprintRecognitionEnable() {
        UiEquipment.ServerConfig serverConfig = ConfigManager.getInstance().getServerConfig();
        return serverConfig != null && serverConfig.uint64Flags != null && (serverConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_ENABLE_VOICE_PRINT) != 0;
    }

    public void setVoiceprintRecognitionPackageName(String packageName) {
        mVoiceprintRecognitionPackageName = packageName;
    }
    private String mVoiceprintRecognitionPackageName = null;
}
