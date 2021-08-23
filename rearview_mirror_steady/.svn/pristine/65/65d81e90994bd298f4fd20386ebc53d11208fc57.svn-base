package com.txznet.txz.jni.data;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.Req_GetResString;
import com.txz.ui.data.UiData.Req_Weather;
import com.txz.ui.data.UiData.Resp_LunarDate;
import com.txz.ui.data.UiData.Resp_Weather;
import com.txznet.txz.jni.CommJNI;
import com.txznet.txz.jni.JNIHelper;

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
	public static String getResPlaceholderString(String id, String placeholder, String content){
		return getResString(id, -1).replace(placeholder, content);
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

	public static int getPhoneType(String sPhone) {
		try {
			return Integer.parseInt(new String(getNativeData(
					UiData.DATA_ID_PHONE_TYPE, sPhone)));
		} catch (Exception e) {
			return 4;
		}
	}

	public static UiData.Resp_PhoneArea getPhoneInfo(String sPhone) {
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

	public static com.txz.ui.contact.ContactData.MobileContact findContactByNumber(
			String number) {
		com.txz.ui.contact.ContactData.MobileContacts cons = findContactsByNumber(number);
		if (cons != null && cons.cons.length > 0) {
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
}
