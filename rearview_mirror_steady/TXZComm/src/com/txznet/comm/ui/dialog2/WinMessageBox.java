package com.txznet.comm.ui.dialog2;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable.HomeObserver;
import com.txznet.comm.ui.ReverseObservable;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZConfigManager.InitParam;
import com.txznet.txz.comm.R;
import com.txznet.txz.util.LanguageConvertor;

/**
 * 消息按钮操作框
 * 
 * @author pppi
 *
 */
public abstract class WinMessageBox extends WinDialog {
	/**
	 * 默认确认文本
	 */
	public final static String DEFAULT_TEXT_SURE = "确定";
	/**
	 * 默认取消文本
	 */
	public final static String DEFAULT_TEXT_CANCEL = "取消";
	
	/**
	 * 弹窗样式大小
	 */
	private static int sMessageDialogType = InitParam.MESSAGE_DIALOG_TYPE_NORMAL;
	/**
	 * 设置弹窗样式大小
	 * @param type
	 */
	public static void setMessageDialogType(int type) {
		sMessageDialogType = type;
		if (sMessageDialogType == InitParam.MESSAGE_DIALOG_TYPE_SMALL) {
			MESSAGE_TITLE_SIZE = 27;
			MESSAGE_MESSAGE_SIZE = 22;
			MESSAGE_SCROLL_SIZE = 24;
			MESSAGE_BTN_LEFT_SIZE = 21;
			MESSAGE_BTN_MIDDLE_SIZE = 21;
			MESSAGE_BTN_RIGHT_SIZE = 21;
		} else {
			MESSAGE_TITLE_SIZE = 36;
			MESSAGE_MESSAGE_SIZE = 30;
			MESSAGE_SCROLL_SIZE = 32;
			MESSAGE_BTN_LEFT_SIZE = 28;
			MESSAGE_BTN_MIDDLE_SIZE = 28;
			MESSAGE_BTN_RIGHT_SIZE = 28;
		}
	}
	
	public static int MESSAGE_TITLE_SIZE = 36;
	public static int MESSAGE_MESSAGE_SIZE = 30;
	public static int MESSAGE_SCROLL_SIZE = 32;
	public static int MESSAGE_BTN_LEFT_SIZE = 28;
	public static int MESSAGE_BTN_MIDDLE_SIZE = 28;
	public static int MESSAGE_BTN_RIGHT_SIZE = 28;

	

	/**
	 * 对话框构建数据类型
	 * 
	 * @author pppi
	 *
	 */
	public static class WinMessageBoxBuildData extends
			WinDialog.DialogBuildData {
		String mTitleText;
		String mMessageText;
		boolean mMessageAllowScroll;
		String mLeftText;
		String mRightText;
		String mMidText;

		@Override
		public void check() {
			super.check();
			this.addExtraInfo("mTitleText", mTitleText);
			this.addExtraInfo("mMessageText", mMessageText);
			this.addExtraInfo("mMessageAllowScroll", mMessageAllowScroll);
			this.addExtraInfo("mLeftText", mLeftText);
			this.addExtraInfo("mRightText", mRightText);
			this.addExtraInfo("mMidText", mMidText);
			this.addExtraInfo("dialogType", WinMessageBox.class.getSimpleName());
		}

		/**
		 * 设置标题文本
		 * 
		 * @param text
		 *            标题文本
		 * @return
		 */
		public WinMessageBoxBuildData setTitleText(String text) {
			this.mTitleText = text;
			return this;
		}

		/**
		 * 设置消息文本
		 * 
		 * @param text
		 *            消息文本
		 * @return
		 */
		public WinMessageBoxBuildData setMessageText(String text) {
			this.mMessageText = text;
			return this;
		}

		/**
		 * 设置消息文本
		 * 
		 * @param text
		 *            消息文本
		 * @param scroll
		 *            是否允许滚动，否则自动调整大小
		 * @return
		 */
		public WinMessageBoxBuildData setMessageText(String text, boolean scroll) {
			this.mMessageText = text;
			this.mMessageAllowScroll = scroll;
			return this;
		}

		/**
		 * 设置消息文本
		 * 
		 * @param scroll
		 *            是否允许滚动，否则自动调整大小
		 * @return
		 */
		public WinMessageBoxBuildData setMessageAllowScroll(boolean scroll) {
			this.mMessageAllowScroll = scroll;
			return this;
		}

		/**
		 * 设置左边按钮的文本
		 * 
		 * @param text
		 *            按钮文本
		 * @return
		 */
		public WinMessageBoxBuildData setLeftText(String text) {
			this.mLeftText = text;
			return this;
		}

		/**
		 * 设置右边按钮的文本
		 * 
		 * @param text
		 *            按钮文本
		 * @return
		 */
		public WinMessageBoxBuildData setRightText(String text) {
			this.mRightText = text;
			return this;
		}

		/**
		 * 设置中间按钮的文本
		 * 
		 * @param text
		 *            按钮文本
		 * @return
		 */
		public WinMessageBoxBuildData setMidText(String text) {
			this.mMidText = text;
			return this;
		}
	};

	/**
	 * 通过构建数据构造对话框
	 * 
	 * @param data
	 */
	public WinMessageBox(WinMessageBoxBuildData data) {
		this(data, true);
	}

	/**
	 * 通过构造数据构造对话框，用于给派生类构造，构造时先不初始化
	 * 
	 * @param data
	 *            构建数据
	 * @param init
	 *            是否初始化，自己构造时传true，派生类构造时传false
	 */
	protected WinMessageBox(WinMessageBoxBuildData data, boolean init) {
		super(data, false);
		mWinMessageBoxBuildData = data;
		if (init) {
			initDialog();
		}
	}

	/**
	 * 视图对象持有类型
	 * 
	 * @author pppi
	 *
	 */
	public static class ViewHolder {
		public View mBlank;
		public View mContent;
		public TextView mTitle;
		public TextView mText;
		public ListView mTextList;
		public ScrollView mScrollView;
		public TextView mScrollText;
		public Button mLeftButton;
		public Button mMidButton;
		public Button mRightButton;
	}

	/**
	 * 视图对象持有者
	 */
	protected ViewHolder mViewHolder;
	/**
	 * 对话框构造数据
	 */
	protected WinMessageBoxBuildData mWinMessageBoxBuildData;

	/**
	 * 设置标题文本，已废弃，建议在初始化构造数据时设置
	 * 
	 * @param title
	 * @return
	 */
	@Deprecated
	protected WinMessageBox setTitle(String title) {
		mWinMessageBoxBuildData.mTitleText = title;
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateTitle();
			}
		}, 0);
		return this;
	}

	/**
	 * 更新标题
	 */
	protected void updateTitle() {
		updateTextView(mViewHolder.mTitle, mWinMessageBoxBuildData.mTitleText);
	}

	/**
	 * 设置消息是否允许滚动，已废弃，建议在初始化构造数据时设置
	 * 
	 * @param scroll
	 */
	@Deprecated
	public void setMessageAllowScroll(boolean scroll) {
		if (mWinMessageBoxBuildData.mMessageAllowScroll != scroll) {
			setMessageText(mWinMessageBoxBuildData.mMessageText, scroll);
		}
	}

	/**
	 * 设置消息文本，已废弃，建议在初始化构造数据时设置
	 * 
	 * @param msg
	 * @return
	 */
	@Deprecated
	protected WinMessageBox setMessageText(String msg) {
		return setMessageText(msg, mWinMessageBoxBuildData.mMessageAllowScroll);
	}

	/**
	 * 设置消息文本以及是否允许滚动，已废弃，建议在初始化构造数据时设置
	 * 
	 * @param msg
	 * @param scroll
	 * @return
	 */
	@Deprecated
	protected WinMessageBox setMessageText(String msg, boolean scroll) {
		mWinMessageBoxBuildData.setMessageText(msg, scroll);

		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateMessageText();
			}
		}, 0);

		return this;
	}

	/**
	 * 更新消息文本
	 */
	protected void updateMessageText() {
		if (mWinMessageBoxBuildData.mMessageAllowScroll) {
			mViewHolder.mText.setText("");
			mViewHolder.mText.setVisibility(View.GONE);
			mViewHolder.mScrollView.setVisibility(View.VISIBLE);
			mViewHolder.mScrollText.setVisibility(View.VISIBLE);
			mViewHolder.mScrollText.setText(LanguageConvertor
					.toLocale(mWinMessageBoxBuildData.mMessageText));
		} else {
			mViewHolder.mScrollText.setText("");
			mViewHolder.mScrollText.setVisibility(View.GONE);
			mViewHolder.mScrollView.setVisibility(View.GONE);
			mViewHolder.mText.setVisibility(View.VISIBLE);
			mViewHolder.mText.setText(LanguageConvertor
					.toLocale(mWinMessageBoxBuildData.mMessageText));
		}
	}

	/**
	 * 设置左边按钮文本，已废弃，建议在初始化构造数据时设置
	 * 
	 * @param text
	 * @return
	 */
	@Deprecated
	protected WinMessageBox setLeftButton(String text) {
		mWinMessageBoxBuildData.setLeftText(text);
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateTextView(mViewHolder.mLeftButton,
						mWinMessageBoxBuildData.mLeftText);
			}
		}, 0);
		return this;
	}

	/**
	 * 设置中间按钮文本，已废弃，建议在初始化构造数据时设置
	 * 
	 * @param text
	 * @return
	 */
	@Deprecated
	protected WinMessageBox setMidButton(String text) {
		mWinMessageBoxBuildData.setMidText(text);
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateTextView(mViewHolder.mMidButton,
						mWinMessageBoxBuildData.mMidText);
			}
		}, 0);
		return this;
	}

	/**
	 * 设置右边按钮文本，已废弃，建议在初始化构造数据时设置
	 * 
	 * @param text
	 * @return
	 */
	@Deprecated
	protected WinMessageBox setRightButton(String text) {
		mWinMessageBoxBuildData.setRightText(text);
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateTextView(mViewHolder.mRightButton,
						mWinMessageBoxBuildData.mRightText);
			}
		}, 0);
		return this;
	}

	/**
	 * 设置左边按钮倒计时
	 * 
	 * @param text
	 *            倒计时格式化文本
	 * @param time
	 *            倒计时时间，单位：秒
	 */
	protected void clickLeftCountDown(final String text, final int time) {
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateCountDown(mViewHolder.mLeftButton, text, time,
						new Runnable() {
							@Override
							public void run() {
								onClickLeft();
							}
						});
			}
		}, 0);
	}

	/**
	 * 设置中间按钮倒计时
	 * 
	 * @param text
	 *            倒计时格式化文本
	 * @param time
	 *            倒计时时间，单位：秒
	 */
	protected void clickMidCountDown(final String text, final int time) {
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateCountDown(mViewHolder.mMidButton, text, time,
						new Runnable() {
							@Override
							public void run() {
								onClickMid();
							}
						});
			}
		}, 0);
	}

	/**
	 * 设置右边按钮倒计时
	 * 
	 * @param text
	 *            倒计时格式化文本
	 * @param time
	 *            倒计时时间，单位：秒
	 */
	protected void clickRightCountDown(final String text, final int time) {
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateCountDown(mViewHolder.mRightButton, text, time,
						new Runnable() {
							@Override
							public void run() {
								onClickRight();
							}
						});
			}
		}, 0);
	}

	/**
	 * 设置标题倒计时
	 * 
	 * @param text
	 *            倒计时格式化文本
	 * @param time
	 *            倒计时时间，单位：秒
	 */
	public void dismissTitleCountDown(final String text, final int time) {
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateCountDown(mViewHolder.mTitle, text, time, new Runnable() {
					@Override
					public void run() {
						WinMessageBox.this.dismissInner();
					}
				});
			}
		}, 0);
	}

	/**
	 * 设置消息倒计时
	 * 
	 * @param text
	 *            倒计时格式化文本
	 * @param time
	 *            倒计时时间，单位：秒
	 */
	public void dismissMessageCountDown(final String text, final int time) {
		runOnUiGround(new Runnable() {
			@Override
			public void run() {
				updateCountDown(
						mWinMessageBoxBuildData.mMessageAllowScroll ? mViewHolder.mScrollText
								: mViewHolder.mText, text, time,
						new Runnable() {
							@Override
							public void run() {
								WinMessageBox.this.dismissInner();
							}
						});
			}
		}, 0);
	}

	/**
	 * 点击左边按钮回调
	 */
	public void onClickLeft() {
		dismissInner();
	}

	/**
	 * 点击中间按钮回调
	 */
	public void onClickMid() {
		dismissInner();
	}

	/**
	 * 点击右边按钮回调
	 */
	public void onClickRight() {
		dismissInner();
	}

	/**
	 * 点击空白处回调
	 */
	public void onClickBlank() {
		DialogBuildData oBuildData = mBuildData;
		if (oBuildData != null){
			//决定对话框是否可以响应界面外的touch事件。注意:这两个参数默认值是null,null应该等同于true。
			if (oBuildData.mCancelable == false || oBuildData.mCancelOutside == false){
				return;
			}
		}
		
		dismissInner();
	}

	/**
	 * 创建视图
	 */
	@SuppressLint("InflateParams")
	@Override
	protected View createView() {
		int layoutId = R.layout.comm_win_messagebox;
		if (sMessageDialogType == InitParam.MESSAGE_DIALOG_TYPE_SMALL) {
			layoutId = R.layout.comm_win_messagebox_small;
		}
		View context = LayoutInflater.from(getContext()).inflate(layoutId, null);
		mViewHolder = new ViewHolder();
		mViewHolder.mBlank = context.findViewById(R.id.frmMessageBox_Blank);
		mViewHolder.mContent = context.findViewById(R.id.llMessageBox_shadow);
		mViewHolder.mTitle = (TextView) context
				.findViewById(R.id.txtMessageBox_Title);
		mViewHolder.mText = (TextView) context
				.findViewById(R.id.txtMessageBox_Message);
		mViewHolder.mTextList = (ListView) context
				.findViewById(R.id.lvMessageBox_Message);
		mViewHolder.mScrollView = (ScrollView) context
				.findViewById(R.id.slMessageBox_Scroll);
		mViewHolder.mScrollText = (TextView) context
				.findViewById(R.id.txtMessageBox_Scroll_Message);
		mViewHolder.mLeftButton = (Button) context
				.findViewById(R.id.btnMessageBox_Button1);
		mViewHolder.mMidButton = (Button) context
				.findViewById(R.id.btnMessageBox_Button3);
		mViewHolder.mRightButton = (Button) context
				.findViewById(R.id.btnMessageBox_Button2);
		initView();
		return context;
	}

	public static final int TYPE_BTN_LEFT = 1;
	public static final int TYPE_BTN_RIGHT = 2;
	public static final int TYPE_BTN_MID = 3;

	@Override
	public void show() {
		if(!TextUtils.isEmpty(dialogTool)){
			GlobalContext.get().registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					switch (intent.getIntExtra("btnType", 0)){
						case TYPE_BTN_LEFT:
							onClickLeft();
							break;
						case TYPE_BTN_RIGHT:
							onClickRight();
							break;
						case TYPE_BTN_MID:
							onClickMid();
							break;
					}
					if(!intent.getBooleanExtra("register", false)){
						GlobalContext.get().unregisterReceiver(this);
					}
					LogUtil.logd("this.hashCode() dialog clickCallback btnType = "
							+ intent.getIntExtra("btnType", 0) + " ,register = "
							+ intent.getBooleanExtra("register", false));
				}
			}, new IntentFilter("txz.dialog.click." + this.hashCode()));
			LogUtil.logd("register clickCallback = " + this.hashCode());
		}
		super.show();

	}

	/**
	 * 点击某个视图
	 * 
	 * @param viewId
	 * @return
	 */
	public boolean clickView(int viewId, boolean fromVoice) {
		if (viewId == R.id.frmMessageBox_Blank) {
			WinMessageBox.this.onClickBlank();
		} else if (viewId == R.id.btnMessageBox_Button1) {
			WinMessageBox.this.onClickLeft();
		} else if (viewId == R.id.btnMessageBox_Button2) {
			WinMessageBox.this.onClickRight();
		} else if (viewId == R.id.btnMessageBox_Button3) {
			WinMessageBox.this.onClickMid();
		} else {
			return super.clickView(viewId, fromVoice);
		}
		return true;
	}

	OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (clickView(v.getId(), false)) {
				WinMessageBox.this.doReport(REPORT_ACTION_TYPE_CLICK,
						getReportViewId(v.getId()));
			}
		}
	};

	private void initView() {
		mViewHolder.mBlank.setOnClickListener(mOnClickListener);
		mViewHolder.mLeftButton.setOnClickListener(mOnClickListener);
		mViewHolder.mMidButton.setOnClickListener(mOnClickListener);
		mViewHolder.mRightButton.setOnClickListener(mOnClickListener);
		mViewHolder.mText.getViewTreeObserver().addOnPreDrawListener(
				new OnPreDrawListener() {
					@Override
					public boolean onPreDraw() {
						autofitTextView(mViewHolder.mText);
						return true;
					}
				});
		
		updateTextSize();
		updateTitle();
		updateMessageText();
		updateTextView(mViewHolder.mLeftButton,
				mWinMessageBoxBuildData.mLeftText);
		updateTextView(mViewHolder.mMidButton, mWinMessageBoxBuildData.mMidText);
		updateTextView(mViewHolder.mRightButton,
				mWinMessageBoxBuildData.mRightText);
		updateButtonBg();
	}
	
	private void updateTextSize() {
		TextViewUtil.setTextSize(mViewHolder.mTitle, ViewConfiger.getInstance().getSize(MESSAGE_TITLE_SIZE));
		TextViewUtil.setTextSize(mViewHolder.mText, ViewConfiger.getInstance().getSize(MESSAGE_MESSAGE_SIZE));
		TextViewUtil.setTextSize(mViewHolder.mScrollText, ViewConfiger.getInstance().getSize(MESSAGE_SCROLL_SIZE));
		mViewHolder.mLeftButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				ViewConfiger.getInstance().getSize(MESSAGE_BTN_LEFT_SIZE));
		mViewHolder.mMidButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				ViewConfiger.getInstance().getSize(MESSAGE_BTN_MIDDLE_SIZE));
		mViewHolder.mRightButton.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				ViewConfiger.getInstance().getSize(MESSAGE_BTN_RIGHT_SIZE));
	}

	/**
	 * 按钮只有一个时的背景图跟有多个时的背景图不同
	 */
	@SuppressLint("NewApi")
	private void updateButtonBg() {
		int count = 0;
		if (mViewHolder.mLeftButton.getVisibility() == View.VISIBLE)
			++count;
		if (mViewHolder.mMidButton.getVisibility() == View.VISIBLE)
			++count;
		if (mViewHolder.mRightButton.getVisibility() == View.VISIBLE)
			++count;
		if (count == 1) {
			if (mViewHolder.mLeftButton.getVisibility() == View.VISIBLE) {
				mViewHolder.mLeftButton.setBackground(
						GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_single));
			}
			if (mViewHolder.mMidButton.getVisibility() == View.VISIBLE) {
				mViewHolder.mMidButton.setBackground(
						GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_single));
			}
			if (mViewHolder.mRightButton.getVisibility() == View.VISIBLE) {
				mViewHolder.mRightButton.setBackground(
						GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_single));
			}
		} else {
			mViewHolder.mLeftButton.setBackground(
					GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_left));
			mViewHolder.mMidButton.setBackground(
					GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_middle));
			mViewHolder.mRightButton.setBackground(
					GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_right));
		}
	}
	
	// 根据textview的高度限制自适应字号
	@SuppressLint("NewApi")
	private void autofitTextView(TextView textView) {
		boolean shouldVisible = false;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			// maxHeight需要SDK>=16，固定dp不需要，但硬编码
			if (textView.getHeight() == textView.getMaxHeight()) {
				shouldVisible = false;
				float fontScale = getContext().getResources()
						.getDisplayMetrics().scaledDensity;
				int postTextSize = (int) (textView.getTextSize() / fontScale + 0.5f) - 1;
				if (postTextSize < 1) {
					postTextSize = 1;
				}
				textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, postTextSize);
			} else {
				shouldVisible = true;
			}
		} else {
			shouldVisible = true;
		}
		textView.setVisibility(shouldVisible ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onDismiss() {
		super.onDismiss();
		if (mRegisted) {
			mRegisted = false;
			try {
				GlobalObservableSupport.getHomeObservable().unregisterObserver(
						mHomeObserver);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onInitDialog() {
		super.onInitDialog();
		onInitFocusView();
	}

	/**
	 * 初始化需要方控控制的焦点列表
	 */
	protected abstract void onInitFocusView();

	private boolean mRegisted = false;
	private HomeObserver mHomeObserver = new HomeObserver() {
		@Override
		public void onHomePressed() {
			WinMessageBox.this.doReport(REPORT_ACTION_TYPE_HOME);
			dismissInner();
		}
	};

	private ReverseObservable.ReverseObserver mRevereObserver=new ReverseObservable.ReverseObserver(){
		@Override
		public void onReversePressed() {
			WinMessageBox.this.doReport(REPORT_ACTION_TYPE_DISMISS);
			dismissInner();
		}
	};

	@Override
	public void onShow() {
		if (!mRegisted) {
			mRegisted = true;
			try {
				GlobalObservableSupport.getHomeObservable().registerObserver(
						mHomeObserver);
				//添加倒车事件的监听
				GlobalObservableSupport.getRevereObservable().registerObserver(
						mRevereObserver);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};


	/**
	 * 获取调试字符串
	 * 
	 * @return
	 */
	public String getDebugString() {
		return this.toString() + "["
				+ this.mWinMessageBoxBuildData.mMessageText + "]";
	}

	/**
	 * 获取对话框的视图上报的ID
	 * 
	 * @return
	 */
	@Override
	public String getReportViewId(int viewId) {
		if (viewId == R.id.frmMessageBox_Blank) {
			return REPORT_ACTION_PARAM_CLICK_BLANK;
		} else if (viewId == R.id.btnMessageBox_Button1) {
			return "left";
		} else if (viewId == R.id.btnMessageBox_Button2) {
			return "right";
		} else if (viewId == R.id.btnMessageBox_Button3) {
			return "mid";
		}
		return super.getReportViewId(viewId);
	}
}
