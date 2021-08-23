package com.txznet.txz.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;

public class FileUtil {
	public static boolean copyFile(String src, String dst) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(src);
			createNewFile(dst);
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

	/**
	 * 创建文件，若文件夹不存在则自动创建文件夹，若文件存在则删除旧文件
	 * @param path :待创建文件路径
	 * */
	public static File createNewFile(String path) {
		File file = new File(path);
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
		} catch (IOException e) {
			LogUtil.e(e.getMessage());
		}
		return file;
	}

	/**
	 * 根据给出路径自动选择复制文件或整个文件夹
	 * @param src :源文件或文件夹路径
	 * @param dest :目标文件或文件夹路径
	 * */
	public static void copyFiles(String src, String dest) {
		File srcFile = new File(src);
		if (srcFile.exists()) {
			if (srcFile.isFile()) {
				copyFile(src, dest);
			} else {
				File[] subFiles = srcFile.listFiles();
				if (subFiles.length == 0) {
					File subDir = new File(dest);
					subDir.mkdirs();
				} else {
					for (File subFile : subFiles) {
						String subDirPath = dest + File.separator + subFile.getName();
						copyFiles(subFile.getAbsolutePath(), subDirPath);
					}
				}
			}
		}
	}

	public static boolean removeDirectory(String dir) {
		try {
			if (TextUtils.isEmpty(dir)) {
				return false;
			}

			return removeDirectory(new File(dir));
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean removeDirectory(File dir) {
		try {
			if (dir == null) {
				return false;
			}
			File[] fs = dir.listFiles();
			if (fs != null) {
				for (File f : fs) {
					if (f.isDirectory()) {
						if (!removeDirectory(f)) {
							return false;
						}
					} else if (!f.delete()) {
						return false;
					}
				}
			}
			return dir.delete();
		} catch (Exception e) {
			return false;
		}
	}
}
