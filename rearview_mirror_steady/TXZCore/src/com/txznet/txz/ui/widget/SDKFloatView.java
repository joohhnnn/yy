package com.txznet.txz.ui.widget;

import android.os.Looper;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.ThemeStyle;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.util.TXZFileConfigUtil;

public class SDKFloatView {

	private static SDKFloatView sInstance = new SDKFloatView();

	private int mFloatViewType = -1;// 0:FLOAT_TOP 1:FLOAT_NORMAL 2:FLOAT_NONE
	public static final int TYPE_FLOAT_TOP = 0;
	public static final int TYPE_FLOAT_NORMAL = 1;
	public static final int TYPE_FLOAT_NONE = 2;
	private ISDKFloatView mFloatView;
	private Object mLock = new Object();

	// 需保存的配置
	private String mTestText = null;
	private String mImageNormal = null;
	private String mImagePressed = null;
	private Boolean mEnableAutoAjust = null;
	private Integer mPositionX = null;
	private Integer mPositionY = null;
	private Long mClickInteval = null;
	private Integer mWinType = null;
	public Boolean mDismiss = null;
	private Integer mFloatToolWidth = null;
	private Integer mFloatToolHeight = null;
    private boolean mEnableFloatView = true;

	private SDKFloatView() {
        mEnableFloatView = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_ENABLE_FLOAT_VIEW_TYPE,true);
	}

	public static SDKFloatView getInstance() {
		return sInstance;
	}

	
	public void newViewInstance(){
		if (mFloatView != null){
		    mFloatView.close();
        }
		mFloatView = null;
		postInitFloatView(null);
	}

	public int beforeState;

	public void recordState(final boolean ifShow) {

		/*
		 * 默认情况下， 启动语音界面会隐藏图标 ， 用户可以配置不隐藏图标
		 * 但是在熟手模式下，启动语音界面的时候必须隐藏语音图标，否则会盖住录音
		 *
		 */
		if (TXZFileConfigUtil.getBooleanSingleConfig(
				TXZFileConfigUtil.KEY_ENABLE_HIDE_FLOAT_ON_RECORD, true) || 
				(ThemeConfigManager.getInstance().getStyle() != null) && 
				(ThemeConfigManager.getInstance().getStyle().getModel() != null) &&
				(ThemeConfigManager.getInstance().getStyle().getModel().getModel() == ThemeStyle.STYLE_MODEL_1)) {

			if (ifShow) {   // 启动界面的时候
//				beforeState = mFloatViewType;
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						setFloatToolType(TYPE_FLOAT_NONE , false);
					}
				});
			} else {  // 关闭界面的时候
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						setFloatToolType(beforeState , false);
					}
				});
			}

		}
		
		LogUtil.logd("SDKFloatView recordState:" + beforeState);
	}


	public void setFloatToolType(int type , boolean whenNoneNeedSetNull) {
        if (!mEnableFloatView) {
            LogUtil.logw("floatView is disable");
            return;
        }
		TXZPowerControl.notifyFloatViewTypeUpdate(type);
		LogUtil.logd("SDKFloatView setFloatToolType:" + type);
		synchronized (mLock) {
			mFloatViewType = type;
			switch (type) {
			case TYPE_FLOAT_TOP:
			case TYPE_FLOAT_NORMAL:
					if (TXZPowerControl.isEnterReverse()) {
						LogUtil.logd("can't setFloatToolType:" + type + " , because TXZPowerControl EnterReverse");
						return;
					}
                    if (needReInit()) {
                        postInitFloatView(new Runnable() {
							@Override
							public void run() {
								mFloatView.open();
							}
						});
                    }else {
						mFloatView.open();
					}
				break;
			case TYPE_FLOAT_NONE:
				if (mFloatView == null) {
					return;
				}
				mFloatView.close();
				if(whenNoneNeedSetNull) {
					mFloatView = null;
				}
				break;
			default:
				break;
			}
		}
	}

	private boolean needReInit(){
		return (mFloatView == null)
				||(ConfigUtil.isCustomFloatView() && mFloatView instanceof SDKFloatViewInner);
	}

	public void showTestFlag(final String text) {
        if (!mEnableFloatView) {
            LogUtil.logw("floatView is disable");
            return;
        }
		mTestText = text;
		if (mFloatViewType == TYPE_FLOAT_NONE) {
			return;
		}
		postInitFloatView(new Runnable() {
			@Override
			public void run() {
				mFloatView.showTestFlag(text);
			}
		});
	}

	public void init() {
		LogUtil.logd("SDKFloatView init:" + mFloatViewType);
		synchronized (mLock) {
			if (mFloatViewType == TYPE_FLOAT_NONE) {
				return;
			}
			postInitFloatView(new Runnable() {
				@Override
				public void run() {
					mFloatView.open();
				}
			});
		}
	}

	private void postInitFloatView(final Runnable endRun){
		if (Looper.myLooper() == Looper.getMainLooper()) {
			initFloatView();
			if (endRun != null) {
				endRun.run();
			}
		} else {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					initFloatView();
					if (endRun != null) {
						endRun.run();
					}
				}
			});
		}
	}

	private void initFloatView() {
		synchronized (mLock) {
			if (mFloatView == null) {
				if (ConfigUtil.isCustomFloatView()) {
					mFloatView = new SDKFloatViewInnerV2(GlobalContext.get());
					LogUtil.logd("create SDKFloatViewInnerV2");
				} else {
					mFloatView = new SDKFloatViewInner(GlobalContext.get());
					LogUtil.logd("create SDKFloatViewInnerV1");
				}
			}
			//TODO 	理论上是不会走到的，所有操作声控图标的都移到ui初始化完成之后了，但是现在出现了
			else if (ConfigUtil.isCustomFloatView() && mFloatView instanceof SDKFloatViewInner) {
				mFloatView.close();
				mFloatView = new SDKFloatViewInnerV2(GlobalContext.get());
				LogUtil.logd("create SDKFloatViewInnerV2");
			}else {
				return;
			}

			if (mTestText != null) {
				mFloatView.showTestFlag(mTestText);
			}
			if (mImageNormal != null || mImagePressed != null) {
				mFloatView.setImageBitmap(mImageNormal, mImagePressed);
			}
			if (mPositionX != null && mPositionY != null) {
				mFloatView.setFloatViewPosition(mPositionX, mPositionY);
			}
			if (mEnableAutoAjust != null) {
				if (mEnableAutoAjust) {
					mFloatView.enableAutoAdjust();
				} else {
					mFloatView.disableAutoAdjust();
				}
			}
			if (mClickInteval != null) {
				mFloatView.setClickInteval(mClickInteval);
			}
			if (mDismiss != null) {
				mFloatView.setDismiss(mDismiss);
			}
			if (mFloatToolWidth != null && mFloatToolHeight != null) {
				mFloatView.setFloatToolSize(mFloatToolWidth, mFloatToolHeight);
			}
			if (mWinType != null) {
				mFloatView.setWinType(mWinType);
			}
		}
	}

	/**
	 * 设置悬浮图标悬浮窗层级
	 * 
	 * @param type
	 */
	public void setWinType(int type) {
        if (!mEnableFloatView) {
            LogUtil.logw("floatView is disable");
            return;
        }
		LogUtil.logd("setWinType :" + type);
		mWinType = type;
		if (mFloatViewType == TYPE_FLOAT_NONE) {
			return;
		}
		postInitFloatView(new Runnable() {
			@Override
			public void run() {
				if (mWinType != null) {
					mFloatView.setWinType(mWinType);
				}
			}
		});
	}
	
	public void setImageBitmap(final String normal, final String pressed) {
        if (!mEnableFloatView) {
            LogUtil.logw("floatView is disable");
            return;
        }
		JNIHelper.logd("setImageBitmap normal:" + normal + ",pressed:" + pressed);
		mImageNormal = normal;
		mImagePressed = pressed;
		if (mFloatViewType == TYPE_FLOAT_NONE) {
			return;
		}
		postInitFloatView(new Runnable() {
			@Override
			public void run() {
				if (mImageNormal != null || mImagePressed != null) {
					mFloatView.setImageBitmap(mImageNormal, mImagePressed);
				}
			}
		});
	}

	public void enableAutoAdjust() {
        if (!mEnableFloatView) {
            LogUtil.logw("floatView is disable");
            return;
        }
		mEnableAutoAjust = true;
		if (mFloatViewType == TYPE_FLOAT_NONE) {
			return;
		}
		postInitFloatView(new Runnable() {
			@Override
			public void run() {
				if (mEnableAutoAjust != null) {
					if (mEnableAutoAjust) {
						mFloatView.enableAutoAdjust();
					} else {
						mFloatView.disableAutoAdjust();
					}
				}
			}
		});
	}

	public void disableAutoAdjust() {
        if (!mEnableFloatView) {
            LogUtil.logw("floatView is disable");
            return;
        }
		mEnableAutoAjust = false;
		if (mFloatViewType == TYPE_FLOAT_NONE) {
			return;
		}
		postInitFloatView(new Runnable() {
			@Override
			public void run() {
				if (mEnableAutoAjust != null) {
					if (mEnableAutoAjust) {
						mFloatView.enableAutoAdjust();
					} else {
						mFloatView.disableAutoAdjust();
					}
				}
			}
		});
	}

	public void setFloatViewPosition(int x, int y) {
        if (!mEnableFloatView) {
            LogUtil.logw("floatView is disable");
            return;
        }
		mPositionX = x;
		mPositionY = y;
		if (mFloatViewType == TYPE_FLOAT_NONE) {
			return;
		}
		postInitFloatView(new Runnable() {
			@Override
			public void run() {
				if (mPositionX != null && mPositionY != null) {
					mFloatView.setFloatViewPosition(mPositionX, mPositionY);
				}
			}
		});
	}

	public void open() {
        if (!mEnableFloatView) {
            LogUtil.logw("floatView is disable");
            return;
        }
		LogUtil.logd("SDKFloatView open");
		if (mFloatViewType == TYPE_FLOAT_NONE) {
			return;
		}
		if (needReInit()) {
			postInitFloatView(new Runnable() {
				@Override
				public void run() {
					mFloatView.open();
				}
			});
		}else {
			mFloatView.open();
		}
	}

	public void close() {
        if (!mEnableFloatView) {
            LogUtil.logw("floatView is disable");
            return;
        }
		if (mFloatView == null) {
			return;
		}
		mFloatView.close();
	}

	public void setClickInteval(long interval) {
        if (!mEnableFloatView) {
            LogUtil.logw("floatView is disable");
            return;
        }
		mClickInteval = interval;
		if (mFloatViewType == TYPE_FLOAT_NONE) {
			return;
		}
		postInitFloatView(new Runnable() {
			@Override
			public void run() {
				if (mClickInteval != null) {
					mFloatView.setClickInteval(mClickInteval);
				}
			}
		});
	}

	public void setFloatToolSize(int floatToolWidth, int floatToolHeight) {
        if (!mEnableFloatView) {
            LogUtil.logw("floatView is disable");
            return;
        }
		mFloatToolWidth = floatToolWidth;
		mFloatToolHeight = floatToolHeight;
		if (mFloatViewType == TYPE_FLOAT_NONE) {
			return;
		}
		postInitFloatView(new Runnable() {
			@Override
			public void run() {
				if (mFloatToolWidth != null && mFloatToolHeight != null) {
					mFloatView.setFloatToolSize(mFloatToolWidth, mFloatToolHeight);
				}
			}
		});
	}

	public int getFloatViewType() {
		return mFloatViewType;
	}
    public boolean isEnableFloatView() {
        return mEnableFloatView;
    }

    public void setEnableFloatView(final boolean enableFloatView) {
        mEnableFloatView = enableFloatView;
    }
}
