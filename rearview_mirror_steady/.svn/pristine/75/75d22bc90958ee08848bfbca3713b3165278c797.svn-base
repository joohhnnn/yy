package com.txznet.feedback.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.txznet.comm.remote.util.LogUtil;

public class IOUtil {
	
	public static InputStream getFileInputStream(String fileName){
		List<File> files = FileUtil.getFileByName(fileName);
		if(files != null){
			try {
				FileInputStream fis = new FileInputStream(files.get(0));
				return fis;
			} catch (FileNotFoundException e) {
				LogUtil.loge("FileNotFoundException 找不到可解析文件！");
			}
		}
		return null;
	}
	
	public static String parseFile(File f){
		if(f == null || !f.exists()){
			return "";
		}
		
		FileInputStream fis = null;
		ByteArrayOutputStream baos = null;
		StringBuilder sb = new StringBuilder();
		try {
			fis = new FileInputStream(f);
			baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int len = 0;
			while ((len = fis.read(buffer)) != -1) {
				baos.write(buffer,0,len);
			}
			
			return baos.toString();
		} catch (FileNotFoundException e) {
			LogUtil.loge(e.toString());
		} catch (IOException e) {
			LogUtil.loge(e.toString());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					LogUtil.loge(e.toString());
				}
			}
			if (baos != null) {
				try {
					baos.close();
				} catch (IOException e) {
					LogUtil.loge(e.toString());
				}
			}
		}
		return "";
	}
}
