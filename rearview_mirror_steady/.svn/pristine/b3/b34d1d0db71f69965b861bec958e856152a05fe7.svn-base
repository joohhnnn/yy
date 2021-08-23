package com.txznet.txz.module.voiceprintrecognition.voiceai;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.util.PreferenceUtil;
import com.voiceai.vprcjavasdk.CloudeUser;
import com.voiceai.vprcjavasdk.Config;
import com.voiceai.vprcjavasdk.Progress;
import com.voiceai.vprcjavasdk.Response;
import com.voiceai.vprcjavasdk.VPRCNet;
import com.voiceai.vprcjavasdk.VPRCNetImpl;
import com.voiceai.vprcjavasdk.VoiceCloudRegisterInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VPRCSDKWrapper {
    private static final VPRCSDKWrapper ourInstance = new VPRCSDKWrapper();
    private final VPRCNet vprcNet = VPRCNetImpl.getInstance();
    private Context context;
    private String mCoreGroupId;
    private Config config;
    private String access_token;
    private static volatile int mUserId = 0;
    private HashMap mAllUserMap = new HashMap<String, List<NativeUser>>();

    public HashMap getAllUserMap() {
        return mAllUserMap;
    }

    public void addToUserMap(String groupId, List<NativeUser> user) {
        mAllUserMap.put(groupId, user);
    }

    public static VPRCSDKWrapper getInstance() {
        return ourInstance;
    }

    private VPRCSDKWrapper() {
    }

    public static String getAccessToken() {
        String accessToken = PreferenceUtil.getInstance().getVoiceRecognitionAccessToken();
        long expiredTime = PreferenceUtil.getInstance().getVoiceRecognitionExpiredTime();
        return expiredTime > System.currentTimeMillis() ? accessToken : "";
    }

    public static void dealAccessToken(VPRCNet vprcNet, Config config, String str) {
        try {
            JSONObject re = new JSONObject(str);
            if (re.getString("access_token").length() > 2) {
                String accessToken = re.getString("access_token");
                PreferenceUtil.getInstance().setVoiceRecognitionAccessToken(accessToken);
                dealTime(vprcNet, config, getExpiredTime(str));
            }
        } catch (JSONException exception) {
            exception.printStackTrace();
        }

    }

    private static void dealTime(final VPRCNet vprcNet, final Config config, long expiredTime) {
        PreferenceUtil.getInstance().setVoiceRecognitionExpiredTime(System.currentTimeMillis() + expiredTime);
        (new Timer()).schedule(new TimerTask() {
            public void run() {
                vprcNet.VPRCRefreshToke(config.getBaseurl(), -1, config.getAppId(), VPRCSDKWrapper.getAccessToken(), new Response() {
                    public void response(String res, Throwable e) {
                        if (e == null) {
                            VPRCSDKWrapper.dealTime(vprcNet, config, VPRCSDKWrapper.getExpiredTime(res));
                        }

                    }
                });
            }
        }, expiredTime);
    }

    private static long getExpiredTime(String res) {
        JSONObject re = null;
        try {
            re = new JSONObject(res);
            long ti = re.getLong("expires");
            ti *= 1000L;
            if (ti > 864000000L) {
                ti = 864000000L;
            }
            if (ti > 700L) {
                ti -= 600L;
            }
            return ti;
        } catch (Exception exception) {
            exception.printStackTrace();
            return 0L;
        }
    }

    public boolean isGroupIdNotExist() {
        return TextUtils.isEmpty(PreferenceUtil.getInstance().getVoiceRecognitionCoreGroupId());
    }


    @SuppressLint({"WrongConstant"})
    public void init(Context context, Config config, final Response response) {
        this.context = context.getApplicationContext();
        this.config = config;
        if (getAccessToken().length() > 0) {
            this.vprcNet.VPRCRefreshToke(this.config.getBaseurl(), -1, this.config.getAppId(), getAccessToken(), new Response() {
                public void response(String res, Throwable e) {
                    if (e == null) {
                        VPRCSDKWrapper.this.access_token = VPRCSDKWrapper.getAccessToken();
                        VPRCSDKWrapper.dealTime(VPRCSDKWrapper.this.vprcNet, VPRCSDKWrapper.this.config, VPRCSDKWrapper.getExpiredTime(res));
                        Long uid = ProjectCfg.getUid();
                        if (uid == null) {
                            response.response("", new Exception("uid is null"));
                            return;
                        }
                        VPRCSDKWrapper.this.searchOrCreate(String.valueOf(uid), "Core Group", response);
                    } else {
                        VPRCSDKWrapper.this.initLogin(response);
                    }

                }
            });
        } else {
            this.initLogin(response);
        }
    }

    public Config getConfig() {
        return this.config;
    }

    private void initLogin(final Response response) {
        this.vprcNet.VPRCSdkLogin(this.config.getBaseurl(), -1, this.config.getAppId(), this.config.getAppSecret(), new Response() {
            public void response(String res, Throwable e) {
                if (e == null) {
                    try {
                        VPRCSDKWrapper.this.access_token = (new JSONObject(res)).getString("access_token");
                        VPRCSDKWrapper.dealAccessToken(VPRCSDKWrapper.this.vprcNet, VPRCSDKWrapper.this.config, res);
                        Long uid = ProjectCfg.getUid();
                        if (uid == null) {
                            response.response("", new Exception("uid is null"));
                            return;
                        }
                        VPRCSDKWrapper.this.searchOrCreate(String.valueOf(uid), "Core Group", response);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    LogUtil.e("init failed" + res + e.toString());
                    if (response != null) {
                        response.response("", e);
                    }
                }

            }
        });
    }

    /**
     * 猜测性别
     *
     * @param sourceSampleRate
     * @param list
     * @param response
     */
    public void analysisGender(int sourceSampleRate, List<String> list, final Response response) {
        this.vprcNet.VPRCSdkAnaly(null, null, sourceSampleRate, this.config.getModeltype(), list, new Response() {
            public void response(String res, Throwable e) {
                if (e == null) {
                    try {
                        Gson gson = new Gson();
                        VoiceCloudRegisterInfo re = (VoiceCloudRegisterInfo) gson.fromJson(res, VoiceCloudRegisterInfo.class);
                        if (response != null) {
                            if (Config.productmode) {
                                response.response(res, e);
                            } else if (re.getFeature().getGender() >= 0.5D) {
                                response.response("women", e);
                            } else {
                                response.response("men", e);
                            }
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    if (response != null) {
                        response.response("", e);
                    }
                    LogUtil.e("analysisGender failed" + res + e.toString());
                }
            }
        });
    }

    public void removeUser(final String clientId, final String groupId, final Response response) {
        this.vprcNet.VPRCSdkClientRemove(Arrays.asList(clientId), groupId, new Response() {
            public void response(String res, Throwable e) {
                if (e == null) {
                    try {
                        JSONArray l = (new JSONObject(res)).getJSONArray("succeed_list");
                        if (response != null) {
                            if (l.length() >= 0) {
                                List<NativeUser> nativeUsers = getNativeUsers(groupId);
                                if (nativeUsers != null) {
                                    NativeUser nativeUser = getNativeUser(clientId, nativeUsers);
                                    if (nativeUser != null) {
                                        nativeUsers.remove(nativeUser);
                                        addToUserMap(groupId, nativeUsers);
                                        saveUserInfo(groupId, nativeUsers);
                                    }
                                }
                                response.response("true", e);
                            } else {
                                response.response("false", new Exception("删除用户失败"));
                            }
                        }
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                } else {
                    if (response != null) {
                        response.response("", e);
                    }
                    LogUtil.e("remove user failed" + res + e.toString());
                }
            }
        });
    }

    public String getCoreGroupId() {
        return mCoreGroupId;
    }

    public void setCoreGroupId(String coreGroupId) {
        mCoreGroupId = coreGroupId;
        PreferenceUtil.getInstance().setVoiceRecognitionCoreGroupId(coreGroupId);
    }


    /////////////////////////////////创建用户组////////////////////////////////////////////////////

    /**
     * 创建用户组
     *
     * @param groupName
     * @param describe
     * @param response
     */
    public void groupAdd(String groupName, String describe, final Response response) {
        searchOrCreate(groupName, describe, response);
    }

    public void searchOrCreate(final String groupName, final String describe, final Response response) {
        this.vprcNet.VPRCSdkGroupSearch(groupName, new Response() {
            public void response(String res, Throwable e) {
                if (e == null) {
                    try {
                        LogUtil.d("groupName: " + groupName);
                        JSONArray p = new JSONArray(res);
                        if (p.length() > 0) {
                            final String groupId = p.getJSONObject(0).getString("groupid");
                            // 如果SharePreference没有保存groupId，同时用户组名为Uid，执行重置逻辑，清空用户数据
                            if (isGroupIdNotExist() && String.valueOf(ProjectCfg.getUid()).equals(groupName)) {
                                // 目前不支持删除非空分组，只能一个个用户删除。
                                LogUtil.d("clear all user data");
                                LogUtil.d("groupId :" + groupId);
                                getAllUserById(groupId, new Response() {
                                    @Override
                                    public void response(String s, Throwable throwable) {
                                        if (throwable == null) {
                                            Gson gson = new Gson();
                                            List<CloudeUser> users = (List) gson.fromJson(s, (new TypeToken<List<CloudeUser>>() {
                                            }).getType());
                                            List<String> ids = new ArrayList<String>();
                                            if (users != null && !users.isEmpty()) {
                                                for (CloudeUser user : users) {
                                                    ids.add(user.getClientid());
                                                }
                                                vprcNet.VPRCSdkClientRemove(ids, groupId, new Response() {
                                                    @Override
                                                    public void response(String s, Throwable throwable) {
                                                        if (throwable == null) {
                                                            String appDir = GlobalContext.get().getApplicationInfo().dataDir;
                                                            File file = new File(appDir + "/" + groupId);
                                                            if (file.exists()) {
                                                                LogUtil.d("delete local data");
                                                                file.delete();
                                                            } else {
                                                                LogUtil.d("file not exist");
                                                            }
                                                        }
                                                        response.response(groupId, throwable);
                                                    }
                                                });
                                            } else {
                                                response.response(groupId, null);
                                            }
                                        } else {
                                            response.response(groupId, throwable);
                                        }
                                    }
                                });
                            } else {
                                response.response(groupId, e);
                            }
                        } else {
                            createGroup(groupName, describe, response);
                        }
                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                } else {
                    if (response != null) {
                        response.response("", e);
                    }
                    LogUtil.e("searchOrCreate failed" + res + e.toString());
                }
            }
        });
    }

    private void createGroup(final String str, String describe, final Response response) {
        this.vprcNet.VPRCSdkGroupAdd(str, describe, new Response() {
            public void response(String res, Throwable e) {
                if (e == null) {
                    try {
                        LogUtil.d(res);
                        response.response((new JSONObject(res)).getString("groupid"), e);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    if (response != null) {
                        response.response("", e);
                    }
                    LogUtil.e("createGroup failed" + res + e.toString());
                }
            }
        });
    }

    /////////////////////////////////删除用户组///////////////////////////////////

    public void groupRemove(String groupId, Response response) {
        this.vprcNet.VPRCSdkGroupRemove(Arrays.asList(groupId), response);
    }


    public void getAllUserById(final String groupId, final Response response) {
        this.vprcNet.VPRCSdkClientGet(groupId, (String) null, new Response() {
            public void response(String res, Throwable e) {
                if (e == null) {
                    try {
                        response.response(res, e);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    LogUtil.e("getAllUserById failed" + res + e.toString());
                    if (response != null) {
                        response.response("", e);
                    }
                }
            }
        });
    }

    ///////////////////////////////////////////////////文件上传/////////////////////////////////////////////////////////////////

    /**
     * 上传声纹文件，返回声纹文件id。声纹文件id用于注册和验证声纹
     *
     * @param file
     * @param response
     * @param progress
     */
    public void upload(File file, Response response, Progress progress) {
        this.vprcNet.VPRCSdkVoiceprintUpload(file, response, progress);
    }
    ///////////////////////////////////////////////////注册声纹/////////////////////////////////////////////////////////////////

    public static class NativeUser {
        private String userName;
        private String originalName;
        private String clientId;
        private String describe;

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

    public static final String appDir = GlobalContext.get().getApplicationInfo().dataDir;

    public List<NativeUser> loadUserInfo(String groupId) {
        LogUtil.d("skyward = " + groupId);
        if (TextUtils.isEmpty(groupId)) {
            return null;
        }
        File file = new File(appDir + "/" + groupId);
        if (!file.exists()) {
            LogUtil.d("user data not exist" + groupId);
            return null;
        }
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
            char[] buffer = new char[1024];
            StringBuilder result = new StringBuilder();
            int read = -1;
            while ((read = fileReader.read(buffer)) > 0) {
                result.append(buffer, 0, read);
            }
            List<NativeUser> users = null;
            LogUtil.d(result);
            JSONArray jsonArray = new JSONArray(result.toString());
            if (jsonArray != null && jsonArray.length() > 0) {
                users = new ArrayList<NativeUser>();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    NativeUser user = new NativeUser();
                    user.setClientId(jsonObject.getString("clientId"));
                    user.setUserName(jsonObject.getString("userName"));
                    user.setDescribe(jsonObject.getString("describe"));
                    user.setOriginalName(jsonObject.getString("originalName"));
                    users.add(user);
                }
            }
            return users;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileReader != null) {
                    fileReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void saveUserInfo(String groupId, List<NativeUser> users) {
        LogUtil.d("saveUserInfo " + groupId);
        if (groupId == null) {
            return;
        }
        if (users == null || users.isEmpty()) {
            return;
        }
        Gson gson = new Gson();
        LogUtil.d(gson.toJson(users));
        File file = new File(appDir + "/" + groupId);
        if (file.exists()) {
            file.delete();
        }
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            fileWriter.write(gson.toJson(users));
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 创建用户
     *
     * @param username
     * @param groupId
     * @param describe
     * @param src_sample_rate
     * @param list
     * @param response
     */
    private void createUser(final String username, final String groupId, final String describe, final int src_sample_rate, final List<String> list, final Response response) {
        this.vprcNet.VPRCSdkClientAdd(username, describe, groupId, new Response() {
            public void response(String res, Throwable e) {
                if (e == null) {
                    try {
                        LogUtil.d("username: " + username + " groupId: " + groupId + " describe: " + describe);
                        LogUtil.d(res);
                        String clientId = (new JSONObject(res)).getString("clientid");
                        List<NativeUser> nativeUsers = getNativeUsers(groupId);
                        if (nativeUsers == null) {
                            nativeUsers = new ArrayList<NativeUser>();
                        }
                        NativeUser nativeUser = new NativeUser();
                        nativeUser.setDescribe(describe);
                        nativeUser.setOriginalName(username);
                        nativeUser.setUserName(username);
                        nativeUser.setClientId(clientId);
                        nativeUsers.add(nativeUser);
                        addToUserMap(groupId, nativeUsers);
                        saveUserInfo(groupId, nativeUsers);
                        VPRCSDKWrapper.this.voiceprintRegister(clientId, groupId, src_sample_rate, list, response);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    if (response != null) {
                        response.response("", e);
                    }
                    LogUtil.e("create user failed" + res + e.toString());
                }

            }
        });
    }

    /**
     * 注册声纹
     *
     * @param clientId
     * @param groupId
     * @param src_sample_rate
     * @param list
     * @param response
     */
    private void voiceprintRegister(final String clientId, final String groupId, int src_sample_rate, List<String> list, final Response response) {
        this.vprcNet.VPRCSdkVoiceprintRegister(clientId, groupId, src_sample_rate, this.config.getModeltype(), list, new Response() {
            public void response(String res, Throwable e) {
                if (e == null) {
                    try {
                        Gson gson = new Gson();
                        VoiceCloudRegisterInfo re = (VoiceCloudRegisterInfo) gson.fromJson(res, VoiceCloudRegisterInfo.class);
                        if (re.isResult()) {
                            response.response(clientId, e);
                        } else {
                            if (response != null) {
                                response.response(null, new NullPointerException(re.getMsg()));
                            }
                            VPRCSDKWrapper.this.removeUser(clientId, groupId, (Response) null);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    if (response != null) {
                        response.response(null, e);
                    }
                    removeUser(clientId, groupId, (Response) null);
                    LogUtil.e("voiceprint register failed" + res + e.toString());
                }

            }
        });
    }

    public List<NativeUser> getNativeUsers(String groupId) {
        List<VPRCSDKWrapper.NativeUser> nativeUsers = null;
        if (mAllUserMap.containsKey(groupId)) {
            nativeUsers = (List<VPRCSDKWrapper.NativeUser>) mAllUserMap.get(groupId);
        }
        if (nativeUsers == null || nativeUsers.isEmpty()) {
            nativeUsers = VPRCSDKWrapper.getInstance().loadUserInfo(groupId);
            if (nativeUsers != null && !nativeUsers.isEmpty()) {
                VPRCSDKWrapper.getInstance().addToUserMap(groupId, nativeUsers);
            }
        }
        return nativeUsers;
    }

    public NativeUser getNativeUser(String clientId, List<NativeUser> users) {
        if (users != null && !users.isEmpty()) {
            for (NativeUser user : users) {
                if (clientId.equals(user.getClientId())) {
                    return user;
                }
            }
        }
        return null;
    }

    public boolean hasUserName(String userName, List<NativeUser> users) {
        if (users != null && !users.isEmpty()) {
            for (NativeUser user : users) {
                if (user.getOriginalName().equals(userName)) {
                    return true;
                }
                if (user.getUserName().equals(userName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param username
     * @param groupId
     * @param src_sample_rate
     * @param list
     * @param response
     */
    public void resetUser(final String clientId, final String username, final String groupId, final int src_sample_rate, final List<String> list, final Response response) {
        final List<NativeUser> nativeUsers = getNativeUsers(groupId);
        final NativeUser nativeUser = getNativeUser(clientId, nativeUsers);
        // 重置之前的声纹用户
        if (nativeUser != null) {
            if (username.equals(nativeUser.getUserName()) || username.equals(nativeUser.getOriginalName()) || !hasUserName(username, nativeUsers)) {
                VPRCSDKWrapper.this.vprcNet.VPRCSdkVoiceprintRegister(clientId, groupId, src_sample_rate, VPRCSDKWrapper.this.config.getModeltype(), list, new Response() {
                    public void response(String res, Throwable e) {
                        if (e == null) {
                            try {
                                Gson gson = new Gson();
                                VoiceCloudRegisterInfo re = (VoiceCloudRegisterInfo) gson.fromJson(res, VoiceCloudRegisterInfo.class);
                                if (re.isResult()) {
                                    nativeUser.setUserName(username);
                                    nativeUsers.remove(nativeUser);
                                    nativeUsers.add(nativeUser);
                                    addToUserMap(groupId, nativeUsers);
                                    saveUserInfo(groupId, nativeUsers);
                                    response.response("true", e);
                                } else {
                                    if (response != null) {
                                        response.response("false", new Exception(re.getMsg()));
                                    }
                                }
                            } catch (Exception exception) {
                                exception.printStackTrace();
                                response.response("false", exception);
                            }
                        } else {
                            if (response != null) {
                                response.response("false", e);
                            }
                            LogUtil.e("voiceprint register failed" + res + e.toString());
                        }
                    }
                });
            } else {
                response.response("false", new Exception("用户名重复"));
            }
        } else {
            response.response("false", new Exception("用户不存在"));
        }
    }

    /**
     * @param username
     * @param groupId
     * @param src_sample_rate
     * @param list
     * @param response
     */
    public void register(final String username, final String describe, final String groupId, final int src_sample_rate, final List<String> list, final Response response) {
        this.vprcNet.VPRCSdkClientSearch(groupId, username, new Response() {
            public void response(String res, Throwable e) {
                if (e == null) {
                    try {
                        JSONArray p = new JSONArray(res);
                        if (p.length() > 0) {
                            response.response("registered", new Exception("用户已注册"));
                        } else {
                            // 创建新的声纹用户
                            VPRCSDKWrapper.this.createUser(username, groupId, describe, src_sample_rate, list, response);
                        }

                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                } else {
                    if (response != null) {
                        response.response("", e);
                    }
                    LogUtil.e("register failed" + res + e.toString());
                }
            }
        });
    }


    ///////////////////////////////////////////////////验证声纹/////////////////////////////////////////////////////////////////

    /**
     * 验证声纹是否匹配指定用户组中的指定用户，匹配返回true，不匹配返回false
     *
     * @param clientId
     * @param groupId
     * @param src_sample_rate
     * @param list
     * @param response
     */
    public void verifyUser(final String clientId, final String groupId, int src_sample_rate, final List<String> list, final Response response) {
        if (clientId == null) {
            response.response("false", new NullPointerException("master not register"));
        } else {
            this.vprcNet.VPRCSdkVoiceprintVerify(clientId, groupId, src_sample_rate, this.config.getModeltype(), list, 5, new Response() {
                public void response(String res, Throwable e) {
                    LogUtil.d("clientid: " + clientId + " groupId: " + groupId + " list: " + list.get(0));
                    if (e == null) {

                        JSONArray array = null;
                        try {
                            LogUtil.d(res);
                            array = (new JSONObject(res)).getJSONArray("matching_list");
                        } catch (Exception exception) {
                            response.response("false", null);
                            exception.printStackTrace();
                            return;
                        }
                        try {
                            if (response != null) {
                                String result = "false";
                                if (array != null && array.length() > 0) {
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject jsonObject = array.getJSONObject(i);
                                        if (clientId.equals(jsonObject.getString("clientid"))) {
                                            double value = jsonObject.getDouble("value");
                                            if (value >= mVoiceprintRecognition) {
                                                result = "true";
                                                break;
                                            }
                                        }
                                    }
                                }
                                response.response(result, (Throwable) null);
                            }
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                    } else {
                        if (response != null) {
                            response.response("", e);
                        }
                        LogUtil.e("verifyUsers" + res + e.toString());
                    }

                }
            });
        }
    }

    public static final double DEFAULT_VOICE_RECOGNITION_SCORE = 70;
    private double mVoiceprintRecognition = DEFAULT_VOICE_RECOGNITION_SCORE;

    public void setVoiceprintRecognition(Double score) {
        if (score != null && score > 0 && score < 100) {
            mVoiceprintRecognition = score;
        } else {
            mVoiceprintRecognition = DEFAULT_VOICE_RECOGNITION_SCORE;
        }
        LogUtil.d("mVoiceprintRecognition=" + mVoiceprintRecognition);
    }


    /**
     * 验证指定用户组中是否存在匹配声纹的用户，返回匹配声纹的用户列表
     *
     * @param groupId
     * @param sourceSampleRate
     * @param list
     * @param response
     */
    public void verifyGroup(String groupId, int sourceSampleRate, List<String> list, final Response response) {
        this.vprcNet.VPRCSdkVoiceprintVerify((String) null, groupId, sourceSampleRate, this.config.getModeltype(), list, 5, new Response() {
            public void response(String res, Throwable e) {
                if (e == null) {
                    try {
                        JSONArray array = null;
                        try {
                            array = (new JSONObject(res)).getJSONArray("matching_list");
                        } catch (Exception exception) {
                            exception.printStackTrace();
                        }
                        LogUtil.d("skyward" + res);
                        JSONArray result = new JSONArray();
                        if (response != null) {
                            if (array != null && array.length() > 0) {
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject jsonObject = array.getJSONObject(i);
                                    double value = jsonObject.getDouble("value");
                                    if (value >= mVoiceprintRecognition) {
                                        result.put(jsonObject);
                                    }
                                }
                            }
                            LogUtil.e("skyward " + result);
                            response.response(result.toString(), null);
                        }
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                } else {
                    if (response != null) {
                        response.response("[]", e);
                    }
                    LogUtil.e("verifyGroup" + res + e.toString());
                }
            }
        });
    }
}

