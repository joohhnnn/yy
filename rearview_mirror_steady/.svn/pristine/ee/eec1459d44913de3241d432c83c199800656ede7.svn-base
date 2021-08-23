package com.txznet.txz.util;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.os.StatFs;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.jni.JNIHelper;

public class SystemInfo {
	// CPU个数
	public static int getNumCores() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			JNIHelper.logd("CPU Count: " + files.length);
			// Return the number of cores (virtual CPU devices)
			return files.length;
		} catch (Exception e) {
			// Print exception
			JNIHelper.loge("CPU Count: Failed");
			e.printStackTrace();
			// Default to return 1 core
			return 1;
		}
	}

	// 获取内存占用信息
	public static String getMemInfo() {
		ActivityManager mActivityManager = (ActivityManager) GlobalContext.get()
				.getSystemService(Context.ACTIVITY_SERVICE);
		android.os.Debug.MemoryInfo[] meminfos = mActivityManager
				.getProcessMemoryInfo(new int[] { Process.myPid() });
		return String.format(
				"dalvik[%d][%d][%d], native[%d][%d][%d], other[%d][%d][%d]",
				meminfos[0].dalvikPrivateDirty, meminfos[0].dalvikPss,
				meminfos[0].dalvikSharedDirty, meminfos[0].nativePrivateDirty,
				meminfos[0].nativePss, meminfos[0].nativeSharedDirty,
				meminfos[0].otherPrivateDirty, meminfos[0].otherPss,
				meminfos[0].otherSharedDirty);
	}

	/**
	 * 获得SD卡总大小
	 */
	public static long getSDTotalSize(String sdcardPath) {
		long nSize = 0;
		StatFs stat = null;
		try {
			stat = new StatFs(sdcardPath);
		} catch (Exception e) {
		}
		if (stat != null) {
			long blockSize = stat.getBlockSize();
			long totalBlocks = stat.getBlockCount();
			nSize = blockSize * totalBlocks;
		}

		return nSize;
	}

	/**
	 * 获得sd卡剩余容量，即可用大小
	 */
	public static long getSDAvailableSize(String sdcardPath) {
		long nSize = 0;
		StatFs stat = null;
		try {
			stat = new StatFs(sdcardPath);
		} catch (Exception e) {
		}
		if (stat != null) {
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			nSize = blockSize * availableBlocks;
		}
		return nSize;
	}
}
