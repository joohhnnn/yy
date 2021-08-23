package com.txznet.comm.ui.theme.test.winlayout.inner;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.FloatPointSP;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.view.ChatFromSysView;
import com.txznet.comm.ui.theme.test.view.HelpButtonView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatFromSysViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZConfigManager.FloatToolType;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;

import java.util.Random;

/**
 * 无屏模式采用的布局
 */
public class WinLayoutNone extends IWinLayout {

	private static WinLayoutNone sInstance = new WinLayoutNone();

	private WinLayoutNone() {
	}

	public static WinLayoutNone getInstance() {
		return sInstance;
	}

	private int lastScreenType;

	private LinearLayout mRootLayout;
	private FrameLayout flTalk;
	private FrameLayout llHelp;
//	private ImageView ivHelp;
	private RelativeLayout rlRecordView;
	private RelativeLayout llContent;
	private LinearLayout llHome;
	private HelpButtonView mHelp;

	private RelativeLayout.LayoutParams rlParams;
	private LinearLayout.LayoutParams llParams;
	
	private int historyX;
	private int historyY;
	private int hor_gravity;
	private int ver_gravity;
	
	private int status_bar_height;
	/*private int dimen_y70;
	private int dimen_y10;
	private int dimen_x10;*/
	private int contentWidth;    //内容框宽度
	private int contentHeight;    //内容框高度
	private int talkHeight;    //对话框高度
	private int talkMagin;    //对话框左右边距
	private int intervalHeight;    //对话框与内容框间隔
	private int paddingRight;    //到屏幕边缘的横向间隔
	private int paddingBottom;    //到屏幕边缘的纵向间隔
	private int helpIconSzie;    //语音中心图标大小
	// width - 640  height - 360
	
	private int screenWidth;
	private int screenHeight;

	private boolean hadShowHelpList;
	
	public void init() {
		//初始化页面列表数量
		SizeConfig.getInstance().init(3);
		if (mRootLayout == null) {
			View tempLayout = LayouUtil.getView("win_layout_none_left_bottom");
			mRootLayout = (LinearLayout) LayouUtil.findViewByName("rlRoot",
					tempLayout);
			
			status_bar_height = GlobalContext.get().getResources().
					getDimensionPixelSize(GlobalContext.get().getResources().getIdentifier("status_bar_height", "dimen", "android"));
			//竖屏下的宽度x就是横屏的高度y，只根据竖屏的宽度来确定内容框打大小
			int unit = ViewParamsUtil.unit;
			contentWidth = 64 * unit;  // old x448
			contentHeight = 36 * unit;  // old y252
			talkHeight = 10 * unit;    //对话框高度
			talkMagin = 2 * unit;    //对话框左右边距
			intervalHeight = unit;    //对话框与内容框间隔
			paddingRight = unit;    //到屏幕边缘的横向间隔
			paddingBottom = unit / 2;    //到屏幕边缘的纵向间隔
			helpIconSzie = 5 * unit;

			screenWidth = SizeConfig.screenWidth;
			screenHeight = SizeConfig.screenHeight;

			mRootLayout.setPadding(paddingRight,paddingBottom,paddingRight,paddingBottom);

			llHome = (LinearLayout) LayouUtil.findViewByName("llHome",
					mRootLayout);
			llHome.setBackground(LayouUtil
					.getDrawable("bg_none"));
			llParams = (android.widget.LinearLayout.LayoutParams) llHome
					.getLayoutParams();
			llParams.height = talkHeight;
			llHome.setLayoutParams(llParams);
			
			rlRecordView = (RelativeLayout) LayouUtil.findViewByName(
					"rlRecordView", tempLayout);
			llParams = (android.widget.LinearLayout.LayoutParams) rlRecordView
					.getLayoutParams();
			//llParams.width = (int) (dimen_y70 * 120.0 / 100.0);
			llParams.width = talkHeight;
			llParams.height = talkHeight;
			rlRecordView.setLayoutParams(llParams);

			flTalk = (FrameLayout) LayouUtil.findViewByName("flTalk",
					tempLayout);
			llParams = (android.widget.LinearLayout.LayoutParams) flTalk.getLayoutParams();
			llParams.leftMargin = talkMagin;
			llParams.rightMargin = talkMagin;
			flTalk.setLayoutParams(llParams);

			llHelp = (FrameLayout) LayouUtil.findViewByName("llHelp",
					tempLayout);
			llHelp.setBackground(LayouUtil
					.getDrawable("help_bg_right"));
			llHelp.setVisibility(View.VISIBLE);
			llParams = (android.widget.LinearLayout.LayoutParams) llHelp.getLayoutParams();
			llParams.width = talkHeight;
			llParams.height = talkHeight;
			llHelp.setLayoutParams(llParams);
			/*llHelp.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					RecordWin2Manager.getInstance().operateView(
							RecordWinController.OPERATE_CLICK,
							RecordWinController.VIEW_HELP, 0, 0);
				}
			});*/

			/*ivHelp = (ImageView) LayouUtil.findViewByName("ivHelp",
					tempLayout);
			llParams = (android.widget.LinearLayout.LayoutParams) ivHelp
					.getLayoutParams();
			llParams.width = (int) (dimen_y70 / 2.0);
			llParams.height = (int) (dimen_y70 / 2.0);
			llParams.leftMargin = (int) (dimen_y70 / 4.0);
			llParams.rightMargin = (int) (dimen_y70 / 4.0);
			ivHelp.setLayoutParams(llParams);
			ivHelp.setImageDrawable(LayouUtil.getDrawable("help_icon"));*/
			mHelp = new HelpButtonView(GlobalContext.get(),helpIconSzie,helpIconSzie);
			FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
			flParams.gravity = Gravity.CENTER;
			/*llParams.leftMargin = (int) (dimen_y70 / 4.0);
			llParams.rightMargin = (int) (dimen_y70 / 4.0);*/
			llHelp.addView(mHelp,flParams);
            mHelp.showByConfig();
			//注册帮助按钮状态监听，更新帮助按钮状态
			ConfigUtil.registerIconStateChangeListener(new ConfigUtil.IconStateChangeListener() {
				@Override
				public void onStateChanged(int i, boolean b) {
                    UI2Manager.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            llHelp.setVisibility(mHelp.showByConfig()?View.VISIBLE:View.GONE);
                        }
                    },0);
				}
			});

			llContent = (RelativeLayout) LayouUtil.findViewByName("llContent",
					tempLayout);
			llContent.setBackground(LayouUtil
					.getDrawable("bg_none"));
			llParams = (android.widget.LinearLayout.LayoutParams) llContent
					.getLayoutParams();
			llParams.width = contentWidth;
			llParams.height = contentHeight;
			llContent.setLayoutParams(llParams);
			
			historyX = FloatPointSP.getInstance().getX();
			historyY = FloatPointSP.getInstance().getY();
			changeLayoutBaseLocation();


		}

	}

	@Override
	public void setBackground(Drawable drawable) {
		get().setBackground(drawable);
	}

	/**
	 * @param type
	 * 
	 * 0 只有fromSys
	 * 1 包含content
	 */
	public void updateScreenType(int type) {
		mRootLayout.measure(0, 0);
		int x = FloatPointSP.getInstance().getX();
		int y = FloatPointSP.getInstance().getY();
		int w = mRootLayout.getMeasuredWidth();
//		int h = mRootLayout.getMeasuredHeight();
		int h = 0;

		y += status_bar_height;
		int floatToolHei = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100 , GlobalContext.get().getResources().getDisplayMetrics());
		int recordViewHei = talkHeight;

		if(hor_gravity == Gravity.RIGHT) {
			x = x + floatToolHei / 2 - recordViewHei / 2 - (w - recordViewHei);
		}/* else {
			x= x + floatToolHei / 2 - recordViewHei / 2;
		}*/
		LogUtil.logd(WinLayout.logTag+ "updateScreenType: x--"+x+"--"+floatToolHei+"--"+recordViewHei);
		x = x < 0 ? 0 : x;

		llParams = (LayoutParams) llHome.getLayoutParams();
		switch (type) {
		case 0:
			y = y + floatToolHei / 2 - recordViewHei / 2;
			y = y < 0 ? 0 : y;
			llParams.bottomMargin = llParams.topMargin = 0;
			h = talkHeight + 2 * paddingBottom;
			break;

		case 1:
			if(ver_gravity == Gravity.BOTTOM) {
				y = y + floatToolHei / 2 - recordViewHei / 2 - (mRootLayout.getMeasuredHeight() - recordViewHei);
				if (llParams.topMargin != 0 || llParams.bottomMargin != 0) y+= intervalHeight;
				llParams.topMargin = intervalHeight;
			} else {
				y = y + floatToolHei / 2 - recordViewHei / 2;
				llParams.bottomMargin = intervalHeight;
			}
			y = y < 0 ? 0 : y;
			// 80 + 252 + 10
			h = talkHeight + contentHeight + intervalHeight + 2 * paddingBottom;
			//由仅展示对话框切换到全部展示时延迟显示
			if (lastScreenType == 0){
				llContent.setVisibility(View.GONE);
				AppLogicBase.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						//延迟显示，防止跳屏
						llContent.setVisibility(View.VISIBLE);
					}
				},100);
			}
			break;
		}
		llHome.setLayoutParams(llParams);
		RecordWin2Manager.getInstance().updateDisplayArea(x , y , w, h);
		lastScreenType = type;
	}
	
	private void changeLayoutBaseLocation() {
		if (historyX < screenWidth / 2) {
			// tempX = tempX + (mRootLayout.getWidth() / 2);
			hor_gravity = Gravity.LEFT;
			llHome.removeAllViews();

			rlParams = new RelativeLayout.LayoutParams(talkHeight, talkHeight);
			llHome.addView(rlRecordView, rlParams);

			llParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			llParams.gravity = Gravity.CENTER_VERTICAL;
			llParams.leftMargin = talkMagin;
			llParams.rightMargin = talkMagin;
			llHome.addView(flTalk, llParams);

			llHelp.setBackground(LayouUtil
					.getDrawable("help_bg_right"));
			llParams = new LinearLayout.LayoutParams(talkHeight,talkHeight);
			llParams.gravity = Gravity.CENTER;
			llHome.addView(llHelp, llParams);

		} else {
			hor_gravity = Gravity.RIGHT;
			// tempX = screenWidth - tempX - (mRootLayout.getWidth() / 2);
			// tempGravity = Gravity.RIGHT;
			llHome.removeAllViews();

			llHelp.setBackground(LayouUtil
					.getDrawable("help_bg_left"));
			llParams = new LinearLayout.LayoutParams(talkHeight,talkHeight);
			llParams.gravity = Gravity.CENTER;
			// llParams.leftMargin = (int) LayouUtil.getDimen("x20");
			/*llParams.rightMargin = dimen_x10;*/
			llHome.addView(llHelp, llParams);

			llParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			llParams.gravity = Gravity.CENTER_VERTICAL;
			llParams.leftMargin = talkMagin;
			llParams.rightMargin = talkMagin;
			llHome.addView(flTalk, llParams);

			llParams = new LinearLayout.LayoutParams(talkHeight, talkHeight);
			llParams.gravity = Gravity.RIGHT;
			llHome.addView(rlRecordView, llParams);
		}

		// if ((tempY + mRootLayout.getHeight() / 2) < screenHeight / 2) {
		if (historyY < screenHeight / 2) {
			// tempY = tempY + (mRootLayout.getHeight() / 2);
			ver_gravity = Gravity.TOP;
			mRootLayout.setOrientation(LinearLayout.VERTICAL);

			llParams = new android.widget.LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,talkHeight);

			mRootLayout.removeAllViews();
			// llParams.bottomMargin = (int) LayouUtil.getDimen("x10");
			llParams.gravity = hor_gravity;
			mRootLayout.addView(llHome, llParams);

			llParams = new LinearLayout.LayoutParams(contentWidth,contentHeight);
			llParams.gravity = hor_gravity;
			mRootLayout.addView(llContent, llParams);

		} else {
			ver_gravity = Gravity.BOTTOM;
			// tempY = screenHeight - tempY
			// - (mRootLayout.getHeight() / 2);
			mRootLayout.removeAllViews();

			llParams = new android.widget.LinearLayout.LayoutParams(contentWidth,contentHeight);
			llParams.gravity = hor_gravity;
			mRootLayout.addView(llContent, llParams);

			llParams = new android.widget.LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,talkHeight);
			// llParams.topMargin = (int) LayouUtil.getDimen("x10");
			llParams.gravity = hor_gravity;
			mRootLayout.addView(llHome, llParams); // 这个有效果，会让content在home的下方
		}
	}

//	String beforeFLoatType;

	/**
	 * 添加录音图标
	 */
	@Override
	public void addRecordView(View recordView) {
		if (recordView != null){
			// 这种形式的录音动画直接在聊天View里面的
			LogUtil.logd(WinLayout.logTag+ "WinLayoutNone.addRecordView() " + "");

			int tempX = FloatPointSP.getInstance().getX();
			int tempY = FloatPointSP.getInstance().getY();
			if ((historyX != tempX) || (historyY != tempY)) {
				historyX = tempX;
				historyY = tempY;
				changeLayoutBaseLocation();
			}

			if (recordView.getParent() == null) {
				rlParams = new android.widget.RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
				rlParams.addRule(RelativeLayout.CENTER_IN_PARENT);// addRule参数对应RelativeLayout
				rlRecordView.addView(recordView, rlParams);
			}

		/*if(!ConfigUtil.isShowHelpInfos() && ConfigUtil.isShowSettings()) {
			llHelp.setVisibility(View.GONE);
		} else {
			llHelp.setVisibility(View.VISIBLE);
		}*/
			updateScreenType(0);
			ifGetType = true;

		}

	}

	@Override
	public View get() {
		LogUtil.logd(WinLayout.logTag+ "WinLayoutNone.get() ");
		return mRootLayout;
	}
	
	
	private boolean ifGetType;
	

	/**
	 * 添加View到对应的地方
	 */
	@Override
	public Object addView(int targetView, View view,
			ViewGroup.LayoutParams winManLayoutP) {
		LogUtil.logd(WinLayout.logTag+ "WinLayoutNone.addView " + targetView);
		
		if(ifGetType) {
			ifGetType = false;
			/*
			beforeFLoatType = ConfigUtil.getFloatTool();
			switch (beforeFLoatType) {
				case "FLOAT_TOP":
				case "FLOAT_NORMAL":
					TXZConfigManager.getInstance().showFloatTool(
							FloatToolType.FLOAT_NONE);
					break;
				case "FLOAT_NONE":
				default:
					break;
			}
			*/
		}

        Object tag = view.getTag();
		LogUtil.logd(WinLayout.logTag+ "addView: tag:"+tag);
		if (tag instanceof Integer && (Integer)tag == ViewData.TYPE_FULL_LIST_COMPETITION){
			//赛事查询列表没有反馈语的展示
			WinLayout.isHideView = false;
		}
		//展示过帮助界面后恢复之前的设置
		if (hadShowHelpList){
			hadShowHelpList = false;
			llContent.setBackground(LayouUtil
					.getDrawable("bg_none"));
			llParams = (LayoutParams) llContent.getLayoutParams();
			llParams.width = contentWidth;
			LogUtil.d("contentWidth:"+llParams.width);
			llContent.setLayoutParams(llParams);
		}

		switch (targetView) {
		case RecordWinController.TARGET_CONTENT_CHAT:
			if (tag instanceof Integer) {
				Integer type = (Integer) tag;
				switch (type) {
                case ViewData.TYPE_CHAT_HELP_TIPS:
                    //不展示的页面需要将vTips清空，防止显示在下一个界面上
                	WinLayout.getInstance().vTips = null;
                    return null;
				case ViewData.TYPE_CHAT_OFFLINE_PROMOTE:
				case ViewData.TYPE_CHAT_WEATHER:
				case ViewData.TYPE_CHAT_SHARE:
				case ViewData.TYPE_CHAT_BIND_DEVICE_QRCODE:
				case ViewData.TYPE_CHAT_CONSTELLATION_FORTUNE:
				case ViewData.TYPE_CHAT_CONSTELLATION_MATCHING:
				case ViewData.TYPE_CHAT_FEEDBACK:
				case ViewData.TYPE_CHAT_FROM_SYS_INTERRUPT:
				case ViewData.TYPE_CHAT_COMPETITION_DETAIL:
                    llContent.removeAllViews();
                    llContent.setVisibility(View.GONE);
					setGuideText();

					llParams = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.MATCH_PARENT);
					llParams.gravity = Gravity.CENTER;
					if (type == ViewData.TYPE_CHAT_FROM_SYS_INTERRUPT){
						ScrollView mScrollView = new ScrollView(GlobalContext.get());
						mScrollView.setFillViewport(true);
						mScrollView.setVerticalScrollBarEnabled(false);
						mScrollView.addView(view);

						llParams = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.MATCH_PARENT);
						llContent.addView(mScrollView, llParams);
						llContent.setVisibility(mScrollView.VISIBLE);
						updateScreenType(1);
						return null;
					}
					llContent.addView(view, llParams);
					llContent.setVisibility(View.VISIBLE);
					updateScreenType(1);
					return null;
				}
			}
			llContent.removeAllViews();
			llContent.setVisibility(View.GONE);
			flTalk.removeAllViews();
			flTalk.setVisibility(View.VISIBLE);
			llParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			llParams.gravity = Gravity.CENTER;
			flTalk.addView(view, llParams);
			updateScreenType(0);
			break;
		case RecordWinController.TARGET_CONTENT_FULL:
			if (tag != null && tag.equals(ViewData.TYPE_FULL_LIST_HELP)) {
				hadShowHelpList = true;
				llContent.setBackgroundColor(Color.parseColor("#00FFFFFF"));
				llParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, contentHeight);
				llContent.setLayoutParams(llParams);
			} else {
				llContent.setBackground(LayouUtil
						.getDrawable("bg_none"));
				llParams = (LayoutParams) llContent.getLayoutParams();
				llParams.width = contentWidth;
				LogUtil.d("contentWidth:"+llParams.width);
				llContent.setLayoutParams(llParams);
			}
			setGuideText();
			llContent.removeAllViews();
			llContent.setVisibility(View.VISIBLE);
			llParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			llContent.addView(view, llParams);
			updateScreenType(1);
			break;
		case RecordWinController.TARGET_VIEW_MIC:
			break;
		default:
			break;
		}

		return null;
	}

	//切换到对话界面
	public void showChatView() {
	    if (flTalk != null && llContent != null){
            llContent.removeAllViews();
            llContent.setVisibility(View.GONE);
            flTalk.removeAllViews();
            flTalk.setVisibility(View.VISIBLE);
            updateScreenType(0);
        }
	}
	
	@Override
	public Object removeLastView() {
		return null;
	}
	
	long lastResetTime;
	
	/**
	 * 重置聊天记录，界面关闭时会调用
	 */
	@Override
	public void reset() {
		long now =  SystemClock.elapsedRealtime();
		if((now - lastResetTime) < 50) {
			return;
		}
		LogUtil.logd(WinLayout.logTag+ "winlayoutnone reset enter");
		lastResetTime = now;
		if(flTalk != null) {
			flTalk.removeAllViews();
		}
		if(llContent != null) {
			llContent.removeAllViews();
			llContent.setVisibility(View.GONE);
		}
		/*
		String curType = ConfigUtil.getFloatTool();
		if (beforeFLoatType != null){
			switch (beforeFLoatType) {
				case "FLOAT_TOP":
					if(curType.equals("FLOAT_TOP")) {
						TXZConfigManager.getInstance().showFloatTool(FloatToolType.FLOAT_TOP);
					} else if (curType.equals("FLOAT_NORMAL")) {
						TXZConfigManager.getInstance().showFloatTool(FloatToolType.FLOAT_NORMAL);
					} else if (curType.equals("FLOAT_NONE")) {
						TXZConfigManager.getInstance().showFloatTool(FloatToolType.FLOAT_TOP);
					}
					break;
				case "FLOAT_NORMAL":
					if(curType.equals("FLOAT_TOP")) {
						TXZConfigManager.getInstance().showFloatTool(FloatToolType.FLOAT_TOP);
					} else if (curType.equals("FLOAT_NORMAL")) {
						TXZConfigManager.getInstance().showFloatTool(FloatToolType.FLOAT_NORMAL);
					} else if (curType.equals("FLOAT_NONE")) {
						TXZConfigManager.getInstance().showFloatTool(FloatToolType.FLOAT_NORMAL);
					}
					break;

				case "FLOAT_NONE":
					if(curType.equals("FLOAT_TOP")) {
						TXZConfigManager.getInstance().showFloatTool(FloatToolType.FLOAT_TOP);
					} else if (curType.equals("FLOAT_NORMAL")) {
						TXZConfigManager.getInstance().showFloatTool(FloatToolType.FLOAT_NORMAL);
					} else if (curType.equals("FLOAT_NONE")) {
						TXZConfigManager.getInstance().showFloatTool(FloatToolType.FLOAT_NONE);
					}
					break;
				default:
					break;
			}
		}
		*/
//		beforeFLoatType = ConfigUtil.getFloatTool(); 
		ifGetType = true;
	}

	/**
	 * 释放内存，界面关闭时会调用
	 */
	@Override
	public void release() {
		LogUtil.logd(WinLayout.logTag+ "winlayoutnone release");
		mRootLayout.removeAllViews();
		mRootLayout = null;
		rlRecordView.removeAllViews();
	}

	//显示引导语
	public void setGuideText(){
		ChatFromSysViewData tipViewData = new ChatFromSysViewData();
		ViewAdapter viewAdapter = null;
		LogUtil.logd(WinLayout.logTag+ "none setGuideText:"+WinLayout.getInstance().vTips);
		if (WinLayout.getInstance().vTips != null) {
			String text = WinLayout.getInstance().vTips;
			WinLayout.getInstance().vTips = null;
			if (text.startsWith("请说")){
				int end = text.indexOf('”');
				SpannableString spannableString = new SpannableString(text);
				ForegroundColorSpan colorSpan = new ForegroundColorSpan(Color.parseColor("#FF16CFFF"));
				spannableString.setSpan(colorSpan,3,end,Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				//tipViewData.textContent = (String) spannableString;
				viewAdapter = ChatFromSysView.getInstance().getView(tipViewData,spannableString);
			}else if(text.startsWith("打开语音助手")){
				tipViewData.textContent = text;
				viewAdapter = ChatFromSysView.getInstance().getView(tipViewData);
			}else {
				/*text = "你可以说：\"" + text.replaceAll("；","\"，\"") + "\"";
				tipViewData.textContent = text;
				viewAdapter = ChatFromSysView.getInstance().getView(tipViewData);*/
                if (text.contains("；")){
                    String[] texts = text.split("；");
                    Random random = new Random();
                    switch (texts.length){
                        case 3:
                            int i = random.nextInt(2);
                            text = "你可以说：“" + texts[i]+"”，“"+texts[i+1]+ "”";
                            break;
                        case 4:
                            i = random.nextInt(3);
                            text = "你可以说：“" + texts[i]+"”，“"+texts[i+1]+ "”";
                            break;
                        case 1:
                        case 2:
                            text = "你可以说：“" + text.replaceAll("；","”，“") + "”";
                            break;
                    }
                }else {
					text = "你可以说：“" + text+ "”";
				}
                tipViewData.textContent = text;
                viewAdapter = ChatFromSysView.getInstance().getView(tipViewData);
			}
			flTalk.removeAllViews();
			flTalk.setVisibility(View.VISIBLE);
			llParams = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			llParams.gravity = Gravity.CENTER;
			flTalk.addView(viewAdapter.view, llParams);
			WinLayout.getInstance().vTips = null;
		}
	}


	public void showOfflinePromote(View view){
		llContent.removeAllViews();
		llContent.setVisibility(View.VISIBLE);
		llParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		llContent.addView(view, llParams);
		updateScreenType(1);
	}

}
