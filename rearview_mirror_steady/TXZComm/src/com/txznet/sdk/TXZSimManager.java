package com.txznet.sdk;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.sdk.TXZService.CommandProcessor;

import android.text.TextUtils;

public class TXZSimManager {

	private static TXZSimManager mInstance = new TXZSimManager();

	private TXZSimManager() {

	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static TXZSimManager getInstance() {
		return mInstance;
	}

	public void onReconnectTXZ() {
		if (mHasSetSimTool) {
			setSimTool(mSimTool);
		}
	}

	/**
	 * 流量卡工具
	 * 
	 * @author bear
	 *
	 */
	public static interface SimTool {
		/**
		 * 流量卡警告回调
		 * 
		 * @param json
		 *            title:标题 msg:警告内容信息 text:tts播报文本
		 */
		void onSimAlarmHandle(String json);

		/**
		 * 流量卡充值二维码回调
		 * 
		 * @param json
		 *            name:套餐名 url:二维码链接 sell_price:促销价格 price:原价
		 */
		void onSimRechargeQrHandle(String json);

		/**
		 * 流量卡充值结果回调
		 * 
		 * @param json
		 *            state:充值结果 0 充值成功 msg:提示信息
		 */
		void onSimRechargeResultHandle(String json);
	}

	/**                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
	 * 
	 * @param tool
	 */
	public void setSimTool(SimTool tool) {
		mSimTool = tool;

		if (tool == null) {
			mHasSetSimTool = false;
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.sim.tool.clear", null, null);
			return;
		}

		mHasSetSimTool = true;
		TXZService.setCommandProcessor("tool.sim.", new CommandProcessor() {

			@Override
			public byte[] process(String packageName, String command, byte[] data) {
				if (TextUtils.isEmpty(command)) {
					return null;
				}
				final SimTool tool = mSimTool;
				if (tool == null) {
					return null;
				}
				if (command.equals("alarm")) {
					tool.onSimAlarmHandle(new String(data));
				} else if (command.equals("recharge.qr")) {
					tool.onSimRechargeQrHandle(new String(data));
				} else if (command.equals("recharge.result")) {
					tool.onSimRechargeResultHandle(new String(data));
				}

				return null;
			}
		});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.sim.tool.set", null, null);
	}

	private SimTool mSimTool;
	private boolean mHasSetSimTool;
}
