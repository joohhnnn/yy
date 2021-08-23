package com.txznet.comm.ui.layout.layout1;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;

/**
 * 竖屏布局
 * 
 */
public class TXZWinLayout1Impl extends IWinLayout {
	private LinearLayout.LayoutParams mParams;
	
	private RelativeLayout mContentView;
	private LayoutParams mContentParams;
	

	/**
	 * 聊天形式的内容
	 */
	private ChatContentView mChatContent;
	private RelativeLayout.LayoutParams mChatContentParams;
	/**
	 * 全屏形式的内容
	 */
	private FullContentView mFullContent;
	private RelativeLayout.LayoutParams mFullContentParams;
	private FrameLayout mRecoderContent;
	private LayoutParams mRecoderContentParams;
	/**
	 * 广告内容
	 */
	private LinearLayout mBannerAdvertisingLayout;
	
	// public static Integer sWeightRecord = null;
	// public static Integer sWeightContent = null;
	// public static void initWeight() {
	// LogUtil.logd("initWeight:" + ConfigUtil.getRecordWeight() + "," +
	// ConfigUtil.getContentWeight());
	// if (ConfigUtil.getRecordWeight() != null && ConfigUtil.getContentWeight()
	// != null) {
	// sWeightRecord = ConfigUtil.getRecordWeight();
	// sWeightContent = ConfigUtil.getContentWeight();
	// }
	// }
	private int mContentMode = IWinLayout.CONTENT_MODE_CHAT;

	public static Integer sRecordHeight = null;
	public static void initHeight() {
		LogUtil.logd("initHeight:" + ConfigUtil.getRecordHeight());
		if (ConfigUtil.getRecordHeight() != null) {
			sRecordHeight = ConfigUtil.getRecordHeight();
		}
	}
	
	
	@Override
	public Object addView(int targetView, final View view,ViewGroup.LayoutParams layoutParams) {
		switch (targetView) {
		case RecordWinController.TARGET_CONTENT_CHAT:
			mContentMode = IWinLayout.CONTENT_MODE_CHAT;
			mChatContent.get().setVisibility(View.VISIBLE);
			mFullContent.get().setVisibility(View.GONE);
			mChatContent.addView(view);
			break;
		case RecordWinController.TARGET_CONTENT_FULL:
			mContentMode = IWinLayout.CONTENT_MODE_FULL;
			mFullContent.reset();
			mFullContent.addView(view);
			mChatContent.get().setVisibility(View.GONE);
			mFullContent.get().setVisibility(View.VISIBLE);
			break;
		case RecordWinController.TARGET_VIEW_MIC:
			mRecoderContent.removeAllViews();
			mRecoderContent.addView(view);
		case RecordWinController.TARGET_VIEW_BANNER_AD:
			mBannerAdvertisingLayout.removeAllViews();
			mBannerAdvertisingLayout.setVisibility(View.VISIBLE);
			mBannerAdvertisingLayout.addView(view);
			break;
		default:
			break;
		}
		return null;
	}
	
	
	@Override
	public void init() {
		super.init();
		if(mRootLayout==null){
			// 初始化一些配置等
			mRootLayout = new LinearLayout(GlobalContext.get());
			mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			mRootLayout.setLayoutParams(mParams);
			mRootLayout.setOrientation(LinearLayout.VERTICAL);

			// 内容
			mContentView = new RelativeLayout(GlobalContext.get());
			mContentParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1);
			mContentView.setPadding((int) LayouUtil.getDimen("x24"), 0, (int) LayouUtil.getDimen("x24"), 0);

			// 聊天内容
			mChatContent = new ChatContentView(GlobalContext.get());
			mChatContentParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
			mContentView.addView(mChatContent.get(), mChatContentParams);
			// 全屏显示的内容
			mFullContent = new FullContentView(GlobalContext.get());
			mFullContentParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
					RelativeLayout.LayoutParams.MATCH_PARENT);
			mContentView.addView(mFullContent.get(), mFullContentParams);

			//banner广告
			mBannerAdvertisingLayout = new LinearLayout(GlobalContext.get());
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
			mBannerAdvertisingLayout.setLayoutParams(params);
			mBannerAdvertisingLayout.setVisibility(View.GONE);
			mContentView.addView(mBannerAdvertisingLayout);

			mRootLayout.addView(mContentView, mContentParams);

			// 声控
			mRecoderContent = new FrameLayout(GlobalContext.get());
			if (sRecordHeight != null) {
				mRecoderContentParams = new LayoutParams(LayoutParams.MATCH_PARENT, sRecordHeight);
			} else {
				mRecoderContentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
						(int) LayouUtil.getDimen("y80"));
			}
			mRootLayout.addView(mRecoderContent, mRecoderContentParams);
		}else {
			if (mContentView != null) {
				mContentView.setPadding((int)LayouUtil.getDimen("x24"), 0, (int)LayouUtil.getDimen("x24"), 0);
			}
		}
	}

	@Override
	public void reset() {
		if (mFullContent != null && mChatContent != null) {
			UI2Manager.runOnUIThread(new Runnable() {
				@Override
				public void run() {
					mFullContent.reset();
					mChatContent.reset();
				}
			}, 0);
		}
	}

	@Override
	public void addRecordView(View recordView) {
		if (mRecoderContent != null) {
			mRecoderContent.removeAllViews();
			mRecoderContent.addView(recordView);
		}
	}


	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object removeLastView() {
		mChatContent.removeLastView();
		return null;
	}

	@Override
	public void updateContentMode(int mode) {
		if (mode != mContentMode) {
			mContentMode = mode;
			switch (mode) {
				case IWinLayout.CONTENT_MODE_CHAT:
					mChatContent.get().setVisibility(View.VISIBLE);
					mFullContent.get().setVisibility(View.GONE);
					break;
				case IWinLayout.CONTENT_MODE_FULL:
					mFullContent.reset();
					mChatContent.get().setVisibility(View.GONE);
					mFullContent.get().setVisibility(View.VISIBLE);
					break;
			}
		}
	}

	@Override
	public void setBackground(Drawable drawable) {
		get().setBackgroundDrawable(drawable);
	}

	@Override
	public void setBannerAdvertisingView(View view) {
		addView(RecordWinController.TARGET_VIEW_BANNER_AD,view);
	}

	@Override
	public void removeBannerAdvertisingView() {
		if(mBannerAdvertisingLayout != null){
			mBannerAdvertisingLayout.removeAllViews();
			mBannerAdvertisingLayout.setVisibility(View.GONE);
		}
	}
}
