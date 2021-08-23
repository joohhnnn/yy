package com.txznet.txz.component.wakeup.mix;

import java.util.List;

import com.txznet.txz.module.wakeup.WakeupCmdTask;

import android.text.TextUtils;

public class WakeupCmdCompileTask extends CmdCompileTask{
	private String mTaskId = null;
	private List<String> mKws = null;
	private int mTaskKwsType = WakeupCmdTask.TYPE_NONE_MASK;
	
	public WakeupCmdCompileTask(String sTaskId, List<String> kws, int taskType) {
		this.mTaskId = sTaskId;
		this.mKws = kws;
		this.mTaskKwsType = taskType;
	}

	public String getTaskId(){
		return mTaskId;
	}
	
	public List<String> getKws(){
		return mKws;
	}
	
	public int getTaskKwsType(){
		return mTaskKwsType;
	}
	
	@Override
	public boolean equals(Object o) {
		boolean bRet = false;
		do{
			if (o == null){
				bRet = false;
				break;
			}
			
			if (o == this){
				bRet = true;
				break;
			}
			
			if (!(o instanceof WakeupCmdCompileTask)){
				bRet = false;
				break;
			}
			
			WakeupCmdCompileTask task = (WakeupCmdCompileTask) o;
			
			if (TextUtils.equals(task.mTaskId, mTaskId)){
				bRet = true;
			}
			
		}while(false);
		
		return bRet;
	}

	@Override
	public int hashCode() {
		if (mTaskId == null) {
			return 0;
		}
		return mTaskId.hashCode();
	}
}
