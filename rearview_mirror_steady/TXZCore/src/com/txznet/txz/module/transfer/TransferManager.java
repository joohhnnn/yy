package com.txznet.txz.module.transfer;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.push_manager.PushManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;

import java.util.HashMap;
import java.util.Map;

public class TransferManager extends IModule{
    private static TransferManager sManager = new TransferManager();

    public static TransferManager getInstance(){
        return sManager;
    }

    @Override
    public int initialize_BeforeStartJni() {
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_LAUNCHER_DATA);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_PUSH_LAUNCHER_DATA);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_THIRD_APP_MESSAGE);
        return super.initialize_BeforeStartJni();
    }

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        if (eventId == UiEvent.EVENT_ACTION_EQUIPMENT) {
            switch (subEventId) {
                case UiEquipment.SUBEVENT_RESP_LAUNCHER_DATA:
                    try {
                        if (data == null) {
                            LogUtil.logd("SUBEVENT_RESP_LAUNCHER_DATA data is null！");
                            onResponse(null);
                            break;
                        }
                        UiEquipment.Resp_Launcher resp_launcher = UiEquipment.Resp_Launcher.parseFrom(data);
                        onResponse(resp_launcher);
                    } catch (InvalidProtocolBufferNanoException e) {
                        e.printStackTrace();
                        onResponse(null);
                    }
                    break;
                case UiEquipment.SUBEVENT_PUSH_LAUNCHER_DATA:
                    ServiceManager.getInstance().sendInvoke(ServiceManager.LAUNCHER,"txz.launcher.push", data, null);
                    break;
                case UiEquipment.SUBEVENT_NOTIFY_THIRD_APP_MESSAGE:
                    try {
                        PushManager.PushCmd_ThirdAppMessage pushCmd_thirdAppMessage = PushManager.PushCmd_ThirdAppMessage
                                .parseFrom(data);
                        if (pushCmd_thirdAppMessage != null) {
                            if (pushCmd_thirdAppMessage.strPackage != null && pushCmd_thirdAppMessage.strPackage.length != 0) {
                                String pkg = new String(pushCmd_thirdAppMessage.strPackage);
                                String cmd = "";
                                if (pushCmd_thirdAppMessage.strSubCmd != null && pushCmd_thirdAppMessage.strSubCmd.length != 0) {
                                    cmd = new String(pushCmd_thirdAppMessage.strSubCmd);
                                }
                                LogUtil.logd("push msg to " + pkg + " : " + cmd );
                                ServiceManager.getInstance().sendInvoke(pkg, "txz.thirdapp.push", data, null);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        return super.onEvent(eventId, subEventId, data);
    }

    public void onFlagSkipJump(boolean isSkip) {
        LogUtil.logd("onFlagSkipJump:" + isSkip);
    }

    private void onResponse(UiEquipment.Resp_Launcher launcher) {
        LogUtil.logd("onResponse:" + launcher);
        int errorCode = -1;
        byte[] data = "".getBytes();
        int sessionId = -1;
        if (launcher != null) { //正常情况不会出现launcher=null
            errorCode = launcher.uint32ErrCode;
            data = launcher.strJson;
            sessionId = launcher.uint32SessionId;
        }

        String dataStr = data != null ? new String(data) : "";

        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("errorCode", errorCode);
        jsonBuilder.put("data", dataStr);
        jsonBuilder.put("sessionId", sessionId);

        if (mSessionPkgMap.containsKey(sessionId)) {
            String pkg = mSessionPkgMap.remove(sessionId);
            LogUtil.logd("onResponse to pkg:" + pkg);
            ServiceManager.getInstance().sendInvoke(pkg, ServiceManager.COMMAND_RESP_TRANSFER, jsonBuilder.toBytes(), null);
        }
    }

    public byte[] invokeTransfer(String packageName,String command,byte[] data) {
        if (ServiceManager.FIELD_REQTRANSFER.equals(command)) {
            doReqTransfer(packageName, data);
        }
        return null;
    }

    private Map<Integer, String> mSessionPkgMap = new HashMap<Integer, String>();

    private void doReqTransfer(String packageName, byte[] data) {
        JSONBuilder jsonBuilder = new JSONBuilder(data);
        String strJson = jsonBuilder.getVal("strJson", String.class);
        int uint32Action = jsonBuilder.getVal("uint32Action", Integer.class);
        int sessionId = jsonBuilder.getVal("sessionId", Integer.class);

        // 保证不同应用间不会存在重复
//        sessionId = (sessionId + packageName).hashCode();

        UiEquipment.Req_Launcher launcher = new UiEquipment.Req_Launcher();
        launcher.strJson = strJson.getBytes();
        launcher.uint32Action = uint32Action;
        launcher.uint32SessionId = sessionId;
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
                UiEquipment.SUBEVENT_REQ_LAUNCHER_DATA, launcher);
        if (mSessionPkgMap != null) {
            mSessionPkgMap.put(sessionId, packageName);
        }
        LogUtil.logd("TransferManager sendEvent pkg:" + packageName + ",strJson:" + jsonBuilder.toString());
    }
}