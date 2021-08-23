package com.txznet.comm.ui.theme.test.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZipUtil {
	private static UnZipUtil sInstance = new UnZipUtil();
	private final static int BUFFER_SIZE = 4096;
	private ThreadLocal<byte[]> mBufferThreadLocal = new ThreadLocal<byte[]>();//1、避免不停的创建读写缓存区。2、可以满足多线程同时调用同一个UnZip对象解压文件。
	
	private UnZipUtil(){
		
	}
	
	public static UnZipUtil getInstance(){
		return sInstance;
	}
	
	/*
	 * 解压整个压缩包
	 * 
	 */
	public void UnZip(String zipFilePath, String dstPath) {
		if (mBufferThreadLocal.get() == null){
			System.out.println("create before : " + mBufferThreadLocal.get());
			mBufferThreadLocal.set(new byte[BUFFER_SIZE]);
			System.out.println("create after : " + mBufferThreadLocal.get());
		}
		
		ZipFile zipFile = null;
		try {
			File file = new File(zipFilePath);// 压缩文件路径
			 zipFile = new ZipFile(file);
			Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
			while(zipEntries.hasMoreElements()){
				ZipEntry entry = zipEntries.nextElement();
				UnZip(zipFile, entry, dstPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//释放资源
		if (zipFile != null) {
			try {
				zipFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (mBufferThreadLocal.get() != null){
			System.out.println("release before : " + mBufferThreadLocal.get());
			mBufferThreadLocal.set(null);
			System.out.println("release after : " + mBufferThreadLocal.get());
		}
	}
	
	
	/*
	 * 解压单个文件 
	 */
	public void UnZip(String zipFilePath, String unzipName, String dstPath) {
		if (mBufferThreadLocal.get() == null){
			System.out.println("create before : " + mBufferThreadLocal.get());
			mBufferThreadLocal.set(new byte[BUFFER_SIZE]);
			System.out.println("create after : " + mBufferThreadLocal.get());
		}
		
		ZipFile zipFile = null;
		try {
			File file = new File(zipFilePath);// 压缩文件路径
			zipFile = new ZipFile(file);
			ZipEntry entry = zipFile.getEntry(unzipName);
			if (entry != null) {
				UnZip(zipFile, entry, dstPath);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 释放资源
		if (zipFile != null) {
			try {
				zipFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		if (mBufferThreadLocal.get() != null){
			System.out.println("release before : " + mBufferThreadLocal.get());
			mBufferThreadLocal.set(null);
			System.out.println("release after : " + mBufferThreadLocal.get());
		}
	}
	
	/*
	 * 解压单个文件以String类型返回(适合小文件、特大文件最好不要使用该接口)
	 */
	public String UnZipToString(String zipFilePath, String unzipName) {
		String strContent = null;
		ZipFile zipFile = null;
		try {
			File file = new File(zipFilePath);// 压缩文件路径
			zipFile = new ZipFile(file);
			ZipEntry entry = zipFile.getEntry(unzipName);
			if (entry != null) {
				InputStream in = zipFile.getInputStream(entry);
				int lenght = in.available();
				if (lenght > 0) {
					byte[] data = new byte[in.available()];
					int read = 0;
					read = in.read(data, 0, lenght);
					if (read > 0){
						strContent = new String(data, 0, read);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 释放资源
		if (zipFile != null) {
			try {
				zipFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return strContent;
	}
	
	/*
	 * 判断判断压缩包里面是否存在某个文件
	 */
	public boolean HasEntry(String zipFilePath, String unzipName) {
		boolean bRet = false;
		ZipFile zipFile = null;
		try {
			File file = new File(zipFilePath);// 压缩文件路径
			zipFile = new ZipFile(file);
			ZipEntry entry = zipFile.getEntry(unzipName);
			bRet = entry != null;
		} catch (Exception e) {
		}
		// 释放资源
		if (zipFile != null) {
			try {
				zipFile.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return bRet;
	}
	
	//Entry只可能是文件或者是空文件夹
	private void UnZip(ZipFile zipFile, ZipEntry zipEntry, String dstPath) {
		InputStream inputStream = null;
		OutputStream outputStream = null;
		try {
			//getName获取的是压缩文件的全路径
			File outFile = new File(dstPath + File.separator + zipEntry.getName());
			//如果是空文件夹只需创建文件夹即可
			if (zipEntry.isDirectory()) {
				if (!outFile.exists()) {
					outFile.mkdirs();
				}
				return;
			}
			
			//
			if (outFile.exists() && outFile.length() == zipEntry.getSize()){
				System.out.println(outFile.getName() + " already has be unziped completed last time!");
				return;
			}
			System.out.println(zipEntry.getName() + " will be unziped !");
			
			//创建必要的路径
			if (!outFile.getParentFile().exists()) {
				outFile.getParentFile().mkdirs();
			}
			
			inputStream = zipFile.getInputStream(zipEntry);
			outputStream = new FileOutputStream(outFile);
			byte[] data = mBufferThreadLocal.get();
			int read = 0;
			while (true) {
				read = inputStream.read(data, 0, data.length);
				if (read < 0) {
					break;
				}
				outputStream.write(data, 0, read);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//释放资源
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (Exception e) {
			}
		}

		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (Exception e) {
			}
		}
	}
}
