package com.txznet.wakeup.module;

import com.txznet.wakeup.component.wakeup.IWakeup;

public class WakeupManager implements IWakeup {
	private static WakeupManager sInstance = null;
	private IWakeup mWakeup = null;
	private String strWakeupImpl = "com.txznet.wakeup.component.wakeup.yunzhisheng.WakeupImpl";//"com.txznet.wakeup.component.wakeup.iflytek.WakeupImpl";

	public static WakeupManager getInstance() {
		if (sInstance == null) {
			synchronized (WakeupManager.class) {
				if (sInstance == null) {
					sInstance = new WakeupManager();
				}
			}
		}
		return sInstance;
	}
    
	private void creatWakeup() {
		if (mWakeup == null) {
			synchronized (strWakeupImpl) {
				if (mWakeup == null) {
					try {
						mWakeup = (IWakeup) Class.forName(strWakeupImpl)
								.newInstance();
					} catch (Exception e) {
					}
				}
			}
		}
	}
	
	@Override
	public int initialize(String[] cmds, IInitCallback oRun) {
        creatWakeup();
		if (mWakeup != null){
			return mWakeup.initialize(cmds, oRun);
		}
		
		return -1;
	}

	@Override
	public int start(IWakeupCallback oCallback) {
		creatWakeup();
		if (mWakeup == null) {
			return -1;
		}
		return mWakeup.start(oCallback);
	}

	@Override
	public int startWithRecord(IWakeupCallback oCallback,
			String savePathPrefix, String[] overTag) {
		if (mWakeup == null) {
			return -1;
		}
		return mWakeup.startWithRecord(oCallback, savePathPrefix, overTag);
	}

	@Override
	public void stop() {
		creatWakeup();
		if (mWakeup == null) {
			return;
		}
		mWakeup.stop();
	}

	@Override
	public void stopWithRecord() {
		if (mWakeup == null) {
			return;
		}
		mWakeup.stopWithRecord();
	}

	@Override
	public void setWakeupKeywords(String[] keywords) {
		if (mWakeup == null) {
			return;
		}
		mWakeup.setWakeupKeywords(keywords);
	}

	@Override
	public void setWakeupThreshold(float val) {
		if (mWakeup == null) {
			return;
		}
		mWakeup.setWakeupThreshold(val);
	}

}
