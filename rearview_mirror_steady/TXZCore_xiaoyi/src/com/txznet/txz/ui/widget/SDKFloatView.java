package com.txznet.txz.ui.widget;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.sp.CommonSp;
import com.txznet.comm.ui.dialog.WinConfirm;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.launch.LaunchManager;
import com.txznet.txz.util.ImageUtil;

public class SDKFloatView extends LinearLayout {
	private WindowManager mWinManager;
	private WindowManager.LayoutParams mLp;
	private boolean mIsOpening;
	private int mWidth, mHeight;
	private int historyX = -1, historyY = -1;
	private ImageView mVoiceAssistant;
	private View mTestFlagView;
	private int mTouchSlop;
	private int mRootWidth;
	private int mRootHeight;

	private static SDKFloatView mSDKFloatView;

	private SDKFloatView(final Context context) {
		super(context);
		mWinManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		initView(context);
	}

	public static SDKFloatView getInstance() {
		if(mSDKFloatView == null){
			mSDKFloatView = new SDKFloatView(GlobalContext.get());
		}
		return mSDKFloatView;
	}

	private String mTestFlag = null;

	public void showTestFlag(final String text) {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				JNIHelper.logd("connect env from " + mTestFlag + " to " + text);
				if (TextUtils.isEmpty(text) != TextUtils.isEmpty(mTestFlag)
						|| (text != null && !(TextUtils.isEmpty(mTestFlag)) && !text
								.equals(mTestFlag))) {
					new WinConfirm(true) {
						@Override
						public void onClickOk() {
							AppLogic.restartProcess();
						}
					}.setMessage("连接的环境切换到: " + text + "\n是否需要重启？").show();
				}
				mTestFlag = text;
				if (mTestFlagView != null) {
					if (TextUtils.isEmpty(mTestFlag)) {
						mTestFlagView.setVisibility(View.GONE);
					} else {
						((TextView) mTestFlagView).setText(mTestFlag);
						mTestFlagView.setVisibility(View.VISIBLE);
					}
				}
			}
		}, 0);
	}

	public void setImageBitmap(final String normal, final String pressed) {
		if (mVoiceAssistant == null) {
			return;
		}
		mVoiceAssistant.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						getViewTreeObserver().removeOnPreDrawListener(this);
						try {
							if (normal == null || pressed == null) {
								mVoiceAssistant
										.setImageResource(R.drawable.widget_voice_assistant);
								return false;
							}
							File fNormal = new File(normal);
							File fPressed = new File(pressed);
							if (!fNormal.exists() || fNormal.length() == 0) {
								mVoiceAssistant
										.setImageResource(R.drawable.widget_voice_assistant);
								JNIHelper
										.loge("SDKFloatView setCustomIcon failed, fNormal not found");
								return false;
							}
							BitmapFactory.Options options = new BitmapFactory.Options();
							options.outWidth = 100;
							options.outHeight = 100;
							if (fPressed != null && fPressed.length() != 0) {
								StateListDrawable drawable = new StateListDrawable();
								int sPressed = android.R.attr.state_pressed;
								Bitmap bPressed = ImageUtil
										.decodeSampledBitmapFromResource(
												pressed,
												mVoiceAssistant.getWidth(),
												mVoiceAssistant.getHeight());
								Drawable dPressed = new BitmapDrawable(bPressed);
								drawable.addState(new int[] { sPressed },
										dPressed);
								Bitmap bNormal = ImageUtil
										.decodeSampledBitmapFromResource(
												normal,
												mVoiceAssistant.getWidth(),
												mVoiceAssistant.getHeight());
								Drawable dNormal = new BitmapDrawable(bNormal);
								drawable.addState(new int[] {}, dNormal);
								mVoiceAssistant.setImageDrawable(drawable);
							} else {
								Bitmap bNormal = ImageUtil
										.decodeSampledBitmapFromResource(
												normal,
												mVoiceAssistant.getWidth(),
												mVoiceAssistant.getHeight());
								Drawable dNormal = new BitmapDrawable(bNormal);
								mVoiceAssistant.setImageDrawable(dNormal);
								JNIHelper
										.loge("SDKFloatView setCustomIcon fPressed not found");
							}
						} catch (Exception e) {
							JNIHelper
									.loge("SDKFloatView setCustomIcon failed, cause "
											+ e.getClass()
											+ "::"
											+ e.getMessage());
						}
						return false;
					}
				});
		mVoiceAssistant.invalidate();
	}

	private Rect mCurVisiableRect = new Rect();
	private boolean mAutoAdjust;

	public void enableAutoAdjust() {
		mAutoAdjust = true;
	}

	public void disableAutoAdjust() {
		mAutoAdjust = false;
	}

	private void initView(Context context) {
		LayoutInflater.from(context).inflate(R.layout.win_sdk_float_view, this);
		View root = findViewById(R.id.llSDK_Float_View_Root);
		mVoiceAssistant = (ImageView) root
				.findViewById(R.id.imgSDK_Float_View_Voice_Assistant);
		mTestFlagView = root.findViewById(R.id.txtSDK_Float_View_TesfFlag);
		mWidth = root.getLayoutParams().width;
		mHeight = root.getLayoutParams().height;
		showTestFlag(mTestFlag);
		enableAutoAdjust();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		JNIHelper.logd("SDKFloatView onAttachedToWindow");
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				JNIHelper.logd("SDKFloatView onPreDraw");
				getViewTreeObserver().removeOnPreDrawListener(this);
				try {
					if (!mMeasureReady) {
						mMeasureReady = true;
						mRootWidth = getRootView().getWidth();
						mRootHeight = getRootView().getHeight();
					}
					mLp.width = mWidth;
					mLp.height = mHeight;

					int cacheX = PointCache.getInstance(getContext()).getX(
							mRootWidth - mWidth);
					int cacheY = PointCache.getInstance(getContext()).getY(
							(mRootHeight - mHeight) / 2);
					// 越界保护
					if (cacheX < 0) {
						cacheX = 0;
					} else if (cacheX > mRootWidth - mWidth) {
						cacheX = mRootWidth - mWidth;
					}
					if (cacheY < 0) {
						cacheY = 0;
					} else if (cacheY > mRootHeight - mHeight) {
						cacheY = mRootHeight - mHeight;
					}
					mLp.x = cacheX;
					mLp.y = cacheY;
					mWinManager.updateViewLayout(SDKFloatView.this, mLp);
					historyX = mLp.x;
					historyY = mLp.y;
					PointCache.getInstance(getContext()).setX(historyX);
					PointCache.getInstance(getContext()).setY(historyY);
					JNIHelper.logd("SDKFloatView onPreDraw finish x=" + mLp.x
							+ ", y=" + mLp.y + ", w=" + mLp.width + ",h = "
							+ mLp.height);
				} catch (Exception e) {
					JNIHelper
							.loge("SDKFloatView onAttachedToWindow[onPreDraw] error, desc="
									+ e.getClass()
									+ "::"
									+ e.getMessage()
									+ ", isOpening=" + mIsOpening);
				}
				return false;
			}
		});
	}

	public void open() {
		if (mIsOpening) {
			return;
		}
		if (!mMeasureReady) {
			mLp = new WindowManager.LayoutParams();
			mLp.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 10;
			mLp.width = WindowManager.LayoutParams.MATCH_PARENT;
			mLp.height = WindowManager.LayoutParams.MATCH_PARENT;
			mLp.flags = 40;
			mLp.format = PixelFormat.RGBA_8888;
			mLp.gravity = Gravity.LEFT | Gravity.TOP;
			historyX = 0;
			historyY = 0;
		}
		mLp.x = historyX;
		mLp.y = historyY;
		mWinManager.addView(this, mLp);
		mIsOpening = true;
		JNIHelper.logd("SDKFloatView OPEN x=" + mLp.x + ", y=" + mLp.y + ", w="
				+ mLp.width + ",h = " + mLp.height);
	}

	private boolean mMeasureReady = false; // 第一次打开时先测量出可用区域

	public void close() {
		if (mIsOpening) {
			mWinManager.removeView(this);
			mIsOpening = false;
		}
	}

	private float downX, downY;
	private int lastLpX, lastLpY;
	private boolean shouldMove;
	private long lastLaunch = 0;
	private static long CLICK_INTERVAL_LIMIT = -1;
	
	public void setClickInteval(long interval) {
		CLICK_INTERVAL_LIMIT = interval;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			setPressed(true);
			downX = event.getRawX();
			downY = event.getRawY();
			lastLpX = mLp.x;
			lastLpY = mLp.y;
			if (mAutoAdjust) {
				getWindowVisibleDisplayFrame(mCurVisiableRect);
				mRootWidth = mCurVisiableRect.width();
				mRootHeight = mCurVisiableRect.height();
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(event.getRawX() - downX) >= mTouchSlop
					|| Math.abs(event.getRawY() - downY) >= mTouchSlop) {
				shouldMove = true;
			}
			if (shouldMove) {
				mLp.x = (int) (lastLpX + event.getRawX() - downX);
				mLp.y = (int) (lastLpY + event.getRawY() - downY);

				if (mLp.x < 0) {
					mLp.x = 0;
				} else if (mLp.x > mRootWidth - mWidth) {
					mLp.x = mRootWidth - mWidth;
				}
				if (mLp.y < 0) {
					mLp.y = 0;
				} else if (mLp.y > mRootHeight - mHeight) {
					mLp.y = mRootHeight - mHeight;
				}
				mWinManager.updateViewLayout(this, mLp);
				historyX = mLp.x;
				historyY = mLp.y;
			}
			return true;
		case MotionEvent.ACTION_UP:
			setPressed(false);
			if (!shouldMove) {
				long now = System.currentTimeMillis();
				if(now - lastLaunch < CLICK_INTERVAL_LIMIT){
					return true;
				}
				playSoundEffect(SoundEffectConstants.CLICK);
				JNIHelper.logd("SDKFloatView doLaunch");
				LaunchManager.getInstance().launchWithRecord();
				lastLaunch = now;
			}
			shouldMove = false;
			// 复位贴边
			if (mLp.x < mWidth / 2) {
				mLp.x = 0;
			} else if (mLp.x > mRootWidth - mWidth - mWidth / 2) {
				mLp.x = mRootWidth - mWidth;
			}
			if (mLp.y < mHeight / 2) {
				mLp.y = 0;
			} else if (mLp.y > mRootHeight - mHeight - mHeight / 2) {
				mLp.y = mRootHeight - mHeight;
			}
			mWinManager.updateViewLayout(this, mLp);
			historyX = mLp.x;
			historyY = mLp.y;
			PointCache.getInstance(getContext()).setX(historyX);
			PointCache.getInstance(getContext()).setY(historyY);
			JNIHelper.logd("SDKFloatView SET x=" + mLp.x + ", y=" + mLp.y
					+ ", w=" + mLp.width + ",h = " + mLp.height);
			return true;
		}
		return super.onTouchEvent(event);
	}

	private static class PointCache extends CommonSp {
		private static final String SP_NAME = "float_view_point_cache";
		private static PointCache sInstance;

		protected PointCache(Context context) {
			super(context, SP_NAME);
		}

		public static PointCache getInstance(Context context) {
			if (sInstance == null) {
				synchronized (PointCache.class) {
					if (sInstance == null) {
						sInstance = new PointCache(context);
					}
				}
			}
			return sInstance;
		}

		private static final String KEY_X = "x";
		private static final String KEY_Y = "y";

		public int getX(int defVal) {
			return getValue(KEY_X, defVal);
		}

		public void setX(int x) {
			setValue(KEY_X, x);
		}

		public int getY(int defVal) {
			return getValue(KEY_Y, defVal);
		}

		public void setY(int y) {
			setValue(KEY_Y, y);
		}
	}
}
