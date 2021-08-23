package com.txznet.comm.ui.theme.test.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.ConfigUtil.IconStateChangeListener;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.recordwin.RecordWin2;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.VersionManager;
import com.txznet.comm.ui.theme.test.rec.RecStepView;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IRecordView;
import com.txznet.record.setting.MainActivity;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;

public class RecordView extends IRecordView {

	private static RecordView sInstance = new RecordView();
	private Object mAccessLock = new Object();
	private RecStepView mRecStepView;
	private ImageView ivSetting;
	private ImageView ivSettingTop;
	private ImageView ivHelp;
	private ImageView ivHelpNewTag;
	private ImageView ivClose;
	private RelativeLayout rlRecord;

	private RecordView() {
	}

	public static RecordView getInstance() {
		return sInstance;
	}

	@Override
	public void release() {
		synchronized (mAccessLock) {
			if (mRecStepView != null) {
				mRecStepView.release();
				mRecStepView = null;
			}
			ivSetting = null;
			ivSettingTop = null;
			ivHelp = null;
			ivHelpNewTag = null;
			ivClose = null;
			rlRecord = null;
		}
		super.release();
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		release();
		ViewAdapter viewAdapter = new ViewAdapter();
		synchronized (mAccessLock) {
			createRecordView();
			if (data.getType() == ViewData.TYPE_BOTTOM_RECORD_VIEW) {
				initBottomParams();
			}else {
				initLeftParams();
			}
			viewAdapter.view = rlRecord;
			viewAdapter.object = RecordView.getInstance();
		}
		return viewAdapter;
	}
	
	private void createRecordView(){
		rlRecord = new RelativeLayout(GlobalContext.get());
		mRecStepView = new RecStepView(GlobalContext.get());
		ivClose = new ImageView(GlobalContext.get());
		ivClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_CLOSE, 0, 0);
			}
		});
		ivHelpNewTag = new ImageView(GlobalContext.get());
		ivHelpNewTag.setId(ViewUtils.generateViewId());
		ivHelpNewTag.setVisibility(View.VISIBLE);
		ivHelpNewTag.setImageDrawable(LayouUtil.getDrawable("ic_help_new_tag"));
		
		ivHelp = new ImageView(GlobalContext.get());
		ivHelp.setId(ViewUtils.generateViewId());
		ivHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (VersionManager.getInstance().isUseHelpNewTag()) {
					ConfigUtil.setShowHelpNewTag(false);
				}
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_HELP, 0, 0);
			}
		});
		ivClose.setVisibility(View.GONE);
		ivClose.setImageDrawable(LayouUtil.getDrawable("ic_close"));
		ivHelp.setVisibility(View.VISIBLE);
		ivHelp.setImageDrawable(LayouUtil.getDrawable("question_mark"));
		ivSettingTop = new ImageView(GlobalContext.get());
		ivSettingTop.setImageDrawable(LayouUtil.getDrawable("ic_setting"));
		ivSettingTop.setVisibility(View.GONE);
		ivSetting = new ImageView(GlobalContext.get());
		ivSetting.setImageDrawable(LayouUtil.getDrawable("ic_setting"));
		ivSetting.setVisibility(View.GONE);
		View.OnClickListener clickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_SETTING, 0, 0);
			}
		};
		
		ivSetting.setOnClickListener(clickListener);
		ivSettingTop.setOnClickListener(clickListener);
		
		mRecStepView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LogUtil.logd("clockRecord");
				RecordWin2Manager.getInstance().operateView(RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_RECORD, 0, 0);
			}
		});
		
		if (ConfigUtil.isShowCloseIcon()) {
			ivClose.setVisibility(View.VISIBLE);
			if (ConfigUtil.isShowSettings()) {
				ivSettingTop.setVisibility(View.GONE);
				ivSetting.setVisibility(View.VISIBLE);
			} else {
				ivSettingTop.setVisibility(View.GONE);
				ivSetting.setVisibility(View.GONE);
			}
		} else {
			ivClose.setVisibility(View.GONE);
			if (ConfigUtil.isShowSettings()) {
				ivSettingTop.setVisibility(View.VISIBLE);
				ivSetting.setVisibility(View.GONE);
			} else {
				ivSettingTop.setVisibility(View.GONE);
				ivSetting.setVisibility(View.GONE);
			}
		}
		
		if(ConfigUtil.isShowHelpInfos()) {
			ivHelp.setVisibility(View.VISIBLE);
			if (VersionManager.getInstance().isUseHelpNewTag()) {
				if (ConfigUtil.isShowHelpNewTag()) {
					ivHelpNewTag.setVisibility(View.VISIBLE);
				}else {
					ivHelpNewTag.setVisibility(View.GONE);
				}
			}else {
				ivHelpNewTag.setVisibility(View.GONE);
			}
		} else {
			ivHelp.setVisibility(View.GONE);
			ivHelpNewTag.setVisibility(View.GONE);
		}

	}
	
	private void initLeftParams(){
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				(int) LayouUtil.getDimen("x300"),
				RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		rlRecord.addView(mRecStepView, layoutParams);

		layoutParams = new RelativeLayout.LayoutParams(
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_WIDTH),
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_HEIGHT));
		layoutParams.leftMargin = 40;
		layoutParams.topMargin = (int) LayouUtil.getDimen("y24");
		rlRecord.addView(ivClose, layoutParams);

		layoutParams = new RelativeLayout.LayoutParams(
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_WIDTH),
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_HEIGHT));
		layoutParams.leftMargin = 40;
		layoutParams.bottomMargin = (int) LayouUtil.getDimen("y24");
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		rlRecord.addView(ivHelp, layoutParams);
		
		layoutParams = new RelativeLayout.LayoutParams(
				(int) (ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_WIDTH)/2.6),
				(int) (ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_HEIGHT)/2.6));
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		layoutParams.bottomMargin = (int) LayouUtil.getDimen("y24")+(int) ThemeConfigManager
				.getY(ThemeConfigManager.HELP_ICON_HEIGHT)/3*2;
		layoutParams.leftMargin = 40 + (int) ThemeConfigManager
				.getY(ThemeConfigManager.HELP_ICON_WIDTH)/3*2;
		
		rlRecord.addView(ivHelpNewTag, layoutParams);
		
		
		layoutParams = new RelativeLayout.LayoutParams(
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_WIDTH),
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_HEIGHT));
		layoutParams.leftMargin = 40;
		layoutParams.topMargin = (int) LayouUtil.getDimen("y24");
		rlRecord.addView(ivSettingTop,layoutParams);
		
		layoutParams = new RelativeLayout.LayoutParams(
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_WIDTH),
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_HEIGHT));
		layoutParams.leftMargin = 40;
		layoutParams.bottomMargin = (int) LayouUtil.getDimen("y24");
		layoutParams.addRule(RelativeLayout.ABOVE, ivHelp.getId());
		rlRecord.addView(ivSetting,layoutParams);
		
	}

	
	private void initBottomParams(){
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				(int) LayouUtil.getDimen("x300"),
				RelativeLayout.LayoutParams.MATCH_PARENT);
		layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
		rlRecord.addView(mRecStepView, layoutParams);

		layoutParams = new RelativeLayout.LayoutParams(
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_WIDTH),
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_HEIGHT));
		layoutParams.leftMargin = 30;
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rlRecord.addView(ivClose, layoutParams);

		layoutParams = new RelativeLayout.LayoutParams(
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_WIDTH),
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_HEIGHT));
		layoutParams.rightMargin = 30;
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rlRecord.addView(ivHelp, layoutParams);
		
		layoutParams = new RelativeLayout.LayoutParams(
				(int) (ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_WIDTH)/2.6),
				(int) (ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_HEIGHT)/2.6));
		layoutParams.rightMargin = 30 - (int) ThemeConfigManager
				.getY(ThemeConfigManager.HELP_ICON_WIDTH)/4;
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		layoutParams.addRule(RelativeLayout.ABOVE, ivHelp.getId());
		layoutParams.bottomMargin = - (int) ThemeConfigManager
				.getY(ThemeConfigManager.HELP_ICON_WIDTH)/3;
		
		rlRecord.addView(ivHelpNewTag, layoutParams);

		layoutParams = new RelativeLayout.LayoutParams(
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_WIDTH),
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_HEIGHT));
		layoutParams.leftMargin = 30;
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		rlRecord.addView(ivSettingTop,layoutParams);
		
		layoutParams = new RelativeLayout.LayoutParams(
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_WIDTH),
				(int) ThemeConfigManager
						.getY(ThemeConfigManager.HELP_ICON_HEIGHT));
		layoutParams.rightMargin = 30;
		layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
		layoutParams.addRule(RelativeLayout.LEFT_OF, ivHelp.getId());
		rlRecord.addView(ivSetting,layoutParams);
	}
	
	@Override
	public void init() {
		ConfigUtil.registerIconStateChangeListener(new IconStateChangeListener() {
			@Override
			public void onStateChanged(int type, boolean enable) {
				UI2Manager.runOnUIThread(new Runnable() {
					@Override
					public void run() {
						if (ivClose == null || ivSetting == null || ivSettingTop == null || ivHelp == null || ivHelpNewTag == null) {
							return;
						}
						if (ConfigUtil.isShowCloseIcon()) {
							ivClose.setVisibility(View.VISIBLE );
							if (ConfigUtil.isShowSettings()) {
								ivSettingTop.setVisibility(View.GONE);
								ivSetting.setVisibility(View.VISIBLE);
							}else {
								ivSettingTop.setVisibility(View.GONE);
								ivSetting.setVisibility(View.GONE);
							}
						}else {
							ivClose.setVisibility(View.GONE );
							if (ConfigUtil.isShowSettings()) {
								ivSettingTop.setVisibility(View.VISIBLE);
								ivSetting.setVisibility(View.GONE);
							}else {
								ivSettingTop.setVisibility(View.GONE);
								ivSetting.setVisibility(View.GONE);
							}
						}
						//需要延时500毫秒
						rlRecord.removeCallbacks(updateHelpNew);
						rlRecord.postDelayed(updateHelpNew, 500);
						if(ConfigUtil.isShowHelpInfos()) {
							ivHelp.setVisibility(View.VISIBLE);
						} else {
							ivHelp.setVisibility(View.GONE);
							ivHelpNewTag.setVisibility(View.GONE);
						}
					}
				}, 0);
			}
		} );
	
		if (GlobalContext.isTXZ()) {
			GlobalContext.get().registerReceiver(new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					Intent startRecordSetting = new Intent(GlobalContext.get(), MainActivity.class);
					startRecordSetting.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					GlobalContext.get().startActivity(startRecordSetting);
					RecordWin2.getInstance().dismiss();	
				}
			}, new IntentFilter("com.txznet.action.openSetting"));
		}
		
	}
	
	private Runnable updateHelpNew = new Runnable() {
		
		@Override
		public void run() {
			if (ivHelpNewTag != null) {
				if(ConfigUtil.isShowHelpInfos()) {
					if (VersionManager.getInstance().isUseHelpNewTag()) {
						if (ConfigUtil.isShowHelpNewTag()) {
							ivHelpNewTag.setVisibility(View.VISIBLE);
						} else {
							ivHelpNewTag.setVisibility(View.GONE);
						}
					} else {
						ivHelpNewTag.setVisibility(View.GONE);
					}
				}else {
					ivHelpNewTag.setVisibility(View.GONE);
				}
			}
		}
	};

	@Override
	public void updateState(int state) {
		LogUtil.logd("updateState " + state);
		switch (state) {
		case STATE_RECORD_START:
			// 录音开始
			if (mRecStepView != null) {
				mRecStepView.onRecord();
			}
			break;
		case STATE_RECORD_END:
			// 处理中
			if (mRecStepView != null) {
				mRecStepView.onLoading();
			}
			break;
		default:
			// 正常状态
			if (mRecStepView != null) {
				mRecStepView.onStart();
			}
			break;
		}

	}

	@Override
	public void updateVolume(int volume) {
//		LogUtil.logd("updateVolume " + volume);
		if (mRecStepView != null) {
			mRecStepView.updateVol(volume);
		}
	}



}
