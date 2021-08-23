package com.txznet.feedback.ui;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.txznet.feedback.AppLogic;
import com.txznet.feedback.R;
import com.txznet.feedback.service.MsgService;
import com.txznet.feedback.ui.WinRecord.OnRecordListener;

public class FeedBackWin implements OnClickListener,OnRecordListener{
	
	private View mView;
	private View mRedView;
	private ImageButton mRecordBtn;
	private ImageButton mNoticeBtn;
	
	private static FeedBackWin instance = new FeedBackWin();
	
	private FeedBackWin(){ }
	
	public static FeedBackWin getInstance(){
		return instance;
	}
	
	public void init(View view){
		this.mView = view;
		initView();
	}
	
	private void initView(){
		mRedView = mView.findViewById(R.id.red_point);
		mRecordBtn = (ImageButton) mView.findViewById(R.id.record_btn);
		mNoticeBtn = (ImageButton) mView.findViewById(R.id.notice_btn);
		
		mRecordBtn.setOnClickListener(this);
		mNoticeBtn.setOnClickListener(this);
		
		// 初始化是否显示RedPoint
		setShowRedPoint(MsgService.getInstance().isNewIn());
	}
	
	/**
	 * 设置RedPoint的显示
	 * @param isShow
	 */
	public void setShowRedPoint(boolean isShow){
		if(mRedView == null){
			return ;
		}
		
		if(isShow){
			mRedView.setVisibility(View.VISIBLE);
		}else {
			mRedView.setVisibility(View.INVISIBLE);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.record_btn:
			startRecord();
			break;
			
		case R.id.notice_btn:
			NoticePagerActivity.navigate();
			break;
		}
	}
	
	private void startRecord(){
		mRecordBtn.setClickable(false);
		AppLogic.runOnUiGround(new Runnable() {
			
			@Override
			public void run() {
				mRecordBtn.setClickable(true);
			}
		}, 1000);
		
		WinRecord.navigate();
		WinRecord.addRecordListener(this);
	}
	
	@Override
	public void onEnd() {
		// 录音界面关闭
	}
}