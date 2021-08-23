package com.txznet.txz.module.home;

import android.os.SystemClock;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.txz.report_manager.ReportManager;
import com.txz.ui.event.UiEvent;
import com.txz.ui.smarthome.UiSmartHome;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.bean.MiHomeInfo;
import com.txznet.sdk.TXZCarControlHomeManager;
import com.txznet.txz.component.choice.list.CarControlHomeWorkChoice;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CarControlHomeManager extends IModule {
    static String mRemoteService = null;
    static int sSessionId = 0;

    private static final CarControlHomeManager instance = new CarControlHomeManager();

    public static CarControlHomeManager getInstance() {
        return instance;
    }

    @Override
    public int initialize_BeforeStartJni() {
        regEvent(UiEvent.EVENT_SMART_HOME_DEVICES, UiSmartHome.SUBEVENT_RESP_REPORT_XIAO_MI_SMART_HOME_DEVICES);
        return super.initialize_BeforeStartJni();
    }

    private static ServiceManager.ConnectionListener mConnectionListener = new ServiceManager.ConnectionListener() {
        @Override
        public void onConnected(String serviceName) {
            if (serviceName.equals(mRemoteService)) {
                JNIHelper.logd(mRemoteService + " remote carControlHome tool onConnected");
            }
        }

        @Override
        public void onDisconnected(String serviceName) {
            // 监听适配程序连接断开
            if (serviceName.equals(mRemoteService)) {
                JNIHelper.logd(mRemoteService + " remote carControlHome tool onDisconnected");
                setRemoteService(null);
            }
        }
    };

    public static void setRemoteService(String serviceName) {
        synchronized (CarControlHomeManager.class) {
            mRemoteService = serviceName;
            JNIHelper.logd("update remote carControlHome tool service: " + mRemoteService);
            if (mRemoteService != null) {
                ServiceManager.getInstance().sendInvoke(mRemoteService, "", null, null);
            }
        }
    }

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        if (eventId == UiEvent.EVENT_SMART_HOME_DEVICES && subEventId == UiSmartHome.SUBEVENT_RESP_REPORT_XIAO_MI_SMART_HOME_DEVICES) {
            if (data != null) {
                ServiceManager.getInstance().sendInvoke(mRemoteService, TXZCarControlHomeManager.UPLOAD_DEVICE_INFO_CALLBACK_EVENT, new String(data).getBytes(), null);
            } else {
                LogUtil.e("skyward EVENT_SMART_HOME_DEVICES null");
            }
        }
        return super.onEvent(eventId, subEventId, data);
    }

    public static byte[] procRemoteResponse(String serviceName, String command,
                                            byte[] data) {
        LogUtil.e(command);
        if (TXZCarControlHomeManager.SEND_SET_TOOL.equals(command)) {
            ServiceManager.getInstance().addConnectionListener(mConnectionListener);
            setRemoteService(serviceName);
            return null;
        } else if (TXZCarControlHomeManager.SEND_CLEAR_TOOL.equals(command)) {
            ServiceManager.getInstance().removeConnectionListener(mConnectionListener);
            setRemoteService(null);
            remoteTaskMapper.clear();
            return null;
        } else if (TXZCarControlHomeManager.SEND_HANDLE_CAR_CONTROL_HOME_RESULT.equals(command)) {
            if (data != null) {
                handleCarControlHomeResult(JSONObject.parseObject(new String(data)));
            } else {
                LogUtil.e("skyward data is null");
            }
            return null;
        } else if (TXZCarControlHomeManager.SEND_UPLOAD_DEVICE_INFO.equals(command)) {
            if (data != null) {

                ReportManager.Req_ReportXiaoMiSmartHomeDevices reqReportXiaoMiSmartHomeDevices = new ReportManager.Req_ReportXiaoMiSmartHomeDevices();
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                LogUtil.e("skyward " + jsonBuilder.toPostString());
                try {
                    JSONArray devices = jsonBuilder.getJSONObject().getJSONArray("devices");
                    if (devices == null || devices.length() <= 0) {
                        LogUtil.e("skyward SEND_UPLOAD_DEVICE_INFO is empty");
                        return null;
                    }
                    ReportManager.XiaoMiSmartHomeDevice[] xiaoMiSmartHomeDevices = new ReportManager.XiaoMiSmartHomeDevice[devices.length()];
                    for (int i = 0; i < devices.length(); i++) {
                        ReportManager.XiaoMiSmartHomeDevice xiaoMiSmartHomeDevice = new ReportManager.XiaoMiSmartHomeDevice();
                        if (devices.getJSONObject(i).has("deviceName")) {
                            String deviceName = devices.getJSONObject(i).getString("deviceName");
                            if (!TextUtils.isEmpty(deviceName)) {
                                xiaoMiSmartHomeDevice.strName = deviceName.getBytes();
                            }
                        }
                        if (devices.getJSONObject(i).has("devicePosition")) {
                            String devicePosition = devices.getJSONObject(i).getString("devicePosition");
                            if (!TextUtils.isEmpty(devicePosition)) {
                                xiaoMiSmartHomeDevice.strPosition = devicePosition.getBytes();
                            }
                        }
                        if (devices.getJSONObject(i).has("devicePositionAdverb")) {
                            String devicePositionAdverb = devices.getJSONObject(i).getString("devicePositionAdverb");
                            if (!TextUtils.isEmpty(devicePositionAdverb)) {
                                xiaoMiSmartHomeDevice.strPositionAdverb = devicePositionAdverb.getBytes();
                            }
                        }
                        xiaoMiSmartHomeDevices[i] = xiaoMiSmartHomeDevice;
                    }
                    reqReportXiaoMiSmartHomeDevices.rptDevices = xiaoMiSmartHomeDevices;
                    JNIHelper.sendEvent(UiEvent.EVENT_SMART_HOME_DEVICES, UiSmartHome.SUBEVENT_REQ_REPORT_XIAO_MI_SMART_HOME_DEVICES, reqReportXiaoMiSmartHomeDevices);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                LogUtil.e("SEND_UPLOAD_DEVICE_INFO skyward data is null");
            }
            return null;
        }

        if (!serviceName.equals(mRemoteService)) {
            return null;
        }
        asyncProcessResponse(command, data);
        return null;
    }

    private static synchronized void asyncProcessResponse(String command, byte[] data) {
        if (data == null) {
            return;
        }
        LogUtil.e(command);
        JSONBuilder jsonBuilder = new JSONBuilder(data);
        String id = null;
        try {
            id = jsonBuilder.getJSONObject().getString("id");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (remoteTaskMapper.get(id) == null) {
            LogUtil.e("skyward id" + id);
            return;
        }
        if (!(Integer.valueOf(id) == sSessionId)){
            LogUtil.e("skyward no current session Id " + id + "  != " + sSessionId);
            return;
        }
        ResultCallBack resultCallBack = remoteTaskMapper.get(id).callback;
        LogUtil.e(jsonBuilder.toPostString());

        int errorCode = jsonBuilder.getVal("errorCode", Integer.class, 0);
        if (errorCode != 0) {
            if (resultCallBack != null) {
                resultCallBack.onError(errorCode);
                remoteTaskMapper.remove(id);
            }
            return;
        }
        if (TXZCarControlHomeManager.QUERY_DEVICE_INFO_CALLBACK_EVENT.equals(command)) {
            invokeResultCallBack(jsonBuilder, resultCallBack, id);
        } else if (TXZCarControlHomeManager.CONTROL_DEVICE_CALLBACK_EVENT.equals(command)) {
            invokeResultCallBack(jsonBuilder, resultCallBack, id);
        } else if (TXZCarControlHomeManager.QUERY_DEVICE_STATE_CALLBACK_EVENT.equals(command)) {
            invokeResultCallBack(jsonBuilder, resultCallBack, id);
        }
    }

    private static void invokeResultCallBack(JSONBuilder jsonBuilder, ResultCallBack resultCallBack, String id) {
        if (resultCallBack != null) {
            resultCallBack.onSuccess(parseResult(jsonBuilder, resultCallBack));
            remoteTaskMapper.remove(id);
        } else {
            LogUtil.e("resultCallBack is null");
        }
    }

    private static List parseResult(JSONBuilder jsonBuilder, ResultCallBack resultCallBack) {
        List result = null;
        try {
            JSONArray devices = null;
            if (jsonBuilder.getJSONObject().has("devices")) {
                devices = jsonBuilder.getJSONObject().getJSONArray("devices");
            }
            if (devices != null && devices.length() > 0) {
                result = new ArrayList<TXZCarControlHomeManager.Device>();
                for (int i = 0; i < devices.length(); i++) {
                    org.json.JSONObject jsonObject = devices.getJSONObject(i);
                    TXZCarControlHomeManager.Device device;
                    if (jsonObject.has("deviceTypeName")) {
                        if ("温湿度传感器".equals(jsonObject.getString("deviceTypeName"))) {
                            device = new TXZCarControlHomeManager.TemperatureHumiditySensorDevice();
                            if (jsonObject.has("temperature")) {
                                ((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) device).setTemperature(jsonObject.getInt("temperature"));
                            }
                            if (jsonObject.has("humidity")) {
                                ((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) device).setHumidity(jsonObject.getInt("humidity"));
                            }
                        } else {
                            device = new TXZCarControlHomeManager.Device();
                        }
                        device.setDeviceTypeName(jsonObject.getString("deviceTypeName"));
                    } else if (jsonObject.has("temperature")) {
                        device = new TXZCarControlHomeManager.TemperatureHumiditySensorDevice();
                        ((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) device).setTemperature(jsonObject.getInt("temperature"));
                        if (jsonObject.has("humidity")) {
                            ((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) device).setHumidity(jsonObject.getInt("humidity"));
                        }
                    } else {
                        device = new TXZCarControlHomeManager.Device();
                    }

                    if (jsonObject.has("deviceArea")) {
                        device.setDeviceArea(jsonObject.getString("deviceArea"));
                    }
                    if (jsonObject.has("deviceId")) {
                        device.setDeviceId(jsonObject.getString("deviceId"));
                    }
                    if (jsonObject.has("deviceLocation")) {
                        device.setDeviceLocation(jsonObject.getString("deviceLocation"));
                    }
                    if (jsonObject.has("deviceName")) {
                        device.setDeviceName(jsonObject.getString("deviceName"));
                    }
                    if (jsonObject.has("deviceState")) {
                        device.setDeviceState(jsonObject.getInt("deviceState"));
                    }
                    if (jsonObject.has("isOnline")) {
                        device.setIsOnline(jsonObject.getInt("isOnline"));
                    }
                    if (jsonObject.has("canControl")) {
                        device.setCanControl(jsonObject.getBoolean("canControl"));
                    }
                    result.add(device);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resultCallBack.onError(-5);
        }
        return result;
    }


    private CarControlHomeManager() {
        RecorderWin.OBSERVABLE.registerObserver(new RecorderWin.StatusObervable.StatusObserver() {

            @Override
            public void onShow() {
            }

            @Override
            public void onDismiss() {
                clearQueryDeviceInfoTimeRunnable();
                remoteTaskMapper.clear();
            }
        });
    }

    public static boolean handleCarControlHomeResult(JSONObject json) {
        JSONObject key_words = json.getJSONObject("key_words");
        String command = key_words.getString("command");
        final String device = key_words.getString("device");
        String position = key_words.getString("position");
        String positionAdverb = key_words.getString("position_adverb");
        if ("打开".equals(command) || "开启".equals(command) || "开".equals(command) || "启动".equals(command)) {
            queryControlDeviceInfo(positionAdverb, device, position, CarControlHomeWorkChoice.ActionType.OPEN);
        } else if ("查询".equals(command) || "查看".equals(command) || "查".equals(command)) {
            queryDeviceInfo(device, positionAdverb, position);
        } else if ("关闭".equals(command) || "关".equals(command) || "关上".equals(command) || "关掉".equals(command) || "关了".equals(command)) {
            queryControlDeviceInfo(positionAdverb, device, position, CarControlHomeWorkChoice.ActionType.CLOSE);
        }
        return true;
    }

    private static void queryDeviceInfo(String device, String location, String position) {
        setQueryDeviceInfoRunnable();
        queryDeviceInfo(position, location, device, new ResultCallBack<List<TXZCarControlHomeManager.Device>>() {
            @Override
            public void onSuccess(List<TXZCarControlHomeManager.Device> devices) {
                clearQueryDeviceInfoTimeRunnable();
                if (devices != null && !devices.isEmpty()) {
                    List data = new ArrayList<CarControlHomeWorkChoice.Device>();
                    int state = MiHomeInfo.STATE_QUERYING;
                    for (TXZCarControlHomeManager.Device device : devices) {
                        CarControlHomeWorkChoice.Device localDevice;
                        if (device instanceof TXZCarControlHomeManager.TemperatureHumiditySensorDevice) {
                            localDevice = new CarControlHomeWorkChoice.TemperatureHumiditySensorDevice();
                            if (((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) device).getHumidity() != null) {
                                ((CarControlHomeWorkChoice.TemperatureHumiditySensorDevice) localDevice).humidity = ((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) device).getHumidity();
                            }
                            if (((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) device).getTemperature() != null) {
                                ((CarControlHomeWorkChoice.TemperatureHumiditySensorDevice) localDevice).temperature = ((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) device).getTemperature();
                            }
                        } else {
                            localDevice = new CarControlHomeWorkChoice.Device();
                        }
                        localDevice.deviceArea = device.getDeviceArea();
                        localDevice.deviceLocation = device.getDeviceLocation();
                        localDevice.deviceId = device.getDeviceId();
                        localDevice.state = state;
                        localDevice.deviceState = device.getDeviceState();
                        localDevice.deviceName = device.getDeviceName();
                        localDevice.isOnline = device.getIsOnline();
                        localDevice.canControl = device.getCanControl();
                        data.add(localDevice);
                    }
                    CarControlHomeWorkChoice.CarControlHomeData carControlHomeData = new CarControlHomeWorkChoice.CarControlHomeData();
                    carControlHomeData.mMiHomeItemList = data;
                    carControlHomeData.action = CarControlHomeWorkChoice.ActionType.QUERY;
                    ChoiceManager.getInstance().showMiHomeList(carControlHomeData, null);
                } else {
                    RecorderWin.speakText(NativeData.getResString("RS_VOICE_NO_AD_DEVICE"), null);
                }
            }

            @Override
            public void onError(int errorCode) {
                clearQueryDeviceInfoTimeRunnable();
                speakErrorTip(errorCode);
            }
        });
    }

    private static void speakErrorTip(int errorCode) {
        String tts = NativeData.getResString("RS_VOICE_QUERY_DATA_FAIL");
        if (errorCode == 901 || errorCode == 902 || errorCode == 11052) {
            tts = NativeData.getResString("RS_VOICE_NO_AUTHORIZATION");
        }
        RecorderWin.speakText(tts, null);
    }

    private static synchronized void setQueryDeviceInfoRunnable() {
        AppLogicBase.removeBackGroundCallback(mQueryDeviceInfoFirstTimeoutRunnable);
        AppLogicBase.runOnBackGround(mQueryDeviceInfoFirstTimeoutRunnable, QUERY_DEVICE_INFO_FIRST_TIMEOUT);
        mHadCleanTimeoutRunnable = false;
    }

    private static synchronized void clearQueryDeviceInfoTimeRunnable() {
        AppLogicBase.removeBackGroundCallback(mQueryDeviceInfoFirstTimeoutRunnable);
        AppLogicBase.removeBackGroundCallback(mQueryDeviceInfoSecondTimeoutRunnable);
        mHadCleanTimeoutRunnable = true;
    }


    public static final int QUERY_DEVICE_INFO_FIRST_TIMEOUT = 1000;
    public static final int QUERY_DEVICE_INFO_SECOND_TIMEOUT = 10000;
    private static Runnable mQueryDeviceInfoFirstTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            AsrManager.getInstance().setNeedCloseRecord(false);
            RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_EXECUTING"), false, new Runnable() {
                @Override
                public void run() {
                    synchronized (CarControlHomeManager.class) {
                        if (!mHadCleanTimeoutRunnable) {
                            AppLogicBase.removeBackGroundCallback(mQueryDeviceInfoSecondTimeoutRunnable);
                            AppLogicBase.runOnBackGround(mQueryDeviceInfoSecondTimeoutRunnable, QUERY_DEVICE_INFO_SECOND_TIMEOUT);
                            AppLogic.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    RecorderWin.refreshState(RecorderWin.STATE_RECORD_END);
                                }
                            });
                        }
                    }
                }
            });
        }
    };

    private static boolean mHadCleanTimeoutRunnable;

    private static Runnable mQueryDeviceInfoSecondTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_IN_NET"), null);
        }
    };



    private static void queryControlDeviceInfo(String location, String device, String position, final int action) {
        setQueryDeviceInfoRunnable();
        queryDeviceInfo(position, location, device, new ResultCallBack<List<TXZCarControlHomeManager.Device>>() {
            @Override
            public void onSuccess(List<TXZCarControlHomeManager.Device> devices) {
                clearQueryDeviceInfoTimeRunnable();
                if (devices != null && !devices.isEmpty()) {
                    Set<String> deviceAreas = new HashSet<String>();
                    List data = new ArrayList<CarControlHomeWorkChoice.AreaDevice>();
                    int state = 0;
                    boolean isSupport = false;
                    for (TXZCarControlHomeManager.Device device : devices) {
                        deviceAreas.add(device.getDeviceArea() + device.getDeviceLocation());
                        if (device.getCanControl()) {
                            isSupport = true;
                        }
                    }
                    // 如果所有的设备都不可控制，直接返回，不展示列表
                    if (!isSupport) {
                        AsrManager.getInstance().setNeedCloseRecord(true);
                        RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_NO_SUPPORT_OPERATION"), null);
                        return;
                    }
                    for (String deviceArea : deviceAreas) {
                        CarControlHomeWorkChoice.AreaDevice areaDevice = new CarControlHomeWorkChoice.AreaDevice();
                        List<CarControlHomeWorkChoice.Device> deviceList = new ArrayList<CarControlHomeWorkChoice.Device>();
                        for (TXZCarControlHomeManager.Device innerDevice : devices) {
                            if (deviceArea.equals(innerDevice.getDeviceArea() + innerDevice.getDeviceLocation())) {
                                areaDevice.deviceArea = innerDevice.getDeviceArea();
                                areaDevice.deviceTypeName = innerDevice.getDeviceTypeName();
                                areaDevice.deviceLocation = innerDevice.getDeviceLocation();
                                areaDevice.state = state;
                                CarControlHomeWorkChoice.Device tempDevice = new CarControlHomeWorkChoice.Device();
                                tempDevice.deviceId = innerDevice.getDeviceId();
                                tempDevice.canControl = innerDevice.getCanControl();
                                tempDevice.isOnline = innerDevice.getIsOnline();
                                tempDevice.deviceState = innerDevice.getDeviceState();
                                deviceList.add(tempDevice);
                            }
                        }
                        areaDevice.devices = deviceList;
                        data.add(areaDevice);
                    }
                    CarControlHomeWorkChoice.CarControlHomeData carControlHomeData = new CarControlHomeWorkChoice.CarControlHomeData();
                    carControlHomeData.mMiHomeItemList = data;
                    carControlHomeData.action = action;
                    ChoiceManager.getInstance().showMiHomeList(carControlHomeData, null);
                } else {
                    RecorderWin.speakText(NativeData.getResString("RS_VOICE_NO_AD_DEVICE"), null);
                }
            }

            @Override
            public void onError(int errorCode) {
                clearQueryDeviceInfoTimeRunnable();
                speakErrorTip(errorCode);
            }
        });
    }

    static class RemoteTask {
        ResultCallBack callback;
        long timeout = 0;
    }

    public static void queryDeviceInfo(String area, String location, String deviceType, ResultCallBack<List<TXZCarControlHomeManager.Device>> callBack) {
        if (mRemoteService == null) {
            LogUtil.e("don't set tool");
            return;
        }
        JSONBuilder jsonBuilder = new JSONBuilder();
        ++sSessionId;
        int id = sSessionId;
        jsonBuilder.put("id", String.valueOf(id));
        jsonBuilder.put("area", area);
        jsonBuilder.put("location", location);
        jsonBuilder.put("deviceType", deviceType);
        addTask(id, callBack);
        ServiceManager.getInstance().sendInvoke(mRemoteService, TXZCarControlHomeManager.SEND_QUERY_DEVICE_INFO, jsonBuilder.toBytes(), null);
    }


    public void queryDeviceState(List<String> deviceIds, ResultCallBack<List<TXZCarControlHomeManager.Device>> callBack) {
        if (deviceIds != null && !deviceIds.isEmpty()) {
            JSONBuilder jsonBuilder = new JSONBuilder();
            ++sSessionId;
            int id = sSessionId;
            jsonBuilder.put("id", String.valueOf(id));
            JSONArray jsonArray = new JSONArray();
            for (String deviceId : deviceIds) {
                jsonArray.put(deviceId);
            }
            jsonBuilder.put("deviceIds", jsonArray);
            addTask(id, callBack);
            ServiceManager.getInstance().sendInvoke(mRemoteService, TXZCarControlHomeManager.SEND_QUERY_DEVICE_STATE, jsonBuilder.toBytes(), null);
        }
    }

    public void controlDevice(List<String> deviceIds, final int actionType, ResultCallBack<List<TXZCarControlHomeManager.Device>> callBack) {
        if (deviceIds != null && !deviceIds.isEmpty()) {
            JSONBuilder jsonBuilder = new JSONBuilder();
            ++sSessionId;
            int id = sSessionId;
            jsonBuilder.put("id", String.valueOf(id));
            jsonBuilder.put("actionType", actionType);
            JSONArray jsonArray = new JSONArray();
            for (String deviceId : deviceIds) {
                jsonArray.put(deviceId);
            }
            jsonBuilder.put("deviceIds", jsonArray);
            addTask(id, callBack);
            ServiceManager.getInstance().sendInvoke(mRemoteService, TXZCarControlHomeManager.SEND_CONTROL_DEVICE, jsonBuilder.toBytes(), null);
        }
    }

    public interface ResultCallBack<T> {
        void onSuccess(T data);

        void onError(int errorCode);
    }

    private static final Map<String, RemoteTask> remoteTaskMapper = new HashMap<String, RemoteTask>();

    public static void addTask(int localTaskId, ResultCallBack callback) {
        addTask(localTaskId, callback, DEFAULT_TASK_TIMEOUT);
    }

    private final static int DEFAULT_TASK_TIMEOUT = 60 * 1000;

    public static void addTask(int localTaskId, ResultCallBack callback, int timeout) {
        synchronized (remoteTaskMapper) {
            long now = SystemClock.elapsedRealtime();
            cleanTimeoutTask(now);
            RemoteTask remoteTask = new RemoteTask();
            remoteTask.callback = callback;
            remoteTask.timeout = now + timeout;
            remoteTaskMapper.put(String.valueOf(localTaskId), remoteTask);
        }
    }

    private static void cleanTimeoutTask(long now) {
        Iterator<String> iterator = remoteTaskMapper.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            RemoteTask task = remoteTaskMapper.get(key);
            if (now > task.timeout) {
                LogUtil.loge("task(" + key + ") process timeout clean");
                iterator.remove();
            }
        }
    }
}
