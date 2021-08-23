package com.txznet.txz.component.wakeup.mix;

import java.util.List;

public class CmdCompileTask {
	public static enum TaskType{
		TYPE_NONE,
		TYPE_WAKEUP,
		TYPE_ASR
	}
	
	private TaskType mTaskType = TaskType.TYPE_NONE;

	public TaskType getTaskType() {
		return mTaskType;
	}
	
	public String getTaskId(){
		return null;
	}
	
	public List<String> getKws(){
		return null;
	}
	
	public int getTaskKwsType(){
		return 0;
	}
	
}
