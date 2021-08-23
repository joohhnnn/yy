package com.txznet.comm.ui.util;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.comm.R;
import com.txznet.txz.util.TXZFileConfigUtil;

import android.app.Dialog;
import android.app.Service;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;

import java.util.HashMap;

public class ScreenUtil {
	private static final String TAG = "ScreenUtil ";
	private static int sScreenWidthChat;
	public static int mListViewRectHeight;
	public static int mListViewRectWidth;
	public static int sWinChatRectHeight;

	static int sItemHeight;
	static int sVisibleCount;
	static int sListViewHeight;
	static int sLvheight;
	
	static View sConView;
	
	static int sY64;
	static int sY66;
	static int sY80;
	static int sY100;
	static int sY110;
	static int mRealItemHeight;
	static int mRealItemCount;
	// 用户配置的item高度和显示数量
	
	static int mConfigItemCount;
	static Integer mSetItemCount = null;
	static Integer mSetWinHeight = null;
	static Integer mSetWinWidth = null;
	static Integer mSetItemHeight = null;
	static Integer mThemeType = null;
	static Boolean mSetAutoItemHeight = null;

	static {
		if (GlobalContext.isTXZ()) {
			sY64 = (int) GlobalContext.get().getResources().getDimension(R.dimen.y64);
			sY66 = (int) GlobalContext.get().getResources().getDimension(R.dimen.y66);
			sY80 = (int) GlobalContext.get().getResources().getDimension(R.dimen.y80);
			sY100 = (int) GlobalContext.get().getResources().getDimension(R.dimen.y100);
			sY110 = (int) GlobalContext.get().getResources().getDimension(R.dimen.y110);
		} else {
			sY64 = (int) LayouUtil.getDimen("y64");
			sY66 = (int) LayouUtil.getDimen("y66");
			sY80 = (int) LayouUtil.getDimen("y80");
			sY100 = (int) LayouUtil.getDimen("y100");
			sY110 = (int) LayouUtil.getDimen("y110");
		}
	}

	// 展示列表用，禁用
	private static int sScreenWidthDisplay;

	public static boolean isLargeScreen(Dialog dialog) {
		Rect outRect = new Rect();
		dialog.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);// 应用界面
		if (outRect.width() >= 1200) {
			return true;
		}
		return false;
	}

	public static boolean isLargeScreen(View view) {
		Rect outRect = new Rect();
		view.getWindowVisibleDisplayFrame(outRect);// 应用界面
		System.out.println("top:" + outRect.top + " ; left: " + outRect.left);
		if (outRect.width() >= 1200) {
			return true;
		}
		return false;
	}
	
	
	//适配方案，小屏，长屏，车机
	public static final int SCREEN_TYPE_LITTLE = 1;
	public static final int SCREEN_TYPE_NORMAL = 2;
	public static final int SCREEN_TYPE_LARGE = 3;
	public static final int SCREEN_TYPE_CAR = 4;
	static int mScreenType = SCREEN_TYPE_LARGE;
	static boolean isAutoItemHeight = true;
	static int mMaxItemHeight_h = 120;
	static int mMaxItemHeight_v = 180;
	//布局方式，使用横向布局或者竖向布局
	public static final int LAYOUT_TYPE_HORIZONTAL = 1;
	public static final int LAYOUT_TYPE_VERTICAL = 2;
	static int mLayoutType = LAYOUT_TYPE_HORIZONTAL;

	private static void beginInit(){
		mWinHeight = 0;
		mWinWidth = 0;
		sY64 = (int) LayouUtil.getDimen("y64");
		sY66 = (int) LayouUtil.getDimen("y66");
		sY80 = (int) LayouUtil.getDimen("y80");
		sY100 = (int) LayouUtil.getDimen("y100");
		sY110 = (int) LayouUtil.getDimen("y110");
		mMaxItemHeight_h = (int) LayouUtil.getDimen("y120");
		mMaxItemHeight_v = (int) LayouUtil.getDimen("y180");
		sCinemaItemCount = 4;
	}

	/**
	 * 1. 宽且高<480;(单独适配)<br>
	 * 
	 * 2. 宽>=790且宽高比>1或者宽高比大于1.64 使用横布局（声控占宽度的25%）<br>
	 * 
	 * 3. 当2成立； <br>
	 * 480<高度可用空间>=400 使用小屏适配规范； （列表时占满高度）<br>
	 * 600<高度可用空间>=480 使用长屏适配规范；（列表时占满高度） <br>
	 * 720<高度可用空间>=600 使用车机适配规范；（列表时上下居中） <br>
	 * 高度可用空间=720 使用大屏车机适配规范； （列表时上下居中）<br>
	 * 
	 * 4. 800 < 可用宽度 > 480,或宽高比=<1.64; 使用竖布局； 使用长屏后视镜规范；<br>
	 * 
	 * @param view 初始化布局使用参数
	 * @return
	 */
	public static void initScreenType(View view) {
		beginInit();
		Rect outRect = new Rect();
		view.getWindowVisibleDisplayFrame(outRect);// 应用界面
		
		int width = outRect.width();
		int height = outRect.height();

		HashMap<String, String> cfg = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_SCREEN_WIDTH, TXZFileConfigUtil.KEY_SCREEN_HEIGHT);
		String strWidth = cfg.get(TXZFileConfigUtil.KEY_SCREEN_WIDTH);
		String strHeight = cfg.get(TXZFileConfigUtil.KEY_SCREEN_HEIGHT);
		if (!TextUtils.isEmpty(strWidth) && !TextUtils.isEmpty(strHeight)) {
			try {
				int iWidth = Integer.parseInt(strWidth);
				int iHeight = Integer.parseInt(strHeight);
				if (iWidth > 0 && iHeight > 0) {
					mSetWinWidth = iWidth;
					mSetWinHeight = iHeight;
					LogUtil.d("load width : " + iWidth + " ; height : " + iHeight);
				}
			}catch (Exception e) {
				LogUtil.e("parse width/height error");
			}
		}

		if (mSetWinHeight != null) {
			height = mSetWinHeight;
		}

		if (mSetWinWidth != null) {
			width = mSetWinWidth;
		}

		float mAspectRatio = (float) width / (float) height;
		if (width < 480 && height < 480) {
			mScreenType = SCREEN_TYPE_LITTLE;
			mRealItemHeight = sY80;
			mRealItemCount = 4;
			isAutoItemHeight = true;
		} else if ((width >= 790 && mAspectRatio > 1) || mAspectRatio > 1.64f) {
			if (height >= 400 && height < 480) {
				mScreenType = SCREEN_TYPE_LITTLE;
				mRealItemHeight = sY80;
				isAutoItemHeight = true;
			} else if (height >= 480 && height < 600) {
				mScreenType = SCREEN_TYPE_LARGE;
				mRealItemHeight = sY80;
				isAutoItemHeight = true;
			} else if (height >= 600) {
				mScreenType = SCREEN_TYPE_CAR;
				mRealItemHeight = sY80;
				isAutoItemHeight = false;
			}else {
				mRealItemHeight = sY80;
				isAutoItemHeight = true;
			}
			mLayoutType = LAYOUT_TYPE_HORIZONTAL;
			mRealItemCount = 4;
		} else if ((width >= 480 && width < 800) || mAspectRatio <= 1.64f) {
			mScreenType = SCREEN_TYPE_LARGE;
			mRealItemHeight = sY80;
			mLayoutType = LAYOUT_TYPE_VERTICAL;
			mRealItemCount = 4;
			isAutoItemHeight = true;
			sCinemaItemCount = 3;
			if (width >1024) {
				mScreenType = SCREEN_TYPE_LARGE;
			} else if (width > 790) {
				mScreenType = SCREEN_TYPE_LITTLE;
			} else if (width > 480) {
				mScreenType = SCREEN_TYPE_LITTLE;
			} else {
				mScreenType = SCREEN_TYPE_CAR;
			}
			if (height >= 1024) {
				mRealItemHeight = sY100;
				isAutoItemHeight = false;
			}else if (height >= 800) {
				mRealItemHeight = sY80;
				isAutoItemHeight = false;
			}

		}else {
			mLayoutType = LAYOUT_TYPE_HORIZONTAL;
			mRealItemHeight = sY80;
			mRealItemCount = 4;
			isAutoItemHeight = true;
		}
		 LogUtil.logd("mRealItemHeight:"+mRealItemHeight);
		 LogUtil.logd(" bottom: " + outRect.bottom + " ; right: " +
		 outRect.right + " ; mScreenType:" + mScreenType
		 + " ; mListViewRectHeight:" + mListViewRectHeight + " ；sVisibleCount:" + sVisibleCount);
		if (mSetItemCount != null) {
			mRealItemCount = mSetItemCount;
		}
		if (mSetAutoItemHeight != null) {
			isAutoItemHeight = mSetAutoItemHeight;
		}

		checkListHeight(height);

		if (mLayoutType == LAYOUT_TYPE_VERTICAL) {
			if (mSetItemCount == null) {
				if (mListViewRectHeight <= 720){
					mRealItemCount = 4;
				}else if (mListViewRectHeight <= 900){
					mRealItemCount = 5;
				}else if (mListViewRectHeight <= 1080){
					mRealItemCount = 6;
				}else if (mListViewRectHeight <= 1260){
					mRealItemCount = 7;
				}else {
					mRealItemCount = 8;
				}
			}
		}
		checkListWidth(width);
		getDisplayLvItemH(true);

	}
	
	/**
	 * 强制使用竖直布局
	 * @param view
	 */
	public static void initVerticalScreenType(View view) {
		beginInit();

		Rect outRect = new Rect();
		view.getWindowVisibleDisplayFrame(outRect);// 应用界面

		int width = outRect.width();
		int height = outRect.height();
		LogUtil.logd("width:"+width+",height:"+height);
		HashMap<String, String> cfg = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_SCREEN_WIDTH, TXZFileConfigUtil.KEY_SCREEN_HEIGHT);
		String strWidth = cfg.get(TXZFileConfigUtil.KEY_SCREEN_WIDTH);
		String strHeight = cfg.get(TXZFileConfigUtil.KEY_SCREEN_HEIGHT);
		if (!TextUtils.isEmpty(strWidth) && !TextUtils.isEmpty(strHeight)) {
			try {
				int iWidth = Integer.parseInt(strWidth);
				int iHeight = Integer.parseInt(strHeight);
				if (iWidth > 0 && iHeight > 0) {
					width = iWidth;
					height = iHeight;
					LogUtil.d("load width : " + iWidth + " ; height : " + iHeight);
				}
			}catch (Exception e) {
				LogUtil.e("parse width/height error");
			}
		}

		float mAspectRatio = (float) width / (float) height;
		mLayoutType = LAYOUT_TYPE_VERTICAL;
		sCinemaItemCount = 3;
		if (width < 480 && height < 480) {
			mScreenType =  SCREEN_TYPE_LITTLE;
			mRealItemHeight = sY80;
			mRealItemCount = 4;
			isAutoItemHeight = true;
		} else if ((width >= 790 && mAspectRatio > 1) || mAspectRatio > 1.64f) {
			if (height >= 400 && height < 480) {
				mScreenType =  SCREEN_TYPE_LITTLE;
				mRealItemHeight = sY80;
				isAutoItemHeight = true;
			} else if (height >= 480 && height < 600) {
				mScreenType =  SCREEN_TYPE_LARGE;
				mRealItemHeight = sY80;
				isAutoItemHeight = true;
			} else if (height >= 600) {
				mScreenType =  SCREEN_TYPE_CAR;
				mRealItemHeight = sY80;
				isAutoItemHeight = false;
			}else {
				mRealItemHeight = sY80;
				isAutoItemHeight = true;
			}
			mRealItemCount = 4;
		} else if ((width >= 480 && width < 800) || mAspectRatio <= 1.64f) {
			mScreenType =  SCREEN_TYPE_LARGE;
			mRealItemHeight = sY80;
			mRealItemCount = 4;
			isAutoItemHeight = true;
			sCinemaItemCount = 3;
			if (width >1024) {
				mScreenType = SCREEN_TYPE_LARGE;
			} else if (width > 790) {
				mScreenType = SCREEN_TYPE_LITTLE;
			} else if (width > 480) {
				mScreenType = SCREEN_TYPE_LITTLE;
			} else {
				mScreenType = SCREEN_TYPE_CAR;
			}
			if (height >= 1024) {
				mRealItemHeight = sY100;
				isAutoItemHeight = false;
			}else if (height >= 800) {
				mRealItemHeight = sY80;
				isAutoItemHeight = false;
			}
		}else {
			mRealItemHeight = sY80;
			isAutoItemHeight = true;
		}
		if (mSetItemCount != null) {
			mRealItemCount = mSetItemCount;
		}

		if (mSetAutoItemHeight != null) {
			isAutoItemHeight = mSetAutoItemHeight;
		}
		
		
		LogUtil.logd("mRealItemHeight:"+mRealItemHeight);
		 LogUtil.logd(" bottom: " + outRect.bottom + " ; right: " +
		 outRect.right + " ; mScreenType:" + mScreenType
		 + " ; mListViewRectHeight:" + mListViewRectHeight + " ；sVisibleCount:" + sVisibleCount);
		
		
		checkListHeight(height);

		if (mLayoutType == LAYOUT_TYPE_VERTICAL) {
			if (mSetItemCount == null) {
				if (mListViewRectHeight <= 720){
					mRealItemCount = 4;
				}else if (mListViewRectHeight <= 900){
					mRealItemCount = 5;
				}else if (mListViewRectHeight <= 1080){
					mRealItemCount = 6;
				}else if (mListViewRectHeight <= 1260){
					mRealItemCount = 7;
				}else {
					mRealItemCount = 8;
				}
			}
		}

		checkListWidth(width);
		getDisplayLvItemH(true);
		
		
	}
	
	
	/**
	 * 强制使用横向布局
	 * @param view
	 */
	public static void initHorizontalScreenType(View view) {
		beginInit();
		
		Rect outRect = new Rect();
		view.getWindowVisibleDisplayFrame(outRect);// 应用界面

		int width = outRect.width();
		int height = outRect.height();
		LogUtil.logd("width:"+width+",height:"+height);
		HashMap<String, String> cfg = TXZFileConfigUtil.getConfig(TXZFileConfigUtil.KEY_SCREEN_WIDTH, TXZFileConfigUtil.KEY_SCREEN_HEIGHT);
		String strWidth = cfg.get(TXZFileConfigUtil.KEY_SCREEN_WIDTH);
		String strHeight = cfg.get(TXZFileConfigUtil.KEY_SCREEN_HEIGHT);
		if (!TextUtils.isEmpty(strWidth) && !TextUtils.isEmpty(strHeight)) {
			try {
				int iWidth = Integer.parseInt(strWidth);
				int iHeight = Integer.parseInt(strHeight);
				if (iWidth > 0 && iHeight > 0) {
					width = iWidth;
					height = iHeight;
					LogUtil.d("load width : " + iWidth + " ; height : " + iHeight);
				}
			}catch (Exception e) {
				LogUtil.e("parse width/height error");
			}
		}
		float mAspectRatio = (float) width / (float) height;
		mLayoutType = LAYOUT_TYPE_HORIZONTAL;
		if (width < 480 && height < 480) {
			mScreenType =  SCREEN_TYPE_LITTLE;
			mRealItemHeight = sY80;
			mRealItemCount = 4;
			isAutoItemHeight = true;
		} else if ((width >= 790 && mAspectRatio > 1) || mAspectRatio > 1.64f) {
			if (height >= 400 && height < 480) {
				mScreenType =  SCREEN_TYPE_LITTLE;
				mRealItemHeight = sY80;
				isAutoItemHeight = true;
			} else if (height >= 480 && height < 600) {
				mScreenType =  SCREEN_TYPE_LARGE;
				mRealItemHeight = sY80;
				isAutoItemHeight = true;
			} else if (height >= 600) {
				mScreenType =  SCREEN_TYPE_CAR;
				mRealItemHeight = sY80;
				isAutoItemHeight = false;
			}else {
				mRealItemHeight = sY80;
				isAutoItemHeight = true;
			}
			mRealItemCount = 4;
		} else if ((width >= 480 && width < 800) || mAspectRatio <= 1.64f) {
			mScreenType =  SCREEN_TYPE_LARGE;
			mRealItemHeight = sY80;
			mRealItemCount = 4;
			isAutoItemHeight = false;
		}else {
			mRealItemHeight = sY80;
			isAutoItemHeight = true;
		}
		if (mSetItemCount != null) {
			mRealItemCount = mSetItemCount;
		}

		if (mSetAutoItemHeight != null) {
			isAutoItemHeight = mSetAutoItemHeight;
		}
		
		LogUtil.logd("mRealItemHeight:"+mRealItemHeight);
		 LogUtil.logd(" bottom: " + outRect.bottom + " ; right: " +
		 outRect.right + " ; mScreenType:" + mScreenType
		 + " ; mListViewRectHeight:" + mListViewRectHeight + " ；sVisibleCount:" + sVisibleCount);
		
		checkListHeight(height);
		checkListWidth(width);
		getDisplayLvItemH(true);
	}
	
	/**
	 * 是否是竖屏的设备
	 * @return
	 */
	public static boolean isVerticalDevice() {
		return mListViewRectHeight > mListViewRectWidth;
	}
	
	/**
	 * @return 获取大屏，车机和小屏
	 */
	public static int getScreenType(){
		return mScreenType;
	}
	
	/**
	 * @return 获取横向还是竖向布局
	 */
	public static int getLayoutType() {
		return mLayoutType;
	}

	public static boolean checkScreenSizeChangeForDisplay() {
		WindowManager wm = (WindowManager) AppLogicBase.getApp().getSystemService(Service.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		if (display != null) {
			Point outSize = new Point();
			display.getSize(outSize);
			LogUtil.logd("outSize:" + outSize.toString() + ",ScreenWidth:" + sScreenWidthDisplay);
			if (outSize.x != sScreenWidthDisplay) {
				sScreenWidthDisplay = outSize.x;
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 检测列表可显示高度发生改变
	 * @return
	 */
	public static boolean isDialogHeightChange(){
		if(sConView != null){
			int height = sConView.getHeight();
			if(height != sLvheight){
				sLvheight = height;
				return true;
			}
		}
		return false;
	}

	/*
	 * 其它禁用
	 */
	public static boolean checkScreenSizeChangeForChat() {
		WindowManager wm = (WindowManager) AppLogicBase.getApp().getSystemService(Service.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		if (display != null) {
			Point outSize = new Point();
			display.getSize(outSize);
			LogUtil.logd("outSize:" + outSize.toString() + ",ScreenWidth:" + sScreenWidthChat);
			if (outSize.x != sScreenWidthChat) {
				sScreenWidthChat = outSize.x;
				return true;
			}
		}
		return false;
	}

	/**
	 * 获取屏幕宽度
	 * 
	 * @return
	 */
	public static int getScreenWidth() {
		WindowManager wm = (WindowManager) AppLogicBase.getApp().getSystemService(Service.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point outSize = new Point();
		if (display != null) {
			display.getSize(outSize);
			LogUtil.logd("outSize:" + outSize.toString());
		}
		return outSize.x;
	}/**
	 * 获取屏幕宽度
	 *
	 * @return
	 */
	public static int getScreenHeight() {
		WindowManager wm = (WindowManager) AppLogicBase.getApp().getSystemService(Service.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point outSize = new Point();
		if (display != null) {
			display.getSize(outSize);
			LogUtil.logd("outSize:" + outSize.toString());
		}
		return outSize.y;
	}

	static int mWinHeight;
	static int mWinWidth;
	public static void checkViewRect(final View contentView) {
		contentView.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@SuppressWarnings("deprecation")
			@Override
			public void onGlobalLayout() {
				contentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				int tmpH = contentView.getHeight();
				int tmpW = contentView.getWidth();
				ScreenUtils.updateScreenSize(contentView.getWidth(), contentView.getHeight(), false);
				if (tmpH != mWinHeight && mSetWinHeight == null) {
					if (tmpH > 0) {
						mWinHeight = tmpH;
						checkListHeight(mWinHeight);
						getDisplayLvItemH(true);
					}
				}
				if (tmpW != mWinWidth && mSetWinWidth == null) {
					if (tmpW > 0) {
						mWinWidth = tmpW;
						checkListWidth(mWinWidth);
					}
				}
			}
		});
	}
	
	public static int mContentWeight = 3;
	public static int mRecordWeight = 1;
	
    private static void checkListWidth(int conWidth) {
		LogUtil.logd(TAG + " checkListWidth start:" + mListViewRectWidth + ",conHeight:" + conWidth);
    	if (conWidth <= 0) {
            return;
        }
		switch (getLayoutType()) {
		case LAYOUT_TYPE_VERTICAL:
			mListViewRectWidth = (int) (conWidth - LayouUtil.getDimen("x30"));
			break;
		case LAYOUT_TYPE_HORIZONTAL:
		default:
			mListViewRectWidth = (int) (mContentWeight * conWidth / (mContentWeight + mRecordWeight)
					- LayouUtil.getDimen("x30"));
			break;
		}
		LogUtil.logd(TAG + " checkListWidth end:" + mListViewRectWidth);
    }
	

	private static void checkListHeight(int conHeight) {
		if (conHeight <= 0) {
			return;
		}

		switch (ScreenUtil.getLayoutType()) {
		case ScreenUtil.LAYOUT_TYPE_VERTICAL:
			if (mThemeType != null) {
				switch (mThemeType) {
				case ConfigUtil.THEME_TYPE_SIRI:
				case ConfigUtil.THEME_TYPE_WAVE:
					//竖屏默认的声控占了110(@_@;)
					sWinChatRectHeight = conHeight - sY110;
					mListViewRectHeight = conHeight - sY64 - sY110;
					break;
				case ConfigUtil.THEME_TYPE_IRONMAN:
					sWinChatRectHeight = conHeight - sY100 - sY80;
					mListViewRectHeight = conHeight - sY64 - sY100 - sY80;
					break;
				default:
					break;
				}
			} else {
				//竖屏默认的声控占了110(@_@;)
				sWinChatRectHeight = conHeight - sY110;
				mListViewRectHeight = conHeight - sY64 - sY110;
			}
			break;
		case ScreenUtil.LAYOUT_TYPE_HORIZONTAL:
			if (mThemeType != null) {
				switch (mThemeType) {
				case ConfigUtil.THEME_TYPE_SIRI:
				case ConfigUtil.THEME_TYPE_WAVE:
					sWinChatRectHeight = conHeight;
					mListViewRectHeight = conHeight - sY64;			
					break;
				case ConfigUtil.THEME_TYPE_IRONMAN:
					switch (mScreenType) {
					case SCREEN_TYPE_LITTLE:
						sWinChatRectHeight = conHeight;
						mListViewRectHeight = conHeight - sY64 - sY100;			
						break;
					default:
						sWinChatRectHeight = conHeight;
						mListViewRectHeight = conHeight - sY64 - sY80;
						break;
					}
					break;
				default:
					break;
				}
			}else {
				sWinChatRectHeight = conHeight;
				mListViewRectHeight = conHeight - sY64;
			}
			break;
		default:
			break;
		}
	}

	
	public static synchronized int getDisplayLvItemH(boolean needReset) {
		if (mSetItemHeight != null) {
			return mSetItemHeight;
		}
		if (!needReset) {
			if (sHook != null) {
				// 如果代码设置了item的高度，则使用item的高度绘制view的高度
					int height = sHook.getItemHeight();
				if (height > 0) {
					return height;
				}
			}
		}
		
		if (sItemHeight > 0 && !needReset) {
			return sItemHeight;
		}
		//横向布局和竖向布局的最大高度，在皮肤包中没有指定高度的时候是不同的
		if (mLayoutType == LAYOUT_TYPE_HORIZONTAL) {
			if (mRealItemHeight > mMaxItemHeight_h) {
				mRealItemHeight = mMaxItemHeight_h;
			}
		} else {
			if (mRealItemHeight > mMaxItemHeight_v) {
				mRealItemHeight = mMaxItemHeight_v;
			}
		}


		if (mListViewRectHeight > 0) {
			if (mRealItemHeight == 0) {
				mRealItemHeight = 1;
			}
			int count = mListViewRectHeight / mRealItemHeight;
			if (count > mRealItemCount) {
				count = mRealItemCount;
				if (isAutoItemHeight) {
					sItemHeight = (int) Math.ceil(mListViewRectHeight / count);
				} else {
					sItemHeight = mRealItemHeight;
				}
			} else {
				if (count == 0) {
					if (sItemHeight > 0) {
						return sItemHeight;
					}else {
						sItemHeight = 1;
					}
				}
				sItemHeight = (int) Math.ceil(mListViewRectHeight / count);
			}
			sVisibleCount = count;
			
			sListViewHeight = sVisibleCount * sItemHeight;
			LogUtil.logd("RectHeight:" + mListViewRectHeight + ",ItemH:" + sItemHeight);
		}

		// ！！设置显示列表数量
		if (sVisibleCount > 0) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.display.count",
					(sVisibleCount + "").getBytes(), null);
		}

		return sItemHeight;
	}
	
	static int sCinemaItemCount = 4;
	static Integer sSetCinemaItemCount = null;
	
	/**
	 * @return 获取电影条目显示的个数，默认4个
	 */
	public static int getCinemaItemCount() {
		return sSetCinemaItemCount == null? sCinemaItemCount : sSetCinemaItemCount;
	}
	

	public static synchronized int getVisbileCount() {
		if (sHook != null && sHook.getVisibleCount() > 0) {
			return sHook.getVisibleCount();
		}
		return sVisibleCount;
	}
	
	public static class ListOptionHook<T> {
		// 返回这个值，将使用默认的计算高度
		public static final int INVALIDATE_HEIGHT = -1;
		
		public T option;

		public int getItemHeight() {
			return INVALIDATE_HEIGHT;
		}

		public int getVisibleCount() {
			return sVisibleCount;
		}
	}
	
	private static ListOptionHook sHook;
	
	public static synchronized void setHook(ListOptionHook hook) {
		LogUtil.logd("screenUtil setHook:" + hook);
		sHook = hook;
	}
}