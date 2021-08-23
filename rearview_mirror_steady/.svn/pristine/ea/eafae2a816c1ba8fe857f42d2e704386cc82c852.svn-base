package com.txznet.txz.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.tts.TtsManager;

public class SDCardUtil {

	private static String mSDCardPath;
	public static final String DEFAULT_SDCARD_PATH = Environment
			.getExternalStorageDirectory().getPath();

	/**
	 * 获取SDCard路径
	 * 
	 * @return
	 */
	public static final String getSDCardPath() {
		if (mSDCardPath == null) {
			mSDCardPath = PreferenceUtil.getInstance().getSDCardPath();
		}
		Log.d("txz", "mSDCardPath=" + mSDCardPath);
		return mSDCardPath;
	}

	/**
	 * 如果设了SDCard路径，以后获取SDCard路径为此路径
	 * 
	 * @param path
	 */
	public static final void setSDCardPath(String path) {
		mSDCardPath = path;
		// 写入SP
		PreferenceUtil.getInstance().setSDCardPath(path);
	}

	/**
	 * 清除sdacrd路径
	 */
	public static final void clearSDCardPath() {
		mSDCardPath = null;
	}

	/**
	 * 测试当前SDCARD目录是否可读
	 * 
	 * @return
	 */
	public static final boolean checkFileReadOnly() {
		if (mSDCardPath != null)
			return checkFileReadOnly(mSDCardPath);
		return checkFileReadOnly(DEFAULT_SDCARD_PATH);
	}

	/**
	 * 测试该目录是否只读
	 * 
	 * @param dir
	 * @return
	 */
	public static final boolean checkFileReadOnly(String dir) {
		File file = new File(dir, ".txz~~");
		boolean r = false;
		do {
			if (file.exists() == false) {
				try {
					if (file.createNewFile() == false) {
						r = true;
						break;
					}
				} catch (IOException e) {
					r = true;
					break;
				}
			}
			r = (file.canWrite() == false);
			file.delete();
		} while (false);

		JNIHelper.logd(dir + " is read only: " + r);

		return r;
	}

	/**
	 * 检测外部sdcard是否可用
	 * 
	 * @param context
	 * @return
	 */
	public static final ArrayList<String> getAvailableExternalSDCard(
			Context context) {
		String[] sdcardPaths = getSDCardsPath(context);
		ArrayList<String> pathAvailable = null;
		for (int i = 0; i < sdcardPaths.length; i++) {
			boolean b = checkFileReadOnly(sdcardPaths[i]);
			if (!b) {
				if (pathAvailable == null)
					pathAvailable = new ArrayList<String>();
				pathAvailable.add(sdcardPaths[i]);
			}
		}
		return pathAvailable;
	}

	/**
	 * 获取系统有多少sdcard路径
	 * 
	 * @param context
	 * @return
	 */
	public static final String[] getSDCardsPath(Context context) {
		StorageManager sm = (StorageManager) context
				.getSystemService(Context.STORAGE_SERVICE);
		// 获取sdcard的路径：外置和内置
		String[] paths = null;
		try {
			paths = (String[]) sm.getClass().getMethod("getVolumePaths")
					.invoke(sm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return paths;
	}

	/**
	 * 
	 * @return 0为成功 -1为不可用 1为需要重启切换配置
	 */
	public static int checkSDCardError() {
		if (checkFileReadOnly()) {
			// 检测外部SD卡能否使用
			final ArrayList<String> pathAvailable = getAvailableExternalSDCard(GlobalContext.get());
			if (pathAvailable != null && pathAvailable.size() != 0) {
				// 弹出提示框
				new WinNotice() {

					@Override
					public void onClickOk() {
						// TODO Auto-generated method
						// stub
						// 设置获取SDCard目录为外部SDCard目录
						setSDCardPath(pathAvailable.get(0));
						// 重启应用
						AppLogic.restartProcess();
					}
				}.setTitle("磁盘写入出现问题，请重启应用!").show();
				return 1;
			} else {
				// 语音提示
				String spk = NativeData.getResString("RS_UTIL_SDCARD_ERROR");
				TtsManager.getInstance().speakText(spk);
				return -1;
			}
		}
		return 0;
	}
}
