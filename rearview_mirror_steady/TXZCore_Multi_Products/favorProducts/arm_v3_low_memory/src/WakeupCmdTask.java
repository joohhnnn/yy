package com.txznet.txz.module.wakeup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.record.RecordManager;

public class WakeupCmdTask {
	public final static String WAKEUP_TASK_ID_SDK = "wakeup_task_id_sdk";
	public final static String WAKEUP_TASK_ID_USER = "wakeup_task_id_user";
	public final static String WAKEUP_TASK_ID_ONESHOT = "wakeup_task_id_oneshot";
	
	public final static String[] STATIC_DEFAULT_SELECT_CMDS = {"第一个", "第二个", "第三个", "第四个", "第五个", "上一页", "下一页", "确定", "取消"};
	public final static String[] STATIC_POI_SELECT_CMDS = {
															"第一个", "第二个", "第三个", "第四个", "第五个",
		                                                    "上一页", "下一页", "确定", "取消",
		                                                    "地图模式", "列表模式","放大地图","缩小地图",
		                                                    "距离排序", "价格排序", "评分排序",
		                                                    "开始导航",
		                                                    "有团购", "有停车场", "有电话",
		                                                    "最近那个"
														  };
	
	public final static String[] STATIC_CALL_SELECT_CMDS = STATIC_DEFAULT_SELECT_CMDS;
	public final static String[] STATIC_WECHAT_SELECT_CMDS = STATIC_DEFAULT_SELECT_CMDS;
	
	
	public final static String[] INCOMING_TASK_KWS = {"接听", "挂断"};
	public final static String[] RECORDING_TASK_KWS = {"欧我欧我", "欧稳欧稳", "完毕完毕", "取消取消"};
	
	public final static int TYPE_NONE_MASK = 0x00;
	public final static int TYPE_EXCLUSIVE_MASK = 0x01;//排他性唤醒词类型
	public final static int TYPE_DYNAMIC_MASK = TYPE_EXCLUSIVE_MASK << 1;//动态生成的唤醒词类型
	public final static int TYPE_STATIC_MASK = TYPE_EXCLUSIVE_MASK << 2;//静态唤醒词类型
	public final static int TYPE_PRE_BUILD_MASK = TYPE_EXCLUSIVE_MASK << 7;//静态唤醒词类型
	
	
	public static boolean contains(String strCmd, String[] kws){
		boolean bRet = false;
		if (TextUtils.isEmpty(strCmd)){
			return false;
		}
		
		if (kws == null){
			return false;
		}
		
		for (String kw : kws ){
			if (TextUtils.equals(strCmd, kw)){
				bRet = true;
				break;
			}
		}
		
		return bRet;
	}
	
	public static boolean isPreBuildType(int type){
		return (type & WakeupCmdTask.TYPE_PRE_BUILD_MASK) != 0;
	}
	
	public static boolean isDynamicType(int type){
		return (type & WakeupCmdTask.TYPE_DYNAMIC_MASK) != 0;
	}
	
	public static boolean isExclusiveType(int type){
		return (type & WakeupCmdTask.TYPE_EXCLUSIVE_MASK) != 0;
	}
	
	public static boolean isStaticType(int type){
		return (type & WakeupCmdTask.TYPE_STATIC_MASK) != 0;
	}
	
	
	public static enum TaskType{
		TYPE_NONE,
		TYPE_GLOBAL_STATIC,//全局静态唤醒词, 不是代码生成的，一直设置到引擎中
		TYPE_SENCE_STATIC,//场景静态唤醒词, 不是代码生成的，特殊场景下才设置到引擎中
		TYPE_DYNAMIC,//动态唤醒词,代码生成的唤醒词,特殊场景下才设置到引擎中
		TYPE_EXCLUSIVE//排他性唤醒词,会将引擎中的其他唤醒词清除,只保留该类型的唤醒词
	}

	private String mTaskId;
	private TaskType mTaskType = TaskType.TYPE_NONE;
	private Set<String> mCmdWords = new HashSet<String>();
	private boolean mUsed = false;
	
	public WakeupCmdTask(String sTaskId, TaskType taskType){
		mTaskId = sTaskId;
		mTaskType = taskType;
	}
	
	public boolean updateCmd(String... cmds){
		boolean bUpdated = false;
		do{
			if (cmds == null) {
				LogUtil.logw("cmds is null");
				break;
			}
			
			Set<String> cmdSet = new HashSet<String>();
			for(String cmd : cmds){
				if (TextUtils.isEmpty(cmd)){
					continue;
				}
				cmdSet.add(cmd);
			}
			//默认不允许清空原来的唤醒词
			if (cmdSet.isEmpty()){
				break;
			}
			
			mCmdWords.clear();
			mCmdWords.addAll(cmdSet);
			
		}while(false);
		
		return bUpdated;
	}
	
	public void useCmd(boolean bUsed){
		mUsed = bUsed;
	}
	
	
	public String getTaskId(){
		return mTaskId;
	}
	
	public TaskType getTaskType(){
		return mTaskType;
	}
	
	public Set<String> getCmds(){
		return mCmdWords;
	}
	
	public boolean isUsed(){
		return mUsed;
	}

	
	public static boolean updateWakeupCmdTaskMap(Map<String, WakeupCmdTask> cmdMap, String strTaskId, boolean bUsed){
		boolean bUpdated = false;
		if (cmdMap == null){
			LogUtil.logw("cmdMap = null");
			return false;
		}
		
		if (TextUtils.isEmpty(strTaskId)){
			LogUtil.logw("strTaskId id empty");
			return false;
		}
		
		WakeupCmdTask task = cmdMap.get(strTaskId);
		if (task != null){
			task.useCmd(bUsed);
			bUpdated = true;
		}
		
		return bUpdated;
	}
	
	public static boolean updateWakeupCmdTaskMap(Map<String, WakeupCmdTask> cmdMap, String strTaskId, String... cmds){
		boolean bUpdated = false;
		if (cmdMap == null){
			LogUtil.logw("cmdMap = null");
			return false;
		}
		
		if (TextUtils.isEmpty(strTaskId)){
			LogUtil.logw("strTaskId id empty");
			return false;
		}
		
		WakeupCmdTask task = cmdMap.get(strTaskId);
		if (task == null){
			task = new WakeupCmdTask(strTaskId, null);
			cmdMap.put(strTaskId, task);
		}
		
		//禁止编译代码动态生成的唤醒词
		if (TextUtils.equals("com.txznet.txz@Poi_Select", strTaskId)){
			cmds = STATIC_POI_SELECT_CMDS;
			LogUtil.logd("use STATIC_POI_SELECT_CMDS : " + strTaskId);
		}else if (TextUtils.equals("com.txznet.txz@Call_Contact_Select", strTaskId)){
			cmds = STATIC_CALL_SELECT_CMDS;
			LogUtil.logd("use STATIC_CALL_SELECT_CMDS : " + strTaskId);
		}else if (TextUtils.equals("com.txznet.txz@WX_Select", strTaskId)){
			cmds = STATIC_WECHAT_SELECT_CMDS;
			LogUtil.logd("use STATIC_WECHAT_SELECT_CMDS : " + strTaskId);
		}else if (strTaskId.startsWith(ServiceManager.TXZ) && strTaskId.endsWith("_Select")){
			cmds = STATIC_DEFAULT_SELECT_CMDS;
			LogUtil.logd("use STATIC_DEFAULT_SELECT_CMDS : " + strTaskId);
		}
		
		bUpdated = task.updateCmd(cmds);
		
		return bUpdated;
	}
	
	public static String[] genWakeupKwsFromTasks(Map<String, WakeupCmdTask> cmdMap){
		Set<String> kwsSet = new HashSet<String>();
		Set<String> taskIdSet = cmdMap.keySet();
		int wakeupCmdType = TYPE_NONE_MASK;
		do {
			WakeupCmdTask taskIncoming = cmdMap.get(ServiceManager.TXZ + "@" + CallManager.WAKEUP_INCOMING_TASK_ID);
			if (taskIncoming != null && taskIncoming.isUsed()){
				kwsSet.addAll(taskIncoming.getCmds());
				wakeupCmdType |= TYPE_EXCLUSIVE_MASK;
				break;
			}
			
			WakeupCmdTask taskRecording = cmdMap.get(ServiceManager.TXZ + "@" + RecordManager.RECORD_TASK_ID);
			if (taskRecording != null && taskRecording.isUsed()){
				kwsSet.addAll(taskRecording.getCmds());
				wakeupCmdType |= TYPE_EXCLUSIVE_MASK;
				break;
			}
			
			for (String taskid : taskIdSet) {
				WakeupCmdTask task = cmdMap.get(taskid);
				if (task == null) {
					continue;
				}
				if (!task.isUsed()) {
					continue;
				}
				
				wakeupCmdType |= TYPE_STATIC_MASK;//默认是静态类型
				kwsSet.addAll(task.getCmds());
			}
		} while (false);
		
		//传递唤醒词的类型等信息
		JSONObject json = new JSONObject();
		try {
			json.put("type", wakeupCmdType);
		} catch (Exception e) {
			LogUtil.logw("Exception:" + e.toString());
		}
		List<String> kwsList = new ArrayList<String>();
		kwsList.addAll(kwsSet);
		kwsList.add(0, json.toString());
		
		String[] kws = new String[kwsList.size()];
		return kwsList.toArray(kws);
	}
	
	//合并需要预编译的唤醒词
	public static String[] mergeKws(String[] originKws, String... cmds){
		if (originKws == null){
			return null;
		}
		
		if (originKws.length == 0){
			return null;
		}
		
		if (cmds == null){
			return null;
		}
		
		if (cmds.length == 0){
			return null;
		}
		
		JSONObject json = null;
		try{
			json = new JSONObject(originKws[0]);
			int type = json.getInt("type");
			//不与代码动态生成的唤醒词合并编译
			if ((type & TYPE_DYNAMIC_MASK) != 0){
				LogUtil.logw("do not merge to dynamic kws set");
				return null;
			}
			
			json.put("type", type|TYPE_PRE_BUILD_MASK);
			
		}catch(Exception e){
			return null;
		}
		
		Set<String> kwsSet = new HashSet<String>();
		for(int i = 1; i < originKws.length; ++i){
			kwsSet.add(originKws[i]);
		}
		
		boolean bUpdated = false;
		for (String kw : cmds){
			if (kwsSet.add(kw)){
				bUpdated = true;
			}
		}
		
		//cmd和originKws内容一样，不要单独预编译一次
		if (!bUpdated){
			return null;
		}
		
		List<String> kwsList = new ArrayList<String>();
		kwsList.addAll(kwsSet);
		kwsList.add(0, json.toString());
		
		String[] kws = new String[kwsList.size()];
		return kwsList.toArray(kws);
	}
	
	
	//生成需要预编译的唤醒词
	public static String[] genKws(int type, String... cmds){
		if (cmds == null){
			return null;
		}
		
		if (cmds.length == 0){
			return null;
		}
		
		Set<String> kwsSet = new HashSet<String>();
		for (String cmd : cmds){
			if (TextUtils.isEmpty(cmd)){
				continue;
			}
			kwsSet.add(cmd);
		}
		
		if (kwsSet.isEmpty()){
			return null;
		}
		
		
		//传递唤醒词的类型等信息
		JSONObject json = new JSONObject();
		try {
			json.put("type", type|TYPE_PRE_BUILD_MASK);
		} catch (Exception e) {
			LogUtil.logw("Exception:" + e.toString());
		}
		
		List<String> kwsList = new ArrayList<String>();
		kwsList.addAll(kwsSet);
		kwsList.add(0, json.toString());
		
		String[] kws = new String[kwsList.size()];
		return kwsList.toArray(kws);
	}
}
