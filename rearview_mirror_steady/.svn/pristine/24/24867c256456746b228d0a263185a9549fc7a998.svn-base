package com.txznet.sdk;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZCarControlManager.ACMgrTool.ACMode;
import com.txznet.sdk.TXZService.CommandProcessor;

public class TXZCarControlManager {
	private static TXZCarControlManager sInstance;

	private TXZCarControlManager() {
	}

	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static TXZCarControlManager getInstance() {
		if (sInstance == null) {
			synchronized (TXZCarControlManager.class) {
				if (sInstance == null) {
					sInstance = new TXZCarControlManager();
				}
			}
		}
		return sInstance;
	}

	/**
	 * 设置空调工具
	 *
	 */
	public static abstract class ACMgrTool {
		/**
		 * 调高温度
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean incTemp() {
			return false;
		}

		/**
		 * 降低温度
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean decTemp() {
			return false;
		}

		/**
		 * 最高温度
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean maxTemp() {
			return false;
		}

		/**
		 * 最低温度
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean minTemp() {
			return false;
		}

		/**
		 * 风速大一点
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean incWSpeed() {
			return false;
		}

		/**
		 * 风速小一点
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean decWSpeed() {
			return false;
		}

		/**
		 * 打开空调开关
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean openAirConditioner() {
			return false;
		}

		/**
		 * 关闭空调开关
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean closeAirConditioner() {
			return false;
		}

		/**
		 * 打开外循环
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean outLoop() {
			return false;
		}

		/**
		 * 打开内循环
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean innerLoop() {
			return false;
		}

		/**
		 * 打开前除霜
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean openFDef() {
			return false;
		}

		/**
		 * 关闭前除霜
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean closeFDef() {
			return false;
		}

		/**
		 * 打开后除霜
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean openADef() {
			return false;
		}

		/**
		 * 关闭后除霜
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean closeADef() {
			return false;
		}

		/**
		 * 打开压缩机
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean openCompressor() {
			return false;
		}

		/**
		 * 关闭压缩机
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean closeCompressor() {
			return false;
		}

		/**
		 * 切换到模式
		 * 
		 * @param mode
		 *            参考 {@link ACMode}
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean selectMode(ACMode mode) {
			return false;
		}

		/**
		 * 调到几度
		 * 
		 * @param temp
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean ctrlToTemp(int temp) {
			return false;
		}

		/**
		 * 调高几度
		 * 
		 * @param temp
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean incTemp(int temp) {
			return false;
		}

		/**
		 * 调低几度
		 * 
		 * @param temp
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean decTemp(int temp) {
			return false;
		}

		/**
		 * 调到几档风
		 * 
		 * @param speed
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean ctrlToWSpeed(int speed) {
			return false;
		}
		
		/**
		 * 最小风速
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean minWSpeed() {
			return false;
		}
		
		/**
		 * 最大风速
		 * 
		 * @return false 表示不处理，提示不支持该指令；<br>
		 *         true 表示自己处理，需要自己控制播报等
		 */
		public boolean maxWSpeed() {
			return false;
		}

		/**
		 * 空调的模式
		 */
		public static enum ACMode {
			/**
			 * 吹脸模式
			 */
			MODE_BLOW_FACE,
			/**
			 * 吹脸吹脚模式
			 */
			MODE_BLOW_FACE_FOOT,
			/**
			 * 吹脚模式
			 */
			MODE_BLOW_FOOT,
			/**
			 * 吹脚除霜
			 */
			MODE_BLOW_FOOT_DEFROST,
			/**
			 * 除霜
			 */
			MODE_DEFROST,
			/**
			 * 自动
			 */
			MODE_AUTO
		}
	}

	private boolean mHasSetAcMgrTool = false;
	private ACMgrTool mACMgrTool = null;

	public void setACMgrTool(ACMgrTool acMgrTool) {
		mHasSetAcMgrTool = true;
		mACMgrTool = acMgrTool;
		if (mACMgrTool == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.ac.acmgr.cleartool", null, null);
			return;
		}
		TXZService.setCommandProcessor("tool.acmgr.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command,
					byte[] data) {
				boolean ret = false;
				if (command.equals("incTemp")) {
					if (data != null && data.length > 0) {
						try {
							ret = mACMgrTool.incTemp(new JSONBuilder(data)
									.getVal("data", Integer.class, 0));
						} catch (Exception e) {
						}
					} else {
						ret = mACMgrTool.incTemp();
					}
				} else if (command.equals("decTemp")) {
					if (data != null && data.length > 0) {
						try {
							ret = mACMgrTool.decTemp(new JSONBuilder(data)
									.getVal("data", Integer.class, 0));
						} catch (Exception e) {
						}
					} else {
						ret = mACMgrTool.decTemp();
					}
				} else if (command.equals("maxTemp")) {
					ret = mACMgrTool.maxTemp();
				} else if (command.equals("minTemp")) {
					ret = mACMgrTool.minTemp();
				} else if (command.equals("incWSpeed")) {
					ret = mACMgrTool.incWSpeed();
				} else if (command.equals("decWSpeed")) {
					ret = mACMgrTool.decWSpeed();
				} else if (command.equals("openAC")) {
					ret = mACMgrTool.openAirConditioner();
				} else if (command.equals("closeAC")) {
					ret = mACMgrTool.closeAirConditioner();
				} else if (command.equals("outLoop")) {
					ret = mACMgrTool.outLoop();
				} else if (command.equals("innerLoop")) {
					ret = mACMgrTool.innerLoop();
				} else if (command.equals("openFDef")) {
					ret = mACMgrTool.openFDef();
				} else if (command.equals("closeFDef")) {
					ret = mACMgrTool.closeFDef();
				} else if (command.equals("openADef")) {
					ret = mACMgrTool.openADef();
				} else if (command.equals("closeADef")) {
					ret = mACMgrTool.closeADef();
				} else if (command.equals("selectMode")) {
					try {
						ret = mACMgrTool.selectMode(ACMode
								.valueOf(new JSONBuilder(data).getVal("data",
										String.class, "")));
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else if (command.equals("openCompressor")) {
					ret = mACMgrTool.openCompressor();
				} else if (command.equals("closeCompressor")) {
					ret = mACMgrTool.closeCompressor();
				} else if (command.equals("ctrlToTemp")) {
					try {
						ret = mACMgrTool.ctrlToTemp(new JSONBuilder(data)
								.getVal("data", Integer.class, 0));
					} catch (Exception e) {
					}
				} else if (command.equals("ctrlToWSpeed")) {
					try {
						ret = mACMgrTool.ctrlToWSpeed(new JSONBuilder(data)
								.getVal("data", Integer.class, 0));
					} catch (Exception e) {
					}
				}else if (command.equals("maxWSpeed")) {
					ret = mACMgrTool.maxWSpeed();
				}else if (command.equals("minWSpeed")) {
					ret = mACMgrTool.minWSpeed();
				}
				if (!ret) {
					TXZResourceManager.getInstance().speakTextOnRecordWin(
							"抱歉，当前不支持该操作", false, null);
				}

				return null;
			}
		});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.ac.acmgr.settool", null, null);
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		if (mHasSetAcMgrTool) {
			if (mACMgrTool == null) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.ac.acmgr.cleartool", null, null);
			} else {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
						"txz.ac.acmgr.settool", null, null);
				if (mMinTempValue != null && mMaxTempValue != null) {
					setTEMPDistance(mMinTempValue, mMaxTempValue);
				}
				if (mMinWSpeedValue != null && mMaxWSpeedValue != null) {
					setWSpeedDistance(mMinWSpeedValue, mMaxWSpeedValue);
				}
			}
			
		}
	}
	
	private Integer mMaxTempValue;
	private Integer mMinTempValue;
	
	/**
	 * 设置空调温度范围
	 * @param minTempValue
	 * @param maxTempValue
	 * @return
	 */
	public boolean setTEMPDistance(int minTempValue,int maxTempValue){
		if (minTempValue >0 && maxTempValue > minTempValue) {
			mMaxTempValue = maxTempValue;
			mMinTempValue = minTempValue;
			JSONBuilder json = new JSONBuilder();
			json.put("maxVal", mMaxTempValue);
			json.put("minVal", mMinTempValue);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.ac.settempdistance", json.toString().getBytes(), null);
			return true;
		}
		return false;
	}
	private Integer mMaxWSpeedValue;
	private Integer mMinWSpeedValue;
	
	/**
	 * 设置风速的范围
	 * @param minWSpeedValue
	 * @param maxWSpeedValue
	 * @return
	 */
	public boolean setWSpeedDistance(int minWSpeedValue,int maxWSpeedValue) {
		if (minWSpeedValue >=0 && maxWSpeedValue > minWSpeedValue) {
			mMaxWSpeedValue = maxWSpeedValue;
			mMinWSpeedValue = minWSpeedValue;
			JSONBuilder json = new JSONBuilder();
			json.put("maxVal", mMaxWSpeedValue);
			json.put("minVal", mMinWSpeedValue);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.ac.setwspeeddistance", json.toString().getBytes(), null);
			return true;
		}
		return false;
	}
	
	
}
