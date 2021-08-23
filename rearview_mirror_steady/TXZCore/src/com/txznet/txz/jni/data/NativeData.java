package com.txznet.txz.jni.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import android.text.TextUtils;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.contact.ContactData;
import com.txz.ui.contact.ContactData.MobileContacts;
import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.Req_GetResString;
import com.txz.ui.data.UiData.Req_Weather;
import com.txz.ui.data.UiData.Resp_LunarDate;
import com.txz.ui.data.UiData.Resp_Weather;
import com.txz.ui.data.UiData.TTime;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.jni.CommJNI;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;

/**
 * native本地数据获取类，封装同步获取native数据的方法
 * 
 * @author bihongpi
 *
 */
public class NativeData extends CommJNI {

	// 获取数据
	public static byte[] getNativeData(int dataId, String param) {
		return getNativeData(dataId, param.getBytes());
	}

	public static byte[] getNativeData(int dataId, MessageNano param) {
		return getNativeData(dataId, MessageNano.toByteArray(param));
	}

	public static byte[] getNativeData(int dataId) {
		return getNativeData(dataId, "");
	}

	// //////////////////////////////////////////////////////////////////////////////

	// 获取版本号
	public static String getVersion() {
		byte[] data = getNativeData(com.txz.ui.data.UiData.DATA_ID_VERSION);
		if (null != data)
			return new String(
					getNativeData(com.txz.ui.data.UiData.DATA_ID_VERSION));

		return "";
	}

	// 获取资源字符串
	public static String getResString(String id) {
		return getResString(id, -1);
	}
	
	/**
	 * 获取资源字符串，使用给定的字符串替换占位符
	 * 
	 * @param id
	 *            资源ID
	 * @param placeholder
	 *            占位符
	 * @param content
	 *            替换占位符的字符串
	 * @return 替换后的字符串
	 */
	/*public static String getResPlaceholderString(String id, String placeholder, String content){
		return getResString(id, -1).replace(placeholder, content);
	}*/
	
	public static String getResPlaceholderString(String id, String... res){
		String ret = getResString(id, -1);
		
		if(TextUtils.isEmpty(ret) || null == res){
			return ret;
		}
		
		// 占位符参数错误
		int len = res.length;
		if(0 != len % 2){
			LogUtil.logd("error converting string [" + ret + "] with " + Arrays.toString(res) + ", res length % 2 != 0");
			return ret;
		}
		
		for (int i = 0; i < len - 1; i += 2) {
			if (TextUtils.isEmpty(res[i]) || TextUtils.isEmpty(res[i + 1])) {
				return ret;
			}
			ret = ret.replace(res[i], res[i + 1]);
		}
		
		return ret;
	}
	
	/**
	 * 获取一个ID对应的命令词列表
	 * @param id
	 * @return
	 */
	public static String[] getResStringArray(String id) {
		List<String> cmds = new ArrayList<String>();

		int i = 0;
		for (;;) {
			String cmd = getResString(id, i);
			if (cmd == null || cmd.length() <= 0) {
				break;
			}
			++i;
			cmds.add(cmd);
		}
		return cmds.toArray(new String[cmds.size()]);
	}

	public static String getResString(String id, int index) {
		Req_GetResString req = new Req_GetResString();
		req.strKey = id;
		req.int32Index = Integer.valueOf(index);
		byte[] ret = getNativeData(com.txz.ui.data.UiData.DATA_ID_GET_RES_STR,
				MessageNano.toByteArray(req));
		com.txz.ui.data.UiData.Resp_GetResString rsp;
		try {
			rsp = com.txz.ui.data.UiData.Resp_GetResString.parseFrom(ret);
			return rsp.strValue;
		} catch (InvalidProtocolBufferNanoException e) {
			e.printStackTrace();
			return "";
		}
	}
	/**
	 * 得到json字符串资源
	 * @param id
	 * @return
	 */
	public static String getResJson(String id){
		byte[] byteData = getNativeData(UiData.DATA_ID_GET_RES_JSON,id);
		String ret = new String(byteData);
		return ret;
	}
	

	public static int getPhoneType(String sPhone) {
		try {
			sPhone = convPhoneNum(sPhone);
			return Integer.parseInt(new String(getNativeData(
					UiData.DATA_ID_PHONE_TYPE, sPhone)));
		} catch (Exception e) {
			return 4;
		}
	}

	public static UiData.Resp_PhoneArea getPhoneInfo(String sPhone) {
		sPhone = convPhoneNum(sPhone);
		UiData.Req_PhoneArea pbReqPhoneArea = new com.txz.ui.data.UiData.Req_PhoneArea();
		pbReqPhoneArea.strPhone = sPhone;
		byte[] byteData = getNativeData(
				com.txz.ui.data.UiData.DATA_ID_PHONE_AREA,
				com.txz.ui.data.UiData.Req_PhoneArea
						.toByteArray(pbReqPhoneArea));
		// UiData.Resp_PhoneArea pbRespPhoneArea = new
		// com.txz.ui.data.UiData.Resp_PhoneArea();
		try {
			return UiData.Resp_PhoneArea.parseFrom(byteData);
		} catch (Exception e) {
			e.printStackTrace();
			JNIHelper.loge("byteData.length:" + byteData.length);

		}
		return null;
	}

	// 获取手机号码归属地
	public static String getPhoneArea(String sPhone) {
		sPhone = convPhoneNum(sPhone);
		StringBuilder sbData = new StringBuilder();
		// String sData = new String();
		UiData.Req_PhoneArea pbReqPhoneArea = new com.txz.ui.data.UiData.Req_PhoneArea();
		pbReqPhoneArea.strPhone = sPhone;
		byte[] byteData = getNativeData(
				com.txz.ui.data.UiData.DATA_ID_PHONE_AREA,
				com.txz.ui.data.UiData.Req_PhoneArea
						.toByteArray(pbReqPhoneArea));
		// UiData.Resp_PhoneArea pbRespPhoneArea = new
		// com.txz.ui.data.UiData.Resp_PhoneArea();
		try {
			UiData.Resp_PhoneArea pbRespPhoneArea = UiData.Resp_PhoneArea
					.parseFrom(byteData);
			if (pbRespPhoneArea.bResult) {
				sbData.append(pbRespPhoneArea.strProvince);
				if (!pbRespPhoneArea.strProvince
						.equals(pbRespPhoneArea.strCity)) {
					sbData.append(pbRespPhoneArea.strCity);
				}

				sbData.append(pbRespPhoneArea.strIsp);
			} else {
				sbData.append("");
			}
		} catch (Exception e) {
			e.printStackTrace();
			JNIHelper.loge("byteData.length:" + byteData.length);

		}
		return sbData.toString();
	}
	
	/**
	 * 去掉号码中的符合
	 * @param orginalNum
	 * @return
	 */
	private static String convPhoneNum(String orginalNum) {
		String resultNum = Pattern.compile("[^0-9]").matcher(orginalNum).replaceAll("");
		if (!TextUtils.isEmpty(resultNum)) {
			if (resultNum.startsWith("86")) {
				resultNum = resultNum.substring(2);
			}
			return resultNum;
		}
		return orginalNum;
	}

	// 获取最后保存的位置信息
	public static com.txz.ui.map.UiMap.LocationInfo getLocationInfo() {
		byte[] ret = getNativeData(com.txz.ui.data.UiData.DATA_ID_LOCATION_INFO);
		if (null == ret)
			return null;
		try {
			return com.txz.ui.map.UiMap.LocationInfo.parseFrom(ret);
		} catch (InvalidProtocolBufferNanoException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getLuarDate() {
		Resp_LunarDate date;
		String str = "";
		byte[] byteData = getNativeData(
				com.txz.ui.data.UiData.DATA_ID_LUNAR_DATE, "");
		try {
			date = Resp_LunarDate.parseFrom(byteData);
			str = "农历  ";
			if (date.bLunarLeap)
				str += "闰";
			str += date.strLunarMonth + "月" + date.strLunarDay;
		} catch (Exception e) {
			str = "";
		}
		return str;
	}

	public static Resp_Weather getWeather() {
		Req_Weather pbReqWeather = new Req_Weather();
		// pbReqWeather.strCityName = sCityName;
		byte[] byteData = getNativeData(com.txz.ui.data.UiData.DATA_ID_WEATHER,
				com.txz.ui.data.UiData.Req_Weather.toByteArray(pbReqWeather));
		if (byteData != null) {
			try {
				return Resp_Weather.parseFrom(byteData);
			} catch (Exception e) {
			}
		}

		return null;
	}

	public static int getUID() {
		try {
			byte[] byteData = getNativeData(com.txz.ui.data.UiData.DATA_ID_UID);
			if (byteData != null) {
				return Integer.parseInt(new String(byteData));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static int getServerTime() {
		try {
			byte[] byteData = getNativeData(com.txz.ui.data.UiData.DATA_ID_SERVER_TIME);
			if (byteData != null) {
				return Integer.parseInt(new String(byteData));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
	
	public static TTime getMilleServerTime() {
		TTime tTime = null;
		try {
			byte[] byteData = getNativeData(com.txz.ui.data.UiData.DATA_ID_SERVER_TIME_MSEL);
			if (byteData != null) {
				tTime = TTime.parseFrom(byteData);
				return tTime;
			}
		} catch (Exception e) {
		}
		tTime = new TTime();
		tTime.uint64Time = System.currentTimeMillis();
		tTime.boolConfidence = false;
		return tTime;
	}

	// 获取App配置
	public static com.txz.ui.data.UiData.AppConfig getAppConfig() {
		byte[] byteData = getNativeData(
				com.txz.ui.data.UiData.DATA_ID_CONFIG_APP, "");
		if (byteData != null) {
			try {
				return com.txz.ui.data.UiData.AppConfig.parseFrom(byteData);
			} catch (Exception e) {
			}
		}
		return null;
	}

	// 获取当前用户配置
	public static com.txz.ui.data.UiData.UserConfig getCurUserConfig() {
		return getUserConfig(0);
	}

	// 获取指定的用户配置
	public static com.txz.ui.data.UiData.UserConfig getUserConfig(long uid) {
		byte[] byteData = getNativeData(
				com.txz.ui.data.UiData.DATA_ID_CONFIG_USER,
				("" + uid).getBytes());
		if (byteData != null) {
			try {
				return com.txz.ui.data.UiData.UserConfig.parseFrom(byteData);
			} catch (Exception e) {
			}
		}
		return null;
	}

	// 查找手机联系人信息
	public static com.txz.ui.contact.ContactData.MobileContacts findContactsByNumber(
			String number) {
		byte[] byteData = getNativeData(
				com.txz.ui.data.UiData.DATA_ID_MOBILE_CONTACT_INFO_BY_NUMBER,
				number.getBytes());
		if (byteData != null) {
			try {
				return com.txz.ui.contact.ContactData.MobileContacts
						.parseFrom(byteData);
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	public static com.txz.ui.contact.ContactData.MobileContacts findContactsByName(String name){
		return findContactsByName(name, 6000, ProjectCfg.getMaxShowContactCount());
	}
	
	public static com.txz.ui.contact.ContactData.MobileContacts findContactsByName(String name,Integer score,Integer maxCount) {
		return findContactsByName(name,score,maxCount,null,null,com.txz.ui.contact.ContactData.QUERY_SEARCH_TYPE_SIMILAR,com.txz.ui.contact.ContactData.QUERY_MOBILE_TYPE_UNKNOW);
	}
	
	public static com.txz.ui.contact.ContactData.MobileContacts findContactsByName(String name,byte[] prefix, byte[] suffix, int searchType,int mobileType) {
		return findContactsByName(name, 6000, ProjectCfg.getMaxShowContactCount(), prefix, suffix, searchType, mobileType);
	}

	public static com.txz.ui.contact.ContactData.MobileContacts findContactsByName(String name,Integer score,Integer maxCount, byte[] prefix, byte[] suffix, int searchType,int mobileType){
		ContactData.QueryMobileContacts query = new ContactData.QueryMobileContacts();
		query.name = name;
		query.score = score;
		query.maxCount = maxCount;
		query.strPrefix = prefix;
		query.strSuffix = suffix;
		query.int32SearchType = searchType;
		query.int32MobileType = mobileType;
		
		byte[] byteData = getNativeData(com.txz.ui.data.UiData.DATA_ID_MOBILE_CONTACT_INFO_BY_NAME,query);
		if(byteData != null){
			try {
				return com.txz.ui.contact.ContactData.MobileContacts.parseFrom(byteData);
			} catch (InvalidProtocolBufferNanoException e) {
			}
		}
		return null;
	}
	
	public static com.txz.ui.contact.ContactData.MobileContact findContactByNumber(
			String number) {
		com.txz.ui.contact.ContactData.MobileContacts cons = findContactsByNumber(number);
		if (cons != null && cons.cons.length > 0) {
			return cons.cons[0];
		}
		return null;
	}
	
	public static com.txz.ui.contact.ContactData.MobileContact findContactByName(String name){
		com.txz.ui.contact.ContactData.MobileContacts cons = findContactsByName(name);
		if(cons != null && cons.cons.length > 0){
			return cons.cons[0];
		}
		return null;
	}

	public static String getMeidaPlayUrl(String media) {
		try {
			byte[] byteData = getNativeData(
					com.txz.ui.data.UiData.DATA_ID_CONVER_MEDIA_URL,
					media.getBytes("UTF-8"));
			if (byteData != null) {
				return new String(byteData);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean setTTSConfig(com.txz.ui.tts.UiTts.TTSConfig config) {
		boolean bRet = false;
		try {
			byte[] byteData = getNativeData(com.txz.ui.data.UiData.DATA_ID_TTS_THEME_SET_CONFIG, config);
			bRet = Integer.parseInt(new String(byteData)) != 0;
		} catch (Exception e) {

		}
		return bRet;
	}
	
	public static com.txz.ui.tts.UiTts.TTSConfig getTTSConfig() {
		com.txz.ui.tts.UiTts.TTSConfig pbConfig = null;
		try {
			byte[] byteData = getNativeData(com.txz.ui.data.UiData.DATA_ID_TTS_THEME_GET_CONFIG, "");
			pbConfig = com.txz.ui.tts.UiTts.TTSConfig.parseFrom(byteData);
		} catch (Exception e) {

		}
		return pbConfig;
	}
	
	public static boolean compareStringWithPinyin(String strFirstString, String strSecondString, int minScore){
		boolean bRet = false;
		if (TextUtils.isEmpty(strFirstString) || TextUtils.isEmpty(strSecondString) || minScore < 0){
			return false;
		}
		
		com.txz.ui.data.UiData.Req_CompareStringWithPinYin pbReq = new com.txz.ui.data.UiData.Req_CompareStringWithPinYin();
		pbReq.strFirstString = strFirstString;
		pbReq.strSecondString = strSecondString;
		pbReq.uint32MinScore = minScore;
		try {
			byte[] byteData = getNativeData(com.txz.ui.data.UiData.DATA_ID_COMPARE_STRING_WITH_PINYIN, pbReq);
			bRet = Integer.parseInt(new String(byteData)) != 0;
		} catch (Exception e) {

		}
		return bRet;
	}

	/**
	 * 合并查分包
	 * @param oldfile	旧包
	 * @param patchfile	查分包
	 * @param newfile	新包
	 * @return	合并结果成功或失败
	 */
	public static boolean comboFileByNames(String oldfile, String patchfile,
			String newfile) {
		if (!TextUtils.isEmpty(oldfile) && !TextUtils.isEmpty(patchfile)
				&& !TextUtils.isEmpty(newfile)) {
			UiData.File_ComboPatchNames names = new UiData.File_ComboPatchNames();
			names.strFirstFileName = oldfile;
			names.strSecondFileName = patchfile;
			names.strTargetFileName = newfile;
			byte[] byteData = getNativeData(UiData.DATA_ID_COMBO_PATCH_FILE,
					names);
			return byteData.length == 0 ? false : Integer.parseInt(new String(byteData)) == 0;
		}
		return false;
	}
	
	public static com.txz.ui.app.UiApp.AppInfo findAppInfoByName(String appName) {
		com.txz.ui.app.UiApp.AppInfo appInfo = null;
		try {
			appInfo = findAppInfoByNames(appName).rptMsgApps[0];
		} catch (Exception e) {
		}
		return appInfo;
	}
	
	public static com.txz.ui.app.UiApp.AppInfoList findAppInfoByNames(String appName){
		com.txz.ui.app.UiApp.AppInfoList appInfoList = null;
		try {
			byte[] byteData = getNativeData(UiData.DATA_ID_GET_APP_INFO, appName);
			appInfoList = com.txz.ui.app.UiApp.AppInfoList.parseFrom(byteData);
		} catch (Exception e) {
		}
		return appInfoList;
	}

	public static byte[] invokeNativeData(final String packageName, String command, byte[] data) {
		//TODO 命令字补全 Terry 2017.07.18
		if("getMilleServerTime".equals(command)){
			TTime time = getMilleServerTime();
			return (""+time.uint64Time).getBytes();
		}
		return null;
	}


	public static int initialize_addPluginCommandProcessor(){
		PluginManager.addCommandProcessor("txz.nativeData.", new CommandProcessor() {
			@Override
			public Object invoke(String cmd, Object[] arg1) {
				if ("getVersion".equals(cmd)) {
					return getVersion();
				} else if ("getResString".equals(cmd)) {
					if (arg1.length == 1) {
						return getResString((String) arg1[0]);
					}
					if (arg1.length == 2) {
						return getResString((String) arg1[0], (Integer) arg1[1]);
					}
					JNIHelper.loge("[NavitveData] plugin cmd arg format error:" + cmd);
				} else if ("getResStringArray".equals(cmd)) {
					if (arg1.length == 1) {
						return getResStringArray((String) arg1[0]);
					}
					JNIHelper.loge("[NavitveData] plugin cmd arg format error:" + cmd);
				} else if ("getResJson".equals(cmd)) {
					if (arg1.length == 1) {
						return getResJson((String) arg1[0]);
					}
					JNIHelper.loge("[NavitveData] plugin cmd arg format error:" + cmd);
				} else if ("getPhoneType".equals(cmd)) {
					if (arg1.length == 1) {
						return getPhoneType((String) arg1[0]);
					}
					JNIHelper.loge("[NavitveData] plugin cmd arg format error:" + cmd);
				} else if ("getPhoneInfo".equals(cmd)) {
					if (arg1.length == 1) {
						return getPhoneInfo((String) arg1[0]);
					}
					JNIHelper.loge("[NavitveData] plugin cmd arg format error:" + cmd);
				} else if ("getCurUserConfig".equals(cmd)) {
					return getCurUserConfig();
				} else if ("getUserConfig".equals(cmd)) {
					if (arg1.length == 1) {
						return getUserConfig((Long) arg1[0]);
					}
					JNIHelper.loge("[NavitveData] plugin cmd arg format error:" + cmd);
				} else if ("findContactsByNumber".equals(cmd)) {
					if (arg1.length == 1) {
						return findContactsByNumber((String) arg1[0]);
					}
					JNIHelper.loge("[NavitveData] plugin cmd arg format error:" + cmd);
				} else if ("getMeidaPlayUrl".equals(cmd)) {
					if (arg1.length == 1) {
						return getMeidaPlayUrl((String) arg1[0]);
					}
					JNIHelper.loge("[NavitveData] plugin cmd arg format error:" + cmd);
				} else if ("compareStringWithPinyin".equals(cmd)) {
					if (arg1.length == 3) {
						return compareStringWithPinyin((String) arg1[0], (String) arg1[1], (Integer) arg1[2]);
					}
					JNIHelper.loge("[NavitveData] plugin cmd arg format error:" + cmd);
				} else if ("getLuarDate".equals(cmd)) {
					return getLuarDate();
				}
				return null;
			}
		});
		return 0;
	}
}
