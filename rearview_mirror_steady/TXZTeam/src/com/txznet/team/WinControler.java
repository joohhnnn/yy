package com.txznet.team;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.comm.ui.dialog.WinProcessing;
import com.txznet.loader.AppLogic;

public class WinControler {

	private WinProcessing mWinProcessing;
	private View mContentView;
	private ImageView code_iv;
	private TextView static_tv;
	private String mLastUrl; // 上次获取的url
	private boolean mIsBind; // 是否绑定成功
	private boolean mIsFirstRequest = true; // 第一次请求
	private boolean mIsFirstTimeException = true; // 是否首次同步失败
	private String mLastNick;

	private final String DEFAULT_TIPS = "打开微信，扫描左侧的二维码，通过微信远程管理你的车队~";
	private final String BIND_TIPS = "，您可以去公众号管理您的车队了~";
	private final int DEFAULT_SYNC_DELAY = 1000 * 60 * 25; // 25分钟
	private final int EXCEPTION_SYNC_DELAY = 1000 * 3; // 3秒
	private int mSyncDelay = DEFAULT_SYNC_DELAY; // 延时间隔去获取二维码

	private Bitmap mBQCodeBitmap;

	private static WinControler mInstance = new WinControler();

	public static WinControler getInstance() {
		return mInstance;
	}

	public void setContentView(View v) {
		mContentView = v;
		init();
	}

	public View getContentView() {
		return mContentView;
	}

	private void init() {
		code_iv = (ImageView) mContentView.findViewById(R.id.code_iv);
		static_tv = (TextView) mContentView.findViewById(R.id.static_tv);
		code_iv.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (code_iv.getDrawable() == null) {
							return;
						}
						if (code_iv != null && code_iv.getWidth() != 0
								&& code_iv.getHeight() != 0) {
							float width = code_iv.getWidth();
							float height = code_iv.getHeight();
							float imgWidth = code_iv.getDrawable()
									.getIntrinsicWidth();
							float imgHeight = code_iv.getDrawable()
									.getIntrinsicHeight();
							Matrix matrix = new Matrix();
							// 移动到中心点
							matrix.preTranslate(width / 2 - imgWidth / 2,
									height / 2 - imgHeight / 2);
							// 拉伸到最大面积 + 额外
							matrix.postScale(width / imgWidth + 0.15f, height
									/ imgHeight + 0.15f, width / 2, height / 2);
							code_iv.setImageMatrix(matrix);
						}
					}
				});

		// 刷新之前的二维码
		if (mBQCodeBitmap != null) {
			code_iv.setImageBitmap(mBQCodeBitmap);
		}

		// 刷新之前的绑定信息
		if (mIsBind) {
			static_tv.setText(mLastNick + BIND_TIPS);
		} else {
			static_tv.setText(DEFAULT_TIPS);
		}
	}

	/**
	 * 刷新二维码
	 * 
	 * @param url
	 */
	private void refreshBQCode(String url) {
		if (url == null || url.equals(""))
			return;
		mLastUrl = url;
		if (mWinProcessing != null)
			mWinProcessing.dismiss();
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				int w = (int) GlobalContext.get().getResources()
						.getDimension(R.dimen.x260);
				int h = (int) GlobalContext.get().getResources()
						.getDimension(R.dimen.y260);
				Bitmap b = null;
				try {
					b = QRCodeHandler.createQRCode(mLastUrl, w > h ? h : w);
				} catch (WriterException e) {
					LogUtil.loge("create QRCode error!");
				}
				if (b != null) {
					code_iv.setImageBitmap(b);
					mBQCodeBitmap = b;
				}
			}
		}, 0);
	}

	/**
	 * 请求二维码
	 * 
	 * @param delay
	 */
	private void requestBQCode(long delay) {
		AppLogic.removeBackGroundCallback(
				mQrCodeSubscribeTask);
		AppLogic
				.runOnBackGround(mQrCodeSubscribeTask, delay);
	}

	/**
	 * 刷新绑定信息
	 * 
	 * @param isbind
	 * @param nick
	 */
	private void refreshBindStatus(boolean isBing, String nick) {
		mIsBind = isBing;
		mLastNick = nick;
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (mIsBind) {
					static_tv.setText(mLastNick + BIND_TIPS);
				} else {
					static_tv.setText(DEFAULT_TIPS);
				}
			}

		}, 0);
	}

	// 刷新二维码任务
	private Runnable mQrCodeSubscribeTask = new Runnable() {
		@Override
		public void run() {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"team.subscribe.qrcode", null, null);
		}
	};

	/**
	 * 响应获取二维码
	 * 
	 * @param issuccess
	 * @param isBing
	 * @param url
	 * @param nick
	 */
	public void respBQCode(boolean issuccess, boolean isBing, String url,
			String carinfo) {
		if (issuccess) { // 获取成功
			refreshBindStatus(isBing, carinfo);
			refreshBQCode(url);
			// 25分钟后再次同步二维码
			mSyncDelay = DEFAULT_SYNC_DELAY;
			requestBQCode(mSyncDelay);
			mIsFirstRequest = false;
			mIsFirstTimeException = false;
		} else {
			if (mIsFirstRequest) { // 第一次请求失败
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						// 弹出提示框退出
						new WinNotice(false) {
							@Override
							public void onClickOk() {
								mWinProcessing.dismiss();
								AppLogic
										.finishMainActivity();
								this.dismiss();
							}
						}.setMessage("获取绑定信息失败").show();
						mIsFirstTimeException = true;
					}
				}, 0);
			} else { // 第一次请求成功后的请求失败
				// 3秒后再次同步二维码
				mSyncDelay = EXCEPTION_SYNC_DELAY;
				requestBQCode(mSyncDelay);
			}
		}
	}

	/**
	 * 同步二维码
	 */
	public void snycBQCode() {
		if (!mIsFirstTimeException) // 第一次同步成功后不再同步
			return;

		MainActivity m = AppLogic.getMainActivity();
		if (m == null)
			return;

		if (mWinProcessing == null || !mWinProcessing.isShowing()) {
			mWinProcessing = new WinProcessing("正在获取绑定信息") {
				@Override
				public void onCancelProcess() {
					dismiss();
				}
			};
			mWinProcessing.show();
		}

		requestBQCode(0);
	}
}
