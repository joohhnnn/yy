package com.txznet.comm.ui.dialog;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable.HomeObserver;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.sdk.TXZConfigManager.InitParam;
import com.txznet.sdk.TXZWheelControlEvent;
import com.txznet.sdk.TXZWheelControlManager;
import com.txznet.sdk.TXZWheelControlManager.OnTXZWheelControlListener;
import com.txznet.txz.comm.R;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.runnables.Runnable1;

import android.annotation.SuppressLint;
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

public class WinMessageBox extends WinDialog implements OnClickListener {
	public WinMessageBox() {
		super();
	}

	public WinMessageBox(boolean isSystem) {
		super(isSystem);
	}

	public static class ViewHolder {
		public View mBlank;
		public View mContent;
		public TextView mTitle;
		public TextView mText;
		public ListView mTextList;
		public ScrollView mScrollView;
		public TextView mScrollText;
		public Button mLeft;
		public Button mMid;
		public Button mRight;
	}

	protected ViewHolder mViewHolder;

	protected boolean mAllowScroll = false;
	
	private static int sMessageDialogType = InitParam.MESSAGE_DIALOG_TYPE_NORMAL;
	
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

	public void setTextScroll(boolean scroll) {
		if (mAllowScroll != scroll) {
			String curMsg = mAllowScroll ? mViewHolder.mScrollText.getText() + "" : mViewHolder.mText.getText() + "";
			mAllowScroll = scroll;
			if (!TextUtils.isEmpty(curMsg)) {
				setMessage(curMsg);
			}
		}
	}

	protected WinMessageBox setTitle(String s) {
		mViewHolder.mTitle.setVisibility(View.VISIBLE);
		mViewHolder.mTitle.setText(LanguageConvertor.toLocale(s));
		return this;
	}

	protected WinMessageBox setMessage(String s) {
		if (mAllowScroll) {
			mViewHolder.mText.setText("");
			mViewHolder.mText.setVisibility(View.GONE);
			mViewHolder.mScrollView.setVisibility(View.VISIBLE);
			mViewHolder.mScrollText.setVisibility(View.VISIBLE);
			mViewHolder.mScrollText.setText(LanguageConvertor.toLocale(s));
		} else {
			mViewHolder.mScrollText.setText("");
			mViewHolder.mScrollText.setVisibility(View.GONE);
			mViewHolder.mScrollView.setVisibility(View.GONE);
			mViewHolder.mText.setVisibility(View.VISIBLE);
			mViewHolder.mText.setText(LanguageConvertor.toLocale(s));
		}
		return this;
	}

	protected WinMessageBox setLeftButton(String s) {
		setButtonText(mViewHolder.mLeft, s);
		return this;
	}

	protected WinMessageBox setMidButton(String s) {
		setButtonText(mViewHolder.mMid, s);
		return this;
	}

	protected WinMessageBox setRightButton(String s) {
		setButtonText(mViewHolder.mRight, s);
		return this;
	}

	protected void setButtonText(Button bt, String s) {
		if (s != null && s.length() > 0) {
			bt.setVisibility(View.VISIBLE);
			bt.setText(LanguageConvertor.toLocale(s));
		} else {
			bt.setText("");
			bt.setVisibility(View.GONE);
		}
		checkButtonCount();
	}
	
	/**
	 * 按钮只有一个时的背景图跟有多个时的背景图不同
	 */
	@SuppressLint("NewApi")
	protected void checkButtonCount() {
		int count = 0;
		if (mViewHolder.mLeft.getVisibility() == View.VISIBLE)
			++count;
		if (mViewHolder.mMid.getVisibility() == View.VISIBLE)
			++count;
		if (mViewHolder.mRight.getVisibility() == View.VISIBLE)
			++count;
		if (count == 1) {
			if (mViewHolder.mLeft.getVisibility() == View.VISIBLE) {
				mViewHolder.mLeft.setBackground(
						GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_single));
			}
			if (mViewHolder.mMid.getVisibility() == View.VISIBLE) {
				mViewHolder.mMid.setBackground(
						GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_single));
			}
			if (mViewHolder.mRight.getVisibility() == View.VISIBLE) {
				mViewHolder.mRight.setBackground(
						GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_single));
			}
		} else {
			mViewHolder.mLeft.setBackground(
					GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_left));
			mViewHolder.mMid.setBackground(
					GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_middle));
			mViewHolder.mRight.setBackground(
					GlobalContext.get().getResources().getDrawable(R.drawable.comm_win_messagebox_btn_bg_right));
		}
	}

	public void onClickLeft() {

	}

	public void onClickMid() {

	}

	public void onClickRight() {

	}

	public void onClickBlank() {

	}

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
		mViewHolder.mTitle = (TextView) context.findViewById(R.id.txtMessageBox_Title);
		mViewHolder.mText = (TextView) context.findViewById(R.id.txtMessageBox_Message);
		mViewHolder.mTextList = (ListView) context.findViewById(R.id.lvMessageBox_Message);
		mViewHolder.mScrollView = (ScrollView) context.findViewById(R.id.slMessageBox_Scroll);
		mViewHolder.mScrollText = (TextView) context.findViewById(R.id.txtMessageBox_Scroll_Message);
		mViewHolder.mLeft = (Button) context.findViewById(R.id.btnMessageBox_Button1);
		mViewHolder.mMid = (Button) context.findViewById(R.id.btnMessageBox_Button3);
		mViewHolder.mRight = (Button) context.findViewById(R.id.btnMessageBox_Button2);
		initView();
		return context;
	}
	
	private boolean mHasSetBackground;

	private void initView() {
		TextViewUtil.setTextSize(mViewHolder.mTitle, ViewConfiger.getInstance().getSize(MESSAGE_TITLE_SIZE));
		TextViewUtil.setTextSize(mViewHolder.mText, ViewConfiger.getInstance().getSize(MESSAGE_MESSAGE_SIZE));
		TextViewUtil.setTextSize(mViewHolder.mScrollText, ViewConfiger.getInstance().getSize(MESSAGE_SCROLL_SIZE));
		mViewHolder.mLeft.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				ViewConfiger.getInstance().getSize(MESSAGE_BTN_LEFT_SIZE));
		mViewHolder.mMid.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				ViewConfiger.getInstance().getSize(MESSAGE_BTN_MIDDLE_SIZE));
		mViewHolder.mRight.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				ViewConfiger.getInstance().getSize(MESSAGE_BTN_RIGHT_SIZE));
		mViewHolder.mLeft.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!mHasSetBackground) {
					mViewHolder.mContent.setBackgroundColor(Color.parseColor("#444a51"));
					mHasSetBackground = true;
				} else {
					mViewHolder.mContent.setBackgroundColor(Color.parseColor("#444a50"));
					mHasSetBackground = false;
				}
			}
		});
		mViewHolder.mMid.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!mHasSetBackground) {
					mViewHolder.mContent.setBackgroundColor(Color.parseColor("#444a51"));
					mHasSetBackground = true;
				} else {
					mViewHolder.mContent.setBackgroundColor(Color.parseColor("#444a50"));
					mHasSetBackground = false;
				}
			}
		});
		mViewHolder.mRight.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!mHasSetBackground) {
					mViewHolder.mContent.setBackgroundColor(Color.parseColor("#444a51"));
					mHasSetBackground = true;
				} else {
					mViewHolder.mContent.setBackgroundColor(Color.parseColor("#444a50"));
					mHasSetBackground = false;
				}
			}
		});
		mViewHolder.mBlank.setOnClickListener(this);
		mViewHolder.mLeft.setOnClickListener(this);
		mViewHolder.mMid.setOnClickListener(this);
		mViewHolder.mRight.setOnClickListener(this);
		mViewHolder.mText.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				autofitTextView(mViewHolder.mText);
				return true;
			}
		});
	}
	
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.frmMessageBox_Blank) {
			onClickBlank();
		} else if (v.getId() == R.id.btnMessageBox_Button1) {
			onClickLeft();
		} else if (v.getId() == R.id.btnMessageBox_Button2) {
			onClickRight();
		} else if (v.getId() == R.id.btnMessageBox_Button3) {
			onClickMid();
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
				float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
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

	Object mMessageData;

	protected WinMessageBox setMessageData(Object data) {
		mMessageData = data;
		return this;
	}

	public Object getMessageData() {
		return mMessageData;
	}

	public <T> T getMessageData(Class<T> cls) {
		return (T) mMessageData;
	}

	protected View[] m_focus;
	protected int mFocusPosition = -1;

	protected void updateFocus(){
		if (m_focus == null || m_focus.length == 0) {
			LogUtil.loge("WinDialog mFocusViews empty");
			return;
		}
		if(mFocusPosition>m_focus.length){
			LogUtil.loge("WinDialog mFocusPosition out of range.mFocusPosition:" + mFocusPosition + ",focus size:"
					+ m_focus.length);
			return;
		}
		LogUtil.logd("update focus :" + m_focus.length);
		for (int i = 0; i < m_focus.length; i++) {
			m_focus[i].post(new Runnable1<Integer>(i) {
				@Override
				public void run() {
					m_focus[mP1].setFocusable(mFocusPosition == mP1);
					m_focus[mP1].setFocusableInTouchMode(mFocusPosition == mP1);
					if (mP1 == mFocusPosition) {
						m_focus[mP1].requestFocus();
					}
				}
			});
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (mFocusPosition > 0 && mFocusPosition < m_focus.length) {
				mFocusPosition--;
			} else {
				mFocusPosition = m_focus.length - 1;
			}
			updateFocus();
			return true;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (mFocusPosition >= 0 && mFocusPosition < m_focus.length - 1) {
				mFocusPosition++;
			} else {
				mFocusPosition = 0;
			}
			updateFocus();
			return true;
		case KeyEvent.KEYCODE_DPAD_CENTER:
		case KeyEvent.KEYCODE_ENTER:
			if (mFocusPosition >= 0 && mFocusPosition < m_focus.length) {
				m_focus[mFocusPosition].post(new Runnable1<Integer>(mFocusPosition) {
					@Override
					public void run() {
						m_focus[mFocusPosition].performClick();
					}
				});
			}
			return true;
		case KeyEvent.KEYCODE_BACK:
			onBackPressed();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void dismiss() {
		super.dismiss();
		if(mRegisted){
			mRegisted = false;
			try {
				GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeObserver);
				TXZWheelControlManager.getInstance().unregisterWheelControlListener(onTXZWheelControlListener);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// show 的时候会reset一次
		// for (int i = 0; i < m_focus_pos.length; i++) {
		// if (m_focus_pos[i] == 1) {
		// m_focus[i].setFocusable(false);
		// m_focus_pos[i] = 0;
		// }
		// }
	}
	
	private boolean mRegisted = false;
	private HomeObserver mHomeObserver = new HomeObserver() {
		@Override
		public void onHomePressed() {
			dismiss();
		}
	};
	
	@Override
	public void show() {
		super.show();
		if (!mRegisted) {
			mRegisted = true;
			try {
				GlobalObservableSupport.getHomeObservable().registerObserver(mHomeObserver);
				TXZWheelControlManager.getInstance().registerWheelControlListener(onTXZWheelControlListener);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		initFocus();
		updateFocus();
	};
	
	
	protected void initFocus(){
		mFocusPosition = -1;
	}
	
	OnTXZWheelControlListener onTXZWheelControlListener = new OnTXZWheelControlListener() {
		@Override
		public void onKeyEvent(int eventId) {
			switch (eventId) {
			case TXZWheelControlEvent.LEVOROTATION_EVENTID:
			case TXZWheelControlEvent.LEFT_KEY_CLICKED_EVENTID:
				onKeyUp(KeyEvent.KEYCODE_DPAD_LEFT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_LEFT));
				break;
			case TXZWheelControlEvent.DEXTROROTATION_EVENTID:
			case TXZWheelControlEvent.RIGHT_KEY_CLICKED_EVENTID:
				onKeyUp(KeyEvent.KEYCODE_DPAD_RIGHT, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_RIGHT));
				break;
			case TXZWheelControlEvent.OK_KEY_CLICKED_EVENTID:
				onKeyUp(KeyEvent.KEYCODE_DPAD_CENTER, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DPAD_CENTER));
				break;
			case TXZWheelControlEvent.BACK_KEY_CLICKED_EVENTID:
				onKeyUp(KeyEvent.KEYCODE_BACK, new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
			default:
				break;
			}
		}
	};
}
