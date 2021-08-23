package com.txznet.feedback.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.util.LogUtil;

import android.os.Environment;

public class FileUtil {

	public static boolean isSDcard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return true;
		}

		return false;
	}

	public static File getSdCardDir() {
		if (isSDcard()) {
			return Environment.getExternalStorageDirectory();
		}

		return null;
	}

	/**
	 * 获取某个文件
	 * 
	 * @param fileName
	 * @return
	 */
	public static List<File> getFileByName(String fileName) {
		List<File> resultFiles = new ArrayList<File>();
		findFileByName(getSdCardDir(), fileName, resultFiles);
		if (resultFiles.size() < 1) {
			return null;
		}
		return resultFiles;
	}

	/**
	 * 根据指定名称获取File列表
	 * 
	 * @param fileName
	 * @param resultFiles
	 */
	public static void getFileByName(String fileName, List<File> resultFiles) {
		findFileByName(getSdCardDir(), fileName, resultFiles);
	}

	/**
	 * 通过文件名获取文件
	 * 
	 * @param root
	 *            当前目录
	 * @param fileName
	 * @return
	 */
	public static void findFileByName(File root, String fileName,
			List<File> resultFiles) {
		if (root == null) {
			return;
		}

		if (!root.exists()) {
			return;
		}

		if (resultFiles == null) {
			resultFiles = new ArrayList<File>();
		}

		if (root.isDirectory()) {
			File[] files = root.listFiles();
			for (File f : files) {
				findFileByName(f, fileName, resultFiles);
			}
		} else {
			String fn = root.getName();
			if (fn.contains(fileName)) {
				resultFiles.add(root.getAbsoluteFile());
			}
		}

		return;
	}

	/**
	 * 删除文件
	 * 
	 * @param fileName
	 */
	public static void deleteFile(String filePathName) {
		List<File> resultFiles = new ArrayList<File>();
		File file = new File(filePathName);
		findFileByName(file.getParentFile(), file.getName(), resultFiles);
		for (File f : resultFiles) {
			if (f.exists()) {
				f.delete();
			}
		}
	}

	public static boolean copyFile(String src, String dst) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(src);
			out = new FileOutputStream(dst);
			byte[] buf = new byte[2048];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			in = null;
			out.close();
			out = null;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public static void copyFile(InputStream is, String dst) {
		if (is == null) {
			return;
		}
		OutputStream out = null;
		try {
			out = new FileOutputStream(dst);
			byte[] buf = new byte[2048];
			int len;
			while ((len = is.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			is.close();
			is = null;
			out.close();
			out = null;
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					LogUtil.loge(e.toString());
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					LogUtil.loge(e.toString());
				}
			}
		}
	}
}