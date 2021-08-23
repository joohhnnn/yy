package com.txznet.txz.module.userconf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

import com.txz.push_manager.PushManager;
import com.txz.report_manager.ReportManager.Req_NotifyUserConfig;
import com.txz.ui.event.UiEvent;
import com.txz.ui.userconfig.UiUserconfig;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.wakeup.WakeupManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.text.TextUtils;

/**
 * @author Andy
 *
 */
public class UserConf  extends IModule{
	public final static int UPDATE_TYPE_CLIENT = 0;
	public final static int UPDATE_TYPE_SERVER = 1;
	public final static String[] EMPTY_WAKEUP_WORDS = new String[0];
	public static final String USERCONF_UPDATE_ACTION = "com.txznet.userconf.update";
	public static final String USERCONF_CORE_UPDATE_ACTION = "com.txznet.userconf.core.update";
	public static final String FACTORY_CONF_CORE_UPDATE_ACTION = "com.txznet.factoryconf.core.update";
	public static final String USERCONF_JSON_CONTENT_NAME = "json_conf";
	public static final String USERCONF_SAVE_DIR = getTXZRootDir();
	public static final String USERCONF_NAME = "userconf.json";
	public static final String FACTORYCONF_NAME = "factoryconf.json";
	public static final String  PACKAGE_NAME_SETTING_APP = "com.txznet.txzsetting";
	private boolean bUseSettingApp = false;
	
	private ConfigData mUserConfigData = null;
	private ConfigData mFactoryConfigData = null;//保存方案商设置的配置
	private BroadcastReceiver mRecerver = new BroadcastReceiver(){
		@Override
		public void onReceive(Context context, Intent intent) {
			if (USERCONF_UPDATE_ACTION.equals(intent.getAction())){
				String strJsonConf = intent.getStringExtra(USERCONF_JSON_CONTENT_NAME);
				parseUserConf(strJsonConf);
			}
		}
	};
	
	public static String getTXZRootDir(){
		String rootDir = "/sdcard/txz";//默认是该路径
		File sdcard = Environment.getExternalStorageDirectory();
		//sdcard如果没有挂载上的话,可能为null
		if (sdcard != null){
			rootDir = sdcard.getPath() + File.separator + "txz";//测试表明,路劲中重复的File.separator会被忽略
		}
		LogUtil.logd("UserConf TXZRootDir : " + rootDir);
		return rootDir;
	}
	
	private static UserConf sIntance = new UserConf();
	
	private UserConf(){
		mUserConfigData = new ConfigData();
		mFactoryConfigData = new ConfigData();
		mFactoryConfigData.mWakeupThreshholdVal = WakeupManager.DEFAULT_WAKEUP_THRESHHLOD;//默认初始化唤醒阈值
		bUseSettingApp = PackageManager.getInstance().checkAppExist(PACKAGE_NAME_SETTING_APP) || !TextUtils.isEmpty(ProjectCfg.getSDKSettingPackage());

		LogUtil.logd("UserConf bUseSettingApp : " + bUseSettingApp);
	}
	
	public static UserConf getInstance(){
		return sIntance;
	}
	
	public ConfigData getUserConfigData(){
		return mUserConfigData;
	}
	
	public ConfigData getFactoryConfigData(){
		return mFactoryConfigData;
	}
	
	public void setFactoryConfigData(){
		
	}
	
	public void saveFactoryConfigData(){
		if (bUseSettingApp){
			saveUserConfig(mFactoryConfigData.toJson(), USERCONF_SAVE_DIR + File.separator + FACTORYCONF_NAME);
			Intent intent = new Intent(FACTORY_CONF_CORE_UPDATE_ACTION);
			AppLogic.getApp().sendBroadcast(intent);
		}
	}
	
	public void saveUserConfigData(){
		saveUserConfig(mUserConfigData.toJson(), USERCONF_SAVE_DIR + File.separator + USERCONF_NAME);
		notifyConfigChanged(mUserConfigData.toJson(), UPDATE_TYPE_SERVER);
	}
	
	@Override
	public int initialize_BeforeStartJni() {
		//不要放在构造方法中,避免互相getIntance出现空指针
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(USERCONF_UPDATE_ACTION);
		AppLogic.getApp().registerReceiver(mRecerver, intentFilter);
		
		// 注册需要处理的事件
		regEvent(UiEvent.EVENT_USER_CONFIG,  UiUserconfig.SUBEVENT_SERVER_USERCONF_UPDATE_REQ);
		return super.initialize_BeforeStartJni();
	}

	@Override
	public int initialize_AfterStartJni() {
		loadUserConf();
		return super.initialize_AfterStartJni();
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		if (UiEvent.EVENT_USER_CONFIG == eventId){
			switch(subEventId){
			case UiUserconfig.SUBEVENT_SERVER_USERCONF_UPDATE_REQ:
				parseServerUserConf(data);
				break;
			default:
				break;
			}
		}
		return super.onEvent(eventId, subEventId, data);
	}
	
	private void parseServerUserConf(byte[] data) {
		try {
			PushManager.PushCmd_NotifyUserConfig pbUserConfig = PushManager.PushCmd_NotifyUserConfig.parseFrom(data);
			String strJson = new String(pbUserConfig.msgUserconfig.strConfig);
			mUserConfigData.parse(strJson);
			useUserConfigImmediately();
			saveUserConfig(strJson, USERCONF_SAVE_DIR + File.separator + USERCONF_NAME);
			notifyConfigChanged(strJson, UPDATE_TYPE_SERVER);
		} catch (Exception e) {
			LogUtil.logw("UserConf : " + e.toString());
		}
	}
	
	private void useUserConfigImmediately(){
		WakeupManager.getInstance().mergeWakeupWords();
	}
	
	private void notifyConfigChanged(String strJson, int type){
		//通知对方更新
		if (UPDATE_TYPE_SERVER == type){
			Intent intent = new Intent(USERCONF_CORE_UPDATE_ACTION);
			AppLogic.getApp().sendBroadcast(intent);
		}else if (UPDATE_TYPE_CLIENT == type){
			Req_NotifyUserConfig pbReq = new Req_NotifyUserConfig();
			pbReq.msgUserconfig = new UiUserconfig.UserConfig();
			pbReq.msgUserconfig.strConfig = strJson.getBytes();
			JNIHelper.sendEvent(UiEvent.EVENT_USER_CONFIG, UiUserconfig.SUBEVENT_CLIENT_USERCONF_UPDATE_REQ, pbReq);
		}
	}
	
	private void saveUserConfig(String strJson, String strUserConfPath){
		Writer writer = null;
		try{
			writer = new FileWriter(strUserConfPath);
			writer.write(strJson);
			writer.flush();
		}catch(Exception e){
			LogUtil.logw("UserConf : " + e.toString());
		}
		
		if (writer != null){
			try{
				writer.close();
			}catch(Exception e){
				LogUtil.logw("UserConf : " + e.toString());
			}
		}
	}
	
	
	private void loadUserConf() {
		String strUserConf = USERCONF_SAVE_DIR + File.separator + USERCONF_NAME;
		File f = new File(strUserConf);
		if (!f.exists()){
			return;
		}
		String strJson =getJson(strUserConf);
		mUserConfigData.parse(strJson);
		useUserConfigImmediately();
	}
	
	//读取json格式的文件,自动去除换行符,适合短文本
	public String getJson(String strUserConf){
		Reader reader = null;
		try {
			reader = new FileReader(strUserConf);
		} catch (Exception e) {
			LogUtil.logw("UserConf : " + e.toString());
			return "";
		}

		BufferedReader bufferReader = new BufferedReader(reader);
		StringBuffer stringBuffer = new StringBuffer();
		if (bufferReader != null) {
			while (true) {
				try {
					String line = bufferReader.readLine();
					if (line == null) {
						break;
					}
					stringBuffer.append(line);
				} catch (Exception e) {
					LogUtil.logw("UserConf : " + e.toString());
					break;
				}
			}
		}
		if (bufferReader != null) {
			try {
				bufferReader.close();
			} catch (Exception e) {
				LogUtil.logw("UserConf : " + e.toString());
			}
		}
		return stringBuffer.toString();
	}
	
	private void parseUserConf(String strJson) {
		mUserConfigData.parse(strJson);
		useUserConfigImmediately();
		//主题样式的仅需要在改变的时候读取
		WinManager.getInstance().onSettingConfigUpgrade();
		saveUserConfig(strJson, USERCONF_SAVE_DIR + File.separator + USERCONF_NAME);
		notifyConfigChanged(strJson, UPDATE_TYPE_CLIENT);
	}
}
