/**
 * 
 */
package com.txznet.music.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.List;

import com.txznet.comm.remote.util.LogUtil;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

/**
 * @desc <pre></pre>
 * @author Erich Lee
 * @Date Mar 9, 2013
 */
public class FileUtils {
	private static final String TAG = "[MUSIC][FILE]FileUtils";

	public static String mBaseStorePath = Environment
			.getExternalStorageDirectory()
			+ File.separator
			+ "TXZ"
			+ File.separator;// :sd卡根目录/TXZ/

	public static boolean isExistSDCard = Environment.getExternalStorageState()
			.equals(android.os.Environment.MEDIA_MOUNTED);

	/*
	 * 检查SD卡是否存在
	 */
	public static boolean chekSDCardExist() {
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	public static void closeQuietly(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
	}

	public static boolean isExist(String path) {
		if (StringUtils.isEmpty(path)) {
			return false;
		}
		File file = new File(path);
		if (file.exists()) {
			return true;
		}
		return false;
	}

	public static void removeDir(String dirPath) {
		if (StringUtils.isEmpty(dirPath)) {
			return;
		}
		removeDir(new File(dirPath));
	}

	public static void removeDir(File dir) {
		if (dir == null || !dir.exists()) {
			return;
		}
		if (dir.isFile()) {
			dir.delete();
			return;
		}
		File[] listFiles = dir.listFiles();
		if (listFiles == null || listFiles.length == 0) {
			dir.delete();
			return;
		}
		// 删除目录中的文件
		for (File file : listFiles) {
			removeDir(file);
		}
		// 删除目录
		String[] list = dir.list();
		if (Array.getLength(list) <= 0) {
			dir.delete();
		}
	}

	public static void recycleBitmaps(List<Bitmap> list) {
		if (CollectionUtils.isEmpty(list)) {
			return;
		}
		for (Bitmap bitmap : list) {
			recycleBitmap(bitmap);
		}
		list.clear();
	}

	public static void recycleBitmap(Bitmap bitmap) {
		if (bitmap != null && !bitmap.isRecycled()) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	/**
	 * 获得指定文件的byte数组
	 */
	public static byte[] getBytes(String filePath) {
		byte[] buffer = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;
		try {
			File file = new File(filePath);
			fis = new FileInputStream(file);
			bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024 * 1024];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);
			}
			buffer = bos.toByteArray();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} catch (OutOfMemoryError e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			closeQuietly(fis);
			closeQuietly(bos);
		}
		return buffer;
	}

	/**
	 * 获得指定文件的byte数组
	 */
	public static byte[] getByteList(String filePath) {
		byte[] buffer = null;
		FileInputStream fis = null;
		ByteArrayOutputStream bos = null;

		try {
			File file = new File(filePath);
			fis = new FileInputStream(file);
			bos = new ByteArrayOutputStream();
			byte[] b = new byte[1024];
			int n;
			while ((n = fis.read(b)) != -1) {
				bos.write(b, 0, n);

			}
			buffer = bos.toByteArray();
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} catch (OutOfMemoryError e) {
			Log.e(TAG, e.getMessage(), e);
		} finally {
			closeQuietly(fis);
			closeQuietly(bos);
		}
		return buffer;
	}

	public static byte[] file2BetyArray(String filePath) {
		FileInputStream fileInputStream = null;
		File file = new File(filePath);
		byte[] bFile = null;
		try {
			bFile = new byte[(int) file.length()];
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bFile);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			FileUtils.closeQuietly(fileInputStream);
			// bFile.clone();

		}
		return bFile;
	}

	/**
	 * 根据byte数组，生成文件
	 */
	public static void getFile(byte[] bfile, String filePath, String fileName) {
		BufferedOutputStream bos = null;
		FileOutputStream fos = null;
		try {
			File dir = new File(filePath);
			if (!dir.exists() && dir.isDirectory()) {// 判断文件目录是否存在
				dir.mkdirs();
			}
			File file = new File(fileName);
			fos = new FileOutputStream(file);
			bos = new BufferedOutputStream(fos);
			bos.write(bfile);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		} finally {
			closeQuietly(bos);
			closeQuietly(fos);
		}
	}

	/**
	 * 删除文件夹
	 */
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // 删除完里面所有内容
			String filePath = folderPath;
			filePath = filePath.toString();
			delFile(filePath);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	public static void delFile(String filePath) {
		java.io.File myFilePath = new java.io.File(filePath);
		if (myFilePath.exists()) {
			myFilePath.delete(); // 删除空文件夹
			LogUtil.logd(TAG + "删除成功");
		} else {
			LogUtil.logd(TAG + "没有该文件");
		}
	}

	/**
	 * 删除文件夹下的文件
	 */
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// 先删除文件夹里面的文件
				delFolder(path + "/" + tempList[i]);// 再删除空文件夹
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * @desc <pre>
	 * 删除拍照以后保存的相片
	 * </pre>
	 * @author Weiliang Hu
	 * @date 2014年5月8日
	 */
	public static void deletePhotoFile() {
		try {
			// delAllFile(Utils.mStoreImagePath);
		} catch (Exception e) {
			Log.e(TAG, e.toString());
		}
	}

	public static boolean isFoundApp(Context mContext, String packageName) {
		PackageInfo packageInfo;
		try {
			packageInfo = mContext.getPackageManager().getPackageInfo(
					packageName, 0);
		} catch (NameNotFoundException e) {
			packageInfo = null;
			Log.e(TAG, e.toString());
		}
		if (packageInfo == null) {
			System.out.println("没有安装");
			return false;
		} else {
			System.out.println("已经安装");
			return true;
		}
	}

	public static File getFilePath(String filePath, String fileName) {
		File file = null;
		makeRootDirectory(filePath);
		try {
			file = new File(filePath + fileName);
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

	public static void makeRootDirectory(String filePath) {
		File file = null;
		try {
			file = new File(filePath);
			if (!file.exists()) {
				file.mkdir();
			}
		} catch (Exception e) {

		}
	}
}
