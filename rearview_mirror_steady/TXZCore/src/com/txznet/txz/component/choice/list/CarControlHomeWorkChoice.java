package com.txznet.txz.component.choice.list;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.record.bean.MiHomeInfo;
import com.txznet.sdk.TXZCarControlHomeManager;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.home.CarControlHomeManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CarControlHomeWorkChoice extends WorkChoice<CarControlHomeWorkChoice.CarControlHomeData, CarControlHomeWorkChoice.MiHomeItem> {
    public boolean isProcessing = false;
    private int mVoiceAction = -1;
    /**
     * 是否已经直接控制过第一个
     */
    private boolean hasControlFirstDirect = false;

    @Override
    protected void onSelectIndex(final MiHomeItem item, boolean isFromPage, int idx, String fromVoice) {
        if (isProcessing) {
            TtsManager.getInstance().speakText(NativeData.getResString("RS_VOICE_CONTROLING"));
            LogUtil.e("processing ");
            return;
        }
        isProcessing = true;
        if (item instanceof Device) {
            controlSingleDevice(item, idx);
        } else if (item instanceof AreaDevice) {
            controlAreaDevice(item, idx, fromVoice);
        }
    }

    private void setControlDeviceTimeoutRunnable() {
        AppLogicBase.removeBackGroundCallback(mControlDeviceTimeoutRunnable);
        AppLogicBase.removeBackGroundCallback(mTipsRunnable);
        AppLogicBase.runOnBackGround(mControlDeviceTimeoutRunnable, CONTROL_DEVICE_TIMEOUT);
    }

    private void cleanControlDeviceTimeoutRunnable() {
        AppLogicBase.removeBackGroundCallback(mControlDeviceTimeoutRunnable);
    }

    private void controlSingleDevice(final MiHomeItem item, final int idx) {
        if (((Device) item).isOnline == 1) {
            TtsManager.getInstance().speakText(NativeData.getResString("RS_VOICE_DEVICE_OFFLINE"));
            isProcessing = false;
            return;
        }
        if (((Device) item).canControl != null && !((Device) item).canControl) {
            TtsManager.getInstance().speakText(NativeData.getResString("RS_VOICE_NO_SUPPORT_OPERATION"));
            isProcessing = false;
            return;
        }
        LogUtil.e(((Device) mData.mMiHomeItemList.get(idx + mPage.getCurrPage() * getOption().getNumPageSize())).deviceName);
        ((Device) mData.mMiHomeItemList.get(idx + mPage.getCurrPage() * getOption().getNumPageSize())).state = MiHomeInfo.STATE_EXECUTING;
        refreshCurrPage();
        setControlDeviceTimeoutRunnable();
        setExitTimeoutRunnable(DEVICE_EXIT_TIME);
        LogUtil.d("skyward " + ((Device) item).deviceId);
        CarControlHomeManager.getInstance().controlDevice(Arrays.asList(((Device) item).deviceId), mVoiceAction, new CarControlHomeManager.ResultCallBack<List<TXZCarControlHomeManager.Device>>() {
            @Override
            public void onSuccess(List<TXZCarControlHomeManager.Device> data) {
                isProcessing = false;
                cleanControlDeviceTimeoutRunnable();
                LogUtil.d("mVoiceAction " + mVoiceAction);
                if (data == null || data.isEmpty()) {
                    LogUtil.e("isProcessing skyward");
                    TtsManager.getInstance().speakText(NativeData.getResString("RS_VOICE_CONTROL_FAIL"));
                    return;
                }
                boolean result = false;
                boolean noChange = false;
                for (TXZCarControlHomeManager.Device datum : data) {

                    if (((Device) item).deviceId.equals(datum.getDeviceId()) && datum.getDeviceState() == mVoiceAction) {
                        if (((Device) item).deviceState == mVoiceAction) {
                            noChange = true;
                        } else {
                            ((Device) mData.mMiHomeItemList.get(idx + mPage.getCurrPage() * getOption().getNumPageSize())).state = MiHomeInfo.STATE_EXECUTING;
                            ((Device) item).deviceState = mVoiceAction;
                        }
                        result = true;
                    }
                }
                LogUtil.e("result skyward" + result);

                if (result) {
                    if (noChange) {
                        if (mVoiceAction == 0) {
                            TtsManager.getInstance().speakText(((Device) item).deviceName + "已是开启状态");
                        } else {
                            TtsManager.getInstance().speakText(((Device) item).deviceName + "已是关闭状态");
                        }
                    } else {
                        TtsManager.getInstance().speakText(buildControlSingleStateTips((Device) item));
                    }
                    item.state = MiHomeInfo.STATE_EXECUTED;
                    if (isSelecting()) {
                        refreshCurrPage();
                    }
                } else {
                    TtsManager.getInstance().speakText(NativeData.getResString("RS_VOICE_CONTROL_FAIL"));
                }
            }

            @Override
            public void onError(int errorCode) {
                onControlDeviceError(errorCode);
            }
        });
    }

    private void onControlDeviceError(int errorCode) {
        String tts = NativeData.getResString("RS_VOICE_CONTROL_FAIL");
        if (errorCode == 11) {
            tts = NativeData.getResString("RS_VOICE_DEVICE_OFFLINE");
        }
        cleanControlDeviceTimeoutRunnable();
        TtsManager.getInstance().speakText(tts);
        LogUtil.e(errorCode + "skyward");
        isProcessing = false;
    }

    private String buildControlSingleStateTips(Device item) {
        String tts;
        if (mVoiceAction == 1) {
            tts = item.deviceName + "已关闭";
        } else {
            tts = item.deviceName + "已开启";
        }
        return tts;
    }

    private void controlAreaDevice(final MiHomeItem item, int idx, String fromVoice) {
        final List<String> deviceIds = filterCanNotControlDeviceIds(((AreaDevice) item).devices);
        boolean isOnline = false;
        for (Device device : ((AreaDevice) item).devices) {
            if (device.isOnline == 0) {
                isOnline = true;
                break;
            }
        }
        String tts = NativeData.getResString("RS_VOICE_NO_SUPPORT_OPERATION");
        if (!isOnline) {
            tts = ((AreaDevice) item).deviceTypeName + "处于离线状态";
            TtsManager.getInstance().speakText(tts, new TtsUtil.ITtsCallback() {
                @Override
                public void onEnd() {
                    super.onEnd();
                    isProcessing = false;
                }
            });
            return;
        } else if (deviceIds == null || deviceIds.isEmpty()) {
            TtsManager.getInstance().speakText(tts, new TtsUtil.ITtsCallback() {
                @Override
                public void onEnd() {
                    super.onEnd();
                    isProcessing = false;
                }
            });
            return;
        }
        item.state = MiHomeInfo.STATE_EXECUTING;
        refreshCurrPage();
        LogUtil.d("skyward " + fromVoice);
        if (!FLAG_FIRST.equals(fromVoice)) {
            setExitTimeoutRunnable(DEVICE_EXIT_TIME);
        }
        setControlDeviceTimeoutRunnable();
        LogUtil.d("controlAreaDevice " + Arrays.toString(deviceIds.toArray()));
        CarControlHomeManager.getInstance().controlDevice(deviceIds, mVoiceAction, new CarControlHomeManager.ResultCallBack<List<TXZCarControlHomeManager.Device>>() {
            @Override
            public void onSuccess(List<TXZCarControlHomeManager.Device> data) {
                isProcessing = false;
                cleanControlDeviceTimeoutRunnable();
                if (data == null || data.isEmpty()) {
                    TtsManager.getInstance().speakText(NativeData.getResString("RS_VOICE_CONTROL_FAIL"));
                    return;
                }
                boolean result = false;
                int openNum = 0;
                int closeNum = 0;
                boolean noChange = true;
                for (TXZCarControlHomeManager.Device datum : data) {
                    for (Device device : ((AreaDevice) item).devices) {
                        if (device.deviceId.equals(datum.getDeviceId())) {
                            if (datum.getDeviceState() == mVoiceAction) {
                                if (datum.getDeviceState() != device.deviceState) {
                                    noChange = false;
                                }
                                result = true;
                            }
                            device.deviceState = datum.getDeviceState();
                            if (device.deviceState == MiHomeInfo.DEVICE_STATE_CLOSE) {
                                ++closeNum;
                            } else if (device.deviceState == MiHomeInfo.DEVICE_STATE_OPEN) {
                                ++openNum;
                            } else {
                                LogUtil.e(device.deviceId + "invalid state " + device.deviceState);
                            }
                        }
                    }
                }
                ((AreaDevice) item).openNum = String.valueOf(openNum);
                ((AreaDevice) item).closeNum = String.valueOf(closeNum);
                LogUtil.e("result skyward" + result);
                if (result) {
                    TtsUtil.ITtsCallback callback = null;
                    if (mData.mMiHomeItemList.size() == 1) {
                        callback = new TtsUtil.ITtsCallback() {
                            @Override
                            public void onEnd() {
                                super.onEnd();
                                setExitTimeoutRunnable(0);
                            }
                        };
                    }
                    if (noChange) {
                        if (mVoiceAction == 0) {
                            TtsManager.getInstance().speakText(((AreaDevice) item).deviceTypeName + "已是开启状态",callback);
                        } else {
                            TtsManager.getInstance().speakText(((AreaDevice) item).deviceTypeName + "已是关闭状态", callback);
                        }
                    } else {
                        TtsManager.getInstance().speakText(buildControlAreaDeviceTips((AreaDevice) item, openNum, closeNum), callback);
                    }
                    item.state = MiHomeInfo.STATE_EXECUTED;
                    if (isSelecting()) {
                        refreshCurrPage();
                    }

                } else {
                    TtsManager.getInstance().speakText(NativeData.getResString("RS_VOICE_CONTROL_FAIL"));
                }
            }

            @Override
            public void onError(int errorCode) {
                onControlDeviceError(errorCode);
            }
        });
    }

    private List<String> filterCanNotControlDeviceIds(List<Device> devices) {
        List<String> result = new ArrayList<String>();
        for (Device device : devices) {
            if (device.canControl && device.isOnline == 0) {
                result.add(device.deviceId);
            }
        }
        return result;

    }

    private String buildControlAreaDeviceTips(AreaDevice item, int openNum, int closeNum) {
        String tts;
        if (mVoiceAction == 1) {
            if (closeNum == 1) {
                tts = item.deviceTypeName + "已关闭";
            } else {
                tts = "已关闭" + closeNum + "个" + item.deviceTypeName;
            }
        } else {
            if (openNum == 1) {
                tts = item.deviceTypeName + "已开启";
            } else {
                tts = "已开启" + openNum + "个" + item.deviceTypeName;
            }
        }
        return tts;
    }


    @Override
    public void showChoices(CarControlHomeData data) {
        if (data == null || data.mMiHomeItemList == null || data.mMiHomeItemList.isEmpty()) {
            return;
        }
        mData = data;
        hasControlFirstDirect = false;
        if (data.mMiHomeItemList.get(0) instanceof Device) {
            final List<String> deviceIds = new ArrayList<String>();
            for (MiHomeItem datum : data.mMiHomeItemList) {
                if (((Device) datum).isOnline == 0) {
                    deviceIds.add(((Device) datum).deviceId);
                }
            }



            queryDeviceState(deviceIds);
            LogUtil.d("skyward queryDeviceState");
        } else {
            setExitTimeoutRunnable(DEVICE_NO_CONTROL_EXIT_TIME);
        }
        exit = false;
        super.showChoices(data);
    }

    private void setExitTimeoutRunnable(long executeTime) {
        AppLogic.removeBackGroundCallback(mExitTimeoutRunnable);
        AppLogic.runOnBackGround(mExitTimeoutRunnable, executeTime);
    }

    public static final int QUERY_DEVICE_STATE_TIMEOUT = 10000;
    public static final int CONTROL_DEVICE_TIMEOUT = 10000;
    public static final int TIPS_TIME = 4000;
    public static final int DEVICE_EXIT_TIME = 7000;
    public static final int DEVICE_NO_CONTROL_EXIT_TIME= 20000;

    private void queryDeviceState(List<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) {
            if (mData.mMiHomeItemList.size() == 1) {
                TtsManager.getInstance().speakText(((Device) mData.mMiHomeItemList.get(0)).deviceName + "为离线状态");
            }
            LogUtil.e("queryDeviceState: deviceIds is empty");
            setExitTimeoutRunnable(DEVICE_NO_CONTROL_EXIT_TIME);
            return;
        }
        AppLogicBase.removeBackGroundCallback(mQueryDeviceStateTimeoutRunnable);
        AppLogicBase.runOnBackGround(mQueryDeviceStateTimeoutRunnable, QUERY_DEVICE_STATE_TIMEOUT);
        isProcessing = true;
        CarControlHomeManager.getInstance().queryDeviceState(deviceIds, new CarControlHomeManager.ResultCallBack<List<TXZCarControlHomeManager.Device>>() {
            @Override
            public void onSuccess(List<TXZCarControlHomeManager.Device> data) {
                isProcessing = false;
                if (data == null || data.isEmpty()) {
                    if (isSelecting()) {
                        clearIsSelecting();
                        RecorderWin.open(NativeData.getResString("RS_VOICE_NO_AD_DEVICE"));
                    }
                    return;
                }
                for (TXZCarControlHomeManager.Device datum : data) {
                    for (MiHomeItem miHomeItem : mData.mMiHomeItemList) {
                        if (((Device) miHomeItem).deviceId.equals(datum.getDeviceId())) {
                            int state;
                            if (datum.getDeviceState() != null && (datum.getDeviceState() == MiHomeInfo.DEVICE_STATE_CLOSE || datum.getDeviceState() == MiHomeInfo.DEVICE_STATE_OPEN)) {
                                ((Device) miHomeItem).deviceState = datum.getDeviceState();
                                miHomeItem.state = MiHomeInfo.STATE_QUERYED;
                            } else {
                                miHomeItem.state = MiHomeInfo.STATE_QUERY_FAIL;
                            }
                            if (datum.getIsOnline() != null) {
                                ((Device) miHomeItem).isOnline = datum.getIsOnline();
                            }
                            if (datum instanceof TXZCarControlHomeManager.TemperatureHumiditySensorDevice && miHomeItem instanceof TemperatureHumiditySensorDevice) {
                                if (((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) datum).getTemperature() != null) {
                                    LogUtil.e("skyward " + ((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) datum).getTemperature());
                                    ((TemperatureHumiditySensorDevice) miHomeItem).temperature = ((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) datum).getTemperature();
                                }
                                if (((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) datum).getHumidity() != null) {
                                    ((TemperatureHumiditySensorDevice) miHomeItem).humidity = ((TXZCarControlHomeManager.TemperatureHumiditySensorDevice) datum).getHumidity();
                                }
                            }
                        }
                    }
                }
                if (mData.mMiHomeItemList.size() == 1) {
                    if (((Device) mData.mMiHomeItemList.get(0)).isOnline == 1) {
                        TtsManager.getInstance().speakText(((Device) mData.mMiHomeItemList.get(0)).deviceName + "为离线状态");
                    } else {
                        if (((Device) mData.mMiHomeItemList.get(0)).deviceState == MiHomeInfo.DEVICE_STATE_CLOSE) {
                            TtsManager.getInstance().speakText(((Device) mData.mMiHomeItemList.get(0)).deviceName + "为关闭状态");
                        } else if (((Device) mData.mMiHomeItemList.get(0)).deviceState == MiHomeInfo.DEVICE_STATE_OPEN) {
                            TtsManager.getInstance().speakText(((Device) mData.mMiHomeItemList.get(0)).deviceName + "为开启状态");
                        } else {
                            LogUtil.e("skyward exception state");
                        }
                    }
                }
                refreshCurrPage();
                boolean canControl = false;
                for (MiHomeItem miHomeItem : mData.mMiHomeItemList) {
                    if (((Device) miHomeItem).canControl) {
                        canControl = true;
                        break;
                    }
                }
                // 设备可控制才会播报控制提示语
                if (canControl) {
                    AppLogic.removeBackGroundCallback(mTipsRunnable);
                    AppLogicBase.runOnBackGround(mTipsRunnable, TIPS_TIME);
                }
                setExitTimeoutRunnable(DEVICE_NO_CONTROL_EXIT_TIME);
                AppLogicBase.removeBackGroundCallback(mQueryDeviceStateTimeoutRunnable);
            }

            @Override
            public void onError(int errorCode) {
                setExitTimeoutRunnable(DEVICE_NO_CONTROL_EXIT_TIME);
                LogUtil.e("queryDeviceState" + errorCode);
                if (isSelecting()) {
                    clearIsSelecting();
                    RecorderWin.open(NativeData.getResString("RS_VOICE_QUERY_DEVICE_STATE_FAIL"));
                }
                AppLogicBase.removeBackGroundCallback(mQueryDeviceStateTimeoutRunnable);
            }
        });
    }


    int mTipTaskId = -1;
    private Runnable mTipsRunnable = new Runnable() {
        @Override
        public void run() {
            mTipTaskId = TtsManager.getInstance().speakText(NativeData.getResString("RS_VOICE_CONTROL_TIPS"));
        }
    };


    private boolean exit = false;

    Runnable mExitTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if(isSelecting() && !exit) {
                exit = true;
                clearIsSelecting();
                RecorderWin.open(NativeData.getResString("RS_VOICE_EXIT_LIST_TIPS"));
            }
        }
    };


    Runnable mQueryDeviceStateTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            isProcessing = false;
            if (isSelecting()) {
                clearIsSelecting();
                RecorderWin.open(NativeData.getResString("RS_VOICE_QUERY_DEVICE_STATE_FAIL"));
            }
        }
    };

    Runnable mControlDeviceTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            isProcessing = false;
            TtsManager.getInstance().speakText(NativeData.getResString("RS_VOICE_CONTROL_TIMEOUT"));
        }
    };

    private void cleanTimeoutRunnable() {
        AppLogicBase.removeBackGroundCallback(mExitTimeoutRunnable);
        AppLogicBase.removeBackGroundCallback(mQueryDeviceStateTimeoutRunnable);
        AppLogicBase.removeBackGroundCallback(mTipsRunnable);
    }


    public interface ActionType {
        int OPEN = 0;
        int CLOSE = 1;
        int QUERY = 2;
    }

    public static class CarControlHomeData {
        public int action;
        public List<MiHomeItem> mMiHomeItemList;
    }

    public static abstract class MiHomeItem {
        public String deviceArea;
        public String deviceLocation;
        public int state;

    }

    public static class TemperatureHumiditySensorDevice extends Device {
        public int temperature;
        public int humidity;
    }

    public static class Device extends MiHomeItem {
        public String deviceId;
        public String deviceName;
        public Boolean canControl;
        /**
         * 0表示开启，1表示关闭
         */
        public int deviceState;
        /**
         * 0表示在线，1表示离线
         */
        public int isOnline;
    }

    public static class AreaDevice extends MiHomeItem {
        public List<Device> devices;
        public String deviceTypeName;
        public String openNum;
        public String closeNum;
    }


    public CarControlHomeWorkChoice(CompentOption<MiHomeItem> option) {
        super(option);
    }

    @Override
    protected void onAddWakeupAsrCmd(AsrUtil.AsrComplexSelectCallback acsc, CarControlHomeData data) {
        acsc.addCommand("CANCEL", NativeData.getResStringArray("RS_CMD_MI_HOME_CANCEL"));
        if (mPage.getMaxPage() > 1) {
            acsc.addCommand("PRE_PAGER", NativeData.getResStringArray("RS_CMD_SELECT_PRE"));
            acsc.addCommand("NEXT_PAGER", NativeData.getResStringArray("RS_CMD_SELECT_NEXT"));
        }
        if (needSureCmd()) {
            acsc.addCommand("SURE", NativeData.getResStringArray("RS_CMD_SELECT_SURE"));
        }

        if (is2_0Version()) {
            if (mPage.getMaxPage() > 1) {
                for (int i = 1; i <= mPage.getMaxPage(); i++) {
                    String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i);
                    acsc.addCommand("PAGE_INDEX_" + i, "第" + strIndex + "页");
                }
            }
        }

        final int currPageSize = mPage.getCurrPageSize();
        if (data.action != ActionType.QUERY) {
            for (int i = 0; i < currPageSize; i++) {
                String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i + 1);
                if (i == 0) {
                    acsc.addCommand("ITEM_INDEX_" + i, "第一个");
                } else {
                    acsc.addCommand("ITEM_INDEX_" + i, "第" + strIndex + "个");
                }
            }
        }

        for (int i = 0; i < currPageSize; i++) {
            String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i + 1);
            if (i == 0) {
                acsc.addCommand("OPEN_ITEM_INDEX_" + i, "打开" + "第一个",
                        "开启" + "第一个", "开" + "第一个", "启动" + "第一个");
            } else {
                acsc.addCommand("OPEN_ITEM_INDEX_" + i, "打开第" + strIndex + "个",
                        "开启第" + strIndex + "个", "开第" + strIndex + "个", "启动第" + strIndex + "个");
            }
        }

        for (int i = 0; i < currPageSize; i++) {
            String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i + 1);
            if (i == 0) {
                acsc.addCommand("CLOSE_ITEM_INDEX_" + i, "关闭" + "第一个",
                        "关上" + "第一个", "关掉" + "第一个", "关了" + "第一个", "关" + "第一个");
            } else {
                acsc.addCommand("CLOSE_ITEM_INDEX_" + i, "关闭第" + strIndex + "个",
                        "关上第" + strIndex + "个", "关掉第" + strIndex + "个",
                        "关了第" + strIndex + "个", "关第" + strIndex + "个");
            }
        }
        if (!hasControlFirstDirect) {
            hasControlFirstDirect = true;
            if (data.mMiHomeItemList.get(0) instanceof AreaDevice && data.mMiHomeItemList.size() == 1) {
                mVoiceAction = data.action;
                selectIndex(0, FLAG_FIRST);
            }
        }
    }

    public static final String FLAG_FIRST = "first";


    @Override
    protected void onConvToJson(CarControlHomeData ts, JSONBuilder jsonBuilder) {
        jsonBuilder.put("type", RecorderWin.MI_HOME_SCENE);
        jsonBuilder.put("prefix", "为你找到以下设备");
        JSONArray jsonArray = new JSONArray();
        String action = "";
        List<MiHomeItem> devices = ts.mMiHomeItemList;
        for (int i = 0; i < devices.size(); i++) {
            JSONBuilder builder = new JSONBuilder();
            if (devices.get(i) instanceof TemperatureHumiditySensorDevice) {
                TemperatureHumiditySensorDevice device = (TemperatureHumiditySensorDevice) devices.get(i);
                builder.put("deviceId", device.deviceId);
                builder.put("deviceName", device.deviceName);
                builder.put("state", device.state);
                builder.put("deviceLocation", device.deviceLocation);
                builder.put("deviceArea", device.deviceArea);
                builder.put("isOnline", device.isOnline);
                builder.put("temperature", device.temperature);
                builder.put("humidity", device.humidity);
                builder.put("deviceState", device.deviceState);
                action = "query";
            } else if (devices.get(i) instanceof Device) {
                Device device = (Device) devices.get(i);
                builder.put("deviceId", device.deviceId);
                builder.put("deviceName", device.deviceName);
                builder.put("state", device.state);
                builder.put("deviceLocation", device.deviceLocation);
                builder.put("deviceArea", device.deviceArea);
                builder.put("isOnline", device.isOnline);
                builder.put("deviceState", device.deviceState);
                action = "query";
            } else if (devices.get(i) instanceof AreaDevice) {
                AreaDevice areaDevice = (AreaDevice) devices.get(i);
                List<Device> deviceLists = areaDevice.devices;
                JSONArray array = new JSONArray();
                if (deviceLists != null && !deviceLists.isEmpty()) {
                    for (Device device : deviceLists) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("canControl", device.canControl);
                            jsonObject.put("deviceId", device.deviceId);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        array.put(jsonObject);
                    }
                }
                builder.put("deviceTypeName", areaDevice.deviceTypeName);
                builder.put("deviceIds", array);
                builder.put("state", areaDevice.state);
                builder.put("deviceLocation", areaDevice.deviceLocation);
                builder.put("deviceArea", areaDevice.deviceArea);
                builder.put("openNum", areaDevice.openNum);
                builder.put("closeNum", areaDevice.closeNum);
                action = "control";
            }
            jsonArray.put(builder.getJSONObject());
        }
        jsonBuilder.put("action", action);
        jsonBuilder.put("devices", jsonArray);
        jsonBuilder.put("count", jsonArray.length());
        jsonBuilder.put("vTips", getTips());
    }

    private String getTips() {
        String tips = "";
        if (mPage != null) {
            if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
                if (mPage.getMaxPage() == 1) {
                    tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_FIRST");
                } else {
                    tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_LAST");
                }
            } else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
                tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_FIRST");
            } else { //其他中间页
                tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE");
            }
        }
        return tips;
    }

    @Override
    protected boolean onCommandSelect(String type, String command) {
        cleanTipsRunnable();
        if (type.startsWith("ITEM_INDEX_")) {
            int index = Integer.parseInt(type.substring("ITEM_INDEX_".length()));
            mVoiceAction = mData.action;
            selectIndex(index, command);
            return true;
        }
        if (type.startsWith("OPEN_ITEM_INDEX_")) {
            int index = Integer.parseInt(type.substring("OPEN_ITEM_INDEX_".length()));
            mVoiceAction = 0;
            selectIndex(index, command);
            return true;
        }
        if (type.startsWith("CLOSE_ITEM_INDEX_")) {
            int index = Integer.parseInt(type.substring("CLOSE_ITEM_INDEX_".length()));
            mVoiceAction = 1;
            selectIndex(index, command);
            return true;
        }

        return super.onCommandSelect(type, command);
    }

    private void cleanTipsRunnable() {
        if (mTipsRunnable != null) {
            AppLogic.removeBackGroundCallback(mTipsRunnable);
            if (mTipTaskId != -1) {
                TtsManager.getInstance().cancelSpeak(mTipTaskId);
            }
        }
    }


    @Override
    protected ResourcePage<CarControlHomeData, MiHomeItem> createPage(CarControlHomeData sources) {
        return new ResourcePage<CarControlHomeData, MiHomeItem>(sources, sources.mMiHomeItemList.size()) {
            @Override
            protected void clearCurrRes(CarControlHomeData currRes) {
                if (currRes != null && currRes.mMiHomeItemList != null) {
                    currRes.mMiHomeItemList.clear();
                    currRes.mMiHomeItemList = null;
                }
            }

            @Override
            protected CarControlHomeData notifyPage(int sIdx, int len, CarControlHomeData sourceRes) {
                List<MiHomeItem> itemList = sourceRes.mMiHomeItemList;
                CarControlHomeData carControlHomeData = new CarControlHomeData();
                carControlHomeData.action = sourceRes.action;
                carControlHomeData.mMiHomeItemList = new ArrayList<MiHomeItem>();
                if (itemList != null) {
                    for (int i = sIdx; i < sIdx + len; i++) {
                        if (i >= 0 && i < itemList.size()) {
                            carControlHomeData.mMiHomeItemList.add(itemList.get(i));
                        }
                    }
                }
                return carControlHomeData;
            }

            @Override
            public int numOfPageSize() {
                return getOption().getNumPageSize();
            }

            @Override
            protected int getCurrResSize(CarControlHomeData currRes) {
                if (currRes != null && currRes.mMiHomeItemList != null) {
                    return currRes.mMiHomeItemList.size();
                }
                return 0;
            }

            @Override
            public MiHomeItem getItemFromCurrPage(int idx) {
                CarControlHomeData carControlHomeData = getResource();
                if (carControlHomeData != null && carControlHomeData.mMiHomeItemList != null && idx >= 0 && idx < carControlHomeData.mMiHomeItemList.size()) {
                    return carControlHomeData.mMiHomeItemList.get(idx);
                }
                return null;
            }

            @Override
            public MiHomeItem getItemFromSource(int idx) {
                CarControlHomeData carControlHomeData = mSourceRes;
                if (carControlHomeData != null && carControlHomeData.mMiHomeItemList != null && idx >= 0 && idx < carControlHomeData.mMiHomeItemList.size()) {
                    return carControlHomeData.mMiHomeItemList.get(idx);
                }
                return null;
            }
        };
    }

    @Override
    protected String convItemToString(MiHomeItem item) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("deviceArea", item.deviceArea);
        jsonBuilder.put("deviceLocation", item.deviceLocation);
        jsonBuilder.put("state", item.state);
        if (item instanceof Device) {
            jsonBuilder.put("deviceId", ((Device) item).deviceId);
            jsonBuilder.put("deviceName", ((Device) item).deviceName);
            jsonBuilder.put("isOnline", ((Device) item).isOnline);
        } else if (item instanceof AreaDevice) {
            jsonBuilder.put("deviceTypeName", ((AreaDevice) item).deviceTypeName);
            List<Device> deviceLists = ((AreaDevice) item).devices;
            JSONArray array = new JSONArray();
            if (deviceLists != null && !deviceLists.isEmpty()) {
                for (Device device : deviceLists) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("canControl", device.canControl);
                        jsonObject.put("deviceId", device.deviceId);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    array.put(jsonObject);
                }
            }
            jsonBuilder.put("deviceIds", array);
        }
        return jsonBuilder.toString();
    }

    @Override
    public String getReportId() {
        return "Mi_Home_Select";
    }

    @Override
    protected void onClearSelecting() {
        super.onClearSelecting();
        cleanTimeoutRunnable();
        isProcessing = false;
    }

}
