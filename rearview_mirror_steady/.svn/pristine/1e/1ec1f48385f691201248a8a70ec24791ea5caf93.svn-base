package com.txznet.txz.module.volume;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import android.content.Context;
import android.media.AudioManager;

import com.txznet.comm.remote.GlobalContext;

public class AudioServiceAdapter implements InvocationHandler {

	private static Object mSysInterface = null;

	private AudioServiceAdapter() {
	}

	public static void adapter() {
		if (mSysInterface == null) {
			AudioManager mAudioManager = (AudioManager) GlobalContext.get()
					.getSystemService(Context.AUDIO_SERVICE);
			mAudioManager.getMode(); // 调用一次赋值sService
			try {
				Field f = AudioManager.class.getDeclaredField("sService");
				f.setAccessible(true);
				mSysInterface = f.get(AudioManager.class);
				if (null != mSysInterface) {
					Object mine = Proxy.newProxyInstance(mSysInterface
							.getClass().getClassLoader(), mSysInterface
							.getClass().getInterfaces(),
							new AudioServiceAdapter());
					if (mine != null) {
						f.set(AudioManager.class, mine);
					}
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
	}

	static boolean mEnableVolumeControl = false;

	/**
	 * 临时允许音量修改
	 */
	public static void enableVolumeControlTemp(boolean b) {
		mEnableVolumeControl = b;
	}

	static boolean mEnableFocusControl = false;

	/**
	 * 临时允许音量修改
	 */
	public static void enableFocusControlTemp(boolean b) {
		mEnableFocusControl = b;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {

//		if ("requestAudioFocus".equals(method.getName())) {
//			if (!mEnableFocusControl) {
//				Log.d(".........","====================disable audio focus");
//				Throwable t = new Throwable();
//				t.printStackTrace();
//				return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
//			}
//		}

		// if (mEnableVolumeControl == false // 不允许音量控制
		// && !(method.getName().startsWith("get") // 非get类接口
		// || method.getName().startsWith("is")) // 非is类接口
		// ) {
		// String as = "call [" + method.getName() + "] args: ";
		// for (int i = 0; i < args.length; ++i) {
		// as = as + args[i] + ",";
		// }
		// JNIHelper.logd(as);
		// if (method.getName().equals("setStreamVolume")
		// || method.getName().equals("adjustVolume")
		// || method.getName().equals("adjustStreamVolume")
		// || method.getName().equals("adjustSuggestedStreamVolume")) {
		// if (method.getReturnType() == void.class)
		// return null;
		// try {
		// return method.getReturnType().newInstance();
		// } catch (Exception e) {
		// }
		// }
		// }

		Object result = method.invoke(mSysInterface, args);

		return result;
	}
}
