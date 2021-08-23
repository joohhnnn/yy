package com.txznet.txz.component.wakeup.mix;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import android.text.TextUtils;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.component.wakeup.mix.CmdCompileTask.TaskType;
import com.txznet.txz.module.wakeup.WakeupCmdTask;
import com.txznet.txz.util.FileUtil;
import com.unisound.client.SpeechConstants;
import com.unisound.client.SpeechUnderstander;
 
public class PreBuildWKCmdUtils{
	private static final String DEFAULT_WAKEUP_PATH = GlobalContext.get().getApplicationInfo().dataDir + "/yzs_asr/v3/YunZhiSheng/asrfix/wakeup.dat";
	public final static String WK_STATIC_DIR = "wk_static";
	public final static String WK_DYNAMIC_DIR = "wk_dynamic";
	public final static String WK_EXCLUSIVE_DIR = "wk_exclusive";
	
	private final static int WK_STATIC_CACHE_MAX_CNT = 200;//200*5K
	private final static int WK_EXCLUSIVE_CACHE_MAX_CNT = 100;//100*1K
	private final static int WK_DYNAMIC_CACHE_MAX_CNT = 50;//50*10K
	
	public static void checkCacheLimit(){
		final String rootDir = GlobalContext.get().getApplicationInfo().dataDir;
		final String[] dirs = new String[]{WK_STATIC_DIR, WK_EXCLUSIVE_DIR, WK_DYNAMIC_DIR};
		for (String dir : dirs) {
			String fullPath = String.format("%s/%s", rootDir,dir);
			File f = new File(fullPath);
			if (!f.isDirectory()){
				continue;
			}
			
			int limitCnt = WK_STATIC_CACHE_MAX_CNT;
			if (TextUtils.equals(dir, WK_DYNAMIC_DIR)){
				limitCnt = WK_DYNAMIC_CACHE_MAX_CNT;
			}else if (TextUtils.equals(dir, WK_EXCLUSIVE_DIR)){
				limitCnt = WK_EXCLUSIVE_CACHE_MAX_CNT;
			}else if (TextUtils.equals(dir, WK_STATIC_DIR)){
				limitCnt = WK_STATIC_CACHE_MAX_CNT;
			}
			LogUtil.logd("checkCacheLimit , " + dir + ":" + limitCnt);
			delectRedundantFile(f.listFiles(), limitCnt);
		}
	}
	
	private static void delectRedundantFile(File[] files, final int limitCnt){
		if (files == null){
			return;
		}
		
		int fileCnt = files.length;
		if (fileCnt < limitCnt){
			return;
		}
		
		for(File f : files){
			if (f == null){
				continue;
			}
			
			boolean ret = f.delete();
			if (!ret){
				LogUtil.logw("del fail:" +  f.getPath());
			}
		}
	}
	
	private static String genKwsGrammarPath(CmdCompileTask task) {
		if (task == null){
			return "";
		}
		
		final int mask = task.getTaskKwsType();
		String grammardir = WK_STATIC_DIR;
		do {
			if ((mask & WakeupCmdTask.TYPE_EXCLUSIVE_MASK) != 0) {
				grammardir = WK_EXCLUSIVE_DIR;
				break;
			}
			
			if ((mask & WakeupCmdTask.TYPE_DYNAMIC_MASK) != 0) {
				grammardir = WK_DYNAMIC_DIR;
				break;
			}
			grammardir = WK_STATIC_DIR;
		} while (false);
		
		final String rootDir = GlobalContext.get().getApplicationInfo().dataDir;
		String fullPath = String.format("%s/%s/%s.dat", rootDir, grammardir, task.getTaskId());
		LogUtil.logd("wk_fullPath:" + fullPath);
		return fullPath;
	}
	
	private static String findKwsGrammarInAllDir(String sTaskId) {
		if (TextUtils.isEmpty(sTaskId)){
			return "";
		}
		String grammarPath = "";
		final String rootDir = GlobalContext.get().getApplicationInfo().dataDir;
		final String[] dirs = new String[]{WK_STATIC_DIR, WK_EXCLUSIVE_DIR, WK_DYNAMIC_DIR};
		for (String dir : dirs) {
			String fullPath = String.format("%s/%s/%s.dat", rootDir,dir, sTaskId);
			File f = new File(fullPath);
			if (f.exists()) {
				grammarPath = fullPath;
				break;
			}
		}
		
		LogUtil.logd("wk_grammarPath:" + grammarPath);
		return grammarPath;
	}
	
	public static String getWakeupGrammarPath(CmdCompileTask task) {
		if (task == null) {
			return null;
		}
		
		//非唤醒词编译任务
		if (task.getTaskType() != TaskType.TYPE_WAKEUP){
			return null;
		}
		
		String grammarPath = null;
		do{
			{
				String wakeupDatPath = genKwsGrammarPath(task);
				if (!TextUtils.isEmpty(wakeupDatPath)){
					File file = new File(wakeupDatPath);
					if (file.exists()) {
						grammarPath = wakeupDatPath;
						break;
					}
				}
			}
			
			// 查找失败,再全路径搜索一遍
			{
				String wakeupDatPath = findKwsGrammarInAllDir(task.getTaskId());
				if (!TextUtils.isEmpty(wakeupDatPath)) {
					File file = new File(wakeupDatPath);
					if (file.exists()) {
						grammarPath = wakeupDatPath;
						break;
					}
				}
			}
			
		}while(false);
		
		return grammarPath;
	}
	
	public static boolean saveWakeupKeywords(CmdCompileTask task) {
		if (task.getTaskType() != TaskType.TYPE_WAKEUP){
			return false;
		}
		
		//保存唤醒词语法
		String grmPath = genKwsGrammarPath(task);
		boolean bRet = false;
		do {
			if (TextUtils.isEmpty(grmPath)){
				break;
			}
			
			bRet = FileUtil.copyFile(DEFAULT_WAKEUP_PATH, grmPath);
			if (bRet) {
				LogUtil.logd("saveWakeupKeywords success : " + task.getTaskId());
			} else {
				//保存过程中出错，需要清除临时生成的文件
				File f = new File(grmPath);
				if (f.exists()){
					f.delete();
					LogUtil.loge("saveWakeupKeywords fail delect tmp file,  " + task.getTaskId());
				}
			}
		} while (false);
		
		return true;
	}
}
