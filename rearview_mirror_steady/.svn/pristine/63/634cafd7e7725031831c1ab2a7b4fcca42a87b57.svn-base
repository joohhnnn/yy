package com.txznet.comm.ui.layout.layout1;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.layout.IWinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;

/**
 * 横屏布局形式
 */
public class TXZWinLayout2Impl extends IWinLayout {
	private LinearLayout.LayoutParams mParams;
	
	private FrameLayout mContentView;
	private LayoutParams mContentParams;
	

	/**
	 * 聊天形式的内容
	 */
	private ChatContentView mChatContent;
	private FrameLayout.LayoutParams mChatContentParams;
	/**
	 * 全屏形式的内容
	 */
	private FullContentView mFullContent;
	private FrameLayout.LayoutParams mFullContentParams;
	private FrameLayout mRecoderContent;
	private LayoutParams mRecoderContentParams;
	/**
	 * 广告内容
	 */
	private LinearLayout mBannerAdvertisingLayout;
	
	public static int weightRecord;
	public static int weightContent;

	private int mContentMode = IWinLayout.CONTENT_MODE_CHAT;

	public static void initWeight() {
		weightRecord = ConfigUtil.getRecordWeight();
		weightContent = ConfigUtil.getContentWeight();
		LogUtil.logd("initWeight:" + weightRecord + "," + weightContent);
	}
	

	@Override
	public Object addView(int targetView, final View view,ViewGroup.LayoutParams layoutParams) {
		switch (targetView) {
		case RecordWinController.TARGET_CONTENT_CHAT:
			mContentMode = IWinLayout.CONTENT_MODE_CHAT;
			mChatContent.get().setVisibility(View.VISIBLE);
			mFullContent.get().setVisibility(View.GONE);
			mFullContent.reset();
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
			break;
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
			LogUtil.logd("init weightRecord:" + weightRecord + ",weightContent:" + weightContent);
			// 初始化一些配置等
			mRootLayout = new LinearLayout(GlobalContext.get());
			mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			mRootLayout.setLayoutParams(mParams);
			mRootLayout.setOrientation(LinearLayout.HORIZONTAL);
			mRootLayout.setWeightSum(weightRecord + weightContent);

			// 声控
			mRecoderContent = new FrameLayout(GlobalContext.get());
			mRecoderContentParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weightRecord);
			mRootLayout.addView(mRecoderContent, mRecoderContentParams);

			// 内容
			mContentView = new FrameLayout(GlobalContext.get());
			mContentParams = new LayoutParams(0, LayoutParams.MATCH_PARENT, weightContent);
			mContentView.setPadding((int) LayouUtil.getDimen("x24"), 0, (int) LayouUtil.getDimen("x24"), 0);

			// 聊天内容
			mChatContent = new ChatContentView(GlobalContext.get());
			mChatContentParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			mContentView.addView(mChatContent.get(), mChatContentParams);

			// 全屏显示的内容
			mFullContent = new FullContentView(GlobalContext.get());
			mFullContentParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.MATCH_PARENT);
			mContentView.addView(mFullContent.get(), mFullContentParams);

			//banner广告
			mBannerAdvertisingLayout = new LinearLayout(GlobalContext.get());
			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
					FrameLayout.LayoutParams.WRAP_CONTENT);
			params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM;
			mBannerAdvertisingLayout.setLayoutParams(params);
			mBannerAdvertisingLayout.setVisibility(View.GONE);
			mContentView.addView(mBannerAdvertisingLayout);

			mRootLayout.addView(mContentView, mContentParams);
		}else {
			if (mContentView != null) {
				mContentView.setPadding((int)LayouUtil.getDimen("x24"), 0, (int)LayouUtil.getDimen("x24"), 0);
			}
		}
	}
	

	@Override
	public void reset() {
		if (mFullContent != null && mChatContent != null) {
			mFullContent.reset();
			mChatContent.reset();
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
	public Object removeLastView() {
		mChatContent.removeLastView();
		return null;
	}

	@Override
	public void release() {
		
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
