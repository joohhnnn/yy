package com.txznet.launcher.domain.voip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.domain.upgrade.UpgradeManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.utils.DeviceUtils;
import com.txznet.sdk.TXZAsrManager;
import com.txznet.sdk.TXZCallManager;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.sdk.TXZSceneManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * voip业务管理
 * created zackzhou 2018-5-28
 */
public class VoipManager extends BaseManager {
    private static final String TAG = VoipManager.class.getSimpleName();
    // protocol
    private static final String TERMINATOR = "\r\n\r\n";
    private static final String PROTOCOL_IMEI = "<Init><IMEI>#IMEI#</IMEI><GPSURL>#GPSURL#</GPSURL><WEBURL>#WEBURL#</WEBURL></Init>" + TERMINATOR; // imei传入
    private static final String PROTOCOL_CALL_BLUE = "<StartSIP><SOS>NO</SOS><GDStatus>#GDStatus#</GDStatus></StartSIP>" + TERMINATOR; // 蓝键启动
    private static final String PROTOCOL_CALL_RED = "<StartSIP><SOS>Yes</SOS><GDStatus>#GDStatus#</GDStatus></StartSIP>" + TERMINATOR; // 红键启动
    private static final String BASE_RESP_REGEX = "<Result>(\\w+)</Result>";
    private static final String CALL_RESP_REGEX = "<cmdType>(\\w+)</cmdType><Result>(\\w+)</Result>";

    private static final int CALL_TYPE_BLUE = 0; // 蓝键
    private static final int CALL_TYPE_RED = 1; // 红键
    private int mLastCallType; // 最后一个触发的按键类型
    private static VoipManager sInstance;

    private Handler mWorkHandler;

    private boolean isGDForeground; // 高德是否处于前台状态

    private VoipManager() {
    }

    public static VoipManager getInstance() {
        if (sInstance == null) {
            synchronized (VoipManager.class) {
                if (sInstance == null) {
                    sInstance = new VoipManager();
                }
            }
        }
        return sInstance;
    }

    private ServerSocket server;
    private TXZCallManager.CallToolStatusListener mCallToolStatusListener;
    private TXZCallManager.CallTool.CallStatus mCallStatus = TXZCallManager.CallTool.CallStatus.CALL_STATUS_IDLE;

    @Override
    public void init() {
        super.init();
        // init work thread
        HandlerThread mHandlerThread = new HandlerThread("VoipWorkThread");
        mHandlerThread.start();
        mWorkHandler = new Handler(mHandlerThread.getLooper());

        mWorkHandler.post(mCreateServerTask);

        mWorkHandler.post(mSendImeiTask);


        // 电话工具会处理电话过程中的tts播报和唤醒，但是不会处理音乐。
        TXZCallManager.getInstance().setCallTool(new TXZCallManager.CallTool() {
            @Override
            public CallStatus getStatus() {
                return mCallStatus;
            }

            @Override
            public boolean makeCall(TXZCallManager.Contact con) {
                return false;
            }

            @Override
            public boolean acceptIncoming() {
                return false;
            }

            @Override
            public boolean rejectIncoming() {
                return false;
            }

            @Override
            public boolean hangupCall() {
                return false;
            }

            @Override
            public void setStatusListener(TXZCallManager.CallToolStatusListener listener) {
                mCallToolStatusListener = listener;
            }
        });


        TXZSceneManager.getInstance().setSceneTool(TXZSceneManager.SceneType.SCENE_TYPE_CALL, new TXZSceneManager.SceneTool() {
            @Override
            public boolean process(TXZSceneManager.SceneType type, String data) {
                TXZResourceManager.getInstance().speakTextOnRecordWin("抱歉，当前不支持该操作", true, null);
                return true;
            }
        });
        TXZAsrManager.getInstance().regCommand(new String[]{"打电话"}, "CALL");
        TXZAsrManager.getInstance().addCommandListener(new TXZAsrManager.CommandListener() {
            @Override
            public void onCommand(String cmd, String data) {
                if ("CALL".equals(data)) {
                    TXZResourceManager.getInstance().speakTextOnRecordWin("抱歉，当前不支持该操作", true, null);
                }
            }
        });

        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra("status", -1);
                LogUtil.logd("receive:" + intent.getAction() + ", status=" +  status);
                switch (status) {
                    case 1:
                        // 这里是打电话的返回值，这这里设置成打过电话比较快，
                        if (!hasCallingAnjixingAfterBoot) {
                            hasCallingAnjixingAfterBoot = true;
                        }
                        break;
                    case 2: // 挂断 - 界面退出后才广播
                        if (TXZCallManager.CallTool.CallStatus.CALL_STATUS_IDLE == mCallStatus) { // 已经是空闲状态
                            break;
                        }
                        mCallStatus = TXZCallManager.CallTool.CallStatus.CALL_STATUS_IDLE;
                        if (mCallToolStatusListener != null) {
                            mCallToolStatusListener.onIdle();
                        }
                        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_VOIP_READY);
                        notifyCallStateChange();
                        break;
                }
            }
        }, new IntentFilter("yf.intent.action.ACTION_PHONEON_STATUS"));
    }

    private Runnable mCreateServerTask = new Runnable() {
        @Override
        public void run() {
            if (server != null) {
                return;
            }
            new Thread() {
                @Override
                public void run() {
                    if (server != null) {
                        return;
                    }
                    try {
                        server = new ServerSocket();
                        server.setReuseAddress(true);
                        server.setSoTimeout(Const.TIME_OUT);
                        server.bind(new InetSocketAddress(Const.CLIENT_SERVER_PORT));
                        LogUtil.logw("create local server success, port=" + Const.CLIENT_SERVER_PORT);
                        mWorkHandler.removeCallbacks(mCreateServerTask);
                        while (true) {
                            try {
                                Socket socket = server.accept();
                                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                String resp = reader.readLine();
                                LogUtil.logd("voip, push=" + resp);

                                // 返回的resp可能是null，不能使用正则。
                                if (resp == null) {
                                    continue;
                                }
                                Pattern regex = Pattern.compile("<PhoneReport>(\\w+)</PhoneReport>");
                                Matcher matcher = regex.matcher(resp);
                                if (matcher.find()) {
                                    String keyword = matcher.group(1);
                                    if (!TextUtils.isEmpty(keyword)) {
                                        switch (keyword) {
                                            case "Ready": // 空闲 - 界面退出前就通知
//                                                mCallStatus = TXZCallManager.CallTool.CallStatus.CALL_STATUS_IDLE;
//                                                if (mCallToolStatusListener != null) {
//                                                    mCallToolStatusListener.onIdle();
//                                                }
                                                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_VOIP_BEFORE_READY);
//                                                notifyCallStateChange();
                                                break;
                                            case "Calling": // 呼叫中
                                                mCallStatus = TXZCallManager.CallTool.CallStatus.CALL_STATUS_RINGING;
                                                if (mCallToolStatusListener != null) {
//                                                    mCallToolStatusListener.onMakeCall(null);
                                                    mCallToolStatusListener.onIncoming(null, false, false);
                                                }
                                                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_VOIP_CALLING);
                                                notifyCallStateChange();
                                                break;
                                            case "Talking": // 通话中
                                                mCallStatus = TXZCallManager.CallTool.CallStatus.CALL_STATUS_OFFHOOK;
                                                if (mCallToolStatusListener != null) {
                                                    mCallToolStatusListener.onOffhook();
                                                }
                                                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_VOIP_TALKING);
                                                notifyCallStateChange();
                                                break;
                                            case "RegisterFalse": // 没网、弱网会
                                                // break是和switch一起的，所以while一直在循环。大概是当获取到数据的时候就执行下面的代码。
                                                break;
                                            case "RegisterSuccess":
                                                break;
                                        }
                                    }
                                }
                            } catch (IOException e) {
                            }
                        }
                    } catch (IOException e) {
                        LogUtil.logw("create local server failed, port=" + Const.CLIENT_SERVER_PORT);
                        mWorkHandler.removeCallbacks(mCreateServerTask);
                        mWorkHandler.postDelayed(mCreateServerTask, 3000);
                    } finally {
                        if (server != null) {
                            try {
                                server.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }.start();
        }
    };


    private Socket mSocket;

    /*
     * 一次交互
     * @param data 数据
     * @return 响应
     */
    private synchronized String oneshot(String data) throws IOException {
        LogUtil.logd("voip, send=" + data);
        mSocket = new Socket(Const.VOIP_LOCAL_SERVER_HOST, Const.VOIP_LOCAL_SERVER_PORT);
        mSocket.setSoTimeout(Const.TIME_OUT);
        OutputStream os = mSocket.getOutputStream();
        os.write(data.getBytes("UTF-8"));
        os.flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        String resp = reader.readLine();
        if (!hasCallingAnjixingAfterBoot && resp.contains("<cmdType>Start</cmdType>")) {
            hasCallingAnjixingAfterBoot = true;
        }
        LogUtil.logd("voip, resp=" + resp);
        return resp;
    }

    private boolean hasCallingAnjixingAfterBoot;// 开机或acc on之后有打过电话。

    // 曾经呼叫过安吉星客服
    public boolean hasCallAfterBoot() {
        return hasCallingAnjixingAfterBoot;
    }

    private class OneshotTask implements Runnable {
        protected final String data;

        public OneshotTask(String data) {
            this.data = data;
        }

        @Override
        public void run() {
            try {
                String resp = oneshot(data);
            } catch (IOException e) {
                LogUtil.logd("voip, failed: eClass=" + e.getClass() + ", eMsg=" + e.getMessage());
            }
        }
    }

    private String getImeiData() {
        String imei = DeviceUtils.getIMEI();
        if (TextUtils.isEmpty(imei)) {
            return null;
        }
        return PROTOCOL_IMEI
                .replace("#IMEI#", imei)
                .replace("#GPSURL#", Const.VOIP_REMOTE_SERVER)
                .replace("#WEBURL#", Const.VOIP_REMOTE_SERVER);
    }

    private void sendImeiSync() throws IOException {
        final String data = getImeiData();
        if (data == null) {
            throw new IOException("imei is null");
        }
        oneshot(data);
    }

    public void callOrHangupBlue(boolean manual) {
        String data = PROTOCOL_CALL_BLUE.replace("#GDStatus#", isGDForeground ? "Run" : "Down");
        mLastCallType = CALL_TYPE_BLUE;
        mWorkHandler.post(new OneshotTask(data));
    }

    public void callOrHangupRed(boolean manual) {
        String data = PROTOCOL_CALL_RED.replace("#GDStatus#", isGDForeground ? "Run" : "Down");
        mLastCallType = CALL_TYPE_RED;
        mWorkHandler.post(new OneshotTask(data));
    }

    private void cancelCall() {
        try {
            if (mSocket != null) {
                mSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtil.logd("voip, cancel");
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{
                EventTypes.EVENT_NAV_FOREGROUND,
                EventTypes.EVENT_NAV_BACKGROUND,
                EventTypes.EVENT_DEVICE_RED_BUTTON_PRESSED,
                EventTypes.EVENT_DEVICE_BLUE_BUTTON_PRESSED,
                EventTypes.EVENT_DEVICE_POWER_WAKEUP
        };
    }

    @Override
    protected void onEvent(String eventType) {
        switch (eventType) {
            case EventTypes.EVENT_NAV_FOREGROUND:
                isGDForeground = true;
                break;
            case EventTypes.EVENT_NAV_BACKGROUND:
                isGDForeground = false;
                break;
            case EventTypes.EVENT_DEVICE_RED_BUTTON_PRESSED:
                if (UpgradeManager.getInstance().isSystemUpgrading()) {
                    // 如果系统升级正在下载中，不执行拨打客服
                }else {
                    callOrHangupRed(true);
                }
                break;
            case EventTypes.EVENT_DEVICE_BLUE_BUTTON_PRESSED:
                if (UpgradeManager.getInstance().isSystemUpgrading()) {
                    // 如果系统升级正在下载中，不执行拨打客服
                }else {
                    callOrHangupBlue(true);
                }
                break;
            case EventTypes.EVENT_DEVICE_POWER_WAKEUP:
                hasCallingAnjixingAfterBoot = false;
                mWorkHandler.post(mSendImeiTask);
                break;
        }
    }

    private Runnable mSendImeiTask = new Runnable() {
        @Override
        public void run() {
            try {
                sendImeiSync();
                LogUtil.logd("voip, sendImei success.");
            } catch (IOException e) {
                LogUtil.logd("voip, sendImei failed, eClass=" + e.getClass() + ", eMsg=" + e.getMessage() + ", retry after 2000ms");
                mWorkHandler.removeCallbacks(mSendImeiTask);
                mWorkHandler.postDelayed(mSendImeiTask, 2000);
            }
        }
    };


    private static final String ACTION_CALL_STATE_CHANGE = "com.txznet.txz.intent.action.ACTION_CALL_STATE_CHANGE";
    private static final String EXTRA_STATE = "state";
    private static final String EXTRA_SOS = "sos";

    // 对外通知呼叫状态发生改变
    private void notifyCallStateChange() {
        Intent intent = new Intent(ACTION_CALL_STATE_CHANGE);
        boolean isSos = mLastCallType == CALL_TYPE_RED;
        switch (mCallStatus) {
            case CALL_STATUS_IDLE:
                intent.putExtra(EXTRA_STATE, 0);
                break;
            case CALL_STATUS_RINGING:
                intent.putExtra(EXTRA_STATE, 1);
                intent.putExtra(EXTRA_SOS, isSos);
                break;
            case CALL_STATUS_OFFHOOK:
                intent.putExtra(EXTRA_STATE, 2);
                intent.putExtra(EXTRA_SOS, isSos);
                break;
        }
        GlobalContext.get().sendBroadcast(intent);
    }

    public TXZCallManager.CallTool.CallStatus getCallStatus() {
        return mCallStatus;
    }
}