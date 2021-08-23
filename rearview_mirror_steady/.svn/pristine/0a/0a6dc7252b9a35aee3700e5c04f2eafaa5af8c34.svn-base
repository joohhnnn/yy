package com.txznet.music.service;

import java.io.File;
import java.io.FileFilter;
import java.util.Random;

import com.google.protobuf.nano.MessageNano;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.music.service.MusicInteractionWithCore.OnAppIDListener;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.PreUrlUtil;

import android.os.Bundle;
import android.os.Environment;

/**
 * 和Core交互的逻辑
 * 
 * @author telenewbie
 *
 */
public class MusicInteractionWithCore {
	private static int curSeq = new Random().nextInt();

	private static int getNewSeq() {
		curSeq++;
		if (curSeq == 0) {
			curSeq++;
		}
		return curSeq;
	}

	/**
	 * 发消息给Core
	 * 
	 * @param command
	 *            命令
	 * @param data
	 *            数据
	 */
	public static void sendToCore(String command, byte[] data) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, command, data, null);
	}

	/**
	 * 请求数据
	 * 
	 * @param command
	 *            命令
	 * @param data
	 *            pb数据
	 * @return 当前请求的ID
	 */
	public static int requestData(String command, String url, byte[] data) {
		com.txz.ui.audio.UiAudio.Req_DataInterface dataInterface = new com.txz.ui.audio.UiAudio.Req_DataInterface();
		dataInterface.strCmd = url;
		dataInterface.strData = data;
		dataInterface.uint32Seq = getNewSeq();
		sendToCore(command, MessageNano.toByteArray(dataInterface));
		return dataInterface.uint32Seq;
	}

	/**
	 * 请求数据
	 * 
	 * @param command
	 *            命令
	 * @param data
	 *            pb数据
	 * @return 当前请求的ID
	 */
	public static byte[] getData(String command, byte[] data) {
		ServiceData sendInvokeSync = ServiceManager.getInstance().sendInvokeSync(ServiceManager.TXZ, command, data);
		if (sendInvokeSync != null) {
			return sendInvokeSync.getBytes();
		}
		return null;
		// return dataInterface.uint32Seq;
	}

	/**
	 * 获取预处理路径
	 * 
	 * @param downloadUrl
	 */
	public static String getProcessUrl(String downloadUrl) {
		return PreUrlUtil.genPreUrl(downloadUrl);
	}

	public static void getCoreVersion(final OnDataListener listener) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.PackageInfo", null, new GetDataCallback() {

			@Override
			public void onGetInvokeResponse(ServiceData sendInvokeSync) {
				Bundle bundle = null;
				if (sendInvokeSync != null) {
					JSONBuilder jsonBuilder = new JSONBuilder(sendInvokeSync.getBytes());
					int versionCode = jsonBuilder.getVal("versionCode", int.class, 0);
					bundle = new Bundle();
					bundle.putInt("versionCode", versionCode);
				}

				if (listener != null) {
					listener.getData(bundle);
				}
			}
		});
	}

	public static void initAppid(final OnAppIDListener listener) {
		PreUrlUtil.initAppid(listener);
	}

	public interface OnAppIDListener {
		public boolean onSuccess(String uid, String appid);

		public void onError(String errorMessage);
	}

	public interface OnDataListener {
		public void getData(Bundle bundle);
	}
}