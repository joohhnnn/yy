package com.txznet.txz.component.nav.baidu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.poi.PoiComparator_Distance;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.CoordType;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.os.SystemClock;
import android.text.TextUtils;

public class NavBaiduFactory implements BDConstants {
	public static class RouteDetails {
		public int trafficlightcnt;
		public int distance;
		public int tollfees;
		public int totaltime;
		public int gasmoney;

		public static RouteDetails getNodeFromJson(String strData) {
			RouteDetails rd = new RouteDetails();
			try {
				JSONObject jo = new JSONObject(strData);
				if (jo.has("trafficlightcnt")) {
					rd.trafficlightcnt = jo.optInt("trafficlightcnt");
				}
				if (jo.has("distance")) {
					rd.distance = jo.optInt("distance");
				}
				if (jo.has("tollfees")) {
					rd.tollfees = jo.optInt("tollfees");
				}
				if (jo.has("totaltime")) {
					rd.totaltime = jo.optInt("totaltime");
				}
				if (jo.has("gasmoney")) {
					rd.gasmoney = jo.optInt("gasmoney");
				}
			} catch (JSONException e) {
			}
			return rd;
		}
	}

	public static class JBuilder {
		JSONObject jsonObject;

		public JBuilder() {
			jsonObject = new JSONObject();
		}

		public JBuilder put(String key, String value) {
			try {
				jsonObject.put(key, value);
			} catch (JSONException e) {
			}
			return this;
		}

		public JBuilder put(String key, int value) {
			try {
				jsonObject.put(key, value);
			} catch (JSONException e) {
			}
			return this;
		}

		public JBuilder put(String key, JSONObject obj) {
			try {
				jsonObject.put(key, obj);
			} catch (JSONException e) {
			}
			return this;
		}
		public JBuilder put(final String key, final JSONArray obj) {
			try {
				jsonObject.put(key, obj);
			} catch (JSONException e) {
			}
			return this;
		}

		public String build() {
			if (jsonObject != null) {
				return jsonObject.toString();
			}
			return "";
		}

		public JSONObject buildObj() {
			return jsonObject;
		}


	}

	public static class PoiNode {
		public int lat;
		public int lng;
		public String name;
		public String address;
		public String city;
		public static PoiNode getNodeFromJson(String strData) {
			PoiNode poiNode = new PoiNode();
			try {
				JSONObject jo = new JSONObject(strData);
				if (jo.has("lat")) {
					poiNode.lat = jo.optInt("lat");
				}
				if (jo.has("lng")) {
					poiNode.lng = jo.optInt("lng");
				}
				if (jo.has("name")) {
					poiNode.name = jo.optString("name");
				}
				if (jo.has("address")) {
					poiNode.address = jo.optString("address");
				}
			} catch (JSONException e) {
			}
			return poiNode;
		}

		public static PoiNode getNodeFromJson(JSONObject jo) {
			PoiNode poiNode = new PoiNode();
			try {
				if (jo.has("lat")) {
					poiNode.lat = jo.optInt("lat");
				}
				if (jo.has("lng")) {
					poiNode.lng = jo.optInt("lng");
				}
				if (jo.has("name")) {
					poiNode.name = jo.optString("name");
				}
				if (jo.has("address")) {
					poiNode.address = jo.optString("address");
				}
			} catch (Exception e) {
			}
			return poiNode;
		}
	}

	public static class NavPathInfo {
		public RouteDetails[] rds;
		public PoiNode[] pns;
		public int passCount;
		public PoiNode startNode;
		public PoiNode endNode;
	}

	public static NavPathInfo getNavInfoFromQueryResult(String strData) {
		JSONBuilder jb = new JSONBuilder(strData);
		int passCount = jb.getVal("passing_point", Integer.class);
		JSONArray passArray = jb.getVal("pass_node", JSONArray.class);
		JSONObject endNode = jb.getVal("end_node", JSONObject.class);
		NavPathInfo navPathInfo = new NavPathInfo();
		PoiNode[] pns = new PoiNode[passCount];
		try {
			for (int i = 0; i < passCount; i++) {
				pns[i] = PoiNode.getNodeFromJson(passArray.getJSONObject(i));
			}
			navPathInfo.pns = pns;
		} catch (JSONException e) {
		}

		navPathInfo.endNode = PoiNode.getNodeFromJson(endNode);
		return navPathInfo;
	}

	public static NavPathInfo getNavPathInfoFromJson(String jsonStr) {
		JSONBuilder jb = new JSONBuilder(jsonStr);
		JSONArray detailArray = jb.getVal("route_details", JSONArray.class);
		int passCount = jb.getVal("passing_point", Integer.class);
		JSONArray passArray = jb.getVal("pass_node", JSONArray.class);
		JSONObject sJson = jb.getVal("start_node", JSONObject.class);
		JSONObject eJson = jb.getVal("end_node", JSONObject.class);

		NavPathInfo navPathInfo = new NavPathInfo();
		try {
			if (detailArray != null) {
				RouteDetails[] rds = new RouteDetails[detailArray.length()];
				for (int i = 0; i < detailArray.length(); i++) {
					rds[i] = RouteDetails.getNodeFromJson(detailArray.get(i).toString());
				}
				navPathInfo.rds = rds;
			}

			if (passCount > 0) {
				if (passArray != null) {
					PoiNode[] pns = new PoiNode[passCount];
					for (int i = 0; i < passArray.length(); i++) {
						pns[i] = PoiNode.getNodeFromJson(passArray.get(i).toString());
					}
					navPathInfo.pns = pns;
				}
			}
			if (sJson != null) {
				navPathInfo.startNode = PoiNode.getNodeFromJson(sJson);
			}
			if (eJson != null) {
				navPathInfo.endNode = PoiNode.getNodeFromJson(eJson);
			}
		} catch (Exception e) {
		}
		return navPathInfo;
	}

	/**
	 * 沿途搜索数据结构
	 * 
	 */
	public static class WayPoiData implements IAsyncData {

		List<WayPoi> wayPois;

		public WayPoiData setWayPois(List<WayPoi> wayPois) {
			this.wayPois = wayPois;
			return this;
		}

		public List<WayPoi> getWayPois() {
			return this.wayPois;
		}

		public List<Poi> getPois() {
			if (wayPois == null) {
				return null;
			}

			List<Poi> pois = new ArrayList<Poi>();
			for (int i = 0; i < wayPois.size(); i++) {
				pois.add(wayPois.get(i).convertToPoi());
			}
			return pois;
		}

		public static class WayPoi {
			public String name;
			public String address;
			public int lat;
			public int lng;
			public int distance;

			public Poi convertToPoi() {
				Poi poi = new Poi();
				poi.setName(name);
				poi.setDistance(distance);
				poi.setCoordType(CoordType.GCJ02);
				poi.setLat(BDHelper.convertDouble(lat));
				poi.setLng(BDHelper.convertDouble(lng));
				poi.setGeoinfo(address);
				poi.setAction(PoiAction.ACTION_JINGYOU);
				return poi;
			}
		}

		public static WayPoiData convertToWayPoiData(String strData) {
			try {
				JSONBuilder jb;
				JSONArray jsonArray;
				jb=new JSONBuilder(strData);
				String s=jb.getVal("res",String.class);
				jsonArray=new JSONArray(s);
				List<WayPoi> wayPois = new ArrayList<WayPoi>();
				for (int i = 0; i < jsonArray.length(); i++) {
					WayPoi wayPoi = new WayPoi();
					JSONObject jo = (JSONObject) jsonArray.get(i);
					if (jo.has("name")) {
						wayPoi.name = jo.optString("name");
					}
					if (jo.has("address")) {
						wayPoi.address = jo.optString("address");
					}
					if (jo.has("lat")) {
						wayPoi.lat = jo.optInt("lat");
					}
					if (jo.has("lng")) {
						wayPoi.lng = jo.optInt("lng");
					}
					if (jo.has("distance")) {
						wayPoi.distance = jo.optInt("distance");
					}
					wayPois.add(wayPoi);
				}

				Collections.sort(wayPois, new PoiComparator_Distance());

				return new WayPoiData().setWayPois(wayPois);
			} catch (Exception e) {
			}
			return new WayPoiData();
		}
	}

	public static interface IAsyncData {
	}

	public static class ViaSearchCommand extends AsrComplexSelectCallback{
		Map<String, Runnable> keySelectRunMap;
		public String taskId;

		public ViaSearchCommand(String taskId){
			this.taskId=taskId;
		}

		public ViaSearchCommand setTaskId(String taskId) {
			this.taskId = taskId;
			return this;
		}

		@Override
		public boolean needAsrState() {
			return false;
		}

		@Override
		public String getTaskId() {
			return taskId;
		}

		@Override
		public void onCommandSelected(String type, String command) {
			Runnable selectRun = keySelectRunMap.get(type);
			if (selectRun != null) {
				selectRun.run();
			}

			destory();
		}

		public void destory(){
			if (keySelectRunMap != null) {
				keySelectRunMap.clear();
			}

			if (!TextUtils.isEmpty(getTaskId())) {
				WakeupManager.getInstance().recoverWakeupFromAsr(getTaskId());
			}
		}

		public void addCmds(String type, Runnable selectRun, String... cmds) {
			if (keySelectRunMap == null) {
				keySelectRunMap = new HashMap<String, Runnable>();
			}
			keySelectRunMap.put(type, selectRun);
			addCommand(type, cmds);
		}

		public void build() {
			WakeupManager.getInstance().useWakeupAsAsr(this);
		}
	}

	public static class NotifyDialog extends AsrComplexSelectCallback {
		public String taskId;
		Map<String, Runnable> keySelectRunMap;
		Runnable destoryListener;

		public NotifyDialog setTaskId(String taskId) {
			this.taskId = taskId;
			return this;
		}

		public NotifyDialog setDestoryListener(Runnable run) {
			destoryListener = run;
			return this;
		}

		public void addCmds(String type, Runnable selectRun, String... cmds) {
			if (keySelectRunMap == null) {
				keySelectRunMap = new HashMap<String, Runnable>();
			}
			keySelectRunMap.put(type, selectRun);
			addCommand(type, cmds);
		}

		public void build() {
			WakeupManager.getInstance().useWakeupAsAsr(this);
		}

		public void onResume() {
			build();
		}

		public void onPause() {
			if (!TextUtils.isEmpty(getTaskId())) {
				WakeupManager.getInstance().recoverWakeupFromAsr(getTaskId());
			}
		}

		public void destory() {
			if (keySelectRunMap != null) {
				keySelectRunMap.clear();
			}

			if (!TextUtils.isEmpty(getTaskId())) {
				WakeupManager.getInstance().recoverWakeupFromAsr(getTaskId());
				taskId = "";
				if (destoryListener != null) {
					destoryListener.run();
				}
			}
		}

		@Override
		public boolean needAsrState() {
			return false;
		}

		@Override
		public String getTaskId() {
			return taskId;
		}

		@Override
		public void onCommandSelected(String type, String command) {
			Runnable selectRun = keySelectRunMap.get(type);
			if (selectRun != null) {
				selectRun.run();
			}

			destory();
		}
	}

	public static class RequestRecord {
		public static final int TYPE_WAY_POI = 0x01;

		public int type;
		public String speech;
		public String keywords;
		public String requestId;

		public void execQuery(int type, String params) {
			if (type == TYPE_WAY_POI) {
				if ("Gas_Station".equals(params)) {
					keywords = "加油站";
				} else if ("Bank".equals(params)) {
					keywords = "银行";
				} else if ("Toilet".equals(params)) {
					keywords = "厕所";
				} else if ("Spots".equals(params)) {
					keywords = "景点";
				} else if ("Hotel".equals(params)) {
					keywords = "酒店";
				} else if ("Restaurant".equals(params)) {
					keywords = "餐饮";
				} else if ("Service".equals(params)) {
					keywords = "服务区";
				} else if ("Park".equals(params)) {
					keywords = "停车场";
				}

				this.type = type;
				requestId = BDHelper.queryByNaviWayPoint(requestId, params);
			}
		}

		public IAsyncData onParseStrData(String requestId, String strData) {
			if (!this.requestId.equals(requestId)) {
				JNIHelper.loge("no equals requestId");
				return null;
			}

			if (this.type == TYPE_WAY_POI) {
				return WayPoiData.convertToWayPoiData(strData);
			}

			return null;
		}
	}

	public static abstract class DialogRecord extends WinConfirmAsr {
		public DialogRecord(WinConfirmAsrBuildData data) {
			super(data);
		}

		public static final String TASK_ID = "DIALOGRECORD_TASK_ID";
		public static final String TYPE_MESSAGE = "message";
		public static final String TYPE_LIST = "list";

		public String type = "message";
		public String requestId;
		public boolean isShow;

		String title = "";
		String msg;
		String firstBtn;
		String secondBtn;

		int speechId = TtsManager.INVALID_TTS_TASK_ID;

		public DialogRecord setSureText(String s, String[] cmds) {
			firstBtn = s;
			return this;
		}

		public DialogRecord setCancelText(String s, String[] cmds) {
			secondBtn = s;
			return this;
		}

		public DialogRecord setHintTts(String text) {
			msg = text;
			return this;
		}

		public DialogRecord setMessage(String s) {
			msg = s;
			return this;
		}

		public DialogRecord setTitle(String title) {
			this.title = title;
			return this;
		}

		public void showVirtual() {
			// 关闭语音界面
			RecorderWin.close();
			if (isShowing()) {
				return;
			}

			requestId = BDHelper.showDialog(type, "SHOW_DIALOG_ID", title, msg, firstBtn, secondBtn);

			showSucc();
		}

		private void showSucc() {
			isShow = true;
			WakeupManager.getInstance().useWakeupAsAsr(new AsrComplexSelectCallback() {

				@Override
				public boolean needAsrState() {
					return false;
				}

				@Override
				public String getTaskId() {
					return TASK_ID;
				}

				public void onCommandSelected(String type, String command) {
					if ("FIRST$BTN".equals(type)) {
						onClickLeft();
					} else if ("SECOND$BTN".equals(type)) {
						onClickRight();
					}

					dismiss();
				}

			}.addCommand("FIRST$BTN", firstBtn).addCommand("SECOND$BTN", secondBtn));
			speechId = TtsManager.getInstance().speakVoice(msg, TtsManager.BEEP_VOICE_URL);
		}

		public void dismiss() {
			BDHelper.dismissDialog(requestId);
			isShow = false;
			TtsManager.getInstance().cancelSpeak(speechId);
			speechId = TtsManager.INVALID_TTS_TASK_ID;
			WakeupManager.getInstance().recoverWakeupFromAsr(TASK_ID);
		}

		@Override
		public boolean isShowing() {
			return isShow;
		}

		public boolean onDialog(String requestId, String strData) {
			if (!this.requestId.equals(requestId)) {
				JNIHelper.loge("requestId is not equals！");
				return false;
			}

			JSONBuilder jb = new JSONBuilder(strData);
			int pos = jb.getVal("order", Integer.class);
			BDHelper.logBdInfo("dialog pos:" + pos);
			if (pos == CoDriver_Dialog_FIRST_BTN) {
				onClickOk();
			} else if (pos == CoDriver_Dialog_SECOND_BTN) {
				onClickRight();
			}

			dismiss();
			return true;
		}

		public boolean onDialogCancel(String requestId, String strData) {
			if (!requestId.equals(this.requestId)) {
				JNIHelper.loge("requestId is not equals！");
				return false;
			}
			JSONBuilder jb = new JSONBuilder(strData);
			String dialogid = jb.getVal("dialogid", String.class);
			if (DIALOG_ID.equals(dialogid)) {
				dismiss();
			}

			return true;
		}
	}

	public static class TTSRecord {
		public int sSpeechId = TtsManager.INVALID_TTS_TASK_ID;

		public void cancelTts() {
			TtsManager.getInstance().cancelSpeak(sSpeechId);
			sSpeechId = TtsManager.INVALID_TTS_TASK_ID;
		}
	}

	public static class AsyncExecutor {

		private static AsyncExecutor sControl = new AsyncExecutor();

		public static AsyncExecutor getInstance() {
			return sControl;
		}

		public static final long DEFAULT_TIME_OUT = 2000;

		public static abstract class ExecuteCallBack {
			public static final int ERROR_TIME_OUT = 1;
			public static final int ERROR_NULL = 2;
			public static final int ERROR_EXEC = 3;
			public boolean mHasTimeOut = true;

			public void onError(int error, String des) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				if (!TextUtils.isEmpty(des)) {
					RecorderWin.speakTextWithClose(des, null);
				} else {
					if (error == ERROR_TIME_OUT) {
						des = NativeData.getResString("RS_VOICE_BD_TIME_OUT");
						RecorderWin.speakTextWithClose(des, null);
						return;
					}

					RecorderWin.close();
				}
			}

			public abstract void onReceive(boolean bSucc, String params);

			public void onReceive(int errCode, String params) {

			}
		}

		public static class ExecuteReq {
			public long mTimeOut;
			public String mReqId;
			public String mReqFunc;
			public boolean mHasExec;
			public ExecuteTask mExecTask;
			public ExecuteCallBack mCallback;
		}

		public static abstract class ExecuteTask {
			public abstract boolean doExecute(ExecuteReq eo);

			public long getTimeOut() {
				return DEFAULT_TIME_OUT;
			}
		}

		public void doAsyncExec(String reqId, String func, ExecuteTask task, ExecuteCallBack callBack) {
			ExecuteReq req = new ExecuteReq();
			req.mCallback = callBack;
			req.mExecTask = task;
			req.mReqId = reqId;
			req.mReqFunc = func;
			req.mTimeOut = SystemClock.elapsedRealtime();
			synchronized (mExecuteReqs) {
				mExecuteReqs.add(req);
			}

			notifyTimeOut(task.getTimeOut());
			procQueue();
		}

		List<ExecuteReq> mExecuteReqs = new ArrayList<ExecuteReq>();

		private void procQueue() {
			synchronized (mExecuteReqs) {
				JNIHelper.logd("procQueue size:" + mExecuteReqs.size());
				for (int i = 0; i < mExecuteReqs.size(); i++) {
					ExecuteReq er = mExecuteReqs.get(i);
					if (er.mHasExec) {
						continue;
					}

					ExecuteTask it = er.mExecTask;
					ExecuteCallBack cb = er.mCallback;
					if (it != null) {
						boolean bSucc = er.mExecTask.doExecute(er);
						JNIHelper.logd("requestId:" + er.mReqId + " exec " + bSucc);
						if (!BDHelper.isNewSDKVersion()) {
							onNaviReceive(0, MAP_CONTROL_SUCCESS, er.mReqId, er.mReqFunc, "");
							continue;
						}

						if (!bSucc) {
							cb.onError(ExecuteCallBack.ERROR_EXEC, "");
							mExecuteReqs.remove(i);
						}
					} else {
						if (cb != null) {
							cb.onError(ExecuteCallBack.ERROR_NULL, "");
						}
						mExecuteReqs.remove(i);
					}

					er.mHasExec = true;
				}
			}
		}

		public boolean onNaviReceive(int type, int errorNo, String requestId, String func, String params) {
			synchronized (mExecuteReqs) {
				JNIHelper.logd("onNaviReceive size:" + mExecuteReqs.size());
				for (int i = 0; i < mExecuteReqs.size(); i++) {
					ExecuteReq er = mExecuteReqs.get(i);
					ExecuteCallBack cb = er.mCallback;
					String reqId = er.mReqId;
					String fun = er.mReqFunc;
					if (cb == null) {
						JNIHelper.loge("onReceive Callback is null！");
						mExecuteReqs.remove(i);
						continue;
					}

					if (reqId.equals(requestId) && fun.equals(func)) {
						JNIHelper.loge("reqId:" + reqId + " onReceive errorNo:" + errorNo);
						if (errorNo == MAP_CONTROL_SUCCESS) {
							cb.onReceive(true, params);
						} else {
							cb.onReceive(false, params);
							cb.onReceive(errorNo, params);
						}

						mExecuteReqs.remove(i);
						return true;
					}
				}
			}
			return false;
		}

		private void notifyTimeOut(long delay) {
			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					long now = SystemClock.elapsedRealtime();
					synchronized (mExecuteReqs) {
						for (int i = 0; i < mExecuteReqs.size(); i++) {
							ExecuteReq er = mExecuteReqs.get(i);
							long pass = now - er.mTimeOut;
							long timeOut = er.mExecTask.getTimeOut();
							if (pass >= timeOut && er.mCallback.mHasTimeOut) {
								ExecuteCallBack cb = er.mCallback;
								JNIHelper.loge("requestId " + er.mReqId + " is Time Out！");
								if (cb != null) {
									cb.onError(ExecuteCallBack.ERROR_TIME_OUT, "");
								}

								mExecuteReqs.remove(i--);
							}
						}
					}
				}
			}, delay);
		}
	}

	public static abstract class RunnableCallBack implements Runnable {
		String taskId;
		String func;

		public String getFunc() {
			return func;
		}

		public String getTaskId() {
			return taskId;
		}

		public boolean onResult(boolean bSucc, String params) {
			return false;
		}
	}

	public static class NavNotifyQueues {
		private List<NotifyDialog> mNotifyDialogs = new ArrayList<NotifyDialog>();
		private static NavNotifyQueues sNavWakeupStore = new NavNotifyQueues();

		private NavNotifyQueues() {

		}

		public void onResume() {
			synchronized (mNotifyDialogs) {
				for (NotifyDialog nd : mNotifyDialogs) {
					if (TextUtils.isEmpty(nd.getTaskId())) {
						mNotifyDialogs.remove(nd);
						continue;
					}

					nd.onResume();
				}
			}
		}

		public void onPause() {
			synchronized (mNotifyDialogs) {
				for (NotifyDialog nd : mNotifyDialogs) {
					if (TextUtils.isEmpty(nd.getTaskId())) {
						mNotifyDialogs.remove(nd);
						continue;
					}
					nd.onPause();
				}
			}
		}

		public static NavNotifyQueues getInstance() {
			return sNavWakeupStore;
		}

		public void removeDialog(String taskId) {
			synchronized (mNotifyDialogs) {
				for (NotifyDialog nd : mNotifyDialogs) {
					if (nd.getTaskId().equals(taskId) || TextUtils.isEmpty(nd.getTaskId())) {
						JNIHelper.logd("removeDialog taskId:" + taskId);
						nd.destory();
						mNotifyDialogs.remove(nd);
						break;
					}
				}
			}
		}

		public NotifyDialog createDialog(final String taskId) {
			removeDialog(taskId);
			if (TextUtils.isEmpty(taskId)) {
				return null;
			}

			NotifyDialog nd = new NotifyDialog();
			nd.setTaskId(taskId);
			nd.setDestoryListener(new Runnable() {

				@Override
				public void run() {
					removeDialog(taskId);
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.close();
				}
			});

			JNIHelper.logd("createDialog taskId:" + taskId);

			synchronized (mNotifyDialogs) {
				mNotifyDialogs.add(nd);
			}
			return nd;
		}

		public void removeAllDialog() {
			synchronized (mNotifyDialogs) {
				for (NotifyDialog nd : mNotifyDialogs) {
					nd.destory();
				}
				mNotifyDialogs.clear();
			}
		}
	}
}