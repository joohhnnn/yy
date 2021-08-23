package com.txznet.comm.ui.theme.test.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.anim.AnimFullView;
import com.txznet.comm.ui.theme.test.anim.AnimHalfView;
import com.txznet.comm.ui.theme.test.anim.AnimNoneView;
import com.txznet.comm.ui.theme.test.anim.AnimVerticalView;
import com.txznet.comm.ui.theme.test.anim.IImageView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IRecordView;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZRecordWinManager.RecordWin2.RecordWinController;

public class RecordView extends IRecordView {

	private static RecordView sInstance = new RecordView();

	private IImageView animationView;

	private String TAG = "RecordView";

	private RecordView() {
	}

	public static RecordView getInstance() {
		return sInstance;
	}

	@Override
	public void release() {
		super.release();
	}

	@Override
	public ViewAdapter getView(ViewData data) {
		LogUtil.logd(WinLayout.logTag+ "RecordView getView");
		ViewAdapter viewAdapter = new ViewAdapter();
		viewAdapter.view = animationView;
		viewAdapter.object = RecordView.getInstance();
		viewAdapter.isFullContent = false;
		return viewAdapter;
	}

	Runnable normalRun = null;
	Runnable recordRun = null;
	Runnable endRun = null;

	private static int currentState = -1;

	@Override
	public void init() {

		LogUtil.logd(WinLayout.logTag+ "RecordView init");

		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			public void onReceive(Context context, Intent intent) {
				currentState = -1;
			}
		}, new IntentFilter("com.txznet.txz.record.show"));

		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			public void onReceive(Context context, Intent intent) {
				if (animationView != null){
					animationView.playEndAnim(0);
					animationView.playEndAnim(1);
					animationView.playEndAnim(2);
				}

			}
		}, new IntentFilter("com.txznet.txz.record.dismiss"));

		normalRun = new Runnable() {
			@Override
			public void run() {
				//animationView.playEndAnim(2);
				animationView.stopAllAnim();
				animationView.playStartAnim(0);
			}
		};

		recordRun = new Runnable() {
			@Override
			public void run() {
				//animationView.playEndAnim(0);
				animationView.stopAllAnim();
				animationView.playStartAnim(1);
			}
		};

		endRun = new Runnable() {
			@Override
			public void run() {
				//animationView.playEndAnim(1);
				animationView.stopAllAnim();
				animationView.playStartAnim(2);

			}
		};
	}

	@Override
	public void updateState(int state) {
		LogUtil.logd(WinLayout.logTag+ "updateState: "+state+"--currentState:"+currentState);
		// if (currentState == state)
		// 	return;
		currentState = state;
		// 开始新的动画
		try {
			switch (state) {
				case STATE_NORMAL: // 播报
					AppLogicBase.runOnUiGround(normalRun, 0);
					break;
				case STATE_RECORD_START: // 录音
					AppLogicBase.runOnUiGround(recordRun, 0);
					break;
				case STATE_RECORD_END: // 处理
					AppLogicBase.runOnUiGround(endRun, 0);
					break;
                case STATE_WIN_OPEN:    //窗口打开
                    WinLayout.getInstance().onUpdateState();
                    break;
                case STATE_WIN_CLOSE:    //窗口关闭
                    break;
                case STATE_SPEAK_START:    //TTS播报
                    break;
                case STATE_SPEAK_END:    //TTS播报结束
                    break;
				default:
					break;
			}
		}catch (Exception e){

		}

	}

	@Override
	public void updateVolume(int volume) {
	}

	//更新对应模式的动画
	public void onUpdateAnim(){
		LogUtil.logd(WinLayout.logTag+ "onUpdateAnim: ");
		if (animationView != null) {
			animationView.destory();
			animationView = null;
		}
		if (WinLayout.isVertScreen){
            switch (StyleConfig.getInstance().getSelectStyleIndex()) {
                case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                    animationView = new AnimVerticalView(GlobalContext.get());
                    break;
                case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                    animationView = new AnimNoneView(GlobalContext.get());
                    break;
                default:
                    animationView = new AnimVerticalView(GlobalContext.get());
                    break;
            }
        }else {
            switch (StyleConfig.getInstance().getSelectStyleIndex()) {
                case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                    animationView = new AnimFullView(GlobalContext.get());
                    break;
                case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                    animationView = new AnimHalfView(GlobalContext.get());
                    break;
                case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                    animationView = new AnimNoneView(GlobalContext.get());
                    break;
                default:
                    animationView = new AnimFullView(GlobalContext.get());
                    break;
            }
        }

		animationView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				RecordWin2Manager.getInstance().operateView(
						RecordWinController.OPERATE_CLICK,
						RecordWinController.VIEW_RECORD,
						0,0);
			}
		});
	}

}
