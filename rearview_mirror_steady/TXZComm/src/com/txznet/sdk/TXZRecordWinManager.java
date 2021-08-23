package com.txznet.sdk;

import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;

import com.alibaba.fastjson.JSON;
import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.base.CrashCommonHandler.CrashLisener;
import com.txznet.comm.base.CrashCommonHandler.OnCrashListener;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.UI2Manager.UIInitListener;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;
import com.txznet.sdk.TXZService.CommandProcessor;
import com.txznet.txz.util.runnables.Runnable1;

/**
 * 类名：语音窗口配置管理类
 * 类描述：同行者默认语音唤醒显示界面管理类，默认以Dialog实现。通过此类可以配置窗口全屏、大小等配置
 */
public class TXZRecordWinManager {

	private static TXZRecordWinManager sInstance = new TXZRecordWinManager();

	private TXZRecordWinManager() {

	}

	/**
	 * 获取单例
	 * 
	 * @return 类实例
	 */
	public static TXZRecordWinManager getInstance() {
		return sInstance;
	}

	/**
	 * 重连时需要重新通知同行者的操作放这里
	 */
	void onReconnectTXZ() {
		if (mFullScreen != null) {
			enableFullScreen(mFullScreen);
		}
		if (mCloseWin != null) {
			setWinRecordCloseWhenProcCmd(mCloseWin);
		}
		if (mHasSetRecordWin != null) {
			setRecordWin2(mRecordWin);
		}
		if (mWinContentWidth != null) {
			setWinContentWidth(mWinContentWidth);
		}
		if (mEnableMsgEntryAnimation != null) {
			enableMsgEntryAnimation(mEnableMsgEntryAnimation);
		}
	}


	public void setSystemUiVisibility(int flags){
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("flag", flags);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.win.setSystemUiVisibility", jsonBuilder.toBytes(), null);
	}


	/**
	 * 方法名：更新分辨率（用于显示区域大小改变时）
	 * 方法描述：动态更新语音默认界面显示分辨率，可以直接更新窗口大小
	 *
	 * @param widthPx  显示区域宽度，单位:pixels
	 * @param heightPx 显示区域高度，单位:pixels
	 */
	public void updateScreenSize(int widthPx,int heightPx){
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("width", widthPx);
		jsonBuilder.put("height", heightPx);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.win.updateScreenSize", jsonBuilder.toBytes(), null);
	}
	
	
	private Integer mWinContentWidth = null;

	/**
	 * 方法名：设置语音内容显示的宽度
	 * 方法描述：该方法主要是为了解决某些异形屏屏幕不是长方形，边缘区域显示不到的问题。该方法可能会导致屏幕适配问题，谨慎使用
	 *
	 * @param width 显示宽度
	 */
	public void setWinContentWidth(int width) {
		mWinContentWidth = width;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.win.contentWidth",
				("" + mWinContentWidth).getBytes(), null);
	}

	/**
	 * 方法名：声控界面状态获取接口
	 * 方法描述：用于判断当前语音界面是否为开启状态
	 *
	 * @return 界面是否打开
	 */
	public boolean isOpened() {
		byte[] data = ServiceManager.getInstance().sendTXZInvokeSync("txz.record.win.isOpened", null);
		if (data == null)
			return false;
		return Boolean.parseBoolean(new String(data));
	}

	/**
	 * 方法名：打开界面并显示文本
	 * 方法描述：打开界面并显示文本，该接口不会进入到语音正常的识别流程，仅仅只会打开界面并显示一段文字
	 * 			需要进入识别流程请使用{@link TXZAsrManager#startWithRawText(String)}
	 *
	 * @param text 显示文件
	 */
	public void openAndShowText(String text) {
		if (TextUtils.isEmpty(text)) {
			return;
		}
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.win.openShowText", text.getBytes(),
				null);
	}
	
	Boolean mFullScreen;

	/**
	 * 方法名：设置语音聊天界面是否全屏
	 * 方法描述：设置语音聊天界面是否全屏，通过WindowManager.LayoutParams.FLAG_FULLSCREEN设置界面为全屏
	 *
	 * @param isFullScreen 是否需要全屏显示
	 */
	public void enableFullScreen(boolean isFullScreen) {
		mFullScreen = isFullScreen;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.winRecord.fullScreen",
				("" + isFullScreen).getBytes(), null);
	}
	
	Boolean mEnableMsgEntryAnimation;

	/**
	 * 方法名：是否开启消息添加时的进入动画。
	 * 方法描述：是否开启消息添加时的进入动画，即系统交互时各个Item的进入动画，默认开启。
	 * 			性能欠佳的机器在关闭时，可以节省界面首次开启时CPU瞬时占用资源，优化体验
	 *
	 * @param enable 是否
	 */
	public void enableMsgEntryAnimation(boolean enable) {
		mEnableMsgEntryAnimation = enable;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.win.enableMsgEntryAnim",
				("" + mEnableMsgEntryAnimation).getBytes(), null);
	}
	
	
	
	Boolean mCloseWin;
	
	/**
	 * 方法名：设置回调命令字(Command)的时候是否要关闭界面
	 * 方法描述：设置当命令字(Command)使用结束是是否需要关闭界面，默认会关闭
	 *
	 * @param isClose 是否需要关闭
	 */
	public void setWinRecordCloseWhenProcCmd(boolean isClose) {
		mCloseWin = isClose;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.winRecord.close",
				(mCloseWin + "").getBytes(), null);
	}


	/**
	 * 接口名：语音界面实现2.0
	 * 接口描述：语音界面接口类，语音APP版本2.0及以上由此实现语音界面
	 */
	public interface RecordWin2 {
		/**
		 * 方法名：显示文本
		 * 方法描述：语音界面实现方法，需要显示文本数据时，回调此接口
		 *
		 * @param data 需显示的数据，json格式
		 * @return false:会显示默认样式 true:不会显示
		 */
		public boolean showData(String data);

		/**
		 * 方法名：设置语音界面控制工具
		 * 方法描述：设置语音界面界面配置类
		 *
		 * @param controller 界面配置工具
		 */
		public void setWinController(RecordWinController controller);

		/**
		 * 接口名：语音界面配置工具
		 * 接听描述：语音UI2.0 界面配置工具，包含界面详细配置
		 */
		public interface RecordWinController {

			/**
			 * 常量名：目标View显示位置配置，聊天列表
			 * 常量描述：希望View显示到的目标位置,追加聊天列表后
			 */
			public static final int TARGET_CONTENT_CHAT = 10;
			/**
			 * 常量名：目标View显示位置配置，全屏
			 * 常量描述：希望View显示到的目标位置,覆盖全部内容窗口
			 */
			public static final int TARGET_CONTENT_FULL = 20;

			/**
			 * 常量名：目标View显示位置配置，录音动画
			 * 常量描述：希望View显示到的目标位置,覆盖录音动画
			 */
			public static final int TARGET_VIEW_MIC = 30;

			/**
			 * 希望View显示到的目标View,覆盖banner广告
			 */
			public static final int TARGET_VIEW_BANNER_AD = 40;
			
			/**
			 * 方法名：显示声控界面
			 * 方法描述：主动操作显示语音界面
			 */
			public void show();
			/**
			 * 方法名：关闭声控界面
			 * 方法描述：主动操作关闭语音界面
			 */
			public void dismiss();

			/**
			 * 方法名：添加View到声控界面
			 * 方法描述：主动添加View到声控界面，并配置显示位置
			 *
			 * @param targetView 希望添加到的地方
			 *                   TARGET_CONTENT_CHAT:聊天列表后追加
			 *                   TARGET_CONTENT_FULL:全屏显示
			 *                   TARGET_VIEW_MIC:替换录音动画
			 * @param view       希望添加的View
			 * @return 实例
			 */
			public Object addView(int targetView, View view);
			
			// /**
			// * 添加一个View到语音界面的指定位置
			// * @param view 想要添加的View，例如一个Button，一个图标之类的
			// * @param layoutParams
			// * 该View位于语音窗口的LayoutParmas
			// * @return
			// */
			// public Object addViewToWindow(View view,FrameLayout.LayoutParams
			// layoutParams);
			//
			//
			// /**
			// * 更改View的背景图片
			// * @param targetView
			// *
			// * @param drawable
			// */
			// public Object setViewBkgImg(int targetView,Drawable drawable);
			
			
			public static final int OPERATE_CLICK = 0;
			public static final int OPERATE_LONG_CLICK = 1;
			public static final int OPERATE_TOUCH = 2;

			/**
			 * 常量名：触发事件，触摸
			 * 常量描述：通过触摸触发的事件
			 */
			public static final int OPERATE_SOURCE_TOUCH = 1;
			/**
			 * 常量名：触发事件，方控
			 * 常量描述：通过方控触发的事件
			 */
			public static final int OPERATE_SOURCE_NAVCONTROL = 2;
			/**
			 * 常量名：View标记，列表下一页View
			 * 常量描述：需要操作对View时，传递View标记，列表下一页View
			 */
			public static final int VIEW_LIST_NEXTPAGE = 1;
			/**
			 * 常量名：View标记，列表上一页View
			 * 常量描述：需要操作对View时，传递View标记，列表上一页View
			 */
			public static final int VIEW_LIST_PREPAGE = 2;
			/**
			 * 常量名：View标记，列表Item
			 * 常量描述：需要操作对View时，传递View标记，列表Item
			 */
			public static final int VIEW_LIST_ITEM = 3;
			/**
			 * 常量名：View标记，帮助按钮
			 * 常量描述：需要操作对View时，传递View标记，帮助按钮
			 */
			public static final int VIEW_HELP = 10;
			/**
			 * 常量名：View标记，设置按钮
			 * 常量描述：需要操作对View时，传递View标记，设置按钮
			 */
			public static final int VIEW_SETTING = 11;
			/**
			 * 常量名：View标记，录音动画View
			 * 常量描述：需要操作对View时，传递View标记，录音动画View
			 */
			public static final int VIEW_RECORD = 12;
			/**
			 * 常量名：View标记，关闭按钮
			 * 常量描述：需要操作对View时，传递View标记，关闭按钮
			 */
			public static final int VIEW_CLOSE = 13;
			/**
			 * 常量名：View标记，建议按钮
			 * 常量描述：需要操作对View时，传递View标记，建议按钮
			 */
			public static final int VIEW_TIPS = 14;
			/**
			 * 常量名：View标记，聊天二维码
			 * 常量描述：需要操作对View时，传递View标记，聊天二维码
			 */
			public static final int VIEW_TTS_QRCODE = 15;
			/**
			 * 常量名：View标记，返回按钮
			 * 常量描述：需要操作对View时，传递View标记，返回按钮
			 */
			public static final int VIEW_BACK = 16;
			/**
			 * 常量名：View标记，城市选择按钮
			 * 常量描述：需要操作对View时，传递View标记，城市选择按钮
			 */
			public static final int VIEW_CITY = 17;
			/**
			 * 常量名：View标记，帮助页面返回按钮
			 * 常量描述：需要操作对View时，传递View标记，帮助页面返回按钮
			 */
			public static final int VIEW_HELP_BACK = 18;
			/**
			 * 常量名：View标记，电影场景电话号码修改按钮
			 * 常量描述：需要操作对View时，传递View标记，发送修改电话号码事件
			 */
			public static final int VIEW_FILM_REPLACE_PHONE = 29;
			public static final int VIEW_HELP_QRCODE = 30;//帮助界面
			public static final int VIEW_TICKET_INFO_COMMIT = 31;//票务购票信息提交按钮。
			public static final int VIEW_TICKET_INFO_CANCEL = 32;//票务购票信息退票与取消订单按钮。

			/**
			 * 方法名：对指定View进行操作
			 * 方法描述：对View进行操作，例如点击某个View等。
			 *
			 * @param actionType 动作类型,默认OPERATE_CLICK
			 * @param view       动作的对象，参考常量名“View_”
			 * @param listType   当View为VIEW_LIST_ITEM时有效,代表list类型
			 * @param listIndex  当View为VIEW_LIST_ITEM时有效，代表其INDEX
			 * @return 实例
			 */
			public Object  operateView(int actionType,int view,int listType,int listIndex);
		}

	}

	
	private Boolean mHasSetRecordWin;
	private RecordWin2 mRecordWin;

	/**
	 * 方法名：设置语音界面窗口实现
	 * 方法描述：当默认语音界面不满足需求时，通过此方法自定义语音界面，接替语音界面全部UI显示及操作
	 *
	 * @param recordWin 语音界面接口回调实现
	 */
	public void setRecordWin2(final RecordWin2 recordWin) {
		mHasSetRecordWin = true;
		mRecordWin = recordWin;
		if (mRecordWin == null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.recordwin2.clear", null, null);
			return;
		}
		if (GlobalContext.get() == null) {
			return;
		}
		// 设置了RecordWin2时默认开启异常捕获
		try {
			String dir = Environment.getExternalStorageDirectory().getPath();
			CrashCommonHandler.init(GlobalContext.get(), new CrashLisener(dir + "/txz/report/"));
		} catch (Exception e) {
		}
		UI2Manager.getInstance().initBySDK(new UIInitListener() {
			@Override
			public void onSuccess() {
				recordWin.setWinController(new RecordWinController() {
					@Override
					public void show() {
						RecordWin2Manager.getInstance().show();
					}

					@Override
					public void dismiss() {
						RecordWin2Manager.getInstance().dismiss();
					}

					@Override
					public Object addView(int targetView, View view) {
						return RecordWin2Manager.getInstance().addView(targetView, view);
					}

					@Override
					public Object operateView(int actionType,int view,int listType,int listIndex){
						return RecordWin2Manager.getInstance().operateView(actionType, view, listType, listIndex);
					}

				});
			}
			
			@Override
			public void onErrorError() {
				
			}
			
			@Override
			public void onError() {
				RecordWin2Manager.getInstance().forceUseUI1();
			}
		});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.recordwin2.set", null, null);
		TXZService.setCommandProcessor("win.record2.", new CommandProcessor() {
			@Override
			public byte[] process(String packageName, String command, byte[] data) {
				if (command.equals("show")) {
					RecordWin2Manager.getInstance().show();
					return null;
				} else if (command.equals("dismiss")) {
					RecordWin2Manager.getInstance().dismiss();
					return null;
				} else if (command.equals("showData")) {
					String json = new String(data);
					if (!mRecordWin.showData(json)) {
						RecordWin2Manager.getInstance().showData(json);
					}
					return null;
				} else if (command.equals("fullScreen")) {
					Boolean isFull = Boolean.parseBoolean(new String(data));
					if (isFull != null) {
						UI2Manager.runOnUIThread(new Runnable1<Boolean>(isFull) {
							@Override
							public void run() {
								com.txznet.comm.ui.recordwin.RecordWin2.getInstance().setIsFullSreenDialog(mP1);
							}
						}, 0);
					}
					return null;
				}
				return null;
			}
		});
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, 
				"txz.recordwin2.set", "false".getBytes(), null);
	}

}
