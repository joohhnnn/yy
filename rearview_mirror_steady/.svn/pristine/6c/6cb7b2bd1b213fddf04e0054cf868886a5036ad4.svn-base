package com.txznet.feedback.ui;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.ui.dialog.WinConfirm;
import com.txznet.feedback.AppLogic;
import com.txznet.feedback.R;
import com.txznet.feedback.data.Message;
import com.txznet.feedback.service.MsgService;
import com.txznet.feedback.service.RecordService;
import com.txznet.feedback.service.RecordService.OnMediaPlayListener;

public class NoticePagerActivity extends BaseActivity implements OnItemClickListener, OnItemLongClickListener, OnMediaPlayListener {

	private ListView mLv;
	private ImageView backIv;
	private NoticeMsgAdapter mAdapter;
	private List<Message> mMsgList;

	private int mDeletePosition;
	private View mPlayView;
	private WinConfirm mWinConfirm;
	private MsgReceiver mMsgReceiver;

	public static void navigate() {
		Intent intent = new Intent(AppLogic.getApp(), NoticePagerActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		AppLogic.getApp().startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		MsgService.getInstance().setIsNewIn(false);
		RecordService.getInstance().setOnMediaPlayListener(this);
		
		setContentView(R.layout.notice_pager_layout);

		backIv = (ImageView) findViewById(R.id.btnWeixin_Back);
		mLv = (ListView) findViewById(R.id.msg_lv);

		backIv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mWinConfirm = new WinConfirm() {

			@Override
			public void onClickOk() {
				doDelete();
			}

			@Override
			public void onClickCancel() {
				super.onClickCancel();
				mDeletePosition = -1;
			}

			@Override
			public void onClickBlank() {
				super.onClickBlank();
				mDeletePosition = -1;
			}
		}.setMessage("是否确定删除该记录？");

		mAdapter = new NoticeMsgAdapter();
		mLv.setAdapter(mAdapter);
		mLv.setOnItemClickListener(this);
		mLv.setOnItemLongClickListener(this);

		new MessageTask().execute();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		regist();
	}

	@Override
	protected void onDestroy() {
		unRegist();
		RecordService.getInstance().onPause();
		super.onDestroy();
	}
	
	private void regist(){
		IntentFilter filter = new IntentFilter();
		filter.addAction("UPDATE_MSG_DOWN");
		mMsgReceiver = new MsgReceiver();
		registerReceiver(mMsgReceiver, filter);
	}
	
	private void unRegist(){
		unregisterReceiver(mMsgReceiver);
	}

	private void doDelete() {
		if (mDeletePosition != -1) {
			Message msg = mMsgList.get(mDeletePosition);
			if (msg == null) {
				return;
			}

			boolean bSuccess = MsgService.getInstance().deleteMsg(msg.id);
			if (bSuccess) {
				new MessageTask().execute();
				if (msg.type == Message.TYPE_SELF) {
					RecordService.getInstance().deleteRealFile(msg.msg);
				}
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Message msg = (Message) mAdapter.getItem(position);
		if (msg == null) {
			return;
		}
		
		onEnd();

		mPlayView = view;

		// 点击自己的语音
		if (msg.type == Message.TYPE_SELF) {

			if (RecordService.getInstance().isPlaying()) {
				RecordService.getInstance().onPause();
			} else {
				RecordService.getInstance().playVoice(msg.msg);
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		
		// 删除对话框
//		if (!mWinConfirm.isShowing()) {
//			mWinConfirm.show();
//		}

		mDeletePosition = position;
		return true;
	}

	private class MessageTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			mMsgList = MsgService.getInstance().getMessageList();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mMsgList == null) {
				return;
			}

			mAdapter.setMsgList(mMsgList);
		}
	}

	@Override
	public void onPlay() {
		if(mPlayView == null){
			return;
		}
		
		ImageView iv = (ImageView) mPlayView.findViewById(R.id.anim_iv);
		if (iv == null) {
			return;
		}

		AnimationDrawable ad = (AnimationDrawable) iv.getDrawable();
		if (ad != null) {
			ad.start();
		}
	}

	@Override
	public void onEnd() {
		if(mPlayView == null){
			return;
		}
		
		ImageView iv = (ImageView) mPlayView.findViewById(R.id.anim_iv);
		if (iv == null) {
			return;
		}

		AnimationDrawable ad = (AnimationDrawable) iv.getDrawable();
		if (ad != null) {
			ad.stop();
			ad.selectDrawable(0);
		}
	}
	
	private class MsgReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			new MessageTask().execute();
		}
	}
	
	public static void sendMsgBroadCast(){
		Intent intent = new Intent("UPDATE_MSG_DOWN");
		AppLogic.getApp().sendBroadcast(intent);
	}
}