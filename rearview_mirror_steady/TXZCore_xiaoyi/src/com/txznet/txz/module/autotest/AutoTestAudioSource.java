package com.txznet.txz.module.autotest;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import com.txznet.txz.jni.JNIHelper;

/**
 * 自动化测试调用的方法
 * 
 * @author ASUS User
 *
 */
public class AutoTestAudioSource {

	/**
	 * 获取单例
	 */
	private static AutoTestAudioSource autoTestInstance = new AutoTestAudioSource();

	public static AutoTestAudioSource getInstance() {
		return autoTestInstance;
	}

	// 字节输入流
	private static InputStream in = null;
	// 获取正在测试的音频路径
	private String path = "";

	/**
	 * 获取自动化测试音频资源的对象
	 */

	/**
	 * 向外暴露的自动化测试音频路径设置路口
	 * 
	 * @param callback
	 * @param path
	 */
	public void getAutoTestAudioResourcePath(String path,
			AutoTestAudioSourceCallback callback) {// path为null，设置识别模式为语音识别，否则设置为文件读取模式
		this.path=callback.setAutoTestAudioSourcePath(path);
		return;
	}

	/**
	 * 打开文件句柄
	 * 
	 * @return
	 */
	public int openAutoTestResource() {
		try {
			in = new FileInputStream(path);
			if (path.endsWith(".wav")) {
				in.skip(44);
			}
			JNIHelper.logd("com.txznet.txz.module.autotest:语音文件读取成功");
		} catch (Exception e) {
			e.printStackTrace();
			JNIHelper.logd("com.txznet.txz.module.autotest:语音文件读取失败");
			return -1;
		}
		return 0;
	}

	/**
	 * 关闭文件句柄
	 */
	public void closeAutoTestResource() {

		if (in != null) {
			try {
				in.close();
				JNIHelper.logd("com.txznet.txz.module.autotest:语音文件读取结束");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 模拟语音识别 从缓冲区读取数据，每次1200个字节，每次耗时37.5毫秒，然后sleep 20毫秒 模拟语音识别从录音机读取数据
	 * 一次读取1200字节，耗时大概45毫秒
	 * 
	 * @param buffer
	 * @param size
	 * @return
	 */
	public int read(byte[] buffer, int size) {
		int read = 0;
		if (in == null) {
			return 0;
		}
		try {
			read = in.read(buffer, 0, size);
			Thread.sleep(30);
		} catch (Exception e) {
			e.printStackTrace();
		}
		JNIHelper.logd("read=" + read);
		if (read == -1) {
			Arrays.fill(buffer, (byte) 0);
			read = size;
		}
		return read;
	}

	/**
	 * 打开语音播放
	 */
	public synchronized int openOut() {
		return -1;
	}

	/**
	 * 关闭语音播放
	 */
	public synchronized void closeOut() {
	}

	/**
	 * 向声音播放源添加数据
	 * 
	 * @param buffer
	 * @param size
	 * @return
	 */
	public int write(byte[] buffer, int size) {
		return 0;
	}
}
