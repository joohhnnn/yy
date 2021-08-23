package com.txznet.sdk;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TXZCarControlHomeManager {
    public static final String PREFIX_SEND = "txz.carControlHome.";
    public static final String PREFIX_CALLBACK_EVENT = "txz.carControlHome.event.";
    public static final String QUERY_DEVICE_INFO = "queryDeviceInfo";
    public static final String QUERY_DEVICE_STATE = "queryDeviceState";
    public static final String CONTROL_DEVICE = "controlDevice";
    public static final String SET_TOOL = "setTool";
    public static final String CLEAR_TOOL = "clearTool";
    public static final String HANDLE_CAR_CONTROL_HOME_RESULT = "handleCarControlHomeResult";
    public static final String UPLOAD_DEVICE_INFO = "uploadDeviceInfo";


    public static final String QUERY_DEVICE_INFO_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + QUERY_DEVICE_INFO;
    public static final String QUERY_DEVICE_STATE_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + QUERY_DEVICE_STATE;
    public static final String CONTROL_DEVICE_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + CONTROL_DEVICE;
    public static final String UPLOAD_DEVICE_INFO_CALLBACK_EVENT = PREFIX_CALLBACK_EVENT + UPLOAD_DEVICE_INFO;


    public static final String SEND_QUERY_DEVICE_INFO = PREFIX_SEND + QUERY_DEVICE_INFO;
    public static final String SEND_QUERY_DEVICE_STATE = PREFIX_SEND + QUERY_DEVICE_STATE;
    public static final String SEND_CONTROL_DEVICE = PREFIX_SEND + CONTROL_DEVICE;
    public static final String SEND_SET_TOOL = PREFIX_SEND + SET_TOOL;
    public static final String SEND_CLEAR_TOOL = PREFIX_SEND + CLEAR_TOOL;
    public static final String SEND_HANDLE_CAR_CONTROL_HOME_RESULT = PREFIX_SEND + HANDLE_CAR_CONTROL_HOME_RESULT;
    public static final String SEND_UPLOAD_DEVICE_INFO = PREFIX_SEND + UPLOAD_DEVICE_INFO;

    private static TXZCarControlHomeManager sInstance = new TXZCarControlHomeManager();
    public static final int CODE_NET_ERROR = -1;

    private TXZCarControlHomeManager() {
    }

    public static TXZCarControlHomeManager getInstance() {
        return sInstance;
    }

    public void onReconnectTXZ() {
        if (mCarControlHomeTool != null) {
            setCarControlHomeTool(mCarControlHomeTool);
        }
    }


    public interface QueryDeviceInfoResultCallBack {
        void onSuccess(List<Device> devices);

        void onError(int errorCode);
    }

    public interface QueryDeviceStateResultCallBack {
        void onSuccess(List<Device> devices);

        void onError(int errorCode);
    }

    public interface ControlDeviceResultCallBack {
        void onSuccess(List<Device> devices);

        void onError(int errorCode);
    }

    public static final int ACTION_OPEN = 0;
    public static final int ACTION_CLOSE = 1;

    public static interface CarControlHomeTool {

        /**
         * 查询指定区域指定设备类型的设备信息
         *
         * @param area       区域
         * @param location   位置
         * @param deviceType 设备类型
         * @param callBack   结果回调
         */
        void queryDeviceInfo(String area, String location, String deviceType, QueryDeviceInfoResultCallBack callBack);

        /**
         * 查询指定设备的状态
         *
         * @param deviceIds 设备唯一标识符
         * @param callBack  结果回调
         */
        void queryDeviceState(List<String> deviceIds, QueryDeviceStateResultCallBack callBack);

        /**
         * 控制指定设备
         *
         * @param deviceIds 设备唯一标识符
         * @param action    操作码
         * @param callBack  结果回调
         */
        void controlDevice(List<String> deviceIds, int action, ControlDeviceResultCallBack callBack);

        /**
         * 上传设备信息结果回调
         */
        void onUploadDeviceInfoCallback(String jsonData);
    }

    private CarControlHomeTool mCarControlHomeTool;

    public void setCarControlHomeTool(final CarControlHomeTool tool) {
        mCarControlHomeTool = tool;
        if (tool == null) {
            TXZService.setCommandProcessor(PREFIX_SEND, null);
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, SEND_CLEAR_TOOL, null, null);
        } else {
            TXZService.setCommandProcessor(PREFIX_SEND, new TXZService.CommandProcessor() {

                @Override
                public byte[] process(String packageName, String command, byte[] data) {
                    LogUtil.e(command);
                    if (command.equals(QUERY_DEVICE_INFO)) {
                        if (data != null) {
                            final JSONBuilder jsonBuilder = new JSONBuilder(data);
                            final String id = jsonBuilder.getVal("id", String.class);
                            final String area = jsonBuilder.getVal("area", String.class);
                            String location = jsonBuilder.getVal("location", String.class);
                            String deviceType = jsonBuilder.getVal("deviceType", String.class);
                            mCarControlHomeTool.queryDeviceInfo(area, location, deviceType, new QueryDeviceInfoResultCallBack() {
                                @Override
                                public void onSuccess(List<Device> devices) {
                                    buildSuccessResult(devices, id, QUERY_DEVICE_INFO_CALLBACK_EVENT);
                                }

                                @Override
                                public void onError(int errorCode) {
                                    buildErrorResult(errorCode, id, QUERY_DEVICE_INFO_CALLBACK_EVENT);
                                }
                            });
                        }
                    } else if (command.equals(QUERY_DEVICE_STATE)) {
                        if (data != null) {
                            final JSONBuilder jsonBuilder = new JSONBuilder(data);
                            final String id = jsonBuilder.getVal("id", String.class);
                            List<String> deviceIds = new ArrayList<String>();
                            try {
                                JSONArray jsonArray = jsonBuilder.getVal("deviceIds", JSONArray.class);
                                if (jsonArray != null && jsonArray.length() > 0) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        deviceIds.add(jsonArray.getString(i));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (deviceIds.isEmpty()) {
                                LogUtil.e("params is empty");
                                return null;
                            }
                            mCarControlHomeTool.queryDeviceState(deviceIds, new QueryDeviceStateResultCallBack() {
                                @Override
                                public void onSuccess(List<Device> devices) {
                                    buildSuccessResult(devices, id, QUERY_DEVICE_STATE_CALLBACK_EVENT);
                                }

                                @Override
                                public void onError(int errorCode) {
                                    buildErrorResult(errorCode, id, QUERY_DEVICE_STATE_CALLBACK_EVENT);
                                }
                            });
                        }
                    } else if (command.equals(CONTROL_DEVICE)) {
                        if (data != null) {
                            final JSONBuilder jsonBuilder = new JSONBuilder(data);
                            final String id = jsonBuilder.getVal("id", String.class);
                            final Integer actionType = jsonBuilder.getVal("actionType", Integer.class);
                            List<String> deviceIds = new ArrayList<String>();
                            try {
                                JSONArray jsonArray = jsonBuilder.getJSONObject().getJSONArray("deviceIds");
                                if (jsonArray != null && jsonArray.length() > 0) {
                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        deviceIds.add(jsonArray.getString(i));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (deviceIds.isEmpty()) {
                                LogUtil.e("params is empty");
                                return null;
                            }
                            mCarControlHomeTool.controlDevice(deviceIds, actionType, new ControlDeviceResultCallBack() {
                                @Override
                                public void onSuccess(List<Device> devices) {
                                    buildSuccessResult(devices, id, CONTROL_DEVICE_CALLBACK_EVENT);
                                }

                                @Override
                                public void onError(int errorCode) {
                                    buildErrorResult(errorCode, id, CONTROL_DEVICE_CALLBACK_EVENT);
                                }
                            });
                        }
                    } else if (command.equals("event.uploadDeviceInfo")) {
                        if (data != null) {
                            mCarControlHomeTool.onUploadDeviceInfoCallback(new String(data));
                        } else {
                            mCarControlHomeTool.onUploadDeviceInfoCallback(null);
                            LogUtil.e("UPLOAD_DEVICE_INFO_CALLBACK_EVENT data is null");
                        }
                    }
                    return null;
                }
            });
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, SEND_SET_TOOL, null, null);
        }
    }

    private void buildErrorResult(int errorCode, String id, String event) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("errorCode", errorCode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, event, jsonObject.toString().getBytes(), null);
        }
    }

    private void buildSuccessResult(List<Device> devices, String id, String event) {
        JSONBuilder builder = new JSONBuilder();
        builder.put("id", id);
        if (devices != null && !devices.isEmpty()) {
            JSONArray array = new JSONArray();
            try {
                for (Device device : devices) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("deviceArea", device.deviceArea);
                    jsonObject.put("deviceId", device.deviceId);
                    jsonObject.put("deviceLocation", device.deviceLocation);
                    jsonObject.put("deviceName", device.deviceName);
                    jsonObject.put("deviceState", device.deviceState);
                    jsonObject.put("deviceTypeName", device.deviceTypeName);
                    jsonObject.put("isOnline", device.isOnline);
                    jsonObject.put("canControl", device.canControl);
                    if (device instanceof TemperatureHumiditySensorDevice) {
                        jsonObject.put("temperature", ((TemperatureHumiditySensorDevice) device).temperature);
                        jsonObject.put("humidity", ((TemperatureHumiditySensorDevice) device).humidity);
                    }
                    array.put(jsonObject);
                }
                builder.put("devices", array);
                LogUtil.e(builder.toPostString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, event, builder.toBytes(), null);
    }

    public static class Device {
        String deviceId;
        String deviceName;
        String deviceTypeName;
        Integer deviceState;
        String deviceLocation;
        String deviceArea;
        Integer isOnline;
        Boolean canControl;

        public String getDeviceTypeName() {
            return deviceTypeName;
        }

        public void setDeviceTypeName(String deviceTypeName) {
            this.deviceTypeName = deviceTypeName;
        }

        public Boolean getCanControl() {
            return canControl;
        }

        public void setCanControl(Boolean canControl) {
            this.canControl = canControl;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }


        public Integer getDeviceState() {
            return deviceState;
        }

        public void setDeviceState(Integer deviceState) {
            this.deviceState = deviceState;
        }

        public String getDeviceLocation() {
            return deviceLocation;
        }

        public void setDeviceLocation(String deviceLocation) {
            this.deviceLocation = deviceLocation;
        }

        public String getDeviceArea() {
            return deviceArea;
        }

        public void setDeviceArea(String deviceArea) {
            this.deviceArea = deviceArea;
        }

        public Integer getIsOnline() {
            return isOnline;
        }

        public void setIsOnline(Integer isOnline) {
            this.isOnline = isOnline;
        }
    }

    public static class TemperatureHumiditySensorDevice extends Device {
        Integer temperature;
        Integer humidity;

        public Integer getHumidity() {
            return humidity;
        }

        public void setHumidity(Integer humidity) {
            this.humidity = humidity;
        }

        public Integer getTemperature() {
            return temperature;
        }

        public void setTemperature(Integer temperature) {
            this.temperature = temperature;
        }
    }

    public void handleCarControlHomeResult(String jsonData) {
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, SEND_HANDLE_CAR_CONTROL_HOME_RESULT, jsonData.getBytes(), null);
    }

    public void uploadDeviceInfo(List<DeviceInfo> infos) {
        if (infos == null || infos.isEmpty()) {
            return;
        }
        JSONBuilder jsonBuilder = new JSONBuilder();
        JSONArray jsonArray = new JSONArray();
        try {
            for (DeviceInfo info : infos) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("deviceName", info.deviceName);
                jsonObject.put("devicePosition", info.devicePosition);
                jsonObject.put("devicePositionAdverb", info.devicePositionAdverb);
                jsonArray.put(jsonObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsonBuilder.put("devices", jsonArray);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, SEND_UPLOAD_DEVICE_INFO, jsonBuilder.toBytes(), null);
    }

    public static class DeviceInfo {
        String deviceName;
        String devicePosition;
        String devicePositionAdverb;

        public DeviceInfo(String deviceName, String devicePosition, String devicePositionAdverb) {
            this.deviceName = deviceName;
            this.devicePosition = devicePosition;
            this.devicePositionAdverb = devicePositionAdverb;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public void setDeviceName(String deviceName) {
            this.deviceName = deviceName;
        }

        public String getDevicePosition() {
            return devicePosition;
        }

        public void setDevicePosition(String devicePosition) {
            this.devicePosition = devicePosition;
        }

        public String getDevicePositionAdverb() {
            return devicePositionAdverb;
        }

        public void setDevicePositionAdverb(String devicePositionAdverb) {
            this.devicePositionAdverb = devicePositionAdverb;
        }
    }


}
