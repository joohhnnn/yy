package com.txznet.music.utils;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Process;

import com.txz.ui.audio.UiAudio.Resp_DataInterface;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.dao.LocalAudioDBHelper;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.bean.response.Audio;

public class DataInterfaceBroadcastHelper {
	private static int mSeq = new Random().nextInt();

	private static int getNextSeq() {
		++mSeq;
		if (mSeq == 0) {
			++mSeq;
		}
		return mSeq;
	}

	static Map<Integer, String> mMapSeqs = new ConcurrentHashMap<Integer, String>();
	static Map<String, RemoteNetListener> mMapListeners = new ConcurrentHashMap<String, RemoteNetListener>();

	public static void initListeners() {
		if (AppLogic.isMainProcess()) {
			initRandomKey();

			IntentFilter intentFilter = new IntentFilter(
					"com.txznet.music.action.ReqDataInterface");
			GlobalContext.get().registerReceiver(new BroadcastReceiver() {

				@Override
				public void onReceive(Context context, Intent intent) {
					String cmdString = intent.getStringExtra("cmd");
					int seqReq = NetHelp.sendRequestByData(cmdString,
							decodeByRandomKey(intent.getByteArrayExtra("data")));
					mMapSeqs.clear();
					mMapSeqs.put(seqReq, intent.getStringExtra("seq"));
				}
			}, intentFilter);
			IntentFilter downlaodFilter = new IntentFilter(
					"com.txznet.music.action.DownloadComplete");
			GlobalContext.get().registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					// XXX:改成内存数据
					try {
						Audio audio = JsonHelper.toObject(
								Audio.class,
								new String(decodeByRandomKey(intent
										.getByteArrayExtra("data"))));
						audio.setSid(0);
						audio.setDownloadType("0");
						audio.setPinyin(PinYinUtil.getPinYin(audio.getName()));
						LogUtil.logd("[Audio]Download complete save to database:" + audio.toString());
						LocalAudioDBHelper.getInstance().saveOrUpdate(audio);
					} catch (Exception e) {
						LogUtil.logw("downloadComplete error,"
								+ decodeByRandomKey(intent
										.getByteArrayExtra("data")));
					}
				}
			}, downlaodFilter);
		}else{
			IntentFilter intentFilter = new IntentFilter(
					"com.txznet.music.action.RespDataInterface");
			GlobalContext.get().registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String seqString = intent.getStringExtra("seq");
					RemoteNetListener remoteNetListener = mMapListeners.remove(seqString);
					if (remoteNetListener != null) {
						String  data=new String(decodeByRandomKey(intent.getByteArrayExtra("data")));
						remoteNetListener
						.response(intent.getIntExtra("code", 0),data.getBytes());
					} else {
						if (!mMapListeners.isEmpty()) {
							LogUtil.logw("can not find RespDataInterface seq: "
									+ seqString);
						}
					}
				}
			}, intentFilter);
		}

	}

	public static boolean sendDataInterfaceResp(Resp_DataInterface resp) {
		String seqBroadcast = mMapSeqs.remove(resp.uint32Seq);
		if (seqBroadcast == null) {
			if (!mMapSeqs.isEmpty()) {
				LogUtil.logw("can not find DataInterface seq: "
						+ resp.uint32Seq);
			}
			return false;
		}

		Intent intent = new Intent();
		intent.putExtra("data", encodeByRandomKey(resp.strData));
		intent.putExtra("cmd", resp.strCmd);
		intent.putExtra("seq", seqBroadcast);
		intent.putExtra("code", resp.uint32ErrCode);
		intent.setAction("com.txznet.music.action.RespDataInterface");
		GlobalContext.get().sendBroadcast(intent);
		return true;
	}

	public static void sendDataInterfaceReq(String cmd, byte[] reqData,
			RemoteNetListener listener) {
		Intent intent = new Intent();
		if (Constant.ISTESTDATA) {
			LogUtil.logd("request pressurl =" + new String(reqData));
		} else {
			LogUtil.logd("sendDataInterfaceReq:" + cmd);
		}
		intent.putExtra("data", encodeByRandomKey(reqData));
		intent.putExtra("cmd", cmd);
		String seq = AppLogic.getProcessName() + "_" + getNextSeq();
		mMapListeners.put(seq, listener);
		intent.putExtra("seq", seq);
		intent.setAction("com.txznet.music.action.ReqDataInterface");
		GlobalContext.get().sendBroadcast(intent);
	}

	public static void sendDownloadBroadcast(Audio tempAudio) {
		Intent intent = new Intent();
		intent.putExtra("data", encodeByRandomKey(JsonHelper.toJson(tempAudio)
				.getBytes()));
		intent.setAction("com.txznet.music.action.DownloadComplete");
		GlobalContext.get().sendBroadcast(intent);
	}

	public static interface RemoteNetListener {
		public void response(int code, byte[] data);
	}

	// ///////////////////////////////////////////////////////////////////////////

	private final static int mChaosKey = 79;
	private static byte[] mRandomKey = null;

	private static byte[] encodeRandomKey(int pid, byte[] key) {
		if (null == key) {
			return null;
		}
		byte[] ret = new byte[key.length];
		for (int i = 0; i < ret.length; ++i) {
			ret[i] = (byte) (mChaosKey ^ pid ^ key[i]);
		}
		return ret;
	}

	public static byte[] getRandomKey() {
		if (!AppLogic.isMainProcess()) {
			return null;
		}
		int pid = Process.myPid();
		return encodeRandomKey(pid, mRandomKey);
	}

	public static void setRandomKey(int pid, byte[] key) {
		if (AppLogic.isMainProcess() == false) {
			mRandomKey = encodeRandomKey(pid, key);
		}
	}

	public static void initRandomKey() {
		if (mRandomKey == null && AppLogic.isMainProcess()) {
			mRandomKey = new byte[32 + new Random().nextInt(32)];
			new Random().nextBytes(mRandomKey);
		}
	}

	// 对称编码
	public static byte[] encodeByRandomKey(byte[] data) {
		int dataLength = data.length;
		int randomKeyLength = mRandomKey.length;
		int offset = (dataLength + randomKeyLength) % mChaosKey;
		for (int i = 0; i < data.length; ++i) {
			data[i] ^= mRandomKey[(i + offset) % mRandomKey.length];
		}
		return data;
	}

	public static byte[] decodeByRandomKey(byte[] data) {
		// 可逆算法
		return encodeByRandomKey(data);
	}
}
