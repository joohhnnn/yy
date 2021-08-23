package com.txznet.comm.remote.util;

import static com.txznet.comm.remote.ServiceManager.TXZ;

import java.lang.reflect.Method;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;

import com.txz.report_manager.ReportManager;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.udprpc.TXZUdpClient;
import com.txznet.comm.remote.udprpc.UdpDataFactory;
import com.txznet.comm.remote.udprpc.UdpDataFactory.UdpData;

import android.os.Bundle;

public class ReportUtil {

	public static long mSessionId = 0;
	public static boolean bNeedReset = true;//是否需要重置SessionId

	private ReportUtil() {
	}

	public static void setSessionId(long id){
		if(bNeedReset){
			mSessionId = id;
			bNeedReset = false;
		}
	}

	/**
	 * 
	 * @param type
	 *            上报类型，参考ReportManager.UAT_类型
	 * @param jsonData
	 * @return
	 */
	public static int doReport(int type, byte[] jsonData) {
		if (GlobalContext.isTXZ()) {
			try {
				Class<?> cls = Class.forName("com.txznet.txz.jni.JNIHelper");
				Method m = cls.getMethod("doReport", int.class, byte[].class);
				m.invoke(cls, type, jsonData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (!TXZUdpClient.getInstance().isInConnection()) {
				ServiceManager.getInstance().sendInvoke(TXZ, "comm.report.type." + type, jsonData, null);
			} else {
				TXZUdpClient.getInstance().sendInvoke(UdpData.CMD_REPORT, UdpDataFactory.combineReportData(type, jsonData));
			}
		}
		return 0;
	}
	
	/**
	 * 立即上报
	 * @param type 上报类型
	 * @param jsonData 上报的json数据
	 * @return
	 */
	public static int doReportImmediate(int type, byte[] jsonData) {
		if(GlobalContext.isTXZ()){
			try {
				Class<?> cls = Class.forName("com.txznet.txz.jni.JNIHelper");
				Method m = cls.getMethod("doReportImmediate", int.class, byte[].class);
				m.invoke(cls, type, jsonData);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			if (!TXZUdpClient.getInstance().isInConnection()) {
				ServiceManager.getInstance().sendInvoke(TXZ, "comm.report.imme." + type, jsonData, null);
			} else {
				TXZUdpClient.getInstance().sendInvoke(UdpData.CMD_REPORT_IMME, UdpDataFactory.combineReportData(type, jsonData));
			}
		}
		return 0;
	}
	
	/**
	 * 立即上报
	 * @param report
	 * @return
	 */
	public static int doReportImmediate(Report report){
	    return doReportImmediate(report.getType(), report.getData().getBytes());
	}
	
	/**
	 * 上报语音行为
	 * @param recognitionType 识别类型,非语音的voice上报填recordType
	 * @param voiceId 
	 * @param jsonData
	 * @return
	 */
	public static int doVoiceReport(int recognitionType, long voiceId, byte[] jsonData) {
	    
	    if (GlobalContext.isTXZ()) {
            try {
                Class<?> cls = Class.forName("com.txznet.txz.jni.JNIHelper");
                Method m = cls.getMethod("doVoiceReport", int.class, long.class, byte[].class);
                m.invoke(cls, recognitionType, voiceId, jsonData);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
	    
	    return 0;
	}
	
	/**
	 * 上报语音行为
	 * @param report
	 * @param recognitionType 识别类型,非语音的voice上报填recordType
	 * @param voiceId
	 * @return
	 */
	public static int doVoiceReport(Report report, int recognitionType, long voiceId){
	    return doVoiceReport(recognitionType, voiceId, report.getData().getBytes());
	}
	
	
	public static int doReport(int type, String json) {
		return doReport(type, json.getBytes());
	}

	public static int doReport(int type, JSONObject jsonContent) {
		String s = jsonContent.toString();
		// Log.i("doReport", s);
		return doReport(type, s);
	}

	public static int doReport(int type, Bundle bundle) {
		JSONObject jsonContent = new JSONObject();

		Set<String> keySet = bundle.keySet(); // 获取所有的Key,
		for (String key : keySet) {
			try {
				jsonContent.put(key, bundle.get(key));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return doReport(type, jsonContent);
	}

	public static int doReport(Report report) {
		return doReport(report.getType(), report.getData());
	}

	public static interface Report {
		int getType();

		String getData();

		public static final class Builder {
			private JSONObject jsonContent;

			public Builder() {
				jsonContent = new JSONObject();
			}
			
			public Builder(String strData) {
				try {
					jsonContent = new JSONObject(strData);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			public Builder setTaskID(String taskID) {
				try {
					jsonContent.put("taskID", taskID);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return this;
			}

			public Builder setKeywords(String kw) {
				try {
					jsonContent.put("keywords", kw);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return this;
			}
			
			public Builder setAction(String action) {
				try {
					jsonContent.put("action", action);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return this;
			}

			public Builder setType(String type) {
				try {
					jsonContent.put("type", type);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return this;
			}
			
			public Builder setRecordType(int recordType) {
				try {
					jsonContent.put("recordType", recordType);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return this;
			}

			public Builder setSessionId() {
				try {
					jsonContent.put("sessionId",mSessionId);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return this;
			}

			public Builder putExtra(String key, Object value) {
				try {
					jsonContent.put(key, value);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return this;
			}

			public Report buildTouchReport() {
				return build();
			}

			public Report buildWakeupReport() {
				try {
					jsonContent.put("scene", "wakeup");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return buildVoiceReport();
			}

			public Report buildVoiceReport() {
				return new Report() {
					@Override
					public String getData() {
						return jsonContent.toString();
					}

					@Override
					public int getType() {
						return ReportManager.UAT_VOICE;
					}
				};
			}
			
			public Report buildTimeVoiceReport() {
				try {
					jsonContent.put("scene", "time");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				return buildVoiceReport();
			}
			
			public Report buildSimReport() {
				return new Report(){

					@Override
					public int getType() {
						return ReportManager.UAT_SIM;
					}

					@Override
					public String getData() {
						return jsonContent.toString();
					}
					
				};
			}
			
			public Report buildSelectReport(){
				return new Report(){

					@Override
					public int getType() {
						return ReportManager.UAT_SELECT;
					}

					@Override
					public String getData() {
						return jsonContent.toString();
					}
				};
			}
			
			public Report buildCommReport(){
				return new Report() {
					
					@Override
					public int getType() {
						return ReportManager.UAT_COMMON;
					}
					
					@Override
					public String getData() {
						return jsonContent.toString();
					}
				};
			}
			
			public Report build() {
				return new Report() {
					@Override
					public String getData() {
						return jsonContent.toString();
					}

					@Override
					public int getType() {
						return ReportManager.UAT_CORE_TOUCH;
					}
				};
			}
		}
	}
}
