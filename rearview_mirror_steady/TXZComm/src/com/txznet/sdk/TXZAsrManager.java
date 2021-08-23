package com.txznet.sdk;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.text.TextUtils;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.IAsrRegCmdCallBack;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZSceneManager.SceneType;
import com.txznet.sdk.TXZService.CommandProcessor;
import com.txznet.txz.util.runnables.Runnable1;

/**
 * 类名：语音识别管理器
 * 类描述：负责语音识别相关功能配置项。主要包含语音识别超时配置选项，命令字(Command)、
 * 		   免唤醒词(wakeUpComplex)，以及设置FM、AM 相关功能。
 *
 */
public class TXZAsrManager {
	private static TXZAsrManager sInstance = new TXZAsrManager();
	private Set<CommandListener> mCommandListeners = new HashSet<CommandListener>();

	private IAsrRegCmdCallBack mCommCallBack = new IAsrRegCmdCallBack() {// 语音识别命令注册的识别回调
		@Override
		public void notify(String cmd, byte[] data) {
			for (CommandListener listener : mCommandListeners) {
				if (listener != null) {
					listener.onCommand(cmd, new String(data));
				}
			}
		}

	};

	private TXZAsrManager() {

	}

	/**
	 * 获取单例
	 * 
	 * @return 类实例
	 */
	public static TXZAsrManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		if (mHasFMTool) {
			if (mMaxFmFreqValue != null && mMinFmFreqValue != null) {
				if (mJumpsPoints != null) {
					setFreqWithJumps(mMinFmFreqValue, mMaxFmFreqValue,
							mJumpsPoints);
				} else {
					setFreqDistance(mMinFmFreqValue, mMaxFmFreqValue);
				}
			}
		}

		if (mHasAMTool) {
			if (mMaxAmValue != null && mMinAmValue != null) {
				setFreqDistanceForAM(mMinAmValue, mMaxAmValue);
			}
		}

		if (mHasAsrTool) {
			setAsrTool(mAsrTool);
		}
		if(mAsrListener != null){
			setAsrListener(mAsrListener);
		}
		if (mBOS != null) {
			setBOS(mBOS);
		}
		if (mEOS != null) {
			setEOS(mEOS);
		}
		if (mClose != null) {
			setCloseWinWhenEndCmd(mClose);
		}
		if (mEnableFMOnlineCmds != null){
			enableFMOnlineCmds(mEnableFMOnlineCmds);
		}
		if(mAsrDelayAfterBeep != null){
			setAsrDelayAfterBeep(mAsrDelayAfterBeep);
		}
		
		if (mRealFictitiousCmds !=null && mRealFictitiousCmds.size() > 0) {
			JSONArray realJry = new JSONArray();
			try {
				for (Entry<String, String> entry : mRealFictitiousCmds.entrySet()) {
					JSONObject job = new JSONObject();
					job.put("real", entry.getValue());
					job.put("fictitious", entry.getKey());
					realJry.put(job);
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.asr.setRealFictitiousCmds", realJry.toString().getBytes(), null);
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
		
	}

	/**
	 * 方法名：触发声控按键
	 * 方法描述：触发声控按键，相当于点击了一次声控按钮
	 */
	public void triggerRecordButton() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"comm.asr.triggerRecordButton", null, null);
	}
	
	
	private Integer mAsrDelayAfterBeep = null;

	/**
	 * 方法名：延迟识别
	 * 方法描述：在滴的一声后延迟delay毫秒再进行识别，谨慎使用
	 *
	 * @param delay 延迟的时间,单位毫秒
	 */
	public void setAsrDelayAfterBeep(int delay) {
		if (delay < 0) {
			return;
		}
		mAsrDelayAfterBeep = delay;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.asr.set.asrDelayAfterBeep",
				("" + delay).getBytes(), null);
	}

	/**
	 * 方法名：启动识别
	 * 方法描述：启动识别，弹出声控界面，如果已经启动录音窗口或者正在识别中，则直接提示文本
	 *
	 * @param hint 提示文本
	 */
	public void start(String hint) {
		AsrUtil.startWithRecordWin(hint);
	}

	/**
	 * 方法名：启动声控
	 * 方法描述：启动声控，无声控界面和语音提示，直接进入语音录音状态，已在界面时直接进入录音状态
	 */
	public void start() {
		AsrUtil.start();
	}

	/**
	 * 仅进行识别，不处理语义结果
	 *
	 */
	public void startOnly(IAsrCallback callback) {
		AsrUtil.startOnly(callback);
	}
	
	/**
	 * 方法名：重新启动声控界面
	 * 方法描述：重新启动声控界面，无论是否打开过界面都会重新打开，并提示文本内容
	 * 			提示内容可以为空，为空时提示默认内容
	 *
	 * @param hint 提示文本
	 */
	public void restart(String hint) {
		if (hint == null || hint.length() == 0) {
			hint = "";
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.asr.restartWithRecordWin", hint.getBytes(), null);
	}

	/**
	 * 方法名：启动语音界面，并以传入文本识别
	 * 方法描述：打开语音界面并传入一段文本，同时以当前传入文本开始识别
	 * 			即以传入文本进行语音识别功能
	 *
	 * @param rawText 待识别文本
	 */
	public void startWithRawText(String rawText) {
		if (rawText == null) {
			return;
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"comm.asr.startWithRawText", rawText.getBytes(), null);
	}

	/**
	 * 方法名：停止录音
	 * 方法描述：立即停止当前录音，开始识别
	 *
	 */
	public void stop() {
		AsrUtil.stop();
	}

	/**
	 * 方法名：取消识别
	 * 方法描述：停止当前的识别和播报，关闭语音界面，推荐使用此方法关闭语音界面
	 */
	public void cancel() {
		AsrUtil.cancel();
	}

	/**
	 * 方法名：注册命令字
	 * 方法描述：通过此方法来注册想要实现功能的命令字
	 * 			离线命令字要求一对一，离线情况下有效，在语音界面时语音输入对应指令会收到回调。
	 * 	   		NOTE：命令字集合名一般不允许重复，重复情况下会与前一个集合进行命令字合并。
	 *
	 * @param cmds 注册的命令字所有说法的集合
	 * @param data 命令字集合的名字，会根据此数据判断命中了哪一个命令字,回调时回传此data
	 * @return 默认返回true
	 */
	public boolean regCommand(String[] cmds, String data) {
		AsrUtil.regCmd(cmds, data, mCommCallBack);
		return true;
	}
	
	
	private HashMap<String, String> mRealFictitiousCmds;

	/**
	 * 方法名：替换语音显示文本
	 * 方法描述：当语音识别生僻字，显示或识别不正确时，可以通过此接口临时替换，以提升用户体验。
	 * 			传入需要显示和识别的文本，以及需要替换的文本集合。后续显示和识别会全部替换
	 *
	 * @param real       真实词，处理之后的词，会显示在语音界面
	 * @param fictitious 虚拟词，需要处理的词。可以同时传入多个虚拟词，将之全部替换为同一个真实词
	 * @return 传入数据格式是否正确 true：正确 false：不正确
	 */
	public boolean setRealFictitiousCmds(String real,String... fictitious){
		if (TextUtils.isEmpty(real) || fictitious == null || fictitious.length==0) {
			return false;
		}
		if (mRealFictitiousCmds == null) {
			synchronized (TXZAsrManager.class) {
				if (mRealFictitiousCmds == null) {
					mRealFictitiousCmds = new HashMap<String, String>();
				}
			}
		}
		JSONArray realJry = new JSONArray();
		try {
			for (String str : fictitious) {
				JSONObject job = new JSONObject();
				job.put("real", real);
				job.put("fictitious", str);
				mRealFictitiousCmds.put(str, real);
				realJry.put(job);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.asr.setRealFictitiousCmds", realJry.toString().getBytes(), null);
		return true;
	}

	/**
	 * 方法名：移除需要替换真身的词
	 * 方法描述：将之前设置的需要替换的词移除
	 *
	 * @param fictitious 虚拟词列表
	 * @return 传入数据格式是否正确 true：正确 false：不正确
	 */
	public boolean removeRealFictitiousCmds(String... fictitious){
		if (fictitious == null || fictitious.length == 0) {
			return false;
		}
		JSONArray jry = new JSONArray();
		for (String str : fictitious) {
			jry.put(str);
			if (mRealFictitiousCmds!=null&&mRealFictitiousCmds.size()>0) {
				mRealFictitiousCmds.remove(str);
			}
		}
		
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.asr.removeRealFictitiousCmds", jry.toString().getBytes(), null);
		return true;
	}


	/**
	 * 方法名：注册命令字
	 * 方法描述：通过此方法来注册离线命令字(Command)功能。
	 * 			离线命令字要求一对一，离线情况下有效，在语音界面时语音输入对应指令会收到回调。
	 * 			NOTE：命令字集合名一般不允许重复，重复情况下会与前一个集合进行命令字合并。
	 *
	 * @param cmds  注册的命令字集合
	 * @param data  命令字集合名
	 * @return 传入数据格式是否正确 true：正确 false：不正确
	 */
	public boolean regCommand(Collection<String> cmds, String data) {
		if (cmds == null) {

			return false;
		}

		int count = 0;
		count = cmds.size();

		if (count <= 0) {
			return false;
		}

		String[] cmdArray = new String[count];
		cmds.toArray(cmdArray);

		AsrUtil.regCmd(cmdArray, data, mCommCallBack);
		return true;
	}

	private boolean mHasFMTool = false;
	private boolean mHasAMTool = false;
	private Float mMinFmFreqValue = null;
	private Float mMaxFmFreqValue = null;
	private Integer mMinAmValue = null;
	private Integer mMaxAmValue = null;
	private float[] mJumpsPoints = null;
	private Integer mBOS = null;
	private Integer mEOS = null;

//	/**
//	 * 方法名：设置FM调整可支持频段
//	 * 方法描述：设置FM可以响应可以使用的范围，设置后可以离线支持
//	 *
//	 * @param minVal 最小频率数值
//	 * @param maxVal 最大频率数值
//	 * @return 传入数据格式是否正确 true：正确 false：不正确
//	 */
	private boolean setFreqDistance(float minVal, float maxVal) {
		if (minVal > 0 && maxVal > minVal) {
			mMinFmFreqValue = minVal;
			mMaxFmFreqValue = maxVal;
			JSONBuilder json = new JSONBuilder();
			json.put("minVal", mMinFmFreqValue);
			json.put("maxVal", mMaxFmFreqValue);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.fm.setdistance", json.toString().getBytes(), null);
			return true;
		}
		return false;
	}

//	/**
//	 * 方法名：设置AM调整可支持频段
//	 * 方法描述：设置AM可以响应可以使用的范围，设置后可以离线支持
//	 *
//	 * @param minVal 最小幅度数值
//	 * @param maxVal 最大幅度数值
//	 * @return 传入数据格式是否正确 true：正确 false：不正确
//	 */
	private boolean setFreqDistanceForAM(int minVal, int maxVal) {
		if (minVal > 0 && maxVal > minVal) {
			mMinAmValue = minVal;
			mMaxAmValue = maxVal;
			JSONBuilder json = new JSONBuilder();
			json.put("minVal", mMinAmValue);
			json.put("maxVal", mMaxAmValue);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.am.setdistance", json.toString().getBytes(), null);
			return true;
		}
		return false;
	}

//	/**
//	 * 方法名：设置FM频段并跳过某些点
//	 * 方法描述：设置F可以使用FM 频段的范围，并且跳过中间的某些点
//	 *
//	 * @param minVal 最小频率数值
//	 * @param maxVal 最大频率数值
//	 * @param jumps  需要跳过的点的数组
//	 * @return 传入数据格式是否正确 true：正确 false：不正确
//	 */
	private boolean setFreqWithJumps(float minVal, float maxVal, float[] jumps) {
		if (jumps == null) {
			setFreqDistance(minVal, maxVal);
			return true;
		}

		if (minVal > 0 && maxVal > minVal) {
			mMinFmFreqValue = minVal;
			mMaxFmFreqValue = maxVal;
			mJumpsPoints = jumps;
			JSONBuilder json = new JSONBuilder();
			json.put("minVal", mMinFmFreqValue);
			json.put("maxVal", mMaxFmFreqValue);
			json.put("hasJump", true);
			for (int i = 0; i < jumps.length; i++) {
				json.put("jump" + i, jumps[i]);
			}

			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.fm.setdistance", json.toString().getBytes(), null);
			return true;
		}
		return false;
	}

	/**
	 * 方法名：设置识别录音前端超时时间
	 * 方法描述：前端超时时间：即录音开始后，若一直没有录到声音，则停止录音的最大等待时间。默认5000ms
	 *
	 * @param val 识别录音前端超时时间, 单位毫秒，取值范围 1000ms - 20000ms。
	 */
	public void setBOS(int val) {
		mBOS = val;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"comm.asr.set.bos", ("" + val).toString().getBytes(), null);
	}

	/**
	 * 方法名：设置识别录音后端超时时间
	 * 方法描述：在录音过程中，一直没有录到声音，等待一定时间后就停止录音，这个时间就是后端超时时间，默认1000ms
	 *
	 * @param val  识别录音后端超时时间, 单位毫秒，取值范围 50ms - 5000ms。
	 *
	 */
	public void setEOS(int val) {
		mEOS = val;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"comm.asr.set.eos", ("" + val).toString().getBytes(), null);
	}

	
	Boolean mEnableFMOnlineCmds = null;

	/**
	 * 方法名：开启FM指令在线识别
	 * 方法描述：开启FM指令在线识别，例如：开启后“收听北京交通广播”等指令会走到调频，未开启时走到电台，默认关闭
	 *
	 * @param enable 是否开启
	 */
	public void enableFMOnlineCmds(boolean enable){
		mEnableFMOnlineCmds = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.asr.enableFMOnlineCmds", (""+mEnableFMOnlineCmds).getBytes(), null);
	}

	/**
	 * 方法名：注册FM控制的命令字
	 * 方法描述：注册[from, to]范围内所有的“调频到xxx.x“、“调频到xxx.x兆赫“命令字
	 *
	 * @param from          开始频段，仅保留小数点后一位
	 * @param to            结束频段，仅保留小数点后一位
	 * @param callback_data 命令字回调
	 * @return 注册是否成功
	 */
	public boolean regCommandForFM(float from, float to,
			final String callback_data) {
		if (from > to || callback_data == null) {
			return false;
		}
		mHasFMTool = true;
		TXZService.setCommandProcessor("tool.fm.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command,
					byte[] data) {
				if (command.equals("toFmFreq")) {
					try {
						JSONBuilder json = new JSONBuilder(new String(data));
						float freq = json.getVal("freqValue", Float.class);
						if (mCommCallBack != null) {
							mCommCallBack.notify("调频到" + freq, (callback_data
									+ "#" + freq).getBytes());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
				return null;
			}
		});
		setFreqDistance(from, to);
		return true;
	}

	/**
	 * 方法名：注册AM控制的命令字
	 * 方法描述：注册[from, to]内所有的“调幅xxx“、“调幅到xxx“命令字
	 *
	 * @param from          开始幅度
	 * @param to            结束幅度
	 * @param callback_data 命令字回调
	 * @return 注册是否成功
	 */
	public boolean regCommandForAM(int from, int to, final String callback_data) {
		if (from > to || callback_data == null) {
			return false;
		}
		mHasAMTool = true;
		TXZService.setCommandProcessor("tool.am.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command,
					byte[] data) {
				if (command.equals("toAmFreq")) {
					try {
						JSONBuilder json = new JSONBuilder(new String(data));
						int freq = json.getVal("freqValue", Integer.class);
						if (mCommCallBack != null) {
							mCommCallBack.notify("调幅到" + freq, (callback_data
									+ "#" + freq).getBytes());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
				return null;
			}
		});
		setFreqDistanceForAM(from, to);
		return true;
	}

	/**
	 * 方法名：注册FM控制的命令字并跳过某些点
	 * 方法描述：注册[from, to]内所有的“调幅xxx“、“调幅到xxx“命令字，并且跳过中间的某些点
	 *
	 * @param from          开始频段，仅保留小数点后一位
	 * @param to            结束频段，仅保留小数点后一位
	 * @param jumps         需要跳过的点的数组
	 * @param callback_data 回调的数据
	 * @return 注册是否成功
	 */
	public boolean regCommandFmWithJumpPoint(float from, float to,
			float[] jumps, final String callback_data) {
		if (from > to || callback_data == null) {
			return false;
		}
		mHasFMTool = true;
		TXZService.setCommandProcessor("tool.fm.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command,
					byte[] data) {
				if (command.equals("toFmFreq")) {
					try {
						JSONBuilder json = new JSONBuilder(new String(data));
						float freq = json.getVal("freqValue", Float.class);
						if (mCommCallBack != null) {
							mCommCallBack.notify("调频到" + freq, (callback_data
									+ "#" + freq).getBytes());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
				return null;
			}
		});
		setFreqWithJumps(from, to, jumps);
		return true;
	}

	/**
	 * 方法名：注册命令字
	 * 方法描述：通过此方法来注册离线命令字(Command)功能。
	 * 			离线命令字要求一对一，离线情况下有效，在语音界面时语音输入对应指令会收到回调。
	 * 			NOTE：命令字集合名一般不允许重复，重复情况下会与前一个集合进行命令字合并。
	 *
	 * @param cmd  需要注册的命令字
	 * @param data 命令字集合名
	 * @return 传入数据格式是否正确 true：正确 false：不正确
	 */
	public boolean regCommand(String cmd, String data) {
		AsrUtil.regCmd(new String[] { cmd }, data, mCommCallBack);
		return true;
	}

	/**
	 * 方法名：注销命令字
	 * 方法描述：根据传入的命令字数组来注销命令字
	 *
	 * @param cmds 注销的命令字数组
	 * @return 返回true
	 */
	public boolean unregCommand(String[] cmds) {
		AsrUtil.unregCmd(cmds);
		return true;
	}


	/**
	 * 方法名：注册命令字的集合，不需要携带指令
	 * 方法描述：使用文件形式注册指令时，不需要在代码中额外增加命令字，只需要提供文件中data，即可收到命令字回调
	 *
	 * @param data 命令字集合名
	 * @return 返回true
	 */
	public boolean regCommandWithNoCmds(String data) {
		AsrUtil.regCmdWithNoCmds(data, mCommCallBack);
		return true;
	}

	/**
	 * 方法名：注册命令字集合名和集合，不需要携带指令
	 * 方法描述：使用文件形式注册指令时，不需要在代码中额外增加命令字，只需要提供文件中data，即可收到命令字回调
	 *
	 * @param data 集合名的集合
	 * @return 返回true
	 */
	public boolean regCommandWithNoCmds(Set<String> data) {
		AsrUtil.regCmdWithNoCmds(data, mCommCallBack);
		return true;
	}

	/**
	 * 方法名：注销命令字集合，不需要携带指令
	 * 方法描述：通过集合名data注销命令字
	 *
	 * @param data 命令字集合名
	 * @return 返回true
	 */
	public boolean unregCmdWithNoCmds(String data) {
		AsrUtil.unregCmdWithNoCmds(data);
		return true;
	}

	/**
	 * 方法名：通过集合注销命令字集合，不需要携带指令
	 * 方法描述：通过集合名的集合data注销命令字
	 *
	 * @param data 命令字集合名的集合
	 * @return 返回true
	 */
	public boolean unregCmdWithNoCmds(Set<String> data) {
		AsrUtil.unregCmdWithNoCmds(data);
		return true;
	}


	/**
	 * 方法名：注销命令字
	 * 方法描述：根据传入的命令字集合来注销命令字
	 *
	 * @param cmds 注销的命令字集合
	 * @return 格式是否正确
	 */
	public boolean unregCommand(Collection<String> cmds) {
		if (cmds == null) {

			return false;
		}

		int count = 0;
		count = cmds.size();

		if (count <= 0) {
			return false;
		}

		String[] cmdArray = new String[count];
		cmds.toArray(cmdArray);

		AsrUtil.unregCmd(cmdArray);
		return true;
	}

	/**
	 * 方法名：注销命令字
	 * 方法描述：注销单个命令字
	 *
	 * @param cmd 注销的命令字
	 * @return 返回true
	 */
	public boolean unregCommand(String cmd) {
		AsrUtil.unregCmd(new String[] { cmd });
		return true;
	}

	Boolean mClose;

	/**
	 * 方法名：设置命令字回调后是否需要关闭语音界面
	 * 方法描述：默认情况下，命令字回调提供给SDK后，无特定操作时，会关闭语音界面
	 *
	 * @param isClose 是否需要关闭，默认关闭
	 */
	public void setCloseWinWhenEndCmd(boolean isClose) {
		mClose = isClose;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.config.end.close", (isClose + "").getBytes(), null);
	}

	/**
	 * 接口名：命令字识别回调监听器
	 * 接中描述：命令字识别回调监听器，注册命令字时需要实现此接口，根据返回的数据来做相应处理
	 */
	public static interface CommandListener {
		/**
		 * 方法名：接口识别回调
		 *
		 * @param cmd  识别到的命令文本
		 * @param data 识别的命令所属集合名
		 */
		public void onCommand(String cmd, String data);
	}

	/**
	 * 方法名：添加命令字识别回调监听器
	 * 方法描述：注册命令字时，传入自定义的命令字识别回调监听器，用来接收命令字回调
	 *
	 * @param listener 监听器对象
	 */
	public void addCommandListener(CommandListener listener) {
		ServiceManager.getInstance().runOnServiceThread(
				new Runnable1<CommandListener>(listener) {
					@Override
					public void run() {
						mCommandListeners.add(mP1);
					}
				}, 0);
	}

	/**
	 * 方法名：删除命令字识别回调监听器
	 * 方法描述：删掉添加的命令字识别回调监听器
	 *
	 * @param listener 监听器对象
	 */
	public void removeCommandListener(CommandListener listener) {
		ServiceManager.getInstance().runOnServiceThread(
				new Runnable1<CommandListener>(listener) {
					@Override
					public void run() {
						mCommandListeners.remove(mP1);
					}
				}, 0);
	}

	/**
	 * 类名：免唤醒识别任务类
	 * 类描述：免唤醒识别任务类，在注册免唤醒词时，需要实现此逻辑
	 * 		   免唤醒识别任务可以注册免唤醒指令，即不需要打开语音界面就可以直接识别，一般不推荐超过10个
	 */
	public static abstract class AsrComplexSelectCallback extends
			AsrUtil.AsrComplexSelectCallback {
		/**
		 * 方法名：添加免唤醒识别命令
		 * 方法描述：添加免唤醒识别命令，支持免唤醒功能
		 *
		 * @param type 识别到后的回调类型数据
		 * @param cmds 唤醒识别命令字
		 */
		public AsrComplexSelectCallback addCommand(String type, String... cmds) {
			super.addCommand(type, cmds);
			return this;
		}

		/**
		 * 方法名：添加索引命令
		 * 方法描述：添加索引命令，请重写onIndexSelected回调进行处理
		 *
		 * @param index 识别到后的回调索引
		 * @param cmds  索引对应的唤醒识别命令字
		 */
		public AsrComplexSelectCallback addIndex(int index, String... cmds) {
			super.addIndex(index, cmds);
			return this;
		}

		/**
		 * 方法名：免唤醒命令选择回调
		 * 方法描述：已添加的免唤醒指令识别到时，回调此方法
		 *
		 * @param type    识别到后的回调类型数据
		 * @param command 唤醒识别的命令字
		 */
		@Override
		public void onCommandSelected(String type, String command) {
			super.onCommandSelected(type, command);
		}

		/**
		 * 方法名:索引选择回调
		 * 方法描述：和addIndex配合使用，使用addIndex注册的免唤醒指令命中时，会回调此方法
		 *
		 * @param indexs  识别到后的满足的索引
		 * @param command 唤醒识别的命令字
		 */
		@Override
		public void onIndexSelected(List<Integer> indexs, String command) {
			super.onIndexSelected(indexs, command);
		}

		/**
		 * 方法名：获取当前免唤醒识别任务ID
		 * 方法描述：返回一个自定义的任务ID，不可为null或空字符串。将在useWakeupAsAsr和recoverWakeupFromAsr时使用。
		 *
		 * @return 返回任务id
		 */
		@Override
		public abstract String getTaskId();


		/**
		 * 方法名：是否需要识别状态
		 * 方法描述：设置为true则在整个唤醒识别任务生效的状态都会将系统静音，默认不需要
		 *
		 * @return 是否需要识别状态
		 */
		@Override
		public abstract boolean needAsrState();
	}

	/**
	 * 方法名：注册免唤醒识别任务
	 * 方法描述：注册自定义的免唤醒任务
	 *
	 * @param callback 识别回调，可以使用AsrComplexSelectCallback
	 */
	public void useWakeupAsAsr(AsrComplexSelectCallback callback) {
		AsrUtil.useWakeupAsAsr(callback);
	}

	/**
	 * 方法名：注销免唤醒识别任务
	 * 方法描述：通过任务ID注销自定义的免唤醒任务
	 *
	 * @param taskId 任务ID
	 */
	public void recoverWakeupFromAsr(String taskId) {
		AsrUtil.recoverWakeupFromAsr(taskId);
	}

	/**
	 * 类名：识别选项参数
	 * 类描述：识别选项类，定义识别设定相关参数
	 */
	public static class AsrOption {
		Integer mBOS = null;
		Integer mEOS = null;
		Integer mKeySpeechTimeout = null;
		Boolean mManual = null;

		/**
		 * 方法名：设置前端静音超时
		 * 方法描述：设置前端静音超时
		 *
		 * @param bos 时间
		 * @return AsrOption本实例
		 */
		public AsrOption setBOS(int bos) {
			mBOS = bos;
			return this;
		}

		/**
		 * 方法名：设置后端静音超时
		 * 方法描述：设置后端静音超时
		 *
		 * @param eos 时间
		 * @return AsrOption本实例
		 */
		public AsrOption setEOS(int eos) {
			mEOS = eos;
			return this;
		}

		/**
		 * 方法名：设置最大语音时长
		 * 方法描述：设置语音识别时录音的最大时长，即录音最久时间，不推荐修改
		 *
		 * @param keySpeechTimeout 最大时间
		 * @return AsrOption本实例
		 */
		public AsrOption setKeySpeechTimeout(int keySpeechTimeout) {
			mKeySpeechTimeout = keySpeechTimeout;
			return this;
		}

		/**
		 * 方法名：设置手动标志
		 * 方法描述：设置手动标志
		 * 
		 * @param manual 是否启用标志
		 * @return AsrOption本实例
		 */
		public AsrOption setManual(boolean manual) {
			mManual = manual;
			return this;
		}
	}

	/**
	 * 接口名：识别回调
	 * 接口描述：自定义识别工具各种状态的回调
	 */
	public static interface AsrCallback {
		/**
		 * 方法名：启动发生异常
		 * 方法描述：启用异常时回调此方法
		 */
		public void onAbort();

		/**
		 * 方法名：录音开始
		 * 方法描述：录音开始时回调此方法
		 */
		public void onBeginRecord();

		/**
		 * 方法名：音量通知
		 * 方法描述：识别音量变化时回调此方法
		 *
		 * @param volume 当前识别到的音量值
		 */
		public void onVolume(int volume);

		/**
		 * 方法名：识别过程中检测到说话
		 * 方法描述：识别过程中检测到说话时回调此方法
		 */
		public void onBeginSpeech();

		/**
		 * 方法名：识别过程中检测到结束说话
		 * 方法描述：识别过程中检测到结束说话时回调此方法
		 */
		public void onEndSpeech();

		/**
		 * 方法名：结束录音，进入识别
		 * 方法描述：结束录音，进入识别时回调此方法
		 */
		public void onEndRecord();

		/**
		 * 方法名：取消识别
		 * 方法描述：取消识别时回调此方法
		 */
		public void onCancel();

		/**
		 * 方法名：识别的场景结果
		 * 方法描述：识别完毕时，回调此方法，提供场景类型和数据
		 *
		 * @param sceneType 场景类型
		 * @param sceneData 数据
		 */
		public void onSceneResult(TXZSceneManager.SceneType sceneType,
				String sceneData);

		/**
		 * 方法名：识别文本结果
		 * 方法描述：识别完毕时，回调此方法，提供识别文本
		 *
		 * @param text 识别文本
		 */
		public void onTextResult(String text);

		/**
		 * 方法名：识别出错
		 * 方法描述：识别出错时回调此方法
		 *
		 * @param errCode 错误码
		 * @param errDesc 错误描述
		 * @param errHint 错误语音提示
		 */
		public void onError(int errCode, String errDesc, String errHint);
	}

	/**
	 * 接口名：识别工具
	 * 接口描述：自定义的识别工具，支持使用第三方的识别引擎
	 */
	public static interface AsrTool {
		/**
		 * 方法名：启动识别
		 * 方法描述：使用自定义第三方引擎时，回调此方法启动识别
		 *
		 * @param option   识别选项
		 * @param callback 识别回调
		 */
		public void start(AsrOption option, AsrCallback callback);

		/**
		 * 方法名：停止录音，开始识别
		 * 方法描述：使用自定义第三方引擎时，回调此方法通知停止识别
		 */
		public void stop();

		/**
		 * 方法名：取消识别
		 * 方法描述：使用自定义第三方引擎时，回调此方法通知取消识别
		 */
		public void cancel();
	}

	boolean mHasAsrTool = false;
	AsrTool mAsrTool = null;

	/**
	 * 方法名：设置第三方识别工具
	 * 方法描述：设置第三方识别工具，设置为null则使用同行者。
	 * 			设置后识别的开始、结束、退出等状态都会通知识别工具，由识别工具完成录音、识别，
	 * 			完成相应操作后回调AsrCallback
	 *
	 * @param asrTool 识别工具
	 */
	public void setAsrTool(AsrTool asrTool) {
		mHasAsrTool = true;
		mAsrTool = asrTool;
		if (mAsrTool == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"comm.asr.clearAsrTool", null, null);
			TXZService.setCommandProcessor("tool.asr.", null);
		} else {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"comm.asr.setAsrTool", null, null);
			TXZService.setCommandProcessor("tool.asr.", new CommandProcessor() {
				@Override
				public byte[] process(String packageName, String command,
						final byte[] data) {
					if ("stop".equals(command)) {
						LogUtil.logd("asr tool stop");
						mAsrTool.stop();
						return null;
					}
					if ("cancel".equals(command)) {
						LogUtil.logd("asr tool cancel");
						mAsrTool.cancel();
						return null;
					}
					if ("start".equals(command)) {
						final JSONBuilder json = new JSONBuilder(data);
						AsrOption option = new AsrOption();
						option.mBOS = json.getVal("BOS", Integer.class);
						option.mEOS = json.getVal("EOS", Integer.class);
						option.mKeySpeechTimeout = json.getVal(
								"KeySpeechTimeout", Integer.class);
						option.mManual = json.getVal("Manual", Boolean.class);

						LogUtil.logd("asr tool start: " + option.mManual);

						mAsrTool.start(option, new AsrCallback() {
							@Override
							public void onVolume(int volume) {
								json.put("volume", volume);
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.asr.onVolume",
										json.toBytes(), null);
							}

							@Override
							public void onSceneResult(SceneType sceneType,
									String sceneData) {
								LogUtil.logd("asr tool onSenceResult: SenceType="
										+ sceneType.name()
										+ ", data: \n"
										+ sceneData);
								json.remove("volume");
								json.put("data", sceneData);
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.asr.onSenceResult",
										json.toBytes(), null);
							}

							@Override
							public void onEndSpeech() {
								LogUtil.logd("asr tool onEndSpeech");
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.asr.onEndSpeech", data, null);
							}

							@Override
							public void onEndRecord() {
								LogUtil.logd("asr tool onEndRecord");
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.asr.onEndRecord", data, null);
							}

							@Override
							public void onCancel() {
								LogUtil.logd("asr tool onCancel");
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.asr.onCancel", data, null);
							}

							@Override
							public void onBeginSpeech() {
								LogUtil.logd("asr tool onBeginSpeech");
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.asr.onBeginSpeech", data,
										null);
							}

							@Override
							public void onBeginRecord() {
								LogUtil.logd("asr tool onBeginRecord");
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.asr.onBeginRecord", data,
										null);
							}

							@Override
							public void onAbort() {
								LogUtil.logd("asr tool onAbort");
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.asr.onAbort", data, null);
							}

							@Override
							public void onError(int errCode, String errDesc,
									String errHint) {
								LogUtil.logd("asr tool onError: errCode="
										+ errCode + ", errDesc=" + errDesc);
								json.remove("volume");
								json.put("errCode", errCode);
								json.put("errDesc", errDesc);
								json.put("errHint", errHint);
								ServiceManager.getInstance().sendInvoke(
										ServiceManager.TXZ,
										"txz.tool.asr.onError", json.toBytes(),
										null);
							}

							@Override
							public void onTextResult(String text) {
								JSONBuilder json = new JSONBuilder();
								json.put("scene", "_raw_online");
								json.put("text", text);
								this.onSceneResult(SceneType.SCENE_TYPE_UNKNOW,
										json.toString());
							}
						});
						return null;
					}
					return null;
				}
			});
		}
	}

	/**
	 * 方法名：设置通过PCM文件识别
	 * 方法描述：设置一个PCM文件的路径，下次启动识别将会识别此文件的内容。
	 * 			不推荐使用，一般用于测试保存的PCM文件是否能正常识别。
	 */
	public void setAsrPcmFile(String path) {
		JSONBuilder json = new JSONBuilder();
		json.put("audioSourcePath", path);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"comm.asr.set.rawaudio", json.toBytes(), null);
	}

	AsrListener mAsrListener;
	/**
	 * 方法名：设置识别生命周期监听
	 * 方法描述：目前只error时回调，后续可增加
	 */
	public void setAsrListener(AsrListener asrListener){
		mAsrListener = asrListener;
		if (mAsrListener == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"comm.asr.clearAsrListener", null, null);
			TXZService.setCommandProcessor("listener.asr.", null);
		} else {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"comm.asr.setAsrListener", null, null);
			TXZService.setCommandProcessor("listener.asr.", new CommandProcessor(){
				@Override
				public byte[] process(String packageName, String command, byte[] data) {
					if ("onError".equals(command)) {
						final JSONBuilder json = new JSONBuilder(data);
						int errorType = json.getVal("errorType", Integer.class);
						String msg = json.getVal("msg", String.class);
						mAsrListener.onError(errorType, msg);
					}
					return null;
				}
			});
		}
	}

	/**
	 * 识别回调接口
	 *
	 * @author ASUS User
	 *
	 */
	public abstract static class IAsrCallback {
		public abstract void onSuccess(String data);

		public abstract void onError(int error);

		public abstract void onAbort(int error);

		public abstract void onCancel();

		/**
		 * 开始录音
		 */
		public abstract void onStart();

		/**
		 * 录音结束，开始使用在线或离线识别
		 */
		public abstract void onEnd();
		
	}

	public abstract static class AsrListener{
		public abstract void onError(int error, String msg);
	}

}
