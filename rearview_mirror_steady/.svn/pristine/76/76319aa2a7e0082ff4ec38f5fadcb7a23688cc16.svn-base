package com.txznet.txz.component.wakeup.mix;

import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.module.wakeup.WakeupCmdTask;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;

import android.text.TextUtils;

public class AsrCmdCompileTask extends CmdCompileTask{
	private final static int  COMPILE_MAX_PRONUNCIATION = 6*4*4*2*2;//离线词条最大发音长度,超过该长度，编译过程中会报错。SDK默认长度为20，这个限制太短了。
	private String mTaskId = null;
	private List<String> mKws = null;
	private int mTaskKwsType = WakeupCmdTask.TYPE_NONE_MASK;
	
	public AsrCmdCompileTask(String sTaskId, List<String> kws, int taskType) {
		super(TaskType.TYPE_ASR);
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
			
			if (!(o instanceof AsrCmdCompileTask)){
				bRet = false;
				break;
			}
			
			AsrCmdCompileTask task = (AsrCmdCompileTask) o;
			
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
	
	@Override
	public boolean compile(ICmdCompiler compiler){
		boolean bRet = false;
		if (compiler != null){		    
			bRet = compiler.compileAsrKws(mKws, mTaskId);
		}
		return bRet;
	}
	
}
