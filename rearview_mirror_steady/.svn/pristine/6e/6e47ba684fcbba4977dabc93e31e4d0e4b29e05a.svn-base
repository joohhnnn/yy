package com.txznet.txz.module.voiceprintrecognition.voiceai;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.module.voiceprintrecognition.IVoiceprintRecognition;
import com.txznet.txz.util.Pcm2Wav;
import com.txznet.txz.util.recordcenter.TXZAudioRecorder;
import com.voiceai.vprcjavasdk.CloudeUser;
import com.voiceai.vprcjavasdk.Config;
import com.voiceai.vprcjavasdk.Progress;
import com.voiceai.vprcjavasdk.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class VoiceAIImpl implements IVoiceprintRecognition {
    private static final String APP_ID = "42bec5025c734d09a6fca0aa96b62171";
    private static final String APP_SECRET = "8da8526709e4063cfd7a77bda8fd5346";
    private static final int SAMPLE_RATE = 16000;
    private String mRecordPath;
    Config mConfig;

    @Override
    public void init() {
        TXZAudioRecorder.addRecordFileStateListener(new TXZAudioRecorder.RecordFileStateListener() {
            @Override
            public void saveSuccess(File file) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("path", file.getAbsolutePath());
                    mRecordPath = file.getAbsolutePath();
                    LogUtil.e("save record file success " + mRecordPath);
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
            }
        });
        mConfig = new Config(
                GlobalContext.get(), APP_ID, APP_SECRET);
        mConfig.setBaseurl("https://api.cloudv2.voiceaitech.com");
        mConfig.setModeltype("model_short_cn_dnn_v2");
        AppLogic.runOnBackGround(mInitRunnable);
    }

    Runnable mInitRunnable = new Runnable() {
        @Override
        public void run() {
            VPRCSDKWrapper.getInstance().init(GlobalContext.get(), mConfig, new Response() {
                @Override
                public void response(String s, Throwable throwable) {
                    if (throwable == null) {
                        VPRCSDKWrapper.getInstance().setCoreGroupId(s);
                        LogUtil.d("VoiceAi init success ：" + s);
                        AppLogic.removeBackGroundCallback(mInitRunnable);
                    } else {
                        LogUtil.e("VoiceAi init failed, please check the network!");
                        AppLogic.runOnBackGround(mInitRunnable, 100);
                        throwable.printStackTrace();
                    }
                }
            });
        }
    };

    public void upload(String path, UploadStateListener listener) {
        if (TextUtils.isEmpty(path) && TextUtils.isEmpty(mRecordPath)) {
            listener.onFail(VoiceAIImpl.CODE_UPLOAD_FILE_NULL);
            return;
        }
        if (TextUtils.isEmpty(path)) {
            LogUtil.d("use core record file");
            path = mRecordPath;
        }

        File file = new File(path);
        if (file.exists()) {
            LogUtil.d("path = " + file.getAbsolutePath());

            if (file.getName().endsWith(".pcm")) {
                String newPath = file.getParentFile().getAbsolutePath() + File.separator + file.getName().replace(".pcm", ".wav");
                boolean encode = Pcm2Wav.encode(file.getAbsolutePath(), newPath, 16000);
                if (encode) {
                    file = new File(newPath);
                } else {
                    LogUtil.e("encode failed");
                    listener.onFail(VoiceAIImpl.CODE_UPLOAD_FILE_BROKEN);
                    return;
                }
            } else if (file.isHidden()) {
                String newPath = file.getParentFile().getAbsolutePath() + File.separator + file.getName() + ".wav";
                boolean encode = Pcm2Wav.encode(file.getAbsolutePath(), newPath, 16000);
                if (encode) {
                    file = new File(newPath);
                } else {
                    LogUtil.e("encode failed");
                    listener.onFail(VoiceAIImpl.CODE_UPLOAD_FILE_BROKEN);
                    return;
                }
            }
            if (file.getName().endsWith(".wav")) {
                upload(file, listener);
            } else {
                LogUtil.e("bad record file format");
                listener.onFail(VoiceAIImpl.CODE_UPLOAD_FILE_BROKEN);
            }
        } else {
            listener.onFail(VoiceAIImpl.CODE_UPLOAD_FILE_NULL);
        }
    }

    /**
     * @param clientId
     * @param groupId
     * @param executeResultCallback
     */
    @Override
    public void removeUser(String clientId, String groupId, final ExecuteResultCallback executeResultCallback) {
        Response response = new Response() {
            @Override
            public void response(String s, Throwable throwable) {
                if (throwable == null) {
                    executeResultCallback.onSuccess(s);
                } else {
                    executeResultCallback.onFail(-1);
                }
            }
        };
            VPRCSDKWrapper.getInstance().removeUser(clientId, groupId, response);

    }

    @Override
    public void getAllUser(final String groupId, final ExecuteResultCallback<List<VPRCSDKWrapper.NativeUser>> executeResultCallback) {
        Response response = new Response() {
            @Override
            public void response(final String s, final Throwable throwable) {
                AppLogic.runOnBackGround(new Runnable() {
                    @Override
                    public void run() {
                        HashMap allUserMap = VPRCSDKWrapper.getInstance().getAllUserMap();
                        List<VPRCSDKWrapper.NativeUser> nativeUsers = null;
                        if (allUserMap.containsKey(groupId)) {
                            nativeUsers = (List<VPRCSDKWrapper.NativeUser>) allUserMap.get(groupId);
                        }
                        if (nativeUsers == null || nativeUsers.isEmpty()) {
                            nativeUsers = VPRCSDKWrapper.getInstance().loadUserInfo(groupId);
                        }
                        if (nativeUsers == null) {
                            nativeUsers = new ArrayList<VPRCSDKWrapper.NativeUser>();
                        }
                        if (throwable == null) {
                            Gson gson = new Gson();
                            List<CloudeUser> users = (List) gson.fromJson(s, (new TypeToken<List<CloudeUser>>() {
                            }).getType());
                            boolean change = false;
                            for (CloudeUser user : users) {
                                String clientId = user.getClientid();
                                VPRCSDKWrapper.NativeUser nativeUser = VPRCSDKWrapper.getInstance().getNativeUser(clientId, nativeUsers);
                                if (nativeUser == null) {
                                    nativeUser = new VPRCSDKWrapper.NativeUser();
                                    nativeUser.setUserName(user.getClient_name());
                                    nativeUser.setClientId(user.getClientid());
                                    nativeUser.setOriginalName(user.getClient_name());
                                    nativeUser.setDescribe(user.getDescribe());
                                    nativeUsers.add(nativeUser);
                                    change = true;
                                }
                            }
                            if (change) {
                                LogUtil.d("skyward " + groupId);
                                VPRCSDKWrapper.getInstance().addToUserMap(groupId, nativeUsers);
                                VPRCSDKWrapper.getInstance().saveUserInfo(groupId, nativeUsers);
                            }
                        }
                        executeResultCallback.onSuccess(nativeUsers);
                    }
                });
            }
        };
        VPRCSDKWrapper.getInstance().getAllUserById(groupId, response);
    }

    @Override
    public void groupAdd(String groupName, String describe, final ExecuteResultCallback<String> executeResultCallback) {
        VPRCSDKWrapper.getInstance().groupAdd(groupName, describe, new Response() {
            @Override
            public void response(String s, Throwable throwable) {
                if (throwable == null) {
                    executeResultCallback.onSuccess(s);
                } else {
                    executeResultCallback.onFail(-1);
                    LogUtil.e(throwable);
                }
            }
        });
    }

    @Override
    public void groupRemove(String groupId, final ExecuteResultCallback<String> executeResultCallback) {
        VPRCSDKWrapper.getInstance().groupRemove(groupId, new Response() {
            @Override
            public void response(String s, Throwable throwable) {
                if (throwable == null) {
                    try {
                        JSONArray jsonArray = (new JSONObject(s)).getJSONArray("succeed_list");
                        if (jsonArray.length() >= 0) {
                            executeResultCallback.onSuccess("true");
                        } else {
                            executeResultCallback.onFail(-2);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        executeResultCallback.onFail(-3);
                    }
                } else {
                    executeResultCallback.onFail(-1);
                    LogUtil.e(throwable);
                }
            }
        });
    }


    @Override
    public void guessGender(List<String> fileIds, final ExecuteResultCallback executeResultCallback) {
        VPRCSDKWrapper.getInstance().analysisGender(SAMPLE_RATE, fileIds, new Response() {
            @Override
            public void response(String s, Throwable throwable) {
                if (throwable == null) {
                    executeResultCallback.onSuccess(s);
                } else {
                    executeResultCallback.onFail(-1);
                    LogUtil.e(throwable);
                }
            }
        });
    }

    /**
     * @param clientId
     * @param groupId
     * @param fileId
     * @param executeResultCallback
     */
    @Override
    public void verify(String clientId, String groupId, String fileId, final ExecuteResultCallback executeResultCallback) {
        Response response = new Response() {
            @Override
            public void response(String s, Throwable throwable) {
                if (throwable == null) {
                    executeResultCallback.onSuccess(s);
                } else {
                    executeResultCallback.onFail(-1);
                }
            }
        };
        if (clientId == null || "".equals(clientId)) {
            VPRCSDKWrapper.getInstance().verifyGroup(groupId, SAMPLE_RATE, Collections.singletonList(fileId), response);
        } else {
            VPRCSDKWrapper.getInstance().verifyUser(clientId, groupId, SAMPLE_RATE, Collections.singletonList(fileId), response);
        }
    }

    /**
     * @param username
     * @param describe
     * @param groupId
     * @param fileIdList
     * @param executeResultCallback
     */
    public void register(String username, String describe, String groupId, List<String> fileIdList, final ExecuteResultCallback executeResultCallback) {
        Response response = new Response() {
            @Override
            public void response(String s, Throwable throwable) {
                if (throwable == null) {
                    executeResultCallback.onSuccess(s);
                } else {
                    executeResultCallback.onFail(-1);
                }
            }
        };
        VPRCSDKWrapper.getInstance().register(username, describe, groupId, SAMPLE_RATE, fileIdList, response);
    }

    /**
     * @param clientId
     * @param username
     * @param groupId
     * @param fileIdList
     * @param executeResultCallback
     */
    public void resetUser(String clientId, String username, String groupId, List<String> fileIdList, final ExecuteResultCallback executeResultCallback) {
        Response response = new Response() {
            @Override
            public void response(String s, Throwable throwable) {
                if (throwable == null) {
                    executeResultCallback.onSuccess(s);
                } else {
                    executeResultCallback.onFail(-1);
                }
            }
        };
        VPRCSDKWrapper.getInstance().resetUser(clientId, username, groupId, SAMPLE_RATE, fileIdList, response);
    }

    public static final int CODE_UPLOAD_SUCCESS = 0;
    public static final int CODE_UPLOAD_FAILED = -1;
    /**
     * 上传文件不存在或者文件对象为空
     */
    public static final int CODE_UPLOAD_FILE_NULL = -2;
    public static final int CODE_UPLOAD_FILE_BROKEN = -3;
    public static final int CODE_UPLOAD_OTHER_EXCEPTION = -4;

    private void upload(final File file, final UploadStateListener listener) {
        if (file != null && file.exists()) {
            VPRCSDKWrapper.getInstance().upload(file, new Response() {
                @Override
                public void response(String s, Throwable throwable) {
                    if (throwable == null) {
                        LogUtil.d("upload success!!! file = " + file.getAbsolutePath() + " result=" + s);
                        try {
                            String fileID = new JSONObject(s).getJSONArray("list").getJSONObject(0).getString("fileid");
                            listener.onSuccess(fileID);
                        } catch (JSONException e) {
                            LogUtil.e("upload failed!!! file = " + file.getAbsolutePath());
                            listener.onFail(CODE_UPLOAD_OTHER_EXCEPTION);
                            e.printStackTrace();
                        }
                    } else {
                        listener.onFail(CODE_UPLOAD_FAILED);
                        throwable.printStackTrace();
                        LogUtil.e("upload failed!!! file = " + file.getAbsolutePath());
                    }
                }
            }, new Progress() {
                @Override
                public void notify(int i) {
                    listener.onProgress(i);
                }
            });
        } else {
            listener.onFail(CODE_UPLOAD_FILE_NULL);
            LogUtil.e("file is null");
        }
    }


}
