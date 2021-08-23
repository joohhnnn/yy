package com.txznet.txz.component.home;

import android.os.SystemClock;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.push_manager.PushManager;
import com.txz.report_manager.ReportManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.AesCBC;
import com.txznet.txz.util.PreferenceUtil;

public class HomeControlManager extends IModule {
    private static final String TAG = "[HomeControlManager]-- ";

    /**
     * 车控家指令处理结果返回协议
     */
    public static final String KEY_RESULT = "result";
    public static final String KEY_DATA = "data";
    public static final String KEY_STATUS = "status";
    public static final String KEY_URL = "url";
    public static final String KEY_RESULT_LIST = "resultList";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_SPEECH = "speech";

    public static final String ACTION_CONTROL = "control";
    public static final String ACTION_AUTHORIZATION = "authorization";

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_WAIT_SPEECH = "wait_speech";
    public static final String STATUS_RUNNING = "running";

    /**
     * 授权结果返回协议
     */
    public static final String KEY_TIPS = "tips";
    public static final String KEY_RESULT_CODE = "result_code";

    /**
     * 执行结果返回协议
     */
    public static final String KEY_RESPONSE = "response";
    public static final String KEY_TIMEOUT = "timeout";


    private int mCurrentSessionId;
    private JSONObject mReportJsonObject;

    public JSONObject getReportJsonObject() {
        return mReportJsonObject;
    }

    public void setReportJsonObject(JSONObject reportJsonObject) {
        mReportJsonObject = reportJsonObject;
    }

    public int getCurrentSessionId() {
        return mCurrentSessionId;
    }

    public void setCurrentSessionId(int currentSessionId) {
        mCurrentSessionId = currentSessionId;
    }

    private static HomeControlManager sInstance = new HomeControlManager();

    public static HomeControlManager getInstance() {
        return sInstance;
    }

    public boolean isWaitSpeech() {
        return mIsWaitSpeech;
    }

    public void setWaitSpeech(boolean waitSpeech) {
        mIsWaitSpeech = waitSpeech;
    }

    private boolean mIsWaitSpeech = false;
    private boolean mIsRunning = false;

    public boolean isRunning() {
        return mIsRunning;
    }

    public void setRunning(boolean running) {
        mIsRunning = running;
    }

    @Override
    public int initialize_BeforeStartJni() {
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
                UiEquipment.SUBEVENT_NOTIFY_HOME_CONTROL_AUTHORIZATION_RESULT);
        regCommand("CANCEL_AUTHORIZATION");
        return super.initialize_BeforeStartJni();
    }

    @Override
    public int onCommand(String cmd) {
        if ("CANCEL_AUTHORIZATION".equals(cmd)) {
            if (TextUtils.isEmpty(ProjectCfg.getFangdeToken())) {
                RecorderWin.speakTextWithClose(NativeData.getResString("RS_NULL_AUTHORIZATION"), null);
            } else {
                notifyDeleteToken();
                AppLogic.runOnBackGround(mDeleteTokenFailRunnable, 2000);
            }

        }
        return super.onCommand(cmd);
    }

    private void saveToken(String token) {
        PreferenceUtil.getInstance().setString(PreferenceUtil.KEY_FANGDE_TOKEN, token);
        ProjectCfg.setFangdeToken(token);
        if (!TextUtils.isEmpty(token)) {
            notifyGetToken();
        }
    }

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        switch (eventId) {
            case UiEvent.EVENT_ACTION_EQUIPMENT:
                switch (subEventId) {
                    case UiEquipment.SUBEVENT_NOTIFY_HOME_CONTROL_AUTHORIZATION_RESULT:
                        JSONObject resultJson = null;
                        try {
                            PushManager.PushCmd_AuthorizationResult pushCmd_authorizationResult = PushManager.PushCmd_AuthorizationResult.parseFrom(data);
                            byte[] result = pushCmd_authorizationResult.strResult;
                            if (null != result && result.length > 0) {

                                resultJson = JSONObject.parseObject(new String(result));
                                JNIHelper.logd(TAG + "result:" + resultJson.toJSONString());
                            } else {
                                JNIHelper.logd(TAG + "result is null");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (InvalidProtocolBufferNanoException e) {
                            e.printStackTrace();
                        }
                        if (null != resultJson) {
                            String type = resultJson.getString("type");
                            if ("executeResult".equals(type)) {
                                handleExecuteResult(resultJson);
                            } else {
                                handleAuthorizationResult(resultJson);
                            }
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
        return super.onEvent(eventId, subEventId, data);
    }

    private void handleAuthorizationResult(JSONObject resultJson) {
        Integer resultCode = resultJson.getInteger(KEY_RESULT_CODE);
        if (resultCode == 0) {
            if (AuthorizationViewManager.getAuthorizationViewManager().isSelecting()) {
                AuthorizationViewManager.getAuthorizationViewManager().close();
            }
            String token = resultJson.getString(KEY_TOKEN);
            JNIHelper.logd(TAG+ "update token =" + token);
            saveToken(token);
        }
    }

    public void showAuthorization(String url) {
        saveToken("");
        AuthorizationViewManager.getAuthorizationViewManager().show(url);
    }

    public boolean handleControlResult(JSONObject data, String status) {
        JSONArray resultList = data.getJSONArray(KEY_RESULT_LIST);
        if (null == resultList || resultList.isEmpty()) {
            JNIHelper.logd(TAG +"the resultList object is null or empty");
        } else {
            if (STATUS_SUCCESS.equals(status)) {
                setWaitSpeech(false);
                StringBuilder stringBuilder = handleResultList(resultList);
                String display = resultList.getJSONObject(resultList.size() - 1).getString(KEY_SPEECH);
                stringBuilder.append(display);
                AsrManager.getInstance().setNeedCloseRecord(true);
                RecorderWin.speakTextNotEqualsDisplay(stringBuilder.toString(), display);
                return true;
            } else if (STATUS_WAIT_SPEECH.equals(status)) {
                setWaitSpeech(true);
                StringBuilder stringBuilder = handleResultList(resultList);
                String display = resultList.getJSONObject(resultList.size() - 1).getString(KEY_SPEECH);
                stringBuilder.append(display);
                AsrManager.getInstance().setNeedCloseRecord(false);
                RecorderWin.speakTextNotEqualsDisplay(stringBuilder.toString(), display);
                return true;
            } else if (STATUS_RUNNING.equals(status)) {
                setWaitSpeech(false);
                setRunning(true);
                mLastRunningTime = SystemClock.elapsedRealtime();
                String speech = resultList.getJSONObject(0).getString(KEY_SPEECH);
                AsrManager.getInstance().setNeedCloseRecord(false);
                mRunningTtsEnd = false;
                RecorderWin.speakTextWithClose(speech, false, new Runnable() {
                    @Override
                    public void run() {
                        mRunningTtsEnd = true;
                        ExecuteResult();
                        if (isRunning()) {
                            long interval = SystemClock.elapsedRealtime() - mLastRunningTime;
                            AppLogic.removeBackGroundCallback(mNetworkTimeoutRunnable);
                            if (mHasResult) {
                                return;
                            }
                            if (interval >= 3000) {
                                AppLogic.runOnBackGround(mNetworkTimeoutRunnable);
                            } else {
                                AppLogic.runOnBackGround(mNetworkTimeoutRunnable, 3000 - interval);
                            }
                        }
                    }
                });

                return true;
            }
        }
        return false;
    }

    private synchronized void ExecuteResult() {
        if (mRunningTtsEnd && mHasResult) {
            AsrManager.getInstance().setNeedCloseRecord(!isWaitSpeech());
            doReport();
            RecorderWin.speakTextWithClose(mExecuteResult, null);
            AppLogic.removeBackGroundCallback(mNetworkTimeoutRunnable);
            setRunning(false);
            mRunningTtsEnd = false;
            mHasResult = false;
        }
    }

    private void handleExecuteResult(JSONObject resultJson) {
        try {
            Integer timeout = resultJson.getInteger(KEY_TIMEOUT);
            if (timeout == 0) {
                String responseStr = resultJson.getString(KEY_RESPONSE);
                JSONObject response = JSONObject.parseObject(responseStr);
                setReportJsonObject(response);
                Integer sessionId = response.getInteger("id");
                String status = response.getJSONObject(KEY_RESULT).getString(KEY_STATUS);
                mExecuteResult = response.getJSONObject(KEY_RESULT).getJSONObject(KEY_DATA).getJSONArray(KEY_RESULT_LIST).getJSONObject(0).getString(KEY_SPEECH);

                if (sessionId == mCurrentSessionId) {
                    if (STATUS_WAIT_SPEECH.equals(status)) {
                        setWaitSpeech(true);
                    } else {
                        setWaitSpeech(false);
                    }
                    mHasResult = true;
                    ExecuteResult();
                } else {
                    JNIHelper.logd(TAG + "no current session id!");
                }
            } else {
                JNIHelper.logd(TAG + "timeout exception");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean mRunningTtsEnd = false;
    private boolean mHasResult = false;
    private String mExecuteResult = "";

    public void resetState() {
        mIsWaitSpeech = false;
        clearRunningState();
    }

    private void clearRunningState() {
        mIsRunning = false;
        mHasResult = false;
        mExecuteResult = "";
        AppLogic.removeBackGroundCallback(mNetworkTimeoutRunnable);
    }

    private long mLastRunningTime;

    private Runnable mNetworkTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_IN_NET"), null);
            buildTimeoutReportJson();
            doReport();
            mCurrentSessionId = -1;
            setRunning(false);
        }
    };

    private StringBuilder handleResultList(JSONArray resultList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < resultList.size() - 1; i++) {
            String speech = resultList.getJSONObject(i).getString(KEY_SPEECH);
            if (TextUtils.isEmpty(speech)) {
                JNIHelper.logd(TAG + "The speech is null or empty" + i);
                continue;
            }
            stringBuilder.append(speech);
            RecorderWin.addSystemMsg(speech);
        }
        return stringBuilder;
    }

    private RequestQueue queue = Volley.newRequestQueue(GlobalContext.get());

    /**
     * 通知后台已经收到推送的token，无需继续推送
     */
    private void notifyGetToken() {
        String url = null;
        try {
            url = "http://thirdparty.txzing.com/nlp/fangde/push_token_ok?uid=" + AesCBC.encrypt(String.valueOf(ProjectCfg.getUid()), "utf-8", AesCBC.sKey, AesCBC.ivParameter);
            JNIHelper.logd(TAG + "request url:" + url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                JNIHelper.logd(TAG + "responese : " + s);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                JNIHelper.logd(TAG + "onErrorResponse" + volleyError.getMessage());
            }
        });
        request.setTag("volleyget");
        queue.add(request);
    }

    /**
     * 通知后台删除token
     */
    private void notifyDeleteToken() {
        String url = null;
        try {
            url = "http://thirdparty.txzing.com/nlp/fangde/async_delete_token?uid=" + AesCBC.encrypt(String.valueOf(ProjectCfg.getUid()), "utf-8", AesCBC.sKey, AesCBC.ivParameter);
            JNIHelper.logd(TAG + "request url:" + url);
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                JNIHelper.logd(TAG + "responese : " + s);
                try {
                    JSONObject result = JSONObject.parseObject(s);
                    Integer errno = result.getInteger("errno");
                    if (errno != 0) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (mDeleteTokenFailRunnable != null) {
                    AppLogic.removeBackGroundCallback(mDeleteTokenFailRunnable);
                }
                saveToken("");
                RecorderWin.speakTextWithClose(NativeData.getResString("RS_CANCEL_AUTHORIZATION"), null);
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                JNIHelper.logd(TAG + "onErrorResponse" + volleyError.getMessage());
                if (mDeleteTokenFailRunnable != null) {
                    AppLogic.removeBackGroundCallback(mDeleteTokenFailRunnable);
                }
                AppLogic.runOnBackGround(mDeleteTokenFailRunnable);
            }
        });
        request.setTag("volleydelete");
        queue.add(request);
    }

    private Runnable mDeleteTokenFailRunnable = new Runnable() {
        @Override
        public void run() {
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_CHECK_NET"), null);
        }
    };

    private void doReport() {
        try {
            if (mReportJsonObject != null) {
                ReportUtil.doReport(ReportManager.UAT_COMMON, new org.json.JSONObject(mReportJsonObject.toJSONString()));
            }
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    private void buildTimeoutReportJson() {
        JSONObject dataJsonObject = mReportJsonObject.getJSONObject(KEY_RESULT).getJSONObject(KEY_DATA);
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_SPEECH, "抱歉，网络连接失败");
        jsonArray.add(jsonObject);
        dataJsonObject.put(KEY_RESULT_LIST, jsonArray);
    }

}
