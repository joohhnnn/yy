package com.txznet.txz.component.wakeup.mix;

import java.util.List;

public class CmdCompileTask {
	public static enum TaskType{
		TYPE_WAKEUP,
		TYPE_ASR
	}
	
	private TaskType mTaskType = null;
	
	protected CmdCompileTask(TaskType type){
		mTaskType = type;
	}
	
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
	
	public boolean compile(ICmdCompiler compiler){
		return false;
	}
	
	public boolean checkPreBuild(){
		return false;
	}
	
	public boolean savePreBuild(){
		return false;
	}
}
