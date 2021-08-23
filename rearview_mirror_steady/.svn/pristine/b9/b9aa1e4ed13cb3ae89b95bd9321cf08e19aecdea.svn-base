package com.txznet.txz.module.sensor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;

import com.amap.api.services.core.bf;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.equipment_manager.EquipmentManager.Req_ReportGSensor;
import com.txz.equipment_manager.EquipmentManager.Resp_ReportGSensor;
import com.txz.ui.data.UiData.TTime;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.gsensor.UiGsensor.GSensorData;
import com.txz.ui.gsensor.UiGsensor.GSensorDataList;
import com.txz.ui.platform.UiPlatform;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.runnables.Runnable1;

public class SensorControlManager extends IModule {

	private static SensorControlManager sInstance = new SensorControlManager();
	private SensorManager mSensorManager;
	private static final String TAG = "Sensor:";
	private SensorEventListener mSensorListener;

	private long mLastTimestamp;
	private long mTimestamp;
	private double mAccX;
	private double mAccY;
	private double mAccZ;
	private long mReportId = 0;
	private int mCollectTime = 100;//默认采集周期
	private int mSaveTime = 5 * 60 * 1000;//定时落地保存周期
	private int mReportTime = 30 * 60 * 1000;//默认上报数据周期
	private int mReportMaxCount = 5 * 60 * 10;//一次上报的最大数量
	private int mSaveMaxCount = 30 * 60 * 10 * 4;//最多保存的数据量，超过此数据量进行丢弃
	private int mTimeOut = 30 * 1000;//超时时间
	private boolean bFirstReport = true;//第一次上报
	private boolean bInited = false;
	
	private Sensor accSensor;
	private Timer mSensorTimer;
	private LinkedList<GSensorData> mDataList = new LinkedList<GSensorData>();//存储所有新增数据
	private LinkedList<GSensorData> mReportDataList = new LinkedList<GSensorData>();//存储具体需要上传的数据
	
	private String mFilePath = Environment.getExternalStorageDirectory().getPath() + "/txz/.GSensorTemp";
	
	private SensorControlManager() {

	}

	public static SensorControlManager getInstance() {
		return sInstance;
	}

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_BEFORE_SLEEP);
		regEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_SLEEP);
		regEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_WAKEUP);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,UiEquipment.SUBEVENT_RESP_REPORT_GSENSOR);
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterInitSuccess() {
		init();
		return super.initialize_AfterInitSuccess();
	}

	private void start() {
		mSensorManager.registerListener(mSensorListener, accSensor, SensorManager.SENSOR_DELAY_GAME);
		mSensorTimer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						if(mTimestamp != mLastTimestamp){//时间戳不同，采用数据
//							LogUtil.logd(TAG + " accX:" + mAccX + " accY:" + mAccY + " accZ:" + mAccZ);
							if(mDataList.size() > mSaveMaxCount){
								mDataList.remove(0);
							}
							GSensorData gSensorData = new GSensorData();
							gSensorData.doubleAxisX = mAccX;
							gSensorData.doubleAxisY = mAccY;
							gSensorData.doubleAxisZ = mAccZ;
							TTime tTime = NativeData.getMilleServerTime();
							gSensorData.uint64CollectTime = tTime.uint64Time;
							gSensorData.boolConfidence = tTime.boolConfidence;
							mDataList.add(gSensorData);
						}
						mLastTimestamp = mTimestamp;
					}
				}, 0);
			}
		}, 0, mCollectTime);
		mSensorTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				AppLogic.runOnUiGround(new Runnable() {
					
					@Override
					public void run() {
						reportGSensorData();
					}
				}, 0);
			}
		}, 0, mReportTime);
		
		mSensorTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				AppLogic.runOnUiGround(new Runnable() {
					
					@Override
					public void run() {
						saveGSensorToFile();
					}
				}, 0);
			}
		}, 0, mSaveTime);
		
	}

	private void release() {
		if(!bInited){
			return;
		}
		saveGSensorToFile();
		if(mSensorManager != null){
			mSensorManager.unregisterListener(mSensorListener);
		}
		if(mSensorTimer != null){
			mSensorTimer.cancel();
		}
		bFirstReport = true;
		bInited = false;
	}

	private void init() {
		if(!enableCollectGSensor()){
			return;
		}
		AppLogic.runOnBackGround(mInitRunnable, 0);
		
	}
	
	Runnable mInitRunnable = new Runnable() {
		
		@Override
		public void run() {
			mSensorManager = (SensorManager) GlobalContext.get().getSystemService(Context.SENSOR_SERVICE);
			mSensorTimer = new Timer("sensorTimer");
			mDataList = getGSensorFromFile();

			List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
			for (Sensor s : sensorList) {
				JNIHelper.logd(TAG + s.getName());
			}

			mSensorListener = new SensorEventListener() {

				@Override
				public void onSensorChanged(SensorEvent event) {
					switch (event.sensor.getType()) {
					case Sensor.TYPE_ACCELEROMETER:
						mTimestamp = event.timestamp;
						mAccX = event.values[0];
						mAccY = event.values[1];
						mAccZ = event.values[2];
						break;
					default:
						break;
					}
				}

				@Override
				public void onAccuracyChanged(Sensor sensor, int accuracy) {
				}
			};

			accSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			if (accSensor == null) {
				LogUtil.loge(TAG + " accelerometer sensor is null");
				return;
			}
			mReportMaxCount = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_GSENSOR_REPORT_MAX_COUNT, 5 * 60 * 10);
			mSaveMaxCount =  PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_GSENSOR_SAVE_MAX_COUNT, 30 * 60 * 10 * 4);
			mCollectTime = PreferenceUtil.getInstance().getInt(PreferenceUtil.KEY_GSENSOR_COLLECT_TIME, 100);
			bInited = true;
			start();
		}
	};
	
	/**
	 * 后台是否配置开启GSensor数据采集上报
	 * @return
	 */
	private boolean enableCollectGSensor(){
		UiEquipment.ServerConfig pbServerConfig = ConfigManager.getInstance()
				.getServerConfig();
		if (pbServerConfig == null
				|| pbServerConfig.uint64Flags == null
				|| ((pbServerConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_ENABLE_COLLECT_GSENSOR) == 0)) {
			LogUtil.logd(TAG + "enableCollectGSensor false");
			return false;
		}
		LogUtil.logd(TAG + "enableCollectGSensor true");
		return true;
	}
	
	/**
	 * 上报GSensor数据
	 * @return
	 */
	private boolean reportGSensorData(){
		if (mReportDataList.isEmpty()) {
			if (mDataList.size() < mReportMaxCount) {
				if(bFirstReport){//第一次上传时即使数据量不满足上报量也要上传
					mReportDataList.addAll(mDataList);
					mDataList.clear();
					bFirstReport = false;
				}else{
					return false;
				}
			}else{
				mReportDataList = new LinkedList<GSensorData>(mDataList.subList(0,
					mReportMaxCount));
				mDataList.removeAll(mReportDataList);
			}
		}
		return reportData();
	}
	
	private boolean reportData(){
		Req_ReportGSensor req_ReportGSensor = new Req_ReportGSensor();
		GSensorDataList gSensorDataList = new GSensorDataList();
		gSensorDataList.rptGsensorData = mReportDataList.toArray(new GSensorData[mReportDataList.size()]);
		req_ReportGSensor.rptGsensorDataList = gSensorDataList;
		req_ReportGSensor.uint64ReportId = NativeData.getMilleServerTime().uint64Time;
		mReportId = req_ReportGSensor.uint64ReportId;
		LogUtil.logd(TAG + "reportId = "+req_ReportGSensor.uint64ReportId);
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REPORT_GSENSOR, Req_ReportGSensor.toByteArray(req_ReportGSensor));
		LogUtil.logd(TAG + "report gsensor data AllCount = "+mDataList.size()+" ,reportCount = " + mReportDataList.size());
		AppLogic.removeUiGroundCallback(runnableReportTimeout);
		AppLogic.runOnUiGround(runnableReportTimeout, mTimeOut);
		return true;
	}
	
	Runnable runnableReportTimeout = new Runnable() {
		
		@Override
		public void run() {
			LogUtil.logw(TAG + "report timeout");
			reportData();
		}
	};
	
	/**
	 * 清除过多的数据
	 */
	private void cleanReportList() {
		if(mDataList.size() > mSaveMaxCount){
			LogUtil.logd(TAG + "discard over data size = " + (mDataList.size() - mSaveMaxCount));
			mDataList.subList(0, mDataList.size() - mSaveMaxCount).clear();
		}
	}

	/**
	 * 落地保存
	 * 定时保存和休眠保存
	 * @return
	 */
	private boolean saveGSensorToFile(){
		if(!bInited){
			LogUtil.logd(TAG + "no init to save size");
			return false;
		}
		cleanReportList();
		GSensorDataList gSensorDataList = new GSensorDataList();
		gSensorDataList.rptGsensorData = mDataList.toArray(new GSensorData[mDataList.size()]);
		LogUtil.logd(TAG + "save size = "+gSensorDataList.rptGsensorData.length);
		AppLogic.runOnBackGround(new Runnable1<GSensorDataList>(gSensorDataList) {

			@Override
			public void run() {
				byte[] dataBytes = GSensorDataList.toByteArray(mP1);
				File saveFile = new File(mFilePath);
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(saveFile);
					fos.write(dataBytes);
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (fos != null) {
							fos.close();
						}
					} catch (IOException e) {
					}
				}
			}
		}, 0);
		return true;
	}
	
	/**
	 * 从文件中将数据读到内存
	 * @return
	 */
	private LinkedList<GSensorData> getGSensorFromFile(){
		File file = new File(mFilePath);
		LinkedList<GSensorData> listData = new LinkedList<GSensorData>();
		if(!file.exists() || file.length() == 0){
			return listData;
		}
		byte[] bytesData = new byte[(int) file.length()];
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			fis.read(bytesData);
			GSensorDataList dataList = GSensorDataList.parseFrom(bytesData);
			Collections.addAll(listData,dataList.rptGsensorData);
			LogUtil.logd(TAG + "readFile size = " + dataList.rptGsensorData.length);
		} catch (IOException e) {
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e) {
			}
		}
		file.delete();
		return listData;
	}
	
	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_SYSTEM_PLATFORM:
			switch (subEventId) {
			case UiPlatform.SUBEVENT_POWER_ACTION_BEFORE_SLEEP:
				release();
				break;
			case UiPlatform.SUBEVENT_POWER_ACTION_SLEEP:
				
				break;
			case UiPlatform.SUBEVENT_POWER_ACTION_WAKEUP:
				init();
				break;
			default:
				break;
			}
			break;
		case UiEvent.EVENT_ACTION_EQUIPMENT:
			if(!bInited){
				break;
			}
			switch (subEventId) {
			case UiEquipment.SUBEVENT_RESP_REPORT_GSENSOR://收到后台返回的结果
				if(data == null || data.length == 0){
					break;
				}
				try {
					Resp_ReportGSensor resp_ReportGSensor = Resp_ReportGSensor.parseFrom(data);
					if(resp_ReportGSensor == null || resp_ReportGSensor.uint64ReportId == null){
						LogUtil.logd(TAG + "SUBEVENT_RESP_REPORT_GSENSOR data is null");
						break;
					}
					
					LogUtil.logd(TAG + "SUBEVENT_RESP_REPORT_GSENSOR Reportid = "+resp_ReportGSensor.uint64ReportId+" ,KeepCount = " + resp_ReportGSensor.uint32KeepCount
							+ " ,MaxCount = "+resp_ReportGSensor.uint32MaxCount + " ,Interval = "+resp_ReportGSensor.uint32SampleInterval);
					if(resp_ReportGSensor.uint32KeepCount != null && resp_ReportGSensor.uint32KeepCount != mSaveMaxCount){
						mSaveMaxCount = resp_ReportGSensor.uint32KeepCount;
						PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_GSENSOR_SAVE_MAX_COUNT, mSaveMaxCount);
					}
					if(resp_ReportGSensor.uint32MaxCount != null && resp_ReportGSensor.uint32MaxCount != mReportMaxCount){
						mReportMaxCount = resp_ReportGSensor.uint32MaxCount;
						PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_GSENSOR_REPORT_MAX_COUNT, mReportMaxCount);
					}
					if(resp_ReportGSensor.uint32SampleInterval != null && resp_ReportGSensor.uint32SampleInterval != mCollectTime){
						mCollectTime = resp_ReportGSensor.uint32SampleInterval;
						PreferenceUtil.getInstance().setInt(PreferenceUtil.KEY_GSENSOR_COLLECT_TIME, mCollectTime);
					}
					if(resp_ReportGSensor.uint64ReportId == mReportId){
						AppLogic.removeUiGroundCallback(runnableReportTimeout);
						mReportDataList.clear();
						reportGSensorData();
					}
				} catch (InvalidProtocolBufferNanoException e) {
					LogUtil.loge(e.getMessage());
				}
				break;

			default:
				break;
			}

		default:
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}

}
